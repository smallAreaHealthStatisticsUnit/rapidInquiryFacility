package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifGenericLibrary.dataStorageLayer.DisplayableItemSorter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.util.FieldValidationUtility;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This is the concept of a Geography as the Data Loader Tool would understand
 * it, rather than the {@link rifServices.businessConceptLayer.Geography}
 * concept that is used within the web-based epidemiology applications.
 * <p>
 * Here, a geography has a name (eg: England) and then a collection of shape
 * files which cover the same area but at different resolutions (eg: England
 * districts, wards, regions).
 * </p>
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
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

public class DLGeography 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String name;
	private ArrayList<DLGeographicalResolutionLevel> levels;
			
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DLGeography() {
		name = "";
		levels = new ArrayList<DLGeographicalResolutionLevel>();
	}

	public static DLGeography newInstance() {
		DLGeography geography
			= new DLGeography();
		return geography;
	}

	public static final DLGeography createCopy(
		final DLGeography originalGeography) {
		
		DLGeography cloneGeography
			= DLGeography.newInstance();
		copyInto(
			originalGeography, 
			cloneGeography);
		
		return cloneGeography;
	}
	
	public static final void copyInto(
		final DLGeography source,
		final DLGeography destination) {
		
		destination.setIdentifier(source.getIdentifier());
		destination.setName(source.getName());
		
		destination.clearLevels();
		
		ArrayList<DLGeographicalResolutionLevel> sourceLevels
			= source.getLevels();
		for (DLGeographicalResolutionLevel sourceLevel : sourceLevels) {
			DLGeographicalResolutionLevel cloneLevel
				= DLGeographicalResolutionLevel.createCopy(sourceLevel);
			destination.addLevel(cloneLevel);
		}
		
	}
	
	public static boolean hasIdenticalContents(
		final ArrayList<DLGeography> geographyListA,
		final ArrayList<DLGeography> geographyListB) {
		
		if (FieldValidationUtility.hasDifferentNullity(
			geographyListA, 
			geographyListB)) {
			//reject if one is null and the other is non-null
			return false;
		}		
		
		if (geographyListA.size() != geographyListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
		
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<DLGeography> geographiesA = sortGeographies(geographyListA);
		ArrayList<DLGeography> geographiesB = sortGeographies(geographyListB);

		int numberOfHealthCodes = geographiesA.size();
		for (int i = 0; i < numberOfHealthCodes; i++) {
			DLGeography geographyA
				= geographiesA.get(i);				
			DLGeography geographyB
				= geographiesB.get(i);
			if (geographyA.hasIdenticalContents(geographyB) == false) {					
				return false;
			}			
		}
		
		return true;
	}

	/**
	 * Sort investigations.
	 *
	 * @param investigations the investigations
	 * @return the array list
	 */

	private static ArrayList<DLGeography> sortGeographies(
		final ArrayList<DLGeography> geographies) {

		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (DLGeography geography : geographies) {
			sorter.addDisplayableListItem(geography);
		}
		
		ArrayList<DLGeography> results = new ArrayList<DLGeography>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			DLGeography sortedGeography 
				= (DLGeography) sorter.getItemFromIdentifier(identifier);
			results.add(sortedGeography);
		}
			
		return results;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
	
	public ArrayList<DLGeographicalResolutionLevel> getLevels() {
		return levels;
	}
	
	public ArrayList<String> getLevelNames() {
		ArrayList<String> levelNames = new ArrayList<String>();
		
		for (DLGeographicalResolutionLevel level : levels) {
			String currentLevelName
				= level.getDisplayName();
			levelNames.add(currentLevelName);
		}
				
		return levelNames;
	}
	
	public DLGeographicalResolutionLevel getLevel(final String targetLevelName) {

		for (DLGeographicalResolutionLevel level : levels) {
			String currentLevelName
				= level.getDisplayName();
			if (currentLevelName.equals(targetLevelName)) {
				return level;
			}
		}
		
		return null;
	}
	
	public void addLevel(
		final DLGeographicalResolutionLevel level) {
		
		levels.add(level);
	}
	
	public void addLevel(
		final int order, 
		final String displayName, 
		final String databaseFieldName) {
		
		DLGeographicalResolutionLevel level 
			= DLGeographicalResolutionLevel.newInstance(
				order, 
				displayName, 
				databaseFieldName);
		
		levels.add(level);
	}
	
	public void clearLevels() {
		levels.clear();
	}
	
	public boolean hasIdenticalContents(
		final DLGeography otherGeography) {
		
		if (otherGeography == null) {
			return false;			
		}
		
		if (this == otherGeography) {
			return true;
		}
		
		if (Objects.deepEquals(
			name, 
			otherGeography.getName()) == false) {

			return false;
		}
		
		//go through the levels
		return DLGeographicalResolutionLevel.hasIdenticalContents(
			levels, 
			otherGeography.getLevels());
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void checkErrors()
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordName
			= RIFDataLoaderToolMessages.getMessage(
				"dlGeography.singular.label");
		
				
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String nameFieldName
			= RIFDataLoaderToolMessages.getMessage("dlGeography.name.label");
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					nameFieldName);
			errorMessages.add(errorMessage);
		}
		
		countErrors(
			RIFDataLoaderToolError.INVALID_DATA_LOADER_GEOGRAPHY, 
			errorMessages);
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	public String getDisplayName() {
		return name;
	}
	
}


