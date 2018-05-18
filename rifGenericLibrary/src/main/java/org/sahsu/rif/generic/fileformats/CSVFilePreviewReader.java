package org.sahsu.rif.generic.fileformats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;

/**
 * A very basic CSV file reader whose main purpose is to check the integrity of the 
 * first N lines in the file.  Currently error checking just focuses on making sure
 * that the lines have a consistent number of columns.  However, in future, it should
 * be modified to check/clean non-printable characters.
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

public class CSVFilePreviewReader {
	
	// ==========================================
	// Section Constants
	// ==========================================
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	private boolean isFirstLineHeader;
	private String delimiterRegularExpression;
	private int numberOfLinesToPreview;	
	private ArrayList<String> firstFewLines;	
	private String[] columnNames;
	
	//This is the LINE X: .... that goes with an annotated line entry
	private String lineLabelText;
	private ArrayList<String> logEntryLines;
	private ArrayList<String[]> previewData;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CSVFilePreviewReader() {

		isFirstLineHeader = true;
		delimiterRegularExpression = ",";
		numberOfLinesToPreview = 1000;
			
		lineLabelText
			= GENERIC_MESSAGES.getMessage(
				"csvFilePreviewReader.line.label");
		
		firstFewLines = new ArrayList<String>();
		columnNames = new String[0];
		logEntryLines = new ArrayList<String>();
		
		previewData = new ArrayList<String[]>();
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setIsFirstLineHeader(final boolean isFirstLineHeader) {
		this.isFirstLineHeader = isFirstLineHeader;
	}
	
	public void setNumberOfLinesToPreview(
		final int numberOfLinesToPreview) {
		
		this.numberOfLinesToPreview = numberOfLinesToPreview;
	}
	
	public void setDelimiterRegularExpression(final String delimiterRegularExpression) {
		this.delimiterRegularExpression = delimiterRegularExpression;
	}
	
	public void readFile(
		final File file)
			throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();		
		int currentLineNumber = 1;		
		
		int errorCount = 0;
		BufferedReader fileReader = null;
		String[] potentialColumnNames = new String[0];
		try {
			
			fileReader = new BufferedReader( new FileReader(file));

			int expectedNumberOfColumnsPerRow = 0;
			String currentLine = fileReader.readLine();
			if (isFirstLineHeader) {
				potentialColumnNames
					= currentLine.split(delimiterRegularExpression);
				expectedNumberOfColumnsPerRow = potentialColumnNames.length;
			}
			
			while ( (currentLine != null) && (currentLineNumber <= numberOfLinesToPreview)) {
				firstFewLines.add(currentLine);					
				
				String[] fieldValues 
					= currentLine.split(delimiterRegularExpression);
				if (currentLineNumber != 1) {
					previewData.add(fieldValues);
				}
				
				int currentNumberOfColumnsPerRow = fieldValues.length;
				if (currentNumberOfColumnsPerRow != expectedNumberOfColumnsPerRow) {
					String errorMessage
						= GENERIC_MESSAGES.getMessage(
							"csvFilePreviewReader.error.unexpectedNumberOfColumns",
							String.valueOf(currentLineNumber),
							String.valueOf(expectedNumberOfColumnsPerRow),
							String.valueOf(currentNumberOfColumnsPerRow));
					errorMessages.add(errorMessage);
					
					addAnnotatedErrorLineEntry(
						currentLineNumber,
						errorMessage,
						currentLine);

					errorCount++;
				}
				
				currentLine = fileReader.readLine();
				currentLineNumber++;
			}				
		}
		catch(FileNotFoundException fileNotFoundException) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"csvFilePreviewReader.error.nonExistentInputFile",
					file.getAbsolutePath());
			errorMessages.add(errorMessage);
		}
		catch(IOException ioException) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"csvFilePreviewReader.error.generalReaderError",
					file.getAbsolutePath());
			errorMessages.add(errorMessage);		
		}		
		finally {
			if (fileReader != null) {
				try {
					fileReader.close();					
				}
				catch(IOException ioException) {
					String errorMessage
						= GENERIC_MESSAGES.getMessage(
							"csvFilePreviewReader.error.unableToCloseFile",
							file.getAbsolutePath());
					errorMessages.add(errorMessage);					
				}
			}
		}
		
		if (logEntryLines.isEmpty() == false) {
			writeLogFile(file);
			
			//throw an exception indicating that 
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"",
					file.getName(),
					String.valueOf(errorCount));
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.ERRORS_DETECTED_IN_CSV_FILE, 
					errorMessage);
			throw rifServiceException;
		}
		else {
			setColumnNames(potentialColumnNames);
		}
		
	}
	
	private void addAnnotatedErrorLineEntry(
		final int lineNumber,
		final String errorMessage,
		final String lineOfText) {
		
		StringBuilder annotatedLine = new StringBuilder();
		annotatedLine.append(lineLabelText);
		annotatedLine.append(" ");
		annotatedLine.append(String.valueOf(lineNumber));
		annotatedLine.append(": ");		
		annotatedLine.append(lineOfText);
	
		logEntryLines.add(errorMessage);
		logEntryLines.add(annotatedLine.toString());		
	}
	
	
	private void setColumnNames(final String[] columnNames) {
		
		this.columnNames = columnNames;
	}
	
	public String[] getColumnNames() {
		return columnNames;
	}

	private void writeLogFile(final File dataFile) 
		throws RIFServiceException {
		
		String dataFilePath = dataFile.getAbsolutePath();
		String logFilePath = dataFilePath + ".log";
		
		File logFile = new File(logFilePath);
		
		BufferedWriter fileWriter = null;
		
		try {
			fileWriter
				= new BufferedWriter(new FileWriter(logFile));

			for (String logEntryLine : logEntryLines) {
				fileWriter.write(logEntryLine);
			}
		}
		catch(IOException ioException) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"csvFilePreviewReader.error.unableToWriteLogFile",
					logFile.getAbsolutePath());
			RIFServiceException rifServiceException
				= new RIFServiceException(
						RIFGenericLibraryError.UNABLE_TO_WRITE_CSV_ERROR_LOG_FILE, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			if (fileWriter != null) {
				try {
					fileWriter.flush();
					fileWriter.close();
				}
				catch(IOException ioException) {
					String errorMessage
						= GENERIC_MESSAGES.getMessage(
							"csvFilePreviewReader.error.unableToCloseFile",
							logFile.getName());
					RIFServiceException rifServiceException
						= new RIFServiceException(
								RIFGenericLibraryError.UNABLE_TO_WRITE_CSV_ERROR_LOG_FILE, 
							errorMessage);
					throw rifServiceException;
				}
			}
		}			
	}
	
	public String getFirstFewLinesAsHTML() {
		
		ByteArrayOutputStream outputStream
			= new ByteArrayOutputStream();
		HTMLUtility htmlUtility = new HTMLUtility();
		htmlUtility.initialise(outputStream, "UTF-8");
	
		htmlUtility.beginDocument();
		htmlUtility.beginBody();
		
		for (String currentLine : firstFewLines) {
			htmlUtility.writeParagraph(currentLine);
		}
		
		htmlUtility.endBody();
		htmlUtility.endDocument();
		
		try {
			String htmlFormattedInstructionText 
				= new String(outputStream.toByteArray(), "UTF-8");	
			outputStream.close();
			return htmlFormattedInstructionText;
		}
		catch(Exception exception) {
			return "";
		}
	}
	
	public String[][] getPreviewData() {
		return previewData.toArray(new String[0][0]);
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


