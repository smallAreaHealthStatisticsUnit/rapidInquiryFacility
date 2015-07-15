package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;


import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.ASCIITextRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.AgeRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.DateRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.DoubleRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.ICDCodeRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.NHSNumberRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.SexRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.UKPostalCodeRIFDataType;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;
import rifDataLoaderTool.dataStorageLayer.AbstractDataLoaderService;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;
import rifDataLoaderTool.fileFormats.DataSetFieldConfigurationHandler;

import javax.swing.*;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

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

public final class CleanDataActivityStepDialog 
	extends AbstractDataLoaderToolDialog
	implements ListSelectionListener {

	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	private RIFDataLoaderToolShutdownManager shutDownManager;
	
	private JEditorPane cleaningInformationEditorPane;
	
	private CleaningConfigurationTable cleaningConfigurationTable;
	private JButton editPropertiesButton;
	private JButton resetPropertiesButton;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleanDataActivityStepDialog(
		final RIFDataLoaderToolSession session) {
		
		super(session);
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderActivityStep.clean.label");
		setDialogTitle(dialogTitle);	
		setSize(700, 500);

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("cleanDataActivityStepDialog.instructions");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 0.7;
		panel.add(createCleaningPropertiesTablePanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.weighty = 0.3;
		panel.add(createCleaningInformationPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(getOKCloseButtonPanel(), panelGC);
		
		setMainPanel(panel);		
		
		JDialog dialog = getDialog();
		shutDownManager
			= new RIFDataLoaderToolShutdownManager(dialog, session);
	}

	private JPanel createCleaningPropertiesTablePanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
	
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String cleaningPropertiesLabelText
			= RIFDataLoaderToolMessages.getMessage("cleanDataActivityStepDialog.cleaningPropertiesTable.label");
		JLabel cleaningPropertiesLabel
			= userInterfaceFactory.createLabel(cleaningPropertiesLabelText);
		panel.add(cleaningPropertiesLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;

		cleaningConfigurationTable 
			= new CleaningConfigurationTable(userInterfaceFactory);
		cleaningConfigurationTable.addListSelectionListener(this);
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(cleaningConfigurationTable.getTable());
		panel.add(scrollPane, panelGC);

		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weighty = 0;
		panel.add(createCleaningPropertyButtonPanel(), panelGC);
		
		return panel;
	}
	
	private JPanel createCleaningPropertyButtonPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();

		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		String editPropertiesButtonText
			= RIFDataLoaderToolMessages.getMessage("cleanDataActivityStepDialog.editProperties.label");
		editPropertiesButton
			= userInterfaceFactory.createButton(editPropertiesButtonText);
		editPropertiesButton.addActionListener(this);
		panel.add(editPropertiesButton, panelGC);
		
		panelGC.gridx++;
		String resetPropertiesButtonText
			= RIFDataLoaderToolMessages.getMessage("cleanDataActivityStepDialog.resetProperties.label");
		resetPropertiesButton
			= userInterfaceFactory.createButton(resetPropertiesButtonText);
		resetPropertiesButton.addActionListener(this);
		panel.add(resetPropertiesButton, panelGC);
		
		return panel;
	}
	
	
	/*
	 * This panel changes the information it provides depending on the
	 * currently selected row in the cleaning properties table
	 */
	private JPanel createCleaningInformationPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String cleaningInformationPanelLabelText
			= RIFDataLoaderToolMessages.getMessage("cleanDataActivityStepDialog.cleaningInformation.label");
		JLabel cleaningInformationPanelLabel
			= userInterfaceFactory.createLabel(cleaningInformationPanelLabelText);
		panel.add(cleaningInformationPanelLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		cleaningInformationEditorPane
			= userInterfaceFactory.createHTMLEditorPane();
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(cleaningInformationEditorPane);
		panel.add(scrollPane, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setData(final DataSetConfiguration dataSetConfiguration) {
		cleaningConfigurationTable.setData(dataSetConfiguration);
	}
	
	private void editSelectedCleaningProperty() {		
		DataSetFieldConfiguration dataSetFieldConfiguration		
			= cleaningConfigurationTable.getSelectedDataSetFieldConfiguration();
		
		RIFDataLoaderToolSession session
			= getSession();
		CleaningFieldConfigurationEditorDialog dialog
			= new CleaningFieldConfigurationEditorDialog(session);
		dialog.setData(dataSetFieldConfiguration);
		dialog.show();
	}
	
	private void resetProperties() {
		
	}
	
	private void ok() {
		hide();
		shutDown();		
	}
	
	private void close() {
		hide();
		shutDown();
	}
	
	private void shutDown() {
		RIFDataLoaderToolShutdownManager shutDownManager
			= new RIFDataLoaderToolShutdownManager(
				getDialog(), 
				getSession());
		shutDownManager.shutDown();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: ActionListener
	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
		
		if (button == editPropertiesButton) {
			editSelectedCleaningProperty();
		}
		else if (button == resetPropertiesButton) {
			resetProperties();
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
	
	//Interface: List Selection Listener
	public void valueChanged(ListSelectionEvent event) {
		
		DataSetFieldConfiguration dataSetFieldConfiguration
			= cleaningConfigurationTable.getSelectedDataSetFieldConfiguration();
		
		DataSetFieldConfigurationHandler cleaningFieldConfigurationHandler
			= new DataSetFieldConfigurationHandler();
		String fieldInformation
			= cleaningFieldConfigurationHandler.getHTML(
				dataSetFieldConfiguration,
				WorkflowState.CLEAN);
		
		cleaningInformationEditorPane.setText(fieldInformation);
		cleaningInformationEditorPane.setCaretPosition(0);
		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


