package rifDataLoaderTool.dataStorageLayer;


import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowQueryGeneratorAPI;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.dataStorageLayer.postgresql.*;
import rifDataLoaderTool.fileFormats.workflows.RIFDataLoadingResultTheme;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.SQLCountQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.RIFResultTable;

import java.io.*;
import java.sql.*;

/**
 * manages database calls related to cleaning a data source.
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

public final class CleanWorkflowManager 
	extends AbstractDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ChangeAuditManager changeAuditManager;
	private DataSetManager dataSetManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleanWorkflowManager(
		final RIFDatabaseProperties rifDatabaseProperties,
		final DataSetManager dataSetManager,
		final ChangeAuditManager changeAuditManager,
		final CleanWorkflowQueryGeneratorAPI queryGenerator) {

		super(rifDatabaseProperties);
		this.dataSetManager = dataSetManager;
		this.changeAuditManager = changeAuditManager;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public RIFResultTable getCleanedTableData(
		final Connection connection,
		final FileWriter logFileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
					
		RIFResultTable resultTable = new RIFResultTable();
		String coreDataSetName = dataSetConfiguration.getName();
		String searchReplaceTableName
			= RIFTemporaryTablePrefixes.CLEAN_CASTING.getTableName(coreDataSetName);
		String[] cleanFieldNames = dataSetConfiguration.getCleanFieldNames();
			
		try {
			resultTable 
				= getTableData(
					connection, 
					searchReplaceTableName, 
					cleanFieldNames);
			
			return resultTable;
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter, 
				sqlException);
		}
		finally {
			SQLQueryUtility.close(connection);
		}
		return resultTable;
	}
		
	public void cleanConfiguration(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
				
		
		RIFLogger logger = RIFLogger.getLogger();
		
		PreparedStatement searchReplaceStatement = null;
		PreparedStatement validationStatement = null;
		PreparedStatement castingStatement = null;
		try {

			String coreDataSetName
				= dataSetConfiguration.getName();
			
			int dataSetIdentifier
				= dataSetManager.getDataSetIdentifier(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			

			/*
			 * Part I: Perform search and replace values to help substitute
			 * poor field values for better ones
			 */
			PostgreSQLDataTypeSearchReplaceUtility searchReplaceUtility
				= new PostgreSQLDataTypeSearchReplaceUtility();
			String searchReplaceQuery
				= searchReplaceUtility.generateSearchReplaceTableStatement(dataSetConfiguration);

			logSQLQuery(
				logFileWriter, 
				"createCleaningSearchReplaceTable", 
				searchReplaceQuery);
			searchReplaceStatement
				= connection.prepareStatement(searchReplaceQuery);
			searchReplaceStatement.executeUpdate();
			exportTable(
				connection, 
				logFileWriter, 
				exportDirectoryPath, 
				RIFDataLoadingResultTheme.STAGES, 
				RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE.getTableName(coreDataSetName));			
			
			
			//check that the search replace table has just as many rows as the
			//original load table			
			checkTotalRowsMatch(
				connection, 
				logFileWriter,
				coreDataSetName,
				RIFTemporaryTablePrefixes.EXTRACT,
				RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE);			

			
			/*
			 * Part II: Now validate the results, and change the field value to 
			 * 'rif_error' if it fails validation
			 */
			PostgreSQLDataTypeValidationUtility validationUtility
				= new PostgreSQLDataTypeValidationUtility();
			String validationQuery
				= validationUtility.generateValidationTableStatement(dataSetConfiguration);

			logSQLQuery(
				logFileWriter, 
				"createCleaningValidationTable", 
				validationQuery);
			validationStatement
				= connection.prepareStatement(validationQuery);
			validationStatement.executeUpdate();

			exportTable(
					connection, 
					logFileWriter, 
					exportDirectoryPath, 
					RIFDataLoadingResultTheme.STAGES, 
					RIFTemporaryTablePrefixes.CLEAN_VALIDATION.getTableName(coreDataSetName));			
			
			checkTotalRowsMatch(
				connection, 
				logFileWriter,
				coreDataSetName,
				RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE,
				RIFTemporaryTablePrefixes.CLEAN_VALIDATION);			
			
			
			/*
			 * Part III: Finally, cast each field to its appropriate data type (eg: int,
			 * double, etc).  If any of the field values have 'rif_error' then cast the
			 * NULL value.
			 */
			PostgreSQLCastingUtility castingUtility = new PostgreSQLCastingUtility();
			String castingQuery
				= castingUtility.generateCastingTableQuery(dataSetConfiguration);

			logSQLQuery(
				logFileWriter, 
				"createCleaningCastingTable", 
				castingQuery);

			castingStatement
				= connection.prepareStatement(castingQuery);
			castingStatement.executeUpdate();

			exportTable(
				connection, 
				logFileWriter, 
				exportDirectoryPath, 
				RIFDataLoadingResultTheme.STAGES, 
				RIFTemporaryTablePrefixes.CLEAN_CASTING.getTableName(coreDataSetName));			
			
			
			checkTotalRowsMatch(
				connection, 
				logFileWriter,
				coreDataSetName,
				RIFTemporaryTablePrefixes.CLEAN_VALIDATION,
				RIFTemporaryTablePrefixes.CLEAN_CASTING);			

			
			/*
			 * Copy the contents of the casting table and call it
			 * the final cleaning table.  Note that in future we may well
			 * just rename the casting table.  But I'm not sure whether we may
			 * need to retain it for other purposes.
			 */
			
			String cleaningCastingTableName
				= RIFTemporaryTablePrefixes.CLEAN_CASTING.getTableName(coreDataSetName);
			String finalCleaningTableName
				= RIFTemporaryTablePrefixes.CLEAN_FINAL.getTableName(coreDataSetName);
			deleteTable(
				connection, 
				logFileWriter,
				finalCleaningTableName);

			
			renameTable(
				connection,
				logFileWriter,
				cleaningCastingTableName,
				finalCleaningTableName);

			exportTable(
				connection, 
				logFileWriter, 
				exportDirectoryPath, 
				RIFDataLoadingResultTheme.STAGES, 
				finalCleaningTableName);			

			
			/*
			addPrimaryKey(
				connection,
				logFileWriter,
				finalCleaningTableName,
				"data_set_id, row_number");
			*/
			
			/*
			 * Part IV: Audit changes that happened between the load table
			 * values and the search and replace values.  Also audit rows
			 * that still failed validation, even after all the cleaning
			 */
			changeAuditManager.auditDataCleaningChanges(
				connection,
				logFileWriter,
				exportDirectoryPath,
				dataSetConfiguration);

			
			changeAuditManager.auditValidationFailures(
				connection,
				logFileWriter,
				exportDirectoryPath,
				dataSetConfiguration);

			//cleanupTemporaryCleanTables(
			//	connection, 
			//	logFileWriter, 
			//	dataSetConfiguration);		
			
			updateLastCompletedWorkState(
				connection,
				logFileWriter,
				dataSetConfiguration,
				WorkflowState.CLEAN);
			
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String cleanedTableName
				= RIFTemporaryTablePrefixes.CLEAN_CASTING.getTableName(
					dataSetConfiguration.getName());
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"cleaningStepManager.error.createCleanedTable",
					cleanedTableName);
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.CLEAN_TABLE, 
					errorMessage);
			throw RIFServiceException;
		}
		finally {
			SQLQueryUtility.close(searchReplaceStatement);
			SQLQueryUtility.close(validationStatement);
			SQLQueryUtility.close(castingStatement);
		}
		
	}
	
	private void cleanupTemporaryCleanTables(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
				
		String coreDataSetName
			= dataSetConfiguration.getName();
		
		String searchReplaceTableName
			= RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE.getTableName(coreDataSetName);

		deleteTable(
			connection, 
			logFileWriter, 
			searchReplaceTableName);
		
		String validationTableName
			= RIFTemporaryTablePrefixes.CLEAN_VALIDATION.getTableName(coreDataSetName);
		deleteTable(
			connection, 
			logFileWriter, 
			validationTableName);
	}
	
	
	public Integer getCleaningTotalBlankValues(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		SQLCountQueryFormatter queryFormatter = new SQLCountQueryFormatter();
		queryFormatter.setCountField("data_source_id");
		queryFormatter.addFromTable("rif_audit_table");
		queryFormatter.addWhereParameter("data_source_id");
		queryFormatter.addWhereParameter("event_type");


		RIFLogger logger = RIFLogger.getLogger();
		
		logger.debugQuery(
			this, 
			"getCleaningTotalBlankValues",
			queryFormatter.generateQuery());

		Integer result = null;
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {			
			int dataSetIdentifier
				= dataSetManager.getDataSetIdentifier(
					connection,
					logFileWriter,
					dataSetConfiguration);
			
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, dataSetIdentifier);
			statement.setString(2, "blank");
			resultSet = statement.executeQuery();
			resultSet.next();
			result = resultSet.getInt(1);
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"getCleaningTotalBlankValues.error.unableToGetTotal",
					dataSetConfiguration.getDisplayName());
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
		
		return result;

	}
		
	public Integer getCleaningTotalChangedValues(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		SQLCountQueryFormatter queryFormatter = new SQLCountQueryFormatter();
		queryFormatter.setCountField("data_source_id");
		queryFormatter.addFromTable("rif_audit_table");
		queryFormatter.addWhereParameter("data_source_id");
		queryFormatter.addWhereParameter("event_type");

		RIFLogger logger = RIFLogger.getLogger();		
		logger.debugQuery(
			this, 
			"getCleaningTotalChangedValues",
			queryFormatter.generateQuery());
				
		Integer result = null;
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			int dataSetIdentifier
				= dataSetManager.getDataSetIdentifier(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, dataSetIdentifier);
			statement.setString(2, "value_changed");			
			resultSet = statement.executeQuery();
			resultSet.next();
			result = resultSet.getInt(1);
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"getCleaningTotalChangedValues.error.unableToGetTotal",
					dataSetConfiguration.getDisplayName());
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
		
		return result;
	}
		
	public Integer getCleaningTotalErrorValues(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
	
		SQLCountQueryFormatter queryFormatter = new SQLCountQueryFormatter();
		queryFormatter.setCountField("data_source_id");
		queryFormatter.addFromTable("rif_audit_table");
		queryFormatter.addWhereParameter("data_source_id");
		queryFormatter.addWhereParameter("event_type");


		RIFLogger logger = RIFLogger.getLogger();		
		logger.debugQuery(
			this, 
			"getCleaningTotalErrorValues",
			queryFormatter.generateQuery());
		
		Integer result = null;
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			int dataSetIdentifier
				= dataSetManager.getDataSetIdentifier(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, dataSetIdentifier);
			statement.setString(2, "error");			
			resultSet = statement.executeQuery();
			resultSet.next();
			result = resultSet.getInt(1);
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"getCleaningTotalErrorValues.error.unableToGetTotal",
					dataSetConfiguration.getDisplayName());
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
		
		return result;
	}
	
	public Boolean cleaningDetectedBlankValue(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException {
				
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("data_source_id");
		queryFormatter.addFromTable("rif_audit_table");
		queryFormatter.addWhereParameter("data_source_id");
		queryFormatter.addWhereParameter("row_number");
		queryFormatter.addWhereParameter("field_name");
		queryFormatter.addWhereParameter("event_type");


		RIFLogger logger = RIFLogger.getLogger();		
		logger.debugQuery(
			this, 
			"cleaningDetectedBlankValue",
			queryFormatter.generateQuery());
		
		Boolean result = null;
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			int dataSetIdentifier
				= dataSetManager.getDataSetIdentifier(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, dataSetIdentifier);
			statement.setInt(2, rowNumber);
			statement.setString(3, targetBaseFieldName);
			statement.setString(4, "blank");
			resultSet = statement.executeQuery();
			result = resultSet.next();			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"cleaningDetectedBlankValue.error.unableToGetStatus",
					String.valueOf(rowNumber),
					targetBaseFieldName,					
					dataSetConfiguration.getDisplayName());
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
		
		return result;
	}
	
	public Boolean cleaningDetectedChangedValue(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException {
	
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("data_source_id");
		queryFormatter.addFromTable("rif_audit_table");
		queryFormatter.addWhereParameter("data_source_id");
		queryFormatter.addWhereParameter("row_number");
		queryFormatter.addWhereParameter("field_name");		
		queryFormatter.addWhereParameter("event_type");
				

		RIFLogger logger = RIFLogger.getLogger();		
		logger.debugQuery(
			this, 
			"cleaningDetectedChangedValue",
			queryFormatter.generateQuery());
		
		Boolean result = null;
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			int dataSetIdentifier
				= dataSetManager.getDataSetIdentifier(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, dataSetIdentifier);
			statement.setInt(2, rowNumber);
			statement.setString(3, targetBaseFieldName);
			statement.setString(4, "value_changed");
			resultSet = statement.executeQuery();
			
			result = resultSet.next();			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"cleaningDetectedChangedValue.error.unableToGetStatus",
					String.valueOf(rowNumber),
					targetBaseFieldName,					
					dataSetConfiguration.getDisplayName());
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
		
		return result;

	}
	
	public Boolean cleaningDetectedErrorValue(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException {
		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("data_source_id");
		queryFormatter.addFromTable("rif_audit_table");
		queryFormatter.addWhereParameter("data_source_id");
		queryFormatter.addWhereParameter("row_number");
		queryFormatter.addWhereParameter("field_name");
		queryFormatter.addWhereParameter("event_type");

		RIFLogger logger = RIFLogger.getLogger();		
		logger.debugQuery(
			this, 
			"cleaningDetectedErrorValue",
			queryFormatter.generateQuery());
		
		Boolean result = null;
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			int dataSetIdentifier
				= dataSetManager.getDataSetIdentifier(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, dataSetIdentifier);
			statement.setInt(2, rowNumber);
			statement.setString(3, targetBaseFieldName);
			statement.setString(4, "error");
			resultSet = statement.executeQuery();
			result = resultSet.next();			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"cleaningDetectedErrorValue.error.unableToGetStatus",
					String.valueOf(rowNumber),
					targetBaseFieldName,					
					dataSetConfiguration.getDisplayName());
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
		
		return result;
	}	
	
	public String[][] getVarianceInFieldData(
		final Connection connection, 
		final DataSetFieldConfiguration dataSetFieldConfiguration) 
		throws RIFServiceException {
		
			
		String fieldOfInterest
			= dataSetFieldConfiguration.getLoadFieldName();
		String coreDataSetName
			= dataSetFieldConfiguration.getCoreFieldName();
		String loadTableName
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(coreDataSetName);

		//KLG: @TODO - eliminate junk data and uncomment code below
		//to make it obtain real data
		String[][] results = new String[50][2];
		for (int i = 0; i < 50; i++) {
			results[i][0] = "value"+ String.valueOf(i);
			results[i][1] = String.valueOf(50 - i);
		}
		
		return results;
		
		/*
		SQLFieldVarianceQueryFormatter queryFormatter
			= new SQLFieldVarianceQueryFormatter();		
		queryFormatter.setFieldOfInterest(fieldOfInterest);		
		queryFormatter.setFromTable(loadTableName);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String[][] results = new String[0][0];
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			resultSet.last();
			
			int numberOfResults = resultSet.getRow();
			results = new String[numberOfResults][2];
			
			resultSet.beforeFirst();
			
			int i = 0;
			while (resultSet.next()) {
				results[i][0] = resultSet.getString(1);
				results[i][1] = resultSet.getString(2);
			}
			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"fieldVarianceDialog.error.unableToObtainVariance",
					loadTableName,
					fieldOfInterest);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED,
					errorMessage);
			
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		return results;
		
		*/
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


