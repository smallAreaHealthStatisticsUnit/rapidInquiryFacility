package rifDataLoaderTool.targetDBScriptGenerator;

import rifDataLoaderTool.businessConceptLayer.*;

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
			geographyMetaData, 
			denominatorEntry, 
			denominator);
		addEntryToRIF40Tables(
			denominatorEntry, 
			denominator);

		return denominatorEntry.toString();
	}
	
	private void createTableStructureAndImportCSV(
		final DLGeographyMetaData geographyMetaData,
		final StringBuilder denominatorEntry,
		final DataSetConfiguration denominator) {
		
		
		//Part I: Make a create table statement 
		PGSQLCreateTableQueryFormatter createTableQueryFormatter
			= new PGSQLCreateTableQueryFormatter();
		
		//The name the table will have in the schema 'pop'
		String publishedDenominatorTableName
			= denominator.getPublishedTableName();		
		//Field properties that will help us construct the 
		//create and copy into statements
		ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurations
			= denominator.getFieldConfigurations();

		
		createTableQueryFormatter.setDatabaseSchemaName("pop");
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
		createTableQueryFormatter.addIntegerFieldDeclaration("total", false);

		//How do we handle extra fields?
		
		
		/*
		for (DataSetFieldConfiguration dataSetFieldConfiguration : dataSetFieldConfigurations) {
			FieldRequirementLevel fieldRequirementLevel
				= dataSetFieldConfiguration.getFieldRequirementLevel();
			if (fieldRequirementLevel != FieldRequirementLevel.IGNORE_FIELD) {
				String cleanName
					= dataSetFieldConfiguration.getCleanFieldName();
				//@TODO. Do we assume that the published table will have
				//the first field being year, the second one being age_sex_group,
				//then all the geographical levels, then one called total?
				//What do we do about extra columns?
				
				
				createTableQueryFormatter.add
				
			}
			
			
		}
		*/
		
		/*
		
		PGSQLImportTableFromCSVQueryFormatter importTableQueryFormatter
			= new PGSQLImportTableFromCSVQueryFormatter();
		importTableQueryFormatter.setTableToImport(publishedDenominatorTableName);
		importTableQueryFormatter.a
		
		SQLGeneralQueryFormatter queryFormatter
			= new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "EXECUTE format ('");
		queryFormatter.addQueryPhrase(0, "COPY ");
		queryFormatter.addQueryPhrase(publishedDenominatorTableName);		
		queryFormatter.addQueryPhrase(" (");
		for (DataSetFieldConfiguration dataSetFieldConfiguration : dataSetFieldConfigurations) {
			FieldRequirementLevel fieldRequirementLevel
				= dataSetFieldConfiguration.getFieldRequirementLevel();
			if (fieldRequirementLevel != FieldRequirementLevel.IGNORE_FIELD) {
				
				
			}
		}
	*/	
		
		

	}
	
	private void addEntryToRIF40Tables(
		final StringBuilder denominatorEntry,
		final DataSetConfiguration denominator) {

		String[] literalParameterValues = new String[14];

		//Obtain the 'theme' field value
		DLHealthTheme healthTheme
			= denominator.getHealthTheme();
		literalParameterValues[0] = healthTheme.getName();
		
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
		literalParameterValues[8] = "0";
		
		
		
		
		
		String yearStart = "1989";
		String yearStop = "1996";
		
		
		
		
		
		
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


