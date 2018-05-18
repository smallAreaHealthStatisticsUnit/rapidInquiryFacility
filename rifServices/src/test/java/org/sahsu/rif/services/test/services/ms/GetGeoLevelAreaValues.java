package org.sahsu.rif.services.test.services.ms;

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

public final class GetGeoLevelAreaValues
		extends CommonRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetGeoLevelAreaValues() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	
	@Test
	@Ignore
	public void getGeoLevelAreaValues_COMMON1() {
		try {
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
		catch(RIFServiceException rifServiceException) {
			fail();
		}
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
	

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
