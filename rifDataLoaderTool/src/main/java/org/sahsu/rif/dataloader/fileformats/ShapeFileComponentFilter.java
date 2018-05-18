package org.sahsu.rif.dataloader.fileformats;

import java.io.File;
import java.io.FileFilter;

import org.sahsu.rif.dataloader.concepts.ShapeFileComponent;

/**
 * A general class for filtering files using the file.listFiles(FileFilter fileFilter) method.
 * The constructor takes a file extension - do not add the file name ".", just the letters
 * of the file extension that would follow it.
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

class ShapeFileComponentFilter implements FileFilter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ShapeFileComponent shapeFileComponent;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFileComponentFilter(final ShapeFileComponent shapeFileComponent) {
		this.shapeFileComponent = shapeFileComponent;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public String getFileExtension() {
		return shapeFileComponent.getFileExtension();
	}
	
	public ShapeFileComponent getShapeFileComponent() {
		return shapeFileComponent;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	public boolean accept(final File candidateFile) {
				
		String fileExtension
			= shapeFileComponent.getFileExtension().toUpperCase();
			
		if (candidateFile.isDirectory()) {
			return false;
		}
		
		String upperCaseFilePath
			= candidateFile.getAbsolutePath().toUpperCase();
		if (upperCaseFilePath.endsWith(fileExtension) == true) {
			return true;
		}
		
		return false;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


