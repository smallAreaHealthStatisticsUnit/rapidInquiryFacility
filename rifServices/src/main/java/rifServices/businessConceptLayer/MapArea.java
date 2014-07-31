package rifServices.businessConceptLayer;

import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceSecurityException;
import rifServices.util.FieldValidationUtility;

import java.util.ArrayList;
import java.text.Collator;
import java.util.Collections;
import java.util.HashSet;


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

public final class MapArea 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The label. */
	private String label;
	
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
		final String identifier,
		final String label) {
		
		setIdentifier(identifier);
		setLabel(label);
	}
	
	/**
	 * Instantiates a new map area.
	 */
	private MapArea() {
		setIdentifier("");
		setLabel("");
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
		final String identifier,
		final String label) {
		
		MapArea mapArea = new MapArea(identifier, label);
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
				originalMapArea.getIdentifier(),
				originalMapArea.getLabel());
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
			String identifier = mapArea.getIdentifier();
			
			if (mapAreaFromIdentifier.contains(identifier)  == true) {
				results.add(mapArea);
			}
			else {
				mapAreaFromIdentifier.add(identifier);
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
	 * Checks for identical contents.
	 *
	 * @param otherMapArea the other map area
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final MapArea otherMapArea) {

		Collator collator = RIFServiceMessages.getCollator();

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
		
		return super.hasIdenticalContents(otherMapArea);
	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#checkSecurityViolations()
	 */
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
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#checkErrors()
	 */
	public void checkErrors() 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();	
		if (fieldValidationUtility.isEmpty(getIdentifier()) == true) {
			String recordType = getRecordType();
			String identifierFieldName
				= RIFServiceMessages.getMessage("general.fieldNames.identifier.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					identifierFieldName);
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
				= RIFServiceMessages.getMessage(
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
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#getDisplayName()
	 */
	@Override
	public String getDisplayName() {

		StringBuilder buffer = new StringBuilder();
		buffer.append(getIdentifier());
		buffer.append("-");
		buffer.append(label);
		
		return buffer.toString();		
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#getRecordType()
	 */
	@Override
	public String getRecordType() {

		String recordType
			= RIFServiceMessages.getMessage("mapArea.label");
		return recordType;
	}

}
