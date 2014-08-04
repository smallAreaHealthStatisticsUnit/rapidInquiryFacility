package rifServices.test.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.util.FieldValidationUtility;

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

public class GetHealthCode extends AbstractHealthCodeProviderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetHealthCode() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	@Test
	public void getHealthCode_COMMON1() {
		try {
			User validUser = cloneValidUser();

			HealthCode healthCode = cloneValidHealthCode();
			
			HealthCode result
				= rifStudySubmissionService.getHealthCode(
					validUser, 
					healthCode.getCode(), 
					healthCode.getNameSpace());
			assertNotNull(result);
			assertEquals(
				healthCode.getCode(), 
				result.getCode());
			assertEquals(
				healthCode.getNameSpace(),
				result.getNameSpace());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	@Test
	public void getHealthCode_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			HealthCode healthCode = cloneValidHealthCode();
			
			rifStudySubmissionService.getHealthCode(
				emptyUser, 
				healthCode.getCode(), 
				healthCode.getNameSpace());			
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
	public void getHealthCode_NULL1() {
		try {
			User emptyUser = cloneEmptyUser();
			HealthCode healthCode = cloneValidHealthCode();
			
			rifStudySubmissionService.getHealthCode(
				null, 
				healthCode.getCode(), 
				healthCode.getNameSpace());			
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
	public void getHealthCode_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			HealthCode healthCode = cloneValidHealthCode();
			healthCode.setCode("");
			
			rifStudySubmissionService.getHealthCode(
				validUser, 
				healthCode.getCode(), 
				healthCode.getNameSpace());			
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
	public void getHealthCode_NULL2() {
		try {
			User validUser = cloneValidUser();
			HealthCode healthCode = cloneValidHealthCode();
			healthCode.setCode(null);
			
			rifStudySubmissionService.getHealthCode(
				validUser, 
				healthCode.getCode(), 
				healthCode.getNameSpace());			
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
	public void getHealthCode_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			HealthCode healthCode = cloneValidHealthCode();
			healthCode.setNameSpace("");
			
			rifStudySubmissionService.getHealthCode(
				validUser, 
				healthCode.getCode(), 
				healthCode.getNameSpace());			
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
	public void getHealthCode_NULL3() {
		try {
			User validUser = cloneValidUser();
			HealthCode healthCode = cloneValidHealthCode();
			healthCode.setNameSpace(null);
			
			rifStudySubmissionService.getHealthCode(
				validUser, 
				healthCode.getCode(), 
				healthCode.getNameSpace());			
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
	public void getHealthCode_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			HealthCode healthCode = cloneValidHealthCode();
			rifStudySubmissionService.getHealthCode(
				nonExistentUser, 
				healthCode.getCode(), 
				healthCode.getNameSpace());			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}
	}

	@Test
	public void getHealthCode_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			HealthCode healthCode = cloneValidHealthCode();
			healthCode.setCode("blah blah");
			rifStudySubmissionService.getHealthCode(
				validUser, 
				healthCode.getCode(), 
				healthCode.getNameSpace());			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.HEALTH_CODE_NOT_KNOWN_TO_PROVIDER,
				1);
		}
	}
	
	@Test
	public void getHealthCode_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			HealthCode validHealthCode = cloneValidHealthCode();
			rifStudySubmissionService.getHealthCode(
				maliciousUser, 
				validHealthCode.getCode(), 
				validHealthCode.getNameSpace());
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
	public void getHealthCode_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			HealthCode validHealthCode = cloneValidHealthCode();
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			validHealthCode.setCode(fieldValidationUtility.getTestMaliciousFieldValue());
			rifStudySubmissionService.getHealthCode(
				validUser, 
				validHealthCode.getCode(), 
				validHealthCode.getNameSpace());
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
	public void getHealthCode_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			HealthCode validHealthCode = cloneValidHealthCode();
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			validHealthCode.setNameSpace(fieldValidationUtility.getTestMaliciousFieldValue());
			rifStudySubmissionService.getHealthCode(
				validUser, 
				validHealthCode.getCode(), 
				validHealthCode.getNameSpace());
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
