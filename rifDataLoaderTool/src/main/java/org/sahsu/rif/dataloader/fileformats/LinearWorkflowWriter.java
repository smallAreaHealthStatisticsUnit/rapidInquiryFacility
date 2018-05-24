package org.sahsu.rif.dataloader.fileformats;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.LinearWorkflow;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;


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

public class LinearWorkflowWriter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public LinearWorkflowWriter() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String write(
		final ArrayList<LinearWorkflow> linearWorkflows,
		final File file) 
		throws RIFServiceException {
				
			try {
				FileOutputStream fileOutputStream
					= new FileOutputStream(file);
						
				LinearWorkflowConfigurationHandler rifWorkflowConfigurationHandler
					= new LinearWorkflowConfigurationHandler();

				ByteArrayOutputStream outputStream
					= new ByteArrayOutputStream();
				XMLCommentInjector commentInjector = new XMLCommentInjector();			
				rifWorkflowConfigurationHandler.initialise(
					fileOutputStream, 
					commentInjector);
				rifWorkflowConfigurationHandler.writeXML(linearWorkflows);
		    	String result 
					= new String(outputStream.toByteArray(), "UTF-8");	
		    	outputStream.flush();
		    	outputStream.close();			
		    	return result;
			}
			catch(Exception exception) {
				RIFServiceExceptionFactory exceptionFactory
					= new RIFServiceExceptionFactory();
				throw exceptionFactory.createFileWritingProblemException("");
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


