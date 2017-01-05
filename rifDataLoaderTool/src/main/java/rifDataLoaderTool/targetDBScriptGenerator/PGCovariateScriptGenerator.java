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

public class PGCovariateScriptGenerator 
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

	public PGCovariateScriptGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String generateScript(
		final DataSetConfiguration covariate) {

		StringBuilder covariateEntry = new StringBuilder();
			
		createTableStructureAndImportCSV(
			covariateEntry, 
			covariate);
		covariateEntry.append("\n");

		addEntryToRIF40CovariatesTable(
			covariateEntry, 
			covariate);	
		covariateEntry.append("\n");

		return covariateEntry.toString();
	}
	
	private void createTableStructureAndImportCSV(
		final StringBuilder covariateEntry,
		final DataSetConfiguration covariate) {
		
		//Part I: Make a create table statement 
		PGSQLCreateTableQueryFormatter createTableQueryFormatter
			= new PGSQLCreateTableQueryFormatter();
		
		//The name the table will have in the schema 'pop'
		String publishedDenominatorTableName
			= covariate.getPublishedTableName().toUpperCase();		
		//Field properties that will help us construct the 
		//create and copy into statements
		ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurations
			= covariate.getFieldConfigurations();
		createTableQueryFormatter.setDatabaseSchemaName("rif_data");
		createTableQueryFormatter.setTableName(publishedDenominatorTableName);
		
		//define the year field
		DataSetFieldConfiguration yearFieldConfiguration
			= getRequiredYearField(covariate);
		createTableQueryFormatter.addIntegerFieldDeclaration(
			yearFieldConfiguration.getCleanFieldName(), 
			false);
		
		//define the geographical resolution field
		DataSetFieldConfiguration resolutionFieldConfiguration
			= getRequiredGeographicalResolutionField(covariate);
		createTableQueryFormatter.addTextFieldDeclaration(
			resolutionFieldConfiguration.getCleanFieldName(), 
			false);

		ArrayList<DataSetFieldConfiguration> covariateFields
			= getCovariateFields(covariate);
		for (DataSetFieldConfiguration covariateField : covariateFields) {
			RIFDataType rifDataType = covariateField.getRIFDataType();
			
			if (rifDataType == RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE) {
				createTableQueryFormatter.addIntegerFieldDeclaration(
					covariateField.getCleanFieldName(), 
					true);
			}
			else if (rifDataType == RIFDataTypeFactory.RIF_DOUBLE_DATA_TYPE) {
				createTableQueryFormatter.addDoubleFieldDeclaration(
					covariateField.getCleanFieldName(), 
					true);				
			}
			else {
				System.out.println("Shouldn't happen field=="+ covariateField.getCleanFieldName()+"==has type=="+ rifDataType.getName()+"==");
			}
		}
		
		covariateEntry.append(createTableQueryFormatter.generateQuery());
		covariateEntry.append("\n");
		
		//Add comments to table
		covariateEntry.append(
			createTableCommentQuery(
				publishedDenominatorTableName, 
				covariate.getDescription()));
		covariateEntry.append(
			createTableFieldCommentQuery(
				publishedDenominatorTableName, 
				yearFieldConfiguration.getCleanFieldName(), 
				yearFieldConfiguration.getDescription()));				
		covariateEntry.append(
			createTableFieldCommentQuery(
				publishedDenominatorTableName, 
				resolutionFieldConfiguration.getCleanFieldName(), 
				resolutionFieldConfiguration.getDescription()));
		for (DataSetFieldConfiguration covariateField : covariateFields) {
			covariateEntry.append(
					createTableFieldCommentQuery(
						publishedDenominatorTableName, 
						covariateField.getCleanFieldName(), 
						covariateField.getDescription()));
		}
		
		//How do we handle extra fields?
		
		SQLGeneralQueryFormatter importFromCSVQueryFormatter
			= new SQLGeneralQueryFormatter();
		importFromCSVQueryFormatter.addQueryLine(0, "EXECUTE format ('");
		importFromCSVQueryFormatter.addQueryPhrase(0, "COPY ");
		importFromCSVQueryFormatter.addQueryPhrase("rif_data.");		
		importFromCSVQueryFormatter.addQueryPhrase(publishedDenominatorTableName);		
		importFromCSVQueryFormatter.addQueryPhrase(" (");
		importFromCSVQueryFormatter.padAndFinishLine();
		
		importFromCSVQueryFormatter.addQueryLine(
			1, 
			yearFieldConfiguration.getCleanFieldName() + ",");
		
		importFromCSVQueryFormatter.addQueryPhrase(
			1, 
			resolutionFieldConfiguration.getCleanFieldName());

		int numberOfCovariateFields = covariateFields.size();
		for (int i = 0; i < numberOfCovariateFields; i++) {
			DataSetFieldConfiguration currentCovariateField
				= covariateFields.get(i);

				importFromCSVQueryFormatter.addQueryPhrase(",");
				importFromCSVQueryFormatter.finishLine();					
				
				importFromCSVQueryFormatter.addQueryPhrase(
					1, 
					currentCovariateField.getCleanFieldName());
		}

		importFromCSVQueryFormatter.addQueryPhrase(")");
		importFromCSVQueryFormatter.padAndFinishLine();
		importFromCSVQueryFormatter.addQueryLine(0, "FROM ");
		importFromCSVQueryFormatter.addQueryLine(1, "%L");
		importFromCSVQueryFormatter.addQueryPhrase(0, "(FORMAT CSV, HEADER)', '");
		
		String filePath
			= super.getPublishedFilePath(covariate);
		importFromCSVQueryFormatter.addQueryPhrase(filePath);
		importFromCSVQueryFormatter.addQueryPhrase(".csv");
		importFromCSVQueryFormatter.addQueryPhrase("'");

		covariateEntry.append(importFromCSVQueryFormatter.generateQuery());
	}

	private DataSetFieldConfiguration getRequiredYearField(
		final DataSetConfiguration dataSetConfiguration) {
		
		//We know that for covariates, there should exactly one required year field
		ArrayList<DataSetFieldConfiguration> results
			= dataSetConfiguration.getDataSetFieldConfigurations(
				RIFDataTypeFactory.RIF_YEAR_DATA_TYPE, 
				FieldRequirementLevel.REQUIRED_BY_RIF);
		return results.get(0);		
	}


	private DataSetFieldConfiguration getRequiredGeographicalResolutionField(
		final DataSetConfiguration dataSetConfiguration) {
		
		//We know that for covariates, there should exactly one required geographical 
		//resolution field
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldPurpose currentFieldPurpose
				= fieldConfiguration.getFieldPurpose();
			FieldRequirementLevel currentFieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if ((currentFieldPurpose == FieldPurpose.GEOGRAPHICAL_RESOLUTION) &&
				((currentFieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) ||
				 (currentFieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD))) {
				
				//there should only be one
				return fieldConfiguration;
			}
		}
		return null;
	}
	
	private ArrayList<DataSetFieldConfiguration> getCovariateFields(
		final DataSetConfiguration dataSetConfiguration) {
		
		// We know that for covariates, there should exactly one required year 
		// field
		ArrayList<DataSetFieldConfiguration> results = new ArrayList<DataSetFieldConfiguration>();
		ArrayList<DataSetFieldConfiguration> fields
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration field : fields) {
			if (field.getFieldPurpose() == FieldPurpose.COVARIATE) {
				results.add(field);				
			}		
		}

		return results;
	}
	
	private void addEntryToRIF40CovariatesTable(
		final StringBuilder covariateEntry,
		final DataSetConfiguration covariateConfiguration) {
	
		String covariateTableName
			= covariateConfiguration.getPublishedTableName().toUpperCase();
		
		DLGeography geography = covariateConfiguration.getGeography();
		String geographyName = geography.getName().toUpperCase();
		
		DataSetFieldConfiguration requiredGeographicalResolutionField
			= this.getRequiredGeographicalResolutionField(covariateConfiguration);
		String geographicalResolutionName
			= requiredGeographicalResolutionField.getCleanFieldName().toUpperCase();
		
		ArrayList<DataSetFieldConfiguration> covariateFieldConfigurations
			= getCovariateFields(covariateConfiguration);
		System.out.println("PGCovariateScriptGenerator - adding ==" + covariateFieldConfigurations.size()+"==");
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
			queryFormatter.addQueryLine(1, "2)");			
		}
		else {
			//otherwise let's assume it's a type '1'
			queryFormatter.addQueryLine(1, "1)");
		}
		queryFormatter.addQueryLine(0, "FROM ");
		queryFormatter.addQueryPhrase(1, covariateTableName);
		String query
			= queryFormatter.generateQuery();
		covariateEntry.append(query);	
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


