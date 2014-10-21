package rifDataLoaderTool.clean;

import java.text.Collator;

import rifDataLoaderTool.dataStorageLayer.DataLoaderService;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.system.RIFServiceMessages;

import org.junit.After;
import org.junit.Before;

import junit.framework.TestCase;


/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public abstract class AbstractRIFDataLoaderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderService dataLoaderService;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRIFDataLoaderTestCase() {

	}

	@Before
	public void setUp() {
		dataLoaderService = new DataLoaderService();
		dataLoaderService.initialiseService();
	}

	@After
	public void tearDown() {

		
	}		
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	protected DataLoaderService getDataLoaderService() {

		return dataLoaderService;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================


	protected boolean expectedAndActualTablesIdentical(
		final RIFResultTable resultTable,
		final String[][] expectedResults) {
		
		String[][] actualResults
			= resultTable.getData();
		
		if (actualResults.length != expectedResults.length) {
			//different number of rows
			System.out.println(
				"Expected number of rows=="+
				expectedResults.length+
				"==actual=="+
				actualResults.length+
				"==");
			return false;
		}
		
		Collator collator = RIFServiceMessages.getCollator();
		for (int ithRow = 0; ithRow < expectedResults.length; ithRow++) {
			int numberOfColumns = expectedResults[ithRow].length;
			if (actualResults[ithRow].length != expectedResults[ithRow].length) {
				//different number of columns for corresponding rows
				System.out.println(
						"Expected number of columns=="+
						expectedResults[ithRow].length+
						"==actual=="+
						actualResults[ithRow].length+
						"==");
				return false;
			}
			else {
				for (int ithColumn = 0; ithColumn < numberOfColumns; ithColumn++) {
					if (collator.equals(
						actualResults[ithRow][ithColumn],
						expectedResults[ithRow][ithColumn]) == false) {

						StringBuilder buffer = new StringBuilder();
						buffer.append("Actual[");
						buffer.append(String.valueOf(ithRow));
						buffer.append("]");
						buffer.append("[");
						buffer.append(String.valueOf(ithColumn));
						buffer.append("]");
						buffer.append("=");
						buffer.append("'");
						buffer.append(actualResults[ithRow][ithColumn]);
						buffer.append("' ");
						buffer.append("Expected[");
						buffer.append(String.valueOf(ithRow));
						buffer.append("]");
						buffer.append("[");
						buffer.append(String.valueOf(ithColumn));
						buffer.append("]");
						buffer.append("=");
						buffer.append("'");
						buffer.append(expectedResults[ithRow][ithColumn]);
						buffer.append("'");

						System.out.println(buffer.toString());
						
						return false;
					}
					
				}				
			}
		}
		
		return true;
	}
	
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


