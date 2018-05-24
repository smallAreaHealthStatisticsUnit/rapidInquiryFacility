package org.sahsu.rif.dataloader.datastorage.ms;

import java.sql.Connection;

import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 * Performs a number of utility operations that involve getting various 
 * kinds of information about the schema.  For example, it will retrieve
 * the names of functions used for cleaning or validation.  These are 
 * important for when users are able to define their own data types and
 * specify database functions to do various activities.  It should also
 * have a method to retrieve a frequency breakdown of values so that 
 * RIF managers may check whether they have designed cleaning and validation
 * rules to anticipate all the different types of values thay have in an
 * imported data set.
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class MSSQLDatabaseSchemaInformationManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLDatabaseSchemaInformationManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public String[][] getVarianceInFieldData(
		final Connection connection,
		final DataSetFieldConfiguration _dataSetFieldConfiguration)
		throws RIFServiceException {
		
		
/*		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
		DataSetFieldConfiguration dataSetFieldConfiguration
			=dataSetConfiguration.getFieldHavingConvertFieldName(fieldName);

		String[][] results = new String[0][0];
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getVarianceInFieldData",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"getVarianceInFieldData",
				"dataSetFieldConfiguration",
				dataSetFieldConfiguration);
			
			//Check for security violations
			validateUser(rifManager);

			String coreDataSetName
				= dataSetFieldConfiguration.getCoreFieldName()
			String loadTableName
				= RIFTemporaryTablePrefixes.LOAD.getTableName(coreDataSetName);
			String fieldOfInterest
				= tableFieldCleaningConfiguration.getLoadTableFieldName();
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getVarianceInFieldData",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					loadTableName,
					fieldOfInterest);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);

			results
				= cleanWorkflowManager.getVarianceInFieldData(
					connection, 
					tableFieldCleaningConfiguration);
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"cleaningDetectedErrorValue",
				rifServiceException);
		}		

		return results;
*/
		
		return null;
	}
	
	public String getFunctionDescription(
		final Connection connection, 
		final String functionName) 
		throws RIFServiceException {
				
		return "Description of " + functionName;		
	}

	public String[] getCleaningFunctionNames(
		final Connection connection)
		throws RIFServiceException {
	
		String[] validationFunctionNames = new String[4];
		validationFunctionNames[0] = "clean_age";
		validationFunctionNames[1] = "clean_date";
		validationFunctionNames[2] = "clean_uk_postal_code";
		validationFunctionNames[3] = "clean_year";
		return validationFunctionNames;
		
		//return getDatabaseFunctionNames(_rifManager, "^clean.*");
	}	

	public String[] getValidationFunctionNames(
		final Connection connection) 
		throws RIFServiceException {

		//return getDatabaseFunctionNames(_rifManager, "^is_valid.*");

		//@TODO: make this come from the database
		String[] validationFunctionNames = new String[4];
		validationFunctionNames[0] = "is_valid_uk_postal_code";
		//validationFunctionNames[1] = "is_valid_icd_code";
		//validationFunctionNames[2] = "is_valid_sex";
		//validationFunctionNames[3] = "is_valid_age";
		return validationFunctionNames;
	}	
	
	/*	
	public String[] getDatabaseFunctionNames(
		final User _rifManager,
		final String functionNamePattern) 
		throws RIFServiceException {

		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);

		if (sqlConnectionManager == null) {
			System.out.println("getDBFunctionNames 1");
		}
		else if (rifManager == null) {
			System.out.println("getDBFunctionNames 2");
		}
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return new String[0];
		}
		
		ArrayList<String> results = new ArrayList<String>();
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		try {
			SQLGeneralQueryFormatter queryFormatter 
				= new SQLGeneralQueryFormatter();
			queryFormatter.addQueryLine(0, "SELECT");
			queryFormatter.addQueryLine(1, "pg_proc.proname AS function_name");
			queryFormatter.addQueryLine(0, "FROM");
			queryFormatter.addQueryLine(1, "pg_proc");
			queryFormatter.addQueryLine(0, "INNER JOIN pg_namespace ON ");
			queryFormatter.addQueryLine(1, "pg_proc.pronamespace = pg_namespace.oid");
			queryFormatter.addQueryLine(0, "WHERE");
			queryFormatter.addQueryPhrase(1, "pg_namespace = 'public' AND ");
			queryFormatter.addQueryPhrase("pg_proc.proname ~ ? ");
			queryFormatter.padAndFinishLine();
			queryFormatter.addQueryLine(0, "ORDER BY");
			queryFormatter.addQueryLine(1, "pg_proc.proname");
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
			System.out.println("ADLS queryFormatter=="+queryFormatter.generateQuery()+"==");
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			String functionFilterExpression = "^clean.*";
			statement.setString(1, functionFilterExpression);
			
			
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				results.add(resultSet.getString(1));
			}
			
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			SQLQueryUtility.close(resultSet);
			SQLQueryUtility.close(statement);
		}
		
		return results.toArray(new String[0]);
		
		//@TODO: make this come from the database
		String[] cleaningFunctionNames = new String[5];
		cleaningFunctionNames[0] = "clean_uk_postal_code";
		cleaningFunctionNames[1] = "clean_icd_code";
		cleaningFunctionNames[2] = "clean_sex";
		cleaningFunctionNames[3] = "clean_age";
		cleaningFunctionNames[4] = "clean_year";
		
		return cleaningFunctionNames;
		
	}
*/
	
	
	
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


