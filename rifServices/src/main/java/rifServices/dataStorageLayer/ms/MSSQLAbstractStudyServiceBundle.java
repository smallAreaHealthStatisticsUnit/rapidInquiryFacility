package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.common.ServiceBundle;
import rifServices.system.RIFServiceStartupOptions;
import rifGenericLibrary.util.FieldValidationUtility;

class MSSQLAbstractStudyServiceBundle implements ServiceBundle {

	private boolean isInitialised;
	private MSSQLRIFServiceResources rifServiceResources;
	private RIFStudySubmissionAPI rifStudySubmissionService;
	private RIFStudyResultRetrievalAPI rifStudyRetrievalService;

	MSSQLAbstractStudyServiceBundle() {
		isInitialised = false;
	}
		
	public synchronized void initialise(
		final RIFServiceStartupOptions rifServiceStartupOptions) 
		throws RIFServiceException {
		
		if (!isInitialised) {
			
			rifServiceResources
				= MSSQLRIFServiceResources.newInstance(rifServiceStartupOptions);
			setRIFServiceResources(rifServiceResources);

			rifStudySubmissionService.initialise(rifServiceResources);		
			rifStudyRetrievalService.initialise(rifServiceResources);
		
			isInitialised = true;
		}
	}	
	
	private void setRIFServiceResources(
					final MSSQLRIFServiceResources rifServiceResources) {

		this.rifServiceResources = rifServiceResources;
	}
	
	protected MSSQLRIFServiceResources getRIFServiceResources() {
		return rifServiceResources;
	}
	
	@Override
	public RIFStudyResultRetrievalAPI getRIFStudyRetrievalService() {
		return rifStudyRetrievalService;
	}

	void setRIFStudyRetrievalService(
					final RIFStudyResultRetrievalAPI rifStudyRetrievalService) {

		this.rifStudyRetrievalService = rifStudyRetrievalService;
	}
	
	@Override
	public RIFStudySubmissionAPI getRIFStudySubmissionService() {

		return rifStudySubmissionService;
	}
	
	void setRIFStudySubmissionService(
					final RIFStudySubmissionAPI rifStudySubmissionService) {

		this.rifStudySubmissionService = rifStudySubmissionService;
	}
	
	/**
	 * starts the session of a user
	 * @param userID
	 * @param password
	 * @throws RIFServiceException
	 */
	@Override
	public void login(
		final String userID,
		final String password) 
		throws RIFServiceException {

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
			MSSQLConnectionManager sqlConnectionManager
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
			MSSQLConnectionManager sqlConnectionManager
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
	
	/**
	 * ends the current session of the user
	 * @param _user
	 * @throws RIFServiceException
	 */
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
			MSSQLConnectionManager sqlConnectionManager
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

		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		sqlConnectionManager.deregisterAllUsers();
	}
		
	protected void logException(
		final User user,
		final String methodName,
		final RIFServiceException rifServiceException) 
		throws RIFServiceException {
			
		boolean userDeregistered = false;
		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (rifServiceException instanceof RIFServiceSecurityException) {
			//gives opportunity to log security issue and deregister user
			
			RIFServiceSecurityException rifServiceSecurityException
				= (RIFServiceSecurityException) rifServiceException;
			
			if (rifServiceSecurityException.getSecurityThreatType() == RIFServiceSecurityException.SecurityThreatType.MALICIOUS_CODE) {
				//gives opportunity to log security issue and deregister user
				
				sqlConnectionManager.addUserIDToBlock(user);
				sqlConnectionManager.logout(user);
				userDeregistered = true;			
			}
			else {
				//suspiciuous behaviour.  
				//log suspicious event and see whether this particular user is associated with
				//a number of suspicious events that exceeds a threshold.
				sqlConnectionManager.logSuspiciousUserEvent(user);
				if (sqlConnectionManager.userExceededMaximumSuspiciousEvents(user)) {					
					sqlConnectionManager.addUserIDToBlock(user);
					sqlConnectionManager.logout(user);
					userDeregistered = true;			
				}
			}
		}

		if (!userDeregistered) {
			//this helps service recover when one call generates an exception
			//and subsequent calls have one less available connection
			//because the try...catch setup didn't allow connection to
			//be put back in the "unused pile".
			
			//KLG: This may be causing problems so leave it out
			//sqlConnectionManager.resetConnectionPoolsForUser(user);					
		}
					
		RIFLogger rifLogger = RIFLogger.getLogger();
		rifLogger.error(
			MSSQLAbstractStudyServiceBundle.class, 
			methodName, 
			rifServiceException);

		throw rifServiceException;
	}
}
