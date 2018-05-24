package org.sahsu.rif.dataloader.presentation.interactive;

import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JFrame;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.HealthTheme;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.presentation.ErrorDialog;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
import org.sahsu.rif.generic.system.RIFServiceException;


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

public class HealthThemesListPanel 
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

	public HealthThemesListPanel(
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
				"rifDataLoaderToolApplication2.healthThemes.label");
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
		ArrayList<HealthTheme> healthThemes
			= dataLoaderToolConfiguration.getHealthThemes();
		ArrayList<DisplayableListItemInterface> listItemsToAdd
			= new ArrayList<DisplayableListItemInterface>();
		for (HealthTheme healthTheme : healthThemes) {
			listItemsToAdd.add(healthTheme);
		}
		setListItems(listItemsToAdd);
	}
	

	@Override
	protected void addListItem() {
		HealthTheme healthTheme
			= HealthTheme.newInstance();
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		HealthThemeEditorDialog dialog
			 = new HealthThemeEditorDialog(
				userInterfaceFactory,
				getExistingDisplayNames());
		dialog.setData(healthTheme);
		dialog.show();
		if (dialog.isCancelled() == false) {
			DataLoaderToolChangeManager changeManager
				= getChangeManager();
			changeManager.addHealthTheme(healthTheme);
			addListItem(healthTheme);			
		}
	}
	
	@Override
	protected void editSelectedListItem() {
		HealthTheme selectedHealthTheme 
			= (HealthTheme) getSelectedListItem();
		
		HealthTheme cloneHealthTheme
			= HealthTheme.createCopy(selectedHealthTheme);
		
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		HealthThemeEditorDialog dialog
		 	= new HealthThemeEditorDialog(
		 		userInterfaceFactory, 
		 		getExistingDisplayNames());
		dialog.setData(cloneHealthTheme);
		dialog.show();
		
		if (dialog.isCancelled() == false) {
			DataLoaderToolChangeManager changeManager
				= getChangeManager();
			//update item in the underlying model object
			changeManager.updateHealthTheme(
				selectedHealthTheme, 
				cloneHealthTheme);
			//update item in the UI
			updateListItem(
				selectedHealthTheme, 
				cloneHealthTheme);
		}
	}
	
	@Override
	protected void deleteSelectedListItems() {
		
		try {
			
			DLDependencyManager dependencyManager
				= getDependencyManager();
		
			ArrayList<DisplayableListItemInterface> itemsToDelete
				= getSelectedListItems();
			for (DisplayableListItemInterface itemToDelete : itemsToDelete) {
				HealthTheme healthThemeToDelete
					= (HealthTheme) itemToDelete;
				dependencyManager.checkHealthThemeDependencies(healthThemeToDelete);
			}
			
			super.deleteSelectedListItems();
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(
				getFrame(), 
				rifServiceException.getErrorMessages());
		}

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
		if (currentState.getStepNumber() >= DataLoadingOrder.GEOGRAPHY_META_DATA_SPECIFIED.getStepNumber()) {
			setEnable(true);			
		}
		else {
			setEnable(false);
		}
	}
}


