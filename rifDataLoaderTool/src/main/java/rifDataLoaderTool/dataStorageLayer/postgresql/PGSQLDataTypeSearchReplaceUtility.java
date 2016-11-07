package rifDataLoaderTool.dataStorageLayer.postgresql;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifGenericLibrary.dataStorageLayer.SQLCreatePrimaryKeyQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLDeleteTableQueryFormatter;








import java.util.ArrayList;


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

public class PGSQLDataTypeSearchReplaceUtility {

	
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLDataTypeSearchReplaceUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateSearchReplaceTableStatement(
		final DataSetConfiguration dataSetConfiguration) {



		/*
		 * CREATE TABLE cln_srch_my_numerator1 AS 
		 * SELECT
		 *    data_set_id, -- no cleaning 
		 *    row_number,
		 *    patient_id,
		 *    CASE -- application of cleaning rules
		 *    	WHEN sex ~ ^[mM] THEN '1'
		 *      WHEN sex ~ ^[fF] THEN '2'
		 *      ELSE sex //no cleaning functions match, pass original value 
		 *    END AS sex,
		 *    ntile(score, 5) OVER score,
		 *    dob,
		 *    clean_uk_postal_code(postal_code) AS postal_code, -- clean function
		 *    clean_icd_code(icd_1) AS icd_1,
		 *    level1,
		 *    level2,
		 *    
		 *    
		 * FROM
		 *    load_my_numerator1;
		 */
	
		//eg: my_numerator1
		String coreDataSetName
			= dataSetConfiguration.getName();
		String loadTableName
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(coreDataSetName);
		String cleanSearchReplaceTableName
			= RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE.getTableName(coreDataSetName);
		
		//add comments to the SQL query
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.setEndWithSemiColon(false);
		String queryCommentLine1
			= RIFDataLoaderToolMessages.getMessage(
				"queryComments.clean.validationQuery.comment1");
		queryFormatter.addCommentLine(queryCommentLine1);
		queryFormatter.addUnderline();
		String queryCommentLine2
			= RIFDataLoaderToolMessages.getMessage(
				"queryComments.clean.validationQuery.comment2");
		queryFormatter.addCommentLine(queryCommentLine2);
		
		//delete any version of the same table
		SQLDeleteTableQueryFormatter deleteQueryFormatter
			= new SQLDeleteTableQueryFormatter();
		deleteQueryFormatter.setTableToDelete(cleanSearchReplaceTableName);
		queryFormatter.addQueryPhrase(deleteQueryFormatter.generateQuery());
		
		
		/*
		 * This is a query of the format:
		 * CREATE TABLE XXXX AS
		 * SELECT
		 *    ...
		 *    ...
		 * FROM
		 *    ...
		 */
		SQLGeneralQueryFormatter createValidationCTASQueryFormatter
			= new SQLGeneralQueryFormatter();	
		createValidationCTASQueryFormatter.setEndWithSemiColon(true);
		createSearchReplaceCTASStatement(
			createValidationCTASQueryFormatter,
			loadTableName,
			cleanSearchReplaceTableName,
			dataSetConfiguration,
			false);
		
		queryFormatter.addQuery(createValidationCTASQueryFormatter);
				
		//Add primary key statement
		SQLCreatePrimaryKeyQueryFormatter createPrimaryKeyQueryFormatter
			= new SQLCreatePrimaryKeyQueryFormatter();
		createPrimaryKeyQueryFormatter.setTable(cleanSearchReplaceTableName);
		createPrimaryKeyQueryFormatter.setPrimaryKeyPhrase("data_set_id, row_number");
		queryFormatter.addQuery(createPrimaryKeyQueryFormatter);

		return queryFormatter.generateQuery();
	}
	
	private void createSearchReplaceCTASStatement(
		final SQLGeneralQueryFormatter queryFormatter,
		final String loadTableName,
		final String cleanSearchReplaceTableName,
		final DataSetConfiguration dataSetConfiguration,
		final boolean includeIgnoredFields) {
				
		queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
		queryFormatter.addQueryPhrase(cleanSearchReplaceTableName);
		queryFormatter.addQueryPhrase(" AS ");
		queryFormatter.padAndFinishLine();		
		queryFormatter.addPaddedQueryLine(0, "SELECT");
		
		queryFormatter.addQueryLine(1, "data_set_id,");
		queryFormatter.addQueryLine(1, "row_number,");
	
		ArrayList<DataSetFieldConfiguration> fieldConfigurations 
			= new ArrayList<DataSetFieldConfiguration>();
		if (includeIgnoredFields) {
			fieldConfigurations
				= dataSetConfiguration.getFieldConfigurations();			
		}
		else {
			fieldConfigurations
				= dataSetConfiguration.getRequiredAndExtraFieldConfigurations();
		}
		
		int numberOfFieldConfigurations = fieldConfigurations.size();
		for (int i = 0; i < numberOfFieldConfigurations; i++) {
			if (i != 0) {
				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();
			}
			addDataSetFieldCleaningFragment(
				1,
				queryFormatter,
				fieldConfigurations.get(i));			
		}
		queryFormatter.finishLine();
		queryFormatter.addPaddedQueryLine(0, "FROM");
		queryFormatter.addQueryPhrase(1, loadTableName);
	}
	
	
	private void addDataSetFieldCleaningFragment(
		final int baseIndentationLevel,
		final SQLGeneralQueryFormatter queryFormatter,
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
			
		String loadFieldName
			= dataSetFieldConfiguration.getLoadFieldName();
		String cleanFieldName
			= dataSetFieldConfiguration.getCleanFieldName();
		
		RIFDataType rifDataType
			= dataSetFieldConfiguration.getRIFDataType();
		RIFFieldActionPolicy fieldCleaningPolicy
			= rifDataType.getFieldCleaningPolicy();

		
		if (fieldCleaningPolicy == RIFFieldActionPolicy.DO_NOTHING) {
			//just allow load field value to pass
			
			queryFormatter.addQueryPhrase(baseIndentationLevel, loadFieldName);
			queryFormatter.addQueryPhrase(" AS ");
			queryFormatter.addQueryPhrase(cleanFieldName);			

/*			
			queryFormatter.addQueryPhrase(
				baseIndentationLevel, 
				loadFieldName);
*/				
		}
		else if (fieldCleaningPolicy == RIFFieldActionPolicy.USE_RULES) {
			/*
			 * eg:
			 *
			 * CASE
			 *    WHEN sex ~ '^male|MALE$' THEN 0
			 *    WHEN sex ~ '^[mM]$' THEN 0
			 *    ...
			 *    ...
			 *    WHEN sex ~ '^female|FEMALE$' THEN 1			 *    
			 *    ELSE sex
			 * END AS sex, //using validation rule
			 * 
			 */
			
			queryFormatter.addQueryPhrase(baseIndentationLevel, "CASE");
			queryFormatter.padAndFinishLine();

			
			ArrayList<CleaningRule> cleaningRules
				= rifDataType.getCleaningRules();
			
			
			for (CleaningRule cleaningRule : cleaningRules) {
				queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "WHEN ");
				queryFormatter.addQueryPhrase(loadFieldName);
				queryFormatter.addQueryPhrase(" ~ ");
				queryFormatter.addQueryPhrase("'");
				queryFormatter.addQueryPhrase(cleaningRule.getSearchValue());
				queryFormatter.addQueryPhrase("'");
				queryFormatter.addQueryPhrase(" THEN '");
				queryFormatter.addQueryPhrase(cleaningRule.getReplaceValue());
				queryFormatter.addQueryPhrase("'");
				queryFormatter.padAndFinishLine();
			}
			
			//does not fit any of the regular expressions, therefore is not valid
			queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "ELSE ");
			queryFormatter.addQueryPhrase(loadFieldName);
			queryFormatter.padAndFinishLine();

			queryFormatter.addQueryPhrase(baseIndentationLevel, "END AS ");
			queryFormatter.addQueryPhrase(cleanFieldName);
		}
		else if (fieldCleaningPolicy == RIFFieldActionPolicy.USE_FUNCTION) {
			/*
			 * eg: 
			 * 
			 * clean_icd(icd_1) AS icd_1
			 * 
			 */
			String cleaningFunctionName
				= rifDataType.getCleaningFunctionName();
			queryFormatter.addQueryPhrase(
				baseIndentationLevel, 
				cleaningFunctionName);
			queryFormatter.addQueryPhrase("(");
			queryFormatter.addQueryPhrase(loadFieldName);
			queryFormatter.addQueryPhrase(") AS ");
			queryFormatter.addQueryPhrase(cleanFieldName);			
		}
		else {
			assert false;
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


