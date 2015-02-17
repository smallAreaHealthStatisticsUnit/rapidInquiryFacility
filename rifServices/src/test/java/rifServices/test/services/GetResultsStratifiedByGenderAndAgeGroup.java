package rifServices.test.services;

import rifServices.system.RIFServiceException;

import rifServices.system.RIFServiceError;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.StudyResultRetrievalContext;
import rifServices.businessConceptLayer.GeoLevelAttributeSource;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.businessConceptLayer.MapArea;

import static org.junit.Assert.fail;
import org.junit.Test;
import java.util.ArrayList;

/**
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

public final class GetResultsStratifiedByGenderAndAgeGroup 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private Integer masterValidYear;
	private Integer masterNonExistentYear;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GetResultsStratifiedByGenderAndAgeGroup() {
		masterValidYear = 1989;
		masterNonExistentYear = 1922;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public Integer cloneValidYear() {
		return (new Integer(masterValidYear));
	}
	
	public Integer cloneNonExistentYear() {
		return (new Integer(masterNonExistentYear));
	}
	
	
	@Test
	public void getResultsStratifiedByGenderAndAgeGroup_COMMON1() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			RIFResultTable rifResultTable
				= rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
					validUser, 
					validStudyResultRetrievalContext, 
					validGeoLevelToMap,
					validGeoLevelAttributeSource, 
					validGeoLevelSourceAttribute, 
					validMapAreas, 
					validYear);

			//@TODO: KLG - fail this for now until we've studied expected results
			fail();

		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			fail();
		}		
	}

	/**
	 * This may be a legal call - to use a year that doesn't appear in the results.
	 * The result should be no results rather than an error.
	 */
	@Test
	public void getResultsStratifiedByGenderAndAgeGroup_UNCOMMON1() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer nonExistentYear = cloneNonExistentYear();
			
			RIFResultTable rifResultTable
				= rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
					validUser, 
					validStudyResultRetrievalContext, 
					validGeoLevelToMap,
					validGeoLevelAttributeSource, 
					validGeoLevelSourceAttribute, 
					validMapAreas, 
					nonExistentYear);

			//@TODO: KLG - fail this for now until we've studied expected results
			fail();

		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			fail();
		}		
	}
	
	
	@Test
	public void getResultsStratifiedByGenderAndAgeGroup_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				emptyUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_NULL1() {
		try {
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				null, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext emptyStudyResultRetrievalContext
				= cloneEmptyResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				emptyStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_STUDY_RESULT_RETRIEVAL_CONTEXT,
				3);
		}		
	}
	
	
	@Test
	public void getResultsStratifiedByGenderAndAgeGroup_NULL2() {
		try {
			User validUser = cloneValidUser();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				null,
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource emptyGeoLevelAttributeSource
				= cloneEmptyGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				emptyGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEO_LEVEL_ATTRIBUTE_SOURCE,
				1);
		}		
	}
	
	
	@Test
	public void getResultsStratifiedByGenderAndAgeGroup_NULL3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				null, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);
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
	public void getResultsStratifiedByGenderAndAgeGroup_EMPTY4() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String emptyGeoLevelSourceAttribute
				= getEmptyGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				emptyGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_NULL4() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				null, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_EMPTY5() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> emptyMapAreas
				= cloneEmptyMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				emptyMapAreas, 
				validYear);

			fail();

		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_MAP_AREA,
				1);
		}
		
	}
	
	@Test
	public void getResultsStratifiedByGenderAndAgeGroup_NULL5() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				null, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_NULL6() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
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
	public void getResultsStratifiedByGenderAndAgeGroup_NONEXISTENT1() {
		try {
			
			User nonExistentUser = cloneNonExistentUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				nonExistentUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_NONEXISTENT2() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext nonExistentStudyResultRetrievalContext
				= cloneValidResultContext();
			nonExistentStudyResultRetrievalContext.setStudyID("9998");
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				nonExistentStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_NONEXISTENT3() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource nonExistentGeoLevelAttributeSource
				= cloneNonExistentGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				nonExistentGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute,
				validMapAreas, 
				validYear);

			fail();

		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEO_LEVEL_ATTRIBUTE_SOURCE,
				1);
		}
		
	}
	
	
	@Test
	public void getResultsStratifiedByGenderAndAgeGroup_NONEXISTENT4() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> nonExistentMapAreas
				= cloneNonExistentMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext,
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				nonExistentMapAreas,
				validYear);

			fail();

		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_MAP_AREA,
				1);
		}
		
	}
	
	
	@Test
	public void getResultsStratifiedByGenderAndAgeGroup_MALICIOUS1() {
		try {
			
			User maliciousUser = cloneMaliciousUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				maliciousUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_MALICIOUS2() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext maliciousStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				maliciousStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_MALICIOUS3() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource maliciousGeoLevelAttributeSource
				= cloneMaliciousGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				maliciousGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_MALICIOUS4() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String maliciousGeoLevelSourceAttribute
				= getMaliciousGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				maliciousGeoLevelSourceAttribute, 
				validMapAreas, 
				validYear);

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
	public void getResultsStratifiedByGenderAndAgeGroup_MALICIOUS5() {
		try {
			
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> maliciousMapAreas
				= cloneMaliciousMapAreas();
			Integer validYear = cloneValidYear();
			
			rifStudyRetrievalService.getResultsStratifiedByGenderAndAgeGroup(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute, 
				maliciousMapAreas, 
				validYear);

			fail();

		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}
		
	}
	
	
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
