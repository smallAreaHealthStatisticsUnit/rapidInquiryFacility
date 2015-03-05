package rifServices.test.services;

import rifGenericLibrary.dataStorageLayer.DisplayableItemSorter;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifServices.businessConceptLayer.StudyResultRetrievalContext;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.GeoLevelAttributeSource;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;

/**
 *
 *<p>
 *The following naming convention is used:
 *[API method name]_[N, EMP, MAL, NE]
 *
 *which means:
 *<ul>
 *<li>
 *<b>N</b> = normal, valid method parameter values.  Should return reasonable
 *results.
 *</li>
 *<li>
 *<b>EMP</b> = checks for null or empty method parameter values
 *</li>
 *<li>
 *<b>MAL</b> = checks for malicious parameter values, one parameter at a time.
 *It could mean either a String parameter value is malicious or a String field
 *of an object used as a parameter has a malicious value.
 *</li>
 *</ul>
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class GetGeoLevelAttributeSources 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GetGeoLevelAttributeSources() {

	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Test
	public void getGeoLevelAttributeSources_COMMON1() {
	
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
		
			ArrayList<GeoLevelAttributeSource> results
				= rifStudyRetrievalService.getGeoLevelAttributeSources(
					validUser, 
					validStudyResultRetrievalContext);
			DisplayableItemSorter sorter = new DisplayableItemSorter();
			for (GeoLevelAttributeSource result: results) {
				sorter.addDisplayableListItem(result);
			}
			
			assertEquals(2, results.size());
			ArrayList<DisplayableListItemInterface> sortedResults
				= sorter.sortList();
			String expectedAttributeSourceTitle
				= RIFServiceMessages.getMessage(
					"geoLevelAttributeSource.extractTableSource.label");
			assertEquals(
				expectedAttributeSourceTitle, 
				sortedResults.get(0).getDisplayName());
			String mapAttributeSourceTitle
				= RIFServiceMessages.getMessage(
					"geoLevelAttributeSource.mapTableSource.label");
			assertEquals(
				mapAttributeSourceTitle, 
				sortedResults.get(1).getDisplayName());
		}
		catch(RIFServiceException rifServiceException) {			
			fail();
		}		
	}

	
	@Test
	public void getGeoLevelAttributeSources_NULL1() {
	
		try {
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
		
			rifStudyRetrievalService.getGeoLevelAttributeSources(
				null, 
				validStudyResultRetrievalContext);			
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
	public void getGeoLevelAttributeSources_EMPTY1() {
	
		try {
			User emptyUser = cloneEmptyUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
		
			rifStudyRetrievalService.getGeoLevelAttributeSources(
				emptyUser, 
				validStudyResultRetrievalContext);			

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
	public void getGeoLevelAttributeSources_NULL2() {
	
		try {
			User validUser = cloneValidUser();		
			rifStudyRetrievalService.getGeoLevelAttributeSources(
				validUser, 
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
	public void getGeoLevelAttributeSources_EMPTY2() {
	
		try {
			User validUser = cloneValidUser();
			
			StudyResultRetrievalContext emptyStudyResultRetrievalContext
				= cloneEmptyResultContext();
		
			rifStudyRetrievalService.getGeoLevelAttributeSources(
				validUser, 
				emptyStudyResultRetrievalContext);			

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
	public void getGeoLevelAttributeSources_NONEXISTENT1() {
	
		try {
			User nonExistentUser = cloneNonExistentUser();			
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
		
			rifStudyRetrievalService.getGeoLevelAttributeSources(
				nonExistentUser, 
				validStudyResultRetrievalContext);			

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
	public void getGeoLevelAttributeSources_NONEXISTENT2() {
	
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext nonExistentStudyResultRetrievalContext
				= cloneValidResultContext();
			nonExistentStudyResultRetrievalContext.setStudyID("4564");
			
			rifStudyRetrievalService.getGeoLevelAttributeSources(
				validUser, 
				nonExistentStudyResultRetrievalContext);			

			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_DISEASE_MAPPING_STUDY,
				1);
		}
	}
	
	@Test
	public void getGeoLevelAttributeSources_MALICIOUS1() {

		try {
			User maliciousUser = cloneMaliciousUser();
			StudyResultRetrievalContext studyResultRetrievalContext
				= cloneValidResultContext();
			rifStudyRetrievalService.getGeoLevelAttributeSources(
				maliciousUser, 
				studyResultRetrievalContext);			

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
	public void getGeoLevelAttributeSources_MALICIOUS2() {
	
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext maliciousStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			maliciousStudyResultRetrievalContext.setStudyID(getTestMaliciousValue());
			rifStudyRetrievalService.getGeoLevelAttributeSources(
				validUser, 
				maliciousStudyResultRetrievalContext);			

			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
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
