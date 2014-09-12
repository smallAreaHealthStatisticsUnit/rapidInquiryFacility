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

public class DBTMetaDataRecord {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String tableName;
	private String tableDescription;
	private Date lastModifiedDate;
	private RIFDataLoaderActivityStep rifDataLoaderActivityStep;
	
	private ArrayList<DBFMetaDataRecord> fieldMetaDataRecords;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DBTMetaDataRecord() {
		fieldMetaDataRecords = new ArrayList<DBFMetaDataRecord>();
	}

	public static DBTMetaDataRecord newInstance() {

		DBTMetaDataRecord dbtMetaDataRecord = new DBTMetaDataRecord();
		return dbtMetaDataRecord;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String getTableName() {

		return tableName;
	}

	public void setTableName(
		final String tableName) {

		this.tableName = tableName;
	}

	public String getTableDescription() {

		return tableDescription;
	}

	public void setTableDescription(
		final String tableDescription) {
		
		this.tableDescription = tableDescription;
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

	public ArrayList<DBFMetaDataRecord> getFieldMetaDataRecords() {

		return fieldMetaDataRecords;
	}

	public void setFieldMetaDataRecords(
		final ArrayList<DBFMetaDataRecord> fieldMetaDataRecords) {

		this.fieldMetaDataRecords = fieldMetaDataRecords;
	}

	public void addFieldMetaDataRecord(
		final DBFMetaDataRecord fieldMetaDataRecord) {
		
		fieldMetaDataRecords.add(fieldMetaDataRecord);
	}
	
	public DBTMetaDataRecord createCopy(
		final DBTMetaDataRecord originalDBTMetaDataRecord) {
		
		//cloning the table-level meta data
		DBTMetaDataRecord cloneDBTMetaDataRecord 
			= new DBTMetaDataRecord();
		cloneDBTMetaDataRecord.setTableName(originalDBTMetaDataRecord.getTableName());
		cloneDBTMetaDataRecord.setTableDescription(originalDBTMetaDataRecord.getTableDescription());
		cloneDBTMetaDataRecord.setLastModifiedDate(originalDBTMetaDataRecord.getLastModifiedDate());

		//cloning the field-level meta data
		ArrayList<DBFMetaDataRecord> originalDBFMetaDataRecords
			= cloneDBTMetaDataRecord.getFieldMetaDataRecords();
		ArrayList<DBFMetaDataRecord> cloneDBFMetaDataRecords
			= new ArrayList<DBFMetaDataRecord>();
		for (DBFMetaDataRecord originalDBFMetaDataRecord : originalDBFMetaDataRecords) {
			DBFMetaDataRecord cloneDBFMetaDataRecord
				= DBFMetaDataRecord.createCopy(originalDBFMetaDataRecord);
			cloneDBFMetaDataRecords.add(cloneDBFMetaDataRecord);
		}
	
		cloneDBTMetaDataRecord.setFieldMetaDataRecords(cloneDBFMetaDataRecords);
		
		return cloneDBTMetaDataRecord;
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


