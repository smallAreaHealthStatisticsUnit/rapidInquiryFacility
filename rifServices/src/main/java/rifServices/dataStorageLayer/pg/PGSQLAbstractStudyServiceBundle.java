package rifServices.dataStorageLayer.pg;


import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.system.RIFServiceStartupOptions;
import rifGenericLibrary.util.FieldValidationUtility;

/**
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

class PGSQLAbstractStudyServiceBundle {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private boolean isInitialised;
	private PGSQLRIFServiceResources rifServiceResources;
	private RIFStudySubmissionAPI rifStudySubmissionService;
	private RIFStudyResultRetrievalAPI rifStudyRetrievalService;

	
	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLAbstractStudyServiceBundle() {
		isInitialised = false;
	}
		
	public synchronized void initialise(
		final RIFServiceStartupOptions rifServiceStartupOptions) 
		throws RIFServiceException {
		
		if (isInitialised == false) {
			
			rifServiceResources
				= PGSQLRIFServiceResources.newInstance(rifServiceStartupOptions);
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
		final PGSQLRIFServiceResources rifServiceResources) {

		this.rifServiceResources = rifServiceResources;
	}
	
	protected PGSQLRIFServiceResources getRIFServiceResources() {
		return rifServiceResources;
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
			PGSQLConnectionManager sqlConnectionManager
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
			PGSQLConnectionManager sqlConnectionManager
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
			PGSQLConnectionManager sqlConnectionManager
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

		PGSQLConnectionManager sqlConnectionManager
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
		PGSQLConnectionManager sqlConnectionManager
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

		if (userDeregistered == false) {
			//this helps service recover when one call generates an exception
			//and subsequent calls have one less available connection
			//because the try...catch setup didn't allow connection to
			//be put back in the "unused pile".
			
			//KLG: This may be causing problems so leave it out
			//sqlConnectionManager.resetConnectionPoolsForUser(user);					
		}
					
		RIFLogger rifLogger = RIFLogger.getLogger();
		rifLogger.error(
			PGSQLAbstractStudyServiceBundle.class, 
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
