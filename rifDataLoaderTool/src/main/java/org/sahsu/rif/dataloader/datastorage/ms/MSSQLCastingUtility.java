package org.sahsu.rif.dataloader.datastorage.ms;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLCreatePrimaryKeyQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLDeleteTableQueryFormatter;

/**
 * Contains code used to create queries that will cast text field values to
 * other data types such as date, integer and double precision.  Its input
 * is a table of text field values that have already been subject to 
 * search-and-replace and validation parts of the cleaning activity.
 * 
 * <p>
 * When text field values are cast to different types, it considers the
 * case when the validation activity has marked a field with <code>rif_error</code>.
 * When this code encounters <code>rif_error</code> values, it will put 
 * null values in the transformed table.
 * </p>
 * 
 * <p>
 * The result of using this code should be a table that now can use data types
 * that would be expected in parts of the RIF schema.  For example, after this 
 * step, a text field called <code>year</code> will be transformed into an 
 * integer type, which is what the RIF production schema will expect in numerator
 * and denominator tables.
 * </p>
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

public final class MSSQLCastingUtility {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLCastingUtility() {

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
		MSSQLDeleteTableQueryFormatter deleteQueryFormatter
			= new MSSQLDeleteTableQueryFormatter();
		//KLG_SCHEMA
		//deleteQueryFormatter.setDatabaseSchemaName("dbo");
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
		MSSQLCreatePrimaryKeyQueryFormatter createPrimaryKeyQueryFormatter
			= new MSSQLCreatePrimaryKeyQueryFormatter();
		//KLG_SCHEMA
		//createPrimaryKeyQueryFormatter.setDatabaseSchemaName("dbo");
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
				
		queryFormatter.addPaddedQueryLine(0, "SELECT");
		
		queryFormatter.addQueryLine(1, "data_set_id,");
		queryFormatter.addQueryLine(1, "row_number,");
	
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= DataSetConfigurationUtility.getRequiredAndExtraFieldConfigurations(
				dataSetConfiguration);
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
		queryFormatter.addQueryPhrase(0, "INTO ");//KLG_SCHEMA
		queryFormatter.addQueryPhrase(castingTableName);
		queryFormatter.padAndFinishLine();		
		queryFormatter.addPaddedQueryLine(0, "FROM");
		queryFormatter.addQueryPhrase(1, "");//KLG_SCHEMA
		queryFormatter.addQueryPhrase(cleanValidationTableName);
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
			
			/*
			 * #POSSIBLE_PORTING_ISSUE
			 * Does SQL Server have something similar to 
			 * to_timestamp() to do its conversion?
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
			 * #POSSIBLE_PORTING_ISSUE
			 * Both PostgreSQL and SQL Server appear to have
			 * support for a cast method but SQL Server also
			 * uses a convert() method, which may have implications
			 * for transforming some data types.
			 */
			
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


