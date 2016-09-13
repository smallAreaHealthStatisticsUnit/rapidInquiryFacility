package rifGenericLibrary.util;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFGenericLibraryError;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.*;
import java.util.HashMap;


/**
 * Provides commmon validation methods used by most of the business classes.
 * Also contains examples of regular expressions for detecting a variety of 
 * malicious attacks such as SQL injection errors and cross scripting errors.
 * The regular expressions have been taken from the following link:
 * http://www.symantec.com/connect/articles/detection-sql-injection-and-cross-site-scripting-attacks
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

public final class FieldValidationUtility {
	
	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	/** The Constant testCaseMaliciousFieldValue. */
	private static final String testCaseMaliciousFieldValue
		= "test_case_malicious_code_pattern";
	
	/** The malicious code pattern from string. */
	private HashMap<String, Pattern> maliciousCodePatternFromString;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new field validation utility.
	 */
	public FieldValidationUtility() {
		
		maliciousCodePatternFromString = new HashMap<String, Pattern>();
		Pattern testPattern = Pattern.compile(testCaseMaliciousFieldValue);
		maliciousCodePatternFromString.put(testCaseMaliciousFieldValue, testPattern);
		initialisePatterns();
	}
	
	//@TODO read from a properties file
	/**
	 * Initialise patterns.
	 */
	private void initialisePatterns() {
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	/**
	 * Sets the malicious code patterns.
	 *
	 * @param maliciousCodePatternPhrases the new malicious code patterns
	 */
	public void setMaliciousCodePatterns(
		final ArrayList<String> maliciousCodePatternPhrases) {
		
		for (String maliciousCodePatternPhrase : maliciousCodePatternPhrases) {
			if (maliciousCodePatternFromString.get(maliciousCodePatternPhrase) == null) {			
				Pattern pattern
					= Pattern.compile(maliciousCodePatternPhrase);
				maliciousCodePatternFromString.put(maliciousCodePatternPhrase, pattern);			
			}
		}
	}
		
	/**
	 * Gets the test malicious field value.
	 *
	 * @return the test malicious field value
	 */
	public String getTestMaliciousFieldValue() {
		
		return testCaseMaliciousFieldValue;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	/**
	 * Check null method parameter.
	 *
	 * @param methodName the method name
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkNullMethodParameter(
		final String methodName,
		final String parameterName,
		final Object parameterValue) 
		throws RIFServiceException {
		
		if (parameterValue == null) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.nullMethodParameter",
					methodName,
					parameterName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER,
					errorMessage);
			throw rifServiceException;		
		}
		else {
			if (parameterValue instanceof String) {
				String parameterValuePhrase
					= (String) parameterValue;
				if (isEmpty(parameterValuePhrase) == true) {
					String errorMessage
						= RIFGenericLibraryMessages.getMessage(
							"general.validation.nullMethodParameter",
							methodName,
							parameterName);
					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER,
							errorMessage);
					throw rifServiceException;		
				}
			}
			
			
			
			
		}
	}
	
	
	
	/**
	 * Check malicious method parameter.
	 *
	 * @param methodName the method name
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 * @throws RIFServiceSecurityException the RIF service security exception
	 */
	public void checkMaliciousMethodParameter(
		final String methodName,
		final String parameterName,
		final String parameterValue) 
		throws RIFServiceSecurityException {

		//TOUR_SECURITY
		/*
		 * Some service API methods have parameters which are just String objects.  In this case,
		 * we want the utility to scan that value and indicate an appropriate error message.
		 * Notice how it iterates through a list of regular expression patterns and attempts
		 * to find a match in the target field value.
		 */
		if (parameterValue == null) {
			return;
		}
		
		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<Pattern>();
		maliciousCodePatterns.addAll(maliciousCodePatternFromString.values());
		for (Pattern maliciousCodePattern : maliciousCodePatterns) {
			Matcher matcher = maliciousCodePattern.matcher(parameterValue);
			if (matcher.find()) {
				String errorMessage
					= RIFGenericLibraryMessages.getMessage(
						"genaral.validation.maliciousMethodParameterDetected",
						methodName,
						parameterName,
						parameterValue);
				RIFServiceSecurityException rifServiceSecurityException
					= new RIFServiceSecurityException(errorMessage);
				throw rifServiceSecurityException;
			}
		}
	}
	
	
	/**
	 * Check malicious method parameter.
	 *
	 * @param methodName the method name
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 * @throws RIFServiceSecurityException the RIF service security exception
	 */
	public void checkMaliciousMethodParameter(
		final String methodName,
		final String parameterName,
		final String[] parameterValues) 
		throws RIFServiceSecurityException {

		if (parameterValues == null) {
			return;
		}
		
		if (parameterValues.length == 0) {
			return;
		}
		
		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<Pattern>();
		maliciousCodePatterns.addAll(maliciousCodePatternFromString.values());
		
		for (String parameterValue : parameterValues) {		
			for (Pattern maliciousCodePattern : maliciousCodePatterns) {
				Matcher matcher = maliciousCodePattern.matcher(parameterValue);
				if (matcher.find()) {
					String errorMessage
						= RIFGenericLibraryMessages.getMessage(
							"genaral.validation.maliciousMethodParameterDetected",
							methodName,
							parameterName,
							parameterValue);
					RIFServiceSecurityException rifServiceSecurityException
						= new RIFServiceSecurityException(errorMessage);
					throw rifServiceSecurityException;
				}
			}		
		}
	}
	
	/**
	 * Check malicious method parameter.
	 *
	 * @param methodName the method name
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 * @throws RIFServiceSecurityException the RIF service security exception
	 */
	public void checkMaliciousPasswordValue(
		final String methodName,
		final String parameterName,
		final char[] password) 
		throws RIFServiceSecurityException {

		if (password == null) {
			return;
		}
		
		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<Pattern>();
		maliciousCodePatterns.addAll(maliciousCodePatternFromString.values());
		for (Pattern maliciousCodePattern : maliciousCodePatterns) {
			Matcher matcher = maliciousCodePattern.matcher(new String(password));
			if (matcher.find()) {
				String errorMessage
					= RIFGenericLibraryMessages.getMessage(
						"genaral.validation.maliciousMethodParameterDetected",
						methodName,
						parameterName,
						new String(password));
				RIFServiceSecurityException rifServiceSecurityException
					= new RIFServiceSecurityException(errorMessage);
				throw rifServiceSecurityException;
			}
		}
	}

	
	/**
	 * Returns true if the field value is either null or empty string.
	 *
	 * @param fieldName the field name
	 * @param fieldValue the field value
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkEmptyRequiredParameterValue(
		final String fieldName, 
		final String fieldValue) 
		throws RIFServiceException {
		
		if (isEmpty(fieldValue)) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredParameter", 
					fieldName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER,
					errorMessage);
			throw rifServiceException;			
		}		
	}
	

	public void checkValidIntegerMethodParameterValue(
		final String methodName, 
		final String fieldName,
		final String fieldValue) 
		throws RIFServiceException {
			
		try {
			Integer.valueOf(fieldValue);
			
		}
		catch(NumberFormatException numberformatException) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.invalidIntegerMethodParameter", 
					methodName,
					fieldValue,
					fieldName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.INVALID_INTEGER_API_METHOD_PARAMETER,
					errorMessage);
			throw rifServiceException;
		}
	}	

		
	/**
	 * Checks if is empty.
	 *
	 * @param fieldValue the field value
	 * @return true, if is empty
	 */
	public boolean isEmpty(
		final String fieldValue) {		

		if (fieldValue == null) {
			return true;
		}
		
		Collator collator = RIFGenericLibraryMessages.getCollator();
		if (collator.equals(fieldValue, "")) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Assumes that the spaces have all been replaced by 
	 * @param candidateDatabaseTableName
	 * @return
	 */
	public boolean isValidDatabaseTableName(String candidateDatabaseTableName) {
		Pattern pattern = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");
		Matcher matcher = pattern.matcher(candidateDatabaseTableName);
		return matcher.matches();
	}

	/**
	 * replaces all spaces with underscores and then converts all letters to 
	 * upper case.
	 * @param candidateTableName
	 * @return
	 */
	public String convertToDatabaseTableName(final String candidateTableName) {
		return candidateTableName.replace(" ", "_").toUpperCase();
	}
	
	
	
	/**
	 * Check malicious code.
	 *
	 * @param recordName the record name
	 * @param fieldName the field name
	 * @param fieldValue the field value
	 * @throws RIFServiceSecurityException the RIF service security exception
	 */
	public void checkMaliciousCode(
		final String recordName,
		final String fieldName,
		final String fieldValue) 
		throws RIFServiceSecurityException {

		//TOUR_SECURITY
		/*
		 * This is the main method that is used by RIF business classes to test whether 
		 * one of their String field values contains malicious codes.  This method works by
		 * iterating through a collection of patterns that would suggest malicious code, then
		 * checking whether the field value has a match for any of them.
		 */
		
		if (fieldValue == null) {
			return;
		}
		
		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<Pattern>();
		maliciousCodePatterns.addAll(maliciousCodePatternFromString.values());
		for (Pattern maliciousCodePattern : maliciousCodePatterns) {
			Matcher matcher = maliciousCodePattern.matcher(fieldValue);
			if (matcher.find()) {
				String errorMessage
					= RIFGenericLibraryMessages.getMessage(
						"genaral.validation.maliciousRecordFieldDetected",
						recordName,
						fieldName,
						fieldValue);
				RIFServiceSecurityException rifServiceSecurityException
					= new RIFServiceSecurityException(errorMessage);
				throw rifServiceSecurityException;
			}
		}				
	}	

	/**
	 * A convenience method to do the simplest of checks: whether
	 * one item is null and the other isn't.
	 *
	 * @param fieldValueA the field value a
	 * @param fieldValueB the field value b
	 * @return true, if successful
	 */
	public static boolean hasDifferentNullity(
		final Object fieldValueA, 
		final Object fieldValueB) {
	
		if ( ((fieldValueA == null) && (fieldValueB != null)) ||
		     ((fieldValueA != null) && (fieldValueB == null))) {		
			//reject if one is null and the other is not
			return true;
		}
		
		return false;		
	}
	
	public static String compareNullities(
		final Object fieldValueA,
		final String recordNameA,
		final String fieldNameA,		
		final Object fieldValueB,
		final String recordNameB,
		final String fieldNameB) {
			
		if (hasDifferentNullity(fieldValueA, fieldValueB) == false) {
			//fieldValueA and fieldValueB are either both null or both non-null
			return null;
		}
		
		String differenceMessage = "";
		if (fieldValueA == null) {
			differenceMessage
				= RIFGenericLibraryMessages.getMessage(
					"fieldValidationUtility.nullityDifferences.fieldLevelDifference",
					recordNameA,
					fieldNameA,
					recordNameB,
					fieldNameB);
		}
		else {
			differenceMessage
				= RIFGenericLibraryMessages.getMessage(
					"fieldValidationUtility.nullityDifferences.fieldLevelDifference",
					recordNameB,
					fieldNameB,
					recordNameA,
					fieldNameA);
		}		
		
		return differenceMessage;			
	}
	
	public static boolean areFieldsEqual(
		final String valueA, 
		final String valueB) {
	
		Collator collator 
			= RIFGenericLibraryMessages.getCollator();
		if ((valueA == null) && (valueB != null)) {
			return false;
		}
		else if ((valueA != null) && (valueB == null)) {
			return false;
		}
		else if (valueA != valueB) {			
			return collator.equals(valueA, valueB);
		}
		else {
			return true;
		}
	}

	public static String generateUniqueListItemName(
		final String baseName,
		final ArrayList<String> existingNames) {
		
		//Check - if base name is already unique then return that
		if (existingNames.contains(baseName) == false) {
			return baseName;
		}
		
		int counter = 1;
		for (;;) {
			counter++;
			String candidateName
				= baseName + "-" + String.valueOf(counter);
			if (existingNames.contains(candidateName) == false) {
				//name is unique
				return candidateName;
			}
		}	
	}
	
	public static void checkListDuplicate(
		final String candidateItem,
		final ArrayList<String> currentListItems) 
		throws RIFServiceException {
				
		if (currentListItems.contains(candidateItem)) {
			//Error Message
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"listItemNameUtility.duplicateFieldName",
					candidateItem);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DUPLICATE_LIST_ITEM_NAME,
					errorMessage);
			throw rifServiceException;
		}	
	}
	
	public boolean hasIdenticalContents(
		final boolean isOrderImportant,
		final ArrayList<String> firstList,
		final ArrayList<String> secondList) {
		
		if (firstList == secondList) {
			return true;
		}
		
		if ((firstList == null) && (secondList != null) ||
			(firstList != null) && (secondList == null)) {
			
			return false;
		}
		
		int numberOfFirstListItems = firstList.size();
		int numberOfSecondListItems = secondList.size();
		
		if (numberOfFirstListItems != numberOfSecondListItems) {
			return false;
		}
					
		if (isOrderImportant) {
			for (int i = 0; i < numberOfFirstListItems; i++) {
				String currentFirstListItem = firstList.get(i);
				String currentSecondListItem = secondList.get(i);
				if (Objects.deepEquals(
					currentFirstListItem, 
					currentSecondListItem) == false) {
					
					return false;
				}			
			}
		}
		else {
			ArrayList<String> listA = new ArrayList<String>();
			listA.addAll(firstList);
			Collections.sort(listA);
			ArrayList<String> listB = new ArrayList<String>();
			listB.addAll(secondList);
			Collections.sort(listB);
			
			for (String firstListItem : firstList) {
				if (secondList.contains(firstListItem) == false) {
					return false;
				}
			}

			for (String secondListItem : secondList) {
				if (firstList.contains(secondListItem) == false) {
					return false;
				}
			}
		}

		return true;		
	}
	
	/**
	 * Count errors.
	 *
	 * @param rifServiceError the rif service error
	 * @param errorMessages the error messages
	 * @throws RIFServiceException the RIF service exception
	 */
	public void countErrors(
		final Object rifErrorEnumeration,
		final ArrayList<String> errorMessages) 
		throws RIFServiceException {

		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(rifErrorEnumeration, errorMessages);
			throw rifServiceException;
		}		
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
