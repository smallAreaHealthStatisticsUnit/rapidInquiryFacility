
package org.sahsu.rif.generic.fileformats;

import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.presentation.HTMLUtility;

import org.xml.sax.helpers.DefaultHandler;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;



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


abstract public class AbstractXMLContentHandler 
	extends DefaultHandler {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The html utility. */
	private HTMLUtility htmlUtility;
	
	/** The xml utility. */
	private XMLUtility xmlUtility;
	
	/** The writer. */
	private Writer writer;
	
	/** The current delegated handler. */
	private AbstractXMLContentHandler currentDelegatedHandler;
	
	/** The current field value. */
	private String currentFieldValue;
	
	/** The is active. */
	private boolean isActive;
	
	/** The singular xml tag. */
	private String singularXMLTag;
	
	/** The plural xml tag. */
	private String pluralXMLTag;
	
	/** The comment injector. */
	private XMLCommentInjector commentInjector;
	
	/** The record header level. */
	private int recordHeaderLevel;
    
	/** The ignored xml start tags. */
	private ArrayList<String> ignoredXMLStartTags;
	
	/** The ignored xml end tags. */
	private ArrayList<String> ignoredXMLEndTags;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new abstract rif concept content handler.
 	*/
	public AbstractXMLContentHandler() {

		xmlUtility = new XMLUtility();
		htmlUtility = new HTMLUtility();
		recordHeaderLevel = 1;
		commentInjector = new XMLCommentInjector();
		
		ignoredXMLStartTags = new ArrayList<>();
		ignoredXMLEndTags = new ArrayList<>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Initialise.
	 *
	 * @param outputStream the output stream
	 * @param commentInjector the comment injector
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		xmlUtility.initialise(commentInjector, outputStream, "UTF-8");
		htmlUtility.initialise(outputStream, "UTF-8");
	}
	
	/**
	 * Initialise.
	 *
	 * @param outputStream the output stream
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		XMLCommentInjector commentInjector = new XMLCommentInjector();
		xmlUtility.initialise(commentInjector, outputStream, "UTF-8");
		htmlUtility.initialise(outputStream, "UTF-8");
	}
	
	
	/**
	 * Gets the writer.
	 *
	 * @return the writer
	 */
	public Writer getWriter() {
		
		return writer;
	}
	
	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		
		return isActive;
	}
	
	/**
	 * Activate.
	 */
	public void activate() {
		
		isActive = true;
	}
	
	/**
	 * Deactivate.
	 */
	public void deactivate() {
		
		isActive = false;
	}
	
	/**
	 * Gets the singular record name.
	 *
	 * @return the singular record name
	 */
	public String getSingularRecordName() {
		
		return singularXMLTag;
	}
	
	/**
	 * Sets the singular record name.
	 *
	 * @param xmlTag the new singular record name
	 */
	public void setSingularRecordName(
		final String xmlTag) {

		this.singularXMLTag = xmlTag;
	}
	
	/**
	 * Gets the plural record name.
	 *
	 * @return the plural record name
	 */
	public String getPluralRecordName() {
		
		return pluralXMLTag;
	}
	
	/**
	 * Sets the plural record name.
	 *
	 * @param pluralXMLTag the new plural record name
	 */
	public void setPluralRecordName(
		final String pluralXMLTag) {
		
		this.pluralXMLTag = pluralXMLTag;
	}
	
	/**
	 * Checks if is singular record name.
	 *
	 * @param candidateXMLTag the candidate xml tag
	 * @return true, if is singular record name
	 */
	public boolean isSingularRecordName(
		final String candidateXMLTag) {
		
		return singularXMLTag.equals(candidateXMLTag);
	}
		
	/**
	 * Checks if is plural record name.
	 *
	 * @param candidatePluralXMLTag the candidate plural xml tag
	 * @return true, if is plural record name
	 */
	public boolean isPluralRecordName(
		final String candidatePluralXMLTag) {
		
		return pluralXMLTag.equals(candidatePluralXMLTag);
	}
	
	/**
	 * Equals field name.
	 *
	 * @param expectedFieldName the expected field name
	 * @param actualFieldName the actual field name
	 * @return true, if successful
	 */
	public boolean equalsFieldName(
		final String expectedFieldName,
		final String actualFieldName) {
		
		return expectedFieldName.equals(actualFieldName);
	}
	
	/**
	 * Checks if is singular record type applicable.
	 *
	 * @param currentTagName the current tag name
	 * @return true, if is singular record type applicable
	 */
	public boolean isSingularRecordTypeApplicable(
		final String currentTagName) {

		return singularXMLTag.equals(currentTagName);
	}

	/**
	 * Checks if is plural record type applicable.
	 *
	 * @param currentTagName the current tag name
	 * @return true, if is plural record type applicable
	 */
	public boolean isPluralRecordTypeApplicable(
		final String currentTagName) {
		
		return pluralXMLTag != null && pluralXMLTag.equals(currentTagName);
		
	}
	
	/**
	 * Assign delegated handler.
	 *
	 * @param delegatedHandler the delegated handler
	 */
	public void assignDelegatedHandler(
		final AbstractXMLContentHandler delegatedHandler) {

		this.currentDelegatedHandler = delegatedHandler;
	}
	
	/**
	 * Checks if is delegated handler assigned.
	 *
	 * @return true, if is delegated handler assigned
	 */
	public boolean isDelegatedHandlerAssigned() {
		
		return currentDelegatedHandler != null;
	}
	
	/**
	 * Unassign delegated handler.
	 */
	public void unassignDelegatedHandler() {
		
		currentDelegatedHandler = null;
	}
	
	/**
	 * Gets the comment injector.
	 *
	 * @return the comment injector
	 */
	public XMLCommentInjector getCommentInjector() {
		
		return commentInjector;
	}
	
	/**
	 * Gets the current field value.
	 *
	 * @return the current field value
	 */
	public String getCurrentFieldValue() {
		
		return currentFieldValue;
	}
	
	/**
	 * Sets the current field value.
	 *
	 * @param currentFieldValue the new current field value
	 */
	private void setCurrentFieldValue(final String currentFieldValue) {

		this.currentFieldValue = currentFieldValue;
	}
	
	/**
	 * Gets the current delegated handler.
	 *
	 * @return the current delegated handler
	 */
	public AbstractXMLContentHandler getCurrentDelegatedHandler() {
		
		return currentDelegatedHandler;
	}
	
	/**
	 * Sets the current delegated handler.
	 *
	 * @param delegatedHandler the new current delegated handler
	 */
	public void setCurrentDelegatedHandler(
		final AbstractXMLContentHandler delegatedHandler) {

		this.currentDelegatedHandler = delegatedHandler;
	}
	
	/**
	 * Gets the XML utility.
	 *
	 * @return the XML utility
	 */
	public XMLUtility getXMLUtility() {

		return xmlUtility;
	}
	
	/**
	 * Gets the HTML utility.
	 *
	 * @return the HTML utility
	 */
	public HTMLUtility getHTMLUtility() {
		
		return htmlUtility;
	}
	
	/**
	 * Gets the record header level.
	 *
	 * @return the record header level
	 */
	protected int getRecordHeaderLevel() {
		
		return recordHeaderLevel;
	}
	
	/**
	 * Sets the record header level.
	 *
	 * @param recordHeaderLevel the new record header level
	 */
	public void setRecordHeaderLevel(
		final int recordHeaderLevel) {

		this.recordHeaderLevel = recordHeaderLevel;
	}
	
	/**
	 * Begin html bulleted list.
	 */
	public void beginHTMLBulletedList() {
		
		HTMLUtility htmlUtility = getHTMLUtility();
		htmlUtility.beginBulletedList();
	}
	
	/**
	 * Write bulleted displayable item.
	 *
	 * @param displayableListItem the displayable list item
	 */
	public void writeBulletedDisplayableItem(
			
		final DisplayableListItemInterface displayableListItem) {
		htmlUtility.writeBulletedItem(displayableListItem.getDisplayName());
	}
	
	/**
	 * End html bulleted list.
	 */
	public void endHTMLBulletedList() {
		
		HTMLUtility htmlUtility = getHTMLUtility();
		htmlUtility.endBulletedList();
	}

	/**
	 * Ignore xml start tag.
	 *
	 * @param xmlStartTag the xml start tag
	 */
	protected void ignoreXMLStartTag(
					final String xmlStartTag) {
		
		ignoredXMLStartTags.add(xmlStartTag);
	}
	
	/**
	 * Checks if is ignored start tag.
	 *
	 * @param xmlStartTag the xml start tag
	 * @return true, if is ignored start tag
	 */
	protected boolean isIgnoredStartTag(
					final String xmlStartTag) {

		return ignoredXMLStartTags.contains(xmlStartTag);
	}
	
	/**
	 * Ignore xml end tag.
	 *
	 * @param xmlEndTag the xml end tag
	 */
	public void ignoreXMLEndTag(String xmlEndTag) {
		ignoredXMLEndTags.add(xmlEndTag);		
	}

	/**
	 * Checks if is ignored end tag.
	 *
	 * @param xmlEndTag the xml end tag
	 * @return true, if is ignored end tag
	 */
	protected boolean isIgnoredEndTag(
					final String xmlEndTag) {

		return ignoredXMLEndTags.contains(xmlEndTag);
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

	@Override
	public void characters(
		final char[] characters, 
		final int start, 
		final int length) {
		
		if (currentDelegatedHandler == null) {
			setCurrentFieldValue(new String(characters, start, length)); 
		}
		else {
			currentDelegatedHandler.characters(characters, start, length);
		}
	}
	

}
