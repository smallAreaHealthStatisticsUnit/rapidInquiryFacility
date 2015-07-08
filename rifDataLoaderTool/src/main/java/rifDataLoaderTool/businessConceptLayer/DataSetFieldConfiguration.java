package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.businessConceptLayer.rifDataTypes.AbstractRIFDataType;
import rifDataLoaderTool.businessConceptLayer.rifDataTypes.TextRIFDataType;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;
import rifServices.util.FieldValidationUtility;

import java.util.ArrayList;



/**
 *
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
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================

	private String coreDataSetName;
	private String coreFieldName;
	private String coreFieldDescription;
	private AbstractRIFDataType rifDataType;
	
	private String loadFieldName;
	private String cleanFieldName;

	private String convertFieldName;
	private FieldPurpose fieldPurpose;

	private boolean optimiseUsingIndex;
	
	private boolean isDuplicateIdentificationField;
	private ArrayList<RIFCheckOption> checkOptions;	
	private boolean isEmptyValueAllowed;
	
	private FieldRequirementLevel fieldRequirementLevel;
	
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
			
		this.coreDataSetName = parentRecordName;
			
		this.coreFieldName = coreFieldName;
		coreFieldDescription = null;
		rifDataType = TextRIFDataType.newInstance();
		loadFieldName = coreFieldName;
		cleanFieldName = loadFieldName;
		convertFieldName = null;
		optimiseUsingIndex = false;
		checkOptions = new ArrayList<RIFCheckOption>();
			
		fieldPurpose = FieldPurpose.OTHER;
		isDuplicateIdentificationField = false;
		isEmptyValueAllowed = true;
		
		fieldRequirementLevel = FieldRequirementLevel.IGNORE_FIELD;
		
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
	
		DataSetFieldConfiguration cloneConfiguration
			= new DataSetFieldConfiguration("", "");
		cloneConfiguration.setCoreDataSetName(
			originalConfiguration.getCoreDataSetName());
		cloneConfiguration.setCoreFieldName(
			originalConfiguration.getCoreFieldName());
		cloneConfiguration.setLoadFieldName(
			originalConfiguration.getLoadFieldName());
		cloneConfiguration.setCleanFieldName(
			originalConfiguration.getCleanFieldName());
		cloneConfiguration.setConvertFieldName(
			originalConfiguration.getConvertFieldName());
		cloneConfiguration.setOptimiseUsingIndex(
			originalConfiguration.optimiseUsingIndex());
		cloneConfiguration.setEmptyValueAllowed(
			originalConfiguration.isEmptyValueAllowed());
		cloneConfiguration.setFieldPurpose(
			originalConfiguration.getFieldPurpose());
		cloneConfiguration.setDuplicateIdentificationField(
			originalConfiguration.isDuplicateIdentificationField());
				
		cloneConfiguration.setFieldRequirementLevel(
			originalConfiguration.getFieldRequirementLevel());
	
		ArrayList<RIFCheckOption> originalCheckOptions
			= originalConfiguration.getCheckOptions();
		cloneConfiguration.setCheckOptions(originalCheckOptions);	
		
		return cloneConfiguration;
		
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

	public AbstractRIFDataType getRIFDataType() {

		return rifDataType;
	}

	public void setRIFDataType(
		final AbstractRIFDataType rifDataType) {

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

	public boolean optimiseUsingIndex() {
		
		return optimiseUsingIndex;
	}

	public void setOptimiseUsingIndex(
		final boolean optimiseUsingIndex) {

		this.optimiseUsingIndex = optimiseUsingIndex;
	}
	
	public ArrayList<RIFCheckOption> getCheckOptions() {
		
		return checkOptions;
	}
	
	public void setCheckOptions(
		final ArrayList<RIFCheckOption> checkOptions) {
		
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
	}
	
	public boolean hasCleaningRules() {
		
		ArrayList<CleaningRule> cleaningRules
			= rifDataType.getCleaningRules();
		if (cleaningRules.isEmpty()) {
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
					"dataSetFieldConfiguration.rifSchemaArea.label");
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
		
		if (fieldValidationUtility.isEmpty(coreFieldName)) {

			String coreFieldDescriptionLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.coreFieldDescription.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					coreFieldDescriptionLabel);
			errorMessages.add(errorMessage);		
		}		
	
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


		if (fieldValidationUtility.isEmpty(convertFieldName)) {
			String convertFieldNameLabel
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetFieldConfiguration.convertFieldName.label");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"general.validation.emptyRequiredField",
					convertFieldNameLabel);
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
	}
	
	
	
	public String getRecordType() {
		String recordType
			= RIFDataLoaderToolMessages.getMessage("dataSetFieldConfiguration.recordType");
		return recordType;		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	public String getDisplayName() {
		StringBuilder buffer = new StringBuilder();
		
		buffer.append(coreDataSetName);
		buffer.append(".");
		buffer.append(coreFieldName);
		
		return buffer.toString();
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


