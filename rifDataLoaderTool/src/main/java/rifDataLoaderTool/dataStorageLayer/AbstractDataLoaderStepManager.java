package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifDataLoaderTool.businessConceptLayer.DataLoaderToolSettings;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifDataLoaderTool.businessConceptLayer.RIFDataLoadingResultTheme;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaArea;
//import rifDataLoaderTool.fileFormats.RIFDataLoadingResultTheme;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifGenericLibrary.dataStorageLayer.*;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.text.Collator;
import java.io.*;

/**
 * provides functionality common to all manager classes associated with different steps
 * of RIF data loading.  For now, all manager classes are expected to need a method for
 * return tabular data in a {@link rifServices.businessConceptLayer.RIFResultTable}.
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

abstract class AbstractDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderToolSettings dataLoaderToolSettings;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractDataLoaderStepManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setDataLoaderToolSettings(
		final DataLoaderToolSettings dataLoaderToolSettings) {
		
		this.dataLoaderToolSettings = dataLoaderToolSettings;
	}
	
	protected RIFResultTable getTableData(
		final Connection connection,
		final String tableName,
		final String[] fieldNames)
		throws SQLException,
		RIFServiceException {
				
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
			
		//SELECT field1, field2, fields3...
		for (String fieldName : fieldNames) {
				
			queryFormatter.addSelectField(fieldName);
		}
			
		//FROM
		queryFormatter.addFromTable(tableName);
			
			
		RIFResultTable rifResultTable = new RIFResultTable();
		rifResultTable.setFieldNames(fieldNames);
			
		//gather results
		PreparedStatement statement = null;
		ResultSet resultSet = null;	
		try {
			statement
				= connection.prepareStatement(
					queryFormatter.generateQuery(), 
					ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			//statement = connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			resultSet.last();
			int numberOfRows = resultSet.getRow();
			String[][] tableData = new String[numberOfRows][fieldNames.length];

			ResultSetMetaData resultSetMetaData
				= resultSet.getMetaData();
			
			
			resultSet.beforeFirst();
			int ithRow = 0;
			while (resultSet.next()) {
				for (int i = 0; i < fieldNames.length; i++) {
					int columnType
						= resultSetMetaData.getColumnType(i + 1);
					
					if (columnType == java.sql.Types.INTEGER) {
						tableData[ithRow][i] 
							= String.valueOf(resultSet.getInt(i + 1));						
					}
					else if (columnType == java.sql.Types.DOUBLE) {
						tableData[ithRow][i] 
							= String.valueOf(resultSet.getDouble(i + 1));					
					}
					else if (columnType == java.sql.Types.TIMESTAMP) {
						Timestamp timeStamp = resultSet.getTimestamp(i + 1);
						if (timeStamp == null) {
							tableData[ithRow][i] = "";						
						}
						else {
							Date date = new Date(timeStamp.getTime());
							String datePhrase
								= RIFServiceMessages.getDatePhrase(date);
							tableData[ithRow][i] = datePhrase;
						}
					}
					else {
						//String type
						tableData[ithRow][i] = resultSet.getString(i + 1);						
					}					
				}
				ithRow++;
			}

			rifResultTable.setData(tableData);
				
			return rifResultTable;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
				
	}
	
	public void checkTotalRowsMatch(
		final Connection connection,
		final Writer logFileWriter,
		final String coreTableName,
		final RIFTemporaryTablePrefixes firstTablePrefix,
		final RIFTemporaryTablePrefixes secondTablePrefix) 
		throws RIFServiceException {
		
		String firstTableName
			= firstTablePrefix.getTableName(coreTableName);
		String secondTableName
			= secondTablePrefix.getTableName(coreTableName);
		
		SQLGeneralQueryFormatter queryFormatter 
			= new SQLGeneralQueryFormatter();
		queryFormatter.addQueryPhrase(0, "WITH");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(" firstTableCount AS");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "(SELECT");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "COUNT(data_set_id) AS total");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "FROM");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, firstTableName);
		queryFormatter.addQueryPhrase("),");
		queryFormatter.finishLine();
		
		queryFormatter.addQueryPhrase(" secondTableCount AS");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "(SELECT");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "COUNT(data_set_id) AS total");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "FROM");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, secondTableName);
		queryFormatter.addQueryPhrase(")");
		queryFormatter.padAndFinishLine();
		
		//now compare the two results
		queryFormatter.addQueryPhrase(0, "SELECT");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "CASE");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "WHEN ");
		queryFormatter.addQueryPhrase("firstTableCount.total = secondTableCount.total THEN TRUE");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "ELSE FALSE");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "END AS result");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "FROM");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "firstTableCount,");
		queryFormatter.finishLine();
		queryFormatter.addQueryPhrase(2, "secondTableCount");
		
		RIFLogger logger = RIFLogger.getLogger();
		logger.debugQuery(
			this, 
			"checkTotalRowsMatch",
			queryFormatter.generateQuery());
		
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			resultSet.next();
			Boolean result = resultSet.getBoolean(1);
			if (result == false) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"tableIntegrityChecker.error.tablesHaveDifferentSizes",
						firstTableName,
						secondTableName);
				RIFServiceException rifDataLoaderException
					= new RIFServiceException(
						RIFDataLoaderToolError.COMPARE_TABLE_SIZES, 
						errorMessage);
				throw rifDataLoaderException;
			}
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage	
				= RIFDataLoaderToolMessages.getMessage("tableIntegrityChecker.error.unableToCompareTables");
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
	
	protected void addComments(
		final Connection connection,
		final Writer logFileWriter,
		final String targetTable,
		final DataSetConfiguration dataSetConfiguration,
		final WorkflowState workflowState) 
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		try {
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "COMMENT ON TABLE ");
			queryFormatter.addQueryPhrase(targetTable);
			queryFormatter.addQueryPhrase(" IS ");
			queryFormatter.addQueryPhrase("'");
			queryFormatter.addQueryPhrase(dataSetConfiguration.getDescription());
			queryFormatter.addQueryPhrase("'");
			
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.executeUpdate();

			//add data_set_id, row_number
			addRIFGeneratedFieldDescriptions(
				connection, 
				logFileWriter,
				targetTable,
				dataSetConfiguration,
				workflowState);
			
			RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
			ArrayList<DataSetFieldConfiguration> fieldConfigurations
				= dataSetConfiguration.getRequiredAndExtraFieldConfigurations();
			for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
				
				if (excludeFieldFromConsideration(
					rifSchemaArea, 
					fieldConfiguration,
					workflowState) == false) {

					addCommentToTableField(
						connection,
						logFileWriter,
						targetTable,
						fieldConfiguration,
						workflowState);
				}
			}
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"abstractDataLoaderStepManager.error.unableToAddComments",
					targetTable);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}		
		finally {
			SQLQueryUtility.close(statement);
		}
	}

	private void addRIFGeneratedFieldDescriptions(
		final Connection connection, 
		final Writer logFileWriter,
		final String targetTable,
		final DataSetConfiguration dataSetConfiguration,
		final WorkflowState workflowState) 
		throws SQLException, 
		RIFServiceException {
		
		String rowNumberFieldDescription
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.rowNumber.description");

		addCommentToTableField(
			connection,
			logFileWriter,
			targetTable,
			"row_number",
			rowNumberFieldDescription);

		String dataSetIdentifierFieldDescription
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.dataSetIdentifier.description");
		addCommentToTableField(
			connection,
			logFileWriter,
			targetTable,
			"data_set_id",
			dataSetIdentifierFieldDescription);
		
		RIFSchemaArea rifSchemaArea
			= dataSetConfiguration.getRIFSchemaArea();
		if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
			(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
			
			String ageSexGroupFieldDescription
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.ageSexGroup.description");
			addCommentToTableField(
				connection,
				logFileWriter,
				targetTable,
				"age_sex_group",
				ageSexGroupFieldDescription);			
		}
		
		if (workflowState.getStateSequenceNumber() >= WorkflowState.CHECK.getStateSequenceNumber()) {
			
			String keepDescription
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.keep.description");
			addCommentToTableField(
				connection,
				logFileWriter,
				targetTable,
				"keep_record",
				keepDescription);			
			
		}
		
	}
	
	private boolean excludeFieldFromConsideration(
		final RIFSchemaArea rifSchemaArea,
		final DataSetFieldConfiguration dataSetFieldConfiguration,
		final WorkflowState workflowState) {
		
		if (workflowState.getStateSequenceNumber() >= WorkflowState.CONVERT.getStateSequenceNumber()) {
			String convertFieldName = dataSetFieldConfiguration.getConvertFieldName();
			
			Collator collator = RIFDataLoaderToolMessages.getCollator();			
			if (collator.equals(convertFieldName, "age") ||
			   collator.equals(convertFieldName, "sex")) {
				
				return true;
			}
		}
		
		return false;
	}
	
	private void addCommentToTableField(
		final Connection connection,
		final Writer logFileWriter,
		final String targetTable,
		final DataSetFieldConfiguration dataSetFieldConfiguration,
		final WorkflowState workflowState)
		throws SQLException,
		RIFServiceException {
		
		String fieldName = "";
		if (workflowState == WorkflowState.EXTRACT) {
			fieldName = dataSetFieldConfiguration.getLoadFieldName();
		}
		else if (workflowState == WorkflowState.CLEAN) {
			fieldName = dataSetFieldConfiguration.getCleanFieldName();
		}
		else {
			fieldName = dataSetFieldConfiguration.getConvertFieldName();				
		}
		addCommentToTableField(
			connection,
			logFileWriter,
			targetTable,
			fieldName,
			dataSetFieldConfiguration.getCoreFieldDescription());
	}
	
	private void addCommentToTableField(
		final Connection connection,
		final Writer logFileWriter,
		final String targetTable,
		final String fieldName,
		final String fieldDescription)
		throws SQLException,
		RIFServiceException {
		
		PreparedStatement statement = null;
		try {
			//Note: we're trying to comment a field name but that field may
			//have different names depending on the stage of the work flow
			//we've just completed.  This could happen if, for example,
			//we've loaded a CSV file with no header and then try to assign
			//field names we'd like to give the auto-named fields (eg: field1
			//could become year)
						
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "COMMENT ON COLUMN ");
			queryFormatter.addQueryPhrase(targetTable);
			queryFormatter.addQueryPhrase(".");
			queryFormatter.addQueryPhrase(fieldName);
			
			queryFormatter.addQueryPhrase(" IS ");
			queryFormatter.addQueryPhrase("'");
			queryFormatter.addQueryPhrase(fieldDescription);
			queryFormatter.addQueryPhrase("'");
			
			
			logSQLQuery(
				logFileWriter,
				"addCommentToTableField", 
				queryFormatter);
			
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.executeUpdate();			
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
	}
	
	protected void addPrimaryKey(
		final Connection connection,
		final Writer logFileWriter,
		final String targetTableName,
		final String primaryKeyFieldPhrase)
		throws RIFServiceException {
		
		SQLGeneralQueryFormatter queryFormatter
			= new SQLGeneralQueryFormatter();
		PreparedStatement statement = null;		
		try {
			queryFormatter.addQueryPhrase("ALTER TABLE ");
			queryFormatter.addQueryPhrase(targetTableName);
			queryFormatter.addQueryPhrase(" ADD PRIMARY KEY (");
			queryFormatter.addQueryPhrase(primaryKeyFieldPhrase);
			queryFormatter.addQueryPhrase(")");
			
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.executeUpdate();		
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"abstractDataLoaderStepManager.error.unableToAddPrimaryKeys",
					targetTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}		
		
		
		
	}
	
	
	
	
	protected void exportTable(
		final Connection connection, 
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final RIFDataLoadingResultTheme rifDataLoaderResultTheme,
		final String tableName) 
		throws RIFServiceException {
				
		StringBuilder exportFileName = new StringBuilder();
		exportFileName.append(exportDirectoryPath);
		exportFileName.append(File.separator);
		exportFileName.append(rifDataLoaderResultTheme.getSubDirectoryName());		
		exportFileName.append(File.separator);
		exportFileName.append(tableName);
		exportFileName.append(".csv");
		BufferedWriter writer = null;		
		try {
			SQLExportTableToCSVQueryFormatter queryFormatter
				= new SQLExportTableToCSVQueryFormatter();
			queryFormatter.setTableToExport(tableName);
			queryFormatter.setOutputFileName(exportFileName.toString());
			
			writer = new BufferedWriter(new FileWriter(exportFileName.toString()));		
			
			CopyManager copyManager = new CopyManager((BaseConnection) connection);
			copyManager.copyOut(queryFormatter.generateQuery(), writer);
			writer.flush();
		}
		catch(Exception exception) {
			logException(
				logFileWriter,
				exception);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"abstractDataLoaderStepManager.error.unableToExportTable",
					tableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			if (writer != null) {
				try {
					writer.close();					
				}
				catch(IOException exception) {
					String errorMessage
						= RIFDataLoaderToolMessages.getMessage(
							"abstractDataLoaderStepManager.error.unableToExportTable");
					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
							errorMessage);
					throw rifServiceException;
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	protected void deleteTable(
		final Connection connection,
		final Writer logFileWriter,
		final String targetTableName) 
		throws RIFServiceException {
		
		SQLDeleteTableQueryFormatter queryFormatter 
			= new SQLDeleteTableQueryFormatter();
		queryFormatter.setTableToDelete(targetTableName);
		PreparedStatement statement = null;
		try {
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.executeUpdate();		
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"abstractDataLoaderStepManager.error.unableToDeleteTable",
					targetTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}		
	}

	protected void renameTable(
		final Connection connection,
		final Writer logFileWriter,
		final String sourceTableName,
		final String destinationTableName) 
		throws RIFServiceException {
			
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		PreparedStatement statement = null;
		try {
			queryFormatter.addQueryPhrase(0, "ALTER TABLE ");
			queryFormatter.addQueryPhrase(sourceTableName);
			queryFormatter.addQueryPhrase(" RENAME TO ");
			queryFormatter.addQueryPhrase(destinationTableName);
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.executeUpdate();			
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter, 
				sqlException);
			logSQLQuery(
				logFileWriter,
				"renameTable", 
				queryFormatter);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"abstractDataLoaderStepManager.error.unableToRenameTable",
					sourceTableName,
					destinationTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
	}
	
	protected void copyTable(
		final Connection connection,
		final Writer logFileWriter,
		final String sourceTableName,
		final String destinationTableName) 
		throws RIFServiceException {
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		PreparedStatement statement = null;
		try {
			queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
			
			queryFormatter.addQueryPhrase(destinationTableName);
			queryFormatter.addQueryPhrase(" AS ");
			queryFormatter.finishLine();
			queryFormatter.addQueryPhrase("SELECT * FROM ");
			queryFormatter.addQueryPhrase(sourceTableName);
			
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.executeUpdate();			
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter, 
				sqlException);
			logSQLQuery(
				logFileWriter,
				"createOptimiseTable", 
				queryFormatter);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"abstractDataLoaderStepManager.error.unableToCopyTable",
					sourceTableName,
					destinationTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}

	}

	protected void updateLastCompletedWorkState(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration,
		final WorkflowState workFlowState) 
		throws RIFServiceException {
				
		SQLUpdateQueryFormatter queryFormatter = new SQLUpdateQueryFormatter();
		PreparedStatement statement = null;
		try {
			
			queryFormatter.setUpdateTable("data_set_configurations");
			queryFormatter.addUpdateField("current_workflow_state");
			queryFormatter.addWhereParameter("core_data_set_name");
			
			logSQLQuery(
				logFileWriter,
				"update state", 
				queryFormatter);
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);

			statement.setString(
				1,
				workFlowState.getCode());
			statement.setString(
				2, 
				dataSetConfiguration.getName());
	
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter, 
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.unableToUpdateLastCompletedState",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException 
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}		
	}
	
	protected PreparedStatement createPreparedStatement(
		final Connection connection,
		final AbstractSQLQueryFormatter queryFormatter) 
		throws SQLException {
				
		return SQLQueryUtility.createPreparedStatement(
			connection,
			queryFormatter);

	}
		

	protected void logSQLQuery(
		final Writer logFileWriter,
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final String... parameters) 
		throws RIFServiceException {

		StringBuilder queryLog = new StringBuilder();
		queryLog.append("==========================================================\n");
		queryLog.append("QUERY NAME:");
		queryLog.append(queryName);
		queryLog.append("\n");
		
		queryLog.append("PARAMETERS:");
		queryLog.append("\n");
		for (int i = 0; i < parameters.length; i++) {
			queryLog.append("\t");
			queryLog.append(i + 1);
			queryLog.append(":\"");
			queryLog.append(parameters[i]);
			queryLog.append("\"\n");			
		}
		queryLog.append("\n");
		queryLog.append("SQL QUERY TEXT\n");
		queryLog.append(queryFormatter.generateQuery());
		queryLog.append("\n");
		queryLog.append("==========================================================\n");
		
		try {
			logFileWriter.write(queryLog.toString());	
			logFileWriter.flush();
		}
		catch(IOException ioException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.unableToWriteToFile");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.UNABLE_TO_WRITE_FILE, 
					errorMessage);
			throw rifServiceException;
		}
	}


	protected void logSQLQuery(
		final Writer logFileWriter,
		final String queryName,
		final String queryText) 
		throws RIFServiceException {

		StringBuilder queryLog = new StringBuilder();
		
		queryLog.append("==========================================================\n");
		queryLog.append("QUERY NAME:");
		queryLog.append(queryName);
		queryLog.append("\n");
		queryLog.append(queryText);
		try {
			logFileWriter.write(queryLog.toString());	
			logFileWriter.flush();
		}
		catch(IOException ioException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.unableToWriteToFile");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.UNABLE_TO_WRITE_FILE, 
					errorMessage);
			throw rifServiceException;
		}
	}
	
	
	protected void logSQLException(
		final Writer logFileWriter,
		final SQLException sqlException) {

		sqlException.printStackTrace(System.out);

		if (logFileWriter == null) {
			//sqlException.printStackTrace(System.out);
		}
		else {
			//PrintWriter printWriter = new PrintWriter(logFileWriter);
			//sqlException.printStackTrace(printWriter);
			//printWriter.flush();
		}

	}

	protected void logException(
		final Writer logFileWriter,
		final Exception exception) {


		if (logFileWriter == null) {
			exception.printStackTrace(System.out);
		}
		else {		
			PrintWriter printWriter = new PrintWriter(logFileWriter);
			exception.printStackTrace(printWriter);
			printWriter.flush();
		}
	}
	
	protected void setAutoCommitOn(
		final Connection connection,
		final boolean isAutoCommitOn)
		throws RIFServiceException {
		
		try {
			connection.setAutoCommit(isAutoCommitOn);			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage("general.db.error.unableToSetCommit");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_TO_ADJUST_AUTO_COMMIT,
					errorMessage);
			throw rifServiceException;
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


