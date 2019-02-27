
package org.sahsu.taxonomyservices;

import java.io.File;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.fileformats.ClasspathFilePath;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;
import org.sahsu.rif.generic.util.TaxonomyLogger;

final class TaxonomyServiceConfigurationXMLReader {

	private TaxonomyServiceContentHandler taxonomyServiceContentHandler;

	private TaxonomyLogger taxonomyLogger = TaxonomyLogger.getLogger();

	/**
     * Instantiates a new RIF job submission xml reader.
     */
	TaxonomyServiceConfigurationXMLReader() {

		taxonomyServiceContentHandler = new TaxonomyServiceContentHandler();
	}

	/**
	 * Read file.
	 *
	 * @throws RIFServiceException the RIF service exception
	 */
	List<TaxonomyServiceConfiguration> readFile() throws RIFServiceException {

		File taxonomyServiceConfigurationFile = AppFile.getTaxonomyInstance(
				"TaxonomyServicesConfiguration.xml").asFile();

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
						
			saxParser.parse(taxonomyServiceConfigurationFile, taxonomyServiceContentHandler);

			return taxonomyServiceContentHandler.getTaxonomyServiceConfigurations();

		} catch(Exception exception) {

			taxonomyLogger.error(this.getClass(),
			                     "Exception processing TaxonomyService Configuration: " +
			                     taxonomyServiceConfigurationFile.getName(), exception);
			RIFServiceExceptionFactory exceptionFactory = new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileReadingProblemException(
					taxonomyServiceConfigurationFile.getName());
		}
	}
}
