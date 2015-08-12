package rifDataLoaderTool.presentationLayer.revisedGUI;

import rifGenericLibrary.presentationLayer.*;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;
import rifDataLoaderTool.system.*;
import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.fileFormats.*;
import rifDataLoaderTool.dataStorageLayer.LinearWorkflowEnactor;
import rifDataLoaderTool.dataStorageLayer.ProductionDataLoaderService;
import rifDataLoaderTool.dataStorageLayer.SampleRIFDatabaseCreationManager;

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

public class WorkflowEditorDialog 
	implements ActionListener {

	public static void main(String[] arguments) {


		try {

			
			
			
			//RIFDataLoaderToolSession session
			//	= new RIFDataLoaderToolSession();
			//session.setUser(user);

			WorkflowEditorDialog workflowEditorDialog
				= new WorkflowEditorDialog();
			workflowEditorDialog.initialiseService();			
			workflowEditorDialog.show();
			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(null, rifServiceException.getErrorMessages());
		}

/*
		try {
			SampleDataGenerator generator = new SampleDataGenerator();
			
			LinearWorkflow linearWorkflow
				= generator.testDataCleaning1Workflow();
			LinearWorkflowWriter writer = new LinearWorkflowWriter();
			File file = new File("C://rif_scripts//test_data//blah_numerator.xml");
			writer.write(linearWorkflow, file);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
*/
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
	
	private LinearWorkflow currentLinearWorkflow;
	
	private JMenuItem initialiseDemoDatabaseMenuItem;
	private JMenuItem loadWorkflowMenuItem;
	private JMenuItem saveWorkflowMenuItem;
	private JMenuItem exitMenuItem;
		
	private JComboBox<String> startingStateComboBox;
	private JComboBox<String> stoppingStateComboBox;
	
	private OrderedListPanel dataSetConfigurationListPanel;
	private ListEditingButtonPanel dataSetConfigurationListButtonPanel;
	
	private JButton runWorkflowButton;
	private JButton saveWorkflowButton;
	private JButton exitButton;
	
	
	private JFrame frame;
	// ==========================================
	// Section Construction
	// ==========================================

	public WorkflowEditorDialog() {

		rifManager = User.newInstance("kgarwood", "111.111.111.111");		

		userInterfaceFactory 
			= new UserInterfaceFactory();
		
		currentLinearWorkflow = LinearWorkflow.newInstance();
		
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
				"workflowEditorDialog.title");
		frame
			= userInterfaceFactory.createFrame(title);
		frame.setJMenuBar(createFileMenuBar());			
		
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
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(
			createBottomPanel(userInterfaceFactory),
			panelGC);
		
		frame.getContentPane().add(panel);
		frame.setSize(500, 500);
	}
	
	private JMenuBar createFileMenuBar() {
		JMenuBar menuBar 
			= userInterfaceFactory.createMenuBar();
		
		String fileMenuText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.fileMenu.label");
		JMenu fileMenu
			= userInterfaceFactory.createMenu(fileMenuText);
				
		String initialiseDemoDatabaseText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.fileMenu.initialiseDemoDatabase.label");		
		initialiseDemoDatabaseMenuItem
			= userInterfaceFactory.createJMenuItem(initialiseDemoDatabaseText);
		initialiseDemoDatabaseMenuItem.addActionListener(this);
		fileMenu.add(initialiseDemoDatabaseMenuItem);
	
		String loadWorkflowText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.fileMenu.loadWorkflow.label");
		loadWorkflowMenuItem
			= userInterfaceFactory.createJMenuItem(loadWorkflowText);
		loadWorkflowMenuItem.addActionListener(this);
		fileMenu.add(loadWorkflowMenuItem);
				
		String saveWorkflowText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.fileMenu.saveWorkflow.label");
		saveWorkflowMenuItem
			= userInterfaceFactory.createJMenuItem(saveWorkflowText);
		saveWorkflowMenuItem.addActionListener(this);
		fileMenu.add(saveWorkflowMenuItem);
		
		String exitText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.fileMenu.exit.label");
		exitMenuItem
			= userInterfaceFactory.createJMenuItem(exitText);
		exitMenuItem.addActionListener(this);
		
		
		menuBar.add(fileMenu);
		
		return menuBar;
	}
	
	private JPanel createStartStopWorkflowStatePanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		String startWorkflowStateText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.startWorkState.label");
		JLabel startWorkflowStateLabel
			= userInterfaceFactory.createLabel(startWorkflowStateText);
		String startWorkflowStateToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.startWorkState.toolTip");
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
				"workflowEditorDialog.stopWorkState.label");
		JLabel stopWorkflowStateLabel
			= userInterfaceFactory.createLabel(stopWorkflowStateText);
		String stopWorkflowStateToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.stopWorkState.toolTip");
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
	
	private JPanel createBottomPanel(
		final UserInterfaceFactory userInterfaceFactory) {
			
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel = userInterfaceFactory.createPanel();
		panelGC.anchor = GridBagConstraints.SOUTHEAST;				
		panelGC.fill = GridBagConstraints.NONE;

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
			panelGC);

		panelGC.gridy++;		
		
		panel.add(
			dataSetConfigurationListButtonPanel.getPanel(), 
			panelGC);

		panelGC.gridy++;		
		
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(
			createButtonControlPanel(userInterfaceFactory),
			panelGC);
		
		return panel;
	}
	
	
	
	private JPanel createButtonControlPanel(
		final UserInterfaceFactory userInterfaceFactory) {
		
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel = userInterfaceFactory.createPanel();
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		
		String runWorkflowButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.run.label");
		runWorkflowButton
			= userInterfaceFactory.createButton(runWorkflowButtonText);
		runWorkflowButton.addActionListener(this);
		panel.add(runWorkflowButton, panelGC);
		
		panelGC.gridx++;
		
		String saveWorkflowButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.save.label");
		saveWorkflowButton
			= userInterfaceFactory.createButton(saveWorkflowButtonText);
		saveWorkflowButton.addActionListener(this);
		panel.add(saveWorkflowButton, panelGC);
				
		panelGC.gridx++;
		
		String exitButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"workflowEditorDialog.exit.label");
		exitButton
			= userInterfaceFactory.createButton(exitButtonText);
		exitButton.addActionListener(this);
		panel.add(exitButton, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void show() {
		frame.setVisible(true);		
	}

	private void initialiseDatabase() {
		
		try {
			InitialiseDemoDatabaseDialog dialog
				= new InitialiseDemoDatabaseDialog(userInterfaceFactory);
			dialog.show();
			
			RIFDataLoaderStartupOptions startupOptions
				= dialog.getDataLoaderStartupOptions();
			
			SampleRIFDatabaseCreationManager databaseCreationManager
				= new SampleRIFDatabaseCreationManager(startupOptions);
			databaseCreationManager.setup();	
			
			//Show a dialog indicating that the setup has completed
			
			
			
			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(frame, rifServiceException.getErrorMessages());
		}
		
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
				= fileChooser.showOpenDialog(frame);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
		
			File workflowXMLFile = fileChooser.getSelectedFile();
			LinearWorkflowReader linearWorkflowReader
				= new LinearWorkflowReader();
			linearWorkflowReader.readFile(workflowXMLFile);
			currentLinearWorkflow
				= linearWorkflowReader.getLinearWorkflow();

			populateForm();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}		
	}
	
	private void saveWorkflow() {
		try {
			validateForm();			
					
			JFileChooser fileChooser
				= userInterfaceFactory.createFileChooser();
		
			XMLFileFilter xmlFileFilter = new XMLFileFilter();
			fileChooser.setFileFilter(xmlFileFilter);
		
			int result
				= fileChooser.showSaveDialog(frame);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
		
			File selectedFile = fileChooser.getSelectedFile();
			String filePath
				= XMLFileFilter.createXMLFileName(selectedFile.getAbsolutePath());
			File workflowXMLFile = new File(filePath);
			
			LinearWorkflowWriter linearWorkflowWriter
				= new LinearWorkflowWriter();

		
			String currentStartingWorkflowStateName
				= (String) startingStateComboBox.getSelectedItem();
			WorkflowState currentStartWorkflowState
				= WorkflowState.getWorkflowStateFromName(
					currentStartingWorkflowStateName);
			currentLinearWorkflow.setStartWorkflowState(currentStartWorkflowState);
			String currentStoppingWorkflowStateName
				= (String) startingStateComboBox.getSelectedItem();
			WorkflowState currentStoppingWorkflowState
				= WorkflowState.getWorkflowStateFromName(
						currentStoppingWorkflowStateName);
			currentLinearWorkflow.setStopWorkflowState(currentStoppingWorkflowState);

			linearWorkflowWriter.write(
				currentLinearWorkflow, 
				workflowXMLFile);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}
			
	}
	
	private void runWorkflow() {
		try {
			//validateForm();
			
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
				currentLinearWorkflow);

			String workflowCompletedMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowEditorDialog.info.workflowFinishedRunning");

			JOptionPane.showMessageDialog(
				frame, 
				workflowCompletedMessage);		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
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
			currentLinearWorkflow, 
			dataSetConfiguration);
		dialog.show();
		if (dialog.isCancelled() == true) {
			return;
		}

		DataSetConfiguration revisedDataSetConfiguration
			= dialog.getDataSetConfiguration();
		currentLinearWorkflow.addDataSetConfiguration(revisedDataSetConfiguration);
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
			= RIFDataLoaderToolMessages.getMessage("workflowEditorDialog.copyDataSetConfiguration.title");
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("workflowEditorDialog.copyDataSetConfiguration.instructions");
		String fieldName
			= RIFDataLoaderToolMessages.getMessage("workflowEditorDialog.copyDataSetConfiguration.fieldName");		
		String[] existingListItemNames
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
			currentLinearWorkflow, 
			dataSetConfiguration);
		dialog.show();
		
		if (dialog.isCancelled() == false) {
			//it means we attempted to save changes
			DataSetConfiguration revisedDataSetConfiguration
				= dialog.getDataSetConfiguration();
			currentLinearWorkflow.replaceDataSetConfiguration(
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
	
	
	private void exit() {
		System.exit(0);		
	}
	
	
	
	private void populateForm() {
		
		/*
		 * Disable action listeners so that we can populate the form
		 * without triggering anything
		 */
		
		WorkflowState startWorkflowState
			= currentLinearWorkflow.getStartWorkflowState();
		WorkflowState stopWorkflowState
			= currentLinearWorkflow.getStopWorkflowState();
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= currentLinearWorkflow.getDataSetConfigurations();
		
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
		System.out.println("Selected stop state=="+currentStoppingWorkflowStateName+"==");
		WorkflowState currentStopWorkflowState
			= WorkflowState.getWorkflowStateFromName(
				currentStoppingWorkflowStateName);
	
		if (currentStartWorkflowState.getStateSequenceNumber() > currentStopWorkflowState.getStateSequenceNumber()) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowEditorDialog.error.startStateComesAfterStopState",
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
		System.out.println("updateButtonStates");
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
		
		if (button == initialiseDemoDatabaseMenuItem) {
			initialiseDatabase();
		}
		else if (button == loadWorkflowMenuItem) {
			loadWorkflow();
		}
		else if ((button == saveWorkflowMenuItem) ||
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
		else if ((button == exitMenuItem) ||
				 (button == exitButton)) {
			exit();
		}
		else if (button == runWorkflowButton) {
			runWorkflow();
		}

	}
		
	// ==========================================
	// Section Override
	// ==========================================

}


