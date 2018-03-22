package rifServices.dataStorageLayer.common;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.dataStorageLayer.ms.MSSQLConnectionManager;

/**
 * Checks a user's validity.
 *
 * <p>This code was originally extracted from
 * {@link rifServices.dataStorageLayer.ms.MSSQLRIFStudySubmissionService}, in the interests of
 * creating smaller, more testable classes.
 * </p>
 */
public class ValidateUser {
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private User user;
	private MSSQLConnectionManager sqlConnectionManager;
	
	public ValidateUser(User user, MSSQLConnectionManager sqlConnectionManager) {
		
		this.user = user;
		this.sqlConnectionManager = sqlConnectionManager;
	}
	
	public void validate() throws RIFServiceException {
		
		user.checkSecurityViolations();
		user.checkErrors();
		
		if (!sqlConnectionManager.userExists(user.getUserID())) {
			
			String errorMessage = GENERIC_MESSAGES.getMessage("user.error.invalidUser",
					user.getUserID());
			RIFServiceSecurityException rifServiceSecurityException =
					new RIFServiceSecurityException(errorMessage);
			rifServiceSecurityException.setSecurityThreatType(
					RIFServiceSecurityException.SecurityThreatType.SUSPICIOUS_BEHAVIOUR);
			
			throw rifServiceSecurityException;
		}
	}
}
