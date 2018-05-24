
package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

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


final public class YearRange 
	extends AbstractRIFConcept {


// ==========================================
// Section Constants
// ==========================================
	
	//KLG: 
	//@TODO At some point, make this something that can be set through a property
	/** The Constant REALISTIC_LOWEST_YEAR. */
	private static final int REALISTIC_LOWEST_YEAR = 1950;
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
	/** The lower bound. */
	private String lowerBound;
	
	/** The upper bound. */
	private String upperBound;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new year range.
     *
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     */
	private YearRange(
		final String lowerBound,
		final String upperBound) {

		this.lowerBound = lowerBound;
		this.upperBound = upperBound;		
    }

    /**
     * Instantiates a new year range.
     */
    private YearRange() {

    	lowerBound = "";
    	upperBound = "";
    }
    
    /**
     * New instance.
     *
     * @return the year range
     */
    public static YearRange newInstance() {       	
  
    	YearRange yearRange = new YearRange();
        return yearRange;
    }
    
    
    /**
     * New instance.
     *
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     * @return the year range
     */
    public static YearRange newInstance(
    	final String lowerBound,
    	final String upperBound) {
    	
    	YearRange yearRange = new YearRange(lowerBound, upperBound);
    	return yearRange;
    }
    
    /**
     * Creates the copy.
     *
     * @param originalYearRange the original year range
     * @return the year range
     */
    public static YearRange createCopy(
    	final YearRange originalYearRange) {
   
    	YearRange cloneYearAge
    		= new YearRange(
    			originalYearRange.getLowerBound(),
    			originalYearRange.getUpperBound());
    	return cloneYearAge;			
    }
    
// ==========================================
// Section Accessors and Mutators
// ==========================================
    /*
     * returns true if the integer represented by the string 'value'
     * is within bounds.  Returns false if either value is not within
     * boundaries or if value is not a legitimate positive integer
     */
	/**
	 * Checks if is value within bounds.
	 *
	 * @param value the value
	 * @return true, if is value within bounds
	 */
    public boolean isValueWithinBounds(
    	final String value) {
		
		try {
			Integer fieldValue = Integer.valueOf(value);
			Integer lowerBoundValue = Integer.valueOf(lowerBound);
			Integer upperBoundValue = Integer.valueOf(upperBound);
			
			if (fieldValue < lowerBoundValue) {
				return false;
			}
			
			if (fieldValue > upperBoundValue) {
				return false;
			}
			
			return true;
			
		}
		catch(NumberFormatException numberFormatException) {
						
		}
		
		return false;
	}

	/**
	 * Gets the lower bound.
	 *
	 * @return the lower bound
	 */
	public String getLowerBound() {
		
		return lowerBound;
	}
	
	/**
	 * Sets the lower bound.
	 *
	 * @param lowerBound the new lower bound
	 */
	public void setLowerBound(
		final String lowerBound) {
		
		this.lowerBound = lowerBound;
	}
	
	/**
	 * Gets the upper bound.
	 *
	 * @return the upper bound
	 */
	public String getUpperBound() {
		
		return upperBound;
	}
	
	/**
	 * Sets the upper bound.
	 *
	 * @param upperBound the new upper bound
	 */
	public void setUpperBound(final String upperBound) {
		
		this.upperBound = upperBound;
	}
	
	/**
	 * returns empty list if number format exceptions
	 * validate should be called before using this method.
	 *
	 * @return the low to high years
	 */
	public ArrayList<String> getLowToHighYears() {
		
		ArrayList<String> results = new ArrayList<String>();
		try {
			int lowerBoundValue = Integer.valueOf(lowerBound);
			int upperBoundValue = Integer.valueOf(upperBound);
			
			for (int i = lowerBoundValue; i <= upperBoundValue; i++) {
				results.add(String.valueOf(i));
			}			
		}
		catch(NumberFormatException numberFormatException) {
			//TODO: ignore this exception
		}
		return results;
	}
	
	/**
	 * Gets the high to low years.
	 *
	 * @return the high to low years
	 */
	public ArrayList<String> getHighToLowYears() {
		
		ArrayList<String> results = new ArrayList<String>();
		try {
			int lowerBoundValue = Integer.valueOf(lowerBound);
			int upperBoundValue = Integer.valueOf(upperBound);
			
			for (int i = upperBoundValue; i >= lowerBoundValue; i--) {
				results.add(String.valueOf(i));
			}			
		}
		catch(NumberFormatException numberFormatException) {
			//TODO: ignore this exception
		}
		return results;		
	}
	
	/**
	 * Assumes checkErrors() has been called on this year range.
	 *
	 * @return a collection of choices for interval.
	 * null if lower or upper bound are invalid
	 */
	public ArrayList<String> getYearIntervalChoices() {
		try {
			Integer lowerBoundValue = Integer.valueOf(lowerBound);
			Integer upperBoundValue = Integer.valueOf(upperBound);
			int numberOfChoices = Math.abs(upperBoundValue - lowerBoundValue) + 1;
		
			ArrayList<String> results = new ArrayList<String>();
			String noneChoice
				= RIFServiceMessages.getMessage("general.choices.none");
			results.add(noneChoice);
			
			for (int i = 1; i <= numberOfChoices; i++) {
				results.add(String.valueOf(i));
			}
			
			return results;		
		}
		catch(NumberFormatException numberFormatException) {
			
		}
		return null;
	}

	/**
	 * Assumes you will have called checkErrors() first 
	 * to verify that lower bound and upper bound are legitimate
	 * integer values and that lower bound is not greater than upper bound.
	 *
	 * @return the maximum interval size
	 */
	public Integer getMaximumIntervalSize() {
		
		Integer result = null;
		try {
			int lowerBoundValue = Integer.valueOf(lowerBound);
			int upperBoundValue = Integer.valueOf(upperBound);
			result = upperBoundValue - lowerBoundValue + 1;
		}
		catch(NumberFormatException numberFormatException) {
			//leave empty.  Assumes checkErrors() has been called
			//to notify user of problems in YearRange object
		}
		
		return result;
	}
	
	/**
	 * Gets the year interval.
	 *
	 * @param yearRange the year range
	 * @return the year interval
	 */
	public static YearInterval getYearInterval(
		final YearRange yearRange) {

		Integer lowerBoundValue = null;
		Integer upperBoundValue = null;
		try {
			lowerBoundValue = Integer.valueOf(yearRange.getLowerBound());
			upperBoundValue = Integer.valueOf(yearRange.getUpperBound());
			
			YearInterval yearInterval
				= YearInterval.newInstance(
					String.valueOf(lowerBoundValue),
					String.valueOf(upperBoundValue));

			return yearInterval;
			
		}
		catch(NumberFormatException numberFormatException) {
			return null;
		}
	}
	
	/**
	 * Assumes checkErrors has already been called to make this a valid
	 * Year Interval.
	 *
	 * @param intervalSize the interval size
	 * @return null if there year interval is invalid
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<YearInterval> splitYearRange(
		final int intervalSize) 
		throws RIFServiceException {

		Integer lowerBoundValue = null;
		Integer upperBoundValue = null;
		try {
			lowerBoundValue = Integer.valueOf(lowerBound);
			upperBoundValue = Integer.valueOf(upperBound);		
		}
		catch(NumberFormatException numberFormatException) {
			return null;
		}
		
		int maximumAllowableIntervalSize
			= getMaximumIntervalSize();
		if (intervalSize > maximumAllowableIntervalSize) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"yearRange.error.intervalTooHigh",
					String.valueOf(intervalSize),
					String.valueOf(maximumAllowableIntervalSize));
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.YEAR_RANGE_INTERVAL_TOO_HIGH,
					errorMessage);
			throw rifServiceException;
		}
		
		//1,2,3,4,5,6
		int completeIntervals = maximumAllowableIntervalSize / intervalSize;
		int sizeOfLastInterval = maximumAllowableIntervalSize % intervalSize;
		
		ArrayList<YearInterval> results = new ArrayList<YearInterval>();
		int currentStartYear = lowerBoundValue;
		int currentEndYear = lowerBoundValue + intervalSize - 1;
		
		for (int i = 0; i < completeIntervals; i++) {
			YearInterval yearInterval
				= YearInterval.newInstance(
					String.valueOf(currentStartYear),
					String.valueOf(currentEndYear));
			results.add(yearInterval);
			currentStartYear = currentEndYear + 1;
			currentEndYear = currentStartYear + intervalSize - 1;
		}
		
		if (sizeOfLastInterval > 0) {
			YearInterval lastInterval
				= YearInterval.newInstance(
					String.valueOf(currentStartYear),
					String.valueOf(upperBound));
			results.add(lastInterval);
		}
		
		return results;
	}
	
	/**
	 * Gets the maximum realistic year.
	 *
	 * @return the maximum realistic year
	 */
	public static int getMaximumRealisticYear() {
		
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int realisticHighestYear = calendar.get(Calendar.YEAR);
        return realisticHighestYear;
	}
	
	/**
	 * Gets the minimum realistic year.
	 *
	 * @return the minimum realistic year
	 */
	public static int getMinimumRealisticYear() {
		
		return REALISTIC_LOWEST_YEAR;
	}
	
	public void identifyDifferences(
		final YearRange anotherYearRange,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherYearRange, 
			differences);		
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherYearRange the other year range
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final YearRange otherYearRange) {

		if (otherYearRange == null) {
			return false;
		}
		
		Collator collator = Collator.getInstance();
		
		String otherLowerBound = otherYearRange.getLowerBound();
		String otherUpperBound = otherYearRange.getUpperBound();
		
		if (FieldValidationUtility.hasDifferentNullity(lowerBound, otherLowerBound)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (lowerBound != null) {
			//they must both be non-null
			if (collator.equals(lowerBound, otherLowerBound) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(upperBound, otherUpperBound)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (upperBound != null) {
			//they must both be non-null
			if (collator.equals(upperBound, otherUpperBound) == false) {
				return false;
			}			
		}
		
		return super.hasIdenticalContents(otherYearRange);
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================
	
	/**
	 * Analyses a collection of year intervals and produces error messages for any
	 * which are outside the boundaries of this year range.
	 * Assumes that year intervals are valid instances of YearInterval.
	 *
	 * @param yearIntervals the year intervals
	 * @return the array list
	 */
	public ArrayList<String> identifyOutOfBoundsErrors(
		final ValidationPolicy validationPolicy,
		final ArrayList<YearInterval> yearIntervals) {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
			
		try {
			int lowerBoundValue = Integer.valueOf(lowerBound);
			int upperBoundValue = Integer.valueOf(upperBound);
			
			for (YearInterval yearInterval : yearIntervals) {
				yearInterval.checkErrors(validationPolicy);
				int startYear = Integer.valueOf(yearInterval.getStartYear());
				int endYear = Integer.valueOf(yearInterval.getEndYear());
				
				//Because year interval should be valid, we can be guaranteed
				//that end year will be at least as great as start year
				if (startYear < lowerBoundValue) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"yearRange.error.startYearLowerThanAllowed",
							yearInterval.getDisplayName(),
							getDisplayName());
					errorMessages.add(errorMessage);
				}
				else if (endYear > upperBoundValue) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"yearRange.error.endYearHigherThanAllowed",
							yearInterval.getDisplayName(),
							getDisplayName());
					errorMessages.add(errorMessage);					
				}
			}			
		}
		catch(RIFServiceException rifServiceException) {
			//we should never encounter this
			assert(false);
		}
				
		return errorMessages;
	}
	

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
				
		String recordType = getRecordType();
		String lowerBoundFieldName
			= RIFServiceMessages.getMessage("yearRange.lowerBound.label");		
		String upperBoundFieldName
			= RIFServiceMessages.getMessage("yearRange.upperBound.label");		
		
		//check for nulls
		if (lowerBound == null) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					lowerBoundFieldName);
			errorMessages.add(errorMessage);
		}
		
		if (upperBound == null) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					upperBoundFieldName);
			errorMessages.add(errorMessage);
		}
		
		//If either of the bounds condition is null, there is no 
		//point in doing further error checking.
		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_YEAR_RANGE, 
					errorMessages);
			throw rifServiceException;
		}
		
		//Assumes that the bounds are both non-null
		
		//get the current year for validation checks later on
		int realisticHighestYear = getMaximumRealisticYear();
		
		
		Integer lowerBoundValue = null;
		try {
			lowerBoundValue = Integer.valueOf(lowerBound);
			
			if (lowerBoundValue < REALISTIC_LOWEST_YEAR) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearRange.error.unrealisticlyLowYearValue",
						getDisplayName(),
						lowerBound,
						String.valueOf(REALISTIC_LOWEST_YEAR));
				errorMessages.add(errorMessage);
			}
			else if (lowerBoundValue > realisticHighestYear) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearRange.error.unrealisticlyHighYearValue",
						getDisplayName(),
						lowerBound,
						String.valueOf(getMaximumRealisticYear()));
				errorMessages.add(errorMessage);				
			}
		}
		catch(NumberFormatException numberFormatException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.illegalInteger",
					lowerBound,
					recordType,
					lowerBoundFieldName);
			errorMessages.add(errorMessage);
		}
		
		Integer upperBoundValue = null;
		try {
			upperBoundValue = Integer.valueOf(upperBound);
			if (upperBoundValue < REALISTIC_LOWEST_YEAR) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearRange.error.unrealisticlyLowYearValue",
						getDisplayName(),
						upperBound,
						String.valueOf(REALISTIC_LOWEST_YEAR));
				errorMessages.add(errorMessage);
			}
			else if (upperBoundValue > realisticHighestYear) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearRange.error.unrealisticlyHighYearValue",
						getDisplayName(),
						upperBound,
						String.valueOf(getMaximumRealisticYear()));
				errorMessages.add(errorMessage);				
			}
		}
		catch(NumberFormatException numberFormatException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.illegalInteger",
					upperBound,
					recordType,
					upperBoundFieldName);
			errorMessages.add(errorMessage);
		}
		
		if ((lowerBoundValue != null) && (upperBoundValue != null)) {
			
			if (lowerBoundValue > upperBoundValue) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearRange.error.lowerBoundGreaterThanUpperBound",
						getDisplayName(),
						lowerBound,
						upperBound);
				errorMessages.add(errorMessage);
			}
		
			if (errorMessages.size() > 0) {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.INVALID_YEAR_RANGE,
						errorMessages);
				throw rifServiceException;
			}
		}
		countErrors(RIFServiceError.INVALID_YEAR_RANGE, errorMessages);
	}
	
	/**
	 * Assumes that checkErrors() already run.
	 *
	 * @param interval the interval
	 * @return the boolean
	 */
	public Boolean isValidInterval(
		final int interval) {

		try {
			if (interval <= 0) {
				return false;
			}
			
			int lowerBoundValue = Integer.valueOf(lowerBound);
			int upperBoundValue = Integer.valueOf(upperBound);
			
			int maximumInterval = upperBoundValue - lowerBoundValue + 1;
			if (interval > maximumInterval) {
				return false;
			}
			
			return true;
		}
		catch(NumberFormatException numberFormatException) {
			return null;			
		}		
	}
	
// ==========================================
// Section Interfaces
// ==========================================

// ==========================================
// Section Override
// ==========================================

	@Override
	public String getDisplayName() {

		StringBuilder buffer = new StringBuilder();
		buffer.append("[");
		buffer.append(lowerBound);
		buffer.append("-");
		buffer.append(upperBound);
		buffer.append("]");
		return buffer.toString();
	}


	@Override
	public String getRecordType() {

		String recordType
			= RIFServiceMessages.getMessage("yearRange.label");
		return recordType;
	}
}
