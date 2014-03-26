package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.User;

import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

public class SQLConnectionManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The rif service startup options. */
	private final RIFServiceStartupOptions rifServiceStartupOptions;
	
	/** The connection from user. */
	private final HashMap<String, Connection> connectionFromUser;
	
	/** The initialisation query. */
	private final String initialisationQuery;
	
	/** The database url. */
	private final String databaseURL;
	
	private HashSet<String> userIDsToBlock;
	
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

		this.rifServiceStartupOptions = rifServiceStartupOptions;
		connectionFromUser = new HashMap<String, Connection>();
		
		userIDsToBlock = new HashSet<String>();
		
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
		urlText.append(rifServiceStartupOptions.getDatabaseDriver());
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

		Connection connection = connectionFromUser.get(userID);
		if (connection != null) {
			return true;
		}
		return false;
	}
	
	public void addUserIDToBlock(
		final String userID) 
		throws RIFServiceException {
			
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
	public Connection registerUser(
		final String userID,
		final String password) 
		throws RIFServiceException {
	
		if (userIDsToBlock.contains(userID)) {
			return null;
		}
		
		Connection connection = connectionFromUser.get(userID);
		if (connection != null) {
			return connection;
		}
		PreparedStatement statement = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection 
				= DriverManager.getConnection(
					databaseURL,
					userID,
					password);
			statement
				= connection.prepareStatement(initialisationQuery);
			statement.execute();
			statement.close();
			connectionFromUser.put(userID, connection);
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
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToRegisterUser",
					userID);

			Logger logger 
				= LoggerFactory.getLogger(SQLConnectionManager.class);
			logger.error(errorMessage, sqlException);				
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_REGISTER_USER,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		return connection;
	}
	
	/**
	 * Assumes that user is valid.
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	public Connection getConnection(
		final User user) 
		throws RIFServiceException {
		
		String userID = user.getUserID();
		if (userIDsToBlock.contains(userID)) {
			return null;
		}

		Connection connection 
			= connectionFromUser.get(userID);
		return connection;			
	}
	
	
	/**
	 * Deregister user.
	 *
	 * @param user the user
	 * @throws RIFServiceException the RIF service exception
	 */
	public void deregisterUser(
		final User user) 
		throws RIFServiceException {

		Connection connection = null;
		try {
			connection 
				= connectionFromUser.get(user.getUserID());
			if (connection != null) {
				connectionFromUser.remove(user);
				connection.close();				
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToDeregisterUser",
					user.getUserID());
			
			Logger logger 
				= LoggerFactory.getLogger(SQLConnectionManager.class);
			logger.error(errorMessage, sqlException);				
		
			
			RIFServiceException serviceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_DEREGISTER_USER,
					errorMessage);
			throw serviceException;			
		}
		
		//We ignore the finally clause because it is essentially doing
		//the close action of the connection
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
