package org.sahsu.taxonomyservices;

import java.nio.file.Path;
import java.util.List;

import org.sahsu.rif.generic.fileformats.CsvFile;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.rif.generic.util.TaxonomyLogger;

/**
 * Provides a disease taxonomy based on a CSV file.
 */
public class GenericCsvBasedTaxonomyService extends AbstractTaxonomyService {

	private static final TaxonomyLogger rifLogger = TaxonomyLogger.getLogger();

	@Override
	public void initialiseService(final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException {

		String taxonomyName = taxonomyServiceConfiguration.getServiceIdentifier();
		String longName = taxonomyServiceConfiguration.getName();
		rifLogger.info(getClass(), String.format("Initialising %s taxonomy service", longName));
		setServiceWorking(false);

		// Basic setup
		setTaxonomyServiceConfiguration(taxonomyServiceConfiguration);

		// Locate the taxonomy file. This works on the assumption that the <taxonomy_service>
		// block contains exactly one <parameter> in its <parameters> block.
		String fileName = taxonomyServiceConfiguration.getParameters().get(0).getName();
		Path taxonomyFile = getTaxonomyFilePath(taxonomyServiceConfiguration, fileName);

		// OK, we've got a real file
		CsvFile file = new CsvFile(taxonomyFile);

		List<TaxonomyTerm> terms = file.parseTaxonomyTerms();
		setTaxonomyTermManager(TaxonomyTermManager.newInstance(taxonomyName));
		for (TaxonomyTerm term : terms) {

			term.setNameSpace(taxonomyName);
			taxonomyTermManager.addTerm(term);
		}

		setIdentifier(taxonomyServiceConfiguration.getServiceIdentifier());
		setServiceWorking(true);
		rifLogger.info(getClass(), String.format("Data for taxonomy %s loaded. Service started",
		                                         longName));
	}
}
