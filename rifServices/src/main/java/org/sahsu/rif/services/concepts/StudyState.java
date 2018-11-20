package org.sahsu.rif.services.concepts;

import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 * Describes the allowable states that a study may have.
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

 /*
	C: created, not verified; 
	V: verified, but no other work done; [NOT USED BY MIDDLEWARE]
	E: extracted imported or created, but no results or maps created; 
	G: Extract failure, extract, results or maps not created;
	R: initial results population, create map table; [NOT USED BY MIDDLEWARE]
	S: R success;
	F: R failure, R has caught one or more exceptions [depends on the exception handler design]
	W: R warning. [NOT USED BY MIDDLEWARE]
 */
public enum StudyState {
	// Not used in the database
	STUDY_NOT_CREATED(
		"X",
		"studyState.studyNotCreated.name",
		"studyState.studyNotCreated.description"),
		
	STUDY_CREATED(
		"C",
		"studyState.studyCreated.name",
		"studyState.studyCreated.description"),		
	STUDY_EXTRACTED(
		"E",
		"studyState.studyExtracted.name",		
		"studyState.studyExtracted.description"),
		STUDY_EXTRACT_FAILURE(
			"G",
			"studyState.studyExtractFailure.name",		
			"studyState.studyExtractFailure.description"),
			
	// Not currently used
	STUDY_RESULTS_CREATED(
		"R",
		"studyState.studyResultsCreated.name",		
		"studyState.studyResultsCreated.description"),
	STUDY_RESULTS_COMPUTED(
		"S",
		"studyState.studyResultsComputed.name",		
		"studyState.studyResultsComputed.description"),
	STUDY_RESULTS_RFAILURE(
		"F",
		"studyState.studyResultsRFailure.name",
		"studyState.studyResultsRFailure.description"),
	STUDY_RESULTS_RWARNING(
		"W",
		"studyState.studyResultsRWarning.name",
		"studyState.studyResultsRWarning.description"),

	// Not used in the database
	STUDY_STATE_UNKNOWN(
		"U", 
		"studyState.studyStateUnknown.name",		
		"studyState.studyStateUnknown.description");

	/* Not used by middleware
	STUDY_VERIFIED(
			"V",
			"studyState.studyVerified.description"),
	*/
	
	final String code;
	final String namePropertyName;
	final String descriptionPropertyName;
		
	private StudyState(
		final String code, 
		final String namePropertyName,
		final String descriptionPropertyName) {

		this.code = code;
		this.namePropertyName = namePropertyName;
		this.descriptionPropertyName = descriptionPropertyName;
	}
	
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		return RIFServiceMessages.getMessage(namePropertyName);
	}
	
	public String getDescription() {
		return RIFServiceMessages.getMessage(descriptionPropertyName);
	}
	
	public static StudyState getStudyStateFromName(final String studyStateName) {
		if (studyStateName == null) {
			return null;
		}
		
		if (STUDY_NOT_CREATED.getCode().equals(studyStateName)) {
			return STUDY_NOT_CREATED;
		}
		else if (STUDY_CREATED.getCode().equals(studyStateName)) {
			return STUDY_CREATED;
		}
//		else if (STUDY_VERIFIED.getName().equals(studyStateName)) {
//			return STUDY_VERIFIED;
//		}
		else if (STUDY_EXTRACTED.getCode().equals(studyStateName)) {
			return STUDY_EXTRACTED;
		}
		else if (STUDY_EXTRACT_FAILURE.getCode().equals(studyStateName)) {
			return STUDY_EXTRACT_FAILURE;
		}
		else if (STUDY_RESULTS_CREATED.getCode().equals(studyStateName)) {
			return STUDY_RESULTS_CREATED;
		}
		else if (STUDY_RESULTS_COMPUTED.getCode().equals(studyStateName)) {
			return STUDY_RESULTS_COMPUTED;
		}
		else if (STUDY_RESULTS_RFAILURE.getCode().equals(studyStateName)) {
			return STUDY_RESULTS_RFAILURE;
		}
		else if (STUDY_RESULTS_RWARNING.getCode().equals(studyStateName)) {
			return STUDY_RESULTS_RWARNING;
		}
		else {
			return null;
		}	
	}	
	
}
