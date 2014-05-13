package rifServices.test.services;


import rifServices.test.TestRIFSubmissionService;


import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifServices.test.AbstractRIFTestCase;
import rifServices.util.DisplayableItemSorter;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


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

public class TestHealthCodeProvider extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The service. */
	private TestRIFSubmissionService service;
	
	/** The icd10 health code taxonomy. */
	private HealthCodeTaxonomy icd10HealthCodeTaxonomy;
	
	/** The test user. */
	private User testUser;
	
	/** The invalid user. */
	private User invalidUser;
	
	/** The master health code. */
	//private HealthCode masterHealthCode;
	
	private HealthCode masterChapter02HealthCode;
	private HealthCode masterC34HealthCode;
	private HealthCode masterC342HealthCode;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test health outcome features.
	 */
	public TestHealthCodeProvider() {
		service = new TestRIFSubmissionService();
		service.initialiseService();

		
		testUser = User.newInstance("keving", "11.111.11.228");
		invalidUser = User.newInstance("nobody", "11.111.11.228");
	
		masterChapter02HealthCode = HealthCode.newInstance();
		masterChapter02HealthCode.setCode("Chapter 02");
		masterChapter02HealthCode.setDescription("Chapter 02; Neoplasms");
		masterChapter02HealthCode.setNameSpace("icd10");
		masterChapter02HealthCode.setTopLevelTerm(true);

		masterC34HealthCode = HealthCode.newInstance();
		masterC34HealthCode.setCode("C34");
		masterC34HealthCode.setDescription("malignant neoplasm of bronchus and lung");
		masterC34HealthCode.setNameSpace("icd10");
	
		masterC342HealthCode = HealthCode.newInstance();
		masterC342HealthCode.setCode("C342");
		masterC342HealthCode.setDescription("middle lobe, bronchus or lung");
		masterC342HealthCode.setNameSpace("icd10");
		
	}

	@Before
	public void setUp() {
		try {
			service.login("keving", new String("a").toCharArray());			
			ArrayList<HealthCodeTaxonomy> healthCodeTaxonomies
				= service.getHealthCodeTaxonomies(testUser);
			icd10HealthCodeTaxonomy
				= healthCodeTaxonomies.get(1);	
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}		
	}
	
	@After
	public void tearDown() {
		try {
			service.clearHealthCodeProviders(testUser);
			service.deregisterAllUsers();		
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
	public void getTopLevelCodesN1() {
		try {			
			ArrayList<HealthCode> topLevelICD10Codes
				= service.getTopLevelCodes(testUser, icd10HealthCodeTaxonomy);
			assertEquals(2, topLevelICD10Codes.size());
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
			HealthCode chapter02
				= HealthCode.createCopy(masterChapter02HealthCode);

			ArrayList<HealthCode> firstGenerationCodes
				= service.getImmediateSubterms(
					testUser, 
					chapter02);			
			assertEquals(2, firstGenerationCodes.size());
			
			DisplayableItemSorter sorter = new DisplayableItemSorter();
			for (HealthCode firstGenerationCode : firstGenerationCodes) {
				sorter.addDisplayableListItem(firstGenerationCode);
			}
			
			HealthCode currentResult = (HealthCode) sorter.sortList().get(0);
			assertEquals("C34", currentResult.getCode());
			
			ArrayList<HealthCode> secondGenerationCodes
				= service.getImmediateSubterms(
					testUser, 
					currentResult);			
			assertEquals(3, secondGenerationCodes.size());
			
			sorter = new DisplayableItemSorter();
			for (HealthCode secondGenerationCode : secondGenerationCodes) {
				sorter.addDisplayableListItem(secondGenerationCode);
			}

			currentResult = (HealthCode) sorter.sortList().get(0);
			assertEquals("C340", currentResult.getCode());		
			currentResult = (HealthCode) sorter.sortList().get(1);
			assertEquals("C341", currentResult.getCode());			
			currentResult = (HealthCode) sorter.sortList().get(2);
			assertEquals("C342", currentResult.getCode());

			//C342 should have no children
			ArrayList<HealthCode> thirdGenerationCodes
				= service.getImmediateSubterms(
					testUser, 
					currentResult);			
			assertEquals(0, thirdGenerationCodes.size());

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
	 * Check that service tries to validate a health code
	 */
	public void getImmediateSubTermsE1() {
		HealthCode healthCode 
			= HealthCode.createCopy(masterC34HealthCode);
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
	 * Check for non-existent health code 
	 */
	public void getImmediateSubTermsE2() {
		HealthCode healthCode 
			= HealthCode.createCopy(masterC34HealthCode);		
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
	 * Gets the immediate sub terms e3.
	 *
	 * @return the immediate sub terms e3
	 */
	@Test
	/**
	 * Check for invalid user 
	 */
	public void getImmediateSubTermsE3() {
		try {
			HealthCode healthCode
				= HealthCode.createCopy(masterC34HealthCode);
			service.getImmediateSubterms(
				invalidUser, 
				healthCode);		
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
			
			HealthCode healthCode 
				= HealthCode.createCopy(masterC342HealthCode);

			HealthCode parentCode
				= service.getParentHealthCode(
					testUser, 
					healthCode);
			assertNotNull(parentCode);
			assertEquals(masterC34HealthCode.getCode(), parentCode.getCode());
			assertEquals(false, parentCode.isTopLevelTerm());
			assertEquals(
				masterC34HealthCode.getDescription(),
				parentCode.getDescription());

			HealthCode grandParentHealthCode
				= service.getParentHealthCode(
					testUser, 
					parentCode);
			assertEquals(masterChapter02HealthCode.getCode(), grandParentHealthCode.getCode());
			System.out.println("getParentHealthCodeN1 grandParentHealthCode=="+grandParentHealthCode.getDisplayName()+"==");
			assertEquals(true, grandParentHealthCode.isTopLevelTerm());
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
				= HealthCode.createCopy(masterC342HealthCode);
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
	 * Check for non-existent health code 
	 */
	public void getParentHealthCodeE2() {
		try {
			HealthCode healthCode 
				= HealthCode.createCopy(masterC342HealthCode);
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
	 * Gets the parent health code e3.
	 *
	 * @return the parent health code e3
	 */
	@Test
	/**
	 * Check for invalid user 
	 */
	public void getParentHealthCodeE3() {
		try {
			HealthCode healthCode
				= HealthCode.createCopy(masterC342HealthCode);
			
			service.getParentHealthCode(
				invalidUser, 
				healthCode);			
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

