package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.system.DataLoaderToolSession;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.DataLoaderToolConfiguration;
import rifDataLoaderTool.businessConceptLayer.ConfigurationHints;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Observable;


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

public class ConfigurationHintsLoadingPanel 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;
	private JPanel panel;
	private DataLoaderToolSession session;
	private DataLoaderToolChangeManager changeManager;
	
	private JLabel panelTitleLabel;
	private JTextField messageTextField;
	private JButton editButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ConfigurationHintsLoadingPanel(	
		final DataLoaderToolSession session,
		final DataLoaderToolChangeManager changeManager) {

		this.session = session;
		userInterfaceFactory = session.getUserInterfaceFactory();
		this.changeManager = changeManager;

		buildUI();		
		updateConfigurationHintsSummary();
	}
	
	private void buildUI() {		
		panel = userInterfaceFactory.createPanel();
		
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String configurationHintsLabelText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication2.configurationHints.label");		
		panelTitleLabel
			= userInterfaceFactory.createLabel(configurationHintsLabelText);
		userInterfaceFactory.setBoldFont(panelTitleLabel);
		panel.add(panelTitleLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		messageTextField 
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(messageTextField, panelGC);		

		panelGC.gridx++;		
		editButton 
			= userInterfaceFactory.createEditButton();
		editButton.addActionListener(this);
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panel.add(editButton, panelGC);
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void refresh() {
		updateConfigurationHintsSummary();
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	private void updateConfigurationHintsSummary() {
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= session.getDataLoaderToolConfiguration();
		ConfigurationHints configurationHints
			= dataLoaderToolConfiguration.getConfigurationHints();			
		int numberOfDataSetHints 
			= configurationHints.getDataSetConfigurationHints().size();
		int numberOfDataSetFieldHints
			= configurationHints.getDataSetFieldConfigurationHints().size();
		int totalHints = numberOfDataSetHints + numberOfDataSetFieldHints;
		
		if (totalHints == 0) {
			String message
				= RIFDataLoaderToolMessages.getMessage(
					"configurationHintsLoadingPanel.hintsStatusMessage0");
			messageTextField.setText(message);
		}
		else if (totalHints == 1) {
			String message
				= RIFDataLoaderToolMessages.getMessage(
					"configurationHintsLoadingPanel.hintsStatusMessage1");
			messageTextField.setText(message);
		}
		else {
			String message
				= RIFDataLoaderToolMessages.getMessage(
					"configurationHintsLoadingPanel.hintsStatusMessageN",
					String.valueOf(totalHints));
			messageTextField.setText(message);
		}
	}
	
	private void edit() {

		ConfigurationHintsEditorDialog dialog
			= new ConfigurationHintsEditorDialog(session);
		//@TODO KLG
		//needs to be tidied up.  It can get config hints
		//from the session object so there is not a need to 
		//pass explicitly.  Not sure whether we need an explicit
		//set data for this one.
		ConfigurationHints configurationHints
			= session.getDataLoaderToolConfiguration().getConfigurationHints();
		int originalTotalHints = configurationHints.getTotalHints();
		dialog.setData(configurationHints);
		dialog.show();
		int revisedTotalHints = configurationHints.getTotalHints();
		updateConfigurationHintsSummary();
		
		if (originalTotalHints != revisedTotalHints) {
			changeManager.indicateSaveChanges();
		}
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(
		final ActionEvent event) {
		
		Object button = event.getSource();
		if (button == editButton) {
			edit();
		}
	}
	
	//Interface: Observer
	public void update(
		final Observable observable,
		final Object object) {
			
		DataLoadingOrder currentState
			= (DataLoadingOrder) object;
		if (currentState.getStepNumber() >= DataLoadingOrder.HEALTH_THEMES_SPECIFIED.getStepNumber()) {
			editButton.setEnabled(true);
		}
		else {
			editButton.setEnabled(false);
		}
	}		
	// ==========================================
	// Section Override
	// ==========================================

}


