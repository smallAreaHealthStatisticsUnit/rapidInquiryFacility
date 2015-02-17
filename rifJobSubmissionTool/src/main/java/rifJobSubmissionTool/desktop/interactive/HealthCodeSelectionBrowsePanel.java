package rifJobSubmissionTool.desktop.interactive;

import rifGenericUILibrary.UserInterfaceFactory;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFStudySubmissionToolSession;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;

import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


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

final class HealthCodeSelectionBrowsePanel {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	/** The session. */
	private RIFStudySubmissionToolSession session;

	//GUI Components
	/** The panel. */
	private JPanel panel;
	/** The parent dialog. */
	private JDialog parentDialog;
	/** The ui factory. */
	private UserInterfaceFactory uiFactory;	
	/** The health code selection tree. */
	private HealthCodeSelectionTree healthCodeSelectionTree;

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new health code selection browse panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param session the session
	 */
	public HealthCodeSelectionBrowsePanel(
		JDialog parentDialog, 
		RIFStudySubmissionToolSession session) {
		
		this.parentDialog = parentDialog;
		this.session = session;
		this.uiFactory = session.getUIFactory();
		
		buildUI();
	}

	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		panel = uiFactory.createPanel();
		GridBagConstraints panelGC 
			= uiFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
	
		healthCodeSelectionTree
			= new HealthCodeSelectionTree(parentDialog, session);
		JScrollPane treeSelectionPane
			= uiFactory.createScrollPane(healthCodeSelectionTree.getTree());
		panel.add(treeSelectionPane, panelGC);
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
				"healthCodeSelectionDialog.browse.label");
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
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @param existingHealthCodesForInvestigation the existing health codes for investigation
	 */
	public void resetPanel(
		HealthCodeTaxonomy healthCodeTaxonomy,
		ArrayList<HealthCode> existingHealthCodesForInvestigation) {

		healthCodeSelectionTree.initialiseTree(healthCodeTaxonomy);		
	}
	
	/**
	 * Gets the selected health codes.
	 *
	 * @return the selected health codes
	 */
	public ArrayList<HealthCode> getSelectedHealthCodes() {
		return healthCodeSelectionTree.getSelectedHealthCodes();
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

}

