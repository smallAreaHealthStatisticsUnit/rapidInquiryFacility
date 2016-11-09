package rifServices.test.services;



import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.Geography;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;

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
 * Copyright 2016 Imperial College London, developed by the Small Area
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

public final class GetMapAreasForBoundaryRectangle 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private GeoLevelSelect validGeoLevelSelect;
	
	private BoundaryRectangle validBoundaryRectangle;
	private BoundaryRectangle emptyBoundaryRectangle;
	private BoundaryRectangle maliciousBoundaryRectangle;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GetMapAreasForBoundaryRectangle() {
		validGeoLevelSelect
			= GeoLevelSelect.newInstance("LEVEL4");
		

		validBoundaryRectangle 
			= BoundaryRectangle.newInstance(
				"-6.68853", 
				"54.6456", 
				"-6.32507", 
				"55.0122");

		emptyBoundaryRectangle
			= BoundaryRectangle.newInstance(
				"-6.68853", 
				"54.6456", 
				"", 
				"55.0122");


		maliciousBoundaryRectangle
			= BoundaryRectangle.newInstance(
				"-6.68853", 
				getTestMaliciousValue(), 
				"-6.32507", 
				"55.0122");
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	@Override
	protected GeoLevelSelect cloneValidGeoLevelSelect() {
		GeoLevelSelect geoLevelSelect
			= GeoLevelSelect.createCopy(validGeoLevelSelect);
		return geoLevelSelect;
	}
	
	protected BoundaryRectangle cloneValidBoundaryRectangle() {
		BoundaryRectangle boundaryRectangle
			= BoundaryRectangle.createCopy(validBoundaryRectangle);
		return boundaryRectangle;
	}
	
	protected BoundaryRectangle cloneEmptyBoundaryRectangle() {
		BoundaryRectangle boundaryRectangle
			= BoundaryRectangle.createCopy(emptyBoundaryRectangle);
		return boundaryRectangle;
	}

	protected BoundaryRectangle cloneMaliciousBoundaryRectangle() {
		BoundaryRectangle boundaryRectangle
			= BoundaryRectangle.createCopy(maliciousBoundaryRectangle);
		return boundaryRectangle;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	//TOUR_ADD_METHOD-4
	/*
	 * Here we're testing the new service method.  This method happens to return
	 * a JSON string, which makes it more difficult to compare expected and actual results.
	 * In most of the methods, we return a collection of RIF business objects which can be 
	 * ordered and counted.  Here, the String result mixes both the results and the 
	 * format.  We will need a utility which can extract and order 'gid' values that 
	 * are embedded in the JSON result.
	 */

	
	@Test
	public void getMapAreasForBoundaryRectangle_COMMON() {
		
		/**
		 * Geography='SAHSU'
		 * GeoLevelView='LEVEL4'
		 * Boundary Rectangle
		 * yMax = 55.0122
		 * xMax = -6.32507
		 * yMin = 54.6456
		 * xMin = -6.68853
		 * 
		 * Should return a JSON string with 4 results and the following gids:
		 * 1, 85, 86, 87
		 * 
		 * 
		 */
		
		try {			
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			String result
				= rifStudySubmissionService.getMapAreasForBoundaryRectangle(
					validUser, 
					validGeography, 
					validGeoLevelSelect, 
					validBoundaryRectangle);
			
			//@TODO: Develop a utility that extract gids from JSON stream and orders them.
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}		
	}
	
	@Test
	public void getMapAreasForBoundaryRectangle_NULL_VALUES() {
		
		//null user
		try {						
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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
		
		//null geography
		try {			
			User validUser = cloneValidUser();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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

		
		//null geo level
		try {						
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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
				
		//null boundary rectangle
		try {						
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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
	public void getMapAreasForBoundaryRectangle_EMPTY_VALUES() {
		
		//empty user
		try {			
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
				emptyUser, 
				validGeography, 
				validGeoLevelSelect, 
				validBoundaryRectangle);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.INVALID_USER,
				1);
		}
		
		//empty geography
		try {			
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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
			
		//empty geo level
		try {			
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect emptyGeoLevelSelect
				= cloneEmptyGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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
				
		try {			
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect
				= cloneValidGeoLevelSelect();
			BoundaryRectangle emptyBoundaryRectangle
				= cloneEmptyBoundaryRectangle();
	
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
				validUser, 
				validGeography, 
				validGeoLevelSelect, 
				emptyBoundaryRectangle);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_BOUNDARY_RECTANGLE,
				1);
		}		
	}
	
	@Test
	public void getMapAreasForBoundaryRectangle_MALICIOUS1() {
				
		//malicious user
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
				maliciousUser, 
				validGeography, 
				validGeoLevelSelect, 
				validBoundaryRectangle);			
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
	public void getMapAreasForBoundaryRectangle_MALICIOUS2() {
	
		//malicious geography
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();

			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
				validUser, 
				maliciousGeography, 
				validGeoLevelSelect, 
				validBoundaryRectangle);			
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
	public void getMapAreasForBoundaryRectangle_MALICIOUS3() {
		
		//malicious geo level
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect maliciousGeoLevelSelect 
				= cloneMaliciousGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();

			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
				validUser, 
				validGeography, 
				maliciousGeoLevelSelect, 
				validBoundaryRectangle);			
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
	public void getMapAreasForBoundaryRectangle_MALICIOUS4() {	
		//malicious valid boundary rectangle

		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect maliciousGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle maliciousBoundaryRectangle
				= cloneMaliciousBoundaryRectangle();

			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
				validUser, 
				validGeography, 
				maliciousGeoLevelSelect, 
				validBoundaryRectangle);			
			//fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}

	}
	
	
	
	@Test
	public void getMapAreasForBoundaryRectangle_NONEXISTENT_VALUES() {
	
		//non-existent user	
		/*
		try {			
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
				nonExistentUser, 
				validGeography, 
				validGeoLevelSelect, 
				validBoundaryRectangle);
			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFSecurityViolation.GENERAL_SECURITY_VIOLATION,
				1);
		}		
		*/

		//non-existent geography
		try {			
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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

		//non-existent geography
		try {			
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			GeoLevelSelect validGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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

		//non-existent geo level
		try {			
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect nonExistentGeoLevelSelect 
				= cloneNonExistentGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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
	
		//non-existent boundary rectangle
		/*
		try {			
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect nonExistentGeoLevelSelect 
				= cloneValidGeoLevelSelect();
			BoundaryRectangle validBoundaryRectangle
				= cloneValidBoundaryRectangle();
		
			rifStudySubmissionService.getMapAreasForBoundaryRectangle(
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
		*/
		
	}	

	
	
	
		
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
