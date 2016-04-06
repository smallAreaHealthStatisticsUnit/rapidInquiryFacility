package rifServices.test.services;





import rifServices.businessConceptLayer.*;
import rifServices.dataStorageLayer.SampleTestObjectGenerator;
import rifServices.system.RIFServiceError;
import rifServices.fileFormats.RIFZipFileWriter;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;

import java.io.File;
import java.util.ArrayList;

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

public final class GetStudySummaries 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String validStudyID;
	private String maliciousStudyID;
	private String nonExistentStudyID;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GetStudySummaries() {
		validStudyID = "13";
		
		maliciousStudyID = getTestMaliciousValue();
		nonExistentStudyID = "blah";
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	private void populateDatabaseWithValidStudy() {

		SampleTestObjectGenerator sampleTestObjectGenerator
			= new SampleTestObjectGenerator();
		RIFStudySubmission studySubmission
			= sampleTestObjectGenerator.createSampleRIFJobSubmission();		
		
		
	}
	
	@Test
	public void getStudySummaries_COMMON1() {

		try {
			User validUser = cloneValidUser();
			
			populateDatabaseWithValidStudy();			
			ArrayList<StudySummary> studySummaries
				= rifStudySubmissionService.getStudySummaries(validUser);
			for (StudySummary studySummary : studySummaries) {
				printStudySummary(studySummary);
			}
			
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}
	}
	
	private void printStudySummary(
		final StudySummary studySummary) {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("Study Summary \n");
		buffer.append("ID:==" + studySummary.getStudyID() + "\n");
		buffer.append("Study Summary:==" + studySummary.getStudyName() + "==\n");
		buffer.append("Study Description:=="+ studySummary.getStudySummary() + "==\n");		
		System.out.println(buffer.toString());
	}
	

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
