package rifDataLoaderTool.dataStorageLayer;



import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifServices.system.RIFServiceException;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;
import rifDataLoaderTool.businessConceptLayer.DataSet;
import rifDataLoaderTool.businessConceptLayer.LoadStepQueryGeneratorAPI;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowFieldConfiguration;
import rifGenericLibrary.dataStorageLayer.SQLInsertQueryFormatter;
import rifServices.dataStorageLayer.SQLQueryUtility;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.util.RIFLogger;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 * Manages database operations associated with loading a new data source into the RIF.
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

public final class LoadStepManager 
	extends AbstractDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDataLoaderStartupOptions startupOptions;
	private LoadStepQueryGeneratorAPI queryGenerator;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public LoadStepManager(
		final RIFDataLoaderStartupOptions startupOptions,
		final LoadStepQueryGeneratorAPI queryGenerator) {

		this.startupOptions = startupOptions;
		this.queryGenerator = queryGenerator;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public RIFResultTable getLoadTableData(
		final Connection connection,
		final CleanWorkflowConfiguration tableCleaningConfiguration) 
		throws RIFServiceException {
		
		RIFResultTable resultTable = new RIFResultTable();
		String coreTableName = tableCleaningConfiguration.getCoreDataSetName();
		String loadTableName
			= RIFTemporaryTablePrefixes.LOAD.getTableName(coreTableName);
		String[] fieldNames = tableCleaningConfiguration.getLoadFieldNames();
		
		try {
			resultTable 
				= getTableData(connection, loadTableName, fieldNames);
			return resultTable;
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
		}
		finally {
			SQLQueryUtility.close(connection);
		}
		return resultTable;
	}
	
	public void createLoadTable(
		final Connection connection,
		final CleanWorkflowConfiguration tableCleaningConfiguration,
		final int textFieldWidth) 
		throws RIFServiceException {

		

		RIFLogger logger = RIFLogger.getLogger();		

		
		String coreTableName = tableCleaningConfiguration.getCoreDataSetName();
		int textColumnWidth
			= startupOptions.getDataLoaderTextColumnSize();
		PreparedStatement dropTableStatement = null;		
		try {
			
			//drop the table if it already exists so we can recreate it
			//without raising a 'table already exists' exception
			
			String dropLoadTableQuery
				= queryGenerator.generateDropLoadTableQuery(tableCleaningConfiguration);
			logger.debugQuery(
				this, 
				"createLoadTable",
				dropLoadTableQuery);

			dropTableStatement = connection.prepareStatement(dropLoadTableQuery);
			dropTableStatement.executeUpdate();	
		}
		catch(SQLException sqlException) {
			String createTableName
				= generateLoadTableName(coreTableName);
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"loadStepManager.error.dropLoadTable",
					createTableName);
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.LOAD_TABLE,
					errorMessage);
			throw RIFServiceException;
		}
		finally {			
			SQLQueryUtility.close(dropTableStatement);
		}
		
		PreparedStatement createLoadTableStatement = null;
		try {
			
			//drop the table if it already exists so we can recreate it
			//without raising a 'table already exists' exception
			
			String createLoadTableQuery
				= queryGenerator.generateLoadTableQuery(
					1,
					tableCleaningConfiguration, 
					textColumnWidth);
			logger.debugQuery(
				this, 
				"createLoadTable",
				createLoadTableQuery);
			createLoadTableStatement
				= connection.prepareStatement(createLoadTableQuery);
			createLoadTableStatement.executeUpdate();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String createTableName
				= RIFTemporaryTablePrefixes.LOAD.getTableName(
					tableCleaningConfiguration.getCoreDataSetName());
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"loadStepManager.error.createLoadTable",
					createTableName);
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.LOAD_TABLE,
					errorMessage);
			throw RIFServiceException;
		}
		finally {			
			SQLQueryUtility.close(createLoadTableStatement);
		}	
	}
	
	public void addLoadTableData(
		final Connection connection,
		final CleanWorkflowConfiguration tableCleaningConfiguration,
		final String[][] tableData) 
		throws RIFServiceException {
		

		String coreTableName 
			= tableCleaningConfiguration.getCoreDataSetName();
		String loadTableName
			= generateLoadTableName(coreTableName);
		SQLInsertQueryFormatter queryFormatter 
			= new SQLInsertQueryFormatter();
		queryFormatter.setIntoTable(loadTableName);
		queryFormatter.addInsertField("data_source_id");
		ArrayList<CleanWorkflowFieldConfiguration> fieldConfigurations
			= tableCleaningConfiguration.getIncludedFieldCleaningConfigurations();
		for (CleanWorkflowFieldConfiguration fieldConfiguration : fieldConfigurations) {
			queryFormatter.addInsertField(fieldConfiguration.getLoadTableFieldName());
		}

		RIFLogger logger = RIFLogger.getLogger();		

		PreparedStatement statement = null;
		try {
			logger.debugQuery(
				this, 
				"addLoadTableData",
				queryFormatter.generateQuery());
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());

			DataSet dataSet = tableCleaningConfiguration.getDataSet();
			Integer dataSetIdentifier
				= Integer.valueOf(dataSet.getIdentifier());

			for (int ithRow = 0; ithRow < tableData.length; ithRow++) {				
				
				statement.setInt(1, dataSetIdentifier);
				for (int ithParameter = 0; ithParameter < tableData[ithRow].length; ithParameter++) {
					statement.setString(ithParameter + 2, tableData[ithRow][ithParameter]);					
				}
				statement.executeUpdate();
			}			
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
	}
	
	
	
	public void dropLoadTable(
		final Connection connection,
		final CleanWorkflowConfiguration tableCleaningConfiguration) 
		throws RIFServiceException {

		RIFLogger logger = RIFLogger.getLogger();		
		
		PreparedStatement statement = null;

		try {
			String dropLoadTableQuery
				= queryGenerator.generateDropLoadTableQuery(tableCleaningConfiguration);
			logger.debugQuery(
				this, 
				"dropLoadTable",
				dropLoadTableQuery);

			statement = connection.prepareStatement(dropLoadTableQuery);
			statement.executeUpdate();	
		}
		catch(SQLException sqlException) {
			String loadTableName
				= RIFTemporaryTablePrefixes.LOAD.getTableName(
					tableCleaningConfiguration.getCoreDataSetName());
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"loadStepManager.error.dropLoadTable",
					loadTableName);
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DROP_TABLE,
					errorMessage);
			throw RIFServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);			
		}	
	}
	
	private String generateLoadTableName(
		final String coreTableName) {
		
		String loadTableName
			= RIFTemporaryTablePrefixes.LOAD.getTableName(
				coreTableName);
		return loadTableName;
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


