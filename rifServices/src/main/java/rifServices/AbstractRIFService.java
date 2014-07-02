package rifServices;

import java.sql.Connection;
import java.util.ArrayList;

import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.Project;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.YearRange;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceSecurityException;
import rifServices.util.FieldValidationUtility;
import rifServices.util.RIFLogger;
import rifServices.dataStorageLayer.SQLConnectionManager;
import rifServices.dataStorageLayer.SQLRIFContextManager;
import rifServices.dataStorageLayer.SQLAgeGenderYearManager;
import rifServices.dataStorageLayer.SQLCovariateManager;
import rifServices.dataStorageLayer.SQLDiseaseMappingStudyManager;


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

class AbstractRIFService {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	protected RIFServiceResources rifServiceResources;
	private boolean isInitialised;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRIFService() {
		isInitialised = false;

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	public void initialise(final Object startupParameter)
		throws RIFServiceException {

		this.rifServiceResources 
			= (RIFServiceResources) startupParameter;
		isInitialised = true;
	}
	/**
	 * Validate user.
	 *
	 * @param user the user
	 * @throws RIFServiceException the RIF service exception
	 */
	public void validateUser(
		final User user) throws RIFServiceException {
				
		user.checkErrors();
		user.checkSecurityViolations();
		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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

	public void logException(
		final User user,
		final String methodName,
		final RIFServiceException rifServiceException) 
		throws RIFServiceException {
		
		boolean userDeregistered = false;
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
			AbstractRIFService.class, 
			methodName, 
			rifServiceException);
	
		throw rifServiceException;
	}

	public ArrayList<DiseaseMappingStudy> getDiseaseMappingStudies(
		final User _user) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
	
	public ArrayList<Geography> getGeographies(
		final User _user) 
		throws RIFServiceException {
			
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
			SQLRIFContextManager sqlRIFContextManager	
				= rifServiceResources.getSQLRIFContextManager();		
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
	
		
	public ArrayList<GeoLevelSelect> getGeographicalLevelSelectValues(
		final User _user,
		final Geography _geography)
		throws RIFServiceException {
			
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
			SQLRIFContextManager sqlRIFContextManager	
				= rifServiceResources.getSQLRIFContextManager();		
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
	
	
	public GeoLevelSelect getDefaultGeoLevelSelectValue(
		final User _user,
		final Geography _geography) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
			SQLRIFContextManager sqlRIFContextManager	
				= rifServiceResources.getSQLRIFContextManager();		
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
	
	public ArrayList<GeoLevelArea> getGeoLevelAreaValues(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
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
				SQLRIFContextManager sqlRIFContextManager	
					= rifServiceResources.getSQLRIFContextManager();		
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

	public ArrayList<GeoLevelView> getGeoLevelViewValues(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
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
			SQLRIFContextManager sqlRIFContextManager	
				= rifServiceResources.getSQLRIFContextManager();		
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
	public YearRange getYearRange(
		final User _user,
		final Geography _geography,
		final NumeratorDenominatorPair _ndPair) 
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
			SQLAgeGenderYearManager sqlAgeGenderYearManager
				= rifServiceResources.getSqlAgeGenderYearManager();
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
	
	
	
	public ArrayList<HealthTheme> getHealthThemes(
		final User _user,
		final Geography _geography)
		throws RIFServiceException {

		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
			SQLRIFContextManager sqlRIFContextManager
				= rifServiceResources.getSQLRIFContextManager();
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
	
	

	public ArrayList<AbstractCovariate> getCovariates(
		final User _user,
		final Geography _geography,
		final GeoLevelSelect _geoLevelSelect,
		final GeoLevelToMap _geoLevelToMap)
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
			Connection connection
				= sqlConnectionManager.getReadConnection(user);
			SQLCovariateManager sqlCovariateManager
				= rifServiceResources.getSqlCovariateManager();
			results 
				= sqlCovariateManager.getCovariates(
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
	
	public ArrayList<AbstractStudy> getStudies(
		final User _user,
		final Project _project) 
		throws RIFServiceException {
		
		//Part I: Defensively copy parameters
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
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
			
			SQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager
				= rifServiceResources.getSqlDiseaseMappingStudyManager();
			results
				= sqlDiseaseMappingStudyManager.getStudies(
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

	public boolean isInitialised() {
		return isInitialised;
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
