package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.system.RIFServiceException;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

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

public class GeographicalResolutionEditorDialog 
	implements ActionListener, ListSelectionListener {

	
	public static void main(String[] arguments) {
		RIFDataLoaderToolSettings settings
			= new RIFDataLoaderToolSettings();
		
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();
		GeographicalResolutionEditorDialog dialog
			= new GeographicalResolutionEditorDialog(userInterfaceFactory);
		dialog.setData(settings.getGeographicalResolutionLevels());
		dialog.show();
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;

	private JDialog dialog;
	

	
	private OrderedListPanel listPanel;
	private ListEditingButtonPanel listEditingButtonPanel;
	
	private GeographicalResolutionLevelEditingPanel resolutionEditingPanel;
	
	
	private OKCloseButtonPanel okCloseButtonPanel;
	
	private boolean allowSelectionChange;
	private int previouslySelectedIndex;
	
	private GeographicalResolutionLevel currentLevel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographicalResolutionEditorDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		allowSelectionChange = true;
		currentLevel = GeographicalResolutionLevel.EMPTY_GEOGRAPHICAL_RESOLUTION_LEVEL;
		buildUI();
	}
	
	private void buildUI() {
		
		String title
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionEditorDialog.title");
		dialog
			= userInterfaceFactory.createDialog(title);
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionEditorDialog.instructions");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		resolutionEditingPanel
			= new GeographicalResolutionLevelEditingPanel(userInterfaceFactory);
		
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
					createListEditingPanel(), 
					resolutionEditingPanel.getPanel());
		splitPane.setDividerLocation(0.20);
		panel.add(splitPane, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		okCloseButtonPanel = new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), panelGC);

		dialog.getContentPane().add(panel);
		dialog.setSize(600, 300);
		dialog.setModal(true);
	}

	private JPanel createListEditingPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		String geographicalResolutionLevelListTitle
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionLevel.plural.label");
		listPanel 
			= new OrderedListPanel(
					geographicalResolutionLevelListTitle,
					"",
					userInterfaceFactory,
					false);
		listPanel.setAlphabeticallySortItems(false);
		//listPanel.addListSelectionListener(this);
		panel.add(listPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		listEditingButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		listEditingButtonPanel.addActionListener(this);
		listEditingButtonPanel.includeAddButton("");
		listEditingButtonPanel.includeCopyButton("");
		listEditingButtonPanel.includeDeleteButton("");
		panel.add(listEditingButtonPanel.getPanel(), panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public ArrayList<GeographicalResolutionLevel> getGeographicalResolutionLevels() {
		ArrayList<GeographicalResolutionLevel> results
			= new ArrayList<GeographicalResolutionLevel>();
		ArrayList<DisplayableListItemInterface> listItems
			= listPanel.getAllItems();
		
		for (DisplayableListItemInterface listItem : listItems) {
			results.add((GeographicalResolutionLevel) listItem);
		}
		
		return results;
	}
	
	public void setData(final ArrayList<GeographicalResolutionLevel> levels) {
		listPanel.removeListSelectionListener(this);
		listPanel.clearList();		
		for (GeographicalResolutionLevel level : levels) {
			listPanel.addListItem(level);
		}
		listPanel.updateUI();
		updateListButtonStates();

		listPanel.selectFirstItem();
		GeographicalResolutionLevel firstItem
			= (GeographicalResolutionLevel) listPanel.getSelectedItem();
		if (firstItem != null) {
			resolutionEditingPanel.setData(firstItem);			
		}
		listPanel.addListSelectionListener(this);	

	}
	
	public void show() {
		dialog.setVisible(true);
	}
	
	public void addGeographicalResolutionLevel() {
		
		try {
			saveFormChanges();
			listPanel.addListItem(currentLevel);
			listPanel.setSelectedItem(currentLevel);			
			updateListButtonStates();			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(dialog, rifServiceException.getErrorMessages());
		}
	}

	public void copyGeographicalResolutionLevel() {
		
		GeographicalResolutionLevel selectedLevel
			= (GeographicalResolutionLevel) listPanel.getSelectedItem();
		GeographicalResolutionLevel cloneLevel
			= GeographicalResolutionLevel.createCopy(selectedLevel);
		
		String nameListItemDialogTitle
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionEditorDialog.copyLevel.title");
		
		ArrayList<String> existingNames = getExistingNames();
		
		NamedListItemDialog nameListItemDialog
			= new NamedListItemDialog(
				userInterfaceFactory,
				nameListItemDialogTitle,
				existingNames);
			
		nameListItemDialog.show();
		if (nameListItemDialog.isCancelled()) {
			return;
		}
		
		cloneLevel.setName(nameListItemDialog.getCandidateName());
		listPanel.addListItem(cloneLevel);
		listPanel.updateUI();
		listPanel.setSelectedItem(cloneLevel);
		
	}

	public void deleteGeographicalResolutionLevel() {
		listPanel.deleteSelectedListItems();
		if (listPanel.isEmpty()) {
			currentLevel = GeographicalResolutionLevel.EMPTY_GEOGRAPHICAL_RESOLUTION_LEVEL;
		}
		else {
			listPanel.selectFirstItem();
		}
		updateListButtonStates();
		
	}
	
	private ArrayList<String> getExistingNames() {
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<DisplayableListItemInterface> listItems
			= listPanel.getAllItems();
		for (DisplayableListItemInterface listItem : listItems) {
			GeographicalResolutionLevel level
				= (GeographicalResolutionLevel) listItem;
			results.add(level.getName());
		}
					
		return results;
	}
	
	private void ok() {

		dialog.setVisible(false);
	}
	
	private void close() {		
		dialog.setVisible(false);
	}
	
	
	private void updateListButtonStates() {
		if (listPanel.isEmpty()) {
			listEditingButtonPanel.indicateEmptyState();
		}
		else {
			listEditingButtonPanel.indicatePopulatedState();

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
		
		if (listEditingButtonPanel.isAddButton(button)) {
			addGeographicalResolutionLevel();
		}
		else if (listEditingButtonPanel.isCopyButton(button)) {
			copyGeographicalResolutionLevel();
		}
		else if (listEditingButtonPanel.isDeleteButton(button)) {
			deleteGeographicalResolutionLevel();
		}
		else if (okCloseButtonPanel.isOKButton(button)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}
	}
	
	public void valueChanged(final ListSelectionEvent event) {
		System.out.println("valueChanged there are=="+ listPanel.getNumberOfItems() + "== items");
		
		if (event.getValueIsAdjusting()) {
			return;
		}

		//Try to save the previously selected item
		try {
			System.out.println("valueChanged 0.1");
			if (currentLevel != GeographicalResolutionLevel.EMPTY_GEOGRAPHICAL_RESOLUTION_LEVEL) {		
				System.out.println("valueChanged 1");
				String oldDisplayName = currentLevel.getDisplayName();
				boolean changesWereSaved = saveFormChanges();
				if (changesWereSaved) {
					System.out.println("valueChanged 2");
					currentLevel.printFields();
					System.out.println("valueChanged 2111");
					//update item in the list
					listPanel.removeListSelectionListener(this);
					listPanel.replaceItem(
						oldDisplayName, 
						currentLevel);
					listPanel.updateUI();
					listPanel.addListSelectionListener(this);
				}
			}			

			//We successfully saved what we had to.  Now, let's move on to accepting the newly
			//selected item
			currentLevel
				= (GeographicalResolutionLevel) listPanel.getSelectedItem();
			resolutionEditingPanel.setData(currentLevel);
			previouslySelectedIndex = event.getFirstIndex();

			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		
			//We tried to save changes and it didn't work.  Revert back to previous selection
			//but we don't want to trigger yet another event
			listPanel.removeListSelectionListener(this);
			if (previouslySelectedIndex != -1) {
				listPanel.setSelectedItem(previouslySelectedIndex);		
			}
			listPanel.addListSelectionListener(this);				
		}

	}
	
	private boolean saveFormChanges() 
		throws RIFServiceException {
		
		GeographicalResolutionLevel candidateLevel = resolutionEditingPanel.getLevelFromForm();

		if (listPanel.isEmpty()) {
			return false;
		}
		else {
			candidateLevel.checkErrors();
			boolean saveChangesNeeded
				= !candidateLevel.hasIdenticalContents(currentLevel);
			System.out.println("GeographicalResolutionEditorDialog saveFormChanges 1 saveChanges=="+saveChangesNeeded+"==");
		
			//commit changes from form to the underlying current level
			currentLevel.setName(candidateLevel.getName());
			currentLevel.setDescription(candidateLevel.getDescription());
		
			currentLevel.printFields();
			
			return saveChangesNeeded;
		}

	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


