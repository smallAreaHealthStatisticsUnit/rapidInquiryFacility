
package rifJobSubmissionTool.desktop.interactive;


import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.presentationLayer.UserInterfaceFactory;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;



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
 * Copyright 2016 Imperial College London, developed by the Small Area
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


final class ParameterTable 
	implements TableModelListener {

// ==========================================
// Section Constants
// ==========================================
	

// ==========================================
// Section Properties
// ==========================================
	
	private JTable table;
	
	/** The parameter table model. */
	private ParameterTableModel parameterTableModel;
	
	/** The table column model. */
	private TableColumnModel tableColumnModel;
	//private DefaultCellEditor valueColumnCellEditor;
    
// ==========================================
// Section Construction
// ==========================================
	
    /**
	 * Instantiates a new parameter table.
	 *
	 * @param userInterfaceFactory the user interface factory
	 */
	public ParameterTable(
		UserInterfaceFactory userInterfaceFactory) {

		parameterTableModel = new ParameterTableModel();

		tableColumnModel = new DefaultTableColumnModel();		
		TableColumn nameColumn = new TableColumn(0);
		ParameterNameTableCellRenderer parameterNameTableCellRenderer
			= new ParameterNameTableCellRenderer();
		nameColumn.setCellRenderer(parameterNameTableCellRenderer);
		tableColumnModel.addColumn(nameColumn);

		TableColumn valueColumn = new TableColumn(1);
		DefaultTableCellRenderer defaultTableCellRenderer
			= new DefaultTableCellRenderer();
		valueColumn.setCellRenderer(defaultTableCellRenderer);
		tableColumnModel.addColumn(valueColumn);		
		
		table = userInterfaceFactory.createTable(parameterTableModel);
		table.setColumnModel(tableColumnModel);	
		parameterTableModel.addTableModelListener(this);		
	}

// ==========================================
// Section Accessors and Mutators
// ==========================================
	
	public JTable getTable() {
		return table;
	}
	
	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public ArrayList<Parameter> getParameters() {
		
		return parameterTableModel.getParameters();
	}
		
	/**
	 * Sets the parameters.
	 *
	 * @param parameters the new parameters
	 */
	public void setParameters(
		ArrayList<Parameter> parameters) {

		parameterTableModel.setParameters(parameters);
	}

// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================
	//Interface: ActionListener
	
	public void tableChanged(
		TableModelEvent event) {
		
		if (event.getType() == TableModelEvent.UPDATE) {
					
			TableCellEditor editor = table.getCellEditor();
			if (editor == null) {
				//no editing taking place
				return;
			}

			String revisedParameterValue
				= (String) editor.getCellEditorValue();
			//= valueColumnTextField.getText().trim();
			int editedRow = event.getLastRow();
			int editedColumn = event.getColumn();
			parameterTableModel.setValueAt(
				revisedParameterValue.trim(), 
				editedRow, 
				editedColumn);		
		}
	}
	
// ==========================================
// Section Override
// ==========================================

}
