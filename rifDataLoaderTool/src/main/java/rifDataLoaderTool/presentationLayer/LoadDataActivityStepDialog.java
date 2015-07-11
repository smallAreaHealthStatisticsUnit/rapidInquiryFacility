package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolSession;


import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.text.Collator;
import java.io.File;
import java.util.ArrayList;


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

public final class LoadDataActivityStepDialog 
	extends AbstractDataLoaderToolDialog
	implements ActionListener {

	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
		
	private ButtonGroup loadDataOptionsButtonGroup;
	
	private JRadioButton loadDataFromSourceFileRadioButton;	
	private JTextField sourceFileNameTextField;
	private JButton browseSourceFileButton;
	
	
	private JRadioButton firstLineIsHeaderRow;
	private JRadioButton firstLineIsNotHeaderRow;

	private JRadioButton loadDataFromSourceTableRadioButton;
	private JTextField sourceTableNameTextField;
	private JButton browseSourceTableButton;
	
	private JButton decideFromExistingTableNamesButton;

	private JTextField destinationTableNameField;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public LoadDataActivityStepDialog(
		final RIFDataLoaderToolSession session) {

		super(session);
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.load.label");
		setDialogTitle(dialogTitle);
		setSize(500, 380);
		
		//create main panel
		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.ipady = 10;
				
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.instructions.label");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		panelGC.gridy++;
		panel.add(createLoadSourceFilePanel(), panelGC);
		panelGC.gridy++;
		panel.add(createLoadSourceTablePanel(), panelGC);
		panelGC.gridy++;
		panel.add(createLoadDestinationTablePanel(), panelGC);

		sensitiseSelectSourceFilePanel(true);
		loadDataFromSourceFileRadioButton.setSelected(true);
		loadDataOptionsButtonGroup = userInterfaceFactory.createButtonGroup();
		loadDataOptionsButtonGroup.add(loadDataFromSourceFileRadioButton);
		loadDataOptionsButtonGroup.add(loadDataFromSourceTableRadioButton);		
		loadDataFromSourceFileRadioButton.addActionListener(this);
		loadDataFromSourceTableRadioButton.addActionListener(this);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(getOKCloseButtonPanel(), panelGC);

		setMainPanel(panel);
	}
	
	private JPanel createLoadSourceFilePanel() {
		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		String loadDataFromSourceFileButtonText
			= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.loadSourceFile.label");
		loadDataFromSourceFileRadioButton
			= userInterfaceFactory.createRadioButton(loadDataFromSourceFileButtonText);
		panel.add(loadDataFromSourceFileRadioButton, panelGC);

		panelGC.gridy++;
		panelGC.insets = userInterfaceFactory.createInsets(0, 20, 0, 0);
		
		panel.add(createSourceFileSelectionPanel(), panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panel.add(createHeaderRowOptionsPanel(), panelGC);
		
		return panel;		
	}
	
	private JPanel createSourceFileSelectionPanel() {
		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();

		
		String loadSourceFileLabelText
			= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.sourceFile.label");
		JLabel sourceFileLabel
			= userInterfaceFactory.createLabel(loadSourceFileLabelText);
		panel.add(sourceFileLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		sourceFileNameTextField
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(sourceFileNameTextField, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String browseButtonText
			= RIFGenericLibraryMessages.getMessage("buttons.browse.label");
		browseSourceFileButton
			= userInterfaceFactory.createButton(browseButtonText);
		browseSourceFileButton.addActionListener(this);
		panel.add(browseSourceFileButton, panelGC);
				
		return panel;		
	}

	private JPanel createHeaderRowOptionsPanel() {

		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
				
		String firstLineHeaderRowText
			= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.firstLineHeaderRow.label");
		JLabel firstLineHeaderRowLabel
			= userInterfaceFactory.createLabel(firstLineHeaderRowText);
		panel.add(firstLineHeaderRowLabel, panelGC);
		
		panelGC.gridx++;
		String yesText
			= RIFGenericLibraryMessages.getMessage("radioButtons.yes.label");
		firstLineIsHeaderRow
			= userInterfaceFactory.createRadioButton(yesText);
		firstLineIsHeaderRow.addActionListener(this);
		panel.add(firstLineIsHeaderRow, panelGC);
		
		panelGC.gridx++;
		String noText
			= RIFGenericLibraryMessages.getMessage("radioButtons.no.label");
		firstLineIsNotHeaderRow
			= userInterfaceFactory.createRadioButton(noText);
		firstLineIsNotHeaderRow.addActionListener(this);
		panel.add(firstLineIsNotHeaderRow, panelGC);
		
		return panel;		
	}
	
	// Methods for rendering the panel that lets users select a published table
	

	private JPanel createLoadSourceTablePanel() {

		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		String loadDataFromSourceTableButtonText
			= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.loadSourceTable.label");
		loadDataFromSourceTableRadioButton
			= userInterfaceFactory.createRadioButton(loadDataFromSourceTableButtonText);
		panel.add(loadDataFromSourceTableRadioButton, panelGC);		
		
		panelGC.insets = userInterfaceFactory.createInsets(0, 20, 0, 0);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;

		panelGC.gridy++;
		panel.add(createSourceTableSelectionPanel(), panelGC);
		
		return panel;		
	}
	
	private JPanel createSourceTableSelectionPanel() {
		
		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		String loadSourceTableLabelText
			= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.sourceTable.label");
		JLabel sourceTableLabel
			= userInterfaceFactory.createLabel(loadSourceTableLabelText);
		panel.add(sourceTableLabel, panelGC);
		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		panelGC.gridx++;
		sourceTableNameTextField
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(sourceTableNameTextField, panelGC);

		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.gridx++;
		String browseButtonText
			= RIFGenericLibraryMessages.getMessage("buttons.browse.label");
		browseSourceTableButton
			= userInterfaceFactory.createButton(browseButtonText);
		browseSourceTableButton.addActionListener(this);
		panel.add(browseSourceTableButton, panelGC);
				
		return panel;		
	}
		
	private JPanel createLoadDestinationTablePanel() {

		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		String destinationTableNameText
			= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.destinationTableName.label");
		JLabel destinationTableNameLabel
			= userInterfaceFactory.createLabel(destinationTableNameText);
		panel.add(destinationTableNameLabel, panelGC);
		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		panelGC.gridx++;
		destinationTableNameField
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(destinationTableNameField, panelGC);
		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.gridx++;		
		String decideFromExistingTableNamesButtonText
			= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.decideFromExistingTableName.label");
		decideFromExistingTableNamesButton
			= userInterfaceFactory.createButton(decideFromExistingTableNamesButtonText);
		decideFromExistingTableNamesButton.addActionListener(this);
		panel.add(decideFromExistingTableNamesButton, panelGC);
				
		return panel;		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	private void browseSourceFile() {

		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();		
		JFileChooser fileChooser	
			= userInterfaceFactory.createFileChooser();
		
		int result = fileChooser.showOpenDialog(getDialog());
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File selectedFile = fileChooser.getSelectedFile();
		sourceFileNameTextField.setText(selectedFile.getAbsolutePath());		
	}
	
	private void browseSourceTable() {
		
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void validateForm() throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		Collator collator = RIFDataLoaderToolMessages.getCollator();
		if (loadDataFromSourceFileRadioButton.isSelected()) {
			String selectedSourceFilePathName = sourceFileNameTextField.getText().trim();
			if (collator.equals(selectedSourceFilePathName, "")) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.error.noSourceFileSpecified");
				errorMessages.add(errorMessage);
			}
		}
		else {
			//loading data from source table option selected
			String selectedSourceTableName
				= sourceTableNameTextField.getText().trim();
			if (collator.equals(selectedSourceTableName, "")) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage("loadDataActivityStepDialog.error.noSourceTableSpecified");
				errorMessages.add(errorMessage);
			}
		}
		
		if (errorMessages.size() > 0) {
			RIFServiceException RIFServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.INVALID_LOAD_SOURCE,
					errorMessages);
			throw RIFServiceException;
		}
		
	}
	
	private void sensitiseSelectSourceFilePanel(boolean isSelectSourceFilePanelSensitised) {
		
		if (isSelectSourceFilePanelSensitised) {
			browseSourceFileButton.setEnabled(true);
			
			browseSourceTableButton.setEnabled(false);
			sourceTableNameTextField.setText("");
		}
		else {
			browseSourceTableButton.setEnabled(true);
			
			browseSourceFileButton.setEnabled(false);
			sourceFileNameTextField.setText("");
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: Action Listener
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == loadDataFromSourceFileRadioButton) {
			sensitiseSelectSourceFilePanel(true);	
		}
		else if (source == loadDataFromSourceTableRadioButton) {
			sensitiseSelectSourceFilePanel(false);
		}		
		else if (source == browseSourceFileButton) {
			browseSourceFile();
		}
		else if (source == browseSourceTableButton) {
			browseSourceTable();
		}	
		else if (isOKButton(source)) {
			try {
				validateForm();
				hide();
			}
			catch(RIFServiceException exception) {
				ErrorDialog.showError(
					getDialog(), 
					exception.getErrorMessages());
			}
		}
		else if (isCloseButton(source)) {
			hide();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


