package rifDataLoaderTool.dataStorageLayer.postgresql;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifGenericLibrary.dataStorageLayer.SQLCreatePrimaryKeyQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLDeleteTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;

import java.util.ArrayList;

/**
 * Contains methods that generate Postgres-specific SQL code that supports
 * the cleaning step.
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

public final class PostgreSQLCastingUtility {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public PostgreSQLCastingUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	public String generateCastingTableQuery(
		final DataSetConfiguration dataSetConfiguration) {


		/*
		 * DROP IF EXISTS cln_cast_my_numerator;
		 * CREATE TABLE cln_cast_my_numerator AS
		 * SELECT
		 *    data_set_id,
		 *    row_number,
		 *    patient_id, -- no validation or cleaning done so leave as is
		 *    CASE
		 *       WHEN year = 'rif_error' THEN NULL
		 *    	 ELSE cast(year AS INTEGER)
		 *    END AS year,		 *    
		 *    CASE
		 *       WHEN sex = 'rif_error' THEN NULL
		 *    	 ELSE cast(sex AS INTEGER)
		 *    END AS sex,
		 *    CASE
		 *       WHEN age = 'rif_error' THEN NULL
		 *       ELSE cast(age AS INTEGER)
		 *    END AS age,
		 *    CASE
		 *       WHEN dob = 'rif_error' THEN NULL
		 *       ELSE to_timestamp(dob, 'dd-mm-yyyy')
		 *    END AS dob,
		 *    CASE
		 *       WHEN postal_code = 'rif_error' THEN NULL
		 *       ELSE postal_code
		 *    END AS postal_code,
		 *    CASE
		 *       WHEN score = 'rif_error' THEN NULL
		 *       ELSE score
		 *    END AS score,		 
		 *        
		 * FROM
		 *    cln_val__my_numerator   
		 * ALTER TABLE cln_cast_my_numerator ADD PRIMARY KEY(data_set_id, row_number);
		 *
		 */
		
		
		String coreDataSetName 
			= dataSetConfiguration.getName();
		String cleanValidationTableName
			= RIFTemporaryTablePrefixes.CLEAN_VALIDATION.getTableName(coreDataSetName);
		String castingTableName
			= RIFTemporaryTablePrefixes.CLEAN_CASTING.getTableName(coreDataSetName);
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.setEndWithSemiColon(false);
		
		String queryCommentLine1
			= RIFDataLoaderToolMessages.getMessage("queryComments.clean.castQuery.comment1");
		queryFormatter.addCommentLine(queryCommentLine1);
		queryFormatter.addUnderline();
		String queryCommentLine2
			= RIFDataLoaderToolMessages.getMessage("queryComments.clean.castQuery.comment2");
		queryFormatter.addCommentLine(queryCommentLine2);
		String queryCommentLine3
			= RIFDataLoaderToolMessages.getMessage("queryComments.clean.castQuery.comment3");
		queryFormatter.addCommentLine(queryCommentLine3);
		String queryCommentLine4
			= RIFDataLoaderToolMessages.getMessage("queryComments.clean.castQuery.comment4");
		queryFormatter.addCommentLine(queryCommentLine4);
		String queryCommentLine5
			= RIFDataLoaderToolMessages.getMessage("queryComments.clean.castQuery.comment5");
		queryFormatter.addCommentLine(queryCommentLine5);
			
		//delete any version of the same table
		SQLDeleteTableQueryFormatter deleteQueryFormatter
			= new SQLDeleteTableQueryFormatter();
		deleteQueryFormatter.setTableToDelete(castingTableName);
		queryFormatter.addQueryPhrase(deleteQueryFormatter.generateQuery());
		queryFormatter.finishLine();
		
		/*
		 * This is a query of the format:
		 * CREATE TABLE XXXX AS
		 * SELECT
		 *    ...
		 *    ...
		 * FROM
		 *    ...
		 */
		SQLGeneralQueryFormatter createCastingCTASQueryFormatter
			= new SQLGeneralQueryFormatter();		
		createCastingCTASStatement(
				createCastingCTASQueryFormatter,
			cleanValidationTableName,
			castingTableName,
			dataSetConfiguration);
		queryFormatter.addQuery(createCastingCTASQueryFormatter);		
				
		//Add primary key statement
		SQLCreatePrimaryKeyQueryFormatter createPrimaryKeyQueryFormatter
			= new SQLCreatePrimaryKeyQueryFormatter();
		createPrimaryKeyQueryFormatter.setTable(castingTableName);
		createPrimaryKeyQueryFormatter.setPrimaryKeyPhrase("data_set_id, row_number");
		queryFormatter.addQuery(createPrimaryKeyQueryFormatter);

		return queryFormatter.generateQuery();
	}

	
	private void createCastingCTASStatement(
		final SQLGeneralQueryFormatter queryFormatter,
		final String cleanValidationTableName,
		final String castingTableName,
		final DataSetConfiguration dataSetConfiguration) {
				
		queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
		queryFormatter.addQueryPhrase(castingTableName);
		queryFormatter.addQueryPhrase(" AS ");		
		queryFormatter.padAndFinishLine();		
		queryFormatter.addPaddedQueryLine(0, "SELECT");
		
		queryFormatter.addQueryLine(1, "data_set_id,");
		queryFormatter.addQueryLine(1, "row_number,");
	
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getRequiredAndExtraFieldConfigurations();
		int numberOfFieldConfigurations = fieldConfigurations.size();
		for (int i = 0; i < numberOfFieldConfigurations; i++) {
			if (i != 0) {
				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();
			}
			addCastingQueryFragment(
				queryFormatter,
				1,
				fieldConfigurations.get(i));			
		}
		queryFormatter.finishLine();
		queryFormatter.addPaddedQueryLine(0, "FROM");
		queryFormatter.addQueryPhrase(1, cleanValidationTableName);
		queryFormatter.addQueryPhrase(";");
		queryFormatter.finishLine();
	}
	

	private void addCastingQueryFragment(
		final SQLGeneralQueryFormatter queryFormatter,
		final int baseIndentationLevel,
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
		
		String cleanedTableFieldName
			= dataSetFieldConfiguration.getCleanFieldName();
		RIFDataType rifDataType
			= dataSetFieldConfiguration.getRIFDataType();
		
		/*
		 * makes this kind of fragment:
		 * 
		 * CASE
		 *    WHEN age IS NULL THEN NULL
		 *    WHEN age = 'rif_error' THEN NULL
		 * 
		 */	
		queryFormatter.addQueryLine(baseIndentationLevel, "CASE ");		
		queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "WHEN ");
		queryFormatter.addQueryPhrase(cleanedTableFieldName);
		queryFormatter.addQueryPhrase(" ='' THEN NULL ");		
		queryFormatter.padAndFinishLine();
		
		queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "WHEN ");
		queryFormatter.addQueryPhrase(cleanedTableFieldName);
		queryFormatter.addQueryPhrase(" = 'rif_error' THEN NULL");
		queryFormatter.padAndFinishLine();
			
		if (RIFDataTypeFactory.isIntegerDataType(rifDataType) ||
			RIFDataTypeFactory.isAgeDataType(rifDataType) ||
			RIFDataTypeFactory.isSexDataType(rifDataType) ||
			RIFDataTypeFactory.isYearDataType(rifDataType) ||
			RIFDataTypeFactory.isQuintiliseDataType(rifDataType)) {
					
			/*
			 * Generates a query fragment like:
			 * 
			 *    ELSE cast(age AS INTEGER)
			 * END
			 */
			
			queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "ELSE ");
			queryFormatter.addQueryPhrase("cast(");
			queryFormatter.addQueryPhrase(cleanedTableFieldName);
			queryFormatter.addQueryPhrase(" AS INTEGER)");
		}
		else if (RIFDataTypeFactory.isDateDataType(rifDataType)) {
			
			/*
			 * @TODO KLG - may have to create a function that
			 * produces a TIMESTAMP object
			 * 
			 * Generates a query fragment like:
			 * 
			 * ELSE to_timestamp(birth_date, 'DD Mon YYYY');
			 */
			queryFormatter.addQueryPhrase(
				baseIndentationLevel + 1, 
				"ELSE ");
			queryFormatter.addQueryPhrase("to_timestamp(");
			queryFormatter.addQueryPhrase(cleanedTableFieldName);
			queryFormatter.addQueryPhrase(",");
			queryFormatter.addQueryPhrase("'");
			queryFormatter.addQueryPhrase(rifDataType.getMainValidationValue());
			queryFormatter.addQueryPhrase("')");
		}
		else if (RIFDataTypeFactory.isDoubleDataType(rifDataType)) {
			
			/*
			 * Generates a query fragment like:
			 * 
			 * ELSE cast(score AS DOUBLE PRECISION)
			 * 
			 */
			queryFormatter.addQueryPhrase(
				baseIndentationLevel + 1,
				"ELSE ");
			queryFormatter.addQueryPhrase("cast(");
			queryFormatter.addQueryPhrase(cleanedTableFieldName);
			queryFormatter.addQueryPhrase(" AS DOUBLE PRECISION)");			
		}
		else {
			//assume it is some kind of formatted text value
			//therefore it does not need to cast from text to text
			queryFormatter.addQueryPhrase(
				baseIndentationLevel + 1, 
				"ELSE ");
			queryFormatter.addQueryPhrase(cleanedTableFieldName);			
		}
		queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(baseIndentationLevel, " END AS ");		
		queryFormatter.addQueryPhrase(cleanedTableFieldName);

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


