package org.sahsu.rif.services.test.businessConceptLayer.ms;

import java.util.ArrayList;

import org.junit.Test;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.services.concepts.AdjustableCovariate;
import org.sahsu.rif.services.concepts.AgeBand;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.CovariateType;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.concepts.YearInterval;
import org.sahsu.rif.services.concepts.YearRange;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.test.AbstractRIFTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;



/**
 *
 *
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

public final class TestInvestigation
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The master investigation. */
	private Investigation masterInvestigation;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new test investigation.
	 */
	public TestInvestigation() {
		masterInvestigation = Investigation.newInstance();
		masterInvestigation.setTitle("Lung Cancer inv");
		HealthTheme healthTheme = HealthTheme.newInstance("cancer");
		masterInvestigation.setHealthTheme(healthTheme);

		NumeratorDenominatorPair ndPair
			= NumeratorDenominatorPair.newInstance(
				"SAHSULAND_CANCER", 
				"Cancer cases in SAHSU land", 
				"SAHSULAND_POP", 
					"SAHSU land population");
		masterInvestigation.setNdPair(ndPair);		
		
		HealthCode healthCode1
			= HealthCode.newInstance(
				"J11", 
				"Influenza, virus not identified", 
				"icd10",
				false);
		masterInvestigation.addHealthCode(healthCode1);

		HealthCode healthCode2
			= HealthCode.newInstance(
				"I21", 
				"Acute myocardial", 
				"icd10",
				false);
		masterInvestigation.addHealthCode(healthCode2);
		
		
		//intentionally mixing up order to test sort routine later
		AgeGroup lowerAgeGroup1
			= AgeGroup.newInstance("123", "10", "12", "10-12");
		AgeGroup upperAgeGroup1 
			= AgeGroup.newInstance("123", "20", "22", "20-22");		
		AgeBand ageBand1
			= AgeBand.newInstance(lowerAgeGroup1, upperAgeGroup1);
		masterInvestigation.addAgeBand(ageBand1);

		AgeGroup lowerAgeGroup3 
			= AgeGroup.newInstance("789", "31", "33", "31-33");
		AgeGroup upperAgeGroup3 
			= AgeGroup.newInstance("789", "48", "50", "48-50");		
		AgeBand ageBand3 
			= AgeBand.newInstance(lowerAgeGroup3, upperAgeGroup3);
		masterInvestigation.addAgeBand(ageBand3);
		
		AgeGroup lowerAgeGroup2 
			= AgeGroup.newInstance("456", "23", "25", "23-25");
		AgeGroup upperAgeGroup2 
			= AgeGroup.newInstance("456", "28", "30", "28-30");		
		AgeBand ageBand2 
			= AgeBand.newInstance(lowerAgeGroup2, upperAgeGroup2);
		masterInvestigation.addAgeBand(ageBand2);
		
		masterInvestigation.setSex(Sex.BOTH);
		YearRange yearRange = YearRange.newInstance("1992", "1997");
		masterInvestigation.setYearRange(yearRange);
		masterInvestigation.setInterval("2");;
		masterInvestigation.addYearInterval(YearInterval.newInstance("1992", "1993"));
		masterInvestigation.addYearInterval(YearInterval.newInstance("1994", "1995"));
		masterInvestigation.addYearInterval(YearInterval.newInstance("1996", "1997"));
		


		AdjustableCovariate adjustableCovariate1
			= AdjustableCovariate.newInstance(
				"SES",
				"1",
				"5",
				CovariateType.NTILE_INTEGER_SCORE);
		masterInvestigation.addCovariate(adjustableCovariate1);
		AdjustableCovariate adjustableCovariate2
			= AdjustableCovariate.newInstance(
				"ETHNICITY", 
				"1", 
				"3",
				CovariateType.NTILE_INTEGER_SCORE);
		
		masterInvestigation.addCovariate(adjustableCovariate2);

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	
	
	@Test
	public void acceptDetectionOfDifferences_COMMON() {
		Investigation investigationA
			= Investigation.createCopy(masterInvestigation);
		investigationA.setSex(Sex.FEMALES);
		
		Investigation investigationB
			= Investigation.createCopy(masterInvestigation);
		investigationB.setDescription("something totally different");
		investigationB.setSex(Sex.BOTH);
		
		ArrayList<String> differences = new ArrayList<String>();
		investigationA.identifyDifferences(
			investigationB, 
			differences);

		assertEquals(2, differences.size());			
	}
	
	
	/**
	 * Test valid investigation n1.
	 */
	@Test
	/**
	 * Accept a valid investigation with typical values.
	 */
	public void acceptValidInstance_COMMON() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}
	}
	
	@Test
	/**
	 * Ensures that the table is able to accept a minimal value for a table name
	 */
	public void acceptValidInstance_UNCOMMON1() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setTitle("b");
			investigation.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}		
	}


	/**
	 * An investigation is invalid if no interval value is specified.
	 * The interval value indicates how the year range should be split to
	 * produce year intervals.  For example, if the year range were [1992, 1995]
	 * an interval of "2" would yield year intervals [1992, 1993], [1994, 1995].
	 */
	@Test
	public void acceptValidInstance_UNCOMMON2() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setInterval("");
			investigation.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}		

		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setInterval(null);
			investigation.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();			
		}		
	}
	
	
	@Test
	/**
	 * Reject invalid titles. The title of the investigation needs to be such
	 * that it can be converted to a valid name for a database table.
	 * For now we're not sure what kind of restrictions apply across 
	 * PostgreSQL, Oracle and SQL Server.  
	 */
	public void rejectInvalidTitle_ERROR() {
		try {
			//should not begin with a number
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setTitle("1 very interesting investigation");
			investigation.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_INVESTIGATION,
					1);
		}

		try {
			//should not begin with a space
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setTitle(" very interesting investigation");
			investigation.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);			
		}
				
		try {
			//should not begin with an underscore
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setTitle("_my_investigation");
			investigation.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);			
		}
		
		try {
			//should not contain characters that are not letter, number or underscore
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setTitle("My qur+$%y investigation");
			investigation.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);			
		}
		
		try {
			//should not allow a value that is more than twenty characters long
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setTitle("This title is more than 20 characters long");
			investigation.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);			
		}
			
	}
		
	/**
	 * Reject blank health theme.
	 */
	@Test
	/**
	 * An investigation is invalid if it has no health theme specified.
	 */
	public void rejectBlankRequiredFields_ERROR() {
		
		//title is blank
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setTitle("");
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}

		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setTitle(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}		
		
		
		//blank numerator denominator pair
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setNdPair(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}		
		
		//sex is blank
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setSex(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}
		
		//year range is blank
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setYearRange(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}
		
		
	}
	
	/**
	 * An investigation is invalid if it has an invalid numerator denominator
	 * pair.
	 */
	@Test
	public void rejectInvalidNumeratorDenominatorPair_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			
			NumeratorDenominatorPair ndPair
				= NumeratorDenominatorPair.newInstance(null, "", "", "");
			investigation.setNdPair(ndPair);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				4);
		}		
	}
	
	
	/**
	 * An investigation is invalid if it has no list of health codes specified.
	 */
	@Test
	public void rejectEmptyHealthCodeList_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setHealthCodes(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}
		
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			ArrayList<HealthCode> emptyHealthCodeList 
				= new ArrayList<HealthCode>();
			investigation.setHealthCodes(emptyHealthCodeList);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}
		
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			ArrayList<HealthCode> healthCodes
				= investigation.getHealthCodes();
			healthCodes.add(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}
		
	}
	
	/**
	 * An investigation is invalid if any of its health codes are invalid.
	 */
	@Test
	public void rejectInvalidHealthCode_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			
			HealthCode invalidHealthCode
				= HealthCode.newInstance("", "", "", false);
			investigation.addHealthCode(invalidHealthCode);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				3);
		}		
	}
		
	/**
	 * An investigation is invalid if it has no age bands specified.
	 */
	@Test
	public void rejectNoAgeBands_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.clearAgeBands();
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}		
	}

	/**
	 * An investigation is invalid if one of its age bands is invalid.
	 * Here, an age band comprises a lower age group and an upper age group
	 * When age band is instantiated, the lower and upper age of both groups is
	 * blanks.  Each of these age group objects has a name, lower limit and upper limit
	 * all of which will be blank. Note that in a valid age group, each of these fields
	 * must be non-blank. Because both age groups are blank, the age band limits 
	 * will also be invalid.  Therefore the total error count will be 3 + 3 + 1 = 7.
	 */
	@Test
	public void rejectInvalidAgeBand_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			AgeBand invalidAgeBand = AgeBand.newInstance();
			investigation.addAgeBand(invalidAgeBand);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				6);
		}	
	}
	
	/**
	 * error if investigation has a flawed year range
	 * The goal here is to just to ensure error checking in year range
	 * is being called.
	 */
	@Test	
	public void invalidYearRange_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			YearRange yearRange = YearRange.newInstance("1997", "1992");
			investigation.setYearRange(yearRange);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}	
	}	
	
	
	/**
	 * error if investigation has no year intervals specified.
	 */
	@Test
	public void rejectEmptyYearIntervalList_ERROR() {

		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setYearIntervals(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}

		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			ArrayList<YearInterval> emptyYearIntervalList 
				= new ArrayList<YearInterval>();
			investigation.setYearIntervals(emptyYearIntervalList);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}
	}	

	
	/**
	 * error if investigation has a flawed year interval
	 * The goal here is to just to ensure error checking in year interval
	 * is being called.
	 */
	@Test
	public void rejectInvalidYearInterval_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			YearInterval yearInterval = YearInterval.newInstance("abc", "def");
			investigation.addYearInterval(yearInterval);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				2);
		}		
	}
	
	
	/**
	 * error if investigation has a year intervals with gaps or overlaps.
	 */
	@Test
	public void rejectInvalidGappingOverlappingYearIntervals_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			
			//overlapping year intervals
			ArrayList<YearInterval> yearIntervals
				= new ArrayList<YearInterval>();
			yearIntervals.add(YearInterval.newInstance("1992", "1995"));
			yearIntervals.add(YearInterval.newInstance("1994", "1996"));
			investigation.setYearIntervals(yearIntervals);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}		

		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			
			//gapping year intervals
			ArrayList<YearInterval> yearIntervals
				= new ArrayList<YearInterval>();
			yearIntervals.add(YearInterval.newInstance("1992", "1994"));
			yearIntervals.add(YearInterval.newInstance("1996", "1997"));
			investigation.setYearIntervals(yearIntervals);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}	
	}

	/**
	 * error if investigation has a year interval with an unreasonable year
	 * in either of its lower or upper limit.
	 */
	@Test
	public void rejectUnreasonableStartEndYearsForYearRange_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			
			//We should get one error because 1066 is unreasonable
			ArrayList<YearInterval> yearIntervals
				= new ArrayList<YearInterval>();
			yearIntervals.add(YearInterval.newInstance("1066", "1992"));
			yearIntervals.add(YearInterval.newInstance("1996", "1997"));
			investigation.setYearIntervals(yearIntervals);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}	
		
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			
			//We should get two errors, first because 1066 is too low
			//second because lower limit is greater than upper limit
			ArrayList<YearInterval> yearIntervals
				= new ArrayList<YearInterval>();
			yearIntervals.add(YearInterval.newInstance("1994", "1066"));
			yearIntervals.add(YearInterval.newInstance("1996", "1997"));
			investigation.setYearIntervals(yearIntervals);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				2);
		}	

		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			
			//Should get two errors: 3453 is unreasonable and it
			//is greater than upper limit
			ArrayList<YearInterval> yearIntervals
				= new ArrayList<YearInterval>();
			yearIntervals.add(YearInterval.newInstance("3453", "1995"));
			yearIntervals.add(YearInterval.newInstance("1996", "1997"));
			investigation.setYearIntervals(yearIntervals);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				2);
		}	
		
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			
			//Should get one error: 4567 is unreasonable
			ArrayList<YearInterval> yearIntervals
				= new ArrayList<YearInterval>();
			yearIntervals.add(YearInterval.newInstance("1994", "4567"));
			yearIntervals.add(YearInterval.newInstance("1996", "1997"));
			investigation.setYearIntervals(yearIntervals);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}	
		
	}

	/**
	 * An investigation is valid if it has no list of covariates specified.
	 *
	@Test
	public void rejectEmptyCovariateList_ERROR() {
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			investigation.setCovariates(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}		

		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			ArrayList<AbstractCovariate> emptyCovariateList
				= new ArrayList<AbstractCovariate>();
			investigation.setCovariates(emptyCovariateList);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}
		
		try {
			Investigation investigation
				= Investigation.createCopy(masterInvestigation);
			ArrayList<AbstractCovariate> covariates
				= investigation.getCovariates();
			covariates.add(null);
			investigation.checkErrors(getValidationPolicy());
			fail();			
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_INVESTIGATION, 
				1);
		}
		
	} */

	/**
	 * Tests whether the investigation can sort its age bands and
	 * provide the correct lowest age group and highest age group
	 * amongst its age bands
	 */
	@Test
	public void acceptCorrectlySortedAgeBands_COMMON() {
		Investigation investigation
			= Investigation.createCopy(masterInvestigation);	
		
		ArrayList<AgeBand> sortedAgeBands
			= AgeBand.sortAgeBands(investigation.getAgeBands());
		assertEquals(3, sortedAgeBands.size());
		
		//first age band should be [10, 12]....[20,22]
		assertEquals(
			"10-12",
			sortedAgeBands.get(0).getLowerLimitAgeGroup().getName());

		assertEquals(
			"23-25",
			sortedAgeBands.get(1).getLowerLimitAgeGroup().getName());
		
		assertEquals(
			"31-33",
			sortedAgeBands.get(2).getLowerLimitAgeGroup().getName());	
	}
	
	/**
	 * Test security violations.
	 */
	@Test
	public void rejectSecurityViolations_MALICIOUS() {
		Investigation maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		maliciousInvestigation.setIdentifier(getTestMaliciousValue());
		try {
			maliciousInvestigation.checkSecurityViolations();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		maliciousInvestigation.setTitle(getTestMaliciousValue());
		try {
			maliciousInvestigation.checkSecurityViolations();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		
		maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		maliciousInvestigation.setInterval(getTestMaliciousValue());
		try {
			maliciousInvestigation.checkSecurityViolations();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	
		/*
		 * Check that checking for security violations is done recursively with 
		 * child objects
		 */
		maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		AgeGroup lowerLimitAgeGroup 
			= AgeGroup.newInstance("33", "20", "25", "[20, 25]");
		AgeGroup maliciousUpperLimitAgeGroup 
			= AgeGroup.newInstance("34", "80", "85", getTestMaliciousValue());
		AgeBand maliciousAgeBand
			= AgeBand.newInstance(lowerLimitAgeGroup, maliciousUpperLimitAgeGroup);
		maliciousInvestigation.addAgeBand(maliciousAgeBand);
		try {
			maliciousInvestigation.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		AdjustableCovariate maliciousCovariate
			= AdjustableCovariate.newInstance("SES", "1", getTestMaliciousValue(), CovariateType.NTILE_INTEGER_SCORE);		
		maliciousInvestigation.addCovariate(maliciousCovariate);
		try {
			maliciousInvestigation.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		HealthCode maliciousHealthCode
			= HealthCode.newInstance("XYZ", getTestMaliciousValue(), "", true);
		maliciousInvestigation.addHealthCode(maliciousHealthCode);
		try {
			maliciousInvestigation.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		YearInterval maliciousYearInterval
			= YearInterval.newInstance("1988", getTestMaliciousValue());		
		maliciousInvestigation.addYearInterval(maliciousYearInterval);
		try {
			maliciousInvestigation.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
		
		maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		NumeratorDenominatorPair maliciousNumeratorDenominatorPair
			= NumeratorDenominatorPair.newInstance(
				getTestMaliciousValue(), 
				"numerator table",
				"my_denominator_table",
				"my denominator description");
		maliciousInvestigation.setNdPair(maliciousNumeratorDenominatorPair);
		try {
			maliciousInvestigation.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}

		maliciousInvestigation
			= Investigation.createCopy(masterInvestigation);
		HealthTheme maliciousHealthTheme
			= HealthTheme.newInstance("cancer", getTestMaliciousValue());
		maliciousInvestigation.setHealthTheme(maliciousHealthTheme);
		try {
			maliciousInvestigation.checkSecurityViolations();
			fail();
		}
		catch(RIFServiceSecurityException rifServiceSecurityException) {
			//pass
		}
	}
	
	

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
