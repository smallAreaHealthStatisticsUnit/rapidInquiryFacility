package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.RIFFieldValidationPolicy;


import rifDataLoaderTool.businessConceptLayer.ValidationRule;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;

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
	
	private JPanel panel;
	
	private UserInterfaceFactory userInterfaceFactory;
	private ButtonGroup validationPolicyButtonGroup;
	
	private JRadioButton useFunctionRadioButton;
	private JRadioButton useRulesRadioButton;
	private JRadioButton doNothingRadioButton;
	
	private OrderedListPanel rulesListPanel;
	private ListEditingButtonPanel rulesListButtonPanel;
	private JComboBox databaseFunctionsComboBox;
	private JTextArea databaseFunctionDescriptionTextArea;
	
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
		rulesListButtonPanel.addActionListener(this);

		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;

		String dataTypeValidationPolicyLabelText
			= RIFDataLoaderToolMessages.getMessage("rifFieldValidationPolicy.label");
		JLabel dataTypeValidationPolicyLabel
			= userInterfaceFactory.createLabel(dataTypeValidationPolicyLabelText);
		userInterfaceFactory.setBoldFont(dataTypeValidationPolicyLabel);
		panel.add(dataTypeValidationPolicyLabel, panelGC);

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
		panel.add(createValidationRuleListEditingPanel(), panelGC);

		panelGC.gridy++;	
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(createValidationFunctionFeaturePanel(), panelGC);
	
		panel.setBorder(LineBorder.createGrayLineBorder());
	}
	
	private JPanel createValidationRuleListEditingPanel() {
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
		String[] validationFunctionNames = new String[4];
		validationFunctionNames[0] = "is_valid_uk_postal_code";
		validationFunctionNames[1] = "is_valid_icd10_code";
		validationFunctionNames[2] = "is_valid_sex";
		validationFunctionNames[3] = "is_valid_age";		
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
		
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public JPanel getPanel() {
		return panel;
	}
	
	public RIFFieldValidationPolicy getFieldValidationPolicy() {
		if (useRulesRadioButton.isSelected()) {
			System.out.println("DataValidationPolicyEditingPanel validation== validation rules");
			return RIFFieldValidationPolicy.VALIDATION_RULES;
		}
		else if (useFunctionRadioButton.isSelected()) {
			System.out.println("DataValidationPolicyEditingPanel validation== validation function");
			return RIFFieldValidationPolicy.VALIDATION_FUNCTION;
		}		
		else {
			System.out.println("DataValidationPolicyEditingPanel validation== no validation");
			return RIFFieldValidationPolicy.NO_VALIDATION;
		}	
	}
		
	public void setNoValidationPolicy() {
		removeActionListeners();		
		doNothingRadioButton.setSelected(true);	
		doNothing();
		addActionListeners();
	}
	
	public void setValidationRulesPolicy(final ArrayList<ValidationRule> validationRules) {
		removeActionListeners();
		useRulesRadioButton.setSelected(true);
		
		rulesListPanel.clearList();
		for (ValidationRule validationRule : validationRules) {
			rulesListPanel.addListItem(validationRule);
		}
		if (rulesListPanel.isEmpty() == false) {
			rulesListPanel.selectFirstItem();
		}		
		rulesListPanel.updateUI();		

		useRules();
		addActionListeners();
	}
	
	public ArrayList<ValidationRule> getValidationRules() {
		ArrayList<ValidationRule> validationRules = new ArrayList<ValidationRule>();
		ArrayList<DisplayableListItemInterface> listItems
			= rulesListPanel.getAllItems();
		for (DisplayableListItemInterface listItem : listItems) {
			validationRules.add((ValidationRule) listItem);
		}
		return validationRules;
	}
	
	public void setValidationFunctionPolicy(final String validationFunctionName) {
		removeActionListeners();	
		useFunctionRadioButton.setSelected(true);
		databaseFunctionsComboBox.setSelectedItem(validationFunctionName);
		validationUseFunction();
		addActionListeners();
	}
		
	public String getValidationFunctionName() {
		return (String) databaseFunctionsComboBox.getSelectedItem();
	}
	
	private void addActionListeners() {
		useFunctionRadioButton.removeActionListener(this);
		useRulesRadioButton.removeActionListener(this);
		doNothingRadioButton.removeActionListener(this);
	}
	
	private void removeActionListeners() {
		useFunctionRadioButton.addActionListener(this);
		useRulesRadioButton.addActionListener(this);
		doNothingRadioButton.addActionListener(this);
	}
		
	private void doNothing() {	
		setEnableValidationFunctionFeature(false);
		setEnableRulesFeature(false);
	}

	private void useRules() {		
		setEnableValidationFunctionFeature(false);
		setEnableRulesFeature(true);
	}
	
	private void validationUseFunction() {
		
		setEnableValidationFunctionFeature(true);
		setEnableRulesFeature(false);
	}
	
	private void addValidationRule() {
		ValidationRuleEditorDialog validationEditorDialog
			= new ValidationRuleEditorDialog(userInterfaceFactory);
		ValidationRule validationRule
			= ValidationRule.newInstance();
		validationEditorDialog.setData(validationRule, true);
		validationEditorDialog.show();
		if (validationEditorDialog.isCancelled()) {
			return;
		}

		rulesListPanel.addListItem(validationEditorDialog.getData());
	}
	
	private void editValidationRule() {
		ValidationRule selectedValidationRule
			= (ValidationRule) rulesListPanel.getSelectedItem();
		ValidationRuleEditorDialog validationRuleEditorDialog
			= new ValidationRuleEditorDialog(userInterfaceFactory);
		validationRuleEditorDialog.setData(selectedValidationRule, true);
		validationRuleEditorDialog.show();
		
	}

	private void copyValidationRule() {
		ValidationRule selectedValidationRule
			= (ValidationRule) rulesListPanel.getSelectedItem();
		ValidationRule cloneValidationRule
			= ValidationRule.createCopy(selectedValidationRule);
		String currentValidationRuleName
			= selectedValidationRule.getName();
		cloneValidationRule.setName("Copy of " + currentValidationRuleName);
		ValidationRuleEditorDialog validationRuleEditorDialog
			= new ValidationRuleEditorDialog(userInterfaceFactory);
		validationRuleEditorDialog.setData(selectedValidationRule, true);
		validationRuleEditorDialog.show();
		
	}
	
	private void deleteValidationRule() {
		rulesListPanel.deleteSelectedListItems();
	}
	
	private void updateValidationFunctionDescription() {
		String currentlySelectedValidationFunctionName
			= (String) databaseFunctionsComboBox.getSelectedItem();
		databaseFunctionDescriptionTextArea.setText(
			"Description of " + currentlySelectedValidationFunctionName);		
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

		rulesListPanel.setEnabled(isEnabled);		
		if (isEnabled) {
			if (rulesListPanel.isEmpty()) {
				rulesListButtonPanel.indicateEmptyState();				
			}
			else {
				rulesListButtonPanel.indicatePopulatedState();
			}
		}
		else {
			rulesListPanel.clearList();
			rulesListButtonPanel.disableAllButtons();	
			rulesListPanel.updateUI();
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
			validationUseFunction();
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


