package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderActivityStep;


import rifServices.system.RIFServiceMessages;

import java.util.Date;
import java.util.ArrayList;


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

public class DBTConfigurationRecord {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String tableConfigurationName;
	private String tableConfigurationDescription;
	private Date lastModifiedDate;
	private RIFDataLoaderActivityStep rifDataLoaderActivityStep;
	
	private ArrayList<DBFConfigurationRecord> fieldMetaDataRecords;
	
	private boolean countCleanedRows;
	private boolean countDuplicateRows;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DBTConfigurationRecord() {
		fieldMetaDataRecords = new ArrayList<DBFConfigurationRecord>();
	}

	public static DBTConfigurationRecord newInstance() {

		DBTConfigurationRecord dbtMetaDataRecord = new DBTConfigurationRecord();
		return dbtMetaDataRecord;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String getTableName() {

		return tableConfigurationName;
	}

	public void setTableConfigurationName(
		final String tableConfigurationName) {

		this.tableConfigurationName = tableConfigurationName;
	}

	public String getTableDescription() {

		return tableConfigurationDescription;
	}

	public void setTableConfigurationDescription(
		final String tableConfigurationDescription) {
		
		this.tableConfigurationDescription = tableConfigurationDescription;
	}

	
	public String getDatePhrase() {
		return RIFServiceMessages.getDatePhrase(lastModifiedDate);
	}
	
	public String getTimePhrase() {
		return RIFServiceMessages.getTimePhrase(lastModifiedDate);
	}
	
	public Date getLastModifiedDate() {

		return lastModifiedDate;
	}

	public String getLastModifiedDatePhrase() {
		
		return RIFServiceMessages.getDatePhrase(lastModifiedDate);
	}
	
	public void setLastModifiedDate(
		final Date lastModifiedDate) {

		this.lastModifiedDate = lastModifiedDate;
	}

	public RIFDataLoaderActivityStep getRIFDataLoaderActivityStep() {

		return rifDataLoaderActivityStep;
	}

	public void setRifDataLoaderActivityStep(
		final RIFDataLoaderActivityStep rifDataLoaderActivityStep) {

		this.rifDataLoaderActivityStep = rifDataLoaderActivityStep;
	}

	public ArrayList<DBFConfigurationRecord> getFieldMetaDataRecords() {

		return fieldMetaDataRecords;
	}

	public void setFieldMetaDataRecords(
		final ArrayList<DBFConfigurationRecord> fieldMetaDataRecords) {

		this.fieldMetaDataRecords = fieldMetaDataRecords;
	}

	public void addFieldMetaDataRecord(
		final DBFConfigurationRecord fieldMetaDataRecord) {
		
		fieldMetaDataRecords.add(fieldMetaDataRecord);
	}
	
	public DBTConfigurationRecord createCopy(
		final DBTConfigurationRecord originalDBTMetaDataRecord) {
		
		//cloning the table-level meta data
		DBTConfigurationRecord cloneDBTMetaDataRecord 
			= new DBTConfigurationRecord();
		cloneDBTMetaDataRecord.setTableConfigurationName(originalDBTMetaDataRecord.getTableName());
		cloneDBTMetaDataRecord.setTableConfigurationDescription(originalDBTMetaDataRecord.getTableDescription());
		cloneDBTMetaDataRecord.setLastModifiedDate(originalDBTMetaDataRecord.getLastModifiedDate());

		//cloning the field-level meta data
		ArrayList<DBFConfigurationRecord> originalDBFMetaDataRecords
			= cloneDBTMetaDataRecord.getFieldMetaDataRecords();
		ArrayList<DBFConfigurationRecord> cloneDBFMetaDataRecords
			= new ArrayList<DBFConfigurationRecord>();
		for (DBFConfigurationRecord originalDBFMetaDataRecord : originalDBFMetaDataRecords) {
			DBFConfigurationRecord cloneDBFMetaDataRecord
				= DBFConfigurationRecord.createCopy(originalDBFMetaDataRecord);
			cloneDBFMetaDataRecords.add(cloneDBFMetaDataRecord);
		}
	
		cloneDBTMetaDataRecord.setFieldMetaDataRecords(cloneDBFMetaDataRecords);
		
		return cloneDBTMetaDataRecord;
	}

	public boolean countCleanedRows() {
		
		return countCleanedRows;
	}
	
	public void setCountCleanedRows(
		final boolean countCleanedRows) {
		
		this.countCleanedRows = countCleanedRows;
	}

	public boolean countDuplicates() {
		
		return countDuplicateRows;
	}
	
	public void setCountDuplicateRows(
		final boolean countDuplicateRows) {
		
		this.countDuplicateRows = countDuplicateRows;
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


