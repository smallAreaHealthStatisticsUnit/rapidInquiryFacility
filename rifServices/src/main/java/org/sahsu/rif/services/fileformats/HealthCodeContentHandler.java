
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.services.concepts.HealthCode;
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


final class HealthCodeContentHandler 
	extends AbstractXMLContentHandler {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The health codes. */
	private ArrayList<HealthCode> healthCodes;
	
	/** The current health code. */
	private HealthCode currentHealthCode;
	
	/** The collator. */
	private Collator collator;
	
	/** The yes answer. */
	private String yesAnswer;
	
	/** The no answer. */
	private String noAnswer;
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new health code content handler.
 	*/
	public HealthCodeContentHandler(){
		
		healthCodes = new ArrayList<HealthCode>();
		setSingularRecordName("health_code");
		this.setPluralRecordName("health_codes");
		collator = Messages.genericMessages().getCollator();
		
		yesAnswer 
			= RIFServiceMessages.getMessage("general.xml.yes");
		noAnswer 
			= RIFServiceMessages.getMessage("general.xml.no");		
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================

	/**
	 * Gets the health codes.
	 *
	 * @return the health codes
	 */
	public ArrayList<HealthCode> getHealthCodes() {
		return healthCodes;
	}
		
	/**
	 * Write xml.
	 *
	 * @param healthCodes the health codes
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(final ArrayList<HealthCode> healthCodes) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());
		
		for (HealthCode healthCode : healthCodes) {
			writeXML(healthCode);
		}
		
		xmlUtility.writeRecordEndTag(getPluralRecordName());
	}
	
    /**
     * Write xml.
     *
     * @param healthCode the health code
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeXML(
    	final HealthCode healthCode) 
    	throws IOException {

    	XMLUtility xmlUtility = getXMLUtility();
		String recordName = getSingularRecordName();
		xmlUtility.writeRecordStartTag(recordName);
		xmlUtility.writeField(recordName, "code", healthCode.getCode());
		xmlUtility.writeField(recordName, "name_space", healthCode.getNameSpace());
		xmlUtility.writeField(recordName, "description", healthCode.getDescription());
		if (healthCode.isTopLevelTerm() == true) {
			String yesAnswer
				= RIFServiceMessages.getMessage("general.xml.yes");
			xmlUtility.writeField(recordName, "is_top_level_term", yesAnswer);
		}
		else {
			String noAnswer
				= RIFServiceMessages.getMessage("general.xml.no");
			xmlUtility.writeField(recordName, "is_top_level_term", noAnswer);			
		}
		
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

	/**
	 * Read health code list.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
    public void readHealthCodeList() 
    	throws IOException {

    	XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());
		xmlUtility.writeStartXML();
		
		xmlUtility.writeRecordStartTag(getPluralRecordName());
		for (HealthCode healthCode : healthCodes) {
			writeXML(healthCode);
		}		
		xmlUtility.writeRecordEndTag(getPluralRecordName());
	}

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName) == true) {
			healthCodes.clear();
			activate();
		}
		else if (isSingularRecordName(qualifiedName) == true) {
			currentHealthCode = HealthCode.newInstance();
		}
	}
	
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
			healthCodes.add(currentHealthCode);
		}
		else if (equalsFieldName("code", qualifiedName) == true) {
			currentHealthCode.setCode(getCurrentFieldValue());
		}
		else if (equalsFieldName("name_space", qualifiedName) == true) {
			currentHealthCode.setNameSpace(getCurrentFieldValue());						
		}
		else if (equalsFieldName("description", qualifiedName) == true) {
			currentHealthCode.setDescription(getCurrentFieldValue());
		}
		else if (equalsFieldName("health_condition_name", qualifiedName) == true) {
			String yesNoAnswer = getCurrentFieldValue();
			if (collator.equals(yesNoAnswer, yesAnswer) == true) {
				currentHealthCode.setTopLevelTerm(true);
			}
			else if (collator.equals(yesNoAnswer, noAnswer) == true) {				
				currentHealthCode.setTopLevelTerm(false);
			}
			else {
				assert false;
			}			
		}		
	}
}
