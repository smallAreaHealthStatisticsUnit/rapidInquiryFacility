
package org.sahsu.rif.services.ontologyservices;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.rif.services.concepts.HealthCodeTaxonomy;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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


final class XMLHealthCodeTaxonomyContentHandler 
	extends DefaultHandler {

// ==========================================
// Section Constants
// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
	/** The is valid rif health code provider. */
	private boolean isValidRifHealthCodeProvider;
	
	/** The collator. */
	private Collator collator;
	
	/** The health code taxonomy. */
	private HealthCodeTaxonomy healthCodeTaxonomy;
	
	/** The parent terms. */
	private Stack<TaxonomyTerm> parentTerms;
	
	/** The current term. */
	private TaxonomyTerm currentTerm;
	
	/** The current field value. */
	private String currentFieldValue;
		
	/** The all terms. */
	private ArrayList<TaxonomyTerm> allTerms;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new XML health code taxonomy content handler.
     */
	public XMLHealthCodeTaxonomyContentHandler() {
    	isValidRifHealthCodeProvider = false;
		collator = GENERIC_MESSAGES.getCollator();
		parentTerms = new Stack<TaxonomyTerm>();
		
		allTerms = new ArrayList<TaxonomyTerm>();
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
		final File healthCodeListFile) 
		throws RIFServiceException {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(healthCodeListFile, this);
		}
		catch(Exception exception) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"io.error.problemReadingHealthCodes",
					healthCodeListFile.getName());
			throw new RIFServiceException(
					RIFServiceError.XML_PROBLEM_READING_HEALTH_CODE_TAXONOMY,
					errorMessage,
					exception);
		}
	}
	
	/**
	 * Gets the health code taxonomy.
	 *
	 * @return the health code taxonomy
	 */
	public HealthCodeTaxonomy getHealthCodeTaxonomy() {
		
		return healthCodeTaxonomy;
	}
	
	/**
	 * Gets the terms containing phrase.
	 *
	 * @param searchPhrase the search phrase
	 * @return the terms containing phrase
	 */
	public ArrayList<TaxonomyTerm> getTermsContainingPhrase(
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
	
	/**
	 * Gets the root terms.
	 *
	 * @return the root terms
	 */
	public ArrayList<TaxonomyTerm> getRootTerms() {
		
		ArrayList<TaxonomyTerm> results = new ArrayList<TaxonomyTerm>();
		for (TaxonomyTerm term : allTerms) {
			if (term.getParentTerm() == null) {
				results.add(term);
			}
		}
		
		return results;
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
	public int getNumberOfTerms() {
		
		return allTerms.size();
	}
	
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
	
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param parentTermLabel the parent term label
	 * @param parentTermNameSpace the parent term name space
	 * @return the immediate subterms
	 */
	public ArrayList<TaxonomyTerm> getImmediateSubterms(
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
	public TaxonomyTerm getParentHealthCode(
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
	public TaxonomyTerm getTerm(
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
	
	/**
	 * Checks if is valid rif health code provider.
	 *
	 * @return true, if is valid rif health code provider
	 */
	public boolean isValidRifHealthCodeProvider() {
		
		return isValidRifHealthCodeProvider;
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
		
		if (collator.equals(currentTagName, definedTagName)) {
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
		
		if (matchesTagName(qualifiedName, "rif_health_code_provider")) {
			isValidRifHealthCodeProvider = true;
		}
		if (matchesTagName(qualifiedName, "provider_information")) {
			healthCodeTaxonomy = HealthCodeTaxonomy.newInstance();		
		}
		else if (matchesTagName(qualifiedName, "term")) {
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
		
		if (matchesTagName(qualifiedName, "provider_name")) {
			healthCodeTaxonomy.setName(getCurrentFieldValue());
		}
		else if (matchesTagName(qualifiedName, "provider_description")) {
			healthCodeTaxonomy.setDescription(getCurrentFieldValue());
		}
		else if (matchesTagName(qualifiedName, "provider_name_space")) {
			healthCodeTaxonomy.setNameSpace(getCurrentFieldValue());
		}		
		else if (matchesTagName(qualifiedName, "provider_version")) {
			healthCodeTaxonomy.setVersion(getCurrentFieldValue());
		}
		else if (matchesTagName(qualifiedName, "term")) {
			TaxonomyTerm finishedTerm = parentTerms.pop();
			finishedTerm.setNameSpace(healthCodeTaxonomy.getNameSpace());
			if (parentTerms.isEmpty() == false) {
				TaxonomyTerm parentTerm = parentTerms.peek();
				parentTerm.addChildTerm(finishedTerm);
				finishedTerm.setParentTerm(parentTerm);
			}

			allTerms.add(finishedTerm);
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
