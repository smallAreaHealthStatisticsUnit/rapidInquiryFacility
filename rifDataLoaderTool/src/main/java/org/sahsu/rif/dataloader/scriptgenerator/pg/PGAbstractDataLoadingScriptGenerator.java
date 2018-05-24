package org.sahsu.rif.dataloader.scriptgenerator.pg;

import java.io.File;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.pg.PGSQLSchemaCommentQueryFormatter;

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

public abstract class PGAbstractDataLoadingScriptGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private File scriptDirectory;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public PGAbstractDataLoadingScriptGenerator() {

	}

	protected void setScriptDirectory(final File scriptDirectory) {
		this.scriptDirectory = scriptDirectory;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/*
	protected void deleteTable(
		final StringBuilder tableEntry, 
		final String tableNameToDelete) {
		
		//@TODO: KLG Note that when we later consider iteratively 
		//running the loading scripts they should not be dropped
		//if other tables already refer to them.
		PGSQLDeleteTableQueryFormatter deleteTableQueryFormatter
			= new PGSQLDeleteTableQueryFormatter();
		deleteTableQueryFormatter.setTableToDelete(tableNameToDelete);
		tableEntry.append(deleteTableQueryFormatter.generateQuery());
		tableEntry.append("\n");
	}
	*/
	
	protected String createBulkCopyStatement(
		final String tableName,
		final ArrayList<String> fieldNames,
		final String importFilePath) {
		
		SQLGeneralQueryFormatter queryFormatter
			= new SQLGeneralQueryFormatter();
		queryFormatter.addQueryPhrase(0, "\\copy ");
		queryFormatter.addQueryPhrase(tableName);
		queryFormatter.addQueryPhrase("(");
		//queryFormatter.finishLine();
		int numberOfFields = fieldNames.size();
		for (int i = 0; i < numberOfFields; i++) {
			if (i != 0) {
				queryFormatter.addQueryPhrase(",");
				//queryFormatter.finishLine();
			}
			queryFormatter.addQueryPhrase(1, fieldNames.get(i));
		}
		queryFormatter.addQueryPhrase(") FROM '");
		queryFormatter.addQueryPhrase(importFilePath);
		queryFormatter.addQueryPhrase("' ");		
		queryFormatter.addQueryPhrase("DELIMITER ',' CSV HEADER");	
				
		return queryFormatter.generateQuery();
	}
	
	protected String getPublishedFilePath(
		final DataSetConfiguration dataSetConfiguration) {
		
		StringBuilder filePath = new StringBuilder();
		//filePath.append(scriptDirectory.getAbsolutePath());
		//filePath.append(File.separator);		
		String coreDataSetName
			= dataSetConfiguration.getName();
		RIFSchemaArea rifSchemaArea
			= dataSetConfiguration.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);		
		filePath.append(publishTableName);
		return filePath.toString();
	}
	

	protected String getPublishedDenominatorFilePathForNumerator(
		final DataSetConfiguration numerator) {
		
		DataSetConfiguration denominator
			= numerator.getDependencyDataSetConfiguration();

		StringBuilder filePath = new StringBuilder();
		filePath.append(scriptDirectory.getAbsolutePath());
		filePath.append(File.separator);		
		String coreDataSetName
			= denominator.getName();
		RIFSchemaArea rifSchemaArea 
			= denominator.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);		
		filePath.append(publishTableName);
		return filePath.toString();
	}	
	
	protected String createTableCommentQuery(
		final String tableName,
		final String comment) {
		
		PGSQLSchemaCommentQueryFormatter queryFormatter 
			= new PGSQLSchemaCommentQueryFormatter();
		
		queryFormatter.setTableComment(
			tableName.toUpperCase(), 
			comment);
		
		return queryFormatter.generateQuery();		
	}

	protected String createTableFieldCommentQuery(
		final String tableName,		
		final String columnName,
		final String comment) {
			
		PGSQLSchemaCommentQueryFormatter queryFormatter 
			= new PGSQLSchemaCommentQueryFormatter();
		queryFormatter.setTableColumnComment(
			tableName.toUpperCase(), 
			columnName.toUpperCase(), 
			comment);
		
		return queryFormatter.generateQuery();
	}
	
	protected void createIndex(
		final StringBuilder dataSetEntry,
		final String tableName, 
		final String fieldName) {
			
		String indexName = tableName.toUpperCase() + "_" + fieldName.toUpperCase();
			
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.setEndWithSemiColon(true);
		queryFormatter.addQueryPhrase(0, "CREATE INDEX ");
		queryFormatter.addQueryPhrase(indexName);
		queryFormatter.addQueryPhrase(" ON ");
		queryFormatter.addQueryPhrase(tableName.toUpperCase());		
		queryFormatter.addQueryPhrase("(");
		queryFormatter.addQueryPhrase(fieldName.toUpperCase());
		queryFormatter.addQueryPhrase(")");
		
		dataSetEntry.append(queryFormatter.generateQuery());
	}
		
	
	protected void createPermissions(
		final StringBuilder dataSetEntry,
		final DataSetConfiguration dataSetConfiguration) {
		
		String publishedTableName
			= dataSetConfiguration.getPublishedTableName().toUpperCase();
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryPhrase(0, "GRANT SELECT ON rif_data.");
		queryFormatter.addQueryPhrase(publishedTableName);
		queryFormatter.addQueryPhrase(" TO rif_user, rif_manager");
		
		dataSetEntry.append("\n");
		dataSetEntry.append(queryFormatter.generateQuery());
		dataSetEntry.append("\n\n");
		
	}
	
	protected void createPrimarykey(
		final StringBuilder dataSetEntry,
		final DataSetConfiguration dataSetConfiguration) {

		
		String publishedTableName
			= dataSetConfiguration.getPublishedTableName();
		String primaryKeyName = this.getPrimaryKeyName(dataSetConfiguration);
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryPhrase(0, "ALTER TABLE rif_data.");
		queryFormatter.addQueryPhrase(publishedTableName);
		queryFormatter.addQueryPhrase(" ADD CONSTRAINT ");
		queryFormatter.addQueryPhrase(primaryKeyName);
		queryFormatter.addQueryPhrase(" PRIMARY KEY(");		
		queryFormatter.addQueryPhrase("YEAR,");
		
		RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();

		if (rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA) {
			
			DataSetFieldConfiguration highestResolutionField
				= DataSetConfigurationUtility.getHighestGeographicalResolutionField(
					dataSetConfiguration);
			
			//We need the age sex group and the highest resolution field
			//along with year to make a primary key field.
			queryFormatter.addQueryPhrase("AGE_SEX_GROUP,");
			queryFormatter.addQueryPhrase(highestResolutionField.getConvertFieldName().toUpperCase());				
		}
		else if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
			
			DataSetFieldConfiguration highestResolutionField
				= DataSetConfigurationUtility.getHighestGeographicalResolutionField(
					dataSetConfiguration);
			
			//We need the age sex group and the highest resolution field
			//along with year to make a primary key field.
			queryFormatter.addQueryPhrase("AGE_SEX_GROUP,");
			queryFormatter.addQueryPhrase(highestResolutionField.getConvertFieldName().toUpperCase());
			queryFormatter.addQueryPhrase(",ICD");
		}
		else if (rifSchemaArea == RIFSchemaArea.COVARIATE_DATA) {
			//Here we only need year and highest resolution
			
			DataSetFieldConfiguration highestResolutionField
				= DataSetConfigurationUtility.getHighestGeographicalResolutionField(
					dataSetConfiguration);
			queryFormatter.addQueryPhrase(highestResolutionField.getConvertFieldName().toUpperCase());				
		}
		
		queryFormatter.addQueryPhrase(");");
		queryFormatter.finishLine();
		queryFormatter.addQueryPhrase(0, "CLUSTER ");
		queryFormatter.addQueryPhrase(publishedTableName);
		queryFormatter.addQueryPhrase(" USING ");
		queryFormatter.addQueryPhrase(primaryKeyName);
		
		
		dataSetEntry.append(queryFormatter.generateQuery());
		dataSetEntry.append("\n\n");		
	}	
	
	private String getPrimaryKeyName(final DataSetConfiguration dataSetConfiguration) {
		String publishedTableName
			= dataSetConfiguration.getPublishedTableName().toUpperCase();
		return(publishedTableName.toLowerCase() + "_pk");
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


