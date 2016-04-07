
package rifServices.businessConceptLayer;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.util.FieldValidationUtility;

import java.util.ArrayList;
import java.text.Collator;
import java.util.Collections;
import java.util.HashMap;


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


public final class CalculationMethod 
	extends AbstractRIFConcept {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The name. */
private String name;
	
	/** The code routine name. */
	private String codeRoutineName;
	
	/** The prior. */
	private CalculationMethodPrior calculationMethodPrior;
	
	/** The description. */
	private String description;
	
	/** The parameters. */
	private ArrayList<Parameter> parameters;
	
// ==========================================
// Section Construction
// ==========================================
	
	private CalculationMethod(
		final String name,
		final String codeRoutineName,
		final String description,
		final CalculationMethodPrior prior,
		final ArrayList<Parameter> parameters) {
		
		this.name = name;
		this.codeRoutineName = codeRoutineName;
		this.description = description;
		this.calculationMethodPrior = prior;
		this.parameters = parameters;
	}
	
	
    /**
     * Instantiates a new calculation method.
     */
	private CalculationMethod() {
    	
		name = "";
		codeRoutineName = "";
		calculationMethodPrior = CalculationMethodPrior.STANDARD_DEVIATION;
		description = "";
		parameters = new ArrayList<Parameter>();
    }

	/**
	 * New instance.
	 *
	 * @return the calculation method
	 */
	static public CalculationMethod newInstance() {
		
		CalculationMethod calculationMethod = new CalculationMethod();
		return calculationMethod;
	}

	/**
	 * New instance.
	 *
	 * @return the calculation method
	 */
	static public CalculationMethod newInstance(
		final String name,
		final String codeRoutineName,
		final String description,
		final CalculationMethodPrior prior,
		final ArrayList<Parameter> parameters) {
		
		CalculationMethod calculationMethod
			= new CalculationMethod(
				name,
				codeRoutineName,
				description,
				prior,
				parameters);
		
		return calculationMethod;
	}
	
	
	/**
	 * Creates the copy.
	 *
	 * @param originalCalculationMethods the original calculation methods
	 * @return the array list
	 */
	static public ArrayList<CalculationMethod> createCopy(
		final ArrayList<CalculationMethod> originalCalculationMethods) {

		if (originalCalculationMethods == null) {
			return null;		
		}
		
		ArrayList<CalculationMethod> clonedCalculationMethods
			= new ArrayList<CalculationMethod>();
		for (CalculationMethod originalCalculationMethod : originalCalculationMethods) {
			clonedCalculationMethods.add(createCopy(originalCalculationMethod));
		}
		
		return clonedCalculationMethods;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalCalculationMethod the original calculation method
	 * @return the calculation method
	 */
	static public CalculationMethod createCopy(
		final CalculationMethod originalCalculationMethod) {

		if (originalCalculationMethod == null) {
			return null;
		}
		
		CalculationMethod cloneCalculationMethod = new CalculationMethod();
		cloneCalculationMethod.setIdentifier(originalCalculationMethod.getIdentifier());
		cloneCalculationMethod.setCodeRoutineName(originalCalculationMethod.getCodeRoutineName());
		cloneCalculationMethod.setDescription(originalCalculationMethod.getDescription());
		cloneCalculationMethod.setName(originalCalculationMethod.getName());
		cloneCalculationMethod.setPrior(originalCalculationMethod.getPrior());
		
		ArrayList<Parameter> clonedParameterList
			= Parameter.createCopy(originalCalculationMethod.getParameters());
		cloneCalculationMethod.setParameters(clonedParameterList);
		return cloneCalculationMethod;
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
	 * Gets the prior.
	 *
	 * @return the prior
	 */
	public CalculationMethodPrior getPrior() {
		
		return calculationMethodPrior;
	}
	
	/**
	 * Sets the prior.
	 *
	 * @param prior the new prior
	 */
	public void setPrior(
		final CalculationMethodPrior prior) {
		
		this.calculationMethodPrior = prior;
	}
	
	/**
	 * Gets the code routine name.
	 *
	 * @return the code routine name
	 */
	public String getCodeRoutineName() {
		
		return codeRoutineName;
	}

	/**
	 * Sets the code routine name.
	 *
	 * @param codeRoutineName the new code routine name
	 */
	public void setCodeRoutineName(
		final String codeRoutineName) {
		
		this.codeRoutineName = codeRoutineName;
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

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public ArrayList<Parameter> getParameters() {
		
		return parameters;
	}

	/**
	 * Adds the parameter.
	 *
	 * @param parameter the parameter
	 */
	public void addParameter(
		final Parameter parameter) {
		
		parameters.add(parameter);
	}
	
	/**
	 * Sets the parameters.
	 *
	 * @param parameters the new parameters
	 */
	public void setParameters(
		final ArrayList<Parameter> parameters) {
		
		this.parameters = parameters;
	}
	
	
	public void identifyDifferences(
		final CalculationMethod anotherCalculationMethod,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherCalculationMethod, 
			differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param calculationMethodsA the calculation methods a
	 * @param calculationMethodsB the calculation methods b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<CalculationMethod> calculationMethodsA, 
		final ArrayList<CalculationMethod> calculationMethodsB) {

		if (FieldValidationUtility.hasDifferentNullity(
			calculationMethodsA, 
			calculationMethodsB)) {
			//reject if one is null and the other is non-null
			return false;
		}
		
		if (calculationMethodsA.size() != calculationMethodsB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
		
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<CalculationMethod> methodsA
			= sortMethodsByCodeRoutineName(calculationMethodsA);
		ArrayList<CalculationMethod> methodsB
			= sortMethodsByCodeRoutineName(calculationMethodsB);
		
		int numberOfCalculationMethods = methodsA.size();
		for (int i = 0; i < numberOfCalculationMethods; i++) {
			CalculationMethod calculationMethodA
				= methodsA.get(i);
			
			CalculationMethod calculationMethodB
				= methodsB.get(i);
			if (calculationMethodA.hasIdenticalContents(calculationMethodB) == false) {
				return false;
			}		
		}
		
		return true;		
	}
	
	/**
	 * Sort methods by code routine name.
	 *
	 * @param calculationMethods the calculation methods
	 * @return the array list
	 */
	private static ArrayList<CalculationMethod> sortMethodsByCodeRoutineName(
		final ArrayList<CalculationMethod> calculationMethods) {
	
		HashMap<String, CalculationMethod> methodFromCodeRoutineName
			= new HashMap<String, CalculationMethod>();
		for (CalculationMethod calculationMethod : calculationMethods) {
			methodFromCodeRoutineName.put(
				calculationMethod.getCodeRoutineName(),
				calculationMethod);
		}
		
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(methodFromCodeRoutineName.keySet());
		
		//@TODO: how well doe this handle duplicates
		ArrayList<CalculationMethod> results = new ArrayList<CalculationMethod>();
		Collections.sort(keys);
		for (String key : keys) {
			results.add(methodFromCodeRoutineName.get(key));
		}
		
		return results;
	}
	
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherCalculationMethod the other calculation method
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final CalculationMethod otherCalculationMethod) {
		
		if (otherCalculationMethod == null) {
			return false;
		}
		
		String otherName 
			= otherCalculationMethod.getName();
		String otherCodeRoutine 
			= otherCalculationMethod.getCodeRoutineName();
		String otherDescription
			= otherCalculationMethod.getDescription();
		
		Collator collator = Collator.getInstance();
		
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is not
			return false;
		}
		else if (name != null) {
			//they must both be non-null
			if (collator.equals(name, otherName) == false) {
				return false;
			}		
		}
		
		if (FieldValidationUtility.hasDifferentNullity(
			codeRoutineName, 
			otherCodeRoutine)) {
			//reject if one is null and the other is not
			return false;
		}
		else if (codeRoutineName != null) {
			//they must both be non-null
			if (collator.equals(codeRoutineName, otherCodeRoutine) == false) {
				return false;
			}		
		}
		
		if (FieldValidationUtility.hasDifferentNullity(
			description, 
			otherDescription)) {

			//reject if one is null and the other is not
			return false;
		}
		else if (description != null) {
			//they must both be non-null
			if (collator.equals(description, otherDescription) == false) {
				return false;
			}		
		}

		ArrayList<Parameter> otherParameters
			= otherCalculationMethod.getParameters();		
		if (Parameter.hasIdenticalContents(parameters, otherParameters) == false) {
			return false;
		}

		return super.hasIdenticalContents(otherCalculationMethod);
	}
// ==========================================
// Section Errors and Validation
// ==========================================
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();	
		
		String recordType = getRecordType();
		
		String nameFieldNameLabel
			= RIFServiceMessages.getMessage("calculationMethod.name.label");
		String codeRoutineFieldNameLabel
			= RIFServiceMessages.getMessage("calculationMethod.codeRoutineName.label");
		String descriptionFieldNameLabel
			= RIFServiceMessages.getMessage("calculationMethod.description.label");
		
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			nameFieldNameLabel, 
			name);
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			codeRoutineFieldNameLabel, 
			codeRoutineName);
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			descriptionFieldNameLabel, 
			description);
		
		//now check all the parameters
		for (Parameter parameter : parameters) {
			parameter.checkSecurityViolations();
		}
	}
	
	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		String recordType = getRecordType();
		
		//Extract field names		
		String nameFieldNameLabel
			= RIFServiceMessages.getMessage("calculationMethod.name.label");
		String priorFieldNameLabel
			= RIFServiceMessages.getMessage("calculationMethod.prior.label");
		String codeRoutineFieldNameLabel
			= RIFServiceMessages.getMessage("calculationMethod.codeRoutineName.label");
		String descriptionFieldNameLabel
			= RIFServiceMessages.getMessage("calculationMethod.description.label");
	
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		
		if (validationPolicy == ValidationPolicy.STRICT) { 
			if (fieldValidationUtility.isEmpty(name) == true) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordType,
						nameFieldNameLabel);
				errorMessages.add(errorMessage);		
			}

			if (calculationMethodPrior == null) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordType,
						priorFieldNameLabel);
				errorMessages.add(errorMessage);		
			}
		}
		
		if (fieldValidationUtility.isEmpty(codeRoutineName) == true) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					codeRoutineFieldNameLabel);
			errorMessages.add(errorMessage);		
		}

/*		
		if (fieldValidationUtility.isEmpty(description) == true) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					descriptionFieldNameLabel);
			errorMessages.add(errorMessage);		
		}
*/			
		if (parameters == null) {
			String parametersFieldLabel
				= RIFServiceMessages.getMessage("parameter.plural.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					parametersFieldLabel);
			errorMessages.add(errorMessage);
		}
		else if (parameters.isEmpty() == false) {
			for (Parameter parameter : parameters) {
				try {
					parameter.checkErrors(validationPolicy);
				}
				catch(RIFServiceException exception) {
					errorMessages.addAll(exception.getErrorMessages());
				}
			}
			
			String duplicatesFoundMessage
				= Parameter.identifyDuplicateParametersWithinList(parameters);
			if (duplicatesFoundMessage != null) {
				errorMessages.add(duplicatesFoundMessage);
			}
		}
	
		countErrors(
			RIFServiceError.INVALID_CALCULATION_METHOD, 
			errorMessages);
	}

// ==========================================
// Section Interfaces
// ==========================================

// ==========================================
// Section Override
// ==========================================

	@Override
	public String getDisplayName() {
		
		return name;
	}


	@Override
	public String getRecordType() {
		
		String recordNameLabel
			= RIFServiceMessages.getMessage("calculationMethod.label");
		return recordNameLabel;
	}
}
