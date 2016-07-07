package taxonomyServices;

import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.taxonomyServices.TaxonomyServiceAPI;
import rifGenericLibrary.taxonomyServices.TaxonomyServiceConfiguration;
import rifGenericLibrary.taxonomyServices.TaxonomyTerm;
import taxonomyServices.system.TaxonomyServiceError;

import java.io.File;

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

public class ICDTaxonomyService 
	implements TaxonomyServiceAPI {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private final String identifier = "icd10";
	private final String nameSpace = "icd10";
	private final String name = "ICD 10 Taxonomy Service";
	private final String version = "1.0";
	private final String description = "provides diagnostic codes for the ICD 10 classification";
	private boolean isServiceWorking;
	
	private final ICD10ClaMLTaxonomyProvider icd10TaxonomyParser 
		= new ICD10ClaMLTaxonomyProvider(identifier);
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ICDTaxonomyService() {
		isServiceWorking = false;
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

	//Interface TaxonomyServiceAPI

	public void initialiseService(
		final String defaultResourceDirectoryPath,
		final TaxonomyServiceConfiguration taxonomyServiceConfiguration) 
		throws RIFServiceException {
		
		try {
			ArrayList<Parameter> parameters
				= taxonomyServiceConfiguration.getParameters();
		
			Parameter icd10FileParameter
				= Parameter.getParameter("icd10_ClaML_file", parameters);
			
			if (icd10FileParameter == null) {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage
					= "ICD10 taxonomy service is missing a parameter for \"icd10_ClaML_file\"";
				RIFServiceException rifServiceException
					= new RIFServiceException(
						TaxonomyServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
						errorMessage);
				throw rifServiceException;
			}		
		
			StringBuilder icd10FileName = new StringBuilder();
			icd10FileName.append(defaultResourceDirectoryPath);
			icd10FileName.append(File.separator);
			icd10FileName.append(icd10FileParameter.getValue());
			
			File icd10File = new File(icd10FileName.toString());
			icd10TaxonomyParser.initialise(icd10File);
			isServiceWorking = true;
		}
		catch(RIFServiceException rifServiceException) {
			isServiceWorking = false;			
		}
	}

	public boolean isServiceWorking() {
		return isServiceWorking;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getNameSpace() {
		return nameSpace;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getVersion() {
		return version;
	}

	
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
		throws RIFServiceException {
		
		return icd10TaxonomyParser.getMatchingTerms(searchText, isCaseSensitive);
	}

	/**
	 * Gets the top level codes.
	 *
	 * @param connection the connection
	 * @return the top level codes
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<TaxonomyTerm> getRootTerms() 
		throws RIFServiceException {

		return icd10TaxonomyParser.getRootTerms();
	}
	
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
		throws RIFServiceException {

		return icd10TaxonomyParser.getImmediateChildTerms(parentTermIdentifier);
	}

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
		throws RIFServiceException {
		
		return icd10TaxonomyParser.getParentTerm(childTermIdentifier);
	}
	
	public TaxonomyTerm getTerm(
		final String termIdentifier) 
		throws RIFServiceException {
		
		return icd10TaxonomyParser.getTerm(termIdentifier);
	}
	
	public boolean termExists(
		final String taxonomyTermIdentifier)
		throws RIFServiceException {
		
		
		return false;
	}
	
	
	
	
	// ==========================================
	// Section Override
	// ==========================================

}

