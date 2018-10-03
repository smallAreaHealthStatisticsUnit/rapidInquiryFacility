package org.sahsu.rif.services.datastorage.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sahsu.rif.generic.datastorage.FunctionCallerQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.datastorage.common.GenerateResultsSubmissionStep;
import org.sahsu.rif.services.datastorage.common.SQLManager;

public final class PGSQLGenerateResultsSubmissionStep implements GenerateResultsSubmissionStep {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");

	private final SQLManager manager;

	private String result = null;
	private String stack = null;
	
	public PGSQLGenerateResultsSubmissionStep(final SQLManager manager) {

		this.manager = manager;
		manager.setEnableLogging(false);
	}
	
	@Override
	public String getResult() {
		return result;
	}	
	@Override
	public String getStack() {
		return stack;
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
	public boolean performStep(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
				
		PreparedStatement runStudyStatement = null;
		ResultSet runStudyResultSet = null;
		boolean res=false;
		int rval=-1;

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
			runStudyStatement.setBoolean(2, true); // Debug
				
			runStudyResultSet = runStudyStatement.executeQuery(); // Returns true/false
			rval = runStudyResultSet.getInt(1);
			runStudyResultSet.next();
			
			stack=SQLQueryUtility.printWarnings(runStudyStatement); // Print output from PL/PGSQL
			
			SQLQueryUtility.commit(connection);
			rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID + " ran OK XXXXXXXXXXXXXXXXXXXXXX");
			if (rval == 1) {
				result="OK";
				res=true;
			}
			else {
				result="Study failed with code: " + rval;
			}
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version

			manager.logSQLException(sqlException);
			String sqlWarnings=SQLQueryUtility.printWarnings(runStudyStatement); // Print output from PL/PGSQL
			result=sqlException.getMessage();
			StringBuilder builder = new StringBuilder(sqlException.getMessage())
					                        .append(lineSeparator)
					                        .append("=============================================")
					                        .append(lineSeparator)
					                        .append("Stack trace of cause follows")
					                        .append(lineSeparator)
					                        .append("=============================================")
					                        .append(lineSeparator);
			for (StackTraceElement element : sqlException.getStackTrace()) {
				builder.append(element.toString()).append(lineSeparator);
			}
			builder.append("=============================================")
					                        .append(lineSeparator)
					                        .append("Output from PL/PGSQL")
					                        .append(lineSeparator)
											.append(sqlWarnings);
			stack=builder.toString();
		
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
			return res;
		}
	}
}
