package org.sahsu.rif.dataloader.datastorage.ms;

import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataLoadingResultTheme;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.datastorage.QueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 * Contains the code used to apply data quality assessments of 
 * individual CSV fields.  The two types are:
 * <ul>
 * <li>
 * report the percent of empty field values for a given column
 * </li>
 * <li>
 * report the percent of empty field values for a given column for
 * a given year.
 * </li>
 * </ul>
 *
 * <p>
 * These values are stored in separate tables of the form 
 * <code>aud_chg_[schema area]_counts</code> and 
 * <code>aud_val_[schema area]_counts</code>.
 * </p>
 * 
 * <p>
 * This is also the class that contains code for identifying duplicate
 * rows in a CSV file.
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

final public class MSSQLCheckWorkflowManager 
	extends AbstractMSSQLDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private MSSQLOptimiseWorkflowManager optimiseWorkflowManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLCheckWorkflowManager(
		final MSSQLOptimiseWorkflowManager optimiseWorkflowManager) {
	
		this.optimiseWorkflowManager = optimiseWorkflowManager;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void checkConfiguration(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		String coreDataSetName 
			= dataSetConfiguration.getName();
		String checkTableName
			= RIFTemporaryTablePrefixes.CHECK.getTableName(coreDataSetName);
		deleteTable(
			connection, 
			logFileWriter,
			checkTableName);

		createCheckTable(
			connection, 
			logFileWriter,
			exportDirectoryPath,
			dataSetConfiguration);
		
		
		createEmptyFieldCheckDataQualityTable(
			connection,
			logFileWriter,
			exportDirectoryPath,
			dataSetConfiguration);

		
		createEmptyPerYearFieldCheckDataQualityTable(
			connection,
			logFileWriter,
			exportDirectoryPath,
			dataSetConfiguration);
		
		exportTable(
				connection,
				logFileWriter,
				exportDirectoryPath,
				DataLoadingResultTheme.ARCHIVE_STAGES,
				checkTableName);
		
		updateLastCompletedWorkState(
				connection,
				logFileWriter,
				dataSetConfiguration,
				WorkflowState.CHECK);
		
		
	}

	private void createCheckTable(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
				
		String coreDataSetName 
			= dataSetConfiguration.getName();
		String optimiseTableName
			= RIFTemporaryTablePrefixes.OPTIMISE.getTableName(coreDataSetName);
		String checkTableName
			= RIFTemporaryTablePrefixes.CHECK.getTableName(coreDataSetName);
		

		PreparedStatement statement = null;
		try {
			deleteTable(
				connection, 
				logFileWriter,
				checkTableName);
			
			optimiseWorkflowManager.deleteIndices(
				connection, 
				logFileWriter, 
				dataSetConfiguration, RIFTemporaryTablePrefixes.CHECK);
			
			/*
			 * #POSSIBLE_PORTING_ISSUE
			 * There may be issues with porting the way the WITH statement
			 * is written?
			 */	
			SQLGeneralQueryFormatter queryFormatter 
				= new SQLGeneralQueryFormatter();

			queryFormatter.addPaddedQueryLine(0, "WITH duplicate_rows AS");
			queryFormatter.addPaddedQueryLine(1, "(SELECT");
			queryFormatter.addQueryLine(2, "data_set_id,");
			queryFormatter.addQueryLine(2, "row_number,");
			
			
			//add in all the fields we're promoting from converted table
			queryFormatter.addPaddedQueryLine(2, "row_number() OVER");
			queryFormatter.addPaddedQueryLine(3, "(PARTITION BY");
			

			String[] duplicateCriteriaFields
				= getDuplicateIdentificationFieldNames(dataSetConfiguration);
			
			for (int i = 0; i < duplicateCriteriaFields.length; i++) {
				if (i != 0) {
					queryFormatter.addQueryPhrase(",");
					queryFormatter.finishLine();
				}
				queryFormatter.addQueryPhrase(
					4, 
					duplicateCriteriaFields[i]);
			}			
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(3, "ORDER BY");
			for (int i = 0; i < duplicateCriteriaFields.length; i++) {
				if (i != 0) {
					queryFormatter.addQueryPhrase(",");
					queryFormatter.finishLine();
				}
				queryFormatter.addQueryPhrase(
					4, 
					duplicateCriteriaFields[i]);
			}			
			queryFormatter.addQueryPhrase(") AS duplicate_number");
			
			addOptimiseFields(
				dataSetConfiguration, 
				2, 
				queryFormatter);
			queryFormatter.padAndFinishLine();
			
			queryFormatter.addPaddedQueryLine(1, "FROM");
			queryFormatter.addQueryPhrase(2, optimiseTableName);		
			queryFormatter.addQueryPhrase(")");
			queryFormatter.padAndFinishLine();
			queryFormatter.addPaddedQueryLine(0, "SELECT");
			queryFormatter.addQueryLine(1, "data_set_id,");
			queryFormatter.addQueryLine(1, "row_number,");

			//add in all the fields we're promoting from converted table
	
			queryFormatter.addPaddedQueryLine(1, "CASE");
			queryFormatter.addPaddedQueryLine(
				2, 
				"WHEN duplicate_number=1 THEN 'Y'");
			queryFormatter.addPaddedQueryLine(
				2,
				"ELSE 'N'");
			queryFormatter.addQueryPhrase(1, "END AS keep_record");
			addOptimiseFields(
				dataSetConfiguration, 
				1, 
				queryFormatter);
			queryFormatter.padAndFinishLine();
			
			queryFormatter.addQueryPhrase(0, "INTO ");
			queryFormatter.addQueryPhrase(checkTableName);
			queryFormatter.padAndFinishLine();
			
			queryFormatter.addPaddedQueryLine(0, "FROM");
			queryFormatter.addQueryPhrase(1, "duplicate_rows");
			
			logSQLQuery(
				logFileWriter,
				"checkConfiguration", 
				queryFormatter);
						
			statement 
				= createPreparedStatement(connection, queryFormatter);
			statement.executeUpdate();

			addPrimaryKey(
				connection,
				logFileWriter,
				checkTableName,
				"data_set_id, row_number");
			
			addComments(
				connection,
				logFileWriter,
				checkTableName,
				dataSetConfiguration,
				WorkflowState.CHECK);
			
			optimiseWorkflowManager.createIndices(
				connection, 
				logFileWriter, 
				dataSetConfiguration, 
				RIFTemporaryTablePrefixes.CHECK);
			
			exportTable(
				connection, 
				logFileWriter, 
				exportDirectoryPath, 
				DataLoadingResultTheme.ARCHIVE_STAGES, 
				checkTableName);
			
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"checkWorkflowManager.error.unableToCreateCheckedTable",
					checkTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}		
		
	}
	
	private String[] getDuplicateIdentificationFieldNames(
		final DataSetConfiguration dataSetConfiguration) {
		

		Collator collator = GENERIC_MESSAGES.getCollator();

		ArrayList<String> filteredDuplicateCriteriaFields
			= new ArrayList<String>();
		
		String[] duplicateCriteriaFields
			= dataSetConfiguration.getFieldsUsedForDuplicationChecks();
		for (String duplicateCriterionField : duplicateCriteriaFields) {
			if ((collator.equals(duplicateCriterionField, "age") == false) &&
				(collator.equals(duplicateCriterionField, "sex") == false)) {
				
				filteredDuplicateCriteriaFields.add(duplicateCriterionField);			
			}			
		}
		
		RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
		if ( (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
			 (rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
			filteredDuplicateCriteriaFields.add("age_sex_group");
		}

		String[] results = filteredDuplicateCriteriaFields.toArray(new String[0]);
		return results;		
	}
	
	private void addOptimiseFields(
		final DataSetConfiguration dataSetConfiguration,
		final int indentationLevel,
		final QueryFormatter queryFormatter) {
		
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= DataSetConfigurationUtility.getRequiredAndExtraFieldConfigurations(
				dataSetConfiguration);
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (excludeFieldFromChecks(fieldConfiguration) == false) {
				String convertFieldName 
					= fieldConfiguration.getConvertFieldName();
				queryFormatter.addQueryPhrase(",");				
				queryFormatter.padAndFinishLine();				
				queryFormatter.addQueryPhrase(
					indentationLevel, 
					convertFieldName);
			}
		}
		
		RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
		if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
			(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
			
			queryFormatter.addQueryPhrase(",");				
			queryFormatter.padAndFinishLine();				
			queryFormatter.addQueryPhrase(
				indentationLevel, 
				"age_sex_group");			
		}

	}

	private void createEmptyFieldCheckDataQualityTable(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {

		String coreDataSetName = dataSetConfiguration.getName();
		
		String emptyFieldsDataQualityTableName
			= RIFTemporaryTablePrefixes.EMPTY_CHECK.getTableName(coreDataSetName);
		deleteTable(
			connection, 
			logFileWriter,
			emptyFieldsDataQualityTableName);
		
		PreparedStatement statement = null;
		try {

			/*
			 * #POSSIBLE_PORTING_ISSUE
			 * There may be issues with porting the way the WITH statement
			 * is written?
			 */	
			
			/*
			 * Trying to create a table such as:
			 * 
			 * CREATE TABLE dq_my_data_set_empty AS
			 * WITH
			 *    summary AS
			 *       (SELECT
			 *           COUNT(data_set_id) AS total_rows
			 *        FROM
			 *           check_my_data_set)
			 *    tmp_field1_empty AS
			 *       (SELECT
			 *           COUNT(data_set_id) AS total_empty_rows
			 *        FROM
			 *           check_my_data_set
			 *        WHERE
			 *           field1 IS NULL),
			 *    tmp_field2_empty AS
			 *       (SELECT
			 *           COUNT(data_set_id) AS total_empty_rows
			 *        FROM
			 *           check_my_data_set
			 *        WHERE
			 *           field2 IS NULL)
			 * SELECT
			 *    data_set_id,
			 *    (tmp_field1_empty.total_empty_rows / summary.total_rows) * 100 AS field1,
			 *    (tmp_field2_empty.total_empty_rows / summary.total_rows) * 100 AS field2,
			 * FROM
			 *    summary,
			 *    tmp_field1_empty,
			 *    tmp_field2_empty
			 */
			
			
			String checkTableName
				= RIFTemporaryTablePrefixes.CHECK.getTableName(coreDataSetName);
		
		
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		
			queryFormatter.addPaddedQueryLine(0, "WITH");
			queryFormatter.addQueryPhrase(1, "identifiers AS");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(2, "(SELECT ");
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(3, "DISTINCT(data_set_id) AS id");
			queryFormatter.addQueryPhrase(2, "FROM");
			queryFormatter.addQueryPhrase(3, checkTableName);
			queryFormatter.addQueryPhrase("),");
			queryFormatter.finishLine();
			
			queryFormatter.addQueryPhrase(1, "summary AS");
			queryFormatter.padAndFinishLine();
			queryFormatter.addPaddedQueryLine(2, "(SELECT");
			queryFormatter.addPaddedQueryLine(3, "COUNT(data_set_id) AS total_rows");		
			queryFormatter.addPaddedQueryLine(2, "FROM");
			queryFormatter.addQueryPhrase(3, checkTableName);
			queryFormatter.addQueryPhrase(")");
		
			RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
			
			//do for each field
			ArrayList<DataSetFieldConfiguration> fieldsWithEmptyFieldCheck
				= dataSetConfiguration.getFieldsWithEmptyFieldCheck();
			for (DataSetFieldConfiguration fieldWithEmptyFieldCheck : fieldsWithEmptyFieldCheck) {
				String convertFieldName
					= fieldWithEmptyFieldCheck.getConvertFieldName();
				
				if (isFieldPartOfAgeSexGroup(
						rifSchemaArea,
						convertFieldName)  == false) {
				
					queryFormatter.addQueryPhrase(",");
					queryFormatter.padAndFinishLine();

					queryFormatter.addQueryPhrase(1, "tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty AS");
					queryFormatter.padAndFinishLine();
					queryFormatter.addPaddedQueryLine(2, "(SELECT");
					queryFormatter.addPaddedQueryLine(3, "COUNT(data_set_id) AS total_empty_rows");
					queryFormatter.addPaddedQueryLine(2, "FROM");
					queryFormatter.addPaddedQueryLine(3, checkTableName);
					queryFormatter.addPaddedQueryLine(2, "WHERE");
					queryFormatter.addQueryPhrase(3, convertFieldName);
					queryFormatter.addQueryPhrase(" IS NULL)");
				}
			}
			

			if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
				(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {

				//there will be a field called age_sex_group
				queryFormatter.addQueryPhrase(",");
				queryFormatter.padAndFinishLine();

				queryFormatter.addQueryPhrase(1, "tmp_age_sex_group_empty AS ");
				queryFormatter.padAndFinishLine();
				queryFormatter.addPaddedQueryLine(2, "(SELECT");
				queryFormatter.addPaddedQueryLine(3, "COUNT(data_set_id) AS total_empty_rows");
				queryFormatter.addPaddedQueryLine(2, "FROM");
				queryFormatter.addPaddedQueryLine(3, checkTableName);
				queryFormatter.addPaddedQueryLine(2, "WHERE");
				queryFormatter.addQueryPhrase(3, "age_sex_group = -1)");
			}
						
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(1, "SELECT");
			queryFormatter.addQueryPhrase(2, "id AS data_set_id");
		
			for (DataSetFieldConfiguration fieldWithEmptyFieldCheck : fieldsWithEmptyFieldCheck) {
				String convertFieldName
					= fieldWithEmptyFieldCheck.getConvertFieldName();

				
				if (isFieldPartOfAgeSexGroup(
					rifSchemaArea,
					convertFieldName)  == false) {
				
					queryFormatter.addQueryPhrase(",");
					queryFormatter.finishLine();

					queryFormatter.addQueryPhrase(2, "tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty.total_empty_rows AS ");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_total,");
					queryFormatter.finishLine();				
					queryFormatter.addQueryPhrase(2, "(CAST(tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty.total_empty_rows AS numeric) / CAST(summary.total_rows AS numeric)) ");
					queryFormatter.addQueryPhrase("* 100 AS ");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_percent");			
				}
			}

			if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
				(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {

				//there will be a field called age_sex_group
				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();

				queryFormatter.addQueryPhrase(2, "tmp_age_sex_group_empty.total_empty_rows AS ");
				queryFormatter.addQueryPhrase("age_sex_group_total,");
				queryFormatter.finishLine();				
				queryFormatter.addQueryPhrase(2, "(CAST(tmp_age_sex_group_empty.total_empty_rows AS numeric) ");
				queryFormatter.addQueryPhrase("/ CAST(summary.total_rows AS numeric)) ");
				queryFormatter.addQueryPhrase("* 100 AS ");
				queryFormatter.addQueryPhrase("age_sex_group_total_percent");
			}
			
			queryFormatter.addQueryPhrase(",");
			queryFormatter.finishLine();

			queryFormatter.addQueryPhrase(2, "summary.total_rows AS total_rows");
			queryFormatter.finishLine();

			queryFormatter.addQueryPhrase(0, "INTO ");
			queryFormatter.addQueryPhrase(emptyFieldsDataQualityTableName);
			queryFormatter.finishLine();

			
			queryFormatter.addPaddedQueryLine(1, "FROM");
			queryFormatter.addQueryLine(2, "identifiers,");
			queryFormatter.addQueryPhrase(2, "summary");
			for (DataSetFieldConfiguration fieldWithEmptyFieldCheck : fieldsWithEmptyFieldCheck) {
				String convertFieldName
					= fieldWithEmptyFieldCheck.getConvertFieldName();
				
				if (isFieldPartOfAgeSexGroup(
					rifSchemaArea,
					convertFieldName)  == false) {
				
					queryFormatter.addQueryPhrase(",");
					queryFormatter.finishLine();

					queryFormatter.addQueryPhrase(2, "tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty");
				
				}
			}


			if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
				(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {

				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();
				queryFormatter.addQueryPhrase(2, "tmp_age_sex_group_empty");
			}
						
			logSQLQuery(
				logFileWriter,
				"createEmptyFieldCheckDataQualityTable", 
				queryFormatter);
			
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.executeUpdate();

			addPrimaryKey(
				connection,
				logFileWriter,
				emptyFieldsDataQualityTableName,
				"data_set_id");

			exportTable(
				connection, 
				logFileWriter, 
				exportDirectoryPath, 
				DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL,
				emptyFieldsDataQualityTableName);
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"checkWorkflowManager.error.unableToCreateEmptyCheckTable",
					emptyFieldsDataQualityTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}

	}

	
	private boolean isFieldPartOfAgeSexGroup(
		final RIFSchemaArea rifSchemaArea,
		final String convertFieldName) {
		
		if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
			(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
			
			Collator collator = GENERIC_MESSAGES.getCollator();
			
			if (collator.equals(convertFieldName, "age") ||
				collator.equals(convertFieldName, "sex") ||
				collator.equals(convertFieldName, "age_sex_group")) {
				return true;
			}
		}
		
		return false;		
	}
	
	
	private void createEmptyPerYearFieldCheckDataQualityTable(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {

		String coreDataSetName = dataSetConfiguration.getName();
		
		String emptyPerYearFieldsDataQualityTableName
			= RIFTemporaryTablePrefixes.EMPTY_PER_YEAR_CHECK.getTableName(coreDataSetName);
		deleteTable(
			connection, 
			logFileWriter,
			emptyPerYearFieldsDataQualityTableName);
		
		PreparedStatement statement = null;
		try {

			
			/*
			 * Trying to create a table such as:
			 * 
			 * CREATE TABLE dq_my_data_set_empty_yr AS
			 * WITH
			 *    summary AS
			 *       (SELECT
			 *           year,
			 *           COUNT(data_set_id) AS total_rows
			 *        FROM
			 *           check_my_data_set
			 *        GROUP BY
			 *           year)
			 *    tmp_field1_empty_yr AS
			 *       (SELECT
			 *           year,
			 *           COUNT(data_set_id) AS total_empty_rows
			 *        FROM
			 *           check_my_data_set
			 *        WHERE
			 *           field1 IS NULL
			 *        GROUP BY
			 *           year),
			 *    tmp_field2_empty_yr AS
			 *       (SELECT
			 *           year,
			 *           COUNT(data_set_id) AS total_empty_rows
			 *        FROM
			 *           check_my_data_set
			 *        WHERE
			 *           field2 IS NULL
			 *        GROUP BY
			 *           year)
			 * 
			 * CREATE TABLE dq_my_data_set AS
			 * SELECT
			 *    data_set_id,
			 *    year,
			 *    (tmp_field1_empty_yr.total_empty_rows / summary.total_rows) * 100 AS field1,
			 *    (tmp_field2_empty_yr.total_empty_rows / summary.total_rows) * 100 AS field2,
			 * FROM
			 *    summary,
			 *    tmp_field1_empty_yr,
			 *    tmp_field2_empty_yr
			 * WHERE
			 *    summary.year = tmp_field1_empty_yr.year AND
			 *    summary.year = tmp_field2_empty_yr.year
			 * ORDER BY
			 *    year
			 *    
			 */
			
			
			String checkTableName
				= RIFTemporaryTablePrefixes.CHECK.getTableName(coreDataSetName);
		
		
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		
			queryFormatter.addPaddedQueryLine(0, "WITH");
			queryFormatter.addQueryPhrase(1, "identifiers AS");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(2, "(SELECT ");
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(3, "DISTINCT(data_set_id) AS id");
			queryFormatter.addPaddedQueryLine(2, "FROM");
			queryFormatter.addQueryPhrase(3, checkTableName);
			queryFormatter.addQueryPhrase("),");
			queryFormatter.finishLine();
			
			queryFormatter.addQueryPhrase(1, "summary AS");
			queryFormatter.padAndFinishLine();
			queryFormatter.addPaddedQueryLine(2, "(SELECT");			
			queryFormatter.addPaddedQueryLine(3, "year,");		
			queryFormatter.addPaddedQueryLine(3, "COUNT(data_set_id) AS total_rows");		
			queryFormatter.addPaddedQueryLine(2, "FROM");
			queryFormatter.addQueryPhrase(3, checkTableName);
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(2, "GROUP BY");
			queryFormatter.addQueryPhrase(3, "year)");

			RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
			
			//do for each field
			ArrayList<DataSetFieldConfiguration> fieldsWithEmptyPerYearFieldCheck
				= dataSetConfiguration.getFieldsWithEmptyFieldCheck();
			for (DataSetFieldConfiguration fieldWithEmptyPerYearFieldCheck : fieldsWithEmptyPerYearFieldCheck) {
				String convertFieldName
					= fieldWithEmptyPerYearFieldCheck.getConvertFieldName();

				if ((isFieldPartOfAgeSexGroup(
						rifSchemaArea,
						convertFieldName)  == false) &&
					(isYearField(convertFieldName) == false)) {

					queryFormatter.addQueryPhrase(",");
					queryFormatter.padAndFinishLine();

					queryFormatter.addQueryPhrase(1, "tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty_yr AS");
					queryFormatter.padAndFinishLine();
					queryFormatter.addPaddedQueryLine(2, "(SELECT");
					queryFormatter.addPaddedQueryLine(3, "year,");
					queryFormatter.addPaddedQueryLine(3, "COUNT(data_set_id) AS total_empty_rows");
					queryFormatter.addPaddedQueryLine(2, "FROM");
					queryFormatter.addPaddedQueryLine(3, checkTableName);
					queryFormatter.addPaddedQueryLine(2, "WHERE");
					queryFormatter.addQueryPhrase(3, convertFieldName);
					queryFormatter.addQueryPhrase(" IS NULL");
					queryFormatter.finishLine();
					queryFormatter.addPaddedQueryLine(2, "GROUP BY");
					queryFormatter.addQueryPhrase(3, "year)");
				}
			}
			
			if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
				(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {

				queryFormatter.addQueryPhrase(",");
				queryFormatter.padAndFinishLine();

				queryFormatter.addQueryPhrase(1, "tmp_age_sex_group_empty_yr AS ");
				queryFormatter.padAndFinishLine();
				queryFormatter.addPaddedQueryLine(2, "(SELECT");
				queryFormatter.addPaddedQueryLine(3, "year,");
				queryFormatter.addPaddedQueryLine(3, "COUNT(data_set_id) AS total_empty_rows");
				queryFormatter.addPaddedQueryLine(2, "FROM");
				queryFormatter.addPaddedQueryLine(3, checkTableName);
				queryFormatter.addPaddedQueryLine(2, "WHERE");
				queryFormatter.addQueryPhrase(3, "age_sex_group = -1");
				queryFormatter.finishLine();
				queryFormatter.addPaddedQueryLine(2, "GROUP BY");
				queryFormatter.addQueryPhrase(3, "year)");
			}
			
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(1, "SELECT");
			queryFormatter.addQueryLine(2, "id AS data_set_id,");
			queryFormatter.addQueryPhrase(2, "summary.year");		
			for (DataSetFieldConfiguration fieldWithEmptyPerYearFieldCheck : fieldsWithEmptyPerYearFieldCheck) {
				
				String convertFieldName
					= fieldWithEmptyPerYearFieldCheck.getConvertFieldName();

				if ((isFieldPartOfAgeSexGroup(
						rifSchemaArea,
						convertFieldName)  == false) &&
					(isYearField(convertFieldName) == false)) {

					/*
					 * #POSSIBLE_PORTING_ISSUE
					 * Coalesce may be implemented the same way for both
					 * SQL Server and PostgreSQL?
					 */	
					queryFormatter.addQueryPhrase(",");
					queryFormatter.finishLine();		
					queryFormatter.addQueryPhrase(2, "COALESCE(tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty_yr.total_empty_rows, 0) AS ");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_total,");
					queryFormatter.finishLine();
				
					queryFormatter.addQueryPhrase(2, "(CAST(COALESCE(tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty_yr.total_empty_rows, 0) AS numeric) / CAST(summary.total_rows AS numeric)) ");
					queryFormatter.addQueryPhrase("* 100 AS ");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_percent");

				}
			}
			

			if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
				(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {

				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();		
				queryFormatter.addQueryPhrase(2, "COALESCE(tmp_age_sex_group_empty_yr.");
				queryFormatter.addQueryPhrase("total_empty_rows, 0) AS ");
				queryFormatter.addQueryPhrase("age_sex_group_total,");
				queryFormatter.finishLine();			
				queryFormatter.addQueryPhrase(2, "(CAST(COALESCE(tmp_age_sex_group_empty_yr.");
				queryFormatter.addQueryPhrase("total_empty_rows, 0) AS numeric) / CAST(summary.total_rows AS numeric)) ");
				queryFormatter.addQueryPhrase("* 100 AS ");
				queryFormatter.addQueryPhrase("age_sex_group_percent");
			}
			
			queryFormatter.addQueryPhrase(",");
			queryFormatter.finishLine();
			
			queryFormatter.addQueryPhrase(2, "COALESCE(summary.total_rows, 0) AS total_rows");
			queryFormatter.padAndFinishLine();
			
			queryFormatter.addQueryPhrase(0, "INTO ");
			queryFormatter.addQueryPhrase(emptyPerYearFieldsDataQualityTableName);
			queryFormatter.finishLine();
			
			queryFormatter.addPaddedQueryLine(1, "FROM");
			queryFormatter.addQueryLine(2, "identifiers,");
			queryFormatter.addQueryPhrase(2, "summary");
			
			for (int i = 0; i < fieldsWithEmptyPerYearFieldCheck.size(); i++) {
				DataSetFieldConfiguration fieldWithEmptyPerYearFieldCheck
					= fieldsWithEmptyPerYearFieldCheck.get(i);

				String convertFieldName
					= fieldWithEmptyPerYearFieldCheck.getConvertFieldName();

				if ((isFieldPartOfAgeSexGroup(
						rifSchemaArea,
						convertFieldName)  == false) &&
					(isYearField(convertFieldName) == false)) {
					
					queryFormatter.finishLine();

					queryFormatter.addQueryPhrase(3, "LEFT JOIN tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty_yr ON ");
					queryFormatter.addQueryPhrase("summary.year=tmp_");
					queryFormatter.addQueryPhrase(convertFieldName);
					queryFormatter.addQueryPhrase("_empty_yr.year");
				}
			}

			if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
				(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {

				queryFormatter.finishLine();

				queryFormatter.addQueryPhrase(3, "LEFT JOIN tmp_age_sex_group_empty_yr ON ");
				queryFormatter.addQueryPhrase("summary.year=");
				queryFormatter.addQueryPhrase("tmp_age_sex_group_empty_yr.year");
			}
			
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(1, "ORDER BY");
			queryFormatter.addPaddedQueryLine(2, "summary.year");

			// need to set the fields that will be the PK to not nullable
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(0, "ALTER TABLE ");
			queryFormatter.addQueryPhrase(emptyPerYearFieldsDataQualityTableName);
			queryFormatter.addQueryPhrase(" ALTER COLUMN data_set_id int NOT NULL");
			queryFormatter.finishLine();
			queryFormatter.addPaddedQueryLine(0, "ALTER TABLE ");
			queryFormatter.addQueryPhrase(emptyPerYearFieldsDataQualityTableName);
			queryFormatter.addQueryPhrase(" ALTER COLUMN year int NOT NULL");
			
			logSQLQuery(
				logFileWriter,
				"createEmptyFieldCheckDataQualityTable", 
				queryFormatter);
			
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.executeUpdate();
			
			addPrimaryKey(
				connection,
				logFileWriter,
				emptyPerYearFieldsDataQualityTableName,
				"data_set_id, year");


			exportTable(
				connection, 
				logFileWriter, 
				exportDirectoryPath, 
				DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL,
				emptyPerYearFieldsDataQualityTableName);
			
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"checkWorkflowManager.error.unableToCreateEmptyCheckTable",
					emptyPerYearFieldsDataQualityTableName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
	}

	private boolean isYearField(
		final String convertFieldName) {
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		if (collator.equals(convertFieldName, "year")) {
			return true;
		}
		
		return false;
		
	}
	
	private boolean excludeFieldFromChecks(
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
		
		String convertFieldName
			= dataSetFieldConfiguration.getConvertFieldName();
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		if (collator.equals(convertFieldName, "age")) {
			return true;
		}
		else if (collator.equals(convertFieldName, "sex")) {
			return true;
		}
		else {
			return false;
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


