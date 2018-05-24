package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.fileformats.DataLoaderToolConfigurationReader;
import org.sahsu.rif.dataloader.fileformats.DataLoaderToolConfigurationWriter;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.generic.fileformats.XMLFileFilter;
import org.sahsu.rif.generic.presentation.ErrorDialog;
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

public class RIFDataLoaderToolMenuBar 
	implements ActionListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private JMenuBar menuBar;
	private DataLoaderToolSession session;
	private RIFDataLoaderToolApplication dataLoaderToolApplication;
	private JFrame frame;
	private JMenu fileMenu;
	
	private JFileChooser fileChooser;
	private JMenuItem loadConfigurationMenuButton;
	private JMenuItem saveConfigurationMenuButton;	
	private JMenuItem exitMenuButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFDataLoaderToolMenuBar(
		final JFrame frame,
		final DataLoaderToolSession session,
		final RIFDataLoaderToolApplication dataLoaderToolApplication) {

		this.frame = frame;
		this.session = session;
		this.dataLoaderToolApplication = dataLoaderToolApplication;
		
		UserInterfaceFactory userInterfaceFactory 
			= session.getUserInterfaceFactory();
		
		fileChooser = userInterfaceFactory.createFileChooser();
		XMLFileFilter fileFilter = new XMLFileFilter();
		fileChooser.setFileFilter(fileFilter);
		
		menuBar = userInterfaceFactory.createMenuBar();
		
		fileMenu = userInterfaceFactory.createFileMenu();

		loadConfigurationMenuButton
			= userInterfaceFactory.createLoadMenuItem();
		loadConfigurationMenuButton.addActionListener(this);
		fileMenu.add(loadConfigurationMenuButton);

		saveConfigurationMenuButton
			= userInterfaceFactory.createSaveAsMenuItem();
		saveConfigurationMenuButton.addActionListener(this);
		fileMenu.add(saveConfigurationMenuButton);
		
		exitMenuButton
			= userInterfaceFactory.createExitMenuItem();
		exitMenuButton.addActionListener(this);
		fileMenu.add(exitMenuButton);
		
		menuBar.add(fileMenu);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public JMenuBar getMenuBar() {
		return menuBar;
	}
	
	private void loadConfiguration() {
		
		try {
			int result = fileChooser.showOpenDialog(frame);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
		
			File selectedFile = fileChooser.getSelectedFile();
			DataLoaderToolConfigurationReader reader
				= new DataLoaderToolConfigurationReader();
			reader.readFile(selectedFile);

			DataLoaderToolConfiguration dataLoaderToolConfiguration
				= reader.getDataLoaderToolConfiguration();
			
			session.setDataLoaderToolConfiguration(dataLoaderToolConfiguration);
			
			DataLoadingOrder completionState
				= determineCompletionState(dataLoaderToolConfiguration);		
			dataLoaderToolApplication.setDataLoaderToolConfiguration(
				dataLoaderToolConfiguration, 
				completionState);
			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}		
	}
	
	private DataLoadingOrder determineCompletionState(
		final DataLoaderToolConfiguration dataLoaderToolConfiguration) {
		
		if (dataLoaderToolConfiguration.getCovariateDataSetConfigurations().size() > 0) {
			return DataLoadingOrder.SUFFICIENT_CONFIGURATION_DATA_SPECIFIED;
		}
		
		if (dataLoaderToolConfiguration.getNumeratorDataSetConfigurations().size() > 0) {
			return DataLoadingOrder.NUMERATORS_SPECIFIED;
		}

		if (dataLoaderToolConfiguration.getDenominatorDataSetConfigurations().size() > 0) {
			return DataLoadingOrder.DENOMINATORS_SPECIFIED;
		}

		if (dataLoaderToolConfiguration.getHealthThemes().size() > 0) {
			return DataLoadingOrder.HEALTH_THEMES_SPECIFIED;
		}
		
		GeographyMetaData geographyMetaData
			= dataLoaderToolConfiguration.getGeographyMetaData();
		if (geographyMetaData.getGeographies().size() > 0) {
			return DataLoadingOrder.GEOGRAPHY_META_DATA_SPECIFIED;
		}
				
		return DataLoadingOrder.NO_CONFIGURATION_DATA_SPECIFIED;
	}
	
	
	private void saveConfiguration() {
		
		int result = fileChooser.showSaveDialog(frame);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		try {
			File selectedFile
				= XMLFileFilter.ensureFileEndsWithXML(fileChooser.getSelectedFile());
			selectedFile
				= XMLFileFilter.ensureFileEndsWithXML(selectedFile);
			DataLoaderToolConfigurationWriter writer
				= new DataLoaderToolConfigurationWriter();
			writer.writeFile(
				selectedFile, 
			session.getDataLoaderToolConfiguration());
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}		
	}
	
	private void exit() {

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
		
		if (button == loadConfigurationMenuButton) {
			loadConfiguration();
		}
		else if (button == saveConfigurationMenuButton) {
			saveConfiguration();
		}
		else if (button == exitMenuButton) {
			exit();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


