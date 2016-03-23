package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.FieldValidationUtility;

import java.util.ArrayList;

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

	public void addShapeFile(final ShapeFile shapeFile) {
		shapeFiles.add(shapeFile);
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
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					nameFieldName);
			errorMessages.add(errorMessage);
		}
		
		String shapeFilesFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.name.plural.label");		
		if (shapeFiles.isEmpty()) {
			String errorMessage
				= RIFServiceMessages.getMessage(
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


