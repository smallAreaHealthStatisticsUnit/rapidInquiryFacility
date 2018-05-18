package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
* Describes the basic properties that a covariate should have.  
* <p>
* <font color="red">Area under development</color>
* <p>
* Currently, covariates must have values which are banded.  In future, we 
* may want to support continuous variables.  In order to do that, we need
* to have broader discussion about 
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

abstract public class AbstractCovariate 
	extends AbstractRIFConcept {

// ==========================================
// Section Constants
// ==========================================

	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
    
    /** The covariate type. */
    private CovariateType covariateType;	
	/** The maximum value. */
	private String maximumValue;
	/** The minimum value. */
	private String minimumValue;
    /** The name. */
	private String name;
	

// ==========================================
// Section Construction
// ==========================================
 
	/**
 * Instantiates a new abstract covariate.
 *
 * @param name the name
 * @param minimumValue the minimum value
 * @param maximumValue the maximum value
 */
	protected AbstractCovariate(
		final String name,
		final String minimumValue,
		final String maximumValue) {
		
		this.name = name;
		this.minimumValue = minimumValue;
		this.maximumValue = maximumValue;
	}
	
	/**
	 * Instantiates a new abstract covariate.
	 */
	protected AbstractCovariate() {
		
        name = "";
		minimumValue = "";
		maximumValue = "";
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
     * Gets the covariate type.
     *
     * @return the covariate type
     */
    public CovariateType getCovariateType() {
    	
        return covariateType;
    }

    /**
     * Sets the covariate type.
     *
     * @param covariateType the new covariate type
     */
    public void setCovariateType(
    	final CovariateType covariateType) {
    	
        this.covariateType = covariateType;
    }
	
	/**
	 * Gets the minimum value.
	 *
	 * @return the minimum value
	 */
	public String getMinimumValue() {
		
		return minimumValue;
	}

	/**
	 * Sets the minimum value.
	 *
	 * @param minimumValue the new minimum value
	 */
	public void setMinimumValue(
		final String minimumValue) {
		
		this.minimumValue = minimumValue;
	}

	/**
	 * Gets the maximum value.
	 *
	 * @return the maximum value
	 */
	public String getMaximumValue() {
		
		return maximumValue;
	}

	/**
	 * Sets the maximum value.
	 *
	 * @param maximumValue the new maximum value
	 */
	public void setMaximumValue(
		final String maximumValue) {
		
		this.maximumValue = maximumValue;
	}
	
	public abstract String getRecordType();
	
	
	public void identifyDifferences(
		final AbstractCovariate anotherCovariate,
		final ArrayList<String> differences) {
		

	}
	
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherCovariate the other covariate
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final AbstractCovariate otherCovariate) {

		Collator collator = GENERIC_MESSAGES.getCollator();
		
		String otherName = otherCovariate.getName();
		String otherMinimumValue = otherCovariate.getMinimumValue();
		String otherMaximumValue = otherCovariate.getMaximumValue();
		CovariateType otherCovariateType = otherCovariate.getCovariateType();

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is non-null
			String errorMessage
				= RIFServiceMessages.getMessage("");
			errorMessages.add(errorMessage);
		}
		else if (name != null) {
			//they must both be non-null
			if (collator.equals(name, otherName) == false) {
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
		
		if (FieldValidationUtility.hasDifferentNullity(minimumValue, otherMinimumValue)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (minimumValue != null) {
			//they must both be non-null
			if (collator.equals(minimumValue, otherMinimumValue) == false) {
				return false;
			}
		}
		
		if (FieldValidationUtility.hasDifferentNullity(maximumValue, otherMaximumValue)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (maximumValue != null) {
			//they must both be non-null
			if (collator.equals(maximumValue, otherMaximumValue) == false) {
				return false;
			}
		}

		if (covariateType != otherCovariateType) {
			return false;
		}
		
		return true;
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================
	
	@Override
	public void checkSecurityViolations()
		throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();
		
		//Obtain field names for possible error messages
		String recordType = getRecordType();
		String nameLabel
			= RIFServiceMessages.getMessage("covariate.name.label");
		String minimumFieldNameLabel
			= RIFServiceMessages.getMessage("covariate.minimum.label");
		String maximumFieldNameLabel
			= RIFServiceMessages.getMessage("covariate.maximum.label");

		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValdidationUtility
			= new FieldValidationUtility();
		fieldValdidationUtility.checkMaliciousCode(
			recordType, 
			nameLabel,
			name);
		fieldValdidationUtility.checkMaliciousCode(
			recordType, 
			minimumFieldNameLabel,
			minimumValue);
		fieldValdidationUtility.checkMaliciousCode(
			recordType, 
			maximumFieldNameLabel,
			maximumValue);		
	}
	
	/**
	 * Check errors.
	 *
	 * @param errorMessages the error messages
	 */
	protected void checkErrors(
		final ValidationPolicy validationPolicy,
		final ArrayList<String> errorMessages) {
			
		//Obtain field names for possible error messages
		String recordType = getRecordType();
		String nameLabel
			= RIFServiceMessages.getMessage("covariate.name.label");
		String covariateFieldTypeLabel
			= RIFServiceMessages.getMessage("covariate.covariateType.label");
		String minimumFieldNameLabel
			= RIFServiceMessages.getMessage("covariate.minimum.label");
		String maximumFieldNameLabel
			= RIFServiceMessages.getMessage("covariate.maximum.label");
		
		FieldValidationUtility fieldValidationUtility 
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage 
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					nameLabel);
			errorMessages.add(errorMessage);
		} 
		
		if (validationPolicy == ValidationPolicy.STRICT) {
		
			if (covariateType == null) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.undefinedObject", 
						recordType,
						covariateFieldTypeLabel);
				errorMessages.add(errorMessage);
			}
		
			if (fieldValidationUtility.isEmpty(minimumValue)) {				
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField",
						recordType,
						minimumFieldNameLabel);
				errorMessages.add(errorMessage);
			}

			if (fieldValidationUtility.isEmpty(maximumValue)) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField",
						recordType,
						maximumFieldNameLabel);
				errorMessages.add(errorMessage);			
			}
		
			if ( (fieldValidationUtility.isEmpty(minimumValue) == false) && 
				(fieldValidationUtility.isEmpty(maximumValue) == false) &&
				(covariateType != null)) {
			
				Double minimumDoubleValue = null;
				Double maximumDoubleValue = null;
				
				try {
					minimumDoubleValue = Double.valueOf(minimumValue);
				}
				catch(NumberFormatException numberFormatException) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"covariate.minimum.error.invalidContinuousMinimum", 
							minimumValue);
					errorMessages.add(errorMessage);
				}					
				
				try {
					maximumDoubleValue = Double.valueOf(maximumValue);
				}
				catch(NumberFormatException numberFormatException) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"covariate.minimum.error.invalidContinuousMaximum",
							maximumValue);
					errorMessages.add(errorMessage);
				}
					
				if ((minimumDoubleValue != null) && (maximumDoubleValue != null) ) {
					if (minimumDoubleValue.doubleValue() > maximumDoubleValue.doubleValue() ) {
						String errorMessage
							= RIFServiceMessages.getMessage(
								"covariate.boundaries.error.minMoreThanMax",
								String.valueOf(minimumDoubleValue),
								String.valueOf(maximumDoubleValue));
						errorMessages.add(errorMessage);
					}
				}					
			}
		}
	}
// ==========================================
// Section Interfaces
// ==========================================
	
// ==========================================
// Section Override
// ==========================================

	//Displayable List Item	
	@Override
	public String getDisplayName() {
		StringBuilder result = new StringBuilder();
		result.append(getRecordType());
		result.append("-");
		result.append(name);
		return result.toString();
	}
	
	@Override
	public String getIdentifier() {
		return name;
	}
}
