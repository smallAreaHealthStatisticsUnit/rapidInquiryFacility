package org.sahsu.rif.services.test.businessConceptLayer.ms;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.test.AbstractRIFTestCase;

import static org.junit.Assert.assertEquals;
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

public final class TestHealthCode
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master health code. */
	private HealthCode masterHealthCode;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test health code.
	 */
	public TestHealthCode() {
		/**
		 * Note this one actually exists in ICD10
		 */
		masterHealthCode = HealthCode.newInstance();
		masterHealthCode.setCode("0880");
		masterHealthCode.setDescription("obstetric air embolism");
		masterHealthCode.setNameSpace("icd10");
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	/**
	 * Accept valid health code.
	 */
	@Test
	/**
	 * Accept a valid health code with typical values.
	 */
	public void acceptValidInstance_COMMON() {
		try {
			HealthCode healthCode
				= HealthCode.createCopy(masterHealthCode);
			healthCode.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * Reject blank code.
	 */
	@Test
	/**
	 * A health code is invalid if it has a blank code.
	 */
	public void rejectBlankRequiredFields_ERROR() {
		
		//code is blank
		try {
			HealthCode healthCode1
				= HealthCode.createCopy(masterHealthCode);
			healthCode1.setCode("");
			healthCode1.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_HEALTH_CODE,
					1);
		}		

		//blank name space
		try {
			HealthCode healthCode2
				= HealthCode.createCopy(masterHealthCode);
			healthCode2.setNameSpace(null);
			healthCode2.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_HEALTH_CODE,
				1);			
		}		

		//blank description
		try {
			HealthCode healthCode3
				= HealthCode.createCopy(masterHealthCode);
			healthCode3.setDescription("");
			healthCode3.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_HEALTH_CODE,
				1);			
		}
	}
	
	
	/**
	 * Reject multiple field errors.
	 */
	@Test
	/**
	 * A health code is invalid if it has multiple field errors.
	 */
	public void rejectMultipleFieldErrors_ERROR() {
		//Check capacity to detect multiple field errors
		try {
			HealthCode healthCode4
				= HealthCode.createCopy(masterHealthCode);
			healthCode4.setCode(null);
			healthCode4.setNameSpace(null);
			healthCode4.setDescription(null);
			healthCode4.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_HEALTH_CODE,
				3);
		}		
	}
	
	/**
	 * Test identical content check.
	 */
	@Test 
	public void testIdenticalContentCheck() {
		
		HealthCode healthCodeA = HealthCode.createCopy(masterHealthCode);
		HealthCode healthCodeB = HealthCode.createCopy(masterHealthCode);
		boolean isContentIdentical
			= healthCodeA.hasIdenticalContents(healthCodeB);
		assertEquals(true, isContentIdentical);		
	}
	
	
	/**
	 * Test security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		HealthCode maliciousHealthCode
			= HealthCode.createCopy(masterHealthCode);
		maliciousHealthCode.setIdentifier(getTestMaliciousValue());
		try {
			maliciousHealthCode.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		
		maliciousHealthCode
			= HealthCode.createCopy(masterHealthCode);
		maliciousHealthCode.setDescription(getTestMaliciousValue());
		try {
			maliciousHealthCode.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		

		
		maliciousHealthCode
			= HealthCode.createCopy(masterHealthCode);
		maliciousHealthCode.setCode(getTestMaliciousValue());
		try {
			maliciousHealthCode.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousHealthCode
			= HealthCode.createCopy(masterHealthCode);
		maliciousHealthCode.setNameSpace(getTestMaliciousValue());
		try {
			maliciousHealthCode.checkSecurityViolations();
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
