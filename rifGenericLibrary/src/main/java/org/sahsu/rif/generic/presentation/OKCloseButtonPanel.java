
package org.sahsu.rif.generic.presentation;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.sahsu.rif.generic.system.Messages;

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
 * Copyright 2017 Imperial College London, developed by the Small Area
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


public final class OKCloseButtonPanel {

// ==========================================
// Section Constants
// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================

	//Data
	private UserInterfaceFactory userInterfaceFactory;
	private boolean includeOKButton;
	
	private ArrayList<JButton> otherButtons;
	
	//GUI Components
	/** The ok button. */
	private JButton okButton;
	/** The close button. */
	private JButton closeButton;
	/** The panel. */
	private JPanel panel;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new OK close panel.
     *
     * @param userInterfaceFactory the user interface factory
     */
	public OKCloseButtonPanel(
		final UserInterfaceFactory userInterfaceFactory) {		

		this.userInterfaceFactory = userInterfaceFactory;
		includeOKButton = true;
		otherButtons = new ArrayList<JButton>();
		panel = userInterfaceFactory.createPanel();
		buildUI();
    }

	public OKCloseButtonPanel(
		final UserInterfaceFactory userInterfaceFactory,
		boolean includeOKButton) {

		this.userInterfaceFactory = userInterfaceFactory;
		this.includeOKButton = includeOKButton;
		otherButtons = new ArrayList<JButton>();		
		buildUI();
	}
	
	public void buildUI() {
		panel.removeAll();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.anchor = GridBagConstraints.SOUTHWEST;
		
		int numberOfOtherButtons = otherButtons.size();
		for (int i = 0; i < numberOfOtherButtons; i++) {
			if (i != 0) {
				panelGC.gridx++;
			}
			panel.add(otherButtons.get(i), panelGC);				
		}
		
		panelGC.gridx++;			
		
		String okText
			= GENERIC_MESSAGES.getMessage("buttons.ok.label");
		okButton = userInterfaceFactory.createButton(okText);

		if (includeOKButton) {
			panel.add(okButton, panelGC);
		}
		
		panelGC.gridx++;
		String closeText
			= GENERIC_MESSAGES.getMessage("buttons.close.label");
		closeButton = userInterfaceFactory.createButton(closeText);
		panel.add(closeButton, panelGC);		
	}
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Adds the action listener.
     *
     * @param actionListener the action listener
     */
	public void addActionListener(ActionListener actionListener) {
    	okButton.addActionListener(actionListener);
    	closeButton.addActionListener(actionListener);
    }
    
	/**
	 * Gets the panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel() {
		return panel;
	}
	
	public void addButton(final JButton otherButton) {
		otherButtons.add(otherButton);
	}
	
	
	/**
	 * Checks if is OK button.
	 *
	 * @param source the source
	 * @return true, if is OK button
	 */
	public boolean isOKButton(
		Object source) {

		if (source == okButton) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is close button.
	 *
	 * @param source the source
	 * @return true, if is close button
	 */
	public boolean isCloseButton(
		Object source) {

		if (source == closeButton) {
			return true;
		}
		return false;		
	}

	public void setEnableOKButton(final boolean isEnabled) {
		okButton.setEnabled(isEnabled);
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
