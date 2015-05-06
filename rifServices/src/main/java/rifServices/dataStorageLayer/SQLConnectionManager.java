package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.User;


import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.util.RIFLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;


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
 *</ul>
 *<p>
 *Note that this connection manager does not pool anonymised connection objects.  Each of them must be associated
 *with a specific userID
 *</p>
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
 */
/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public final class SQLConnectionManager 
	extends AbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int POOLED_READ_ONLY_CONNECTIONS_PER_PERSON = 10;
	private static final int POOLED_WRITE_CONNECTIONS_PER_PERSON = 5;

	
	private static final int MAXIMUM_SUSPICIOUS_EVENTS_THRESHOLD = 5;
	
	// ==========================================
	// Section Properties
	// ==========================================
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
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL connection manager.
	 *
	 * @param rifServiceStartupOptions the rif service startup options
	 */
	public SQLConnectionManager(
		final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions.getRIFDatabaseProperties());
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		readOnlyConnectionsFromUser = new HashMap<String, ConnectionQueue>();
		writeConnectionsFromUser = new HashMap<String, ConnectionQueue>();
		
		
		userIDsToBlock = new HashSet<String>();
		registeredUserIDs = new HashSet<String>();
	
		suspiciousEventCounterFromUser = new HashMap<String, Integer>();
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append("rif40_startup() AS rif40_init;");
		initialisationQuery = query.toString();
		
		databaseURL = generateURLText();
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

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
		urlText.append("/");
		urlText.append(rifServiceStartupOptions.getDatabaseName());
		
		return urlText.toString();
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
		if (userID == null) {
			return false;
		}
		
		return userIDsToBlock.contains(userID);
	}
	
	public void logSuspiciousUserEvent(
		final User user) {
	
		String userID = user.getUserID();
		
		Integer suspiciousEventCounter
			= suspiciousEventCounterFromUser.get(userID);
		if (suspiciousEventCounter == null) {

			//no incidents recorded yet, this is the first
			suspiciousEventCounterFromUser.put(userID, 1);
		}
		else {
			suspiciousEventCounterFromUser.put(
				userID, 
				(suspiciousEventCounter + 1));
		}		
	}
	
	public boolean userExceededMaximumSuspiciousEvents(
		final User user) {
		
		String userID = user.getUserID();
		Integer suspiciousEventCounter
			= suspiciousEventCounterFromUser.get(userID);
		if (suspiciousEventCounter == null) {
			return false;
		}
		
		if (suspiciousEventCounter < MAXIMUM_SUSPICIOUS_EVENTS_THRESHOLD) {
			return false;
		}
		
		return true;
		
	}
	
	public void addUserIDToBlock(
		final User user) 
		throws RIFServiceException {
			
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
		
		if (isLoggedIn(userID)) {
			return;
		}

		Connection currentConnection = null;
		PreparedStatement statement = null;
		try {
			Class.forName(rifServiceStartupOptions.getDatabaseDriverClassName());

			//Establish read-only connections
			ConnectionQueue readOnlyConnectionQueue = new ConnectionQueue();
			for (int i = 0; i < POOLED_READ_ONLY_CONNECTIONS_PER_PERSON; i++) {
				currentConnection 
					= DriverManager.getConnection(
						databaseURL,
						userID,
						password);
				statement
					= SQLQueryUtility.createPreparedStatement(
						currentConnection, 
						initialisationQuery);
				statement.execute();
				statement.close();
				currentConnection.setReadOnly(false);
				currentConnection.setAutoCommit(false);
				readOnlyConnectionQueue.addConnection(currentConnection);
			}
			readOnlyConnectionsFromUser.put(userID, readOnlyConnectionQueue);
			
			//Establish write-only connections
			ConnectionQueue writeOnlyConnectionQueue = new ConnectionQueue();
			for (int i = 0; i < POOLED_WRITE_CONNECTIONS_PER_PERSON; i++) {
				currentConnection 
					= DriverManager.getConnection(
						databaseURL,
						userID,
						password);
				statement
					= SQLQueryUtility.createPreparedStatement(
						currentConnection, 
						initialisationQuery);
				statement.execute();
				statement.close();
				currentConnection.setAutoCommit(false);
				writeOnlyConnectionQueue.addConnection(currentConnection);
			}			
			writeConnectionsFromUser.put(userID, writeOnlyConnectionQueue);
			
			registeredUserIDs.add(userID);
			
		}
		catch(ClassNotFoundException classNotFoundException) {
			classNotFoundException.printStackTrace(System.out);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToLoadDatabaseDriver",
					userID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_TO_LOAD_DRIVER,
					errorMessage);
			throw rifServiceException;			
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToRegisterUser",
					userID);
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
					SQLConnectionManager.class, 
				errorMessage, 
				sqlException);
									
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_REGISTER_USER,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
	}
	
	public boolean isLoggedIn(
		final String userID) {

		if (registeredUserIDs.contains(userID)) {
			return true;
		}

		return false;
				
	}
		
	/**
	 * Assumes that user is valid.
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	public Connection assignPooledReadConnection(
		final User user) 
		throws RIFServiceException {
		
		Connection result = null;

		String userID = user.getUserID();
		if (userIDsToBlock.contains(userID)) {
			return result;
		}
		
		ConnectionQueue availableReadConnectionQueue
			= readOnlyConnectionsFromUser.get(user.getUserID());

		try {
			Connection connection = availableReadConnectionQueue.assignConnection();
			//turn AUTOCOMMMIT OFF		
			connection.setAutoCommit(false);
			result = connection;
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.db.error.unableToSetCommit");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLConnectionManager.class, 
				errorMessage, 
				exception);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		return result;
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
					"sqlRIFSubmissionManager.error.unableToControlAutocommitSetting");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLConnectionManager.class, 
				errorMessage, 
				exception);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
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
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToControlAutocommitSetting");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLConnectionManager.class, 
				errorMessage, 
				exception);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
	

	}
	
	/**
	 * Assumes that user is valid.  This method used a connection object that
	 * has been configured for write operations
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */

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
		
		Connection result = null;
		try {
			
			String userID = user.getUserID();
			if (userIDsToBlock.contains(userID)) {
				return result;
			}
			
			ConnectionQueue writeConnectionQueue
				= writeConnectionsFromUser.get(user.getUserID());
			Connection connection
				= writeConnectionQueue.assignConnection();
			result = connection;			
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.db.error.unableToSetCommit");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLConnectionManager.class, 
				errorMessage, 
				exception);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}

		return result;
	}

	public void logout(
		final User user) 
		throws RIFServiceException {
		
		if (user == null) {
			return;
		}
		
		String userID = user.getUserID();
		if (userID == null) {
			return;
		}
		
		if (registeredUserIDs.contains(userID) == false) {
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
	 * @param user the user
	 * @throws RIFServiceException the RIF service exception
	 */
	public void closeConnectionsForUser(
		final String userID) 
		throws RIFServiceException {
				
		try {
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
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToDeregisterUser",
					userID);
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
					SQLConnectionManager.class, 
				errorMessage, 
				sqlException);
			
			RIFServiceException serviceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_DEREGISTER_USER,
					errorMessage);
			throw serviceException;			
		}
		
		//We ignore the finally clause because it is essentially doing
		//the close action of the connection
	}

	public void resetConnectionPoolsForUser(final User user)
		throws RIFServiceException {
		
		if (user == null) {
			return;
		}

		String userID = user.getUserID();
		if (userID == null) {
			return;
		}
		
		try {
			
			//adding all the used read connections back to the available
			//connections pool
			ConnectionQueue readOnlyConnectionQueue
				= readOnlyConnectionsFromUser.get(userID);	
			if (readOnlyConnectionQueue != null) {
				readOnlyConnectionQueue.closeAllConnections();
				readOnlyConnectionQueue.clearConnections();
			}
		
			//adding all the used write connections back to the available
			//connections pool
			ConnectionQueue writeConnectionQueue
				= writeConnectionsFromUser.get(userID);
			if (writeConnectionQueue != null) {
				writeConnectionQueue.closeAllConnections();
				writeConnectionQueue.clearConnections();
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToResetConnectionPools",
					user.getUserID());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_TO_RESET_CONNECTION_POOL, 
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
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
