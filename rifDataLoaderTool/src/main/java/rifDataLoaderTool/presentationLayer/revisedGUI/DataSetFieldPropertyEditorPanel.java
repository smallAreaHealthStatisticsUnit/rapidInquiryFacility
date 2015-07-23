package rifDataLoaderTool.presentationLayer.revisedGUI;


import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.*;
import rifDataLoaderTool.system.RIFDataLoaderMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;


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

public class DataSetFieldPropertyEditorPanel 
	implements ActionListener {
	

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataSetFieldConfiguration originalDataSetFieldConfiguration;
	
	private UserInterfaceFactory userInterfaceFactory;
	
	private Color LOAD_COLOUR = new Color(255, 220, 220);
	private Color CLEAN_COLOUR = new Color(255, 200, 200);
	private Color CONVERT_COLOUR = new Color(255, 180, 180);
	private Color OPTIMISE_COLOUR = new Color(255, 150, 150);
	private Color CHECK_COLOUR = new Color(255, 120, 120);
	private Color PUBLISH_COLOUR = new Color(255, 90, 90);
	
	private JPanel panel;

	private JLabel titleLabel;
	
	private JComboBox rifDataTypeComboBox;
	

	private JTextField loadTextField;
	private JTextField cleanTextField;
	private JTextField convertTextField;
	private JTextField conversionFunctionTextField;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetFieldPropertyEditorPanel(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		panel = userInterfaceFactory.createPanel();
	
		buildUI();
	}

	private void buildUI() {
		panel.removeAll();
		
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();			
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		
		titleLabel = userInterfaceFactory.createLabel("");
		panel.add(titleLabel, panelGC);
		
		panelGC.gridy++;
		
		String loadPhaseLabelText
			= RIFDataLoaderToolMessages.getMessage("workflowState.load.phaseLabel");
		JLabel loadPhaseLabel
			= userInterfaceFactory.createLabel(loadPhaseLabelText);
		loadPhaseLabel.setBackground(LOAD_COLOUR);
		panel.add(loadPhaseLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createLoadAttributesPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String cleanPhaseLabelText
			= RIFDataLoaderToolMessages.getMessage("workflowState.clean.phaseLabel");
		JLabel cleanPhaseLabel
			= userInterfaceFactory.createLabel(cleanPhaseLabelText);
		loadPhaseLabel.setBackground(CLEAN_COLOUR);
		panel.add(cleanPhaseLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createCleanAttributesPanel(), panelGC);
		
				
		panelGC.gridy++;
		panelGC.gridx = 0;
		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String convertPhaseLabelText
			= RIFDataLoaderToolMessages.getMessage("workflowState.convert.phaseLabel");
		JLabel convertPhaseLabel
			= userInterfaceFactory.createLabel(convertPhaseLabelText);
		loadPhaseLabel.setBackground(CONVERT_COLOUR);
		panel.add(convertPhaseLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createConvertAttributesPanel(), panelGC);
				
	}
	
	private JPanel createLoadAttributesPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		panel.setBackground(LOAD_COLOUR);
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String loadFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.loadFieldName.label");
		JLabel loadFieldLabel
			= userInterfaceFactory.createLabel(loadFieldLabelText);
		panel.add(loadFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		loadTextField
			= userInterfaceFactory.createTextField();
		panel.add(loadTextField, panelGC);
		
		return panel;
	}
	
	private JPanel createCleanAttributesPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		panel.setBackground(CLEAN_COLOUR);
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String rifDataTypeLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"abstractRIFDataType.name.label");
		JLabel rifDataTypeLabel
			= userInterfaceFactory.createLabel(rifDataTypeLabelText);
		panel.add(rifDataTypeLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		rifDataTypeComboBox
			= userInterfaceFactory.createComboBox(rifDataTypeFactory.getDataTypeNames());
		rifDataTypeComboBox.addActionListener(this);
		panel.add(rifDataTypeComboBox, panelGC);

		panelGC.gridy++;
				
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String cleanFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.cleanFieldName.label");
		JLabel cleanFieldLabel
			= userInterfaceFactory.createLabel(cleanFieldLabelText);
		panel.add(cleanFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		cleanTextField
			= userInterfaceFactory.createTextField();
		panel.add(cleanTextField, panelGC);

		return panel;
	}
	
	private JPanel createConvertAttributesPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		panel.setBackground(CONVERT_COLOUR);
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;	
		panelGC.gridx = 0;		
		String convertFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.convertFieldName.label");
		JLabel convertFieldLabel
			= userInterfaceFactory.createLabel(convertFieldLabelText);
		panel.add(convertFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		convertTextField
			= userInterfaceFactory.createTextField();
		panel.add(convertTextField, panelGC);
		
		panelGC.gridy++;
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String convertFunctionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.convertFieldName.label");
		JLabel convertFunctionFieldLabel
			= userInterfaceFactory.createLabel(convertFunctionFieldLabelText);
		panel.add(convertFunctionFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		conversionFunctionTextField
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(conversionFunctionTextField, panelGC);
		
		return panel;
	}
	
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public JPanel getPanel() {
		
		return panel;
	}
	
	public void setDataFieldConfiguration(
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
		
		this.originalDataSetFieldConfiguration = dataSetFieldConfiguration;
		
		DataSetFieldConfiguration revisedDataSetFieldConfiguration
			= DataSetFieldConfiguration.createCopy(originalDataSetFieldConfiguration);
		populateForm(revisedDataSetFieldConfiguration);
	}

	private void populateForm(
		final DataSetFieldConfiguration fieldConfiguration) {

		String dataSetFieldPanelTitle
			= RIFDataLoaderMessages.getMessage(
				"dataSetFieldConfigurationEditorPanel.title",
				fieldConfiguration.getDisplayName());
		titleLabel.setText(dataSetFieldPanelTitle);
		
		AbstractRIFDataType rifDataType
			= fieldConfiguration.getRIFDataType();
		rifDataTypeComboBox.setSelectedItem(rifDataType.getName());
		loadTextField.setText(fieldConfiguration.getLoadFieldName());
		cleanTextField.setText(fieldConfiguration.getCleanFieldName());
		convertTextField.setText(fieldConfiguration.getConvertFieldName());		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == rifDataTypeComboBox) {
			String rifDataTypeName
				= (String) rifDataTypeComboBox.getSelectedItem();
			RIFDataTypeFactory rifDataTypeFactory
				= RIFDataTypeFactory.newInstance();
			//rifDataTypeFactory.getDataType(dataTypeName);
			
			//conversionFunctionTextField.setText(t);
		}
		else {
			
		}
		
	}
	

	// ==========================================
	// Section Override
	// ==========================================

}


