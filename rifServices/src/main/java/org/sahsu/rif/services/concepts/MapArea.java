package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

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

public final class MapArea 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The label. */
	private String label;
	private String geographicalIdentifier;
	private Integer band;
	private Integer intersectCount;
	private Double distanceFromNearestSource;
	private String nearestRifShapePolyId;
	private Double exposureValue;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map area.
	 *
	 * @param identifier the identifier
	 * @param label the label
	 */
	private MapArea(
		final String geographicalIdentifier,
		final String identifier,
		final String label,
		final Integer band) {
		
		setGeographicalIdentifier(geographicalIdentifier);
		setIdentifier(identifier);
		setLabel(label);
		setBand(band);
		setIntersectCount(0);
		setDistanceFromNearestSource(0.0);
		setNearestRifShapePolyId("");
		setExposureValue(0.0);
	}
	private MapArea(
		final String geographicalIdentifier,
		final String identifier,
		final String label,
		final Integer band,
		final Integer intersectCount,
		final Double distanceFromNearestSource,
		final String nearestRifShapePolyId,
		final Double exposureValue) {
		
		setGeographicalIdentifier(geographicalIdentifier);
		setIdentifier(identifier);
		setLabel(label);
		setBand(band);
		setIntersectCount(intersectCount);
		setDistanceFromNearestSource(distanceFromNearestSource);
		setNearestRifShapePolyId(nearestRifShapePolyId);
		setExposureValue(exposureValue);
	}
	
	/**
	 * Instantiates a new map area.
	 */
	private MapArea() {
		setGeographicalIdentifier("");
		setIdentifier("");
		setLabel("");
		setBand(0);
		setIntersectCount(0);
		setDistanceFromNearestSource(0.0);
		setNearestRifShapePolyId("");
		setExposureValue(0.0);
	}

	/**
	 * New instance.
	 *
	 * @return the map area
	 */
	public static MapArea newInstance() {		
		MapArea mapArea = new MapArea();
		return mapArea;		
	}
	
	
	/**
	 * New instance.
	 *
	 * @param identifier the identifier
	 * @param label the label
	 * @return the map area
	 */
	public static MapArea newInstance(
		final String geographicalIdentifier,
		final String identifier,
		final String label,
		final Integer band,
		final Integer intersectCount,
		final Double distanceFromNearestSource,
		final String nearestRifShapePolyId,
		final Double exposureValue) {
		
		MapArea mapArea 	
			= new MapArea(
				geographicalIdentifier,
				identifier, 
				label,
				band,
				intersectCount,
				distanceFromNearestSource,
				nearestRifShapePolyId,
				exposureValue);
		return mapArea;		
	}
	public static MapArea newInstance(
		final String geographicalIdentifier,
		final String identifier,
		final String label,
		final Integer band) {
		
		MapArea mapArea 	
			= new MapArea(
				geographicalIdentifier,
				identifier, 
				label,
				band);
		return mapArea;		
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalMapArea the original map area
	 * @return the map area
	 */
	public static MapArea createCopy(
		final MapArea originalMapArea) {

		if (originalMapArea == null) {
			return null;
		}
		
		MapArea cloneMapArea
			= new MapArea(
				originalMapArea.getGeographicalIdentifier(),
				originalMapArea.getIdentifier(),
				originalMapArea.getLabel(),
				originalMapArea.getBand(),
				originalMapArea.getIntersectCount(),
				originalMapArea.getDistanceFromNearestSource(),
				originalMapArea.getNearestRifShapePolyId(),
				originalMapArea.getExposureValue());
		return cloneMapArea;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalMapAreas the original map areas
	 * @return the array list
	 */
	public static ArrayList<MapArea> createCopy(
		final ArrayList<MapArea> originalMapAreas) {
		
		if (originalMapAreas == null) {
			return null;
		}
		
		ArrayList<MapArea> cloneMapAreas = new ArrayList<MapArea>();
		
		for (MapArea originalMapArea : originalMapAreas) {
			MapArea cloneMapArea = MapArea.createCopy(originalMapArea);
			cloneMapAreas.add(cloneMapArea);			
		}
		
		return cloneMapAreas;
	}

	/**
	 * Identify list of unique areas.
	 *
	 * @param sourceMapAreaList the source map area list
	 * @param destinationMapAreaList the destination map area list
	 * @return the array list
	 */
	public static ArrayList<MapArea> identifyListOfUniqueAreas(
		final ArrayList<MapArea> sourceMapAreaList,			
		final ArrayList<MapArea> destinationMapAreaList) {

		//@TODO It might be more efficient to just have a nested loop 
		//instead of making a method call to "listContainsMapArea"		
		ArrayList<MapArea> results = new ArrayList<MapArea>();

		for (MapArea sourceMapArea : sourceMapAreaList) {
			if (sourceMapArea.findItemInList(destinationMapAreaList) == null) {
				results.add(sourceMapArea);
			}
		}
		
		return results;
	}
	
	
	/**
	 * Identify list of duplicates.
	 *
	 * @param sourceMapAreaList the source map area list
	 * @param destinationMapAreaList the destination map area list
	 * @return the array list
	 */
	public static ArrayList<MapArea> identifyListOfDuplicates(
		final ArrayList<MapArea> sourceMapAreaList,			
		final ArrayList<MapArea> destinationMapAreaList) {

		//@TODO It might be more efficient to just have a nested loop 
		//instead of making a method call to "listContainsMapArea"		
		ArrayList<MapArea> duplicateMapAreas = new ArrayList<MapArea>();

		for (MapArea sourceMapArea : sourceMapAreaList) {			
			for (MapArea destinationMapArea : destinationMapAreaList) {
				if (sourceMapArea.hasIdenticalContents(destinationMapArea)) {
					duplicateMapAreas.add(sourceMapArea);
					break;
				}
			}
		}
		
		return duplicateMapAreas;
	}
	

	/**
	 * Identify duplicates within list.
	 *
	 * @param mapAreas the map areas
	 * @return the array list
	 */
	public static ArrayList<MapArea> identifyDuplicatesWithinList(
		final ArrayList<MapArea> mapAreas) {
		
		ArrayList<MapArea> results = new ArrayList<MapArea>();
		
		HashSet<String> mapAreaFromIdentifier = new HashSet<String>();
		for (MapArea mapArea : mapAreas) {
			String geographicalIdentifier = mapArea.getGeographicalIdentifier();
			
			if (mapAreaFromIdentifier.contains(geographicalIdentifier)  == true) {
				results.add(mapArea);
			}
			else {
				mapAreaFromIdentifier.add(geographicalIdentifier);
			}
		}
		
		
		return results;
	}
		
	/**
	 * Find item in list.
	 *
	 * @param otherMapAreas the other map areas
	 * @return the map area
	 */
	public MapArea findItemInList(
		final ArrayList<MapArea> otherMapAreas) {		

		for (MapArea otherMapArea : otherMapAreas) {
			if (hasIdenticalContents(otherMapArea)) {
				return otherMapArea;
			}
		}		
		return null;
	}
	
	/**
	 * Gets the alphabetised list.
	 *
	 * @param mapAreas the map areas
	 * @return the alphabetised list
	 */
	public static ArrayList<String> getAlphabetisedList(
		final ArrayList<MapArea> mapAreas) {
		
		int numberOfMapAreas = mapAreas.size();
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < numberOfMapAreas; i++) {
			results.add(mapAreas.get(i).getDisplayName());
		}
		
		Collections.sort(results);
		return results;
	}
	
	
	public void identifyDifferences(
		final MapArea anotherMapArea,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherMapArea, 
			differences);		
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param mapAreasA the map areas a
	 * @param mapAreasB the map areas b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<MapArea> mapAreasA,
		final ArrayList<MapArea> mapAreasB) {
		
		if (((mapAreasA == null) && (mapAreasB != null)) ||
			((mapAreasA != null) && (mapAreasB == null))) {
			return false;
		}
		
		if ( mapAreasA.size() != mapAreasB.size() ) {
			return false;
		}
		
		//assumes the lists have the same size
		for (int i = 0; i < mapAreasA.size(); i++) {
			MapArea mapAreaA = mapAreasA.get(i);
			MapArea mapAreaB = mapAreasB.get(i);
			if (mapAreaA.hasIdenticalContents(mapAreaB) == false) {
				return false;
			}
		}
		
		return true;		
	}
			
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(
		final String label) {

		this.label = label;
	}
		
	/**
	 * Gets the band.
	 *
	 * @return the band
	 */
	public Integer getBand() {
		return band;
	}

	/**
	 * Sets the band.
	 *
	 * @param band the new band
	 */
	public void setBand(
		final Integer band) {

		this.band = band;
	}

	/**
	 * Gets the intersectCount.
	 *
	 * @return the intersectCount
	 */
	public Integer getIntersectCount() {
		return intersectCount;
	}

	/**
	 * Sets the intersectCount.
	 *
	 * @param intersectCount the new intersectCount
	 */
	public void setIntersectCount(
		final Integer intersectCount) {

		this.intersectCount = intersectCount;
	}
		
	/**
	 * Gets the distanceFromNearestSource.
	 *
	 * @return the distanceFromNearestSource
	 */
	public Double getDistanceFromNearestSource() {
		return distanceFromNearestSource;
	}

	/**
	 * Sets the distanceFromNearestSource.
	 *
	 * @param distanceFromNearestSource the new distanceFromNearestSource
	 */
	public void setDistanceFromNearestSource(
		final Double distanceFromNearestSource) {

		this.distanceFromNearestSource = distanceFromNearestSource;
	}
		
	/**
	 * Gets the nearestRifShapePolyId.
	 *
	 * @return the nearestRifShapePolyId
	 */
	public String getNearestRifShapePolyId() {
		return nearestRifShapePolyId;
	}

	/**
	 * Sets the nearestRifShapePolyId.
	 *
	 * @param nearestRifShapePolyId the new nearestRifShapePolyId
	 */
	public void setNearestRifShapePolyId(
		final String nearestRifShapePolyId) {

		this.nearestRifShapePolyId = nearestRifShapePolyId;
	}
	
	/**
	 * Gets the exposureValue.
	 *
	 * @return the exposureValue
	 */
	public Double getExposureValue() {
		return exposureValue;
	}

	/**
	 * Sets the exposureValue.
	 *
	 * @param exposureValue the new exposureValue
	 */
	public void setExposureValue(
		final Double exposureValue) {

		this.exposureValue = exposureValue;
	}
	
	public String getGeographicalIdentifier() {
		return geographicalIdentifier;
	}

	public void setGeographicalIdentifier(String geographicalIdentifier) {
		this.geographicalIdentifier = geographicalIdentifier;
	}

	/**
	 * Checks for identical contents.
	 *
	 * @param otherMapArea the other map area
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final MapArea otherMapArea) {

		Collator collator = GENERIC_MESSAGES.getCollator();

		String otherLabel = otherMapArea.getLabel();
		if (FieldValidationUtility.hasDifferentNullity(label, otherLabel)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (label != null) {
			//they must both be non-null
			if (collator.equals(label, otherLabel) == false) {
				return false;
			}			
		}

		String otherGeographicalIdentifier 
			= otherMapArea.getGeographicalIdentifier();
		if (FieldValidationUtility.hasDifferentNullity(
				geographicalIdentifier, 
				otherGeographicalIdentifier)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (geographicalIdentifier != null) {
			//they must both be non-null
			if (collator.equals(
					geographicalIdentifier, 
					otherGeographicalIdentifier) == false) {
				return false;
			}			
		}
		
		return super.hasIdenticalContents(otherMapArea);
	}

	static public String[] getMapAreaIdentifierList(
		final ArrayList<MapArea> mapAreas) {
		
		int numberOfMapAreas = mapAreas.size();
		String[] results = new String[mapAreas.size()];
		
		for (int i = 0; i < numberOfMapAreas; i++) {
			results[i] = mapAreas.get(i).getGeographicalIdentifier();
		}

		return results;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType = getRecordType();
		if (label != null) {
			String labelFieldName
				= RIFServiceMessages.getMessage("mapArea.label.label");
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				labelFieldName,
				label);
		}	
		
		if (geographicalIdentifier != null) {
			String geographicalIdentifierFieldName
				= RIFServiceMessages.getMessage("mapArea.geographicalIdentifier.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				geographicalIdentifierFieldName,
				geographicalIdentifier);
		}	
		
	}
	
	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();	
		if (fieldValidationUtility.isEmpty(getGeographicalIdentifier()) == true) {
			String recordType = getRecordType();
			String geographicalIdentifierFieldName
				= RIFServiceMessages.getMessage("mapArea.geographicalIdentifier.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					geographicalIdentifierFieldName);
			errorMessages.add(errorMessage);
		}		

		
		
	//@TODO uncomment this line when we get new release of RIF database
	//05-0302014
/*		
		if (fieldValidationUtility.isEmpty(label)) {
			String recordType = getRecordType();
			String labelFieldName
				= RIFServiceMessages.getMessage("mapArea.label.label");	
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					labelFieldName);
			errorMessages.add(errorMessage);
		}
*/
		countErrors(RIFServiceError.INVALID_MAP_AREA, errorMessages);
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
	

	@Override
	public String getDisplayName() {

		StringBuilder buffer = new StringBuilder();
		buffer.append(getGeographicalIdentifier());
		buffer.append("-");
		buffer.append(label);
		
		return buffer.toString();		
	}
	

	@Override
	public String getRecordType() {

		String recordType
			= RIFServiceMessages.getMessage("mapArea.label");
		return recordType;
	}

}
