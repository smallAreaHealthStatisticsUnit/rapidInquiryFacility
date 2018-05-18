package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.sahsu.rif.dataloader.concepts.CleaningRule;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.OKCloseButtonDialog;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

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

class CleaningRuleEditorDialog
	extends OKCloseButtonDialog {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	private CleaningRule originalCleaningRule;
	private ArrayList<String> existingListItemNames;
		
	//GUI Components	
	private JTextField nameTextField;
	private JTextArea descriptionTextArea;
	private JTextField searchTextField;
	private JTextField searchTestTextField;
	private JButton testSearchConditionButton;
	private JTextField replaceTextField;
	private JTextField replaceTestTextField;
	private JButton testReplaceConditionButton;
	private JTextArea testResultTextArea;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public CleaningRuleEditorDialog(
		final DataLoaderToolSession session) {

		super(session.getUserInterfaceFactory());
		
		originalCleaningRule = CleaningRule.newInstance();
		existingListItemNames = new ArrayList<String>();
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.title");
		setDialogTitle(dialogTitle);
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.instructions");
		setInstructionText(instructionsText);
		setMainPanel(createMainPanel());
		
		setSize(500, 500);
		buildUI();
	}

	private JPanel createMainPanel() {

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
	
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 0.5;		
		panel.add(createGeneralPropertyPanel(), panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;				
		panel.add(createRegularExpressionPanel(), panelGC);		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 0.5;
		panel.add(createTestResultDisplayArea(), panelGC);
	
		populateForm(true);
		
		return panel;
	}
	
	private JPanel createGeneralPropertyPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		String nameFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRule.name.label");
		JLabel nameFieldLabel
			= userInterfaceFactory.createLabel(nameFieldLabelText);
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
		String descriptionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRule.description.label");
		JLabel descriptionFieldLabel
			= userInterfaceFactory.createLabel(descriptionFieldLabelText);
		panel.add(descriptionFieldLabel, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		descriptionTextArea 
			= userInterfaceFactory.createTextArea();
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(descriptionTextArea);
		panel.add(scrollPane, panelGC);

		return panel;		
	}
	
	private JPanel createRegularExpressionPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
				
		String searchLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRule.searchValue.label");
		JLabel searchLabel
			= userInterfaceFactory.createLabel(searchLabelText);
		panel.add(searchLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.5;
		searchTextField
			= userInterfaceFactory.createTextField();
		panel.add(searchTextField, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String searchTestLabelText
			= RIFDataLoaderToolMessages.getMessage("ruleEditorDialog.buttons.test");
		testSearchConditionButton
			= userInterfaceFactory.createButton(searchTestLabelText);
		testSearchConditionButton.addActionListener(this);
		panel.add(testSearchConditionButton, panelGC);
				
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.5;
		searchTestTextField 
			= userInterfaceFactory.createTextField();
		panel.add(searchTestTextField, panelGC);

		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;		
		String replaceLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRule.replaceValue.label");
		JLabel replaceLabel
			= userInterfaceFactory.createLabel(replaceLabelText);
		panel.add(replaceLabel, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.5;
		replaceTextField
			= userInterfaceFactory.createTextField();
		panel.add(replaceTextField, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String replaceTestLabelText
			= RIFDataLoaderToolMessages.getMessage("ruleEditorDialog.buttons.test");
		testReplaceConditionButton		
			= userInterfaceFactory.createButton(replaceTestLabelText);
		panel.add(testReplaceConditionButton, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.5;
		replaceTestTextField 
			= userInterfaceFactory.createTextField();
		panel.add(replaceTestTextField, panelGC);
		
		return panel;
	}
	
	private JPanel createTestResultDisplayArea() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String testResultLabelText
			= RIFDataLoaderToolMessages.getMessage("ruleEditorDialog.testResult.label");
		JLabel testResultLabel
			= userInterfaceFactory.createLabel(testResultLabelText);
		panel.add(testResultLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		testResultTextArea
			= userInterfaceFactory.createNonEditableTextArea(2, 30);
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(testResultTextArea);
		panel.add(scrollPane, panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
	}
	

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public CleaningRule getData() {
		return originalCleaningRule;		
	}
	
	public void setData(
		final CleaningRule originalCleaningRule,
		final ArrayList<String> existingListItemNames,
		final boolean isEditable) {
		
		this.originalCleaningRule = originalCleaningRule;
		this.existingListItemNames = existingListItemNames;
		
		populateForm(isEditable);
	}
	
	private void populateForm(final boolean isEditable) {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		nameTextField.setText(originalCleaningRule.getName());
		descriptionTextArea.setText(originalCleaningRule.getDescription());
		searchTextField.setText(originalCleaningRule.getSearchValue());
		replaceTextField.setText(originalCleaningRule.getReplaceValue());		
		searchTestTextField.setText("");
		replaceTestTextField.setText("");	
		
		nameTextField.setEditable(isEditable);
		descriptionTextArea.setEditable(isEditable);
		searchTextField.setEditable(isEditable);
		replaceTextField.setEditable(isEditable);	
		
		userInterfaceFactory.setEditableAppearance(
			nameTextField,
			isEditable);				
		userInterfaceFactory.setEditableAppearance(
			descriptionTextArea,
			isEditable);				
		userInterfaceFactory.setEditableAppearance(
			searchTextField,
			isEditable);				
		userInterfaceFactory.setEditableAppearance(
			replaceTextField,
			true);	
	}
	
	public boolean saveChanges() {
		
		boolean saveChanges = false;
		try {
			CleaningRule cleaningRuleFromForm = getCleaningRuleFromForm();
			cleaningRuleFromForm.checkErrors();
			
			new FieldValidationUtility().checkListDuplicate(
				cleaningRuleFromForm.getDisplayName(), 
				existingListItemNames);
			
			saveChanges
				= !originalCleaningRule.hasIdenticalContents(cleaningRuleFromForm);
			
			CleaningRule.copyInto(
				cleaningRuleFromForm, 
				originalCleaningRule);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
		}
		return saveChanges;
	}
	
	private CleaningRule getCleaningRuleFromForm() {
		CleaningRule cleaningRuleFromForm = CleaningRule.newInstance();
		cleaningRuleFromForm.setName(nameTextField.getText().trim());
		cleaningRuleFromForm.setDescription(descriptionTextArea.getText().trim());
		cleaningRuleFromForm.setSearchValue(searchTextField.getText().trim());
		cleaningRuleFromForm.setReplaceValue(replaceTextField.getText().trim());
		
		return cleaningRuleFromForm;
	}
	
	private void testSearchValue() {
		String currentSearchValue
			= searchTextField.getText().trim();
		String currentTestValue
			= searchTestTextField.getText().trim();
		String passMessage
			= RIFDataLoaderToolMessages.getMessage(
				"ruleEditorDialog.passMessage",
				currentSearchValue,
				currentTestValue);
		String failMessage
			= RIFDataLoaderToolMessages.getMessage(
				"ruleEditorDialog.failMessage",
				currentSearchValue,
				currentTestValue);
		performTest(
			currentSearchValue,
			currentTestValue,
			passMessage,
			failMessage);
	}

	private void performTest(
		final String patternValue,
		final String testValue,
		final String passMessage,
		final String failMessage) {

		if (Pattern.matches(patternValue, testValue)) {
			testResultTextArea.setText(passMessage);
		}
		else {
			testResultTextArea.setText(failMessage);			
		}
	}
				
	private void testReplaceValue() {
		String currentReplaceValue
			= replaceTextField.getText().trim();
		String currentTestValue
			= replaceTestTextField.getText().trim();
		String passMessage
			= RIFDataLoaderToolMessages.getMessage(
				"ruleEditorDialog.passMessage",
				currentTestValue,				
				currentReplaceValue);
		String failMessage
			= RIFDataLoaderToolMessages.getMessage(
				"ruleEditorDialog.failMessage",
				currentReplaceValue,
				currentTestValue);
		
		performTest(
			currentReplaceValue,
			currentTestValue,
			passMessage,
			failMessage);
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
		
		if (button == testSearchConditionButton) {
			testSearchValue();
		}
		else if (button == testReplaceConditionButton) {
			testReplaceValue();
		}
		else {
			this.performOKCloseActions(event);
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

	@Override
	public void okAction()
		throws RIFServiceException {
		
		CleaningRule cleaningRuleFromForm = getCleaningRuleFromForm();
		cleaningRuleFromForm.checkErrors();
	}
}	
