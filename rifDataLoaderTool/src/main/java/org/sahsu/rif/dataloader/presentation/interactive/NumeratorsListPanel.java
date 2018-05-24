package org.sahsu.rif.dataloader.presentation.interactive;

import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JFrame;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;

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

public class NumeratorsListPanel 
	extends AbstractDataLoadingThemePanel {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	public NumeratorsListPanel(
		final JFrame frame,
		final DataLoaderToolSession session,
		final DLDependencyManager dependencyManager,
		final DataLoaderToolChangeManager changeManager) {
		
		super(
			frame, 
			session, 
			dependencyManager,
			changeManager);
		
		String listTitle
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderToolApplication2.numerators.label");
		setListTitle(listTitle);
		
		buildUI();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void refresh() {
		clearListItems();

		DataLoaderToolSession session = getSession();
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= session.getDataLoaderToolConfiguration();
		ArrayList<DataSetConfiguration> numerators
			= dataLoaderToolConfiguration.getNumeratorDataSetConfigurations();
		ArrayList<DisplayableListItemInterface> listItemsToAdd
			= new ArrayList<DisplayableListItemInterface>();
		for (DataSetConfiguration numerator : numerators) {
			listItemsToAdd.add(numerator);
		}
		setListItems(listItemsToAdd);
	}
	
	@Override
	protected void addListItem() {

		/**
		 * Create a data set based on the properties derived from
		 * examining an imported CSV file.  Then set the RIF Schema
		 * Area in a way that is appropriate to the concept supported
		 * by this list panel class.
		 */
		DataLoaderToolSession session = getSession();
		CSVFileSelectionDialog csvFileSelectionDialog
			= new CSVFileSelectionDialog(session);
		csvFileSelectionDialog.show();
	
		if (csvFileSelectionDialog.isCancelled()) {
			//User pressed cancel, there is no new item to add
			return;
		}
	
		DataSetConfiguration originalNumerator
			= csvFileSelectionDialog.getDataSetConfiguration();
		originalNumerator.setRIFSchemaArea(RIFSchemaArea.HEALTH_NUMERATOR_DATA);

		/**
		 * Use the kind of RIF Schema Area supported by this list panel
		 * class to parameterise an editor that will let users set 
		 * properties of the newly created data set configuration.
		 */
		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(
				session, 
				RIFSchemaArea.HEALTH_NUMERATOR_DATA);
		dialog.setData(originalNumerator);
		dialog.show();
		if (dialog.isCancelled() == true) {
			return;
		}

		/**
		 * Register new items both in the underlying model being 
		 * managed by the change manager and by the GUI list that will
		 * accommodate new additions.
		 */
		DataSetConfiguration revisedNumerator
			= dialog.getDataSetConfigurationFromForm();
		DataLoaderToolChangeManager changeManager
			= getChangeManager();
		changeManager.addNumerator(revisedNumerator);
		addListItem(revisedNumerator);
	}
	
	@Override
	protected void editSelectedListItem() {
		DataSetConfiguration originalNumerator 
			= (DataSetConfiguration) getSelectedListItem();		
		
		DataLoaderToolSession session = getSession();
		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(
				session, 
				RIFSchemaArea.HEALTH_NUMERATOR_DATA);
		dialog.setData(originalNumerator);
		dialog.show();
		if (dialog.isCancelled() == true) {
			return;
		}
		
		/**
		 * Update the underlying data loader tool configuration model
		 * object managed by the change manager and update the GUI 
		 * list as well.  The change manager will compare original
		 * and revised copies and if they are different, it will 
		 * set a saveChanges field.
		 */
		DataSetConfiguration revisedNumerator
			= dialog.getDataSetConfigurationFromForm();
		DataLoaderToolChangeManager changeManager
			= getChangeManager();
		updateListItem(originalNumerator, revisedNumerator);
		changeManager.updateNumerator(
			originalNumerator, 
			revisedNumerator);		
	}
	
	protected void deleteSelectedListItems() {
		
		DLDependencyManager dependencyManager
			= getDependencyManager();
		ArrayList<DisplayableListItemInterface> itemsToDelete
			= getSelectedListItems();
			
		//Remove any dependencies that either a Geography or a Health Theme may 
		//have on one of the numerators
		for (DisplayableListItemInterface itemToDelete : itemsToDelete) {
			DataSetConfiguration dataSetConfigurationToDelete
				= (DataSetConfiguration) itemToDelete;
			dependencyManager.deregisterDependenciesOfDataSet(dataSetConfigurationToDelete);
		}
			
		//We're now ready to delete the items.  Delete them from both the 
		//data loader tool configuration model object that is being managed
		//by the change manager and the GUI list
		DataLoaderToolChangeManager changeManager
			= getChangeManager();
		ArrayList<DataSetConfiguration> numeratorsToDelete
			= new ArrayList<DataSetConfiguration>();
		for (DisplayableListItemInterface itemToDelete : itemsToDelete) {
			DataSetConfiguration dataSetConfigurationToDelete
				= (DataSetConfiguration) itemToDelete;
			numeratorsToDelete.add(dataSetConfigurationToDelete);
		}
		
		changeManager.deleteNumerators(numeratorsToDelete);
		super.deleteSelectedListItems();	
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

	//Overriding method for Observer

	public void update(
		final Observable observable,
		final Object object) {
		
		DataLoadingOrder currentState
			= (DataLoadingOrder) object;
		if (currentState.getStepNumber() >= DataLoadingOrder.DENOMINATORS_SPECIFIED.getStepNumber()) {
			setEnable(true);			
		}
		else {
			setEnable(false);
		}
	}	
	
}


