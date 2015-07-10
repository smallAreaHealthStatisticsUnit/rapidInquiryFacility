package rifDataLoaderTool.dataStorageLayer;



import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;



import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifGenericLibrary.dataStorageLayer.SQLInsertQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLDeleteTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLCreateTableQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.SQLQueryUtility;
import rifServices.businessConceptLayer.RIFResultTable;






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

public final class LoadWorkflowManager 
	extends AbstractDataLoaderStepManager {

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

	public LoadWorkflowManager(
		final RIFDataLoaderStartupOptions startupOptions) {

		super(startupOptions);
		
		this.startupOptions = startupOptions;

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public RIFResultTable getLoadTableData(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		RIFResultTable resultTable = new RIFResultTable();
		String coreTableName = dataSetConfiguration.getName();
		String loadTableName
			= RIFTemporaryTablePrefixes.LOAD.getTableName(coreTableName);
		String[] fieldNames = dataSetConfiguration.getLoadFieldNames();
		
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
	
	public void loadConfiguration(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {

	
		
		int textColumnWidth
			= startupOptions.getDataLoaderTextColumnSize();
		PreparedStatement dropTableStatement = null;		
		String coreDataSetName 
			= dataSetConfiguration.getName();
		String targetLoadTable
			= RIFTemporaryTablePrefixes.LOAD.getTableName(coreDataSetName);
		try {
			
			//drop the table if it already exists so we can recreate it
			//without raising a 'table already exists' exception
			
			SQLDeleteTableQueryFormatter deleteLoadTableQueryFormatter 
				= new SQLDeleteTableQueryFormatter();
			logSQLQuery(
				"delete_existing_load_table", 
				deleteLoadTableQueryFormatter);
			
			deleteLoadTableQueryFormatter.setTableToDelete(targetLoadTable);
			dropTableStatement
				= createPreparedStatement(connection, deleteLoadTableQueryFormatter);
			dropTableStatement.executeUpdate();	
		}
		catch(SQLException sqlException) {			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"loadWorkflowManager.error.dropLoadTable",
					targetLoadTable);
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
			SQLCreateTableQueryFormatter createLoadTableQueryFormatter
				 = new SQLCreateTableQueryFormatter();
			createLoadTableQueryFormatter.setTextFieldLength(textColumnWidth);
			createLoadTableQueryFormatter.setTableName(targetLoadTable);
			createLoadTableQueryFormatter.addFieldDeclaration(
				"data_source_id", 
				"INTEGER", 
				false);
			createLoadTableQueryFormatter.addFieldDeclaration(
				"row_number", 
				"SERIAL", 
				false);
						
			ArrayList<DataSetFieldConfiguration> fieldConfigurations
				= dataSetConfiguration.getFieldConfigurations();
			
			for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
				createLoadTableQueryFormatter.addTextFieldDeclaration(
					fieldConfiguration.getLoadFieldName(), 
					true);
			}
			
			logSQLQuery(
				"create_load_table", 
				createLoadTableQueryFormatter);
			
			createLoadTableStatement
				= connection.prepareStatement(createLoadTableQueryFormatter.generateQuery());
			createLoadTableStatement.executeUpdate();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String createTableName
				= RIFTemporaryTablePrefixes.LOAD.getTableName(
						dataSetConfiguration.getName());
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"loadWorkflowManager.error.createLoadTable",
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
		final DataSetConfiguration dataSetConfiguration,
		final String[][] tableData) 
		throws RIFServiceException {
		
		String loadTableName
			= RIFTemporaryTablePrefixes.LOAD.getTableName(
					dataSetConfiguration.getName());		
		
		SQLInsertQueryFormatter queryFormatter 
			= new SQLInsertQueryFormatter();
		queryFormatter.setIntoTable(loadTableName);
		queryFormatter.addInsertField("data_source_id");
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			queryFormatter.addInsertField(fieldConfiguration.getLoadFieldName());
		}

		logSQLQuery(
			"addLoadTableData", 
			queryFormatter);
		
		PreparedStatement statement = null;
		try {

			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			Integer dataSetIdentifier
				= Integer.valueOf(dataSetConfiguration.getIdentifier());

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
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {


		String coreDataSetName = dataSetConfiguration.getName();
		String tableToDelete
			= RIFTemporaryTablePrefixes.LOAD.getTableName(coreDataSetName);		
				
		PreparedStatement statement = null;
		try {
	
			SQLDeleteTableQueryFormatter deleteLoadTableQueryFormatter
				= new SQLDeleteTableQueryFormatter();
			deleteLoadTableQueryFormatter.setTableToDelete(tableToDelete);

			statement 
				= createPreparedStatement(connection, deleteLoadTableQueryFormatter);
			statement.executeUpdate();	
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"loadWorkflowManager.error.dropLoadTable",
					tableToDelete);
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


