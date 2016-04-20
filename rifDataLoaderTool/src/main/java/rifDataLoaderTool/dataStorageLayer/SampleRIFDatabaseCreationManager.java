package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLCreateTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLDeleteTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.PropertyResourceBundle;



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
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDataLoaderStartupOptions startupOptions;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public SampleRIFDatabaseCreationManager(
		final RIFDataLoaderStartupOptions startupOptions) {

		this.startupOptions = startupOptions;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setup()
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		Connection connection = null;
		try {
			
			File userLoginDetailsFile = new File("C://rif_scripts//db//RIFDatabaseProperties.txt");
			FileReader fileReader = new FileReader(userLoginDetailsFile);
			PropertyResourceBundle userLoginResourceBundle
				= new PropertyResourceBundle(fileReader);
			
			String userID = (String) userLoginResourceBundle.getObject("userID");
			String password = (String) userLoginResourceBundle.getObject("password");
			
			
			StringBuilder urlText = new StringBuilder();
			urlText.append(startupOptions.getJDBCDriverPrefix());
			urlText.append(":");
			urlText.append("//");
			urlText.append(startupOptions.getHost());
			urlText.append(":");
			urlText.append(startupOptions.getPort());
			urlText.append("/");
			String databaseURL = urlText.toString();	
			
			connection
				= DriverManager.getConnection(databaseURL, userID, password);
			SQLGeneralQueryFormatter queryFormatter
				= new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase("DROP DATABASE IF EXISTS ");
			queryFormatter.addQueryPhrase(startupOptions.getDatabaseName());
			queryFormatter.addQueryPhrase(";");
			queryFormatter.finishLine();
			queryFormatter.addQueryPhrase("CREATE DATABASE ");
			queryFormatter.addQueryPhrase(startupOptions.getDatabaseName());
			queryFormatter.addQueryPhrase(";");
			queryFormatter.finishLine();

			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.executeUpdate();
			createDatabaseTables(userID, password);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			String errorMessage	
				= RIFDataLoaderToolMessages.getMessage(
					"sampleRIFDatabaseCreationManager.error.unableToInitialiseDatabase");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(connection);
		}
		
	}
		
	public void createDatabaseTables(
		final String userID,
		final String password) 
		throws RIFServiceException {

		PreparedStatement statement = null;
		Connection connection = null;		
		try {
			StringBuilder urlText = new StringBuilder();
			urlText.append(startupOptions.getJDBCDriverPrefix());
			urlText.append(":");
			urlText.append("//");
			urlText.append(startupOptions.getHost());
			urlText.append(":");
			urlText.append(startupOptions.getPort());
			urlText.append("/");
			urlText.append(startupOptions.getDatabaseName());
			String databaseURL = urlText.toString();			
			
			
			connection
				= DriverManager.getConnection(databaseURL, userID, password);

			createCovariatesTable(connection);
			createDataSetConfigurationsTable(connection);
			createAuditChangesTable(connection);
			createAuditFailedValidationTable(connection);		
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			String errorMessage	
				= RIFDataLoaderToolMessages.getMessage(
					"sampleRIFDatabaseCreationManager.error.unableToInitialiseDatabase");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(connection);
		}
		
	}
	
	private void createCovariatesTable(
		final Connection connection) 
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		try {			
			//create covariates table
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
		
			statement
				= SQLQueryUtility.createPreparedStatement(
					connection, 
					createCovariateTableQueryFormatter);
		
			statement.executeUpdate();
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
			SQLQueryUtility.close(statement);
			
		}
	}

	private void createDataSetConfigurationsTable(
		final Connection connection) 
		throws RIFServiceException {
		
		PreparedStatement createIDSequenceStatement = null;
		PreparedStatement createDataSetConfigurationTableStatement = null;
		PreparedStatement sequenceOwnershipStatement = null;
		try {
			
			//Create a sequence that will auto-increment
			SQLGeneralQueryFormatter createIDSequenceQueryFormatter = new SQLGeneralQueryFormatter();
			createIDSequenceQueryFormatter.addQueryPhrase(
				0, 
				"CREATE SEQUENCE data_set_sequence;");
			createIDSequenceStatement
				= SQLQueryUtility.createPreparedStatement(
						connection, 
						createIDSequenceQueryFormatter);
			createIDSequenceStatement.executeUpdate();
			
			//Make the 'id' field get its value from the sequence
			SQLCreateTableQueryFormatter createDataSetConfigurationsTableFormatter 
				= new SQLCreateTableQueryFormatter();
			createDataSetConfigurationsTableFormatter.setTableName("data_set_configurations");
			createDataSetConfigurationsTableFormatter.addSequenceField("id", "data_set_sequence");
			createDataSetConfigurationsTableFormatter.addTextFieldDeclaration("core_data_set_name", 50, false);
			createDataSetConfigurationsTableFormatter.addTextFieldDeclaration("version", 20, false);
			createDataSetConfigurationsTableFormatter.addCreationTimestampField("creation_date");
			String startCode = WorkflowState.START.getCode();
			createDataSetConfigurationsTableFormatter.addTextFieldDeclaration(
				"current_workflow_state", 
				20, 
				startCode,
				false);
			
			createDataSetConfigurationTableStatement
				= SQLQueryUtility.createPreparedStatement(
					connection, 
					createDataSetConfigurationsTableFormatter);
			
			createDataSetConfigurationTableStatement.executeUpdate();
			
			//Ensure that the id field owns the sequence so that no other table could
			//increment it
			SQLGeneralQueryFormatter sequenceOwnershipQueryFormatter
				= new SQLGeneralQueryFormatter();
			sequenceOwnershipQueryFormatter.addQueryPhrase(0, "ALTER SEQUENCE data_set_sequence ");
			sequenceOwnershipQueryFormatter.addQueryPhrase("OWNED BY ");
			sequenceOwnershipQueryFormatter.addQueryPhrase("data_set_configurations.id;");
			sequenceOwnershipStatement
				= SQLQueryUtility.createPreparedStatement(
					connection, 
					sequenceOwnershipQueryFormatter);
			sequenceOwnershipStatement.executeUpdate();			
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
			SQLQueryUtility.close(createIDSequenceStatement);
			SQLQueryUtility.close(createDataSetConfigurationTableStatement);
			SQLQueryUtility.close(sequenceOwnershipStatement);		
		}	
		
	}
	
	public void createAuditChangesTable(
		final Connection connection) 
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		try {
			SQLCreateTableQueryFormatter queryFormatter
				= new SQLCreateTableQueryFormatter();
			queryFormatter.setTableName("rif_change_log");

			queryFormatter.addFieldDeclaration(
				"data_set_id", 
				"INTEGER", 
				false);

			queryFormatter.addFieldDeclaration(
				"row_number", 
				"INTEGER", 
				false);
			
			queryFormatter.addTextFieldDeclaration(
				"field_name", 
				30, 
				false);

			
			queryFormatter.addTextFieldDeclaration(
				"old_value", 
				30, 
				true);

			
			queryFormatter.addTextFieldDeclaration(
				"new_value", 
				30, 
				true);
		
			queryFormatter.addCreationTimestampField("time_stamp");
			statement
				= SQLQueryUtility.createPreparedStatement(
				connection, 
				queryFormatter);
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"sampleRIFDatabaseCreationManager.error.unableToInitialiseDB");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}	
	}

	
	public void createAuditFailedValidationTable(
		final Connection connection) 
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		try {
			SQLCreateTableQueryFormatter queryFormatter
				= new SQLCreateTableQueryFormatter();
			queryFormatter.setTableName("rif_failed_val_log");

			queryFormatter.addFieldDeclaration(
				"data_set_id", 
				"INTEGER", 
				false);

			queryFormatter.addFieldDeclaration(
				"row_number", 
				"INTEGER", 
				false);
			
			queryFormatter.addTextFieldDeclaration(
				"field_name", 
				30, 
				false);

			
			queryFormatter.addTextFieldDeclaration(
				"invalid_value", 
				30, 
				true);
		
			queryFormatter.addCreationTimestampField("time_stamp");
			statement
				= SQLQueryUtility.createPreparedStatement(
				connection, 
				queryFormatter);
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"sampleRIFDatabaseCreationManager.error.unableToInitialiseDB");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
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


