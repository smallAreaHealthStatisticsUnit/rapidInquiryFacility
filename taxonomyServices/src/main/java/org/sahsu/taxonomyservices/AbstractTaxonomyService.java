package org.sahsu.taxonomyservices;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;

public abstract class AbstractTaxonomyService implements TaxonomyServiceAPI {

	private String identifier;
	private String name;
	private String description;
	private String version;
	
	private ArrayList<Parameter> parameters;
	private AtomicBoolean isServiceWorking = new AtomicBoolean(false);
	
	protected TaxonomyTermManager taxonomyTermManager;

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
	public abstract void initialiseService(
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

	/**
	 * Returns the {@code Path} value of a parameter in the configuration file which has a key
	 * of the specified name.
	 * @param taxonomyServiceConfiguration the configuration object
	 * @param taxonomyFileParameter the name of the parameter whose value represents a file
	 * @return the {@code Path} to the file, if found
	 * @throws RIFServiceException if the file name is not in the configuration, or the file is
	 * not found.
	 */
	Path getTaxonomyFilePath(
			final TaxonomyServiceConfiguration taxonomyServiceConfiguration,
			String taxonomyFileParameter) throws RIFServiceException {

		String taxonomyFileName = extractParameterValue(taxonomyServiceConfiguration,
		                                                taxonomyFileParameter);

		if (StringUtils.isEmpty(taxonomyFileName)) {

			throw new RIFServiceException("Taxonomy file name for %s not found in configuration "
			                              + "file", taxonomyServiceConfiguration.getName());
		}

		return AppFile.getTaxonomyInstance(taxonomyFileName).path();
	}

	/**
	 * Gets the value of parameter from the {@code TaxonomyServiceConfiguration}, given its name.
	 * @param config the {@code TaxonomyServiceConfiguration}
	 * @param name the parameter name
	 * @return the parameter value, or the empty string if it doesn't exist
	 */
	protected String extractParameterValue(TaxonomyServiceConfiguration config, String name) {

		List<Parameter> params = config.getParameters();
		return Parameter.getParameter(name, params).getValue();
	}
}
