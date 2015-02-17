package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifDataLoaderTool.businessConceptLayer.*;
import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.UserInterfaceFactory;
import rifGenericUILibrary.OrderedListComboBox;

import rifServices.system.RIFServiceException;

import javax.swing.border.LineBorder;
import javax.swing.*;

import java.awt.*;
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

public final class CleaningFieldConfigurationEditorDialog 
	extends AbstractDataLoaderToolDialog {


	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private TableFieldCleaningConfiguration tableFieldCleaningConfiguration;
	
	private JLabel loadDataFieldNameLabel;	
	private JTextField preferredCleanedFieldNameTextField;
	
	private OrderedListComboBox dataTypeComboBox;
	private JTextField limitsTextField;
	
	private CleaningRuleEditingPanel cleaningRuleEditingPanel;
	
	private JButton exploreVarianceButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleaningFieldConfigurationEditorDialog(
		final RIFDataLoaderToolSession session) {

		super(session);
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.title");
		setDialogTitle(dialogTitle);
		setSize(400, 400);
		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		panel.add(createBasicInformationPanel(), panelGC);
		panelGC.gridy++;
		panel.add(createPropertiesAndConstraintsPanel(), panelGC);
		
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		
		cleaningRuleEditingPanel 
			= new CleaningRuleEditingPanel(getSession());
		panel.add(cleaningRuleEditingPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createBottomButtonPanel(), panelGC);
		
		setMainPanel(panel);
	}

	private JPanel createBasicInformationPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String basicInformationLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.basicInformation.label");		
		JLabel basicInformationLabel
			= userInterfaceFactory.createLabel(basicInformationLabelText);
		panel.add(basicInformationLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		String loadDataFieldNameText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.loadDataFieldName.label");
		panel.add(
			userInterfaceFactory.createLabel(loadDataFieldNameText),
			panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		loadDataFieldNameLabel = userInterfaceFactory.createLabel("");
		panel.add(loadDataFieldNameLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;		
		String preferredCleanedFieldNameText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.preferredCleanedFieldName.label");
		JLabel preferredCleanedFieldNameLabel
			= userInterfaceFactory.createLabel(preferredCleanedFieldNameText);
		panel.add(preferredCleanedFieldNameLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		preferredCleanedFieldNameTextField
			= userInterfaceFactory.createTextField();
		panel.add(preferredCleanedFieldNameTextField, panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
	}

	private JPanel createPropertiesAndConstraintsPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String propertiesPanelText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.properties.label");
		JLabel propertiesPanelLabel
			= userInterfaceFactory.createLabel(propertiesPanelText);
		panel.add(
			propertiesPanelLabel, 
			panelGC);
		
		panelGC.gridy++;
		
		String rifDataTypeLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.dataType.label");
		JLabel rifDataTypeLabel
			= userInterfaceFactory.createLabel(rifDataTypeLabelText);
		panel.add(rifDataTypeLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		dataTypeComboBox
			= new OrderedListComboBox(getUserInterfaceFactory());
		dataTypeComboBox.addListItem(AgeRIFDataType.newInstance());
		dataTypeComboBox.addListItem(ASCIITextRIFDataType.newInstance());
		dataTypeComboBox.addListItem(DateRIFDataType.newInstance());
		dataTypeComboBox.addListItem(DoubleRIFDataType.newInstance());
		dataTypeComboBox.addListItem(ICDCodeRIFDataType.newInstance());
		dataTypeComboBox.addListItem(IntegerRIFDataType.newInstance());
		dataTypeComboBox.addListItem(NHSNumberRIFDataType.newInstance());
		dataTypeComboBox.addListItem(TextRIFDataType.newInstance());
		dataTypeComboBox.addListItem(UKPostalCodeRIFDataType.newInstance());
		dataTypeComboBox.addListItem(YearRIFDataType.newInstance());
		dataTypeComboBox.selectFirstItem();
		panel.add(dataTypeComboBox.getComboBox(), panelGC);
				
		panelGC.gridy++;
		panelGC.gridx = 0;
		String limitsLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.limits.label");
		JLabel limitsLabel
			= userInterfaceFactory.createLabel(limitsLabelText);
		panel.add(limitsLabel, panelGC);
		panelGC.gridx++;
		limitsTextField = userInterfaceFactory.createTextField();
		panel.add(limitsTextField, panelGC);

		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;		
	}
	
	private JPanel createDataCleaningRulesPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String dataCleaningRulesLabelText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.dataCleaningRules.label");
		JLabel dataCleaningRulesLabel
			= userInterfaceFactory.createLabel(dataCleaningRulesLabelText);
		panel.add(dataCleaningRulesLabel, panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;		
	}
	
	private JPanel createBottomButtonPanel() {
		UserInterfaceFactory userInterfaceFactory 
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createBorderLayoutPanel();
		
		String exploreVarianceButtonText
			= RIFDataLoaderToolMessages.getMessage("cleaningConfigurationFieldEditorDialog.button.exploreVariance");
		exploreVarianceButton
			= userInterfaceFactory.createButton(exploreVarianceButtonText);
		exploreVarianceButton.addActionListener(this);
		panel.add(exploreVarianceButton, BorderLayout.WEST);
		
		panel.add(getOKCloseButtonPanel(), BorderLayout.EAST);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setData(final TableFieldCleaningConfiguration tableFieldCleaningConfiguration) {
		this.tableFieldCleaningConfiguration = tableFieldCleaningConfiguration;
		RIFDataTypeInterface rifDataType = tableFieldCleaningConfiguration.getRifDataType();
		cleaningRuleEditingPanel.setData(rifDataType);
	}
	
	private void exploreVariance() {
		FieldVarianceDialog fieldVarianceDialog
			= new FieldVarianceDialog(getSession());
		
		try {
			fieldVarianceDialog.setData(tableFieldCleaningConfiguration);
			fieldVarianceDialog.show();			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
		}
	}
	
	private void ok() {
		try {
			validateForm();			
			

			hide();			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(),
				rifServiceException.getErrorMessages());
		}
	}
	
	private void close() {
		hide();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void validateForm() 
		throws RIFServiceException {
		
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: Action Listener
	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
		
		if (button == exploreVarianceButton) {
			exploreVariance();
		}
		else if (isOKButton(button)) {
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


