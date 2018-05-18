
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.util.ArrayList;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.services.concepts.YearInterval;
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


final class YearIntervalContentHandler 
	extends AbstractXMLContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The current year intervals. */
	private ArrayList<YearInterval> currentYearIntervals;
	
	/** The current year interval. */
	private YearInterval currentYearInterval;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new year interval content handler.
     */
	public YearIntervalContentHandler() { 
		
		setSingularRecordName("year_interval");
		setPluralRecordName("year_intervals");
		
		currentYearIntervals = new ArrayList<YearInterval>();
		
		ignoreXMLStartTag("start_year");
		ignoreXMLStartTag("end_year");
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Gets the year interval.
     *
     * @return the year interval
     */
	public YearInterval getYearInterval() {
		
		return currentYearInterval;
	}
	
    /**
     * Gets the year intervals.
     *
     * @return the year intervals
     */
    public ArrayList<YearInterval> getYearIntervals() {
    	
    	return currentYearIntervals;
    }
    
	/**
	 * Write xml.
	 *
	 * @param yearIntervals the year intervals
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final ArrayList<YearInterval> yearIntervals) 
		throws IOException {

		String pluralRecordName = getPluralRecordName();
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(pluralRecordName);				

		for (YearInterval yearInterval : yearIntervals) {
			writeXML(yearInterval);
		}

		xmlUtility.writeRecordEndTag(pluralRecordName);		
	}
	
	/**
	 * Write xml.
	 *
	 * @param yearInterval the year interval
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final YearInterval yearInterval) 
		throws IOException {		

		String recordName = getSingularRecordName();
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);		
		xmlUtility.writeField(recordName, "start_year", yearInterval.getStartYear());
		xmlUtility.writeField(recordName, "end_year", yearInterval.getEndYear());
		xmlUtility.writeRecordEndTag(recordName);
	}
	
	/**
	 * Write html.
	 *
	 * @param yearIntervals the year intervals
	 */
	public void writeHTML(
		final ArrayList<YearInterval> yearIntervals) {

		HTMLUtility htmlUtility = getHTMLUtility();
		
		htmlUtility.beginBulletedList();
		for (YearInterval yearInterval : yearIntervals) {
			htmlUtility.writeBulletedItem(yearInterval.getDisplayName());
		}
		htmlUtility.endBulletedList();		
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

		if (isPluralRecordName(qualifiedName)) {
			currentYearIntervals.clear();
			activate();			
		}
		else if (isSingularRecordName(qualifiedName)) {
			currentYearInterval = YearInterval.newInstance();			
		}
		else if (isIgnoredStartTag(qualifiedName) == false) {
			assert false;
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
			currentYearIntervals.add(currentYearInterval);
		}
		else if (equalsFieldName(qualifiedName, "start_year") == true) {
			currentYearInterval.setStartYear(getCurrentFieldValue());
		}		
		else if (equalsFieldName(qualifiedName, "end_year") == true) {
			currentYearInterval.setEndYear(getCurrentFieldValue());
		}		
		else {
			assert false;
		}
	}
}
