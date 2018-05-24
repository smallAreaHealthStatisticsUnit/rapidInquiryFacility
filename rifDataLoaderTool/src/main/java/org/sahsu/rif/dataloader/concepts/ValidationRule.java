package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

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
 * Copyright 2017 Imperial College London, developed by the Small Area
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
	extends AbstractDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
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
		setIdentifier("");
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
		setIdentifier("");
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
		copyInto(
			originalValidationRule, 
			cloneValidationRule);
			
		return cloneValidationRule;
	}
	
	public static void copyInto(
		final ValidationRule sourceValidationRule,
		final ValidationRule destinationValidationRule) {
		
		destinationValidationRule.setName(sourceValidationRule.getName());
		destinationValidationRule.setDescription(sourceValidationRule.getDescription());
		destinationValidationRule.setRegularExpressionSearch(sourceValidationRule.isRegularExpressionSearch());
		destinationValidationRule.setValidValue(sourceValidationRule.getValidValue());
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public boolean hasIdenticalContents(
		final ValidationRule otherValidationRule) {
		
		String otherName = otherValidationRule.getName();
		String otherDescription = otherValidationRule.getDescription();
		String otherValidValue = otherValidationRule.getValidValue();
		if (Objects.deepEquals(name, otherName) == false) {
			System.out.println("ValidationRule hic 1");
			return false;
		}
		if (Objects.deepEquals(description, otherDescription) == false) {
			System.out.println("ValidationRule hic 2 desc=="+description+"==otherDesc=="+otherDescription+"==");
			return false;
		}

		if (Objects.deepEquals(validValue, otherValidValue) == false) {
			System.out.println("ValidationRule hic 3 validValue=="+validValue+"==otherValidValue=="+otherValidValue+"==");
			return false;
		}

		if (lastModifiedDatesIdentical(otherValidationRule) == false) {
			System.out.println("ValidationRule hic 4 validValue==");
			return false;
		}
		
		return true;		
	}
	
	
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
	
	static public boolean validationRulesAreEqual(
		final ArrayList<ValidationRule> listAValidationRules,
		final ArrayList<ValidationRule> listBValidationRules) {
		
		if (listAValidationRules.size() != listBValidationRules.size()) {
			return false;
		}
		
		HashMap<String, ValidationRule> ruleFromIdentifierA
			= new HashMap<String, ValidationRule>();
		for (ValidationRule listAValidationRule : listAValidationRules) {
			ruleFromIdentifierA.put(
				listAValidationRule.getIdentifier(), 
				listAValidationRule);
		}
			
		HashMap<String, ValidationRule> ruleFromIdentifierB
			= new HashMap<String, ValidationRule>();
		for (ValidationRule listBValidationRule : listBValidationRules) {
			ruleFromIdentifierB.put(
				listBValidationRule.getIdentifier(), 
				listBValidationRule);
		}

		ArrayList<String> listAKeys = new ArrayList<String>();
		listAKeys.addAll(ruleFromIdentifierA.keySet());
		for (String listAKey : listAKeys) {
			ValidationRule ruleFromListA
				= ruleFromIdentifierA.get(listAKey);
			ValidationRule ruleFromListB
				= ruleFromIdentifierB.get(listAKey);
			if (ruleFromListB == null) {
				return false;
			}
			else {
				if (ruleFromListA.hasIdenticalContents(ruleFromListB) == false) {
					return false;
				}
			}
		}

		ArrayList<String> listBKeys = new ArrayList<String>();
		listBKeys.addAll(ruleFromIdentifierB.keySet());	
		for (String listBKey : listBKeys) {
			ValidationRule ruleFromListB
				= ruleFromIdentifierB.get(listBKey);
			ValidationRule ruleFromListA
				= ruleFromIdentifierA.get(listBKey);
			if (ruleFromListA == null) {
				return false;
			}
			else {
				if (ruleFromListB.hasIdenticalContents(ruleFromListA) == false) {
					return false;
				}
			}
		}

		return true;		
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
		
		String validFieldName
			= RIFDataLoaderToolMessages.getMessage("validationRule.validValue.label");

		ArrayList<String> errorMessages = new ArrayList<String>();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
			
		if (fieldValidationUtility.isEmpty(validValue)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					validFieldName);
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
		return name;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


