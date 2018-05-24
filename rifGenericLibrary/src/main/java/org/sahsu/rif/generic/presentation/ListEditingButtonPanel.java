package org.sahsu.rif.generic.presentation;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.sahsu.rif.generic.system.Messages;

/**
 *
 * Convenience class for creating and managing panels of buttons associated with
 * list operations.  When you set up an instance, do the following:
 * <ol>
 * <li>
 * use "includeXXX(...)" methods to establish which buttons will appear in the button panel.
 * The order in which you call the include methods is the order in which the buttons will appear
 * from left to right
 * </li>
 * <li> 
 * call "leftJustifyButtons()" or "rightJustifyButtons()", which determines whether the
 * panel shows buttons flushed to the left (southwest corner) or right (southeast corner)
 * </li>
 * <li>
 * call the "addActionListener(..)" method to establish which class will be responsible for 
 * the actionPerformed(ActionEvent event) method.
 * </li>
 * <li>
 * call getPanel(), which will assemble the buttons and return a panel with them 
 * </li>
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

public final class ListEditingButtonPanel {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	private boolean isLeftJustified;
	
	private UserInterfaceFactory userInterfaceFactory;
	private ArrayList<JButton> buttonsToInclude;
	private ArrayList<ActionListener> actionListeners;
		
	private JPanel panel;	
	private JButton addButton;
	private JButton editButton;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton clearButton;
	private JButton importButton;
	private JButton exportButton;
	
	private boolean includeBorder;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ListEditingButtonPanel(
		final UserInterfaceFactory userInterfaceFactory) {
		
		this.userInterfaceFactory = userInterfaceFactory;
		buttonsToInclude = new ArrayList<JButton>();
		actionListeners = new ArrayList<ActionListener>();
		
		panel = userInterfaceFactory.createPanel();
		
		String addButtonText
			= GENERIC_MESSAGES.getMessage("buttons.add.label");
		addButton = userInterfaceFactory.createButton(addButtonText);
		
		String editButtonText
			= GENERIC_MESSAGES.getMessage("buttons.edit.label");
		editButton = userInterfaceFactory.createButton(editButtonText);

		String copyButtonText
			= GENERIC_MESSAGES.getMessage("buttons.copy.label");
		copyButton = userInterfaceFactory.createButton(copyButtonText);

		String deleteButtonText
			= GENERIC_MESSAGES.getMessage("buttons.delete.label");
		deleteButton = userInterfaceFactory.createButton(deleteButtonText);
		
		String clearButtonText
			= GENERIC_MESSAGES.getMessage("buttons.clear.label");
		clearButton = userInterfaceFactory.createButton(clearButtonText);
		
		String importButtonText
			= GENERIC_MESSAGES.getMessage("buttons.import.label");
		importButton = userInterfaceFactory.createButton(importButtonText);
		
		String exportButtonText
			= GENERIC_MESSAGES.getMessage("buttons.export.label");
		exportButton = userInterfaceFactory.createButton(exportButtonText);
		
		isLeftJustified = false;
		includeBorder = false;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setIncludeBorder(
		final boolean includeBorder) {
		
		this.includeBorder = includeBorder;
		
	}
	
	public void includeAddButton(
		final String toolTipText) {

		if (toolTipText != null) {
			addButton.setToolTipText(toolTipText);
		}
		
		buttonsToInclude.add(addButton);
	}

	public JButton getAddButton() {

		return addButton;
	}
	
	public boolean isAddButton(Object item) {

		if (item == null) {
			return false;
		}		
		return addButton.equals(item);
	}
	
	public void includeEditButton(
		final String toolTipText) {

		if (toolTipText != null) {
			editButton.setToolTipText(toolTipText);
		}
		
		buttonsToInclude.add(editButton);		
	}

	public boolean isEditButton(
		final Object item) {
		
		if (item == null) {
			return false;
		}		
		return editButton.equals(item);
	}
	
	
	public JButton getEditButton() {

		return editButton;
	}
	

	public void includeCopyButton(
		final String toolTipText) {

		if (toolTipText != null) {
			copyButton.setToolTipText(toolTipText);
			
			
		}
		buttonsToInclude.add(copyButton);		
	}

	public boolean isCopyButton(
		final Object item) {
		
		if (item == null) {
			return false;
		}		
		return copyButton.equals(item);
	}
		
		
	public JButton getCopyButton() {

		return copyButton;
	}
	
	public void includeDeleteButton(
		final String toolTipText) {

		if (toolTipText != null) {
			deleteButton.setToolTipText(toolTipText);
		}

		buttonsToInclude.add(deleteButton);			
	}
	
	public JButton getDeleteButton() {
		return deleteButton;
	}

	public boolean isDeleteButton(
		final Object item) {

		if (item == null) {
			return false;
		}		
		return deleteButton.equals(item);
	}
	
	public void includeClearButton(
		final String toolTipText) {

		if (toolTipText != null) {
			clearButton.setToolTipText(toolTipText);
		}
				
		buttonsToInclude.add(clearButton);		
	}
	
	public JButton getClearButton() {

		return clearButton;
	}

	public boolean isClearButton(
		final Object item) {

		if (item == null) {
			return false;
		}		
		return clearButton.equals(item);
	}

	public void includeImportButton(
		final String toolTipText) {

		if (toolTipText != null) {
			importButton.setToolTipText(toolTipText);
		}
			
		buttonsToInclude.add(importButton);				
	}
	
	public JButton getImportButton() {
		return importButton;
	}
	
	public boolean isImportButton(
		final Object item) {

		if (item == null) {
			return false;
		}		
		return importButton.equals(item);
	}
	
	public void includeExportButton(
		final String toolTipText) {
		
		if (toolTipText != null) {
			exportButton.setToolTipText(toolTipText);
		}
			
		buttonsToInclude.add(exportButton);				
	}
	
	public JButton getExportButton() {

		return exportButton;
	}
	
	public boolean isExportButton(
		final Object item) {

		if (item == null) {
			return false;
		}		
		return exportButton.equals(item);		
	}
	
	public void leftJustifyButtons() {
		isLeftJustified = true;
	}
	
	public void rightJustifyButtons() {
		isLeftJustified = false;
	}
	
	public void addSpecialisedButton(JButton specialisedButton) {
		buttonsToInclude.add(specialisedButton);
	}
	
	public void addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
		refreshActionListenersForButtons();
	}

	
	private void refreshActionListenersForButtons() {
		
		for (JButton buttonToInclude : buttonsToInclude) {
			setActionListenersForButton(buttonToInclude);
		}
	}
	
	public void setActionListenersForButton(JButton button) {
		for (ActionListener actionListener : actionListeners) {
			button.removeActionListener(actionListener);
		}

		for (ActionListener actionListener : actionListeners) {
			button.addActionListener(actionListener);
		}
		
	}
	
	public void indicateEmptyState() {
		addButton.setEnabled(true);
		
		String editButtonText
			= GENERIC_MESSAGES.getMessage("buttons.edit.label");
		editButton.setText(editButtonText);		
		editButton.setEnabled(false);
		copyButton.setEnabled(false);
		deleteButton.setEnabled(false);
		clearButton.setEnabled(false);
	}

	public void indicatePopulatedState() {
		addButton.setEnabled(true);
		
		String editButtonText
			= GENERIC_MESSAGES.getMessage("buttons.edit.label");
		editButton.setText(editButtonText);		
		editButton.setEnabled(true);
		copyButton.setEnabled(true);
		deleteButton.setEnabled(true);
		clearButton.setEnabled(true);
	}
	
	public void indicateViewOnlyState() {
		addButton.setEnabled(false);
		
		String viewButtonText
			= GENERIC_MESSAGES.getMessage("buttons.view.label");
		editButton.setText(viewButtonText);
		editButton.setEnabled(true);
		copyButton.setEnabled(false);
		deleteButton.setEnabled(false);
		clearButton.setEnabled(false);		
	}
	
	public void indicateViewAndCopyState() {
		addButton.setEnabled(true);
		
		String viewButtonText
			= GENERIC_MESSAGES.getMessage("buttons.view.label");
		editButton.setText(viewButtonText);
		editButton.setEnabled(true);
		copyButton.setEnabled(true);
		deleteButton.setEnabled(false);
		clearButton.setEnabled(false);		
	}	
	
	public void indicateFullEditingState() {
		addButton.setEnabled(true);
		
		String editButtonText
			= GENERIC_MESSAGES.getMessage("buttons.edit.label");
		editButton.setText(editButtonText);
		editButton.setEnabled(true);
		copyButton.setEnabled(true);
		deleteButton.setEnabled(true);
		clearButton.setEnabled(true);		
	}	
	
	public void disableAllButtons() {
		for (JButton buttonToInclude : buttonsToInclude) {
			buttonToInclude.setEnabled(false);
		}
	}
	
	public void enableAllButtons() {
		for (JButton buttonToInclude : buttonsToInclude) {
			buttonToInclude.setEnabled(true);
		}		
	}
	
	
	public void sensitiseSpecialisedButton(
		final JButton sensitisedButton,
		final boolean isSensitised) {
		
		sensitisedButton.setEnabled(isSensitised);
	}

	public JPanel getPanel() {
		panel.removeAll();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.gridx = 0;
		for (JButton buttonToInclude : buttonsToInclude) {
			setActionListenersForButton(buttonToInclude);
			panel.add(buttonToInclude, panelGC);
			panelGC.gridx++;
		}
		
		if (includeBorder == true) {
			panel.setBorder(LineBorder.createGrayLineBorder());
		}
		else {
			panel.setBorder(null);
		}
		
		return panel;
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
