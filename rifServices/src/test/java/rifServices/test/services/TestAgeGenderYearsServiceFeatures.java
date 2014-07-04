package rifServices.test.services;



import rifServices.businessConceptLayer.*;
import rifServices.system.*;

import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.*;


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

public class TestAgeGenderYearsServiceFeatures 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	/** The sahsu geography. */
	private Geography sahsuGeography;
	
	/** The invalid geography. */
	private Geography invalidGeography;
	
	/** The non existent geography. */
	private Geography nonExistentGeography;
	
	/** The test user. */
	private User testUser;
	
	/** The invalid user. */
	private User invalidUser;
	
	/** The non existent user. */
	private User nonExistentUser;
	
	/** The sahsu cancer nd pair. */
	private NumeratorDenominatorPair sahsuCancerNDPair;
	
	/** The invalid nd pair. */
	private NumeratorDenominatorPair invalidNDPair;
	
	/** The non existent nd pair. */
	private NumeratorDenominatorPair nonExistentNDPair;
	
	
	/** The master health code. */
	private HealthCode masterHealthCode;
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test age gender years service features.
	 */
	public TestAgeGenderYearsServiceFeatures() {
				
		masterHealthCode = HealthCode.newInstance();
		masterHealthCode.setCode("0880");
		masterHealthCode.setDescription("obstetric air embolism");
		masterHealthCode.setNameSpace("icd10");


		testUser = User.newInstance("kgarwood", "11.111.11.228");
		invalidUser = User.newInstance(null, "11.111.11.228");
		nonExistentUser = User.newInstance("nobody", "11.111.11.228");
		
		sahsuGeography
			= Geography.newInstance("SAHSU", "stuff about sahsuland");
		invalidGeography
			= Geography.newInstance(null, "");
		nonExistentGeography
			= Geography.newInstance("nonexistent area", "non-existent area stuff");

		sahsuCancerNDPair
			= NumeratorDenominatorPair.newInstance(
				"SAHSULAND_CANCER", 
				"Cancer cases in SAHSU land", 
				"SAHSULAND_POP",
				"SAHSU land population");
		invalidNDPair
			= NumeratorDenominatorPair.newInstance(
				"", 
				"Cancer cases in SAHSU land", 
				null,
				"SAHSU land population");
		nonExistentNDPair
			= NumeratorDenominatorPair.newInstance(
				"NON_EXISTENT_NUM_TABLE", 
				"description of num table", 
				"NON_EXISTENT_DENOM_TABLE",
				"description of denom table");		
		
		try {
			initialiseService();
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TestAgeGenderYearsServiceFeatures", rifServiceException);
		}		
	}

	@Before
	public void setUp() {
		try {
			rifServiceBundle.login("kgarwood", new String("a").toCharArray());			
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}		
	}
	
	@After
	public void tearDown() {
		try {
			rifServiceBundle.deregisterAllUsers();		
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}				
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the age groups accept valid inputs.
	 *
	 * @return the age groups accept valid inputs
	 */
	public void getAgeGroupsAcceptValidInputs() {
		try {
			ArrayList<AgeGroup> ageGroups
				= rifStudySubmissionService.getAgeGroups(
					testUser, 
					sahsuGeography, 
					sahsuCancerNDPair, 
					RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			if (ageGroups.size() == 0) {
				fail();
			}
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * Test null parameters.
	 *
	 * @return the age groups reject null parameters
	 */
	public void getAgeGroupsRejectNullParameters() {
		try {
			rifStudySubmissionService.getAgeGroups(
				null, 
				sahsuGeography, 
				sahsuCancerNDPair,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
		
		try {
			rifStudySubmissionService.getAgeGroups(
				testUser, 
				null, 
				sahsuCancerNDPair,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
		
		try {
			rifStudySubmissionService.getAgeGroups(
				testUser, 
				sahsuGeography, 
				null,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
	}
	
	/**
	 * Gets the age groups reject invalid parameters.
	 *
	 * @return the age groups reject invalid parameters
	 */
	@Test
	/**
	 * check invalid parameters
	 */
	public void getAgeGroupsRejectInvalidParameters() {
		try {
			rifStudySubmissionService.getAgeGroups(
				invalidUser, 
				sahsuGeography, 
				sahsuCancerNDPair,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
				1);
		}
		
		try {
			rifStudySubmissionService.getAgeGroups(
				testUser, 
				invalidGeography, 
				sahsuCancerNDPair,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);
		}
		
		try {
			rifStudySubmissionService.getAgeGroups(
				testUser, 
				sahsuGeography, 
				invalidNDPair,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				2);
		}
	}
	
	/**
	 * Gets the age groups reject non existent parameters.
	 *
	 * @return the age groups reject non existent parameters
	 */
	@Test
	/**
	 * Check non-existent parameters
	 */
	public void getAgeGroupsRejectNonExistentParameters() {
		try {
			rifStudySubmissionService.getAgeGroups(
				nonExistentUser, 
				sahsuGeography, 
				sahsuCancerNDPair,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}
		
		try {
			rifStudySubmissionService.getAgeGroups(
				testUser, 
				nonExistentGeography, 
				sahsuCancerNDPair,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}
		
		try {
			rifStudySubmissionService.getAgeGroups(
				testUser, 
				sahsuGeography, 
				nonExistentNDPair,
				RIFStudySubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_ND_PAIR,
				1);
		}
	}
		
	/**
	 * Gets the genders accept valid inputs.
	 *
	 * @return the genders accept valid inputs
	 */
	@Test
	public void getGendersAcceptValidInputs() {
		try {
			ArrayList<Sex> sexs
				= rifStudySubmissionService.getSexes(testUser);
			assertEquals(3, sexs.size());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}

	/**
	 * Gets the genders reject null parameters.
	 *
	 * @return the genders reject null parameters
	 */
	@Test
	/**
	 * check null parameters
	 */
	public void getGendersRejectNullParameters() {
		try {
			rifStudySubmissionService.getSexes(null);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
	}
	
	/**
	 * Gets the genders reject invalid parameters.
	 *
	 * @return the genders reject invalid parameters
	 */
	@Test
	/**
	 * check invalid parameters
	 */
	public void getGendersRejectInvalidParameters() {
		try {
			rifStudySubmissionService.getSexes(invalidUser);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
				1);
		}
	}
	
	/**
	 * Gets the genders reject non existent parameters.
	 *
	 * @return the genders reject non existent parameters
	 */
	@Test
	/**
	 * check non-existent parameters
	 */
	public void getGendersRejectNonExistentParameters() {
		try {
			rifStudySubmissionService.getSexes(nonExistentUser);
			fail();
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
