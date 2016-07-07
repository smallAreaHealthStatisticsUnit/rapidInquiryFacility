package rifGenericLibrary.taxonomyServices;

import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.system.RIFServiceException;


import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

public class TaxonomyTermManager {

	// ==========================================
	// Section Constants
	// ==========================================

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

	public TaxonomyTermManager(final String taxonomyServiceID) {
		this.taxonomyServiceID = taxonomyServiceID;
		rootTerms = new ArrayList<TaxonomyTerm>();
		allTerms = new ArrayList<TaxonomyTerm>();
		termFromIdentifier = new HashMap<String, TaxonomyTerm>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
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
		
		Collator collator = RIFGenericLibraryMessages.getCollator();

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
			Matcher patternCodeMatcher
				= searchPattern.matcher(term.getLabel());
			if (patternCodeMatcher.matches()) {
				results.add(term);
			}
			else {			
				Matcher patternDescriptionMatcher
					= searchPattern.matcher(term.getDescription());
				if (patternDescriptionMatcher.matches()) {
					results.add(term);
				}
			}
		}
		
		return results;
	}
	
	public ArrayList<TaxonomyTerm> getImmediateChildTerms(
		final String parentTermIdentifier) 
		throws RIFServiceException {
		
		TaxonomyTerm parentTerm
			= getTerm(parentTermIdentifier);
		if (parentTerm == null) {
			RIFServiceExceptionFactory rifServiceExceptionFactory
				= new RIFServiceExceptionFactory();
			rifServiceExceptionFactory.createNonExistentTaxonomyTerm(
				taxonomyServiceID, 
				parentTermIdentifier);
		}
		return parentTerm.getSubTerms();
	}
	
	public TaxonomyTerm getParentTerm(final String childTermIdentifier) {
		if (childTermIdentifier == null) {
			return TaxonomyTerm.NULL_TERM;
		}
		
		TaxonomyTerm childTerm
			= getTerm(childTermIdentifier);
		if (childTerm == null) {
			RIFServiceExceptionFactory rifServiceExceptionFactory
				= new RIFServiceExceptionFactory();
			rifServiceExceptionFactory.createNonExistentTaxonomyTerm(
				taxonomyServiceID, 
				childTermIdentifier);
		}
		
		TaxonomyTerm parentTerm = TaxonomyTerm.NULL_TERM;
		if (childTerm.getParentTerm() != null) {
			return TaxonomyTerm.NULL_TERM;
		}
		else {
			return parentTerm;
		}
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
