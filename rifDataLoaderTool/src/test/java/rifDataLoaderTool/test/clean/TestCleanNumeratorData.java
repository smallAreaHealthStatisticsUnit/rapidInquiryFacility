package rifDataLoaderTool.test.clean;


import junit.framework.TestCase;

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

public final class TestCleanNumeratorData 
	extends TestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String createNumeratorTableQuery;
	private String deleteNumeratorTableQuery;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public TestCleanNumeratorData() {
		//createNumeratorTable
		
		
	}
	


	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	private void populate() {
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE uncleaned_numerator_table (");
		query.append("row_number INTEGER,");
		query.append("data_source VARCHAR(30),");
		query.append("nhs_number VARCHAR(30),");
		query.append("birth_date VARCHAR(30),");
		query.append("postal_code VARCHAR(30),");
		query.append("age VARCHAR(30),");
		query.append("sex VARCHAR(30),");
		query.append("level1 VARCHAR(30),");
		query.append("level2 VARCHAR(30),");
		query.append("level3 VARCHAR(30),");
		query.append("level4 VARCHAR(30),");
		query.append("icd_1 VARCHAR(30),");
		query.append("icd_2 VARCHAR(30),");
		query.append("opcs_code_1 VARCHAR(30),");
		query.append("total VARCHAR(30));");

		//return query.toString();
	}
		
	private String getDestroyTableQuery() {
		StringBuilder query = new StringBuilder();
		query.append("DROP TABLE ");
		query.append("uncleaned_numerator_table");
		query.append(";");
		
		return query.toString();
	}
	
	public void test1() {
		
		
		
		
		RawTableGenerator rawTableGenerator = new RawTableGenerator();
		rawTableGenerator.addFieldName("age");
		
		
		
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


