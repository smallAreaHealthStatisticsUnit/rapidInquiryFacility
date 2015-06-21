package rifDataLoaderTool.dataStorageLayer;




import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;



import rifDataLoaderTool.system.RIFDataLoaderMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifServices.system.RIFServiceException;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.CheckWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSet;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowFieldConfiguration;
import rifGenericLibrary.dataStorageLayer.SQLCountQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLFieldVarianceQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifServices.dataStorageLayer.SQLQueryUtility;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.util.RIFLogger;


import java.sql.*;
import java.util.ArrayList;



/**
 * manages database calls related to cleaning a data source.
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

public final class CheckWorkflowManager 
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

	public CheckWorkflowManager(
		final RIFDataLoaderStartupOptions startupOptions) {

		super(startupOptions);
	
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void checkWorkflowConfiguration(
		final Connection connection,
		final CheckWorkflowConfiguration checkWorkflowConfiguration) 
		throws RIFServiceException {
		
		checkWorkflowConfiguration.checkErrors();
		
		String coreDataSetName 
			= checkWorkflowConfiguration.getCoreDataSetName();
		PreparedStatement statement = null;
		try {
			
			String convertTableName
				= RIFTemporaryTablePrefixes.CONVERT.getTableName(coreDataSetName);
			String checkTableName
				= RIFTemporaryTablePrefixes.CHECK.getTableName(coreDataSetName);
				
			SQLGeneralQueryFormatter queryFormatter 
				= new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
			queryFormatter.addQueryPhrase(checkTableName);
			queryFormatter.padAndFinishLine();
			queryFormatter.addPaddedQueryLine(0, "AS");
			queryFormatter.addPaddedQueryLine(0, "WITH duplicate_rows AS");
			queryFormatter.addPaddedQueryLine(1, "(SELECT");
			queryFormatter.addQueryLine(2, "data_source_id");
			queryFormatter.addQueryLine(2, "row_number");
			
			
			//add in all the fields we're promoting from converted table
			
			
			
			
			
			
			
			queryFormatter.addPaddedQueryLine(2, "row_number() OVER");
			queryFormatter.addPaddedQueryLine(3, "(PARTITION BY");
			

			ArrayList<String> duplicateCriteriaFields
				= checkWorkflowConfiguration.getDuplicateRowCheckFields();
			int numberOfDuplicateCriteriaFields
				= duplicateCriteriaFields.size();
			for (int i = 0; i < numberOfDuplicateCriteriaFields; i++) {
				if (i != 0) {
					queryFormatter.addQueryPhrase(",");
					queryFormatter.finishLine();
				}
				queryFormatter.addQueryPhrase(
					4, 
					duplicateCriteriaFields.get(i));
			}			
		
			queryFormatter.addPaddedQueryLine(3, "ORDER BY");
			for (int i = 0; i < numberOfDuplicateCriteriaFields; i++) {
				if (i != 0) {
					queryFormatter.addQueryPhrase(",");
					queryFormatter.finishLine();
				}
				queryFormatter.addQueryPhrase(
					4, 
					duplicateCriteriaFields.get(i));
			}			
			queryFormatter.addQueryPhrase(") AS duplicate_number");
			queryFormatter.padAndFinishLine();
			
			queryFormatter.addPaddedQueryLine(0, "SELECT");
			queryFormatter.addQueryLine(1, "duplicate_clusters.data_source_id");
			queryFormatter.addQueryLine(1, "duplicate_clusters.row_number");

			//add in all the fields we're promoting from converted table
			
			
			
			queryFormatter.addPaddedQueryLine(1, "CASE");
			queryFormatter.addPaddedQueryLine(
				2, 
				"WHEN duplicate_clusters.duplicate_number=1 THEN 'Y'");
			queryFormatter.addPaddedQueryLine(
				2,
				"ELSE 'N'");
			queryFormatter.addPaddedQueryLine(1, "END AS keep_record");
			queryFormatter.addPaddedQueryLine(0, "FROM");
			queryFormatter.addPaddedQueryLine(1, "duplicate_clusters;");
			
			statement 
				= createPreparedStatement(connection, queryFormatter);
			statement.executeUpdate();
			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderMessages.getMessage("");
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

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


