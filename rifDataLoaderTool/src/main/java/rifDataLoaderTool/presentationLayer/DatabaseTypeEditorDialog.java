package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.rifDataTypes.AbstractRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.CustomRIFDataType;


import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;


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

public class DatabaseTypeEditorDialog 
	implements ActionListener,
	ListSelectionListener {

	
	public static void main(String[] arguments) {
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		DatabaseTypeEditorDialog dialog
			= new DatabaseTypeEditorDialog(
				userInterfaceFactory,
				rifDataTypeFactory);
		dialog.show();
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;	
	private RIFDataTypeFactory rifDataTypeFactory;
	private JDialog dialog;	
	
	private OrderedListPanel currentlySupportedDataTypesPanel;
	private ListEditingButtonPanel dataTypeListEditingPanel;
	private DataTypeEditingPanel dataTypeEditingPanel;
	
	private OKCloseButtonPanel okCloseButtonPanel;
	
	private JLabel readOnlyLabel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DatabaseTypeEditorDialog(
		final UserInterfaceFactory userInterfaceFactory,
		final RIFDataTypeFactory rifDataTypeFactory) {
		
		this.userInterfaceFactory = userInterfaceFactory;	
		this.rifDataTypeFactory = rifDataTypeFactory;
		buildUI();
	}

	private void buildUI() {

		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("databaseTypeEditorDialog.title");
		dialog = userInterfaceFactory.createDialog(dialogTitle);
		dialog.setModal(true);
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("databaseTypeEditorDialog.instructions");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		panelGC.gridy++;		
		panel.add(createDataTypeEditingPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		
		panel.add(createBottomPanel(), panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(800, 600);
		
	}

	private JPanel createDataTypeEditingPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
	
		dataTypeEditingPanel
			= new DataTypeEditingPanel(userInterfaceFactory);
		
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				createListEditingPanel(), 
				dataTypeEditingPanel.getPanel());

		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panel.add(splitPane, panelGC);
		splitPane.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
	}
	
	
	private JPanel createListEditingPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;		
		panelGC.weighty = 1;
		
		String listTitle
			= RIFDataLoaderToolMessages.getMessage("databaseTypeEditorDialog.currentlySupportedTypes.label");
		String listToolTipText = "";
		currentlySupportedDataTypesPanel
			= new OrderedListPanel(
				listTitle,
				listToolTipText,
				userInterfaceFactory,
				false);	
	
		ArrayList<RIFDataTypeInterface> registeredDataTypes
			= rifDataTypeFactory.getRegisteredDataTypes();
		for (RIFDataTypeInterface registeredDataType : registeredDataTypes) {
			currentlySupportedDataTypesPanel.addListItem(registeredDataType);			
		}
		
		currentlySupportedDataTypesPanel.addListSelectionListener(this);
		panel.add(currentlySupportedDataTypesPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weighty = 0;		
		dataTypeListEditingPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		dataTypeListEditingPanel.includeAddButton("");
		dataTypeListEditingPanel.includeCopyButton("");
		dataTypeListEditingPanel.includeEditButton("");
		dataTypeListEditingPanel.includeDeleteButton("");
		dataTypeListEditingPanel.addActionListener(this);
		panel.add(
			dataTypeListEditingPanel.getPanel(), 
			panelGC);
		
		return panel;
	}
	
	private JPanel createBottomPanel() {
		JPanel panel = userInterfaceFactory.createBorderLayoutPanel();
		
		readOnlyLabel = userInterfaceFactory.createLabel("");
		panel.add(readOnlyLabel, BorderLayout.WEST);

		okCloseButtonPanel = new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);

		panel.add(okCloseButtonPanel.getPanel(), BorderLayout.EAST);

		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void show() {
		dialog.setVisible(true);
	}
	
	private void addSelectedDataType() {
		System.out.println("add selected data type 1");
		CustomRIFDataType customRIFDataType
			= CustomRIFDataType.newInstance(
				"custom-data-type-1", 
				"custom data type 1", 
				"");
		
		
		currentlySupportedDataTypesPanel.addListItem(customRIFDataType);		
		currentlySupportedDataTypesPanel.updateUI();
		currentlySupportedDataTypesPanel.setSelectedItem(customRIFDataType);		
	}

	private void editSelectedDataType() {
		
		
	}
	
	private void copySelectedDataType() {
		
	}
	
	private void deleteSelectedDataType() {
		
	}
	
	private void updateListButtonEditingStates() {
		
		
	}
	
	private void ok() {
		
		dialog.setVisible(false);		
		System.exit(0);
	}
	
	private void close() {
		dialog.setVisible(false);
		System.exit(0);
	}
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();
		
		if (dataTypeListEditingPanel.isAddButton(button)) {
			addSelectedDataType();			
		}
		else if (dataTypeListEditingPanel.isEditButton(button)) {
			editSelectedDataType();			
		}
		else if (dataTypeListEditingPanel.isCopyButton(button)) {
			copySelectedDataType();
		}
		else if (dataTypeListEditingPanel.isDeleteButton(button)) {
			deleteSelectedDataType();
		}		
		else if (okCloseButtonPanel.isOKButton(button)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}
	}
	
	//Interface: List Selection Listener
	public void valueChanged(final ListSelectionEvent event) {
		
		AbstractRIFDataType selectedRIFDataType
			= (AbstractRIFDataType) currentlySupportedDataTypesPanel.getSelectedItem();
		if (selectedRIFDataType == null) {
			return;
		}
		dataTypeEditingPanel.setData(selectedRIFDataType);
		
		if (selectedRIFDataType instanceof CustomRIFDataType) {
			readOnlyLabel.setText("");			
		}
		else {
			String readOnlyLabelText
				= RIFDataLoaderToolMessages.getMessage("general.readOnly.label");
			readOnlyLabel.setText(readOnlyLabelText);
		}
			
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


