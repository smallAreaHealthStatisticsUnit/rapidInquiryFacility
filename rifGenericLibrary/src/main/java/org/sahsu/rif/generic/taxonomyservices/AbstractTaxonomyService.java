package org.sahsu.rif.generic.taxonomyservices;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;
import org.sahsu.rif.generic.concepts.Parameter;

import java.util.ArrayList;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
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

public abstract class AbstractTaxonomyService 
	implements TaxonomyServiceAPI {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String identifier;
	private String name;
	private String description;
	private String version;
	
	private ArrayList<Parameter> parameters;
	//private AtomicBoolean initialisationHasBegun = new AtomicBoolean(false);
	private AtomicBoolean isServiceWorking = new AtomicBoolean(false);
	
	
	private TaxonomyTermManager taxonomyTermManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractTaxonomyService() {
		identifier = "";
		name = "";
		description = "";
		version = "";
		parameters = new ArrayList<Parameter>();
		
		taxonomyTermManager = TaxonomyTermManager.newInstance();
		
		//initialisationHasBegun.set(false);
		isServiceWorking.set(false);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setTaxonomyTermManager(final TaxonomyTermManager taxonomyTermManager) {
		this.taxonomyTermManager = taxonomyTermManager;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
		taxonomyTermManager.setTaxonomyServiceIdentifier(identifier);
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public void setTaxonomyServiceConfiguration(final TaxonomyServiceConfiguration taxonomyServiceConfiguration) {
		identifier = taxonomyServiceConfiguration.getServiceIdentifier();
		taxonomyTermManager.setTaxonomyServiceIdentifier(identifier);
		name = taxonomyServiceConfiguration.getName();
		description = taxonomyServiceConfiguration.getDescription();
		version = taxonomyServiceConfiguration.getVersion();
		parameters.addAll(taxonomyServiceConfiguration.getParameters());
	}
	
	public String getParameterValue(final String parameterName)
			throws RIFServiceException {
		
		Parameter parameter
			= Parameter.getParameter(parameterName, parameters);
		if (parameter == null) {
			RIFServiceExceptionFactory rifServiceExceptionFactory
				= new RIFServiceExceptionFactory();
			rifServiceExceptionFactory.createNonExistentParameter(parameterName);
		}
		return parameter.getValue();

	}
	
	protected void checkTermFileExists(final File termFile) {
		if (termFile.exists() == false) {

			RIFServiceExceptionFactory rifServiceExceptionFactory
				= new RIFServiceExceptionFactory();
			rifServiceExceptionFactory.createNonExistentFile(termFile.getAbsolutePath());
		}
	}

	/*
	public boolean hasInitialisationBegun() {
		return initialisationHasBegun.get();
	}

	public void initialisationBegun() {
		initialisationHasBegun.set(true);
	}
	*/

	protected void setServiceWorking(final boolean isServiceWorking) {
		this.isServiceWorking.set(isServiceWorking);
	}
	
	public boolean isServiceWorking() {
		return isServiceWorking.get();
	}
	
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: TaxonomyServiceAPI

	public abstract void initialiseService(
		final String defaultResourceDirectoryPath,
		final TaxonomyServiceConfiguration taxonomyServiceConfiguration) 
		throws RIFServiceException;
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param connection the connection
	 * @param parentTaxonomyTerm the parent health code
	 * @return the immediate subterms
	 * @throws RIFServiceException the RIF service exception
	 */
	public List<TaxonomyTerm> getImmediateChildTerms(
		final String parentTermIdentifier) 
		throws RIFServiceException {
		
		return taxonomyTermManager.getImmediateChildTerms(parentTermIdentifier);
	}

	/**
	 * Gets the parent health code.
	 *
	 * @param connection the connection
	 * @param childTaxonomyTerm the child health code
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */

	public TaxonomyTerm getParentTerm(final String childTermIdentifier) 
		throws RIFServiceException {
		
		return taxonomyTermManager.getParentTerm(childTermIdentifier);		
	}

	/**
	 * Gets the root terms.
	 *
	 * @return the root terms
	 */
	public List<TaxonomyTerm> getRootTerms() {
		
		return taxonomyTermManager.getRootTerms();
	}
      
	public List<TaxonomyTerm> getMatchingTerms(
		final String searchPhrase,
		final boolean isCaseSensitive) {
	
		return taxonomyTermManager.getMatchingTerms(
			searchPhrase, 
			isCaseSensitive);
	}	
	
	
	// ==========================================
	// Section Override
	// ==========================================
}
