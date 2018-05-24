package org.sahsu.rif.dataloader.presentation.interactive;

import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.fileformats.GeographyMetaDataConfigurationHandler;
import org.sahsu.rif.dataloader.fileformats.GeographyMetaDataReader;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.presentation.OKCloseButtonDialog;
import org.sahsu.rif.generic.system.RIFServiceException;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.io.File;



/**
 *
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class GeographyMetaDataEditingDialog extends OKCloseButtonDialog {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private DataLoaderToolSession session;
	private JTextField geographyFileNameTextField;
	
	private File selectedFile;
	private JButton browseButton;
	
	private GeographyMetaData geographyMetaData;
	private JScrollPane scrollPane;
	private JEditorPane htmlPane;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographyMetaDataEditingDialog(
		final DataLoaderToolSession session) {

		super(session.getUserInterfaceFactory());
		
		this.session = session;
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("geographyEditingDialog.title");
		setDialogTitle(dialogTitle);
		
		setMainPanel(createMainPanel());
		buildUI();
		setSize(600, 300);
	}
	
	private JPanel createMainPanel() {

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel = userInterfaceFactory.createPanel();

		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		panel.add(createBrowseGeographyFilePanel(), panelGC);
		panelGC.gridy++;
		
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;		
		panel.add(
			userInterfaceFactory.createDescriptionLabel(), 
			panelGC);
				
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;		
		htmlPane = userInterfaceFactory.createHTMLEditorPane();
		scrollPane
			= userInterfaceFactory.createScrollPane(htmlPane);
		panel.add(scrollPane, panelGC);
			
		return panel;
	}
	
	private JPanel createBrowseGeographyFilePanel() {
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
	
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel = userInterfaceFactory.createPanel();
		
		String geographyFileLabelText
			= RIFDataLoaderToolMessages.getMessage("geographyEditingDialog.browseGeographyFile.label");
		JLabel geographyFileLabel
			= userInterfaceFactory.createLabel(geographyFileLabelText);
		panel.add(geographyFileLabel, panelGC);
		
		panelGC.gridx++;
		geographyFileNameTextField 
			= userInterfaceFactory.createNonEditableTextField();
		panelGC.weightx = 1;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panel.add(geographyFileNameTextField, panelGC);
		panelGC.gridx++;		
		panelGC.weightx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		browseButton = userInterfaceFactory.createBrowseButton();
		browseButton.addActionListener(this);
		panel.add(browseButton, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setData(final GeographyMetaData geographyMetaData) {
		this.geographyMetaData = geographyMetaData;
		
		try {
			if (geographyMetaData != null) {
				if (geographyMetaData.getGeographies().size() > 0) {					
					geographyFileNameTextField.setText(geographyMetaData.getFilePath());
					showGeographyMetaDataProperties(geographyMetaData);
				}
			}
		}
		catch(RIFServiceException rifServiceException) {
			//KLG: @TODO
		}
	}

	public GeographyMetaData getData() {
		return geographyMetaData;
	}
	
	private void browse() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		
		
		JFileChooser fileChooser = userInterfaceFactory.createFileChooser();
		fileChooser.setCurrentDirectory(session.getCurrentBrowsingDirectory());
		int result = fileChooser.showOpenDialog(getDialog());
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		try {
			File selectedFile = fileChooser.getSelectedFile();		
			session.setWorkingDirectoryFromSelectedFile(selectedFile);

			GeographyMetaDataReader geographyMetaDataReader
				= new GeographyMetaDataReader();
			geographyMetaDataReader.readFile(selectedFile);
			GeographyMetaData fileGeographyMetaData
				= geographyMetaDataReader.getGeographicalMetaData();
			
			GeographyMetaData.copyInto(
				fileGeographyMetaData, 
				geographyMetaData);

			showGeographyMetaDataProperties(geographyMetaData);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
			
		}
		
		selectedFile = fileChooser.getSelectedFile();
		geographyFileNameTextField.setText(selectedFile.getAbsolutePath());
	}
	
	private void showGeographyMetaDataProperties(
		final GeographyMetaData geographyMetaData) 
		throws RIFServiceException {
		
		GeographyMetaDataConfigurationHandler metaDataHandler
			= new GeographyMetaDataConfigurationHandler();
		String htmlReport = metaDataHandler.getHTML(geographyMetaData);
		htmlPane.setText(htmlReport);
		htmlPane.setCaretPosition(0);
		htmlPane.updateUI();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();
		
		if (button == browseButton) {
			browse();
		}
		else {
			performOKCloseActions(event);			
		}
	}
	
}


