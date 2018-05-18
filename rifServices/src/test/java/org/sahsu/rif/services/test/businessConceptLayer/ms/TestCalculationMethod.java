package org.sahsu.rif.services.test.businessConceptLayer.ms;

import org.junit.Test;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.CalculationMethod;
import org.sahsu.rif.services.datastorage.common.SampleTestObjectGenerator;
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

public final class TestCalculationMethod
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master calculation method. */
	private CalculationMethod masterCalculationMethod;
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test calculation method.
	 */
	public TestCalculationMethod() {
		SampleTestObjectGenerator generator
			= new SampleTestObjectGenerator();
		masterCalculationMethod 
			= generator.createSampleBYMMethod();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	
	/**
	 * Accept valid calculation method.
	 */
	@Test
	/**
	 * Accept a valid calculation method with typical values.
	 */
	public void acceptValidInstance_COMMON() {
		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			
			calculationMethod.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	@Test
	/**
	 * A calculation method is invalid if it has a blank name
	 */
	public void rejectBlankRequiredFields_ERROR() {
		
		//name is blank
		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			calculationMethod.setName(null);
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_CALCULATION_METHOD,
					1);
		}
		
		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			calculationMethod.setName("");
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				1);
		}		
		
		//the code routine name is blank
		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			calculationMethod.setCodeRoutineName("");
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				1);
		}

		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			calculationMethod.setCodeRoutineName(null);
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				1);
		}
		
		//the calculation method prior is blank
		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			calculationMethod.setPrior(null);
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				1);
		}	
		
		//reject blank description
		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			calculationMethod.setDescription(null);
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				1);
		}		

		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			calculationMethod.setDescription("");
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				1);
		}	
	}
	
	@Test
	public void rejectMultipleFieldErrors_ERROR() {
		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			calculationMethod.setName("");
			calculationMethod.setDescription(null);
			calculationMethod.setPrior(null);
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				3);
		}		
	}
	
	/**
	 * A calculation method is invalid if one of its parameters is invalid.
	 */
	/**
	 * A calculation method is invalid if it has an invalid parameter
	 */
	@Test
	public void rejectInvalidParameter_ERROR() {
		try {
			CalculationMethod calculationMethod
				= CalculationMethod.createCopy(masterCalculationMethod);
			
			//Expect two errors: empty name, empty value
			Parameter invalidParameter
				= Parameter.newInstance(null, "");
			calculationMethod.addParameter(invalidParameter);
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				2);
		}		
	}	
	
	/**
	 * Reject duplicate parameters.
	 */
	@Test
	public void rejectDuplicateParameters_ERROR() {
		CalculationMethod calculationMethod
			= CalculationMethod.createCopy(masterCalculationMethod);
		
		Parameter p1 = Parameter.newInstance("x", "10");
		calculationMethod.addParameter(p1);
		Parameter p2 = Parameter.newInstance("y", "20");
		calculationMethod.addParameter(p2);
		Parameter p3 = Parameter.newInstance("x", "423");
		calculationMethod.addParameter(p3);
		
		try {
			calculationMethod.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_CALCULATION_METHOD, 
				1);			
		}
		
	}
	
	/**
	 * Check security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		CalculationMethod maliciousCalculationMethod
			= CalculationMethod.createCopy(masterCalculationMethod);
		masterCalculationMethod.setIdentifier(getTestMaliciousValue());
		try {
			masterCalculationMethod.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		
		maliciousCalculationMethod
			= CalculationMethod.createCopy(masterCalculationMethod);
		masterCalculationMethod.setName(getTestMaliciousValue());
		try {
			masterCalculationMethod.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
				
		maliciousCalculationMethod
			= CalculationMethod.createCopy(masterCalculationMethod);
		masterCalculationMethod.setDescription(getTestMaliciousValue());
		try {
			masterCalculationMethod.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
				
		maliciousCalculationMethod
			= CalculationMethod.createCopy(masterCalculationMethod);
		masterCalculationMethod.setCodeRoutineName(getTestMaliciousValue());
		try {
			masterCalculationMethod.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		/*
		 * Check that security violations checked recursively on its child
		 * components 
		 */
		maliciousCalculationMethod
			= CalculationMethod.createCopy(masterCalculationMethod);
		Parameter maliciousParameter
			= Parameter.newInstance(getTestMaliciousValue(), "10");
		maliciousCalculationMethod.addParameter(maliciousParameter);
		try {
			masterCalculationMethod.checkSecurityViolations();
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
