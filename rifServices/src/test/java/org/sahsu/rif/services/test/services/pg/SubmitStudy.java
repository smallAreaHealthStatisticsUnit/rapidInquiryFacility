package org.sahsu.rif.services.test.services.pg;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.datastorage.common.SampleTestObjectGenerator;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

import static org.junit.Assert.fail;
import static org.sahsu.rif.generic.system.RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER;

public final class SubmitStudy extends CommonRIFServiceTestCase {

	public SubmitStudy() {}

	@Test
	public void submitStudy_EMPTY1() {

		File validOutputFile = null;
		
		try {
			User emptyUser = cloneEmptyUser();
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission studySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				emptyUser, 
				studySubmission, 
				validOutputFile, "");
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
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission validStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				null, 
				validStudySubmission, 
				validOutputFile, "");
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
	public void submitStudy_NULL2() {
		File validOutputFile = null;
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				null,
				validOutputFile, "");
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
	public void submitStudy_NONEXISTENT1() {

		File validOutputFile = null;

		try {
			User nonExistentUser = cloneNonExistentUser();
			
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission validStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				nonExistentUser, 
				validStudySubmission, 
				validOutputFile, "");
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
	 * ensure malicious user checked
	 */
	@Test
	public void submitStudy_MALICIOUS1() {

		File validOutputFile = null;
		
		try {
			User maliciousUser = cloneMaliciousUser();
			
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission validStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();

			rifStudySubmissionService.submitStudy(
				maliciousUser, 
				validStudySubmission, 
				validOutputFile, "");
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
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission maliciousStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			maliciousStudySubmission.setProject(cloneMaliciousProject());
			
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				maliciousStudySubmission, 
				validOutputFile, "");
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
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
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
				validOutputFile, "");
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
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission maliciousStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			DiseaseMappingStudy diseaseMappingStudy 
				= (DiseaseMappingStudy) maliciousStudySubmission.getStudy();
			AbstractStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getStudyArea();
			diseaseMappingStudyArea.setGeoLevelSelect(cloneMaliciousGeoLevelSelect());
					
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();
			rifStudySubmissionService.submitStudy(
				validUser, 
				maliciousStudySubmission, 
				validOutputFile, "");
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
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
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
				validOutputFile, "");
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
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
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
				validOutputFile, "");
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
