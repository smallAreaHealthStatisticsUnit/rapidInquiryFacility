package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.sahsu.rif.dataloader.concepts.DataLoaderServiceAPI;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.concepts.LinearWorkflow;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
import org.sahsu.rif.dataloader.datastorage.LinearWorkflowEnactor;
import org.sahsu.rif.dataloader.datastorage.ms.ProductionMSDataLoaderService;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.OKCloseButtonPanel;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
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

public class RIFDataLoaderToolApplication 
	implements ActionListener, 
	Observer {

	public static final void main(final String[] arguments) {
		
		try {
			DataLoaderToolSession session
				= new DataLoaderToolSession();
			//ProductionPGDataLoaderService dataLoaderService
			//	= new ProductionPGDataLoaderService();
			ProductionMSDataLoaderService dataLoaderService
				= new ProductionMSDataLoaderService();
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
	private final DataLoaderToolSession session;
	private DLDependencyManager dependencyManager;
	private DataLoaderToolChangeManager changeManager;

	private JFrame frame;
	private RIFDataLoaderToolMenuBar menuBar;
	private UserInterfaceFactory userInterfaceFactory;
	
	private GeographyMetaData geographyMetaData;
		
	private GeographyMetaDataLoadingPanel geographyMetaDataPanel;
	private HealthThemesListPanel healthThemeListPanel;
	
	
	private DataTypesLoadingPanel dataTypesLoadingPanel;
	private ConfigurationHintsLoadingPanel configurationHintsPanel;
	
	private DenominatorsListPanel denominatorsListPanel;
	private NumeratorsListPanel numeratorsListPanel;
	private CovariatesListPanel covariatesListPanel;
	
	private JFileChooser outputDirectoryFileChooser;
	private JTextField outputDirectoryTextField;
	private JButton browseOutputDirectoryButton;	
	private JButton runButton;
	private File outputDirectory;
	
	
	private OKCloseButtonPanel okCloseButtonPanel;
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFDataLoaderToolApplication(final DataLoaderToolSession session) {
		
		this.session = session;
		
		this.userInterfaceFactory = session.getUserInterfaceFactory();

		geographyMetaData = GeographyMetaData.newInstance();
		dependencyManager = new DLDependencyManager();
		changeManager = new DataLoaderToolChangeManager(session);

		geographyMetaDataPanel
			= new GeographyMetaDataLoadingPanel(
				frame,
				session,
				changeManager);
		
		healthThemeListPanel 
			= new HealthThemesListPanel(
				frame,
				session,
				dependencyManager,
				changeManager);
		changeManager.addObserver(healthThemeListPanel);
		
		dataTypesLoadingPanel
			= new DataTypesLoadingPanel(
				session,
				changeManager);
		changeManager.addObserver(dataTypesLoadingPanel);
		
		configurationHintsPanel
			= new ConfigurationHintsLoadingPanel(
				session,
				changeManager);
		changeManager.addObserver(configurationHintsPanel);
		
		denominatorsListPanel 
			= new DenominatorsListPanel(
				frame,
				session,
				dependencyManager,
				changeManager);
		changeManager.addObserver(denominatorsListPanel);
		
		numeratorsListPanel 
			= new NumeratorsListPanel(
				frame,
				session,
				dependencyManager,
				changeManager);
		changeManager.addObserver(numeratorsListPanel);
		
		covariatesListPanel 
			= new CovariatesListPanel(
				frame,
				session,
				dependencyManager,
				changeManager);
		changeManager.addObserver(covariatesListPanel);
		
		okCloseButtonPanel 
			= new OKCloseButtonPanel(userInterfaceFactory);
		okCloseButtonPanel.buildUI();
	
		String title
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication2.title");
		frame = userInterfaceFactory.createFrame(title);
		
		menuBar 
			= new RIFDataLoaderToolMenuBar(frame, session, this);
		frame.setJMenuBar(menuBar.getMenuBar());

		frame.getContentPane().add(createMainPanel());
		frame.setSize(700, 600);
		
		changeManager.addObserver(this);
		changeManager.resetDataLoadingSteps();
	}
	
	private JPanel createMainPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panelGC.weighty = 0;
		
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderToolApplication2.instructions");
		JPanel instructionsPanel
			= userInterfaceFactory.createHTMLInstructionPanel(instructionsText);
		panel.add(instructionsPanel, panelGC);
		
		panelGC.gridy++;
		
		double verticalExpansionProportion = 0.20;

		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
			geographyMetaDataPanel.getPanel(),
			panelGC);

		panelGC.gridy++;

		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 0.20;
		panel.add(
			healthThemeListPanel.getPanel(), 
			panelGC);

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
				dataTypesLoadingPanel.getPanel(), 
				panelGC);

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
				configurationHintsPanel.getPanel(), 
				panelGC);

		panelGC.gridy++;

		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 0.60;
		panel.add(createNumeratorAndDenominatorPanel(), panelGC);
		
		panelGC.gridy++;

		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 0.20;
		panel.add(
			covariatesListPanel.getPanel(), 
			panelGC);		
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
				createRunWorkflowPanel(), 
				panelGC);		

		panelGC.gridy++;

		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(
			okCloseButtonPanel.getPanel(), 
			panelGC);
		okCloseButtonPanel.addActionListener(this);
		return panel;		
	}
	
	private JPanel createNumeratorAndDenominatorPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;

		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				denominatorsListPanel.getPanel(), 
				numeratorsListPanel.getPanel());
		splitPane.setDividerLocation(0.5);
		panel.add(
			splitPane, 
			panelGC);

		return panel;
	}
	
	private JPanel createRunWorkflowPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();

		String outputDirectoryText
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderToolApplication.outputDirectory.label");
		JLabel outputDirectoryLabel
			= userInterfaceFactory.createLabel(outputDirectoryText);
		panel.add(outputDirectoryLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		outputDirectoryTextField 
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(outputDirectoryTextField, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		browseOutputDirectoryButton
			= userInterfaceFactory.createBrowseButton();
		browseOutputDirectoryButton.addActionListener(this);
		panel.add(browseOutputDirectoryButton, panelGC);
		
		panelGC.gridx++;
		runButton
			= userInterfaceFactory.createRunButton();
		runButton.addActionListener(this);
		panel.add(runButton, panelGC);

		outputDirectoryFileChooser = userInterfaceFactory.createFileChooser();
		outputDirectoryFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return panel;
	}
		
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setDataLoaderToolConfiguration(
		final DataLoaderToolConfiguration dataLoaderToolConfiguration,
		final DataLoadingOrder completionState) {
		
		
		dependencyManager.resetDependencies(dataLoaderToolConfiguration);
		geographyMetaDataPanel.refresh();
		healthThemeListPanel.refresh();
		dataTypesLoadingPanel.refresh();
		configurationHintsPanel.refresh();
		denominatorsListPanel.refresh();
		numeratorsListPanel.refresh();
		covariatesListPanel.refresh();
		
		changeManager.notifyDataLoadingObservers(completionState);
	}
	
	private void browseOutputDirectory() {
		int result = outputDirectoryFileChooser.showOpenDialog(frame);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		outputDirectory 
			= outputDirectoryFileChooser.getSelectedFile();
		outputDirectoryTextField.setText(outputDirectory.getAbsolutePath());		
	}
	
	
	private void runWorkflow() {
		
		try {

			DataLoaderToolConfiguration dataLoaderToolConfiguration
				= session.getDataLoaderToolConfiguration();
			LinearWorkflow workflow = LinearWorkflow.newInstance();
		
			ArrayList<DataSetConfiguration> dataSetConfigurations
				= dataLoaderToolConfiguration.getAllDataSetConfigurations();
			workflow.setDataSetConfigurations(dataSetConfigurations);
			workflow.setStartWorkflowState(WorkflowState.START);
			workflow.setStopWorkflowState(WorkflowState.STOP);
			DataLoaderServiceAPI dataLoaderService
				= session.getDataLoaderService();
			LinearWorkflowEnactor linearWorkflowEnactor
				= new LinearWorkflowEnactor(
					session.getRIFManager(), 
					dataLoaderService);
			linearWorkflowEnactor.runWorkflow(
				outputDirectory, 
				session.getDataLoaderToolConfiguration(),
				workflow);
			
		
				String workflowCompletedMessage
				= RIFDataLoaderToolMessages.getMessage(
					"rifDataLoaderToolApplication.info.workflowFinishedRunning");

				JOptionPane.showMessageDialog(
				frame, 
				workflowCompletedMessage);		

			
			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				frame, 
				rifServiceException.getErrorMessages());
		}
	}
	
	public void show() {
		frame.setVisible(true);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(final ActionEvent actionEvent) {
		
		Object button = actionEvent.getSource();

		if (button == browseOutputDirectoryButton) {
			browseOutputDirectory();
		}
		if (button == runButton) {
			runWorkflow();
		}
		if (okCloseButtonPanel.isOKButton(button)) {
			System.exit(0);
		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			System.exit(0);
		}
	}
	
	public void update(
		final Observable observable,
		final Object object) {
		
		DataLoadingOrder currentState
			= (DataLoadingOrder) object;
		if (currentState.getStepNumber() >= DataLoadingOrder.HEALTH_THEMES_SPECIFIED.getStepNumber()) {
			runButton.setEnabled(true);
		}
		else {
			runButton.setEnabled(false);
		}
	}		
	
	// ==========================================
	// Section Override
	// ==========================================

}


