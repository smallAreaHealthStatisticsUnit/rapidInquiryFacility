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
 * <li><b>coreTableName</b>: the base name that will be used in the names of all temporary
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

public class DataSource extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String coreTableName;
	private Boolean derivedFromExistingTable;
	private String sourceName;
	private String userID;
	private Date registrationDate;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DataSource() {

	}

	
	public static DataSource newInstance() {
		DataSource dataSource = new DataSource();
		return dataSource;		
	}
	
	public static DataSource newInstance(
		final String coreTableName,
		final Boolean derivedFromExistingTable,
		final String sourceName,
		final String userID) {
		
		DataSource dataSource = new DataSource();
		dataSource.setCoreTableName(coreTableName);
		dataSource.setDerivedFromExistingTable(derivedFromExistingTable);
		dataSource.setSourceName(sourceName);
		dataSource.setUserID(userID);
		
		return dataSource;
	}
	
	public static DataSource createCopy(
		final DataSource originalDataSource) {
		
		if (originalDataSource == null) {
			return null;
		}
		
		DataSource cloneDataSource = new DataSource();
		cloneDataSource.setIdentifier(originalDataSource.getIdentifier());
		cloneDataSource.setCoreTableName(originalDataSource.getCoreTableName());
		cloneDataSource.setDerivedFromExistingTable(originalDataSource.isDerivedFromExistingTable());
		Date originalRegistrationDate
			= originalDataSource.getRegistrationDate();
		if (originalRegistrationDate == null) {
			cloneDataSource.setRegistrationDate(null);
		}
		else {
			Date cloneRegistrationDate
				= new Date(originalRegistrationDate.getTime());
			cloneDataSource.setRegistrationDate(cloneRegistrationDate);			
		}
		cloneDataSource.setSourceName(originalDataSource.getSourceName());
		cloneDataSource.setUserID(originalDataSource.getUserID());
				
		return cloneDataSource;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String getCoreTableName() {
		return coreTableName;
	}

	public void setCoreTableName(String coreTableName) {
		this.coreTableName = coreTableName;
	}

	public boolean isDerivedFromExistingTable() {
		return derivedFromExistingTable;
	}

	public void setDerivedFromExistingTable(boolean derivedFromExistingTable) {
		this.derivedFromExistingTable = derivedFromExistingTable;
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
			= RIFDataLoaderToolMessages.getMessage("dataSource.label");
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
				
		if (coreTableName != null) {
			String codeFieldName
				= RIFDataLoaderToolMessages.getMessage("dataSource.coreTableName.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				codeFieldName,
				coreTableName);
		}

		if (sourceName != null) {
			String sourceNameFieldName
				= RIFDataLoaderToolMessages.getMessage("dataSource.sourceName.label");
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				sourceNameFieldName,
				sourceName);			
		}

		if (userID != null) {
			String userIDFieldName
				= RIFDataLoaderToolMessages.getMessage("dataSource.userID.label");
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
		
		String coreTableNameFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSource.coreTableName.label");		
		String derivedFromExistingTableFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSource.derivedFromExistingTable.label");
		String sourceNameFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSource.sourceName.label");
		String registrationDateFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSource.registrationDate.label");
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(coreTableName)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					coreTableNameFieldName);
			errorMessages.add(errorMessage);			
		}
		
		if (derivedFromExistingTable == null) {
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
		return coreTableName;
	}
	
}


