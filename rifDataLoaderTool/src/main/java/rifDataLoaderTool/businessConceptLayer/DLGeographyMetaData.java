package rifDataLoaderTool.businessConceptLayer;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.util.FieldValidationUtility;

import java.text.Collator;
import java.util.ArrayList;

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

public class DLGeographyMetaData 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String filePath;
	private ArrayList<DLGeography> geographies;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DLGeographyMetaData() {
		geographies = new ArrayList<DLGeography>();
		filePath = "";
	}

	public static DLGeographyMetaData newInstance() {
		DLGeographyMetaData cloneGeographyMetaData
			= new DLGeographyMetaData();
		return cloneGeographyMetaData;
	}
	
	public static DLGeographyMetaData createCopy(final DLGeographyMetaData originalGeographyMetaData) {
		DLGeographyMetaData cloneGeographyMetaData
			= new DLGeographyMetaData();
		copyInto(originalGeographyMetaData, cloneGeographyMetaData);
		return cloneGeographyMetaData;
	}
	
	public static void copyInto(
		final DLGeographyMetaData source, 
		final DLGeographyMetaData destination) {
		
		destination.setFilePath(source.getFilePath());
		
		ArrayList<DLGeography> originalGeographies
			= source.getGeographies();
		for (DLGeography originalGeography : originalGeographies) {
			DLGeography cloneGeography
				= DLGeography.createCopy(originalGeography);
			destination.addGeography(cloneGeography);
		}		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(final String filePath) {

		this.filePath = filePath;
	}
	
	public String[] getAllGeographyNames() {
		ArrayList<String> geographyNames = new ArrayList<String>();

		for (DLGeography geography : geographies) {
			geographyNames.add(geography.getName());
		}
				
		return geographyNames.toArray(new String[0]);
	}
	
	public DLGeography getGeography(final String targetGeographName) {
		for (DLGeography geography : geographies) {
			String currentGeographyName
				= geography.getName();
			if (currentGeographyName.equals(targetGeographName)) {
				return geography;
			}
		}
		
		return null;
	}
	
	public ArrayList<DLGeography> getGeographies() {
		return geographies;
	}
	
	public void addGeography(final DLGeography geography) {
		geographies.add(geography);
	}

	public void clearGeographies() {
		geographies.clear();
	}
	
	/**
	 * To simplify the code, we assume that if the file paths match then the 
	 * meta data will match as well.
	 * 
	 * @param geographyMetaData
	 * @return
	 */
	public boolean hasIdenticalContents(
		final DLGeographyMetaData otherGeographyMetaData) {
		
		if (otherGeographyMetaData == null) {
			return false;
		}
		
		Collator collator = Collator.getInstance();
		
		String otherFilePath = otherGeographyMetaData.getFilePath();
		
		if (FieldValidationUtility.hasDifferentNullity(filePath, otherFilePath)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (filePath != null) {
			//they must both be non-null
			if (collator.equals(filePath, otherFilePath) == false) {
				return false;
			}			
		}

		return DLGeography.hasIdenticalContents(
			geographies, 
			otherGeographyMetaData.getGeographies());
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

	public String getIdentifier() {
		return filePath;
	}
	
	public String getDisplayName() {
		return filePath;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


