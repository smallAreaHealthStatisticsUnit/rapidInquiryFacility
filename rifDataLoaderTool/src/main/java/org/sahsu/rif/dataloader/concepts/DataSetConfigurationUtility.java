package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;

/**
 *
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

public class DataSetConfigurationUtility {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetConfigurationUtility() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public static ArrayList<DataSetFieldConfiguration> getRequiredAndExtraFieldConfigurations(
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();		
		ArrayList<DataSetFieldConfiguration> requiredAndExtraFieldConfigurations
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldRequirementLevel fieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if (fieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD ||
					fieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) {

				requiredAndExtraFieldConfigurations.add(fieldConfiguration);
			}
		}

		return requiredAndExtraFieldConfigurations;	
	}

	
	/**
	 * filters data set field configurations based on data type and
	 * requirement level.  If requirement level is null it will retrieve
	 * all fields that are marked required or extra.  Note that it will not
	 * include fields marked "ignore"
	 * 
	 * @param rifDataType
	 * @param fieldRequirementLevel
	 * @return
	 */
	public static ArrayList<DataSetFieldConfiguration> getDataSetFieldConfigurations(
		final DataSetConfiguration dataSetConfiguration,
		final RIFDataType targetRIFDataType,
		final FieldRequirementLevel targetFieldRequirementLevel) {
				
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();	
		ArrayList<DataSetFieldConfiguration> results
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {

			RIFDataType currentDataType
				= fieldConfiguration.getRIFDataType();
			if (currentDataType.hasIdenticalContents(targetRIFDataType)) {
				FieldRequirementLevel currentFieldRequirementLevel
					= fieldConfiguration.getFieldRequirementLevel();
				if (targetFieldRequirementLevel == null) {
					if ((currentFieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) ||
						(currentFieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD)) {						
						results.add(fieldConfiguration);						
					}
				}
				else if (targetFieldRequirementLevel == currentFieldRequirementLevel) {
					results.add(fieldConfiguration);
				}
			}
		}
		return results;		
	}
	
	public static ArrayList<DataSetFieldConfiguration> getDataSetFieldConfigurations(
		final DataSetConfiguration dataSetConfiguration,
		final FieldPurpose targetFieldPurpose,
		final FieldRequirementLevel targetFieldRequirementLevel) {
					
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();	
		ArrayList<DataSetFieldConfiguration> results
			= new ArrayList<DataSetFieldConfiguration>();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldPurpose currentFieldPurpose
				= fieldConfiguration.getFieldPurpose();
			if (currentFieldPurpose == targetFieldPurpose) {
				FieldRequirementLevel currentFieldRequirementLevel
					= fieldConfiguration.getFieldRequirementLevel();
				if (targetFieldRequirementLevel == null) {
					if ((currentFieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) ||
						(currentFieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD)) {						
						results.add(fieldConfiguration);						
					}
				}
				else {	
					if (targetFieldRequirementLevel == currentFieldRequirementLevel) {
						results.add(fieldConfiguration);
					}
				}
			}
		}			
		return results;		
	}

	
	public static ArrayList<DataSetConfiguration> getDataSetConfigurations(
		final ArrayList<DataSetConfiguration> dataSetConfigurations, 
		final RIFSchemaArea filteringRIFSchemaArea) {
		
		ArrayList<DataSetConfiguration> results
			= new ArrayList<DataSetConfiguration>();
		
		if (filteringRIFSchemaArea == null) {
			return results;
		}
		
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			RIFSchemaArea rifSchemaArea = dataSetConfiguration.getRIFSchemaArea();
			if (rifSchemaArea == filteringRIFSchemaArea) {
				results.add(dataSetConfiguration);
			}
		}
		
		return results;
	}
	
	public static ArrayList<DataSetFieldConfiguration> getChangeAuditFields(
		final DataSetConfiguration dataSetConfiguration,
		final FieldChangeAuditLevel fieldChangeAuditLevel) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();	
		ArrayList<DataSetFieldConfiguration> changeAuditFields
			= new ArrayList<DataSetFieldConfiguration>();
		
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldRequirementLevel fieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if (fieldRequirementLevel != FieldRequirementLevel.IGNORE_FIELD) {
				FieldChangeAuditLevel currentFieldChangeAuditLevel
					= fieldConfiguration.getFieldChangeAuditLevel();
			
				if (currentFieldChangeAuditLevel == fieldChangeAuditLevel) {
					changeAuditFields.add(fieldConfiguration);				
				}
			}

		}
		
		return changeAuditFields;
		
	}
	
	
	public static ArrayList<DataSetFieldConfiguration> getFieldsWithConversionFunctions(
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
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

	public static ArrayList<DataSetFieldConfiguration> getFieldsWithoutConversionFunctions(
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();	
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
	
	public static DataSetFieldConfiguration getHighestGeographicalResolutionField(
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= getAllGeographicalResolutionFields(dataSetConfiguration);
		if (fieldConfigurations.size() == 0) {
			return null;
		}
		
		int lastIndex = fieldConfigurations.size() - 1;
		return fieldConfigurations.get(lastIndex);
	}

	public static ArrayList<DataSetFieldConfiguration> getAllGeographicalResolutionFields(
		final DataSetConfiguration dataSetConfiguration) {
		
		//We know that for covariates, there should exactly one required geographical 
		//resolution field
		ArrayList<DataSetFieldConfiguration> results
			= new ArrayList<DataSetFieldConfiguration>();
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldPurpose currentFieldPurpose
				= fieldConfiguration.getFieldPurpose();
			FieldRequirementLevel currentFieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if ((currentFieldPurpose == FieldPurpose.GEOGRAPHICAL_RESOLUTION) &&
				((currentFieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) ||
				 (currentFieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD))) {
				
				results.add(fieldConfiguration);
			}
		}
		return results;
	}

	public static DataSetFieldConfiguration getRequiredGeographicalResolutionField(
		final DataSetConfiguration dataSetConfiguration) {
		
		//We know that for covariates, there should exactly one required geographical 
		//resolution field
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration fieldConfiguration : fieldConfigurations) {
			FieldPurpose currentFieldPurpose
				= fieldConfiguration.getFieldPurpose();
			FieldRequirementLevel currentFieldRequirementLevel
				= fieldConfiguration.getFieldRequirementLevel();
			if ((currentFieldPurpose == FieldPurpose.GEOGRAPHICAL_RESOLUTION) &&
				((currentFieldRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF) ||
				 (currentFieldRequirementLevel == FieldRequirementLevel.EXTRA_FIELD))) {
				
				//there should only be one
				return fieldConfiguration;
			}
		}
		return null;
	}
	
	
	public static DataSetFieldConfiguration getRequiredYearField(
		final DataSetConfiguration dataSetConfiguration) {
		
		//We know that for covariates, there should exactly one required year field
		ArrayList<DataSetFieldConfiguration> results
			= getDataSetFieldConfigurations(
				dataSetConfiguration,
				RIFDataTypeFactory.RIF_YEAR_DATA_TYPE, 
				FieldRequirementLevel.REQUIRED_BY_RIF);
		return results.get(0);		
	}

	
	public static DataSetFieldConfiguration getHealthCodeField(
		final DataSetConfiguration numerator) {
		
		ArrayList<DataSetFieldConfiguration> fields
			= numerator.getFieldConfigurations();
		for (DataSetFieldConfiguration field : fields) {
			FieldRequirementLevel currentRequirementLevel
				= field.getFieldRequirementLevel();
			FieldPurpose fieldPurpose
				= field.getFieldPurpose();
			if (fieldPurpose == FieldPurpose.HEALTH_CODE &&
				currentRequirementLevel == FieldRequirementLevel.REQUIRED_BY_RIF){
				
				return field;
			}
		}
		
		return null;
	}

	
	public static ArrayList<DataSetFieldConfiguration> getCovariateFields(
		final DataSetConfiguration dataSetConfiguration) {
		
		// We know that for covariates, there should exactly one required year 
		// field
		ArrayList<DataSetFieldConfiguration> results = new ArrayList<DataSetFieldConfiguration>();
		ArrayList<DataSetFieldConfiguration> fields
			= dataSetConfiguration.getFieldConfigurations();
		for (DataSetFieldConfiguration field : fields) {
			if (field.getFieldPurpose() == FieldPurpose.COVARIATE) {
				results.add(field);				
			}		
		}

		return results;
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


