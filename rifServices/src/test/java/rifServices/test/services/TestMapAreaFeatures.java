package rifServices.test.services;


import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.MapArea;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import static org.junit.Assert.*;

import java.util.ArrayList;

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

public class TestMapAreaFeatures extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	
	/** The valid user. */
	private User validUser;
	
	/** The invalid user. */
	private User invalidUser;
	
	/** The non existent user. */
	private User nonExistentUser;
	
	/** The sahsu geography. */
	private Geography sahsuGeography;
	
	/** The invalid geography. */
	private Geography invalidGeography;
	
	/** The non existent geography. */
	private Geography nonExistentGeography;

	/** The valid geo level select. */
	private GeoLevelSelect validGeoLevelSelect;
	
	/** The invalid geo level select. */
	private GeoLevelSelect invalidGeoLevelSelect;
	
	/** The non existent geo level select. */
	private GeoLevelSelect nonExistentGeoLevelSelect;
	
	/** The valid geo level area. */
	private GeoLevelArea validGeoLevelArea;
	
	/** The invalid geo level area. */
	private GeoLevelArea invalidGeoLevelArea;
	
	/** The non existent geo level area. */
	private GeoLevelArea nonExistentGeoLevelArea;
		
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
	 * Instantiates a new test map area features.
	 */
	public TestMapAreaFeatures() {
		validUser = User.newInstance("keving", "11.111.11.228");
		invalidUser = User.newInstance(null, "11.111.11.228");
		nonExistentUser = User.newInstance("xyzz", "11.111.11.228");
		
		sahsuGeography
			= Geography.newInstance("SAHSU", "stuff about sahsuland");
		invalidGeography
			= Geography.newInstance(null, "");
		nonExistentGeography
			= Geography.newInstance("Never ever land", "blah");

		validGeoLevelSelect
			= GeoLevelSelect.newInstance("LEVEL2");
		invalidGeoLevelSelect
			= GeoLevelSelect.newInstance("");
		nonExistentGeoLevelSelect
			= GeoLevelSelect.newInstance("non existent geolevel");		
		
		validGeoLevelArea
			= GeoLevelArea.newInstance();
		validGeoLevelArea.setIdentifier("01.004");
		validGeoLevelArea.setName("Hambly");	
		invalidGeoLevelArea = GeoLevelArea.newInstance();
		nonExistentGeoLevelArea
			= GeoLevelArea.newInstance();
		nonExistentGeoLevelArea.setIdentifier("zzz");
		nonExistentGeoLevelArea.setName("QQQ");
		
		validGeoLevelToMap 
			= GeoLevelToMap.newInstance("LEVEL4");
		invalidGeoLevelToMap
			= GeoLevelToMap.newInstance("");
		nonExistentGeoLevelToMap
			= GeoLevelToMap.newInstance("non existent geolevel");
		
		
		try {
			initialiseService();
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TestMapAreaFeatures", rifServiceException);
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
	
	
	
	/**
	 * Gets the map areas n1.
	 *
	 * @return the map areas n1
	 */
	@Test
	public void getMapAreasN1() {
		
		try {
			ArrayList<MapArea> mapAreas
				= rifStudySubmissionService.getMapAreas(
					validUser, 
					sahsuGeography,
					validGeoLevelSelect,
					validGeoLevelArea,
					validGeoLevelToMap);
			assertEquals(57, mapAreas.size());
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TRTRT", rifServiceException);
			fail();
		}
	}
	
	/**
	 * Gets the map areas n2.
	 *
	 * @return the map areas n2
	 */
	@Test
	public void getMapAreasN2() {
		
		try {
			ArrayList<MapArea> mapAreas
				= rifStudySubmissionService.getMapAreas(
					validUser, 
					sahsuGeography,
					validGeoLevelSelect,
					validGeoLevelArea,
					validGeoLevelToMap,
					1,
					20);
			assertEquals(20, mapAreas.size());
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TRTRT", rifServiceException);
			fail();
		}

	
		try {
			ArrayList<MapArea> mapAreas
				= rifStudySubmissionService.getMapAreas(
					validUser, 
					sahsuGeography,
					validGeoLevelSelect,
					validGeoLevelArea,
					validGeoLevelToMap,
					50,
					80);
			
			assertEquals(8, mapAreas.size());
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TRTRT", rifServiceException);
			fail();
		}
	
	
	
	
	}	
	
	
	/**
	 * Gets the map areas e1.
	 *
	 * @return the map areas e1
	 */
	@Test
	/**
	 * check null parameters
	 */
	public void getMapAreasE1() {
		
		try {
			rifStudySubmissionService.getMapAreas(
				null, 
				sahsuGeography,
				validGeoLevelSelect,
				validGeoLevelArea,					
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				null, 
				validGeoLevelSelect,
				validGeoLevelArea,					
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography,
				null,
				validGeoLevelArea,					
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography,
				validGeoLevelSelect,
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography, 
				validGeoLevelSelect,
				validGeoLevelArea,
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
	 * Gets the map areas e2.
	 *
	 * @return the map areas e2
	 */
	@Test
	/**
	 * check for invalid parameters
	 */
	public void getMapAreasE2() {
		
		try {
			rifStudySubmissionService.getMapAreas(
				invalidUser, 
				sahsuGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				invalidGeography, 
				validGeoLevelSelect,
				validGeoLevelArea,
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography,
				invalidGeoLevelSelect,
				validGeoLevelArea,
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography,
				validGeoLevelSelect,
				invalidGeoLevelArea,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_AREA, 
				1);
		}
		
		try {
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
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
	 * Gets the map areas e3.
	 *
	 * @return the map areas e3
	 */
	@Test
	/**
	 * check for non-existent parameter values
	 */
	public void getMapAreasE3() {
		
		try {
			rifStudySubmissionService.getMapAreas(
				nonExistentUser, 
				sahsuGeography, 
				validGeoLevelSelect,
				validGeoLevelArea,
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				nonExistentGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography, 
				nonExistentGeoLevelSelect,
				validGeoLevelArea,
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
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography, 
				validGeoLevelSelect,
				nonExistentGeoLevelArea,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEOLEVEL_AREA_VALUE, 
				1);
		}
				
		try {
			rifStudySubmissionService.getMapAreas(
				validUser, 
				sahsuGeography, 
				validGeoLevelSelect,
				validGeoLevelArea,
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
	// Section Accessors and Mutators
	// ==========================================

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
