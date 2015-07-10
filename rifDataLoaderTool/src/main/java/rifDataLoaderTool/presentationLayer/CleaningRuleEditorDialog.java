package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifDataLoaderTool.businessConceptLayer.CleaningRule;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.YesNoQuestionPanel;
import rifGenericLibrary.system.RIFServiceException;






import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class CleaningRuleEditorDialog 
	extends AbstractDataLoaderToolDialog
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private CleaningRule currentCleaningRule;
	
	private JTextField nameValueTextField;
	private JTextArea descriptionValueTextArea;
	private JTextField searchValueTextField;
	private JTextField replaceValueTextField;
	
	private YesNoQuestionPanel yesNoQuestionPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleaningRuleEditorDialog(
		final RIFDataLoaderToolSession session) {

		super(session);
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.title");
		setDialogTitle(dialogTitle);
		
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.instructions");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;		
		panel.add(createMainFieldPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		String questionText
			= RIFDataLoaderToolMessages.getMessage("cleaningRuleEditorDialog.isRegularExpressionSearch.label");
		yesNoQuestionPanel 
			= new YesNoQuestionPanel(
				questionText,
				userInterfaceFactory,
				true);
		yesNoQuestionPanel.buildUI();
		panel.add(yesNoQuestionPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.weighty = 0;
		panelGC.weightx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(getOKCloseButtonPanel(), panelGC);
		
		setMainPanel(panel);
		setSize(400, 400);
	}

	private JPanel createMainFieldPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		
		String nameValueFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRule.name.label");
		JLabel nameValueLabel
			= userInterfaceFactory.createLabel(nameValueFieldLabelText);
		panel.add(nameValueLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		nameValueTextField = userInterfaceFactory.createTextField();
		panel.add(nameValueTextField, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		String descriptionValueFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRule.description.label");
		JLabel descriptionValueLabel
			= userInterfaceFactory.createLabel(descriptionValueFieldLabelText);
		panel.add(descriptionValueLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		descriptionValueTextArea
			= userInterfaceFactory.createTextArea();
		JScrollPane scrollPane 
			= userInterfaceFactory.createScrollPane(descriptionValueTextArea);
		panel.add(scrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		String searchValueLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRule.searchValue.label");
		JLabel searchValueLabel
			= userInterfaceFactory.createLabel(searchValueLabelText);
		panel.add(searchValueLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		searchValueTextField
			= userInterfaceFactory.createTextField();
		panel.add(searchValueTextField, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;		
		String replaceValueLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningRule.replaceValue.label");
		JLabel replaceValueLabel
			= userInterfaceFactory.createLabel(replaceValueLabelText);
		panel.add(replaceValueLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		replaceValueTextField
			= userInterfaceFactory.createTextField();
		panel.add(replaceValueTextField, panelGC);
				
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setData(
		final CleaningRule currentCleaningRule) {

		this.currentCleaningRule = currentCleaningRule;
		
		nameValueTextField.setText(currentCleaningRule.getName());
		descriptionValueTextArea.setText(currentCleaningRule.getDescription());
		searchValueTextField.setText(currentCleaningRule.getSearchValue());
		replaceValueTextField.setText(currentCleaningRule.getReplaceValue());	
		yesNoQuestionPanel.setIsYesSelected(currentCleaningRule.isRegularExpressionSearch());
	}
	
	private void ok() {
		try {
			validateForm();
			
			currentCleaningRule.setName(nameValueTextField.getText().trim());
			currentCleaningRule.setDescription(descriptionValueTextArea.getText().trim());
			currentCleaningRule.setSearchValue(searchValueTextField.getText().trim());
			currentCleaningRule.setReplaceValue(replaceValueTextField.getText().trim());
			currentCleaningRule.setRegularExpressionSearch(yesNoQuestionPanel.isYesSelected());
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
		}
		
		hide();
	}
	
	private void close() {

		hide();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private void validateForm() 
		throws RIFServiceException {

		CleaningRule candidateCleaningRule
			= CleaningRule.newInstance();
		candidateCleaningRule.setName(nameValueTextField.getText().trim());
		candidateCleaningRule.setDescription(descriptionValueTextArea.getText().trim());
		candidateCleaningRule.setSearchValue(searchValueTextField.getText().trim());
		candidateCleaningRule.setReplaceValue(replaceValueTextField.getText().trim());
		
		candidateCleaningRule.checkErrors();
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
		
		if (isOKButton(button)) {
			ok();
		}
		else if (isCloseButton(button)) {
			close();
		}
		else {
			assert(false);
		}		
	}
	

	// ==========================================
	// Section Override
	// ==========================================

}


