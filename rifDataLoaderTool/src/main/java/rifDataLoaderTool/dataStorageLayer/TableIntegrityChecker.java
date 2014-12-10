package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;

import rifServices.system.RIFServiceException;
import rifServices.dataStorageLayer.SQLQueryUtility;
import rifServices.dataStorageLayer.SQLGeneralQueryFormatter;
import rifServices.util.RIFLogger;

import java.sql.*;

/**
 * Contains code used to check whether two tables have the same number of rows.  The check
 * is used to determine whether a database processing step has resulted in a bad table
 * join that resulted in a table with fewer or more rows.
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

public class TableIntegrityChecker {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public TableIntegrityChecker() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void checkTotalRowsMatch(
		final Connection connection,
		final String coreTableName,
		final RIFTemporaryTablePrefixes firstTablePrefix,
		final RIFTemporaryTablePrefixes secondTablePrefix) 
		throws RIFServiceException {
		
		String firstTableName
			= firstTablePrefix.getTableName(coreTableName);
		String secondTableName
			= secondTablePrefix.getTableName(coreTableName);
		
		SQLGeneralQueryFormatter queryFormatter 
			= new SQLGeneralQueryFormatter();
		queryFormatter.addQueryPhrase(0, "WITH");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(" firstTableCount AS");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "(SELECT");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "COUNT(data_source_id) AS total");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "FROM");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, firstTableName);
		queryFormatter.addQueryPhrase("),");
		queryFormatter.finishLine();
		
		queryFormatter.addQueryPhrase(" secondTableCount AS");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "(SELECT");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "COUNT(data_source_id) AS total");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "FROM");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, secondTableName);
		queryFormatter.addQueryPhrase(")");
		queryFormatter.padAndFinishLine();
		
		//now compare the two results
		queryFormatter.addQueryPhrase(0, "SELECT");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "CASE");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "WHEN ");
		queryFormatter.addQueryPhrase("firstTableCount.total = secondTableCount.total THEN TRUE");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "ELSE FALSE");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "END AS result");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(1, "FROM");
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(2, "firstTableCount,");
		queryFormatter.finishLine();
		queryFormatter.addQueryPhrase(2, "secondTableCount;");
		queryFormatter.finishLine();
		
		RIFLogger logger = RIFLogger.getLogger();
		logger.debugQuery(
			this, 
			"checkTotalRowsMatch",
			queryFormatter.generateQuery());
		
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			resultSet.next();
			Boolean result = resultSet.getBoolean(1);
			if (result == false) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"tableIntegrityChecker.error.tablesHaveDifferentSizes",
						firstTableName,
						secondTableName);
				RIFServiceException rifDataLoaderException
					= new RIFServiceException(
						RIFDataLoaderToolError.COMPARE_TABLE_SIZES, 
						errorMessage);
				throw rifDataLoaderException;
			}
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage	
				= RIFDataLoaderToolMessages.getMessage("tableIntegrityChecker.error.unableToCompareTables");
			RIFServiceException RIFServiceException
				= new RIFServiceException(
						RIFDataLoaderToolError.DATABASE_QUERY_FAILED,
						errorMessage);
			throw RIFServiceException;
		}
		finally {
			SQLQueryUtility.close(resultSet);
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


