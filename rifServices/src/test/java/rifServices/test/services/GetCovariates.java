package rifServices.test.services;


import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifServices.util.DisplayableItemSorter;

import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
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

public class GetCovariates extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
			
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test covariate features.
	 */
	public GetCovariates() {
	
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	@Test
	public void getCovariates_COMMON1() {
		try {
			
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();

			ArrayList<AbstractCovariate> results
				= rifStudySubmissionService.getCovariates(
					validUser, 
					validGeography,
					validGeoLevelSelect,
					validGeoLevelToMap);
			assertEquals(4, results.size());
						
			AbstractCovariate firstCovariate
				= results.get(0);
			assertEquals("areatri1km", firstCovariate.getName());
			
			AbstractCovariate lastCovariate
				= results.get(results.size() - 1);
			assertEquals("tri_1km", lastCovariate.getName());	
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	@Test
	public void getCovariates_NULL1() {
		try {
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();			
			
			rifStudySubmissionService.getCovariates(
				null, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
		
	}

	@Test
	public void getCovariates_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();			
			
			rifStudySubmissionService.getCovariates(
				emptyUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_USER, 
				1);
		}
		
	}
	
	
	
	@Test
	public void getCovariates_NULL2() {
	
		try {
			User validUser = cloneValidUser();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getCovariates(
				validUser, 
				null,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}		

	}
	
	@Test
	public void getCovariates_EMPTY2() {
	
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getCovariates(
				validUser, 
				emptyGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
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
	public void getCovariates_NULL3() {
			
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();

			rifStudySubmissionService.getCovariates(
				validUser, 
				validGeography,
				null,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
		
	}	

	@Test
	public void getCovariates_EMPTY3() {
			
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect emptyGeoLevelSelect = cloneEmptyGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();

			rifStudySubmissionService.getCovariates(
				validUser, 
				validGeography,
				emptyGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_SELECT, 
				1);
		}
		
	}	
	
	
	
	@Test
	public void getCovariates_NULL4() {
					
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();

			rifStudySubmissionService.getCovariates(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				null);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}			
	}
		
	@Test
	public void getCovariates_EMPTY4() {
					
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();
			
			rifStudySubmissionService.getCovariates(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				emptyGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEOLEVEL_TO_MAP, 
				1);
		}			
	}
	
	@Test
	public void getCovariates_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			rifStudySubmissionService.getCovariates(
				nonExistentUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}		

	}
	
	@Test
	public void getCovariates_NONEXISTENT2() {
		
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();

			rifStudySubmissionService.getCovariates(
				validUser, 
				nonExistentGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
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
	public void getCovariates_NONEXISTENT3() {

		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect nonExistentGeoLevelSelect = cloneNonExistentGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
						
			rifStudySubmissionService.getCovariates(
				validUser, 
				validGeography,
				nonExistentGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE, 
				1);
		}		
	}
		
	@Test
	public void getCovariates_NONEXISTENT4() {
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap nonExistentGeoLevelToMap = cloneNonExistentGeoLevelToMap();
					
			rifStudySubmissionService.getCovariates(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				nonExistentGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEOLEVEL_TO_MAP_VALUE, 
				1);
		}			
	}
	
	@Test
	public void getCovariates_MALICIOUS1() {
		
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
					
			rifStudySubmissionService.getCovariates(
				maliciousUser, 
				validGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}			
	}
	
	@Test
	public void getCovariates_MALICIOUS2() {
		
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
					
			rifStudySubmissionService.getCovariates(
				validUser, 
				maliciousGeography,
				validGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}			
	}
	
	@Test
	public void getCovariates_MALICIOUS3() {
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect maliciousGeoLevelSelect = cloneMaliciousGeoLevelSelect();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
					
			rifStudySubmissionService.getCovariates(
				validUser, 
				validGeography,
				maliciousGeoLevelSelect,
				validGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}			
	}
	
	@Test
	public void getCovariates_MALICIOUS4() {
		
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			GeoLevelToMap maliciousGeoLevelToMap = cloneMaliciousGeoLevelToMap();
					
			rifStudySubmissionService.getCovariates(
				validUser, 
				validGeography,
				validGeoLevelSelect,
				maliciousGeoLevelToMap);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}			
	}

	private static ArrayList<AbstractCovariate> sortCovariates(
		final ArrayList<AbstractCovariate> covariates) {
		DisplayableItemSorter sorter = new DisplayableItemSorter();
					
		for (AbstractCovariate covariate : covariates) {
			sorter.addDisplayableListItem(covariate);
		}
					
		ArrayList<AbstractCovariate> results = new ArrayList<AbstractCovariate>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			AbstractCovariate sortedCovariate 
				= (AbstractCovariate) sorter.getItemFromIdentifier(identifier);
			results.add(sortedCovariate);
		}
				
		return results;
	}	
	
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
