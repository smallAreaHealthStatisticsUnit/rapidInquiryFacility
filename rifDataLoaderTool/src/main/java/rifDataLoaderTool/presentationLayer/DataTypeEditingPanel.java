package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.CustomRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.AbstractRIFDataType;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
	
	private UserInterfaceFactory userInterfaceFactory;
	private JTextField identifierTextField;
	private JTextField nameTextField;
	private JTextField descriptionTextField;
	
	private DataCleaningPolicyEditingPanel dataCleaningPolicyEditingPanel;
	private DataValidationPolicyEditingPanel dataValidationPolicyEditingPanel;
	
	private AbstractRIFDataType originalRIFDataType;
	private CustomRIFDataType currentRIFDataType;
	
	private boolean isEditable;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataTypeEditingPanel(final UserInterfaceFactory userInterfaceFactory) {
		this.userInterfaceFactory = userInterfaceFactory;
		isEditable = true;
		
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
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String identifierLabelText
			= RIFDataLoaderToolMessages.getMessage("rifDataType.identifier.label");
		JLabel identifierLabel
			= userInterfaceFactory.createLabel(identifierLabelText);
		panel.add(identifierLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		identifierTextField = userInterfaceFactory.createTextField();
		panel.add(identifierTextField, panelGC);
		
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
	
	public void setData(final AbstractRIFDataType originalRIFDataType) {
		this.originalRIFDataType = originalRIFDataType;
	
		this.currentRIFDataType = RIFDataTypeFactory.createCopy(originalRIFDataType);
		
		identifierTextField.setText(originalRIFDataType.getIdentifier());
		nameTextField.setText(originalRIFDataType.getName());
		descriptionTextField.setText(originalRIFDataType.getDescription());
			
		if (originalRIFDataType instanceof CustomRIFDataType) {	
			userInterfaceFactory.setReadOnlyAppearance(
				identifierTextField,
				false);
			userInterfaceFactory.setReadOnlyAppearance(
				nameTextField,
				false);
			userInterfaceFactory.setReadOnlyAppearance(
				descriptionTextField,
				false);			

			dataCleaningPolicyEditingPanel.setData(currentRIFDataType, true);
			dataValidationPolicyEditingPanel.setData(currentRIFDataType, true);		
		}
		else {
			userInterfaceFactory.setReadOnlyAppearance(
				identifierTextField,
				true);
			userInterfaceFactory.setReadOnlyAppearance(
				nameTextField,
				true);
			userInterfaceFactory.setReadOnlyAppearance(
				descriptionTextField,
				true);	

			dataCleaningPolicyEditingPanel.setData(currentRIFDataType, false);
			dataValidationPolicyEditingPanel.setData(currentRIFDataType, false);				
		}
	}
	
	private void populateForm(final boolean isEditable) {
				
		identifierTextField.setText(currentRIFDataType.getIdentifier());
		nameTextField.setText(currentRIFDataType.getName());
		descriptionTextField.setText(currentRIFDataType.getDescription());
		
		identifierTextField.setEditable(isEditable);
		nameTextField.setEditable(isEditable);
		descriptionTextField.setEditable(isEditable);				
	}
	
	public void saveChanges() {
		dataCleaningPolicyEditingPanel.saveChanges();
		dataValidationPolicyEditingPanel.saveChanges();		
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


