package rifDataLoaderTool.dataStorageLayer;


import rifDataLoaderTool.system.*;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.dataStorageLayer.postgresql.*;
import rifDataLoaderTool.dataStorageLayer.SQLConnectionManager;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.businessConceptLayer.User;
import rifServices.util.FieldValidationUtility;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;

import java.io.FileWriter;
import java.sql.*;
import java.util.ArrayList;
import java.io.*;

/**
 * Main implementation of the {@link rifDataLoaderTool.dataStorageLayer.AbstractDataLoaderService}.
 * Almost every method in this class has the following common steps:
 * <ol>
 * <li>
 * safely copy parameter values so that the middleware is not vulnerable to changes the client makes
 * to them while the method is executing.
 * </li>
 * <li>
 * if the rifManager making the request has been black listed because of a previous security incident, return
 * as soon as possible.
 * </li>
 * <li>
 * check for any null parameter values
 * </li>
 * <li>
 * check for any security violations that could occur, either in the rifManager parameter object,
 * or any text field value of any business object. If any security violation is detected, log it and
 * black list the rifManager until further notice.
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

public abstract class AbstractDataLoaderService 
	implements DataLoaderServiceAPI {
	
	// ==========================================
	// Section Constants
	// ==========================================

	
	// ==========================================
	// Section Properties
	// ==========================================
	
	private SQLConnectionManager sqlConnectionManager;
	private DataSetManager dataSetManager;
	private LoadWorkflowManager loadWorkflowManager;
	
	private ChangeAuditManager changeAuditManager;
	private CleanWorkflowManager cleanWorkflowManager;
	
	private CombineWorkflowManager combineWorkflowManager;
	private SplitWorkflowManager splitWorkflowManager;
	private ConvertWorkflowManager convertWorkflowManager;
	private OptimiseWorkflowManager optimiseWorkflowManager;
	
	private CheckWorkflowManager checkWorkflowManager;
	private PublishWorkflowManager publishWorkflowManager;
	
	
	private ArrayList<DataSetConfiguration> dataSetConfigurations;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractDataLoaderService() {
			
		dataSetConfigurations 
			= new ArrayList<DataSetConfiguration>();
		
	}
	
	public void initialiseService() 
		throws RIFServiceException {

		//RIFServiceStartupOptions rifServiceStartupOptions
		//	= RIFServiceStartupOptions.newInstance(false, true);
		
		String databaseDriverName = "org.postgresql.Driver";
		String databaseDriverPrefix = "jdbc:postgresql";
		String host = "localhost";
		String port = "5432";
		String databaseName = "tmp_sahsu_db";
		sqlConnectionManager
			= new SQLConnectionManager(
				databaseDriverName,
				databaseDriverPrefix,
				host,
				port,
				databaseName);
		sqlConnectionManager.initialiseConnectionQueue("kgarwood", "kgarwood");
			
	
		RIFDatabaseProperties rifDatabaseProperties 
			= RIFDatabaseProperties.newInstance(
				DatabaseType.POSTGRESQL, 
				false);
		dataSetManager = new DataSetManager(rifDatabaseProperties);
		loadWorkflowManager
			= new LoadWorkflowManager(
				rifDatabaseProperties,
				dataSetManager);
		
		changeAuditManager
			= new ChangeAuditManager(rifDatabaseProperties);
		
		PostgresCleaningStepQueryGenerator cleanWorkflowQueryGenerator
			= new PostgresCleaningStepQueryGenerator();
		cleanWorkflowManager
			= new CleanWorkflowManager(
				rifDatabaseProperties,
				dataSetManager,
				changeAuditManager,
				cleanWorkflowQueryGenerator);
					
		convertWorkflowManager
			= new ConvertWorkflowManager(
				rifDatabaseProperties);
		
		combineWorkflowManager
			= new CombineWorkflowManager(
				rifDatabaseProperties);
				
		splitWorkflowManager
			= new SplitWorkflowManager(
				rifDatabaseProperties);
		
		optimiseWorkflowManager
			= new OptimiseWorkflowManager(
				rifDatabaseProperties);
				
		checkWorkflowManager
			= new CheckWorkflowManager(
				rifDatabaseProperties);
		
		publishWorkflowManager
			= new PublishWorkflowManager(
				rifDatabaseProperties);
	}
	
	public void shutdownService() 
		throws RIFServiceException {

	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void login(
		final String rifManagerID,
		final String password)
		throws RIFServiceException {
		
		sqlConnectionManager.login(
			rifManagerID, 
			password);		
	}
		
	public void logout(
		final User rifManager) 
		throws RIFServiceException {
		
		sqlConnectionManager.logout(rifManager);
	}
	
	public void shutdown() throws RIFServiceException {
		sqlConnectionManager.deregisterAllUsers();
	}
	
	protected DataSetManager getDataSetManager() {
		return dataSetManager;
	}
	
	protected ChangeAuditManager getChangeAuditManager() {
		return changeAuditManager;
	}
	
	protected SQLConnectionManager getSQLConnectionManger() {
		return sqlConnectionManager;
	}
	
	public ArrayList<DataSetConfiguration> getDataSetConfigurations(
		final User rifManager) 
		throws RIFServiceException {
		
		return dataSetConfigurations;
	}
		
	public ArrayList<DataSetConfiguration> getDataSetConfigurations(
		final User rifManager,
		final String searchPhrase) 
		throws RIFServiceException {
		
		
		return dataSetConfigurations;
	}
	
	public boolean dataSetConfigurationExists(
		final User rifManager,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {

		return false;
	}

	public void deleteDataSetConfigurations(
		final User rifManager,
		final ArrayList<DataSetConfiguration> dataSetConfigurationsToDelete) 
		throws RIFServiceException {

		for (DataSetConfiguration dataSetConfigurationToDelete : dataSetConfigurationsToDelete) {
			dataSetConfigurations.remove(dataSetConfigurationToDelete);			
		}		
	}

	public void registerDataSetConfiguration(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration)
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);

		try {
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"registerDataSetConfiguration",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"registerDataSetConfiguration",
				"dataSetConfiguration",
				dataSetConfiguration);
			
			//sortingOrder can be null, it just means that the
			//order will be ascending lower limit
		
			//Check for security violations
			validateUser(rifManager);
			dataSetConfiguration.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.registerDataSet",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);

			//Assign pooled connection
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(rifManager);
			
			dataSetManager.addDataSetConfiguration(
				connection,
				logFileWriter,
				dataSetConfiguration);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printStackTrace(System.out);
			//Audit failure of operation
			logException(
				rifManager,
				"registerdataSet",
				rifServiceException);
		}		
	}
		
		
	public void loadConfiguration(
		final User _rifManager,
		final Writer logFileWriter,		
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException,
		RIFServiceException {

		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
		RIFSchemaAreaPropertyManager schemaAreaPropertyManager
			= new RIFSchemaAreaPropertyManager();
		WorkflowValidator workFlowValidator 
			= new WorkflowValidator(schemaAreaPropertyManager);
		workFlowValidator.validateLoad(dataSetConfiguration);
		
		
		try {			
			//Check for empty parameters
			checkCommonParameters(
				"loadConfiguration",
				rifManager,
				dataSetConfiguration);
			
						
			//Check for security violations
			validateUser(rifManager);
			dataSetConfiguration.checkSecurityViolations();

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.loadConfiguration",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
						rifManager);
			
			loadWorkflowManager.loadConfiguration(
				connection, 
				logFileWriter,
				dataSetConfiguration);

			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
		
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"loadConfiguration",
				rifServiceException);
			throw rifServiceException;
		}		
	}
	
	public void addLoadTableData(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration,
		final String[][] tableData)
		throws RIFServiceException,
		RIFServiceException {
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
	
		try {

			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"addLoadTableData",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"addLoadTableData",
				"dataSetConfiguration",
				dataSetConfiguration);
			fieldValidationUtility.checkNullMethodParameter(
				"addLoadTableData",
				"tableData",
				tableData);
			
			//Check for security violations
			validateUser(rifManager);
			dataSetConfiguration.checkSecurityViolations();
			
			//@TODO: we obviously need something to check the array of Strings
			//for security violations
			//checkTableDataForSecurityViolations(tableData)


			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.addLoadTableData",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);

			loadWorkflowManager.addLoadTableData(
				connection, 
				logFileWriter,
				dataSetConfiguration, 
				tableData);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"addLoadTableData",
				rifServiceException);
		}		

	}

	public RIFResultTable getLoadTableData(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException,
		RIFServiceException {
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);

				
		RIFResultTable results = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getLoadTableData",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"getLoadTableData",
				"dataSetConfiguration",
				dataSetConfiguration);
			

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getLoadTableData",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
						
			results
				= loadWorkflowManager.getLoadTableData(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"getLoadTableData",
				rifServiceException);
		}

		return results;
	}
		
	public void cleanConfiguration(
		final User _rifManager,
		final Writer logFileWriter,		
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException,
		RIFServiceException {
		
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
		
		try {
			
			//Check for empty parameters
			checkCommonParameters(
				"cleanConfiguration",
				rifManager,
				dataSetConfiguration);
						
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.cleanConfiguration",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
		
			System.out.println("DataLoader Service ================doing clean configuration !!!!!!!!!!!");

			cleanWorkflowManager.cleanConfiguration(
				connection, 
				logFileWriter,
				dataSetConfiguration);
						
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"cleanConfiguration",
				rifServiceException);
			throw rifServiceException;
		}		

	}
	
	public RIFResultTable getCleanedTableData(
		final User _rifManager,			
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException,
		RIFServiceException {

		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
				
		RIFResultTable result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCleanedTableData",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"getCleanedTableData",
				"dataSetConfiguration",
				dataSetConfiguration);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getCleanedTableData",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
								
			result
				= cleanWorkflowManager.getCleanedTableData(
					connection, 
					dataSetConfiguration);
			
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"getCleanedTableData",
				rifServiceException);
		}		
	
		return result;

	}

	
	public void combineConfiguration(
		final User _rifManager,
		final Writer logFileWriter,		
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException,
		RIFServiceException {
	
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
	
		try {
			
			//Check for empty parameters
			checkCommonParameters(
				"combineConfiguration",
				rifManager,
				dataSetConfiguration);
					
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.combineConfiguration",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
		
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
							
			combineWorkflowManager.combineConfiguration(
				connection, 
				logFileWriter,
				dataSetConfiguration);
					
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);	
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"combineConfiguration",
				rifServiceException);
			throw rifServiceException;
		}
	}
	
	public void splitConfiguration(
		final User _rifManager,
		final Writer logFileWriter,		
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException,
		RIFServiceException {
	
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
	
		try {
			
			//Check for empty parameters
			checkCommonParameters(
				"splitConfiguration",
				rifManager,
				dataSetConfiguration);
					
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.splitConfiguration",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
		
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
							
			splitWorkflowManager.splitConfiguration(
				connection, 
				logFileWriter,
				dataSetConfiguration);
					
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);	
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"splitConfiguration",
				rifServiceException);
			throw rifServiceException;
		}
	}
	
	public void convertConfiguration(
		final User _rifManager,
		final Writer logFileWriter,		
		final DataSetConfiguration _dataSetConfiguration)
		throws RIFServiceException,
		RIFServiceException {
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
				
		try {
			
			//Check for empty parameters
			checkCommonParameters(
				"convertConfiguration",
				rifManager,
				dataSetConfiguration);
						
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.convertConfiguration",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
								
			convertWorkflowManager.convertConfiguration(
				connection, 
				logFileWriter,
				dataSetConfiguration);
					
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"convertConfiguration",
				rifServiceException);
			throw rifServiceException;
		}		
	}

	public void optimiseConfiguration(
		final User _rifManager,
		final Writer logFileWriter,		
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
		
		try {
			//Check for empty parameters
			checkCommonParameters(
				"optimiseConfiguration",
				rifManager,
				dataSetConfiguration);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage(
					"logging.optimiseConfiguration",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);

			optimiseWorkflowManager.optimiseConfiguration(
				connection, 
				logFileWriter,
				dataSetConfiguration);
					
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"optimiseConfiguration",
				rifServiceException);
			throw rifServiceException;
		}	
	}


	public void checkConfiguration(
		final User _rifManager,
		final Writer logFileWriter,		
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
		
		try {
			//Check for empty parameters
			checkCommonParameters(
				"checkConfiguration",
				rifManager,
				dataSetConfiguration);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.checkConfiguration",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);

			checkWorkflowManager.checkConfiguration(
				connection, 
				logFileWriter,
				dataSetConfiguration);
					
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"checkConfiguration",
				rifServiceException);
			throw rifServiceException;
		}	
	}
	

	public void publishConfiguration(
		final User _rifManager,
		final Writer logFileWriter,		
		final DataSetConfiguration _dataSetConfiguration) 
		throws RIFServiceException {
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);
		
		try {
			//Check for empty parameters
			checkCommonParameters(
				"publishConfiguration",
				rifManager,
				dataSetConfiguration);

			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.publishConfiguration",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);

			publishWorkflowManager.publishConfiguration(
				connection,
				logFileWriter,
				dataSetConfiguration);
					
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"publishConfiguration",
				rifServiceException);
			throw rifServiceException;
		}	
	}
	
	public Integer getCleaningTotalBlankValues(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration)
		throws RIFServiceException,
		RIFServiceException {

		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);

		Integer result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalBlankValues",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalBlankValues",
				"dataSetConfiguration",
				dataSetConfiguration);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getCleaningTotalBlankValues",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
			result
				= cleanWorkflowManager.getCleaningTotalBlankValues(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"getCleaningTotalBlankValues",
				rifServiceException);
		}		

		return result;
	}
		
	public Integer getCleaningTotalChangedValues(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration)
		throws RIFServiceException,
		RIFServiceException {
		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);

		Integer result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalChangedValues",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalChangedValues",
				"dataSetConfiguration",
				dataSetConfiguration);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getCleaningTotalChangedValues",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
			result
				= cleanWorkflowManager.getCleaningTotalChangedValues(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"getCleaningTotalBlankValues",
				rifServiceException);
		}		

		return result;
	}
		
	public Integer getCleaningTotalErrorValues(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration)
		throws RIFServiceException,
		RIFServiceException {

		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);

		Integer result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalErrorValues",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"getCleaningTotalErrorValues",
				"dataSetConfiguration",
				dataSetConfiguration);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.getCleaningTotalErrorValues",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName());
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);
			result
				= cleanWorkflowManager.getCleaningTotalErrorValues(
					connection, 
					logFileWriter,
					dataSetConfiguration);
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"getCleaningTotalErrorValues",
				rifServiceException);
		}		

		return result;
	}
	
	public Boolean cleaningDetectedBlankValue(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFServiceException {

		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);

		Boolean result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedBlankValue",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedBlankValue",
				"dataSetConfiguration",
				dataSetConfiguration);

			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedBlankValue",
				"targetBaseFieldName",
				targetBaseFieldName);
					
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage(
					"logging.cleaningDetectedBlankValue",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName(),
					targetBaseFieldName, 
					String.valueOf(rowNumber));
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);

			result
				= cleanWorkflowManager.cleaningDetectedBlankValue(
					connection, 
					logFileWriter,
					dataSetConfiguration,
					rowNumber,
					targetBaseFieldName);
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"cleaningDetectedBlankValue",
				rifServiceException);
		}		

		return result;		
	}
	
	public Boolean cleaningDetectedChangedValue(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFServiceException {

		
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);

		Boolean result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedChangedValue",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedChangedValue",
				"dataSetConfiguration",
				dataSetConfiguration);

			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedChangedValue",
				"targetBaseFieldName",
				targetBaseFieldName);
			
			
			//Audit attempt to do operation
			RIFLogger rifLogger = RIFLogger.getLogger();				
			String auditTrailMessage
				= RIFDataLoaderToolMessages.getMessage("logging.cleaningDetectedChangedValue",
					rifManager.getUserID(),
					rifManager.getIPAddress(),
					dataSetConfiguration.getDisplayName(),
					targetBaseFieldName);
			rifLogger.info(
				getClass(),
				auditTrailMessage);
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);

			result
				= cleanWorkflowManager.cleaningDetectedChangedValue(
					connection, 
					logFileWriter,
					dataSetConfiguration,
					rowNumber,
					targetBaseFieldName);
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"cleaningDetectedChangedValue",
				rifServiceException);
		}		

		return result;		

	}
	
	public Boolean cleaningDetectedErrorValue(
		final User _rifManager,
		final Writer logFileWriter,
		final DataSetConfiguration _dataSetConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFServiceException {
				
		//Defensively copy parameters and guard against blocked rifManagers
		User rifManager = User.createCopy(_rifManager);
		if (sqlConnectionManager.isUserBlocked(rifManager) == true) {
			return null;
		}
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.createCopy(_dataSetConfiguration);

		Boolean result = null;
		try {
			
			//Check for empty parameters
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedErrorValue",
				"rifManager",
				rifManager);
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedErrorValue",
				"dataSetConfiguration",
				dataSetConfiguration);
			
			fieldValidationUtility.checkNullMethodParameter(
				"cleaningDetectedErrorValue",
				"targetBaseFieldName",
				targetBaseFieldName);
				
			
			//Audit attempt to do operation
			
			Connection connection 
				= sqlConnectionManager.assignPooledWriteConnection(
					rifManager);

			result
				= cleanWorkflowManager.cleaningDetectedErrorValue(
					connection, 
					logFileWriter,
					dataSetConfiguration,
					rowNumber,
					targetBaseFieldName);
			sqlConnectionManager.reclaimPooledWriteConnection(
				rifManager, 
				connection);
			return result;
		}
		catch(RIFServiceException rifServiceException) {
			//Audit failure of operation
			logException(
				rifManager,
				"cleaningDetectedErrorValue",
				rifServiceException);
		}		

		return result;
	}
	
	public String[][] getVarianceInFieldData(
		final User _rifManager,
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
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private void checkCommonParameters(
		final String methodName,
		final User rifManager,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
	
		//Check for empty parameters
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		fieldValidationUtility.checkNullMethodParameter(
			methodName,
			"rifManager",
			rifManager);
		fieldValidationUtility.checkNullMethodParameter(
			methodName,
			"dataSetConfiguration",
			dataSetConfiguration);
		
		rifManager.checkErrors(ValidationPolicy.STRICT);

		//validateUser(rifManager);
		dataSetConfiguration.checkSecurityViolations();
	}
	
	
	
	
	
	
	
	
	
	
	//Audit failure of operation
	public void logException(
		User rifManager,
		String methodName,
		RIFServiceException rifServiceException) {
		
		rifServiceException.printErrors();	
	}
	
	public void validateUser(
		final User rifManager) 
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


