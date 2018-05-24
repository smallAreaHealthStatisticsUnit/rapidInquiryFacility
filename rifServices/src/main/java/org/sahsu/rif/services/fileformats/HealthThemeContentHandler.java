
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.util.ArrayList;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.services.concepts.HealthTheme;
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


final class HealthThemeContentHandler 
	extends AbstractXMLContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The current health theme. */
	private HealthTheme currentHealthTheme;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new health theme content handler.
     */
	public HealthThemeContentHandler() {
		
    	setPluralRecordName("health_themes");
		setSingularRecordName("health_theme");
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Gets the health theme.
     *
     * @return the health theme
     */
	public HealthTheme getHealthTheme() {
		
		return currentHealthTheme;
	}

    
	/**
	 * Write xml.
	 *
	 * @param healthThemes the health themes
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final ArrayList<HealthTheme> healthThemes) 
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());	
		
		for (HealthTheme healthTheme : healthThemes) {
			writeXML(healthTheme);
		}
		
		xmlUtility.writeRecordEndTag(getPluralRecordName());		
	}
	
	/**
	 * Write xml.
	 *
	 * @param healthTheme the health theme
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final HealthTheme healthTheme) 
		throws IOException {		

		String recordName = getSingularRecordName();
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);	
		xmlUtility.writeField(recordName, "name", healthTheme.getName());		
		xmlUtility.writeField(recordName, "description", healthTheme.getDescription());
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

		if (equalsFieldName(qualifiedName, getSingularRecordName()) == true) {
			currentHealthTheme = HealthTheme.newInstance();
			activate();
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
		else if (equalsFieldName(qualifiedName, "name") == true) {
			currentHealthTheme.setName(getCurrentFieldValue());
		}		
		else if (equalsFieldName(qualifiedName, "description") == true) {
			currentHealthTheme.setDescription(getCurrentFieldValue());
		}		
		else {
			assert false;
		}
	}
}
