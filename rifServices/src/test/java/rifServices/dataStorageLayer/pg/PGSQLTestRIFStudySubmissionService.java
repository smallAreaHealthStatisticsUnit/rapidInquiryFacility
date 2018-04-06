package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.FieldValidationUtility;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AdjustableCovariate;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.ExposureCovariate;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.common.CovariateManager;
import rifServices.dataStorageLayer.common.DiseaseMappingStudyManager;
import rifServices.dataStorageLayer.common.HealthOutcomeManager;
import rifServices.dataStorageLayer.common.SQLManager;
import rifServices.dataStorageLayer.common.SubmissionManager;
import rifServices.ontologyServices.HealthCodeProviderInterface;

public final class PGSQLTestRIFStudySubmissionService extends PGSQLAbstractRIFStudySubmissionService
		implements RIFStudySubmissionAPI {

	PGSQLTestRIFStudySubmissionService() {

	}

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
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();				
		if (sqlConnectionManager.isUserBlocked(user)) {
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
			AgeGenderYearManager sqlAgeGenderYearManager
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
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();						
		if (sqlConnectionManager.isUserBlocked(user)) {
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

			CovariateManager sqlCovariateManager
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
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(adminUser)) {
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
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();
		if (sqlConnectionManager.isUserBlocked(adminUser)) {
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
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user)) {
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

			SubmissionManager sqlRIFSubmissionManager
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
		
	public void clearStudiesForUser(
		final User _user)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		if (sqlConnectionManager.isUserBlocked(user)) {
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
			DiseaseMappingStudyManager sqlDiseaseMappingStudyManager
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
		SQLManager sqlConnectionManager
			= rifServiceResources.getSqlConnectionManager();	
		
		RIFStudySubmission result = null;
		if (sqlConnectionManager.isUserBlocked(user)) {
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

			SubmissionManager sqlRIFSubmissionManager
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
}

