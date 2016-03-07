package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

/**
 * A processing flag that tells SQL code generation classes which kind of cleaning 
 * is associated with the field:
 * <ul>
 * <li>
 * no cleaning
 * </li>
 * <li>
 * cleaning rules
 * </li>
 * <li>
 * a cleaning function
 * </li>
 * </ul>
 * 
 * <p>
 * Each of these options corresponds to a different SQL construction for the field.  In the case of
 * no cleaning, the field value from the load table is returned for the cleaned field unchanged.
 * When cleaning rules are applied, the SQL code generators create a <code>CASE</code> statement where each
 * cleaning rule forms a <code>WHEN</code> statement that includes search and replace criteria.  When
 * the cleaning policy is a cleaning function, then the field value and additional parameter values
 * may be passed to the function.
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

public enum RIFFieldCleaningPolicy {
	CLEANING_FUNCTION(
		"cleaning_function", 
		"rifFieldCleaningPolicy.cleaningFunction.label"),
	CLEANING_RULES(
		"cleaning_rules",
		"rifFieldCleaningPolicy.cleaningRules.label"),
	NO_CLEANING(
		"no_cleaning", 
		"rifFieldCleaningPolicy.noCleaning.label");
	
	private String tagName;
	private String propertyName;
	
	RIFFieldCleaningPolicy(
		final String tagName,
		final String propertyName) {
		
		this.tagName = tagName;
		this.propertyName = propertyName;
	}
	
	public String getName() {
		return RIFDataLoaderToolMessages.getMessage(propertyName);
	}
	
	public String getTagName() {
		return tagName;
	}
	
}


