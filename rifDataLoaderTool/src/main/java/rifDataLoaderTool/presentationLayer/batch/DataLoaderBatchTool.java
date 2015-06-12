package rifDataLoaderTool.presentationLayer.batch;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifServices.system.RIFServiceException;

import java.text.Collator;

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

public class DataLoaderBatchTool {

	public static void main(String[] arguments) {
		
		DataLoaderBatchTool dataLoaderBatchTool
			= new DataLoaderBatchTool();
		dataLoaderBatchTool.run(arguments);
	}
	
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DataLoaderBatchTool() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void run(final String[] commandLineArguments) {
		
		try {
			validateCommandLineArguments(commandLineArguments);

			
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
		
		//the last argument should be the file name
		
		
		
		
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private void validateCommandLineArguments(
		final String[] commandLineArguments) 
		throws RIFServiceException {
		
		int totalArguments = commandLineArguments.length;
		
		if (totalArguments == 0) {
			//ERROR: must at least specify an input XML file
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.NO_COMMAND_LINE_ARGUMENTS_SPECIFIED,
					errorMessage);
			throw rifServiceException;
		}
		
		String configurationFilePath
			= commandLineArguments[totalArguments - 1];
		if (configurationFilePath.startsWith("-")) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.ILLEGAL_CONFIGURATION_FILE_SPECIFIED,
					errorMessage);
			throw rifServiceException;			
		}
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


