package rifDataLoaderTool.targetDBScriptGenerator.ms;

import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaArea;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.ms.MSSQLSchemaCommentQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;

import java.io.File;

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

public abstract class MSAbstractDataLoadingScriptGenerator {

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

	public MSAbstractDataLoadingScriptGenerator() {

	}

	protected void setScriptDirectory(final File scriptDirectory) {
		this.scriptDirectory = scriptDirectory;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	protected String createBulkCopyStatement(
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		MSBulkInsertQueryFormatter queryFormatter
			= new MSBulkInsertQueryFormatter();
		
		//first, write out the *.fmt file that is needed by
		//MS Server's bulk insert command
		queryFormatter.writeFormatFile(
			scriptDirectory, 
			dataSetConfiguration);
		//now write out the actual syntax for the bulk insert
		//command
		
		String tableName
			= dataSetConfiguration.getPublishedTableName();
		queryFormatter.setTableName(tableName);
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
		final String databaseSchemaName,
		final String tableName,
		final String comment) {
		
		MSSQLSchemaCommentQueryFormatter queryFormatter 
			= new MSSQLSchemaCommentQueryFormatter();
		queryFormatter.setDatabaseSchemaName(databaseSchemaName);
		
		queryFormatter.setTableComment(
			tableName.toUpperCase(), 
			comment);
		
		return queryFormatter.generateQuery();		
	}

	protected String createTableFieldCommentQuery(
		final String tableName,		
		final String columnName,
		final String comment) {
			
		MSSQLSchemaCommentQueryFormatter queryFormatter 
			= new MSSQLSchemaCommentQueryFormatter();
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
		queryFormatter.addQueryPhrase(0, "CREATE INDEX ");
		queryFormatter.addQueryPhrase(indexName);
		queryFormatter.addQueryPhrase(" ON ");
		queryFormatter.addQueryPhrase(tableName.toUpperCase());		
		queryFormatter.addQueryPhrase("(");
		queryFormatter.addQueryPhrase(fieldName.toUpperCase());
		queryFormatter.addQueryPhrase(")");
		
		dataSetEntry.append(queryFormatter.generateQuery());
	}
		
	
	protected String createPermissions(
		final DataSetConfiguration dataSetConfiguration) {
		
		String publishedTableName
			= dataSetConfiguration.getPublishedTableName().toUpperCase();
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryPhrase(0, "GRANT SELECT ON ");
		queryFormatter.addQueryPhrase(publishedTableName);
		queryFormatter.addQueryPhrase(" TO PUBLIC");
		
		return queryFormatter.generateQuery();
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


