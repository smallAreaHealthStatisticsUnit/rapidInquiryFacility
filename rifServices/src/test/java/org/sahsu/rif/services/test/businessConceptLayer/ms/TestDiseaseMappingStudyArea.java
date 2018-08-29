package org.sahsu.rif.services.test.businessConceptLayer.ms;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.MapArea;
import org.sahsu.rif.services.concepts.StudyType;
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

public final class TestDiseaseMappingStudyArea extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master disease mapping study area. */
	private AbstractStudyArea masterStudyArea;
	
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

		masterStudyArea = AbstractStudyArea.newInstance(StudyType.DISEASE_MAPPING);
		masterStudyArea.setGeoLevelSelect(geoLevelSelect);
		masterStudyArea.setGeoLevelArea(geoLevelArea);
		masterStudyArea.setGeoLevelView(geoLevelView);
		masterStudyArea.setGeoLevelToMap(geoLevelToMap);
		masterStudyArea.addMapArea(mapArea1);
		masterStudyArea.addMapArea(mapArea2);		
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
			
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
			AbstractStudyArea diseaseMappingStudyArea
				= AbstractStudyArea.copy(masterStudyArea);
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
		AbstractStudyArea maliciousDiseaseMappingArea
			= AbstractStudyArea.copy(masterStudyArea);
		maliciousDiseaseMappingArea.setIdentifier(getTestMaliciousValue());
		try {
			maliciousDiseaseMappingArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousDiseaseMappingArea
			= AbstractStudyArea.copy(masterStudyArea);
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
			= AbstractStudyArea.copy(masterStudyArea);
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
			= AbstractStudyArea.copy(masterStudyArea);
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
			= AbstractStudyArea.copy(masterStudyArea);
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
			= AbstractStudyArea.copy(masterStudyArea);
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
