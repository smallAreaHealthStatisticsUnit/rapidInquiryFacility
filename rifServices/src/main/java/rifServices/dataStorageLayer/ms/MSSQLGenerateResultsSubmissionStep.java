package rifServices.dataStorageLayer.ms;

import rifServices.system.*;
import rifGenericLibrary.dataStorageLayer.*;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;

import java.sql.*;

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

final class MSSQLGenerateResultsSubmissionStep 
	extends MSSQLAbstractSQLManager {

	
	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public MSSQLGenerateResultsSubmissionStep(
		final RIFDatabaseProperties rifDatabaseProperties) {

		super(rifDatabaseProperties);		
		setEnableLogging(false);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	/**
	 * submit rif study submission.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param rifStudySubmission the rif job submission
	 * @throws RIFServiceException the RIF service exception
	 */	

	public String performStep(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
				
		String result = null;
		PreparedStatement runStudyStatement = null;
		PreparedStatement computeResultsStatement = null;
		ResultSet runStudyResultSet = null;
		ResultSet computeResultSet = null;
		
		try {
			SQLGeneralQueryFormatter generalQueryFormatter = new SQLGeneralQueryFormatter();		
			
			generalQueryFormatter.addQueryLine(1, "BEGIN");
			generalQueryFormatter.addQueryLine(2, "DECLARE @study_id INT=[rif40].[rif40_sequence_current_value] ('rif40.rif40_study_id_seq');");
			generalQueryFormatter.addQueryLine(2, "BEGIN TRANSACTION;");
			generalQueryFormatter.addQueryLine(2, "DECLARE @rval INT;");
			generalQueryFormatter.addQueryLine(2, "DECLARE @msg VARCHAR(MAX);");
			generalQueryFormatter.addUnderline();
			generalQueryFormatter.addQueryLine(2, "BEGIN TRY");
			generalQueryFormatter.addQueryLine(3, "EXECUTE @rval=rif40.rif40_run_study");
			generalQueryFormatter.addQueryLine(3, "@study_id /* Study_id */, ");
			generalQueryFormatter.addQueryLine(3, "1                /* Debug: 0/1 */, ");
			generalQueryFormatter.addQueryLine(3, "default    /* Recursion level: Use default */;");
			generalQueryFormatter.addUnderline();
			generalQueryFormatter.addQueryLine(2, "END TRY");
			generalQueryFormatter.addQueryLine(2, "BEGIN CATCH");
			generalQueryFormatter.addQueryLine(3, "SET @rval=0;");
			generalQueryFormatter.addQueryLine(3, "SET @msg='Caught error in rif40.rif40_run_study(' + CAST(@study_id AS VARCHAR) + ')' + CHAR(10) + ");
			generalQueryFormatter.addQueryLine(3, "'Error number: ' + NULLIF(CAST(ERROR_NUMBER() AS VARCHAR), 'N/A') + CHAR(10) + ");
			generalQueryFormatter.addQueryLine(3, "'Error severity: ' + NULLIF(CAST(ERROR_SEVERITY() AS VARCHAR), 'N/A') + CHAR(10) + ");
			generalQueryFormatter.addQueryLine(3, "'Error state: ' + NULLIF(CAST(ERROR_STATE() AS VARCHAR), 'N/A') + CHAR(10) + ");
			generalQueryFormatter.addQueryLine(3, "'Procedure with error: ' + NULLIF(ERROR_PROCEDURE() + CHAR(10), 'N/A') + ");
			generalQueryFormatter.addQueryLine(3, "'Procedure line: ' + NULLIF(CAST(ERROR_LINE() AS VARCHAR), 'N/A') + CHAR(10) + ");
			generalQueryFormatter.addQueryLine(3, "'Error message: ' + NULLIF(ERROR_MESSAGE(), 'N/A') + CHAR(10);");
			generalQueryFormatter.addQueryLine(3, "PRINT @msg;");
			generalQueryFormatter.addQueryLine(3, "EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif40_run_study]';");
			generalQueryFormatter.addQueryLine(2, "END CATCH;");
			generalQueryFormatter.addUnderline();
			generalQueryFormatter.addCommentLine("Always commit, even though this may fail because trigger failure have caused a rollback:");
			generalQueryFormatter.addCommentLine("The COMMIT TRANSACTION request has no corresponding BEGIN TRANSACTION.");
			generalQueryFormatter.addUnderline();
			generalQueryFormatter.addQueryLine(2, "COMMIT TRANSACTION;");
			generalQueryFormatter.addUnderline();
			generalQueryFormatter.addQueryLine(2, "SET @msg = 'Study ' + CAST(@study_id AS VARCHAR) + ' OK';");
			generalQueryFormatter.addQueryLine(2, "IF @rval = 1");
			generalQueryFormatter.addQueryLine(3, "PRINT @msg;");
			generalQueryFormatter.addQueryLine(2, "ELSE");
			generalQueryFormatter.addQueryLine(3, "RAISERROR('Study %i FAILED (see previous errors)', 16, 1, @study_id);");
			generalQueryFormatter.addQueryLine(1, "END;");
			
			logSQLQuery(
					"runStudy", 
					generalQueryFormatter,
					studyID);
			
			runStudyStatement
				= createPreparedStatement(
					connection,
					generalQueryFormatter);
			
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out.print(generalQueryFormatter.generateQuery());
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			
			
			
			runStudyResultSet
				= runStudyStatement.executeQuery();
					
			runStudyResultSet.next();
				
			result = String.valueOf(runStudyResultSet.getBoolean(1));	
			
			SQLWarning warning = runStudyStatement.getWarnings();
			while (warning != null) {
				
		     //   System.out.println("Message:" + warning.getMessage());
		     //   System.out.println("SQLState:" + warning.getSQLState());
		     //   System.out.print("Vendor error code: ");
		     //   System.out.println(warning.getErrorCode());
		     //   System.out.println("==");
		       
		        warning = warning.getNextWarning();
	
			}
			
			connection.commit();
			return result;
			
			
		/*	TODO: (DM) delete when above is working
			PGSQLFunctionCallerQueryFormatter runStudyQueryFormatter = new PGSQLFunctionCallerQueryFormatter();
			runStudyQueryFormatter.setDatabaseSchemaName("rif40_sm_pkg");
			runStudyQueryFormatter.setFunctionName("rif40_run_study");
			runStudyQueryFormatter.setNumberOfFunctionParameters(2);

			logSQLQuery(
				"runStudy", 
				runStudyQueryFormatter,
				studyID);
			
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
				
		     //   System.out.println("Message:" + warning.getMessage());
		     //   System.out.println("SQLState:" + warning.getSQLState());
		     //   System.out.print("Vendor error code: ");
		     //   System.out.println(warning.getErrorCode());
		     //   System.out.println("==");
		       
		        warning = warning.getNextWarning();

			}
			
			connection.commit();
			
			return result;*/
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
				MSSQLGenerateResultsSubmissionStep.class, 
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
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================
		
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
