
package rifJobSubmissionTool.desktopApplication;

import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;

import rifJobSubmissionTool.util.UserInterfaceFactory;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
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


public class ListEditingButtonControlPanel {

// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	
	//Data
	private boolean includeAddButton;
	/** The include edit button. */
	private boolean includeEditButton;
	/** The include copy button. */
	private boolean includeCopyButton;	
	/** The include delete button. */
	private boolean includeDeleteButton;	
	
	//GUI Components	
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The add button. */
	private JButton addButton;
	/** The edit button. */
	private JButton editButton;
	/** The copy button. */
	private JButton copyButton;
	/** The delete button. */
	private JButton deleteButton;
	/** The include add button. */
	/** The panel. */
	private JPanel panel;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new list editing button control panel.
     *
     * @param userInterfaceFactory the user interface factory
     */
	public ListEditingButtonControlPanel(
		UserInterfaceFactory userInterfaceFactory) {		

		this.userInterfaceFactory = userInterfaceFactory;
		
		includeAddButton = true;
		includeEditButton = true;
		includeCopyButton = true;
		includeDeleteButton = true;		

		panel = userInterfaceFactory.createPanel();
    	//instantiate the buttons
		String addButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.buttons.add.label");
		addButton = userInterfaceFactory.createButton(addButtonText);
		String editButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.buttons.edit.label");
		editButton = userInterfaceFactory.createButton(editButtonText);
		String copyButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.buttons.copy.label");		
		copyButton = userInterfaceFactory.createButton(copyButtonText);
		String deleteButtonText
			= RIFJobSubmissionToolMessages.getMessage("general.buttons.delete.label");
		deleteButton = userInterfaceFactory.createButton(deleteButtonText);
    }

    /**
     * Builds the ui.
     */
    public void buildUI() {
    	
    	panel.removeAll();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.anchor = GridBagConstraints.SOUTHEAST;
		
		ArrayList<JButton> buttonsToInclude = new ArrayList<JButton>();
		if (includeAddButton) {
			buttonsToInclude.add(addButton);
		}		
		if (includeEditButton) {
			buttonsToInclude.add(editButton);			
		}
		if (includeCopyButton) {
			buttonsToInclude.add(copyButton);			
		}
		if (includeDeleteButton) {
			buttonsToInclude.add(deleteButton);			
		}
		
		for (int i = 0; i < buttonsToInclude.size(); i++) {
			if (i != 0) {
				panelGC.gridx++;
			}
			panel.add(buttonsToInclude.get(i), panelGC);				
		}		
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
	 * Adds the action listener.
	 *
	 * @param actionListener the action listener
	 */
	public void addActionListener(
		ActionListener actionListener) {

		addButton.addActionListener(actionListener);
		editButton.addActionListener(actionListener);
		copyButton.addActionListener(actionListener);
		deleteButton.addActionListener(actionListener);
	}
	
	/**
	 * Checks if is adds the button.
	 *
	 * @param source the source
	 * @return true, if is adds the button
	 */
	public boolean isAddButton(
		Object source) {

		if (source == addButton) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is edits the button.
	 *
	 * @param source the source
	 * @return true, if is edits the button
	 */
	public boolean isEditButton(
		Object source) {

		if (source == editButton) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is copy button.
	 *
	 * @param source the source
	 * @return true, if is copy button
	 */
	public boolean isCopyButton(
		Object source) {

		if (source == copyButton) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is delete button.
	 *
	 * @param source the source
	 * @return true, if is delete button
	 */
	public boolean isDeleteButton(
		Object source) {

		if (source == deleteButton) {
			return true;
		}
		return false;
	}
		
	/**
	 * Sets the include add button.
	 *
	 * @param includeAddButton the new include add button
	 */
	public void setIncludeAddButton(
		boolean includeAddButton) {

		this.includeAddButton = includeAddButton;
	}
	
	/**
	 * Sets the include edit button.
	 *
	 * @param includeEditButton the new include edit button
	 */
	public void setIncludeEditButton(
		boolean includeEditButton) {

		this.includeEditButton = includeEditButton;
	}
	
	/**
	 * Sets the include copy button.
	 *
	 * @param includeCopyButton the new include copy button
	 */
	public void setIncludeCopyButton(
		boolean includeCopyButton) {

		this.includeCopyButton = includeCopyButton;
	}
	
	/**
	 * Sets the include delete button.
	 *
	 * @param includeDeleteButton the new include delete button
	 */
	public void setIncludeDeleteButton(
		boolean includeDeleteButton) {

		this.includeDeleteButton = includeDeleteButton;		
	}
	
	/**
	 * Sets the empty state.
	 */
	public void setEmptyState() {

		editButton.setEnabled(false);
		copyButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}
	
	/**
	 * Sets the non empty state.
	 */
	public void setNonEmptyState() {
		
		editButton.setEnabled(true);
		copyButton.setEnabled(true);
		deleteButton.setEnabled(true);		
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
