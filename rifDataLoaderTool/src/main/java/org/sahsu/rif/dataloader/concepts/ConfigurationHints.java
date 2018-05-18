package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.sahsu.rif.generic.util.FieldValidationUtility;

/**
 * <p>
 * The purpose of ConfigurationHints is to greatly reduce the number of 
 * configuration options which need to be explicitly set by end-users.
 * </p>
 * 
 * <p>
 * When users of the data loader tool import a CSV file, they will find approximately
 * a dozen separate configuration options which help govern the journey the data field
 * makes from its original data set to a final processed data set that is ready to be
 * loaded into the RIF production database.  To import a CSV file of 10 fields, it
 * would present the user with over 120 configurable options.  In the UK, numerator
 * files such as the Hospital Episode Statistics (HES) data sets can have well over
 * a hundred fields, which would present the user with over 1200 separate configurable
 * options.
 * </p>
 * 
 * <p>
 * In order to practically handle tables with a large number of fields, the Data 
 * Loader Tool has a Configuration Hints option.  The hints feature re-uses 
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration} and
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration} to 
 * match the names of input files and input file fields with fragments of configuration.
 * </p>
 * 
 * <p>
 * For example, suppose a file is named "numerator_cancer_cases".  A hint might be
 * "^numerator_*", which, if it matches the name of the file, would make 
 * this class assume that the RIFSchema field of the 
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration} should be
 * "health numerator data". When the data set configuration is created to hold 
 * properties of the file, ConfigurationHints would set the RIF Schema Area 
 * automatically based on the fact that the file name matched the name of a hint
 * which indicates the RIF schema area should be a numerator.
 * </p>
 * 
 * <p>
 * As another example, a CSV field may be named "birth_date" and a hint might be
 * "*_date*".  The hint may indicate that the RIFDataType should be set to Date
 * or another data type that can validate and clean a date field..
 * </p> 
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

public class ConfigurationHints {

	// ==========================================
	// Section Constants
	// ==========================================
	
	/**
	 * Hints for general data set configuration fields
	 * (eg: version, name, description).  They store regular expressions that
	 * attempt to match the name of the imported CSV file.
	 */
	private ArrayList<String> dataSetCoreNamePatterns;
	private HashMap<String, DataSetConfiguration> dataSetConfigurationFromPattern;
	
	/**
	 * Hints for the fields of a 
	 * {@link dataLoaderTool.businessConceptLayer.DataSetConfiguration}
	 * (eg: data type, requirement level, audit level).  They store regular
	 * expressions that attempt to match the name of a CSV file.
	 */
	private ArrayList<String> dataSetFieldCoreNamePatterns;
	private HashMap<String, DataSetFieldConfiguration> dataSetFieldConfigurationFromPattern;
		
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ConfigurationHints() {
		dataSetCoreNamePatterns = new ArrayList<String>();
		dataSetConfigurationFromPattern = new HashMap<String, DataSetConfiguration>();
		
		dataSetFieldCoreNamePatterns = new ArrayList<String>();
		dataSetFieldConfigurationFromPattern = new HashMap<String, DataSetFieldConfiguration>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public ArrayList<DataSetConfiguration> getDataSetConfigurationHints() {
		ArrayList<DataSetConfiguration> results = new ArrayList<DataSetConfiguration>();
		for (String dataSetCoreNamePattern : dataSetCoreNamePatterns) {
			DataSetConfiguration dataSetConfigurationHint
				= dataSetConfigurationFromPattern.get(dataSetCoreNamePattern);
			results.add(dataSetConfigurationHint);
		}
		
		return results;		
	}
	
	public ArrayList<DataSetFieldConfiguration> getDataSetFieldConfigurationHints() {
		ArrayList<DataSetFieldConfiguration> results = new ArrayList<DataSetFieldConfiguration>();
		for (String dataSetFieldCoreNamePattern : dataSetFieldCoreNamePatterns) {
			DataSetFieldConfiguration dataSetFieldConfigurationHint
				= dataSetFieldConfigurationFromPattern.get(dataSetFieldCoreNamePattern);
			results.add(dataSetFieldConfigurationHint);
		}
		
		return results;			
	}
	
	public void setDataSetConfigurationHints(
		final ArrayList<DataSetConfiguration> dataSetConfigurationHintsToAdd) {
	
		dataSetCoreNamePatterns.clear();
		dataSetConfigurationFromPattern.clear();
		for (DataSetConfiguration dataSetConfigurationHintToAdd : dataSetConfigurationHintsToAdd) {
			addDataSetConfigurationHint(dataSetConfigurationHintToAdd);
		}
	}
		
	public void setDataSetFieldConfigurationHints(
		final ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurationHintsToAdd) {
	
		dataSetFieldCoreNamePatterns.clear();
		dataSetFieldConfigurationFromPattern.clear();
		for (DataSetFieldConfiguration dataSetFieldConfigurationHintToAdd : dataSetFieldConfigurationHintsToAdd) {
			addDataSetFieldConfigurationHint(dataSetFieldConfigurationHintToAdd);
		}
	}
	
	public void addDataSetConfigurationHint(
		final DataSetConfiguration dataSetConfigurationHint) {
		
		String pattern = dataSetConfigurationHint.getName();
		dataSetCoreNamePatterns.add(pattern);
		dataSetConfigurationFromPattern.put(
			pattern, 
			dataSetConfigurationHint);
	}
	
	public void addDataSetFieldConfigurationHint(
		final DataSetFieldConfiguration dataSetFieldConfigurationHint) {
		
		String pattern = dataSetFieldConfigurationHint.getCoreFieldName();

		dataSetFieldCoreNamePatterns.add(pattern);
		dataSetFieldConfigurationFromPattern.put(
			pattern, 
			dataSetFieldConfigurationHint);		
	}
	
	public void clear() {
		dataSetCoreNamePatterns.clear();
		dataSetConfigurationFromPattern.clear();
		
		dataSetFieldCoreNamePatterns.clear();
		dataSetFieldConfigurationFromPattern.clear();
	}
	
	public void applyHintsToDataSetConfiguration(final DataSetConfiguration dataSetConfiguration) {
		
		/*
		 * This is done in two steps.  First, we want to scan a data set
		 * configuration to check whether there are any patterns that could be
		 * assigned which could help set general properties of the data set
		 * such as the schema area, description and version.
		 * 
		 * Then we go through each field of the skeleton template produced by
		 * the CSV import and use the field name to assign other properties
		 * (eg: field purpose, requirement level, clean field name, etc)
		 */
		applyGeneralDataSetHint(dataSetConfiguration);
		String coreDataSetName = dataSetConfiguration.getName();
		ArrayList<DataSetFieldConfiguration> fields
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration field : fields) {
			applyDataSetFieldHint(coreDataSetName, field);
		}		
	}
	
	private void applyGeneralDataSetHint(
		final DataSetConfiguration dataSetToConfigure) {
		
		String coreDataSetName
			= dataSetToConfigure.getName();
		
		for (String dataSetCoreNamePattern : dataSetCoreNamePatterns) {
			Pattern pattern = Pattern.compile(dataSetCoreNamePattern, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(coreDataSetName);
			if (matcher.matches()) {
				//We have our first match.  Copy hints into data set
				DataSetConfiguration dataSetConfigurationHint
					= dataSetConfigurationFromPattern.get(dataSetCoreNamePattern);
				if (dataSetConfigurationHint != null) {
					dataSetToConfigure.setDescription(
						dataSetConfigurationHint.getDescription());
					dataSetToConfigure.setVersion(
						dataSetConfigurationHint.getVersion());
					dataSetToConfigure.setRIFSchemaArea(
						dataSetConfigurationHint.getRIFSchemaArea());
				}
				break;
			}		
		}
	}
	
	private void applyDataSetFieldHint(
		final String coreDataSetName,
		final DataSetFieldConfiguration dataSetFieldToConfigure) {

		String coreDataSetFieldName
			= dataSetFieldToConfigure.getCoreFieldName();
		
		for (String dataSetCoreFieldNamePattern : dataSetFieldCoreNamePatterns) {
			Pattern pattern = Pattern.compile(dataSetCoreFieldNamePattern, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(coreDataSetFieldName);
			if (matcher.matches()) {
				//We have our first match.  Copy hints into data set
				DataSetFieldConfiguration dataSetFieldConfigurationHint
					= dataSetFieldConfigurationFromPattern.get(dataSetCoreFieldNamePattern);

				FieldValidationUtility fieldValidationUtility
					= new FieldValidationUtility();
				String hintCleanFieldValue
					= dataSetFieldConfigurationHint.getCleanFieldName();
				if (fieldValidationUtility.isEmpty(hintCleanFieldValue) == false) {
					dataSetFieldToConfigure.setCleanFieldName(hintCleanFieldValue);
				}
				
				String hintConvertFieldValue
					= dataSetFieldConfigurationHint.getConvertFieldName();
				if (fieldValidationUtility.isEmpty(hintCleanFieldValue) == false) {
					dataSetFieldToConfigure.setConvertFieldName(hintConvertFieldValue);
				}
							
				dataSetFieldToConfigure.setCoreFieldDescription(
					dataSetFieldConfigurationHint.getCoreFieldDescription());
				dataSetFieldToConfigure.setFieldRequirementLevel(
					dataSetFieldConfigurationHint.getFieldRequirementLevel());
				dataSetFieldToConfigure.setFieldPurpose(
					dataSetFieldConfigurationHint.getFieldPurpose());
				dataSetFieldToConfigure.setEmptyValueAllowed(
					dataSetFieldConfigurationHint.isEmptyValueAllowed());
				dataSetFieldToConfigure.setCheckOptions(
					dataSetFieldConfigurationHint.getCheckOptions());
				dataSetFieldToConfigure.setFieldChangeAuditLevel(
					dataSetFieldConfigurationHint.getFieldChangeAuditLevel());
				dataSetFieldToConfigure.setOptimiseUsingIndex(
					dataSetFieldConfigurationHint.optimiseUsingIndex());
				dataSetFieldToConfigure.setRIFDataType(
					dataSetFieldConfigurationHint.getRIFDataType());
				dataSetFieldToConfigure.setDuplicateIdentificationField(
					dataSetFieldConfigurationHint.isDuplicateIdentificationField());

				applyOtherDataSetFieldNameRules(dataSetFieldToConfigure);
				break;
			}		
		}
		
		dataSetFieldToConfigure.setCoreDataSetName(coreDataSetName);
		dataSetFieldToConfigure.setCoreFieldName(coreDataSetFieldName);
		dataSetFieldToConfigure.setLoadFieldName(coreDataSetFieldName);
	}
	/**
	 * Applies a set of bespoke rules which cater to special fields such 
	 * as the age/sex conversion field, the year data type or the 
	 * geographical resolution fields.
	 * @param fieldConfiguration
	 */
	private void applyOtherDataSetFieldNameRules(final DataSetFieldConfiguration fieldConfiguration) {
		FieldRequirementLevel fieldRequirementLevel
			= fieldConfiguration.getFieldRequirementLevel();
		if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {

			ConversionFunctionFactory conversionFunctionFactory
				= ConversionFunctionFactory.newInstance();
			ConversionFunction convertAgeSexFunction
				= conversionFunctionFactory.getRIFConvertFunction("convert_age_sex");
			
			RIFDataType rifDataType = fieldConfiguration.getRIFDataType();
			if (rifDataType.hasSameIdentifier(RIFDataTypeFactory.RIF_AGE_DATA_TYPE)) {
				fieldConfiguration.setCleanFieldName("age");
				fieldConfiguration.setConvertFieldName("age");
				fieldConfiguration.setConvertFunction(convertAgeSexFunction);
			}
			else if (rifDataType.hasSameIdentifier(RIFDataTypeFactory.RIF_SEX_DATA_TYPE)) { 
				fieldConfiguration.setCleanFieldName("sex");
				fieldConfiguration.setConvertFieldName("sex");
				fieldConfiguration.setConvertFunction(convertAgeSexFunction);				
			}
			else if (rifDataType.hasSameIdentifier(RIFDataTypeFactory.RIF_YEAR_DATA_TYPE)) {
				fieldConfiguration.setCleanFieldName("year");
				fieldConfiguration.setConvertFieldName("year");
			} 
		}
		
		FieldPurpose fieldPurpose
			= fieldConfiguration.getFieldPurpose();
		if (fieldPurpose == FieldPurpose.GEOGRAPHICAL_RESOLUTION) {
			fieldConfiguration.setConvertFieldName(fieldConfiguration.getCleanFieldName());
		}
	}
	
	public boolean hasIdenticalContents(final ConfigurationHints otherConfigurationHints) {
		
		//KLG: Stubbed - this is a lot of work we'll do later on.  Configuration hints
		//only influence activities within the tool and if they change they won't have any
		//effect on how data sets are loaded into the RIF production database.
		return true;
	}
	
	public int getTotalHints() {
		return dataSetCoreNamePatterns.size() + dataSetFieldCoreNamePatterns.size();
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


