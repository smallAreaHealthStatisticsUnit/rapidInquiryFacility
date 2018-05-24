package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import org.sahsu.rif.dataloader.concepts.DescriptiveConfigurationItem;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.ListEditingButtonPanel;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;

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

public class ConfigurationHintTablePanel
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;

	private ConfigurationHintTableModel tableModel;	
	private JPanel panel;
	private JTable table;
	private ListSelectionModel listSelectionModel;
	private ListEditingButtonPanel listEditingButtonPanel;
	private JButton shiftItemUpButton;
	private JButton shiftItemDownButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ConfigurationHintTablePanel(
		final UserInterfaceFactory userInterfaceFactory) {
		this.userInterfaceFactory = userInterfaceFactory;
		
		tableModel = new ConfigurationHintTableModel();
		table = userInterfaceFactory.createTable(tableModel);
		listSelectionModel
			= table.getSelectionModel();
		listSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		buildUI();
	}
	
	private void buildUI() {
		
		panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();	
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(table);
		panel.add(scrollPane, panelGC);
	
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		listEditingButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		listEditingButtonPanel.includeAddButton("");
		listEditingButtonPanel.includeDeleteButton("");
		
		String shiftItemUpButtonText
			= RIFDataLoaderToolMessages.getMessage("general.buttons.shiftUp.label");
		shiftItemUpButton
			= userInterfaceFactory.createButton(shiftItemUpButtonText);
		String shiftItemDownButtonText
			= RIFDataLoaderToolMessages.getMessage("general.buttons.shiftDown.label");
		shiftItemDownButton
			= userInterfaceFactory.createButton(shiftItemDownButtonText);
		listEditingButtonPanel.addSpecialisedButton(shiftItemUpButton);
		listEditingButtonPanel.addSpecialisedButton(shiftItemDownButton);
		
		panel.add(listEditingButtonPanel.getPanel(), panelGC);
			
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public JPanel getPanel() {
		return panel;
		
	}
	
	public ArrayList<DescriptiveConfigurationItem> getAllHintItems() {
		return tableModel.getData();
	}
	
	public void setData(final ArrayList<DescriptiveConfigurationItem> configurationHintItems) {
		tableModel.setData(configurationHintItems);
		
		selectFirstItem();
		updateUIWithListState();
	}
	
	public DescriptiveConfigurationItem getSelectedHintItem() {
		int selectedItemIndex = table.getSelectedRow();
		if (selectedItemIndex == -1) {
			return null;
		}
		else {
			return tableModel.getRow(selectedItemIndex);			
		}		
	}
	
	public int getSelectedHintIndex() {
		int selectedItemIndex = table.getSelectedRow();
		return selectedItemIndex;
	}	

	public DescriptiveConfigurationItem getHintItem(final int rowIndex) {
		if (rowIndex == -1) {
			return null;
		}
		else {
			return tableModel.getRow(rowIndex);			
		}		
	}	
	
	public boolean generatedListSelectionChangeEvent(final Object eventSource) {
		return(listSelectionModel == eventSource);
	}	
	
	public void addHintItem(final DescriptiveConfigurationItem descriptiveConfigurationItem) {
		tableModel.addHintItem(descriptiveConfigurationItem);
		tableModel.fireTableDataChanged();
		
		int newItemIndex = tableModel.getIndex(descriptiveConfigurationItem);
		listSelectionModel.setSelectionInterval(newItemIndex, newItemIndex);
		updateUIWithListState();
		//table.updateUI();
	}

	public void updateItem(final DescriptiveConfigurationItem updatedItem) {
		tableModel.updateItem(updatedItem);		
	}
	
	public void clear() {
		tableModel.clear();
		updateUIWithListState();
	}
	
	public void selectFirstItem() {
		if (tableModel.isEmpty() == false) {
			listSelectionModel.setSelectionInterval(0, 0);		
		}
	}
	
	public void refresh() {
		table.updateUI();
	}
	private void shiftItemUp() {
		int oldSelectedItemIndex = table.getSelectedRow();
		int updatedSelectedItemIndex 
			= tableModel.shiftItemUpInTable(oldSelectedItemIndex);
		if (oldSelectedItemIndex != updatedSelectedItemIndex) {
			listSelectionModel.setSelectionInterval(
				updatedSelectedItemIndex, 
				updatedSelectedItemIndex);
			table.updateUI();
		}
	}
	
	private void shiftItemDown() {
		int oldSelectedItemIndex = table.getSelectedRow();
		int updatedSelectedItemIndex
			= tableModel.shiftItemDownInTable(oldSelectedItemIndex);
		if (oldSelectedItemIndex != updatedSelectedItemIndex) {
			listSelectionModel.setSelectionInterval(
				updatedSelectedItemIndex, 
				updatedSelectedItemIndex);
			table.updateUI();
		}		
	}

	private void deleteSelectedHintItems() {
		int[] selectedRowIndices = table.getSelectedRows();
		if (selectedRowIndices.length == 0) {
			return;
		}
		tableModel.deleteSelectedItems(selectedRowIndices);
		table.updateUI();
		if (tableModel.isEmpty() == false) {
			//now select the first item by default, just for convience
			listSelectionModel.setSelectionInterval(0, 0);
			
		}
		updateUIWithListState();
	}
	
	public boolean isAddButton(final Object eventSource) {
		return listEditingButtonPanel.isAddButton(eventSource);
	}
	public void addActionListener(final ActionListener actionListener) {
		listEditingButtonPanel.addActionListener(actionListener);
	}
	
	public void addListSelectionListener(final ListSelectionListener listSelectionListener) {
		ListSelectionModel listSelectionModel
			= table.getSelectionModel();
		listSelectionModel.addListSelectionListener(listSelectionListener);
	}

	private void updateUIWithListState() {
		if (tableModel.isEmpty()) {
			listEditingButtonPanel.indicateEmptyState();
			shiftItemUpButton.setEnabled(false);
			shiftItemDownButton.setEnabled(false);
		}
		else {
			listEditingButtonPanel.indicateFullEditingState();
			shiftItemUpButton.setEnabled(true);
			shiftItemDownButton.setEnabled(true);		
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	public void actionPerformed(final ActionEvent event) {
		
		performActionListenerActivities(event);
	}
	
	public boolean performActionListenerActivities(final ActionEvent event) {
		Object button = event.getSource();
		
		//We will handle button presses for delete, shift up or shift down
		//because they require no knowledge of the class of object
		//(ie: DataSetFieldConfiguration, DataSetConfiguration) that the
		//list contains.  The "add" button will be handled by the client
		//(ie: ConfigurationHintTableDialog) because it needs to know which
		//kind of configuration hint class it needs to create
		
		if (listEditingButtonPanel.isDeleteButton(button)) {
			deleteSelectedHintItems();
			return true;
		}
		else if (button == shiftItemUpButton) {
			shiftItemUp();
			return true;
		}
		else if (button == shiftItemDownButton) {
			shiftItemDown();
			return true;
		}
		
		//indicate that the class had no luck making use of the action listener 
		//event.  It was generated by something else.
		return false;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


