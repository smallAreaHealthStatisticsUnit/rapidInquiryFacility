package org.sahsu.rif.services.test.services.ms;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.Geography;
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

public final class GetNDPairFromNumeratorTable
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

	public GetNDPairFromNumeratorTable() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Test
	@Ignore
	public void getNDPairFromNumeratorTable_COMMON1() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();

			NumeratorDenominatorPair result
				= rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
					validUser, 
					validGeography, 
					validNDPair.getNumeratorTableName());
			
			Assert.assertNotNull(result);
			assertEquals(
				validNDPair.getNumeratorTableName(),
				result.getNumeratorTableName());
			
			assertEquals(
				validNDPair.getDenominatorTableName(),
				result.getDenominatorTableName());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}				
	}

	@Test
	public void getNDPairFromNumeratorTable_NULL1() {
		try {
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair validNDPair
				= cloneValidNDPair();
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				null, 
				validGeography, 
				validNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair validNDPair
				= cloneValidNDPair();
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				emptyUser, 
				validGeography, 
				validNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_NULL2() {
		try {
			User validUser = cloneValidUser();
			NumeratorDenominatorPair validNDPair
				= cloneValidNDPair();
			
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				validUser, 
				null, 
				validNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			NumeratorDenominatorPair validNDPair
				= cloneValidNDPair();
			
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				validUser, 
				emptyGeography, 
				validNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_NULL3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();

			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
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
	public void getNDPairFromNumeratorTable_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair emptyNDPair
				= cloneEmptyNDPair();

			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				validUser, 
				validGeography, 
				emptyNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair validNDPair
				= cloneValidNDPair();
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				nonExistentUser, 
				validGeography, 
				validNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			NumeratorDenominatorPair validNDPair
				= cloneValidNDPair();
			
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				validUser, 
				nonExistentGeography, 
				validNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_NONEXISTENT3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair nonExistentNDPair
				=cloneNonExistentNDPair();

			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				validUser, 
				validGeography, 
				nonExistentNDPair.getNumeratorTableName());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_NUMERATOR_TABLE,
				1);
		}				
	}
	
	@Test
	public void getNDPairFromNumeratorTable_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair validNDPair
				= cloneValidNDPair();
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				maliciousUser, 
				validGeography, 
				validNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			NumeratorDenominatorPair validNDPair
				= cloneValidNDPair();
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				validUser, 
				maliciousGeography, 
				validNDPair.getNumeratorTableName());
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
	public void getNDPairFromNumeratorTable_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair maliciousNDPair
				= cloneMaliciousNDPair();
			rifStudySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
				validUser, 
				validGeography, 
				maliciousNDPair.getNumeratorTableName());
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
