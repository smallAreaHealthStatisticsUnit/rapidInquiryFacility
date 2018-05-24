package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;

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

public class WorkflowValidator {
	
	// ==========================================
	// Section Constants
	// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private RIFSchemaAreaPropertyManager schemaAreaPropertyManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public WorkflowValidator(
		final RIFSchemaAreaPropertyManager schemaAreaPropertyManager) {

		this.schemaAreaPropertyManager = schemaAreaPropertyManager;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	public void validateLinearWorkflow(
		final LinearWorkflow linearWorkflow)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= linearWorkflow.getDataSetConfigurations();
		
		//Make sure that none of the data set configurations have
		//errors related to empty or null field values
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			dataSetConfiguration.checkEmptyFields(errorMessages);
		}

		detectException(
				errorMessages,
				RIFDataLoaderToolError.INVALID_DATA_SET_CONFIGURATION);
				
		//Check that the states in the work flow and in each data set are valid
		WorkflowState startWorkflowState
			= linearWorkflow.getStartWorkflowState();
		WorkflowState stopWorkflowState
			= linearWorkflow.getStopWorkflowState();
		
		checkStartStopStatesInOrder(
			startWorkflowState,
			stopWorkflowState,
			errorMessages);

		checkDataSetsHaveSameState(
			dataSetConfigurations,
			errorMessages);		
		
		//By now we can assume that all of the states are the same
		//We must ensure that the common state is not SPLIT, COMBINE or PUBLISH.
		//SPLIT and COMBINE are handled through a BranchedWorkflow
		//
		//Data sets marked PUBLISH cannot be reused in other work flows
		WorkflowState commonWorkflowState
			= dataSetConfigurations.get(0).getCurrentWorkflowState();
		if (commonWorkflowState == WorkflowState.SPLIT) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowValidator.error.illegalCurrentStateInLinearWorkflow");
			errorMessages.add(errorMessage);
		}
		else if (commonWorkflowState == WorkflowState.COMBINE) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowValidator.error.illegalCurrentStateInLinearWorkflow");
			errorMessages.add(errorMessage);			
		}
		else if (commonWorkflowState == WorkflowState.PUBLISH) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowValidator.error.illegalCurrentStateInLinearWorkflow");
			errorMessages.add(errorMessage);						
		}		

		//The common current state of the data set configurations must be
		//the same as starting state of the workflow
		//work flow starting state must be the same as the common
		//work flow state of its 

		WorkflowState expectedStartStateOfDataSets
			= linearWorkflow.getStartWorkflowState();
		if (commonWorkflowState != expectedStartStateOfDataSets) {
			System.out.println("Print common=="+commonWorkflowState.getStateName() + "==expected=="+ expectedStartStateOfDataSets.getStateName()+"==");
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowValidator.error.workflowAndDataSetsNeedSameStartingState",
					expectedStartStateOfDataSets.getStateName(),
					commonWorkflowState.getStateName());
			errorMessages.add(errorMessage);			
		}
		
		detectException(
			errorMessages,
			RIFDataLoaderToolError.INVALID_DATA_SET_CONFIGURATION);
		
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {			
			validateDataSetConfiguration(
				stopWorkflowState,
				dataSetConfiguration);				
		}
		
		detectException(
			errorMessages,
			RIFDataLoaderToolError.INVALID_DATA_SET_CONFIGURATION);
	}

	private void checkStartStopStatesInOrder(
		final WorkflowState startWorkflowState,
		final WorkflowState stopWorkflowState,
		final ArrayList<String> errorMessages) {
		
		//check that the end state does not come before the start state
		if (WorkflowState.areStatesInOrder(
				startWorkflowState, 
				stopWorkflowState) == false) {
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowValidator.error.startStopStatesNotInOrder",
					stopWorkflowState.getStateName(),
					startWorkflowState.getStateName());
			errorMessages.add(errorMessage);
		}
	}
	
	private void checkDataSetsHaveSameState(
		final ArrayList<DataSetConfiguration> dataSetConfigurations,
		final ArrayList<String> errorMessages) {
		
		
		//KLG: I would have used a HashSet to get a unique list of states
		//but when I'm constructing the error message I like to know the
		//index of the first item - which will not be preceded by a comma
		//The Iterator will go through the list but then I need another flag
		//isFirstElement in order to identify this.  Therefore, just going
		//to use ArrayList to get a unique list
		ArrayList<WorkflowState> dataSetWorkflowStates 
			= new ArrayList<WorkflowState>();				
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			WorkflowState workflowState
				= dataSetConfiguration.getCurrentWorkflowState();
			if (dataSetWorkflowStates.contains(workflowState) == false) {
				dataSetWorkflowStates.add(workflowState);				
			}
		}

		if (dataSetWorkflowStates.size() > 1) {
			StringBuilder listOfStates = new StringBuilder();
			for (int i = 0; i < dataSetWorkflowStates.size(); i++) {
				if (i != 0) {
					listOfStates.append(",");
				}
				listOfStates.append(dataSetWorkflowStates.get(i).getStateName());
			}
						
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowValidator.error.dataSetsHaveDifferentStates",
					listOfStates.toString());
			errorMessages.add(errorMessage);
		}
		
	}
	
	public void validateDataSetConfiguration(
		final WorkflowState workFlowState,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
				
		ArrayList<String> errorMessages
			= new ArrayList<String>();
		
		if (workFlowState == WorkflowState.EXTRACT) {
			validateLoad(
				dataSetConfiguration, 
				errorMessages);
		}
		else if (workFlowState == WorkflowState.CLEAN) {
			validateClean(
				dataSetConfiguration, 
				errorMessages);
		}
		else if (workFlowState == WorkflowState.CONVERT) {
			validateConvert(
				dataSetConfiguration, 
				errorMessages);
		}
		else if (workFlowState == WorkflowState.SPLIT) {
			validateSplit(
				dataSetConfiguration, 
				errorMessages);
		}
		else if (workFlowState == WorkflowState.COMBINE) {
			validateCombine(
				dataSetConfiguration, 
				errorMessages);
		}
		else if (workFlowState == WorkflowState.OPTIMISE) {
			validateOptimise(
				dataSetConfiguration, 
				errorMessages);
		}
		else if (workFlowState == WorkflowState.CHECK) {
			validateCheck(
				dataSetConfiguration, 
				errorMessages);
		}
		else if (workFlowState == WorkflowState.PUBLISH) {
			validatePublish(
				dataSetConfiguration, 
				errorMessages);
		}

		countErrors(
			errorMessages, 
			RIFDataLoaderToolError.INVALID_DATA_SET_CONFIGURATION);					
	}
	
	private void validateDataSetConfigurationProperties(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {
					
		dataSetConfiguration.checkEmptyFields(errorMessages);
	}
	
	public void validateLoad(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		validateLoad(dataSetConfiguration, errorMessages);
		
		countErrors(
			errorMessages, 
			RIFDataLoaderToolError.INVALID_LOAD_STATE);
		
	}
	
	private void validateLoad(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {
		
		validateDataSetConfigurationProperties(
			dataSetConfiguration,
			errorMessages);
	}
	
	
	public void validateClean(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		validateClean(dataSetConfiguration, errorMessages);
		
		countErrors(
			errorMessages, 
			RIFDataLoaderToolError.INVALID_CLEAN_STATE);
	}
	
	private void validateClean(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {
		
		validateLoad(
			dataSetConfiguration,
			errorMessages);
	}
	
	public void validateConvert(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		validateConvert(
			dataSetConfiguration,
			errorMessages);
		
		countErrors(
			errorMessages, 
			RIFDataLoaderToolError.INVALID_CONVERT_STATE);
	}
	
	private void validateConvert(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {
		
		validateClean(
			dataSetConfiguration,
			errorMessages);
				
		/**
		 * Examine the names of convert field names so that
		 * we can find out if there are any field names expected
		 * in the target RIF schema which are not defined in the
		 * list of fields.
		 * 
		 * For example, suppose a data set meant to be a numerator table
		 * has fields which have associated convert field names of "age" 
		 * and "ethnicity".  
		 * 
		 * 
		 * 
		 */
		String[] convertFieldNames
			= dataSetConfiguration.getConvertFieldNames();
	
		RIFSchemaArea rifSchemaArea
			= dataSetConfiguration.getRIFSchemaArea();
		
		String[] missingRequiredFieldNames
			= schemaAreaPropertyManager.getMissingRequiredConvertFieldNames(
				rifSchemaArea,
				convertFieldNames);
		
		if (missingRequiredFieldNames.length > 0) {
			StringBuilder listOfMissingFields = new StringBuilder();
			for (int i = 0; i < missingRequiredFieldNames.length; i++) {
				if (i != 0) {
					listOfMissingFields.append(",");
				}
				listOfMissingFields.append(missingRequiredFieldNames[i]);
			}
			
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"workflowValidator.error.missingRequiredConvertFields",
					rifSchemaArea.getName(),
					listOfMissingFields.toString());
			errorMessages.add(errorMessage);
		}
	
		/*
		 * Consider the data fields which have been mapped to fields that
		 * appear in one of the RIF schema tables.  The RIF schema will expect
		 * that the DataSetConfiguration has an expected field name and have
		 * an expected field data type.  In this section, we look up the expected
		 * data types for fields 
		 */
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			
			//Here we want to identify mapped conversion fields by 
			//using the 'convertFieldName
			String fieldName = fieldConfiguration.getConvertFieldName();
			RIFDataType proposedDataType
				= fieldConfiguration.getRIFDataType();
			String proposedDataTypeIdentifier
				= proposedDataType.getIdentifier();
			
			//expectedDataType might be null if the schema property manager 
			//does not know about the field.
			RIFDataType expectedDataType
				= schemaAreaPropertyManager.getExpectedRIFDataType(
					rifSchemaArea, 
					fieldName);
			/*
			if (expectedDataType != null) {
				String expectedDataTypeIdentifier
					= expectedDataType.getIdentifier();
				Collator collator
					= GENERIC_MESSAGES.getCollator();
				if (collator.equals(
					proposedDataTypeIdentifier, 
					expectedDataTypeIdentifier) == false) {

					System.out.println("WFV rifSchema=="+rifSchemaArea.getName()+"==field name=="+fieldName+"==");
					System.out.println("WFV validateConvert==proposed=="+proposedDataTypeIdentifier+"=="+expectedDataTypeIdentifier+"==");
					
					//The schema properties manager thinks that this field
					//should have a different data type than the one
					//that appears in the DataFieldConfiguration
					String errorMessage
						= RIFDataLoaderToolMessages.getMessage(
							"workflowValidator.error.unexpectedDataTypeForConvertField",
							fieldConfiguration.getDisplayName(),
							expectedDataType.getDisplayName(),
							proposedDataType.getDisplayName());
					errorMessages.add(errorMessage);
				}
			}
			*/			
		}
		
		/*
		 * Here we are looking at more specific parts of the RIF Schema:
		 *    - numerator data
		 *    - denominator data
		 *    - covariate data
		 *    
		 * In numerator and denominator data sets, we need to have at least one column which 
		 * is a geospatial resolution field.  In numerator data tables, we expect there to 
		 * be at least one health code field as well.
		 * 
		 * In covariate data, we expect exactly one column to be the name of a covariate
		 */
		if ((rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) ||
			(rifSchemaArea == RIFSchemaArea.POPULATION_DENOMINATOR_DATA)) {
			
			int numberOfGeographicalFields
				= dataSetConfiguration.getNumberOfGeospatialFields();
			if (numberOfGeographicalFields == 0) {
				//There are no geographical resolution fields
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"workflowValidator.error.noGeographicalFields",
						dataSetConfiguration.getDisplayName(),
						rifSchemaArea.getName());
				errorMessages.add(errorMessage);
			}
			
			if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
				int numberOfHealthCodeFields
					= dataSetConfiguration.getNumberOfHealthCodeFields();
				if (numberOfHealthCodeFields == 0) {
					//There needs to be at least one health code in the numerator
					//so that the RIF can identify records with relevant health
					//outcomes
					String errorMessage
						= RIFDataLoaderToolMessages.getMessage(
							"workflowValidator.error.noHealthCodeFields",
							dataSetConfiguration.getDisplayName(),
							rifSchemaArea.getName());
					errorMessages.add(errorMessage);
				}			
			}
		}
		else if (rifSchemaArea == RIFSchemaArea.COVARIATE_DATA) {
			int numberOfCovariateFields
				= dataSetConfiguration.getNumberOfCovariateFields();
			if (numberOfCovariateFields == 0) {
				//There needs to be at least one health code in the numerator
				//so that the RIF can identify records with relevant health
				//outcomes
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"workflowValidator.error.noCovariateFields",
						dataSetConfiguration.getDisplayName(),
						rifSchemaArea.getName());
				errorMessages.add(errorMessage);
			}
		}
				
	}
	
	public void validateSplit(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		validateSplit(dataSetConfiguration, errorMessages);
		
		countErrors(
			errorMessages, 
			RIFDataLoaderToolError.INVALID_SPLIT_STATE);
	}
	
	private void validateSplit(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {

		validateConvert(
			dataSetConfiguration,
			errorMessages);		
	}

	public void validateCombine(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		validateCombine(dataSetConfiguration, errorMessages);
		
		countErrors(
			errorMessages, 
			RIFDataLoaderToolError.INVALID_COMBINE_STATE);	
	}
	
	private void validateCombine(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {

		validateCombine(
			dataSetConfiguration,
			errorMessages);		
	}
	
	public void validateOptimise(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		validateOptimise(dataSetConfiguration, errorMessages);
		
		countErrors(
			errorMessages, 
			RIFDataLoaderToolError.INVALID_OPTIMISE_STATE);		
	}
	
	private void validateOptimise(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {
		
		validateConvert(
			dataSetConfiguration,
			errorMessages);		
		
		/*
		 * Here, we are trying to check that fields expected by
		 * a target part of the RIF schema have not been inadvertently
		 * set so that "optimiseUsingIndex" is not set to false
		 * 
		 * For example, suppose the data set is being transformed into
		 * a numerator table.  We expect the data set to have a field
		 * with a convertFieldName of "year".  But we also then expect
		 * the setting for optimiseUsingIndex to be true because the RIF
		 * will automatically index the field. 
		 * 
		 * These kind of errors should not occur in the GUI tool, which provides
		 * guided data entry features to automatically set the optimiseUsingIndex
		 * flag correctly.  However, we also have to consider dealing with input
		 * that is in an XML file.  In the command line deployment, it may be the 
		 * case that the tags have been incorrectly specified.
		*/
		RIFSchemaArea rifSchemaArea 
			= dataSetConfiguration.getRIFSchemaArea();
		String[] requiredFieldNames
			= schemaAreaPropertyManager.getRequiredConvertFieldNames(rifSchemaArea);
		for (String requiredFieldName : requiredFieldNames) {

			DataSetFieldConfiguration fieldConfiguration
				= dataSetConfiguration.getFieldHavingConvertFieldName(requiredFieldName);	
			if (fieldConfiguration == null) {
				//this can only mean that the field has been synthesised from conversion
				//For now, the only field to consider is age_sex_group
				Collator collator = GENERIC_MESSAGES.getCollator();
				if (collator.equals(requiredFieldName, "age_sex_group") == false) {
					String errorMessage
						= RIFDataLoaderToolMessages.getMessage(
							"workflowValidator.error.notIndexingSynthesisedField",
							requiredFieldName);
					errorMessages.add(errorMessage);
				}
			}
			else if (fieldConfiguration.optimiseUsingIndex() == false) {
				//ERROR: the optimise field should always have a value of 'true'
				//when it is a required field used to map data from the data set
				//into a field required by the RIF schema
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"workflowValidator.error.notIndexingRequiredField",
						fieldConfiguration.getDisplayName());
				errorMessages.add(errorMessage);
			}
		}
		
	}
	

	public void validateCheck(
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		validateCheck(dataSetConfiguration, errorMessages);
		
		countErrors(
			errorMessages,
			RIFDataLoaderToolError.INVALID_CHECK_STATE);		
	}
	
	private void validateCheck(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {
				
		validateOptimise(
			dataSetConfiguration, 
			errorMessages);

		//make sure the options are available
		RIFSchemaArea rifSchemaArea 
			= dataSetConfiguration.getRIFSchemaArea();
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			
			ArrayList<CheckOption> checkOptions
				= fieldConfiguration.getCheckOptions();		
			for (CheckOption checkOption : checkOptions) {
				if (schemaAreaPropertyManager.isRIFCheckOptionAllowed(
					rifSchemaArea, 
					checkOption) == false) {
					
					String errorMessage
						= RIFDataLoaderToolMessages.getMessage(
							"workflowValidator.error.checkOptionNotSupportedForSchemaArea",
							fieldConfiguration.getDisplayName(),
							checkOption.getName());
					errorMessages.add(errorMessage);
				}
			}			
		}
	}
	
	
	public void validatePublish(
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		validatePublish(
			dataSetConfiguration,
			errorMessages);
		
		countErrors(
			errorMessages,
			RIFDataLoaderToolError.INVALID_PUBLISH_STATE);
	}
	
	private void validatePublish(
		final DataSetConfiguration dataSetConfiguration,
		final ArrayList<String> errorMessages) {
		
		validateCheck(
			dataSetConfiguration, 
			errorMessages);
		
	}

	private void detectException(
		final ArrayList<String> errorMessages,
		final RIFDataLoaderToolError errorCode) 
		throws RIFServiceException {
		
		if (errorMessages.isEmpty() == false) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					errorCode,
					errorMessages);
			throw rifServiceException;
		}		
	}
	
	private void countErrors(
		final ArrayList<String> errorMessages,
		final RIFDataLoaderToolError errorCode) 
		throws RIFServiceException {
		
		if (errorMessages.isEmpty() == false) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					errorCode, 
					errorMessages);
			throw rifServiceException;			
		}
		
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


