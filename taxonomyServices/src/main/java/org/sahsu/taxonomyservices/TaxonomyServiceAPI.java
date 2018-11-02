package org.sahsu.taxonomyservices;

import java.util.List;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;

public interface TaxonomyServiceAPI {

	void initialiseService(final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException;

	boolean isServiceWorking();
	String getIdentifier();
	String getName();
	String getDescription();
	String getVersion();

	/**
	 * Gets the health codes.
	 *
	 * @param searchText the search text
	 * @param isCaseSensitive
	 * @return the child terms
	 * @throws RIFServiceException the RIF service exception
	 */	
	List<TaxonomyTerm> getMatchingTerms(final String searchText, final boolean isCaseSensitive)
			throws RIFServiceException;

	/**
	 * Gets the top level codes.
	 *
	 * @return the top level codes
	 * @throws RIFServiceException the RIF service exception
	 */
	List<TaxonomyTerm> getRootTerms() throws RIFServiceException;
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @return the immediate subterms
	 * @throws RIFServiceException the RIF service exception
	 */
	List<TaxonomyTerm> getImmediateChildTerms(final String parentTermIdentifier)
			throws RIFServiceException;

	/**
	 * Gets the parent health code.
	 *
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */
	TaxonomyTerm getParentTerm(final String childTermIdentifier) throws RIFServiceException;
}
