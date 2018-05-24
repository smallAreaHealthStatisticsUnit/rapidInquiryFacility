package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 * Centralises validation checks for each of denominator, numerator and
 * covariate data sets.  Often they are checking that either a required 
 * field exists, or that there is one or more fields that serve a specific
 * purpose.  The validation routines are activated whenever a RIF manager
 * tries to save the changes that are made to the way they define an 
 * imported data set in the 
 * {@link rifDataLoaderTool.presentationLayer.interactive.DataSetConfigurationEditorDialog}
 * dialog.  
 * 
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class DataSetValidator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetValidator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * A numerator must meet the following criteria:
	 * (1) It must have exactly one required field of type Year
	 * (2) It must have at least one Geographical Resolution Field
	 * (3) It must have exactly one required field of type Age
	 * (4) It must have exactly one required field of type Sex
	 * (5) It must have exactly one field of type Health Code
	 * @param numerator
	 * @throws RIFServiceException
	 */
	public void validateNumerator(final DataSetConfiguration numerator) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();

		checkSingleRequiredYearFieldExists(errorMessages, numerator);
		checkSingleRequiredSexFieldExists(errorMessages, numerator);
		checkSingleRequiredAgeFieldExists(errorMessages, numerator);
		checkMultipleResolutionFieldsExist(errorMessages, numerator);
		checkSingleRequiredHealthCodeFieldExists(errorMessages, numerator);
		checkSingleRequiredTotalFieldExists(errorMessages, numerator);
		
		checkErrorMessageCount(
				RIFDataLoaderToolError.INVALID_NUMERATOR,
				errorMessages);
	}
	
	public void validateDenominator(final DataSetConfiguration denominator) 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		checkSingleRequiredYearFieldExists(errorMessages, denominator);
		checkSingleRequiredSexFieldExists(errorMessages, denominator);
		checkSingleRequiredAgeFieldExists(errorMessages, denominator);
		checkMultipleResolutionFieldsExist(errorMessages, denominator);
		checkSingleRequiredTotalFieldExists(errorMessages, denominator);
	
		checkErrorMessageCount(
			RIFDataLoaderToolError.INVALID_DENOMINATOR, 
			errorMessages);		
	}
	
	public void validateCovariate(final DataSetConfiguration covariate) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		checkSingleRequiredYearFieldExists(errorMessages, covariate);
		checkSingleRequiredResolutionFieldExists(errorMessages, covariate);
		checkAtLeastOneCovariateField(errorMessages, covariate);
		
		checkErrorMessageCount(
			RIFDataLoaderToolError.INVALID_COVARIATE, 
			errorMessages);		
	}

	private void checkSingleRequiredYearFieldExists(
		final ArrayList<String> errorMessages,
		final DataSetConfiguration dataSet) {
		
		ArrayList<DataSetFieldConfiguration> yearFields
			= DataSetConfigurationUtility.getDataSetFieldConfigurations(
				dataSet, 
				RIFDataTypeFactory.RIF_YEAR_DATA_TYPE, 
				FieldRequirementLevel.REQUIRED_BY_RIF);
		if (yearFields.size() != 1) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.singleRequiredYearField");
			errorMessages.add(errorMessage);
		}
	}

	private void checkSingleRequiredSexFieldExists(
		final ArrayList<String> errorMessages,
		final DataSetConfiguration dataSet) {
		
		ArrayList<DataSetFieldConfiguration> sexFields
			= DataSetConfigurationUtility.getDataSetFieldConfigurations(
				dataSet, 
				RIFDataTypeFactory.RIF_SEX_DATA_TYPE, 
				FieldRequirementLevel.REQUIRED_BY_RIF);
		if (sexFields.size() != 1) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.singleRequiredSexField");
			errorMessages.add(errorMessage);
		}		
	
	}
	
	private void checkSingleRequiredAgeFieldExists(
		final ArrayList<String> errorMessages,
		final DataSetConfiguration dataSet) {
	
		ArrayList<DataSetFieldConfiguration> ageFields
			= DataSetConfigurationUtility.getDataSetFieldConfigurations(
				dataSet, 
				RIFDataTypeFactory.RIF_AGE_DATA_TYPE, 
				FieldRequirementLevel.REQUIRED_BY_RIF);
		if (ageFields.size() != 1) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.singleRequiredAgeField");
			errorMessages.add(errorMessage);
		}		
	}
	
	
	private void checkMultipleResolutionFieldsExist(
		final ArrayList<String> errorMessages,
		final DataSetConfiguration dataSet) {
	
		
		ArrayList<DataSetFieldConfiguration> resolutionFields
			= DataSetConfigurationUtility.getDataSetFieldConfigurations(
				dataSet, 
				FieldPurpose.GEOGRAPHICAL_RESOLUTION, 
				null);
		if (resolutionFields.size() < 2) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.multipleResolutionField");
			errorMessages.add(errorMessage);
		}		
		
		//Now make sure the type is correct.
		boolean atLeastOneResolutionFieldNotText = false;
		for (DataSetFieldConfiguration resolutionField : resolutionFields) {
			RIFDataType rifDataType = resolutionField.getRIFDataType();
			if (rifDataType != RIFDataTypeFactory.RIF_TEXT_DATA_TYPE) {
				atLeastOneResolutionFieldNotText = true;
				break;
			}
		}
		
		if (atLeastOneResolutionFieldNotText) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.resolutionFieldsMustBeText");
			errorMessages.add(errorMessage);
		}
	}
	
	private void checkSingleRequiredTotalFieldExists(
		final ArrayList<String> errorMessages,
		final DataSetConfiguration dataSet) {
		
		ArrayList<DataSetFieldConfiguration> totalFields
			= DataSetConfigurationUtility.getDataSetFieldConfigurations(
				dataSet, 
				FieldPurpose.TOTAL_COUNT, 
				FieldRequirementLevel.REQUIRED_BY_RIF);
		
		if (totalFields.size() != 1) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.singleRequiredTotalField");
			errorMessages.add(errorMessage);
		}		

		if (totalFields.size() >0) {
			RIFDataType rifDataType = totalFields.get(0).getRIFDataType();
			if (rifDataType != RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"dataSetValidator.error.totalMustBeInteger");
				errorMessages.add(errorMessage);
			}
		}
	}	
	
	private void checkSingleRequiredResolutionFieldExists(
		final ArrayList<String> errorMessages,
		final DataSetConfiguration dataSet) {

		ArrayList<DataSetFieldConfiguration> resolutionFields
			= DataSetConfigurationUtility.getDataSetFieldConfigurations(
				dataSet, 
				FieldPurpose.GEOGRAPHICAL_RESOLUTION, 
				FieldRequirementLevel.REQUIRED_BY_RIF);
		if (resolutionFields.size() < 1) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.atLeastOneResolutionField");
			errorMessages.add(errorMessage);
		}

		if (resolutionFields.size() > 0) {
			RIFDataType rifDataType = resolutionFields.get(0).getRIFDataType();
			if (rifDataType != RIFDataTypeFactory.RIF_TEXT_DATA_TYPE) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"dataSetValidator.error.resolutionFieldsMustBeText");
				errorMessages.add(errorMessage);
			}
		}
	}
	
	private void checkSingleRequiredHealthCodeFieldExists(
		final ArrayList<String> errorMessages,
		final DataSetConfiguration dataSet) {
		

		ArrayList<DataSetFieldConfiguration> healthCodeFields
			= DataSetConfigurationUtility.getDataSetFieldConfigurations(
				dataSet, 
				FieldPurpose.HEALTH_CODE, 
				FieldRequirementLevel.REQUIRED_BY_RIF);
		if (healthCodeFields.size() != 1) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.singleRequiredHealthCodeField");
			errorMessages.add(errorMessage);
		}		
	}
			
	private void checkAtLeastOneCovariateField(
		final ArrayList<String> errorMessages,
		final DataSetConfiguration dataSet) {
			

		ArrayList<DataSetFieldConfiguration> resolutionFields
			= DataSetConfigurationUtility.getDataSetFieldConfigurations(
				dataSet, 
				FieldPurpose.COVARIATE, 
				null);
		if (resolutionFields.size() == 0) {
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage(
					"dataSetValidator.error.atLeastOneCovariateField");
			errorMessages.add(errorMessage);
		}		
	}

	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	private void checkErrorMessageCount(
		final RIFDataLoaderToolError errorCode,
		final ArrayList<String> errorMessages) 
		throws RIFServiceException {
		
		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(errorCode, errorMessages);
			throw rifServiceException;
		}
		
	}

	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


