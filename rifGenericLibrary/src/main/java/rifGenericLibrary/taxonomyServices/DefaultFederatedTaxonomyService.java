package rifGenericLibrary.taxonomyServices;

import rifGenericLibrary.system.RIFGenericLibraryError;

import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.ClassFileLocator;
import rifGenericLibrary.system.RIFServiceException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

public class DefaultFederatedTaxonomyService {

	public static void main(String[] args) {
		DefaultFederatedTaxonomyService service
			= new DefaultFederatedTaxonomyService();
		try {
			String defaultResourceDirectoryPath
				= ClassFileLocator.getClassRootLocation("rifGenericLibrary");
			service.initialise(defaultResourceDirectoryPath);
			
			ArrayList<TaxonomyServiceProvider> taxonomyServiceProviders
				= service.getTaxonomyServiceProviders();
			ArrayList<TaxonomyTerm> rootTerms
				= service.getRootTerms("icd10");
			for (TaxonomyTerm rootTerm : rootTerms) {
				System.out.println("Root Term:"+rootTerm.getLabel()+"==");
			}
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private HashMap<String, TaxonomyServiceAPI> taxonomyServiceFromIdentifier;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DefaultFederatedTaxonomyService() {
		taxonomyServiceFromIdentifier 
			= new HashMap<String, TaxonomyServiceAPI>();
	}

	public void initialise(final String defaultResourceDirectoryPath) 
		throws RIFServiceException {
		TaxonomyServiceConfigurationXMLReader reader
			= new TaxonomyServiceConfigurationXMLReader();
		reader.readFile(
			defaultResourceDirectoryPath);			
		
		ArrayList<TaxonomyServiceAPI> taxonomyServices
			= reader.getTaxonomyServices();
		for (TaxonomyServiceAPI taxonomyService : taxonomyServices) {
			System.out.println("Adding service with identifier=="+taxonomyService.getIdentifier()+"==");
			taxonomyServiceFromIdentifier.put(
				taxonomyService.getIdentifier(), 
				taxonomyService);
		}
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	
	public ArrayList<TaxonomyServiceProvider> getTaxonomyServiceProviders() 
		throws RIFServiceException {

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
		final String childTermIdentifier,
		final boolean isCaseSensitive)
		throws RIFServiceException {
		
		TaxonomyServiceAPI taxonomyService
			= getTaxonomyService(taxonomyServiceIdentifier);
		return taxonomyService.getParentTerm(childTermIdentifier);	
	}
	
	private TaxonomyServiceAPI getTaxonomyService(
		final String taxonomyServiceIdentifier) 
		throws RIFServiceException {

		TaxonomyServiceAPI matchingService
			= taxonomyServiceFromIdentifier.get(taxonomyServiceIdentifier);
		if (matchingService == null) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"taxonomyServices.error.nonExistentTaxonomyService", 
					taxonomyServiceIdentifier);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.NON_EXISTENT_TAXONOMY_SERVICE, 
					errorMessage);
			throw rifServiceException;
		}
		return matchingService;
	}
	
	
	// ==========================================
	// Section Override
	// ==========================================
}
