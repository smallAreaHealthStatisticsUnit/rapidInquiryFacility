package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.sahsu.rif.dataloader.concepts.ConfigurationHints;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.DescriptiveConfigurationItem;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.OKCloseButtonDialog;
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

public class ConfigurationHintsEditorDialog 
	extends OKCloseButtonDialog 
	implements ListSelectionListener {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderToolSession session;
	
	
	private ConfigurationHintTablePanel dataSetHintTablePanel;
	private DataSetPropertyEditorPanel dataSetPropertyEditorPanel;
	
	private ConfigurationHintTablePanel dataSetFieldHintTablePanel;
	private DataSetFieldPropertyEditorPanel dataSetFieldPropertyEditorPanel;
		
	// ==========================================
	// Section Construction
	// ==========================================

	public ConfigurationHintsEditorDialog(final DataLoaderToolSession session) {
		super(session.getUserInterfaceFactory());

		this.session = session;
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("configurationHintsEditorDialog.title");
		setDialogTitle(dialogTitle);
		String instructions
			= RIFDataLoaderToolMessages.getMessage("configurationHintsEditorDialog.instructions");
		setInstructionText(instructions);
		setMainPanel(createMainPanel());
		buildUI();
		setSize(1200, 550);
	}
	
	private JPanel createMainPanel() {

		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();

		dataSetHintTablePanel
			= new ConfigurationHintTablePanel(
				userInterfaceFactory);
		dataSetHintTablePanel.addActionListener(this);
		dataSetHintTablePanel.addListSelectionListener(this);
		dataSetPropertyEditorPanel
			= new DataSetPropertyEditorPanel(
				session,
				null);
		
		dataSetFieldHintTablePanel
			= new ConfigurationHintTablePanel(
				userInterfaceFactory);
		dataSetFieldHintTablePanel.addActionListener(this);
		dataSetFieldHintTablePanel.addListSelectionListener(this);
		dataSetFieldPropertyEditorPanel
			= new DataSetFieldPropertyEditorPanel(
				getDialog(),
				session,
				true);
		
		
		
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		JTabbedPane tabbedPane
			= userInterfaceFactory.createTabbedPane();
		
		String dataSetConfigurationHintTitle
			= RIFDataLoaderToolMessages.getMessage(
					"configurationHintsEditorDialog.dataSetConfigurationHints.title");
		tabbedPane.addTab(
			dataSetConfigurationHintTitle,
			createDataSetConfigurationHintPanel());	
		
		String dataSetFieldConfigurationHintsTitle
			= RIFDataLoaderToolMessages.getMessage(
				"configurationHintsEditorDialog.dataSetFieldConfigurationHints.title");
		tabbedPane.addTab(
			dataSetFieldConfigurationHintsTitle,
			createDataSetFieldConfigurationHintPanel());
		panel.add(tabbedPane, panelGC);
		
		return panel;
	}

	private JPanel createDataSetConfigurationHintPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
	

		JPanel rightPanel = userInterfaceFactory.createBorderLayoutPanel();
		rightPanel.add(dataSetPropertyEditorPanel.getPanel(), BorderLayout.NORTH);
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				dataSetHintTablePanel.getPanel(), 
				rightPanel);
		splitPane.setDividerLocation(400);
		panel.add(splitPane, panelGC);
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;		
	}
	
	private JPanel createDataSetFieldConfigurationHintPanel() {
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		JPanel panel = userInterfaceFactory.createPanel();
		GridBagConstraints panelGC 
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
		
		JPanel rightPanel = userInterfaceFactory.createBorderLayoutPanel();
		rightPanel.add(dataSetFieldPropertyEditorPanel.getPanel(), BorderLayout.NORTH);
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				dataSetFieldHintTablePanel.getPanel(), 
				rightPanel);
		splitPane.setDividerLocation(350);
		panel.add(splitPane, panelGC);
		panel.setBorder(LineBorder.createGrayLineBorder());
		
		return panel;		
	}
	
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setData(final ConfigurationHints configurationHints) {
		ArrayList<DataSetConfiguration> dataSetConfigurationHints
			= configurationHints.getDataSetConfigurationHints();
		ArrayList<DescriptiveConfigurationItem> dataSetItems
			= new ArrayList<DescriptiveConfigurationItem>();
		for (DataSetConfiguration dataSetConfigurationHint : dataSetConfigurationHints) {
			dataSetItems.add(dataSetConfigurationHint);
		}
		if (dataSetItems.isEmpty()) {
			dataSetPropertyEditorPanel.setIsEnabled(false);
		}
		else {
			dataSetPropertyEditorPanel.setIsEnabled(true);
			dataSetHintTablePanel.selectFirstItem();
		}
		dataSetHintTablePanel.setData(dataSetItems);
		
		ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurationHints
			= configurationHints.getDataSetFieldConfigurationHints();
		
		ArrayList<DescriptiveConfigurationItem> dataSetFieldItems
			= new ArrayList<DescriptiveConfigurationItem>();
		for (DataSetFieldConfiguration dataSetFieldConfigurationHint : dataSetFieldConfigurationHints) {
			dataSetFieldItems.add(dataSetFieldConfigurationHint);
		}
		if (dataSetFieldItems.isEmpty()) {
			dataSetFieldPropertyEditorPanel.setIsEnabled(false);			
		}
		else {
			dataSetFieldPropertyEditorPanel.setIsEnabled(true);
			dataSetFieldHintTablePanel.selectFirstItem();			
		}
		dataSetFieldHintTablePanel.setData(dataSetFieldItems);
	}
	
	public ArrayList<DataSetConfiguration> getDataSetConfigurationHints() {
		ArrayList<DescriptiveConfigurationItem> dataSetConfigurationItems
			= dataSetHintTablePanel.getAllHintItems();
		ArrayList<DataSetConfiguration> results 
			= new ArrayList<DataSetConfiguration>();
		for (DescriptiveConfigurationItem dataSetConfigurationItem : dataSetConfigurationItems) {
			results.add( (DataSetConfiguration) dataSetConfigurationItem);
		}
		
		return results;
	}
		
	public ArrayList<DataSetFieldConfiguration> getDataSetFieldConfigurationHints() {
		ArrayList<DescriptiveConfigurationItem> dataSetFieldConfigurationItems
			= dataSetFieldHintTablePanel.getAllHintItems();
		
		ArrayList<DataSetFieldConfiguration> results 
			= new ArrayList<DataSetFieldConfiguration>();
		for (DescriptiveConfigurationItem dataSetFieldConfigurationItem : dataSetFieldConfigurationItems) {
			results.add( (DataSetFieldConfiguration) dataSetFieldConfigurationItem);
		}
		
		return results;
	}
		
	private void addDataSetConfigurationHint() {
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.newInstance();
		dataSetConfiguration.setName("Configuration Data Set Hint");
		dataSetConfiguration.setDescription("What it does");
		dataSetConfiguration.setIsHint(true);
		dataSetHintTablePanel.addHintItem(dataSetConfiguration);			
	}
	
	private void addDataSetFieldConfigurationHint() {
		DataSetFieldConfiguration dataSetFieldConfiguration
			= DataSetFieldConfiguration.newInstance();
		dataSetFieldConfiguration.setCoreFieldName("Configuration Data Set Field Hint");
		dataSetFieldConfiguration.setCoreFieldDescription("What it does");
		dataSetFieldConfiguration.setIsHint(true);
		dataSetFieldHintTablePanel.addHintItem(dataSetFieldConfiguration);			
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	//Interface: List Selection Listener
	public void valueChanged(final ListSelectionEvent event) {
		if (event.getValueIsAdjusting()) {
			return;
		}
		
		Object listSelectionModel = event.getSource();
		if (dataSetHintTablePanel.generatedListSelectionChangeEvent(listSelectionModel)) {
			//save changes made to the old one
			DataSetConfiguration currentlyEditedDataSetConfiguration
				= dataSetPropertyEditorPanel.getData();

			dataSetPropertyEditorPanel.saveChanges();

			dataSetHintTablePanel.updateItem(
				currentlyEditedDataSetConfiguration);
 			DataSetConfiguration dataSetConfigurationItem
				= (DataSetConfiguration) dataSetHintTablePanel.getSelectedHintItem();
			dataSetPropertyEditorPanel.setData(
				dataSetConfigurationItem);
		}
		else if (dataSetFieldHintTablePanel.generatedListSelectionChangeEvent(listSelectionModel)) {
			//save changes made to the old one
			DataSetFieldConfiguration currentlyEditedDataSetConfiguration
				= dataSetFieldPropertyEditorPanel.getData();
			if (currentlyEditedDataSetConfiguration != null) {
				String coreFieldName = currentlyEditedDataSetConfiguration.getCoreFieldName();
			}
			dataSetFieldPropertyEditorPanel.saveChanges();
			if (currentlyEditedDataSetConfiguration != null) {
				String coreFieldName = currentlyEditedDataSetConfiguration.getCoreFieldName();
			}

			dataSetFieldHintTablePanel.updateItem(
				currentlyEditedDataSetConfiguration);
 			DataSetFieldConfiguration dataSetFieldConfigurationItem
				= (DataSetFieldConfiguration) dataSetFieldHintTablePanel.getSelectedHintItem();
 			DataSetConfiguration dataSetConfiguration
 				= DataSetConfiguration.newInstance();
 			
 			Geography currentlySelectedGeography
 				= dataSetPropertyEditorPanel.getSelectedGeography();
			dataSetFieldPropertyEditorPanel.setData(
				dataSetConfiguration,
				currentlySelectedGeography,
				dataSetFieldConfigurationItem);
		}
	}
	
	// ==========================================
	// Section Override
	// ==========================================
	public void actionPerformed(final ActionEvent event) {
		Object button = event.getSource();

		/*
		 * Here we have to be careful about how we handle action listener events
		 * because this class and two instances of ConfigurationHintTablePanel
		 * also implement the action listener interface.  
		 * 
		 * It appears that when you press a button on one of the hint table panels,
		 * the event first comes to this class first.  
		 * 
		 * The hint table panel is designed so that wherever possible, it looks after
		 * actions related to its own list.  Managing the state of its buttons is 
		 * something that it can hide from everything else.  Actions like pressing
		 * the "delete" button are also contained within these classes because
		 * it does the same action no matter what kind of configuration hint it is
		 * managing.
		 * 
		 * However adding is something that this class handles.  If the add button on
		 * one of the hint buttons is pressed, this class picks up the responsibility
		 * because the creation of the DataSetConfiguration is different than
		 * the creation of the DataSetFieldConfiguration, and I'd prefer to keep knowledge
		 * about these business concepts away from the ConfigurationHint GUI classes
		 * wherever possible.  The UI classes are designed so that they may be reused
		 * regardless of whether we are managing configuration hints for fields or 
		 * configuration hints for data sets.
		 * 
		 */
		
		if (dataSetHintTablePanel.isAddButton(button)) {
			addDataSetConfigurationHint();
		}
		if (dataSetFieldHintTablePanel.isAddButton(button)) {
			addDataSetFieldConfigurationHint();
		}
		else if (dataSetHintTablePanel.performActionListenerActivities(event) == false) {
			if (dataSetFieldHintTablePanel.performActionListenerActivities(event) == false) {
				//we have explored every other possibility.  The event wasn't fired
				//by either the data set hint panel or the data set field hint panel.
				//The only other source of events would be the OK and Close buttons,
				//which are managed in the superclass OKCloseButtonDialog.
				performOKCloseActions(event);				
			}
		}
	}

	public void okAction() {
		dataSetPropertyEditorPanel.saveChanges();
		dataSetFieldPropertyEditorPanel.saveChanges();
	}
}


