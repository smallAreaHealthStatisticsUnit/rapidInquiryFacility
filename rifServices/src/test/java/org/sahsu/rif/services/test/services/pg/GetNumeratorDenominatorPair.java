package org.sahsu.rif.services.test.services.pg;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
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

public final class GetNumeratorDenominatorPair extends CommonRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetNumeratorDenominatorPair() {
	
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	
	@Test
	@Ignore
	public void getNumeratorDenominatorPair_COMMON1() throws RIFServiceException {

		User validUser = cloneValidUser();
		Geography validGeography = cloneValidGeography();
		HealthTheme validHealthTheme = cloneValidHealthTheme();
		ArrayList<NumeratorDenominatorPair> ndPairs
			= rifStudySubmissionService.getNumeratorDenominatorPairs(
				validUser,
				validGeography,
				validHealthTheme);
		assertEquals(1, ndPairs.size());
	}
	
	@Test
	public void getNumeratorDenominatorPair_NULL1() {
		try {
			Geography validGeography = cloneValidGeography();
			HealthTheme validHealthTheme = cloneValidHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				null, 
				validGeography, 
				validHealthTheme);	
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
	public void getNumeratorDenominatorPair_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			HealthTheme validHealthTheme = cloneValidHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				emptyUser, 
				validGeography, 
				validHealthTheme);	
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
	@Ignore
	public void getNumeratorDenominatorPair_EMPTY2() {
	
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			HealthTheme validHealthTheme = cloneValidHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				validUser, 
				emptyGeography, 
				validHealthTheme);	
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
	public void getNumeratorDenominatorPair_NULL2() {
	
		try {
			User validUser = cloneValidUser();
			HealthTheme validHealthTheme = cloneValidHealthTheme();
						
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				validUser, 
				null, 
				validHealthTheme);	
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
	public void getNumeratorDenominatorPair_EMPTY3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			HealthTheme emptyHealthTheme = cloneEmptyHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				validUser, 
				validGeography, 
				emptyHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_HEALTH_THEME,
				1);
		}
	}
	
	
	@Test
	public void getNumeratorDenominatorPair_NULL3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
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
	

	
	/**
	 * Gets the numerator denominator pair e3.
	 *
	 * @return the numerator denominator pair e3
	 */
	@Test
	/**
	 * Checking nonexistent parameters
	 */
	public void getNumeratorDenominatorPair_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			HealthTheme validHealthTheme = cloneValidHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				nonExistentUser, 
				validGeography, 
				validHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}
	
	public void getNumeratorDenominatorPair_NONEXISTENT2() {	
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			HealthTheme validHealthTheme = cloneValidHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				validUser, 
				nonExistentGeography, 
				validHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}
	}

	public void getNumeratorDenominatorPair_NONEXISTENT3() {	
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			HealthTheme nonExistentHealthTheme = cloneNonExistentHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				validUser, 
				validGeography, 
				nonExistentHealthTheme);	
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_HEALTH_THEME,
				1);
		}
	}	

	@Test
	public void getNumeratorDenominatorPair_MALICIOUS1() {	
		
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			HealthTheme validHealthTheme = cloneValidHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				maliciousUser, 
				validGeography, 
				validHealthTheme);	
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
	public void getNumeratorDenominatorPair_MALICIOUS2() {	
		
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			HealthTheme validHealthTheme = cloneValidHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				validUser, 
				maliciousGeography, 
				validHealthTheme);	
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
	public void getNumeratorDenominatorPair_MALICIOUS3() {	
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			HealthTheme maliciousHealthTheme = cloneMaliciousHealthTheme();
			
			rifStudySubmissionService.getNumeratorDenominatorPairs(
				validUser, 
				validGeography, 
				maliciousHealthTheme);	
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
