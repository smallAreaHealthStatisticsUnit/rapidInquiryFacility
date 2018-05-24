package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;
import java.util.Objects;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

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

public class Geography 
	extends AbstractDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private String name;
	private ArrayList<GeographicalResolutionLevel> levels;
			
	
	// ==========================================
	// Section Construction
	// ==========================================

	private Geography() {
		name = "";
		levels = new ArrayList<GeographicalResolutionLevel>();
	}

	public static Geography newInstance() {
		Geography geography
			= new Geography();
		return geography;
	}

	public static final Geography createCopy(
		final Geography originalGeography) {
		
		Geography cloneGeography
			= Geography.newInstance();
		copyInto(
			originalGeography, 
			cloneGeography);
		
		return cloneGeography;
	}
	
	public static final void copyInto(
		final Geography source,
		final Geography destination) {
		
		destination.setIdentifier(source.getIdentifier());
		destination.setName(source.getName());
		
		destination.clearLevels();
		
		ArrayList<GeographicalResolutionLevel> sourceLevels
			= source.getLevels();
		for (GeographicalResolutionLevel sourceLevel : sourceLevels) {
			GeographicalResolutionLevel cloneLevel
				= GeographicalResolutionLevel.createCopy(sourceLevel);
			destination.addLevel(cloneLevel);
		}
		
	}
	
	public static boolean hasIdenticalContents(
		final ArrayList<Geography> geographyListA,
		final ArrayList<Geography> geographyListB) {
		
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
		ArrayList<Geography> geographiesA = sortGeographies(geographyListA);
		ArrayList<Geography> geographiesB = sortGeographies(geographyListB);

		int numberOfHealthCodes = geographiesA.size();
		for (int i = 0; i < numberOfHealthCodes; i++) {
			Geography geographyA
				= geographiesA.get(i);				
			Geography geographyB
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

	private static ArrayList<Geography> sortGeographies(
		final ArrayList<Geography> geographies) {

		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (Geography geography : geographies) {
			sorter.addDisplayableListItem(geography);
		}
		
		ArrayList<Geography> results = new ArrayList<Geography>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			Geography sortedGeography 
				= (Geography) sorter.getItemFromIdentifier(identifier);
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
	
	public ArrayList<GeographicalResolutionLevel> getLevels() {
		return levels;
	}
	
	public ArrayList<String> getLevelCodeNames() {
		ArrayList<String> levelCodeNames = new ArrayList<String>();
		
		for (GeographicalResolutionLevel level : levels) {
			String currentLevelName
				= level.getDatabaseFieldName();
			levelCodeNames.add(currentLevelName);
		}
				
		return levelCodeNames;		
	}
	
	public ArrayList<String> getLevelNames() {

		ArrayList<String> levelNames = new ArrayList<String>();
		
		for (GeographicalResolutionLevel level : levels) {
			String currentLevelName
				= level.getDisplayName();
			levelNames.add(currentLevelName);
		}
				
		return levelNames;
	}
	
	public GeographicalResolutionLevel getLevel(final String targetLevelName) {

		for (GeographicalResolutionLevel level : levels) {
			String currentLevelName
				= level.getDisplayName();
			if (currentLevelName.equals(targetLevelName)) {
				return level;
			}
		}
		
		return null;
	}
	
	public void addLevel(
		final GeographicalResolutionLevel level) {
		
		levels.add(level);
	}
	
	public void addLevel(
		final int order, 
		final String displayName, 
		final String databaseFieldName) {
		
		GeographicalResolutionLevel level 
			= GeographicalResolutionLevel.newInstance(
				order, 
				displayName, 
				databaseFieldName);
		
		levels.add(level);
	}
	
	public void clearLevels() {
		levels.clear();
	}
	
	public boolean hasIdenticalContents(
		final Geography otherGeography) {
		
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
		return GeographicalResolutionLevel.hasIdenticalContents(
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
				= GENERIC_MESSAGES.getMessage(
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


