package rifDataLoaderTool.businessConceptLayer;

import java.util.HashMap;

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

public class RIFDataTypeFactory {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private HashMap<String, AbstractRIFDataType> dataTypeFromName;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFDataTypeFactory() {
		
		dataTypeFromName = new HashMap<String, AbstractRIFDataType>();

		
		AgeRIFDataType ageRIFDataType = AgeRIFDataType.newInstance();
		dataTypeFromName.put(ageRIFDataType.getName(), ageRIFDataType);
		
		DoubleRIFDataType doubleRIFDataType = DoubleRIFDataType.newInstance();
		dataTypeFromName.put(doubleRIFDataType.getName(), doubleRIFDataType);
		
		DateRIFDataType dateRIFDataType = DateRIFDataType.newInstance();
		dataTypeFromName.put(dateRIFDataType.getName(), dateRIFDataType);		
		
		ICDCodeRIFDataType icdCodeRIFDataType = ICDCodeRIFDataType.newInstance();
		dataTypeFromName.put(icdCodeRIFDataType.getName(), icdCodeRIFDataType);		
		
		IntegerRIFDataType integerRIFDataType = IntegerRIFDataType.newInstance();
		dataTypeFromName.put(integerRIFDataType.getName(), integerRIFDataType);
		
		NHSNumberRIFDataType nhsNumberRIFDataType = NHSNumberRIFDataType.newInstance();
		dataTypeFromName.put(nhsNumberRIFDataType.getName(), nhsNumberRIFDataType);
		
		SexRIFDataType sexRIFDataType = SexRIFDataType.newInstance();
		dataTypeFromName.put(sexRIFDataType.getName(), sexRIFDataType);
		
		TextRIFDataType textRIFDataType = TextRIFDataType.newInstance();
		dataTypeFromName.put(textRIFDataType.getName(), textRIFDataType);
		
		UKPostalCodeRIFDataType ukPostalCodeRIFDataType
			= UKPostalCodeRIFDataType.newInstance();
		dataTypeFromName.put(ukPostalCodeRIFDataType.getName(), ukPostalCodeRIFDataType);
		
		YearRIFDataType yearRIFDataType = YearRIFDataType.newInstance();
		dataTypeFromName.put(yearRIFDataType.getName(), yearRIFDataType);
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public AbstractRIFDataType getDataType(final String dataTypeName) {
		return dataTypeFromName.get(dataTypeName);		
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


