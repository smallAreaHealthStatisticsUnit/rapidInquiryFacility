package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.system.DataLoaderToolSession;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.ShapeFile;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFServiceException;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

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

class ShapeFileEditorPanel 
	implements ActionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	private DataLoaderToolSession session;
	private ShapeFile originalShapeFile;
	private ShapeFile currentShapeFile;

	//GUI Components	
	private JList<String> shapeFileComponentList;	
	private JTextField projectionTextField;
	
	private ShapeFilePropertyPreviewTablePanel shapeFilePropertyPreviewTablePanel;
	private DefaultListModel<String> shapeFileComponentListModel;
	private JPanel panel;
	private JLabel currentShapeFileTitleLabel;
	private JButton getShapeFileInfoButton;
	private String pleaseChooseMessage;
	private JLabel totalAreaIdentifiersLabel;
	private JTextField shapeFileDescriptionTextField;
	/**
	 * This is the primary key field name for an area eg: ladua
	 */
	private JComboBox<String> areaIdentifierFieldComboBox;
	/**
	 * This is the natural key field name for an area eg: district
	 */
	private JComboBox<String> nameFieldComboBox;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFileEditorPanel(
		final DataLoaderToolSession session) {
		
		this.session = session;
		buildUI();
	}
	
	private void buildUI() {
		
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();
		
		shapeFileComponentListModel = new DefaultListModel<String>();
		
		pleaseChooseMessage
			= RIFDataLoaderToolMessages.getMessage("shapeFileEditorPanel.pleaseChoose");
		
		panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			 = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createBannerPanel(), panelGC);
		
		panelGC.gridy++;
		panel.add(createDescriptionPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 0.30;
		panel.add(createShapeFileComponentPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createProjectionNameFieldPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 0.70;
		shapeFilePropertyPreviewTablePanel
			= new ShapeFilePropertyPreviewTablePanel(session);
		panel.add(
			shapeFilePropertyPreviewTablePanel.getPanel(), 
			panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createKeyFieldsPanel(), panelGC);		
		setData(null);
	}
	
	private JPanel createBannerPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		String emptyShapeFileTitleLabelText
			= RIFDataLoaderToolMessages.getMessage("shapeFileEditorPanel.emptyTitle");
		currentShapeFileTitleLabel
			= userInterfaceFactory.createLabel(emptyShapeFileTitleLabelText);
		panel.add(currentShapeFileTitleLabel, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.NORTHEAST;
		String unknownTotalAreaIdentifiersText
			= RIFDataLoaderToolMessages.getMessage("shapeFile.totalAreaIdentifiers.unknown.label");
		totalAreaIdentifiersLabel
			= userInterfaceFactory.createLabel(unknownTotalAreaIdentifiersText);
		panel.add(totalAreaIdentifiersLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		String getShapeFileInfoButtonText
			= RIFDataLoaderToolMessages.getMessage("shapeFileEditorPanel.buttons.getInfo");
		getShapeFileInfoButton
			= userInterfaceFactory.createButton(getShapeFileInfoButtonText);
		getShapeFileInfoButton.addActionListener(this);
		panel.add(getShapeFileInfoButton, panelGC);
		
		return panel;
	}
	
	/**
	 * This method creates the panel that shows all of the file components that are 
	 * part of the shape file
	 * @return
	 */
	private JPanel createShapeFileComponentPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String shapeFileComponentListTitleText
			= RIFDataLoaderToolMessages.getMessage("shapeFileEditorPanel.shapeFileComponents.label");
		JLabel shapeFileComponentListTitleLabel
			= userInterfaceFactory.createLabel(shapeFileComponentListTitleText);
		panel.add(
			shapeFileComponentListTitleLabel, 
			panelGC);		

		panelGC.gridy++;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("shapeFileEditorPanel.shapeFileComponents.instructions");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		shapeFileComponentList
			= userInterfaceFactory.createList(shapeFileComponentListModel);
		
		shapeFileComponentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(shapeFileComponentList);
		panel.add(scrollPane, panelGC);
		
		return panel;
	}
		
	private JPanel createProjectionNameFieldPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String projectionText
			= RIFDataLoaderToolMessages.getMessage("shapeFile.projection.label");
		JLabel projectionLabel
			= userInterfaceFactory.createLabel(projectionText);
		panel.add(projectionLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		projectionTextField
			= userInterfaceFactory.createNonEditableTextField();
		userInterfaceFactory.setEditableAppearance(projectionTextField, false);
		panel.add(projectionTextField, panelGC);		
		
		return panel;
	}
	
	
	private JPanel createDescriptionPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		String shapeFileDescriptionLabelText
			= RIFDataLoaderToolMessages.getMessage("shapeFile.description.label");
		JLabel shapeFileDescriptionLabel
			= userInterfaceFactory.createLabel(shapeFileDescriptionLabelText);
		panel.add(shapeFileDescriptionLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		shapeFileDescriptionTextField
			= userInterfaceFactory.createTextField();
		panel.add(shapeFileDescriptionTextField, panelGC);
		
		return panel;
	}
	
	private JPanel createKeyFieldsPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		String areaIdentifierFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("shapeFile.areaIdentifierFieldName.label");
		JLabel areaIdentifierFieldLabel
			= userInterfaceFactory.createLabel(areaIdentifierFieldLabelText);
		panel.add(areaIdentifierFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		areaIdentifierFieldComboBox
			= userInterfaceFactory.createComboBox();	
		panel.add(areaIdentifierFieldComboBox, panelGC);		

		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String nameFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("shapeFile.nameFieldName.label");
		JLabel nameFieldLabel
			= userInterfaceFactory.createLabel(nameFieldLabelText);
		panel.add(nameFieldLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		nameFieldComboBox
			= userInterfaceFactory.createComboBox();	
		panel.add(nameFieldComboBox, panelGC);
		
		return panel;		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public JPanel getPanel() {
		return panel;
	}
	
	public ShapeFile getData() {
		return originalShapeFile;
	}
	
	public void setData(final ShapeFile originalShapeFile) {
		this.originalShapeFile = originalShapeFile;

		currentShapeFile		
			= ShapeFile.createCopy(originalShapeFile);
		
		shapeFilePropertyPreviewTablePanel.showNoPreview();
		populateForm(currentShapeFile);
		
	}

	public boolean saveChanges() {
		
		ShapeFile shapeFileFromForm = getShapeFileFromForm();
		if (shapeFileFromForm == null) {
			return false;
		}
				
		boolean changesSaved
			= !originalShapeFile.hasIdenticalContents(shapeFileFromForm);
		if (changesSaved == true) {
			ShapeFile.copyInto(shapeFileFromForm, originalShapeFile); 			
		}
		
		return changesSaved;
		
	}
	
	public void restoreOriginalShapeFile() {
		//xxx
		populateForm(originalShapeFile);
		
	}

	private void populateForm(final ShapeFile shapeFile) {
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();

		shapeFileComponentListModel.clear();

		if (shapeFile == null) {
			String shapeFileTitleLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"shapeFileEditorPanel.emptyTitle");
			currentShapeFileTitleLabel.setText(shapeFileTitleLabelText);

			getShapeFileInfoButton.setEnabled(false);
						
			shapeFileDescriptionTextField.setText("");
			userInterfaceFactory.setEditableAppearance(
				shapeFileDescriptionTextField, 
				false);
			
			areaIdentifierFieldComboBox.setSelectedItem("");
			areaIdentifierFieldComboBox.setEnabled(false);

			nameFieldComboBox.setSelectedItem("");			
			nameFieldComboBox.setEnabled(false);
		}
		else {
			String shapeFileTitleLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"shapeFileEditorPanel.nonemptyTitle",
					shapeFile.getDisplayName());
			currentShapeFileTitleLabel.setText(shapeFileTitleLabelText);
		
			getShapeFileInfoButton.setEnabled(true);
	
			userInterfaceFactory.setEditableAppearance(
				shapeFileDescriptionTextField, 
				true);
			shapeFileDescriptionTextField.setText(
				shapeFile.getShapeFileDescription());				
		
			ArrayList<String> shapeFileComponentPaths
				= shapeFile.getShapeFileComponentPaths();
			for (String shapeFileComponentPath : shapeFileComponentPaths) {
				shapeFileComponentListModel.addElement(shapeFileComponentPath);				
			}
		
			ArrayList<String> shapeFileFieldChoices
				= shapeFile.getShapeFileFieldNames();		
		
			areaIdentifierFieldComboBox.setEnabled(true);			
			String areaIdentifierFieldName
				= shapeFile.getAreaIdentifierFieldName();
			updateAreaIdentifierFieldComboBox(
				shapeFileFieldChoices, 
				areaIdentifierFieldName);
			
			nameFieldComboBox.setEnabled(true);
			String nameFieldName
				= shapeFile.getNameFieldName();
			updateNameFieldComboBox(
				shapeFileFieldChoices, 
				nameFieldName);	
			projectionTextField.setText(shapeFile.getProjection());
		}
		updateTotalAreaIdentifiersCount(shapeFile);
		
	}
	
	private void updateNameFieldComboBox(
		final ArrayList<String> nameFieldChoices, 
		final String selectedNameFieldName) {
	

		ArrayList<String> shapeFileFieldNames = new ArrayList<String>();
		shapeFileFieldNames.addAll(nameFieldChoices);

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(selectedNameFieldName)) {
			shapeFileFieldNames.add(0, pleaseChooseMessage);
		}		
		DefaultComboBoxModel<String> nameFieldComboBoxModel
			= new DefaultComboBoxModel<String>(shapeFileFieldNames.toArray(new String[0]));
		nameFieldComboBox.setModel(nameFieldComboBoxModel);		
		nameFieldComboBox.updateUI();
		if (fieldValidationUtility.isEmpty(selectedNameFieldName)) {
			nameFieldComboBox.setSelectedItem(pleaseChooseMessage);
		}
		else {
			nameFieldComboBox.setSelectedItem(selectedNameFieldName);			
		}
	}
	
	private void updateAreaIdentifierFieldComboBox(
		final ArrayList<String> fieldChoices, 
		final String selectedAreaIdentifierFieldName) {

		ArrayList<String> shapeFileFieldNames = new ArrayList<String>();
		shapeFileFieldNames.addAll(fieldChoices);

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(selectedAreaIdentifierFieldName)) {
			shapeFileFieldNames.add(0, pleaseChooseMessage);
		}		
		DefaultComboBoxModel<String> areaIdentifierFieldComboBoxModel
			= new DefaultComboBoxModel<String>(shapeFileFieldNames.toArray(new String[0]));
		areaIdentifierFieldComboBox.setModel(areaIdentifierFieldComboBoxModel);		
		areaIdentifierFieldComboBox.updateUI();
		
		if (fieldValidationUtility.isEmpty(selectedAreaIdentifierFieldName)) {
			areaIdentifierFieldComboBox.setSelectedItem(pleaseChooseMessage);
		}
		else {
			areaIdentifierFieldComboBox.setSelectedItem(selectedAreaIdentifierFieldName);			
		}
		
	}
	
	
	public ShapeFile getShapeFileFromForm() {
		ShapeFile formShapeFile
			= ShapeFile.createCopy(currentShapeFile);
		if (formShapeFile == null) {
			return null;
		}
					
		formShapeFile.setShapeFileDescription(
			shapeFileDescriptionTextField.getText().trim());
		
		ArrayList<String> currentFormChoices
			= getComboBoxListChoices(areaIdentifierFieldComboBox);
		formShapeFile.setShapeFileFieldNames(currentFormChoices);
		String areaIdentifierFieldName
			= (String) areaIdentifierFieldComboBox.getSelectedItem();
		if (areaIdentifierFieldName.equals(pleaseChooseMessage)) {
			formShapeFile.setAreaIdentifierFieldName("");
		}
		else {
			formShapeFile.setAreaIdentifierFieldName(areaIdentifierFieldName);			
		}
		
		String selectedNameFieldName
			= (String) nameFieldComboBox.getSelectedItem();
		if (selectedNameFieldName.equals(pleaseChooseMessage)) {
			formShapeFile.setNameFieldName("");
		}
		else {
			formShapeFile.setNameFieldName(selectedNameFieldName);			
		}
		
		return formShapeFile;
	}

	private ArrayList<String> getComboBoxListChoices(final JComboBox<String> comboBox) {
		ArrayList<String> results = new ArrayList<String>();
		
		ComboBoxModel<String> comboBoxModel = comboBox.getModel();
		int numberOfChoices = comboBoxModel.getSize();
		for (int i = 0; i < numberOfChoices; i++) {
			String currentChoice = (String) comboBoxModel.getElementAt(i);
			if (currentChoice.equals(pleaseChooseMessage) == false) {
				results.add((String) comboBoxModel.getElementAt(i));				
			}
		}
		
		return results;
	}
	
	private void getShapeFileInfo() {
		try {
			shapeFilePropertyPreviewTablePanel.showPreviewData(currentShapeFile);	
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				null, 
				rifServiceException.getErrorMessages());
		}
		
	}
	
	private void updateTotalAreaIdentifiersCount(final ShapeFile shapeFile) {
		if (shapeFile == null) {
			String totalAreaIdentifiersLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"shapeFile.totalAreaIdentifiers.unknown.label");
			totalAreaIdentifiersLabel.setText(totalAreaIdentifiersLabelText);
		}
		else {
			
			int totalAreaIdentifiers
				= shapeFile.getTotalAreaIdentifiers();
			if (totalAreaIdentifiers == ShapeFile.UNKNOWN_TOTAL_AREA_IDENTIFIERS) {
				String totalAreaIdentifiersLabelText
					= RIFDataLoaderToolMessages.getMessage(
						"shapeFile.totalAreaIdentifiers.unknown.label");
				totalAreaIdentifiersLabel.setText(totalAreaIdentifiersLabelText);
			}
			else {
				String totalAreaIdentifiersLabelText
					= RIFDataLoaderToolMessages.getMessage(
						"shapeFile.totalAreaIdentifiers.known.label",
						String.valueOf(totalAreaIdentifiers));
				totalAreaIdentifiersLabel.setText(totalAreaIdentifiersLabelText);				
			}
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void validateForm() 
		throws RIFServiceException {
		
		ShapeFile shapeFileFromForm
			= getShapeFileFromForm();
		if (shapeFileFromForm != null) {
			shapeFileFromForm.checkErrors();			
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Performed
	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();
		
		if (button == getShapeFileInfoButton) {
			getShapeFileInfo();
		}		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

	
}


