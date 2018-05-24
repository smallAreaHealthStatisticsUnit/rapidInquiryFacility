package org.sahsu.rif.dataloader.datastorage.ms;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.ConnectionQueue;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;

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
 * Copyright 2017 Imperial College London, developed by the Small Area
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

final class MSSQLConnectionManager {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	private static final int MAXIMUM_DATA_LOADER_CONNECTIONS = 5;
	
	// ==========================================
	// Section Properties
	// ==========================================
		
	/** The write connection from user. */
	private ConnectionQueue writeConnections;
		
	/** The database url. */
	private final String databasePasswordFilePath;
	private final String databaseURL;
	private final String databaseDriverClassName;
	
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
	public MSSQLConnectionManager(
		final String databasePasswordFilePath,
		final String databaseDriverClassName,
		final String databaseURL) {
			
		registeredUserIDs = new HashSet<String>();
		userIDsToBlock = new HashSet<String>();
		writeConnections = new ConnectionQueue();

		this.databasePasswordFilePath = databasePasswordFilePath;
		this.databaseDriverClassName = databaseDriverClassName;
		this.databaseURL = databaseURL;		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void initialiseConnectionQueue()
		throws RIFServiceException {
		
		try {		
			Class.forName(databaseDriverClassName);
			
			//File userLoginDetailsFile = new File("C://rif_scripts//db//RIFDatabaseProperties.txt");
			File userLoginDetailsFile = new File(databasePasswordFilePath);

			FileReader fileReader = new FileReader(userLoginDetailsFile);
			PropertyResourceBundle userLoginResourceBundle
				= new PropertyResourceBundle(fileReader);
			
			String userID = (String) userLoginResourceBundle.getObject("userID");
			String password = (String) userLoginResourceBundle.getObject("password");

			//Establish read-only connections
			for (int i = 0; i < MAXIMUM_DATA_LOADER_CONNECTIONS; i++) {
				Connection currentConnection
					= createConnection(
						userID,
						password,
						true);
				writeConnections.addConnection(currentConnection);
			}
		}
		catch(Exception sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"sqlConnectionManager.error.unableToCreateConnections");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
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

	/*
	 * At some point in the future, if the Data Loader is delivered via web application,
	 * we may need a method to check if a user has been blocked because of suspicious
	 * behaviour.  For now, assume that users are never blocked until we get more time
	 * to deal with this issue.  It is unlikely it will present much of an issue
	 * because initially, we expect that the Data Loader Tool will be used by a single
	 * user who operates within a very secure network.
	 */
	public boolean isUserBlocked(
		final User user) {
		
		return false;
	}
	
	/**
	 * Register user. Most of this has been commented out because for now
	 * we are not looking at doing user sessions - when they use the data
	 * loader tool, they operate in an administrative mode and their use
	 * of connections does not time out.
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

		/*

		if (userIDsToBlock.contains(userID)) {
			return;
		}
		
		if (isLoggedIn(userID)) {
			return;
		}

		try {
			Class.forName(databaseDriverClassName);
			
			//Establish write-only connections
			for (int i = 0; i < POOLED_WRITE_CONNECTIONS_PER_PERSON; i++) {
				Connection currentConnection
					= createConnection(
						userID,
						password);
				writeConnections.addConnection(currentConnection);
				
				private Connection createConnection(
						final String userID,
						final String password)
				
			}			

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
			writeConnections.closeAllConnections();				
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

		*/
	}
	
	public boolean isLoggedIn(
		final String userID) {

		if (registeredUserIDs.contains(userID)) {
			return true;
		}

		return false;
				
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
			writeConnections.reclaimConnection(connection);			
		}	
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"sqlConnectionManager.error.unableToReclaimWriteConnection");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				MSSQLConnectionManager.class, 
				errorMessage, 
				exception);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
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
			
			Connection connection
				= writeConnections.assignConnection();
			result = connection;			
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"sqlConnectionManager.error.unableToAssignWriteConnection");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				MSSQLConnectionManager.class, 
				errorMessage, 
				exception);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
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

		writeConnections.closeAllConnections();		
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
		
		//adding all the used write connections back to the available
		//connections pool
		writeConnections.closeAllConnections();
		writeConnections.clearConnections();
	}
	
	public void deregisterAllUsers() throws RIFServiceException {
		for (String registeredUserID : registeredUserIDs) {
			closeConnectionsForUser(registeredUserID);
		}
		
		registeredUserIDs.clear();
	}
	

	
	public Connection createDatabaseConnection(
		final String databaseCreationURL,
		final String userID,
		final String password,
		final boolean isAutoCommitOn)
		throws SQLException,
		RIFServiceException {
		
		Connection connection = null;
		try {
			
			Properties databaseProperties = new Properties();
			
			if (userID != null) {
				databaseProperties.setProperty("user", userID);
				databaseProperties.setProperty("password", password);
			}
			

			/*
			 * #POSSIBLE_PORTING_ISSUE
			 * There may be some issues with setting some of the properties
			 * of the database connections.  
			 */
			
			//databaseProperties.setProperty("ssl", "true");
			//databaseProperties.setProperty("logUnclosedConnections", "true");
			databaseProperties.setProperty("prepareThreshold", "3");
			//KLG: @TODO this introduces a porting issue
			//int logLevel = org.postgresql.Driver.DEBUG;
			//databaseProperties.setProperty("loglevel", String.valueOf(logLevel));
			
			connection
				= DriverManager.getConnection(databaseURL, databaseProperties);
			connection.setReadOnly(false);				
			connection.setAutoCommit(true);
		}
		finally {

		}

		return connection;

	}
		
	
	
	public Connection createConnection(
		final String userID,
		final String password,
		final boolean isAutoCommitOn)
		throws SQLException,
		RIFServiceException {
		
		Connection connection = null;
		try {
			
			Properties databaseProperties = new Properties();
			
			if (userID != null) {
				System.out.println("createConnection 222 userID ==" + userID + "==");
				databaseProperties.setProperty("user", userID);
				databaseProperties.setProperty("password", password);
			}
			//databaseProperties.setProperty("ssl", "true");
			//databaseProperties.setProperty("logUnclosedConnections", "true");
			//databaseProperties.setProperty("prepareThreshold", "3");
			//KLG: @TODO this introduces a porting issue
			//int logLevel = org.postgresql.Driver.DEBUG;
			//databaseProperties.setProperty("loglevel", String.valueOf(logLevel));
			System.out.println("MSSQLConnectionManager databaseURL=="+ databaseURL + "==");

			connection
				= DriverManager.getConnection(databaseURL, databaseProperties);
			connection.setReadOnly(false);
			connection.setAutoCommit(true);
		}
		finally {

		}

		return connection;

	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private void logException(RIFServiceException rifServiceException) {
		
	}
	
	private void logException(Exception exception) {
		
	}
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
