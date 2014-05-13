package rifServices;

import rifServices.businessConceptLayer.*;
import rifServices.dataStorageLayer.*;
import rifServices.system.*;
import rifServices.taxonomyServices.HealthCodeProvider;
import rifServices.util.FieldValidationUtility;


import rifServices.util.RIFLogger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;

/**
 * Main implementation of the RIF middle ware.  
 * <p>
 * The main roles of this class are to support:
 * <ul>
 * <li>
 * use final parameters for all API methods to help minimise concurrency issues.
 * </li>
 * <li>
 * defensively copy method parameters to minimise concurrency and security issues
 * </li>
 * <li>
 * detect any empty method parameters
 * <li>
 * <li>
 * detect any security violations (eg: injection attacks)
 * </li>
 * </ul>
 *<p>
 *Most methods in the class perform the same sequence of steps:
 *<ol>
 *<li><b>Defensively copy parameter values<b>.  This step ensures that parameter values passed by the
 *client will not change any of their values while the method executes.  Defensive copying helps
 *minimise problems of multiple threads altering parameter values that are passed to the methods.  The
 *technique also helps reduce the likelihood of certain types of security attacks.
 *</li>
 *<li>
 *Ensure none of the required parameter values are empty.  By empty, we mean they are either null, or in the case
 *of Strings, null or an empty string.  This step helps minimise the effort the service has to spend recovering from
 *null pointer exceptions.
 *</li>
 *<li>
 *Scan every parameter value for malicious field values.  Sometimes this just means scanning a String parameter value
 *for text that could be used as part of a malicous code attack.  In this step, the <code>checkSecurityViolations(..)</code>
 *methods for business objects are called.  Note that the <code>checkSecurityViolations</code> method of a business
 *object will scan each text field for malicious field values, and recursively call the same method in any child
 *business objects it may possess.
 *</li>
 *<li>
 *Obtain a connection object from the {@link rifServices.dataStorageLayer.SLQConnectionManager}.  
 *</li>
 *<li>
 *Delegate to a manager class, using a method with the same name.  Pass the connection object as part of the call. 
 *</li>
 *<li>
 *Return the connection object to the connection pool.
 *<li>
 *
 *<h2>Security</h2>
 *The RIF software contains many different types of checks which are designed to detect or prevent malicious code
 *attacks.  Defensive copying prevents an attacker from trying to change parameter values sometime when the method
 *has not finished executing.  The use of PreparedStatement objects, and trigger checks that are part of the database
 *should eliminate the prospect of a malicious attack.  However, calls to the <code>checkSecurityViolations(..)</code>
 *methods are intended to identify an attack anyway.  We feel that we should not merely prevent malicious attacks but
 *log any attempts to do so.  If the method throws a {@link rifServices.businessConceptLayer.RIFServiceSecurityException},
 *then this class will log it and continue to pass it to client application.
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
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

public class ProductionRIFJobSubmissionService implements RIFJobSubmissionAPI {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	/** The health outcome manager. */
	private SQLHealthOutcomeManager healthOutcomeManager;
	
	/** The covariate manager. */
	private SQLCovariateManager covariateManager;
	
	/** The sql connection manager. */
	private SQLConnectionManager sqlConnectionManager;
	
	/** The sql rif context manager. */
	private SQLRIFContextManager sqlRIFContextManager;
	
	/** The sql age gender year manager. */
	private SQLAgeGenderYearManager sqlAgeGenderYearManager;
	
	/** The sql map data manager. */
	private SQLMapDataManager sqlMapDataManager;
	
	/** The disease mapping study manager. */
	private SQLDiseaseMappingStudyManager diseaseMappingStudyManager;
	
	private SQLRIFSubmissionManager rifSubmissionManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new production rif job submission service.
	 */
	public ProductionRIFJobSubmissionService() {
		
	}

	public void initialiseService() {
		initialiseService(new RIFServiceStartupOptions());
	}
	
	public void initialiseService(RIFServiceStartupOptions rifServiceStartupOptions) {
		sqlConnectionManager = new SQLConnectionManager(rifServiceStartupOptions);
		healthOutcomeManager = new SQLHealthOutcomeManager(rifServiceStartupOptions);
		sqlRIFContextManager = new SQLRIFContextManager();
		sqlAgeGenderYearManager 
			= new SQLAgeGenderYearManager(sqlRIFContextManager);
		sqlMapDataManager 
			= new SQLMapDataManager(
				rifServiceStartupOptions, 
				sqlRIFContextManager);
		covariateManager = new SQLCovariateManager(sqlRIFContextManager);
		diseaseMappingStudyManager = new SQLDiseaseMappingStudyManager();
		
		rifSubmissionManager 
			= new SQLRIFSubmissionManager(
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				covariateManager);
		
		try {
			healthOutcomeManager.initialiseTaxomies();			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printStackTrace(System.out);
		}
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	/**
	 * Validate user.
	 *
	 * @param user the user
	 * @throws RIFServiceException the RIF service exception
	 */
	private void validateUser(
		final User user) throws RIFServiceException {
				
		user.checkErrors();
		user.checkSecurityViolations();
				
		if (sqlConnectionManager.userExists(user.getUserID())) {
			//user exists so valid
			//KLG: expand this feature later to handle things like
			//time-outs
			return;
		}
		
		//invalid user attempting to use service
		String errorMessage
			= RIFServiceMessages.getMessage(
				"user.error.invalidUser",
				user.getUserID());
		RIFServiceSecurityException rifServiceSecurityException
			 = new RIFServiceSecurityException(errorMessage);
		throw rifServiceSecurityException;
	}
		
	private void logException(
		final User user,
		final String methodName,
		final RIFServiceException rifServiceException) 
		throws RIFServiceException {
		
		boolean userDeregistered = false;
		if (rifServiceException instanceof RIFServiceSecurityException) {
			//gives opportunity to log security issue and deregister user
			sqlConnectionManager.addUserIDToBlock(user);
			sqlConnectionManager.deregisterUser(user);
			userDeregistered = true;
		}

		if (userDeregistered == false) {
			//this helps service recover when one call generates an exception
			//and subsequent calls have one less available connection
			//because the try...catch setup didn't allow connection to
			//be put back in the "unused pile".
			sqlConnectionManager.resetConnectionPoolsForUser(user);					
		}
				
		RIFLogger rifLogger = new RIFLogger();
		rifLogger.error(
			ProductionRIFJobSubmissionService.class, 
			methodName, 
			rifServiceException);
	
		throw rifServiceException;
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#login(java.lang.String, java.lang.String)
	 */
	public void login(
		final String userID,
		final char[] password) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		//No need -- userID and password are final objects
		
		//Part II: Check for empty parameters
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
		
			//Part III: Check for security violations

			fieldValidationUtility.checkMaliciousMethodParameter(
				"login",
				"userID",
				userID);
			fieldValidationUtility.checkMaliciousPasswordValue(
				"login",
				"password",
				password);

			//Part IV: Perform operation
			sqlConnectionManager.registerUser(userID, password);		
		}
		catch(RIFServiceException rifServiceException) {

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				ProductionRIFJobSubmissionService.class, 
				"login", 
				rifServiceException);
			
		}
		
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#logout(rifServices.businessConceptLayer.User)
	 */
	public void logout(
		final User _user) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);

		try {
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"logout",
				"user",
				user);
		
			//Part III: Check for security violations
			user.checkSecurityViolations();

			sqlConnectionManager.deregisterUser(user);		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"logout",
				rifServiceException);
		}
	
	}

	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getRIFServiceInformation(rifServices.businessConceptLayer.User)
	 */
	public RIFServiceInformation getRIFServiceInformation(
		final User _user) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		RIFServiceInformation result = null;
		try {

			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getRIFServiceInformation",
				"user",
				user);
		
			//Part III: Check for security violations
			validateUser(user);
		
			//Part IV: Perform operation
			SampleTestObjectGenerator generator
				= new SampleTestObjectGenerator();
			result = generator.getSampleRIFServiceInformation();
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getRIFServiceInformation",
				rifServiceException);
		}
		
		return result;
	}
		
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getAvailableRIFOutputOptions(rifServices.businessConceptLayer.User)
	 */
	public ArrayList<RIFOutputOption> getAvailableRIFOutputOptions(
		final User _user)
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		ArrayList<RIFOutputOption> results
			= new ArrayList<RIFOutputOption>();
		try {

			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getAvailableRIFOutputOptions",
				"user",
				user);
		
			//Part III: Check for security violations
			validateUser(user);

			results.add(RIFOutputOption.DATA);
			results.add(RIFOutputOption.MAPS);
			results.add(RIFOutputOption.RATIOS_AND_RATES);
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getAvailableRIFOutputOptions",
				rifServiceException);
		}
		
		return results;
	}
		
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getAvailableCalculationMethods(rifServices.businessConceptLayer.User)
	 */
	public ArrayList<CalculationMethod> getAvailableCalculationMethods(
		final User _user) 
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		ArrayList<CalculationMethod> results 
			= new ArrayList<CalculationMethod>();
		try {			

			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getAvailableCalculationMethods",
				"user",
				user);
		
			//Part III: Check for security violations
			validateUser(user);
				
			SampleTestObjectGenerator generator
				= new SampleTestObjectGenerator();
			results = generator.getSampleCalculationMethods();
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getAvailableCalculationMethods",
				rifServiceException);
		}
		
		return results;	
	}

	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getDiseaseMappingStudies(rifServices.businessConceptLayer.User)
	 */
	public ArrayList<DiseaseMappingStudy> getDiseaseMappingStudies(
		final User _user) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		ArrayList<DiseaseMappingStudy> results
			= new ArrayList<DiseaseMappingStudy>();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getDiseaseMappingStudies",
				"user",
				user);
		
			//Part III: Check for security violations
			validateUser(user);
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getDiseaseMappingStudies",
				rifServiceException);
		}
		
		return results;		
	}

	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getAgeGroups(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.NumeratorDenominatorPair, rifServices.businessConceptLayer.RIFJobSubmissionAPI.AgeGroupSortingOption)
	 */
	public ArrayList<AgeGroup> getAgeGroups(
		final User _user,
		final Geography _geography,
		final NumeratorDenominatorPair _ndPair,
		final AgeGroupSortingOption sortingOrder) 
		throws RIFServiceException {
				
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.createCopy(_ndPair);
		//no need to defensively copy sortingOrder because
		//it is an enumerated type
		
		ArrayList<AgeGroup> results = new ArrayList<AgeGroup>();
		try {			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getAgeGroups",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getAgeGroups",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getAgeGroups",
				"numeratorDenominatorPair",
				ndPair);

			//sortingOrder can be null, it just means that the
			//order will be ascending lower limit
		
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			ndPair.checkSecurityViolations();
		
			//Part IV: Perform operation
			Connection connection
				= sqlConnectionManager.getReadConnection(user);
			results
				= sqlAgeGenderYearManager.getAgeGroups(
					connection,
					geography,
					ndPair,
					sortingOrder);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getAgeGroups",
				rifServiceException);
		}
		
		return results;
		
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getGenders(rifServices.businessConceptLayer.User)
	 */
	public ArrayList<Sex> getGenders(
		final User _user)
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		ArrayList<Sex> results = new ArrayList<Sex>();
		try {
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGenders",
				"user",
				user);
		
			//Part III: Check for security violations
			validateUser(user);		
			//Part IV: Perform operation
			results
				= sqlAgeGenderYearManager.getGenders();
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getGenders",
				rifServiceException);
		}
		
		return results;	
	}
	
	/**
	 * Adds the health code provider.
	 *
	 * @param _adminUser the _admin user
	 * @param healthCodeProvider the health code provider
	 * @throws RIFServiceException the RIF service exception
	 */
	protected void addHealthCodeProvider(
		User _adminUser,
		HealthCodeProvider healthCodeProvider) throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User adminUser = User.createCopy(_adminUser);
		if (sqlConnectionManager.isUserBlocked(adminUser) == true) {
			return;
		}
		
		//Part II: Check for security violations
		try {
			validateUser(adminUser);
			healthOutcomeManager.addHealthCodeProvider(healthCodeProvider);
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				adminUser,
				"addHealthCodeProvider",
				rifServiceException);
		}
	}

	/**
	 * Clear health code providers.
	 *
	 * @param _adminUser the _admin user
	 * @throws RIFServiceException the RIF service exception
	 */
	protected void clearHealthCodeProviders(User _adminUser) 
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User adminUser = User.createCopy(_adminUser);
		if (sqlConnectionManager.isUserBlocked(adminUser) == true) {
			return;
		}
		
		try {
			//Part II: Check for security violations
			validateUser(adminUser);
			healthOutcomeManager.clearHealthCodeProviders();		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				adminUser,
				"clearHealthCodeProviders",
				rifServiceException);
		}
	}
		
	
	/**
	 * Gets the health code taxonomy given the name space
	 *
	 * @param user the user
	 * @return the health code taxonomy corresponding to the name space
	 * @throws RIFServiceException the RIF service exception
	 */
	public HealthCodeTaxonomy getHealthCodeTaxonomyFromNameSpace(
		final User _user, 
		final String healthCodeTaxonomyNameSpace) throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		HealthCodeTaxonomy result = null;
		try {
			//Part II: Check for security violations
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCodeTaxonomyFromNameSpace",
				"user",
				user);		
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCodeTaxonomyFromNameSpace",
				"healthCodeTaxonomyNameSpace",
				healthCodeTaxonomyNameSpace);
			
			//Part III: Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getHealthCodeTaxonomyFromNameSpace", 
				"healthCodeTaxonomyNameSpace", 
				healthCodeTaxonomyNameSpace);

			result 
				= healthOutcomeManager.getHealthCodeTaxonomyFromNameSpace(
					healthCodeTaxonomyNameSpace);
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getHealthCodeTaxonomies",
				rifServiceException);
		}
		
		return result;
		
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getHealthCodeTaxonomies(rifServices.businessConceptLayer.User)
	 */
	public ArrayList<HealthCodeTaxonomy> getHealthCodeTaxonomies(
		final User _user) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		ArrayList<HealthCodeTaxonomy> results = new ArrayList<HealthCodeTaxonomy>();
		//Part II: Check for security violations
		try {
			validateUser(user);
			results 
				= healthOutcomeManager.getHealthCodeTaxonomies();
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getHealthCodeTaxonomies",
				rifServiceException);
		}
		
		return results;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getTopLevelCodes(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.HealthCodeTaxonomy)
	 */
	public ArrayList<HealthCode> getTopLevelCodes(
		final User _user,
		final HealthCodeTaxonomy healthCodeTaxonomy) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		//Part II: Check for empty parameter values
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
		try {
			
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getTopLevelCodes",
				"user",
				user);		
			fieldValidationUtility.checkNullMethodParameter(
				"getTopLevelCodes",
				"healthCodeTaxonomy",
				healthCodeTaxonomy);
		
			//Part III: Check for security violations
			validateUser(user);
			healthCodeTaxonomy.checkSecurityViolations();
		
			//Part IIV: Perform operation
			Connection connection
				= sqlConnectionManager.getReadConnection(user);
			results 
				= healthOutcomeManager.getTopLevelCodes(
						healthCodeTaxonomy);	
			
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getTopLevelCodes",
				rifServiceException);
		}
	
		return results;
	}
	
	
	/**
	 * Test method used to ensure that service can identify age groups that
	 * don't exist in the age groups providers
	 * @param user
	 * @param connection
	 * @param healthCodes
	 * @throws RIFServiceException
	 */
	protected void checkNonExistentAgeGroups(
		User _user,
		ArrayList<AgeBand> _ageBands) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		ArrayList<AgeBand> ageBands
			= AgeBand.createCopy(_ageBands);
			
		//no need to defensively copy sortingOrder because
		//it is an enumerated type
			
		try {			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"checkNonExistentAgeGroups",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"checkNonExistentAgeGroups",
				"ageBands",
				ageBands);

			//sortingOrder can be null, it just means that the
			//order will be ascending lower limit
			
			//Part III: Check for security violations
			validateUser(user);
			for (AgeBand ageBand : ageBands) {
				ageBand.checkSecurityViolations();					
			}

			//Part IV: Perform operation
			Connection connection
				= sqlConnectionManager.getReadConnection(user);

			sqlAgeGenderYearManager.checkNonExistentAgeGroups(
				connection,
				ageBands); 
			
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);
				

		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"checkNonExistentAgeGroups",
				rifServiceException);
		}			
	}	
		
	/**
	 * Test method used to ensure that service can identify age groups that
	 * don't exist in the age groups providers
	 * @param user
	 * @param connection
	 * @param healthCodes
	 * @throws RIFServiceException
	 */
	protected void checkNonExistentCovariates(
		User _user,
		ArrayList<AbstractCovariate> _covariates) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		ArrayList<AbstractCovariate> covariates = new ArrayList<AbstractCovariate>();	
		for (AbstractCovariate _covariate : _covariates) {
			if (_covariate instanceof AdjustableCovariate) {
				//adjustable covariate
				AdjustableCovariate adjustableCovariate
					= (AdjustableCovariate) _covariate;
				covariates.add(AdjustableCovariate.createCopy(adjustableCovariate));
			}
			else {
				//exposure covariate
				ExposureCovariate exposureCovariate
					= (ExposureCovariate) _covariate;
				covariates.add(ExposureCovariate.createCopy(exposureCovariate));				
			}			
		}
		
		//no need to defensively copy sortingOrder because
		//it is an enumerated type
			
		try {			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"checkNonExistentCovariates",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"checkNonExistentCovariates",
				"covariates",
				covariates);

			//sortingOrder can be null, it just means that the
			//order will be ascending lower limit
			
			//Part III: Check for security violations
			validateUser(user);
			for (AbstractCovariate covariate : covariates) {
				covariate.checkSecurityViolations();					
			}

			//Part IV: Perform operation
			Connection connection
				= sqlConnectionManager.getReadConnection(user);

			covariateManager.checkNonExistentCovariates(
				connection,
				covariates); 		

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);

		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"checkNonExistentCovariates",
				rifServiceException);
		}			
	}	
	
	/**
	 * a helper method used to support web services that pass single string values rather than
	 * a java object to represent a record.  This method uses two values, healthCodeName and
	 * healthCodeNameSpace, to retrieve a fully populated HealthCode object.  The object can
	 * then be available to make successive calls to other parts of the RIFJobSubmissionAPI.
	 * @param _user
	 * @param healthCodeName
	 * @param healthCodeNameSpace
	 * @return
	 * @throws RIFServiceException
	 */
	public HealthCode getHealthCode(
		User _user,
		String healthCodeName,
		String healthCodeNameSpace) throws RIFServiceException {
	
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
	
		HealthCode result = null;
		try {

			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCode",
				"user",
				user);		
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCode",
				"healthCodeName",
				healthCodeName);		
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCode",
				"healthCodeNameSpace",
				healthCodeName);		

			//Part III: Check for security violations
			validateUser(user);		
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getHealthCode",
				"healthCodeName",
				healthCodeName);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getHealthCode", 
				"healthCodeNameSpace",
				healthCodeNameSpace);
			
			result
				= healthOutcomeManager.getHealthCode(
					healthCodeName, 
					healthCodeNameSpace);
			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getHealthCode",
				rifServiceException);
		}
		
		return result;
		
	}
		
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getImmediateSubterms(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.HealthCode)
	 */
	public ArrayList<HealthCode> getImmediateSubterms(
		final User _user,			
		final HealthCode _parentHealthCode) 
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		HealthCode parentHealthCode = HealthCode.createCopy(_parentHealthCode);
		
		//Part II: Check for empty parameter values
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
		try {
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getImmediateSubterms",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getImmediateSubterms",
				"parentHealthCode",
				parentHealthCode);		
		
			//Part III: Check for security violations
			validateUser(user);		
			parentHealthCode.checkSecurityViolations();			
				
			//Part IV: Perform operation
			Connection connection
				= sqlConnectionManager.getReadConnection(user);		
			results
				= healthOutcomeManager.getImmediateSubterms(
					parentHealthCode);			

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getTopLevelCodes",
				rifServiceException);
		}
		
		return results;		
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getParentHealthCode(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.HealthCode)
	 */
	public HealthCode getParentHealthCode(
		final User _user,
		final HealthCode _childHealthCode) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		HealthCode childHealthCode = HealthCode.createCopy(_childHealthCode);
		
		//Part II: Check for empty parameter values
		HealthCode result = HealthCode.newInstance();
		try {
			
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getParentHealthCode",
				"user",
				user);		
			fieldValidationUtility.checkNullMethodParameter(
					"getParentHealthCode",
					"childHealthCode",
					childHealthCode);		
						
			//Part III: Check for security violations
			validateUser(user);		
			childHealthCode.checkSecurityViolations();			
		
			//Part IV: Perform operation
			Connection connection
				= sqlConnectionManager.getReadConnection(user);
			result
				= healthOutcomeManager.getParentHealthCode(
					childHealthCode);
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);

		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getTopLevelCodes",
				rifServiceException);
		}
				
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getHealthCodes(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.HealthCodeTaxonomy, java.lang.String)
	 */
	public ArrayList<HealthCode> getHealthCodes(
		final User _user,
		final HealthCodeTaxonomy _healthCodeTaxonomy,
		final String searchText) throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		HealthCodeTaxonomy healthCodeTaxonomy
			= HealthCodeTaxonomy.createCopy(_healthCodeTaxonomy);
		
		ArrayList<HealthCode> results = new ArrayList<HealthCode>(); 
		try {
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCodes",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCodes",
				"healthCodeTaxonomy",
				healthCodeTaxonomy);
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCodes",
				"searchText", 
				searchText);
		
			//Part III: Check for security violations
			validateUser(user);
			healthCodeTaxonomy.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getHealthCodes", 
				"searchText", 
				searchText);
		
			//Part IV: Perform operation		
			Connection connection
				= sqlConnectionManager.getReadConnection(user);
			results 
				= healthOutcomeManager.getHealthCodes(
					healthCodeTaxonomy, 
					searchText);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getTopLevelCodes",
				rifServiceException);
		}
		
		return results;		

	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getCovariates(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect, rifServices.businessConceptLayer.GeoLevelToMap)
	 */
	public ArrayList<AbstractCovariate> getCovariates(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelToMap _geoLevelToMap)
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect 
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
	
		ArrayList<AbstractCovariate> results = new ArrayList<AbstractCovariate>();
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCovariates",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getCovariates",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getCovariates",
				"geoLevelSelect",
				geoLevelSelect);
		
			fieldValidationUtility.checkNullMethodParameter(
				"getCovariates",
				"geoLevelToMap",
				geoLevelToMap);
						
			//Part III: Check for security violations
			System.out.println("ProfRIFSubmissionService getCovariates 1");
			validateUser(user);
			System.out.println("ProfRIFSubmissionService getCovariates 2");
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();

			//Part IV: Perform operation		
			System.out.println("ProfRIFSubmissionService getCovariates 3");
			Connection connection
				= sqlConnectionManager.getReadConnection(user);
			System.out.println("ProfRIFSubmissionService getCovariates 4");
			results 
				= covariateManager.getCovariates(
					connection, 
					geography, 
					geoLevelSelect,
					geoLevelToMap);

			System.out.println("ProfRIFSubmissionService getCovariates 5");
			
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);
			System.out.println("ProfRIFSubmissionService getCovariates 6");

		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getCovariates",
				rifServiceException);	
		}
				
		return results;		
	}
	
	/**
	 * Gets the geographies.
	 *
	 * @param _user the _user
	 * @param _geography the _geography
	 * @return the geographies
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Geography> getGeographies(
		final User _user,
		final Geography _geography) 
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);

		ArrayList<Geography> results = new ArrayList<Geography>();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographies",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographies",
				"geography",
				geography);
						
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
				
			//Part IV: Perform operation		
			Connection connection
				= sqlConnectionManager.getReadConnection(user);
			results = sqlRIFContextManager.getGeographies(connection);
			
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getGeographies",
				rifServiceException);	
		}
					
		return results;
	}
	
	
	//Features for RIF Context
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getGeographies(rifServices.businessConceptLayer.User)
	 */
	public ArrayList<Geography> getGeographies(
		final User _user) 
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		ArrayList<Geography> results = new ArrayList<Geography>();
		try {
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographies",
				"user",
				user);
		
			//Part III: Check for security violations
			validateUser(user);

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			results
				= sqlRIFContextManager.getGeographies(connection);
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getGeographies",
				rifServiceException);	
		}
		
		return results;		
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getHealthThemes(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography)
	 */
	public ArrayList<HealthTheme> getHealthThemes(
		final User _user,
		final Geography _geography)
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		
		ArrayList<HealthTheme> results = new ArrayList<HealthTheme>();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthThemes",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthThemes",
				"geography",
				geography);
		
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
		
			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			results
				= sqlRIFContextManager.getHealthThemes(connection, geography);
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getHealthThemes",
				rifServiceException);	
		}
			
		return results;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getNumeratorDenominatorPairs(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.HealthTheme)
	 */
	public ArrayList<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
		final User _user,
		final Geography _geography,
		final HealthTheme _healthTheme) 
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		HealthTheme healthTheme = HealthTheme.createCopy(_healthTheme);
		Geography geography = Geography.createCopy(_geography);
		
		ArrayList<NumeratorDenominatorPair> results = 
			new ArrayList<NumeratorDenominatorPair>();
		
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getNumeratorDenominatorPair",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getNumeratorDenominatorPair",
				"healthTheme",
				healthTheme);
			fieldValidationUtility.checkNullMethodParameter(
				"getNumeratorDenominatorPair",
				"geography",
				geography);		
			
			//Part III: Check for security violations
			validateUser(user);
			healthTheme.checkSecurityViolations();
		
			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			results
				= sqlRIFContextManager.getNumeratorDenominatorPairs(
					connection, 
					geography,
					healthTheme);			
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getNumeratorDenominatorPair",
				rifServiceException);	
		}

		return results;
	}


	/**
	 * Convenience method to obtain everything that is needed to get 
	 * @param user
	 * @param geography
	 * @param healthTheme
	 * @param numeratorTableName
	 * @return
	 * @throws RIFServiceException
	 */
	public NumeratorDenominatorPair getNumeratorDenominatorPairFromNumeratorTable(
		final User _user,
		final Geography _geography,
		final String numeratorTableName) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		Geography geography = Geography.createCopy(_geography);
		
		
		NumeratorDenominatorPair result = null; 
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getNumeratorDenominatorPairFromNumeratorTable",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getNumeratorDenominatorPairFromNumeratorTable",
				"geography",
				geography);		
			
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getNumeratorDenominatorPairFromNumeratorTable", 
				"numeratorTableName", 
				numeratorTableName);
			
			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			
			result
				= sqlRIFContextManager.getNDPairFromNumeratorTableName(
					connection, 
					geography,
					numeratorTableName);
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getNumeratorDenominatorPair",
				rifServiceException);	
		}

		return result;
	
	}
	
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getGeographicalLevelSelectValues(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography)
	 */
	public ArrayList<GeoLevelSelect> getGeographicalLevelSelectValues(
		final User _user,
		final Geography _geography)
		throws RIFServiceException {
			
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);

		ArrayList<GeoLevelSelect> results = new ArrayList<GeoLevelSelect>();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographicalLevelSelectValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographicalLevelSelectValues",
				"geography",
				geography);

			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			results
				= sqlRIFContextManager.getGeoLevelSelectValues(
					connection, 
					geography);
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getGeographicalLevelSelectValues",
				rifServiceException);	
		}
		
		return results;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getDefaultGeoLevelSelectValue(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography)
	 */
	public GeoLevelSelect getDefaultGeoLevelSelectValue(
		final User _user,
		final Geography _geography) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);

		GeoLevelSelect result = GeoLevelSelect.newInstance();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getDefaultGeoLevelSelectValue",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getDefaultGeoLevelSelectValue",
				"geography",
				geography);

			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			result
				= sqlRIFContextManager.getDefaultGeoLevelSelectValue(
					connection, 
					geography);
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getDefaultGeoLevelSelectValue",
				rifServiceException);	
		}
		
		return result;

	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getGeoLevelAreaValues(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect)
	 */
	public ArrayList<GeoLevelArea> getGeoLevelAreaValues(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
	
		ArrayList<GeoLevelArea> results = new ArrayList<GeoLevelArea>();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAreaValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAreaValues",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAreaValues",
				"geoLevelSelect",
				geoLevelSelect);	
		
			//Part III: Check for security violations
				validateUser(user);
				geography.checkSecurityViolations();
				geoLevelSelect.checkSecurityViolations();

			//Part IV: Perform operation
				Connection connection 
					= sqlConnectionManager.getReadConnection(user);
				results
					= sqlRIFContextManager.getGeoLevelAreaValues(
						connection, 
						geography,
						geoLevelSelect);

				sqlConnectionManager.releaseReadConnection(
					user, 
					connection);			

		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getGeoLevelAreaValues",
				rifServiceException);	
		}

		return results;
	}	

	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getGeoLevelViewValues(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect)
	 */
	public ArrayList<GeoLevelView> getGeoLevelViewValues(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		
		ArrayList<GeoLevelView> results = new ArrayList<GeoLevelView>();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelViewValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelViewValues",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelViewValues",
				"geoLevelSelect",
				geoLevelSelect);	
		
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
	
			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			results
				= sqlRIFContextManager.getGeoLevelViewValues(
					connection, 
					geography,
					geoLevelSelect);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getGeoLevelViewValues",
				rifServiceException);	
		}
		
		return results;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getGeoLevelToMapValues(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect)
	 */
	public ArrayList<GeoLevelToMap> getGeoLevelToMapValues(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		
		ArrayList<GeoLevelToMap> results = new ArrayList<GeoLevelToMap>();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelToMapValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelToMapValues",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelToMapValues",
				"geoLevelSelect",
				geoLevelSelect);	
			
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
		
			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			results
				= sqlRIFContextManager.getGeoLevelToMapValues(
					connection, 
					geography,
					geoLevelSelect);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			
			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getGeoLevelToMapValues",
				rifServiceException);	
		}
			
		return results;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getYearRange(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.NumeratorDenominatorPair)
	 */
	public YearRange getYearRange(
		final User _user,
		final Geography _geography,
		final NumeratorDenominatorPair _ndPair) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);		
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);
		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.createCopy(_ndPair);
		
		YearRange result = YearRange.newInstance();
		try {			
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getYearRange",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getYearRange",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getYearRange",
				"numeratorDenominatorPair",
				ndPair);	
		
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			ndPair.checkSecurityViolations();

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			result
				= sqlAgeGenderYearManager.getYearRange(
					connection, 
					geography,
					ndPair);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);			

		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getYearRange",
				rifServiceException);	
		}
		
		return result;		
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getSummaryDataForCurrentExtent(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect, rifServices.businessConceptLayer.GeoLevelArea, rifServices.businessConceptLayer.GeoLevelToMap)
	 */
	public MapAreaSummaryData getSummaryDataForCurrentExtent(
			User _user,
			Geography _geography,
			GeoLevelSelect _geoLevelSelect,
			GeoLevelArea _geoLevelArea,
			GeoLevelToMap _geoLevelToMap) throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);		
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		GeoLevelArea geoLevelArea
			= GeoLevelArea.createCopy(_geoLevelArea);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
	
		MapAreaSummaryData result = MapAreaSummaryData.newInstance();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getSummaryDataForCurrentExtent",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getSummaryDataForCurrentExtent",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getSummaryDataForCurrentExtent",
				"geoLevelSelect",
				geoLevelSelect);
			fieldValidationUtility.checkNullMethodParameter(
				"getSummaryDataForCurrentExtent",
				"geoLevelArea",
				geoLevelArea);
			fieldValidationUtility.checkNullMethodParameter(
				"getSummaryDataForCurrentExtent",
				"geoLevelToMap",
				geoLevelToMap);	

			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelArea.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();
	
			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);

			result
				= sqlMapDataManager.getSummaryDataForExtentAreas(
					connection, 
					geography,
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getSummaryDataForCurrentExtent",
				rifServiceException);	
		}
		
		return result;	
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getMapAreas(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect, rifServices.businessConceptLayer.GeoLevelArea, rifServices.businessConceptLayer.GeoLevelToMap)
	 */
	public ArrayList<MapArea> getMapAreas(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelToMap _geoLevelToMap) throws RIFServiceException {
			
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);		
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		GeoLevelArea geoLevelArea
			= GeoLevelArea.createCopy(_geoLevelArea);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
		
		//Part II: Check for empty parameters
		ArrayList<MapArea> results = new ArrayList<MapArea>();
		try {
			
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"geoLevelSelect",
				geoLevelSelect);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"geoLevelArea",
			geoLevelArea);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"geoLevelToMap",
				geoLevelToMap);	

			//Part III: Check for security violations
				validateUser(user);
				geography.checkSecurityViolations();
				geoLevelSelect.checkSecurityViolations();
				geoLevelArea.checkSecurityViolations();
				geoLevelToMap.checkSecurityViolations();

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			
			results
				= sqlMapDataManager.getMapAreas(
					connection, 
					geography,
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);		
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getMapAreas",
				rifServiceException);	
		}
		
		return results;
	}

	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getMapAreas(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect, rifServices.businessConceptLayer.GeoLevelArea, rifServices.businessConceptLayer.GeoLevelToMap, java.lang.Integer, java.lang.Integer)
	 */
	public ArrayList<MapArea> getMapAreas(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelToMap _geoLevelToMap,
		final Integer startIndex,
		final Integer endIndex) throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);		
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		GeoLevelArea geoLevelArea
			= GeoLevelArea.createCopy(_geoLevelArea);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
		
		ArrayList<MapArea> results = new ArrayList<MapArea>();
		try {
			
			//Part II: Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"geoLevelSelect",
				geoLevelSelect);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"geoLevelArea",
				geoLevelArea);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreas",
				"geoLevelToMap",
				geoLevelToMap);	

			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelArea.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
		
			results
				= sqlMapDataManager.getMapAreas(
					connection, 
					geography,
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap,
					startIndex,
					endIndex);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);		
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getMapAreas",
				rifServiceException);	
		}
		
		return results;
	}
	
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getMapAreaSummaryInformation(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect, rifServices.businessConceptLayer.GeoLevelArea, rifServices.businessConceptLayer.GeoLevelToMap, java.util.ArrayList)
	 */
	public MapAreaSummaryData getMapAreaSummaryInformation(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelToMap _geoLevelToMap,
		final ArrayList<MapArea> _mapAreas) throws RIFServiceException {
	
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect 
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		GeoLevelArea geoLevelArea
			= GeoLevelArea.createCopy(_geoLevelArea);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
		ArrayList<MapArea> mapAreas
			= MapArea.createCopy(_mapAreas);
		
		MapAreaSummaryData result = MapAreaSummaryData.newInstance();
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaSummaryInformation",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaSummaryInformation",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaSummaryInformation",
				"geoLevelSelect",
				geoLevelSelect);
		
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaSummaryInformation",
				"geoLevelToMap",
				geoLevelToMap);
						
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaSummaryInformation",
				"mapAreas",
				mapAreas);
				
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();
			
			for (MapArea mapArea : mapAreas) {
				mapArea.checkSecurityViolations();
			}

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			
			result
				= sqlMapDataManager.getMapAreaSummaryInformation(
					connection, 
					geography,
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap,
					mapAreas);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getMapAreaSummaryInformation",
				rifServiceException);	
		}
		
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getGeometry(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Geography, rifServices.businessConceptLayer.GeoLevelSelect, rifServices.businessConceptLayer.GeoLevelToMap, java.util.ArrayList)
	 */
	public String getGeometry(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelToMap _geoLevelToMap,
		final ArrayList<MapArea> _mapAreas) throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect 
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
		ArrayList<MapArea> mapAreas
			= MapArea.createCopy(_mapAreas);
		
		String result = "";
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeometry",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeometry",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeometry",
				"geoLevelSelect",
				geoLevelSelect);		
			fieldValidationUtility.checkNullMethodParameter(
				"getGeometry",
				"geoLevelToMap",
				geoLevelToMap);						
			fieldValidationUtility.checkNullMethodParameter(
				"getGeometry",
				"mapAreas",
				mapAreas);
				
			//Part III: Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();
			
			for (MapArea mapArea : mapAreas) {
				mapArea.checkSecurityViolations();
			}

			//Part IV: Perform operation		
			//@TODO
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getGeometry",
				rifServiceException);	
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getProjects(rifServices.businessConceptLayer.User)
	 */
	public ArrayList<Project> getProjects(
		final User _user) throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		ArrayList<Project> results = new ArrayList<Project>();
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getProjects",
				"user",
				user);
		
			//Part III: Check for security violations
			validateUser(user);
		
			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);			
			results
				= diseaseMappingStudyManager.getProjects(
					connection,
					user);		
			
			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);		
				

		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getProjects",
				rifServiceException);	
		}

		return results;			
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getStudies(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.Project)
	 */
	public ArrayList<AbstractStudy> getStudies(
		final User _user,
		final Project _project) 
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Project project = Project.createCopy(_project);
		
		ArrayList<AbstractStudy> results = new ArrayList<AbstractStudy>();
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getStudies",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getStudies",
				"project",
				project);		
		
			//Part III: Check for security violations
			validateUser(user);
			project.checkSecurityViolations();

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			
			results
				= diseaseMappingStudyManager.getStudies(
					connection,
					user,
					project);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);		
		
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getStudies",
				rifServiceException);	
		}
		
		return results;				
	}

	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#getImage(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.GeoLevelSelect, rifServices.businessConceptLayer.GeoLevelArea, rifServices.businessConceptLayer.GeoLevelView, java.util.ArrayList)
	 */
	public BufferedImage getImage(
		final User _user,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelView _geoLevelView,
		final ArrayList<MapArea> _mapAreas)
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		GeoLevelArea geoLevelArea
			= GeoLevelArea.createCopy(_geoLevelArea);
		GeoLevelView geoLevelView
			= GeoLevelView.createCopy(_geoLevelView);
		ArrayList<MapArea> mapAreas
			= MapArea.createCopy(_mapAreas);
				
		BufferedImage result = null;
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getImage",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getImage",
				"geoLevelSelect",
				geoLevelSelect);		
			fieldValidationUtility.checkNullMethodParameter(
				"getImage",
				"geoLevelArea",
				geoLevelArea);		
			fieldValidationUtility.checkNullMethodParameter(
				"getImage",
				"geoLevelView",
				geoLevelView);		
			fieldValidationUtility.checkNullMethodParameter(
				"getImage",
				"mapAreas",
				mapAreas);	
		
			//Part III: Check for security violations
			validateUser(user);
			geoLevelSelect.checkSecurityViolations();
			geoLevelArea.checkSecurityViolations();
			geoLevelView.checkSecurityViolations();
			for (MapArea mapArea : mapAreas) {
				mapArea.checkSecurityViolations();
			}
		
			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(user);
			
			result
				= sqlMapDataManager.getImage(
					connection,
					geoLevelSelect,
					geoLevelArea,
					geoLevelView,
					mapAreas);

			sqlConnectionManager.releaseReadConnection(
				user, 
				connection);		
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getImage",
				rifServiceException);	
		}
				
		return result;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.RIFJobSubmissionAPI#submitStudy(rifServices.businessConceptLayer.User, rifServices.businessConceptLayer.RIFJobSubmission, java.io.File)
	 */
	public void submitStudy(
		User _user,
		RIFJobSubmission _rifJobSubmission,
		File _outputFile) throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		RIFJobSubmission rifJobSubmission
			= RIFJobSubmission.createCopy(_rifJobSubmission);
		
		File outputFile = null;
		if (_outputFile != null) {
			outputFile = new File(_outputFile.getAbsolutePath());			
		}

		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"submitStudy",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"submitStudy",
				"rifJobSubmission",
				rifJobSubmission);	
		
			//Part III: Check for security violations
			validateUser(user);
			rifJobSubmission.checkSecurityViolations();
		
			rifJobSubmission.checkErrors();		

			RIFLogger rifLogger = new RIFLogger();	
			
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.submittingStudy",
					user.getUserID(),
					user.getIPAddress(),
					rifJobSubmission.getDisplayName());
			rifLogger.info(
				ProductionRIFJobSubmissionService.class,
				auditTrailMessage);
		
			/*
			Connection connection
				= sqlConnectionManager.getWriteConnection(user);
			rifSubmissionManager.addRIFJobSubmission(
				connection, 
				user, 
				rifJobSubmission);
			*/
			
			//RIFZipFileWriter rifZipFileWriter = new RIFZipFileWriter();
			//rifZipFileWriter.writeZipFile(outputFile, rifJobSubmission);		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"submitStudy",
				rifServiceException);	
		}

	}
	
	protected void clearRIFJobSubmissions(
		User _adminUser) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User adminUser = User.createCopy(_adminUser);
		if (sqlConnectionManager.isUserBlocked(adminUser) == true) {
			return;
		}
		
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"clearRIFJobSubmissions",
				"adminUser",
				adminUser);	

			//Part III: Check for security violations
			validateUser(adminUser);

			//Part IV: Perform operation
			Connection connection 
				= sqlConnectionManager.getReadConnection(adminUser);

			rifSubmissionManager.clearRIFJobSubmissions(connection);

			sqlConnectionManager.releaseReadConnection(
				adminUser, 
				connection);		
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				adminUser,
				"clearRIFJobSubmissions",
				rifServiceException);	
		}
		
	}	
	
	protected void deregisterAllUsers() throws RIFServiceException {		
		sqlConnectionManager.deregisterAllUsers();
	}
	
	// ==========================================
	// Section Override
	// ==========================================
}
