package org.sahsu.taxonomyservices;

import java.io.File;
import java.util.List;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.AbstractTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.FederatedTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceConfiguration;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTermManager;
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
		final String defaultResourceDirectoryPath,
		final TaxonomyServiceConfiguration taxonomyServiceConfiguration) {
			
		String name = "UNKNOWN";
		String description = "UNKNOWN";
		
		try {
			ICD10TaxonomyTermParser icd1011TaxonomyParser 
				= new ICD10TaxonomyTermParser();	// May need a separate ICD11 parser if the ClaML format is different		
			
			setTaxonomyServiceConfiguration(taxonomyServiceConfiguration);

			List<Parameter> parameters = taxonomyServiceConfiguration.getParameters();
		
			Parameter icd1011FileParameter = Parameter.getParameter("icd10_ClaML_file", parameters);
			if (icd1011FileParameter == Parameter.NULL_PARAM) {
				icd1011FileParameter = Parameter.getParameter("icd11_ClaML_file", parameters);
			}
			name = taxonomyServiceConfiguration.getName();
			description = taxonomyServiceConfiguration.getDescription();
			
			if (icd1011FileParameter == Parameter.NULL_PARAM) {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage = "ICD10/11 taxonomy service: " + name + " is missing "
				                      + "a parameter for \"icd10_ClaML_file\" "
				                      + "or \"icd11_ClaML_file\"";

				throw new RIFServiceException(
					TaxonomyServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
					errorMessage);
			}		
		
			StringBuilder icd1011FileName = new StringBuilder();
			icd1011FileName.append(defaultResourceDirectoryPath);
			icd1011FileName.append(File.separator);
			icd1011FileName.append(icd1011FileParameter.getValue());
			
			File icd1011File = new File(icd1011FileName.toString());
			if (icd1011File.exists()) {		
				icd1011TaxonomyParser.readFile(icd1011File);
				rifLogger.info(this.getClass(), "icd101/1TaxonomyParser: " + name + " read: \"" + icd1011FileName + "\".");
				setTaxonomyTermManager(icd1011TaxonomyParser.getTaxonomyTermManager());
				setServiceWorking(true);
				rifLogger.info(this.getClass(), "icd101/1TaxonomyParser: " + name + " initialised: " + description + ".");
			}
			else {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage
					= "ICD10/11 taxonomy service: " + name + " file: \"" + icd1011FileParameter + "\" not found.";

				throw new RIFServiceException(
					TaxonomyServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
					errorMessage);
			}
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(
				this.getClass(), 
				"ICD10/11 taxonomy service: " + name + " initialiseService() error", 
				rifServiceException);
			setServiceWorking(false);
		}
	}
}

