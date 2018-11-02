package org.sahsu.rif.generic.util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.system.RifError;

/**
 * Provides common validation methods used by most of the business classes.
 * Also contains examples of regular expressions for detecting a variety of 
 * malicious attacks such as SQL injection errors and cross scripting errors.
 * The regular expressions have been taken from the following link:
 * http://www.symantec.com/connect/articles/detection-sql-injection-and-cross-site-scripting-attacks
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * Kevin Garwood
 * @author kgarwood
 */

public final class FieldValidationUtility {
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();

	/** The Constant testCaseMaliciousFieldValue. */
	private static final String testCaseMaliciousFieldValue
		= "test_case_malicious_code_pattern";
	
	/** The malicious code pattern from string. */
	private HashMap<String, Pattern> maliciousCodePatternFromString;
	
	/**
	 * Instantiates a new field validation utility.
	 */
	public FieldValidationUtility() {
		
		maliciousCodePatternFromString = new HashMap<>();
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

	/**
	 * Gets the test malicious field value.
	 *
	 * @return the test malicious field value
	 */
	public String getTestMaliciousFieldValue() {
		
		return testCaseMaliciousFieldValue;
	}
	
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
				= GENERIC_MESSAGES.getMessage(
					"general.validation.nullMethodParameter",
					methodName,
					parameterName);
			throw new RIFServiceException(
					RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER,
					errorMessage);
		}
		else {
			if (parameterValue instanceof String) {
				String parameterValuePhrase
					= (String) parameterValue;
				if (isEmpty(parameterValuePhrase)) {
					String errorMessage
						= GENERIC_MESSAGES.getMessage(
							"general.validation.nullMethodParameter",
							methodName,
							parameterName);
					throw new RIFServiceException(
						RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER,
						errorMessage);
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

		/*
		 * Some service API methods have parameters which are just String objects.  In this case,
		 * we want the utility to scan that value and indicate an appropriate error message.
		 * Notice how it iterates through a list of regular expression patterns and attempts
		 * to find a match in the target field value.
		 */
		if (parameterValue == null) {
			return;
		}

		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<>(
				maliciousCodePatternFromString.values());
		for (Pattern maliciousCodePattern : maliciousCodePatterns) {
			Matcher matcher = maliciousCodePattern.matcher(parameterValue);
			if (matcher.find()) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.maliciousMethodParameterDetected",
						methodName,
						parameterName,
						parameterValue);
				throw new RIFServiceSecurityException(errorMessage);
			}
		}
	}
	
	
	/**
	 * Check malicious method parameter.
	 *
	 * @param methodName the method name
	 * @param parameterName the parameter name
	 * @param parameterValues the parameter value
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

		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<>(
				maliciousCodePatternFromString.values());
		
		for (String parameterValue : parameterValues) {		
			for (Pattern maliciousCodePattern : maliciousCodePatterns) {
				Matcher matcher = maliciousCodePattern.matcher(parameterValue);
				if (matcher.find()) {
					String errorMessage
						= GENERIC_MESSAGES.getMessage(
							"general.validation.maliciousMethodParameterDetected",
							methodName,
							parameterName,
							parameterValue);
					throw new RIFServiceSecurityException(errorMessage);
				}
			}		
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
				= GENERIC_MESSAGES.getMessage(
					"general.validation.invalidIntegerMethodParameter", 
					methodName,
					fieldValue,
					fieldName);
			throw new RIFServiceException(
				RIFGenericLibraryError.INVALID_INTEGER_API_METHOD_PARAMETER,
				errorMessage);
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
		
		Collator collator = GENERIC_MESSAGES.getCollator();
		return collator.equals(fieldValue, "");
	}
	
	public boolean isEmpty(Collection<?> collection) {
		
		return collection.isEmpty();
	}

	/**
	 * replaces all spaces with underscores and then converts all letters to 
	 * upper case.
	 * @param candidateTableName the table name to convert
	 * @return the converted name
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

		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<>(
				maliciousCodePatternFromString.values());
		for (Pattern maliciousCodePattern : maliciousCodePatterns) {
			Matcher matcher = maliciousCodePattern.matcher(fieldValue);
			if (matcher.find()) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.maliciousRecordFieldDetected",
						recordName,
						fieldName,
						fieldValue);
				throw new RIFServiceSecurityException(errorMessage);
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

		return ((fieldValueA == null) && (fieldValueB != null)) ||
		       ((fieldValueA != null) && (fieldValueB == null));

	}
	
	public static String generateUniqueListItemName(
		final String baseName,
		final ArrayList<String> existingNames) {
		
		//Check - if base name is already unique then return that
		if (!existingNames.contains(baseName)) {
			return baseName;
		}
		
		int counter = 1;
		for (;;) {
			counter++;
			String candidateName
				= baseName + "-" + String.valueOf(counter);
			if (!existingNames.contains(candidateName)) {
				//name is unique
				return candidateName;
			}
		}	
	}
	
	public void checkListDuplicate(
		final String candidateItem,
		final ArrayList<String> currentListItems) 
		throws RIFServiceException {
				
		if (currentListItems.contains(candidateItem)) {
			//Error Message
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"listItemNameUtility.duplicateFieldName",
					candidateItem);
			throw new RIFServiceException(
				RIFGenericLibraryError.DUPLICATE_LIST_ITEM_NAME,
				errorMessage);
		}	
	}
	
	public boolean hasIdenticalContents(
		final boolean isOrderImportant,
		final ArrayList<String> firstList,
		final ArrayList<String> secondList) {
		
		if (firstList == secondList) {
			return true;
		}
		
		if (firstList == null || secondList == null) {
			
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
				if (!Objects.deepEquals(
						currentFirstListItem,
						currentSecondListItem)) {
					
					return false;
				}			
			}
		} else {
			Collections.sort(firstList);
			Collections.sort(secondList);
			
			for (String firstListItem : firstList) {
				if (!secondList.contains(firstListItem)) {
					return false;
				}
			}

			for (String secondListItem : secondList) {
				if (!firstList.contains(secondListItem)) {
					return false;
				}
			}
		}

		return true;		
	}
	
	/**
	 * Count errors.
	 *
	 * @param rifErrorEnumeration the rif service errors
	 * @param errorMessages the error messages
	 * @throws RIFServiceException the RIF service exception
	 */
	public void throwExceptionIfErrorsFound(final RifError rifErrorEnumeration,
			final ArrayList<String> errorMessages) throws RIFServiceException {

		if (errorMessages.size() > 0) {

			throw new RIFServiceException(rifErrorEnumeration, errorMessages);
		}		
	}
}
