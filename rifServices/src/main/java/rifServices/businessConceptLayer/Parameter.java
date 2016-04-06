
package rifServices.businessConceptLayer;

import rifGenericLibrary.dataStorageLayer.DisplayableItemSorter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.businessConceptLayer.AbstractRIFConcept;


import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifGenericLibrary.util.FieldValidationUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.text.Collator;


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
 * Copyright 2014 Imperial College London, developed by the Small Area
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


public final class Parameter 
	extends AbstractRIFConcept {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The name. */
	private String name;
	
	/** The value. */
	private String value;
    
// ==========================================
// Section Construction
// ==========================================

	/**
	 * Instantiates a new parameter.
	 *
	 * @param name the name
	 * @param value the value
	 */
	private Parameter(
		final String name, 
		final String value) {

		this.name = name;
		this.value = value;
	}

    /**
     * Instantiates a new parameter.
     */
    private Parameter() {
		name = "";
		value = "";
    }

	/**
	 * New instance.
	 *
	 * @return the parameter
	 */
	static public Parameter newInstance() {
		Parameter parameter = new Parameter();
		return parameter;
	}

	/**
	 * New instance.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the parameter
	 */
	static public Parameter newInstance(
		final String name, 
		final String value) {
		
		Parameter parameter = new Parameter(name, value);
		return parameter;
	}

	/**
	 * Creates the copy.
	 *
	 * @param originalParameters the original parameters
	 * @return the array list
	 */
	static public ArrayList<Parameter> createCopy(
		final ArrayList<Parameter> originalParameters) {

		if (originalParameters == null) {
			return null;
		}
		
		ArrayList<Parameter> cloneParameters = new ArrayList<Parameter>();
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

	public void identifyDifferences(
		final Parameter anotherParameter,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherParameter, 
			differences);		
	}
	
	/**
	 * Could be useful for identifying which parameters are in one 
	 * list but not the other
	 * @param nameOfListOwnerA
	 * @param parameterListA
	 * @param nameOfListOwnerB
	 * @param parameterListB
	 * @return
	 */
	public static ArrayList<String> identifyDifferences(
		final String nameOfListOwnerA,
		final ArrayList<Parameter> parameterListA,
		final String nameOfListOwnerB,
		final ArrayList<Parameter> parameterListB) {
		
		ArrayList<String> differences 
			= new ArrayList<String>();
		
		return differences;
	}
	
	
	/**
	 * Checks for identical contents.
	 *
	 * @param parameterListA the parameter list a
	 * @param parameterListB the parameter list b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<Parameter> parameterListA, 
		final ArrayList<Parameter> parameterListB) {

		if (FieldValidationUtility.hasDifferentNullity(
			parameterListA, 
			parameterListB)) {
			//reject if one is null and the other is non-null
			return false;
		}
			
		if (parameterListA.size() != parameterListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
			
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<Parameter> parametersA = sortParameters(parameterListA);
		ArrayList<Parameter> parametersB = sortParameters(parameterListB);
			
		int numberOfCalculationMethods = parametersA.size();
		for (int i = 0; i < numberOfCalculationMethods; i++) {
			Parameter parameterA
				= parametersA.get(i);				
			Parameter parameterB
				= parametersB.get(i);
			if (parameterA.hasIdenticalContents(parameterB) == false) {					
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
	private static ArrayList<Parameter> sortParameters(
		final ArrayList<Parameter> parameters) {
		
		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (Parameter parameter : parameters) {
			sorter.addDisplayableListItem(parameter);
		}

		ArrayList<Parameter> results = new ArrayList<Parameter>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			Parameter sortedParameter 
				= (Parameter) sorter.getItemFromIdentifier(identifier);
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
			if (collator.equals(name, otherName) == false) {
				
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
			if (collator.equals(value, otherValue) == false) {
					
				return false;
			}			
		}
				
		return super.hasIdenticalContents(otherParameter);
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================

	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();
				String recordType = getRecordType();
		
		//Extract field names
		String nameFieldLabel
			= RIFServiceMessages.getMessage("parameter.name.label");
		String valueFieldLabel
			= RIFServiceMessages.getMessage("parameter.value.label");
	
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
	

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {
					
		String recordType = getRecordType();
		
		//Extract field names
		String nameFieldLabel
			= RIFServiceMessages.getMessage("parameter.name.label");
		String valueFieldLabel
			= RIFServiceMessages.getMessage("parameter.value.label");
				
		ArrayList<String> errorMessages = new ArrayList<String>();
		FieldValidationUtility fieldValidationUtility 
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					nameFieldLabel);
			errorMessages.add(errorMessage);			
		}
			
		if (fieldValidationUtility.isEmpty(value)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					valueFieldLabel);
			errorMessages.add(errorMessage);			
		}
		
		countErrors(RIFServiceError.INVALID_PARAMETER, errorMessages);

	}
	
	/**
	 * Identify duplicate parameters within list.
	 *
	 * @param parameters the parameters
	 * @return the string
	 */
	public static String identifyDuplicateParametersWithinList(
		final ArrayList<Parameter> parameters) {		

		ArrayList<Parameter> duplicateParameters = new ArrayList<Parameter>();
		
		HashSet<String> existingNames = new HashSet<String>();
		for (Parameter parameter : parameters) {
			
			String currentParameterName = parameter.getName();
			if (existingNames.contains(currentParameterName) == true) {
				duplicateParameters.add(parameter);				
			}
			else {
				existingNames.add(currentParameterName);
			}			
		}
		
		if (duplicateParameters.isEmpty() ) {
			//return a null value to indicate no duplicates found
			return null;
		}
		else {
			StringBuilder duplicateParameterListing = new StringBuilder();
			for (int i = 0; i < duplicateParameters.size(); i++) {
				if (i != 0) {
					duplicateParameterListing.append(",");
				}
				duplicateParameterListing.append(duplicateParameters.get(i).getDisplayName());
			}
			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"parameter.error.duplicateParameters", 
					duplicateParameterListing.toString());

			return errorMessage;
		}		
	}
// ==========================================
// Section Interfaces
// ==========================================


	@Override
	public String getDisplayName() {

		StringBuilder buffer = new StringBuilder();
		buffer.append(name);
		buffer.append("-");
		buffer.append(value);
		
		return buffer.toString();
	}
	
// ==========================================
// Section Override
// ==========================================
	

@Override
	public String getRecordType() {
		String recordNameLabel
			= RIFServiceMessages.getMessage("parameter.label");
		return recordNameLabel;
	}



}
