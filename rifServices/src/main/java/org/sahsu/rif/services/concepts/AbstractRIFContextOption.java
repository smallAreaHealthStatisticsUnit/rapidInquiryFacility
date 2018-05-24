package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 *
 * Manages generic properties of many of the options that users apply to create
 * their studies.  The properties for "name" and "description" are mainly meant
 * to help foster display and context senstive help.
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

public abstract class AbstractRIFContextOption 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The name. */
	private String name;
	
	/** The description. */
	private String description;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new abstract rif context option.
	 */
	protected AbstractRIFContextOption() {

		name = "";
		description = "";
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
	public void setName(
		final String name) {
		
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
	public void setDescription(
		final String description) {

		this.description = description;
	}

	public void identifyDifferences(
		final AbstractRIFContextOption anotherRIFContextOption,
		final ArrayList<String> differences) {
	
		super.identifyDifferences(
			anotherRIFContextOption, 
			differences);

	}
		
	/**
	 * Checks for identical contents.
	 *
	 * @param otherRIFContextOption the other rif context option
	 * @return true, if successful
	 */
	protected boolean hasIdenticalContents(
		final AbstractRIFContextOption otherRIFContextOption) {		

		if (otherRIFContextOption == null) {
			return false;
		}
		
		String otherName = otherRIFContextOption.getName();
		String otherDescription = otherRIFContextOption.getDescription();
		
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is not
			return false;
		}
		else if (name != null) {
			//both must be non-null values
			Collator collator = Collator.getInstance();
			if (collator.equals(name, otherRIFContextOption.getName()) == false) {
				return false;
			}
		}
		
		if (FieldValidationUtility.hasDifferentNullity(description, otherDescription)) {
			//reject if one is null and the other is not
			return false;
		}
		else if (description != null) {
			//both must be non-null values
			Collator collator = Collator.getInstance();
			if (collator.equals(description, otherDescription) == false) {
				return false;
			}
		}
		
		return super.hasIdenticalContents(otherRIFContextOption);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	/**
	 * Checks if is blank.
	 *
	 * @return true, if is blank
	 */
	public boolean isBlank() {

		FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();	
		if (fieldValidationUtility.isEmpty(name)) {
			return true;
		}
		
		return false;
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {	
		
		String recordType = getRecordType();
		String nameFieldName
			= RIFServiceMessages.getMessage("abstractGeographicalLevel.name.label");
		
		super.checkSecurityViolations();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			nameFieldName, 
			name);

		String descriptionFieldName
			= RIFServiceMessages.getMessage("abstractGeographicalLevel.description.label");		
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			descriptionFieldName, 
			description);		
	}
	
	/**
	 * Check errors.
	 *
	 * @param rifServiceError the rif service error
	 * @param errorMessages the error messages
	 * @throws RIFServiceException the RIF service exception
	 */
	protected void checkErrors(
		final ValidationPolicy validationPolicy,
		final RIFServiceError rifServiceError,
		final ArrayList<String> errorMessages) 
		throws RIFServiceException {
		
		String recordType = getRecordType();
		
		String nameFieldName
			= RIFServiceMessages.getMessage("abstractGeographicalLevel.name.label");
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					nameFieldName);
			errorMessages.add(errorMessage);
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	@Override
	public String getDisplayName() {
		
		return getName();
	}	
	
}
