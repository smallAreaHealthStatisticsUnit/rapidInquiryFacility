
package rifServices.fileFormats;

import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.AgeGroup;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;


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


class AgeBandContentHandler 
	extends AbstractRIFConceptContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The age bands. */
	private ArrayList<AgeBand> ageBands;
	
	/** The current age band. */
	private AgeBand currentAgeBand;
	
	/** The current age group. */
	private AgeGroup currentAgeGroup;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new age band content handler.
     */
	public AgeBandContentHandler() {
		
    	setPluralRecordName("age_bands");
		setSingularRecordName("age_band");
		ageBands = new ArrayList<AgeBand>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    
    /**
     * Gets the age bands.
     *
 	 * @return the age bands
 	 */
	public ArrayList<AgeBand> getAgeBands() {

		return ageBands;
    }

    
    /**
     * Write xml.
     *
     * @param ageBands the age bands
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeXML(
    	final ArrayList<AgeBand> ageBands) 
    	throws IOException {

    	String recordName = getPluralRecordName();
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);
		
		xmlUtility.writeRecordEndTag(recordName);
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
		
		AgeGroup lowerLimitAgeGroup = ageBand.getLowerLimitAgeGroup();
		xmlUtility.writeRecordStartTag("lower_limit_age_group");
		xmlUtility.writeField(
			recordName, 
			"lower_limit_age", 
			lowerLimitAgeGroup.getLowerLimit());
		xmlUtility.writeField(
			recordName, 
			"upper_limit_age", 
			lowerLimitAgeGroup.getUpperLimit());
		
		AgeGroup upperLimitAgeGroup = ageBand.getUpperLimitAgeGroup();
		xmlUtility.writeRecordStartTag("upper_limit_age_group");
		xmlUtility.writeField(
			recordName, 
			"lower_limit_age", 
			upperLimitAgeGroup.getLowerLimit());
		xmlUtility.writeField(
			recordName, 
			"upper_limit_age", 
			upperLimitAgeGroup.getUpperLimit());
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

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
    public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName) == true) {
			activate();
		}
		if (isSingularRecordName(qualifiedName) == true) {
			currentAgeBand = AgeBand.newInstance();
		}
		else if ( equalsFieldName(qualifiedName, "lower_limit_age_group") || 
			equalsFieldName(qualifiedName, "upper_limit_age_group")) {
			currentAgeGroup = AgeGroup.newInstance();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {
				
		if (isPluralRecordName(qualifiedName) == true) {
			deactivate();
		}	
		else if (isSingularRecordName(qualifiedName) == true) {
			ageBands.add(currentAgeBand);
		}		
		else if (equalsFieldName(qualifiedName, "lower_limit_age_group") == true) {
			currentAgeBand.setLowerLimitAgeGroup(currentAgeGroup);
		}
		else if (equalsFieldName(qualifiedName, "upper_limit_age_group") == true) {
			currentAgeBand.setUpperLimitAgeGroup(currentAgeGroup);
		}
		else if (equalsFieldName(qualifiedName, "lower_limit_age") == true) {
			currentAgeGroup.setLowerLimit(getCurrentFieldValue());
		}
		else if (equalsFieldName(qualifiedName, "lower_limit_age") == true) {
			currentAgeGroup.setLowerLimit(getCurrentFieldValue());
		}
		else {
			assert false;
		}
	}
}
