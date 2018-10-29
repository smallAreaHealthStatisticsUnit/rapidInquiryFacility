package org.sahsu.taxonomyservices;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;

/**
 * A convenience class to manage a collection of {@link TaxonomyTerm} objects in-memory.  The
 * main reason for developing it is to separate the code used to exercise relationships
 * between instances of <code>TaxonomyTerm</code> objects from the code used to 
 * parse them from whatever data format that is used to serialise them.  In practice,
 * we expect that taxonomy services will not vary much in their implementations, other 
 * than how they initially pull out collections of terms from text files.  Once they 
 * do that, they can register the terms with an instance of this class.  The
 * <code>TaxonomyTermManager</code> can then provide most of the code used to support
 * methods that are expected by the 
 * {@link TaxonomyServiceAPI} interface.
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

public class TaxonomyTermManager {

	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();

	private String taxonomyServiceID;
	private List<TaxonomyTerm> rootTerms;
	/** The all terms. */
	private List<TaxonomyTerm> allTerms;
	
	private Map<String, TaxonomyTerm> termFromIdentifier;
	
	private TaxonomyTermManager(final String taxonomyServiceID) {
		init(taxonomyServiceID);		
	}

	public static TaxonomyTermManager newInstance() {
		return new TaxonomyTermManager("");
	}
	
	public static TaxonomyTermManager newInstance(
		final String taxonomyServiceID) {

		return new TaxonomyTermManager(taxonomyServiceID);
	}

	private void init(final String taxonomyServiceID) {
		this.taxonomyServiceID = taxonomyServiceID;
		rootTerms = new ArrayList<>();
		allTerms = new ArrayList<>();
		termFromIdentifier = new HashMap<>();
	}

	void setTaxonomyServiceIdentifier(final String taxonomyServiceID) {

		this.taxonomyServiceID = taxonomyServiceID;
	}
	
	public void determineRootTerms() {
		for (TaxonomyTerm term : allTerms) {
			if (term.getParentTerm() == null) {
				rootTerms.add(term);
			}
		}		
	}
	
	public void addTerm(final TaxonomyTerm taxonomyTerm) {

		allTerms.add(taxonomyTerm);
		termFromIdentifier.put(
			taxonomyTerm.getIdentifier(), 
			taxonomyTerm);		
	}
	
	public TaxonomyTerm getTerm(final String termIdentifier) {

		if (termIdentifier == null) {
			return null;			
		}
		
		Collator collator = GENERIC_MESSAGES.getCollator();

		TaxonomyTerm targetTerm = null;
		for (TaxonomyTerm term : allTerms) {			
			if (collator.equals(term.getLabel(), termIdentifier)) {
				targetTerm = term;
				break;
			}
		}

		return targetTerm;		
		
	}

	public List<TaxonomyTerm> getRootTerms() {
		return rootTerms;
	}
	
	/**
	 * Gets the terms containing phrase.
	 *
	 * @param searchPhrase the search phrase
	 * @return the terms containing phrase
	 */
	public List<TaxonomyTerm> getMatchingTerms(final String searchPhrase,
			final boolean isCaseSensitive) {
		
		Pattern searchPattern;
		
		if (isCaseSensitive) {
			searchPattern = Pattern.compile(".*" + searchPhrase + ".*");
		}
		else {
			searchPattern = Pattern.compile(".*" + searchPhrase + ".*", Pattern.CASE_INSENSITIVE);
		}

		return allTerms.stream().filter(
				taxonomyTerm -> searchPattern.matcher(taxonomyTerm.getDescription().trim()).matches()
				             || searchPattern.matcher(taxonomyTerm.getLabel().trim()).matches())
				       .collect(Collectors.toList());
	}
	
	public List<TaxonomyTerm> getImmediateChildTerms(final String parentTermIdentifier)
			throws RIFServiceException {
		
		List<TaxonomyTerm> results = new ArrayList<>();
		if (parentTermIdentifier == null) {
			return results;
		}
		
		TaxonomyTerm parentTerm = getTerm(parentTermIdentifier);
		if (parentTerm == null) {
			RIFServiceExceptionFactory rifServiceExceptionFactory
				= new RIFServiceExceptionFactory();
			throw rifServiceExceptionFactory.createNonExistentTaxonomyTerm(
				taxonomyServiceID, 
				parentTermIdentifier);
		}
		return parentTerm.getChildTerms();
	}
	
	public TaxonomyTerm getParentTerm(final String childTermIdentifier) 
		throws RIFServiceException {
		
		if (childTermIdentifier == null) {
			return null;
		}
		
		TaxonomyTerm childTerm
			= getTerm(childTermIdentifier);
		if (childTerm == null) {
			RIFServiceExceptionFactory rifServiceExceptionFactory
				= new RIFServiceExceptionFactory();
			throw rifServiceExceptionFactory.createNonExistentTaxonomyTerm(
				taxonomyServiceID, 
				childTermIdentifier);
		}
		
		return childTerm.getParentTerm();
	}
}
