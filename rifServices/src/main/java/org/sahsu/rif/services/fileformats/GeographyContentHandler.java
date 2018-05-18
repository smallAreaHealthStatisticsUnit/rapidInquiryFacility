
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


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


final class GeographyContentHandler 
	extends AbstractXMLContentHandler {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The calculation methods. */
	private Geography geography;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new calculation method content handler.
     */
	public GeographyContentHandler() {
		
		setSingularRecordName("geography");		
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the calculation methods.
	 *
	 * @return the calculation methods
	 */
	public Geography getGeography() {
		
		return geography;
	}

	
	/**
	 * Write html.
	 *
	 * @param headerLevel the header level
	 * @param investigations the investigations
	 * @param isFragmentWithinLargerReport the is fragment within larger report
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final int headerLevel,
		final Geography geography, 
		final boolean isFragmentWithinLargerReport) 
		throws IOException {

		HTMLUtility htmlUtility = getHTMLUtility();		
		if (isFragmentWithinLargerReport == false) {
			htmlUtility.beginDocument();
		}
		
		String investigationsTitle
			= RIFServiceMessages.getMessage("geography.label");
		htmlUtility.writeHeader(headerLevel, investigationsTitle);
		
		if (isFragmentWithinLargerReport == false) {
			htmlUtility.endDocument();
		}
	}	
	
	
	/**
	 * Write xml.
	 *
	 * @param calculationMethods the calculation methods
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final Geography geography) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
		String recordName = getSingularRecordName();

		xmlUtility.writeRecordStartTag(getSingularRecordName());
		xmlUtility.writeField(
			recordName, 
			"name", 
			geography.getName());
		xmlUtility.writeField(
			recordName, 
			"description", 
			geography.getDescription());
		
		xmlUtility.writeRecordEndTag(getSingularRecordName());
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
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
	}
	
	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName)) {
			activate();
			geography = Geography.newInstance();
		}
	}
	
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {
		
		if (isSingularRecordName(qualifiedName) == true) {
			deactivate();
		}
		else if ( equalsFieldName("name", qualifiedName) == true) {
			geography.setName(getCurrentFieldValue());
		}
		else if ( equalsFieldName("description", qualifiedName) == true) {
			geography.setDescription(getCurrentFieldValue());
		}
		else if (isIgnoredEndTag(qualifiedName) == false) {
			//illegal field name
			assert false;
		}
	}
}
