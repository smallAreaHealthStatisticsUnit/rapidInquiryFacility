package rifDataLoaderTool.businessConceptLayer;

import java.text.Collator;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public enum RIFSchemaArea {
	
	
	COVARIATE_DATA("rifSchema.covariateData", "covariate_data"),
	HEALTH_CODE_DATA("rifSchema.healthCodeData", "health_code_data"),
	HEALTH_THEMES("rifSchema.healthThemes", "health_themes"),
	HEALTH_NUMERATOR_DATA("rifSchema.healthNumeratorData", "health_numerator_data"),
	POPULATION_DENOMINATOR_DATA("rifSchema.populationDenominatorData", "population_denominator_data"),
	GEOMETRY_DATA("rifSchema.geometryData", "geometry_data"),
	CONTEXTUAL_MAP_DATA("rifSchema.contextualMapData", "contextual_map_data");
		
	private String propertyName;
	private String code;
	
	RIFSchemaArea(
		final String propertyName,
		final String code) {
		
		this.propertyName = propertyName;
		this.code = code;
	}
	
	public String getName() {
		return RIFDataLoaderToolMessages.getMessage(propertyName);
	}
	
	public String getCode() {
		return code;
	}
	
	public boolean matches(final String candidateCode) {
		Collator collator = RIFDataLoaderToolMessages.getCollator();
		
		return collator.equals(code, candidateCode);
	}
	
	public static RIFSchemaArea getSchemaAreaFromName(final String candidateCode) {
		Collator collator = RIFDataLoaderToolMessages.getCollator();
		
		RIFSchemaArea result = null;
		
		if (COVARIATE_DATA.matches(candidateCode)) {
			result = COVARIATE_DATA;
		}
		else if (HEALTH_CODE_DATA.matches(candidateCode)) {
			result = HEALTH_CODE_DATA;
		}
		else if (HEALTH_THEMES.matches(candidateCode)) {
			result = HEALTH_THEMES;
		}
		else if (HEALTH_NUMERATOR_DATA.matches(candidateCode)) {
			result = HEALTH_NUMERATOR_DATA;
		}
		else if (POPULATION_DENOMINATOR_DATA.matches(candidateCode)) {
			result = POPULATION_DENOMINATOR_DATA;
		}
		else if (GEOMETRY_DATA.matches(candidateCode)) {
			result = GEOMETRY_DATA;
		}
		else if (CONTEXTUAL_MAP_DATA.matches(candidateCode)) {
			result = GEOMETRY_DATA;
		}

		return result;
		
	}
	
	
}


