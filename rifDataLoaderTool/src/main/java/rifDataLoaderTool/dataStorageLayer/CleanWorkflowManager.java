package rifDataLoaderTool.dataStorageLayer;


import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowQueryGeneratorAPI;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifGenericLibrary.dataStorageLayer.SQLCountQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.SQLQueryUtility;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.util.RIFLogger;

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
	private CleanWorkflowQueryGeneratorAPI queryGenerator;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleanWorkflowManager(
		final RIFDataLoaderStartupOptions startupOptions,
		final CleanWorkflowQueryGeneratorAPI queryGenerator) {

		super(startupOptions);
		this.queryGenerator = queryGenerator;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================


	
	public RIFResultTable getCleanedTableData(
		final Connection connection,
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
			sqlException.printStackTrace(System.out);
		}
		finally {
			SQLQueryUtility.close(connection);
		}
		return resultTable;
	}
	
	
	
	public void createCleanedTable(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		RIFLogger logger = RIFLogger.getLogger();
		
		String coreDataSetName
			= dataSetConfiguration.getName();
		
		String dropSearchReplaceTableQuery
			= queryGenerator.generateDropSearchReplaceTableQuery(dataSetConfiguration);	
				
		PreparedStatement dropSearchReplaceTableStatement = null;
		
		String createSearchReplaceTableQuery
			= queryGenerator.generateSearchReplaceTableQuery(dataSetConfiguration);
		PreparedStatement createSearchReplaceTableStatement = null;
		
		PreparedStatement dropValidationTableStatement = null;
		String dropValidationTableQuery
			= queryGenerator.generateDropValidationTableQuery(dataSetConfiguration);
		
		PreparedStatement createValidationTableStatement = null;
		String createValidationTableQuery
			= queryGenerator.generateValidationTableQuery(dataSetConfiguration);
		
		
		PreparedStatement dropCastingTableStatement = null;
		String dropCastingTableQuery
			= queryGenerator.generateDropCastingTableQuery(dataSetConfiguration);
		System.out.println(dropCastingTableQuery);
		
		PreparedStatement createCastingTableStatement = null;
		String createCastingTableQuery
			= queryGenerator.generateCastingTableQuery(dataSetConfiguration);
		
		PreparedStatement deleteAuditChangesStatement = null;
		String deleteAuditChangesQuery
			= queryGenerator.generateDeleteAuditsQuery(dataSetConfiguration);
		
		PreparedStatement createAuditChangesStatement = null;
		String createAuditChangesQuery
			= queryGenerator.generateAuditChangesQuery(dataSetConfiguration);
		PreparedStatement createAuditErrorsStatement = null;
		String createAuditErrorsQuery
			= queryGenerator.generateAuditErrorsQuery(dataSetConfiguration);
		PreparedStatement createAuditBlanksStatement = null;
		String createAuditBlanksQuery
			= queryGenerator.generateAuditBlanksQuery(dataSetConfiguration);
		
		
		try {

			/*
			 * Delete old tables so we can recreate them.
			 */

			logger.debugQuery(
				this, 
				"createCleanedTable",
				dropSearchReplaceTableQuery);
			dropSearchReplaceTableStatement
				= connection.prepareStatement(dropSearchReplaceTableQuery);
			dropSearchReplaceTableStatement.executeUpdate();

			logger.debugQuery(
				this, 
				"createCleanedTable",
				dropValidationTableQuery);
			dropValidationTableStatement
				= connection.prepareStatement(dropValidationTableQuery);
			dropValidationTableStatement.executeUpdate();
			

			logger.debugQuery(
				this, 
				"createCleanedTable",
				dropCastingTableQuery);
			dropCastingTableStatement
				= connection.prepareStatement(dropCastingTableQuery);
			dropCastingTableStatement.executeUpdate();
			
			//drop previous rows in the provenance table that relate to the/
			//data source

			logger.debugQuery(
				this, 
				"createCleanedTable",
				deleteAuditChangesQuery);
			deleteAuditChangesStatement
				= connection.prepareStatement(deleteAuditChangesQuery);
			deleteAuditChangesStatement.setInt(1, 
				Integer.valueOf(dataSetConfiguration.getIdentifier()));			
			deleteAuditChangesStatement.executeUpdate();
			
			//First, create the table that does substitution activities

			logger.debugQuery(
				this, 
				"createCleanedTable",
				createSearchReplaceTableQuery);
			createSearchReplaceTableStatement
				= connection.prepareStatement(createSearchReplaceTableQuery);
			createSearchReplaceTableStatement.executeUpdate();

			//check that the search replace table has just as many rows as the
			//original load table			
			checkTotalRowsMatch(
				connection, 
				coreDataSetName,
				RIFTemporaryTablePrefixes.LOAD,
				RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE);			
			
			
			//Second, do a validation pass which will either leave a table cell
			//value unchanged or with 'rif_error' in it

			logger.debugQuery(
				this, 
				"createCleanedTable",
				createValidationTableQuery);
			createValidationTableStatement
				= connection.prepareStatement(createValidationTableQuery);
			createValidationTableStatement.executeUpdate();

			
			//check that the validation table has just as many rows as the
			//search replace table			
			checkTotalRowsMatch(
				connection, 
				coreDataSetName,
				RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE,
				RIFTemporaryTablePrefixes.CLEAN_VALIDATION);			
			
			//Third, create the table that does casting operations

			logger.debugQuery(
				this, 
				"createCleanedTable",
				createCastingTableQuery);
			createCastingTableStatement
				= connection.prepareStatement(createCastingTableQuery);
			createCastingTableStatement.executeUpdate();

			//check that the casted table has just as many rows as the
			//cleaned validated data			
			checkTotalRowsMatch(
				connection, 
				coreDataSetName,
				RIFTemporaryTablePrefixes.CLEAN_VALIDATION,
				RIFTemporaryTablePrefixes.CLEAN_CASTING);			
			
			
			//Fourth, add audit trail messages for errors, changed values and blanks

			logger.debugQuery(
				this, 
				"createCleanedTable",
				createAuditChangesQuery);
			createAuditChangesStatement
				= connection.prepareStatement(createAuditChangesQuery);
			createAuditChangesStatement.executeUpdate();
			

			logger.debugQuery(
				this, 
				"createCleanedTable",
				createAuditErrorsQuery);
			createAuditErrorsStatement
				= connection.prepareStatement(createAuditErrorsQuery);
			createAuditErrorsStatement.executeUpdate();
			

			logger.debugQuery(
				this, 
				"createCleanedTable",
				createAuditBlanksQuery);
			createAuditBlanksStatement
				= connection.prepareStatement(createAuditBlanksQuery);
			createAuditBlanksStatement.executeUpdate();			
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
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
			SQLQueryUtility.close(dropSearchReplaceTableStatement);
			SQLQueryUtility.close(createSearchReplaceTableStatement);
			SQLQueryUtility.close(dropCastingTableStatement);
			SQLQueryUtility.close(createCastingTableStatement);
			SQLQueryUtility.close(createCastingTableStatement);
		}
		
	}
	
	public Integer getCleaningTotalBlankValues(
		final Connection connection,
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
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(dataSetConfiguration.getIdentifier()));
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
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(dataSetConfiguration.getIdentifier()));
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
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(dataSetConfiguration.getIdentifier()));
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
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(dataSetConfiguration.getIdentifier()));
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
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(dataSetConfiguration.getIdentifier()));
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
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(dataSetConfiguration.getIdentifier()));
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
			= RIFTemporaryTablePrefixes.LOAD.getTableName(coreDataSetName);

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


