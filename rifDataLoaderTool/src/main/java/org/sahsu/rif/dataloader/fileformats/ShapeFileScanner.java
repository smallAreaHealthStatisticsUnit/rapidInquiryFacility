package org.sahsu.rif.dataloader.fileformats;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.sahsu.rif.dataloader.concepts.ShapeFile;
import org.sahsu.rif.dataloader.concepts.ShapeFileComponent;
import org.sahsu.rif.generic.fileformats.DirectoryFileFilter;

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
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<ShapeFile> shapeFiles;
	private DirectoryFileFilter directoryFileFilter;
	private ShapeFileComponentFilter shpFileFilter;
	private ShapeFileComponentFilter shxFileFilter;
	private ShapeFileComponentFilter dbfFileFilter;	
	private ShapeFileComponentFilter prjFileFilter;
	private ShapeFileComponentFilter sbxFileFilter;
	private ShapeFileComponentFilter fbnFileFilter;
	private ShapeFileComponentFilter fbxFileFilter;
	private ShapeFileComponentFilter ainFileFilter;
	private ShapeFileComponentFilter aihFileFilter;
	private ShapeFileComponentFilter ixsFileFilter;
	private ShapeFileComponentFilter mxsFileFilter;
	private ShapeFileComponentFilter atxFileFilter;
	private ShapeFileComponentFilter cpgFileFilter;
	private ShapeFileComponentFilter qixFileFilter;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFileScanner() {
		shapeFiles = new ArrayList<ShapeFile>();
		
		
		directoryFileFilter = new DirectoryFileFilter();
				
		//Mandatory shape file components
		shpFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.SHP);
		shxFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.SHX);
		dbfFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.DBF);
		
		//Optional shape file components
		prjFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.PRJ);
		sbxFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.SBX);
		fbnFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.FBN);
		fbxFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.FBX);
		ainFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.AIN);
		aihFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.AIH);
		ixsFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.IXS);
		mxsFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.MXS);
		atxFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.ATX);
		cpgFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.CPG);
		qixFileFilter = new ShapeFileComponentFilter(ShapeFileComponent.QIX);
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
		
		String currentDirectoryBasePath
			= directory.getAbsolutePath();
		
		//Part I: Scan for any files that would be relevant to the
		//shape files
		HashSet<String> allFileNames = new HashSet<String>();
		
		//Create ShapeFile entries for all main shape files (*.shp)
		//that can be detected at this level
		HashSet<String> shpFileNames = new HashSet<String>();
		File[] shpFiles = directory.listFiles(shpFileFilter);
		for (File shpFile : shpFiles) {
			//addFileToPossible
			String baseFileName = extractFileName(shpFile);
			allFileNames.add(baseFileName);
			shpFileNames.add(baseFileName);
		}
		
		//Identify any *.shx index files
		HashSet<String> shxFileNames = new HashSet<String>();
		File[] shxFiles = directory.listFiles(shxFileFilter);
		for (File shxFile : shxFiles) {
			//addFileToPossible
			String baseFileName = extractFileName(shxFile);
			
			allFileNames.add(baseFileName);
			shxFileNames.add(baseFileName);
		}
		
		//Identify any *.dbf files
		HashSet<String> dbfFileNames = new HashSet<String>();
		File[] dbfFiles = directory.listFiles(dbfFileFilter);
		for (File dbfFile : dbfFiles) {
			//addFileToPossible
			String baseFileName = extractFileName(dbfFile);
			allFileNames.add(baseFileName);
			dbfFileNames.add(baseFileName);
		}		
		
		//Part II: Select shape files that meet minimum requirements

		HashMap<String, ShapeFile> shapeFileFromName
			= new HashMap<String, ShapeFile>();
		Iterator<String> allFilesIterator = allFileNames.iterator();
		while (allFilesIterator.hasNext()) {
			String currentCandidate = allFilesIterator.next();
			if (shpFileNames.contains(currentCandidate) &&
				shxFileNames.contains(currentCandidate) &&
				dbfFileNames.contains(currentCandidate)) {
				
				//We have the minimum to have a shape file
				ShapeFile shapeFile = ShapeFile.newInstance();

				setShapeFileComponentPath(
					shapeFile,
					currentDirectoryBasePath,
					directory,
					currentCandidate,
					shpFileFilter);			
				
				setShapeFileComponentPath(
					shapeFile,
					currentDirectoryBasePath,
					directory,
					currentCandidate,
					shxFileFilter);			

				setShapeFileComponentPath(
					shapeFile,
					currentDirectoryBasePath,
					directory,
					currentCandidate,
					dbfFileFilter);

				shapeFileFromName.put(currentCandidate, shapeFile);		
			}
		}
				
		//Add optional layers
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			prjFileFilter);
	
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			sbxFileFilter);
				
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			fbnFileFilter);
		
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			fbxFileFilter);
				
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			ainFileFilter);
	
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			aihFileFilter);
				
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			ixsFileFilter);
			
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			mxsFileFilter);

		addShapeFileLayer(
			directory,
			shapeFileFromName,
			atxFileFilter);
		
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			cpgFileFilter);
		
		addShapeFileLayer(
			directory,
			shapeFileFromName,
			qixFileFilter);		
		
		shapeFiles.addAll(shapeFileFromName.values());
		
		//now visit directories recursively
		File[] subDirectories
			= directory.listFiles(directoryFileFilter);
		for (File subDirectory : subDirectories) {
			scan(subDirectory);
		}
	}

	private void addShapeFileLayer(
		final File directory,
		final HashMap<String, ShapeFile> shapeFileFromName,
		final ShapeFileComponentFilter filter) {
	
		String currentDirectoryBasePath = directory.getAbsolutePath();
		
		File[] candidateFiles = directory.listFiles(filter);
		
		for (File candidateFile : candidateFiles) {
			String candidateBaseName = extractFileName(candidateFile);
			ShapeFile shapeFile = shapeFileFromName.get(candidateBaseName);

			if (shapeFile != null) {
				StringBuilder path = new StringBuilder();
				path.append(currentDirectoryBasePath);
				path.append(File.separator);
				path.append(candidateBaseName);
				path.append(filter.getFileExtension());
				
				shapeFile.setShapeFileComponentPath(
					filter.getShapeFileComponent(), 
					path.toString());
			}
		}	
	}
		
	private void setShapeFileComponentPath(
		final ShapeFile shapeFile,
		final String currentDirectoryBasePath,
		final File directory,
		final String currentCandidate,
		final ShapeFileComponentFilter filter) {

		StringBuilder path = new StringBuilder();
		path.append(currentDirectoryBasePath);
		path.append(File.separator);
		path.append(currentCandidate);
		path.append(filter.getFileExtension());
		
		shapeFile.setShapeFileComponentPath(
			filter.getShapeFileComponent(), 
			path.toString());
	}
/*
	private String[] getCandidatesForLayer(
		final File directory,
		final ShapeFileComponentFilter filter) {
		
		//Processing optional files
		File[] prjFiles = directory.listFiles(filter);
		String[] results = new String[prjFiles.length];
		for (int i = 0; i < prjFiles.length; i++) {
			results[i] = extractFileName(prjFiles[i]);
		}
		
		return results;
	}
	
	private String createShapeFileComponentPath(
		final String baseDirectoryPath,
		final String baseFileName,
		final ShapeFileComponentFilter filter) {
		
		StringBuilder path = new StringBuilder();
		path.append(baseDirectoryPath);
		path.append(File.separator);
		path.append(baseFileName);
		path.append(filter.getFileExtension());
		return path.toString();
	}
*/
	
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


