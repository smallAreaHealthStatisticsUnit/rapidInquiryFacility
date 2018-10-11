
package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

/**
 * Describes properties that are common to the concept of a study area and a 
 * comparison area.  Study areas may be defined differently depending on whether
 * they are part of a disease mapping study or a risk analysis study.  
 * <p>
 * Comparison areas should be the same for either type of study.
 * </p>
 */
public abstract class AbstractGeographicalArea extends AbstractRIFConcept {

	private static final Messages SERVICE_MESSAGES = Messages.serviceMessages();
	
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

	/**
     * Instantiates a new abstract geographical area.
     */
	protected AbstractGeographicalArea() {
    	
    	geoLevelSelect = GeoLevelSelect.newInstance();
    	geoLevelArea = GeoLevelArea.newInstance();
    	geoLevelView = GeoLevelView.newInstance();
    	geoLevelToMap = GeoLevelToMap.newInstance();
    	mapAreas = new ArrayList<>();
    }

	public void identifyDifferences(
		final AbstractGeographicalArea anotherGeographicalArea,
		final ArrayList<String> differences) {
	
		super.identifyDifferences(
			anotherGeographicalArea, 
			differences);
	}

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
		else if (!geoLevelSelect.hasIdenticalContents(otherGeoLevelSelect)) {
			return false;
		}
		
		if (geoLevelArea == null) {
			if (otherGeoLevelArea != null) {
				return false;
			}
		} else if (!geoLevelView.hasIdenticalContents(otherGeoLevelArea)) {
			return false;
		}
		
		if (geoLevelView == null) {
			if (otherGeoLevelView != null) {
				return false;
			}
		} else if (!geoLevelView.hasIdenticalContents(otherGeoLevelView)) {
			return false;
		}
		
		if (geoLevelToMap == null) {
			if (otherGeoLevelToMap != null) {
				return false;
			}
		} else if (!geoLevelToMap.hasIdenticalContents(otherGeoLevelToMap)) {
			return false;
		}

		if (geoLevelArea == null && otherGeoLevelArea != null) {
			return false;
		}

		if (geoLevelArea != null) {
			if (otherGeoLevelArea == null) {
				return false;
			} else if (!geoLevelArea.hasIdenticalContents(otherGeoLevelArea)) {
				return false;
			}
		}
		
		ArrayList<MapArea> otherMapAreas = otherGeographicalArea.getMapAreas();
		if (!MapArea.hasIdenticalContents(mapAreas, otherMapAreas)) {
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

	/**
	 * Adds the map area.
	 *
	 * @param identifier the identifier
	 * @param label the label
	 */
	public void addMapArea(
		final String geographicalIdentifier,
		final String identifier, 
		final String label,
		final Integer band) {
		
		MapArea mapArea 
			= MapArea.newInstance(
				geographicalIdentifier,
				identifier, 
				label,
				band);
		mapAreas.add(mapArea);
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
			= SERVICE_MESSAGES.getMessage("mapArea.identifier.label");
		String mapAreaLabelFieldName
			= SERVICE_MESSAGES.getMessage("mapArea.label.label");
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
		final ValidationPolicy validationPolicy,
		final ArrayList<String> errorMessages) {
		
		String recordType = getRecordType();
		
		if (geoLevelView == null) {
			String fieldName
				= SERVICE_MESSAGES.getMessage("geoLevelView.label");
			String errorMessage
				= SERVICE_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				geoLevelView.checkErrors(validationPolicy);			
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}

		if (validationPolicy == ValidationPolicy.STRICT) { 		
			if (geoLevelArea == null) {
				String fieldName
					= SERVICE_MESSAGES.getMessage("geoLevelArea.label");
				String errorMessage
					= SERVICE_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						fieldName);
				errorMessages.add(errorMessage);
			}
			else {
				try {
					geoLevelArea.checkErrors(validationPolicy);			
				}
				catch(RIFServiceException exception) {
					errorMessages.addAll(exception.getErrorMessages());
				}			
			}
		}
		
		
		if (geoLevelSelect == null) {
			String fieldName
				= SERVICE_MESSAGES.getMessage("geoLevelSelect.label");
			String errorMessage
				= SERVICE_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				geoLevelSelect.checkErrors(validationPolicy);			
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
		
		if (geoLevelToMap == null) {
			String fieldName
				= SERVICE_MESSAGES.getMessage("geoLevelToMap.label");
			String errorMessage
				= SERVICE_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					fieldName);			
			errorMessages.add(errorMessage);
		}
		else {
			try {				
				geoLevelToMap.checkErrors(validationPolicy);			
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
				
		if (mapAreas == null) {
			
			String mapAreasFieldLabel
				= SERVICE_MESSAGES.getMessage("mapArea.plural.label");
			String errorMessage
				= SERVICE_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					mapAreasFieldLabel);			
			errorMessages.add(errorMessage);		
		} else {
			if (mapAreas.isEmpty()) {
				
				//ERROR: StudyArea must have at least one area identifier
				String errorMessage
					= SERVICE_MESSAGES.getMessage(
						"abstractGeographicalArea.error.noMapAreasDefined",
						recordType);
				errorMessages.add(errorMessage);
			} else {
				boolean areMapAreasValid = true;
				for (MapArea mapArea : mapAreas) {
					if (mapArea == null) {
						String mapAreaRecordType
							= SERVICE_MESSAGES.getMessage("mapArea.label");
						String errorMessage
							= SERVICE_MESSAGES.getMessage(
								"general.validation.nullListItem",
								getRecordType(),
								mapAreaRecordType);
						errorMessages.add(errorMessage);
						areMapAreasValid = false;
					}
					else {
						try {
							mapArea.checkErrors(validationPolicy);
						}
						catch(RIFServiceException rifServiceException) {
							areMapAreasValid = false;
							errorMessages.addAll(rifServiceException.getErrorMessages());
						}
					}
				}
				
				if (areMapAreasValid) {
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
							= SERVICE_MESSAGES.getMessage(
								"abstractGeographicalArea.error.duplicateMapAreas",
								recordType,
								buffer.toString());
						errorMessages.add(errorMessage);					
					}
				}
			}		
		}
	}
	
	abstract public String getRecordType();
	
	@Override
	public String getDisplayName() {
	
		return getIdentifier();
	}
}
