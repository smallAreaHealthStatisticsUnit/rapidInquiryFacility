package rifDataLoaderTool.dataStorageLayer;

import java.sql.*;


import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLDeleteTableQueryFormatter;

import rifServices.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;


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

public abstract class AbstractDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDatabaseProperties rifDatabaseProperties;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractDataLoaderStepManager(
		final RIFDatabaseProperties rifDatabaseProperties) {
		
		this.rifDatabaseProperties = rifDatabaseProperties;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
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
		queryFormatter.addQueryPhrase(2, "secondTableCount;");
		queryFormatter.finishLine();
		
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
			logSQLException(sqlException);
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
	
	protected void addPrimaryKey(
		final Connection connection,
		final String targetTableName,
		final String primaryKeyFieldPhrase)
		throws RIFServiceException {
		
		PreparedStatement statement = null;		
		try {
			SQLGeneralQueryFormatter queryFormatter
				= new SQLGeneralQueryFormatter();
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
			logSQLException(sqlException);
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
	
	protected void deleteTable(
		final Connection connection,
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
			logSQLException(sqlException);
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
	
	protected void copyTable(
		final Connection connection,
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
			logSQLException(sqlException);
			logSQLQuery(
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
/*
	protected void createRowNumberAndDataSetIdentifierIndices(
		final Connection connection,
		final String targetTable) 
		throws RIFServiceException {
		

		PreparedStatement rowNumberIndexStatement = null;
		PreparedStatement dataSetIndexStatement = null;

		try {

			SQLGeneralQueryFormatter rowNumberIndexQueryFormatter 
				= new SQLGeneralQueryFormatter();
			rowNumberIndexQueryFormatter.addQueryPhrase(0, "CREATE INDEX idx_");
			rowNumberIndexQueryFormatter.addQueryPhrase("rn_");
			rowNumberIndexQueryFormatter.addQueryPhrase(targetTable);
			rowNumberIndexQueryFormatter.addQueryPhrase(" ON ");
			rowNumberIndexQueryFormatter.addQueryPhrase(targetTable);
			rowNumberIndexQueryFormatter.addQueryPhrase(" (row_number);");
			
			logSQLQuery("lwfm_create_rn_index", rowNumberIndexQueryFormatter);
					
			rowNumberIndexStatement
				= createPreparedStatement(connection, rowNumberIndexQueryFormatter);
			rowNumberIndexStatement.executeUpdate();
			
			SQLGeneralQueryFormatter dataSetIndexQueryFormatter = new SQLGeneralQueryFormatter();
			dataSetIndexQueryFormatter.addQueryPhrase(0, "CREATE INDEX idx_");
			dataSetIndexQueryFormatter.addQueryPhrase("ds_");
			dataSetIndexQueryFormatter.addQueryPhrase(targetTable);
			dataSetIndexQueryFormatter.addQueryPhrase(" ON ");
			dataSetIndexQueryFormatter.addQueryPhrase(targetTable);
			dataSetIndexQueryFormatter.addQueryPhrase(" (data_set_id);");

			logSQLQuery("lwfm_create_ds_index", dataSetIndexQueryFormatter);
			
			dataSetIndexStatement
				= createPreparedStatement(connection, dataSetIndexQueryFormatter);
			dataSetIndexStatement.executeUpdate();
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"abstractDataLoaderStepManager.error.unableToCreateCommonIndices",
					targetTable);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(rowNumberIndexStatement);			
			SQLQueryUtility.close(dataSetIndexStatement);			
		}		
	}
*/	
	
	
	protected PreparedStatement createPreparedStatement(
		final Connection connection,
		final AbstractSQLQueryFormatter queryFormatter) 
		throws SQLException {
				
		return SQLQueryUtility.createPreparedStatement(
			connection,
			queryFormatter);

	}
		

	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final String... parameters) {

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
		
		System.out.println(queryLog.toString());	

	}
	
	protected void logSQLException(final SQLException sqlException) {
		sqlException.printStackTrace(System.out);
	}

	protected void logException(final Exception exception) {
		exception.printStackTrace(System.out);
	}
	
	protected void setAutoCommitOn(
		final Connection connection,
		final boolean isAutoCommitOn)
		throws RIFServiceException {
		
		try {
			System.out.println("Setting autocommit to=="+isAutoCommitOn+"==");
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


