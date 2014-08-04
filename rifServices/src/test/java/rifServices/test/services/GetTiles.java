package rifServices.test.services;

import static org.junit.Assert.fail;

import org.junit.Test;

import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.Geography;
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

public class GetTiles extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetTiles() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================


	@Test
	public void getTiles_COMMON1() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			String result
				= rifStudyRetrievalService.getTiles(
					validUser, 
					validGeography, 
					validGeoLevelSelect,
					validBoundaryRectangle);			
		}
		catch(RIFServiceException rifServiceException) {
			
			fail();
		}
	}

	@Test
	public void getTiles_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				emptyUser, 
				validGeography, 
				validGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_NULL1() {
		try {
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				null, 
				validGeography, 
				validGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				emptyGeography, 
				validGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_NULL2() {
		try {
			User validUser = cloneValidUser();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				null, 
				validGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect emptyGeoLevelSelect = cloneEmptyGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				validGeography, 
				emptyGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_NULL3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				validGeography, 
				null,
				validBoundaryRectangle);			
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
	public void getTiles_NULL4() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			rifStudyRetrievalService.getTiles(
				validUser, 
				validGeography, 
				validGeoLevelSelect,
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
	public void getTiles_INVALID_BOUNDARY1() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle invalidBoundaryRectangle 
				= cloneInvalidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				validGeography, 
				validGeoLevelSelect,
				invalidBoundaryRectangle);			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_BOUNDARY_RECTANGLE,
				2);
		}
	}

	@Test
	public void getTiles_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				nonExistentUser, 
				validGeography, 
				validGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				nonExistentGeography, 
				validGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_NONEXISTENT3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect nonExistentGeoLevelSelect = cloneNonExistentGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				validGeography, 
				nonExistentGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				maliciousUser, 
				validGeography, 
				validGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				maliciousGeography, 
				validGeoLevelSelect,
				validBoundaryRectangle);			
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
	public void getTiles_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validMaliciousGeoLevelSelect = cloneMaliciousGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			rifStudyRetrievalService.getTiles(
				validUser, 
				validGeography, 
				validMaliciousGeoLevelSelect,
				validBoundaryRectangle);			
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
