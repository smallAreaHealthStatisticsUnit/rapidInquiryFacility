package org.sahsu.rif.services.test.services.ms;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.Geography;
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

public final class GetGeoLevelSelectValues extends
                                           CommonRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetGeoLevelSelectValues() {
	
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	
	@Test
	@Ignore
	public void getGeoLevelSelectValues_COMMON1() {
		try {
			User validUser = cloneValidUser();
			ArrayList<Geography> geographies
				= rifStudySubmissionService.getGeographies(validUser);
			assertEquals(3, geographies.size());
			
			DisplayableItemSorter sorter = new DisplayableItemSorter();
			for (Geography geography : geographies) {
				sorter.addDisplayableListItem(geography);
			}
			//Three geographies should be returned
			//EW01
			//SAHSU
			//UK91

			//the second one should be "SAHSU"
			Geography sahsuGeography 
				= (Geography) sorter.sortList().get(1);
			//test check
			assertEquals("SAHSU", sahsuGeography.getDisplayName());
			
			ArrayList<GeoLevelSelect> geoLevelSelectValues
				= rifStudySubmissionService.getGeoLevelSelectValues(
					validUser, 
					sahsuGeography);
				
			//The list of all geo levels is: LEVEL1, LEVEL2, LEVEL3, LEVEL4
			//But we will exclude LEVEL4.  Otherwise, when GeoLevelSelect is LEVEL4
			//it will be impossible to choose a GeoLevelView or GeoLevelToMap
			//value.  Both of these values must be at least one level lower

                        //Nan edit on April 15, 2015, the method now should return all levels.
                        //change magic number from 3 to 4.

			assertEquals(4, geoLevelSelectValues.size());
			
			//first one should be LEVEL1
			GeoLevelSelect firstValue = geoLevelSelectValues.get(0);
			assertEquals("LEVEL1", firstValue.getName().toUpperCase());
						
			//last one should be LEVEL3
			GeoLevelSelect lastValue = geoLevelSelectValues.get(2);
			assertEquals("LEVEL3", lastValue.getName().toUpperCase());		
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
		
	
	@Test
	public void getGeoLevelSelectValue_NULL1() {
				
		try {			
			Geography validGeography = cloneValidGeography();
			rifStudySubmissionService.getGeoLevelSelectValues(
				null, 
				validGeography);
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
	public void getGeoLevelSelectValue_EMPTY1() {
		
		try {			
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			rifStudySubmissionService.getGeoLevelSelectValues(
				emptyUser, 
				validGeography);
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
	public void getGeoLevelSelectValue_NULL2() {
		
		try {
			User validUser = cloneValidUser();

			rifStudySubmissionService.getGeoLevelSelectValues(
				validUser, 
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
	@Ignore
	public void getGeoLevelSelectValue_EMPTY2() {
		
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();

			rifStudySubmissionService.getGeoLevelSelectValues(
				validUser, 
				emptyGeography);
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
	public void getGeoLevelSelectValues_NONEXISTENT1() {
		
		//try to get results using invalid user
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			rifStudySubmissionService.getGeoLevelSelectValues(
				nonExistentUser, 
				validGeography);
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
	public void getGeoLevelSelectValues_NONEXISTENT2() {
		
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();

			rifStudySubmissionService.getGeoLevelSelectValues(
				validUser, 
				nonExistentGeography);
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
	public void getGeoLevelSelectValues_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();

			rifStudySubmissionService.getGeoLevelSelectValues(
				maliciousUser, 
				validGeography);
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
	public void getGeoLevelSelectValues_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();

			rifStudySubmissionService.getGeoLevelSelectValues(
				validUser, 
				maliciousGeography);
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
