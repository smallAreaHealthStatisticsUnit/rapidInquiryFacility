package org.sahsu.rif.dataloader.scriptgenerator.pg;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
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

public class PGDenominatorScriptGenerator 
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

	public PGDenominatorScriptGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateScript(
		final DataSetConfiguration denominator) {

		StringBuilder denominatorEntry = new StringBuilder();
		
		createTableStructureAndImportCSV(
			denominatorEntry, 
			denominator);
		
		createPrimarykey(
			denominatorEntry,
			denominator);			
		
		addSchemaComments(
			denominatorEntry,
			denominator);
		createIndices(
			denominatorEntry,
			denominator);
		
		addEntryToRIF40Tables(
			denominatorEntry, 
			denominator);

		createPermissions(
			denominatorEntry, 
			denominator);

		return denominatorEntry.toString();
	}
	
	private void createTableStructureAndImportCSV(
		final StringBuilder denominatorEntry,
		final DataSetConfiguration denominator) {

		DataSetFieldConfiguration yearFieldConfiguration
			= denominator.getFieldHavingConvertFieldName("year");		
		ArrayList<DataSetFieldConfiguration> resolutionFields
			= DataSetConfigurationUtility.getAllGeographicalResolutionFields(denominator);
		DataSetFieldConfiguration totalFieldConfiguration
			= denominator.getFieldHavingConvertFieldName("total");
		
		//The name the table will have in the schema 'pop'
		String publishedDenominatorTableName
			= denominator.getPublishedTableName().toUpperCase();		
		
		//Make a create table statement 
		PGSQLCreateTableQueryFormatter createTableQueryFormatter
			= new PGSQLCreateTableQueryFormatter();
		
		//Field properties that will help us construct the 
		//create and copy into statements

		createTableQueryFormatter.setDatabaseSchemaName("rif_data");
		createTableQueryFormatter.setTableName(publishedDenominatorTableName);
		
		createTableQueryFormatter.addIntegerFieldDeclaration(
			yearFieldConfiguration.getCleanFieldName().toUpperCase(), 
			false);
		
		//Do we assume these field names will be in increasing order of 
		//geographical resolution?
		
		for (DataSetFieldConfiguration resolutionField : resolutionFields) {
			String fieldName = resolutionField.getConvertFieldName().toUpperCase();
			createTableQueryFormatter.addTextFieldDeclaration(fieldName, 20, false);
		}
		
		createTableQueryFormatter.addIntegerFieldDeclaration(
			totalFieldConfiguration.getConvertFieldName().toUpperCase(), 
			false);
		
		//This is a field that will not appear in the input files but is derived
		//by the data loader tool.
		createTableQueryFormatter.addIntegerFieldDeclaration(
			"AGE_SEX_GROUP", 
			false);

		denominatorEntry.append(createTableQueryFormatter.generateQuery());
		denominatorEntry.append("\n\n");
		
		//How do we handle extra fields?
		ArrayList<String> fieldNames = new ArrayList<String>();
		fieldNames.add("YEAR");
		for (DataSetFieldConfiguration resolutionField : resolutionFields) {
			String fieldName = resolutionField.getConvertFieldName().toUpperCase();
			fieldNames.add(fieldName);
		}		
		fieldNames.add("TOTAL");
		fieldNames.add("AGE_SEX_GROUP");
			
		String filePath
			= super.getPublishedFilePath(denominator) + ".csv";		
		String bulkInsertStatement
			= createBulkCopyStatement(
				publishedDenominatorTableName,
				fieldNames,
				filePath);

		denominatorEntry.append(bulkInsertStatement);
	}
	
	private void addEntryToRIF40Tables(
		final StringBuilder denominatorEntry,
		final DataSetConfiguration denominator) {
		
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
		queryFormatter.addQueryLine(1, "'cancers',");
		queryFormatter.addQueryLine(1, "'" + denominator.getPublishedTableName().toUpperCase() + "',");
		queryFormatter.addQueryLine(1, "'" + denominator.getDescription() + "',");
		queryFormatter.addQueryLine(1, "MIN(YEAR),");
		queryFormatter.addQueryLine(1, "MAX(YEAR),");
		queryFormatter.addQueryLine(1, "null,");
		queryFormatter.addQueryLine(1, "1,");
		queryFormatter.addQueryLine(1, "0,");
		queryFormatter.addQueryLine(1, "0,");
		queryFormatter.addQueryLine(1, "1,");
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


	private void addSchemaComments(
		final StringBuilder denominatorEntry,
		final DataSetConfiguration denominator) {

		String publishedCovariateTableName
			= denominator.getPublishedTableName().toUpperCase();		

		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetConfigurationUtility.getRequiredYearField(denominator);
					
		//Add comments to table
		denominatorEntry.append(
			createTableCommentQuery(
				publishedCovariateTableName, 
				denominator.getDescription()));

		denominatorEntry.append(
			createTableFieldCommentQuery(
				publishedCovariateTableName, 
				yearFieldConfiguration.getConvertFieldName(), 
				yearFieldConfiguration.getDescription()));	
		
		//age sex group field
		String ageSexGroupComment
			= RIFDataLoaderToolMessages.getMessage("defaultSchemaComments.ageSexGroup");
		denominatorEntry.append(
				createTableFieldCommentQuery(
					publishedCovariateTableName, 
					"AGE_SEX_GROUP", 
					ageSexGroupComment));		
		
		ArrayList<DataSetFieldConfiguration> resolutionFields
			= DataSetConfigurationUtility.getAllGeographicalResolutionFields(denominator);
		for (DataSetFieldConfiguration resolutionField : resolutionFields) {			
			denominatorEntry.append(
				createTableFieldCommentQuery(
					publishedCovariateTableName, 
					resolutionField.getConvertFieldName(), 
					resolutionField.getDescription()));			
		}
		
		DataSetFieldConfiguration totalField
			= denominator.getFieldHavingConvertFieldName("total");
		denominatorEntry.append(
			createTableFieldCommentQuery(
				publishedCovariateTableName, 
				totalField.getConvertFieldName(), 
				totalField.getDescription()));
	}
	
	private void createIndices(
		final StringBuilder denominatorEntry, 
		final DataSetConfiguration denominator) {
		
		String tableName
			= denominator.getPublishedTableName().toUpperCase();
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetConfigurationUtility.getRequiredYearField(denominator);
		createIndex(
			denominatorEntry,
			tableName,
			yearFieldConfiguration.getConvertFieldName());
		createIndex(
			denominatorEntry, 
			tableName, 
			"age_sex_group");
		
		ArrayList<DataSetFieldConfiguration> resolutionFields
			= DataSetConfigurationUtility.getAllGeographicalResolutionFields(denominator);
		for (DataSetFieldConfiguration resolutionField : resolutionFields) {
			String fieldName
				= resolutionField.getConvertFieldName();
			createIndex(
				denominatorEntry, 
				tableName, 
				fieldName);
		}
		
		createIndex(
			denominatorEntry,
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


