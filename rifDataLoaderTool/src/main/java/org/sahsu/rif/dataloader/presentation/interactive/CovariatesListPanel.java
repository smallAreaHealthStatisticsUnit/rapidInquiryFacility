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

public class CovariatesListPanel 
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

	public CovariatesListPanel(
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
				"rifDataLoaderToolApplication2.covariates.label");
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
		ArrayList<DataSetConfiguration> covariates
			= dataLoaderToolConfiguration.getCovariateDataSetConfigurations();
		ArrayList<DisplayableListItemInterface> listItemsToAdd
			= new ArrayList<DisplayableListItemInterface>();
		for (DataSetConfiguration covariate : covariates) {
			listItemsToAdd.add(covariate);
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
	
		DataSetConfiguration originalCovariate
			= csvFileSelectionDialog.getDataSetConfiguration();
		originalCovariate.setRIFSchemaArea(RIFSchemaArea.COVARIATE_DATA);

		/**
		 * Use the kind of RIF Schema Area supported by this list panel
		 * class to parameterise an editor that will let users set 
		 * properties of the newly created data set configuration.
		 */
		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(
				session, 
				RIFSchemaArea.COVARIATE_DATA);
		dialog.setData(originalCovariate);
		dialog.show();
		if (dialog.isCancelled() == true) {
			return;
		}

		/**
		 * Register new items both in the underlying model being 
		 * managed by the change manager and by the GUI list that will
		 * accommodate new additions.
		 */
		DataSetConfiguration revisedCovariate
			= dialog.getDataSetConfigurationFromForm();
		DataLoaderToolChangeManager changeManager
			= getChangeManager();
		changeManager.addCovariate(revisedCovariate);
		addListItem(revisedCovariate);
	}
	
	@Override
	protected void editSelectedListItem() {
		DataSetConfiguration originalCovariate 
			= (DataSetConfiguration) getSelectedListItem();		
		
		DataLoaderToolSession session = getSession();
		DataSetConfigurationEditorDialog dialog
			= new DataSetConfigurationEditorDialog(
				session, 
				RIFSchemaArea.COVARIATE_DATA);
		dialog.setData(originalCovariate);
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
		DataSetConfiguration revisedCovariate
			= dialog.getDataSetConfigurationFromForm();
		DataLoaderToolChangeManager changeManager
			= getChangeManager();
		updateListItem(originalCovariate, revisedCovariate);
		changeManager.updateCovariate(
			originalCovariate, 
			revisedCovariate);		
	}
	
	protected void deleteSelectedListItems() {
		
		System.out.println("CovariatesListPanel deleteSelectedListItems 1");
		
		DLDependencyManager dependencyManager
			= getDependencyManager();
		ArrayList<DisplayableListItemInterface> itemsToDelete
			= getSelectedListItems();
			
		//Remove any dependencies that either a Geography or a 
		//Health Theme may have on one of the covariates
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
		ArrayList<DataSetConfiguration> covariatesToDelete
			= new ArrayList<DataSetConfiguration>();
		for (DisplayableListItemInterface itemToDelete : itemsToDelete) {
			DataSetConfiguration dataSetConfigurationToDelete
				= (DataSetConfiguration) itemToDelete;
			covariatesToDelete.add(dataSetConfigurationToDelete);
		}
			
		changeManager.deleteCovariates(covariatesToDelete);	
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


