package rifServices.dataStorageLayer.pg;

import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;
import rifServices.fileFormats.RIFZipFileWriter;
import rifServices.businessConceptLayer.RIFOutputOption;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.businessConceptLayer.User;



import java.nio.file.*;
import java.sql.*;
import java.io.*;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

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

public class SQLPublishResultsSubmissionStep {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private static final String RESULTS_DIRECTORY = "results";

	private File scratchSpaceDirectory;
	private File extraDirectoryForExtractFiles;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public SQLPublishResultsSubmissionStep() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void initialise(
		final File scratchSpaceDirectory,
		final File extraDirectoryForExtractFiles) {
		
		this.scratchSpaceDirectory = scratchSpaceDirectory;
		this.extraDirectoryForExtractFiles = extraDirectoryForExtractFiles;		
	}
	
	
	public void performStep(			
		final Connection connection,
		final User user,
		final RIFStudySubmission rifStudySubmission,
		final String studyID)
		throws RIFServiceException {
			
		//This will be used for name of the directory and the names of the
		//the extract, query and map files
		String baseFileName
			= rifStudySubmission.getStudy().getName();
		baseFileName = baseFileName.replaceAll("\\s", "");

		String zipFileDirectoryName
			= getZipFileName(
				scratchSpaceDirectory, 
				baseFileName);
		File zipFile = new File(zipFileDirectoryName);
		
		try {			
			//export the extract file
			String extractTableName = deriveExtractTableName(studyID);
			String extractCSVFileName 
				= deriveExtractCSVFileName(
					scratchSpaceDirectory, 
					baseFileName);
			exportTableToFile(
				connection,
				extractTableName, 
				extractCSVFileName);
			
			//export the map file
			String mapTableName = deriveMapTableName(studyID);
			String mapCSVFileName 
				= deriveMapCSVFileName(
					scratchSpaceDirectory, 
					baseFileName);
			exportTableToFile(
				connection,
				mapTableName, 
				mapCSVFileName);
			
			RIFZipFileWriter zipFileWriter = new RIFZipFileWriter();
			zipFileWriter.setExtraDirectoryForExtractFiles(extraDirectoryForExtractFiles);
			
			File extractFile = new File(extractCSVFileName);
			zipFileWriter.addOutputFileToInclude(
				RIFOutputOption.DATA,
				extractFile);
			
			File mapFile = new File(mapCSVFileName);
			zipFileWriter.addOutputFileToInclude(
				RIFOutputOption.DATA,
				mapFile);
			
			zipFileWriter.writeZipFile(
				user, 
				zipFile, 
				rifStudySubmission);
			
			//clean up
			extractFile.delete();
			mapFile.delete();
			
			
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudySubmissionFileExportService.error.unableToWriteZipFile",
					studyID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.UNABLE_TO_WRITE_STUDY_ZIP_FILE, 
					errorMessage);
			throw rifServiceException;
		}
		
	}
	
	private String getZipFileName(
		final File scratchDirectory,
		final String baseFileName) {
		
		StringBuilder zipFileName = new StringBuilder();
		zipFileName.append(scratchDirectory.getAbsolutePath());
		zipFileName.append(File.separator);
		zipFileName.append(baseFileName);
		zipFileName.append("-");
		zipFileName.append(
			RIFGenericLibraryMessages.getDatePhrase(new Date(System.currentTimeMillis())));
		zipFileName.append(".zip");
		
		return zipFileName.toString();
	}
	
	private String deriveExtractTableName(
		final String studyID) {
		
		StringBuilder tableName = new StringBuilder();
		tableName.append("rif_studies.s");
		tableName.append(studyID);
		tableName.append("_extract");
		return tableName.toString();
	}

	private String deriveExtractCSVFileName(
		final File temporaryDirectory,
		final String baseFileName) {

		StringBuilder extractFileName = new StringBuilder();
		extractFileName.append(temporaryDirectory.getAbsolutePath());
		extractFileName.append(File.separator);
		extractFileName.append(baseFileName);
		extractFileName.append("_");
		extractFileName.append("extract.csv");

		return extractFileName.toString();
	}

	private String deriveMapTableName(
		final String studyID) {
		
		StringBuilder tableName = new StringBuilder();
		tableName.append("rif_studies.s");
		tableName.append(studyID);
		tableName.append("_map");
		return tableName.toString();
	}


	private String deriveMapCSVFileName(
		final File temporaryDirectory,
		final String baseFileName) {

		StringBuilder extractFileName = new StringBuilder();
		extractFileName.append(temporaryDirectory.getAbsolutePath());
		extractFileName.append(File.separator);
		extractFileName.append(baseFileName);
		extractFileName.append("_");
		extractFileName.append("map.csv");

		return extractFileName.toString();
	}
		
	private void exportTableToFile(
		final Connection connection,
		final String tableName, 
		final String csvFileName) 
		throws Exception {
		
		StringBuilder query = new StringBuilder();
		query.append("COPY ");
		query.append(tableName);
		query.append(" TO STDOUT WITH CSV HEADER");
		
		BufferedWriter fileWriter = null;		
		CopyManager copyManager = new CopyManager((BaseConnection) connection);		
		try {
			fileWriter 
				= new BufferedWriter(new FileWriter(csvFileName));
			copyManager.copyOut(
				query.toString(), 
				fileWriter);
		}
		finally {
			if (fileWriter != null) {
				fileWriter.close();				
			}			
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
