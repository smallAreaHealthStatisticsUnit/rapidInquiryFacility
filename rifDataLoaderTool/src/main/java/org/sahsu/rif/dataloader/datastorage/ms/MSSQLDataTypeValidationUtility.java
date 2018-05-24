package org.sahsu.rif.dataloader.datastorage.ms;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldActionPolicy;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.ValidationRule;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLCreatePrimaryKeyQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLDeleteTableQueryFormatter;


/**
 * This is the class that the cleaning activity uses to validate
 * field values.  This class may present some porting issues because it 
 * heavily relies on regular expression patterns.  
 * The web site: <a href="https://www.pg-versus-ms.com/">
 * PostgreSQL vs MS SQL Server</a> page provides some interesting discussion
 * on this matter.  As well, some of the search and replace functions that
 * are advertised to RIF managers will almost certainly have to be re-implemented
 * for both PostgreSQL and SQL Server databases.
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

public class MSSQLDataTypeValidationUtility {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLDataTypeValidationUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateValidationTableStatement(
		final DataSetConfiguration dataSetConfiguration) {

		/*
		 * #POSSIBLE_PORTING_ISSUE
		 * Do PostgreSQL and SQL Server support regular expressions
		 * in an identical way?
		 */	
		
		/*
		 * DROP IF EXISTS cln_val_my_numerator1;
		 * CREATE TABLE cln_val_my_numerator1 AS
		 * SELECT
		 *    data_set_id,
		 *    row_number,
		 *    patient_id,
		 *    CASE
		 *       WHEN sex ~ ^[0|1|2|3] THEN sex
		 *       ELSE 'rif_error'
		 *    END AS sex,
		 *    CASE
		 *       WHEN is_valid_postal_code(postal_code) THEN postal_code
		 *       ELSE 'rif_error'
		 *    END AS postal_code
		 * FROM
		 *     cln_srch_my_numerator1; --the search and replace table
		 * ALTER TABLE cln_srch_my_numerator1 ADD PRIMARY KEY (data_set_id, row_number);    
		 */	
		
		
		//eg: my_numerator1
		String coreDataSetName
			= dataSetConfiguration.getName();
		
		
		//eg: cln_val_my_numerator1
		String cleanValidationTableName
			= RIFTemporaryTablePrefixes.CLEAN_VALIDATION.getTableName(coreDataSetName);
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
		deleteQueryFormatter.setTableToDelete(cleanValidationTableName);
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
		SQLGeneralQueryFormatter createValidationCTASQueryFormatter
			= new SQLGeneralQueryFormatter();		
		createValidationCTASStatement(
			createValidationCTASQueryFormatter,
			cleanSearchReplaceTableName,
			cleanValidationTableName,
			dataSetConfiguration);
		queryFormatter.addQuery(createValidationCTASQueryFormatter);
				
		//Add primary key statement
		MSSQLCreatePrimaryKeyQueryFormatter createPrimaryKeyQueryFormatter
			= new MSSQLCreatePrimaryKeyQueryFormatter();
		createPrimaryKeyQueryFormatter.setTable(cleanValidationTableName);
		createPrimaryKeyQueryFormatter.setPrimaryKeyPhrase("data_set_id, row_number");
		queryFormatter.addQuery(createPrimaryKeyQueryFormatter);

		System.out.println(queryFormatter.generateQuery());
		
		return queryFormatter.generateQuery();
	}
	
	private void createValidationCTASStatement(
		final SQLGeneralQueryFormatter queryFormatter,
		final String cleanSearchReplaceTableName,
		final String cleanValidationTableName,
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
			addDataSetFieldValidationFragment(
				1,
				queryFormatter,
				fieldConfigurations.get(i));			
		}
		queryFormatter.finishLine();
		queryFormatter.addQueryPhrase(0, "INTO ");
		queryFormatter.addQueryPhrase(cleanValidationTableName);
		queryFormatter.padAndFinishLine();		

		queryFormatter.addPaddedQueryLine(0, "FROM");
		queryFormatter.addQueryPhrase(1, cleanSearchReplaceTableName);
	}
	
	
	private void addDataSetFieldValidationFragment(
		final int baseIndentationLevel,
		final SQLGeneralQueryFormatter queryFormatter,
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
			
		String loadFieldName
			= dataSetFieldConfiguration.getLoadFieldName();
		String cleanFieldName
			= dataSetFieldConfiguration.getCleanFieldName();
		
		RIFDataType rifDataType
			= dataSetFieldConfiguration.getRIFDataType();
		FieldActionPolicy fieldValidationPolicy
			= rifDataType.getFieldValidationPolicy();
		if (fieldValidationPolicy == FieldActionPolicy.DO_NOTHING) {
			//just allow load field value to pass
			queryFormatter.addQueryPhrase(
				baseIndentationLevel, 
				cleanFieldName); //xxx
		}
		else if (fieldValidationPolicy == FieldActionPolicy.USE_RULES) {
			/*
			 * eg:
			 *
			 * CASE
			 *    WHEN age ~ '^[0-9]+' THEN age
			 *    ELSE 'rif_error'
			 * END AS age, //using validation rule
			 * 
			 */
			ArrayList<ValidationRule> validationRules
				= rifDataType.getValidationRules();
			if (validationRules.isEmpty()) {
				queryFormatter.addQueryPhrase(baseIndentationLevel, cleanFieldName);
			}
			else {
				
				queryFormatter.addQueryPhrase(baseIndentationLevel, "CASE");
				queryFormatter.padAndFinishLine();

				for (ValidationRule validationRule : validationRules) {
					queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "WHEN ");
					queryFormatter.addQueryPhrase(cleanFieldName);//xxx
					queryFormatter.addQueryPhrase(" LIKE ");
					queryFormatter.addQueryPhrase("'");
					queryFormatter.addQueryPhrase(validationRule.getValidValue());
					queryFormatter.addQueryPhrase("'");
					queryFormatter.addQueryPhrase(" THEN ");
					queryFormatter.addQueryPhrase(cleanFieldName);//xxx
					queryFormatter.padAndFinishLine();
				}
			
				if (dataSetFieldConfiguration.isEmptyValueAllowed()) {
					queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "WHEN ");
					queryFormatter.addQueryPhrase(cleanFieldName);//xxx
					queryFormatter.addQueryPhrase(" ='' ");
					queryFormatter.addQueryPhrase("THEN ");
					queryFormatter.addQueryPhrase(cleanFieldName);//xxx
					String allowBlankValuesMessage
						= RIFDataLoaderToolMessages.getMessage(
							"sqlQuery.comment.allowBlankValues");
					queryFormatter.addComment(allowBlankValuesMessage);		
					queryFormatter.padAndFinishLine();
				}	

				//does not fit any of the regular expressions, therefore is not valid
				queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "ELSE 'rif_error'");
				queryFormatter.padAndFinishLine();

				queryFormatter.addQueryPhrase(baseIndentationLevel, "END AS ");
				queryFormatter.addQueryPhrase(cleanFieldName);
			
			}
		}
		else if (fieldValidationPolicy == FieldActionPolicy.USE_FUNCTION) {
			
				
			/*
			 * eg: 
			 * 
			 * is_valid_uk_postal_code(postal_code)
			 * 
			 * for MS SQL eg:
			 * [dbo].[is_valid_uk_postal_code](postal_code)
			 * 
			 */
			queryFormatter.addQueryPhrase(baseIndentationLevel, "CASE");
			queryFormatter.padAndFinishLine();
			
			queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "WHEN ");
			
			String validationFunctionName
				= rifDataType.getValidationFunctionName();
			String parameterValuesPhrase
				= rifDataType.getValidationFunctionParameterValues();
			queryFormatter.addQueryPhrase("[dbo].[");
			queryFormatter.addQueryPhrase(validationFunctionName);
			queryFormatter.addQueryPhrase("]");
			queryFormatter.addQueryPhrase("(");
			queryFormatter.addQueryPhrase(cleanFieldName);
			
			if (RIFDataTypeFactory.isDateDataType(rifDataType)) {
				//in the case of dates, we call a validation function, but
				//we also make use of the validation expressions to obtain
				//the date format that should be used
				ArrayList<ValidationRule> validationRules
					= rifDataType.getValidationRules();
				queryFormatter.addQueryPhrase(",'");
				queryFormatter.addQueryPhrase(validationRules.get(0).getValidValue());
				queryFormatter.addQueryPhrase("'");
			}
			queryFormatter.addQueryPhrase(") = 1 THEN ");
			queryFormatter.addQueryPhrase(cleanFieldName);
			queryFormatter.padAndFinishLine();
				
			queryFormatter.addQueryPhrase(baseIndentationLevel + 1, "ELSE 'rif_error'");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(baseIndentationLevel, "END AS ");
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


