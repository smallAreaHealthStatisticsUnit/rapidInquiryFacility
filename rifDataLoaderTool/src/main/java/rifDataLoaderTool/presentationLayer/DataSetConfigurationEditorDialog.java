package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.*;
import rifGenericLibrary.presentationLayer.*;
import rifGenericLibrary.system.RIFServiceException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.ArrayList;

import javax.swing.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

import java.io.*;

/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public class DataSetConfigurationEditorDialog 
	implements ActionListener,
	ListSelectionListener,
	ListDataListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private UserInterfaceFactory userInterfaceFactory;
	
	private LinearWorkflow originalLinearWorkflow;
	
	private DataSetConfiguration originalDataSetConfiguration;
	private DataSetConfiguration workingCopyDataSetConfiguration;
	
	private OrderedListPanel dataSetFieldListPanel;
	private ListEditingButtonPanel dataSetFieldListButtonPanel;
	
	private JScrollPane currentFieldScrollPane;
	
	private JDialog dialog;
	
	private JTextField nameTextField;
	private JTextField versionTextField;
	private JTextArea descriptionTextArea;
	private JTextField filePathTextField;
		
	private File selectedInputFile;
	private JButton browseButton;
	
	private JComboBox<String> rifSchemaAreasComboBox;
	
	
	private DataSetFieldPropertyEditorPanel dataSetFieldPropertyEditorPanel;
	
	
	private OKCloseButtonPanel okCloseButtonPanel;	

	
	private boolean isCancelled;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetConfigurationEditorDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		
		originalDataSetConfiguration
			= DataSetConfiguration.newInstance();
		isCancelled = false;
		
		String dataSetFieldListTitleText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfigurationEditorDialog.fieldListName.label");
		
		dataSetFieldListPanel 
			= new OrderedListPanel(
				dataSetFieldListTitleText,
		    	"",
				userInterfaceFactory,
				true);	
		//very important -- ensure that the list preserves the order in which the fields are
		//made
		dataSetFieldListPanel.setAlphabeticallySortItems(false);
		dataSetFieldListPanel.setPrototypeListValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		
		dataSetFieldListPanel.addListSelectionListener(this);
		dataSetFieldListPanel.addListDataListener(this);
		
		dataSetFieldPropertyEditorPanel
			= new DataSetFieldPropertyEditorPanel(
				dialog, 
				userInterfaceFactory);
		
		buildUI();
	}

	private void buildUI() {		

		String title
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfigurationEditorDialog.title");
		dialog
			= userInterfaceFactory.createDialog(title);
		dialog.setModal(true);

		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		JPanel panel
			= userInterfaceFactory.createPanel();
			
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfigurationEditorDialog.instructions");
		JLabel instructionsLabel
			= userInterfaceFactory.createInstructionLabel(
				instructionsText);
		panel.add(
			instructionsLabel,
			panelGC);
				
		panelGC.gridy++;
		
		panel.add(createUpperPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		
		panel.add(
			createCentralPanel(), 
			panelGC);

		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		
		okCloseButtonPanel
			= new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(
			okCloseButtonPanel.getPanel(), 
			panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(1200, 700);
	}
	
	private JPanel createUpperPanel() {
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		JPanel panel
			= userInterfaceFactory.createPanel();

		panel.add(
			createGeneralPropertiesPanel(), 
			panelGC);
		
		panelGC.gridy++;
		
		GridBagConstraints chooseFilePanelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel chooseFilePanel
			= userInterfaceFactory.createPanel();
		String filePathLabelText
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.filePath.label");		
		JLabel filePathLabel
			= userInterfaceFactory.createLabel(filePathLabelText);
		String filePathToolTip
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.filePath.toolTip");		
		filePathLabel.setToolTipText(filePathToolTip);
		
		chooseFilePanel.add(
			filePathLabel, 
			chooseFilePanelGC);
		
		chooseFilePanelGC.gridx++;
		chooseFilePanelGC.fill = GridBagConstraints.HORIZONTAL;
		chooseFilePanelGC.weightx = 1;
		filePathTextField
			= userInterfaceFactory.createNonEditableTextField();		
		chooseFilePanel.add(
			filePathTextField,
			chooseFilePanelGC);
		
		chooseFilePanelGC.gridx++;
		chooseFilePanelGC.fill = GridBagConstraints.NONE;
		chooseFilePanelGC.weightx = 0;
		
		String browseButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"general.buttons.browse.label");
		browseButton
			= userInterfaceFactory.createButton(browseButtonText);
		browseButton.addActionListener(this);
		chooseFilePanel.add(
			browseButton, 
			chooseFilePanelGC);
		
		panelGC.gridy++;
		panel.add(chooseFilePanel, panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
	}
	
	private JPanel createGeneralPropertiesPanel() {
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel
			= userInterfaceFactory.createPanel();
		
		String nameFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.name.label");
		JLabel nameFieldLabel
			= userInterfaceFactory.createLabel(nameFieldLabelText);
		String nameFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.name.toolTip");
		nameFieldLabel.setToolTipText(nameFieldToolTip);
		
		panel.add(nameFieldLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		nameTextField
			= userInterfaceFactory.createTextField();
		panel.add(nameTextField, panelGC);
		
		panelGC.gridy++;
			
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;		
		String versionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.version.label");
		JLabel versionFieldLabel
			= userInterfaceFactory.createLabel(versionFieldLabelText);
		String versionFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.version.toolTip");
		versionFieldLabel.setToolTipText(versionFieldToolTip);
		
		panel.add(versionFieldLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		versionTextField
			= userInterfaceFactory.createTextField();
		panel.add(versionTextField, panelGC);
		
		panelGC.gridy++;		
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		String descriptionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.description.label");
		JLabel descriptionFieldLabel
			= userInterfaceFactory.createLabel(descriptionFieldLabelText);
		String descriptionFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.description.toolTip");
		descriptionFieldLabel.setToolTipText(descriptionFieldToolTip);
		
		panel.add(descriptionFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		descriptionTextArea
			= userInterfaceFactory.createTextArea();
		panel.add(
			descriptionTextArea, 
			panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String rifSchemaAreasLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.rifSchemaArea.label");
		JLabel rifSchemaAreasLabel
			= userInterfaceFactory.createLabel(rifSchemaAreasLabelText);
		String rifSchemaAreasToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.rifSchemaArea.toolTip");		
		rifSchemaAreasLabel.setToolTipText(rifSchemaAreasToolTip);
		panel.add(rifSchemaAreasLabel, panelGC);	
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		rifSchemaAreasComboBox
			= userInterfaceFactory.createComboBox(RIFSchemaArea.getAllSchemaNames());
		rifSchemaAreasComboBox.addActionListener(this);
		panel.add(rifSchemaAreasComboBox, panelGC);

		return panel;
	}
	
	
	private JPanel createCentralPanel() {
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;

		panelGC.weightx = 0.3;
		JPanel panel
			= userInterfaceFactory.createPanel();
		currentFieldScrollPane
			= userInterfaceFactory.createScrollPane(dataSetFieldPropertyEditorPanel.getPanel());
				
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				createDataSetListArea(), 
				createFieldEditingArea());

		splitPane.setDividerLocation(0.4);
		panel.add(splitPane, panelGC);
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
	
	
	
	private JPanel createDataSetListArea() {
		
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel
			= userInterfaceFactory.createPanel();
		panel.setMinimumSize(new Dimension(250, 400));
		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
				
		JScrollPane fieldPane
			= userInterfaceFactory.createScrollPane(
				dataSetFieldListPanel.getPanel());		
		panel.add(
			fieldPane, 
			panelGC);
		
		dataSetFieldListButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		dataSetFieldListButtonPanel.includeAddButton(null);
		dataSetFieldListButtonPanel.includeCopyButton(null);
		dataSetFieldListButtonPanel.includeEditButton(null);		
		dataSetFieldListButtonPanel.includeDeleteButton(null);
		dataSetFieldListButtonPanel.rightJustifyButtons();
		dataSetFieldListButtonPanel.addActionListener(this);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(dataSetFieldListButtonPanel.getPanel(), panelGC);
		
		return panel;
	}
	
	private JPanel createFieldEditingArea() {
		JPanel panel
			= userInterfaceFactory.createBorderLayoutPanel();
		panel.setMinimumSize(new Dimension(750, 400));

		panel.add(dataSetFieldPropertyEditorPanel.getPanel(), BorderLayout.NORTH);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	public void setData(
		final LinearWorkflow originalLinearWorkflow,
		final DataSetConfiguration originalDataSetConfiguration) {
		
		this.originalLinearWorkflow = originalLinearWorkflow;
		this.originalDataSetConfiguration = originalDataSetConfiguration;
		
		workingCopyDataSetConfiguration
			= DataSetConfiguration.createCopy(originalDataSetConfiguration);
		
		
		populateFormFromWorkingCopy();
	}
	
	private void populateFormFromWorkingCopy() {
		
		nameTextField.setText(workingCopyDataSetConfiguration.getName());
		versionTextField.setText(workingCopyDataSetConfiguration.getVersion());
		descriptionTextArea.setText(workingCopyDataSetConfiguration.getDescription());
		filePathTextField.setText(workingCopyDataSetConfiguration.getFilePath());
		
		RIFSchemaArea rifSchemaArea = workingCopyDataSetConfiguration.getRIFSchemaArea();
		rifSchemaAreasComboBox.removeActionListener(this);
		rifSchemaAreasComboBox.setSelectedItem(rifSchemaArea.getName());
		rifSchemaAreasComboBox.addActionListener(this);
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= workingCopyDataSetConfiguration.getFieldConfigurations();
		
		dataSetFieldListPanel.removeListSelectionListener(this);
		dataSetFieldListPanel.clearList();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			dataSetFieldListPanel.addListItem(fieldConfiguration);					
		}
		
		if (fieldConfigurations.isEmpty() == false) {
			dataSetFieldListPanel.setSelectedItem(0);
			
			DataSetFieldConfiguration selectedDataSetFieldConfiguration
				= (DataSetFieldConfiguration) dataSetFieldListPanel.getSelectedItem();
			
			dataSetFieldPropertyEditorPanel.setDataSetConfiguration(workingCopyDataSetConfiguration);
		}
		
		dataSetFieldListPanel.addListSelectionListener(this);
	}
	
	private void populateWorkingCopyFromForm() {
		
		//capture the general fields
		workingCopyDataSetConfiguration.setName(nameTextField.getText().trim());
		workingCopyDataSetConfiguration.setVersion(versionTextField.getText().trim());
		workingCopyDataSetConfiguration.setDescription(descriptionTextArea.getText().trim());
		workingCopyDataSetConfiguration.setFilePath(filePathTextField.getText().trim());
		
		//the fields are already accounted for 

	}
	
	
	public void show() {
		dialog.setVisible(true);
	}
	
	
	private void saveChanges() {
		try {
			populateWorkingCopyFromForm();
			//check all errors that are independent of any other 
			workingCopyDataSetConfiguration.checkErrors();			

			DataSetConfiguration.copyInto(
				workingCopyDataSetConfiguration, 
				originalDataSetConfiguration);
					
			isCancelled = false;
			dialog.setVisible(false);		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(dialog, rifServiceException.getErrorMessages());
		}		
	}
	
	private void browseForInputFile() {
		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
		CSVFileFilter csvFileFilter
			= new CSVFileFilter();
		fileChooser.setFileFilter(csvFileFilter);
		int result
			= fileChooser.showOpenDialog(dialog);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		selectedInputFile
			= fileChooser.getSelectedFile();
		filePathTextField.setText(selectedInputFile.getAbsolutePath());
	}
	

	private void addDataSetFieldConfiguration() {
		DataSetFieldConfiguration dataSetFieldConfiguration
			= DataSetFieldConfiguration.newInstance();
		String coreFieldName 
			= DataSetFieldNameGenerator.generateCoreFieldName(workingCopyDataSetConfiguration);
		String loadFieldName 
			= DataSetFieldNameGenerator.generateLoadFieldName(workingCopyDataSetConfiguration);
		String cleanFieldName 
			= DataSetFieldNameGenerator.generateCleanFieldName(workingCopyDataSetConfiguration);
		dataSetFieldConfiguration.setCoreFieldName(coreFieldName);
		dataSetFieldConfiguration.setLoadFieldName(loadFieldName);
		dataSetFieldConfiguration.setCleanFieldName(cleanFieldName);
		
		dataSetFieldListPanel.addListItem(dataSetFieldConfiguration);
		dataSetFieldListPanel.setSelectedItem(dataSetFieldConfiguration);
		updateButtonStates();
		
		dataSetFieldListPanel.updateUI();	
	}
	
	private void copyDataSetFieldConfiguration() {
		DataSetFieldConfiguration selectedDataSetFieldConfiguration
			= (DataSetFieldConfiguration) dataSetFieldListPanel.getSelectedItem();		
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("dataSetConfigurationEditorDialog.copyDataSetFieldConfiguration.title");
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("dataSetConfigurationEditorDialog.copyDataSetFieldConfiguration.instructions");
		String fieldName
			= RIFDataLoaderToolMessages.getMessage("dataSetConfigurationEditorDialog.copyDataSetFieldConfiguration.fieldName");		
		String[] existingListItemNames
			= dataSetFieldListPanel.getDisplayNames();
		
		CopyListItemNameEditorDialog dialog
			= new CopyListItemNameEditorDialog(
				userInterfaceFactory,
				dialogTitle,
				instructionsText,
				fieldName,
				existingListItemNames);
		
		dialog.show();
		if (dialog.isCancelled()) {
			return;
		}
		
		String selectedName = dialog.getFieldName();
		DataSetFieldConfiguration copiedDataSetFieldConfiguration
			= DataSetFieldConfiguration.createCopy(selectedDataSetFieldConfiguration);
		copiedDataSetFieldConfiguration.setLoadFieldName(selectedName);
		dataSetFieldListPanel.addListItem(copiedDataSetFieldConfiguration);
		dataSetFieldListPanel.setSelectedItem(copiedDataSetFieldConfiguration);
		updateButtonStates();
		dataSetFieldListPanel.updateUI();		
	}
	
	private void editDataSetFieldConfiguration() {
		DataSetFieldConfiguration dataSetFieldConfiguration
			= (DataSetFieldConfiguration) dataSetFieldListPanel.getSelectedItem();
	}
	
	private void deleteDataSetFieldConfiguration() {
		dataSetFieldListPanel.deleteSelectedListItems();
		updateButtonStates();
	}

	
	private void close() {
		isCancelled = true;
		dialog.setVisible(false);
	}

	public boolean isCancelled() {
		return isCancelled;		
	}
	
	public DataSetConfiguration getDataSetConfiguration() {
		
		return workingCopyDataSetConfiguration;
	}
	
	
	public void resetForm() {
		
	}
	
	private void updateButtonStates() {
		if (dataSetFieldListPanel.isEmpty()) {
			dataSetFieldListButtonPanel.indicateEmptyState();
		}
		else {
			dataSetFieldListButtonPanel.indicatePopulatedState();			
		}		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (okCloseButtonPanel.isOKButton(source)) {
			saveChanges();
		}
		else if (source == rifSchemaAreasComboBox) {
			dataSetFieldPropertyEditorPanel.updateUI();
		}
		else if (source == browseButton) {
			browseForInputFile();
		}
		else if (dataSetFieldListButtonPanel.isAddButton(source)) {
			addDataSetFieldConfiguration();
		}
		else if (dataSetFieldListButtonPanel.isCopyButton(source)) {
			copyDataSetFieldConfiguration();			
		}
		else if (dataSetFieldListButtonPanel.isEditButton(source)) {
			editDataSetFieldConfiguration();			
		}
		else if (dataSetFieldListButtonPanel.isDeleteButton(source)) {
			deleteDataSetFieldConfiguration();			
		}
		
		else if (okCloseButtonPanel.isCloseButton(source)) {
			close();
		}
	}
	
	public void valueChanged(ListSelectionEvent event) {
		//validate the currently displayed field
		try {
			dataSetFieldPropertyEditorPanel.saveChanges();

			DataSetFieldConfiguration originalDataSetFieldConfiguration
				= dataSetFieldPropertyEditorPanel.getOriginalDataSetFieldConfiguration();
			
			//Save changes happened and did not throw any exceptions
			//It is safe to change the currently displayed field
			DataSetFieldConfiguration selectedFieldConfiguration
				= (DataSetFieldConfiguration) dataSetFieldListPanel.getSelectedItem();
			if (selectedFieldConfiguration == null) {
				//a list item may have been added or deleted
				dataSetFieldListPanel.selectFirstItem();
			}
			
			dataSetFieldPropertyEditorPanel.setData(
				selectedFieldConfiguration);
			
			JScrollBar verticalScrollBar
				= currentFieldScrollPane.getVerticalScrollBar();
			verticalScrollBar.setValue(0);	
		}
		catch(RIFServiceException rifServiceException) {
			//We need to go back to the previously selected item
			DataSetFieldConfiguration previouslySelectedDataSetFieldConfiguration
				= dataSetFieldPropertyEditorPanel.getOriginalDataSetFieldConfiguration();			
			dataSetFieldListPanel.removeListSelectionListener(this);
			dataSetFieldListPanel.setSelectedItem(previouslySelectedDataSetFieldConfiguration);
			dataSetFieldListPanel.addListSelectionListener(this);
			ErrorDialog.showError(dialog, rifServiceException.getErrorMessages());			
		}
	}
	
	//Interface: List Data Listener
	public void contentsChanged(final ListDataEvent event) {
		updateButtonStates();
	}
	
	public void intervalAdded(final ListDataEvent event) {
		updateButtonStates();		
	}
	
	public void intervalRemoved(final ListDataEvent event) {
		updateButtonStates();		
	}

	
	// ==========================================
	// Section Override
	// ==========================================

}


