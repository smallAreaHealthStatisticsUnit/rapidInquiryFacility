package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.sahsu.rif.dataloader.concepts.FieldActionPolicy;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.presentation.ListEditingButtonPanel;
import org.sahsu.rif.generic.presentation.OrderedListPanel;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;

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

abstract class AbstractFieldPolicyEditingPanel 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	private DataLoaderToolSession session;
		
	//GUI Components
	private JPanel panel;
	private ButtonGroup policyButtonGroup;	
	private JRadioButton useFunctionRadioButton;
	private JRadioButton useRulesRadioButton;
	private OrderedListPanel rulesListPanel;
	private ListEditingButtonPanel rulesListButtonPanel;	
	private JRadioButton doNothingRadioButton;	
	private JComboBox<String> databaseFunctionsComboBox;
	private JTextArea databaseFunctionDescriptionTextArea;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractFieldPolicyEditingPanel(final DataLoaderToolSession session) {
		this.session = session;
	
	}
	
	protected void buildUI(
		final String fieldPolicyTitle,
		final String policyFeatureInstructionsText,
		final String[] functionNames) {
		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
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

		JLabel fieldPolicyLabel
			= userInterfaceFactory.createLabel(fieldPolicyTitle);
		userInterfaceFactory.setBoldFont(fieldPolicyLabel);
		panel.add(fieldPolicyLabel, panelGC);

		panelGC.gridy++;		
		JLabel instructionsLabel
			= userInterfaceFactory.createLabel(policyFeatureInstructionsText);
		panel.add(instructionsLabel, panelGC);

		policyButtonGroup 
			= userInterfaceFactory.createButtonGroup();
	
		panelGC.gridy++;		
		doNothingRadioButton
			= userInterfaceFactory.createRadioButton(
				FieldActionPolicy.DO_NOTHING.getName());
		policyButtonGroup.add(doNothingRadioButton);
		doNothingRadioButton.addActionListener(this);
		panel.add(doNothingRadioButton, panelGC);
	
		panelGC.gridy++;	
		useRulesRadioButton
			= userInterfaceFactory.createRadioButton(
				FieldActionPolicy.USE_RULES.getName());
		useRulesRadioButton.addActionListener(this);		
		policyButtonGroup.add(useRulesRadioButton);
		panel.add(useRulesRadioButton, panelGC);
		
		panelGC.gridy++;	
		panelGC.fill = GridBagConstraints.BOTH;		
		panelGC.weighty = 1;		
		panel.add(createRuleListEditingPanel(), panelGC);

		panelGC.gridy++;	
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
			createFunctionFeaturePanel(functionNames), 
			panelGC);
	
		panel.setBorder(LineBorder.createGrayLineBorder());
	}
	
	private JPanel createRuleListEditingPanel() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
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
	
	private JPanel createFunctionFeaturePanel(final String[] functionNames) {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();		

		JPanel upperPanel = userInterfaceFactory.createPanel();
		GridBagConstraints upperPanelGC
			= userInterfaceFactory.createGridBagConstraints();		
		useFunctionRadioButton
			= userInterfaceFactory.createRadioButton(
				FieldActionPolicy.USE_FUNCTION.getName());
		policyButtonGroup.add(useFunctionRadioButton);
		useFunctionRadioButton.addActionListener(this);	
		upperPanel.add(useFunctionRadioButton, upperPanelGC);
		
		upperPanelGC.gridx++;
		upperPanelGC.fill = GridBagConstraints.HORIZONTAL;
		upperPanelGC.weightx = 1;
		databaseFunctionsComboBox
			= userInterfaceFactory.createComboBox(functionNames);
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

	protected DataLoaderToolSession getSession() {
		return session;
	}
	
	public ArrayList<String> getExistingRuleNames() {
		return rulesListPanel.getDisplayNames();
	}
	
	public FieldActionPolicy getFieldActionPolicy() {
		if (useRulesRadioButton.isSelected()) {
			return FieldActionPolicy.USE_RULES;
		}
		else if (useFunctionRadioButton.isSelected()) {
			return FieldActionPolicy.USE_FUNCTION;
		}		
		else {
			return FieldActionPolicy.DO_NOTHING;
		}	
	}
		
	public void setDoNothingPolicy() {
		removeActionListeners();		
		doNothingRadioButton.setSelected(true);	
		doNothing();
		addActionListeners();
	}
	
	public void setUseRulesPolicy(
		final ArrayList<DisplayableListItemInterface> rules) {

		removeActionListeners();
		useRulesRadioButton.setSelected(true);
		
		rulesListPanel.clearList();
		for (DisplayableListItemInterface rule : rules) {
			rulesListPanel.addListItem(rule);
		}
		rulesListPanel.updateUI();		
		if (rulesListPanel.isEmpty() == false) {
			rulesListPanel.selectFirstItem();
		}

		useRules();
		addActionListeners();
	}
	
	public ArrayList<DisplayableListItemInterface> getRules() {
		ArrayList<DisplayableListItemInterface> rules
			= rulesListPanel.getAllItems();
		return rules;
	}
	
	public void setUseFunctionPolicy(final String functionName) {
		removeActionListeners();	
		useFunctionRadioButton.setSelected(true);
		databaseFunctionsComboBox.setSelectedItem(functionName);
		cleaningUseFunction();
		addActionListeners();
	}
		
	public String getFunctionName() {
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
		setEnableUseFunctionFeature(false);
		setEnableUseRulesFeature(false);
	}

	private void useRules() {		
		setEnableUseFunctionFeature(false);
		setEnableUseRulesFeature(true);
	}
	
	private void cleaningUseFunction() {
		
		setEnableUseFunctionFeature(true);
		setEnableUseRulesFeature(false);
	}
	
	abstract protected void addRule();
	abstract protected void editRule();
	abstract protected void copyRule();
	abstract protected void updateSelectedFunctionDescription();

	
	/**
	 * Convenience methods for managing rule lists
	 * 
	 */
	protected DisplayableListItemInterface getSelectedRule() {
		return rulesListPanel.getSelectedItem();
	}
	
	protected void addRuleListItem(final DisplayableListItemInterface ruleListItem) {
		rulesListPanel.addListItem(ruleListItem);
		rulesListPanel.updateUI();
		rulesListPanel.setSelectedItem(ruleListItem);
		updateListEditingButtonStates();
	}

	protected void updateRuleListItem(
		final String oldDisplayName, 
		final String newDisplayName,
		final DisplayableListItemInterface selectedRule) {
				
		if (oldDisplayName.equals(newDisplayName) == false) {
			rulesListPanel.replaceItem(
				oldDisplayName, 
				selectedRule);
			rulesListPanel.updateUI();
			rulesListPanel.setSelectedItem(selectedRule);
		}
		
		updateListEditingButtonStates();		
	}
	
	protected void deleteSelectedRules() {
		if (rulesListPanel.noItemsSelected() == false) {
			session.setSaveChanges(true);
		}
		
		rulesListPanel.deleteSelectedListItems();
		if (rulesListPanel.isEmpty() == false) {
			rulesListPanel.selectFirstItem();
		}
		
		updateListEditingButtonStates();
	}
		
	protected String getSelectedFunctionName() {
		String result
			= (String) databaseFunctionsComboBox.getSelectedItem();
		return result;
	}
	
	protected void updateFunctionDescription(final String functionDescription) {
		databaseFunctionDescriptionTextArea.setText(
			functionDescription);
	}

	protected void setEnableUseFunctionFeature(
		final boolean isEnabled) {
		
		databaseFunctionsComboBox.setEnabled(isEnabled);
		if (isEnabled == false) {
			databaseFunctionsComboBox.removeActionListener(this);
			databaseFunctionsComboBox.setSelectedItem(0);
			databaseFunctionsComboBox.addActionListener(this);
			databaseFunctionDescriptionTextArea.setText("");
		}
	}
	
	private void setEnableUseRulesFeature(
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
		
	private void updateListEditingButtonStates() {
		if (rulesListPanel.isEnabled()) {
			if (rulesListPanel.isEmpty()) {
				rulesListButtonPanel.indicateEmptyState();
			}
			else {
				rulesListButtonPanel.indicateFullEditingState();
			}
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
			addRule();
		}
		else if (rulesListButtonPanel.isEditButton(button)) {
			editRule();
		}
		else if (rulesListButtonPanel.isCopyButton(button)) {
			copyRule();
		}
		else if (rulesListButtonPanel.isDeleteButton(button)) {
			deleteSelectedRules();
		}
		else if (button == databaseFunctionsComboBox) {
			updateSelectedFunctionDescription();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


