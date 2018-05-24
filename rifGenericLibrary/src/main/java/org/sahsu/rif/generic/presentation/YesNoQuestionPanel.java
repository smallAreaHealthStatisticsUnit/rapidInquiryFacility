package org.sahsu.rif.generic.presentation;

import java.awt.GridBagConstraints;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.sahsu.rif.generic.system.Messages;

/**
 *
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

public final class YesNoQuestionPanel {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	private UserInterfaceFactory userInterfaceFactory;
	private boolean useYesAsDefaultChoice;
	private JPanel panel;
	private JLabel questionLabel;
	private JRadioButton yesChoice;
	private JRadioButton noChoice;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public YesNoQuestionPanel(
		final String questionText,
		final UserInterfaceFactory userInterfaceFactory,
		final boolean useYesAsDefaultChoice) {
		
		this.userInterfaceFactory = userInterfaceFactory;
		this.useYesAsDefaultChoice = useYesAsDefaultChoice;
		
		panel = userInterfaceFactory.createPanel();

		questionLabel
			= userInterfaceFactory.createLabel(questionText);
		String yesChoiceText
			= GENERIC_MESSAGES.getMessage("radioButtons.yes.label");
		yesChoice
			= userInterfaceFactory.createRadioButton(yesChoiceText);
		String noChoiceText
			= GENERIC_MESSAGES.getMessage("radioButtons.no.label");
		noChoice
			= userInterfaceFactory.createRadioButton(noChoiceText);
		ButtonGroup buttonGroup
			= userInterfaceFactory.createButtonGroup();
		buttonGroup.add(yesChoice);
		buttonGroup.add(noChoice);

	}

	public void buildUI() {
		
		panel.removeAll();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.anchor = GridBagConstraints.NORTHWEST;
		panel.add(questionLabel, panelGC);
		
		panelGC.gridx++;
		panel.add(yesChoice, panelGC);
			
		panelGC.gridx++;
		panel.add(noChoice, panelGC);
		
		if (useYesAsDefaultChoice) {
			yesChoice.setSelected(true);
		}
		else {
			noChoice.setSelected(true);			
		}
	}
	
	public JLabel getQuestionLabel() {
		return questionLabel;
	}
	
	public JPanel createAnswerPanel() {
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC = userInterfaceFactory.createGridBagConstraints();
		panel.add(questionLabel, panelGC);
		
		panelGC.gridx++;
		panel.add(yesChoice, panelGC);
		panelGC.gridx++;
		panel.add(noChoice, panelGC);
		return panel;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public boolean isYesSelected() {
		return yesChoice.isSelected();
	}
	
	public void setIsYesSelected(
		final boolean isYesSelected) {
		
		if (isYesSelected == true) {
			yesChoice.setSelected(true);
		}
		else {
			noChoice.setSelected(true);
		}
	}
	
	public void setToYes() {
		yesChoice.setSelected(true);		
	}
	
	public void setToNo() {
		noChoice.setSelected(true);		
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
