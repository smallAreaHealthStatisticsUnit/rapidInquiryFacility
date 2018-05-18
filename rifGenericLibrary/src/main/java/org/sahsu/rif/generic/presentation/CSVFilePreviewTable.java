package org.sahsu.rif.generic.presentation;

import javax.swing.*;



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

public class CSVFilePreviewTable {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private JTable table;
	private CSVFilePreviewTableModel tableModel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CSVFilePreviewTable(
		final UserInterfaceFactory userInterfaceFactory) {

		tableModel = new CSVFilePreviewTableModel(); 		
		table = userInterfaceFactory.createTable(tableModel);
		table.setFocusable(false);
		table.setRowSelectionAllowed(false);		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public JTable getTable() {		
		return table;
	}
	
	public void clearData() {
		
		setData(
			new String[0], 
			new String[0][0]);
	}
	
	public String[] getFieldNames() {
		return tableModel.getFieldNames();
	}
	
	public String[][] getPreviewData() {
		return tableModel.getPreviewData();
	}
	
	public void setData(
		final String[] fieldNames, 
		final String[][] previewData) {
		
		tableModel.setData(fieldNames, previewData);
		table.updateUI();
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


