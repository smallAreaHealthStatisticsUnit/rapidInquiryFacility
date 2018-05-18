package org.sahsu.rif.services.test.businessConceptLayer.pg;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.RIFStudySubmissionAPI;
import org.sahsu.rif.services.concepts.YearInterval;
import org.sahsu.rif.services.concepts.YearRange;
import org.sahsu.rif.services.datastorage.pg.PGSQLTestRIFStudyRetrievalService;
import org.sahsu.rif.services.datastorage.pg.PGSQLTestRIFStudyServiceBundle;
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

public final class TestYearRangeYearInterval
		extends AbstractRIFTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The service. */
	
	private PGSQLTestRIFStudyServiceBundle rifStudyServiceBundle;
	private RIFStudySubmissionAPI service;

	
	/** The master interval91. */
	private YearInterval masterInterval91;
	
	/** The master interval92. */
	private YearInterval masterInterval92;
	
	/** The master interval93. */
	private YearInterval masterInterval93;
	
	/** The master interval94. */
	private YearInterval masterInterval94;
	
	/** The master interval95. */
	private YearInterval masterInterval95;
	
	/** The master interval96. */
	private YearInterval masterInterval96;
	
	/** The master interval97. */
	private YearInterval masterInterval97;
	
	/** The master interval98. */
	private YearInterval masterInterval98;

	
	/** The master interval9192. */
	private YearInterval masterInterval9192;
	
	/** The master interval9293. */
	private YearInterval masterInterval9293;
	
	/** The master interval9394. */
	private YearInterval masterInterval9394;
	
	/** The master interval9495. */
	private YearInterval masterInterval9495;
	
	/** The master interval9596. */
	private YearInterval masterInterval9596;
	
	/** The master interval9697. */
	private YearInterval masterInterval9697;
	
	/** The master interval919293. */
	private YearInterval masterInterval919293;
	
	/** The master interval929394. */
	private YearInterval masterInterval929394;
	
	/** The master interval939495. */
	private YearInterval masterInterval939495;
	
	/** The master interval949596. */
	private YearInterval masterInterval949596;
	
	/** The master interval959697. */
	private YearInterval masterInterval959697;

	@Mock
	RIFStudySubmissionAPI submission;

	/**
	 * Instantiates a new test year range year interval.
	 */
	public TestYearRangeYearInterval() {
		
		masterInterval91 = YearInterval.newInstance("1991", "1991");
		masterInterval92 = YearInterval.newInstance("1992", "1992");
		masterInterval93 = YearInterval.newInstance("1993", "1993");
		masterInterval94 = YearInterval.newInstance("1994", "1994");
		masterInterval95 = YearInterval.newInstance("1995", "1995");
		masterInterval96 = YearInterval.newInstance("1996", "1996");
		masterInterval97 = YearInterval.newInstance("1997", "1997");
		masterInterval98 = YearInterval.newInstance("1998", "1998");
		
		masterInterval9192 = YearInterval.newInstance("1991", "1992");
		masterInterval9293 = YearInterval.newInstance("1992", "1993");
		masterInterval9394 = YearInterval.newInstance("1993", "1994");
		masterInterval9495 = YearInterval.newInstance("1994", "1995");
		masterInterval9596 = YearInterval.newInstance("1995", "1996");		
		masterInterval9697 = YearInterval.newInstance("1996", "1997");
		
		masterInterval919293 = YearInterval.newInstance("1991", "1993");
		masterInterval929394 = YearInterval.newInstance("1992", "1994");
		masterInterval939495 = YearInterval.newInstance("1993", "1995");
		masterInterval949596 = YearInterval.newInstance("1994", "1996");
		masterInterval959697 = YearInterval.newInstance("1995", "1997");
	}

	@Before
	public void setup() {

		// Make sure the mocks defined in the parent are created before we try to use them.
		super.setup();
		try {
			rifStudyServiceBundle = new PGSQLTestRIFStudyServiceBundle(
					resources,
					submission,
					new PGSQLTestRIFStudyRetrievalService());

			service = rifStudyServiceBundle.getRIFStudySubmissionService();
			rifStudyServiceBundle.login(
					"kgarwood",
					"kgarwood");
		}
		catch(RIFServiceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Valid year range n1.
	 */
	@Test
	/**
	 * Accept a vaid year range with typical values.
	 */
	public void acceptValidInstance_COMMON() {
		try {
			YearRange yearRange = YearRange.newInstance("1989", "1992");
			yearRange.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
		
		try {
			YearInterval yearInterval = YearInterval.newInstance("1992", "1993");
			yearInterval.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
	
	@Test
	public void acceptValidInstance_UNCOMMON() {
		
		try {
			YearInterval yearInterval = YearInterval.newInstance("1993", "1993");
			yearInterval.checkErrors(getValidationPolicy());
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}
	
	
	
	/**
	 * Reject blank lower bound for year range.
	 */
	@Test
	/**
	 * A year range is invalid if its lower bound is not specified.
	 */
	public void rejectBlankLowerBoundForYearRange_ERROR() {
		try {
			YearRange yearRange = YearRange.newInstance(null, "1992");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFServiceError.INVALID_YEAR_RANGE,
					1);
		}

		try {
			YearRange yearRange = YearRange.newInstance("", "1992");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				1);
		}		
	}

	/**
	 * Reject invalid lower bound for year range.
	 */
	@Test
	/**
	 * A year range is invalid if its lower bound is not a valid number
	 */
	public void rejectInvalidLowerBoundForYearRange_ERROR() {
		try {
			YearRange yearRange = YearRange.newInstance("blah", "1992");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				1);
		}		
	}
	
	/**
	 * Reject unrealistic lower bound for year range.
	 */
	@Test
	/**
	 * A year range is invalid if its lower bound has an unrealistic value
	 */
	public void rejectUnrealisticLowerBoundForYearRange_ERROR() {
		try {
			YearRange yearRange = YearRange.newInstance("1758", "1992");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				1);
		}
		
		try {
			//Should get two errors: (1) 2544 is unrealistic and
			//(2) lower bound greater than upper bound
			YearRange yearRange = YearRange.newInstance("2544", "1993");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				2);
		}		
	}
	
	/**
	 * Reject blank upper bound for year range.
	 */
	@Test
	/**
	 * A year range is invalid if it has no upper bound specified.
	 */
	public void rejectBlankUpperBoundForYearRange_ERROR() {
		try {
			YearRange yearRange = YearRange.newInstance("1991", null);
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				1);
		}

		try {
			YearRange yearRange = YearRange.newInstance("1991", "");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				1);
		}		
	}
	
	/**
	 * Reject invalid upper bound for year range.
	 */
	@Test
	/**
	 * A year range is invalid if its upper bound is not a valid number
	 */
	public void rejectInvalidUpperBoundForYearRange_ERROR() {
		try {
			YearRange yearRange = YearRange.newInstance("1991", "blah");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				1);
		}		
	}

	/**
	 * Reject unrealistic upper bound for year range.
	 */
	@Test
	/**
	 * A year range is invalid if its upper bound has an unrealistic value
	 */
	public void rejectUnrealisticUpperBoundForYearRange_ERROR() {
		try {
			YearRange yearRange = YearRange.newInstance("1991", "1666");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				2);
		}
		
		try {
			YearRange yearRange = YearRange.newInstance("1991", "3456");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				1);
		}	
	}
	

	/**
	 * Illegal lower bound upper bound combination e1.
	 */
	@Test
	/**
	 * A year range is invalid if its lower bound exceeds its upper bound.
	 */
	public void illegalLowerBoundUpperBoundCombination_ERROR() {
		try {
			YearRange yearRange = YearRange.newInstance("2001", "1987");
			yearRange.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_RANGE, 
				1);			
		}		
	}
	
	
	
	/**
	 * Reject blank start year for year interval.
	 */
	@Test
	/**
	 * A year interval is invalid if it has no start year specified.
	 */
	public void rejectBlankStartYearForYearInterval_ERROR() {
		try {
			YearInterval yearInterval = YearInterval.newInstance(null, "1992");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);
		}

		try {
			YearInterval yearInterval = YearInterval.newInstance("", "1992");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);
		}		
	}
	
	/**
	 * A year interval is invalid if its start year is not a valid number.
	 */
	public void rejectInvalidStartYearForYearInterval_ERROR() {
		try {
			YearInterval yearInterval = YearInterval.newInstance("blah", "1992");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);
		}		
	}
	
	/**
	 * Reject unrealistic start year for year interval.
	 */
	@Test
	/**
	 * A year interval is invalid if its start year has an unrealistic value.
	 */
	public void rejectUnrealisticStartYearForYearInterval_ERROR() {
		try {
			YearInterval yearInterval = YearInterval.newInstance("1758", "1992");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);
		}
		
		try {
			//We should get two errors: (1) 2544 is unrealistic and 
			//(2) it is higher than upper bound
			YearInterval yearInterval = YearInterval.newInstance("2544", "1993");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				2);
		}		
	}
	
	/**
	 * Reject empty end year for year interval.
	 */
	@Test
	/**
	 * A year interval is invalid if it has no end year specified.
	 */
	public void rejectEmptyEndYearForYearInterval_ERROR() {
		try {
			YearInterval yearInterval = YearInterval.newInstance("1991", null);
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);
		}

		try {
			YearInterval yearInterval = YearInterval.newInstance("1991", "");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);
		}		
	}
	
	/**
	 * Reject invalid end year for year interval.
	 */
	@Test
	/**
	 * A year interval is invalid if its end year is not a valid number.
	 */
	public void rejectInvalidEndYearForYearInterval_ERROR() {
		try {
			YearInterval yearInterval = YearInterval.newInstance("1991", "blah");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);
		}		
	}
	
	/**
	 * Reject unrealistic end year for year interval.
	 */
	@Test
	/**
	 * A year interval is invalid if its end year is an unrealistic value.
	 */
	public void rejectUnrealisticEndYearForYearInterval_ERROR() {
		try {
			//We should get two errors: (1) lower bound is greater than upper
			//bound and (2) upper bound is unrealistic
			YearInterval yearInterval = YearInterval.newInstance("1991", "1666");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				2);
		}
		
		try {
			YearInterval yearInterval = YearInterval.newInstance("1991", "3456");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);
		}				
	}

	/**
	 * Illegal lower stard end date combination e1.
	 */
	@Test
	/**
	 * A year interval is invalid if its start year exceeds its end year.
	 */
	public void illegalLowerStartEndDateCombination_ERROR() {
		try {
			YearInterval yearInterval = YearInterval.newInstance("2001", "1987");
			yearInterval.checkErrors(getValidationPolicy());
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException, 
				RIFServiceError.INVALID_YEAR_INTERVAL, 
				1);			
		}		
	}

	/**
	 * Accept valid interval value.
	 */
	@Test
	/**
	 * Accept a valid interval value N which is used to split a year range
	 * into year intervals.  N should be an integer but it is managed as a
	 * String.
	 */
	public void acceptValidIntervalValue_COMMON() {
		YearRange yearRange = YearRange.newInstance("2000", "2010");

		boolean isValidInterval = yearRange.isValidInterval(2);

		assertEquals(true, isValidInterval);
	}
	
	/**
	 * Reject zero interval value.
	 */
	@Test
	/**
	 * An interval value is invalid if it is zero.  It must be non-zero in order
	 * to help split a year range into multiple year intervals.
	 */
	public void rejectZeroIntervalValue_ERROR() {
		YearRange yearRange = YearRange.newInstance("2000", "2005");
		boolean isValidInterval1 = yearRange.isValidInterval(0);
		assertEquals(false, isValidInterval1);
		
		boolean isValidInterval2 = yearRange.isValidInterval(7);
		assertEquals(false, isValidInterval2);
	}

	/**
	 * Detect extent of gaps and overlaps.
	 */
	@Test
	/**
	 * A utility should detect the extent of gap or overlap between two year
	 * intervals.
	 */
	public void acceptExtentCalculationsOfGapsAndOverlaps_COMMON() {
		
		YearInterval interval919293 = YearInterval.createCopy(masterInterval919293);
		YearInterval interval959697 = YearInterval.createCopy(masterInterval959697);
		
		//Scenario 1: 3 and 3 - Gap of 1
		//Interval A: [1991 1992 1993]
		//                           |1994|
		//Interval B:                     [1995 1996 1997]
		int fitExtent1 = interval919293.calculateFitExtent(interval959697);
		assertEquals(1, fitExtent1);
		
		//Scenario 2: 3 and 3 - Contiguous
		//Interval A: [1991 1992 1993]
		//                           |1994|
		//Interval B:                [1994 1995 1996]		
		YearInterval interval949596 = YearInterval.createCopy(masterInterval949596);
		int fitExtent2 = interval919293.calculateFitExtent(interval949596);
		assertEquals(0, fitExtent2);

		//Scenario 3: 3 and 3 - Overlap of 1
		//Interval A: [1991 1992 1993]
		//                      |1993|
		//Interval B:           [1993 1994 1995]
		YearInterval interval939495 = YearInterval.createCopy(masterInterval939495);
		int fitExtent3 = interval919293.calculateFitExtent(interval939495);
		assertEquals(-1, fitExtent3);
		
		//Scenario 4: 3 and 3 - Overlap of 2
		//Interval A: [1991 1992 1993]
		//                 |1992 1993|
		//Interval B:      [1992 1993 1994]
		YearInterval interval929394 = YearInterval.createCopy(masterInterval929394);
		int fitExtent4 = interval929394.calculateFitExtent(interval939495);
		assertEquals(-2, fitExtent4);
		
		//Scenario 5: 3 and 3 - Overlap of 3
		//Interval A: [1991 1992 1993]
		//            |1991 1992 1993|
		//Interval B: [1991 1992 1993]
		YearInterval interval919293B = YearInterval.createCopy(masterInterval919293);
		int fitExtent5 = interval919293.calculateFitExtent(interval919293B);
		assertEquals(-3, fitExtent5);
		
		//Scenario 6: 2 and 2 - Gap of 1
		//Interval A: [1991 1992]
		//                      |1993|
		//Interval B:                [1994 1995]
		YearInterval interval9192 = YearInterval.createCopy(masterInterval9192);
		YearInterval interval9495 = YearInterval.createCopy(masterInterval9495);
		int fitExtent6 = interval9192.calculateFitExtent(interval9495);
		assertEquals(1, fitExtent6);

		//Scenario 7: 2 and 2 - Contiguous
		//Interval A: [1991 1992]
		//                 |1992|
		//Interval B:           [1993 1994]
		YearInterval interval9394 = YearInterval.createCopy(masterInterval9394);
		int fitExtent7 = interval9192.calculateFitExtent(interval9394);
		assertEquals(0, fitExtent7);
		
		//Scenario 8: 2 and 2 - Overlap of 1
		//Interval A: [1991 1992]
		//                 |1992|
		//Interval B:      [1992 1993]
		YearInterval interval9293 = YearInterval.createCopy(masterInterval9293);
		int fitExtent8 = interval9192.calculateFitExtent(interval9293);
		assertEquals(-1, fitExtent8);
		
		//Scenario 9: 2 and 2 - Overlap of 2
		//Interval A: [1991 1992]
		//            |1991 1992|
		//Interval B: [1991 1992]
		YearInterval interval9192B = YearInterval.createCopy(masterInterval9192);
		int fitExtent9 = interval9192.calculateFitExtent(interval9192B);
		assertEquals(-2, fitExtent9);
		
		//Scenario 10: 2 and 1 - Gap of 1
		//Interval A: [1991 1992]
		//                      |1993|
		//Interval B:                [1994]
		YearInterval interval94 = YearInterval.createCopy(masterInterval94);
		int fitExtent10 = interval9192.calculateFitExtent(interval94);
		assertEquals(1, fitExtent10);
		
		//Scenario 11: 2 and 1 - Contiguous
		//Interval A: [1991 1992]
		//                      |1993|
		//Interval B:           [1993]
		YearInterval interval93 = YearInterval.createCopy(masterInterval93);
		int fitExtent11 = interval9192.calculateFitExtent(interval93);
		assertEquals(0, fitExtent11);

		//Scenario 12: 2 and 1 - Overlap of 1
		//Interval A: [1991 1992]
		//                 |1992|
		//Interval B:      [1992]
		YearInterval interval92 = YearInterval.createCopy(masterInterval92);
		int fitExtent12 = interval9192.calculateFitExtent(interval92);
		assertEquals(-1, fitExtent12);
		
		//Scenario 13: 2 and 1 - Overlap of 1, shared start date
		//Interval A: [1991 1992]
		//            |1991|
		//Interval B: [1991]
		YearInterval interval91 = YearInterval.createCopy(masterInterval91);
		int fitExtent13 = interval9192.calculateFitExtent(interval91);
		assertEquals(-1, fitExtent13);
		
		//Scenario 14: 1 and 1 - Gap of 1
		//Interval A: [1991]
		//                 |1992|
		//Interval B:           [1993]
		int fitExtent14 = interval91.calculateFitExtent(interval93);
		assertEquals(1, fitExtent14);
		
		//Scenario 15: 1 and 1 - Contiguous
		//Interval A: [1991]
		//                 |1992|
		//Interval B:      [1992]
		int fitExtent15 = interval91.calculateFitExtent(interval92);
		assertEquals(0, fitExtent15);
		
		//Scenario 16: 1 and 1 - Contiguous
		//Interval A: [1991]
		//            |1991|
		//Interval B: [1991]
		YearInterval interval91B = YearInterval.createCopy(masterInterval91);
		int fitExtent16 = interval91.calculateFitExtent(interval91B);
		assertEquals(-1, fitExtent16);
	}
	
	/**
	 * Detect multiple gaps overlaps e1.
	 */
	@Test
	/**
	 * A utility should be able to detect multiple gaps and overlaps within 
	 * a collection of year intervals.
	 */
	public void rejectMultipleGapsOverlaps_ERROR() {
		
		//[1991] [1991,1993] [1995,1996] [1997]
		ArrayList<YearInterval> yearIntervals1 = new ArrayList<YearInterval>();
		yearIntervals1.add(YearInterval.createCopy(masterInterval91));
		yearIntervals1.add(YearInterval.createCopy(masterInterval919293));
		yearIntervals1.add(YearInterval.createCopy(masterInterval9596));
		yearIntervals1.add(YearInterval.createCopy(masterInterval97));
		
		ArrayList<String> errorMessages1
			= YearInterval.checkGapsAndOverlaps(yearIntervals1);
		assertEquals(2, errorMessages1.size());
				
		//[1991,1992] [1993,1994] [1995, 1996]
		ArrayList<YearInterval> yearIntervals2 = new ArrayList<YearInterval>();
		yearIntervals2.add(YearInterval.createCopy(masterInterval9192));
		yearIntervals2.add(YearInterval.createCopy(masterInterval9394));
		yearIntervals2.add(YearInterval.createCopy(masterInterval9596));
		ArrayList<String> errorMessages2
			= YearInterval.checkGapsAndOverlaps(yearIntervals2);
		assertEquals(0, errorMessages2.size());
		
		//[1991] [1996]
		ArrayList<YearInterval> yearIntervals3 = new ArrayList<YearInterval>();
		yearIntervals3.add(YearInterval.createCopy(masterInterval91));
		yearIntervals3.add(YearInterval.createCopy(masterInterval96));
		ArrayList<String> errorMessages3
			= YearInterval.checkGapsAndOverlaps(yearIntervals3);
		assertEquals(1, errorMessages3.size());		
	}

	
	/**
	 * Detect all contiguous year intervals1.
	 */
	@Test
	/**
	 * A utility should be able to identify whether an ordered collection of 
	 * year intervals has contiguous values 
	 */
	public void acceptAllContiguousYearIntervals_COMMON() {		
		YearRange yearRange = YearRange.newInstance("1993", "1996");
		//[1993, 1994], [1995, 1995], [1996, 1996]
		ArrayList<YearInterval> yearIntervals1 = new ArrayList<YearInterval>();
		yearIntervals1.add(YearInterval.createCopy(masterInterval9394));
		yearIntervals1.add(YearInterval.createCopy(masterInterval95));
		yearIntervals1.add(YearInterval.createCopy(masterInterval96));
		ArrayList<String> errorMessages
			= yearRange.identifyOutOfBoundsErrors(
				getValidationPolicy(), 
				yearIntervals1);
		
		//no gaps or overlaps
		assertEquals(0, errorMessages.size());
	}
	
	/**
	 * Detect out of bounds year intervals e1.
	 */
	@Test
	/**
	 * A utility should be able to identify year intervals which fall outside
	 * of bounds set by year range
	 */
	public void rejectOutOfBoundsYearIntervals_ERROR() {
		
		YearRange yearRange = YearRange.newInstance("1993", "1996");
		//[1991, 1992] [1993,1994] [1995,1995] [1996, 1997], [1998, 1998]
		ArrayList<YearInterval> yearIntervals1 = new ArrayList<YearInterval>();
		yearIntervals1.add(YearInterval.createCopy(masterInterval9192));
		yearIntervals1.add(YearInterval.createCopy(masterInterval9394));
		yearIntervals1.add(YearInterval.createCopy(masterInterval95));
		yearIntervals1.add(YearInterval.createCopy(masterInterval9697));
		yearIntervals1.add(YearInterval.createCopy(masterInterval98));
		
		ArrayList<String> errorMessages
			= yearRange.identifyOutOfBoundsErrors(
				getValidationPolicy(),
				yearIntervals1);
		assertEquals(3, errorMessages.size());
	}
	
	/**
	 * Split year range n1.
	 */
	@Test
	/**
	 * A utility should be able to use a year range and an interval value
	 * to create a collection of year intervals that fit within that year range.
	 */
	public void acceptSplitYearRange_COMMON() {
		YearRange yearRange1 = YearRange.newInstance("1992", "1996");
		try {
			ArrayList<YearInterval> yearIntervals1
				= yearRange1.splitYearRange(2);
			assertEquals(3, yearIntervals1.size());
			
			//Year Intervals should be:
			//[1992, 1993], [1994, 1995], [1996, 1996]
			assertEquals("1992", yearIntervals1.get(0).getStartYear());
			assertEquals("1993", yearIntervals1.get(0).getEndYear());
			assertEquals("1994", yearIntervals1.get(1).getStartYear());
			assertEquals("1995", yearIntervals1.get(1).getEndYear());
			assertEquals("1996", yearIntervals1.get(2).getStartYear());
			assertEquals("1996", yearIntervals1.get(2).getEndYear());
			
			YearRange yearRange2 = YearRange.newInstance("1992", "1997");
			ArrayList<YearInterval> yearIntervals2
				= yearRange2.splitYearRange(2);
			assertEquals(3, yearIntervals2.size());
			
			//Year Intervals should be:
			//[1992, 1993], [1994, 1995], [1996, 1996]
			assertEquals("1992", yearIntervals2.get(0).getStartYear());
			assertEquals("1993", yearIntervals2.get(0).getEndYear());
			assertEquals("1994", yearIntervals2.get(1).getStartYear());
			assertEquals("1995", yearIntervals2.get(1).getEndYear());
			assertEquals("1996", yearIntervals2.get(2).getStartYear());
			assertEquals("1997", yearIntervals2.get(2).getEndYear());
			

			YearRange yearRange3 = YearRange.newInstance("1992", "1997");
			ArrayList<YearInterval> yearIntervals3
				= yearRange3.splitYearRange(3);
			assertEquals(2, yearIntervals3.size());
			
			//Year Intervals should be:
			//[1992, 1993, 1994], [1995, 1996, 1997]
			assertEquals("1992", yearIntervals3.get(0).getStartYear());
			assertEquals("1994", yearIntervals3.get(0).getEndYear());
			assertEquals("1995", yearIntervals3.get(1).getStartYear());
			assertEquals("1997", yearIntervals3.get(1).getEndYear());			
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
