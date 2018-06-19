package org.sahsu.rif.dataloader.datastorage.ms;

import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.ConversionFunction;
import org.sahsu.rif.dataloader.concepts.ConversionFunctionFactory;
import org.sahsu.rif.dataloader.concepts.DataLoadingResultTheme;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldRequirementLevel;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.concepts.RIFSchemaAreaPropertyManager;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.dataloader.concepts.WorkflowValidator;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 * This class manages the code used to map fields from an imported table to
 * fields that are expected for specific parts of the RIF schema.  Conversion
 * can either involve something as simple as renaming a field name so it has
 * an expected value or calling a conversion function to massage the values of
 * one or more field values into an expected value.  The best example of this is
 * <code>convert_age_sex(birth_date, event_date, sex) AS age_sex_group</code>
 * operation, which will take multiple field values from the source data table 
 * and create a single age_sex_group column that the RIF expects to appear in
 * its <code>numerator</code> and <code>denominator</code> tables.
 * 
 * <p>
 * With respect to possible porting issues, functions like 
 * <code>convert_age_sex</code> will likely have to be rewritten for both 
 * PostgreSQL and SQL Server.
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

final public class MSSQLConvertWorkflowManager 
	extends AbstractMSSQLDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================


	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLConvertWorkflowManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void convertConfiguration(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {

		/**
		 * CREATE TABLE convert_my_table2001 AS 
		 * SELECT
		 *    data_set_id,
		 *    row_number,
		 *    pst AS postal_code,
		 *    convert_age_sex(birth_date, event_date, sex) AS age_sex_group,
		 *    year AS year,
		 *    other_field1,
		 *    other_field2
		 * FROM
		 *    cln_cast_my_table2001 AS cleanedTableName;
		 * 
		 * 
		 */
		RIFSchemaAreaPropertyManager schemaAreaPropertyManager
			= new RIFSchemaAreaPropertyManager();
		WorkflowValidator workflowValidator
			= new WorkflowValidator(schemaAreaPropertyManager);
		workflowValidator.validateConvert(dataSetConfiguration);
		
		PreparedStatement statement = null;
		try {
			String coreDataSetName = dataSetConfiguration.getName();
			String cleanedTableName
				= RIFTemporaryTablePrefixes.CLEAN_FINAL.getTableName(coreDataSetName);
			String convertedTableName
				= RIFTemporaryTablePrefixes.CONVERT.getTableName(coreDataSetName);
			deleteTable(
				connection, 
				logFileWriter,
				convertedTableName);
			
			//Create the first part of the query used to create a converted table
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "SELECT");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryLine(1, "data_set_id,");
			queryFormatter.addQueryPhrase(1, "row_number");

			processFieldsWithoutConversions(
				queryFormatter,
				2,
				dataSetConfiguration);
			
			//This is where we may have to map multiple fields from a cleaned table
			//to a different number of fields in the convert table
			//eg: columns age, sex in cleaned table mapping to age_sex_group in
			//converted table.
			processFieldsWithConversions(
				queryFormatter,
				2,
				dataSetConfiguration);		

			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(3, "INTO ");//KLG_SCHEMA
			queryFormatter.addQueryPhrase(convertedTableName);
			queryFormatter.padAndFinishLine();

			queryFormatter.addQueryPhrase(0, "FROM ");
			queryFormatter.addQueryPhrase(cleanedTableName);
			logSQLQuery(
				logFileWriter,
				"convert_configuration", 
				queryFormatter);
			
			statement	
				= createPreparedStatement(
					connection, 
					queryFormatter);	
			statement.executeUpdate();
					
			exportTable(
					connection,
					logFileWriter,
					exportDirectoryPath,
					DataLoadingResultTheme.ARCHIVE_STAGES,
					convertedTableName);
			
			updateLastCompletedWorkState(
					connection,
					logFileWriter,
					dataSetConfiguration,
					WorkflowState.CONVERT);
			
			
		}
		catch(SQLException sqlException) {
			logSQLException(
				logFileWriter,
				sqlException);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"convertWorkflowManager.error.unableToCreateConvertTable",
					dataSetConfiguration.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}		
	}

	private void processFieldsWithoutConversions(
		final SQLGeneralQueryFormatter queryFormatter,
		final int indentationLevel,
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= DataSetConfigurationUtility.getFieldsWithoutConversionFunctions(
				dataSetConfiguration);
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			queryFormatter.addQueryPhrase(",");
			queryFormatter.finishLine();			
			FieldRequirementLevel fieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
				addConvertQueryFragment(
					queryFormatter,
					indentationLevel,
					fieldConfiguration);
			}
			else if (fieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD) {
				queryFormatter.addQueryPhrase(
					indentationLevel,
					fieldConfiguration.getCleanFieldName());				
			}
		}
	}

	/**
	 * For numerator and denominator tables, we need to be careful about how
	 * to assemble the queries.  If the tables happen to have a field
	 * called 'age_sex_group', then we just promote the field in the converted
	 * table.  However, if it has separate fields for age and sex, they need
	 * to be combined.
	 */
	private void processFieldsWithConversions(
		final SQLGeneralQueryFormatter queryFormatter,
		final int indentationLevel,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
			
		ArrayList<DataSetFieldConfiguration> fieldsWithConversions
			= DataSetConfigurationUtility.getFieldsWithConversionFunctions(dataSetConfiguration);
		for (DataSetFieldConfiguration fieldWithConversion : fieldsWithConversions) {
			ConversionFunction rifConversionFunction
				= fieldWithConversion.getConvertFunction();
			if (rifConversionFunction.supportsOneToOneConversion()) {
				rifConversionFunction.addActualParameter(fieldWithConversion);
				String queryFragment 
					= rifConversionFunction.generateQueryFragment();
				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();
				queryFormatter.addQueryPhrase(
					indentationLevel, 
					queryFragment);				
			}
		}
		
		/*
		 * KLG: We may need a more intelligent way of figuring out
		 * how to assemble them together.  For now, we're mainly concerned
		 * with mapping age and sex to a single column.  If the original
		 * data set happened to have a column called age_sex_group, then
		 * it would not be associated with any conversion function.
		 * 
		 */
	
		if ((dataSetConfiguration.getRIFSchemaArea() == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
		    (dataSetConfiguration.getRIFSchemaArea() == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
			
			DataSetFieldConfiguration ageFieldConfiguration
				= dataSetConfiguration.getFieldHavingConvertFieldName("age");
			DataSetFieldConfiguration sexFieldConfiguration
				= dataSetConfiguration.getFieldHavingConvertFieldName("sex");
			
			if ((ageFieldConfiguration != null) &&
				(sexFieldConfiguration != null)) {
				
				ConversionFunctionFactory rifConversionFunctionFactory
					= ConversionFunctionFactory.newInstance();

				/*
				 * #POSSIBLE_PORTING_ISSUE
				 * These conversion functions will have to be rewritten for both
				 * SQL Server and PostgreSQL
				 */				
				ConversionFunction ageSexConversionFunction
					= rifConversionFunctionFactory.getRIFConvertFunction("[dbo].[convert_age_sex]");
				ageSexConversionFunction.addActualParameter(ageFieldConfiguration);
				ageSexConversionFunction.addActualParameter(sexFieldConfiguration);
				
				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();
				queryFormatter.addQueryPhrase(
					indentationLevel, 
					ageSexConversionFunction.generateQueryFragment());				
			}
		}
		else if (fieldsWithConversions.size() > 0)  {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"convertWorkflowManager.error.unknownConversionActivity");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.UNKNOWN_CONVERSION_ACTIVITY, 
					errorMessage);
			throw rifServiceException;
		}
		
	}
	
	private void addConvertQueryFragment(
		final SQLGeneralQueryFormatter queryFormatter,
		final int baseIndentationLevel,
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
	
		/**
		 * cleanedTableName.postal_code AS postal_code,
		 * convert_age_sex(age, sex)
		 * 
		 */
		//there is no function
		Collator collator = GENERIC_MESSAGES.getCollator();
		String cleanFieldName = dataSetFieldConfiguration.getCleanFieldName();
		String convertFieldName = dataSetFieldConfiguration.getConvertFieldName();
		
		if (collator.equals(cleanFieldName, convertFieldName)) {
			queryFormatter.addQueryPhrase(
				baseIndentationLevel, 
				convertFieldName);
		}
		else {			
			queryFormatter.addQueryPhrase(
				baseIndentationLevel,
				dataSetFieldConfiguration.getCleanFieldName());
			queryFormatter.addQueryPhrase(" AS ");
			queryFormatter.addQueryPhrase(
				dataSetFieldConfiguration.getConvertFieldName());
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


