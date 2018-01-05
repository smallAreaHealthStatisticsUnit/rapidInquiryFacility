package rifServices.dataStorageLayer.pg;


import rifServices.businessConceptLayer.*;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.system.*;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.*;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLInsertQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
 */
/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

final class PGSQLCreateStudySubmissionStep 
	extends PGSQLAbstractSQLManager {

	
	// ==========================================
	// Section Constants
	// ==========================================

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	// ==========================================
	// Section Properties
	// ==========================================
	private PGSQLDiseaseMappingStudyManager diseaseMappingStudyManager;
	private PGSQLMapDataManager mapDataManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public PGSQLCreateStudySubmissionStep(
		final RIFDatabaseProperties rifDatabaseProperties,
		final PGSQLDiseaseMappingStudyManager diseaseMappingStudyManager,
		final PGSQLMapDataManager mapDataManager) {

		super(rifDatabaseProperties);	
		this.diseaseMappingStudyManager = diseaseMappingStudyManager;
		this.mapDataManager = mapDataManager;
		setEnableLogging(false);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String performStep(
		final Connection connection,
		final User user,
		final RIFStudySubmission studySubmission) 
		throws RIFServiceException {

		//Validate parameters
		//ValidationPolicy validationPolicy = getValidationPolicy();
		//studySubmission.checkErrors(validationPolicy);
		studySubmission.checkErrors(ValidationPolicy.RELAXED);

		//perform various checks for non-existent objects
		//such as geography,  geo level selects, covariates
		checkNonExistentItems(
			connection, 
			studySubmission);

		//KLG: TODO: Later on we should not rely on casting - it might
		//be a risk analysis study
		String result = null;
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) studySubmission.getStudy();
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
				true, 
				diseaseMappingStudy);
	
			result = getCurrentStudyID(connection);	
			connection.commit();
			
			rifLogger.info(this.getClass(), "======SQLCREATESTUDYSUBMISSIONSTEP====studyID=="+result+"==");
			return result;
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
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
	private String getCurrentStudyID(
		final Connection connection) 
		throws SQLException,
		RIFServiceException {
					

		String result = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryLine(0, "SELECT");
			queryFormatter.addQueryLine(1, "currval('rif40_study_id_seq'::regclass);");


			logSQLQuery(
				"getCurrentStudyID", 
				queryFormatter);
						
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			resultSet
				= statement.executeQuery();
			resultSet.next();
			
			result = String.valueOf(resultSet.getInt(1));
			
			return result;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
		
	}

	private void addGeneralInformationToStudy(
		final Connection connection,
		final User user,
		final Project project,
		final DiseaseMappingStudy diseaseMappingStudy,
		final RIFStudySubmission studySubmission) 
		throws SQLException,
		RIFServiceException {
	

		PreparedStatement studyShareStatement = null;
		PreparedStatement addStudyStatement = null;
		try {
			
			//add information about who can share the study
			PGSQLInsertQueryFormatter studyQueryFormatter
				= new PGSQLInsertQueryFormatter();
			studyQueryFormatter.setIntoTable("rif40_studies");
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

			logSQLQuery(
				"addGeneralInformationToStudy", 
				studyQueryFormatter);
			
			addStudyStatement
				= createPreparedStatement(
					connection,
					studyQueryFormatter);
			int ithQueryParameter = 1;	

			Geography geography 
				= diseaseMappingStudy.getGeography();
			addStudyStatement.setString(
				ithQueryParameter++, 
				geography.getName());
			
			addStudyStatement.setString(
				ithQueryParameter++, 
				project.getName());
			
			addStudyStatement.setString(
				ithQueryParameter++, 
				diseaseMappingStudy.getName());
			
			//study type will be "1" for diseaseMappingStudy
			addStudyStatement.setInt(
				ithQueryParameter++,
				1);

			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			addStudyStatement.setString(
				ithQueryParameter++, 
				comparisonArea.getGeoLevelToMap().getName());
						
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			addStudyStatement.setString(
				ithQueryParameter++, 
				diseaseMappingStudyArea.getGeoLevelToMap().getName());
			
			//KLG: is this a good idea below - considering that each of the 
			//investigations can have different denominator tables?
			Investigation firstInvestigation
				= diseaseMappingStudy.getInvestigations().get(0);
			NumeratorDenominatorPair ndPair
				= firstInvestigation.getNdPair();
			addStudyStatement.setString(
				ithQueryParameter++,
				ndPair.getDenominatorTableName());
			
			YearRange yearRange 
				= firstInvestigation.getYearRange();
			//year_start
			addStudyStatement.setInt(
				ithQueryParameter++, 
				Integer.valueOf(yearRange.getLowerBound()));
			//year_stop
			addStudyStatement.setInt(
				ithQueryParameter++, 
				Integer.valueOf(yearRange.getUpperBound()));

			//max_age_group
			AgeGroup maximumAgeGroup = firstInvestigation.getMaximumAgeGroup();
			int maximumAgeGroupOffset
				= getOffsetFromAgeGroup(
					connection, 
					ndPair,
					maximumAgeGroup);						
			addStudyStatement.setInt(
				ithQueryParameter++, 
				maximumAgeGroupOffset);
			
			//min_age_group
			AgeGroup minimumAgeGroup = firstInvestigation.getMinimumAgeGroup();			
			int minimumAgeGroupOffset
				= getOffsetFromAgeGroup(
					connection, 
					ndPair,
					minimumAgeGroup);			
			addStudyStatement.setInt(
				ithQueryParameter++, 
				minimumAgeGroupOffset);

			//KLG: Ask about this -- if we left it out would it get a value automatically?
			//for now, set suppression threshold to zero
			addStudyStatement.setInt(
				ithQueryParameter++, 
				0);
			
			//setting extract permitted
			addStudyStatement.setInt(
				ithQueryParameter++, 
				0);			

			//setting transfer permitted
			addStudyStatement.setInt(
				ithQueryParameter++, 
				0);			
				
			//setting stats method
			CalculationMethod calculationMethod
				= studySubmission.getCalculationMethods().get(0); 
				// For some reason this is an array of one!
			rifLogger.info(this.getClass(), "CalculationMethod name: " +
				calculationMethod.getName() + "; description: " +
				calculationMethod.getDescription() + "; stats method: " +
				calculationMethod.getStatsMethod() + "; code routine name: " +
				calculationMethod.getCodeRoutineName());				
			addStudyStatement.setString(
				ithQueryParameter++,	
				calculationMethod.getStatsMethod());	
				
			addStudyStatement.executeUpdate();			
			
			//add information about who can share the study
			PGSQLInsertQueryFormatter studyShareQueryFormatter
				= new PGSQLInsertQueryFormatter();
			studyShareQueryFormatter.setIntoTable("rif40_study_shares");
			studyShareQueryFormatter.addInsertField("grantee_username");

			studyShareStatement 
				= createPreparedStatement(
					connection,
					studyShareQueryFormatter);
			studyShareStatement.setString(1, user.getUserID());
			studyShareStatement.executeUpdate();			
		}
		finally {
			//Cleanup database resources	
			PGSQLQueryUtility.close(studyShareStatement);
			PGSQLQueryUtility.close(addStudyStatement);
		}
	}
	
	private void addInvestigationsToStudy(
		final Connection connection,
		final boolean isGeography,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws SQLException,
		RIFServiceException {
		
		PreparedStatement statement = null;
		try {
		
			//we assume that the study is valid and that the caller has 
			//invoked rifStudySubmission.checkErrors();
		
			ArrayList<Investigation> investigations
				= diseaseMappingStudy.getInvestigations();
			if (investigations.isEmpty()) {
				return;
			}

			PGSQLInsertQueryFormatter queryFormatter
				= new PGSQLInsertQueryFormatter();
			queryFormatter.setIntoTable("rif40_investigations");		
			queryFormatter.addInsertField("inv_name");
			queryFormatter.addInsertField("inv_description");
			queryFormatter.addInsertField("genders");
			queryFormatter.addInsertField("numer_tab");
			queryFormatter.addInsertField("year_start");
			queryFormatter.addInsertField("year_stop");
			queryFormatter.addInsertField("max_age_group");
			queryFormatter.addInsertField("min_age_group");
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);
			
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			for (Investigation investigation : investigations) {		
				
				//extracting field values that will be used for the query
				
				String invNameParameter
					= fieldValidationUtility.convertToDatabaseTableName(investigation.getTitle());
				String invDescriptionParameter
					= investigation.getDescription();
				Sex sex = investigation.getSex();
				Integer genderCodeParameter = null;
				if (sex == Sex.MALES) {
					genderCodeParameter = 1;
				}
				else if (sex == Sex.FEMALES) {
					genderCodeParameter = 2;
				}
				else {
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
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
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
		
		PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();

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
			statement 
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.setInt(1, 1);
			statement.setString(2, numeratorTableName);
			statement.setString(3, ageGroup.getName());
			resultSet = statement.executeQuery();
			resultSet.next();
			return resultSet.getInt(1);
		}
		finally {
			PGSQLQueryUtility.close(statement);
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
			
			PGSQLInsertQueryFormatter queryFormatter
				= new PGSQLInsertQueryFormatter();
			queryFormatter.setIntoTable("rif40_study_areas");
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
			
			
/*
			
			for (MapArea mapArea : mapAreas) {
				
				//KLG:
				//is this the right area_id or should we use getIdentifier() instead?
				statement.setString(1, mapArea.getGeographicalIdentifier());
				
				//In disease mapping studies, all the areas belong to one band.
				//Therefore, let's give it a default value of 1.

				statement.executeUpdate();
			}
*/			
			
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
		}	
	}
	
	private void addComparisonAreaToStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws SQLException,
		RIFServiceException {
	
		PreparedStatement statement = null;
		try {
			PGSQLInsertQueryFormatter queryFormatter
				= new PGSQLInsertQueryFormatter();
			queryFormatter.setIntoTable("rif40_comparison_areas");
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
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
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
		
			PGSQLSelectQueryFormatter getMinMaxCovariateValuesQueryFormatter
				= new PGSQLSelectQueryFormatter();
			getMinMaxCovariateValuesQueryFormatter.addSelectField("min");
			getMinMaxCovariateValuesQueryFormatter.addSelectField("max");
			getMinMaxCovariateValuesQueryFormatter.addFromTable("rif40_covariates");
			getMinMaxCovariateValuesQueryFormatter.addWhereParameter("geography");
			getMinMaxCovariateValuesQueryFormatter.addWhereParameter("geolevel_name");
			getMinMaxCovariateValuesQueryFormatter.addWhereParameter("covariate_name");
			
			PGSQLInsertQueryFormatter addCovariateQueryFormatter
				= new PGSQLInsertQueryFormatter();
			addCovariateQueryFormatter.setIntoTable("rif40_inv_covariates");
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
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(addCovariateStatement);
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
							
				PGSQLSelectQueryFormatter getOutcomeGroupNameQueryFormatter
					= new PGSQLSelectQueryFormatter();
				getOutcomeGroupNameQueryFormatter.addSelectField("outcome_group_name");
				getOutcomeGroupNameQueryFormatter.addSelectField("field_name");			
				getOutcomeGroupNameQueryFormatter.addFromTable("rif40.rif40_numerator_outcome_columns");
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

					PGSQLInsertQueryFormatter addHealthOutcomeQueryFormatter
						= new PGSQLInsertQueryFormatter();
					
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
			}
			finally {
				//Cleanup database resources	
				PGSQLQueryUtility.close(getOutcomeGroupNameStatement);
				PGSQLQueryUtility.close(addHealthCodeStatement);
			}		
		}

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	private void checkNonExistentItems(
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
		diseaseMappingStudyManager.checkNonExistentItems(
			connection, 
			diseaseMappingStudy);
		
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
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setLookupKeyFieldName("project");
			queryFormatter.setFromTable("rif40_projects");

			logSQLQuery(
					"checkProjectExists", 
					queryFormatter,
					project.getName());			
			
			//KLG: TODO - change table name
				
			checkProjectExistsStatement
				= createPreparedStatement(
					connection,
					queryFormatter);			

			checkProjectExistsStatement.setString(1, project.getName());
			checkProjectExistsResultSet 
				= checkProjectExistsStatement.executeQuery();
		
			if (checkProjectExistsResultSet.next() == false) {
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
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String recordType
				= RIFServiceMessages.getMessage("project.label");			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					project.getName());

			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
					
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(checkProjectExistsStatement);
			PGSQLQueryUtility.close(checkProjectExistsResultSet);			
		}		
	}
	
	private void checkCalculationMethodExists(
		final Connection connection,
		final CalculationMethod calculationMethod) 
		throws RIFServiceException {
		
		//@TODO: Implement when RIF is able to register R routines
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
