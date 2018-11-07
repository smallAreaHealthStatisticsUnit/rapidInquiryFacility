
package org.sahsu.taxonomyservices;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;
import org.sahsu.rif.generic.util.TaxonomyLogger;

public final class TaxonomyServiceConfigurationXMLReader {

	private TaxonomyServiceContentHandler taxonomyServiceContentHandler;

	private TaxonomyLogger taxonomyLogger = TaxonomyLogger.getLogger();

	/**
     * Instantiates a new RIF job submission xml reader.
     */
	public TaxonomyServiceConfigurationXMLReader() {

		taxonomyServiceContentHandler = new TaxonomyServiceContentHandler();
	}

	/**
	 * Read file.
	 *
	 * @param defaultResourceDirectoryPath the directory
	 * @throws RIFServiceException the RIF service exception
	 */
	List<TaxonomyServiceConfiguration> readFile(final Path defaultResourceDirectoryPath)
			throws RIFServiceException {

		File taxonomyServiceConfigurationFile = defaultResourceDirectoryPath.resolve(
				"TaxonomyServicesConfiguration.xml").toFile();

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
