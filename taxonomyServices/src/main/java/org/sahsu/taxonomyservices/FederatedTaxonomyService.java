package org.sahsu.taxonomyservices;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.rif.generic.util.TaxonomyLogger;

/**
 * This class reads descriptions of taxonomy services from a configuration file and then
 * initialises an instance of each of them.  Its main job is to help control the instantiation
 * of services and to minimise the risk of concurrency errors happening. It manages a 
 * Singleton instance of itself and tries to ensure that all of the services will have
 * been fully instantiated before any of them are actually used.
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */
public class FederatedTaxonomyService {

	private static final TaxonomyLogger logger = TaxonomyLogger.getLogger();
	private static final FederatedTaxonomyService federatedTaxonomyService
			= new FederatedTaxonomyService();

	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	private AtomicBoolean initialisationHasBegun = new AtomicBoolean(false);
	private volatile boolean isInitialised;
	private TaxonomyServices services;

	private FederatedTaxonomyService() {

		isInitialised = false;
	}

	public static FederatedTaxonomyService getFederatedTaxonomyService() {
		return federatedTaxonomyService;
	}
	
	public void initialise() throws RIFServiceException {

		// Prevent multiple threads from trying to initialise the service
		if (!isInitialised && initialisationHasBegun.compareAndSet(false, true)) {

			TaxonomyServiceConfigurationXMLReader reader =
					new TaxonomyServiceConfigurationXMLReader();
			List<TaxonomyServiceConfiguration> servicesList = reader.readFile();
			services = new TaxonomyServices(servicesList);
			List<String> errorMessages = services.start();

			if (!errorMessages.isEmpty()) {

				logger.warning(getClass(), "Not all taxonomy services could be started. "
				                           + "See earlier messages.");
			}

			isInitialised = true;
		}
	}
	
	boolean isInitialised() {
		return isInitialised;
	}
	
	private void checkFederatedServiceWorkingProperly() throws RIFServiceException {
		
		if (!isInitialised()) {

			//The federated taxonomy service itself has not finished initialising
			String errorMessage = GENERIC_MESSAGES.getMessage(
					"federatedTaxonomyService.error.notInitialised");
			throw new RIFServiceException(
					RIFGenericLibraryError.FEDERATED_TAXONOMY_SERVICE_NOT_INITIALISED,
					errorMessage);
		}
	}
	
	private void checkTaxonomyServiceWorkingProperly(final String taxonomyServiceIdentifier)
			throws RIFServiceException {
		
		checkFederatedServiceWorkingProperly();

		//check service exists
		TaxonomyServiceAPI taxonomyService = services.getApi(taxonomyServiceIdentifier);
		if (taxonomyService == null) {

			String errorMessage = GENERIC_MESSAGES.getMessage(
					"taxonomyServices.error.nonExistentTaxonomyService",
					taxonomyServiceIdentifier);
			throw new RIFServiceException(RIFGenericLibraryError.NON_EXISTENT_TAXONOMY_SERVICE,
			                              errorMessage);
		} else {

			if (!taxonomyService.isServiceWorking()) {

				String errorMessage = GENERIC_MESSAGES.getMessage(
						"taxonomyServices.error.serviceNotWorking", taxonomyServiceIdentifier);
				throw new RIFServiceException(RIFGenericLibraryError.TAXONOMY_SERVICE_NOT_WORKING,
				                              errorMessage);
			}
		}
	}

	List<TaxonomyServiceProvider> getTaxonomyServiceProviders() throws RIFServiceException {

		checkFederatedServiceWorkingProperly();

		return services.getProviders();
	}
		
	List<TaxonomyTerm> getMatchingTerms(final String taxonomyServiceIdentifier,
			final String searchText, final boolean isCaseSensitive) throws RIFServiceException {

		TaxonomyServiceAPI taxonomyService = getTaxonomyService(taxonomyServiceIdentifier);
		
		return taxonomyService.getMatchingTerms(searchText, isCaseSensitive);
	}
	
	public List<TaxonomyTerm> getRootTerms(final String taxonomyServiceIdentifier)
			throws RIFServiceException {
		
		return getTaxonomyService(taxonomyServiceIdentifier).getRootTerms();
	}
	
	public List<TaxonomyTerm> getImmediateChildTerms(final String taxonomyServiceIdentifier,
			final String parentTermIdentifier) throws RIFServiceException {
		
		return getTaxonomyService(taxonomyServiceIdentifier).getImmediateChildTerms(parentTermIdentifier);
	}

	private TaxonomyServiceAPI getTaxonomyService(final String taxonomyServiceIdentifier)
			throws RIFServiceException {

		checkTaxonomyServiceWorkingProperly(taxonomyServiceIdentifier);

		return services.getApi(taxonomyServiceIdentifier);
	}
}
