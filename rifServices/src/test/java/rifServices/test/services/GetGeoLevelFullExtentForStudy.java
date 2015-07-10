package rifServices.test.services;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFSecurityViolation;
import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.StudyResultRetrievalContext;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceError;
import static org.junit.Assert.assertEquals;
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

public final class GetGeoLevelFullExtentForStudy 
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

	public GetGeoLevelFullExtentForStudy() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Test
	public void getGeoLevelFullExtentForStudy_COMMON1() {
	
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL4");
			System.out.println("Geography=="+validStudyResultRetrievalContext.getGeographyName()+"==GeoLevelSelect=="+validStudyResultRetrievalContext.getGeoLevelSelectName()+"==");
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					validUser, 
					validStudyResultRetrievalContext);
			assertEquals("-4.88653803", boundaryRectangle.getXMax());
			assertEquals("55.5268097", boundaryRectangle.getYMax());
			assertEquals("52.6875343", boundaryRectangle.getYMin());
			assertEquals("-7.58829451", boundaryRectangle.getXMin());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	@Test
	public void getGeoLevelFullExtentForStudy_EMPTY1() {
	
		try {
			User emptyUser = cloneEmptyUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();

			rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
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
	public void getGeoLevelFullExtentForStudy_NULL1() {
	
		try {
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();

			rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
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
	public void getGeoLevelFullExtentForStudy_EMPTY2() {
	
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext emptyStudyResultRetrievalContext
				= cloneValidResultContext();
			emptyStudyResultRetrievalContext.setStudyID("");
			rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
				validUser, 
				emptyStudyResultRetrievalContext);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_STUDY_RESULT_RETRIEVAL_CONTEXT, 
				1);
		}		
	}

	@Test
	public void getGeoLevelFullExtentForStudy_NULL2() {
	
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext emptyStudyResultRetrievalContext
				= cloneEmptyResultContext();						
			rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
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
	public void getGeoLevelFullExtentForStudy_NONEXISTENT1() {
	
		try {
			User nonExistentUser = cloneNonExistentUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();						
			rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
				nonExistentUser, 
				validStudyResultRetrievalContext);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFSecurityViolation.GENERAL_SECURITY_VIOLATION, 
				1);
		}		
	}

	@Test
	public void getGeoLevelFullExtentForStudy_NONEXISTENT2() {
	
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext nonExistentStudyResultRetrievalContext
				= cloneValidResultContext();					
			nonExistentStudyResultRetrievalContext.setStudyID("9998");
			rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
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
	public void getGeoLevelFullExtentForStudy_MALICIOUS1() {
	
		try {
			User maliciousUser = cloneMaliciousUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
				maliciousUser, 
				validStudyResultRetrievalContext);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFSecurityViolation.GENERAL_SECURITY_VIOLATION, 
				1);
		}		
	}
	
	@Test
	public void getGeoLevelFullExtentForStudy_MALICIOUS2() {
	
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext maliciousStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
				validUser, 
				maliciousStudyResultRetrievalContext);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFSecurityViolation.GENERAL_SECURITY_VIOLATION, 
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
