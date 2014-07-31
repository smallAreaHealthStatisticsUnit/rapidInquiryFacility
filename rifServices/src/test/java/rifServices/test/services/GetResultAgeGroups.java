package rifServices.test.services;

import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceError;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.StudyResultRetrievalContext;
import rifServices.businessConceptLayer.GeoLevelAttributeSource;

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

public class GetResultAgeGroups extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetResultAgeGroups() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	@Test
	public void getResultAgeGroups_COMMON1() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			ArrayList<AgeGroup> results
				= rifStudyRetrievalService.getResultAgeGroups(
					validUser, 
					validResultRetrievalContext, 
					validGeoLevelAttributeSource, 
					validGeoLevelSourceAttribute);
			
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			fail();
		}
	}
	
	@Test
	public void getResultAgeGroups_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				emptyUser, 
				validResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_NULL1() {
		try {
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				null, 
				validResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext emptyResultRetrievalContext
				= cloneEmptyResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				validUser, 
				emptyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_NULL2() {
		try {
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				null, 
				validResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource emptyGeoLevelAttributeSource
				= cloneEmptyGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				validUser, 
				validResultRetrievalContext, 
				emptyGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_NULL3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				validUser, 
				validResultRetrievalContext, 
				null, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_EMPTY4() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String emptyGeoLevelSourceAttribute
				= getEmptyGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				validUser, 
				validResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				emptyGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_NULL4() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			rifStudyRetrievalService.getResultAgeGroups(
				validUser, 
				validResultRetrievalContext, 
				validGeoLevelAttributeSource, 
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
	public void getResultAgeGroups_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				maliciousUser, 
				validResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext maliciousResultRetrievalContext
				= cloneMaliciousResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				validUser, 
				maliciousResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource maliciousGeoLevelAttributeSource
				= cloneMaliciousGeoLevelAttributeSource();
			String validGeoLevelSourceAttribute
				= getValidGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				validUser, 
				validResultRetrievalContext, 
				maliciousGeoLevelAttributeSource, 
				validGeoLevelSourceAttribute);
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
	public void getResultAgeGroups_MALICIOUS4() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			String maliciousGeoLevelSourceAttribute
				= getMaliciousGeoLevelSourceAttribute();
			rifStudyRetrievalService.getResultAgeGroups(
				validUser, 
				validResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				maliciousGeoLevelSourceAttribute);
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
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
