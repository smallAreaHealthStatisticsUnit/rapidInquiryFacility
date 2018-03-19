package rifGenericLibrary.taxonomyServices;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceExceptionFactory;

/**
 * A convenience class to manage a collection of 
 * {@link rifGenericLibrary.taxonomyService.TaxonomyTerm} objects in-memory.  The 
 * main reason for developing it is to separate the code used to exercise relationships
 * between instances of <code>TaxonomyTerm</code> objects from the code used to 
 * parse them from whatever data format that is used to serialise them.  In practice,
 * we expect that taxonomy services will not vary much in their implementations, other 
 * than how they initially pull out collections of terms from text files.  Once they 
 * do that, they can register the terms with an instance of this class.  The
 * <code>TaxonomyTermManager</code> can then provide most of the code used to support
 * methods that are expected by the 
 * {@link rifGenericLibrary.taxonomyServices.TaxonomyServiceAPI} interface.
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

public class TaxonomyTermManager {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private String taxonomyServiceID;
	private ArrayList<TaxonomyTerm> rootTerms;
	/** The all terms. */
	private ArrayList<TaxonomyTerm> allTerms;
	
	private HashMap<String, TaxonomyTerm> termFromIdentifier;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private TaxonomyTermManager(final String taxonomyServiceID) {
		init(taxonomyServiceID);		
	}

	public static TaxonomyTermManager newInstance() {
		TaxonomyTermManager taxonomyTermManager
			= new TaxonomyTermManager("");
		return taxonomyTermManager;
	}
	
	public static TaxonomyTermManager newInstance(
		final String taxonomyServiceID) {
		
		TaxonomyTermManager taxonomyTermManager
			= new TaxonomyTermManager(taxonomyServiceID);
		return taxonomyTermManager;		
	}

	private void init(final String taxonomyServiceID) {
		this.taxonomyServiceID = taxonomyServiceID;
		rootTerms = new ArrayList<TaxonomyTerm>();
		allTerms = new ArrayList<TaxonomyTerm>();
		termFromIdentifier = new HashMap<String, TaxonomyTerm>();
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setTaxonomyServiceIdentifier(final String taxonomyServiceID) {
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
	
	public boolean termExists(final String termIdentifier) {
		if (termFromIdentifier.get(termIdentifier) == null) {
			return false;
		}
		return true;
	}

	public int getNumberOfTerms() {
		
		return allTerms.size();
	}
	
	public ArrayList<TaxonomyTerm> getRootTerms() {
		return rootTerms;
	}
	
	/**
	 * Gets the terms containing phrase.
	 *
	 * @param searchPhrase the search phrase
	 * @return the terms containing phrase
	 */
	public ArrayList<TaxonomyTerm> getMatchingTerms(
		final String searchPhrase,
		final boolean isCaseSensitive) {
		
		/*
		 * Using regular expressions to do a contains than a string.contains(...)
		 * feature.  
		 */
		
		Pattern searchPattern;
		
		if (isCaseSensitive) {
			searchPattern
				= Pattern.compile(".*"+searchPhrase+".*");
		}
		else {
			searchPattern
				= Pattern.compile(".*"+searchPhrase+".*", Pattern.CASE_INSENSITIVE);
		}
				
		ArrayList<TaxonomyTerm> results = new ArrayList<TaxonomyTerm>();
		for (TaxonomyTerm term : allTerms) {
			Matcher descriptionMatcher
				= searchPattern.matcher(term.getDescription());			
			if (descriptionMatcher.matches()) {
				results.add(term);
			}
			else {
				Matcher labelMatcher
					= searchPattern.matcher(term.getLabel());
				if (labelMatcher.matches()) {
					results.add(term);
				}				
			}
		}
		
		return results;
	}
	
	public ArrayList<TaxonomyTerm> getImmediateChildTerms(
		final String parentTermIdentifier) 
		throws RIFServiceException {
		
		ArrayList<TaxonomyTerm> results
			= new ArrayList<TaxonomyTerm>();
		if (parentTermIdentifier == null) {
			return results;
		}
		
		TaxonomyTerm parentTerm
			= getTerm(parentTermIdentifier);
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
	
	public int getTotalChildren() {
		return allTerms.size();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
