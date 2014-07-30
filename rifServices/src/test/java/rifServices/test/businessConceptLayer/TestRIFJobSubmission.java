package rifServices.test.businessConceptLayer;



import rifServices.businessConceptLayer.*;
import rifServices.dataStorageLayer.SampleTestObjectGenerator;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;
import rifServices.test.AbstractRIFTestCase;
import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Date;


/**
 *
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

public class TestRIFJobSubmission extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The generator. */
	private SampleTestObjectGenerator generator;
	
	/** The master rif job submission. */
	private RIFStudySubmission masterRIFJobSubmission;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test rif job submission.
	 */
	public TestRIFJobSubmission() {
		generator = new SampleTestObjectGenerator();
		masterRIFJobSubmission
			= RIFStudySubmission.newInstance();
		masterRIFJobSubmission.addCalculationMethod(generator.createSampleHETMethod());
		masterRIFJobSubmission.addCalculationMethod(generator.createSampleBYMMethod());	
		masterRIFJobSubmission.setJobSubmissionTime(new Date());
		
		User testUser = User.newInstance("kgarwood", "11.111.11.228");
		masterRIFJobSubmission.setUser(testUser);
		
		DiseaseMappingStudy diseaseMappingStudy
			= generator.createSampleDiseaseMappingStudy();
		masterRIFJobSubmission.setStudy(diseaseMappingStudy);
		
		masterRIFJobSubmission.addRIFOutputOption(RIFOutputOption.RATIOS_AND_RATES);
	}
		
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Accept valid rif job submission.
	 */
	@Test
	/**
	 * Accept a valid rif job submission with typical values.
	 */
	public void acceptValidRIFJobSubmission() {
		try {
			RIFStudySubmission rifJobSubmission
				= RIFStudySubmission.createCopy(masterRIFJobSubmission);
			rifJobSubmission.checkErrors();
		}
		catch(RIFServiceException rifServiceException) {
			fail();	
		}
	}
	
	/**
	 * Reject blank study.
	 */
	@Test
	/**
	 * rif job submission is invalid if no study is specified
	 */
	public void rejectBlankStudy() {
		try {
			RIFStudySubmission rifJobSubmission
				= RIFStudySubmission.createCopy(masterRIFJobSubmission);
			rifJobSubmission.setStudy(null);
			rifJobSubmission.checkErrors();
			fail();	
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_RIF_JOB_SUBMISSION, 
				1);
		}
	}

	/**
	 * Reject invalid study.
	 */
	@Test
	/**
	 * rif job submission is invalid if its study is invalid
	 */
	public void rejectInvalidStudy() {
		try {
			RIFStudySubmission rifJobSubmission
				= RIFStudySubmission.createCopy(masterRIFJobSubmission);
			
			SampleTestObjectGenerator generator 
				= new SampleTestObjectGenerator();
			DiseaseMappingStudy invalidDiseaseMappingStudy
				= generator.createSampleDiseaseMappingStudy();
			invalidDiseaseMappingStudy.setName(null);
			rifJobSubmission.setStudy(invalidDiseaseMappingStudy);
			rifJobSubmission.checkErrors();
			fail();	
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_RIF_JOB_SUBMISSION, 
				1);
		}
	}
	
	/**
	 * Reject invalid calculation method.
	 */
	@Test
	/**
	 * rif job submission is invalid if it has an invalid calculation method
	 */
	public void rejectInvalidCalculationMethod() {
		try {
			RIFStudySubmission rifJobSubmission
				= RIFStudySubmission.createCopy(masterRIFJobSubmission);	
			SampleTestObjectGenerator generator
				= new SampleTestObjectGenerator();
			CalculationMethod invalidCalculationMethod
				= generator.createSampleCalculationMethod("Blah");
			invalidCalculationMethod.setPrior(null);			
			rifJobSubmission.addCalculationMethod(invalidCalculationMethod);
			rifJobSubmission.checkErrors();
			fail();	
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_RIF_JOB_SUBMISSION, 
				1);
		}
	}
	
	/**
	 * Reject empty rif output options.
	 */
	@Test
	/**
	 * rif job submission is invalid if no output options specified
	 */
	public void rejectEmptyRIFOutputOptions() {
		try {
			RIFStudySubmission rifJobSubmission
				= RIFStudySubmission.createCopy(masterRIFJobSubmission);
			rifJobSubmission.setRIFOutputOptions(null);			
			rifJobSubmission.checkErrors();
			fail();	
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_RIF_JOB_SUBMISSION, 
				1);
		}

		try {
			RIFStudySubmission rifJobSubmission
				= RIFStudySubmission.createCopy(masterRIFJobSubmission);
			rifJobSubmission.clearRIFOutputOptions();			
			rifJobSubmission.checkErrors();
			fail();	
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_RIF_JOB_SUBMISSION, 
				1);
		}
	}
	
	/**
	 * Test security violations.
	 */
	@Test
	public void testSecurityViolations() {
		RIFStudySubmission maliciousRIFJobSubmission
			= RIFStudySubmission.createCopy(masterRIFJobSubmission);
		maliciousRIFJobSubmission.setIdentifier(getTestMaliciousValue());
		try {
			maliciousRIFJobSubmission.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		/*
		 * Check that checking for security violation is recursive
		 */
		maliciousRIFJobSubmission
			= RIFStudySubmission.createCopy(masterRIFJobSubmission);
		DiseaseMappingStudy maliciousDiseaseMappingStudy
			= generator.createSampleDiseaseMappingStudy();
		maliciousDiseaseMappingStudy.setDescription(getTestMaliciousValue());
		maliciousRIFJobSubmission.setStudy(maliciousDiseaseMappingStudy);
		try {
			maliciousRIFJobSubmission.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousRIFJobSubmission
			= RIFStudySubmission.createCopy(masterRIFJobSubmission);
		CalculationMethod maliciousCalculationMethod
			= generator.createSampleBYMMethod();
		maliciousCalculationMethod.setDescription(getTestMaliciousValue());
		maliciousRIFJobSubmission.addCalculationMethod(maliciousCalculationMethod);
		try {
			maliciousRIFJobSubmission.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousRIFJobSubmission
			= RIFStudySubmission.createCopy(masterRIFJobSubmission);
		Project maliciousProject
			= Project.newInstance();
		maliciousProject.setName(getTestMaliciousValue());
		maliciousRIFJobSubmission.setProject(maliciousProject);
		try {
			maliciousRIFJobSubmission.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousRIFJobSubmission
			= RIFStudySubmission.createCopy(masterRIFJobSubmission);
		User maliciousUser 
			= User.newInstance(getTestMaliciousValue(),
					"11.111.11.228");
		maliciousRIFJobSubmission.setUser(maliciousUser);
		try {
			maliciousRIFJobSubmission.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
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
