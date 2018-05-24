package org.sahsu.rif.services.ontologyservices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthCodeTaxonomy;
import org.sahsu.rif.services.system.RIFServiceError;

/**
 * This is the basic outline for a taxonomy service which will read the WHO's ICD 10 data files.
 * It is not meant to be complete code but a guide that will be used to finish it later.
 * Because we can't ship ICD code files, the reference to the file path for the code file must be included as
 * a parameter during initialisation.
 * 
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

public class ICD10TaxonomyService 
	implements HealthCodeProviderInterface {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private HealthCodeTaxonomy healthCodeTaxonomy;
	private File icd10File;
	private TaxonomyTerm rootTerm;
	private ArrayList<TaxonomyTerm> allTerms;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ICD10TaxonomyService() {

		//KLG: eventually these fields should go into a properties file.		
		//We don't want to encourage hard-coding description strings in the code 
		HealthCodeTaxonomy healthCodeTaxonomy
			= HealthCodeTaxonomy.newInstance(
				"ICD 10 Taxonomy", 
				"codes for ICD10", 
				"icd10", 
					"1.0");
		
		rootTerm = TaxonomyTerm.newInstance();
		allTerms = new ArrayList<TaxonomyTerm>();
	}

	public void initialise(ArrayList<Parameter> parameters)
			throws RIFServiceException {
		// TODO Auto-generated method stub

		try {			
		
			Parameter icd10FileParameter
				= getParameter(
					"icd10_file_path",
					parameters);
			if (icd10FileParameter == null) {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage
					= "ICD10 taxonomy service is missing a parameter for \"icd10_file_path\"";
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
						errorMessage);
				throw rifServiceException;
				
			}
			
			String filePath = icd10FileParameter.getValue();		
			icd10File = new File(filePath);
		
			readICD10Codes(icd10File);		
		}
		catch(Exception exception) {
			
			//
			String errorMessage 
				= "Something wrong happened when ICD10 taxonomy service was initialised";
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
					errorMessage);
			throw rifServiceException;
		}
				
	}

	private Parameter getParameter(
		final String parameterName,
		final ArrayList<Parameter> parameters) {
		
		Collator collator = GENERIC_MESSAGES.getCollator();		
		for (Parameter parameter : parameters) {
			if (collator.equals(parameterName, parameter.getName())) {
				return parameter;
			}			
		}
		
		//parameter was not found
		return null;
	}
	
	private void readICD10Codes(
		final File icd10InputFile) 
		throws Exception {
		
		BufferedReader fileReader
			= new BufferedReader(new FileReader(icd10InputFile));
		
		try {
			String currentLine = fileReader.readLine();
			while (currentLine != null) {
				addICDTerm(currentLine);			
				currentLine = fileReader.readLine();
			}
		}
		finally {
			//cleanup resources
			fileReader.close();
		}		
	}

	private void addICDTerm(
		final String icdFileTextLine) {
		
		TaxonomyTerm taxonomyTerm = TaxonomyTerm.newInstance();
		//add this term to hierarchy of terms
		
		
		
		//somehow relate this taxonomyTerm to the rootTerm
		
		
		//also add term to a list
		allTerms.add(taxonomyTerm);
	}
	
	


	
	private boolean matchesNameSpace(
		final String otherNameSpace) {
		
		String nameSpace = healthCodeTaxonomy.getNameSpace();
		Collator collator = GENERIC_MESSAGES.getCollator();
		
		if (collator.equals(nameSpace, otherNameSpace)) {
			return true;			
		}
		
		return false;
	}

	//KLG: Check performance of recursion. This is just the first way of searching the tree I could find
	private void searchTaxonomy(
		final ArrayList<TaxonomyTerm> matchingTerms,
		final String searchPhrase,
		boolean isCaseSensitive) {
		
		
		
		
	}
		
	/**
	 * Gets the immediate subterms.
	 *
	 * @param parentTermLabel the parent term label
	 * @param parentTermNameSpace the parent term name space
	 * @return the immediate subterms
	 */
	private ArrayList<TaxonomyTerm> getImmediateSubterms(
		final String parentTermLabel,
		final String parentTermNameSpace) {
		
		TaxonomyTerm parentTerm
			= getTerm(parentTermLabel, parentTermNameSpace);
		if (parentTerm == null) {
			ArrayList<TaxonomyTerm> results = new ArrayList<TaxonomyTerm>();
			return results;
		}
		
		return parentTerm.getChildTerms();		
	}
	
	/**
	 * Gets the parent health code.
	 *
	 * @param childTermLabel the child term label
	 * @param childTermNameSpace the child term name space
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */
	private TaxonomyTerm getParentTerm(
		final String childTermLabel,
		final String childTermNameSpace)
		throws RIFServiceException {

		TaxonomyTerm childTerm
			= getTerm(childTermLabel, childTermNameSpace);
		if (childTerm == null) {
			return null;
		}
		
		return childTerm.getParentTerm();
	}
	
	/**
	 * Gets the term.
	 *
	 * @param label the label
	 * @param nameSpace the name space
	 * @return the term
	 */
	private TaxonomyTerm getTerm(
		final String label,
		final String nameSpace) {
		
		Collator collator = GENERIC_MESSAGES.getCollator();
		
		String healthTaxonomyNameSpace 
			= healthCodeTaxonomy.getNameSpace();
		if (collator.equals(
			nameSpace, 
			healthTaxonomyNameSpace) == false) {

			//child health code is from a different name space
			return null;
		}
		
		TaxonomyTerm targetTerm = null;
		for (TaxonomyTerm term : allTerms) {			
			if (collator.equals(term.getLabel(), label)) {
				targetTerm = term;
				break;
			}
		}

		return targetTerm;
	}
	
	private HealthCode convertTaxonomyTerm(
		final TaxonomyTerm taxonomyTerm) {
				
		boolean isTopLevelTerm = false;
		if (taxonomyTerm.getParentTerm() == null) {
			isTopLevelTerm = true;
		}
		
		HealthCode healthCode
			= HealthCode.newInstance(
				taxonomyTerm.getLabel(), 
				taxonomyTerm.getNameSpace(), 
				taxonomyTerm.getDescription(), 
				isTopLevelTerm);
			
		return healthCode;
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void checkTermExists(
		final HealthCode healthCode) 
		throws RIFServiceException {
		
		if (healthCodeExists(healthCode) == false) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Health Code ");
			errorMessage.append(healthCode.getCode());
			errorMessage.append(" does not exist.");
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
					errorMessage.toString());
			throw rifServiceException;
		}
		
	}

	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: HealthCodeProviderInterface

	public HealthCodeTaxonomy getHealthCodeTaxonomy() {		
		return healthCodeTaxonomy;
	}

	public boolean supportsTaxonomy(
		final HealthCodeTaxonomy otherHealthCodeTaxonomy) {

		String otherNameSpace
			= otherHealthCodeTaxonomy.getNameSpace();
		return matchesNameSpace(otherNameSpace);		
	}

	public boolean supportsTaxonomy(
		final HealthCode healthCode) {

		String otherNameSpace = healthCode.getNameSpace();
		return matchesNameSpace(otherNameSpace);		
	}
	
	public ArrayList<HealthCode> getTopLevelCodes() throws RIFServiceException {
		return null;
	}

	public ArrayList<HealthCode> getImmediateSubterms(
		final HealthCode parentHealthCode) throws RIFServiceException {		
		checkTermExists(parentHealthCode);
		TaxonomyTerm parentTerm 
			= getTerm(
				parentHealthCode.getCode(), 
				parentHealthCode.getNameSpace());
		
		
		ArrayList<TaxonomyTerm> immediateSubTerms
			= parentTerm.getChildTerms();
		
		
		return null;
	}

	
	public ArrayList<HealthCode> getHealthCodes(
		final String searchText,
		final boolean isCaseSensitive) throws RIFServiceException {

		
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
		
		
		
		
		
		
		
		return results;
	}
	
	public HealthCode getParentHealthCode(
		final HealthCode childHealthCode)
		throws RIFServiceException {

		checkTermExists(childHealthCode);
		return null;
	}

	public HealthCode getHealthCode(
		final String code, 
		final String nameSpace)
		throws RIFServiceException {

		return null;
	}

	public boolean healthCodeExists(
		final HealthCode healthCode)
		throws RIFServiceException {

		return false;
	}
	
	
	// ==========================================
	// Section Override
	// ==========================================
}
