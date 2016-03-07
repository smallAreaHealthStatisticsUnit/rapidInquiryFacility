package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.dataStorageLayer.*;
import rifDataLoaderTool.system.*;
import rifGenericLibrary.system.*;
import rifGenericLibrary.dataStorageLayer.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;



/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public class PostgreSQLDataLoadingScriptWriter {
		
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public PostgreSQLDataLoadingScriptWriter() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void writeFile(
		final File dataLoadingScriptFile,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		try {
			
			FileWriter fileWriter = new FileWriter(dataLoadingScriptFile);

			//write comments
			writeCommentSectionDivider(fileWriter);
			String headerCommentLine
				= RIFDataLoaderToolMessages.getMessage(
					"loadingScriptWriter.comment.header",
					dataSetConfiguration.getName());
			writeCommentLine(fileWriter, headerCommentLine);
			
			String databaseType
				= RIFDataLoaderToolMessages.getMessage(
					"loadingScriptWriter.comment.databaseType.postgreSQL");
			
			String targetDatabaseCommentLine
				= RIFDataLoaderToolMessages.getMessage(
					"loadingScriptWriter.comment.targetDatabase",
					databaseType);
			writeCommentLine(fileWriter, targetDatabaseCommentLine);

			Date currentTime = new Date();
			String currentTimePhrase
				= RIFDataLoaderToolMessages.getTimePhrase(currentTime);
			String timeStampCommentLine
				= RIFDataLoaderToolMessages.getMessage(
					"loadingScriptWriter.comment.timeStamp",
					currentTimePhrase);
			writeCommentLine(fileWriter, timeStampCommentLine);
			writeCommentSectionDivider(fileWriter);

			addBlankLine(fileWriter);
			addBlankLine(fileWriter);
			addBlankLine(fileWriter);

			writeCreateTableStatement(
				fileWriter, 
				dataSetConfiguration);

			addBlankLine(fileWriter);
			addBlankLine(fileWriter);
		

			writeCopyIntoStatement(
				fileWriter, 
				dataSetConfiguration);
			
			addBlankLine(fileWriter);
			addBlankLine(fileWriter);
						
			writeIndices(
				fileWriter,
				dataSetConfiguration);

			addBlankLine(fileWriter);
			addBlankLine(fileWriter);
			
			writeSchemaComments(
				fileWriter, 
				dataSetConfiguration);
			
			fileWriter.flush();
			fileWriter.close();
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
	}
	
	private void writeCommentSectionDivider(
		final FileWriter fileWriter)
		throws Exception {
		
		fileWriter.write("--");
		String sectionDividerText
			= RIFDataLoaderToolMessages.getMessage("loadingScriptWriter.comment.sectionDivider");
		fileWriter.write(sectionDividerText);
		fileWriter.write("\n");
		fileWriter.flush();
	}
	
	private void writeCommentLine(
		final FileWriter fileWriter,
		final String comment) 
		throws Exception {

		fileWriter.write("-- ");
		fileWriter.write(comment);
		fileWriter.write("\n");
		fileWriter.flush();
	}
	
	private void writeCreateTableStatement(
		final FileWriter fileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws Exception {

		writeCommentSectionDivider(fileWriter);
		String createTableComment
			= RIFDataLoaderToolMessages.getMessage("loadingScriptWriter.comment.createTable");
		writeCommentLine(fileWriter, createTableComment);
		writeCommentSectionDivider(fileWriter);
				
		//Write published table
		String coreDataSetName
			= dataSetConfiguration.getName();		
		RIFSchemaArea rifSchemaArea
			= dataSetConfiguration.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);
			
		SQLCreateTableQueryFormatter createTableQueryFormatter
			= new SQLCreateTableQueryFormatter();
		createTableQueryFormatter.setEndWithSemiColon(true);
		createTableQueryFormatter.setTableName(publishTableName);

		//these two fields will be the primary keys
		createTableQueryFormatter.addFieldDeclaration(
			"data_source_id", 
			"int", 
			false);
		createTableQueryFormatter.addFieldDeclaration(
			"row_number", 
			"int", 
			false);
		
		//Process the fields which did not require conversion
		ArrayList<DataSetFieldConfiguration> fieldsWithoutConversion
			= dataSetConfiguration.getFieldsWithoutConversionFunctions();
		for (DataSetFieldConfiguration fieldWithoutConversion : fieldsWithoutConversion) {
			boolean isRequired
				= !fieldWithoutConversion.isEmptyValueAllowed();
			String dataTypePhrase
				= getDataTypePhrase(fieldWithoutConversion);
			createTableQueryFormatter.addFieldDeclaration(
				fieldWithoutConversion.getConvertFieldName(), 
				dataTypePhrase, 
				null, 
				isRequired);
		}		
		fileWriter.write(createTableQueryFormatter.generateQuery());
		
		fileWriter.write("\n");
		
		//Add primary keys
		
		writeCommentSectionDivider(fileWriter);
		String addingPrimaryKeysComment
			= RIFDataLoaderToolMessages.getMessage("loadingScriptWriter.comment.addingPrimaryKeys");
		writeCommentLine(fileWriter, addingPrimaryKeysComment);
		writeCommentSectionDivider(fileWriter);
		
		
		SQLGeneralQueryFormatter addPrimaryKeysQueryFormatter
			= new SQLGeneralQueryFormatter();
		addPrimaryKeysQueryFormatter.setEndWithSemiColon(true);
		addPrimaryKeysQueryFormatter.addQueryPhrase("ALTER TABLE ");
		addPrimaryKeysQueryFormatter.addQueryPhrase(publishTableName);
		addPrimaryKeysQueryFormatter.addQueryPhrase(" ADD PRIMARY KEY (data_set_id, row_number)");		
		fileWriter.write(addPrimaryKeysQueryFormatter.generateQuery());
		

	}

	private void writeCopyIntoStatement(
		final FileWriter fileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws Exception {

		
		String coreDataSetName = dataSetConfiguration.getName();
		RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);

		StringBuilder csvFileName = new StringBuilder();
		csvFileName.append(publishTableName);
		csvFileName.append(".csv");
		
		SQLImportTableFromCSVQueryFormatter queryFormatter
			= new SQLImportTableFromCSVQueryFormatter();
		queryFormatter.setEndWithSemiColon(true);
		queryFormatter.setImportFileName(csvFileName.toString());
		queryFormatter.setTableToImport(publishTableName);

		fileWriter.write(queryFormatter.generateQuery());
	}
	
	/*
	 * Creates field type of "text", "int", "double precision", "date"
	 */
	private String getDataTypePhrase(final DataSetFieldConfiguration dataSetFieldConfiguration) {
		RIFDataType dataType = dataSetFieldConfiguration.getRIFDataType();
		
		if ( RIFDataTypeFactory.isAgeDataType(dataType) ||
			 RIFDataTypeFactory.isYearDataType(dataType) ||
			 RIFDataTypeFactory.isIntegerDataType(dataType)) {
			
			return "INT";
		}
		else if (RIFDataTypeFactory.isDoubleDataType(dataType)) {
			return "DOUBLE PRECISION";
		}
		else if (RIFDataTypeFactory.isDateDataType(dataType)) {
			return "DATE";
		}		
		else {
			return "TEXT";
		}
		
		
	}
	
	private void writeIndices(
		final FileWriter fileWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws Exception {
		
		writeCommentSectionDivider(fileWriter);
		String addingPrimaryKeysComment
			= RIFDataLoaderToolMessages.getMessage("loadingScriptWriter.comment.addingIndices");
		writeCommentLine(fileWriter, addingPrimaryKeysComment);
		writeCommentSectionDivider(fileWriter);
		
		String coreDataSetName
			= dataSetConfiguration.getName();
		RIFSchemaArea rifSchemaArea
			= dataSetConfiguration.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if ((fieldConfiguration.getFieldRequirementLevel() == FieldRequirementLevel.REQUIRED_BY_RIF) ||
				(fieldConfiguration.optimiseUsingIndex())) {
				
				SQLCreateIndexQueryFormatter queryFormatter
					= new SQLCreateIndexQueryFormatter();
				queryFormatter.setIndexTable(publishTableName);
				queryFormatter.setIndexTableField(fieldConfiguration.getConvertFieldName());
				queryFormatter.setEndWithSemiColon(true);
				writeQuery(fileWriter, queryFormatter);
			}
		}
		
		
		
	}
	
	
	private void writeSchemaComments(
		final FileWriter fileWriter, 
		final DataSetConfiguration dataSetConfiguration) 
		throws Exception {

		writeCommentSectionDivider(fileWriter);
		String createTableComment
			= RIFDataLoaderToolMessages.getMessage("loadingScriptWriter.comment.addingSchemaComments");
		writeCommentLine(fileWriter, createTableComment);
		writeCommentSectionDivider(fileWriter);
				

		
		String coreDataSetName = dataSetConfiguration.getName();
		String publishTableName
			= RIFTemporaryTablePrefixes.PUBLISH.getTableName(coreDataSetName);
		
		SQLAddCommentQueryFormatter tableCommentQueryFormatter
			= new SQLAddCommentQueryFormatter();
		tableCommentQueryFormatter.setEndWithSemiColon(true);
		tableCommentQueryFormatter.setTableName(publishTableName);
		tableCommentQueryFormatter.setComment(dataSetConfiguration.getDescription());
		writeQuery(fileWriter, tableCommentQueryFormatter);
				
		SQLAddCommentQueryFormatter tableFieldCommentQueryFormatter
			= new SQLAddCommentQueryFormatter();
		tableFieldCommentQueryFormatter.setEndWithSemiColon(true);		
		tableFieldCommentQueryFormatter.setTableName(publishTableName);
		tableFieldCommentQueryFormatter.setTableFieldName("data_set_id");
		String dataSetIdentifierComment
			= RIFDataLoaderToolMessages.getMessage("dataSetFieldConfiguration.dataSetIdentifier.description");
		tableFieldCommentQueryFormatter.setComment(dataSetIdentifierComment);
		writeQuery(fileWriter, tableFieldCommentQueryFormatter);

		String dataSetRowNumberComment
			= RIFDataLoaderToolMessages.getMessage("dataSetFieldConfiguration.rowNumber.description");
		tableFieldCommentQueryFormatter.setTableFieldName("row_number");
		tableFieldCommentQueryFormatter.setComment(dataSetRowNumberComment);		
		writeQuery(fileWriter, tableFieldCommentQueryFormatter);
		
		//process the fields that underwent some kind of conversion
		
		
		
		
		
		
		
	}
	
	private void writeQuery(final FileWriter fileWriter,
		final AbstractSQLQueryFormatter queryFormatter) 
		throws Exception {
				
		fileWriter.write(queryFormatter.generateQuery());
		addBlankLine(fileWriter);
		addBlankLine(fileWriter);
	}
	
	private void addBlankLine(final FileWriter fileWriter) 
		throws Exception {
		
		fileWriter.write("\n");
		fileWriter.flush();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void logException(final Exception exception) {
		
	}

	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


