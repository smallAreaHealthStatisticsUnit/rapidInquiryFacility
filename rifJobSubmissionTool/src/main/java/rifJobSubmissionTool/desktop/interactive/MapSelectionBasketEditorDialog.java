package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.MapAreaSelectionBasket;
import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;
import rifJobSubmissionTool.system.RIFSession;
import rifJobSubmissionTool.util.UserInterfaceFactory;

import rifServices.businessConceptLayer.AbstractGeographicalArea;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

class MapSelectionBasketEditorDialog 
	implements ActionListener, 
	Observer {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	
	//Data
	/** The current geographical area. */
	private AbstractGeographicalArea currentGeographicalArea;
	
	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The map area selection table view. */
	private MapAreaSelectionTableView mapAreaSelectionTableView;
	/** The dialog. */
	private JDialog dialog;
	/** The status label. */
	private JLabel statusLabel;	
	/** The delete selected items button. */
	private JButton deleteSelectedItemsButton;
	/** The close button. */
	private JButton closeButton;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map selection basket editor dialog.
	 *
	 * @param rifSession the rif session
	 */
	public MapSelectionBasketEditorDialog(
		RIFSession rifSession) {

		this.userInterfaceFactory = rifSession.getUIFactory();
		
		mapAreaSelectionTableView
			= new MapAreaSelectionTableView(dialog, rifSession, false);

		String deleteItemButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.buttons.delete.label");
		deleteSelectedItemsButton
			= userInterfaceFactory.createButton(deleteItemButtonText);
		deleteSelectedItemsButton.addActionListener(this);
		
		String closeButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.buttons.close.label");
		closeButton
			= userInterfaceFactory.createButton(closeButtonText);
		closeButton.addActionListener(this);

	}

	/**
	 * Builds the ui.
	 */
	private void buildUI() {
		
		String dialogTitleText
			= RIFJobSubmissionToolMessages.getMessage(
				"mapSelectionBasketEditor.title");
		dialog = userInterfaceFactory.createDialog(dialogTitleText);				

		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		panel.add(mapAreaSelectionTableView.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(createButtonPanel(), panelGC);

		dialog.getContentPane().add(panel);
		dialog.setSize(600, 500);
		dialog.setModal(true);
	}
	
	/**
	 * Creates the button panel.
	 *
	 * @return the j panel
	 */
	private JPanel createButtonPanel() {
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		
		panelGC.anchor = GridBagConstraints.SOUTHWEST;
		statusLabel
			= userInterfaceFactory.createLabel("");
		panel.add(statusLabel, panelGC);
				
		panelGC.gridx++;
		panelGC.anchor = GridBagConstraints.SOUTHEAST;		
		panel.add(deleteSelectedItemsButton, panelGC);
		
		panelGC.gridx++;
		panel.add(closeButton, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Show.
	 */
	public void show() {
		
		dialog.setVisible(true);
	}
	
	/**
	 * Initialise.
	 *
	 * @param currentGeographical the current geographical
	 * @param currentMapAreaSelectionBasket the current map area selection basket
	 */
	public void initialise(
		AbstractGeographicalArea currentGeographical,
		MapAreaSelectionBasket currentMapAreaSelectionBasket) {
		
		mapAreaSelectionTableView.initialiseForm(
			currentGeographicalArea,
			currentMapAreaSelectionBasket);
		currentMapAreaSelectionBasket.addObserver(this);
		mapAreaSelectionTableView.showFirstResultPage();
		buildUI();
	}
	
	/**
	 * Delete selected items button.
	 */
	public void deleteSelectedItemsButton() {
		
		mapAreaSelectionTableView.deleteSelectedItems();
	}
	
	/**
	 * Close.
	 */
	public void close() {
		
		dialog.setVisible(false);
	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Action Listener
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(
		ActionEvent event) {
		
		Object button = event.getSource();
		
		if (button == deleteSelectedItemsButton) {
			deleteSelectedItemsButton();
		}
		else if (button == closeButton) {
			close();
		}		
	}
	
	//Interface: Action Listener
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(
		Observable observable, 
		Object argument) {

		mapAreaSelectionTableView.updateDisplay();
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}

