package rifDataLoaderTool.presentationLayer;


import rifGenericLibrary.presentationLayer.*;

import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.fileFormats.ZipFileUtility;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.util.ShapeFileScanner;
import rifDataLoaderTool.businessConceptLayer.ShapeFile;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
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

public class ShapeFileLoaderDialog 
	implements ActionListener {

	public static void main(String[] args) {
		
		UserInterfaceFactory userInterfaceFactory
			= new UserInterfaceFactory();		
		ShapeFileLoaderDialog dialog = new ShapeFileLoaderDialog(userInterfaceFactory);
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
	
	
	private OrderedListPanel shapeFileListPanel;
	private JTextField shapeFileBrowseDirectoryTextField;
	
	private JButton browseButton;
	private OKCloseButtonPanel okCloseButtonPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFileLoaderDialog(final UserInterfaceFactory userInterfaceFactory) {
		this.userInterfaceFactory = userInterfaceFactory;
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("shapeFileLoaderDialog.title");
		dialog
			= userInterfaceFactory.createDialog(dialogTitle);
		
		JPanel panel 
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage("shapeFileLoaderDialog.instructions");	
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
			
		panelGC.gridy++;
		panel.add(createBrowseShapeFilePanel(), panelGC);
		
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
				false);	
		panel.add(shapeFileListPanel.getPanel(), panelGC);
				
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		okCloseButtonPanel
			= new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.addActionListener(this);
		panel.add(okCloseButtonPanel.getPanel(), panelGC);
		dialog.getContentPane().add(panel);
		dialog.setSize(500, 500);
	}
	
	private JPanel createBrowseShapeFilePanel() {
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
	
	public void show() {
		dialog.setVisible(true);
	}
	
	private void browse() {
		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = fileChooser.showOpenDialog(dialog);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
			
		File selectedDirectory = fileChooser.getSelectedFile();
		shapeFileBrowseDirectoryTextField.setText(selectedDirectory.getAbsolutePath());
	
		//update the total number of shape files that are available
		updateAvailableShapeFiles(selectedDirectory);
	}
	
	private void ok() {
		
		File destinationZipFile = null;
		try {
			
			//obtain the currently selected Shape File
			ShapeFile shapeFile
				= (ShapeFile) shapeFileListPanel.getSelectedItem();
			if (shapeFile != null) {
				destinationZipFile
					= createDestinationZipFile(shapeFile.getBaseFilePath());
				String[] shapeFileComponents = shapeFile.getFilePaths();
			
				ZipFileUtility zipFileUtility = new ZipFileUtility();
				zipFileUtility.writeZipFile(
					destinationZipFile, 
					shapeFileComponents);
			}		
			dialog.setVisible(false);		
		}
		catch(IOException exception) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"shapeFileLoaderDialog.error.unableToLoadShapeFile",
					destinationZipFile.getName());
			ErrorDialog.showError(dialog, errorMessage);
		}

	}
	
	private void close() {	
		dialog.setVisible(false);
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
		else if (okCloseButtonPanel.isOKButton(button)) {
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


