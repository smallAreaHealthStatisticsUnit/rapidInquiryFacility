package rifDataLoaderTool.businessConceptLayer.rifDataTypes;

import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;
import rifDataLoaderTool.businessConceptLayer.RIFFieldCleaningPolicy;
import rifDataLoaderTool.businessConceptLayer.RIFFieldValidationPolicy;
import rifDataLoaderTool.system.RIFDataLoaderMessages;

/**
 * A data type for age.  In future, the type may need to be modfied so that it can 
 * accept a different upper age limit.  Some researchers may want the RIF to check
 * unrealistic age values but others may not.  In particular, some unreasonable ages
 * may in fact be special codes to indicate "not specified", "not given" or other
 * codes that may explain why a valid value does not exist.
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

public final class AgeRIFDataType 
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

	private AgeRIFDataType(
		final String identifier,
		final String name,
		final String description) {

		super(identifier, 
			name, 
			description);

		/**
		 * currently accepts the following ranges:
		 * <ul>
		 * <li>0 to 9 </li>
		 * <li>10 to 99</li>
		 * <li>100 to 119</li>
		 * </ul>
		 */
		addValidationExpression("^[0-9]{1,2}$|1[0-1][0-9]$");
		setFieldValidationPolicy(RIFFieldValidationPolicy.VALIDATION_RULES);
		setFieldCleaningPolicy(RIFFieldCleaningPolicy.NO_CLEANING);
	}

	public static AgeRIFDataType newInstance() {

		String identifier = "rif_age";
		String name
			= RIFDataLoaderMessages.getMessage("rifDataType.age.label");
		String description
			= RIFDataLoaderMessages.getMessage("rifDataType.age.description");

		AgeRIFDataType ageRIFDataType
			= new AgeRIFDataType(
				identifier,	
				name, 
				description);
		
		return ageRIFDataType;
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
	
	public RIFDataTypeInterface createCopy() {
		AgeRIFDataType cloneAgeRIFDataType = AgeRIFDataType.newInstance();
		copyAttributes(cloneAgeRIFDataType);
		return cloneAgeRIFDataType;
	}
	
}


