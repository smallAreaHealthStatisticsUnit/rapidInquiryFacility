package rifServices.test.services;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifServices.businessConceptLayer.StudySummary;
import rifGenericLibrary.util.FieldValidationUtility;
import rifServices.system.RIFServiceError;
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

public final class GetCalculatedResultsByBlock 
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

	public GetCalculatedResultsByBlock() {

	}

	private String[] cloneValidResultColumnFieldNames() {
		String[] fieldNames = new String[1];
		fieldNames[0] = "fieldA";
		return fieldNames;
	}
	
	private String[] cloneEmptyResultColumnFieldNames() {
		String[] fieldNames = new String[1];
		fieldNames[0] = "";
		return fieldNames;
	}
	
	private String[] cloneNonExistentResultColumnFieldNames() {
		String[] fieldNames = new String[1];
		fieldNames[0] = "xyzabc";
		return fieldNames;
	}

	private String[] cloneMaliciousResultColumnFieldNames() {
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String[] fieldNames = new String[1];
		fieldNames[0] = fieldValidationUtility.getTestMaliciousFieldValue();
		return fieldNames;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	@Test
	public void getCalculatedResultsByBlock_COMMON1() {
		try {
			User validUser = cloneValidUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			RIFResultTable result
				= rifStudyRetrievalService.getCalculatedResultsByBlock(
					validUser, 
					validStudySummary, 
					validCalculatedResultColumnFieldNames, 
					validStartIndex, 
					validEndIndex);
			assertNotNull(result);
		}
		catch(RIFServiceException rifServiceException) {
			
			fail();
		}
	}

	@Test
	public void getCalculatedResultsByBlock_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				emptyUser, 
				validStudySummary, 
				validCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
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
	public void getCalculatedResultsByBlock_NULL1() {
		try {
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				null,
				validStudySummary, 
				validCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
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
	public void getCalculatedResultsByBlock_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			StudySummary emptyStudySummary
				= cloneEmptyStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				emptyStudySummary, 
				validCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_STUDY_SUMMARY, 
				1);
		}
	}
	
	@Test
	public void getCalculatedResultsByBlock_NULL2() {
		try {
			User validUser = cloneValidUser();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				null,
				validCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
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
	public void getCalculatedResultsByBlock_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] emptyCalculatedResultColumnFieldNames
				= cloneEmptyResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				validStudySummary, 
				emptyCalculatedResultColumnFieldNames,
				validStartIndex, 
				validEndIndex);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_TABLE_FIELD_NAMES, 
				1);
		}
	}
	
	@Test
	public void getCalculatedResultsByBlock_EMPTY4() {
		try {
			User validUser = cloneValidUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] emptyCalculatedResultColumnFieldNames
				= cloneEmptyResultColumnFieldNames();
			emptyCalculatedResultColumnFieldNames[0] = "";
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				validStudySummary, 
				emptyCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_TABLE_FIELD_NAMES, 
				1);
		}
	}
	
	@Test
	public void getCalculatedResultsByBlock_NULL3() {
		try {
			User validUser = cloneValidUser();
			StudySummary emptyStudySummary
				= cloneEmptyStudySummary();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				emptyStudySummary, 
				null, 
				validStartIndex, 
				validEndIndex);
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
	public void getCalculatedResultsByBlock_NULL4() {
		try {
			User validUser = cloneValidUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				validStudySummary, 
				validCalculatedResultColumnFieldNames, 
				null,
				validEndIndex);
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
	public void getCalculatedResultsByBlock_NULL5() {
		try {
			User validUser = cloneValidUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				validStudySummary, 
				validCalculatedResultColumnFieldNames, 
				validStartIndex,
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
	public void getCalculatedResultsByBlock_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				nonExistentUser, 
				validStudySummary, 
				validCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
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
	public void getCalculatedResultsByBlock_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			StudySummary nonExistentStudySummary
				= cloneNonExistentStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				nonExistentStudySummary, 
				validCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_DISEASE_MAPPING_STUDY, 
				1);
		}
	}
	
	@Test
	public void getCalculatedResultsByBlock_NONEXISTENT3() {
		try {
			User validUser = cloneValidUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] nonExistentColumnFieldNames
				= cloneNonExistentResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				validStudySummary, 
				nonExistentColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_TABLE_FIELD_NAME, 
				1);
		}
	}
	

	@Test
	public void getCalculatedResultsByBlock_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				maliciousUser, 
				validStudySummary, 
				validCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
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
	public void getCalculatedResultsByBlock_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			StudySummary maliciousStudySummary
				= cloneMaliciousStudySummary();
			String[] validCalculatedResultColumnFieldNames
				= cloneValidResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				maliciousStudySummary, 
				validCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
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
	public void getCalculatedResultsByBlock_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			String[] maliciousCalculatedResultColumnFieldNames
				= cloneMaliciousResultColumnFieldNames();
			Integer validStartIndex = 1;
			Integer validEndIndex = 10;
			
			rifStudyRetrievalService.getCalculatedResultsByBlock(
				validUser, 
				validStudySummary, 
				maliciousCalculatedResultColumnFieldNames, 
				validStartIndex, 
				validEndIndex);
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
