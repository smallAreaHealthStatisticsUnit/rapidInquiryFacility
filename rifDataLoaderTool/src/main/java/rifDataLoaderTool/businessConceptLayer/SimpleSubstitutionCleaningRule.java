package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderMessages;

import java.text.Collator;



/**
 *
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

public class SimpleSubstitutionCleaningRule 
	implements CleaningRule {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String whenStatement;
	
	private String originalTableName;
	private String originalFieldName;
	private String originalValue;
	private String cleanedValue;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public SimpleSubstitutionCleaningRule() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void initialiseRule(
		final String originalTableName,
		final String originalFieldName,
		final String originalValue, 
		final String cleanedValue) {
		
		this.originalTableName = originalTableName;
		this.originalFieldName = originalFieldName;

		
		this.originalValue = originalValue;
		this.cleanedValue = cleanedValue;
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("WHEN");
		buffer.append(" ");
		buffer.append(originalTableName);
		buffer.append(".");
		buffer.append(originalFieldName);
		buffer.append("=");
		buffer.append(originalValue);
		buffer.append(" ");
		buffer.append("THEN RETURN ");
		buffer.append(cleanedValue);
		
		whenStatement = buffer.toString();
	}
	
	public String getWhenStatement() {
		return whenStatement;
	}
	
	public boolean isApplicable(String candidateValue) {
		Collator collator = RIFDataLoaderMessages.getCollator();
		if (collator.equals(originalValue, candidateValue)) {
			return true;
		}
		
		return false;
		
	}
	
	public String getName() {
		String name
			= RIFDataLoaderMessages.getMessage("simpleSubstitutionCleaningRule.name");
		return name;
		
	}
	
	public String getDescription() {
		String description
			= RIFDataLoaderMessages.getMessage("simpleSubstitutionCleaningRule.description");
		return description;
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


