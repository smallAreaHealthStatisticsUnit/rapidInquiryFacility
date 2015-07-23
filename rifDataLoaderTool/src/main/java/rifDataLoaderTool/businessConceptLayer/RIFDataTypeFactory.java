package rifDataLoaderTool.businessConceptLayer;


import rifDataLoaderTool.businessConceptLayer.rifDataTypes.*;

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
	private HashMap<String, AbstractRIFDataType> dataTypeFromName;
	private ArrayList<String> dataTypeCodes;
	private ArrayList<String> dataTypeNames;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private RIFDataTypeFactory() {
		
		dataTypeFromName = new HashMap<String, AbstractRIFDataType>();
		dataTypeCodes = new ArrayList<String>();
		dataTypeNames = new ArrayList<String>();
		
		AgeRIFDataType ageRIFDataType = AgeRIFDataType.newInstance();
		dataTypeFromName.put(ageRIFDataType.getIdentifier(), ageRIFDataType);
		dataTypeCodes.add(ageRIFDataType.getIdentifier());
		dataTypeNames.add(ageRIFDataType.getName());
		
		DoubleRIFDataType doubleRIFDataType = DoubleRIFDataType.newInstance();
		dataTypeFromName.put(doubleRIFDataType.getIdentifier(), doubleRIFDataType);
		dataTypeCodes.add(doubleRIFDataType.getIdentifier());
		dataTypeNames.add(doubleRIFDataType.getName());
		
		DateRIFDataType dateRIFDataType = DateRIFDataType.newInstance();
		dataTypeFromName.put(dateRIFDataType.getIdentifier(), dateRIFDataType);		
		dataTypeCodes.add(dateRIFDataType.getIdentifier());
		dataTypeNames.add(dateRIFDataType.getName());
				
		ICDCodeRIFDataType icdCodeRIFDataType = ICDCodeRIFDataType.newInstance();
		dataTypeFromName.put(icdCodeRIFDataType.getIdentifier(), icdCodeRIFDataType);		
		dataTypeCodes.add(icdCodeRIFDataType.getIdentifier());

		QuintiliseRIFDataType quintiliseRIFDataType = QuintiliseRIFDataType.newInstance();
		dataTypeFromName.put(quintiliseRIFDataType.getIdentifier(), quintiliseRIFDataType);		
		dataTypeCodes.add(quintiliseRIFDataType.getIdentifier());		
		dataTypeNames.add(quintiliseRIFDataType.getName());
				
		IntegerRIFDataType integerRIFDataType = IntegerRIFDataType.newInstance();
		dataTypeFromName.put(integerRIFDataType.getIdentifier(), integerRIFDataType);
		dataTypeCodes.add(integerRIFDataType.getIdentifier());
		dataTypeNames.add(integerRIFDataType.getName());
		
		NHSNumberRIFDataType nhsNumberRIFDataType = NHSNumberRIFDataType.newInstance();
		dataTypeFromName.put(nhsNumberRIFDataType.getIdentifier(), nhsNumberRIFDataType);
		dataTypeCodes.add(nhsNumberRIFDataType.getIdentifier());
		dataTypeNames.add(nhsNumberRIFDataType.getName());
		
		SexRIFDataType sexRIFDataType = SexRIFDataType.newInstance();
		dataTypeFromName.put(sexRIFDataType.getIdentifier(), sexRIFDataType);
		dataTypeCodes.add(sexRIFDataType.getIdentifier());
		dataTypeNames.add(sexRIFDataType.getName());
		
		TextRIFDataType textRIFDataType = TextRIFDataType.newInstance();
		dataTypeFromName.put(textRIFDataType.getIdentifier(), textRIFDataType);
		dataTypeCodes.add(textRIFDataType.getIdentifier());
		dataTypeNames.add(textRIFDataType.getName());
		
		UKPostalCodeRIFDataType ukPostalCodeRIFDataType
			= UKPostalCodeRIFDataType.newInstance();
		dataTypeFromName.put(ukPostalCodeRIFDataType.getIdentifier(), ukPostalCodeRIFDataType);
		dataTypeCodes.add(ukPostalCodeRIFDataType.getIdentifier());
		dataTypeNames.add(ukPostalCodeRIFDataType.getName());
		
		YearRIFDataType yearRIFDataType = YearRIFDataType.newInstance();
		dataTypeFromName.put(yearRIFDataType.getIdentifier(), yearRIFDataType);
		dataTypeCodes.add(yearRIFDataType.getIdentifier());
		dataTypeNames.add(yearRIFDataType.getName());
		
	}

	public static RIFDataTypeFactory newInstance() {
		RIFDataTypeFactory rifDataTypeFactory = new RIFDataTypeFactory();
		return rifDataTypeFactory;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public AbstractRIFDataType getDataType(final String dataTypeName) {
		return dataTypeFromName.get(dataTypeName);		
	}
	
	public DateRIFDataType getDateType(final String dateFormat) {
		DateRIFDataType dateRIFDataType
			= DateRIFDataType.newInstance();
		dateRIFDataType.addValidationExpression(dateFormat);
		
		return dateRIFDataType;		
	}
	
	public String[] getDataTypeCodes() {
		return dataTypeCodes.toArray(new String[0]);
	}
	
	public String[] getDataTypeNames() {
		return dataTypeNames.toArray(new String[0]);
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


