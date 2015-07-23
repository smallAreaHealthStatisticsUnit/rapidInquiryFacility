package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

/**
 * Describes the way a field found in an initial load table will be validated.  
 * There are three options:
 * <ul>
 * <li>
 * no validation
 * </li>
 * <li>
 * validation rules
 * </li>
 * <li>
 * validation function
 * </li>
 * </ul>
 *
 * In the RIF Data Loader Tool, once any cleaning rules have been applied, the field values are migrated to
 * a validated values table.  If no validation is used, then the cleaned value is carried forward as a valid 
 * value.  If one or more validation rules is used, then the SQL code generators create a <code>CASE</code>
 * statement which returns true when the first regular expression shows a match or false if no regular expressions
 * result in a match.  If the validation policy involves a function, then the field value is passed to some
 * function which returns true or false.
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

public enum RIFFieldValidationPolicy {

	VALIDATION_FUNCTION("rifFieldValidationPolicy.validationFunction.label"),
	VALIDATION_RULES("rifFieldValidationPolicy.validationRules.label"),
	SQL_FRAGMENT("rifFieldValidationPolicy.validationRules.label"),
	NO_VALIDATION("rifFieldValidationPolicy.noValidation.label");
	
	private String propertyName;
	
	RIFFieldValidationPolicy(
		final String propertyName) {
		
		this.propertyName = propertyName;
	}
	
	public String getName() {
		return RIFDataLoaderToolMessages.getMessage(propertyName);
	}
	
}


