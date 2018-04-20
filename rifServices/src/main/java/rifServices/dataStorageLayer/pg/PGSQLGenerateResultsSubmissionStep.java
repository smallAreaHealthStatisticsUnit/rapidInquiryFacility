package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.dataStorageLayer.common.SQLManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;

final class PGSQLGenerateResultsSubmissionStep {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private final SQLManager manager;

	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public PGSQLGenerateResultsSubmissionStep(final SQLManager manager) {

		this.manager = manager;
		manager.setEnableLogging(false);
	}

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
		Boolean resultBoolean = false;
		String traceMessage = null;
		
		try {
			
			manager.enableDatabaseDebugMessages(connection);
			
			PGSQLFunctionCallerQueryFormatter runStudyQueryFormatter = new PGSQLFunctionCallerQueryFormatter();
			runStudyQueryFormatter.setDatabaseSchemaName("rif40_sm_pkg");
			runStudyQueryFormatter.setFunctionName("rif40_run_study");
			runStudyQueryFormatter.setNumberOfFunctionParameters(2);
			
			manager.logSQLQuery(
				"runStudy", 
				runStudyQueryFormatter,
				studyID);
			
			runStudyStatement
				= manager.createPreparedStatement(
					connection,
					runStudyQueryFormatter);
			runStudyStatement.setInt(1, Integer.valueOf(studyID));
			runStudyStatement.setBoolean(2, true);
				
			runStudyResultSet
				= runStudyStatement.executeQuery();
			runStudyResultSet.next();
			
			result = String.valueOf(runStudyResultSet.getBoolean(1));
			resultBoolean=runStudyResultSet.getBoolean(1);

			SQLQueryUtility.printWarnings(runStudyStatement); // Print output from PL/PGSQL
			
			SQLQueryUtility.commit(connection);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			
			if (result == null) {
				result="NULL [EXCEPTION THROWN BY DB]";
			}
			
			traceMessage = SQLQueryUtility.printWarnings(runStudyStatement); // Print output from PL/PGSQL
			if (traceMessage == null) {
				traceMessage="No trace from PL/PGSQL Statement.";
			}	
			traceMessage = sqlException.getMessage() + lineSeparator + traceMessage;
			
			manager.logSQLException(sqlException);
		
			SQLQueryUtility.commit(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRunStudy",
					studyID);
/* DO NOT RETHROW!
			rifLogger.error(
				PGSQLGenerateResultsSubmissionStep.class, 
				errorMessage, 
				sqlException);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					sqlException.getMessage());
			throw rifServiceException;
 */
		}
		finally {
			
			//Cleanup database resources			
			SQLQueryUtility.close(runStudyStatement);
			SQLQueryUtility.close(runStudyResultSet);
			SQLQueryUtility.close(computeResultsStatement);
			SQLQueryUtility.close(computeResultSet);

			if (result == null) {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.DATABASE_QUERY_FAILED,
						"Study " + studyID + " had NULL result");
				throw rifServiceException;
			}
			
			if (resultBoolean) {
				rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID + " ran OK XXXXXXXXXXXXXXXXXXXXXX");
			}
			else {
				rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID + " failed with code: " + result + 
					" XXXXXXXXXXXXXXXXXXXXXX");

				if (traceMessage == null) {
					traceMessage="No trace from PL/PGSQL Statement.";
				}			
				RIFServiceExceptionFactory rifServiceExceptionFactory
					= new RIFServiceExceptionFactory();
				RIFServiceException rifServiceException = 
					rifServiceExceptionFactory.createExtractException(traceMessage);
				throw rifServiceException;	
			}
	
			return result;
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
