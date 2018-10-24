
package org.sahsu.rif.generic.taxonomyservices;

import java.io.File;
import java.io.InputStream;
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

public final class TaxonomyServiceConfigurationXMLReader {

	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private TaxonomyServiceContentHandler taxonomyServiceContentHandler;
	
	private ArrayList<TaxonomyServiceAPI> taxonomyServices;
	
	private TaxonomyLogger taxonomyLogger = TaxonomyLogger.getLogger();
	
    /**
     * Instantiates a new RIF job submission xml reader.
     */
	public TaxonomyServiceConfigurationXMLReader() {
		taxonomyServiceContentHandler
			= new TaxonomyServiceContentHandler();
		taxonomyServices = new ArrayList<>();
    }

/**
 * Read file.
 *
 * @param defaultResourceDirectoryPath the directory
 * @throws RIFServiceException the RIF service exception
 */
	public void readFile(
		final String defaultResourceDirectoryPath) 
		throws RIFServiceException {

		File taxonomyServiceConfigurationFile;
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
			ArrayList<String> errorMessages = new ArrayList<>();
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
					Class<? extends TaxonomyServiceAPI> taxonomyServiceClass = Class.forName(
							ontologyServiceClassName).asSubclass(TaxonomyServiceAPI.class);
					TaxonomyServiceAPI taxonomyService =
							taxonomyServiceClass.getConstructor().newInstance();

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
					throw new RIFServiceException(
						RIFGenericLibraryError.TAXONOMY_SERVICE_INITIALISATION_FAILURE,
						errorMessages);
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
					throw new RIFServiceException(
						RIFGenericLibraryError.TAXONOMY_SERVICE_INITIALISATION_FAILURE,
						errorMessages);
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
	
	HashMap<String, TaxonomyServiceAPI> getTaxonomyFromIdentifierHashMap() {
		
		HashMap<String, TaxonomyServiceAPI> serviceFromIdentifier = new HashMap<>();
		for (TaxonomyServiceAPI taxonomyService : taxonomyServices) {
			serviceFromIdentifier.put(taxonomyService.getIdentifier().trim(), taxonomyService);
		}
		
		return serviceFromIdentifier;
	}
	
	public ArrayList<TaxonomyServiceAPI> getTaxonomyServices() {
		return taxonomyServices;
	}
}
