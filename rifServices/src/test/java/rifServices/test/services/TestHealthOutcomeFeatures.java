package rifServices.test.services;


import rifServices.ProductionRIFJobSubmissionService;
import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifServices.test.AbstractRIFTestCase;

import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.Test;


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

public class TestHealthOutcomeFeatures extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The service. */
	private ProductionRIFJobSubmissionService service;
	
	/** The icd10 health code taxonomy. */
	private HealthCodeTaxonomy icd10HealthCodeTaxonomy;
	
	/** The test user. */
	private User testUser;
	
	/** The invalid user. */
	private User invalidUser;
	
	/** The master health code. */
	private HealthCode masterHealthCode;
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test health outcome features.
	 */
	public TestHealthOutcomeFeatures() {
		service
			= new ProductionRIFJobSubmissionService();
		
		testUser = User.newInstance("keving", "11.111.11.228");
		invalidUser = User.newInstance("nobody", "11.111.11.228");
	
		masterHealthCode = HealthCode.newInstance();
		masterHealthCode.setCode("0880");
		masterHealthCode.setDescription("obstetric air embolism");
		masterHealthCode.setNameSpace("icd10");
		
		try {
			service.login("keving", "a");	
			
			ArrayList<HealthCodeTaxonomy> healthCodeTaxonomies
				= service.getHealthCodeTaxonomies(testUser);
			icd10HealthCodeTaxonomy
				= healthCodeTaxonomies.get(1);	
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}
		
	}


	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the top level code n1.
	 *
	 * @return the top level code n1
	 */
	@Test
	public void getTopLevelCodeN1() {
		try {			
			ArrayList<HealthCode> topLevelICD10Codes
				= service.getTopLevelCodes(testUser, icd10HealthCodeTaxonomy);
			assertEquals(20, topLevelICD10Codes.size());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}
	
	/**
	 * Gets the top level code e1.
	 *
	 * @return the top level code e1
	 */
	@Test
	public void getTopLevelCodeE1() {
		try {			
			service.getTopLevelCodes(invalidUser, icd10HealthCodeTaxonomy);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}		
	}
	
	
	/**
	 * Gets the immediate sub terms n1.
	 *
	 * @return the immediate sub terms n1
	 */
	@Test
	public void getImmediateSubTermsN1() {
		try {		

			System.out.println("TEST getImmediateSubTerms START ===================");
			HealthCode chapter15
				= HealthCode.newInstance(
					"Chapter 15",
					"icd10",
					"Chapter 15; Pregnancy, childbirth and the puerperium",
					true);
			ArrayList<HealthCode> level3Codes
				= service.getImmediateSubterms(
					testUser, 
					chapter15);			
			assertEquals(60, level3Codes.size());
			HealthCode firstLevel3Code = level3Codes.get(0);
			assertEquals("O00", firstLevel3Code.getCode());
			assertEquals("ECTOPIC PREGNANCY", firstLevel3Code.getDescription());
			int lastLevel3Index = level3Codes.size() - 1;
			HealthCode lastLevel3Code = level3Codes.get(lastLevel3Index);
			assertEquals("O99", lastLevel3Code.getCode());
						
			HealthCode codeO87
				= HealthCode.newInstance(
					"O87",
					"icd10",
					"venous complications in the puerperium",
					false);
			ArrayList<HealthCode> level4Codes
				= service.getImmediateSubterms(
					testUser, 
					codeO87);
			assertEquals(6, level4Codes.size());
			HealthCode firstSubTerm = level4Codes.get(0);
			assertEquals("O870", firstSubTerm.getCode());
			
			HealthCode lastSubTerm = level4Codes.get(5);
			assertEquals("O879", lastSubTerm.getCode());	
						
			HealthCode codeO872
				= HealthCode.newInstance(
					"O872",
					"icd10",
					"haemorrhoids in the puerperium",
					false);
			
			ArrayList<HealthCode> level5Codes
				= service.getImmediateSubterms(
					testUser, 
					codeO872);
			assertEquals(0, level5Codes.size());	
			System.out.println("TEST getImmediateSubTerms END ===================");

		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("getImmediateSubTermsN1", rifServiceException);
			fail();
		}		
	}

	/**
	 * Gets the immediate sub terms e1.
	 *
	 * @return the immediate sub terms e1
	 */
	@Test
	/**
	 * Check for invalid health code 
	 */
	public void getImmediateSubTermsE1() {
		HealthCode healthCode 
			= HealthCode.createCopy(masterHealthCode);
		healthCode.setDescription("");
		healthCode.setCode(null);
		
		try {
			service.getImmediateSubterms(
				testUser, 
				healthCode);			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_HEALTH_CODE, 
				2);
		}
	}	
	
	
	/**
	 * Gets the immediate sub terms e2.
	 *
	 * @return the immediate sub terms e2
	 */
	@Test
	/**
	 * Check for valid health code that violates ICD rules 
	 */
	public void getImmediateSubTermsE2() {
		HealthCode healthCode 
			= HealthCode.createCopy(masterHealthCode);
		//RIF does not support two digit code
		healthCode.setCode("08");
		try {
			service.getImmediateSubterms(
				testUser, 
				healthCode);			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_ICD_CODE, 
				1);
		}
	}	
	
	/**
	 * Gets the immediate sub terms e3.
	 *
	 * @return the immediate sub terms e3
	 */
	@Test
	/**
	 * Check for non-existent health code 
	 */
	public void getImmediateSubTermsE3() {
		HealthCode healthCode 
			= HealthCode.createCopy(masterHealthCode);		
		//this code guaranteed not to exist in ICD10
		healthCode.setCode("XYZ");
	
		try {
			service.getImmediateSubterms(
				testUser, 
				healthCode);			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_HEALTH_CODE, 
				1);
		}		
	}
	
	/**
	 * Gets the immediate sub terms e4.
	 *
	 * @return the immediate sub terms e4
	 */
	@Test
	/**
	 * Check for invalid user 
	 */
	public void getImmediateSubTermsE4() {
		try {
			HealthCode codeO872
				= HealthCode.newInstance(
					"O872",
					"icd10",
					"haemorrhoids in the puerperium",
					false);
			service.getImmediateSubterms(
				invalidUser, 
				codeO872);		
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION,
				1);			
		}		
	}	
	
	
	/**
	 * Gets the parent health code n1.
	 *
	 * @return the parent health code n1
	 */
	@Test 
	public void getParentHealthCodeN1() {
		try {
			HealthCode codeO872
				= HealthCode.newInstance(
					"O872",
					"icd10",
					"haemorrhoids in the puerperium",
					false);
			HealthCode level3ParentCode
				= service.getParentHealthCode(
					testUser, 
					codeO872);
			assertNotNull(level3ParentCode);
			assertEquals("O87", level3ParentCode.getCode());
			assertEquals(false, level3ParentCode.isTopLevelTerm());
			assertEquals(
				"VENOUS COMPLICATIONS IN THE PUERPERIUM",
				level3ParentCode.getDescription());

			HealthCode level1ParentCode
				= service.getParentHealthCode(
					testUser, 
					level3ParentCode);
			assertEquals("Chapter 15", level1ParentCode.getCode());
			assertEquals(true, level1ParentCode.isTopLevelTerm());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}	
	
	/**
	 * Gets the parent health code e1.
	 *
	 * @return the parent health code e1
	 */
	@Test
	/**
	 * Check for invalid health code 
	 */
	public void getParentHealthCodeE1() {
		try {
			HealthCode healthCode 
				= HealthCode.createCopy(masterHealthCode);
			healthCode.setCode("");
			healthCode.setDescription(null);
						
			service.getParentHealthCode(
				testUser, 
				healthCode);		
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_HEALTH_CODE,
				2);			
		}
		
	}

	/**
	 * Gets the parent health code e2.
	 *
	 * @return the parent health code e2
	 */
	@Test
	/**
	 * Check for valid health code that violates ICD rules 
	 */
	public void getParentHealthCodeE2() {
		try {
			HealthCode healthCode 
				= HealthCode.createCopy(masterHealthCode);
			healthCode.setCode("80");

			service.getParentHealthCode(
				testUser, 
				healthCode);			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_ICD_CODE,
				1);
		}
	}
	
	/**
	 * Gets the parent health code e3.
	 *
	 * @return the parent health code e3
	 */
	@Test
	/**
	 * Check for non-existent health code 
	 */
	public void getParentHealthCodeE3() {
		try {
			HealthCode healthCode 
				= HealthCode.createCopy(masterHealthCode);
			healthCode.setCode("XYZ");

			service.getParentHealthCode(
				testUser, 
				healthCode);			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_HEALTH_CODE,
				1);			
		}		
	}
	

	/**
	 * Gets the parent health code e4.
	 *
	 * @return the parent health code e4
	 */
	@Test
	/**
	 * Check for invalid user 
	 */
	public void getParentHealthCodeE4() {
		try {
			HealthCode codeO872
				= HealthCode.newInstance(
					"O872",
					"icd10",
					"haemorrhoids in the puerperium",
					false);

			service.getParentHealthCode(
				invalidUser, 
				codeO872);			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION,
				1);			
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
