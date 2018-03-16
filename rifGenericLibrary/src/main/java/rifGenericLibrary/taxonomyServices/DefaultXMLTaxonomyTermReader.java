package rifGenericLibrary.taxonomyServices;

import java.io.File;
import java.text.Collator;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifGenericLibrary.system.RIFServiceException;


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

public class DefaultXMLTaxonomyTermReader 
	extends DefaultHandler {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The current field value. */
	private String currentFieldValue;
	
	/** The collator. */
	private Collator collator;
	
	private TaxonomyTermManager taxonomyTermManager;
		
	/** The parent terms. */
	private Stack<TaxonomyTerm> parentTerms;
	
	/** The current term. */
	private TaxonomyTerm currentTerm;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public DefaultXMLTaxonomyTermReader() {
		collator = Messages.genericMessages().getCollator();
		taxonomyTermManager = TaxonomyTermManager.newInstance();
		parentTerms = new Stack<TaxonomyTerm>();

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setTaxonomyServiceIdentifier(final String taxonomyServiceID) {
		taxonomyTermManager.setTaxonomyServiceIdentifier(taxonomyServiceID);
	}
	
	public TaxonomyTermManager getTaxonomyTermManager() {
		return taxonomyTermManager;
	}
	
	
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
		}
		catch(Exception exception) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"defaultXMLTaxonomyService.error.unableToReadFile",
					taxonomyTermsFile.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.DEFAULT_XML_TAXONOMY_READ_FILE_ERROR, 
					errorMessage);
			
			throw rifServiceException;
		}
	}

	
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

		if (matchesTagName(qualifiedName, "terms")) {
			//we have reached the end of the file.  
			
			taxonomyTermManager.determineRootTerms();
		}
		else if (matchesTagName(qualifiedName, "term")) {
			TaxonomyTerm finishedTerm = parentTerms.pop();

			if (parentTerms.isEmpty() == false) {
				TaxonomyTerm parentTerm = parentTerms.peek();
				parentTerm.addChildTerm(finishedTerm);
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
	 * Gets the current field value.
	 *
	 * @return the current field value
	 */
	private String getCurrentFieldValue() {
		
		return currentFieldValue;
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
