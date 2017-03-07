package rifGenericLibrary.fileFormats;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

public class ZipFileUtility {
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final int BUFFER = 1024;
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ZipFileUtility() {
		
		
	}


	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void writeZipFile(
		final File destinationZipFile, 
		final String[] filePathsToInclude) 
		throws IOException {
			
		ZipOutputStream zipOutputStream 
			= new ZipOutputStream(new FileOutputStream(destinationZipFile));
		BufferedInputStream inputStream = null;
		byte[] dataBlock = new byte[BUFFER];

		try {
			for (String filePathToInclude : filePathsToInclude) {
				File file = new File(filePathToInclude);
				inputStream = new BufferedInputStream(
					new FileInputStream(filePathToInclude), 
					BUFFER);
				ZipEntry fileNameZipEntry 
					= new ZipEntry(file.getName());
				zipOutputStream.putNextEntry(fileNameZipEntry);
				int count = inputStream.read(dataBlock);
				while (count > 0) {
					zipOutputStream.write(dataBlock, 0, count);
					zipOutputStream.flush();
					count = inputStream.read(dataBlock);
				}
				inputStream.close();
				zipOutputStream.closeEntry();					
			}
			zipOutputStream.flush();
			zipOutputStream.close();			
		}
		finally {
		}
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
