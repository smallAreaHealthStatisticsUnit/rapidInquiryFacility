package rifDataLoaderTool.test;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.AgeRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.DateRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.IntegerRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.NHSNumberRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.SexRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.TextRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.UKPostalCodeRIFDataType;
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
		final String coreDataSetName) {
				
		DataSet dataSet
			= DataSet.newInstance(
				"hes_2001",
				false,
				"HES file hes2001.csv", 
				"kgarwood");
		dataSet.setIdentifier("1");
		dataSet.setCoreDataSetName(coreDataSetName);
		
		CleanWorkflowConfiguration masterTableCleaningConfiguration 
			= CleanWorkflowConfiguration.newInstance(dataSet);
		
		
		CleanWorkflowFieldConfiguration nhsNumber
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"nhs_number", 
				"nhs_number",
				"A number used to identify a patient in the UK's NHS system",
				NHSNumberRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(nhsNumber);
		
		CleanWorkflowFieldConfiguration birthDate
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"birth_date", 
				"birth_date",
				"birth date of patient",
				DateRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(birthDate);
		
		CleanWorkflowFieldConfiguration postalCode
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"postal_code", 
				"postal code",
				"UK postal code",
				UKPostalCodeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(postalCode);

		CleanWorkflowFieldConfiguration age
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"age", 
				"age",
				"age of patient",
				AgeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(age);
		
		CleanWorkflowFieldConfiguration sex
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"sex", 
				"sex",
				"Sex of patient",
				SexRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(sex);

		CleanWorkflowFieldConfiguration level1
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"level1", 
				"level1",
				"Most general level of resolution",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(level1);
		
		CleanWorkflowFieldConfiguration level2
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"level2",
				"level2",
				"Level 2 geographical resolution",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(level2);
		
		CleanWorkflowFieldConfiguration level3
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"level3", 
				"level3",
				"Level 3 geographical resolution",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(level3);

		CleanWorkflowFieldConfiguration level4
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"level4", 
				"level4",
				"Level 4 geographical resolution",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(level4);

		CleanWorkflowFieldConfiguration icd1
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"icd_1", 
				"icd_1",
				"icd code 1",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(icd1);
		
		CleanWorkflowFieldConfiguration icd2
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"icd_2", 
				"icd_2",
				"icd code 2",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(icd2);
		
		CleanWorkflowFieldConfiguration opcsCode1
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"opcs_code_1", 
				"opcs code 1",
				"OPCS field 1",
				TextRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(opcsCode1);

		CleanWorkflowFieldConfiguration total
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"total", 
				"total",
				"total cases",
				IntegerRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(total);

		return masterTableCleaningConfiguration;
		
	}
	
	public CleanWorkflowConfiguration createAgeSexBirthDateTableConfiguration() {
		DataSet dataSet
			= DataSet.newInstance(
				"hes_2001",
				false,
				"HES file hes2001.csv", 
				"kgarwood");
		dataSet.setIdentifier("1");

		CleanWorkflowConfiguration masterTableCleaningConfiguration
			= CleanWorkflowConfiguration.newInstance(dataSet);
		
		CleanWorkflowFieldConfiguration birthDate
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"birth_date", 
				"birth date",
				"birth date of patient",
				DateRIFDataType.newInstance());
		birthDate.setAllowBlankValues(true);
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(birthDate);
	
		CleanWorkflowFieldConfiguration age
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"age",
				"age",
				"age of patient",
				AgeRIFDataType.newInstance());
		masterTableCleaningConfiguration.addCleanWorkflowFieldConfiguration(age);
	
		CleanWorkflowFieldConfiguration sex
			= CleanWorkflowFieldConfiguration.newInstance(
				dataSet, 
				"sex", 
				"sex", 
				"sex of patient",
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


