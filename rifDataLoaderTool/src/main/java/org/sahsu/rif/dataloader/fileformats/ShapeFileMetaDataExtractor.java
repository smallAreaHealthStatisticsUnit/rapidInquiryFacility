package org.sahsu.rif.dataloader.fileformats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.sahsu.rif.dataloader.concepts.ShapeFile;
import org.sahsu.rif.dataloader.concepts.ShapeFileComponent;
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

public class ShapeFileMetaDataExtractor {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String projectionName;
	private ArrayList<String> shapeFileFieldNames;
	private String[][] shapeFilePreviewData;
	private Integer totalAreaIdentifiers;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private ShapeFileMetaDataExtractor() {
		projectionName = "";
		shapeFileFieldNames = new ArrayList<String>();
		shapeFilePreviewData = new String[0][0];
		totalAreaIdentifiers = -1;
	}

	public static ShapeFileMetaDataExtractor newInstance() {
		ShapeFileMetaDataExtractor shapeFileMetaData = new ShapeFileMetaDataExtractor();
		
		return shapeFileMetaData;		
	}
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public int getTotalAreaIdentifiers() {
		return totalAreaIdentifiers;
	}
	
	public String getProjectionName() {
		return projectionName;
	}
		
	public String[][] getSampleData() {
		return shapeFilePreviewData;
	}
	
	public void extractMetaDataAndSampleDBFRows(final ShapeFile shapeFile)
		throws RIFServiceException {
		extractMetaDataFromPRJFile(shapeFile);
		extractMetaDataFromDBFFile(shapeFile);
	}
	
	private void extractMetaDataFromPRJFile(final ShapeFile shapeFile) 
		throws RIFServiceException {
		
		String shapeFilePRJPath 
			= shapeFile.getShapeFileComponentPath(ShapeFileComponent.PRJ);
		File shapeFilePRJFile = new File(shapeFilePRJPath); 
		BufferedReader reader = null;
		try {
			
			reader
				= new BufferedReader(new FileReader(shapeFilePRJFile));

			//It's a short file.  Read it all in
			StringBuilder prjText = new StringBuilder();
			String readLine = reader.readLine();
			while (readLine != null) {
				prjText.append(readLine);
				readLine = reader.readLine();
			}		
	
			//We are scanning for the first occurrence of PROJCS[" or GEOGCS["
			//and capture the phrase the ends just before the closing ""
			//eg1: in a UK file the first line might be:
			//PROJCS["OSGB_1936_British_National_Grid",GEOGCS["GCS_OSGB 
			//and here we want to get OSGB_1936_British_National_Grid
			//
			//eg2: In a US file the first line might be:
			//GEOGCS["GCS_North_American_1983",DATUM["D_North_American_
			//and here we want to get GCS_North_American_1983
			//
			//eg3: In Canada, a file might start with
			//GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_
			//and here we want to get GCS_WGS_1984
			projectionName 
				= extractProjectionName(
					shapeFilePRJFile.getName(), 
					prjText.toString());		
		}
		catch(IOException ioException) {
			ioException.printStackTrace(System.out);
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileReadingProblemException(shapeFilePRJFile.getName());
		}
		finally {
			if (reader != null) {
				try {
					reader.close();					
				}
				catch(IOException ioException) {
					RIFServiceExceptionFactory exceptionFactory
						= new RIFServiceExceptionFactory();
					throw exceptionFactory.createFileReadingProblemException(shapeFilePRJFile.getName());					
				}
			}
		}		
	}

	private String extractProjectionName(
		final String fileName,
		final String prjText) 
		throws RIFServiceException {
		
		//eg: PROJCS["OSGB_1936_British_National_Grid",GEOGCS["GCS_OSGB 
		String relevantText = prjText;
		int firstOpenBracketQuoteOccurrence
			= prjText.toString().indexOf("[\"");
		if (firstOpenBracketQuoteOccurrence == -1) {
			//not found.  perhaps a malformed prj file
			RIFServiceExceptionFactory factory
				= new RIFServiceExceptionFactory();
			throw factory.createFileReadingProblemException(fileName);
		}

		//eg: OSGB_1936_British_National_Grid",GEOGCS["GCS_OSGB 
		relevantText 
			= relevantText.substring(firstOpenBracketQuoteOccurrence + 2);
		int nextQuoteOccurrence = relevantText.indexOf("\"");
		if (nextQuoteOccurrence == -1) {
			RIFServiceExceptionFactory factory
				= new RIFServiceExceptionFactory();
			throw factory.createFileReadingProblemException(fileName);
		}
		relevantText = relevantText.substring(0, nextQuoteOccurrence);
			
		return relevantText;
	}
	
	private void extractMetaDataFromDBFFile(final ShapeFile shapeFile) 
		throws RIFServiceException {

		String shapeFileComponentPath
			= shapeFile.getShapeFileComponentPath(ShapeFileComponent.DBF);
		try {		
			FileInputStream fileInputStream
				= new FileInputStream(shapeFileComponentPath);
			FileChannel in 
				= fileInputStream.getChannel();		
			DbaseFileReader dbfFileReader
				= new DbaseFileReader(in, false, Charset.forName("ISO-8859-1"));
		
			//extract the number of DBF field names
			shapeFileFieldNames = new ArrayList<String>();
			DbaseFileHeader dbfFileHeader = dbfFileReader.getHeader();
			int numberOfFields = dbfFileHeader.getNumFields();
			for (int i = 0; i < numberOfFields; i++) {
				shapeFileFieldNames.add(dbfFileHeader.getFieldName(i));
			}

			//Get the number of records
			totalAreaIdentifiers = dbfFileHeader.getNumRecords();	
			fileInputStream.close();
			in.close();
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			RIFServiceExceptionFactory rifServiceExceptionFactory
				= new RIFServiceExceptionFactory();
			rifServiceExceptionFactory.createFileReadingProblemException(shapeFileComponentPath);
		}
	}
	
	public void extractSampleDBFRows(final ShapeFile shapeFile) 
		throws RIFServiceException {
		
		String shapeFileComponentPath
			= shapeFile.getShapeFileComponentPath(ShapeFileComponent.DBF);
		try {
			FileInputStream fileInputStream
				= new FileInputStream(shapeFileComponentPath);
			FileChannel in 
				= fileInputStream.getChannel();		
			DbaseFileReader dbfFileReader
				= new DbaseFileReader(in, false, Charset.forName("ISO-8859-1"));
						
			//extract the number of DBF field names
			shapeFileFieldNames = new ArrayList<String>();
			DbaseFileHeader dbfFileHeader = dbfFileReader.getHeader();
			int numberOfFields = dbfFileHeader.getNumFields();
			for (int i = 0; i < numberOfFields; i++) {
				shapeFileFieldNames.add(dbfFileHeader.getFieldName(i));
			}

			//Get the number of records
			totalAreaIdentifiers = dbfFileHeader.getNumRecords();
				
			//Get some preview rows
		
			int numberOfSampleRows = 5;
			if (numberOfSampleRows > totalAreaIdentifiers) {
				numberOfSampleRows = totalAreaIdentifiers;
			}
		
			int ithSampleRow = 0;
			shapeFilePreviewData = new String[numberOfSampleRows][numberOfFields];
			while (dbfFileReader.hasNext() && ithSampleRow < numberOfSampleRows) {
				Object[] currentRow = new Object[numberOfFields];
				dbfFileReader.readEntry(currentRow);
			
				DbaseFileReader.Row rowReader = dbfFileReader.readRow();
				for (int i = 0; i < numberOfFields; i++) {
					//shapeFilePreviewData[ithSampleRow][i] = (String) rowReader.read(i);

					shapeFilePreviewData[ithSampleRow][i] = String.valueOf(rowReader.read(i));
				}
				ithSampleRow++;
			}
			
			in.close();
			fileInputStream.close();
			dbfFileReader.close();
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileReadingProblemException(shapeFileComponentPath);
		}
	}
	
	public ArrayList<String> getShapeFileFieldNames() {
		return shapeFileFieldNames;
	}

	public void setShapeFileFieldNames(ArrayList<String> shapeFileFieldNames) {
		this.shapeFileFieldNames = shapeFileFieldNames;
	}

	public String[][] getShapeFilePreviewData() {
		return shapeFilePreviewData;
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


