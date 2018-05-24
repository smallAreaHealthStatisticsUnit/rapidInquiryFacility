package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.concepts.AbstractRIFConcept.ValidationPolicy;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
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

public final class StudySummary {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The study identifier. */
	private String studyID;
	
	/** The study name. */
	private String studyName;
	
	/** The study summary. */
	private String studySummary;
	
	/** The current processing state of the study */
	private StudyState studyState;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new RIF job submission summary.
	 *
	 * @param studyIdentifier the study identifier
	 * @param studyName the study name
	 * @param studySummary the study summary
	 */
	
	private StudySummary(
		final String studyID,
		final String studyName,
		final String studySummary) {

		this.studyID = studyID;
		this.studyName = studyName;
		this.studySummary = studySummary;
		studyState = StudyState.STUDY_STATE_UNKNOWN;
	}

	private StudySummary(
		final String studyID,
		final String studyName,
		final String studySummary,
		final StudyState studyState) {

		this.studyID = studyID;
		this.studyName = studyName;
		this.studySummary = studySummary;
		this.studyState = studyState;
	}
	
		
	/**
	 * New instance.
	 *
	 * @param studyIdentifier the study identifier
	 * @param studyName the study name
	 * @param studySummary the study summary
	 * @return the RIF job submission summary
	 */
	
	static public StudySummary newInstance(
		final String studyID,
		final String studyName,
		final String studySummary) {
		
		StudySummary summary
			= new StudySummary(
				studyID, 
				studyName, 
				studySummary);
		
		return summary;
	}
	
	static public StudySummary newInstance(
		final String studyID,
		final String studyName,
		final String studySummary,
		final StudyState studyState) {
		
		StudySummary summary
			= new StudySummary(
				studyID, 
				studyName, 
				studySummary,
				studyState);
		
		return summary;
	}
	
	
	static public StudySummary createCopy(
		final StudySummary originalStudySummary) {
		
		if (originalStudySummary == null) {
			return null;
		}
		
		StudySummary cloneStudySummary
			= StudySummary.newInstance(
				originalStudySummary.getStudyID(), 
				originalStudySummary.getStudyName(), 
				originalStudySummary.getStudySummary(),
				originalStudySummary.getStudyState());
		
		return cloneStudySummary;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Gets the study identifier.
	 *
	 * @return the study identifier
	 */
	public String getStudyID() {
		
		return studyID;
	}

	public void setStudyID(final String studyID) {
		this.studyID = studyID;
	}
	
	/**
	 * Gets the study name.
	 *
	 * @return the study name
	 */
	public String getStudyName() {
		
		return studyName;
	}

	/**
	 * Gets the study summary.
	 *
	 * @return the study summary
	 */
	public String getStudySummary() {
		
		return studySummary;
	}
	
	public StudyState getStudyState() {
		return studyState;
	}
	
	public void identifyDifferences(
		final StudySummary anotherStudySummary) {
		
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType
			= RIFServiceMessages.getMessage("studySummary.label");
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		if (fieldValidationUtility.isEmpty(studyID)) {
			String studyIDFieldName
				= RIFServiceMessages.getMessage("studySummary.identifier.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					studyIDFieldName);
			errorMessages.add(errorMessage);			
		}

		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_STUDY_SUMMARY,
					errorMessages);
			throw rifServiceException;
		}
		
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType
			= RIFServiceMessages.getMessage("studySummary.label");

		if (studyID != null) {
			String identifierFieldName
				= RIFServiceMessages.getMessage("studySummary.identifier.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				identifierFieldName,
				studyID);
		}

		if (studyName != null) {
			String nameFieldName
				= RIFServiceMessages.getMessage("studySummary.name.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				nameFieldName,
				studyName);
		}		
		
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
