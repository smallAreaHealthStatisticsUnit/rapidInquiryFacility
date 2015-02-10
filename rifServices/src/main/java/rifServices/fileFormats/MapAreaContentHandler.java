
package rifServices.fileFormats;

import rifServices.businessConceptLayer.AbstractGeographicalArea;

import rifServices.businessConceptLayer.MapArea;
import rifServices.system.RIFServiceMessages;
import rifServices.util.HTMLUtility;
import rifServices.util.XMLUtility;

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


public class MapAreaContentHandler 
	extends AbstractRIFConceptContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The current map areas. */
	private ArrayList<MapArea> currentMapAreas;
	
	/** The current map area. */
	private MapArea currentMapArea;
	    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new map area content handler.
     */
	public MapAreaContentHandler() {
		
		setPluralRecordName("map_areas");    	
		setSingularRecordName("map_area");
		
		currentMapAreas = new ArrayList<MapArea>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Gets the map areas.
     *
     * @return the map areas
     */
	public ArrayList<MapArea> getMapAreas() {
		
		return currentMapAreas;
	}
	
    /**
     * Clear map areas.
     */
    public void clearMapAreas() {
    	
    	currentMapAreas.clear();
    }
    
	/**
	 * Write xml.
	 *
	 * @param mapAreas the map areas
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final ArrayList<MapArea> mapAreas) 
		throws IOException {		
		
		String pluralRecordName = getPluralRecordName();
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(pluralRecordName);	
		
		for(MapArea mapArea : mapAreas) {
			writeXML(mapArea);
		}
		
		xmlUtility.writeRecordEndTag(pluralRecordName);		
	}
    
	/**
	 * Write xml.
	 *
	 * @param mapArea the map area
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final MapArea mapArea) 
		throws IOException {		

		String recordName = getSingularRecordName();

		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(recordName);	
		xmlUtility.writeField(recordName, "id", mapArea.getIdentifier());
		xmlUtility.writeField(recordName, "label", mapArea.getLabel());

		xmlUtility.writeRecordEndTag(recordName);
	}
	
	/**
	 * Write html map areas.
	 *
	 * @param geographicalArea the geographical area
	 * @param headerLevel the header level
	 */
	public void writeHTMLMapAreas(
		final AbstractGeographicalArea geographicalArea,
		int headerLevel) {
		
		HTMLUtility htmlUtility = getHTMLUtility();
			
		ArrayList<MapArea> mapAreas = geographicalArea.getMapAreas();
			
		int numberOfMapAreas = mapAreas.size();
		if (numberOfMapAreas == 0) {
			return;
		}
						
		String mapAreasTitle
			= RIFServiceMessages.getMessage("mapArea.plural.label");
		htmlUtility.writeHeader(headerLevel, mapAreasTitle);

		if (numberOfMapAreas == 1) {
			//just write it as one line.
			htmlUtility.writeParagraph(mapAreas.get(0).getDisplayName());
		}
		else if (numberOfMapAreas < 20) {
			//probably 20 is a good maximum number before bulleted lists start
			//to take up too much room
			ArrayList<String> mapAreaDisplayNames
				= MapArea.getAlphabetisedList(mapAreas);
				htmlUtility.writeBulletedList(mapAreaDisplayNames);
		}
		else {
			//render as one big comma separated list
			StringBuilder oneLongList = new StringBuilder();
			for (int i = 0; i < numberOfMapAreas; i++) {
				if (i != 0) {
					oneLongList.append(",");					
				}
				oneLongList.append(mapAreas.get(i).getDisplayName());				
			}
			htmlUtility.writeParagraph(oneLongList.toString());
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
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
    public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName)) {
			currentMapAreas.clear();
			activate();
		}	
		else if (isSingularRecordName(qualifiedName)) {
			currentMapArea = MapArea.newInstance();
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
			currentMapAreas.add(currentMapArea);
		}
		else if (equalsFieldName(qualifiedName, "id") == true) {
			currentMapArea.setIdentifier(getCurrentFieldValue());
		}		
		else if (equalsFieldName(qualifiedName, "label") == true) {
			currentMapArea.setLabel(getCurrentFieldValue());
		}		
		else {
			assert false;
		}
	}
}
