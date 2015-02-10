package rifDataLoaderTool.dataStorageLayer.postgresql;

import rifDataLoaderTool.businessConceptLayer.LoadStepQueryGeneratorAPI;
import rifDataLoaderTool.businessConceptLayer.TableCleaningConfiguration;


import rifDataLoaderTool.businessConceptLayer.TableFieldCleaningConfiguration;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifServices.dataStorageLayer.SQLGeneralQueryFormatter;

import java.util.ArrayList;

/**
 * Contains methods that generate Postgres-specific SQL code that supports
 * the load step.
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

public class PostgresLoadStepQueryGenerator 
	implements LoadStepQueryGeneratorAPI {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public PostgresLoadStepQueryGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateLoadTableQuery(
		final int dataSourceIdentifier,
		final TableCleaningConfiguration tableCleaningConfiguration,
		final int textColumnWidth) {

		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
		String coreTableName = tableCleaningConfiguration.getCoreTableName();
		String loadTableName = RIFTemporaryTablePrefixes.LOAD.getTableName(coreTableName);
		queryFormatter.addQueryPhrase(loadTableName);
		queryFormatter.addQueryPhrase(" (");
		queryFormatter.finishLine();
		
		queryFormatter.addQueryLine(1, "data_source_id INTEGER NOT NULL,");
		queryFormatter.addQueryLine(1, "row_number SERIAL NOT NULL,");
		ArrayList<TableFieldCleaningConfiguration> fieldCleaningConfigurations
			= tableCleaningConfiguration.getIncludedFieldCleaningConfigurations();
		int numberTableFieldConfigurations = fieldCleaningConfigurations.size();
			
		for (int i = 0; i < numberTableFieldConfigurations; i++) {
			
			if (i != 0) {
				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();				
			}
			TableFieldCleaningConfiguration currentTableFieldConfiguration
				= fieldCleaningConfigurations.get(i);
			queryFormatter.addQueryPhrase(1, currentTableFieldConfiguration.getLoadTableFieldName());
			queryFormatter.addQueryPhrase(" VARCHAR(");
			queryFormatter.addQueryPhrase(String.valueOf(textColumnWidth));
			queryFormatter.addQueryPhrase(")");
		}
					
		queryFormatter.addQueryPhrase(");");
		queryFormatter.finishLine();
			
		return queryFormatter.generateQuery();		
	}
		
	public String generateDropLoadTableQuery(
		final TableCleaningConfiguration tableCleaningConfiguration) {

		StringBuilder query = new StringBuilder();		
		query.append("DROP TABLE IF EXISTS ");
		String coreTableName = tableCleaningConfiguration.getCoreTableName();
		query.append(RIFTemporaryTablePrefixes.LOAD.getTableName(coreTableName));
		return query.toString();
			
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


