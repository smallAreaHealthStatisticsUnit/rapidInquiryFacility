package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 * A convenience class that centralises the creation of data type objects.
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public class RIFDataTypeFactory {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
		
	private HashMap<String, RIFDataType> dataTypeFromCodes;
	private HashMap<String, RIFDataType> dataTypeFromNames;
	
	public static final RIFDataType RIF_AGE_DATA_TYPE = createAgeRIFDataType();
	public static final RIFDataType RIF_SEX_DATA_TYPE = createSexDataType();	
	public static final RIFDataType RIF_YEAR_DATA_TYPE = createYearDataType();
	
	public static final RIFDataType RIF_INTEGER_DATA_TYPE = createIntegerDataType();
	public static final RIFDataType RIF_DOUBLE_DATA_TYPE = createDoubleRIFDataType();
	public static final RIFDataType RIF_TEXT_DATA_TYPE = createTextDataType();
	public static final RIFDataType RIF_DATE_DATA_TYPE = createDateDataType();
	public static final RIFDataType RIF_ICD_DATA_TYPE = createICDDataType();
	public static final RIFDataType RIF_ASCII_DATA_TYPE = createASCIIDataType();
	public static final RIFDataType RIF_QUINTILISE_DATA_TYPE = createQuintiliseDataType();	
	public static final RIFDataType RIF_UK_POSTAL_CODE_DATA_TYPE = createUKPostalCodeDataType();
	
	// ==========================================
	// Section Construction
	// ==========================================

	private RIFDataTypeFactory() {
		dataTypeFromCodes = new HashMap<String, RIFDataType>();
		dataTypeFromNames = new HashMap<String, RIFDataType>();	
	}


	
	public void printRegisteredDataTypes() {
		ArrayList<RIFDataType> rifDataTypes = new ArrayList<RIFDataType>();
		rifDataTypes.addAll(dataTypeFromCodes.values());
		for (RIFDataType rifDataType : rifDataTypes) {
			rifDataType.printFields();
		}	
	}
	
	public void populateFactoryWithBuiltInTypes() {

		clearDataTypes();
		registerReservedDataType(RIF_AGE_DATA_TYPE);
		registerReservedDataType(RIF_SEX_DATA_TYPE);
		registerReservedDataType(RIF_DOUBLE_DATA_TYPE);
		registerReservedDataType(RIF_ASCII_DATA_TYPE);
		registerReservedDataType(RIF_ICD_DATA_TYPE);
		registerReservedDataType(RIF_TEXT_DATA_TYPE);
		registerReservedDataType(RIF_INTEGER_DATA_TYPE);	
		registerReservedDataType(RIF_YEAR_DATA_TYPE);
		registerReservedDataType(RIF_DATE_DATA_TYPE);		
		registerReservedDataType(RIF_QUINTILISE_DATA_TYPE);
		registerReservedDataType(RIF_UK_POSTAL_CODE_DATA_TYPE);
	}
	
	public static RIFDataTypeFactory newInstance() {
		RIFDataTypeFactory rifDataTypeFactory = new RIFDataTypeFactory();
		return rifDataTypeFactory;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public static boolean isReservedIdentifier(final RIFDataType rifDataType) {
		return true;
		
		
		
	}

	
	public RIFDataType createCustomRIFDataType() {
	
		RIFDataType customRIFDataType
			= RIFDataType.newInstance();
		customRIFDataType.setIdentifier(generateCustomDataTypeIdentifier());			

		return customRIFDataType;
	}	
	
	
	public RIFDataType createCustomRIFDataType(
		final String identifier,
		final String name,
		final String description) {
	
		RIFDataType customRIFDataType
			= RIFDataType.newInstance(
				identifier, 
				name, 
				description,
				false);		
		customRIFDataType.setIdentifier(generateCustomDataTypeIdentifier());			
	
		
		return customRIFDataType;
	}
	
	public static RIFDataType createAgeRIFDataType() {
		String identifier = "rif_age";
		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.age.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.age.description");
		RIFDataType ageRIFDataType
			= RIFDataType.newInstance(
				identifier, 
				name, 
				description,
				true);		
		
		/*
		String regularExpression
			= "^[0-9]{1,2}$|1[0-1][0-9]$";	
		String validationRuleName1
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataType.age.vaidationRule1.name");
		String validationRuleDescription1
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataType.age.vaidationRule1.description");
	
		ValidationRule validationRule 
			= ValidationRule.newInstance(
				validationRuleName1, 
				validationRuleDescription1, 
				regularExpression, 
				true);
	
		ageRIFDataType.setFieldValidationPolicy(RIFFieldActionPolicy.USE_RULES);
		ageRIFDataType.addValidationRule(validationRule);
		*/
		
		ageRIFDataType.setFieldValidationPolicy(FieldActionPolicy.USE_FUNCTION);
		ageRIFDataType.setValidationFunctionName("is_valid_age");

		ageRIFDataType.setFieldCleaningPolicy(FieldActionPolicy.USE_FUNCTION);
		ageRIFDataType.setCleaningFunctionName("clean_age");
		
		return ageRIFDataType;
	}
	
	public static RIFDataType createAgeRIFDataTypeMS() {
		//MS specific version of the function, make sure this is called correctly for MS data access
		String identifier = "rif_age";
		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.age.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.age.description");
		RIFDataType ageRIFDataType
			= RIFDataType.newInstance(
				identifier, 
				name, 
				description,
				true);		
		
		/*
		String regularExpression
			= "^[0-9]{1,2}$|1[0-1][0-9]$";	
		String validationRuleName1
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataType.age.vaidationRule1.name");
		String validationRuleDescription1
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataType.age.vaidationRule1.description");
	
		ValidationRule validationRule 
			= ValidationRule.newInstance(
				validationRuleName1, 
				validationRuleDescription1, 
				regularExpression, 
				true);
	
		ageRIFDataType.setFieldValidationPolicy(RIFFieldActionPolicy.USE_RULES);
		ageRIFDataType.addValidationRule(validationRule);
		*/
		
		ageRIFDataType.setFieldValidationPolicy(FieldActionPolicy.USE_FUNCTION);
		ageRIFDataType.setValidationFunctionName("is_valid_age");

		ageRIFDataType.setFieldCleaningPolicy(FieldActionPolicy.USE_FUNCTION);
		ageRIFDataType.setCleaningFunctionName("clean_age");
		
		return ageRIFDataType;
	}
	private static RIFDataType createDoubleRIFDataType() {
		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.double.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.double.description");
		
		RIFDataType doubleRIFDataType
			= RIFDataType.newInstance(
				"rif_double", 
				name, 
				description,
				true);

		/*
		String validationRegularExpression
			= "?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
		//= "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
		
		ValidationRule validationRule
			= ValidationRule.newInstance(
				"", 
				"", 
				validationRegularExpression, 
				true);
		doubleRIFDataType.addValidationRule(validationRule);
		doubleRIFDataType.setFieldValidationPolicy(RIFFieldActionPolicy.USE_RULES);		
		*/
		
		doubleRIFDataType.setFieldValidationPolicy(FieldActionPolicy.USE_FUNCTION);
		doubleRIFDataType.setValidationFunctionName("is_valid_double");
		
		return doubleRIFDataType;
	}
	
	private static RIFDataType createICDDataType() {
		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.icd.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.icd.description");
		RIFDataType icdCodeRIFDataType
			= RIFDataType.newInstance(
				"rif_icd_code",
				name, 
				description,
				true);		
		
		icdCodeRIFDataType.setCleaningFunctionName("clean_icd_code");	
		icdCodeRIFDataType.setFieldCleaningPolicy(FieldActionPolicy.USE_FUNCTION);
		icdCodeRIFDataType.setFieldValidationPolicy(FieldActionPolicy.DO_NOTHING);
		
		return icdCodeRIFDataType;
	}
	
	private static RIFDataType createYearDataType() {
		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.year.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.year.description");

		RIFDataType yearRIFDataType
			= RIFDataType.newInstance(
				"rif_year",
				name, 
				description,
				true);
			
		ValidationRule validationRule
			= ValidationRule.newInstance();
		String validationRuleName1
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataType.year.vaidationRule1.name");
		validationRule.setName(validationRuleName1);
		String validationRuleDescription1
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataType.year.vaidationRule1.description");
		validationRule.setDescription(validationRuleDescription1);
		
		// MS SQL specific code - need different regular expression code for MS SQL
		//validationRule.setValidValue("^(19|20)\\d{2}$"); //postgres
		//validationRule.setValidValue("[12][09][0-9][0-9]"); // SQL compatible - doesn't have full regexp capabilities
		// Hopefully the rule below works for both MS and PG, not tested on PG fully...
		validationRule.setValidValue("[1|2][9|0][0|1|2|3|4|5|6|7|8|9][0|1|2|3|4|5|6|7|8|9]");
		yearRIFDataType.setFieldValidationPolicy(FieldActionPolicy.USE_RULES);
		yearRIFDataType.setFieldCleaningPolicy(FieldActionPolicy.DO_NOTHING);		
		yearRIFDataType.addValidationRule(validationRule);
		return yearRIFDataType;		
	}
	
	private static RIFDataType createSexDataType() {
		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description");
	
		/**
		 * 0 = male
		 * 1 = female
		 * 2 = hermaphrodite
		 * 3 = unknown
		 */
		RIFDataType sexRIFDataType
			= RIFDataType.newInstance(
				"rif_sex",
				name, 
				description,
				true);
		


		String cleaningName1 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.name1");
		String description1
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.description1");
		CleaningRule cleaningRule1
			= CleaningRule.newInstance(
				cleaningName1, 
				description1, 
				"^female|FEMALE$", 
				"2", 
				true);
		sexRIFDataType.addCleaningRule(cleaningRule1);
			
		String cleaningName2 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.name2");
		String cleaningDescription2
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.description2");
		CleaningRule cleaningRule2
			= CleaningRule.newInstance(
				cleaningName2, 
				cleaningDescription2, 
				"^male|MALE$", 
				"1", 
				true);
		sexRIFDataType.addCleaningRule(cleaningRule2);

		String cleaningName3 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.name3");
		String cleaningDescription3
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.description3");		
		CleaningRule cleaningRule3
			= CleaningRule.newInstance(
				cleaningName3, 
				cleaningDescription3, 
				"^hermaphrodite|HERMAPHRODITE$", 
				"5", 
				true);
		sexRIFDataType.addCleaningRule(cleaningRule3);

		String cleaningName4 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.name4");
		String cleaningDescription4
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.description4");		
		CleaningRule cleaningRule4
			= CleaningRule.newInstance(
				cleaningName4, 
				cleaningDescription4, 
				"^unknown|UNKNOWN$", 
				"6", 
				true);
		sexRIFDataType.addCleaningRule(cleaningRule4);
				
		String cleaningName5 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.name5");
		String cleaningDescription5
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.description5");		
		CleaningRule cleaningRule5
			= CleaningRule.newInstance(
				cleaningName5, 
				cleaningDescription5, 
				"^[fF]$", 
				"2", 
				true);
		sexRIFDataType.addCleaningRule(cleaningRule5);
				
		String cleaningName6 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.name6");
		String cleaningDescription6
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.description6");		
		CleaningRule cleaningRule6
			= CleaningRule.newInstance(
				cleaningName6, 
				cleaningDescription6, 
				"^[mM]$", 
				"1", 
				true);
		sexRIFDataType.addCleaningRule(cleaningRule6);
		
		String cleaningName7 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.name7");
		String cleaningDescription7
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.description7");		
		CleaningRule cleaningRule7
			= CleaningRule.newInstance(
				cleaningName7, 
				cleaningDescription7, 
				"^[hH]$", 
				"5", 
				true);
		sexRIFDataType.addCleaningRule(cleaningRule7);

		String cleaningName8 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.name8");
		String cleaningDescription8
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.cleaning.description8");		
		CleaningRule cleaningRule8
			= CleaningRule.newInstance(
				cleaningName8, 
				cleaningDescription8, 
				"^[uU]$", 
				"6", 
				true);
		sexRIFDataType.addCleaningRule(cleaningRule8);
		
		
		sexRIFDataType.setFieldValidationPolicy(FieldActionPolicy.USE_RULES);
		
		sexRIFDataType.setFieldCleaningPolicy(FieldActionPolicy.USE_RULES);		

		ValidationRule validationRule
			= ValidationRule.newInstance();
		
		String validatingName1 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.validating.name1");
		String validatingDescription8
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.validating.description1");	
		validationRule.setName(validatingName1);
		validationRule.setDescription(validatingDescription8);
		validationRule.setValidValue("[1|2|3|5|6]");
		sexRIFDataType.addValidationRule(validationRule);
	
		return sexRIFDataType;
	}
		
	private static RIFDataType createIntegerDataType() {

		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.integer.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.integer.description");
		RIFDataType integerRIFDataType
			= RIFDataType.newInstance(
				"rif_integer",
				name, 
				description,
				true);		
	
		/*
		ValidationRule validationRule
			= ValidationRule.newInstance();
		validationRule.setValidValue("^(\\d+)");
		integerRIFDataType.setFieldValidationPolicy(RIFFieldActionPolicy.USE_RULES);
		integerRIFDataType.setFieldCleaningPolicy(RIFFieldActionPolicy.DO_NOTHING);			
		integerRIFDataType.addValidationRule(validationRule);
		*/
		
		integerRIFDataType.setFieldValidationPolicy(FieldActionPolicy.USE_FUNCTION);
		integerRIFDataType.setValidationFunctionName("is_valid_integer");
		
		
		return integerRIFDataType;
	}
		
	private static RIFDataType createTextDataType() {
		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.text.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.text.description");		
		RIFDataType textDataType
			= RIFDataType.newInstance(
				"rif_text",
				name, 
				description,
				true);		
		
		ValidationRule validationRule
			= ValidationRule.newInstance();
		validationRule.setValidValue("^(\\w+)");
		textDataType.setFieldValidationPolicy(FieldActionPolicy.USE_RULES);
		textDataType.setFieldCleaningPolicy(FieldActionPolicy.DO_NOTHING);			
		
		return textDataType;
	}

	
	public static boolean isTextDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_TEXT_DATA_TYPE.getIdentifier());
		return result;
	}
	
	
	public static boolean isAgeDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_AGE_DATA_TYPE.getIdentifier());		
		return result;
	}

	
	public static boolean isSexDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_SEX_DATA_TYPE.getIdentifier());		
		return result;
	}
	
	public static boolean isDoubleDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_DOUBLE_DATA_TYPE.getIdentifier());		
		return result;	
	}

	public static boolean isIntegerDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_INTEGER_DATA_TYPE.getIdentifier());		
		return result;			
	}	
	

	public static boolean isASCIIDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_ASCII_DATA_TYPE.getIdentifier());		
		return result;
	}	

	public static boolean isICDDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_ICD_DATA_TYPE.getIdentifier());		
		return result;		
	}	
	
	public static boolean isYearDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_YEAR_DATA_TYPE.getIdentifier());		
		return result;		
	}	

	public static boolean isUKPostalCodeDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_UK_POSTAL_CODE_DATA_TYPE.getIdentifier());		
		return result;		
	}	

	public static boolean isQuintiliseDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_QUINTILISE_DATA_TYPE.getIdentifier());		
		return result;		
	}	

	public static boolean isDateDataType(final RIFDataType rifDataType) {
		if (rifDataType == null) {
			return false;
		}
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		boolean result
			= collator.equals(rifDataType.getIdentifier(), 
				RIF_DATE_DATA_TYPE.getIdentifier());		
		return result;
	}	
		
	private static RIFDataType createQuintiliseDataType() {

		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.quintilise.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.quintilise.description");
		RIFDataType quintiliseDataType
			= RIFDataType.newInstance(
				"rif_quintilise",
				name, 
				description,
				true);		
		
		//String validationRegularExpression = "^(\\d+)";
		//addValidationExpression(validationRegularExpression);
		quintiliseDataType.setFieldValidationPolicy(FieldActionPolicy.DO_NOTHING);		
		
		return quintiliseDataType;
	}
	
	public static RIFDataType createASCIIDataType() {

		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.asciiText.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.asciiText.description");
		RIFDataType asciiTextRIFDataType
			= RIFDataType.newInstance(
				"rif_ascii_text",
				name, 
				description,
				true);

		String validationRuleName1
			= RIFDataLoaderToolMessages.getMessage("rifDataType.asciiText.validationRule1.name");
		String validationRuleDescription1
			= RIFDataLoaderToolMessages.getMessage("rifDataType.asciiText.validationRule1.description");

		ValidationRule validationRule
			= ValidationRule.newInstance(
				validationRuleName1, 
				validationRuleDescription1, 
				"^(\\w+)", 
				true);

		asciiTextRIFDataType.setFieldValidationPolicy(FieldActionPolicy.USE_RULES);
		asciiTextRIFDataType.addValidationRule(validationRule);	
		asciiTextRIFDataType.setFieldCleaningPolicy(FieldActionPolicy.DO_NOTHING);			

		return asciiTextRIFDataType;
	}
	
	private static RIFDataType createUKPostalCodeDataType() {

		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.ukPostalCode.label");
		String description
			= RIFDataLoaderToolMessages.getMessage("rifDataType.ukPostalCode.description");
		RIFDataType ukPostalCodeRIFDataType
			= RIFDataType.newInstance(
				"rif_uk_postcode",
				name, 
				description,
				true);

		ukPostalCodeRIFDataType.setFieldValidationPolicy(FieldActionPolicy.USE_FUNCTION);
		ukPostalCodeRIFDataType.setValidationFunctionName("is_valid_uk_postal_code");
		ukPostalCodeRIFDataType.setFieldCleaningPolicy(FieldActionPolicy.USE_FUNCTION);
		ukPostalCodeRIFDataType.setCleaningFunctionName("clean_uk_postal_code");		
		
		return ukPostalCodeRIFDataType;
	}
	
	public ArrayList<RIFDataType> getRegisteredDataTypes() {
		ArrayList<RIFDataType> dataTypes
			= new ArrayList<RIFDataType>();
		dataTypes.addAll(dataTypeFromCodes.values());
		
		return dataTypes;
	}
	
	private static RIFDataType createDateDataType() {

		String name
			= RIFDataLoaderToolMessages.getMessage("rifDataType.date.label");

		//this is the order of date formats which are used to validate RIF dates

		ArrayList<ValidationRule> validationRules = new ArrayList<ValidationRule>();
		ValidationRule mmddyyyyValidationRule
			= ValidationRule.newInstance(
				"", 
				"", 
				"MM/DD/YYYY", 
				true);
		validationRules.add(mmddyyyyValidationRule);

		StringBuilder dateFormatPatternList = new StringBuilder();
		for (int i = 0; i < validationRules.size(); i++) {
			if (i != 0) {
				dateFormatPatternList.append(",");
			}
			dateFormatPatternList.append(validationRules.get(i).getValidValue());
		}
		
		String description
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataType.date.description",
				dateFormatPatternList.toString());
		
		RIFDataType dateDataType
			= RIFDataType.newInstance(
				"rif_date", 
				name, 
				description,
				true);
		
		dateDataType.setFieldValidationPolicy(FieldActionPolicy.USE_RULES);
		
		dateDataType.setValidationRules(validationRules);	
		
		return dateDataType;
	}

	

	private void deregisterDataType(final RIFDataType rifDataTypeToDelete) {
		dataTypeFromCodes.remove(rifDataTypeToDelete.getIdentifier());
		dataTypeFromNames.remove(rifDataTypeToDelete.getName());		
	}
	
	
	/*
	 * This method attempts to register a new custom data type.  When it encounters
	 * a type that is already known, it has two choices.  First, it can throw an
	 * exception, indicating that an attempt to add a duplicate type has been made.
	 * Second, it can simply overwrite fields in the currently registered type.
	 * 
	 * The first use is helpful in cases where a new type is being added through the UI.
	 * The second use is helpful when we're reading a list of saved rif data types
	 * from file and use them with the intent of overwriting any existing definitions
	 * 
	 */
	public void registerCustomDataType(
		final RIFDataType candidateDataType, 
		final boolean allowOverwrite) 
		throws RIFServiceException {

		//Validate fields of data type
		String candidateDataTypeName
			= candidateDataType.getName();
		
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(dataTypeFromNames.keySet());
		RIFDataType existingRegisteredDataType
			= dataTypeFromNames.get(candidateDataTypeName);
		if (existingRegisteredDataType != null) {
			//Type already exists
			
			if (allowOverwrite) {
				deregisterDataType(existingRegisteredDataType);
				
				RIFDataType.copyInto(
					candidateDataType, 
					existingRegisteredDataType);		
				
				dataTypeFromCodes.put(
					existingRegisteredDataType.getIdentifier(), 
					existingRegisteredDataType);
				dataTypeFromNames.put(
					existingRegisteredDataType.getName(), 
					existingRegisteredDataType);
			}
			else {
				
				//Error: Attempt to register a duplicate data type
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"general.validation.duplicateValue",
						candidateDataType.getName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFDataLoaderToolError.DUPLICATE_RIF_DATA_TYPE,
						errorMessage);
				throw rifServiceException;
			}		
		}
		else {
			dataTypeFromCodes.put(candidateDataType.getIdentifier(), candidateDataType);
			dataTypeFromNames.put(candidateDataType.getName(), candidateDataType);			
		}
	}
	
	public RIFDataType getDataTypeFromCode(final String dataTypeCode) {
		return dataTypeFromCodes.get(dataTypeCode);		
	}
	
	public RIFDataType getDataTypeFromName(final String dataTypeName) {
		return dataTypeFromNames.get(dataTypeName);		
	}
	
	public String[] getDataTypeCodes() {
		ArrayList<String> codes = new ArrayList<String>();
		codes.addAll(dataTypeFromCodes.keySet());
		return codes.toArray(new String[0]);
	}
	
	public String[] getDataTypeNames() {
		ArrayList<String> names = new ArrayList<String>();
		names.addAll(dataTypeFromNames.keySet());
		return names.toArray(new String[0]);
	}
	
	public void clearDataTypes() {
		dataTypeFromCodes.clear();
		dataTypeFromNames.clear();
	}

	private void registerReservedDataType(final RIFDataType reservedRIFDataType) {
		//Validate fields of data type
		dataTypeFromCodes.put(reservedRIFDataType.getIdentifier(), reservedRIFDataType);
		dataTypeFromNames.put(reservedRIFDataType.getName(), reservedRIFDataType);	
	}		
	
	
	public void setDataTypes(final ArrayList<RIFDataType> dataTypes) {
		clearDataTypes();
		for (RIFDataType dataType : dataTypes) {
			dataTypeFromCodes.put(dataType.getIdentifier(), dataType);
			dataTypeFromNames.put(dataType.getName(), dataType);			
		}
	}
	
	
	public String generateCustomDataTypeIdentifier() {		
    	Date date = new Date();
    	
    	SimpleDateFormat simpleTimeFormat 
 	   		= new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:SSS");
 	   	String result = simpleTimeFormat.format(date);
 	   	return result;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


