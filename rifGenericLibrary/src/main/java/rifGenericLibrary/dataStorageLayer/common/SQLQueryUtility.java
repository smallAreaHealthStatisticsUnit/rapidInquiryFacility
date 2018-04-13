package rifGenericLibrary.dataStorageLayer.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;

/**
 * Properly closes down statements, connections, result sets.  When these operations
 * fail, it produces error messages.  The class is used to help minimise and 
 * standardise code for cleaning up resources for database queries
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
 *</p>
 */
public class SQLQueryUtility {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static String callingClassName="rifGenericLibrary.dataStorageLayer.pg.SQLQueryUtility";
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
		
	/**
	 * Instantiates a new SQL query utility.
	 */
	public SQLQueryUtility() { // Made public for printWarnings()

	}

	/**
	 * Close.
	 *
	 * @param connection the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	public static void close(
		final Connection connection) 
		throws RIFServiceException {

		if (connection == null) {
			return;
		}
		
		try {
			connection.close();
		}
		catch(SQLException sqlException) {			
			String errorMessage
				= GENERIC_MESSAGES.getMessage("sqlConnectionManager.error.unableToCloseResource");
			
			rifLogger.error(
				SQLQueryUtility.class, 
				errorMessage, 
				sqlException);

			throw new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_CLOSE_RESOURCE,
				errorMessage);
		}
	}
	
	/**
	 * Close.
	 *
	 * @param resultSet the result set
	 * @throws RIFServiceException the RIF service exception
	 */
	public static void close(
		final ResultSet resultSet) 
		throws RIFServiceException {
		
		if (resultSet == null) {
			return;
		}
		
		try {
			resultSet.close();			
		}
		catch(SQLException sqlException) {

			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"sqlConnectionManager.error.unableToCloseResource");
			
			rifLogger.error(
				SQLQueryUtility.class, 
				errorMessage, 
				sqlException);

			throw new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_CLOSE_RESOURCE,
				errorMessage);
		}
	}
	
	/**
	 * Close.
	 *
	 * @param statement the statement
	 * @throws RIFServiceException the RIF service exception
	 */
	public static void close(
		final Statement statement) 
		throws RIFServiceException {

		if (statement == null) {
			return;
		}
		
		try {
			statement.close();			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"sqlConnectionManager.error.unableToCloseResource");
			
			rifLogger.error(
				SQLQueryUtility.class, 
				errorMessage, 
				sqlException);
																		
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DB_UNABLE_CLOSE_RESOURCE,
					errorMessage);
			throw rifServiceException;			
		}		
	}
	
	/**
	 * printWarnings. Print info and warning messages
	 *
	 * @param warning SQLWarning
	 */
	public String printWarnings(PreparedStatement runStudyStatement) {
		SQLWarning warnings;
		StringBuilder message;
		int warningCount=0;
		
		try {
			warnings=runStudyStatement.getWarnings();
			message = new StringBuilder();
			
			while (warnings != null) {	
				warningCount++;
				if (warnings.getErrorCode() == 0) {
					message.append(warnings.getMessage()).append(lineSeparator);
				}
				else {
					message.append("SQL Error/Warning >>>").append(lineSeparator)
							.append("Message:           ").append(warnings.getMessage())
							.append(lineSeparator).append("SQLState:          ")
							.append(warnings.getSQLState()).append(lineSeparator)
							.append("Vendor error code: ").append(warnings.getErrorCode())
							.append(lineSeparator);
						
					rifLogger.warning(this.getClass(), 
						"SQL Error/Warning >>>" + lineSeparator +
						"Message:           " + warnings.getMessage() + lineSeparator +
						"SQLState:          " + warnings.getSQLState() + lineSeparator +
						"Vendor error code: " +	warnings.getErrorCode() + lineSeparator);	       
				}
				warnings = warnings.getNextWarning();
			}
			
			if (message.length() > 0) {
				rifLogger.info(this.getClass(), warningCount + " warnings/messages" + lineSeparator +
					message.toString());
					
					return warningCount + " warnings/messages" + lineSeparator + message.toString();
			}	 
			else {
				rifLogger.warning(this.getClass(), "No warnings/messages found.");
				return "No warnings/messages found.";
			}
		}		
		catch(SQLException sqlException) { // Do nothing - they are warnings!
			rifLogger.warning(this.getClass(), "SQLQueryUtility.printWarnings() caught sqlException: " + 
				sqlException.getMessage());
		}
		
		return null;
	}
	
	public static void commit(
		final Connection connection ) 
		throws RIFServiceException {
		
		if (connection == null) {
			return;
		}
		
		try {
			connection.commit();
			rifLogger.info(callingClassName, "COMMIT");	
		}
		catch(SQLException sqlException) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage("general.db.error.unableToCommit");
			throw new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_TO_COMMIT,
				errorMessage);
		}		
	}
	
	public static void rollback(
		final Connection connection ) 
		throws RIFServiceException {
		
		if (connection == null) {
			return;
		}
		
		try {
			connection.rollback();
			rifLogger.info(callingClassName, "ROLLBACK");	       
		}
		catch(SQLException sqlException) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage("general.db.error.unableToRollback");
			throw new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_TO_ROLLBACK,
				errorMessage);
		}		
	}
	
	public static PreparedStatement createPreparedStatement(
		final Connection connection,
		final AbstractSQLQueryFormatter queryFormatter) 
		throws SQLException {

		return connection.prepareStatement(
			queryFormatter.generateQuery(),
			ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_READ_ONLY,
			ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	public static PreparedStatement createPreparedStatement(
		final Connection connection,
		final String query) 
		throws SQLException {

		return connection.prepareStatement(
			query,
			ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_READ_ONLY,
			ResultSet.CLOSE_CURSORS_AT_COMMIT);
	
	}
}
