package rifServices.dataStorageLayer.ms;


import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.ms.MSSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.dataStorageLayer.common.AbstractSQLManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public abstract class MSSQLAbstractSQLManager extends AbstractSQLManager {

	private static String lineSeparator = System.getProperty("line.separator");
	
	protected RIFLogger rifLogger = RIFLogger.getLogger();

	/**
	 * Instantiates a new abstract sql manager.
	 */
	public MSSQLAbstractSQLManager(
		final RIFDatabaseProperties rifDatabaseProperties) {

		super(rifDatabaseProperties);
		
	}
 
	/**
	 * Use appropariate table name case.
	 *
	 * @param tableComponentName the table component name
	 * @return the string
	 */
	protected String useAppropriateTableNameCase(
		final String tableComponentName) {
		
		//TODO: KLG - find out more about when we will need to convert
		//to one case or another
		return tableComponentName;
	}
	
	protected RIFDatabaseProperties getRIFDatabaseProperties() {
		return rifDatabaseProperties;
	}
	
	protected void configureQueryFormatterForDB(
		final AbstractSQLQueryFormatter queryFormatter) {
		
		queryFormatter.setDatabaseType(
			rifDatabaseProperties.getDatabaseType());
		queryFormatter.setCaseSensitive(
			rifDatabaseProperties.isCaseSensitive());
		
	}
	
	protected PreparedStatement createPreparedStatement(
		final Connection connection,
		final AbstractSQLQueryFormatter queryFormatter) 
		throws SQLException {
				
		return PGSQLQueryUtility.createPreparedStatement(
			connection,
			queryFormatter);

	}
	
	protected CallableStatement createPreparedCall( // Use MSSQLQueryUtility
		final Connection connection,
		final String query) 
		throws SQLException {
				
		return MSSQLQueryUtility.createPreparedCall(
			connection,
			query);

	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final String... parameters) {

		final boolean enableLogging = true;
		if (!enableLogging || !queryLoggingIsEnabled(queryName)) {
			return;
		}

		
		StringBuilder queryLog = new StringBuilder();
		queryLog.append("QUERY NAME: ").append(queryName).append(lineSeparator);
		queryLog.append("PARAMETERS:").append(lineSeparator);
		for (int i = 0; i < parameters.length; i++) {
			queryLog.append("\t");
			queryLog.append(i + 1);
			queryLog.append(":\"");
			queryLog.append(parameters[i]);
			queryLog.append("\"").append(lineSeparator);
		}
		queryLog.append("MSSQL QUERY TEXT: ").append(lineSeparator);
		queryLog.append(queryFormatter.generateQuery()).append(lineSeparator);
		queryLog.append("<<< End MSSQLAbstractSQLManager logSQLQuery").append(lineSeparator);
	
		rifLogger.info(this.getClass(), "MSSQLAbstractSQLManager logSQLQuery >>>" + lineSeparator + queryLog.toString());	

	}
	
	protected void logSQLException(final SQLException sqlException) {
		rifLogger.error(this.getClass(), "MSSQLAbstractSQLManager.logSQLException error", sqlException);
	}

	protected void logException(final Exception exception) {
		rifLogger.error(this.getClass(), "MSSQLAbstractSQLManager.logException error", exception);
	}
		
	protected void setAutoCommitOn(
		final Connection connection,
		final boolean isAutoCommitOn)
		throws RIFServiceException {
		
		try {
			connection.setAutoCommit(isAutoCommitOn);			
		}
		catch(SQLException sqlException) {
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createUnableToChangeDBCommitException();
		}
		
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
