package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;
import java.util.HashMap;

import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;


/**
 * Provides a description of a conversion activity in the RIF that can be used
 * by SQL code generators to produce code for different database targets such as
 * Postgresql and Microsoft SQL Server.  In a table conversion configuration, the fields
 * from a cleaned table (one that has been transformed by cleaning rules, evaluated by
 * validation features and cast to database types) are mapped to fields which are expected
 * in various parts of the RIF schema.  Typically one field in the cleaned table will 
 * map to one field in the converted field.  However, some RIF fields such as age-sex group
 * may require a function to combine separate age and sex fields, or derive the age field
 * from birth date and event date column values.  
 * 
 * <p>
 * Each conversion configuration will specify two important kinds of fields:
 * <ul>
 * <li>
 * required fields
 * </li>
 * <li>
 * optional fields
 * </li>
 * </ul>
 *
 *<p>
 * Conversion is an activity where some fields in cleaned table must map to expected fields, whereas
 * some conversion activities may have extra fields which are retained in RIF tables but which are not
 * used by the RIF tool suite.  For example, suppose RIF managers are converting a cleaned table to 
 * a health code table.  They will need to map one field to the required field "label" and another one
 * to a required field called "description".  The conversion activity won't need extra fields.
 * <p>
 * In another example, a numerator table will feature required fields and may also include extra fields. 
 * </p>
 *
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

public final class TableConversionConfiguration {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String coreTableName;
	private ArrayList<TableFieldConversionConfiguration> requiredFieldConfigurations;
	private ArrayList<TableFieldCleaningConfiguration> extraFieldConfigurations;
	
	private HashMap<
		TableFieldConversionConfiguration, 
		ArrayList<TableFieldCleaningConfiguration>> cleaningFromConversionConfiguration;
	private HashMap<
		TableFieldConversionConfiguration,
		String> conversionFunctionNameFromConversionConfiguration;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private TableConversionConfiguration() {
		requiredFieldConfigurations = new ArrayList<TableFieldConversionConfiguration>();
		cleaningFromConversionConfiguration 
			= new HashMap<
				TableFieldConversionConfiguration,
				ArrayList<TableFieldCleaningConfiguration>>();
		conversionFunctionNameFromConversionConfiguration
			= new HashMap<TableFieldConversionConfiguration, String>();
		
		extraFieldConfigurations
			= new ArrayList<TableFieldCleaningConfiguration>();
	}

	public static TableConversionConfiguration newInstance(
		final String coreTableName) {
		TableConversionConfiguration tableConversionConfiguration
			= new TableConversionConfiguration();
		
		return tableConversionConfiguration;
	}
	
	public static TableConversionConfiguration createCopy(
		final TableConversionConfiguration originalConversionConfiguration) {
		
		String coreTableName
			= originalConversionConfiguration.getCoreTableName();
		TableConversionConfiguration cloneConversionConfiguration
			= TableConversionConfiguration.newInstance(coreTableName);
		
		//first, make clones of conversion configurations
		ArrayList<TableFieldConversionConfiguration> originalFieldConversionConfigurations
			= originalConversionConfiguration.getRequiredFieldConfigurations();
		ArrayList<TableFieldConversionConfiguration> cloneFieldConversionConfigurations
			= new ArrayList<TableFieldConversionConfiguration>();
		for (TableFieldConversionConfiguration originalFieldConversionConfiguration : originalFieldConversionConfigurations) {
			
			/*
			 * for each conversion field, we have to do two things:
			 * (1) clone the conversion field
			 * (2) get the associated cleaning field configurations with the original
			 * (3) clone the collection of cleaning field configurations in (2)
			 * (4) obtain the name of any cleaning function associated with the
			 *     conversion field
			 */
			
			//clone conversion fields
			TableFieldConversionConfiguration cloneFieldConversionConfiguration
				= TableFieldConversionConfiguration.createCopy(
					originalFieldConversionConfiguration);

			//get cleaning field configurations associated with original conversion field.
			ArrayList<TableFieldCleaningConfiguration> originalFieldCleaningConfigurations
				= originalConversionConfiguration.getCleaningConfigurations(
					originalFieldConversionConfiguration);
			//clone the collection of cleaning fields
			ArrayList<TableFieldCleaningConfiguration> cloneFieldCleaningConfigurations
				= TableFieldCleaningConfiguration.createCopy(originalFieldCleaningConfigurations);

			//get the name of the conversion function (if any) associated with a
			//conversion field configuration
			String conversionFunctionName
				= originalConversionConfiguration.getConversionFunctionName(
					originalFieldConversionConfiguration);
			
			cloneConversionConfiguration.map(
				cloneFieldConversionConfiguration, 
				cloneFieldCleaningConfigurations,
				conversionFunctionName);
		
		}
		
		//make clones of extra fields
		ArrayList<TableFieldCleaningConfiguration> originalExtraFields
			= originalConversionConfiguration.getExtraFields();
		ArrayList<TableFieldCleaningConfiguration> cloneExtraFields
			= TableFieldCleaningConfiguration.createCopy(originalExtraFields);
		cloneConversionConfiguration.setExtraFields(cloneExtraFields);
		
		return cloneConversionConfiguration;		
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void addRequiredFieldConfiguration(		
		final String conversionTableFieldName,
		final RIFDataTypeInterface rifDataType) {
		
		TableFieldConversionConfiguration fieldConversionConfiguration
			= TableFieldConversionConfiguration.newInstance(
				coreTableName, 
				conversionTableFieldName, 
				rifDataType);
		requiredFieldConfigurations.add(fieldConversionConfiguration);		
	}

	public ArrayList<TableFieldConversionConfiguration> getRequiredFieldConfigurations() {
		
		return requiredFieldConfigurations;
	}

	public ArrayList<TableFieldCleaningConfiguration> getExtraFields() {

		return 	extraFieldConfigurations;
	}
	
	public void setExtraFields(
		final ArrayList<TableFieldCleaningConfiguration> extraFieldConfigurations) {
		
		this.extraFieldConfigurations = extraFieldConfigurations;
	}
	
	public ArrayList<TableFieldCleaningConfiguration> getCleaningConfigurations(
		final TableFieldConversionConfiguration fieldConversionConfiguration) {
		
		return cleaningFromConversionConfiguration.get(fieldConversionConfiguration);
	}
	
	/*
	 * Straight mapping, no conversion function needed.
	 */
	public void map(
		final TableFieldConversionConfiguration fieldConversionConfiguration,
		final TableFieldCleaningConfiguration tableFieldCleaningConfiguration) {
	
		map(fieldConversionConfiguration,
			tableFieldCleaningConfiguration,
			null);	
	}
	
	/*
	 * One to one mapping, with the help of a conversion function
	 */
	public void map(
		final TableFieldConversionConfiguration fieldConversionConfiguration,
		final TableFieldCleaningConfiguration tableFieldCleaningConfiguration,
		final String conversionFunctionName) {
		
		ArrayList<TableFieldCleaningConfiguration> cleaningFieldConfigurations
			= new ArrayList<TableFieldCleaningConfiguration>();
		cleaningFieldConfigurations.add(tableFieldCleaningConfiguration);
		cleaningFromConversionConfiguration.put(
			fieldConversionConfiguration, 
			cleaningFieldConfigurations);

		if (conversionFunctionName != null) {			
			conversionFunctionNameFromConversionConfiguration.put(
				fieldConversionConfiguration, 
				conversionFunctionName);
		}
		
	}

	
	public String getConversionFunctionName(
		final TableFieldConversionConfiguration fieldConversionConfiguration) {
		
		return conversionFunctionNameFromConversionConfiguration.get(
			fieldConversionConfiguration);
	}
	
	/*
	 * One to many mapping, with the help of a conversion function
	 */
	public void map(
		final TableFieldConversionConfiguration fieldConversionConfiguration,
		final ArrayList<TableFieldCleaningConfiguration> tableFieldCleaningConfigurations,
		final String conversionFunctionName) {

		
		cleaningFromConversionConfiguration.put(
			fieldConversionConfiguration, 
			tableFieldCleaningConfigurations);

		if (conversionFunctionName != null) {			
			conversionFunctionNameFromConversionConfiguration.put(
				fieldConversionConfiguration, 
				conversionFunctionName);
		}
	}
		
	public String getCoreTableName() {
		
		return coreTableName;
	}
	
	public void setCoreTableName(
		final String coreTableName) {
		
		this.coreTableName = coreTableName;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		//@TODO fill in later
		
	}

	public void checkErrors() 
		throws RIFServiceException {
		
		//@TODO fill in later
	}
	
	public String getDisplayName() {
		String tableName
			= RIFTemporaryTablePrefixes.CONVERT.getTableName(coreTableName);
		return tableName;
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


