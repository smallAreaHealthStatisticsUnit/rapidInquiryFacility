package rifServices.dataStorageLayer.pg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.AgeGroupSortingOption;
import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.Sex;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.common.DiseaseMappingStudyManager;
import rifServices.dataStorageLayer.common.HealthOutcomeManager;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.dataStorageLayer.common.SQLManager;
import rifServices.dataStorageLayer.common.StudyExtractManager;
import rifServices.dataStorageLayer.common.SubmissionManager;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.system.files.TomcatBase;
import rifServices.system.files.TomcatFile;
import rifServices.dataStorageLayer.common.SampleTestObjectGenerator;
/**
 * Main implementation of the RIF middleware.
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
 *Obtain a connection object from the {@lin SQLConnectionManager}.
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
  * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
 */

abstract class PGSQLAbstractRIFStudySubmissionService extends PGSQLAbstractRIFUserService {

		private static String lineSeparator = System.getProperty("line.separator");	

	/**
	 * Instantiates a new production rif job submission service.
	 */
	public PGSQLAbstractRIFStudySubmissionService() {

		String serviceName
		= RIFServiceMessages.getMessage("rifStudySubmissionService.name");
		setServiceName(serviceName);
		String serviceVersion
		= RIFServiceMessages.getMessage("rifStudySubmissionService.version");
		String serviceDescription
		= RIFServiceMessages.getMessage("rifStudySubmissionService.description");
		setServiceDescription(serviceDescription);
		String serviceContactEmail
		= RIFServiceMessages.getMessage("rifStudySubmissionService.contactEmail");
		setServiceContactEmail(serviceContactEmail);
	}

	public void test(final User user)
			throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();

		Connection connection = null;
		try {

			//Assign pooled connection
			connection 
			= sqlConnectionManager.assignPooledWriteConnection(user);
			String password=sqlConnectionManager.getUserPassword(user);
			SampleTestObjectGenerator testDataGenerator
			= new SampleTestObjectGenerator();
			RIFStudySubmission studySubmission
			= testDataGenerator.createSampleRIFJobSubmission();

			//Delegate operation to a specialised manager class			
			RIFServiceStartupOptions rifServiceStartupOptions
			= getRIFServiceStartupOptions();			
			PGSQLRunStudyThread runStudyThread = new PGSQLRunStudyThread();
			runStudyThread.initialise(
					connection, 
					user, 
					password,
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

	public ArrayList<CalculationMethod> getAvailableCalculationMethods(
			final User _user) 
					throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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
			SampleTestObjectGenerator generator = new SampleTestObjectGenerator();
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
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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
			AgeGenderYearManager sqlAgeGenderYearManager
			= rifServiceResources.getSqlAgeGenderYearManager();
			results
			= sqlAgeGenderYearManager.getAgeGroups(
					User.NULL_USER,
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
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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
			AgeGenderYearManager sqlAgeGenderYearManager
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
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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
		SQLManager sqlConnectionManager
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
		SQLManager sqlConnectionManager
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
		SQLManager sqlConnectionManager
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
			RIFContextManager sqlRIFContextManager
			= rifServiceResources.getSQLRIFContextManager();
			results
			= sqlRIFContextManager.getNumeratorDenominatorPairs(
					connection, 
					geography,
					healthTheme,
					User.NULL_USER);
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
	 * @param _user
	 * @param _geography
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
		SQLManager sqlConnectionManager
		= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user)) {
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
			RIFContextManager sqlRIFContextManager
			= rifServiceResources.getSQLRIFContextManager();
			result
			= sqlRIFContextManager.getNDPairFromNumeratorTableName(
					User.NULL_USER,
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


	public ArrayList<Project> getProjects(
			final User _user) throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
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
			DiseaseMappingStudyManager sqlDiseaseMappingStudyManager
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

	
	public String submitStudy(
			final User _user,
			final RIFStudySubmission _rifStudySubmission,
			final File _outputFile) throws RIFServiceException {



		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();

		String result = null;
		if (sqlConnectionManager.isUserBlocked(user)) {
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
			String password=sqlConnectionManager.getUserPassword(user);

			//Delegate operation to a specialised manager class			
			RIFServiceStartupOptions rifServiceStartupOptions
			= getRIFServiceStartupOptions();			
			PGSQLRunStudyThread runStudyThread = new PGSQLRunStudyThread();
			runStudyThread.initialise(
					connection, 
					user, 
					password,
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

	
	public String getFrontEndParameters(
		final User user) {
										
		String defaultJson = "{" + lineSeparator +
					"	parameters: {" + lineSeparator +
					"		usePouchDBCache: 	false,	// DO NOT Use PouchDB caching in TopoJSONGridLayer.js; it interacts with the diseasemap sync;" + lineSeparator +
					"		debugEnabled:		false,	// Disable front end debugging" + lineSeparator +
					"		mappingDefaults: 	{" + lineSeparator +					
					"			'diseasemap1': {}," + lineSeparator +
					"			'diseasemap2': {}," + lineSeparator +
					"			'viewermap': {}" + lineSeparator +
					"		}," + lineSeparator +
					"		defaultLogin: {" + lineSeparator +
					"			username: 	\"peter\"," + lineSeparator +
					"			password:	\"peter\"" + lineSeparator +
					"		}" + lineSeparator +
					"	}" + lineSeparator +
					"}";
		RIFLogger rifLogger = RIFLogger.getLogger();
		
		String jsonFromFile;

		TomcatFile tcFile = new TomcatFile(new TomcatBase(), TomcatFile.FRONT_END_PARAMETERS_FILE);

		try (BufferedReader reader = tcFile.reader()) {

				rifLogger.info(this.getClass(),
					"MSSQLRIFStudySubmissionService.getFrontEndParameters: using: " + tcFile.absolutePath());
				// Read and string escape JSON
				jsonFromFile = "{\"file\": \"" + StringEscapeUtils.escapeJavaScript(tcFile.absolutePath()) + "\", \"frontEndParameters\": \"" +
					StringEscapeUtils.escapeJavaScript(reader.lines().parallel().collect(Collectors.joining(lineSeparator))) +
					"\"}";
		} catch (IOException e) {
			rifLogger.warning(this.getClass(),
					"MSSQLRIFStudySubmissionService.getFrontEndParameters error for file: " +
							tcFile.absolutePath(), e);
			return defaultJson;
		}
		rifLogger.info(getClass(), "get FrontEnd Parameters: " + defaultJson);

		int len=jsonFromFile.length();
		StringBuilder escapedJson = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c0 = escapedJson.charAt(i);
			char c1;
			if ((i+1) >= len) {
				c1=0;
			} else {
				c1=escapedJson.charAt(i+1);
			}

			if (c0 != '\\' || c1 != '\'') {
				 escapedJson.append(c0);
			} // "'" Does need to be escaped as in double quotes
		}

		return escapedJson.toString();
	}
	
	/**
	 * Get textual extract status of a study.                          
	 * <p>   
	 * This function determines whether a study can be extracted from the database and the
	 * results returned to the user in a ZIP file
	 * </p>
	 * <p>
	 * Returns the following textual strings:
	 * <il>
	 *   <li>STUDY_INCOMPLETE_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
	 *     <ul>
	 *	     <li>C: created, not verified;</li>
	 *	     <li>V: verified, but no other work done; [NOT USED BY MIDDLEWARE]</li>
	 *	     <li>E: extracted imported or created, but no results or maps created;</li> 
	 *	     <li>R: initial results population, create map table; [NOT USED BY MIDDLEWARE] design]</li>
	 *	     <li>W: R warning. [NOT USED BY MIDDLEWARE]</li>
	 *     <ul>
	 *   </li>
	 *   <li>STUDY_FAILED_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings:
	 *	     <li>G: Extract failure, extract, results or maps not created;</li> 
	 *	     <li>F: R failure, R has caught one or more exceptions [depends on the exception handler]</li> 
	 *   </li>
	 *   <li>STUDY_EXTRACTABLE_NEEDS_ZIPPING: returned for the following rif40_studies.study_state code/meaning of: S: R success; 
	 *       when the ZIP extrsct file has not yet been created
	 *   </il>
	 *   <li>STUDY_EXTRABLE_ZIPPID: returned for the following rif40_studies.study_statu  code/meaning of: S: R success; 
	 *       when the ZIP extrsct file has been created
	 *   </il>
	 *   <il>STUDY_NOT_FOUND: returned where the studyID was not found in rif40_studies
	 *   </il>
	 *   <il>STUDY_ZIP_FAILED: returned for the following rif40_studies.study_statu  code/meaning of: S: R success; 
	 *       when the ZIP extract error file has been created
	 *   </il>
	 *   <il>STUDY_ZIP_IN_PROGRESS: returned for the following rif40_studies.study_statu  code/meaning of: S: R success; 
	 *       when the ZIP extract file has been created
	 *   </il>	 
	 * </il>
	 * </p>
	 * <p>
	 * Calls PGSQLStudyExtractManager.getExtractStatus()
	 * </p>
	 *
	 * @param  _user 		Database username of logged on user.
	 * @param  studyID 		Integer study identifier (database study_id field).
	 *
	 * @return 				Textual extract status 
	 *						NULL on exception or permission denied by sqlConnectionManager
	 */
	public String getExtractStatus(
			final User _user,
			final String studyID) 
					throws RIFServiceException {
						
		String result = null;
		RIFLogger rifLogger = RIFLogger.getLogger();
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}

		Connection connection = null;
		try {

			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
					"getExtractStatus",
					"user",
					user);
			fieldValidationUtility.checkNullMethodParameter(
					"getExtractStatus",
					"studyID",
					studyID);	

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getExtractStatus", 
					"studyID", 
					studyID);

			//Audit attempt to do operation
			String auditTrailMessage
			= RIFServiceMessages.getMessage("logging.getExtractStatus",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
			= sqlConnectionManager.assignPooledWriteConnection(user);

			SubmissionManager sqlRIFSubmissionManager
			= rifServiceResources.getRIFSubmissionManager();
			RIFStudySubmission rifStudySubmission
			= sqlRIFSubmissionManager.getRIFStudySubmission(
					connection, 
					user, 
					studyID);

			StudyExtractManager studyExtractManager
			= rifServiceResources.getSQLStudyExtractManager();
			result=studyExtractManager.getExtractStatus(
					connection, 
					user, 
					rifStudySubmission,
					studyID);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
					user,
					"getExtractStatus",
					rifServiceException);	
			// Effectively return NULL
		}
		finally {
			rifLogger.info(getClass(), "get ZIP file extract status, study: " + studyID + ": " + result);
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
					user, 
					connection);			
		}	
		
		return result;
	}	
	
	/**
	 * Get the JSON setup file for a run study.                          
	 * <p>   
	 * This function returns the JSON setup file for a run study, including the print setup 
	 * </p>
	 * <p>
	 * Returns the following textual strings:	
	 * @param  _user 		Database username of logged on user.
	 * @param  studyID 		Integer study identifier (database study_id field).
	 * @param  locale 		locale
	 * @param  tomcatServer e.g. http://localhost:8080.
	 *
	 * @return 				Textual JSON 
	 *						NULL on exception or permission denied by sqlConnectionManager
	 */	
	public String getJsonFile(
			final User _user,
			final String studyID,
			final Locale locale,
			final String tomcatServer) 
					throws RIFServiceException {
										
		String result = null;
		RIFLogger rifLogger = RIFLogger.getLogger();
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
		= rifServiceResources.getSqlConnectionManager();			
		
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		Connection connection = null;
		try {

			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
					"getJsonFile",
					"user",
					user);
			fieldValidationUtility.checkNullMethodParameter(
					"getJsonFile",
					"studyID",
					studyID);	

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getJsonFile", 
					"studyID", 
					studyID);

			//Audit attempt to do operation
			String auditTrailMessage
			= RIFServiceMessages.getMessage("logging.getJsonFile",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
			= sqlConnectionManager.assignPooledWriteConnection(user);

			SubmissionManager sqlRIFSubmissionManager
			= rifServiceResources.getRIFSubmissionManager();
			RIFStudySubmission rifStudySubmission
			= sqlRIFSubmissionManager.getRIFStudySubmission(
					connection, 
					user, 
					studyID);

			StudyExtractManager studyExtractManager
			= rifServiceResources.getSQLStudyExtractManager();
			result=studyExtractManager.getJsonFile(
					connection, 
					user, 
					rifStudySubmission,
					studyID,
					locale,
					tomcatServer);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
					user,
					"getJsonFile",
					rifServiceException);	
			// Effectively return NULL
		}
		finally {
			if (result == null) {
				result="{}";
			}
			rifLogger.info(getClass(), "get JSON file for study: " + studyID + "; locale: " + locale.toLanguageTag());
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
					user, 
					connection);			
		}	
		
		return result;
	}	
	
	public void createStudyExtract(
			final User _user,
			final String studyID,
			final String zoomLevel,
			final Locale locale,
			final String tomcatServer) 
					throws RIFServiceException {


		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
		= rifServiceResources.getSqlConnectionManager();			
		
		if (sqlConnectionManager.isUserBlocked(user)) {
			return;
		}

		RIFLogger rifLogger = RIFLogger.getLogger();
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
			fieldValidationUtility.checkNullMethodParameter(
					"createStudyExtract",
					"zoomLevel",
					zoomLevel);	


			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"createStudyExtract", 
					"studyID", 
					studyID);

			//Audit attempt to do operation
			String auditTrailMessage
			= RIFServiceMessages.getMessage("logging.createStudyExtract",
					user.getUserID(),
					user.getIPAddress(),
					studyID,
					zoomLevel);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
			= sqlConnectionManager.assignPooledWriteConnection(user);

			SubmissionManager sqlRIFSubmissionManager
			= rifServiceResources.getRIFSubmissionManager();
			RIFStudySubmission rifStudySubmission
			= sqlRIFSubmissionManager.getRIFStudySubmission(
					connection, 
					user, 
					studyID);

			StudyExtractManager studyExtractManager
			= rifServiceResources.getSQLStudyExtractManager();
			studyExtractManager.createStudyExtract(
					connection, 
					user, 
					rifStudySubmission,
					zoomLevel,
					studyID,
					locale,
					tomcatServer);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
					user,
					"createStudyExtract",
					rifServiceException);	
		}
		finally {
			rifLogger.info(getClass(), "Create ZIP file completed OK");
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
					user, 
					connection);			
		}

	}
	
	public String getStudyExtractFIleName(
			final User user,
			final String studyID)
		throws RIFServiceException {
		
			StudyExtractManager studyExtractManager
			= rifServiceResources.getSQLStudyExtractManager();
			return studyExtractManager.getStudyExtractFIleName(
					user, 
					studyID);			
		}
		
	public FileInputStream getStudyExtract(
			final User _user,
			final String studyID,
			final String zoomLevel) 
					throws RIFServiceException {


		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		FileInputStream fileInputStream = null;
		SQLManager sqlConnectionManager
		= rifServiceResources.getSqlConnectionManager();			

		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		RIFLogger rifLogger = RIFLogger.getLogger();
		Connection connection = null;
		try {

			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
					"getStudyExtract",
					"user",
					user);
			fieldValidationUtility.checkNullMethodParameter(
					"getStudyExtract",
					"studyID",
					studyID);	
			fieldValidationUtility.checkNullMethodParameter(
					"getStudyExtract",
					"zoomLevel",
					zoomLevel);	


			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getStudyExtract", 
					"studyID", 
					studyID);

			//Audit attempt to do operation
			String auditTrailMessage
			= RIFServiceMessages.getMessage("logging.getStudyExtract",
					user.getUserID(),
					user.getIPAddress(),
					studyID,
					zoomLevel);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
			= sqlConnectionManager.assignPooledWriteConnection(user);

			SubmissionManager sqlRIFSubmissionManager
			= rifServiceResources.getRIFSubmissionManager();
			RIFStudySubmission rifStudySubmission
			= sqlRIFSubmissionManager.getRIFStudySubmission(
					connection, 
					user, 
					studyID);

			StudyExtractManager studyExtractManager
			= rifServiceResources.getSQLStudyExtractManager();
			fileInputStream = studyExtractManager.getStudyExtract( 
					connection, 
					user, 
					rifStudySubmission,
					zoomLevel,
					studyID);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
					user,
					"getStudyExtract",
					rifServiceException);	
		}
		finally {
			rifLogger.info(getClass(), "Extract ZIP GET completed OK");
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
					user, 
					connection);			
		}

		return fileInputStream;
	}


	/**
	 * Gets the health code taxonomy given the name space
	 *
	 * @param _user the user
	 * @return the health code taxonomy corresponding to the name space
	 * @throws RIFServiceException the RIF service exception
	 */
	public HealthCodeTaxonomy getHealthCodeTaxonomyFromNameSpace(
			final User _user,
			final String nameSpace) throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
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
		SQLManager sqlConnectionManager
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
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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
