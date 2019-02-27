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
*/
public abstract class AbstractCovariate extends AbstractRIFConcept {

	public enum Type {

		INTEGER_SCORE(1),
		CONTINUOUS_VARIABLE(2),
		UNKNOWN_TYPE(-1);

		private final int numericType;
		Type(final int type) {
			this.numericType = type;
		}

		public String stringValue() {
			return String.valueOf(numericType);
		}

		public static Type fromNumber(final Number n) {

			switch (n.intValue()) {
				case 1:
					return INTEGER_SCORE;
				case 2:
					return CONTINUOUS_VARIABLE;
			}

			// Shouldn't happen
			return UNKNOWN_TYPE;
		}
	}

	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();

	private String maximumValue;
	private String minimumValue;
	private String name;
	private String description;
	private Type type;

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
	public void setMaximumValue(final String maximumValue) {
		
		this.maximumValue = maximumValue;
	}

	public Type getType() {
		return type;
	}

	public AbstractCovariate setType(final Type type) {
		this.type = type;
		return this;
	}

	public abstract String getRecordType();

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void identifyDifferences(final AbstractCovariate anotherCovariate,
			final ArrayList<String> differences) {
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherCovariate the other covariate
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(final AbstractCovariate otherCovariate) {

		Collator collator = GENERIC_MESSAGES.getCollator();
		
		String otherName = otherCovariate.getName();
		String otherMinimumValue = otherCovariate.getMinimumValue();
		String otherMaximumValue = otherCovariate.getMaximumValue();

		ArrayList<String> errorMessages = new ArrayList<>();
		
		
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is non-null
			String errorMessage
				= RIFServiceMessages.getMessage("");
			errorMessages.add(errorMessage);
		}
		else if (name != null) {
			//they must both be non-null
			if (!collator.equals(name, otherName)) {
				return false;
			}
		}
		
		
		
		
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (name != null) {
			//they must both be non-null
			if (!collator.equals(name, otherName)) {
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
				(fieldValidationUtility.isEmpty(maximumValue) == false)) {
			
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
