package org.sahsu.rif.dataloader.concepts;

import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.util.FieldValidationUtility;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;

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

public class HealthTheme implements DisplayableListItemInterface {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String name;
	private String description;
	private Date lastModifiedTime;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private HealthTheme() {
		this.name = "";
		this.description = "";
		updateLastModifiedTime();
	}

	private HealthTheme(
		final String name, 
		final String description) {
		
		this.name = name;
		this.description = description;
		updateLastModifiedTime();
	}
	
	public static HealthTheme newInstance(
		final String name, 
		final String description) {
		
		HealthTheme healthTheme 
			 = new HealthTheme(
					name, 
					description);
		return healthTheme;
	}

	public static HealthTheme newInstance() {			
		HealthTheme healthTheme 
			= new HealthTheme();
		return healthTheme;
	}	
	
	public static HealthTheme createCopy(
		final HealthTheme healthTheme) {
		
		HealthTheme cloneHealthTheme
			= HealthTheme.newInstance();
		copyInto(healthTheme, cloneHealthTheme);
		return cloneHealthTheme;
	}
	
	public static void copyInto(
		final HealthTheme source, 
		final HealthTheme destination) {
		
		destination.setName(source.getName());
		destination.setDescription(source.getDescription());
		destination.setLastModifiedTime(source.getLastModifiedTime());
	}
	
	public static boolean hasIdenticalContents(
		final ArrayList<HealthTheme> healthThemesListA,
		final ArrayList<HealthTheme> healthThemesListB) {
			
		if (FieldValidationUtility.hasDifferentNullity(
			healthThemesListA, 
			healthThemesListB)) {
			//reject if one is null and the other is non-null
			return false;
		}		
			
		if (healthThemesListA.size() != healthThemesListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
			
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<HealthTheme> healthThemesA = sortHealthThemes(healthThemesListA);
		ArrayList<HealthTheme> healthThemesB = sortHealthThemes(healthThemesListB);

		int numberOfHealthCodes = healthThemesA.size();
		for (int i = 0; i < numberOfHealthCodes; i++) {
			HealthTheme healthThemeA
				= healthThemesA.get(i);				
			HealthTheme healthThemeB
				= healthThemesB.get(i);
			if (healthThemeA.hasIdenticalContents(healthThemeB) == false) {
				System.out.println("hasIdent 4");
				return false;
			}			
		}
			
		return true;
	}


	private static ArrayList<HealthTheme> sortHealthThemes(
		final ArrayList<HealthTheme> healthThemes) {

		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (HealthTheme healthTheme : healthThemes) {
			sorter.addDisplayableListItem(healthTheme);
		}
		
		ArrayList<HealthTheme> results = new ArrayList<HealthTheme>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			HealthTheme sortedHealthTheme
				= (HealthTheme) sorter.getItemFromIdentifier(identifier);
			results.add(sortedHealthTheme);
		}
		
		return results;
	}
	
	public boolean hasIdenticalContents(final HealthTheme otherHealthTheme) {
		if (otherHealthTheme == null) {
			return false;			
		}
		
		if (this == otherHealthTheme) {
			return true;
		}
		
		Collator collator = Collator.getInstance();

		String otherName = otherHealthTheme.getName();
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (name != null) {
			//they must both be non-null
			if (collator.equals(name, otherName) == false) {
				return false;
			}			
		}		
		
		String otherDescription = otherHealthTheme.getDescription();
		if (FieldValidationUtility.hasDifferentNullity(
			description, 
			otherDescription)) {

			//reject if one is null and the other is non-null
			return false;
		}
		else if (description != null) {
			//they must both be non-null
			if (collator.equals(
				description, 
				otherDescription) == false) {

				return false;
			}			
		}
		
		Date otherLastModifiedTime = otherHealthTheme.getLastModifiedTime();
		if (FieldValidationUtility.hasDifferentNullity(
			lastModifiedTime, 
			otherLastModifiedTime)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (lastModifiedTime.compareTo(otherLastModifiedTime) != 0) {
			//they must both be non-null
			if (collator.equals(name, otherName) == false) {
				return false;
			}			
		}			
		
		
		return true;
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void update(final HealthTheme revisedHealthTheme) {
		HealthTheme.copyInto(revisedHealthTheme, this);
		updateLastModifiedTime();
	}
	
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
	
	public Date getLastModifiedTime() {
		return lastModifiedTime;		
	}
	
	public void setLastModifiedTime(final Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	private void updateLastModifiedTime() {
		lastModifiedTime = new Date(System.currentTimeMillis());
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


