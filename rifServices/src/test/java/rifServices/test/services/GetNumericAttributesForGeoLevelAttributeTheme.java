package rifServices.test.services;

import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.util.FieldValidationUtility;
import rifServices.businessConceptLayer.GeoLevelAttributeSource;
import rifServices.businessConceptLayer.GeoLevelAttributeTheme;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.StudyResultRetrievalContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

public final class GetNumericAttributesForGeoLevelAttributeTheme 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private GeoLevelAttributeSource validGeoLevelAttributeSource;
	private GeoLevelAttributeSource emptyGeoLevelAttributeSource;
	private GeoLevelAttributeSource nonExistentGeoLevelAttributeSource;
	private GeoLevelAttributeSource maliciousGeoLevelAttributeSource;
	
	private GeoLevelAttributeTheme validGeoLevelAttributeTheme;
	private GeoLevelAttributeTheme emptyGeoLevelAttributeTheme;
	private GeoLevelAttributeTheme nonExistentGeoLevelAttributeTheme;
	private GeoLevelAttributeTheme maliciousGeoLevelAttributeTheme;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public GetNumericAttributesForGeoLevelAttributeTheme() {
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		
		validGeoLevelAttributeSource
			= GeoLevelAttributeSource.newInstance("sahsuland_pop");
		emptyGeoLevelAttributeSource
			= GeoLevelAttributeSource.newInstance("");
		nonExistentGeoLevelAttributeSource
			= GeoLevelAttributeSource.newInstance("abcdefg_hij");
		maliciousGeoLevelAttributeSource
			= GeoLevelAttributeSource.newInstance(fieldValidationUtility.getTestMaliciousFieldValue());
		
		validGeoLevelAttributeTheme
			= GeoLevelAttributeTheme.newInstance("population");
		emptyGeoLevelAttributeTheme
			= GeoLevelAttributeTheme.newInstance("");
		nonExistentGeoLevelAttributeTheme
			= GeoLevelAttributeTheme.newInstance("zzzblahblahblah");
		maliciousGeoLevelAttributeTheme
			= GeoLevelAttributeTheme.newInstance(
				fieldValidationUtility.getTestMaliciousFieldValue());

		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	@Override
	protected GeoLevelAttributeSource cloneValidGeoLevelAttributeSource() {
		return GeoLevelAttributeSource.createCopy(validGeoLevelAttributeSource);
	}
	
	@Override
	protected GeoLevelAttributeSource cloneEmptyGeoLevelAttributeSource() {
		return GeoLevelAttributeSource.createCopy(emptyGeoLevelAttributeSource);
	}
	
	@Override
	protected GeoLevelAttributeSource cloneNonExistentGeoLevelAttributeSource() {
		return GeoLevelAttributeSource.createCopy(nonExistentGeoLevelAttributeSource);
	}
	
	@Override
	protected GeoLevelAttributeSource cloneMaliciousGeoLevelAttributeSource() {
		return GeoLevelAttributeSource.createCopy(maliciousGeoLevelAttributeSource);
	}

	private GeoLevelAttributeTheme cloneValidGeoLevelAttributeTheme() {
		return GeoLevelAttributeTheme.createCopy(validGeoLevelAttributeTheme);
	}
	
	private GeoLevelAttributeTheme cloneEmptyGeoLevelAttributeTheme() {
		return GeoLevelAttributeTheme.createCopy(emptyGeoLevelAttributeTheme);
	}
	
	private GeoLevelAttributeTheme cloneNonExistentGeoLevelAttributeTheme() {
		return GeoLevelAttributeTheme.createCopy(nonExistentGeoLevelAttributeTheme);
	}
	
	private GeoLevelAttributeTheme cloneMaliciousGeoLevelAttributeTheme() {
		return GeoLevelAttributeTheme.createCopy(maliciousGeoLevelAttributeTheme);
	}
	
	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_COMMON1() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			
			//this is a kludge.  Sounds like we should be passing a GeoLevelToMap??
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL4");
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			
			/**
			 * (1) Geography = 'SAHSU'
			 * (2) GeoLevel -- 'LEVEL4' which is probably being incorrectly filled in by
			 * GeoLevelSelect. Perhaps GeoLevelToMap? If so, 
			 * then how to pass GeoLevelToMap without GeoLevelSelect?
			 * (3) geo level attribute 'population'
			 * (4) geo level attribute source = 'sahsuland_pop'
			 * 
			 * Should yield results:
			 * age_sex_group,
			 * level1
			 * level2
			 * level3
			 * total
			 * year
			 */
			String[] results
				= rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
					validUser, 
					validStudyResultRetrievalContext, 
					validGeoLevelAttributeSource, 
					validGeoLevelAttributeTheme);
			assertEquals(6, results.length);
			assertEquals("age_sex_group", results[0]);
			assertEquals("year", results[5]);
			
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			fail();
		}
	}
	
	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				emptyUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_USER, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_NULL1() {
		try {
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				null, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_EMPTY2() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext emptyStudyResultRetrievalContext
				= cloneEmptyResultContext();

			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				emptyStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_STUDY_RESULT_RETRIEVAL_CONTEXT, 
				3);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_NULL2() {
		try {
			User validUser = cloneValidUser();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				null, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_EMPTY3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource emptyGeoLevelAttributeSource
				= cloneEmptyGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				validStudyResultRetrievalContext, 
				emptyGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEO_LEVEL_ATTRIBUTE_SOURCE, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_NULL3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				validStudyResultRetrievalContext, 
				null, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_EMPTY4() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme emptyGeoLevelAttributeTheme
				= cloneEmptyGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				emptyGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_GEO_LEVEL_ATTRIBUTE_THEME, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_NULL4() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				null);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				nonExistentUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_NONEXISTENT2() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext nonExistentStudyResultRetrievalContext
				= cloneValidResultContext();
			nonExistentStudyResultRetrievalContext.setStudyID("99989");
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				nonExistentStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_DISEASE_MAPPING_STUDY, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_NONEXISTENT3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource nonExistentGeoLevelAttributeSource
				= cloneNonExistentGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				validStudyResultRetrievalContext, 
				nonExistentGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEO_LEVEL_ATTRIBUTE_SOURCE, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_NONEXISTENT4() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			
			//this is a kludge.  Sounds like we should be passing a GeoLevelToMap??
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme nonExistentGeoLevelAttributeTheme
				= cloneNonExistentGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				nonExistentGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.NON_EXISTENT_GEO_LEVEL_ATTRIBUTE_THEME, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				maliciousUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_MALICIOUS2() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext maliciousStudyResultRetrievalContext
				= cloneMaliciousResultContext();
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				maliciousStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_MALICIOUS3() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			GeoLevelAttributeSource maliciousGeoLevelAttributeSource
				= cloneMaliciousGeoLevelAttributeSource();
			GeoLevelAttributeTheme validGeoLevelAttributeTheme
				= cloneValidGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				validStudyResultRetrievalContext, 
				maliciousGeoLevelAttributeSource, 
				validGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
	}

	@Test
	public void getNumericAttributesForGeoLevelAttributeTheme_MALICIOUS4() {
		try {
			User validUser = cloneValidUser();
			StudyResultRetrievalContext validStudyResultRetrievalContext
				= cloneValidResultContext();
			
			//this is a kludge.  Sounds like we should be passing a GeoLevelToMap??
			validStudyResultRetrievalContext.setGeoLevelSelectName("LEVEL4");
			GeoLevelAttributeSource validGeoLevelAttributeSource
				= cloneValidGeoLevelAttributeSource();
			GeoLevelAttributeTheme maliciousGeoLevelAttributeTheme
				= cloneMaliciousGeoLevelAttributeTheme();
			rifStudyRetrievalService.getNumericAttributesForGeoLevelAttributeTheme(
				validUser, 
				validStudyResultRetrievalContext, 
				validGeoLevelAttributeSource, 
				maliciousGeoLevelAttributeTheme);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.SECURITY_VIOLATION, 
				1);
		}
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
