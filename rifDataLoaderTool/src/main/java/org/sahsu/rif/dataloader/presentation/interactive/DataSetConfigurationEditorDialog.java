package org.sahsu.rif.dataloader.presentation.interactive;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldNameGenerator;
import org.sahsu.rif.dataloader.concepts.DataSetValidator;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.ListEditingButtonPanel;
import org.sahsu.rif.generic.presentation.NamedListItemDialog;
import org.sahsu.rif.generic.presentation.OKCloseButtonDialog;
import org.sahsu.rif.generic.presentation.OrderedListPanel;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

class DataSetConfigurationEditorDialog
		extends OKCloseButtonDialog
	implements ListSelectionListener,
	ListDataListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	private DataLoaderToolSession session;
	private RIFSchemaArea rifSchemaArea;
	private DataSetConfiguration originalDataSetConfiguration;
	private DataSetConfiguration workingCopyDataSetConfiguration;
		
	//GUI Components	
	private OrderedListPanel dataSetFieldListPanel;
	private ListEditingButtonPanel dataSetFieldListButtonPanel;
	private JScrollPane currentFieldScrollPane;	
	
	private DataSetPropertyEditorPanel dataSetPropertyEditorPanel;
	
	private DataSetFieldPropertyEditorPanel dataSetFieldPropertyEditorPanel;
	private int previouslySelectedIndex;
	private boolean isRollbackSelectionHappening;
	
	// ==========================================
	// Section Construction
	// ==========================================

	
	public DataSetConfigurationEditorDialog(
		final DataLoaderToolSession session) {
		
		super(session.getUserInterfaceFactory());
		init(session, null);
	}
	
	
	public DataSetConfigurationEditorDialog(
		final DataLoaderToolSession session,
		final RIFSchemaArea rifSchemaArea) {
		
		super(session.getUserInterfaceFactory());
		init(session, rifSchemaArea);
	}
	
	private void init(
		final DataLoaderToolSession session,
		final RIFSchemaArea rifSchemaArea) {
		
		this.session = session;
		previouslySelectedIndex = -1;
		isRollbackSelectionHappening = false;		
		originalDataSetConfiguration
			= DataSetConfiguration.newInstance();
		this.rifSchemaArea = rifSchemaArea;
		String dialogTitle = "";
		if (rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA) {
			dialogTitle
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfigurationEditorDialog.denominatorTitle");			
		}
		else if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
			dialogTitle
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfigurationEditorDialog.numeratorTitle");			
		}
		else if (rifSchemaArea == RIFSchemaArea.COVARIATE_DATA) {
			dialogTitle
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfigurationEditorDialog.covariateTitle");			
		}
		else {
			dialogTitle
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfigurationEditorDialog.otherTitle");			
		}
		setDialogTitle(dialogTitle);
			
		String instructionsText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfigurationEditorDialog.instructions");
		setInstructionText(instructionsText);
		setMainPanel(createMainPanel(rifSchemaArea));
		setSize(1200, 700);
		///dataSetPropertyEditorPanel

		buildUI();
	}

	private JPanel createMainPanel(final RIFSchemaArea rifSchemaArea) {		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		String dataSetFieldListTitleText
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfigurationEditorDialog.fieldListName.label");		
		dataSetFieldListPanel 
			= new OrderedListPanel(
				dataSetFieldListTitleText,
		    	"",
				userInterfaceFactory,
				true);	
		//very important -- ensure that the list preserves the order in which the fields are
		//made
		dataSetFieldListPanel.setAlphabeticallySortItems(false);
		dataSetFieldListPanel.setPrototypeListValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		
		dataSetFieldListPanel.addListSelectionListener(this);
		dataSetFieldListPanel.addListDataListener(this);
		
		dataSetPropertyEditorPanel
			= new DataSetPropertyEditorPanel(
				session,
				rifSchemaArea);
		dataSetPropertyEditorPanel.addActionListener(this);
		
		dataSetFieldPropertyEditorPanel
			= new DataSetFieldPropertyEditorPanel(
				getDialog(), 
				session,
				false);

		JPanel panel
			= userInterfaceFactory.createPanel();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.HORIZONTAL;
		panelGC.weightx = 1;
		panel.add(dataSetPropertyEditorPanel.getPanel(), panelGC);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;
		
		panel.add(
			createLowerPanel(), 
			panelGC);
		
		return panel;
	}
	
	private JPanel createLowerPanel() {

		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weighty = 1;

		panelGC.weightx = 0.3;
		JPanel panel
			= userInterfaceFactory.createPanel();
		currentFieldScrollPane
			= userInterfaceFactory.createScrollPane(dataSetFieldPropertyEditorPanel.getPanel());
				
		JSplitPane splitPane
			= userInterfaceFactory.createLeftRightSplitPane(
				createDataSetListArea(), 
				createFieldEditingArea());

		splitPane.setDividerLocation(0.4);
		panel.add(splitPane, panelGC);
		panel.setBorder(LineBorder.createGrayLineBorder());
		return panel;
	}
	
	
	
	private JPanel createDataSetListArea() {

		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		
		GridBagConstraints panelGC
			= userInterfaceFactory.createGridBagConstraints();
		JPanel panel
			= userInterfaceFactory.createPanel();
		panel.setMinimumSize(new Dimension(250, 400));
		
		panelGC.fill = GridBagConstraints.BOTH;
		panelGC.weightx = 1;
		panelGC.weighty = 1;
				
		JScrollPane fieldPane
			= userInterfaceFactory.createScrollPane(
				dataSetFieldListPanel.getPanel());		
		panel.add(
			fieldPane, 
			panelGC);
		
		dataSetFieldListButtonPanel
			= new ListEditingButtonPanel(userInterfaceFactory);
		dataSetFieldListButtonPanel.includeAddButton(null);
		dataSetFieldListButtonPanel.includeCopyButton(null);
		dataSetFieldListButtonPanel.includeDeleteButton(null);
		dataSetFieldListButtonPanel.rightJustifyButtons();
		dataSetFieldListButtonPanel.addActionListener(this);
		
		panelGC.gridy++;
		panelGC.fill = GridBagConstraints.NONE;
		panelGC.weightx = 0;
		panelGC.weighty = 0;
		panel.add(dataSetFieldListButtonPanel.getPanel(), panelGC);
		
		return panel;
	}
	
	private JPanel createFieldEditingArea() {

		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		JPanel panel
			= userInterfaceFactory.createBorderLayoutPanel();
		panel.setMinimumSize(new Dimension(750, 400));

		panel.add(dataSetFieldPropertyEditorPanel.getPanel(), BorderLayout.NORTH);
		
		return panel;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	public void setData(
		final DataSetConfiguration originalDataSetConfiguration) {
		
		this.originalDataSetConfiguration = originalDataSetConfiguration;
		
		workingCopyDataSetConfiguration
			= DataSetConfiguration.createCopy(originalDataSetConfiguration);
		buildUI();
		populateFormFromWorkingCopy(workingCopyDataSetConfiguration);
	}
	
	private void populateFormFromWorkingCopy(
		final DataSetConfiguration dataSetConfiguration) {
		
		dataSetPropertyEditorPanel.populateForm(dataSetConfiguration);
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		
		dataSetFieldListPanel.removeListSelectionListener(this);
		dataSetFieldListPanel.clearList();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			dataSetFieldListPanel.addListItem(fieldConfiguration);					
		}
		
		if (fieldConfigurations.isEmpty() == false) {
			dataSetFieldListPanel.setSelectedItem(0);
			
			DataSetFieldConfiguration selectedDataSetFieldConfiguration
				= (DataSetFieldConfiguration) dataSetFieldListPanel.getSelectedItem();
			
			Geography currentlySelectedGeography
				= dataSetPropertyEditorPanel.getSelectedGeography();
			dataSetFieldPropertyEditorPanel.setData(
				dataSetConfiguration,
				currentlySelectedGeography,
				selectedDataSetFieldConfiguration);
		}

		dataSetFieldListPanel.addListSelectionListener(this);
	}
	
	public DataSetConfiguration getDataSetConfigurationFromForm() {

		DataSetConfiguration dataSetConfigurationFromForm
			= DataSetConfiguration.createCopy(originalDataSetConfiguration);
		
		dataSetPropertyEditorPanel.populateDataSetConfigurationFromForm(dataSetConfigurationFromForm);
		
		//the fields are already accounted for 
		dataSetConfigurationFromForm.clearFieldConfigurations();
		ArrayList<DisplayableListItemInterface> currentFields
			= dataSetFieldListPanel.getListItems();
		for (DisplayableListItemInterface currentField : currentFields) {
			dataSetConfigurationFromForm.addFieldConfiguration((DataSetFieldConfiguration) currentField);
		}		
		
		return dataSetConfigurationFromForm;
	}
		
	public boolean saveChanges() {
		//first, commit changes to whatever current field is being displayed
		dataSetFieldPropertyEditorPanel.saveChanges();
			
		DataSetConfiguration dataSetConfigurationFromForm
			= getDataSetConfigurationFromForm();
		boolean changesMade
			= !originalDataSetConfiguration.hasIdenticalContents(dataSetConfigurationFromForm);
			
		DataSetConfiguration.copyInto(
			dataSetConfigurationFromForm, 
			originalDataSetConfiguration);
		
		return changesMade;
	}
	
	private void addDataSetFieldConfiguration() {
		String coreDataSetName
			= workingCopyDataSetConfiguration.getName();
		DataSetFieldConfiguration dataSetFieldConfiguration
			= DataSetFieldConfiguration.newInstance();
		dataSetFieldConfiguration.setCoreDataSetName(coreDataSetName);
		String coreFieldName 
			= DataSetFieldNameGenerator.generateCoreFieldName(workingCopyDataSetConfiguration);
		String loadFieldName 
			= DataSetFieldNameGenerator.generateLoadFieldName(workingCopyDataSetConfiguration);
		String cleanFieldName 
			= DataSetFieldNameGenerator.generateCleanFieldName(workingCopyDataSetConfiguration);
		dataSetFieldConfiguration.setCoreFieldName(coreFieldName);
		dataSetFieldConfiguration.setLoadFieldName(loadFieldName);
		dataSetFieldConfiguration.setCleanFieldName(cleanFieldName);
				
		dataSetFieldListPanel.addListItem(dataSetFieldConfiguration);
		dataSetFieldListPanel.updateUI();
		dataSetFieldListPanel.selectFirstItem();
		//dataSetFieldListPanel.setSelectedItem(dataSetFieldConfiguration);
		updateButtonStates();
		
	}
	
	private void copyDataSetFieldConfiguration() {
		DataSetFieldConfiguration selectedDataSetFieldConfiguration
			= (DataSetFieldConfiguration) dataSetFieldListPanel.getSelectedItem();		
		
		String dialogTitle
			= RIFDataLoaderToolMessages.getMessage("dataSetConfigurationEditorDialog.copyDataSetFieldConfiguration.title");
		ArrayList<String> existingListItemNames
			= dataSetFieldListPanel.getDisplayNames();

		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();	
		
		NamedListItemDialog dialog
			= new NamedListItemDialog(
				userInterfaceFactory,
				dialogTitle,
				existingListItemNames);
		
		dialog.show();
		if (dialog.isCancelled()) {
			return;
		}
		
		String selectedName = dialog.getCandidateName();
		DataSetFieldConfiguration copiedDataSetFieldConfiguration
			= DataSetFieldConfiguration.createCopy(selectedDataSetFieldConfiguration);
		copiedDataSetFieldConfiguration.setLoadFieldName(selectedName);
		dataSetFieldListPanel.addListItem(copiedDataSetFieldConfiguration);
		dataSetFieldListPanel.setSelectedItem(copiedDataSetFieldConfiguration);
		updateButtonStates();
		dataSetFieldListPanel.updateUI();		
	}
	
	private void deleteDataSetFieldConfiguration() {
		dataSetFieldListPanel.removeListSelectionListener(this);
		dataSetFieldListPanel.deleteSelectedListItems();
		dataSetFieldListPanel.addListSelectionListener(this);
		dataSetFieldListPanel.selectFirstItem();
		updateButtonStates();
	}
	
	public DataSetConfiguration getDataSetConfiguration() {		
		return workingCopyDataSetConfiguration;
	}
	
	
	public void resetForm() {
		
	}
	
	private void updateButtonStates() {
		if (dataSetFieldListPanel.isEmpty()) {
			dataSetFieldListButtonPanel.indicateEmptyState();
		}
		else {
			dataSetFieldListButtonPanel.indicatePopulatedState();			
		}		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (dataSetPropertyEditorPanel.isSchemaAreaComboBox(source)) {
			RIFSchemaArea rifSchemaArea
				= dataSetPropertyEditorPanel.getSelectedRIFSchemaArea();
			dataSetFieldPropertyEditorPanel.setCurrentRIFSchemaArea(rifSchemaArea);
		}
		else if (dataSetPropertyEditorPanel.isGeographyComboBox(source)) {
			Geography selectedGeography
				= dataSetPropertyEditorPanel.getSelectedGeography();
			dataSetFieldPropertyEditorPanel.setCurrentGeography(selectedGeography);
		}		
		else if (dataSetFieldListButtonPanel.isAddButton(source)) {
			addDataSetFieldConfiguration();
		}
		else if (dataSetFieldListButtonPanel.isCopyButton(source)) {
			copyDataSetFieldConfiguration();			
		}
		else if (dataSetFieldListButtonPanel.isDeleteButton(source)) {
			deleteDataSetFieldConfiguration();			
		}
		else {
			performOKCloseActions(event);
		}
	}
	
	public void valueChanged(ListSelectionEvent event) {
		
		if (event.getValueIsAdjusting()) {
			return;
		}
		
		if (isRollbackSelectionHappening) {
			isRollbackSelectionHappening = false;
			return;
		}
		
		//validate the currently displayed field
		try {
			if (previouslySelectedIndex != -1) {
				dataSetFieldPropertyEditorPanel.validateForm();
				dataSetFieldPropertyEditorPanel.saveChanges();
			}
			DataSetFieldConfiguration selectedFieldConfiguration
				= (DataSetFieldConfiguration) dataSetFieldListPanel.getSelectedItem();

			Geography selectedGeography
				= dataSetPropertyEditorPanel.getSelectedGeography();
			dataSetFieldPropertyEditorPanel.setData(
				workingCopyDataSetConfiguration,
				selectedGeography,
				selectedFieldConfiguration);	
			previouslySelectedIndex = event.getLastIndex();
						
			JScrollBar verticalScrollBar
				= currentFieldScrollPane.getVerticalScrollBar();
			verticalScrollBar.setValue(0);	
		}
		catch(RIFServiceException rifServiceException) {
			//We need to go back to the previously selected item
			ErrorDialog.showError(
				getDialog(), 
				rifServiceException.getErrorMessages());
			isRollbackSelectionHappening = true;
			dataSetFieldListPanel.setSelectedItem(previouslySelectedIndex);			
		}
	}
	
	//Interface: List Data Listener
	public void contentsChanged(final ListDataEvent event) {
		updateButtonStates();
	}
	
	public void intervalAdded(final ListDataEvent event) {
		updateButtonStates();		
	}
	
	public void intervalRemoved(final ListDataEvent event) {
		updateButtonStates();		
	}

	
	// ==========================================
	// Section Override
	// ==========================================

	@Override
	public void okAction() 
		throws RIFServiceException {
		
		dataSetFieldPropertyEditorPanel.validateForm();
		dataSetFieldPropertyEditorPanel.saveChanges();
		DataSetConfiguration dataSetConfigurationFromForm
			= getDataSetConfigurationFromForm();		
		//dataSetConfigurationFromForm.checkErrors();
		
		DataSetValidator dataSetValidator = new DataSetValidator();
		RIFSchemaArea rifSchemaArea 
			= dataSetConfigurationFromForm.getRIFSchemaArea();
		if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
			dataSetValidator.validateNumerator(dataSetConfigurationFromForm);
		}
		else if (rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA) {
			dataSetValidator.validateDenominator(dataSetConfigurationFromForm);
		}
		else if (rifSchemaArea == RIFSchemaArea.COVARIATE_DATA) {
			dataSetValidator.validateCovariate(dataSetConfigurationFromForm);
		}		
	}
	
}


