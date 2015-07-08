package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.CleaningRule;


import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class CleaningRuleEditingPanel 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private RIFDataLoaderToolSession session;
	
	private JPanel panel;
	
	private JTable table;
	private CleaningRuleTableModel cleaningRuleTableModel;

	private ListEditingButtonPanel listEditingButtonPanel;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleaningRuleEditingPanel(
		final RIFDataLoaderToolSession session) {
		
		this.session = session;
		
		UserInterfaceFactory userInterfaceFactory
			 = session.getUserInterfaceFactory();
		panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();

		
		String cleaningRuleLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.dataCleaningRules.label");
		JLabel cleaningRuleLabel
			= userInterfaceFactory.createLabel(cleaningRuleLabelText);
		panel.add(cleaningRuleLabel, panelGC);
		
		panelGC.gridy++;		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		cleaningRuleTableModel
			= new CleaningRuleTableModel();
		table
			= userInterfaceFactory.createTable(cleaningRuleTableModel);
		ListSelectionModel listSelectionModel = table.getSelectionModel();
		listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(table);
		panel.add(scrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		listEditingButtonPanel = new ListEditingButtonPanel(userInterfaceFactory);
		listEditingButtonPanel.includeAddButton("");
		listEditingButtonPanel.includeEditButton("");
		listEditingButtonPanel.includeDeleteButton("");
		listEditingButtonPanel.addActionListener(this);
		panel.add(listEditingButtonPanel.getPanel(), panelGC);

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setData(final RIFDataTypeInterface rifDataType) {		
		cleaningRuleTableModel.setData(rifDataType);
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	private void addCleaningRule() {
		CleaningRule cleaningRule 
			= CleaningRule.newInstance();

		CleaningRuleEditorDialog dialog
			= new CleaningRuleEditorDialog(session);
		dialog.setData(cleaningRule);
		dialog.show();
	}
	
	private void editSelectedCleaningRule() {
		CleaningRule cleaningRule
			= getSelectedCleaningRule();

		CleaningRuleEditorDialog dialog
			= new CleaningRuleEditorDialog(session);
		dialog.setData(cleaningRule);
		dialog.show();
	}
	
	private void deleteSelectedCleaningRule() {
		CleaningRule selectedCleaningRule
			= getSelectedCleaningRule();
		
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			return;
		}
		
		cleaningRuleTableModel.deleteRow(selectedRow);
		cleaningRuleTableModel.fireTableRowsDeleted(selectedRow, selectedRow);		
	}

	public CleaningRule getSelectedCleaningRule() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			return null;
		}
		
		return cleaningRuleTableModel.getRow(selectedRow);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
		
		if (listEditingButtonPanel.isAddButton(button)) {
			addCleaningRule();
		}
		else if (listEditingButtonPanel.isEditButton(button)) {
			editSelectedCleaningRule();
		}
		else if (listEditingButtonPanel.isDeleteButton(button)) {
			deleteSelectedCleaningRule();
		}
		else {
			assert(false);
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


