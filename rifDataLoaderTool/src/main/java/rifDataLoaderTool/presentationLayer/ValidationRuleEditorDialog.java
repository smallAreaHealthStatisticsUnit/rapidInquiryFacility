package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.ValidationRule;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;


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

public class ValidationRuleEditorDialog 
	implements ActionListener {

	public static void main(final String[] arguments) {
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();
		ValidationRuleEditorDialog dialog
			= new ValidationRuleEditorDialog(userInterfaceFactory);
		dialog.show();
		System.exit(0);

	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private ValidationRule originalValidationRule;
	
	
	private UserInterfaceFactory userInterfaceFactory;
	
	private JDialog dialog;
	private JTextField nameTextField;
	private JTextArea descriptionTextArea;
	private JTextField validValueTextField;
	private JTextField validValueTestTextField;
	private JButton testValidValueConditionButton;

	private JTextArea testResultTextArea;

	private OKCloseButtonPanel okCloseButtonPanel;
	
	private JLabel readOnlyLabel;
	
	private boolean isCancelled;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ValidationRuleEditorDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		this.originalValidationRule = ValidationRule.newInstance();
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("validatingRuleEditorDialog.title");
		dialog
			= userInterfaceFactory.createDialog(dialogTitle);

		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("validatingRuleEditorDialog.instructions");
		JPanel instructionPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionPanel, panelGC);
		
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
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;		
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(createBottomPanel(), panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(500, 500);
		dialog.setModal(true);
		
		isCancelled = false;
		populateForm(true);
	}

	private JPanel createGeneralPropertyPanel() {
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		String nameFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("validationRule.name.label");
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
			= RIFDataLoaderToolMessages.getMessage("validationRule.description.label");
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
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
				
		String validLabelText
			= RIFDataLoaderToolMessages.getMessage("validationRule.validValue.label");
		JLabel searchLabel
			= userInterfaceFactory.createLabel(validLabelText);
		panel.add(searchLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.5;
		validValueTextField
			= userInterfaceFactory.createTextField();
		panel.add(validValueTextField, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String searchTestLabelText
			= RIFDataLoaderToolMessages.getMessage("validatingRuleEditorDialog.buttons.test");
		testValidValueConditionButton
			= userInterfaceFactory.createButton(searchTestLabelText);
		testValidValueConditionButton.addActionListener(this);
		panel.add(testValidValueConditionButton, panelGC);
				
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 0.5;
		validValueTestTextField 
			= userInterfaceFactory.createTextField();
		panel.add(validValueTestTextField, panelGC);
		
		return panel;
	}
	
	private JPanel createTestResultDisplayArea() {
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String testResultLabelText
			= RIFDataLoaderToolMessages.getMessage("validatingRuleEditorDialog.testResult.label");
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
	
	private JPanel createBottomPanel() {
		JPanel panel
			= userInterfaceFactory.createBorderLayoutPanel();
		
		readOnlyLabel = userInterfaceFactory.createLabel("");
		panel.add(readOnlyLabel, BorderLayout.WEST);
		
		okCloseButtonPanel = new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), BorderLayout.EAST);
		
		return panel;
	}
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void show() {
		dialog.setVisible(true);
	}
	
	public void setEditable(final boolean isEditable) {
		nameTextField.setEditable(isEditable);
		validValueTextField.setEditable(isEditable);
		descriptionTextArea.setEditable(isEditable);

		if (isEditable) {
			readOnlyLabel.setText("");		
		}
		else {
			String readOnlyLabelText
				= RIFDataLoaderToolMessages.getMessage("general.readOnly.label");
			readOnlyLabel.setText(readOnlyLabelText);
		}
	}
	
	public ValidationRule getData() {
		return originalValidationRule;		
	}
	
	public void setData(
		final ValidationRule originalValidationRule,
		final boolean isEditable) {
		
		this.originalValidationRule = originalValidationRule;
		
		populateForm(isEditable);
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	private void populateForm(final boolean isEditable) {
		nameTextField.setText(originalValidationRule.getName());
		descriptionTextArea.setText(originalValidationRule.getDescription());
		validValueTextField.setText(originalValidationRule.getValidValue());
		validValueTestTextField.setText("");
		
		nameTextField.setEditable(isEditable);
		descriptionTextArea.setEditable(isEditable);
		validValueTextField.setEditable(isEditable);
		
		userInterfaceFactory.setEditableAppearance(
			nameTextField,
			true);				
		userInterfaceFactory.setEditableAppearance(
			descriptionTextArea,
			true);				
		userInterfaceFactory.setEditableAppearance(
			validValueTextField,
			true);		
	}
	
	private void saveChanges() {
		ValidationRule currentValidationRule = ValidationRule.newInstance();
		currentValidationRule.setName(nameTextField.getText().trim());
		currentValidationRule.setDescription(descriptionTextArea.getText().trim());
		currentValidationRule.setValidValue(validValueTextField.getText().trim());
		
		try {
			currentValidationRule.checkErrors();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}
		
		originalValidationRule.setValidValue(validValueTextField.getText());
	}
	
	private void testValidValue() {
		String currentSearchValue
			= validValueTextField.getText().trim();
		String currentTestValue
			= validValueTestTextField.getText().trim();
		String passMessage
			= RIFDataLoaderToolMessages.getMessage(
				"cleaningRuleEditorDialog.passMessage",
				currentSearchValue,
				currentTestValue);
		String failMessage
			= RIFDataLoaderToolMessages.getMessage(
				"cleaningRuleEditorDialog.failMessage",
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
	
	private void ok() {
		saveChanges();
		dialog.setVisible(false);
		isCancelled = false;
	}
	
	private void close() {
		dialog.setVisible(false);
		isCancelled = true;
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
		
		if (button == testValidValueConditionButton) {
			testValidValue();
		}
		if (okCloseButtonPanel.isOKButton(button)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}	
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


