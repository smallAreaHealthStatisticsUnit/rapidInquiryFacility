package org.sahsu.rif.dataloader.concepts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

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
	extends AbstractDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	public static final int UNKNOWN_TOTAL_AREA_IDENTIFIERS = -1;
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	private int totalAreaIdentifiers;
	private String shapeFileDescription;
	private String areaIdentifierFieldName;
	private String nameFieldName;
	private ArrayList<String> shapeFileFieldNames;
	private HashMap<ShapeFileComponent, String> filePathForShapeFileComponent;
	
	private String projection;
	// ==========================================
	// Section Construction
	// ==========================================

	private ShapeFile() {
		filePathForShapeFileComponent 
			= new HashMap<ShapeFileComponent, String>();
		
		totalAreaIdentifiers = UNKNOWN_TOTAL_AREA_IDENTIFIERS;
		shapeFileDescription = "";
				
		shapeFileFieldNames = new ArrayList<String>();
		areaIdentifierFieldName = "";
		nameFieldName = "";
		
		projection = "";
	}

	public static ShapeFile newInstance() {
		ShapeFile shapeFile = new ShapeFile();
		return shapeFile;
	}

	public static ShapeFile createCopy(final ShapeFile originalShapeFile) {
		if (originalShapeFile == null) {
			return null;
		}
		
		ShapeFile cloneShapeFile = new ShapeFile();
		
		copyInto(originalShapeFile, cloneShapeFile);
		
		return cloneShapeFile;
	}
	
	public static void copyInto(
		final ShapeFile sourceShapeFile,
		final ShapeFile destinationShapeFile) {
		
		destinationShapeFile.setTotalAreaIdentifiers(
			sourceShapeFile.getTotalAreaIdentifiers());
		destinationShapeFile.setShapeFileDescription(
			sourceShapeFile.getShapeFileDescription());
		destinationShapeFile.setAreaIdentifierFieldName(
			sourceShapeFile.getAreaIdentifierFieldName());
		destinationShapeFile.setNameFieldName(
			sourceShapeFile.getNameFieldName());
		destinationShapeFile.setProjection(
			sourceShapeFile.getProjection());
		ArrayList<String> originalShapeFileFieldNames
			= sourceShapeFile.getShapeFileFieldNames();
		destinationShapeFile.clearShapeFileFieldNames();
		for (String originalShapeFileFieldName : originalShapeFileFieldNames) {
			destinationShapeFile.addShapeFileFieldName(originalShapeFileFieldName);
		}
			
		ArrayList<String> originalShapeFileComponentPaths
			= sourceShapeFile.getShapeFileComponentPaths();
		for (String originalShapeFileComponentPath : originalShapeFileComponentPaths) {
			destinationShapeFile.addShapeFileComponentPath(originalShapeFileComponentPath);
		}		
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public int getTotalAreaIdentifiers() {
		return totalAreaIdentifiers;
	}
	
	public void setTotalAreaIdentifiers(final int totalAreaIdentifiers) {
		this.totalAreaIdentifiers = totalAreaIdentifiers;
	}
	
	public String getProjection() {
		return projection;
	}
	
	public void setProjection(final String projection) {
		this.projection = projection;
	}
	
	public static boolean hasIdenticalContents(
		final ArrayList<ShapeFile> shapeFileListA,
		final ArrayList<ShapeFile> shapeFileListB) {
		
		if (shapeFileListA == shapeFileListB) {
			//they point to the same place in memory
			return true;
		}
		
		if ((shapeFileListA != null) && (shapeFileListB == null) ||
			(shapeFileListA == null) && (shapeFileListB != null)){
			return false;
		}
		
		if (shapeFileListA.size() != shapeFileListB.size()) {
			return false;
		}
		
		HashMap<String, ShapeFile> shapeFileFromBaseNameA 
			= new HashMap<String, ShapeFile>();
		for (ShapeFile shapeFile : shapeFileListA) {
			shapeFileFromBaseNameA.put(shapeFile.getBaseFilePath(), shapeFile);
		}
		
		HashMap<String, ShapeFile> shapeFileFromBaseNameB 
			= new HashMap<String, ShapeFile>();
		for (ShapeFile shapeFile : shapeFileListB) {
			shapeFileFromBaseNameB.put(shapeFile.getBaseFilePath(), shapeFile);
		}
		
		ArrayList<String> keysListA = new ArrayList<String>();
		keysListA.addAll(shapeFileFromBaseNameA.keySet());
		for (String keyA : keysListA) {
			ShapeFile shapeFileB
				= shapeFileFromBaseNameB.get(keyA);
			if (shapeFileB == null) {
				return false;
			}
			
			ShapeFile shapeFileA
				= shapeFileFromBaseNameA.get(keyA);
			if (shapeFileA.hasIdenticalContents(shapeFileB) == false) {
				return false;
			}
		}
		
		
		ArrayList<String> keysListB = new ArrayList<String>();
		keysListB.addAll(shapeFileFromBaseNameB.keySet());
		for (String keyB : keysListB) {
			ShapeFile shapeFileA
				= shapeFileFromBaseNameB.get(keyB);
			if (shapeFileA == null) {
				return false;
			}
			
			ShapeFile shapeFileB
				= shapeFileFromBaseNameA.get(keyB);
			if (shapeFileA.hasIdenticalContents(shapeFileB) == false) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean hasIdenticalContents(final ShapeFile otherShapeFile) {
	
		int otherTotalAreaIdentifiers
			= otherShapeFile.getTotalAreaIdentifiers();
		String otherShapeFileDescription
			= otherShapeFile.getShapeFileDescription();
		String otherAreaIdentifierFieldName
			= otherShapeFile.getAreaIdentifierFieldName();
		String otherNameFieldName
			= otherShapeFile.getNameFieldName();
		String otherProjection
			= otherShapeFile.getProjection();
		if (totalAreaIdentifiers != otherTotalAreaIdentifiers) {
			return false;
		}
				
		if (Objects.deepEquals(
			shapeFileDescription, 
			otherShapeFileDescription) == false) {

			return false;
		}
		if (Objects.deepEquals(
			areaIdentifierFieldName, 
			otherAreaIdentifierFieldName) == false) {

			return false;
		}
		
		if (Objects.deepEquals(
			nameFieldName, 
			otherNameFieldName) == false) {

			return false;
		}

		if (Objects.deepEquals(
			projection, 
			otherProjection) == false) {

			return false;
		}
		
		ArrayList<String> otherShapeFileFieldNames
			= otherShapeFile.getShapeFileFieldNames();
		if (shapeFileFieldNames.size() != otherShapeFileFieldNames.size()) {
			return false;
		}
		
		for (int i = 0; i < shapeFileFieldNames.size(); i++) {
			if (shapeFileFieldNames.get(i).equals(otherShapeFileFieldNames.get(i)) == false) {
				return false;
			}
		}

		ArrayList<String> shapeFilePathComponents
			= getShapeFileComponentPaths();
		ArrayList<String> otherShapeFilePathComponents
			= otherShapeFile.getShapeFileComponentPaths();
		if (shapeFilePathComponents.size() != otherShapeFilePathComponents.size()) {
			return false;
		}
		
		for (int i = 0; i < shapeFilePathComponents.size(); i++) {
			if (shapeFilePathComponents.get(i).equals(otherShapeFilePathComponents.get(i)) == false) {
				return false;
			}
		}
		
		return true;
		
	}
		
	public String getShapeFileDescription() {
		return shapeFileDescription;
	}

	public void setShapeFileDescription(String shapeFileDescription) {
		this.shapeFileDescription = shapeFileDescription;
	}

	public ArrayList<String> getShapeFileFieldNames() {
		return shapeFileFieldNames;
	}

	public void addShapeFileFieldName(final String shapeFileFieldName) {
		shapeFileFieldNames.add(shapeFileFieldName);
	}
		
	public void setShapeFileFieldNames(final ArrayList<String> shapeFileFieldNames) {
		this.shapeFileFieldNames = shapeFileFieldNames;
	}
	
	public void clearShapeFileFieldNames() {
		shapeFileFieldNames.clear();
	}
	
	public String getDatabaseFieldName() {
		String result 
			= shapeFileDescription.toLowerCase();
		result
			= result.replaceAll(" ", "_");
		return result;
	}
	
	public String getAreaIdentifierFieldName() {
		return areaIdentifierFieldName;
	}

	public void setAreaIdentifierFieldName(String areaIdentifierFieldName) {
		this.areaIdentifierFieldName = areaIdentifierFieldName;
	}

	public String getNameFieldName() {
		return nameFieldName;
	}

	public void setNameFieldName(String nameFieldName) {
		this.nameFieldName = nameFieldName;
	}
	
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
		
		String recordName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.name.label");
		String shapeFileDescriptionFieldLabel
			= RIFDataLoaderToolMessages.getMessage("shapeFile.description.label");		
		String areaIdentifierFieldLabel
			= RIFDataLoaderToolMessages.getMessage("shapeFile.areaIdentifierFieldName.label");
		String nameFieldLabel
			= RIFDataLoaderToolMessages.getMessage("shapeFile.nameFieldName.label");
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(shapeFileDescription)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					shapeFileDescriptionFieldLabel);
			errorMessages.add(errorMessage);			
		}
		
		if (fieldValidationUtility.isEmpty(areaIdentifierFieldName)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					areaIdentifierFieldLabel);
			errorMessages.add(errorMessage);			
		}
		
		if (fieldValidationUtility.isEmpty(nameFieldName)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					nameFieldLabel);
			errorMessages.add(errorMessage);
		}
		
		String shpFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.shp.label");
		String shxFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.shx.label");
		String dbfFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.dbf.label");
		String prjFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.prj.label");
		/*
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
		*/

		if (fieldValidationUtility.isEmpty(nameFieldName) == false) {
			if (Objects.deepEquals(nameFieldName, areaIdentifierFieldName)) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage("shapeFile.error.identicalNameAndAreaIDFields");
				errorMessages.add(errorMessage);
			}
		}
		
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

		/*
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
		*/
		
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
		else {			
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
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		
		
	}
	
	public void print() {
		StringBuilder buffer = new StringBuilder();

		buffer.append("Description:" + shapeFileDescription + "==\n");
		buffer.append("Total Area Identifiers:" + totalAreaIdentifiers + "==\n");
		buffer.append("Area Identifier:" + areaIdentifierFieldName + "==\n");
		buffer.append("Name Field:" + nameFieldName + "==\n");

		ArrayList<ShapeFileComponent> shapeFileComponents
			= new ArrayList<ShapeFileComponent>(filePathForShapeFileComponent.keySet());
		for (ShapeFileComponent shapeFileComponent : shapeFileComponents) {
			buffer.append(shapeFileComponent.getFileExtension());
			buffer.append(" : ");
			buffer.append(filePathForShapeFileComponent.get(shapeFileComponent));
			buffer.append("\n");
		}

		buffer.append("Shape file fields:\n");
		for (String shapeFileFieldName : shapeFileFieldNames) {
			buffer.append(shapeFileFieldName);
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


