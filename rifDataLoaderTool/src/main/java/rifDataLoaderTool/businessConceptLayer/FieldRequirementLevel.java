package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import rifGenericLibrary.system.RIFGenericLibraryMessages;

import java.text.Collator;


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

public enum FieldRequirementLevel {
	REQUIRED_BY_RIF(
		"required_by_rif",
		"fieldRequirementLevel.requiredByRIF"),		
	EXTRA_FIELD(
		"extra_field",
		"fieldRequirementLevel.extraField"),
	IGNORE_FIELD(
		"ignore_field",
		"fieldRequirementLevel.ignoreField");
	
	private String code;
	private String propertyName;
	
	private FieldRequirementLevel(
		final String code,
		final String propertyName) {
		
		this.code = code;
		this.propertyName = propertyName;	
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		String name
			= RIFDataLoaderToolMessages.getMessage(propertyName);
		return name;
	}	
	
	public static String[] getNames() {
		String[] names = new String[3];
		names[0] = REQUIRED_BY_RIF.getName();
		names[1] = EXTRA_FIELD.getName();
		names[2] = IGNORE_FIELD.getName();
		
		return names;
		
	}
	
	
	public static FieldRequirementLevel getValueFromCode(
		final String code) {
		
		Collator collator = RIFGenericLibraryMessages.getCollator();
		if (collator.equals(code, REQUIRED_BY_RIF.getCode()) == true) {
			return REQUIRED_BY_RIF;
		}
		else if (collator.equals(code, EXTRA_FIELD.getCode()) == true) {
			return EXTRA_FIELD;
		}
		else if (collator.equals(code, IGNORE_FIELD.getCode()) == true) {
			return IGNORE_FIELD;
		}
		else {
			assert false;
			return null;
		}		
	}	

	public static FieldRequirementLevel getValueFromName(
		final String name) {
			
		Collator collator = RIFGenericLibraryMessages.getCollator();
		if (collator.equals(name, REQUIRED_BY_RIF.getName()) == true) {
			return REQUIRED_BY_RIF;
		}
		else if (collator.equals(name, EXTRA_FIELD.getName()) == true) {
			return EXTRA_FIELD;
		}
		else if (collator.equals(name, IGNORE_FIELD.getName()) == true) {
			return IGNORE_FIELD;
		}
		else {
			assert false;
			return null;
		}		
	}	
	
	
}


