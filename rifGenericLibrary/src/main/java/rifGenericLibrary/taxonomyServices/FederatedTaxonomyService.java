package rifGenericLibrary.taxonomyServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;

/**
 * This class reads descriptions of taxonomy services from a configuration file and then
 * initialises an instance of each of them.  Its main job is to help control the instantiation
 * of services and to minimise the risk of concurrency errors happening. It manages a 
 * Singleton instance of itself and tries to ensure that all of the services will have
 * been fully instantiated before any of them are actually used.
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public class FederatedTaxonomyService {
	
	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private static final FederatedTaxonomyService federatedTaxonomyService 
		= new FederatedTaxonomyService();
	
	private AtomicBoolean hasInitialisationBegun = new AtomicBoolean(false);
	private volatile boolean isInitialised = false;
	
	private HashMap<String, TaxonomyServiceAPI> taxonomyServiceFromIdentifier;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private FederatedTaxonomyService() {
		taxonomyServiceFromIdentifier 
			= new HashMap<String, TaxonomyServiceAPI>();
		isInitialised = false;
	}

	public static FederatedTaxonomyService getFederatedTaxonomyService() {
		return federatedTaxonomyService;
	}
	
	public void initialise(final String defaultResourceDirectoryPath) 
		throws RIFServiceException {

		if (hasInitialisationBegun.get() == true) {
			//prevent multiple threads from trying to initialise the service
			return;
		}
		else {
			hasInitialisationBegun.set(true);
			
			/*
			 * For now, the default taxonomy service will only create and
			 * initialise a taxonomy service once.  It will not deal with 
			 * cases where a service dies, it tries to restart it etc.
			 */
			TaxonomyServiceConfigurationXMLReader reader
				= new TaxonomyServiceConfigurationXMLReader();
			reader.readFile(
				defaultResourceDirectoryPath);
			taxonomyServiceFromIdentifier
				= reader.getTaxonomyFromIdentifierHashMap();
			
			isInitialised = true;
		}

	}
	
	public boolean isInitialised() {
		return isInitialised;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkFederatedServiceWorkingProperly() 
		throws RIFServiceException {
		
		if (isInitialised() == false) {
			//The federated taxonomy service itself has not finished initialising

			String errorMessage
				= GENERIC_MESSAGES.getMessage("federatedTaxonomyService.error.notInitialised");
			RIFServiceException rifServiceException
				 = new RIFServiceException(
						RIFGenericLibraryError.FEDERATED_TAXONOMY_SERVICE_NOT_INITIALISED, 
						errorMessage);
			throw rifServiceException;
		}
	}
	
	public void checkTaxonomyServiceWorkingProperly(
		final String taxonomyServiceIdentifier) 
		throws RIFServiceException {
		
		checkFederatedServiceWorkingProperly();

		//check service exists
		TaxonomyServiceAPI taxonomyService
			= taxonomyServiceFromIdentifier.get(taxonomyServiceIdentifier);
		if (taxonomyService == null) {
			//Error: Non-existent service
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"taxonomyServices.error.nonExistentTaxonomyService", 
					taxonomyServiceIdentifier);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.NON_EXISTENT_TAXONOMY_SERVICE, 
					errorMessage);
			throw rifServiceException;
		}
		else {
			//service exists but check if it's working
			if (taxonomyService.isServiceWorking() == false) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"taxonomyServices.error.serviceNotWorking",
						taxonomyServiceIdentifier);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFGenericLibraryError.TAXONOMY_SERVICE_NOT_WORKING, 
						errorMessage);
				throw rifServiceException;
			}
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	public ArrayList<TaxonomyServiceProvider> getTaxonomyServiceProviders() 
		throws RIFServiceException {

		checkFederatedServiceWorkingProperly();
		
		ArrayList<TaxonomyServiceAPI> taxonomyServices
			= new ArrayList<TaxonomyServiceAPI>();
		taxonomyServices.addAll(taxonomyServiceFromIdentifier.values());
		
		ArrayList<TaxonomyServiceProvider> results
			= new ArrayList<TaxonomyServiceProvider>();
		for (TaxonomyServiceAPI taxonomyService : taxonomyServices) {
			TaxonomyServiceProvider taxonomyServiceProvider
				= new TaxonomyServiceProvider();
			taxonomyServiceProvider.setIdentifier(taxonomyService.getIdentifier());
			taxonomyServiceProvider.setName(taxonomyService.getName());
			taxonomyServiceProvider.setDescription(taxonomyService.getDescription());
			results.add(taxonomyServiceProvider);
		}
		
		return results;
	}
		
	public ArrayList<TaxonomyTerm> getMatchingTerms(
		final String taxonomyServiceIdentifier,
		final String searchText,
		final boolean isCaseSensitive)
		throws RIFServiceException {

		TaxonomyServiceAPI taxonomyService
			= getTaxonomyService(taxonomyServiceIdentifier);
		
		return taxonomyService.getMatchingTerms(
			searchText, 
			isCaseSensitive);		
	}
	
	public ArrayList<TaxonomyTerm> getRootTerms(
		final String taxonomyServiceIdentifier)
		throws RIFServiceException {
		
		TaxonomyServiceAPI taxonomyService
			= getTaxonomyService(taxonomyServiceIdentifier);
		return taxonomyService.getRootTerms();
	}
	
	public ArrayList<TaxonomyTerm> getImmediateChildTerms(
		final String taxonomyServiceIdentifier,
		final String parentTermIdentifier)
		throws RIFServiceException {
		
		TaxonomyServiceAPI taxonomyService
			= getTaxonomyService(taxonomyServiceIdentifier);
		return taxonomyService.getImmediateChildTerms(parentTermIdentifier);
	}

	public TaxonomyTerm getParentTerm(
		final String taxonomyServiceIdentifier,
		final String childTermIdentifier)
		throws RIFServiceException {
		
		TaxonomyServiceAPI taxonomyService
			= getTaxonomyService(taxonomyServiceIdentifier);
		return taxonomyService.getParentTerm(childTermIdentifier);	
	}
	
	public TaxonomyServiceAPI getTaxonomyService(
		final String taxonomyServiceIdentifier) 
		throws RIFServiceException {

		checkTaxonomyServiceWorkingProperly(taxonomyServiceIdentifier);

		TaxonomyServiceAPI matchingService
			= taxonomyServiceFromIdentifier.get(taxonomyServiceIdentifier);		
		return matchingService;
	}
	
	
	// ==========================================
	// Section Override
	// ==========================================
}
