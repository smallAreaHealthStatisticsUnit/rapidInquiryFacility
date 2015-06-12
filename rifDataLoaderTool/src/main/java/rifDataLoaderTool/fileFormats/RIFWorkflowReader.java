
package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.CleanWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.RIFWorkflowConfiguration;


import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;



import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.*;


/**
 *
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
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


public final class RIFWorkflowReader {

	public static void main(String[] arguments) {
		
		
		try {
			StringBuilder inputFileName = new StringBuilder();
			inputFileName.append("C:");
			inputFileName.append(File.separator);
			inputFileName.append("rif_test_data");
			inputFileName.append(File.separator);
			inputFileName.append("data_loader_tool");
			inputFileName.append(File.separator);
			inputFileName.append("ExampleDataLoaderConfigurationFile.xml");

			
			File file 
				= new File(inputFileName.toString());
		
			RIFWorkflowReader rifWorkflowReader = new RIFWorkflowReader();
			rifWorkflowReader.readFile(file);
			
			RIFWorkflowConfiguration rifWorkflowConfiguration
				= rifWorkflowReader.getRIFWorkflowConfiguration();
			
			
			CleanWorkflowConfiguration cleanWorkflowConfiguration
				= rifWorkflowConfiguration.getCleanWorkflowConfiguration();
			cleanWorkflowConfiguration.printFields();
			
			//Now write it out
			StringBuilder outputFileName = new StringBuilder();
			outputFileName.append("C:");
			outputFileName.append(File.separator);
			outputFileName.append("rif_test_data");
			outputFileName.append(File.separator);
			outputFileName.append("data_loader_tool");
			outputFileName.append(File.separator);
			outputFileName.append("ResultConfiguration.xml");
			File outputFile = new File(outputFileName.toString());
			
			RIFWorkflowWriter rifWorkflowWriter = new RIFWorkflowWriter();
			rifWorkflowWriter.write(rifWorkflowConfiguration, outputFile);

			
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
		
	}
	
// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	private RIFWorkflowConfigurationHandler rifWorkflowConfigurationHandler;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF job submission xml reader.
     */
	public RIFWorkflowReader() {
		rifWorkflowConfigurationHandler
			= new RIFWorkflowConfigurationHandler();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    
	/**
 * Read file.
 *
 * @param rifSubmissionFile the rif submission file
 * @throws RIFServiceException the RIF service exception
 */
	public void readFile(
		final File rifWorfklowConfigurationFile) 
		throws RIFServiceException {

		try {			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(rifWorfklowConfigurationFile, rifWorkflowConfigurationHandler);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"io.error.problemReadingFile",
					rifWorfklowConfigurationFile.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.XML_FILE_PARSING_PROBLEM, 
					errorMessage);
			throw rifServiceException;
		}
	}

	public void readFile(
		final InputStream inputStream) 
		throws RIFServiceException {

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(
				inputStream, 
				rifWorkflowConfigurationHandler);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"io.error.problemReadingFile",
					"blah");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.XML_FILE_PARSING_PROBLEM, 
					errorMessage);
			throw rifServiceException;
		}
	}			
	
	
	public RIFWorkflowConfiguration getRIFWorkflowConfiguration() {
		return rifWorkflowConfigurationHandler.getRIFWorkflowConfiguration();
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
