package org.sahsu.taxonomyservices;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.TaxonomyLogger;
import org.sahsu.taxonomyservices.system.TaxonomyServiceError;

/**
 * A taxonomy service that provides terms from ICD collections.  The main activity
 * of the class is to call a custom parser that understands the XML data format that
 * the WHO uses to represent ICD terms.  The parser registers the terms it reads into
 * an instance of {@link TaxonomyTermManager}, which
 * holds manages them in-memory.  The taxonomy term manager provides the main mechanism
 * by which the superclass {@link AbstractTaxonomyService}
 * supports most of the service calls.  
 * 
 * <p>
 * Most of the potential concurrency problems that may arise in simultaneous access to the
 * service are controlled by {@link FederatedTaxonomyService}.
 * </p>
 */
public class ClaMlTaxonomyService extends AbstractTaxonomyService {

	private static final TaxonomyLogger rifLogger = TaxonomyLogger.getLogger();

	public void initialiseService(
			final TaxonomyServiceConfiguration taxonomyServiceConfiguration) {
			
		String name = "UNKNOWN";
		String description;
		
		try {
			// May need a separate ICD11 parser if the ClaML format is different
			ICD10TaxonomyTermParser icd1011TaxonomyParser = new ICD10TaxonomyTermParser();
			
			setTaxonomyServiceConfiguration(taxonomyServiceConfiguration);
			String icdParm = "icd10_ClaML_file";
			if (StringUtils.isEmpty(extractParameterValue(taxonomyServiceConfiguration, icdParm))) {
				icdParm = "icd11_ClaML_file";
			}
			Path icdFile = getTaxonomyFilePath(taxonomyServiceConfiguration, icdParm);

			name = taxonomyServiceConfiguration.getName();
			description = taxonomyServiceConfiguration.getDescription();
			
			File icd1011File = icdFile.toFile();
			if (icd1011File.exists()) {		
				icd1011TaxonomyParser.readFile(icd1011File);
				rifLogger.info(this.getClass(), "icd101/1TaxonomyParser: " + name
				                                + " read: \"" + icdFile + "\".");
												
				setTaxonomyTermManager(icd1011TaxonomyParser.getTaxonomyTermManager());
				setServiceWorking(true);
				rifLogger.info(this.getClass(), "icd101/1TaxonomyParser: " + name
				                                + " initialised: " + description + ".");
			}
			else {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage
					= "ICD10/11 taxonomy service: " + name + " file: \"" + icdFile + "\" not "
					  + "found.";

				throw new RIFServiceException(
					TaxonomyServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
					errorMessage);
			}
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(getClass(), "ICD10/11 taxonomy service: " + name
			                            + " initialiseService() error", rifServiceException);
			setServiceWorking(false);
		}
	}
}

