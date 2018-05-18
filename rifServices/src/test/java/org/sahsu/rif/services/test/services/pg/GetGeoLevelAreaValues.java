package org.sahsu.rif.services.test.services.pg;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.sahsu.rif.generic.system.RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER;

public final class GetGeoLevelAreaValues extends CommonRIFServiceTestCase {

	public GetGeoLevelAreaValues() {

	}

	@Test
	@Ignore
	public void getGeoLevelAreaValues_COMMON1() throws RIFServiceException {

		User validUser = cloneValidUser();
		Geography validGeography = cloneValidGeography();
		GeoLevelSelect validGeoLevelSelectValue = cloneValidGeoLevelSelect();

		ArrayList<GeoLevelArea> geoLevelAreaValues
			= rifStudySubmissionService.getGeoLevelAreaValues(
				validUser,
				validGeography,
				validGeoLevelSelectValue);

		assertEquals(17, geoLevelAreaValues.size());
		GeoLevelArea firstResult = geoLevelAreaValues.get(0);
		assertEquals("Abellan", firstResult.getName());
		GeoLevelArea lastResult = geoLevelAreaValues.get(16);
		assertEquals("Tirado", lastResult.getName());
	}

	@Test
	public void getGeoLevelAreaValues_NULL1() {
		
		//user
		try {
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelectValue = cloneValidGeoLevelSelect();
			
			rifStudySubmissionService.getGeoLevelAreaValues(
				null, 
				validGeography, 
				validGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				EMPTY_API_METHOD_PARAMETER,
				1);
		}
	}		

	@Test
	public void getGeoLevelAreaValues_EMPTY1() {
		
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelectValue = cloneValidGeoLevelSelect();
			
			rifStudySubmissionService.getGeoLevelAreaValues(
				emptyUser, 
				validGeography, 
				validGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.INVALID_USER,
				1);
		}
		
	}
	
	@Test
	public void getGeoLevelAreaValues_NULL2() {
		
		//geography
		try {
			User validUser =  cloneValidUser();
			GeoLevelSelect validGeoLevelSelectValue = cloneValidGeoLevelSelect();

			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser, 
				null, 
				validGeoLevelSelectValue);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					EMPTY_API_METHOD_PARAMETER,
					1);
		}

	}
	
	@Test
	@Ignore
	public void getGeoLevelAreaValues_EMPTY2() {
		
		try {
			User validUser =  cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			GeoLevelSelect validGeoLevelSelectValue = cloneValidGeoLevelSelect();

			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser, 
				emptyGeography, 
				validGeoLevelSelectValue);
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
	public void getGeoLevelAreaValues_NULL3() {
		
		//geo level select
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			
			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser, 
				validGeography, 
				null);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				EMPTY_API_METHOD_PARAMETER,
				1);
		}		
	}
	
	@Test
	@Ignore
	public void getGeoLevelAreaValues_EMPTY3() {
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect emptyGeoLevelSelectValue = cloneEmptyGeoLevelSelect();
					
			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser, 
				validGeography, 
				emptyGeoLevelSelectValue);
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
	public void getGeoLevelAreaValues_NONEXISTENT1() {

		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelectValue = cloneValidGeoLevelSelect();

			rifStudySubmissionService.getGeoLevelAreaValues(
				nonExistentUser, 
				validGeography, 
				validGeoLevelSelectValue);
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
	@Ignore
	public void getGeoLevelAreaValues_NONEXISTENT2() {
		
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			GeoLevelSelect validGeoLevelSelectValue = cloneValidGeoLevelSelect();

			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser, 
				nonExistentGeography, 
				validGeoLevelSelectValue);
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
	@Ignore
	public void getGeoLevelAreaValues_NONEXISTENT3() {		
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect nonExistentGeoLevelSelectValue = cloneNonExistentGeoLevelSelect();

			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser, 
				validGeography, 
				nonExistentGeoLevelSelectValue);
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
	public void getGeoLevelAreaValues_MALICIOUS1() {		
		
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();

			rifStudySubmissionService.getGeoLevelAreaValues(
				maliciousUser, 
				validGeography, 
				validGeoLevelSelect);
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
	public void getGeoLevelAreaValues_MALICIOUS2() {
		
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();

			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser, 
				maliciousGeography, 
				validGeoLevelSelect);
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
	public void getGeoLevelAreaValues_MALICIOUS3() {
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect maliciousGeoLevelSelect = cloneMaliciousGeoLevelSelect();

			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser, 
				validGeography, 
				maliciousGeoLevelSelect);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);	
		}
	}
}
