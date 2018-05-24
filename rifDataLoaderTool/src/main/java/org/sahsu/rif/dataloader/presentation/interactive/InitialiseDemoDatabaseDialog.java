package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sahsu.rif.dataloader.concepts.DatabaseConnectionsConfiguration;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.OKCloseButtonDialog;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;


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

class InitialiseDemoDatabaseDialog
		extends OKCloseButtonDialog {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	private DatabaseConnectionsConfiguration databaseConnectionParameters;
	
	//GUI Components
	private JTextField databaseNameTextField;
	private JTextField portTextField;
	private JTextField hostTextField;		
	private JTextField userTextField;
	private JTextField passwordTextField;
			
	// ==========================================
	// Section Construction
	// ==========================================

	public InitialiseDemoDatabaseDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		super(userInterfaceFactory);
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage(
				"initialiseDemoDatabaseDialog.title");
		setDialogTitle(dialogTitle);	
		setMainPanel(createMainPanel());
		buildUI();	
		setSize(350, 220);
	}

	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String instructionText
			= RIFDataLoaderToolMessages.getMessage(
				"initialiseDemoDatabaseDialog.instructions");
		JLabel instructionLabel
			= userInterfaceFactory.createInstructionLabel(instructionText);
		panel.add(instructionLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		panel.add(createFieldPanel(), panelGC);
		
		return panel;
	}
	
	private JPanel createFieldPanel() {	

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel = userInterfaceFactory.createPanel();
		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String databaseNameLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"initialiseDemoDatabaseDialog.databaseName.label");
		JLabel databaseNameLabel
			= userInterfaceFactory.createLabel(databaseNameLabelText);
		panel.add(
			databaseNameLabel, 
			panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		databaseNameTextField
			= userInterfaceFactory.createTextField();
		panel.add(
			databaseNameTextField, 
			panelGC);

		panelGC.gridy++;
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String hostText
			= RIFDataLoaderToolMessages.getMessage(
				"initialiseDemoDatabaseDialog.databaseHost.label");
		JLabel hostLabel
			= userInterfaceFactory.createLabel(hostText);
		panel.add(
			hostLabel, 
			panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		hostTextField
			= userInterfaceFactory.createTextField();
		panel.add(
			hostTextField, 
			panelGC);
		
		
		panelGC.gridy++;
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String portLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"initialiseDemoDatabaseDialog.databasePort.label");
		JLabel portLabel
			= userInterfaceFactory.createLabel(portLabelText);
		panel.add(
			portLabel, 
			panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		portTextField
			= userInterfaceFactory.createTextField();
		panel.add(
			portTextField, 
			panelGC);

		panelGC.gridy++;
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String userLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"initialiseDemoDatabaseDialog.userID.label");
		JLabel userLabel
			= userInterfaceFactory.createLabel(userLabelText);
		panel.add(
			userLabel, 
			panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		userTextField
			= userInterfaceFactory.createTextField();
		panel.add(
			userTextField, 
			panelGC);
		userTextField.setText("kgarwood");

		panelGC.gridy++;
		
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String passwordLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"initialiseDemoDatabaseDialog.password.label");
		JLabel passwordLabel
			= userInterfaceFactory.createLabel(passwordLabelText);
		panel.add(
			passwordLabel, 
			panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		passwordTextField
			= userInterfaceFactory.createTextField();
		panel.add(
			passwordTextField, 
			panelGC);
		passwordTextField.setText("kgarwood");
		
		
		return panel;

	}	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getUserID() {
		return userTextField.getText();
	}

	public String getPassword() {
		return passwordTextField.getText();
	}

	public DatabaseConnectionsConfiguration getData() {	
		return databaseConnectionParameters;		
	}
	
	public void setData(
		final DatabaseConnectionsConfiguration databaseConnectionParameters) {
		
		this.databaseConnectionParameters = databaseConnectionParameters;
		populateForm();
	}
	
	private void populateForm() {
		
		databaseNameTextField.setText(
			databaseConnectionParameters.getDatabaseName());
		portTextField.setText(
			databaseConnectionParameters.getPortName());
		hostTextField.setText(
			databaseConnectionParameters.getHostName());	
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================


	public void actionPerformed(ActionEvent event) {
		performOKCloseActions(event);
	}
	
	// ==========================================
	// Section Override
	// ==========================================
	
	@Override
	public void okAction()
		throws RIFServiceException {
		
		databaseConnectionParameters.setDatabaseName(
			databaseNameTextField.getText());
		databaseConnectionParameters.setHostName(
			hostTextField.getText());
		databaseConnectionParameters.setPortName(
			portTextField.getText());
				
	}

}



