package org.sahsu.rif.services.test.businessConceptLayer.ms;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.AdjustableCovariate;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.CovariateType;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.MapArea;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.datastorage.common.SampleTestObjectGenerator;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.test.AbstractRIFTestCase;

import static org.junit.Assert.fail;

public final class TestDiseaseMappingStudy
		extends AbstractRIFTestCase {

	/** The master disease mapping study. */
	private SampleTestObjectGenerator generator;
	private DiseaseMappingStudy masterDiseaseMappingStudy;

	private AdjustableCovariate masterNearDistCovariate;
	private AdjustableCovariate masterSESCovariate;
	private AdjustableCovariate masterTri1KMCovariate;
	
	/**
	 * Instantiates a new test disease mapping study.
	 */
	public TestDiseaseMappingStudy() {
		generator
			= new SampleTestObjectGenerator();
		masterDiseaseMappingStudy 
			= generator.createSampleDiseaseMappingStudy();

		CovariateType covariateType = CovariateType.CONTINUOUS_VARIABLE;
		masterNearDistCovariate
			= AdjustableCovariate.newInstance(
				"NEAR_DIST", 
				String.valueOf("358.28"), 
				String.valueOf("78786.62"), 
				covariateType);	
		masterSESCovariate
			= AdjustableCovariate.newInstance(
				"SES", 
				String.valueOf("1"), 
				String.valueOf("5"), 
				covariateType);	
		masterTri1KMCovariate
			= AdjustableCovariate.newInstance(
				"TRI_1KM", 
				String.valueOf("0"), 
				String.valueOf("1"), 
				covariateType);			
		
	}

	/**
	 * Accept a valid disease mapping study with typical fields
	 */
	@Test
	public void acceptValidInstance_COMMON1() {
		try {
			AbstractStudy diseaseMappingStudy =
					AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.checkErrors(getValidationPolicy());

		} catch(RIFServiceException e) {

			fail(e.toString());
		}
	}
	
	/**
	 * A disease mapping study is invalid if it has a blank name
	 */
	@Test
	public void rejectBlankRequiredFields_ERROR() {
		
		//name is blank
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.setName(null);
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_DISEASE_MAPPING_STUDY,
					1);
		}
		
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.setName("");
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, 
				1);			
		}
		
		//description is blank
		
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.setDescription(null);
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, 
				1);			
		}
		
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.setDescription("");
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, 
				1);			
		}

		//comparison area is blank
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.setComparisonArea(null);
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, 
				1);			
		}		
		
		
	}
	
	@Test
	public void rejectMultipleFieldErrors_ERROR() {
		
		
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.setName("");
			diseaseMappingStudy.setDescription(null);
			diseaseMappingStudy.setGeography(null);
			diseaseMappingStudy.setComparisonArea(null);
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, 
				4);			
		}
	}
	
	/**
	 * Reject empty investigation list.
	 */
	@Test
	/**
	 * A disease mapping study is invalid if no investigations are specified
	 */
	public void rejectEmptyInvestigationList_ERROR() {
		
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.setInvestigations(null);
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, 
				1);			
		}		
		
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.clearInvestigations();
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, 
				1);			
		}
	}
	
	/**
	 * Reject invalid comparison area.
	 */
	@Test
	/**
	 * A disease mapping study is invalid if its comparison area is invalid
	 */
	public void rejectInvalidComparisonArea_ERROR() {
		//Intentionally creating a comparison area that has an invalid
		//GeoLevelArea.  This should be picked up when disease mapping study
		//validates
		GeoLevelSelect geoLevelSelect = GeoLevelSelect.newInstance("LEVEL2");
		
		//injecting error
		GeoLevelArea invalidGeoLevelArea = GeoLevelArea.newInstance(null);

		GeoLevelView geoLevelView = GeoLevelView.newInstance("LEVEL3");
		GeoLevelToMap geoLevelToMap = GeoLevelToMap.newInstance("LEVEL3");
		
		MapArea mapArea1 = MapArea.newInstance("111", "111", "Brent", 1);
		MapArea mapArea2 = MapArea.newInstance("222", "111", "Barnet", 1);
				
		ComparisonArea invalidComparisonArea
			= ComparisonArea.newInstance();
		invalidComparisonArea.addMapArea(mapArea1);
		invalidComparisonArea.addMapArea(mapArea2);
		invalidComparisonArea.setGeoLevelSelect(geoLevelSelect);
		invalidComparisonArea.setGeoLevelArea(invalidGeoLevelArea);
		invalidComparisonArea.setGeoLevelView(geoLevelView);
		invalidComparisonArea.setGeoLevelToMap(geoLevelToMap);

		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.setComparisonArea(invalidComparisonArea);
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY,
				1);
		}		
	}
	
	@Test
	/**
	 * A disease mapping study is invalid if it has an invalid investigation
	 */	
	public void rejectInvalidInvestigation_ERROR() {
		SampleTestObjectGenerator generator
			= new SampleTestObjectGenerator();
		Investigation invalidInvestigation
			= generator.createSampleInvestigation("This is an invalid investigation");
		HealthCode invalidHealthCode
			= HealthCode.newInstance("", null, "", true);
		invalidInvestigation.addHealthCode(invalidHealthCode);
		
		try {

			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.addInvestigation(invalidInvestigation);
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY,
				4);
		}		
	}

	
	@Test
	public void rejectInvestigationsWithDifferentDenominators_ERROR() {

		AbstractStudy diseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		diseaseMappingStudy.clearInvestigations();
		
		SampleTestObjectGenerator generator
			= new SampleTestObjectGenerator();
		Investigation investigation1
			= generator.createSampleInvestigation("Investigation 1");
		NumeratorDenominatorPair ndPair1
			= investigation1.getNdPair();
		ndPair1.setDenominatorTableName("denominator_table_name1");
		diseaseMappingStudy.addInvestigation(investigation1);
		
		Investigation investigation2
			= generator.createSampleInvestigation("Investigation 2");
		NumeratorDenominatorPair ndPair2
			= investigation2.getNdPair();
		ndPair2.setDenominatorTableName("denominator_table_name2");
		diseaseMappingStudy.addInvestigation(investigation2);

		Investigation investigation3
			= generator.createSampleInvestigation("Investigation 3");
		NumeratorDenominatorPair ndPair3
			= investigation3.getNdPair();
		ndPair3.setDenominatorTableName("denominator_table_name1");
		diseaseMappingStudy.addInvestigation(investigation3);

		try {
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY,
				1);			
		}
		
	}
	
	@Test
	/**
	 * 
	 */
	public void acceptInvestigationsWithIdenticalCovariates_COMMON() {
		
		/**
		 * Investigation 1 has {nearDist, ses}
		 * Investigation 2 has {tri1KM}
		 */
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.clearInvestigations();
			
			Investigation investigation1 
				= generator.createSampleInvestigation("investigation1");
			investigation1.clearCovariates();
			AdjustableCovariate nearDistCovariate1
				= AdjustableCovariate.createCopy(masterNearDistCovariate);			
			investigation1.addCovariate(nearDistCovariate1);
			AdjustableCovariate sesCovariate1
				= AdjustableCovariate.createCopy(masterSESCovariate);			
			investigation1.addCovariate(sesCovariate1);
			diseaseMappingStudy.addInvestigation(investigation1);
			
			Investigation investigation2 
				= generator.createSampleInvestigation("investigation2");
			investigation2.clearCovariates();
			AdjustableCovariate nearDistCovariate2
				= AdjustableCovariate.createCopy(masterNearDistCovariate);			
			investigation2.addCovariate(nearDistCovariate2);
			AdjustableCovariate sesCovariate2
				= AdjustableCovariate.createCopy(masterSESCovariate);			
			investigation2.addCovariate(sesCovariate2);
			diseaseMappingStudy.addInvestigation(investigation2);
			
			diseaseMappingStudy.checkErrors(getValidationPolicy());			
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void rejectInvestigationsWithDifferentCovariates_ERROR() {

		
		/**
		 * Investigation 1 has {nearDist, ses}
		 * Investigation 2 has {tri1KM}
		 */
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.clearInvestigations();
			
			Investigation investigation1 
				= generator.createSampleInvestigation("investigation1");
			investigation1.clearCovariates();
			AdjustableCovariate nearDistCovariate1
				= AdjustableCovariate.createCopy(masterNearDistCovariate);			
			investigation1.addCovariate(nearDistCovariate1);
			AdjustableCovariate sesCovariate1
				= AdjustableCovariate.createCopy(masterSESCovariate);			
			investigation1.addCovariate(sesCovariate1);
			diseaseMappingStudy.addInvestigation(investigation1);
			
			Investigation investigation2 
				= generator.createSampleInvestigation("investigation2");
			investigation2.clearCovariates();
			AdjustableCovariate tri1KMCovariate
				= AdjustableCovariate.createCopy(masterTri1KMCovariate);
			investigation2.addCovariate(tri1KMCovariate);
			diseaseMappingStudy.addInvestigation(investigation2);
			
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY,
				1);			
		}

		/**
		 * Investigation 1 has {nearDist}
		 * Investigation 2 has {nearDist, ses}
		 */
		try {
			AbstractStudy diseaseMappingStudy
				= AbstractStudy.createCopy(masterDiseaseMappingStudy);
			diseaseMappingStudy.clearInvestigations();
			
			Investigation investigation1 
				= generator.createSampleInvestigation("investigation1");
			investigation1.clearCovariates();
			AdjustableCovariate nearDistCovariate1
				= AdjustableCovariate.createCopy(masterNearDistCovariate);			
			investigation1.addCovariate(nearDistCovariate1);
			diseaseMappingStudy.addInvestigation(investigation1);
			
			Investigation investigation2 
				= generator.createSampleInvestigation("investigation2");
			investigation2.clearCovariates();
			AdjustableCovariate nearDistCovariate2
				= AdjustableCovariate.createCopy(masterNearDistCovariate);
			AdjustableCovariate sesCovariate2
				= AdjustableCovariate.createCopy(masterSESCovariate);			
			investigation2.addCovariate(nearDistCovariate2);
			investigation2.addCovariate(sesCovariate2);		
			diseaseMappingStudy.addInvestigation(investigation2);
			
			diseaseMappingStudy.checkErrors(getValidationPolicy());
			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_DISEASE_MAPPING_STUDY,
				1);			
		}
	}

	/**
	 * Test security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		AbstractStudy maliciousDiseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		maliciousDiseaseMappingStudy.setIdentifier(getTestMaliciousValue());
		try {
			maliciousDiseaseMappingStudy.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousDiseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		maliciousDiseaseMappingStudy.setName(getTestMaliciousValue());
		try {
			maliciousDiseaseMappingStudy.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousDiseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		maliciousDiseaseMappingStudy.setDescription(getTestMaliciousValue());
		try {
			maliciousDiseaseMappingStudy.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousDiseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		maliciousDiseaseMappingStudy.setOtherNotes(getTestMaliciousValue());
		try {
			maliciousDiseaseMappingStudy.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousDiseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		Geography maliciousGeography
			= Geography.newInstance(getTestMaliciousValue(), "not much description");
		maliciousDiseaseMappingStudy.setGeography(maliciousGeography);		
		try {
			maliciousDiseaseMappingStudy.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousDiseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		ComparisonArea maliciousComparisonArea
			= maliciousDiseaseMappingStudy.getComparisonArea();
		maliciousComparisonArea.setIdentifier(getTestMaliciousValue());
		try {
			maliciousDiseaseMappingStudy.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousDiseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		AbstractStudyArea diseaseMappingStudyArea
			= maliciousDiseaseMappingStudy.getStudyArea();
		MapArea maliciousMapArea = MapArea.newInstance("454", "454", getTestMaliciousValue(), 1);
		diseaseMappingStudyArea.addMapArea(maliciousMapArea);
		try {
			maliciousDiseaseMappingStudy.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousDiseaseMappingStudy
			= AbstractStudy.createCopy(masterDiseaseMappingStudy);
		Investigation maliciousInvestigation
			= Investigation.newInstance();
		maliciousInvestigation.setTitle(getTestMaliciousValue());
		maliciousDiseaseMappingStudy.addInvestigation(maliciousInvestigation);
		try {
			maliciousDiseaseMappingStudy.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}		
	}
}
