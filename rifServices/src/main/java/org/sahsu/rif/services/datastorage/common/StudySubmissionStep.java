package org.sahsu.rif.services.datastorage.common;

import org.json.JSONObject;

import org.sahsu.rif.services.system.files.TomcatBase;
import org.sahsu.rif.services.system.files.TomcatFile;
import org.sahsu.rif.services.util.Json5Parse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Array;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.InsertQueryFormatter;
import org.sahsu.rif.generic.datastorage.RecordExistsQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.UpdateQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AbstractRIFConcept.ValidationPolicy;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.CalculationMethod;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.MapArea;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Project;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.concepts.YearRange;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public final class StudySubmissionStep extends BaseSQLManager {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");

	private DiseaseMappingStudyManager diseaseMappingStudyManager;
	private MapDataManager mapDataManager;
	StringBuilder sqlWarnings = new StringBuilder();

	public StudySubmissionStep(
			final RIFServiceStartupOptions options,
			final DiseaseMappingStudyManager diseaseMappingStudyManager,
			final MapDataManager mapDataManager) {

		super(options);
		this.diseaseMappingStudyManager = diseaseMappingStudyManager;
		this.mapDataManager = mapDataManager;
		setEnableLogging(true);
	}
	
	public void updateSelectState(final Connection connection, final User user, final String studyID, final JSONObject studySelection) 
		throws RIFServiceException {
	
		if (studySelection == null) {
			throw new RIFServiceException(
					RIFServiceError.UPDATE_SELECTSTATE_FAILED,
					"updateSelectState studyID: " + studyID +
					"; studySelection: is NULL");
		}
		
		UpdateQueryFormatter updateSelectStateFormatter1 =
			UpdateQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());
			
		String studySelectionText=studySelection.toString();

		updateSelectStateFormatter1.setDatabaseSchemaName("rif40");
		if (rifDatabaseProperties.getDatabaseType() == DatabaseType.POSTGRESQL) { // Supports JSON natively
			updateSelectStateFormatter1.addUpdateField("select_state", "JSON");
		}
		else { // SQL Server doesn't yet
			updateSelectStateFormatter1.addUpdateField("select_state");
		}
		updateSelectStateFormatter1.setUpdateTable("rif40_studies");
		updateSelectStateFormatter1.addWhereParameter("study_id");

		logSQLQuery("updateSelectState", updateSelectStateFormatter1, studySelectionText, studyID);
	
		PreparedStatement statement1 = null;
		try {

			statement1 = connection.prepareStatement(updateSelectStateFormatter1.generateQuery());
			statement1.setString(1, studySelectionText);
			statement1.setInt(2, Integer.parseInt(studyID));
			int rc = statement1.executeUpdate();
		
			if (rc != 1) { 
				throw new RIFServiceException(
					RIFServiceError.DATABASE_UPDATE_FAILED,
					"updateSelectState query 1; expected 1 row, got none for rif40_studies.study_id: " + studyID + " update");
			}

		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"studySubmissionStep.unableToSetSelectState",
					studyID, studySelection.toString(2));
			throw new RIFServiceException(
					RIFServiceError.UPDATE_SELECTSTATE_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement1);
		}
	}
	
	public	void updatePrintState(final Connection connection, final User user, final String studyID) 
		throws RIFServiceException {
			
		UpdateQueryFormatter updateSelectStateFormatter1 =
			UpdateQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());
		
		updateSelectStateFormatter1.setDatabaseSchemaName("rif40");
		if (rifDatabaseProperties.getDatabaseType() == DatabaseType.POSTGRESQL) { // Supports JSON natively
			updateSelectStateFormatter1.addUpdateField("print_state", "JSON");
		}
		else { // SQL Server doesn't yet
			updateSelectStateFormatter1.addUpdateField("print_state");
		}
		updateSelectStateFormatter1.setUpdateTable("rif40_studies");
		updateSelectStateFormatter1.addWhereParameter("study_id");

		JSONObject printSelectionJson = null;
		PreparedStatement statement1 = null;
		try {

			BufferedReader reader = new TomcatFile(
					new TomcatBase(), TomcatFile.FRONT_END_PARAMETERS_FILE).reader();

			Json5Parse json5Parse = new Json5Parse(reader);
			String jsonText = json5Parse.toString();
		
			JSONObject json = json5Parse.toJson(); // Check it parses OK
			JSONObject parametersJson = json.optJSONObject("parameters");
			if (parametersJson == null) {
				throw new RIFServiceException(
					RIFServiceError.JSON_PARSE_ERROR,
					"updatePrintState json parse error: missing \"parameters\" key for rif40_studies.study_id: " + studyID + " update");
			}
			printSelectionJson = parametersJson.optJSONObject("mappingDefaults");
			if (printSelectionJson == null) {
				throw new RIFServiceException(
					RIFServiceError.JSON_PARSE_ERROR,
					"updatePrintState json parse error: missing \"mappingDefaults\" key for rif40_studies.study_id: " + studyID + " update");
			}
			String printSelectionText=printSelectionJson.toString();
			rifLogger.info(getClass(), "Retrieve printSelection from FrontEnd Parameters: " + printSelectionJson.toString(2));
			logSQLQuery("updateSelectState", updateSelectStateFormatter1, printSelectionText, studyID);
		
			statement1 = connection.prepareStatement(updateSelectStateFormatter1.generateQuery());
			statement1.setString(1, printSelectionText);
			statement1.setInt(2, Integer.parseInt(studyID));
			int rc = statement1.executeUpdate();
		
			if (rc != 1) { 
				throw new RIFServiceException(
					RIFServiceError.DATABASE_UPDATE_FAILED,
					"updatePrintState query 1; expected 1 row, got none for rif40_studies.study_id: " + studyID + " update");
			}

		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"studySubmissionStep.unableToSetPrintState",
					studyID, printSelectionJson.toString(2));
			throw new RIFServiceException(
					RIFServiceError.UPDATE_PRINTSTATE_FAILED,
					errorMessage);
		} catch(IOException ioException) {
			//Record original exception, throw sanitised, human-readable version
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".TomcatFile IO error", ioException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"studySubmissionStep.unableToSetPrintState",
					studyID, printSelectionJson.toString(2));
			throw new RIFServiceException(
					RIFServiceError.UPDATE_PRINTSTATE_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement1);
		}
	}
		
	String performStep(
			final Connection connection, final User user, final RIFStudySubmission studySubmission)
			throws Exception, RIFServiceException {

		studySubmission.checkErrors(ValidationPolicy.RELAXED);
		checkNonExistentItems(user, connection, studySubmission);
		
		//KLG: TODO: Later on we should not rely on casting - it might
		//be a risk analysis study
		String studyID=null;
		AbstractStudy study = studySubmission.getStudy();
		try {

			Project project = studySubmission.getProject();
			addGeneralInformationToStudy(
					connection,
					user,
					project,
					study,
					studySubmission);
			studyID = getCurrentStudyID(connection);

			addComparisonAreaToStudy(
					connection,
					study);

			addStudyAreaToStudy(
					connection,
					study);

			addInvestigationsToStudy(
					connection,
					study);

			connection.commit();

			rifLogger.info(this.getClass(), "XXXXXXXXXX Study create " + studyID + " OK XXXXXXXXXX");
			return studyID;
		} catch (Exception exception) {

			rifLogger.info(this.getClass(), "XXXXXXXXXX Study create " + studyID + " failed: " + exception.getMessage() + " XXXXXXXXXX");
			StringBuilder builder = new StringBuilder(exception.getMessage())
					                        .append(lineSeparator)
					                        .append("=============================================")
					                        .append(lineSeparator)
					                        .append("Stack trace of cause follows")
					                        .append(lineSeparator)
					                        .append("=============================================")
					                        .append(lineSeparator);
			for (StackTraceElement element : exception.getStackTrace()) {
				builder.append(element.toString()).append(lineSeparator);
			}
			builder.append("=============================================")
					                        .append(lineSeparator)
					                        .append("Output from PL/PGSQL")
					                        .append(lineSeparator)
					                        .append("=============================================")
					                        .append(lineSeparator)
											.append(sqlWarnings.toString());
			String stack=builder.toString();
		
			SQLQueryUtility.commit(connection);
			setStudyExtractToFail(connection, studyID, "Study create " + studyID + " failed", stack);
			
			throw exception;
		}
	}

	/*
	 * This function returns the study ID of the most recently created study
	 */
	private String getCurrentStudyID(final Connection connection)
			throws SQLException, RIFServiceException {

		String result;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryLine(0, "SELECT");
			queryFormatter.addQueryLine(
					1,
					rifDatabaseProperties.getDatabaseType().studyIdQuery());

			logSQLQuery("getCurrentStudyID", queryFormatter);

			statement = createPreparedStatement(connection, queryFormatter);
			resultSet = statement.executeQuery();
			resultSet.next();

			result = String.valueOf(resultSet.getInt(1));

			return result;
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}

	}

	private void addGeneralInformationToStudy(
			final Connection connection,
			final User user,
			final Project project,
			final AbstractStudy rifStudy,
			final RIFStudySubmission studySubmission)
			throws Exception {

		JSONObject studySelection = studySubmission.getStudySelection();
		int riskAnalysisType=studySelection.optInt("riskAnalysisType", -1);
		
		PreparedStatement studyShareStatement = null;
		PreparedStatement addStudyStatement = null;
		try {

			//add information about who can share the study
			InsertQueryFormatter studyQueryFormatter = InsertQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			studyQueryFormatter.setIntoTable("rif40.rif40_studies");
			studyQueryFormatter.addInsertField("geography");
			studyQueryFormatter.addInsertField("project");
			studyQueryFormatter.addInsertField("study_name");
			studyQueryFormatter.addInsertField("study_type");
			studyQueryFormatter.addInsertField("comparison_geolevel_name");
			studyQueryFormatter.addInsertField("study_geolevel_name");
			studyQueryFormatter.addInsertField("denom_tab");
			studyQueryFormatter.addInsertField("year_start");
			studyQueryFormatter.addInsertField("year_stop");
			studyQueryFormatter.addInsertField("max_age_group");
			studyQueryFormatter.addInsertField("min_age_group");
			studyQueryFormatter.addInsertField("suppression_value");
			studyQueryFormatter.addInsertField("extract_permitted");
			studyQueryFormatter.addInsertField("transfer_permitted");
			studyQueryFormatter.addInsertField("stats_method");

			logSQLQuery("addGeneralInformationToStudy", studyQueryFormatter);

			addStudyStatement = createPreparedStatement(connection, studyQueryFormatter);
			int ithQueryParameter = 1;

			Geography geography = rifStudy.getGeography();
			addStudyStatement.setString(ithQueryParameter++, geography.getName());

			addStudyStatement.setString(ithQueryParameter++, project.getName());

			addStudyStatement.setString(ithQueryParameter++, rifStudy.getName());

			//study type will be "1" for diseaseMappingStudy
			if (rifStudy.isDiseaseMapping()) {
				addStudyStatement.setInt(ithQueryParameter++, 1);
				addStudyStatement.setInt(ithQueryParameter++, 1); // disease mapping study
			}
			else if (riskAnalysisType == -1) {
				throw new Exception("No risk analysis type in studySelection JSON");
			}
			else if (riskAnalysisType == 11 || 
			         riskAnalysisType == 12 || 
					 riskAnalysisType == 13 || 
					 riskAnalysisType == 14 || 
					 riskAnalysisType == 15) {
				addStudyStatement.setInt(ithQueryParameter++, riskAnalysisType); // From studySelection JSON
				// 11 - Risk Analysis (many areas, one band), 
				// 12 - Risk Analysis (point sources), 
				// 13 - Risk Analysis (exposure covariates), 
				// 14 - Risk Analysis (coverage shapefile), 
				// 15 - Risk Analysis (exposure shapefile)
			}
			else {
				throw new Exception("Invalid risk analysis type in studySelection JSON: " + riskAnalysisType);
			}
			
			ComparisonArea comparisonArea = rifStudy.getComparisonArea();
			addStudyStatement.setString(ithQueryParameter++,
			                            comparisonArea.getGeoLevelToMap().getName());

			AbstractStudyArea diseaseMappingStudyArea =
					rifStudy.getStudyArea();
			addStudyStatement.setString(ithQueryParameter++,
			                            diseaseMappingStudyArea.getGeoLevelToMap().getName());

			//KLG: is this a good idea below - considering that each of the 
			//investigations can have different denominator tables?
			Investigation firstInvestigation = rifStudy.getInvestigations().get(0);
			NumeratorDenominatorPair ndPair = firstInvestigation.getNdPair();
			addStudyStatement.setString(ithQueryParameter++, ndPair.getDenominatorTableName());

			YearRange yearRange = firstInvestigation.getYearRange();
			//year_start
			addStudyStatement
					.setInt(ithQueryParameter++, Integer.valueOf(yearRange.getLowerBound()));
			//year_stop
			addStudyStatement
					.setInt(ithQueryParameter++, Integer.valueOf(yearRange.getUpperBound()));

			//max_age_group
			AgeGroup maximumAgeGroup = firstInvestigation.getMaximumAgeGroup();
			int maximumAgeGroupOffset = getOffsetFromAgeGroup(connection, ndPair, maximumAgeGroup);
			addStudyStatement.setInt(ithQueryParameter++, maximumAgeGroupOffset);

			//min_age_group
			AgeGroup minimumAgeGroup = firstInvestigation.getMinimumAgeGroup();
			int minimumAgeGroupOffset = getOffsetFromAgeGroup(connection, ndPair, minimumAgeGroup);
			addStudyStatement.setInt(ithQueryParameter++, minimumAgeGroupOffset);

			//KLG: Ask about this -- if we left it out would it get a value automatically?
			//for now, set suppression threshold to zero
			addStudyStatement.setInt(ithQueryParameter++, 0);

			//setting extract permitted
			addStudyStatement.setInt(ithQueryParameter++, 0);

			//setting transfer permitted
			addStudyStatement.setInt(ithQueryParameter++, 0);

			//setting stats method
			CalculationMethod calculationMethod = studySubmission.getCalculationMethods().get(0);
			// For some reason this is an array of one
			rifLogger.info(this.getClass(), "CalculationMethod name: " +
			                                calculationMethod.getName() + "; description: " +
			                                calculationMethod.getDescription() + "; stats method: "
			                                +
			                                calculationMethod.getStatsMethod()
			                                + "; code routine name: " +
			                                calculationMethod.getCodeRoutineName());
			addStudyStatement.setString(ithQueryParameter++, calculationMethod.getStatsMethod());

			addStudyStatement.executeUpdate();
			SQLQueryUtility.printWarnings(addStudyStatement); // Print output from T-SQL or PL/pgsql

			//add information about who can share the study
			InsertQueryFormatter studyShareQueryFormatter = InsertQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			studyShareQueryFormatter.setIntoTable("rif40.rif40_study_shares");
			studyShareQueryFormatter.addInsertField("grantee_username");

			studyShareStatement
					= createPreparedStatement(
					connection,
					studyShareQueryFormatter);
			studyShareStatement.setString(1, user.getUserID());
			studyShareStatement.executeUpdate();
			SQLQueryUtility.printWarnings(studyShareStatement); // Print output from T-SQL or PL/pgsql
			
		} catch(Exception exception) {
			if (addStudyStatement != null) {
				sqlWarnings.append(SQLQueryUtility.printWarnings(addStudyStatement) + lineSeparator);
			}
			if (studyShareStatement != null) {
				sqlWarnings.append(SQLQueryUtility.printWarnings(studyShareStatement) + lineSeparator); // Print output from PL/PGSQL
			}
			throw exception;
		} finally {
			//Cleanup database resources	
			SQLQueryUtility.close(studyShareStatement);
			SQLQueryUtility.close(addStudyStatement);
		}
	}

	private void addInvestigationsToStudy(
			final Connection connection,
			final AbstractStudy study)
			throws SQLException,
			       RIFServiceException {

		PreparedStatement statement = null;
		try {

			//we assume that the study is valid and that the caller has 
			//invoked rifStudySubmission.checkErrors();

			ArrayList<Investigation> investigations = study.getInvestigations();
			if (investigations.isEmpty()) {
				return;
			}

			InsertQueryFormatter queryFormatter = InsertQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			queryFormatter.setIntoTable("rif40.rif40_investigations");
			queryFormatter.addInsertField("inv_name");
			queryFormatter.addInsertField("inv_description");
			queryFormatter.addInsertField("genders");
			queryFormatter.addInsertField("numer_tab");
			queryFormatter.addInsertField("year_start");
			queryFormatter.addInsertField("year_stop");
			queryFormatter.addInsertField("max_age_group");
			queryFormatter.addInsertField("min_age_group");

			statement = createPreparedStatement(connection, queryFormatter);

			FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();
			for (Investigation investigation : investigations) {

				//extracting field values that will be used for the query

				String invNameParameter
						= fieldValidationUtility
								  .convertToDatabaseTableName(investigation.getTitle());
				String invDescriptionParameter
						= investigation.getDescription();
				Sex sex = investigation.getSex();
				Integer genderCodeParameter = null;
				if (sex == Sex.MALES) {
					genderCodeParameter = 1;
				} else if (sex == Sex.FEMALES) {
					genderCodeParameter = 2;
				} else {
					genderCodeParameter = 3;
				}

				NumeratorDenominatorPair ndPair = investigation.getNdPair();
				String numerTabParameter
						= ndPair.getNumeratorTableName();

				YearRange yearRange = investigation.getYearRange();
				Integer yearStartParameter = Integer.valueOf(yearRange.getLowerBound());
				Integer yearStopParameter = Integer.valueOf(yearRange.getUpperBound());

				AgeGroup maximumAgeGroup = investigation.getMaximumAgeGroup();
				int maxAgeGroupParameter
						= getOffsetFromAgeGroup(
						connection,
						ndPair,
						maximumAgeGroup);

				AgeGroup minimumAgeGroup = investigation.getMinimumAgeGroup();
				int minAgeGroupParameter
						= getOffsetFromAgeGroup(
						connection,
						ndPair,
						minimumAgeGroup);

				logSQLQuery(
						"addInvestigation",
						queryFormatter,
						invNameParameter,
						invDescriptionParameter,
						String.valueOf(genderCodeParameter),
						numerTabParameter,
						String.valueOf(yearStartParameter),
						String.valueOf(yearStopParameter),
						String.valueOf(maxAgeGroupParameter),
						String.valueOf(minAgeGroupParameter));

				//setting the parameters
				int ithQueryParameter = 1;
				statement.setString(
						ithQueryParameter++,
						invNameParameter);
				statement.setString(
						ithQueryParameter++,
						invDescriptionParameter);
				statement.setInt(ithQueryParameter++, genderCodeParameter);
				statement.setString(ithQueryParameter++, numerTabParameter);
				//year_start
				statement.setInt(
						ithQueryParameter++,
						yearStartParameter);
				//year_stop
				statement.setInt(
						ithQueryParameter++,
						yearStopParameter);
				//max_age_group						
				statement.setInt(
						ithQueryParameter++,
						maxAgeGroupParameter);
				//min_age_group				
				statement.setInt(
						ithQueryParameter++,
						minAgeGroupParameter);

				statement.executeUpdate();
				SQLQueryUtility.printWarnings(statement); // Print output from T-SQL or PL/pgsql

				addCovariatesToStudy(
						connection,
						study,
						investigation);

				//now add health outcomes for this investigation
				addHealthOutcomes(
						connection,
						study,
						investigation);
			}
		} catch(Exception exception) {	
			if (statement != null) {
				sqlWarnings.append(SQLQueryUtility.printWarnings(statement) + lineSeparator); // Print output from PL/PGSQL
			}
			throw exception;			
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
		}

	}

	/*
	 * A convenience method that assumes the parameters have already been checked
	 */
	private int getOffsetFromAgeGroup(
			final Connection connection,
			final NumeratorDenominatorPair ndPair,
			final AgeGroup ageGroup)
			throws SQLException,
			       RIFServiceException {

		SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
				rifDatabaseProperties.getDatabaseType());

		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.addSelectField("\"offset\"");
		queryFormatter.addFromTable("rif40_age_groups");
		queryFormatter.addFromTable("rif40_tables");
		queryFormatter.addWhereJoinCondition(
				"rif40_age_groups",
				"age_group_id",
				"rif40_tables",
				"age_group_id");
		queryFormatter.addWhereParameter("rif40_tables", "isnumerator");
		queryFormatter.addWhereParameter("rif40_tables", "table_name");
		queryFormatter.addWhereParameter("rif40_age_groups", "fieldname");

		logSQLQuery(
				"getOffsetFromAgeGroup",
				queryFormatter,
				"1",
				ndPair.getNumeratorTableName(),
				ageGroup.getName());

		//here it doesn't matter which of numerator or denominator table we get
		//In any ndPair, both numerator and denominator should recognise the same
		//age group classifications
		String numeratorTableName
				= ndPair.getNumeratorTableName();

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = createPreparedStatement(connection, queryFormatter);
			statement.setInt(1, 1);
			statement.setString(2, numeratorTableName);
			statement.setString(3, ageGroup.getName());
			resultSet = statement.executeQuery();
			resultSet.next();
			return resultSet.getInt(1);
		} finally {
			SQLQueryUtility.close(statement);
		}
	}

	private void addStudyAreaToStudy(
			final Connection connection,
			final AbstractStudy diseaseMappingStudy)
			throws Exception {

		PreparedStatement statement = null;
		try {

			Geography geography = diseaseMappingStudy.getGeography();
			AbstractStudyArea diseaseMappingStudyArea
					= diseaseMappingStudy.getStudyArea();

			ArrayList<MapArea> allMapAreas
					= mapDataManager.getAllRelevantMapAreas(
					connection,
					geography,
					diseaseMappingStudyArea,
					true /* StudyArea */);

			InsertQueryFormatter queryFormatter = InsertQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			queryFormatter.setIntoTable("rif40.rif40_study_areas");
			queryFormatter.addInsertField("area_id");
			queryFormatter.addInsertField("band_id");

			logSQLQuery(
					"addStudyAreaToStudy",
					queryFormatter);

			statement
					= createPreparedStatement(
					connection,
					queryFormatter);
			
//			DatabaseType databaseType = rifDatabaseProperties.getDatabaseType();
			ArrayList<String> list1 = new ArrayList<String>();
			ArrayList<Integer> list2 = new ArrayList<Integer>();
			for (MapArea currentMapArea : allMapAreas) {
				list1.add(currentMapArea.getLabel());
				list2.add(currentMapArea.getBand());
			}
			String list1String = String.join(", ", list1);
			String list2String = list2.stream().map(Object::toString)
					.collect(Collectors.joining(", "));
//			if (databaseType == DatabaseType.POSTGRESQL) { 	// Do array insert: not possible, see: https://github.com/swaldman/c3p0/issues/88
//				Array array1 = connection.createArrayOf("VARCHAR", list1.toArray());
//				Array array2 = connection.createArrayOf("INTEGER", list2.toArray());
//				statement.setArray(1, array1);
//				statement.setArray(2, array2);
//
//				rifLogger.info(this.getClass(), "Do Postgres study area array insert; 1: " + list1.size() + "; " + 
//					(list1String.length() > 100 ? list1String.substring(0, 100) : list1String) +				
//					"; 2: " + list2.size() + "; " + 
//					(list2String.length() > 100 ? list2String.substring(0, 100) : list2String));
//				statement.executeUpdate();
//			}
//			else if (databaseType == DatabaseType.SQL_SERVER) { 	// Don't or you will get:
													// java.sql.SQLFeatureNotSupportedException: This operation is not supported.
													//		at com.microsoft.sqlserver.jdbc.SQLServerConnection.createArrayOf(SQLServerConnection.java:5073)

//				rifLogger.info(this.getClass(), "Done SQL Server study area non array insert; 1: " + 
				rifLogger.info(this.getClass(), "Done study area non array insert; 1: " + 
					(list1String.length() > 100 ? list1String.substring(0, 100) : list1String) +				
					"; 2: " + list2.size() + "; " + 
					(list2String.length() > 100 ? list2String.substring(0, 100) : list2String));
				for (MapArea currentMapArea : allMapAreas) {
					statement.setString(1, currentMapArea.getLabel());
					statement.setInt(2, currentMapArea.getBand());
					statement.executeUpdate();
				}
//			}
//			else {
//				throw new IllegalStateException("Unknown database type in "
//				                                + "GenerateResultsSubmissionStep");
//			}			
			rifLogger.info(this.getClass(), "addStudyAreaToStudy() OK");
		} catch(Exception exception) {
			rifLogger.info(this.getClass(), "addStudyAreaToStudy() FAILED: " + exception.getMessage());
			if (statement != null) {
				sqlWarnings.append(SQLQueryUtility.printWarnings(statement) + lineSeparator); // Print output from PL/PGSQL
			}
			throw exception;	
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
		}
	}

	private void addComparisonAreaToStudy(
			final Connection connection,
			final AbstractStudy diseaseMappingStudy)
			throws Exception {

		PreparedStatement statement = null;
		try {
			InsertQueryFormatter queryFormatter = InsertQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			queryFormatter.setIntoTable("rif40.rif40_comparison_areas");
			queryFormatter.addInsertField("area_id");
			logSQLQuery(
					"addComparisonAreaToStudy",
					queryFormatter);
					
			statement
					= createPreparedStatement(
					connection,
					queryFormatter);

			Geography geography
					= diseaseMappingStudy.getGeography();

			ComparisonArea comparisonArea
					= diseaseMappingStudy.getComparisonArea();

			/*
			 * The user may have selected areas at a higher resolution
			 * (geo level select) but want the actual map areas that are
			 * specified in the study to be at a much lower level.  For example,
			 * we could specify geo level select= "district", but have a geo level
			 * to map resolution of ward level.
			 */
			ArrayList<MapArea> allMapAreas
					= mapDataManager.getAllRelevantMapAreas(
					connection,
					geography,
					comparisonArea,
					false /* ComparisonArea */);
					
//			DatabaseType databaseType = rifDatabaseProperties.getDatabaseType();
			ArrayList<String> list = new ArrayList<String>();
			for (MapArea currentMapArea : allMapAreas) {
				list.add(currentMapArea.getLabel());
			}	
			String listString = String.join(", ", list);
//			if (databaseType == DatabaseType.POSTGRESQL) { 	// Do array insert: not possible, see: https://github.com/swaldman/c3p0/issues/88
//	
//
//				Array array = connection.createArrayOf("VARCHAR", list.toArray());
//				statement.setArray(1, array);
//				rifLogger.info(this.getClass(), "Do Postgres comparison area array insert; 1: " + list.size() + 
//					"; " + (listString.length() > 100 ? listString.substring(0, 100) : listString));
//				statement.executeUpdate();
//			}
//			else if (databaseType == DatabaseType.SQL_SERVER) { 	// Don't or you will get:
													// java.sql.SQLFeatureNotSupportedException: This operation is not supported.
													//		at com.microsoft.sqlserver.jdbc.SQLServerConnection.createArrayOf(SQLServerConnection.java:5073)
				for (MapArea currentMapArea : allMapAreas) {
					statement.setString(1, currentMapArea.getLabel());
					statement.executeUpdate();
				}
//				rifLogger.info(this.getClass(), "Done SQL Server comparison area non array insert; 1: " + list.size() + 
//					"; " + (listString.length() > 100 ? listString.substring(0, 100) : listString));
				rifLogger.info(this.getClass(), "Done comparison area non array insert; 1: " + list.size() + 
					"; " + (listString.length() > 100 ? listString.substring(0, 100) : listString));
//			}
//			else {
//				throw new IllegalStateException("Unknown database type in "
//				                                + "GenerateResultsSubmissionStep");
//			}
//		} catch (Exception e) {		
//			logException(e);	
//			throw e;	
		}
		catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(Exception exception) {
			sqlWarnings.append(SQLQueryUtility.printWarnings(statement) + lineSeparator); // Print output from PL/PGSQL
			throw exception;		
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
		}

	}

	private void addCovariatesToStudy(
			final Connection connection,
			final AbstractStudy study,
			final Investigation investigation)
			throws SQLException,
			       RIFServiceException {

		PreparedStatement getMinMaxCovariateValueStatement = null;
		PreparedStatement addCovariateStatement = null;
		try {

			SelectQueryFormatter getMinMaxCovariateValuesQueryFormatter =
					SelectQueryFormatter.getInstance(
							rifDatabaseProperties.getDatabaseType());
			getMinMaxCovariateValuesQueryFormatter.addSelectField("min");
			getMinMaxCovariateValuesQueryFormatter.addSelectField("max");
			getMinMaxCovariateValuesQueryFormatter.addFromTable("rif40.rif40_covariates");
			getMinMaxCovariateValuesQueryFormatter.addWhereParameter("geography");
			getMinMaxCovariateValuesQueryFormatter.addWhereParameter("geolevel_name");
			getMinMaxCovariateValuesQueryFormatter.addWhereParameter("covariate_name");

			InsertQueryFormatter addCovariateQueryFormatter = InsertQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			addCovariateQueryFormatter.setIntoTable("rif40.rif40_inv_covariates");
			addCovariateQueryFormatter.addInsertField("geography");
			addCovariateQueryFormatter.addInsertField("covariate_name");
			addCovariateQueryFormatter.addInsertField("study_geolevel_name");
			addCovariateQueryFormatter.addInsertField("min");
			addCovariateQueryFormatter.addInsertField("max");

			getMinMaxCovariateValueStatement
					= createPreparedStatement(
					connection,
					getMinMaxCovariateValuesQueryFormatter);

			addCovariateStatement
					= createPreparedStatement(
					connection,
					addCovariateQueryFormatter);
			int ithQueryParameter = 1;

			Geography geography = study.getGeography();
			String geographyName = geography.getName();

			AbstractStudyArea diseaseMappingStudyArea
					= study.getStudyArea();
			String studyGeoLevelName
					= diseaseMappingStudyArea.getGeoLevelToMap().getName();

			ArrayList<AbstractCovariate> covariates
					= investigation.getCovariates();
			ResultSet getMinMaxCovariateValueResultSet = null;
			for (AbstractCovariate covariate : covariates) {

				logSQLQuery(
						"getMinMaxCovariateValue",
						getMinMaxCovariateValuesQueryFormatter,
						geographyName,
						studyGeoLevelName,
						covariate.getName().toUpperCase());

				getMinMaxCovariateValueStatement.setString(1, geographyName);
				getMinMaxCovariateValueStatement.setString(2, studyGeoLevelName);
				getMinMaxCovariateValueStatement.setString(3, covariate.getName().toUpperCase());
				//we can assume that the covariate will exist
				getMinMaxCovariateValueResultSet
						= getMinMaxCovariateValueStatement.executeQuery();
				getMinMaxCovariateValueResultSet.next();
				Double minimumCovariateValue = getMinMaxCovariateValueResultSet.getDouble(1);
				Double maximumCovariateValue = getMinMaxCovariateValueResultSet.getDouble(2);
				getMinMaxCovariateValueResultSet.close();

				logSQLQuery(
						"addCovariateValue",
						addCovariateQueryFormatter,
						geographyName,
						covariate.getName().toUpperCase(),
						studyGeoLevelName,
						String.valueOf(minimumCovariateValue),
						String.valueOf(maximumCovariateValue));

				addCovariateStatement.setString(
						ithQueryParameter++,
						geographyName);
				addCovariateStatement.setString(
						ithQueryParameter++,
						covariate.getName().toUpperCase());
				addCovariateStatement.setString(
						ithQueryParameter++,
						studyGeoLevelName);
				addCovariateStatement.setDouble(
						ithQueryParameter++,
						minimumCovariateValue);
				addCovariateStatement.setDouble(
						ithQueryParameter++,
						maximumCovariateValue);
				addCovariateStatement.executeUpdate();
				SQLQueryUtility.printWarnings(addCovariateStatement); // Print output from T-SQL or PL/pgsql
				ithQueryParameter = 1;
			}
		} catch(Exception exception) {
			if (addCovariateStatement != null) {
				sqlWarnings.append(SQLQueryUtility.printWarnings(addCovariateStatement) + lineSeparator); // Print output from PL/PGSQL
			}
			throw exception;				
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(addCovariateStatement);
		}
	}

	private void addHealthOutcomes(
			final Connection connection,
			final AbstractStudy study,
			final Investigation investigation)
			throws SQLException,
			       RIFServiceException {

		PreparedStatement getOutcomeGroupNameStatement = null;
		ResultSet getOutcomeGroupNameResultSet = null;
		PreparedStatement addHealthCodeStatement = null;
		try {

			SelectQueryFormatter getOutcomeGroupNameQueryFormatter =
					SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());
			getOutcomeGroupNameQueryFormatter.addSelectField("outcome_group_name");
			getOutcomeGroupNameQueryFormatter.addSelectField("field_name");
			getOutcomeGroupNameQueryFormatter.addFromTable
					                                  ("rif40.rif40_numerator_outcome_columns");
			getOutcomeGroupNameQueryFormatter.addWhereParameter("geography");
			getOutcomeGroupNameQueryFormatter.addWhereParameter("table_name");

			Geography geography = study.getGeography();
			NumeratorDenominatorPair ndPair = investigation.getNdPair();

			logSQLQuery(
					"getOutcomeGroupName",
					getOutcomeGroupNameQueryFormatter,
					geography.getName(),
					ndPair.getNumeratorTableName());

			getOutcomeGroupNameStatement
					= createPreparedStatement(
					connection,
					getOutcomeGroupNameQueryFormatter);
			getOutcomeGroupNameStatement.setString(1, geography.getName());
			getOutcomeGroupNameStatement.setString(2, ndPair.getNumeratorTableName());
			getOutcomeGroupNameResultSet
					= getOutcomeGroupNameStatement.executeQuery();
			getOutcomeGroupNameResultSet.next();
			String outcomeGroupName
					= getOutcomeGroupNameResultSet.getString(1);

			//determine what kinds of codes the numerator table supports

			ArrayList<HealthCode> healthCodes
					= investigation.getHealthCodes();
			int totalHealthCodes = healthCodes.size();

			//KLG: TODO: try adding one health code maximum
			//TODO: (DM) if multiple conditions supplied in currentHealthCode.getCode() 
			if (totalHealthCodes > 0) {

				InsertQueryFormatter addHealthOutcomeQueryFormatter =
						InsertQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

				addHealthOutcomeQueryFormatter.setIntoTable("rif40.rif40_inv_conditions");
				addHealthOutcomeQueryFormatter.addInsertField("outcome_group_name");
				addHealthOutcomeQueryFormatter.addInsertField("min_condition");
				addHealthOutcomeQueryFormatter.addInsertField("max_condition");
				addHealthOutcomeQueryFormatter.addInsertField("predefined_group_name");
				addHealthOutcomeQueryFormatter.addInsertField("line_number");

				for (int i = 1; i <= totalHealthCodes; i++) {
					HealthCode currentHealthCode = healthCodes.get(i - 1);

					rifLogger.info(this.getClass(),
					               "XXXXXXXXXX currentHealthCode XXXXXXXXXXXXXXXXXXXXXX" + lineSeparator +
					               "Code: " + currentHealthCode.getCode() +
					               "; namespace: " + currentHealthCode.getNameSpace() +
					               "; isTopLevelTerm: " + currentHealthCode.isTopLevelTerm() + lineSeparator +
					               "; description: " + currentHealthCode.getDescription() +
					               "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

					addHealthCodeStatement
							= createPreparedStatement(
							connection,
							addHealthOutcomeQueryFormatter);
							
					if (currentHealthCode.getCode().contains("-")) { // 
						String minCondition=currentHealthCode.getCode().substring(0, currentHealthCode.getCode().indexOf("-"));
						String maxCondition=currentHealthCode.getCode().substring(currentHealthCode.getCode().indexOf("-")+1);
						logSQLQuery(
								"add_inv_condition",
								addHealthOutcomeQueryFormatter,
								outcomeGroupName,
								minCondition,
								maxCondition,
								null, //predefined_group_name not supported yet
								String.valueOf(i));

						addHealthCodeStatement.setString(1, outcomeGroupName);
						addHealthCodeStatement.setString(2, minCondition);
						addHealthCodeStatement.setString(3, maxCondition);
						addHealthCodeStatement.setString(4, null);
						addHealthCodeStatement.setInt(5, i);
					}
					else {
						logSQLQuery(
								"add_inv_condition",
								addHealthOutcomeQueryFormatter,
								outcomeGroupName,
								currentHealthCode.getCode(),
								null, //max_condition not supported yet
								null, //predefined_group_name not supported yet
								String.valueOf(i));
						addHealthCodeStatement.setString(1, outcomeGroupName);
						addHealthCodeStatement.setString(2, currentHealthCode.getCode());
						addHealthCodeStatement.setString(3, null);
						addHealthCodeStatement.setString(4, null);
						addHealthCodeStatement.setInt(5, i);

					}

					addHealthCodeStatement.executeUpdate();
					SQLQueryUtility.printWarnings(addHealthCodeStatement); // Print output from T-SQL or PL/pgsql
				}
			}
		} catch(Exception exception) {
			if (getOutcomeGroupNameStatement != null) {
				sqlWarnings.append(SQLQueryUtility.printWarnings(getOutcomeGroupNameStatement) + lineSeparator); // Print output from PL/PGSQL
			}
			if (addHealthCodeStatement != null) {
				sqlWarnings.append(SQLQueryUtility.printWarnings(addHealthCodeStatement) + lineSeparator); // Print output from PL/PGSQL
			}
			throw exception;				
		} finally {
			//Cleanup database resources	
			SQLQueryUtility.close(getOutcomeGroupNameStatement);
			SQLQueryUtility.close(addHealthCodeStatement);
		}
	}

	private void checkNonExistentItems(
			final User user,
			final Connection connection,
			final RIFStudySubmission rifStudySubmission)
			throws RIFServiceException {

		Project project
				= rifStudySubmission.getProject();
		checkProjectExists(
				connection,
				project);

		AbstractStudy study = rifStudySubmission.getStudy();
		diseaseMappingStudyManager.checkNonExistentItems(user, connection, study);

		ArrayList<CalculationMethod> calculationMethods
				= rifStudySubmission.getCalculationMethods();
		for (CalculationMethod calculationMethod : calculationMethods) {
			checkCalculationMethodExists(
					connection,
					calculationMethod);
		}

		//we can assume that all rif output options that are supplied
		//do in fact exist because they are taken from an enumerated type
	}

	private void checkProjectExists(
			final Connection connection,
			final Project project)
			throws RIFServiceException {

		//Parameterise and execute query		
		PreparedStatement checkProjectExistsStatement = null;
		ResultSet checkProjectExistsResultSet = null;
		try {
			//Create SQL query
			RecordExistsQueryFormatter queryFormatter
					= RecordExistsQueryFormatter
							  .getInstance(rifDatabaseProperties.getDatabaseType());
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setLookupKeyFieldName("project");
			queryFormatter.setFromTable("rif40.rif40_projects");

			logSQLQuery(
					"checkProjectExists",
					queryFormatter,
					project.getName());

			checkProjectExistsStatement
					= createPreparedStatement(
					connection,
					queryFormatter);

			checkProjectExistsStatement.setString(1, project.getName());
			checkProjectExistsResultSet
					= checkProjectExistsStatement.executeQuery();

			if (!checkProjectExistsResultSet.next()) {
				//ERROR: no such project exists
				String recordType
						= RIFServiceMessages.getMessage("project.label");
				String errorMessage
						= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						project.getName());
				RIFServiceException rifServiceException
						= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_PROJECT,
						errorMessage);

				connection.commit();

				throw rifServiceException;
			}

			connection.commit();

		} catch (SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String recordType
					= RIFServiceMessages.getMessage("project.label");
			String errorMessage
					= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					project.getName());

			rifLogger.error(
					getClass(),
					errorMessage,
					sqlException);

			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(checkProjectExistsStatement);
			SQLQueryUtility.close(checkProjectExistsResultSet);
		}
	}

	private void checkCalculationMethodExists(
			final Connection connection,
			final CalculationMethod calculationMethod)
			throws RIFServiceException {

		//@TODO: Implement when RIF is able to register R routines
	}
	
	public void setStudyExtractToFail(final Connection connection, final String studyID, final String message, final String stack)
		throws RIFServiceException {

		InsertQueryFormatter studyQueryFormatter = InsertQueryFormatter.getInstance(
				rifDatabaseProperties.getDatabaseType());
		studyQueryFormatter.setIntoTable("rif40.rif40_study_status");
		studyQueryFormatter.addInsertField("study_id");
		studyQueryFormatter.addInsertField("study_state");
		studyQueryFormatter.addInsertField("message");
		if (stack != null) {
			studyQueryFormatter.addInsertField("trace");
		}

		logSQLQuery("setStudyExtractToFail", studyQueryFormatter);

		PreparedStatement statement1 = null;
	
		try {
			statement1 = connection.prepareStatement(studyQueryFormatter.generateQuery());
			statement1.setInt(1, Integer.parseInt(studyID));
			statement1.setString(2, "G"); //  Extract failure, extract, results or maps not created
			if (message != null) {
				statement1.setString(3, message);
			}
			else {
				statement1.setString(3, "Extract failure, extract, results or maps not created");
			}
			if (stack != null) {
				statement1.setString(4, stack);
			}
			int rc = statement1.executeUpdate();
		
			if (rc != 1) { 
				throw new RIFServiceException(
					RIFServiceError.SETSTUDYEXTRACTTOFAIL_FAILED,
					"setStudyExtractToFail query 1; expected 1 row, got none for rif40_studies.study_id: " + studyID + " insert");
			}
			connection.commit();

		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"studySubmissionStep.unableToSetStudyExtractToFail",
					studyID, message);
			throw new RIFServiceException(
					RIFServiceError.SETSTUDYEXTRACTTOFAIL_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement1);
		}
	}
	
}
