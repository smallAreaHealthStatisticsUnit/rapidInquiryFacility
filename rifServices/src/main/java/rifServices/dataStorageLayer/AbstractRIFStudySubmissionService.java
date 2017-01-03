package rifServices.dataStorageLayer;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifGenericLibrary.util.FieldValidationUtility;





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
 *log any attempts to do so.  If the method throws a {@link rifGenericLibrary.system.RIFServiceSecurityException},
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
 * Copyright 2017 Imperial College London, developed by the Small Area
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

abstract class AbstractRIFStudySubmissionService 
	extends AbstractRIFUserService
	implements RIFStudySubmissionAPI {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new production rif job submission service.
	 */
	public AbstractRIFStudySubmissionService() {

		String serviceName
			= RIFServiceMessages.getMessage("rifStudySubmissionService.name");
		setServiceName(serviceName);
		String serviceVersion
			= RIFServiceMessages.getMessage("rifStudySubmissionService.version");
		setServiceVersion(Double.valueOf(serviceVersion));
		String serviceDescription
			= RIFServiceMessages.getMessage("rifStudySubmissionService.description");
		setServiceDescription(serviceDescription);
		String serviceContactEmail
			= RIFServiceMessages.getMessage("rifStudySubmissionService.contactEmail");
		setServiceContactEmail(serviceContactEmail);
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	
	public void test()		
		throws RIFServiceException {
			
		//Defensively copy parameters and guard against blocked users
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	

		User user = User.newInstance("kgarwood", "kgarwood");

		Connection connection = null;
		try {
			
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			
			SampleTestObjectGenerator testDataGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission studySubmission
				= testDataGenerator.createSampleRIFJobSubmission();
			
			//Delegate operation to a specialised manager class			
			RIFServiceStartupOptions rifServiceStartupOptions
				= getRIFServiceStartupOptions();			
			RunStudyThread runStudyThread = new RunStudyThread();
			runStudyThread.initialise(
				connection, 
				user, 
				studySubmission, 
				rifServiceStartupOptions, 
				rifServiceResources);
			
			Thread thread = new Thread(runStudyThread);
			thread.start();
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			//Audit failure of operation
			logException(
				user,
				"test",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
	
	}

	
	
	public ArrayList<RIFOutputOption> getAvailableRIFOutputOptions(
		final User _user)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();		
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		ArrayList<RIFOutputOption> results
			= new ArrayList<RIFOutputOption>();
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getAvailableRIFOutputOptions",
				"user",
				user);
		
			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getAvailableRIFOutputOptions",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Perform operation
			results.add(RIFOutputOption.DATA);
			results.add(RIFOutputOption.MAPS);
			results.add(RIFOutputOption.RATIOS_AND_RATES);
			results.add(RIFOutputOption.POPULATION_HOLES);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getAvailableRIFOutputOptions",
				rifServiceException);
		}
		
		return results;
	}
		
	public ArrayList<CalculationMethod> getAvailableCalculationMethods(
		final User _user) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		ArrayList<CalculationMethod> results 
			= new ArrayList<CalculationMethod>();
		try {			

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getAvailableCalculationMethods",
				"user",
				user);
		
			//Check for security violations
			validateUser(user);
		
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getAvailableCalculationMethods",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Delegate operation to a specialised manager class		
			SampleTestObjectGenerator generator
				= new SampleTestObjectGenerator();
			results = generator.getSampleCalculationMethods();
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getAvailableCalculationMethods",
				rifServiceException);
		}
		
		return results;	
	}

	

	public ArrayList<AgeGroup> getAgeGroups(
		final User _user,
		final Geography _geography,
		final NumeratorDenominatorPair _ndPair,
		final AgeGroupSortingOption sortingOrder) 
		throws RIFServiceException {
				
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.createCopy(_ndPair);
		//no need to defensively copy sortingOrder because
		//it is an enumerated type
		
		ArrayList<AgeGroup> results = new ArrayList<AgeGroup>();		
		Connection connection = null;
		try {			
			//Check for empty parameters
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
		
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			ndPair.checkSecurityViolations();

			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getAgeGroups",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					ndPair.getDisplayName(),
					sortingOrder.name());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
						
			
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLAgeGenderYearManager sqlAgeGenderYearManager
				= rifServiceResources.getSqlAgeGenderYearManager();
			results
				= sqlAgeGenderYearManager.getAgeGroups(
					connection,
					geography,
					ndPair,
					sortingOrder);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getAgeGroups",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		
		return results;
		
	}
	
	public ArrayList<Sex> getSexes(
		final User _user)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		ArrayList<Sex> results = new ArrayList<Sex>();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getSexes",
				"user",
				user);
		
			//Check for security violations
			validateUser(user);		

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getSexes",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			
			//Delegate operation to a specialised manager class
			SQLAgeGenderYearManager sqlAgeGenderYearManager
				= rifServiceResources.getSqlAgeGenderYearManager();
			results
				= sqlAgeGenderYearManager.getGenders();
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getSexes",
				rifServiceException);
		}
		
		return results;	
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
		final User _user,
		final String healthCodeName,
		final String healthCodeNameSpace) throws RIFServiceException {
	
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
	
		HealthCode result = HealthCode.newInstance();
		try {

			//Check for empty parameters
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
				healthCodeNameSpace);		

			//Check for security violations
			validateUser(user);		
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getHealthCode",
				"healthCodeName",
				healthCodeName);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getHealthCode", 
				"healthCodeNameSpace",
				healthCodeNameSpace);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getHealthCode",
					user.getUserID(),
					user.getIPAddress(),
					healthCodeName,
					healthCodeNameSpace);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Delegate operation to a specialised manager class
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();
			result
				= healthOutcomeManager.getHealthCode(
					healthCodeName, 
					healthCodeNameSpace);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getHealthCode",
				rifServiceException);
		}
		
		return result;
		
	}
		
	public ArrayList<HealthCode> getImmediateChildHealthCodes(
		final User _user,		
		final HealthCode _parentHealthCode) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
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
		
			//Check for security violations
			validateUser(user);		
			parentHealthCode.checkSecurityViolations();			
							
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getImmediateSubterms",
					user.getUserID(),
					user.getIPAddress(),
					parentHealthCode.getCode(),
					parentHealthCode.getNameSpace());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			

			//Delegate operation to a specialised manager class
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();
			results
				= healthOutcomeManager.getImmediateSubterms(
					parentHealthCode);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getTopLevelCodes",
				rifServiceException);
		}
		
		return results;		
	}
	
	public HealthCode getParentHealthCode(
		final User _user,
		final HealthCode _childHealthCode) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
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
						
			//Check for security violations
			validateUser(user);		
			childHealthCode.checkSecurityViolations();			

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getParentHealthCode",
					user.getUserID(),
					user.getIPAddress(),
					childHealthCode.getCode(),
					childHealthCode.getNameSpace());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
						
			//Delegate operation to a specialised manager class
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();
			result
				= healthOutcomeManager.getParentHealthCode(
					childHealthCode);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getTopLevelCodes",
				rifServiceException);
		}
				
		return result;
	}
	
	
	public ArrayList<HealthCode> getHealthCodesMatchingSearchText(
		final User _user,
		final HealthCodeTaxonomy _healthCodeTaxonomy,
		final String searchText,
		final boolean isCaseSensitive)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		HealthCodeTaxonomy healthCodeTaxonomy
			= HealthCodeTaxonomy.createCopy(_healthCodeTaxonomy);

		ArrayList<HealthCode> results = new ArrayList<HealthCode>(); 
		Connection connection = null;
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
			
			//Check for security violations
			validateUser(user);
			healthCodeTaxonomy.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getHealthCodes", 
				"searchText", 
				searchText);
		
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getHealthCodes",
					user.getUserID(),
					user.getIPAddress(),
					healthCodeTaxonomy.getDisplayName(),
					searchText);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class		
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();			
			results 
				= healthOutcomeManager.getHealthCodes(
					healthCodeTaxonomy, 
					searchText,
					isCaseSensitive);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getTopLevelCodes",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		
		return results;		

	}
	//Features for RIF Context
	public ArrayList<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
		final User _user,
		final Geography _geography,
		final HealthTheme _healthTheme) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		HealthTheme healthTheme = HealthTheme.createCopy(_healthTheme);
		
		ArrayList<NumeratorDenominatorPair> results = 
			new ArrayList<NumeratorDenominatorPair>();		
		Connection connection = null;
		try {
			
			//Check for empty parameters
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
			
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			healthTheme.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getNumeratorDenominatorPairs",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					healthTheme.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class			
			SQLRIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
			results
				= sqlRIFContextManager.getNumeratorDenominatorPairs(
					connection, 
					geography,
					healthTheme);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getNumeratorDenominatorPair",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
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

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		Geography geography = Geography.createCopy(_geography);
		
		
		NumeratorDenominatorPair result = NumeratorDenominatorPair.newInstance(); 
		Connection connection = null;
		try {
			
			//Check for empty parameters
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

			fieldValidationUtility.checkNullMethodParameter(
				"getNumeratorDenominatorPairFromNumeratorTable",
				"numeratorTableName",
				numeratorTableName);		
			
			
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getNumeratorDenominatorPairFromNumeratorTable", 
				"numeratorTableName", 
				numeratorTableName);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getNumeratorDenominatorPairFromNumeratorTable",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					numeratorTableName);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
						
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);
			
			//Delegate operation to a specialised manager class
			SQLRIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
			result
				= sqlRIFContextManager.getNDPairFromNumeratorTableName(
					connection, 
					geography,
					numeratorTableName);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getNumeratorDenominatorPair",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}

		return result;
	
	}

	public MapAreaSummaryData getSummaryDataForCurrentExtent(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelToMap _geoLevelToMap) throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
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
		Connection connection = null;
		try {
			
			//Check for empty parameters
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

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelArea.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();
	
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getSummaryDataForCurrentExtent",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName(),
					geoLevelArea.getDisplayName(),
					geoLevelToMap.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLMapDataManager sqlMapDataManager
				= rifServiceResources.getSQLMapDataManager();
			result
				= sqlMapDataManager.getSummaryDataForExtentAreas(
					connection, 
					geography,
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getSummaryDataForCurrentExtent",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		
		return result;	
	}

	
	public ArrayList<MapArea> getMapAreas(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelToMap _geoLevelToMap) 
		throws RIFServiceException {
			
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
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
		
		//Check for empty parameters
		ArrayList<MapArea> results = new ArrayList<MapArea>();
		Connection connection = null;
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

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelArea.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();
			String auditTrailMessage 
				= RIFServiceMessages.getMessage(
					"logging.getMapAreas",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName(),
					geoLevelToMap.getDisplayName());
			
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLMapDataManager sqlMapDataManager
				= rifServiceResources.getSQLMapDataManager();			
			results
				= sqlMapDataManager.getMapAreas(
					connection, 
					geography,
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap);
		
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			//Audit failure of operation
			logException(
				user,
				"getMapAreas",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
	
		return results;
	}

	
	//TOUR_ADD_METHOD-2
	/*	
	 * Once we add a new method to the interface, we need to implement it in the
	 * appropriate service class.
	 */
	public String getMapAreasForBoundaryRectangle(
		final User _user,
		final Geography _geography,			
		final GeoLevelSelect _geoLevelSelect,
		final BoundaryRectangle _boundaryRectangle) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {			
			return null;
		}
		
		Geography geography 
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		BoundaryRectangle boundaryRectangle
			= BoundaryRectangle.createCopy(_boundaryRectangle);

		//Check for empty parameters
		String results = "";
		Connection connection = null;
		try {
			
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreasForBoundaryRectangle",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreasForBoundaryRectangle",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreasForBoundaryRectangle",
				"geoLevelSelect",
				geoLevelSelect);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreasForBoundaryRectangle",
				"boundaryRectangle",
				boundaryRectangle);
						
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			boundaryRectangle.checkSecurityViolations();
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getMapAreasForBoundaryRectangle",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName(),
					boundaryRectangle.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLMapDataManager sqlMapDataManager
				= rifServiceResources.getSQLMapDataManager();			
			results
				= sqlMapDataManager.getMapAreasForBoundaryRectangle(
					connection, 
					geography,
					geoLevelSelect,
					boundaryRectangle);						
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getMapAreasForBoundaryArea",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
	
	
		return results;
	}

	
	public ArrayList<MapArea> getMapAreasByBlock(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelToMap _geoLevelToMap,
		final Integer startIndex,
		final Integer endIndex) throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
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
		Connection connection = null;
		try {
			
			//Check for empty parameters
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

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelArea.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getSummaryDataForCurrentExtent",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName(),
					geoLevelArea.getDisplayName(),
					geoLevelToMap.getDisplayName(),
					String.valueOf(startIndex),
					String.valueOf(endIndex));
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLMapDataManager sqlMapDataManager
				= rifServiceResources.getSQLMapDataManager();		
			results
				= sqlMapDataManager.getMapAreas(
					connection, 
					geography,
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap,
					startIndex,
					endIndex);
		
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getMapAreas",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		
		return results;
	}
	
	public ArrayList<GeoLevelToMap> getGeoLevelToMapValues(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography 
			= Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		
		ArrayList<GeoLevelToMap> results = new ArrayList<GeoLevelToMap>();
		Connection connection = null;
		try {
			
			//Check for empty parameters
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
			
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
		
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getGeoLevelToMapValues",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
						
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLRIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
			results
				= sqlRIFContextManager.getGeoLevelToMapValues(
					connection, 
					geography,
					geoLevelSelect);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelToMapValues",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
			
		return results;
	}
	
	
	
	public MapAreaSummaryData getMapAreaSummaryInformation(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelToMap _geoLevelToMap,
		final ArrayList<MapArea> _mapAreas) throws RIFServiceException {
	
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
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
		Connection connection = null;
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
				
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();			
			for (MapArea mapArea : mapAreas) {
				mapArea.checkSecurityViolations();
			}

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getMapAreaSummaryInformation",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName(),
					geoLevelArea.getDisplayName(),
					geoLevelToMap.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLMapDataManager sqlMapDataManager
				= rifServiceResources.getSQLMapDataManager();			
			result
				= sqlMapDataManager.getMapAreaSummaryInformation(
					connection, 
					geography,
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap,
					mapAreas);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getMapAreaSummaryInformation",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		
		return result;
	}
	
	
	public ArrayList<Project> getProjects(
		final User _user) throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		ArrayList<Project> results = new ArrayList<Project>();
		Connection connection = null;
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getProjects",
				"user",
				user);
		
			//Check for security violations
			validateUser(user);
		
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getProjects",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
						
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);	

			//Delegate operation to a specialised manager class
			SQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager
				= rifServiceResources.getSqlDiseaseMappingStudyManager();
			results
				= sqlDiseaseMappingStudyManager.getProjects(
					connection,
					user);		
				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getProjects",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}

		return results;			
	}

	public BufferedImage getImage(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelArea _geoLevelArea,
		final GeoLevelView _geoLevelView,
		final ArrayList<MapArea> _mapAreas)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(_geoLevelSelect);
		GeoLevelArea geoLevelArea
			= GeoLevelArea.createCopy(_geoLevelArea);
		GeoLevelView geoLevelView
			= GeoLevelView.createCopy(_geoLevelView);
		ArrayList<MapArea> mapAreas
			= MapArea.createCopy(_mapAreas);
				
		BufferedImage result = new 
			BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
		
		Connection connection = null;
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
				"geography",
				geography);		
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
		
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelArea.checkSecurityViolations();
			geoLevelView.checkSecurityViolations();
			for (MapArea mapArea : mapAreas) {
				mapArea.checkSecurityViolations();
			}
		
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getImage",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName(),
					geoLevelArea.getDisplayName(),
					geoLevelView.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);
			
			//Delegate operation to a specialised manager class
			SQLMapDataManager sqlMapDataManager
				= rifServiceResources.getSQLMapDataManager();			
			result
				= sqlMapDataManager.getImage(
					connection,
					geoLevelSelect,
					geoLevelArea,
					geoLevelView,
					mapAreas);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getImage",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
				
		return result;
	}
	
	public String submitStudy(
		final User _user,
		final RIFStudySubmission _rifStudySubmission,
		final File _outputFile) throws RIFServiceException {
		
		
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		
		String result = null;
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return result;
		}
		RIFStudySubmission rifStudySubmission
			= RIFStudySubmission.createCopy(_rifStudySubmission);
		
		File outputFile = null;
		if (_outputFile != null) {
			outputFile = new File(_outputFile.getAbsolutePath());			
		}

		Connection connection = null;
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
				"rifStudySubmission",
				rifStudySubmission);	
						
			//Check for security violations
			validateUser(user);
			rifStudySubmission.checkSecurityViolations();		
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();
			String outputFileName = "";
			if (outputFile != null) {
				outputFileName = outputFile.getAbsolutePath();
			}
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.submittingStudy",
					user.getUserID(),
					user.getIPAddress(),
					rifStudySubmission.getDisplayName(),
					outputFileName);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
		
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledWriteConnection(user);

			//Delegate operation to a specialised manager class			
			RIFServiceStartupOptions rifServiceStartupOptions
				= getRIFServiceStartupOptions();			
			RunStudyThread runStudyThread = new RunStudyThread();
			runStudyThread.initialise(
				connection, 
				user, 
				rifStudySubmission, 
				rifServiceStartupOptions, 
				rifServiceResources);
			
			Thread thread = new Thread(runStudyThread);
			thread.run();	
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"submitStudy",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);			
		}

		return result;
	}
	
	
	
	public void createStudyExtract(
		final User _user,
		final String studyID) 
		throws RIFServiceException {
		
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();			
		String result;
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}

		Connection connection = null;
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"createStudyExtract",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"createStudyExtract",
				"studyID",
				studyID);	
		
			
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"createStudyExtract", 
				"studyID", 
				studyID);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.createStudyExtract",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledWriteConnection(user);
			
			SQLRIFSubmissionManager sqlRIFSubmissionManager
				= rifServiceResources.getRIFSubmissionManager();
			RIFStudySubmission rifStudySubmission
				= sqlRIFSubmissionManager.getRIFStudySubmission(
					connection, 
					user, 
					studyID);
			
			SQLStudyExtractManager studyExtractManager
				= rifServiceResources.getSQLStudyExtractManager();
			studyExtractManager.createStudyExtract(
				connection, 
				user, 
				rifStudySubmission);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"createStudyExtract",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);			
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
		final String nameSpace) throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		HealthCodeTaxonomy result = HealthCodeTaxonomy.newInstance();
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
				"nameSpace",
				nameSpace);
			
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getHealthCodeTaxonomyFromNameSpace", 
				"healthCodeTaxonomyNameSpace", 
				nameSpace);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();	
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getHealthCodeTaxonomyFromNameSpace",
					user.getUserID(),
					user.getIPAddress(),
					nameSpace);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Delegate operation to a specialised manager class
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();
			result 
				= healthOutcomeManager.getHealthCodeTaxonomyFromNameSpace(
					nameSpace);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getHealthCodeTaxonomies",
				rifServiceException);
		}
		
		return result;
		
	}
	
	public ArrayList<HealthCodeTaxonomy> getHealthCodeTaxonomies(
		final User _user) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		ArrayList<HealthCodeTaxonomy> results = new ArrayList<HealthCodeTaxonomy>();
		try {
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getHealthCodeTaxonomies",
				"user",
				user);

			//Check for security violations
			validateUser(user);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();	
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getHealthCodeTaxonomies",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			
			//Delegate operation to a specialised manager class			
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();			
			results 
				= healthOutcomeManager.getHealthCodeTaxonomies();
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getHealthCodeTaxonomies",
				rifServiceException);
		}
		
		return results;
	}
	
	public ArrayList<HealthCode> getTopLevelHealthCodes(
		final User _user,
		final HealthCodeTaxonomy healthCodeTaxonomy) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
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
		
			//Check for security violations
			validateUser(user);
			healthCodeTaxonomy.checkSecurityViolations();
		
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();	
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getTopLevelCodes",
					user.getUserID(),
					user.getIPAddress(),
					healthCodeTaxonomy.getName(),
					healthCodeTaxonomy.getNameSpace());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Delegate operation to a specialised manager class
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();			
			results 
				= healthOutcomeManager.getTopLevelCodes(
						healthCodeTaxonomy);	
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getTopLevelCodes",
				rifServiceException);
		}
	
		return results;
	}
		
	// ==========================================
	// Section Override
	// ==========================================
}
