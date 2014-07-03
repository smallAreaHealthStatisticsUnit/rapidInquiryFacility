package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.util.UserInterfaceFactory;

import rifServices.businessConceptLayer.CalculationMethod;

import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;


/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 * 
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
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

class CalculationMethodTable 
	extends JTable {

// ==========================================
// Section Constants
// ==========================================
	private static final long serialVersionUID = 5562467314672057209L;

// ==========================================
// Section Properties
// ==========================================
	
	//Data
	
	//GUI Components	
	/** The calculation method table model. */
	private CalculationMethodTableModel calculationMethodTableModel;	
	/** The list selection model. */
	private ListSelectionModel listSelectionModel;
	
// ==========================================
// Section Construction
// ==========================================
    /**
 	 Instantiates a new calculation method table.
     *
     * @param userInterfaceFactory the user interface factory
     */
	public CalculationMethodTable(
		UserInterfaceFactory userInterfaceFactory) {

		calculationMethodTableModel = new CalculationMethodTableModel();
		setModel(calculationMethodTableModel);
		listSelectionModel = createDefaultSelectionModel();
		listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================

    /**
     * Use multiple interval selection.
     */
	public void useMultipleIntervalSelection() {
		
		listSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);    	
    }
    
    /**
     * Use single item selection.
     */
    public void useSingleItemSelection() {
    	
		listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
	/**
	 * Sets the calculation methods.
	 *
	 * @param calculationMethods the new calculation methods
	 */
	public void setCalculationMethods(
		ArrayList<CalculationMethod> calculationMethods) {
		
		calculationMethodTableModel.setCalculationMethods(calculationMethods);
	}
	
	/**
	 * Gets the selected calculation method.
	 *
	 * @return the selected calculation method
	 */
	public CalculationMethod getSelectedCalculationMethod() {
		
		int selectedIndex = getSelectedRow();
		if (selectedIndex == -1) {
			return null;
		}
		else {			
			CalculationMethod selectedCalculationMethod
				= calculationMethodTableModel.getRow(selectedIndex);
			return selectedCalculationMethod;
		}
	}
	
	/**
	 * Gets the selected calculation methods.
	 *
	 * @return the selected calculation methods
	 */
	public ArrayList<CalculationMethod> getSelectedCalculationMethods() {
		
		ArrayList<CalculationMethod> calculationMethods
			= new ArrayList<CalculationMethod>();
		
		int[] selectedRowNumbers = getSelectedRows();
		for (int i = 0; i < selectedRowNumbers.length; i++) {
			calculationMethods.add(calculationMethodTableModel.getRow(selectedRowNumbers[i]));
		}
		
		return calculationMethods;
	}
	
	/**
	 * Gets the calculation methods.
	 *
	 * @return the calculation methods
	 */
	public ArrayList<CalculationMethod> getCalculationMethods() {
		
		return calculationMethodTableModel.getCalculationMethods();
	}

	/**
	 * Adds the calculation method.
	 *
	 * @param calculationMethod the calculation method
	 */
	void addCalculationMethod(
		CalculationMethod calculationMethod) {
		
		calculationMethodTableModel.addCalculationMethod(calculationMethod);
	}
	
	/**
	 * Adds the calculation methods.
	 *
	 * @param calculationMethods the calculation methods
	 */
	public void addCalculationMethods(
		ArrayList<CalculationMethod> calculationMethods) {
		
		for (CalculationMethod calculationMethod : calculationMethods) {
			calculationMethodTableModel.addCalculationMethod(calculationMethod);
		}		
	}
	
	/**
	 * Update calculation method.
	 *
	 * @param calculationMethod the calculation method
	 */
	public void updateCalculationMethod(
		CalculationMethod calculationMethod) {
		
		calculationMethodTableModel.fireTableDataChanged();
		int rowIndex 
			= calculationMethodTableModel.getIndexForObject(calculationMethod);
		setRowSelectionInterval(rowIndex, rowIndex);
	}
	
	/**
	 * Delete selected selected calculation methods.
	 */
	public void deleteSelectedSelectedCalculationMethods() {
		
		ArrayList<CalculationMethod> calculationMethodsToDelete
			= new ArrayList<CalculationMethod>();
		
		int[] selectedRowNumbers = getSelectedRows();
		for (int i = 0; i < selectedRowNumbers.length; i++) {
			CalculationMethod selectedCalculationMethod
				= calculationMethodTableModel.getRow(selectedRowNumbers[i]);
			calculationMethodsToDelete.add(selectedCalculationMethod);
		}
		
		calculationMethodTableModel.deleteCalculationMethods(calculationMethodsToDelete);
		
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
