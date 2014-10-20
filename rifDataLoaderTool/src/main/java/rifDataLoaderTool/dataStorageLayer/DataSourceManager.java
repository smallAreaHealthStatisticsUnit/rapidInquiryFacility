package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.DataSource;
import rifDataLoaderTool.system.RIFDataLoaderToolException;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;


import rifServices.dataStorageLayer.SQLDeleteRowsQueryFormatter;
import rifServices.dataStorageLayer.SQLInsertQueryFormatter;
import rifServices.dataStorageLayer.SQLRecordExistsQueryFormatter;
import rifServices.dataStorageLayer.SQLSelectQueryFormatter;

import java.sql.*;


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

public class DataSourceManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DataSourceManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/*
	 * Assume this table is created in RIF scripts?
	 */
	public void clearAllDataSources(
		final Connection connection) 
		throws RIFDataLoaderToolException {

		//Create SQL query
		SQLDeleteRowsQueryFormatter queryFormatter = new SQLDeleteRowsQueryFormatter();
		queryFormatter.setFromTable("data_source_registry");

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("dataSourceManager.error.unableToClearDataSources");
			RIFDataLoaderToolException rifDataLoaderToolException
				= new RIFDataLoaderToolException(
					RIFDataLoaderToolError.CLEAR_ALL_DATA_SOURCES, 
					errorMessage);
			throw rifDataLoaderToolException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}		
	}
		
	public void registerDataSource(
		final Connection connection,
		final DataSource dataSource) 
		throws RIFDataLoaderToolException {
			
		//Validate parameters
		dataSource.checkErrors();
		
		checkDuplicateDataSource(
			connection,
			dataSource);

		//Create SQL query		
		SQLInsertQueryFormatter queryFormatter = new SQLInsertQueryFormatter();
		queryFormatter.addInsertField("core_table_name");
		queryFormatter.addInsertField("derived_from_existing_table");
		queryFormatter.addInsertField("source_name");
		queryFormatter.addInsertField("user_id");
		queryFormatter.setIntoTable("data_source_registry");
				
		PreparedStatement statement = null;
		
		try {
			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, dataSource.getCoreTableName());
			statement.setBoolean(2, dataSource.isDerivedFromExistingTable());
			statement.setString(3, dataSource.getSourceName());
			statement.setString(4, dataSource.getUserID());
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("dataSourceManager.error.registerDataSource");
			RIFDataLoaderToolException rifDataLoaderToolException
				= new RIFDataLoaderToolException(
					RIFDataLoaderToolError.REGISTER_DATA_SOURCE,
					errorMessage);
			throw rifDataLoaderToolException;
		}
		finally {
			SQLQueryUtility.close(connection);;
		}		
	}
	
	public DataSource getDataSourceFromCoreTableName(
		final Connection connection,
		final String coreTableName) 
		throws RIFDataLoaderToolException {
		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("identifier");
		queryFormatter.addSelectField("derived_from_existing_table");
		queryFormatter.addSelectField("source_name");
		queryFormatter.addSelectField("user_id");
		queryFormatter.addFromTable("data_source_registry");
		
		DataSource result = DataSource.newInstance();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			resultSet.next();
			
			result.setCoreTableName(coreTableName);
			result.setIdentifier(resultSet.getString(1));
			result.setDerivedFromExistingTable(resultSet.getBoolean(2));
			result.setSourceName(resultSet.getString(3));
			result.setUserID(resultSet.getString(4));
			result.setRegistrationDate(resultSet.getDate(5));
		}
		catch(SQLException sqlException) {
			SQLQueryUtility.close(resultSet);
			SQLQueryUtility.close(connection);
		}

		return result;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private void checkDuplicateDataSource(
		final Connection connection,
		final DataSource dataSource) 
			throws RIFDataLoaderToolException {
		
		String coreTableName = dataSource.getCoreTableName();

		SQLRecordExistsQueryFormatter queryFormatter 
			= new SQLRecordExistsQueryFormatter();
		queryFormatter.setLookupKeyFieldName("core_table_name");
		queryFormatter.setFromTable("data_source_registry");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, coreTableName);
			resultSet = statement.executeQuery();
			
			if (resultSet.next() == true) {
				//there is already a data source with this core table name
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"dataSourceManager.error.dataSourceAlreadyExists",
						coreTableName);
				RIFDataLoaderToolException rifDataLoaderToolException
					= new RIFDataLoaderToolException(
						RIFDataLoaderToolError.DUPLICATE_DATA_SOURCE,
						errorMessage);
				throw rifDataLoaderToolException;
			}
			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("sqlQuery.databaseFailed");
			RIFDataLoaderToolException rifDataLoaderToolException
				= new RIFDataLoaderToolException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifDataLoaderToolException;
		}
		finally {
			SQLQueryUtility.close(resultSet);
			SQLQueryUtility.close(statement);			
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


