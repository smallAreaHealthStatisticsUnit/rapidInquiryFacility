package org.sahsu.rif.generic.taxonomyservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;

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

	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	private static final FederatedTaxonomyService federatedTaxonomyService
			= new FederatedTaxonomyService();
	private AtomicBoolean hasInitialisationBegun = new AtomicBoolean(false);
	private volatile boolean isInitialised = false;
	private HashMap<String, TaxonomyServiceAPI> taxonomyServiceFromIdentifier;

	private FederatedTaxonomyService() {
		taxonomyServiceFromIdentifier = new HashMap<>();
		isInitialised = false;
	}

	public static FederatedTaxonomyService getFederatedTaxonomyService() {
		return federatedTaxonomyService;
	}
	
	public void initialise(final String defaultResourceDirectoryPath)
			throws RIFServiceException {

		// Prevent multiple threads from trying to initialise the service
		if (!hasInitialisationBegun.get()) {

			hasInitialisationBegun.set(true);

			/*
			 * For now, the default taxonomy service will only create and
			 * initialise a taxonomy service once.  It will not deal with
			 * cases where a service dies, it tries to restart it etc.
			 */
			TaxonomyServiceConfigurationXMLReader reader
					= new TaxonomyServiceConfigurationXMLReader();
			reader.readFile(defaultResourceDirectoryPath);
			taxonomyServiceFromIdentifier = reader.getTaxonomyFromIdentifierHashMap();
			isInitialised = true;
		}
	}
	
	public boolean isInitialised() {
		return isInitialised;
	}
	
	private void checkFederatedServiceWorkingProperly() throws RIFServiceException {
		
		if (!isInitialised()) {
			//The federated taxonomy service itself has not finished initialising

			String errorMessage
				= GENERIC_MESSAGES.getMessage("federatedTaxonomyService.error.notInitialised");
			throw new RIFServiceException(
					RIFGenericLibraryError.FEDERATED_TAXONOMY_SERVICE_NOT_INITIALISED,
					errorMessage);
		}
	}
	
	private void checkTaxonomyServiceWorkingProperly(final String taxonomyServiceIdentifier)
			throws RIFServiceException {
		
		checkFederatedServiceWorkingProperly();

		//check service exists
		TaxonomyServiceAPI taxonomyService
				= taxonomyServiceFromIdentifier.get(taxonomyServiceIdentifier);
		if (taxonomyService == null) {
			//Error: Non-existent service
			String errorMessage = GENERIC_MESSAGES.getMessage(
					"taxonomyServices.error.nonExistentTaxonomyService",
					taxonomyServiceIdentifier);
			throw new RIFServiceException(
				RIFGenericLibraryError.NON_EXISTENT_TAXONOMY_SERVICE,
				errorMessage);
		}
		else {
			//service exists but check if it's working
			if (!taxonomyService.isServiceWorking()) {
				String errorMessage = GENERIC_MESSAGES.getMessage(
						"taxonomyServices.error.serviceNotWorking",
						taxonomyServiceIdentifier);
				throw new RIFServiceException(
					RIFGenericLibraryError.TAXONOMY_SERVICE_NOT_WORKING,
					errorMessage);
			}
		}
	}

	public ArrayList<TaxonomyServiceProvider> getTaxonomyServiceProviders()
			throws RIFServiceException {

		checkFederatedServiceWorkingProperly();

		ArrayList<TaxonomyServiceAPI> taxonomyServices = new ArrayList<>(
				taxonomyServiceFromIdentifier.values());
		
		ArrayList<TaxonomyServiceProvider> results = new ArrayList<>();
		for (TaxonomyServiceAPI taxonomyService : taxonomyServices) {
			TaxonomyServiceProvider taxonomyServiceProvider = new TaxonomyServiceProvider();
			taxonomyServiceProvider.setIdentifier(taxonomyService.getIdentifier());
			taxonomyServiceProvider.setName(taxonomyService.getName());
			taxonomyServiceProvider.setDescription(taxonomyService.getDescription());
			results.add(taxonomyServiceProvider);
		}
		
		return results;
	}
		
	public List<TaxonomyTerm> getMatchingTerms(final String taxonomyServiceIdentifier,
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

	public TaxonomyTerm getParentTerm(final String taxonomyServiceIdentifier,
			final String childTermIdentifier) throws RIFServiceException {
		
		return getTaxonomyService(taxonomyServiceIdentifier).getParentTerm(childTermIdentifier);
	}
	
	private TaxonomyServiceAPI getTaxonomyService(final String taxonomyServiceIdentifier)
			throws RIFServiceException {

		checkTaxonomyServiceWorkingProperly(taxonomyServiceIdentifier);

		return taxonomyServiceFromIdentifier.get(taxonomyServiceIdentifier);
	}
}
