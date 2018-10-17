package org.sahsu.rif.services.test.services;

import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelAttributeSource;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.MapArea;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Project;
import org.sahsu.rif.services.concepts.StudyResultRetrievalContext;
import org.sahsu.rif.services.concepts.StudySummary;
import org.sahsu.rif.services.test.AbstractRIFTestCase;

public class CommonRIFServiceTestCase extends AbstractRIFTestCase {

	/** The invalid user. */
	private User emptyUser;

	/** The non existent user. */
	private User nonExistentUser;
	
	/** The malicious user. */
	private User maliciousUser;
	
	/** The sahsu geography. */
	private Geography validGeography;
	
	/** The invalid geography. */
	private Geography emptyGeography;

	/** The non existent geography. */
	private Geography nonExistentGeography;
	
	/** The malicious geography. */
	private Geography maliciousGeography;	
	
	/** The valid sahsu geo level select value. */
	private GeoLevelSelect validGeoLevelSelectValue;
	/** The invalid geo level select value. */
	private GeoLevelSelect emptyGeoLevelSelectValue;

	/** The non existent geo level select value. */
	private GeoLevelSelect nonExistentGeoLevelSelectValue;	
	private GeoLevelSelect maliciousGeoLevelSelectValue;	

	private GeoLevelArea validGeoLevelAreaValue;
	private GeoLevelArea emptyGeoLevelAreaValue;	
	private GeoLevelArea nonExistentGeoLevelAreaValue;
	private GeoLevelArea maliciousGeoLevelAreaValue;
		
	private GeoLevelToMap validGeoLevelToMapValue;
	private GeoLevelToMap emptyGeoLevelToMapValue;
	private GeoLevelToMap nonExistentGeoLevelToMapValue;
	private GeoLevelToMap maliciousGeoLevelToMapValue;
		
	private GeoLevelView validGeoLevelViewValue;
	private GeoLevelView emptyGeoLevelViewValue;
	private GeoLevelView nonExistentGeoLevelViewValue;
	private GeoLevelView maliciousGeoLevelViewValue;
		
	private NumeratorDenominatorPair validNDPair;
	private NumeratorDenominatorPair emptyNDPair;
	private NumeratorDenominatorPair nonExistentNDPair;
	private NumeratorDenominatorPair maliciousNDPair;	
	
	private HealthTheme validHealthTheme;
	private HealthTheme emptyHealthTheme;
	private HealthTheme nonExistentHealthTheme;
	private HealthTheme maliciousHealthTheme;
	
	/** a test study retrieval context */
	private StudyResultRetrievalContext validStudyResultRetrievalContext;

	/** an invalid study retrieval context */
	private StudyResultRetrievalContext emptyStudyResultRetrievalContext;

	/** a non-existent study retrieval context */
	private StudyResultRetrievalContext nonExistentStudyResultRetrievalContext;

	/** a malicious study retrieval context */
	private StudyResultRetrievalContext maliciousStudyRetrievalContext;	
	
	private MapArea validMapArea;
	
	private MapArea emptyMapArea;

	private MapArea nonExistentMapArea;

	private MapArea maliciousMapArea;

	private GeoLevelAttributeSource validGeoLevelAttributeSource;
	private GeoLevelAttributeSource emptyGeoLevelAttributeSource;
	private GeoLevelAttributeSource nonExistentGeoLevelAttributeSource;
	private GeoLevelAttributeSource maliciousGeoLevelAttributeSource;
	
	private String validGeoLevelSourceAttribute;
	private String emptyGeoLevelSourceAttribute;
	private String nonExistentGeoLevelSourceAttribute;
	private String maliciousGeoLevelSourceAttribute;
	
	private StudySummary masterValidStudySummary;
	private StudySummary masterEmptyStudySummary;
	private StudySummary masterNonExistentStudySummary;
	private StudySummary masterMaliciousStudySummary;

	
	private Project masterValidProject;
	private Project masterEmptyProject;
	private Project masterNonExistentProject;
	private Project masterMaliciousProject;

	public CommonRIFServiceTestCase() {
		super();

		FieldValidationUtility fieldValidationUtility 
			= new FieldValidationUtility();
		String maliciousFieldValue 
			= fieldValidationUtility.getTestMaliciousFieldValue();

		nonExistentUser = User.newInstance("nobody", "11.111.11.228");
		emptyUser = User.newInstance(null, "11.111.11.228");
		maliciousUser = User.newInstance(maliciousFieldValue, "11.111.11.228");

		validGeography
			= Geography.newInstance("SAHSU", "stuff about sahsuland");
		nonExistentGeography
			= Geography.newInstance("NeverEverLand", "something something");
		emptyGeography
			= Geography.newInstance("", "");
		maliciousGeography
			= Geography.newInstance(maliciousFieldValue, "");	
			
		validGeoLevelSelectValue
			= GeoLevelSelect.newInstance("LEVEL2");
		emptyGeoLevelSelectValue
			= GeoLevelSelect.newInstance(null);
		nonExistentGeoLevelSelectValue
			= GeoLevelSelect.newInstance("Blah-de-blah");
		maliciousGeoLevelSelectValue
			= GeoLevelSelect.newInstance(maliciousFieldValue);

		validGeoLevelAreaValue
			= GeoLevelArea.newInstance();
		validGeoLevelAreaValue.setIdentifier("01.004");
		validGeoLevelAreaValue.setName("Hambly");	
		emptyGeoLevelAreaValue = GeoLevelArea.newInstance();
		nonExistentGeoLevelAreaValue
			= GeoLevelArea.newInstance();
		nonExistentGeoLevelAreaValue.setIdentifier("zzz");
		nonExistentGeoLevelAreaValue.setName("QQQ");
		maliciousGeoLevelAreaValue
			= GeoLevelArea.newInstance();
		maliciousGeoLevelAreaValue.setIdentifier(maliciousFieldValue);
		maliciousGeoLevelAreaValue.setName(maliciousFieldValue);
				
		validGeoLevelToMapValue 
			= GeoLevelToMap.newInstance("LEVEL4");
		emptyGeoLevelToMapValue
			= GeoLevelToMap.newInstance("");
		nonExistentGeoLevelToMapValue
			= GeoLevelToMap.newInstance("non existent geolevel");
		maliciousGeoLevelToMapValue
			= GeoLevelToMap.newInstance(maliciousFieldValue);
	
		validGeoLevelViewValue 
			= GeoLevelView.newInstance("LEVEL4");
		emptyGeoLevelViewValue
			= GeoLevelView.newInstance("");
		nonExistentGeoLevelViewValue
			= GeoLevelView.newInstance("non existent geolevel");
		maliciousGeoLevelViewValue
			= GeoLevelView.newInstance(maliciousFieldValue);
		
		validNDPair
		= NumeratorDenominatorPair.newInstance(
			"SAHSULAND_CANCER", 
			"Cancer cases in SAHSU land", 
			"SAHSULAND_POP", 
			"SAHSU land population");
	
		emptyNDPair
			= NumeratorDenominatorPair.newInstance(
				"", 
				"Cancer cases in SAHSU land", 
				"SAHSULAND_POP",
					"SAHSU land population");
	
		nonExistentNDPair
			= NumeratorDenominatorPair.newInstance(
				"SAHSULAND_XYZ_CANCER", 
				"non existent numerator table", 
				"SAHSULAND_POP_XYZ", 
					"non existent denominator table");
	
		maliciousNDPair
			= NumeratorDenominatorPair.newInstance(
				fieldValidationUtility.getTestMaliciousFieldValue(), 
				"Cancer cases in SAHSU land", 
				"SAHSULAND_POP", 
				"SAHSU land population");
		

		
		validHealthTheme
			= HealthTheme.newInstance(
				"SAHSULAND", 
				"SAHSU land cancer incidence example data");

		emptyHealthTheme
			= HealthTheme.newInstance(
				"",
				null);
		
		nonExistentHealthTheme
			= HealthTheme.newInstance(
				"KEVSTERLAND", 
				"a land far far away.");

		maliciousHealthTheme
			= HealthTheme.newInstance(
				"SAHSULAND",
				fieldValidationUtility.getTestMaliciousFieldValue());


		validStudyResultRetrievalContext 
			= StudyResultRetrievalContext.newInstance(
				"SAHSU",
				"LEVEL3",
				"1");
		nonExistentStudyResultRetrievalContext
			= StudyResultRetrievalContext.newInstance(
				"blah1", 
				"blah2",
				"blah3");
		emptyStudyResultRetrievalContext
			= StudyResultRetrievalContext.newInstance(
				null,
				"",
				null);
		maliciousStudyRetrievalContext
			= StudyResultRetrievalContext.newInstance(
				maliciousFieldValue,
				"LEVEL3",
				"1");
				
		validMapArea 
			= MapArea.newInstance(
				"01.008.003500.1", 
				"01.008.003500.1", 
				"01.008.003500.1",
			1);		

		emptyMapArea
			= MapArea.newInstance(
				"", 
				"01.008.003500.1",				
				"01.008.003500.1",
			1);		
		nonExistentMapArea
			= MapArea.newInstance(
				"99.998.999599", 
				"99.998.999599", 
				"99.999.999999",
			1);		
		maliciousMapArea
			= MapArea.newInstance(
				maliciousFieldValue, 
				maliciousFieldValue, 
				maliciousFieldValue,
			1);

		validGeoLevelAttributeSource
			= GeoLevelAttributeSource.newInstance("s1_map");
		emptyGeoLevelAttributeSource
			= GeoLevelAttributeSource.newInstance("");
		nonExistentGeoLevelAttributeSource
			= GeoLevelAttributeSource.newInstance("blah_de_blah");
		maliciousGeoLevelAttributeSource
			= GeoLevelAttributeSource.newInstance(getTestMaliciousValue());
		
		validGeoLevelSourceAttribute = "observed";
		emptyGeoLevelSourceAttribute = "";
		nonExistentGeoLevelSourceAttribute = "obyyyserved";
		maliciousGeoLevelSourceAttribute
			= getTestMaliciousValue();
		masterValidStudySummary
			= StudySummary.newInstance(
				"1", 
				"SAHSULAND test example", 
					"");
		masterEmptyStudySummary
			= StudySummary.newInstance(
				"", 
				"", 
					"");
		masterNonExistentStudySummary
			= StudySummary.newInstance(
				"9999", 
				"SAHSULAND test example", 
					"");
		masterMaliciousStudySummary
			= StudySummary.newInstance(
				fieldValidationUtility.getTestMaliciousFieldValue(), 
				"", 
				"");

		masterValidProject 
			= Project.newInstance(
				"TEST",
				"Test Project. Will be disabled when in production.");
		
		masterEmptyProject 
			= Project.newInstance(
				"",
				"Test Project. Will be disabled when in production.");

		masterNonExistentProject 
			= Project.newInstance(
				"blah de blah",
				"Test Project. Will be disabled when in production.");

		masterMaliciousProject 
			= Project.newInstance(
				maliciousFieldValue,
				"Test Project. Will be disabled when in production.");
	}

	// @After
	// public void tearDown() {
	//
	// 	try {
	// 		rifServiceBundle.deregisterAllUsers();
	// 	}
	// 	catch(RIFServiceException exception) {
	// 		exception.printStackTrace(System.out);
	// 	}
	// }

	protected Geography cloneValidGeography() {
		return Geography.createCopy(validGeography);
	}

	protected Geography cloneEmptyGeography() {
		return Geography.createCopy(emptyGeography);
	}
	
	protected Geography cloneNonExistentGeography() {
		return Geography.createCopy(nonExistentGeography);
	}
	
	protected Geography cloneMaliciousGeography() {
		return Geography.createCopy(maliciousGeography);
	}
	
	protected User cloneValidUser() {
		return User.createCopy(validUser);
	}
	
	protected User cloneEmptyUser() {
		return User.createCopy(emptyUser);
	}

	protected User cloneNonExistentUser() {
		return User.createCopy(nonExistentUser);
	}
	
	protected User cloneMaliciousUser() {
		return User.createCopy(maliciousUser);
	}

	
	protected GeoLevelSelect cloneValidGeoLevelSelect() {
		return GeoLevelSelect.createCopy(validGeoLevelSelectValue);
	}
	
	protected GeoLevelSelect cloneEmptyGeoLevelSelect() {
		return GeoLevelSelect.createCopy(emptyGeoLevelSelectValue);
	}
	
	protected GeoLevelSelect cloneNonExistentGeoLevelSelect() {
		return GeoLevelSelect.createCopy(nonExistentGeoLevelSelectValue);		
	}
	
	protected GeoLevelSelect cloneMaliciousGeoLevelSelect() {
		return GeoLevelSelect.createCopy(maliciousGeoLevelSelectValue);		
	}

	protected GeoLevelToMap cloneValidGeoLevelToMap() {
		return GeoLevelToMap.createCopy(validGeoLevelToMapValue);
	}
	
	protected GeoLevelToMap cloneEmptyGeoLevelToMap() {
		return GeoLevelToMap.createCopy(emptyGeoLevelToMapValue);
	}
	
	protected GeoLevelToMap cloneNonExistentGeoLevelToMap() {
		return GeoLevelToMap.createCopy(nonExistentGeoLevelToMapValue);		
	}
	
	protected GeoLevelToMap cloneMaliciousGeoLevelToMap() {
		return GeoLevelToMap.createCopy(maliciousGeoLevelToMapValue);		
	}

	protected GeoLevelView cloneValidGeoLevelView() {
		return GeoLevelView.createCopy(validGeoLevelViewValue);
	}
	
	protected GeoLevelView cloneEmptyGeoLevelView() {
		return GeoLevelView.createCopy(emptyGeoLevelViewValue);
	}
	
	protected GeoLevelView cloneNonExistentGeoLevelView() {
		return GeoLevelView.createCopy(nonExistentGeoLevelViewValue);		
	}
	
	protected GeoLevelView cloneMaliciousGeoLevelView() {
		return GeoLevelView.createCopy(maliciousGeoLevelViewValue);		
	}

	
	
	
	protected GeoLevelArea cloneValidGeoLevelArea() {
		return GeoLevelArea.createCopy(validGeoLevelAreaValue);
	}
	
	protected GeoLevelArea cloneEmptyGeoLevelArea() {
		return GeoLevelArea.createCopy(emptyGeoLevelAreaValue);
	}
	
	protected GeoLevelArea cloneNonExistentGeoLevelArea() {
		return GeoLevelArea.createCopy(nonExistentGeoLevelAreaValue);
	}
	
	protected GeoLevelArea cloneMaliciousGeoLevelArea() {
		return GeoLevelArea.createCopy(maliciousGeoLevelAreaValue);
	}

	protected NumeratorDenominatorPair cloneValidNDPair() {
		return NumeratorDenominatorPair.createCopy(validNDPair);
	}
	
	protected NumeratorDenominatorPair cloneEmptyNDPair() {
		return NumeratorDenominatorPair.createCopy(emptyNDPair);
	}
	
	protected NumeratorDenominatorPair cloneNonExistentNDPair() {
		return NumeratorDenominatorPair.createCopy(nonExistentNDPair);
	}
	
	protected NumeratorDenominatorPair cloneMaliciousNDPair() {
		return NumeratorDenominatorPair.createCopy(maliciousNDPair);
	}
	
	protected HealthTheme cloneValidHealthTheme() {
		return HealthTheme.createCopy(validHealthTheme);
	}

	protected HealthTheme cloneNonExistentHealthTheme() {
		return HealthTheme.createCopy(nonExistentHealthTheme);		
	}
	
	protected HealthTheme cloneMaliciousHealthTheme() {
		return HealthTheme.createCopy(maliciousHealthTheme);	
	}
	
	protected HealthTheme cloneEmptyHealthTheme() {
		return HealthTheme.createCopy(emptyHealthTheme);
	}
	
	protected StudyResultRetrievalContext cloneValidResultContext() {
		return StudyResultRetrievalContext.createCopy(validStudyResultRetrievalContext);		
	}
	
	protected StudyResultRetrievalContext cloneEmptyResultContext() {
		return StudyResultRetrievalContext.createCopy(emptyStudyResultRetrievalContext);		
	}

	protected StudyResultRetrievalContext cloneNonExistentResultContext() {
		return StudyResultRetrievalContext.createCopy(nonExistentStudyResultRetrievalContext);		
	}

	protected StudyResultRetrievalContext cloneMaliciousResultContext() {
		return StudyResultRetrievalContext.createCopy(maliciousStudyRetrievalContext);		
	}
	
	protected MapArea cloneValidMapArea() {
		return MapArea.createCopy(validMapArea);
	}
	
	protected MapArea cloneEmptyMapArea() {
		return MapArea.createCopy(emptyMapArea);
	}
	
	protected MapArea cloneNonExistentMapArea() {
		return MapArea.createCopy(nonExistentMapArea);		
	}
	
	protected MapArea cloneMaliciousMapArea() {
		return MapArea.createCopy(maliciousMapArea);
	}

	protected ArrayList<MapArea> cloneValidMapAreas() {
		ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
		mapAreas.add(MapArea.createCopy(validMapArea));
		return mapAreas;
	}
	
	protected ArrayList<MapArea> cloneEmptyMapAreas() {
		ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
		mapAreas.add(MapArea.createCopy(validMapArea));
		mapAreas.add(MapArea.createCopy(emptyMapArea));
		return mapAreas;
	}
	
	protected ArrayList<MapArea> cloneNonExistentMapAreas() {
		ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
		mapAreas.add(MapArea.createCopy(validMapArea));
		mapAreas.add(MapArea.createCopy(nonExistentMapArea));
		return mapAreas;
	}
	
	protected ArrayList<MapArea> cloneMaliciousMapAreas() {
		ArrayList<MapArea> mapAreas = new ArrayList<MapArea>();
		mapAreas.add(MapArea.createCopy(validMapArea));
		mapAreas.add(MapArea.createCopy(maliciousMapArea));
		return mapAreas;
	}
		
	protected GeoLevelAttributeSource cloneValidGeoLevelAttributeSource() {
		return GeoLevelAttributeSource.createCopy(validGeoLevelAttributeSource);
	}
	
	protected GeoLevelAttributeSource cloneEmptyGeoLevelAttributeSource() {
		return GeoLevelAttributeSource.createCopy(emptyGeoLevelAttributeSource);
	}
	
	protected GeoLevelAttributeSource cloneNonExistentGeoLevelAttributeSource() {
		return GeoLevelAttributeSource.createCopy(nonExistentGeoLevelAttributeSource);
	}
	
	protected GeoLevelAttributeSource cloneMaliciousGeoLevelAttributeSource() {
		return GeoLevelAttributeSource.createCopy(maliciousGeoLevelAttributeSource);
	}

	protected String getValidGeoLevelSourceAttribute() {
		return validGeoLevelSourceAttribute;
	}
	
	protected String getEmptyGeoLevelSourceAttribute() {
		return emptyGeoLevelSourceAttribute;
	}
	
	protected String getNonExistentGeoLevelSourceAttribute() {
		return nonExistentGeoLevelSourceAttribute;
	}
	
	protected String getMaliciousGeoLevelSourceAttribute() {
		return maliciousGeoLevelSourceAttribute;
	}

	protected StudySummary cloneValidStudySummary() {
		return StudySummary.createCopy(masterValidStudySummary);
	}
	
	protected StudySummary cloneEmptyStudySummary() {
		return StudySummary.createCopy(masterEmptyStudySummary);
	}
	
	protected StudySummary cloneNonExistentStudySummary() {
		return StudySummary.createCopy(masterNonExistentStudySummary);
	}
	
	protected StudySummary cloneMaliciousStudySummary() {
		return StudySummary.createCopy(masterMaliciousStudySummary);
	}

	protected Project cloneValidProject() {
		return Project.createCopy(masterValidProject);
	}
	
	protected Project cloneEmptyProject() {
		return Project.createCopy(masterEmptyProject);
	}
	
	protected Project cloneNonExistentProject() {
		return Project.createCopy(masterNonExistentProject);
	}
	
	protected Project cloneMaliciousProject() {
		return Project.createCopy(masterMaliciousProject);
	}
}
