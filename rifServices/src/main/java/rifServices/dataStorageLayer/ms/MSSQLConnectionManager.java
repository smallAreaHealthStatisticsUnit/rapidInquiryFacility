package rifServices.dataStorageLayer.ms;

import java.sql.Connection;
import java.util.HashMap;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.ConnectionQueue;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.common.AbstractSQLManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

public class MSSQLConnectionManager extends AbstractSQLManager {
	
	private static final int MAXIMUM_SUSPICIOUS_EVENTS_THRESHOLD = 5;
	
	private final HashMap<String, Integer> suspiciousEventCounterFromUser;
	
	/**
	 * Instantiates a new SQL connection manager.
	 *
	 * @param rifServiceStartupOptions the rif service startup options
	 */
	public MSSQLConnectionManager(
		final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions);
		
		suspiciousEventCounterFromUser = new HashMap<>();
		
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
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
			result = connection;
		}
		catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToAssignReadConnection");

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
		return result;
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
					"sqlConnectionManager.error.unableToReclaimWriteConnection");

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
	
	/**
	 * Assumes that user is valid.  This method used a connection object that
	 * has been configured for write operations
	 *
	 * @param user the user
	 * @return the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	
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

	public void resetConnectionPoolsForUser(final User user)
		throws RIFServiceException {
		
		if (user == null) {
			return;
		}

		String userID = user.getUserID();
		if (userID == null) {
			return;
		}
		
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
