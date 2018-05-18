package org.sahsu.rif.generic.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 *
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class FilePathCleaner {

	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public FilePathCleaner() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/*
	 * This routine is used to ensure that in parts of the file path that have spaces, the fragment
	 * is enclosed by spaces.  Otherwise, on a windows system, the spaces will tokenise parts of the
	 * file path as command line arguments
	 */
	public static String correctWindowsPathForSpaces(
		final String separatorCharacter,
		final String originalPath) {

		StringBuilder cleanedPath = new StringBuilder();

		String[] pathComponents = originalPath.split(separatorCharacter);		
		
		for (int i = 0; i < pathComponents.length; i++) {
			if (i != 0) {
				cleanedPath.append(File.separator);
			}
			
			if (pathComponents[i].contains(" ")) {
				cleanedPath.append("\"");
				cleanedPath.append(pathComponents[i]);
				cleanedPath.append("\"");
			}
			else {
				cleanedPath.append(pathComponents[i]);				
			}
		}
		

		
		return cleanedPath.toString();
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
