package rifServices.test.services;

import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.StudyResultRetrievalContext;
import rifServices.businessConceptLayer.GeoLevelAttributeSource;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.MapArea;
import rifServices.system.RIFServiceError;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;

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

public final class GetPyramidDataByMapAreas 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private Integer masterNonExistentYear;
	private Integer masterValidYear;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GetPyramidDataByMapAreas() {
		masterValidYear = 1989;
		masterNonExistentYear = 1978;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private Integer cloneValidYear() {
		return new Integer(masterValidYear);
	}
	

	@Test
	public void getPyramidDataByMapAreas_COMMON1() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap
				= cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
			
			//@TODO: fail because it is not fully implemented
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			
			fail();
		}
	}

	@Test
	public void getPyramidDataByMapAreas_EMPTY1() {
		
		try {
			User emptyUser = cloneEmptyUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelToMap validGeoLevelToMap
				= cloneValidGeoLevelToMap();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				emptyUser, 
				validStudyResultRetrievalContext,
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_NULL1() {
		
		try {
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap
				= cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				null, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_EMPTY2() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext emptyValidStudyResultRetrievalContext
				= cloneEmptyResultContext();
			GeoLevelToMap validGeoLevelToMap
				= cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				emptyValidStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_NULL2() {
		
		try {
			User validUser = cloneValidUser();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				null, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_EMPTY3() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap emptyGeoLevelToMap = cloneEmptyGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				emptyGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
			
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
	public void getPyramidDataByMapAreas_NULL3() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext,
				null,
				validGeoLevelAttributeSource,
				validGeoLevelAttribute,
				validMapAreas);
			
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
	public void getPyramidDataByMapAreas_EMPTY4() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource emptyGeoLevelAttributeSource
				= cloneEmptyGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				emptyGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
			
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
	public void getPyramidDataByMapAreas_NULL4() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();			
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext,
				validGeoLevelToMap,
				null, 
				validGeoLevelAttribute,
				validMapAreas);
			
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
	public void getPyramidDataByMapAreas_EMPTY5() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();			
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String emptyGeoLevelAttribute
				= getEmptyGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				emptyGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_NULL5() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				null,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_EMPTY6() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute = getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> emptyMapAreas
				= cloneEmptyMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext,
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				emptyMapAreas);
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
	public void getPyramidDataByMapAreas_NULL6() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();

			String validGeoLevelAttribute = getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByMapAreas_NONEXISTENT1() {
		
		try {
			User nonExistentUser = cloneNonExistentUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				nonExistentUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_NONEXISTENT2() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext nonExistentStudyResultRetrievalContext
				= cloneValidResultContext();
			nonExistentStudyResultRetrievalContext.setStudyID("99945");
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				nonExistentStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_NONEXISTENT3() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap nonExistentGeoLevelToMap = cloneNonExistentGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				nonExistentGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_NONEXISTENT4() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource nonExistentGeoLevelAttributeSource
				= cloneNonExistentGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				nonExistentGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_NONEXISTENT5() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String nonExistentGeoLevelAttribute
				= getNonExistentGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				nonExistentGeoLevelAttribute,
				validMapAreas);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEO_LEVEL_ATTRIBUTE, 
				1);
		}
	}
	
	@Test
	public void getPyramidDataByMapAreas_NONEXISTENT6() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> nonExistentMapAreas
				= cloneNonExistentMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				nonExistentMapAreas);
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
	public void getPyramidDataByMapAreas_MALICIOUS1() {
		
		try {
			User maliciousUser = cloneMaliciousUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				maliciousUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_MALICIOUS2() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext maliciousStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				maliciousStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_MALICIOUS3() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap maliciousGeoLevelToMap = cloneMaliciousGeoLevelToMap();			
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				maliciousGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_MALICIOUS4() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();			
			GeoLevelAttributeSource maliciousGeoLevelAttributeSource
				= cloneMaliciousGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				maliciousGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_MALICIOUS5() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();			
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String maliciousGeoLevelAttribute
				= getMaliciousGeoLevelSourceAttribute();
			ArrayList<MapArea> validMapAreas
				= cloneValidMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				maliciousGeoLevelAttribute,
				validMapAreas);
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
	public void getPyramidDataByMapAreas_MALICIOUS6() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelToMap validGeoLevelToMap = cloneValidGeoLevelToMap();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<MapArea> maliciousMapAreas
				= cloneMaliciousMapAreas();
			rifStudyRetrievalService.getPyramidDataByMapAreas(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelToMap,
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				maliciousMapAreas);
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
