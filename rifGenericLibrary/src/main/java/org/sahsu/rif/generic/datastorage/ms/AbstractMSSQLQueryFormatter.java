package org.sahsu.rif.generic.datastorage.ms;

import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;

/**
 *
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class AbstractMSSQLQueryFormatter
		extends AbstractSQLQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	boolean useGoCommand = false;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractMSSQLQueryFormatter() {
		init(true);
	}
	
	public AbstractMSSQLQueryFormatter(final boolean useGoCommand) {
		init(useGoCommand);
	}
	
	private void init(final boolean useGoCommand) { // DO NOT USE GO WITH JDBC!!!!!!
		this.useGoCommand = useGoCommand;
		setEndWithSemiColon(false);		
		//KLG_SCHEMA
		//setDatabaseSchemaName("dbo");
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String generateQuery() {
		StringBuilder result = new StringBuilder();		
		result.append(getQueryBuilder().toString());
		
		// I think Kev needed to put a check in here regarding semicolons
		if (endWithSemiColon()){
			result.append(";\n");
		}
		else {
			result.append("\n");
	    }
//		if (useGoCommand) { // DO NOT USE GO WITH JDBC!!!!!!
//			result.append("GO");
//		}
//		else {
			result.append("\n");			
//		}
		result.append("\n");
		
		return result.toString();
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
