package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.*;
import rifDataLoaderTool.dataStorageLayer.DataLoaderService;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;

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

public final class DataSetProcessingDialog 
	implements ActionListener,
	ListSelectionListener {

	public static final void main(String[] args) {
		
		try {
			RIFDataLoaderToolSession session = new RIFDataLoaderToolSession();
			User testUser = User.newInstance("kgarwood", "111.111.11.11");
			session.setUser(testUser);
			
			DataLoaderService service = new DataLoaderService();
			session.setService(service);
			
			DataSetProcessingDialog dialog = new DataSetProcessingDialog(session);
			dialog.show();			
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
	private JDialog dialog;
	private DataSetActivityStepButtonPanel dataSetActivityStepButtonPanel;
	private JButton deleteButton;
	private OKCloseButtonPanel okCloseButtonPanel;
	private DataSetConfigurationTable dataSetConfigurationTable;
	
	// ==========================================
	// Section Construction
	// ==========================================

	DataSetProcessingDialog(
		final RIFDataLoaderToolSession session) 
		throws RIFServiceException {
		
		this.session = session;
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();

		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("dataSetProcessingDialog.title");
		dialog = userInterfaceFactory.createDialog(dialogTitle);
		
		dataSetActivityStepButtonPanel
			= new DataSetActivityStepButtonPanel(userInterfaceFactory);
		dataSetActivityStepButtonPanel.addActionListener(this);
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("dataSetProcessingDialog.instructions");
		JPanel instructionsPanel 
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		//add the scroll pane showing the table of RIF data set configurations
		dataSetConfigurationTable
			= new DataSetConfigurationTable(session);
		dataSetConfigurationTable.addListSelectionListener(this);
		dataSetConfigurationTable.updateDataSetConfigurations();
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(dataSetConfigurationTable.getTable());
		panel.add(scrollPane, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(dataSetActivityStepButtonPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panel.add(createBottomButtonPanel(), panelGC);

		if (dataSetConfigurationTable.isEmpty()) {
			dataSetActivityStepButtonPanel.indicateEmptyList();
			deleteButton.setEnabled(false);
		}
		else {
			dataSetActivityStepButtonPanel.indicatePopulatedList();
			deleteButton.setEnabled(true);
		}
				
		dialog.getContentPane().add(panel);
		dialog.setModal(true);
		dialog.setSize(700, 500);
	}

	private JPanel createBottomButtonPanel() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		
		JPanel panel 
			= userInterfaceFactory.createBorderLayoutPanel();		
		String deleteButtonText
			= RIFDataLoaderToolMessages.getMessage("dataSetProcessingDialog.buttons.delete.label");
		deleteButton
			= userInterfaceFactory.createButton(deleteButtonText);
		deleteButton.addActionListener(this);
		panel.add(deleteButton, BorderLayout.WEST);
		
		okCloseButtonPanel = new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), BorderLayout.EAST);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void show() {
		dialog.setVisible(true);
	}
	
	public void hide() {
		dialog.setVisible(false);
	}
	
	private void loadSelectedDataSet() {
		LoadDataActivityStepDialog loadDataActivityStepDialog
			= new LoadDataActivityStepDialog(session);
		loadDataActivityStepDialog.show();
	}

	private void cleanSelectedDataSet() {
		CleanDataActivityStepDialog cleanDataActivityStepDialog
			= new CleanDataActivityStepDialog(session);
		cleanDataActivityStepDialog.show();
	}

	private void convertSelectedDataSet() {
		
	}
	
	private void combineSelectedDataSets() {
		
	}

	private void optimiseSelectedDataSet() {
		
	}
	
	private void checkSelectedDataSet() {
		
	}
	
	private void publishSelectedDataSet() {
		
	}
	
	private void deleteSelectedDataSets() {
		try {
			dataSetConfigurationTable.deleteSelectedDataSetConfigurations();			
			if (dataSetConfigurationTable.isEmpty() == true) {
				dataSetActivityStepButtonPanel.indicateEmptyList();
				deleteButton.setEnabled(false);
			}
			else {
				dataSetActivityStepButtonPanel.indicatePopulatedList();
				deleteButton.setEnabled(true);
			}
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}
	}
	
	private void ok() {
		dialog.setVisible(false);
	}
	
	private void close() {
		dialog.setVisible(false);		
	}
	
	/**
	 * if the number of data set configurations is currently zero, then
	 * desensitise all the appropriate buttons
	 */
	private void checkButtonStatesForEmptyList() {
		if (dataSetConfigurationTable.isEmpty()) {
			
		}
		else {
			
		}
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
		
		if (dataSetActivityStepButtonPanel.isLoadButton(button)) {
			loadSelectedDataSet();
		}
		else if (dataSetActivityStepButtonPanel.isCleanButton(button)) {
			cleanSelectedDataSet();
		}
		else if (dataSetActivityStepButtonPanel.isConvertButton(button)) {
			convertSelectedDataSet();
		}
		else if (dataSetActivityStepButtonPanel.isCombineButton(button)) {
			combineSelectedDataSets();
		}
		else if (dataSetActivityStepButtonPanel.isOptimiseButton(button)) {
			optimiseSelectedDataSet();
		}
		else if (dataSetActivityStepButtonPanel.isCheckButton(button)) {
			checkSelectedDataSet();
		}
		else if (dataSetActivityStepButtonPanel.isPublishButton(button)) {
			publishSelectedDataSet();
		}
		else if (button == deleteButton) {
			deleteSelectedDataSets();
		}
		else if (okCloseButtonPanel.isOKButton(button)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}
	}
	
	//Interface: List Selection Listener
	public void valueChanged(ListSelectionEvent event) {
		DataSetConfiguration selectedDataSetConfiguration
			= dataSetConfigurationTable.getSelectedDataSetConfiguration();
		if (selectedDataSetConfiguration != null) {
			RIFDataLoaderActivityStep activityStep
				= selectedDataSetConfiguration.getLastActivityStepPerformed();
			dataSetActivityStepButtonPanel.setCurrentActivityStep(activityStep);
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


