package rifServices.test.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
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

public class GetHealthCodesMatchingSearchText extends
		AbstractHealthCodeProviderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetHealthCodesMatchingSearchText() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================


	
	@Test
	/**
	 * case sensitive search text yields multiple results
	 */
	public void getHealthCodesMatchingSearchText_COMMON1() {
		try {
			User validUser = cloneValidUser();
			String searchText = "bronchus";
			
			HealthCodeTaxonomy icd10HealthCodeTaxonomy
				= cloneICD10HealthTaxonomy();
			
			ArrayList<HealthCode> healthCodes
				= rifStudySubmissionService.getHealthCodesMatchingSearchText(
					validUser, 
					icd10HealthCodeTaxonomy,
					searchText,
					true);
			assertEquals(4, healthCodes.size());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	@Test
	/**
	 * case sensitive search text yields one result
	 */
	public void getHealthCodesMatchingSearchText_COMMON2() {
		try {
			User validUser = cloneValidUser();
			String searchText = "main bronchus";

			HealthCodeTaxonomy icd10HealthCodeTaxonomy
				= cloneICD10HealthTaxonomy();
			ArrayList<HealthCode> healthCodes
				= rifStudySubmissionService.getHealthCodesMatchingSearchText(
					validUser, 
					icd10HealthCodeTaxonomy,
					searchText,
					true);
			assertEquals(1, healthCodes.size());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}
	
	
	
	@Test
	/**
	 * case sensitive search text yields no results
	 */
	public void getHealthCodesMatchingSearchText_COMMON3() {
		try {
			User validUser = cloneValidUser();
			String searchText = "zzzz";

			HealthCodeTaxonomy icd10HealthCodeTaxonomy
				= cloneICD10HealthTaxonomy();
			ArrayList<HealthCode> healthCodes
				= rifStudySubmissionService.getHealthCodesMatchingSearchText(
					validUser, 
					icd10HealthCodeTaxonomy,
					searchText,
					true);
			assertEquals(0, healthCodes.size());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}
	
	@Test
	/**
	 * case insensitive search text yields expected results
	 */
	public void getHealthCodesMatchingSearchText_COMMON4() {

		try {
			User validUser = cloneValidUser();
			String searchText = "j206";
			
			HealthCodeTaxonomy icd10HealthCodeTaxonomy
				= cloneICD10HealthTaxonomy();
			ArrayList<HealthCode> healthCodes
				= rifStudySubmissionService.getHealthCodesMatchingSearchText(
					validUser, 
					icd10HealthCodeTaxonomy,
					searchText,
					true);
			assertEquals(0, healthCodes.size());
			
			healthCodes
				= rifStudySubmissionService.getHealthCodesMatchingSearchText(
					validUser, 
					icd10HealthCodeTaxonomy,
					searchText,
					false);
			assertEquals(1, healthCodes.size());

		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
		
		
	}
	
	
	@Test
	public void getHealthCodesMatchingSearchText_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				emptyUser, 
				validHealthCodeTaxonomy,
				getValidSearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_USER, 
				1);			
		}
	}

	@Test
	public void getHealthCodesMatchingSearchText_NULL1() {
		try {
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				null, 
				validHealthCodeTaxonomy,
				getValidSearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);			
		}
	}
	
	@Test
	public void getHealthCodesMatchingSearchText_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			HealthCodeTaxonomy emptyHealthCodeTaxonomy
				= cloneEmptyHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				validUser, 
				emptyHealthCodeTaxonomy,
				getValidSearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_HEALTH_CODE_TAXONOMY, 
				1);			
		}
	}
	
	@Test
	public void getHealthCodesMatchingSearchText_NULL2() {
		try {
			User validUser = cloneValidUser();
			HealthCodeTaxonomy emptyHealthCodeTaxonomy
				= cloneEmptyHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				validUser, 
				null,
				getValidSearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);			
		}
	}
	
	@Test
	public void getHealthCodesMatchingSearchText_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				validUser, 
				validHealthCodeTaxonomy,
				getEmptySearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);			
		}
	}
	
	@Test
	public void getHealthCodesMatchingSearchText_NULL3() {
		try {
			User validUser = cloneValidUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				validUser, 
				validHealthCodeTaxonomy,
				null,
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);			
		}
	}
	
	@Test
	public void getHealthCodesMatchingSearchText_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();			
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				nonExistentUser, 
				validHealthCodeTaxonomy,
				getValidSearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);			
		}
	}
		
	@Test
	public void getHealthCodesMatchingSearchText_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			HealthCodeTaxonomy nonExistentHealthCodeTaxonomy
				= cloneNonExistentHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				validUser, 
				nonExistentHealthCodeTaxonomy,
				getValidSearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_HEALTH_CODE_PROVIDER, 
				1);			
		}
	}
	
	@Test
	public void getHealthCodesMatchingSearchText_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				maliciousUser, 
				validHealthCodeTaxonomy,
				getValidSearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);			
		}
	}
	
	@Test
	public void getHealthCodesMatchingSearchText_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			HealthCodeTaxonomy maliciousHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				validUser, 
				maliciousHealthCodeTaxonomy,
				getValidSearchText(),
				true);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);			
		}
	}
	
	@Test
	public void getHealthCodesMatchingSearchText_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			HealthCodeTaxonomy maliciousHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getHealthCodesMatchingSearchText(
				validUser, 
				maliciousHealthCodeTaxonomy,
				getMaliciousSearchText(),
				true);
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
