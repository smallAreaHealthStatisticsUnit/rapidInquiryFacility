package rifDataLoaderTool.businessConceptLayer;

import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;

import java.util.ArrayList;

/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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
	implements DisplayableListItemInterface {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String name;
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
		
		destination.setName(source.getName());
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(final String filePath) {

		this.filePath = filePath;
	}
	
	public ArrayList<DLGeography> getGeographies() {
		return geographies;
	}
	
	public void addGeography(final DLGeography geography) {
		geographies.add(geography);
	}
	
	/**
	 * To simplify the code, we assume that if the file paths match then the 
	 * meta data will match as well.
	 * 
	 * @param geographyMetaData
	 * @return
	 */
	public boolean hasIdenticalContents(final DLGeographyMetaData geographyMetaData) {
		
		return true;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	public String getIdentifier() {
		return name;
	}
	
	public String getDisplayName() {
		return name;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


