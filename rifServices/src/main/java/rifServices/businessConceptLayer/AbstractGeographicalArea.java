
package rifServices.businessConceptLayer;

import rifServices.businessConceptLayer.AbstractRIFConcept;
import rifServices.system.RIFServiceSecurityException;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.FieldValidationUtility;

import java.util.ArrayList;

/**
 * Describes properties that are common to the concept of a study area and a 
 * comparison area.  Study areas may be defined differently depending on whether
 * they are part of a disease mapping study or a risk analysis study.  
 * <p>
 * Comparison areas should be the same for either type of study.
 * </p>
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


abstract public class AbstractGeographicalArea 
	extends AbstractRIFConcept {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	/** The geo level view. */
	private GeoLevelView geoLevelView;
	
	/** The geo level area. */
	private GeoLevelArea geoLevelArea;
	
	/** The geo level select. */
	private GeoLevelSelect geoLevelSelect;
	
	/** The geo level to map. */
	private GeoLevelToMap geoLevelToMap;
	
	/** The map areas. */
	private ArrayList<MapArea> mapAreas;
    
// ==========================================
// Section Construction
// ==========================================
    /**
 * Instantiates a new abstract geographical area.
 */
protected AbstractGeographicalArea() {
    	
    	geoLevelSelect = GeoLevelSelect.newInstance();
    	geoLevelArea = GeoLevelArea.newInstance();
    	geoLevelView = GeoLevelView.newInstance();
    	geoLevelToMap = GeoLevelToMap.newInstance();
    	mapAreas = new ArrayList<MapArea>();
    }
    
// ==========================================
// Section Accessors and Mutators
// ==========================================
    
	/**
	 * Checks for identical contents.
	 *
	 * @param otherGeographicalArea the other geographical area
	 * @return true, if successful
	 */
	protected boolean hasIdenticalContents(
		final AbstractGeographicalArea otherGeographicalArea) {
		
		GeoLevelSelect otherGeoLevelSelect
			= otherGeographicalArea.getGeoLevelSelect();				
		GeoLevelArea otherGeoLevelArea
			= otherGeographicalArea.getGeoLevelArea();
		GeoLevelView otherGeoLevelView
			= otherGeographicalArea.getGeoLevelView();
		GeoLevelToMap otherGeoLevelToMap
			= otherGeographicalArea.getGeoLevelToMap();
		
		if (geoLevelSelect == null) {
			if (otherGeoLevelSelect != null) {
				return false;
			}
		}
		else if (geoLevelSelect.hasIdenticalContents(otherGeoLevelSelect) == false) {
			return false;
		}
		
		if (geoLevelArea == null) {
			if (otherGeoLevelArea != null) {
				return false;
			}
		}
		else if (geoLevelView.hasIdenticalContents(otherGeoLevelArea) == false) {
			return false;
		}
		
		if (geoLevelView == null) {
			if (otherGeoLevelView != null) {
				return false;
			}
		}
		else if (geoLevelView.hasIdenticalContents(otherGeoLevelView) == false) {
			return false;
		}
		
		if (geoLevelToMap == null) {
			if (otherGeoLevelToMap != null) {
				return false;
			}
		}
		else if (geoLevelToMap.hasIdenticalContents(otherGeoLevelToMap) == false) {
			return false;
		}
				
		if ( ((geoLevelArea == null) && (otherGeoLevelArea != null)) ||
			 ((geoLevelArea != null) && (otherGeoLevelArea == null))) {
			return false;
		}
		if (geoLevelArea.hasIdenticalContents(otherGeoLevelArea) == false) {
			return false;
		}
		
		ArrayList<MapArea> otherMapAreas 
			= otherGeographicalArea.getMapAreas();		
		if (MapArea.hasIdenticalContents(mapAreas, otherMapAreas) == false) {
			return false;
		}
		
		return super.hasIdenticalContents(otherGeographicalArea);
	}
	
	/**
	 * Gets the map areas.
	 *
	 * @return the map areas
	 */
	public ArrayList<MapArea> getMapAreas() {
		
		return mapAreas;
	}
	
	/**
	 * Adds the map area.
	 *
	 * @param mapArea the map area
	 */
	public void addMapArea(
		final MapArea mapArea) {
		
		mapAreas.add(mapArea);
	}
	
	
	public String[] getMapAreaIdentifiers() {
		String[] areaIdentifiers
			= new String[mapAreas.size()];
		for (int i = 0; i < mapAreas.size(); i++) {
			areaIdentifiers[i] = mapAreas.get(i).getIdentifier();
		}
		
		return areaIdentifiers;
		
	}
	
	/**
	 * Adds the map area.
	 *
	 * @param identifier the identifier
	 * @param label the label
	 */
	public void addMapArea(
		final String identifier, 
		final String label) {
		
		MapArea mapArea = MapArea.newInstance(identifier, label);
		mapAreas.add(mapArea);
	}
	
	/**
	 * Adds the map areas.
	 *
	 * @param mapAreasToAdd the map areas to add
	 */
	public void addMapAreas(
		final ArrayList<MapArea> mapAreasToAdd) {
		
		mapAreas.addAll(mapAreasToAdd);
	}

	/**
	 * Sets the map areas.
	 *
	 * @param mapAreas the new map areas
	 */
	public void setMapAreas(
		final ArrayList<MapArea> mapAreas) {
		
		this.mapAreas = mapAreas;
	}
	
	/**
	 * Clear map areas.
	 */
	public void clearMapAreas() {
		
		mapAreas.clear();
	}
	
	/**
	 * Gets the geo level view.
	 *
	 * @return the geo level view
	 */
	public GeoLevelView getGeoLevelView() {
		
		return geoLevelView;
	}
	
	/**
	 * Sets the geo level view.
	 *
	 * @param geoLevelView the new geo level view
	 */
	public void setGeoLevelView(
		final GeoLevelView geoLevelView) {
		
		this.geoLevelView = geoLevelView;
	}
	
	/**
	 * Gets the geo level area.
	 *
	 * @return the geo level area
	 */
	public GeoLevelArea getGeoLevelArea() {
		
		return geoLevelArea;
	}
	
	/**
	 * Sets the geo level area.
	 *
	 * @param geoLevelArea the new geo level area
	 */
	public void setGeoLevelArea(
		final GeoLevelArea geoLevelArea) {
		
		this.geoLevelArea = geoLevelArea;
	}
	
	/**
	 * Gets the geo level select.
	 *
	 * @return the geo level select
	 */
	public GeoLevelSelect getGeoLevelSelect() {
		
		return geoLevelSelect;
	}
	
	/**
	 * Sets the geo level select.
	 *
	 * @param geoLevelSelect the new geo level select
	 */
	public void setGeoLevelSelect(
		final GeoLevelSelect geoLevelSelect) {
		
		this.geoLevelSelect = geoLevelSelect;
	}
	
	/**
	 * Gets the geo level to map.
	 *
	 * @return the geo level to map
	 */
	public GeoLevelToMap getGeoLevelToMap() {
		
		return geoLevelToMap;
	}
	
	/**
	 * Sets the geo level to map.
	 *
	 * @param geoLevelToMap the new geo level to map
	 */
	public void setGeoLevelToMap(
		final GeoLevelToMap geoLevelToMap) {
		
		this.geoLevelToMap = geoLevelToMap;
	}
			
// ==========================================
// Section Errors and Validation
// ==========================================
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#checkSecurityViolations()
	 */
	@Override
	protected void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();

		if (geoLevelView != null) {
			geoLevelView.checkSecurityViolations();		
		}
		
		if (geoLevelArea != null) {
			geoLevelArea.checkSecurityViolations();
		}
		
		if (geoLevelSelect != null) {
			geoLevelSelect.checkSecurityViolations();			
		}
		
		if (geoLevelToMap != null) {
			geoLevelToMap.checkSecurityViolations();		
		}
		
		String recordType = getRecordType();

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String mapAreaIdentiferFieldName
			= RIFServiceMessages.getMessage("mapArea.identifier.label");
		String mapAreaLabelFieldName
			= RIFServiceMessages.getMessage("mapArea.label.label");
		for (MapArea mapArea : mapAreas) {
			if (mapArea != null) {
				fieldValidationUtility.checkMaliciousCode(
					recordType, 
					mapAreaIdentiferFieldName,
					mapArea.getIdentifier());
			
				fieldValidationUtility.checkMaliciousCode(
					recordType,
					mapAreaLabelFieldName,
					mapArea.getLabel());
			}		
		}
	}

	/**
	 * Check errors.
	 *
	 * @param errorMessages the error messages
	 */
	protected void checkErrors(
		final ArrayList<String> errorMessages) {
		
		String recordType = getRecordType();
		
		if (geoLevelView == null) {
			String fieldName
				= RIFServiceMessages.getMessage("geoLevelView.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				geoLevelView.checkErrors();			
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
		
		if (geoLevelArea == null) {
			String fieldName
				= RIFServiceMessages.getMessage("geoLevelArea.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					fieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				geoLevelArea.checkErrors();			
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}

		if (geoLevelSelect == null) {
			String fieldName
				= RIFServiceMessages.getMessage("geoLevelSelect.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				geoLevelSelect.checkErrors();			
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
		
		if (geoLevelToMap == null) {
			String fieldName
				= RIFServiceMessages.getMessage("geoLevelToMap.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				geoLevelToMap.checkErrors();			
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
				
		if (mapAreas == null) {
			
			String mapAreasFieldLabel
				= RIFServiceMessages.getMessage("mapArea.plural.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					mapAreasFieldLabel);			
			errorMessages.add(errorMessage);		
		}
		else {			
			if (mapAreas.isEmpty() == true) {
				
				//ERROR: StudyArea must have at least one area identifier
				String errorMessage
					= RIFServiceMessages.getMessage(
						"abstractGeographicalArea.error.noMapAreasDefined",
						recordType);
				errorMessages.add(errorMessage);
			}
			else {			
				boolean areMapAreasValid = true;
				for (MapArea mapArea : mapAreas) {
					if (mapArea == null) {
						String mapAreaRecordType
							= RIFServiceMessages.getMessage("mapArea.label");
						String errorMessage
							= RIFServiceMessages.getMessage(
								"general.validation.nullListItem",
								getRecordType(),
								mapAreaRecordType);
						errorMessages.add(errorMessage);
						areMapAreasValid = false;
					}
					else {
						try {
							mapArea.checkErrors();
						}
						catch(RIFServiceException rifServiceException) {
							areMapAreasValid = false;
							errorMessages.addAll(rifServiceException.getErrorMessages());
						}
					}
				}
				
				if (areMapAreasValid == true) {
					ArrayList<MapArea> duplicateMapAreas
						= MapArea.identifyDuplicatesWithinList(mapAreas);
					
					if (duplicateMapAreas.size() > 0) {
						StringBuilder buffer = new StringBuilder();
						for (int i = 0; i < duplicateMapAreas.size(); i++) {
							if (i != 0) {
								buffer.append(",");
							}
							buffer.append(duplicateMapAreas.get(i).getDisplayName());						
						}
					
						String errorMessage
							= RIFServiceMessages.getMessage(
								"abstractGeographicalArea.error.duplicateMapAreas",
								recordType,
								buffer.toString());
						errorMessages.add(errorMessage);					
					}
				}
			}		
		}
	}
	
// ==========================================
// Section Interfaces
// ==========================================

// ==========================================
// Section Override
// ==========================================

	abstract public String getRecordType();
	
	//Interface: DisplayableListItem
	@Override
	public String getDisplayName() {
	
		return getIdentifier();
	}
}
