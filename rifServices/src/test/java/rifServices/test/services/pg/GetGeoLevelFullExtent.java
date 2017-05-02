package rifServices.test.services.pg;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.system.RIFServiceError;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public final class GetGeoLevelFullExtent 
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

	public GetGeoLevelFullExtent() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	@Test
	public void getGeoLevelFullExtent_COMMON1() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			validGeoLevelSelect.setName("LEVEL2");

			
			//Geography = 'SAHSU'
			//GeoLevelSelect = 'LEVEL2'
			//
			//Should result in:
			//y_max  |  x_max   |  y_min  |  x_min
			 //---------+----------+---------+----------
			 // 55.5268 | -4.88654 | 52.6875 | -7.58829
			 //(1 row)
						
			BoundaryRectangle result
				= rifStudyRetrievalService.getGeoLevelFullExtent(
					validUser, 
					validGeography, 
					validGeoLevelSelect);
			
			assertEquals("55.5268097", result.getYMax());
			assertEquals("-4.88653803", result.getXMax());
			assertEquals("52.6875343", result.getYMin());
			assertEquals("-7.58829451", result.getXMin());
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			fail();
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				emptyUser, 
				validGeography, 
				validGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFGenericLibraryError.INVALID_USER,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_NULL1() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				null, 
				validGeography, 
				validGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}		
	}

	@Test
	public void getGeoLevelFullExtent_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				validUser, 
				emptyGeography, 
				validGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_NULL2() {
		try {
			User validUser = cloneValidUser();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				validUser, 
				null, 
				validGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect emptyGeoLevelSelect = cloneEmptyGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				validUser, 
				validGeography, 
				emptyGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFServiceError.INVALID_GEOLEVEL_SELECT,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_NULL3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				validUser, 
				validGeography, 
				null);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				nonExistentUser, 
				validGeography, 
				validGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				validUser, 
				nonExistentGeography, 
				validGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}		
	}

	@Test
	public void getGeoLevelFullExtent_NONEXISTENT3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect nonExistentGeoLevelSelect = cloneNonExistentGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				validUser, 
				validGeography, 
				nonExistentGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				maliciousUser, 
				validGeography, 
				validGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				validUser, 
				maliciousGeography, 
				validGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtent_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect maliciousGeoLevelSelect = cloneMaliciousGeoLevelSelect();
			
			rifStudyRetrievalService.getGeoLevelFullExtent(
				validUser, 
				validGeography, 
				maliciousGeoLevelSelect);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException,
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
