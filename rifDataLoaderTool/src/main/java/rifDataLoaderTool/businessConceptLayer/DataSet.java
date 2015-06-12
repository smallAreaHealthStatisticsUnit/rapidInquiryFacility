package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;
import java.util.Date;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;
import rifServices.util.FieldValidationUtility;



/**
 * A data source that is used to populate part of the RIF database schema.  The
 * data source will typically be a text file that is either in a CSV or XML format.
 * Each data source has the following properties:
 * <ul>
 * <li><b>coreDataSetName</b>: the base name that will be used in the names of all temporary
 * and permanent tables that are associated with this data source </li>
 * <li><b>derivedFromExistingTable</b>: a field which can indicate whether the data source
 * was imported externally or if the source is an existing published table.
 * </li>
 * <li><b>sourceName</b>:will either be the name of an existing published RIF table or
 * it will be a file path for the input file that was used.
 * </li>
 * <li>
 * <b>userID</b>: the id of the person who loaded the data
 * </li>
 * <li>
 * <b>registrationDate</b>: the date the data were loaded.
 * </li>
 * </ul>
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

public final class DataSet extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String coreDataSetName;
	private Boolean derivedFromExistingDataSet;
	private String sourceName;
	private String userID;
	private Date registrationDate;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DataSet() {

	}

	
	public static DataSet newInstance() {
		DataSet dataSet = new DataSet();
		return dataSet;		
	}
	
	public static DataSet newInstance(
		final String coreDataSetName,
		final Boolean derivedFromExistingTable,
		final String sourceName,
		final String userID) {
		
		DataSet dataSet = new DataSet();
		dataSet.setCoreDataSetName(coreDataSetName);
		dataSet.setDerivedFromExistingTable(derivedFromExistingTable);
		dataSet.setSourceName(sourceName);
		dataSet.setUserID(userID);
		
		return dataSet;
	}
	
	public static DataSet createCopy(
		final DataSet originaldataSet) {
		
		if (originaldataSet == null) {
			return null;
		}
		
		DataSet clonedataSet = new DataSet();
		clonedataSet.setIdentifier(originaldataSet.getIdentifier());
		clonedataSet.setCoreDataSetName(originaldataSet.getCoreDataSetName());
		clonedataSet.setDerivedFromExistingTable(originaldataSet.isDerivedFromExistingTable());
		Date originalRegistrationDate
			= originaldataSet.getRegistrationDate();
		if (originalRegistrationDate == null) {
			clonedataSet.setRegistrationDate(null);
		}
		else {
			Date cloneRegistrationDate
				= new Date(originalRegistrationDate.getTime());
			clonedataSet.setRegistrationDate(cloneRegistrationDate);			
		}
		clonedataSet.setSourceName(originaldataSet.getSourceName());
		clonedataSet.setUserID(originaldataSet.getUserID());
				
		return clonedataSet;
	}
	
	public static ArrayList<DataSet> createCopy(
		final ArrayList<DataSet> originalDataSets) {
		
		ArrayList<DataSet> clonedataSets = new ArrayList<DataSet>();
		for (DataSet originalDataSet : originalDataSets) {
			DataSet clonedataSet = DataSet.createCopy(originalDataSet);
			clonedataSets.add(clonedataSet);
		}
		
		return clonedataSets;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String getCoreDataSetName() {
		return coreDataSetName;
	}

	public void setCoreDataSetName(String coreDataSetName) {
		this.coreDataSetName = coreDataSetName;
	}

	public boolean isDerivedFromExistingTable() {
		return derivedFromExistingDataSet;
	}

	public void setDerivedFromExistingTable(boolean derivedFromExistingTable) {
		this.derivedFromExistingDataSet = derivedFromExistingTable;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getRecordType() {
		String recordNameLabel
			= RIFDataLoaderToolMessages.getMessage("dataSet.label");
		return recordNameLabel;
	}
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType = getRecordType();
				
		if (coreDataSetName != null) {
			String codeFieldName
				= RIFDataLoaderToolMessages.getMessage("dataSet.coreDataSetName.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				codeFieldName,
				coreDataSetName);
		}

		if (sourceName != null) {
			String sourceNameFieldName
				= RIFDataLoaderToolMessages.getMessage("dataSet.sourceName.label");
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				sourceNameFieldName,
				sourceName);			
		}

		if (userID != null) {
			String userIDFieldName
				= RIFDataLoaderToolMessages.getMessage("dataSet.userID.label");
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				userIDFieldName,
				userID);		
		}
	}
	
	public void checkErrors() 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordName = getRecordType();
		
		String coreDataSetNameFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSet.coreDataSetName.label");		
		String derivedFromExistingTableFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSet.derivedFromExistingTable.label");
		String sourceNameFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSet.sourceName.label");
		String registrationDateFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSet.registrationDate.label");
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(coreDataSetName)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					coreDataSetNameFieldName);
			errorMessages.add(errorMessage);			
		}
		
		if (derivedFromExistingDataSet == null) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					derivedFromExistingTableFieldName);
			errorMessages.add(errorMessage);			
		}

		if (fieldValidationUtility.isEmpty(sourceName)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordName,
						sourceNameFieldName);
			errorMessages.add(errorMessage);			
		}	

		countErrors(
			RIFDataLoaderToolError.INVALID_DATA_SOURCE, 
			errorMessages);		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	
	public String getDisplayName() {
		return coreDataSetName;
	}
	
}


