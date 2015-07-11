package rifDataLoaderTool.system;

/**
 * Describes a source of error in the system.
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

public enum RIFDataLoaderToolError {
	INVALID_DATA_SOURCE,
	DUPLICATE_DATA_SOURCE,
	CLEAR_ALL_DATA_SOURCES,
	REGISTER_DATA_SOURCE,
	DEREGISTER_DATA_SOURCE,
	DATA_SOURCE_EXISTS,
	LOAD_TABLE,
	CLEAN_TABLE,
	DROP_TABLE,
	CLOSE_RESOURCE,
	DELETE_DATA_SOURCE_REGISTRY,
	LOAD_DB_DRIVER,
	GET_CONNECTION,
	DATABASE_QUERY_FAILED,
	COMPARE_TABLE_SIZES,
	INVALID_LOAD_SOURCE, 
	INVALID_CLEANING_RULE,
	EMPTY_API_METHOD_PARAMETER,
	NO_COMMAND_LINE_ARGUMENTS_SPECIFIED,
	ILLEGAL_CONFIGURATION_FILE_SPECIFIED,
	INVALID_DATA_SET_CONFIGURATION,
	INVALID_DATA_SET_FIELD_CONFIGURATION,
	IMPROPERLY_SET_CONVERSION_FUNCTION,
	UNKNOWN_COMMAND_LINE_OPTION,
	NO_SCHEMA_AREA_SPECIFIED,
	NO_CONFIGURATION_FILE_SPECIFIED,
	NON_EXISTENT_CONFIGURATION_FILE,
	UNABLE_TO_READ_CONFIGURATION_FILE,
	UNABLE_TO_DELETE_DATA_SET_CONFIGURATION,
	UNABLE_TO_IMPORT_CSV_FILE,
	UNABLE_TO_WRITE_CSV_ERROR_LOG_FILE,
	ERRORS_DETECTED_IN_CSV_FILE,
	UNABLE_TO_CREATE_FAKE_RIF_DB,
	INVALID_LOAD_STATE,
	INVALID_CLEAN_STATE,
	INVALID_CONVERT_STATE,
	INVALID_SPLIT_STATE,
	INVALID_COMBINE_STATE,
	INVALID_OPTIMISE_STATE,
	INVALID_CHECK_STATE,
	INVALID_PUBLISH_STATE,

	
}


