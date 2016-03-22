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
	
	public ArrayList<String> getShapeFileComponentPaths() {
		ArrayList<String> results = new ArrayList<String>();		
		results.addAll(filePathForShapeFileComponent.values());		
		return results;
	}
	
	public void addShapeFileComponentPath(
		final String shapeFileComponentPath) {
		
		ShapeFileComponent shapeFileComponent
			= ShapeFileComponent.getShapeFileComponent(shapeFileComponentPath);
		if (shapeFileComponent != null) {
			setShapeFileComponentPath(
				shapeFileComponent, 
				shapeFileComponentPath);
		}
	}
	
	public void setShapeFileComponentPath(
		final ShapeFileComponent shapeFileComponent,
		final String filePath) {
		
		filePathForShapeFileComponent.put(shapeFileComponent, filePath);		
	}
	
	public String getShapeFileComponentPath(
		final ShapeFileComponent shapeFileComponent) {
		
		return filePathForShapeFileComponent.get(shapeFileComponent);
	}
	
	private String extractBaseFileName(final String filePath) {
		File file = new File(filePath);
		String baseFileName = file.getName();
		
		int dotIndex = baseFileName.lastIndexOf(".");
		if (dotIndex != -1) {
			return baseFileName.substring(0, dotIndex);
		}
		else {
			return baseFileName;		
		}			
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
			ShapeFileComponent.SHP,
			errorMessages);

		checkFile(
			shxFieldName,
			ShapeFileComponent.SHX,
			errorMessages);

		checkFile(
			dbfFieldName,
			ShapeFileComponent.DBF,
			errorMessages);

		checkFile(
			prjFieldName,
			ShapeFileComponent.PRJ,
			errorMessages);
				
		checkFile(
			sbnFieldName,
			ShapeFileComponent.SBN,
			errorMessages);
		
		checkFile(
			sbxFieldName,
			ShapeFileComponent.SBX,
			errorMessages);
		
		checkFile(
			fbnFieldName,
			ShapeFileComponent.FBN,
			errorMessages);
	
		checkFile(
			fbxFieldName,
			ShapeFileComponent.FBX,
			errorMessages);
				
		checkFile(
			ainFieldName,
			ShapeFileComponent.AIH,
			errorMessages);
		
		checkFile(
			ixsFieldName,
			ShapeFileComponent.IXS,
			errorMessages);

		checkFile(
			mxsFieldName,
			ShapeFileComponent.MXS,
			errorMessages);

		checkFile(
			atxFieldName,
			ShapeFileComponent.ATX,
			errorMessages);
		
		checkFile(
			cpgFieldName,
			ShapeFileComponent.CPG,
			errorMessages);
		
		checkFile(
			qixFieldName,
			ShapeFileComponent.QIX,
			errorMessages);
		
		return errorMessages;
	}

	public String[] getFilePaths() {
		ArrayList<String> filePaths = new ArrayList<String>();
		filePaths.addAll(filePathForShapeFileComponent.values());
		
		String[] results = filePaths.toArray(new String[0]);
		return results;		
	}
	
	private void checkFile(
		final String fieldName,
		final ShapeFileComponent shapeFileComponent,
		final ArrayList<String> errorMessages) {

		String filePath
			= filePathForShapeFileComponent.get(shapeFileComponent);
		
		if (filePath == null) {
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
	
	public String getBaseFilePath() {
		String filePath
			= filePathForShapeFileComponent.get(ShapeFileComponent.SHP);	
		int dotIndex = filePath.lastIndexOf(".");
		if (dotIndex == -1) {
			return filePath;
		}
		else {
			return filePath.substring(0, dotIndex);
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: DisplayableListItemInterface
	public String getDisplayName() {
		String filePath
			= filePathForShapeFileComponent.get(ShapeFileComponent.SHP);
		String shapeFileName
			= extractBaseFileName(filePath);
		return shapeFileName;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


