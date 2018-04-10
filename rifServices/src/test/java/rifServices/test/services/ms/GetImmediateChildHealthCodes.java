package rifServices.test.services.ms;


import rifServices.businessConceptLayer.HealthCode;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.DisplayableItemSorter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import static rifGenericLibrary.system.RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER;

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

public final class GetImmediateChildHealthCodes 
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

	public GetImmediateChildHealthCodes() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	@Test
	@Ignore
	public void getImmediateChildHealthCodes_COMMON1() {
		try {		
			User validUser = cloneValidUser();
			HealthCode validChapter02HealthCode
				= cloneChapter02HealthCode();

			ArrayList<HealthCode> firstGenerationCodes
				= rifStudySubmissionService.getImmediateChildHealthCodes(
					validUser, 
					validChapter02HealthCode);			
			assertEquals(2, firstGenerationCodes.size());
			
			DisplayableItemSorter sorter = new DisplayableItemSorter();
			for (HealthCode firstGenerationCode : firstGenerationCodes) {
				sorter.addDisplayableListItem(firstGenerationCode);
			}
			
			HealthCode currentResult = (HealthCode) sorter.sortList().get(0);
			assertEquals("C34", currentResult.getCode());
			
			ArrayList<HealthCode> secondGenerationCodes
				= rifStudySubmissionService.getImmediateChildHealthCodes(
					validUser, 
					currentResult);			
			assertEquals(3, secondGenerationCodes.size());
			
			sorter = new DisplayableItemSorter();
			for (HealthCode secondGenerationCode : secondGenerationCodes) {
				sorter.addDisplayableListItem(secondGenerationCode);
			}

			currentResult = (HealthCode) sorter.sortList().get(0);
			assertEquals("C340", currentResult.getCode());		
			currentResult = (HealthCode) sorter.sortList().get(1);
			assertEquals("C341", currentResult.getCode());			
			currentResult = (HealthCode) sorter.sortList().get(2);
			assertEquals("C342", currentResult.getCode());

			//C342 should have no children
			ArrayList<HealthCode> thirdGenerationCodes
				= rifStudySubmissionService.getImmediateChildHealthCodes(
					validUser, 
					currentResult);			
			assertEquals(0, thirdGenerationCodes.size());

		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	@Test
	public void getImmediateChildHealthCodes_EMPTY1() {
		
		try {
			User emptyUser = cloneEmptyUser();
			HealthCode validHealthCode = cloneValidHealthCode();
			
			rifStudySubmissionService.getImmediateChildHealthCodes(
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
	public void getImmediateChildHealthCodes_NULL1() {
		
		try {
			HealthCode validHealthCode 
				= cloneValidHealthCode();
			rifStudySubmissionService.getImmediateChildHealthCodes(
				null, 
				validHealthCode);			
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
	public void getImmediateChildHealthCodes_EMPTY2() {
		
		try {
			User validUser = cloneValidUser();
			HealthCode emptyHealthCode
				= cloneEmptyHealthCode();
			
			rifStudySubmissionService.getImmediateChildHealthCodes(
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
	public void getImmediateChildHealthCodes_NULL2() {
		
		try {
			User validUser = cloneValidUser();
			
			rifStudySubmissionService.getImmediateChildHealthCodes(
				validUser, 
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
	public void getImmediateChildHealthCodes_NONEXISTENT1() {
		
		try {
			User nonExistentUser = cloneNonExistentUser();
			HealthCode validHealthCode = cloneValidHealthCode();
			
			rifStudySubmissionService.getImmediateChildHealthCodes(
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
	@Ignore
	public void getImmediateChildHealthCodes_NONEXISTENT2() {
		
		try {
			User validUser = cloneValidUser();
			HealthCode nonExistentHealthCode
				= cloneNonExistentHealthCode();
			
			rifStudySubmissionService.getImmediateChildHealthCodes(
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
	public void getImmediateChildHealthCodes_MALICIOUS1() {
		
		try {
			User maliciousUser = cloneMaliciousUser();
			HealthCode validHealthCode = cloneValidHealthCode();
			
			rifStudySubmissionService.getImmediateChildHealthCodes(
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
	public void getImmediateChildHealthCodes_MALICIOUS2() {
		
		try {
			User validUser = cloneValidUser();
			HealthCode maliciousHealthCode = cloneMaliciousHealthCode();
			
			rifStudySubmissionService.getImmediateChildHealthCodes(
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
