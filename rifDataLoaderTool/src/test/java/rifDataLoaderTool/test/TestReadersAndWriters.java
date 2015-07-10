package rifDataLoaderTool.test;

import rifDataLoaderTool.dataStorageLayer.SampleDataGenerator;
import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.fileFormats.LinearWorkflowWriter;
import rifDataLoaderTool.fileFormats.LinearWorkflowReader;
import rifGenericLibrary.system.RIFServiceException;
import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;

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

public class TestReadersAndWriters {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public TestReadersAndWriters() {
		
		
	}

	@Test
	public void test1() {
		SampleDataGenerator sampleDataGenerator
			= new SampleDataGenerator();
		LinearWorkflow numeratorWorkflow
			= sampleDataGenerator.createSahsulandNumeratorWorkflow();
		try {			
			
			File outputFile 
				= new File("C://rif_scripts//test_data//cancer_numerator_data.xml");
			LinearWorkflowWriter writer1 = new LinearWorkflowWriter();
			writer1.write(numeratorWorkflow, outputFile);
			
			LinearWorkflowReader reader1 = new LinearWorkflowReader();
			reader1.readFile(outputFile);
			
			LinearWorkflow numeratorWorkflow2
				= reader1.getLinearWorkflow();
			
			File outputFile2 
				= new File("C://rif_scripts//test_data//cancer_numerator_data2.xml");
			LinearWorkflowWriter writer2 = new LinearWorkflowWriter();
			writer2.write(numeratorWorkflow2, outputFile2);
			
			
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			fail();
		}		
	}

	@Test
	public void test2() {
		SampleDataGenerator sampleDataGenerator
			= new SampleDataGenerator();
		LinearWorkflow minimalLinearWorkflow
			= sampleDataGenerator.createMinimalLinearWorkflow();
		try {			
			
			File outputFile 
				= new File("C://rif_scripts//test_data//minimal_linear_workflow.xml");
			LinearWorkflowWriter writer = new LinearWorkflowWriter();
			writer.write(minimalLinearWorkflow, outputFile);
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


