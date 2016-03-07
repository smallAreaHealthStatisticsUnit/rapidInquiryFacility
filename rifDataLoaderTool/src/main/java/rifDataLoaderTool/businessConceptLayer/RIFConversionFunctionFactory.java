package rifDataLoaderTool.businessConceptLayer;

import java.util.HashMap;

/**
 * A factory class that manufacturers instances of RIF functions that help
 * ensure that fields from a cleaned data table map to fields in a converted stage
 * table.
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

public class RIFConversionFunctionFactory {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private HashMap<String, RIFConversionFunction> functionFromName;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	private RIFConversionFunctionFactory() {
		functionFromName = new HashMap<String, RIFConversionFunction>();
	}
	
	public static RIFConversionFunctionFactory newInstance() {

		RIFConversionFunctionFactory factory
			= new RIFConversionFunctionFactory();
		
		/*
		 * Function: age_sex_group_converter
		 * Inputs:
		 * (1) "age": AgeRIFDataType
		 * (2) "sex": SexRIFDataType
		 * 
		 * Returns:
		 * age_sex_group : IntegerRIFDataType
		 */
		final RIFConversionFunction ageSexConversionFunction
			= RIFConversionFunction.newInstance();
		ageSexConversionFunction.setSupportsOneToOneConversion(false);
		ageSexConversionFunction.setSchemaName(null);
		ageSexConversionFunction.setFunctionName("convert_age_sex");
		ageSexConversionFunction.defineFormalParameter(
			"age", RIFDataTypeFactory.createAgeRIFDataType());
		ageSexConversionFunction.defineFormalParameter(
			"sex", RIFDataTypeFactory.RIF_SEX_DATA_TYPE);
		ageSexConversionFunction.setConvertFieldName("age_sex_group");
		factory.registerConvertFunction(
			ageSexConversionFunction.getFunctionName(), 
			ageSexConversionFunction);
		
		/*
		 * Function: format_date
		 * Inputs:
		 * (1) "date": AgeRIFDataType
		 * 
		 * Returns:
		 * age_sex_group : IntegerRIFDataType
		 */
		final RIFConversionFunction dateFormattingFunction
			= RIFConversionFunction.newInstance();
		dateFormattingFunction.setSchemaName(null);
		dateFormattingFunction.setFunctionName("format_date");
		dateFormattingFunction.setSupportsOneToOneConversion(true);
		
		dateFormattingFunction.defineFormalParameter(
			"date", 
			RIFDataTypeFactory.RIF_DATE_DATA_TYPE);
		factory.registerConvertFunction(
			ageSexConversionFunction.getFunctionName(), 
			ageSexConversionFunction);
				
		/*
		 * Function: extract_age
		 * Inputs:
		 * (1) "date": AgeRIFDataType
		 * 
		 * Returns:
		 * age : AgeRIFDataType
		 */
		final RIFConversionFunction extractAgeFromDateFunction
			= RIFConversionFunction.newInstance();
		extractAgeFromDateFunction.setSupportsOneToOneConversion(true);
		extractAgeFromDateFunction.setSchemaName(null);
		extractAgeFromDateFunction.setFunctionName("extract_age");
		factory.registerConvertFunction(
			extractAgeFromDateFunction.getFunctionName(), 
			extractAgeFromDateFunction);		
		
		
		
		return factory;
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	private void registerConvertFunction(
		final String code,
		final RIFConversionFunction rifConversionFunction) {
		
		functionFromName.put(code, rifConversionFunction);		
	}
	
	public RIFConversionFunction getRIFConvertFunction(
		final String code) {
		
		return functionFromName.get(code);
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


