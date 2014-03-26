package rifServices.test.businessConceptLayer;


import rifServices.businessConceptLayer.AdjustableCovariate;

import rifServices.businessConceptLayer.CovariateType;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;
import rifServices.test.AbstractRIFTestCase;
import static org.junit.Assert.*;

import org.junit.Test;


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
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public class TestAdjustableCovariate extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master adjustable covariate. */
	private AdjustableCovariate masterAdjustableCovariate;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test adjustable covariate.
	 */
	public TestAdjustableCovariate() {
		masterAdjustableCovariate = AdjustableCovariate.newInstance();
		masterAdjustableCovariate.setCovariateType(CovariateType.BINARY_INTEGER_SCORE);
		masterAdjustableCovariate.setName("SES");
		masterAdjustableCovariate.setMinimumValue("1");
		masterAdjustableCovariate.setMaximumValue("5");
	}

	/**
	 * Accept valid adjustable covariate.
	 */
	@Test
	/**
	 * Accept an adjustable covariate with typical values.
	 */
	public void acceptValidAdjustableCovariate() {
		try {
			AdjustableCovariate adjustableCovariate1
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate1.checkErrors();
		}
		catch(RIFServiceException rifServiceException) {
			fail();
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}
	}
	
	/**
	 * Reject blank name.
	 */
	@Test
	/**
	 * An adjustable covariate is invalid if it has blank name
	 */
	public void rejectBlankName() {
		try {
			AdjustableCovariate adjustableCovariate
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate.setName(null);
			adjustableCovariate.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}

		try {
			AdjustableCovariate adjustableCovariate
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate.setName("");
			adjustableCovariate.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}		
	}
	
	/**
	 * Reject blank covariate type.
	 */
	@Test
	/**
	 * An adjustable covariate is invalid if it has no covariate type specified.
	 */
	public void rejectBlankCovariateType() {
		try {
			AdjustableCovariate adjustableCovariate
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate.setCovariateType(null);
			adjustableCovariate.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}	
	}

	/**
	 * Reject blank minimum value.
	 */
	@Test
	/**
	 * An adjustable covariate is invalid if it has a blank minimum value 
	 */
	public void rejectBlankMinimumValue() {
		try {
			AdjustableCovariate adjustableCovariate
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate.setMinimumValue("");
			adjustableCovariate.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}

		try {
			AdjustableCovariate adjustableCovariate
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate.setMinimumValue(null);
			adjustableCovariate.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}		
	}
	
	/**
	 * Reject blank maximum value.
	 */
	@Test
	/**
	 * An adjustable covariate is invalid if it has a blank maximum value
	 */
	public void rejectBlankMaximumValue() {
		try {
			AdjustableCovariate adjustableCovariate5
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate5.setMaximumValue("");
			adjustableCovariate5.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}		
	}
	
	
	/**
	 * Reject invalid range values.
	 */
	@Test
	/**
	 * An adjustable covariate is invalid if its minimum and/or maximum values
	 * are not valid integers.  It should fail if a value is a String.  If it
	 * is a binary integer score, it should not have a double number
	 */
	public void rejectInvalidRangeValues() {		
		try {
			AdjustableCovariate adjustableCovariate1
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate1.setMinimumValue("abc");
			adjustableCovariate1.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}

		try {
			AdjustableCovariate adjustableCovariate2
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate2.setMaximumValue("xyz");
			adjustableCovariate2.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);
		}
		
		//Consider the case where the boundaries should be integers 
		//but they are expressed as doubles
		
		/*
		 * @TODO KLG: Note that currently we don't check for types
		 * in the covariates but we should do so later on. 
		 */
		
		/*
		try {
			AdjustableCovariate adjustableCovariate3
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate3.setCovariateType(CovariateType.BINARY_INTEGER_SCORE);
			adjustableCovariate3.setMinimumValue("3.4");
			adjustableCovariate3.setMaximumValue("4.5");
			adjustableCovariate3.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				2);
		}
		*/
	}
	
	/**
	 * Reject illegal min max bounds.
	 */
	@Test
	/**
	 * An adjustable covariate is invalid if its minimum value is greater than 
	 * its maximum value
	 */
	public void rejectIllegalMinMaxBounds() {
		try {
			AdjustableCovariate adjustableCovariate1
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate1.setCovariateType(CovariateType.CONTINUOUS_VARIABLE);
			adjustableCovariate1.setMinimumValue("5.6");
			adjustableCovariate1.setMaximumValue("3.4");
			adjustableCovariate1.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);			
		}
		
		try {
			AdjustableCovariate adjustableCovariate2
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate2.setCovariateType(CovariateType.BINARY_INTEGER_SCORE);
			adjustableCovariate2.setMinimumValue("4");
			adjustableCovariate2.setMaximumValue("3");
			adjustableCovariate2.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				1);			
		}
	}
	
	/**
	 * Reject multiple field errors.
	 */
	@Test
	/**
	 * An adjustable covariate is invalid if it has multiple field errors
	 */
	public void rejectMultipleFieldErrors() {
		try {
			AdjustableCovariate adjustableCovariate1
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate1.setName(null);
			adjustableCovariate1.setCovariateType(null);
			adjustableCovariate1.setMinimumValue("");
			adjustableCovariate1.setMaximumValue("");
			adjustableCovariate1.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				4);
		}
		
		try {
			AdjustableCovariate adjustableCovariate2
				= AdjustableCovariate.createCopy(masterAdjustableCovariate);
			adjustableCovariate2.setMinimumValue("abc");
			adjustableCovariate2.setMaximumValue("def");
			adjustableCovariate2.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(rifServiceException, 
				RIFServiceError.INVALID_ADJUSTABLE_COVARIATE, 
				2);
		}		
	}
		
	/**
	 * Test security violations.
	 */
	@Test
	public void testSecurityViolations() {
		AdjustableCovariate maliciousCovariate
			= AdjustableCovariate.createCopy(masterAdjustableCovariate);
		maliciousCovariate.setIdentifier(getTestMaliciousValue());
		try {
			maliciousCovariate.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousCovariate
			= AdjustableCovariate.createCopy(masterAdjustableCovariate);
		maliciousCovariate.setName(getTestMaliciousValue());
		try {
			maliciousCovariate.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousCovariate
			= AdjustableCovariate.createCopy(masterAdjustableCovariate);
		maliciousCovariate.setMinimumValue(getTestMaliciousValue());
		try {
			maliciousCovariate.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousCovariate
			= AdjustableCovariate.createCopy(masterAdjustableCovariate);
		maliciousCovariate.setMaximumValue(getTestMaliciousValue());
		try {
			maliciousCovariate.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

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
