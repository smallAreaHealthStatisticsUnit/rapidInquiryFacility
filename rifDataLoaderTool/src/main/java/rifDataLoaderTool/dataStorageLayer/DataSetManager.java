package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.DataSet;
import rifServices.system.RIFServiceException;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;


import rifGenericLibrary.dataStorageLayer.SQLDeleteRowsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLInsertQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifServices.dataStorageLayer.SQLQueryUtility;

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

public final class DataSetManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/*
	 * Assume this table is created in RIF scripts?
	 */
	public void clearAlldataSets(
		final Connection connection) 
		throws RIFServiceException {

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
				= RIFDataLoaderToolMessages.getMessage("dataSetManager.error.unableToCleardataSets");
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.CLEAR_ALL_DATA_SOURCES, 
					errorMessage);
			throw RIFServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}		
	}
		
	public void registerDataSet(
		final Connection connection,
		final DataSet dataSet) 
		throws RIFServiceException {
			
		//Validate parameters
		dataSet.checkErrors();
		
		checkDuplicatedataSet(
			connection,
			dataSet);

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
			statement.setString(1, dataSet.getCoreDataSetName());
			statement.setBoolean(2, dataSet.isDerivedFromExistingTable());
			statement.setString(3, dataSet.getSourceName());
			statement.setString(4, dataSet.getUserID());
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("dataSetManager.error.registerdataSet");
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.REGISTER_DATA_SOURCE,
					errorMessage);
			throw RIFServiceException;
		}
		finally {
			SQLQueryUtility.close(connection);;
		}		
	}
	
	public DataSet getDataSetFromCoreTableName(
		final Connection connection,
		final String coreDataSetName) 
		throws RIFServiceException {
		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("identifier");
		queryFormatter.addSelectField("core_data_set");
		queryFormatter.addSelectField("derived_from_existing_table");
		queryFormatter.addSelectField("source_name");
		queryFormatter.addSelectField("user_id");
		queryFormatter.addSelectField("registration_date");
		queryFormatter.addFromTable("data_source_registry");
		
		DataSet result = DataSet.newInstance();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			resultSet.next();
			
			result.setCoreDataSetName(resultSet.getString(1));
			result.setIdentifier(resultSet.getString(2));
			result.setDerivedFromExistingTable(resultSet.getBoolean(3));
			result.setSourceName(resultSet.getString(4));
			result.setUserID(resultSet.getString(5));
			result.setRegistrationDate(resultSet.getDate(6));
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

	private void checkDuplicatedataSet(
		final Connection connection,
		final DataSet dataSet) 
			throws RIFServiceException {
		
		String coreDataSetName = dataSet.getCoreDataSetName();

		SQLRecordExistsQueryFormatter queryFormatter 
			= new SQLRecordExistsQueryFormatter();
		queryFormatter.setLookupKeyFieldName("core_table_name");
		queryFormatter.setFromTable("data_source_registry");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, coreDataSetName);
			resultSet = statement.executeQuery();
			
			if (resultSet.next() == true) {
				//there is already a data source with this core table name
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"dataSetManager.error.dataSetAlreadyExists",
						coreDataSetName);
				RIFServiceException RIFServiceException
					= new RIFServiceException(
						RIFDataLoaderToolError.DUPLICATE_DATA_SOURCE,
						errorMessage);
				throw RIFServiceException;
			}
			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("sqlQuery.databaseFailed");
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw RIFServiceException;
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


