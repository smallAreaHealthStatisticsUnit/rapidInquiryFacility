package org.sahsu.rif.dataloader.presentation.interactive;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldChangeAuditLevel;
import org.sahsu.rif.dataloader.concepts.FieldPurpose;
import org.sahsu.rif.dataloader.concepts.FieldRequirementLevel;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;

/**
 *
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

class DataSetConfigurationHints {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private String[] numeratorTableHints;
	private String[] denominatorTableHints;
	private String[] healthCodeTableHints;
	private String[] covariateTableHints;
	private String[] geographicalResolutionFieldNameHints;
	private String[] geoSpatialTableHints;

	private String[] requiredFieldNameHints;
	private String[] yearFieldNameHints;
	private String[] ageFieldNameHints;
	private String[] sexFieldNameHints;
	private String[] healthCodeFieldNameHints;
	private String[] postCodeNameHints;
	private String[] dateNameHints;
		
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetConfigurationHints() {
		numeratorTableHints = new String[2];
		numeratorTableHints[0] = ".*num_.*";
		numeratorTableHints[1] = ".*numerator.*";
		
		denominatorTableHints = new String[4];
		denominatorTableHints[0] = ".*pop_.*";
		denominatorTableHints[1] = ".*population.*";
		denominatorTableHints[2] = ".*denom_.*";
		denominatorTableHints[3] = ".*denominator.*";
		
		healthCodeTableHints = new String[3];
		healthCodeTableHints[0] = ".*tax_.*";
		healthCodeTableHints[1] = ".*icd.*";
		healthCodeTableHints[2] = ".*opcs.*";
		
		covariateTableHints = new String[6];
		covariateTableHints[0] = ".*cov_.*";
		covariateTableHints[1] = ".*covariate.*";
		covariateTableHints[2] = ".*eth_.*";
		covariateTableHints[3] = ".*ethnicity.*";
		covariateTableHints[4] = ".*ses_.*";
		covariateTableHints[5] = ".*socio_economic_status.*";
		
		geoSpatialTableHints = new String[2];
		geoSpatialTableHints[0] = ".*geo_.*";
		geoSpatialTableHints[1] = ".*geospatial.*";

		//strict set of field name requirements
		requiredFieldNameHints = new String[4];
		requiredFieldNameHints[0] = "^year$";
		requiredFieldNameHints[1] = "^age$";
		requiredFieldNameHints[2] = "^sex$";
		requiredFieldNameHints[3] = "^icd.*";
		
		
		//Use common ones for the UK
		geographicalResolutionFieldNameHints = new String[19];
		geographicalResolutionFieldNameHints[0] = "region";
		geographicalResolutionFieldNameHints[1] = "district";
		geographicalResolutionFieldNameHints[2] = "^ward.*";
		geographicalResolutionFieldNameHints[3] = "^currward";
		geographicalResolutionFieldNameHints[4] = "^resgor$";
		geographicalResolutionFieldNameHints[5] = "^gortreat$";
		geographicalResolutionFieldNameHints[6] = "^soal$"; // HES code
		geographicalResolutionFieldNameHints[7] = "^soam$"; // HES code
		geographicalResolutionFieldNameHints[8] = "^soa$"; // HES code
		geographicalResolutionFieldNameHints[9] = "^oacode$"; // HES code
		geographicalResolutionFieldNameHints[10] = "^oa$"; // HES code
		geographicalResolutionFieldNameHints[11] = "output_area";
		geographicalResolutionFieldNameHints[12] = "super_output_area";

		//Use hints based on place naming conventions used by US Census Bureau
		geographicalResolutionFieldNameHints[13] = ".*county.*";
		geographicalResolutionFieldNameHints[14] = ".*state.*";
		geographicalResolutionFieldNameHints[15] = "^place.*";
		geographicalResolutionFieldNameHints[16] = "^place.*";
		geographicalResolutionFieldNameHints[17] = ".*bna.*"; //US: block numbering areas
		geographicalResolutionFieldNameHints[18] = ".*census[ ,_]{0,1}tract.*"; //US 
		
		
		
		yearFieldNameHints = new String[2];
		yearFieldNameHints[0] = ".*yr.*";
		yearFieldNameHints[1] = ".*year.*";
		
		ageFieldNameHints = new String[3];
		ageFieldNameHints[0] = "^age$";		
		ageFieldNameHints[1] = ".*age_.*";
		ageFieldNameHints[2] = ".*_age.*";
		
		sexFieldNameHints = new String[2];
		sexFieldNameHints[0] = ".*sex.*";
		sexFieldNameHints[1] = ".*gender.*";
		
		healthCodeFieldNameHints = new String[2];
		healthCodeFieldNameHints[0] = ".*icd.*";
		healthCodeFieldNameHints[1] = ".*opcs.*";

		postCodeNameHints = new String[2];
		postCodeNameHints[0] = ".*post.*";
		postCodeNameHints[1] = ".*postal.*";
		
		dateNameHints = new String[2];
		dateNameHints[0] = ".*date.*";
		dateNameHints[1] = ".*dob.*";
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void configureDataSetConfiguration(
		final RIFDataTypeFactory rifDataTypeFactory,
		final DataSetConfiguration dataSetConfiguration,
		final String[] fieldNames,
		final String[][] previewData) {
		
		//Inspect the core data set name and guess the RIF Schema Area
		
		String coreDataSetName = dataSetConfiguration.getName();
		RIFSchemaArea rifSchemaArea = guessRIFSchemaArea(coreDataSetName);
		dataSetConfiguration.setRIFSchemaArea(rifSchemaArea);
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			String coreFieldName
				= fieldConfiguration.getCoreFieldName();
			
			RIFDataType rifDataType
				= guessDataTypeFromFieldName(
					rifDataTypeFactory,
					coreFieldName);
			
			FieldPurpose fieldPurpose
				= guessFieldPurposeFromFieldName(coreFieldName);
			fieldConfiguration.setFieldPurpose(fieldPurpose);
			fieldConfiguration.setRIFDataType(rifDataType);
			
			FieldRequirementLevel fieldRequirementLevel
				= guessFieldRequirementLevelFromFieldName(coreFieldName);
			fieldConfiguration.setFieldRequirementLevel(fieldRequirementLevel);
			
			if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
				fieldConfiguration.setFieldChangeAuditLevel(FieldChangeAuditLevel.INCLUDE_FIELD_CHANGE_DESCRIPTION);
			}
			else if (fieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD) {
				fieldConfiguration.setFieldChangeAuditLevel(FieldChangeAuditLevel.INCLUDE_FIELD_NAME_ONLY);
			}
			else {
				//ignore
				fieldConfiguration.setFieldChangeAuditLevel(FieldChangeAuditLevel.NONE);
			}
		}		
	}
	
	private RIFSchemaArea guessRIFSchemaArea(
		final String coreDataSetName) {
		
		if (valueInHints(coreDataSetName, denominatorTableHints)) {
			return RIFSchemaArea.POPULATION_DENOMINATOR_DATA;
		}
		else if (valueInHints(coreDataSetName, healthCodeTableHints)) {
			return RIFSchemaArea.HEALTH_CODE_DATA;
		}
		else if (valueInHints(coreDataSetName, covariateTableHints)) {
			return RIFSchemaArea.COVARIATE_DATA;
		}
		else {
			return RIFSchemaArea.HEALTH_NUMERATOR_DATA;
		}		
	}

	private FieldRequirementLevel guessFieldRequirementLevelFromFieldName(
		final String fieldName) {

		if (valueInHints(fieldName,requiredFieldNameHints) ||
			valueInHints(fieldName, geographicalResolutionFieldNameHints)) {
			return FieldRequirementLevel.REQUIRED_BY_RIF;
		}
		else {
			return FieldRequirementLevel.IGNORE_FIELD;
		}
	}	
	
	private FieldPurpose guessFieldPurposeFromFieldName(
		final String fieldName) {

		if (valueInHints(fieldName,covariateTableHints)) {
			return FieldPurpose.COVARIATE;
		}
		else if (valueInHints(fieldName, geographicalResolutionFieldNameHints)) {
			return FieldPurpose.GEOGRAPHICAL_RESOLUTION;
		}
		else if (valueInHints(fieldName, healthCodeFieldNameHints)) {
			return FieldPurpose.HEALTH_CODE;
		}
		else {
			return FieldPurpose.OTHER;
		}
	}
	
	private RIFDataType guessDataTypeFromFieldName(
		final RIFDataTypeFactory rifDataTypeFactory,
		final String fieldName) {
				
		if (valueInHints(fieldName, yearFieldNameHints)) {
			return rifDataTypeFactory.getDataTypeFromCode("rif_year");
		}
		else if (valueInHints(fieldName, ageFieldNameHints)) {
			return rifDataTypeFactory.getDataTypeFromCode("rif_age");
		}
		else if (valueInHints(fieldName, sexFieldNameHints)) {
			return rifDataTypeFactory.getDataTypeFromCode("rif_sex");
		}
		else if (valueInHints(fieldName, healthCodeFieldNameHints)) {
			return rifDataTypeFactory.getDataTypeFromCode("rif_icd_code");
		}
		else if (valueInHints(fieldName, postCodeNameHints)) {
			return rifDataTypeFactory.getDataTypeFromCode("rif_uk_postcode");
		}
		else if (valueInHints(fieldName, dateNameHints)) {
			return rifDataTypeFactory.getDataTypeFromCode("rif_date");
		}	
		else {
			return rifDataTypeFactory.getDataTypeFromCode("rif_text");
		}	
	}
	
	private boolean valueInHints(
		final String value, 
		final String[] hints) {
		
		for (String hint : hints) {
			Pattern pattern = Pattern.compile(hint.toUpperCase());
			Matcher matcher = pattern.matcher(value.toUpperCase());
			if (matcher.matches()) {
				return true;
			}
		}
		
		return false;
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


