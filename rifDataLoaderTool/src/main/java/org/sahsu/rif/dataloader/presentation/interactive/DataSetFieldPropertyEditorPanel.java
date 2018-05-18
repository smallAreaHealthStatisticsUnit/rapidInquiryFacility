package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.sahsu.rif.dataloader.concepts.CheckOption;
import org.sahsu.rif.dataloader.concepts.ConversionFunction;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldChangeAuditLevel;
import org.sahsu.rif.dataloader.concepts.FieldPurpose;
import org.sahsu.rif.dataloader.concepts.FieldRequirementLevel;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.concepts.RIFSchemaAreaPropertyManager;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

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

class DataSetFieldPropertyEditorPanel 
	implements ActionListener,
	CaretListener {
	

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderToolSession session;
	private DataSetConfiguration originalDataSetConfiguration;
	private DataSetFieldConfiguration originalDataSetFieldConfiguration;
	private DataSetFieldConfiguration workingCopyDataSetFieldConfiguration;
	private Geography currentGeography;
	private RIFSchemaArea currentRIFSchemaArea;
	
	private boolean isRenderingForConfigurationHintsFeature;	
	
	private Color EXTRACT_COLOUR = new Color(220, 220, 220);
	private Color CLEAN_COLOUR = new Color(200, 200, 200);
	private Color CONVERT_COLOUR = new Color(180, 180, 180);
	private Color OPTIMISE_COLOUR = new Color(150, 150, 150);
	private Color CHECK_COLOUR = new Color(120, 120, 120);
	
	private JPanel panel;


	private JLabel titleLabel;
	
	private JComboBox<String> rifDataTypeComboBox;
	
	private JTextField coreNameTextField;
	private JTextField loadTextField;
	private JTextField descriptionTextField;
	private JComboBox<String> fieldPurposeComboBox;
	private JComboBox<String> fieldRequirementLevelComboBox;
	private JComboBox<String> cleanComboBox;
	private JComboBox<String> fieldChangeAuditLevelComboBox;
	private JCheckBox isRequiredField;		
	private JComboBox<String> convertComboBox;
	private JTextField conversionFunctionTextField;		
	private JCheckBox includePercentEmptyCheckBox;
	private JCheckBox includePercentEmptyPerYearCheckBox;
	private JCheckBox usedToIdentifyDuplicatesCheckBox;	
	private JCheckBox optimiseUsingIndexCheckBox;
	
	private boolean changesMade;
	
	private String pleaseChooseMessage;

	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetFieldPropertyEditorPanel(
			final JDialog parentDialog,
			final DataLoaderToolSession session,
			final boolean isRenderingForConfigurationHintsFeature) {

		this.session = session;
		this.isRenderingForConfigurationHintsFeature 
			= isRenderingForConfigurationHintsFeature;
		pleaseChooseMessage
			= RIFDataLoaderToolMessages.getMessage("shapeFileEditorPanel.pleaseChoose");

		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		panel = userInterfaceFactory.createPanel();
	
		changesMade = false;
		
		buildUI();
	}

	private void buildUI() {				
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.ipadx = 10;
		panelGC.fill = GridBagConstraints.HORIZONTAL;		
		panelGC.weightx = 1;

		if (isRenderingForConfigurationHintsFeature == false) {
			//Only put a title if we're editing definitions of data set configuration
			//fields, not when we're defining hints.
			String dataSetFieldPanelTitle
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfigurationEditorPanel.title");
			titleLabel = userInterfaceFactory.createHTMLLabel(1, dataSetFieldPanelTitle);
			panel.add(titleLabel, panelGC);
		}

		panelGC.gridy++;
		panel.add(createCoreFieldAttributesPanel(), panelGC);		

		panelGC.gridy++;
		panel.add(userInterfaceFactory.createSeparator(), panelGC);
		panelGC.gridy++;
		panel.add(
			createWorkflowPropertiesPanel(), 
			panelGC);
		enableActionListeners(true);		
	}

	private JPanel createCoreFieldAttributesPanel() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			 = userInterfaceFactory.createGridBagConstraints();
		
		coreNameTextField
			= userInterfaceFactory.createTextField();
		if (isRenderingForConfigurationHintsFeature) {
			String coreFieldNameText
				= RIFDataLoaderToolMessages.getMessage("configurationHint.regularExpressionPattern.label");
			JLabel coreFieldNameLabel
				= userInterfaceFactory.createLabel(coreFieldNameText);
			panel.add(coreFieldNameLabel, panelGC);
			panelGC.gridx++;
			panelGC.weightx = 1;
			panelGC.fill = GridBagConstraints.HORIZONTAL;
			panel.add(coreNameTextField, panelGC);
			panel.setBorder(LineBorder.createGrayLineBorder());
		
			panelGC.gridy++;
		}
		
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
	
	private JPanel createWorkflowPropertiesPanel() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
				
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		panelGC.weightx = 0.3;		
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
		panel.add(loadLabelPanel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 0.7;		
		panel.add(createLoadAttributesPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;		
		panelGC.weightx = 0.3;		
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
		panel.add(cleanLabelPanel, panelGC);
		panelGC.gridx++;
		panelGC.weightx = 0.7;
		panel.add(createCleanAttributesPanel(), panelGC);
				
		panelGC.gridy++;
		
		panelGC.gridx = 0;
		panelGC.weightx = 0.3;		
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
		panel.add(convertLabelPanel, panelGC);
		panelGC.gridx++;
		panelGC.weightx = 0.7;
		panel.add(createConvertAttributesPanel(), panelGC);

		
		panelGC.gridy++;

		panelGC.gridx = 0;
		panelGC.weightx = 0.3;		
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
		panel.add(optimiseLabelPanel, panelGC);
		panelGC.gridx++;
		panelGC.weightx = 0.7;
		panel.add(createOptimiseAttributesPanel(), panelGC);
			
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.weightx = 0.3;		
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
		panel.add(checkLabelPanel, panelGC);
		panelGC.gridx++;
		panelGC.weightx = 0.7;
		panel.add(createCheckAttributesPanel(), panelGC);
		
		return panel;
	}
	
	private JPanel createLoadAttributesPanel() {		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
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
		
		return panel;
	}
	
	private JPanel createCleanAttributesPanel() {		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
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
			= session.getRIFDataTypeFactory();
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
		cleanComboBox
			= userInterfaceFactory.createComboBox();
		cleanComboBox.setEditable(true);
		//cleanComboBox.addCaretListener(this);
		panel.add(cleanComboBox, panelGC);


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
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
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
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
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
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
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
	
	public void setData(
		final DataSetConfiguration originalDataSetConfiguration,
		final Geography currentGeography,
		final DataSetFieldConfiguration originalDataSetFieldConfiguration) {

		this.originalDataSetConfiguration = originalDataSetConfiguration;
		this.currentGeography = currentGeography;
		this.originalDataSetFieldConfiguration = originalDataSetFieldConfiguration;

		workingCopyDataSetFieldConfiguration
			= DataSetFieldConfiguration.createCopy(originalDataSetFieldConfiguration);
		populateForm(workingCopyDataSetFieldConfiguration);
		
		changesMade = false;
	}

	public DataSetFieldConfiguration getData() {
		return originalDataSetFieldConfiguration;
	}
	
	public void setIsEnabled(final boolean isEnabled) {
		coreNameTextField.setEnabled(isEnabled);
		loadTextField.setEnabled(isEnabled);
		rifDataTypeComboBox.setEnabled(isEnabled);
		descriptionTextField.setEnabled(isEnabled);
		fieldPurposeComboBox.setEnabled(isEnabled);
		fieldRequirementLevelComboBox.setEnabled(isEnabled);
		cleanComboBox.setEnabled(isEnabled);
		fieldChangeAuditLevelComboBox.setEnabled(isEnabled);
		isRequiredField.setEnabled(isEnabled);
		convertComboBox.setEnabled(isEnabled);
		conversionFunctionTextField.setEnabled(isEnabled);
		includePercentEmptyCheckBox.setEnabled(isEnabled);
		includePercentEmptyPerYearCheckBox.setEnabled(isEnabled);
		includePercentEmptyPerYearCheckBox.setEnabled(isEnabled);
		usedToIdentifyDuplicatesCheckBox.setEnabled(isEnabled);
		optimiseUsingIndexCheckBox.setEnabled(isEnabled);			
	}
	
	private void populateForm(final DataSetFieldConfiguration dataSetFieldConfiguration) {

		if (dataSetFieldConfiguration == null) {
			setIsEnabled(false);
		}
		else {
			setIsEnabled(true);
			
			//remove any action listeners that may cause things to change
			enableActionListeners(false);

			coreNameTextField.setText(dataSetFieldConfiguration.getCoreFieldName());
			descriptionTextField.setText(dataSetFieldConfiguration.getCoreFieldDescription());

			String fieldName = dataSetFieldConfiguration.getCoreFieldName();
			RIFDataType rifDataType
				= dataSetFieldConfiguration.getRIFDataType();
			rifDataTypeComboBox.setSelectedItem(rifDataType.getName());
			loadTextField.setText(dataSetFieldConfiguration.getLoadFieldName());
		
			FieldPurpose fieldPurpose = dataSetFieldConfiguration.getFieldPurpose();
			if (fieldPurpose != null) {
				fieldPurposeComboBox.setSelectedItem(fieldPurpose.getName());			
			}
			
			FieldRequirementLevel fieldRequirementLevel
			 	= dataSetFieldConfiguration.getFieldRequirementLevel();
			fieldRequirementLevelComboBox.setSelectedItem(fieldRequirementLevel.getName());

			updateCleanFieldName(fieldPurpose);
		
			if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
				isRequiredField.setSelected(true);
			}
			else {
				isRequiredField.setSelected(!dataSetFieldConfiguration.isEmptyValueAllowed());			
			}
			FieldChangeAuditLevel currentFieldChangeAuditLevel
				= dataSetFieldConfiguration.getFieldChangeAuditLevel();
			fieldChangeAuditLevelComboBox.setSelectedItem(currentFieldChangeAuditLevel.getName());

			convertComboBox.setSelectedItem(dataSetFieldConfiguration.getConvertFieldName());
			ConversionFunction rifConversionFunction
				= dataSetFieldConfiguration.getConvertFunction();
			if (rifConversionFunction == null) {
				conversionFunctionTextField.setText("");			
			}
			else {
				conversionFunctionTextField.setText(rifConversionFunction.getFunctionName());			
			}
		
			includePercentEmptyCheckBox.setSelected(false);
			includePercentEmptyPerYearCheckBox.setSelected(false);

			ArrayList<CheckOption> checkOptions
				= dataSetFieldConfiguration.getCheckOptions();
			for (CheckOption checkOption : checkOptions) {
				if (checkOption == CheckOption.PERCENT_EMPTY) {
					includePercentEmptyCheckBox.setSelected(true);
				}
				else if (checkOption == CheckOption.PERCENT_EMPTY_PER_YEAR) {
					includePercentEmptyPerYearCheckBox.setSelected(true);
				}
			}
		
			usedToIdentifyDuplicatesCheckBox.setSelected(
				dataSetFieldConfiguration.isDuplicateIdentificationField());
			optimiseUsingIndexCheckBox.setSelected(
				dataSetFieldConfiguration.optimiseUsingIndex());		
			
			enableActionListeners(true);
		}
	}
	
	private void enableActionListeners(final boolean actionListenersEnabled) {
		if (actionListenersEnabled) {
			fieldPurposeComboBox.addActionListener(this);
			fieldRequirementLevelComboBox.addActionListener(this);
			rifDataTypeComboBox.addActionListener(this);
			fieldChangeAuditLevelComboBox.addActionListener(this);
			cleanComboBox.addActionListener(this);
			convertComboBox.addActionListener(this);	
			optimiseUsingIndexCheckBox.addActionListener(this);
			includePercentEmptyCheckBox.addActionListener(this);
			includePercentEmptyCheckBox.addActionListener(this);
			includePercentEmptyPerYearCheckBox.addActionListener(this);
			usedToIdentifyDuplicatesCheckBox.addActionListener(this);
		}
		else {
			fieldPurposeComboBox.removeActionListener(this);
			fieldRequirementLevelComboBox.removeActionListener(this);
			rifDataTypeComboBox.removeActionListener(this);
			fieldChangeAuditLevelComboBox.removeActionListener(this);
			cleanComboBox.removeActionListener(this);
			convertComboBox.removeActionListener(this);	
			optimiseUsingIndexCheckBox.removeActionListener(this);
			includePercentEmptyCheckBox.removeActionListener(this);
			includePercentEmptyCheckBox.removeActionListener(this);
			includePercentEmptyPerYearCheckBox.removeActionListener(this);
			usedToIdentifyDuplicatesCheckBox.removeActionListener(this);
		}
		
	}
	
	private void updateCleanFieldName(final FieldPurpose fieldPurpose) {
		//Setting the clean field values
		String cleanField
			= workingCopyDataSetFieldConfiguration.getCleanFieldName();

		if (fieldPurpose == FieldPurpose.GEOGRAPHICAL_RESOLUTION) {
			//restrict values to a combo box of fields to the names of geographical resolution
			//fields
			cleanComboBox.setEditable(false);

			DataLoaderToolConfiguration dataLoaderToolConfiguration
				= session.getDataLoaderToolConfiguration();
			
			ArrayList<String> geographicalResolutionFieldNames
				= currentGeography.getLevelCodeNames();
							
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();		
			if (fieldValidationUtility.isEmpty(cleanField)) {
				//if no value has been chosen prompt them to make a choice
				
				geographicalResolutionFieldNames.add(0, pleaseChooseMessage);				
				DefaultComboBoxModel<String> defaultComboBoxModel
					= new DefaultComboBoxModel<String>(geographicalResolutionFieldNames.toArray(new String[0]));
				cleanComboBox.setModel(defaultComboBoxModel);
				convertComboBox.setSelectedItem(pleaseChooseMessage);
			}
			else {
				DefaultComboBoxModel<String> defaultComboBoxModel
					= new DefaultComboBoxModel<String>(geographicalResolutionFieldNames.toArray(new String[0]));
				cleanComboBox.setModel(defaultComboBoxModel);
				cleanComboBox.setSelectedItem(cleanField);
				convertComboBox.setSelectedItem(cleanField);
			}	
		}
		else if (fieldPurpose == FieldPurpose.TOTAL_COUNT) {
			//if it is meant to be the total field, then there is no choice
			//in the clean and convert field names - they should be total as well
			cleanComboBox.setSelectedItem("total");
			convertComboBox.setSelectedItem("total");
			cleanComboBox.setEditable(false);
			convertComboBox.setEditable(false);
		}
		else {
			//let user specify whatever clean field name they want
			cleanComboBox.setEditable(true);
			convertComboBox.setEditable(true);
				
			DefaultComboBoxModel<String> defaultComboBoxModel
				= new DefaultComboBoxModel<String>(new String[0]);
			cleanComboBox.setModel(defaultComboBoxModel);
			cleanComboBox.setSelectedItem(cleanField);
			convertComboBox.setSelectedItem(cleanField);			
		}
	}

	public void setCurrentRIFSchemaArea(final RIFSchemaArea currentRIFSchemaArea) {
		this.currentRIFSchemaArea = currentRIFSchemaArea;
		updateUI();
	}
		
	public void setCurrentGeography(final Geography currentGeography) {
		this.currentGeography = currentGeography;
		
		FieldPurpose fieldPurpose
			= workingCopyDataSetFieldConfiguration.getFieldPurpose();
		System.out.println("Updating current geography field purpose=="+ fieldPurpose.getName()+"==");
		if (fieldPurpose == FieldPurpose.GEOGRAPHICAL_RESOLUTION) {
			ArrayList<String> geographicalResolutionFieldNames
				= currentGeography.getLevelNames();
			geographicalResolutionFieldNames.add(0, pleaseChooseMessage);				
			DefaultComboBoxModel<String> defaultComboBoxModel
				= new DefaultComboBoxModel<String>(geographicalResolutionFieldNames.toArray(new String[0]));
			cleanComboBox.setModel(defaultComboBoxModel);
			cleanComboBox.setSelectedItem(pleaseChooseMessage);
			cleanComboBox.updateUI();
		}
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
	
	public DataSetFieldConfiguration getDataSetFieldConfigurationFromForm() {
		if (originalDataSetFieldConfiguration == null) {
			return null;
		}
		
		DataSetFieldConfiguration dataSetFieldConfigurationFromForm
			= DataSetFieldConfiguration.createCopy(originalDataSetFieldConfiguration);

		//Copy original field values that are not displayed
		dataSetFieldConfigurationFromForm.setCoreDataSetName(
			originalDataSetFieldConfiguration.getCoreDataSetName());
		
		if (isRenderingForConfigurationHintsFeature) {
			dataSetFieldConfigurationFromForm.setCoreFieldName(
				coreNameTextField.getText().trim());
		}
		else {
			dataSetFieldConfigurationFromForm.setCoreFieldName(
				originalDataSetFieldConfiguration.getCoreFieldName());			
		}
		
		//Set Load stage attributes
		dataSetFieldConfigurationFromForm.setLoadFieldName(
			loadTextField.getText().trim());
		String currentlySelectedFieldPurposeName
			= (String) fieldPurposeComboBox.getSelectedItem();
		FieldPurpose currentFieldPurpose
			= FieldPurpose.getFieldPurposeFromName(currentlySelectedFieldPurposeName);
		dataSetFieldConfigurationFromForm.setFieldPurpose(currentFieldPurpose);
		String currentlySelectedFieldRequirementLevelName
			= (String) fieldRequirementLevelComboBox.getSelectedItem();
		FieldRequirementLevel currentFieldRequirementLevel
			= FieldRequirementLevel.getValueFromName(currentlySelectedFieldRequirementLevelName);
		dataSetFieldConfigurationFromForm.setFieldRequirementLevel(currentFieldRequirementLevel);		
		String currentlySelectedRIFDataTypeName
			= (String) rifDataTypeComboBox.getSelectedItem();
		RIFDataTypeFactory dataTypeFactory
			= session.getRIFDataTypeFactory();
		RIFDataType currentlySelectedRIFDataType
			= dataTypeFactory.getDataTypeFromName(currentlySelectedRIFDataTypeName);
		dataSetFieldConfigurationFromForm.setRIFDataType(currentlySelectedRIFDataType);
				
		//Set Clean stage attributes
		String selectedCleanFieldItem
			= (String) cleanComboBox.getSelectedItem();
		if (selectedCleanFieldItem == null) {
			dataSetFieldConfigurationFromForm.setCleanFieldName("");
		}
		else {
			dataSetFieldConfigurationFromForm.setCleanFieldName(
				selectedCleanFieldItem.trim());			
		}
		dataSetFieldConfigurationFromForm.setEmptyValueAllowed(
			!isRequiredField.isSelected());
		String currentlySelectedChangeAuditLevelName
			= (String) fieldChangeAuditLevelComboBox.getSelectedItem();
		FieldChangeAuditLevel currentlySelectedChangeAuditLevel
			= FieldChangeAuditLevel.getValueFromName(currentlySelectedChangeAuditLevelName);
		dataSetFieldConfigurationFromForm.setFieldChangeAuditLevel(currentlySelectedChangeAuditLevel);
		
		//Set Convert stage attributes
		dataSetFieldConfigurationFromForm.setConvertFieldName(
			(String) convertComboBox.getSelectedItem());
		dataSetFieldConfigurationFromForm.setCoreFieldDescription(
			descriptionTextField.getText().trim());
		dataSetFieldConfigurationFromForm.setOptimiseUsingIndex(
			optimiseUsingIndexCheckBox.isSelected());
		
		
		//Set Check stage attributes
		dataSetFieldConfigurationFromForm.clearCheckOptions();
		if (includePercentEmptyCheckBox.isSelected()) {
			dataSetFieldConfigurationFromForm.addCheckOption(CheckOption.PERCENT_EMPTY);
		}
		if (includePercentEmptyPerYearCheckBox.isSelected()) {
			dataSetFieldConfigurationFromForm.addCheckOption(CheckOption.PERCENT_EMPTY_PER_YEAR);
		}
		dataSetFieldConfigurationFromForm.setDuplicateIdentificationField(
			usedToIdentifyDuplicatesCheckBox.isSelected());
		
		return dataSetFieldConfigurationFromForm;
	}
		
	public boolean saveChanges() {
		if (originalDataSetFieldConfiguration == null) {
			return false;
		}
		
		DataSetFieldConfiguration dataSetFieldConfigurationFromForm
			= getDataSetFieldConfigurationFromForm();

		//Now perform validation checks in the context of the data set configuration
		//as a whole.  We're looking for duplicate field names at stages of load, clean and convert
		boolean saveChanges
			= !originalDataSetFieldConfiguration.hasIdenticalContents(dataSetFieldConfigurationFromForm);
		
		//Replace the parent data set's old copy of the field configuration
		//with the one we have scraped from the data entry form	
		
		DataSetFieldConfiguration.copyInto(
			dataSetFieldConfigurationFromForm, 
			originalDataSetFieldConfiguration);			
		return saveChanges;
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
			if (rifSchemaArea != null) {				
				if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA ||
					rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA) {
					includePercentEmptyPerYearCheckBox.setSelected(true);
					includePercentEmptyPerYearCheckBox.setEnabled(false);
				}		
			}

		}
		else {
			if (fieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD) {
				//suggest the minimal auditing for an extra field
				fieldChangeAuditLevelComboBox.setSelectedItem(
					FieldChangeAuditLevel.INCLUDE_FIELD_NAME_ONLY);
				optimiseUsingIndexCheckBox.setEnabled(true);
				usedToIdentifyDuplicatesCheckBox.setEnabled(true);
				includePercentEmptyCheckBox.setEnabled(true);
				includePercentEmptyPerYearCheckBox.setEnabled(true);				
			}
			else {
				//ignore field
				fieldChangeAuditLevelComboBox.setSelectedItem(
					FieldChangeAuditLevel.NONE);
				optimiseUsingIndexCheckBox.setEnabled(false);
				usedToIdentifyDuplicatesCheckBox.setEnabled(false);
				includePercentEmptyCheckBox.setEnabled(false);
				includePercentEmptyPerYearCheckBox.setEnabled(false);				
			}
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void validateForm() 
		throws RIFServiceException {
		
		if (isRenderingForConfigurationHintsFeature == false) {
			/**
			 * We won't bother validating hints.  We just want users to define
			 * bits and pieces of them so that their properties can be used to
			 * set the properties of CSV fields.  If any properties are set wrong
			 * they will appear when user starts to check the properties of individual
			 * fields in the Data Set Editor
			 */
			DataSetFieldConfiguration dataSetFieldConfigurationFromForm
				= getDataSetFieldConfigurationFromForm();		
			dataSetFieldConfigurationFromForm.checkErrors();
			originalDataSetConfiguration.checkDuplicateFieldNames(
				dataSetFieldConfigurationFromForm);
		}
	}
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: Action Listener
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == rifDataTypeComboBox) {
			String rifDataTypeName
				= (String) rifDataTypeComboBox.getSelectedItem();
			//RIFDataTypeFactory rifDataTypeFactory
			//	= RIFDataTypeFactory.newInstance();
			//rifDataTypeFactory.getDataType(dataTypeName);
			
			//conversionFunctionTextField.setText(t);
		}
		else if (source == fieldRequirementLevelComboBox) {
			updateFormFromRequirementLevelChange();

		}
		else if (source == fieldPurposeComboBox) {
			String currentFieldPurposeText
				= (String) fieldPurposeComboBox.getSelectedItem();
			FieldPurpose currentFieldPurpose
				= FieldPurpose.getFieldPurposeFromName(currentFieldPurposeText);
			updateCleanFieldName(currentFieldPurpose);
		}
		else if (source == cleanComboBox) {
			String currentCleanFieldValue
				= (String) cleanComboBox.getSelectedItem();
			convertComboBox.setSelectedItem(currentCleanFieldValue);
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


