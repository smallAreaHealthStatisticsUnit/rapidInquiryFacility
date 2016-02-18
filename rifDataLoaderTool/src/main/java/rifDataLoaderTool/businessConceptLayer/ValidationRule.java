package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.FieldValidationUtility;

import java.util.ArrayList;

/**
 * A rule that is used to generate SQL code that can search and replace values. Apart from
 * name and description fields, the three main important fields are:
 * <ul>
 * <li><b>searchValue</b>: which can just be a phrase or some complex regular expression </li>
 * <li><b>replaceValue</b>: the value to be used to replace an existing flawed value</li>
 * <li><b>isRegularExpressionSearch</b>: determines whether the search value should be
 * treated as part of a simple search and replace statement or if it should be phrased as
 * a search that involves regular expressions.  
 * </li> 
 * </ul>
 *
 *<p>
 * This flag helps inform the SQL code generation classes about how to write the rule.  For example,
 * a statement could be: 
 * </p>
 * <p>
 * <pre>
 * CASE
 *    WHEN sex='M' THEN 0
 *    ...
 * END
 * </pre>
 * 
 * <p>
 * or
 *<p>
 *<pre>
 * CASE
 *    WHEN sex ~ '^[mM] THEN 0
 *    ...
 * END
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
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

public final class ValidationRule 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
		
	private String name;
	private String description;
	private String validValue;
	
	private boolean isRegularExpressionSearch;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private ValidationRule() {
		name = "";
		description = "";
		validValue = "";
	}
	
	private ValidationRule(
		final String name,
		final String description,
		final String validValue,
		final boolean isRegularExpressionSearch) {

		this.name = name;
		this.description = description;
		this.validValue = validValue;
		this.isRegularExpressionSearch = isRegularExpressionSearch;
	}

	public static ValidationRule newInstance() {
		ValidationRule validationRule
			= new ValidationRule();		
		return validationRule;
	}
	
	public static ValidationRule newInstance(
		final String name,
		final String description,
		final String validValue,
		final boolean isRegularExpressionSearch) {
		
		ValidationRule validationRule
			= new ValidationRule(
				name,
				description,
				validValue,
				isRegularExpressionSearch);
		return validationRule;
	}

	public static ValidationRule createCopy(
		final ValidationRule originalValidationRule) {
		
		ValidationRule cloneValidationRule
			= new ValidationRule();
		cloneValidationRule.setName(originalValidationRule.getName());
		cloneValidationRule.setDescription(originalValidationRule.getDescription());
		cloneValidationRule.setRegularExpressionSearch(originalValidationRule.isRegularExpressionSearch());
		cloneValidationRule.setValidValue(originalValidationRule.getValidValue());
			
		return cloneValidationRule;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String getName() {
		return name;
	}

	public void setName(
		final String name) {

		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(
		final String description) {

		this.description = description;
	}

	public String getValidValue() {
		return validValue;
	}

	public void setValidValue(
		final String validValue) {

		this.validValue = validValue;
	}

	public boolean isRegularExpressionSearch() {
		return isRegularExpressionSearch;
	}

	public void setRegularExpressionSearch(
		final boolean isRegularExpressionSearch) {

		this.isRegularExpressionSearch = isRegularExpressionSearch;
	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType = getRecordType();

		if (name != null) {
			String nameField
				= RIFDataLoaderToolMessages.getMessage("validationRule.name.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				nameField,
				name);
		}

		if (description != null) {
			String descriptionFieldName
				= RIFDataLoaderToolMessages.getMessage("validationRule.description.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				descriptionFieldName,
				description);
		}
		
		if (validValue != null) {
			String searchValueFieldName
				= RIFDataLoaderToolMessages.getMessage("validationRule.validValue.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				searchValueFieldName,
				validValue);
		}
	}	
	
	public void checkErrors() throws RIFServiceException {
		String recordType
			= getRecordType();
		
		String nameField
			= RIFDataLoaderToolMessages.getMessage("validationRule.name.label");
		String descriptionFieldName
			= RIFDataLoaderToolMessages.getMessage("validationRule.description.label");
		String validFieldName
			= RIFDataLoaderToolMessages.getMessage("validationRule.validValue.label");

		ArrayList<String> errorMessages = new ArrayList<String>();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
			
		if (fieldValidationUtility.isEmpty(validValue)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					validValue);
			errorMessages.add(errorMessage);			
		}
						
		countErrors(
			RIFDataLoaderToolError.INVALID_CLEANING_RULE, 
			errorMessages);		
		
	}
	
	public String getRecordType() {
		String recordType
			= RIFDataLoaderToolMessages.getMessage("validationRule.label");
		return recordType;
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Display Name
	public String getDisplayName() {
		return validValue;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


