package rifServices.dataStorageLayer.common;

import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.common.SQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;

import java.io.IOException;
import java.util.Properties;

import java.sql.*;
import com.sun.rowset.CachedRowSetImpl;
import rifServices.system.files.TomcatBase;
import rifServices.system.files.TomcatFile;

public abstract class AbstractSQLManager implements SQLManager {

	private static final String ABSTRACT_SQLMANAGER_PROPERTIES = "AbstractSQLManager.properties";

	protected RIFDatabaseProperties rifDatabaseProperties;
	private ValidationPolicy validationPolicy = ValidationPolicy.STRICT;
	private boolean enableLogging = true;
	private static Properties prop = null;
	private static String lineSeparator = System.getProperty("line.separator");
	
	protected static final RIFLogger rifLogger = RIFLogger.getLogger();
	
	private static DatabaseType databaseType;

	/**
	 * Instantiates a new abstract sql manager.
	 */
	public AbstractSQLManager(
		final RIFDatabaseProperties rifDatabaseProperties) {

		this.rifDatabaseProperties = rifDatabaseProperties;
		databaseType = this.rifDatabaseProperties.getDatabaseType();
	}

	@Override
	public ValidationPolicy getValidationPolicy() {
		return validationPolicy;
	}
		
	@Override
	public void setValidationPolicy(
			final ValidationPolicy validationPolicy) {
		
		this.validationPolicy = validationPolicy;
	}
	
	/**
	 * Use appropriate table name case.
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
				
		return SQLQueryUtility.createPreparedStatement(
			connection,
			queryFormatter);
	}

	/** 
	 * Create cached row set from AbstractSQLQueryFormatter.
	 * No checks 0,1 or 1+ rows returned
	 * 
	 * @param Connection connection, 
	 * @param AbstractSQLQueryFormatter queryFormatter, 
	 * @param String queryName, 
	 * @param String[] params
	 *
	 * @return CachedRowSetImpl cached row set
	 */		
	protected CachedRowSetImpl createCachedRowSet(
			final Connection connection,
			AbstractSQLQueryFormatter queryFormatter,
			final String queryName,
			final String[] params)
				throws Exception {
			
		CachedRowSetImpl cachedRowSet=null;	
		ResultSet resultSet=null;
		PreparedStatement statement = createPreparedStatement(connection, queryFormatter);		
		try {
			for (int i=0; i < params.length; i++) {
				statement.setString((i+1), params[i]);	
			}
			logSQLQuery(queryName, queryFormatter, params);	
			resultSet = statement.executeQuery();
			 // create CachedRowSet and populate
			cachedRowSet = new CachedRowSetImpl();
			cachedRowSet.populate(resultSet);
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + queryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
		return cachedRowSet;			
	}

	/** 
	 * Create cached row set from AbstractSQLQueryFormatter.
	 * No checks 0,1 or 1+ rows returned
	 * 
	 * @param Connection connection, 
	 * @param AbstractSQLQueryFormatter queryFormatter, 
	 * @param String queryName
	 *
	 * @return CachedRowSetImpl cached row set
	 */	
	protected CachedRowSetImpl createCachedRowSet(
			final Connection connection,
			AbstractSQLQueryFormatter queryFormatter,
			final String queryName)
				throws Exception {
			
		CachedRowSetImpl cachedRowSet=null;	
		ResultSet resultSet=null;
		PreparedStatement statement = createPreparedStatement(connection, queryFormatter);		
		try {
			logSQLQuery(queryName, queryFormatter);	
			resultSet = statement.executeQuery();
			 // create CachedRowSet and populate
			cachedRowSet = new CachedRowSetImpl();
			cachedRowSet.populate(resultSet);
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + queryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
		return cachedRowSet;			
	}
	
	/** 
	 * Create cached row set from AbstractSQLQueryFormatter.
	 * No checks 0,1 or 1+ rows returned
	 * 
	 * @param Connection connection, 
	 * @param AbstractSQLQueryFormatter queryFormatter, 
	 * @param String queryName, 
	 * @param int[] params
	 *
	 * @return CachedRowSetImpl cached row set
	 */	
	protected CachedRowSetImpl createCachedRowSet(
			final Connection connection,
			AbstractSQLQueryFormatter queryFormatter,
			final String queryName,
			final int[] params)
				throws Exception {
			
		CachedRowSetImpl cachedRowSet=null;	
		ResultSet resultSet=null;
		PreparedStatement statement = createPreparedStatement(connection, queryFormatter);		
		try {
			for (int i=0; i < params.length; i++) {
				statement.setInt((i+1), params[i]);	
			}
			logSQLQuery(queryName, queryFormatter, params);	
			resultSet = statement.executeQuery();
			 // create CachedRowSet and populate
			cachedRowSet = new CachedRowSetImpl();
			cachedRowSet.populate(resultSet);
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + queryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
		return cachedRowSet;			
	}

	/** 
	 * Get row from cached row set
	 * Row set must contain one row, and the value must not be null
	 * 
	 * @param CachedRowSetImpl cachedRowSet, 
	 * @param String columnName
	 *
	 * @return String column comment
	 */		
	protected String getColumnFromResultSet(
			final CachedRowSetImpl cachedRowSet,
			final String columnName)
			throws Exception {
		return getColumnFromResultSet(cachedRowSet, columnName, 
			false /* allowNulls */, false /* allowNoRows */);
	}

	/** 
	 * Get row from cached row set
	 * Checks 0,1 or 1+ rows; assumed multiple rows not permitted
	 * Flag controls 0/1 null/not null checks
	 * 
	 * @param achedRowSetImpl cachedRowSet, 
	 * @param String columnName, 
	 * @param boolean allowNulls, 
	 * @param boolean allowNoRows
	 *
	 * @return String column comment
	 */		
	protected String getColumnFromResultSet(
			final CachedRowSetImpl cachedRowSet,
			final String columnName,
			final boolean allowNulls,
			final boolean allowNoRows)
			throws Exception {
			
		String columnValue=null;
		boolean columnFound=false;
		if (cachedRowSet.first()) {			
			ResultSetMetaData rsmd = cachedRowSet.getMetaData();
			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);
				String value = cachedRowSet.getString(i);	
				
				if (name.toUpperCase().equals(columnName.toUpperCase())) {
					columnValue=value;
					columnFound=true;
				} 
			}
			if (cachedRowSet.next()) {
				throw new Exception("getColumnFromResultSet(): expected 1 row, got >1");
			}
			if (!columnFound) {
				throw new Exception("getColumnFromResultSet(): column not found: " + columnName);
			}
			if (columnValue == null && !allowNulls) {
				throw new Exception("getColumnFromResultSet(): got null for column: " + columnName);
			}
		}
		else if (!allowNoRows) {
			throw new Exception("getColumnFromResultSet(): expected 1 row, got none");
		}
		
		return columnValue;
	}

	/** 
	 * Get column comment from data dictionary
	 * 
	 * @param Connection connection, 
	 * @param String schemaName, 
	 * @param String tableName, 
	 * @param String columnName
	 */	
	protected String getColumnComment(Connection connection, 
		String schemaName, String tableName, String columnName)
			throws Exception {
		SQLGeneralQueryFormatter columnCommentQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		if (databaseType == DatabaseType.POSTGRESQL) {
			columnCommentQueryFormatter.addQueryLine(0, // Postgres
				"SELECT pg_catalog.col_description(c.oid, cols.ordinal_position::int) AS column_comment");
			columnCommentQueryFormatter.addQueryLine(0, "  FROM pg_catalog.pg_class c, information_schema.columns cols");
			columnCommentQueryFormatter.addQueryLine(0, " WHERE cols.table_catalog = current_database()");
			columnCommentQueryFormatter.addQueryLine(0, "   AND cols.table_schema  = ?");
			columnCommentQueryFormatter.addQueryLine(0, "   AND cols.table_name    = ?");
			columnCommentQueryFormatter.addQueryLine(0, "   AND cols.table_name    = c.relname");
			columnCommentQueryFormatter.addQueryLine(0, "   AND cols.column_name   = ?");
		}
		else if (databaseType == DatabaseType.SQL_SERVER) {
			columnCommentQueryFormatter.addQueryLine(0, "SELECT CAST(value AS VARCHAR(2000)) AS column_comment"); // SQL Server
			columnCommentQueryFormatter.addQueryLine(0, "FROM fn_listextendedproperty (NULL, 'schema', ?, 'table', ?, 'column', ?)");
			columnCommentQueryFormatter.addQueryLine(0, "UNION");
			columnCommentQueryFormatter.addQueryLine(0, "SELECT CAST(value AS VARCHAR(2000)) AS column_comment");
			columnCommentQueryFormatter.addQueryLine(0, "FROM fn_listextendedproperty (NULL, 'schema', ?, 'view', ?, 'column', ?)");
		}
		else {
			throw new Exception("getColumnComment(): invalid databaseType: " + 
				databaseType);
		}
		PreparedStatement statement = createPreparedStatement(connection, columnCommentQueryFormatter);
		
		String columnComment=columnName.substring(0, 1).toUpperCase() + 
			columnName.substring(1).replace("_", " "); // Default if not found [initcap, remove underscores]
		try {			
		
			statement.setString(1, schemaName);	
			statement.setString(2, tableName);	
			statement.setString(3, columnName);	
			if (databaseType == DatabaseType.SQL_SERVER) {
				statement.setString(4, schemaName);	
				statement.setString(5, tableName);
				statement.setString(6, columnName);		
			}			
			resultSet = statement.executeQuery();
			if (resultSet.next()) {		
				columnComment=resultSet.getString(1);
				if (resultSet.next()) {		
					throw new Exception("getColumnComment() database: " + databaseType +
						"; expected 1 row, got >1");
				}
			}
			else {
				rifLogger.debug(this.getClass(), "getColumnComment() database: " + databaseType +
					"; expected 1 row, got none");
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement (" + databaseType + ") >>> " + 
				lineSeparator + columnCommentQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
		return columnComment;
	}
	
	protected void enableDatabaseDebugMessages(
		final Connection connection) 
		throws RIFServiceException {
			
		SQLFunctionCallerQueryFormatter setupDatabaseLogQueryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		setupDatabaseLogQueryFormatter.setDatabaseSchemaName("rif40_log_pkg");
		setupDatabaseLogQueryFormatter.setFunctionName("rif40_log_setup");
		setupDatabaseLogQueryFormatter.setNumberOfFunctionParameters(0);		
		PreparedStatement setupLogStatement = null;
		
		SQLFunctionCallerQueryFormatter sendDebugToInfoQueryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		sendDebugToInfoQueryFormatter.setDatabaseSchemaName("rif40_log_pkg");
		sendDebugToInfoQueryFormatter.setFunctionName("rif40_send_debug_to_info");
		sendDebugToInfoQueryFormatter.setNumberOfFunctionParameters(1);		
		
		
		PreparedStatement sendDebugToInfoStatement = null;
		try {
			setupLogStatement 
				= createPreparedStatement(
					connection, 
					setupDatabaseLogQueryFormatter);
			setupLogStatement.executeQuery();
			
			sendDebugToInfoStatement 
				= createPreparedStatement(
					connection, 
					sendDebugToInfoQueryFormatter);
			sendDebugToInfoStatement.setBoolean(1, true);
			sendDebugToInfoStatement.executeQuery();						
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage("abstractSQLManager.error.unableToEnableDatabaseDebugging");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_TO_MAINTAIN_DEBUG, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(setupLogStatement);
			SQLQueryUtility.close(sendDebugToInfoStatement);	
		}		
	}
	
	@Override
	public void setEnableLogging(final boolean enableLogging) {
		this.enableLogging = enableLogging;
	}	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final String... parameters) {
		
		if (!enableLogging || !queryLoggingIsEnabled(queryName)) {
			return;
		}

		StringBuilder queryLog = new StringBuilder();
		queryLog.append("QUERY NAME: " + queryName + lineSeparator);
		queryLog.append("PARAMETERS:" + lineSeparator);
		for (int i = 0; i < parameters.length; i++) {
			queryLog.append("\t");
			queryLog.append(i + 1);
			queryLog.append(":\"");
			queryLog.append(parameters[i]);
			queryLog.append("\"" + lineSeparator);			
		}
		queryLog.append("PGSQL QUERY TEXT: " + lineSeparator);
		queryLog.append(queryFormatter.generateQuery() + lineSeparator);
		queryLog.append("<<< End AbstractSQLManager logSQLQuery" + lineSeparator);
	
		rifLogger.info(this.getClass(), "AbstractSQLManager logSQLQuery >>>" +
			lineSeparator + queryLog.toString());	

	}
	
	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final int[] parameters) {
		
		if (enableLogging == false || queryLoggingIsEnabled(queryName) == false) {
			return;
		}

		StringBuilder queryLog = new StringBuilder();
		queryLog.append("QUERY NAME: " + queryName + lineSeparator);
		queryLog.append("PARAMETERS:" + lineSeparator);
		for (int i = 0; i < parameters.length; i++) {
			queryLog.append("\t");
			queryLog.append(i + 1);
			queryLog.append(":\"");
			queryLog.append(parameters[i]);
			queryLog.append("\"" + lineSeparator);			
		}
		queryLog.append("PGSQL QUERY TEXT: " + lineSeparator);
		queryLog.append(queryFormatter.generateQuery() + lineSeparator);
		queryLog.append("<<< End AbstractSQLManager logSQLQuery" + lineSeparator);
	
		rifLogger.info(this.getClass(), "AbstractSQLManager logSQLQuery >>>" +
			lineSeparator + queryLog.toString());	

	}
	
	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter) {
		
		if (enableLogging == false || queryLoggingIsEnabled(queryName) == false) {
			return;
		}

		StringBuilder queryLog = new StringBuilder();
		queryLog.append("QUERY NAME: " + queryName + lineSeparator);
		queryLog.append("NO PARAMETERS." + lineSeparator);
		queryLog.append("PGSQL QUERY TEXT: " + lineSeparator);
		queryLog.append(queryFormatter.generateQuery() + lineSeparator);
		queryLog.append("<<< End AbstractSQLManager logSQLQuery" + lineSeparator);
	
		rifLogger.info(this.getClass(), "AbstractSQLManager logSQLQuery >>>" +
			lineSeparator + queryLog.toString());	

	}
		
	protected void logSQLException(final SQLException sqlException) {
		rifLogger.error(this.getClass(), "AbstractSQLManager.logSQLException error", sqlException);
	}

	protected void logException(final Exception exception) {
		rifLogger.error(this.getClass(), "AbstractSQLManager.logException error", exception);
	}

	protected boolean queryLoggingIsEnabled(
			final String queryName) {

		if (prop == null) {

			try {
				prop = new TomcatFile(
						new TomcatBase(),
						AbstractSQLManager.ABSTRACT_SQLMANAGER_PROPERTIES).properties();
			} catch (IOException e) {
				rifLogger.warning(this.getClass(),
						"AbstractSQLManager.checkIfQueryLoggingEnabled error for" +
						AbstractSQLManager.ABSTRACT_SQLMANAGER_PROPERTIES, e);
				return true;
			}
		}
		String value = prop.getProperty(queryName);
		if (value != null) {
			if (value.toLowerCase().equals("true")) {
				rifLogger.debug(this.getClass(),
						"AbstractSQLManager checkIfQueryLoggingEnabled=TRUE property: " +
								queryName + "=" + value);
				return true;
			} else {
				rifLogger.debug(this.getClass(),
						"AbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: " +
								queryName + "=" + value);
				return false;
			}
		} else {
			rifLogger.warning(this.getClass(),
					"AbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: " +
							queryName + " NOT FOUND");
			return false;
		}
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
}
