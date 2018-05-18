package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.fileformats.GeographyMetaDataConfigurationHandler;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.fileformats.XMLFileFilter;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.HTMLViewerDialog;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;

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

public class GeographyMetaDataLoadingPanel 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final Color populatedColour = new Color(0, 128, 0);
	
	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderToolSession session;
	private GeographyMetaData geographyMetaData;
	private DataLoaderToolChangeManager changeManager;
	private UserInterfaceFactory userInterfaceFactory;
	private JFrame frame;
	private JLabel panelTitleLabel;
	private JTextField filePathTextField;
	private JButton browseButton;
	
	private JFileChooser fileChooser;
	
	private JPanel panel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographyMetaDataLoadingPanel(
		final JFrame frame,
		final DataLoaderToolSession session,
		final DataLoaderToolChangeManager changeManager) {

		this.session = session;
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= session.getDataLoaderToolConfiguration();
		
		geographyMetaData = dataLoaderToolConfiguration.getGeographyMetaData();
		this.changeManager = changeManager;		
		this.userInterfaceFactory = session.getUserInterfaceFactory();
		fileChooser = userInterfaceFactory.createFileChooser();
		fileChooser.setFileFilter(new XMLFileFilter());
		buildUI();
	}

	private void buildUI() {
		panel = userInterfaceFactory.createPanel();
		
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String geographyMetaDataFileLabelText
			= RIFDataLoaderToolMessages
					  .getMessage("rifDataLoaderToolApplication2.geographies.label");
		panelTitleLabel
			= userInterfaceFactory.createLabel(geographyMetaDataFileLabelText);
		panelTitleLabel.setForeground(populatedColour);
		userInterfaceFactory.setBoldFont(panelTitleLabel);
		panel.add(panelTitleLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		filePathTextField 
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(filePathTextField, panelGC);		

		panelGC.gridx++;		
		browseButton 
			= userInterfaceFactory.createBrowseButton();
		browseButton.addActionListener(this);
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panel.add(browseButton, panelGC);		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void refresh() {
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= session.getDataLoaderToolConfiguration();
		geographyMetaData = dataLoaderToolConfiguration.getGeographyMetaData();
		populateForm();		
	}
	
	public void setData(final GeographyMetaData geographyMetaData) {
		this.geographyMetaData = geographyMetaData;
		populateForm();
	}
	
	private void populateForm() {
		if (geographyMetaData != null) {
			filePathTextField.setText(geographyMetaData.getFilePath());
		}
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	private void browse() {
		GeographyMetaDataEditingDialog geographyMetaDataEditorDialog
			= new GeographyMetaDataEditingDialog(session);
		geographyMetaDataEditorDialog.setData(geographyMetaData);
		geographyMetaDataEditorDialog.show();
		if (geographyMetaDataEditorDialog.isCancelled() == false) {
			try {
				changeManager.setGeographyMetaData(geographyMetaData);
				filePathTextField.setText(geographyMetaData.getFilePath());
			}
			catch(RIFServiceException rifServiceException) {
				ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
			}
		}
	}
	
	private void view() {
		GeographyMetaDataConfigurationHandler handler
			= new GeographyMetaDataConfigurationHandler();
		try {
			String dialogTitle
				= RIFDataLoaderToolMessages.getMessage("geographyMetaDataViewerDialog.title");
			String htmlText = handler.getHTML(geographyMetaData);
			HTMLViewerDialog dialog
				= new HTMLViewerDialog(
					userInterfaceFactory, 
					dialogTitle, 
					htmlText);
			dialog.show();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}
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
	}

	// ==========================================
	// Section Override
	// ==========================================

}


