package rifServices.dataStorageLayer.common;

import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.common.SQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Map;

import java.sql.*;
import com.sun.rowset.CachedRowSetImpl;

/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
 *
 * Made in common class by Peter Hambly
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

public abstract class SQLAbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDatabaseProperties rifDatabaseProperties;
	private ValidationPolicy validationPolicy = ValidationPolicy.STRICT;
	private boolean enableLogging = true;
	private static Properties prop = null;
	private static String lineSeparator = System.getProperty("line.separator");
	
	protected static final RIFLogger rifLogger = RIFLogger.getLogger();
	
	private static DatabaseType databaseType;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new abstract sql manager.
	 */
	public SQLAbstractSQLManager(
		final RIFDatabaseProperties rifDatabaseProperties) {

		this.rifDatabaseProperties = rifDatabaseProperties;
		this.databaseType=this.rifDatabaseProperties.getDatabaseType();
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public ValidationPolicy getValidationPolicy() {
		return validationPolicy;
	}
		
	public void setValidationPolicy(
		final ValidationPolicy validationPolicy) {
		
		this.validationPolicy = validationPolicy;
	}
	
	/**
	 * Use appropariate table name case.
	 *
	 * @param tableComponentName the table component name
	 * @return the string
	 */
	protected String useAppropariateTableNameCase(
		final String tableComponentName) {
		
		//TODO: KLG - find out more about when we will need to convert
		//to one case or another
		return tableComponentName;
	}
	
	protected RIFDatabaseProperties getRIFDatabaseProperties() {
		return rifDatabaseProperties;
	}
	
	protected void configureQueryFormatterForDB(
		final AbstractSQLQueryFormatter queryFormatter) {
		
		queryFormatter.setDatabaseType(
			rifDatabaseProperties.getDatabaseType());
		queryFormatter.setCaseSensitive(
			rifDatabaseProperties.isCaseSensitive());
		
	}
	
	protected PreparedStatement createPreparedStatement(
		final Connection connection,
		final AbstractSQLQueryFormatter queryFormatter) 
		throws SQLException {
				
		return SQLQueryUtility.createPreparedStatement(
			connection,
			queryFormatter);
	}
	
	protected String getColumnFromResultSet(
			final CachedRowSetImpl cachedRowSet,
			final String columnName)
			throws Exception {
		return getColumnFromResultSet(cachedRowSet, columnName, 
			false /* allowNulls */, false /* allowNoRows */);
	}
			
	protected String getColumnFromResultSet(
			final CachedRowSetImpl cachedRowSet,
			final String columnName,
			final boolean allowNulls,
			final boolean allowNoRows)
			throws Exception {
			
		String columnValue=null;
		boolean columnFound=false;
		if (cachedRowSet.first()) {			
			ResultSetMetaData rsmd = cachedRowSet.getMetaData();
			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);
				String value = cachedRowSet.getString(i);	
				
				if (name.toUpperCase().equals(columnName.toUpperCase())) {
					columnValue=value;
					columnFound=true;
				} 
			}
			if (cachedRowSet.next()) {
				throw new Exception("getColumnFromResultSet(): expected 1 row, got >1");
			}
			if (!columnFound) {
				throw new Exception("getColumnFromResultSet(): column not found: " + columnName);
			}
			if (columnValue == null && !allowNulls) {
				throw new Exception("getColumnFromResultSet(): got null for column: " + columnName);
			}
		}
		else if (!allowNoRows) {
			throw new Exception("getColumnFromResultSet(): expected 1 row, got none");
		}
		
		return columnValue;
	}

	protected String getColumnComment(Connection connection, 
		String schemaName, String tableName, String columnName)
			throws Exception {
		SQLGeneralQueryFormatter columnCommentQueryFormatter = new SQLGeneralQueryFormatter();		
		ResultSet resultSet = null;
		if (databaseType == DatabaseType.POSTGRESQL) {
			columnCommentQueryFormatter.addQueryLine(0, // Postgres
				"SELECT pg_catalog.col_description(c.oid, cols.ordinal_position::int) AS column_comment");
			columnCommentQueryFormatter.addQueryLine(0, "  FROM pg_catalog.pg_class c, information_schema.columns cols");
			columnCommentQueryFormatter.addQueryLine(0, " WHERE cols.table_catalog = current_database()");
			columnCommentQueryFormatter.addQueryLine(0, "   AND cols.table_schema  = ?");
			columnCommentQueryFormatter.addQueryLine(0, "   AND cols.table_name    = ?");
			columnCommentQueryFormatter.addQueryLine(0, "   AND cols.table_name    = c.relname");
			columnCommentQueryFormatter.addQueryLine(0, "   AND cols.column_name   = ?");
		}
		else if (databaseType == DatabaseType.SQL_SERVER) {
			columnCommentQueryFormatter.addQueryLine(0, "SELECT CAST(value AS VARCHAR(2000)) AS column_comment"); // SQL Server
			columnCommentQueryFormatter.addQueryLine(0, "FROM fn_listextendedproperty (NULL, 'schema', ?, 'table', ?, 'column', ?)");
			columnCommentQueryFormatter.addQueryLine(0, "UNION");
			columnCommentQueryFormatter.addQueryLine(0, "SELECT CAST(value AS VARCHAR(2000)) AS column_comment");
			columnCommentQueryFormatter.addQueryLine(0, "FROM fn_listextendedproperty (NULL, 'schema', ?, 'view', ?, 'column', ?)");
		}
		else {
			throw new Exception("getColumnComment(): invalid databaseType: " + 
				databaseType);
		}
		PreparedStatement statement = createPreparedStatement(connection, columnCommentQueryFormatter);
		
		String columnComment=columnName.substring(0, 1).toUpperCase() + 
			columnName.substring(1).replace("_", " "); // Default if not found [initcap, remove underscores]
		try {			
		
			statement.setString(1, schemaName);	
			statement.setString(2, tableName);	
			statement.setString(3, columnName);	
			if (databaseType == DatabaseType.SQL_SERVER) {
				statement.setString(4, schemaName);	
				statement.setString(5, tableName);
				statement.setString(6, columnName);		
			}			
			resultSet = statement.executeQuery();
			if (resultSet.next()) {		
				columnComment=resultSet.getString(1);
				if (resultSet.next()) {		
					throw new Exception("getColumnComment() database: " + databaseType +
						"; expected 1 row, got >1");
				}
			}
			else {
				rifLogger.warning(this.getClass(), "getColumnComment() database: " + databaseType +
					"; expected 1 row, got none");
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement (" + databaseType + ") >>> " + 
				lineSeparator + columnCommentQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
		return columnComment;
	}
	
	protected void enableDatabaseDebugMessages(
		final Connection connection) 
		throws RIFServiceException {
			
		SQLFunctionCallerQueryFormatter setupDatabaseLogQueryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		setupDatabaseLogQueryFormatter.setDatabaseSchemaName("rif40_log_pkg");
		setupDatabaseLogQueryFormatter.setFunctionName("rif40_log_setup");
		setupDatabaseLogQueryFormatter.setNumberOfFunctionParameters(0);		
		PreparedStatement setupLogStatement = null;
		
		SQLFunctionCallerQueryFormatter sendDebugToInfoQueryFormatter 
			= new SQLFunctionCallerQueryFormatter();
		sendDebugToInfoQueryFormatter.setDatabaseSchemaName("rif40_log_pkg");
		sendDebugToInfoQueryFormatter.setFunctionName("rif40_send_debug_to_info");
		sendDebugToInfoQueryFormatter.setNumberOfFunctionParameters(1);		
		
		
		PreparedStatement sendDebugToInfoStatement = null;
		try {
			setupLogStatement 
				= createPreparedStatement(
					connection, 
					setupDatabaseLogQueryFormatter);
			setupLogStatement.executeQuery();
			
			sendDebugToInfoStatement 
				= createPreparedStatement(
					connection, 
					sendDebugToInfoQueryFormatter);
			sendDebugToInfoStatement.setBoolean(1, true);
			sendDebugToInfoStatement.executeQuery();						
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage("abstractSQLManager.error.unableToEnableDatabaseDebugging");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_TO_MAINTAIN_DEBUG, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(setupLogStatement);
			SQLQueryUtility.close(sendDebugToInfoStatement);	
		}		
	}
	
	/**
	 * Use appropriate field name case.
	 *
	 * @param fieldName the field name
	 * @return the string
	 */
	/*
	protected String useAppropriateFieldNameCase(
		final String fieldName) {

		return fieldName.toLowerCase();
	}
	*/
	
	
	public void setEnableLogging(final boolean enableLogging) {
		this.enableLogging = enableLogging;
	}	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final String... parameters) {
		
		if (enableLogging == false || checkIfQueryLoggingEnabled(queryName) == false) {
			return;
		}

		StringBuilder queryLog = new StringBuilder();
		queryLog.append("QUERY NAME: " + queryName + lineSeparator);
		queryLog.append("PARAMETERS:" + lineSeparator);
		for (int i = 0; i < parameters.length; i++) {
			queryLog.append("\t");
			queryLog.append(i + 1);
			queryLog.append(":\"");
			queryLog.append(parameters[i]);
			queryLog.append("\"" + lineSeparator);			
		}
		queryLog.append("PGSQL QUERY TEXT: " + lineSeparator);
		queryLog.append(queryFormatter.generateQuery() + lineSeparator);
		queryLog.append("<<< End SQLAbstractSQLManager logSQLQuery" + lineSeparator);
	
		rifLogger.info(this.getClass(), "SQLAbstractSQLManager logSQLQuery >>>" + 
			lineSeparator + queryLog.toString());	

	}
	
	protected void logSQLException(final SQLException sqlException) {
		rifLogger.error(this.getClass(), "SQLAbstractSQLManager.logSQLException error", sqlException);
	}

	protected void logException(final Exception exception) {
		rifLogger.error(this.getClass(), "SQLAbstractSQLManager.logException error", exception);
	}
	
	protected boolean checkIfQueryLoggingEnabled(
		final String queryName) {

		if (prop == null) {
			Map<String, String> environmentalVariables = System.getenv();
			prop = new Properties();
			InputStream input = null;
			String fileName1;
			String fileName2;
			String catalinaHome = environmentalVariables.get("CATALINA_HOME");
			if (catalinaHome != null) {
				fileName1=catalinaHome + "\\conf\\AbstractSQLManager.properties";
				fileName2=catalinaHome + "\\webapps\\rifServices\\WEB-INF\\classes\\AbstractSQLManager.properties";
			}
			else {
				rifLogger.warning(this.getClass(), 
					"SQLAbstractSQLManager.checkIfQueryLoggingEnabled: CATALINA_HOME not set in environment"); 
				fileName1="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\conf\\AbstractSQLManager.properties";
				fileName2="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\AbstractSQLManager.properties";
			}

			try {
				input = new FileInputStream(fileName1);
					rifLogger.info(this.getClass(), 
						"SQLAbstractSQLManager.checkIfInfoLoggingEnabled: using: " + fileName1);
				// load a properties file
				prop.load(input);
			} 
			catch (IOException ioException) {
				try {
					input = new FileInputStream(fileName2);
						rifLogger.info(this.getClass(), 
							"SQLAbstractSQLManager.checkIfInfoLoggingEnabled: using: " + fileName2);
					// load a properties file
					prop.load(input);
				} 
				catch (IOException ioException2) {				
					rifLogger.warning(this.getClass(), 
						"SQLAbstractSQLManager.checkIfQueryLoggingEnabled error for files: " + 
							fileName1 + " and " + fileName2, 
						ioException2);
					return true;
				}
			} 
			finally {
				if (input != null) {
					try {
						input.close();
					} 
					catch (IOException ioException) {
						rifLogger.warning(this.getClass(), 
							"SQLAbstractSQLManager.checkIfQueryLoggingEnabled error for files: " + 
								fileName1 + " and " + fileName2, 
							ioException);
						return true;
					}
				}
			}
		}
		
		if (prop == null) { // There would have been previous warnings
			return true;
		}			
		else {
			String value = prop.getProperty(queryName);
			if (value != null) {	
				if (value.toLowerCase().equals("true")) {
					rifLogger.debug(this.getClass(), 
						"SQLAbstractSQLManager checkIfQueryLoggingEnabled=TRUE property: " + 
						queryName + "=" + value);
					return true;			
				}
				else {
					rifLogger.debug(this.getClass(), 
						"SQLAbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: " + 
						queryName + "=" + value);
					return false;	
				}		
			}
			else {
				rifLogger.warning(this.getClass(), 
					"SQLAbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: " + 
					queryName + " NOT FOUND");	
				return false;
			}
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
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
