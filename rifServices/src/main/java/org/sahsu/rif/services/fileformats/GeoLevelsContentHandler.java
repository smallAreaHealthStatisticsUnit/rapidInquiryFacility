
package org.sahsu.rif.services.fileformats;

import java.io.IOException;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import org.sahsu.rif.services.concepts.AbstractGeographicalArea;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.system.RIFServiceMessages;
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


final class GeoLevelsContentHandler 
	extends AbstractXMLContentHandler {

// ==========================================
// Section Constants
// ==========================================
	/**
	 * The Enum CurrentGeoLevelType.
	 */
	private static enum CurrentGeoLevelType {
		/** The geolevel select. */
		GEOLEVEL_SELECT, 
		/** The geolevel area. */
		GEOLEVEL_AREA, 
		/** The geolevel view. */
		GEOLEVEL_VIEW, 
		/** The geolevel to map. */
		GEOLEVEL_TO_MAP};
	
	/** The Constant geoLevelViewRecordName. */
	private static final String geoLevelViewRecordName = "geolevel_view";
	
	/** The Constant geoLevelAreaRecordName. */
	private static final String geoLevelAreaRecordName = "geolevel_area";
	
	/** The Constant geoLevelSelectRecordName. */
	private static final String geoLevelSelectRecordName = "geolevel_select";
	
	/** The Constant geoLevelToMapRecordName. */
	private static final String geoLevelToMapRecordName = "geolevel_to_map";
	
// ==========================================
// Section Properties
// ==========================================
	
	/** The current geo level type. */
	private CurrentGeoLevelType currentGeoLevelType;
	
	/** The current geo level view. */
	private GeoLevelView currentGeoLevelView;
	
	/** The current geo level select. */
	private GeoLevelSelect currentGeoLevelSelect;
	
	/** The current geo level area. */
	private GeoLevelArea currentGeoLevelArea;
	
	/** The current geo level to map. */
	private GeoLevelToMap currentGeoLevelToMap;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new geo levels content handler.
     */
	public GeoLevelsContentHandler() {
		
    	currentGeoLevelType = null;
		setSingularRecordName("geo_levels");
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================

    /**
     * Gets the geo level select.
     *
     * @return the geo level select
     */
	public GeoLevelSelect getGeoLevelSelect() {
		
    	return currentGeoLevelSelect;
    }

    /**
     * Gets the geo level area.
     *
     * @return the geo level area
     */
    public GeoLevelArea getGeoLevelArea() {
    
    	return currentGeoLevelArea;
    }
    
    /**
     * Gets the geo level view.
     *
     * @return the geo level view
     */
    public GeoLevelView getGeoLevelView() {
    	
    	return currentGeoLevelView;
    }
        
    /**
     * Gets the geo level to map.
     *
     * @return the geo level to map
     */
    public GeoLevelToMap getGeoLevelToMap() {
    	
    	return currentGeoLevelToMap;
    }
    

	/**
	 * Write xml.
	 *
	 * @param geographicArea the geographic area
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(
		final AbstractGeographicalArea geographicArea)
		throws IOException {
		
		String recordName = getSingularRecordName();
		XMLUtility xmlUtility = getXMLUtility();
	
		xmlUtility.writeRecordStartTag(recordName);	

		GeoLevelSelect geoLevelSelect
			= geographicArea.getGeoLevelSelect();
		writeXMLGeoLevelSelect(geoLevelSelect);
		
		GeoLevelArea geoLevelArea
			= geographicArea.getGeoLevelArea();
		writeXMLGeoLevelArea(geoLevelArea);
		
		GeoLevelView geoLevelView
			= geographicArea.getGeoLevelView();
		writeXMLGeoLevelView(geoLevelView);		

		GeoLevelToMap geoLevelToMap
			= geographicArea.getGeoLevelToMap();
		writeXMLGeoLevelToMap(geoLevelToMap);		
		
		xmlUtility.writeRecordEndTag(recordName);			
	}
	
	/**
	 * Write xml geo level select.
	 *
	 * @param geoLevelSelect the geo level select
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeXMLGeoLevelSelect(
		final GeoLevelSelect geoLevelSelect) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();

		if (geoLevelSelect != null) {
			xmlUtility.writeRecordStartTag(geoLevelSelectRecordName,
				"id",
				geoLevelSelect.getIdentifier());	
			xmlUtility.writeField(geoLevelSelectRecordName, "name", geoLevelSelect.getName());
			xmlUtility.writeRecordEndTag(geoLevelSelectRecordName);
		}		
	}
	
	/**
	 * Write xml geo level area.
	 *
	 * @param geoLevelArea the geo level area
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeXMLGeoLevelArea(
		final GeoLevelArea geoLevelArea) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();

		if (geoLevelArea != null) {
			xmlUtility.writeRecordStartTag(geoLevelAreaRecordName,
				"id",
				geoLevelArea.getIdentifier());	
			xmlUtility.writeField(geoLevelAreaRecordName, "name", geoLevelArea.getName());
			xmlUtility.writeRecordEndTag(geoLevelAreaRecordName);
		}	
	}
	
	/**
	 * Write xml geo level view.
	 *
	 * @param geoLevelView the geo level view
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeXMLGeoLevelView(
		final GeoLevelView geoLevelView) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
	
		if (geoLevelView != null) {
			xmlUtility.writeRecordStartTag(geoLevelViewRecordName,
				"id",
				geoLevelView.getIdentifier());	
			xmlUtility.writeField(geoLevelViewRecordName, "name", geoLevelView.getName());
			xmlUtility.writeRecordEndTag(geoLevelViewRecordName);
		}	
	}	
	
	/**
	 * Write xml geo level to map.
	 *
	 * @param geoLevelToMap the geo level to map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeXMLGeoLevelToMap(
		final GeoLevelToMap geoLevelToMap) 
		throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
	
		if (geoLevelToMap != null) {
			xmlUtility.writeRecordStartTag(geoLevelToMapRecordName,
				"id",
				geoLevelToMap.getIdentifier());	
			xmlUtility.writeField(geoLevelToMapRecordName, "name", geoLevelToMap.getName());
			xmlUtility.writeRecordEndTag(geoLevelToMapRecordName);
		}	
	}
		
	/**
	 * Write html.
	 *
	 * @param geographicalArea the geographical area
	 */
	public void writeHTML(
		final AbstractGeographicalArea geographicalArea) {

		HTMLUtility htmlUtility = getHTMLUtility();
				
		htmlUtility.beginTable();
		
		htmlUtility.beginRow();
		String geoLevelViewTitle
			= RIFServiceMessages.getMessage("geoLevelView.label");
		htmlUtility.writeBoldColumnValue(geoLevelViewTitle);
		GeoLevelView geoLevelView = geographicalArea.getGeoLevelView();
		htmlUtility.writeColumnValue(geoLevelView.getDisplayName());
		htmlUtility.endRow();
		
		htmlUtility.beginRow();
		String geoLevelAreaTitle
			= RIFServiceMessages.getMessage("geoLevelArea.label");
		htmlUtility.writeBoldColumnValue(geoLevelAreaTitle);
		GeoLevelArea geoLevelArea = geographicalArea.getGeoLevelArea();		
		htmlUtility.writeColumnValue(geoLevelArea.getDisplayName());
		htmlUtility.endRow();
		
		htmlUtility.beginRow();
		String geoLevelSelectTitle
			= RIFServiceMessages.getMessage("geoLevelSelect.label");
		htmlUtility.writeBoldColumnValue(geoLevelSelectTitle);
		GeoLevelSelect geoLevelSelect = geographicalArea.getGeoLevelSelect();		
		htmlUtility.writeColumnValue(geoLevelSelect.getDisplayName());
		htmlUtility.endRow();

		htmlUtility.beginRow();
		String geoLevelToMapTitle
			= RIFServiceMessages.getMessage("geoLevelToMap.label");
		htmlUtility.writeBoldColumnValue(geoLevelToMapTitle);
		GeoLevelToMap geoLevelToMap = geographicalArea.getGeoLevelToMap();		
		htmlUtility.writeColumnValue(geoLevelToMap.getDisplayName());
		htmlUtility.endRow();
		
		htmlUtility.endTable();
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

		if (isSingularRecordName(qualifiedName)) {
			activate();
		}
		else if (equalsFieldName(qualifiedName, geoLevelSelectRecordName)) {
			currentGeoLevelType = CurrentGeoLevelType.GEOLEVEL_SELECT;
			currentGeoLevelSelect = GeoLevelSelect.newInstance();
			String identifier = attributes.getValue("id");
			currentGeoLevelSelect.setIdentifier(identifier);
		}
		else if (equalsFieldName(qualifiedName, geoLevelAreaRecordName)) {
			currentGeoLevelType = CurrentGeoLevelType.GEOLEVEL_AREA;
			currentGeoLevelArea = GeoLevelArea.newInstance();			
			String identifier = attributes.getValue("id");
			currentGeoLevelArea.setIdentifier(identifier);
		}		
		else if (equalsFieldName(qualifiedName, geoLevelViewRecordName)) {
			currentGeoLevelType = CurrentGeoLevelType.GEOLEVEL_VIEW;
			currentGeoLevelView = GeoLevelView.newInstance();			
			String identifier = attributes.getValue("id");
			currentGeoLevelView.setIdentifier(identifier);
		}			
		else if (equalsFieldName(qualifiedName, geoLevelToMapRecordName)) {
			currentGeoLevelType = CurrentGeoLevelType.GEOLEVEL_TO_MAP;
			currentGeoLevelToMap = GeoLevelToMap.newInstance();			
			String identifier = attributes.getValue("id");
			currentGeoLevelToMap.setIdentifier(identifier);
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
			if (currentGeoLevelType == CurrentGeoLevelType.GEOLEVEL_SELECT) {
				currentGeoLevelSelect.setName(getCurrentFieldValue());
			}
			else if (currentGeoLevelType == CurrentGeoLevelType.GEOLEVEL_AREA) {
				currentGeoLevelArea.setName(getCurrentFieldValue());				
			}
			else if (currentGeoLevelType == CurrentGeoLevelType.GEOLEVEL_VIEW) {
				currentGeoLevelView.setName(getCurrentFieldValue());			
			}
			else if (currentGeoLevelType == CurrentGeoLevelType.GEOLEVEL_TO_MAP) {
				currentGeoLevelToMap.setName(getCurrentFieldValue());				
			}
			else {
				assert false;
			}
		}		
		else if (equalsFieldName(qualifiedName, "description") == true) {
			if (currentGeoLevelType == CurrentGeoLevelType.GEOLEVEL_SELECT) {
				currentGeoLevelSelect.setDescription(getCurrentFieldValue());
			}
			else if (currentGeoLevelType == CurrentGeoLevelType.GEOLEVEL_AREA) {
				currentGeoLevelArea.setDescription(getCurrentFieldValue());				
			}
			else if (currentGeoLevelType == CurrentGeoLevelType.GEOLEVEL_VIEW) {
				currentGeoLevelView.setDescription(getCurrentFieldValue());			
			}
			else if (currentGeoLevelType == CurrentGeoLevelType.GEOLEVEL_TO_MAP) {
				currentGeoLevelToMap.setDescription(getCurrentFieldValue());				
			}
			else {
				assert false;
			}
		}
		else if ((equalsFieldName(qualifiedName, geoLevelSelectRecordName) == false) &&
				 (equalsFieldName(qualifiedName, geoLevelAreaRecordName) == false) &&
				 (equalsFieldName(qualifiedName, geoLevelViewRecordName) == false) &&
				 (equalsFieldName(qualifiedName, geoLevelToMapRecordName) == false)) {
		
			//we ignore the end tags to the geolevel classes.  But if it is any
			//other tag, we want a way of identifying that this isn't legal.
			assert false;
		}	
	}
}
