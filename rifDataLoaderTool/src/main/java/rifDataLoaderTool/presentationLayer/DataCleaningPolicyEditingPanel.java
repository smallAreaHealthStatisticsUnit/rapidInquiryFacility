package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.RIFFieldCleaningPolicy;
import rifDataLoaderTool.businessConceptLayer.CleaningRule;
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

public class DataCleaningPolicyEditingPanel 
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

	public DataCleaningPolicyEditingPanel(final UserInterfaceFactory userInterfaceFactory) {
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

		String dataTypeCleaningPolicyLabelText
			= RIFDataLoaderToolMessages.getMessage("rifFieldCleaningPolicy.label");
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
				RIFFieldCleaningPolicy.NO_CLEANING.getName());
		validationPolicyButtonGroup.add(doNothingRadioButton);
		doNothingRadioButton.addActionListener(this);
		panel.add(doNothingRadioButton, panelGC);
	
		panelGC.gridy++;	
		useRulesRadioButton
			= userInterfaceFactory.createRadioButton(
				RIFFieldCleaningPolicy.CLEANING_RULES.getName());
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
				RIFFieldCleaningPolicy.CLEANING_FUNCTION.getName());
		validationPolicyButtonGroup.add(useFunctionRadioButton);
		useFunctionRadioButton.addActionListener(this);	
		upperPanel.add(useFunctionRadioButton, upperPanelGC);
		
		upperPanelGC.gridx++;
		upperPanelGC.fill = GridBagConstraints.HORIZONTAL;
		upperPanelGC.weightx = 1;
		String[] validationFunctionNames = new String[4];
		validationFunctionNames[0] = "clean_uk_postal_code";
		validationFunctionNames[1] = "clean_icd_code";
		validationFunctionNames[2] = "clean_sex";
		validationFunctionNames[3] = "clean_age";
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
	
	public RIFFieldCleaningPolicy getFieldCleaningPolicy() {
		if (useRulesRadioButton.isSelected()) {
			return RIFFieldCleaningPolicy.CLEANING_RULES;
		}
		else if (useFunctionRadioButton.isSelected()) {
			return RIFFieldCleaningPolicy.CLEANING_FUNCTION;
		}		
		else {
			return RIFFieldCleaningPolicy.NO_CLEANING;
		}	
	}
		
	public void setNoCleaningPolicy() {
		removeActionListeners();		
		doNothingRadioButton.setSelected(true);	
		doNothing();
		addActionListeners();
	}
	
	public void setCleaningRulesPolicy(final ArrayList<CleaningRule> cleaningRules) {
		removeActionListeners();
		useRulesRadioButton.setSelected(true);
		
		rulesListPanel.clearList();
		for (CleaningRule cleaningRule : cleaningRules) {
			rulesListPanel.addListItem(cleaningRule);
		}
		if (rulesListPanel.isEmpty() == false) {
			rulesListPanel.selectFirstItem();
		}
		rulesListPanel.updateUI();		

		useRules();
		addActionListeners();
	}
	
	public ArrayList<CleaningRule> getCleaningRules() {
		ArrayList<CleaningRule> cleaningRules = new ArrayList<CleaningRule>();
		ArrayList<DisplayableListItemInterface> listItems
			= rulesListPanel.getAllItems();
		for (DisplayableListItemInterface listItem : listItems) {
			cleaningRules.add((CleaningRule) listItem);
		}
		return cleaningRules;
	}
	
	public void setCleaningFunctionPolicy(final String cleaningFunctionName) {
		removeActionListeners();	
		useFunctionRadioButton.setSelected(true);
		databaseFunctionsComboBox.setSelectedItem(cleaningFunctionName);
		cleaningUseFunction();
		addActionListeners();
	}
		
	public String getCleaningFunctionName() {
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
	
	private void cleaningUseFunction() {
		
		setEnableValidationFunctionFeature(true);
		setEnableRulesFeature(false);
	}
	
	private void addCleaningRule() {
		CleaningRuleEditorDialog cleaningEditorDialog
			= new CleaningRuleEditorDialog(userInterfaceFactory);
		CleaningRule cleaningRule
			= CleaningRule.newInstance();
		cleaningEditorDialog.setData(cleaningRule, true);
		cleaningEditorDialog.show();
		if (cleaningEditorDialog.isCancelled()) {
			return;
		}

		rulesListPanel.addListItem(cleaningEditorDialog.getData());
	}
	
	private void editCleaningRule() {
		CleaningRule selectedCleaningRule
			= (CleaningRule) rulesListPanel.getSelectedItem();
		CleaningRuleEditorDialog cleaningRuleEditorDialog
			= new CleaningRuleEditorDialog(userInterfaceFactory);
		cleaningRuleEditorDialog.setData(selectedCleaningRule, true);
		cleaningRuleEditorDialog.show();
		
	}

	private void copyCleaningRule() {
		CleaningRule selectedCleaningRule
			= (CleaningRule) rulesListPanel.getSelectedItem();
		CleaningRule cloneCleaningRule
			= CleaningRule.createCopy(selectedCleaningRule);
		String currentCleaningRuleName
			= selectedCleaningRule.getName();
		cloneCleaningRule.setName("Copy of " + currentCleaningRuleName);
		CleaningRuleEditorDialog cleaningRuleEditorDialog
			= new CleaningRuleEditorDialog(userInterfaceFactory);
		cleaningRuleEditorDialog.setData(selectedCleaningRule, true);
		cleaningRuleEditorDialog.show();
		
	}
	
	private void deleteCleaningRule() {
		rulesListPanel.deleteSelectedListItems();		
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
			cleaningUseFunction();
		}
		else if (rulesListButtonPanel.isAddButton(button)) {
			addCleaningRule();
		}
		else if (rulesListButtonPanel.isEditButton(button)) {
			editCleaningRule();
		}
		else if (rulesListButtonPanel.isCopyButton(button)) {
			copyCleaningRule();
		}
		else if (rulesListButtonPanel.isDeleteButton(button)) {
			deleteCleaningRule();
		}
		else if (button == databaseFunctionsComboBox) {
			updateValidationFunctionDescription();
		}
		
		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


