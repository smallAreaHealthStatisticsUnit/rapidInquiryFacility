
package org.sahsu.rif.services.fileformats;

import java.io.IOException;

import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.services.concepts.ComparisonArea;
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


final class ComparisonAreaContentHandler 
	extends AbstractGeographicalAreaContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The current comparison area. */
	private ComparisonArea currentComparisonArea;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new comparison area content handler.
     */
	public ComparisonAreaContentHandler() {
			
		setSingularRecordName("comparison_area");
	}

// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	/**
	* Write xml.
	*
	* @param comparisonArea the comparison area
	* @throws IOException Signals that an I/O exception has occurred.
	*/
	public void writeXML(
		final ComparisonArea comparisonArea) 
		throws IOException {

		String recordName = getSingularRecordName();
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);
		
		super.writeXML(comparisonArea);
		
		xmlUtility.writeRecordEndTag(recordName);
	}
		
	/**
	 * Write html.
	 *
	 * @param headerLevel the header level
	 * @param studyArea the study area
	 * @param isFragmentWithinLargerReport the is fragment within larger report
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final int headerLevel,
		final ComparisonArea studyArea, 
		final boolean isFragmentWithinLargerReport) 
		throws IOException {

		HTMLUtility htmlUtility = getHTMLUtility();
		
		if (isFragmentWithinLargerReport == true) {
			htmlUtility.beginDocument();
		}
		String comparisonAreaTitle
			= RIFServiceMessages.getMessage("comparisonArea.label");
		htmlUtility.writeHeader(headerLevel, comparisonAreaTitle);
		super.writeHTML(headerLevel, studyArea);

		if (isFragmentWithinLargerReport == true) {
			htmlUtility.endDocument();
		}
		
	}
	
	/**
	 * Gets the comparison area.
	 *
	 * @return the comparison area
	 */
	public ComparisonArea getComparisonArea() {
		
		currentComparisonArea.setMapAreas(getMapAreas());
		currentComparisonArea.setGeoLevelSelect(getGeoLevelSelect());
		currentComparisonArea.setGeoLevelArea(getGeoLevelArea());
		currentComparisonArea.setGeoLevelView(getGeoLevelView());
		currentComparisonArea.setGeoLevelToMap(getGeoLevelToMap());		
		return currentComparisonArea;
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
    public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName) == true) {
			currentComparisonArea = ComparisonArea.newInstance();
			activate();
		}
		else {
			super.startElement(nameSpaceURI, localName, qualifiedName, attributes);
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
		else {
			super.endElement(nameSpaceURI, localName, qualifiedName);
		}
	}
}
