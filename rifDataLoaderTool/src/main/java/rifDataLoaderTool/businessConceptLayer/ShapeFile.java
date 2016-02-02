package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;

import rifServices.system.RIFServiceMessages;

import java.io.File;
import java.util.ArrayList;
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
	private String mainFilePath;
	private String indexFilePath;
	private String databaseTableFilePath;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private ShapeFile() {

	}

	public static ShapeFile newInstance() {
		ShapeFile shapeFile = new ShapeFile();
		return shapeFile;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getMainFilePath() {
		return mainFilePath;
	}

	public void setMainFilePath(final String mainFilePath) {
		this.mainFilePath = mainFilePath;
	}

	public String getIndexFilePath() {
		return indexFilePath;
	}

	public void setIndexFilePath(final String indexFilePath) {
		this.indexFilePath = indexFilePath;
	}

	public String getDatabaseTableFilePath() {
		return databaseTableFilePath;
	}

	public void setDatabaseTableFilePath(final String databaseTableFilePath) {
		this.databaseTableFilePath = databaseTableFilePath;
	}
	
	
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
		
		String mainFileFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.mainFile.label");
		String indexFileFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.indexFile.label");
		String databaseTableFieldName
			= RIFDataLoaderToolMessages.getMessage("shapeFile.databaseTableFile.label");
	
		//check if any of them are null
		if (mainFilePath == null) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					mainFileFieldName);			
			errorMessages.add(errorMessage);		
		}
		File mainFile = new File(mainFilePath);
		if (mainFile.exists() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.nonExistentFile",
					mainFilePath);
			errorMessages.add(errorMessage);
		}
		else if (mainFile.canRead() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.unableToReadFile",
					mainFilePath);			
			errorMessages.add(errorMessage);
		}
	
		if (indexFilePath == null) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					indexFileFieldName);			
			errorMessages.add(errorMessage);		
		}
		File indexFile = new File(indexFilePath);
		if (indexFile.exists() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.nonExistentFile",
					indexFilePath);
			errorMessages.add(errorMessage);
		}
		else if (indexFile.canRead() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.unableToReadFile",
					indexFilePath);			
			errorMessages.add(errorMessage);
		}		
	
		if (databaseTableFilePath == null) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					databaseTableFieldName);			
			errorMessages.add(errorMessage);		
		}
		File databaseTableFile = new File(databaseTableFilePath);
		if (databaseTableFile.exists() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.nonExistentFile",
					databaseTableFilePath);
			errorMessages.add(errorMessage);
		}
		else if (databaseTableFile.canRead() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.io.unableToReadFile",
					databaseTableFilePath);			
			errorMessages.add(errorMessage);
		}		
			
		//Now check that they all have the same base name
		Collator collator = RIFServiceMessages.getCollator();
		String baseMainFileName 
			= extractBaseFileName(mainFilePath);
		String baseIndexFileName
			= extractBaseFileName(indexFilePath);
		String baseDatabaseTableFileName
			= extractBaseFileName(databaseTableFilePath);
		if (collator.equals(baseMainFileName, baseIndexFileName) == false ||
			collator.equals(baseMainFileName, baseDatabaseTableFileName) == false) {
		
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"shapeFile.error.invalidBaseShapeFileName");			
			errorMessages.add(errorMessage);
		}
		
		return errorMessages;
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: DisplayableListItemInterface
	public String getDisplayName() {
		String shapeFileName
			= extractBaseFileName(mainFilePath);
		return shapeFileName;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


