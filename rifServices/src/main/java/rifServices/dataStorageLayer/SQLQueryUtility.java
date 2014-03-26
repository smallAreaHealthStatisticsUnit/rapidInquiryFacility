package rifServices.dataStorageLayer;

import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public class SQLQueryUtility {

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
	private SQLQueryUtility() {

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
				= RIFServiceMessages.getMessage("sqlConnectionManager.error.unableToCloseResource");
			
			Logger logger 
				= LoggerFactory.getLogger(SQLMapDataManager.class);
			logger.error(errorMessage, sqlException);				
													
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_CLOSE_RESOURCE,
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
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToCloseResource");
			
			Logger logger 
				= LoggerFactory.getLogger(SQLQueryUtility.class);
			logger.error(errorMessage, sqlException);				
															
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_CLOSE_RESOURCE,
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
				= RIFServiceMessages.getMessage(
					"sqlConnectionManager.error.unableToCloseResource");
			
			Logger logger 
				= LoggerFactory.getLogger(SQLQueryUtility.class);
			logger.error(errorMessage, sqlException);				
																
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DB_UNABLE_CLOSE_RESOURCE,
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
