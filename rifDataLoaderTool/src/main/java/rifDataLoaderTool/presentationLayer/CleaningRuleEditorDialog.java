package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.CleaningRule;
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

public class CleaningRuleEditorDialog 
	implements ActionListener {

	public static void main(final String[] arguments) {
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();
		CleaningRuleEditorDialog dialog
			= new CleaningRuleEditorDialog(userInterfaceFactory);
		dialog.show();
		System.exit(0);

	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private CleaningRule originalCleaningRule;
	
	
	private UserInterfaceFactory userInterfaceFactory;
	
	private JDialog dialog;
	private JTextField nameTextField;
	private JTextArea descriptionTextArea;
	private JTextField searchTextField;
	private JTextField searchTestTextField;
	private JButton testSearchConditionButton;
	
	private JTextField replaceTextField;
	private JTextField replaceTestTextField;
	private JButton testReplaceConditionButton;

	private JTextArea testResultTextArea;

	private OKCloseButtonPanel okCloseButtonPanel;
	
	private JLabel readOnlyLabel;
	
	private boolean isCancelled;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleaningRuleEditorDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		this.originalCleaningRule = CleaningRule.newInstance();
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.title");
		dialog
			= userInterfaceFactory.createDialog(dialogTitle);

		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.instructions");
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
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.buttons.test");
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
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.buttons.test");
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
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String testResultLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.testResult.label");
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
		searchTextField.setEditable(isEditable);
		descriptionTextArea.setEditable(isEditable);
		replaceTextField.setEditable(isEditable);

		
		
		if (isEditable) {
			readOnlyLabel.setText("");		
		}
		else {
			String readOnlyLabelText
				= RIFDataLoaderToolMessages.getMessage("general.readOnly.label");
			readOnlyLabel.setText(readOnlyLabelText);
		}
	}
	
	public CleaningRule getData() {
		return originalCleaningRule;		
	}
	
	public void setData(
		final CleaningRule originalCleaningRule,
		final boolean isEditable) {
		
		this.originalCleaningRule = originalCleaningRule;
		
		populateForm(isEditable);
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	private void populateForm(final boolean isEditable) {
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
		
		userInterfaceFactory.setReadOnlyAppearance(
			nameTextField,
			true);				
		userInterfaceFactory.setReadOnlyAppearance(
			descriptionTextArea,
			true);				
		userInterfaceFactory.setReadOnlyAppearance(
			searchTextField,
			true);				
		userInterfaceFactory.setReadOnlyAppearance(
			replaceTextField,
			true);			
		
	}
	
	private void saveChanges() {
		CleaningRule currentCleaningRule = CleaningRule.newInstance();
		currentCleaningRule.setName(nameTextField.getText().trim());
		currentCleaningRule.setDescription(descriptionTextArea.getText().trim());
		currentCleaningRule.setSearchValue(searchTextField.getText().trim());
		currentCleaningRule.setReplaceValue(replaceTextField.getText().trim());
		
		try {
			currentCleaningRule.checkErrors();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}
		
		originalCleaningRule.setSearchValue(searchTextField.getText());
		originalCleaningRule.setReplaceValue(replaceTextField.getText());		
	}
	
	private void testSearchValue() {
		String currentSearchValue
			= searchTextField.getText().trim();
		String currentTestValue
			= searchTestTextField.getText().trim();
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
				
	private void testReplaceValue() {
		String currentReplaceValue
			= replaceTextField.getText().trim();
		String currentTestValue
			= replaceTestTextField.getText().trim();
		String passMessage
			= RIFDataLoaderToolMessages.getMessage(
				"cleaningRuleEditorDialog.passMessage",
				currentTestValue,				
				currentReplaceValue);
		String failMessage
			= RIFDataLoaderToolMessages.getMessage(
				"cleaningRuleEditorDialog.failMessage",
				currentReplaceValue,
				currentTestValue);
		
		performTest(
			currentReplaceValue,
			currentTestValue,
			passMessage,
			failMessage);
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
		
		if (button == testSearchConditionButton) {
			testSearchValue();
		}
		else if (button == testReplaceConditionButton) {
			testReplaceValue();
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


