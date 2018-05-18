
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.util.ArrayList;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.services.concepts.AgeBand;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


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


final class AgeBandContentHandler 
	extends AbstractXMLContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The age bands. */
	
	/** The current age band. */
	private AgeBand ageBand;
	
	/** The current age group. */
	private AgeGroup currentAgeGroup;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new age band content handler.
     */
	public AgeBandContentHandler() {
		setSingularRecordName("age_band");
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    
    /**
     * Gets the age bands.
     *
 	 * @return the age bands
 	 */
	
	public AgeBand getAgeBand() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Lower Age Group lower limit=="+ageBand.getLowerLimitAgeGroup().getLowerLimit()+"==");
		buffer.append("Lower Age Group upper limit=="+ageBand.getLowerLimitAgeGroup().getUpperLimit()+"==");
		
		buffer.append("Upper Age Group lower limit=="+ageBand.getUpperLimitAgeGroup().getLowerLimit()+"==");
		buffer.append("Upper Age Group upper limit=="+ageBand.getUpperLimitAgeGroup().getUpperLimit()+"==");
		
		return ageBand;
    }

    
    /**
     * Write xml.
     *
     * @param ageBands the age band
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeXML(
    	final ArrayList<AgeBand> ageBands) 
    	throws IOException {

    	if (ageBands.isEmpty()) {
    		return;
    	}
    	
    	AgeBand firstAgeBand = ageBands.get(0);
    	
    	writeXML(firstAgeBand);
    	
    }
    
	/**
	 * Write xml.
	 *
	 * @param ageBand the age band
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final AgeBand ageBand) 
		throws IOException {		

    	String recordName = getSingularRecordName();
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);
 	
    	//we only care about the first one

    	AgeGroup lowerAgeGroup 
    		= ageBand.getLowerLimitAgeGroup(); 		
    	xmlUtility.writeRecordStartTag("lower_age_group");
    	xmlUtility.writeField(recordName, "id", lowerAgeGroup.getIdentifier());
    	xmlUtility.writeField(recordName, "name", lowerAgeGroup.getName());
    	xmlUtility.writeField(recordName, "lower_limit", lowerAgeGroup.getLowerLimit());
    	xmlUtility.writeField(recordName, "upper_limit", lowerAgeGroup.getUpperLimit());
    	xmlUtility.writeRecordEndTag("lower_age_group");
    	
    	AgeGroup upperAgeGroup 
    		= ageBand.getLowerLimitAgeGroup(); 		
    	xmlUtility.writeRecordStartTag("upper_age_group");
    	xmlUtility.writeField(recordName, "id", upperAgeGroup.getIdentifier());
    	xmlUtility.writeField(recordName, "name", upperAgeGroup.getName());
    	xmlUtility.writeField(recordName, "lower_limit", upperAgeGroup.getLowerLimit());
    	xmlUtility.writeField(recordName, "upper_limit", upperAgeGroup.getUpperLimit());
    	xmlUtility.writeRecordEndTag("upper_age_group");
    			
    	xmlUtility.writeRecordEndTag(recordName);
	
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
			ageBand = AgeBand.newInstance();
			activate();
		}		
		else if ( equalsFieldName(qualifiedName, "lower_age_group")) {
			currentAgeGroup = ageBand.getLowerLimitAgeGroup();
		}
		else if ( equalsFieldName(qualifiedName, "upper_age_group")) {
			currentAgeGroup = ageBand.getUpperLimitAgeGroup();
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
		else if (equalsFieldName(qualifiedName, "id") == true) {
			currentAgeGroup.setIdentifier(getCurrentFieldValue());
			//currentAgeGroup.setName(getCurrentFieldValue());
		}		
		else if (equalsFieldName(qualifiedName, "name") == true) {
			currentAgeGroup.setName(getCurrentFieldValue());
		}		
		else if (equalsFieldName(qualifiedName, "lower_limit") == true) {
			currentAgeGroup.setLowerLimit(getCurrentFieldValue());
		}
		else if (equalsFieldName(qualifiedName, "upper_limit") == true) {
			currentAgeGroup.setUpperLimit(getCurrentFieldValue());
		}
		else {
			assert false;
		}
	}
}
