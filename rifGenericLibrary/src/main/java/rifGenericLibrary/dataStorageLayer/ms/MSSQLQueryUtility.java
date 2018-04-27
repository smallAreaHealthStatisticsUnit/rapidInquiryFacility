package rifGenericLibrary.dataStorageLayer.ms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 */
public final class MSSQLQueryUtility {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();

	/**
	 * Instantiates a new SQL query utility.
	 */
	private MSSQLQueryUtility() {

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
				MSSQLQueryUtility.class, 
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
				MSSQLQueryUtility.class, 
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
				MSSQLQueryUtility.class, 
				errorMessage, 
				sqlException);
			
			throw new RIFServiceException(
				RIFGenericLibraryError.DB_UNABLE_CLOSE_RESOURCE,
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
		
		//holdability set at connection level, not statement level
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
		
		//holdability set at connection level, not statement level
		PreparedStatement statement
			= connection.prepareStatement(
				query,
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		if (connection.getHoldability() != ResultSet.CLOSE_CURSORS_AT_COMMIT) {
				connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
		}					
		
		return statement;
	
	}

	/*
	 * PH: Add prepareCall; only for the basic string query formatter. For use with 
	 * SQL Server procedures e.g. rif40_run_study()
	 * THIS IS THE ONLY DIFFERENCE FROM THE POSTGRES VERSION!
	 */
	public static CallableStatement createPreparedCall(
		final Connection connection,
		final String query) 
		throws SQLException {
		
		//holdability set at connection level, not statement level

		return connection.prepareCall(query);
	}
}
