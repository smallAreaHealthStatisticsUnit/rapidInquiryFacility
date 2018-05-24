package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Objects;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

/**
 * This is one of the major business classes that describes properties
 * associated with the columns of a data set.  Each data set that is imported
 * into the RIF database has the following properties:
 * <ul>
 * <li>
 * <b>name</b>: used as a base identifier that can track the data set through
 * various transformations.  In most of the work flow steps, a prefix and the
 * name will be used to produce a temporary table.  For example, "convert_myDataSet1"
 * will be used to hold the results of transforming the cln_cast_myDataSet1 table
 * that is produced during the cleaning step.
 * </li>
 * <li>
 * <b>version</b>: used along with name to make a unique identifier for the data set
 * </li>
 * <li>
 * <b>description</b>: comments that will be used to describe the table when it is 
 * published and is available to RIF users
 * </li>
 * <b>rifSchemaArea</b>: a {@link rifDataLoaderTool.businessConceptLayer.RIFSchemaArea}
 * that describes the ultimate destination in the RIF schema where the CSV file is 
 * supposed to go.
 * <li>
 * <b>currentWorkFlowState</b>: indicates the last work flow step that was successfully
 * applied to transform the imported data set.
 * </li>
 * </ul>
 * 
 * <p>
 * Each data set has a collection of 
 * {@link DataSetFieldConfiguration} field
 * configurations, each of which have their own properties.
 * </p>
 *
 * <p>
 * This class, like 
 * {@link DataSetFieldConfiguration},
 * is used in two contexts: to define a configurable aspect of a CSV file and
 * to define a hint that can be used to automatically configure aspects of a CSV
 * file.
 * </p>
 * 
 * <p>
 * When the {@link CSVFileSelectionDialog}
 * class imports a CSV file, it creates an instance of a 
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration} that describes
 * the configuration options that will be used to transform the CSV file into a data
 * set that can be used by the RIF.
 * </p>
 * <p>
 * Initially we just let an end-user fill in the configuration options for each field
 * of the data set configuration.  However, we soon realised that when a file had a 
 * large number of fields, it would be impractical for users to set every configuration
 * option.
 * </p>
 * 
 * <p>
 * When it is used as a configuration hint, the data set configuration is used help
 * set configuration options automatically.  Its name becomes a regular expression 
 * which is compared against the name of a given data set configuration.  If there
 * is a match, the field values in the hint are copied into the respective fields
 * of the data set configuration.
 * </p>
 * 
 * <p>
 * For example, consider a CSV file called "cancer_data.csv" that needs to be imported
 * into the RIF.  The CSVFileSelectionDialog will allow the user to specify the file.
 * The dialog will then use the file base name "cancer_data" and the names of its 
 * fields to generate a skeleton instance of a DataSetConfiguration class.
 * </p>
 * <p>
 * Now consider a hint that isan instance of
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration} where the name
 * is <code>^cancer*</code> and it has its {@link rifDataLoadertool.businessConceptLayer.RIFSchemaArea}
 * set to <code>HEALTH_NUMERATOR_DATA</code>.
 * 
 * <p>
 * After the <code>cancer_data</code> <code>DataSetConfiguration</code> has been
 * created, a {@link ConfigurationHints} object
 * is used to match "cancer_data" with the regular expression <code>^cancer*</code>.
 * After the hint is applied, the <code>cancer_data</code> data set configuration will
 * have a <code>RIFSchemaArea</code> that is set to <code>HEALTH_NUMERATOR_DATA</code>.
 * </p>
 * 
 * <p>
 * Therefore, this class is used in two contexts: one in which it represents
 * a complete set of configuration options for a data set and another where it is
 * used to define fragments of configuration settings that are applied based on 
 * matches of regular expressions with the name of another configuration.
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

public class DataSetConfiguration 
	extends AbstractDataLoaderToolConcept 
	implements DescriptiveConfigurationItem {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	
	private boolean isNewRecord;
	private String name;
	private String version;
	private String description;
	private String filePath;
	private RIFSchemaArea rifSchemaArea;
	private Geography geography;
	private HealthTheme healthTheme;
	private ArrayList<DataSetFieldConfiguration> fieldConfigurations;
	public WorkflowState currentWorkflowState;
	private boolean fileHasFieldNamesDefined;
	private boolean isHint;

	private DataSetConfiguration dependencyDataSetConfiguration;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DataSetConfiguration() {
		isNewRecord = true;
		currentWorkflowState = WorkflowState.START;
		version = "1.0";
		fieldConfigurations = new ArrayList<DataSetFieldConfiguration>();
		filePath = "";
		fileHasFieldNamesDefined = false;
		isHint = false;
		
		dependencyDataSetConfiguration = null;
	}
	
	
	/*
	 * Used when imported data does not specify field names
	 */
	public static DataSetConfiguration newInstance(
		final String name,
		final int numberOfFields) {
		
		DataSetConfiguration dataSetConfiguration
			= new DataSetConfiguration();
		dataSetConfiguration.setName(name);
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= new ArrayList<DataSetFieldConfiguration>();
		
		String baseDefaultFieldName
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetFieldConfiguration.baseDefaultFieldName");
		for (int i = 0; i < numberOfFields; i++) {
			String defaultCoreFieldName
				= baseDefaultFieldName + String.valueOf(i + 1);
			DataSetFieldConfiguration dataSetFieldConfiguration
				= DataSetFieldConfiguration.newInstance(
					name, 
					defaultCoreFieldName);
			fieldConfigurations.add(dataSetFieldConfiguration);
		}
		
		dataSetConfiguration.setFieldConfigurations(fieldConfigurations);
		
		return dataSetConfiguration;
		
	}

	public static DataSetConfiguration newInstance() {
		DataSetConfiguration dataSetConfiguration
			= new DataSetConfiguration();
		return dataSetConfiguration;
	}

	public static DataSetConfiguration newInstance(
		final String name,
		final String[] fieldNames) {
		
		DataSetConfiguration dataSetConfiguration
			= new DataSetConfiguration();
		dataSetConfiguration.setName(name);
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= new ArrayList<DataSetFieldConfiguration>();
		
		for (String fieldName : fieldNames) {
			DataSetFieldConfiguration dataSetFieldConfiguration
				= DataSetFieldConfiguration.newInstance(
					name,
					fieldName);
			fieldConfigurations.add(dataSetFieldConfiguration);
		}
		
		dataSetConfiguration.setFieldConfigurations(fieldConfigurations);
		return dataSetConfiguration;		
	}

	public static ArrayList<DataSetConfiguration> createCopy(
		final ArrayList<DataSetConfiguration> originalDataSetConfigurations) {
		
		ArrayList<DataSetConfiguration> cloneDataSetConfigurations
			= new ArrayList<DataSetConfiguration>();
		
		for (DataSetConfiguration originalDataSetConfiguration : originalDataSetConfigurations) {
			cloneDataSetConfigurations.add(createCopy(originalDataSetConfiguration));
		}
		
		return cloneDataSetConfigurations;
	}
	
	
	public static DataSetConfiguration createCopy(
		final DataSetConfiguration originalDataSetConfiguration) {

		DataSetConfiguration cloneDataSetConfiguration
			= new DataSetConfiguration();
		copyInto(
			originalDataSetConfiguration, 
			cloneDataSetConfiguration);
				
		return cloneDataSetConfiguration;
	}
		
	public static void copyInto(
		final DataSetConfiguration sourceDataSetConfiguration,
		final DataSetConfiguration destinationDataSetConfiguration) {
		
		destinationDataSetConfiguration.setNewRecord(
			sourceDataSetConfiguration.isNewRecord());
		destinationDataSetConfiguration.setName(
			sourceDataSetConfiguration.getName());		
		destinationDataSetConfiguration.setVersion(
			sourceDataSetConfiguration.getVersion());
		destinationDataSetConfiguration.setFilePath(
			sourceDataSetConfiguration.getFilePath());		
		destinationDataSetConfiguration.setDescription(
			sourceDataSetConfiguration.getDescription());
		destinationDataSetConfiguration.setFileHasFieldNamesDefined(
			sourceDataSetConfiguration.fileHasFieldNamesDefined());
		destinationDataSetConfiguration.setCurrentWorkflowState(
			sourceDataSetConfiguration.getCurrentWorkflowState());
		destinationDataSetConfiguration.setRIFSchemaArea(
			sourceDataSetConfiguration.getRIFSchemaArea());
		destinationDataSetConfiguration.setGeography(
			sourceDataSetConfiguration.getGeography());
		destinationDataSetConfiguration.setHealthTheme(
			sourceDataSetConfiguration.getHealthTheme());
		destinationDataSetConfiguration.setLastModifiedTime(
			sourceDataSetConfiguration.getLastModifiedTime());
		
		destinationDataSetConfiguration.setIsHint(
			sourceDataSetConfiguration.isHint());
		ArrayList<DataSetFieldConfiguration> originalFieldConfigurations
			= sourceDataSetConfiguration.getFieldConfigurations();
		ArrayList<DataSetFieldConfiguration> cloneFieldConfigurations
			= DataSetFieldConfiguration.createCopy(originalFieldConfigurations);	
		
		destinationDataSetConfiguration.setFieldConfigurations(cloneFieldConfigurations);

		//We don't clone the dependency, we share the same reference across
		//all the copies.
		DataSetConfiguration dependencyDataSetConfiguration
			= sourceDataSetConfiguration.getDependencyDataSetConfiguration();
		destinationDataSetConfiguration.setDependencyDataSetConfiguration(dependencyDataSetConfiguration);
		destinationDataSetConfiguration.setGeography(sourceDataSetConfiguration.getGeography());
	}
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setDependencyDataSetConfiguration(
		final DataSetConfiguration dependencyDataSetConfiguration) {
		
		this.dependencyDataSetConfiguration = dependencyDataSetConfiguration;
	}
		
	public DataSetConfiguration getDependencyDataSetConfiguration() {
		return dependencyDataSetConfiguration;
	}	
	
	public boolean isNewRecord() {
		return isNewRecord;
	}
	
	public void setNewRecord(boolean isNewRecord) {
		this.isNewRecord = isNewRecord;
	}
	
	public String getName() {
		
		return name;
	}

	public void setName(
		final String name) {
		
		this.name = name;
	}

	public String getVersion() {
		return version;
	}
	
	public void setVersion(
		final String version) {

		this.version = version;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(
		final String filePath) {
		
		this.filePath = filePath;
	}
	
	
	public String getDescription() {
		
		return description;
	}

	public void setDescription(
		final String description) {
		
		this.description = description;
	}

	public boolean fileHasFieldNamesDefined() {	
		
		return fileHasFieldNamesDefined;
	}
	
	public void setFileHasFieldNamesDefined(
		final boolean fileHasFieldNamesDefined) {
		
		this.fileHasFieldNamesDefined = fileHasFieldNamesDefined;
	}
	
	public WorkflowState getCurrentWorkflowState() {
		return currentWorkflowState;
	}
	
	public void setCurrentWorkflowState(
		final WorkflowState currentWorkflowState) {
		
		this.currentWorkflowState = currentWorkflowState;
	}
	
	public RIFSchemaArea getRIFSchemaArea() {
		
		return rifSchemaArea;
	}

	public void setRIFSchemaArea(
		final RIFSchemaArea rifSchemaArea) {

		this.rifSchemaArea = rifSchemaArea;
	}

	public Geography getGeography() {
		return geography;
	}
	
	public void setGeography(final Geography geography) {
		this.geography = geography;
	}
	
	public HealthTheme getHealthTheme() {
		return healthTheme;
	}
	
	public void setHealthTheme(final HealthTheme healthTheme) {
		this.healthTheme = healthTheme;
	}
	
	public int getIndexForField(
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
		
		
		return fieldConfigurations.indexOf(dataSetFieldConfiguration);
	}
	
	public void replaceField(
		final DataSetFieldConfiguration originalDataSetFieldConfiguration,
		final DataSetFieldConfiguration revisedDataSetFieldConfiguration) {
		
		int index
			= fieldConfigurations.indexOf(originalDataSetFieldConfiguration);
		if (index != -1) {
			fieldConfigurations.set(index, revisedDataSetFieldConfiguration);
		}
	}
	
	public ArrayList<DataSetFieldConfiguration> getFieldConfigurations() {
		
		return fieldConfigurations;
	}

	public DataSetFieldConfiguration getFieldConfiguration(
		final int index) {
		
		return fieldConfigurations.get(index);		
	}
	
	public void addFieldConfiguration(
		final DataSetFieldConfiguration dataSetFieldConfiguration) {
		
		dataSetFieldConfiguration.setCoreDataSetName(name);
		fieldConfigurations.add(dataSetFieldConfiguration);
		
	}
	
	public void deleteField(final String convertFieldName) {
	
		int numberOfFieldConfigurations = fieldConfigurations.size();
		for (int i = 0; i < numberOfFieldConfigurations; i++) {
			DataSetFieldConfiguration fieldConfiguration
				= fieldConfigurations.get(i);
			String currentConvertFieldName
				= fieldConfiguration.getConvertFieldName();
			if (currentConvertFieldName.equals(convertFieldName)) {
				fieldConfigurations.remove(i);
				break;
			}
		}		
	}
	
	public void setFieldConfigurations(
		final ArrayList<DataSetFieldConfiguration> fieldConfigurations) {
		
			this.fieldConfigurations = fieldConfigurations;
			
			for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
				fieldConfiguration.setCoreDataSetName(name);
			}
	}
	
	public String[] getConvertFieldNames() {
		ArrayList<String> convertFieldNames
			= new ArrayList<String>();
		
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			convertFieldNames.add(fieldConfiguration.getConvertFieldName());
		}
		
		String[] results
			= convertFieldNames.toArray(new String[0]);
		return results;
	}
	
	public int getTotalFieldCount() {
		return fieldConfigurations.size();
	}
	
	/**
	 * Here, order of elements in corresponding arrays is not considered
	 * important.
	 * @param dataSetConfigurationsA
	 * @param dataSetConfigurationsB
	 * @return
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<DataSetConfiguration> dataSetConfigurationsA,
		final ArrayList<DataSetConfiguration> dataSetConfigurationsB) {

		if (dataSetConfigurationsA == dataSetConfigurationsB) {
			return true;
		}
		
		if ( (dataSetConfigurationsA == null && dataSetConfigurationsB != null) ||
			 (dataSetConfigurationsA != null && dataSetConfigurationsB == null)) {
			return false;
		}
		
		if (dataSetConfigurationsA.size() != dataSetConfigurationsB.size()) {
			return false;
		}

		return true;
	}
	
	public boolean hasIdenticalContents(
		final DataSetConfiguration otherDataSetConfiguration) {
		
		if (otherDataSetConfiguration == null) {
			return false;
		}

		if (this == otherDataSetConfiguration) {
			return true;
		}
		
		if (Objects.deepEquals(
			isNewRecord, 
			otherDataSetConfiguration.isNewRecord()) == false) {
			
			return false;
		}
				
		if (Objects.deepEquals(
			name, 
			otherDataSetConfiguration.getName()) == false) {
			
			return false;
		}
		
		if (Objects.deepEquals(
			version, 
			otherDataSetConfiguration.getVersion()) == false) {
	
			return false;
		}
				
		if (Objects.deepEquals(
			description, 
			otherDataSetConfiguration.getDescription()) == false) {
	
			return false;
		}
		
		if (Objects.deepEquals(
			filePath, 
			otherDataSetConfiguration.getFilePath()) == false) {

			return false;
		}

		RIFSchemaArea otherRIFSchemaArea
			= otherDataSetConfiguration.getRIFSchemaArea();
		if (rifSchemaArea != otherRIFSchemaArea) {
			return false;
		}
		
		ArrayList<DataSetFieldConfiguration> otherDataSetFieldConfigurations
			= otherDataSetConfiguration.getFieldConfigurations();
		if (DataSetFieldConfiguration.hasIdenticalContents(
			fieldConfigurations, 
			otherDataSetFieldConfigurations) == false) {
			
			return false;
		}

		WorkflowState otherCurrentWorkflowState
			= otherDataSetConfiguration.getCurrentWorkflowState();
		if (currentWorkflowState != otherCurrentWorkflowState) {
			return false;
		}
				
		if (Objects.deepEquals(
			fileHasFieldNamesDefined, 
			otherDataSetConfiguration.fileHasFieldNamesDefined()) == false) {

			return false;
		}
		
		if (lastModifiedDatesIdentical(otherDataSetConfiguration) == false) {
			return false;
		}
		
		return true;
	}
	
	public boolean isHint() {
		return isHint;
	}
	
	public void setIsHint(final boolean isHint) {
		this.isHint = isHint;
	}
	
	public void update(final DataSetConfiguration revisedDataSetConfiguration) {
		if (hasIdenticalContents(revisedDataSetConfiguration)) {
			return;
		}
		DataSetConfiguration.copyInto(revisedDataSetConfiguration, this);		
		updateLastModifiedTime();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
				
		String recordType = getRecordType();
			
		String nameLabel
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.name.label");
		String versionLabel
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.version.label");
		String descriptionLabel
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.description.label");
			
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			nameLabel, 
			name);
		
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			versionLabel, 
			version);
		
		
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			descriptionLabel, 
			description);

		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			fieldConfiguration.checkSecurityViolations();
		}
	
	}
	
	public void checkErrors() 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		checkEmptyFields(errorMessages);
		checkAtLeastOneRequiredFieldExists(errorMessages);
		
		
		
		countErrors(
				RIFDataLoaderToolError.INVALID_DATA_SET_CONFIGURATION,
				errorMessages);
		
	}
	
	/*
	 * Determines whether the data set field configuration has load, clean or convert
	 * field names that already appear in other fields.  This is expected to be used
	 * in the context where the user has just made editing changes and we're checking
	 * to make sure they're not using field names that are already allocated to other fields
	 * If 'existingFieldIndex' is null, we assume that the field is a new field.  Otherwise
	 * a non-null index will indicate that the data set field configuration already represents
	 * a field which is already in the data set configuration. 
	 */
	public void checkDuplicateFieldNames(
		final DataSetFieldConfiguration targetDataSetFieldConfiguration) 
		throws RIFServiceException {
	

		Collator collator = Messages.genericMessages().getCollator();
		
		String targetCoreFieldName = targetDataSetFieldConfiguration.getCoreFieldName();
		
		ArrayList<String> errorMessages = new ArrayList<>();
		
		String targetLoadFieldName
			= targetDataSetFieldConfiguration.getLoadFieldName();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			String currentCoreFieldName
				= fieldConfiguration.getCoreFieldName();
			String currentLoadFieldName
				= fieldConfiguration.getLoadFieldName();
			if (collator.equals(currentLoadFieldName, targetLoadFieldName) &&
				collator.equals(currentCoreFieldName, targetCoreFieldName)) {
				
				String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.error.duplicateLoadFieldName",
					targetLoadFieldName);
				errorMessages.add(errorMessage);
				
				break;
			}
		}
		
		String targetCleanFieldName
			= targetDataSetFieldConfiguration.getCleanFieldName();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			String currentCoreFieldName
				= fieldConfiguration.getCoreFieldName();
			String currentCleanFieldName
				= fieldConfiguration.getCleanFieldName();
			if (collator.equals(currentCleanFieldName, targetCleanFieldName) &&
				collator.equals(currentCoreFieldName, targetCoreFieldName) == false) {
				
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"dataSetFieldConfiguration.error.duplicateCleanFieldName",
						targetLoadFieldName);
				errorMessages.add(errorMessage);				
				break;
			}
		}
		
		String targetConvertFieldName
			= targetDataSetFieldConfiguration.getCleanFieldName();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			String currentCoreFieldName
				= fieldConfiguration.getCoreFieldName();
			String currentConvertFieldName
				= fieldConfiguration.getConvertFieldName();
			if (collator.equals(currentConvertFieldName, targetConvertFieldName) &&
				collator.equals(currentCoreFieldName, targetCoreFieldName) == false) {
				
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"dataSetFieldConfiguration.error.duplicateConvertFieldName",
						targetLoadFieldName);
				errorMessages.add(errorMessage);				
				break;
			}
		}
		
		countErrors(
			RIFDataLoaderToolError.INVALID_DATA_SET_FIELD_CONFIGURATION, 
			errorMessages);
	}
		
	public void checkEmptyFields(
		final ArrayList<String> errorMessages) {
					
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();

		
		if (fieldValidationUtility.isEmpty(name)) {
			String nameFieldLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.name.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					nameFieldLabel);
			errorMessages.add(errorMessage);		
		}		

		
		if (fieldValidationUtility.isEmpty(version)) {
			String versionFieldLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.version.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					versionFieldLabel);
			errorMessages.add(errorMessage);		
		}

		
		if (fieldValidationUtility.isEmpty(filePath)) {
			
			String versionFieldLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.filePath.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					versionFieldLabel);
			errorMessages.add(errorMessage);		
		}
		
		//description may be empty
		if (currentWorkflowState == null) {
		
			String currentWorkflowStateFieldLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.currentWorkflowState.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					currentWorkflowStateFieldLabel);
			errorMessages.add(errorMessage);
		}

		if (rifSchemaArea == null) {

			String currentWorkflowStateFieldLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.rifSchemaArea.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					currentWorkflowStateFieldLabel);
			errorMessages.add(errorMessage);						
		}
		
		if (fieldConfigurations.isEmpty()) {

			String currentWorkflowStateFieldLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.plural.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredList",
					currentWorkflowStateFieldLabel);
			errorMessages.add(errorMessage);	
		}
			
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			fieldConfiguration.checkEmptyFields(errorMessages);
		}
	}

	private void checkAtLeastOneRequiredFieldExists(
		final ArrayList<String> errorMessages) {
		
		int numberRequiredFields = 0;
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldConfiguration.getFieldRequirementLevel() == FieldRequirementLevel.REQUIRED_BY_RIF) {
				numberRequiredFields++;
			}
		}
		
		if (numberRequiredFields == 0) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetConfiguration.error.noRequiredFields");
			errorMessages.add(errorMessage);			
		}
	}
	public int getNumberOfCovariateFields() {
		
		int numberOfCovariateFields = 0;
		
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldConfiguration.getFieldPurpose() == FieldPurpose.COVARIATE) {
				numberOfCovariateFields++;
			}
		}

		return numberOfCovariateFields;
	}
	
	public DataSetFieldConfiguration getFieldHavingConvertFieldName(
		final String convertFieldName) {
		
		Collator collator
			= GENERIC_MESSAGES.getCollator();
		
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			String currentFieldName
				= fieldConfiguration.getConvertFieldName();
			if (collator.equals(currentFieldName, convertFieldName)) {
				return fieldConfiguration;
			}
		}

		return null;
		
	}

	public ArrayList<DataSetFieldConfiguration> getFieldsWithValidationChecks() {
		
		ArrayList<DataSetFieldConfiguration> fieldsWithValidationChecks
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldRequirementLevel fieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if (fieldRequirementLevel != FieldRequirementLevel.IGNORE_FIELD){
				
				RIFDataType dataType = fieldConfiguration.getRIFDataType();
				if (dataType.getFieldValidationPolicy() != FieldActionPolicy.DO_NOTHING) {
					fieldsWithValidationChecks.add(fieldConfiguration);
				}			
			}
		}
		
		return fieldsWithValidationChecks;
	}

	public ArrayList<DataSetFieldConfiguration> getFieldsWithEmptyFieldCheck() {
		ArrayList<DataSetFieldConfiguration> fieldsWithEmptyFieldCheck
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldSupportsFieldCheck(
				fieldConfiguration, 
				CheckOption.PERCENT_EMPTY)) {

				fieldsWithEmptyFieldCheck.add(fieldConfiguration);
			}
		}
	
		return fieldsWithEmptyFieldCheck;	
	}

	public ArrayList<DataSetFieldConfiguration> getFieldsWithEmptyPerYearFieldCheck() {
		ArrayList<DataSetFieldConfiguration> fieldsWithEmptyPerYearFieldCheck
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldSupportsFieldCheck(
				fieldConfiguration, 
				CheckOption.PERCENT_EMPTY_PER_YEAR)) {

				fieldsWithEmptyPerYearFieldCheck.add(fieldConfiguration);
			}
		}
	
		return fieldsWithEmptyPerYearFieldCheck;	
	}
	
	
	
	private boolean fieldSupportsFieldCheck(
		final DataSetFieldConfiguration dataSetFieldConfiguration, 
		final CheckOption targetCheckOption) {
		
		ArrayList<CheckOption> rifCheckOptions
			= dataSetFieldConfiguration.getCheckOptions();
		for (CheckOption rifCheckOption : rifCheckOptions) {
			if (targetCheckOption == rifCheckOption) {
				return true;
			}
		}
	
		return false;
	}
		
	public int getNumberOfGeospatialFields() {
		int numberOfCovariateFields = 0;
				
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldConfiguration.getFieldPurpose() == FieldPurpose.GEOGRAPHICAL_RESOLUTION) {
				numberOfCovariateFields++;
			}
		}

		return numberOfCovariateFields;
	}

	
	public int getNumberOfHealthCodeFields() {
		
		int numberOfHealthCodeFields = 0;
				
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldConfiguration.getFieldPurpose() == FieldPurpose.HEALTH_CODE) {
				numberOfHealthCodeFields++;
			}
		}

		return numberOfHealthCodeFields;
	}	
	
	public String[] getFieldsUsedForDuplicationChecks() {
		
		ArrayList<String> duplicateCriteriaFieldNames = new ArrayList<String>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldRequirementLevel fieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if (fieldRequirementLevel != FieldRequirementLevel.IGNORE_FIELD && 
				fieldConfiguration.isDuplicateIdentificationField()) {
				System.out.println("DataSetConfiguration getFieldsForDuplicate=="+fieldConfiguration.getConvertFieldName()+"==");
				duplicateCriteriaFieldNames.add(fieldConfiguration.getConvertFieldName());
			}			
		}
		
		String[] results
			= duplicateCriteriaFieldNames.toArray(new String[0]);
		return results;
		
	}

	public String[] getIndexFieldNames() {
		
		ArrayList<String> indexFieldNames = new ArrayList<String>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldConfiguration.optimiseUsingIndex() == true) {
				indexFieldNames.add(fieldConfiguration.getConvertFieldName());
			}
		}
		
		String[] results
			= indexFieldNames.toArray(new String[0]);		
		return results;
		
	}
	
	public void clearFieldConfigurations() {
		fieldConfigurations.clear();
	}
	
	public String[] getCoreFieldNames() {
		
		ArrayList<String> coreFieldNames = new ArrayList<String>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			coreFieldNames.add(fieldConfiguration.getCoreFieldName());
		}
		
		String[] results
			= coreFieldNames.toArray(new String[0]);
		return results;
	}	
	
	public String[] getLoadFieldNames() {
		
		ArrayList<String> cleanFieldNames = new ArrayList<String>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			cleanFieldNames.add(fieldConfiguration.getLoadFieldName());
		}
		
		String[] results
			= cleanFieldNames.toArray(new String[0]);
		return results;
	}	
	
	public String[] getCleanFieldNames() {
		
		ArrayList<String> cleanFieldNames = new ArrayList<String>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			cleanFieldNames.add(fieldConfiguration.getCleanFieldName());
		}
		
		String[] results
			= cleanFieldNames.toArray(new String[0]);
		return results;
	}
	
	public String getDisplayName() {
		StringBuilder buffer = new StringBuilder();
		if (isHint) {
			buffer.append(name);
		}
		else {
			buffer.append(name);
			buffer.append("-");
			buffer.append(version);			
		}
		return buffer.toString();		
	}
	
	public String getRecordType() {
		String recordType
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.recordType");
		return recordType;		
	}
	
	public String getPublishedTableName() {
		return rifSchemaArea.getPublishedTableName(name);
	}
	
	/*
	 * Diagnostic method useful in testting
	 */
	public void print() {
		System.out.println("===================== BEGINNING DATA SET =========================");
		System.out.println("Data Set Name:==" + getDisplayName()+"==");
		System.out.println("Total Fields:" + fieldConfigurations.size());
		System.out.println("Field Details:");

		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			fieldConfiguration.print();
				
			
		}
		
		System.out.println("===================== ENDING DATA SET ============================");		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


