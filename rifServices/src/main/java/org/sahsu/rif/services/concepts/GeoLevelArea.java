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
 * that fill in the "select_area" part of this statement.
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

public final class GeoLevelArea 
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
	 * Instantiates a new geo level area.
	 */
	private GeoLevelArea() {
		
		super();
	}

	/**
	 * New instance.
	 *
	 * @return the geo level area
	 */
	public static GeoLevelArea newInstance() {
		
		GeoLevelArea geoLevelArea = new GeoLevelArea();
		return geoLevelArea;
	}
	
	/**
	 * New instance.
	 *
	 * @param name the name
	 * @param description the description
	 * @return the geo level area
	 */
	public static GeoLevelArea newInstance(
		final String name,
		final String description) {
		
		GeoLevelArea geoLevelArea = new GeoLevelArea();
		geoLevelArea.setName(name);
		geoLevelArea.setDescription(description);
		
		return geoLevelArea;
	}	
	
	/**
	 * New instance.
	 *
	 * @param name the name
	 * @return the geo level area
	 */
	public static GeoLevelArea newInstance(
		final String name) {
			
		GeoLevelArea geoLevelArea = new GeoLevelArea();
		geoLevelArea.setName(name);
			
		return geoLevelArea;
	}	
	
	
	/**
	 * Creates the copy.
	 *
	 * @param originalGeoLevel the original geo level
	 * @return the geo level area
	 */
	public static GeoLevelArea createCopy(
		final GeoLevelArea originalGeoLevel) {
		
		if (originalGeoLevel == null) {
			return null;
		}
		
		GeoLevelArea cloneGeoLevelArea = new GeoLevelArea();	
		cloneGeoLevelArea.setIdentifier(originalGeoLevel.getIdentifier());
		cloneGeoLevelArea.setName(originalGeoLevel.getName());
		cloneGeoLevelArea.setDescription(originalGeoLevel.getDescription());
		
		return cloneGeoLevelArea;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void identifyDifferences(
		final GeoLevelArea anotherGeoLevelArea,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherGeoLevelArea, 
			differences);
		
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherGeoLevelArea the other geo level area
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final GeoLevelArea otherGeoLevelArea) {
		
		if (otherGeoLevelArea == null) {
			return false;
		}
		
		return super.hasIdenticalContents(otherGeoLevelArea);
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
				RIFServiceError.INVALID_GEOLEVEL_AREA,
				errorMessages);
		countErrors(RIFServiceError.INVALID_GEOLEVEL_AREA, errorMessages);
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
			= RIFServiceMessages.getMessage("geoLevelArea.label");
		return recordType;
	}
	

	public String getDisplayName() {
		
		String name = getName();
		if (name == null) {
			return getIdentifier();
		}
		else {
			return name;
		}
	}	
	
}
