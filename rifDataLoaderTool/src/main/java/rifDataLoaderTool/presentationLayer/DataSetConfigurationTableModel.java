package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolSession;


import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataLoaderServiceAPI;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;

import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;


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

public final class DataSetConfigurationTableModel 
	extends AbstractTableModel {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int CREATION_DATE_COLUMN = 0;
	private static final int TABLE_NAME_COLUMN = 1;
	private static final int TABLE_DESCRIPTION_COLUMN = 2;
	private static final int LAST_ACTIVITY_STEP_COMPLETED_COLUMN = 3;
	
	
	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDataLoaderToolSession session;

	private ArrayList<DataSetConfiguration> dataSetConfigurations;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetConfigurationTableModel(
		final RIFDataLoaderToolSession session) {

		this.session = session;
		
		dataSetConfigurations = new ArrayList<DataSetConfiguration>();
		
		
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String getColumnName(int columnIndex) {

		String result = null;
		if (columnIndex == CREATION_DATE_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.creationDate.label");
		}
		else if (columnIndex == TABLE_NAME_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.name.label");
		}
		else if (columnIndex == TABLE_DESCRIPTION_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.description.label");
		}
		else if (columnIndex == LAST_ACTIVITY_STEP_COMPLETED_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.state.label");
		}
		else {
			//this should never happen
			assert(false);
		}

		return result;
	}
	
	/**
	 * make table ineditable by end-users
	 */
	public boolean isCellEditable(
		final int row,
		final int column) {
		
		return false;
	}
	
	public int getRowCount() {
		return dataSetConfigurations.size();
	}

	public int getColumnCount() {
		return 4;
	}
	
	public boolean isEmpty() {
		if (dataSetConfigurations.size() == 0) {
			return true;
		}
		
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		
		DataSetConfiguration dataSetConfiguration
			= dataSetConfigurations.get(rowIndex);

		String result = null;
		
		//@TODO
		/*
		if (columnIndex == CREATION_DATE_COLUMN) {
			result = dataSetConfiguration.getCreationDatePhrase();
		}
		else if (columnIndex == TABLE_NAME_COLUMN) {
			result = dataSetConfiguration.getName();
		}
		else if (columnIndex == TABLE_DESCRIPTION_COLUMN) {
			result = dataSetConfiguration.getDescription();
		}
		else if (columnIndex == LAST_ACTIVITY_STEP_COMPLETED_COLUMN) {
			result = dataSetConfiguration.getCurrentWorkflowState().getStateName();
		}
		else {
			//this should never happen
			assert(false);
		}
		*/
		return result;
	}

	public void updateDataSetConfigurations() 
		throws RIFServiceException {
		
		DataLoaderServiceAPI service
			= session.getService();
		User currentUser
			= session.getUser();
		
		//@TODO
		//dataSetConfigurations
		//	= service.getDataSetConfigurations(currentUser);
		fireTableDataChanged();
	}
	
	public void updateDataSetConfigurations(
		final String searchPhrase) 
		throws RIFServiceException {
		
		DataLoaderServiceAPI service
			= session.getService();
		User currentUser
			= session.getUser();
		
		//TODO
		//dataSetConfigurations
		//	= service.getDataSetConfigurations(
		//		currentUser, 
		//		searchPhrase);		
		fireTableDataChanged();
	}
	
	public DataSetConfiguration getRow(
		final int row) {
		
		return dataSetConfigurations.get(row);
	}
	
	public void deleteRows(
		final int[] selectedRows) throws RIFServiceException {
		
		//rework this later so that it read
		ArrayList<DataSetConfiguration> dataSetConfigurationsToDelete
			= new ArrayList<DataSetConfiguration>();
		for (int i = 0; i < selectedRows.length; i++) {
			dataSetConfigurationsToDelete.add(dataSetConfigurations.get(selectedRows[i]));
		}
		
		//now remove items from list
		DataLoaderServiceAPI service
			= session.getService();
		User currentUser
			= session.getUser();
	
		/*
		 * @TODO 
		 */
		/*
		service.deleteDataSetConfigurations(
			currentUser, 
			dataSetConfigurationsToDelete);

		dataSetConfigurations
			= service.getDataSetConfigurations(currentUser);
		*/
		
		fireTableDataChanged();				
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


