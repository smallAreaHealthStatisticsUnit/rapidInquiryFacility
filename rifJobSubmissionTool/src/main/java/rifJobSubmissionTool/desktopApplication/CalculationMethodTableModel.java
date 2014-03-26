package rifJobSubmissionTool.desktopApplication;

import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.businessConceptLayer.CalculationMethodPrior;
import rifServices.businessConceptLayer.Parameter;
import rifServices.system.RIFServiceMessages;

import java.text.Collator;
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

public class CalculationMethodTableModel 
	extends AbstractTableModel {


// ==========================================
// Section Constants
// ==========================================
	private static final long serialVersionUID = -3282841892998785048L;

	/** The Constant NAME_COLUMN. */
	private static final int NAME_COLUMN = 0;	
	/** The Constant PRIOR_COLUMN. */
	private static final int PRIOR_COLUMN = 1;	
	/** The Constant PARAMETERS_COLUMN. */
	private static final int PARAMETERS_COLUMN = 2;	
	/** The Constant DESCRIPTION_TRUNCATION_LENGTH. */
	private static final int DESCRIPTION_TRUNCATION_LENGTH = 20;
	
// ==========================================
// Section Properties
// ==========================================
	
	//Data
	/** The calculation methods. */
	private ArrayList<CalculationMethod> calculationMethods;

	//GUI Components
		
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new calculation method table model.
     */
	public CalculationMethodTableModel() {
		calculationMethods = new ArrayList<CalculationMethod>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
	
    /**
     * Gets the calculation methods.
     *
     * @return the calculation methods
    */
	public ArrayList<CalculationMethod> getCalculationMethods() {
		
    	return calculationMethods;
    }
    
	/**
	 * Sets the calculation methods.
	 *
	 * @param calculationMethods the new calculation methods
	 */
	public void setCalculationMethods(
		ArrayList<CalculationMethod> calculationMethods) {
		
		this.calculationMethods = calculationMethods;
		fireTableDataChanged();
	}
	
	/**
	 * Adds the calculation method.
	 *
	 * @param calculationMethod the calculation method
	 */
	public void addCalculationMethod(
		CalculationMethod calculationMethod) {
		
		addByAscendingOrderOfName(calculationMethod);
		fireTableDataChanged();
	}
	
	/**
	 * Adds the calculation methods.
	 *
	 * @param calculationMethodsToAdd the calculation methods to add
	 */
	public void addCalculationMethods(
		ArrayList<CalculationMethod> calculationMethodsToAdd) {
		
		for (CalculationMethod calculationMethodToAdd : calculationMethodsToAdd) {
			addByAscendingOrderOfName(calculationMethodToAdd);			
		}
		fireTableDataChanged();
	}
	
	/**
	 * Delete calculation methods.
	 *
	 * @param calculationMethodsToDelete the calculation methods to delete
	 */
	public void deleteCalculationMethods(
			ArrayList<CalculationMethod> calculationMethodsToDelete) {
		
		for (CalculationMethod calculationMethodToDelete : calculationMethodsToDelete) {
			calculationMethods.remove(calculationMethodToDelete);
		}
		
		fireTableDataChanged();		
	}
	
	/**
	 * Performs a simple sort to ensure that items are displayed in order of ascending names
	 * The sorting routine is O(n2) which would be poor for large lists of plugins.
	 * But we anticipate there won't actually be that many plugins being used.
	 *
	 * @param calculationMethodToInsert the calculation method to insert
	 */
	private void addByAscendingOrderOfName(
		CalculationMethod calculationMethodToInsert) {
		
		Collator collator = Collator.getInstance();
		
		String candidateName = calculationMethodToInsert.getName();
		int numberOfMethods = calculationMethods.size();
		int i = 0;
		for (i = 0; i < numberOfMethods; i++) {
			CalculationMethod currentCalculationMethod
				= calculationMethods.get(i);
			String currentName = currentCalculationMethod.getName();
			if (collator.compare(candidateName, currentName) < 0) {
				break;
			}			
		}
		calculationMethods.add(i, calculationMethodToInsert);		
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
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		
		return calculationMethods.size();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		
		return 3;
	}
	
	/**
	 * Gets the index for object.
	 *
	 * @param targetCalculationMethod the target calculation method
	 * @return the index for object
	 */
	public int getIndexForObject(
		CalculationMethod targetCalculationMethod) {
		
		int i = 0;
		for (CalculationMethod calculationMethod : calculationMethods) {
			if (targetCalculationMethod.equals(calculationMethod) == true) {
				return i;
			}
			i++;
		}
		
		return i;
	}
	
	/**
	 * Gets the row.
	 *
	 * @param row the row
	 * @return the row
	 */
	CalculationMethod getRow(
		int row) {

		return calculationMethods.get(row);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(
		int row, 
		int column) {

		CalculationMethod calculationMethod = calculationMethods.get(row);
		
		String value = "";
		if (column == NAME_COLUMN) {
			value = calculationMethod.getName();
		}
		else if (column == PRIOR_COLUMN) {
			CalculationMethodPrior prior 
				= calculationMethod.getPrior();
			value = prior.getName();
		}
		else if (column == PARAMETERS_COLUMN) {
			//truncated study description
			StringBuilder buffer = new StringBuilder();
			ArrayList<Parameter> parameters
				= calculationMethod.getParameters();
			for (int i = 0; i < parameters.size(); i++) {
				if (i != 0) {
					buffer.append(",");
				}
				Parameter currentParameter = parameters.get(i);
				buffer.append(currentParameter.getName());	
				buffer.append("=");
				buffer.append(currentParameter.getValue());				
			}

			String longDescription = buffer.toString();
			StringBuilder truncatedDescription = new StringBuilder();
			if (longDescription.length() > DESCRIPTION_TRUNCATION_LENGTH) {
				truncatedDescription.append(longDescription.substring(0,DESCRIPTION_TRUNCATION_LENGTH));				
				truncatedDescription.append("...");
				value = truncatedDescription.toString();
			}
			else {
				value = longDescription;
			}
		}		
		else {
			assert(false);
		}
		return value;
	}

	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(
		int column) {
		
		String columnName = "";
		
		if (column == NAME_COLUMN) {
			columnName = RIFServiceMessages.getMessage("calculationMethod.name.label");
			return columnName;
		}
		else if (column == PRIOR_COLUMN) {
			columnName = RIFServiceMessages.getMessage("calculationMethod.prior.label");
		}
		else if (column == PARAMETERS_COLUMN) {
			columnName = RIFServiceMessages.getMessage("parameter.plural.label");
		}
		else {
			assert(false);
		}
		
		return columnName;		
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(
		int row, 
		int column) {
		
		return false;
	}	
}
