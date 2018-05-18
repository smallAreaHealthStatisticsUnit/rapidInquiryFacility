package org.sahsu.rif.dataloader.concepts;

import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.util.FieldValidationUtility;

import java.text.Collator;

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

public class DatabaseConnectionsConfiguration {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private String databasePasswordFilePath;
	
	private String databaseDriverClassName;
	private DatabaseType databaseType;
	private String databaseDriverPrefix;
	private String databaseName;
	private String hostName;
	private String portName;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DatabaseConnectionsConfiguration() {
		databaseType = DatabaseType.POSTGRESQL;
		
		databaseDriverClassName = "org.postgresql.Driver";
		databaseDriverPrefix = "jdbc:postgresql";
		databaseName = "tmp_sahsu_db";
		portName = "5432";
		hostName = "localhost";
	}

	public static DatabaseConnectionsConfiguration newInstance() {
		DatabaseConnectionsConfiguration rifDatabaseConnectionParameters
			= new DatabaseConnectionsConfiguration();
		return rifDatabaseConnectionParameters;
	}
	
	public static DatabaseConnectionsConfiguration createDefaultPostgreSQL() {
		DatabaseConnectionsConfiguration configuration
			= new DatabaseConnectionsConfiguration();
		configuration.setDatabaseType(DatabaseType.POSTGRESQL);
		configuration.setDatabaseDriverClassName("org.postgresql.Driver");
		configuration.setDatabaseDriverPrefix("jdbc:postgresql");
		configuration.setDatabaseName("tmp_sahsu_db");
		configuration.setPortName("5432");
		configuration.setHostName("localhost");
		
		return configuration;
	}

	public static DatabaseConnectionsConfiguration createDefaultSQLServer() {
		DatabaseConnectionsConfiguration configuration
			= new DatabaseConnectionsConfiguration();
		configuration.setDatabaseType(DatabaseType.SQL_SERVER);
		configuration.setDatabaseDriverClassName(
			"com.microsoft.sqlserver.jdbc.SQLServerDriver");
		configuration.setDatabaseDriverPrefix("jdbc:sqlserver");
		configuration.setDatabaseName("tmp_sahsu_db");
		configuration.setPortName("1433");
		configuration.setHostName("KEVIN_GARWOOD\\SQLEXPRESS");

		return configuration;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String getDatabasePasswordFilePath() {
		return databasePasswordFilePath;
	}
	
	public void setDatabasePasswordFilePath(final String databasePasswordFilePath) {
		this.databasePasswordFilePath = databasePasswordFilePath;
	}
	
	public String getDatabaseDriverClassName() {
		return databaseDriverClassName;
	}

	public void setDatabaseDriverClassName(String databaseDriverClassName) {
		this.databaseDriverClassName = databaseDriverClassName;
	}
		
	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	public String getDatabaseDriverPrefix() {
		return databaseDriverPrefix;
	}
	
	public void setDatabaseDriverPrefix(String databseDriverPrefix) {
		this.databaseDriverPrefix = databseDriverPrefix;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	
	public String getDatabaseServerURL() {
		StringBuilder urlText = new StringBuilder();
		urlText.append(databaseDriverPrefix);
		urlText.append(":");
		urlText.append("//");
		urlText.append(hostName);
		urlText.append(":");
		urlText.append(portName);
		urlText.append("/");
		return urlText.toString();
	}
	
	
	public String getDatabaseURL() {
		
		StringBuilder urlText = new StringBuilder();
		if (databaseType == DatabaseType.POSTGRESQL) {

			urlText.append(databaseDriverPrefix);
			urlText.append(":");
			urlText.append("//");
			urlText.append(hostName);
			urlText.append(":");
			urlText.append(portName);
			urlText.append("/");
			urlText.append(databaseName);
		}
		else {
			urlText.append(databaseDriverPrefix);
			urlText.append(":");
			urlText.append("//");
			urlText.append(hostName);
			urlText.append(":");
			urlText.append(portName);
			urlText.append(";");
			urlText.append("databaseName=");
			urlText.append(databaseName);
		}
		
		return urlText.toString();
	}
	
	public boolean hasIdenticalContents(
		final DatabaseConnectionsConfiguration otherDatabaseConnectionsConfiguration) {
		
		Collator collator = Collator.getInstance();
		
		String otherDatabaseDriverClassName
			= otherDatabaseConnectionsConfiguration.getDatabaseDriverClassName();
		if (FieldValidationUtility.hasDifferentNullity(
			databaseDriverClassName, 
			otherDatabaseDriverClassName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (databaseDriverClassName != null) {
			//they must both be non-null
			if (collator.equals(
				databaseDriverClassName, 
				otherDatabaseDriverClassName) == false) {

				return false;
			}			
		}		
		
		DatabaseType otherDatabaseType
			= otherDatabaseConnectionsConfiguration.getDatabaseType();
		if (databaseType != otherDatabaseType) {
			return false;
		}

		String otherDatabaseDriverPrefix
			= otherDatabaseConnectionsConfiguration.getDatabaseDriverPrefix();
		if (FieldValidationUtility.hasDifferentNullity(
			databaseDriverPrefix, 
			otherDatabaseDriverPrefix)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (databaseDriverPrefix != null) {
			//they must both be non-null
			if (collator.equals(
				databaseDriverPrefix, 
				otherDatabaseDriverPrefix) == false) {

				return false;
			}			
		}		
		
		
		String otherDatabaseName
			= otherDatabaseConnectionsConfiguration.getDatabaseName();
		if (FieldValidationUtility.hasDifferentNullity(
			databaseName, 
			otherDatabaseName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (databaseName != null) {
			//they must both be non-null
			if (collator.equals(
				databaseName, 
				otherDatabaseName) == false) {

				return false;
			}			
		}		
				
		String otherHostName
			= otherDatabaseConnectionsConfiguration.getHostName();
		if (FieldValidationUtility.hasDifferentNullity(
			hostName, 
			otherHostName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (hostName != null) {
			//they must both be non-null
			if (collator.equals(
				hostName, 
				otherHostName) == false) {

				return false;
			}			
		}		
		
		String otherPortName
			= otherDatabaseConnectionsConfiguration.getPortName();
		if (FieldValidationUtility.hasDifferentNullity(
			portName, 
			otherPortName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (portName != null) {
			//they must both be non-null
			if (collator.equals(
				portName, 
				otherPortName) == false) {

				return false;
			}			
		}		
		
		return true;
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


