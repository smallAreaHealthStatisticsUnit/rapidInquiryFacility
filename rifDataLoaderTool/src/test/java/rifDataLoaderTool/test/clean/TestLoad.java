package rifDataLoaderTool.test.clean;


import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.dataStorageLayer.*;
import rifDataLoaderTool.test.AbstractRIFDataLoaderTestCase;
import rifDataLoaderTool.test.DummyDataLoaderGenerator;
import rifServices.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;
import static org.junit.Assert.*;

import org.junit.Test;

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

public class TestLoad extends AbstractRIFDataLoaderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private TableCleaningConfiguration masterDateAgeSexTableConfiguration;
	//private TableCleaningConfiguration masterTableCleaningConfiguration;
	
	private User testUser;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public TestLoad() {
		
		DummyDataLoaderGenerator dummyDataGenerator
			= new DummyDataLoaderGenerator();
		testUser = dummyDataGenerator.createTestUser();
		masterDateAgeSexTableConfiguration
			= dummyDataGenerator.createAgeSexBirthDateTableConfiguration();
	}
	
	public void test1() {
		
		/*
		 * Date(dd-MMM-yyyy)	Age		Sex
		 * 02-JAN-2001			5		M
		 * 02-JAN-2001			5		female
		 * 
		 */
		TableCleaningConfiguration tableCleaningConfiguration1
			= TableCleaningConfiguration.createCopy(masterDateAgeSexTableConfiguration);	
		
		DataSource ds = tableCleaningConfiguration1.getDataSource();
		if (ds == null) {
			System.out.println("ds is NULL");			
		}
		else {
			System.out.println("DS is NOT NULL");
		}
		
		String[][] tableData 
			= { {"02 JAN 2001", "5", "M"},
				{"02 JAN 2001", "5", "female"},
				{"02 JAN 2001", "5", "male"}};
		
		try {
			DataLoaderService dataLoaderService
				= getDataLoaderService();
			dataLoaderService.loadConfiguration(
				testUser, 
				tableCleaningConfiguration1);
			dataLoaderService.addLoadTableData(
				testUser,
				tableCleaningConfiguration1,
				tableData);
			
			RIFResultTable rifResultTable
				= dataLoaderService.getLoadTableData(
					testUser,
					tableCleaningConfiguration1);
			printResultTable(rifResultTable);
			
			dataLoaderService.cleanConfiguration(
				testUser,
				tableCleaningConfiguration1);
			RIFResultTable rifResultTable2
				= dataLoaderService.getCleanedTableData(
					testUser,
					tableCleaningConfiguration1);
			printResultTable(rifResultTable2);
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printStackTrace();
		}

	}

	@Test
	public void test2() {
		/*
		 * Date(dd-MMM-yyyy)	Age		Sex
		 * 02-JAN-2001			5		M
		 * 02-JAN-2001			5		female
		 * 
		 */
		TableCleaningConfiguration tableCleaningConfiguration1
			= TableCleaningConfiguration.createCopy(masterDateAgeSexTableConfiguration);	

		String[][] tableData 
			= { {"02 JAN 2001", "5", "M"},
				{"02 JAN 2001", "5", "female"},
				{"02 JAN 2001", "5", "male"},
				{"02 JAN 2001", "5", "unknown"},
				{"02 JAN 2001", "5", "h"},
				{"02 02 2001", "5", "blah"},
				{"02 JAN 2001", "5", "99"},
				{"02 JAN 2001", "5", "feemale"},
				{"02 JAN 2001", "5", "U"},
				{"02 JAN 2001", "blah", "U"},
				{"02 JAN 2001", "18", "U"},
				{"02 JAN 2001", "119", "U"},
				{"02 JAN 2001", "120", "U"},
				{"02 JAN 2001", "1099", "U"},
				{"", "45", "M"},
				{"02/03/2008", "55", "F"},
				{"02 JAN 2001", "-5", "U"}};
		
		try {
			DataLoaderService dataLoaderService
				= getDataLoaderService();
			
			dataLoaderService.loadConfiguration(
				testUser, 
				tableCleaningConfiguration1);
			dataLoaderService.addLoadTableData(
				testUser,
				tableCleaningConfiguration1,
				tableData);
			
			RIFResultTable rifResultTable
				= dataLoaderService.getLoadTableData(
					testUser,
					tableCleaningConfiguration1);
			printResultTable(rifResultTable);
			
			dataLoaderService.cleanConfiguration(
				testUser,
				tableCleaningConfiguration1);
			RIFResultTable rifResultTable2
				= dataLoaderService.getCleanedTableData(
					testUser,
					tableCleaningConfiguration1);
			printResultTable(rifResultTable2);
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printStackTrace();
		}

	}

	@Test
	public void test3() {

		String[][] originalLoadTableData 
			= { {"02 JAN 2001", "5", "M"},
				{"02 JAN 2001", "5", "female"},
				{"02 JAN 2001", "5", "male"},
				{"02 JAN 2001", "5", "unknown"},
				{"02 JAN 2001", "blah", "h"},
				{"02 JAN 2001", "5", "blah"},
				{"02 JAN 2001", "-45", "989"},
				{"", "-45", "989"}};

		String[][] expectedCleanedTableData 
			= { {"02-Jan-2001", "5", "0"},
				{"02-Jan-2001", "5", "1"},
				{"02-Jan-2001", "5", "0"},
				{"02-Jan-2001", "5", "3"},
				{"02-Jan-2001", "0", "2"},
				{"02-Jan-2001", "5", "0"},
				{"02-Jan-2001", "0", "0"},
				{"", "0", "0"}};
				
		TableCleaningConfiguration tableCleaningConfiguration1
			= TableCleaningConfiguration.createCopy(masterDateAgeSexTableConfiguration);	
		
		try {
			DataLoaderService dataLoaderService
				= getDataLoaderService();
		
			System.out.println("test3 - 1");
			dataLoaderService.loadConfiguration(
				testUser, 
				tableCleaningConfiguration1);
			System.out.println("test3 - 2");
			
			dataLoaderService.addLoadTableData(
				testUser,
				tableCleaningConfiguration1,
				originalLoadTableData);
			System.out.println("test3 - 3");
		
			dataLoaderService.cleanConfiguration(
				testUser,
				tableCleaningConfiguration1);
			System.out.println("test3 - 4");
			
			RIFResultTable rifResultTable
				= dataLoaderService.getCleanedTableData(
					testUser,
					tableCleaningConfiguration1);
			
			printResultTable(rifResultTable);
			System.out.println("test3 - 5");
			
			boolean resultsMatch
				= expectedAndActualTablesIdentical(
					rifResultTable,
					expectedCleanedTableData);
			System.out.println("test3 - 6");
			
			assertEquals(true, resultsMatch);
			
			//check that auditing was properly recorded
			int actualErrorsReported
				= dataLoaderService.getCleaningTotalErrorValues(
					testUser, 
					tableCleaningConfiguration1);
			assertEquals(6, actualErrorsReported);

			System.out.println("test3 - 7");
			
			int actualBlanksReported
				= dataLoaderService.getCleaningTotalBlankValues(
					testUser, 
					tableCleaningConfiguration1);
			assertEquals(1, actualBlanksReported);

			System.out.println("test3 - 8");
			
			int actualChangesReported
				= dataLoaderService.getCleaningTotalChangedValues(
					testUser, 
					tableCleaningConfiguration1);
			assertEquals(5, actualChangesReported);

			System.out.println("test3 - 9");
			
			boolean isChanged
				= dataLoaderService.cleaningDetectedChangedValue(
					testUser, 
					tableCleaningConfiguration1, 
					4, 
					"age");
			assertEquals(false, isChanged);
			
			isChanged
				= dataLoaderService.cleaningDetectedChangedValue(
					testUser, 
					tableCleaningConfiguration1, 
					2, 
					"sex");
			assertEquals(true, isChanged);
			
			boolean isBlank
				= dataLoaderService.cleaningDetectedBlankValue(
					testUser, 
					tableCleaningConfiguration1, 
					2, 
					"sex");
			assertEquals(false, isBlank);
			
			
			isBlank
				= dataLoaderService.cleaningDetectedBlankValue(
					testUser, 
					tableCleaningConfiguration1, 
					8, 
					"birth_date");
			assertEquals(true, isBlank);
			
			boolean isError
				= dataLoaderService.cleaningDetectedErrorValue(
					testUser, 
					tableCleaningConfiguration1,
					5, 
					"sex");
			assertEquals(false, isError);
			
			isError
				= dataLoaderService.cleaningDetectedErrorValue(
					testUser, 
					tableCleaningConfiguration1,
					6, 
					"sex");
			assertEquals(true, isError);
			
			
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			fail();
		}		
	}
	
	public void test4() {
		

		String[][] originalLoadTableData 
			= { {"02 JAN 2001", "5", "M"},
				{"02 JAN 2001", "5", "female"},
				{"02 JAN 2001", "5", "male"},
				{"02 JAN 2001", "5", "unknown"},
				{"02 JAN 2001", "blah", "h"},
				{"02 JAN 2001", "5", "blah"},
				{"02 JAN 2001", "-45", "989"},
				{"", "-45", "989"}};

		String[][] expectedCleanedTableData 
			= { {"02-Jan-2001", "5", "0"},
				{"02-Jan-2001", "5", "1"},
				{"02-Jan-2001", "5", "0"},
				{"02-Jan-2001", "5", "3"},
				{"02-Jan-2001", "0", "2"},
				{"02-Jan-2001", "5", "0"},
				{"02-Jan-2001", "0", "0"},
				{"", "0", "0"}};
				
		TableCleaningConfiguration tableCleaningConfiguration1
			= TableCleaningConfiguration.createCopy(masterDateAgeSexTableConfiguration);	
		
		try {
			DataLoaderService dataLoaderService
				= getDataLoaderService();
		
			dataLoaderService.loadConfiguration(
				testUser, 
				tableCleaningConfiguration1);
			
			dataLoaderService.addLoadTableData(
				testUser,
				tableCleaningConfiguration1,
				originalLoadTableData);
		
			dataLoaderService.cleanConfiguration(
				testUser,
				tableCleaningConfiguration1);

			dataLoaderService.cleanConfiguration(
				testUser,
				tableCleaningConfiguration1);
			
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			fail();
		}		
		
		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	private void printResultTable(
		final RIFResultTable rifResultTable) {
		
		System.out.println("Field Names");
		
		StringBuilder headerRow = new StringBuilder();
		String[] fieldNames = rifResultTable.getFieldNames();
		for (String fieldName : fieldNames) {
			headerRow.append(fieldName);
			headerRow.append(" ");
		}
		System.out.println(headerRow.toString());
		
		String[][] tableData = rifResultTable.getData();
		for (int ithRow = 0; ithRow < tableData.length; ithRow++) {
			StringBuilder dataRow = new StringBuilder();
			
			for (int ithColumn = 0; ithColumn < tableData[ithRow].length; ithColumn++) {
				dataRow.append(tableData[ithRow][ithColumn]);
				dataRow.append(" ");
			}
			System.out.println(dataRow.toString());
		}
		
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


