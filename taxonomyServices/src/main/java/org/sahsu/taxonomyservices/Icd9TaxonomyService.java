package org.sahsu.taxonomyservices;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.fileformats.CsvFile;
import org.sahsu.rif.generic.fileformats.FilePath;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.AbstractTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfiguration;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTermManager;
import org.sahsu.rif.generic.util.TaxonomyLogger;

public class Icd9TaxonomyService extends AbstractTaxonomyService {

	private static final TaxonomyLogger rifLogger = TaxonomyLogger.getLogger();

	@Override
	public void initialiseService(final String defaultResourceDirectoryPath,
			final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException {

		rifLogger.info(getClass(), "Initialising ICD9 taxonomy service");

		// Basic setup
		setTaxonomyServiceConfiguration(taxonomyServiceConfiguration);

		// Locate the ICD 9 file
		Path icd9File = locateTheICD9File(
				taxonomyServiceConfiguration);

		// OK, we've got a real file
		CsvFile file = new CsvFile(icd9File);
		// List<TaxonomyTerm> terms = file.parse(TaxonomyTerm.class);
		List<TaxonomyTerm> terms = file.parse();
		TaxonomyTermManager manager = TaxonomyTermManager.newInstance("icd9");
		for (TaxonomyTerm term : terms) {

			term.setNameSpace("icd9");
			manager.addTerm(term);
		}

		rifLogger.info(getClass(), "ICD9 data loaded. Service started");
		setIdentifier(taxonomyServiceConfiguration.getServiceIdentifier());
		setTaxonomyTermManager(manager);
		setServiceWorking(true);
	}

	private Path locateTheICD9File(final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException {

		List<Parameter> params = taxonomyServiceConfiguration.getParameters();
		String icd9FileName = Parameter.getParameter("icd9_file", params).getValue();

		if (StringUtils.isEmpty(icd9FileName)) {

			throw new RIFServiceException("ICD9 file name not found in configuration file");
		}

		return new FilePath(icd9FileName).getPath();
	}


}
