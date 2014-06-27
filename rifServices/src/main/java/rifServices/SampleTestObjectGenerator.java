package rifServices;

import rifServices.businessConceptLayer.*;

import java.util.ArrayList;
import java.util.Date;

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
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public class SampleTestObjectGenerator {

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
	public RIFJobSubmission createSampleRIFJobSubmission() {
		RIFJobSubmission rifJobSubmission = RIFJobSubmission.newInstance();
		rifJobSubmission.setNewRecord(false);

		User user = User.newInstance("keving", "11.111.11.228");
		rifJobSubmission.setUser(user);
		rifJobSubmission.setJobSubmissionTime(new Date());
		
		Project project = Project.newInstance();
		project.setName("SAHSU");
		project.setDescription("SAHSU Project used for all old projects on import. Disabled after import");
		rifJobSubmission.setProject(project);
		
		SampleTestObjectGenerator generator
			= new SampleTestObjectGenerator();
		DiseaseMappingStudy diseaseMappingStudy
			= generator.createSampleDiseaseMappingStudy();
		rifJobSubmission.setStudy(diseaseMappingStudy);

		CalculationMethod bymMethod = generator.createSampleBYMMethod();
		CalculationMethod hetMethod = generator.createSampleHETMethod();
		CalculationMethod carMethod = generator.createSampleCARMethod();
		rifJobSubmission.addCalculationMethod(bymMethod);
		rifJobSubmission.addCalculationMethod(hetMethod);
		rifJobSubmission.addCalculationMethod(carMethod);

		rifJobSubmission.addRIFOutputOption(RIFOutputOption.DATA);
		rifJobSubmission.addRIFOutputOption(RIFOutputOption.MAPS);
		rifJobSubmission.addRIFOutputOption(RIFOutputOption.RATIOS_AND_RATES);
		
		return rifJobSubmission;
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
		calculationMethods.add(createSampleBYMMethod());
		calculationMethods.add(createSampleHETMethod());
		
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
		calculationMethod.setDescription("Applies CAR-based bayesian smoothing");
		calculationMethod.setPrior(CalculationMethodPrior.STANDARD_DEVIATION);		
		Parameter c = Parameter.newInstance("a", "5");
		calculationMethod.addParameter(c);
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
		calculationMethod.setDescription("Besag, York and Mollie Model");
		calculationMethod.setPrior(CalculationMethodPrior.STANDARD_DEVIATION);		
		Parameter c = Parameter.newInstance("c", "10");
		calculationMethod.addParameter(c);
		return calculationMethod;		
	}

	
	/**
	 * Creates the sample calculation method.
	 *
	 * @param methodName the method name
	 * @return the calculation method
	 */
	public CalculationMethod createSampleCalculationMethod(String methodName) {
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
		calculationMethod.setDescription("Does HET things");
		calculationMethod.setPrior(CalculationMethodPrior.STANDARD_DEVIATION);		
		Parameter a = Parameter.newInstance("a", "10");
		Parameter b = Parameter.newInstance("b", "5");

		calculationMethod.addParameter(a);
		calculationMethod.addParameter(b);

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
		diseaseMappingStudy.setName("public health study");
		diseaseMappingStudy.setDescription("Examining air pollution");
		
		Geography geography = Geography.newInstance("SAHSU", "xxx");
		diseaseMappingStudy.setGeography(geography);
		
		GeoLevelSelect geoLevelSelect = GeoLevelSelect.newInstance("LEVEL2");
		geoLevelSelect.setNewRecord(false);
		GeoLevelArea geoLevelArea = GeoLevelArea.newInstance("Clarke");
		geoLevelArea.setNewRecord(false);
		GeoLevelView geoLevelView = GeoLevelView.newInstance("LEVEL4");
		geoLevelView.setNewRecord(false);
		GeoLevelToMap geoLevelToMap = GeoLevelToMap.newInstance("LEVEL4");
		geoLevelToMap.setNewRecord(false);
	
		MapArea mapArea1 = MapArea.newInstance("01.011.012600.1", "Clarke LEVEL4(01.011.012600.1)");
		mapArea1.setNewRecord(false);
		MapArea mapArea2 = MapArea.newInstance("01.011.012600.2", "Clarke LEVEL4(01.011.012600.2)");
		mapArea2.setNewRecord(false);
			
		ComparisonArea comparisonArea
			= ComparisonArea.newInstance();
		comparisonArea.setNewRecord(false);		
		comparisonArea.addMapArea(mapArea1);
		comparisonArea.addMapArea(mapArea2);
		comparisonArea.setGeoLevelSelect(geoLevelSelect);
		comparisonArea.setGeoLevelArea(geoLevelArea);
		comparisonArea.setGeoLevelView(geoLevelView);
		comparisonArea.setGeoLevelToMap(geoLevelToMap);
		diseaseMappingStudy.setComparisonArea(comparisonArea);
	
		DiseaseMappingStudyArea diseaseMappingStudyArea
			= DiseaseMappingStudyArea.newInstance();
		diseaseMappingStudyArea.setNewRecord(false);		
		diseaseMappingStudyArea.addMapArea(mapArea1);
		diseaseMappingStudyArea.addMapArea(mapArea2);
		diseaseMappingStudyArea.setGeoLevelSelect(geoLevelSelect);
		diseaseMappingStudyArea.setGeoLevelArea(geoLevelArea);
		diseaseMappingStudyArea.setGeoLevelView(geoLevelView);
		diseaseMappingStudyArea.setGeoLevelToMap(geoLevelToMap);		
		diseaseMappingStudy.setDiseaseMappingStudyArea(diseaseMappingStudyArea);
	
		SampleTestObjectGenerator generator
			= new SampleTestObjectGenerator();
		//Investigation sampleInvestigation1
		//	= generator.createSampleInvestigation("Lung cancer study");
		Investigation sampleInvestigation1
			= generator.createSampleInvestigation("CANCERSTUDY1");
		sampleInvestigation1.setNewRecord(false);		
		diseaseMappingStudy.addInvestigation(sampleInvestigation1);
		Investigation sampleInvestigation2
			= generator.createSampleInvestigation("BRAINCANCERSTUDY");
		sampleInvestigation2.setNewRecord(false);		
		diseaseMappingStudy.addInvestigation(sampleInvestigation2);	
		
		return diseaseMappingStudy;
	}
	
	/**
	 * Creates the sample investigation.
	 *
	 * @param investigationTitle the investigation title
	 * @return the investigation
	 */
	public Investigation createSampleInvestigation(String investigationTitle) {
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
				"J11", 
				"icd10",
				"Influenza, virus not identified", 
				false);
		healthCode1.setNewRecord(false);
		investigation.addHealthCode(healthCode1);

		HealthCode healthCode2
			= HealthCode.newInstance(
				"I21", 
				"icd10",
				"Acute myocardial", 
				false);
						
		healthCode2.setNewRecord(false);
		investigation.addHealthCode(healthCode2);
		
		AgeGroup lowerLimitAgeGroup = AgeGroup.newInstance("23", "9", "12", "[9-12]");
		AgeGroup upperLimitAgeGroup = AgeGroup.newInstance("24", "17", "20", "[17-20]");

		AgeBand ageBand1 = AgeBand.newInstance(lowerLimitAgeGroup, upperLimitAgeGroup);
		ageBand1.setNewRecord(false);
		
		investigation.addAgeBand(ageBand1);		
		investigation.setSex(Sex.BOTH);
		YearRange yearRange = YearRange.newInstance("1992", "1997");
		yearRange.setNewRecord(false);
		investigation.setYearRange(yearRange);
		investigation.setInterval("2");;
		investigation.addYearInterval(YearInterval.newInstance("1992", "1993"));
		investigation.addYearInterval(YearInterval.newInstance("1994", "1995"));
		investigation.addYearInterval(YearInterval.newInstance("1996", "1997"));
		
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
		AdjustableCovariate adjustableCovariate2
			= AdjustableCovariate.newInstance(
				"ETHNICITY", 
				"1", 
				"3",
				CovariateType.NTILE_INTEGER_SCORE);
		adjustableCovariate2.setNewRecord(false);
		investigation.addCovariate(adjustableCovariate2);
		
		return investigation;
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
