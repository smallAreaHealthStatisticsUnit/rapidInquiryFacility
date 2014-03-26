package rifServices.test.businessConceptLayer;


import rifServices.businessConceptLayer.GeoLevelSelect;

import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;
import rifServices.system.RIFServiceError;
import rifServices.test.AbstractRIFTestCase;
import static org.junit.Assert.*;

import org.junit.Test;


/**
 * This test case covers valid GeoLevelSelect, GeoLevelArea, GeoLevelToMap, 
 * GeoLevelView classes. Note that test methods exercise features which 
 * do not require knowledge from the database.
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

public class TestGeoLevelClasses extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master geo level select. */
	private GeoLevelSelect masterGeoLevelSelect;
	
	/** The master geo level area. */
	private GeoLevelArea masterGeoLevelArea;
	
	/** The master geo level view. */
	private GeoLevelView masterGeoLevelView;
	
	/** The master geo level to map. */
	private GeoLevelToMap masterGeoLevelToMap;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test geo level classes.
	 */
	public TestGeoLevelClasses() {

		masterGeoLevelSelect = GeoLevelSelect.newInstance("LEVEL1", "level one");
		masterGeoLevelArea = GeoLevelArea.newInstance("Barnet", "The Barnett area");
		masterGeoLevelView = GeoLevelView.newInstance("LEVEL3", "level three");
		masterGeoLevelToMap = GeoLevelToMap.newInstance("LEVEL3", "level three");		
	}

	/**
	 * accept a valid geo level select with typical values.
	 */
	@Test
	public void acceptValidGeoLevelSelect() {

		try {
			GeoLevelSelect geoLevelSelect 
				= GeoLevelSelect.createCopy(masterGeoLevelSelect);
			geoLevelSelect.checkErrors();
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * A geo level select is invalid if it has a blank name.
	 */
	@Test
	public void rejectBlankNameInGeoLevelSelect() {

		try {
			GeoLevelSelect geoLevelSelect 
				= GeoLevelSelect.createCopy(masterGeoLevelSelect);
			geoLevelSelect.setName("");
			geoLevelSelect.checkErrors();
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
	 * accept a valid geo level select with typical values.
	 */
	@Test
	public void acceptValidGeoLevelArea() {

		try {
			GeoLevelArea geoLevelArea
				= GeoLevelArea.createCopy(masterGeoLevelArea);
			geoLevelArea.checkErrors();
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * A geo level area is invalid if it has a blank name.
	 */
	@Test
	public void validGeoLevelAreaE1() {

		try {
			GeoLevelArea geoLevelArea 
				= GeoLevelArea.createCopy(masterGeoLevelArea);
			geoLevelArea.setName(null);
			geoLevelArea.checkErrors();

			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_AREA, 
				1);
		}
	}
		
	/**
	 * accept a valid geo level view with typical values.
	 */
	@Test
	public void validGeoLevelViewN1() {

		try {
			GeoLevelView geoLevelView 
				= GeoLevelView.createCopy(masterGeoLevelView);
			geoLevelView.checkErrors();
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * A geo level view is invalid if it has a blank name.
	 */
	@Test
	public void validGeoLevelViewE1() {

		try {
			GeoLevelView geoLevelView 
				= GeoLevelView.createCopy(masterGeoLevelView);
			geoLevelView.setName("");
			geoLevelView.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_VIEW, 
				1);
		}
	}

	/**
	 * accept a valid geo-level-to-map with typical values.
	 */
	@Test
	public void validGeoLevelToMapN1() {

		try {
			GeoLevelToMap geoLevelToMap 
				= GeoLevelToMap.createCopy(masterGeoLevelToMap);
			geoLevelToMap.checkErrors();
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * A geo-level-to-map is invalid if it has a blank name.
	 */
	@Test
	public void validGeoLevelToMapE1() {

		try {
			GeoLevelToMap geoLevelToMap 
				= GeoLevelToMap.createCopy(masterGeoLevelToMap);
			geoLevelToMap.setName(null);
			geoLevelToMap.checkErrors();
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
	 * Test geo level select security violations.
	 */
	public void testGeoLevelSelectSecurityViolations() {

		GeoLevelSelect maliciousGeoLevelSelect
			= GeoLevelSelect.createCopy(masterGeoLevelSelect);
		maliciousGeoLevelSelect.setIdentifier(getTestMaliciousValue());
		try {
			maliciousGeoLevelSelect.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	
		maliciousGeoLevelSelect
			= GeoLevelSelect.createCopy(masterGeoLevelSelect);
		maliciousGeoLevelSelect.setName(getTestMaliciousValue());
		try {
			maliciousGeoLevelSelect.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousGeoLevelSelect
			= GeoLevelSelect.createCopy(masterGeoLevelSelect);
		maliciousGeoLevelSelect.setDescription(getTestMaliciousValue());
		try {
			maliciousGeoLevelSelect.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

	}
	
	/**
	 * Test geo level area security violations.
	 */
	@Test
	public void testGeoLevelAreaSecurityViolations() {	

		GeoLevelArea maliciousGeoLevelArea
			= GeoLevelArea.createCopy(masterGeoLevelArea);
		maliciousGeoLevelArea.setIdentifier(getTestMaliciousValue());
		try {
			maliciousGeoLevelArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousGeoLevelArea
			= GeoLevelArea.createCopy(masterGeoLevelArea);
		maliciousGeoLevelArea.setName(getTestMaliciousValue());
		try {
			maliciousGeoLevelArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	
		maliciousGeoLevelArea
			= GeoLevelArea.createCopy(masterGeoLevelArea);
		maliciousGeoLevelArea.setDescription(getTestMaliciousValue());
		try {
			maliciousGeoLevelArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	}
	
	/**
	 * Test geo level view security violations.
	 */
	@Test
	public void testGeoLevelViewSecurityViolations() {	
		
		GeoLevelView maliciousGeoLevelView
			= GeoLevelView.createCopy(masterGeoLevelView);
		maliciousGeoLevelView.setIdentifier(getTestMaliciousValue());
		try {
			maliciousGeoLevelView.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousGeoLevelView
			= GeoLevelView.createCopy(masterGeoLevelView);
		maliciousGeoLevelView.setName(getTestMaliciousValue());
		try {
			maliciousGeoLevelView.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	
		maliciousGeoLevelView
			= GeoLevelView.createCopy(masterGeoLevelView);
		maliciousGeoLevelView.setDescription(getTestMaliciousValue());
		try {
			maliciousGeoLevelView.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

	}
	
	/**
	 * Test geo level to map security violations.
	 */
	@Test
	public void testGeoLevelToMapSecurityViolations() {	

		GeoLevelToMap maliciousGeoLevelToMap
			= GeoLevelToMap.createCopy(masterGeoLevelToMap);
		maliciousGeoLevelToMap.setIdentifier(getTestMaliciousValue());
		try {
			maliciousGeoLevelToMap.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousGeoLevelToMap
			= GeoLevelToMap.createCopy(masterGeoLevelToMap);
		maliciousGeoLevelToMap.setName(getTestMaliciousValue());
		try {
			maliciousGeoLevelToMap.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	
		maliciousGeoLevelToMap
			= GeoLevelToMap.createCopy(masterGeoLevelToMap);
		maliciousGeoLevelToMap.setDescription(getTestMaliciousValue());
		try {
			maliciousGeoLevelToMap.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
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
