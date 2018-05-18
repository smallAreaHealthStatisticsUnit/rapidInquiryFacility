package org.sahsu.rif.generic.system;

import java.io.File;

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

public class ClassFileLocator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ClassFileLocator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

    public static String getClassRootLocation(final String subProjectName) {
    	String classPath = System.getProperty("java.class.path");
    	
    	//we will get every directory used in the classpath.  Now we want to 
    	//find the directory that is the start of where the class files for the
    	//rifServices project are located.  This location can be used to find
    	//other resource files (eg: text and XML files).
		String[] classPathEntries
			= classPath.split(File.pathSeparator);
		StringBuilder pathToFind = new StringBuilder();
		pathToFind.append("rapidInquiryFacility");
		pathToFind.append(File.separator);
		//pathToFind.append("rifGenericLibrary");
		pathToFind.append(subProjectName);
		pathToFind.append(File.separator);
		pathToFind.append("target");
		pathToFind.append(File.separator);
		pathToFind.append("classes");
    	
		String targetPath = pathToFind.toString();
		
		String targetClassesEntry = null;
    	for (String classPathEntry : classPathEntries) {
    		if (classPathEntry.endsWith(pathToFind.toString())) {
    			targetClassesEntry = classPathEntry;
    			break;
    		}
    	}
    	
    	return targetClassesEntry;
    	
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
