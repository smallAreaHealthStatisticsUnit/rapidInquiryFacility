package rifDataLoaderTool.targetDBScriptGenerator.pg;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.dataStorageLayer.*;
import rifGenericLibrary.dataStorageLayer.pg.*;

import java.util.ArrayList;

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

public class PGNumeratorScriptGenerator 
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

	public PGNumeratorScriptGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateScript(
		final DLGeographyMetaData geographyMetaData,
		final DataSetConfiguration numerator) {

		StringBuilder numeratorEntry = new StringBuilder();
		
		createTableStructureAndImportCSV(
			numeratorEntry, 
			geographyMetaData, 
			numerator);
		addSchemaComments(
			numeratorEntry, 
			numerator);
		createIndices(
			numeratorEntry, 
			numerator);
		
		numeratorEntry.append("\n");
		addEntryToRIF40Tables(
				numeratorEntry, 
			numerator);	
		numeratorEntry.append("\n");
		//addEntryToNumDenomTable(
		//	numeratorEntry,
		//	numerator);
		addRIF40TableOutcomesEntry(
			numeratorEntry,
			numerator);

		return numeratorEntry.toString();
	}
	
	private void createTableStructureAndImportCSV(
		final StringBuilder denominatorEntry,
		final DLGeographyMetaData geographyMetaData,
		final DataSetConfiguration denominator) {
		
		//Part I: Make a create table statement 
		PGSQLCreateTableQueryFormatter createTableQueryFormatter
			= new PGSQLCreateTableQueryFormatter();
		
		//The name the table will have in the schema 'pop'
		String publishedDenominatorTableName
			= denominator.getPublishedTableName();		
		//Field properties that will help us construct the 
		//create and copy into statements
		createTableQueryFormatter.setDatabaseSchemaName("rif_data");
		createTableQueryFormatter.setTableName(publishedDenominatorTableName);
		createTableQueryFormatter.addIntegerFieldDeclaration("year", false);
		createTableQueryFormatter.addIntegerFieldDeclaration("age_sex_group", false);

		//Do we assume these field names will be in increasing order of 
		//geographical resolution?
		DLGeography geography = denominator.getGeography();
		ArrayList<String> levelNames = geography.getLevelNames();
		for (String levelName : levelNames) {
			createTableQueryFormatter.addTextFieldDeclaration(levelName, false);
		}
		createTableQueryFormatter.addTextFieldDeclaration("icd", false);
		createTableQueryFormatter.addIntegerFieldDeclaration("total", false);

		denominatorEntry.append(createTableQueryFormatter.generateQuery());
		denominatorEntry.append("\n");
		
		//How do we handle extra fields?
		ArrayList<String> fieldNames = new ArrayList<String>();
		fieldNames.add("year");
		fieldNames.add("age_sex_group");
		for (String levelName : levelNames) {
			fieldNames.add(levelName);
		}
		fieldNames.add("icd");
		fieldNames.add("total");
			
		String filePath
			= super.getPublishedFilePath(denominator) + ".csv";
		
		String bulkInsertStatement
			= createBulkCopyStatement(
				publishedDenominatorTableName,
				fieldNames,
				filePath);

		denominatorEntry.append(bulkInsertStatement);
		
		/*
		SQLGeneralQueryFormatter importFromCSVQueryFormatter
			= new SQLGeneralQueryFormatter();
		importFromCSVQueryFormatter.addQueryLine(0, "EXECUTE format ('");
		importFromCSVQueryFormatter.addQueryPhrase(0, "COPY ");
		importFromCSVQueryFormatter.addQueryPhrase("rif_data.");		
		importFromCSVQueryFormatter.addQueryPhrase(publishedDenominatorTableName);		
		importFromCSVQueryFormatter.addQueryPhrase(" (");
		importFromCSVQueryFormatter.padAndFinishLine();
		importFromCSVQueryFormatter.addQueryLine(1, "year,");
		importFromCSVQueryFormatter.addQueryLine(1, "age_sex_group,");
		for (String levelName : levelNames) {
			importFromCSVQueryFormatter.addQueryLine(1, levelName.toLowerCase() + ",");
		}
		importFromCSVQueryFormatter.addQueryLine(1, "total)");
		importFromCSVQueryFormatter.addQueryLine(0, "FROM ");
		importFromCSVQueryFormatter.addQueryLine(1, "%L");
		importFromCSVQueryFormatter.addQueryPhrase(0, "(FORMAT CSV, HEADER)', '");
		
		String filePath
			= super.getPublishedFilePath(denominator);
		importFromCSVQueryFormatter.addQueryPhrase(filePath);
		importFromCSVQueryFormatter.addQueryPhrase(".csv");
		importFromCSVQueryFormatter.addQueryPhrase("')");

		denominatorEntry.append(importFromCSVQueryFormatter.generateQuery());
		*/
	}
	
	private void addEntryToRIF40Tables(
		final StringBuilder denominatorEntry,
		final DataSetConfiguration dataSet) {

		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "INSERT INTO rif40.rif40_tables (");
		queryFormatter.addQueryLine(1, "theme,");
		queryFormatter.addQueryLine(1, "table_name,");
		queryFormatter.addQueryLine(1, "description,");
		queryFormatter.addQueryLine(1, "year_start,");
		queryFormatter.addQueryLine(1, "year_stop,");
		queryFormatter.addQueryLine(1, "total_field,");
		queryFormatter.addQueryLine(1, "isindirectdenominator,");		
		queryFormatter.addQueryLine(1, "isdirectdenominator,");
		queryFormatter.addQueryLine(1, "isnumerator,");
		queryFormatter.addQueryLine(1, "automatic,");
		queryFormatter.addQueryLine(1, "sex_field_name,");
		queryFormatter.addQueryLine(1, "age_group_field_name,");
		queryFormatter.addQueryLine(1, "age_sex_group_field_name,");
		queryFormatter.addQueryLine(1, "age_group_id) ");
		queryFormatter.addQueryLine(0, "SELECT ");
		
		DLHealthTheme healthTheme = dataSet.getHealthTheme();
		if (healthTheme == null) {
			queryFormatter.addQueryLine(1, "'SAHSULAND',");			
		}
		else {
			queryFormatter.addQueryLine(1, "'" + healthTheme.getName().toUpperCase() + "',");			
		}
		
		queryFormatter.addQueryLine(1, "'" + dataSet.getPublishedTableName().toUpperCase() + "',");
		queryFormatter.addQueryLine(1, "'" + dataSet.getDescription() + "',");
		queryFormatter.addQueryLine(1, "MIN(year),");
		queryFormatter.addQueryLine(1, "MAX(year),");
		queryFormatter.addQueryLine(1, "null,");
		queryFormatter.addQueryLine(1, "0,");
		queryFormatter.addQueryLine(1, "0,");
		queryFormatter.addQueryLine(1, "1,");
		queryFormatter.addQueryLine(1, "1,");
		queryFormatter.addQueryLine(1, "null,");
		queryFormatter.addQueryLine(1, "null,");
		queryFormatter.addQueryLine(1, "'AGE_SEX_GROUP',");
		queryFormatter.addQueryLine(1, "1");
		queryFormatter.addQueryLine(0, "FROM");
		queryFormatter.addQueryPhrase(1, dataSet.getPublishedTableName());
		String query
			= queryFormatter.generateQuery();
		denominatorEntry.append(query);	
	}

	private void addEntryToNumDenomTable(
		final StringBuilder numeratorEntry,
		final DataSetConfiguration numerator) {

		String[] literalParameterValues = new String[7];
		
		//Obtain value for 'geography' field
		DLGeography geography
			= numerator.getGeography();
		literalParameterValues[0] = geography.getName();
		
		//Obtain value for the 'numerator_table' field
		literalParameterValues[1] = numerator.getPublishedTableName().toUpperCase();
		//Obtain value for the 'numerator_description' field
		literalParameterValues[2] = numerator.getDescription();
		//Obtain value for the 'theme_description'
		DLHealthTheme healthTheme = numerator.getHealthTheme();
		//Obtain value for health theme description
		literalParameterValues[3] = healthTheme.getDescription();
		
		//Obtain value for 'denominator_table' field
		DataSetConfiguration denominator
			= numerator.getDependencyDataSetConfiguration();
		literalParameterValues[4] 
			= denominator.getPublishedTableName().toUpperCase();
		
		//Obtain value for 'denominator_description' field
		literalParameterValues[5] = denominator.getDescription();
		
		PGSQLInsertQueryFormatter insertQueryFormatter
			= new PGSQLInsertQueryFormatter();
		insertQueryFormatter.setDatabaseSchemaName("rif40");
		insertQueryFormatter.setIntoTable("rif_num_denom");
		insertQueryFormatter.addInsertField("geography", true);
		insertQueryFormatter.addInsertField("numerator_table", true);
		insertQueryFormatter.addInsertField("numerator_description", true);
		insertQueryFormatter.addInsertField("theme_description", true);		
		insertQueryFormatter.addInsertField("denominator_table", true);
		insertQueryFormatter.addInsertField("denominator_description", true);	
		
		String query
			= insertQueryFormatter.generateQueryWithLiterals(literalParameterValues);
		numeratorEntry.append(query);		
	}
	
	private void addRIF40TableOutcomesEntry(
		final StringBuilder numeratorEntry,
		final DataSetConfiguration numerator) {
		
		String publishedNumeratorTableName
			= numerator.getPublishedTableName();
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "INSERT INTO rif40.rif40_table_outcomes (");
		queryFormatter.addQueryLine(1, "outcome_group_name,");
		queryFormatter.addQueryLine(1, "numer_tab,");
		queryFormatter.addQueryLine(1, "current_version_start_year) ");
		queryFormatter.addQueryLine(0, "SELECT ");
		queryFormatter.addQueryLine(1, "'SAHSULAND_ICD',");
		queryFormatter.addQueryLine(1, "'" + publishedNumeratorTableName.toUpperCase() + "',");
		queryFormatter.addQueryLine(1, "MIN(year) ");
		queryFormatter.addQueryLine(0, "FROM ");
		queryFormatter.addQueryPhrase(1, publishedNumeratorTableName.toUpperCase());
		numeratorEntry.append(queryFormatter.generateQuery());		
	}

	private void addSchemaComments(
		final StringBuilder numeratorEntry,
		final DataSetConfiguration numerator) {

		String publishedCovariateTableName
			= numerator.getPublishedTableName().toUpperCase();		

		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetConfigurationUtility.getRequiredYearField(numerator);
					
		//Add comments to table
		numeratorEntry.append(
			createTableCommentQuery(
				publishedCovariateTableName, 
				numerator.getDescription()));

		numeratorEntry.append(
			createTableFieldCommentQuery(
				publishedCovariateTableName, 
				yearFieldConfiguration.getConvertFieldName(), 
				yearFieldConfiguration.getDescription()));	
		
		//age sex group field
		String ageSexGroupComment
			= RIFDataLoaderToolMessages.getMessage("defaultSchemaComments.ageSexGroup");
		numeratorEntry.append(
				createTableFieldCommentQuery(
					publishedCovariateTableName, 
					"age_sex_group", 
					ageSexGroupComment));		
		
		ArrayList<DataSetFieldConfiguration> resolutionFields
			= DataSetConfigurationUtility.getAllGeographicalResolutionFields(numerator);
		for (DataSetFieldConfiguration resolutionField : resolutionFields) {			
			numeratorEntry.append(
				createTableFieldCommentQuery(
					publishedCovariateTableName, 
					resolutionField.getConvertFieldName(), 
					resolutionField.getDescription()));			
		}
		
		DataSetFieldConfiguration healthFieldConfiguration
			= DataSetConfigurationUtility.getHealthCodeField(numerator);
		numeratorEntry.append(
			createTableFieldCommentQuery(
				publishedCovariateTableName, 
				healthFieldConfiguration.getConvertFieldName(), 
				healthFieldConfiguration.getDescription()));		

		DataSetFieldConfiguration totalField
			= numerator.getFieldHavingConvertFieldName("total");
		numeratorEntry.append(
			createTableFieldCommentQuery(
				publishedCovariateTableName, 
				totalField.getConvertFieldName(), 
				totalField.getDescription()));
	}
	
	private void createIndices(
		final StringBuilder numeratorEntry, 
		final DataSetConfiguration numerator) {
		
		String tableName
			= numerator.getPublishedTableName().toUpperCase();
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetConfigurationUtility.getRequiredYearField(numerator);
		createIndex(
			numeratorEntry,
			tableName,
			yearFieldConfiguration.getConvertFieldName());
		createIndex(
			numeratorEntry, 
			tableName, 
			"age_sex_group");
		
		ArrayList<DataSetFieldConfiguration> resolutionFields
			= DataSetConfigurationUtility.getAllGeographicalResolutionFields(numerator);
		for (DataSetFieldConfiguration resolutionField : resolutionFields) {
			String fieldName
				= resolutionField.getConvertFieldName();
			createIndex(
				numeratorEntry, 
				tableName, 
				fieldName);
		}
		
		DataSetFieldConfiguration healthCodeFieldConfiguration
			= DataSetConfigurationUtility.getHealthCodeField(numerator);
	
		createIndex(
			numeratorEntry,
			tableName,
			healthCodeFieldConfiguration.getConvertFieldName());	
		
		createIndex(
			numeratorEntry,
			tableName,
			"total");		
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


