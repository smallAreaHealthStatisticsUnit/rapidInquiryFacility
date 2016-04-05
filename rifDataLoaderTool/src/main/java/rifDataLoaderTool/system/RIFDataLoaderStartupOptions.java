package rifDataLoaderTool.system;

import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;

/**
 * Contain a set of parameter values that will be made available to the RIF Data Loader tool
 * when it starts up.
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

public final class RIFDataLoaderStartupOptions {

	// ==========================================
	// Section Constants
	// ==========================================

	public static final int MAXIMUM_TABLE_NAME_WIDTH = 30;
	public static final int MAXIMUM_DESCRIPTION_FIELD_WIDTH=200;
	public static final int MAXIMUM_USER_ID_WIDTH = 20;
	
	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	private DatabaseType databaseType;
	private String jdbcDriverPrefix;
	private String databaseName;
	private String port;
	private String host;
	
	//GUI Components
	
	// ==========================================
	// Section Construction
	// ==========================================
	
	
	public RIFDataLoaderStartupOptions() {
		databaseType = DatabaseType.POSTGRESQL;
		
		jdbcDriverPrefix = "jdbc:postgresql";
		databaseName = "tmp_sahsu_db";
		port = "5432";
		host = "localhost";
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setDatabaseType(
		final DatabaseType databaseType) {
		
		this.databaseType = databaseType;
	}
	
	public DatabaseType getDatabaseType() {
		
		return databaseType;
	}
	
	public int getDataLoaderTextColumnSize() {
		return 30;
	}
	
	public String getJDBCDriverPrefix() {
		return jdbcDriverPrefix;
	}

	public void setJDBCDriverPrefix(String jdbcDriverPrefix) {
		this.jdbcDriverPrefix = jdbcDriverPrefix;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public RIFDatabaseProperties getRIFDatabaseProperties() {
		
		RIFDatabaseProperties rifDatabaseProperties
			= RIFDatabaseProperties.newInstance(
				databaseType, 
				true);
		
		return rifDatabaseProperties;
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


