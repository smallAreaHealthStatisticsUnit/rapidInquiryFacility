package org.sahsu.rif.dataloader.datastorage.ms;

import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataLoadingResultTheme;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.ms.MSSQLCreateTableQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLDeleteTableQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLInsertQueryFormatter;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 *
 * Manages database operations associated with loading a new data source into the RIF.
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

final class MSSQLExtractWorkflowManager 
	extends AbstractMSSQLDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int TEXT_FIELD_WIDTH = 30;
	
	// ==========================================
	// Section Properties
	// ==========================================
	private MSSQLDataSetManager dataSetManager;


	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLExtractWorkflowManager(
		final MSSQLDataSetManager dataSetManager) {

		this.dataSetManager = dataSetManager;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public RIFResultTable getExtractTableData(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		RIFResultTable resultTable = new RIFResultTable();
		String coreTableName = dataSetConfiguration.getName();
		String loadTableName
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(coreTableName);
		String[] fieldNames = dataSetConfiguration.getLoadFieldNames();
		
		try {
			resultTable 
				= getTableData(connection, loadTableName, fieldNames);
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
	
	public void extractConfiguration(
		final Connection connection,				
		final Writer logFileWriter, 
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		String coreDataSetName 
			= dataSetConfiguration.getName();

		String targetExtractTable
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(coreDataSetName);
		deleteTable(
			connection, 
			logFileWriter,
			targetExtractTable);
		
		int dataSetIdentifier
			= dataSetManager.updateDataSetRegistration(
				connection, 
				logFileWriter,
				dataSetConfiguration);
		
		PreparedStatement createExtractTableStatement = null;
		try {
			
			//drop the table if it already exists so we can recreate it
			//without raising a 'table already exists' exception
			MSSQLCreateTableQueryFormatter createExtractTableQueryFormatter
				 = new MSSQLCreateTableQueryFormatter();
			//KLG_SCHEMA
			//createExtractTableQueryFormatter.setDatabaseSchemaName("dbo");
			createExtractTableQueryFormatter.setTextFieldLength(TEXT_FIELD_WIDTH);
			createExtractTableQueryFormatter.setTableName(targetExtractTable);

			ArrayList<DataSetFieldConfiguration> fieldConfigurations
				= dataSetConfiguration.getFieldConfigurations();
			
			for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
				createExtractTableQueryFormatter.addTextFieldDeclaration(
					fieldConfiguration.getLoadFieldName(), 
					true);
			}
			
			logSQLQuery(
				logFileWriter,
				"create_extract_table", 
				createExtractTableQueryFormatter);
			
			System.out.println("YYYYYYYYYYYYYYY START");
			System.out.println(createExtractTableQueryFormatter.generateQuery());
			System.out.println("YYYYYYYYYYYYYYY END");
			createExtractTableStatement
				= connection.prepareStatement(createExtractTableQueryFormatter.generateQuery());
			createExtractTableStatement.executeUpdate();
			
			
			//now import the data from the CSV file
			importCSVFile(
				connection,
				logFileWriter,
				dataSetConfiguration.getFilePath(),
				targetExtractTable);
			
			addDataSourceIdentifierField(
				connection,
				logFileWriter,
				targetExtractTable,
				dataSetIdentifier);
			addOriginalRowNumbers(
				connection, 
				logFileWriter,
				targetExtractTable);
	
			addPrimaryKey(
				connection,
				logFileWriter,
				targetExtractTable,
				"data_set_id, row_number");
			
			exportTable(
					connection,
					logFileWriter,
					exportDirectoryPath,
					DataLoadingResultTheme.ARCHIVE_STAGES,
					targetExtractTable);
			
			updateLastCompletedWorkState(
					connection,
					logFileWriter,
					dataSetConfiguration,
					WorkflowState.EXTRACT);
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			logSQLException(
				logFileWriter,
				sqlException);
			String createTableName
				= RIFTemporaryTablePrefixes.EXTRACT.getTableName(
						dataSetConfiguration.getName());
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"extractWorkflowManager.error.createExtractTable",
					createTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.LOAD_TABLE,
					errorMessage);
			throw rifServiceException;
		}
		finally {			
			SQLQueryUtility.close(createExtractTableStatement);
		}	
	}
	
	private void importCSVFile(
		final Connection connection,
		final Writer logFileWriter,
		final String csvFilePath,
		final String destinationTableName)
		throws RIFServiceException {
		
		
		try {
			
			PreparedStatement bulkInsert = null;
			
			SQLGeneralQueryFormatter queryFormatter
				= new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "BULK INSERT ");
			queryFormatter.addQueryPhrase(destinationTableName);
			queryFormatter.addQueryPhrase(" FROM '");
			queryFormatter.addQueryPhrase(csvFilePath);
			queryFormatter.addQueryPhrase("' WITH ( FIRSTROW = 2, FIELDTERMINATOR = ',', ROWTERMINATOR = '\\n', TABLOCK)");
			// by specifying FIRSTROW = 2 we skip the header row
		
			System.out.println(queryFormatter.generateQuery());
			
			bulkInsert = createPreparedStatement(
				connection, 
				queryFormatter);
			bulkInsert.executeUpdate();
			/*
			 * #POSSIBLE_PORTING_ISSUE
			 * It is likely that SQL Server will have at least a slightly
			 * different syntax for importing CSV files than PostgreSQL.
			 */				
			//COPY t FROM STDIN
			
			/*
			SQLGeneralQueryFormatter queryFormatter
				= new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "COPY ");
			queryFormatter.addQueryPhrase(destinationTableName);
			queryFormatter.addQueryPhrase(" FROM STDIN WITH DELIMITER ',' CSV HEADER");
			
			System.out.println(queryFormatter.generateQuery());
			System.out.println(csvFilePath);
			
			
			CopyManager copyManager 
				= new CopyManager((BaseConnection) connection);
			
			FileReader fileReader
				= new FileReader(new File(csvFilePath));
			
			copyManager.copyIn(
				queryFormatter.generateQuery(), 
				fileReader);
				*/
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			logException(
				logFileWriter,
				exception);
		}		
	}
		
	private void addDataSourceIdentifierField(
		final Connection connection,
		final Writer logFileWriter,
		final String targetExtractTable,
		final int dataSetIdentifier)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		try {
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "ALTER TABLE ");//KLG_SCHEMA
			queryFormatter.addQueryPhrase(targetExtractTable);
			queryFormatter.addQueryPhrase(" ADD data_set_id INTEGER DEFAULT ");
			queryFormatter.addQueryPhrase(String.valueOf(dataSetIdentifier));
			queryFormatter.addQueryPhrase(" NOT NULL");
			System.out.println(queryFormatter.generateQuery());
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
					"extractWorkflowManager.error.unableToDataSetNumber",
					targetExtractTable);
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
	
	/**
	 * Adding a column for row_number
	 * @param targetExtractTable
	 * @throws RIFServiceException
	 */
	private void addOriginalRowNumbers(
		final Connection connection,
		final Writer logFileWriter,
		final String targetExtractTable) 
		throws RIFServiceException {
				
		PreparedStatement statement = null;
		try {
			
			/*
			 * #POSSIBLE_PORTING_ISSUE
			 * Do PostgreSQL and SQL Server provide support for BIGSERIAL? - int IDENTITY?
			 */				
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "ALTER TABLE ");//KLG_SCHEMA
			queryFormatter.addQueryPhrase(targetExtractTable);
			queryFormatter.addQueryPhrase(" ADD row_number int IDENTITY(1,1) NOT NULL");
			System.out.println(queryFormatter.generateQuery());
	
			statement 
				= createPreparedStatement(connection, queryFormatter);
			statement.executeUpdate();
			
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"extractWorkflowManager.error.unableToAddRowNumbers",
					targetExtractTable);
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
	public void addExtractTableData(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration,
		final String[][] tableData) 
		throws RIFServiceException {
		
		String loadTableName
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(
					dataSetConfiguration.getName());		
		
		MSSQLInsertQueryFormatter queryFormatter 
			= new MSSQLInsertQueryFormatter();
		queryFormatter.setIntoTable(loadTableName);
		queryFormatter.addInsertField("data_source_id");
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			queryFormatter.addInsertField(fieldConfiguration.getLoadFieldName());
		}

		logSQLQuery(
			logFileWriter,
			"addExtractTableData", 
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
			logSQLException(
				logFileWriter, 
				sqlException);
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
	}
	
	public void dropExtractTable(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {


		String coreDataSetName = dataSetConfiguration.getName();
		String tableToDelete
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(coreDataSetName);		
				
		PreparedStatement statement = null;
		try {
			
			/*
			 * #POSSIBLE_PORTING_ISSUE
			 * Porting need to consider that in the PostgreSQL query formatter,
			 * it uses a drop-if-exists, which is a construction that may be
			 * supported differently in SQL Server
			 */				
			MSSQLDeleteTableQueryFormatter deleteExtractTableQueryFormatter
				= new MSSQLDeleteTableQueryFormatter();
			deleteExtractTableQueryFormatter.setTableToDelete(tableToDelete);

			statement 
				= createPreparedStatement(
					connection, 
					deleteExtractTableQueryFormatter);
			statement.executeUpdate();	
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"extractWorkflowManager.error.dropExtractTable",
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


