
package rifServices.fileFormats;

import rifServices.businessConceptLayer.DiseaseMappingStudyArea;
import rifServices.system.RIFServiceMessages;
import rifServices.util.HTMLUtility;
import rifServices.util.XMLCommentInjector;
import rifServices.util.XMLUtility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


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


class DiseaseMappingStudyAreaContentHandler 
	extends AbstractGeographicalAreaContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The current disease mapping study area. */
	private DiseaseMappingStudyArea currentDiseaseMappingStudyArea;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study area content handler.
     */
	public DiseaseMappingStudyAreaContentHandler() {
		
		setSingularRecordName("disease_mapping_study_area");
	}

// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	/**
	 * Gets the disease mapping study area.
	 *
	 * @return the disease mapping study area
	 */
	public DiseaseMappingStudyArea getDiseaseMappingStudyArea() {
		
		currentDiseaseMappingStudyArea.setMapAreas(getMapAreas());
		currentDiseaseMappingStudyArea.setGeoLevelSelect(getGeoLevelSelect());
		currentDiseaseMappingStudyArea.setGeoLevelArea(getGeoLevelArea());
		currentDiseaseMappingStudyArea.setGeoLevelView(getGeoLevelView());
		currentDiseaseMappingStudyArea.setGeoLevelToMap(getGeoLevelToMap());
		
		return currentDiseaseMappingStudyArea;
	}
	
	/**
	 * Write xml.
	 *
	 * @param diseaseMappingStudyArea the disease mapping study area
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final DiseaseMappingStudyArea diseaseMappingStudyArea) 
		throws IOException {

		String recordName = getSingularRecordName();
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);
		super.writeXML(diseaseMappingStudyArea);		
		xmlUtility.writeRecordEndTag(recordName);
	}
	
	/**
	 * Write html.
	 *
	 * @param headerLevel the header level
	 * @param diseaseMappingStudyArea the disease mapping study area
	 * @param isFragmentWithinLargerReport the is fragment within larger report
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeHTML(
		final int headerLevel,
		final DiseaseMappingStudyArea diseaseMappingStudyArea,
		final boolean isFragmentWithinLargerReport) 
		throws IOException {

		HTMLUtility htmlUtility = getHTMLUtility();
		
		if (isFragmentWithinLargerReport) {
			htmlUtility.beginDocument();
		}
		String diseaseMappingAreaTitle
			= RIFServiceMessages.getMessage("diseaseMappingStudyArea.label");
		htmlUtility.writeHeader(headerLevel, diseaseMappingAreaTitle);
		super.writeHTML(headerLevel, diseaseMappingStudyArea);	

		if (isFragmentWithinLargerReport) {
			htmlUtility.endDocument();
		}

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

	/* (non-Javadoc)
	 * @see rifServices.io.AbstractGeographicalAreaContentHandler#initialise(java.io.OutputStream, rifServices.util.XMLCommentInjector)
	 */
	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
	}
		
	/* (non-Javadoc)
	 * @see rifServices.io.AbstractGeographicalAreaContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
    public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName)) {
			currentDiseaseMappingStudyArea = DiseaseMappingStudyArea.newInstance();
			activate();				
		}
		else {
			super.startElement(nameSpaceURI, localName, qualifiedName, attributes);
		}
	}
	
	/* (non-Javadoc)
	 * @see rifServices.io.AbstractGeographicalAreaContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
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
