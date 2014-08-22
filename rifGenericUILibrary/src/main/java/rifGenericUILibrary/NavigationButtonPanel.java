package rifGenericUILibrary;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.util.ArrayList;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public class NavigationButtonPanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	private ArrayList<ActionListener> actionListeners;

	private UserInterfaceFactory userInterfaceFactory;

	private JPanel panel;
	
	private JButton startAgainButton;
	
	private JButton firstButton;
	private JButton previousButton;
	private JButton nextButton;
	private JButton lastButton;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public NavigationButtonPanel(
		final UserInterfaceFactory userInterfaceFactory) {
		
		this.userInterfaceFactory = userInterfaceFactory;

		
		String firstButtonText
			= RIFGenericUIMessages.getMessage("buttons.first.label");
		firstButton
			= userInterfaceFactory.createButton(firstButtonText);

		String previousButtonText
			= RIFGenericUIMessages.getMessage("buttons.previous.label");
		previousButton
			= userInterfaceFactory.createButton(previousButtonText);

		String nextButtonText
			= RIFGenericUIMessages.getMessage("buttons.next.label");
		nextButton
			= userInterfaceFactory.createButton(nextButtonText);
		
		String lastButtonText
			= RIFGenericUIMessages.getMessage("buttons.last.label");
		lastButton
			= userInterfaceFactory.createButton(lastButtonText);		
	}

	public void startAgainButton(JButton resetButton) {
		this.startAgainButton = startAgainButton;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	public boolean isFirstButton(
		final Object item) {
		
		if (item == null) {
			return false;
		}
		return firstButton.equals(item);
	}
	
	public JButton getFirstButton() {
		return firstButton;
	}
	
	public boolean isPreviousButton(
			final Object item) {
			
		if (item == null) {
			return false;
		}
		return previousButton.equals(item);
	}
		
	public JButton getPreviousButton() {
		return previousButton;
	}
	
	public boolean isNextButton(
			final Object item) {
			
		if (item == null) {
			return false;
		}
		return nextButton.equals(item);
	}
		
	public JButton getNextButton() {
		return nextButton;
	}
	
	public boolean isLastButton(
			final Object item) {
			
		if (item == null) {
			return false;
		}
		return lastButton.equals(item);
	}
		
	public JButton getLastButton() {
		return lastButton;
	}
		
	public void indicateBeginningState() {

		firstButton.setEnabled(false);
		previousButton.setEnabled(false);
		nextButton.setEnabled(true);
		lastButton.setEnabled(true);		
	}
	
	
	public void indicateEndingState() {

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
	
	public void buildUI() {
		
	}
	
	public JPanel getPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();

		panelGC.gridx = 0;
		if (startAgainButton != null) {
			panelGC.anchor = GridBagConstraints.SOUTHWEST;
			panel.add(startAgainButton, panelGC);			
		}

		panelGC.anchor = GridBagConstraints.SOUTHEAST;

		panelGC.gridx++;
		panel.add(firstButton, panelGC);
		panelGC.gridx++;
		panel.add(previousButton, panelGC);
		panelGC.gridx++;
		panel.add(nextButton, panelGC);
		panelGC.gridx++;
		panel.add(lastButton, panelGC);
		
		return panel;		
	}
	
	public void addActionListener(
		final ActionListener actionListener) {
	
		actionListeners.add(actionListener);
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
