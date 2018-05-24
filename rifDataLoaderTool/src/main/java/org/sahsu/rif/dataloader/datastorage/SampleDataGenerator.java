package org.sahsu.rif.dataloader.datastorage;

import org.sahsu.rif.dataloader.concepts.CheckOption;
import org.sahsu.rif.dataloader.concepts.ConfigurationHints;
import org.sahsu.rif.dataloader.concepts.ConversionFunction;
import org.sahsu.rif.dataloader.concepts.ConversionFunctionFactory;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldChangeAuditLevel;
import org.sahsu.rif.dataloader.concepts.FieldPurpose;
import org.sahsu.rif.dataloader.concepts.FieldRequirementLevel;
import org.sahsu.rif.dataloader.concepts.GeographicalResolutionLevel;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.concepts.LinearWorkflow;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.concepts.WorkflowState;

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

	public GeographyMetaData getSahsulandGeographyMetaData() {
		GeographyMetaData geographyMetaData
			= GeographyMetaData.newInstance();
		
		Geography sahsulandGeography = Geography.newInstance();
		sahsulandGeography.setName("SAHSULAND");
		
		GeographicalResolutionLevel level1
			= GeographicalResolutionLevel.newInstance(
				1, 
				"Level 1", 
				"SAHSU_GRD_LEVEL1");
		sahsulandGeography.addLevel(level1);
		
		GeographicalResolutionLevel level2
			= GeographicalResolutionLevel.newInstance(
				2, 
				"Level 2", 
				"SAHSU_GRD_LEVEL2");
		sahsulandGeography.addLevel(level2);
		
		GeographicalResolutionLevel level3
			= GeographicalResolutionLevel.newInstance(
				3, 
				"Level 3", 
				"SAHSU_GRD_LEVEL3");
		sahsulandGeography.addLevel(level3);
		
		GeographicalResolutionLevel level4
			= GeographicalResolutionLevel.newInstance(
				4, 
				"Level 4", 
				"SAHSU_GRD_LEVEL4");
		sahsulandGeography.addLevel(level4);

		geographyMetaData.addGeography(sahsulandGeography);
		
		return geographyMetaData;
	}
	
	public DataLoaderToolConfiguration createSahsulandConfiguration() {
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= DataLoaderToolConfiguration.newInstance();
		
		GeographyMetaData geographyMetaData
			= getSahsulandGeographyMetaData();
		dataLoaderToolConfiguration.setGeographyMetaData(geographyMetaData);

		Geography geography
			= geographyMetaData.getGeography("SAHSULAND");

		ConfigurationHints configurationHints = new ConfigurationHints();
		dataLoaderToolConfiguration.setConfigurationHints(configurationHints);
				
		DataSetConfiguration denominator = createSahsulandDenominator();
		denominator.setGeography(geography);
		dataLoaderToolConfiguration.addDenominatorDataSetConfiguration(denominator);
		
		DataSetConfiguration numerator 
			= createSahsulandNumeratorConfiguration();
		numerator.setDependencyDataSetConfiguration(denominator);
		numerator.setGeography(geography);
			
		DataSetConfiguration level3Covariate = createSahsulandLevel3Covariates();
		level3Covariate.setGeography(geography);
		DataSetConfiguration level4Covariate = createSahsulandLevel4Covariates();
		level4Covariate.setGeography(geography);
		dataLoaderToolConfiguration.addCovariateDataSetConfiguration(level4Covariate);
		
		return dataLoaderToolConfiguration;
	}
	
	public LinearWorkflow createSahsulandNumeratorWorkflow() {
		
		/*
		 * In this example, we produce a linear work flow that starts
		 * at the LOAD work flow state and ends when the CONVERT state
		 * has completed
		 */
		LinearWorkflow numeratorWorkflow = LinearWorkflow.newInstance();

		numeratorWorkflow.setStartWorkflowState(WorkflowState.EXTRACT);
		numeratorWorkflow.setStopWorkflowState(WorkflowState.CONVERT);
		
		//This configuration describes the CSV file we want to load.
		//The goal of this data loading activity is to produce
		//the sahsuland_cancer table that already comes with the RIF installation
		DataSetConfiguration myNumeratorDataToLoad
			= createSahsulandNumeratorConfiguration();
		numeratorWorkflow.addDataSetConfiguration(myNumeratorDataToLoad);
		
		return numeratorWorkflow;		
	}
	
	public DataSetConfiguration createSahsulandNumeratorConfiguration() {

		ConversionFunctionFactory rifConversionFactory
			= ConversionFunctionFactory.newInstance();
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();
		
		
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
		 * Column 9: total
		 */
		
		DataSetConfiguration numeratorDataSetConfiguration
			= DataSetConfiguration.newInstance();
		numeratorDataSetConfiguration.setName("cancer_data");
		numeratorDataSetConfiguration.setVersion("2.0");
		numeratorDataSetConfiguration.setFilePath("C://rif_scripts/test_data/my_cancer_numerator_data.csv");
		numeratorDataSetConfiguration.setFileHasFieldNamesDefined(true);
		numeratorDataSetConfiguration.setDescription("Improved cancer data");
		
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
			RIFDataTypeFactory.RIF_YEAR_DATA_TYPE);
		//will appear as a comment in the schema for the table
		yearFieldConfiguration.setDuplicateIdentificationField(true);
		yearFieldConfiguration.setCoreFieldDescription("fiscal year of records");
		//if the table is destined to be a numerator data table then the schema will
		//expect that there is a field called "year"
		yearFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		yearFieldConfiguration.setEmptyValueAllowed(false);
		numeratorDataSetConfiguration.addFieldConfiguration(yearFieldConfiguration);
		
		DataSetFieldConfiguration sexFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"sex");
		sexFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_SEX_DATA_TYPE);
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


		DataSetFieldConfiguration ageFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"age");
		//the rif
		ageFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_AGE_DATA_TYPE);
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
			RIFDataTypeFactory.RIF_ICD_DATA_TYPE);
		icdFieldConfiguration.setDuplicateIdentificationField(true);
		icdFieldConfiguration.setCoreFieldDescription("eg: super output area");		
		//remember that each numerator needs exactly one health code field
		icdFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);

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

		DataSetFieldConfiguration totalFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"cancer_data", 
				"total");
		totalFieldConfiguration.setFieldPurpose(FieldPurpose.TOTAL_COUNT);
		totalFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
		
		totalFieldConfiguration.setDuplicateIdentificationField(false);
		totalFieldConfiguration.setCoreFieldDescription("total number of patients having some health outcome.");				

		//This is a flag that tells the data loader tool to not bother promoting the
		//field through any workflow state beyond Load
		totalFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);

		totalFieldConfiguration.setEmptyValueAllowed(false);
		numeratorDataSetConfiguration.addFieldConfiguration(totalFieldConfiguration);
		
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
		covariateWorkflow.setStartWorkflowState(WorkflowState.EXTRACT);
		covariateWorkflow.setStopWorkflowState(WorkflowState.PUBLISH);
		
		
		//Part II: Define which data set configurations we want to run through
		//the work flow we established above
				
		//We want to apply the same work flow to covariate data taken from two
		//files, one for level4 geographic resolution and one for level3
		DataSetConfiguration level3CovariatesConfiguration
			= createSahsulandLevel3Covariates();
		covariateWorkflow.addDataSetConfiguration(level3CovariatesConfiguration);
		DataSetConfiguration level4CovariatesConfiguration
			= createSahsulandLevel4Covariates();
		covariateWorkflow.addDataSetConfiguration(level4CovariatesConfiguration);
				
		//This work flow will be run inside 
		//rifDataLoaderTool.datastorage.LinearWorkflowEnactor
		
		return covariateWorkflow;
	}
	

	
	
	public DataSetConfiguration createSahsulandLevel3Covariates() {
		
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
		
		DataSetConfiguration sahuslandLevel3CovariatesConfiguration
			= DataSetConfiguration.newInstance();
		sahuslandLevel3CovariatesConfiguration.setName("sahsuland_covariates_level3");
		sahuslandLevel3CovariatesConfiguration.setVersion("1.5");
		sahuslandLevel3CovariatesConfiguration.setFilePath("C://rif_scripts/test_data/sahsuland_covariates_level3.csv");
		sahuslandLevel3CovariatesConfiguration.setFileHasFieldNamesDefined(true);
		sahuslandLevel3CovariatesConfiguration.setDescription("Covariate data for level3");		
		
		
		
		//indicates what part of the RIF schema we are targetting by
		//importing the data set
		sahuslandLevel3CovariatesConfiguration.setRIFSchemaArea(
			RIFSchemaArea.COVARIATE_DATA);
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level3", 
				"year");
		yearFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_YEAR_DATA_TYPE);
		yearFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
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
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
		sesFieldConfiguration.setDuplicateIdentificationField(true);
		sesFieldConfiguration.setCoreFieldDescription("socio-economic status in quintiles");				
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel3CovariatesConfiguration.addFieldConfiguration(sesFieldConfiguration);	
		
		DataSetFieldConfiguration ethnicityFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level3", 
				"ethnicity");	
		ethnicityFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		ethnicityFieldConfiguration.setDuplicateIdentificationField(true);
		ethnicityFieldConfiguration.setCoreFieldDescription("non-white ethnicity score with categories 1,2,3");				
		sahuslandLevel3CovariatesConfiguration.addFieldConfiguration(ethnicityFieldConfiguration);
			
		return sahuslandLevel3CovariatesConfiguration;
		
	}


	public DataSetConfiguration createSahsulandLevel4Covariates() {
		
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
				
		DataSetConfiguration sahuslandLevel4CovariatesConfiguration
			= DataSetConfiguration.newInstance();
		sahuslandLevel4CovariatesConfiguration.setName("sahsuland_covariates_level4");
		sahuslandLevel4CovariatesConfiguration.setVersion("1.5");
		sahuslandLevel4CovariatesConfiguration.setFilePath("C://rif_scripts/test_data/sahsuland_covariates_level4.csv");
		sahuslandLevel4CovariatesConfiguration.setFileHasFieldNamesDefined(true);
		sahuslandLevel4CovariatesConfiguration.setDescription("Covariate data for level4");		

		
		//indicates what part of the RIF schema we are targetting by
		//importing the data set
		sahuslandLevel4CovariatesConfiguration.setRIFSchemaArea(
			RIFSchemaArea.COVARIATE_DATA);
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"year");
		yearFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_YEAR_DATA_TYPE);
		yearFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		
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
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
		sesFieldConfiguration.setDuplicateIdentificationField(true);
		sesFieldConfiguration.setCoreFieldDescription("socio-economic status in quintiles");				
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(sesFieldConfiguration);	


		DataSetFieldConfiguration areatri1kmFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"areatri1km");	
		areatri1kmFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
		areatri1kmFieldConfiguration.setDuplicateIdentificationField(true);
		areatri1kmFieldConfiguration.setCoreFieldDescription("toxic release inventory within 1km of area (0=no, 1=yes)");				
		areatri1kmFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(areatri1kmFieldConfiguration);	
		

		DataSetFieldConfiguration nearDistFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"areatri1km");	
		nearDistFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_DOUBLE_DATA_TYPE);
		nearDistFieldConfiguration.setDuplicateIdentificationField(true);
		nearDistFieldConfiguration.setCoreFieldDescription("Distance (m) from area centroid to nearest TRI site");				
		nearDistFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(nearDistFieldConfiguration);	
		

		DataSetFieldConfiguration tri1kmFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"areatri1km");	
		tri1kmFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_DOUBLE_DATA_TYPE);
		tri1kmFieldConfiguration.setDuplicateIdentificationField(true);
		tri1kmFieldConfiguration.setCoreFieldDescription("Toxic Release Inventory within 1 km of area centroid (0=no, 1=yes)");				
		tri1kmFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(tri1kmFieldConfiguration);	
		
		
		DataSetFieldConfiguration ethnicityFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"sahsuland_covariates_level4", 
				"ethnicity");	
		ethnicityFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		ethnicityFieldConfiguration.setDuplicateIdentificationField(true);
		ethnicityFieldConfiguration.setCoreFieldDescription("non-white ethnicity score with categories 1,2,3");				
		sahuslandLevel4CovariatesConfiguration.addFieldConfiguration(ethnicityFieldConfiguration);
		
		
		return sahuslandLevel4CovariatesConfiguration;
		
	}

	
	public LinearWorkflow getLinearWorkflowTest4StudyID1() {
		LinearWorkflow linearWorkflow = LinearWorkflow.newInstance();
		DataSetConfiguration dataSetConfiguration
			= createTest4StudyID1ExtractConfiguration();
		linearWorkflow.addDataSetConfiguration(dataSetConfiguration);
		
		linearWorkflow.initialise();
		
		return linearWorkflow;		
	}
	
	public DataSetConfiguration createTest4StudyID1ExtractConfiguration() {
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		ConversionFunctionFactory rifConversionFunctionFactory
			= ConversionFunctionFactory.newInstance();
		
		DataSetConfiguration test4Study1Configuration
			= DataSetConfiguration.newInstance();
		test4Study1Configuration.setName("test4_study_id1_numerator");
		test4Study1Configuration.setFilePath("C://rif_scripts/test_data/test_4_study_id_1_extract.csv");
		test4Study1Configuration.setFileHasFieldNamesDefined(true);
		test4Study1Configuration.setDescription("numerator data from test 4 study 1");		
		test4Study1Configuration.setRIFSchemaArea(RIFSchemaArea.HEALTH_NUMERATOR_DATA);
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1_numerator", 
				"year");	
		yearFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_YEAR_DATA_TYPE);
		yearFieldConfiguration.setDuplicateIdentificationField(true);
		yearFieldConfiguration.setCoreFieldDescription("year of data");
		yearFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		yearFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		
		//@TODO Note: if it's a field required by the RIF, then it should always be optimised right?
		test4Study1Configuration.addFieldConfiguration(yearFieldConfiguration);	
		
		DataSetFieldConfiguration studyOrComparisonFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1", 
				"study_or_comparison");	
		studyOrComparisonFieldConfiguration.setDuplicateIdentificationField(true);
		studyOrComparisonFieldConfiguration.setCoreFieldDescription("C=Comparison Area S=Study Area");
		studyOrComparisonFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		studyOrComparisonFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		test4Study1Configuration.addFieldConfiguration(studyOrComparisonFieldConfiguration);	
				
		DataSetFieldConfiguration studyIDFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1_numerator", 
				"study_id");	
		studyIDFieldConfiguration.setRIFDataType(
		rifDataTypeFactory.getDataTypeFromCode(
			"rif_integer"));
		studyIDFieldConfiguration.setDuplicateIdentificationField(true);
		studyIDFieldConfiguration.setCoreFieldDescription("study identifier");
		studyIDFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		studyIDFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		test4Study1Configuration.addFieldConfiguration(studyIDFieldConfiguration);	
		
		
		DataSetFieldConfiguration areaIDFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1", 
				"area_id");	
		areaIDFieldConfiguration.setDuplicateIdentificationField(true);
		areaIDFieldConfiguration.setCoreFieldDescription("area identifier");
		areaIDFieldConfiguration.addCheckOption(CheckOption.PERCENT_EMPTY);
		areaIDFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		areaIDFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		test4Study1Configuration.addFieldConfiguration(areaIDFieldConfiguration);	
	
		
		DataSetFieldConfiguration bandIDFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1", 
				"band_id");	
		bandIDFieldConfiguration.setCoreFieldDescription("band identifier");
		bandIDFieldConfiguration.addCheckOption(CheckOption.PERCENT_EMPTY);
		bandIDFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		bandIDFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		test4Study1Configuration.addFieldConfiguration(bandIDFieldConfiguration);	
		
		
		DataSetFieldConfiguration sexFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1", 
				"sex");	
		sexFieldConfiguration.setRIFDataType(
		rifDataTypeFactory.getDataTypeFromCode("rif_sex"));
		sexFieldConfiguration.setDuplicateIdentificationField(true);
		sexFieldConfiguration.setCoreFieldDescription("sex");

		ConversionFunction ageSexConversionFunction
			= rifConversionFunctionFactory.getRIFConvertFunction("convert_age_sex");
		sexFieldConfiguration.setConvertFunction(ageSexConversionFunction);
		sexFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		sexFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		test4Study1Configuration.addFieldConfiguration(sexFieldConfiguration);	
			
		DataSetFieldConfiguration ageGroupFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1", 
				"age_group");	
		ageGroupFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_AGE_DATA_TYPE);
		ageGroupFieldConfiguration.setDuplicateIdentificationField(true);
		ageGroupFieldConfiguration.setCleanFieldName("age");
		ageGroupFieldConfiguration.setConvertFieldName("age");
		ageGroupFieldConfiguration.setCoreFieldDescription("a person's age");
		ageGroupFieldConfiguration.setConvertFunction(ageSexConversionFunction);
		ageGroupFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		ageGroupFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		test4Study1Configuration.addFieldConfiguration(ageGroupFieldConfiguration);	

		
		DataSetFieldConfiguration sesFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1", 
				"ses");	
		sesFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
		sesFieldConfiguration.setCoreFieldDescription("socio economic status");
		sesFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		sesFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		test4Study1Configuration.addFieldConfiguration(sesFieldConfiguration);	


		//pretending this is an icd field, just to pass validation
		DataSetFieldConfiguration inv1FieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1", 
				"inv_1");	
		inv1FieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		inv1FieldConfiguration.setFieldPurpose(FieldPurpose.HEALTH_CODE);
		test4Study1Configuration.addFieldConfiguration(inv1FieldConfiguration);	
			
		DataSetFieldConfiguration totalPopFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test4_study_id1", 
				"total_pop");	
		totalPopFieldConfiguration.setDuplicateIdentificationField(true);
		totalPopFieldConfiguration.setCoreFieldDescription("total population");
		totalPopFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		totalPopFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		test4Study1Configuration.addFieldConfiguration(totalPopFieldConfiguration);	

		
		return test4Study1Configuration;
	}
		
	
	public LinearWorkflow createMinimalLinearWorkflow() {
		
		LinearWorkflow minimalLinearWorkflow
			= LinearWorkflow.newInstance();
		minimalLinearWorkflow.setStartWorkflowState(WorkflowState.EXTRACT);
		minimalLinearWorkflow.setStopWorkflowState(WorkflowState.PUBLISH);
		
		DataSetConfiguration minimalDataSetConfiguration
			= createMinimalDataSetConfiguration();
		minimalLinearWorkflow.addDataSetConfiguration(minimalDataSetConfiguration);
		
		return minimalLinearWorkflow;
	}
	
	
	public LinearWorkflow testDataCleaning1Workflow() {
		LinearWorkflow linearWorkflow = LinearWorkflow.newInstance();
		linearWorkflow.addDataSetConfiguration(createDataCleaning1Configuration());
		linearWorkflow.setStartWorkflowState(WorkflowState.START);
		linearWorkflow.setStopWorkflowState(WorkflowState.STOP);
		
		return linearWorkflow;		
	}
	
	public DataSetConfiguration createDataCleaning1Configuration() {		
		
		DataSetConfiguration dataSetConfiguration
			= DataSetConfiguration.newInstance();
		dataSetConfiguration.setRIFSchemaArea(RIFSchemaArea.HEALTH_NUMERATOR_DATA);
		dataSetConfiguration.setName("test_cleaning1");
		//dataSetConfiguration.setFilePath("C://rif_scripts//test_data//test_cleaning1_2015.csv");
		dataSetConfiguration.setFilePath("C://rif_loader_demo//num_cleaning1_2015.csv");
		
		DataSetFieldConfiguration idFieldConfiguration
			= DataSetFieldConfiguration.newInstance("test_cleaning1", "id");
		idFieldConfiguration.setEmptyValueAllowed(false);

		//don't record sensitive data in the change logs
		idFieldConfiguration.setFieldChangeAuditLevel(FieldChangeAuditLevel.NONE);
		idFieldConfiguration.setDuplicateIdentificationField(true);
		idFieldConfiguration.setCoreFieldDescription("patient identifier");
		idFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		idFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		dataSetConfiguration.addFieldConfiguration(idFieldConfiguration);	
			
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetFieldConfiguration.newInstance("test_cleaning1", "year");
		yearFieldConfiguration.setEmptyValueAllowed(false);
		yearFieldConfiguration.setFieldChangeAuditLevel(FieldChangeAuditLevel.INCLUDE_FIELD_NAME_ONLY);
		yearFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_YEAR_DATA_TYPE);
		yearFieldConfiguration.setDuplicateIdentificationField(true);
		yearFieldConfiguration.setCoreFieldDescription("year of record");
		yearFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.REQUIRED_BY_RIF);
		yearFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		dataSetConfiguration.addFieldConfiguration(yearFieldConfiguration);	
	
		DataSetFieldConfiguration sexFieldConfiguration
			= DataSetFieldConfiguration.newInstance("test_cleaning1", "sex");
		idFieldConfiguration.setEmptyValueAllowed(false);

		//don't record sensitive data in the change logs
		sexFieldConfiguration.setFieldChangeAuditLevel(
			FieldChangeAuditLevel.INCLUDE_FIELD_CHANGE_DESCRIPTION);
		sexFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_SEX_DATA_TYPE);
		sexFieldConfiguration.setDuplicateIdentificationField(true);
		sexFieldConfiguration.setCoreFieldDescription("sex of patient");
		sexFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);

		ConversionFunctionFactory rifConversionFunctionFactory
			= ConversionFunctionFactory.newInstance();
		ConversionFunction ageSexConversionFunction
			= rifConversionFunctionFactory.getRIFConvertFunction("convert_age_sex");	
		sexFieldConfiguration.setConvertFunction(ageSexConversionFunction);		
		
		sexFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);		
		dataSetConfiguration.addFieldConfiguration(sexFieldConfiguration);	
		
		DataSetFieldConfiguration ageFieldConfiguration
			= DataSetFieldConfiguration.newInstance("test_cleaning1", "age");
		ageFieldConfiguration.setEmptyValueAllowed(false);

		//every age will change because an age value is being replaced
		//by a value from 1 to 5.  Therefore, don't bother trying to 
		//capture this change
		ageFieldConfiguration.setFieldChangeAuditLevel(
			FieldChangeAuditLevel.INCLUDE_FIELD_NAME_ONLY);
		ageFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_AGE_DATA_TYPE);
		ageFieldConfiguration.setDuplicateIdentificationField(true);
		ageFieldConfiguration.setCoreFieldDescription("age of patient");
		ageFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		ageFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		ageFieldConfiguration.setConvertFunction(ageSexConversionFunction);
		dataSetConfiguration.addFieldConfiguration(ageFieldConfiguration);	
		
		DataSetFieldConfiguration dobFieldConfiguration
			= DataSetFieldConfiguration.newInstance("test_cleaning1", "dob");
		dobFieldConfiguration.setEmptyValueAllowed(false);		
		dobFieldConfiguration.setFieldChangeAuditLevel(
			FieldChangeAuditLevel.INCLUDE_FIELD_CHANGE_DESCRIPTION);
		dobFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_DATE_DATA_TYPE);
		dobFieldConfiguration.setDuplicateIdentificationField(true);
		dobFieldConfiguration.setCoreFieldDescription("date of birth of patient");
		dobFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		dobFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		dobFieldConfiguration.addCheckOption(CheckOption.PERCENT_EMPTY);
		dataSetConfiguration.addFieldConfiguration(dobFieldConfiguration);		
		
		DataSetFieldConfiguration postalCodeFieldConfiguration
			= DataSetFieldConfiguration.newInstance("test_cleaning1", "postal_code");
		postalCodeFieldConfiguration.setEmptyValueAllowed(false);		
		postalCodeFieldConfiguration.setFieldChangeAuditLevel(
			FieldChangeAuditLevel.NONE);
		postalCodeFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_UK_POSTAL_CODE_DATA_TYPE);
		postalCodeFieldConfiguration.setDuplicateIdentificationField(true);
		postalCodeFieldConfiguration.setCoreFieldDescription("UK postal code");
		postalCodeFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		postalCodeFieldConfiguration.addCheckOption(CheckOption.PERCENT_EMPTY);
		postalCodeFieldConfiguration.addCheckOption(CheckOption.PERCENT_EMPTY_PER_YEAR);
		postalCodeFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		
		dataSetConfiguration.addFieldConfiguration(postalCodeFieldConfiguration);		
	
		DataSetFieldConfiguration scoreFieldConfiguration
			= DataSetFieldConfiguration.newInstance("test_cleaning1", "score");
		scoreFieldConfiguration.setEmptyValueAllowed(false);		
		scoreFieldConfiguration.setFieldChangeAuditLevel(
			FieldChangeAuditLevel.NONE);
		scoreFieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
		scoreFieldConfiguration.setDuplicateIdentificationField(true);
		scoreFieldConfiguration.setCoreFieldDescription("some kind of score that needs to be quintilised");
		scoreFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		scoreFieldConfiguration.setFieldPurpose(FieldPurpose.COVARIATE);
		scoreFieldConfiguration.addCheckOption(CheckOption.PERCENT_EMPTY_PER_YEAR);
		dataSetConfiguration.addFieldConfiguration(scoreFieldConfiguration);		
		
		DataSetFieldConfiguration icd1FieldConfiguration
			= DataSetFieldConfiguration.newInstance("test_cleaning1", "icd1");
		icd1FieldConfiguration.setEmptyValueAllowed(false);		
		icd1FieldConfiguration.setFieldChangeAuditLevel(
			FieldChangeAuditLevel.INCLUDE_FIELD_CHANGE_DESCRIPTION);
		icd1FieldConfiguration.setRIFDataType(
			RIFDataTypeFactory.RIF_ICD_DATA_TYPE);
		icd1FieldConfiguration.setDuplicateIdentificationField(true);
		icd1FieldConfiguration.setCoreFieldDescription("health code");
		icd1FieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		icd1FieldConfiguration.setFieldPurpose(FieldPurpose.HEALTH_CODE);
		dataSetConfiguration.addFieldConfiguration(icd1FieldConfiguration);		
		
		
		DataSetFieldConfiguration level1FieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test_cleaning1", 
				"level1");
		level1FieldConfiguration.setEmptyValueAllowed(false);
		level1FieldConfiguration.setFieldChangeAuditLevel(
			FieldChangeAuditLevel.NONE);
		level1FieldConfiguration.setDuplicateIdentificationField(false);
		level1FieldConfiguration.setCoreFieldDescription("level 1 resolution");
		level1FieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		level1FieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		dataSetConfiguration.addFieldConfiguration(level1FieldConfiguration);
				
		DataSetFieldConfiguration level2FieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				"test_cleaning1", 
				"level2");
		level2FieldConfiguration.setEmptyValueAllowed(false);
		level2FieldConfiguration.setFieldChangeAuditLevel(
			FieldChangeAuditLevel.NONE);
		level2FieldConfiguration.setDuplicateIdentificationField(true);
		level2FieldConfiguration.setCoreFieldDescription("level 2 resolution");
		level2FieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		level2FieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		dataSetConfiguration.addFieldConfiguration(level2FieldConfiguration);
				
		return dataSetConfiguration;
	}

	public DataSetConfiguration createSahsulandDenominator() {
		DataSetConfiguration denominator
			= createSahsulandNumeratorConfiguration();
		denominator.deleteField("icd");
		denominator.setDescription("SAHSULAND population");
		denominator.setRIFSchemaArea(RIFSchemaArea.POPULATION_DENOMINATOR_DATA);
		denominator.setDependencyDataSetConfiguration(null);	
		return denominator;
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
		
		DataSetConfiguration ethnicityLevel3DataSetConfiguration
			= DataSetConfiguration.newInstance();
		ethnicityLevel3DataSetConfiguration.setName("ethnicity_covariate_level3");
		//indicates what part of the RIF schema we are targetting by
		//importing the data set
		ethnicityLevel3DataSetConfiguration.setRIFSchemaArea(
			RIFSchemaArea.COVARIATE_DATA);
		
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
			RIFDataTypeFactory.RIF_INTEGER_DATA_TYPE);
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


