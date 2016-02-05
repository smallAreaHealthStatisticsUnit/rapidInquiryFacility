package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.OrderedListPanel;
import rifGenericLibrary.presentationLayer.ListEditingButtonPanel;


import java.awt.*;

import javax.swing.*;

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

public class DatabaseTypeEditorDialog 
	implements ActionListener {

	
	public static void main(String[] arguments) {
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();
		DatabaseTypeEditorDialog dialog
			= new DatabaseTypeEditorDialog(userInterfaceFactory);
		dialog.show();
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;	
	private JDialog dialog;	
	
	private OrderedListPanel currentlySupportedDataTypesPanel;
	private ListEditingButtonPanel dataTypeListEditingPanel;
	
	private OKCloseButtonPanel okCloseButtonPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DatabaseTypeEditorDialog(
		final UserInterfaceFactory userInterfaceFactory) {
		
		this.userInterfaceFactory = userInterfaceFactory;		
		buildUI();
	}

	private void buildUI() {

		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("databaseTypeEditorDialog.title");
		dialog = userInterfaceFactory.createDialog(dialogTitle);
		dialog.setModal(true);
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("databaseTypeEditorDialog.instructions");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		panelGC.gridy++;
		panel.add(createDataTypeEditingPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;		
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		okCloseButtonPanel = new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(400, 400);
		
	}

	private JPanel createDataTypeEditingPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();


		
		String listTitle
			= RIFDataLoaderToolMessages.getMessage("databaseTypeEditorDialog.currentlySupportedTypes.label");
		String listToolTipText = "";
		currentlySupportedDataTypesPanel
			= new OrderedListPanel(
				listTitle,
				listToolTipText,
				userInterfaceFactory,
				false);

		
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				currentlySupportedDataTypesPanel.getPanel(), 
				createDataTypePropertiesEditingPanel());

		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panel.add(splitPane, panelGC);
				
		return panel;
	}
	
	private JPanel createDataTypePropertiesEditingPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		
		return panel;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void show() {
		dialog.setVisible(true);
	}
	
	private void ok() {

		
		dialog.setVisible(false);		
	}
	
	private void close() {
		dialog.setVisible(false);
	}
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

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


