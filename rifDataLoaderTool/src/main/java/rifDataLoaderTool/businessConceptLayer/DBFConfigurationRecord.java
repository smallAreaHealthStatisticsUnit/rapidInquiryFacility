package rifDataLoaderTool.businessConceptLayer;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public class DBFConfigurationRecord {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String originalFieldName;
	private String cleanedFieldName;
	private RIFDataType rifDataType;
	private boolean countEmptyValues;
	private boolean checkForUnknownValues;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DBFConfigurationRecord() {
		countEmptyValues = false;
		checkForUnknownValues = false;
	}

	public static DBFConfigurationRecord newInstance() {

		DBFConfigurationRecord dbfMetaDataRecord = new DBFConfigurationRecord();
		return dbfMetaDataRecord;
	}
	
	public static DBFConfigurationRecord createCopy(
		final DBFConfigurationRecord originalDBFMetaDataRecord) {
		
		DBFConfigurationRecord cloneDBFMetaDataRecord
			= new DBFConfigurationRecord();
		
		return cloneDBFMetaDataRecord;
	}

	public String getOriginalFieldName() {
		
		return originalFieldName;
	}

	public void setOriginalFieldName(
		final String originalFieldName) {

		this.originalFieldName = originalFieldName;
	}

	public String getCleanedFieldName() {

		return cleanedFieldName;
	}

	public void setCleanedFieldName(
		final String cleanedFieldName) {

		this.cleanedFieldName = cleanedFieldName;
	}

	public RIFDataType getDataType() {

		return rifDataType;
	}

	public void setDataType(
		final RIFDataType rifDataType) {

		this.rifDataType = rifDataType;
	}
	
	public boolean countEmptyValues() {

		return countEmptyValues;
	}
	
	public boolean checkForUnknownValues() {
		
		return checkForUnknownValues;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
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


