package org.sahsu.rif.services.test.businessConceptLayer.pg;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.test.AbstractRIFTestCase;

import static org.junit.Assert.fail;

/**
 * This test case covers valid GeoLevelArea, GeoLevelArea, GeoLevelToMap, 
 * GeoLevelView classes. Note that test methods exercise features which 
 * do not require knowledge from the database.
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

public final class TestGeoLevelArea
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master geo level area. */
	private GeoLevelArea masterGeoLevelArea;
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test geo level classes.
	 */
	public TestGeoLevelArea() {
		masterGeoLevelArea 	
			= GeoLevelArea.newInstance(
				"Barnet", 
				"The Barnett area");
	}

	
// ==========================================
// Section Accessors and Mutators
// ==========================================

// ==========================================
// Section Errors and Validation
// ==========================================
	
	/**
	 * accept a valid geo level area with typical values.
	 */
	@Test
	public void acceptValidInstance_COMMON() {

		try {
			GeoLevelArea geoLevelArea 
				= GeoLevelArea.createCopy(masterGeoLevelArea);
			geoLevelArea.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * A geo level area is invalid if it has a blank name.
	 */
	@Test
	public void rejectBlankRequiredFields_ERROR() {

		try {
			GeoLevelArea geoLevelArea 
				= GeoLevelArea.createCopy(masterGeoLevelArea);
			geoLevelArea.setName("");
			geoLevelArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_GEOLEVEL_AREA,
					1);
		}
		

		try {
			GeoLevelArea geoLevelArea 
				= GeoLevelArea.createCopy(masterGeoLevelArea);
			geoLevelArea.setName(null);
			geoLevelArea.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_AREA, 
				1);
		}
		
	}

	@Test
	/**
	 * Test geo level area security violations.
	 */
	public void rejectSecurityViolations_MALICIOUS() {

		GeoLevelArea maliciousGeoLevelArea
			= GeoLevelArea.createCopy(masterGeoLevelArea);
		maliciousGeoLevelArea.setIdentifier(getTestMaliciousValue());
		try {
			maliciousGeoLevelArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	
		maliciousGeoLevelArea
			= GeoLevelArea.createCopy(masterGeoLevelArea);
		maliciousGeoLevelArea.setName(getTestMaliciousValue());
		try {
			maliciousGeoLevelArea.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousGeoLevelArea
			= GeoLevelArea.createCopy(masterGeoLevelArea);
		maliciousGeoLevelArea.setDescription(getTestMaliciousValue());
		try {
			maliciousGeoLevelArea.checkSecurityViolations();
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
