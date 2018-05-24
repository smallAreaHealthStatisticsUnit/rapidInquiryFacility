package org.sahsu.rif.services.test.services.pg;

import org.junit.Ignore;
import org.junit.Test;
import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.StudyState;
import org.sahsu.rif.services.datastorage.pg.PGSQLTestRIFStudyRetrievalService;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public final class GetStudyStatusUpdates extends CommonRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public GetStudyStatusUpdates() {

	}

	@Test
	@Ignore
	public void addStudyStatusUpdates_COMMON1() throws RIFServiceException {

		User validUser = cloneValidUser();

		PGSQLTestRIFStudyRetrievalService testService
			= (PGSQLTestRIFStudyRetrievalService) rifStudyRetrievalService;
		testService.clearStudyStatusUpdates(validUser, "212");

		testService.updateStudyStatus(validUser, "212", StudyState.STUDY_RESULTS_COMPUTED, "testing111");
	}
	

	@Test
	@Ignore
	public void getAllStudyStatusUpdates_COMMON1() throws RIFServiceException {

		User validUser = cloneValidUser();

		PGSQLTestRIFStudyRetrievalService testService
			= (PGSQLTestRIFStudyRetrievalService) rifStudyRetrievalService;
		testService.clearStudyStatusUpdates(validUser, "211");
		testService.clearStudyStatusUpdates(validUser, "212");

		testService.updateStudyStatus(validUser, "211", StudyState.STUDY_RESULTS_COMPUTED, "This happened.");

		for (int i = 0; i < 10000000; i++) {

		}

		testService.updateStudyStatus(
			validUser,
			"211",
			StudyState.STUDY_RESULTS_COMPUTED,
			"It was really exciting.");

		for (int i = 0; i < 10000000; i++) {

		}
		testService.updateStudyStatus(
			validUser,
			"211",
			StudyState.STUDY_RESULTS_COMPUTED,
			"But eventually it got dull.");
		testService.updateStudyStatus(
			validUser,
			"212",
			StudyState.STUDY_RESULTS_COMPUTED,
			"Oh and then this happened.");

		for (int i = 0; i < 10000000; i++) {

		}

		testService.updateStudyStatus(
			validUser,
			"212",
			StudyState.STUDY_RESULTS_COMPUTED,
			"And then one more thing.");

		RIFResultTable resultTable
			= testService.getCurrentStatusAllStudies(validUser);
		resultTable.print();
	}
		
	
	@Test
	@Ignore
	public void getAllStudyStatusUpdates_COMMON2() throws RIFServiceException {

		User validUser = cloneValidUser();

		PGSQLTestRIFStudyRetrievalService testService
			= (PGSQLTestRIFStudyRetrievalService) rifStudyRetrievalService;

		RIFResultTable resultTable
			= testService.getCurrentStatusAllStudies(validUser);
		resultTable.print();
	}
}
