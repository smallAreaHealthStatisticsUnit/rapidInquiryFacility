package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;

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

public class DataTypesLoadingPanel 
	implements ActionListener, 
	Observer {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final Color populatedColour = new Color(0, 128, 0);
	private static final Color disabledColour = Color.BLACK;
	
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

	public DataTypesLoadingPanel(	
		final DataLoaderToolSession session,
		final DataLoaderToolChangeManager changeManager) {

		this.session = session;
		userInterfaceFactory = session.getUserInterfaceFactory();
		this.changeManager = changeManager;

		buildUI();		
		updateSummary();
	}
	
	private void buildUI() {		
		panel = userInterfaceFactory.createPanel();
		
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String configurationHintsLabelText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication2.dataTypes.label");
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
		updateSummary();
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	private void updateSummary() {
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= session.getDataLoaderToolConfiguration();
		
		RIFDataTypeFactory dataTypeFactory
			= dataLoaderToolConfiguration.getRIFDataTypeFactory();
		int numberOfDataTypes
			= dataTypeFactory.getRegisteredDataTypes().size();
		String message
			= RIFDataLoaderToolMessages.getMessage(
				"dataTypesLoadingPanel.statusMessage",
				String.valueOf(numberOfDataTypes));
		messageTextField.setText(message);
	}
	
	private void edit() {
		DataTypeEditorDialog dataTypeEditorDialog
			= new DataTypeEditorDialog(session);		
		dataTypeEditorDialog.show();
		
		if (dataTypeEditorDialog.isCancelled()) {
			return;
		}

		updateSummary();
		
		//KLG: @TODO We should have a more nuanced indicator for
		//save changes.  But for now, assume that if they pressed the
		//"OK" button, then prompt to save changes
		changeManager.indicateSaveChanges();
	}
	
	private void setEnable(final boolean isEnabled) {
		
		if (isEnabled == true) {
			panelTitleLabel.setForeground(populatedColour);				
		}
		else {
			panelTitleLabel.setForeground(disabledColour);				
		}
		
		editButton.setEnabled(isEnabled);
		
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
			setEnable(true);
		}
		else {
			setEnable(false);
		}
	}		
	// ==========================================
	// Section Override
	// ==========================================

}


