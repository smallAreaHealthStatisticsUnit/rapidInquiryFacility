package org.sahsu.rif.services.datastorage.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.AgeGroupSortingOption;
import org.sahsu.rif.services.concepts.CalculationMethod;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Project;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.concepts.RIFStudySubmissionAPI;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.concepts.YearRange;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

import org.json.JSONObject;

public class StudySubmissionService extends StudyService implements RIFStudySubmissionAPI {

	private static String lineSeparator = System.getProperty("line.separator");

	public StudySubmissionService() {

	}

	@Override
	public YearRange getYearRange(
			final User _user,
			final Geography _geography,
			final NumeratorDenominatorPair _ndPair)
		throws RIFServiceException {

		//TOUR_SECURITY
		/*
		 * The client code may be able to change the value of a parameter object within the
		 * period between when this method starts and when it finishes.  Theoretically, a
		 * malicious client could attempt to change harmless parameter values into malicious values
		 * some time after security checks have been done but before a database operation is actually
		 * being executed.  Safe copying takes a snapshot of the parameter values and uses that for
		 * the rest of the method's code block.
		 *
		 */

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			//TOUR SECURITY
			/*
			 * The first most important security task is to check whether the user has
			 * already been blacklisted because of some prior security exception.  This
			 * check is designed to ensure that malicious attacks use the least amount of
			 * computational resources as possible.
			 */
			return null;
		}
		Geography geography
			= Geography.createCopy(_geography);
		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.createCopy(_ndPair);

		YearRange result = YearRange.newInstance();
		Connection connection = null;
		try {

			//Check for empty parameters
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


			//TOUR_SECURITY
			/*
			 * Here we go through each of the method parameter objects that are instantiated from
			 * concepts classes.  "checkSecurityViolations()" is meeant to recursively
			 * check through all the String field values and check whether they have patterns
			 * which could match known types of malicious code attacks.
			 */
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			ndPair.checkSecurityViolations();

			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getYearRange",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					ndPair.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			AgeGenderYearManager sqlAgeGenderYearManager
				= rifServiceResources.getSqlAgeGenderYearManager();
			result
				= sqlAgeGenderYearManager.getYearRange(
					user,
					connection,
					geography,
					ndPair);
		}
		catch(RIFServiceException rifServiceException) {

			//TOUR_SECURITY
			/*
			 * logException(...) will determine whether the error is a security exception
			 * or not.  If it is, the middleware will blacklist the user
			 */

			//Audit failure of operation
			logException(
				user,
				"getYearRange",
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


	@Override
	public List<HealthTheme> getHealthThemes(
			final User _user,
			final Geography _geography)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);

		ArrayList<HealthTheme> results = new ArrayList<>();
		Connection connection = null;
		try {

			//Check for empty parameters
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

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();


			//TOUR_CONCURRENCY
			/*
			 * The logging facility here is an example of a shared resource that
			 * will be a source of contention.  The logger is managed as a single
			 * instance that will be used by all threads.
			 */
			//Audit attempt to do operation
			String auditTrailMessage
				= SERVICE_MESSAGES.getMessage("logging.getHealthThemes",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName());
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
				= sqlRIFContextManager.getHealthThemes(connection, geography);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getHealthThemes",
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



	@Override
	public List<AbstractCovariate> getCovariates(final User _user, final Geography _geography,
			final GeoLevelToMap _geoLevelToMap) throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}

		Geography geography = Geography.createCopy(_geography);
		GeoLevelToMap geoLevelToMap = GeoLevelToMap.createCopy(_geoLevelToMap);

		List<AbstractCovariate> results = new ArrayList<>();
		Connection connection = null;
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter("getCovariates", "user", user);
			fieldValidationUtility.checkNullMethodParameter("getCovariates", "geography",
			                                                geography);

			fieldValidationUtility.checkNullMethodParameter("getCovariates", "geoLevelToMap",
			                                                geoLevelToMap);

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();

			//Audit attempt to do operation
			String auditTrailMessage = SERVICE_MESSAGES.getMessage("logging.getCovariates",
			                                                       user.getUserID(),
			                                                       user.getIPAddress(),
			                                                       geography.getDisplayName(),
			                                                       geoLevelToMap.getDisplayName());
			rifLogger.info(getClass(), auditTrailMessage);

			// Assign pooled connection
			connection = sqlConnectionManager.assignPooledReadConnection(user);

			// Delegate operation to a specialised manager class
			CovariateManager sqlCovariateManager = rifServiceResources.getSqlCovariateManager();
			results = sqlCovariateManager.getCovariates(connection, geography, geoLevelToMap);

		}
		catch(RIFServiceException rifServiceException) {
			// Audit failure of operation
			logException(user, "getCovariates", rifServiceException);
		}
		finally {
			// Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(user, connection);
		}

		return results;
	}

	@Override
	public RIFServiceStartupOptions getRIFServiceStartupOptions() {
		return rifServiceResources.getRIFServiceStartupOptions();
	}

	/**
	 * Rationale here seems to be that we create a string containing a default set
	 * of JSON values, and then try to read from a file in one of the standard
	 * locations. If the file is found, its JSON is read, escaped, and returned.
	 * Otherwise the default JSON is returned.
	 * @param user is not used
	 * @return either the default escaped JSON string, or the version from the file
	 */
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
		AppFile appFile = AppFile.getFrontEndParametersInstance();

		try (BufferedReader reader = appFile.reader()) {

				rifLogger.info(getClass(),
					"StudySubmissionService.getFrontEndParameters: using: "
							+ appFile.asString());

				// Read and string escape JSON
				jsonFromFile = "{\"file\": \"" +
				               StringEscapeUtils.escapeJavaScript(appFile.asString()) +
				               "\", \"frontEndParameters\": \"" +
				               StringEscapeUtils.escapeJavaScript(
								reader.lines().parallel().collect(Collectors
										.joining(lineSeparator))) + "\"}";
		} catch (IOException ioException2) {
				rifLogger.warning(this.getClass(),
					"StudySubmissionService.getFrontEndParameters error for file: " +
						appFile.asString(), ioException2);
				return defaultJson;
		}

		int len = jsonFromFile.length();
		StringBuilder escapedJson = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c0 = jsonFromFile.charAt(i);
			char c1;
			if ((i+1) >= len) {
				c1=0;
			} else {
				c1=jsonFromFile.charAt(i+1);
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
	 * This fucntion determines whether a study can be extracted from the database and the results returned to the user in a ZIP file
	 * </p>
	 * <p>
	 * Returns the following textual strings:
	 * <il>
	 *   <li>STUDY_INCOMPLETE_NOT_ZIPPABLE: returned for the following rif40_studies.study_state codes/meanings]:
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
	 *	     <li>F: R failure, R has caught one or more exceptions [depends on the exception handler</li>
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
	 * Calls StudyExtractManager.getExtractStatus()
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
		SQLManager sqlConnectionManager
		= rifServiceResources.getSqlConnectionManager();

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

			SubmissionManager sqlRIFSubmissionManager =
					rifServiceResources.getRIFSubmissionManager();
			RIFStudySubmission rifStudySubmission = sqlRIFSubmissionManager.getRIFStudySubmission(
					connection,
					user,
					studyID);

			StudyExtractManager studyExtractManager =
					rifServiceResources.getSQLStudyExtractManager();
			result=studyExtractManager.getExtractStatus(
					connection, user, rifStudySubmission, studyID);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(user, "getExtractStatus", rifServiceException);
			// Effectively return NULL
		}
		finally {
			rifLogger.info(getClass(), "get ZIP file extract status, study: " + studyID
			                           + ": " + result);
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(user, connection);
		}

		return result;
	}

    	//Features for RIF Context
	public JSONObject getRif40NumDenom(
		final User _user)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}

		JSONObject results = new JSONObject();
		Connection connection = null;
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getNumeratorDenominatorPair",
				"user",
				user);

			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getNumeratorDenominatorPairs",
					user.getUserID(),
					user.getIPAddress(),
					"N/A",
					"N/A");
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
				= sqlRIFContextManager.getRif40NumDenom(
					connection, user);
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

/*
	//Features for RIF Context
	public ArrayList<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
		final User _user,
		final Geography _geography,
		final HealthTheme _healthTheme)
		throws RIFServiceException {
     */
  public ArrayList<Project> getProjects(
		final User _user) throws RIFServiceException {   

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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

	public String getStudyExtractFIleName(
			final User user,
			final String studyID) {

			StudyExtractManager studyExtractManager
			= rifServiceResources.getSQLStudyExtractManager();
			return studyExtractManager.getStudyExtractFIleName(
					user,
					studyID);
		}

	public void createStudyExtract(final User user, final String studyID, final String zoomLevel,
			final Locale locale, final String url) throws RIFServiceException {

		new StudyExtract(user, studyID, zoomLevel, locale, url,
		                 rifServiceResources).create();
	}

	/**
	 * Convenience method to obtain everything that is needed to get
	 * @param u
	 * @param geog
	 * @param numeratorTableName
	 * @return
	 * @throws RIFServiceException
	 */
	public NumeratorDenominatorPair getNumeratorDenominatorPairFromNumeratorTable(
		final User u,
		final Geography geog,
		final String numeratorTableName)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(u);
		SQLManager sqlConnectionManager =
						rifServiceResources.getSqlConnectionManager();

		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}

		Geography geography = Geography.createCopy(geog);
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
					user,
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

	public List<AgeGroup> getAgeGroups(
		final User _user,
		final Geography _geography,
		final NumeratorDenominatorPair _ndPair,
		final AgeGroupSortingOption sortingOrder)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.createCopy(_ndPair);
		//no need to defensively copy sortingOrder because
		//it is an enumerated type

		List<AgeGroup> results = new ArrayList<>();
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
			results = sqlAgeGenderYearManager.getAgeGroups(user, connection, geography, ndPair,
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

	public List<CalculationMethod> getAvailableCalculationMethods(
		final User _user)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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

	public String submitStudy(final User _user, final RIFStudySubmission rifStudySubmission,
			final File _outputFile, final String url) throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();

		String result = null;
		if (sqlConnectionManager.isUserBlocked(user)) {
			return null;
		}
		// RIFStudySubmission rifStudySubmission
		// 	= RIFStudySubmission.createCopy(_rifStudySubmission);

		File outputFile = null;
		if (_outputFile != null) {
			outputFile = new File(_outputFile.getAbsolutePath());
		}

		Connection connection = null;
		ExecutorService exec = Executors.newSingleThreadExecutor();
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
			rifLogger.info(getClass(), auditTrailMessage);

			//Assign pooled connection
			connection = sqlConnectionManager.assignPooledWriteConnection(user);
			String password=sqlConnectionManager.getUserPassword(user);

			//Delegate operation to a specialised manager class
			RIFServiceStartupOptions rifServiceStartupOptions = getRIFServiceStartupOptions();
			RunStudyThread runStudyThread = new RunStudyThread();
			runStudyThread.initialise(
				connection,
				user,
				password,
				rifStudySubmission,
				rifServiceStartupOptions,
				rifServiceResources,
				url);

			exec.execute(runStudyThread);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"submitStudy",
				rifServiceException);
		}
		finally {

			exec.shutdown();

			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
				user,
				connection);
		}

		return result;
	}

	public List<Sex> getSexes(
		final User _user)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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

	public void test(final User user, String url)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();

		Connection connection = null;
		ExecutorService exec = Executors.newSingleThreadExecutor();
		try {

			//Assign pooled connection
			connection = sqlConnectionManager.assignPooledWriteConnection(user);
			String password=sqlConnectionManager.getUserPassword(user);

			SampleTestObjectGenerator testDataGenerator = new SampleTestObjectGenerator();
			RIFStudySubmission studySubmission = testDataGenerator.createSampleRIFJobSubmission();

			//Delegate operation to a specialised manager class
			RIFServiceStartupOptions rifServiceStartupOptions = getRIFServiceStartupOptions();
			RunStudyThread runStudyThread = new RunStudyThread();
			runStudyThread.initialise(
				connection,
				user,
				password,
				studySubmission,
				rifServiceStartupOptions,
				rifServiceResources,
				url);

			exec.execute(runStudyThread);
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

			exec.shutdown();

			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user,
				connection);
		}

	}

	//Features for RIF Context
	public List<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
		final User _user,
		final Geography _geography,
		final HealthTheme _healthTheme)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
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
					user);
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
	 * Get the JSON setup file for a run study.
	 * <p>
	 * This function returns the JSON setup file for a run study, including the print setup
	 * </p>
	 * <p>
	 * Returns the following textual strings:
	 * @param  _user 		Database username of logged on user.
	 * @param  studyID 		Integer study identifier (database study_id field).
	 * @param  locale 		locale
	 * @param  url e.g. http://localhost:8080.
	 *
	 * @return 				Textual JSON
	 *						NULL on exception or permission denied by sqlConnectionManager
	 */
	public String getJsonFile(
			final User _user,
			final String studyID,
			final Locale locale,
			final String url)
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

			rifLogger.info(this.getClass(), "Call getJsonFile() using URL: " +
				url);
			result=studyExtractManager.getJsonFile(
					connection,
					user,
					rifStudySubmission,
					studyID,
					locale,
					url);

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

		if (sqlConnectionManager.isUserBlocked(user)) {
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
}
