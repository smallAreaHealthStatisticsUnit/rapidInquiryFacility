package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.DataLoaderServiceAPI;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.LinearWorkflow;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaAreaPropertyManager;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.businessConceptLayer.WorkflowValidator;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;

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

		workflowValidator.validateLinearWorkflow(linearWorkflow);
				
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= linearWorkflow.getDataSetConfigurations();

		//run the workflow from start to finish for each of the
		//data set configurations
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			try {				
				processDataSetConfiguration(
					dataSetConfiguration,
					linearWorkflow);
				
				String finishedProcessingDataSetMessage
					= RIFDataLoaderToolMessages.getMessage(
						"workflowEnactor.finishedProcessingDataSet",
						dataSetConfiguration.getDisplayName());
				logMessage(finishedProcessingDataSetMessage);
				
			}
			catch(RIFServiceException rifServiceException) {
				logException(rifServiceException);
			}			
		}
	}
	
	private void processDataSetConfiguration(
		final DataSetConfiguration dataSetConfiguration,
		final LinearWorkflow linearWorkflow) 
		throws RIFServiceException {
		
		linearWorkflow.resetWorkflow();
				
		while (linearWorkflow.hasNext()) {
			
			processWorkflowStep(
				dataSetConfiguration,
				linearWorkflow.getCurrentWorkflowState());

			linearWorkflow.next();
		}

	}
	
	private void processWorkflowStep(
		final DataSetConfiguration dataSetConfiguration,
		final WorkflowState currentWorkflowState) 
		throws RIFServiceException {
		
		if (currentWorkflowState == WorkflowState.LOAD) {
			dataLoaderService.loadConfiguration(
				rifManager, 
				dataSetConfiguration);
		}
		else if (currentWorkflowState == WorkflowState.CLEAN) { 
			dataLoaderService.cleanConfiguration(
				rifManager, 
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.CONVERT) { 
			dataLoaderService.convertConfiguration(
				rifManager, 
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.OPTIMISE) { 
			dataLoaderService.optimiseConfiguration(
				rifManager,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.CHECK) { 
			dataLoaderService.checkConfiguration(
				rifManager,
				dataSetConfiguration);			
		}
		else if (currentWorkflowState == WorkflowState.PUBLISH) { 
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


