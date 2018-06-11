package org.sahsu.rif.generic.datastorage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.util.RIFLogger;

/**
 * Properly closes down statements, connections, result sets.  When these operations
 * fail, it produces error messages.  The class is used to help minimise and 
 * standardise code for cleaning up resources for database queries
 */
public class SQLQueryUtility {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static String callingClassName="rifGenericLibrary.datastorage.pg.SQLQueryUtility";
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();

	/**
	 * Close.
	 *
	 * @param connection the connection
	 * @throws RIFServiceException the RIF service exception
	 */
	public static void close(final Connection connection) throws RIFServiceException {

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

			throw new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_CLOSE_RESOURCE,
				errorMessage);
		}		
	}

	/**
	 * printWarnings. Print info and warning messages
	 *
	 * @param runStudyStatement The {@link PreparedStatement} whose warnings to print
	 */
	public static String printWarnings(PreparedStatement runStudyStatement) {
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

					rifLogger.warning(SQLQueryUtility.class,
					                  "SQL Error/Warning >>>" + lineSeparator +
					                  "Message:           " + warnings.getMessage() + lineSeparator +
					                  "SQLState:          " + warnings.getSQLState() + lineSeparator +
					                  "Vendor error code: " +	warnings.getErrorCode() + lineSeparator);
				}
				warnings = warnings.getNextWarning();
			}

			if (message.length() > 0) {
				rifLogger.info(SQLQueryUtility.class, warningCount + " warnings/messages" +
				                                 lineSeparator +
				                                message.toString());

				return warningCount + " warnings/messages" + lineSeparator + message.toString();
			}
			else {
				rifLogger.warning(SQLQueryUtility.class, "No warnings/messages found.");
				return "No warnings/messages found.";
			}
		}
		catch(SQLException sqlException) { // Do nothing - they are warnings!
			rifLogger.warning(SQLQueryUtility.class, "PGSQLQueryUtility.printWarnings() caught "
			                                    + "sqlException: " +
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
		final QueryFormatter queryFormatter)
		throws SQLException {

		PreparedStatement statement
				= connection.prepareStatement(
				queryFormatter.generateQuery(),
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		if (connection.getHoldability() != ResultSet.CLOSE_CURSORS_AT_COMMIT) {
			connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
		}

		return statement;
	}

	public static PreparedStatement createPreparedStatement(
		final Connection connection,
		final String query) 
		throws SQLException {

		PreparedStatement statement = connection.prepareStatement(
				query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if (connection.getHoldability() != ResultSet.CLOSE_CURSORS_AT_COMMIT) {
			connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
		}

		return statement;
	}

	public static CallableStatement createPreparedCall(
			final Connection connection,
			final String query)
			throws SQLException {

		//holdability set at connection level, not statement level

		return connection.prepareCall(query);
	}
}
