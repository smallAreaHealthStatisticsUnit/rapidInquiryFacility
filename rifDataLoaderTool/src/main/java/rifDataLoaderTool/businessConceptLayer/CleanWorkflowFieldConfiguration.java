package rifDataLoaderTool.businessConceptLayer;




import java.util.ArrayList;

import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;

/**
 * Cleaning takes values which appear in a table created in the load phase of processing and
 * transforms them into values that can be used in subsequent steps.  The activity is done in 
 * three main steps: (1) search and replace substitutions (2) validate changed table and (3) cast 
 * valid values to appropriate database data types implied by the RIF data type associated with
 * each field.  The fourth step is creating an audit trail that records which fields were blank,
 * errors or changes made to original values. 
 * <p>
 * For example, consider a load table that has a text-based column called sex.  Most
 * of its values are "M", "F", "male", "female", "0", and "1" but we want to have them as
 * numeric values 0, 1, or 2.  
 * </p>
 * 
 * <p>
 * As part of Step 1, we might use a set of cleaning rules which specify
 * search and replace conditions that would correct text-based entries.  Once the search and replace
 * actions have been applied, the resulting table may still have errors in it.  There are two reasons
 * for this:
 * <ul>
 * <li>
 * the cleaning rules could find a String and convert it to a value which is not allowed by the type
 * </li>
 * </li>
 * the cleaning rules may have missed examples of field values that would remain invalid
 * </li>
 * </ul>
 * </p>
 * <p>
 * In Step 2, the values would be validated, often against one or more regular expressions but sometimes
 * using database functions as well. The resulting table would contain values which were either legitimate
 * or had the value "rif_error".  In Step 3, valid values would be correctly cast to database field types
 * such as integer, double, date etc.  Field values which were blank or contained "rif_error" would be
 * carried forth to the casted table as null values.
 * </p>
 * 
 * <p>
 * Although most of the information used to validate field values comes from the 
 * {@link rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface}, each cleaning field configuration contains
 * a flag which indicates whether a value is required or not.  In one field configuration, an
 * {@link rifDataLoaderTool.businessConceptLayer.IntegerRIFDataType} might be optional whereas in another
 * table column it may be required.  This scenario explains why the flag for 'allows blank' is in the field
 * configuration and not in the data type definition.
 * </p>
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

public final class CleanWorkflowFieldConfiguration 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private String coreTableName;
	private String loadTableFieldName;
	private String cleanedTableFieldName;
	private String description;
	private RIFDataTypeInterface rifDataType;
	private boolean allowBlankValues;
	
	private boolean applyDataTypeCleaningRules;
	
	
	private boolean includeFieldInRIFProcessing;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CleanWorkflowFieldConfiguration(
		final String coreTableName,
		final String loadTableFieldName,
		final String cleanedTableFieldName,
		final String description,
		final RIFDataTypeInterface rifDataType) {
		
		this.coreTableName = coreTableName;
		this.loadTableFieldName = loadTableFieldName;
		this.cleanedTableFieldName = cleanedTableFieldName;
		this.description = description;
		this.rifDataType = rifDataType;
		
		applyDataTypeCleaningRules = true;
		allowBlankValues = true;
		includeFieldInRIFProcessing = true;
	}

	public static CleanWorkflowFieldConfiguration newInstance() {
		
		CleanWorkflowFieldConfiguration tableFieldCleaningConfiguration
			= new CleanWorkflowFieldConfiguration(
				"",
				"",
				"",
				"",
				null);
		
		return tableFieldCleaningConfiguration;
	}
	
	public static CleanWorkflowFieldConfiguration newInstance(
		final String coreTableName,
		final String loadTableFieldName,
		final String cleanedTableFieldName,
		final String description,
		final RIFDataTypeInterface rifDataType) {
		
		CleanWorkflowFieldConfiguration tableFieldCleaningConfiguration
			= new CleanWorkflowFieldConfiguration(
				coreTableName,
				loadTableFieldName,
				cleanedTableFieldName,
				description,
				rifDataType);
		
		return tableFieldCleaningConfiguration;
	}

	public static CleanWorkflowFieldConfiguration newInstance(
		final String coreTableName,
		final String coreFieldName,
		final String description) {
			
		TextRIFDataType rifDataType = TextRIFDataType.newInstance();
		CleanWorkflowFieldConfiguration tableFieldCleaningConfiguration
			= new CleanWorkflowFieldConfiguration(
				coreTableName,
				coreFieldName,
				coreFieldName,
				description,
				rifDataType);
		
		return tableFieldCleaningConfiguration;
	}
	
	public static CleanWorkflowFieldConfiguration newInstance(
		final String coreTableName,
		final String coreFieldName,
		final String description,
		final RIFDataTypeInterface rifDataType) {
				
		CleanWorkflowFieldConfiguration tableFieldCleaningConfiguration
			= new CleanWorkflowFieldConfiguration(
				coreTableName,
				coreFieldName,
				coreFieldName,
				description,
				rifDataType);
			
		return tableFieldCleaningConfiguration;
	}
	
	public static CleanWorkflowFieldConfiguration createCopy(
		final CleanWorkflowFieldConfiguration originalFieldConfiguration) {
		
		if (originalFieldConfiguration == null) {
			return null;
		}

		String coreTableName = originalFieldConfiguration.getCoreTableName();
		
		String loadTableFieldName
			= originalFieldConfiguration.getLoadTableFieldName();
		String description
			= originalFieldConfiguration.getDescription();
		String cleanedTableFieldName
			= originalFieldConfiguration.getCleanedTableFieldName();
		RIFDataTypeInterface originalRIFDataType
			= originalFieldConfiguration.getRifDataType();
		RIFDataTypeInterface clonedRIFDataType
			= originalRIFDataType.createCopy();
		CleanWorkflowFieldConfiguration cloneFieldConfiguration
			= new CleanWorkflowFieldConfiguration(
				coreTableName,
				loadTableFieldName,
				cleanedTableFieldName,
				description,
				clonedRIFDataType);
		
		return cloneFieldConfiguration;		
	}
	
	public static ArrayList<CleanWorkflowFieldConfiguration> createCopy(
		final ArrayList<CleanWorkflowFieldConfiguration> originalFieldConfigurations) {
		
		if (originalFieldConfigurations == null) {
			return null;
		}

		ArrayList<CleanWorkflowFieldConfiguration> cloneFieldConfigurations
			= new ArrayList<CleanWorkflowFieldConfiguration>();
		
		if (originalFieldConfigurations.size() == 0) {
			return cloneFieldConfigurations;
		}

		//the core table name should be the same across all fields
		//KLG: @TODO does the field configuration need to have it?
		String coreTableName = originalFieldConfigurations.get(0).getCoreTableName();
		
		for (CleanWorkflowFieldConfiguration originalFieldConfiguration : originalFieldConfigurations) {
			String loadTableFieldName
				= originalFieldConfiguration.getLoadTableFieldName();
			
			String cleanedTableFieldName
				= originalFieldConfiguration.getCleanedTableFieldName();
			String originalDescription
				= originalFieldConfiguration.getDescription();
			RIFDataTypeInterface originalRIFDataType
				= originalFieldConfiguration.getRifDataType();
			RIFDataTypeInterface clonedRIFDataType
				= originalRIFDataType.createCopy();
			CleanWorkflowFieldConfiguration cloneFieldConfiguration
				= new CleanWorkflowFieldConfiguration(
					coreTableName,
					loadTableFieldName,
					cleanedTableFieldName,
					originalDescription,
					clonedRIFDataType);
			cloneFieldConfigurations.add(cloneFieldConfiguration);
		}
		
		
		return cloneFieldConfigurations;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public boolean includeFieldInRIFprocessing() {
		return includeFieldInRIFProcessing;
	}
	
	public void setIncludeFieldInRIFProcessing(
		final boolean includeFieldInRIFProcessing) {
		
		this.includeFieldInRIFProcessing = includeFieldInRIFProcessing;
	}

	public void setAllowBlankValues(
		final boolean allowBlankValues) {
		
		this.allowBlankValues = allowBlankValues;
	}
	
	public boolean allowBlankValues() {

		return allowBlankValues;
	}
	
	
	
	public boolean applyDataTypeCleaningRules() {
		return applyDataTypeCleaningRules;
	}
	
	public void setApplyDataTypeCleaningRules(boolean applyDataTypeCleaningRules) {
		this.applyDataTypeCleaningRules = applyDataTypeCleaningRules;
	}
	
	
	public boolean hasCleaningRules() {
		if (applyDataTypeCleaningRules == false) {
			return false;
		}
		
		if (rifDataType.getCleaningRules().size() == 0) {
			return false;
		}
		
		return true;
	}
	
	public String getCoreTableName() {
		return coreTableName;
	}

	public void setCoreTableName(String coreTableName) {
		this.coreTableName = coreTableName;
	}

	
	public String getLoadTableFieldName() {
		return loadTableFieldName;
	}
	
	public void setLoadTableFieldName(String loadTableFieldName) {
		this.loadTableFieldName = loadTableFieldName;
	}

	public String getCleanedTableFieldName() {
		return cleanedTableFieldName;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(final String description) {
		
		this.description = description;
	}
	
	public void setCleanedTableFieldName(
		final String cleanedTableFieldName) {
		
		this.cleanedTableFieldName = cleanedTableFieldName;
	}

	public RIFDataTypeInterface getRifDataType() {
		return rifDataType;
	}



	public void setRifDataType(RIFDataTypeInterface rifDataType) {
		this.rifDataType = rifDataType;
	}


	@Override
	public String getDisplayName() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(coreTableName);
		buffer.append(".");
		buffer.append(cleanedTableFieldName);
		
		return buffer.toString();
		
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================


	@Override
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		//@TODO
	}
	
	@Override
	public void checkErrors() 
		throws RIFServiceException {

		//@TODO
		
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


