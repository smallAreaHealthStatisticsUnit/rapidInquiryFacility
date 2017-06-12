package rifServices.dataStorageLayer.pg;

import rifServices.businessConceptLayer.*;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.fileFormats.RIFZipFileWriter;
import rifServices.system.*;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.*;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLCreateTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLDeleteRowsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLDeleteTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLInsertQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSchemaCommentQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.util.FieldValidationUtility;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.File;
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

final class PGSQLRIFSubmissionManager 
	extends PGSQLAbstractSQLManager {

	
	public static void main(String[] args) {

		SQLGeneralQueryFormatter insertStudyAreasQueryFormatter
			= new SQLGeneralQueryFormatter();
		insertStudyAreasQueryFormatter.addQueryLine(0, "INSERT INTO g_rif40_study_areas ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "(study_id, area_id, band_id) ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "SELECT");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id, area_id, band_id ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "FROM ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "rif40_study_areas ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "WHERE ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id=? ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "ORDER BY ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id, area_id, band_id");
		insertStudyAreasQueryFormatter.endWithSemiColon();					
		
	}
	
	
	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	private PGSQLDiseaseMappingStudyManager diseaseMappingStudyManager;
	private PGSQLRIFContextManager rifContextManager;
	private PGSQLAgeGenderYearManager ageGenderYearManager;
	private PGSQLCovariateManager covariateManager;
	private PGSQLMapDataManager mapDataManager;
	private PGSQLStudyStateManager studyStateManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public PGSQLRIFSubmissionManager(
		final RIFDatabaseProperties rifDatabaseProperties,
		final PGSQLRIFContextManager rifContextManager,
		final PGSQLAgeGenderYearManager ageGenderYearManager,
		final PGSQLCovariateManager covariateManager,
		final PGSQLDiseaseMappingStudyManager diseaseMappingStudyManager,
		final PGSQLMapDataManager mapDataManager,
		final PGSQLStudyStateManager studyStateManager) {

		super(rifDatabaseProperties);		
		this.rifContextManager = rifContextManager;
		this.ageGenderYearManager = ageGenderYearManager;
		this.covariateManager = covariateManager;		
		this.diseaseMappingStudyManager = diseaseMappingStudyManager;
		this.mapDataManager = mapDataManager;
		this.studyStateManager = studyStateManager;
		
		setEnableLogging(false);
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
			PGSQLDeleteRowsQueryFormatter deleteComparisonAreasQueryFormatter
				= new PGSQLDeleteRowsQueryFormatter();
			configureQueryFormatterForDB(deleteComparisonAreasQueryFormatter);
			deleteComparisonAreasQueryFormatter.setFromTable("t_rif40_comparison_areas");
			deleteComparisonAreasQueryFormatter.addWhereParameter("username");

			logSQLQuery(
				"deleteComparisonAreasQueryFormatter", 
				deleteComparisonAreasQueryFormatter,
				userID);				
			
			deleteComparisonAreasStatement 			
				= createPreparedStatement(
					connection,
					deleteComparisonAreasQueryFormatter);
			deleteComparisonAreasStatement.setString(1, userID);
			deleteComparisonAreasStatement.executeUpdate();
			
			PGSQLDeleteRowsQueryFormatter deleteStudyAreasQueryFormatter
				= new PGSQLDeleteRowsQueryFormatter();
			configureQueryFormatterForDB(deleteStudyAreasQueryFormatter);
			deleteStudyAreasQueryFormatter.setFromTable("t_rif40_study_areas");
			deleteStudyAreasQueryFormatter.addWhereParameter("username");

			logSQLQuery(
				"deleteStudyAreasQueryFormatter", 
				deleteComparisonAreasQueryFormatter,
				userID);				
						
			deleteStudyAreasStatement 
				= createPreparedStatement(
					connection,
					deleteStudyAreasQueryFormatter);
			deleteStudyAreasStatement.setString(1, userID);
			deleteStudyAreasStatement.executeUpdate();
			
			PGSQLDeleteRowsQueryFormatter deleteInvestigationsQueryFormatter
				= new PGSQLDeleteRowsQueryFormatter();
			configureQueryFormatterForDB(deleteInvestigationsQueryFormatter);
			deleteInvestigationsQueryFormatter.setFromTable("t_rif40_investigations");
			deleteInvestigationsQueryFormatter.addWhereParameter("username");

			logSQLQuery(
				"deleteInvestigationsQueryFormatter", 
				deleteInvestigationsQueryFormatter,
				userID);				
			
			
			deleteInvestigationsStatement
				= createPreparedStatement(
					connection,
					deleteInvestigationsQueryFormatter);
			deleteInvestigationsStatement.setString(1, userID);
			deleteInvestigationsStatement.executeUpdate();
			
			PGSQLDeleteRowsQueryFormatter deleteStudiesQueryFormatter
				= new PGSQLDeleteRowsQueryFormatter();
			configureQueryFormatterForDB(deleteStudiesQueryFormatter);
			deleteStudiesQueryFormatter.setFromTable("t_rif40_studies");
			deleteStudiesQueryFormatter.addWhereParameter("username");
			logSQLQuery(
					"deleteStudiesQueryFormatter", 
					deleteStudiesQueryFormatter,
					userID);				

			deleteStudiesStatement
				= createPreparedStatement(
					connection,
					deleteStudiesQueryFormatter);
			deleteStudiesStatement.setString(1, userID);
			deleteStudiesStatement.executeUpdate();

			
			connection.commit();
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
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
			PGSQLQueryUtility.close(deleteComparisonAreasStatement);
			PGSQLQueryUtility.close(deleteStudyAreasStatement);
			PGSQLQueryUtility.close(deleteInvestigationsStatement);
		}
	}
	
	
	/**
	 * submit rif study submission.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param rifStudySubmission the rif job submission
	 * @throws RIFServiceException the RIF service exception
	 */	

	//TODO: (DM) This is probably never called and is what triggers the save to Zip file routines
	public String submitStudy(
		final Connection connection,
		final User user,
		final RIFStudySubmission studySubmission,
		final RIFServiceStartupOptions rifServiceStartupOptions) 
		throws RIFServiceException {

		
		//Validate parameters
		ValidationPolicy validationPolicy = getValidationPolicy();
		studySubmission.checkErrors(validationPolicy);
		
		//perform various checks for non-existent objects
		//such as geography,  geo level selects, covariates
		checkNonExistentItems(
			connection, 
			studySubmission);

		//KLG: TODO: Later on we should not rely on casting - it might
		//be a risk analysis study
		String studyID = null;
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) studySubmission.getStudy();
		try {
			/*
			 * Part I: Add the study description to the database
			 */
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
			studyID = getCurrentStudyID(connection);			
			connection.commit();
			
			/*
			 * Part II: Run the study to produce two tables:
			 * (1) rif_studies.s[study_id].extract and
			 * (2) rif_studies.s[study_id].map
			 * 
			 * At the end of this step, we will have a skeleton map file
			 * that needs to be populated with smoothed results.  For this,
			 * we will next call the smoothing service
			 */
			runStudy(connection, studyID);

			
			
			/*
			 * Part III: Run smoothing on the data extract
			 */
			
			String rScriptFileName = "Adj_Cov_Smooth.R";
			/*
			 * Originally we thought a study might be associated with multiple calculation
			 * methods that could each be used to post-process extract data to produce
			 * different smoothed result sets.  Presently we think that a given study will
			 * only have one CalculationMethod, and later on the business classes will
			 * likely be changed to reflect the use of one rather han many calculation methods
			 */
			CalculationMethod calculationMethod
				= studySubmission.getCalculationMethods().get(0);
			
			//@TODO: This needs to be reworked.  At the moment, we support
			//only one covariate for a study.  We're passing that as a parameter
			//to the script generation service to make it easier for the R program
			//to just use the name of the covariate rather than itself try to
			//obtain the covariate name.  Here we assume we have at least one 
			//investigation, which will have exactly one covariate.
			Investigation investigation
				= studySubmission.getStudy().getInvestigations().get(0);
			AbstractCovariate covariate
				= investigation.getCovariates().get(0);
			
			writeStudyToZipFile(
				connection,
				rifServiceStartupOptions,
				user,
				studySubmission,
				String.valueOf(studyID));

			System.out.println("STUDY =="+studyID+"==successfully submitted!!!!");
			return studyID;
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
	 * This is a method used to test whether the RIF will correctly write a 
	 * study submission and its results to a single zip file
	 */
	public void writeStudyToZipFile(
		final Connection connection,
		final RIFServiceStartupOptions rifServiceStartupOptions,
		final User user,
		final RIFStudySubmission rifStudySubmission,
		final String studyID) throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		
		PGSQLPublishResultsSubmissionStep fileExportService
			= new PGSQLPublishResultsSubmissionStep();
		File scratchSpaceDirectory 
			= new File(rifServiceStartupOptions.getExtractDirectory());
		File extraFilesDirectory 
			= new File(rifServiceStartupOptions.getExtraExtractFilesDirectoryPath());
		fileExportService.initialise(
			scratchSpaceDirectory, 
			extraFilesDirectory);
			
		fileExportService.performStep(
			connection, 
			user,
			rifStudySubmission, 
			studyID);
	}

	
	
	/**
	 * writes the extract and smoothed data tables to csv files that will appear in 
	 * a specified output scratch directory
	 * @param connection
	 * @param scratchFileDirectory
	 * @param studyID
	 */
	public void writeResultFiles(
		final Connection connection,
		final File scratchFileDirectory, 
		final String studyID) 
		throws RIFServiceException {
		
		
		PreparedStatement writeExtractFileStatement = null;
		PreparedStatement writeSmoothedResultsFileStatement = null;
		
		//CopyManager copyManager 
		//	= new CopyManager((BaseConnection) connection);
		//try {
			
			
			
			
		//}
		//catch(SQLException sqlException) {
			/*
			String errorMessage
				= RIFServiceMessages.getMessage("", studyID);
			RIFServiceException rifServiceException
				= new RIFServiceException(	
					RIFServiceError, 
					errorMessage);
			throw rifServiceException;
			*/
		//}
		//finally {			
		//}		
	}
	

	public void writeSmoothedResultFile(
		final Connection connection,
		final File scratchFileDirectory, 
		final String studyID) {
		
		
		
	}
	

	public String addStudyToDatabase(
		final Connection connection,
		final User user,
		final RIFStudySubmission studySubmission) 
		throws RIFServiceException {

		
		//Validate parameters
		ValidationPolicy validationPolicy = getValidationPolicy();
		studySubmission.checkErrors(validationPolicy);
		
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
	
			result = getCurrentStudyID(connection);
			
			connection.commit();
			
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

	public void verifyStudyProperlyCreated(
		final Connection connection,
		final String studyID,
		final String userID) 
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			
			//resultSet = statement.executeQuery();
		}
		catch(Exception sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"",
					studyID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			PGSQLQueryUtility.close(resultSet);
			PGSQLQueryUtility.close(statement);			
		}
				
	}
	

	/*
	 * Creates the extract table
	 */
	public void createStudyExtractTable(
		final Connection connection,
		final String userID,
		final String studyID,
		final AbstractStudy study) 
		throws RIFServiceException {
				
		String extractTableName 		
			= generateUserSchemaExtractTableName(
				userID, 
				studyID);
				
		PGSQLCreateTableQueryFormatter queryFormatter 
			= new PGSQLCreateTableQueryFormatter();
		queryFormatter.setTableName(extractTableName);			
		queryFormatter.addSmallIntegerFieldDeclaration(
			"year", 
			false);
		queryFormatter.addTextFieldDeclaration(
			"study_or_comparison", 
			1, 
			false);			
		queryFormatter.addIntegerFieldDeclaration(
			"study_id", 
			false);
		queryFormatter.addTextFieldDeclaration(
			"area_id", 
			false);						
		queryFormatter.addIntegerFieldDeclaration(
			"band_id", 
			true);			
		queryFormatter.addSmallIntegerFieldDeclaration(
			"sex", 
			true);						
		queryFormatter.addTextFieldDeclaration(
			"age_group", 
			true);						
		queryFormatter.addDoubleFieldDeclaration(
			"total_pop", 
			true);

		//add fields for covariates
		ArrayList<Investigation> investigations
			= study.getInvestigations();	

		/*
		 * Each investigation should have the same set of covariates.
		 * Therefore, to get the covariates for the study, take the
		 * first investigation and retrieve the collection of covariates
		 */
		ArrayList<AbstractCovariate> covariates
			= investigations.get(0).getCovariates();
		for (AbstractCovariate covariate : covariates) {
			queryFormatter.addTextFieldDeclaration(
				covariate.getName(), 
				true);
		}
	
		/*
		 * Add a column for the name of each investigation
		 */
		for (Investigation investigation : investigations) {
			queryFormatter.addTextFieldDeclaration(
				investigation.getDisplayName(),
				true);
		}

		logSQLQuery(
			"createExtractTable", 
			queryFormatter);

		PreparedStatement statement = null;
		try {
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.executeUpdate();
					
			/*
			 * Now comment the schema
			 */
			String extractTableComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable");
			addSchemaTableComment(
				connection, 
				extractTableName, 
				extractTableComment);

			String extractTableYearComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable.year");

			addSchemaTableColumnComment(
				connection,
				extractTableName, 
				"year", 
				extractTableYearComment);

			String extractTableStudyOrComparisonComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable.studyOrComparison");
			addSchemaTableColumnComment(
				connection,
				extractTableName, 
				"study_or_comparison", 
				extractTableStudyOrComparisonComment);
		
			String extractTableStudyIDComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable.studyID");
			addSchemaTableColumnComment(
				connection,
				extractTableName, 
				"study_id", 
				extractTableStudyIDComment);
					
			String extractTableAreaIDComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable.areaID");
			addSchemaTableColumnComment(
				connection,
				extractTableName, 
				"area_id", 
				extractTableAreaIDComment);
					
			String extractTableBandIDComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable.bandID");
			addSchemaTableColumnComment(
				connection,
				extractTableName, 
				"band_id", 
				extractTableBandIDComment);
			
			String extractTableSexComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable.sex");
			addSchemaTableColumnComment(
				connection,
				extractTableName, 
				"sex", 
				extractTableSexComment);

			String extractTableAgeGroupComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable.ageGroup");
			addSchemaTableColumnComment(
				connection,
				extractTableName, 
				"age_group", 
				extractTableAgeGroupComment);

			String extractTableTotalPopulationComment
				= RIFServiceMessages.getMessage("schemaComments.extractTable.totalPopulation");
			addSchemaTableColumnComment(
				connection,
				extractTableName, 
				"total_pop", 
				extractTableTotalPopulationComment);
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToCreateExtractTable",
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFSubmissionManager.class, 
				errorMessage, 
				sqlException);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			PGSQLQueryUtility.close(statement);
		}
		
	}
	

	private void registerRIFExtractInDatabase() 
		throws RIFServiceException {
		
		
		SQLGeneralQueryFormatter insertStudyAreasQueryFormatter
			= new SQLGeneralQueryFormatter();
		insertStudyAreasQueryFormatter.addQueryLine(0, "INSERT INTO g_rif40_study_areas ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "(study_id, area_id, band_id) ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "SELECT");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id, area_id, band_id ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "FROM ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "rif40_study_areas ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "WHERE ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id=? ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "ORDER BY ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id, area_id, band_id");
		insertStudyAreasQueryFormatter.endWithSemiColon();
		
			
		SQLGeneralQueryFormatter insertComparisonAreasQueryFormatter
			= new SQLGeneralQueryFormatter();
		insertStudyAreasQueryFormatter.addQueryLine(0, "INSERT INTO g_rif40_comparison_areas ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "(study_id, area_id) ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "SELECT");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id, area_id ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "FROM ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "rif40_comparison_areas ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "WHERE ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id=? ");
		insertStudyAreasQueryFormatter.addQueryLine(1, "ORDER BY ");
		insertStudyAreasQueryFormatter.addQueryLine(2, "study_id, area_id");
		insertStudyAreasQueryFormatter.endWithSemiColon();
		
		
		
		//insertStudyAreasQueryFormatter.setIntoTable("g_rif40_study_areas");
		
		
		//try {
			
			
		//}
		//catch(SQLException sqlException) {
			
			
		//}
		//finally {
			
			
		//}
		
	}
	
	
	/*
	 * Creates a temporary table that the middleware uses to store status messages
	 * that can be provided to users so they get interactive feedback about how
	 * their submissions are progressing
	 */
	public void createStudyStatusTable(
		final Connection connection,
		final String userID, 
		final String studyID)
		throws RIFServiceException {
				
		String statusTableName
			= generateUserSchemaStatusTableName(
				userID, 
				studyID);
		
		
		PGSQLDeleteTableQueryFormatter deleteTableQueryFormatter
			= new PGSQLDeleteTableQueryFormatter();
		deleteTableQueryFormatter.setTableToDelete(statusTableName);
		
		PGSQLCreateTableQueryFormatter createTableQueryFormatter
			= new PGSQLCreateTableQueryFormatter();
		createTableQueryFormatter.useTemporaryTable();
		createTableQueryFormatter.setTableName(statusTableName);
		createTableQueryFormatter.addDateFieldDeclaration("log_time", false);		
		createTableQueryFormatter.addTextFieldDeclaration("message", false);
			
		logSQLQuery(
			"deleetStatusTable", 
			deleteTableQueryFormatter);
		logSQLQuery(
			"createStatusTable", 
			createTableQueryFormatter);
		
		PreparedStatement deleteTableStatement = null;
		PreparedStatement createTableStatement = null;
		
		try {
			deleteTableStatement 
				= connection.prepareStatement(deleteTableQueryFormatter.generateQuery());
			deleteTableStatement.executeUpdate();			

			createTableStatement 
				= connection.prepareStatement(createTableQueryFormatter.generateQuery());
			createTableStatement.executeUpdate();		
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToCreateStatusTable",
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFSubmissionManager.class, 
				errorMessage, 
				sqlException);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			PGSQLQueryUtility.close(deleteTableStatement);			
			PGSQLQueryUtility.close(createTableStatement);			
		}		
	}
	
	public void computeSmoothedResults(
		final Connection connection, 
		final AbstractStudy study,
		final String studyID,
		final String userID)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		String mapTableName = generateMapTableName(studyID);
		
		try {
			//Create the map table
			PGSQLCreateTableQueryFormatter queryFormatter 
				= new PGSQLCreateTableQueryFormatter();
			queryFormatter.setTableName(mapTableName);			
			queryFormatter.addTextFieldDeclaration(
				"area_id", 
				300, 
				false);
			queryFormatter.addSmallIntegerFieldDeclaration(
				"year", 
				false);
			queryFormatter.addTextFieldDeclaration(
				"study_or_comparison", 
				1, 
				false);			
			queryFormatter.addIntegerFieldDeclaration(
				"study_id", 
				false);			
			queryFormatter.addTextFieldDeclaration(
				"area_id", 
				false);					
			queryFormatter.addIntegerFieldDeclaration(
				"band_id", 
				true);
			queryFormatter.addSmallIntegerFieldDeclaration(
				"sex", 
				true);					
			queryFormatter.addTextFieldDeclaration(
				"age_group", 
				true);						
			queryFormatter.addDoubleFieldDeclaration(
				"total_pop", 
				true);

			//add fields for covariates
			ArrayList<Investigation> investigations
				= study.getInvestigations();	
			/*
			 * Each investigation should have the same set of covariates.
			 * Therefore, to get the covariates for the study, take the
			 * first investigation and retrieve the collection of covariates
			 */
			ArrayList<AbstractCovariate> covariates
				= investigations.get(0).getCovariates();
			for (AbstractCovariate covariate : covariates) {
				queryFormatter.addTextFieldDeclaration(
					covariate.getName(), 
					true);
			}
			
			/*
			 * Add a column for the name of each investigation
			 */
			for (Investigation investigation : investigations) {
				queryFormatter.addTextFieldDeclaration(
					investigation.getDisplayName(),
					true);
			}
			
			logSQLQuery(
				"createMapTable", 
				queryFormatter);
			
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			resultSet
				= statement.executeQuery();
			
			//Comment the map table
			String mapTableComment
				= RIFServiceMessages.getMessage("schemaComments.mapTable");
			addSchemaTableComment(
				connection, 
				mapTableName, 
				mapTableComment);
			
			String extractTableYearComment
				= RIFServiceMessages.getMessage("schemaComments.mapTable.year");
			addSchemaTableColumnComment(
				connection,
				mapTableName, 
				"year", 
				extractTableYearComment);

			/*
			 * @TODO: KLG
			 * Ignore code for adding comments about covariates because currently
			 * they do not have a description field.
			 */
			/*
			for (AbstractCovariate covariate : covariates) {
				addSchemaTableColumnComment(
					connection,
					mapTableName, 
					covariate.getName(), 
					covariate.getName()); //@TODO: KLG: add new method "getDescription()"
			}
			*/
			
			/*
			 * @TODO: KLG
			 * Ignore code for adding comments about investigations because currently
			 * they do not have a name field.  Given that this is intended to be 
			 * a table column name, it could not be allowed to have spaces or other
			 * strange characters.  
			 */
			/*
			for(Investigation investigation : investigations) {
				addSchemaTableColumnComment(
					connection,
					mapTableName, 
					investigation.getDisplayName(), 
					covariate.getDescription()); //@TODO: KLG: add new method "getDescription()"				
			}
			*/
			
			connection.commit();			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToCreateMapTable",
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFSubmissionManager.class, 
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
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
		
	}	
	
	private void addSchemaTableComment(
		final Connection connection,
		final String tableName,
		final String comment) 
		throws SQLException, 
		RIFServiceException {
	
		PGSQLSchemaCommentQueryFormatter queryFormatter
			= new PGSQLSchemaCommentQueryFormatter();
		queryFormatter.setTableComment(tableName, comment);
		
		logSQLQuery(
			"addSchemaTableComment", 
			queryFormatter);
		
		PreparedStatement statement = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.executeUpdate();
		}
		finally {
			PGSQLQueryUtility.close(statement);
		}		
	}

	
	private void addSchemaTableColumnComment(
		final Connection connection,
		final String tableName,
		final String columnName,
		final String comment) 
		throws SQLException, 
		RIFServiceException {
		
		PGSQLSchemaCommentQueryFormatter queryFormatter
			= new PGSQLSchemaCommentQueryFormatter();
		queryFormatter.setTableColumnComment(tableName, columnName, comment);
		
		logSQLQuery(
			"addSchemaTableColumnComment", 
			queryFormatter);
		
		PreparedStatement statement = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.executeUpdate();
		}
		finally {
			PGSQLQueryUtility.close(statement);
		}		
	}
	
	public String runStudy(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
		
		
		String result = null;
		PreparedStatement runStudyStatement = null;
		PreparedStatement computeResultsStatement = null;
		ResultSet runStudyResultSet = null;
		ResultSet computeResultSet = null;
		
		try {
			
			enableDatabaseDebugMessages(connection);		
			
			PGSQLFunctionCallerQueryFormatter runStudyQueryFormatter = new PGSQLFunctionCallerQueryFormatter();
			runStudyQueryFormatter.setDatabaseSchemaName("rif40_sm_pkg");
			runStudyQueryFormatter.setFunctionName("rif40_run_study");
			runStudyQueryFormatter.setNumberOfFunctionParameters(2);

			logSQLQuery(
				"runStudy", 
				runStudyQueryFormatter,
				studyID,
				"0");
			
			runStudyStatement
				= createPreparedStatement(
					connection,
					runStudyQueryFormatter);
			runStudyStatement.setInt(1, Integer.valueOf(studyID));
			runStudyStatement.setBoolean(2, true);
			runStudyResultSet
				= runStudyStatement.executeQuery();
			runStudyResultSet.next();
			
			result = String.valueOf(runStudyResultSet.getBoolean(1));	
			
			SQLWarning warning = runStudyStatement.getWarnings();
			while (warning != null) {

		        System.out.println("Message:" + warning.getMessage());
		        System.out.println("SQLState:" + warning.getSQLState());
		        System.out.print("Vendor error code: ");
		        System.out.println(warning.getErrorCode());
		        System.out.println("==");

		        warning = warning.getNextWarning();
			}
			
			connection.commit();
			
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRunStudy",
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFSubmissionManager.class, 
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
			PGSQLQueryUtility.close(runStudyStatement);
			PGSQLQueryUtility.close(runStudyResultSet);
			PGSQLQueryUtility.close(computeResultsStatement);
			PGSQLQueryUtility.close(computeResultSet);
		}
	}

	
	public String deleteStudy(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
		
		
		String result = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			
			PGSQLFunctionCallerQueryFormatter queryFormatter = new PGSQLFunctionCallerQueryFormatter();
			queryFormatter.setDatabaseSchemaName("rif40_sm_pkg");
			queryFormatter.setFunctionName("rif40_delete_study");
			queryFormatter.setNumberOfFunctionParameters(1);

			logSQLQuery(
				"deleteStudy", 
				queryFormatter,
				studyID);
			
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet
				= statement.executeQuery();
			resultSet.next();
			
			result = String.valueOf(resultSet.getBoolean(1));	
			
			connection.commit();
			
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToDeleteStudy",
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFSubmissionManager.class, 
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
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
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
		final DiseaseMappingStudy diseaseMappingStudy) 
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
					genderCodeParameter = 0;
				}
				else if (sex == Sex.FEMALES) {
					genderCodeParameter = 1;
				}
				else {
					genderCodeParameter = 2;
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

	public void deleteStudy(
		final Connection connection,
		final User user,
		final AbstractStudy study)
		throws RIFServiceException {
		
		
		String studyID 
			= study.getIdentifier();
		studyStateManager.checkNonExistentStudyID(
			connection,
			user,
			studyID);
		
		PreparedStatement statement = null;
		try {
			PGSQLFunctionCallerQueryFormatter queryFormatter 
				= new PGSQLFunctionCallerQueryFormatter();
			queryFormatter.setDatabaseSchemaName("rif40_sm_pkg");
			queryFormatter.setFunctionName("rif40_delete_study");
			queryFormatter.setNumberOfFunctionParameters(1);
		
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setString(1, study.getIdentifier());
			statement.executeUpdate();
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToDeleteStudy",
					study.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLAgeGenderYearManager.class, 
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
			
			System.out.println("===========================================");
			System.out.println("===========================================");
			System.out.println("Adding 1=="+ diseaseMappingStudyArea.getMapAreas().size()+"==");
			System.out.println("Adding 2=="+ allMapAreas.size()+"==");
			System.out.println("===========================================");
			System.out.println("===========================================");
			
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
			
			GeoLevelSelect geoLevelSelect
				= comparisonArea.getGeoLevelSelect();
			GeoLevelToMap geoLevelToMap
				= comparisonArea.getGeoLevelToMap();

			ArrayList<MapArea> selectedMapAreas
				= comparisonArea.getMapAreas();

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
			
			for (MapArea mapArea : allMapAreas) {
				logSQLQuery("adding to comparison area", queryFormatter, mapArea.getGeographicalIdentifier());		
			}
						
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
			getOutcomeGroupNameQueryFormatter.addFromTable("rif40_numerator_outcome_columns");
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
			String fieldName
				= getOutcomeGroupNameResultSet.getString(2);

			//determine what kinds of codes the numerator table supports
			
			ArrayList<HealthCode> healthCodes
				= investigation.getHealthCodes();
			int totalHealthCodes = healthCodes.size();
			
			//KLG: TODO: try adding one health code maximum
			if (totalHealthCodes > 0) {

				PGSQLInsertQueryFormatter addHealthOutcomeQueryFormatter
					= new PGSQLInsertQueryFormatter();
				addHealthOutcomeQueryFormatter.setIntoTable("rif40_inv_conditions");
				addHealthOutcomeQueryFormatter.addInsertField("min_condition");
				addHealthOutcomeQueryFormatter.addInsertField("outcome_group_name");				
				addHealthOutcomeQueryFormatter.addInsertField("numer_tab");				
				addHealthOutcomeQueryFormatter.addInsertField("field_name");				
				addHealthOutcomeQueryFormatter.addInsertField("line_number");				

				for (int i = 1; i <= totalHealthCodes; i++) {
					HealthCode currentHealthCode = healthCodes.get(i - 1);
										
					logSQLQuery(
						"add_inv_condition", 
						addHealthOutcomeQueryFormatter, 
						currentHealthCode.getCode(),
						outcomeGroupName,
						ndPair.getNumeratorTableName(),
						fieldName,
						String.valueOf(i));
					
					addHealthCodeStatement
						= createPreparedStatement(
							connection,
							addHealthOutcomeQueryFormatter);
					addHealthCodeStatement.setString(1, currentHealthCode.getCode());
					addHealthCodeStatement.setString(2, outcomeGroupName);
					addHealthCodeStatement.setString(3, ndPair.getNumeratorTableName());
					addHealthCodeStatement.setString(4, fieldName);
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

	private String createSQLHealthCodePhrase(
		final Connection connection,
		final NumeratorDenominatorPair ndPair,
		final ArrayList<HealthCode> healthCodes)
		throws SQLException,
		RIFServiceException {

		
		StringBuilder sqlHealthCodePhrase = new StringBuilder();
		int numberOfHealthCodes = healthCodes.size();
		for (int i = 0; i < numberOfHealthCodes; i++) {
			
			if (i != 0) {
				sqlHealthCodePhrase.append(" OR ");
			}
			HealthCode currentHealthCode = healthCodes.get(i);
			String healthCodeTableFieldName 
				= getHealthCodeTableFieldName(
					connection,
					ndPair.getNumeratorTableName());
			sqlHealthCodePhrase.append("\"");
			sqlHealthCodePhrase.append(healthCodeTableFieldName);
			sqlHealthCodePhrase.append("\" LIKE ");
			sqlHealthCodePhrase.append("'");
			sqlHealthCodePhrase.append(currentHealthCode.getCode());
			sqlHealthCodePhrase.append("%'");
		}
		
		return sqlHealthCodePhrase.toString();

	}
	
	private String getHealthCodeTableFieldName(
		final Connection connection,
		final String numeratorTableName) 
		throws SQLException,
		RIFServiceException {

		String result = null;
		
		PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
		queryFormatter.addSelectField("outcome_type");
		queryFormatter.addFromTable("rif40_outcome_groups");
		queryFormatter.addFromTable("rif40_table_outcomes");
		queryFormatter.addWhereJoinCondition(
			"rif40_outcome_groups", 
			"outcome_group_name", 
			"rif40_outcome_groups",
			"outcome_group_name");
		queryFormatter.addWhereParameter("rif40_table_outcomes", "numer_tab");
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.setString(1, numeratorTableName);
			
			resultSet = statement.executeQuery();
			resultSet.next();
			
			result = resultSet.getString(1);
			if (result != null) {
				result = result.toLowerCase();
			}
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
		
		return result;
	}
	
	
	
	public RIFStudySubmission getRIFStudySubmission(
		final Connection connection,
		final User user,
		final String studyID)
		throws RIFServiceException {
		
		PGSQLSampleTestObjectGenerator testDataGenerator
			= new PGSQLSampleTestObjectGenerator();
		RIFStudySubmission rifStudySubmission
			= testDataGenerator.createSampleRIFJobSubmission();
		DiseaseMappingStudy diseaseMappingStudy
			= getDiseaseMappingStudy(
				connection, 
				user, 
				studyID);		
		rifStudySubmission.setStudy(diseaseMappingStudy);
		
		return rifStudySubmission;
	}


	/*
	 * Methods below are for retrieving a study
	 */
	public DiseaseMappingStudy getDiseaseMappingStudy(
		final Connection connection,
		final User user,
		final String studyID) 
		throws RIFServiceException {
				
		//check for non-existent study given id
		//checkNonExistentStudy(studyID);
		
		studyStateManager.checkNonExistentStudyID(
			connection,
			user,
			studyID);

		DiseaseMappingStudy result
			= DiseaseMappingStudy.newInstance();
		try {
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
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToGetDiseaseMappingStudy",
					studyID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}		
	}
	
	private void retrieveGeneralInformationForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws SQLException,
		RIFServiceException {
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addSelectField("study_name");
			queryFormatter.addSelectField("geography");
			//queryFormatter.addSelectField("project");
			queryFormatter.addSelectField("comparison_geolevel_name");
			queryFormatter.addSelectField("study_geolevel_name");
			queryFormatter.addSelectField("denom_tab");
			queryFormatter.addWhereParameter("study_id");
			
			logSQLQuery(
				"retrieveGeneralInformationForStudy", 
				queryFormatter,
				diseaseMappingStudy.getIdentifier());		
			
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setInt(1, Integer.valueOf(diseaseMappingStudy.getIdentifier()));
			
			resultSet = statement.executeQuery();
			resultSet.next();
			
			diseaseMappingStudy.setName(resultSet.getString(1));
			Geography geography
				= Geography.newInstance(resultSet.getString(2), "");
			diseaseMappingStudy.setGeography(geography);		
			
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
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}		
	}
		
	private void retrieveStudyAreaForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws SQLException,
		RIFServiceException {
					
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40_study_areas");
			queryFormatter.addSelectField("area_id");
			queryFormatter.addSelectField("band_id");
			queryFormatter.addWhereParameter("study_id");
			
			logSQLQuery(
				"retrieveStudyAreaForStudy", 
				queryFormatter,
				diseaseMappingStudy.getIdentifier());	
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(1, Integer.valueOf(diseaseMappingStudy.getIdentifier()));
						
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
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
	}	
	
	private void retrieveComparisonAreaForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws SQLException,
		RIFServiceException {
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
			= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40_comparison_areas");
			queryFormatter.addSelectField("area_id");
			queryFormatter.addWhereParameter("study_id");
			
			logSQLQuery(
				"retrieveComparisonAreaForStudy", 
				queryFormatter,
				diseaseMappingStudy.getIdentifier());	

			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(1, Integer.valueOf(diseaseMappingStudy.getIdentifier()));
						
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
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
	}
	
	private void retrieveInvestigationsForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws SQLException,
		RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
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
			
			logSQLQuery(
					"retrieveInvestigationsForStudy", 
					queryFormatter,
					diseaseMappingStudy.getIdentifier());
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(1, Integer.valueOf(diseaseMappingStudy.getIdentifier()));
			resultSet
				= statement.executeQuery();
			while (resultSet.next()) {
				
				Investigation investigation = Investigation.newInstance();
				//set identifier
				int ithParameter = 1;
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
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
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
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		AgeGroup result = null;
		try {
			
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40_age_groups");
			queryFormatter.addSelectField("low_age");
			queryFormatter.addSelectField("high_age");
			queryFormatter.addSelectField("fieldname");	
			queryFormatter.addWhereParameter("age_group_id");
			queryFormatter.addWhereParameter("\"offset\"");
			
			logSQLQuery(
					"getAgeGroupFromIdentifier", 
					queryFormatter,
					"1",
					String.valueOf(ageGroupIdentifier));
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(1, 1);
			statement.setInt(2, Integer.valueOf(ageGroupIdentifier));
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
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}
		
		return result;
	}
	
	private NumeratorDenominatorPair getNDPairForNumeratorTableName(
		final Connection connection,
		final String numeratorTableName) 
		throws SQLException,
		RIFServiceException {

		ResultSet resultSet = null;
		NumeratorDenominatorPair result = null;
		PreparedStatement statement = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setUseDistinct(true);
			queryFormatter.addSelectField("numerator_description");
			queryFormatter.addSelectField("denominator_table");
			queryFormatter.addSelectField("denominator_description");		
			queryFormatter.addFromTable("rif40_num_denom");
			queryFormatter.addWhereParameter("numerator_table");		

			logSQLQuery(
					"getNDPairForNumeratorTableName", 
					queryFormatter,
					String.valueOf(numeratorTableName));			
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, numeratorTableName);
			resultSet = statement.executeQuery();
			resultSet.next();
			result = NumeratorDenominatorPair.newInstance();
			result.setNumeratorTableName(numeratorTableName);
			result.setNumeratorTableDescription(resultSet.getString(1));
			result.setDenominatorTableName(resultSet.getString(2));
			result.setDenominatorTableDescription(resultSet.getString(3));
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
		
		return result;
	}
	
	public ArrayList<StudySummary> getStudySummariesForUser(
		final Connection connection,
		final User user) 
		throws RIFServiceException {
		
		ArrayList<StudySummary> results = new ArrayList<StudySummary>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addSelectField("study_id");
			queryFormatter.addSelectField("study_name");
			queryFormatter.addSelectField("description");
			queryFormatter.addWhereParameter("username");

			logSQLQuery(
					"getNDPairForNumeratorTableName", 
					queryFormatter,
					user.getUserID());			

			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
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

			
			connection.commit();
			return results;			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToGetStudySummariesForUser",
					user.getUserID());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLAgeGenderYearManager.class, 
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
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}		
		
	}

	
	private String generateUserSchemaExtractTableName(
		final String userID,
		final String studyID) {
		
		StringBuilder extractTableName = new StringBuilder();		
		extractTableName.append(userID);
		extractTableName.append(".s");
		extractTableName.append(studyID);
		extractTableName.append("_extract");
		return extractTableName.toString();
	}

	
	private String generateUserSchemaStatusTableName(
		final String userID,
		final String studyID) {
		
		StringBuilder extractTableName = new StringBuilder();		
		extractTableName.append("s");
		extractTableName.append(studyID);
		extractTableName.append("_status");
		return extractTableName.toString();
	}
	
	
	private String generateMapTableName(final String studyID) {
		StringBuilder extractTableName = new StringBuilder();
	
		extractTableName.append("rif_studies.s");
		extractTableName.append(studyID);
		extractTableName.append("_map");
		return extractTableName.toString();
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

			RIFLogger rifLogger = RIFLogger.getLogger();
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
