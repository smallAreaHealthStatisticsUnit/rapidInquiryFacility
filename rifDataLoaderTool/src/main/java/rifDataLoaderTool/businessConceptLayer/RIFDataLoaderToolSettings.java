package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import java.util.ArrayList;

/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
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

public class RIFDataLoaderToolSettings {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<GeographicalResolutionLevel> levels;
	private RIFDataTypeFactory rifDataTypeFactory;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFDataLoaderToolSettings() {
		levels = new ArrayList<GeographicalResolutionLevel>();
		rifDataTypeFactory = RIFDataTypeFactory.newInstance();
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();

		String regionName
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionLevel.region.label");
		String regionDescription
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionLevel.region.description");
		GeographicalResolutionLevel regionLevel
			= GeographicalResolutionLevel.newInstance(
				regionName, 
				regionDescription);
		levels.add(regionLevel);
			
		String districtName
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionLevel.district.label");
		String districtDescription
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionLevel.district.description");
		GeographicalResolutionLevel districtLevel
			= GeographicalResolutionLevel.newInstance(
				districtName, 
				districtDescription);
		levels.add(districtLevel);
		
		String oaName
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionLevel.oa.label");
		String oaDescription
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionLevel.oa.description");
		GeographicalResolutionLevel oaLevel
			= GeographicalResolutionLevel.newInstance(
				oaName, 
				oaDescription);
		levels.add(oaLevel);
		
		String soaName
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionLevel.soa.label");
		String soaDescription
			= RIFDataLoaderToolMessages.getMessage("geographicalResolutionLevel.soa.description");
		GeographicalResolutionLevel soaLevel
			= GeographicalResolutionLevel.newInstance(
				soaName, 
				soaDescription);
		levels.add(soaLevel);
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public ArrayList<GeographicalResolutionLevel> getGeographicalResolutionLevels() {
		return levels;
	}
	
	public void addGeographicalResolutionLevel(final GeographicalResolutionLevel level) {
		levels.add(level);
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


