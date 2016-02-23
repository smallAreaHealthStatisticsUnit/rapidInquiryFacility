package rifDataLoaderTool.businessConceptLayer;


import rifDataLoaderTool.businessConceptLayer.rifDataTypes.*;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;




import java.util.HashMap;
import java.util.ArrayList;

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

	// ==========================================
	// Section Properties
	// ==========================================
	private HashMap<String, AbstractRIFDataType> dataTypeFromCodes;
	private HashMap<String, AbstractRIFDataType> dataTypeFromNames;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private RIFDataTypeFactory() {
		
		dataTypeFromCodes = new HashMap<String, AbstractRIFDataType>();
		dataTypeFromNames = new HashMap<String, AbstractRIFDataType>();
		
		AgeRIFDataType ageRIFDataType = AgeRIFDataType.newInstance();
		dataTypeFromCodes.put(ageRIFDataType.getIdentifier(), ageRIFDataType);
		dataTypeFromNames.put(ageRIFDataType.getName(), ageRIFDataType);
		
		DoubleRIFDataType doubleRIFDataType = DoubleRIFDataType.newInstance();
		dataTypeFromCodes.put(doubleRIFDataType.getIdentifier(), doubleRIFDataType);
		dataTypeFromNames.put(doubleRIFDataType.getName(), doubleRIFDataType);
		
		DateRIFDataType dateRIFDataType = DateRIFDataType.newInstance();
		dataTypeFromCodes.put(dateRIFDataType.getIdentifier(), dateRIFDataType);		
		dataTypeFromNames.put(dateRIFDataType.getName(), dateRIFDataType);		

		ICDCodeRIFDataType icdCodeRIFDataType = ICDCodeRIFDataType.newInstance();
		dataTypeFromCodes.put(icdCodeRIFDataType.getIdentifier(), icdCodeRIFDataType);		
		dataTypeFromNames.put(icdCodeRIFDataType.getName(), icdCodeRIFDataType);		

		QuintiliseRIFDataType quintiliseRIFDataType = QuintiliseRIFDataType.newInstance();
		dataTypeFromCodes.put(quintiliseRIFDataType.getIdentifier(), quintiliseRIFDataType);		
		dataTypeFromNames.put(quintiliseRIFDataType.getName(), quintiliseRIFDataType);		
				
		IntegerRIFDataType integerRIFDataType = IntegerRIFDataType.newInstance();
		dataTypeFromCodes.put(integerRIFDataType.getIdentifier(), integerRIFDataType);
		dataTypeFromNames.put(integerRIFDataType.getName(), integerRIFDataType);
		
		NHSNumberRIFDataType nhsNumberRIFDataType = NHSNumberRIFDataType.newInstance();
		dataTypeFromCodes.put(nhsNumberRIFDataType.getIdentifier(), nhsNumberRIFDataType);
		dataTypeFromNames.put(nhsNumberRIFDataType.getName(), nhsNumberRIFDataType);
				
		SexRIFDataType sexRIFDataType = SexRIFDataType.newInstance();
		dataTypeFromCodes.put(sexRIFDataType.getIdentifier(), sexRIFDataType);
		dataTypeFromNames.put(sexRIFDataType.getName(), sexRIFDataType);
		
		TextRIFDataType textRIFDataType = TextRIFDataType.newInstance();
		dataTypeFromCodes.put(textRIFDataType.getIdentifier(), textRIFDataType);
		dataTypeFromNames.put(textRIFDataType.getName(), textRIFDataType);
			
		UKPostalCodeRIFDataType ukPostalCodeRIFDataType
			= UKPostalCodeRIFDataType.newInstance();
		dataTypeFromCodes.put(ukPostalCodeRIFDataType.getIdentifier(), ukPostalCodeRIFDataType);
		dataTypeFromNames.put(ukPostalCodeRIFDataType.getName(), ukPostalCodeRIFDataType);
		
		YearRIFDataType yearRIFDataType = YearRIFDataType.newInstance();
		dataTypeFromCodes.put(yearRIFDataType.getIdentifier(), yearRIFDataType);
		dataTypeFromNames.put(yearRIFDataType.getName(), yearRIFDataType);		
	}

	public static RIFDataTypeFactory newInstance() {
		RIFDataTypeFactory rifDataTypeFactory = new RIFDataTypeFactory();
		return rifDataTypeFactory;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * takes any given instance of a rif data type, and creates a custom rif data type that has
	 * all the same attributes
	 * @param originalRIFDataType
	 * @return
	 */
	public static CustomRIFDataType createCopy(
		final AbstractRIFDataType originalRIFDataType) {
		
		String originalIdentifier = originalRIFDataType.getIdentifier();
		String originalName = originalRIFDataType.getName();
		String originalDescription = originalRIFDataType.getDescription();
		
		CustomRIFDataType customRIFDataType
			= CustomRIFDataType.newInstance(
				originalIdentifier, 
				originalName, 
				originalDescription);
		originalRIFDataType.copyAttributes(customRIFDataType);
		
		return customRIFDataType;
	}
	
	public ArrayList<RIFDataTypeInterface> getRegisteredDataTypes() {
		ArrayList<RIFDataTypeInterface> dataTypes
			= new ArrayList<RIFDataTypeInterface>();
		dataTypes.addAll(dataTypeFromCodes.values());
		
		return dataTypes;
	}
	
	public void registerDataType(final CustomRIFDataType customRIFDataType) 
		throws RIFServiceException {

		//Validate fields of data type
		
		
		String dataTypeIdentifier = customRIFDataType.getIdentifier();
				
		if (dataTypeFromCodes.containsKey(dataTypeIdentifier)) {
			//Error: Attempt to register a duplicate data type
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.duplicateValue",
					dataTypeIdentifier);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DUPLICATE_RIF_DATA_TYPE, 
					errorMessage);
			throw rifServiceException;
		}
		
		dataTypeFromCodes.put(customRIFDataType.getIdentifier(), customRIFDataType);
		dataTypeFromNames.put(customRIFDataType.getName(), customRIFDataType);	
	}
	
	public AbstractRIFDataType getDataTypeFromCode(final String dataTypeCode) {
		return dataTypeFromCodes.get(dataTypeCode);		
	}
	
	public AbstractRIFDataType getDataTypeFromName(final String dataTypeName) {
		return dataTypeFromNames.get(dataTypeName);		
	}
	
	public DateRIFDataType getDateType(final String dateFormat) {
		DateRIFDataType dateRIFDataType
			= DateRIFDataType.newInstance();
		
		ValidationRule validationRule
			= ValidationRule.newInstance();
		validationRule.setValidValue(dateFormat);
		dateRIFDataType.addValidationRule(validationRule);
		
		return dateRIFDataType;		
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


