package rifDataLoaderTool.presentationLayer;

import rifGenericLibrary.presentationLayer.*;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifServices.businessConceptLayer.User;
import rifDataLoaderTool.system.*;
import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.fileFormats.*;
import rifDataLoaderTool.dataStorageLayer.LinearWorkflowEnactor;
import rifDataLoaderTool.dataStorageLayer.ProductionDataLoaderService;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;

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

public class PopulationHealthDataLoaderDialog 
	implements ActionListener {

	public static void main(String[] arguments) {


		try {
			
			//RIFDataLoaderToolSession session
			//	= new RIFDataLoaderToolSession();
			//session.setUser(user);
			UserInterfaceFactory userInterfaceFactory
				= new UserInterfaceFactory();
			PopulationHealthDataLoaderDialog populationHealthDataLoaderDialog
				= new PopulationHealthDataLoaderDialog(userInterfaceFactory);
			populationHealthDataLoaderDialog.initialiseService();			
			populationHealthDataLoaderDialog.show();
			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(null, rifServiceException.getErrorMessages());
		}
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private UserInterfaceFactory userInterfaceFactory;
	
	private User rifManager;
	private DataLoaderServiceAPI dataLoaderService;
	
	private LinearWorkflow originalLinearWorkflow;
	private LinearWorkflow workingCopyLinearWorkflow;
	
	private JMenuItem closeMenuItem;
		
	private JComboBox<String> startingStateComboBox;
	private JComboBox<String> stoppingStateComboBox;
	
	private OrderedListPanel dataSetConfigurationListPanel;
	private ListEditingButtonPanel dataSetConfigurationListButtonPanel;
	
	private JButton runWorkflowButton;
	private JButton loadWorkflowButton;
	private JButton saveWorkflowButton;
	private JButton saveAsWorkflowButton;	
	private JButton closeButton;
	
	private File currentlySelectedFile;	
	private JDialog dialog;
	
	private OKCloseButtonPanel okCloseButtonPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public PopulationHealthDataLoaderDialog(final UserInterfaceFactory userInterfaceFactory) {

		rifManager = User.newInstance("kgarwood", "111.111.111.111");		

		this.userInterfaceFactory = userInterfaceFactory;
		
		originalLinearWorkflow = LinearWorkflow.newInstance();
		
		this.workingCopyLinearWorkflow = LinearWorkflow.createCopy(originalLinearWorkflow);
		buildUI();
	}

	public void initialiseService() 
		throws RIFServiceException {
		

		//dataLoaderService = new ProductionDataLoaderService();			
		//dataLoaderService.initialiseService();		
	}
	
	private void buildUI() {
		//set the title of the dialog
		String title
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.title");
		dialog
			= userInterfaceFactory.createDialog(title);
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(
			createStartStopWorkflowStatePanel(),
			panelGC);

		panelGC.gridy++;
				
		String dataSetListPanelTitleText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.name.plural.label");
		String dataSetListPanelToolTipText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.name.plural.toolTip");		
		dataSetConfigurationListPanel
			= new OrderedListPanel(
				dataSetListPanelTitleText,
				dataSetListPanelToolTipText,
				userInterfaceFactory,
				true);
		
		panelGC.gridy++;				
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panel.add(
			dataSetConfigurationListPanel.getPanel(),
			panelGC);
		
		panelGC.gridy++;				
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(
			createOptionButtonPanel(userInterfaceFactory),
			panelGC);
		
		panelGC.gridy++;				
		panelGC.weightx = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		okCloseButtonPanel 
			= new OKCloseButtonPanel(userInterfaceFactory, false);
		okCloseButtonPanel.addActionListener(this);
		panel.add(
			okCloseButtonPanel.getPanel(), 
			panelGC);
		
		dialog.getContentPane().add(panel);
		dialog.setSize(500, 500);
	}
	
	private JPanel createStartStopWorkflowStatePanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String startWorkflowStateText
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.startWorkState.label");
		JLabel startWorkflowStateLabel
			= userInterfaceFactory.createLabel(startWorkflowStateText);
		String startWorkflowStateToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.startWorkState.toolTip");
		startWorkflowStateLabel.setToolTipText(startWorkflowStateToolTip);
		
		panel.add(startWorkflowStateLabel, panelGC);
		panelGC.gridx++;
		startingStateComboBox
			= userInterfaceFactory.createComboBox(
				WorkflowState.getAllStateNames());
		startingStateComboBox.addActionListener(this);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(startingStateComboBox, panelGC);
			
		panelGC.gridy++;
		panelGC.gridx = 0;
		
		String stopWorkflowStateText
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.stopWorkState.label");
		JLabel stopWorkflowStateLabel
			= userInterfaceFactory.createLabel(stopWorkflowStateText);
		String stopWorkflowStateToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.stopWorkState.toolTip");
		stopWorkflowStateLabel.setToolTipText(stopWorkflowStateToolTip);
		panel.add(stopWorkflowStateLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		stoppingStateComboBox
			= userInterfaceFactory.createComboBox(
				WorkflowState.getAllStateNames());
		stoppingStateComboBox.addActionListener(this);
		panel.add(stoppingStateComboBox, panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
		
	private JPanel createOptionButtonPanel(
		final UserInterfaceFactory userInterfaceFactory) {

		JPanel panel = userInterfaceFactory.createBorderLayoutPanel();
		panel.add(
			createWorkflowOptionsButtonPanel(), 
			BorderLayout.WEST);

		panel.add(createDataSetOptionsPanel(),
			BorderLayout.EAST);
		
		return panel;
	}
		
	private JPanel createWorkflowOptionsButtonPanel() {

		JPanel panel
			= userInterfaceFactory.createBorderLayoutPanel();
		
		String workflowOptionsPanelTitle
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.workflowOptions.label");
		JLabel workflowOptionsPanelLabel
			= userInterfaceFactory.createLabel(workflowOptionsPanelTitle);
		panel.add(workflowOptionsPanelLabel, BorderLayout.NORTH);
				
		GridBagConstraints lowerPanelGC
			= userInterfaceFactory.createGridBagConstraints();		
		JPanel lowerPanel = userInterfaceFactory.createPanel();
		lowerPanelGC.anchor = GridBagConstraints.SOUTHEAST;

		String runWorkflowButtonText
			= RIFGenericLibraryMessages.getMessage(
				"buttons.run.label");
		runWorkflowButton
			= userInterfaceFactory.createButton(runWorkflowButtonText);
		runWorkflowButton.addActionListener(this);
		lowerPanel.add(runWorkflowButton, lowerPanelGC);
		
		lowerPanelGC.gridx++;				
		String loadWorkflowText
			= RIFGenericLibraryMessages.getMessage(
				"buttons.load.label");
		loadWorkflowButton
			= userInterfaceFactory.createButton(loadWorkflowText);
		loadWorkflowButton.addActionListener(this);
		lowerPanel.add(loadWorkflowButton, lowerPanelGC);

		lowerPanelGC.gridx++;				
		String saveWorkflowButtonText
			= RIFGenericLibraryMessages.getMessage(
				"buttons.save.label");
		saveWorkflowButton
			= userInterfaceFactory.createButton(saveWorkflowButtonText);
		saveWorkflowButton.addActionListener(this);
		lowerPanel.add(saveWorkflowButton, lowerPanelGC);
		
		lowerPanelGC.gridx++;				
		String saveAsWorkflowButtonText
			= RIFGenericLibraryMessages.getMessage(
				"buttons.saveAs.label");
		saveAsWorkflowButton
			= userInterfaceFactory.createButton(saveAsWorkflowButtonText);
		saveAsWorkflowButton.addActionListener(this);
		lowerPanel.add(saveAsWorkflowButton, lowerPanelGC);
				
		panel.add(lowerPanel, BorderLayout.CENTER);
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
	
	private JPanel createDataSetOptionsPanel() {
		JPanel panel
			= userInterfaceFactory.createBorderLayoutPanel();
		
		String dataSetOptionsPanelTitle
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.dataSetOptions.label");
		JLabel dataSetOptionsTitleLabel
			= userInterfaceFactory.createLabel(dataSetOptionsPanelTitle);
		panel.add(dataSetOptionsTitleLabel, BorderLayout.NORTH);
		
		dataSetConfigurationListButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		dataSetConfigurationListButtonPanel.includeAddButton(null);
		dataSetConfigurationListButtonPanel.includeCopyButton(null);
		dataSetConfigurationListButtonPanel.includeEditButton(null);

		dataSetConfigurationListButtonPanel.includeDeleteButton(null);
		dataSetConfigurationListButtonPanel.rightJustifyButtons();
		dataSetConfigurationListButtonPanel.indicateEmptyState();
		dataSetConfigurationListButtonPanel.addActionListener(this);
		panel.add(
			dataSetConfigurationListButtonPanel.getPanel(),
			BorderLayout.CENTER);
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void show() {
		dialog.setVisible(true);		
	}

	private void loadWorkflow() {		
		try {
			
			File defaultDir = new File("C://");
			
			JFileChooser fileChooser
				= userInterfaceFactory.createFileChooser();
			fileChooser.setCurrentDirectory(defaultDir);
			
			XMLFileFilter xmlFileFilter = new XMLFileFilter();
			fileChooser.setFileFilter(xmlFileFilter);
		
			int result
				= fileChooser.showOpenDialog(dialog);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
		
			currentlySelectedFile = fileChooser.getSelectedFile();
			LinearWorkflowReader linearWorkflowReader
				= new LinearWorkflowReader();
			linearWorkflowReader.readFile(currentlySelectedFile);
			workingCopyLinearWorkflow
				= linearWorkflowReader.getLinearWorkflow();
			
			populateFormFromWorkingCopy();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}		
	}
	
	private void saveWorkflow() {
		try {
			validateForm();			
					
			populateWorkingCopyFromForm();
			
			LinearWorkflow.copyInto(
				workingCopyLinearWorkflow, 
				originalLinearWorkflow);
			
			if (originalLinearWorkflow.isNewRecord()) {
			
				JFileChooser fileChooser
					= userInterfaceFactory.createFileChooser();
		
				XMLFileFilter xmlFileFilter = new XMLFileFilter();
				fileChooser.setFileFilter(xmlFileFilter);
		
				int result
					= fileChooser.showSaveDialog(dialog);
				if (result != JFileChooser.APPROVE_OPTION) {
					return;
				}
		
				File selectedFile = fileChooser.getSelectedFile();
				String filePath
					= XMLFileFilter.createXMLFileName(selectedFile.getAbsolutePath());
				currentlySelectedFile = new File(filePath);
			}
			
			writeCurrentDataToFile();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}
			
	}

	private void saveAsWorkflow() {

	}
	
	private void writeCurrentDataToFile() 
		throws RIFServiceException {
				
		LinearWorkflowWriter linearWorkflowWriter
			= new LinearWorkflowWriter();

		String currentStartingWorkflowStateName
			= (String) startingStateComboBox.getSelectedItem();
		WorkflowState currentStartWorkflowState
			= WorkflowState.getWorkflowStateFromName(
				currentStartingWorkflowStateName);
		originalLinearWorkflow.setStartWorkflowState(currentStartWorkflowState);
		String currentStoppingWorkflowStateName
			= (String) startingStateComboBox.getSelectedItem();
		WorkflowState currentStoppingWorkflowState
			= WorkflowState.getWorkflowStateFromName(
				currentStoppingWorkflowStateName);
		originalLinearWorkflow.setStopWorkflowState(currentStoppingWorkflowState);
		linearWorkflowWriter.write(
			originalLinearWorkflow, 
			currentlySelectedFile);		
	}
	
	
	private void runWorkflow() {
		try {
			
			
			validateForm();			
			populateWorkingCopyFromForm();

			if (workingCopyLinearWorkflow.isNewRecord()) {
				saveWorkflow();				
			}
			
			dataLoaderService = new ProductionDataLoaderService();			
			dataLoaderService.initialiseService();	

			LinearWorkflowEnactor linearWorkflowEnactor
				= new LinearWorkflowEnactor(
					rifManager, 
					dataLoaderService);
						
			File dummyFile = new File("C://rif_scripts//test_data//blah.log");
			
			linearWorkflowEnactor.runWorkflow(
				dummyFile, 
				null, 
				currentlySelectedFile,
				workingCopyLinearWorkflow);

			String workflowCompletedMessage
				= RIFDataLoaderToolMessages.getMessage(
					"populationHealthDataLoaderDialog.info.workflowFinishedRunning");

			JOptionPane.showMessageDialog(
				dialog, 
				workflowCompletedMessage);		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}
		
	}
	
	private void addDataSetConfiguration() {
		
		CSVFileSelectionDialog csvFileSelectionDialog
			= new CSVFileSelectionDialog(userInterfaceFactory);
		csvFileSelectionDialog.show();
		if (csvFileSelectionDialog.isCancelled()) {
			return;
		}
		
		DataSetConfiguration dataSetConfiguration
			= csvFileSelectionDialog.getDataSetConfiguration();
		
		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(userInterfaceFactory);
		dialog.setData(
			originalLinearWorkflow, 
			dataSetConfiguration);
		dialog.show();
		if (dialog.isCancelled() == true) {
			return;
		}

		DataSetConfiguration revisedDataSetConfiguration
			= dialog.getDataSetConfiguration();
		originalLinearWorkflow.addDataSetConfiguration(revisedDataSetConfiguration);
		dataSetConfigurationListPanel.addListItem(revisedDataSetConfiguration);
		dataSetConfigurationListPanel.updateUI();
		dataSetConfigurationListPanel.setSelectedItem(revisedDataSetConfiguration);
		
		dataSetConfigurationListButtonPanel.indicatePopulatedState();
	}
	
	private void copyDataSetConfiguration() {
		
		DataSetConfiguration selectedDataSetConfiguration
			= (DataSetConfiguration) dataSetConfigurationListPanel.getSelectedItem();
		if (selectedDataSetConfiguration == null) {
			//no item in the list is selected
			return;
		}
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("populationHealthDataLoaderDialog.copyDataSetConfiguration.title");
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("populationHealthDataLoaderDialog.copyDataSetConfiguration.instructions");
		String fieldName
			= RIFDataLoaderToolMessages.getMessage("populationHealthDataLoaderDialog.copyDataSetConfiguration.fieldName");		
		ArrayList<String> existingListItemNames
			= dataSetConfigurationListPanel.getDisplayNames();
		
		CopyListItemNameEditorDialog dialog
			= new CopyListItemNameEditorDialog(
				userInterfaceFactory,
				dialogTitle,
				instructionsText,
				fieldName,
				existingListItemNames);
		
		dialog.show();
		if (dialog.isCancelled()) {
			return;
		}
		
		String selectedName = dialog.getFieldName();
		DataSetConfiguration copiedDataSetConfiguration
			= DataSetConfiguration.createCopy(selectedDataSetConfiguration);
		copiedDataSetConfiguration.setName(selectedName);
		dataSetConfigurationListPanel.addListItem(copiedDataSetConfiguration);
		dataSetConfigurationListPanel.setSelectedItem(copiedDataSetConfiguration);
		dataSetConfigurationListPanel.updateUI();
		
	}
	
	private void editDataSetConfiguration() {
		DataSetConfiguration dataSetConfiguration
			= (DataSetConfiguration) dataSetConfigurationListPanel.getSelectedItem();
				
		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(userInterfaceFactory);
		dialog.setData(
			originalLinearWorkflow, 
			dataSetConfiguration);
		dialog.show();
		
		if (dialog.isCancelled() == false) {
			//it means we attempted to save changes
			DataSetConfiguration revisedDataSetConfiguration
				= dialog.getDataSetConfiguration();
			originalLinearWorkflow.replaceDataSetConfiguration(
				dataSetConfiguration, 
				revisedDataSetConfiguration);
			//now replace the item in the list of data set configurations as well
			dataSetConfigurationListPanel.replaceItem(
				dataSetConfiguration, 
				revisedDataSetConfiguration);
		}
				
	}
	
	private void deleteSelectedDataSetConfigurations() {		
		dataSetConfigurationListPanel.deleteSelectedListItems();

		if (dataSetConfigurationListPanel.isEmpty()) {
			dataSetConfigurationListButtonPanel.indicateEmptyState();			
		}
	}
	
	
	private void close() {
		dialog.setVisible(false);
	}
	
	private void populateWorkingCopyFromForm() {

		String selectedStartStatePhrase
			= (String) startingStateComboBox.getSelectedItem();
		WorkflowState startWorkflowState
			= WorkflowState.getWorkflowStateFromName(selectedStartStatePhrase);
		workingCopyLinearWorkflow.setStartWorkflowState(startWorkflowState);
		
		String selectedStopStatePhrase
			= (String) stoppingStateComboBox.getSelectedItem();
		WorkflowState stopWorkflowState
			= WorkflowState.getWorkflowStateFromName(selectedStopStatePhrase);
		workingCopyLinearWorkflow.setStopWorkflowState(stopWorkflowState);
		
		ArrayList<DisplayableListItemInterface> currentListItems
			= dataSetConfigurationListPanel.getAllItems();
		workingCopyLinearWorkflow.clearDataSetConfigurations();
		for (DisplayableListItemInterface currentListItem : currentListItems) {
			workingCopyLinearWorkflow.addDataSetConfiguration((DataSetConfiguration) currentListItem);
		}		
	}
	
	private void populateFormFromWorkingCopy() {
		
		/*
		 * Disable action listeners so that we can populate the form
		 * without triggering anything
		 */
		
		WorkflowState startWorkflowState
			= workingCopyLinearWorkflow.getStartWorkflowState();
		WorkflowState stopWorkflowState
			= workingCopyLinearWorkflow.getStopWorkflowState();
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= workingCopyLinearWorkflow.getDataSetConfigurations();
		
		/*
		 * Reset UI components
		 */
		startingStateComboBox.setSelectedItem(
			startWorkflowState.getStateName());
		stoppingStateComboBox.setSelectedItem(
			stopWorkflowState.getStateName());
		
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			dataSetConfigurationListPanel.addListItem(dataSetConfiguration);
		}
		if (dataSetConfigurationListPanel.isEmpty()) {
			dataSetConfigurationListButtonPanel.indicateEmptyState();
		}
		else {
			dataSetConfigurationListPanel.selectFirstItem();
			dataSetConfigurationListButtonPanel.indicatePopulatedState();
		}
		dataSetConfigurationListPanel.updateUI();
	}
	

	private void validateForm() 
		throws RIFServiceException {
			
		String currentStartingWorkflowStateName
			= (String) startingStateComboBox.getSelectedItem();
		WorkflowState currentStartWorkflowState
			= WorkflowState.getWorkflowStateFromName(
				currentStartingWorkflowStateName);

		String currentStoppingWorkflowStateName
			= (String) stoppingStateComboBox.getSelectedItem();
		WorkflowState currentStopWorkflowState
			= WorkflowState.getWorkflowStateFromName(
				currentStoppingWorkflowStateName);
	
		if (currentStartWorkflowState.getStateSequenceNumber() > currentStopWorkflowState.getStateSequenceNumber()) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"populationHealthDataLoaderDialog.error.startStateComesAfterStopState",
					currentStartWorkflowState.getStateName(),
					currentStopWorkflowState.getStateName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.INVALID_LINEAR_WORKFLOW,
					errorMessage);
			throw rifServiceException;			
		}
		
	}

	private void updateButtonStates() {
		if (dataSetConfigurationListPanel.isEmpty()) {
			dataSetConfigurationListButtonPanel.indicateEmptyState();
		}
		else {
			dataSetConfigurationListButtonPanel.indicatePopulatedState();
		}
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
		

		if (button == loadWorkflowButton) {
			loadWorkflow();
		}
		else if ((button == saveWorkflowButton) ||
				 (button == saveWorkflowButton)) {
			saveWorkflow();
		}
		else if (dataSetConfigurationListButtonPanel.isAddButton(button)) {
			addDataSetConfiguration();
		}
		else if (dataSetConfigurationListButtonPanel.isCopyButton(button)) {
			copyDataSetConfiguration();
		}		
		else if (dataSetConfigurationListButtonPanel.isEditButton(button)) {
			editDataSetConfiguration();			
		}
		else if (dataSetConfigurationListButtonPanel.isDeleteButton(button)) {
			deleteSelectedDataSetConfigurations();
		}
		else if (button == runWorkflowButton) {
			runWorkflow();
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			close();
		}

	}
		
	// ==========================================
	// Section Override
	// ==========================================

}


