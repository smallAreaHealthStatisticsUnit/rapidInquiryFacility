package rifJobSubmissionTool.desktop.interactive;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import rifGenericLibrary.presentationLayer.UserInterfaceFactory;




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

final class MapAreaTableCellRenderer 
	extends DefaultTableCellRenderer {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final long serialVersionUID = 868146796402429323L;

	// ==========================================
	// Section Properties
	// ==========================================
	

	//Data
	/** The highlighted indices. */
	private ArrayList<Integer> highlightedIndices;	


	//GUI Components
	/** The user interface factory. */
	private UserInterfaceFactory userInterfaceFactory;
	/** The unhighlighted colour. */
	private Color unhighlightedColour;

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map area table cell renderer.
	 *
	 * @param userInterfaceFactory the user interface factory
	 */
	public MapAreaTableCellRenderer(
		UserInterfaceFactory userInterfaceFactory) {

		this.userInterfaceFactory = userInterfaceFactory;
		highlightedIndices = new ArrayList<Integer>();
		unhighlightedColour = getForeground();
		setBackground(Color.white);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
 
	public Component getTableCellRendererComponent(
    	JTable table, 
    	Object value, 
    	boolean isSelected, 
    	boolean hasFocus, 
    	int row, 
    	int column) {

    	// delegate the rendering part to the default renderer (am i right ???)
        Component comp 
        	= super.getTableCellRendererComponent(
        		table, 
        		value, 
        		isSelected, 
        		hasFocus, 
        		row, 
        		column);

        if (highlightedIndices.contains(row)) {
        	comp.setForeground(Color.blue);
        	userInterfaceFactory.setBoldFont(comp);
        	
        }
        else {
        	comp.setForeground(unhighlightedColour);
        	userInterfaceFactory.setPlainFont(comp);
        }
        
        return comp;
    }
	
    /**
     * Clear highlighted rows.
     */
    public void clearHighlightedRows() {
    	
    	highlightedIndices.clear();
    }
    
    /**
     * Adds the row numbers to highlight.
     *
     * @param rowNumbers the row numbers
     */
    public void addRowNumbersToHighlight(
    	ArrayList<Integer> rowNumbers) {
    	
    	highlightedIndices.addAll(rowNumbers);
    }
    
    /**
     * Adds the row number to highlight.
     *
     * @param rowNumber the row number
     */
    public void addRowNumberToHighlight(
    	int rowNumber) {
    	
    	highlightedIndices.add(rowNumber);
    }
    
    /**
     * Delete row numbers to highlight.
     *
     * @param rowNumbers the row numbers
     */
    public void deleteRowNumbersToHighlight(
    	ArrayList<Integer> rowNumbers) {
    	
    	for (Integer rowNumber : rowNumbers) {
    		highlightedIndices.remove(rowNumber);
    	}
    }
        
    /**
     * Clear.
     */
    public void clear() {
    	
    	highlightedIndices.clear();
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

