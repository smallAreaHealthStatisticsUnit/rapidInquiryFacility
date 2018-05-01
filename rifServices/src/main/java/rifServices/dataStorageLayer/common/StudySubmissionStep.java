package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.RecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SelectQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.ms.MSSQLInsertQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.ComparisonArea;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudyArea;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.MapArea;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.Sex;
import rifServices.businessConceptLayer.YearRange;
import rifServices.dataStorageLayer.common.BaseSQLManager;
import rifServices.dataStorageLayer.common.DiseaseMappingStudyManager;
import rifServices.dataStorageLayer.common.MapDataManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

public final class StudySubmissionStep extends BaseSQLManager {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");

	private DiseaseMappingStudyManager diseaseMappingStudyManager;
	private MapDataManager mapDataManager;

	public StudySubmissionStep(
			final RIFServiceStartupOptions options,
			final DiseaseMappingStudyManager diseaseMappingStudyManager,
			final MapDataManager mapDataManager) {

		super(options);
		this.diseaseMappingStudyManager = diseaseMappingStudyManager;
		this.mapDataManager = mapDataManager;
		setEnableLogging(true);
	}

	public String performStep(
			final Connection connection, final User user, final RIFStudySubmission studySubmission)
			throws RIFServiceException {

		studySubmission.checkErrors(ValidationPolicy.RELAXED);
		checkNonExistentItems(user, connection, studySubmission);

		//KLG: TODO: Later on we should not rely on casting - it might
		//be a risk analysis study
		String result;
		DiseaseMappingStudy diseaseMappingStudy = (DiseaseMappingStudy) studySubmission.getStudy();
		try {

			Project project = studySubmission.getProject();
			addGeneralInformationToStudy(
					connection,
					user,
					project,
					diseaseMappingStudy,
					studySubmission);

			addComparisonAreaToStudy(
					connection,
					diseaseMappingStudy);

			addStudyAreaToStudy(
					connection,
					diseaseMappingStudy);

			addInvestigationsToStudy(
					connection,
					diseaseMappingStudy);

			result = getCurrentStudyID(connection);
			connection.commit();

			rifLogger.info(this.getClass(),
			               "======SQLCREATESTUDYSUBMISSIONSTEP====studyID==" + result + "==");
			return result;
		} catch (SQLException sqlException) {
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddStudySubmission",
					diseaseMappingStudy.getDisplayName());
			RIFServiceException rifServiceException
					= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
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
					"rif40.rif40_sequence_current_value('rif40.rif40_study_id_seq')");

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
			final DiseaseMappingStudy diseaseMappingStudy,
			final RIFStudySubmission studySubmission)
			throws SQLException, RIFServiceException {

		PreparedStatement studyShareStatement = null;
		PreparedStatement addStudyStatement = null;
		try {

			//add information about who can share the study
			MSSQLInsertQueryFormatter studyQueryFormatter
					= new MSSQLInsertQueryFormatter(false);
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

			Geography geography = diseaseMappingStudy.getGeography();
			addStudyStatement.setString(ithQueryParameter++, geography.getName());

			addStudyStatement.setString(ithQueryParameter++, project.getName());

			addStudyStatement.setString(ithQueryParameter++, diseaseMappingStudy.getName());

			//study type will be "1" for diseaseMappingStudy
			addStudyStatement.setInt(ithQueryParameter++, 1);

			ComparisonArea comparisonArea = diseaseMappingStudy.getComparisonArea();
			addStudyStatement.setString(ithQueryParameter++,
			                            comparisonArea.getGeoLevelToMap().getName());

			DiseaseMappingStudyArea diseaseMappingStudyArea =
					diseaseMappingStudy.getDiseaseMappingStudyArea();
			addStudyStatement.setString(ithQueryParameter++,
			                            diseaseMappingStudyArea.getGeoLevelToMap().getName());

			//KLG: is this a good idea below - considering that each of the 
			//investigations can have different denominator tables?
			Investigation firstInvestigation = diseaseMappingStudy.getInvestigations().get(0);
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

			//add information about who can share the study
			MSSQLInsertQueryFormatter studyShareQueryFormatter
					= new MSSQLInsertQueryFormatter(false);
			studyShareQueryFormatter.setIntoTable("rif40.rif40_study_shares");
			studyShareQueryFormatter.addInsertField("grantee_username");

			studyShareStatement
					= createPreparedStatement(
					connection,
					studyShareQueryFormatter);
			studyShareStatement.setString(1, user.getUserID());
			studyShareStatement.executeUpdate();
		} finally {
			//Cleanup database resources	
			SQLQueryUtility.close(studyShareStatement);
			SQLQueryUtility.close(addStudyStatement);
		}
	}

	private void addInvestigationsToStudy(
			final Connection connection,
			final DiseaseMappingStudy diseaseMappingStudy)
			throws SQLException,
			       RIFServiceException {

		PreparedStatement statement = null;
		try {

			//we assume that the study is valid and that the caller has 
			//invoked rifStudySubmission.checkErrors();

			ArrayList<Investigation> investigations = diseaseMappingStudy.getInvestigations();
			if (investigations.isEmpty()) {
				return;
			}

			MSSQLInsertQueryFormatter queryFormatter = new MSSQLInsertQueryFormatter(false);
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

				addCovariatesToStudy(
						connection,
						diseaseMappingStudy,
						investigation);

				//now add health outcomes for this investigation
				addHealthOutcomes(
						connection,
						diseaseMappingStudy,
						investigation);
			}
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
			final DiseaseMappingStudy diseaseMappingStudy)
			throws SQLException,
			       RIFServiceException {

		PreparedStatement statement = null;
		try {

			Geography geography = diseaseMappingStudy.getGeography();
			DiseaseMappingStudyArea diseaseMappingStudyArea
					= diseaseMappingStudy.getDiseaseMappingStudyArea();

			ArrayList<MapArea> allMapAreas
					= mapDataManager.getAllRelevantMapAreas(
					connection,
					geography,
					diseaseMappingStudyArea);

			MSSQLInsertQueryFormatter queryFormatter
					= new MSSQLInsertQueryFormatter(false);
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
			int i = 1;
			for (MapArea currentMapArea : allMapAreas) {
				statement.setString(1, currentMapArea.getLabel());

				statement.setInt(2, i);

				statement.executeUpdate();

				i++;
			}
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
		}
	}

	private void addComparisonAreaToStudy(
			final Connection connection,
			final DiseaseMappingStudy diseaseMappingStudy)
			throws SQLException,
			       RIFServiceException {

		PreparedStatement statement = null;
		try {
			MSSQLInsertQueryFormatter queryFormatter
					= new MSSQLInsertQueryFormatter(false);
			queryFormatter.setIntoTable("rif40.rif40_comparison_areas");
			queryFormatter.addInsertField("area_id");

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
					comparisonArea);

			for (MapArea currentMapArea : allMapAreas) {
				statement.setString(1, currentMapArea.getLabel());
				statement.executeUpdate();
			}
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
		}

	}

	private void addCovariatesToStudy(
			final Connection connection,
			final DiseaseMappingStudy diseaseMappingStudy,
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

			MSSQLInsertQueryFormatter addCovariateQueryFormatter
					= new MSSQLInsertQueryFormatter(false);
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

			Geography geography = diseaseMappingStudy.getGeography();
			String geographyName = geography.getName();

			DiseaseMappingStudyArea diseaseMappingStudyArea
					= diseaseMappingStudy.getDiseaseMappingStudyArea();
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
				ithQueryParameter = 1;
			}
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(addCovariateStatement);
		}
	}

	private void addHealthOutcomes(
			final Connection connection,
			final DiseaseMappingStudy diseaseMappingStudy,
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

			Geography geography = diseaseMappingStudy.getGeography();
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

				MSSQLInsertQueryFormatter addHealthOutcomeQueryFormatter
						= new MSSQLInsertQueryFormatter(false);

				addHealthOutcomeQueryFormatter.setIntoTable("rif40.rif40_inv_conditions");
				addHealthOutcomeQueryFormatter.addInsertField("outcome_group_name");
				addHealthOutcomeQueryFormatter.addInsertField("min_condition");
				addHealthOutcomeQueryFormatter.addInsertField("max_condition");
				addHealthOutcomeQueryFormatter.addInsertField("predefined_group_name");
				addHealthOutcomeQueryFormatter.addInsertField("line_number");

				for (int i = 1; i <= totalHealthCodes; i++) {
					HealthCode currentHealthCode = healthCodes.get(i - 1);

					rifLogger.info(this.getClass(),
					               "XXXXXXXXXX currentHealthCode XXXXXXXXXXXXXXXXXXXXXX"
					               + lineSeparator +
					               currentHealthCode + lineSeparator +
					               "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

					logSQLQuery(
							"add_inv_condition",
							addHealthOutcomeQueryFormatter,
							outcomeGroupName,
							currentHealthCode.getCode(),
							null, //max_condition not supported yet
							null, //predefined_group_name not supported yet
							String.valueOf(i));

					addHealthCodeStatement
							= createPreparedStatement(
							connection,
							addHealthOutcomeQueryFormatter);
					addHealthCodeStatement.setString(1, outcomeGroupName);
					addHealthCodeStatement.setString(2, currentHealthCode.getCode());
					addHealthCodeStatement.setString(3, null);
					addHealthCodeStatement.setString(4, null);
					addHealthCodeStatement.setInt(5, i);

					addHealthCodeStatement.executeUpdate();
				}
			}
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

		DiseaseMappingStudy diseaseMappingStudy
				= (DiseaseMappingStudy) rifStudySubmission.getStudy();
		diseaseMappingStudyManager.checkNonExistentItems(user, connection, diseaseMappingStudy);

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
}
