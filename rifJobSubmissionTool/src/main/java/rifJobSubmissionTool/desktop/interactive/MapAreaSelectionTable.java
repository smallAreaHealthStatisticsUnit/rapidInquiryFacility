package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.util.UserInterfaceFactory;

import rifServices.businessConceptLayer.MapArea;
import rifServices.system.RIFServiceMessages;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.ArrayList;


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

class MapAreaSelectionTable 
	extends JTable {
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final long serialVersionUID = 3471627219064225804L;

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	
	
	//GUI Components
	/** The map area selection table model. */
	private MapAreaSelectionTableModel mapAreaSelectionTableModel;
	/** The identifier column header renderer. */
	private MapAreaTableCellRenderer identifierColumnHeaderRenderer;	
	/** The label column header renderer. */
	private MapAreaTableCellRenderer labelColumnHeaderRenderer;

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map area selection table.
	 *
	 * @param userInterfaceFactory the user interface factory
	 */
	public MapAreaSelectionTable(
		UserInterfaceFactory userInterfaceFactory) {

		mapAreaSelectionTableModel = new MapAreaSelectionTableModel();
		setModel(mapAreaSelectionTableModel);
	
		TableColumnModel tableColumnModel = getColumnModel();

		JTableHeader tableHeader = getTableHeader();
		
		TableColumn identifierTableColumn
			= tableColumnModel.getColumn(MapAreaSelectionTableModel.ID_COLUMN);
		identifierColumnHeaderRenderer
			= new MapAreaTableCellRenderer(userInterfaceFactory);
		String identifierToolTipText
			= RIFServiceMessages.getMessage("mapArea.identifier.toolTip");
		identifierColumnHeaderRenderer.setToolTipText(identifierToolTipText);
		identifierColumnHeaderRenderer.setBackground(tableHeader.getBackground());
		identifierTableColumn.setCellRenderer(identifierColumnHeaderRenderer);
							
		labelColumnHeaderRenderer 
			= new MapAreaTableCellRenderer(userInterfaceFactory);
		labelColumnHeaderRenderer.setBackground(tableHeader.getBackground());
		String labelToolTipText
			= RIFServiceMessages.getMessage("mapArea.label.toolTip");
		labelColumnHeaderRenderer.setText(labelToolTipText);
		TableColumn labelTableColumn
			= tableColumnModel.getColumn(MapAreaSelectionTableModel.LABEL_COLUMN);
		labelTableColumn.setCellRenderer(labelColumnHeaderRenderer);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Delete selected map areas.
	 */
	public void deleteSelectedMapAreas() {
		
		int[] selectedIndices = getSelectedRows();
		
		ArrayList<MapArea> mapAreasToDelete = new ArrayList<MapArea>();
		for (int i = 0; i < selectedIndices.length; i++) {
			mapAreasToDelete.add(
				mapAreaSelectionTableModel.getRow(selectedIndices[i]));			
		}
		mapAreaSelectionTableModel.deleteItems(mapAreasToDelete);
	}
	
	/**
	 * Highlight map areas.
	 *
	 * @param mapAreas the map areas
	 */
	public void highlightMapAreas(
		ArrayList<MapArea> mapAreas) {

		ArrayList<Integer> mapAreaIndices = new ArrayList<Integer>();
		
		for (MapArea mapArea : mapAreas) {
			int index
				= mapAreaSelectionTableModel.getIndex(mapArea);
			if (index != -1) {
				mapAreaIndices.add(index);
			}
		}
		
		labelColumnHeaderRenderer.addRowNumbersToHighlight(mapAreaIndices);
		identifierColumnHeaderRenderer.addRowNumbersToHighlight(mapAreaIndices);
		updateUI();
	}
	
	/**
	 * Clear highlighted areas.
	 */
	public void clearHighlightedAreas() {
		
		clearSelection();
		labelColumnHeaderRenderer.clearHighlightedRows();
		identifierColumnHeaderRenderer.clearHighlightedRows();
		updateUI();
	}
	
	/**
	 * Unhighlight map areas.
	 *
	 * @param mapAreas the map areas
	 */
	public void unhighlightMapAreas(
		ArrayList<MapArea> mapAreas) {	
		
		ArrayList<Integer> mapAreaIndices = new ArrayList<Integer>();
		
		for (MapArea mapArea : mapAreas) {
			int index
				= mapAreaSelectionTableModel.getIndex(mapArea);
			if (index != -1) {
				mapAreaIndices.add(index);
			}
		}
		
		labelColumnHeaderRenderer.deleteRowNumbersToHighlight(mapAreaIndices);
		identifierColumnHeaderRenderer.deleteRowNumbersToHighlight(mapAreaIndices);		
		updateUI();
	}
		
	/**
	 * Gets the selected map areas.
	 *
	 * @return the selected map areas
	 */
	public ArrayList<MapArea> getSelectedMapAreas() {
		
		ArrayList<MapArea> results = new ArrayList<MapArea>();
		int[] selectedIndices = getSelectedRows();
		for (int i = 0; i < selectedIndices.length; i++) {
			MapArea mapArea
				= mapAreaSelectionTableModel.getRow(selectedIndices[i]);
			results.add(mapArea);
		}
		
		return results;
	}
	
	/**
	 * Gets the all map areas.
	 *
	 * @return the all map areas
	 */
	public ArrayList<MapArea> getAllMapAreas() {
		
		return mapAreaSelectionTableModel.getMapAreas();	
	}
	
	/**
	 * Sets the map areas.
	 *
	 * @param mapAreas the new map areas
	 */
	public void setMapAreas(ArrayList<MapArea> mapAreas) {
		
		mapAreaSelectionTableModel.setMapAreas(mapAreas);
	}
	
	/**
	 * Clear list.
	 */
	public void clearList() {
		
		mapAreaSelectionTableModel.clearList();
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

