
package org.sahsu.rif.generic.taxonomyservices;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;
import org.sahsu.rif.generic.util.TaxonomyLogger;
import org.xml.sax.InputSource;

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


public final class TaxonomyServiceConfigurationXMLReader {

// ==========================================
// Section Constants
// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
	private TaxonomyServiceContentHandler taxonomyServiceContentHandler;
	
	private ArrayList<TaxonomyServiceAPI> taxonomyServices;
	
	private TaxonomyLogger taxonomyLogger = TaxonomyLogger.getLogger();
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new RIF job submission xml reader.
     */
	public TaxonomyServiceConfigurationXMLReader() {
		taxonomyServiceContentHandler
			= new TaxonomyServiceContentHandler();
		taxonomyServices = new ArrayList<TaxonomyServiceAPI>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    
	/**
 * Read file.
 *
 * @param defaultResourceDirectoryPath the directory
 * @throws RIFServiceException the RIF service exception
 */
	public void readFile(
		final String defaultResourceDirectoryPath) 
		throws RIFServiceException {

		File taxonomyServiceConfigurationFile = null;
		StringBuilder filePath = new StringBuilder();
		StringBuilder filePath2 = new StringBuilder();
		String resourceDirectoryPath = defaultResourceDirectoryPath;
		
		Map<String, String> environmentalVariables = System.getenv();
		
		String catalinaHome = environmentalVariables.get("CATALINA_HOME");
		if (catalinaHome == null) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					"taxonomyServices.error.initialisationFailure: CATALINA_HOME not set in the "
					+ "environment");
			taxonomyLogger.error(this.getClass(), "TaxonomyServiceConfigurationXMLReader error", rifServiceException);
			throw rifServiceException;
		}
		filePath.append(catalinaHome);
		filePath.append(File.separator);
		filePath.append("conf");
		filePath.append(File.separator);
		filePath.append("TaxonomyServicesConfiguration.xml");
		taxonomyServiceConfigurationFile
			= new File(filePath.toString());
		if (taxonomyServiceConfigurationFile.exists()) {
			resourceDirectoryPath = catalinaHome + File.separator + "conf";
			taxonomyLogger.info(this.getClass(), "TaxonomyService configuration file: " + filePath.toString());
		}
		else {
			filePath2.append(defaultResourceDirectoryPath);
			filePath2.append(File.separator);
  			filePath2.append("TaxonomyServicesConfiguration.xml");
			taxonomyServiceConfigurationFile
				= new File(filePath2.toString());
			if (taxonomyServiceConfigurationFile.exists()) {
				taxonomyLogger.info(this.getClass(), "TaxonomyService configuration file: " + filePath2.toString());
			}
			else {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						"taxonomyServices.error.initialisationFailure: Cannot find TaxonomyService"
						+ " configuration file: TaxonomyServicesConfiguration.xml");
				taxonomyLogger.error(this.getClass(), "TaxonomyServiceConfigurationXMLReader error", rifServiceException);
				throw rifServiceException;
			}
		}
		
		try {			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
						
			saxParser.parse(
				taxonomyServiceConfigurationFile, 
				taxonomyServiceContentHandler);
			
			//now that the file has been read, instantiate the services
			ArrayList<String> errorMessages = new ArrayList<String>();
			ArrayList<TaxonomyServiceConfiguration> currentServiceConfigurations
				= taxonomyServiceContentHandler.getTaxonomyServiceConfigurations();
			for (TaxonomyServiceConfiguration currentServiceConfiguration : currentServiceConfigurations) {
				String ontologyServiceClassName = null;
				try {
					
					ontologyServiceClassName = currentServiceConfiguration.getOntologyServiceClassName().trim();
					
					/*
					 * Load the class in to memory, if it is not loaded - which means creating in-memory 
					 * representation of the class from the .class file so that an instance can be created out of it. This includes initializing static variables (resolving of that class)
					 *
					 * create an instance of that class and store the reference to the variable.
					 */
					Class taxonomyServiceClass
						= Class.forName(ontologyServiceClassName);
					TaxonomyServiceAPI taxonomyService
						= (TaxonomyServiceAPI) taxonomyServiceClass.getConstructor().newInstance();

					taxonomyService.initialiseService(
						resourceDirectoryPath, 
						currentServiceConfiguration);
					
					taxonomyServices.add(taxonomyService);
					
				}	
				catch(NoClassDefFoundError noClassDefFoundError) {
					taxonomyLogger.error(this.getClass(), "Unable to load taxonomyService: " + 
						ontologyServiceClassName, 
						noClassDefFoundError);

					String errorMessage
						= GENERIC_MESSAGES.getMessage(
							"taxonomyServices.error.initialisationFailure", 
							currentServiceConfiguration.getName());				
					errorMessages.add(errorMessage);
					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFGenericLibraryError.TAXONOMY_SERVICE_INITIALISATION_FAILURE,
							errorMessages);
					throw rifServiceException;
				} 
				catch(Exception exception) {
					taxonomyLogger.error(this.getClass(), "Exception initializing taxonomyService: " + 
						ontologyServiceClassName, 
						exception);
					String errorMessage
						= GENERIC_MESSAGES.getMessage(
							"taxonomyServices.error.initialisationFailure", 
							currentServiceConfiguration.getName());				
					errorMessages.add(errorMessage);
					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFGenericLibraryError.TAXONOMY_SERVICE_INITIALISATION_FAILURE,
							errorMessages);
					throw rifServiceException;
				}
			}
		}
		catch(Exception exception) {
			taxonomyLogger.error(this.getClass(), "Exception processing TaxonomyService Configuration: " + 
				taxonomyServiceConfigurationFile.getName(), 
				exception);
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileReadingProblemException(
				taxonomyServiceConfigurationFile.getName());
		}
	}
	
	public void readFileFromString (
		final String rifStudySubmissionAsXML) 
		throws RIFServiceException {

		try {
			StringReader stringReader = new StringReader(rifStudySubmissionAsXML);
			InputSource inputSource = new InputSource(stringReader);
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(inputSource, taxonomyServiceContentHandler);
		}
		catch(Exception exception) {
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileReadingProblemException("");			
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
				taxonomyServiceContentHandler);
		}
		catch(Exception exception) {
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileReadingProblemException("");			
		}
	}			
	
	public HashMap<String, TaxonomyServiceAPI> getTaxonomyFromIdentifierHashMap() {
		
		HashMap<String, TaxonomyServiceAPI> serviceFromIdentifier
			= new HashMap<String, TaxonomyServiceAPI>();
		for (TaxonomyServiceAPI taxonomyService : taxonomyServices) {
			serviceFromIdentifier.put(taxonomyService.getIdentifier(), taxonomyService);
		}
		
		return serviceFromIdentifier;
	}
	
	public ArrayList<TaxonomyServiceAPI> getTaxonomyServices() {
		return taxonomyServices;
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
