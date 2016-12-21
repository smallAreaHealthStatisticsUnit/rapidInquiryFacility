package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.dataStorageLayer.pg.ProductionPGDataLoaderService;
import rifDataLoaderTool.system.DataLoaderToolSession;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.DLGeographyMetaData;

import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.presentationLayer.OKCloseButtonPanel;
import rifGenericLibrary.system.RIFServiceException;

import javax.swing.*;

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

public class RIFDataLoaderToolApplication2 
	implements ActionListener {

	public static final void main(final String[] arguments) {
		
		try {
			DataLoaderToolSession session 
				= new DataLoaderToolSession();
			ProductionPGDataLoaderService dataLoaderService
				= new ProductionPGDataLoaderService();
			session.setDataLoaderService(dataLoaderService);
			session.initialiseService();
		
			RIFDataLoaderToolApplication2 rifDataLoaderToolApplication
				= new RIFDataLoaderToolApplication2(session);
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

	private JFrame frame;
	private UserInterfaceFactory userInterfaceFactory;
	
	private JLabel geographyMetaDataFileLabel;
	private JTextField geographyMetaDataFileTextField;
	private JButton browseGeographyMetaDataButton;
	private DLGeographyMetaData geographyMetaData;
		
	private HealthThemesListPanel healthThemeListPanel;
	private DenominatorsListPanel denominatorsListPanel;
	private NumeratorsListPanel numeratorsListPanel;
	private CovariatesListPanel covariatesListPanel;
	
	private OKCloseButtonPanel okCloseButtonPanel;
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFDataLoaderToolApplication2(final DataLoaderToolSession session) {
		
		this.session = session;
		
		this.userInterfaceFactory = session.getUserInterfaceFactory();

		geographyMetaData = DLGeographyMetaData.newInstance();
		dependencyManager = new DLDependencyManager();
		
		healthThemeListPanel 
			= new HealthThemesListPanel(
				frame,
				session,
				dependencyManager);
		denominatorsListPanel 
			= new DenominatorsListPanel(
				frame,
				session,
				dependencyManager);
		numeratorsListPanel 
			= new NumeratorsListPanel(
				frame,
				session,
				dependencyManager);
		covariatesListPanel 
			= new CovariatesListPanel(
				frame,
				session,
				dependencyManager);
		
		okCloseButtonPanel 
			= new OKCloseButtonPanel(userInterfaceFactory);
		
		String title
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication2.title");
		frame = userInterfaceFactory.createFrame(title);
		frame.getContentPane().add(createMainPanel());
		frame.setSize(700, 600);
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

		//Add the geographies panel


		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = verticalExpansionProportion;
		panel.add(
			createGeographiesPanel(),
			panelGC);

		panelGC.gridy++;

		panelGC.fill = GridBagConstraints.BOTH;
		panel.add(
			healthThemeListPanel.getPanel(), 
			panelGC);

		panelGC.gridy++;

		panelGC.fill = GridBagConstraints.BOTH;
		panel.add(
			denominatorsListPanel.getPanel(), 
			panelGC);		
		
		panelGC.gridy++;

		panelGC.fill = GridBagConstraints.BOTH;
		panel.add(
			numeratorsListPanel.getPanel(), 
			panelGC);		
		
		panelGC.gridy++;

		panelGC.fill = GridBagConstraints.BOTH;
		panel.add(
			covariatesListPanel.getPanel(), 
			panelGC);		
		
		/*
		panelGC.gridy++;
		
		String healthThemesListPanelTitle
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderToolApplication2.healthThemesTitle");
		healthThemesPanelLabel
			= userInterfaceFactory.createLabel(geographiesListPanelTitle);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
			healthThemesPanelLabel, 
			panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = verticalExpansionProportion;
		panel.add(
			healthThemesListEditingPanel.getPanel(), 
			panelGC);
		panelGC.gridy++;

		String denominatorListPanelTitle
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderToolApplication2.denominatorTitle");
		denominatorPanelLabel
			= userInterfaceFactory.createLabel(denominatorListPanelTitle);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
			denominatorPanelLabel, 
			panelGC);		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = verticalExpansionProportion;
		panel.add(
			denominatorListEditingPanel.getPanel(), 
			panelGC);
		
		panelGC.gridy++;

		String numeratorListPanelTitle
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderToolApplication2.numeratorTitle");
		numeratorPanelLabel
			= userInterfaceFactory.createLabel(numeratorListPanelTitle);
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
			numeratorPanelLabel, 
			panelGC);				
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = verticalExpansionProportion;
		panel.add(
			numeratorListEditingPanel.getPanel(), 
			panelGC);
		
		panelGC.gridy++;

		String covariatesListPanelTitle
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderToolApplication2.covariatesTitle");
		covariatesPanelLabel
			= userInterfaceFactory.createLabel(covariatesListPanelTitle);		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weighty = 0;
		panel.add(
			covariatesPanelLabel, 
			panelGC);

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = verticalExpansionProportion;
		panel.add(
			covariatesListEditingPanel.getPanel(), 
			panelGC);

		*/
		panelGC.gridy++;

		okCloseButtonPanel
			= new OKCloseButtonPanel(userInterfaceFactory);
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
	
	private JPanel createGeographiesPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		
		String geographyMetaDataFileLabelText
			= RIFDataLoaderToolMessages.getMessage("rifDataLoaderToolApplication2.geographies.label");		
		geographyMetaDataFileLabel
			= userInterfaceFactory.createLabel(geographyMetaDataFileLabelText);
		userInterfaceFactory.setBoldFont(geographyMetaDataFileLabel);
		panel.add(geographyMetaDataFileLabel, panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		geographyMetaDataFileTextField 
			= userInterfaceFactory.createNonEditableTextField();
		panel.add(geographyMetaDataFileTextField, panelGC);		

		panelGC.gridx++;		
		browseGeographyMetaDataButton 
			= userInterfaceFactory.createBrowseButton();
		browseGeographyMetaDataButton.addActionListener(this);
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panel.add(browseGeographyMetaDataButton, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	private void browseGeographyMetaDatafile() {
		GeographyEditingDialog geographyMetaDataEditorDialog
			= new GeographyEditingDialog(userInterfaceFactory);
		geographyMetaDataEditorDialog.setData(geographyMetaData);
		geographyMetaDataEditorDialog.show();
		geographyMetaDataFileTextField.setText(geographyMetaData.getFilePath());
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


		if (button == browseGeographyMetaDataButton) {
			browseGeographyMetaDatafile();
		}
		if (okCloseButtonPanel.isOKButton(button)) {

		}
		else if (okCloseButtonPanel.isCloseButton(button)) {
			System.exit(0);
		}
		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


