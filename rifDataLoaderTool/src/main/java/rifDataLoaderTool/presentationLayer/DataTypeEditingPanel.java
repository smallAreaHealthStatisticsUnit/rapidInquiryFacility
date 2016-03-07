package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.RIFDataType;
import rifDataLoaderTool.businessConceptLayer.RIFFieldCleaningPolicy;
import rifDataLoaderTool.businessConceptLayer.CleaningRule;
import rifDataLoaderTool.businessConceptLayer.RIFFieldValidationPolicy;
import rifDataLoaderTool.businessConceptLayer.ValidationRule;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;

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

public class DataTypeEditingPanel 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private JPanel panel;
	private Component parentComponent;
	
	private UserInterfaceFactory userInterfaceFactory;
	private JTextField nameTextField;
	private JTextField descriptionTextField;
	
	private DataCleaningPolicyEditingPanel dataCleaningPolicyEditingPanel;
	private DataValidationPolicyEditingPanel dataValidationPolicyEditingPanel;
	
	private RIFDataType originalRIFDataType;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataTypeEditingPanel(
		final UserInterfaceFactory userInterfaceFactory,
		final Component parentComponent) {
		
		this.userInterfaceFactory = userInterfaceFactory;
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
			= new DataCleaningPolicyEditingPanel(userInterfaceFactory);
		policyPanel.add(
			dataCleaningPolicyEditingPanel.getPanel(), 
			policyPanelGC);
		
		policyPanelGC.gridx++;
		dataValidationPolicyEditingPanel
			= new DataValidationPolicyEditingPanel(userInterfaceFactory);
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
		
		System.out.println("DTEP - setData original data type identifier=="+originalRIFDataType.getIdentifier()+"==");
		
		this.originalRIFDataType = originalRIFDataType;
		
		nameTextField.setText(originalRIFDataType.getName());
		descriptionTextField.setText(originalRIFDataType.getDescription());
			
		boolean isEditable = true;
		if (originalRIFDataType.isReservedDataType()) {
			isEditable = false;
		}

		
		userInterfaceFactory.setEditableAppearance(
			nameTextField,
			isEditable);
		userInterfaceFactory.setEditableAppearance(
			descriptionTextField,
			isEditable);			

		RIFFieldCleaningPolicy fieldCleaningPolicy
			= originalRIFDataType.getFieldCleaningPolicy();
		if (fieldCleaningPolicy == RIFFieldCleaningPolicy.CLEANING_RULES) {
			ArrayList<CleaningRule> cleaningRules
				= originalRIFDataType.getCleaningRules();
			dataCleaningPolicyEditingPanel.setCleaningRulesPolicy(cleaningRules);
		}
		else if (fieldCleaningPolicy == RIFFieldCleaningPolicy.CLEANING_FUNCTION) {
			String cleaningFunctionName
				= originalRIFDataType.getCleaningFunctionName();
			dataCleaningPolicyEditingPanel.setCleaningFunctionPolicy(cleaningFunctionName);
		}
		else {
			dataCleaningPolicyEditingPanel.setNoCleaningPolicy();
		}

		RIFFieldValidationPolicy fieldValidationPolicy
			= originalRIFDataType.getFieldValidationPolicy();
		if (fieldValidationPolicy == RIFFieldValidationPolicy.VALIDATION_RULES) {
			ArrayList<ValidationRule> validationRules
				= originalRIFDataType.getValidationRules();
			System.out.println("DataTypeEditingPanel setting validation rules=="+validationRules.size()+"==");
			dataValidationPolicyEditingPanel.setValidationRulesPolicy(validationRules);
		}
		else if (fieldValidationPolicy == RIFFieldValidationPolicy.VALIDATION_FUNCTION) {
			String validationFunctionName
				= originalRIFDataType.getValidationFunctionName();
			dataValidationPolicyEditingPanel.setValidationFunctionPolicy(validationFunctionName);
		}
		else {
			dataValidationPolicyEditingPanel.setNoValidationPolicy();
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
			//changes have been made.  Ask users if they want to save changes
			String message
				= RIFDataLoaderToolMessages.getMessage(
						"databaseTypeEditorDialog.saveChanges.message");
			String title
				= RIFDataLoaderToolMessages.getMessage(
					"databaseTypeEditorDialog.saveChanges.title");
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

			System.out.println("BEFORE form description=="+rifDataTypeFromForm.getDescription()+"==");
			System.out.println("BEFORE original description=="+originalRIFDataType.getDescription()+"==");
			
			//Changes were made and the result is a valid 
			RIFDataType.copyInto(
				rifDataTypeFromForm, 
				originalRIFDataType);
			
			System.out.println("AFTER original description=="+originalRIFDataType.getDescription()+"==");			
			return true;
		}		
	}
	
	private RIFDataType createDataTypeFromForm() {
		RIFDataType rifDataType = RIFDataType.newInstance();
		rifDataType.setIdentifier(originalRIFDataType.getIdentifier());
		rifDataType.setName(nameTextField.getText().trim());
		rifDataType.setDescription(descriptionTextField.getText().trim());
		
		//set values in the cleaning policy panel
		RIFFieldCleaningPolicy rifFieldCleaningPolicy
			= dataCleaningPolicyEditingPanel.getFieldCleaningPolicy();
		rifDataType.setFieldCleaningPolicy(rifFieldCleaningPolicy);
		if (rifFieldCleaningPolicy == RIFFieldCleaningPolicy.CLEANING_RULES) {
			ArrayList<CleaningRule> cleaningRules
				= dataCleaningPolicyEditingPanel.getCleaningRules();
			rifDataType.setCleaningRules(cleaningRules);
		}
		else if (rifFieldCleaningPolicy == RIFFieldCleaningPolicy.CLEANING_FUNCTION) {
			String cleaningFunctionName
				= dataCleaningPolicyEditingPanel.getCleaningFunctionName();
			rifDataType.setCleaningFunctionName(cleaningFunctionName);
		}

		System.out.println("DataTypeEditingPanel getting validation rules==111==");
		
		//set values in the validation policy panel
		RIFFieldValidationPolicy rifFieldValidationPolicy
			= dataValidationPolicyEditingPanel.getFieldValidationPolicy();
		rifDataType.setFieldValidationPolicy(rifFieldValidationPolicy);
		if (rifFieldValidationPolicy == RIFFieldValidationPolicy.VALIDATION_RULES) {
			ArrayList<ValidationRule> validationRules
				= dataValidationPolicyEditingPanel.getValidationRules();
			System.out.println("DataTypeEditingPanel getting validation rules=="+validationRules.size()+"==");

			rifDataType.setValidationRules(validationRules);
		}
		else if (rifFieldValidationPolicy == RIFFieldValidationPolicy.VALIDATION_FUNCTION) {
			String validationFunctionName
				= dataValidationPolicyEditingPanel.getValidationFunctionName();
			rifDataType.setValidationFunctionName(validationFunctionName);
		}
		else {
			
		}
			
		return rifDataType;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	
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


