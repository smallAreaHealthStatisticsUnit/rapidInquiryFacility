package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifGenericLibrary.util.FieldValidationUtility;
import rifServices.businessConceptLayer.StudyState;
import rifServices.dataStorageLayer.pg.PGSQLConnectionManager;
import rifServices.dataStorageLayer.pg.PGSQLSmoothedResultManager;

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

abstract class MSSQLAbstractRIFStudyRetrievalService 
	extends MSSQLAbstractRIFUserService 
	implements RIFStudyResultRetrievalAPI {

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
	public MSSQLAbstractRIFStudyRetrievalService() {

		String serviceName
			= RIFServiceMessages.getMessage("rifStudyRetrievalService.name");
		setServiceName(serviceName);
		String serviceVersion
			= RIFServiceMessages.getMessage("rifStudyRetrievalService.version");
		setServiceVersion(Double.valueOf(serviceVersion));
	
		String serviceDescription
			= RIFServiceMessages.getMessage("rifStudyRetrievalService.description");
		setServiceDescription(serviceDescription);
		String serviceContactEmail
			= RIFServiceMessages.getMessage("rifStudyRetrievalService.contactEmail");
		setServiceContactEmail(serviceContactEmail);		
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	
	public void clearStudyStatusUpdates(
		final User _user, 
		final String studyID)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);

		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
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
			MSSQLStudyStateManager studyStateManager
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

		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		
		RIFResultTable result = null;
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
			MSSQLStudyStateManager studyStateManager
				= rifServiceResources.getStudyStateManager();
			studyStateManager.updateStudyStatus(
				connection, 
				user, 
				studyID, 
				studyState,
				message);
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

		MSSQLConnectionManager sqlConnectionManager
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
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);			

			//Delegate operation to a specialised manager class
			MSSQLStudyStateManager studyStateManager
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
	
	
	/**
	 * Returns a table with the following fields:
	 *    Total denominator in study
	 *    Observed in study
		- Number of areas in study
		- Average observed in study
		- Total expected adj
		- Average expected adj
		- Relative Risk adj
		- Total expected unadj
		- Average expected unadj
		- Relative Risk unadj
	 * 
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getStudyResultGeneralInfo(
		final User _user,
		final StudySummary _studySummary)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		MSSQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudySummary studySummary 
			= StudySummary.createCopy(_studySummary);
		
		RIFResultTable results = new RIFResultTable();
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getResultStudyGeneralInfo",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getResultStudyGeneralInfo",
				"studySummary",
				studySummary);	
	
			//Check for security violations
			validateUser(user);
			studySummary.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getResultStudyGeneralInfo",
					user.getUserID(),
					user.getIPAddress(),
					studySummary.getStudyID());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
						
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			MSSQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getResultStudyGeneralInfo(
					connection,
					user,
					studySummary);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getResultStudyGeneralInfo",
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
			
	public ArrayList<String> getSmoothedResultAttributes(
			final User _user) 
			throws RIFServiceException {
			

			//Defensively copy parameters and guard against blocked users
			User user = User.createCopy(_user);
			MSSQLConnectionManager sqlConnectionManager
				= rifServiceResources.getSqlConnectionManager();
			if (sqlConnectionManager.isUserBlocked(user) == true) {
				return null;
			}

			ArrayList<String> results = new ArrayList<String>();
			Connection connection = null;
			try {
				//Check for empty parameters
				FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
				fieldValidationUtility.checkNullMethodParameter(
					"getSmoothedResultAttributes",
					"user",
					user);
				
				//Check for security violations
				validateUser(user);

				
				//Audit attempt to do operation
				RIFLogger rifLogger = RIFLogger.getLogger();				
				String auditTrailMessage
					= RIFServiceMessages.getMessage("logging.getSmoothedResultAttributes",
						user.getUserID(),
						user.getIPAddress());
				rifLogger.info(
					getClass(),
					auditTrailMessage);

				//Assign pooled connection
				connection
					= sqlConnectionManager.assignPooledReadConnection(user);

				//Delegate operation to a specialised manager class
				MSSQLSmoothedResultManager sqlSmoothedResultQueryManager
					= rifServiceResources.getSQLSmoothedResultManager();
				results
					= sqlSmoothedResultQueryManager.getSmoothedResultAttributes();
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

	
	public RIFResultTable getSmoothedResults(
		final User _user,
		final String studyID,
		final String sex) 
		throws RIFServiceException {
		

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		MSSQLConnectionManager sqlConnectionManager
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
			MSSQLSmoothedResultManager sqlSmoothedResultQueryManager
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

	
	public RIFResultTable getSmoothedResultsForAttributes(
		final User _user,
		final ArrayList<String> smoothedAttributesToInclude,
		final String studyID,
		final String sex) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		MSSQLConnectionManager sqlConnectionManager
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
				
			//Check for security violations
			validateUser(user);

			fieldValidationUtility.checkMaliciousMethodParameter(
				"getSmoothedResults", 
				"studyID", 
				studyID);
			
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
			MSSQLSmoothedResultManager sqlSmoothedResultQueryManager
				= rifServiceResources.getSQLSmoothedResultManager();

			results
				= sqlSmoothedResultQueryManager.getSmoothedResultsForAttributes(
					connection, 
					smoothedAttributesToInclude,
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
		MSSQLConnectionManager sqlConnectionManager
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
			MSSQLSmoothedResultManager sqlSmoothedResultQueryManager
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
		
	public RIFResultTable getPopulationPyramidData(
		final User _user,
		final String studyID,
		final String year,
		final ArrayList<MapArea> mapAreas)
		throws RIFServiceException {
		
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		MSSQLConnectionManager sqlConnectionManager
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
				"getPopulationPyramidDataForAreas",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getPopulationPyramidDataForAreas",
				"studyID",
				studyID);
			fieldValidationUtility.checkNullMethodParameter(
				"getPopulationPyramidDataForAreas",
				"year",
				year);
			
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getPopulationPyramidDataForAreas", 
				"studyID", 
				studyID);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getPopulationPyramidDataForAreas", 
				"year", 
				year);
						
			if (mapAreas != null) {
				
				for (MapArea mapArea : mapAreas) {
					
					fieldValidationUtility.checkMaliciousMethodParameter(
						"getPopulationPyramidDataForAreas", 
						"mapArea", 
						mapArea.getLabel());				
				
				}
			}			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getPopulationPyramidDataForAreas",
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
			MSSQLSmoothedResultManager sqlSmoothedResultQueryManager
				= rifServiceResources.getSQLSmoothedResultManager();

			if (mapAreas == null) {
				results
					= sqlSmoothedResultQueryManager.getPopulationPyramidData(
						connection, 
						studyID,
						Integer.valueOf(year));				
			}
			else {
				results
					= sqlSmoothedResultQueryManager.getPopulationPyramidData(
						connection, 
						studyID,
						Integer.valueOf(year),
						mapAreas);
			}
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getPopulationPyramidData",
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
		MSSQLConnectionManager sqlConnectionManager
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
			MSSQLSmoothedResultManager smoothedResultManager
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
		MSSQLConnectionManager sqlConnectionManager
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
			MSSQLSmoothedResultManager smoothedResultManager
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
		MSSQLConnectionManager sqlConnectionManager
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
			MSSQLSmoothedResultManager smoothedResultManager
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
			MSSQLConnectionManager sqlConnectionManager
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
				MSSQLSmoothedResultManager smoothedResultManager
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
			MSSQLConnectionManager sqlConnectionManager
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
				MSSQLSmoothedResultManager smoothedResultManager
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
		MSSQLConnectionManager sqlConnectionManager
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
			MSSQLSmoothedResultManager sqlSmoothedResultQueryManager
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
	
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	// ==========================================
	// Section Override
	// ==========================================
}
