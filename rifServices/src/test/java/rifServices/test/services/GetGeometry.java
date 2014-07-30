package rifServices.test.services;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.MapArea;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

public class GetGeometry extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetGeometry() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	@Test
	public void getGeometry_COMMON1() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			String result
				= rifStudyRetrievalService.getGeometry(
					validUser, 
					validGeography,
					validGeoLevelSelect,
					validGeoLevelToMap,
					validMapAreas);
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}

	@Test
	public void getGeometry_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				emptyUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_USER, 
				1);
		}
	}
		
	@Test
	public void getGeometry_NULL1() {
		try {
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				null, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}
	
	@Test
	public void getGeometry_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				emptyGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOGRAPHY, 
				1);
		}
	}

	@Test
	public void getGeometry_NULL2() {
		try {
			User validUser = cloneValidUser();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				null,
				validGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}
	
	
	@Test
	public void getGeometry_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect emptyGeoLevelSelect = cloneEmptyGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				emptyGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_SELECT, 
				1);
		}
	}

	@Test
	public void getGeometry_NULL3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				null,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}

	@Test
	public void getGeometry_EMPTY4() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				emptyGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_TO_MAP, 
				1);
		}
	}
	
	
	@Test
	public void getGeometry_NULL4() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				null,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}
	
	@Test
	public void getGeometry_EMPTY5() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> emptyMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				emptyMapAreas);
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}

	@Test
	public void getGeometry_EMPTY51() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> emptyMapAreas = new ArrayList<MapArea>();
			MapArea emptyMapArea = cloneEmptyMapArea();
			emptyMapAreas.add(emptyMapArea);
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				emptyMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_MAP_AREA,
				1);
		}
	}
	
	@Test
	public void getGeometry_NULL5() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				null);
			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}
	
	@Test
	public void getGeometry_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				nonExistentUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}
	
	@Test
	public void getGeometry_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				nonExistentGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEOGRAPHY, 
				1);
		}
	}

	@Test
	public void getGeometry_NONEXISTENT3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect nonExistentGeoLevelSelect = cloneNonExistentGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				nonExistentGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE, 
				1);
		}
	}
	
	@Test
	public void getGeometry_NONEXISTENT4() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap nonExistentGeoLevelToMap = cloneNonExistentGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				nonExistentGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOLEVEL_TO_MAP_VALUE, 
				1);
		}
	}

	@Test
	public void getGeometry_NONEXISTENT5() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> nonExistentMapAreas = new ArrayList<MapArea>();
			nonExistentMapAreas.add(cloneNonExistentMapArea());
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				nonExistentMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_MAP_AREA, 
				1);
		}
	}
	
	@Test
	public void getGeometry_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				maliciousUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}
	
	@Test
	public void getGeometry_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				maliciousGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}

	@Test
	public void getGeometry_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect maliciousGeoLevelSelect = cloneMaliciousGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				maliciousGeoLevelSelect,
				validGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}
	
	@Test
	public void getGeometry_MALICIOUS4() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap maliciousGeoLevelToMap = cloneMaliciousGeoLevelToMap();
			ArrayList<MapArea> validMapAreas = new ArrayList<MapArea>();
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				maliciousGeoLevelToMap,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}

	@Test
	public void getGeometry_MALICIOUS5() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> maliciousMapAreas = new ArrayList<MapArea>();
			maliciousMapAreas.add(cloneMaliciousMapArea());
			
			rifStudyRetrievalService.getGeometry(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap,
				maliciousMapAreas);
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
