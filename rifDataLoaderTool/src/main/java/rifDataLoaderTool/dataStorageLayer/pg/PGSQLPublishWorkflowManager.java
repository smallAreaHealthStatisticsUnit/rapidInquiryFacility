package rifDataLoaderTool.dataStorageLayer.pg;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.fileFormats.PostgreSQLDataLoadingScriptWriter;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.system.RIFServiceException;

import org.apache.commons.io.FileUtils;

import java.sql.*;
import java.io.*;
import java.util.zip.*;
import java.util.Date;

/**
 *
 * Manages all database operations used to convert a cleaned table into tabular data
 * expected by some part of the RIF (eg: numerator data, health codes, geospatial data etc)
 * 
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

final class PGSQLPublishWorkflowManager 
	extends AbstractPGSQLDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================	

	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLPublishWorkflowManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void publishConfiguration(
		final Connection connection,
		final Writer logFileWriter,
		final String exportDirectoryPath,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
	
		//validate parameters
		dataSetConfiguration.checkErrors();
			
		String coreDataSetName
			= dataSetConfiguration.getName();
		String checkTableName
			= RIFTemporaryTablePrefixes.CHECK.getTableName(coreDataSetName);

		//Determine the prefix of the final destination table
		RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);
		deleteTable(
			connection, 
			logFileWriter, 
			publishTableName);
		renameTable(
			connection, 
			logFileWriter, 
			checkTableName, 
			publishTableName);
		
		
		
		/*
		 * Exporting Part I: Generate the archive data file 
		 */
		exportTable(
			connection, 
			logFileWriter, 
			exportDirectoryPath, 
			RIFDataLoadingResultTheme.ARCHIVE_RESULTS,
			publishTableName);
		
		//Generate the script that is supposed to be specific to SQL Server or PostgreSQL
		PostgreSQLDataLoadingScriptWriter scriptWriter
			= new PostgreSQLDataLoadingScriptWriter();
		File dataLoadingScriptFile
			= createDataLoadingScriptFileName(
				exportDirectoryPath, 
				RIFDataLoadingResultTheme.ARCHIVE_RESULTS,
				dataSetConfiguration);
		scriptWriter.writeFile(
			dataLoadingScriptFile, 
			dataSetConfiguration);		
		
		updateLastCompletedWorkState(
			connection,
			logFileWriter,
			dataSetConfiguration,
			WorkflowState.CHECK);
	}
	
	
	private File createDataLoadingScriptFileName(
		final String exportDirectoryPath,
		final RIFDataLoadingResultTheme dataLoadingResultTheme,
		final DataSetConfiguration dataSetConfiguration) {
		
		String coreDataSetName = dataSetConfiguration.getName();
		RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(exportDirectoryPath);
		buffer.append(File.separator);
		if (dataLoadingResultTheme != RIFDataLoadingResultTheme.MAIN_RESULTS) {
			buffer.append(dataLoadingResultTheme.getSubDirectoryName());
			buffer.append(File.separator);			
		}
		buffer.append("run_");
		buffer.append(publishTableName);
		buffer.append(".sql");
		
		return new File(buffer.toString());
	}
	
	
	public void establishTableAccessPrivileges(
		final Connection connection,
		final String coreDataSetName,
		final String rifRoleName) 
		throws SQLException,
		RIFServiceException {

		SQLGeneralQueryFormatter queryFormatter
			= new SQLGeneralQueryFormatter();
		
		//@TODO: fill in this query
			
		PreparedStatement statement = null;
					
		try {
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.executeUpdate();
		}
		finally {
			PGSQLQueryUtility.close(statement);			
		}
	}
	
	/**
	 * 
	 * @param mainExportDirectoryPath - the main directory where the output of jobs is sent
	 * @param dataSetConfiguration
	 * @throws RIFServiceException
	 */
	public void createZipArchiveFileAndCleanupTemporaryFiles(
		final Writer logFileWriter,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {

		
		try {			
			StringBuilder zipFileName = new StringBuilder();
			String coreDataSetName = dataSetConfiguration.getName();
			
			
			/*
			 * Get the directory where all the files for a given data load job have
			 * been stored.  eg: C:\loading_jobs\cancer_data_2001
			 */
			StringBuilder loadJobDirectoryPath = new StringBuilder();
			loadJobDirectoryPath.append(exportDirectory.getAbsolutePath());
			loadJobDirectoryPath.append(File.separator);
			loadJobDirectoryPath.append(coreDataSetName);
			File loadJobDirectory = new File(loadJobDirectoryPath.toString());


			/*
			 * Eg: num_cancer_data_2001_22042015.zip
			 */
			zipFileName.append(exportDirectory.getAbsolutePath());
			zipFileName.append(File.separator);			
			RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
			String publishTableName = rifSchemaArea.getPublishedTableName(coreDataSetName);
			zipFileName.append(publishTableName);
			zipFileName.append("_");
			String datePhrase
				= RIFGenericLibraryMessages.getTimeStampForFileName(new Date());
			zipFileName.append(datePhrase);
			zipFileName.append(".zip");
			
			FileOutputStream fileOutputStream = new FileOutputStream(zipFileName.toString());
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
					
			addDirectory(
				loadJobDirectory, 
				zipOutputStream);
			zipOutputStream.flush();
			zipOutputStream.close();
			
			//Make a copy of the main result file used to generate the archive
			//zip file and put it in the main export directory
			
			StringBuilder temporaryResultsDirectoryPath = new StringBuilder();
			temporaryResultsDirectoryPath.append(loadJobDirectoryPath.toString());
			temporaryResultsDirectoryPath.append(File.separator);
			temporaryResultsDirectoryPath.append(RIFDataLoadingResultTheme.ARCHIVE_RESULTS.getSubDirectoryName());
			File temporaryResultsDirectory 
				= new File(temporaryResultsDirectoryPath.toString());
			FileUtils.copyDirectory(temporaryResultsDirectory, exportDirectory);

			//Cleanup temporary directory used for zipping
			FileUtils.deleteDirectory(loadJobDirectory);			
		}
		catch(IOException ioException) {
			logException(
				logFileWriter, 
				ioException);
			String errorMessage 
				= RIFDataLoaderToolMessages.getMessage(
					"publishWorkflowManager.unableToZipResults",
					dataSetConfiguration.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.UNABLE_TO_ZIP_RESULTS, 
					errorMessage);
			throw rifServiceException;
		}
		
		
	}
	
	private void addDirectory(
		File directory, 
		ZipOutputStream outputStream) 
		throws IOException {
		
		File[] files = directory.listFiles();
	
		byte[] dataBuffer = new byte[1024];
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				addDirectory(files[i], outputStream);
			}
			else {
				FileInputStream inputStream = new FileInputStream(files[i].getAbsolutePath());

				StringBuilder outputDirectoryPath = new StringBuilder();
				outputDirectoryPath.append(directory.getName());
				outputDirectoryPath.append(File.separator);
				outputDirectoryPath.append(files[i].getName());
				//ZipEntry zipEntry = new ZipEntry(files[i].getAbsolutePath());
				ZipEntry zipEntry = new ZipEntry(outputDirectoryPath.toString());

				outputStream.putNextEntry(zipEntry);
				int length = inputStream.read(dataBuffer);
				while (length > 0) {
					outputStream.write(dataBuffer, 0, length);					
					length = inputStream.read(dataBuffer);
				}
				outputStream.closeEntry();
				inputStream.close();
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


