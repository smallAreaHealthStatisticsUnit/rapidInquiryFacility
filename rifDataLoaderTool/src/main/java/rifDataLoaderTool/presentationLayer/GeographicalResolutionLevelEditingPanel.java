package rifDataLoaderTool.presentationLayer;

import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import javax.swing.*;
import javax.swing.border.LineBorder;

import rifDataLoaderTool.businessConceptLayer.GeographicalResolutionLevel;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import java.awt.*;

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

public class GeographicalResolutionLevelEditingPanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private UserInterfaceFactory userInterfaceFactory;
	private JPanel panel;
	
	private JTextField nameTextField;
	private JTextArea descriptionTextArea;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GeographicalResolutionLevelEditingPanel(final UserInterfaceFactory userInterfaceFactory) {
		this.userInterfaceFactory = userInterfaceFactory;
		
		panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		String nameLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionLevel.name.label");
		JLabel nameLabel
			= userInterfaceFactory.createLabel(nameLabelText);
		panel.add(nameLabel, panelGC);
		panelGC.gridx++;		
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		nameTextField = userInterfaceFactory.createTextField();
		panel.add(nameTextField, panelGC);
		
		panelGC.gridy++;
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		String descriptionLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"geographicalResolutionLevel.description.label");
		JLabel descriptionLabel
			= userInterfaceFactory.createLabel(descriptionLabelText);
		panel.add(descriptionLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		descriptionTextArea
			= userInterfaceFactory.createTextArea();
		JScrollPane scrollPane
			= userInterfaceFactory.createScrollPane(descriptionTextArea);
		panel.add(scrollPane, panelGC);
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public JPanel getPanel() {
		return panel;
	}
	
	public void resetForm() {
		nameTextField.setText("");
		descriptionTextArea.setText("");
	}
	
	public GeographicalResolutionLevel getLevelFromForm() {
		GeographicalResolutionLevel levelFromForm
			= GeographicalResolutionLevel.newInstance();
		levelFromForm.setName(nameTextField.getText().trim());
		levelFromForm.setDescription(descriptionTextArea.getText().trim());
		
		return levelFromForm;		
	}
	
	public void setData(final GeographicalResolutionLevel geographicalResolutionLevel) {
		if (geographicalResolutionLevel == null) {
			resetForm();
			return;
		}
		nameTextField.setText(geographicalResolutionLevel.getName());
		descriptionTextArea.setText(geographicalResolutionLevel.getDescription());		
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


