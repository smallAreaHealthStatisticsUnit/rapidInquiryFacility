
package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceError;

public final class CalculationMethod extends AbstractRIFConcept {

	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	private Messages SERVICE_MESSAGES = Messages.serviceMessages();
	
	/** The name. */
	private String name;
	
	/** The code routine name. */
	private String codeRoutineName;
	
	/** The prior. */
	private CalculationMethodPrior calculationMethodPrior;
	
	/** The description. */
	private String description;
	
	/** The parameters. */
	private List<Parameter> parameters;
	
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
		parameters = new ArrayList<>();
    }

	/**
	 * New instance.
	 *
	 * @return the calculation method
	 */
	static public CalculationMethod newInstance() {

		return new CalculationMethod();
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

		return new CalculationMethod(
			name,
			codeRoutineName,
			description,
			prior,
			parameters);
	}

	/**
	 * Creates the copy.
	 *
	 * @param originalCalculationMethods the original calculation methods
	 * @return the array list
	 */
	static public List<CalculationMethod> createCopy(
			final List<CalculationMethod> originalCalculationMethods) {

		if (originalCalculationMethods == null) {
			return null;		
		}
		
		List<CalculationMethod> clonedCalculationMethods = new ArrayList<>();
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
		
		List<Parameter> clonedParameterList =
				Parameter.createCopy(originalCalculationMethod.getParameters());
		cloneCalculationMethod.setParameters(clonedParameterList);
		return cloneCalculationMethod;
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
	public void setPrior(final CalculationMethodPrior prior) {
		
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
	public void setCodeRoutineName(final String codeRoutineName) {
		
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
	 * Gets the stats_method (in database form).
	 *
	 * @return the description
	 */
	public String getStatsMethod() {
		String statsMethod = "NONE";

		switch (codeRoutineName) {
			case "het_r_procedure":
				statsMethod = "HET";
				break;
			case "bym_r_procedure":
				statsMethod = "BYM";
				break;
			case "car_r_procedure":
				statsMethod = "CAR";
				break;
		}
		return statsMethod;
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
	public List<Parameter> getParameters() {
		
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
	public void setParameters(final List<Parameter> parameters) {
		
		this.parameters = parameters;
	}

	public void identifyDifferences(final CalculationMethod anotherCalculationMethod,
			final ArrayList<String> differences) {
		
		super.identifyDifferences(anotherCalculationMethod, differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param calculationMethodsA the calculation methods a
	 * @param calculationMethodsB the calculation methods b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
			final List<CalculationMethod> calculationMethodsA,
			final List<CalculationMethod> calculationMethodsB) {

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
		List<CalculationMethod> methodsA = sortMethodsByCodeRoutineName(calculationMethodsA);
		List<CalculationMethod> methodsB = sortMethodsByCodeRoutineName(calculationMethodsB);
		
		int numberOfCalculationMethods = methodsA.size();
		for (int i = 0; i < numberOfCalculationMethods; i++) {
			CalculationMethod calculationMethodA
				= methodsA.get(i);
			
			CalculationMethod calculationMethodB
				= methodsB.get(i);
			if (!calculationMethodA.hasIdenticalContents(calculationMethodB)) {
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
	private static List<CalculationMethod> sortMethodsByCodeRoutineName(
		final List<CalculationMethod> calculationMethods) {
	
		HashMap<String, CalculationMethod> methodFromCodeRoutineName = new HashMap<>();
		for (CalculationMethod calculationMethod : calculationMethods) {
			methodFromCodeRoutineName.put(
				calculationMethod.getCodeRoutineName(),
				calculationMethod);
		}

		List<String> keys = new ArrayList<>(methodFromCodeRoutineName.keySet());
		
		//@TODO: how well doe this handle duplicates
		List<CalculationMethod> results = new ArrayList<>();
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
			if (!collator.equals(name, otherName)) {
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
			if (!collator.equals(codeRoutineName, otherCodeRoutine)) {
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
			if (!collator.equals(description, otherDescription)) {
				return false;
			}		
		}

		List<Parameter> otherParameters = otherCalculationMethod.getParameters();
		if (!Parameter.hasIdenticalContents(parameters, otherParameters)) {
			return false;
		}

		return super.hasIdenticalContents(otherCalculationMethod);
	}
	public void checkSecurityViolations() throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();	
		
		String recordType = getRecordType();
		
		String nameFieldNameLabel = SERVICE_MESSAGES.getMessage("calculationMethod.name.label");
		String codeRoutineFieldNameLabel= SERVICE_MESSAGES.getMessage(
				"calculationMethod.codeRoutineName.label");
		String descriptionFieldNameLabel = SERVICE_MESSAGES.getMessage(
				"calculationMethod.description.label");
		
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();
		fieldValidationUtility.checkMaliciousCode(recordType, nameFieldNameLabel, name);
		fieldValidationUtility.checkMaliciousCode(recordType, codeRoutineFieldNameLabel,
		                                          codeRoutineName);
		fieldValidationUtility.checkMaliciousCode(recordType, descriptionFieldNameLabel,
		                                          description);
		
		//now check all the parameters
		for (Parameter parameter : parameters) {
			parameter.checkSecurityViolations();
		}
	}
	
	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<>();
		String recordType = getRecordType();
		
		//Extract field names		
		String nameFieldNameLabel = SERVICE_MESSAGES.getMessage("calculationMethod.name.label");
		String priorFieldNameLabel = SERVICE_MESSAGES.getMessage(
				"calculationMethod.prior.label");
		String codeRoutineFieldNameLabel = SERVICE_MESSAGES.getMessage(
				"calculationMethod.codeRoutineName.label");
		String descriptionFieldNameLabel = SERVICE_MESSAGES.getMessage(
				"calculationMethod.description.label");
	
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();
		
		if (validationPolicy == ValidationPolicy.STRICT) { 
			if (fieldValidationUtility.isEmpty(name)) {
				String errorMessage = GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordType,
						nameFieldNameLabel);
				errorMessages.add(errorMessage);		
			}

			if (calculationMethodPrior == null) {
				String errorMessage = GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordType,
						priorFieldNameLabel);
				errorMessages.add(errorMessage);		
			}
		}
		
		if (fieldValidationUtility.isEmpty(codeRoutineName)) {
			String errorMessage = GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					codeRoutineFieldNameLabel);
			errorMessages.add(errorMessage);		
		}

		if (fieldValidationUtility.isEmpty(description)) {
			String errorMessage = GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					descriptionFieldNameLabel);
			errorMessages.add(errorMessage);		
		}
		
		if (parameters == null) {
			String parametersFieldLabel = SERVICE_MESSAGES.getMessage("parameter.plural.label");
			String errorMessage = GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					parametersFieldLabel);
			errorMessages.add(errorMessage);
		}
		else if (!parameters.isEmpty()) {
			for (Parameter parameter : parameters) {
				try {
					parameter.checkErrors();
				}
				catch(RIFServiceException exception) {
					errorMessages.addAll(exception.getErrorMessages());
				}
			}
			
			String duplicatesFoundMessage =
					Parameter.identifyDuplicateParametersWithinList(parameters);
			if (duplicatesFoundMessage != null) {
				errorMessages.add(duplicatesFoundMessage);
			}
		}
	
		countErrors(RIFServiceError.INVALID_CALCULATION_METHOD, errorMessages);
	}

	@Override
	public String getDisplayName() {
		
		return name;
	}

	@Override
	public String getRecordType() {

		return SERVICE_MESSAGES.getMessage("calculationMethod.label");
	}
}
