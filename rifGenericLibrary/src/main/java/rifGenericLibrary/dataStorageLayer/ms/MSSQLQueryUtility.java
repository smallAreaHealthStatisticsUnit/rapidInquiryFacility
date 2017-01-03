package rifGenericLibrary.dataStorageLayer.ms;

import rifGenericLibrary.dataStorageLayer.AbstractSQLQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFGenericLibraryMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;





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

public final class MSSQLQueryUtility {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL query utility.
	 */
	private MSSQLQueryUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

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
				= RIFGenericLibraryMessages.getMessage("sqlConnectionManager.error.unableToCloseResource");
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				MSSQLQueryUtility.class, 
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
				= RIFGenericLibraryMessages.getMessage(
					"sqlConnectionManager.error.unableToCloseResource");
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				MSSQLQueryUtility.class, 
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
				= RIFGenericLibraryMessages.getMessage(
					"sqlConnectionManager.error.unableToCloseResource");
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				MSSQLQueryUtility.class, 
				errorMessage, 
				sqlException);
																		
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DB_UNABLE_CLOSE_RESOURCE,
					errorMessage);
			throw rifServiceException;			
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
				= RIFGenericLibraryMessages.getMessage("general.db.error.unableToRollback");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DB_UNABLE_TO_ROLLBACK,
					errorMessage);
			throw rifServiceException;
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
