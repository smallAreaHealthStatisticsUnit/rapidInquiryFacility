package org.sahsu.rif.dataloader.scriptgenerator.pg;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.pg.PGSQLCreateTableQueryFormatter;

/**
 *
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

public class PGCovariateScriptGenerator 
	extends PGAbstractDataLoadingScriptGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public PGCovariateScriptGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateScript(
		final DataSetConfiguration covariate) {

		//Make a new table in the database for the covariate file
		StringBuilder covariateEntry = new StringBuilder();			
		createTableStructureAndImportCSV(
			covariateEntry, 
			covariate);
		
		createPrimarykey(
			covariateEntry,
			covariate);	

		addSchemaComments(
			covariateEntry,
			covariate);
			
		createPermissions(
			covariateEntry, 
			covariate);
		
		//Ensure covariate geolevel is known in t_rif50_geolevels
		updateRIF40GeoLevels(
			covariateEntry,
			covariate);
		covariateEntry.append("\n\n");
		
		//Register covariates in admin tables
		addEntryToRIF40CovariatesTable(
			covariateEntry, 
			covariate);	
		return covariateEntry.toString();
	}
	
	private void createTableStructureAndImportCSV(
		final StringBuilder covariateEntry,
		final DataSetConfiguration covariate) {

		//The name the table will have in the schema 'pop'
		String publishedCovariateTableName
			= covariate.getPublishedTableName().toUpperCase();		
		
		//Identify the fields of interest
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetConfigurationUtility.getRequiredYearField(covariate);
		DataSetFieldConfiguration resolutionFieldConfiguration
			= DataSetConfigurationUtility.getRequiredGeographicalResolutionField(covariate);
		ArrayList<DataSetFieldConfiguration> covariateFields
			= DataSetConfigurationUtility.getCovariateFields(covariate);
		
		//Create an empty table that will be the target of the import
		PGSQLCreateTableQueryFormatter createTableQueryFormatter
			= new PGSQLCreateTableQueryFormatter();
		createTableQueryFormatter.setDatabaseSchemaName("rif_data");
		createTableQueryFormatter.setTableName(publishedCovariateTableName);
		createTableQueryFormatter.addIntegerFieldDeclaration(
			yearFieldConfiguration.getConvertFieldName(), 
			false);
		createTableQueryFormatter.addTextFieldDeclaration(
			resolutionFieldConfiguration.getConvertFieldName(), 
			20, 
			false);

		for (DataSetFieldConfiguration covariateField : covariateFields) {
			
			RIFDataType rifDataType = covariateField.getRIFDataType();
			if (rifDataType == RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE) {
				createTableQueryFormatter.addIntegerFieldDeclaration(
					covariateField.getConvertFieldName(), 
					true);
			}
			else if (rifDataType == RIFDataTypeFactory.RIF_DOUBLE_DATA_TYPE) {
				createTableQueryFormatter.addDoubleFieldDeclaration(
					covariateField.getConvertFieldName(), 
					true);				
			}
			else {
				assert false;
			}
		}
		covariateEntry.append(createTableQueryFormatter.generateQuery());
		covariateEntry.append("\n");

		ArrayList<String> fieldNames = new ArrayList<String>();
		fieldNames.add(yearFieldConfiguration.getConvertFieldName());
		fieldNames.add(resolutionFieldConfiguration.getConvertFieldName());
		for (DataSetFieldConfiguration covariateField : covariateFields) {
			fieldNames.add(covariateField.getConvertFieldName());
		}					
		String filePath
			= super.getPublishedFilePath(covariate) + ".csv";
		
		String bulkInsertStatement
			= createBulkCopyStatement(
				publishedCovariateTableName,
				fieldNames,
				filePath);

		covariateEntry.append(bulkInsertStatement);
	}

	private void addSchemaComments(
		final StringBuilder covariateEntry,
		final DataSetConfiguration covariate) {

		String publishedCovariateTableName
			= covariate.getPublishedTableName().toUpperCase();		

		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetConfigurationUtility.getRequiredYearField(covariate);
		//define the geographical resolution field
		DataSetFieldConfiguration resolutionFieldConfiguration
			= DataSetConfigurationUtility.getRequiredGeographicalResolutionField(covariate);		
				
		//Add comments to table
		covariateEntry.append(
			createTableCommentQuery(
				publishedCovariateTableName, 
				covariate.getDescription()));
		covariateEntry.append(
			createTableFieldCommentQuery(
				publishedCovariateTableName, 
				yearFieldConfiguration.getCleanFieldName(), 
				yearFieldConfiguration.getDescription()));				
		covariateEntry.append(
			createTableFieldCommentQuery(
				publishedCovariateTableName, 
				resolutionFieldConfiguration.getCleanFieldName(), 
				resolutionFieldConfiguration.getDescription()));
		
		ArrayList<DataSetFieldConfiguration> covariateFields
			= DataSetConfigurationUtility.getCovariateFields(covariate);
		for (DataSetFieldConfiguration covariateField : covariateFields) {
			covariateEntry.append(
					createTableFieldCommentQuery(
						publishedCovariateTableName, 
						covariateField.getCleanFieldName(), 
						covariateField.getDescription()));
		}
		
		
		
	}

	
	private void updateRIF40GeoLevels(
		final StringBuilder covariateEntry,
		final DataSetConfiguration covariate) {
		
		//UPDATE t_rif40_geolevels
		//SET covariate_table=[published covariate table name]
		//WHERE geography=[geography] AND geolevel_name=[resolutionField.getName()
		
		Geography geography = covariate.getGeography();
		DataSetFieldConfiguration resolutionFieldLevel
			= DataSetConfigurationUtility.getRequiredGeographicalResolutionField(covariate);
		String publishedTableName
			= covariate.getPublishedTableName();
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "UPDATE t_rif40_geolevels ");
		queryFormatter.addQueryPhrase("SET covariate_table = '");
		queryFormatter.addQueryPhrase(publishedTableName.toUpperCase());
		queryFormatter.addQueryPhrase("' WHERE geography='");
		queryFormatter.addQueryPhrase(geography.getName().toUpperCase());
		queryFormatter.addQueryPhrase("' AND geolevel_name='");
		queryFormatter.addQueryPhrase(resolutionFieldLevel.getConvertFieldName().toUpperCase());
		queryFormatter.addQueryPhrase("'");		
		covariateEntry.append(queryFormatter.generateQuery());
	}
	
	private void addEntryToRIF40CovariatesTable(
		final StringBuilder covariateEntry,
		final DataSetConfiguration covariateConfiguration) {
	
		String covariateTableName
			= covariateConfiguration.getPublishedTableName().toUpperCase();
		
		Geography geography = covariateConfiguration.getGeography();
		String geographyName = geography.getName().toUpperCase();
		
		DataSetFieldConfiguration requiredGeographicalResolutionField
			= DataSetConfigurationUtility.getRequiredGeographicalResolutionField(
				covariateConfiguration);
		String geographicalResolutionName
			= requiredGeographicalResolutionField.getCleanFieldName().toUpperCase();
		
		ArrayList<DataSetFieldConfiguration> covariateFieldConfigurations
			= DataSetConfigurationUtility.getCovariateFields(covariateConfiguration);
		for (DataSetFieldConfiguration covariateFieldConfiguration : covariateFieldConfigurations) {
			addCovariateField(
				covariateEntry,
				covariateTableName,
				geographyName,
				geographicalResolutionName,
				covariateFieldConfiguration);
		}
	}

	private void addCovariateField(
		final StringBuilder covariateEntry,
		final String covariateTableName,
		final String geographyName,
		final String geographicalResolutionName,
		final DataSetFieldConfiguration covariateFieldConfiguration) {
		
		String covariateFieldName
			= covariateFieldConfiguration.getCleanFieldName().toUpperCase();		
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "INSERT INTO rif40.rif40_covariates (");
		queryFormatter.addQueryLine(1, "geography,");
		queryFormatter.addQueryLine(1, "geolevel_name,");
		queryFormatter.addQueryLine(1, "covariate_name,");
		queryFormatter.addQueryLine(1, "min,");
		queryFormatter.addQueryLine(1, "max,");
		queryFormatter.addQueryLine(1, "type) ");
		queryFormatter.addQueryLine(0, "SELECT ");
		queryFormatter.addQueryLine(1, "'" + geographyName + "',");
		queryFormatter.addQueryLine(1, "'" + geographicalResolutionName + "',");
		queryFormatter.addQueryLine(1, "'" + covariateFieldName + "',");
		queryFormatter.addQueryLine(1, "MIN(" + covariateFieldName + "),");
		queryFormatter.addQueryLine(1, "MAX(" + covariateFieldName + "),");

		RIFDataType rifDataType
			= covariateFieldConfiguration.getRIFDataType();
		if (rifDataType == RIFDataTypeFactory.RIF_DOUBLE_DATA_TYPE) {
			//Assume it's a continuous variable, type '2'
			queryFormatter.addQueryLine(1, "2");			
		}
		else {
			//otherwise let's assume it's a type '1'
			queryFormatter.addQueryLine(1, "1");
		}
		queryFormatter.addQueryLine(0, "FROM ");
		queryFormatter.addQueryPhrase(1, covariateTableName);
		String query
			= queryFormatter.generateQuery();
		covariateEntry.append(query);	
	}

	protected void createIndices(
		final StringBuilder covariateEntry,
		final DataSetConfiguration covariate) {

		String publishedTableName
			= covariate.getPublishedTableName().toLowerCase();
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetConfigurationUtility.getRequiredYearField(covariate);
		String yearFieldName
			= yearFieldConfiguration.getConvertFieldName().toUpperCase();
		//define the geographical resolution field
		DataSetFieldConfiguration resolutionFieldConfiguration
			= DataSetConfigurationUtility.getRequiredGeographicalResolutionField(covariate);
		String resolutionFieldName
			= resolutionFieldConfiguration.getConvertFieldName().toUpperCase();
		
		String indexName = publishedTableName + "_pk";
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryPhrase(0, "CREATE UNIQUE INDEX ");
		queryFormatter.addQueryPhrase(indexName);
		queryFormatter.addQueryPhrase(" ON rif_data.");
		queryFormatter.addQueryPhrase(publishedTableName);		
		queryFormatter.addQueryPhrase("(");
		queryFormatter.addQueryPhrase(yearFieldName);
		queryFormatter.addQueryPhrase(",");
		queryFormatter.addQueryPhrase(resolutionFieldName);
		queryFormatter.addQueryPhrase(")");
		
		covariateEntry.append(queryFormatter.generateQuery());
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


