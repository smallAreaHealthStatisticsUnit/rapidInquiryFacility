package rifServices.test.services;


import rifServices.businessConceptLayer.HealthCode;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

public final class GetParentHealthCode 
	extends AbstractHealthCodeProviderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetParentHealthCode() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Test 
	public void getParentHealthCode_COMMON1() {
		try {
			User validUser = cloneValidUser();
			HealthCode c342HealthCode = cloneC342HealthCode();

			HealthCode parentCode
				= rifStudySubmissionService.getParentHealthCode(
					validUser, 
					c342HealthCode);
			assertNotNull(parentCode);
			
			HealthCode c34HealthCode = cloneC34HealthCode();
			assertEquals(c34HealthCode.getCode(), parentCode.getCode());
			assertEquals(false, parentCode.isTopLevelTerm());
			assertEquals(
				c34HealthCode.getDescription(),
				parentCode.getDescription());

			HealthCode grandParentHealthCode
				= rifStudySubmissionService.getParentHealthCode(
					validUser, 
					parentCode);
			HealthCode chapter02HealthCode = cloneChapter02HealthCode();
			assertEquals(chapter02HealthCode.getCode(), grandParentHealthCode.getCode());
			assertEquals(
				true, 
				grandParentHealthCode.isTopLevelTerm());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}	
	
	@Test
	public void getParentHealthCode_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			HealthCode validHealthCode = cloneValidHealthCode();
						
			rifStudySubmissionService.getParentHealthCode(
				emptyUser, 
				validHealthCode);		
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
	public void getParentHealthCode_NULL1() {
		try {
			HealthCode validHealthCode = cloneValidHealthCode();						
			rifStudySubmissionService.getParentHealthCode(
				null, 
				validHealthCode);		
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
	public void getParentHealthCode_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			HealthCode emptyHealthCode = cloneEmptyHealthCode();
						
			rifStudySubmissionService.getParentHealthCode(
				validUser, 
				emptyHealthCode);		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_HEALTH_CODE,
				1);			
		}
	}
	
	@Test
	public void getParentHealthCode_NULL2() {
		try {
			User validUser = cloneValidUser();
						
			rifStudySubmissionService.getParentHealthCode(
				validUser, 
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
	public void getParentHealthCode_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			HealthCode validHealthCode = cloneValidHealthCode();			
			
			rifStudySubmissionService.getParentHealthCode(
				nonExistentUser,
				validHealthCode);		
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
	public void getParentHealthCode_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			HealthCode nonExistentHealthCode
				= cloneNonExistentHealthCode();
			
			rifStudySubmissionService.getParentHealthCode(
				validUser,
				nonExistentHealthCode);		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.HEALTH_CODE_NOT_KNOWN_TO_PROVIDER,
				1);
		}
	}

	@Test
	public void getParentHealthCode_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			HealthCode validHealthCode = cloneValidHealthCode();
			
			rifStudySubmissionService.getParentHealthCode(
				maliciousUser,
				validHealthCode);		
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
	public void getParentHealthCode_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			HealthCode maliciousHealthCode
				= cloneMaliciousHealthCode();
			
			rifStudySubmissionService.getParentHealthCode(
				validUser,
				maliciousHealthCode);		
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
