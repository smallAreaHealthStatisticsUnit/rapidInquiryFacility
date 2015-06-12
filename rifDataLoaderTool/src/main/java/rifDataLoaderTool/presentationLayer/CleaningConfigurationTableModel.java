package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.businessConceptLayer.CleanWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.CleanWorkflowFieldConfiguration;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

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

public final class CleaningConfigurationTableModel 
	extends AbstractTableModel {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int ORIGINAL_FIELD_NAME_COLUMN = 0;
	private static final int CLEANED_FIELD_NAME_COLUMN = 1;
	private static final int CASTED_DATA_TYPE_COLUMN = 2;
	private static final int HAS_CLEANING_RULES_COLUMN = 3;

	// ==========================================
	// Section Properties
	// ==========================================
	
	private CleanWorkflowConfiguration tableCleaningConfiguration;
	// ==========================================
	// Section Construction
	// ==========================================

	public CleaningConfigurationTableModel() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public int getRowCount() {
		return tableCleaningConfiguration.getFieldCount();
	}

	public int getColumnCount() {
		return 4;
	}

	public CleanWorkflowFieldConfiguration getRow(
		final int rowIndex) {
		
		return tableCleaningConfiguration.getFieldCleaningConfiguration(rowIndex);
	}
	
	public String getColumnName(int columnIndex) {
		String result = "";
		if (columnIndex == ORIGINAL_FIELD_NAME_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("cleaningConfigurationTableModel.originalTableFieldName.label");
		}
		else if (columnIndex == CLEANED_FIELD_NAME_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("cleaningConfigurationTableModel.cleanedTableFieldName.label");			
		}
		else if (columnIndex == CASTED_DATA_TYPE_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("cleaningConfigurationTableModel.dataType.label");			
		}
		else if (columnIndex == HAS_CLEANING_RULES_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("cleaningConfigurationTableModel.hasCleaningRules.label");			
		}
		else {
			assert(false);
		}
	
		return result;
	}
	
	/*
	 * make table non-editable by the end-user
	 */
	public boolean isCellEditable(
		final int rowIndex, 
		final int columnIndex) {
		
		return false;
	}
	
	public Object getValueAt(
		final int rowIndex, 
		final int columnIndex) {
		
		Object result = null;

		CleanWorkflowFieldConfiguration tableFieldCleaningConfiguration
			= tableCleaningConfiguration.getFieldCleaningConfiguration(rowIndex);
		
		if (columnIndex == ORIGINAL_FIELD_NAME_COLUMN) {
			result = tableFieldCleaningConfiguration.getLoadTableFieldName();
		}
		else if (columnIndex == CLEANED_FIELD_NAME_COLUMN) {
			result = tableFieldCleaningConfiguration.getCleanedTableFieldName();
		}
		else if (columnIndex == CASTED_DATA_TYPE_COLUMN) {
			RIFDataTypeInterface rifDataType
				= tableFieldCleaningConfiguration.getRifDataType();
			result = rifDataType.getName();
		}
		else if (columnIndex == HAS_CLEANING_RULES_COLUMN) {
			if (tableFieldCleaningConfiguration.hasCleaningRules()) {
				result = RIFDataLoaderToolMessages.getMessage("cleaningConfigurationTableModel.yes.label");
			}
			else {
				result = RIFDataLoaderToolMessages.getMessage("cleaningConfigurationTableModel.no.label");				
			}
		}
		else {
			assert(false);
		}
		
		return result;
	}

	public void setData(final CleanWorkflowConfiguration tableCleaningConfiguration) {
		this.tableCleaningConfiguration = tableCleaningConfiguration;
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


