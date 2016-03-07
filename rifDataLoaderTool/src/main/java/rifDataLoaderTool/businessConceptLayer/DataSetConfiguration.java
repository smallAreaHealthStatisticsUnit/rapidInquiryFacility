package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFServiceSecurityException;
import rifServices.util.FieldValidationUtility;

import java.text.Collator;
import java.util.ArrayList;

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
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration} field 
 * configurations, each of which have their own properties.
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
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	
	private boolean isNewRecord;
	private String name;
	private String version;
	private String description;
	private String filePath;
	private RIFSchemaArea rifSchemaArea;
	private ArrayList<DataSetFieldConfiguration> fieldConfigurations;
	public WorkflowState currentWorkflowState;
	private boolean fileHasFieldNamesDefined;
	
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
		copyInto(originalDataSetConfiguration, cloneDataSetConfiguration);
				
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
		
		ArrayList<DataSetFieldConfiguration> originalFieldConfigurations
			= sourceDataSetConfiguration.getFieldConfigurations();
		ArrayList<DataSetFieldConfiguration> cloneFieldConfigurations
			= DataSetFieldConfiguration.createCopy(originalFieldConfigurations);	
		
		destinationDataSetConfiguration.setFieldConfigurations(cloneFieldConfigurations);

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
	

		Collator collator = RIFDataLoaderToolMessages.getCollator();
		
		String targetCoreFieldName = targetDataSetFieldConfiguration.getCoreFieldName();
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String targetLoadFieldName
			= targetDataSetFieldConfiguration.getLoadFieldName();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			String currentCoreFieldName
				= fieldConfiguration.getCoreFieldName();
			String currentLoadFieldName
				= fieldConfiguration.getLoadFieldName();
			if (collator.equals(currentLoadFieldName, targetLoadFieldName) &&
				collator.equals(currentCoreFieldName, targetCoreFieldName) == false) {
				
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
					"dataSetFieldConfiguration.name.label");
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
	
	
	public ArrayList<DataSetFieldConfiguration> getChangeAuditFields(
		final FieldChangeAuditLevel fieldChangeAuditLevel) {
		
		
		ArrayList<DataSetFieldConfiguration> changeAuditFields
			= new ArrayList<DataSetFieldConfiguration>();
		
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldChangeAuditLevel currentFieldChangeAuditLevel
				= fieldConfiguration.getFieldChangeAuditLevel();
			
			if (currentFieldChangeAuditLevel == fieldChangeAuditLevel) {
				changeAuditFields.add(fieldConfiguration);				
			}
		}
		
		return changeAuditFields;
		
	}
	
	public DataSetFieldConfiguration getFieldHavingConvertFieldName(
		final String convertFieldName) {
		
		Collator collator
			= RIFDataLoaderToolMessages.getCollator();
		
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
			RIFDataType dataType = fieldConfiguration.getRIFDataType();
			if (dataType.getFieldValidationPolicy() != RIFFieldValidationPolicy.NO_VALIDATION) {
				fieldsWithValidationChecks.add(fieldConfiguration);
			}
		}
		
		return fieldsWithValidationChecks;
	}
	
	public ArrayList<DataSetFieldConfiguration> getFieldsWithConversionFunctions() {
		
		ArrayList<DataSetFieldConfiguration> fieldsWithConversionFunctions
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldConfiguration.getConvertFunction() != null) {
				
				FieldRequirementLevel fieldRequirementLevel
					= fieldConfiguration.getFieldRequirementLevel();
					
				if (fieldRequirementLevel != FieldRequirementLevel.IGNORE_FIELD) {
					fieldsWithConversionFunctions.add(fieldConfiguration);					
				}
			}
		}
		
		return fieldsWithConversionFunctions;		
	}

	public ArrayList<DataSetFieldConfiguration> getFieldsWithoutConversionFunctions() {
		
		ArrayList<DataSetFieldConfiguration> fieldsWithoutConversionFunctions
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldConfiguration.getConvertFunction() == null) {

				FieldRequirementLevel fieldRequirementLevel
					= fieldConfiguration.getFieldRequirementLevel();
				if (fieldRequirementLevel != FieldRequirementLevel.IGNORE_FIELD) {
					fieldsWithoutConversionFunctions.add(fieldConfiguration);					
				}				
			}
		}
		
		return fieldsWithoutConversionFunctions;		
	}	
	
	public ArrayList<DataSetFieldConfiguration> getFieldsWithEmptyFieldCheck() {
		ArrayList<DataSetFieldConfiguration> fieldsWithEmptyFieldCheck
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			if (fieldSupportsFieldCheck(
				fieldConfiguration, 
				RIFCheckOption.PERCENT_EMPTY)) {

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
				RIFCheckOption.PERCENT_EMPTY_PER_YEAR)) {

				fieldsWithEmptyPerYearFieldCheck.add(fieldConfiguration);
			}
		}
	
		return fieldsWithEmptyPerYearFieldCheck;	
	}
	
	
	
	private boolean fieldSupportsFieldCheck(
		final DataSetFieldConfiguration dataSetFieldConfiguration, 
		final RIFCheckOption targetCheckOption) {
		
		ArrayList<RIFCheckOption> rifCheckOptions
			= dataSetFieldConfiguration.getCheckOptions();
		for (RIFCheckOption rifCheckOption : rifCheckOptions) {
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
			if (fieldConfiguration.isDuplicateIdentificationField()) {
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
		buffer.append(name);
		buffer.append("-");
		buffer.append(version);
		return buffer.toString();		
	}
	
	public String getRecordType() {
		String recordType
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.recordType");
		return recordType;		
	}
		
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


