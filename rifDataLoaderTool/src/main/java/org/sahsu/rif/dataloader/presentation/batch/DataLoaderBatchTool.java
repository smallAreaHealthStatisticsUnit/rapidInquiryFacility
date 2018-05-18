package org.sahsu.rif.dataloader.presentation.batch;

import java.io.File;
import java.text.Collator;

import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.concepts.RIFSchemaAreaPropertyManager;
import org.sahsu.rif.dataloader.concepts.WorkflowState;
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

public class DataLoaderBatchTool {

	public static void main(String[] arguments) {
		
		try {
			DataLoaderBatchTool dataLoaderBatchTool
				= new DataLoaderBatchTool();		
			dataLoaderBatchTool.run(arguments);		
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
	}
	
	
	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DataLoaderBatchTool() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void run(final String[] commandLineArguments) 
		throws RIFServiceException {
		
		try {
			if (commandLineArguments.length == 0) {
				//ERROR: must at least one argument to specify an operation
				String errorMessage
					= RIFDataLoaderToolMessages
							  .getMessage("dataLoaderBatchTool.error.noArgumentsSpecified");
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFDataLoaderToolError.NO_COMMAND_LINE_ARGUMENTS_SPECIFIED,
						errorMessage);
				throw rifServiceException;
			}
			
			Collator collator = GENERIC_MESSAGES.getCollator();
			String operationArgument = commandLineArguments[0].toUpperCase();
			
			if (collator.equals(operationArgument, "-RESERVEDFIELDNAMES")) {
				showReservedFieldNames();
			}
			else if (collator.equals(operationArgument, "-RIFDATATYPES")) {
				showSupportedRIFDataTypes();
			}
			else if (collator.equals(operationArgument, "-RIFREQUIREMENTTYPES")) {
				showRIFRequirementTypes();
			}
			else if (collator.equals(operationArgument, "-RIFSCHEMAAREAS")) {
				showRIFSchemaAreas();
			}
			else if (collator.equals(operationArgument, "-REQUIREDFIELDS")) {
				showRequiredFieldsForSchemaArea(commandLineArguments);
			}
			else if (collator.equals(operationArgument, "-WORKFLOWSTAGES")) {
				showWorkflowStages();
			}
			else if (collator.equals(operationArgument, "-PROCESSDATASET")) {
				processDataSet(commandLineArguments);
			}
			else {
				//unrecognised command
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.error.unknownCommandLineOption");
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFDataLoaderToolError.UNKNOWN_COMMAND_LINE_OPTION, 
						errorMessage);
				throw rifServiceException;				
			}
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
		
		//the last argument should be the file name	
	}
	
	private void showReservedFieldNames() {

		String responseHeaderMessage
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showReservedFieldNames.response");
		
		String[] reservedFieldNames = new String[4];
		reservedFieldNames[0]
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showReservedFieldNames.dataSourceID.label");
		reservedFieldNames[1]
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showReservedFieldNames.rowNumber.label");
		reservedFieldNames[2]
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showReservedFieldNames.keepRecord.label");
		reservedFieldNames[3]
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showReservedFieldNames.total.label");

		showOptionListResult(
			responseHeaderMessage, 
			reservedFieldNames);			
	}
	
	private void showSupportedRIFDataTypes() {
		
		String responseHeaderMessage
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showSupportedRIFDataTypes.response");		
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();		
		String[] supportedRIFDataTypes = rifDataTypeFactory.getDataTypeCodes();

		showOptionListResult(
			responseHeaderMessage, 
			supportedRIFDataTypes);	
	}
	
	private void showRIFRequirementTypes() {
		String responseHeaderMessage
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showRIFRequirementTypes.response");
	
		String[] rifRequirementTypes = new String[4];
		rifRequirementTypes[0]
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showRIFRequirementTypes.rifRequiredField.label");
		rifRequirementTypes[1]
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showRIFRequirementTypes.extraField.label");
		rifRequirementTypes[2]
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showRIFRequirementTypes.fieldToIgnore.label");

		showOptionListResult(
			responseHeaderMessage, 
			rifRequirementTypes);	

	}
	
	private String[] showRIFSchemaAreas() {
		String responseHeaderMessage
			= RIFDataLoaderToolMessages.getMessage("dataLoaderBatchTool.showRIFSchemaAreas.response");	
		String[] rifSchemaAreas
			= RIFSchemaArea.getAllSchemaNames();
		showOptionListResult(responseHeaderMessage, rifSchemaAreas);
		return rifSchemaAreas;			
	}
	
	private String[] showRequiredFieldsForSchemaArea(
		final String[] commandLineArguments) 
		throws RIFServiceException {
		
		if (commandLineArguments.length < 2) {
			//ERROR: command is missing the parameter that indicating the schema area
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataLoaderBatchTool.showRIFSchemaAreas.error.noSchemaAreaSpecified");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.NO_SCHEMA_AREA_SPECIFIED, 
					errorMessage);
			throw rifServiceException;
		}
		
		String schemaAreaCode = commandLineArguments[1];
		
		RIFSchemaAreaPropertyManager rifSchemaAreaPropertyManager
			= new RIFSchemaAreaPropertyManager();
		RIFSchemaArea rifSchemaArea
			= RIFSchemaArea.getSchemaAreaFromName(schemaAreaCode);
		
		
		String responseHeaderMessage
			= RIFDataLoaderToolMessages.getMessage(
				"",
				rifSchemaArea.getName());
		
		String[] requiredFieldNamesForSchemaArea
			= rifSchemaAreaPropertyManager.getRequiredConvertFieldNames(rifSchemaArea);
		showOptionListResult(responseHeaderMessage, requiredFieldNamesForSchemaArea);
		
		return requiredFieldNamesForSchemaArea;
		
	}
	
	private String[] showWorkflowStages() {
		String responseMessage
			= RIFDataLoaderToolMessages.getMessage(
				"dataLoaderBatchTool.showWorkflowStages.response");
		String[] workFlowStages = WorkflowState.getAllStateNames();
		showOptionListResult(responseMessage, workFlowStages);	
		
		return workFlowStages;
		
	}
	
	
	private void showOptionListResult(
		final String responseHeaderMessage,
		final String[] listItems) {
		
		System.out.println(responseHeaderMessage);
		for (String listItem : listItems) {
			System.out.println("\t" + listItem);
		}		
	}
	
	
	private void processDataSet(
		final String[] commandLineArguments) 
		throws RIFServiceException {
		
		
		//Validate parameters
		if (commandLineArguments.length < 2) {
			//ERROR: command is missing the parameter that specifies the configuration XML file
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataLoaderBatchTool.processDataSet.error.noConfigurationFileSpecified");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.NO_CONFIGURATION_FILE_SPECIFIED, 
					errorMessage);
			throw rifServiceException;
		}
		
		String configurationFilePath = commandLineArguments[2];
		File rifWorfklowConfigurationFile = new File(configurationFilePath);
		if (rifWorfklowConfigurationFile.exists() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataLoaderBatchTool.processDataSet.error.nonExistentConfigurationFile");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.NON_EXISTENT_CONFIGURATION_FILE, 
					errorMessage);
			throw rifServiceException;
		}
		
		if (rifWorfklowConfigurationFile.canRead() == false) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataLoaderBatchTool.processDataSet.error.unreadableConfigurationFile");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError.UNABLE_TO_READ_CONFIGURATION_FILE, 
					errorMessage);
			throw rifServiceException;			
		}
		
		
		/*
		DataLoaderServiceAPI dataLoaderService = null;
		User rifManager = null;		
		
		RIFWorkflowReader rifWorkflowReader
			= new RIFWorkflowReader();
		rifWorkflowReader.readFile(rifWorfklowConfigurationFile);
			
		LinearWorkflow linearWorkflow
			= rifWorkflowReader.getLinearWorkflow();
			
		dataLoaderService.initialiseService();
					
		LinearWorkflowEnactor linearWorkflowEnactor
			= new LinearWorkflowEnactor(
				rifManager, 
				dataLoaderService);
		linearWorkflowEnactor.runWorkflow(linearWorkflow);
		
		*/
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


