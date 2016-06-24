package rifServices.dataStorageLayer;


import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractCovariate;


import rifServices.businessConceptLayer.AdjustableCovariate;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.ExposureCovariate;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.ontologyServices.HealthCodeProviderInterface;
import rifGenericLibrary.util.FieldValidationUtility;

import java.sql.Connection;
import java.util.ArrayList;

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

public final class TestRIFStudySubmissionService 
	extends AbstractRIFStudySubmissionService {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public TestRIFStudySubmissionService() {

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

	// ==========================================
	// Section Override
	// ==========================================
	
	/**
	 * Test method used to ensure that service can identify age groups that
	 * don't exist in the age groups providers
	 * @param user
	 * @param connection
	 * @param healthCodes
	 * @throws RIFServiceException
	 */
	public void checkNonExistentAgeGroups(
		final User _user,
		final NumeratorDenominatorPair ndPair,
		final ArrayList<AgeBand> _ageBands) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		ArrayList<AgeBand> ageBands
			= AgeBand.createCopy(_ageBands);
			
		//no need to defensively copy sortingOrder because
		//it is an enumerated type
			
		try {			
			//Check for empty parameters
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
			
			//Check for security violations
			validateUser(user);
			for (AgeBand ageBand : ageBands) {
				ageBand.checkSecurityViolations();					
			}

			//Delegate operation to a specialised manager class
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);
			SQLAgeGenderYearManager sqlAgeGenderYearManager
				= rifServiceResources.getSqlAgeGenderYearManager();
			sqlAgeGenderYearManager.checkNonExistentAgeGroups(
				connection,
				ndPair,
				ageBands); 
			
			sqlConnectionManager.reclaimPooledReadConnection(
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
	public void checkNonExistentCovariates(
		final User _user,
		final Geography _geography,
		final GeoLevelToMap _geoLevelToMap,
		final ArrayList<AbstractCovariate> _covariates) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();						
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		Geography geography = Geography.createCopy(_geography);
		GeoLevelToMap geoLevelToMap = GeoLevelToMap.createCopy(_geoLevelToMap);
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
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"checkNonExistentCovariates",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"checkNonExistentCovariates",
				"geography",
				geography);
			fieldValidationUtility.checkNullMethodParameter(
				"checkNonExistentCovariates",
				"geoLevelToMap",
				geoLevelToMap);			
			fieldValidationUtility.checkNullMethodParameter(
				"checkNonExistentCovariates",
				"covariates",
				covariates);

			//sortingOrder can be null, it just means that the
			//order will be ascending lower limit
			
			//Check for security violations
			validateUser(user);
			for (AbstractCovariate covariate : covariates) {
				covariate.checkSecurityViolations();					
			}

			//Delegate operation to a specialised manager class
			Connection connection
				= sqlConnectionManager.assignPooledReadConnection(user);

			SQLCovariateManager sqlCovariateManager
				= rifServiceResources.getSqlCovariateManager();
			sqlCovariateManager.checkNonExistentCovariates(
				connection,
				geography,
				geoLevelToMap,
				covariates); 		

			sqlConnectionManager.reclaimPooledReadConnection(
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
	 * Adds the health code provider.
	 *
	 * @param _adminUser the _admin user
	 * @param healthCodeProvider the health code provider
	 * @throws RIFServiceException the RIF service exception
	 */
	public void addHealthCodeProvider(
		final User _adminUser,
		final HealthCodeProviderInterface healthCodeProvider) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User adminUser = User.createCopy(_adminUser);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(adminUser) == true) {
			return;
		}
		
		//Part II: Check for security violations
		try {
			validateUser(adminUser);
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();
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
	public void clearHealthCodeProviders(
		final User _adminUser) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User adminUser = User.createCopy(_adminUser);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(adminUser) == true) {
			return;
		}
		
		try {
			//Part II: Check for security violations
			validateUser(adminUser);
			HealthOutcomeManager healthOutcomeManager
				= rifServiceResources.getHealthOutcomeManager();			
			healthOutcomeManager.clearHealthCodeProviders();		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				adminUser,
				"clearHealthCodeProviders",
				rifServiceException);
		}
	}
	
	public void deleteStudy(
		final User _user,
		final String studyID)
		throws RIFServiceException {
		

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"deleteStudy",
				"user",
				user);	
			fieldValidationUtility.checkNullMethodParameter(
					"deleteStudy",
					"studyID",
					studyID);	

			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"deleteStudy", 
				"studyID", 
				studyID);
					
			//Delegate operation to a specialised manager class
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);

			SQLRIFSubmissionManager sqlRIFSubmissionManager
				= rifServiceResources.getRIFSubmissionManager();
			sqlRIFSubmissionManager.deleteStudy(connection, studyID);

			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);		
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"deleteStudy",
				rifServiceException);	
		}		
	}
	
	public void clearRIFJobSubmissionsForUser(
		final User _user) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"clearRIFJobSubmissions",
				"adminUser",
				user);	

			//Check for security violations
			validateUser(user);

			//Delegate operation to a specialised manager class
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);

			SQLRIFSubmissionManager sqlRIFSubmissionManager
				= rifServiceResources.getRIFSubmissionManager();
			sqlRIFSubmissionManager.clearRIFJobSubmissionsForUser(connection, user);

			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);		
		
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"clearRIFJobSubmissions",
				rifServiceException);	
		}
		
	}

	
	public void clearStudiesForUser(
		final User _user)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"clearStudiesForUser",
				"user",
				user);	

			//Check for security violations
			validateUser(user);

			//Delegate operation to a specialised manager class
			Connection connection 
				= sqlConnectionManager.assignPooledReadConnection(user);
			SQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager
				= rifServiceResources.getSqlDiseaseMappingStudyManager();
			sqlDiseaseMappingStudyManager.clearStudiesForUser(
				connection, 
				user);
			
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"clearRIFJobSubmissions",
				rifServiceException);	
		}		
	}
	
	public RIFStudySubmission getRIFStudySubmission(
		final User _user,
		final String studyID)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		
		RIFStudySubmission result = null;
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return result;
		}
		
		Connection connection = null;
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getRIFStudySubmission",
				"user",
				user);	
			fieldValidationUtility.checkNullMethodParameter(
				"getRIFStudySubmission",
				"studyID",
				studyID);	
			//Check for security violations
			validateUser(user);

			//Delegate operation to a specialised manager class
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			SQLRIFSubmissionManager sqlRIFSubmissionManager
				= rifServiceResources.getRIFSubmissionManager();	
			result
				= sqlRIFSubmissionManager.getRIFStudySubmission(
					connection, 
					user, 
					studyID);

		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"getRIFStudySubmission",
				rifServiceException);	
		}
		finally {
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
		
		return result;		
		
		
	}

	
	public void runStudy(
		final User _user,
		final String studyID)
		throws RIFServiceException {
	
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
	
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
				
		Connection connection = null;
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"runStudy",
				"user",
				user);	
			fieldValidationUtility.checkNullMethodParameter(
				"runStudy",
				"studyID",
				studyID);
			
			//Check for security violations
			validateUser(user);
		
			//Delegate operation to a specialised manager class
			connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			SQLRIFSubmissionManager sqlRIFSubmissionManager
				= rifServiceResources.getRIFSubmissionManager();
			sqlRIFSubmissionManager.runStudy(
				connection, 
				studyID);			
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"runStudy",
				rifServiceException);	
		}
		finally {
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);				
		}		
		
		
		
	}
		

	/*
	public void dumpDatabaseTableToCSVFile(
		final User _user,
		final String tableName,
		final String outputFilePath)
		throws Exception {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);		
		SQLConnectionManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();		
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		
		Connection connection = null;
		try {
			
			//Part II: Check for empty parameter values
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"dumpDatabaseTableToCSVFile",
				"user",
				user);	
			fieldValidationUtility.checkNullMethodParameter(
				"dumpDatabaseTableToCSVFile",
				"tableName",
				tableName);	
			fieldValidationUtility.checkNullMethodParameter(
				"dumpDatabaseTableToCSVFile",
				"outputFilePath",
				outputFilePath);	
			
			//Check for security violations
			validateUser(user);

			//Delegate operation to a specialised manager class
			connection 
				= sqlConnectionManager.assignPooledReadConnection(user);

			SQLStudyExtractManager sqlStudyExtractManager
				= rifServiceResources.getSQLStudyExtractManager();
			sqlStudyExtractManager.dumpDatabaseTableToCSVFile(
				connection, 
				tableName,
				outputFilePath);
		}
		catch(RIFServiceException rifServiceException) {
			logException(
				user,
				"dumpDatabaseTableToCSVFile",
				rifServiceException);	
		}
		finally {
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);				
		}
	}
	*/
	
}

