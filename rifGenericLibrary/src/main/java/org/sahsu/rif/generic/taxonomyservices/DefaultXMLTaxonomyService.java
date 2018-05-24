
package org.sahsu.rif.generic.taxonomyservices;

import org.sahsu.rif.generic.util.TaxonomyLogger;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;

import java.io.File;
import java.util.ArrayList;

import java.util.Map;

/**
 *
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
 * Copyright 2017 Imperial College London, developed by the Small Area
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


final class DefaultXMLTaxonomyService 
	extends AbstractTaxonomyService
	implements TaxonomyServiceAPI {
		
// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	private TaxonomyLogger taxonomyLogger = TaxonomyLogger.getLogger();
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new XML health code taxonomy content handler.
     */
	public DefaultXMLTaxonomyService() {

    }
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
		
	/**
	 * Prints the terms.
	 */
	public void printTerms() {
		
		ArrayList<TaxonomyTerm> rootTerms = getRootTerms();
		System.out.println("Print all terms there are =="+rootTerms.size()+"== root terms");
		for (TaxonomyTerm rootTerm : rootTerms) {
			printTerm(rootTerm, 0);
		}
	}
	
	/**
	 * Prints the term.
	 *
	 * @param taxonomyTerm the taxonomy term
	 * @param indentationLevel the indentation level
	 */
	private void printTerm(
		final TaxonomyTerm taxonomyTerm, 
		final int indentationLevel) {

		StringBuilder textLine = new StringBuilder();
		for (int i = 0; i < indentationLevel; i++) {
			textLine.append("\t");
		}
		
		if (indentationLevel == 0) {
			textLine.append("ROOT:");
		}
		textLine.append(taxonomyTerm.getLabel());
		
		System.out.println(textLine.toString());
		ArrayList<TaxonomyTerm> subTerms = taxonomyTerm.getChildTerms();
		for (TaxonomyTerm subTerm : subTerms) {
			printTerm(subTerm, indentationLevel + 1);
		}
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================
	
	public void initialiseService(
		final String defaultResourceDirectoryPath,
		final TaxonomyServiceConfiguration taxonomyServiceConfiguration)
			throws RIFServiceException {

		setTaxonomyServiceConfiguration(taxonomyServiceConfiguration);

		Map<String, String> environmentalVariables = System.getenv();		
		String catalinaHome = environmentalVariables.get("CATALINA_HOME");
		if (catalinaHome == null) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					"taxonomyServices.error.initialisationFailure",  
					"CATALINA_HOME not set in the environment");
			taxonomyLogger.error(this.getClass(), "DefaultXMLTaxonomyService error", rifServiceException);
			throw rifServiceException;
		}
		
		String fileName = "";
		RIFServiceExceptionFactory exceptionFactory
			= new RIFServiceExceptionFactory();
		try {
				
			Parameter termFileParameter
				= Parameter.getParameter(
					"term_file", 
					taxonomyServiceConfiguration.getParameters());
			if (termFileParameter == null) {
				throw exceptionFactory.createNonExistentParameter("term_file");
			}
					
			String filePath
				= getFilePath(
					catalinaHome + File.separator + "conf",
					termFileParameter.getValue());
			File termFile = new File(filePath);
			if (termFile.exists() == false) {			
				String filePath2
					= getFilePath(
						defaultResourceDirectoryPath,
						termFileParameter.getValue());
				if (termFile.exists() == false) {	
					throw exceptionFactory.createNonExistentFile(fileName);
				}
				else {
					taxonomyLogger.info(this.getClass(), "TaxonomyService term file: " + filePath.toString());
				}
			}
			else {
				taxonomyLogger.info(this.getClass(), "TaxonomyService term file: " + filePath.toString());
			}
	
			DefaultXMLTaxonomyTermReader taxonomyTermReader
				= new DefaultXMLTaxonomyTermReader();

			taxonomyTermReader.readFile(termFile);
			taxonomyTermReader.setTaxonomyServiceIdentifier(getIdentifier());
			setTaxonomyTermManager(
				taxonomyTermReader.getTaxonomyTermManager());

			setServiceWorking(true);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			setServiceWorking(false);
			throw exceptionFactory.createFileReadingProblemException(
				fileName);
		}		
	}

	private String getFilePath(
		final String targetPathValue,
		final String baseFileName) {

		StringBuilder fileName = new StringBuilder();
		
		fileName.append(targetPathValue);
		fileName.append(File.separator);
		fileName.append(baseFileName);
		
		return fileName.toString();
	}
				
// ==========================================
// Section Override
// ==========================================

}
