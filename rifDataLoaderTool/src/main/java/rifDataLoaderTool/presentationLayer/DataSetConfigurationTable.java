package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolSession;
import rifServices.system.RIFServiceException;
import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import javax.swing.*;

import java.util.ArrayList;

import javax.swing.event.ListSelectionListener;


/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class DataSetConfigurationTable {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private JTable table;
	private DataSetConfigurationTableModel dataSetConfigurationTableModel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetConfigurationTable(
		final RIFDataLoaderToolSession session) {
		
		dataSetConfigurationTableModel 
			= new DataSetConfigurationTableModel(session);
		
		UserInterfaceFactory userInterfaceFactory
			= session.getUserInterfaceFactory();
		table = userInterfaceFactory.createTable(dataSetConfigurationTableModel);
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public JTable getTable() {
		return table;
	}
	
	public void addListSelectionListener(
		final ListSelectionListener listSelectionListener) {
		
		ListSelectionModel listSelectionModel
			= table.getSelectionModel();
		listSelectionModel.addListSelectionListener(listSelectionListener);
	}
	
	public void updateDataSetConfigurations() 
		throws RIFServiceException {

		dataSetConfigurationTableModel.updateDataSetConfigurations();
		if (dataSetConfigurationTableModel.isEmpty() == false) {
			ListSelectionModel listSelectionModel
				= table.getSelectionModel();
			listSelectionModel.setLeadSelectionIndex(0);
		}		
	}
	
	public void updateDataSetConfigurations(
		final String currentSearchPhrase) 
		throws RIFServiceException {
		
		dataSetConfigurationTableModel.updateDataSetConfigurations(currentSearchPhrase);
	}

	public ArrayList<DataSetConfiguration> getSelectedDataSetConfigurations() {
		int[] selectedRows = table.getSelectedRows();
		
		ArrayList<DataSetConfiguration> selectedDataSetConfigurations
			= new ArrayList<DataSetConfiguration>();
		for (int i = 0; i < selectedRows.length; i++) {
			DataSetConfiguration dataSetConfiguration
				= dataSetConfigurationTableModel.getRow(selectedRows[i]);
			selectedDataSetConfigurations.add(dataSetConfiguration);
		}
		
		return selectedDataSetConfigurations;
		
	}

	public DataSetConfiguration getSelectedDataSetConfiguration() {
		
		if (dataSetConfigurationTableModel.isEmpty()) {
			//no rows in table so nothing could possibly be selected			
			return null;
		}
		
		ListSelectionModel listSelectionModel = table.getSelectionModel();
		int rowIndex = listSelectionModel.getLeadSelectionIndex();
		if (rowIndex == -1) {
			return null;
		}

		DataSetConfiguration dataSetConfiguration
			= dataSetConfigurationTableModel.getRow(rowIndex);
		return dataSetConfiguration;
	}
	
	public void deleteSelectedDataSetConfigurations() 
		throws RIFServiceException {
		
		if (dataSetConfigurationTableModel.isEmpty() == false) {		
			int[] selectedRows = table.getSelectedRows();

			dataSetConfigurationTableModel.deleteRows(selectedRows);
			ListSelectionModel listSelectionModel
				= table.getSelectionModel();
			listSelectionModel.setSelectionInterval(0,0);
		}
	}
	
	public boolean isEmpty() {
		return dataSetConfigurationTableModel.isEmpty();
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
