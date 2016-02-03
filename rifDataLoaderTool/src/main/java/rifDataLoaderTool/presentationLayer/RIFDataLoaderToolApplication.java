package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.dataStorageLayer.SampleRIFDatabaseCreationManager;
import rifDataLoaderTool.system.RIFDataLoaderMessages;
import rifDataLoaderTool.system.RIFDataLoaderStartupOptions;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.presentationLayer.ErrorDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
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
	private UserInterfaceFactory userInterfaceFactory;
	private JFrame frame;
	
	private JButton initialiseDemoDatabaseButton;
	private JButton editDatabaseDataTypesButton;
	private JButton loadShapeFilesButton;
	private JButton loadPopulationHealthDataButton;
	private JButton quitButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFDataLoaderToolApplication() {
		userInterfaceFactory
			= new UserInterfaceFactory();
		
		String title
			= RIFDataLoaderMessages.getMessage("rifDataLoaderToolApplication.title");
		frame = userInterfaceFactory.createFrame(title);
		JPanel panel = createMainPanel();
		frame.getContentPane().add(panel);
		frame.setSize(300, 300);
	}

	private JPanel createMainPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		String instructionsText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderToolApplication.instructions");	
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
				"populationHealthDataLoaderDialog.buttons.initialiseDemoDatabase.label");		
		initialiseDemoDatabaseButton
			= userInterfaceFactory.createButton(initialiseDemoDatabaseText);
		initialiseDemoDatabaseButton.addActionListener(this);
		panel.add(initialiseDemoDatabaseButton, panelGC);
				
		panelGC.gridy++;
		String editDatabaseDataTypesButtonText
			= RIFDataLoaderToolMessages.getMessage(
				"populationHealthDataLoaderDialog.buttons.editDatabaseDataTypes.label");		
		editDatabaseDataTypesButton
			= userInterfaceFactory.createButton(editDatabaseDataTypesButtonText);
		editDatabaseDataTypesButton.addActionListener(this);
		panel.add(editDatabaseDataTypesButton, panelGC);
				
		panelGC.gridy++;
		String loadShapeFilesButtonText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderToolApplication.buttons.loadShapeFiles.label");
		loadShapeFilesButton
			= userInterfaceFactory.createButton(loadShapeFilesButtonText);
		loadShapeFilesButton.addActionListener(this);
		panel.add(loadShapeFilesButton, panelGC);
	
		panelGC.gridy++;
		String loadPopulationHealthDataText
			= RIFDataLoaderMessages.getMessage("rifDataLoaderToolApplication.buttons.loadPopulationHealthData.label");
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
		
		String quitButtonText
			= RIFGenericLibraryMessages.getMessage("buttons.quit.label");
		quitButton
			= userInterfaceFactory.createButton(quitButtonText);
		quitButton.addActionListener(this);
		panel.add(quitButton, panelGC);
		
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
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}		
	}
	
	private void editDatabaseButtonDataTypes() {
		
	}
	
	private void loadShapeFiles() {
		ShapeFileLoaderDialog dialog 
			= new ShapeFileLoaderDialog(userInterfaceFactory);
		dialog.show();
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
		else if (button == editDatabaseDataTypesButton) {
			editDatabaseButtonDataTypes();
		}
		else if (button == loadShapeFilesButton) {
			loadShapeFiles();
		}
		else if (button == loadPopulationHealthDataButton) {
			loadPopulationHealthData();
		}
		else if (button == quitButton) {
			quit();
		}	
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


