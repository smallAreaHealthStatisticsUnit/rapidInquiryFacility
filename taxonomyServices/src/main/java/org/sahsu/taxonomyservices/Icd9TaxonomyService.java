package org.sahsu.taxonomyservices;

import java.nio.file.Path;
import java.util.List;

import org.sahsu.rif.generic.fileformats.CsvFile;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.AbstractTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfiguration;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTermManager;
import org.sahsu.rif.generic.util.TaxonomyLogger;

// import com.google.common.reflect.TypeToken;

/**
 * Provides the ICD 9 taxonomy.
 */
public class Icd9TaxonomyService extends AbstractTaxonomyService {

	private static final TaxonomyLogger rifLogger = TaxonomyLogger.getLogger();
	private static final String ICD_9_TAXONOMY_NAME = "icd9";

	@Override
	public void initialiseService(final String defaultResourceDirectoryPath,
			final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException {

		rifLogger.info(getClass(), "Initialising ICD9 taxonomy service");
		setServiceWorking(false);

		// Basic setup
		setTaxonomyServiceConfiguration(taxonomyServiceConfiguration);

		// Locate the ICD 9 file
		Path icd9File = getTaxonomyFilePath(taxonomyServiceConfiguration, "icd9_file");

		// OK, we've got a real file
		CsvFile file = new CsvFile(icd9File);

		// TypeToken<TaxonomyTerm> t = new TypeToken<TaxonomyTerm>() {};
		// List<TaxonomyTerm> terms = file.parse(t);
		List<TaxonomyTerm> terms = file.parseTaxonomyTerms();
		TaxonomyTermManager manager = TaxonomyTermManager.newInstance(ICD_9_TAXONOMY_NAME);
		for (TaxonomyTerm term : terms) {

			term.setNameSpace(ICD_9_TAXONOMY_NAME);
			manager.addTerm(term);
		}

		rifLogger.info(getClass(), "ICD9 data loaded. Service started");
		setIdentifier(taxonomyServiceConfiguration.getServiceIdentifier());
		setTaxonomyTermManager(manager);
		setServiceWorking(true);
	}
}
