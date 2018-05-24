package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sahsu.rif.dataloader.concepts.CleaningRule;
import org.sahsu.rif.dataloader.concepts.FieldActionPolicy;
import org.sahsu.rif.dataloader.concepts.RIFDataType;
import org.sahsu.rif.dataloader.concepts.ValidationRule;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;

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

class DataTypeEditingPanel 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private DataLoaderToolSession session;
	
	private JPanel panel;
	private Component parentComponent;	
	private JTextField nameTextField;
	private JTextField descriptionTextField;
	
	private DataCleaningPolicyEditingPanel dataCleaningPolicyEditingPanel;
	private DataValidationPolicyEditingPanel dataValidationPolicyEditingPanel;
	
	private RIFDataType originalRIFDataType;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataTypeEditingPanel(
		final DataLoaderToolSession session,
		final Component parentComponent) {
		
		this.session = session;
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		this.parentComponent = parentComponent;
		
		panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		panel.add(createFieldPanel(), panelGC);
	
		GridBagConstraints policyPanelGC 
			= userInterfaceFactory.createGridBagConstraints();
		JPanel policyPanel 
			= userInterfaceFactory.createPanel();
		policyPanelGC.fill = GridBagConstraints.BOTH;
		policyPanelGC.weightx = 0.5;	
		policyPanelGC.weighty = 1;
		dataCleaningPolicyEditingPanel
			= new DataCleaningPolicyEditingPanel(session);
		policyPanel.add(
			dataCleaningPolicyEditingPanel.getPanel(), 
			policyPanelGC);
		
		policyPanelGC.gridx++;
		dataValidationPolicyEditingPanel
			= new DataValidationPolicyEditingPanel(session);
		policyPanel.add(
			dataValidationPolicyEditingPanel.getPanel(), 
			policyPanelGC);

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panel.add(policyPanel, panelGC);
				
	}

	private JPanel createFieldPanel() {		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		//panelGC.fill = GridBagConstraints.NONE;
		//panelGC.weightx = 0;
		//String identifierLabelText
		//	= RIFDataLoaderToolMessages.getMessage("rifDataType.identifier.label");
		//JLabel identifierLabel
		//	= userInterfaceFactory.createLabel(identifierLabelText);
		//panel.add(identifierLabel, panelGC);
		
		//panelGC.gridx++;
		//panelGC.fill = GridBagConstraints.HORIZONTAL;
		//panelGC.weightx = 1;
		//identifierTextField = userInterfaceFactory.createTextField();
		//panel.add(identifierTextField, panelGC);
		
		panelGC.gridx = 0;
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String nameLabelText
			= RIFDataLoaderToolMessages.getMessage("rifDataType.name.label");
		JLabel nameLabel
			= userInterfaceFactory.createLabel(nameLabelText);
		panel.add(nameLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		nameTextField = userInterfaceFactory.createTextField();
		panel.add(nameTextField, panelGC);
				
		panelGC.gridx = 0;
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String descriptionLabelText
			= RIFDataLoaderToolMessages.getMessage("rifDataType.description.label");
		JLabel descriptionLabel
			= userInterfaceFactory.createLabel(descriptionLabelText);
		panel.add(descriptionLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		descriptionTextField = userInterfaceFactory.createTextField();
		panel.add(descriptionTextField, panelGC);

		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public JPanel getPanel() {
		return panel;
	}
	
	
	
	
	public void setData(final RIFDataType originalRIFDataType) {
		this.originalRIFDataType = originalRIFDataType;
		
		nameTextField.setText(originalRIFDataType.getName());
		descriptionTextField.setText(originalRIFDataType.getDescription());
			
		boolean isEditable = true;
		if (originalRIFDataType.isReservedDataType()) {
			isEditable = false;
		}

		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();		
		userInterfaceFactory.setEditableAppearance(
			nameTextField,
			isEditable);
		userInterfaceFactory.setEditableAppearance(
			descriptionTextField,
			isEditable);			


		FieldActionPolicy fieldCleaningPolicy
			= originalRIFDataType.getFieldCleaningPolicy();
		if (fieldCleaningPolicy == FieldActionPolicy.USE_RULES) {
			ArrayList<CleaningRule> cleaningRules
				= originalRIFDataType.getCleaningRules();
			dataCleaningPolicyEditingPanel.setCleaningRulesPolicy(cleaningRules);
		}
		else if (fieldCleaningPolicy == FieldActionPolicy.USE_FUNCTION) {
			String cleaningFunctionName
				= originalRIFDataType.getCleaningFunctionName();
			dataCleaningPolicyEditingPanel.setUseFunctionPolicy(cleaningFunctionName);
		}
		else {
			dataCleaningPolicyEditingPanel.setDoNothingPolicy();
		}

		FieldActionPolicy fieldValidationPolicy
			= originalRIFDataType.getFieldValidationPolicy();
		if (fieldValidationPolicy == FieldActionPolicy.USE_RULES) {
			ArrayList<ValidationRule> validationRules
				= originalRIFDataType.getValidationRules();
			dataValidationPolicyEditingPanel.setValidationRulesPolicy(validationRules);
		}
		else if (fieldValidationPolicy == FieldActionPolicy.USE_FUNCTION) {
			String validationFunctionName
				= originalRIFDataType.getValidationFunctionName();
			dataValidationPolicyEditingPanel.setUseFunctionPolicy(validationFunctionName);
		}
		else if (fieldValidationPolicy == FieldActionPolicy.DO_NOTHING) {
			dataValidationPolicyEditingPanel.setDoNothingPolicy();			
		}
		else {
			assert false;
		}		
	}
	
	public RIFDataType getDataType() {
		return originalRIFDataType;
	}
	
	public boolean saveChanges() 
		throws RIFServiceException {
		
		RIFDataType rifDataTypeFromForm = createDataTypeFromForm();
		
		if (originalRIFDataType.hasIdenticalContents(rifDataTypeFromForm)) {
			//no changes need to be made and we can assume that
			//it is valid
			return false;
		}
		else {
			System.out.println("Data type has saved changes");
			
			
			//changes have been made.  Ask users if they want to save changes
			String message
				= RIFDataLoaderToolMessages.getMessage(
						"general.saveChanges.message");
			String title
				= RIFDataLoaderToolMessages.getMessage(
					"general.saveChanges.title");
			int result 
				= JOptionPane.showConfirmDialog(
					parentComponent,
					message,
					title,
					JOptionPane.YES_NO_OPTION);
			if (result != JOptionPane.YES_OPTION) {
				return false;
			}

			//The original version and the version provided through the GUI differ.
			//Assume that the original was valid but validate the one provided through
			//the form
			rifDataTypeFromForm.checkErrors();

			//Changes were made and the result is a valid 
			RIFDataType.copyInto(
				rifDataTypeFromForm, 
				originalRIFDataType);
			
			return true;
		}		
	}
	
	private RIFDataType createDataTypeFromForm() {
		RIFDataType rifDataType = RIFDataType.newInstance();
		rifDataType.setIdentifier(originalRIFDataType.getIdentifier());
		rifDataType.setName(nameTextField.getText().trim());
		rifDataType.setDescription(descriptionTextField.getText().trim());
		
		//set values in the cleaning policy panel
		FieldActionPolicy fieldCleaningActionPolicy
			= dataCleaningPolicyEditingPanel.getFieldActionPolicy();
		rifDataType.setFieldCleaningPolicy(fieldCleaningActionPolicy);
		if (fieldCleaningActionPolicy == FieldActionPolicy.USE_RULES) {
			ArrayList<CleaningRule> cleaningRules
				= dataCleaningPolicyEditingPanel.getCleaningRules();
			rifDataType.setCleaningRules(cleaningRules);
		}
		else if (fieldCleaningActionPolicy == FieldActionPolicy.USE_FUNCTION) {
			String cleaningFunctionName
				= dataCleaningPolicyEditingPanel.getCleaningFunctionName();
			rifDataType.setCleaningFunctionName(cleaningFunctionName);
		}
		
		//set values in the validation policy panel
		FieldActionPolicy fieldValidationPolicy
			= dataValidationPolicyEditingPanel.getFieldActionPolicy();
		rifDataType.setFieldValidationPolicy(fieldValidationPolicy);
		if (fieldValidationPolicy == FieldActionPolicy.USE_RULES) {
			ArrayList<ValidationRule> validationRules
				= dataValidationPolicyEditingPanel.getValidationRules();

			rifDataType.setValidationRules(validationRules);
		}
		else if (fieldValidationPolicy == FieldActionPolicy.USE_FUNCTION) {
			String validationFunctionName
				= dataValidationPolicyEditingPanel.getFunctionName();
			rifDataType.setValidationFunctionName(validationFunctionName);
		}
			
		return rifDataType;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void validateForm() 
		throws RIFServiceException {
		
		RIFDataType rifDataTypeFromForm = createDataTypeFromForm();
		rifDataTypeFromForm.checkErrors();
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	//Interface: Action Listener
	public void actionPerformed(final ActionEvent event) {
		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


