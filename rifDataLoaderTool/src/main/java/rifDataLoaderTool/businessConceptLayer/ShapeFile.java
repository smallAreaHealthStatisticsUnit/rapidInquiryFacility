package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;

import rifServices.system.RIFServiceMessages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.Collator;

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

public class ShapeFile 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/**
	 * shape format, contains feature geometry
	 */
	private String shpFilePath;
	
	/**
	 * shape index format
	 */
	private String shxFilePath; 
	
	/**
	 * attribute format, columnar attributes for each shape
	 */
	private String dbfFilePath;
	
	/**
	 * projection format, the coordinate system and projection information
	 */
	private String prjFilePath;

	/**
	 * sbn spatial index of features
	 */
	private String sbnFilePath;
	
	/**
	 * sbx spatial index of features
	 */
	private String sbxFilePath;

	/**
	 * fbn spatial index of features that are read-only
	 */
	private String fbnFilePath;

	/**
	 * fbx spatial index of features that are read-only
	 */
	private String fbxFilePath;

	/**
	 * ain attribute index of the active fields in a table
	 */
	private String ainFilePath;
	
	/**
	 * aih attribute index of the active fields in a table
	 */
	private String aihFilePath;
	
	/**
	 * a geocoding index for read-write datasets
	 */
	private String ixsFilePath;
	
	/**
	 * a geocoding index for read-write datasets (ODB format)
	 */
	private String mxsFilePath;
	
	/**
	 * an attribute index for the .dbf file in the 
	 * form of shapefile.columnname.atx (ArcGIS 8 and later)
	 */
	private String atxFilePath;
	
	/**
	 * used to specify the code page (only for .dbf) for identifying the character encoding to be used
	 */
	private String cpgFilePath;
	
	/**
	 * an alternative quadtree spatial index used by MapServer and GDAL/OGR software
	 */
	private String qixFilePath;
		
	
	private HashMap<ShapeFileComponent, String> filePathForShapeFileComponent;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private ShapeFile() {
		filePathForShapeFileComponent 
			= new HashMap<ShapeFileComponent, String>();
		
	}

	public static ShapeFile newInstance() {
		ShapeFile shapeFile = new ShapeFile();
		return shapeFile;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setShapeFileComponentPath(
		final ShapeFileComponent shapeFileComponent,
		final String filePath) {
		
		filePathForShapeFileComponent.put(shapeFileComponent, filePath);		
	}
	
	public String getShapeFileComponentPath(
		final ShapeFileComponent shapeFileComponent) {
		
		return filePathForShapeFileComponent.get(shapeFileComponent);
	}

	/*
	public String getShpFilePath() {
		return shpFilePath;
	}

	public void setShpFilePath(final String shpFilePath) {
		this.shpFilePath = shpFilePath;
	}

	public String getShxFilePath() {
		return shxFilePath;
	}

	public void setShxFilePath(final String shxFilePath) {
		this.shxFilePath = shxFilePath;
	}

	public String getDbfFilePath() {
		return dbfFilePath;
	}

	public void setDbfFilePath(final String dbfFilePath) {
		this.dbfFilePath = dbfFilePath;
	}
	

	public String getPrjFilePath() {
		return prjFilePath;
	}

	public void setPrjFilePath(String prjFilePath) {
		this.prjFilePath = prjFilePath;
	}


	public String getSbnFilePath() {
		return sbnFilePath;
	}

	public void setSbnFilePath(String sbnFilePath) {
		this.sbnFilePath = sbnFilePath;
	}

	public String getSbxFilePath() {
		return sbxFilePath;
	}

	public void setSbxFilePath(String sbxFilePath) {
		this.sbxFilePath = sbxFilePath;
	}

	public String getFbnFilePath() {
		return fbnFilePath;
	}

	public void setFbnFilePath(String fbnFilePath) {
		this.fbnFilePath = fbnFilePath;
	}

	public String getFbxFilePath() {
		return fbxFilePath;
	}

	public void setFbxFilePath(String fbxFilePath) {
		this.fbxFilePath = fbxFilePath;
	}

	public String getAinFilePath() {
		return ainFilePath;
	}

	public void setAinFilePath(String ainFilePath) {
		this.ainFilePath = ainFilePath;
	}

	public String getAihFilePath() {
		return aihFilePath;
	}

	public void setAihFilePath(String aihFilePath) {
		this.aihFilePath = aihFilePath;
	}

	public String getIxsFilePath() {
		return ixsFilePath;
	}

	public void setIxsFilePath(String ixsFilePath) {
		this.ixsFilePath = ixsFilePath;
	}

	public String getMxsFilePath() {
		return mxsFilePath;
	}

	public void setMxsFilePath(String mxsFilePath) {
		this.mxsFilePath = mxsFilePath;
	}

	public String getAtxFilePath() {
		return atxFilePath;
	}

	public void setAtxFilePath(String atxFilePath) {
		this.atxFilePath = atxFilePath;
	}

	public String getCpgFilePath() {
		return cpgFilePath;
	}

	public void setCpgFilePath(String cpgFilePath) {
		this.cpgFilePath = cpgFilePath;
	}

	public String getQixFilePath() {
		return qixFilePath;
	}

	public void setQixFilePath(String qixFilePath) {
		this.qixFilePath = qixFilePath;
	}
	
	*/

	
	private String extractBaseFileName(final String filePath) {
		File file = new File(filePath);
		String baseFileName = file.getName();
			
		return baseFileName;
	}
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	public boolean isValidShapeFile() {
		
		ArrayList<String> errorMessages = identifyErrorMessages();
		if (errorMessages.isEmpty() == false) {
			return false;
		}
		
		return true;
	}
	
	public void checkErrors() 
		throws RIFServiceException {

		ArrayList<String> errorMessages = identifyErrorMessages();

		countErrors(
			RIFDataLoaderToolError.INVALID_SHAPE_FILE, 
			errorMessages);		
	}
		
	private ArrayList<String> identifyErrorMessages() {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String shpFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.shp.label");
		String shxFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.shx.label");
		String dbfFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.dbf.label");
		String prjFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.prj.label");
		String sbnFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.sbn.label");
		String sbxFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.sbx.label");
		String fbnFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.fbn.label");
		String fbxFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.fbx.label");
		String ainFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.ain.label");
		String aihFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.aih.label");
		String ixsFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.ixs.label");
		String mxsFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.mxs.label");
		String atxFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.atx.label");
		String cpgFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.cpg.label");
		String qixFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.qix.label");
			
		//check if any of them are null
		checkFile(
			shpFieldName,
			shpFilePath,
			errorMessages);

		checkFile(
			shxFieldName,
			shxFilePath,
			errorMessages);

		checkFile(
			shxFieldName,
			shxFilePath,
			errorMessages);

		checkFile(
			dbfFieldName,
			dbfFilePath,
			errorMessages);

		
		checkFile(
			prjFieldName,
			prjFilePath,
			errorMessages);
				
		checkFile(
			sbnFieldName,
			sbnFilePath,
			errorMessages);
		
		checkFile(
			sbxFieldName,
			sbxFilePath,
			errorMessages);
		
		checkFile(
			fbnFieldName,
			fbnFilePath,
			errorMessages);

		
		checkFile(
			fbxFieldName,
			fbxFilePath,
			errorMessages);
				
		checkFile(
			ainFieldName,
			aihFilePath,
			errorMessages);
		
		checkFile(
			ixsFieldName,
			ixsFilePath,
			errorMessages);

		checkFile(
			mxsFieldName,
			mxsFilePath,
			errorMessages);

		checkFile(
			atxFieldName,
			atxFilePath,
			errorMessages);
		
		checkFile(
			cpgFieldName,
			cpgFilePath,
			errorMessages);
		
		checkFile(
			qixFieldName,
			qixFilePath,
			errorMessages);
		
		//Now check that they all have the same base name
		Collator collator = RIFServiceMessages.getCollator();
		String baseShpFileName 
			= extractBaseFileName(shpFilePath);
		String baseShxFileName
			= extractBaseFileName(shxFilePath);
		String baseDbfFileName
			= extractBaseFileName(dbfFilePath);
		if (collator.equals(baseShpFileName, baseShxFileName) == false ||
			collator.equals(baseShpFileName, baseDbfFileName) == false) {
		
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"shapeFile.error.invalidBaseShapeFileName");			
			errorMessages.add(errorMessage);
		}
		
		return errorMessages;
	}
	
	private void checkFile(
		final String fieldName,
		final String filePath,
		final ArrayList<String> errorMessages) {

		if (shxFilePath == null) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					fieldName);			
			errorMessages.add(errorMessage);		
		}		
		
		File file = new File(filePath);
		if (file.exists() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.nonExistentFile",
					filePath);
			errorMessages.add(errorMessage);
		}
		else if (file.canRead() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.unableToReadFile",
					filePath);			
			errorMessages.add(errorMessage);
		}	
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		
		
	}
	
	public void print() {
		StringBuilder buffer = new StringBuilder();

		ArrayList<ShapeFileComponent> shapeFileComponents
			= new ArrayList<ShapeFileComponent>(filePathForShapeFileComponent.keySet());
		for (ShapeFileComponent shapeFileComponent : shapeFileComponents) {
			buffer.append(shapeFileComponent.getFileExtension());
			buffer.append(" : ");
			buffer.append(filePathForShapeFileComponent.get(shapeFileComponent));
			buffer.append("\n");
		}

		System.out.println(buffer.toString());
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: DisplayableListItemInterface
	public String getDisplayName() {
		String shapeFileName
			= extractBaseFileName(shpFilePath);
		return shapeFileName;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


