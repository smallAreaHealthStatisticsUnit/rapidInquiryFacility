package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.ErrorDialog;
import rifGenericUILibrary.NoDataAvailablePanel;
import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.desktop.interactive.HealthCodeTableModel;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.dataStorageLayer.RIFStudySubmissionAPI;
import rifServices.system.RIFServiceException;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;


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

class HealthCodeSelectionSearchPanel 
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
	/** The current health code taxonomy. */
	private HealthCodeTaxonomy currentHealthCodeTaxonomy;

	//GUI Components
	/** The parent dialog. */
	private JDialog parentDialog;
	/** The ui factory. */
	private UserInterfaceFactory uiFactory;	
	/** The panel. */
	private JPanel panel;
	/** The search button panel. */
	private JPanel searchButtonPanel;
	/** The search text field. */
	private JTextField searchTextField;
	/** The apply button. */
	private JButton applyButton;
	/** The reset button. */
	private JButton resetButton;
	/** The health code scroll pane. */
	private JScrollPane healthCodeScrollPane;
	/** The health code table. */
	private JTable healthCodeTable;
	/** The health code table model. */
	private HealthCodeTableModel healthCodeTableModel;	
	/** The current result component. */
	private JComponent currentResultComponent;	
	/** The no data available panel. */
	private NoDataAvailablePanel noDataAvailablePanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new health code selection search panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param session the session
	 */
	public HealthCodeSelectionSearchPanel(
		JDialog parentDialog,
		RIFStudySubmissionToolSession session) {
		
		this.parentDialog = parentDialog;
		this.session = session;
		this.uiFactory = session.getUIFactory();
		
		healthCodeTableModel = new HealthCodeTableModel();
		healthCodeTable = uiFactory.createTable(healthCodeTableModel);

		String specifySearchCriteriaMessage
			= RIFJobSubmissionToolMessages.getMessage(
				"healthCodeSelectionDialog.specifySearchCriteria.label");
		noDataAvailablePanel
			= new NoDataAvailablePanel(
				uiFactory,
				specifySearchCriteriaMessage,
				true);
		
		currentResultComponent = noDataAvailablePanel.getPanel();

		healthCodeScrollPane = uiFactory.createScrollPane(healthCodeTable);
		
		searchButtonPanel = createSearchButtonPanel();
		panel = uiFactory.createPanel();

		rebuildUI();
	}

	/**
	 * Rebuild ui.
	 */
	private void rebuildUI() {
		
		panel.removeAll();
		GridBagConstraints panelGC = uiFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(searchButtonPanel, panelGC);
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;	
		panel.add(currentResultComponent, panelGC);			
		panel.updateUI();
	}
	
	/**
	 * Creates the search button panel.
	 *
	 * @return the jpanel
	 */
	private JPanel createSearchButtonPanel() {	
		
		JPanel panel = uiFactory.createPanel();
		GridBagConstraints panelGC = uiFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.anchor = GridBagConstraints.NORTHWEST;

		String searchLabelText
			= RIFJobSubmissionToolMessages.getMessage(
				"healthCodeSelectionDialog.search.label");
		JLabel searchLabel
			= uiFactory.createLabel(searchLabelText);
		panel.add(searchLabel, panelGC);
		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		searchTextField = uiFactory.createTextField();
		panel.add(searchTextField, panelGC);
		
		panelGC.gridx++;
		panelGC.anchor = GridBagConstraints.NORTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String applyButtonText
			= RIFJobSubmissionToolMessages.getMessage(
				"healthCodeSelectionDialog.applyButton.label");
		applyButton = uiFactory.createButton(applyButtonText);
		applyButton.addActionListener(this);
		panel.add(applyButton, panelGC);
		
		panelGC.gridx++;
		String resetButtonText
			= RIFJobSubmissionToolMessages.getMessage(
				"healthCodeSelectionDialog.resetButton.label");
		resetButton = uiFactory.createButton(resetButtonText);
		resetButton.addActionListener(this);
		panel.add(resetButton, panelGC);	
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Gets the view title.
	 *
	 * @return the view title
	 */
	public String getViewTitle() {
		
		String title
			= RIFJobSubmissionToolMessages.getMessage(
				"healthCodeSelectionDialog.search.label");
		return title;
	}
	
	/**
	 * Gets the panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel() {
		
		return panel;
	}
	
	/**
	 * Checks if is panel.
	 *
	 * @param tabbedPaneItem the tabbed pane item
	 * @return true, if is panel
	 */
	public boolean isPanel(
		Object tabbedPaneItem) {

		if (panel == tabbedPaneItem) {
			return true;
		}
		
		return false;
	}
		
	/**
	 * Reset panel.
	 *
	 * @param currentHealthCodeTaxonomy the current health code taxonomy
	 * @param existingHealthCodesForInvestigation the existing health codes for investigation
	 */
	public void resetPanel(
			
		HealthCodeTaxonomy currentHealthCodeTaxonomy,
		ArrayList<HealthCode> existingHealthCodesForInvestigation) {

		this.currentHealthCodeTaxonomy = currentHealthCodeTaxonomy;
		healthCodeTableModel.clearList();
		currentResultComponent = noDataAvailablePanel.getPanel();
		
		searchTextField.setText("");
		applyButton.setEnabled(true);
		resetButton.setEnabled(false);

		rebuildUI();
	}

	/**
	 * Gets the selected health codes.
	 *
	 * @return the selected health codes
	 */
	public ArrayList<HealthCode> getSelectedHealthCodes() {
		
		return healthCodeTableModel.getHealthCodes();
	}
	
	/**
	 * Apply search criteria.
	 */
	private void applySearchCriteria() {
		
		String searchText = searchTextField.getText().trim();
		
		RIFStudySubmissionAPI service = session.getRIFStudySubmissionService();
		try {
			ArrayList<HealthCode> searchResults
				= service.getHealthCodesMatchingSearchText(
					session.getUser(),
					currentHealthCodeTaxonomy,
					searchText);
			healthCodeTableModel.clearList();
			
			healthCodeTableModel.setHealthCodes(searchResults);
						
			applyButton.setEnabled(false);
			resetButton.setEnabled(true);
			
			currentResultComponent = healthCodeScrollPane;
			rebuildUI();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}
	}
	
	/**
	 * Reset search criteria.
	 */
	private void resetSearchCriteria() {
		
		searchTextField.setText("");
		
		applyButton.setEnabled(true);
		resetButton.setEnabled(false);
		
		currentResultComponent = noDataAvailablePanel.getPanel();
		rebuildUI();

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
		
		Object button = event.getSource();
		
		if (button == applyButton) {
			applySearchCriteria();			
		}
		else if (button == resetButton) {
			resetSearchCriteria();
		}			
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

