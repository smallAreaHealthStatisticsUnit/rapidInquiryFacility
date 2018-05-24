package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;


/**
 * A class used to help users determine what areas they want to view in the map.
 * The application makes use of the following statement:
 * "Select [select_level] area [select_area].  View the area at the resolution
 * of [select_view] and map to [select_map_at]."
 * 
 * Eg: Select [Region] area [Southwest England]. View the area at the resolution
 * of [District] and map at [District].  This class is used to carry values
 * that fill in the "select_map_at" part of this statement.
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

final public class GeoLevelToMap 
	extends AbstractRIFContextOption {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new geo level to map.
	 */
	private GeoLevelToMap() {
		
		super();
	}

	/**
	 * New instance.
	 *
	 * @return the geo level to map
	 */
	public static GeoLevelToMap newInstance() {
		
		GeoLevelToMap geoLevelArea = new GeoLevelToMap();
		return geoLevelArea;
	}
	
	/**
	 * New instance.
	 *
	 * @param name the name
	 * @param description the description
	 * @return the geo level to map
	 */
	public static GeoLevelToMap newInstance(
		final String name,
		final String description) {
		
		GeoLevelToMap geoLevelArea = new GeoLevelToMap();
		geoLevelArea.setName(name);
		geoLevelArea.setDescription(description);
		
		return geoLevelArea;
	}	
	
	/**
	 * New instance.
	 *
	 * @param name the name
	 * @return the geo level to map
	 */
	public static GeoLevelToMap newInstance(
		final String name) {
			
		GeoLevelToMap geoLevelArea = new GeoLevelToMap();
		geoLevelArea.setName(name);
			
		return geoLevelArea;
	}	
	
	/**
	 * Creates the copy.
	 *
	 * @param originalGeoLevel the original geo level
	 * @return the geo level to map
	 */
	public static GeoLevelToMap createCopy(
		final GeoLevelToMap originalGeoLevel) {
		
		if (originalGeoLevel == null) {
			return null;
		}
		
		GeoLevelToMap cloneGeoLevelArea = new GeoLevelToMap();		
		cloneGeoLevelArea.setName(originalGeoLevel.getName());
		cloneGeoLevelArea.setDescription(originalGeoLevel.getDescription());
		
		return cloneGeoLevelArea;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Checks for identical contents.
	 *
	 * @param otherGeoLevelToMap the other geo level to map
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final GeoLevelToMap otherGeoLevelToMap) {
		
		if (otherGeoLevelToMap == null) {
			return false;
		}
		
		return super.hasIdenticalContents(otherGeoLevelToMap);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================


	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {	
		
		super.checkSecurityViolations();
	}
	

	public void checkErrors(
		final ValidationPolicy validationPolicy)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		super.checkErrors(
				validationPolicy,
				RIFServiceError.INVALID_GEOLEVEL_TO_MAP,
				errorMessages);
		countErrors(RIFServiceError.INVALID_GEOLEVEL_TO_MAP, errorMessages);
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	@Override
	public String getRecordType() {
		
		String recordType
			= RIFServiceMessages.getMessage("geoLevelToMap.label");
		return recordType;
	}
	

}
