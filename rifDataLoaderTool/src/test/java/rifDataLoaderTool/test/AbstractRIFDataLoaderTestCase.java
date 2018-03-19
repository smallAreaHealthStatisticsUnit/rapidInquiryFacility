package rifDataLoaderTool.test;

import java.io.File;
import java.text.Collator;

import org.junit.After;
import org.junit.Before;

import rifDataLoaderTool.businessConceptLayer.DatabaseConnectionsConfiguration;
import rifDataLoaderTool.businessConceptLayer.TestDataLoaderServiceAPI;
import rifDataLoaderTool.dataStorageLayer.ms.TestMSDataLoaderService;
import rifDataLoaderTool.dataStorageLayer.pg.TestPGDataLoaderService;
import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.system.ClassFileLocator;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.FieldValidationUtility;


/**
 *
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

public abstract class AbstractRIFDataLoaderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================
	// ==========================================
	// Section Properties
	// ==========================================
	
	private User rifManager;
	public static DatabaseType databaseType = DatabaseType.POSTGRESQL;
		
	private File exportDirectory;
	
	private TestDataLoaderServiceAPI dataLoaderService;

	/** The test user. */
	private User validAdministrationUser;
	
	/** The invalid user. */
	private User emptyAdministrationUser;

	private User invalidAdministrationUser;
	
	/** The non existent user. */
	private User nonExistentAdministrationUser;
	
	/** The malicious user. */
	private User maliciousAdministrationUser;

	private String testFileDirectoryPath;
	
	// ==========================================
	// Section Construction
	// ==========================================

	
	public AbstractRIFDataLoaderTestCase() {
		init(DatabaseType.POSTGRESQL);		
	}
	
	public AbstractRIFDataLoaderTestCase(final DatabaseType databaseType) {
		this.databaseType = databaseType;
		init(databaseType);
	}

	private void init(final DatabaseType databaseType) {

		testFileDirectoryPath 
			= ClassFileLocator.getClassRootLocation("rifDataLoaderTool");
		exportDirectory = new File(testFileDirectoryPath);

		validAdministrationUser = User.newInstance("kgarwood", "11.111.11.228");
		nonExistentAdministrationUser = User.newInstance("nobody", "11.111.11.228");
		emptyAdministrationUser = User.newInstance(null, "11.111.11.228");
		invalidAdministrationUser = User.newInstance("9$£blah", "11.111.11.228");
		
		FieldValidationUtility fieldValidationUtility 
			= new FieldValidationUtility();
		String maliciousFieldValue 
			= fieldValidationUtility.getTestMaliciousFieldValue();		
		maliciousAdministrationUser = User.newInstance(maliciousFieldValue, "11.111.11.228");
		
		rifManager = User.newInstance("kgarwood", "111.111.111.111");		
	}
	
	protected String getTestFilePath(final String fileName) {
		StringBuilder filePath = new StringBuilder();
		filePath.append(testFileDirectoryPath);
		filePath.append(File.separator);
		filePath.append(fileName);
		return filePath.toString();		
	}

	@Before
	public void setUp() {
		if (databaseType == DatabaseType.POSTGRESQL) {
			System.out.println("Running a Postgres service");
			dataLoaderService = new TestPGDataLoaderService();
			try {
				DatabaseConnectionsConfiguration connectionsConfiguration
					= DatabaseConnectionsConfiguration.createDefaultPostgreSQL();
				System.out.println("AbstractRIFDLTestCase databaseURL=="+ connectionsConfiguration.getDatabaseURL());
				dataLoaderService.initialiseService(connectionsConfiguration);	
				dataLoaderService.clearAllDataSets(rifManager, null);
			}
			catch(RIFServiceException rifServiceException) {
				rifServiceException.printErrors();
			}		
		}
		else {
			System.out.println("Running an SQL Server service");
			dataLoaderService = new TestMSDataLoaderService();
			try {
				DatabaseConnectionsConfiguration connectionsConfiguration
					= DatabaseConnectionsConfiguration.createDefaultSQLServer();
				dataLoaderService.initialiseService(connectionsConfiguration);	
				dataLoaderService.clearAllDataSets(rifManager, null);
			}
			catch(RIFServiceException rifServiceException) {
				rifServiceException.printErrors();
			}
		}
	}

	@After
	public void tearDown() {
		try {
			dataLoaderService.closeAllConnectionsForUser(rifManager);	
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}

		
	}		
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public File getExportDirectory() {
		return exportDirectory;
	}
	
	protected User getRIFManager() {
		return rifManager;		
	}
	
	protected TestDataLoaderServiceAPI getDataLoaderService() {

		return dataLoaderService;
	}

	protected User cloneValidAdministrationUser() {
		return User.createCopy(validAdministrationUser);
	}
	
	protected User cloneEmptyAdministrationUser() {
		return User.createCopy(emptyAdministrationUser);
	}

	protected User cloneNonExistentAdministrationUser() {
		return User.createCopy(nonExistentAdministrationUser);
	}
	
	protected User cloneInvalidAdministrationUser() {
		return User.createCopy(nonExistentAdministrationUser);		
	}
	
	protected User cloneMaliciousAdministrationUser() {
		return User.createCopy(maliciousAdministrationUser);
	}
	
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================


	protected boolean expectedAndActualTablesIdentical(
		final RIFResultTable resultTable,
		final String[][] expectedResults) {
		
		String[][] actualResults
			= resultTable.getData();
		
		if (actualResults.length != expectedResults.length) {
			//different number of rows
			System.out.println(
				"Expected number of rows=="+
				expectedResults.length+
				"==actual=="+
				actualResults.length+
				"==");
			return false;
		}
		
		Collator collator = Messages.genericMessages().getCollator();
		for (int ithRow = 0; ithRow < expectedResults.length; ithRow++) {
			int numberOfColumns = expectedResults[ithRow].length;
			if (actualResults[ithRow].length != expectedResults[ithRow].length) {
				//different number of columns for corresponding rows
				System.out.println(
						"Expected number of columns=="+
						expectedResults[ithRow].length+
						"==actual=="+
						actualResults[ithRow].length+
						"==");
				return false;
			}
			else {
				for (int ithColumn = 0; ithColumn < numberOfColumns; ithColumn++) {
					if (!collator.equals(
									actualResults[ithRow][ithColumn],
									expectedResults[ithRow][ithColumn])) {

						StringBuilder buffer = new StringBuilder();
						buffer.append("Actual[");
						buffer.append(String.valueOf(ithRow));
						buffer.append("]");
						buffer.append("[");
						buffer.append(String.valueOf(ithColumn));
						buffer.append("]");
						buffer.append("=");
						buffer.append("'");
						buffer.append(actualResults[ithRow][ithColumn]);
						buffer.append("' ");
						buffer.append("Expected[");
						buffer.append(String.valueOf(ithRow));
						buffer.append("]");
						buffer.append("[");
						buffer.append(String.valueOf(ithColumn));
						buffer.append("]");
						buffer.append("=");
						buffer.append("'");
						buffer.append(expectedResults[ithRow][ithColumn]);
						buffer.append("'");

						System.out.println(buffer.toString());
						
						return false;
					}
					
				}				
			}
		}
		
		return true;
	}
	
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


