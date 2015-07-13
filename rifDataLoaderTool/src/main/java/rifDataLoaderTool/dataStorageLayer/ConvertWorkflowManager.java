package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.*;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;

import java.util.ArrayList;
import java.sql.*;
import java.text.Collator;

/**
 *
 * Manages all database operations used to convert a cleaned table into tabular data
 * expected by some part of the RIF (eg: numerator data, health codes, geospatial data etc)
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

public final class ConvertWorkflowManager 
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

	public ConvertWorkflowManager(
		final RIFDatabaseProperties rifDatabaseProperties) {

		super(rifDatabaseProperties);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void convertConfiguration(
		final Connection connection,
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
	
			//Create the first part of the query used to create a converted table
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
			queryFormatter.addQueryPhrase(convertedTableName);
			queryFormatter.addQueryPhrase(" AS");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(1, "SELECT");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryLine(2, "data_set_id,");
			queryFormatter.addQueryLine(2, "row_number,");
			
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
			queryFormatter.addQueryPhrase(0, "FROM");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(1, cleanedTableName);
			logSQLQuery("convert_configuration", queryFormatter);
						
			statement	
				= createPreparedStatement(
					connection, 
					queryFormatter);	
			statement.executeUpdate();
			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
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
			= dataSetConfiguration.getFieldsWithoutConversionFunctions();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldRequirementLevel fieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
				addConvertQueryFragment(
					queryFormatter,
					indentationLevel,
					fieldConfiguration);
			}
			else if (fieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD) {
				queryFormatter.addQueryPhrase(",");
				queryFormatter.finishLine();			
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
			= dataSetConfiguration.getFieldsWithConversionFunctions();
		for (DataSetFieldConfiguration fieldWithConversion : fieldsWithConversions) {
			RIFConversionFunction rifConversionFunction
				= fieldWithConversion.getConvertFunction();
			if (rifConversionFunction.supportsOneToOneConversion()) {
				rifConversionFunction.addActualParameter(fieldWithConversion);
				String queryFragment 
					= rifConversionFunction.generateQueryFragment();
				queryFormatter.finishLine();
				queryFormatter.addQueryPhrase(",");
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
			(dataSetConfiguration.getRIFSchemaArea() == RIFSchemaArea.HEALTH_NUMERATOR_DATA)) {
			
			DataSetFieldConfiguration ageFieldConfiguration
				= dataSetConfiguration.getFieldHavingConvertFieldName("age");
			DataSetFieldConfiguration sexFieldConfiguration
				= dataSetConfiguration.getFieldHavingConvertFieldName("sex");
			RIFConversionFunction rifConversionFunction
				= ageFieldConfiguration.getConvertFunction();

			rifConversionFunction.addActualParameter(ageFieldConfiguration);
			rifConversionFunction.addActualParameter(sexFieldConfiguration);
			
			queryFormatter.addQueryPhrase(",");
			queryFormatter.finishLine();
			queryFormatter.addQueryPhrase(
				indentationLevel, 
				rifConversionFunction.generateQueryFragment());				
		}
		else {
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
		Collator collator = RIFDataLoaderToolMessages.getCollator();
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
	/*
	private String concatenateFunctionParameters(
		final ArrayList<CleanWorkflowFieldConfiguration> fieldConfigurations) {
		
		StringBuilder buffer = new StringBuilder();

		int numberOfFieldConfigurations
			= fieldConfigurations.size();

		if (numberOfFieldConfigurations == 0) {
			buffer.append(fieldConfigurations.get(0).getCleanedTableFieldName());
		}
		else {
			for (int i = 0; i < numberOfFieldConfigurations; i++) {
				if (i != 0) {
					buffer.append(",");
				}
				buffer.append(fieldConfigurations.get(i).getCleanedTableFieldName());
			}
		}
		
		return buffer.toString();
	}	
	*/
	
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


