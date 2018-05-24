package org.sahsu.rif.services.rest;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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

@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder= {
	"studyID",
	"studyName",
	"studySummary"
	}
)
public final class StudySummaryProxy {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The study identifier. */
 	@XmlElement(required = true)	
	private String studyID;
	
	/** The study name. */
 	@XmlElement(required = true)	
	private String studyName;
	
	/** The study summary. */
 	@XmlElement(required = true)	
	private String studySummary;
	
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
	private StudySummaryProxy(
		final String studyID,
		final String studyName,
		final String studySummary) {

		this.studyID = studyID;
		this.studyName = studyName;
		this.studySummary = studySummary;
	}

	/**
	 * New instance.
	 *
	 * @param studyIdentifier the study identifier
	 * @param studyName the study name
	 * @param studySummary the study summary
	 * @return the RIF job submission summary
	 */
	static public StudySummaryProxy newInstance(
		final String studyID,
		final String studyName,
		final String studySummary) {
		
		StudySummaryProxy summary
			= new StudySummaryProxy(
				studyID, 
				studyName, 
				studySummary);
		
		return summary;
	}
	
	static public StudySummaryProxy createCopy(
		final StudySummaryProxy originalStudySummary) {
		
		if (originalStudySummary == null) {
			return null;
		}
		
		StudySummaryProxy cloneStudySummary
			= StudySummaryProxy.newInstance(
				originalStudySummary.getStudyID(), 
				originalStudySummary.getStudyName(), 
				originalStudySummary.getStudySummary());
		
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
	
	public void identifyDifferences(
		final StudySummaryProxy anotherStudySummary) {
		
		
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
