package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.RIFStudyResultRetrievalAPI;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.concepts.StudyState;
import org.sahsu.rif.services.system.RIFServiceMessages;

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
 *for text that could be used as part of a malicious code attack.  In this step, the
 * <code>checkSecurityViolations(..)</code>
 *methods for business objects are called.  Note that the <code>checkSecurityViolations</code> method of a business
 *object will scan each text field for malicious field values, and recursively call the same method in any child
 *business objects it may possess.
 *</li>
 *<li>
 * Obtain a connection object from the
 * {@link rifServices.dataStorageLayer.common.SQLManager}.
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
 *log any attempts to do so.  If the method throws a {@link RIFServiceSecurityException},
 *then this class will log it and continue to pass it to client application.
 */
public abstract class StudyRetrievalService extends CommonUserService
		implements RIFStudyResultRetrievalAPI {

	/**
	 * Instantiates a new production rif job submission service.
	 */
	protected StudyRetrievalService() {

		String serviceName
			= RIFServiceMessages.getMessage("rifStudyRetrievalService.name");
		setServiceName(serviceName);

		String serviceDescription
			= RIFServiceMessages.getMessage("rifStudyRetrievalService.description");
		setServiceDescription(serviceDescription);
		String serviceContactEmail
			= RIFServiceMessages.getMessage("rifStudyRetrievalService.contactEmail");
		setServiceContactEmail(serviceContactEmail);		
	}

	public void clearStudyStatusUpdates(
		final User _user, 
		final String studyID)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);

		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return;
		}
		
		Connection connection = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"clearStudyStatusUpdates",
				"user",
				user);
			
			//Check for security violations
			validateUser(user);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.clearStudyStatusUpdates",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledWriteConnection(user);

			//Delegate operation to a specialised manager class
			StudyStateManager studyStateManager
				= rifServiceResources.getStudyStateManager();
			studyStateManager.clearStudyStatusUpdates(
				connection, 
				user, 
				studyID);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"clearStudyStatusUpdates",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
		}
	}
	
	public void updateStudyStatus(
		final User _user, 
		final String studyID, 
		final StudyState studyState,
		final String message) 
		throws RIFServiceException {
				
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);

		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user)) {
			return;
		}
		
		Connection connection = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"updateStudyStatus",
				"user",
				user);
			
			//Check for security violations
			validateUser(user);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.updateStudyStatus",
					user.getUserID(),
					user.getIPAddress(),
					studyID,
					user.getUserID());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledWriteConnection(user);

			//Delegate operation to a specialised manager class
			StudyStateManager studyStateManager
				= rifServiceResources.getStudyStateManager();
			studyStateManager.updateStudyStatus(
				connection, 
				user, 
				studyID, 
				studyState,
				message,
				null);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"updateStudyStatus",
				rifServiceException);	
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
		}
	}
	
	public RIFResultTable getCurrentStatusAllStudies(final User _user)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);

		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		RIFResultTable result = null;
		Connection connection = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCurrentStatusAllStudies",
				"user",
				user);
			
			//Check for security violations
			validateUser(user);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getCurrentStatusAllStudies",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.debug(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);			

			//Delegate operation to a specialised manager class
			StudyStateManager studyStateManager
				= rifServiceResources.getStudyStateManager();
			result 
				= studyStateManager.getCurrentStatusAllStudies(
					connection, 
					user);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getCurrentStatusAllStudies",
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
	
				
	
	public RIFResultTable getSmoothedResults(
		final User _user,
		final String studyID,
		final String sex) 
		throws RIFServiceException {
		

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		RIFResultTable results = new RIFResultTable();
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getSmoothedResults",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getSmoothedResults",
				"studyID",
				studyID);
			fieldValidationUtility.checkNullMethodParameter(
				"getSmoothedResults",
				"sex",
				sex);
				
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getSmoothedResults", 
				"studyID", 
				studyID);
			fieldValidationUtility.checkNullMethodParameter(
				"getSmoothedResults",
				"sex",
				sex);
	
			fieldValidationUtility.checkValidIntegerMethodParameterValue(
				"getSmoothedResults", 
				"sex", 
				sex);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getSmoothedResults",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SmoothedResultManager sqlSmoothedResultQueryManager
				= rifServiceResources.getSQLSmoothedResultManager();
			
			results
				= sqlSmoothedResultQueryManager.getSmoothedResults(
					connection,
					studyID,
					Sex.getSexFromCode(Integer.valueOf(sex)));
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getSmoothedResults",
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

	public RIFResultTable getPopulationPyramidData(
		final User _user,
		final String studyID,
		final String year)
		throws RIFServiceException {
		
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		RIFResultTable results = new RIFResultTable();
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getAllPopulationPyramidData",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getAllPopulationPyramidData",
				"studyID",
				studyID);
			fieldValidationUtility.checkNullMethodParameter(
				"getAllPopulationPyramidData",
				"year",
				year);
			
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getAllPopulationPyramidData", 
				"studyID", 
				studyID);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getAllPopulationPyramidData", 
				"year", 
				year);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getAllPopulationPyramidData",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SmoothedResultManager sqlSmoothedResultQueryManager
				= rifServiceResources.getSQLSmoothedResultManager();

			results
				= sqlSmoothedResultQueryManager.getPopulationPyramidData(
					connection, 
					studyID,
					Integer.valueOf(year));
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getAllPopulationPyramidData",
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
			
	public ArrayList<Integer> getYearsForStudy(
		final User _user, 
		final String studyID)
		throws RIFServiceException {
		
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
	
		ArrayList<Integer> results = new ArrayList<Integer>();
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getYearsForStudy",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getYearsForStudy",
				"studyID",
				studyID);
		
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getYearsForStudy", 
				"studyID", 
				studyID);
								
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getYearsForStudy",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
			getClass(),
			auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class			
			SmoothedResultManager smoothedResultManager
				= rifServiceResources.getSQLSmoothedResultManager();
			results
				= smoothedResultManager.getYears(
					connection, 
					studyID);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getYearsForStudy",
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
		
	public ArrayList<Sex> getSexesForStudy(
		final User _user,
		final String studyID)
		throws RIFServiceException {
			
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		ArrayList<Sex> results = new ArrayList<Sex>();
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getSexesForStudy",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getSexesForStudy",
				"studyID",
				studyID);
			
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getSexesForStudy", 
				"studyID", 
				studyID);
									
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getSexesForStudy",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class			
			SmoothedResultManager smoothedResultManager
				= rifServiceResources.getSQLSmoothedResultManager();
			results
				= smoothedResultManager.getSexes(
					connection, 
					studyID);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getSexesForStudy",
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
		
	public String[] getGeographyAndLevelForStudy(
		final User _user,
		final String studyID)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		String[] results = null;
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographyAndLevelForStudy",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographyAndLevelForStudy",
				"studyID",
				studyID);
			
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getGeographyAndLevelForStudy", 
				"studyID", 
				studyID);
									
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeographyAndLevelForStudy",
					user.getUserID(),
					user.getIPAddress(),
					studyID);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
	
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);
	
			//Delegate operation to a specialised manager class			
			SmoothedResultManager smoothedResultManager
				= rifServiceResources.getSQLSmoothedResultManager();
			results
				= smoothedResultManager.getGeographyAndLevelForStudy(connection, studyID);			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeographyAndLevelForStudy",
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
	
	public String[] getDetailsForProcessedStudy(
			final User _user,
			final String studyID)
			throws RIFServiceException {
			
			//Defensively copy parameters and guard against blocked users
			User user = User.createCopy(_user);
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			if (sqlConnectionManager.isUserBlocked(user) == true) {
				return null;
			}
			
			String[] results = null;
			Connection connection = null;
			try {
				//Check for empty parameters
				FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
				fieldValidationUtility.checkNullMethodParameter(
					"getDetailsForProcessedStudy",
					"user",
					user);
				fieldValidationUtility.checkNullMethodParameter(
					"getDetailsForProcessedStudy",
					"studyID",
					studyID);
				
				//Check for security violations
				validateUser(user);
				fieldValidationUtility.checkMaliciousMethodParameter(
					"getDetailsForProcessedStudy", 
					"studyID", 
					studyID);
										
				//Audit attempt to do operation
				RIFLogger rifLogger = RIFLogger.getLogger();				
				String auditTrailMessage
					= RIFServiceMessages.getMessage("logging.getDetailsForProcessedStudy",
						user.getUserID(),
						user.getIPAddress(),
						studyID);
				rifLogger.info(
					getClass(),
					auditTrailMessage);
		
				//Assign pooled connection
				connection
					= sqlConnectionManager.assignPooledReadConnection(user);
		
				//Delegate operation to a specialised manager class			
				SmoothedResultManager smoothedResultManager
					= rifServiceResources.getSQLSmoothedResultManager();
				results
					= smoothedResultManager.getDetailsForProcessedStudy(connection, studyID);			
			}
			catch(RIFServiceException rifServiceException) {
				//Audit failure of operation
				logException(
					user,
					"getDetailsForProcessedStudy",
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
	
	public String[] getHealthCodesForProcessedStudy(
			final User _user,
			final String studyID)
			throws RIFServiceException {
			
			//Defensively copy parameters and guard against blocked users
			User user = User.createCopy(_user);
			SQLManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			if (sqlConnectionManager.isUserBlocked(user) == true) {
				return null;
			}
		
			String[] results = null;
			Connection connection = null;
			try {
				//Check for empty parameters
				FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
				fieldValidationUtility.checkNullMethodParameter(
					"getHealthCodesForProcessedStudy",
					"user",
					user);
				fieldValidationUtility.checkNullMethodParameter(
					"getHealthCodesForProcessedStudy",
					"studyID",
					studyID);
				
				//Check for security violations
				validateUser(user);
				fieldValidationUtility.checkMaliciousMethodParameter(
					"getHealthCodesForProcessedStudy", 
					"studyID", 
					studyID);
										
				//Audit attempt to do operation
				RIFLogger rifLogger = RIFLogger.getLogger();				
				String auditTrailMessage
					= RIFServiceMessages.getMessage("logging.getHealthCodesForProcessedStudy",
						user.getUserID(),
						user.getIPAddress(),
						studyID);
				rifLogger.info(
					getClass(),
					auditTrailMessage);
		
				//Assign pooled connection
				connection
					= sqlConnectionManager.assignPooledReadConnection(user);
		
				//Delegate operation to a specialised manager class			
				SmoothedResultManager smoothedResultManager
					= rifServiceResources.getSQLSmoothedResultManager();
				results
					= smoothedResultManager.getHealthCodesForProcessedStudy(connection, studyID);			
			}
			catch(RIFServiceException rifServiceException) {
				//Audit failure of operation
				logException(
					user,
					"getHealthCodesForProcessedStudy",
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

	public RIFResultTable getStudyTableForProcessedStudy(
			final User _user,
			final String studyID,
			final String type,
			final String stt,
			final String stp)
					throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
		= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		RIFResultTable results = new RIFResultTable();
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
					"getStudyTableForProcessedStudy",
					"user",
					user);
			fieldValidationUtility.checkNullMethodParameter(
					"getStudyTableForProcessedStudy",
					"studyID",
					studyID);
			fieldValidationUtility.checkNullMethodParameter(
					"getStudyTableForProcessedStudy",
					"type",
					type);
			fieldValidationUtility.checkNullMethodParameter(
					"getStudyTableForProcessedStudy",
					"stt",
					stt);
			fieldValidationUtility.checkNullMethodParameter(
					"getStudyTableForProcessedStudy",
					"stp",
					stt);

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getStudyTableForProcessedStudy", 
					"studyID", 
					studyID);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getStudyTableForProcessedStudy", 
					"type", 
					type);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getStudyTableForProcessedStudy", 
					"stt", 
					stt);
			fieldValidationUtility.checkMaliciousMethodParameter(
					"getStudyTableForProcessedStudy", 
					"stp", 
					stp);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
			= RIFServiceMessages.getMessage("logging.getStudyTableForProcessedStudy",
					user.getUserID(),
					user.getIPAddress(),
					studyID, type, stt, stp);
			rifLogger.info(
					getClass(),
					auditTrailMessage);

			//Assign pooled connection
			connection
			= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SmoothedResultManager sqlSmoothedResultQueryManager
			= rifServiceResources.getSQLSmoothedResultManager();

			results
			= sqlSmoothedResultQueryManager.getStudyTableForProcessedStudy(
					connection, 
					studyID,
					type,
					stt,
					stp);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
					user,
					"getStudyTableForProcessedStudy",
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
}
