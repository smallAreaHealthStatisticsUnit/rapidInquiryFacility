package rifServices.test.services;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.ComparisonArea;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.SampleTestObjectGenerator;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;

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

public class SubmitStudy extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public SubmitStudy() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	
	@Test
	public void submitStudy_COMMON1() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission studySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();

			rifStudySubmissionService.submitStudy(
				validUser, 
				studySubmission, 
				validOutputFile);
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}
		finally {
			validOutputFile.delete();
		}
	}
	
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
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
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
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}
		finally {
			validOutputFile.delete();			
		}
	}

	@Test
	public void submitStudy_EMPTY2() {

		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
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
				RIFServiceError.INVALID_RIF_JOB_SUBMISSION,
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
				validOutputFile);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	@Test
	public void submitStudy_NULL3() {
		try {
			User validUser = cloneValidUser();

			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
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
				validOutputFile);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	@Test
	public void submitStudy_NONEXISTENT2() {
		File validOutputFile = null;
		
		try {
			User validUser = cloneValidUser();
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission emptyStudySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			//randomly insert an empty value into the field of some
			//object that is part of the study submission
			
			DiseaseMappingStudy study = (DiseaseMappingStudy) emptyStudySubmission.getStudy();
			ComparisonArea comparisonArea
				= study.getComparisonArea();
			GeoLevelSelect nonExistentGeoLevelSelect
				= cloneNonExistentGeoLevelSelect();
			comparisonArea.setGeoLevelSelect(nonExistentGeoLevelSelect);
			
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
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE,
				1);
		}
		finally {
			validOutputFile.delete();
		}		
	}

	@Test
	public void submitStudy_MALICIOUS1() {

		File validOutputFile = null;
		
		try {
			User maliciousUser = cloneMaliciousUser();
			
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission studySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();
			validOutputFile
				= sampleTestObjectGenerator.generateSampleOutputFile();

			rifStudySubmissionService.submitStudy(
				maliciousUser, 
				studySubmission, 
				validOutputFile);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	@Test
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
			//randomly insert an empty value into the field of some
			//object that is part of the study submission
			
			DiseaseMappingStudy study 
				= (DiseaseMappingStudy) maliciousStudySubmission.getStudy();
			ComparisonArea comparisonArea = study.getComparisonArea();
			comparisonArea.setGeoLevelSelect(cloneMaliciousGeoLevelSelect());

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
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}
		finally {
			validOutputFile.delete();
		}
	}
	
	@Test
	/**
	 * tests whether file has write permissions enabled
	 */
	public void submitStudy_FILE_PERMISSIONS() {
		fail();
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
