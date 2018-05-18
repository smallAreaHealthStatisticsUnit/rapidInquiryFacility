package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;


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

public final class GeoLevelAttributeTheme 
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
	 * Instantiates a new geo level attribute theme.
	 */
	private GeoLevelAttributeTheme() {
		
		super();
	}

	/**
	 * New instance.
	 *
	 * @return the health theme
	 */
	public static GeoLevelAttributeTheme newInstance() {
		
		GeoLevelAttributeTheme geoLevelAttributeTheme = new GeoLevelAttributeTheme();
		return geoLevelAttributeTheme;
	}
	
	/**
	 * New instance.
	 *
	 * @param name the name
	 * @return the health theme
	 */
	public static GeoLevelAttributeTheme newInstance(
		final String name) {
			
		GeoLevelAttributeTheme geoLevelAttributeTheme = new GeoLevelAttributeTheme();
		geoLevelAttributeTheme.setName(name);
		geoLevelAttributeTheme.setDescription("");
			
		return geoLevelAttributeTheme;
	}	

	
	/**
	 * New instance.
	 *
	 * @param name the name
	 * @param description the description
	 * @return the geo level attribute theme
	 */
	public static GeoLevelAttributeTheme newInstance(
		final String name,
		final String description) {
		
		GeoLevelAttributeTheme geoLevelAttributeTheme 
			= new GeoLevelAttributeTheme();
		geoLevelAttributeTheme.setName(name);
		geoLevelAttributeTheme.setDescription(description);
		
		return geoLevelAttributeTheme;
	}	
	
	/**
	 * Creates the copy.
	 *
	 * @param originalGeoLevelAttributeTheme the original geo level attribute theme
	 * @return a cloned version of the original geo level attribute theme
	 */
	public static GeoLevelAttributeTheme createCopy(
		final GeoLevelAttributeTheme originalGeoLevelAttributeTheme) {

		if (originalGeoLevelAttributeTheme == null) {
			return null;
		}
		
		GeoLevelAttributeTheme cloneGeoLevelAttributeTheme = new GeoLevelAttributeTheme();		
		cloneGeoLevelAttributeTheme.setName(originalGeoLevelAttributeTheme.getName());
		cloneGeoLevelAttributeTheme.setDescription(originalGeoLevelAttributeTheme.getDescription());
		
		return cloneGeoLevelAttributeTheme;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void identifyDifferences(
		final GeoLevelAttributeTheme anotherGeoLevelAttributeTheme,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherGeoLevelAttributeTheme, 
			differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param othergeoLevelAttributeTheme the other health theme
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final GeoLevelAttributeTheme otherGeoLevelAttributeTheme) {
		
		return super.hasIdenticalContents(otherGeoLevelAttributeTheme);		
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
				RIFServiceError.INVALID_GEO_LEVEL_ATTRIBUTE_THEME,
				errorMessages);
		countErrors(RIFServiceError.INVALID_GEO_LEVEL_ATTRIBUTE_THEME, errorMessages);
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
			= RIFServiceMessages.getMessage("geoLevelAttributeTheme.label");
		return recordType;
	}
	

	@Override
	public String getDisplayName() {	
		
		return getDescription();
	}	
	
	@Override
	public String getIdentifier() {
		
		return getName();
	}
	
}
