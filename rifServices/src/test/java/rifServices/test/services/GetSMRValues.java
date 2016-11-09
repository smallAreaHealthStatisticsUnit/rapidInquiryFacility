package rifServices.test.services;

import rifServices.system.RIFServiceError;
import rifServices.businessConceptLayer.StudySummary;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;
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

public final class GetSMRValues 
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

	public GetSMRValues() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================


	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Test
	public void getSMRValues_COMMON1() {
		
		try {
			User validUser = cloneValidUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			rifStudyRetrievalService.getSMRValues(
				validUser, 
				validStudySummary);
			//@TODO: fix this test case by adding assertions
			//for now, fail the test case
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}

	
	@Test
	public void getSMRValues_EMPTY1() {
		
		try {
			User emptyUser = cloneEmptyUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			rifStudyRetrievalService.getSMRValues(
				emptyUser, 
				validStudySummary);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.INVALID_USER,
				1);
		}
	}
		
	@Test
	public void getSMRValues_NULL1() {
		
		try {
			StudySummary validStudySummary
				= cloneValidStudySummary();
			rifStudyRetrievalService.getSMRValues(
				null, 
				validStudySummary);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}
	}
	
	@Test
	public void getSMRValues_EMPTY2() {
		
		try {
			User validUser = cloneValidUser();
			StudySummary emptyStudySummary
				= cloneEmptyStudySummary();
			rifStudyRetrievalService.getSMRValues(
				validUser, 
				emptyStudySummary);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_STUDY_SUMMARY,
				1);
		}
	}
	
	@Test
	public void getSMRValues_NULL2() {
		
		try {
			User validUser = cloneValidUser();
			rifStudyRetrievalService.getSMRValues(
				validUser,
				null);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}
	}
		
	@Test
	public void getSMRValues_NONEXISTENT1() {
		
		try {
			User nonExistentUser = cloneNonExistentUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			rifStudyRetrievalService.getSMRValues(
				nonExistentUser, 
				validStudySummary);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}

	
	@Test
	public void getSMRValues_NONEXISTENT2() {
	
		try {
			User validUser = cloneValidUser();
			StudySummary nonExistentStudySummary
				= cloneNonExistentStudySummary();
			rifStudyRetrievalService.getSMRValues(
				validUser, 
				nonExistentStudySummary);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_DISEASE_MAPPING_STUDY,
				1);
		}
	}

	@Test
	public void getSMRValues_MALICIOUS1() {
		
		try {
			User maliciousUser = cloneMaliciousUser();
			StudySummary validStudySummary
				= cloneValidStudySummary();
			rifStudyRetrievalService.getSMRValues(
				maliciousUser, 
				validStudySummary);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}

	@Test
	public void getSMRValues_MALICIOUS2() {
	
		try {
			User validUser = cloneValidUser();
			StudySummary maliciousStudySummary
				= cloneMaliciousStudySummary();
			rifStudyRetrievalService.getSMRValues(
				validUser, 
				maliciousStudySummary);
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
