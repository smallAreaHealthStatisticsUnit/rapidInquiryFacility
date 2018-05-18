package org.sahsu.rif.dataloader.presentation.interactive;

import org.sahsu.rif.dataloader.concepts.ConfigurationHints;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.fileformats.CSVFileFilter;
import org.sahsu.rif.generic.fileformats.CSVFilePreviewReader;
import org.sahsu.rif.generic.presentation.CSVFilePreviewTable;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.presentation.OKCloseButtonDialog;
import org.sahsu.rif.generic.system.RIFServiceException;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

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

class CSVFileSelectionDialog 
	extends OKCloseButtonDialog {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	private DataLoaderToolSession session;
	private String[] csvFieldNames;
	
	//GUI Components
	private File selectedCSVFile;
	private JTextField csvFileSelectionTextField;
	private JTextField delimiterTextField;
	private JButton browseCSVFileButton;	
	private CSVFilePreviewTable previewTable;
	private JScrollPane previewCSVScrollPane;
	private JButton previewCSVFileButton;
	private JButton resetButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CSVFileSelectionDialog(
		final DataLoaderToolSession session) {

		super(session.getUserInterfaceFactory());

		this.session = session;		
		csvFieldNames = new String[0];
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("csvFileSelectionDialog.title");
		setDialogTitle(dialogTitle);
		String instructionsLabelText
			= RIFDataLoaderToolMessages.getMessage("csvFileSelectionDialog.instructions");		
		setInstructionText(instructionsLabelText);
		setMainPanel(createMainPanel());
		buildUI();
		setSize(600, 300);	
	}
	
	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createUpperPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		previewTable = new CSVFilePreviewTable(userInterfaceFactory);
		previewCSVScrollPane
			= userInterfaceFactory.createScrollPane(previewTable.getTable());
		panel.add(previewCSVScrollPane, panelGC);
		
		return panel;
	}

	private JPanel createUpperPanel() {
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createFileSelectionFieldPanel(), panelGC);		
		panelGC.gridy++;
		panel.add(createDelimiterFieldPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.weightx = 0;
		panel.add(createPreviewAndClearButtonPanel(), panelGC);
		
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;
	}
	
	private JPanel createFileSelectionFieldPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();

		String csvFileLabelText
			= RIFDataLoaderToolMessages.getMessage("csvFileSelectionDialog.csvFile.label");
		JLabel csvFileLabel
			= userInterfaceFactory.createLabel(csvFileLabelText);
		panel.add(csvFileLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		csvFileSelectionTextField = userInterfaceFactory.createNonEditableTextField();
		panel.add(csvFileSelectionTextField, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		browseCSVFileButton = userInterfaceFactory.createBrowseButton();
		browseCSVFileButton.addActionListener(this);
		panel.add(browseCSVFileButton, panelGC);
		
		return panel;
	}

	private JPanel createDelimiterFieldPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		
		String delimiterLabelText
			= RIFDataLoaderToolMessages.getMessage("csvFileSelectionDialog.delimiter.label");
		JLabel delimiterLabel
			= userInterfaceFactory.createLabel(delimiterLabelText);
		panel.add(delimiterLabel, panelGC);
		
		panelGC.gridx++;		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		delimiterTextField
			= userInterfaceFactory.createTextField();
		delimiterTextField.setText(",");
		panel.add(delimiterTextField, panelGC);
		
		return panel;
	}
	
	private JPanel createPreviewAndClearButtonPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		
		String previewCSVFileButtonText
			= RIFDataLoaderToolMessages.getMessage("csvFileSelectionDialog.preview.label");
		previewCSVFileButton
			= userInterfaceFactory.createButton(previewCSVFileButtonText);
		previewCSVFileButton.addActionListener(this);
		previewCSVFileButton.setEnabled(false);
		panel.add(previewCSVFileButton, panelGC);

		panelGC.gridx++;
		String resetButtonText
			= RIFDataLoaderToolMessages.getMessage("csvFileSelectionDialog.resetDisplay.label");
		resetButton
			= userInterfaceFactory.createButton(resetButtonText);
		resetButton.addActionListener(this);
		panel.add(resetButton, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public DataSetConfiguration getDataSetConfiguration() {
		
		String coreDataSetName = selectedCSVFile.getName();
		
		//Strip file extension
		int dotIndex = coreDataSetName.lastIndexOf(".");
		if (dotIndex != -1) {
			//Assume that CSV file filter will prevent
			//cases where file name begins with a dot.
			
			coreDataSetName = coreDataSetName.substring(0, dotIndex);
		}
		
		String[] csvFieldNames
			= previewTable.getFieldNames();
		String[][] csvPreviewData
			= previewTable.getPreviewData();
		
		
		DataSetConfiguration originalDataSetConfiguration
			= DataSetConfiguration.newInstance(
				coreDataSetName, 
				csvFieldNames);		
		originalDataSetConfiguration.setFilePath(selectedCSVFile.getAbsolutePath());
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= session.getDataLoaderToolConfiguration();
		ConfigurationHints configurationHints
			= dataLoaderToolConfiguration.getConfigurationHints();
		configurationHints.applyHintsToDataSetConfiguration(
			originalDataSetConfiguration);

		return originalDataSetConfiguration;
	}
	
	public File getSelectedFile() {
		
		return null;
	}
	
	public String[] getFieldNames() {
		return csvFieldNames;
	}
	
	private void previewSelectedCSVFile() {
		
		try {
			//selectedCSVFile
			CSVFilePreviewReader reader = new CSVFilePreviewReader();
			reader.setIsFirstLineHeader(true);
			reader.setNumberOfLinesToPreview(100);
			
			
			reader.readFile(selectedCSVFile);
			
			previewTable.setData(
				reader.getColumnNames(),
				reader.getPreviewData());
			
			csvFieldNames = reader.getColumnNames();
			
			delimiterTextField.setEnabled(false);
			browseCSVFileButton.setEnabled(false);	
			setEnableOKButton(true);
			previewCSVFileButton.setEnabled(false);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());			
		}
	}
	
	private void browseCSVFile() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JFileChooser fileChooser
			= userInterfaceFactory.createFileChooser();		
		CSVFileFilter csvFileFilter = new CSVFileFilter();
		fileChooser.setCurrentDirectory(session.getCurrentBrowsingDirectory());
		fileChooser.setFileFilter(csvFileFilter);
		
		int result = fileChooser.showOpenDialog(getDialog());
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		selectedCSVFile = fileChooser.getSelectedFile();
		session.setWorkingDirectoryFromSelectedFile(selectedCSVFile);
		csvFileSelectionTextField.setText(selectedCSVFile.getAbsolutePath());
		
		previewCSVFileButton.setEnabled(true);
		resetButton.setEnabled(true);
	}
	
	private void resetDisplay() {
		csvFileSelectionTextField.setText("");
		delimiterTextField.setText(",");
		
		previewTable.clearData();
		setEnableOKButton(false);
		
		browseCSVFileButton.setEnabled(true);
		//resetButton.setEnabled(false);
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
		
		if (button == browseCSVFileButton) {
			browseCSVFile();
		}
		else if (button == previewCSVFileButton) {
			previewSelectedCSVFile();
		}
		else if (button == resetButton) {
			resetDisplay();
		}
		else {
			performOKCloseActions(event);
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================	
}


