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

public final class AgeGroup 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();

	/**
	 * The Enum DisplayNameOption.
	 */
	private enum DisplayNameOption {
		
		/** The lower limit only. */
		LOWER_LIMIT_ONLY, 
		
		/** The upper limit only. */
		UPPER_LIMIT_ONLY, 
		
		/** The both lower and upper limits. */
		BOTH_LOWER_AND_UPPER_LIMITS};
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The lower limit. */
	private String lowerLimit;
	
	/** The upper limit. */
	private String upperLimit;
	
	/** The name. */
	private String name;
	
	/** The display name option. */
	private DisplayNameOption displayNameOption;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new age group.
	 */
	private AgeGroup() {
		
		lowerLimit = "";
		upperLimit = "";
		name = "";
		displayNameOption = DisplayNameOption.BOTH_LOWER_AND_UPPER_LIMITS;
	}

	/**
	 * New instance.
	 *
	 * @return the age group
	 */
	public static AgeGroup newInstance() {
		
		AgeGroup ageGroup = new AgeGroup();
		return ageGroup;
	}
	
	/**
	 * New instance.
	 *
	 * @param identifier the identifier
	 * @param lowerLimit the lower limit
	 * @param upperLimit the upper limit
	 * @param name the name
	 * @return the age group
	 */
	public static AgeGroup newInstance(
		final String identifier,
		final String lowerLimit,
		final String upperLimit,
		final String name) {
		
		AgeGroup ageGroup = new AgeGroup();
		ageGroup.setIdentifier(identifier);
		ageGroup.setLowerLimit(lowerLimit);
		ageGroup.setUpperLimit(upperLimit);
		ageGroup.setName(name);
		
		return ageGroup;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalAgeGroup the original age group
	 * @return the age group
	 */
	public static AgeGroup createCopy(
		final AgeGroup originalAgeGroup) {
		
		if (originalAgeGroup == null) {
			return null;
		}
		
		AgeGroup cloneAgeGroup = new AgeGroup();
		cloneAgeGroup.setIdentifier(originalAgeGroup.getIdentifier());
		cloneAgeGroup.setLowerLimit(originalAgeGroup.getLowerLimit());
		cloneAgeGroup.setUpperLimit(originalAgeGroup.getUpperLimit());
		cloneAgeGroup.setName(originalAgeGroup.getName());
		
		return cloneAgeGroup;
	}
	
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the lower limit.
	 *
	 * @return the lower limit
	 */
	public String getLowerLimit() {
		
		return lowerLimit;
	}

	/**
	 * Sets the lower limit.
	 *
	 * @param lowerLimit the new lower limit
	 */
	public void setLowerLimit(
		final String lowerLimit) {
		
		this.lowerLimit = lowerLimit;
	}

	/**
	 * Gets the upper limit.
	 *
	 * @return the upper limit
	 */
	public String getUpperLimit() {
		
		return upperLimit;
	}

	/**
	 * Sets the upper limit.
	 *
	 * @param upperLimit the new upper limit
	 */
	public void setUpperLimit(
		final String upperLimit) {
		
		this.upperLimit = upperLimit;
	}

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
	
	public void identifyDifferences(
		final AgeGroup anotherAgeGroup,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherAgeGroup, 
			differences);
		
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherAgeGroup the other age group
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final AgeGroup otherAgeGroup) {
		
		if (otherAgeGroup == null) {
			return false;
		}
		
		String otherLowerLimit 
			= otherAgeGroup.getLowerLimit();
		String otherUpperLimit
			= otherAgeGroup.getUpperLimit();
		String otherName
			= otherAgeGroup.getName();
		
		Collator collator = Collator.getInstance();
		if (FieldValidationUtility.hasDifferentNullity(lowerLimit, otherLowerLimit)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (lowerLimit != null) {
			//they must both be non-null
			if (collator.equals(lowerLimit, otherLowerLimit) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(upperLimit, otherUpperLimit)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (upperLimit != null) {
			//they must both be non-null
			if (collator.equals(upperLimit, otherUpperLimit) == false) {
				return false;
			}			
		}
		
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
				
		return super.hasIdenticalContents(otherAgeGroup);		
	}
	
	/**
	 * Show only lower limit in display name.
	 */
	public void showOnlyLowerLimitInDisplayName() {
		
		displayNameOption = DisplayNameOption.LOWER_LIMIT_ONLY;
	}
	
	/**
	 * Show only upper limit in display name.
	 */
	public void showOnlyUpperLimitInDisplayName() {
		
		displayNameOption = DisplayNameOption.UPPER_LIMIT_ONLY;	
	}
	
	/**
	 * Show lower and upper limit in display name.
	 */
	public void showLowerAndUpperLimitInDisplayName() {
		
		displayNameOption = DisplayNameOption.BOTH_LOWER_AND_UPPER_LIMITS;		
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

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordType
			= RIFServiceMessages.getMessage("ageGroup.label");

		String nameFieldName
			= RIFServiceMessages.getMessage("ageGroup.name.label");
		String nameFieldValue = getName();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		
		if (fieldValidationUtility.isEmpty(nameFieldValue)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					nameFieldName);
			errorMessages.add(errorMessage);
		}
		
		if (validationPolicy == ValidationPolicy.STRICT) {
			
			//Test whether lower limit and upper limit are numbers
			String lowerLimitFieldName
				= RIFServiceMessages.getMessage("ageGroup.lowerLimit.label");
			String lowerLimitFieldValue = getLowerLimit();
			Integer lowerLimitNumber = null;
			if (fieldValidationUtility.isEmpty(lowerLimitFieldValue)) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordType,
						lowerLimitFieldName);
				errorMessages.add(errorMessage);
			}
			else { 
				try {
					lowerLimitNumber = Integer.valueOf(lowerLimitFieldValue);			
				}
				catch(NumberFormatException numberFormatException) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"general.validation.nonNumericRecordField",
							recordType,
							lowerLimitFieldName,
							lowerLimitFieldValue);
					errorMessages.add(errorMessage);
				}
			}
		
			String upperLimitFieldName
				= RIFServiceMessages.getMessage("ageGroup.upperLimit.label");
			String upperLimitFieldValue = getUpperLimit();
			Integer upperLimitNumber = null;
			if (fieldValidationUtility.isEmpty(upperLimitFieldValue)) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordType,
						upperLimitFieldName,
						upperLimitFieldValue);
				errorMessages.add(errorMessage);
			}
			else {
				try {
					upperLimitNumber = Integer.valueOf(upperLimitFieldValue);
				}
				catch(NumberFormatException numberFormatException) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"general.validation.nonNumericRecordField",
							recordType,
							upperLimitFieldName,
							upperLimitFieldValue);
					errorMessages.add(errorMessage);
				}
			}
		
			if ((lowerLimitNumber != null) && (upperLimitNumber) != null) {
				if (lowerLimitNumber.intValue() > upperLimitNumber.intValue()) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"ageGroup.error.lowerGreaterthanUpperLimit",
							lowerLimitFieldValue,
							upperLimitFieldValue);
					errorMessages.add(errorMessage);
				}
			}
		}
		
		
		countErrors(RIFServiceError.INVALID_AGE_GROUP, errorMessages);
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();
		
		String recordType = getRecordType();
		String nameFieldName
			= RIFServiceMessages.getMessage("ageGroup.name.label");
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		fieldValidationUtility.checkMaliciousCode(
			recordType,
			nameFieldName, 
			name);		
		
		String lowerLimitFieldName
			= RIFServiceMessages.getMessage("ageGroup.lowerLimit.label");
		fieldValidationUtility.checkMaliciousCode(
			recordType,
			lowerLimitFieldName, 
			lowerLimit);
		String upperLimitFieldName
			= RIFServiceMessages.getMessage("ageGroup.upperLimit.label");
		fieldValidationUtility.checkMaliciousCode(
			recordType,
			upperLimitFieldName, 
			upperLimit);
	}
	

	@Override
	public String getDisplayName() {
		
		/**
		 * The order of the choices below is meant to help minimise 
		 * the amount of work needed to render display name.  In almost
		 * all cases, it will be both lower and upper limits.  However,
		 * in age bands, it may be desirable to select the lower limit
		 * or the upper limit to render.
		 */
		
		if (displayNameOption == DisplayNameOption.BOTH_LOWER_AND_UPPER_LIMITS) {
			//expected to be the most common case
			return name;			
		}
		else if (displayNameOption == DisplayNameOption.LOWER_LIMIT_ONLY) {
			return lowerLimit;
		}
		else if (displayNameOption == DisplayNameOption.UPPER_LIMIT_ONLY) {
			return upperLimit;
		}
		else {
			//default
			return name;
		}
	}
	

	@Override
	public String getRecordType() {
		
		String recordType
			= RIFServiceMessages.getMessage("ageGroup.label");
		return recordType;
	}

}
