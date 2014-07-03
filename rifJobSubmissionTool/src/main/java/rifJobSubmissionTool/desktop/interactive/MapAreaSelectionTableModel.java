package rifJobSubmissionTool.desktop.interactive;

import rifServices.businessConceptLayer.MapArea;
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

class MapAreaSelectionTableModel  
	extends AbstractTableModel {
	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final long serialVersionUID = -3880362814390600015L;
	/** The Constant ID_COLUMN. */
	public static final int ID_COLUMN = 0;	
	/** The Constant LABEL_COLUMN. */
	public static final int LABEL_COLUMN = 1;	
	/** The Constant LABEL_TRUNCATION_LENGTH. */
	private static final int LABEL_TRUNCATION_LENGTH = 40;
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	/** The map areas. */
	private ArrayList<MapArea> mapAreas;
	
	//GUI Components
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map area selection table model.
	 */
	public MapAreaSelectionTableModel() {

		mapAreas = new ArrayList<MapArea>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Gets the map areas.
	 *
	 * @return the map areas
	 */
	public ArrayList<MapArea> getMapAreas() {
		
		return mapAreas;
	}
	
	/**
	 * Sets the map areas.
	 *
	 * @param mapAreas the new map areas
	 */
	public void setMapAreas(
		ArrayList<MapArea> mapAreas) {
		
		this.mapAreas = mapAreas;
		fireTableDataChanged();
	}
	
	/**
	 * Gets the index.
	 *
	 * @param mapAreaToFind the map area to find
	 * @return the index
	 */
	public int getIndex(
		MapArea mapAreaToFind) {

		for (int i = 0; i < mapAreas.size(); i++) {
			MapArea currentMapArea = mapAreas.get(i);
			if (currentMapArea.hasIdenticalContents(mapAreaToFind)) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Gets the row.
	 *
	 * @param row the row
	 * @return the row
	 */
	public MapArea getRow(
		int row) {

		return mapAreas.get(row);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		
		return mapAreas.size();
	}
	
	/**
	 * Clear list.
	 */
	public void clearList() {
		
		mapAreas.clear();
		fireTableDataChanged();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		
		return 2;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(
		int row, 
		int column) {

		MapArea mapArea = mapAreas.get(row);
		
		String value = "";
		if (column == ID_COLUMN) {
			value = mapArea.getIdentifier();
		}
		else if (column == LABEL_COLUMN) {
			String fieldLabel = mapArea.getLabel();
			if (fieldLabel == null) {
				value = "";
			}
			else {
				value = truncateFieldValue(mapArea.getLabel());				
			}
		}
		else {
			assert(false);
		}
		return value;
	}

	/**
	 * Truncate field value.
	 *
	 * @param fieldValue the field value
	 * @return the string
	 */
	private String truncateFieldValue(
		String fieldValue) {

		StringBuilder value = new StringBuilder();
		if (fieldValue.length() <= LABEL_TRUNCATION_LENGTH) {
			value.append(fieldValue);
		}
		else {
			value.append(fieldValue.substring(0,LABEL_TRUNCATION_LENGTH));
			value.append("...");			
		}
		return value.toString();
	}

	/**
	 * Delete items.
	 *
	 * @param mapAreasToDelete the map areas to delete
	 */
	public void deleteItems(
		ArrayList<MapArea> mapAreasToDelete) {

		for (MapArea mapAreaToDelete : mapAreasToDelete) {
			mapAreas.remove(mapAreaToDelete);
		}
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

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(
		int column) {

		String columnName = "";
		
		if (column == ID_COLUMN) {
			columnName = RIFServiceMessages.getMessage("mapArea.identifier.label");
			return columnName;
		}
		else if (column == LABEL_COLUMN) {
			columnName = RIFServiceMessages.getMessage("mapArea.label.label");
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

