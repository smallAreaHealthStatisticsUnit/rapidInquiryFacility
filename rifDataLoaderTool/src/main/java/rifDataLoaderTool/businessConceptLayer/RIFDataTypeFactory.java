package rifDataLoaderTool.businessConceptLayer;


import rifDataLoaderTool.businessConceptLayer.rifDataTypes.AbstractRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.AgeRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.DateRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.DoubleRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.ICDCodeRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.IntegerRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.NHSNumberRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.SexRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.TextRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.UKPostalCodeRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.YearRIFDataType;

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
	
	// ==========================================
	// Section Construction
	// ==========================================

	private RIFDataTypeFactory() {
		
		dataTypeFromName = new HashMap<String, AbstractRIFDataType>();
		dataTypeCodes = new ArrayList<String>();
		
		
		AgeRIFDataType ageRIFDataType = AgeRIFDataType.newInstance();
		dataTypeFromName.put(ageRIFDataType.getIdentifier(), ageRIFDataType);
		dataTypeCodes.add(ageRIFDataType.getIdentifier());
		
		DoubleRIFDataType doubleRIFDataType = DoubleRIFDataType.newInstance();
		dataTypeFromName.put(doubleRIFDataType.getIdentifier(), doubleRIFDataType);
		dataTypeCodes.add(doubleRIFDataType.getIdentifier());
		
		DateRIFDataType dateRIFDataType = DateRIFDataType.newInstance();
		dataTypeFromName.put(dateRIFDataType.getIdentifier(), dateRIFDataType);		
		dataTypeCodes.add(dateRIFDataType.getIdentifier());
				
		ICDCodeRIFDataType icdCodeRIFDataType = ICDCodeRIFDataType.newInstance();
		dataTypeFromName.put(icdCodeRIFDataType.getIdentifier(), icdCodeRIFDataType);		
		dataTypeCodes.add(icdCodeRIFDataType.getIdentifier());
				
		IntegerRIFDataType integerRIFDataType = IntegerRIFDataType.newInstance();
		dataTypeFromName.put(integerRIFDataType.getIdentifier(), integerRIFDataType);
		dataTypeCodes.add(integerRIFDataType.getIdentifier());
		
		NHSNumberRIFDataType nhsNumberRIFDataType = NHSNumberRIFDataType.newInstance();
		dataTypeFromName.put(nhsNumberRIFDataType.getIdentifier(), nhsNumberRIFDataType);
		dataTypeCodes.add(nhsNumberRIFDataType.getIdentifier());
		
		SexRIFDataType sexRIFDataType = SexRIFDataType.newInstance();
		dataTypeFromName.put(sexRIFDataType.getIdentifier(), sexRIFDataType);
		dataTypeCodes.add(sexRIFDataType.getIdentifier());
		
		TextRIFDataType textRIFDataType = TextRIFDataType.newInstance();
		dataTypeFromName.put(textRIFDataType.getIdentifier(), textRIFDataType);
		dataTypeCodes.add(textRIFDataType.getIdentifier());
		
		UKPostalCodeRIFDataType ukPostalCodeRIFDataType
			= UKPostalCodeRIFDataType.newInstance();
		dataTypeFromName.put(ukPostalCodeRIFDataType.getIdentifier(), ukPostalCodeRIFDataType);
		dataTypeCodes.add(ukPostalCodeRIFDataType.getIdentifier());
		
		YearRIFDataType yearRIFDataType = YearRIFDataType.newInstance();
		dataTypeFromName.put(yearRIFDataType.getIdentifier(), yearRIFDataType);
		dataTypeCodes.add(yearRIFDataType.getIdentifier());
		
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
	
	public String[] getDataTypeNames() {
		return dataTypeCodes.toArray(new String[0]);
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


