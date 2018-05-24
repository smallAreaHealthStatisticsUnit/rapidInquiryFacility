package org.sahsu.rif.services.datastorage.common;

import java.util.Objects;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.RIFStudyResultRetrievalAPI;
import org.sahsu.rif.services.concepts.RIFStudySubmissionAPI;

public class StudyServiceBundle implements ServiceBundle {

	private final ServiceResources rifServiceResources;
	private final RIFStudySubmissionAPI rifStudySubmissionService;
	private final RIFStudyResultRetrievalAPI rifStudyRetrievalService;

	protected StudyServiceBundle(final ServiceResources resources,
			final RIFStudySubmissionAPI submission,final RIFStudyResultRetrievalAPI retrieval) {

		Objects.requireNonNull(resources);
		Objects.requireNonNull(submission);
		Objects.requireNonNull(retrieval);

		rifServiceResources = resources;
		rifStudySubmissionService = submission;
		rifStudyRetrievalService = retrieval;
		rifStudySubmissionService.initialise(rifServiceResources);
		rifStudyRetrievalService.initialise(rifServiceResources);
	}

	protected ServiceResources getRIFServiceResources() {
		return rifServiceResources;
	}
	
	@Override
	public RIFStudyResultRetrievalAPI getRIFStudyRetrievalService() {
		return rifStudyRetrievalService;
	}

	@Override
	public RIFStudySubmissionAPI getRIFStudySubmissionService() {

		return rifStudySubmissionService;
	}
	
	@Override
	public void login(final String userID, final String password) throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		//No need -- userID and password are final objects
		
		//Check for empty parameters
		try {
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"login",
				"userID",
				userID);
			fieldValidationUtility.checkNullMethodParameter(
				"login",
				"password",
				password);		
		
			//Check for security violations

			fieldValidationUtility.checkMaliciousMethodParameter(
				"login",
				"userID",
				userID);
			
			fieldValidationUtility.checkMaliciousMethodParameter(
				"login",
				"password",
				password);
			
			//Delegate operation to a specialised manager class
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			sqlConnectionManager.login(userID, password);
		}
		catch(RIFServiceException rifServiceException) {
			User user = User.newInstance(userID, password);
			logException(
				user,
				"login",
				rifServiceException);
		}
		
	}

	@Override
	public boolean isLoggedIn(
			final String userID)
		throws RIFServiceException {
		
		//Check for empty parameters
		boolean result = false;
		try {
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"isLoggedIn",
				"userID",
				userID);
			
			//Delegate operation to a specialised manager class
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			result = sqlConnectionManager.isLoggedIn(userID);			
		}
		catch(RIFServiceException rifServiceException) {
			User user = User.newInstance(userID, null);
			logException(
				user,
				"isLoggedIn",
				rifServiceException);
		}
		
		return result;
	}
	
	@Override
	public void logout(
			final User _user)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);

		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"logout",
				"user",
				user);
		
			//Check for security violations
			user.checkSecurityViolations();
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			sqlConnectionManager.logout(user);		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"logout",
				rifServiceException);
		}
	
	}
	
	protected void deregisterAllUsers() 
		throws RIFServiceException {		

		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		sqlConnectionManager.deregisterAllUsers();
	}
		
	protected void logException(
		final User user,
		final String methodName,
		final RIFServiceException rifServiceException) 
		throws RIFServiceException {

		boolean userDeregistered = false;
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (rifServiceException instanceof RIFServiceSecurityException) {
			//gives opportunity to log security issue and deregister user
			
			RIFServiceSecurityException rifServiceSecurityException
				= (RIFServiceSecurityException) rifServiceException;
			
			if (rifServiceSecurityException.getSecurityThreatType() == RIFServiceSecurityException.SecurityThreatType.MALICIOUS_CODE) {
				//gives opportunity to log security issue and deregister user
				
				sqlConnectionManager.addUserIDToBlock(user);
				sqlConnectionManager.logout(user);
			}
			else {
				//suspicious behaviour.
				//log suspicious event and see whether this particular user is associated with
				//a number of suspicious events that exceeds a threshold.
				sqlConnectionManager.logSuspiciousUserEvent(user);
				if (sqlConnectionManager.userExceededMaximumSuspiciousEvents(user)) {					
					sqlConnectionManager.addUserIDToBlock(user);
					sqlConnectionManager.logout(user);
				}
			}
		}

		RIFLogger rifLogger = RIFLogger.getLogger();
		rifLogger.error(
			getClass(),
			methodName, 
			rifServiceException);

		throw rifServiceException;
	}
}
