package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.system.RIFServiceSecurityException.SecurityThreatType;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.RIFServiceInformation;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

/**
 * The base class of all services in the RIF tool suite.
  *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

public class MSSQLAbstractRIFService {
	
	protected static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private double serviceVersion;
	private String serviceName;
	private String serviceDescription;
	private String serviceContactEmail;
	
	protected MSSQLRIFServiceResources rifServiceResources;
	private boolean isInitialised;
		
	public MSSQLAbstractRIFService() {
		isInitialised = false;

	}

	public void initialise(final Object startupParameter) {

		this.rifServiceResources 
			= (MSSQLRIFServiceResources) startupParameter;
		isInitialised = true;
	}
	
	protected double getServiceVersion() {
		return serviceVersion;
	}

	protected void setServiceVersion(final double serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	protected String getServiceName() {
		return serviceName;
	}

	protected void setServiceName(final String serviceName) {
		this.serviceName = serviceName;
	}

	protected String getServiceDescription() {
		return serviceDescription;
	}

	protected void setServiceDescription(final String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	protected String getServiceContactEmail() {
		return serviceContactEmail;
	}

	protected void setServiceContactEmail(final String serviceContactEmail) {
		this.serviceContactEmail = serviceContactEmail;
	}

	/**
	 * Validate user.
	 *
	 * @param user the user
	 * @throws RIFServiceException the RIF service exception
	 */
	public void validateUser(
		final User user) 
		throws RIFServiceException {
				
		user.checkSecurityViolations();
		user.checkErrors();

		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.userExists(user.getUserID())) {
			//user exists so valid
			//KLG: expand this feature later to handle things like
			//time-outs
			return;
		}
		
		//invalid user attempting to use service
		String errorMessage
			= GENERIC_MESSAGES.getMessage(
				"user.error.invalidUser",
				user.getUserID());
		RIFServiceSecurityException rifServiceSecurityException
			 = new RIFServiceSecurityException(errorMessage);
		rifServiceSecurityException.setSecurityThreatType(SecurityThreatType.SUSPICIOUS_BEHAVIOUR);
		
		throw rifServiceSecurityException;
	}

	public void logException(
		final User user,
		final String methodName,
		final RIFServiceException rifServiceException) 
		throws RIFServiceException {
		
		boolean userDeregistered = false;
		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (rifServiceException instanceof RIFServiceSecurityException) {
			//TOUR_SECURITY
			/*
			 * If the code encounters a security exception, then the user
			 * associated with the exception will be blacklisted.
			 */
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

		if (userDeregistered == false) {
			//this helps service recover when one call generates an exception
			//and subsequent calls have one less available connection
			//because the try...catch setup didn't allow connection to
			//be put back in the "unused pile".
			
			//KLG: This may be causing problems.  Let connections be assigned and reclaimed
			//through explicit sqlConnectionManager calls
			//sqlConnectionManager.resetConnectionPoolsForUser(user);					
		}
				
		rifLogger.error(
			MSSQLAbstractRIFService.class, 
			methodName, 
			rifServiceException);
	
		throw rifServiceException;
	}

	public RIFServiceInformation getRIFServiceInformation(
		final User _user) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
			
		RIFServiceInformation result
			= RIFServiceInformation.newInstance();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getRIFServiceInformation",
				"user",
				user);
		
			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getRIFSubmissionServiceInformation",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
							
			//Delegate operation to a specialised manager class
			result.setServiceName(serviceName);
			result.setServiceDescription(serviceDescription);
			result.setContactEmail(serviceContactEmail);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getRIFServiceInformation",
				rifServiceException);
		}
		
		return result;
	}
	
	public RIFServiceStartupOptions getRIFServiceStartupOptions() {
		return rifServiceResources.getRIFServiceStartupOptions();
	}
	
	
	public boolean isInitialised() {
		return isInitialised;
	}
	
	public void test() 
		throws RIFServiceException {
		
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
