package rifDataLoaderTool.presentationLayer;



import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifGenericLibrary.util.ListItemNameUtility;
import rifGenericLibrary.system.RIFServiceException;
import rifDataLoaderTool.businessConceptLayer.RIFDataType;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;

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
		try {
			
			UserInterfaceFactory userInterfaceFactory
				= new UserInterfaceFactory();
			RIFDataTypeFactory rifDataTypeFactory
				= RIFDataTypeFactory.newInstance();
			rifDataTypeFactory.populateFactoryWithBuiltInTypes();
			DatabaseTypeEditorDialog dialog
				= new DatabaseTypeEditorDialog(
					userInterfaceFactory,
					rifDataTypeFactory);
			dialog.show();
		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(null, rifServiceException.getErrorMessages());
		}
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
	
	private boolean allowSelectionChange;
	
	private int previouslySelectedIndex;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DatabaseTypeEditorDialog(
		final UserInterfaceFactory userInterfaceFactory,
		final RIFDataTypeFactory rifDataTypeFactory) {
		
		this.userInterfaceFactory = userInterfaceFactory;	
		this.rifDataTypeFactory = rifDataTypeFactory;
		
		allowSelectionChange = true;
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

		//now that everything is initialised, set the initial data
		//there should be at least one data type in the list
		currentlySupportedDataTypesPanel.selectFirstItem();
		updateDisplayForSelectedDataType();
		previouslySelectedIndex 
			= currentlySupportedDataTypesPanel.getSelectedIndex();
		currentlySupportedDataTypesPanel.addListSelectionListener(this);
			
		dialog.getContentPane().add(panel);
		dialog.setSize(800, 600);
		
	}

	private JPanel createDataTypeEditingPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
	
		dataTypeEditingPanel
			= new DataTypeEditingPanel(
				userInterfaceFactory, 
				dialog);
		
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
	
		ArrayList<RIFDataType> registeredDataTypes
			= rifDataTypeFactory.getRegisteredDataTypes();
		for (RIFDataType registeredDataType : registeredDataTypes) {
			currentlySupportedDataTypesPanel.addListItem(registeredDataType);			
		}
		
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
		
		currentlySupportedDataTypesPanel.selectFirstItem();			

		
		
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
		//validate current form
		try {
			//Get them to fix any problems in the current form first
			dataTypeEditingPanel.saveChanges();

			//Now create the new data type
			RIFDataType customRIFDataType
				= RIFDataType.newInstance();	
			ArrayList<String> currentDatatypeNames
				= getRIFDataTypeNameFieldValues();
			String title
				= RIFDataLoaderToolMessages.getMessage("addDataTypeDialog.title");
			NamedListItemDialog dialog
				= new NamedListItemDialog(
					userInterfaceFactory, 
					title, 
					currentDatatypeNames);
			dialog.show();
			if (dialog.isCancelled()) {
				return;
			}
		
			customRIFDataType.setName(dialog.getCandidateName());
			System.out.println("DTED == about to add a data type 1");
		
			currentlySupportedDataTypesPanel.addListItem(customRIFDataType);		
			currentlySupportedDataTypesPanel.updateUI();		
			currentlySupportedDataTypesPanel.setSelectedItem(customRIFDataType);

			System.out.println("DTED == about to add a data type 2");
		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}

	}

	private void editSelectedDataType() {
		
		
	}
	
	private void copySelectedDataType() {
		RIFDataType originalRIFDataType
			= (RIFDataType) currentlySupportedDataTypesPanel.getSelectedItem();
		RIFDataType cloneRIFDataType
			= RIFDataType.createCopy(originalRIFDataType);
		cloneRIFDataType.setReservedDataType(false);
	
		ArrayList<String> currentDatatypeNames
			= getRIFDataTypeNameFieldValues();
		
		String baseName
			= cloneRIFDataType.getName().toLowerCase();
		//find out existing names of list items
		String candidateIdentifier
			= ListItemNameUtility.generateUniqueListItemName(
				baseName, 
				currentDatatypeNames);
		cloneRIFDataType.setIdentifier(candidateIdentifier);
		dataTypeEditingPanel.setData(cloneRIFDataType);		
	}
		
	private void deleteSelectedDataType() {
		currentlySupportedDataTypesPanel.deleteSelectedListItems();
		if (currentlySupportedDataTypesPanel.isEmpty()) {
			dataTypeListEditingPanel.indicateEmptyState();
		}
		else {
			currentlySupportedDataTypesPanel.selectFirstItem();			
		}		
	}
	
	private void ok() {
		
		dialog.setVisible(false);		
		System.exit(0);
	}
	
	private void close() {
		dialog.setVisible(false);
		System.exit(0);
	}
	
	private void updateDisplayForSelectedDataType() {
		System.out.println("updateDisplayForSelectedDataType 11");
		RIFDataType currentlySelectedDataType
			= (RIFDataType) currentlySupportedDataTypesPanel.getSelectedItem();
		if (currentlySelectedDataType == null) {
			//list is empty.  Ensure buttons indicate empty look
			dataTypeListEditingPanel.indicateEmptyState();			
			dataTypeEditingPanel.setData(RIFDataType.EMPTY_RIF_DATA_TYPE);
		}
		else {

			if (currentlySelectedDataType.isReservedDataType()){
				dataTypeListEditingPanel.indicateViewAndCopyState();			
				String warningLabelText
					= RIFDataLoaderToolMessages.getMessage("databaseTypeEditorDialog.warning.dataTypeIsReserved");
				readOnlyLabel.setText(warningLabelText);
				readOnlyLabel.setForeground(Color.RED);				
				dataTypeListEditingPanel.indicateViewAndCopyState();				
			}
			else {
				dataTypeListEditingPanel.indicateFullEditingState();
				readOnlyLabel.setForeground(Color.BLACK);
				readOnlyLabel.setText("");				
			}
			dataTypeEditingPanel.setData(currentlySelectedDataType);
		}
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
		
		System.out.println("valueChanged 1");
		if (event.getValueIsAdjusting()) {
			return;			
		}		
		
		if (allowSelectionChange == true) {

			try {
				RIFDataType unalteredRIFDataType
					= dataTypeEditingPanel.getDataType();
				String oldDisplayName = unalteredRIFDataType.getDisplayName();
				boolean changesWereSaved = dataTypeEditingPanel.saveChanges();
				if (changesWereSaved) {
					//changes in the item may have affected the display name
					RIFDataType changedRIFDataType
						= dataTypeEditingPanel.getDataType();
					//update item in the list
					currentlySupportedDataTypesPanel.replaceItem(
						oldDisplayName, 
						changedRIFDataType);
				}
			}
			catch(RIFServiceException rifServiceException) {
				ErrorDialog.showError(
					dialog, 
					rifServiceException.getErrorMessages());
				allowSelectionChange = false;
			}
		}

		if (allowSelectionChange == true) {
			updateDisplayForSelectedDataType();
			previouslySelectedIndex = event.getFirstIndex();			
		}
		else {

			//remove listeners to prevent more triggers
			currentlySupportedDataTypesPanel.removeListSelectionListener(this);
			currentlySupportedDataTypesPanel.setSelectedItem(previouslySelectedIndex);		
			currentlySupportedDataTypesPanel.addListSelectionListener(this);
		}
		
	}
	
	private ArrayList<String> getRIFDataTypeNameFieldValues() {
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<DisplayableListItemInterface> currentListItems
			= currentlySupportedDataTypesPanel.getAllItems();
		for (DisplayableListItemInterface currentListItem : currentListItems) {
			RIFDataType currentDataType
				= (RIFDataType) currentListItem;
			results.add(currentDataType.getIdentifier());
		}
		
		return results;
		
	}
	// ==========================================
	// Section Override
	// ==========================================

}


