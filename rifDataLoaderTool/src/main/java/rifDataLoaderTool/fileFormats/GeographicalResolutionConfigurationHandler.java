
package rifDataLoaderTool.fileFormats;


import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.fileFormats.AbstractDataLoaderConfigurationHandler;
import rifServices.fileFormats.XMLCommentInjector;
import rifServices.fileFormats.XMLUtility;





import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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


final class GeographicalResolutionConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	private ArrayList<GeographicalResolutionLevel> geographicalResolutionLevels;
	private GeographicalResolutionLevel currentLevel;
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study content handler.
     */
	public GeographicalResolutionConfigurationHandler() {
		setPluralRecordName("geographical_resolutions");
		setSingularRecordName("geographical_resolution");
		
		geographicalResolutionLevels = new ArrayList<GeographicalResolutionLevel>();
	}


	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
	}

	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);
	}
	
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    	
	/**
	 * Gets the disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public ArrayList<GeographicalResolutionLevel> getGeographicalResolutionLevels() {
		return geographicalResolutionLevels;
	}

	public void writeXML(
		final ArrayList<GeographicalResolutionLevel> levels)
		throws IOException {
			
		XMLUtility xmlUtility = getXMLUtility();
		
		xmlUtility.writeRecordStartTag(getPluralRecordName());		
		String recordType = getSingularRecordName();		
		for (GeographicalResolutionLevel level : levels) {
			xmlUtility.writeRecordStartTag(recordType);
			xmlUtility.writeField(
				recordType, 
				"identifier", 
				level.getIdentifier());
			xmlUtility.writeField(
				recordType, 
				"name", 
				level.getName());
			xmlUtility.writeField(
				recordType, 
				"description", 
				level.getDescription());
	
			xmlUtility.writeRecordEndTag(recordType);
		}
		xmlUtility.writeRecordEndTag(getPluralRecordName());
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
			activate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			currentLevel = GeographicalResolutionLevel.newInstance();
		}		

	}
	
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {
		
		if (isPluralRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			geographicalResolutionLevels.add(currentLevel);
		}
		else if (equalsFieldName("identifier", qualifiedName)) {
			currentLevel.setIdentifier(getCurrentFieldValue());
		}
		else if (equalsFieldName("name", qualifiedName)) {
			currentLevel.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName)) {
			currentLevel.setDescription(getCurrentFieldValue());
		}		
		else {
			assert false;
		}
	}
}
