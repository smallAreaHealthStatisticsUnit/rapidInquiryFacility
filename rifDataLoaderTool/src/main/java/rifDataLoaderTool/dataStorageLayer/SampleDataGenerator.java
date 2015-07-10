package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration;
import rifDataLoaderTool.businessConceptLayer.FieldPurpose;
import rifDataLoaderTool.businessConceptLayer.FieldRequirementLevel;
import rifDataLoaderTool.businessConceptLayer.LinearWorkflow;
import rifDataLoaderTool.businessConceptLayer.RIFConversionFunctionFactory;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeFactory;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaArea;
import rifDataLoaderTool.businessConceptLayer.WorkflowState;


/**
 * Contains sample data set configurations designed to import data into
 * sahsuland_cancer and other data sources.
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

public class SampleDataGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public SampleDataGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	public LinearWorkflow createSahsulandNumeratorWorkflow() {
		
		/*
		 * In this example, we produce a linear work flow that starts
		 * at the LOAD work flow state and ends when the CONVERT state
		 * has completed
		 */
		LinearWorkflow numeratorWorkflow = LinearWorkflow.newInstance();

		numeratorWorkflow.setStartWorkflowState(WorkflowState.LOAD);
		numeratorWorkflow.setStopWorkflowState(WorkflowState.CONVERT);
		
		//This configuration describes the CSV file we want to load.
		//The goal of this data loading activity is to produce
		//the sahsuland_cancer table that already comes with the RIF installation
		DataSetConfiguration myNumeratorDataToLoad
			= createCancerNumeratorConfiguration();
		numeratorWorkflow.addDataSetConfiguration(myNumeratorDataToLoad);
		
		return numeratorWorkflow;		
	}
	
	public DataSetConfiguration createCancerNumeratorConfiguration() {

		RIFConversionFunctionFactory rifConversionFactory
			= RIFConversionFunctionFactory.newInstance();
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		
		/*		
		 * This is a data set configuration for a CSV file that contains
		 * data we want to promote as a numerator table in the RIF Database.
		 * It might be called "our_cancer_data.csv" and we'll track it in the
		 * database using the name "cancer_data".  This name will be used to
		 * create a sequence of temporary tables that are used to transform
		 * the CSV file into a published table.  Our imagined CSV file would
		 * contain the following fields:
		 * 
		 * Column 1: year
		 * Column 2: age
		 * Column 3: sex
		 * Column 4: level1
		 * Column 5: level2
		 * Column 6: level3
		 * Column 7: level4
		 * Column 8: icd
		 * Column 9: health_provider_code [to be ignored by the RIF]
		 * Column 10: treatment_duration [to be ignored by the RIF]
		 * 
		 */
		
		
		
		DataSetConfiguration numeratorDataSetConfiguration
			= DataSetConfiguration.newInstance();
		numeratorDataSetConfiguration.setName("cancer_data");
		//indicates what part of the RIF schema we are targetting by
		//importing the data set
		numeratorDataSetConfiguration.setRIFSchemaArea(
			RIFSchemaArea.HEALTH_NUMERATOR_DATA);

		/*
		 * Note that in the following field declarations, we have
		 * preserved the same value for loadFieldName, cleanFieldName
		 * and convertFieldName.  In practice, these names may be differ
		 * from one another
		 */
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"year");
		//this data type comes with transformation rules that can help
		//validate field values so they are reasonable (eg: start with a "19" or a "20")
		yearFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_year"));
		//will appear as a comment in the schema for the table
		yearFieldConfiguration.setDuplicateIdentificationField(true);
		yearFieldConfiguration.setCoreFieldDescription("fiscal year of records");
		//if the table is destined to be a numerator data table then the schema will
		//expect that there is a field called "year"
		yearFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		yearFieldConfiguration.setEmptyValueAllowed(false);
		numeratorDataSetConfiguration.addFieldConfiguration(yearFieldConfiguration);
	

		DataSetFieldConfiguration ageFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"age");
		//the rif
		ageFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_age"));
		ageFieldConfiguration.setDuplicateIdentificationField(true);
		ageFieldConfiguration.setCoreFieldDescription("age of patient");
		ageFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		//Ensuring that this field is used as input to the function
		//convert_age_sex, which should cause a cleaned version of this data
		//set to have its age and sex fields mapped to a single "age_sex_group"
		//field in the converted version
		ageFieldConfiguration.setConvertFunction(
			rifConversionFactory.getRIFConvertFunction("convert_age_sex"));
		ageFieldConfiguration.setEmptyValueAllowed(false);
		numeratorDataSetConfiguration.addFieldConfiguration(ageFieldConfiguration);

		DataSetFieldConfiguration sexFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"sex");
		sexFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_sex"));
		sexFieldConfiguration.setDuplicateIdentificationField(true);
		sexFieldConfiguration.setCoreFieldDescription("sex of patient");
		//We expect that there will be likely be a field called 'sex' and if it appears
		//in the configuration, we expect it to map to age_sex_group during the convert
		//stage
		sexFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		//Ensuring that this field is used as input to the function
		//convert_age_sex, which should cause a cleaned version of this data
		//set to have its age and sex fields mapped to a single "age_sex_group"
		//field in the converted version
		sexFieldConfiguration.setConvertFunction(
			rifConversionFactory.getRIFConvertFunction("convert_age_sex"));
		sexFieldConfiguration.setEmptyValueAllowed(false);
		numeratorDataSetConfiguration.addFieldConfiguration(sexFieldConfiguration);


		/*
		 * The following field definitions describe geographical resolutions
		 * "level1" is the broadest level and may refer to something like state or
		 * region.  "level4" could refer to something like the UK's super-output area,
		 * which describes a well defined administrative area that may include just
		 * a few streets.
		 */

		DataSetFieldConfiguration level1ResolutionFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"level1");
		//by default, the data type is "TEXT" TextRIFDataType
		//level1ResolutionFieldConfiguration.setRIFDataType
		//sexFieldConfiguration.setRIFDataType(
		//	rifDataTypeFactory.getDataType("rif_text"));

		
		//we do not have to use all fields in a table to determine whether two rows
		//are duplicates of one another
		//level1ResolutionFieldConfiguration.setDuplicateIdentificationField(false);
		level1ResolutionFieldConfiguration.setCoreFieldDescription("most general geographic resolution level, eg: region");		
		
		//There is no requirement that we have a field called "level1" in our numerator table.
		//This is why the field requirement level is set to "EXTRA_FIELD" rather than
		//"REQUIRED_BY_RIF"
		//
		//However, we do have a requirement that at least one field describes a geographical
		//resolution level. We can validate the data set configuration and ensure that it has
		//at least one of these kinds of fields
		level1ResolutionFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		level1ResolutionFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		level1ResolutionFieldConfiguration.setEmptyValueAllowed(false);
		numeratorDataSetConfiguration.addFieldConfiguration(level1ResolutionFieldConfiguration);
		

		DataSetFieldConfiguration level2ResolutionFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"level2");
		//by default, this field is not considered in the process of identifying
		//duplicate records
		//level2ResolutionFieldConfiguration.setDuplicateIdentificationField(false);
		level2ResolutionFieldConfiguration.setCoreFieldDescription("eg: district");				
		level2ResolutionFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		level2ResolutionFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		//We will allow blank values for resolution
		level2ResolutionFieldConfiguration.setEmptyValueAllowed(true);
		numeratorDataSetConfiguration.addFieldConfiguration(level2ResolutionFieldConfiguration);
		

		DataSetFieldConfiguration level3ResolutionFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"level3");
		//by default, this field is not considered in the process of identifying
		//duplicate records
		//level3ResolutionFieldConfiguration.setDuplicateIdentificationField(false);

		level3ResolutionFieldConfiguration.setCoreFieldDescription("eg: output area");				
		level3ResolutionFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		level3ResolutionFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		//We will allow blank values for resolution
		level3ResolutionFieldConfiguration.setEmptyValueAllowed(true);
		numeratorDataSetConfiguration.addFieldConfiguration(level3ResolutionFieldConfiguration);
		

		DataSetFieldConfiguration level4ResolutionFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"level4");
		
		//We can use this field along with year, age and sex to identify duplicat records
		level4ResolutionFieldConfiguration.setDuplicateIdentificationField(true);
		level4ResolutionFieldConfiguration.setCoreFieldDescription("eg: super output area");				
		level4ResolutionFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		level4ResolutionFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		//We will allow blank values for resolution
		level4ResolutionFieldConfiguration.setEmptyValueAllowed(true);
		numeratorDataSetConfiguration.addFieldConfiguration(level4ResolutionFieldConfiguration);
		
		/*
		 * Now we'll add a health code field so that RIF scientists can specify
		 * records that correspond to some kind of health outcome of interest, typically
		 * from an ICD 10 code
		 * 
		 * In some data sets such as the UK's HES records, we would expect multiple columns
		 * named icd1, icd2, icd3,...icd20, each of which could contain a health code.  In
		 * this example, we only have one health code field
		 */
		DataSetFieldConfiguration icdFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"icd");
		icdFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_icd_code"));
		icdFieldConfiguration.setDuplicateIdentificationField(true);
		icdFieldConfiguration.setCoreFieldDescription("eg: super output area");				
		icdFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);

		//If the data set is destined to be a numerator table, then it must have 
		//at least one health code
		icdFieldConfiguration.setFieldPurpose(FieldPurpose.HEALTH_CODE);
		icdFieldConfiguration.setEmptyValueAllowed(true);
		numeratorDataSetConfiguration.addFieldConfiguration(icdFieldConfiguration);

		/*
		 * We'll also include two fields that appear in the CSV file but ones which
		 * the RIF will not bother to import. Note that we must have a field configuration
		 * defined for each field we expect to encounter in the CSV file, whether the
		 * RIF manager is interested in it or not. 
		 */		

		DataSetFieldConfiguration healthProviderFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"health_provider_code");
		healthProviderFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_icd_code"));
		healthProviderFieldConfiguration.setDuplicateIdentificationField(true);
		healthProviderFieldConfiguration.setCoreFieldDescription("code for hospital where visit was recorded");				

		//This is a flag that tells the data loader tool to not bother promoting the
		//field through any workflow state beyond Load
		healthProviderFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.IGNORE_FIELD);

		//By default, the purpose of this field will be "Other"
		//healthProviderFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		
		//By default, the field will allow empty values
		//healthProviderFieldConfiguration.setEmptyValueAllowed(true);
		numeratorDataSetConfiguration.addFieldConfiguration(healthProviderFieldConfiguration);
				
		
		DataSetFieldConfiguration treatmentDurationFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"treatment_duration");
		treatmentDurationFieldConfiguration.setDuplicateIdentificationField(true);
		//This is a flag that tells the data loader tool to not bother promoting the
		//field through any workflow state beyond Load
		treatmentDurationFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.IGNORE_FIELD);
		numeratorDataSetConfiguration.addFieldConfiguration(treatmentDurationFieldConfiguration);	
		
		return numeratorDataSetConfiguration;
		
	}

	public LinearWorkflow createSahsulandCovariateWorkflow() {
		
		LinearWorkflow covariateWorkflow = LinearWorkflow.newInstance();

		//Part I: Establish how much we will promote a data set through
		//different workflow stages
		
		//We want the workflow to go all the way from the step where we
		//load the data to the step where we publish it.  Not all work flows
		//have to go through all the steps in the data loading process.  For
		//example, we may want to go from LOAD --> CONVERT and 
		//OPTIMISE --> PUBLISH so that we can use converted tables as part
		//of a BranchedWorkflow - one that uses split and combine operations.
		//For this example though, we use LOAD --> PUBLISH 
		covariateWorkflow.setStartWorkflowState(WorkflowState.LOAD);
		covariateWorkflow.setStopWorkflowState(WorkflowState.PUBLISH);
		
		
		//Part II: Define which data set configurations we want to run through
		//the work flow we established above
				
		//We want to apply the same work flow to covariate data taken from two
		//files, one for level4 geographic resolution and one for level3
		DataSetConfiguration level3CovariatesConfiguration
			= createSahsulandLevel3CovariatesCovariateConfiguration();
		covariateWorkflow.addDataSetConfiguration(level3CovariatesConfiguration);
		DataSetConfiguration level4CovariatesConfiguration
			= createSahsulandLevel4CovariatesCovariateConfiguration();
		covariateWorkflow.addDataSetConfiguration(level4CovariatesConfiguration);
				
		//This work flow will be run inside 
		//rifDataLoaderTool.dataStorageLayer.LinearWorkflowEnactor
		
		return covariateWorkflow;
	}
	

	
	
	public DataSetConfiguration createSahsulandLevel3CovariatesCovariateConfiguration() {
		
		/*
		 * Here we create a data set configuration for a CSV file that will contain
		 * values for ethnicity for level3 geographical resolution.  We are anticipating
		 * that the CSV file will map to sahsuland_covariates_level3, and will have
		 * these fields:
		 * 
		 * year
		 * level3
		 * ses
		 * ethnicity
		 */	
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		
		DataSetConfiguration sahuslandLevel3CovariatesConfiguration
			= DataSetConfiguration.newInstance();
		sahuslandLevel3CovariatesConfiguration.setName("sahsuland_covariates_level3");
		//indicates what part of the RIF schema we are targetting by
		//importing the data set
		sahuslandLevel3CovariatesConfiguration.setRIFSchemaArea(
			RIFSchemaArea.GEOMETRY_DATA);
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level3", 
				"year");
		yearFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_year"));
		yearFieldConfiguration.setDuplicateIdentificationField(true);
		yearFieldConfiguration.setCoreFieldDescription("calendar year of covariate data");				
		sahuslandLevel3CovariatesConfiguration.addFieldConfiguration(yearFieldConfiguration);


		DataSetFieldConfiguration level3ResolutionFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level3", 
				"level3");
		level3ResolutionFieldConfiguration.setCoreFieldDescription("eg: output area");				
		level3ResolutionFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		level3ResolutionFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		//We will allow blank values for resolution
		level3ResolutionFieldConfiguration.setEmptyValueAllowed(true);
		sahuslandLevel3CovariatesConfiguration.addFieldConfiguration(level3ResolutionFieldConfiguration);

		DataSetFieldConfiguration sesFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level3", 
				"ses");	
		sesFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_integer"));
		sesFieldConfiguration.setDuplicateIdentificationField(true);
		sesFieldConfiguration.setCoreFieldDescription("socio-economic status in quintiles");				
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel3CovariatesConfiguration.addFieldConfiguration(sesFieldConfiguration);	
		
		DataSetFieldConfiguration ethnicityFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level3", 
				"ethnicity");	
		ethnicityFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_integer"));
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		ethnicityFieldConfiguration.setDuplicateIdentificationField(true);
		ethnicityFieldConfiguration.setCoreFieldDescription("non-white ethnicity score with categories 1,2,3");				
		sahuslandLevel3CovariatesConfiguration.addFieldConfiguration(ethnicityFieldConfiguration);
			
		return sahuslandLevel3CovariatesConfiguration;
		
	}


	public DataSetConfiguration createSahsulandLevel4CovariatesCovariateConfiguration() {
		
		/*
		 * Here we create a data set configuration for a CSV file that will contain
		 * values for ethnicity for level4 geographical resolution.  We are anticipating
		 * that the CSV file will map to sahsuland_covariates_level4, and will have
		 * these fields:
		 * 
		 * year
		 * level4
		 * ses
		 * areatri1km
		 * near_dist
		 * tri_1km
		 */		
		

		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		
		DataSetConfiguration sahuslandLevel4CovariatesConfiguration
			= DataSetConfiguration.newInstance();
		sahuslandLevel4CovariatesConfiguration.setName("sahsuland_covariates_level4");
		//indicates what part of the RIF schema we are targetting by
		//importing the data set
		sahuslandLevel4CovariatesConfiguration.setRIFSchemaArea(
			RIFSchemaArea.GEOMETRY_DATA);
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"year");
		yearFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_year"));
		yearFieldConfiguration.setDuplicateIdentificationField(true);
		yearFieldConfiguration.setCoreFieldDescription("calendar year of covariate data");				
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(yearFieldConfiguration);


		DataSetFieldConfiguration level3ResolutionFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"level4");
		level3ResolutionFieldConfiguration.setCoreFieldDescription("eg: output area");				
		level3ResolutionFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		level3ResolutionFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		//We will allow blank values for resolution
		level3ResolutionFieldConfiguration.setEmptyValueAllowed(true);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(level3ResolutionFieldConfiguration);

		DataSetFieldConfiguration sesFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"ses");	
		sesFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_integer"));
		sesFieldConfiguration.setDuplicateIdentificationField(true);
		sesFieldConfiguration.setCoreFieldDescription("socio-economic status in quintiles");				
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(sesFieldConfiguration);	


		DataSetFieldConfiguration areatri1kmFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"areatri1km");	
		areatri1kmFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_integer"));
		areatri1kmFieldConfiguration.setDuplicateIdentificationField(true);
		areatri1kmFieldConfiguration.setCoreFieldDescription("toxic release inventory within 1km of area (0=no, 1=yes)");				
		areatri1kmFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(areatri1kmFieldConfiguration);	
		

		DataSetFieldConfiguration nearDistFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"areatri1km");	
		nearDistFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_double"));
		nearDistFieldConfiguration.setDuplicateIdentificationField(true);
		nearDistFieldConfiguration.setCoreFieldDescription("Distance (m) from area centroid to nearest TRI site");				
		nearDistFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(nearDistFieldConfiguration);	
		

		DataSetFieldConfiguration tri1kmFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"areatri1km");	
		tri1kmFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_integer"));
		tri1kmFieldConfiguration.setDuplicateIdentificationField(true);
		tri1kmFieldConfiguration.setCoreFieldDescription("Toxic Release Inventory within 1 km of area centroid (0=no, 1=yes)");				
		tri1kmFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(tri1kmFieldConfiguration);	
		
		
		DataSetFieldConfiguration ethnicityFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"ethnicity");	
		ethnicityFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_integer"));
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		ethnicityFieldConfiguration.setDuplicateIdentificationField(true);
		ethnicityFieldConfiguration.setCoreFieldDescription("non-white ethnicity score with categories 1,2,3");				
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(ethnicityFieldConfiguration);
		
		
		return sahuslandLevel4CovariatesConfiguration;
		
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public LinearWorkflow createMinimalLinearWorkflow() {
		
		LinearWorkflow minimalLinearWorkflow
			= LinearWorkflow.newInstance();
		minimalLinearWorkflow.setStartWorkflowState(WorkflowState.LOAD);
		minimalLinearWorkflow.setStopWorkflowState(WorkflowState.PUBLISH);
		
		DataSetConfiguration minimalDataSetConfiguration
			= createMinimalDataSetConfiguration();
		minimalLinearWorkflow.addDataSetConfiguration(minimalDataSetConfiguration);
		
		return minimalLinearWorkflow;
	}
	
	
	public DataSetConfiguration createMinimalDataSetConfiguration() {
		
		/*
		 * Here we create a data set configuration for a CSV file that will contain
		 * values for ethnicity for level3 geographical resolution.  The fields will
		 * include:
		 * 
		 * level3
		 * ethnicity
		 */		
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		
		DataSetConfiguration ethnicityLevel3DataSetConfiguration
			= DataSetConfiguration.newInstance();
		ethnicityLevel3DataSetConfiguration.setName("ethnicity_covariate_level3");
		//indicates what part of the RIF schema we are targetting by
		//importing the data set
		ethnicityLevel3DataSetConfiguration.setRIFSchemaArea(
			RIFSchemaArea.GEOMETRY_DATA);
		
		DataSetFieldConfiguration level3ResolutionFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"ethnicity_covariate_level3", 
				"level3");		
		level3ResolutionFieldConfiguration.setDuplicateIdentificationField(true);
		level3ResolutionFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		level3ResolutionFieldConfiguration.setCoreFieldDescription("map areas expressed at level 3 resolution");				
		ethnicityLevel3DataSetConfiguration.addFieldConfiguration(level3ResolutionFieldConfiguration);
		
		DataSetFieldConfiguration ethnicityFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"ethnicity_covariate_level3", 
				"ethnicity");	
		ethnicityFieldConfiguration.setRIFDataType(
			rifDataTypeFactory.getDataType("rif_integer"));
		ethnicityFieldConfiguration.setDuplicateIdentificationField(true);
		ethnicityFieldConfiguration.setCoreFieldDescription("map areas expressed at level 3 resolution");				
		ethnicityLevel3DataSetConfiguration.addFieldConfiguration(ethnicityFieldConfiguration);
		
		
		return ethnicityLevel3DataSetConfiguration;
		
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


