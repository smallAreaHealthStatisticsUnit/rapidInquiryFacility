package org.sahsu.rif.services.rest;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.json.JSONObject;


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

public final class RIFResultTableJSONGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public RIFResultTableJSONGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String writeResultTable(
		final RIFResultTable resultTable) {
		
		return writeResultTable(resultTable, null);
			
	}
	
	public String writeResultTable(
		final RIFResultTable resultTable,
		final JSONObject additionalTableJson) {
		
		StringBuilder result = new StringBuilder();
		
		String[] columnNames = resultTable.getColumnNames();
		RIFResultTable.ColumnDataType[] dataTypes = resultTable.getColumnDataTypes();
		String[][] data = resultTable.getData();
		
		int numberOfRows = data.length;
		int numberOfColumns = columnNames.length;		
			
		result.append("{");
		if (additionalTableJson != null) {
			result.append("\"additionalTableJson\":");
			result.append(additionalTableJson.toString());
			result.append(",");
		}
		
		//write out table header
		result.append("\"smoothed_results_header\":[");
		for (int ithColumnName = 0; ithColumnName < numberOfColumns; ithColumnName++) {
			if (ithColumnName != 0) {
				result.append(",");
			}
			result.append("\"");
			result.append(columnNames[ithColumnName]);
			result.append("\"");
		}
		result.append("],");
		
		//write out data
		result.append("\"smoothed_results\":[");		
		for (int currentRow = 0; currentRow < numberOfRows; currentRow++) {
			if (currentRow != 0) {
				result.append(",");
			}
			
			result.append("{");
			for (int currentColumn = 0; currentColumn < numberOfColumns; currentColumn++) {
				if (currentColumn != 0) {
					result.append(",");
				}
				if (dataTypes[currentColumn] == RIFResultTable.ColumnDataType.TEXT) {
					result.append("\"");
					result.append(columnNames[currentColumn]);
					result.append("\"");
					result.append(":");
					result.append(RIFResultTable.quote(data[currentRow][currentColumn]));	
				}				
				else if (dataTypes[currentColumn] == RIFResultTable.ColumnDataType.JSON) { // No escaping at all: you do it yourself!!!!
					result.append("\"");
					result.append(columnNames[currentColumn]);
					result.append("\"");
					result.append(":");
					result.append(data[currentRow][currentColumn]);	
				}
				else {
					//it is numeric
					result.append("\"");
					result.append(columnNames[currentColumn]);
					result.append("\"");
					result.append(":");
					result.append(data[currentRow][currentColumn]);					
				}
				
			}			
			result.append("}");
			
		}		
		result.append("]");

		//finish off the whole data set
		result.append("}");
		
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
