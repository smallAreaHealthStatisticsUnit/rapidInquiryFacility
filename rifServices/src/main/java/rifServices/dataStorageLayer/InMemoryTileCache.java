package rifServices.dataStorageLayer;

import java.util.HashMap;
import java.util.LinkedList;

import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.Geography;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

public class InMemoryTileCache {

	// ==========================================
	// Section Constants
	// ==========================================
	private int MAXIMUM_HASH_KEYS_MAINTAINED = 100;

	private static final InMemoryTileCache inMemoryTileCache = new InMemoryTileCache();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private HashMap<String, String> tileResultFromKey;
	private LinkedList<String> agingKeys;

	
	private Object lock = new Object();
	
	// ==========================================
	// Section Construction
	// ==========================================

	private InMemoryTileCache() {
		tileResultFromKey = new HashMap<String, String>();
		agingKeys = new LinkedList<String>();
	}

	public static InMemoryTileCache getInMemoryTileCache() {
		return inMemoryTileCache;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void putTileResult(
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final String tileIdentifier,
		final Integer zoomFactor,
		final BoundaryRectangle boundaryRectangle,
		final String tileResult) {

		String key 
			= generateKey(
				geography,
				geoLevelSelect,
				tileIdentifier,
				zoomFactor,
				boundaryRectangle);
		
		
		synchronized(lock) {
			if (tileResultFromKey.size() > MAXIMUM_HASH_KEYS_MAINTAINED) {
				//find the oldest key and remove it
				String oldestKey
					= agingKeys.removeFirst();
				tileResultFromKey.remove(oldestKey);
			}
			
			tileResultFromKey.put(key, tileResult);
			agingKeys.addLast(key);
		}
	}

	public String getTileResult(
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final String tileIdentifier,
		final Integer zoomFactor,
		final BoundaryRectangle boundaryRectangle) {

		String key 
			= generateKey(
				geography,
				geoLevelSelect,
				tileIdentifier,
				zoomFactor,
				boundaryRectangle);

		synchronized(lock) {
			String tileResult = tileResultFromKey.get(key);
			
			if (tileResult != null) {
				//find key in queue of most recently used keys
				//and put it at the end of the queue
				agingKeys.remove(key);
				agingKeys.addLast(key);				
			}
			
			return tileResult;
		}
	}
		
	private String generateKey(
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final String tileIdentifier,
		final Integer zoomFactor,
		final BoundaryRectangle boundaryRectangle) {
			
		StringBuilder tileHashKey = new StringBuilder();
		tileHashKey.append(geography.getName());
		tileHashKey.append("-");
		tileHashKey.append(geoLevelSelect.getName());
		tileHashKey.append("-");
		tileHashKey.append(tileIdentifier);
		tileHashKey.append("-");
		tileHashKey.append(String.valueOf(zoomFactor));
		tileHashKey.append("-");
		tileHashKey.append(boundaryRectangle.convertToKey());
		
		return tileHashKey.toString();	
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
