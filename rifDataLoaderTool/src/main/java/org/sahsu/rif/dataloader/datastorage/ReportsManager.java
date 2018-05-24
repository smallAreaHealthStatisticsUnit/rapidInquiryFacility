package org.sahsu.rif.dataloader.datastorage;

import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.generic.presentation.HTMLUtility;
import java.sql.*;

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

class ReportsManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ReportsManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public String generateHTMLReport(
		final Connection connection,
		final DataSetConfiguration dataSetConfiguration) {
		
		HTMLUtility htmlUtility = new HTMLUtility();

		/**
		 * Write report header including the name and description
		 * of the data set configuration
		 */
		//writeReportHeader(htmlUtility, dataSetConfiguration);
		
		/**
		 * Count the number of records which were processed
		 * the number which were considered duplicates
		 * the number which were cleaned
		 */
		//writeProcessingSummary(htmlUtility, dataSetConfiguration);
		
		/*
		 * 
		 */
		
		
		
		return htmlUtility.getHTML();
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


