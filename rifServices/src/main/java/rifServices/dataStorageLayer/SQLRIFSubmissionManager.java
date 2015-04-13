package rifServices.dataStorageLayer;

import rifGenericLibrary.dataStorageLayer.SQLDeleteRowsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLInsertQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifServices.util.RIFLogger;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
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
 * Copyright 2014 Imperial College London, developed by the Small Area
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

final class SQLRIFSubmissionManager 
	extends AbstractSQLManager {
		
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private SQLDiseaseMappingStudyManager diseaseMappingStudyManager;
	private SQLRIFContextManager rifContextManager;
	private SQLAgeGenderYearManager ageGenderYearManager;
	private SQLCovariateManager covariateManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public SQLRIFSubmissionManager(
		final RIFDatabaseProperties rifDatabaseProperties,
		final SQLRIFContextManager rifContextManager,
		final SQLAgeGenderYearManager ageGenderYearManager,
		final SQLCovariateManager covariateManager,
		final SQLDiseaseMappingStudyManager diseaseMappingStudyManager) {

		super(rifDatabaseProperties);		
		this.rifContextManager = rifContextManager;
		this.ageGenderYearManager = ageGenderYearManager;
		this.covariateManager = covariateManager;		
		this.diseaseMappingStudyManager = diseaseMappingStudyManager;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	
	/**
	 * Used as a test method to clear rif job submissions.
	 * @param connection
	 * @throws RIFServiceException
	 */
	public void clearRIFJobSubmissionsForUser(
		final Connection connection,
		final User user)
		throws RIFServiceException {
	
		String userID = user.getUserID();
		PreparedStatement deleteComparisonAreasStatement = null;
		PreparedStatement deleteStudyAreasStatement = null;
		PreparedStatement deleteInvestigationsStatement = null;
		PreparedStatement deleteStudiesStatement = null;

		try {
			SQLDeleteRowsQueryFormatter deleteComparisonAreasQueryFormatter
				= new SQLDeleteRowsQueryFormatter();
			configureQueryFormatterForDB(deleteComparisonAreasQueryFormatter);
			deleteComparisonAreasQueryFormatter.setFromTable("t_rif40_comparison_areas");
			deleteComparisonAreasQueryFormatter.addWhereParameter("username");
			
			deleteComparisonAreasStatement 
				= connection.prepareStatement(
					deleteComparisonAreasQueryFormatter.generateQuery());
			deleteComparisonAreasStatement.setString(1, userID);
			deleteComparisonAreasStatement.executeUpdate();
			
			SQLDeleteRowsQueryFormatter deleteStudyAreasQueryFormatter
				= new SQLDeleteRowsQueryFormatter();
			configureQueryFormatterForDB(deleteStudyAreasQueryFormatter);
			deleteStudyAreasQueryFormatter.setFromTable("t_rif40_study_areas");
			deleteStudyAreasQueryFormatter.addWhereParameter("username");
			deleteStudyAreasStatement 
				= connection.prepareStatement(
					deleteStudyAreasQueryFormatter.generateQuery());
			deleteStudyAreasStatement.setString(1, userID);
			deleteStudyAreasStatement.executeUpdate();
			
			SQLDeleteRowsQueryFormatter deleteInvestigationsQueryFormatter
				= new SQLDeleteRowsQueryFormatter();
			configureQueryFormatterForDB(deleteInvestigationsQueryFormatter);
			deleteInvestigationsQueryFormatter.setFromTable("t_rif40_investigations");
			deleteInvestigationsQueryFormatter.addWhereParameter("username");
			deleteInvestigationsStatement
				= connection.prepareStatement(
					deleteInvestigationsQueryFormatter.generateQuery());
			deleteInvestigationsStatement.setString(1, userID);
			deleteInvestigationsStatement.executeUpdate();
			
			SQLDeleteRowsQueryFormatter deleteStudiesQueryFormatter
				= new SQLDeleteRowsQueryFormatter();
			configureQueryFormatterForDB(deleteStudiesQueryFormatter);
			deleteStudiesQueryFormatter.setFromTable("t_rif40_studies");
			deleteStudiesQueryFormatter.addWhereParameter("username");
			deleteStudiesStatement
				= connection.prepareStatement(
					deleteStudiesQueryFormatter.generateQuery());
			deleteStudiesStatement.setString(1, userID);
			deleteStudiesStatement.executeUpdate();

		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToClearSubmissionsForUser",
					user.getUserID());
			RIFServiceException rifServiceException	
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(deleteComparisonAreasStatement);
			SQLQueryUtility.close(deleteStudyAreasStatement);
			SQLQueryUtility.close(deleteInvestigationsStatement);
		}
	}
	
	
	/**
	 * submit rif study submission.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param rifJobSubmission the rif job submission
	 * @throws RIFServiceException the RIF service exception
	 */	
	public String submitStudy(
		final Connection connection,
		final User user,
		final RIFStudySubmission studySubmission) 
		throws RIFServiceException {
		
		//Validate parameters
		studySubmission.checkErrors();
		
		//perform various checks for non-existent objects
		//such as geography,  geo level selects, covariates
		checkNonExistentItems(
			connection, 
			studySubmission);

		//KLG: TODO: Later on we should not rely on casting - it might
		//be a risk analysis study
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) studySubmission.getStudy();
		
		Project project = studySubmission.getProject();
		addGeneralInformationToStudy(
			connection,
			user,
			project,
			diseaseMappingStudy);

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
		
		return getCurrentStudyID(connection);
	}
	
	/*
	 * This function returns the study ID of the most recently created study
	 */
	private String getCurrentStudyID(
		final Connection connection) 
		throws RIFServiceException {
					
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "SELECT");
		queryFormatter.addQueryLine(1, "currval('rif40_study_id_seq'::regclass);");

		String result = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			resultSet
				= statement.executeQuery();
			resultSet.next();
			
			result = resultSet.getString(1);			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToGetCurrentStudyIdentifier");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLRIFSubmissionManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		return result;
	}

	private void addGeneralInformationToStudy(
		final Connection connection,
		final User user,
		final Project project,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws RIFServiceException {
	
		//add information about who can share the study
		SQLInsertQueryFormatter studyShareQueryFormatter
			= new SQLInsertQueryFormatter();
		studyShareQueryFormatter.setIntoTable("rif40_study_shares");
		studyShareQueryFormatter.addInsertField("grantee_username");

		PreparedStatement studyShareStatement = null;
		try {
			studyShareStatement 
				= connection.prepareStatement(studyShareQueryFormatter.generateQuery());
			studyShareStatement.setString(1, user.getUserID());
			studyShareStatement.executeUpdate();			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddComparisonAreaToStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLRIFSubmissionManager.class, 
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
			SQLQueryUtility.close(studyShareStatement);
		}
		
		//add information about who can share the study
		SQLInsertQueryFormatter studyQueryFormatter
			= new SQLInsertQueryFormatter();
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

		PreparedStatement statement = null;
		try {
			statement
				= connection.prepareStatement(studyQueryFormatter.generateQuery());
			int ithQueryParameter = 0;	
			
			Geography geography 
				= diseaseMappingStudy.getGeography();
			statement.setString(
				ithQueryParameter++, 
				geography.getName());
			
			statement.setString(
				ithQueryParameter++, 
				project.getName());
			
			statement.setString(
				ithQueryParameter++, 
				diseaseMappingStudy.getName());
			
			//study type will always be "C" for created
			statement.setString(
				ithQueryParameter++,
				"C");
			
			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			statement.setString(
				ithQueryParameter++, 
				comparisonArea.getGeoLevelToMap().getName());
						
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			statement.setString(
				ithQueryParameter++, 
				diseaseMappingStudyArea.getGeoLevelToMap().getName());
			
			//KLG: is this a good idea below - considering that each of the 
			//investigations can have different denominator tables?
			Investigation firstInvestigation
				= diseaseMappingStudy.getInvestigations().get(0);
			NumeratorDenominatorPair ndPair
				= firstInvestigation.getNdPair();
			statement.setString(
				ithQueryParameter++,
				ndPair.getDenominatorTableName());
			
			YearRange yearRange 
				= firstInvestigation.getYearRange();
			//year_start
			statement.setInt(
				ithQueryParameter++, 
				Integer.valueOf(yearRange.getLowerBound()));
			//year_stop
			statement.setInt(
				ithQueryParameter++, 
				Integer.valueOf(yearRange.getUpperBound()));
			//max_age_group
			AgeGroup maximumAgeGroup = firstInvestigation.getMaximumAgeGroup();
			statement.setInt(
				ithQueryParameter++, 
				Integer.valueOf(maximumAgeGroup.getUpperLimit()));
			//min_age_group
			AgeGroup minimumAgeGroup = firstInvestigation.getMinimumAgeGroup();
			statement.setInt(
				ithQueryParameter++, 
				Integer.valueOf(minimumAgeGroup.getLowerLimit()));

			//KLG: Ask about this -- if we left it out would it get a value automatically?
			//for now, set suppression threshold to zero
			statement.setInt(
				ithQueryParameter++, 
				0);
			
			//setting extract permitted
			statement.setInt(
				ithQueryParameter++, 
				0);			

			//setting transfer permitted
			statement.setInt(
				ithQueryParameter++, 
				0);			
			
			statement.executeUpdate();			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddComparisonAreaToStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
		}
	}
	
	private void addInvestigationsToStudy(
		final Connection connection,
		final boolean isGeography,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws RIFServiceException {
		
		//we assume that the study is valid and that the caller has 
		//invoked rifStudySubmission.checkErrors();
		
		ArrayList<Investigation> investigations
			= diseaseMappingStudy.getInvestigations();
		if (investigations.isEmpty()) {
			return;
		}
		
		SQLInsertQueryFormatter queryFormatter
			= new SQLInsertQueryFormatter();
		queryFormatter.setIntoTable("rif40_investigations");
		
		if (isGeography == true) {
			queryFormatter.addInsertField("geography");			
		}
				
		queryFormatter.addInsertField("inv_name");
		queryFormatter.addInsertField("inv_description");
		queryFormatter.addInsertField("genders");
		queryFormatter.addInsertField("numer_tab");
		queryFormatter.addInsertField("year_start");
		queryFormatter.addInsertField("year_stop");
		queryFormatter.addInsertField("max_age_group");
		queryFormatter.addInsertField("min_age_group");
			
		PreparedStatement statement = null;
		Investigation currentInvestigation = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			
			for (Investigation investigation : investigations) {
				
				//setting current investigation so that if there is a problem the
				//catch block will have a reference to the offending investigation
				//Another way to do this would be to have a for loop that encloses a 
				//try...catch statement.  However, this is probably slower and we
				//want to fail the operation if we have problems adding any investigation
				//to the study
				currentInvestigation = investigation;
				
				
				int ithQueryParameter = 0;	
				if (isGeography == true) {
					Geography geography = diseaseMappingStudy.getGeography();
					statement.setString(ithQueryParameter++, geography.getName());
				}
				statement.setString(
					ithQueryParameter++, 
					investigation.getTitle());
				statement.setString(
					ithQueryParameter++,
					investigation.getDescription());
				
				Sex sex = investigation.getSex();
				if (sex == Sex.MALES) {
					statement.setInt(ithQueryParameter++, 1);	
				}
				else if (sex == Sex.FEMALES){
					statement.setInt(ithQueryParameter++, 2);	
				}	
				else {
					//assume both
					statement.setInt(ithQueryParameter++, 3);	
				}
				
				//setting name of numerator table
				NumeratorDenominatorPair ndPair = investigation.getNdPair();
				statement.setString(ithQueryParameter++, ndPair.getNumeratorTableName());

				YearRange yearRange = investigation.getYearRange();
				//year_start
				statement.setInt(
					ithQueryParameter++, 
					Integer.valueOf(yearRange.getLowerBound()));
				//year_stop
				statement.setInt(
					ithQueryParameter++, 
					Integer.valueOf(yearRange.getUpperBound()));
				//max_age_group
				AgeGroup maximumAgeGroup = investigation.getMaximumAgeGroup();
				statement.setInt(
					ithQueryParameter++, 
					Integer.valueOf(maximumAgeGroup.getUpperLimit()));
				//min_age_group
				AgeGroup minimumAgeGroup = investigation.getMinimumAgeGroup();
				statement.setInt(
					ithQueryParameter++, 
					Integer.valueOf(minimumAgeGroup.getLowerLimit()));
				
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
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddInvestigationToStudy",
					currentInvestigation.getTitle(),
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
		}

	}
	
	public void deleteStudy(
		final Connection connection,
		final AbstractStudy study)
		throws RIFServiceException {
		
		
		SQLFunctionCallerQueryFormatter queryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		queryFormatter.setSchema("rif40_sm_pkg");
		queryFormatter.setFunctionName("rif40_delete_study");
		queryFormatter.setNumberOfFunctionParameters(1);
		
		
		PreparedStatement statement = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, study.getIdentifier());
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToDeleteStudy",
					study.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
		}	
	}
	
	
	
	
	private void addStudyAreaToStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws RIFServiceException {
				
		SQLInsertQueryFormatter queryFormatter
			= new SQLInsertQueryFormatter();
		queryFormatter.setIntoTable("rif40_study_areas");
		queryFormatter.addInsertField("area_id");
		queryFormatter.addInsertField("band_id");
				
		PreparedStatement statement = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
						
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			ArrayList<MapArea> mapAreas
				= diseaseMappingStudyArea.getMapAreas();
			for (MapArea mapArea : mapAreas) {
				
				//KLG:
				//is this the right area_id or should we use getIdentifier() instead?
				statement.setString(1, mapArea.getGeographicalIdentifier());
				
				//In disease mapping studies, all the areas belong to one band.
				//Therefore, let's give it a default value of 1.
				statement.setString(2, "1");
				statement.executeUpdate();
			}			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddStudyAreaToStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
		}	
	}
	
	private void addComparisonAreaToStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws RIFServiceException {
	
		SQLInsertQueryFormatter queryFormatter
			= new SQLInsertQueryFormatter();
		queryFormatter.setIntoTable("rif40_comparison_areas");
		queryFormatter.addInsertField("area_id");
		
		PreparedStatement statement = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			
			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			ArrayList<MapArea> mapAreas
				= comparisonArea.getMapAreas();
			for (MapArea mapArea : mapAreas) {
				statement.setString(1, mapArea.getGeographicalIdentifier());
				statement.executeUpdate();
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddComparisonAreaToStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLRIFSubmissionManager.class, 
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
			SQLQueryUtility.close(statement);
		}
		
	}
	
	
	private void addCovariatesToStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy,
		final Investigation investigation) 
		throws RIFServiceException {
		
		SQLInsertQueryFormatter queryFormatter
			= new SQLInsertQueryFormatter();
		queryFormatter.setIntoTable("rif40_inv_covariates");
		queryFormatter.addInsertField("geography");
		queryFormatter.addInsertField("covariate_name");
		queryFormatter.addInsertField("study_geolevel_name");
		queryFormatter.addInsertField("min");
		queryFormatter.addInsertField("max");
		
		PreparedStatement statement = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			int ithQueryParameter = 0;
						
			Geography geography = diseaseMappingStudy.getGeography();
			String geographyName = geography.getName();

			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			String studyGeoLevelName
				= diseaseMappingStudyArea.getGeoLevelToMap().getName();

			ArrayList<AbstractCovariate> covariates
				= investigation.getCovariates();
			for (AbstractCovariate covariate : covariates) {
				statement.setString(
					ithQueryParameter++, 
					geographyName);
				statement.setString(
					ithQueryParameter++,
					covariate.getName());
				statement.setString(
					ithQueryParameter++,
					studyGeoLevelName);
				statement.setDouble(
					ithQueryParameter++,
					Double.valueOf(covariate.getMinimumValue()));
				statement.setDouble(
					ithQueryParameter++,
					Double.valueOf(covariate.getMaximumValue()));
				statement.executeUpdate();
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddCovariatesToStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
		}
	}
		
	private void addHealthOutcomes(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy,
		final Investigation investigation) 
		throws RIFServiceException {
				
		SQLInsertQueryFormatter queryFormatter
			= new SQLInsertQueryFormatter();
		queryFormatter.setIntoTable("rif40_inv_conditions");
		queryFormatter.addInsertField("condition");
				
		PreparedStatement statement = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());

			ArrayList<HealthCode> healthCodes
				= investigation.getHealthCodes();
			
			for (HealthCode healthCode : healthCodes) {
				//KLG: Note - we need to improve what we capture for health codes
				//table should be expanded to include name space
				statement.setString(1, healthCode.getCode());				
				statement.executeUpdate();
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddComparisonAreaToStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
		}		
	}

	/*
	 * Methods below are for retrieving a study
	 */
	public DiseaseMappingStudy getDiseaseMappingStudy(
		final Connection connection,
		final String studyID) 
		throws RIFServiceException {
				
		//check for non-existent study given id
		//checkNonExistentStudy(studyID);
		
		DiseaseMappingStudy result
			= DiseaseMappingStudy.newInstance();
		result.setIdentifier(studyID);
		
		retrieveGeneralInformationForStudy(
			connection,
			result);
		
		retrieveStudyAreaForStudy(
			connection,
			result);
		
		retrieveComparisonAreaForStudy(
			connection,
			result);

		retrieveInvestigationsForStudy(
			connection,
			result);
		
		return result;
		
	}
	
	private void retrieveGeneralInformationForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws RIFServiceException {
				
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addSelectField("study_name");
		queryFormatter.addSelectField("geography");
		//queryFormatter.addSelectField("project");
		queryFormatter.addSelectField("comparison_geolevel_name");
		queryFormatter.addSelectField("study_geolevel_name");
		queryFormatter.addSelectField("denom_tab");
		queryFormatter.addWhereParameter("study_id");

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, diseaseMappingStudy.getIdentifier());
			
			resultSet = statement.executeQuery();
			resultSet.next();
			
			diseaseMappingStudy.setName(resultSet.getString(1));
			Geography geography
				= Geography.newInstance(resultSet.getString(2), "");
			
			//KLG: Note that we cannot reconstitute geolevel select, geolevel view,
			//geo level area -- just 'to map'
			GeoLevelToMap comparisonAreaGeoLevelToMap
				= GeoLevelToMap.newInstance(resultSet.getString(3));
			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			comparisonArea.setGeoLevelToMap(comparisonAreaGeoLevelToMap);
			
			GeoLevelToMap diseaseMappingStudyAreaGeoLevelToMap
				= GeoLevelToMap.newInstance(resultSet.getString(3));
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			diseaseMappingStudyArea.setGeoLevelToMap(diseaseMappingStudyAreaGeoLevelToMap);
			
			//retrieving denom is awkward because we need both denom and numer to
			//reconstitute ndPair
			
			
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			
			//KLG TODO: perhaps modify what we report here.  We may not have a name
			//to report here yet
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRetrieveGeneralInformationForStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}
		
	private void retrieveStudyAreaForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws RIFServiceException {
					
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addFromTable("rif40_study_areas");
		queryFormatter.addSelectField("area_id");
		queryFormatter.addSelectField("band_id");
		queryFormatter.addWhereParameter("study_id");
			
			
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, diseaseMappingStudy.getIdentifier());
						
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.newInstance();
			//KLG: TODO - how can we improve this so we can add in extra
			//information?
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String geographicalIdentifier
					= resultSet.getString(1);
				MapArea mapArea = MapArea.newInstance(
					geographicalIdentifier, 
					geographicalIdentifier, 
					geographicalIdentifier);
				diseaseMappingStudyArea.addMapArea(mapArea);
			}
				diseaseMappingStudy.setDiseaseMappingStudyArea(diseaseMappingStudyArea);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRetrieveHealthCodesForStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}	
	
	private void retrieveComparisonAreaForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws RIFServiceException {
				
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addFromTable("rif40_comparison_areas");
		queryFormatter.addSelectField("area_id");
		queryFormatter.addWhereParameter("study_id");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, diseaseMappingStudy.getIdentifier());
						
			ComparisonArea comparisonArea
				= ComparisonArea.newInstance();
			//KLG: TODO - how can we improve this so we can add in extra
			//information?
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String geographicalIdentifier
					= resultSet.getString(1);
				MapArea mapArea = MapArea.newInstance(
					geographicalIdentifier, 
					geographicalIdentifier, 
					geographicalIdentifier);
				comparisonArea.addMapArea(mapArea);
			}

			diseaseMappingStudy.setComparisonArea(comparisonArea);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRetrieveComparisonAreaForStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
	
	private void retrieveInvestigationsForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws RIFServiceException {
		
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("inv_id");
		queryFormatter.addSelectField("inv_name");
		queryFormatter.addSelectField("inv_description");
		queryFormatter.addSelectField("year_start");
		queryFormatter.addSelectField("year_stop");
		queryFormatter.addSelectField("max_age_group");
		queryFormatter.addSelectField("min_age_group");
		queryFormatter.addSelectField("genders");
		queryFormatter.addSelectField("numer_tab");
		
		queryFormatter.addFromTable("rif40_investigations");
		queryFormatter.addWhereParameter("study_id");
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, diseaseMappingStudy.getIdentifier());
			resultSet
				= statement.executeQuery();
			while (resultSet.next()) {
				
				Investigation investigation = Investigation.newInstance();
				//set identifier
				int ithParameter = 0;
				investigation.setIdentifier(
					resultSet.getString(ithParameter++));

				//set title
				investigation.setTitle(
					resultSet.getString(ithParameter++));
				//set description
				investigation.setDescription(
					resultSet.getString(ithParameter++));
		
				//set year range
				int startYearValue
					= resultSet.getInt(ithParameter++);
				int stopYearValue
					= resultSet.getInt(ithParameter++);
				YearRange yearRange
					= YearRange.newInstance(
						String.valueOf(startYearValue), 
						String.valueOf(stopYearValue));
				investigation.setYearRange(yearRange);
				
				
				//set the age bands.  
				//KLG: To do: we are not able to reconstitute age bands properly
				int maximumAgeGroupID
					= resultSet.getInt(ithParameter++);
				AgeGroup upperLimitAgeGroup
					= getAgeGroupFromIdentifier(
						connection,
						maximumAgeGroupID);
				int minimumAgeGroupID
					= resultSet.getInt(ithParameter++);
				AgeGroup lowerLimitAgeGroup
					= getAgeGroupFromIdentifier(
						connection,
						minimumAgeGroupID);				
				AgeBand ageBand
					= AgeBand.newInstance(lowerLimitAgeGroup, upperLimitAgeGroup);
				investigation.addAgeBand(ageBand);
								
				//set the sex value
				int sexIntValue
					= resultSet.getInt(ithParameter++);
				if (sexIntValue == 1) {
					investigation.setSex(Sex.MALES);
				}
				else if (sexIntValue == 2) {
					investigation.setSex(Sex.FEMALES);
				}
				else {
					investigation.setSex(Sex.BOTH);
				}
		
				//set the numerator denominator values
				String numeratorTableName
					= resultSet.getString(ithParameter++);
				NumeratorDenominatorPair ndPair
					= getNDPairForNumeratorTableName(
						connection,
						numeratorTableName);
				investigation.setNdPair(ndPair);
			
				diseaseMappingStudy.addInvestigation(investigation);				
			}
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRetrieveInvestigationsForStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
		
	/**
	 * We assume the age group identifier will be valid
	 * @param connection
	 * @param ageGroupIdentifier
	 * @return
	 * @throws SQLException
	 */
	private AgeGroup getAgeGroupFromIdentifier(
		final Connection connection,
		final int ageGroupIdentifier) 
		throws SQLException,
		RIFServiceException {
				
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addFromTable("rif40_age_groups");
		queryFormatter.addSelectField("low_age");
		queryFormatter.addSelectField("high_age");
		queryFormatter.addSelectField("fieldname");
			
		AgeGroup result = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, ageGroupIdentifier);
			resultSet
				= statement.executeQuery();
			resultSet.next();
			result
				= AgeGroup.newInstance(
					String.valueOf(ageGroupIdentifier), 					
					resultSet.getString(1), 
					resultSet.getString(2), 
					resultSet.getString(3));		
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
		
		return result;
	}
	
	private void retrieveCovariatesForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy,
		final Investigation investigation)
		throws RIFServiceException {
		

		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addFromTable("rif40_inv_covariates");
		queryFormatter.addSelectField("covariate_name");
		queryFormatter.addSelectField("min");
		queryFormatter.addSelectField("max");				
		queryFormatter.addWhereParameter("study_id");
		queryFormatter.addWhereParameter("inv_id");

		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			int ithParameter = 0;
			statement.setString(
				ithParameter++, 
				diseaseMappingStudy.getIdentifier());
			statement.setString(
				ithParameter++,
				investigation.getIdentifier());
			
			resultSet
				= statement.executeQuery();
			while (resultSet.next()) {
				//KLG: TODO - In the future we will have either adjustable or 
				//exposure covariates.  We will need a way of telling the difference
				//when we retrieve covariate data from the database.
				
				ExposureCovariate covariate
					= ExposureCovariate.newInstance();
				//We need a way of finding covariate type out.  This is just a guess
				covariate.setCovariateType(CovariateType.BINARY_INTEGER_SCORE);
				
				//do we need geography?
				ithParameter = 0;
				
				covariate.setName(
					resultSet.getString(ithParameter++));
				covariate.setMinimumValue(String.valueOf(resultSet.getDouble(ithParameter++)));
				covariate.setMaximumValue(String.valueOf(resultSet.getDouble(ithParameter++)));
				investigation.addCovariate(covariate);
			}
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRetrieveHealthCodesForStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
	

	private void retrieveHealthCodesForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy,
		final Investigation investigation) 
		throws RIFServiceException {
		
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addFromTable("rif40_inv_conditions");
		queryFormatter.addSelectField("condition");
		queryFormatter.addWhereParameter("study_id");
		queryFormatter.addWhereParameter("inv_id");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, diseaseMappingStudy.getIdentifier());
			statement.setString(2, investigation.getIdentifier());
			resultSet
				= statement.executeQuery();
						
			while (resultSet.next()) {
				HealthCode healthCode
					= HealthCode.newInstance();
				//KLG TODO:
				//at the moment we can only get the code.  later we should
				//get the name space as well
				healthCode.setCode(resultSet.getString(1));
				healthCode.setNameSpace("needs_name_space");
				investigation.addHealthCode(healthCode);
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRetrieveHealthCodesForStudy",
					diseaseMappingStudy.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}
	
	private NumeratorDenominatorPair getNDPairForNumeratorTableName(
		final Connection connection,
		final String numeratorTableName) 
		throws SQLException,
		RIFServiceException {
			
		NumeratorDenominatorPair result = null;
		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setUseDistinct(true);
		queryFormatter.addSelectField("numerator_description");
		queryFormatter.addSelectField("denominator_table");
		queryFormatter.addSelectField("denominator_description");		
		queryFormatter.addFromTable("rif40_num_denom");
		queryFormatter.addWhereParameter("numerator_table");
		
		PreparedStatement statement 
			= connection.prepareStatement(queryFormatter.generateQuery());
		statement.setString(1, numeratorTableName);
		ResultSet resultSet = null;
		try {
			resultSet = statement.executeQuery();
			resultSet.next();
			result = NumeratorDenominatorPair.newInstance();
			result.setNumeratorTableName(numeratorTableName);
			result.setNumeratorTableDescription(resultSet.getString(1));
			result.setDenominatorTableName(resultSet.getString(2));
			result.setDenominatorTableDescription(resultSet.getString(3));
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		return result;
	}
	
	public ArrayList<StudySummary> getStudySummariesForUser(
		final Connection connection,
		final User user) 
		throws RIFServiceException {
		
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addSelectField("study_id");
		queryFormatter.addSelectField("study_name");
		queryFormatter.addSelectField("description");
		queryFormatter.addWhereParameter("username");

		ArrayList<StudySummary> results = new ArrayList<StudySummary>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, user.getUserID());
			
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String studyID = resultSet.getString(1);
				String studyName = resultSet.getString(2);
				String studyDescription = resultSet.getString(3);
				
				StudySummary studySummary
					= StudySummary.newInstance(
						studyID, 
						studyName, 
						studyDescription);
				results.add(studySummary);
			}
			
			return results;			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRetrieveHealthCodesForStudy",
					user.getUserID());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLAgeGenderYearManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
		
	}
			
			
			
			
	/*
	public ArrayList<RIFJobSubmission> getRIFJobSubmissionsForUser(
		final Connection connection,
		final User user) {
		
		//Step 1: Obtain study records
		
		
		//Step 2: Obtain investigations for a given study
		
		//Step 3: Find health outcomes for a given investigation
		
		//Step 4: Find covariates for a given investigation
		

		
		
		
	}
	*/
	
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
	
	
	private void checkNonExistentStudyID(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
		
		SQLRecordExistsQueryFormatter queryFormatter
			= new SQLRecordExistsQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setLookupKeyFieldName("study_id");
		queryFormatter.setFromTable("rif40_studies");		

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, studyID);
			resultSet = statement.executeQuery();

			if (resultSet.next() == false) {
				//ERROR: no such project exists
				String recordType
					= RIFServiceMessages.getMessage("abstractStudy.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						studyID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_STUDY, 
						errorMessage);
				throw rifServiceException;
			}			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String recordType
				= RIFServiceMessages.getMessage("abstractStudy.label");			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}		
	}
	
	private void checkProjectExists(
		final Connection connection,
		final Project project) 
		throws RIFServiceException {
		
		//Create SQL query
		SQLRecordExistsQueryFormatter queryFormatter
			= new SQLRecordExistsQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		queryFormatter.setLookupKeyFieldName("project");
		queryFormatter.setFromTable("t_rif40_projects");
		
		//KLG: TODO - change table name
		
		PreparedStatement checkProjectExistsStatement = null;
		ResultSet checkProjectExistsResultSet = null;
		
		//Parameterise and execute query		
		try {
			checkProjectExistsStatement
				= connection.prepareStatement(
					queryFormatter.generateQuery());
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
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String recordType
				= RIFServiceMessages.getMessage("project.label");			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					project.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
