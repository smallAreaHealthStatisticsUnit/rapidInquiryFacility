package org.sahsu.rif.services.datastorage.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.AdjustableCovariate;
import org.sahsu.rif.services.concepts.AgeBand;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.CalculationMethod;
import org.sahsu.rif.services.concepts.CalculationMethodPrior;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.CovariateType;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.MapArea;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Project;
import org.sahsu.rif.services.concepts.RIFServiceInformation;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.concepts.StudyType;
import org.sahsu.rif.services.concepts.YearInterval;
import org.sahsu.rif.services.concepts.YearRange;

/**
 *
 * Generates various examples of studies and parts of studies that are used
 * in testing and in demonstrations of the application.
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
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

public final class SampleTestObjectGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new sample test object generator.
	 */
	public SampleTestObjectGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Gets the sample rif job submission.
	 *
	 * @return the sample rif job submission
	 */
	

	public RIFStudySubmission createSampleRIFJobSubmission() {
		RIFStudySubmission rifStudySubmission =
				RIFStudySubmission.newInstance();
		rifStudySubmission.setNewRecord(false);

		rifStudySubmission.setJobSubmissionTime(new Date());
		
		Project project = Project.newInstance();
		project.setName("TEST");
		project.setDescription("Test Project. Will be disabled when in production.");
		rifStudySubmission.setProject(project);
		
		SampleTestObjectGenerator generator
			= new SampleTestObjectGenerator();
		DiseaseMappingStudy diseaseMappingStudy
			= generator.createSampleDiseaseMappingStudy();
		rifStudySubmission.setStudy(diseaseMappingStudy);

		CalculationMethod bymMethod = generator.createSampleBYMMethod();
		CalculationMethod hetMethod = generator.createSampleHETMethod();
		CalculationMethod carMethod = generator.createSampleCARMethod();
		rifStudySubmission.addCalculationMethod(bymMethod);
		rifStudySubmission.addCalculationMethod(hetMethod);
		rifStudySubmission.addCalculationMethod(carMethod);

		//rifStudySubmission.addRIFOutputOption(RIFOutputOption.DATA);
		//rifStudySubmission.addRIFOutputOption(RIFOutputOption.MAPS);
		//rifStudySubmission.addRIFOutputOption(RIFOutputOption.RATIOS_AND_RATES);
		
		return rifStudySubmission;
	}
	
	
	/**
	 * Gets the sample rif service information.
	 *
	 * @return the sample rif service information
	 */
	public RIFServiceInformation getSampleRIFServiceInformation() {
		RIFServiceInformation rifServiceInformation
			= RIFServiceInformation.newInstance();
		rifServiceInformation.setNewRecord(false);
		rifServiceInformation.setContactName("John Smith");
		rifServiceInformation.setContactEmail("jsmith@somewhere.com");
		rifServiceInformation.setOrganisation("Local Public Health Unit");
		rifServiceInformation.setServiceName("RIF service");
		rifServiceInformation.setVersionNumber(3.4);
		
		return rifServiceInformation;
	}
	
	/**
	 * Gets the sample calculation methods.
	 *
	 * @return the sample calculation methods
	 */
	public ArrayList<CalculationMethod> getSampleCalculationMethods() {
		ArrayList<CalculationMethod> calculationMethods
			= new ArrayList<CalculationMethod>();
		calculationMethods.add(createSampleCARMethod());
		calculationMethods.add(createSampleHETMethod());
		calculationMethods.add(createSampleBYMMethod());
		
		return calculationMethods;
	}
	
	/**
	 * Creates the sample car method.
	 *
	 * @return the calculation method
	 */
	public CalculationMethod createSampleCARMethod() {
		CalculationMethod calculationMethod
			= CalculationMethod.newInstance();
		calculationMethod.setNewRecord(false);
		calculationMethod.setName("CAR");
		calculationMethod.setCodeRoutineName("car_r_procedure");
		calculationMethod.setDescription("Conditional auto-regressive (CAR) model type");
		calculationMethod.setPrior(CalculationMethodPrior.STANDARD_DEVIATION);
		//Parameter c = Parameter.newInstance("a", "5");
		//calculationMethod.addParameter(c);
		return calculationMethod;		
	}
	
	/**
	 * Creates the sample bym method.
	 *
	 * @return the calculation method
	 */
	public CalculationMethod createSampleBYMMethod() {
		CalculationMethod calculationMethod
			= CalculationMethod.newInstance();
		calculationMethod.setNewRecord(false);
		calculationMethod.setName("BYM");
		calculationMethod.setCodeRoutineName("bym_r_procedure");
		calculationMethod.setDescription("Besag, York and Mollie (BYM) model type");
		calculationMethod.setPrior(CalculationMethodPrior.STANDARD_DEVIATION);		
	//	Parameter c = Parameter.newInstance("c", "10");
	//	calculationMethod.addParameter(c);
		return calculationMethod;		
	}

	
	/**
	 * Creates the sample calculation method.
	 *
	 * @param methodName the method name
	 * @return the calculation method
	 */
	public CalculationMethod createSampleCalculationMethod(final String methodName) {
		CalculationMethod calculationMethod
			= CalculationMethod.newInstance();
		calculationMethod.setNewRecord(false);
		calculationMethod.setName(methodName);
		calculationMethod.setCodeRoutineName("some_r_procedure");
		calculationMethod.setDescription("Transforms the data in some way");
		calculationMethod.setPrior(CalculationMethodPrior.STANDARD_DEVIATION);		
		Parameter a = Parameter.newInstance("a", "5");
		Parameter b = Parameter.newInstance("b", "5");

		calculationMethod.addParameter(a);
	calculationMethod.addParameter(b);

		return calculationMethod;		
	}		
	
	/**
	 * Creates the sample het method.
	 *
	 * @return the calculation method
	 */
	public CalculationMethod createSampleHETMethod() {
		CalculationMethod calculationMethod
			= CalculationMethod.newInstance();
		calculationMethod.setNewRecord(false);
		calculationMethod
			= CalculationMethod.newInstance();
		calculationMethod.setName("HET");
		calculationMethod.setCodeRoutineName("het_r_procedure");
		calculationMethod.setDescription("Heterogenous (HET) model type");
		calculationMethod.setPrior(CalculationMethodPrior.STANDARD_DEVIATION);		
		//Parameter a = Parameter.newInstance("a", "10");
		//Parameter b = Parameter.newInstance("b", "5");

		//calculationMethod.addParameter(a);
		//calculationMethod.addParameter(b);

		return calculationMethod;		
	}	
	
	/**
	 * Creates the sample disease mapping study.
	 *
	 * @return the disease mapping study
	 */
	public DiseaseMappingStudy createSampleDiseaseMappingStudy() {
		DiseaseMappingStudy diseaseMappingStudy
			= DiseaseMappingStudy.newInstance();
		diseaseMappingStudy.setNewRecord(false);
		diseaseMappingStudy.setName("public health study with results");
		diseaseMappingStudy.setDescription("Examining air pollution");
		
		Geography geography = Geography.newInstance("SAHSU", "xxx");
		diseaseMappingStudy.setGeography(geography);
		
		GeoLevelSelect geoLevelSelect = GeoLevelSelect.newInstance("LEVEL4");
		geoLevelSelect.setNewRecord(false);
		GeoLevelArea geoLevelArea = GeoLevelArea.newInstance("Elliot");
		geoLevelArea.setNewRecord(false);
		GeoLevelView geoLevelView = GeoLevelView.newInstance("LEVEL4");
		geoLevelView.setNewRecord(false);
		GeoLevelToMap geoLevelToMap = GeoLevelToMap.newInstance("LEVEL4");
		geoLevelToMap.setNewRecord(false);
	
		MapArea mapArea1 = MapArea.newInstance(
			"01.009.002900.3", 
			"01.009.002900.3", 
			"Elliot LEVEL4(01.009.002900.3)",
			1);
		mapArea1.setNewRecord(false);
		MapArea mapArea2 = MapArea.newInstance(
			"01.009.002800.5", 
			"01.009.002800.5",
			"Elliot LEVEL4(01.009.002800.5)",
			1);
		mapArea2.setNewRecord(false);

		MapArea mapArea3 = MapArea.newInstance(
			"01.009.002900.1", 
			"01.009.002900.1",
			"Elliot LEVEL4(01.009.002900.1)",
			1);
		mapArea3.setNewRecord(false);
		MapArea mapArea4 = MapArea.newInstance(
			"01.009.002900.2", 
			"01.009.002900.2",
			"Elliot LEVEL4(01.009.002900.2)",
			1);
		mapArea4.setNewRecord(false);
		MapArea mapArea5 = MapArea.newInstance(
			"01.009.002800.4", 
			"01.009.002800.4",
			"Elliot LEVEL4(01.009.002800.4)",
			1);
		mapArea5.setNewRecord(false);		
		
		ComparisonArea comparisonArea
			= ComparisonArea.newInstance();
		comparisonArea.setNewRecord(false);		
		comparisonArea.addMapArea(mapArea1);
		comparisonArea.addMapArea(mapArea2);
		comparisonArea.addMapArea(mapArea3);
		comparisonArea.addMapArea(mapArea4);
		comparisonArea.addMapArea(mapArea5);		
		
		
		comparisonArea.setGeoLevelSelect(geoLevelSelect);
		comparisonArea.setGeoLevelArea(geoLevelArea);
		comparisonArea.setGeoLevelView(geoLevelView);
		comparisonArea.setGeoLevelToMap(geoLevelToMap);
		diseaseMappingStudy.setComparisonArea(comparisonArea);
	
		AbstractStudyArea diseaseMappingStudyArea =
				AbstractStudyArea.newInstance(StudyType.DISEASE_MAPPING);
		diseaseMappingStudyArea.setNewRecord(false);		

		diseaseMappingStudyArea.addMapArea(mapArea1);
		diseaseMappingStudyArea.addMapArea(mapArea2);
		diseaseMappingStudyArea.addMapArea(mapArea3);
		diseaseMappingStudyArea.addMapArea(mapArea4);
		diseaseMappingStudyArea.addMapArea(mapArea5);		
		
		diseaseMappingStudyArea.setGeoLevelSelect(geoLevelSelect);
		diseaseMappingStudyArea.setGeoLevelArea(geoLevelArea);
		diseaseMappingStudyArea.setGeoLevelView(geoLevelView);
		diseaseMappingStudyArea.setGeoLevelToMap(geoLevelToMap);		
		diseaseMappingStudy.setStudyArea(diseaseMappingStudyArea);
	
		SampleTestObjectGenerator generator
			= new SampleTestObjectGenerator();
		//Investigation sampleInvestigation1
		//	= generator.createSampleInvestigation("Lung cancer study");
		Investigation sampleInvestigation1
			= generator.createSampleInvestigationHavingResults("CANCERSTUDY2");
		sampleInvestigation1.setNewRecord(false);
		diseaseMappingStudy.addInvestigation(sampleInvestigation1);	
		
		return diseaseMappingStudy;
	}
	

	/**
	 * Creates the sample investigation.
	 *
	 * @param investigationTitle the investigation title
	 * @return the investigation
	 */
	public Investigation createSampleInvestigation(final String investigationTitle) {
		Investigation investigation = Investigation.newInstance();
		investigation.setNewRecord(false);
		investigation.setTitle(investigationTitle);
		
		HealthTheme healthTheme
			= HealthTheme.newInstance(
				"SAHSULAND",  
				"SAHSU land cancer incidence example data");
		investigation.setHealthTheme(healthTheme);

		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.newInstance(
				"SAHSULAND_CANCER", 
				"Cancer cases in SAHSU land", 
				"SAHSULAND_POP", 
				"SAHSU land population");
		ndPair.setNewRecord(false);
		investigation.setNdPair(ndPair);		
		
		HealthCode healthCode1
			= HealthCode.newInstance(
				"C340", 
				"icd10",
				"malignant neoplasm of bronchus and lung", 
				false);
		healthCode1.setNewRecord(false);
		investigation.addHealthCode(healthCode1);

		/*
		HealthCode healthCode2
			= HealthCode.newInstance(
				"C81", 
				"icd10",
				"Hodgkin's disease", 
				false);

		
		healthCode2.setNewRecord(false);
		investigation.addHealthCode(healthCode2);
		*/		
		AgeGroup lowerLimitAgeGroup = AgeGroup.newInstance("1", "5", "9", "5_9");
		AgeGroup upperLimitAgeGroup = AgeGroup.newInstance("1", "15", "19", "15_19");

		AgeBand ageBand1 = AgeBand.newInstance(lowerLimitAgeGroup, upperLimitAgeGroup);
		ageBand1.setNewRecord(false);
		
		investigation.addAgeBand(ageBand1);		
		investigation.setSex(Sex.BOTH);
		YearRange yearRange = YearRange.newInstance("1995", "1996");
		yearRange.setNewRecord(false);
		investigation.setYearRange(yearRange);
		investigation.setInterval("1");;
		investigation.addYearInterval(YearInterval.newInstance("1995", "1995"));
		investigation.addYearInterval(YearInterval.newInstance("1996", "1996"));
		
		Geography sahsuGeography = Geography.newInstance("SAHSU", "Describes SAHSU Land");
		sahsuGeography.setNewRecord(false);

		AdjustableCovariate adjustableCovariate1
			= AdjustableCovariate.newInstance(
				"SES",
				"1",
				"5",
				CovariateType.NTILE_INTEGER_SCORE);
		adjustableCovariate1.setNewRecord(false);

		investigation.addCovariate(adjustableCovariate1);
		/*		
		AdjustableCovariate adjustableCovariate2
			= AdjustableCovariate.newInstance(
				"ETHNICITY", 
				"1", 
				"3",
				CovariateType.NTILE_INTEGER_SCORE);
		adjustableCovariate2.setNewRecord(false);
		investigation.addCovariate(adjustableCovariate2);
		 */
		
		return investigation;
	}

	/**
	 * Creates the sample investigation.
	 *
	 * @param investigationTitle the investigation title
	 * @return the investigation
	 */
	public Investigation createSampleInvestigationHavingResults(final String investigationTitle) {
		Investigation investigation = Investigation.newInstance();
		investigation.setNewRecord(false);
		investigation.setTitle(investigationTitle);
		
		HealthTheme healthTheme
			= HealthTheme.newInstance(
				"SAHSULAND",  
				"SAHSU land cancer incidence example data");
		investigation.setHealthTheme(healthTheme);

		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.newInstance(
				"SAHSULAND_CANCER", 
				"Cancer cases in SAHSU land", 
				"SAHSULAND_POP", 
				"SAHSU land population");
		ndPair.setNewRecord(false);
		investigation.setNdPair(ndPair);		
		
		HealthCode healthCode1 
			= HealthCode.newInstance(
				"1749", 
				"icd9",
				"Mixed cellularity", 
				false);
		healthCode1.setNewRecord(false);
		HealthCode healthCode2 
			= HealthCode.newInstance(
				"1889", 
				"icd9",
				"Hodgkin's paragranuloma", 
				false);
		healthCode2.setNewRecord(false);
				
		investigation.addHealthCode(healthCode1);
		investigation.addHealthCode(healthCode2);
		
		
		AgeGroup lowerLimitAgeGroup = AgeGroup.newInstance("1", "10", "14", "10_14");
		AgeGroup upperLimitAgeGroup = AgeGroup.newInstance("1", "50", "54", "50_54");

		AgeBand ageBand1 = AgeBand.newInstance(lowerLimitAgeGroup, upperLimitAgeGroup);
		ageBand1.setNewRecord(false);
		
		investigation.addAgeBand(ageBand1);		
		investigation.setSex(Sex.FEMALES);
		YearRange yearRange = YearRange.newInstance("1990", "1991");
		yearRange.setNewRecord(false);
		investigation.setYearRange(yearRange);
		investigation.setInterval("1");
		investigation.addYearInterval(YearInterval.newInstance("1990", "1991"));
		
		Geography sahsuGeography = Geography.newInstance("SAHSU", "Describes SAHSU Land");
		sahsuGeography.setNewRecord(false);

		AdjustableCovariate adjustableCovariate1
			= AdjustableCovariate.newInstance(
				"SES", 
				"1", 
				"5",
				CovariateType.NTILE_INTEGER_SCORE);
		adjustableCovariate1.setNewRecord(false);

		investigation.addCovariate(adjustableCovariate1);
		/*		
		AdjustableCovariate adjustableCovariate2
			= AdjustableCovariate.newInstance(
				"ETHNICITY", 
				"1", 
				"3",
				CovariateType.NTILE_INTEGER_SCORE);
		adjustableCovariate2.setNewRecord(false);
		investigation.addCovariate(adjustableCovariate2);
		 */
		
		return investigation;
	}
	
	
	public RIFStudySubmission createTypicalStudySubmission() {

		RIFStudySubmission rifStudySubmission =
				RIFStudySubmission.newInstance();
		rifStudySubmission.setNewRecord(false);

		rifStudySubmission.setJobSubmissionTime(new Date());
		
		Project project = Project.newInstance();
		project.setName("TEST");
		project.setDescription("Test Project. Will be disabled when in production.");		
		rifStudySubmission.setProject(project);
		
	
		DiseaseMappingStudy diseaseMappingStudy
			= DiseaseMappingStudy.newInstance();
		diseaseMappingStudy.setNewRecord(false);
		diseaseMappingStudy.setName("public health study with results");
		diseaseMappingStudy.setDescription("Examining air pollution");
		Geography geography 
			= Geography.newInstance(
				"SAHSU", 
				"SAHSU example database");
		diseaseMappingStudy.setGeography(geography);
	

		//Set the study area
		AbstractStudyArea diseaseMappingStudyArea =
				AbstractStudyArea.newInstance(StudyType.DISEASE_MAPPING);
		diseaseMappingStudyArea.setNewRecord(false);		
		GeoLevelSelect studyAreaGeoLevelSelect = GeoLevelSelect.newInstance("LEVEL2");
		studyAreaGeoLevelSelect.setNewRecord(false);
		GeoLevelArea studyAreaGeoLevelArea = GeoLevelArea.newInstance("Abellan");
		studyAreaGeoLevelArea.setNewRecord(false);
		GeoLevelView studyAreaGeoLevelView = GeoLevelView.newInstance("LEVEL4");
		studyAreaGeoLevelView.setNewRecord(false);
		GeoLevelToMap studyAreaGeoLevelToMap = GeoLevelToMap.newInstance("LEVEL4");
		studyAreaGeoLevelToMap.setNewRecord(false);		
		diseaseMappingStudyArea.setGeoLevelSelect(studyAreaGeoLevelSelect);
		diseaseMappingStudyArea.setGeoLevelArea(studyAreaGeoLevelArea);
		diseaseMappingStudyArea.setGeoLevelView(studyAreaGeoLevelView);
		diseaseMappingStudyArea.setGeoLevelToMap(studyAreaGeoLevelToMap);		
		diseaseMappingStudy.setStudyArea(diseaseMappingStudyArea);

		//Where to find out what levels the study area has?
		MapArea mapArea1 = MapArea.newInstance(
			"01.001.000100.1", 
			"01.001.000100.1", 
			"Abellan LEVEL4(01.001.000100.1)",
			1);
		mapArea1.setNewRecord(false);
					
		MapArea mapArea2 = MapArea.newInstance(
			"01.001.000100.2", 
			"01.001.000100.2", 
			"Abellan LEVEL4(01.001.000100.2)",
			1);
		mapArea2.setNewRecord(false);
					
		MapArea mapArea3 = MapArea.newInstance(
			"01.001.000200.1", 
			"01.001.000200.1", 
			"Abellan LEVEL4(01.001.000200.1)",
			1);
		mapArea3.setNewRecord(false);
			
		MapArea mapArea4 = MapArea.newInstance(
			"01.001.000300.1", 
			"01.001.000300.1", 
			"Abellan LEVEL4(01.001.000300.1)",
			1);
		mapArea4.setNewRecord(false);
							
		diseaseMappingStudyArea.addMapArea(mapArea1);
		diseaseMappingStudyArea.addMapArea(mapArea2);
		diseaseMappingStudyArea.addMapArea(mapArea3);
		diseaseMappingStudyArea.addMapArea(mapArea4);
	
		//Set the 
		ComparisonArea comparisonArea
			= ComparisonArea.newInstance();
		MapArea mapArea5 = MapArea.newInstance(
			"01.002", 
			"01.002", 
			"Abellan LEVEL2(01.002)",
			0 /* Dummy */);
		mapArea5.setNewRecord(false);
		comparisonArea.addMapArea(mapArea5);
		
		//default comparison area LEVEL2 default study area LEVEL4
		GeoLevelSelect comparisonAreaGeoLevelSelect = GeoLevelSelect.newInstance("LEVEL1");
		comparisonAreaGeoLevelSelect.setNewRecord(false);
		GeoLevelArea comparisonAreaGeoLevelArea = GeoLevelArea.newInstance("Abellan");
		comparisonAreaGeoLevelArea.setNewRecord(false);
		studyAreaGeoLevelView.setNewRecord(false);
		GeoLevelToMap comparisonAreaGeoLevelToMap = GeoLevelToMap.newInstance("LEVEL2");
		comparisonAreaGeoLevelToMap.setNewRecord(false);		

		comparisonArea.setGeoLevelSelect(comparisonAreaGeoLevelSelect);
		comparisonArea.setGeoLevelArea(comparisonAreaGeoLevelArea);
		comparisonArea.setGeoLevelView(studyAreaGeoLevelView);
		comparisonArea.setGeoLevelToMap(comparisonAreaGeoLevelToMap);		
		diseaseMappingStudy.setComparisonArea(comparisonArea);
			
		//study level geolevel is LEVEL4
		
		//sahsuland_pop (denominator table)
		
		Investigation investigation = Investigation.newInstance();
		investigation.setTitle("INV1");
		investigation.setDescription("Lung cancer");
		
		HealthTheme healthTheme
			= HealthTheme.newInstance(
				"SAHSULAND",
				"SAHSU land cancer incidence example data");
		investigation.setHealthTheme(healthTheme);
		
		NumeratorDenominatorPair ndPair
		= NumeratorDenominatorPair.newInstance(
			"SAHSULAND_CANCER",
			"Cancer cases in SAHSU land", 
			"SAHSULAND_POP",
			"SAHSU land population");
		investigation.setNdPair(ndPair);
		
		//Set age groups, year range and sex
		AgeGroup lowerLimitAgeGroup = AgeGroup.newInstance("0", "0", "0", "0");
		AgeGroup upperLimitAgeGroup = AgeGroup.newInstance("21", "85", "255", "85PLUS");
		AgeBand ageBand1 = AgeBand.newInstance(lowerLimitAgeGroup, upperLimitAgeGroup);
		ageBand1.setNewRecord(false);

		YearRange yearRange = YearRange.newInstance("1989", "1996");
		yearRange.setNewRecord(false);
		investigation.setYearRange(yearRange);
		investigation.setInterval("1");;
		investigation.addYearInterval(YearInterval.newInstance("1989", "1996"));				
		investigation.addAgeBand(ageBand1);		
		investigation.setSex(Sex.BOTH);

		//Add health codes.  ICD10: C34 and ICD9: codes from 162.0 to 162.9
		HealthCode icd10C34 
			= HealthCode.newInstance(
				"C34", 
				"icd10", 
				"Malignant neoplasm of bronchus and lung", 
				false);
		investigation.addHealthCode(icd10C34);

/*		
		HealthCode icd91620
			= HealthCode.newInstance(
				"1620", 
				"icd9", 
				"Malignant neo trachea", 
				false);
		investigation.addHealthCode(icd91620);				
		HealthCode icd91622
			= HealthCode.newInstance(
				"1622", 
				"icd9", 
				"Malig neo main bronchus", 
				false);
		investigation.addHealthCode(icd91622);		
		HealthCode icd91623
			= HealthCode.newInstance(
				"1623", 
				"icd9", 
				"Mal neo upper lobe lung", 
				false);		
		investigation.addHealthCode(icd91623);				
		HealthCode icd91624
			= HealthCode.newInstance(
				"1624", 
				"icd9", 
				"Mal neo middle lobe lung", 
				false);		
		investigation.addHealthCode(icd91624);
		HealthCode icd91625
			= HealthCode.newInstance(
				"1625", 
				"icd9", 
				"Mal neo lower lobe lung", 
				false);		
		investigation.addHealthCode(icd91625);
		HealthCode icd91628
			= HealthCode.newInstance(
				"1625", 
				"icd9", 
				"Mal neo bronch/lung NEC", 
				false);		
		investigation.addHealthCode(icd91628);		
		HealthCode icd91629
			= HealthCode.newInstance(
				"1629", 
				"icd9", 
				"Mal neo bronch/lung NOS", 
				false);		
		investigation.addHealthCode(icd91629);				
*/	
		
		//SES LEVEL4 SAHSU min 1.0 max 5.0
		AdjustableCovariate adjustableCovariate1
			= AdjustableCovariate.newInstance(
				"SES", 
				"1", 
				"5",
				CovariateType.NTILE_INTEGER_SCORE);
		adjustableCovariate1.setNewRecord(false);
		investigation.addCovariate(adjustableCovariate1);
		diseaseMappingStudy.addInvestigation(investigation);		
				
		rifStudySubmission.setStudy(diseaseMappingStudy);

		CalculationMethod bymMethod = createSampleBYMMethod();
		rifStudySubmission.addCalculationMethod(bymMethod);
		
		
		ComparisonArea compArea
			= diseaseMappingStudy.getComparisonArea();
		System.out.println("Number of areas=="+compArea.getMapAreas().size()+"==");
		
		
		return rifStudySubmission;
	}
	
	
	
	
	public File generateSampleOutputFile() {
		StringBuilder fileName = new StringBuilder();
		fileName.append(".");
		fileName.append(File.separator);
		fileName.append("SampleOutputFile.txt");
		File sampleOutputFile = new File(fileName.toString());
		return sampleOutputFile;
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
