package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.util.FieldValidationUtility;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

public class DataLoaderToolGeography 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String name;
	private ArrayList<ShapeFile> shapeFiles;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DataLoaderToolGeography() {
		name = "";
		shapeFiles = new ArrayList<ShapeFile>();
	}

	public static DataLoaderToolGeography newInstance() {
		DataLoaderToolGeography geography
			= new DataLoaderToolGeography();
		return geography;
	}

	public static final DataLoaderToolGeography createCopy(
		final DataLoaderToolGeography originalGeography) {
		
		DataLoaderToolGeography cloneGeography
			= DataLoaderToolGeography.newInstance();
		copyInto(
			originalGeography, 
			cloneGeography);
		
		return cloneGeography;
	}
	
	public static final void copyInto(
		final DataLoaderToolGeography source,
		final DataLoaderToolGeography destination) {
		
		destination.setIdentifier(source.getIdentifier());
		destination.setName(source.getName());
		
		ArrayList<ShapeFile> sourceShapeFiles
			= source.getShapeFiles();
		destination.clearShapeFiles();
		for (ShapeFile sourceShapeFile : sourceShapeFiles) {
			ShapeFile cloneShapeFile
				= ShapeFile.createCopy(sourceShapeFile);
			destination.addShapeFile(cloneShapeFile);
		}
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ArrayList<ShapeFile> getShapeFiles() {
		return shapeFiles;
	}

	public void setShapeFiles(final ArrayList<ShapeFile> shapeFiles) {
		this.shapeFiles = shapeFiles;
	}

	public void addAllShapeFiles(final ArrayList<ShapeFile> shapeFilesToAdd) {
		shapeFiles.addAll(shapeFilesToAdd);
	}
	
	public void addShapeFile(final ShapeFile shapeFile) {
		shapeFiles.add(shapeFile);
	}
	
	public void clearShapeFiles() {
		shapeFiles.clear();
	}
	
	public static final boolean hasIdenticalContents(
		final DataLoaderToolGeography geographyA,
		final DataLoaderToolGeography geographyB) {
		
		if (geographyA == geographyB) {
			return true;
		}
		
		if ((geographyA == null) && (geographyB != null) ||
			(geographyA != null) && (geographyB == null)) {
			return false;
		}

		
		if (Objects.deepEquals(
			geographyA.getIdentifier(), 
			geographyB.getIdentifier()) == false) {

			return false;
		}
		
		if (Objects.deepEquals(
			geographyA.getName(), 
			geographyB.getName()) == false) {

			return false;
		}

		ArrayList<ShapeFile> shapeFilesA
			= geographyA.getShapeFiles();
		ArrayList<ShapeFile> shapeFilesB
			= geographyB.getShapeFiles();
		return ShapeFile.hasIdenticalContents(
			shapeFilesA, 
			shapeFilesB);

	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void checkErrors()
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordName
			= RIFDataLoaderToolMessages.getMessage(
				"dataLoaderToolGeography.singular.label");
		
				
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String nameFieldName
			= RIFDataLoaderToolMessages.getMessage("dataLoaderToolGeography.name.label");
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					nameFieldName);
			errorMessages.add(errorMessage);
		}
		
		String shapeFilesFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.name.plural.label");		
		if (shapeFiles.isEmpty()) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					shapeFilesFieldName);
			errorMessages.add(errorMessage);
		}

		countErrors(
			RIFDataLoaderToolError.INVALID_DATA_LOADER_GEOGRAPHY, 
			errorMessages);
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	public String getDisplayName() {
		return name;
	}
	
}


