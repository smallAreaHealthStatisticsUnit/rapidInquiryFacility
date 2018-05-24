package org.sahsu.rif.generic.presentation;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.sahsu.rif.generic.system.Messages;

/**
 *
 * A generic button control panel that supports navigation through a work flow.  Only buttons that
 * can be pressed appear in any state.  For example, if the work flow is set on the first state, 
 * the previous and first button will not show.  It also suggests the distinction between "First" and
 * "Start Again" buttons.  The "First" button is meant to move the work flow to the first step in the 
 * work flow.  In a workflow, it is meant to suggest that if you go back to the first step, you will
 * preserve the work you've done in that step.  "Start Again" suggests that when the work flow moves 
 * to the first step, but all changed all the work done before will be discarded.
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

public final class WorkflowNavigationButtonPanel 
	extends AbstractNavigationPanel {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	private JPanel panel;
	
	private JButton startAgainButton;
		
	private JButton submitButton;
	private JButton quitButton;	
		
	// ==========================================
	// Section Construction
	// ==========================================

	public WorkflowNavigationButtonPanel(
		final UserInterfaceFactory userInterfaceFactory) {
	
		super(userInterfaceFactory);

		String submitButtonText
			= GENERIC_MESSAGES.getMessage("buttons.submit.label");
		submitButton
			= userInterfaceFactory.createButton(submitButtonText);
		
		String quitButtonText
			= GENERIC_MESSAGES.getMessage("buttons.quit.label");
		quitButton
			= userInterfaceFactory.createButton(quitButtonText);
		
		panel = userInterfaceFactory.createBorderLayoutPanel();

	}

	public void startAgainButton(JButton startAgainButton) {
		this.startAgainButton = startAgainButton;
	}
	
	public boolean isStartAgainButton(Object item) {
		if (startAgainButton == null) {
			return false;
		}
		
		return startAgainButton.equals(item);
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public boolean isQuitButton(
		final Object item) {
			
		if (item == null) {
			return false;
		}
		return quitButton.equals(item);
	}

	public boolean isSubmitButton(
		final Object item) {
			
		if (item == null) {
			return false;
		}
		return submitButton.equals(item);
	}
	
	
	public void showStartState() {
		panel.removeAll();

		JPanel rightPanel = userInterfaceFactory.createPanel();
		GridBagConstraints rightPanelGC = userInterfaceFactory.createGridBagConstraints();
		
		rightPanel.add(nextButton, rightPanelGC);
		rightPanelGC.gridx++;
		rightPanel.add(quitButton, rightPanelGC);
		rightPanelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(rightPanel, BorderLayout.EAST);
		
		panel.updateUI();
	}
	
	public void showMiddleState() {
		panel.removeAll();
		panel.add(createLeftPanel(), BorderLayout.WEST);
		
		JPanel rightPanel = userInterfaceFactory.createPanel();
		GridBagConstraints rightPanelGC = userInterfaceFactory.createGridBagConstraints();
		rightPanel.add(firstButton, rightPanelGC);
		rightPanelGC.gridx++;
		rightPanel.add(previousButton, rightPanelGC);
		rightPanelGC.gridx++;
		rightPanel.add(nextButton, rightPanelGC);
		rightPanelGC.gridx++;
		rightPanel.add(quitButton, rightPanelGC);
		rightPanelGC.anchor = GridBagConstraints.SOUTHEAST;
		panel.add(rightPanel, BorderLayout.EAST);
		panel.updateUI();
	}
	
	public void showEndState() {
		panel.removeAll();
		panel.add(createLeftPanel(), BorderLayout.WEST);

		JPanel rightPanel = userInterfaceFactory.createPanel();
		GridBagConstraints rightPanelGC = userInterfaceFactory.createGridBagConstraints();
		rightPanelGC.anchor = GridBagConstraints.SOUTHEAST;
		rightPanel.add(firstButton, rightPanelGC);
		rightPanelGC.gridx++;
		rightPanel.add(previousButton, rightPanelGC);
		rightPanelGC.gridx++;		
		rightPanel.add(submitButton, rightPanelGC);
		panel.add(rightPanel, BorderLayout.EAST);		
		panel.updateUI();
	}
	
	private JPanel createLeftPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		
		if (startAgainButton != null) {
			GridBagConstraints panelGC 
				= userInterfaceFactory.createGridBagConstraints();
			panelGC.anchor = GridBagConstraints.SOUTHWEST;
			panel.add(startAgainButton, panelGC);
		}
		
		return panel;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void addActionListener(
		final ActionListener actionListener) {
	
		super.addActionListener(actionListener);
		startAgainButton.addActionListener(actionListener);		
		submitButton.addActionListener(actionListener);
		quitButton.addActionListener(actionListener);
				
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
