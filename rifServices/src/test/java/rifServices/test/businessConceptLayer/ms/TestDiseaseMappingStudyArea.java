package rifServices.test.businessConceptLayer.ms;


import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.businessConceptLayer.DiseaseMappingStudyArea;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.MapArea;
import rifServices.system.RIFServiceError;
import rifServices.dataStorageLayer.common.AbstractRIFTestCase;
import static org.junit.Assert.*;

import org.junit.Test;


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

public final class TestDiseaseMappingStudyArea 
	extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master disease mapping study area. */
	private DiseaseMappingStudyArea masterDiseaseMappingStudyArea;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test disease mapping study area.
	 */
	public TestDiseaseMappingStudyArea() {
		GeoLevelSelect geoLevelSelect = GeoLevelSelect.newInstance("LEVEL2");
		GeoLevelArea geoLevelArea = GeoLevelArea.newInstance("Camden");
		GeoLevelView geoLevelView = GeoLevelView.newInstance("LEVEL3");
		GeoLevelToMap geoLevelToMap = GeoLevelToMap.newInstance("LEVEL3");
		
		MapArea mapArea1 = MapArea.newInstance("111", "111", "Brent");
		MapArea mapArea2 = MapArea.newInstance("222", "222", "Barnet");

		masterDiseaseMappingStudyArea
			= DiseaseMappingStudyArea.newInstance();
		masterDiseaseMappingStudyArea.setGeoLevelSelect(geoLevelSelect);
		masterDiseaseMappingStudyArea.setGeoLevelArea(geoLevelArea);
		masterDiseaseMappingStudyArea.setGeoLevelView(geoLevelView);
		masterDiseaseMappingStudyArea.setGeoLevelToMap(geoLevelToMap);
		masterDiseaseMappingStudyArea.addMapArea(mapArea1);
		masterDiseaseMappingStudyArea.addMapArea(mapArea2);		
	}

	
// ==========================================
// Section Accessors and Mutators
// ==========================================

// ==========================================
// Section Errors and Validation
// ==========================================


	/**
	 * Valid comparison area.
	 */
	@Test
	public void acceptValidInstance_COMMON() {
		try {
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * error when comparison area has empty GeoLevelSelect.
	 */
	@Test	
	public void rejectBlankRequiredFields_ERROR() {
		
		//no geo level select specified
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			diseaseMappingStudyArea.setGeoLevelSelect(null);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}

		//no geo level area specified
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			diseaseMappingStudyArea.setGeoLevelArea(null);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}

		//no geo level view specified
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			diseaseMappingStudyArea.setGeoLevelView(null);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}		
		
		//no geo level to map specified
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			diseaseMappingStudyArea.setGeoLevelToMap(null);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}

		//no disease mapping study area specified
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			diseaseMappingStudyArea.setMapAreas(null);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}
	}
	
	/**
	 * error when comparison area has empty list of map areas.
	 */
	@Test	
	public void rejectEmptyMapAreas_ERROR() {
		
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			diseaseMappingStudyArea.clearMapAreas();
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}
	}	
	
	/**
	 * Duplicate map areas e1.
	 */
	@Test
	public void rejectDuplicateMapAreas_ERROR() {
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			
			MapArea duplicateMapArea1 = MapArea.newInstance("111", "111", "Brent");
			MapArea duplicateMapArea2 = MapArea.newInstance("222", "222", "Barnet");
			diseaseMappingStudyArea.addMapArea(duplicateMapArea1);
			diseaseMappingStudyArea.addMapArea(duplicateMapArea2);
			
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}		
	}
	
	/**
	 * check that error checking picks up errors in the geo level select
	 * object used by the comparison area.
	 */
	@Test	
	public void rejectInvalidGeoLevelValuesForComparisonArea_ERROR() {
		
		//reject invalid geo level select
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(null, "");
			diseaseMappingStudyArea.setGeoLevelSelect(geoLevelSelect);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}	
		
		//reject invalid geo level area
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			GeoLevelArea geoLevelArea
				= GeoLevelArea.newInstance(null, "");			
			diseaseMappingStudyArea.setGeoLevelArea(geoLevelArea);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}		
		
		//reject invalid geo level view
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			GeoLevelView geoLevelView
				= GeoLevelView.newInstance(null, "");
			diseaseMappingStudyArea.setGeoLevelView(geoLevelView);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}			

		//reject invalid geo level to map
		try {			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
			GeoLevelToMap geoLevelToMap
				= GeoLevelToMap.newInstance(null, "");
			diseaseMappingStudyArea.setGeoLevelToMap(geoLevelToMap);
			diseaseMappingStudyArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY_AREA, 
				1);
		}	
	}	
	
	/**
	 * Test security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		DiseaseMappingStudyArea maliciousDiseaseMappingArea
			= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
		maliciousDiseaseMappingArea.setIdentifier(getTestMaliciousValue());
		try {
			maliciousDiseaseMappingArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousDiseaseMappingArea
			= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
		GeoLevelSelect maliciousGeoLevelSelect
			= GeoLevelSelect.newInstance(getTestMaliciousValue(), "");
		maliciousDiseaseMappingArea.setGeoLevelSelect(maliciousGeoLevelSelect);
		try {
			maliciousDiseaseMappingArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		
		maliciousDiseaseMappingArea
			= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
		GeoLevelArea maliciousGeoLevelArea
			= GeoLevelArea.newInstance(getTestMaliciousValue(), "");
		maliciousDiseaseMappingArea.setGeoLevelArea(maliciousGeoLevelArea);
		try {
			maliciousDiseaseMappingArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		

		maliciousDiseaseMappingArea
			= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
		GeoLevelView maliciousGeoLevelView
			= GeoLevelView.newInstance(getTestMaliciousValue(), "");
		maliciousDiseaseMappingArea.setGeoLevelView(maliciousGeoLevelView);
		try {
			maliciousDiseaseMappingArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousDiseaseMappingArea
			= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
		GeoLevelToMap maliciousGeoLevelToMap
			= GeoLevelToMap.newInstance(getTestMaliciousValue(), "");
		maliciousDiseaseMappingArea.setGeoLevelToMap(maliciousGeoLevelToMap);
		try {
			maliciousDiseaseMappingArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	
		maliciousDiseaseMappingArea
			= DiseaseMappingStudyArea.copy(masterDiseaseMappingStudyArea);
		MapArea maliciousMapArea
			= MapArea.newInstance("666", "666", getTestMaliciousValue());
		maliciousDiseaseMappingArea.addMapArea(maliciousMapArea);
		try {
			maliciousDiseaseMappingArea.checkSecurityViolations();
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
