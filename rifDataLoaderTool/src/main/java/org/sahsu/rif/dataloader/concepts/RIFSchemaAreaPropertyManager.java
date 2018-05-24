package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;

import org.sahsu.rif.generic.system.Messages;

/**
 * Holds knowledge about what properties are expected for different parts of the
 * RIF schema {@link rifDataLoaderTool.businessConceptLayer.RIFSchemaArea}.
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

public class RIFSchemaAreaPropertyManager {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();

	// ==========================================
	// Section Properties
	// ==========================================
	
	private HashMap<String, RIFDataType> dataTypeFromCovariateFieldName;
	private HashMap<String, RIFDataType> dataTypeFromHealthCodeFieldName;
	private HashMap<String, RIFDataType> dataTypeFromNumeratorFieldName;
	private HashMap<String, RIFDataType> dataTypeFromDenominatorFieldName;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFSchemaAreaPropertyManager() {
		
		
		/*
		 * Fields in covariate data
		 */
		dataTypeFromCovariateFieldName = new HashMap<String, RIFDataType>();
		dataTypeFromCovariateFieldName.put(
			"geography", 
			RIFDataTypeFactory.RIF_TEXT_DATA_TYPE);
		dataTypeFromCovariateFieldName.put(
			"geolevel_name", 
			RIFDataTypeFactory.RIF_TEXT_DATA_TYPE);
		dataTypeFromCovariateFieldName.put(
			"covariate_name", 
			RIFDataTypeFactory.RIF_TEXT_DATA_TYPE);
		dataTypeFromCovariateFieldName.put(
			"min", 
			RIFDataTypeFactory.RIF_DOUBLE_DATA_TYPE);		
		dataTypeFromCovariateFieldName.put(
			"max", 
			RIFDataTypeFactory.RIF_DOUBLE_DATA_TYPE);		
		dataTypeFromCovariateFieldName.put(
			"type", 
			RIFDataTypeFactory.RIF_DOUBLE_DATA_TYPE);			

		dataTypeFromHealthCodeFieldName
			= new HashMap<String, RIFDataType>();
		dataTypeFromHealthCodeFieldName.put(
			"code", 
			RIFDataTypeFactory.RIF_TEXT_DATA_TYPE);
		dataTypeFromHealthCodeFieldName.put(
			"label", 
			RIFDataTypeFactory.RIF_TEXT_DATA_TYPE);
		dataTypeFromHealthCodeFieldName.put(
			"description", 
			RIFDataTypeFactory.RIF_TEXT_DATA_TYPE);		
		dataTypeFromHealthCodeFieldName.put(
			"nameSpace", 
			RIFDataTypeFactory.RIF_TEXT_DATA_TYPE);
				
		dataTypeFromNumeratorFieldName = new HashMap<String, RIFDataType>();
		dataTypeFromNumeratorFieldName.put(
			"year", 
			RIFDataTypeFactory.RIF_YEAR_DATA_TYPE);
		dataTypeFromNumeratorFieldName.put(
			"age_sex_group", 
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);

		dataTypeFromDenominatorFieldName 
			= new HashMap<String, RIFDataType>();
		dataTypeFromDenominatorFieldName.put(
			"year", 
			RIFDataTypeFactory.RIF_YEAR_DATA_TYPE);
		dataTypeFromDenominatorFieldName.put(
			"age", 
			RIFDataTypeFactory.createAgeRIFDataType());
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public ArrayList<CheckOption> getCheckOptions(
		final RIFSchemaArea rifSchemaArea) {
		
		ArrayList<CheckOption> rifCheckOptions
			= new ArrayList<CheckOption>();
		
		rifCheckOptions.add(CheckOption.PERCENT_EMPTY);
		if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
			(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
			
			//with these parts of the schema, we can guarantee there will
			//be a year field
			rifCheckOptions.add(CheckOption.PERCENT_EMPTY_PER_YEAR);
		}
		
		return rifCheckOptions;		
	}
	
	public boolean isRIFCheckOptionAllowed(
		final RIFSchemaArea rifSchemaArea,
		final CheckOption checkOption) {
		
		if ((checkOption == CheckOption.PERCENT_EMPTY_PER_YEAR) &&
			(rifSchemaArea != RIFSchemaArea.HEALTH_NUMERATOR_DATA) &&
			(rifSchemaArea != RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
				
			//yearly checks are only allowed in the parts of the schema where
			//we expect a year to be
			return false;
		}			
		
		return true;	
	}
	
	public String[] getMissingRequiredConvertFieldNames(
		final RIFSchemaArea rifSchemaArea,
		final String[] convertFieldNames) {

		ArrayList<String> missingRequiredFieldNames = new ArrayList<String>();

		Collator collator = GENERIC_MESSAGES.getCollator();
		
		String[] requiredConvertFieldNames
			= getRequiredConvertFieldNames(rifSchemaArea);		
		for (String requiredConvertFieldName : requiredConvertFieldNames) {
			boolean fieldFound = false;			
			for (String convertFieldName : convertFieldNames) {
				if (collator.equals(requiredConvertFieldName, convertFieldName)) {
					fieldFound = true;
					break;
				}
			}
			if (fieldFound == false) {
				if (collator.equals(requiredConvertFieldName, "age_sex_group")) {
					//Check - if it's the age_sex_group field, we have a special case
					//to consider.  There may not be an 'age_sex_group'
					if ( listContainsItem("age", convertFieldNames) == false ||
						 listContainsItem("sex", convertFieldNames) == false) {
						
						missingRequiredFieldNames.add(requiredConvertFieldName);					
					}
				}
				else {
					missingRequiredFieldNames.add(requiredConvertFieldName);					
				}
				
			}
		}

		String[] results = new String[0];
		//String[] results = missingRequiredFieldNames.toArray(new String[0]);
		return results;
	}
	
	private boolean listContainsItem(
		final String searchItem,
		final String[] listItems) {
		
		Collator collator = GENERIC_MESSAGES.getCollator();
		
		for (String listItem : listItems) {
			if (collator.equals(searchItem, listItem)) {
				return true;
			}
		}
		return false;
	}
	
	public String[] getRequiredConvertFieldNames(
		final RIFSchemaArea rifSchemaArea) {
		
		String[] requiredConvertFieldNames = new String[0];
		if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
			requiredConvertFieldNames = new String[2];
			requiredConvertFieldNames[0] = "year";
			requiredConvertFieldNames[1] = "age_sex_group";			
		}
		else if (rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA) {
			requiredConvertFieldNames = new String[2];
			requiredConvertFieldNames[0] = "year";
			requiredConvertFieldNames[1] = "age_sex_group";			
		}		
		
		return requiredConvertFieldNames;
		
	}
	
	public RIFDataType getExpectedRIFDataType(
		final RIFSchemaArea rifSchemaArea,
		final String fieldName) {
		
		if ((rifSchemaArea == null) ||
			(fieldName == null)) {
			return null;
		}
		
		if (rifSchemaArea == RIFSchemaArea.HEALTH_CODE_DATA) {
			return dataTypeFromHealthCodeFieldName.get(fieldName);			
		}
		else if (rifSchemaArea == RIFSchemaArea.COVARIATE_DATA) {
			return dataTypeFromCovariateFieldName.get(fieldName);
		}
		else if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
			return dataTypeFromNumeratorFieldName.get(fieldName);
		}
		else if (rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA) {
			return dataTypeFromDenominatorFieldName.get(fieldName);
		}
		else {
			return null;
		}
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


