package rifJobSubmissionTool.desktop.interactive;


import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.OKCloseButtonPanel;
import rifGenericUILibrary.OrderedListComboBox;
import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.system.RIFServiceException;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;


/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 * 
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
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

class HealthCodeSelectionDialog 
	implements ActionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data	
	/** The session. */
	private RIFStudySubmissionToolSession session;	
	/** The service. */
	private RIFStudySubmissionAPI service;
	/** The selected health codes. */
	private ArrayList<HealthCode> selectedHealthCodes;
	/** The existing health codes for investigation. */
	private ArrayList<HealthCode> existingHealthCodesForInvestigation;
	/** The is cancelled. */
	private boolean isCancelled;	
	
	//GUI Components
	/** The ui factory. */
	private UserInterfaceFactory uiFactory;
	/** The dialog. */
	private JDialog dialog;
	/** The available health code taxonomies. */
	private OrderedListComboBox availableHealthCodeTaxonomies;
	/** The health code selection tabbed pane. */
	private JTabbedPane healthCodeSelectionTabbedPane;	
	/** The health code selection browse panel. */
	private HealthCodeSelectionBrowsePanel healthCodeSelectionBrowsePanel;
	/** The health code selection search panel. */
	private HealthCodeSelectionSearchPanel healthCodeSelectionSearchPanel;
	/** The ok close panel. */
	private OKCloseButtonPanel okCloseButtonPanel; 
	

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new health code selection dialog.
	 *
	 * @param session the session
	 */
	public HealthCodeSelectionDialog(
		RIFStudySubmissionToolSession session) {

		this.session = session;		
		uiFactory = session.getUIFactory();
		service = session.getRIFStudySubmissionService();

		selectedHealthCodes = new ArrayList<HealthCode>();
		
		existingHealthCodesForInvestigation
			= new ArrayList<HealthCode>();
		
		availableHealthCodeTaxonomies
			= new OrderedListComboBox(uiFactory);

		okCloseButtonPanel = new OKCloseButtonPanel(uiFactory);
		okCloseButtonPanel.addActionListener(this);

		buildUI();
	}

	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		String dialogTitle
			= RIFJobSubmissionToolMessages.getMessage(
				"healthCodeSelectionDialog.title");
		dialog
			= uiFactory.createDialog(dialogTitle);
		
		JPanel panel = uiFactory.createPanel();
		GridBagConstraints panelGC = uiFactory.createGridBagConstraints();

		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		panel.add(createInstructionsPanel(), panelGC);

		panelGC.gridy++;
		panel.add(createHealthCodeTaxonomyListPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1.0;
				
		healthCodeSelectionTabbedPane
			= uiFactory.createTabbedPane();
		healthCodeSelectionBrowsePanel
			= new HealthCodeSelectionBrowsePanel(dialog, session);
		healthCodeSelectionTabbedPane.add(
			healthCodeSelectionBrowsePanel.getViewTitle(),
			healthCodeSelectionBrowsePanel.getPanel());
		
		healthCodeSelectionSearchPanel
			= new HealthCodeSelectionSearchPanel(dialog, session);
		healthCodeSelectionTabbedPane.add(
			healthCodeSelectionSearchPanel.getViewTitle(),
			healthCodeSelectionSearchPanel.getPanel());

		panel.add(healthCodeSelectionTabbedPane, panelGC);

		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
				
		panel.add(okCloseButtonPanel.getPanel(), panelGC);

		resetViews();
		
		dialog.getContentPane().add(panel);
		dialog.setModal(true);
		dialog.setSize(400, 500);
				
	}
	
	/**
	 * Creates the instructions panel.
	 *
	 * @return the j panel
	 */
	private JPanel createInstructionsPanel() {
		
		JPanel panel = uiFactory.createPanel();
		GridBagConstraints panelGC = uiFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		
		String instructionsText
			= RIFJobSubmissionToolMessages.getMessage("healthCodeSelectionDialog.instructions");
		JTextArea instructionsTextArea
			= uiFactory.createNonEditableTextArea(3, 30);
		instructionsTextArea.setText(instructionsText);
		panel.add(instructionsTextArea, panelGC);
		
		return panel;
	}
	
	/**
	 * Creates the health code taxonomy list panel.
	 *
	 * @return the j panel
	 */
	public JPanel createHealthCodeTaxonomyListPanel() {
		
		JPanel panel = uiFactory.createPanel();
		
		String taxonomySelectionLabelText
			= RIFJobSubmissionToolMessages.getMessage(
				"healthCodeSelectionDialog.taxonomySelectionList.label");
		JLabel taxonomySelectionLabel
			= uiFactory.createLabel(taxonomySelectionLabelText);
		
		try {
			ArrayList<HealthCodeTaxonomy> healthCodeTaxonomies
				= service.getHealthCodeTaxonomies(session.getUser());
			for (HealthCodeTaxonomy healthCodeTaxonomy : healthCodeTaxonomies) {
				availableHealthCodeTaxonomies.addListItem(healthCodeTaxonomy);			
			}
			
			availableHealthCodeTaxonomies.selectFirstItem();
			availableHealthCodeTaxonomies.addActionListener(this);			
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				dialog, 
				rifServiceException.getErrorMessages());
		}		
		
		GridBagConstraints panelGC
			= uiFactory.createGridBagConstraints();
		panel.add(taxonomySelectionLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1.0;
		panel.add(availableHealthCodeTaxonomies.getComboBox(), panelGC);		
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Sets the existing health codes for investigation.
	 *
	 * @param existingHealthCodesForInvestigation the new existing health codes for investigation
	 */
	public void setExistingHealthCodesForInvestigation(
		ArrayList<HealthCode> existingHealthCodesForInvestigation) {

		this.existingHealthCodesForInvestigation = existingHealthCodesForInvestigation;
	}
	
	/**
	 * Gets the selected health codes.
	 *
	 * @return the selected health codes
	 */
	public ArrayList<HealthCode> getSelectedHealthCodes() {
		
		return selectedHealthCodes;
	}
	
	/**
	 * Show.
	 */
	public void show() {

		dialog.setVisible(true);
	}
	
	/**
	 * Checks if is cancelled.
	 *
	 * @return true, if is cancelled
	 */
	public boolean isCancelled() {
		
		return isCancelled;
	}
		
	/**
	 * Ok.
	 */
	private void ok() {	
		
		Component selectedComponent
			= healthCodeSelectionTabbedPane.getSelectedComponent();
		if (healthCodeSelectionBrowsePanel.isPanel(selectedComponent)) {
			selectedHealthCodes 
				= healthCodeSelectionBrowsePanel.getSelectedHealthCodes();
		}
		else if (healthCodeSelectionSearchPanel.isPanel(selectedComponent)) {
			selectedHealthCodes 
				= healthCodeSelectionSearchPanel.getSelectedHealthCodes();			
		}
		else {
			assert false;
		}

		isCancelled = false;
		dialog.setVisible(false);
	}
	
	/**
	 * Close.
	 */
	private void close() {
		
		isCancelled = true;
		dialog.setVisible(false);
	}
	
	
	/**
	 * Reset views.
	 */
	private void resetViews() {
		
		HealthCodeTaxonomy selectedHealthCodeTaxonomy
			= (HealthCodeTaxonomy) availableHealthCodeTaxonomies.getSelectedItem();
		healthCodeSelectionBrowsePanel.resetPanel(
			selectedHealthCodeTaxonomy, 
			existingHealthCodesForInvestigation);
		healthCodeSelectionSearchPanel.resetPanel(
			selectedHealthCodeTaxonomy, 
			existingHealthCodesForInvestigation);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: ActionListener
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(
		ActionEvent event) {
		
		Object source = event.getSource();
		if (availableHealthCodeTaxonomies.isComboBox(source)) {
			resetViews();
		}
		else if (okCloseButtonPanel.isOKButton(source)) {
			ok();
		}
		else if (okCloseButtonPanel.isCloseButton(source)) {
			close();
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================
}

