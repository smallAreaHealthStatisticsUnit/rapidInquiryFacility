package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifGenericLibrary.dataStorageLayer.SQLCreateIndexQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLDeleteIndexQueryFormatter;



import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;

import java.sql.*;

/**
 *
 * Manages all database operations used to convert a cleaned table into tabular data
 * expected by some part of the RIF (eg: numerator data, health codes, geospatial data etc)
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

public final class OptimiseWorkflowManager 
	extends AbstractDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================


	// ==========================================
	// Section Construction
	// ==========================================

	public OptimiseWorkflowManager(
		final RIFDataLoaderStartupOptions startupOptions) {

		super(startupOptions);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void optimiseConfiguration(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
	
		//validate parameters
		
		dataSetConfiguration.checkErrors();
		checkNonExistentFields(dataSetConfiguration);
		

		deleteIndices(
			connection,
			dataSetConfiguration.getName(),
			dataSetConfiguration.getIndexFieldNames());			
			
		createIndices(
			connection,
			dataSetConfiguration.getName(),
			dataSetConfiguration.getIndexFieldNames());

	}
	
	private void createIndices(
		final Connection connection,
		final String coreDataSetName,
		final String[] indexFieldNames) 
		throws RIFServiceException {
				
		String currentIndexFieldName = null;
		PreparedStatement statement = null;
		try {

			String targetTable
				= RIFTemporaryTablePrefixes.CONVERT.getTableName(coreDataSetName);
	
			for (String indexFieldName : indexFieldNames) {
				currentIndexFieldName = indexFieldName;
				
				SQLCreateIndexQueryFormatter queryFormatter
					= new SQLCreateIndexQueryFormatter();
				queryFormatter.setIndexTable(targetTable);				
				queryFormatter.setIndexTableField(indexFieldName);

				logSQLQuery(
					"createIndices", 
					queryFormatter, 
					targetTable,
					indexFieldName);
				
				statement 
					= createPreparedStatement(
						connection, 
						queryFormatter);
				statement.executeQuery();
				
				SQLQueryUtility.close(statement);				
			}
			
			connection.commit();
			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"optimiseWorkflowManager.error.unableToCreateIndex",
					currentIndexFieldName,
					coreDataSetName);
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
		
	private void deleteIndices(
		final Connection connection,
		final String coreDataSetName,
		final String[] indexFieldNames) 
		throws RIFServiceException {
	
		String currentFieldName = null;
		PreparedStatement statement = null;
		try {

			String targetTable
				= RIFTemporaryTablePrefixes.CONVERT.getTableName(coreDataSetName);
	
			for (String indexFieldName : indexFieldNames) {
				currentFieldName = indexFieldName;
				SQLDeleteIndexQueryFormatter queryFormatter
					= new SQLDeleteIndexQueryFormatter();
				queryFormatter.setIndexTable(targetTable);				
				queryFormatter.setIndexTableField(indexFieldName);

				logSQLQuery(
					"deleteIndices", 
					queryFormatter, 
					targetTable,
					indexFieldName);
				
				statement 
					= createPreparedStatement(
						connection, 
						queryFormatter);
				statement.executeQuery();
				
				SQLQueryUtility.close(statement);				
			}
			
			connection.commit();
			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"optimiseWorkflowManager.error.unableToDeleteIndex",
					currentFieldName,
					coreDataSetName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		
	}
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private void checkNonExistentFields(
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		//@TODO
		
		
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


