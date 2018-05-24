package org.sahsu.rif.dataloader.fileformats;

import java.io.File;

import org.sahsu.rif.dataloader.concepts.ConfigurationHints;
import org.sahsu.rif.dataloader.concepts.ConversionFunctionFactory;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldPurpose;
import org.sahsu.rif.dataloader.concepts.FieldRequirementLevel;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.concepts.HealthTheme;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;

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

public class DLTestDataGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DLTestDataGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public DataLoaderToolConfiguration createExampleDataLoaderToolConfiguration() {
		
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= DataLoaderToolConfiguration.newInstance();
		
		GeographyMetaData geographyMetaData
			= createTestGeographyMetaData();
		dataLoaderToolConfiguration.setGeographyMetaData(geographyMetaData);
		
		//Create Health Themes
		HealthTheme cancerHealthTheme
			= HealthTheme.newInstance("cancers", "cancer registry data");
		HealthTheme respiratoryHealthTheme
			= HealthTheme.newInstance("respiratory disorders", "Asthma UK data set");
		HealthTheme skinDiseasesHealthTheme
			= HealthTheme.newInstance("skin diseases", "");
		dataLoaderToolConfiguration.addHealthTheme(cancerHealthTheme);
		dataLoaderToolConfiguration.addHealthTheme(respiratoryHealthTheme);
		dataLoaderToolConfiguration.addHealthTheme(skinDiseasesHealthTheme);
		
		//Create denominator data set
		Geography ukGeography = geographyMetaData.getGeography("UK");
				
		DataSetConfiguration ukDenominator
			= createPopulationHealthDataSet(
				RIFSchemaArea.POPULATION_DENOMINATOR_DATA,
				ukGeography,
				null,
				null,
				"UK population",
				"1.0",
				"Describes demographics of UK population",
				"C:\\rifDataLoaderTool\\brain_cancer_data_set1");	
		dataLoaderToolConfiguration.addDenominatorDataSetConfiguration(ukDenominator);
		//Create a cancer numerator data set
		
		DataSetConfiguration brainCancerNumerator		
			= createPopulationHealthDataSet(
				RIFSchemaArea.HEALTH_NUMERATOR_DATA,
				ukGeography,
				cancerHealthTheme,
				ukDenominator,
				"brain cancer",
				"1.0",
				"Describes UK patients who have developed brain cancer",
				"C:\\rifDataLoaderTool\\brain_cancer_data_set1");	
		dataLoaderToolConfiguration.addNumeratorDataSetConfiguration(brainCancerNumerator);

		DataSetConfiguration lungCancerNumerator		
			= createPopulationHealthDataSet(
				RIFSchemaArea.HEALTH_NUMERATOR_DATA,
				ukGeography,
				cancerHealthTheme,
				ukDenominator,
				"lung cancer",
				"1.0",
				"Describes UK patients who have developed lung cancer",
				"C:\\rifDataLoaderTool\\lung_cancer_data_set1");	
		dataLoaderToolConfiguration.addNumeratorDataSetConfiguration(lungCancerNumerator);
			
		DataSetConfiguration eczemaNumerator		
			= createPopulationHealthDataSet(
				RIFSchemaArea.HEALTH_NUMERATOR_DATA,
				ukGeography,
				cancerHealthTheme,
				ukDenominator,
				"eczema",
				"1.0",
				"Describes UK patients who have eczema",
				"C:\\rifDataLoaderTool\\eczema_data_set1");
		dataLoaderToolConfiguration.addNumeratorDataSetConfiguration(eczemaNumerator);


		
		
		ConfigurationHints configurationHints
			= createTestConfigurationHints();
		dataLoaderToolConfiguration.setConfigurationHints(configurationHints);
		
		return dataLoaderToolConfiguration;
	}
	
	
	public GeographyMetaData createTestGeographyMetaData() {
		GeographyMetaData geographyMetaData
			= GeographyMetaData.newInstance();
		
		StringBuilder filePath = new StringBuilder();
		filePath.append("C:");
		filePath.append(File.separator);
		filePath.append("rifDataLoaderTool");
		filePath.append(File.separator);
		filePath.append("ExampleRIFGeographyMetaData.xml");
		geographyMetaData.setFilePath(filePath.toString());
		
		Geography ukGeography = Geography.newInstance();
		ukGeography.setName("UK");
		ukGeography.setIdentifier("uk");
		ukGeography.addLevel(1, "Region", "region_level");
		ukGeography.addLevel(2, "District", "district_level");
		ukGeography.addLevel(3, "Ward", "ward_level");
		ukGeography.addLevel(4, "Output Area", "oa_level");
		ukGeography.addLevel(5, "Super Output Area", "soa_level");
			
		Geography usaGeography = Geography.newInstance();
		usaGeography.setName("USA");
		usaGeography.setIdentifier("usa");
		usaGeography.addLevel(1, "Region", "region_level");
		usaGeography.addLevel(2, "State", "state_level");
		usaGeography.addLevel(3, "County", "county_level");
		
		Geography canadaGeography = Geography.newInstance();
		canadaGeography.setName("Canada");
		canadaGeography.setIdentifier("ca");
		canadaGeography.addLevel(1, "Region", "region_level");
		canadaGeography.addLevel(2, "Province", "province_level");
		canadaGeography.addLevel(3, "District", "district_level");
		
		geographyMetaData.addGeography(ukGeography);
		geographyMetaData.addGeography(usaGeography);
		geographyMetaData.addGeography(canadaGeography);
		
		return geographyMetaData;
	}
	
	public DataSetConfiguration createCancerNumerator(
		final Geography geography,
		final HealthTheme healthTheme,
		final DataSetConfiguration denominator) {
		
		DataSetConfiguration result = DataSetConfiguration.newInstance();
		result.setGeography(geography);
		result.setHealthTheme(healthTheme);
		result.setDependencyDataSetConfiguration(denominator);
		
		
		
		return result;
	}
	
	public DataSetConfiguration createPopulationHealthDataSet(
		final RIFSchemaArea rifSchemaArea,
		final Geography geography,		
		final HealthTheme healthTheme,
		final DataSetConfiguration denominator,
		final String name,
		final String version,
		final String description,
		final String filePath) {

		DataSetConfiguration populationHealthDataSet
			= DataSetConfiguration.newInstance();
		populationHealthDataSet.setGeography(geography);
		populationHealthDataSet.setHealthTheme(healthTheme);
		populationHealthDataSet.setDependencyDataSetConfiguration(denominator);
		populationHealthDataSet.setRIFSchemaArea(rifSchemaArea);

		populationHealthDataSet.setName(name);
		populationHealthDataSet.setVersion(version);
		populationHealthDataSet.setFilePath(filePath);
		populationHealthDataSet.setFileHasFieldNamesDefined(true);
		populationHealthDataSet.setDescription(description);
				
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
		

		/*
		 * Note that in the following field declarations, we have
		 * preserved the same value for loadFieldName, cleanFieldName
		 * and convertFieldName.  In practice, these names may be differ
		 * from one another
		 */
		RIFDataTypeFactory rifDataTypeFactory
			= RIFDataTypeFactory.newInstance();
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();		
		
		ConversionFunctionFactory rifConversionFactory
			= ConversionFunctionFactory.newInstance();
		
		DataSetFieldConfiguration yearFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				name, 
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
		populationHealthDataSet.addFieldConfiguration(yearFieldConfiguration);
	

		DataSetFieldConfiguration ageFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				name, 
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
		populationHealthDataSet.addFieldConfiguration(ageFieldConfiguration);

		DataSetFieldConfiguration sexFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				name, 
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
		populationHealthDataSet.addFieldConfiguration(sexFieldConfiguration);


		/*
		 * The following field definitions describe geographical resolutions
		 * "level1" is the broadest level and may refer to something like state or
		 * region.  "level4" could refer to something like the UK's super-output area,
		 * which describes a well defined administrative area that may include just
		 * a few streets.
		 */

		
		DataSetFieldConfiguration regionFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				name, 
				"Region");
		regionFieldConfiguration.setCoreFieldDescription(
			"UK region");		
		regionFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		regionFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		regionFieldConfiguration.setEmptyValueAllowed(false);
		populationHealthDataSet.addFieldConfiguration(regionFieldConfiguration);
			
		DataSetFieldConfiguration districtFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				name, 
				"District");
		districtFieldConfiguration.setCoreFieldDescription(
			"districts in the UK");		
		districtFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		districtFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		districtFieldConfiguration.setEmptyValueAllowed(false);
		populationHealthDataSet.addFieldConfiguration(districtFieldConfiguration);
				
		DataSetFieldConfiguration wardFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				name, 
				"Ward");
		wardFieldConfiguration.setCoreFieldDescription(
			"describes ward administrative areas in the UK");		
		wardFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		wardFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		wardFieldConfiguration.setEmptyValueAllowed(false);
		populationHealthDataSet.addFieldConfiguration(wardFieldConfiguration);
				
		DataSetFieldConfiguration outputAreaFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				name, 
				"Output Area");
		outputAreaFieldConfiguration.setCoreFieldDescription(
			"output areas in the UK");		
		outputAreaFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		outputAreaFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		outputAreaFieldConfiguration.setEmptyValueAllowed(false);
		populationHealthDataSet.addFieldConfiguration(outputAreaFieldConfiguration);

		DataSetFieldConfiguration superOutputAreaFieldConfiguration
			= DataSetFieldConfiguration.newInstance(
				name, 
				"Super Output Area");
		superOutputAreaFieldConfiguration.setCoreFieldDescription(
			"output areas in the UK");		
		superOutputAreaFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);
		superOutputAreaFieldConfiguration.setFieldPurpose(FieldPurpose.GEOGRAPHICAL_RESOLUTION);
		superOutputAreaFieldConfiguration.setEmptyValueAllowed(false);
		populationHealthDataSet.addFieldConfiguration(superOutputAreaFieldConfiguration);		
		
		/*
		 * Now we'll add a health code field so that RIF scientists can specify
		 * records that correspond to some kind of health outcome of interest, typically
		 * from an ICD 10 code
		 * 
		 * In some data sets such as the UK's HES records, we would expect multiple columns
		 * named icd1, icd2, icd3,...icd20, each of which could contain a health code.  In
		 * this example, we only have one health code field
		 */
		if (rifSchemaArea == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {

			DataSetFieldConfiguration icdFieldConfiguration
				= DataSetFieldConfiguration.newInstance(
					name, 
					"icd");
			icdFieldConfiguration.setRIFDataType(
				RIFDataTypeFactory.RIF_ICD_DATA_TYPE);
			icdFieldConfiguration.setDuplicateIdentificationField(true);
			icdFieldConfiguration.setCoreFieldDescription("eg: super output area");				
			icdFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.EXTRA_FIELD);

			//If the data set is destined to be a numerator table, then it must have 
			//at least one health code
			icdFieldConfiguration.setFieldPurpose(FieldPurpose.HEALTH_CODE);
			icdFieldConfiguration.setEmptyValueAllowed(true);
			populationHealthDataSet.addFieldConfiguration(icdFieldConfiguration);

			/*
			 * We'll also include two fields that appear in the CSV file but ones which
			 * the RIF will not bother to import. Note that we must have a field configuration
			 * defined for each field we expect to encounter in the CSV file, whether the
			 * RIF manager is interested in it or not. 
			 */		

			DataSetFieldConfiguration healthProviderFieldConfiguration
				= DataSetFieldConfiguration.newInstance(
					name, 
					"health_provider_code");
			healthProviderFieldConfiguration.setRIFDataType(
				RIFDataTypeFactory.RIF_ICD_DATA_TYPE);
			healthProviderFieldConfiguration.setDuplicateIdentificationField(true);
			healthProviderFieldConfiguration.setCoreFieldDescription("code for hospital where visit was recorded");				

			//This is a flag that tells the data loader tool to not bother promoting the
			//field through any workflow state beyond Load
			healthProviderFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.IGNORE_FIELD);

			//By default, the purpose of this field will be "Other"
			//healthProviderFieldConfiguration.setFieldPurpose(FieldPurpose.OTHER);
		
			//By default, the field will allow empty values
			//healthProviderFieldConfiguration.setEmptyValueAllowed(true);
			populationHealthDataSet.addFieldConfiguration(healthProviderFieldConfiguration);

			DataSetFieldConfiguration treatmentDurationFieldConfiguration
				= DataSetFieldConfiguration.newInstance(
					name, 
					"treatment_duration");
			treatmentDurationFieldConfiguration.setDuplicateIdentificationField(true);
			//This is a flag that tells the data loader tool to not bother promoting the
			//field through any workflow state beyond Load
			treatmentDurationFieldConfiguration.setFieldRequirementLevel(FieldRequirementLevel.IGNORE_FIELD);
			populationHealthDataSet.addFieldConfiguration(treatmentDurationFieldConfiguration);	
		}
		
		return populationHealthDataSet;		
	}
	
	public ConfigurationHints createTestConfigurationHints() {
		ConfigurationHints configurationHints
			= new ConfigurationHints();
		
		return configurationHints;		
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


