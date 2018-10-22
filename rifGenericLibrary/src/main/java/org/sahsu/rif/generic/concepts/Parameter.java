
package org.sahsu.rif.generic.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

public final class Parameter
		implements DisplayableListItemInterface {

	private static Messages GENERIC_MESSAGES = Messages.genericMessages();

	// Use the "null object" pattern instead of returning null.
	private static final Parameter NULL_PARAM = new Parameter();
	
	/** The name. */
	private String name;
	
	/** The value. */
	private String value;

	/**
	 * Instantiates a new parameter.
	 *
	 * @param name the name
	 * @param value the value
	 */
	private Parameter(final String name, final String value) {

		this.name = name;
		this.value = value;
	}

    /**
     * Instantiates a new parameter.
     */
    private Parameter() {

	    this("","");
    }

	/**
	 * New instance.
	 *
	 * @return the parameter
	 */
	public static Parameter newInstance() {

		return new Parameter();
	}

	/**
	 * New instance.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the parameter
	 */
	public static Parameter newInstance(final String name, final String value) {

		return new Parameter(name, value);
	}

	/**
	 * Creates the copy.
	 *
	 * @param originalParameters the original parameters
	 * @return the array list
	 */
	static public List<Parameter> createCopy(final List<Parameter> originalParameters) {

		if (originalParameters == null) {
			return null;
		}
		
		List<Parameter> cloneParameters = new ArrayList<>();
		for (Parameter originalParameter : originalParameters) {
			cloneParameters.add(createCopy(originalParameter));
		}

		return cloneParameters;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param parameter the parameter
	 * @return the parameter
	 */
	static public Parameter createCopy(
		final Parameter parameter) {

		if (parameter == null) {
			return null;
		}
		
		Parameter cloneParameter = new Parameter();		
		cloneParameter.setName(parameter.getName());
		cloneParameter.setValue(parameter.getValue());
		
		return cloneParameter;
	}

	/**
	 * Returns a {@code Parameter} object if the provided {@code List} contains one whose name
	 * matches the provided {@code targetParameterName}. If no such {@code Parameter} exists, the
	 * {@code NULL_PARAM} singleton is returned.
	 * @param targetParameterName the name of the required parameter
	 * @param parameters the list of parameters to search
	 * @return the matching parameter, or {@code NULL_PARAM}
	 */
	public static Parameter getParameter(final String targetParameterName,
			final List<Parameter> parameters) {
		
		for (Parameter parameter : parameters) {
			String parameterName
				= parameter.getName();
			if (parameterName.equals(targetParameterName)) {
				return parameter;
			}
		}
		
		return NULL_PARAM;
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
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(
		final String value) {

		this.value = value;
	}

	/**
	 * Checks for identical contents.
	 *
	 * @param parameterListA the parameter list a
	 * @param parameterListB the parameter list b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(final List<Parameter> parameterListA,
			final List<Parameter> parameterListB) {

		if (FieldValidationUtility.hasDifferentNullity(parameterListA, parameterListB)) {
			//reject if one is null and the other is non-null
			return false;
		}
			
		if (parameterListA.size() != parameterListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
			
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		List<Parameter> parametersA = sortParameters(parameterListA);
		List<Parameter> parametersB = sortParameters(parameterListB);
			
		int numberOfCalculationMethods = parametersA.size();
		for (int i = 0; i < numberOfCalculationMethods; i++) {
			Parameter parameterA
				= parametersA.get(i);				
			Parameter parameterB
				= parametersB.get(i);
			if (!parameterA.hasIdenticalContents(parameterB)) {
				return false;
			}			
		}
		
		return true;
	}

	/**
	 * Sort parameters.
	 *
	 * @param parameters the parameters
	 * @return the array list
	 */
	private static List<Parameter> sortParameters(final List<Parameter> parameters) {
		
		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (Parameter parameter : parameters) {
			sorter.addDisplayableListItem(parameter);
		}

		List<Parameter> results = new ArrayList<>();
		List<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			Parameter sortedParameter = (Parameter) sorter.getItemFromIdentifier(identifier);
			results.add(sortedParameter);
		}
	
		return results;
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherParameter the other parameter
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final Parameter otherParameter) {

		if (otherParameter == null) {
			return false;
		}
		
		Collator collator = Collator.getInstance();
		
		String otherName = otherParameter.getName();
		String otherValue = otherParameter.getValue();

		
		if (FieldValidationUtility.hasDifferentNullity(
			name, 
			otherName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (name != null) {
			//they must both be non-null
			if (!collator.equals(name, otherName)) {
				
				return false;
			}			
		}

		if (FieldValidationUtility.hasDifferentNullity(
			value, 
			otherValue)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (value != null) {
			//they must both be non-null
			return collator.equals(value, otherValue);
		}
				
		return true;
	}

	public void checkSecurityViolations() throws RIFServiceSecurityException {
		
		String recordType = getRecordType();
		
		//Extract field names
		String nameFieldLabel
			= GENERIC_MESSAGES.getMessage("parameter.name.label");
		String valueFieldLabel
			= GENERIC_MESSAGES.getMessage("parameter.value.label");
	
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			nameFieldLabel, 
			name);
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			valueFieldLabel, 
			value);
	}

	public void checkErrors() throws RIFServiceException {
					
		String recordType = getRecordType();
		
		//Extract field names
		String nameFieldLabel
			= GENERIC_MESSAGES.getMessage("parameter.name.label");
		String valueFieldLabel
			= GENERIC_MESSAGES.getMessage("parameter.value.label");
				
		ArrayList<String> errorMessages = new ArrayList<>();
		FieldValidationUtility fieldValidationUtility 
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					nameFieldLabel);
			errorMessages.add(errorMessage);			
		}
			
		if (fieldValidationUtility.isEmpty(value)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					valueFieldLabel);
			errorMessages.add(errorMessage);			
		}
		
		fieldValidationUtility.throwExceptionIfErrorsFound(RIFGenericLibraryError.INVALID_PARAMETER,
		                                                   errorMessages);
	}
	
	/**
	 * Identify duplicate parameters within list.
	 *
	 * @param parameters the parameters
	 * @return the string
	 */
	public static String identifyDuplicateParametersWithinList(final List<Parameter> parameters) {

		List<Parameter> duplicateParameters = new ArrayList<>();
		
		HashSet<String> existingNames = new HashSet<>();
		for (Parameter parameter : parameters) {
			
			String currentParameterName = parameter.getName();
			if (existingNames.contains(currentParameterName)) {
				duplicateParameters.add(parameter);				
			} else {
				existingNames.add(currentParameterName);
			}			
		}
		
		if (duplicateParameters.isEmpty() ) {
			//return a null value to indicate no duplicates found
			return null;
		} else {
			StringBuilder duplicateParameterListing = new StringBuilder();
			for (int i = 0; i < duplicateParameters.size(); i++) {
				if (i != 0) {
					duplicateParameterListing.append(",");
				}
				duplicateParameterListing.append(duplicateParameters.get(i).getDisplayName());
			}

			return GENERIC_MESSAGES.getMessage("parameter.error.duplicateParameters",
			                                   duplicateParameterListing.toString());
		}		
	}

	public String getDisplayName() {

		return name + "-" + value;
	}

	public String getIdentifier() {
		return name;
	}

	public String getRecordType() {
		return GENERIC_MESSAGES.getMessage("parameter.label");
	}
}
