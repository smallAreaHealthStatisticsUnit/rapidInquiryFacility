package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.ConnectionQueue;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifServices.dataStorageLayer.common.AbstractSQLManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceStartupOptions;

public abstract class PGSQLAbstractSQLManager extends AbstractSQLManager {
	
	private static final int POOLED_READ_ONLY_CONNECTIONS_PER_PERSON = 10;
	private static final int POOLED_WRITE_CONNECTIONS_PER_PERSON = 5;
	private static final Messages SERVICE_MESSAGES = Messages.serviceMessages();

	/** The rif service startup options. */
	protected final RIFServiceStartupOptions rifServiceStartupOptions;
	/** The initialisation query. */
	private final String initialisationQuery;
	/** The database url. */
	private final String databaseURL;
	private static HashMap<String, String> passwordHashList;
	
	/**
	 * Instantiates a new abstract sql manager.
	 */
	public PGSQLAbstractSQLManager(final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions.getRIFDatabaseProperties());
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		databaseURL = generateURLText();
		passwordHashList = new HashMap<>();
		initialisationQuery = "SELECT rif40_startup(?) AS rif40_init;";
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
	public void enableDatabaseDebugMessages(
			final Connection connection)
		throws RIFServiceException {
			
		PGSQLFunctionCallerQueryFormatter setupDatabaseLogQueryFormatter 
			= new PGSQLFunctionCallerQueryFormatter();
		setupDatabaseLogQueryFormatter.setDatabaseSchemaName("rif40_log_pkg");
		setupDatabaseLogQueryFormatter.setFunctionName("rif40_log_setup");
		setupDatabaseLogQueryFormatter.setNumberOfFunctionParameters(0);		
		PreparedStatement setupLogStatement = null;
		
		PGSQLFunctionCallerQueryFormatter sendDebugToInfoQueryFormatter 
			= new PGSQLFunctionCallerQueryFormatter();
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
				= SERVICE_MESSAGES.getMessage("abstractSQLManager.error.unableToEnableDatabaseDebugging");

			throw new RIFServiceException(
				RIFServiceError.DB_UNABLE_TO_MAINTAIN_DEBUG,
				errorMessage);
		}
		finally {
			PGSQLQueryUtility.close(setupLogStatement);
			PGSQLQueryUtility.close(sendDebugToInfoStatement);	
		}		
	}

	protected void logException(final Exception exception) {
		rifLogger.error(this.getClass(), "PGSQLAbstractSQLManager.logException error", exception);
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
	 * Generate url text.
	 *
	 * @return the string
	 */
	private String generateURLText() {
		
		return rifServiceStartupOptions.getDatabaseDriverPrefix()
		       + ":"
		       + "//"
		       + rifServiceStartupOptions.getHost()
		       + ":"
		       + rifServiceStartupOptions.getPort()
		       + "/"
		       + rifServiceStartupOptions.getDatabaseName();
	}
	
	/**
	 * User password.
	 *
	 * @param user the user id
	 * @return password String, if successful
	 */
	@Override
	public String getUserPassword(final User user) {

		if (userExists(user.getUserID()) && !isUserBlocked(user)) {
			return passwordHashList.get(user.getUserID());
		}
		else {
			return null;
		}
	}
	
	/**
	 * Register user.
	 *
	 * @param userID the user id
	 * @param password the password
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public void login(final String userID, final String password) throws RIFServiceException {

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
				Connection currentConnection = createConnection(userID, password,
								isFirstConnectionForUser, true);
				readOnlyConnectionQueue.addConnection(currentConnection);
			}
			readOnlyConnectionsFromUser.put(userID, readOnlyConnectionQueue);

			//Establish write-only connections
			for (int i = 0; i < POOLED_WRITE_CONNECTIONS_PER_PERSON; i++) {
				Connection currentConnection = createConnection(userID, password,
								false, false);
				writeOnlyConnectionQueue.addConnection(currentConnection);
			}
			writeConnectionsFromUser.put(userID, writeOnlyConnectionQueue);
			if (passwordHashList == null) {
				passwordHashList = new HashMap<>();
			}
			registeredUserIDs.add(userID);
			rifLogger.info(this.getClass(), "XXXXXXXXXXX P O S T G R E S Q L XXXXXXXXXX");
		}
		catch(ClassNotFoundException classNotFoundException) {
			RIFServiceExceptionFactory exceptionFactory
			= new RIFServiceExceptionFactory();
			throw exceptionFactory.createUnableLoadDBDriver();
		}
		catch(SQLException sqlException) {
			readOnlyConnectionQueue.closeAllConnections();
			writeOnlyConnectionQueue.closeAllConnections();
			String errorMessage = SERVICE_MESSAGES.getMessage(
							"sqlConnectionManager.error.unableToRegisterUser", userID);

			rifLogger.error(PGSQLConnectionManager.class, errorMessage, sqlException);

			RIFServiceExceptionFactory exceptionFactory = new RIFServiceExceptionFactory();
			throw exceptionFactory.createUnableToRegisterUser(userID);
		}
	}
	
	@Override
	public void reclaimPooledReadConnection(final User user, final Connection connection)
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
			String errorMessage = SERVICE_MESSAGES.getMessage(
							"sqlConnectionManager.error.unableToReclaimReadConnection");

			rifLogger.error(
					PGSQLConnectionManager.class,
					errorMessage,
					exception);
			
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}
	}
	
	@Override
	public void deregisterAllUsers() throws RIFServiceException {
		for (String registeredUserID : registeredUserIDs) {
			closeConnectionsForUser(registeredUserID);
		}

		registeredUserIDs.clear();
	}
	
	private Connection createConnection(
					final String userID,
					final String password,
					final boolean isFirstConnectionForUser,
					final boolean isReadOnly) throws SQLException, RIFServiceException {

		Connection connection;
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

			databaseProperties.setProperty("prepareThreshold", "3");
			connection = DriverManager.getConnection(databaseURL, databaseProperties);
			statement = PGSQLQueryUtility.createPreparedStatement(connection, initialisationQuery);
			
			if (isFirstConnectionForUser) {

				statement.setBoolean(1, false);
			}
			else {
				statement.setBoolean(1, true);
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
			PGSQLQueryUtility.close(statement);
		}

		return connection;
	}
}
