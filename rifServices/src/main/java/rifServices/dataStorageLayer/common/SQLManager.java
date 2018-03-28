package rifServices.dataStorageLayer.common;

import java.sql.Connection;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;

public interface SQLManager {
	
	ValidationPolicy getValidationPolicy();
	
	void setValidationPolicy(ValidationPolicy validationPolicy);
	
	void setEnableLogging(boolean enableLogging);
	
	String getUserPassword(final User user);
	
	boolean userExists(final String userID);
	
	boolean isUserBlocked(final User user);
	
	void logSuspiciousUserEvent(final User user);
	
	boolean userExceededMaximumSuspiciousEvents(final User user);
	
	void addUserIDToBlock(final User user);
	
	void login(final String userID, final String password) throws RIFServiceException;
	
	boolean isLoggedIn(final String userID);
	
	Connection assignPooledReadConnection(final User user) throws RIFServiceException;
	
	void reclaimPooledReadConnection(final User user, final Connection connection)
			throws RIFServiceException;
	
	void reclaimPooledWriteConnection(final User user, final Connection connection)
			throws RIFServiceException;
	
	Connection assignPooledWriteConnection(final User user) throws RIFServiceException;
	
	void logout(final User user) throws RIFServiceException;
	
	void deregisterAllUsers() throws RIFServiceException;
	
}
