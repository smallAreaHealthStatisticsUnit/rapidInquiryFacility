package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.DLHealthTheme;
import rifGenericLibrary.presentationLayer.OKCloseButtonDialog;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.util.FieldValidationUtility;
import rifGenericLibrary.system.RIFServiceException;

import java.util.ArrayList;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

public class HealthThemeEditorDialog extends OKCloseButtonDialog {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DLHealthTheme healthTheme;
	private String originalDisplayName;
	private JTextField healthThemeNameField;
	private JTextArea healthThemeDescriptionTextArea;
	private ArrayList<String> currentDisplayNames;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public HealthThemeEditorDialog(
		final UserInterfaceFactory userInterfaceFactory,
		final ArrayList<String> currentDisplayNames) {

		super(userInterfaceFactory);
		
		this.currentDisplayNames = currentDisplayNames;
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("healthThemeEditorDialog.title");
		setDialogTitle(dialogTitle);
		setMainPanel(createMainPanel());
		setSize(300, 250);
		buildUI();
	}

	private JPanel createMainPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(createNameFieldPanel(), panelGC);

		panelGC.gridy++;
		JLabel healthThemeDescriptionLabel
			= userInterfaceFactory.createDescriptionLabel();
		panel.add(healthThemeDescriptionLabel, panelGC);
		panelGC.gridy++;
		healthThemeDescriptionTextArea
			= userInterfaceFactory.createTextArea();
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(healthThemeDescriptionTextArea);
		panel.add(scrollPane, panelGC);
		
		return panel;
	}
	
	private JPanel createNameFieldPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panel.add(userInterfaceFactory.createNameLabel());
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		healthThemeNameField = userInterfaceFactory.createTextField();
		panel.add(healthThemeNameField, panelGC);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setData(final DLHealthTheme healthTheme) {
		this.healthTheme = healthTheme;
		originalDisplayName = healthTheme.getDisplayName();
		currentDisplayNames.remove(originalDisplayName);
		populateForm();
	}
	
	public DLHealthTheme getData() {
		return healthTheme;
	}
	
	private void populateForm() {
		healthThemeNameField.setText(healthTheme.getName());
		healthThemeDescriptionTextArea.setText(healthTheme.getDescription());
	}

	private DLHealthTheme getDataFromForm() {
		DLHealthTheme formHealthTheme
			= DLHealthTheme.newInstance(
				healthThemeNameField.getText().trim(),
				healthThemeDescriptionTextArea.getText().trim());
		return formHealthTheme;
	}
	
	@Override
	public void okAction() 
		throws RIFServiceException {
		
		DLHealthTheme formHealthTheme = getDataFromForm();
		String revisedDisplayName = formHealthTheme.getDisplayName();
		
		if (originalDisplayName != revisedDisplayName) {

			FieldValidationUtility.checkListDuplicate(
				revisedDisplayName, 
				currentDisplayNames);

			//Error: Attempting to update item to appear to be something already in the list

		}
		DLHealthTheme.copyInto(formHealthTheme, healthTheme);
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

	public void actionPerformed(final ActionEvent event) {
		performOKCloseActions(event);
	}	
}


