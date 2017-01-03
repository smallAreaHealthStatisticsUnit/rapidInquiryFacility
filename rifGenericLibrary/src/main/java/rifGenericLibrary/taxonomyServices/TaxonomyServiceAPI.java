package rifGenericLibrary.taxonomyServices;

import java.util.ArrayList;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.businessConceptLayer.Parameter;

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

public interface TaxonomyServiceAPI {

	public void initialiseService(
		final String defaultResourceDirectoryPath,
		final TaxonomyServiceConfiguration taxonomyServiceConfiguration) 
		throws RIFServiceException;
	public boolean isServiceWorking();
	
	//public boolean hasInitialisationBegun();
	public String getIdentifier();
	public String getName();
	public String getDescription();
	public String getVersion();

	
	/**
	 * Gets the health codes.
	 *
	 * @param connection the connection
	 * @param searchText the search text
	 * @param isCaseSensitive
	 * @return the child terms
	 * @throws RIFServiceException the RIF service exception
	 */	
	public ArrayList<TaxonomyTerm> getMatchingTerms(
		final String searchText,
		final boolean isCaseSensitive)
		throws RIFServiceException;	

	/**
	 * Gets the top level codes.
	 *
	 * @param connection the connection
	 * @return the top level codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<TaxonomyTerm> getRootTerms() 
		throws RIFServiceException;
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param connection the connection
	 * @param parentTaxonomyTerm the parent health code
	 * @return the immediate subterms
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<TaxonomyTerm> getImmediateChildTerms(
		final String parentTermIdentifier) 
		throws RIFServiceException;

	/**
	 * Gets the parent health code.
	 *
	 * @param connection the connection
	 * @param childTaxonomyTerm the child health code
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */
	public TaxonomyTerm getParentTerm(
		final String childTermIdentifier) 
		throws RIFServiceException;
	
	public TaxonomyTerm getTerm(
		final String termIdentifier) 
		throws RIFServiceException;
	
	public boolean termExists(
		final String taxonomyTermIdentifier)
		throws RIFServiceException;
	
}
