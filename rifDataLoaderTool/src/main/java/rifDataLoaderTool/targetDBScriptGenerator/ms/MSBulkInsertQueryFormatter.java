package rifDataLoaderTool.targetDBScriptGenerator.ms;

import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;

import rifGenericLibrary.system.*;
import rifGenericLibrary.dataStorageLayer.ms.AbstractMSSQLQueryFormatter;

import java.io.*;
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

public class MSBulkInsertQueryFormatter extends
	AbstractMSSQLQueryFormatter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String path;
	private String tableName;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public MSBulkInsertQueryFormatter() {
		path = "$(pwd)";
		this.setEndWithSemiColon(false);
	}


	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	@Override
	public String generateQuery() {
		addQueryPhrase(0, "BULK INSERT \'");
		addQueryPhrase(generateCSVFilePath());
		addQueryPhrase("'");
		finishLine();
		addQueryLine(0, "WITH ");
		addQueryLine(0, "(");
		addQueryPhrase(1, "FORMATFILE = '");
		addQueryPhrase(generateFormatFilePath());
		addQueryPhrase("',");
		finishLine();
		addQueryLine(1, "TABLOCK");
		addQueryPhrase(0, ")");
		
		return super.generateQuery();
	}

	public void writeFormatFile(
		final File outputDirectory, 
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		BufferedWriter writer = null;		
		RIFServiceExceptionFactory exceptionFactory
			= new RIFServiceExceptionFactory();	
		
		String tableName = dataSetConfiguration.getPublishedTableName();
		this.setTableName(tableName);
		String formatFilePath = generateFormatFilePath();
		
		File formatFile = new File(formatFilePath);
		
		try {		
			writer = new BufferedWriter(new FileWriter(formatFile));
			writer.write("<?xml version=\"1.0\"?>");
			writer.newLine();
			
			writer.write("<!-- MS SQL Server bulk load format files.  ");
			writer.write("The insistence on quotes excludes the header row -->");
			writer.newLine();
			
			ArrayList<DataSetFieldConfiguration> fields
				= dataSetConfiguration.getFieldConfigurations();
			int numberOfFields = fields.size();

			//Write record section
			writer.write("<RECORD>");
			for (int i = 0; i < numberOfFields; i++) {				
				writer.write("<FIELD ID=\"");
				writer.write(String.valueOf(i + 1));
				writer.write("\" xsi-type=\"CharTerm\" TERMINATOR=',' />");
				writer.newLine();
			}			
			writer.write("</RECORD>");
			writer.newLine();
			
			//Write row section
			writer.write("<ROW>");
			for (int i = 0; i < numberOfFields; i++) {				
				writer.write("<FIELD ID=\"");
				writer.write(String.valueOf(i + 1));
				writer.write("\" NAME=\"");
				DataSetFieldConfiguration fieldConfiguration
					= fields.get(i);				
				String fieldName
					= fieldConfiguration.getConvertFieldName();
				writer.write(fieldName);
				writer.write("\" xsi-type=\"SQLVARYCHAR\" />");
				writer.newLine();
			}			
			writer.write("</ROW>");
			writer.newLine();
			
		}
		catch(IOException ioException) {
			exceptionFactory.createFileWritingProblemException(formatFilePath);
		}
		finally {
			try {
				writer.flush();
				writer.close();
			}
			catch(IOException ioException) {
				exceptionFactory.createFileWritingProblemException(formatFilePath);				
			}
		}
		
		
	}
	
	private String generateCSVFilePath() {
		StringBuilder filePath = new StringBuilder();
		filePath.append("");
		filePath.append(path);
		filePath.append("/");
		filePath.append(tableName);
		filePath.append(".csv");
		return filePath.toString();
	}
	
	private String generateFormatFilePath() {
		StringBuilder filePath = new StringBuilder();
		filePath.append(tableName);
		filePath.append(".fmt");
		return filePath.toString();
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


