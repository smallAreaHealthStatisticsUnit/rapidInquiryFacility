package rifServices.dataStorageLayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.MapArea;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.YearRange;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
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

class AbstractRIFUserService extends AbstractRIFService {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
				
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRIFUserService() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
			
	public ArrayList<DiseaseMappingStudy> getDiseaseMappingStudies(
		final User _user) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}

		ArrayList<DiseaseMappingStudy> results
			= new ArrayList<DiseaseMappingStudy>();
		try {
				
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getDiseaseMappingStudies",
				"user",
				user);
		
			//Check for security violations
			validateUser(user);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getDiseaseMappingStudies",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Delegate operation to a specialised manager class

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getDiseaseMappingStudies",
				rifServiceException);
		}
			
		return results;		
	}
	
	public ArrayList<Geography> getGeographies(
		final User _user) 
		throws RIFServiceException {
			
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
			
		ArrayList<Geography> results = new ArrayList<Geography>();
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeographies",
				"user",
				user);
			
			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeographies",
					user.getUserID(),
					user.getIPAddress());
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
				= sqlRIFContextManager.getGeographies(connection);			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeographies",
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
	
		
	public ArrayList<GeoLevelSelect> getGeoLevelSelectValues(
		final User _user,
		final Geography _geography)
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

		ArrayList<GeoLevelSelect> results = new ArrayList<GeoLevelSelect>();
		Connection connection = null;
		try {
			//TOUR_VALIDATION
			/*
			 * Make sure that none of the required parameter values are null.
			 */
			
			//Check for empty parameters
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

			//TOUR_VALIDATION
			/*
			 * Checks that the user is valid
			 */
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeographicalLevelSelectValues",
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
			SQLRIFContextManager sqlRIFContextManager	
				= rifServiceResources.getSQLRIFContextManager();		
			results
				= sqlRIFContextManager.getGeoLevelSelectValues(
					connection, 
					geography);
		
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeographicalLevelSelectValues",
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
	
	
	public GeoLevelSelect getDefaultGeoLevelSelectValue(
		final User _user,
		final Geography _geography) 
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

		GeoLevelSelect result = GeoLevelSelect.newInstance();
		Connection connection = null;
		try {
			
			//Check for empty parameters
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

			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getDefaultGeoLevelSelectValue",
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
			SQLRIFContextManager sqlRIFContextManager	
				= rifServiceResources.getSQLRIFContextManager();		
			result
				= sqlRIFContextManager.getDefaultGeoLevelSelectValue(
					connection, 
					geography);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getDefaultGeoLevelSelectValue",
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

	//TOUR_CONCURRENCY
	/*
	 * We mark all method parameters as "final" to ensure that the code within the method
	 * cannot reassign the parameter within the code block.  It is used to prevent accidentally
	 * doing it as the code block is altered for maintenance.
	 */
	public ArrayList<GeoLevelArea> getGeoLevelAreaValues(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect) 
		throws RIFServiceException {

		//TOUR_CONCURRENCY
		/*
		 * The main task of this code block is to create complete local copies
		 * of method parameter values.  Doing this means that the 
		 * code block is not vulnerable to multiple threads which may attempt to change
		 * the parameter objects provided by the client, as the code block is executing.
		 * 
		 */
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
	
		ArrayList<GeoLevelArea> results = new ArrayList<GeoLevelArea>();
		Connection connection = null;
		try {
			
			//Check for empty parameters
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
		
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeoLevelAreaValues",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//TOUR_CONCURRENCY
			/*
			 * The pool of connections for each user will be a resource that could
			 * be a source of contention between threads.  However, mechanism for 
			 * synchronising access does not have to be exposed in this class.  Instead,
			 * it can be developed and improved within the implementation of 
			 * the SQLConnectionManager class's methods that include:
			 * <ul>
			 * <li>assignPooledReadConnection</li>
			 * <li>reclaimPooledReadConnection</li>
			 * <li>assignPooledWriteConnection</li>
			 * <li>reclaimPooledWriteConnection</li>
			 * </ul>
			 * 
			 * 
			 * <p>
			 * Also note that because we are only reading geo level area values,
			 * we can use the more constraining read-only connection.  Using 
			 * the read-only connection may help improve performance. It also 
			 * acts as a safety check if developers later alter the database query code 
			 * in a way that suddenly causes the code to write values.
			 */
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class				
			SQLRIFContextManager sqlRIFContextManager	
				= rifServiceResources.getSQLRIFContextManager();		
			results
				= sqlRIFContextManager.getGeoLevelAreaValues(
					connection, 
					geography,
					geoLevelSelect);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelAreaValues",
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

	public ArrayList<GeoLevelView> getGeoLevelViewValues(
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
		
		ArrayList<GeoLevelView> results = new ArrayList<GeoLevelView>();
		Connection connection = null;
		try {
			
			//Check for empty parameters
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
		
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeoLevelViewValues",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
			rifLogger.info(
				AbstractRIFUserService.class,
				auditTrailMessage);

			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLRIFContextManager sqlRIFContextManager	
				= rifServiceResources.getSQLRIFContextManager();		
			results
				= sqlRIFContextManager.getGeoLevelViewValues(
					connection, 
					geography,
					geoLevelSelect);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelViewValues",
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
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
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
			 * businessConceptLayer classes.  "checkSecurityViolations()" is meeant to recursively
			 * check through all the String field values and check whether they have patterns
			 * which could match known types of malicious code attacks.
			 */			
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			ndPair.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getYearRange",
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
			SQLAgeGenderYearManager sqlAgeGenderYearManager
				= rifServiceResources.getSqlAgeGenderYearManager();
			result
				= sqlAgeGenderYearManager.getYearRange(
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
	
	
	
	public ArrayList<HealthTheme> getHealthThemes(
		final User _user,
		final Geography _geography)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		
		ArrayList<HealthTheme> results = new ArrayList<HealthTheme>();
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
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getHealthThemes",
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
			SQLRIFContextManager sqlRIFContextManager
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
	
	

	public ArrayList<AbstractCovariate> getCovariates(
		final User _user,
		final Geography _geography,
		final GeoLevelToMap _geoLevelToMap)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Geography geography = Geography.createCopy(_geography);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
	
		ArrayList<AbstractCovariate> results = new ArrayList<AbstractCovariate>();
		Connection connection = null;
		try {
			
			//Check for empty parameters
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
				"geoLevelToMap",
				geoLevelToMap);
						
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getCovariates",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelToMap.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);
			
			System.out.println("AbstractRIFUserService - getCovariates 1 geoLevelToMap=="+geoLevelToMap.getName()+"==");
			//Delegate operation to a specialised manager class		
			SQLCovariateManager sqlCovariateManager
				= rifServiceResources.getSqlCovariateManager();
			results 
				= sqlCovariateManager.getCovariates(
					connection, 
					geography, 
					geoLevelToMap);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getCovariates",
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
	
	public ArrayList<AbstractStudy> getStudies(
		final User _user,
		final Project _project) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		Project project = Project.createCopy(_project);
		
		ArrayList<AbstractStudy> results = new ArrayList<AbstractStudy>();
		Connection connection = null;
		try {
			
			//Check for empty parameters
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
		
			//Check for security violations
			validateUser(user);
			project.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getStudies",
					user.getUserID(),
					user.getIPAddress(),
					project.getDisplayName());
			rifLogger.info(
				AbstractRIFUserService.class,
				auditTrailMessage);
			
			
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);
			
			//Delegate operation to a specialised manager class
			SQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager
				= rifServiceResources.getSqlDiseaseMappingStudyManager();
			results
				= sqlDiseaseMappingStudyManager.getStudies(
					connection,
					user,
					project);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getStudies",
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

	protected String getStudyName(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
					
		SQLSelectQueryFormatter query
			= new SQLSelectQueryFormatter();
		query.addSelectField("study_name");
		query.addFromTable("rif40_studies");
		query.addWhereParameter("study_id");
			
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(query.generateQuery());
			statement.setInt(
				1, 
				Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			if (resultSet.next() == false) {
				String recordType
					= RIFServiceMessages.getMessage("diseaseMappingStudy.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						studyID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_DISEASE_MAPPING_STUDY, 
							errorMessage);
					throw rifServiceException;
			}
				return resultSet.getString(1);
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetStudyName",
					studyID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
			
	}

	
	public String getTiles(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final String tileIdentifier,
		final Integer zoomFactor,
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
		
		String result = "";
		Connection connection = null;
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getTiles",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getTiles",
				"geography",
				geography);	
			fieldValidationUtility.checkNullMethodParameter(
				"getTiles",
				"getLevelSelect",
				geoLevelSelect);	
			fieldValidationUtility.checkNullMethodParameter(
				"getTiles",
				"tileIdentifier",
				tileIdentifier);	
			fieldValidationUtility.checkNullMethodParameter(
				"getTiles",
				"zoomFactor",
				zoomFactor);		
			fieldValidationUtility.checkNullMethodParameter(
				"getTiles",
				"boundaryRectangle",
				boundaryRectangle);	

			//check that zoomFactor
			if ((zoomFactor <1) || (zoomFactor > 20)) {
				//zoom factor is out of range.
				String errorMessage
					= RIFServiceMessages.getMessage(
						"getTiles.zoomFactor.error",
						String.valueOf(zoomFactor));
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.INVALID_ZOOM_FACTOR, 
						errorMessage);
				throw rifServiceException;
			}
			
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();			
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getTiles", 
				"boundaryRectangle", 
				tileIdentifier);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getTiles",
					user.getUserID(),
					user.getIPAddress(),
					boundaryRectangle.getDisplayName(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			result
				= sqlResultsQueryManager.getTiles(
					connection,
					user,
					geography,
					geoLevelSelect,
					tileIdentifier,
					zoomFactor,
					boundaryRectangle);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getTiles",
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
	
	public ArrayList<MapArea> getMapAreasForBoundaryRectangle(
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
		ArrayList<MapArea> results = new ArrayList<MapArea>();
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
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
