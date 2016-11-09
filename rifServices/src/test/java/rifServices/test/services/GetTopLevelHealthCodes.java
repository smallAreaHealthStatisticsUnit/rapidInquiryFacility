package rifServices.test.services;

import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

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

public final class GetTopLevelHealthCodes 
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

	public GetTopLevelHealthCodes() {
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	@Test
	public void getTopLevelHealthCodes_COMMON1() {
		try {			
			User validUser = cloneValidUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			ArrayList<HealthCode> topLevelICD10Codes
				= rifStudySubmissionService.getTopLevelHealthCodes(
					validUser, 
					validHealthCodeTaxonomy);
			assertEquals(2, topLevelICD10Codes.size());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}
	
	@Test
	public void getTopLevelCode_EMPTY1() {
		try {			
			User emptyUser = cloneEmptyUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getTopLevelHealthCodes(
				emptyUser, 
				validHealthCodeTaxonomy);
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
	public void getTopLevelCode_NULL1() {
		try {			
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();

			rifStudySubmissionService.getTopLevelHealthCodes(
				null, 
				validHealthCodeTaxonomy);
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
	public void getTopLevelCode_EMPTY2() {
		try {			
			User validUser = cloneValidUser();
			HealthCodeTaxonomy emptyHealthCodeTaxonomy
				= cloneEmptyHealthCodeTaxonomy();
			rifStudySubmissionService.getTopLevelHealthCodes(
				validUser, 
				emptyHealthCodeTaxonomy);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_HEALTH_CODE_TAXONOMY, 
				1);
		}		
	}
	
	@Test
	public void getTopLevelCode_NULL2() {
		try {			
			User validUser = cloneValidUser();
			rifStudySubmissionService.getTopLevelHealthCodes(
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
	public void getTopLevelCode_NONEXISTENT1() {
		try {			
			User nonExistentUser = cloneNonExistentUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getTopLevelHealthCodes(
				nonExistentUser, 
				validHealthCodeTaxonomy);
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
	public void getTopLevelCode_NONEXISTENT2() {
		try {			
			User validUser = cloneValidUser();
			HealthCodeTaxonomy nonExistentHealthCodeTaxonomy
				= cloneNonExistentHealthCodeTaxonomy();
			rifStudySubmissionService.getTopLevelHealthCodes(
				validUser, 
				nonExistentHealthCodeTaxonomy);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_HEALTH_CODE_PROVIDER, 
				1);
		}		
	}
	
	@Test
	public void getTopLevelCode_MALICIOUS1() {
		try {			
			User maliciousUser = cloneMaliciousUser();
			HealthCodeTaxonomy validHealthCodeTaxonomy
				= cloneValidHealthCodeTaxonomy();
			rifStudySubmissionService.getTopLevelHealthCodes(
				maliciousUser, 
				validHealthCodeTaxonomy);
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
	public void getTopLevelCode_MALICIOUS2() {
		try {			
			User validUser = cloneValidUser();
			HealthCodeTaxonomy maliciousHealthCodeTaxonomy
				= cloneMaliciousHealthCodeTaxonomy();
			rifStudySubmissionService.getTopLevelHealthCodes(
				validUser, 
				maliciousHealthCodeTaxonomy);
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
