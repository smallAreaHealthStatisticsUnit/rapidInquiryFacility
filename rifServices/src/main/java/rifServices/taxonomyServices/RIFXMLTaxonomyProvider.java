package rifServices.taxonomyServices;


import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.businessConceptLayer.Parameter;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;


/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
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

public class RIFXMLTaxonomyProvider 
	implements HealthCodeProviderInterface {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The taxonomy reader. */
	private XMLHealthCodeTaxonomyContentHandler taxonomyReader;

	/** The health code taxonomy. */
	private HealthCodeTaxonomy healthCodeTaxonomy;
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new RIFXML taxonomy provider.
	 */
	public RIFXMLTaxonomyProvider() {
		
		taxonomyReader = new XMLHealthCodeTaxonomyContentHandler();
	}

	public void initialise(
		final ArrayList<Parameter> parameters) 
		throws RIFServiceException {

		String inputFileParameterValue
			= getParameterValue("input_file", parameters);
		if (inputFileParameterValue == null) {
			String errorMessage
				= RIFServiceMessages.getMessage("rifXMLTaxonomyProvider.error.noInputFileSpecified");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.XML_TAXONOMY_READER_NO_INPUT_FILE_SPECIFIED, 
					errorMessage);
			throw rifServiceException;
		}
		
		File healthCodeListFile
			= new File(inputFileParameterValue.toString());
		taxonomyReader.readFile(healthCodeListFile);
		healthCodeTaxonomy = taxonomyReader.getHealthCodeTaxonomy();
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the parameter value.
	 *
	 * @param parameterName the parameter name
	 * @param parameters the parameters
	 * @return the parameter value
	 */
	public String getParameterValue(
		final String parameterName,
		final ArrayList<Parameter> parameters) {
	
		Collator collator = RIFServiceMessages.getCollator();
		for (Parameter parameter : parameters) {
			String currentParameterName
				= parameter.getName();
			if (collator.equals(parameterName, currentParameterName)) {
				return parameter.getValue();
			}
		}
		
		return null;
	}

	public HealthCodeTaxonomy getHealthCodeTaxonomy() {
		
		return healthCodeTaxonomy;
	}

	public boolean supportsTaxonomy(
		final HealthCodeTaxonomy otherHealthCodeTaxonomy) {

		Collator collator = RIFServiceMessages.getCollator();
		
		String nameSpace = healthCodeTaxonomy.getNameSpace();		
		String otherNameSpace = otherHealthCodeTaxonomy.getNameSpace();
		
		if (collator.equals(nameSpace, otherNameSpace)) {
			return true;
		}
		
		return false;
	}
	
	public boolean supportsTaxonomy(
		final HealthCode healthCode) {

		Collator collator = RIFServiceMessages.getCollator();
		
		String nameSpace = healthCodeTaxonomy.getNameSpace();		
		String otherNameSpace = healthCode.getNameSpace();
		
		if (collator.equals(nameSpace, otherNameSpace)) {
			return true;
		}
		
		return false;
	}

	public ArrayList<HealthCode> getHealthCodes(
		final String searchText,
		final boolean isCaseSensitive) 
		throws RIFServiceException {
		
		ArrayList<TaxonomyTerm> searchTerms
			= taxonomyReader.getTermsContainingPhrase(
				searchText,
				isCaseSensitive);
		return convertToHealthCodes(searchTerms);
	}

	public ArrayList<HealthCode> getTopLevelCodes()
		throws RIFServiceException {
				
		ArrayList<TaxonomyTerm> taxonomyTerms
			= taxonomyReader.getRootTerms();
		
		taxonomyReader.printTerms();
		return convertToHealthCodes(taxonomyTerms);
	}

	/**
	 * Convert to health codes.
	 *
	 * @param taxonomyTerms the taxonomy terms
	 * @return the array list
	 */
	private ArrayList<HealthCode> convertToHealthCodes(
		final ArrayList<TaxonomyTerm> taxonomyTerms) {
		
		ArrayList<HealthCode> healthCodes = new ArrayList<HealthCode>();
		for (TaxonomyTerm taxonomyTerm : taxonomyTerms) {
			HealthCode healthCode = HealthCode.newInstance();
			healthCode.setCode(taxonomyTerm.getLabel());
			healthCode.setDescription(taxonomyTerm.getDescription());
			healthCode.setNameSpace(taxonomyTerm.getNameSpace());
			healthCodes.add(healthCode);
			healthCode.setNumberOfSubTerms(taxonomyTerm.getSubTerms().size());
		}
		return healthCodes;
	}
	
	public ArrayList<HealthCode> getImmediateSubterms(
		final HealthCode parentHealthCode) 
		throws RIFServiceException {
		
		checkHealthCodeExists(
			parentHealthCode.getCode(), 
			parentHealthCode.getNameSpace());
		
		
		ArrayList<TaxonomyTerm> results
			= taxonomyReader.getImmediateSubterms(
				parentHealthCode.getCode(), 
				parentHealthCode.getNameSpace());
				
		return convertToHealthCodes(results);
	}

	public HealthCode getParentHealthCode(
		final HealthCode childHealthCode) 
		throws RIFServiceException {
		
		checkHealthCodeExists(
			childHealthCode.getCode(), 
			childHealthCode.getNameSpace());
		
		TaxonomyTerm parentTerm
			= taxonomyReader.getParentHealthCode(
				childHealthCode.getCode(), 
				childHealthCode.getNameSpace());
		if (parentTerm == null) {
			return null;
		}
		
		HealthCode parentHealthCode = HealthCode.newInstance();
		parentHealthCode.setCode(parentTerm.getLabel());
		parentHealthCode.setDescription(parentTerm.getDescription());
		parentHealthCode.setNameSpace(parentTerm.getNameSpace());
		if (parentTerm.getParentTerm() == null) {
			parentHealthCode.setTopLevelTerm(true);
		}
		else {
			parentHealthCode.setTopLevelTerm(false);			
		}
		return parentHealthCode;
	}
	
	
	public HealthCode getHealthCode(
		final String code,
		final String nameSpace) throws RIFServiceException {
		
		TaxonomyTerm term
			= taxonomyReader.getTerm(
				code, 
				nameSpace);

		if (term == null) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthCodeProvider.error.nonExistentHealthCode", 
					code, 
					healthCodeTaxonomy.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.HEALTH_CODE_NOT_KNOWN_TO_PROVIDER, 
					errorMessage);
			throw rifServiceException;
		}
		
		HealthCode healthCode = HealthCode.newInstance();
		healthCode.setCode(code);
		healthCode.setDescription(term.getDescription());
		healthCode.setNameSpace(nameSpace);
		if (term.getParentTerm() == null) {
			healthCode.setTopLevelTerm(true);
		}
		else {
			healthCode.setTopLevelTerm(false);			
		}
		
		return healthCode;
		
	}
	
	public boolean healthCodeExists(
		final HealthCode healthCode) 
		throws RIFServiceException {
		
		TaxonomyTerm term
			= taxonomyReader.getTerm(
				healthCode.getCode(), 
				healthCode.getNameSpace());
		if (term == null) {
			return false;
		}
		
		return true;		
	}
	
	private void checkHealthCodeExists(
		final String code, 
		final String nameSpace) 
		throws RIFServiceException {
		
		TaxonomyTerm term
			= taxonomyReader.getTerm(
				code, 
				nameSpace);
		if (term == null) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"healthCodeProvider.error.nonExistentHealthCode", 
					code, 
					healthCodeTaxonomy.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.HEALTH_CODE_NOT_KNOWN_TO_PROVIDER, 
					errorMessage);
			throw rifServiceException;
		}		
	}
	

	/**
	 * Gets the number of terms.
	 *
	 * @return the number of terms
	 */
	public int getNumberOfTerms() {
		
		return taxonomyReader.getNumberOfTerms();
		
	}
	
	/**
	 * Prints the terms.
	 */
	public void printTerms() {
		
		taxonomyReader.printTerms();
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
