
package rifServices.fileFormats;

import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.MapArea;

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


abstract class AbstractGeographicalAreaContentHandler 
	extends AbstractRIFConceptContentHandler {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	/** The geo levels content handler. */
	private GeoLevelsContentHandler geoLevelsContentHandler;
	
	/** The map area content handler. */
	private MapAreaContentHandler mapAreaContentHandler;
	
		
// ==========================================
// Section Construction
// ==========================================
    /**
 * Instantiates a new abstract geographical area content handler.
 */
public AbstractGeographicalAreaContentHandler() {
    	geoLevelsContentHandler = new GeoLevelsContentHandler();
    	mapAreaContentHandler = new MapAreaContentHandler();
	}

// ==========================================
// Section Accessors and Mutators
// ==========================================
	
    /**
     * Write xml.
     *
     * @param geographicArea the geographic area
     * @throws IOException Signals that an I/O exception has occurred.
     */
	protected void writeXML(
		final AbstractGeographicalArea geographicArea) 
		throws IOException {
   
		geoLevelsContentHandler.writeXML(geographicArea);
    	mapAreaContentHandler.writeXML(geographicArea.getMapAreas());    	
    }
    
    /**
     * Write html.
     *
     * @param headerLevel the header level
     * @param geographicArea the geographic area
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void writeHTML(
    	final int headerLevel,
    	final AbstractGeographicalArea geographicArea) 
    	throws IOException {

    	geoLevelsContentHandler.writeHTML(geographicArea);
    	mapAreaContentHandler.writeHTMLMapAreas(geographicArea, headerLevel + 1);    	
    }

    /**
     * Gets the geo level select.
     *
     * @return the geo level select
     */
    public GeoLevelSelect getGeoLevelSelect() {
    	
    	return geoLevelsContentHandler.getGeoLevelSelect();
    }

    /**
     * Gets the geo level area.
     *
     * @return the geo level area
     */
    public GeoLevelArea getGeoLevelArea() {
    	
    	return geoLevelsContentHandler.getGeoLevelArea();
    }
    
    /**
     * Gets the geo level view.
     *
     * @return the geo level view
     */
    public GeoLevelView getGeoLevelView() {
    	
    	return geoLevelsContentHandler.getGeoLevelView();
    }

    /**
     * Gets the geo level to map.
     *
     * @return the geo level to map
     */
    public GeoLevelToMap getGeoLevelToMap() {
    	
    	return geoLevelsContentHandler.getGeoLevelToMap();
    }
    
    /**
     * Gets the map areas.
     *
     * @return the map areas
     */
    public ArrayList<MapArea> getMapAreas() {
    	
    	return mapAreaContentHandler.getMapAreas();
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
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);

		geoLevelsContentHandler.initialise(outputStream, commentInjector);
		mapAreaContentHandler.initialise(outputStream, commentInjector);	
	}	
	

	@Override
	public void initialise(
		final OutputStream outputStream) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream);

		geoLevelsContentHandler.initialise(outputStream);
		mapAreaContentHandler.initialise(outputStream);	
	}	
		
	

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
		else if (isDelegatedHandlerAssigned()) {
			AbstractRIFConceptContentHandler currentContentHandler
				= getCurrentDelegatedHandler();
			currentContentHandler.startElement(nameSpaceURI, localName, qualifiedName, attributes);
		}
		else {			
			if (geoLevelsContentHandler.isSingularRecordTypeApplicable(qualifiedName)) {
				//we've encountered map_areas tag
				assignDelegatedHandler(geoLevelsContentHandler);
			}	
			else if (mapAreaContentHandler.isPluralRecordTypeApplicable(qualifiedName)) {
				//we've encountered map_areas tag
				assignDelegatedHandler(mapAreaContentHandler);
			}
			
			if (isDelegatedHandlerAssigned()) {
				AbstractRIFConceptContentHandler currentContentHandler
					= getCurrentDelegatedHandler();
				currentContentHandler.startElement(
					nameSpaceURI, 
					localName, 
					qualifiedName, 
					attributes);				
			}
			else {
				//by now something should have been assigned a delegate handle
				assert false;
			}			
		}
	}
	

	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {
		
		if (isSingularRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isDelegatedHandlerAssigned()) {
			AbstractRIFConceptContentHandler currentContentHandler
				= getCurrentDelegatedHandler();

			currentContentHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			
			if (currentContentHandler.isActive() == false) {
				unassignDelegatedHandler();
			}
		}
		else {
			assert false;
		}
	}
}
