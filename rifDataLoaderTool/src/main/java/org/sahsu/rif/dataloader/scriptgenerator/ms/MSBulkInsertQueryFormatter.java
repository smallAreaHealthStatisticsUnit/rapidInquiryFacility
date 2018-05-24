package org.sahsu.rif.dataloader.scriptgenerator.ms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.generic.datastorage.AbstractSQLQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;

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

public class MSBulkInsertQueryFormatter extends AbstractSQLQueryFormatter {

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
		addQueryPhrase(0, "BULK INSERT ");
		addQueryPhrase(getDatabaseSchemaName());
		addQueryPhrase(".");
		addQueryPhrase(tableName);
		addQueryPhrase(" FROM '");
		addQueryPhrase(generateCSVFilePath());
		addQueryPhrase("'");
		finishLine();
		addQueryLine(0, "WITH ");
		addQueryLine(0, "(");
		addQueryPhrase(1, "FORMATFILE = '$(pwd)/");
		addQueryPhrase(generateFormatFilePath());
		addQueryPhrase("',");
		finishLine();
		addQueryLine(1, "TABLOCK,");
		addQueryLine(1, "FIRSTROW=2");
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
		
		StringBuilder formatFilePath = new StringBuilder();
		formatFilePath.append(outputDirectory.getAbsolutePath());
		formatFilePath.append(File.separator);
		formatFilePath.append(generateFormatFilePath());

		File formatFile = new File(formatFilePath.toString());
		System.out.println("Format file path==" + formatFilePath + "==");
		
		try {		
			writer = new BufferedWriter(new FileWriter(formatFile));
			writer.write("<?xml version=\"1.0\"?>");
			writer.newLine();
			
			writer.write("<!-- MS SQL Server bulk load format files.  ");
			writer.write("The insistence on quotes excludes the header row -->");
			writer.newLine();
			writer.write("<BCPFORMAT xmlns=\"http://schemas.microsoft.com/sqlserver/2004/bulkload/format\" ");
			writer.write("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
			writer.newLine();
			ArrayList<DataSetFieldConfiguration> fields
				= dataSetConfiguration.getFieldConfigurations();
			int numberOfFields = fields.size();
			
			RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
			if (rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA) {
				writeDenominatorFragment(writer, dataSetConfiguration);
			}
			else if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
				writeNumeratorFragment(writer, dataSetConfiguration);
			}
			else if (rifSchemaArea == RIFSchemaArea.COVARIATE_DATA) {
				writeCovariateFragment(writer, dataSetConfiguration);
			}
			writer.newLine();
			writer.write("</BCPFORMAT>");
			writer.newLine();			
		}
		catch(IOException ioException) {
			exceptionFactory.createFileWritingProblemException(formatFilePath.toString());
		}
		finally {
			try {
				writer.flush();
				writer.close();
			}
			catch(IOException ioException) {
				exceptionFactory.createFileWritingProblemException(formatFilePath.toString());				
			}
		}
		
		
	}
	
	private void writeDenominatorFragment(
		final BufferedWriter writer,
		final DataSetConfiguration dataSetConfiguration) 
		throws IOException {


		//Write record section
		writer.write("<RECORD>");			
		writer.newLine();

		int index = 0;
		writeFieldEntry(writer, ++index, ","); //year
		writeFieldEntry(writer, ++index, ","); //age_sex_group
		
		ArrayList<DataSetFieldConfiguration> geographicalResolutionFields
			= DataSetConfigurationUtility.getAllGeographicalResolutionFields(dataSetConfiguration);
		for (DataSetFieldConfiguration geographicalResolutionField : geographicalResolutionFields) {
			writeFieldEntry(writer, ++index, ","); //geographical resolution field				
		}
		writeFieldEntry(writer, ++index, "\\n"); //total
		writer.write("</RECORD>");			
		writer.newLine();			

		writer.write("<ROW>");
		writer.newLine();

		index = 0;
		
		writeColumnSourceEntry(writer, ++index, "year");			
		//writeColumnSourceEntry(writer, ++index, "age_sex_group");

		for (DataSetFieldConfiguration geographicalResolutionField : geographicalResolutionFields) {
			String fieldName
				= geographicalResolutionField.getConvertFieldName().toLowerCase();
			writeColumnSourceEntry(writer, ++index, fieldName); //geographical resolution field				
		}
		writeColumnSourceEntry(writer, ++index, "total");
		writeColumnSourceEntry(writer, ++index, "age_sex_group");
		
		writer.write("</ROW>");
		writer.newLine();			
	}

	
	private void writeNumeratorFragment(
		final BufferedWriter writer,
		final DataSetConfiguration dataSetConfiguration) 
		throws IOException {


		//Write record section
		writer.write("<RECORD>");			
		writer.newLine();

		int index = 0;
		writeFieldEntry(writer, ++index, ","); //year
		writeFieldEntry(writer, ++index, ","); //age_sex_group
		
		ArrayList<DataSetFieldConfiguration> geographicalResolutionFields
			= DataSetConfigurationUtility.getAllGeographicalResolutionFields(dataSetConfiguration);
		for (DataSetFieldConfiguration geographicalResolutionField : geographicalResolutionFields) {
			writeFieldEntry(writer, ++index, ","); //geographical resolution field				
		}
		writeFieldEntry(writer, ++index, ","); //icd
		writeFieldEntry(writer, ++index, "\\n"); //total
		writer.write("</RECORD>");			
		writer.newLine();			

		writer.write("<ROW>");
		writer.newLine();

		index = 0;
		
		writeColumnSourceEntry(writer, ++index, "year");			

		for (DataSetFieldConfiguration geographicalResolutionField : geographicalResolutionFields) {
			String fieldName
				= geographicalResolutionField.getConvertFieldName().toLowerCase();
			writeColumnSourceEntry(writer, ++index, fieldName); //geographical resolution field				
		}
		writeColumnSourceEntry(writer, ++index, "icd");
		writeColumnSourceEntry(writer, ++index, "total");
		writeColumnSourceEntry(writer, ++index, "age_sex_group");
		
		writer.write("</ROW>");
		writer.newLine();			
	}

	private void writeCovariateFragment(
		final BufferedWriter writer,
		final DataSetConfiguration dataSetConfiguration) 
		throws IOException {
		
		ArrayList<DataSetFieldConfiguration> covariateFields
			= DataSetConfigurationUtility.getCovariateFields(dataSetConfiguration);
		DataSetFieldConfiguration geographicalResolutionField
			= DataSetConfigurationUtility.getRequiredGeographicalResolutionField(dataSetConfiguration);
		
		//Write record section
		writer.write("<RECORD>");			
		writer.newLine();

		int index = 0;
		writeFieldEntry(writer, ++index, ","); //year
		writeFieldEntry(writer, ++index, ","); //geographical field level

		int numberOfCovariates = covariateFields.size();
		int lastCovariateIndex = numberOfCovariates - 1;
		
		for (int i = 0; i < numberOfCovariates; i++) {		
			if (i == lastCovariateIndex) {
				writeFieldEntry(writer, ++index, "\\n"); //geographical field level
			}
			else {
				writeFieldEntry(writer, ++index, ","); //geographical field level				
			}
		}
		writer.write("</RECORD>");			
		writer.newLine();			

		writer.write("<ROW>");
		writer.newLine();

		index = 0;
		
		writeColumnSourceEntry(writer, ++index, "year");
		
		writeColumnSourceEntry(
			writer, 
			++index, 
			geographicalResolutionField.getConvertFieldName().toLowerCase());
		
		for (DataSetFieldConfiguration covariateField : covariateFields) {
			String fieldName = covariateField.getConvertFieldName().toLowerCase();
			writeColumnSourceEntry(writer, ++index, fieldName); //geographical resolution field				
		}
		writer.write("</ROW>");
		writer.newLine();			
	}
	
	private void writeFieldEntry(
		final BufferedWriter writer,
		final int index,
		final String terminatorCharacter) 
		throws IOException {
		
		writer.write("<FIELD ID=\"");
		writer.write(String.valueOf(index));
		writer.write("\" xsi:type=\"CharTerm\" TERMINATOR='");
		writer.write(terminatorCharacter);
		writer.write("' />");
		writer.newLine();
		writer.flush();
	}

	private void writeColumnSourceEntry(
		final BufferedWriter writer,
		final int index,
		final String fieldName) 
		throws IOException {
		
		writer.write("<COLUMN SOURCE=\"");
		writer.write(String.valueOf(index));
		writer.write("\" NAME=\"");
		writer.write(fieldName);
		writer.write("\" xsi:type=\"SQLVARYCHAR\" />");
		writer.newLine();		
		writer.flush();
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


