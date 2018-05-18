package org.sahsu.rif.generic.presentation;

import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComboBox;

import org.sahsu.rif.generic.system.Messages;

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
 * Copyright 2017 Imperial College London, developed by the Small Area
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


public final class OrderedListComboBox {

// ==========================================
// Section Constants
// ==========================================

	private static final String CHOOSE_PROMPT
		= Messages.genericMessages().getMessage("comboBox.choices.choose");

// ==========================================
// Section Properties
// ==========================================	
	
	//Data
	/** The alphabetically sort items. */
	private boolean alphabeticallySortItems;
	/** The item from list name. */
	private HashMap<String, DisplayableListItemInterface> itemFromListName;	
	/** The list items. */
	private Vector<String> listItems;

	//GUI Components
	/** The combo box. */
	private JComboBox<String> comboBox;
	
		
// ==========================================
// Section Construction
// ==========================================
    /**
 * Instantiates a new ordered list combo box.
 *
 * @param userInterfaceFactory the user interface factory
 */
public OrderedListComboBox(UserInterfaceFactory userInterfaceFactory) {
	
		itemFromListName = new HashMap<String, DisplayableListItemInterface>();
		listItems = new Vector<String>();
		
		alphabeticallySortItems = true;
		comboBox = userInterfaceFactory.createComboBox(listItems);
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the combo box.
	 *
	 * @return the combo box
	 */
	public JComboBox<String> getComboBox() {
		
		return comboBox;
	}
	
	/**
	 * Checks if is combo box.
	 *
	 * @param object the object
	 * @return true, if is combo box
	 */
	public boolean isComboBox(
		Object object) {

		if (object == comboBox) {
			return true;
		}
		return false;
	}
	
	/**
	 * Adds the action listener.
	 *
	 * @param actionListener the action listener
	 */
	public void addActionListener(
		ActionListener actionListener) {

		comboBox.addActionListener(actionListener);
	}

	/**
	 * Removes the action listener.
	 *
	 * @param actionListener the action listener
	 */
	public void removeActionListener(
		ActionListener actionListener) {

		comboBox.removeActionListener(actionListener);
	}	
	
	/**
	 * Clear action listeners.
	 */
	public void clearActionListeners() {
		
		ActionListener[] actionListeners
			= comboBox.getActionListeners();
		for (ActionListener actionListener : actionListeners) {
			comboBox.removeActionListener(actionListener);			
		}
	}
	
	/**
	 * Sets the enabled.
	 *
	 * @param isEnabled the new enabled
	 */
	public void setEnabled(
		boolean isEnabled) {

		comboBox.setEnabled(isEnabled);
	}
	
	/**
	 * Clear list.
	 */
	public void clearList() {
		
		itemFromListName.clear();
		listItems.clear();
		comboBox.setSelectedIndex(-1);
	}
	
    /**
     * Sets the alphabetically sort items.
     *
     * @param alphabeticallySortItems the new alphabetically sort items
     */
    public void setAlphabeticallySortItems(
    	boolean alphabeticallySortItems) {

    	this.alphabeticallySortItems = alphabeticallySortItems;
    }
    
    /**
     * Checks if is alphabetically sorted.
     *
     * @return true, if is alphabetically sorted
     */
    public boolean isAlphabeticallySorted() {
    	
    	return alphabeticallySortItems;
    }
	
	
	/**
	 * Adds the list item.
	 *
	 * @param listItem the list item
	 */
	public void addListItem(
		DisplayableListItemInterface listItem) {		
		
		itemFromListName.put(listItem.getDisplayName(), listItem);		
		listItems.add(listItem.getDisplayName());				
		if (alphabeticallySortItems == true) {
			Collections.sort(listItems);			
		}
	}
	
	/**
	 * Gets the first item.
	 *
	 * @return the first item
	 */
	public DisplayableListItemInterface getFirstItem() {
		
		if (listItems.isEmpty()) {
			return null;
		}
		
		DisplayableListItemInterface firstItem
			= itemFromListName.get(listItems.get(0));
		return firstItem;
	}
	
	/**
	 * Gets the selected item.
	 *
	 * @return the selected item
	 */
	public DisplayableListItemInterface getSelectedItem() {
		
		String selectedDisplayName
			= (String) comboBox.getSelectedItem();
		DisplayableListItemInterface selectedItem
			= itemFromListName.get(selectedDisplayName);
		return selectedItem;
	}
	
	/**
	 * Gets the number of items.
	 *
	 * @return the number of items
	 */
	public int getNumberOfItems() {
		
		return itemFromListName.size();
	}
	
	/**
	 * Select first item.
	 */
	public void selectFirstItem() {
		
		if (comboBox.getItemCount() > 0) {
			comboBox.setSelectedIndex(0);
		}
	}
	
	/**
	 * Select last item.
	 */
	public void selectLastItem() {
		
		int size = comboBox.getItemCount();
		if (size > 0) {
			comboBox.setSelectedIndex(size - 1);			
		}
	}
	
	/**
	 * Sets the selected item.
	 *
	 * @param ithItem the new selected item
	 */
	public void setSelectedItem(
		int ithItem) {
		
		comboBox.setSelectedIndex(ithItem);
	}
	
	/**
	 * Contains.
	 *
	 * @param displayableListItem the displayable list item
	 * @return true, if successful
	 */
	public boolean contains(
		DisplayableListItemInterface displayableListItem) {
		
		if (displayableListItem == null) {
			return false;
		}
		
		return listItems.contains(displayableListItem.getDisplayName());
	}
	
	/**
	 * Sets the selected item.
	 *
	 * @param displayName the new selected item
	 */
	public void setSelectedItem(
		String displayName) {
		
		if (displayName == null) {
			comboBox.setSelectedItem(CHOOSE_PROMPT);			
		}
		comboBox.setSelectedItem(displayName);		
	}
	
	/**
	 * Sets the selected item.
	 *
	 * @param displayableListItem the new selected item
	 */
	public void setSelectedItem(
		DisplayableListItemInterface displayableListItem) {
		
		if (displayableListItem == null) {
			setSelectedItem(CHOOSE_PROMPT);
		}
		else {			
			String displayName 
				= displayableListItem.getDisplayName();
			setSelectedItem(displayName);
		}
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
