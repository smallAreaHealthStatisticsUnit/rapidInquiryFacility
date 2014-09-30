package rifDataLoaderTool.presentationLayer;


import rifDataLoaderTool.businessConceptLayer.DBTConfigurationRecord;


import rifDataLoaderTool.system.RIFDataLoaderActivityStep;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

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

public class DBTMetaDataTableModel extends AbstractTableModel {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int TABLE_NAME_COLUMN = 0;
	private static final int TABLE_DESCRIPTION_COLUMN = 1;
	private static final int LAST_MODIFIED_DATE_COLUMN = 2;
	private static final int ACTIVITY_STEP_COLUMN = 3;
	
	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<DBTConfigurationRecord> tableMetaDataRows;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DBTMetaDataTableModel() {

		tableMetaDataRows = new ArrayList<DBTConfigurationRecord>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setDBTMetaDataRecords(
		final ArrayList<DBTConfigurationRecord> dbtMetaDataTableRecords) {
		
		tableMetaDataRows = dbtMetaDataTableRecords;
	}
	
	public int getRowCount() {
		return tableMetaDataRows.size();
	}
	
	public int getColumnCount() {
		return 4;
	}
	
	public Object getValueAt(
		final int row,
		final int column) {
		
		DBTConfigurationRecord tableMetaDataRecord
			= tableMetaDataRows.get(row);
		
		if (column == TABLE_NAME_COLUMN) {
			return tableMetaDataRecord.getTableName();
		}
		else if (column == TABLE_DESCRIPTION_COLUMN) {
			return tableMetaDataRecord.getTableDescription();
		}
		else if (column == LAST_MODIFIED_DATE_COLUMN) {
			return tableMetaDataRecord.getLastModifiedDatePhrase();
		}
		else if (column == ACTIVITY_STEP_COLUMN) {
			
			RIFDataLoaderActivityStep rifDataLoaderActivityStep
				= tableMetaDataRecord.getRIFDataLoaderActivityStep();
			return rifDataLoaderActivityStep.getCompletedStatusMessage();
		}
		else {
			//must be Activity Step
			assert false;
			return null;
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

	@Override
	/**
	 * make table non-editable
	 */
	public boolean isCellEditable(
		final int row,
		final int column) {
		
		return false;
	}
	
}
