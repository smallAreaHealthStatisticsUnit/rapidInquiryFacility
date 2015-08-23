package rifDataLoaderTool.presentationLayer;


import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.*;
import rifDataLoaderTool.system.RIFDataLoaderMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.presentationLayer.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

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
	implements ActionListener,
	CaretListener {
	

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataSetConfiguration originalDataSetConfiguration;
	private DataSetFieldConfiguration originalDataSetFieldConfiguration;
	private DataSetFieldConfiguration workingCopyDataSetFieldConfiguration;
	
	private UserInterfaceFactory userInterfaceFactory;
	
	private Color EXTRACT_COLOUR = new Color(220, 220, 220);
	private Color CLEAN_COLOUR = new Color(200, 200, 200);
	private Color CONVERT_COLOUR = new Color(180, 180, 180);
	private Color OPTIMISE_COLOUR = new Color(150, 150, 150);
	private Color CHECK_COLOUR = new Color(120, 120, 120);
	private Color PUBLISH_COLOUR = new Color(100, 100, 100);
	
	private JDialog parentDialog;
	private JPanel panel;
	private JPanel fieldPanel;

	private JLabel titleLabel;
	
	private JComboBox<String> rifDataTypeComboBox;
	

	private JTextField loadTextField;
	private JTextField descriptionTextField;
	private JComboBox<String> fieldPurposeComboBox;
	private JComboBox<String> fieldRequirementLevelComboBox;
	
	
	private JTextField cleanTextField;
	private JComboBox<String> fieldChangeAuditLevelComboBox;
	private JCheckBox isRequiredField;
	
	
	private JComboBox<String> convertComboBox;
	private JTextField conversionFunctionTextField;
	
	
	private JCheckBox includePercentEmptyCheckBox;
	private JCheckBox includePercentEmptyPerYearCheckBox;
	private JCheckBox usedToIdentifyDuplicatesCheckBox;
	
	private JCheckBox optimiseUsingIndexCheckBox;
	
	private boolean changesMade;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetFieldPropertyEditorPanel(
		final JDialog parentDialog,
		final UserInterfaceFactory userInterfaceFactory) {

		this.parentDialog = parentDialog;
		
		this.userInterfaceFactory = userInterfaceFactory;
		panel = userInterfaceFactory.createPanel();
		fieldPanel = userInterfaceFactory.createGridLayoutPanel(6, 2);
	
		changesMade = false;
		
		buildUI();
	}

	private void buildUI() {		
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.ipadx = 10;
		panelGC.fill = GridBagConstraints.HORIZONTAL;		
		panelGC.weightx = 1;
		
		String dataSetFieldPanelTitle
			= RIFDataLoaderMessages.getMessage(
				"dataSetFieldConfigurationEditorPanel.title");
		titleLabel = userInterfaceFactory.createHTMLLabel(1, dataSetFieldPanelTitle);
		panel.add(titleLabel, panelGC);
		
		panelGC.gridy++;
		
		JPanel fieldPanel = userInterfaceFactory.createPanel();
		GridBagConstraints fieldPanelGC
			= userInterfaceFactory.createGridBagConstraints();
		fieldPanelGC.fill = GridBagConstraints.BOTH;
		fieldPanelGC.weighty = 1;
		fieldPanelGC.weightx = 0.3;		
		String extractPhaseLabelText
			= RIFDataLoaderToolMessages.getMessage("workflowState.extract.phaseLabel");
		String extractPhaseToolTip
			= RIFDataLoaderToolMessages.getMessage("workflowState.extract.phaseLabel.toolTip");
		WorkflowStateLabelPanel loadLabelPanel
			= new WorkflowStateLabelPanel(
				userInterfaceFactory,
				extractPhaseLabelText,
				extractPhaseToolTip,
				EXTRACT_COLOUR);
		fieldPanel.add(loadLabelPanel, fieldPanelGC);
		fieldPanelGC.gridx++;
		fieldPanelGC.fill = GridBagConstraints.BOTH;
		fieldPanelGC.weightx = 0.7;		
		fieldPanel.add(createLoadAttributesPanel(), fieldPanelGC);
		
		fieldPanelGC.gridy++;
		fieldPanelGC.gridx = 0;		
		fieldPanelGC.weightx = 0.3;		
		String cleanPhaseLabelText
			= RIFDataLoaderToolMessages.getMessage("workflowState.clean.phaseLabel");
		String cleanPhaseToolTip
			= RIFDataLoaderToolMessages.getMessage("workflowState.clean.phaseLabel.toolTip");		
		WorkflowStateLabelPanel cleanLabelPanel
			= new WorkflowStateLabelPanel(
				userInterfaceFactory,
				cleanPhaseLabelText,
				cleanPhaseToolTip,
				CLEAN_COLOUR);
		fieldPanel.add(cleanLabelPanel, fieldPanelGC);
		fieldPanelGC.gridx++;
		fieldPanelGC.weightx = 0.7;
		fieldPanel.add(createCleanAttributesPanel(), fieldPanelGC);
				
		fieldPanelGC.gridy++;
		
		fieldPanelGC.gridx = 0;
		fieldPanelGC.weightx = 0.3;		
		String convertPhaseLabelText
			= RIFDataLoaderToolMessages.getMessage("workflowState.convert.phaseLabel");
		String convertPhaseToolTip
			= RIFDataLoaderToolMessages.getMessage("workflowState.convert.phaseLabel.toolTip");
		WorkflowStateLabelPanel convertLabelPanel
			= new WorkflowStateLabelPanel(
				userInterfaceFactory,
				convertPhaseLabelText,
				convertPhaseToolTip,
				CONVERT_COLOUR);
		fieldPanel.add(convertLabelPanel, fieldPanelGC);
		fieldPanelGC.gridx++;
		fieldPanelGC.weightx = 0.7;
		fieldPanel.add(createConvertAttributesPanel(), fieldPanelGC);

		
		fieldPanelGC.gridy++;

		fieldPanelGC.gridx = 0;
		fieldPanelGC.weightx = 0.3;		
		String optimisePhaseLabelText
			= RIFDataLoaderToolMessages.getMessage("workflowState.optimise.phaseLabel");
		String optimisePhaseToolTip
			= RIFDataLoaderToolMessages.getMessage("workflowState.optimise.phaseLabel.toolTip");		
		WorkflowStateLabelPanel optimiseLabelPanel
			= new WorkflowStateLabelPanel(
				userInterfaceFactory,
				optimisePhaseLabelText,
				optimisePhaseToolTip,
				OPTIMISE_COLOUR);
		fieldPanel.add(optimiseLabelPanel, fieldPanelGC);
		fieldPanelGC.gridx++;
		fieldPanelGC.weightx = 0.7;
		fieldPanel.add(createOptimiseAttributesPanel(), fieldPanelGC);
		
		
		fieldPanelGC.gridy++;

		fieldPanelGC.gridx = 0;
		fieldPanelGC.weightx = 0.3;		
		String checkPhaseLabelText
			= RIFDataLoaderToolMessages.getMessage("workflowState.check.phaseLabel");
		String checkPhaseToolTip
			= RIFDataLoaderToolMessages.getMessage("workflowState.check.phaseLabel.toolTip");
		WorkflowStateLabelPanel checkLabelPanel
			= new WorkflowStateLabelPanel(
				userInterfaceFactory,
				checkPhaseLabelText,
				checkPhaseToolTip,
				CHECK_COLOUR);
		fieldPanel.add(checkLabelPanel, fieldPanelGC);
		fieldPanelGC.gridx++;
		fieldPanelGC.weightx = 0.7;
		fieldPanel.add(createCheckAttributesPanel(), fieldPanelGC);
		

		panel.add(fieldPanel, panelGC);
		
	}

	private void populateFieldPanel() {
		fieldPanel.removeAll();
		

		
		
		
		
	}
	
	private JPanel createLoadAttributesPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		panel.setBackground(EXTRACT_COLOUR);
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
		String loadFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
			"dataSetFieldConfiguration.loadFieldName.toolTip");
		loadFieldLabel.setToolTipText(loadFieldToolTip);
		
		panel.add(loadFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		loadTextField
			= userInterfaceFactory.createTextField();
		loadTextField.addCaretListener(this);
		panel.add(loadTextField, panelGC);

		panelGC.gridy++;
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String fieldPurposeLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.fieldPurpose.label");
		JLabel fieldPurposeLabel
			= userInterfaceFactory.createLabel(fieldPurposeLabelText);
		String fieldPurposeToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.fieldPurpose.toolTip");
		fieldPurposeLabel.setToolTipText(fieldPurposeToolTip);
		
		panel.add(fieldPurposeLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		fieldPurposeComboBox
			= userInterfaceFactory.createComboBox(FieldPurpose.getNames());
		fieldPurposeComboBox.setBackground(EXTRACT_COLOUR);
		fieldPurposeComboBox.addActionListener(this);
		panel.add(fieldPurposeComboBox, panelGC);

		panelGC.gridy++;

		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String fieldRequirementLevelLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.fieldRequirementLevel.label");
				
		JLabel fieldRequirementLevelLabel
			= userInterfaceFactory.createLabel(fieldRequirementLevelLabelText);
		String fieldRequirementLevelToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.fieldRequirementLevel.toolTip");
		fieldRequirementLevelLabel.setToolTipText(
			userInterfaceFactory.createHTMLToolTipText(fieldRequirementLevelToolTip));
		
		panel.add(fieldRequirementLevelLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		fieldRequirementLevelComboBox
			= userInterfaceFactory.createComboBox(FieldRequirementLevel.getNames());
		fieldRequirementLevelComboBox.setBackground(EXTRACT_COLOUR);
		fieldRequirementLevelComboBox.addActionListener(this);
		panel.add(fieldRequirementLevelComboBox, panelGC);

		panelGC.gridy++;
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String descriptionLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.coreFieldDescription.label");
		JLabel descriptionLabel
			= userInterfaceFactory.createLabel(descriptionLabelText);
		String descriptionToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.coreFieldDescription.toolTip");
		descriptionLabel.setToolTipText(
			userInterfaceFactory.createHTMLToolTipText(descriptionToolTip));
		
		panel.add(descriptionLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		descriptionTextField
			= userInterfaceFactory.createTextField();
		panel.add(descriptionTextField, panelGC);
		
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
		String rifDataTypeToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"abstractRIFDataType.name.toolTip");
		rifDataTypeLabel.setToolTipText(rifDataTypeToolTip);
		
		panel.add(rifDataTypeLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		rifDataTypeComboBox
			= userInterfaceFactory.createComboBox(rifDataTypeFactory.getDataTypeNames());
		rifDataTypeComboBox.setBackground(CLEAN_COLOUR);
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
		String cleanFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.cleanFieldName.toolTip");
		cleanFieldLabel.setToolTipText(cleanFieldToolTip);
		
		panel.add(cleanFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		cleanTextField
			= userInterfaceFactory.createTextField();
		cleanTextField.addCaretListener(this);
		panel.add(cleanTextField, panelGC);


		panelGC.gridy++;
				
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String fieldChangeAuditLevelLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.fieldChangeAuditLevel.label");
		JLabel fieldChangeAuditLevelFieldLabel
			= userInterfaceFactory.createLabel(fieldChangeAuditLevelLabelText);
		String fieldChangeAuditLevelToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.fieldChangeAuditLevel.toolTip");
		fieldChangeAuditLevelFieldLabel.setToolTipText(fieldChangeAuditLevelToolTip);
		
		panel.add(fieldChangeAuditLevelFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		fieldChangeAuditLevelComboBox
			= userInterfaceFactory.createComboBox(FieldChangeAuditLevel.getNames());
		fieldChangeAuditLevelComboBox.addActionListener(this);
		panel.add(fieldChangeAuditLevelComboBox, panelGC);
				
		panelGC.gridy++;
		
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String isRequiredFieldText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.isRequired.label");
		isRequiredField
			= userInterfaceFactory.createCheckBox(isRequiredFieldText);
		String isRequiredFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.isRequired.toolTip");
		isRequiredField.setToolTipText(isRequiredFieldToolTip);
		
		panel.add(isRequiredField, panelGC);	
				
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
		String convertFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.convertFieldName.toolTip");
		convertFieldLabel.setToolTipText(convertFieldToolTip);
		
		panel.add(convertFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		convertComboBox
			= userInterfaceFactory.createComboBox(new String[0]);
		convertComboBox.setEditable(true);
		convertComboBox.addActionListener(this);
		panel.add(convertComboBox, panelGC);
		
		panelGC.gridy++;
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String convertFunctionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.convertFunctionName.label");
		JLabel convertFunctionFieldLabel
			= userInterfaceFactory.createLabel(convertFunctionFieldLabelText);
		String convertFunctionFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.convertFunctionName.toolTip");
		convertFunctionFieldLabel.setToolTipText(convertFunctionFieldToolTip);
				
		panel.add(convertFunctionFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		conversionFunctionTextField
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(conversionFunctionTextField, panelGC);
		
		return panel;
	}
	
	private JPanel createOptimiseAttributesPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		panel.setBackground(OPTIMISE_COLOUR);
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.anchor = GridBagConstraints.SOUTHWEST;
		panelGC.weightx = 1;	
		panelGC.gridx = 0;		
		String optimiseUsingIndexLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.optimiseUsingIndex.label");
		optimiseUsingIndexCheckBox
			= userInterfaceFactory.createCheckBox(optimiseUsingIndexLabelText);
		String optimiseUsingIndexToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.optimiseUsingIndex.toolTip");
		optimiseUsingIndexCheckBox.setToolTipText(optimiseUsingIndexToolTip);
		
		optimiseUsingIndexCheckBox.addActionListener(this);
		panel.add(optimiseUsingIndexCheckBox, panelGC);
		
		return panel;		
	}	
	
	private JPanel createCheckAttributesPanel() {
		JPanel panel = userInterfaceFactory.createGridLayoutPanel(3, 1);		
		panel.setBackground(CHECK_COLOUR);
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		panelGC.gridy++;
		JPanel checkOptionsPanel = userInterfaceFactory.createGridLayoutPanel(3, 1);
		checkOptionsPanel.setBackground(CHECK_COLOUR);
		String includePercentEmptyLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.checkOptions.includePercentEmptyCheck.label");
		includePercentEmptyCheckBox
			= userInterfaceFactory.createCheckBox(includePercentEmptyLabelText);
		String includePercentEmptyToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.checkOptions.includePercentEmptyCheck.toolTip");
		includePercentEmptyCheckBox.setToolTipText(includePercentEmptyToolTip);
		
		includePercentEmptyCheckBox.addActionListener(this);
		String includePercentEmptyToolTipText
			= RIFDataLoaderToolMessages.getMessage("rifCheckOption.percentEmpty.description");
		includePercentEmptyCheckBox.setToolTipText(includePercentEmptyToolTipText);
		checkOptionsPanel.add(includePercentEmptyCheckBox);
		String includePercentEmptyPerYearLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.checkOptions.includePercentEmptyPerYearCheck.label");
		includePercentEmptyPerYearCheckBox
			= userInterfaceFactory.createCheckBox(includePercentEmptyPerYearLabelText);
		String includePercentEmptyPerYearToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.checkOptions.includePercentEmptyPerYearCheck.toolTip");
		includePercentEmptyPerYearCheckBox.setToolTipText(includePercentEmptyPerYearToolTip);		
		includePercentEmptyPerYearCheckBox.addActionListener(this);
		String includePercentEmptyPerYearToolTipText
			= RIFDataLoaderToolMessages.getMessage("rifCheckOption.percentEmptyPerYear.description");
		includePercentEmptyCheckBox.setToolTipText(includePercentEmptyPerYearToolTipText);
		
		checkOptionsPanel.add(includePercentEmptyPerYearCheckBox);		
		panel.add(checkOptionsPanel, panelGC);

		String usedToIdentifyDuplicatesLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.isDuplicateIdentificationField.label");				
		usedToIdentifyDuplicatesCheckBox
			= userInterfaceFactory.createCheckBox(usedToIdentifyDuplicatesLabelText);
		String usedToIdentifyDuplicatesToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.isDuplicateIdentificationField.toolTip");				
		usedToIdentifyDuplicatesCheckBox.setToolTipText(usedToIdentifyDuplicatesToolTip);
		usedToIdentifyDuplicatesCheckBox.addActionListener(this);
		checkOptionsPanel.add(usedToIdentifyDuplicatesCheckBox, panelGC);

		panel.add(checkOptionsPanel, panelGC);
		
		return panel;
	}	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public JPanel getPanel() {
		
		return panel;
	}
	
	public DataSetFieldConfiguration getOriginalDataSetFieldConfiguration() {
		return originalDataSetFieldConfiguration;
	}
	
	/*
	public void setDataSetConfiguration(
		final DataSetConfiguration originalDataSetConfiguration) {
		
		this.originalDataSetConfiguration = originalDataSetConfiguration;
	}
	*/
	
	public void setData(
		final DataSetConfiguration originalDataSetConfiguration,
		final DataSetFieldConfiguration originalDataSetFieldConfiguration) {
				
		this.originalDataSetConfiguration = originalDataSetConfiguration;
		this.originalDataSetFieldConfiguration = originalDataSetFieldConfiguration
				;
		workingCopyDataSetFieldConfiguration
			= DataSetFieldConfiguration.createCopy(originalDataSetFieldConfiguration);

		populateFormFromWorkingCopy();
		
		changesMade = false;
	}

	private void populateFormFromWorkingCopy() {

		AbstractRIFDataType rifDataType
			= workingCopyDataSetFieldConfiguration.getRIFDataType();
		rifDataTypeComboBox.setSelectedItem(rifDataType.getName());
		loadTextField.setText(workingCopyDataSetFieldConfiguration.getLoadFieldName());
		descriptionTextField.setText(workingCopyDataSetFieldConfiguration.getCoreFieldDescription());
		
		FieldPurpose fieldPurpose = workingCopyDataSetFieldConfiguration.getFieldPurpose();
		if (fieldPurpose != null) {
			fieldPurposeComboBox.setSelectedItem(fieldPurpose.getName());			
		}
			
		FieldRequirementLevel fieldRequirementLevel
			 = workingCopyDataSetFieldConfiguration.getFieldRequirementLevel();
		fieldRequirementLevelComboBox.setSelectedItem(fieldRequirementLevel.getName());

		cleanTextField.setText(workingCopyDataSetFieldConfiguration.getCleanFieldName());
		
		if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
			isRequiredField.setSelected(true);
		}
		else {
			isRequiredField.setSelected(!workingCopyDataSetFieldConfiguration.isEmptyValueAllowed());			
		}
		FieldChangeAuditLevel currentFieldChangeAuditLevel
			= workingCopyDataSetFieldConfiguration.getFieldChangeAuditLevel();
		fieldChangeAuditLevelComboBox.setSelectedItem(currentFieldChangeAuditLevel.getName());

		convertComboBox.setSelectedItem(workingCopyDataSetFieldConfiguration.getConvertFieldName());
		RIFConversionFunction rifConversionFunction
			= workingCopyDataSetFieldConfiguration.getConvertFunction();
		if (rifConversionFunction == null) {
			conversionFunctionTextField.setText("");			
		}
		else {
			conversionFunctionTextField.setText(rifConversionFunction.getFunctionName());			
		}
		
		includePercentEmptyCheckBox.setSelected(false);
		includePercentEmptyPerYearCheckBox.setSelected(false);

		ArrayList<RIFCheckOption> checkOptions
			= workingCopyDataSetFieldConfiguration.getCheckOptions();
		for (RIFCheckOption checkOption : checkOptions) {
			if (checkOption == RIFCheckOption.PERCENT_EMPTY) {
				includePercentEmptyCheckBox.setSelected(true);
			}
			else if (checkOption == RIFCheckOption.PERCENT_EMPTY_PER_YEAR) {
				includePercentEmptyPerYearCheckBox.setSelected(true);
			}
		}
		
		usedToIdentifyDuplicatesCheckBox.setSelected(
			workingCopyDataSetFieldConfiguration.isDuplicateIdentificationField());
		optimiseUsingIndexCheckBox.setSelected(
			workingCopyDataSetFieldConfiguration.optimiseUsingIndex());		
	}
	
	public void updateUI() {
		
		RIFSchemaArea rifSchemaArea
			= originalDataSetConfiguration.getRIFSchemaArea();
		
		String fieldRequirementLevelText
			= (String) fieldRequirementLevelComboBox.getSelectedItem();
		FieldRequirementLevel fieldRequirementLevel
			= FieldRequirementLevel.getValueFromName(fieldRequirementLevelText);
		
		if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
			isRequiredField.setSelected(true);
			isRequiredField.setEnabled(false);
			
			includePercentEmptyCheckBox.setSelected(true);
			includePercentEmptyCheckBox.setEnabled(false);
			
			optimiseUsingIndexCheckBox.setSelected(true);
			optimiseUsingIndexCheckBox.setEnabled(false);

			usedToIdentifyDuplicatesCheckBox.setSelected(true);
			usedToIdentifyDuplicatesCheckBox.setEnabled(false);
			
			
			RIFSchemaAreaPropertyManager rifSchemaAreapropertyManager
				= new RIFSchemaAreaPropertyManager();
			String[] convertFieldNames
				= rifSchemaAreapropertyManager.getRequiredConvertFieldNames(rifSchemaArea);
			DefaultComboBoxModel<String> comboBoxModel
				= (DefaultComboBoxModel<String>) convertComboBox.getModel();
			comboBoxModel.removeAllElements();
			for (String convertFieldName : convertFieldNames) {
				comboBoxModel.addElement(convertFieldName);
			}			
			convertComboBox.setEditable(false);
			
			if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
			   (rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
			
				includePercentEmptyPerYearCheckBox.setSelected(true);
				includePercentEmptyPerYearCheckBox.setEnabled(false);
			}
			
		}
		else {

			isRequiredField.setEnabled(true);			
			includePercentEmptyCheckBox.setEnabled(true);
			optimiseUsingIndexCheckBox.setEnabled(true);
			usedToIdentifyDuplicatesCheckBox.setEnabled(true);
			
			DefaultComboBoxModel<String> comboBoxModel
				= (DefaultComboBoxModel<String>) convertComboBox.getModel();
			comboBoxModel.removeAllElements();
			
			if ( (rifSchemaArea != RIFSchemaArea.HEALTH_NUMERATOR_DATA) &&
			    (rifSchemaArea != RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
				includePercentEmptyPerYearCheckBox.setEnabled(false);
			}
			else {
				includePercentEmptyPerYearCheckBox.setSelected(true);				
			}	
		}		
	}
	
	private void populateWorkingCopyFromForm() {
		//Copy original field values that are not displayed
		workingCopyDataSetFieldConfiguration.setCoreDataSetName(
			originalDataSetFieldConfiguration.getCoreDataSetName());
		workingCopyDataSetFieldConfiguration.setCoreFieldName(
			originalDataSetFieldConfiguration.getCoreFieldName());
		
		//Set Load stage attributes
		workingCopyDataSetFieldConfiguration.setLoadFieldName(
			loadTextField.getText().trim());
		String currentlySelectedFieldPurposeName
			= (String) fieldPurposeComboBox.getSelectedItem();
		FieldPurpose currentFieldPurpose
			= FieldPurpose.getFieldPurposeFromName(currentlySelectedFieldPurposeName);
		workingCopyDataSetFieldConfiguration.setFieldPurpose(currentFieldPurpose);
		String currentlySelectedFieldRequirementLevelName
			= (String) fieldRequirementLevelComboBox.getSelectedItem();
		FieldRequirementLevel currentFieldRequirementLevel
			= FieldRequirementLevel.getValueFromName(currentlySelectedFieldRequirementLevelName);
		workingCopyDataSetFieldConfiguration.setFieldRequirementLevel(currentFieldRequirementLevel);		
		String currentlySelectedRIFDataTypeName
			= (String) rifDataTypeComboBox.getSelectedItem();
		RIFDataTypeFactory dataTypeFactory
			= RIFDataTypeFactory.newInstance();
		AbstractRIFDataType currentlySelectedRIFDataType
			= dataTypeFactory.getDataTypeFromName(currentlySelectedRIFDataTypeName);
		workingCopyDataSetFieldConfiguration.setRIFDataType(currentlySelectedRIFDataType);
		
		
		//Set Clean stage attributes
		workingCopyDataSetFieldConfiguration.setCleanFieldName(
			cleanTextField.getText().trim());			
		workingCopyDataSetFieldConfiguration.setEmptyValueAllowed(
			!isRequiredField.isSelected());
		String currentlySelectedChangeAuditLevelName
			= (String) fieldChangeAuditLevelComboBox.getSelectedItem();
		FieldChangeAuditLevel currentlySelectedChangeAuditLevel
			= FieldChangeAuditLevel.getValueFromName(currentlySelectedChangeAuditLevelName);
		workingCopyDataSetFieldConfiguration.setFieldChangeAuditLevel(currentlySelectedChangeAuditLevel);
		
		//Set Convert stage attributes
		workingCopyDataSetFieldConfiguration.setConvertFieldName(
			(String) convertComboBox.getSelectedItem());
		workingCopyDataSetFieldConfiguration.setCoreFieldDescription(
			descriptionTextField.getText().trim());
		workingCopyDataSetFieldConfiguration.setOptimiseUsingIndex(
			optimiseUsingIndexCheckBox.isSelected());
		
		
		//Set Check stage attributes
		workingCopyDataSetFieldConfiguration.clearCheckOptions();
		if (includePercentEmptyCheckBox.isSelected()) {
			workingCopyDataSetFieldConfiguration.addCheckOption(RIFCheckOption.PERCENT_EMPTY);
		}
		if (includePercentEmptyPerYearCheckBox.isSelected()) {
			workingCopyDataSetFieldConfiguration.addCheckOption(RIFCheckOption.PERCENT_EMPTY_PER_YEAR);
		}
		workingCopyDataSetFieldConfiguration.setDuplicateIdentificationField(
			usedToIdentifyDuplicatesCheckBox.isSelected());
	}
		
	public void saveChanges() 
		throws RIFServiceException {
	
		populateWorkingCopyFromForm();
		//Perform all the validation checks that do not require referencing
		//other fields
		
		workingCopyDataSetFieldConfiguration.checkErrors();

		//Now perform validation checks in the context of the data set configuration
		//as a whole.  We're looking for duplicate field names at stages of load, clean and convert
		
		originalDataSetConfiguration.checkDuplicateFieldNames(
			workingCopyDataSetFieldConfiguration);
		
		//Replace the parent data set's old copy of the field configuration
		//with the one we have scraped from the data entry form	
		DataSetFieldConfiguration.copyInto(
			workingCopyDataSetFieldConfiguration, 
			originalDataSetFieldConfiguration);	
	}
	
	
	public boolean changesMade() {
		return changesMade;
	}
	
	public void resetChangesMade() {
		changesMade = false;
	}
	
	private void updateFormFromRequirementLevelChange() {
		String fieldRequirementLevelText
			= (String) fieldRequirementLevelComboBox.getSelectedItem();
		FieldRequirementLevel fieldRequirementLevel
			= FieldRequirementLevel.getValueFromName(fieldRequirementLevelText);
		if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
			optimiseUsingIndexCheckBox.setSelected(false);			
			optimiseUsingIndexCheckBox.setEnabled(false);
			usedToIdentifyDuplicatesCheckBox.setSelected(true);
			usedToIdentifyDuplicatesCheckBox.setEnabled(false);
			includePercentEmptyCheckBox.setSelected(true);
			includePercentEmptyCheckBox.setEnabled(false);

			RIFSchemaArea rifSchemaArea
				= originalDataSetConfiguration.getRIFSchemaArea();
			if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA ||
				rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA) {
				includePercentEmptyPerYearCheckBox.setSelected(true);
				includePercentEmptyPerYearCheckBox.setEnabled(false);
			}		
		}
		else {
			optimiseUsingIndexCheckBox.setEnabled(true);
			usedToIdentifyDuplicatesCheckBox.setEnabled(true);
			includePercentEmptyCheckBox.setEnabled(true);
			includePercentEmptyPerYearCheckBox.setEnabled(true);		
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: Action Listener
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
		else if (source == fieldRequirementLevelComboBox) {
			updateFormFromRequirementLevelChange();

		}
		
		changesMade = true;
		
	}
	
	public void caretUpdate(CaretEvent event) {
		//KLG: Assume if they update the text position they have made a change
		changesMade = true;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


