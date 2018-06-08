package org.sahsu.rif.dataloader.datastorage.ms;

import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataLoadingResultTheme;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldChangeAuditLevel;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.datastorage.DeleteRowsQueryFormatter;
import org.sahsu.rif.generic.datastorage.RecordExistsQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.ms.MSSQLInsertQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLRecordExistsQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLSelectQueryFormatter;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 * This manager class provides the code used to record descriptions of data 
 * cleaning changes.  It will audit two types of changes 
 * <ul>
 * <li>
 * changes made: eg "Row W field X changed from Y to Z"
 * </li>
 * <li>
 * validation failures: eg "row W field X had invalid field value Y"
 * </li>
 * </ul>
 *
 * <p>
 * The sentiment of these will be recorded in CSV tables.
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

final public class MSSQLChangeAuditManager 
	extends AbstractMSSQLDataLoaderStepManager {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLChangeAuditManager() {
						
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void clearChangeLogs(
		final Connection connection,
		final Writer logFileWriter)
		throws RIFServiceException {

		DeleteRowsQueryFormatter clearChangeLogQueryFormatter
			= new DeleteRowsQueryFormatter();
		//KLG_SCHEMA
		//clearChangeLogQueryFormatter.setDatabaseSchemaName("dbo");
		clearChangeLogQueryFormatter.setFromTable("rif_change_log");		

		DeleteRowsQueryFormatter clearValidationFailuresLogQueryFormatter
			= new DeleteRowsQueryFormatter();
		//KLG_SCHEMA
		//clearValidationFailuresLogQueryFormatter.setDatabaseSchemaName("dbo");
		clearValidationFailuresLogQueryFormatter.setFromTable("rif_failed_val_log");			
		
		PreparedStatement clearChangeLogStatement = null;
		PreparedStatement clearValidationFailuresLogStatement = null;
		try {
			clearChangeLogStatement
				= createPreparedStatement(connection, clearChangeLogQueryFormatter);
			clearChangeLogStatement.executeUpdate();
			clearValidationFailuresLogStatement
				= createPreparedStatement(connection, clearValidationFailuresLogQueryFormatter);
			clearValidationFailuresLogStatement.executeUpdate();
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"changeAuditManager.error.unableToClearAuditLogs");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(clearChangeLogStatement);
			SQLQueryUtility.close(clearValidationFailuresLogStatement);
		}
	}
	
	
	public void auditValidationFailures(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		/**
		 * In theory, the cleaning routines should mean that all the values
		 * are legal.  However, this may not be the case so we have to check
		 */
		String coreDataSetName
			= dataSetConfiguration.getName();
		
		String cleaningSearchReplaceTableName
			= RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE.getTableName(coreDataSetName);
		String cleaningValidationTableName
			= RIFTemporaryTablePrefixes.CLEAN_VALIDATION.getTableName(coreDataSetName);
		
		String auditValidationFailuresTable
			= RIFTemporaryTablePrefixes.AUD_FAILED_VALIDATION.getTableName(coreDataSetName);
		deleteTable(
			connection, 
			logFileWriter, 
			auditValidationFailuresTable);
		
		
		ArrayList<DataSetFieldConfiguration> fieldsWithValidationChecks
			= dataSetConfiguration.getFieldsWithValidationChecks();
			
		/*
		 * CREATE TABLE aud_val_my_numerator AS 
		 * SELECT
		 *    cln_srch_my_numerator.data_set_id,
		 *    cln_srch_my_numerator.row_number,
		 *    'age' AS field_name,
		 *    cln_srch_my_numerator.age AS invalid_field_value
		 *    current_timestamp AS time_stamp
		 * FROM
		 *    cln_srch_my_numerator,
		 *    cln_val_my_numerator
		 * WHERE
		 *    cln_srch_my_numerator.data_set_id=cln_val_my_numerator.data_set_id AND
		 *    cln_srch_my_numerator.row_number=cln_val_my_numerator.row_number AND
		 * 	  cln_val_my_numerator.age='rif_error';
		 * 
		 * 
		 */

		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		//queryFormatter.addQueryPhrase(0, "CREATE TABLE ");//KLG_SCHEMA
		//queryFormatter.addQueryPhrase(auditValidationFailuresTable);
		//queryFormatter.addQueryPhrase(" AS");		
		//queryFormatter.padAndFinishLine();
		queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
		queryFormatter.addQueryPhrase(auditValidationFailuresTable);
		queryFormatter.addQueryPhrase(" (data_set_id INTEGER NOT NULL, row_number INTEGER NOT NULL, field_name NVARCHAR(30) NOT NULL, invalid_field_value NVARCHAR(30), time_stamp DATETIME NOT NULL) ");
		queryFormatter.padAndFinishLine();
		// Now some insert into 
		queryFormatter.addQueryPhrase(0, "INSERT INTO ");
		queryFormatter.addQueryPhrase(auditValidationFailuresTable);
		queryFormatter.addQueryPhrase(" (data_set_id, row_number, field_name, invalid_field_value, time_stamp) ");

		
		int numberOfFieldsWithValidationChecks = fieldsWithValidationChecks.size();
		for (int i = 0; i < numberOfFieldsWithValidationChecks; i++) {
			DataSetFieldConfiguration dataSetFieldConfiguration
				= fieldsWithValidationChecks.get(i);
			if (i != 0) {			
				queryFormatter.padAndFinishLine();
				queryFormatter.addQueryPhrase(0, "UNION ALL");
				queryFormatter.padAndFinishLine();	
			}
			String fieldLevelFailureSelectQuery
				= createFieldLevelFailures(
					cleaningSearchReplaceTableName,
					cleaningValidationTableName,
					dataSetFieldConfiguration);
			queryFormatter.addQueryPhrase(fieldLevelFailureSelectQuery);
		}
		System.out.println(queryFormatter.generateQuery());

		logSQLQuery(
			logFileWriter,
			"add to validation table", 
			queryFormatter);
		PreparedStatement statement = null;
		try {
			statement	
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"changeAuditManager.error.unableToAuditValidationFailures");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		
		exportTable(
				connection,
				logFileWriter,
				exportDirectoryPath,
				DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL,
				auditValidationFailuresTable);
	}
	
	private String createFieldLevelFailures(
		final String searchReplaceTableName,
		final String searchValidationTableName,
		final DataSetFieldConfiguration dataSetFieldConfiguration)
		throws RIFServiceException {
		
		String cleanFieldName = dataSetFieldConfiguration.getCleanFieldName();
		
		SelectQueryFormatter queryFormatter
			= new MSSQLSelectQueryFormatter();
		queryFormatter.setEndWithSemiColon(false);
		queryFormatter.addSelectField(searchReplaceTableName, "data_set_id");
		queryFormatter.addSelectField(searchReplaceTableName, "row_number");
		queryFormatter.addTextLiteralSelectField(cleanFieldName, "field_name");
		queryFormatter.addSelectFieldWithAlias(
			searchReplaceTableName, 
			cleanFieldName, 
			"invalid_field_value");
		queryFormatter.addSelectFieldWithAlias("current_timestamp", "time_stamp");
		//KLG_SCHEMA
		//queryFormatter.setDatabaseSchemaName("dbo");
		queryFormatter.addFromTable(searchReplaceTableName);
		queryFormatter.addFromTable(searchValidationTableName);
		queryFormatter.addWhereJoinCondition(
			searchReplaceTableName, 
			"data_set_id", 
			searchValidationTableName, 
			"data_set_id");
		queryFormatter.addWhereJoinCondition(
			searchReplaceTableName, 
			"row_number", 
			searchValidationTableName, 
			"row_number");
		
		queryFormatter.addWhereParameterWithLiteralValue(
			searchValidationTableName,
			cleanFieldName,
			"rif_error");
		
		return queryFormatter.generateQuery();
	}
		
	public void auditDataCleaningChanges(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.setEndWithSemiColon(false);

		String coreDataSetName = dataSetConfiguration.getName();
		String auditTableName
			= RIFTemporaryTablePrefixes.AUD_CHANGES.getTableName(coreDataSetName);
		PreparedStatement statement = null;		
		try {

			deleteTable(
				connection, 
				logFileWriter, 
				auditTableName);
			
			String includeFieldOnlyQuery
				= getIncludeFieldOnlyChangesQuery(
					dataSetConfiguration);
			
			String includeFieldChangeDescriptionsQuery
				= getIncludeFieldChangeDescriptionsQuery(
					dataSetConfiguration);

			if ((includeFieldOnlyQuery == null) &&
				(includeFieldChangeDescriptionsQuery == null)) {
				//no changes to audit
				return;
			}
			else {
			
				//queryFormatter.addQueryPhrase(0, "CREATE TABLE ");//KLG_SCHEMA
				//queryFormatter.addQueryPhrase(auditTableName);
				//queryFormatter.addQueryPhrase(" AS");
				//queryFormatter.padAndFinishLine();
				// Have to make this change because MS SQL needs different CREATE TABLE syntax which is incompatible with the 
				// way Kevin designed this class. Hopefully the code below will work and won't break with other audit changes
				queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
				queryFormatter.addQueryPhrase(auditTableName);
				queryFormatter.addQueryPhrase(" (data_set_id INTEGER NOT NULL, row_number INTEGER NOT NULL, field_name NVARCHAR(30) NOT NULL, old_value NVARCHAR(30), new_value NVARCHAR(30), time_stamp DATETIME NOT NULL) ");
				queryFormatter.padAndFinishLine();
				//queryFormatter.addQueryPhrase(0, "GO ");
				//queryFormatter.padAndFinishLine();
				// Now some insert into 
				queryFormatter.addQueryPhrase(0, "INSERT INTO ");
				queryFormatter.addQueryPhrase(auditTableName);
				queryFormatter.addQueryPhrase(" (data_set_id, row_number, field_name, old_value, new_value, time_stamp) ");
				
				if ((includeFieldOnlyQuery != null) &&
					(includeFieldChangeDescriptionsQuery != null)) {
			
					queryFormatter.addQueryPhrase(includeFieldOnlyQuery);
					queryFormatter.padAndFinishLine();
					queryFormatter.addQueryPhrase(0, "UNION ALL");
					queryFormatter.padAndFinishLine();				
					queryFormatter.addQueryPhrase(includeFieldChangeDescriptionsQuery);
				
				}
				else if (includeFieldOnlyQuery != null) {
					//only the include field only changes generated results
					queryFormatter.addQueryPhrase(includeFieldOnlyQuery);
				}	
				else {
					//only the include field descriptions changes generated results
					queryFormatter.addQueryPhrase(includeFieldChangeDescriptionsQuery);				
				}
			}
			
			logSQLQuery(
				logFileWriter,
				"change_audit_manager audit changes", 
				queryFormatter);
			System.out.println(queryFormatter.generateQuery());
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			
			statement.executeUpdate();

		}
		catch(SQLException sqlException) {			
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"changeAuditManager.error.unableToAuditFieldOnlyChanges",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}	
		
		
		exportTable(
			connection, 
			logFileWriter, 
			exportDirectoryPath, 
			DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL, 
			auditTableName);

	}
	
	private String getIncludeFieldOnlyChangesQuery(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<DataSetFieldConfiguration> auditableChangeFields
			= DataSetConfigurationUtility.getChangeAuditFields(
				dataSetConfiguration,
				FieldChangeAuditLevel.INCLUDE_FIELD_NAME_ONLY);
		int numberOfAuditableChangeFields 
			= auditableChangeFields.size();

		if (numberOfAuditableChangeFields == 0) {
			return null;
		}


		/**
		 * We should now have a statement such as:
		 * 
		 * SELECT
		 *    data_set_id,
		 *    row_number,
		 *    'age' AS field_name,
		 *    '' AS old_value,
		 *    '' AS new_value,
		 *    current_timestamp AS time_stamp
		 * FROM
		 *    load_test_cleaning1,
		 *    clean_test_cleaning1
		 * WHERE
		 *    load_test_cleaning1.data_set_id = clean_test_cleaning1.data_set_id AND 
		 *    load_test_cleaning1.row_number = clean_test_cleaning1.row_number;
		 * UNION ALL
		 * SELECT
		 *    data_set_id,
		 *    row_number,
		 *    'dob' AS field_name,
		 *    '' AS old_value,
		 *    '' AS new_value,
		 * FROM
		 *    load_test_cleaning1,
		 *    clean_test_cleaning1
		 * WHERE
		 *    load_test_cleaning1.data_set_id = clean_test_cleaning1.data_set_id AND 
		 *    load_test_cleaning1.row_number = clean_test_cleaning1.row_number
		 */	
		
		String coreDataSetName
			= dataSetConfiguration.getName();
		String loadTableName
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(coreDataSetName);
		String cleanSearchReplaceTableName
			= RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE.getTableName(coreDataSetName);
		
		SQLGeneralQueryFormatter queryFormatter 
			= new SQLGeneralQueryFormatter();
		queryFormatter.setEndWithSemiColon(false);
		
		for (int i = 0; i < numberOfAuditableChangeFields; i++) {
			DataSetFieldConfiguration dataSetFieldConfiguration
				= auditableChangeFields.get(i);
		
			if (i != 0) {
				queryFormatter.finishLine();
				queryFormatter.addPaddedQueryLine(0, "UNION ALL");
			}

			String singleChangeFieldQuery
				= createFieldOnlyChanges(
					loadTableName,
					cleanSearchReplaceTableName,
					dataSetFieldConfiguration);

			queryFormatter.addQueryPhrase(0, singleChangeFieldQuery);
		}

		return queryFormatter.generateQuery();
	}
	
	private String getIncludeFieldChangeDescriptionsQuery(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<DataSetFieldConfiguration> auditableChangeFields
			= DataSetConfigurationUtility.getChangeAuditFields(
				dataSetConfiguration, 
				FieldChangeAuditLevel.INCLUDE_FIELD_CHANGE_DESCRIPTION);
		int numberOfAuditableChangeFields 
			= auditableChangeFields.size();
		
		if (numberOfAuditableChangeFields == 0) {
			return null;
		}
		

		/**
		 * We should now have a statement such as:
		 * 
		 * SELECT
		 *    data_set_id,
		 *    row_number,
		 *    'age' AS field_name,
		 *    test_cleaning1.age AS old_value,
		 *    test_cleaning1.age AS new_value,
		 *    current_time_stamp AS time_stamp
		 * FROM
		 *    load_test_cleaning1,
		 *    clean_test_cleaning1
		 * WHERE
		 *    load_test_cleaning1.data_set_id = clean_test_cleaning1.data_set_id AND 
		 *    load_test_cleaning1.row_number = clean_test_cleaning1.row_number AND
		 *    load_test_cleaning1.age != clean_test_cleaning1.age
		 * UNION ALL
		 * SELECT
		 *    data_set_id,
		 *    row_number,
		 *    'dob' AS field_name,
		 *    test_cleaning1.dob AS old_value,
		 *    test_cleaning1.dob AS new_value,
		 * FROM
		 *    load_test_cleaning1,
		 *    clean_test_cleaning1
		 * WHERE
		 *    load_test_cleaning1.data_set_id = clean_test_cleaning1.data_set_id AND 
		 *    load_test_cleaning1.row_number = clean_test_cleaning1.row_number AND
		 *    load_test_cleaning1.dob != clean_test_cleaning1.dob;
		 */		
		
		String coreDataSetName
			= dataSetConfiguration.getName();
		String loadTableName
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(coreDataSetName);
		String cleanSearchReplaceTableName
			= RIFTemporaryTablePrefixes.CLEAN_SEARCH_REPLACE.getTableName(coreDataSetName);
		
		SQLGeneralQueryFormatter queryFormatter 
			= new SQLGeneralQueryFormatter();
		
		for (int i = 0; i < numberOfAuditableChangeFields; i++) {
			DataSetFieldConfiguration dataSetFieldConfiguration
				= auditableChangeFields.get(i);
			if (i != 0) {
				queryFormatter.finishLine();
				queryFormatter.addPaddedQueryLine(0, "UNION ALL");
			}
				
			String singleChangeFieldDescriptionsQuery				
				= createFieldChangeDescriptions(
					loadTableName,
					cleanSearchReplaceTableName,
					dataSetFieldConfiguration);

			queryFormatter.addQueryPhrase(singleChangeFieldDescriptionsQuery);
		}
		queryFormatter.setEndWithSemiColon(false);
		return queryFormatter.generateQuery();
	}
	
	private String createFieldChangeDescriptions(
		final String loadTableName,
		final String cleanSearchReplaceTableName,
		final DataSetFieldConfiguration auditableField) {
		
		
		String loadFieldName
			= auditableField.getLoadFieldName();
		String cleanFieldName
			= auditableField.getCleanFieldName();
		
		SelectQueryFormatter queryFormatter
			= new MSSQLSelectQueryFormatter();
		//KLG_SCHEMA
		//queryFormatter.setDatabaseSchemaName("dbo");
		queryFormatter.setEndWithSemiColon(false);

		queryFormatter.addSelectField(cleanSearchReplaceTableName, "data_set_id");
		queryFormatter.addSelectField(cleanSearchReplaceTableName, "row_number");
		queryFormatter.addTextLiteralSelectField(
			cleanFieldName, 
			"field_name");
		queryFormatter.addSelectFieldWithAlias(
			loadTableName, 
			loadFieldName, 
			"old_value");
		queryFormatter.addSelectFieldWithAlias(
				cleanSearchReplaceTableName, 
			cleanFieldName, 
			"new_value");
		queryFormatter.addSelectFieldWithAlias("current_timestamp", "time_stamp");
		
		queryFormatter.addFromTable(loadTableName);
		queryFormatter.addFromTable(cleanSearchReplaceTableName);
		queryFormatter.addWhereJoinCondition(
			loadTableName, 
			"data_set_id", 
			cleanSearchReplaceTableName, 
			"data_set_id");
		queryFormatter.addWhereJoinCondition(
				loadTableName, 
				"row_number", 
				cleanSearchReplaceTableName, 
				"row_number");		
		queryFormatter.addWhereParameterWithOperator(
			loadTableName, 
			loadFieldName, 
			"!=",
			cleanSearchReplaceTableName, 
			cleanFieldName);

		return queryFormatter.generateQuery();
	}
		
	private String createFieldOnlyChanges(
		final String loadTableName,
		final String finalCleanedTableName,
		final DataSetFieldConfiguration auditableField) {
				
		String loadFieldName
			= auditableField.getLoadFieldName();
		String cleanFieldName
			= auditableField.getCleanFieldName();
		
		SelectQueryFormatter queryFormatter
			= new MSSQLSelectQueryFormatter();
		//KLG_SCHEMA
		//queryFormatter.setDatabaseSchemaName("dbo");
		queryFormatter.setEndWithSemiColon(false);

		queryFormatter.addSelectField(finalCleanedTableName, "data_set_id");
		queryFormatter.addSelectField(finalCleanedTableName, "row_number");
		
		queryFormatter.addTextLiteralSelectField(
			cleanFieldName, 
			"field_name");

		queryFormatter.addTextLiteralSelectField(
			"", 
			"old_value");
		queryFormatter.addTextLiteralSelectField(
			"", 
			"new_value");
		queryFormatter.addSelectFieldWithAlias("current_timestamp", "time_stamp");
		
		queryFormatter.addFromTable(loadTableName);
		queryFormatter.addFromTable(finalCleanedTableName);

		queryFormatter.addWhereJoinCondition(
			loadTableName, 
			"data_set_id", 
			finalCleanedTableName, 
			"data_set_id");
		
		queryFormatter.addWhereJoinCondition(
			loadTableName, 
			"row_number", 
			finalCleanedTableName, 
			"row_number");
		
		queryFormatter.addWhereParameterWithOperator(
			loadTableName, 
			loadFieldName, 
			"!=", 
			finalCleanedTableName, 
			cleanFieldName);
		
		return queryFormatter.generateQuery();
	}
	
	public int getDataSetIdentifier(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		int result = 0;
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		try {
			SelectQueryFormatter queryFormatter
				= new MSSQLSelectQueryFormatter();
			queryFormatter.addSelectField("id");
			queryFormatter.addFromTable("data_set_configurations");
			queryFormatter.addWhereParameter("core_data_set_name");
			queryFormatter.addWhereParameter("version");
			
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.setString(1, dataSetConfiguration.getName());
			statement.setString(2, dataSetConfiguration.getVersion());
			resultSet = statement.executeQuery();
			if (resultSet.next() == false) {
				//ERROR: non-existent data set specified
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage("");
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFGenericLibraryError.DATABASE_QUERY_FAILED,
						errorMessage);
				throw rifServiceException;
			}

			result = resultSet.getInt(1);
			return result;
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(resultSet);
			SQLQueryUtility.close(statement);
		}
	}
	
	public int addDataSetConfiguration(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {

		if (dataSetConfigurationExists(
			connection, 
			logFileWriter,
			dataSetConfiguration)) {
			//trying to add a data set configuration that already exists
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.dataSetConfigurationAlreadyExists",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}

		int result = 0;

		PreparedStatement getIdentifierStatement = null;		
		PreparedStatement addDataSetStatement = null;
		ResultSet resultSet = null;
		MSSQLInsertQueryFormatter addDataSetQueryFormatter
			= new MSSQLInsertQueryFormatter();
		
		SQLGeneralQueryFormatter getIdentifierQueryFormatter
			= new SQLGeneralQueryFormatter();
		
		try {
			//KLG_SCHEMA
			//addDataSetQueryFormatter.setDatabaseSchemaName("dbo");
			addDataSetQueryFormatter.setIntoTable("data_set_configurations");
			addDataSetQueryFormatter.addInsertField("core_data_set_name");
			addDataSetQueryFormatter.addInsertField("version");
			
			addDataSetStatement
				= createPreparedStatement(
					connection, 
					addDataSetQueryFormatter);
			addDataSetStatement.setString(
				1, 
				dataSetConfiguration.getName());		
			addDataSetStatement.setString(
				2, 
				dataSetConfiguration.getVersion());

			/*
			 * #POSSIBLE_PORTING_ISSUE
			 * The use of CURRVAL to create sequences may be something
			 * PostgreSQL does that SQL Server does not?
			 */
			addDataSetStatement.executeUpdate();
			getIdentifierQueryFormatter.addQueryPhrase(0, "SELECT CURRVAL('data_set_sequence');");
			getIdentifierStatement
				= createPreparedStatement(
					connection, 
					getIdentifierQueryFormatter);
			resultSet
				= getIdentifierStatement.executeQuery();
			resultSet.next();
			result = resultSet.getInt(1);
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.unableToAddDataSetConfiguration",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(addDataSetStatement);
			SQLQueryUtility.close(getIdentifierStatement);
			SQLQueryUtility.close(resultSet);
		}
		
		return result;
	}
		
	public void deleteDataSetConfiguration(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
	
		DeleteRowsQueryFormatter deleteDataSetStatementQueryFormatter
			= new DeleteRowsQueryFormatter();
		//KLG_SCHEMA
		//deleteDataSetStatementQueryFormatter.setDatabaseSchemaName("dbo");
		deleteDataSetStatementQueryFormatter.setFromTable("data_set_configurations");
		deleteDataSetStatementQueryFormatter.addWhereParameter("core_data_set_name");
		deleteDataSetStatementQueryFormatter.addWhereParameter("version");
		
		PreparedStatement deleteDataSetConfigurationStatement = null;
		try {			
			deleteDataSetConfigurationStatement
				= createPreparedStatement(
					connection,
					deleteDataSetStatementQueryFormatter);
			deleteDataSetConfigurationStatement.setString(
				1, 
				dataSetConfiguration.getName());
			deleteDataSetConfigurationStatement.setString(
				2, 
				dataSetConfiguration.getVersion());
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.unableToDeleteDataSetConfiguration",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(deleteDataSetConfigurationStatement);
		}
		
	}
		
	private boolean dataSetConfigurationExists(
		final Connection connection,
		final Writer logFileWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
				
		PreparedStatement statement = null;
		RecordExistsQueryFormatter queryFormatter
			= new MSSQLRecordExistsQueryFormatter();
		//KLG_SCHEMA
		//queryFormatter.setDatabaseSchemaName("dbo");
		queryFormatter.setFromTable("data_set_configurations");
		queryFormatter.setLookupKeyFieldName("core_data_set_name");
		queryFormatter.addWhereParameter("version");
				
		boolean result = false;
		ResultSet resultSet = null;
		try {
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.setString(1, dataSetConfiguration.getName());
			statement.setString(2, dataSetConfiguration.getVersion());
			
			resultSet = statement.executeQuery();
			result = resultSet.next();
			return result;
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetManager.error.unableToCheckDataSetConfigurationExists",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}

	}
		
	/*
	 * Assume this table is created in RIF scripts?
	 */
	public void clearAllDataSets(
		final Connection connection,
		final Writer logFileWriter) 
		throws RIFServiceException {

		//Create SQL query
		DeleteRowsQueryFormatter queryFormatter
			= new DeleteRowsQueryFormatter();
		//KLG_SCHEMA
		//queryFormatter.setDatabaseSchemaName("dbo");
		queryFormatter.setFromTable("data_set_configurations");

		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("dataSetManager.error.unableToCleardataSets");
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw RIFServiceException;
		}
		finally {
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


