package rifServices.util;

import rifServices.system.RIFServiceException;

import rifServices.system.RIFServiceSecurityException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;

import java.text.Collator;
import java.util.ArrayList;
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
				= RIFServiceMessages.getMessage(
					"general.validation.nullMethodParameter",
					methodName,
					parameterName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.NULL_API_METHOD_PARAMETER,
					errorMessage);
			throw rifServiceException;		
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

		if (parameterValue == null) {
			return;
		}
		
		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<Pattern>();
		maliciousCodePatterns.addAll(maliciousCodePatternFromString.values());
		for (Pattern maliciousCodePattern : maliciousCodePatterns) {
			Matcher matcher = maliciousCodePattern.matcher(parameterValue);
			if (matcher.find()) {
				String errorMessage
					= RIFServiceMessages.getMessage(
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
	 * Returns true if the field value is either null or empty string.
	 *
	 * @param fieldName the field name
	 * @param fieldValue the field value
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkEmptyRequiredFieldValue(
		final String fieldName, 
		final String fieldValue) 
		throws RIFServiceException {
		
		if (isEmpty(fieldValue)) {
			String errorMessage
				= RIFServiceMessages.getMessage("", fieldName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.EMPTY_REQUIRED_FIELD,
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
		
		Collator collator = RIFServiceMessages.getCollator();
		if (collator.equals(fieldValue, "")) {
			return true;
		}
		
		return false;
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
				
		if (fieldValue == null) {
			return;
		}
		
		ArrayList<Pattern> maliciousCodePatterns = new ArrayList<Pattern>();
		maliciousCodePatterns.addAll(maliciousCodePatternFromString.values());
		for (Pattern maliciousCodePattern : maliciousCodePatterns) {
			Matcher matcher = maliciousCodePattern.matcher(fieldValue);
			if (matcher.find()) {
				String errorMessage
					= RIFServiceMessages.getMessage(
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
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
