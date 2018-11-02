package org.sahsu.rif.dataloader.system;

import org.sahsu.rif.generic.system.RifError;

/**
 * Describes a source of error in the system.
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public enum RIFDataLoaderToolError implements RifError {
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
	UNABLE_TO_CREATE_FAKE_RIF_DB,
	INVALID_LOAD_STATE,
	INVALID_CLEAN_STATE,
	INVALID_CONVERT_STATE,
	INVALID_SPLIT_STATE,
	INVALID_COMBINE_STATE,
	INVALID_OPTIMISE_STATE,
	INVALID_CHECK_STATE,
	INVALID_PUBLISH_STATE,
	UNKNOWN_CONVERSION_ACTIVITY,
	UNABLE_TO_WRITE_FILE,
	INVALID_LINEAR_WORKFLOW,
	UNABLE_TO_CREATE_TEMPORARY_DIRECTORIES,
	UNABLE_TO_COPY_ORIGINAL_DATA,
	UNABLE_TO_ZIP_RESULTS,
	UNABLE_TO_INCLUDE_WORKFLOW_IN_RESULTS,
	INVALID_SHAPE_FILE,
	DUPLICATE_RIF_DATA_TYPE,
	INVALID_RIF_DATA_TYPE,
	INVALID_DATA_LOADER_GEOGRAPHY,
	UNABLE_TO_CHECK_DB_EXISTS,
	DATA_LOADER_DEPENDENCY_PROBLEM,
	DUPLICATE_DATA_SET_CONFIGURATION_NAME,
	DUPLICATE_HEALTH_THEME_NAME,
	INVALID_DENOMINATOR,
	INVALID_NUMERATOR,
	INVALID_COVARIATE,
	CANNOT_INITIALISE_RUN_ENVIRONMENT
}


