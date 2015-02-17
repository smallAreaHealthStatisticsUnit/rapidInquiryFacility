
package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFJobSubmissionToolMessages;

import rifServices.businessConceptLayer.HealthCode;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.AbstractTableModel;


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


final class HealthCodeTableModel 
	extends AbstractTableModel {

// ==========================================
// Section Constants
// ==========================================
	private static final long serialVersionUID = 78630418485126505L;

	/** The Constant SOURCE. */
	public static final int SOURCE = 0;
	/** The Constant CODE. */
	public static final int CODE = 1;	
	/** The Constant DESCRIPTION. */
	public static final int DESCRIPTION = 2;
	/** The Constant DESCRIPTION_TRUNCATION_LENGTH. */
	private static final int DESCRIPTION_TRUNCATION_LENGTH=40;
		
// ==========================================
// Section Properties
// ==========================================
	
	//Data
	/** The health codes for investigation. */
	private ArrayList<HealthCode> healthCodesForInvestigation;   
    /** The health code from name. */
    private HashMap<String, HealthCode> healthCodeFromName;
	
    //GUI Components
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new health code table model.
     */
    public HealthCodeTableModel() {
    	
		healthCodesForInvestigation = new ArrayList<HealthCode>();
		healthCodeFromName = new HashMap<String, HealthCode>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    
    /**
     * Gets the health codes.
     *
     * @return the health codes
     */
    public ArrayList<HealthCode> getHealthCodes() {
    	
    	ArrayList<HealthCode> results = new ArrayList<HealthCode>();
    	results.addAll(healthCodesForInvestigation);
    	return results;
    }
    
	/**
	 * Gets the selected health code.
	 *
	 * @param row the row
	 * @return the selected health code
	 */
	public HealthCode getSelectedHealthCode(
		int row) {

		return healthCodesForInvestigation.get(row);
	}
	
	/**
	 * Clear list.
	 */
	public void clearList() {
		
		healthCodesForInvestigation.clear();
		healthCodeFromName.clear();
	}
	
	/**
	 * Delete health codes.
	 *
	 * @param rowsToDelete the rows to delete
	 */
	public void deleteHealthCodes(
		int[] rowsToDelete) {

		ArrayList<HealthCode> healthCodesToDelete 
			= new ArrayList<HealthCode>();
		
		for (int i = 0; i < rowsToDelete.length; i++) {
			HealthCode healthCodeToDelete
				= getSelectedHealthCode(rowsToDelete[i]);
			healthCodesToDelete.add(healthCodeToDelete);
		}	

		for (HealthCode healthCodeToDelete : healthCodesToDelete) {
			healthCodesForInvestigation.remove(healthCodeToDelete);
			healthCodeFromName.remove(healthCodeToDelete.getDisplayName());			
		}
		
		fireTableDataChanged();
	}
	
	/**
	 * Adds the health codes.
	 *
	 * @param healthCodesToAdd the health codes to add
	 */
	public void addHealthCodes(
		ArrayList<HealthCode> healthCodesToAdd) {		

		for (HealthCode healthCodeToAdd : healthCodesToAdd) {
			String displayName
				= healthCodeToAdd.getDisplayName();
			if (healthCodeFromName.containsKey(displayName) == false) {
				healthCodesForInvestigation.add(healthCodeToAdd);
				healthCodeFromName.put(displayName, healthCodeToAdd);
			}			
		}
		fireTableDataChanged();
	}
	
    /**
     * Sets the health codes.
     *
     * @param _healthCodesForInvestigation the new health codes
     */
    public void setHealthCodes(
    	ArrayList<HealthCode> _healthCodesForInvestigation) {
   
    	clearList();
    	addHealthCodes(_healthCodesForInvestigation);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		
		return healthCodesForInvestigation.size();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		
		return 3;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(
		int row, 
		int column) {
		
		HealthCode healthCode = healthCodesForInvestigation.get(row);
		
		String value = "";
		
		if (column == CODE) {
			value = healthCode.getCode();	
		}
		else if (column == DESCRIPTION) {
			value = truncateFieldValue(healthCode.getDescription());
		}
		else if (column == SOURCE) {
			value = truncateFieldValue(healthCode.getNameSpace());
		}		
		else {
				assert(false);
		}					

		return value;
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

	//AbstractTableModel

	/**
	 * Truncate field value.
	 *
	 * @param fieldValue the field value
	 * @return the string
	 */
	private String truncateFieldValue(
		String fieldValue) {

		StringBuilder value = new StringBuilder();
		if (fieldValue.length() <= DESCRIPTION_TRUNCATION_LENGTH) {
			value.append(fieldValue);
		}
		else {
			value.append(fieldValue.substring(0,DESCRIPTION_TRUNCATION_LENGTH));
			value.append("...");			
		}
		return value.toString();
	}
	

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(
		int column) {

		String columnName = "";
		
		if (column == CODE) {
			columnName = RIFJobSubmissionToolMessages.getMessage("healthCodeListPanel.code.label");
			return columnName;
		}
		else if (column == DESCRIPTION) {
			columnName = RIFJobSubmissionToolMessages.getMessage("healthCodeListPanel.description.label");
		}
		else if (column == SOURCE) {
			columnName = RIFJobSubmissionToolMessages.getMessage("healthCodeListPanel.source.label");
		}
		else {
			assert(false);
		}
		
		return columnName;		
	}

}
