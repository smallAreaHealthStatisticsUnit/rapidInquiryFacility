package rifDataLoaderTool.presentationLayer.interactive;


import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.ShapeFile;
import rifDataLoaderTool.fileFormats.ShapeFileScanner;

import rifGenericLibrary.presentationLayer.*;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;

import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

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

class ShapeFileSelectionDialog 
	extends OKCloseButtonDialog {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	
	//GUI Components
	private OrderedListPanel shapeFileListPanel;
	private JTextField shapeFileBrowseDirectoryTextField;	
	private JButton browseButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFileSelectionDialog(
		final UserInterfaceFactory userInterfaceFactory) {

		super(userInterfaceFactory);

		String title
			= RIFDataLoaderToolMessages.getMessage("shapeFileSelectionDialog.title");
		setDialogTitle(title);
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("shapeFileSelectionDialog.instructions");	
		setInstructionText(instructionsText);
		setMainPanel(createMainPanel());		
		buildUI();
		setSize(500, 500);			
	}
	
	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		JPanel panel 
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
					
		panelGC.gridy++;
		panel.add(
			createBrowseShapeFilePanel(userInterfaceFactory), 
			panelGC);
		
		//create the list of shape files
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		String shapeFileListTitle
			= RIFDataLoaderToolMessages.getMessage("shapeFile.name.plural.label");	
		shapeFileListPanel
			= new OrderedListPanel(
				shapeFileListTitle,
				null,
				userInterfaceFactory,
				true);	
		panel.add(
			shapeFileListPanel.getPanel(), 
			panelGC);
		return panel;
	}
	
	private JPanel createBrowseShapeFilePanel(
		final UserInterfaceFactory userInterfaceFactory) {

		JPanel panel 
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();

		String shapeFileDirectoryLabelText
			= RIFDataLoaderToolMessages.getMessage("shapeFileDirectory.label");
		JLabel shapeFileDirectoryLabel
			= userInterfaceFactory.createLabel(shapeFileDirectoryLabelText);
		panel.add(shapeFileDirectoryLabel, panelGC);

		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		shapeFileBrowseDirectoryTextField
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(shapeFileBrowseDirectoryTextField, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;		
		String browseButtonText
			= RIFGenericLibraryMessages.getMessage("buttons.browse.label");
		browseButton
			= userInterfaceFactory.createButton(browseButtonText);
		browseButton.addActionListener(this);
		panel.add(browseButton, panelGC);
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public ShapeFile getSelectedShapeFile() {
		return (ShapeFile) shapeFileListPanel.getSelectedItem();
	}

	public ArrayList<ShapeFile> getSelectedShapeFiles() {
		ArrayList<ShapeFile> results = new ArrayList<ShapeFile>();
		ArrayList<DisplayableListItemInterface> listItems
			= shapeFileListPanel.getSelectedItems();
		for (DisplayableListItemInterface listItem : listItems) {
			results.add((ShapeFile) listItem);
		}
		
		return results;
	}
	
	public void updateAvailableShapeFiles(
		final File selectedDirectory) {
		
		ShapeFileScanner shapeFileScanner
			= new ShapeFileScanner();
		ArrayList<ShapeFile> shapeFiles
			= shapeFileScanner.scanForShapeFiles(selectedDirectory);
		shapeFileListPanel.clearList();
		for (ShapeFile shapeFile : shapeFiles) {
			shapeFileListPanel.addListItem(shapeFile);
		}
		shapeFileListPanel.updateUI();
	}
	
	private void browse() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = fileChooser.showOpenDialog(getDialog());
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
			
		File selectedDirectory = fileChooser.getSelectedFile();
		shapeFileBrowseDirectoryTextField.setText(
			selectedDirectory.getAbsolutePath());
	
		//update the total number of shape files that are available
		updateAvailableShapeFiles(selectedDirectory);
	}
	private File createDestinationZipFile(
		final String baseFileName) {
		
		StringBuilder path = new StringBuilder();
		path.append(baseFileName);
		path.append(".zip");
		
		File zipFile = new File(path.toString());
		return zipFile;
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
		
		if (button == browseButton) {
			browse();
		}
		else {
			performOKCloseActions(event);
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


