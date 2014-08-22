package rifDataLoaderTool.dataStorageLayer;


import rifDataLoaderTool.businessConceptLayer.CleaningRule;


import java.util.Hashtable;
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

public class TableCleaningManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String originalTableName;
	private String cleanedTableName;
	
	private Hashtable<String, String> cleanedFieldNameFromOriginalFieldName;
	private Hashtable<String, String> descriptionFromOriginalFieldName;
	private Hashtable<String, TableFieldCleaningManager> cleaningManagerFromOriginalFieldName;
	// ==========================================
	// Section Construction
	// ==========================================

	public TableCleaningManager() {
		cleanedFieldNameFromOriginalFieldName
			= new Hashtable<String, String>();
		descriptionFromOriginalFieldName
			= new Hashtable<String, String>();
		cleaningManagerFromOriginalFieldName 
			= new Hashtable<String, TableFieldCleaningManager>();		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void registerOriginalFieldName(
		final String originalFieldName,
		final String originalFieldDescription) {
		
		//by default, use the same name of the new field as the old one
		cleanedFieldNameFromOriginalFieldName.put(originalFieldName, originalFieldName);
		descriptionFromOriginalFieldName.put(originalFieldName, originalFieldDescription);
		TableFieldCleaningManager tableFieldCleaningManager
			= new TableFieldCleaningManager();
		tableFieldCleaningManager.initialise(
			originalTableName, 
			originalFieldName, 
			originalFieldName);
		cleaningManagerFromOriginalFieldName.put(originalFieldName, tableFieldCleaningManager);
	}
	
	public void associateCleanedFieldNameWithOriginalFieldName(
		String originalTableFieldName,
		String cleanedTableFieldName) {
		
		cleanedFieldNameFromOriginalFieldName.put(
			originalTableFieldName, 
			cleanedTableFieldName);
		
		TableFieldCleaningManager tableFieldCleaningManager
			= cleaningManagerFromOriginalFieldName.get(originalTableFieldName);
		tableFieldCleaningManager.initialise(
			originalTableName, 
			originalTableFieldName, 
			cleanedTableFieldName);
	}
	
	public void addCleaningRule(
		final String originalFieldName,
		final CleaningRule cleaningRule) {
		
		//assume field exists
		TableFieldCleaningManager tableFieldCleaningManager
			= cleaningManagerFromOriginalFieldName.get(originalFieldName);
		tableFieldCleaningManager.addCleaningRule(cleaningRule);
	}
	
	public void addSimpleSubsitutionCleaningRule(
		final String originalFieldName,
		final String originalValue,
		final String cleanedValue) {
		
		TableFieldCleaningManager tableFieldCleaningManager
			= cleaningManagerFromOriginalFieldName.get(originalFieldName);		
	}
	
	public void setOriginalAndCleanedTables(
		final String originalTableName,
		final String cleanedTableName) {
		
		this.originalTableName = originalTableName;
		this.cleanedTableName = cleanedTableName;	
	}
	
	public String generateQuery() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("CREATE TABLE ");
		buffer.append(originalTableName);
		buffer.append(" AS ");
		
		buffer.append("SELECT ");
				
		ArrayList<String> originalTableFieldNames = new ArrayList<String>();
		originalTableFieldNames.addAll(cleanedFieldNameFromOriginalFieldName.keySet());
		for (String originalTableFieldName : originalTableFieldNames) {
			TableFieldCleaningManager tableFieldCleaningManager
				= cleaningManagerFromOriginalFieldName.get(originalTableFieldName);		
			buffer.append(tableFieldCleaningManager.generateCaseStatement());
		}
		buffer.append(";");
		
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


