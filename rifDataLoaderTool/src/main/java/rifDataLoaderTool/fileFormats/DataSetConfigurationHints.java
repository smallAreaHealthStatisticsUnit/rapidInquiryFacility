package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

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

public class DataSetConfigurationHints {

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
	private String[] geoSpatialTableHints;
	
	private String[] yearFieldNameHints;
	private String[] ageFieldNameHints;
	private String[] sexFieldNameHints;
	private String[] healthCodeFieldNameHints;
	private String[] postCodeNameHints;
	private String[] dateNameHints;
	
	
	private String[] geographicalResolutionHints;
	
	
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

		yearFieldNameHints = new String[2];
		yearFieldNameHints[0] = ".*yr.*";
		yearFieldNameHints[1] = ".*year.*";
		
		ageFieldNameHints = new String[2];
		ageFieldNameHints[0] = ".*age_.*";
		ageFieldNameHints[1] = ".*_age.*";
		
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
			RIFDataType rifDataType
				= guessDataTypeFromFieldName(fieldConfiguration.getCoreFieldName());
			fieldConfiguration.setRIFDataType(rifDataType);
		}		
	}
	
	private RIFSchemaArea guessRIFSchemaArea(
		final String coreDataSetName) {
		
		System.out.println("guessRIFSchemaArea for=="+coreDataSetName+"==");
		if (valueInHints(coreDataSetName, denominatorTableHints)) {
			return RIFSchemaArea.POPULATION_DENOMINATOR_DATA;
		}
		else if (valueInHints(coreDataSetName, healthCodeTableHints)) {
			return RIFSchemaArea.HEALTH_CODE_DATA;
		}
		else if (valueInHints(coreDataSetName, covariateTableHints)) {
			System.out.println("Data Set Configuration Hints - it's a covariate table");
			return RIFSchemaArea.COVARIATE_DATA;
		}
		else if (valueInHints(coreDataSetName, geoSpatialTableHints)) {
			return RIFSchemaArea.GEOMETRY_DATA;
		}
		else {
			System.out.println("Data Set Configuration Hints - it's a health numerator table");
			return RIFSchemaArea.HEALTH_NUMERATOR_DATA;
		}		
	}
	
	private RIFDataType guessDataTypeFromFieldName(
		final String fieldName) {
		
		RIFDataTypeFactory rifDataTypeFactory = RIFDataTypeFactory.newInstance();
		
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
			Pattern pattern = Pattern.compile(hint);
			Matcher matcher = pattern.matcher(value);
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


