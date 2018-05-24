package org.sahsu.rif.dataloader.datastorage.ms;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.CleaningRule;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldActionPolicy;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLCreatePrimaryKeyQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLDeleteTableQueryFormatter;


/**
 * This is the class that the cleaning activity uses to search for poor
 * quality values and replace them with better quality values.  This class
 * may present some porting issues because it heavily relies on regular
 * expression patterns.  The web site: <a href="https://www.pg-versus-ms.com/">
 * PostgreSQL vs MS SQL Server</a> page provides some interesting discussion
 * on this matter.  As well, some of the search and replace functions that
 * are advertised to RIF managers will almost certainly have to be re-implemented
 * for both PostgreSQL and SQL Server databases.
 * </a>
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

public class MSSQLDataTypeSearchReplaceUtility {

	
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLDataTypeSearchReplaceUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateSearchReplaceTableStatement(
		final DataSetConfiguration dataSetConfiguration) {

		/*
		 * #POSSIBLE_PORTING_ISSUE
		 * Do PostgreSQL and SQL Server support regular expressions
		 * in an identical way?
		 * BP says: YES ! use LIKE instead of ~
		 */	
		
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
		MSSQLDeleteTableQueryFormatter deleteQueryFormatter
			= new MSSQLDeleteTableQueryFormatter();
		//KLG_SCHEMA
		//deleteQueryFormatter.setDatabaseSchemaName("dbo");
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
		MSSQLCreatePrimaryKeyQueryFormatter createPrimaryKeyQueryFormatter
			= new MSSQLCreatePrimaryKeyQueryFormatter();
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
				
		//queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
		//queryFormatter.addQueryPhrase(cleanSearchReplaceTableName);
		//queryFormatter.addQueryPhrase(" AS ");
		//queryFormatter.padAndFinishLine();		
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
				= DataSetConfigurationUtility.getRequiredAndExtraFieldConfigurations(
					dataSetConfiguration);
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
		queryFormatter.addQueryPhrase(0, "INTO ");
		queryFormatter.addQueryPhrase(cleanSearchReplaceTableName);
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
		FieldActionPolicy fieldCleaningPolicy
			= rifDataType.getFieldCleaningPolicy();

		
		if (fieldCleaningPolicy == FieldActionPolicy.DO_NOTHING) {
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
		else if (fieldCleaningPolicy == FieldActionPolicy.USE_RULES) {
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
				queryFormatter.addQueryPhrase(" LIKE ");
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
		else if (fieldCleaningPolicy == FieldActionPolicy.USE_FUNCTION) {
			/*
			 * eg: 
			 * 
			 * clean_icd(icd_1) AS icd_1
			 * 
			 * For MS SQL functions, need to be in this format
			 * 
			 * [dbo].[clean_icd](icd_1) as icd_1
			 * 
			 */
			String cleaningFunctionName
				= rifDataType.getCleaningFunctionName();
			//queryFormatter.addQueryPhrase(
			//	baseIndentationLevel, 
			//	cleaningFunctionName);
			//MS SQL additional formatting
			queryFormatter.addQueryPhrase(
				baseIndentationLevel, 
				"[dbo].[");
			queryFormatter.addQueryPhrase(cleaningFunctionName);
			queryFormatter.addQueryPhrase("]");
						
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


