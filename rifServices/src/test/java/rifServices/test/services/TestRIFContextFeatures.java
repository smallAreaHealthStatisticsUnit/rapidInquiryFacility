package rifServices.test.services;



import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifServices.util.DisplayableItemSorter;
import rifServices.util.FieldValidationUtility;

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

public class TestRIFContextFeatures extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	/** The test user. */
	private User testUser;
	
	/** The non existent user. */
	private User nonExistentUser;
	
	/** The invalid user. */
	private User invalidUser;
	
	/** The malicious user. */
	private User maliciousUser;
	
	/** The sahsu geography. */
	private Geography sahsuGeography;
	
	/** The non existent geography. */
	private Geography nonExistentGeography;
	
	/** The invalid geography. */
	private Geography invalidGeography;
	
	/** The malicious geography. */
	private Geography maliciousGeography;
	
	/** The valid sahsu geo level select value. */
	private GeoLevelSelect validSAHSUGeoLevelSelectValue;
	
	/** The non existent geo level select value. */
	private GeoLevelSelect nonExistentGeoLevelSelectValue;
	
	/** The invalid geo level select value. */
	private GeoLevelSelect invalidGeoLevelSelectValue;
	
	/** The cancer health theme. */
	private HealthTheme cancerHealthTheme;
	
	/** The non existent health theme. */
	private HealthTheme nonExistentHealthTheme;
	
	/** The invalid health theme. */
	private HealthTheme invalidHealthTheme;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test rif context features.
	 */
	public TestRIFContextFeatures() {

		FieldValidationUtility fieldValidationUtility 
			= new FieldValidationUtility();
		String maliciousFieldValue 
			= fieldValidationUtility.getTestMaliciousFieldValue();

		sahsuGeography
			= Geography.newInstance("SAHSU", "stuff about sahsuland");
		nonExistentGeography
			= Geography.newInstance("NeverEverLand", "something something");
		invalidGeography
			= Geography.newInstance("", "");
		maliciousGeography
			= Geography.newInstance(maliciousFieldValue, "");
		
		cancerHealthTheme
			= HealthTheme.newInstance("SAHSULAND",  "SAHSU land cancer incidence example data");
		nonExistentHealthTheme
			= HealthTheme.newInstance("Blah", "non-existent health theme");
		invalidHealthTheme
			= HealthTheme.newInstance("");
		
		validSAHSUGeoLevelSelectValue
			= GeoLevelSelect.newInstance("LEVEL2");
		nonExistentGeoLevelSelectValue
			= GeoLevelSelect.newInstance("Blah-de-blah");
		invalidGeoLevelSelectValue
			= GeoLevelSelect.newInstance(null);
				
		testUser = User.newInstance("kgarwood", "11.111.11.228");
		nonExistentUser = User.newInstance("nobody", "11.111.11.228");
		invalidUser = User.newInstance(null, "11.111.11.228");
		maliciousUser = User.newInstance(maliciousFieldValue, "11.111.11.228");
		
		
		try {
			initialiseService();
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TestRIFContextFeatures", rifServiceException);
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
	 * Gets the geographies n1.
	 *
	 * @return the geographies n1
	 */
	@Test
	public void getGeographiesN1() {
		try {
			ArrayList<Geography> geographies
				= rifStudySubmissionService.getGeographies(testUser);
			assertEquals(3, geographies.size());
			
			DisplayableItemSorter sorter = new DisplayableItemSorter();
			for (Geography geography : geographies) {
				sorter.addDisplayableListItem(geography);
			}			

			Geography sahsuGeography
				= (Geography) sorter.sortList().get(1);
			assertEquals("SAHSU", sahsuGeography.getName());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}
	
	/**
	 * Gets the geographies e1.
	 *
	 * @return the geographies e1
	 */
	@Test
	/**
	 * check null method parameters
	 */
	public void getGeographiesE1() {
		try {
			rifStudySubmissionService.getGeographies(null);
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
	 * Gets the geographies e2.
	 *
	 * @return the geographies e2
	 */
	@Test
	/**
	 * check invalid parameters
	 */
	public void getGeographiesE2() {
		try {
			rifStudySubmissionService.getGeographies(invalidUser);
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
	 * Gets the geographies e3.
	 *
	 * @return the geographies e3
	 */
	@Test
	/**
	 * check non-existent parameters
	 */
	public void getGeographiesE3() {
		try {
			rifStudySubmissionService.getGeographies(nonExistentUser);
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
	 * Gets the geographies s1.
	 *
	 * @return the geographies s1
	 */
	@Test
	/**
	 * check non-existent parameters
	 */
	public void getGeographiesS1() {
		try {
			rifStudySubmissionService.getGeographies(maliciousUser);
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
	 * Gets the health themes n1.
	 *
	 * @return the health themes n1
	 */
	@Test
	public void getHealthThemesN1() {
		try {
			ArrayList<HealthTheme> healthThemes
				= rifStudySubmissionService.getHealthThemes(testUser, sahsuGeography);
			//there should be one health theme
			HealthTheme sahsuCancerTheme = healthThemes.get(0);

			assertEquals(
				"SAHSULAND",
				sahsuCancerTheme.getName());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}
	
	/**
	 * Gets the health themes e1.
	 *
	 * @return the health themes e1
	 */
	@Test
	/**
	 * check for null method parameters
	 */
	public void getHealthThemesE1() {
		try {
			rifStudySubmissionService.getHealthThemes(null, sahsuGeography);
			//there should be one health theme
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
		
		try {
			rifStudySubmissionService.getHealthThemes(testUser, null);
			//there should be one health theme
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
	 * Gets the health themes e2.
	 *
	 * @return the health themes e2
	 */
	@Test
	/**
	 * Test invalid parameters
	 */
	public void getHealthThemesE2() {
		try {

			rifStudySubmissionService.getHealthThemes(invalidUser, sahsuGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
				1);
		}		
		
		try {
			rifStudySubmissionService.getHealthThemes(testUser, invalidGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);
		}		
	}
	
	/**
	 * Gets the health themes e3.
	 *
	 * @return the health themes e3
	 */
	@Test
	/**
	 * Test non-existent parameters
	 */
	public void getHealthThemesE3() {
		try {

			rifStudySubmissionService.getHealthThemes(nonExistentUser, sahsuGeography);
			//there should be one health theme
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}		
		
		try {
			rifStudySubmissionService.getHealthThemes(testUser, nonExistentGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}		
	}

	/**
	 * Gets the health themes s1.
	 *
	 * @return the health themes s1
	 */
	@Test
	/**
	 * check for security violations
	 */
	public void getHealthThemesS1() {
		try {
			rifStudySubmissionService.getHealthThemes(maliciousUser, sahsuGeography);
			//there should be one health theme
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			System.out.println("TestRIFContextFeatures 3");
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}		
	}
	
	@Test
	public void getHealthThemesS2() {
		try {
			rifStudySubmissionService.getHealthThemes(testUser, maliciousGeography);
			//there should be one health theme
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
	 * Gets the numerator denominator pair n1.
	 *
	 * @return the numerator denominator pair n1
	 */
	@Test
	public void getNumeratorDenominatorPairN1() {
		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				testUser, 
				sahsuGeography, 
				cancerHealthTheme);	
		}
		catch(RIFServiceException rifServiceException) {
			printErrors("getNumeratorDenomPairN1", rifServiceException);
			fail();
		}				
	}
	
	/**
	 * Gets the numerator denominator pair e1.
	 *
	 * @return the numerator denominator pair e1
	 */
	@Test
	/**
	 * check null method parameters
	 */
	public void getNumeratorDenominatorPairE1() {
		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				null, 
				sahsuGeography, 
				cancerHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
		
		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				testUser, 
				null, 
				cancerHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
		
		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				testUser, 
				sahsuGeography, 
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
	 * Gets the numerator denominator pair e2.
	 *
	 * @return the numerator denominator pair e2
	 */
	@Test
	/**
	 * Checking invalid parameters
	 */
	public void getNumeratorDenominatorPairE2() {
		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				invalidUser, 
				sahsuGeography, 
				cancerHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
				1);
		}

		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				testUser, 
				invalidGeography, 
				cancerHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);
		}
	
		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				testUser, 
				sahsuGeography, 
				invalidHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_HEALTH_THEME,
				1);
		}
	}	
	
	
	
	/**
	 * Gets the numerator denominator pair e3.
	 *
	 * @return the numerator denominator pair e3
	 */
	@Test
	/**
	 * Checking nonexistent parameters
	 */
	public void getNumeratorDenominatorPairE3() {
		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				nonExistentUser, 
				sahsuGeography, 
				cancerHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}

		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				testUser, 
				nonExistentGeography, 
				cancerHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}
	
		try {
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				testUser, 
				sahsuGeography, 
				nonExistentHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_HEALTH_THEME,
				1);
		}
	}	

		
	/**
	 * Gets the geo level select values n1.
	 *
	 * @return the geo level select values n1
	 */
	@Test
	public void getGeoLevelSelectValuesN1() {
		try {
			ArrayList<Geography> geographies
				= rifStudySubmissionService.getGeographies(testUser);
			assertEquals(3, geographies.size());
			
			DisplayableItemSorter sorter = new DisplayableItemSorter();
			for (Geography geography : geographies) {
				sorter.addDisplayableListItem(geography);
			}
			//Three geographies should be returned
			//EW01
			//SAHSU
			//UK91

			//the second one should be "SAHSU"
			Geography sahsuGeography 
				= (Geography) sorter.sortList().get(1);
			assertEquals("SAHSU", sahsuGeography.getDisplayName());
			
			ArrayList<GeoLevelSelect> geoLevelSelectValues
				= rifStudySubmissionService.getGeographicalLevelSelectValues(
					testUser, 
					sahsuGeography);
				
			//The list of all geo levels is: LEVEL1, LEVEL2, LEVEL3, LEVEL4
			//But we will exclude LEVEL4.  Otherwise, when GeoLevelSelect is LEVEL4
			//it will be impossible to choose a GeoLevelView or GeoLevelToMap
			//value.  Both of these values must be at least one level lower
			assertEquals(3, geoLevelSelectValues.size());
			
			//first one should be LEVEL1
			GeoLevelSelect firstValue = geoLevelSelectValues.get(0);
			assertEquals("LEVEL1", firstValue.getName().toUpperCase());
						
			//last one should be LEVEL3
			GeoLevelSelect lastValue = geoLevelSelectValues.get(2);
			assertEquals("LEVEL3", lastValue.getName().toUpperCase());		
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
		
	/**
	 * Gets the geo level select value e1.
	 *
	 * @return the geo level select value e1
	 */
	@Test
	/**
	 * check null parameters
	 */
	public void getGeoLevelSelectValueE1() {
		try {
			rifStudySubmissionService.getGeographicalLevelSelectValues(
				null, 
				sahsuGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER, 
				1);		
		}

		try {
			rifStudySubmissionService.getGeographicalLevelSelectValues(
				testUser, 
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
	 * Gets the geo level select values e2.
	 *
	 * @return the geo level select values e2
	 */
	@Test
	/**
	 * check for invalid method parameters
	 */
	public void getGeoLevelSelectValuesE2() {
		
		//try to get results using invalid user
		try {
			rifStudySubmissionService.getGeographicalLevelSelectValues(
				invalidUser, 
				sahsuGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER, 
				1);
		}
		
		//try to get results using invalid user
		try {
			rifStudySubmissionService.getGeographicalLevelSelectValues(
				testUser, 
				invalidGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY, 
				1);
		}
	}

	/**
	 * Gets the geo level select values e3.
	 *
	 * @return the geo level select values e3
	 */
	@Test
	/**
	 * check for non-existent parameters
	 */
	public void getGeoLevelSelectValuesE3() {
		
		//try to get results using invalid user
		try {
			rifStudySubmissionService.getGeographicalLevelSelectValues(
				nonExistentUser, 
				sahsuGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
		
		//try to get results using invalid user
		try {
			rifStudySubmissionService.getGeographicalLevelSelectValues(
				testUser, 
				nonExistentGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY, 
				1);
		}
	}
	
	/**
	 * Gets the default geo level select value n1.
	 *
	 * @return the default geo level select value n1
	 */
	@Test
	public void getDefaultGeoLevelSelectValueN1() {
		try {
			
			GeoLevelSelect defaultSelectValue
				= rifStudySubmissionService.getDefaultGeoLevelSelectValue(
					testUser, 
					sahsuGeography);
			assertEquals("LEVEL2", defaultSelectValue.getName());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	/**
	 * Gets the default geo level select value e1.
	 *
	 * @return the default geo level select value e1
	 */
	@Test
	/**
	 * check null parameter values
	 */
	public void getDefaultGeoLevelSelectValueE1() {

		try {
			rifStudySubmissionService.getDefaultGeoLevelSelectValue(
				null, 
				sahsuGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}

		try {			
			rifStudySubmissionService.getDefaultGeoLevelSelectValue(
				testUser, 
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
	 * Gets the default geo level select value e2.
	 *
	 * @return the default geo level select value e2
	 */
	@Test
	/**
	 * check invalid parameter values
	 */
	public void getDefaultGeoLevelSelectValueE2() {
		try {
			
			rifStudySubmissionService.getDefaultGeoLevelSelectValue(
				invalidUser, 
				sahsuGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("WER", rifServiceException);
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
				1);
		}		

		try {
			
			rifStudySubmissionService.getDefaultGeoLevelSelectValue(
				testUser, 
				invalidGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);
		}
	}
	

	/**
	 * Gets the default geo level select value e3.
	 *
	 * @return the default geo level select value e3
	 */
	@Test
	/**
	 * check non-existent parameter values
	 */
	public void getDefaultGeoLevelSelectValueE3() {
		try {
			
			rifStudySubmissionService.getDefaultGeoLevelSelectValue(
				nonExistentUser, 
				sahsuGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}
		
		try {
				rifStudySubmissionService.getDefaultGeoLevelSelectValue(
					testUser, 
					nonExistentGeography);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}				
	}
	
	/**
	 * Gets the geo level areas values n1.
	 *
	 * @return the geo level areas values n1
	 */
	@Test
	public void getGeoLevelAreasValuesN1() {
		try {
			ArrayList<GeoLevelArea> geoLevelAreaValues
				= rifStudySubmissionService.getGeoLevelAreaValues(
					testUser, 
					sahsuGeography, 
					validSAHSUGeoLevelSelectValue);
			assertEquals(17, geoLevelAreaValues.size());
			GeoLevelArea firstResult = geoLevelAreaValues.get(0);
			assertEquals("Abellan", firstResult.getName());
			
			GeoLevelArea lastResult = geoLevelAreaValues.get(16);
			assertEquals("Tirado", lastResult.getName());
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("RRR", rifServiceException);
			fail();
		}
	}

	/**
	 * Gets the geo level areas values e1.
	 *
	 * @return the geo level areas values e1
	 */
	@Test
	/**
	 * check for null parameter values
	 */
	public void getGeoLevelAreasValuesE1() {
		
		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				null, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
		
		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				testUser, 
				null, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.NULL_API_METHOD_PARAMETER,
					1);
		}
		
		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				testUser, 
				sahsuGeography, 
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
	 * Gets the geo level areas values e2.
	 *
	 * @return the geo level areas values e2
	 */
	@Test
	/**
	 * Test invalid method parameters
	 */
	public void getGeoLevelAreasValuesE2() {

		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				invalidUser, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
				1);	
		}
		
		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				testUser, 
				invalidGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);	
		}

		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				testUser, 
				sahsuGeography, 
				invalidGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOLEVEL_SELECT,
				1);	
		}
	}
	
	/**
	 * Gets the geo level areas values e3.
	 *
	 * @return the geo level areas values e3
	 */
	@Test
	/**
	 * Test non-existent parameters
	 */
	public void getGeoLevelAreasValuesE3() {

		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				nonExistentUser, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);	
		}
		
		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				testUser, 
				nonExistentGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);	
		}

		try {
			rifStudySubmissionService.getGeoLevelAreaValues(
				testUser, 
				sahsuGeography, 
				nonExistentGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE,
				1);	
		}
	}
	

	
	/**
	 * Gets the geo level view values n1.
	 *
	 * @return the geo level view values n1
	 */
	@Test
	public void getGeoLevelViewValuesN1() {
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * Gets the geo level view values e1.
	 *
	 * @return the geo level view values e1
	 */
	@Test
	/**
	 * check null parameters
	 */
	public void getGeoLevelViewValuesE1() {
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				null, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}

		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				null, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
		
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				sahsuGeography, 
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
	 * Gets the geo level view values e2.
	 *
	 * @return the geo level view values e2
	 */
	@Test
	/**
	 * check invalid parameters
	 */
	public void getGeoLevelViewValuesE2() {
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				invalidUser, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
				1);
		}

		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				invalidGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);
		}
		
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				sahsuGeography, 
				invalidGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOLEVEL_SELECT,
				1);
		}
	}
	
	/**
	 * Gets the geo level view values e3.
	 *
	 * @return the geo level view values e3
	 */
	@Test
	/**
	 * check non-existent parameters
	 */
	public void getGeoLevelViewValuesE3() {
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				nonExistentUser, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}

		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				nonExistentGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}
		
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				sahsuGeography, 
				nonExistentGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE,
				1);
		}		
	}
	
	/**
	 * Gets the geo level to map values n1.
	 *
	 * @return the geo level to map values n1
	 */
	@Test
	public void getGeoLevelToMapValuesN1() {
		try {
			rifStudySubmissionService.getGeoLevelToMapValues(
				testUser, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}

	/**
	 * Gets the geo level to map values e1.
	 *
	 * @return the geo level to map values e1
	 */
	@Test
	/**
	 * check null parameters
	 */
	public void getGeoLevelToMapValuesE1() {
		try {
			rifStudySubmissionService.getGeoLevelToMapValues(
				null, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}

		try {
			rifStudySubmissionService.getGeoLevelToMapValues(
				testUser, 
				null, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NULL_API_METHOD_PARAMETER,
				1);
		}
		
		try {
			rifStudySubmissionService.getGeoLevelToMapValues(
				testUser, 
				sahsuGeography, 
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
	 * Gets the geo level to map values e2.
	 *
	 * @return the geo level to map values e2
	 */
	@Test
	/**
	 * check invalid parameters
	 */
	public void getGeoLevelToMapValuesE2() {
		try {
			rifStudySubmissionService.getGeoLevelToMapValues(
				invalidUser, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER,
				1);
		}

		try {
			rifStudySubmissionService.getGeoLevelToMapValues(
				testUser, 
				invalidGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);
		}
		
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				sahsuGeography, 
				invalidGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOLEVEL_SELECT,
				1);
		}
	}
	
	/**
	 * Gets the geo level to map values e3.
	 *
	 * @return the geo level to map values e3
	 */
	@Test
	/**
	 * check non-existent parameters
	 */
	public void getGeoLevelToMapValuesE3() {
		try {
			rifStudySubmissionService.getGeoLevelToMapValues(
				nonExistentUser, 
				sahsuGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}

		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				nonExistentGeography, 
				validSAHSUGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}
		
		try {
			rifStudySubmissionService.getGeoLevelViewValues(
				testUser, 
				sahsuGeography, 
				nonExistentGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE,
				1);
		}		
	}
		
	private static ArrayList<AbstractCovariate> sortCovariates(
			final ArrayList<AbstractCovariate> covariates) {
			DisplayableItemSorter sorter = new DisplayableItemSorter();
				
			for (AbstractCovariate covariate : covariates) {
				sorter.addDisplayableListItem(covariate);
			}
				
			ArrayList<AbstractCovariate> results = new ArrayList<AbstractCovariate>();
			ArrayList<String> identifiers = sorter.sortIdentifiersList();
			for (String identifier : identifiers) {
				AbstractCovariate sortedCovariate 
					= (AbstractCovariate) sorter.getItemFromIdentifier(identifier);
				results.add(sortedCovariate);
			}
				
			return results;
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
