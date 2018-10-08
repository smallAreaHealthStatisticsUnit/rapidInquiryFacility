package org.sahsu.rif.generic.taxonomyservices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.RIFServiceException;

public abstract class AbstractTaxonomyService implements TaxonomyServiceAPI {

	private String identifier;
	private String name;
	private String description;
	private String version;
	
	private ArrayList<Parameter> parameters;
	private AtomicBoolean isServiceWorking = new AtomicBoolean(false);
	
	private TaxonomyTermManager taxonomyTermManager;

	public AbstractTaxonomyService() {
		identifier = "";
		name = "";
		description = "";
		version = "";
		parameters = new ArrayList<>();
		
		taxonomyTermManager = TaxonomyTermManager.newInstance();
		
		isServiceWorking.set(false);
	}

	protected void setTaxonomyTermManager(final TaxonomyTermManager taxonomyTermManager) {
		this.taxonomyTermManager = taxonomyTermManager;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
		taxonomyTermManager.setTaxonomyServiceIdentifier(identifier);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	protected void setTaxonomyServiceConfiguration(
			final TaxonomyServiceConfiguration taxonomyServiceConfiguration) {

		identifier = taxonomyServiceConfiguration.getServiceIdentifier();
		taxonomyTermManager.setTaxonomyServiceIdentifier(identifier);
		name = taxonomyServiceConfiguration.getName();
		description = taxonomyServiceConfiguration.getDescription();
		version = taxonomyServiceConfiguration.getVersion();
		parameters.addAll(taxonomyServiceConfiguration.getParameters());
	}

	protected void setServiceWorking(final boolean isServiceWorking) {
		this.isServiceWorking.set(isServiceWorking);
	}

	@Override
	public boolean isServiceWorking() {
		return isServiceWorking.get();
	}

	@Override
	public abstract void initialiseService(final String defaultResourceDirectoryPath,
			final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException;
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param parentTermIdentifier the parent health code
	 * @return the immediate subterms
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public List<TaxonomyTerm> getImmediateChildTerms(final String parentTermIdentifier)
			throws RIFServiceException {
		
		return taxonomyTermManager.getImmediateChildTerms(parentTermIdentifier);
	}

	/**
	 * Gets the parent health code.
	 *
	 * @param childTermIdentifier the child health code
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */
	@Override
	public TaxonomyTerm getParentTerm(final String childTermIdentifier) throws RIFServiceException {
		
		return taxonomyTermManager.getParentTerm(childTermIdentifier);		
	}

	/**
	 * Gets the root terms.
	 *
	 * @return the root terms
	 */
	@Override
	public List<TaxonomyTerm> getRootTerms() {
		
		return taxonomyTermManager.getRootTerms();
	}

	@Override
	public List<TaxonomyTerm> getMatchingTerms(
		final String searchPhrase,
		final boolean isCaseSensitive) {
	
		return taxonomyTermManager.getMatchingTerms(searchPhrase, isCaseSensitive);
	}	
}
