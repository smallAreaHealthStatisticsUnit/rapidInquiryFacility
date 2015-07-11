package rifServices.test.services;

import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.StudyResultRetrievalContext;
import rifServices.businessConceptLayer.GeoLevelAttributeSource;
import rifServices.system.RIFServiceError;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;

import static org.junit.Assert.fail;
import org.junit.Test;


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

public final class GetPyramidDataByYear 
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

	public GetPyramidDataByYear() {
		masterValidYear = 1989;
		masterNonExistentYear = 1978;
	}

	
	private Integer cloneValidYear() {
		return new Integer(masterValidYear);
	}
	
	private Integer cloneNonExistentYear() {
		return new Integer(masterNonExistentYear);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Test
	public void getPyramidDataByYear_COMMON1() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validYear);
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}

	@Test
	public void getPyramidDataByYear_EMPTY1() {
		
		try {
			User emptyUser = cloneEmptyUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				emptyUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByYear_NULL1() {
		
		try {
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				null, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByYear_EMPTY2() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext emptyValidStudyResultRetrievalContext
				= cloneEmptyResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				emptyValidStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByYear_NULL2() {
		
		try {
			User validUser = cloneValidUser();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();			
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				null, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByYear_EMPTY3() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource emptyGeoLevelAttributeSource
				= cloneEmptyGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();			
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				emptyGeoLevelAttributeSource, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByYear_NULL3() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				null, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByYear_EMPTY4() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String emptyGeoLevelAttribute
				= getEmptyGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				emptyGeoLevelAttribute,
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
	public void getPyramidDataByYear_NULL5() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
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
	public void getPyramidDataByYear_NULL6() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute = getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
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
	public void getPyramidDataByYear_NONEXISTENT1() {
		
		try {
			User nonExistentUser = cloneNonExistentUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				nonExistentUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validYear);
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
	public void getPyramidDataByYear_NONEXISTENT2() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext nonExistentStudyResultRetrievalContext
				= cloneValidResultContext();
			nonExistentStudyResultRetrievalContext.setStudyID("99945");
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				nonExistentStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByYear_NONEXISTENT3() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource nonExistentGeoLevelAttributeSource
				= cloneNonExistentGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				nonExistentGeoLevelAttributeSource, 
				validGeoLevelAttribute,
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
	public void getPyramidDataByYear_NONEXISTENT4() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String nonExistentGeoLevelAttribute
				= getNonExistentGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				nonExistentGeoLevelAttribute,
				validYear);
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
	public void getPyramidDataByYear_NONEXISTENT5() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer nonExistentYear = cloneNonExistentYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				nonExistentYear);
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	@Test
	public void getPyramidDataByYear_MALICIOUS1() {
		
		try {
			User maliciousUser = cloneMaliciousUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();			
			rifStudyRetrievalService.getPyramidDataByYear(
				maliciousUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validYear);
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
	public void getPyramidDataByYear_MALICIOUS2() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext maliciousStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				maliciousStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validYear);
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
	public void getPyramidDataByYear_MALICIOUS3() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource maliciousGeoLevelAttributeSource
				= cloneMaliciousGeoLevelAttributeSource();
			String validGeoLevelAttribute
				= getValidGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				maliciousGeoLevelAttributeSource, 
				validGeoLevelAttribute,
				validYear);
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
	public void getPyramidDataByYear_MALICIOUS4() {
		
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String maliciousGeoLevelAttribute
				= getMaliciousGeoLevelSourceAttribute();
			Integer validYear = cloneValidYear();
			rifStudyRetrievalService.getPyramidDataByYear(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				maliciousGeoLevelAttribute,
				validYear);
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
