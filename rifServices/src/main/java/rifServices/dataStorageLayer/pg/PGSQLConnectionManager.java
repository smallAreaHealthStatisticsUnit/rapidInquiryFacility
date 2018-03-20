package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.ConnectionQueue;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceStartupOptions;

/**
 * Responsible for managing a pool of connections for each registered user.  Connections will
 * be configured to be write only or read only.  We do this for two reasons:
 * <ol>
 * <li><b>security</b>.  Many of the operations will require a read connection.  Therefore, should
 * the connection be used to execute a query that contains malicious code, it will likely fail because
 * many malicious attacks use <i>write</i> operations.
 * </li>
 * <li>
 * <b>efficiency</b>. It is easier to develop database clustering if the kinds of operations for connections
 * are streamlined
 * </li>
 * </ul>
 * <p>
 * Note that this connection manager does not pool anonymised connection objects.  Each of them must be associated
 * with a specific userID
 * </p>
 *
 */

public final class PGSQLConnectionManager extends PGSQLAbstractSQLManager {

	private static final int POOLED_READ_ONLY_CONNECTIONS_PER_PERSON = 10;
	private static final int POOLED_WRITE_CONNECTIONS_PER_PERSON = 5;
	private static final int MAXIMUM_SUSPICIOUS_EVENTS_THRESHOLD = 5;
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static final Messages SERVICE_MESSAGES = Messages.serviceMessages();
	
	/** The rif service startup options. */
	private final RIFServiceStartupOptions rifServiceStartupOptions;

	/** The read connection from user. */
	private final HashMap<String, ConnectionQueue> readOnlyConnectionsFromUser;

	/** The write connection from user. */
	private final HashMap<String, ConnectionQueue> writeConnectionsFromUser;

	/** The initialisation query. */
	private final String initialisationQuery;

	/** The database url. */
	private final String databaseURL;

	private final HashMap<String, Integer> suspiciousEventCounterFromUser;

	private final HashSet<String> registeredUserIDs;
	private final HashSet<String> userIDsToBlock;

	/**
	 * Instantiates a new SQL connection manager.
	 *
	 * @param rifServiceStartupOptions the rif service startup options
	 */
	public PGSQLConnectionManager(
			final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions.getRIFDatabaseProperties());

		this.rifServiceStartupOptions = rifServiceStartupOptions;
		readOnlyConnectionsFromUser = new HashMap<>();
		writeConnectionsFromUser = new HashMap<>();

		userIDsToBlock = new HashSet<>();
		registeredUserIDs = new HashSet<>();

		suspiciousEventCounterFromUser = new HashMap<>();
		
		initialisationQuery = "SELECT rif40_startup(?) AS rif40_init;";

		databaseURL = generateURLText();
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
	 * User exists.
	 *
	 * @param userID the user id
	 * @return true, if successful
	 */
	public boolean userExists(
			final String userID) {

		return registeredUserIDs.contains(userID);
	}

	public boolean isUserBlocked(
			final User user) {
		
		if (user == null) {
			return false;
		}
		
		String userID = user.getUserID();
		return userID != null && userIDsToBlock.contains(userID);
		
	}

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

	public boolean userExceededMaximumSuspiciousEvents(final User user) {
		
		String userID = user.getUserID();
		Integer suspiciousEventCounter
						= suspiciousEventCounterFromUser.get(userID);
		return suspiciousEventCounter != null
		       && suspiciousEventCounter >= MAXIMUM_SUSPICIOUS_EVENTS_THRESHOLD;
		
	}

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
	 * Register user.
	 *
	 * @param userID the user id
	 * @param password the password
	 * @throws RIFServiceException the RIF service exception
	 */
	public void login(final String userID, final String password) throws RIFServiceException {

		if (userIDsToBlock.contains(userID)) {
			return;
		}

		/*
		 * First, check whether person is already logged in.  We can do this 
		 * by checking whether 
		 */

		if (isLoggedIn(userID)) {
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

	public boolean isLoggedIn(
			final String userID) {
		
		return registeredUserIDs.contains(userID);
	}

	/**
	 * Assumes that user is valid.
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
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
					PGSQLConnectionManager.class, 
					errorMessage, 
					exception);
			
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}
		return result;
	}

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

			rifLogger.error(PGSQLConnectionManager.class, errorMessage, exception);
			
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}
	}

	/**
	 * Assumes that user is valid.
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
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
					PGSQLConnectionManager.class, 
					errorMessage, 
					exception);
			
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}

		return result;
	}

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
	private void closeConnectionsForUser(
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
