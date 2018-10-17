package org.sahsu.rif.services.test.rifWebServices;

import java.util.ArrayList;

import org.junit.Test;
import org.sahsu.rif.services.concepts.MapArea;
import org.sahsu.rif.services.rest.MapAreaJSONGenerator;

import static org.junit.Assert.assertEquals;

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

public final class TestMapAreaJSONGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public TestMapAreaJSONGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Test
	public void acceptGeneratedJSONResult_COMMON1() {
		ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();

		MapArea mapArea1
			= MapArea.newInstance("1", "Birganj", "Ruth", 1);
		MapArea mapArea2
			= MapArea.newInstance("2", "Campo Largo", "Barbara", 1);
		MapArea mapArea3
			= MapArea.newInstance("3", "Ashbourne", "Johnny", 1);
		MapArea mapArea4
			= MapArea.newInstance("4", "Zarichchya", "Mildred", 1);
		MapArea mapArea5
			= MapArea.newInstance("5", "Eixo", "Ruby", 1);
		mapAreas.add(mapArea1);
		mapAreas.add(mapArea2);
		mapAreas.add(mapArea3);
		mapAreas.add(mapArea4);
		mapAreas.add(mapArea5);
			
		MapAreaJSONGenerator mapAreaJSONGenerator
			= new MapAreaJSONGenerator();
		String actualResult 	
			= mapAreaJSONGenerator.writeJSONMapAreas(mapAreas);
		
		StringBuilder expectedResult = new StringBuilder();
		expectedResult.append("[{\"gid\":[");
		expectedResult.append("\"1\",\"2\",\"3\",\"4\",\"5\"");
		expectedResult.append("]},");
		expectedResult.append("{\"id\":[");
		expectedResult.append("\"Birganj\",\"Campo Largo\",\"Ashbourne\",\"Zarichchya\",\"Eixo\"");
		expectedResult.append("]},");
		expectedResult.append("{\"label\":[");
		expectedResult.append("\"Ruth\",\"Barbara\",\"Johnny\",\"Mildred\",\"Ruby\"");
		expectedResult.append("]}]");
		
		assertEquals(expectedResult.toString(), actualResult);
	}
	

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
