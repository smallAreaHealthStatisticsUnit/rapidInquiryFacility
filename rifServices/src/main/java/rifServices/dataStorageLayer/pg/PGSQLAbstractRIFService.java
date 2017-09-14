package rifServices.dataStorageLayer.pg;

import rifServices.businessConceptLayer.RIFServiceInformation;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.system.RIFServiceSecurityException.SecurityThreatType;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.util.FieldValidationUtility;


/**
 * The base class of all services in the RIF tool suite.
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

public class PGSQLAbstractRIFService {

	// ==========================================
	// Section Constants
	// ==========================================
	protected static final RIFLogger rifLogger = RIFLogger.getLogger();

	// ==========================================
	// Section Properties
	// ==========================================
	
	private double serviceVersion;
	private String serviceName;
	private String serviceDescription;
	private String serviceContactEmail;
	
	protected PGSQLRIFServiceResources rifServiceResources;
	private boolean isInitialised;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLAbstractRIFService() {
		isInitialised = false;

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

		
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	public void initialise(final Object startupParameter)
		throws RIFServiceException {

		this.rifServiceResources 
			= (PGSQLRIFServiceResources) startupParameter;
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

		PGSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.userExists(user.getUserID())) {
			//user exists so valid
			//KLG: expand this feature later to handle things like
			//time-outs
			return;
		}
		
		//invalid user attempting to use service
		String errorMessage
			= RIFGenericLibraryMessages.getMessage(
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
		PGSQLConnectionManager sqlConnectionManager
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
			PGSQLAbstractRIFService.class, 
			methodName, 
			rifServiceException);
	
		throw rifServiceException;
	}

	public RIFServiceInformation getRIFServiceInformation(
		final User _user) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		PGSQLConnectionManager sqlConnectionManager
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
