package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
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

public class Project 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The name. */
	private String name;
	
	/** The description. */
	private String description;
	
	/** The start date. */
	private String startDate;
	
	/** The end date. */
	private String endDate;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new project.
	 */
	private Project() {
		name = "";
		description = "";
		startDate = "";
		endDate = "";
	}

	/**
	 * New instance.
	 *
	 * @return the project
	 */
	public static Project newInstance() {
		Project project = new Project();
		return project;
	}
	
	public static Project newInstance(
		String name, 
		String description) {

		Project project = new Project();
		project.setName(name);
		project.setDescription(description);
		return project;
	}
	
	
	/**
	 * Creates the copy.
	 *
	 * @param originalProject the original project
	 * @return the project
	 */
	public static Project createCopy(
		final Project originalProject) {

		Project cloneProject = new Project();
		cloneProject.setName(originalProject.getName());
		cloneProject.setDescription(originalProject.getDescription());
		cloneProject.setStartDate(originalProject.getStartDate());
		cloneProject.setEndDate(originalProject.getEndDate());
		
		return cloneProject;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(final String name) {
		
		this.name = name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(final String description) {
		
		this.description = description;
	}

	/**
	 * Gets the start date.
	 *
	 * @return the start date
	 */
	public String getStartDate() {
		
		return startDate;
	}

	/**
	 * Sets the start date.
	 *
	 * @param startDate the new start date
	 */
	public void setStartDate(
		final String startDate) {
		
		this.startDate = startDate;
	}

	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 */
	public String getEndDate() {
		
		return endDate;
	}

	/**
	 * Sets the end date.
	 *
	 * @param endDate the new end date
	 */
	public void setEndDate(final String endDate) {
		
		this.endDate = endDate;
	}
		
	
	public void identifyDifferences(
		final Project anotherProject,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherProject, 
			differences);
		
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherProject the other project
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final Project otherProject) {
			
		if (otherProject == null) {
			return false;
		}
		
		String otherName = otherProject.getName();
		String otherDescription = otherProject.getDescription();
		String otherStartDate = otherProject.getStartDate();
		String otherEndDate = otherProject.getEndDate();
		
		Collator collator = Collator.getInstance();
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (name != null) {
			//they must both be non-null
			if (collator.equals(name, otherName) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(description, otherDescription)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (description != null) {
			//they must both be non-null
			if (collator.equals(description, otherDescription) == false) {
				return false;
			}			
		}		

		if (FieldValidationUtility.hasDifferentNullity(startDate, otherStartDate)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (startDate != null) {
			//they must both be non-null
			if (collator.equals(startDate, otherStartDate) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(endDate, otherEndDate)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (endDate != null) {
			//they must both be non-null
			if (collator.equals(endDate, otherEndDate) == false) {
				return false;
			}		
		}
		
		return super.hasIdenticalContents(otherProject);		
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


	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();
		
		String recordType = getRecordType();
		if (name != null) {
			String nameFieldName
				= RIFServiceMessages.getMessage("project.name.label");
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				nameFieldName, 
				name);			
		}

		if (description != null) {
			String descriptionFieldName
				= RIFServiceMessages.getMessage("project.description.label");
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				descriptionFieldName, 
				description);			
		}

		if (startDate != null) {
			String startDateFieldName
				= RIFServiceMessages.getMessage("project.startDate.label");
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				startDateFieldName, 
				startDate);			
		}
		
		if (endDate != null) {
			String startDateFieldName
				= RIFServiceMessages.getMessage("project.endDate.label");
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				startDateFieldName, 
				endDate);			
		}
	}
		

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordType = getRecordType();
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		
		String nameFieldName
			= RIFServiceMessages.getMessage("project.name.label");
		if (fieldValidationUtility.isEmpty(getName())) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					nameFieldName);
			errorMessages.add(errorMessage);			
		}
		
		String descriptionFieldName
			= RIFServiceMessages.getMessage("project.description.label");
		if (fieldValidationUtility.isEmpty(getDescription())) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					descriptionFieldName);
			errorMessages.add(errorMessage);			
		}
		
		String startDateFieldName
			= RIFServiceMessages.getMessage("project.startDate.label");
		String startDatePhrase = getStartDate();
		Date startDate = null;
		if (fieldValidationUtility.isEmpty(startDatePhrase)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					startDateFieldName);
			errorMessages.add(errorMessage);			
		}
		else {
			startDate = GENERIC_MESSAGES.getDate(startDatePhrase);		
			if (startDate == null) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.invalidDateFormat",
						recordType,
						startDateFieldName,
						startDatePhrase);
				errorMessages.add(errorMessage);			
			}
		}
		
		
		String endDateFieldName
			= RIFServiceMessages.getMessage("project.endDate.label");
		String endDatePhrase = getEndDate();
		Date endDate = null;

		/*
		if (fieldValidationUtility.isEmpty(endDatePhrase)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					endDateFieldName);
			errorMessages.add(errorMessage);			
		}
		*/
		
		if (fieldValidationUtility.isEmpty(endDatePhrase) == false) {
//		else {
			endDate = GENERIC_MESSAGES.getDate(endDatePhrase);		
			if (endDate == null) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.invalidDateFormat",
						recordType,
						endDateFieldName,
						endDatePhrase);
				errorMessages.add(errorMessage);
			}
		}	
		
		countErrors(RIFServiceError.INVALID_PROJECT, errorMessages);
	}


	public String getRecordType() {

		String recordType
			= RIFServiceMessages.getMessage("project.label");
		return recordType;
	}
	

	public String getDisplayName() {

		return name;
	}
}
