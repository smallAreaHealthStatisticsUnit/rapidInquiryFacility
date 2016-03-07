package rifGenericLibrary.presentationLayer;


import rifGenericLibrary.system.*;


import rifGenericLibrary.util.ListItemNameUtility;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
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

public class CopyListItemNameEditorDialog 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList existingListItemNames;
	private String fieldName;
	private boolean isCancelled;

	private UserInterfaceFactory userInterfaceFactory;
	private JTextField listItemNameTextField;
	private JDialog dialog;	
	private OKCloseButtonPanel okCloseButtonPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CopyListItemNameEditorDialog(
		final UserInterfaceFactory userInterfaceFactory,
		final String dialogTitle,
		final String instructionsText,
		final String fieldName,
		final ArrayList<String> existingListItemNames) {

		isCancelled = false;
		this.userInterfaceFactory = userInterfaceFactory;
		this.existingListItemNames = existingListItemNames;
		this.fieldName = fieldName;
		
		okCloseButtonPanel 
			= new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		
		dialog = userInterfaceFactory.createDialog(dialogTitle);
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		if (instructionsText != null) {
			JLabel instructionsLabel
				= userInterfaceFactory.createInstructionLabel(instructionsText);
			panel.add(instructionsLabel, panelGC);			
			panelGC.gridy++;
		}

		panel.add(createFieldPanel(fieldName), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(okCloseButtonPanel.getPanel(), panelGC);
				
		dialog.getContentPane().add(panel);
		dialog.setModal(true);
		dialog.setMinimumSize(new Dimension(300, 120));
		dialog.pack();
	}
	
	private JPanel createFieldPanel(
		final String fieldLabelText) {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		JLabel fieldLabel
			= userInterfaceFactory.createLabel(fieldLabelText);
		panel.add(fieldLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		listItemNameTextField = userInterfaceFactory.createTextField();
		panel.add(listItemNameTextField, panelGC);
				
		return panel;		
	}
	
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void show() {	
		String candidateName
			= ListItemNameUtility.generateUniqueListItemName(
				fieldName, 
				existingListItemNames);
		listItemNameTextField.setText(candidateName);
		
		dialog.setVisible(true);
	}
	
	public String getFieldName() {
		return listItemNameTextField.getText().trim();
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	private void ok() {
		
		try {			
			validateForm();
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
	private void validateForm() 
		throws RIFServiceException {

		String candidateListItemName = getFieldName();		
		ListItemNameUtility.checkListDuplicate(
			candidateListItemName, 
			existingListItemNames);
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


