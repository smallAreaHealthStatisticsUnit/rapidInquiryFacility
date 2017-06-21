package rifServices.dataStorageLayer.ms;


import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.fileFormats.RIFStudySubmissionContentHandler;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.util.FieldValidationUtility;
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
	private static final String STATISTICAL_POSTPROCESSING_SUBDIRECTORY = "statistical_post_processing";
	
	private static final String TERMS_CONDITIONS_SUBDIRECTORY = "terms_and_conditions";

	private static final int BASE_FILE_STUDY_NAME_LENGTH = 10;
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	private File termsAndConditionsDirectory;
	
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
		final RIFStudySubmission rifStudySubmission)
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
			exception.printStackTrace(System.out);
			
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
			final RIFStudySubmission rifStudySubmission)
					throws Exception {

		//Add extract file to zip file
		StringBuilder tileTableName = new StringBuilder();
		
		tileTableName.append("rif_data.geometry_");
		tileTableName.append("sahsuland"); //rifStudySubmission.getStudy().getGeography().toString()
		tileTableName.append("_geolevel_id_");
		tileTableName.append("3"); //TODO: needs variable
		tileTableName.append("_zoomlevel_");
		tileTableName.append("9"); //TODO: needs variable
		
		System.out.println(tileTableName);
		
		StringBuilder tileFileName = new StringBuilder();
		tileFileName.append(GEOGRAPHY_SUBDIRECTORY);
		tileFileName.append(File.separator);
		tileFileName.append(baseStudyName);
		tileFileName.append(".csv");

		//rifStudySubmission.getStudy().
		
		dumpMapDatabaseTableToCSVFile(
				connection,
				submissionZipOutputStream,
				tileTableName.toString(),
				tileFileName.toString());

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
            System.out.print('.');
            System.out.flush();
        }
        System.out.println();

        fileInputStream.close();
        submissionZipOutputStream.closeEntry();
    }
    
    	*/
	
	public void dumpMapDatabaseTableToCSVFile(
			final Connection connection,
			final ZipOutputStream submissionZipOutputStream,		
			final String tableName,
			final String outputFilePath)
					throws Exception {

	/*	PGSQLFunctionCallerQueryFormatter queryFormatter = new PGSQLFunctionCallerQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40_dmp_pkg");
		queryFormatter.setFunctionName("csv_dump");
		queryFormatter.setNumberOfFunctionParameters(1);

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(submissionZipOutputStream);
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

		PreparedStatement statement
		= createPreparedStatement(connection, queryFormatter);		
		ResultSet resultSet = null;*/
		
		//TODO: if contains then write line
		//return array of area_id x2
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();		

		queryFormatter.addQueryLine(1, "SELECT * from rif_data.geometry_sahsuland_geolevel_id_3_zoomlevel_9 limit 10");
		
		//TODO: permission denied
		
		PreparedStatement statement
		= createPreparedStatement(connection, queryFormatter);		
		ResultSet resultSet = null;
		
		
		//..
		
		
		try {
			statement = createPreparedStatement(connection, queryFormatter);
			//statement.setString(1, tableName);
			resultSet = statement.executeQuery();

			//ZipEntry zipEntry = new ZipEntry(outputFilePath);
			//submissionZipOutputStream.putNextEntry(zipEntry);

			while (resultSet.next()) {
				//bufferedWriter.write(resultSet.getString(1));
				System.out.println("resultSet.getString(2)");
				
			}

			//bufferedWriter.flush();
			//submissionZipOutputStream.closeEntry();

			connection.commit();
		}
		finally {
			PGSQLQueryUtility.close(statement);
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
