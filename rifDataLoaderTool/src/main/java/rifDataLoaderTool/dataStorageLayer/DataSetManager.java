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


import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.SQLDeleteRowsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLInsertQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.SQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLCreateTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;

import java.sql.*;
import java.util.ArrayList;
import java.io.*;

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
		final RIFDatabaseProperties rifDatabaseProperties) {
		
		super(rifDatabaseProperties);
				
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public int getDataSetIdentifier(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		int result = 0;
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		try {
			SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
			queryFormatter.addSelectField("id");
			queryFormatter.addFromTable("data_set_configurations");
			queryFormatter.addWhereParameter("core_data_set_name");
			queryFormatter.addWhereParameter("version");
			
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.setString(1, dataSetConfiguration.getName());
			statement.setString(2, dataSetConfiguration.getVersion());
			resultSet = statement.executeQuery();
			if (resultSet.next() == false) {
				//ERROR: non-existent data set specified
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage("");
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFGenericLibraryError.DATABASE_QUERY_FAILED,
						errorMessage);
				throw rifServiceException;
			}

			result = resultSet.getInt(1);
			return result;
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(resultSet);
			SQLQueryUtility.close(statement);
		}
	}
	
	public int addDataSetConfiguration(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {

		if (dataSetConfigurationExists(
			connection, 
			logFileWriter,
			dataSetConfiguration)) {
			
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

		int result = 0;

		PreparedStatement getIdentifierStatement = null;		
		PreparedStatement addDataSetStatement = null;
		ResultSet resultSet = null;
		SQLInsertQueryFormatter addDataSetQueryFormatter
			= new SQLInsertQueryFormatter();		
		
		SQLGeneralQueryFormatter getIdentifierQueryFormatter
			= new SQLGeneralQueryFormatter();
		
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
			
			addDataSetStatement.executeUpdate();
			getIdentifierQueryFormatter.addQueryPhrase(0, "SELECT CURRVAL('data_set_sequence');");
			getIdentifierStatement
				= createPreparedStatement(
					connection, 
					getIdentifierQueryFormatter);
			resultSet
				= getIdentifierStatement.executeQuery();
			resultSet.next();
			result = resultSet.getInt(1);
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
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
			SQLQueryUtility.close(getIdentifierStatement);			
			SQLQueryUtility.close(resultSet);			
		}
		
		return result;
	}
		
	public void deleteDataSetConfiguration(
		final Connection connection,
		final Writer logFileWriter,
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
			logSQLException(
				logFileWriter,
				sqlException);
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
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
				
		PreparedStatement statement = null;
		SQLRecordExistsQueryFormatter queryFormatter
			= new SQLRecordExistsQueryFormatter();
		queryFormatter.setFromTable("data_set_configurations");
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
			result = resultSet.next();
			return result;
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
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

	}
		
	/*
	 * Assume this table is created in RIF scripts?
	 */
	public void clearAllDataSets(
		final Connection connection,
		final Writer logFileWriter) 
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
			logSQLException(
				logFileWriter,
				sqlException);
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


