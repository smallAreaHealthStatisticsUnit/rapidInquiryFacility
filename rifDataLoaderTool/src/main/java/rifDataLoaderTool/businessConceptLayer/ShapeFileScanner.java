package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.fileFormats.GeneralFileFilter;
import rifDataLoaderTool.fileFormats.DirectoryFileFilter;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a utility class that will recursively scan directories for ESRI shape files.  
 * Each shape file is actually made from three separate files which have the same base
 * name but have different extensions.  These are:
 * <ul>
 * <li>a main file (<code>*.shp</code>)</li>
 * <li>an index file (<code>*.shx</code>)</li>
 * <li>a dBASE table name (<code>*.dbf</code>) </li>
 * </ul>
 * 
 * <p>
 * When the scanner scans a level, it identifies sets of three files that would make up a 
 * shape file.  We assume that all three of these files will appear in the same
 * directory level. 
 * </p>
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

public class ShapeFileScanner {

	public static final void main(String[] args) {
		String filePath = "C:/mystuff/blah.txt";
		File file = new File(filePath);
		
		String baseName
			= ShapeFileScanner.extractFileName(file);
		System.out.println("File name=="+baseName+"==");
		
	}
	
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<ShapeFile> shapeFiles;
	private DirectoryFileFilter directoryFileFilter;
	private GeneralFileFilter mainFileFilter;
	private GeneralFileFilter indexFileFilter;
	private GeneralFileFilter databaseTableFileFilter;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFileScanner() {
		shapeFiles = new ArrayList<ShapeFile>();
		
		directoryFileFilter = new DirectoryFileFilter();
		mainFileFilter = new GeneralFileFilter("SHP");
		indexFileFilter = new GeneralFileFilter("SHX");
		databaseTableFileFilter = new GeneralFileFilter("DBF");
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public static String extractFileName(final File file) {
		String fileName = file.getName();
		int index = fileName.lastIndexOf(".");
		if (index == -1) {
			//no dot found.  Therefore base file name is the whole file name
			return fileName;
		}
		else {
			return fileName.substring(0, index);
		}		
	}
	
	public ArrayList<ShapeFile> scanForShapeFiles(final File directory) {
		
		shapeFiles.clear();
						
		scan(directory);
		
		return shapeFiles;
	}
	
	private void scan(final File directory) {
		
		HashMap<String, ShapeFile> shapeFileFromBaseFileName 
			= new HashMap<String, ShapeFile>();
		
		//Create ShapeFile entries for all main shape files (*.shp)
		//that can be detected at this level
		File[] mainShapeFiles = directory.listFiles(mainFileFilter);
		for (File mainShapeFile : mainShapeFiles) {
			//addFileToPossible
			String baseFileName = extractFileName(mainShapeFile);
			ShapeFile shapeFile
				= shapeFileFromBaseFileName.get(baseFileName);
			if (shapeFile == null) {
				shapeFile = ShapeFile.newInstance();
				shapeFileFromBaseFileName.put(baseFileName, shapeFile);
			}
			shapeFile.setMainFilePath(mainShapeFile.getAbsolutePath());
		}
		
		File[] indexShapeFiles = directory.listFiles(indexFileFilter);

		
		
		

		File[] subDirectories = directory.listFiles(directoryFileFilter);

		
		
		/*
		for (File directoryFile : directoryFiles) {
			directoryFile.
			if (directoryFile.isDirectory()) {
				subDirectories.add(directoryFile);
			}
			else {
				
				
				
				
			}
			
			
		}
		
		*/
		
		
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


