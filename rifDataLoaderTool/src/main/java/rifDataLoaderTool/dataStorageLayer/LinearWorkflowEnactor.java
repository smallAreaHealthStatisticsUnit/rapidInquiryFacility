package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.DataLoaderServiceAPI;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.LinearWorkflow;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaAreaPropertyManager;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.businessConceptLayer.WorkflowValidator;
import rifDataLoaderTool.fileFormats.RIFDataLoadingResultTheme;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;

import java.util.ArrayList;
import java.io.*;


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

	// ==========================================
	// Section Properties
	// ==========================================
	
	private File logFile;
	private File reportFile;
	private File linearWorkflowFile;
	private BufferedWriter logWriter;
	private BufferedWriter reportWriter;
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

	public void runWorkflow(
		final LinearWorkflow linearWorkflow) 
		throws RIFServiceException {

		runWorkflow(
			null, 
			null, 
			null,
			linearWorkflow);
	}
	
	public void runWorkflow(
		final File logOutputFile,
		final File reportFile,
		final File linearWorkflowFile,
		final LinearWorkflow linearWorkflow) 
		throws RIFServiceException {

		setLogFiles(logOutputFile, reportFile);
		this.linearWorkflowFile = linearWorkflowFile;
		
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
	}
	
	public void setLogFiles(
		final File logFile,
		final File reportFile)
		throws RIFServiceException {
		
		try {
			if (logFile != null) {
				this.logFile = logFile;
				logWriter = new BufferedWriter(new FileWriter(logFile));
			}
			if (reportFile != null) {
				this.reportFile = reportFile;
				reportWriter = new BufferedWriter(new FileWriter(reportFile));
			}
		}
		catch(IOException ioException) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DATABASE_QUERY_FAILED,
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
				logWriter, 
				dataSetConfiguration);
			
			dataLoaderService.addFileToDataSetResults(
				rifManager,
				logWriter,
				linearWorkflowFile,
				RIFDataLoadingResultTheme.AUDIT_TRAIL,
				dataSetConfiguration);
			
			dataLoaderService.extractConfiguration(
				rifManager, 
				logWriter,
				dataSetConfiguration);
			
			System.out.println("processWorkflowStep 3");
			
		}
		else if (currentWorkflowState == WorkflowState.CLEAN) { 
			dataLoaderService.cleanConfiguration(
				rifManager, 
				logWriter,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.CONVERT) { 
			dataLoaderService.convertConfiguration(
				rifManager, 
				logWriter,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.OPTIMISE) { 
			dataLoaderService.optimiseConfiguration(
				rifManager,
				logWriter,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.CHECK) { 
			dataLoaderService.checkConfiguration(
				rifManager,
				logWriter,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.PUBLISH) { 
			dataLoaderService.publishConfiguration(
				rifManager,
				logWriter,
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


