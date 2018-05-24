package org.sahsu.rif.generic.presentation;

import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.sahsu.rif.generic.system.Messages;

/**
 * A convenience class for holding properties of navigation panels.
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

public abstract class AbstractNavigationPanel {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	protected UserInterfaceFactory userInterfaceFactory;
	
	protected JButton firstButton;
	protected JButton previousButton;
	protected JButton nextButton;
	protected JButton lastButton;

	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractNavigationPanel(
		final UserInterfaceFactory userInterfaceFactory) {
		
		this.userInterfaceFactory = userInterfaceFactory;
		
		String firstButtonText
			= GENERIC_MESSAGES.getMessage("buttons.first.label");
		firstButton
			= userInterfaceFactory.createButton(firstButtonText);

		String previousButtonText
			= GENERIC_MESSAGES.getMessage("buttons.previous.label");
		previousButton
			= userInterfaceFactory.createButton(previousButtonText);

		String nextButtonText
			= GENERIC_MESSAGES.getMessage("buttons.next.label");
		nextButton
			= userInterfaceFactory.createButton(nextButtonText);
	
		String lastButtonText
			= GENERIC_MESSAGES.getMessage("buttons.last.label");
		lastButton
			= userInterfaceFactory.createButton(lastButtonText);
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
	
	protected JButton getLastButton() {
		return lastButton;
	}

	public void addActionListener(
		final ActionListener actionListener) {
		
		firstButton.addActionListener(actionListener);
		previousButton.addActionListener(actionListener);
		nextButton.addActionListener(actionListener);
		lastButton.addActionListener(actionListener);
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
