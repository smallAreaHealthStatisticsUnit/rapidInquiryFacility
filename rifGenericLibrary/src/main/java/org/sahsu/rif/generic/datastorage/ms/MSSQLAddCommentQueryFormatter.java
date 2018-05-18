package org.sahsu.rif.generic.datastorage.ms;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;

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

public class MSSQLAddCommentQueryFormatter extends AbstractSQLQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String tableName;
	private String fieldName;
	private String comment = "";
	
	// ==========================================
	// Section Construction
	// ==========================================

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}
	
	public void setTableFieldName(final String fieldName) {
		this.fieldName = fieldName;
	}
	
	public void setComment(final String comment) {
		this.comment = comment;
	}
	
	//use a user-defined function to simplify adding/updating comments
	@Override	
	public String generateQuery()  {
		
		
		if (fieldName != null ) {
			addQueryPhrase(0,"exec rif40.rif40_addorupdate_comment ?,?,?,?");
		} else {
			addQueryPhrase(0,"exec rif40.rif40_addorupdate_comment ?,?,null,?");
		}
		
		return super.generateQuery();

	}
	
	public void addCommentFields(PreparedStatement statement) throws SQLException {
		String schemaName = getDatabaseSchemaName();
		
		
		if (fieldName != null) {
			statement.setString(1,schemaName);
			statement.setString(2,tableName);
			statement.setString(3,fieldName);
			statement.setString(4,comment);
		} else {
			statement.setString(1,schemaName);
			statement.setString(2,tableName);
			statement.setString(3,comment);
		}
		
		
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
