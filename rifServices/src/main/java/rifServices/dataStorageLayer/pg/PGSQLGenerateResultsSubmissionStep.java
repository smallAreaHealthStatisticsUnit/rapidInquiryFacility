package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import rifGenericLibrary.dataStorageLayer.FunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.dataStorageLayer.common.GenerateResultsSubmissionStep;
import rifServices.dataStorageLayer.common.SQLManager;

public final class PGSQLGenerateResultsSubmissionStep implements GenerateResultsSubmissionStep {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	private final SQLManager manager;

	public PGSQLGenerateResultsSubmissionStep(final SQLManager manager) {

		this.manager = manager;
		manager.setEnableLogging(false);
	}

	/**
	 * submit rif study submission.
	 *
	 * @param user the user
	 * @param rifStudySubmission the rif job submission
	 * @param connection the connection
	 * @throws RIFServiceException the RIF service exception
	 */	
	@Override
	public void performStep(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
				
		PreparedStatement runStudyStatement = null;
		ResultSet runStudyResultSet = null;

		try {
			
			manager.enableDatabaseDebugMessages(connection);
			
			FunctionCallerQueryFormatter runStudyQueryFormatter = new FunctionCallerQueryFormatter();
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
				
			runStudyResultSet = runStudyStatement.executeQuery();
			runStudyResultSet.next();
			
			SQLQueryUtility.printWarnings(runStudyStatement); // Print output from PL/PGSQL
			
			SQLQueryUtility.commit(connection);
			rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID
			                                + " ran OK XXXXXXXXXXXXXXXXXXXXXX");
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version

			manager.logSQLException(sqlException);
		
			SQLQueryUtility.commit(connection);

			rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID
			                                + " failed XXXXXXXXXXXXXXXXXXXXXX");

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
		} finally {
			
			//Cleanup database resources
			SQLQueryUtility.close(runStudyStatement);
			SQLQueryUtility.close(runStudyResultSet);
		}
	}
}
