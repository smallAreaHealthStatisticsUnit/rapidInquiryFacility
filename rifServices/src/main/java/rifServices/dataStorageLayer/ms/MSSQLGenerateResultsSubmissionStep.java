package rifServices.dataStorageLayer.ms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.dataStorageLayer.common.SQLManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;

final class MSSQLGenerateResultsSubmissionStep {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private final SQLManager manager;
	
	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public MSSQLGenerateResultsSubmissionStep(final SQLManager manager) {

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
		CallableStatement runStudyStatement = null;
		PreparedStatement computeResultsStatement = null;
		ResultSet runStudyResultSet = null;
		ResultSet computeResultSet = null;
		boolean res;
		int rval=-1;
		String traceMessage = null;
		
		try {
			SQLGeneralQueryFormatter generalQueryFormatter = new SQLGeneralQueryFormatter();		
			//EXECUTE @rval=rif40.rif40_run_study <study id> <debug: 0/1> <rval>
			String stmt = "{call rif40.rif40_run_study(?, ?, ?)}";
			generalQueryFormatter.addQueryLine(1, stmt);
			
			manager.logSQLQuery(
					"runStudy", 
					generalQueryFormatter,
					studyID);
			
			runStudyStatement
				= manager.createPreparedCall(
					connection,
					stmt); /* This should be generalQueryFormatter but more work needs to be done
							  on Kev's layers to support CallableStatement; only String is supported */
			
			runStudyStatement.setInt(1, Integer.valueOf(studyID));
			runStudyStatement.setInt(2, 1);		
			runStudyStatement.registerOutParameter(3, java.sql.Types.INTEGER);
			
			rifLogger.info(this.getClass(), "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" + lineSeparator + 
				generalQueryFormatter.generateQuery() + lineSeparator + 
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			
			res = runStudyStatement.execute();
			if (res) { // OK we has a resultSet
				runStudyResultSet=runStudyStatement.getResultSet();
				rval = runStudyResultSet.getInt(3);
				runStudyResultSet.next(); // No rows returned
			} // Need to handle no resultSet to retrieve rval
			else {
				rval = runStudyStatement.getInt(3);	
			}
		
			result = String.valueOf(rval);	
			
			SQLQueryUtility.printWarnings(runStudyStatement); // Print output from T-SQL
			
			SQLQueryUtility.commit(connection);
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version, print warning dialogs
			
			if (result == null) {
				result="NULL [EXCEPTION THROWN BY DB]";
				rval=-3;
			}
			
			traceMessage = SQLQueryUtility.printWarnings(runStudyStatement); // Print output from
			// T-SQL
			if (traceMessage == null) {
				traceMessage="No trace from T-SQL Statement.";
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
				MSSQLGenerateResultsSubmissionStep.class, 
				errorMessage, 
				sqlException);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					sqlException.getMessage());
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
			
			if (rval == 1) {
				rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID + " ran OK XXXXXXXXXXXXXXXXXXXXXX");
			}
			else {
				rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID + " failed with code: " + result + 
					" XXXXXXXXXXXXXXXXXXXXXX");

				if (traceMessage == null) {
					traceMessage="No trace from T-SQL Statement.";
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
}
