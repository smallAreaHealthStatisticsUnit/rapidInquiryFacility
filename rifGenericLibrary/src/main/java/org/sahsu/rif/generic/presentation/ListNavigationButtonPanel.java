package org.sahsu.rif.generic.presentation;

import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;

/**
 * A generic control panel to help support navigation in a list.  Here, the button panel
 * uses a policy of desensitising any button that can't be used.  For example, if you're at
 * the first item in the list, the first and previous button will become desensitised.  Whereas
 * the {@link rifGenericLibrary.WorkflowNavigationButtonPanel} enforces the idea that you 
 * can't skip steps, here, you can go to "Last" without having to go through "Next".
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

public final class ListNavigationButtonPanel 
	extends AbstractNavigationPanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	private JPanel panel;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ListNavigationButtonPanel(
		final UserInterfaceFactory userInterfaceFactory) {
		
		super(userInterfaceFactory);

		panel = createPanel();
	}

	private JPanel createPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panelGC.anchor = GridBagConstraints.SOUTHEAST;		
		
		panel.add(firstButton, panelGC);
		panelGC.gridx++;
		panel.add(previousButton, panelGC);
		panelGC.gridx++;
		panel.add(nextButton, panelGC);
		panelGC.gridx++;
		panel.add(lastButton, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void showFirstItemState() {
		panel.removeAll();

		firstButton.setEnabled(false);
		previousButton.setEnabled(false);
		nextButton.setEnabled(true);
		lastButton.setEnabled(true);
	}

	public void showOnlyItemState() {

		firstButton.setEnabled(false);
		previousButton.setEnabled(false);
		nextButton.setEnabled(false);
		lastButton.setEnabled(false);		
	}	
	
	public void showMiddleItemState() {

		firstButton.setEnabled(true);
		previousButton.setEnabled(true);
		nextButton.setEnabled(true);
		lastButton.setEnabled(true);		
	}
	
	public void showLastItemState() {

		firstButton.setEnabled(true);
		previousButton.setEnabled(true);
		nextButton.setEnabled(false);
		lastButton.setEnabled(false);		
	}
	
	public void disableAllButtons() {

		firstButton.setEnabled(false);
		previousButton.setEnabled(false);
		nextButton.setEnabled(false);
		lastButton.setEnabled(false);		
	}
	
	public void enableAllButtons() {

		firstButton.setEnabled(true);
		previousButton.setEnabled(true);
		nextButton.setEnabled(true);
		lastButton.setEnabled(true);		
	}	
		
	public JPanel getPanel() {
		return panel;
	}
	
	public void addActionListener(
		final ActionListener actionListener) {
	
		super.addActionListener(actionListener);
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
