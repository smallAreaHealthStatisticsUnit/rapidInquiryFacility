package org.sahsu.rif.generic.system;

/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public enum RIFGenericLibraryError implements RifError {
	SECURITY_VIOLATION,
	DB_UNABLE_TO_CLOSE_CONNECTIONS,
	DB_UNABLE_CLOSE_RESOURCE,
	DB_UNABLE_TO_ROLLBACK,
	DB_UNABLE_TO_COMMIT,
	DATABASE_QUERY_FAILED, //@TODO Purge this code from RIFServicesError
	STATE_MACHINE_ERROR,
	DUPLICATE_LIST_ITEM_NAME,
	ERRORS_DETECTED_IN_CSV_FILE,
	UNABLE_TO_WRITE_CSV_ERROR_LOG_FILE,
	DUPLICATE_ITEM_NAME,
	EMPTY_API_METHOD_PARAMETER,
	INVALID_INTEGER_API_METHOD_PARAMETER,
	
	/** The invalid user. */
	INVALID_USER,
	
	R_ERROR,
	
	DB_UNABLE_TO_ADJUST_AUTO_COMMIT,
	DB_UNABLE_TO_LOAD_DRIVER,
	DB_MAX_READ_CONNECTIONS_EXCEEDED,
	DB_MAX_WRITE_CONNECTIONS_EXCEEDED,
	DB_UNABLE_TO_USE_CONNECTION_STRING,
	/** The db unable to grant connection. */
	DB_UNABLE_TO_GRANT_CONNECTION,		
	/** The db unable register user. */
	DB_UNABLE_REGISTER_USER,	
	/** The db unable deregister user. */
	DB_UNABLE_DEREGISTER_USER,
	/* A database exception occurred */
	DB_EXCEPTION,
	
	COMMAND_LINE_EXECUTION_PROBLEM,
	FILE_PARSING_PROBLEM,
	FILE_WRITING_PROBLEM,
	NON_EXISTENT_FILE,
	INVALID_PARAMETER,
	NON_EXISTENT_PARAMETER,
	NON_EXISTENT_TAXONOMY_SERVICE,
	NON_EXISTENT_TAXONOMY_TERM,
	TAXONOMY_SERVICE_INITIALISATION_FAILURE,
	TAXONOMY_SERVICE_NOT_WORKING,	
	DEFAULT_XML_TAXONOMY_READ_FILE_ERROR,
	FEDERATED_TAXONOMY_SERVICE_NOT_INITIALISED,
	FILE_NOT_FOUND
}
