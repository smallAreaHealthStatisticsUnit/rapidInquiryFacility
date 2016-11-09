package rifDataLoaderTool.dataStorageLayer.pg;

import java.util.Date;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.businessConceptLayer.RIFUserRole;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.SQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;

import java.sql.*;

/**
 * Manages SQL operations related to adding, updating or deleting a user record.
 * This class will likely migrate to the rif governance tool later in development
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

final class PGSQLUserManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLUserManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void addUser(
		final Connection connection,
		final String userID,
		final String password,
		final RIFUserRole rifUserRole,
		final Date expirationDate)
		throws RIFServiceException {

		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_add_user");
		queryFormatter.setNumberOfFunctionParameters(4);
		
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, userID);
			statement.setString(2, password);
			
			if (rifUserRole == RIFUserRole.RIF_MANAGER) {
				statement.setString(3, "rif_manager");
			}
			else if (rifUserRole == RIFUserRole.RIF_STUDENT) {
				statement.setString(3, "rif_student");			
			}
			else if (rifUserRole == RIFUserRole.RIF_USER) {
				statement.setString(3, "rif_user");				
			}
			else {
				assert(false);
			}
			
			java.sql.Date sqlDate = new java.sql.Date(expirationDate.getTime());
			statement.setDate(4, sqlDate);
			
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"userManager.error.unableToAddUser",
					userID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
	}
	
	public void alterUser(
		final Connection connection,
		final User user,
		final String updatedPassword,
		final RIFUserRole rifUserRole,
		final Date expirationDate) 
		throws RIFServiceException {
		
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_alter_user");
		queryFormatter.setNumberOfFunctionParameters(4);
	
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			
			statement.setString(1, user.getUserID());
			statement.setString(2, updatedPassword);
		
			if (rifUserRole == RIFUserRole.RIF_MANAGER) {
				statement.setString(3, "rif_manager");
			}
			else if (rifUserRole == RIFUserRole.RIF_STUDENT) {
				statement.setString(3, "rif_student");			
			}
			else if (rifUserRole == RIFUserRole.RIF_USER) {
				statement.setString(3, "rif_user");				
			}
			else {
				assert(false);
			}
		
			java.sql.Date sqlDate = new java.sql.Date(expirationDate.getTime());
			statement.setDate(4, sqlDate);
		
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"userManager.error.unableToChangeUser",
					user.getUserID());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}			
	}
			
	public void deleteUser(
		final Connection connection,
		final User user) 
		throws RIFServiceException {
					
		SQLFunctionCallerQueryFormatter queryFormatter
			= new SQLFunctionCallerQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		queryFormatter.setFunctionName("rif40_delete_user");
		queryFormatter.setNumberOfFunctionParameters(1);
	
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			
			statement.setString(1, user.getUserID());
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"userManager.error.unableToDeleteUser",
					user.getUserID());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
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


