package org.sahsu.rif.dataloader.test;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.LinearWorkflow;
import org.sahsu.rif.dataloader.concepts.TestDataLoaderServiceAPI;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.dataloader.datastorage.LinearWorkflowEnactor;
import org.sahsu.rif.dataloader.datastorage.SampleDataGenerator;
import org.sahsu.rif.generic.concepts.User;
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

	private LinearWorkflowEnactor getEnactor() {
		User rifManager = getRIFManager();
		TestDataLoaderServiceAPI service = getDataLoaderService();
		LinearWorkflowEnactor linearWorkflowEnactor
			= new LinearWorkflowEnactor(rifManager, service);
		return linearWorkflowEnactor;		
	}
	
	private DataLoaderToolConfiguration getDataLoaderToolConfiguration() {
		SampleDataGenerator sampleDataGenerator
			= new SampleDataGenerator();
		return sampleDataGenerator.createSahsulandConfiguration();
	}
	
	private LinearWorkflow getNumeratorWorkflow(final String fileName) {
		String numeratorFilePath
			= getTestFilePath(fileName);	
		SampleDataGenerator sampleDataGenerator
			= new SampleDataGenerator();
		DataSetConfiguration numerator
			= sampleDataGenerator.createSahsulandNumeratorConfiguration();
		numerator.setFilePath(numeratorFilePath);

		LinearWorkflow linearWorkflow = LinearWorkflow.newInstance();
		linearWorkflow.addDataSetConfiguration(numerator);
		linearWorkflow.setStartWorkflowState(WorkflowState.START);
		linearWorkflow.setStopWorkflowState(WorkflowState.STOP);

		return linearWorkflow;		
	}
	
	@Test
	@Ignore
	public void test2() {

		LinearWorkflow numeratorWorkflow = getNumeratorWorkflow("sahsuland_cancer.csv");
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getDataLoaderToolConfiguration();
				
		LinearWorkflowEnactor enactor = getEnactor();
		
		File exportDirectory = getExportDirectory();
		
		try {
			enactor.runWorkflow(
				exportDirectory, 
				dataLoaderToolConfiguration, 
				numeratorWorkflow);
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			//rifServiceException.printStackTrace();
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


