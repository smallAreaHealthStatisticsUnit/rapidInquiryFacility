package rifDataLoaderTool.test;

import rifDataLoaderTool.businessConceptLayer.*;
import rifServices.businessConceptLayer.User;

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

public final class DummyDataLoaderGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DummyDataLoaderGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public User createTestUser() {
		User testUser = User.newInstance("kgarwood", "22.231.113.64");
		return testUser;
	}

	public CleanWorkflowConfiguration createNumeratorTableConfiguration(
		final String coreTableName) {
		
		CleanWorkflowConfiguration masterTableCleaningConfiguration 
			= CleanWorkflowConfiguration.newInstance(coreTableName);
		
		DataSource dataSource
			= DataSource.newInstance(
				"hes_2001",
				false,
				"HES file hes2001.csv", 
				"kgarwood");
		dataSource.setIdentifier("1");
		
		masterTableCleaningConfiguration.setDataSource(dataSource);
		
		
		CleanWorkflowFieldConfiguration nhsNumber
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"nhs_number", 
				"A number used to identify a patient in the UK's NHS system",
				NHSNumberRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(nhsNumber);
		
		CleanWorkflowFieldConfiguration birthDate
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"birth_date", 
				"birth date of patient",
				DateRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(birthDate);
		
		CleanWorkflowFieldConfiguration postalCode
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"postal_code", 
				"postal code",
				UKPostalCodeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(postalCode);

		CleanWorkflowFieldConfiguration age
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"age", 
				"age of patient",
				AgeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(age);
		
		CleanWorkflowFieldConfiguration sex
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"sex", 
				"sex",
				SexRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(sex);

		CleanWorkflowFieldConfiguration level1
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"level1", 
				"Most general level of resolution",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(level1);
		
		CleanWorkflowFieldConfiguration level2
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"level2",
				"Level 2 geographical resolution",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(level2);
		
		CleanWorkflowFieldConfiguration level3
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"level3", 
				"Level 3 geographical resolution",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(level3);

		CleanWorkflowFieldConfiguration level4
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"level4", 
				"Level 4 geographical resolution",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(level4);

		CleanWorkflowFieldConfiguration icd1
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"icd_1", 
				"icd code 1",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(icd1);
		
		CleanWorkflowFieldConfiguration icd2
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"icd_2", 
				"icd code 2",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(icd2);
		
		CleanWorkflowFieldConfiguration opcsCode1
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"opcs_code_1", 
				"opcs code 1",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(opcsCode1);

		CleanWorkflowFieldConfiguration total
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"total", 
				"total cases",
				IntegerRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(total);

		return masterTableCleaningConfiguration;
		
	}
	
	public CleanWorkflowConfiguration createAgeSexBirthDateTableConfiguration() {
		String coreTableName = "hes_2001";

		CleanWorkflowConfiguration masterTableCleaningConfiguration
			= CleanWorkflowConfiguration.newInstance(coreTableName);

		DataSource dataSource
			= DataSource.newInstance(
				"hes_2001",
				false,
				"HES file hes2001.csv", 
				"kgarwood");
		dataSource.setIdentifier("1");
		masterTableCleaningConfiguration.setDataSource(dataSource);
		
		
		CleanWorkflowFieldConfiguration birthDate
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"birth_date", 
				"birth date",
				DateRIFDataType.newInstance());
		birthDate.setAllowBlankValues(true);
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(birthDate);
	
		CleanWorkflowFieldConfiguration age
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"age",
				"age",
				AgeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(age);
	
		CleanWorkflowFieldConfiguration sex
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName, 
				"sex", 
				"sex", 
				SexRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(sex);

		return masterTableCleaningConfiguration;
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


