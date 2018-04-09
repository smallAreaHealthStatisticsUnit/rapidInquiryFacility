package rifServices.dataStorageLayer.ms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.ConnectionQueue;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.ms.MSSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.dataStorageLayer.common.AbstractSQLManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

public abstract class MSSQLAbstractSQLManager extends AbstractSQLManager {
	
	protected static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static final int POOLED_READ_ONLY_CONNECTIONS_PER_PERSON = 10;
	private static final int POOLED_WRITE_CONNECTIONS_PER_PERSON = 5;
	private static String lineSeparator = System.getProperty("line.separator");
	/** The rif service startup options. */
	protected final RIFServiceStartupOptions rifServiceStartupOptions;
	/** The read connection from user. */
	protected final HashMap<String, ConnectionQueue> readOnlyConnectionsFromUser;
	/** The write connection from user. */
	protected final HashMap<String, ConnectionQueue> writeConnectionsFromUser;
	/** The initialisation query. */
	protected final String initialisationQuery;
	/** The database url. */
	protected final String databaseURL;
	protected final HashMap<String, String> passwordHashList;
	protected final HashSet<String> registeredUserIDs;
	protected final HashSet<String> userIDsToBlock;
	

	/**
	 * Instantiates a new abstract sql manager.
	 */
	public MSSQLAbstractSQLManager(final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions.getRIFDatabaseProperties());
		
		userIDsToBlock = new HashSet<>();
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		writeConnectionsFromUser = new HashMap<>();
		registeredUserIDs = new HashSet<>();
		passwordHashList = new HashMap<>();
		readOnlyConnectionsFromUser = new HashMap<>();
		initialisationQuery = "EXEC rif40.rif40_startup ?";
		databaseURL = generateURLText();
		
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
	
	public void configureQueryFormatterForDB(final AbstractSQLQueryFormatter queryFormatter) {
		
		queryFormatter.setDatabaseType(
			rifDatabaseProperties.getDatabaseType());
		queryFormatter.setCaseSensitive(
			rifDatabaseProperties.isCaseSensitive());
		
	}
	
	public PreparedStatement createPreparedStatement(final Connection connection,
			final AbstractSQLQueryFormatter queryFormatter) throws SQLException {
				
		return PGSQLQueryUtility.createPreparedStatement(
			connection,
			queryFormatter);

	}
	
	@Override
	public CallableStatement createPreparedCall( // Use MSSQLQueryUtility
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

	public void logSQLQuery(final String queryName, final AbstractSQLQueryFormatter queryFormatter,
			final String... parameters) {

		final boolean enableLogging = true;
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
		queryLog.append("MSSQL QUERY TEXT: ").append(lineSeparator);
		queryLog.append(queryFormatter.generateQuery()).append(lineSeparator);
		queryLog.append("<<< End MSSQLAbstractSQLManager logSQLQuery").append(lineSeparator);
	
		rifLogger.info(this.getClass(), "MSSQLAbstractSQLManager logSQLQuery >>>" + lineSeparator + queryLog.toString());	

	}
	
	public void logSQLException(final SQLException sqlException) {
		
		rifLogger.error(this.getClass(), "MSSQLAbstractSQLManager.logSQLException error",
				sqlException);
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
	
	/**
	 * Register user.
	 *
	 * @param userID the user id
	 * @param password the password
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	public void login(
		final String userID,
		final String password)
		throws RIFServiceException {
	
		if (userIDsToBlock.contains(userID)) {
			return;
		}
		
		/*
		 * First, check whether person is already logged in.  We can do this
		 * by checking whether
		 */
		
		if (userExists(userID)) {
			return;
		}

		ConnectionQueue readOnlyConnectionQueue = new ConnectionQueue();
		ConnectionQueue writeOnlyConnectionQueue = new ConnectionQueue();
		try {
			Class.forName(rifServiceStartupOptions.getDatabaseDriverClassName());

			//note that in order to optimise the setup of connections,
			//we call rif40_init(boolean no_checks).  The first time we call it
			//for a user, we let the checks occur (set flag to false)
			//for all other times, set the flag to true, to ignore checks

			//Establish read-only connections
			for (int i = 0; i < POOLED_READ_ONLY_CONNECTIONS_PER_PERSON; i++) {
				boolean isFirstConnectionForUser = false;
				if (i == 0) {
					isFirstConnectionForUser = true;
				}
				Connection currentConnection
					= createConnection(
						userID,
						password,
						isFirstConnectionForUser,
						true);
				readOnlyConnectionQueue.addConnection(currentConnection);
			}
			readOnlyConnectionsFromUser.put(userID, readOnlyConnectionQueue);
			
			//Establish write-only connections
			for (int i = 0; i < POOLED_WRITE_CONNECTIONS_PER_PERSON; i++) {
				Connection currentConnection
					= createConnection(
						userID,
						password,
						false,
						false);
				writeOnlyConnectionQueue.addConnection(currentConnection);
			}
			writeConnectionsFromUser.put(userID, writeOnlyConnectionQueue);

			passwordHashList.put(userID, password);
			registeredUserIDs.add(userID);
			
		//	rifLogger.info(this.getClass(), "JAVA LIBRARY PATH >>>");
		//	rifLogger.info(this.getClass(), System.getProperty("java.library.path"));
			
			rifLogger.info(this.getClass(), "XXXXXXXXXXX M S S Q L S E R V E R XXXXXXXXXX");
		}
		catch(ClassNotFoundException classNotFoundException) {
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createUnableLoadDBDriver();
		}
		catch(SQLException sqlException) {
			readOnlyConnectionQueue.closeAllConnections();
			writeOnlyConnectionQueue.closeAllConnections();
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToRegisterUser",
					userID);
			
			rifLogger.error(
					MSSQLConnectionManager.class,
				errorMessage,
				sqlException);
			
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createUnableToRegisterUser(userID);
		}
		
	}
	
	private Connection createConnection(
		final String userID,
		final String password,
		final boolean isFirstConnectionForUser,
		final boolean isReadOnly)
		throws SQLException,
		RIFServiceException {
		
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			
			Properties databaseProperties = new Properties();
			databaseProperties.setProperty("user", userID);
			databaseProperties.setProperty("password", password);
			
			boolean isSSLSupported
				= rifServiceStartupOptions.getRIFDatabaseProperties().isSSLSupported();
			if (isSSLSupported) {
				databaseProperties.setProperty("ssl", "true");
			}

			//databaseProperties.setProperty("logUnclosedConnections", "true");
			databaseProperties.setProperty("prepareThreshold", "3");

			//KLG: @TODO this introduces a porting issue
			//int logLevel = org.postgresql.Driver.DEBUG;
			//databaseProperties.setProperty("loglevel", String.valueOf(logLevel));
			
			connection
				= DriverManager.getConnection(databaseURL, databaseProperties);
			/*
			Connection currentConnection
				= DriverManager.getConnection(
					databaseURL,
					userID,
					password);
			*/
			
			//Execute RIF start-up function
			//MSSQL > EXEC rif40.rif40_startup ?
			//PGSQL > SELECT rif40_startup(?) AS rif40_init;
			
			statement
				= MSSQLQueryUtility.createPreparedStatement(
					connection,
					initialisationQuery);
			
			if (isFirstConnectionForUser) {
				//perform checks
				statement.setInt(1, 1);
			}
			else {
				statement.setInt(1, 0);
			}
			
			
			statement.execute();
			statement.close();

			if (isReadOnly) {
				connection.setReadOnly(true);
			}
			else {
				connection.setReadOnly(false);
			}
			connection.setAutoCommit(false);
		}
		finally {
			MSSQLQueryUtility.close(statement);
		}

		return connection;
	}
	
	/**
	 * Generate url text.
	 *
	 * @return the string
	 */
	private String generateURLText() {
		
		StringBuilder urlText = new StringBuilder();
		
		urlText.append(rifServiceStartupOptions.getDatabaseDriverPrefix());
		urlText.append(":");
		urlText.append("//");
		urlText.append(rifServiceStartupOptions.getHost());
		urlText.append(":");
		urlText.append(rifServiceStartupOptions.getPort());
		urlText.append(";");
		urlText.append("databaseName=");
		urlText.append(rifServiceStartupOptions.getDatabaseName());
		
		return urlText.toString();
	}
	
	public void reclaimPooledReadConnection(
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
			String userID = user.getUserID();
			ConnectionQueue availableReadConnections
				= readOnlyConnectionsFromUser.get(userID);
			availableReadConnections.reclaimConnection(connection);
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToReclaimReadConnection");


			rifLogger.error(
				MSSQLConnectionManager.class,
				errorMessage,
				exception);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		
	}
	
	public void deregisterAllUsers() throws RIFServiceException {
		for (String registeredUserID : registeredUserIDs) {
			closeConnectionsForUser(registeredUserID);
		}
		
		registeredUserIDs.clear();
	}
	
	/**
	 * User password.
	 *
	 * @param user the user id
	 * @return password String, if successful
	 */
	public String getUserPassword(
			final User user) {

		if (userExists(user.getUserID()) && !isUserBlocked(user)) {
			return passwordHashList.get(user.getUserID());
		}
		else {
			return null;
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
