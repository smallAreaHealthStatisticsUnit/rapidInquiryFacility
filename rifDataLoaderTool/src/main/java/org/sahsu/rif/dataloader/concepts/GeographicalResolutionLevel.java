package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;

/**
 *
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

public class GeographicalResolutionLevel 
	extends AbstractDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private int order;
	private String displayName;
	private String databaseFieldName;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private GeographicalResolutionLevel(
		final int order,
		final String displayName,
		final String databaseFieldName) {

		this.order = order;
		this.displayName = displayName;
		this.databaseFieldName = databaseFieldName;		
	}

	private GeographicalResolutionLevel() {
		order = 0;
		displayName = "";
		databaseFieldName = "";
	}
	
	public static GeographicalResolutionLevel newInstance(
		final int order,
		final String displayName,
		final String databaseFieldName) {
		
		GeographicalResolutionLevel level
			 = new GeographicalResolutionLevel(
				order,
				displayName,
				databaseFieldName);
		
		return level;		
	}

	
	public static GeographicalResolutionLevel newInstance() {
		
		GeographicalResolutionLevel level
			 = new GeographicalResolutionLevel(1, "", "");
		
		return level;		
	}
	
	public static GeographicalResolutionLevel createCopy(
		final GeographicalResolutionLevel originalLevel) {
		
		GeographicalResolutionLevel cloneLevel
			= new GeographicalResolutionLevel();
		copyInto(
			originalLevel, 
			cloneLevel);
		
		return cloneLevel;
	}
	
	public static void copyInto(
		final GeographicalResolutionLevel source,
		final GeographicalResolutionLevel destination) {
		
		destination.setOrder(source.getOrder());
		destination.setDisplayName(source.getDisplayName());
		destination.setDatabaseFieldName(source.getDatabaseFieldName());		
	}
	
	public static final boolean hasIdenticalContents(
		final ArrayList<GeographicalResolutionLevel> levelsA, 
		final ArrayList<GeographicalResolutionLevel> levelsB) {
		
		if (FieldValidationUtility.hasDifferentNullity(
			levelsA, 
			levelsB)) {
			//reject if one is null and the other is non-null
			return false;
		}
				
		if (levelsA.size() != levelsB.size() ) {
			//reject if lists do not have the same size
			return false;
		}		
		
		ArrayList<GeographicalResolutionLevel> sortedALevels
			= sortLevels(levelsA);
		ArrayList<GeographicalResolutionLevel> sortedBLevels
			= sortLevels(levelsB);
	
		int numberOfHealthCodes = sortedALevels.size();
		for (int i = 0; i < numberOfHealthCodes; i++) {
			GeographicalResolutionLevel levelA
				= sortedALevels.get(i);				
			GeographicalResolutionLevel levelB
				= sortedBLevels.get(i);
			if (levelA.hasIdenticalContents(levelB) == false) {					
				return false;
			}			
		}
				
		return true;
	}
	
	public static ArrayList<GeographicalResolutionLevel> sortLevels(
		final ArrayList<GeographicalResolutionLevel> levels) {
		
		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (GeographicalResolutionLevel level : levels) {
			sorter.addDisplayableListItem(level);
		}
		
		ArrayList<GeographicalResolutionLevel> results 
			= new ArrayList<GeographicalResolutionLevel>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			GeographicalResolutionLevel sortedLevel 
				= (GeographicalResolutionLevel) sorter.getItemFromIdentifier(identifier);
			results.add(sortedLevel);
		}
			
		return results;	
	}
	
	public boolean hasIdenticalContents(
		final GeographicalResolutionLevel otherLevel) {
		
		if (otherLevel == null) {
			return false;
		}
		
		if (order != otherLevel.getOrder()) {
			return false;
		}

		Collator collator = Collator.getInstance();
		String otherDisplayName = otherLevel.getDisplayName();
		if (FieldValidationUtility.hasDifferentNullity(
			displayName, 
			otherDisplayName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		if (displayName != null) {
			//they must both be non-null
			if (collator.equals(displayName, otherDisplayName) == false) {
				return false;
			}	
		}		
			
		String otherDatabaseFieldName = otherLevel.getDatabaseFieldName();
		if (FieldValidationUtility.hasDifferentNullity(
			databaseFieldName, 
			otherDatabaseFieldName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		if (databaseFieldName != null) {
			//they must both be non-null
			if (collator.equals(databaseFieldName, otherDatabaseFieldName) == false) {
				return false;
			}	
		}		

		return true;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDatabaseFieldName() {
		return databaseFieldName;
	}

	public void setDatabaseFieldName(final String databaseFieldName) {
		this.databaseFieldName = databaseFieldName;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void checkErrors() 
		throws RIFServiceException {
		
		
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

}


