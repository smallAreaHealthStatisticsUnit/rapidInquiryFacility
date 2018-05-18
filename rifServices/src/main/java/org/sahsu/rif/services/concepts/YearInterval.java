package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
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


final public class YearInterval 
	extends AbstractRIFConcept {

// ==========================================
// Section Constants
// ==========================================
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
    
    /** The start year. */
	private String startYear;
    
    /** The end year. */
    private String endYear;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new year interval.
     */
    private YearInterval() {
        startYear = "";
        endYear = "";
    }
    
    /**
     * Instantiates a new year interval.
     *
     * @param startYear the start year
     * @param endYear the end year
     */
    private YearInterval(
    	final String startYear,
    	final String endYear) {
    	
    	this.startYear = startYear;
    	this.endYear = endYear;
    }
	
	/**
	 * New instance.
	 *
	 * @return the year interval
	 */
	static public YearInterval newInstance() {
		
		YearInterval yearInterval = new YearInterval();
		return yearInterval;
	}
	
	/**
	 * New instance.
	 *
	 * @param startYear the start year
	 * @param endYear the end year
	 * @return the year interval
	 */
	static public YearInterval newInstance(
		final String startYear, 
		final String endYear) {
		
		YearInterval yearInterval = new YearInterval(startYear, endYear);
		return yearInterval;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalYearInterval the original year interval
	 * @return the year interval
	 */
	static public YearInterval createCopy(
		final YearInterval originalYearInterval) {

		if (originalYearInterval == null) {
			return null;
		}
		
		YearInterval cloneYearInterval = new YearInterval();
		cloneYearInterval.setIdentifier(originalYearInterval.getIdentifier());
		cloneYearInterval.setStartYear(originalYearInterval.getStartYear());
		cloneYearInterval.setEndYear(originalYearInterval.getEndYear());
        return cloneYearInterval;
	}	
	
	/**
	 * Creates the copy.
	 *
	 * @param originalYearIntervals the original year intervals
	 * @return the array list
	 */
	public static ArrayList<YearInterval> createCopy(
		final ArrayList<YearInterval> originalYearIntervals) {

		if (originalYearIntervals == null) {
			return null;
		}
		
		ArrayList<YearInterval> cloneYearIntervals = new ArrayList<YearInterval>();
		for (YearInterval originalYearInterval : originalYearIntervals) {
			YearInterval cloneYearInterval 
				= YearInterval.createCopy(originalYearInterval);
			cloneYearIntervals.add(cloneYearInterval);
		}
		
		return cloneYearIntervals;
	}
	
	/**
	 * Assumes all the year intervals are valid.
	 *
	 * @param yearIntervals the year intervals
	 * @return the minimum lower limit
	 */
	public static Integer getMinimumLowerLimit(
		final ArrayList<YearInterval> yearIntervals) {

		Integer result = null;
		try {
			for (YearInterval yearInterval : yearIntervals) {
				String currentStartYearPhrase = yearInterval.getStartYear();
				Integer currentStartYear = Integer.valueOf(currentStartYearPhrase);
				if (result == null) {
					result = currentStartYear;
				}
				else {
					if (currentStartYear < result) {
						result = currentStartYear;
					}
				}
			}
		}
		catch(Exception exception) {
			//@TODO how to deal with this exception
		}
		
		return result;
	}

	/**
	 * Assumes all the year intervals are valid.
	 *
	 * @param yearIntervals the year intervals
	 * @return the maximum upper limit
	 */
	public static Integer getMaximumUpperLimit(
		final ArrayList<YearInterval> yearIntervals) {

		Integer result = null;
		try {
			for (YearInterval yearInterval : yearIntervals) {
				String currentStartYearPhrase = yearInterval.getStartYear();
				Integer currentStartYear = Integer.valueOf(currentStartYearPhrase);
				if (result == null) {
					result = currentStartYear;
				}
				else {
					if (currentStartYear > result) {
						result = currentStartYear;
					}
				}
			}
		}
		catch(Exception exception) {
			//@TODO how to deal with this exception
		}
		
		return result;
	}
		
// ==========================================
// Section Accessors and Mutators
// ==========================================

    /**
     * Gets the start year.
     *
     * @return the start year
     */
	public String getStartYear() {
   
		return startYear;
    }

    /**
     * Sets the start year.
     *
     * @param startYear the new start year
     */
    public void setStartYear(
    	final String startYear) {
    
    	this.startYear = startYear;
    }

    /**
     * Gets the end year.
     *
     * @return the end year
     */
    public String getEndYear() {
        return endYear;
    }

    /**
     * Sets the end year.
     *
     * @param endYear the new end year
     */
    public void setEndYear(
    	final String endYear) {

    	this.endYear = endYear;
    }

    /**
     * Assumes that both year intervals involved are valid.
     *
     * @param yearInterval the year interval
     * @return null if the start or end years of either interval are
     * not legal integers
     */
    public Boolean startsBefore(
    	final YearInterval yearInterval) {
  
    	try {
    		int startYearA = Integer.valueOf(startYear);
    		int startYearB = Integer.valueOf(yearInterval.getStartYear());
    		
    		if (startYearB < startYearA) {
    			return true;
    		}
    		
    		return false;
    	}
    	catch(NumberFormatException numberFormatException) {
        	return null;
    	}    	
    }
    
    /**
     * Calculate fit extent.
     *
     * @param yearInterval the year interval
     * @return null if any of the integer values are invalid
     * positive number indicating gaps
     * negative numbers indicating overlaps
     */
    public Integer calculateFitExtent(
    	final YearInterval yearInterval) {

    	try {
    		int startYearA = Integer.valueOf(startYear);
    		int endYearA = Integer.valueOf(endYear);
    		
    		int startYearB = Integer.valueOf(yearInterval.getStartYear());
    		int endYearB = Integer.valueOf(yearInterval.getEndYear());

    		if (startYearA < startYearB) {
    			int fitExtent = startYearB - endYearA - 1;
    			return fitExtent;
    		}
    		else {
    			int fitExtent = startYearA - endYearB - 1;
    			return fitExtent;
    		}
    	}
    	catch(NumberFormatException numberFormatException) {
        	return null;
    	}    	
    }
    
    
	/**
	 * Checks for identical contents.
	 *
	 * @param yearIntervalListA the year interval list a
	 * @param yearIntervalListB the year interval list b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<YearInterval> yearIntervalListA, 
		final ArrayList<YearInterval> yearIntervalListB) {

		if (FieldValidationUtility.hasDifferentNullity(
			yearIntervalListA, 
			yearIntervalListB)) {
			//reject if one is null and the other is non-null
			return false;
		}
				
		if (yearIntervalListA.size() != yearIntervalListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
		
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<YearInterval> yearIntervalsA 
			= sortYearIntervals(yearIntervalListA);
		ArrayList<YearInterval> yearIntervalsB 
			= sortYearIntervals(yearIntervalListB);
			
		int numberOfYearIntervals = yearIntervalListA.size();
		for (int i = 0; i < numberOfYearIntervals; i++) {
			YearInterval yearIntervalA
				= yearIntervalsA.get(i);				
			YearInterval yearIntervalB
				= yearIntervalsB.get(i);
			if (yearIntervalA.hasIdenticalContents(yearIntervalB) == false) {					
				return false;
			}			
		}
			
		return true;
	}

	/**
	 * Sort year intervals.
	 *
	 * @param yearIntervals the year intervals
	 * @return the array list
	 */
	private static ArrayList<YearInterval> sortYearIntervals(
		final ArrayList<YearInterval> yearIntervals) {
		
		DisplayableItemSorter sorter = new DisplayableItemSorter();
			
		for (YearInterval yearInterval : yearIntervals) {
			sorter.addDisplayableListItem(yearInterval);
		}
			
		ArrayList<YearInterval> results = new ArrayList<YearInterval>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			YearInterval sortedYearInterval 
				= (YearInterval) sorter.getItemFromIdentifier(identifier);
			results.add(sortedYearInterval);
		}
				
		return results;
	}
	
	public void identifyDifferences(
		final YearInterval anotherYearInterval,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherYearInterval, 
			differences);		
	}
		
	/**
	 * Checks for identical contents.
	 *
	 * @param otherYearInterval the other year interval
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final YearInterval otherYearInterval) {

		if (otherYearInterval == null) {
			return false;
		}
		
		Collator collator = Collator.getInstance();
		
		String otherStartYear = otherYearInterval.getStartYear();
		String otherEndYear = otherYearInterval.getEndYear();
		
		if (FieldValidationUtility.hasDifferentNullity(startYear, otherStartYear)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (startYear != null) {
			//they must both be non-null
			if (collator.equals(startYear, otherStartYear) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(endYear, otherEndYear)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (endYear != null) {
			//they must both be non-null
			if (collator.equals(endYear, otherEndYear) == false) {
				return false;
			}			
		}
		
		return super.hasIdenticalContents(otherYearInterval);
	}
    
// ==========================================
// Section Errors and Validation
// ==========================================

	

	@Override
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();
		
		String recordType = getRecordType();
        String startYearLabel
            = RIFServiceMessages.getMessage("yearInterval.startYear.label");
        String endYearLabel
            = RIFServiceMessages.getMessage("yearInterval.endYear.label");
		
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		fieldValidationUtility.checkMaliciousCode(
			recordType,			
			startYearLabel, 
			getStartYear());			
		fieldValidationUtility.checkMaliciousCode(
			recordType,			
			endYearLabel, 
			getEndYear());		
	}
	
	/**
	 * Compiles a list of error messages that describe any two intervals which either result
	 * in a gap or an overlap of years. Assumes all of the year interval objects are valid.
	 *
	 * @param yearIntervals the year intervals
	 * @return null if any of the year interval objects is invalid
	 * an empty list if no errors are detected
	 * a populated list of errors describing pairs that showed a gap or overlap
	 */
	public static ArrayList<String> checkGapsAndOverlaps(
		final ArrayList<YearInterval> yearIntervals) {

		ArrayList<String> errorMessages = new ArrayList<String>();
		
		if (yearIntervals.size() == 1) {
			return errorMessages;
		}
		
		ArrayList<YearInterval> sortedYearIntervals = new ArrayList<YearInterval>();
		
		//This algorithm seems inefficient.  But we anticipate that there will
		//not be many year intervals to sort because of the minimum realistic
		//year and because of the year interval size people will likely use.
		for (YearInterval yearInterval : yearIntervals) {
			int i;
			for (i = 0; i < sortedYearIntervals.size(); i++) {
				YearInterval currentSortedInterval
					= sortedYearIntervals.get(i);
				Boolean startsBefore = currentSortedInterval.startsBefore(yearInterval);
				if (startsBefore == null) {
					//this result means that one of the intervals has an invalid
					//integer value for a start or end year
					return null;
				}
				if (startsBefore) {
					sortedYearIntervals.add(i, yearInterval);
				}
			}
			if (i == sortedYearIntervals.size()) {
				//we have come to the end of the list
				sortedYearIntervals.add(yearInterval);
			}				
		}
		
		/**
		 * Sorted year intervals should now be sorted based on ascending order
		 * of start year.  We can assume here that all the intervals have
		 * valid integer values for start and end years.
		 */
		int firstIntervalIndex = 0;
		int secondIntervalIndex = 1;
		int maximumIndex = sortedYearIntervals.size() - 1;
		while (secondIntervalIndex <= maximumIndex) {
			YearInterval firstInterval 
				= sortedYearIntervals.get(firstIntervalIndex);
			YearInterval secondInterval 
				= sortedYearIntervals.get(secondIntervalIndex);
			
			int fitExtent = firstInterval.calculateFitExtent(secondInterval);

			if (fitExtent > 0) {
				//indicates a gap
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearInterval.error.gapExists",
						String.valueOf(Math.abs(fitExtent)),
						firstInterval.getDisplayName(),
						secondInterval.getDisplayName());
				errorMessages.add(errorMessage);					
			}
			else if (fitExtent < 0) {
				//indicates an overlap
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearInterval.error.overlapExists",
						String.valueOf(Math.abs(fitExtent)),
						firstInterval.getDisplayName(),
						secondInterval.getDisplayName());
				errorMessages.add(errorMessage);
			}
							
			//shift indices for first and second intervals on in the list
			firstIntervalIndex = secondIntervalIndex;
			secondIntervalIndex++;
		}
		
		return errorMessages;
	}
	
    /**
     * Makes the following checks:
     * <ul>
     * <li>ageCode is a two character non-empty value</li>
     * <li>startYear and endYear are non-negative integers</li>
     * <li>neither startYear nor endYear exceed a pre-defined maximum age</li>
     * <li>startYear is not greater than endYear</li>
     * </ul>.
     *
     * @throws RIFServiceException the RIF service exception
     */
    public void checkErrors(
    	final ValidationPolicy validationPolicy) 
    	throws RIFServiceException {

    	String recordType = getRecordType();	

        String startYearFieldName
            = RIFServiceMessages.getMessage("yearInterval.startYear.label");
        String endYearFieldName
            = RIFServiceMessages.getMessage("yearInterval.endYear.label");
				
        ArrayList<String> errorMessages = new ArrayList<String>();
        
		//check for nulls
		if (startYear == null) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					startYearFieldName);
			errorMessages.add(errorMessage);
		}
		
		if (endYear == null) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					endYearFieldName);
			errorMessages.add(errorMessage);
		}
		
		//If either of the bounds condition is null, there is no 
		//point in doing further error checking.
		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.INVALID_YEAR_INTERVAL,
					errorMessages);
			throw rifServiceException;
		}
		
		//Assumes that the bounds are both non-null
		
		//get the current year for validation checks later on
		int realisticHighestYear = YearRange.getMaximumRealisticYear();
		int realisticLowestYear = YearRange.getMinimumRealisticYear();
		
		Integer startYearValue = null;
		try {
			startYearValue = Integer.valueOf(startYear);
	
			if (startYearValue < realisticLowestYear) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearInterval.error.unrealisticlyLowYearValue",
						getDisplayName(),
						startYear,
						String.valueOf(realisticLowestYear));
				errorMessages.add(errorMessage);
			}
			else if (startYearValue > realisticHighestYear) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearInterval.error.unrealisticlyHighYearValue",
						getDisplayName(),
						startYear,
						String.valueOf(realisticHighestYear));
				errorMessages.add(errorMessage);				
			}
		}
		catch(NumberFormatException numberFormatException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.illegalInteger",
					startYear,
					recordType,
					startYearFieldName);
			errorMessages.add(errorMessage);
		}
		
		Integer endYearValue = null;
		try {
			endYearValue = Integer.valueOf(endYear);

			if (endYearValue < realisticLowestYear) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearInterval.error.unrealisticlyLowYearValue",
						getDisplayName(),
						endYear,
						String.valueOf(realisticLowestYear));
				errorMessages.add(errorMessage);
			}
			else if (endYearValue > realisticHighestYear) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearInterval.error.unrealisticlyHighYearValue",
						getDisplayName(),
						endYear,
						String.valueOf(realisticHighestYear));
				errorMessages.add(errorMessage);				
			}
		}
		catch(NumberFormatException numberFormatException) {			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.illegalInteger",
					endYear,
					recordType,
					endYearFieldName);
			errorMessages.add(errorMessage);
		}
		
		if ((startYearValue != null) && (endYearValue != null)) {			
			if (startYearValue > endYearValue) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"yearInterval.error.lowerStartYearGreaterThanEndYear",
						getDisplayName(),
						startYear,
						endYear);
				errorMessages.add(errorMessage);
			}
		}        
        countErrors(RIFServiceError.INVALID_YEAR_INTERVAL, errorMessages);
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
		buffer.append(startYear);
		buffer.append(" - ");
		buffer.append(endYear);
		buffer.append("]");
		return buffer.toString();
	}
	

	@Override
	public String getRecordType() {

		String recordTypeLabel
			= RIFServiceMessages.getMessage("yearInterval.label");
		return recordTypeLabel;
	}
	
}
