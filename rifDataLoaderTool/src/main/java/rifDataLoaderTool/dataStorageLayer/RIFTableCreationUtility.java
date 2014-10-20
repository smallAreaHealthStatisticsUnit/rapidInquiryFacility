package rifDataLoaderTool.dataStorageLayer;

import java.sql.*;

import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.SQLGeneralQueryFormatter;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;


/**
 *
 * This will be a very short lived class. Its purpose is mainly to create some
 * tables that the RIF Data Loader Tool may need.  If the tables continue to be needed
 * then the code used to create tables will be migrated into the main RIF setup scripts.
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

public class RIFTableCreationUtility {

	public static void main(String arguments[]) {
		RIFTableCreationUtility utility = new RIFTableCreationUtility();
		utility.createTables();
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public RIFTableCreationUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void createTables() {
		
		
		SQLConnectionManager sqlConnectionManager
			= new SQLConnectionManager();
		
		User user = User.newInstance("kgarwood", "blah");
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "DROP TABLE IF EXISTS rif_audit_table;");
		queryFormatter.addQueryPhrase(0, "CREATE TABLE rif_audit_table (");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryLine(1, "data_source_id INT NOT NULL,");
		queryFormatter.addQueryLine(1, "row_number INT NOT NULL,");
		queryFormatter.addQueryLine(1, "event_type VARCHAR(30) NOT NULL,");
		queryFormatter.addQueryLine(1, "field_name VARCHAR(30),");
		queryFormatter.addQueryLine(1, "time_stamp TIMESTAMP DEFAULT current_timestamp);");
		queryFormatter.addQueryLine(0, "DROP TABLE IF EXISTS data_source_registry;");
		queryFormatter.addQueryLine(0, "CREATE TABLE data_source_registry (");
		queryFormatter.addQueryPhrase(1, "core_table_name VARCHAR(");
		queryFormatter.addQueryPhrase(String.valueOf(RIFDataLoaderStartupOptions.MAXIMUM_TABLE_NAME_WIDTH));
		queryFormatter.addQueryPhrase("),");
		queryFormatter.finishLine();
		queryFormatter.addQueryLine(1, "derived_from_existing_table BOOLEAN,");
		queryFormatter.addQueryLine(1, "source_name VARCHAR(");
		queryFormatter.addQueryPhrase(String.valueOf(RIFDataLoaderStartupOptions.MAXIMUM_DESCRIPTION_FIELD_WIDTH));
		queryFormatter.addQueryPhrase("),");
		queryFormatter.finishLine();
		queryFormatter.addQueryPhrase(1, "user_id VARCHAR(");
		queryFormatter.addQueryPhrase(String.valueOf(RIFDataLoaderStartupOptions.MAXIMUM_USER_ID_WIDTH));
		queryFormatter.addQueryPhrase("));");
		queryFormatter.finishLine();
				
		System.out.println(queryFormatter.generateQuery());
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.executeUpdate();
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(connection);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
		finally {

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


