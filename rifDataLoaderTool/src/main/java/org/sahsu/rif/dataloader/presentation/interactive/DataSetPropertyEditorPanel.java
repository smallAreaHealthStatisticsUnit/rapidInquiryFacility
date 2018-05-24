package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.concepts.HealthTheme;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;

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

public class DataSetPropertyEditorPanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private boolean isRenderingForConfigurationHintsFeature;
	private DataLoaderToolConfiguration dataLoaderToolConfiguration;
	private DataSetConfiguration dataSetConfiguration;
	private GeographyMetaData geographyMetaData;
	
	private UserInterfaceFactory userInterfaceFactory;
	private JPanel panel;
	private JTextField nameTextField;
	private JTextField versionTextField;
	private JTextArea descriptionTextArea;
	private JTextField filePathTextField;
	
	private JComboBox<String> geographyComboBox;
	private JComboBox<String> healthThemesComboBox;
	private JComboBox<String> denominatorComboBox;
	
	private JComboBox<String> rifSchemaAreasComboBox;
	private RIFSchemaArea preSetSchemaArea;
	
	private ArrayList<ActionListener> actionListeners;
	// ==========================================
	// Section Construction
	// ==========================================
	
	public DataSetPropertyEditorPanel(
		final DataLoaderToolSession session,
		final RIFSchemaArea preSetSchemaArea) {
		
		userInterfaceFactory = session.getUserInterfaceFactory();
		dataLoaderToolConfiguration
			= session.getDataLoaderToolConfiguration();
		//assume data loader configuration is not ever going to be null
		geographyMetaData 
			= session.getDataLoaderToolConfiguration().getGeographyMetaData();
		
		this.preSetSchemaArea = preSetSchemaArea;
		if (preSetSchemaArea == null) {
			isRenderingForConfigurationHintsFeature = true;			
		}
		else {
			isRenderingForConfigurationHintsFeature = false;						
		}
		
		actionListeners = new ArrayList<ActionListener>();
		buildUI();	
	}

	public void buildUI() {

		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panel
			= userInterfaceFactory.createPanel();
		
		String nameFieldLabelText = "";
		if (isRenderingForConfigurationHintsFeature) {
			nameFieldLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"configurationHint.regularExpressionPattern.label");
		}
		else {
			nameFieldLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.name.label");			
		}
		JLabel nameFieldLabel
			= userInterfaceFactory.createLabel(nameFieldLabelText);
		String nameFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.name.toolTip");
		nameFieldLabel.setToolTipText(nameFieldToolTip);
		
		panel.add(nameFieldLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		nameTextField
			= userInterfaceFactory.createTextField();
		panel.add(nameTextField, panelGC);
		
		panelGC.gridy++;
			
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;		
		String versionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.version.label");
		JLabel versionFieldLabel
			= userInterfaceFactory.createLabel(versionFieldLabelText);
		String versionFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.version.toolTip");
		versionFieldLabel.setToolTipText(versionFieldToolTip);
		
		panel.add(versionFieldLabel, panelGC);		
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		versionTextField
			= userInterfaceFactory.createTextField();
		panel.add(versionTextField, panelGC);
		
		panelGC.gridy++;		
		panelGC.gridx = 0;
		panelGC.fill = GridBagConstraints.NONE;
		String descriptionFieldLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.description.label");
		JLabel descriptionFieldLabel
			= userInterfaceFactory.createLabel(descriptionFieldLabelText);
		String descriptionFieldToolTip
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.description.toolTip");
		descriptionFieldLabel.setToolTipText(descriptionFieldToolTip);
		
		panel.add(descriptionFieldLabel, panelGC);
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		descriptionTextArea
			= userInterfaceFactory.createTextArea();
		panel.add(
			descriptionTextArea, 
			panelGC);

		//Adding in Geography combo box
		panelGC.gridy++;
		panelGC.gridx = 0;		
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		String geographyLabelText
			= RIFDataLoaderToolMessages.getMessage(
				"dlGeography.singular.label");
		JLabel geographyLabel
			= userInterfaceFactory.createLabel(geographyLabelText);
		panel.add(geographyLabel, panelGC);	
		panelGC.gridx++;
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		
		String[] geographyNames
			= geographyMetaData.getAllGeographyNames();
		geographyComboBox
			= userInterfaceFactory.createComboBox(geographyNames);
		panel.add(geographyComboBox, panelGC);
		
		if (preSetSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {	
			//Adding in HealthTheme combo box
			panelGC.gridy++;
			panelGC.gridx = 0;		
			panelGC.fill = GridBagConstraints.NONE;
			panelGC.weightx = 0;
			String healthThemesLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"dlHealthTheme.singular.label");
			JLabel healthThemesLabel
				= userInterfaceFactory.createLabel(healthThemesLabelText);
			panel.add(healthThemesLabel, panelGC);	
			panelGC.gridx++;
			panelGC.fill = GridBagConstraints.HORIZONTAL;
			panelGC.weightx = 1;
		
			String[] healthThemeNames
				= dataLoaderToolConfiguration.getHealthThemeNames();
			healthThemesComboBox
				= userInterfaceFactory.createComboBox(
					dataLoaderToolConfiguration.getHealthThemeNames());
			panel.add(healthThemesComboBox, panelGC);
		
			//Adding in Denominator combo box
			panelGC.gridy++;
			panelGC.gridx = 0;		
			panelGC.fill = GridBagConstraints.NONE;
			panelGC.weightx = 0;
			String denominatorsLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetPropertyEditorPanel.denominator.label");
			JLabel denominatorsLabel
				= userInterfaceFactory.createLabel(denominatorsLabelText);
			panel.add(denominatorsLabel, panelGC);	
			panelGC.gridx++;
			panelGC.fill = GridBagConstraints.HORIZONTAL;
			panelGC.weightx = 1;
			denominatorComboBox
				= userInterfaceFactory.createComboBox(
					dataLoaderToolConfiguration.getDenominatorNames());
			panel.add(denominatorComboBox, panelGC);
		}
		
		if (isRenderingForConfigurationHintsFeature == true) {
			//If we are rendering configuration hints, we want to select
			//a RIF Schema Area.  If we're adjusting properties as we
			//actually edit a Denominator, Numerator or Covariate
			//data set then the RIFSchemaArea is already chosen for us
			
			panelGC.gridy++;
			panelGC.gridx = 0;		
			panelGC.fill = GridBagConstraints.NONE;
			panelGC.weightx = 0;
			String rifSchemaAreasLabelText
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.rifSchemaArea.label");
			JLabel rifSchemaAreasLabel
				= userInterfaceFactory.createLabel(rifSchemaAreasLabelText);
			String rifSchemaAreasToolTip
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.rifSchemaArea.toolTip");		
			rifSchemaAreasLabel.setToolTipText(rifSchemaAreasToolTip);
			panel.add(rifSchemaAreasLabel, panelGC);	
			panelGC.gridx++;
			panelGC.fill = GridBagConstraints.HORIZONTAL;
			panelGC.weightx = 1;
			rifSchemaAreasComboBox
				= userInterfaceFactory.createComboBox(
					RIFSchemaArea.getAllSchemaNames());			
			panel.add(rifSchemaAreasComboBox, panelGC);
			
		}

		filePathTextField
			= userInterfaceFactory.createNonEditableTextField();
		if (isRenderingForConfigurationHintsFeature == false) {
			panelGC.gridy++;
			panelGC.gridx = 0;		
			panelGC.fill = GridBagConstraints.NONE;
			String filePathLabelText
				= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.filePath.label");
			JLabel filePathLabel
				= userInterfaceFactory.createLabel(filePathLabelText);
			panel.add(filePathLabel, panelGC);
			panelGC.gridx++;
			panelGC.fill = GridBagConstraints.HORIZONTAL;
			panelGC.weightx = 1;

			panel.add(filePathTextField, panelGC);		
		}
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public DataSetConfiguration getData() {
		return dataSetConfiguration;
	}
	
	public void setData(
		final DataSetConfiguration dataSetConfiguration) {
		this.dataSetConfiguration = dataSetConfiguration;
		populateForm(dataSetConfiguration);
	}
	
	public RIFSchemaArea getSelectedRIFSchemaArea() {
		String rifSchemaAreaName
			= (String)rifSchemaAreasComboBox.getSelectedItem();
		return RIFSchemaArea.getSchemaAreaFromName(rifSchemaAreaName);
	}
	
	public Geography getSelectedGeography() {
		String selectedGeographyName
			= (String) geographyComboBox.getSelectedItem();
		return geographyMetaData.getGeography(selectedGeographyName);
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void saveChanges() {
		populateDataSetConfigurationFromForm(dataSetConfiguration);
	}
	
	public void populateDataSetConfigurationFromForm(
		final DataSetConfiguration dataSetConfiguration) {

		if (dataSetConfiguration == null) {
			return;
		}
		//capture the general fields
		dataSetConfiguration.setName(nameTextField.getText().trim());
		dataSetConfiguration.setVersion(versionTextField.getText().trim());
		dataSetConfiguration.setDescription(descriptionTextArea.getText().trim());

		if (isRenderingForConfigurationHintsFeature == true) {			
			String rifSchemaPhrase
				= (String) rifSchemaAreasComboBox.getSelectedItem();
			RIFSchemaArea rifSchemaArea = RIFSchemaArea.getSchemaAreaFromName(rifSchemaPhrase);
			dataSetConfiguration.setRIFSchemaArea(rifSchemaArea);
		}	
		
		String currentlySelectedGeographyName
			= (String) geographyComboBox.getSelectedItem();
		Geography currentGeography
			= geographyMetaData.getGeography(currentlySelectedGeographyName);
		dataSetConfiguration.setGeography(currentGeography);
		
		if (preSetSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
			String healthThemeName
				= (String) healthThemesComboBox.getSelectedItem();
			HealthTheme healthTheme
				= dataLoaderToolConfiguration.getHealthTheme(healthThemeName);
			dataSetConfiguration.setHealthTheme(healthTheme);
			
			String selectedDenominatorName
				= (String) denominatorComboBox.getSelectedItem();
			DataSetConfiguration denominator
				= dataLoaderToolConfiguration.getDenominator(selectedDenominatorName);
			dataSetConfiguration.setDependencyDataSetConfiguration(denominator);
		}
	}
	
	public void setIsEnabled(final boolean isEnabled) {
		nameTextField.setEnabled(isEnabled);
		versionTextField.setEnabled(isEnabled);
		descriptionTextArea.setEnabled(isEnabled);
		
		userInterfaceFactory.setEditableAppearance(nameTextField, isEnabled);
		userInterfaceFactory.setEditableAppearance(versionTextField, isEnabled);
		userInterfaceFactory.setEditableAppearance(descriptionTextArea, isEnabled);
		
		if (isRenderingForConfigurationHintsFeature == true) {
			rifSchemaAreasComboBox.setEnabled(isEnabled);			
		}
		
		geographyComboBox.setEnabled(isEnabled);
		if (preSetSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
			healthThemesComboBox.setEnabled(isEnabled);
			denominatorComboBox.setEnabled(isEnabled);
		}
	}
	
	public void populateForm(final DataSetConfiguration dataSetConfiguration) {
		if (dataSetConfiguration == null) {
			setIsEnabled(false);
		}
		else {
			setIsEnabled(true);

			nameTextField.setText(dataSetConfiguration.getName());
			versionTextField.setText(dataSetConfiguration.getVersion());
			descriptionTextArea.setText(dataSetConfiguration.getDescription());			
			filePathTextField.setText(dataSetConfiguration.getFilePath());
		
			removeAllActionListeners();
			if (isRenderingForConfigurationHintsFeature == true) {				
				RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();			
				if (rifSchemaArea != null) {
					rifSchemaAreasComboBox.setSelectedItem(rifSchemaArea.getName());				
				}
			}
			
			Geography geography = dataSetConfiguration.getGeography();
			if (geography != null) {
				geographyComboBox.setSelectedItem(geography.getDisplayName());		
			}
			restoreAllActionListeners();
		
			if (preSetSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {				
				HealthTheme healthTheme
					= dataSetConfiguration.getHealthTheme();
				if (healthTheme != null) {
					healthThemesComboBox.setSelectedItem(healthTheme.getDisplayName());
				}

				DataSetConfiguration denominator
					= dataSetConfiguration.getDependencyDataSetConfiguration();
				if (denominator != null) {
					denominatorComboBox.setSelectedItem(denominator.getDisplayName());					
				}
			}
		}
	}
	
	public void removeActionListener(final ActionListener actionListener) {
		actionListeners.remove(actionListener);
		if (rifSchemaAreasComboBox != null) {
			rifSchemaAreasComboBox.removeActionListener(actionListener);			
		}
		geographyComboBox.removeActionListener(actionListener);	
	}
	
	private void removeAllActionListeners() {
		for (ActionListener actionListener : actionListeners) {
			if (rifSchemaAreasComboBox != null) {
				rifSchemaAreasComboBox.removeActionListener(actionListener);			
			}
			geographyComboBox.removeActionListener(actionListener);			
		}
	}
	
	private void restoreAllActionListeners() {
		for (ActionListener actionListener : actionListeners) {
			if (rifSchemaAreasComboBox != null) {
				rifSchemaAreasComboBox.addActionListener(actionListener);			
			}
			geographyComboBox.addActionListener(actionListener);			
		}
	}
	
	public void addActionListener(final ActionListener actionListener) {
		actionListeners.add(actionListener);
		
		if (rifSchemaAreasComboBox != null) {
			rifSchemaAreasComboBox.addActionListener(actionListener);			
		}
		geographyComboBox.addActionListener(actionListener);
	}
	
	public boolean isSchemaAreaComboBox(final Object eventObject) {
		return(eventObject == rifSchemaAreasComboBox);
	}
	
	public boolean isGeographyComboBox(final Object eventObject) {
		return(eventObject == geographyComboBox);
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


