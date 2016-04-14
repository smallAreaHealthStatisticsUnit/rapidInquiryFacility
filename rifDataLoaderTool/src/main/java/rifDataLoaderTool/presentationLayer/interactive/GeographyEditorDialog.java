package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.businessConceptLayer.ShapeFile;
import rifDataLoaderTool.system.DataLoaderToolSession;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.DataLoaderToolGeography;
import rifDataLoaderTool.fileFormats.ShapeFileMetaDataExtractor;

import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OKCloseButtonDialog;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.awt.event.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;

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

class GeographyEditorDialog 
	extends OKCloseButtonDialog 
	implements ListSelectionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	private DataLoaderToolSession session;
	private DataLoaderToolGeography originalGeography;
	private ShapeFile currentShapeFile;
	
	//GUI Components
	private JTextField nameTextField;
	private OrderedListPanel shapeFileListPanel;
	private ListEditingButtonPanel shapeFileListEditingButtonPanel;
	private ShapeFileEditorPanel shapeFileEditorPanel;	
	private int previouslySelectedIndex;
	private boolean isRollbackSelectionHappening;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographyEditorDialog(
		final DataLoaderToolSession session) {
		
		super(session.getUserInterfaceFactory());
		
		this.session = session;
		
		previouslySelectedIndex = -1;
		isRollbackSelectionHappening = false;
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("geographyEditorDialog.title");
		setDialogTitle(dialogTitle);
		
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("geographyEditorDialog.instructions");
		setInstructionText(instructionsText);
		
		setMainPanel(createMainPanel());
		setSize(1000, 700);		
		buildUI();
	}

	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			 = userInterfaceFactory.createGridBagConstraints();

		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		panel.add(
			createNameFieldPanel(), 
			panelGC);
		
		panelGC.gridy++;		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		shapeFileEditorPanel
			= new ShapeFileEditorPanel(session);		
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				createListPanel(), 
				shapeFileEditorPanel.getPanel());
		splitPane.setDividerLocation(0.4);
		splitPane.setBorder(LineBorder.createGrayLineBorder());
		panel.add(splitPane, panelGC);
		
		return panel;
	}
	
	private JPanel createNameFieldPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String nameFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataLoaderToolGeography.name.label");
		JLabel nameFieldLabel
			= userInterfaceFactory.createLabel(nameFieldLabelText);
		panel.add(nameFieldLabel, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		panelGC.weighty = 0;

		nameTextField = userInterfaceFactory.createTextField();
		panel.add(nameTextField, panelGC);
		
		return panel;
	}
	
	private JPanel createListPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		String shapeFileListPanelTitle
			= RIFDataLoaderToolMessages.getMessage("shapeFile.name.plural.label");
		shapeFileListPanel
			= new OrderedListPanel(
				shapeFileListPanelTitle,
				"",
				userInterfaceFactory,
				false);
		shapeFileListPanel.addListSelectionListener(this);
		panel.add(
			shapeFileListPanel.getPanel(), 
			panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		
		shapeFileListEditingButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		shapeFileListEditingButtonPanel.includeAddButton("");
		shapeFileListEditingButtonPanel.includeDeleteButton("");
		shapeFileListEditingButtonPanel.addActionListener(this);
		panel.add(shapeFileListEditingButtonPanel.getPanel(), panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setData(final DataLoaderToolGeography originalGeography) {
		this.originalGeography = originalGeography;
		
		ArrayList<ShapeFile> shapeFiles
			= originalGeography.getShapeFiles();
		for (ShapeFile shapeFile : shapeFiles) {
			shapeFile.print();
		}			
		
		populateForm(originalGeography);	
	}
	
	private void populateForm(final DataLoaderToolGeography geography) {
		
		nameTextField.setText(geography.getName());
		
		shapeFileListPanel.removeListSelectionListener(this);
		
		ArrayList<ShapeFile> shapeFiles
			= geography.getShapeFiles();		
		shapeFileListPanel.clearList();
		for (ShapeFile shapeFile : shapeFiles) {
			shapeFileListPanel.addListItem(shapeFile);
		}
		
		shapeFileListPanel.addListSelectionListener(this);
		previouslySelectedIndex = -1;
		
		shapeFileListPanel.updateUI();			
		if (shapeFileListPanel.isEmpty() == false) {
			shapeFileListPanel.selectFirstItem();
		}
	}
	
	public DataLoaderToolGeography getGeographyFromForm() {
		DataLoaderToolGeography geographyFromForm
			= DataLoaderToolGeography.newInstance();
		geographyFromForm.setIdentifier(originalGeography.getIdentifier());		
		geographyFromForm.setName(nameTextField.getText().trim());
		

		ArrayList<DisplayableListItemInterface> listItems
			= shapeFileListPanel.getAllItems();
		for (DisplayableListItemInterface listItem : listItems) {
			ShapeFile currentShapeFile
				= (ShapeFile) listItem;
			geographyFromForm.addShapeFile(currentShapeFile);
		}
		
		return geographyFromForm;
	}
	
	
	private void addShapeFile() {

		try {
			
			UserInterfaceFactory userInterfaceFactory
				= getUserInterfaceFactory();
			ShapeFileSelectionDialog shapeFileSelectionDialog
				= new ShapeFileSelectionDialog(userInterfaceFactory);
			shapeFileSelectionDialog.show();
		
			if (shapeFileSelectionDialog.isCancelled()) {
				return;
			}

			shapeFileListPanel.removeListSelectionListener(this);
			ArrayList<ShapeFile> selectedShapeFiles
				= shapeFileSelectionDialog.getSelectedShapeFiles();
			for (ShapeFile selectedShapeFile : selectedShapeFiles) {
				ShapeFileMetaDataExtractor extractor
					= ShapeFileMetaDataExtractor.newInstance();
				extractor.extractMetaDataAndSampleDBFRows(selectedShapeFile);
				selectedShapeFile.setTotalAreaIdentifiers(
					extractor.getTotalAreaIdentifiers());
				ArrayList<String> shapeFileFieldNames
					= extractor.getShapeFileFieldNames();
				selectedShapeFile.setShapeFileFieldNames(shapeFileFieldNames);
				String projectionName
					= extractor.getProjectionName();
				selectedShapeFile.setProjection(projectionName);
				shapeFileListPanel.addListItem(selectedShapeFile);
			}

			shapeFileListPanel.updateUI();
			shapeFileListPanel.addListSelectionListener(this);
			shapeFileListPanel.selectFirstItem();
		
			updateShapeFileListButtons();
		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(getDialog(), rifServiceException.getErrorMessages());
		}		
	}
	
	private void deleteSelectedShapeFiles() {
		
		ArrayList<DisplayableListItemInterface> itemsToDelete
			= shapeFileListPanel.getSelectedItems();
		ShapeFile currentlyEditingShapeFile
			= shapeFileEditorPanel.getData();
		
		shapeFileListPanel.removeListSelectionListener(this);
		if (itemsToDelete.contains(currentlyEditingShapeFile)) {
			shapeFileEditorPanel.setData(null);
		}
	
		shapeFileListPanel.addListSelectionListener(this);

		shapeFileListPanel.deleteSelectedListItems();
		if (shapeFileListPanel.isEmpty() == false) {
			shapeFileListPanel.selectFirstItem();
		}
		
		updateShapeFileListButtons();
	}
	
	private void updateShapeFileListButtons() {
		if (shapeFileListPanel.isEmpty()) {
			shapeFileListEditingButtonPanel.indicateEmptyState();
		}
		else {
			shapeFileListEditingButtonPanel.indicatePopulatedState();			
		}	
	}


	public boolean saveChanges() {
		DataLoaderToolGeography geographyFromForm
			= getGeographyFromForm();

		boolean changesMade = shapeFileEditorPanel.saveChanges();
		
		//if they are identical, no changes made and if they aren't identical
		//changes were made
		if (changesMade == false) {
			changesMade
				= !DataLoaderToolGeography.hasIdenticalContents(
					originalGeography, 
					geographyFromForm);
		}
		DataLoaderToolGeography.copyInto(
			geographyFromForm, 
			originalGeography);
		
		return changesMade;
	}
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	public void valueChanged(final ListSelectionEvent event) {
		
		if (event.getValueIsAdjusting()) {			
			return;
		}
		
		if (isRollbackSelectionHappening) {
			isRollbackSelectionHappening = false;
			return;
		}
		
		try {
			currentShapeFile 
				= (ShapeFile) shapeFileListPanel.getSelectedItem();
			if (previouslySelectedIndex != -1) {
				shapeFileEditorPanel.validateForm();
				shapeFileEditorPanel.saveChanges();
			}
			shapeFileEditorPanel.setData(currentShapeFile);	
			previouslySelectedIndex = event.getLastIndex();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
			isRollbackSelectionHappening = true;
			shapeFileListPanel.setSelectedItem(previouslySelectedIndex);			
		}		
		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

	@Override
	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();
		
		if (shapeFileListEditingButtonPanel.isAddButton(button)) {
			addShapeFile();
		}
		else if (shapeFileListEditingButtonPanel.isDeleteButton(button)) {
			deleteSelectedShapeFiles();
		}
		
		
		performOKCloseActions(event);
	}
	
	@Override
	public void okAction() 
		throws RIFServiceException {
			ShapeFile currentShapeFile
				= shapeFileEditorPanel.getShapeFileFromForm();
			currentShapeFile.checkErrors();
		
			DataLoaderToolGeography geographyFromForm
				= getGeographyFromForm();
			geographyFromForm.checkErrors();			
	}
	
}


