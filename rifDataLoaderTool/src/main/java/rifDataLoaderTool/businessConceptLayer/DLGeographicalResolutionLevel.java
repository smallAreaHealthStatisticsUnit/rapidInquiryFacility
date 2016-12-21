package rifDataLoaderTool.businessConceptLayer;

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

public class DLGeographicalResolutionLevel {

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

	private DLGeographicalResolutionLevel(
		final int order,
		final String displayName,
		final String databaseFieldName) {

		this.order = order;
		this.displayName = displayName;
		this.databaseFieldName = databaseFieldName;		
	}

	private DLGeographicalResolutionLevel() {
		order = 0;
		displayName = "";
		databaseFieldName = "";
	}
	
	public static DLGeographicalResolutionLevel newInstance(
		final int order,
		final String displayName,
		final String databaseFieldName) {
		
		DLGeographicalResolutionLevel level
			 = new DLGeographicalResolutionLevel(
				order,
				displayName,
				databaseFieldName);
		
		return level;		
	}

	
	public static DLGeographicalResolutionLevel newInstance() {
		
		DLGeographicalResolutionLevel level
			 = new DLGeographicalResolutionLevel(1, "", "");
		
		return level;		
	}
	
	public static DLGeographicalResolutionLevel createCopy(
		final DLGeographicalResolutionLevel originalLevel) {
		
		DLGeographicalResolutionLevel cloneLevel
			= new DLGeographicalResolutionLevel();
		copyInto(
			originalLevel, 
			cloneLevel);
		
		return cloneLevel;
	}
	
	public static void copyInto(
		final DLGeographicalResolutionLevel source,
		final DLGeographicalResolutionLevel destination) {
		
		destination.setOrder(source.getOrder());
		destination.setDisplayName(source.getDisplayName());
		destination.setDatabaseFieldName(source.getDatabaseFieldName());		
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

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


