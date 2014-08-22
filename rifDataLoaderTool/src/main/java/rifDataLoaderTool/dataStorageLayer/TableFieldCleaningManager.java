package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.CleaningRule;
import rifDataLoaderTool.businessConceptLayer.SimpleSubstitutionCleaningRule;


import java.util.ArrayList;

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

public class TableFieldCleaningManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<CleaningRule> fieldCleaningRules;
	private String originalTableName;
	private String originalTableFieldName;
	private String cleanedTableFieldName;
	
	// ==========================================
	// Section Construction
	// ==========================================
	
	public TableFieldCleaningManager() {
		fieldCleaningRules = new ArrayList<CleaningRule>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void initialise(
		final String originalTableName,
		final String originalFieldName,
		final String cleanedTableFieldName) {
		
		this.originalTableName = originalTableName;
		this.cleanedTableFieldName = cleanedTableFieldName;
	}
	
	public void addSimpleSubstitutionRule(
		String originalValue,
		String cleanedValue) {
		
		SimpleSubstitutionCleaningRule simpleSubstitutionCleaningRule
			= new SimpleSubstitutionCleaningRule();
		simpleSubstitutionCleaningRule.initialiseRule(
			originalTableName, 
			cleanedTableFieldName, 
			originalValue, 
			cleanedValue);
		fieldCleaningRules.add(simpleSubstitutionCleaningRule);
		
	}
	
	
	public void addCleaningRule(
		final CleaningRule cleaningRule) {
		
		fieldCleaningRules.add(cleaningRule);
	}
	
	public String generateCaseStatement() {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("CASE ");
		for (CleaningRule fieldCleaningRule : fieldCleaningRules) {
			buffer.append(fieldCleaningRule.getWhenStatement());
		}
		
		buffer.append("END AS");
		buffer.append(cleanedTableFieldName);
		
		return buffer.toString();		
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


