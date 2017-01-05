package rifDataLoaderTool.targetDBScriptGenerator;

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

public class PGDenominatorScriptGenerator 
	extends AbstractDataLoadingScriptGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public PGDenominatorScriptGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateScript(
		final DLGeographyMetaData geographyMetaData,
		final DataSetConfiguration denominator) {

		StringBuilder denominatorEntry = new StringBuilder();
		
		createTableStructureAndImportCSV(
			denominatorEntry, 
			geographyMetaData, 
			denominator);
		denominatorEntry.append("\n\n");
		addEntryToRIF40Tables(
			denominatorEntry, 
			denominator);

		return denominatorEntry.toString();
	}
	
	private void createTableStructureAndImportCSV(
		final StringBuilder denominatorEntry,
		final DLGeographyMetaData geographyMetaData,
		final DataSetConfiguration denominator) {

		
		DataSetFieldConfiguration yearFieldConfiguration
			= denominator.getFieldHavingConvertFieldName("year");		
		ArrayList<DataSetFieldConfiguration> resolutionFields
			= denominator.getDataSetFieldConfigurations(
				FieldPurpose.GEOGRAPHICAL_RESOLUTION, 
				null);
		DataSetFieldConfiguration totalFieldConfiguration
			= denominator.getFieldHavingConvertFieldName("total");
		
		//Part I: Make a create table statement 
		PGSQLCreateTableQueryFormatter createTableQueryFormatter
			= new PGSQLCreateTableQueryFormatter();
		
		//The name the table will have in the schema 'pop'
		String publishedDenominatorTableName
			= denominator.getPublishedTableName().toUpperCase();		
		//Field properties that will help us construct the 
		//create and copy into statements
		ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurations
			= denominator.getFieldConfigurations();
		createTableQueryFormatter.setDatabaseSchemaName("pop");
		createTableQueryFormatter.setTableName(publishedDenominatorTableName);
		
		createTableQueryFormatter.addIntegerFieldDeclaration(
			yearFieldConfiguration.getCleanFieldName().toUpperCase(), 
			false);
		
		//This is a field that will not appear in the input files but is derived
		//by the data loader tool.
		createTableQueryFormatter.addIntegerFieldDeclaration(
			"AGE_SEX_GROUP", 
			false);

		//Do we assume these field names will be in increasing order of 
		//geographical resolution?
		DLGeography geography = denominator.getGeography();
		ArrayList<String> levelNames = geography.getLevelNames();
		for (String levelName : levelNames) {
			createTableQueryFormatter.addTextFieldDeclaration(levelName, false);
		}
		
		createTableQueryFormatter.addIntegerFieldDeclaration(
			totalFieldConfiguration.getConvertFieldName().toUpperCase(), 
			false);

		denominatorEntry.append(createTableQueryFormatter.generateQuery());
		denominatorEntry.append("\n\n");
		
		//How do we handle extra fields?
		
		//Add schema Comments
		denominatorEntry.append(createTableCommentQuery(
			publishedDenominatorTableName, 
			denominator.getDescription()));
		denominatorEntry.append(createTableFieldCommentQuery(
			publishedDenominatorTableName, 
			yearFieldConfiguration.getConvertFieldName().toUpperCase(),
			yearFieldConfiguration.getDescription()));				
		String ageSexGroupComment
			= RIFDataLoaderToolMessages.getMessage("defaultSchemaComments.ageSexGroup");

		denominatorEntry.append(createTableFieldCommentQuery(
			publishedDenominatorTableName, 
			"AGE_SEX_GROUP",
			ageSexGroupComment));
		for (DataSetFieldConfiguration resolutionField : resolutionFields) {
			denominatorEntry.append(createTableFieldCommentQuery(
				publishedDenominatorTableName, 
				resolutionField.getCleanFieldName().toUpperCase(),
				resolutionField.getDescription()));
		}
		denominatorEntry.append(createTableFieldCommentQuery(
			publishedDenominatorTableName, 
			totalFieldConfiguration.getCleanFieldName().toUpperCase(),
			totalFieldConfiguration.getDescription()));
				
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
	}
	
	private void addEntryToRIF40Tables(
		final StringBuilder denominatorEntry,
		final DataSetConfiguration denominator) {

		String[] literalParameterValues = new String[14];

		//Obtain the 'theme' field value
		DLHealthTheme healthTheme
			= denominator.getHealthTheme();
		//@TODO: Why does a denominator have a health theme?
		literalParameterValues[0] = "denominator health theme";
		
		//Obtain the 'table_name' field value
		literalParameterValues[1]
			= denominator.getPublishedTableName();
		//Obtain the 'description' field value
		literalParameterValues[2]
			= denominator.getDescription();

		/*
		 * @TODO.  We must assume that by the time this query is called the
		 * contents of the denominator CSV file will have already been loaded
		 * into a table in the 'pop' schema.  We need to develop a query that
		 * can extract the min and max years of the denominator table in order
		 * to fill in the fields year_start and year_stop in this table.
		 */
		//Obtain the 'year_start' field value
		literalParameterValues[3] = "1989";

		//Obtain the 'year_stop' field value
		literalParameterValues[4] = "1996";
		
		//Obtain the total_field.  As far as I know this will always be null
		literalParameterValues[5] = null;
		
		//Obtain the value for isindirectdenominator which should be the only
		//kind the RIF supports now
		literalParameterValues[6] = "1";
		
		//Obtain the value for isdirectdenominator.  This is unsupported right
		//now and should always be zero.
		literalParameterValues[7] = "0";

		//Obtain the value for isnumerator.  This will always be zero for
		//denominator table entries.
		literalParameterValues[8] = "0";
		
		//Obtain the value for automatic.  Not sure what this means but it
		//appears to always be 1
		literalParameterValues[9] = "0";

		//Obtain the value for sex_field_name.  It seems this does not 
		//have to be set.  @TODO Won't it always be age_sex_group?
		literalParameterValues[10] = null;		
		
		//Obtain the value for age_group_field_name.  It seems this does not 
		//have to be set.  @TODO Won't it always be age_sex_group?
		literalParameterValues[11] = null;			

		//Obtain the value for age_sex_group_field_name.  It seems this does not 
		//have to be set.  @TODO Won't it always be age_sex_group?
		literalParameterValues[12] = "age_sex_group";			
		
		//Obtain the value for age_group_id.  It seems this does not 
		//have to be set.
		literalParameterValues[13] = "1";
		
		/*
		PGSQLInsertQueryFormatter insertQueryFormatter
			= new PGSQLInsertQueryFormatter();
		insertQueryFormatter.setDatabaseSchemaName("rif40");
		insertQueryFormatter.setIntoTable("rif40_tables");
		
		insertQueryFormatter.addInsertField("theme", true);
		insertQueryFormatter.addInsertField("table_name", true);
		insertQueryFormatter.addInsertField("description", true);
		insertQueryFormatter.addInsertField("year_start", false);
		insertQueryFormatter.addInsertField("year_stop", false);
		insertQueryFormatter.addInsertField("total_field", true);
		insertQueryFormatter.addInsertField("isindirectdenominator", false);
		insertQueryFormatter.addInsertField("isdirectdenominator", false);
		insertQueryFormatter.addInsertField("isnumerator", false);
		insertQueryFormatter.addInsertField("automatic", false);
		insertQueryFormatter.addInsertField("sex_field_name", true);
		insertQueryFormatter.addInsertField("age_group_field_name", true);
		insertQueryFormatter.addInsertField("age_sex_group_field_name", true);
		insertQueryFormatter.addInsertField("age_group_id", false);
		String query
			= insertQueryFormatter.generateQueryWithLiterals(literalParameterValues);
		denominatorEntry.append(query);	
		*/
		
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
		queryFormatter.addQueryLine(1, "'denominator health theme',");
		queryFormatter.addQueryLine(1, "'" + denominator.getPublishedTableName().toUpperCase() + "',");
		queryFormatter.addQueryLine(1, "'" + denominator.getDescription() + "',");
		queryFormatter.addQueryLine(1, "MIN(year),");
		queryFormatter.addQueryLine(1, "MAX(year),");
		queryFormatter.addQueryLine(1, "null,");
		queryFormatter.addQueryLine(1, "1,");
		queryFormatter.addQueryLine(1, "0,");
		queryFormatter.addQueryLine(1, "0,");
		queryFormatter.addQueryLine(1, "0,");
		queryFormatter.addQueryLine(1, "null,");
		queryFormatter.addQueryLine(1, "null,");
		queryFormatter.addQueryLine(1, "'AGE_SEX_GROUP',");
		queryFormatter.addQueryLine(1, "1");
		queryFormatter.addQueryLine(0, "FROM");
		queryFormatter.addQueryPhrase(1, denominator.getPublishedTableName().toUpperCase());
		String query
			= queryFormatter.generateQuery();
		denominatorEntry.append(query);
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


