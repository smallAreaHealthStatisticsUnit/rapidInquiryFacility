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

	public TableCleaningConfiguration createNumeratorTableConfiguration(
		final String coreTableName) {
		
		TableCleaningConfiguration masterTableCleaningConfiguration 
			= TableCleaningConfiguration.newInstance(coreTableName);
		
		DataSource dataSource
			= DataSource.newInstance(
				"hes_2001",
				false,
				"HES file hes2001.csv", 
				"kgarwood");
		dataSource.setIdentifier("1");
		
		masterTableCleaningConfiguration.setDataSource(dataSource);
		
		
		TableFieldCleaningConfiguration nhsNumber
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"nhs_number", 
				NHSNumberRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(nhsNumber);
		
		TableFieldCleaningConfiguration birthDate
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"birth_date", 
				DateRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(birthDate);
		
		TableFieldCleaningConfiguration postalCode
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"postal_code", 
				UKPostalCodeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(postalCode);

		TableFieldCleaningConfiguration age
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"age", 
				AgeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(age);
		
		TableFieldCleaningConfiguration sex
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"sex", 
				SexRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(sex);

		TableFieldCleaningConfiguration level1
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"level1", 
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(level1);
		
		TableFieldCleaningConfiguration level2
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"level2", 
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(level2);
		
		TableFieldCleaningConfiguration level3
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"level3", 
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(level3);

		TableFieldCleaningConfiguration level4
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"level4", 
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(level4);

		TableFieldCleaningConfiguration icd1
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"icd_1", 
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(icd1);
		
		TableFieldCleaningConfiguration icd2
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"icd_2", 
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(icd2);
		
		TableFieldCleaningConfiguration opcsCode1
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"opcs_code_1", 
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(opcsCode1);

		TableFieldCleaningConfiguration total
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"total", 
				IntegerRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(total);

		return masterTableCleaningConfiguration;
		
	}
	
	public TableCleaningConfiguration createAgeSexBirthDateTableConfiguration() {
		String coreTableName = "hes_2001";

		TableCleaningConfiguration masterTableCleaningConfiguration
			= TableCleaningConfiguration.newInstance(coreTableName);

		DataSource dataSource
			= DataSource.newInstance(
				"hes_2001",
				false,
				"HES file hes2001.csv", 
				"kgarwood");
		dataSource.setIdentifier("1");
		masterTableCleaningConfiguration.setDataSource(dataSource);
		
		
		TableFieldCleaningConfiguration birthDate
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"birth_date", 
				DateRIFDataType.newInstance());
		birthDate.setAllowBlankValues(true);
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(birthDate);
	
		TableFieldCleaningConfiguration age
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"age", 
				AgeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(age);
	
		TableFieldCleaningConfiguration sex
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName, 
				"sex", 
				SexRIFDataType.newInstance());
		masterTableCleaningConfiguration.addTableFieldCleaningConfiguration(sex);

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


