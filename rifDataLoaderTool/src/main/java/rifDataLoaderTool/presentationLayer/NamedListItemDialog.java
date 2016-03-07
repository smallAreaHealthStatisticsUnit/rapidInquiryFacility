package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFServiceException;
import rifDataLoaderTool.system.RIFDataLoaderToolError;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.text.Collator;
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

public class NamedListItemDialog 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;
	private JDialog dialog;
	
	private JTextField nameTextField;
	private OKCloseButtonPanel okCloseButtonPanel;
	
	private ArrayList<String> existingNames;
	
	private boolean isCancelled;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public NamedListItemDialog(
		final UserInterfaceFactory userInterfaceFactory,
		final String title,
		final ArrayList<String> existingNames) {
		
		this.userInterfaceFactory = userInterfaceFactory;
		this.existingNames = existingNames;
	
		isCancelled = false;
		
		dialog
			= userInterfaceFactory.createDialog(title);
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		panel.add(createFieldPanel(), panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		okCloseButtonPanel
			= new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), panelGC);
		
		dialog.getContentPane().add(panel);
		
		dialog.setSize(300, 300);
		dialog.setModal(true);
	}

	private JPanel createFieldPanel() {
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String nameFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("copyItemDialog.name.label");
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
	public void show() {
		dialog.setVisible(true);
	}
	
	public String getCandidateName() {
		return nameTextField.getText().trim();
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	private void ok() {
		try {
			checkErrors();
			
			isCancelled = false;
			dialog.setVisible(false);			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}
		
	}
	
	private void close() {
		isCancelled = true;
		dialog.setVisible(false);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void checkErrors() 
		throws RIFServiceException {
		
		Collator collator
			= RIFDataLoaderToolMessages.getCollator();
		
		String currentNameCandidate
			= nameTextField.getText().trim();
		for (String existingName : existingNames) {
			if (collator.equals(existingName, currentNameCandidate)) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"copyItemDialog.error.duplicateCopiedName",
						currentNameCandidate);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFDataLoaderToolError.DUPLICATE_COPIED_ITEM_NAME, 
						errorMessage);
				throw rifServiceException;
			}
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================
	//Interface: Action Listener
	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();
		
		if (okCloseButtonPanel.isOKButton(button)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


