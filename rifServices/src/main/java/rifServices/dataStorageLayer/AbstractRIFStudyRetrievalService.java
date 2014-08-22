package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifServices.util.FieldValidationUtility;
import rifServices.util.RIFLogger;

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

abstract class AbstractRIFStudyRetrievalService 
	extends AbstractRIFService 
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
	public AbstractRIFStudyRetrievalService() {

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
	

	
	public String getGeometry(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelView _geoLevelView,
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
		GeoLevelView geoLevelView
			= GeoLevelView.createCopy(_geoLevelView);
		ArrayList<MapArea> mapAreas
			= MapArea.createCopy(_mapAreas);
		
		String result = "";
		try {
			
			//Check for empty parameters
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
				"geoLevelView",
				geoLevelView);						
			fieldValidationUtility.checkNullMethodParameter(
				"getGeometry",
				"mapAreas",
				mapAreas);

			for (MapArea mapArea : mapAreas) {
				fieldValidationUtility.checkNullMethodParameter(
					"getGeometry",
					"mapAreas",
					mapArea);
			}
			
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			geoLevelView.checkSecurityViolations();
			
			for (MapArea mapArea : mapAreas) {
				mapArea.checkSecurityViolations();
			}
			
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeometry",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName(),
					geoLevelView.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);			

			//Delegate operation to a specialised manager class
			SQLMapDataManager sqlMapDataManager
				= rifServiceResources.getSQLMapDataManager();
			result 
				= sqlMapDataManager.getGeometry(
					connection, 
					user, 
					geography, 
					geoLevelSelect,
					geoLevelView, 
					mapAreas);
				
			//Reclaim pooled connection		
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeometry",
				rifServiceException);	
		}
		
		return result;
	}
		
	public ArrayList<MapAreaAttributeValue> getMapAreaAttributeValues(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelAttributeSource _geoLevelAttributeSource,
		final String geoLevelAttribute) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();		
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);
		ArrayList<MapAreaAttributeValue> results
			= new ArrayList<MapAreaAttributeValue>();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaAttributeValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaAttributeValues",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaAttributeValues",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);	
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaAttributeValues",
				"geoLevelAttribute",
				geoLevelAttribute);	
		
			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(			
				"getMapAreaAttributeValues",
				"geoLevelAttribute",
				geoLevelAttribute);	

			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getMapAreaAttributeValues",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName(),
					geoLevelAttribute);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);
			
			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getMapAreaAttributeValues(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute);

			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);		
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getMapAreaAttributeValues",
				rifServiceException);			
		}
		
		return results;
	}
	
	public ArrayList<GeoLevelAttributeSource> getGeoLevelAttributeSources(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}		
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		
		ArrayList<GeoLevelAttributeSource> results 
			= new ArrayList<GeoLevelAttributeSource>();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAttributeSources",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAttributeSources",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);	
			
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAttributeSources",
				"studyID",
				studyResultRetrievalContext.getStudyID());	
			
			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeoLevelAttributeSources",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);
					
			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getGeoLevelAttributeSources(
					connection,
					user,
					studyResultRetrievalContext); 
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);		
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelAttributeSources",
				rifServiceException);			
		}
		return results;
	}
	
	
	public ArrayList<GeoLevelAttributeTheme> getGeoLevelAttributeThemes(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelAttributeSource _geoLevelAttributeSource)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext 
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);
		
		ArrayList<GeoLevelAttributeTheme> results 
			= new ArrayList<GeoLevelAttributeTheme>();
		
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAttributeThemes",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelAttributeThemes",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);	
			fieldValidationUtility.checkNullMethodParameter(
				"getMapAreaAttributeValues",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);	
			
			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeoLevelAttributeThemes",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);			

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();			
			results
				= sqlResultsQueryManager.getGeoLevelAttributeThemes(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelAttributeSource);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelAttributeThemes",
				rifServiceException);						
		}
				
		return results;
	}

	public String[] getAllAttributesForGeoLevelAttributeTheme(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelAttributeSource _geoLevelAttributeSource,
		final GeoLevelAttributeTheme _geoLevelAttributeTheme)
		throws RIFServiceException {
			
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);
		GeoLevelAttributeTheme geoLevelAttributeTheme
			= GeoLevelAttributeTheme.createCopy(_geoLevelAttributeTheme);
			String[] results = new String[0];
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getAllAttributesForGeoLevelAttributeTheme",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getAllAttributesForGeoLevelAttributeTheme",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);
			fieldValidationUtility.checkNullMethodParameter(
				"getAllAttributesForGeoLevelAttributeTheme",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);	
			fieldValidationUtility.checkNullMethodParameter(
				"getAllAttributesForGeoLevelAttributeTheme",
				"geoLevelAttributeTheme",
				geoLevelAttributeTheme);	
			
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();
			geoLevelAttributeTheme.checkSecurityViolations();
						
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getAllAttributesForGeoLevelAttributeTheme",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName(),
					geoLevelAttributeTheme.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//KLG @TODO - where do we get this? Is it passed or does the middleware manage it?
			String attributeArrayName = "attributeNameTheme";
			
			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();			
			results
				= sqlResultsQueryManager.getAllAttributesForGeoLevelAttributeTheme(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelAttributeTheme,
					geoLevelAttributeSource,
					attributeArrayName);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);		

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelAttributeThemes",
				rifServiceException);						
		}
					
		return results;
	}

	
	public String[] getNumericAttributesForGeoLevelAttributeTheme(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelAttributeSource _geoLevelAttributeSource,
		final GeoLevelAttributeTheme _geoLevelAttributeTheme)
		throws RIFServiceException {
				
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);
		GeoLevelAttributeTheme geoLevelAttributeTheme
			= GeoLevelAttributeTheme.createCopy(_geoLevelAttributeTheme);
		String[] results = new String[0];
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getNumericAttributesForGeoLevelAttributeTheme",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getNumericAttributesForGeoLevelAttributeTheme",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);	
			fieldValidationUtility.checkNullMethodParameter(
				"getNumericAttributesForGeoLevelAttributeTheme",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);			
			fieldValidationUtility.checkNullMethodParameter(
				"getNumericAttributesForGeoLevelAttributeTheme",
				"geoLevelAttributeTheme",
				geoLevelAttributeTheme);	
			
			//check security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();
			geoLevelAttributeTheme.checkSecurityViolations();
			
			//Audit attempt to do operation			
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getNumericAttributesForGeoLevelAttributeTheme",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName(),
					geoLevelAttributeTheme.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();				
			results
				= sqlResultsQueryManager.getNumericAttributesForGeoLevelAttributeTheme(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttributeTheme);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getNumericAttributesForGeoLevelAttributeTheme",
				rifServiceException);						
		}
						
		return results;
	}
	
	public RIFResultTable getCalculatedResultsByBlock(
		final User _user,
		final StudySummary _studySummary,
		final String[] calculatedResultColumnFieldNames,
		final Integer startRowIndex,
		final Integer endRowIndex)
		throws RIFServiceException {

		
		RIFResultTable results = new RIFResultTable();
	
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return results;
		}

		StudySummary studySummary
			= StudySummary.createCopy(_studySummary);
		
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCalculatedResultsByBlock",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getCalculatedResultsByBlock",
				"studySummary",
				studySummary);
			fieldValidationUtility.checkNullMethodParameter(
				"getCalculatedResultsByBlock",
				"calculatedResultColumnFieldNames",
				calculatedResultColumnFieldNames);	
			fieldValidationUtility.checkNullMethodParameter(
				"getCalculatedResultsByBlock",
				"endRowIndex",
				endRowIndex);	
			fieldValidationUtility.checkNullMethodParameter(
				"getCalculatedResultsByBlock",
				"startRowIndex",
				startRowIndex);	
			fieldValidationUtility.checkNullMethodParameter(
				"getCalculatedResultsByBlock",
				"endRowIndex",
				endRowIndex);	
			
			for (String calculatedResultColumnFieldName : calculatedResultColumnFieldNames) {
				fieldValidationUtility.checkMaliciousMethodParameter(
					"getCalculatedResultsByBlock", 
					"calculatedResultColumnFieldNames", 
					calculatedResultColumnFieldName);
			}
			
			//Check for security violations
			validateUser(user);
			studySummary.checkSecurityViolations();
				
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();			
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getCalculatedResultsByBlock",
					user.getUserID(),
					user.getIPAddress(),
					studySummary.getStudyID(),
					String.valueOf(startRowIndex),
					String.valueOf(endRowIndex));
			rifLogger.info(
				getClass(),
				auditTrailMessage);
	
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);
			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getCalculatedResultsByBlock(
					connection,
					user,
					studySummary,
					calculatedResultColumnFieldNames,
					startRowIndex,
					endRowIndex);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getCalculatedResultsByBlock",
				rifServiceException);	
		}	
		
		return results;
	}
	
	public RIFResultTable getExtractResultsByBlock(
		final User _user,
		final StudySummary _studySummary,
		final String[] extractResultColumnFieldNames,
		final Integer startRowIndex,
		final Integer endRowIndex)
		throws RIFServiceException {

			
		RIFResultTable results = new RIFResultTable();
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return results;
		}
		
		StudySummary studySummary = StudySummary.createCopy(_studySummary);

		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();			
			fieldValidationUtility.checkNullMethodParameter(
				"getExtractByBlock",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getExtractByBlock",
				"studySummary",
				studySummary);	
			fieldValidationUtility.checkNullMethodParameter(
				"getExtractByBlock",
				"extractResultColumnFieldNames",
				extractResultColumnFieldNames);	
			fieldValidationUtility.checkNullMethodParameter(
				"getExtractByBlock",
				"startRowIndex",
				startRowIndex);	
			fieldValidationUtility.checkNullMethodParameter(
				"getExtractByBlock",
				"endRowIndex",
				endRowIndex);	

			//Check for security violations
			validateUser(user);
			studySummary.checkSecurityViolations();
					
			for (String extractResultColumnFieldName : extractResultColumnFieldNames) {
				fieldValidationUtility.checkMaliciousMethodParameter(
					"getExtractByBlock", 
					"extractResultColumnFieldNames", 
					extractResultColumnFieldName);
			}
						
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();			
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getExtractByBlock",
					user.getUserID(),
					user.getIPAddress(),
					studySummary.getStudyID(),
					String.valueOf(startRowIndex),
					String.valueOf(endRowIndex));
			rifLogger.info(
				getClass(),
				auditTrailMessage);
		
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);
			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getExtractResultsByBlock(
					connection,
					user,
					studySummary,
					extractResultColumnFieldNames,
					startRowIndex,
					endRowIndex);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		catch(RIFServiceException rifServiceException) {	
			//Audit failure of operation
			logException(
				user,
				"getExtractByBlock",
				rifServiceException);	
		}	
			
		return results;
	}
			
	public RIFResultTable getResultsStratifiedByGenderAndAgeGroup(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelToMap _geoLevelToMap,
		final GeoLevelAttributeSource _geoLevelAttributeSource,
		final String geoLevelSourceAttribute,
		final ArrayList<MapArea> _mapAreas,
		final Integer year)
		throws RIFServiceException {
		
		
		RIFResultTable results = new RIFResultTable();
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return results;
		}
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);
		ArrayList<MapArea> mapAreas = MapArea.createCopy(_mapAreas);
		
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();			
			fieldValidationUtility.checkNullMethodParameter(
				"getResultsStratifiedByGenderAndAgeGroup",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getResultsStratifiedByGenderAndAgeGroup",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);	
			fieldValidationUtility.checkNullMethodParameter(
				"getResultsStratifiedByGenderAndAgeGroup",
				"geoLevelToMap",
				geoLevelToMap);	
			fieldValidationUtility.checkNullMethodParameter(
				"getResultsStratifiedByGenderAndAgeGroup",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);
			fieldValidationUtility.checkNullMethodParameter(
				"getResultsStratifiedByGenderAndAgeGroup",
				"geoLevelSourceAttribute",
				geoLevelSourceAttribute);	
			fieldValidationUtility.checkNullMethodParameter(
				"getResultsStratifiedByGenderAndAgeGroup",
				"mapAreas",
				mapAreas);	

			for (MapArea mapArea : mapAreas) {
				fieldValidationUtility.checkNullMethodParameter(
					"getResultsStratifiedByGenderAndAgeGroup",
					"mapAreas",
					mapArea);
			}
			
			fieldValidationUtility.checkNullMethodParameter(
				"getResultsStratifiedByGenderAndAgeGroup",
				"year",
				year);	
						
			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getResultsStratifiedByGenderAndAgeGroup", 
				"geoLevelAttribute", 
				geoLevelSourceAttribute);
			
			if (mapAreas != null) {
				for (MapArea mapArea : mapAreas) {
					mapArea.checkSecurityViolations();
				}
			}
			
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();			
			String auditTrailMessage
				= RIFServiceMessages.getMessage(
					"logging.getResultsStratifiedByGenderAndAgeGroup",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName(),
					geoLevelSourceAttribute);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getResultsStratifiedByGenderAndAgeGroup(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelToMap,
					geoLevelAttributeSource,
					geoLevelSourceAttribute,
					mapAreas,
					year);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getResultsStratifiedByGenderAndAgeGroup",
				rifServiceException);	
			
		}
		
		return results;
	}

	public BoundaryRectangle getGeoLevelBoundsForArea(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final MapArea _mapArea)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		MapArea mapArea = MapArea.createCopy(_mapArea);
				
		BoundaryRectangle result
			= BoundaryRectangle.newInstance();
		try {			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelBoundsForArea",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelBoundsForArea",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelBoundsForArea",
				"mapArea",
				mapArea);		
		
			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			mapArea.checkSecurityViolations();
			
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeoLevelBoundsForArea",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					mapArea.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();		
			result
				= sqlResultsQueryManager.getGeoLevelBoundsForArea(
					connection,
					user,
					studyResultRetrievalContext,
					mapArea);
						
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelBoundsForArea",
				rifServiceException);	
		}		
		
		return result;
	}
		
	public BoundaryRectangle getGeoLevelFullExtentForStudy(
		final User _user,		
		final StudyResultRetrievalContext _studyResultRetrievalContext)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
				
		BoundaryRectangle result = BoundaryRectangle.newInstance();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelFullExtentForStudy",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelFullExtentForStudy",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);

			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeoLevelFullExtentForStudy",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);
			
			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();		
			result
				= sqlResultsQueryManager.getGeoLevelFullExtentForStudy(
					connection,
					user,
					studyResultRetrievalContext);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelFullExtentForStudy",
				rifServiceException);			
		}
		return result;
	}
	
	public BoundaryRectangle getGeoLevelFullExtent(
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
		Geography geography = Geography.createCopy(_geography);
		GeoLevelSelect geoLevelSelect = GeoLevelSelect.createCopy(_geoLevelSelect);
			
		BoundaryRectangle result = BoundaryRectangle.newInstance();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelFullExtent",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelFullExtent",
				"geography",
				geography);	
			fieldValidationUtility.checkNullMethodParameter(
				"getGeoLevelFullExtent",
				"geoLevelSelect",
				geoLevelSelect);
				
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
				
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getGeoLevelFullExtent",
					user.getUserID(),
					user.getIPAddress(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();			
			result
				= sqlResultsQueryManager.getGeoLevelFullExtent(
					connection,
					user,
					geography,
					geoLevelSelect);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getGeoLevelFullExtent",
				rifServiceException);			
		}
		return result;
	}
	
	public String getTiles(
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
		
		String result = "";
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
				"boundaryRectangle",
				boundaryRectangle);	

			
			//Check for security violations
			validateUser(user);
			geography.checkSecurityViolations();
			geoLevelSelect.checkSecurityViolations();
			
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
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
			Connection connection
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
					boundaryRectangle);

			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getTiles",
				rifServiceException);			
		}
		return result;
		
	}	

	/**
	 * returns data stratified by age group
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param geoLevelSource
	 * @param geoLevelAttribute
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getPyramidData(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelAttributeSource _geoLevelAttributeSource,
		final String geoLevelAttribute) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);

		RIFResultTable results = new RIFResultTable();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidData",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidData",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);	
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidData",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);	
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidData",
				"geoLevelAttribute",
				geoLevelAttribute);	

			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getPyramidData", 
				"geoLevelAttribute", 
				geoLevelAttribute);

			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getPyramidData",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName(),
					geoLevelAttribute);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			System.out.println("About to get pooled connection for user=="+user.getUserID()+"==");
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);
			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();			
			results
				= sqlResultsQueryManager.getPyramidData(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelAttribute);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getPyramidData",
				rifServiceException);			
		}
		
		return results;
	}
	
	public RIFResultTable getPyramidDataByYear(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelAttributeSource _geoLevelSource,
		final String geoLevelAttribute,
		final Integer year) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext 
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelAttributeSource geoLevelSource
			= GeoLevelAttributeSource.createCopy(_geoLevelSource);
		
		RIFResultTable results = new RIFResultTable();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByYear",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByYear",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByYear",
				"geoLevelSource",
				geoLevelSource);
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByYear",
				"geoLevelAttribute",
				geoLevelAttribute);
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByYear",
				"year",
				year);

			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelSource.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getPyramidDataByYear", 
				"geoLevelAttribute", 
				geoLevelAttribute);			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getPyramidDataByYear",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelSource.getDisplayName(),
					geoLevelAttribute,
					String.valueOf(year));
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();			
			results
				= sqlResultsQueryManager.getPyramidDataByYear(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelSource,
					geoLevelAttribute,
					year);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getPyramidDataByYear",
				rifServiceException);
		}

		return results;		
	}
		
	/**
	 * returns a table with these fields:
	 * eg:
	 * agegroup     | sex    popcount
	 * @param user
	 * @param geography
	 * @param geoLevelSelect
	 * @param geoLevelSource
	 * @param geoLevelAttribute
	 * @param mapAreas
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getPyramidDataByMapAreas(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelToMap _geoLevelToMap,
		final GeoLevelAttributeSource _geoLevelAttributeSource,
		final String geoLevelAttribute,
		final ArrayList<MapArea> _mapAreas) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext 
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelToMap geoLevelToMap
			= GeoLevelToMap.createCopy(_geoLevelToMap);
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);
		ArrayList<MapArea> mapAreas
			= MapArea.createCopy(_mapAreas);
				
		RIFResultTable results = new RIFResultTable();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByMapAreas",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByMapAreas",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);	
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByMapAreas",
				"geoLevelToMap",
				geoLevelToMap);				
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByMapAreas",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByMapAreas",
				"geoLevelAttribute",
				geoLevelAttribute);
			fieldValidationUtility.checkNullMethodParameter(
				"getPyramidDataByMapAreas",
				"mapAreas",
				mapAreas);

			//check for any null map areas
			for (MapArea mapArea : mapAreas) {
				fieldValidationUtility.checkNullMethodParameter(
					"getPyramidDataByMapAreas",
					"mapAreas",
					mapArea);
			}

			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelToMap.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getPyramidDataByMapAreas", 
				"geoLevelAttribute", 
				geoLevelAttribute);			

			for (MapArea mapArea : mapAreas) {
				mapArea.checkSecurityViolations();
			}

			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getPyramidDataByMapAreas",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					studyResultRetrievalContext.getGeoLevelSelectName(),
					geoLevelAttributeSource.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);
			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getPyramidDataByMapAreas(
					connection,
					user,
					studyResultRetrievalContext,	
					geoLevelToMap,
					geoLevelAttributeSource,
					geoLevelAttribute,
					mapAreas);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getPyramidDataByMapAreas",
				rifServiceException);
		}

		return results;			
	}
	
	public String[] getResultFieldsStratifiedByAgeGroup(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelAttributeSource _geoLevelAttributeSource)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext 
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);		
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);

		
		String[] results = new String[0];
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getResultFieldsStratifiedByAgeGroup",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getResultFieldsStratifiedByAgeGroup",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);	
			fieldValidationUtility.checkNullMethodParameter(
				"getResultFieldsStratifiedByAgeGroup",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);			
			
			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getResultFieldsStratifiedByAgeGroup",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getResultFieldsStratifiedByAgeGroup(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelAttributeSource);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getResultFieldsStratifiedByAgeGroup",
				rifServiceException);
		}

		return results;		
	}
	

	/**
	 * returns field with the following headers:
	 * GID, SMR, CL, CU
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 */
	public RIFResultTable getSMRValues(
		final User _user,
		final StudySummary _studySummary)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudySummary studySummary
			= StudySummary.createCopy(_studySummary);

		RIFResultTable results = new RIFResultTable();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getSMRValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getSMRValues",
				"studySummary",
				studySummary);	
			
			//Check for security violations
			validateUser(user);
			studySummary.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getSMRValues",
					user.getUserID(),
					user.getIPAddress(),
					studySummary.getStudyID());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getSMRValues(
					connection,
					user,
					studySummary);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getSMRValues",
				rifServiceException);
		}

		return results;		
	}


	/**
	 * obtains RR (unsmoothed - adjusted) and its confidence intervals for the
	 * study area
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getRRValues(
		final User _user,
		final StudySummary _studySummary)
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudySummary studySummary
			= StudySummary.createCopy(_studySummary);
		
		RIFResultTable results = new RIFResultTable();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getRRValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getRRValues",
				"studySummary",
				studySummary);	
			
			//Check for security violations
			validateUser(user);
			studySummary.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getRRValues",
					user.getUserID(),
					user.getIPAddress(),
					studySummary.getStudyID());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getRRValues(
					connection,
					user,
					studySummary);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getRRValues",
				rifServiceException);
		}

		return results;		
	}
	
	/**
	 * returns a table with the following columns
	 * GID  |  RR_unadj  | CL  | CU
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	public RIFResultTable getRRUnadjustedValues(
		final User _user,
		final StudySummary _studySummary)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		StudySummary studySummary
			= StudySummary.createCopy(_studySummary);
		
		RIFResultTable results = new RIFResultTable();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getRRUnadjustedValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getRRUnadjustedValues",
				"studySummary",
				studySummary);	
			
			//Check for security violations
			validateUser(user);
			studySummary.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getRRUnadjustedValues",
					user.getUserID(),
					user.getIPAddress(),
					studySummary.getStudyID());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getRRUnadjustedValues(
					connection,
					user,
					studySummary);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getRRUnadjustedValues",
				rifServiceException);
		}

		return results;		
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
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudySummary studySummary 
			= StudySummary.createCopy(_studySummary);
		
		RIFResultTable results = new RIFResultTable();
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
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getResultStudyGeneralInfo",
					user.getUserID(),
					user.getIPAddress(),
					studySummary.getStudyID());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
						
			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();
			results
				= sqlResultsQueryManager.getResultStudyGeneralInfo(
					connection,
					user,
					studySummary);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);

		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getResultStudyGeneralInfo",
				rifServiceException);
		}

		return results;		
		
	}
	
	public ArrayList<AgeGroup> getResultAgeGroups(
		final User _user,
		final StudyResultRetrievalContext _studyResultRetrievalContext,
		final GeoLevelAttributeSource _geoLevelAttributeSource,
		final String geoLevelSourceAttribute)
		throws RIFServiceException {
		

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		StudyResultRetrievalContext studyResultRetrievalContext
			= StudyResultRetrievalContext.createCopy(_studyResultRetrievalContext);
		GeoLevelAttributeSource geoLevelAttributeSource
			= GeoLevelAttributeSource.createCopy(_geoLevelAttributeSource);
		
		ArrayList<AgeGroup> results = new ArrayList<AgeGroup>();
		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getResultAgeGroups",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getResultAgeGroups",
				"studyResultRetrievalContext",
				studyResultRetrievalContext);
			fieldValidationUtility.checkNullMethodParameter(
				"getResultAgeGroups",
				"geoLevelAttributeSource",
				geoLevelAttributeSource);
			fieldValidationUtility.checkNullMethodParameter(
				"getResultAgeGroups",
				"geoLevelSourceAttribute",
				geoLevelSourceAttribute);
				
			//Check for security violations
			validateUser(user);
			studyResultRetrievalContext.checkSecurityViolations();
			geoLevelAttributeSource.checkSecurityViolations();
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getResultAgeGroups", 
				"geoLevalAttribute", 
				geoLevelSourceAttribute);

			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = new RIFLogger();				
			String auditTrailMessage
				= RIFServiceMessages.getMessage("logging.getResultAgeGroups",
					user.getUserID(),
					user.getIPAddress(),
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName(),
					geoLevelSourceAttribute);
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			//Delegate operation to a specialised manager class
			SQLResultsQueryManager sqlResultsQueryManager
				= rifServiceResources.getSqlResultsQueryManager();			
			results
				= sqlResultsQueryManager.getResultAgeGroups(
					connection,
					user,
					studyResultRetrievalContext,
					geoLevelAttributeSource,
					geoLevelSourceAttribute);
			
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getResultAgeGroups",
				rifServiceException);			
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
