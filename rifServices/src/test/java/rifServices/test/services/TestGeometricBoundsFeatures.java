package rifServices.test.services;

import rifServices.businessConceptLayer.StudyResultRetrievalContext;

import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.MapArea;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.util.FieldValidationUtility;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

public class TestGeometricBoundsFeatures extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================
	private double TOLERANCE = 0.0001;
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	/** A test user **/
	private User validUser;

	/** The non existent user. */
	private User nonExistentUser;
	
	/** The invalid user. */
	private User invalidUser;
	
	/** The malicious user. */
	private User maliciousUser;
	
	/** a test study retrieval context */
	private StudyResultRetrievalContext validStudyResultRetrievalContext;

	/** a non-existent study retrieval context */
	private StudyResultRetrievalContext nonExistentStudyResultRetrievalContext;

	/** an invalid study retrieval context */
	private StudyResultRetrievalContext invalidStudyResultRetrievalContext;

	/** a malicious study retrieval context */
	private StudyResultRetrievalContext maliciousStudyRetrievalContext;
	

	
	// ==========================================
	// Section Construction
	// ==========================================

	public TestGeometricBoundsFeatures() {

		FieldValidationUtility fieldValidationUtility 
			= new FieldValidationUtility();
		String maliciousFieldValue 
			= fieldValidationUtility.getTestMaliciousFieldValue();		
		
		try {
			validUser = User.newInstance("kgarwood", "11.111.11.228");
			nonExistentUser = User.newInstance("nobody", "11.111.11.228");
			invalidUser = User.newInstance(null, "11.111.11.228");
			maliciousUser = User.newInstance(maliciousFieldValue, "11.111.11.228");

			validStudyResultRetrievalContext 
				= StudyResultRetrievalContext.newInstance(
					"SAHSU",
					"LEVEL4",
					"1");
			nonExistentStudyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					"blah1", 
					"blah2",
					"blah3");
			invalidStudyResultRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					null,
					"",
					null);
			maliciousStudyRetrievalContext
				= StudyResultRetrievalContext.newInstance(
					maliciousFieldValue,
					"LEVEL4",
					"1");
			
			initialiseService();

		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("TestRIFContextFeatures", rifServiceException);
		}		
	}

	@Before
	public void setUp() {
		try {
			rifServiceBundle.login("kgarwood", new String("a").toCharArray());			
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}		
	}
	
	@After
	public void tearDown() {
		try {
			rifServiceBundle.deregisterAllUsers();		
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}				
	}	
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	@Test
	public void getGeoLevelFullExtentForStudy_V() {
	
		try {
			User user = User.createCopy(validUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);
			
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
			assertEquals(-4.88654, boundaryRectangle.getXMax(), TOLERANCE);
			assertEquals(55.5268, boundaryRectangle.getYMax(), TOLERANCE);
			assertEquals(52.6875, boundaryRectangle.getYMin(), TOLERANCE);
			assertEquals(-7.58829,boundaryRectangle.getXMin(), TOLERANCE);
		}
		catch(RIFServiceException rifServiceException) {
			this.printErrors("acceptValidInputs", rifServiceException);
			fail();
		}		
	}


	@Test
	public void getGeoLevelFullExtentForStudy_EMP() {
	
		try {
			
			User user = User.createCopy(invalidUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);

			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
			fail();
		}
		catch(RIFServiceException rifServiceException) {

		}
		
		try {
			
			User user = User.createCopy(validUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(invalidStudyResultRetrievalContext);
						
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
			fail();
		}
		catch(RIFServiceException rifServiceException) {

		}		
	}
	
	@Test
	public void getGeoLevelFullExtentForStudy_MAL() {
		
		try {
			User user = User.createCopy(maliciousUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION,
				1);
		}
		
		try {
			User user = User.createCopy(validUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(maliciousStudyRetrievalContext);
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
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
	public void getGeoLevelFullExtentForStudy_NE() {
				
		try {
			User user = User.createCopy(nonExistentUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
		
		//test a non-existent geography
		try {
			User user = User.createCopy(validUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);
			studyResultRetrievalContext.setGeographyName("blah");
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}		

		//non-existent geo level select
		try {
			User user = User.createCopy(validUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);
			studyResultRetrievalContext.setGeoLevelSelectName("non-existent level");
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE,
				1);
		}

		//non-existent study id
		try {
			User user = User.createCopy(validUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);
			studyResultRetrievalContext.setStudyID("999999");
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelFullExtentForStudy(
					user, 
					studyResultRetrievalContext);
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
	public void getGeoLevelBoundsForArea_V() {

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
			User user = User.createCopy(validUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);
			studyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			MapArea mapArea 
				= MapArea.newInstance(
					"01.008.005600", 
					"01.008.005600");
			
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelBoundsForArea(
					user, 
					studyResultRetrievalContext,
					mapArea);
			
			assertEquals(-6.56952, boundaryRectangle.getXMax(), TOLERANCE);
			assertEquals(54.5492, boundaryRectangle.getYMax(), TOLERANCE);
			assertEquals(54.5205, boundaryRectangle.getYMin(), TOLERANCE);
			assertEquals(-6.62246, boundaryRectangle.getXMin(), TOLERANCE);
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			fail();
		}
		
		
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
			User user = User.createCopy(validUser);
			StudyResultRetrievalContext studyResultRetrievalContext
				= StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);
			studyResultRetrievalContext.setGeoLevelSelectName("LEVEL3");
			MapArea mapArea 
				= MapArea.newInstance(
					"01.008.003500", 
					"01.008.003500");
			
			BoundaryRectangle boundaryRectangle
				= rifStudyRetrievalService.getGeoLevelBoundsForArea(
					user, 
					studyResultRetrievalContext,
					mapArea);
			
			assertEquals(-6.57221, boundaryRectangle.getXMax(), TOLERANCE);
			assertEquals(54.7442, boundaryRectangle.getYMax(), TOLERANCE);
			assertEquals(54.2998, boundaryRectangle.getYMin(), TOLERANCE);
			assertEquals(-7.00093, boundaryRectangle.getXMin(), TOLERANCE);
		}
		catch(RIFServiceException rifServiceException) {
			fail();
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
