package rifServices.test.services;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.MapArea;
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

public final class GetGeoLevelBoundsForArea 
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

	public GetGeoLevelBoundsForArea() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	
	@Test
	public void getGeoLevelBoundsForArea_COMMON1() {

		/**
		 * Inputs: 
		 * geography='SAHSU'
		 * geoLevelSelect='LEVEL3'
		 * studyID=1
		 * mapAreaID='01.008.005600'
		 * 
		 * should yield the results:
		 * "(54.5492,-6.56952,54.5205,-6.62246)"
		 * 
		 */
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.005600",
					"01.008.005600", 
					"01.008.005600");
			
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelBoundsForArea(
					validUser, 
					validStudyResultRetrievalContext,
					validMapArea);
			
			assertEquals("-6.56952", boundaryRectangle.getXMax());
			assertEquals("54.5492", boundaryRectangle.getYMax());
			assertEquals("54.5205", boundaryRectangle.getYMin());
			assertEquals("-6.62246", boundaryRectangle.getXMin());
		}
		catch(RIFServiceException rifServiceException) {
			
			fail();
		}
	}	

	@Test
	public void getGeoLevelBoundsForArea_COMMON2() {
	
		/**
		 * Inputs: 
		 * geography='SAHSU'
		 * geoLevelSelect='LEVEL3'
		 * studyID=1
		 * mapAreaID='01.008.003500'
		 * 
		 * should yield the results:
		 * "(54.7442,-6.57221,54.2998,-7.00093)"
		 * 
		 */
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500",					
					"01.008.003500");
			
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelBoundsForArea(
					validUser, 
					validStudyResultRetrievalContext,
					validMapArea);
			
			assertEquals("-6.57221", boundaryRectangle.getXMax());
			assertEquals("54.7442", boundaryRectangle.getYMax());
			assertEquals("54.2998", boundaryRectangle.getYMin());
			assertEquals("-7.00093", boundaryRectangle.getXMin());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	@Test
	public void getGeoLevelBoundsForArea_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500", 
					"01.008.003500");
			
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				emptyUser, 
				validStudyResultRetrievalContext,
				validMapArea);
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
	public void getGeoLevelBoundsForArea_NULL1() {
		try {
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500", 
					"01.008.003500");
			
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				null, 
				validStudyResultRetrievalContext,
				validMapArea);
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
	public void getGeoLevelBoundsForArea_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext emptyStudyResultRetrievalContext
				= cloneValidResultContext();
			emptyStudyResultRetrievalContext.setStudyID("");
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500", 
					"01.008.003500");
			
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				validUser, 
				emptyStudyResultRetrievalContext,
				validMapArea);
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
	public void getGeoLevelBoundsForArea_NULL2() {
		try {
			User validUser = cloneValidUser();
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500", 
					"01.008.003500");
			
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				validUser, 
				null,
				validMapArea);
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
	public void getGeoLevelBoundsForArea_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			MapArea emptyMapArea 
				= MapArea.newInstance(
					"", 
					"01.008.003500",					
					"01.008.003500");
			
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				validUser, 
				validStudyResultRetrievalContext,
				emptyMapArea);
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
	public void getGeoLevelBoundsForArea_NULL3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				validUser, 
				validStudyResultRetrievalContext,
				null);
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}		
	}
	
	@Test
	public void getGeoLevelBoundsForArea_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500", 
					"01.008.003500");
			
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				maliciousUser, 
				validStudyResultRetrievalContext,
				validMapArea);
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
	public void getGeoLevelBoundsForArea_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();			
			StudyResultRetrievalContext maliciousStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500", 
					"01.008.003500");
			
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				validUser, 
				maliciousStudyResultRetrievalContext,
				validMapArea);
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
	public void getGeoLevelBoundsForArea_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();			
			StudyResultRetrievalContext maliciousStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			MapArea validMapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500", 
					"01.008.003500");
		
			rifStudyRetrievalService.getGeoLevelBoundsForArea(
				validUser, 
				maliciousStudyResultRetrievalContext,
				validMapArea);
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
