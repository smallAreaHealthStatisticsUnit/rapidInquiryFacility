package rifJobSubmissionTool.system;

import rifServices.businessConceptLayer.MapArea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;



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

public final class MapAreaSelectionBasket 
	extends Observable {
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	/** The map areas from display name. */
	private HashMap<String, MapArea> mapAreasFromDisplayName;	
	/** The map areas. */
	private ArrayList<MapArea> mapAreas;
	
	//GUI Components
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map area selection basket.
	 */
	private MapAreaSelectionBasket() {
		mapAreas = new ArrayList<MapArea>();
		mapAreasFromDisplayName = new HashMap<String, MapArea>();
	}

	/**
	 * New instance.
	 *
	 * @return the map area selection basket
	 */
	public static MapAreaSelectionBasket newInstance() {
		MapAreaSelectionBasket basket = new MapAreaSelectionBasket();
		return basket;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		
		if (mapAreas.isEmpty()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the map areas.
	 *
	 * @return the map areas
	 */
	public ArrayList<MapArea> getMapAreas() {
		
		return mapAreas;
	}
	
	/**
	 * Gets the map areas.
	 *
	 * @param startIndex the start index
	 * @param endIndex the end index
	 * @return the map areas
	 */
	public ArrayList<MapArea> getMapAreas(
		final int startIndex, 
		final int endIndex) {
	
		int size = getSize();
		
		int lastIndex = endIndex;
		if (endIndex >= size) {
			lastIndex = size - 1;
		}
		
		ArrayList<MapArea> results = new ArrayList<MapArea>();
		for (int i = startIndex; i <= lastIndex; i++) {
			results.add(mapAreas.get(i));
		}
		
		return results;		
	}
	
	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public int getSize() {
		
		return mapAreasFromDisplayName.size();
	}
	
	/**
	 * Clear map areas.
	 */
	public void clearMapAreas() {
		
		mapAreasFromDisplayName.clear();
		mapAreas.clear();
		
		MapAreaSelectionEvent mapSelectionEvent
			= new MapAreaSelectionEvent(
				MapAreaSelectionEvent.OperationType.BASKET_EMPTIED);
		setChanged();
		notifyObservers(mapSelectionEvent);			
	}
	
	/**
	 * Adds the map areas.
	 *
	 * @param mapAreasToAdd the map areas to add
	 */
	public void addMapAreas(
		ArrayList<MapArea> mapAreasToAdd) {

		ArrayList<MapArea> uniqueItemsToAdd
			= new ArrayList<MapArea>();
		for (MapArea mapAreaToAdd : mapAreasToAdd) {
			String displayName = mapAreaToAdd.getDisplayName();
			if (mapAreasFromDisplayName.containsKey(displayName) == false) {
				mapAreasFromDisplayName.put(displayName, mapAreaToAdd);
				mapAreas.add(mapAreaToAdd);
				uniqueItemsToAdd.add(mapAreaToAdd);
			}
		}
	
		MapAreaSelectionEvent mapSelectionEvent
			= new MapAreaSelectionEvent(
				MapAreaSelectionEvent.OperationType.ITEMS_ADDED,
				uniqueItemsToAdd);
		setChanged();
		notifyObservers(mapSelectionEvent);		
	}
	
	/**
	 * Sets the map areas.
	 *
	 * @param mapAreas the new map areas
	 */
	public void setMapAreas(
		ArrayList<MapArea> mapAreas) {

		mapAreasFromDisplayName.clear();
		mapAreas.clear();
		addMapAreas(mapAreas);
	}
	
	/**
	 * Contains map area.
	 *
	 * @param mapArea the map area
	 * @return true, if successful
	 */
	public boolean containsMapArea(
		MapArea mapArea) {

		return mapAreasFromDisplayName.containsKey(mapArea.getDisplayName());
	}
	
	/**
	 * Delete map areas.
	 *
	 * @param mapAreasToDelete the map areas to delete
	 */
	public void deleteMapAreas(
		ArrayList<MapArea> mapAreasToDelete) {

		ArrayList<MapArea> uniqueItemsToDelete
			= new ArrayList<MapArea>();
		for (MapArea mapAreaToDelete : mapAreasToDelete) {
			String displayName = mapAreaToDelete.getDisplayName();
			if (mapAreasFromDisplayName.containsKey(displayName) == true) {
				uniqueItemsToDelete.add(mapAreaToDelete);
				MapArea targetMapArea
					= mapAreasFromDisplayName.get(displayName);
				mapAreasFromDisplayName.remove(displayName);
				mapAreas.remove(targetMapArea);
			}
		}
	
		MapAreaSelectionEvent mapSelectionEvent
			= new MapAreaSelectionEvent(
				MapAreaSelectionEvent.OperationType.ITEMS_DELETED,
				uniqueItemsToDelete);
		setChanged();
		notifyObservers(mapSelectionEvent);		
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

