package org.sahsu.rif.dataloader.presentation.interactive;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.sahsu.rif.dataloader.concepts.DescriptiveConfigurationItem;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;

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

public class ConfigurationHintTableModel extends AbstractTableModel {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int REGULAR_EXPRESSION_COLUMN = 0;
	private static final int REGULAR_EXPRESSION_DESCRIPTION = 1;
		
	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<DescriptiveConfigurationItem> configurationHintItems;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ConfigurationHintTableModel() {
		configurationHintItems = new ArrayList<DescriptiveConfigurationItem>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public int getRowCount() {
		return configurationHintItems.size();
	}
	
	public Object getValueAt(final int row, int column) {
		DescriptiveConfigurationItem configurationHintItem
			= configurationHintItems.get(row);
		if (column == REGULAR_EXPRESSION_COLUMN) {
			return configurationHintItem.getDisplayName();
		}
		else {
			//regular expression hint description
			return configurationHintItem.getDescription();
		}
	}
	
	public String getColumnName(final int column) {
		if (column == REGULAR_EXPRESSION_COLUMN) {
			String regularExpressionColumn
				= RIFDataLoaderToolMessages.getMessage("configurationHintTableModel.pattern.label");
			return regularExpressionColumn;			
		}
		else {
			String regularExpressionColumn
				= RIFDataLoaderToolMessages.getMessage("configurationHintTableModel.description.label");
			return regularExpressionColumn;			
		}
	}
	
	public DescriptiveConfigurationItem getRow(final int rowIndex) {
		return configurationHintItems.get(rowIndex);
	}
	
	public int getColumnCount() {
		return 2;
	}
	
	public boolean isCellEditable(final int row, final int column) {
		return false;
	}
	
	public void addHintItem(final DescriptiveConfigurationItem descriptiveConfigurationItem) {
		configurationHintItems.add(descriptiveConfigurationItem);
	}
	
	public void deleteSelectedItems(final int[] selectedIndices) {
		
		//First we gather up all the references to items we want to delete
		//if we were to delete by index, we'd soon encounter an out of range index
		//exception.
		ArrayList<DescriptiveConfigurationItem> itemsToDelete
			= new ArrayList<DescriptiveConfigurationItem>();
		for (int i = 0; i < selectedIndices.length; i++) {		
			itemsToDelete.add(configurationHintItems.get(selectedIndices[i]));
		}
		
		for (DescriptiveConfigurationItem itemToDelete : itemsToDelete) {
			configurationHintItems.remove(itemToDelete);
		}		
	}
	
	public ArrayList<DescriptiveConfigurationItem> getData() {
		return configurationHintItems;
	}
	
	public void setData(final ArrayList<DescriptiveConfigurationItem> configurationHintItems) {
		
		this.configurationHintItems = configurationHintItems;
	}
	
	public boolean isEmpty() {
		return configurationHintItems.isEmpty();
	}
	
	public int getIndex(final DescriptiveConfigurationItem configurationHintItem) {
		return configurationHintItems.indexOf(configurationHintItem);
	}
	
	public void clear() {
		configurationHintItems.clear();
	}
	
	public int shiftItemUpInTable(final int originalRowPosition) {

		int newRowPosition = originalRowPosition - 1;
		
		if (newRowPosition < 0) {
			newRowPosition = 0;
		}

		replaceItem(originalRowPosition, newRowPosition);
		return newRowPosition;
	}

	public int shiftItemDownInTable(final int originalRowPosition) {

		int newRowPosition = originalRowPosition + 1;
		int currentNumberOfHints = configurationHintItems.size();
		if (newRowPosition >= currentNumberOfHints) {
			newRowPosition = currentNumberOfHints - 1;
		}

		replaceItem(originalRowPosition, newRowPosition);
		return newRowPosition;
	}
	
	private void replaceItem(
		final int originalRowPosition,
		final int newRowPosition) {
		
		if (originalRowPosition == newRowPosition) {
			return;
		}

		DescriptiveConfigurationItem selectedItem
			= configurationHintItems.get(originalRowPosition);
		
		if (newRowPosition != originalRowPosition) {
			configurationHintItems.remove(originalRowPosition);		
			configurationHintItems.add(newRowPosition, selectedItem);
		}
		fireTableDataChanged();		
	}
	
	public void updateItem(final DescriptiveConfigurationItem updatedItem) {

		for (int row = 0; row < configurationHintItems.size(); row++) {
			DescriptiveConfigurationItem currentItem
				= configurationHintItems.get(row);
			if (updatedItem == currentItem) {
				fireTableCellUpdated(row, 0);
				fireTableCellUpdated(row, 1);
				break;
			}
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


