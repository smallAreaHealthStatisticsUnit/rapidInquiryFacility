package rifServices.dataStorageLayer;


import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.util.FieldValidationUtility;
import rifServices.util.RIFLogger;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
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

class AbstractStudyServiceBundle {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private boolean isInitialised;
	private RIFServiceResources rifServiceResources;
	private RIFStudySubmissionAPI rifStudySubmissionService;
	private RIFStudyResultRetrievalAPI rifStudyRetrievalService;

	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractStudyServiceBundle() {
		isInitialised = false;
	}
		
	public synchronized void initialise(
		final RIFServiceStartupOptions rifServiceStartupOptions) 
		throws RIFServiceException {
		
		if (isInitialised == false) {
			
			rifServiceResources
				= RIFServiceResources.newInstance(rifServiceStartupOptions);
			setRIFServiceResources(rifServiceResources);

			rifStudySubmissionService.initialise(rifServiceResources);		
			rifStudyRetrievalService.initialise(rifServiceResources);
		
			isInitialised = true;
		}
	}	
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	protected void setRIFServiceResources(
		final RIFServiceResources rifServiceResources) {

		this.rifServiceResources = rifServiceResources;
	}
	
	
	
	public RIFStudyResultRetrievalAPI getRIFStudyRetrievalService() {
		return rifStudyRetrievalService;
	}

	protected void setRIFStudyRetrievalService(
		final RIFStudyResultRetrievalAPI rifStudyRetrievalService) {

		this.rifStudyRetrievalService = rifStudyRetrievalService;
	}
	
	public RIFStudySubmissionAPI getRIFStudySubmissionService() {

		return rifStudySubmissionService;
	}
	
	protected void setRIFStudySubmissionService(
		final RIFStudySubmissionAPI rifStudySubmissionService) {

		this.rifStudySubmissionService = rifStudySubmissionService;
	}
	
	/**
	 * starts the session of a user
	 * @param userID
	 * @param password
	 * @throws RIFServiceException
	 */
	public void login(
		final String userID,
		final String password) 
		throws RIFServiceException {

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
			SQLConnectionManager sqlConnectionManager
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
	
	/**
	 * ends the current session of the user
	 * @param _user
	 * @throws RIFServiceException
	 */
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
			SQLConnectionManager sqlConnectionManager
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

		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		sqlConnectionManager.deregisterAllUsers();
	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	protected void logException(
		final User user,
		final String methodName,
		final RIFServiceException rifServiceException) 
		throws RIFServiceException {
			
		boolean userDeregistered = false;
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (rifServiceException instanceof RIFServiceSecurityException) {
			//gives opportunity to log security issue and deregister user
			
			RIFServiceSecurityException rifServiceSecurityException
				= (RIFServiceSecurityException) rifServiceException;
			
			if (rifServiceSecurityException.getSecurityThreatType() == RIFServiceSecurityException.SecurityThreatType.MALICIOUS_CODE) {
				//gives opportunity to log security issue and deregister user
				
				System.out.println("AbstractRIFService logException encountered a MALICIOUS CODE threat");
				sqlConnectionManager.addUserIDToBlock(user);
				sqlConnectionManager.logout(user);
				userDeregistered = true;			
			}
			else {
				System.out.println("AbstractRIFService logException encountered a SUSPICIOUS BEHAVIOUR threat");
				//suspiciuous behaviour.  
				//log suspicious event and see whether this particular user is associated with
				//a number of suspicious events that exceeds a threshold.
				sqlConnectionManager.logSuspiciousUserEvent(user);
				if (sqlConnectionManager.userExceededMaximumSuspiciousEvents(user)) {
					System.out.println("AbstractRIFService logException encountered a SUSPICIOUS BEHAVIOUR threat -- max times exceeded");
					
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
			sqlConnectionManager.resetConnectionPoolsForUser(user);					
		}
					
		RIFLogger rifLogger = RIFLogger.getLogger();
		rifLogger.error(
			AbstractStudyServiceBundle.class, 
			methodName, 
			rifServiceException);

		throw rifServiceException;
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
