package rifDataLoaderTool.dataStorageLayer;


import rifDataLoaderTool.system.*;
import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.dataStorageLayer.postgresql.*;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.AbstractRIFService;
import rifServices.system.RIFServiceException;
import rifServices.util.FieldValidationUtility;
import rifServices.util.RIFLogger;

import java.sql.*;

/**
 * Main implementation of the {@link rifDataLoaderTool.dataStorageLayer.DataLoaderService}.
 * Almost every method in this class has the following common steps:
 * <ol>
 * <li>
 * safely copy parameter values so that the middleware is not vulnerable to changes the client makes
 * to them while the method is executing.
 * </li>
 * <li>
 * if the user making the request has been black listed because of a previous security incident, return
 * as soon as possible.
 * </li>
 * <li>
 * check for any null parameter values
 * </li>
 * <li>
 * check for any security violations that could occur, either in the user parameter object,
 * or any text field value of any business object. If any security violation is detected, log it and
 * black list the user until further notice.
 * </li>
 * <li>
 * audit the attempt to perform the method operation
 * </li>
 * <li>
 * obtain a pooled database connection
 * <li>
 * <li>
 * call a manager class, which is either contains the needed SQL queries or delegates to vendor-specific
 * SQL code generation classes.
 * </li>
 * <li>
 * reclaim the pooled database connection
 * </li>
 * <li>
 * log any failed attempt
 * </li>
 * </ol>
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

public class DataLoaderService 
	extends AbstractRIFService
	implements DataLoaderServiceAPI {
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final int TEXT_COLUMN_WIDTH = 30;
	// ==========================================
	// Section Properties
	// ==========================================
	
	private SQLConnectionManager sqlConnectionManager;
	private RIFDataLoaderStartupOptions startupOptions;
	
	private DataSourceManager dataSourceManager;
	private LoadStepManager loadStepManager;
	private CleanStepManager cleanStepManager;
	private ConvertStepManager convertStepManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataLoaderService() {
		startupOptions = new RIFDataLoaderStartupOptions();
		sqlConnectionManager = new SQLConnectionManager();		
	}
	public void initialiseService() {
		if (startupOptions.getDatabaseType() == RIFDataLoaderStartupOptions.DatabaseType.POSTGRESQL) {
			
			dataSourceManager
				= new DataSourceManager();
			
			PostgresLoadStepQueryGenerator loadStepQueryGenerator
				= new PostgresLoadStepQueryGenerator();
			loadStepManager
				= new LoadStepManager(
					startupOptions, 
					loadStepQueryGenerator);
			
			PostgresCleaningStepQueryGenerator cleaningStepQueryGenerator
				= new PostgresCleaningStepQueryGenerator();
			cleanStepManager
				= new CleanStepManager(
					startupOptions,
					cleaningStepQueryGenerator);
			
			PostgresConvertStepQueryGenerator convertStepQueryGenerator
				= new PostgresConvertStepQueryGenerator();
			convertStepManager
				= new ConvertStepManager(
					startupOptions,
					convertStepQueryGenerator); 
		}
		else {
			
		}		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	

	
	
	public void clearAllDataSources(
		final User _user)  
		throws RIFServiceException,
		RIFDataLoaderToolException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}

		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"clearAllDataSources",
				"user",
				user);

			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.clearAllDataSources",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
		
			//Assign pooled connection
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			
			dataSourceManager.clearAllDataSources(connection);

			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"clearAllDataSources",
				rifServiceException);
		}		
	
	}	
		
	public void registerDataSource(
		final User _user,
		final DataSource _dataSource)
		throws RIFServiceException,
		RIFDataLoaderToolException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		DataSource dataSource 
			= DataSource.createCopy(_dataSource);

		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"registerDataSource",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"registerDataSource",
				"dataSource",
				dataSource);
			
			//sortingOrder can be null, it just means that the
			//order will be ascending lower limit
		
			//Check for security violations
			validateUser(user);
			dataSource.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.registerDataSource",
					user.getUserID(),
					user.getIPAddress(),
					dataSource.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			
			dataSourceManager.registerDataSource(
				connection, 
				dataSource);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printStackTrace(System.out);
			//Audit failure of operation
			logException(
				user,
				"registerDataSource",
				rifServiceException);
		}		
	}
		
	public DataSource getDataSourceFromCoreTableName(
		final User _user,
		final String coreTableName)
		throws RIFServiceException,
		RIFDataLoaderToolException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		
		DataSource result = null;
		try {
		
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getDataSourceFromCoreTableName",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getDataSourceFromCoreTableName",
				"coreTableName",
				coreTableName);
			
			
			//Check for security violations
			validateUser(user);
			fieldValidationUtility.checkMaliciousMethodParameter(
				"getDataSourceFromCoreTableName", 
				"coreTableName", 
				coreTableName);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getDataSourceFromCoreTableName",
					user.getUserID(),
					user.getIPAddress(),
					coreTableName);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			//Assign pooled connection
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			
			result
				= dataSourceManager.getDataSourceFromCoreTableName(
					connection,
					coreTableName);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getDataSourceFromCoreTableName",
				rifServiceException);
		}		
		
		return result;
		
	}
		
	public void loadConfiguration(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFDataLoaderToolException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);
		
		
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"loadConfiguration",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"loadConfiguration",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			
			//Check for security violations
			validateUser(user);
			tableCleaningConfiguration.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.loadConfiguration",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);
				
			loadStepManager.createLoadTable(
				connection, 
				tableCleaningConfiguration, 
				TEXT_COLUMN_WIDTH);

			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
		
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"loadConfiguration",
				rifServiceException);
		}		
	}
		
	public void addLoadTableData(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration,
		final String[][] tableData)
		throws RIFServiceException,
		RIFDataLoaderToolException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);
	
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"addLoadTableData",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"addLoadTableData",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			fieldValidationUtility.checkNullMethodParameter(
				"addLoadTableData",
				"tableData",
				tableData);
			
			//Check for security violations
			validateUser(user);
			tableCleaningConfiguration.checkSecurityViolations();
			
			//@TODO: we obviously need something to check the array of Strings
			//for security violations
			//checkTableDataForSecurityViolations(tableData)


			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.addLoadTableData",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);

			loadStepManager.addLoadTableData(
				connection, 
				tableCleaningConfiguration, 
				tableData);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"addLoadTableData",
				rifServiceException);
		}		

	}

	public RIFResultTable getLoadTableData(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFDataLoaderToolException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);

				
		RIFResultTable results = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getLoadTableData",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getLoadTableData",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getLoadTableData",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);
						
			results
				= loadStepManager.getLoadTableData(
					connection, 
					tableCleaningConfiguration);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getLoadTableData",
				rifServiceException);
		}		
		catch(RIFDataLoaderToolException dataLoaderToolException) {
			dataLoaderToolException.printStackTrace(System.out);
		}
		return results;
	}
		
	public void cleanConfiguration(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFDataLoaderToolException {
		
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {

			return;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);
		
		
		
		try {

			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"cleanConfiguration",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"cleanConfiguration",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.cleanConfiguration",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);
								
			cleanStepManager.createCleanedTable(
				connection, 
				tableCleaningConfiguration);
						
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"cleanConfiguration",
				rifServiceException);
		}		

	}
	
	public RIFResultTable getCleanedTableData(
		final User _user,			
		final TableCleaningConfiguration _tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFDataLoaderToolException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);
				
		RIFResultTable result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCleanedTableData",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getCleanedTableData",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getCleanedTableData",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);
								
			result
				= cleanStepManager.getCleanedTableData(
					connection, 
					tableCleaningConfiguration);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getCleanedTableData",
				rifServiceException);
		}		
	
		return result;

	}
	
	public void convertConfiguration(
		final User _user,
		final TableConversionConfiguration _tableConversionConfiguration)
		throws RIFServiceException,
		RIFDataLoaderToolException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return;
		}
		TableConversionConfiguration tableConversionConfiguration
			= TableConversionConfiguration.createCopy(_tableConversionConfiguration);
				
		RIFResultTable result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"convertConfiguration",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"convertConfiguration",
				"tableConversionConfiguration",
				tableConversionConfiguration);
						
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.convertConfiguration",
					user.getUserID(),
					user.getIPAddress(),
					tableConversionConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);
								
			convertStepManager.convertConfiguration(
				connection, 
				tableConversionConfiguration);
					
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"convertConfiguration",
				rifServiceException);
		}		
		
	}
	
	
	
	
	
	
	
	
	public Integer getCleaningTotalBlankValues(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration)
		throws RIFServiceException,
		RIFDataLoaderToolException {

		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);

		Integer result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalBlankValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalBlankValues",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getCleaningTotalBlankValues",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);
			result
				= cleanStepManager.getCleaningTotalBlankValues(
					connection, 
					tableCleaningConfiguration);
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getCleaningTotalBlankValues",
				rifServiceException);
		}		

		return result;
	}
		
	public Integer getCleaningTotalChangedValues(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration)
		throws RIFServiceException,
		RIFDataLoaderToolException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);

		Integer result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalChangedValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalChangedValues",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getCleaningTotalChangedValues",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);
			result
				= cleanStepManager.getCleaningTotalChangedValues(
					connection, 
					tableCleaningConfiguration);
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getCleaningTotalBlankValues",
				rifServiceException);
		}		

		return result;
	}
		
	public Integer getCleaningTotalErrorValues(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration)
		throws RIFServiceException,
		RIFDataLoaderToolException {

		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);

		Integer result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalErrorValues",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalErrorValues",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getCleaningTotalErrorValues",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);
			result
				= cleanStepManager.getCleaningTotalErrorValues(
					connection, 
					tableCleaningConfiguration);
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"getCleaningTotalErrorValues",
				rifServiceException);
		}		

		return result;
	}
	
	public Boolean cleaningDetectedBlankValue(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFDataLoaderToolException {

		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);

		Boolean result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedBlankValue",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedBlankValue",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);

			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedBlankValue",
				"targetBaseFieldName",
				targetBaseFieldName);
					
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage(
					"logging.cleaningDetectedBlankValue",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName(),
					targetBaseFieldName, 
					String.valueOf(rowNumber));
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);

			result
				= cleanStepManager.cleaningDetectedBlankValue(
					connection, 
					tableCleaningConfiguration,
					rowNumber,
					targetBaseFieldName);
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"cleaningDetectedBlankValue",
				rifServiceException);
		}		

		return result;		
	}
	
	public Boolean cleaningDetectedChangedValue(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFDataLoaderToolException {

		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);

		Boolean result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedChangedValue",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedChangedValue",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);

			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedChangedValue",
				"targetBaseFieldName",
				targetBaseFieldName);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.cleaningDetectedChangedValue",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName(),
					targetBaseFieldName);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);

			result
				= cleanStepManager.cleaningDetectedChangedValue(
					connection, 
					tableCleaningConfiguration,
					rowNumber,
					targetBaseFieldName);
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"cleaningDetectedChangedValue",
				rifServiceException);
		}		

		return result;		

	}
	
	public Boolean cleaningDetectedErrorValue(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFDataLoaderToolException {
				
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(_tableCleaningConfiguration);

		Boolean result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedErrorValue",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedErrorValue",
				"tableCleaningConfiguration",
				tableCleaningConfiguration);
			
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedErrorValue",
				"targetBaseFieldName",
				targetBaseFieldName);
				
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.cleaningDetectedErrorValue",
					user.getUserID(),
					user.getIPAddress(),
					tableCleaningConfiguration.getDisplayName(),
					targetBaseFieldName);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);

			result
				= cleanStepManager.cleaningDetectedErrorValue(
					connection, 
					tableCleaningConfiguration,
					rowNumber,
					targetBaseFieldName);
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"cleaningDetectedErrorValue",
				rifServiceException);
		}		

		return result;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	//Audit failure of operation

	@Override
	public void logException(
		User user,
		String methodName,
		RIFServiceException rifServiceException) {
		
				
	}
	

	public void logException(
		User user,
		String methodName,
		RIFDataLoaderToolException rifDataLoaderToolException) {
	
			
	}

	@Override
	public void validateUser(
		final User user) 
		throws RIFServiceException {

		//@TODO: harmonise this with the underlying AbstractRIFService call
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


