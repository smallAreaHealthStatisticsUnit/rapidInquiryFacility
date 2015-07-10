package rifDataLoaderTool.businessConceptLayer;


import rifDataLoaderTool.businessConceptLayer.rifDataTypes.*;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.text.Collator;
/**
 * <p>
 * Describes the properties of a function that is used to convert a cleaned
 * data field to a field that is expected in the RIF schema.  The class was originally
 * developed to combine the values of a year and age field in a cleaned table so
 * that a new column <code>age_sex_group</code>, would appear in the converted table.  
 * </p>
 * 
 * <p>
 * When RIF managers are trying to import new denominator and numerator tables, their
 * original data sets may have separate columns for <code>sex</code> and <code>age</code>.
 * However, the RIF schema requires that these fields are combined into a single 
 * <code>age_sex_group</code>.
 * </p>
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

public class RIFConversionFunction {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String code;	
	private String schemaName;
	private String functionName;
	private ArrayList<String> formalParameterNames;
	private HashMap<String, AbstractRIFDataType> dataTypeFromFormalParameterName;
	
	private ArrayList<DataSetFieldConfiguration> actualParameterValues;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFConversionFunction() {
		formalParameterNames = new ArrayList<String>();
		dataTypeFromFormalParameterName = new HashMap<String, AbstractRIFDataType>();

		actualParameterValues = new ArrayList<DataSetFieldConfiguration>();
	}
	
	public void defineFormalParameter(
		final String parameterName,
		final AbstractRIFDataType rifDataType) {
		
		formalParameterNames.add(parameterName);
		dataTypeFromFormalParameterName.put(
			parameterName, 
			rifDataType);		
	}

	public void addActualParameter(
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
		
		actualParameterValues.add(dataSetFieldConfiguration);		
	}
	
	public void checkErrors() 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		//check they have the same number of parameters
		if (formalParameterNames.size() != actualParameterValues.size()) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.IMPROPERLY_SET_CONVERSION_FUNCTION, 
					errorMessage);
			throw rifServiceException;
		}
		else {		
			Collator collator = RIFDataLoaderToolMessages.getCollator();
		
			for (int i = 0; i < formalParameterNames.size(); i++) {		
				String currentFormalParameterName
					= formalParameterNames.get(i);
				String formalParameterTypeIdentifier
					= dataTypeFromFormalParameterName.get(currentFormalParameterName).getIdentifier();
			
				DataSetFieldConfiguration currentFieldConfiguration
					= actualParameterValues.get(i);
				String actualFieldName= currentFieldConfiguration.getCleanFieldName();
				String actualParameterTypeIdentifier
					= currentFieldConfiguration.getRIFDataType().getIdentifier();
			
				if (collator.equals(
					currentFormalParameterName,
					actualFieldName) == false) {
				
					//parameter names do not match
					String errorMessage
						= RIFDataLoaderToolMessages.getMessage("");
					errorMessages.add(errorMessage);
				
				}
				else if (collator.equals(
					formalParameterTypeIdentifier,
					actualParameterTypeIdentifier) == false) {
				
					//parameters have same name but do not have the same data types			
				}
			}	
		}
	
		if (errorMessages.size() > 0) {			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.IMPROPERLY_SET_CONVERSION_FUNCTION, 
					errorMessages);	
			throw rifServiceException;
		}
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
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


