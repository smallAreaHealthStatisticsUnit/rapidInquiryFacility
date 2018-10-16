package org.sahsu.taxonomyservices;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.AbstractTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfiguration;

public class Icd9TaxonomyService extends AbstractTaxonomyService {

	@Override
	public void initialiseService(final String defaultResourceDirectoryPath,
			final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException {

		// Locate the ICD 9 file
		Path icd9File = locateTheICD9File(defaultResourceDirectoryPath,
		                                 taxonomyServiceConfiguration);

		// OK, we've got a real file
		// CsvFile file = new CsvFile(icd9File);


	}

	private Path locateTheICD9File(final String defaultResourceDirectoryPath,
			final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException {

		List<Parameter> params = taxonomyServiceConfiguration.getParameters();
		String icd9FileName = Parameter.getParameter("icd9_file", params).getValue();

		if (StringUtils.isEmpty(icd9FileName)) {

			throw new RIFServiceException("ICD9 file name not found in configuration file");
		}

		Path icd9File = FileSystems.getDefault().getPath(defaultResourceDirectoryPath,
		                                                 icd9FileName);
		if (!icd9File.toFile().exists()) {

			String msg = String.format("ICD9 file %s not found", icd9File.toString());
			throw new RIFServiceException(msg);
		}

		return icd9File;
	}
}
