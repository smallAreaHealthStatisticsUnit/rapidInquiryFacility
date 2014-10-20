package rifDataLoaderTool.dataStorageLayer;

import java.sql.*;

import rifDataLoaderTool.system.*;
import rifServices.businessConceptLayer.User;


/**
 * This is a temporary class used to test the way the RIF data loader can create temporary
 * tables involved with processing steps.  Eventually it will be replaced by the 
 * {@link rifServices.dataStorageLayer.SQLConnectionManager} class that has already been
 * developed.  However, in the short term, we will use this class because of the restrictive
 * access permissions that are currently associated with the RIF schema.
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

public class SQLConnectionManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String databaseDriverClassName;
	private String databaseDriver;
	private String host;
	private String port;
	private String databaseName;

	private String databaseConnectionURL;
	// ==========================================
	// Section Construction
	// ==========================================

	public SQLConnectionManager() {
		databaseDriverClassName = "org.postgresql.Driver";
		databaseDriver = "jdbc:postgresql";
		host = "localhost";
		port = "5432";
		databaseName = "test";

		
		StringBuilder urlText = new StringBuilder();
		urlText.append(databaseDriver);
		urlText.append(":");
		urlText.append("//");
		urlText.append(host);
		urlText.append(":");
		urlText.append(port);
		urlText.append("/");
		urlText.append(databaseName);

		databaseConnectionURL = urlText.toString();
		
	}

	public Connection assignPooledWriteConnection(
		final User user) 
		throws RIFDataLoaderToolException {
		
		try {			
			Class.forName(databaseDriverClassName);
			
			Connection currentConnection 
				= DriverManager.getConnection(
					databaseConnectionURL,
					"kgarwood",
					"a");
			return currentConnection;
		}
		catch(ClassNotFoundException classNotFoundException) {
			classNotFoundException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"sqlConnectionManager.error.unableToLoadDatabaseDriver",
					"kgarwood");
			RIFDataLoaderToolException rifDataLoaderToolException
				= new RIFDataLoaderToolException(
					RIFDataLoaderToolError.LOAD_DB_DRIVER,
					errorMessage);
			throw rifDataLoaderToolException;			
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"sqlConnectionManager.error.unableToRegisterUser",
					"kgarwood");
			
			RIFDataLoaderToolException rifDataLoaderToolException
				= new RIFDataLoaderToolException(
					RIFDataLoaderToolError.GET_CONNECTION,
					errorMessage);
			throw rifDataLoaderToolException;		
		}

	}
	
	public void reclaimPooledWriteConnection(
		final User user, 
		final Connection connection) 
		throws RIFDataLoaderToolException {

		//KLG: Stub this until we can eventually just use one SQLConnectionManager
		//for the entire tool suite
		
		SQLQueryUtility.close(connection);
		
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public boolean isUserBlocked(
		final User user) {
		
		return false;
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


