package org.sahsu.rif.dataloader.datastorage.ms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolSettings;
import org.sahsu.rif.dataloader.concepts.DataLoadingResultTheme;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.datastorage.QueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.ms.MSSQLDeleteTableQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLSchemaCommentQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLSelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLUpdateQueryFormatter;
import org.sahsu.rif.generic.datastorage.pg.PGSQLSelectQueryFormatter;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;
import org.sahsu.rif.generic.util.RIFLogger;

/**
 * provides functionality common to all manager classes associated with different steps
 * of RIF data loading.  For now, all manager classes are expected to need a method for
 * return tabular data in a {@link RIFResultTable}.
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

abstract class AbstractMSSQLDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderToolSettings dataLoaderToolSettings;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractMSSQLDataLoaderStepManager() {

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
				
		SelectQueryFormatter queryFormatter
			= new MSSQLSelectQueryFormatter();
		//queryFormatter.setDatabaseSchemaName("dbo");//KLG_SCHEMA
		//SELECT field1, field2, fields3...
		for (String fieldName : fieldNames) {
				
			queryFormatter.addSelectField(fieldName);
		}
			
		//FROM
		queryFormatter.addFromTable(tableName);
			
			
		RIFResultTable rifResultTable = new RIFResultTable();
		rifResultTable.setColumnProperties(fieldNames);
			
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
								= GENERIC_MESSAGES.getDatePhrase(date);
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
		queryFormatter.addQueryPhrase(2, "");//KLG_SCHEMA
		queryFormatter.addQueryPhrase(firstTableName);
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
		queryFormatter.addQueryPhrase(2, "");//KLG_SCHEMA
		queryFormatter.addQueryPhrase(secondTableName);
		queryFormatter.addQueryPhrase(")");
		queryFormatter.padAndFinishLine();
		
		//now compare the two results
		queryFormatter.addQueryPhrase(0, "SELECT");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "CASE");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "WHEN ");
		queryFormatter.addQueryPhrase("firstTableCount.total = secondTableCount.total THEN 1");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "ELSE 0");
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

			MSSQLSchemaCommentQueryFormatter queryFormatter
				= new MSSQLSchemaCommentQueryFormatter();
			queryFormatter.setTableComment(
				targetTable,
				dataSetConfiguration.getDescription());
			queryFormatter.setDatabaseSchemaName("dbo");
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			
			//System.out.println(queryFormatter.toString());
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
				= DataSetConfigurationUtility.getRequiredAndExtraFieldConfigurations(
					dataSetConfiguration);
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
			
			Collator collator = GENERIC_MESSAGES.getCollator();
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
		final String columnName,
		final String comment)
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
			
			MSSQLSchemaCommentQueryFormatter queryFormatter
				= new MSSQLSchemaCommentQueryFormatter();
			//KLG_SCHEMA
			queryFormatter.setDatabaseSchemaName("dbo");
			queryFormatter.setTableColumnComment(
				targetTable, 
				columnName, 
				comment);
			
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
			queryFormatter.addQueryPhrase("ALTER TABLE ");//KLG_SCHEMA
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
	
	
	protected File createExportTableFile(
		final String exportDirectoryPath,
		final DataLoadingResultTheme rifDataLoaderResultTheme,
		final String tableName) {
		
		StringBuilder exportFileName = new StringBuilder();
		exportFileName.append(exportDirectoryPath);
		exportFileName.append(File.separator);
		if (rifDataLoaderResultTheme != DataLoadingResultTheme.MAIN_RESULTS) {
			exportFileName.append(rifDataLoaderResultTheme.getSubDirectoryName());		
			exportFileName.append(File.separator);
		}
		exportFileName.append(tableName);
		exportFileName.append(".csv");
		
		File file = new File(exportFileName.toString());
		return file;
	}
	
	protected void exportTable(
		final Connection connection, 
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataLoadingResultTheme rifDataLoaderResultTheme,
		final String tableName) 
		throws RIFServiceException {
				
		StringBuilder exportFileName = new StringBuilder();
		exportFileName.append(exportDirectoryPath);
		exportFileName.append(File.separator);
		if (rifDataLoaderResultTheme != DataLoadingResultTheme.MAIN_RESULTS) {
			exportFileName.append(rifDataLoaderResultTheme.getSubDirectoryName());		
			exportFileName.append(File.separator);
		}

		exportFileName.append(tableName);
		exportFileName.append(".csv");
		
		File exportTableFile
			= createExportTableFile(
				exportDirectoryPath,
				rifDataLoaderResultTheme,
				tableName);
						
		BufferedWriter writer = null;		
        FileWriter cname = null;
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		try {
			
			cname = new FileWriter(exportFileName.toString());
			SelectQueryFormatter selFormatter = new  MSSQLSelectQueryFormatter();
			selFormatter.addSelectField("*");
			selFormatter.addFromTable(tableName);
			statement = createPreparedStatement(connection, selFormatter);
			//statement.setString(1, dataSetConfiguration.getName());
			//statement.setString(2, dataSetConfiguration.getVersion());
			resultSet = statement.executeQuery();
			
            ResultSetMetaData rsmd = resultSet.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            	// Not interested in the data_set_id, row_number or keep_record fields
            	String colName = rsmd.getColumnName(i);
            	if (colName.equals("data_set_id") == false && colName.equals("row_number") == false && colName.equals("keep_record") == false)
            	{
	                cname.append(rsmd.getColumnName(i));
	                if (i < rsmd.getColumnCount()) {
	                	cname.append(",");
	                }
	               cname.flush();
            	}
            }
            cname.append("\n");

            // WRITE DATA
            while (resultSet.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                	
                	// Not interested in the data_set_id, row_number or keep_record fields
                	String colName = rsmd.getColumnName(i);
                	if (colName.equals("data_set_id") == false && colName.equals("row_number") == false && colName.equals("keep_record") == false)
                	{
	                    if (resultSet.getObject(i) != null) {
	                         cname.append(resultSet.getObject(i).toString());
	                    } else {
	                        cname.append("null");
	                    }
	                    if (i < rsmd.getColumnCount()) {
	                    	cname.append(",");
	                    }
                	}
                }
                //new line entered after each row
                cname.append("\n");


            }
			
			
			//MSSQLExportTableToCSVQueryFormatter queryFormatter
			//	= new MSSQLExportTableToCSVQueryFormatter(false);
			//queryFormatter.setTableToExport(tableName);
			//queryFormatter.setOutputFileName(exportFileName.toString());
			
			//writer = new BufferedWriter(new FileWriter(exportTableFile));		
			

			//CopyManager copyManager = new CopyManager((BaseConnection) connection);
			//copyManager.copyOut(queryFormatter.generateQuery(), writer);
			//writer.flush();
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
			try {
	           if (cname != null) {
	                cname.flush();
	                cname.close();
	            }
	            if (resultSet != null) {
	            	resultSet.close();
	            }
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
		
		System.out.println("deleteTable 1");
		printAnything(connection);
		System.out.println("deleteTable 2");
		
		MSSQLDeleteTableQueryFormatter queryFormatter 
			= new MSSQLDeleteTableQueryFormatter();
		//KLG_SCHEMA
		queryFormatter.setDatabaseSchemaName("dbo");
		System.out.println("TAble name=="+ targetTableName + "==");
		queryFormatter.setTableToDelete(targetTableName);
		PreparedStatement statement = null;
		try {
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			System.out.println("ZZZZZZZZZZZZZZZ BEGIN");
			System.out.println(queryFormatter.generateQuery());
			System.out.println("ZZZZZZZZZZZZZZZ END");
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

	private void printAnything(final Connection connection) {
		System.out.println("Print anything");
		PGSQLSelectQueryFormatter queryFormatter 
			= new PGSQLSelectQueryFormatter();
		queryFormatter.setEndWithSemiColon(true);
		queryFormatter.addFromTable("rif_failed_val_log");
		queryFormatter.addSelectField("data_set_id");
		queryFormatter.addSelectField("row_number");
		queryFormatter.addSelectField("field_name");
		queryFormatter.addSelectField("invalid_value");
		
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		
		try {
			System.out.println("printanything 1-1");
			System.out.println(queryFormatter.generateQuery());
			System.out.println("printanything 1-2");

			statement
				= connection.prepareStatement(queryFormatter.generateQuery());
			System.out.println("printanything 2");
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				System.out.println("printanything 3");
				
				System.out.println("DS=="+ resultSet.getInt(1) + "==");
				System.out.println("Row=="+ resultSet.getInt(2) + "==");
				System.out.println("Field Name=="+ resultSet.getString(3) + "==");
				System.out.println("Invalid Field Name=="+ resultSet.getString(4) + "==");
			}
			System.out.println("printanything 4");
		}
		catch(SQLException exception) {
			exception.printStackTrace(System.out);
		}		
		finally {
			
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
			queryFormatter.addQueryPhrase(0, "exec sp_rename ");//KLG_SCHEMA
			queryFormatter.addQueryPhrase(sourceTableName);
			queryFormatter.addQueryPhrase(", ");
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
			queryFormatter.addQueryPhrase("SELECT * INTO ");//KLG_SCHEMA
			queryFormatter.addQueryPhrase(destinationTableName);
			queryFormatter.addQueryPhrase(" FROM ");
			queryFormatter.finishLine();
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
				
		MSSQLUpdateQueryFormatter queryFormatter 
			= new MSSQLUpdateQueryFormatter();
		//KLG_SCHEMA
		//queryFormatter.setDatabaseSchemaName("dbo");
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
		final QueryFormatter queryFormatter)
		throws SQLException {
				
		return SQLQueryUtility.createPreparedStatement(
			connection,
			queryFormatter);
	}
		
	protected PreparedStatement createPreparedStatement(
		final Connection connection,
		final String query) 
		throws SQLException {
				
		return SQLQueryUtility.createPreparedStatement(
			connection,
			query);
	}
	
	

	protected void logSQLQuery(
		final Writer logFileWriter,
		final String queryName,
		final QueryFormatter queryFormatter,
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
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createUnableToChangeDBCommitException();
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


