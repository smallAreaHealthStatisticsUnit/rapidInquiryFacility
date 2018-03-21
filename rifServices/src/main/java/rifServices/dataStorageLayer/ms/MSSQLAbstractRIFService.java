package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.RIFServiceInformation;
import rifServices.dataStorageLayer.common.ExceptionLog;
import rifServices.dataStorageLayer.common.ValidateUser;
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
	void validateUser(final User user) throws RIFServiceException {
		
		new ValidateUser(user, rifServiceResources.getSqlConnectionManager()).validate();
	}
	
	public void logException(
		final User user,
		final String methodName,
		final RIFServiceException rifServiceException) 
		throws RIFServiceException {
		
		new ExceptionLog(user, methodName, rifServiceException, rifServiceResources, rifLogger).log();
	}
	
	public RIFServiceInformation getRIFServiceInformation(
		final User _user) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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
	
}
