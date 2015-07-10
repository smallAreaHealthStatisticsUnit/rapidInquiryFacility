package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.CleanWorkflowQueryGeneratorAPI;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaArea;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;


import rifGenericLibrary.dataStorageLayer.SQLDeleteRowsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLInsertQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLCreateTableQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.SQLQueryUtility;
import rifServices.businessConceptLayer.User;

import java.sql.*;
import java.util.ArrayList;


/**
 * Manages code used to store and retrieve information about data sources that are
 * processed by the RIF Data Loader Tool.
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

public final class DataSetManager 
	extends AbstractDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetManager(
		final RIFDataLoaderStartupOptions startupOptions) {
		
		super(startupOptions);
				
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void createDataSetConfigurationTable(
		final Connection connection) 
		throws RIFServiceException {
			
		PreparedStatement createDataSetConfigurationTableStatement = null;
		SQLCreateTableQueryFormatter queryFormatter 
			= new SQLCreateTableQueryFormatter();
		try {
			queryFormatter.setTableName("data_set_configurations");
			queryFormatter.addTextFieldDeclaration(
				"core_data_set_name", 
				false);			
			queryFormatter.addTextFieldDeclaration(
				"version", 
				100, 
				true);
			
			createDataSetConfigurationTableStatement
				= createPreparedStatement(connection, queryFormatter);
			createDataSetConfigurationTableStatement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.unableToCreateDataSetConfigurationsTable");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(createDataSetConfigurationTableStatement);			
		}
	}
	
	public void addDataSetConfiguration(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {

		if (dataSetConfigurationExists(connection, dataSetConfiguration)) {
			//trying to add a data set configuration that already exists
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.dataSetConfigurationAlreadyExists",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
				
		PreparedStatement addDataSetStatement = null;
		SQLInsertQueryFormatter addDataSetQueryFormatter
			= new SQLInsertQueryFormatter();		
		try {
			addDataSetQueryFormatter.setIntoTable("data_set_configurations");
			addDataSetQueryFormatter.addInsertField("core_data_set_name");
			addDataSetQueryFormatter.addInsertField("version");
			
			addDataSetStatement
				= createPreparedStatement(
					connection, 
					addDataSetQueryFormatter);
			addDataSetStatement.setString(
				1, 
				dataSetConfiguration.getName());		
			addDataSetStatement.setString(
				2, 
				dataSetConfiguration.getVersion());
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.unableToAddDataSetConfiguration",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(addDataSetStatement);			
		}
	}
		
	public void deleteDataSetConfiguration(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
	
		SQLDeleteRowsQueryFormatter deleteDataSetStatementQueryFormatter 
			= new SQLDeleteRowsQueryFormatter();
		deleteDataSetStatementQueryFormatter.setFromTable("data_set_configurations");
		deleteDataSetStatementQueryFormatter.addWhereParameter("core_data_set_name");
		deleteDataSetStatementQueryFormatter.addWhereParameter("version");
		
		PreparedStatement deleteDataSetConfigurationStatement = null;
		try {			
			deleteDataSetConfigurationStatement
				= createPreparedStatement(
					connection,
					deleteDataSetStatementQueryFormatter);
			deleteDataSetConfigurationStatement.setString(
				1, 
				dataSetConfiguration.getName());
			deleteDataSetConfigurationStatement.setString(
				2, 
				dataSetConfiguration.getVersion());
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.unableToDeleteDataSetConfiguration",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(deleteDataSetConfigurationStatement);
		}
		
	}
		
	private boolean dataSetConfigurationExists(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
				
		PreparedStatement statement = null;
		SQLRecordExistsQueryFormatter queryFormatter
			= new SQLRecordExistsQueryFormatter();
		queryFormatter.setLookupKeyFieldName("core_data_set_name");
		queryFormatter.addWhereParameter("version");
		
		
		boolean result = false;
		ResultSet resultSet = null;
		try {
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.setString(1, dataSetConfiguration.getName());
			statement.setString(2, dataSetConfiguration.getVersion());
			
			resultSet = statement.executeQuery();
			resultSet.next();
			
			result = resultSet.getBoolean(1);
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.unableToCheckDataSetConfigurationExists",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}

		return result;
	}
		
	/*
	 * Assume this table is created in RIF scripts?
	 */
	public void clearAllDataSets(
		final Connection connection) 
		throws RIFServiceException {

		//Create SQL query
		SQLDeleteRowsQueryFormatter queryFormatter = new SQLDeleteRowsQueryFormatter();
		queryFormatter.setFromTable("data_set_configurations");

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("dataSetManager.error.unableToCleardataSets");
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw RIFServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
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


