package rifDataLoaderTool.dataStorageLayer;


import rifDataLoaderTool.system.*;
import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.dataStorageLayer.postgresql.*;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.businessConceptLayer.User;
import rifServices.dataStorageLayer.AbstractRIFService;
import rifServices.util.FieldValidationUtility;
import rifServices.util.RIFLogger;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.dataStorageLayer.RIFServiceResources;
import rifServices.dataStorageLayer.SQLConnectionManager;







import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

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

public final class DataLoaderService 
	extends AbstractRIFService
	implements RIFDataLoaderServiceAPI {
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final int TEXT_COLUMN_WIDTH = 30;
	// ==========================================
	// Section Properties
	// ==========================================
	
	private SQLConnectionManager sqlConnectionManager;
	private RIFDataLoaderStartupOptions startupOptions;
	
	private RIFServiceResources rifServiceResources;

	private UserManager userManager;
	
	private DataSourceManager dataSourceManager;
	private LoadStepManager loadStepManager;
	private CleanStepManager cleanStepManager;
	private ConvertStepManager convertStepManager;
	
	
	private ArrayList<DataSetConfiguration> dataSetConfigurations;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataLoaderService() {
		
		
		dataSetConfigurations 
			= new ArrayList<DataSetConfiguration>();
		
		DataSetConfiguration exampleDataSetConfiguration1
			= new DataSetConfiguration();
		exampleDataSetConfiguration1.setName("klg_cancer_study_2013");
		exampleDataSetConfiguration1.setDescription("An investigation about various cancers");
		exampleDataSetConfiguration1.setCreationDatePhrase("10-JUN-2013");
		exampleDataSetConfiguration1.setLastActivityStepPerformed(RIFDataLoaderActivityStep.LOAD);
		dataSetConfigurations.add(exampleDataSetConfiguration1);
		
		DataSetConfiguration exampleDataSetConfiguration2
			= new DataSetConfiguration();
		exampleDataSetConfiguration2.setName("ff_breathing_disorders");
		exampleDataSetConfiguration2.setDescription("An investigation about respiratory problems.");
		exampleDataSetConfiguration2.setCreationDatePhrase("10-OCT-2014");
		exampleDataSetConfiguration2.setLastActivityStepPerformed(RIFDataLoaderActivityStep.CLEAN);
		dataSetConfigurations.add(exampleDataSetConfiguration2);
		
		DataSetConfiguration exampleDataSetConfiguration3
			= new DataSetConfiguration();
		exampleDataSetConfiguration3.setName("js_dioxins_05082014");
		exampleDataSetConfiguration3.setDescription("An investigation about respiratory problems.");
		exampleDataSetConfiguration3.setCreationDatePhrase("05-AUG-2014");		
		exampleDataSetConfiguration3.setLastActivityStepPerformed(RIFDataLoaderActivityStep.CONVERT);
		dataSetConfigurations.add(exampleDataSetConfiguration3);

		DataSetConfiguration exampleDataSetConfiguration4
			= new DataSetConfiguration();
		exampleDataSetConfiguration4.setName("asthma_data1");
		exampleDataSetConfiguration4.setDescription("An investigation about asthma.");
		exampleDataSetConfiguration4.setCreationDatePhrase("06-SEP-2012");		
		exampleDataSetConfiguration4.setLastActivityStepPerformed(RIFDataLoaderActivityStep.COMBINE);
		dataSetConfigurations.add(exampleDataSetConfiguration4);
		
		DataSetConfiguration exampleDataSetConfiguration5
			= new DataSetConfiguration();
		exampleDataSetConfiguration5.setName("asthma_data2");
		exampleDataSetConfiguration5.setDescription("Another data set about asthma data.");
		exampleDataSetConfiguration5.setCreationDatePhrase("07-OCT-2012");		
		exampleDataSetConfiguration5.setLastActivityStepPerformed(RIFDataLoaderActivityStep.COMBINE);
		dataSetConfigurations.add(exampleDataSetConfiguration5);
		
		
		DataSetConfiguration exampleDataSetConfiguration6
			= new DataSetConfiguration();
		exampleDataSetConfiguration6.setName("cardiac_arrest_incidents_04052013");
		exampleDataSetConfiguration6.setDescription("Another data set about heart attack incidents.");
		exampleDataSetConfiguration6.setCreationDatePhrase("08-NOV-2013");		
		exampleDataSetConfiguration6.setLastActivityStepPerformed(RIFDataLoaderActivityStep.OPTIMISE);
		dataSetConfigurations.add(exampleDataSetConfiguration6);
				
		DataSetConfiguration exampleDataSetConfiguration7
			= new DataSetConfiguration();
		exampleDataSetConfiguration7.setName("lung_cancer_arrest_incidents_10112013");
		exampleDataSetConfiguration7.setDescription("Another data set about lung cancer.");
		exampleDataSetConfiguration7.setCreationDatePhrase("15-NOV-2013");		
		exampleDataSetConfiguration7.setLastActivityStepPerformed(RIFDataLoaderActivityStep.CHECK);
		dataSetConfigurations.add(exampleDataSetConfiguration7);

		
	}
	public void initialiseService() 
		throws RIFServiceException {

		RIFServiceStartupOptions rifServiceStartupOptions
			= RIFServiceStartupOptions.newInstance(false, true);
		rifServiceResources
			= RIFServiceResources.newInstance(rifServiceStartupOptions);
		
		startupOptions = new RIFDataLoaderStartupOptions();
		sqlConnectionManager = rifServiceResources.getSqlConnectionManager();
			
		userManager = new UserManager();
		
		dataSourceManager = new DataSourceManager();
		
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
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void login(
		final String userID,
		final String password)
		throws RIFServiceException {
		
		sqlConnectionManager.login(
			userID, 
			password);		
	}
		
	public void logout(
		final User user) 
		throws RIFServiceException {
		
		sqlConnectionManager.logout(user);
	}
	
	public void shutdown() throws RIFServiceException {
		sqlConnectionManager.deregisterAllUsers();
	}
	
	public void addUser(
		final User _user,
		final String password,
		final RIFUserRole rifUserRole,
		final Date _expirationDate)
		throws RIFServiceException {
				
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		Date expirationDate = new Date(_expirationDate.getTime()); 
		
		Connection connection = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"addUser",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"addUser",
				"password",
				password);
			fieldValidationUtility.checkNullMethodParameter(
				"addUser",
				"rifUserRole",
				rifUserRole);

			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.addUser",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
		
			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			userManager.addUser(
				connection, 
				user.getUserID(), 
				password, 
				rifUserRole, 
				expirationDate);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"addUser",
				rifServiceException);
		}		
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		
	}

	public void alterUser(
		final User _user,
		final String updatedPassword,
		final RIFUserRole updatedRIFUserRole,
		final Date _updatedExpirationDate) 
		throws RIFServiceException {
				
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		Date updatedExpirationDate = new Date(_updatedExpirationDate.getTime()); 

		Connection connection = null;
		try {
	
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"alterUser",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"alterUser",
				"updatedPassword",
				updatedPassword);
			fieldValidationUtility.checkNullMethodParameter(
				"alterUser",
				"updatedRIFUserRole",
				updatedRIFUserRole);

			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.alterUser",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			userManager.alterUser(
				connection, 
				user,
				updatedPassword, 
				updatedRIFUserRole, 
				updatedExpirationDate);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"alterUser",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		
	}
		
	public void deleteUser(
		final User _user) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);

		Connection connection = null;
		try {
	
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"deleteUser",
				"user",
				user);

			//Check for security violations
			validateUser(user);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.deleteUser",
					user.getUserID(),
					user.getIPAddress());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			connection 
				= sqlConnectionManager.assignPooledWriteConnection(user);
			userManager.deleteUser(
				connection, 
				user);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"deleteUser",
				rifServiceException);
		}
		finally {
			//Reclaim pooled connection
			sqlConnectionManager.reclaimPooledReadConnection(
				user, 
				connection);			
		}
		
	}
	
	public ArrayList<DataSetConfiguration> getDataSetConfigurations(
		final User user) 
		throws RIFServiceException {
		
		return dataSetConfigurations;
	}
	
	
	public ArrayList<DataSetConfiguration> getDataSetConfigurations(
		final User user,
		final String searchPhrase) 
		throws RIFServiceException {
		
		
		return dataSetConfigurations;
	}
	
	public boolean dataSetConfigurationExists(
		final User user,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {

		return false;
	}

	public void deleteDataSetConfigurations(
		final User user,
		final ArrayList<DataSetConfiguration> dataSetConfigurationsToDelete) 
		throws RIFServiceException {

		for (DataSetConfiguration dataSetConfigurationToDelete : dataSetConfigurationsToDelete) {
			dataSetConfigurations.remove(dataSetConfigurationToDelete);			
		}		
	}
	
	public void clearAllDataSources(
		final User _user)  
		throws RIFServiceException,
		RIFServiceException {

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
		RIFServiceException {
		
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
		RIFServiceException {
		
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
		RIFServiceException {

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
		RIFServiceException {
		
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
		RIFServiceException {
		
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

		return results;
	}
		
	public void cleanConfiguration(
		final User _user,
		final TableCleaningConfiguration _tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFServiceException {
		
		
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
		RIFServiceException {

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
		RIFServiceException {
		
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
		RIFServiceException {

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
		RIFServiceException {
		
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
		RIFServiceException {

		
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
		RIFServiceException {

		
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
		RIFServiceException {

		
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
		RIFServiceException {
				
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
	
	public String[][] getVarianceInFieldData(
		final User _user,
		final TableFieldCleaningConfiguration _tableFieldCleaningConfiguration)
		throws RIFServiceException {
	
		//Defensively copy parameters and guard against blocked users
		User user = User.createCopy(_user);
		if (sqlConnectionManager.isUserBlocked(user) == true) {
			return null;
		}
		TableFieldCleaningConfiguration tableFieldCleaningConfiguration
			= TableFieldCleaningConfiguration.createCopy(_tableFieldCleaningConfiguration);

		String[][] results = new String[0][0];
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getVarianceInFieldData",
				"user",
				user);
			fieldValidationUtility.checkNullMethodParameter(
				"getVarianceInFieldData",
				"tableFieldCleaningConfiguration",
				tableFieldCleaningConfiguration);
			
			//Check for security violations
			validateUser(user);

			String coreTableName
				= tableFieldCleaningConfiguration.getCoreTableName();
			String loadTableName
				= RIFTemporaryTablePrefixes.LOAD.getTableName(coreTableName);
			String fieldOfInterest
				= tableFieldCleaningConfiguration.getLoadTableFieldName();
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getVarianceInFieldData",
					user.getUserID(),
					user.getIPAddress(),
					loadTableName,
					fieldOfInterest);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					user);

			results
				= cleanStepManager.getVarianceInFieldData(
					connection, 
					tableFieldCleaningConfiguration);
			sqlConnectionManager.reclaimPooledWriteConnection(
				user, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				user,
				"cleaningDetectedErrorValue",
				rifServiceException);
		}		

		return results;
	
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	//Audit failure of operation

	public void logException(
		User user,
		String methodName,
		RIFServiceException rifServiceException) {
		
				
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


