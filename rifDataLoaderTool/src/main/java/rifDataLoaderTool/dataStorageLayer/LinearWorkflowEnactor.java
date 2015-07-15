package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.DataLoaderServiceAPI;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.LinearWorkflow;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaAreaPropertyManager;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.businessConceptLayer.WorkflowValidator;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;

import java.util.ArrayList;


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

		ArrayList<DataSetConfiguration> dataSetConfigurations
			= linearWorkflow.getDataSetConfigurations();		

		workflowValidator.validateLinearWorkflow(linearWorkflow);

		//run the workflow from start to finish for each of the
		//data set configurations
		try {			
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
		catch(RIFServiceException rifServiceException) {
			logException(rifServiceException);
		}			

	}
	
	private void processDataSetConfiguration(
		final DataSetConfiguration dataSetConfiguration,
		final LinearWorkflow linearWorkflow) 
		throws RIFServiceException {
		
		linearWorkflow.resetWorkflow();
				
		while (linearWorkflow.next()) {			
			processWorkflowStep(
				dataSetConfiguration,
				linearWorkflow.getCurrentWorkflowState());
		}

	}
	
	private void processWorkflowStep(
		final DataSetConfiguration dataSetConfiguration,
		final WorkflowState currentWorkflowState) 
		throws RIFServiceException {
		
		if (currentWorkflowState == WorkflowState.LOAD) {
			System.out.println("processWorkflowStep LOAD for " + 
				dataSetConfiguration.getDisplayName()+"==");
			dataLoaderService.loadConfiguration(
				rifManager, 
				dataSetConfiguration);
		}
		else if (currentWorkflowState == WorkflowState.CLEAN) { 
			System.out.println("processWorkflowStep CLEAN for " + 
					dataSetConfiguration.getDisplayName()+"==");
			dataLoaderService.cleanConfiguration(
				rifManager, 
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.CONVERT) { 
			System.out.println("processWorkflowStep CONVERT for " + 
				dataSetConfiguration.getDisplayName()+"==");
			dataLoaderService.convertConfiguration(
				rifManager, 
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.OPTIMISE) { 
			System.out.println("processWorkflowStep OPTIMISE for " + 
				dataSetConfiguration.getDisplayName()+"==");
			dataLoaderService.optimiseConfiguration(
				rifManager,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.CHECK) { 
			System.out.println("processWorkflowStep CHECK for " + 
				dataSetConfiguration.getDisplayName()+"==");
			dataLoaderService.checkConfiguration(
				rifManager,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.PUBLISH) { 
			System.out.println("processWorkflowStep PUBLISH for " + 
				dataSetConfiguration.getDisplayName()+"==");
			dataLoaderService.checkConfiguration(
				rifManager,
				dataSetConfiguration);			
		}
		
	}
	
	private void logException(
		final RIFServiceException rifServiceException) {
		
	}
	
	private void logMessage(
		final String logMessage) {
		
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


