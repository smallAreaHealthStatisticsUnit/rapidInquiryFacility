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
 * This is the concept of a Geography as the Data Loader Tool would understand
 * it, rather than the {@link rifServices.businessConceptLayer.Geography}
 * concept that is used within the web-based epidemiology applications.
 * <p>
 * Here, a geography has a name (eg: England) and then a collection of shape
 * files which cover the same area but at different resolutions (eg: England
 * districts, wards, regions).
 * </p>
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

public class DLGeography 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String name;
	private String filePath;	
	private ArrayList<DLGeographicalResolutionLevel> levels;
			
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DLGeography() {
		name = "";
		filePath = "";
		levels = new ArrayList<DLGeographicalResolutionLevel>();
	}

	public static DLGeography newInstance() {
		DLGeography geography
			= new DLGeography();
		return geography;
	}

	public static final DLGeography createCopy(
		final DLGeography originalGeography) {
		
		DLGeography cloneGeography
			= DLGeography.newInstance();
		copyInto(
			originalGeography, 
			cloneGeography);
		
		return cloneGeography;
	}
	
	public static final void copyInto(
		final DLGeography source,
		final DLGeography destination) {
		
		destination.setIdentifier(source.getIdentifier());
		destination.setName(source.getName());
		destination.setFilePath(source.getFilePath());
		
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

	public void setFilePath(final String filePath) {
		this.filePath = filePath;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public ArrayList<DLGeographicalResolutionLevel> getLevels() {
		return levels;
	}
	
	public void addLevel(
		final DLGeographicalResolutionLevel level) {
		
		levels.add(level);
	}
	
	public void clearLevels() {
		levels.clear();
	}
	
	public static final boolean hasIdenticalContents(
		final DLGeography geographyA,
		final DLGeography geographyB) {
		
		if (geographyA == geographyB) {
			return true;
		}
		
		if ((geographyA == null) && (geographyB != null) ||
			(geographyA != null) && (geographyB == null)) {
			return false;
		}

		
		if (Objects.deepEquals(
			geographyA.getName(), 
			geographyB.getName()) == false) {

			return false;
		}
		
		
		if (Objects.deepEquals(
			geographyA.getFilePath(), 
			geographyB.getFilePath()) == false) {

			return false;
		}

		return true;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void checkErrors()
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordName
			= RIFDataLoaderToolMessages.getMessage(
				"dlGeography.singular.label");
		
				
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String nameFieldName
			= RIFDataLoaderToolMessages.getMessage("dlGeography.name.label");
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					nameFieldName);
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


