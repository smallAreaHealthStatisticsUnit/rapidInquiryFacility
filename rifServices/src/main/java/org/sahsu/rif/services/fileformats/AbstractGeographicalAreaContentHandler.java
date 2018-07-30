
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.services.concepts.AbstractGeographicalArea;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.MapArea;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

abstract class AbstractGeographicalAreaContentHandler
	extends AbstractXMLContentHandler {
	/** The current disease mapping study area. */
	AbstractStudyArea currentStudyArea;

	/** The geo levels content handler. */
	private GeoLevelsContentHandler geoLevelsContentHandler;
	
	/** The map area content handler. */
	private MapAreaContentHandler mapAreaContentHandler;

    /**
	 * Instantiates a new abstract geographical area content handler.
	 */
    AbstractGeographicalAreaContentHandler() {
    	geoLevelsContentHandler = new GeoLevelsContentHandler();
    	mapAreaContentHandler = new MapAreaContentHandler();
	}

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
     */
    void writeHTML(
		    final int headerLevel,
		    final AbstractGeographicalArea geographicArea) {

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

	/**
	 * Gets the disease mapping study area.
	 *
	 * @return the disease mapping study area
	 */
	public AbstractStudyArea getStudyArea() {

		currentStudyArea.setMapAreas(getMapAreas());
		currentStudyArea.setGeoLevelSelect(getGeoLevelSelect());
		currentStudyArea.setGeoLevelArea(getGeoLevelArea());
		currentStudyArea.setGeoLevelView(getGeoLevelView());
		currentStudyArea.setGeoLevelToMap(getGeoLevelToMap());

		return currentStudyArea;
	}

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
			AbstractXMLContentHandler currentContentHandler
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
				AbstractXMLContentHandler currentContentHandler
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
			AbstractXMLContentHandler currentContentHandler
				= getCurrentDelegatedHandler();

			currentContentHandler.endElement(
				nameSpaceURI, 
				localName, 
				qualifiedName);
			
			if (!currentContentHandler.isActive()) {
				unassignDelegatedHandler();
			}
		}
		else {
			assert false;
		}
	}
}
