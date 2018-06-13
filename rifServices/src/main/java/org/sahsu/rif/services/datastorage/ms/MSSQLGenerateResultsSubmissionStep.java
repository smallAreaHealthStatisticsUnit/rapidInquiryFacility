package org.sahsu.rif.services.datastorage.ms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sahsu.rif.generic.datastorage.GeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.datastorage.common.GenerateResultsSubmissionStep;
import org.sahsu.rif.services.datastorage.common.SQLManager;

public final class MSSQLGenerateResultsSubmissionStep implements GenerateResultsSubmissionStep {

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
	
	@Override
	public void performStep(
			final Connection connection,
			final String studyID)
		throws RIFServiceException {
				
		String result;
		CallableStatement runStudyStatement = null;
		ResultSet runStudyResultSet = null;
		boolean res;
		int rval=-1;

		try {
			GeneralQueryFormatter generalQueryFormatter = new GeneralQueryFormatter();
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
				runStudyResultSet = runStudyStatement.getResultSet();
				rval = runStudyResultSet.getInt(3);
				runStudyResultSet.next(); // No rows returned
			} // Need to handle no resultSet to retrieve rval
			else {
				rval = runStudyStatement.getInt(3);	
			}
		
			result = String.valueOf(rval);	
			
			SQLQueryUtility.printWarnings(runStudyStatement); // Print output from T-SQL
			
			SQLQueryUtility.commit(connection);

			if (rval == 1) {
				rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID
				                                + " ran OK XXXXXXXXXXXXXXXXXXXXXX");
			}
			else {
				rifLogger.info(this.getClass(), "XXXXXXXXXX Study " + studyID
				                                + " failed with code: " + result +
				                                " XXXXXXXXXXXXXXXXXXXXXX");

			}
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version, print warning dialogs

			result="NULL [EXCEPTION THROWN BY DB]";
			rval=-3;

			manager.logSQLException(sqlException);
			SQLQueryUtility.commit(connection);
			
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
		} finally {
		
			//Cleanup database resources			
			SQLQueryUtility.close(runStudyStatement);
			SQLQueryUtility.close(runStudyResultSet);
		}
	}
}
