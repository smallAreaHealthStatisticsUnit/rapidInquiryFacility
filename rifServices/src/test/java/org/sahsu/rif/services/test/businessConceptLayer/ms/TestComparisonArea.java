package org.sahsu.rif.services.test.businessConceptLayer.ms;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.MapArea;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.test.AbstractRIFTestCase;

import static org.junit.Assert.fail;

/**
 * This test case only tests the properties of comparison area that do
 * not require using the database.  For example, the tests will check whether fields
 * are null but not whether a GeoLevelSelect, GeoLevelArea exists in the database.
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public final class TestComparisonArea
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master comparison area. */
	private ComparisonArea masterComparisonArea;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test comparison area.
	 */
	public TestComparisonArea() {
		GeoLevelSelect geoLevelSelect = GeoLevelSelect.newInstance("LEVEL2");
		GeoLevelArea geoLevelArea = GeoLevelArea.newInstance("Camden");
		GeoLevelView geoLevelView = GeoLevelView.newInstance("LEVEL3");
		GeoLevelToMap geoLevelToMap = GeoLevelToMap.newInstance("LEVEL3");
		
		MapArea mapArea1 = MapArea.newInstance("111", "111", "Brent", 0);
		MapArea mapArea2 = MapArea.newInstance("222", "222", "Barnet", 0);

		masterComparisonArea = ComparisonArea.newInstance();
		masterComparisonArea.setGeoLevelSelect(geoLevelSelect);
		masterComparisonArea.setGeoLevelArea(geoLevelArea);
		masterComparisonArea.setGeoLevelView(geoLevelView);
		masterComparisonArea.setGeoLevelToMap(geoLevelToMap);
		masterComparisonArea.addMapArea(mapArea1);
		masterComparisonArea.addMapArea(mapArea2);		
	}

	// =========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	/**
	 * Accept valid comparison area.
	 */
	@Test
	/**
	 * Accept a comparison area with typical values.
	 */
	public void acceptValidInstance_COMMON() {
		try {
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			comparisonArea.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}

	
	/**
	 * Reject blank geo level select.
	 */
	@Test
	/**
	 * A comparison area is invalid if no geo level select is specified
	 */
	public void rejectBlankRequiredFields_ERROR() {

		//no geo level select specified
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			comparisonArea.setGeoLevelSelect(null);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_COMPARISON_AREA,
					1);
		}		
		
		//no geo level area specified
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			comparisonArea.setGeoLevelArea(null);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}	
		
		//no geo level view specified
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			comparisonArea.setGeoLevelView(null);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}		
		
		
		//no geo level to map specified
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			comparisonArea.setGeoLevelToMap(null);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}
	}
	
	/**
	 * Reject empty map area list.
	 */
	@Test
	/**
	 * A comparison area is invalid if its list of map areas is either
	 * null or empty.
	 */
	public void rejectEmptyMapAreaList_ERROR() {
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			comparisonArea.setMapAreas(null);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}
		
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			comparisonArea.clearMapAreas();
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}
	}	

	/**
	 * Reject duplicate map areas.
	 */
	@Test
	/**
	 * A comparison area is invalid if it contains any duplicate map areas.
	 */
	public void rejectDuplicateMapAreas_ERROR() {
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			
			MapArea duplicateMapArea1 = MapArea.newInstance("111", "111", "Brent", 0);
			MapArea duplicateMapArea2 = MapArea.newInstance("222", "222", "Barnet", 0);
			comparisonArea.addMapArea(duplicateMapArea1);
			comparisonArea.addMapArea(duplicateMapArea2);			
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}		
	}
	
	/**
	 * A comparison area is invalid if it has an invalid geo level select.
	 */
	@Test	
	public void rejectInvalidGeoLevelValue_ERROR() {
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(null, "");
			comparisonArea.setGeoLevelSelect(geoLevelSelect);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}	
	}	
	
	/**
	 * A comparison area is invalid if it has an invalid geo level area.
	 */
	@Test
	public void rejectInvalidGeoLevelArea_ERROR() {
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			GeoLevelArea geoLevelArea
				= GeoLevelArea.newInstance(null, "");			
			comparisonArea.setGeoLevelArea(geoLevelArea);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}		
	}
	
	/**
	 * A comparison area is invalid if it has an invalid geo level view.
	 */
	@Test
	public void rejectInvalidGeoLevelView_ERROR() {
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			GeoLevelView geoLevelView
				= GeoLevelView.newInstance(null, "");
			comparisonArea.setGeoLevelView(geoLevelView);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}		
	}	
	
	/**
	 * A comparison area is invalid if it has an invalid geo-level-to-map.
	 */
	@Test
	public void rejectInvalidGeoLevelToMap_ERROR() {
		try {			
			ComparisonArea comparisonArea
				= ComparisonArea.createCopy(masterComparisonArea);
			GeoLevelToMap geoLevelToMap
				= GeoLevelToMap.newInstance(null, "");
			comparisonArea.setGeoLevelToMap(geoLevelToMap);
			comparisonArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_COMPARISON_AREA, 
				1);
		}		
	}
	
	/**
	 * Test security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		ComparisonArea maliciousComparisonArea
			= ComparisonArea.createCopy(masterComparisonArea);
		maliciousComparisonArea.setIdentifier(getTestMaliciousValue());
		try {
			maliciousComparisonArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		/*
		 * test that security violations checked recursively on child objects
		 */
		maliciousComparisonArea
			= ComparisonArea.createCopy(masterComparisonArea);
		GeoLevelSelect maliciousGeoLevelSelect
			= maliciousComparisonArea.getGeoLevelSelect();
		maliciousGeoLevelSelect.setDescription(getTestMaliciousValue());		
		try {
			maliciousComparisonArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}		
		
		maliciousComparisonArea
			= ComparisonArea.createCopy(masterComparisonArea);
		GeoLevelArea maliciousGeoLevelArea
			= maliciousComparisonArea.getGeoLevelArea();
		maliciousGeoLevelArea.setDescription(getTestMaliciousValue());
		try {
			maliciousComparisonArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}		

		maliciousComparisonArea
			= ComparisonArea.createCopy(masterComparisonArea);
		GeoLevelView maliciousGeoLevelView
			= maliciousComparisonArea.getGeoLevelView();
		maliciousGeoLevelView.setIdentifier(getTestMaliciousValue());
		try {
			maliciousComparisonArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}		
		
		maliciousComparisonArea
			= ComparisonArea.createCopy(masterComparisonArea);
		GeoLevelToMap maliciousGeoLevelToMap
			= maliciousComparisonArea.getGeoLevelToMap();
		maliciousGeoLevelToMap.setName(getTestMaliciousValue());
		try {
			maliciousComparisonArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}		
				
		maliciousComparisonArea
			= ComparisonArea.createCopy(masterComparisonArea);
		MapArea maliciousMapArea
			= MapArea.newInstance("234", "234", getTestMaliciousValue(), 0);
		maliciousComparisonArea.addMapArea(maliciousMapArea);
		try {
			maliciousComparisonArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
