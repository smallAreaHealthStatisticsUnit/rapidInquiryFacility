package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.sahsu.rif.dataloader.concepts.ConfigurationHints;
import org.sahsu.rif.dataloader.concepts.DataLoaderServiceAPI;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.LinearWorkflow;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.dataloader.datastorage.LinearWorkflowEnactor;
import org.sahsu.rif.dataloader.datastorage.pg.ProductionPGDataLoaderService;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.fileformats.DirectoryFileFilter;
import org.sahsu.rif.generic.fileformats.XMLFileFilter;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.ListEditingButtonPanel;
import org.sahsu.rif.generic.presentation.NamedListItemDialog;
import org.sahsu.rif.generic.presentation.OKCloseButtonDialog;
import org.sahsu.rif.generic.presentation.OrderedListPanel;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;

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
			ProductionPGDataLoaderService service
				= new ProductionPGDataLoaderService();
			DataLoaderToolSession session
				= new DataLoaderToolSession();
			session.setDataLoaderService(service);
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
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
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
	
	private File exportDirectory;
	private JTextField exportDirectoryPathTextField;
	private JButton browseExportDirectoryButton;
	
	//GUI Components				
	private JComboBox<String> startingStateComboBox;
	private JComboBox<String> stoppingStateComboBox;
	private JButton defineConfigurationHintsButton;
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
	
	}
	
	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();

		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		//set the title of the dialog
		
		//String instructionsText
		//	= RIFDataLoaderToolMessages.getMessage("");
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createConfigurationHintsPanel(), panelGC);

		panelGC.gridy++;		
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

		dataSetConfigurationListButtonPanel.rightJustifyButtons();
		dataSetConfigurationListButtonPanel.indicateEmptyState();
		dataSetConfigurationListButtonPanel.addActionListener(this);
		
		panel.add(
			dataSetConfigurationListButtonPanel.getPanel(),
			panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(
			createRunWorkflowPanel(), 
			panelGC);
				
		//panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
	
	private JPanel createConfigurationHintsPanel() {

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		String configurationHintInstructionsText
			= RIFDataLoaderToolMessages.getMessage("populationHealthDataLoaderDialog.configurationHints.instructions");
		JPanel configurationInstructionPanel
			= userInterfaceFactory.createHTMLInstructionPanel(configurationHintInstructionsText);
		panel.add(configurationInstructionPanel, panelGC);

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		String defineHintsButtonText
			= RIFDataLoaderToolMessages.getMessage("populationHealthDataLoaderDialog.configurationHints.button.label");
		defineConfigurationHintsButton
			= userInterfaceFactory.createButton(defineHintsButtonText);
		defineConfigurationHintsButton.addActionListener(this);
		panel.add(defineConfigurationHintsButton, panelGC);
		
		
		
		
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
	

	private JPanel createRunWorkflowPanel() {
		
		//Now work on a small inner panel that shows:
		//Export Directory   |                            | [Browse...] [Run]
		UserInterfaceFactory userInterfaceFactory = getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		
		String runWorkflowText
			= RIFDataLoaderToolMessages.getMessage("populationHealthDataLoaderDialog.runWorkflow.exportDirectory.label");
		JLabel runWorkflowLabel
			= userInterfaceFactory.createLabel(runWorkflowText);
		panel.add(runWorkflowLabel, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		exportDirectoryPathTextField
			= userInterfaceFactory.createNonEditableTextField();
		userInterfaceFactory.setEditableAppearance(exportDirectoryPathTextField, false);
		panel.add(exportDirectoryPathTextField, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;		
		browseExportDirectoryButton
			= userInterfaceFactory.createBrowseButton();
		browseExportDirectoryButton.addActionListener(this);
		panel.add(browseExportDirectoryButton, panelGC);

		panelGC.gridx++;
		String runWorkflowButtonText
			= GENERIC_MESSAGES.getMessage("buttons.run.label");
		runWorkflowButton
			= userInterfaceFactory.createButton(runWorkflowButtonText);
		runWorkflowButton.addActionListener(this);
		panel.add(runWorkflowButton, panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
		
	}
		
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setData(final LinearWorkflow originalLinearWorkflow) {
		this.originalLinearWorkflow = originalLinearWorkflow;
		workingCopyLinearWorkflow = LinearWorkflow.createCopy(originalLinearWorkflow);
		
		populateFormFromWorkingCopy(originalLinearWorkflow);
	}

	private void defineConfigurationHints() {

		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= session.getDataLoaderToolConfiguration();
		ConfigurationHints configurationHints
			= dataLoaderToolConfiguration.getConfigurationHints();
		
		ConfigurationHintsEditorDialog configurationHintsEditorDialog
			= new ConfigurationHintsEditorDialog(session);
		configurationHintsEditorDialog.setData(configurationHints);
		configurationHintsEditorDialog.show();
		if (configurationHintsEditorDialog.isCancelled()) {
			return;
		}
		
		ArrayList<DataSetConfiguration> currentDataSetConfigurationHints
			= configurationHintsEditorDialog.getDataSetConfigurationHints();
		
		configurationHints.setDataSetConfigurationHints(currentDataSetConfigurationHints);
		ArrayList<DataSetFieldConfiguration> currentDataSetFieldConfigurations
			= configurationHintsEditorDialog.getDataSetFieldConfigurationHints();

		configurationHints.setDataSetFieldConfigurationHints(currentDataSetFieldConfigurations);
	}
	
	private void browseExportDirectory() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		DirectoryFileFilter directoryFileFilter
			= new DirectoryFileFilter();
		fileChooser.setFileFilter(directoryFileFilter);
		int result
			= fileChooser.showOpenDialog(getDialog());
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		exportDirectory = fileChooser.getSelectedFile();
		exportDirectoryPathTextField.setText(exportDirectory.getAbsolutePath());
		updateButtonStates();
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
			
			saveWasSuccessful = true;
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
		}
			
		return saveWasSuccessful;
		
	}
	
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
			dataLoaderService = new ProductionPGDataLoaderService();	
			
			DataLoaderToolConfiguration dataLoaderToolConfiguration
				= session.getDataLoaderToolConfiguration();
			
			dataLoaderService.initialiseService(dataLoaderToolConfiguration.getDatabaseConnectionConfiguration());	

			LinearWorkflowEnactor linearWorkflowEnactor
				= new LinearWorkflowEnactor(
					rifManager, 
					dataLoaderService);
			linearWorkflowEnactor.setExportDirectory(exportDirectory);
			
			/*
			linearWorkflowEnactor.runWorkflow(
				exportDirectory,
				workingCopyLinearWorkflow);
			*/
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
			= new DataSetConfigurationEditorDialog(
				session,
				RIFSchemaArea.POPULATION_DENOMINATOR_DATA);
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
		
		workingCopyLinearWorkflow.addDataSetConfiguration(originalDataSetConfiguration);
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
			= new DataSetConfigurationEditorDialog(
				session,
				RIFSchemaArea.POPULATION_DENOMINATOR_DATA);
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
		dataSetConfigurationListPanel.updateUI();
		
		if (dataSetConfigurationListPanel.isEmpty()) {
			dataSetConfigurationListButtonPanel.indicateEmptyState();
		}
		else {
			dataSetConfigurationListPanel.selectFirstItem();
			dataSetConfigurationListButtonPanel.indicatePopulatedState();
		}
		
		updateButtonStates();
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
		
		if (exportDirectory == null) {
			runWorkflowButton.setEnabled(false);
		}
		else {
			runWorkflowButton.setEnabled(true);
		}
		
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
		
		if (button == defineConfigurationHintsButton) {
			defineConfigurationHints();
		}
		if (button == browseExportDirectoryButton) {
			browseExportDirectory();
		}
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


