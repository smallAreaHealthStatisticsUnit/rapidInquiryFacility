package rifDataLoaderTool.test;


import rifDataLoaderTool.dataStorageLayer.TestDataLoaderService;

import rifDataLoaderTool.dataStorageLayer.SampleDataGenerator;
import rifDataLoaderTool.businessConceptLayer.DataLoaderServiceAPI;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.LinearWorkflow;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.dataStorageLayer.LinearWorkflowEnactor;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

public class TestCheck extends AbstractRIFDataLoaderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	
	// ==========================================
	// Section Construction
	// ==========================================

	public TestCheck() {

	}


	@Test
	public void test2() {
		SampleDataGenerator sampleDataGenerator
			= new SampleDataGenerator();

		LinearWorkflow linearWorkflow		
			= sampleDataGenerator.getLinearWorkflowTest4StudyID1();
		linearWorkflow.setStopWorkflowState(WorkflowState.CLEAN);
	
		User rifManager = getRIFManager();
		TestDataLoaderService dataLoaderService
			= getDataLoaderService();
		try {

			LinearWorkflowEnactor linearWorkflowEnactor
				= new LinearWorkflowEnactor(rifManager, dataLoaderService);
			linearWorkflowEnactor.runWorkflow(linearWorkflow);
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();			
			fail();
		}

	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

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


