package rifDataLoaderTool.businessConceptLayer;

import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;

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

public class DLHealthTheme implements DisplayableListItemInterface {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String name;
	private String description;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DLHealthTheme() {
		this.name = "";
		this.description = "";
	}

	private DLHealthTheme(
		final String name, 
		final String description) {
		
		this.name = name;
		this.description = description;
	}
	
	public static DLHealthTheme newInstance(
		final String name, 
		final String description) {
		
		DLHealthTheme healthTheme 
			 = new DLHealthTheme(
					name, 
					description);
		return healthTheme;
	}

	public static DLHealthTheme newInstance() {			
		DLHealthTheme healthTheme 
			= new DLHealthTheme();
		return healthTheme;
	}	
	
	public static DLHealthTheme createCopy(
		final DLHealthTheme healthTheme) {
		
		DLHealthTheme cloneHealthTheme
			= DLHealthTheme.newInstance();
		copyInto(healthTheme, cloneHealthTheme);
		return cloneHealthTheme;
	}
	
	public static void copyInto(
		final DLHealthTheme source, 
		final DLHealthTheme destination) {
		
		destination.setName(source.getName());
		destination.setDescription(source.getDescription());
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
		
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	//Interface: Displayable List Item
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


