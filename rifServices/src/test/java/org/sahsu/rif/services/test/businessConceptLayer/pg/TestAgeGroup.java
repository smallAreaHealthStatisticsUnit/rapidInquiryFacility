package org.sahsu.rif.services.test.businessConceptLayer.pg;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.AgeGroup;
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

public final class TestAgeGroup
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master age group1. */
	private AgeGroup masterAgeGroup1;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test age group.
	 */
	public TestAgeGroup() {
		masterAgeGroup1 = AgeGroup.newInstance();
		masterAgeGroup1.setName("5 9");
		masterAgeGroup1.setLowerLimit("5");
		masterAgeGroup1.setUpperLimit("9");
	}

	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	/**
	 * Accept valid age group.
	 */
	@Test
	/**
	 * Accept an age group with typical values.
	 */
	public void acceptValidInstance_COMMON() {
		AgeGroup ageGroup = AgeGroup.createCopy(masterAgeGroup1);
		
		try {
			ageGroup.checkErrors(getValidationPolicy());		
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	/**
	 * Reject blank required fields.
	 */
	@Test
	/**
	 * An age group is invalid if it has a blank name field.
	 */
	public void rejectBlankRequiredFields_ERROR() {

		//name field is empty
		AgeGroup ageGroup1 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup1.setName("");			
			ageGroup1.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_AGE_GROUP,
					1);
		}	
		
		//lower limit field is empty
		AgeGroup ageGroup2 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup2.setLowerLimit(null);		
			ageGroup2.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_AGE_GROUP,
				1);
		}		

		//upper limit is empty
		AgeGroup ageGroup3 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup3.setUpperLimit("");		
			ageGroup3.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_AGE_GROUP,
				1);
		}		
	}
		
	/**
	 * Reject invalid number for limit values.
	 */
	@Test
	/**
	 * An age group is invalid if either its lower limit or upper limit
	 * contains an invalid number.
	 */
	public void rejectInvalidNumberForLimitValues_ERROR() {
		AgeGroup ageGroup1 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup1.setLowerLimit("abc");
			ageGroup1.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_AGE_GROUP,
				1);
		}
		
		AgeGroup ageGroup2 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup2.setLowerLimit(".");
			ageGroup2.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_AGE_GROUP,
				1);
		}		
		
		AgeGroup ageGroup3 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup3.setUpperLimit("unknown");
			ageGroup3.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_AGE_GROUP,
				1);
		}
		
	}
	
	/**
	 * Reject double precision values for limits.
	 */
	@Test
	/**
	 * An age group is invalid if either its lower limit or its upper limit
	 * have a double value.  It should be an integer.
	 */
	public void rejectDoublePrecisionValuesForLimits_ERROR() {
		AgeGroup ageGroup4 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup4.setUpperLimit("11.456");
			ageGroup4.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_AGE_GROUP,
				1);
		}		
	}
		
	/**
	 * An age group is invalid if its lower limit exceeds its upper limit.
	 */
	@Test
	public void rejectLowerLimitExceedsUpperLimit_ERROR() {
		AgeGroup ageGroup1 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup1.setLowerLimit("10");
			ageGroup1.setUpperLimit("5");
			ageGroup1.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_AGE_GROUP,
				1);
		}
	}
	
	/**
	 * An age group is invalid if it contains multiple field errors.
	 */
	@Test
	public void rejectMultipleFieldErrors_ERROR() {
		AgeGroup ageGroup1 = AgeGroup.createCopy(masterAgeGroup1);		
		try {
			ageGroup1.setName("");
			ageGroup1.setLowerLimit("10");
			ageGroup1.setUpperLimit("5");
			ageGroup1.checkErrors(getValidationPolicy());		
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_AGE_GROUP,
				2);
		}
	}
	
	/**
	 * Test security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		AgeGroup maliciousAgeGroup
			= AgeGroup.createCopy(masterAgeGroup1);
		maliciousAgeGroup.setIdentifier(getTestMaliciousValue());		
		try {
			maliciousAgeGroup.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousAgeGroup
			= AgeGroup.createCopy(masterAgeGroup1);
		maliciousAgeGroup.setName(getTestMaliciousValue());
		try {
			maliciousAgeGroup.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousAgeGroup
			= AgeGroup.createCopy(masterAgeGroup1);
		maliciousAgeGroup.setLowerLimit(getTestMaliciousValue());
		try {
			maliciousAgeGroup.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousAgeGroup
			= AgeGroup.createCopy(masterAgeGroup1);
		maliciousAgeGroup.setUpperLimit(getTestMaliciousValue());
		try {
			maliciousAgeGroup.checkSecurityViolations();
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
