package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.*;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifServices.dataStorageLayer.SQLQueryUtility;
import rifServices.system.RIFServiceException;

import java.util.ArrayList;
import java.sql.*;

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
		final RIFDataLoaderStartupOptions startupOptions) {

		super(startupOptions);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void convertConfiguration(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {

		
		RIFConversionFunctionFactory conversionFunctionFactory
			= RIFConversionFunctionFactory.newInstance();
		
		/**
		 * CREATE TABLE convert_my_table2001 AS 
		 * SELECT
		 *    data_source_id,
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
		
		PreparedStatement statement = null;
		String coreDataSetName 
			= dataSetConfiguration.getName();
		try {			
			String cleanedTableName
				= RIFTemporaryTablePrefixes.CLEAN_CASTING.getTableName(coreDataSetName);
			String convertedTableName
				= RIFTemporaryTablePrefixes.CONVERT.getTableName(coreDataSetName);
		
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		
			queryFormatter.addQueryPhrase(0, "CREATE TABLE ");
			queryFormatter.addQueryPhrase(convertedTableName);
			queryFormatter.addQueryPhrase(" AS");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(1, "SELECT");
			queryFormatter.padAndFinishLine();
		
			queryFormatter.addQueryLine(2, "data_source_id,");
			queryFormatter.addQueryLine(2, "row_number,");
				
			ArrayList<DataSetFieldConfiguration> fieldConfigurations
				= dataSetConfiguration.getFieldConfigurations();
			for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
				FieldRequirementLevel fieldRequirementLevel
					= fieldConfiguration.getFieldRequirementLevel();
				if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
					addConvertQueryFragment(
						queryFormatter,
						conversionFunctionFactory,
						2,
						fieldConfiguration);
				}
			}
			
			//Now add on the extra fields
			for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
				FieldRequirementLevel fieldRequirementLevel
					= fieldConfiguration.getFieldRequirementLevel();
				if (fieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD) {
					queryFormatter.addQueryPhrase(",");
					queryFormatter.finishLine();			
					queryFormatter.addQueryPhrase(
						fieldConfiguration.getCleanFieldName());
				}
			}
			
			queryFormatter.addQueryPhrase(0, "FROM");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryPhrase(1, cleanedTableName);

			statement	
				= createPreparedStatement(
					connection, 
					queryFormatter);
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"convertWorkflowManager.error.unableToCreateConvertTable",
					coreDataSetName);
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
	
	private void addConvertQueryFragment(
		final SQLGeneralQueryFormatter queryFormatter,
		final RIFConversionFunctionFactory conversionFunctionFactory,
		final int baseIndentationLevel,
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
	
		/**
		 * cleanedTableName.postal_code AS postal_code,
		 * convert_age_sex(age, sex)
		 * 
		 */
		
		/*
		 * 
		 * KLG: @TODO FIX LATER
		 */
		
		/*
		String conversionFunctionName
			= dataSetFieldConfiguration.getConversionFunctionName();		
		RIFConversionFunction rifConversionFunction
			= conversionFunctionFactory.getRIFConvertFunction(conversionFunctionName);
		
		if (conversionFunctionName == null) {
			//there is no function
			queryFormatter.addQueryPhrase(
				baseIndentationLevel,
				fieldConfigurations.get(0).getCleanedTableFieldName());
			queryFormatter.addQueryPhrase(" AS ");
			queryFormatter.addQueryPhrase(
					dataSetFieldConfiguration.getConvertFieldName());
		}
		else {
			//there is a function
			queryFormatter.addQueryPhrase(
				baseIndentationLevel, 
				conversionFunctionName);
			queryFormatter.addQueryPhrase("(");
			queryFormatter.addQueryPhrase(
				concatenateFunctionParameters(
					fieldConfigurations));				
			queryFormatter.addQueryPhrase(") AS ");
			queryFormatter.addQueryPhrase(
				dataSetFieldConfiguration.getConvertFieldName());
			queryFormatter.padAndFinishLine();
		}
		
		*/
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


