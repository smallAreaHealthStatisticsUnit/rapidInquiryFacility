package org.sahsu.rif.generic.datastorage;

import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.text.Collator;



/**
 * Assumes items are unique, guaranteed by getIdentifier() method
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

public final class DisplayableItemSorter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The displayable item from identifier. */
	private Hashtable<String, DisplayableListItemInterface> displayableItemFromIdentifier;
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new displayable item sorter.
	 */
	public DisplayableItemSorter() {

		displayableItemFromIdentifier = new Hashtable<String, DisplayableListItemInterface>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Clear.
	 */
	public void clear() {
		
		displayableItemFromIdentifier.clear();
	}
	
	/**
	 * Adds the displayable list item.
	 *
	 * @param displayableListItem the displayable list item
	 */
	public void addDisplayableListItem(
		final DisplayableListItemInterface displayableListItem) {
		
		displayableItemFromIdentifier.put(
			displayableListItem.getDisplayName(),
			displayableListItem);
	}

	/**
	 * Sort identifiers list.
	 *
	 * @return the array list
	 */
	public ArrayList<String> sortIdentifiersList() {
		
		ArrayList<String> identifiers = new ArrayList<String>();
		identifiers.addAll(displayableItemFromIdentifier.keySet());
		
		Collator collator = Collator.getInstance();
		Collections.sort(identifiers, collator);
	
		return identifiers;
	}
	
	/**
	 * Gets the item from identifier.
	 *
	 * @param identifier the identifier
	 * @return the item from identifier
	 */
	public DisplayableListItemInterface getItemFromIdentifier(
		final String identifier) {
		
		return displayableItemFromIdentifier.get(identifier);
	}
	
	/**
	 * Sort list.
	 *
	 * @return the array list
	 */
	public ArrayList<DisplayableListItemInterface> sortList() {

		ArrayList<DisplayableListItemInterface> sortedDisplayableListItems
			= new ArrayList<DisplayableListItemInterface>();
		
		ArrayList<String> identifiers = new ArrayList<String>();
		identifiers.addAll(displayableItemFromIdentifier.keySet());
		Collator collator
			= Collator.getInstance();
		Collections.sort(identifiers, collator);
		
		for (String identifier : identifiers) {
			sortedDisplayableListItems.add(displayableItemFromIdentifier.get(identifier));
		}
				
		return sortedDisplayableListItems;		
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
