package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.businessConceptLayer.DataLoaderToolGeography;
import rifDataLoaderTool.system.DataLoaderToolSession;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.OKCloseButtonDialog;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.ArrayList;

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

class GeographyListEditingDialog 
	extends OKCloseButtonDialog {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
		
	private DataLoaderToolSession session;
	
	private OrderedListPanel listPanel;
	private ListEditingButtonPanel listEditingButtonPanel;
	private JButton generateScriptsButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographyListEditingDialog(
		final DataLoaderToolSession session) {
		
		super(session.getUserInterfaceFactory());
			
		this.session = session;
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("geographyListEditingDialog.title");
		setDialogTitle(dialogTitle);
		String dialogInstructionsText
			= RIFDataLoaderToolMessages.getMessage("geographyListEditingDialog.instructions");
		setInstructionText(dialogInstructionsText);
		setMainPanel(createMainPanel());
		buildUI();
		setSize(500, 500);
	}

	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();	
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;		
		listPanel
			= new OrderedListPanel(
				"",
				"",
				userInterfaceFactory,
				false);
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(listPanel.getPanel());
		panel.add(scrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;		
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		
		String generateScriptsButtonText
			= RIFDataLoaderToolMessages.getMessage("geographyListEditingDialog.buttons.generateGeographyScripts");
		generateScriptsButton
			= userInterfaceFactory.createButton(generateScriptsButtonText);		
		listEditingButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		listEditingButtonPanel.includeAddButton("");
		listEditingButtonPanel.includeEditButton("");
		listEditingButtonPanel.includeDeleteButton("");		
		listEditingButtonPanel.addSpecialisedButton(generateScriptsButton);
		listEditingButtonPanel.addActionListener(this);
		panel.add(listEditingButtonPanel.getPanel(), panelGC);
				
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public ArrayList<DataLoaderToolGeography> getData() {
		ArrayList<DataLoaderToolGeography> results
			= new ArrayList<DataLoaderToolGeography>();
		
		ArrayList<DisplayableListItemInterface> listItems
			= listPanel.getAllItems();
		for (DisplayableListItemInterface listItem : listItems) {
			results.add((DataLoaderToolGeography) listItem);
		}
		
		return results;
	}
	
	public void setData(final ArrayList<DataLoaderToolGeography> dataLoaderGeographies) {
		for (DataLoaderToolGeography dataLoaderGeography : dataLoaderGeographies) {
			listPanel.addListItem(dataLoaderGeography);
		}
		listPanel.updateUI();
		listPanel.selectFirstItem();
	}
	
	
	private void addGeography() {
		DataLoaderToolGeography geography
			= DataLoaderToolGeography.newInstance();
		GeographyEditorDialog geographyEditorDialog
			= new GeographyEditorDialog(session);
		geographyEditorDialog.setData(geography);
		geographyEditorDialog.show();

		if (geographyEditorDialog.isCancelled()) {
			return;
		}
		
		geographyEditorDialog.saveChanges();
		session.setSaveChanges(true);
		listPanel.addListItem(geography);		
		listPanel.updateUI();	
		listPanel.setSelectedItem(geography);

		updateListEditingButtonStates();

	}

	private void editGeography() {
		DataLoaderToolGeography selectedGeography
			= (DataLoaderToolGeography) listPanel.getSelectedItem();
		String oldDisplayName = selectedGeography.getDisplayName();
		GeographyEditorDialog geographyEditorDialog
			= new GeographyEditorDialog(session);
		geographyEditorDialog.setData(selectedGeography);
		geographyEditorDialog.show();		
		if (geographyEditorDialog.isCancelled()) {
			return;
		}
		
		boolean saveChanges
			= geographyEditorDialog.saveChanges();
		if (saveChanges) {
			session.setSaveChanges(true);
		}
		
		String newDisplayName
			= selectedGeography.getDisplayName();
		if (oldDisplayName.equals(newDisplayName) == false) {
			//changes were made.  Some may potentially alter the name,
			//which could affect the item's place in the list
			listPanel.replaceItem(
				oldDisplayName, 
				selectedGeography);
			listPanel.updateUI();		
		}	
		updateListEditingButtonStates();
	}
	
	private void deleteGeography() {
		if (listPanel.noItemsSelected() == false) {
			session.setSaveChanges(true);
		}
		
		listPanel.deleteSelectedListItems();
		if (listPanel.isEmpty() == false) {
			listPanel.selectFirstItem();
		}
		updateListEditingButtonStates();
	}
	
	private void updateListEditingButtonStates() {
		if (listPanel.isEmpty()) {
			listEditingButtonPanel.indicateEmptyState();
		}
		else {
			listEditingButtonPanel.indicateFullEditingState();
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

	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();
		
		if (listEditingButtonPanel.isAddButton(button)) {
			addGeography();
		}
		else if (listEditingButtonPanel.isEditButton(button)) {
			editGeography();
		}
		else if (listEditingButtonPanel.isDeleteButton(button)) {
			deleteGeography();
		}
		else {
			performOKCloseActions(event);
		}
		
	}	
}


