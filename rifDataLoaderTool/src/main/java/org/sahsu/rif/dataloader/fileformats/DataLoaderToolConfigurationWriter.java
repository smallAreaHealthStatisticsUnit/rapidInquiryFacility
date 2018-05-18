package org.sahsu.rif.dataloader.fileformats;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;

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
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public final class DataLoaderToolConfigurationWriter {

	public static void main(String[] args) {
		DLTestDataGenerator generator = new DLTestDataGenerator();
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= generator.createExampleDataLoaderToolConfiguration();
		
		DataLoaderToolConfigurationWriter writer
			= new DataLoaderToolConfigurationWriter();
		try {
			StringBuilder outputFilePath = new StringBuilder();
			outputFilePath.append("C:");
			outputFilePath.append(File.separator);
			outputFilePath.append("rifDataLoaderTool");
			outputFilePath.append(File.separator);
			outputFilePath.append("MyDLConfigFile.xml");
			
			File outputFile = new File(outputFilePath.toString());
			writer.writeFile(outputFile, dataLoaderToolConfiguration);
			
			DataLoaderToolConfigurationReader reader
				= new DataLoaderToolConfigurationReader();
			reader.readFile(outputFile);
			DataLoaderToolConfiguration dataLoaderToolConfiguration2
				= reader.getDataLoaderToolConfiguration();
			boolean configurationsIdentical
				= dataLoaderToolConfiguration.hasIdenticalContents(dataLoaderToolConfiguration2);
			System.out.println("Result=="+configurationsIdentical+"==");

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
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new RIF job submission xml writer.
	 */
	public DataLoaderToolConfigurationWriter() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public String writeFile(
		final File file,
		final DataLoaderToolConfiguration dataLoaderToolConfiguration)
		throws RIFServiceException {
			
		try {
			FileOutputStream fileOutputStream
				= new FileOutputStream(file);
			
			DataLoaderToolConfigurationHandler rifDataLoaderConfigurationHandler
				= new DataLoaderToolConfigurationHandler();
			ByteArrayOutputStream outputStream
				= new ByteArrayOutputStream();
			XMLCommentInjector commentInjector = new XMLCommentInjector();			
			rifDataLoaderConfigurationHandler.initialise(
				fileOutputStream, 
				commentInjector);
			rifDataLoaderConfigurationHandler.writeXML(dataLoaderToolConfiguration);
			outputStream.flush();
	    	String result 
				= new String(outputStream.toByteArray(), "UTF-8");	
	    	outputStream.close();			
	    	return result;
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileWritingProblemException(
				file.getAbsolutePath());
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
