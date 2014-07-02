package rifServices.test.services;


import rifServices.businessConceptLayer.*;
import rifServices.system.*;

import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
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

public class TestCovariateFeatures extends AbstractRIFServiceTestCase {

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
	
	/** The master health code. */
	private HealthCode masterHealthCode;
	
	/** The invalid health code. */
	private HealthCode invalidHealthCode;
	
	/** The non existent health code. */
	private HealthCode nonExistentHealthCode;
	
	/** The valid geo level select. */
	private GeoLevelSelect validGeoLevelSelect;
	
	/** The invalid geo level select. */
	private GeoLevelSelect invalidGeoLevelSelect;
	
	/** The non existent geo level select. */
	private GeoLevelSelect nonExistentGeoLevelSelect;

	/** The valid geo level to map. */
	private GeoLevelToMap validGeoLevelToMap;
	
	/** The invalid geo level to map. */
	private GeoLevelToMap invalidGeoLevelToMap;
	
	/** The non existent geo level to map. */
	private GeoLevelToMap nonExistentGeoLevelToMap;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test covariate features.
	 */
	public TestCovariateFeatures() {
		sahsuGeography
			= Geography.newInstance("SAHSU", "stuff about sahsuland");
		invalidGeography
			= Geography.newInstance(null, null);
		nonExistentGeography
			= Geography.newInstance("Nonexistent geography", "blah");
		
		testUser = User.newInstance("keving", "11.111.11.228");
		invalidUser = User.newInstance(null, "11.111.11.228");
		nonExistentUser = User.newInstance("zzzz", "11.111.11.228");
		
		masterHealthCode = HealthCode.newInstance();
		masterHealthCode.setCode("0880");
		masterHealthCode.setDescription("obstetric air embolism");
		masterHealthCode.setNameSpace("icd10");
		invalidHealthCode = HealthCode.newInstance();
		invalidHealthCode.setCode(null);
		invalidHealthCode.setDescription("blah");
		invalidHealthCode.setNameSpace("blah");		
		nonExistentHealthCode = HealthCode.newInstance();
		nonExistentHealthCode.setCode("AX3X");
		nonExistentHealthCode.setDescription("just making it up");
		nonExistentHealthCode.setNameSpace("icd10");
		
		validGeoLevelSelect
			= GeoLevelSelect.newInstance("LEVEL2");
		invalidGeoLevelSelect
			= GeoLevelSelect.newInstance(null);
		nonExistentGeoLevelSelect
			= GeoLevelSelect.newInstance("non-existent level");

		validGeoLevelToMap
			= GeoLevelToMap.newInstance("LEVEL4");
		invalidGeoLevelToMap
			= GeoLevelToMap.newInstance(null);
		nonExistentGeoLevelToMap
			= GeoLevelToMap.newInstance("non-existent level");
		
		try {
			initialiseService();
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TestCovariateFeatures", rifServiceException);
		}
	
	}

	
	@Before
	public void setUp() {
		try {
			rifServiceBundle.login("keving", new String("a").toCharArray());			
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
	 * Gets the covariates n1.
	 *
	 * @return the covariates n1
	 */
	@Test
	public void getCovariatesN1() {
		try {
			ArrayList<AbstractCovariate> results
				= rifStudySubmissionService.getCovariates(
					testUser, 
					sahsuGeography,
					validGeoLevelSelect,
					validGeoLevelToMap);
			assertEquals(4, results.size());
						
			AbstractCovariate firstCovariate
				= results.get(0);
			assertEquals("areatri1km", firstCovariate.getName());
			
			AbstractCovariate lastCovariate
				= results.get(results.size() - 1);
			assertEquals("tri_1km", lastCovariate.getName());	
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	/**
	 * Gets the covariates e1.
	 *
	 * @return the covariates e1
	 */
	@Test
	/**
	 * check null parameters
	 */
	public void getCovariatesE1() {
		try {
			rifStudySubmissionService.getCovariates(
				null, 
				sahsuGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NULL_API_METHOD_PARAMETER, 
				1);
		}		

		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				null,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NULL_API_METHOD_PARAMETER, 
				1);
		}		
	
		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				sahsuGeography,
				null,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NULL_API_METHOD_PARAMETER, 
				1);
		}		
	
		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				sahsuGeography,
				validGeoLevelSelect,
				null);
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
	 * Gets the covariates e2.
	 *
	 * @return the covariates e2
	 */
	@Test
	/**
	 * check for invalid parameters
	 */
	public void getCovariatesE2() {
		try {
			System.out.println("TestCovariates - getCovariatesE2 START");
			rifStudySubmissionService.getCovariates(
				invalidUser, 
				sahsuGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_USER, 
				1);
		}		

		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				invalidGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOGRAPHY, 
				1);
		}		
	
		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				sahsuGeography,
				invalidGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_SELECT, 
				1);
		}		
	
		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				sahsuGeography,
				validGeoLevelSelect,
				invalidGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_TO_MAP, 
				1);
		}			
	}

	/**
	 * Gets the covariates e3.
	 *
	 * @return the covariates e3
	 */
	@Test
	/**
	 * check for non-existent parameters
	 */
	public void getCovariatesE3() {
		try {
			rifStudySubmissionService.getCovariates(
				nonExistentUser, 
				sahsuGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}		

		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				nonExistentGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEOGRAPHY, 
				1);
		}		
	
		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				sahsuGeography,
				nonExistentGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE, 
				1);
		}		
	
		try {
			rifStudySubmissionService.getCovariates(
				testUser, 
				sahsuGeography,
				validGeoLevelSelect,
				nonExistentGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEOLEVEL_TO_MAP_VALUE, 
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
