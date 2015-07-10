package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifDataLoaderTool.businessConceptLayer.DataLoaderServiceAPI;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.RIFGenericUIMessages;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.ProductionRIFStudyServiceBundle;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.util.FieldValidationUtility;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.InetAddress;

import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;


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

public final class TableNameSelectionDialog 
	implements ActionListener,
	CaretListener {

	public static void main(String[] args) {
		try {
			ProductionRIFStudyServiceBundle rifStudyServiceBundle
				= new ProductionRIFStudyServiceBundle();
			RIFServiceStartupOptions rifServiceStartupOptions 
				= RIFServiceStartupOptions.newInstance(false, true);
			rifStudyServiceBundle.initialise(rifServiceStartupOptions);
			rifStudyServiceBundle.login("kgarwood", "kgarwood");				

			String ipAddress = InetAddress.getLocalHost().getHostAddress();
		
			/*
			UserInterfaceFactory userInterfaceFactory
				= new UserInterfaceFactory();
			User testUser = User.newInstance("kgarwood", ipAddress);
			RIFDataLoaderToolSession session
				= new RIFDataLoaderToolSession();
			session.setUser(testUser);
			session.setService());
			
			TableNameSelectionDialog dialog = new TableNameSelectionDialog(session);
			dialog.show();
			*/
		}
		catch(Exception exception) {
			
			exception.printStackTrace(System.out);
		}
		
		
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private RIFDataLoaderToolSession session;
	private UserInterfaceFactory userInterfaceFactory;
	
	private JDialog dialog;
	
	private JTextField proposedTableFieldNameTextField;
	
	private JTextField searchTextField;
	private JButton applyButton;
	private JButton resetButton;
	
	private DataSetConfigurationTable dataSetConfigurationTable;
	
	private OKCloseButtonPanel okCloseButtonPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public TableNameSelectionDialog(
		final RIFDataLoaderToolSession session) {

		this.session = session;
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("");
		dialog = userInterfaceFactory.createDialog(dialogTitle);
		
		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.gridy++;
		panel.add(createProposedTableNameField(), panelGC);
		panelGC.gridy++;
		panel.add(createCheckExistingTableNamesPanel(), panelGC);
		
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(okCloseButtonPanel.getPanel(), panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setModal(true);
		dialog.setSize(300, 300);
	}
	
	private JPanel createProposedTableNameField() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String proposedTableNameLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		JLabel proposedTableNameLabel
			= userInterfaceFactory.createLabel(proposedTableNameLabelText);
		panel.add(proposedTableNameLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		proposedTableFieldNameTextField
			= userInterfaceFactory.createTextField();
		panel.add(proposedTableFieldNameTextField, panelGC);

		return panel;
	}
	
	private JPanel createCheckExistingTableNamesPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panel.add(createSearchFieldPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		dataSetConfigurationTable = new DataSetConfigurationTable(session);
		
		JTable table = dataSetConfigurationTable.getTable();
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(table);
		panel.add(scrollPane, panelGC);
		
		return panel;
	}
	
	private JPanel createSearchFieldPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String searchLabelText
			= RIFGenericUIMessages.getMessage("");
		JLabel searchLabel
			= userInterfaceFactory.createLabel(searchLabelText);
		panel.add(searchLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		searchTextField
			= userInterfaceFactory.createTextField();
		panel.add(searchTextField, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String applyButtonText
			= RIFGenericUIMessages.getMessage("");
		applyButton
			= userInterfaceFactory.createButton(applyButtonText);
		applyButton.addActionListener(this);		
		panel.add(applyButton, panelGC);
		
		panelGC.gridx++;
		String resetButtonText
			= RIFGenericUIMessages.getMessage("");
		resetButton
			= userInterfaceFactory.createButton(resetButtonText);
		resetButton.addActionListener(this);
		panel.add(resetButton, panelGC);
		
		return panel;
	}
	

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	private void applySearch() {
		
	}
	
	private void resetSearch() {
		searchTextField.setText("");		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	private void validateForm() 
		throws RIFServiceException {
		
		String proposedTableFieldName
			= proposedTableFieldNameTextField.getText().trim();
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		
		if (fieldValidationUtility.isEmpty(proposedTableFieldName)) {
			//check if proposed table name is empty
			
		}
		else {
			//check for 
		}
	}
	

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == applyButton) {
			applySearch();
		}
		else if (source == resetButton) {
			resetSearch();
		}
		else if (okCloseButtonPanel.isOKButton(source)) {
			
		}
		else if (okCloseButtonPanel.isCloseButton(source)) {
			
		}
		
		
	}
	
	//Interface: Caret Listener
	public void caretUpdate(CaretEvent event) {

		DataLoaderServiceAPI service
			= session.getService();
		User currentUser
			= session.getUser();

		String currentSearchPhrase
			= searchTextField.getText().trim();
		try {			
			dataSetConfigurationTable.updateDataSetConfigurations(currentSearchPhrase);
		}
		catch(RIFServiceException RIFServiceException) {
			ErrorDialog.showError(
				dialog, 
				RIFServiceException.getErrorMessages());
		}
		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


