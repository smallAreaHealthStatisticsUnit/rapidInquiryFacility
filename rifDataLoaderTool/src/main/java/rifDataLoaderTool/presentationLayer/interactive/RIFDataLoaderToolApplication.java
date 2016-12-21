package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.dataStorageLayer.pg.ProductionPGDataLoaderService;
import rifDataLoaderTool.system.DataLoaderToolSession;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.fileFormats.RIFDataLoaderSettingsWriter;
import rifGenericLibrary.fileFormats.XMLFileFilter;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifDataLoaderTool.fileFormats.RIFDataLoaderSettingsReader;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
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

public class RIFDataLoaderToolApplication implements ActionListener {

	public static final void main(final String[] arguments) {
		
		try {
			DataLoaderToolSession session 
				= new DataLoaderToolSession();
			ProductionPGDataLoaderService dataLoaderService
				= new ProductionPGDataLoaderService();
			session.setDataLoaderService(dataLoaderService);
			session.initialiseService();
		
			RIFDataLoaderToolApplication rifDataLoaderToolApplication
				= new RIFDataLoaderToolApplication(session);
			rifDataLoaderToolApplication.show();
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
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
	private DataLoaderToolSettings dataLoaderToolSettings;
	private File currentDataLoaderSettingsFile;
	
	//GUI Components
	private RIFDataLoaderToolShutdownManager shutdownManager;
	private JFrame frame;
	private JMenuItem loadDataLoaderSettingsMenuButton;
	private JMenuItem saveDataLoaderSettingsMenuButton;	
	private JMenuItem quitMenuItem;
	private JButton editGeographiesButton;
	private JButton editDatabaseDataTypesButton;
	private JButton loadPopulationHealthDataButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFDataLoaderToolApplication(final DataLoaderToolSession session) {
		
		this.session = session;
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		currentDataLoaderSettingsFile = null;		
		dataLoaderToolSettings = session.getDataLoaderToolSettings();
		
		buildUI(userInterfaceFactory);
		updateButtonStates(dataLoaderToolSettings);
	}

	private void buildUI(final UserInterfaceFactory userInterfaceFactory) {		
		String title
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication.title");
		frame = userInterfaceFactory.createFrame(title);		
		frame.setJMenuBar(createMenuBar());
		JPanel panel = createMainPanel();
		frame.getContentPane().add(panel);
		frame.setSize(300, 300);
		frame.setResizable(false);
		
		shutdownManager
			= new RIFDataLoaderToolShutdownManager(frame, session);
	}
	
	private JMenuBar createMenuBar() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		
		JMenuBar menuBar = userInterfaceFactory.createMenuBar();
		
		JMenu fileMenu
			= userInterfaceFactory.createFileMenu();
		loadDataLoaderSettingsMenuButton
			= userInterfaceFactory.createLoadMenuItem();
		loadDataLoaderSettingsMenuButton.addActionListener(this);
		fileMenu.add(loadDataLoaderSettingsMenuButton);
				
		saveDataLoaderSettingsMenuButton
			= userInterfaceFactory.createSaveAsMenuItem();
		saveDataLoaderSettingsMenuButton.addActionListener(this);
		fileMenu.add(saveDataLoaderSettingsMenuButton);

		quitMenuItem
			= userInterfaceFactory.createExitMenuItem();
		quitMenuItem.addActionListener(this);
		fileMenu.add(quitMenuItem);
		
		menuBar.add(fileMenu);
		
		return menuBar;
	}
		
	private JPanel createMainPanel() {
		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();

		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication.instructions");	
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createOptionsPanel(), panelGC);

		return panel;		
	}
	
	private JPanel createOptionsPanel() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.insets
			= userInterfaceFactory.createInsets(5, 30, 5, 30);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.anchor = GridBagConstraints.NORTHWEST;
		
		String editGeographiesButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderApplication.buttons.editGeographies.label");
		editGeographiesButton
			= userInterfaceFactory.createButton(editGeographiesButtonText);
		editGeographiesButton.addActionListener(this);
		panel.add(editGeographiesButton, panelGC);
		
		panelGC.gridy++;
		String editDatabaseDataTypesButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderApplication.buttons.editDatabaseDataTypes.label");		
		editDatabaseDataTypesButton
			= userInterfaceFactory.createButton(editDatabaseDataTypesButtonText);
		editDatabaseDataTypesButton.addActionListener(this);
		panel.add(editDatabaseDataTypesButton, panelGC);

		panelGC.gridy++;
		String loadPopulationHealthDataText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication.buttons.loadPopulationHealthData.label");
		loadPopulationHealthDataButton
			= userInterfaceFactory.createButton(loadPopulationHealthDataText);
		loadPopulationHealthDataButton.addActionListener(this);
		panel.add(loadPopulationHealthDataButton, panelGC);
				
		return panel;
	}
		
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void show() {		
		frame.setVisible(true);
	}

	private void loadDataLoaderSettings() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
			
		try {
			int result = fileChooser.showOpenDialog(frame);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			
			File selectedFile = fileChooser.getSelectedFile();
			RIFDataLoaderSettingsReader reader
				= new RIFDataLoaderSettingsReader();
			reader.readFile(selectedFile);

			dataLoaderToolSettings = reader.getDataLoaderToolSettings();		
			session.setDataLoaderToolSettings(dataLoaderToolSettings);
			session.initialiseService();
			
			updateButtonStates(dataLoaderToolSettings);
			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}	
	}

	private void saveDataLoaderSettings() {
		if (currentDataLoaderSettingsFile == null) {
			saveAsDataLoaderSettings();
		}
		else {
			try {
				RIFDataLoaderSettingsWriter writer
					= new RIFDataLoaderSettingsWriter();
				writer.writeFile(
					currentDataLoaderSettingsFile, 
					dataLoaderToolSettings);	
				session.setSaveChanges(false);		
			}
			catch(RIFServiceException rifServiceException) {
				ErrorDialog.showError(
					frame, 
					rifServiceException.getErrorMessages());
			}
		}		
	}
	
	private void saveAsDataLoaderSettings() {
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		
		XMLFileFilter xmlFileFilter = new XMLFileFilter();
		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
		fileChooser.setFileFilter(xmlFileFilter);
		
		try {
			int result = fileChooser.showSaveDialog(frame);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
		
			String selectedFilePath 
				= fileChooser.getSelectedFile().getAbsolutePath();
			selectedFilePath
				= XMLFileFilter.createXMLFileName(selectedFilePath);
			currentDataLoaderSettingsFile = new File(selectedFilePath);
			RIFDataLoaderSettingsWriter writer
				= new RIFDataLoaderSettingsWriter();
			writer.writeFile(
				currentDataLoaderSettingsFile, 
				dataLoaderToolSettings);	
			session.setSaveChanges(false);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}	
		
	}
	
	private void updateButtonStates(final DataLoaderToolSettings dataLoaderToolSettings) {
		if (dataLoaderToolSettings.areGeographiesValid() == false) {
			editGeographiesButton.setEnabled(true);
			editDatabaseDataTypesButton.setEnabled(false);
			loadPopulationHealthDataButton.setEnabled(false);			
		}
		else if (dataLoaderToolSettings.areDataTypesValid() == false) {
			editGeographiesButton.setEnabled(true);
			editDatabaseDataTypesButton.setEnabled(true);
			loadPopulationHealthDataButton.setEnabled(false);						
		}
		else {
			editGeographiesButton.setEnabled(true);
			editDatabaseDataTypesButton.setEnabled(true);
			loadPopulationHealthDataButton.setEnabled(true);						
		}
	}
		
	private void initialiseDatabase() {		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();

		//try {
			DataLoaderToolSettings settings
				= new DataLoaderToolSettings();

			InitialiseDemoDatabaseDialog dialog
				= new InitialiseDemoDatabaseDialog(userInterfaceFactory);
			
			RIFDatabaseConnectionParameters dbSettings
				= settings.getDatabaseConnectionParameters();
			dialog.setData(dbSettings);
			dialog.show();
	}
	
	private void editGeographies() {
		
		GeographyListEditingDialog geographyListEditingDialog
			= new GeographyListEditingDialog(session);
		ArrayList<DLGeography> currentGeographies
			= dataLoaderToolSettings.getGeographies();
		geographyListEditingDialog.setData(currentGeographies);
		geographyListEditingDialog.show();

		if (geographyListEditingDialog.isCancelled() == false) {
			ArrayList<DLGeography> updatedGeographies
				= geographyListEditingDialog.getData();	
			dataLoaderToolSettings.setGeographies(updatedGeographies);
			updateButtonStates(dataLoaderToolSettings);
		}		
	}
	
	private void editDataTypes() {
		DataTypeEditorDialog dataTypeEditorDialog
			= new DataTypeEditorDialog(session);		
		dataTypeEditorDialog.show();
		if (dataTypeEditorDialog.isCancelled()) {
			return;
		}
		
		boolean saveChanges
			= dataTypeEditorDialog.saveChanges();
		if (saveChanges) {
			session.setSaveChanges(true);
		}
		updateButtonStates(session.getDataLoaderToolSettings());

	}
	
	private void loadPopulationHealthData() {
		PopulationHealthDataLoaderDialog dialog
			= new PopulationHealthDataLoaderDialog(session);
		ArrayList<LinearWorkflow> existingWorkflows
			= dataLoaderToolSettings.getWorkflows();
		
		//@TODO: For now, we will assume that the data loader tool
		//is editing a single linear workflow.  But in future releases
		//we anticipate that the tool will have to manage multiple
		//work flows
		LinearWorkflow originalWorkflow = null;
		if (existingWorkflows.isEmpty()) {
			originalWorkflow = LinearWorkflow.newInstance();
		}
		else {
			originalWorkflow = existingWorkflows.get(0);
		}
		dialog.setData(originalWorkflow);
		dialog.show();
		if (dialog.isCancelled()) {
			return;
		}
				
		LinearWorkflow revisedWorkflow
			= dialog.populateWorkflowFromForm();
		boolean saveChanges
			= originalWorkflow.hasIdenticalContents(revisedWorkflow);
		if (saveChanges) {
			session.setSaveChanges(saveChanges);
		}
		
		LinearWorkflow.copyInto(
			revisedWorkflow, 
			originalWorkflow);
		ArrayList<LinearWorkflow> results = new ArrayList<LinearWorkflow>();
		results.add(originalWorkflow);
		dataLoaderToolSettings.setWorkflows(results);
		
		updateButtonStates(session.getDataLoaderToolSettings());
	}
	
	private void quit() {
		if (session.saveChanges()) {
			//Ask if user wants to save changes
		}
		
		frame.setVisible(false);
		System.exit(0);
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

		if (button == editGeographiesButton) {
			editGeographies();
		}
		else if (button == editDatabaseDataTypesButton) {
			editDataTypes();
		}
		else if (button == loadPopulationHealthDataButton) {
			loadPopulationHealthData();
		}
		else if (button == loadDataLoaderSettingsMenuButton) {
			loadDataLoaderSettings();
		}
		else if (button == saveDataLoaderSettingsMenuButton) {
			saveAsDataLoaderSettings();
		}
		else if (button == quitMenuItem) {
			quit();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


