package rifServices.test.services;


import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.MapArea;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceError;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import java.util.ArrayList;


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

public final class GetMapAreasByBlock 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetMapAreasByBlock() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
		
	@Test
	public void getMapAreasByBlock_COMMON1() {
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			ArrayList<MapArea> mapAreas
				= rifStudySubmissionService.getMapAreasByBlock(
					validUser, 
					validGeography,
					validGeoLevelSelect,
					validGeoLevelArea,
					validGeoLevelToMap,
					1,
					20);
			assertEquals(20, mapAreas.size());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}

	}

	@Test
	public void getMapAreasByBlock_COMMON2() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			ArrayList<MapArea> mapAreas
				= rifStudySubmissionService.getMapAreasByBlock(
					validUser, 
					validGeography,
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

	
	@Test
	/**
	 * Range start and end have the same value
	 */
	public void getMapAreasByBlock_UNCOMMON1() {
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			ArrayList<MapArea> mapAreas
				= rifStudySubmissionService.getMapAreasByBlock(
					validUser, 
					validGeography,
					validGeoLevelSelect,
					validGeoLevelArea,
					validGeoLevelToMap,
					50,
					80);
			
			assertEquals(8, mapAreas.size());
			
			//@TODO - fix this
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TRTRT", rifServiceException);
			fail();
		}
	}	
	
	
	@Test
	public void getMapAreasByBlock_NULL1() {
	
		try {
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				null, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				50,
				80);
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
	public void getMapAreasByBlock_EMPTY1() {
	
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				emptyUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				50,
				80);
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
	public void getMapAreasByBlock_NULL2() {
	
		try {
			User validUser = cloneValidUser();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				null,
				validGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				50,
				80);
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
	public void getMapAreasByBlock_EMPTY2() {
	
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				emptyGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				50,
				80);
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
	public void getMapAreasByBlock_NULL3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				null,
				validGeoLevelArea,
				validGeoLevelToMap,
				50,
				80);
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
	public void getMapAreasByBlock_EMPTY3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect emptyGeoLevelSelect = cloneEmptyGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				emptyGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				50,
				80);
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
	public void getMapAreasByBlock_NULL4() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				null,
				validGeoLevelToMap,
				50,
				80);
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
	public void getMapAreasByBlock_EMPTY4() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea emptyGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				emptyGeoLevelArea,
				validGeoLevelToMap,
				50,
				80);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOLEVEL_AREA,
				1);
		}
	}	

	@Test
	public void getMapAreasByBlock_NULL5() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				null,
				50,
				80);
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
	public void getMapAreasByBlock_EMPTY5() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				emptyGeoLevelToMap,
				50,
				80);
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
	public void getMapAreasByBlock_NULL6() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				emptyGeoLevelToMap,
				null,
				80);
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
	public void getMapAreasByBlock_NULL7() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				emptyGeoLevelToMap,
				50,
				null);
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
	/**
	 * negative lower value
	 */
	public void getMapAreasByBlock_RANGE1() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				emptyGeoLevelToMap,
				-1,
				80);
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
	/**
	 * negative upper value
	 */
	public void getMapAreasByBlock_RANGE2() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				emptyGeoLevelToMap,
				50,
				-1);
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
	/**
	 * lower bound value is more than upper bound value
	 */
	public void getMapAreasByBlock_RANGE3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				emptyGeoLevelToMap,
				50,
				40);
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
	public void getMapAreasByBlock_NONEXISTENT1() {
	
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				nonExistentUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				1,
				20);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}	

	
	@Test
	public void getMapAreasByBlock_NONEXISTENT2() {
	
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				nonExistentGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				1,
				20);
				
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
	public void getMapAreasByBlock_NONEXISTENT3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect nonExistentGeoLevelSelect = cloneNonExistentGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				nonExistentGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				1,
				20);
				
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
	public void getMapAreasByBlock_NONEXISTENT4() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea nonExistentGeoLevelArea = cloneNonExistentGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				nonExistentGeoLevelArea,
				validGeoLevelToMap,
				1,
				20);
				
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOLEVEL_AREA_VALUE,
				1);
		}
	}	

	
	@Test
	public void getMapAreasByBlock_NONEXISTENT5() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap nonExistentGeoLevelToMap = cloneNonExistentGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				nonExistentGeoLevelToMap,
				1,
				20);
				
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
	public void getMapAreasByBlock_MALICIOUS1() {
	
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				maliciousUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				1,
				20);
				
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}	

	@Test
	public void getMapAreasByBlock_MALICIOUS2() {
	
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				maliciousGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				1,
				20);
				
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}	
	
	@Test
	public void getMapAreasByBlock_MALICIOUS3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect maliciousGeoLevelSelect = cloneMaliciousGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				maliciousGeoLevelSelect,
				validGeoLevelArea,
				validGeoLevelToMap,
				1,
				20);
				
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}

	@Test
	public void getMapAreasByBlock_MALICIOUS4() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea maliciousGeoLevelArea = cloneMaliciousGeoLevelArea();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				maliciousGeoLevelArea,
				validGeoLevelToMap,
				1,
				20);
				
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}	

	
	@Test
	public void getMapAreasByBlock_MALICIOUS5() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelArea validGeoLevelArea = cloneValidGeoLevelArea();
			GeoLevelToMap maliciousGeoLevelToMap = cloneMaliciousGeoLevelToMap();

			rifStudySubmissionService.getMapAreasByBlock(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelArea,
				maliciousGeoLevelToMap,
				1,
				20);
				
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}	
	

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
