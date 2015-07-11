package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolError;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLCreateTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLDeleteTableQueryFormatter;

import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Because the data loader tool can modify a lot of important tables in the RIF, we
 * use a fake rif database for testing purposes.  This class creates some of the
 * basic tables that appear in the production RIF database.
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

public class SampleRIFDatabaseCreationManager {

	public static final void main(String[] arguments) {
		
		try {
			SampleRIFDatabaseCreationManager fakeDatabaseCreationManager
				= new SampleRIFDatabaseCreationManager();
			
			
			String databaseDriverName = "org.postgresql.Driver";
			String databaseDriverPrefix = "jdbc:postgresql";
			String host = "localhost";
			String port = "5432";
			String databaseName = "tmp_sahsu_db";
			SQLConnectionManager sqlConnectionManager
				= new SQLConnectionManager(
					databaseDriverName,
					databaseDriverPrefix,
					host,
					port,
					databaseName);	
			Connection connection 
				= sqlConnectionManager.createConnection(
					"kgarwood", 
					"kgarwood", 
					true);
			//fakeDatabaseCreationManager.createTables(connection);
			fakeDatabaseCreationManager.deleteTables(connection);
			
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
		}
		
		
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

	public SampleRIFDatabaseCreationManager() {

	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	public void createTables(
		final Connection connection) 
		throws RIFServiceException {
		
		PreparedStatement createCovariateTableStatement = null;
		try {
			SQLCreateTableQueryFormatter createCovariateTableQueryFormatter
				= new SQLCreateTableQueryFormatter();
			createCovariateTableQueryFormatter.setTableName("rif40_covariates");
			createCovariateTableQueryFormatter.addTextFieldDeclaration(
				"geography", 
				50, 
				false);
			createCovariateTableQueryFormatter.addTextFieldDeclaration(
				"geolevel_name", 
				30, 
				false);
			createCovariateTableQueryFormatter.addTextFieldDeclaration(
				"covariate_name", 
				30, 
				false);						
			createCovariateTableQueryFormatter.addFieldDeclaration(
				"min", 
				"double precision", 
				false);			
			
			createCovariateTableQueryFormatter.addFieldDeclaration(
				"max", 
				"double precision", 
				false);					
			createCovariateTableQueryFormatter.addFieldDeclaration(
				"type", 
				"double precision", 
				false);			
			
			createCovariateTableStatement
				= SQLQueryUtility.createPreparedStatement(
					connection, 
					createCovariateTableQueryFormatter);
			
			createCovariateTableStatement.executeUpdate();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"sampleRIFDatabaseCreationManager.error.unableToInitialiseDB");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.UNABLE_TO_CREATE_FAKE_RIF_DB, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(createCovariateTableStatement);
		}	
	}


	
	public void deleteTables(
		final Connection connection) 
		throws RIFServiceException {
		
		PreparedStatement deleteCovariateTableStatement = null;
		try {
			SQLDeleteTableQueryFormatter deleteCovariateTableQueryFormatter
				= new SQLDeleteTableQueryFormatter();
			deleteCovariateTableQueryFormatter.setTableToDelete("rif40_covariates");

			deleteCovariateTableStatement
				= SQLQueryUtility.createPreparedStatement(
					connection, 
					deleteCovariateTableQueryFormatter);
			
			System.out.println("About to delete tables");
			deleteCovariateTableStatement.executeUpdate();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"sampleRIFDatabaseCreationManager.error.unableToInitialiseDB");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.UNABLE_TO_CREATE_FAKE_RIF_DB, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(deleteCovariateTableStatement);
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


