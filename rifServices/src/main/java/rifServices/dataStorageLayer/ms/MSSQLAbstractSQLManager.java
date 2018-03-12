package rifServices.dataStorageLayer.ms;


import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
// Mainly use Postgres
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.ms.MSSQLQueryUtility; // Only used for createPreparedCall
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Map;

import java.sql.*;

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

public abstract class MSSQLAbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDatabaseProperties rifDatabaseProperties;
	private ValidationPolicy validationPolicy = ValidationPolicy.STRICT;
	private boolean enableLogging = true;
	private static String lineSeparator = System.getProperty("line.separator");
	private static Properties prop = null;
	
	protected RIFLogger rifLogger = RIFLogger.getLogger();
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new abstract sql manager.
	 */
	public MSSQLAbstractSQLManager(
		final RIFDatabaseProperties rifDatabaseProperties) {

		this.rifDatabaseProperties = rifDatabaseProperties;
		
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
				
		return PGSQLQueryUtility.createPreparedStatement(
			connection,
			queryFormatter);

	}
	
	protected CallableStatement createPreparedCall( // Use MSSQLQueryUtility
		final Connection connection,
		final String query) 
		throws SQLException {
				
		return MSSQLQueryUtility.createPreparedCall(
			connection,
			query);

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


	void setEnableLogging(final boolean enableLogging) {
		this.enableLogging = enableLogging;
	}	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	protected void logSQLQuery(
		final String queryName,
		final AbstractSQLQueryFormatter queryFormatter,
		final String... parameters) {
		
		if (!enableLogging || !checkIfQueryLoggingEnabled(queryName)) {
			return;
		}

		
		StringBuilder queryLog = new StringBuilder();
		queryLog.append("QUERY NAME: ").append(queryName).append(lineSeparator);
		queryLog.append("PARAMETERS:").append(lineSeparator);
		for (int i = 0; i < parameters.length; i++) {
			queryLog.append("\t");
			queryLog.append(i + 1);
			queryLog.append(":\"");
			queryLog.append(parameters[i]);
			queryLog.append("\"").append(lineSeparator);
		}
		queryLog.append("MSSQL QUERY TEXT: ").append(lineSeparator);
		queryLog.append(queryFormatter.generateQuery()).append(lineSeparator);
		queryLog.append("<<< End MSSQLAbstractSQLManager logSQLQuery").append(lineSeparator);
	
		rifLogger.info(this.getClass(), "MSSQLAbstractSQLManager logSQLQuery >>>" + lineSeparator + queryLog.toString());	

	}
	
	protected void logSQLException(final SQLException sqlException) {
		rifLogger.error(this.getClass(), "MSSQLAbstractSQLManager.logSQLException error", sqlException);
	}

	protected void logException(final Exception exception) {
		rifLogger.error(this.getClass(), "MSSQLAbstractSQLManager.logException error", exception);
	}
		
	private boolean checkIfQueryLoggingEnabled(
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
					"MSSQLAbstractSQLManager.checkIfQueryLoggingEnabled: CATALINA_HOME not set in environment"); 
				fileName1="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\conf\\AbstractSQLManager.properties";
				fileName2="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\AbstractSQLManager.properties";
			}

			try {
				input = new FileInputStream(fileName1);
					rifLogger.info(this.getClass(), 
						"MSSQLAbstractSQLManager.checkIfInfoLoggingEnabled: using: " + fileName1);
				// load a properties file
				prop.load(input);
			} 
			catch (IOException ioException) {
				try {
					input = new FileInputStream(fileName2);
						rifLogger.info(this.getClass(), 
							"MSSQLAbstractSQLManager.checkIfInfoLoggingEnabled: using: " + fileName2);
					// load a properties file
					prop.load(input);
				} 
				catch (IOException ioException2) {				
					rifLogger.warning(this.getClass(), 
						"MSSQLAbstractSQLManager.checkIfQueryLoggingEnabled error for files: " + 
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
							"MSSQLAbstractSQLManager.checkIfQueryLoggingEnabled error for files: " + 
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
						"MSSQLAbstractSQLManager checkIfQueryLoggingEnabled=TRUE property: " + 
						queryName + "=" + value);
					return true;			
				}
				else {
					rifLogger.debug(this.getClass(), 
						"MSSQLAbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: " + 
						queryName + "=" + value);
					return false;	
				}		
			}
			else {
				rifLogger.warning(this.getClass(), 
					"MSSQLAbstractSQLManager checkIfQueryLoggingEnabled=FALSE property: " + 
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
