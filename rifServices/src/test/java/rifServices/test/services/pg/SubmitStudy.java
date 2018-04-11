package rifServices.test.services.pg;

import java.io.File;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.ComparisonArea;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudyArea;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.dataStorageLayer.pg.PGSQLSampleTestObjectGenerator;
import rifServices.system.RIFServiceError;
import rifServices.test.services.CommonRIFServiceTestCase;

import static org.junit.Assert.fail;

import static rifGenericLibrary.system.RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER;
import static rifServices.system.RIFServiceError.INVALID_RIF_JOB_SUBMISSION;

public final class SubmitStudy extends CommonRIFServiceTestCase {

	public SubmitStudy() {}

	@Test
	@Ignore
	public void submitStudy_COMMON1() throws RIFServiceException {

		File validOutputFile = null;
		
		User validUser = cloneValidUser();

		//use an example rif submission from the sample data
		//generator we have
		PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
			= new PGSQLSampleTestObjectGenerator();
		RIFStudySubmission studySubmission
			= sampleTestObjectGenerator.createSampleRIFJobSubmission();
		validOutputFile
			= sampleTestObjectGenerator.generateSampleOutputFile();

		String results
			= rifStudySubmissionService.submitStudy(
				validUser,
				studySubmission,
				validOutputFile);
		System.out.println("submitStudy_COMMON1=="+results+"==");
	}
	
	@Test
	public void submitStudy_EMPTY1() {

		File validOutputFile = null;
		
		try {
			User emptyUser = cloneEmptyUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission studySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				emptyUser, 
				studySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.INVALID_USER,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}

	@Test
	public void submitStudy_NULL1() {

		File validOutputFile = null;
		
		try {
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission validStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				null, 
				validStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				EMPTY_API_METHOD_PARAMETER,
				1);
		}
		finally {
			validOutputFile.delete();			
		}
	}

	/**
	 * Ensure empty checks are being done in Investigations
	 */
	@Test
	@Ignore
	public void submitStudy_EMPTY2() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			//randomly insert an empty value into the field of some
			//object that is part of the study submission
			
			DiseaseMappingStudy study = (DiseaseMappingStudy) emptyStudySubmission.getStudy();
			ArrayList<Investigation> investigations
				= study.getInvestigations();
			Investigation investigation = investigations.get(0);
			ArrayList<AgeBand> ageBands = investigation.getAgeBands();
			AgeGroup ageGroup = ageBands.get(0).getLowerLimitAgeGroup();
			ageGroup.setLowerLimit("");
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				emptyStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				INVALID_RIF_JOB_SUBMISSION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}

	/**
	 * Ensure empty checks are being done in Project
	 */
	@Test
	@Ignore
	public void submitStudy_EMPTY3() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			emptyStudySubmission.setProject(cloneEmptyProject());
			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				emptyStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_PROJECT,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	/**
	 * Ensure empty checks are being done in Comparison Area
	 */
	@Test
	@Ignore
	public void submitStudy_EMPTY4() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			DiseaseMappingStudy diseaseMappingStudy
				= (DiseaseMappingStudy) emptyStudySubmission.getStudy();
			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			comparisonArea.setGeoLevelView(cloneEmptyGeoLevelView());			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				emptyStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				INVALID_RIF_JOB_SUBMISSION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	
	
	@Test
	public void submitStudy_NULL2() {
		File validOutputFile = null;
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				null,
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				EMPTY_API_METHOD_PARAMETER,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	@Test
	@Ignore
	public void submitStudy_NULL3() {
		try {
			User validUser = cloneValidUser();

			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission validStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			rifStudySubmissionService.submitStudy(
				validUser, 
				validStudySubmission,
				null);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}
	}

	@Test
	@Ignore
	/**
	 * make sure a null value somewhere deep within the RIF Study Submission
	 * object tree is detected
	 */
	public void submitStudy_NULL4() {
		try {
			User validUser = cloneValidUser();

			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			DiseaseMappingStudy diseaseMappingStudy
				= (DiseaseMappingStudy) emptyStudySubmission.getStudy();
			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			comparisonArea.addMapArea(null);

			File validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				emptyStudySubmission,
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				INVALID_RIF_JOB_SUBMISSION,
				1);
		}
	}	
	
	
	@Test
	public void submitStudy_NONEXISTENT1() {

		File validOutputFile = null;

		try {
			User nonExistentUser = cloneNonExistentUser();
			
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission validStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				nonExistentUser, 
				validStudySubmission, 
				validOutputFile);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	/**
	 * check non-existent geography
	 */
	@Test
	@Ignore
	public void submitStudy_NONEXISTENT2() {
		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			//randomly insert an empty value into the field of some
			//object that is part of the study submission
			
			DiseaseMappingStudy study 
				= (DiseaseMappingStudy) emptyStudySubmission.getStudy();
			study.setGeography(cloneNonExistentGeography());
			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				emptyStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}
		finally {
			validOutputFile.delete();
		}		
	}

	/**
	 * check whether non-existent items are being checked in study area
	 */
	@Test
	@Ignore
	public void submitStudy_NONEXISTENT3() {
		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			//randomly insert an empty value into the field of some
			//object that is part of the study submission
			
			DiseaseMappingStudy study 
				= (DiseaseMappingStudy) emptyStudySubmission.getStudy();
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= study.getDiseaseMappingStudyArea();
			diseaseMappingStudyArea.setGeoLevelToMap(cloneNonExistentGeoLevelToMap());
			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				emptyStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOLEVEL_TO_MAP_VALUE,
				1);
		}
		finally {
			validOutputFile.delete();
		}		
	}

	
	/**
	 * check whether non-existent map areas are being checked
	 */
	@Test
	@Ignore
	public void submitStudy_NONEXISTENT4() {
		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			//randomly insert an empty value into the field of some
			//object that is part of the study submission
			
			DiseaseMappingStudy study 
				= (DiseaseMappingStudy) emptyStudySubmission.getStudy();			
			ComparisonArea comparisonArea
				= study.getComparisonArea();
			comparisonArea.addMapArea(cloneNonExistentMapArea());
			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				emptyStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_MAP_AREA,
				1);
		}
		finally {
			validOutputFile.delete();
		}		
	}

	/**
	 * check whether non-existent items are being checked in investigations
	 */
	@Test
	@Ignore
	public void submitStudy_NONEXISTENT5() {
		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			//randomly insert an empty value into the field of some
			//object that is part of the study submission
			
			DiseaseMappingStudy study 
				= (DiseaseMappingStudy) emptyStudySubmission.getStudy();
			ArrayList<Investigation> investigations
				= study.getInvestigations();
			Investigation firstInvestigation
				= investigations.get(0);
			firstInvestigation.setHealthTheme(cloneNonExistentHealthTheme());

			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				emptyStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_HEALTH_THEME,
				1);
		}
		finally {
			validOutputFile.delete();
		}		
	}

	/**
	 * check whether non-existent project is done
	 */
	@Test
	@Ignore
	public void submitStudy_NONEXISTENT6() {
		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission nonExistentStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			//randomly insert an empty value into the field of some
			//object that is part of the study submission
			
			nonExistentStudySubmission.setProject(cloneNonExistentProject());

			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			if (validOutputFile == null) {
				System.out.println("Valid output file is NULL");
			}
			rifStudySubmissionService.submitStudy(
				validUser, 
				nonExistentStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_PROJECT,
				1);
		}
		finally {
			validOutputFile.delete();
		}		
	}

	
	/**
	 * ensure malicious user checked
	 */
	@Test
	public void submitStudy_MALICIOUS1() {

		File validOutputFile = null;
		
		try {
			User maliciousUser = cloneMaliciousUser();
			
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission validStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();

			rifStudySubmissionService.submitStudy(
				maliciousUser, 
				validStudySubmission, 
				validOutputFile);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}

	@Test
	/**
	 * ensure malicious code checks are happening in the Project object
	 */
	public void submitStudy_MALICIOUS2() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission maliciousStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			maliciousStudySubmission.setProject(cloneMaliciousProject());
			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				maliciousStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	@Test
	/**
	 * ensure malicious code checks are happening in the Comparison Area
	 */
	public void submitStudy_MALICIOUS3() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission maliciousStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			DiseaseMappingStudy diseaseMappingStudy 
				= (DiseaseMappingStudy) maliciousStudySubmission.getStudy();
			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			comparisonArea.addMapArea(cloneMaliciousMapArea());
					
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				maliciousStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}

	@Test
	/**
	 * ensure malicious code checks are happening in the Study Area
	 */
	public void submitStudy_MALICIOUS4() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission maliciousStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			DiseaseMappingStudy diseaseMappingStudy 
				= (DiseaseMappingStudy) maliciousStudySubmission.getStudy();
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			diseaseMappingStudyArea.setGeoLevelSelect(cloneMaliciousGeoLevelSelect());
					
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				maliciousStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	@Test
	/**
	 * ensure malicious code checks are happening in the Investigation
	 */
	public void submitStudy_MALICIOUS5() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission maliciousStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			DiseaseMappingStudy diseaseMappingStudy 
				= (DiseaseMappingStudy) maliciousStudySubmission.getStudy();
			ArrayList<Investigation> investigations
				= diseaseMappingStudy.getInvestigations();
			Investigation firstInvestigation
				= investigations.get(0);
			firstInvestigation.setDescription(getTestMaliciousValue());
			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				maliciousStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	@Test
	/**
	 * ensure malicious code checks are happening in the study
	 */
	public void submitStudy_MALICIOUS6() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			PGSQLSampleTestObjectGenerator sampleTestObjectGenerator
				= new PGSQLSampleTestObjectGenerator();
			RIFStudySubmission maliciousStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			
			DiseaseMappingStudy diseaseMappingStudy
				= (DiseaseMappingStudy) maliciousStudySubmission.getStudy();
			diseaseMappingStudy.setName(getTestMaliciousValue());
			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				maliciousStudySubmission, 
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
}
