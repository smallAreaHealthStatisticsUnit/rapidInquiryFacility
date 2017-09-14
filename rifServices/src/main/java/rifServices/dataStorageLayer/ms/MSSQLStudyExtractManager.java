package rifServices.dataStorageLayer.ms;


import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.fileFormats.RIFStudySubmissionContentHandler;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.fileFormats.XMLCommentInjector;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFDateFormat;

import java.io.*;
import java.sql.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Date;



/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
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

public class MSSQLStudyExtractManager extends MSSQLAbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================
	private static String EXTRACT_DIRECTORY;
	private static final String STUDY_QUERY_SUBDIRECTORY = "study_query";
	private static final String STUDY_EXTRACT_SUBDIRECTORY = "study_extract";
	private static final String RATES_AND_RISKS_SUBDIRECTORY = "rates_and_risks";
	private static final String GEOGRAPHY_SUBDIRECTORY = "geography";
	//private static final String STATISTICAL_POSTPROCESSING_SUBDIRECTORY = "statistical_post_processing";
	
	//private static final String TERMS_CONDITIONS_SUBDIRECTORY = "terms_and_conditions";

	private static final int BASE_FILE_STUDY_NAME_LENGTH = 10;
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	//private File termsAndConditionsDirectory;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLStudyExtractManager(
		final RIFServiceStartupOptions rifServiceStartupOptions) {

		
		super(rifServiceStartupOptions.getRIFDatabaseProperties());
		
		EXTRACT_DIRECTORY = rifServiceStartupOptions.getExtractDirectory();
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void createStudyExtract(
			final Connection connection,
			final User user,
			final RIFStudySubmission rifStudySubmission,
			final String zoomLevel)
					throws RIFServiceException {

		//Validate parameters
		String temporaryDirectoryPath = null;
		File temporaryDirectory = null;
		try {
			//Establish the phrase that will be used to help name the main zip
			//file and data files within its directories
			String baseStudyName 
			= createBaseStudyFileName(rifStudySubmission);

			temporaryDirectoryPath = 
					createTemporaryDirectoryPath(
							user, 
							baseStudyName);
			temporaryDirectory = new File(temporaryDirectoryPath);

			File submissionZipFile 
			= createSubmissionZipFile(
					user,
					baseStudyName);
			ZipOutputStream submissionZipOutputStream 
			= new ZipOutputStream(new FileOutputStream(submissionZipFile));


			//write the study the user made when they first submitted their query
			writeQueryFile(
					submissionZipOutputStream,
					user,
					baseStudyName,
					rifStudySubmission);


			writeExtractFiles(
					connection,
					temporaryDirectoryPath,
					submissionZipOutputStream,
					baseStudyName,
					rifStudySubmission);


			writeRatesAndRisksFiles(
					connection,
					temporaryDirectoryPath,
					submissionZipOutputStream,
					baseStudyName,
					rifStudySubmission);

			writeGeographyFiles(
					connection,
					temporaryDirectoryPath,
					submissionZipOutputStream,
					baseStudyName,
					zoomLevel,
					rifStudySubmission);

			/*
			writeStatisticalPostProcessingFiles(
				connection,
				temporaryDirectoryPath,
				submissionZipOutputStream,				
				baseStudyName,
				rifStudySubmission);

			writeTermsAndConditionsFiles(
				submissionZipOutputStream);	
			 */	
			submissionZipOutputStream.flush();
			submissionZipOutputStream.close();
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), "MSSQLStudyExtractManager ERROR", exception);
		}
		finally {
			temporaryDirectory.delete();
		}
	}

	private File createSubmissionZipFile(
		final User user,
		final String baseStudyName) {

		StringBuilder fileName = new StringBuilder();
		fileName.append(EXTRACT_DIRECTORY);
		fileName.append(File.separator);
		fileName.append(user.getUserID());		
		fileName.append("-");
		fileName.append(baseStudyName);
		fileName.append("-");
		
		RIFDateFormat rifDateFormat = RIFDateFormat.getRIFDateFormat();
		String timeStamp = rifDateFormat.getFileTimeStamp(new Date());
		if (timeStamp != null) {
			fileName.append(timeStamp);
		}		
		fileName.append(".rifZ");
		
		return new File(fileName.toString());		
	}

	
	/*
	 * Produces the base name for result files.
	 */
	private String createBaseStudyFileName(
		final RIFStudySubmission rifStudySubmission) {
		
		AbstractStudy study = rifStudySubmission.getStudy();
		String name = study.getName().toLowerCase();
		//concatenate study name length.  We need to be mindful about
		//the length of file names we produce so that they are not too
		//long for some operating systems to handle.
		
		if (name.length() > BASE_FILE_STUDY_NAME_LENGTH) {
			name = name.substring(0, BASE_FILE_STUDY_NAME_LENGTH);
		}
		
		
		//replace any spaces with underscores
		name = name.replaceAll(" ", "_");
		
		return name;
	}
	
	private String createTemporaryDirectoryPath(
		final User user,
		final String baseStudyName) {
		
		StringBuilder fileName = new StringBuilder();
		fileName.append(EXTRACT_DIRECTORY);
		fileName.append(File.separator);
		fileName.append(baseStudyName);
	
		return fileName.toString();
	}
	
	
	
	
	private void writeQueryFile(
		final ZipOutputStream submissionZipOutputStream,
		final User user,
		final String baseStudyName,
		final RIFStudySubmission rifStudySubmission)
		throws Exception {
		
		XMLCommentInjector commentInjector = new XMLCommentInjector();
		RIFStudySubmissionContentHandler rifStudySubmissionContentHandler
			= new RIFStudySubmissionContentHandler();
		rifStudySubmissionContentHandler.initialise(
			submissionZipOutputStream, 
			commentInjector);
	
		//KLG @TODO.  Right now we have only 
		
		//write the query file to a special directory.
		//this folder should only contain one file
		StringBuilder queryFileName = new StringBuilder();
		queryFileName.append(STUDY_QUERY_SUBDIRECTORY);
		queryFileName.append(File.separator);
		queryFileName.append(baseStudyName);
		queryFileName.append("_query.xml");
		
		ZipEntry rifQueryFileNameZipEntry = new ZipEntry(queryFileName.toString());
		submissionZipOutputStream.putNextEntry(rifQueryFileNameZipEntry);
		rifStudySubmissionContentHandler.writeXML(
			user, 
			rifStudySubmission);
		submissionZipOutputStream.closeEntry();
		
		
		
	}
	
	
	private void writeExtractFiles(
			final Connection connection,
			final String temporaryDirectoryPath,
			final ZipOutputStream submissionZipOutputStream,
			final String baseStudyName,
			final RIFStudySubmission rifStudySubmission)
					throws Exception {

		//Add extract file to zip file
		StringBuilder extractTableName = new StringBuilder();

		extractTableName.append("s");
		extractTableName.append(rifStudySubmission.getStudyID());
		extractTableName.append("_extract");

		StringBuilder extractFileName = new StringBuilder();
		extractFileName.append(STUDY_EXTRACT_SUBDIRECTORY);
		extractFileName.append(File.separator);
		extractFileName.append(baseStudyName);
		extractFileName.append(".csv");

		dumpDatabaseTableToCSVFile(
				connection,
				submissionZipOutputStream,
				extractTableName.toString(),
				extractFileName.toString());

		/* IG NOT YET INCLUDED
		File infoGovernanceDirectory
			= new File("C:" + File.separator + "rif_test_data" + File.separator + "information_governance");
		File[] files = infoGovernanceDirectory.listFiles();

		for (File file : files) { 

			StringBuilder zipEntryName = new StringBuilder();
			zipEntryName.append(TERMS_CONDITIONS_SUBDIRECTORY);
			zipEntryName.append(File.separator);
			zipEntryName.append(file.getName());

			addFileToZipFile(
				submissionZipOutputStream,
				zipEntryName.toString(),
				file);

		}
		 */
		
	}
	
	private void writeRatesAndRisksFiles(
			final Connection connection,
			final String temporaryDirectoryPath,
			final ZipOutputStream submissionZipOutputStream,
			final String baseStudyName,
			final RIFStudySubmission rifStudySubmission)
					throws Exception {

		//Add extract file to zip file
		StringBuilder mapTableName = new StringBuilder();

		mapTableName.append("s");
		mapTableName.append(rifStudySubmission.getStudyID());
		mapTableName.append("_map");

		StringBuilder mapFileName = new StringBuilder();
		mapFileName.append(RATES_AND_RISKS_SUBDIRECTORY);
		mapFileName.append(File.separator);
		mapFileName.append(baseStudyName);
		mapFileName.append(".csv");

		dumpDatabaseTableToCSVFile(
				connection,
				submissionZipOutputStream,
				mapTableName.toString(),
				mapFileName.toString());

	}	
	
	private void writeGeographyFiles(
			final Connection connection,
			final String temporaryDirectoryPath,
			final ZipOutputStream submissionZipOutputStream,
			final String baseStudyName,
			final String zoomLevel,
			final RIFStudySubmission rifStudySubmission)
					throws Exception {
		
		String studyID = rifStudySubmission.getStudyID();
	
		//Add geographies to zip file
		StringBuilder tileTableName = new StringBuilder();	
		tileTableName.append("rif_data.geometry_");
		String geog = rifStudySubmission.getStudy().getGeography().getName();			
		tileTableName.append(geog);
						
		StringBuilder tileFilePath = new StringBuilder();
		tileFilePath.append(GEOGRAPHY_SUBDIRECTORY);
		tileFilePath.append(File.separator);
		tileFilePath.append(baseStudyName);
		
		//Write study area
		StringBuilder tileFileName = null;
		tileFileName = new StringBuilder();
		tileFileName.append(tileFilePath.toString());
		tileFileName.append("_studyArea");
		tileFileName.append(".txt");
		
		writeMapQueryTogeoJSONFile(
				connection,
				submissionZipOutputStream,
				"rif40_study_areas",
				tileTableName.toString(),
				tileFileName.toString(),
				zoomLevel,
				studyID);
		
		//Write comparison area
		tileFileName = new StringBuilder();
		tileFileName.append(tileFilePath.toString());
		tileFileName.append("_comparisonArea");
		tileFileName.append(".txt");
		
		writeMapQueryTogeoJSONFile(
				connection,
				submissionZipOutputStream,
				"rif40_comparison_areas",
				tileTableName.toString(),
				tileFileName.toString(),
				zoomLevel,
				studyID);
	}	

	
	/*
	private void writeStatisticalPostProcessingFiles(
		final Connection connection,
		final String temporaryDirectoryPath,		
		final ZipOutputStream submissionZipOutputStream,
		final String baseStudyName,
		final RIFStudySubmission rifStudySubmission)
		throws Exception {
				
		
		ArrayList<CalculationMethod> calculationMethods
			= rifStudySubmission.getCalculationMethods();
		for (CalculationMethod calculationMethod : calculationMethods) {
			
			StringBuilder postProcessedTableName = new StringBuilder();			
			postProcessedTableName.append("s");
			postProcessedTableName.append(rifStudySubmission.getStudyID());
			postProcessedTableName.append("_");
			postProcessedTableName.append(calculationMethod.getName());
			
			StringBuilder postProcessedFileName = new StringBuilder();
			postProcessedFileName.append(temporaryDirectoryPath);
			postProcessedFileName.append(File.separator);
			postProcessedFileName.append(baseStudyName);
			postProcessedFileName.append("_");
			postProcessedFileName.append(calculationMethod.getName());
			postProcessedFileName.append(".csv");
			
			File postProcessedFile = new File(postProcessedFileName.toString());
			addFileToZipFile(
				submissionZipOutputStream, 
				STATISTICAL_POSTPROCESSING_SUBDIRECTORY, 
				postProcessedFile);
		}
	}	
	*/
	
	/*
	private void writeTermsAndConditionsFiles(
		final ZipOutputStream submissionZipOutputStream) 
		throws Exception {
		
		File[] files = termsAndConditionsDirectory.listFiles();
		for (File file : files) {
			addFileToZipFile(
				submissionZipOutputStream, 
				TERMS_CONDITIONS_SUBDIRECTORY,
				file);			
		}		
	}
	*/

	
	/*
	 * General methods for writing to zip files
	 */

	public void addFileToZipFile(
		final ZipOutputStream submissionZipOutputStream,
		final String zipEntryName,
		final File inputFile)
		throws Exception {
		
		ZipEntry rifQueryFileNameZipEntry = new ZipEntry(zipEntryName);
		submissionZipOutputStream.putNextEntry(rifQueryFileNameZipEntry);
				
		byte[] BUFFER = new byte[4096 * 1024];
		FileInputStream fileInputStream = new FileInputStream(inputFile);		
		int bytesRead = fileInputStream.read(BUFFER);		
		while (bytesRead != -1) {
			submissionZipOutputStream.write(BUFFER, 0, bytesRead);			
			bytesRead = fileInputStream.read(BUFFER);
		}
		submissionZipOutputStream.flush();
		fileInputStream.close();
		submissionZipOutputStream.closeEntry();
	}

	public void dumpDatabaseTableToCSVFile(
		final Connection connection,
		final ZipOutputStream submissionZipOutputStream,		
		final String tableName,
		final String outputFilePath)
		throws Exception {
				
		PGSQLFunctionCallerQueryFormatter queryFormatter = new PGSQLFunctionCallerQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40_dmp_pkg");
		queryFormatter.setFunctionName("csv_dump");
		queryFormatter.setNumberOfFunctionParameters(1);
		
		
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(submissionZipOutputStream);
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
		
		PreparedStatement statement
			= createPreparedStatement(connection, queryFormatter);		
		ResultSet resultSet = null;
		try {
			statement = createPreparedStatement(connection, queryFormatter);
			statement.setString(1, tableName);
			resultSet = statement.executeQuery();
			
			ZipEntry zipEntry = new ZipEntry(outputFilePath);
			submissionZipOutputStream.putNextEntry(zipEntry);
			
			while (resultSet.next()) {
				bufferedWriter.write(resultSet.getString(1));
			}

			bufferedWriter.flush();
			submissionZipOutputStream.closeEntry();
			
			connection.commit();
		}
		finally {
			PGSQLQueryUtility.close(statement);
		}

	}
		

	
	/*
    private void addFileToZipFile(
    	final ZipOutputStream submissionZipOutputStream, 
    	final String zipFilePath, 
    	final File file) throws Exception {
    	
    	
    	if (file.isDirectory()) {
    		addDirectoryToZipFile(
    		submissionZipOutputStream,
    		zipFilePath,
    		file); 
    		return;
    	}
    	
    	//assume file is not a directory
    	ZipEntry zipEntry
    		= createZipEntry(
    			zipFilePath,
    			file);
    	submissionZipOutputStream.putNextEntry(zipEntry);

        FileInputStream fileInputStream 
        	= new FileInputStream(file);
        byte[] buffer = new byte[4092];
        int byteCount = 0;
        while ((byteCount = fileInputStream.read(buffer)) != -1)
        {
        	submissionZipOutputStream.write(buffer, 0, byteCount);
            rifLogger.info(this.getClass(), '.');
        }

        fileInputStream.close();
        submissionZipOutputStream.closeEntry();
    }
    
    	*/
	
	public void writeMapQueryTogeoJSONFile(
			final Connection connection,
			final ZipOutputStream submissionZipOutputStream,	
			final String areaTableName,
			final String tableName,
			final String outputFilePath,
			final String zoomLevel,
			final String studyID)
					throws Exception {
		
		//Type of area
		String type = "S";
		
		//get geolevel
		SQLGeneralQueryFormatter geolevelQueryFormatter = new SQLGeneralQueryFormatter();	
		geolevelQueryFormatter.addQueryLine(0, "SELECT b.geolevel_id");
		geolevelQueryFormatter.addQueryLine(0, "FROM rif40_studies a, rif40_geolevels b");
		geolevelQueryFormatter.addQueryLine(0, "WHERE study_id = ?");
		if (areaTableName.equals("rif40_comparison_areas")) {
			geolevelQueryFormatter.addQueryLine(0, "AND a.comparison_geolevel_name = b.geolevel_name");
			type = "C";
		} else {
			geolevelQueryFormatter.addQueryLine(0, "AND a.study_geolevel_name = b.geolevel_name");
		}
	
		//count areas
		SQLGeneralQueryFormatter countQueryFormatter = new SQLGeneralQueryFormatter();
		countQueryFormatter.addQueryLine(0, "SELECT count(area_id) from rif40." + areaTableName + " where study_id = ?");
		
		//TODO: possible issues with Multi-polygon and point arrays
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "SELECT b.areaid, b.zoomlevel, b.wkt from (select area_id from rif40." + areaTableName + " where study_id = ?) a");
		queryFormatter.addQueryLine(0, "left join " + tableName + " b ");
		queryFormatter.addQueryLine(0, "on a.area_id = b.areaid");
		queryFormatter.addQueryLine(0, "WHERE geolevel_id = ? AND zoomlevel = ?");
		
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(submissionZipOutputStream);
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
		
		PreparedStatement geolevelStatement = createPreparedStatement(connection, geolevelQueryFormatter);		
		ResultSet geolevelResultSet = null;
		PreparedStatement countStatement = createPreparedStatement(connection, countQueryFormatter);		
		ResultSet countResultSet = null;
		PreparedStatement statement = createPreparedStatement(connection, queryFormatter);		
		ResultSet resultSet = null;
		
		try {
			geolevelStatement = createPreparedStatement(connection, geolevelQueryFormatter);
			geolevelStatement.setInt(1, Integer.parseInt(studyID));	
			geolevelResultSet = geolevelStatement.executeQuery();
			geolevelResultSet.next();
			Integer geolevel = geolevelResultSet.getInt(1);
			
			
			countStatement = createPreparedStatement(connection, countQueryFormatter);
			countStatement.setInt(1, Integer.parseInt(studyID));	
			countResultSet = countStatement.executeQuery();
			countResultSet.next();
			int rows = countResultSet.getInt(1);

			statement = createPreparedStatement(connection, queryFormatter);
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, geolevel);
			statement.setInt(3, Integer.parseInt(zoomLevel));
						
			resultSet = statement.executeQuery();

			ZipEntry zipEntry = new ZipEntry(outputFilePath);
			submissionZipOutputStream.putNextEntry(zipEntry);
			
			//Write WKT to geoJSON
			int i = 0;
			bufferedWriter.write("{ \"type\": \"FeatureCollection\", \"features\": [\r\n");	
			while (resultSet.next()) {
				bufferedWriter.write("{ \"type\": \"Feature\",\r\n");
				bufferedWriter.write("\"geometry\": {\r\n\"type\": \"Polygon\",\r\n\"coordinates\": [");
				bufferedWriter.write("[\r\n");
				//Full wkt string
				String polygon = resultSet.getString(3);				
				//trim head and tail
				polygon = polygon.replaceAll("MULTIPOLYGON", "");
				polygon = polygon.replaceAll("[()]", "");				
				//get coordinate pairs
				String[] coords = polygon.split(",");
				for (Integer j = 0; j < coords.length; j++) {
					String node = coords[j].replaceFirst(" ", ",");
					bufferedWriter.write("[" + node + "]");		
					if (j != coords.length - 1) {
						bufferedWriter.write(",");	
					}
				}				
				//get properties
				bufferedWriter.write("]\r\n");					
				bufferedWriter.write("]},\r\n\"properties\": {\r\n");
				bufferedWriter.write("\"area_id\": \"" + resultSet.getString(1) + "\",\r\n");
				bufferedWriter.write("\"zoomLevel\": \"" + resultSet.getString(2) + "\",\r\n");
				bufferedWriter.write("\"areatype\": \"" + type + "\"\r\n");
				bufferedWriter.write("}\r\n");
				bufferedWriter.write("}");
				if (i != rows) {
					bufferedWriter.write(","); 
				}
				bufferedWriter.write("\r\n");
				i++;
			}
			
			bufferedWriter.write("]\r\n");
			bufferedWriter.write("}");

			bufferedWriter.flush();
			submissionZipOutputStream.closeEntry();

			connection.commit();
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(countStatement);
			PGSQLQueryUtility.close(geolevelStatement);
		}
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
