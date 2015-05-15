package rifServices.studyDataExtraction;

import rifServices.test.services.AbstractRIFServiceTestCase;

import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.RIFStudySubmission;


import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.dataStorageLayer.TestRIFStudyServiceBundle;
import rifServices.dataStorageLayer.TestRIFStudySubmissionService;

import static org.junit.Assert.fail;
import java.util.ArrayList;
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

public final class TestStudyDataExtraction 
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

	public TestStudyDataExtraction() {
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Test
	public void testExtract1() {
		try {
			User validUser = cloneValidUser();
			String validStudyID = "19";
			
			TestRIFStudyServiceBundle testRIFStudyServiceBundle
				= getRIFServiceBundle();
			
			TestRIFStudySubmissionService testSubmissionService
				= (TestRIFStudySubmissionService) testRIFStudyServiceBundle.getRIFStudySubmissionService();
			
			
			RIFStudySubmission rifStudySubmission
				= testSubmissionService.getRIFStudySubmission(
					validUser, 
					validStudyID);
		
			testSubmissionService.generateExtract(
				validUser, 
				rifStudySubmission);
			
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	@Test
	public void runStudy_COMMON1() {		
		try {
			User validUser = cloneValidUser();
			String validStudyID = "31";
			
			TestRIFStudyServiceBundle testRIFStudyServiceBundle
				= getRIFServiceBundle();
			
			TestRIFStudySubmissionService testSubmissionService
				= (TestRIFStudySubmissionService) testRIFStudyServiceBundle.getRIFStudySubmissionService();
			testSubmissionService.runStudy(validUser, validStudyID);			
			
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	@Test
	public void clearStudiesForUser() {		
		try {
			User validUser = cloneValidUser();
			String validStudyID = "19";
			
			TestRIFStudyServiceBundle testRIFStudyServiceBundle
				= getRIFServiceBundle();
			
			TestRIFStudySubmissionService testSubmissionService
				= (TestRIFStudySubmissionService) testRIFStudyServiceBundle.getRIFStudySubmissionService();
			testSubmissionService.clearRIFJobSubmissionsForUser(validUser);		
			
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}	
	
	@Test
	public void deleteStudy() {		
		try {
			User validUser = cloneValidUser();
			String validStudyID = "18";
			
			TestRIFStudyServiceBundle testRIFStudyServiceBundle
				= getRIFServiceBundle();
			
			TestRIFStudySubmissionService testSubmissionService
				= (TestRIFStudySubmissionService) testRIFStudyServiceBundle.getRIFStudySubmissionService();
			testSubmissionService.deleteStudy(validUser, validStudyID);
			
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}		
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
