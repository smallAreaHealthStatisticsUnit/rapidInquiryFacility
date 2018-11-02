package org.sahsu.rif.services.test.services.ms;

import java.util.ArrayList;
import java.util.List;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.AgeGroupSortingOption;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthCodeTaxonomy;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.concepts.YearInterval;
import org.sahsu.rif.services.concepts.YearRange;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

public final class TestGUIScenario
		extends CommonRIFServiceTestCase {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		TestGUIScenario testScenario = new TestGUIScenario();
		testScenario.runScenario();
	}

	/**
	 * Run scenario.
	 */
	private void runScenario() {
		try {
			initialiseService(resources);
			rifServiceBundle.login("kgarwood", "kgarwood");			
			
			System.out.println("Logging on as kgarwood...");
			User testUser = User.newInstance("kgarwood", "11.111.11.228");
			
			System.out.println();
			System.out.println("Getting available geographies...");
			ArrayList<Geography> geographies
				= rifStudySubmissionService.getGeographies(testUser);
			for (Geography geography : geographies) {
				System.out.println("GEOGRAPHY:=="+geography.getDisplayName()+"==");
			}

			System.out.println();
			System.out.println("We'll pick the SAHSU geography and get available health themes");
			Geography sahsuGeography
				= Geography.newInstance("SAHSU", "stuff about sahsuland");
			ArrayList<HealthTheme> healthThemes
				= rifStudySubmissionService.getHealthThemes(testUser, sahsuGeography);
			for (HealthTheme healthTheme : healthThemes) {
				System.out.println("HEALTH THEME:=="+healthTheme.getDisplayName()+"==");			
			}
			
			System.out.println();
			System.out.println("We will pick the health theme of cancer and get ND pairs");
			HealthTheme cancerHealthTheme
				= HealthTheme.newInstance("SAHSU land cancer incidence example data", "");
			ArrayList<NumeratorDenominatorPair> cancerNDPairs
				= rifStudySubmissionService.getNumeratorDenominatorPairs(
					testUser, 
					sahsuGeography, 
					cancerHealthTheme);
			
			NumeratorDenominatorPair cancerNDPair
				= cancerNDPairs.get(0);
			
			System.out.println("NDPair numerator=="+cancerNDPair.getNumeratorTableName()+"==");
			System.out.println("NDPair denominator=="+cancerNDPair.getDenominatorTableName()+"==");
			System.out.println("ND_PAIR:=="+cancerNDPair.getDisplayName()+"==");
			System.out.println();
			System.out.println("Now let's focus on the levels of geographic resolution");
			System.out.println("GeoLevel select values:");
			ArrayList<GeoLevelSelect> geoLevelSelectValues
				= rifStudySubmissionService.getGeoLevelSelectValues(testUser, sahsuGeography);
			for (GeoLevelSelect geoLevelSelectValue : geoLevelSelectValues) {
				System.out.println("GEOLEVELSELECT:=="+geoLevelSelectValue.getDisplayName()+"==");
			}
			System.out.println();
			System.out.println("Obtain the default geo level select:");
			GeoLevelSelect defaultGeoLevelSelect
				= rifStudySubmissionService.getDefaultGeoLevelSelectValue(testUser, sahsuGeography);
			System.out.println("DEFAULT GEOLEVELSELECT:=="+defaultGeoLevelSelect.getDisplayName()+"==");
			
			System.out.println();
			System.out.println("Find geo level areas that match default geoselect");
			GeoLevelSelect validSAHSUGeoLevelSelectValue
				= GeoLevelSelect.newInstance("LEVEL2");
			ArrayList<GeoLevelArea> geoLevelAreas
				= rifStudySubmissionService.getGeoLevelAreaValues(
					testUser, 
					sahsuGeography, 
					validSAHSUGeoLevelSelectValue);
			for (GeoLevelArea geoLevelArea : geoLevelAreas) {
				System.out.println("GEOLEVELAREA:=="+geoLevelArea.getDisplayName()+"==");
			}
			
			System.out.println();
			System.out.println("Now get geo level view objects appropriate for geolevelSelect=="+validSAHSUGeoLevelSelectValue.getDisplayName()+"==");
			ArrayList<GeoLevelView> geoLevelViews
				= rifStudySubmissionService.getGeoLevelViewValues(testUser, sahsuGeography, validSAHSUGeoLevelSelectValue);
			for (GeoLevelView geoLevelView : geoLevelViews) {
				System.out.println("GEOLEVELVIEW:=="+geoLevelView.getDisplayName()+"==");
			}
			
			System.out.println();
			System.out.println("Get Age groups for the ND pair=="+cancerNDPair.getDisplayName()+"==");
			List<AgeGroup> ageGroups
				= rifStudySubmissionService.getAgeGroups(
					testUser,
					sahsuGeography,
					cancerNDPair,
					AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			for (AgeGroup ageGroup : ageGroups) {
				System.out.println("AGEGROUP:=="+ageGroup.getDisplayName()+"==");
			}
			
			System.out.println();
			System.out.println("Get Year limits that are appropriate for ND Pair=="+cancerNDPair.getDisplayName()+"==");
			YearRange yearRange
				= rifStudySubmissionService.getYearRange(testUser, sahsuGeography, cancerNDPair);
			System.out.println("GUI should show min year=="+yearRange.getLowerBound()+"==max year=="+yearRange.getUpperBound()+"==");

			System.out.println();
			System.out.println("Supposing user chooses an inteval of three year bands.  The year intervals become:");
			ArrayList<YearInterval> yearIntervals
				= yearRange.splitYearRange(3);
			for (YearInterval yearInterval : yearIntervals) {
				System.out.println("YEAR INTERVAL=="+yearInterval.getDisplayName()+"==");
			}
					
			System.out.println();
			System.out.println("Get sex values");
			ArrayList<Sex> sexs
				= rifStudySubmissionService.getSexes(testUser);
			for (Sex sex : sexs) {
				System.out.println("GENDER:=="+sex.getName()+"==");
			}
		
			System.out.println();
			System.out.println("Get covariates using a geoLevelToMap of 'LEVEL4'");
			GeoLevelToMap geoLevelToMap = GeoLevelToMap.newInstance("LEVEL4");
			ArrayList<AbstractCovariate> covariates
				= rifStudySubmissionService.getCovariates(
					testUser, 
					sahsuGeography, 
					geoLevelToMap);
			for (AbstractCovariate covariate : covariates) {
				System.out.println("COVARIATE:=="+covariate.getDisplayName()+"==");
			}
		}
		catch(RIFServiceException rifServiceException) {
			List<String> errorMessages = rifServiceException.getErrorMessages();
			for (String errorMessage : errorMessages) {
				System.out.println("ERROR:=="+errorMessage+"==");
			}
		}
	
	}
}
