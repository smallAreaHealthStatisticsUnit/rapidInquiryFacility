package org.sahsu.rif.services.rest;

import java.util.List;

import org.sahsu.rif.services.concepts.AgeGroup;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
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

final public class AgeGroupJSONGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public AgeGroupJSONGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String writeJSONMapAreas(
		final List<AgeGroup> ageGroups) {
		

		StringBuilder result = new StringBuilder();
		
		StringBuilder namePhrase = new StringBuilder();
		StringBuilder lowerAgeLimitPhrase = new StringBuilder();
		StringBuilder upperLimitPhrase = new StringBuilder();
		
		namePhrase.append("{\"name\":[");
		lowerAgeLimitPhrase.append("{\"lowerAgeLimit\":[");
		upperLimitPhrase.append("{\"upperAgeLimit\":[");
		
		int numberOfAgeGroups = ageGroups.size();
		for (int i = 0; i < numberOfAgeGroups; i++) {
			AgeGroup ageGroup = ageGroups.get(i);
			
			if (i != 0) {
				namePhrase.append(",");
				lowerAgeLimitPhrase.append(",");
				upperLimitPhrase.append(",");				
			}

			namePhrase.append("\"");
			namePhrase.append(ageGroup.getName());
			namePhrase.append("\"");
			
			lowerAgeLimitPhrase.append("\"");
			lowerAgeLimitPhrase.append(ageGroup.getLowerLimit());
			lowerAgeLimitPhrase.append("\"");
			
			upperLimitPhrase.append("\"");
			upperLimitPhrase.append(ageGroup.getUpperLimit());			
			upperLimitPhrase.append("\"");
			
		}
		namePhrase.append("]}");
		lowerAgeLimitPhrase.append("]}");
		upperLimitPhrase.append("]}");

		result.append("[");
		result.append(namePhrase.toString());
		result.append(",");
		result.append(lowerAgeLimitPhrase.toString());
		result.append(",");
		result.append(upperLimitPhrase.toString());
		result.append("]");
				
		return result.toString();		
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
