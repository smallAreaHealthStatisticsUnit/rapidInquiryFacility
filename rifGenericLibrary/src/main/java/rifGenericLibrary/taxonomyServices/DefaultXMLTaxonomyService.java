
package rifGenericLibrary.taxonomyServices;

import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceExceptionFactory;
import rifGenericLibrary.taxonomyServices.TaxonomyTerm;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


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


final class DefaultXMLTaxonomyService 
	extends DefaultHandler
	implements TaxonomyServiceAPI {
		
// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	private boolean isServiceWorking;
	
	private String serviceIdentifier;
	private String serviceName;
	private String serviceDescription;
	private String serviceNameSpace;
	private String serviceVersion;
	
	/** The collator. */
	private Collator collator;
	
	/** The parent terms. */
	private Stack<TaxonomyTerm> parentTerms;
	
	/** The current term. */
	private TaxonomyTerm currentTerm;
	
	/** The current field value. */
	private String currentFieldValue;
		
	
	//private ArrayList<TaxonomyTerm> rootTerms;
	private TaxonomyTermManager taxonomyTermManager;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new XML health code taxonomy content handler.
     */
	public DefaultXMLTaxonomyService() {
		collator = RIFGenericLibraryMessages.getCollator();
		parentTerms = new Stack<TaxonomyTerm>();
		isServiceWorking = false;
    }
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	/**
	 * Read file.
	 *
	 * @param healthCodeListFile the health code list file
	 * @throws RIFServiceException the RIF service exception
	 */
	public void readFile(
		final File taxonomyTermsFile) 
		throws RIFServiceException {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(taxonomyTermsFile, this);
			isServiceWorking = true;
		}
		catch(Exception exception) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"defaultXMLTaxonomyService.error.unableToReadFile",
					taxonomyTermsFile.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DEFAULT_XML_TAXONOMY_READ_FILE_ERROR, 
					errorMessage);
			isServiceWorking = false;
			
			throw rifServiceException;
		}
	}
	
	/**
	 * Gets the current field value.
	 *
	 * @return the current field value
	 */
	private String getCurrentFieldValue() {
		
		return currentFieldValue;
	}
	
	/**
	 * Sets the current field value.
	 *
	 * @param currentFieldValue the new current field value
	 */
	private void setCurrentFieldValue(
		final String currentFieldValue) {
		
		this.currentFieldValue = currentFieldValue;
	}
    
	/**
	 * Gets the number of terms.
	 *
	 * @return the number of terms
	 */
/*	
	public int getNumberOfTerms() {
		
		return allTerms.size();
	}
*/
	
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
		ArrayList<TaxonomyTerm> subTerms = taxonomyTerm.getSubTerms();
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

		synchronized(this) {
			
			serviceIdentifier 
				= taxonomyServiceConfiguration.getServiceIdentifier();
			serviceName
				= taxonomyServiceConfiguration.getName();
			serviceDescription
				= taxonomyServiceConfiguration.getDescription();
			serviceVersion
				= taxonomyServiceConfiguration.getVersion();

			taxonomyTermManager = new TaxonomyTermManager(serviceIdentifier);

			String fileName = "";
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			try {
				ArrayList<Parameter> pms = taxonomyServiceConfiguration.getParameters();
				System.out.println("DefaultXMLTaxonomyService number of parms=="+pms.size()+"==");
				for (Parameter pm : pms) {
					System.out.println("Parm:=="+pm.getDisplayName()+"==");
				}
				
				Parameter termFileParameter
					= Parameter.getParameter(
						"term_file", 
						taxonomyServiceConfiguration.getParameters());
				if (termFileParameter == null) {
					System.out.println("initService 11111");
					throw exceptionFactory.createNonExistentParameter("term_file");
				}
					
				String filePath
					= getFilePath(
						defaultResourceDirectoryPath,
						termFileParameter.getValue());
				File termFile = new File(filePath);
				if (termFile.exists() == false) {
					throw exceptionFactory.createNonExistentFile(fileName);
				}
	
				//Parse file containing terms
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(termFile, this);
				taxonomyTermManager.determineRootTerms();
				isServiceWorking = true;
			}
			catch(Exception exception) {
				System.out.println("DefaultXMLTaxonomyService defaultPath=="+defaultResourceDirectoryPath+"==service=="+this.getIdentifier()+"==");
				System.out.println("DefaultXMLTaxonomyService description=="+this.getDescription()+"==");
				exception.printStackTrace(System.out);
				isServiceWorking = false;
				throw exceptionFactory.createFileReadingProblemException(
					fileName);
			}		
		}
	}

	public boolean isServiceWorking() {
		synchronized(this) {
			return isServiceWorking;
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
		
		
	public String getIdentifier() {
		return serviceIdentifier;
	}
	
	public String getNameSpace() {
		return serviceNameSpace;
	}
	
	public String getName() {
		return serviceName;
	}
	
	public String getDescription() {
		return serviceDescription;
	}
	
	public String getVersion() {
		return serviceVersion;
	}
	
	
	/**
	 * Gets the root terms.
	 *
	 * @return the root terms
	 */
	public ArrayList<TaxonomyTerm> getRootTerms() {
		
		return taxonomyTermManager.getRootTerms();
	}
      
	public ArrayList<TaxonomyTerm> getMatchingTerms(
		final String searchPhrase,
		final boolean isCaseSensitive) {
	
		return taxonomyTermManager.getMatchingTerms(
			searchPhrase, 
			isCaseSensitive);
	}
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param parentTermIdentifier the parent term label
	 * @param parentTermNameSpace the parent term name space
	 * @return the immediate subterms
	 */
	public ArrayList<TaxonomyTerm> getImmediateChildTerms(
		final String parentTermIdentifier) {
		
		TaxonomyTerm parentTerm
			= getTerm(parentTermIdentifier);
		if (parentTerm == null) {
			ArrayList<TaxonomyTerm> results = new ArrayList<TaxonomyTerm>();
			return results;
		}
		
		return parentTerm.getSubTerms();		
	}
	
	
	/**
	 * Gets the parent term
	 *
	 * @param childTermLabel the child term label
	 * @param childTermNameSpace the child term name space
	 * @return the parent term
	 * @throws RIFServiceException the RIF service exception
	 */
	public TaxonomyTerm getParentTerm(
		final String childTermIdentifier)
		throws RIFServiceException {

		TaxonomyTerm childTerm
			= getTerm(childTermIdentifier);
		if (childTerm == null) {
			return null;
		}
		
		return childTerm.getParentTerm();
	}

	
	/**
	 * Gets the term.
	 *
	 * @param termIdentifier the label
	 * @param nameSpace the name space
	 * @return the term
	 */
	public TaxonomyTerm getTerm(
		final String termIdentifier) {
		
		return taxonomyTermManager.getTerm(termIdentifier);
	}

	public boolean termExists(
		final String taxonomyTermIdentifier)
		throws RIFServiceException {
		
		TaxonomyTerm term = getTerm(taxonomyTermIdentifier);
		if (term == null) {
			return false;
		}
		return true;
	}
				
				
// ==========================================
// Section Override
// ==========================================
	
	/**
	 * Matches tag name.
	 *
	 * @param currentTagName the current tag name
	 * @param definedTagName the defined tag name
	 * @return true, if successful
	 */
	private boolean matchesTagName(
		final String currentTagName, 
		final String definedTagName) {
		
		if (collator.equals(
				currentTagName, 
				definedTagName)) {
			return true;
		}	
		return false;
	}
	

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {
		
		if (matchesTagName(qualifiedName, "term")) {
			currentTerm = TaxonomyTerm.newInstance();
			parentTerms.push(currentTerm);
		}
	}
	

	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

		if (matchesTagName(qualifiedName, "service_identifier")) {
			serviceIdentifier = getCurrentFieldValue();
		}
		else if (matchesTagName(qualifiedName, "service_name")) {
			serviceName = getCurrentFieldValue();
		}
		else if (matchesTagName(qualifiedName, "service_description")) {
			serviceDescription = getCurrentFieldValue();
		}
		else if (matchesTagName(qualifiedName, "service_name_space")) {
			serviceNameSpace = getCurrentFieldValue();
		}		
		else if (matchesTagName(qualifiedName, "service_version")) {
			serviceVersion = getCurrentFieldValue();
		}
		else if (matchesTagName(qualifiedName, "term")) {
			TaxonomyTerm finishedTerm = parentTerms.pop();
			finishedTerm.setNameSpace(serviceNameSpace);
			if (parentTerms.isEmpty() == false) {
				TaxonomyTerm parentTerm = parentTerms.peek();
				parentTerm.addSubTerm(finishedTerm);
				finishedTerm.setParentTerm(parentTerm);
			}

			taxonomyTermManager.addTerm(finishedTerm);
		}		
		else if (matchesTagName(qualifiedName, "term_name")) {
			currentTerm.setLabel(getCurrentFieldValue());
		}
		else if (matchesTagName(qualifiedName, "term_description")) {
			currentTerm.setDescription(getCurrentFieldValue());			
		}
	}
	

	@Override
	public void characters(
		final char[] characters, 
		final int start, 
		final int length)
		throws SAXException {
		
		setCurrentFieldValue(new String(characters, start, length)); 
	}

}
