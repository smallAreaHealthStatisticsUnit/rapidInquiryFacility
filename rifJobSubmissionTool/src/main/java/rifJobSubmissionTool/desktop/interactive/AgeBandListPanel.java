package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFSession;
import rifJobSubmissionTool.util.UserInterfaceFactory;

import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.DisplayableListItem;
import rifServices.businessConceptLayer.Investigation;
import rifServices.system.RIFServiceMessages;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JPanel;


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
 *<p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *</p>
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

class AgeBandListPanel 
	implements ActionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	/** The rif session. */
	private RIFSession rifSession;
	/** The current investigation. */
	private Investigation currentInvestigation;

	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;	
	/** The panel. */
	private JPanel panel;	
	/** The list panel. */
	private OrderedListPanel listPanel;	
	/** The editing button panel. */
	private ListEditingButtonControlPanel editingButtonPanel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new age band list panel.
	 *
	 * @param parentDialog the parent dialog
	 * @param rifSession the rif session
	 */
	public AgeBandListPanel(
		JDialog parentDialog,
		RIFSession rifSession) {

		this.rifSession = rifSession;
		
		userInterfaceFactory
			= rifSession.getUIFactory();
		panel = userInterfaceFactory.createPanel();

		String title
			= RIFServiceMessages.getMessage("ageBand.plural.label");
		String titleToolTipText
			= RIFServiceMessages.getMessage("ageBand.toolTip");				
		listPanel
	    	= new OrderedListPanel(
	    		title,
	    		titleToolTipText,
	    		userInterfaceFactory,
	    		true);
		editingButtonPanel
			= new ListEditingButtonControlPanel(userInterfaceFactory);
		editingButtonPanel.setIncludeCopyButton(false);
		editingButtonPanel.addActionListener(this);		
	}

	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		panel.removeAll();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;		
		panel.add(listPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;	
		editingButtonPanel.buildUI();	
		panel.add(editingButtonPanel.getPanel(), panelGC);		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Gets the panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel() {
		
		return panel;
	}
	
	/**
	 * Adds the age band.
	 */
	private void addAgeBand() {
		
		AgeBandEditorDialog ageBandEditorDialog 
			= new AgeBandEditorDialog(rifSession);
		AgeBand ageBand = AgeBand.newInstance();
		ageBand.setNewRecord(true);
		ageBandEditorDialog.initialise(
			currentInvestigation, 
			ageBand);
		ageBandEditorDialog.show();
		if (ageBandEditorDialog.isCancelled() == false) {
			listPanel.addListItem(ageBand);
		}		

		listPanel.updateUI();
		updateListGUIFeatures();
	}
	
	/**
	 * Edits the age band.
	 */
	private void editAgeBand() {
		
		AgeBand selectedAgeBand
			= (AgeBand) listPanel.getSelectedItem();
		AgeBand copyOfSelectedAgeBand
			= AgeBand.createCopy(selectedAgeBand);
		
		AgeBandEditorDialog ageBandEditorDialog 
			= new AgeBandEditorDialog(rifSession);
		selectedAgeBand.setNewRecord(false);
		ageBandEditorDialog.initialise(
			currentInvestigation, 
			copyOfSelectedAgeBand);
		ageBandEditorDialog.show();
		if (ageBandEditorDialog.isCancelled() == false) {
			listPanel.replaceItem(selectedAgeBand, copyOfSelectedAgeBand);
		}	
		listPanel.updateUI();
	}
	
	/**
	 * Delete age band.
	 */
	private void deleteAgeBand() {
		
		listPanel.deleteSelectedListItems();
		updateListGUIFeatures();
	}
	
	/**
	 * Gets the age bands.
	 *
	 * @return the age bands
	 */
	public ArrayList<AgeBand> getAgeBands() {
		
		ArrayList<DisplayableListItem> listItems
			= listPanel.getAllItems();
		ArrayList<AgeBand> ageBands = new ArrayList<AgeBand>();
		for (DisplayableListItem listItem : listItems) {
			ageBands.add((AgeBand) listItem);
		}
		return ageBands;
	}
	
	/**
	 * Initialise.
	 *
	 * @param currentInvestigation the current investigation
	 */
	public void initialise(
		Investigation currentInvestigation) {
		
		this.currentInvestigation = currentInvestigation;
		listPanel.clearList();
		ArrayList<AgeBand> ageBands = currentInvestigation.getAgeBands();
		for (AgeBand ageBand : ageBands) {
			listPanel.addListItem(ageBand);			
		}
		buildUI();		
		updateListGUIFeatures();
	}
	
	/**
	 * Update list gui features.
	 */
	private void updateListGUIFeatures() {
		
		if (listPanel.isEmpty()) {
			editingButtonPanel.setEmptyState();
		}
		else {
			listPanel.setSelectedItem(0);
			editingButtonPanel.setNonEmptyState();
		}
	}
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	public void actionPerformed(
		ActionEvent event) {
		
		Object source = event.getSource();
		
		if (editingButtonPanel.isAddButton(source)) {
			addAgeBand();
		}
		else if (editingButtonPanel.isEditButton(source)) {
			editAgeBand();
		}
		else if (editingButtonPanel.isDeleteButton(source)) {
			deleteAgeBand();
		}		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

