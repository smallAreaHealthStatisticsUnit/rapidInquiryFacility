package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;

import rifDataLoaderTool.businessConceptLayer.DataLoaderToolSettings;
import rifDataLoaderTool.businessConceptLayer.GeographicalResolutionLevel;
import rifDataLoaderTool.businessConceptLayer.RIFDatabaseConnectionParameters;
import rifDataLoaderTool.businessConceptLayer.RIFDataType;


import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import rifDataLoaderTool.fileFormats.RIFDataLoaderSettingsReader;
import rifDataLoaderTool.fileFormats.RIFDataLoaderSettingsWriter;

import rifGenericLibrary.system.RIFServiceException;

import rifDataLoaderTool.businessConceptLayer.ShapeFile;

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
		
		RIFDataLoaderToolApplication rifDataLoaderToolApplication
			= new RIFDataLoaderToolApplication();
		rifDataLoaderToolApplication.show();
		
	}
	
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private DataLoaderToolSettings dataLoaderToolSettings;
	
	private UserInterfaceFactory userInterfaceFactory;
	private JFrame frame;
		
	private JMenuItem loadDataLoaderSettingsMenuButton;
	private JMenuItem saveDataLoaderSettingsMenuButton;	
	private JMenuItem quitMenuItem;

	private JButton initialiseDemoDatabaseButton;
	private JButton editGeographicalResolutionsButton;
	private JButton loadShapeFilesButton;
	private JButton editDatabaseDataTypesButton;
	private JButton loadPopulationHealthDataButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFDataLoaderToolApplication() {
		userInterfaceFactory
			= new UserInterfaceFactory();
		
		String title
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication.title");
		frame = userInterfaceFactory.createFrame(title);
		
		frame.setJMenuBar(createMenuBar());
		JPanel panel = createMainPanel();
		frame.getContentPane().add(panel);
		frame.setSize(300, 300);
		
		dataLoaderToolSettings = new DataLoaderToolSettings();
		
		RIFDataTypeFactory rifDataTypeFactory
			= dataLoaderToolSettings.getRIFDataTypeFactory();
		ArrayList<RIFDataType> dataTypes
			= rifDataTypeFactory.getRegisteredDataTypes();
		System.out.println("There are 222=="+dataTypes.size()+"==");
		
		updateButtonStates(dataLoaderToolSettings);
	}

	private JMenuBar createMenuBar() {
		
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
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		panel.add(createOptionsPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;		
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(createQuitButtonPanel(), panelGC);
				
		return panel;		
	}
	
	private JPanel createOptionsPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.insets
			= userInterfaceFactory.createInsets(5, 30, 5, 30);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.anchor = GridBagConstraints.NORTHWEST;
		
		String initialiseDemoDatabaseText
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderApplication.buttons.initialiseDemoDatabase.label");		
		initialiseDemoDatabaseButton
			= userInterfaceFactory.createButton(initialiseDemoDatabaseText);
		initialiseDemoDatabaseButton.addActionListener(this);
		panel.add(initialiseDemoDatabaseButton, panelGC);
				
		panelGC.gridy++;
		String editGeographicalResolutionLevelsButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderApplication.buttons.editGeographicalResolutionLevels.label");
		editGeographicalResolutionsButton
			= userInterfaceFactory.createButton(editGeographicalResolutionLevelsButtonText);
		editGeographicalResolutionsButton.addActionListener(this);
		panel.add(editGeographicalResolutionsButton, panelGC);

		panelGC.gridy++;
		String loadShapeFilesButtonText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication.buttons.loadShapeFiles.label");
		loadShapeFilesButton
			= userInterfaceFactory.createButton(loadShapeFilesButtonText);
		loadShapeFilesButton.addActionListener(this);
		panel.add(loadShapeFilesButton, panelGC);		
		
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
	
	private JPanel createQuitButtonPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void show() {		
		frame.setVisible(true);
	}

	private void loadDataLoaderSettings() {
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
			
			updateButtonStates(dataLoaderToolSettings);
			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}	
	}

	private void saveAsDataLoaderSettings() {
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
		File selectedFile = new File(selectedFilePath);
		RIFDataLoaderSettingsWriter writer
			= new RIFDataLoaderSettingsWriter();
		writer.writeFile(selectedFile, dataLoaderToolSettings);		
		updateButtonStates(dataLoaderToolSettings);
		
	}
	catch(RIFServiceException rifServiceException) {
		ErrorDialog.showError(
			frame, 
			rifServiceException.getErrorMessages());
	}	
		
		
		
	}
	
	private void updateButtonStates(final DataLoaderToolSettings dataLoaderToolSettings) {

		if (dataLoaderToolSettings.areDatabaseConnectionSettingsValid() == false) {
			initialiseDemoDatabaseButton.setEnabled(true);
			editGeographicalResolutionsButton.setEnabled(false);
			loadShapeFilesButton.setEnabled(false);
			editDatabaseDataTypesButton.setEnabled(false);
			loadPopulationHealthDataButton.setEnabled(false);
		}
		else if (dataLoaderToolSettings.areGeographicalResolutionLevelsValid() == false) {
			initialiseDemoDatabaseButton.setEnabled(true);
			editGeographicalResolutionsButton.setEnabled(true);
			loadShapeFilesButton.setEnabled(false);
			editDatabaseDataTypesButton.setEnabled(false);
			loadPopulationHealthDataButton.setEnabled(false);			
		}
		else if (dataLoaderToolSettings.areShapeFileSettingsValid() == false) {
			initialiseDemoDatabaseButton.setEnabled(true);
			editGeographicalResolutionsButton.setEnabled(true);
			loadShapeFilesButton.setEnabled(true);
			editDatabaseDataTypesButton.setEnabled(false);
			loadPopulationHealthDataButton.setEnabled(false);						
		}
		else if (dataLoaderToolSettings.areDataTypesValid() == false) {
			initialiseDemoDatabaseButton.setEnabled(true);
			editGeographicalResolutionsButton.setEnabled(true);
			loadShapeFilesButton.setEnabled(true);
			editDatabaseDataTypesButton.setEnabled(true);
			loadPopulationHealthDataButton.setEnabled(false);						
		}
		else {
			initialiseDemoDatabaseButton.setEnabled(true);
			editGeographicalResolutionsButton.setEnabled(true);
			loadShapeFilesButton.setEnabled(true);
			editDatabaseDataTypesButton.setEnabled(true);
			loadPopulationHealthDataButton.setEnabled(true);						
		}
	}
		
	private void initialiseDatabase() {		
		//try {
			DataLoaderToolSettings settings
				= new DataLoaderToolSettings();

			InitialiseDemoDatabaseDialog dialog
				= new InitialiseDemoDatabaseDialog(userInterfaceFactory);
			
			RIFDatabaseConnectionParameters dbSettings
				= settings.getDatabaseConnectionParameters();
			if (dbSettings == null) {
				System.out.println("DB Settings were null!");
			}
			dialog.setData(dbSettings);
			dialog.show();
			
			
			
			//SampleRIFDatabaseCreationManager databaseCreationManager
			//	= new SampleRIFDatabaseCreationManager(startupOptions);
			//databaseCreationManager.setup();	
			
			//Show a dialog indicating that the setup has completed			
		//}
		//catch(RIFServiceException rifServiceException) {
		//	ErrorDialog.showError(
		//		frame, 
		//		rifServiceException.getErrorMessages());
		//}		
	}
	
	private void editGeographicalResolutionLevels() {
		GeographicalResolutionEditorDialog resolutionEditorDialog
			= new GeographicalResolutionEditorDialog(userInterfaceFactory);
		ArrayList<GeographicalResolutionLevel> currentLevels
			= dataLoaderToolSettings.getGeographicalResolutionLevels();		
		resolutionEditorDialog.setData(currentLevels);
		resolutionEditorDialog.show();
		if (resolutionEditorDialog.isCancelled() == false) {
			ArrayList<GeographicalResolutionLevel> updatedLevels
				= resolutionEditorDialog.getGeographicalResolutionLevels();			
			dataLoaderToolSettings.setGeographicalResolutionLevels(updatedLevels);
			updateButtonStates(dataLoaderToolSettings);
		}		
	}
	
	private void editDataTypes() {
		RIFDataTypeFactory rifDataTypeFactory 
			= dataLoaderToolSettings.getRIFDataTypeFactory();
		DatabaseTypeEditorDialog databaseTypeEditorDialog
			= new DatabaseTypeEditorDialog(userInterfaceFactory, rifDataTypeFactory);
		databaseTypeEditorDialog.show();
	}
	
	private void loadShapeFiles() {
		ShapeFileListEditingDialog dialog
			= new ShapeFileListEditingDialog(userInterfaceFactory);
		dialog.setData(dataLoaderToolSettings.getShapeFiles());
		dialog.show();
		if (dialog.isCancelled()) {
			return;
		}
		
		ArrayList<ShapeFile> shapeFiles
			= dialog.getShapeFiles();
		dataLoaderToolSettings.setShapeFiles(shapeFiles);
		
		
		updateButtonStates(dataLoaderToolSettings);

	}

	private void loadPopulationHealthData() {
		PopulationHealthDataLoaderDialog dialog
			= new PopulationHealthDataLoaderDialog(userInterfaceFactory);
		dialog.show();
	}
	
	private void quit() {
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

		if (button == initialiseDemoDatabaseButton) {
			initialiseDatabase();
		}
		else if (button == editGeographicalResolutionsButton) {
			editGeographicalResolutionLevels();
		}
		else if (button == loadShapeFilesButton) {
			loadShapeFiles();
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


