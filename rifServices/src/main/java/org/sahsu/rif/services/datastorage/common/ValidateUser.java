package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;

/**
 * Checks a user's validity.
 *
 * <p>This code was originally extracted from
 * {@link StudySubmissionService}, in the interests of
 * creating smaller, more testable classes.
 * </p>
 */
public class ValidateUser {
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private User user;
	private SQLManager sqlConnectionManager;
	
	public ValidateUser(User user, SQLManager sqlConnectionManager) {
		
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
