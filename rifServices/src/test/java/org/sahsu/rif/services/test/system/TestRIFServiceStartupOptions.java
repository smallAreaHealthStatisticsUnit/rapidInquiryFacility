package org.sahsu.rif.services.test.system;

import java.util.ResourceBundle;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.test.AbstractRIFTestCase;
import org.sahsu.rif.services.test.util.Bundle;

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

public final class TestRIFServiceStartupOptions
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	private ResourceBundle testBundle = new Bundle();

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	/**
	 * Accept valid startup options.
	 */
	@Test
	public void acceptValidStartupOptions_COMMON() throws RIFServiceException {

		RIFServiceStartupOptions rifServiceStartupOptions = RIFServiceStartupOptions.newInstance(
				false, true, testBundle);

		rifServiceStartupOptions.checkErrors();
	}
	
	/**
	 * Reject null field values.
	 */
	@Test
	public void rejectNullFieldValues() {
		
		RIFServiceStartupOptions rifServiceStartupOptions =
				RIFServiceStartupOptions.newInstance(
						false, true, testBundle);
		rifServiceStartupOptions.setDatabaseDriverClassName(null);
		try {
			rifServiceStartupOptions.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_STARTUP_OPTIONS,
					1);
		} 

		rifServiceStartupOptions =
				RIFServiceStartupOptions.newInstance(
						false, true, testBundle);
		rifServiceStartupOptions.setDatabaseDriverPrefix(null);
		try {
			rifServiceStartupOptions.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_STARTUP_OPTIONS, 
				1);
		}

		rifServiceStartupOptions =
				RIFServiceStartupOptions.newInstance(
						false, true, testBundle);
		rifServiceStartupOptions.setDatabaseName(null);
		try {
			rifServiceStartupOptions.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_STARTUP_OPTIONS, 
				1);
		}

		rifServiceStartupOptions =
				RIFServiceStartupOptions.newInstance(
						false, true, testBundle);
		rifServiceStartupOptions.setHost(null);
		try {
			rifServiceStartupOptions.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_STARTUP_OPTIONS, 
				1);
		}


		rifServiceStartupOptions =
				RIFServiceStartupOptions.newInstance(
						false, true, testBundle);
		rifServiceStartupOptions.setPort(null);
		try {
			rifServiceStartupOptions.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_STARTUP_OPTIONS, 
				1);
		}
		
		//check multiple null field values		
		rifServiceStartupOptions =
				RIFServiceStartupOptions.newInstance(
						false, true, testBundle);
		rifServiceStartupOptions.setPort(null);
		rifServiceStartupOptions.setHost(null);
		rifServiceStartupOptions.setDatabaseName(null);
		
		try {
			rifServiceStartupOptions.checkErrors();
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_STARTUP_OPTIONS, 
				3);
		}
	}
	
	/**
	 * Test security violations.
	 */
	@Test 
	public void rejectSecurityViolations_MALICIOUS() {
		RIFServiceStartupOptions rifServiceStartupOptions
			= RIFServiceStartupOptions.newInstance(false, true);
		
		rifServiceStartupOptions.setDatabaseDriverClassName(getTestMaliciousValue());
		try {
			rifServiceStartupOptions.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		rifServiceStartupOptions
			= RIFServiceStartupOptions.newInstance(false, true);
		rifServiceStartupOptions.setDatabaseDriverPrefix(getTestMaliciousValue());
		try {
			rifServiceStartupOptions.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		
		
		rifServiceStartupOptions
			= RIFServiceStartupOptions.newInstance(false, true);
		rifServiceStartupOptions.setDatabaseName(getTestMaliciousValue());
		try {
			rifServiceStartupOptions.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		
		rifServiceStartupOptions
			= RIFServiceStartupOptions.newInstance(false, true);
		rifServiceStartupOptions.setHost(getTestMaliciousValue());
		try {
			rifServiceStartupOptions.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		
		rifServiceStartupOptions
			= RIFServiceStartupOptions.newInstance(false, true);
		rifServiceStartupOptions.setPort(getTestMaliciousValue());
		try {
			rifServiceStartupOptions.checkSecurityViolations();
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
