
package rifJobSubmissionTool.desktop.interactive;

import rifServices.businessConceptLayer.Parameter;
import rifServices.system.RIFServiceMessages;

import java.util.ArrayList;
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


final class ParameterTableModel 
	extends AbstractTableModel {

// ==========================================
// Section Constants
// ==========================================
	
	private static final long serialVersionUID = 5301916576450363582L;
	/** The Constant NAME_COLUMN. */
	private static final int NAME_COLUMN = 0;
	/** The Constant VALUE_COLUMN. */
	private static final int VALUE_COLUMN = 1;
	
// ==========================================
// Section Properties
// ==========================================
	/** The parameters. */
	private ArrayList<Parameter> parameters;
	
// ==========================================
// Section Construction
// ==========================================
	
    /**
     * Instantiates a new parameter table model.
     */
	public ParameterTableModel() {
		
		parameters = new ArrayList<Parameter>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public ArrayList<Parameter> getParameters() {
		
		return parameters;
	}
	
    /**
     * Sets the parameters.
     *
     * @param parameters the new parameters
     */
    public void setParameters(
    	ArrayList<Parameter> parameters) {

    	this.parameters = parameters;
		fireTableDataChanged();
	}


	public int getRowCount() {
		
		return parameters.size();
	}
	

	public int getColumnCount() {
		
		return 2;
	}
	
	/**
	 * Gets the row.
	 *
	 * @param row the row
	 * @return the row
	 */
	Parameter getRow(
		int row) {

		return parameters.get(row);
	}
	

	public Object getValueAt(
		int row, 
		int column) {

		Parameter parameter = parameters.get(row);
		
		String value = "";
		if (column == NAME_COLUMN) {
			value = parameter.getName();
		}
		else if (column == VALUE_COLUMN) {
			value = parameter.getValue();
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
	

	@Override
	public void setValueAt(
		Object revisedParameterValue, 
		int row, 
		int column) {
		
		Parameter parameter = parameters.get(row);
		parameter.setValue(String.valueOf(revisedParameterValue));			
	}

	@Override
	public String getColumnName(
		int column) {

		String columnName = "";
		
		if (column == NAME_COLUMN) {
			columnName = RIFServiceMessages.getMessage("parameter.name.label");
			return columnName;
		}
		else if (column == VALUE_COLUMN) {
			columnName = RIFServiceMessages.getMessage("parameter.value.label");
		}
		else {
			assert(false);
		}
		
		return columnName;		
	}
	

	@Override
	public boolean isCellEditable(
		int row, 
		int column) {

		if (column == NAME_COLUMN) {
			return false;
		}
		else {
			return true;
		}
	}
}
