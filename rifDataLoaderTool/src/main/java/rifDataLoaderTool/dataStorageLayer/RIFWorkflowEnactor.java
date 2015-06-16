package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.RIFWorkflowCollection;
import rifDataLoaderTool.businessConceptLayer.RIFWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSet;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.ConvertWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.OptimiseWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.PublishWorkflowConfiguration;

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

public class RIFWorkflowEnactor {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderService dataLoaderService;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFWorkflowEnactor() {
		dataLoaderService = new DataLoaderService();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void enactWorfklows(
		final User rifManager,
		final RIFWorkflowCollection rifWorkflowCollection) {
		ArrayList<DataSet> dataSets
			= rifWorkflowCollection.getDataSets();
		RIFWorkflowConfiguration rifWorkflowConfiguration 
			= rifWorkflowCollection.getRIFWorkflowConfiguration();
		for (DataSet dataSet : dataSets) {
			enactWorkflow(
				rifManager,
				dataSet, 
				rifWorkflowConfiguration);
		}		
	}
	
	private void enactWorkflow(
		final User rifManager,
		final DataSet dataSet,
		final RIFWorkflowConfiguration rifWorkflowConfiguration) {
		
		
		try {
			
			//initialise the work flow with a given data set
			rifWorkflowConfiguration.setDataSet(dataSet);
			
			
			CleanWorkflowConfiguration cleanWorkflowConfiguration
				= rifWorkflowConfiguration.getCleanWorkflowConfiguration();		
			dataLoaderService.loadConfiguration(
				rifManager, 
				cleanWorkflowConfiguration);

			dataLoaderService.cleanConfiguration(
				rifManager, 
				cleanWorkflowConfiguration);
			
			ConvertWorkflowConfiguration convertWorkflowConfiguration
				= rifWorkflowConfiguration.getConvertWorkflowConfiguration();
			dataLoaderService.convertConfiguration(
				rifManager, 
				convertWorkflowConfiguration);
			
			/*
			OptimiseWorkflowConfiguration optimiseWorkflowConfiguration
				= rifWorkflowConfiguration.getOptimiseWorkflowConfiguration();
			dataLoaderService.optimiseConfiguration(
				rifManager,
				optimiseWorkflowConfiguration);

			PublishWorkflowConfiguration publishWorkflowConfiguration
				= rifWorkflowConfiguration.getPublishWorkflowConfiguration();
			dataLoaderService.publishConfiguration(
				rifManager,
				publishWorkflowConfiguration);
			*/
			
			
		}
		catch(RIFServiceException rifServiceException) {
			
			
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	private void load(final RIFWorkflowConfiguration rifWorkflowConfiguration)
		throws RIFServiceException {
		
		
		
		
		
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


