package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.ListEditingButtonPanel;
import org.sahsu.rif.generic.presentation.NamedListItemDialog;
import org.sahsu.rif.generic.presentation.OKCloseButtonDialog;
import org.sahsu.rif.generic.presentation.OrderedListPanel;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;


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

class DataTypeEditorDialog 
	extends OKCloseButtonDialog
	implements ListSelectionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderToolSession session;
		
	private OrderedListPanel currentlySupportedDataTypesPanel;
	private ListEditingButtonPanel dataTypeListEditingPanel;
	private DataTypeEditingPanel dataTypeEditingPanel;
	private JLabel readOnlyLabel;
	
	private boolean allowSelectionChange;
	
	private int previouslySelectedIndex;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataTypeEditorDialog(
		final DataLoaderToolSession session) {
		
		super(session.getUserInterfaceFactory());
		this.session = session;
		
		allowSelectionChange = true;
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("dataTypeEditorDialog.title");
		setDialogTitle(dialogTitle);
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("dataTypeEditorDialog.instructions");
		setInstructionText(instructionsText);
		setMainPanel(createMainPanel());
		setSize(800, 600);
		buildUI();

	}

	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
	
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
				
		return panel;
	}

	private JPanel createDataTypeEditingPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
	
		dataTypeEditingPanel
			= new DataTypeEditingPanel(
				session, 
				getDialog());
		
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
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;		
		panelGC.weighty = 1;
		
		String listTitle
			= RIFDataLoaderToolMessages.getMessage("dataTypeEditorDialog.currentlySupportedTypes.label");
		String listToolTipText = "";
		currentlySupportedDataTypesPanel
			= new OrderedListPanel(
				listTitle,
				listToolTipText,
				userInterfaceFactory,
				false);	
	
		RIFDataTypeFactory rifDataTypeFactory
			= session.getRIFDataTypeFactory();
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
		dataTypeListEditingPanel.includeDeleteButton("");
		
		dataTypeListEditingPanel.addActionListener(this);
		
		currentlySupportedDataTypesPanel.selectFirstItem();			
		
		panel.add(
			dataTypeListEditingPanel.getPanel(), 
			panelGC);
		
		return panel;
	}
	
	private JPanel createBottomPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel 
			= userInterfaceFactory.createBorderLayoutPanel();
		
		readOnlyLabel = userInterfaceFactory.createLabel("");
		panel.add(readOnlyLabel, BorderLayout.WEST);

		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	private ArrayList<RIFDataType> getCurrentRIFDataTypes() {
		ArrayList<RIFDataType> results
			= new ArrayList<RIFDataType>();
		
		ArrayList<DisplayableListItemInterface> listItems
			= currentlySupportedDataTypesPanel.getAllItems();
		for (DisplayableListItemInterface listItem : listItems) {
			results.add((RIFDataType) listItem);
		}
		
		return results;
		
	}
	
	public boolean saveChanges() {
		RIFDataTypeFactory rifDataTypeFactory
			= session.getRIFDataTypeFactory();
		
		
		ArrayList<RIFDataType> originalRIFDataTypes
			= rifDataTypeFactory.getRegisteredDataTypes();
		
		ArrayList<RIFDataType> currentRIFDataTypes
			= getCurrentRIFDataTypes();
	
		//If the two lists differ then we need to save changes
		boolean saveChanges
			= !RIFDataType.hasIdenticalContents(
				originalRIFDataTypes, 
				currentRIFDataTypes);
		if (saveChanges) {
			rifDataTypeFactory.setDataTypes(currentRIFDataTypes);
		}
		
		return saveChanges;
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
					getUserInterfaceFactory(), 
					title, 
					currentDatatypeNames);
			dialog.show();
			if (dialog.isCancelled()) {
				return;
			}

			String candidateName 
				= dialog.getCandidateName();
			String rifDataTypeIdentifier
				= convertCandidateNameToRIFIdentifier(candidateName);
			customRIFDataType.setIdentifier(rifDataTypeIdentifier);
			customRIFDataType.setName(dialog.getCandidateName());
			currentlySupportedDataTypesPanel.addListItem(customRIFDataType);		
			session.setSaveChanges(true);

			currentlySupportedDataTypesPanel.updateUI();		
			currentlySupportedDataTypesPanel.setSelectedItem(customRIFDataType);		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
		}

	}

	private String convertCandidateNameToRIFIdentifier(final String candidateName) {
		String result
			= candidateName.toLowerCase();
		result
			= result.replaceAll(" ", "_");
		return result;
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
			= FieldValidationUtility.generateUniqueListItemName(
				baseName, 
				currentDatatypeNames);
		candidateIdentifier
			= convertCandidateNameToRIFIdentifier(candidateIdentifier);
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
		
	private void updateDisplayForSelectedDataType() {
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
					= RIFDataLoaderToolMessages.getMessage("dataTypeEditorDialog.warning.dataTypeIsReserved");
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
		else if (dataTypeListEditingPanel.isCopyButton(button)) {
			copySelectedDataType();
		}
		else if (dataTypeListEditingPanel.isDeleteButton(button)) {
			deleteSelectedDataType();
		}	
		else {
			this.performOKCloseActions(event);
		}
	}
	
	//Interface: List Selection Listener
	public void valueChanged(final ListSelectionEvent event) {
		
		if (event.getValueIsAdjusting()) {
			return;			
		}		
		
		if (allowSelectionChange == true) {

			try {
				dataTypeEditingPanel.validateForm();
				RIFDataType unalteredRIFDataType
					= dataTypeEditingPanel.getDataType();
				String oldDisplayName = unalteredRIFDataType.getDisplayName();
				boolean changesWereSaved = dataTypeEditingPanel.saveChanges();
				if (changesWereSaved) {
					//changes in the item may have affected the display name
					String newDisplayName
						= unalteredRIFDataType.getDisplayName();
					if (oldDisplayName.equals(newDisplayName) == false) {
						RIFDataType changedRIFDataType
							= dataTypeEditingPanel.getDataType();
						//update item in the list
						currentlySupportedDataTypesPanel.replaceItem(
							oldDisplayName, 
							changedRIFDataType);					

					}
				}
			}
			catch(RIFServiceException rifServiceException) {
				ErrorDialog.showError(
					getDialog(), 
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

	@Override
	public void okAction() 
		throws RIFServiceException {
		
		dataTypeEditingPanel.validateForm();
		dataTypeEditingPanel.saveChanges();
		
	}
}


