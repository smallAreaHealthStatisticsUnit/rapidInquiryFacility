package rifDataLoaderTool.presentationLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;
import rifDataLoaderTool.businessConceptLayer.CleaningRule;

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

public class CleaningRuleTableModel extends AbstractTableModel {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final int SEARCH_VALUE_TABLE_COLUMN = 0;
	private static final int REPLACE_VALUE_TABLE_COLUMN = 1;
	
	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDataTypeInterface rifDataType;
	private ArrayList<CleaningRule> cleaningRules;
	// ==========================================
	// Section Construction
	// ==========================================

	public CleaningRuleTableModel() {
		cleaningRules = new ArrayList<CleaningRule>();
	}

	public int getRowCount() {
		return cleaningRules.size();
	}

	public int getColumnCount() {
		return 2;
	}

	public CleaningRule getRow(final int rowIndex) {
		return cleaningRules.get(rowIndex);		
	}
	
	public String getColumnName(
		final int columnIndex) {
		
		String result = "";
		if (columnIndex == SEARCH_VALUE_TABLE_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("cleaningRule.searchValue.label");
		}
		else if (columnIndex == REPLACE_VALUE_TABLE_COLUMN) {
			result = RIFDataLoaderToolMessages.getMessage("cleaningRule.replaceValue.label");
		}
		else {
			assert(false);
		}
		
		return result;		
	}
	
	public Object getValueAt(
		final int rowIndex, 
		final int columnIndex) {

		CleaningRule cleaningRule = cleaningRules.get(rowIndex);
		
		String result = "";
		if (columnIndex == SEARCH_VALUE_TABLE_COLUMN) {
			result = cleaningRule.getSearchValue();
		}
		else if (columnIndex == REPLACE_VALUE_TABLE_COLUMN) {
			result = cleaningRule.getReplaceValue();
		}
		else {
			assert(false);
		}
		
		return result;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setData(final RIFDataTypeInterface rifDataType) {
		this.rifDataType = rifDataType;
		
		cleaningRules = rifDataType.getCleaningRules();
		fireTableDataChanged();
		System.out.println("CleaningRuleTableModel setData==cleaning rules total=="+cleaningRules.size()+"==");
	}
	
	public void deleteRow(final int row) {
		cleaningRules.remove(row);
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


