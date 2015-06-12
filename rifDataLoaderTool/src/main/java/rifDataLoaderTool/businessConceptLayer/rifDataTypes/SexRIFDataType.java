package rifDataLoaderTool.businessConceptLayer.rifDataTypes;


import rifDataLoaderTool.businessConceptLayer.CleaningRule;
import rifDataLoaderTool.businessConceptLayer.RIFFieldCleaningPolicy;
import rifDataLoaderTool.businessConceptLayer.RIFFieldValidationPolicy;
import rifDataLoaderTool.system.RIFDataLoaderMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
/**
 * A data type for Sex.  Note that different data sources may have different codings for sex.
 * In SAHSU, sex has values '1' or '2'.  But in data sets such as the maternity records for 
 * England's Hospital Episode Statistics, sex may have values which include: 1 for male, 2
 * for female, 9 for not specified and 0 for not known. Therefore, RIF managers will be encouraged
 * to use custom data types to accommodate the concept of sex as it is supported in other
 * sources of health data.
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

public final class SexRIFDataType 
	extends AbstractRIFDataType {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	private SexRIFDataType(
		final String identifier,
		final String name,
		final String description) {

		super(
			identifier, 
			name, 
			description);
		
		addValidationExpression("^[0|1|2|3]");

		String name1 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.name1");
		String description1
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description1");
		CleaningRule cleaningRule1
			= CleaningRule.newInstance(
				name1, 
				description1, 
				"^female|FEMALE$", 
				"1", 
				true);
		addCleaningRule(cleaningRule1);
			
		String name2 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.name2");
		String description2
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description2");
		CleaningRule cleaningRule2
			= CleaningRule.newInstance(
				name2, 
				description2, 
				"^male|MALE$", 
				"0", 
				true);
		addCleaningRule(cleaningRule2);

		String name3 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.name3");
		String description3
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description3");		
		CleaningRule cleaningRule3
			= CleaningRule.newInstance(
				name3, 
				description3, 
				"^hermaphrodite|HERMAPHRODITE$", 
				"2", 
				true);
		addCleaningRule(cleaningRule3);

		String name4 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.name4");
		String description4
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description4");		
		CleaningRule cleaningRule4
			= CleaningRule.newInstance(
				name4, 
				description4, 
				"^unknown|UNKNOWN$", 
				"3", 
				true);
		addCleaningRule(cleaningRule4);
				
		String name5 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.name5");
		String description5
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description5");		
		CleaningRule cleaningRule5
			= CleaningRule.newInstance(
				name5, 
				description5, 
				"^[fF]$", 
				"1", 
				true);
		addCleaningRule(cleaningRule5);
				
		String name6 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.name6");
		String description6
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description6");		
		CleaningRule cleaningRule6
			= CleaningRule.newInstance(
				name5, 
				description5, 
				"^[mM]$", 
				"0", 
				true);
		addCleaningRule(cleaningRule6);
		
		String name7 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.name7");
		String description7
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description7");		
		CleaningRule cleaningRule7
			= CleaningRule.newInstance(
				name7, 
				description7, 
				"^[hH]$", 
				"2", 
				true);
		addCleaningRule(cleaningRule7);

		String name8 
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.name8");
		String description8
			= RIFDataLoaderToolMessages.getMessage("rifDataType.sex.description8");		
		CleaningRule cleaningRule8
			= CleaningRule.newInstance(
				name8, 
				description8, 
				"^[uU]$", 
				"3", 
				true);
		addCleaningRule(cleaningRule8);
		
		
		setFieldValidationPolicy(RIFFieldValidationPolicy.VALIDATION_RULES);
		setFieldCleaningPolicy(RIFFieldCleaningPolicy.CLEANING_RULES);		

	}

	public static SexRIFDataType newInstance() {

		String name
			= RIFDataLoaderMessages.getMessage("rifDataType.sex.label");
		String description
			= RIFDataLoaderMessages.getMessage("rifDataType.sex.description");
		
		/**
		 * 0 = male
		 * 1 = female
		 * 2 = hermaphrodite
		 * 3 = unknown
		 */
		SexRIFDataType ageRIFDataType
			= new SexRIFDataType(
				"rif_sex",
				name, 
				description);

		
		return ageRIFDataType;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
	
	public RIFDataTypeInterface createCopy() {
		SexRIFDataType cloneSexRIFDataType = newInstance();
		copyAttributes(cloneSexRIFDataType);
		return cloneSexRIFDataType;
	}
	
}


