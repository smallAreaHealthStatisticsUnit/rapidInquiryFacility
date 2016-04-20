package rifDataLoaderTool.presentationLayer.interactive;


import rifDataLoaderTool.businessConceptLayer.RIFSchemaArea;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.ActionListener;

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

public class DataSetPropertyEditorPanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private boolean isRenderingForConfigurationHintsFeature;
	private DataSetConfiguration dataSetConfiguration;
	
	private UserInterfaceFactory userInterfaceFactory;
	private JPanel panel;
	private JTextField nameTextField;
	private JTextField versionTextField;
	private JTextArea descriptionTextArea;
	private JTextField filePathTextField;		
	private JComboBox<String> rifSchemaAreasComboBox;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetPropertyEditorPanel(
		final UserInterfaceFactory userInterfaceFactory,
		final boolean isRenderingForConfigurationHintsFeature) {
		
		this.userInterfaceFactory = userInterfaceFactory;
		this.isRenderingForConfigurationHintsFeature = isRenderingForConfigurationHintsFeature;
		buildUI();
	}

	private void buildUI() {

		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panel
			= userInterfaceFactory.createPanel();
		
		String nameFieldLabelText = "";
		if (isRenderingForConfigurationHintsFeature) {
			nameFieldLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"configurationHint.regularExpressionPattern.label");
		}
		else {
			nameFieldLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.name.label");			
		}
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
		panel.add(rifSchemaAreasComboBox, panelGC);
		
		filePathTextField
			= userInterfaceFactory.createNonEditableTextField();
		if (isRenderingForConfigurationHintsFeature == false) {
			panelGC.gridy++;
			panelGC.gridx = 0;		
			panelGC.fill = GridBagConstraints.NONE;
			String filePathLabelText
				= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.filePath.label");
			JLabel filePathLabel
				= userInterfaceFactory.createLabel(filePathLabelText);
			panel.add(filePathLabel, panelGC);
			panelGC.gridx++;
			panelGC.fill = GridBagConstraints.HORIZONTAL;
			panelGC.weightx = 1;

			panel.add(filePathTextField, panelGC);		
		}

	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public DataSetConfiguration getData() {
		return dataSetConfiguration;
	}
	
	public void setData(final DataSetConfiguration dataSetConfiguration) {
		this.dataSetConfiguration = dataSetConfiguration;
		populateForm(dataSetConfiguration);
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void saveChanges() {
		populateDataSetConfigurationFromForm(dataSetConfiguration);
	}
	
	public void populateDataSetConfigurationFromForm(
		final DataSetConfiguration dataSetConfiguration) {

		if (dataSetConfiguration == null) {
			return;
		}
		//capture the general fields
		dataSetConfiguration.setName(nameTextField.getText().trim());
		dataSetConfiguration.setVersion(versionTextField.getText().trim());
		dataSetConfiguration.setDescription(descriptionTextArea.getText().trim());
		
		String rifSchemaPhrase
			= (String) rifSchemaAreasComboBox.getSelectedItem();
		RIFSchemaArea rifSchemaArea = RIFSchemaArea.getSchemaAreaFromName(rifSchemaPhrase);
		dataSetConfiguration.setRIFSchemaArea(rifSchemaArea);
	}
	
	public void setIsEnabled(final boolean isEnabled) {
		nameTextField.setEnabled(isEnabled);
		versionTextField.setEnabled(isEnabled);
		descriptionTextArea.setEnabled(isEnabled);
		
		userInterfaceFactory.setEditableAppearance(nameTextField, isEnabled);
		userInterfaceFactory.setEditableAppearance(versionTextField, isEnabled);
		userInterfaceFactory.setEditableAppearance(descriptionTextArea, isEnabled);
		rifSchemaAreasComboBox.setEnabled(isEnabled);					
	}
	
	public void populateForm(final DataSetConfiguration dataSetConfiguration) {
		if (dataSetConfiguration == null) {
			setIsEnabled(false);
		}
		else {
			setIsEnabled(true);

			nameTextField.setText(dataSetConfiguration.getName());
			versionTextField.setText(dataSetConfiguration.getVersion());
			descriptionTextArea.setText(dataSetConfiguration.getDescription());			
			filePathTextField.setText(dataSetConfiguration.getFilePath());
		
			RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
			//rifSchemaAreasComboBox.removeActionListener(this);
			
			if (rifSchemaArea != null) {
				rifSchemaAreasComboBox.setSelectedItem(rifSchemaArea.getName());				
			}
			//rifSchemaAreasComboBox.addActionListener(this);
		}
	}
	
	public void removeActionListener(final ActionListener actionListener) {
		rifSchemaAreasComboBox.removeActionListener(actionListener);
		
	}
	
	public void addActionListener(final ActionListener actionListener) {
		rifSchemaAreasComboBox.addActionListener(actionListener);
	}
	
	public boolean isSchemaAreaComboBox(final Object eventObject) {
		return(eventObject == rifSchemaAreasComboBox);
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

}


