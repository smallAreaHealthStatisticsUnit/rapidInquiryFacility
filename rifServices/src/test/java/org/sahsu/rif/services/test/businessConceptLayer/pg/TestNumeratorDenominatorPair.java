package org.sahsu.rif.services.test.businessConceptLayer.pg;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.test.AbstractRIFTestCase;

import static org.junit.Assert.fail;

/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
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

public final class TestNumeratorDenominatorPair
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master nd pair. */
	private NumeratorDenominatorPair masterNDPair;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test numerator denominator pair.
	 */
	public TestNumeratorDenominatorPair() {
		masterNDPair 
			= NumeratorDenominatorPair.newInstance(
				"some_numerator_table",
				"This is the numerator table",
				"some_denominator_table",
				"This is the denominator table");
		
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	/**
	 * Accept valid nd pair.
	 */
	@Test
	public void acceptValidInstance_COMMON() {
		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		
		try {
			ndPair.checkErrors(getValidationPolicy());			
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
		
		ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setNumeratorTableDescription("");
		
	}

	/**
	 * Reject empty field values.
	 */
	@Test
	public void rejectBlankRequiredFields_ERROR() {
		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setNumeratorTableName(null);
		try {
			ndPair.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
					1);
		}
		
		ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setNumeratorTableName("");
		try {
			ndPair.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				1);
		}
		
		
		ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setNumeratorTableDescription(null);
		try {
			ndPair.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				1);
		}
		
				
		ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setNumeratorTableDescription("");
		try {
			ndPair.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				1);
		}
		
		
		
		ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setDenominatorTableName(null);
		try {
			ndPair.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				1);
		}
				
		ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setDenominatorTableName("");
		try {
			ndPair.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				1);
		}
						
		ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setDenominatorTableDescription(null);
		try {
			ndPair.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				1);
		}
				
		ndPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		ndPair.setDenominatorTableDescription("");
		try {
			ndPair.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				1);
		}	
	}
	
	
	/**
	 * Test security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		NumeratorDenominatorPair maliciousNDPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		maliciousNDPair.setIdentifier(getTestMaliciousValue());
		try {
			maliciousNDPair.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		
		maliciousNDPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		maliciousNDPair.setNumeratorTableName(getTestMaliciousValue());
		try {
			maliciousNDPair.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
				
		maliciousNDPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		maliciousNDPair.setNumeratorTableDescription(getTestMaliciousValue());
		try {
			maliciousNDPair.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}		
				
		maliciousNDPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		maliciousNDPair.setDenominatorTableName(getTestMaliciousValue());
		try {
			maliciousNDPair.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
				
		maliciousNDPair
			= NumeratorDenominatorPair.createCopy(masterNDPair);
		maliciousNDPair.setDenominatorTableDescription(getTestMaliciousValue());
		try {
			maliciousNDPair.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
	}

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
