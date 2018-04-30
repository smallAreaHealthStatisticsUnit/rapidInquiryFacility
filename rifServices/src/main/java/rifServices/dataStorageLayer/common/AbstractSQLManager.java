package rifServices.dataStorageLayer.common;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.sun.rowset.CachedRowSetImpl;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.ConnectionQueue;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.files.TomcatBase;
import rifServices.system.files.TomcatFile;

public abstract class AbstractSQLManager implements SQLManager {
	
	private static final Messages SERVICE_MESSAGES = Messages.serviceMessages();
	private static final String ABSTRACT_SQLMANAGER_PROPERTIES = "AbstractSQLManager.properties";
	private static final int MAXIMUM_SUSPICIOUS_EVENTS_THRESHOLD = 5;

	protected static final RIFLogger rifLogger = RIFLogger.getLogger();
	protected static final Set<String> registeredUserIDs = new HashSet<>();;
	protected static final Set<String> userIDsToBlock = new HashSet<>();;

	/** The read connection from user. */
	protected static final Map<String, ConnectionQueue> readOnlyConnectionsFromUser
			= new HashMap<>();

	/** The write connection from user. */
	protected static final Map<String, ConnectionQueue> writeConnectionsFromUser = new HashMap<>();

	private static final HashMap<String, Integer> suspiciousEventCounterFromUser = new HashMap<>();

	protected RIFDatabaseProperties rifDatabaseProperties;

	private static Properties prop = null;
	private static String lineSeparator = System.getProperty("line.separator");
	private static DatabaseType databaseType;

	private ValidationPolicy validationPolicy = ValidationPolicy.STRICT;
	private boolean enableLogging = true;

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
	
	@Override
	public void configureQueryFormatterForDB(
			final AbstractSQLQueryFormatter queryFormatter) {
		
		queryFormatter.setDatabaseType(
			rifDatabaseProperties.getDatabaseType());
		queryFormatter.setCaseSensitive(
			rifDatabaseProperties.isCaseSensitive());
		
	}
	
	@Override
	public PreparedStatement createPreparedStatement(final Connection connection, final AbstractSQLQueryFormatter queryFormatter)
			throws SQLException {
				
		return new SQLQueryUtility().createPreparedStatement(
			connection,
			queryFormatter);
	}

	/** 
	 * Create cached row set from AbstractSQLQueryFormatter.
	 * No checks 0,1 or 1+ rows returned
	 * 
	 * @param connection an SQL connection
	 * @param queryFormatter  the Formatter
	 * @param queryName the name
	 * @param params the query parameters
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
			closeStatement(statement);
		}
		
		return cachedRowSet;			
	}

	/** 
	 * Create cached row set from AbstractSQLQueryFormatter.
	 * No checks 0,1 or 1+ rows returned
	 * 
	 * @param connection,
	 * @param queryFormatter,
	 * @param queryName
	 *
	 * @return CachedRowSetImpl cached row set
	 */
	@Override
	public CachedRowSetImpl createCachedRowSet(
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
			closeStatement(statement);
		}
		
		return cachedRowSet;			
	}
	
	/** 
	 * Create cached row set from AbstractSQLQueryFormatter.
	 * No checks 0,1 or 1+ rows returned
	 * 
	 * @param connection,
	 * @param queryFormatter,
	 * @param queryName,
	 * @param params
	 *
	 * @return CachedRowSetImpl cached row set
	 */
	@Override
	public CachedRowSetImpl createCachedRowSet(
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
			closeStatement(statement);
		}
		
		return cachedRowSet;			
	}

	/** 
	 * Get row from cached row set
	 * Row set must contain one row, and the value must not be null
	 * 
	 * @param cachedRowSet,
	 * @param columnName
	 *
	 * @return String column comment
	 */
	@Override
	public String getColumnFromResultSet(
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
	 * @param cachedRowSet,
	 * @param columnName,
	 * @param allowNulls,
	 * @param allowNoRows
	 *
	 * @return String column comment
	 */
	@Override
	public String getColumnFromResultSet(
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
	 * @param connection,
	 * @param schemaName,
	 * @param tableName,
	 * @param columnName
	 */
	@Override
	public String getColumnComment(Connection connection, String schemaName, String tableName,
			String columnName) throws Exception {
		
		SQLGeneralQueryFormatter columnCommentQueryFormatter = new SQLGeneralQueryFormatter();
		ResultSet resultSet;
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
			closeStatement(statement);
		}
		
		return columnComment;
	}
	
	@Override
	public void enableDatabaseDebugMessages(final Connection connection)
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
			closeStatement(setupLogStatement);
			closeStatement(sendDebugToInfoStatement);	
		}		
	}
	
	@Override
	public void setEnableLogging(final boolean enableLogging) {
		this.enableLogging = enableLogging;
	}	
	
	@Override
	public void logSQLQuery(final String queryName, final AbstractSQLQueryFormatter queryFormatter,
			final String... parameters) {
		
		if (!enableLogging || queryLoggingIsDisabled(queryName)) {
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
		queryLog.append("SQL QUERY TEXT: ").append(lineSeparator);
		queryLog.append(queryFormatter.generateQuery()).append(lineSeparator);
		queryLog.append("<<< End AbstractSQLManager logSQLQuery").append(lineSeparator);
	
		rifLogger.info(this.getClass(), "AbstractSQLManager logSQLQuery >>>" +
			lineSeparator + queryLog.toString());
	}
	
	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final int[] parameters) {
		
		if (!enableLogging || queryLoggingIsDisabled(queryName)) {
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
		queryLog.append("SQL QUERY TEXT: ").append(lineSeparator);
		queryLog.append(queryFormatter.generateQuery()).append(lineSeparator);
		queryLog.append("<<< End AbstractSQLManager logSQLQuery").append(lineSeparator);
	
		rifLogger.info(this.getClass(), "AbstractSQLManager logSQLQuery >>>" +
			lineSeparator + queryLog.toString());	

	}
	
	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter) {
		
		if (!enableLogging || queryLoggingIsDisabled(queryName)) {
			return;
		}
		
		String queryLog = ("QUERY NAME: " + queryName + lineSeparator)
		                  + "NO PARAMETERS." + lineSeparator
		                  + "SQL QUERY TEXT: " + lineSeparator
		                  + queryFormatter.generateQuery() + lineSeparator
		                  + "<<< End AbstractSQLManager logSQLQuery" + lineSeparator;
		rifLogger.info(this.getClass(), "AbstractSQLManager logSQLQuery >>>" +
		                                lineSeparator + queryLog);
	}
		
	@Override
	public void logSQLException(final SQLException sqlException) {
		rifLogger.error(getClass(), "AbstractSQLManager.logSQLException error",
				sqlException);
	}

	protected void logException(final Exception exception) {
		rifLogger.error(this.getClass(), "AbstractSQLManager.logException error",
				 exception);
	}

	protected boolean queryLoggingIsDisabled(
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
				return false;
			}
		}
		String value = prop.getProperty(queryName);
		if (value != null) {
			if (value.toLowerCase().equals("true")) {
				rifLogger.debug(this.getClass(),
						"AbstractSQLManager checkIfQueryLoggingEnabled=TRUE property: " +
								queryName + "=" + value);
				return false;
			} else {
				rifLogger.debug(this.getClass(),
						"AbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: " +
								queryName + "=" + value);
				return true;
			}
		} else {
			rifLogger.warning(this.getClass(),
					"AbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: " +
							queryName + " NOT FOUND");
			return true;
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
	
	@Override
	public boolean isUserBlocked(
			final User user) {
		
		if (user == null) {
			return false;
		}
		
		String userID = user.getUserID();
		return userID != null && userIDsToBlock.contains(userID);
		
	}
	
	@Override
	public void logSuspiciousUserEvent(
			final User user) {

		String userID = user.getUserID();

		Integer suspiciousEventCounter = suspiciousEventCounterFromUser.get(userID);
		if (suspiciousEventCounter == null) {

			//no incidents recorded yet, this is the first
			suspiciousEventCounterFromUser.put(userID, 1);
		}
		else {
			suspiciousEventCounterFromUser.put(userID, ++suspiciousEventCounter);
		}
	}
	
	/**
	 * Assumes that user is valid.
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public Connection assignPooledReadConnection(final User user) throws RIFServiceException {

		Connection result;

		String userID = user.getUserID();
		if (userIDsToBlock.contains(userID)) {
			return null;
		}

		ConnectionQueue availableReadConnectionQueue =
						readOnlyConnectionsFromUser.get(user.getUserID());

		try {
			result = availableReadConnectionQueue.assignConnection();
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage = SERVICE_MESSAGES.getMessage(
					"sqlConnectionManager.error.unableToAssignReadConnection");

			rifLogger.error(
					getClass(),
					errorMessage,
					exception);
			
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}
		return result;
	}
	
	@Override
	public void reclaimPooledWriteConnection(
			final User user,
			final Connection connection)
					throws RIFServiceException {

		try {

			if (user == null) {
				return;
			}
			if (connection == null) {
				return;
			}

			//connection.setAutoCommit(true);
			ConnectionQueue writeOnlyConnectionQueue
			= writeConnectionsFromUser.get(user.getUserID());
			writeOnlyConnectionQueue.reclaimConnection(connection);
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage = SERVICE_MESSAGES.getMessage(
					"sqlConnectionManager.error.unableToReclaimWriteConnection");

			rifLogger.error(getClass(), errorMessage, exception);
			
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}
	}
	
	@Override
	public void logout(final User user) throws RIFServiceException {

		if (user == null) {
			return;
		}

		String userID = user.getUserID();
		if (userID == null) {
			return;
		}

		if (!registeredUserIDs.contains(userID)) {
			//Here we anticipate the possibility that the user
			//may not be registered.  In this case, there is no chance
			//that there are connections that need to be closed for that ID
			return;
		}

		closeConnectionsForUser(userID);
		registeredUserIDs.remove(userID);
		suspiciousEventCounterFromUser.remove(userID);
	}
	
	/**
	 * Deregister user.
	 *
	 * @param userID the user
	 * @throws RIFServiceException the RIF service exception
	 */
	protected void closeConnectionsForUser(
			final String userID)
					throws RIFServiceException {

		ConnectionQueue readOnlyConnectionQueue
		= readOnlyConnectionsFromUser.get(userID);
		if (readOnlyConnectionQueue != null) {
			readOnlyConnectionQueue.closeAllConnections();
		}

		ConnectionQueue writeConnectionQueue
		= writeConnectionsFromUser.get(userID);
		if (writeConnectionQueue != null) {
			writeConnectionQueue.closeAllConnections();
		}
	}
	
	@Override
	public boolean isLoggedIn(
			final String userID) {

		return registeredUserIDs.contains(userID);
	}
	
	@Override
	public boolean userExceededMaximumSuspiciousEvents(final User user) {
		
		String userID = user.getUserID();
		Integer suspiciousEventCounter
						= suspiciousEventCounterFromUser.get(userID);
		return suspiciousEventCounter != null
		       && suspiciousEventCounter >= MAXIMUM_SUSPICIOUS_EVENTS_THRESHOLD;
		
	}
	
	/**
	 * User exists.
	 *
	 * @param userID the user id
	 * @return true, if successful
	 */
	@Override
	public boolean userExists(final String userID) {

		return isLoggedIn(userID);
	}
	
	@Override
	public void addUserIDToBlock(final User user) {

		if (user == null) {
			return;
		}

		String userID = user.getUserID();
		if (userID == null) {
			return;
		}

		if (userIDsToBlock.contains(userID)) {
			return;
		}

		userIDsToBlock.add(userID);
	}
	
	/**
	 * Assumes that user is valid.
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public Connection assignPooledWriteConnection(
			final User user)
					throws RIFServiceException {

		Connection result;
		try {

			String userID = user.getUserID();
			if (userIDsToBlock.contains(userID)) {
				return null;
			}

			ConnectionQueue writeConnectionQueue = writeConnectionsFromUser.get(user.getUserID());
			result = writeConnectionQueue.assignConnection();
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage = SERVICE_MESSAGES.getMessage(
					"sqlConnectionManager.error.unableToAssignWriteConnection");

			rifLogger.error(
					getClass(),
					errorMessage,
					exception);
			
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}

		return result;
	}

	private void closeStatement(PreparedStatement statement) {

		if (statement == null) {
			return;
		}

		try {
			statement.close();
		}
		catch(SQLException ignore) {}
	}
}
