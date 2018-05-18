package org.sahsu.rif.generic.presentation;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.text.Collator;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
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

public class NamedListItemDialog 
	extends OKCloseButtonDialog {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private JTextField nameTextField;
	private ArrayList<String> existingNames;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public NamedListItemDialog(
		final UserInterfaceFactory userInterfaceFactory,
		final String title,
		final ArrayList<String> existingNames) {
		
		super(userInterfaceFactory);
		this.existingNames = existingNames;
	
		setDialogTitle(title);
		setMainPanel(createMainPanel());
		setMainPanelFillConstraints(
			GridBagConstraints.HORIZONTAL, 
			1, 
			0);
		buildUI();
		setSize(300, 100);
		
	}

	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String nameFieldLabelText
			= GENERIC_MESSAGES.getMessage("nameListItemDialog.name.label");
		JLabel nameFieldLabel
			= userInterfaceFactory.createLabel(nameFieldLabelText);
		panel.add(nameFieldLabel, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		nameTextField
			 = userInterfaceFactory.createTextField();
		panel.add(nameTextField, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getCandidateName() {
		return nameTextField.getText().trim();
	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void checkErrors()
			throws RIFServiceException {
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		
		String currentNameCandidate
			= nameTextField.getText().trim();
		for (String existingName : existingNames) {
			if (collator.equals(existingName, currentNameCandidate)) {
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"nameListItemDialog.error.duplicateCopiedName",
						currentNameCandidate);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFGenericLibraryError.DUPLICATE_ITEM_NAME,
						errorMessage);
				throw rifServiceException;
			}
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	//Interface: Action Listener
	@Override
	public void actionPerformed(final ActionEvent event) {
		
		performOKCloseActions(event);
	}
	
	// ==========================================
	// Section Override
	// ==========================================

	@Override
	public void okAction() 
		throws RIFServiceException {

		checkErrors();
	}

	
}


