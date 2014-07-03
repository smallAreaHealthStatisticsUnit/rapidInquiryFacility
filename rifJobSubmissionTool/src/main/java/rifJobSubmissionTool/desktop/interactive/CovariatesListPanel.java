
package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFSession;
import rifJobSubmissionTool.util.UserInterfaceFactory;

import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.DisplayableListItem;
import rifServices.system.RIFServiceMessages;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.event.ListSelectionListener;
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


class CovariatesListPanel implements ActionListener {
	
// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	//Data
	/** The rif session. */
	private RIFSession rifSession;
	
	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The panel. */
	private JPanel panel;
	/** The list panel. */
	private OrderedListPanel listPanel;	
	/** The list editing button control panel. */
	private ListEditingButtonControlPanel listEditingButtonControlPanel;
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new covariates list panel.
     *
     * @param rifSession the rif session
     */
	public CovariatesListPanel(
		RIFSession rifSession) {
   
		this.rifSession = rifSession;
		this.userInterfaceFactory = rifSession.getUIFactory();
		panel = userInterfaceFactory.createPanel();
		
		String panelTitleLabelText
			= RIFServiceMessages.getMessage("covariate.plural.label");
		String panelTitleLabelToolTipText
			= RIFServiceMessages.getMessage("covariate.plural.toolTip");		
	    listPanel
	    	= new OrderedListPanel(
	    		panelTitleLabelText,
	    		panelTitleLabelToolTipText,
	    		userInterfaceFactory,
	    		true);
		
		listEditingButtonControlPanel
			= new ListEditingButtonControlPanel(userInterfaceFactory);
		listEditingButtonControlPanel.setIncludeEditButton(false);
		listEditingButtonControlPanel.setIncludeCopyButton(false);
		listEditingButtonControlPanel.setIncludeDeleteButton(true);
		listEditingButtonControlPanel.addActionListener(this);
		
		buildUI();
    }

    /**
     * Builds the ui.
     */
    private void buildUI() {
    	
    	panel.removeAll();

		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
    	
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1.0;
		panelGC.weighty = 1.0;
		panel.add(listPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		listEditingButtonControlPanel.buildUI();
		panel.add(listEditingButtonControlPanel.getPanel(), panelGC);
    }
    
// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	/**
	 * Adds the list selection listener.
	 *
	 * @param listSelectionListener the list selection listener
	 */
    public void addListSelectionListener(
    	ListSelectionListener listSelectionListener) {
	
		listPanel.addListSelectionListener(listSelectionListener);
	}
	
	/**
	 * Gets the covariates.
	 *
	 * @return the covariates
	 */
	public ArrayList<AbstractCovariate> getCovariates() {
		
		ArrayList<AbstractCovariate> covariates
			= new ArrayList<AbstractCovariate>();
		
		ArrayList<DisplayableListItem> listItems
			= listPanel.getAllItems();
		for (DisplayableListItem listItem : listItems) {
			covariates.add((AbstractCovariate) listItem);
		}
		return covariates;
	}
	
	/**
	 * Gets the currently selected covariate.
	 *
	 * @return the currently selected covariate
	 */
	public AbstractCovariate getCurrentlySelectedCovariate() {
		
		AbstractCovariate currentlySelectedCovariate
			= (AbstractCovariate) listPanel.getSelectedItem();
		return currentlySelectedCovariate;		
	}
	
	/**
	 * Clear list.
	 */
	public void clearList() {
		
		listPanel.clearList();
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
     * Sets the data.
     *
     * @param covariates the new data
     */
    public void setData(
    	ArrayList<AbstractCovariate> covariates) {
    	
    	listPanel.clearList();
    	for (AbstractCovariate covariate : covariates) {
    		listPanel.addListItem(covariate);
    	}
    	updateListGUIFeatures();
    }
    
	/**
	 * Adds the covariate.
	 */
	private void addCovariate() {
		
		CovariatesSelectionDialog covariateSelectionDialog
			= new CovariatesSelectionDialog(rifSession);
		
		covariateSelectionDialog.show();
		if (covariateSelectionDialog.isCancelled() == true) {
			return;
		}
		
		ArrayList<AbstractCovariate> covariatesToAdd
			= covariateSelectionDialog.getSelectedCovariates();
		for (AbstractCovariate covariateToAdd : covariatesToAdd) {
			listPanel.addListItem(covariateToAdd);
		}
		
		listPanel.updateUI();
		updateListGUIFeatures();		
	}
		
	/**
	 * Delete selected covariates.
	 */
	private void deleteSelectedCovariates() {
		
		listPanel.deleteSelectedListItems();
		listPanel.updateUI();
		updateListGUIFeatures();		
	}

	/**
	 * Update list gui features.
	 */
	private void updateListGUIFeatures() {	
		
		if (listPanel.isEmpty() == true) {
			listEditingButtonControlPanel.setEmptyState();
		}
		else {
			listPanel.setSelectedItem(0);
			listEditingButtonControlPanel.setNonEmptyState();			
		}
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
		
		if (listEditingButtonControlPanel.isAddButton(button)) {
			addCovariate();
		}
		else if (listEditingButtonControlPanel.isDeleteButton(button)) {
			deleteSelectedCovariates();
		}		
	}
	
// ==========================================
// Section Override
// ==========================================




}
