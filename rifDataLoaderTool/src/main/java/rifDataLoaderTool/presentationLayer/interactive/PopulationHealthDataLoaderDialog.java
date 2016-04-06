package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.system.*;
import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.dataStorageLayer.LinearWorkflowEnactor;
import rifDataLoaderTool.dataStorageLayer.ProductionDataLoaderService;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.fileFormats.XMLFileFilter;
import rifGenericLibrary.presentationLayer.*;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
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

class PopulationHealthDataLoaderDialog
	extends OKCloseButtonDialog {

	public static void main(String[] arguments) {


		try {
			ProductionDataLoaderService service
				= new ProductionDataLoaderService();
			DataLoaderToolSession session
				= new DataLoaderToolSession(service);
			PopulationHealthDataLoaderDialog populationHealthDataLoaderDialog
				= new PopulationHealthDataLoaderDialog(session);
			populationHealthDataLoaderDialog.initialiseService();
			populationHealthDataLoaderDialog.doSystemExitOnClose();
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
	
	//Data
	private DataLoaderToolSession session;	
	//private DataLoaderServiceAPI dataLoaderService;
	private User rifManager;
	private LinearWorkflow originalLinearWorkflow;
	private LinearWorkflow workingCopyLinearWorkflow;
	private File currentlySelectedFile;	
	
	//GUI Components				
	private JComboBox<String> startingStateComboBox;
	private JComboBox<String> stoppingStateComboBox;	
	private OrderedListPanel dataSetConfigurationListPanel;
	private ListEditingButtonPanel dataSetConfigurationListButtonPanel;	
	private JButton runWorkflowButton;	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public PopulationHealthDataLoaderDialog(
		final DataLoaderToolSession session) {

		super(session.getUserInterfaceFactory());
		
		this.session = session;
		rifManager = User.newInstance("kgarwood", "111.111.111.111");				
		originalLinearWorkflow = LinearWorkflow.newInstance();
		workingCopyLinearWorkflow = LinearWorkflow.createCopy(originalLinearWorkflow);
		
		String title
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.title");
		setDialogTitle(title);
		setMainPanel(createMainPanel());
		buildUI();
		updateButtonStates();
		setSize(500, 500);		
	}

	public void initialiseService() 
		throws RIFServiceException {
		

		//dataLoaderService = new ProductionDataLoaderService();			
		//dataLoaderService.initialiseService();		
	}
	
	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		
		//set the title of the dialog
		
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
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		
		dataSetConfigurationListButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		dataSetConfigurationListButtonPanel.includeAddButton(null);
		dataSetConfigurationListButtonPanel.includeCopyButton(null);
		dataSetConfigurationListButtonPanel.includeEditButton(null);
		dataSetConfigurationListButtonPanel.includeDeleteButton(null);	
		String runWorkflowButtonText
			= RIFDataLoaderToolMessages.getMessage("populationHealthDataLoaderDialog.buttons.run.label");
		runWorkflowButton
			= userInterfaceFactory.createButton(runWorkflowButtonText);
		runWorkflowButton.addActionListener(this);
		dataSetConfigurationListButtonPanel.addSpecialisedButton(runWorkflowButton);
		dataSetConfigurationListButtonPanel.rightJustifyButtons();
		dataSetConfigurationListButtonPanel.indicateEmptyState();
		dataSetConfigurationListButtonPanel.addActionListener(this);
		
		panel.add(
			dataSetConfigurationListButtonPanel.getPanel(),
			panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
	
	private JPanel createStartStopWorkflowStatePanel() {

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
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
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setData(final LinearWorkflow originalLinearWorkflow) {
		this.originalLinearWorkflow = originalLinearWorkflow;
		
		populateFormFromWorkingCopy(originalLinearWorkflow);
	}
	
	private boolean saveWorkflow() {

		boolean saveWasSuccessful = false;
		
		try {
			validateForm();			
			LinearWorkflow.copyInto(
				workingCopyLinearWorkflow, 
				originalLinearWorkflow);
			
			if (originalLinearWorkflow.isNewRecord()) {

				UserInterfaceFactory userInterfaceFactory
					= getUserInterfaceFactory();
			
				JFileChooser fileChooser
					= userInterfaceFactory.createFileChooser();
		
				XMLFileFilter xmlFileFilter = new XMLFileFilter();
				fileChooser.setFileFilter(xmlFileFilter);
		
				int result
					= fileChooser.showSaveDialog(getDialog());
				if (result != JFileChooser.APPROVE_OPTION) {
					//save did not happen, signals caller to decide whether
					//to continue an operation
					return saveWasSuccessful;
				}
		
				File selectedFile = fileChooser.getSelectedFile();
				String filePath
					= XMLFileFilter.createXMLFileName(selectedFile.getAbsolutePath());
				currentlySelectedFile = new File(filePath);
			}
			
			//writeCurrentDataToFile();			
			saveWasSuccessful = true;
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
		}
			
		return saveWasSuccessful;
		
	}
	
	/*
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
	*/
	
	private void runWorkflow() {
		try {
			validateForm();			

			if (workingCopyLinearWorkflow.isNewRecord()) {
				if (saveWorkflow() == false) {
					//save did not happen, so abort the rest of the 
					//operation
					return;
				}
			}
			
			DataLoaderServiceAPI dataLoaderService
				= session.getDataLoaderService();
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
				getDialog(), 
				workflowCompletedMessage);		
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
		}
		
	}
	
	private void addDataSetConfiguration() {
				
		CSVFileSelectionDialog csvFileSelectionDialog
			= new CSVFileSelectionDialog(session);
		csvFileSelectionDialog.show();
		
		if (csvFileSelectionDialog.isCancelled()) {
			return;
		}
		
		DataSetConfiguration originalDataSetConfiguration
			= csvFileSelectionDialog.getDataSetConfiguration();

		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(session);
		dialog.setData(
			originalDataSetConfiguration);
		dialog.show();
		if (dialog.isCancelled() == true) {
			return;
		}

		session.setSaveChanges(true);
		DataSetConfiguration revisedDataSetConfiguration
			= dialog.getDataSetConfiguration();
		DataSetConfiguration.copyInto(
			revisedDataSetConfiguration, 
			originalDataSetConfiguration);
		
		originalLinearWorkflow.addDataSetConfiguration(originalDataSetConfiguration);
		dataSetConfigurationListPanel.addListItem(originalDataSetConfiguration);
		dataSetConfigurationListPanel.updateUI();
		dataSetConfigurationListPanel.setSelectedItem(originalDataSetConfiguration);
		
		updateButtonStates();

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

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		NamedListItemDialog dialog
			= new NamedListItemDialog(
				userInterfaceFactory,
				dialogTitle,
				existingListItemNames);
		
		dialog.show();
		if (dialog.isCancelled()) {
			return;
		}
		
		String selectedName = dialog.getCandidateName();
		DataSetConfiguration copiedDataSetConfiguration
			= DataSetConfiguration.createCopy(selectedDataSetConfiguration);
		copiedDataSetConfiguration.setName(selectedName);
		dataSetConfigurationListPanel.addListItem(copiedDataSetConfiguration);
		dataSetConfigurationListPanel.setSelectedItem(copiedDataSetConfiguration);
		dataSetConfigurationListPanel.updateUI();
		updateButtonStates();
		
	}
	
	private void editDataSetConfiguration() {
		DataSetConfiguration originalDataSetConfiguration
			= (DataSetConfiguration) dataSetConfigurationListPanel.getSelectedItem();
		
		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(session);
		dialog.setData(
			originalDataSetConfiguration);
		dialog.show();
		
		if (dialog.isCancelled()) {
			return;
		}
		
		DataSetConfiguration revisedDataSetConfiguration
			= dialog.getDataSetConfigurationFromForm();
		boolean saveChanges
			= !originalDataSetConfiguration.hasIdenticalContents(revisedDataSetConfiguration);
		if (saveChanges) {
			System.out.println("PHDLD -- editDataSet 1");
			session.setSaveChanges(true);
			
			String oldDisplayName
				= originalDataSetConfiguration.getDisplayName();
			
			DataSetConfiguration.copyInto(
				revisedDataSetConfiguration, 
				originalDataSetConfiguration);

			String newDisplayName
				= originalDataSetConfiguration.getDisplayName();
			
			//now replace the item in the list of data set configurations as well
			if (Objects.equals(oldDisplayName, newDisplayName) == false) {
				dataSetConfigurationListPanel.replaceItem(
					oldDisplayName, 
					originalDataSetConfiguration);			
			}		
		}

	}
	
	private void deleteSelectedDataSetConfigurations() {		
		dataSetConfigurationListPanel.deleteSelectedListItems();
		updateButtonStates();
	}
		
	public LinearWorkflow populateWorkflowFromForm() {

		LinearWorkflow populateWorkflowFromForm
			= LinearWorkflow.createCopy(originalLinearWorkflow);
		
		String selectedStartStatePhrase
			= (String) startingStateComboBox.getSelectedItem();
		WorkflowState startWorkflowState
			= WorkflowState.getWorkflowStateFromName(selectedStartStatePhrase);
		populateWorkflowFromForm.setStartWorkflowState(startWorkflowState);
		
		String selectedStopStatePhrase
			= (String) stoppingStateComboBox.getSelectedItem();
		WorkflowState stopWorkflowState
			= WorkflowState.getWorkflowStateFromName(selectedStopStatePhrase);
		populateWorkflowFromForm.setStopWorkflowState(stopWorkflowState);
		
		ArrayList<DisplayableListItemInterface> currentListItems
			= dataSetConfigurationListPanel.getAllItems();
		populateWorkflowFromForm.clearDataSetConfigurations();
		for (DisplayableListItemInterface currentListItem : currentListItems) {
			populateWorkflowFromForm.addDataSetConfiguration((DataSetConfiguration) currentListItem);
		}	
		
		return populateWorkflowFromForm;
	}
	
	private void populateFormFromWorkingCopy(final LinearWorkflow linearWorkflow) {
		
		/*
		 * Disable action listeners so that we can populate the form
		 * without triggering anything
		 */
		
		WorkflowState startWorkflowState
			= linearWorkflow.getStartWorkflowState();
		WorkflowState stopWorkflowState
			= linearWorkflow.getStopWorkflowState();
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= linearWorkflow.getDataSetConfigurations();
		
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
			runWorkflowButton.setEnabled(false);
		}
		else {
			dataSetConfigurationListButtonPanel.indicatePopulatedState();
			runWorkflowButton.setEnabled(true);
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
		
		if (dataSetConfigurationListButtonPanel.isAddButton(button)) {
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
		else {
			performOKCloseActions(event);
		}

	}
		
	// ==========================================
	// Section Override
	// ==========================================

}


