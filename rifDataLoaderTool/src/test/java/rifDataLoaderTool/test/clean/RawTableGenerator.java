package rifDataLoaderTool.test.clean;

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

public class RawTableGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String tableName;
	
	private ArrayList<String> fieldNames;
	private Integer textFieldSize;
	
	private String typeDeclaration;
	// ==========================================
	// Section Construction
	// ==========================================

	public RawTableGenerator() {
		fieldNames = new ArrayList<String>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setTextFieldSize(
		final Integer textFieldSize) {

		StringBuilder buffer = new StringBuilder();
		buffer.append("VARCHAR(");
		buffer.append(String.valueOf(textFieldSize));
		buffer.append(")");
		
		typeDeclaration = buffer.toString();
		
	}
	
	public void setTableName(
		final String tableName) {
		
		this.tableName = tableName;
	}
	
	public void addFieldName(
		final String fieldName) {
		
		fieldNames.add(fieldName);
	}
	
	public String generateCreateTableQuery() {
		
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE ");
		query.append(tableName);
		query.append(" (");
		query.append("row_number INTEGER,");
		query.append("data_source VARCHAR(30)");
		for (String fieldName : fieldNames) {
			query.append(",");
			query.append(fieldName);
			query.append(" ");
			query.append(typeDeclaration);
			query.append(")");
		}
		query.append(");");
	
		return query.toString();
	}
	
	public String generateDropTableQuery() {

		StringBuilder query = new StringBuilder();
		
		query.append("DROP TABLE ");
		query.append(tableName);
		query.append(";");
		
		return query.toString();
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


