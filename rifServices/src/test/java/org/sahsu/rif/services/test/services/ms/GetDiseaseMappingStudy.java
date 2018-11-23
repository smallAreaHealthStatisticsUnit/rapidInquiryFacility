package org.sahsu.rif.services.test.services.ms;

import java.io.File;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.AgeBand;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.datastorage.common.SampleTestObjectGenerator;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

import static org.junit.Assert.fail;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public final class GetDiseaseMappingStudy
		extends CommonRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String validStudyID;
	private String maliciousStudyID;
	private String nonExistentStudyID;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GetDiseaseMappingStudy() {
		validStudyID = "13";
		
		maliciousStudyID = getTestMaliciousValue();
		nonExistentStudyID = "blah";
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	private void populateDatabaseWithValidStudy() {

		SampleTestObjectGenerator sampleTestObjectGenerator
			= new SampleTestObjectGenerator();
		RIFStudySubmission studySubmission
			= sampleTestObjectGenerator.createSampleRIFJobSubmission();		
		
		
	}
	
	@Test
	@Ignore
	public void getDiseaseMappingStudy_COMMON1() {

		try {
			User validUser = cloneValidUser();
			
			populateDatabaseWithValidStudy();			
			
			//use an example rif submission from the sample data
			//generator we have
			SampleTestObjectGenerator sampleTestObjectGenerator
				= new SampleTestObjectGenerator();
			RIFStudySubmission studySubmission
				= sampleTestObjectGenerator.createSampleRIFJobSubmission();

			DiseaseMappingStudy diseaseMappingStudy
				= rifStudySubmissionService.getDiseaseMappingStudy(
					validUser, 
					validStudyID);
			
			RIFStudySubmission submission =
					RIFStudySubmission.newInstance();
			submission.setStudy(diseaseMappingStudy);
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}
	}
	
	
	@Test
	public void getDiseaseMappingStudy_EMPTY1() {

		try {
			User validUser = cloneValidUser();
			
			populateDatabaseWithValidStudy();			
			
			//use an example rif submission from the sample data
			//generator we have


			DiseaseMappingStudy diseaseMappingStudy
				= rifStudySubmissionService.getDiseaseMappingStudy(
					validUser, 
					validStudyID);
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}
		
		
		
		
	}
	
	@Test
	public void submitStudy_EMPTY1() {

		try {
			User validUser = cloneValidUser();
			
			//use an example rif submission from the sample data
			//generator we have


			DiseaseMappingStudy diseaseMappingStudy
				= rifStudySubmissionService.getDiseaseMappingStudy(
					validUser, 
					validStudyID);
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}
	}

	@Test
	public void getDiseaseMappingStudy_NULL1() {

		

		
		
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
	
	/**
	 * tests whether file has write permissions enabled
	 */
	@Test
	@Ignore
	public void submitStudy_FILE_PERMISSIONS() {
		fail();
	}
}
