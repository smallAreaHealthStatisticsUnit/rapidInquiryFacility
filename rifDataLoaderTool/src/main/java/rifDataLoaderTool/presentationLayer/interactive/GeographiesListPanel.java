package rifDataLoaderTool.presentationLayer.interactive;

import rifDataLoaderTool.businessConceptLayer.DLGeography;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.DataLoaderToolSession;

import rifGenericLibrary.presentationLayer.UserInterfaceFactory;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;
import rifDataLoaderTool.businessConceptLayer.LoadingOrderState;
import rifDataLoaderTool.businessConceptLayer.DLGeographyMetaData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Observable;

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

public class GeographiesListPanel 
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

	public GeographiesListPanel(
		final JFrame frame,
		final DataLoaderToolSession session,
		final DLDependencyManager dependencyManager) {
		
		super(
			frame, 
			session, 
			dependencyManager);
		
		String listTitle
			= RIFDataLoaderToolMessages.getMessage(
				"rifDataLoaderToolApplication2.geographies.label");
		setListTitle(listTitle);
		
		buildUI();
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	

	@Override
	protected void addListItem() {
		
		System.out.println("Adding geography");

		DLGeographyMetaData geographyMetaData
			= new DLGeographyMetaData();	
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		GeographyEditingDialog dialog
			= new GeographyEditingDialog(userInterfaceFactory);
		dialog.setData(geographyMetaData);
		dialog.show();		
	}
	
	@Override
	protected void editSelectedListItem() {
		DLGeographyMetaData geographyMetaData 
			= (DLGeographyMetaData) getSelectedListItem();
		UserInterfaceFactory userInterfaceFactory
			= getUserInterfaceFactory();
		GeographyEditingDialog dialog
			= new GeographyEditingDialog(userInterfaceFactory);
		dialog.setData(geographyMetaData);
		dialog.show();
		
	}
	
	@Override
	protected void checkDependenciesForItemsToDelete()
		throws RIFServiceException {
		
		DLDependencyManager dependencyManager
			= getDependencyManager();
		ArrayList<DisplayableListItemInterface> itemsToDelete
			= getSelectedListItems();
		for (DisplayableListItemInterface itemToDelete : itemsToDelete) {
			DLGeography geographyToDelete
				= (DLGeography) itemToDelete;
			dependencyManager.checkGeographyDependencies(geographyToDelete);
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
		
		LoadingOrderState currentState
			= (LoadingOrderState) object;
		if (currentState.getOrder() >= LoadingOrderState.DEFINE_HEALTH_THEMES.getOrder()) {
			setEnable(true);			
		}
		else {
			setEnable(false);
		}
	}
}


