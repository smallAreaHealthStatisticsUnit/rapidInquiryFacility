package rifDataLoaderTool.businessConceptLayer.rifDataTypes;

import rifDataLoaderTool.businessConceptLayer.RIFFieldCleaningPolicy;
import rifDataLoaderTool.businessConceptLayer.RIFFieldValidationPolicy;
import rifDataLoaderTool.system.RIFDataLoaderMessages;
/**
 * a data type for Integers.
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class QuintiliseRIFDataType 
	extends AbstractRIFDataType {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	private QuintiliseRIFDataType(
		final String identifier,
		final String name,
		final String description) {

		super(
			identifier,
			name, 
			description);
		
		//String validationRegularExpression = "^(\\d+)";
		//addValidationExpression(validationRegularExpression);
		setFieldCleaningPolicy(RIFFieldCleaningPolicy.SQL_FRAGMENT);
		setFieldValidationPolicy(RIFFieldValidationPolicy.NO_VALIDATION);
	}

	public static QuintiliseRIFDataType newInstance() {

		String name
			= RIFDataLoaderMessages.getMessage("rifDataType.quintilise.label");
		String description
			= RIFDataLoaderMessages.getMessage("rifDataType.quintilise.description");
		QuintiliseRIFDataType integerRIFDataType
			= new QuintiliseRIFDataType(
				"rif_quintilise",
				name, 
				description);
		
		return integerRIFDataType;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	public QuintiliseRIFDataType createCopy() {
		QuintiliseRIFDataType cloneIntegerRIFDataType = newInstance();
		copyAttributes(cloneIntegerRIFDataType);
		return cloneIntegerRIFDataType;
	}	
	
}


