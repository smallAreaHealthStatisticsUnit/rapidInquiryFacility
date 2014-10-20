package rifDataLoaderTool.clean;

import rifDataLoaderTool.businessConceptLayer.DataSource;
import rifDataLoaderTool.businessConceptLayer.SexRIFDataType;
import rifDataLoaderTool.businessConceptLayer.TableCleaningConfiguration;
import rifDataLoaderTool.businessConceptLayer.TableFieldCleaningConfiguration;
import rifDataLoaderTool.dataStorageLayer.DataLoaderService;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.businessConceptLayer.User;

import org.junit.Test;
import static org.junit.Assert.*;

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

public class TestCleanSex extends AbstractRIFDataLoaderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private TableCleaningConfiguration masterTableCleaningConfiguration;
	private TableFieldCleaningConfiguration masterSexFieldConfiguration;
	private User testUser;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public TestCleanSex() {

		String coreTableName = "hes_2002";
		masterTableCleaningConfiguration 
			= TableCleaningConfiguration.newInstance(coreTableName);
	
		DataSource dataSource
			= DataSource.newInstance(
				coreTableName,
				false,
				"HES file hes2001.csv", 
					"kgarwood");
		dataSource.setIdentifier("1");
	
		masterTableCleaningConfiguration.setDataSource(dataSource);
		
		masterSexFieldConfiguration
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"sex", 
				SexRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(masterSexFieldConfiguration);
		
		DummyDataLoaderGenerator dummyDataGenerator
			= new DummyDataLoaderGenerator();
		testUser = dummyDataGenerator.createTestUser();		
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	@Test
	public void acceptCorrectOriginalSexValues() {

		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(masterTableCleaningConfiguration);
		
		String[][] originalLoadTableData 
			= { {"0"},
				{"1"},
				{"0"},
				{"3"},
				{"2"}};

		//these data should be unchanged
		try {
			DataLoaderService dataLoaderService
				= getDataLoaderService();
	
			dataLoaderService.loadConfiguration(
				testUser, 
				tableCleaningConfiguration);
			dataLoaderService.addLoadTableData(
				testUser,
				tableCleaningConfiguration,
				originalLoadTableData);
	
			dataLoaderService.cleanConfiguration(
				testUser,
				tableCleaningConfiguration);
			RIFResultTable rifResultTable
				= dataLoaderService.getCleanedTableData(
					testUser,
					tableCleaningConfiguration);
		
			boolean resultsMatch
				= expectedAndActualTablesIdentical(
					rifResultTable,
					originalLoadTableData);
			assertEquals(true, resultsMatch);
			
			
		}
		catch(Exception exception) {
			fail();
		}
		
	}
	
	@Test
	public void acceptProperlyCleanedDataSet() {
		
		TableCleaningConfiguration tableCleaningConfiguration
			= TableCleaningConfiguration.createCopy(masterTableCleaningConfiguration);
	
		String[][] originalLoadTableData 
			= { {"0"}, //Row 1, legal, should go unchanged
				{"male"}, //Row 2, convert to a 0
				{"MALE"}, //Row 3, convert to a 0
				{"m"}, //Row 4, convert to a 0
				{"M"}, //Row 5, convert to a 0
				{"1"}, //Row 6, legal, should go unchanged
				{"female"}, //Row 7, convert to a 1
				{"FEMALE"}, //Row 8, convert to a 1
				{"f"}, //Row 9, convert to a 1
				{"F"}, //Row 10, convert to a 1			
				{"2"}, //Row 11, legal, should go unchanged
				{"h"}, //Row 12, convert to a 2
				{"H"}, //Row 13, convert to a 2
				{"hermaphrodite"}, //Row 14, convert to a 2
				{"HERMAPHRODITE"}, //Row 15, convert to a 2
				{"3"}, //Row 16, should go unchanged
				{"u"}, //Row 17, convert to a 3
				{"U"}, //Row 18, convert to a 3
				{"unknown"}, //Row 19, convert to a 3
				{"UNKNOWN"}, //Row 20, convert to a 3
				{"blah"}, //Row 21, convert to 'rif_error'
				{"6"} }; //Row 22, convert to 'rif_error'

	
		String[][] expectedCleanedTableData 
			= { {"0"}, //Row 1, legal, should go unchanged
				{"0"}, //Row 2, convert to a 0
				{"0"}, //Row 3, convert to a 0
				{"0"}, //Row 4, convert to a 0
				{"0"}, //Row 5, convert to a 0
				{"1"}, //Row 6, legal, should go unchanged
				{"1"}, //Row 7, convert to a 1
				{"1"}, //Row 8, convert to a 1
				{"1"}, //Row 9, convert to a 1
				{"1"}, //Row 10, convert to a 1			
				{"2"}, //Row 11, legal, should go unchanged
				{"2"}, //Row 12, convert to a 2
				{"2"}, //Row 13, convert to a 2
				{"2"}, //Row 14, convert to a 2
				{"2"}, //Row 15, convert to a 2
				{"3"}, //Row 16, should go unchanged
				{"3"}, //Row 17, convert to a 3
				{"3"}, //Row 18, convert to a 3
				{"3"}, //Row 19, convert to a 3
				{"3"}, //Row 20, convert to a 3
				{"rif_error"}, //Row 21, convert to 'rif_error'
				{"rif_error"} }; //Row 22, convert to 'rif_error'
	
		try {
			DataLoaderService dataLoaderService
				= getDataLoaderService();

			dataLoaderService.loadConfiguration(
				testUser, 
				tableCleaningConfiguration);
			dataLoaderService.addLoadTableData(
				testUser,
				tableCleaningConfiguration,
				originalLoadTableData);

			dataLoaderService.cleanConfiguration(
				testUser,
				tableCleaningConfiguration);
			RIFResultTable rifResultTable
				= dataLoaderService.getCleanedTableData(
					testUser,
					tableCleaningConfiguration);
	
			boolean resultsMatch
				= expectedAndActualTablesIdentical(
					rifResultTable,
					expectedCleanedTableData);
			assertEquals(true, resultsMatch);

			//check audit trail
			
		}
		catch(Exception exception) {
			fail();
		}
		
	}
	
	@Test
	public void ignoreDefaultCleaningRules() {

		
		
		
		
	}
	
	@Test
	public void overrideCleaningRules() {
		
		
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


