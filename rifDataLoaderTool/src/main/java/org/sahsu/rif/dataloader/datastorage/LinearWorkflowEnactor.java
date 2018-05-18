package org.sahsu.rif.dataloader.datastorage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import org.sahsu.rif.dataloader.concepts.DataLoaderServiceAPI;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataLoadingResultTheme;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.LinearWorkflow;
import org.sahsu.rif.dataloader.concepts.RIFSchemaAreaPropertyManager;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.dataloader.concepts.WorkflowValidator;
import org.sahsu.rif.dataloader.scriptgenerator.ms.MSDataLoadingScriptGenerator;
import org.sahsu.rif.dataloader.scriptgenerator.pg.PGDataLoadingScriptGenerator;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;

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

public class LinearWorkflowEnactor {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private File exportDirectory;
	
	private File runDirectory;
	private File logFile;
	private File linearWorkflowFile;
	private BufferedWriter logWriter;
	private RIFSchemaAreaPropertyManager schemaAreaPropertyManager;
	private WorkflowValidator workflowValidator;
	
	private User rifManager;
	private DataLoaderServiceAPI dataLoaderService;
	
	// ==========================================
	// Section Construction
	// ==========================================

	
	public LinearWorkflowEnactor(
		final User rifManager,
		final DataLoaderServiceAPI dataLoaderService) {

		this.rifManager = rifManager;
		this.dataLoaderService = dataLoaderService;		
		
		schemaAreaPropertyManager
			= new RIFSchemaAreaPropertyManager();	
		
		workflowValidator
			= new WorkflowValidator(schemaAreaPropertyManager);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setExportDirectory(final File exportDirectory) {
		
		this.exportDirectory = exportDirectory;
	}
	
	public void runWorkflow(
		final File exportDirectory,
		final DataLoaderToolConfiguration dataLoaderToolConfiguration,
		final LinearWorkflow linearWorkflow)
		throws RIFServiceException {

		//Create a temporary directory where all the outputs will be created.
		initialiseJobRunEnvironment(exportDirectory);
				
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= linearWorkflow.getDataSetConfigurations();		

		workflowValidator.validateLinearWorkflow(linearWorkflow);

		//run the workflow from start to finish for each of the
		//data set configurations
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			processDataSetConfiguration(
				dataSetConfiguration,
				linearWorkflow);
	
			String finishedProcessingDataSetMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowEnactor.finishedProcessingDataSet",
					dataSetConfiguration.getDisplayName());
			logMessage(finishedProcessingDataSetMessage);
		}
		System.out.println("Finished workflow!");
		
		//Now generate a single script

		PGDataLoadingScriptGenerator pgScriptGenerator
			= new PGDataLoadingScriptGenerator();
		
		pgScriptGenerator.writeScript(
			runDirectory, 
			dataLoaderToolConfiguration);
		
		MSDataLoadingScriptGenerator msScriptGenerator
			= new MSDataLoadingScriptGenerator();
		
		msScriptGenerator.writeScript(
			runDirectory, 
			dataLoaderToolConfiguration);
		
		
		
	}

	private void initialiseJobRunEnvironment(final File exportDirectory) 
		throws RIFServiceException {
		
		try {
			
			StringBuilder runDirectoryPath = new StringBuilder();
			runDirectoryPath.append(exportDirectory.getAbsolutePath());
			runDirectoryPath.append(File.separator);
			runDirectoryPath.append("run_");
			String timeStamp 
				= GENERIC_MESSAGES.getTimeStampForFileName(new Date());
			runDirectoryPath.append(timeStamp);
			runDirectory = new File(runDirectoryPath.toString());

			Path path = Paths.get(runDirectoryPath.toString());
			if (!Files.exists(path)) {
				try {
					Files.createDirectories(path);
				} catch (IOException ioException) {
					//fail to create directory
					ioException.printStackTrace();
				}
			}
		
			//initialise the log file, which will appear within the run directory
			StringBuilder logFilePath = new StringBuilder();
			logFilePath.append(runDirectoryPath.toString());
			logFilePath.append(File.separator);
			logFilePath.append("QueryLog_");
			logFilePath.append(timeStamp);
			logFilePath.append(".txt");
			logFile = new File(logFilePath.toString());     
			logWriter = new BufferedWriter(new FileWriter(logFile));
		}
		catch(IOException ioException) {
			ioException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"linearWorkflow.error.unableToInitialiseRunEnvironment");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.CANNOT_INITIALISE_RUN_ENVIRONMENT,
					errorMessage);
			throw rifServiceException;
		}
	}
		
	private void processDataSetConfiguration(
		final DataSetConfiguration dataSetConfiguration,
		final LinearWorkflow linearWorkflow) 
		throws RIFServiceException {

		linearWorkflow.resetWorkflow();
		
		while (linearWorkflow.next()) {			
			processWorkflowStep(
				linearWorkflow,
				dataSetConfiguration,
				linearWorkflow.getCurrentWorkflowState());
		}

	}
	
	private void processWorkflowStep(
		final LinearWorkflow linearWorkflow,
		final DataSetConfiguration dataSetConfiguration,
		final WorkflowState currentWorkflowState)
		throws RIFServiceException {
		
		if (currentWorkflowState == WorkflowState.EXTRACT) {
			
			dataLoaderService.setupConfiguration(
				rifManager,
				runDirectory,
				dataSetConfiguration);
			
			dataLoaderService.addFileToDataSetResults(
					rifManager,
					logWriter,
					runDirectory,
					linearWorkflowFile,
					DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL,
					dataSetConfiguration);
			
			dataLoaderService.extractConfiguration(
				rifManager, 
				logWriter,
				runDirectory,
				dataSetConfiguration);
			
		}
		else if (currentWorkflowState == WorkflowState.CLEAN) { 
			dataLoaderService.cleanConfiguration(
				rifManager, 
				logWriter,
				runDirectory,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.CONVERT) { 
			dataLoaderService.convertConfiguration(
				rifManager, 
				logWriter,
				runDirectory,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.OPTIMISE) { 
			dataLoaderService.optimiseConfiguration(
				rifManager,
				logWriter,
				runDirectory,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.CHECK) { 
			dataLoaderService.checkConfiguration(
				rifManager,
				logWriter,
				runDirectory,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.PUBLISH) { 			
			dataLoaderService.publishConfiguration(
				rifManager,
				logWriter,
				runDirectory,
				dataSetConfiguration);			
		}

	}
	
	private void logException(
		final RIFServiceException rifServiceException) {
		
	}
	
	private void logMessage(
		final String logMessage) 
		throws RIFServiceException {
		
		try {
			logWriter.write(logMessage);
			logWriter.newLine();
			logWriter.flush();
		}
		catch(IOException ioException) {
			ioException.printStackTrace(System.out);
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.unableToWriteToFile",
					logFile.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.UNABLE_TO_WRITE_FILE, 
					errorMessage);
			throw rifServiceException;
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


