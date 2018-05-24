package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;
import java.util.Objects;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;


/**
 * One of the major business classes that describes properties of columns in 
 * an imported data set, which is represented by the 
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration} class.
 * Whereas most of the properties of the 
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration} are 
 * independent of work flow steps, the properties of this configuration class
 * <i>are</i> usually dependent on them.  For example, the <code>clean_field_name</code>
 * property is only relevant in the Clean work flow state.  The property
 * <code>optimise_using_index</code> will only be relevant if the data set reaches
 * the Optimise step of a {@link rifDataLoaderTool.businessConceptLayer.LinearWorkflow}
 *
 * <p>
 * This class is used in two context: as a set of configuration options for a CSV
 * field and as a configuration hint that is used to help set those options.  When
 * it is used as a set of configurations, its core field name will have a name
 * taken from a CSV field.  When it is used as a configuration hint, the name
 * will represent a regular expression pattern.  When the name of a hint is matched
 * with the name of a set of configurations, settings in the hint will be copied into
 * the configuration options.
 * </p>
 * 
 * <p>
 * For example, "age" may be the core field name assigned to a 
 * <code>DataSetFieldConfiguration</code> that comes from a CSV file.  A hint
 * with the core field name "^age$" may be used to find a match with any field
 * which starts with an "a", followed by a a "g", followed by and "e" and then
 * terminates.  In the matching process, settings in the hint 
 * <code>DataSetFieldConfiguration</code> will be used to set corresponding
 * fields in the <code>DataSetFieldConfiguration</code> that describes the 
 * configuration options that will be used for the field "age".
 * </p>
 * 
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public class DataSetFieldConfiguration 
	extends AbstractDataLoaderToolConcept 
	implements DescriptiveConfigurationItem {

	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================

	private boolean isHint;
	
	private boolean isNewRecord;
	
	/**
	 * is the core data set name of the 
	 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration}
	 * that contains this field.
	 */
	private String coreDataSetName;
	
	/*
	 * The core field name of this field
	 */
	private String coreFieldName;
	
	/**
	 * This description will appear as comments in the published database table.
	 */
	private String coreFieldDescription;
	
	/**
	 * Used to transform a text-based column field value into a data type that is
	 * recognised by the RIF database.
	 */
	private RIFDataType rifDataType;
	
	/**
	 * The name of this field when it is imported during the load step.  In a CSV
	 * file, the header row can provide this name.  Otherwise, if a CSV file contains
	 * no header row, an auto-generated field name will be created.
	 */
	private String loadFieldName;
	
	/**
	 * The name that will be used for this field after the loaded table is transformed
	 * into a cleaned table.  Sometimes a RIF manager may want to rename the fields
	 * that appeared in the original data set.
	 */
	private String cleanFieldName;

	/**
	 * The name that will be be used after the cleaned data set is transformed
	 * into a table that is expected to be moved into the RIF database.  In many
	 * cases, the convert field name will be the same as the cleaned field name.
	 * This will happen when RIF managers are importing a field from an original
	 * data set which they want to appear in a published data set but which is not
	 * required by the RIF schema.  
	 * 
	 * The cleaned name and converted name may be different when a cleaned data set
	 * is transformed into a converted one.  In this situation, there are certain
	 * field names that are expected to appear in the converted data set result.
	 * For example, in numerator tables, the RIF expects that a column "year" appears.
	 * It may be called "fiscal_year" in the cleaned version, but the database
	 * procedures that create extract files from study descriptions will expect
	 * that a field called "year" will be in a numerator table.  We recommend that
	 * wherever possible, load, clean and convert field names have the same value.
	 * 
	 */
	private String convertFieldName;
	
	/**
	 * Function that is associated with mapping a cleaned field value to 
	 * a field in the converted table (eg: age, sex --> age_sex_group).
	 */
	private ConversionFunction rifConversionFunction;
	
	/**
	 * Describes what the field does, and is used later on in validation to make
	 * sure that imported tables have a minimum number of fields which serve a 
	 * specific purpose.  For example, a numerator table must have at least one
	 * health code field.  In the Convert work flow step, the
	 * {@link rifDataLoaderTool.businessConceptLayer.WorkflowValidator},
	 * the main class for performing validation checks that are specific to a given
	 * work flow step, checks that at least one field is a health code.  The value
	 * of this field is also used to check things like whether a denominator table
	 * has at least one geographical resolution field and whether a covariate table
	 * has at least one column with the covariate name. 
	 * <ul>
	 * <li>
	 * <b>
	 * </ul>
	 */
	private FieldPurpose fieldPurpose;

	/**
	 * Indicates whether this field should be indexed during the Optimise step.
	 * This field is automatically set to "true" once a data set has been transformed
	 * through the Convert step.  For example, if we know that a table is destined
	 * to be mapped to a numerator table, then we know that table must contain a
	 * field called "year" which is of 
	 * {@link rifDataLoaderTool.businessConceptLayer.rifDataTypes.YearRIFDataType}.
	 * If we know the table will have a year field, then we can automatically index it.
	 * Therefore, if this configuration describes a "year" field, optimiseUsingIndex
	 * should not ever be "false".  This kind of check is done in the Optimise
	 * validation done by the 
	 * {@link rifDataLoaderTool.businessConceptLayer.WorkflowValidator}.
	 */
	private boolean optimiseUsingIndex;
	
	/**
	 * In the Check work flow step, the Data Loader Tool tries to determine whether
	 * a given table row is a duplicate row.  In order to know whether it is or not,
	 * it considers a set of duplicate identification fields.  If two rows have 
	 * identical field values for each of these duplicate identification fields, then
	 * it can identify both rows as duplicates.  It will then mark the first row as
	 * the one to keep and all other duplicates as rows that should be discarded.
	 */
	private boolean isDuplicateIdentificationField;
	
	/**
	 * Describes what data quality checks should appear in the published data set.
	 * For example, the PERCENT_EMPTY 
	 * {@link rifDataLoaderTool.businessConceptLayer.CheckOption} check will 
	 * determine how many of the rows showed an empty value for a given column.
	 */
	private ArrayList<CheckOption> checkOptions;	
	
	/**
	 * Determines whether a field can accept an empty value or not.
	 */
	private boolean isEmptyValueAllowed;
	
	/**
	 * Tells the RIF how important a field is.  Choices include:
	 * <ul>
	 * <li><b>REQUIRED_BY_RIF</b>: a field that is destined to be one of the 
	 * required fields expected in the RIF database schema.  For example,
	 * <code>age_sex_group</code></li>
	 * <li><b>EXTRA_FIELD</b>: a field that may be useful for scientists but
	 * which is not needed by the RIF database procedures that create study
	 * extracts
	 * </li> 
	 * <li>
	 * <b>IGNORE_FIELD</b>: a field that may appear in the load table but which
	 * should not be promoted through the rest of the workflow steps such as
	 * clean, convert, optimise, check and publish.
	 * </li>
	 * </ul>
	 */
	private FieldRequirementLevel fieldRequirementLevel;
	
	private FieldChangeAuditLevel fieldChangeAuditLevel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DataSetFieldConfiguration() {

		initialise("", "");	
	}

	private DataSetFieldConfiguration(
		final String parentRecordName,
		final String coreFieldName) {
		
		initialise(
			parentRecordName, 
			coreFieldName);
	}

	private void initialise(
		final String parentRecordName,
		final String coreFieldName) {
			
		this.isNewRecord = true;
		this.isHint = false;
		this.coreDataSetName = parentRecordName;
			
		this.coreFieldName = coreFieldName;
		loadFieldName = coreFieldName;
		cleanFieldName = coreFieldName;
		convertFieldName = coreFieldName;
		
		
		coreFieldDescription = "";
		rifDataType = RIFDataTypeFactory.RIF_TEXT_DATA_TYPE;
		optimiseUsingIndex = false;
		checkOptions = new ArrayList<CheckOption>();
			
		fieldPurpose = FieldPurpose.OTHER;
		isDuplicateIdentificationField = false;
		isEmptyValueAllowed = true;
		
		rifConversionFunction = null;
		
		fieldRequirementLevel = FieldRequirementLevel.IGNORE_FIELD;	
		fieldChangeAuditLevel = FieldChangeAuditLevel.INCLUDE_FIELD_CHANGE_DESCRIPTION;		
	}

	public static DataSetFieldConfiguration newInstance(
		final String parentRecordName,
		final String coreFieldName) {

		DataSetFieldConfiguration dataSetFieldConfiguration
			= new DataSetFieldConfiguration(
				parentRecordName, 
				coreFieldName);
		return dataSetFieldConfiguration;
	}
	
	public static DataSetFieldConfiguration newInstance() {
		DataSetFieldConfiguration dataSetFieldConfiguration
			= new DataSetFieldConfiguration();
		return dataSetFieldConfiguration;
	}
	
	public static DataSetFieldConfiguration createCopy(
		final DataSetFieldConfiguration originalConfiguration) {
	
		if (originalConfiguration == null) {
			return null;
		}
		DataSetFieldConfiguration cloneConfiguration
			= new DataSetFieldConfiguration("", "");
		copyInto(originalConfiguration, cloneConfiguration);
		
		return cloneConfiguration;
	}	
		
	public static void copyInto(
		final DataSetFieldConfiguration sourceConfiguration,
		final DataSetFieldConfiguration destinationConfiguration) {
	
		destinationConfiguration.setNewRecord(sourceConfiguration.isNewRecord());
		destinationConfiguration.setIsHint(sourceConfiguration.isHint());
		destinationConfiguration.setCoreDataSetName(
				sourceConfiguration.getCoreDataSetName());
		destinationConfiguration.setCoreFieldDescription(sourceConfiguration.getCoreFieldDescription());
		destinationConfiguration.setCoreFieldName(
				sourceConfiguration.getCoreFieldName());
		destinationConfiguration.setLoadFieldName(
				sourceConfiguration.getLoadFieldName());
		destinationConfiguration.setCleanFieldName(
				sourceConfiguration.getCleanFieldName());
		destinationConfiguration.setConvertFieldName(
				sourceConfiguration.getConvertFieldName());
		
		destinationConfiguration.setRIFDataType(sourceConfiguration.getRIFDataType());
		
		ConversionFunction originalRIFConversionFunction
			= sourceConfiguration.getConvertFunction();
		destinationConfiguration.setConvertFunction(originalRIFConversionFunction);
		
		destinationConfiguration.setOptimiseUsingIndex(
			sourceConfiguration.optimiseUsingIndex());
		destinationConfiguration.setEmptyValueAllowed(
			sourceConfiguration.isEmptyValueAllowed());
		destinationConfiguration.setFieldPurpose(
			sourceConfiguration.getFieldPurpose());
		destinationConfiguration.setDuplicateIdentificationField(
			sourceConfiguration.isDuplicateIdentificationField());
				
		destinationConfiguration.setFieldRequirementLevel(
			sourceConfiguration.getFieldRequirementLevel());
		destinationConfiguration.setFieldChangeAuditLevel(
			sourceConfiguration.getFieldChangeAuditLevel());

		
		ArrayList<CheckOption> originalCheckOptions
			= sourceConfiguration.getCheckOptions();
		destinationConfiguration.setCheckOptions(originalCheckOptions);	
		
	}

	public static ArrayList<DataSetFieldConfiguration> createCopy(
		final ArrayList<DataSetFieldConfiguration> originalConfigurations) {
		
		ArrayList<DataSetFieldConfiguration> cloneConfigurations
			= new ArrayList<DataSetFieldConfiguration>();

		for (DataSetFieldConfiguration originalConfiguration : originalConfigurations) {
			DataSetFieldConfiguration cloneConfiguration
				= createCopy(originalConfiguration);
			cloneConfigurations.add(cloneConfiguration);
		}
		
		return cloneConfigurations;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public boolean isNewRecord() {
		return isNewRecord;
	}
	
	public void setNewRecord(boolean isNewRecord) {
		this.isNewRecord = isNewRecord;		
	}
	
	public boolean isHint() {
		return isHint;
	}

	public void setIsHint(final boolean isHint) {
		this.isHint = isHint;
	}
	
	public String getCoreDataSetName() {
		return coreDataSetName;
	}
	
	public void setCoreDataSetName(
		final String coreDataSetName) {
		
		this.coreDataSetName = coreDataSetName;
	}
	
	public String getCoreFieldName() {
		return coreFieldName;
	}

	public void setCoreFieldName(
		final String coreFieldName) {		

		this.coreFieldName = coreFieldName;		
	}

	public String getCoreFieldDescription() {

		return coreFieldDescription;
	}

	public void setCoreFieldDescription(
		final String coreFieldDescription) {

		this.coreFieldDescription = coreFieldDescription;
	}

	public RIFDataType getRIFDataType() {

		return rifDataType;
	}

	public void setRIFDataType(
		final RIFDataType rifDataType) {

		this.rifDataType = rifDataType;
	}

	public String getLoadFieldName() {
		
		return loadFieldName;
	}

	public void setLoadFieldName(
		final String loadFieldName) {

		this.loadFieldName = loadFieldName;
	}

	public String getCleanFieldName() {
		return cleanFieldName;
	}
	
	public void setCleanFieldName(
		final String cleanFieldName) {
		
		this.cleanFieldName = cleanFieldName;
	}
	
	public String getConvertFieldName() {

		return convertFieldName;
	}

	public void setConvertFieldName(
		final String convertFieldName) {

		this.convertFieldName = convertFieldName;
	}

	public void setConvertFunction(
		final ConversionFunction rifConversionFunction) {
		
		this.rifConversionFunction = rifConversionFunction;
	}
	
	public ConversionFunction getConvertFunction() {
		
		return rifConversionFunction;
	}
	
	public boolean optimiseUsingIndex() {
		
		return optimiseUsingIndex;
	}

	public void setOptimiseUsingIndex(
		final boolean optimiseUsingIndex) {

		this.optimiseUsingIndex = optimiseUsingIndex;
	}
	
	public ArrayList<CheckOption> getCheckOptions() {
		
		return checkOptions;
	}
	
	public void addCheckOption(
		final CheckOption rifCheckOption) {
		
		checkOptions.add(rifCheckOption);
	}
	
	
	public void setCheckOptions(
		final ArrayList<CheckOption> checkOptions) {
		
		this.checkOptions = checkOptions;
	}

	
	public FieldPurpose getFieldPurpose() {
		return fieldPurpose;		
	}
	
	public void setFieldPurpose(
		final FieldPurpose fieldPurpose) {

		this.fieldPurpose = fieldPurpose;
	}
		
	public boolean isDuplicateIdentificationField() {
		return isDuplicateIdentificationField;
	}
	
	public void setDuplicateIdentificationField(
		final boolean isDuplicateIdentificationField) {
		
		this.isDuplicateIdentificationField = isDuplicateIdentificationField;
	}
	
	
	public boolean isEmptyValueAllowed() {
		return isEmptyValueAllowed;
	}
	
	public void setEmptyValueAllowed(
		final boolean isEmptyValueAllowed) {

		this.isEmptyValueAllowed = isEmptyValueAllowed;
	}
	
	public FieldRequirementLevel getFieldRequirementLevel() {
		return fieldRequirementLevel;
	}
	
	public void setFieldRequirementLevel(
		final FieldRequirementLevel fieldRequirementLevel) {
		
		this.fieldRequirementLevel = fieldRequirementLevel;
		
		if (fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {
			optimiseUsingIndex = true;
		}
	}
	
	public FieldChangeAuditLevel getFieldChangeAuditLevel() {
		return fieldChangeAuditLevel;
	}
	
	public void setFieldChangeAuditLevel(
		final FieldChangeAuditLevel fieldChangeAuditLevel) {
		
		this.fieldChangeAuditLevel = fieldChangeAuditLevel;
	}
	
	public boolean hasCleaningRules() {
		
		ArrayList<CleaningRule> cleaningRules
			= rifDataType.getCleaningRules();
		if (cleaningRules.isEmpty()) {
			return false;
		}
		
		return true;		
	}
	
	
	static public boolean hasIdenticalContents(
		final ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurationsA, 
		final ArrayList<DataSetFieldConfiguration> dataSetFieldConfigurationsB) {
		
		if (dataSetFieldConfigurationsA == dataSetFieldConfigurationsB) {
			return true;
		}

		if ((dataSetFieldConfigurationsA == null && dataSetFieldConfigurationsB != null) ||
			(dataSetFieldConfigurationsA != null && dataSetFieldConfigurationsB == null)) {
				
			return false;
		}

		int numberOfFieldConfigurationsA = dataSetFieldConfigurationsA.size();
		if (numberOfFieldConfigurationsA != dataSetFieldConfigurationsB.size()) {
			
			return false;
		}
		
		for (int i = 0; i < numberOfFieldConfigurationsA; i++) {
			DataSetFieldConfiguration dataSetFieldConfigurationA
				= dataSetFieldConfigurationsA.get(i);
			DataSetFieldConfiguration dataSetFieldConfigurationB
				= dataSetFieldConfigurationsB.get(i);
			if (dataSetFieldConfigurationA.hasIdenticalContents(dataSetFieldConfigurationB) == false) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean hasIdenticalContents(final DataSetFieldConfiguration otherDataSetFieldConfiguration) {
		if (this == otherDataSetFieldConfiguration) {
			return true;
		}

		if (isNewRecord != otherDataSetFieldConfiguration.isNewRecord()) {
			return false;
		}
		
		if (Objects.deepEquals(
			coreDataSetName, 
			otherDataSetFieldConfiguration.getCoreDataSetName()) == false) {
			
			return false;			
		}
		
		if (Objects.deepEquals(
			coreFieldName, 
			otherDataSetFieldConfiguration.getCoreFieldName()) == false) {
			
			return false;			
		}
		
		if (Objects.deepEquals(
			coreFieldDescription, 
			otherDataSetFieldConfiguration.getCoreFieldDescription()) == false) {
			
			return false;			
		}

		RIFDataType otherRIFDataType
			= otherDataSetFieldConfiguration.getRIFDataType();
		if (rifDataType != otherRIFDataType) {
			if ((rifDataType == null) || (otherRIFDataType == null)) {
				return false;
			}
			else if (rifDataType.hasIdenticalContents(otherRIFDataType) == false) {
				return false;
			}
		}
		
		if (Objects.deepEquals(
			loadFieldName, 
			otherDataSetFieldConfiguration.getLoadFieldName()) == false) {
			
			return false;			
		}
		
		if (Objects.deepEquals(
			cleanFieldName, 
			otherDataSetFieldConfiguration.getCleanFieldName()) == false) {
			
			return false;			
		}
				
		if (Objects.deepEquals(
			convertFieldName, 
			otherDataSetFieldConfiguration.getConvertFieldName()) == false) {
			
			return false;			
		}
		
		if (Objects.deepEquals(
			rifConversionFunction, 
			otherDataSetFieldConfiguration.getConvertFieldName()) == false) {
	
			return false;			
		}
		
		ConversionFunction otherRIFConversionFunction
			= otherDataSetFieldConfiguration.getConvertFunction();
		
		if (rifConversionFunction != otherRIFConversionFunction) {
			if (rifConversionFunction == null) {
				return false;
			}
			else {
				if (rifConversionFunction.hasIdenticalContents(otherRIFConversionFunction) == false) {
					return false;
				}
			}
		}

		if (Objects.deepEquals(
			optimiseUsingIndex, 
			otherDataSetFieldConfiguration.optimiseUsingIndex()) == false) {
	
			return false;			
		}
		
		if (Objects.deepEquals(
			isDuplicateIdentificationField, 
			otherDataSetFieldConfiguration.isDuplicateIdentificationField()) == false) {
		
			return false;			
		}

		FieldPurpose otherFieldPurpose
			= otherDataSetFieldConfiguration.getFieldPurpose();
		if (fieldPurpose != otherFieldPurpose) {
			return false;
		}
		
		ArrayList<CheckOption> otherCheckOptions
			= otherDataSetFieldConfiguration.getCheckOptions();
		if (CheckOption.hasIdenticalContents(checkOptions, otherCheckOptions) == false) {
			return false;
		}

		if (Objects.deepEquals(
			isEmptyValueAllowed, 
			otherDataSetFieldConfiguration.isEmptyValueAllowed()) == false) {
			
			return false;
		}
		
		if (Objects.deepEquals(
			fieldRequirementLevel, 
			otherDataSetFieldConfiguration.getFieldRequirementLevel()) == false) {
			
			return false;			
		}
		
		if (Objects.deepEquals(
			fieldChangeAuditLevel, 
			otherDataSetFieldConfiguration.getFieldChangeAuditLevel()) == false) {
			
			return false;			
		}
		
		return true;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {	
	
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType = getRecordType();
		
		String parentRecordNameLabel
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.parentRecordName.label");
		String coreFieldNameLabel
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.coreFieldName.label");		
		
		String coreFieldDescriptionLabel
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.coreFieldDescription.label");		
				
		String loadFieldNameLabel
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.loadFieldName.label");		
		
		String cleanFieldNameLabel
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.cleanFieldName.label");		
		
		String convertFieldNameLabel
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.convertFieldName.label");		
				
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			parentRecordNameLabel, 
			coreDataSetName);
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			coreFieldNameLabel, 
			coreFieldName);
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			coreFieldDescriptionLabel, 
			coreFieldDescription);
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			loadFieldNameLabel, 
			loadFieldName);
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			cleanFieldNameLabel, 
			cleanFieldName);
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			convertFieldNameLabel, 
			convertFieldName);
	}

	public void checkErrors() 
		throws RIFServiceException {
				
		ArrayList<String> errorMessages = new ArrayList<String>();
		checkEmptyFields(errorMessages);
		countErrors(
				RIFDataLoaderToolError.INVALID_DATA_SET_FIELD_CONFIGURATION,
				errorMessages);
	}
	
	public void checkEmptyFields(
		final ArrayList<String> errorMessages) {
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();

		
		if (fieldValidationUtility.isEmpty(coreDataSetName)) {
			String parentRecordNameLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.parentRecordName.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					parentRecordNameLabel);
			errorMessages.add(errorMessage);	
			
		}

		
		if (fieldValidationUtility.isEmpty(coreFieldName)) {
			String coreFieldNameLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.coreFieldName.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					coreFieldNameLabel);
			errorMessages.add(errorMessage);	
			
		}
		
		/*
		if (fieldValidationUtility.isEmpty(coreFieldDescription)) {

			String coreFieldDescriptionLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.coreFieldDescription.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					coreFieldDescriptionLabel);
			errorMessages.add(errorMessage);		
		}
		*/		
	
		if (rifDataType == null) {
			String rifDataTypeLabel
				= RIFDataLoaderToolMessages.getMessage(
					"abstractRIFDataType.name.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					rifDataTypeLabel);
			errorMessages.add(errorMessage);		
		}
		
		if (fieldValidationUtility.isEmpty(loadFieldName)) {
			String loadFieldNameLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.loadFieldName.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					loadFieldNameLabel);
			errorMessages.add(errorMessage);		
		}
		
		if (fieldPurpose == null) {
			String fieldPurposeLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.fieldPurpose.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					fieldPurposeLabel);
			errorMessages.add(errorMessage);			
		}	
				
		if (fieldRequirementLevel == null) {
			String fieldPurposeLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.fieldRequirementLevel.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					fieldPurposeLabel);
			errorMessages.add(errorMessage);			
		}	

		
		if (fieldChangeAuditLevel == null) {
			String fieldPurposeLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.fieldChangeAuditLevel.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					fieldPurposeLabel);
			errorMessages.add(errorMessage);			
		}			
	}
	
	/**
	 * Assumes that they are both valid when viewed in isolation of other fields
	 * @param anotherField
	 * @return
	 */
	/*
	public ArrayList<String> identifyDifferences(
		final DataSetFieldConfiguration anotherField) {
		
		ArrayList<String> fieldValueDifferences = new ArrayList<String>();
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		
		Collator collator = RIFGenericLibraryMessages.getCollator();
		
		
		String originalRecordLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		String revisedRecordLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		
		//compare values for 'coreDataSetFieldName'
		String coreDataSetLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		String currentFieldDifferenceMessage
			= identifyFieldDifference(
				coreDataSetLabelText, 
				originalRecordLabelText,
				coreDataSetName, 
				revisedRecordLabelText,
				anotherField.getCoreDataSetName());
		if (currentFieldDifferenceMessage != null) {
			fieldValueDifferences.add(currentFieldDifferenceMessage);
		}
		
		
		//compare values for 'coreFieldName'
		String coreFieldNameLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		currentFieldDifferenceMessage
			= identifyFieldDifference(
				coreFieldNameLabelText,
				originalRecordLabelText,
				coreFieldName, 
				revisedRecordLabelText,
				anotherField.getCoreFieldName());
		if (currentFieldDifferenceMessage != null) {
			fieldValueDifferences.add(currentFieldDifferenceMessage);
		}

		
		//compare values for 'coreFieldDescription'
		String coreFieldDescriptionLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		currentFieldDifferenceMessage
			= identifyFieldDifference(
				coreFieldDescriptionLabelText,
				originalRecordLabelText,
				coreFieldDescription, 
				revisedRecordLabelText,
				anotherField.getCoreFieldDescription());
		if (currentFieldDifferenceMessage != null) {
			fieldValueDifferences.add(currentFieldDifferenceMessage);
		}
		
		
		//compare values for 'loadFieldName'
		String loadFieldNameLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		currentFieldDifferenceMessage
			= identifyFieldDifference(
				loadFieldNameLabelText, 
				originalRecordLabelText,
				coreFieldDescription, 
				revisedRecordLabelText,
				anotherField.getCoreFieldDescription());
		if (currentFieldDifferenceMessage != null) {
			fieldValueDifferences.add(currentFieldDifferenceMessage);
		}
		
		
		//compare values for 'cleanFieldName'
		String cleanFieldNameLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		currentFieldDifferenceMessage
			= identifyFieldDifference(
				cleanFieldNameLabelText,
				originalRecordLabelText,
				cleanFieldName,
				revisedRecordLabelText,
				anotherField.getCleanFieldName());
		if (currentFieldDifferenceMessage != null) {
			fieldValueDifferences.add(currentFieldDifferenceMessage);
		}		
		
		//compare values for 'convertFieldName'
		String convertFieldNameLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		currentFieldDifferenceMessage
			= identifyFieldDifference(
				convertFieldNameLabelText,
				originalRecordLabelText,
				convertFieldName, 
				revisedRecordLabelText,
				anotherField.getConvertFieldName());
		if (currentFieldDifferenceMessage != null) {
			fieldValueDifferences.add(currentFieldDifferenceMessage);
		}
		
		//field purpose
		String fieldPurposeLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		FieldPurpose anotherFieldPurpose
			= anotherField.getFieldPurpose();		
		String nullityDifference
			= FieldValidationUtility.compareNullities(
				fieldPurpose, 
				originalRecordLabelText, 
				fieldPurposeLabelText, 
				anotherFieldPurpose, 
				revisedRecordLabelText, 
				fieldPurposeLabelText);
		if (nullityDifference != null) {
			fieldValueDifferences.add(nullityDifference);
		}
		else if ((fieldPurpose != null) && (anotherFieldPurpose != null)) {
			if (fieldPurpose.compareTo(anotherFieldPurpose) != 0) {
				String fieldValueDifference
					= RIFDataLoaderMessages.getMessage(
						"",
						fieldPurpose.getName(),
						anotherFieldPurpose.getName());
				fieldValueDifferences.add(fieldValueDifference);				
			}
		}
		
		String optimiseUsingIndexLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		boolean anotherOptimiseUsingIndex
			= anotherField.optimiseUsingIndex();
		if (optimiseUsingIndex != anotherOptimiseUsingIndex) {
			String fieldValueDifference
				= RIFDataLoaderToolMessages.getMessage(
					"", 
					optimiseUsingIndexLabelText,
					String.valueOf(optimiseUsingIndex),
					String.valueOf(anotherOptimiseUsingIndex));
			fieldValueDifferences.add(fieldValueDifference);
		}

		
		String isDuplicateIdentificationFieldLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		boolean anotherIsDuplicateIdentificationField
			= anotherField.optimiseUsingIndex();
		if (isDuplicateIdentificationField != anotherIsDuplicateIdentificationField) {
			String fieldValueDifference
				= RIFDataLoaderToolMessages.getMessage(
					"", 
					isDuplicateIdentificationFieldLabelText,
					String.valueOf(isEmptyValueAllowed),
					String.valueOf(anotherIsDuplicateIdentificationField));
			fieldValueDifferences.add(fieldValueDifference);
		}
		
		
		//compare collections
		ArrayList<RIFCheckOption> anotherRIFCheckOptionsList
			= anotherField.getCheckOptions();
		ArrayList<String> rifCheckOptionDifferences
			= RIFCheckOption.identifyDifferences(
				checkOptions, 
				originalRecordLabelText, 
				anotherRIFCheckOptionsList, 
				revisedRecordLabelText);
		fieldValueDifferences.addAll(rifCheckOptionDifferences);
		
		String isEmptyAllowedIndexLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		boolean anotherIsEmptyValueAllowed
			= anotherField.optimiseUsingIndex();
		if (isEmptyValueAllowed != anotherOptimiseUsingIndex) {
			String fieldValueDifference
				= RIFDataLoaderToolMessages.getMessage(
					"", 
					isEmptyAllowedIndexLabelText,
					String.valueOf(isEmptyValueAllowed),
					String.valueOf(anotherIsEmptyValueAllowed));
			fieldValueDifferences.add(fieldValueDifference);
		}

		//field requirement level
		String fieldRequirementLevelLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		FieldRequirementLevel anotherFieldRequirementLevel
			= anotherField.getFieldRequirementLevel();		
		nullityDifference
			= FieldValidationUtility.compareNullities(
				fieldRequirementLevel, 
				originalRecordLabelText, 
				fieldRequirementLevelLabelText, 
				anotherFieldRequirementLevel, 
				revisedRecordLabelText, 
				fieldRequirementLevelLabelText);
		if (nullityDifference != null) {
			fieldValueDifferences.add(nullityDifference);
		}
		else if ((fieldRequirementLevel != null) && (anotherFieldRequirementLevel != null)) {
			if (fieldRequirementLevel.compareTo(anotherFieldRequirementLevel) != 0) {
				String fieldValueDifference
					= RIFDataLoaderMessages.getMessage(
						"",
						fieldRequirementLevel.getName(),
						anotherFieldRequirementLevel.getName());
				fieldValueDifferences.add(fieldValueDifference);				
			}
		}

		//KLG: this is a weakness in comparing two rif data type objects, which 
		//are cast in terms of an abstract class.  For now, assume they are
		//the same if their codes match
		String rifDataTypeLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		AbstractRIFDataType anotherRIFDataType
			= anotherField.getRIFDataType();
		nullityDifference
			= FieldValidationUtility.compareNullities(
				rifDataType, 
				originalRecordLabelText, 
				rifDataTypeLabelText, 
				anotherRIFDataType, 
				revisedRecordLabelText, 
				rifDataTypeLabelText);
		if (nullityDifference != null) {
			fieldValueDifferences.add(nullityDifference);
		}
		else if ((rifDataType != null) && (anotherRIFDataType != null)) {
			if (collator.equals(
					rifDataType.getName(), 
					anotherRIFDataType.getName())) {
				String fieldValueDifference
					= RIFDataLoaderToolMessages.getMessage(
						"",
						rifDataTypeLabelText,
						rifDataType.getName(),
						anotherRIFDataType.getName());
				fieldValueDifferences.add(fieldValueDifference);				
			}
		}
		
		//field requirement level
		String fieldChangeAuditLevelLabelText
			= RIFDataLoaderToolMessages.getMessage("");
		FieldChangeAuditLevel anotherFieldChangeAuditLevel
			= anotherField.getFieldChangeAuditLevel();
		nullityDifference
			= FieldValidationUtility.compareNullities(
				fieldRequirementLevel, 
				originalRecordLabelText, 
				fieldRequirementLevelLabelText, 
				anotherFieldRequirementLevel, 
				revisedRecordLabelText, 
				fieldRequirementLevelLabelText);
		if (nullityDifference != null) {
			fieldValueDifferences.add(nullityDifference);
		}
		else if ((fieldChangeAuditLevel != null) && (anotherFieldChangeAuditLevel != null)) {
			if (fieldChangeAuditLevel.compareTo(anotherFieldChangeAuditLevel) != 0) {
				String fieldValueDifference
					= RIFDataLoaderMessages.getMessage(
						"",
						fieldChangeAuditLevel.getName(),
						anotherFieldChangeAuditLevel.getName());
				fieldValueDifferences.add(fieldValueDifference);				
			}
		}
		
		
		return fieldValueDifferences;
	}

	private String identifyFieldDifference(
		final String fieldName,
		final String originalRecordLabelText,
		final String originalFieldValue,
		final String revisedRecordLabelText,
		final String revisedFieldValue) {
		
		String fieldDifference = null;
		
		String nullityDifference
			= FieldValidationUtility.compareNullities(
				originalFieldValue, 
				originalRecordLabelText, 
				fieldName, 
				revisedFieldValue, 
				revisedRecordLabelText, 
				fieldName);
		if (nullityDifference != null) {
			//the difference is that one was null and the other was not
			return nullityDifference;
		}
		
		Collator collator = RIFGenericLibraryMessages.getCollator();
		if (collator.equals(
			originalFieldValue, 
			revisedFieldValue)) {

			//both values are non-null but different
			
			String fieldValueChangeMessage
				= RIFDataLoaderToolMessages.getMessage(
					"",
					fieldName,
					originalFieldValue,
					revisedFieldValue);
			
			return fieldValueChangeMessage;
		}
		
		//no difference detected
		return null;
		
		
		
		
		
	}
	*/
	
	public String getRecordType() {
		String recordType
			= RIFDataLoaderToolMessages.getMessage("dataSetFieldConfiguration.recordType");
		return recordType;		
	}
	
	public void clearCheckOptions() {
		checkOptions.clear();
	}
	
	public void print() {
		FieldPurpose fieldPurpose = getFieldPurpose();
		FieldRequirementLevel fieldRequirementLevel = getFieldRequirementLevel();
		RIFDataType rifDataType = getRIFDataType();
		
		String fieldPurposePhrase = "null";
		if (fieldPurpose != null) {
			fieldPurposePhrase = fieldPurpose.getName();
		}
		String requirementLevelPhrase = "null";
		if (fieldRequirementLevel != null) {
			requirementLevelPhrase = fieldRequirementLevel.getName();
		}
		String rifDataTypePhrase = "null";
		if (rifDataType != null) {
			rifDataTypePhrase = rifDataType.getName();
		}
		
		System.out.println(
			"=" + 
			getConvertFieldName() + 
			"=Purpose=" + 
			fieldPurposePhrase +
			"=Priority=" + 
			requirementLevelPhrase + 
			"=Type=" +
			rifDataTypePhrase +
			"==");
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	public String getDisplayName() {		
		return coreFieldName;
	}
	
	public String getDescription() {
        return coreFieldDescription;		
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


