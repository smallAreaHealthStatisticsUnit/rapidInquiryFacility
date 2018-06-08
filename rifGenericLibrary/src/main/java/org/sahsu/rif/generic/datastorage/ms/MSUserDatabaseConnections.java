package org.sahsu.rif.generic.datastorage.ms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.util.RIFLogger;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

public final class MSUserDatabaseConnections {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int READ_CONNECTIONS_PER_PERSON = 5;
	private static final int WRITE_CONNECTIONS_PER_PERSON = 5;
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");	
	private static String callingClassName="rifGenericLibrary.datastorage.ms.MSUserDatabaseConnections";
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	private String userID;
	
	private Object readOnlyConnectionsLock;
	private Set<Connection> availableReadOnlyConnections;
	private Set<Connection> usedReadOnlyConnections;
	
	private Object writeOnlyConnectionsLock;
	private Set<Connection> availableWriteOnlyConnections;
	private Set<Connection> usedWriteOnlyConnections;
	
	/** The initialisation query. */
	private final String initialisationQuery;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private MSUserDatabaseConnections(
		final String userID) {
		
		this.userID = userID;
		
		StringBuilder query = new StringBuilder();
		query.append("EXEC rif40.rif40_startup 1"); //TODO: (DM) replaced PG code (Never used??)
		initialisationQuery = query.toString();
		
		readOnlyConnectionsLock = new Object();
		writeOnlyConnectionsLock = new Object();		
	}

	/**
	 * This method is used to ensure safe construction of a pre-set collection of database 
	 * connections that are associated with a user
	 * @param userID
	 * @param password
	 * @param databaseDriverClassName
	 * @param initialisationQuery
	 * @param readOnlyDatabaseConnectionString
	 * @param writeOnlyDatabaseConnectionString
	 * @return
	 * @throws RIFServiceException
	 */
	public static MSUserDatabaseConnections newInstance(
		final String userID,
		final char[] password,
		final String databaseDriverClassName,
		final String initialisationQuery,
		final String readOnlyDatabaseConnectionString,
		final String writeOnlyDatabaseConnectionString) 
		throws RIFServiceException {
		
		MSUserDatabaseConnections userDatabaseConnections
			= new MSUserDatabaseConnections(userID);
		
		/*
		 * Use thread-safe sets
		 */
		Set<Connection> availableReadOnlyConnections
			= Collections.synchronizedSet(new HashSet<Connection>());
		Set<Connection> usedReadOnlyConnections
			= Collections.synchronizedSet(new HashSet<Connection>());
		
		Set<Connection> availableWriteOnlyConnections
			= Collections.synchronizedSet(new HashSet<Connection>());
		Set<Connection> usedWriteOnlyConnections
			= Collections.synchronizedSet(new HashSet<Connection>());
		
		//Establish read-only connections
		PreparedStatement statement = null;
		try {
			
			Class.forName(databaseDriverClassName);

			for (int i = 0; i < READ_CONNECTIONS_PER_PERSON; i++) {
				Connection currentConnection 
					= DriverManager.getConnection(
						readOnlyDatabaseConnectionString,
						userID,
						new String(password));
				statement
					= SQLQueryUtility.createPreparedStatement(
						currentConnection, 
						initialisationQuery);
				statement.execute();
				statement.close();
				currentConnection.setReadOnly(true);
				currentConnection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
				currentConnection.setAutoCommit(false);
				availableReadOnlyConnections.add(currentConnection);
			}
			
			for (int i = 0; i < WRITE_CONNECTIONS_PER_PERSON; i++) {
				Connection currentConnection 
					= DriverManager.getConnection(
						writeOnlyDatabaseConnectionString,
						userID,
						new String(password));
				statement = SQLQueryUtility.createPreparedStatement(
						currentConnection, 
						initialisationQuery);
				statement.execute();
				statement.close();
				currentConnection.setReadOnly(false);
				currentConnection.setAutoCommit(false);
				currentConnection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				availableWriteOnlyConnections.add(currentConnection);
			}
			
			userDatabaseConnections.setAvailableReadOnlyConnections(availableReadOnlyConnections);
			userDatabaseConnections.setUsedReadOnlyConnections(usedReadOnlyConnections);
			userDatabaseConnections.setAvailableWriteOnlyConnections(availableWriteOnlyConnections);
			userDatabaseConnections.setUsedWriteOnlyConnections(usedWriteOnlyConnections);
			
			return userDatabaseConnections;
		}
		catch(ClassNotFoundException classNotFoundException) {
			rifLogger.error(callingClassName, "Jdb.error.unableToLoadDatabaseDriver ERROR", 
				classNotFoundException);
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"db.error.unableToLoadDatabaseDriver");
			throw new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_TO_LOAD_DRIVER,
				errorMessage);
		}
		catch(SQLException sqlException) {
			rifLogger.error(callingClassName, "Jdb.error.unableToLoadDatabaseDriver ERROR", 
				sqlException);
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"db.error.unableToRegisterUser",
					userID);
			
			rifLogger.error(
					MSUserDatabaseConnections.class, 
				errorMessage, 
				sqlException);
									
			RIFServiceException rifServiceException
				= new RIFServiceException(
						RIFGenericLibraryError.DB_UNABLE_REGISTER_USER,
					errorMessage);
			throw rifServiceException;		
		}
		finally {
			SQLQueryUtility.close(statement);
		}
				
	}
	
	public void shutdownReadOnlyConnections() 
		throws RIFServiceException{

		synchronized(readOnlyConnectionsLock) {
			
			Iterator<Connection> iterator = availableReadOnlyConnections.iterator();
			while (iterator.hasNext()) {
				Connection currentConnection
					= iterator.next();
				SQLQueryUtility.close(currentConnection);
			}
			
			iterator = usedReadOnlyConnections.iterator();
			while (iterator.hasNext()) {
				Connection currentConnection
					= iterator.next();
				SQLQueryUtility.close(currentConnection);
			}			
		}
	}

	public void shutdownWriteOnlyConnections() 
		throws RIFServiceException{

		synchronized(writeOnlyConnectionsLock) {
				
			Iterator<Connection> iterator = availableWriteOnlyConnections.iterator();
			while (iterator.hasNext()) {
				Connection currentConnection
					= iterator.next();
				SQLQueryUtility.close(currentConnection);
			}

			iterator = usedWriteOnlyConnections.iterator();
			while (iterator.hasNext()) {
				Connection currentConnection
					= iterator.next();
				SQLQueryUtility.close(currentConnection);
			}			
		}
	}
		
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Assumes that user is valid.
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	public Connection assignPooledReadConnection(
		final String userID) 
		throws RIFServiceException {
	
		//Note: availableReadOnlyConnections is already a set with 
		//thread-safe access.  However, we need to ensure that removing an item
		//from available and putting it into used is done in one atomic step
		
		synchronized(readOnlyConnectionsLock) {
			if (availableReadOnlyConnections.isEmpty()) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"db.error.maximumReadConnectionsExceeded",
						userID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
							RIFGenericLibraryError.DB_MAX_READ_CONNECTIONS_EXCEEDED, 
						errorMessage);
				throw rifServiceException;
			}
			else {
				Iterator<Connection> iterator = availableReadOnlyConnections.iterator();
				Connection connection = iterator.next();			
				availableReadOnlyConnections.remove(connection);			
				usedReadOnlyConnections.add(connection);
				return connection;
			}		
		}

	}
	
	public void reclaimPooledReadConnection(
		final User user, 
		final Connection connection) 
		throws RIFServiceException {
		
		synchronized(readOnlyConnectionsLock) {
			availableReadOnlyConnections.add(connection);
			usedReadOnlyConnections.remove(connection);
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
		final String userID) 
		throws RIFServiceException {
	
		//Note: availableWriteOnlyConnections is already a set with 
		//thread-safe access.  However, we need to ensure that removing an item
		//from available and putting it into used is done in one atomic step
		
		synchronized(writeOnlyConnectionsLock) {
			if (availableWriteOnlyConnections.isEmpty()) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"db.error.maximumReadConnectionsExceeded",
						userID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFGenericLibraryError.DB_MAX_WRITE_CONNECTIONS_EXCEEDED, 
						errorMessage);
				throw rifServiceException;
			}
			else {
				Iterator<Connection> iterator = availableWriteOnlyConnections.iterator();
				Connection connection = iterator.next();			
				availableWriteOnlyConnections.remove(connection);			
				usedWriteOnlyConnections.add(connection);
				return connection;
			}		
		}

	}
	
	public void reclaimPooledWriteConnection(
		final User user, 
		final Connection connection) 
		throws RIFServiceException {
			
		synchronized(readOnlyConnectionsLock) {
			availableWriteOnlyConnections.add(connection);
			usedWriteOnlyConnections.remove(connection);
		}
	}
	
	private void setAvailableReadOnlyConnections(
		final Set<Connection> availableReadOnlyConnections) {
		
		this.availableReadOnlyConnections = availableReadOnlyConnections;
	}
	
	private void setUsedReadOnlyConnections(
		final Set<Connection> usedReadOnlyConnections) {
			
		this.usedReadOnlyConnections = usedReadOnlyConnections;
	}
	
	private void setAvailableWriteOnlyConnections(
		final Set<Connection> availableWriteOnlyConnections) {
			
		this.availableWriteOnlyConnections = availableWriteOnlyConnections;
	}
		
	private void setUsedWriteOnlyConnections(
		final Set<Connection> usedWriteOnlyConnections) {
				
		this.usedWriteOnlyConnections = usedWriteOnlyConnections;
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
