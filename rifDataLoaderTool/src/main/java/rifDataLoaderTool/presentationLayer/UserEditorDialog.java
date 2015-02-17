package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.RIFUserRole;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifServices.system.RIFServiceMessages;
import rifServices.businessConceptLayer.User;

import javax.swing.*;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



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

public final class UserEditorDialog 
	extends AbstractDataLoaderToolDialog 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private User user;
	private String password;
	private RIFUserRole userRole;
	
	private boolean isNewUser;

	private JTextField userIDTextField;
	private JTextField passwordTextField;
	private JComboBox userRoleComboBox;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public UserEditorDialog(
		final RIFDataLoaderToolSession session) {

		super(session);
		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		
		JPanel panel = userInterfaceFactory.createPanel();
		
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		String userIDLabelText
			= RIFServiceMessages.getMessage("user.userID.label");
		JLabel userIDLabel
			= userInterfaceFactory.createLabel(userIDLabelText);
		panel.add(userIDLabel, panelGC);
		panelGC.gridx++;
		userIDTextField = userInterfaceFactory.createTextField();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(userIDTextField, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		
		String passwordLabelText
			= RIFServiceMessages.getMessage("user.password.label");
		JLabel passwordLabel 
			= userInterfaceFactory.createLabel(passwordLabelText);
		panel.add(passwordLabel, panelGC);
		panelGC.gridx++;
		passwordTextField
			= userInterfaceFactory.createTextField();
		panel.add(passwordTextField, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		String userRolesLabelText
			= RIFDataLoaderToolMessages.getMessage("userRole.label");
		JLabel userRoleLabel
			= userInterfaceFactory.createLabel(userRolesLabelText);
		panel.add(userRoleLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;		
		String[] userRoleNames = RIFUserRole.getUserRoleNames();
		userRoleComboBox
			= userInterfaceFactory.createComboBox(userRoleNames);
		panel.add(userRoleComboBox, panelGC);
		
		setMainPanel(panel);
		setSize(300, 300);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setData(
		final User user,
		final String password,
		final RIFUserRole userRole, 
		boolean isNewUser) {
	
		this.user = user;
		this.password = password;
		this.userRole = userRole;
		
		this.isNewUser = isNewUser;
	}	
		
	private void updateForm() {
		userIDTextField.setText(user.getUserID());
		passwordTextField.setText(password);
		userRoleComboBox.setSelectedItem(userRole.getName());
	}
	
	private void ok() {
		/*

		try {
			
			RIFDataLoaderServiceAPI service = getService();
			
			String userID = userIDTextField.getText().trim();			
			String password = passwordTextField.getText().trim();
			RIFUserRole rifUserRole
				= RIFUserRole.getUserRole( (String) userRoleComboBox.getSelectedItem());
			Date expirationDate
				= 
			
			if (isNewUser == true) {
				service.addUser(
					user, 
					password, 
					rifUserRole, 
					expirationDate);
			}
			else {
				
			}
			hide();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(getDialog(), rifServiceException);
		}
		
		*/
		
	}
	
	
	private void close() {
		hide();
	}
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	@Override
	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
		
		if (this.isOKButton(button)) {
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


