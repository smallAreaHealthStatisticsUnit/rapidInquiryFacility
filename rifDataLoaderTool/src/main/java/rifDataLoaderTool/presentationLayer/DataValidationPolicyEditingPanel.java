package rifDataLoaderTool.presentationLayer;



import rifDataLoaderTool.businessConceptLayer.RIFFieldValidationPolicy;
import rifDataLoaderTool.businessConceptLayer.ValidationRule;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.CustomRIFDataType;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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

public class DataValidationPolicyEditingPanel 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private CustomRIFDataType currentRIFDataType;
	
	private JPanel panel;
	//private CustomRIFDataType customRIFDataType;
	private UserInterfaceFactory userInterfaceFactory;
	private ButtonGroup validationPolicyButtonGroup;
		
	private JRadioButton useFunctionRadioButton;
	private JRadioButton useRulesRadioButton;
	private JRadioButton useSQLFragmentRadioButton;
	private JRadioButton doNothingRadioButton;
	
	private OrderedListPanel rulesListPanel;
	private ListEditingButtonPanel rulesListButtonPanel;
	private JComboBox databaseFunctionsComboBox;
	private JTextArea databaseFunctionDescriptionTextArea;
	
	private JTextField sqlQueryFragmentTextField;

	private boolean isEditable;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataValidationPolicyEditingPanel(final UserInterfaceFactory userInterfaceFactory) {
		this.userInterfaceFactory = userInterfaceFactory;
		panel 
			= userInterfaceFactory.createPanel();
		
		rulesListPanel
			= new OrderedListPanel(
				null,
				"",
				userInterfaceFactory,
				false);	
		rulesListButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
				rulesListButtonPanel.includeAddButton("");
				rulesListButtonPanel.includeEditButton("");
				rulesListButtonPanel.includeCopyButton("");
				rulesListButtonPanel.includeDeleteButton("");

		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;

		String dataTypeCleaningPolicyLabelText
			= RIFDataLoaderToolMessages.getMessage("rifFieldValidationPolicy.label");
		JLabel dataTypeCleaningPolicyLabel
			= userInterfaceFactory.createLabel(dataTypeCleaningPolicyLabelText);
		userInterfaceFactory.setBoldFont(dataTypeCleaningPolicyLabel);
		panel.add(dataTypeCleaningPolicyLabel, panelGC);

		panelGC.gridy++;		
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("dataTypeEditingPanel.validationPolicy.instructions");
		JLabel instructionsLabel
			= userInterfaceFactory.createLabel(instructionsText);
		panel.add(instructionsLabel, panelGC);

		validationPolicyButtonGroup 
			= userInterfaceFactory.createButtonGroup();
	
		panelGC.gridy++;		
		doNothingRadioButton
			= userInterfaceFactory.createRadioButton(
				RIFFieldValidationPolicy.NO_VALIDATION.getName());
		validationPolicyButtonGroup.add(doNothingRadioButton);
		doNothingRadioButton.addActionListener(this);
		panel.add(doNothingRadioButton, panelGC);
	
		panelGC.gridy++;	
		useRulesRadioButton
			= userInterfaceFactory.createRadioButton(
				RIFFieldValidationPolicy.VALIDATION_RULES.getName());
		useRulesRadioButton.addActionListener(this);		
		validationPolicyButtonGroup.add(useRulesRadioButton);
		panel.add(useRulesRadioButton, panelGC);
		
		panelGC.gridy++;	
		panelGC.fill = GridBagConstraints.BOTH;		
		panelGC.weighty = 1;		
		panel.add(createCleaningRuleListEditingPanel(), panelGC);

		panelGC.gridy++;	
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(createValidationFunctionFeaturePanel(), panelGC);
	
		panelGC.gridy++;	
		useSQLFragmentRadioButton
			= userInterfaceFactory.createRadioButton(
				RIFFieldValidationPolicy.SQL_FRAGMENT.getName());
		useSQLFragmentRadioButton.addActionListener(this);			
		validationPolicyButtonGroup.add(useSQLFragmentRadioButton);
		panel.add(createSQLFragmentFeaturePanel(), panelGC);

		panel.setBorder(LineBorder.createGrayLineBorder());
	}

	private JPanel createCleaningRuleListEditingPanel() {
		JPanel panel 
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
				
		rulesListPanel
			= new OrderedListPanel(
				null,
				"",
				userInterfaceFactory,
				false);		
		panel.add(
			rulesListPanel.getPanel(), 
			panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(rulesListButtonPanel.getPanel(), panelGC);

		return panel;
	}
	
	private JPanel createValidationFunctionFeaturePanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();		

		JPanel upperPanel = userInterfaceFactory.createPanel();
		GridBagConstraints upperPanelGC
			= userInterfaceFactory.createGridBagConstraints();		
		useFunctionRadioButton
			= userInterfaceFactory.createRadioButton(
				RIFFieldValidationPolicy.VALIDATION_FUNCTION.getName());
		validationPolicyButtonGroup.add(useFunctionRadioButton);
		useFunctionRadioButton.addActionListener(this);	
		upperPanel.add(useFunctionRadioButton, upperPanelGC);
		
		upperPanelGC.gridx++;
		upperPanelGC.fill = GridBagConstraints.HORIZONTAL;
		upperPanelGC.weightx = 1;
		String[] validationFunctionNames = new String[3];
		validationFunctionNames[0] = "is_uk_post_code_valid";
		validationFunctionNames[1] = "is_icd10_code_valid";
		validationFunctionNames[2] = "is_sex_valid";
		databaseFunctionsComboBox
			= userInterfaceFactory.createComboBox(validationFunctionNames);
		databaseFunctionsComboBox.addActionListener(this);
		upperPanel.add(databaseFunctionsComboBox, upperPanelGC);		
		
		panel.add(upperPanel, panelGC);
		
		panelGC.gridy++;
		String functionDescriptionLabelText
			= RIFDataLoaderToolMessages.getMessage("dataTypeEditingPanel.functionDescription");
		JLabel functionDescriptionLabel
			= userInterfaceFactory.createLabel(functionDescriptionLabelText);
		panel.add(functionDescriptionLabel, panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		databaseFunctionDescriptionTextArea
			= userInterfaceFactory.createNonEditableTextArea(2, 30);
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(databaseFunctionDescriptionTextArea);
		panel.add(scrollPane, panelGC);
		
		return panel;
	}
	
	private JPanel createSQLFragmentFeaturePanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();		
		
		useSQLFragmentRadioButton
			= userInterfaceFactory.createRadioButton(
				RIFFieldValidationPolicy.SQL_FRAGMENT.getName());
		useSQLFragmentRadioButton.addActionListener(this);			
		validationPolicyButtonGroup.add(useSQLFragmentRadioButton);
		panel.add(useSQLFragmentRadioButton, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		JPanel lowerPanel = userInterfaceFactory.createPanel();
		GridBagConstraints lowerPanelGC
			= userInterfaceFactory.createGridBagConstraints();		
		
		String fragmentLabelText
			= RIFDataLoaderToolMessages.getMessage("dataTypeEditingPanel.sqlFragment.label");
		JLabel fragmentLabel
			= userInterfaceFactory.createLabel(fragmentLabelText);
		lowerPanel.add(fragmentLabel, lowerPanelGC);
		
		lowerPanelGC.gridx++;
		lowerPanelGC.fill = GridBagConstraints.HORIZONTAL;
		lowerPanelGC.weightx = 1;
		sqlQueryFragmentTextField = userInterfaceFactory.createTextField();
		lowerPanel.add(sqlQueryFragmentTextField, lowerPanelGC);
		panel.add(lowerPanel, panelGC);

		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public JPanel getPanel() {
		return panel;
	}
	
	public void setData(
		final CustomRIFDataType originalRIFDataType,
		final boolean isEditable) {
		
		this.currentRIFDataType = originalRIFDataType;
		this.isEditable = isEditable;
		populateForm();
	}
	
	private void populateForm() {
		
		useFunctionRadioButton.removeActionListener(this);
		useRulesRadioButton.removeActionListener(this);
		useSQLFragmentRadioButton.removeActionListener(this);
		doNothingRadioButton.removeActionListener(this);		
		
		RIFFieldValidationPolicy fieldValidationPolicy
			= currentRIFDataType.getFieldValidationPolicy();
		if (fieldValidationPolicy == RIFFieldValidationPolicy.NO_VALIDATION) {
			doNothingRadioButton.setSelected(true);			
			doNothing();
		}
		else if (fieldValidationPolicy == RIFFieldValidationPolicy.VALIDATION_RULES) {
			useRulesRadioButton.setSelected(true);		
			rulesListPanel.clearList();
			ArrayList<ValidationRule> validationRules
				= currentRIFDataType.getValidationRules();
			for (ValidationRule validationRule : validationRules) {
				rulesListPanel.addListItem(validationRule);		
			}
			rulesListPanel.updateUI();
		}
		else if (fieldValidationPolicy == RIFFieldValidationPolicy.VALIDATION_FUNCTION) {
			useFunctionRadioButton.setSelected(true);			
			databaseFunctionsComboBox.setSelectedItem(
				currentRIFDataType.getValidationFunctionName());
			validatingUseFunction();
		}
		else if (fieldValidationPolicy == RIFFieldValidationPolicy.SQL_FRAGMENT) {
			useSQLFragmentRadioButton.setSelected(true);
			validatingUseSQLFragment();			
		}
		
		doNothingRadioButton.addActionListener(this);
		useRulesRadioButton.addActionListener(this);
		useFunctionRadioButton.addActionListener(this);
		useSQLFragmentRadioButton.addActionListener(this);
		
		//determine if buttons should be enabled or disabled
		useFunctionRadioButton.setEnabled(isEditable);
		useRulesRadioButton.setEnabled(isEditable);
		useSQLFragmentRadioButton.setEnabled(isEditable);
		doNothingRadioButton.setEnabled(isEditable);		
		databaseFunctionsComboBox.setEnabled(isEditable);
		sqlQueryFragmentTextField.setEnabled(isEditable);
		if (isEditable) {
			if (rulesListPanel.isEmpty()) {
				rulesListButtonPanel.indicateEmptyState();
			}
			else {
				rulesListButtonPanel.indicatePopulatedState();				
			}			
		}
		else {
			rulesListButtonPanel.indicateViewOnlyState();
		}			
		
	}
	
	public void saveChanges() {
		
	}
	
	private void doNothing() {	
		setEnableValidationFunctionFeature(false);
		setEnableRulesFeature(false);
		setEnableSQLFragmentFeature(false);
	}

	private void useRules() {		
		setEnableValidationFunctionFeature(false);
		setEnableRulesFeature(true);
		setEnableSQLFragmentFeature(false);
	}
	
	private void validatingUseFunction() {
		
		setEnableValidationFunctionFeature(true);
		setEnableRulesFeature(false);
		setEnableSQLFragmentFeature(false);
	}

	private void validatingUseSQLFragment() {	

		setEnableValidationFunctionFeature(false);
		setEnableRulesFeature(false);
		setEnableSQLFragmentFeature(true);		
	}
	
	private void addValidationRule() {
		
	}
	
	private void editValidationRule() {
		
	}

	private void copyValidationRule() {
		
	}
	
	private void deleteValidationRule() {
		
	}
	
	private void updateValidationFunctionDescription() {
		String currentlySelectedCleaningFunctionName
			= (String) databaseFunctionsComboBox.getSelectedItem();
		databaseFunctionDescriptionTextArea.setText(
			"Description of " + currentlySelectedCleaningFunctionName);		
	}

	private void setEnableValidationFunctionFeature(
		final boolean isEnabled) {
		
		databaseFunctionsComboBox.setEnabled(isEnabled);
		if (isEnabled == false) {
			databaseFunctionsComboBox.removeActionListener(this);
			databaseFunctionsComboBox.setSelectedItem(0);
			databaseFunctionsComboBox.addActionListener(this);
			databaseFunctionDescriptionTextArea.setText("");
		}
		else {
			
		}
	}
	
	private void setEnableRulesFeature(
		final boolean isEnabled) {

		if (isEnabled) {
			rulesListButtonPanel.indicateEmptyState();		
		}
		else {
			rulesListPanel.clearList();
			rulesListButtonPanel.disableAllButtons();
			rulesListPanel.updateUI();
		}		
	}
	
	private void setEnableSQLFragmentFeature(
		final boolean isEnabled) {
		
		sqlQueryFragmentTextField.setEnabled(isEnabled);
		if (isEnabled == false) {
			sqlQueryFragmentTextField.setText("");
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
		
		if (button == doNothingRadioButton) {
			doNothing();
		}
		else if (button == useRulesRadioButton) {
			useRules();
		}
		else if (button == useFunctionRadioButton) {
			validatingUseFunction();
		}
		else if (button == useSQLFragmentRadioButton) {
			validatingUseSQLFragment();
		}
		else if (rulesListButtonPanel.isAddButton(button)) {
			addValidationRule();
		}
		else if (rulesListButtonPanel.isEditButton(button)) {
			editValidationRule();
		}
		else if (rulesListButtonPanel.isCopyButton(button)) {
			copyValidationRule();
		}
		else if (rulesListButtonPanel.isDeleteButton(button)) {
			deleteValidationRule();
		}
		else if (button == databaseFunctionsComboBox) {
			updateValidationFunctionDescription();
		}
		
		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


