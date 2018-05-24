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

public final class AgeBand 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	private Messages GENERIC_MESSAGES = Messages.genericMessages();

	// ==========================================
	// Section Properties
	// ==========================================
	/** The lower limit age group. */
	private AgeGroup lowerLimitAgeGroup;
	
	/** The upper limit age group. */
	private AgeGroup upperLimitAgeGroup;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new age band.
	 */
	private AgeBand() {
		
		lowerLimitAgeGroup = AgeGroup.newInstance();
		upperLimitAgeGroup = AgeGroup.newInstance();
	}

	
	/**
	 * New instance.
	 *
	 * @return the age band
	 */
	public static AgeBand newInstance() {
		
		AgeBand ageBand = new AgeBand();
		return ageBand;
	}
	
	/**
	 * New instance.
	 *
	 * @param lowerLimitAgeGroup the lower limit age group
	 * @param upperLimitAgeGroup the upper limit age group
	 * @return the age band
	 */
	public static AgeBand newInstance(
		final AgeGroup lowerLimitAgeGroup,
		final AgeGroup upperLimitAgeGroup) {
		
		AgeBand ageBand = AgeBand.newInstance();
		ageBand.setLowerLimitAgeGroup(lowerLimitAgeGroup);
		ageBand.setUpperLimitAgeGroup(upperLimitAgeGroup);
		
		return ageBand;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalAgeBand the original age band
	 * @return the age band
	 */
	public static AgeBand createCopy(
		final AgeBand originalAgeBand) {
		
		if (originalAgeBand == null) {
			return null;
		}
		
		AgeBand cloneAgeGroup = new AgeBand();
		cloneAgeGroup.setIdentifier(originalAgeBand.getIdentifier());
		cloneAgeGroup.setLowerLimitAgeGroup(originalAgeBand.getLowerLimitAgeGroup());
		cloneAgeGroup.setUpperLimitAgeGroup(originalAgeBand.getUpperLimitAgeGroup());
		
		return cloneAgeGroup;
	}
		
	/**
	 * Creates the copy.
	 *
	 * @param originalAgeBands the original age bands
	 * @return the array list
	 */
	public static ArrayList<AgeBand> createCopy(
		final ArrayList<AgeBand> originalAgeBands) {
		
		ArrayList<AgeBand> cloneAgeBands = new ArrayList<AgeBand>();
		
		for (AgeBand originalAgeBand : originalAgeBands) {
			AgeBand cloneAgeBand
				= AgeBand.createCopy(originalAgeBand);
			cloneAgeBands.add(cloneAgeBand);
		}

		return cloneAgeBands;		
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the lower limit age group.
	 *
	 * @return the lower limit age group
	 */
	public AgeGroup getLowerLimitAgeGroup() {
		
		return lowerLimitAgeGroup;
	}

	/**
	 * Sets the lower limit age group.
	 *
	 * @param lowerLimitAgeGroup the new lower limit age group
	 */
	public void setLowerLimitAgeGroup(
		final AgeGroup lowerLimitAgeGroup) {
		
		this.lowerLimitAgeGroup = lowerLimitAgeGroup;
	}

	/**
	 * Gets the upper limit age group.
	 *
	 * @return the upper limit age group
	 */
	public AgeGroup getUpperLimitAgeGroup() {
		
		return upperLimitAgeGroup;
	}

	/**
	 * Sets the upper limit age group.
	 *
	 * @param upperLimitAgeGroup the new upper limit age group
	 */
	public void setUpperLimitAgeGroup(
		final AgeGroup upperLimitAgeGroup) {
		
		this.upperLimitAgeGroup = upperLimitAgeGroup;
	}

	
	public void identifyDifferences(
		final AgeBand anotherAgeBand,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherAgeBand, 
			differences);
		

	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param ageBandListA the age band list a
	 * @param ageBandListB the age band list b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<AgeBand> ageBandListA,
		final ArrayList<AgeBand> ageBandListB) {
		
		if (FieldValidationUtility.hasDifferentNullity(
			ageBandListA, 
			ageBandListB)) {
			//reject if one is null and the other is non-null
			return false;
		}
				
		if (ageBandListA.size() != ageBandListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
				
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<AgeBand> ageBandsA = sortAgeBands(ageBandListA);
		ArrayList<AgeBand> ageBandsB = sortAgeBands(ageBandListB);
				
			int numberOfHealthCodes = ageBandsA.size();
			for (int i = 0; i < numberOfHealthCodes; i++) {
				AgeBand ageBandA
					= ageBandsA.get(i);				
				AgeBand ageBandB
					= ageBandsB.get(i);
				if (ageBandA.hasIdenticalContents(ageBandB) == false) {			
					return false;
				}			
			}
				
			return true;
	}

	/**
	 * Sort age bands.
	 *
	 * @param ageBands the age bands
	 * @return the array list
	 */
	public static ArrayList<AgeBand> sortAgeBands(
		final ArrayList<AgeBand> ageBands) {
		
		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (AgeBand ageBand : ageBands) {
			sorter.addDisplayableListItem(ageBand);
		}
		
		ArrayList<AgeBand> results = new ArrayList<AgeBand>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			AgeBand sortedAgeBand 
				= (AgeBand) sorter.getItemFromIdentifier(identifier);
			results.add(sortedAgeBand);
		}
		
		return results;
	}
	
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherAgeBand the other age band
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final AgeBand otherAgeBand) {
		
		if (otherAgeBand == null) {
			return false;
		}
		
		AgeGroup otherLowerLimitAgeGroup 
			= otherAgeBand.getLowerLimitAgeGroup();
		AgeGroup otherUpperLimitAgeGroup
			= otherAgeBand.getUpperLimitAgeGroup();
		
		Collator collator = Collator.getInstance();
		if (FieldValidationUtility.hasDifferentNullity(
			lowerLimitAgeGroup, 
			otherLowerLimitAgeGroup)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (lowerLimitAgeGroup != null) {
			//they must both be non-null
			if (collator.equals(
				lowerLimitAgeGroup.getDisplayName(), 
				otherLowerLimitAgeGroup.getDisplayName()) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(
			upperLimitAgeGroup.getDisplayName(), 
			otherUpperLimitAgeGroup.getDisplayName())) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (upperLimitAgeGroup != null) {
			//they must both be non-null
			if (collator.equals(
				upperLimitAgeGroup.getDisplayName(), 
				otherUpperLimitAgeGroup.getDisplayName()) == false) {
				return false;
			}			
		}
				
		return super.hasIdenticalContents(otherAgeBand);		
	}
	
	
	/**
	 * Check gaps and overlaps.
	 *
	 * @param ageBands the age bands
	 * @return the array list
	 */
	public static ArrayList<String> checkGapsAndOverlaps(
		final ArrayList<AgeBand> ageBands) {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		if (ageBands.size() == 1) {
			//no errors if there is just one age band
			return errorMessages;
		}
		
		ArrayList<AgeBand> sortedAgeBands = new ArrayList<AgeBand>();
		
		//This algorithm seems inefficient.  But we anticipate that there will
		//not be many year intervals to sort because of the minimum realistic
		//year and because of the year interval size people will likely use.
		for (AgeBand ageBand : ageBands) {
			int i;
			for (i = 0; i < sortedAgeBands.size(); i++) {
				AgeBand currentSortedAgeBand
					= sortedAgeBands.get(i);
				Boolean startsBefore = currentSortedAgeBand.startsBefore(ageBand);
				if (startsBefore == null) {
					//this result means that one of the intervals has an invalid
					//integer value for a start or end year
					String errorMessage
						= RIFServiceMessages.getMessage(
							"ageBand.error.invalidLimits",
							currentSortedAgeBand.getDisplayName(),
							ageBand.getDisplayName());
					errorMessages.add(errorMessage);
					break;
				}
				if (startsBefore) {
					sortedAgeBands.add(i, ageBand);
					break;
				}
			}
			if (i == sortedAgeBands.size()) {
				//we have come to the end of the list
				sortedAgeBands.add(ageBand);
			}				
		}
		
		/*
		 * If there are any errors accumulated so far, then there will
		 * be at least one interval which has a lower or upper limit that is
		 * not valid.  In this case there is no point doing further
		 * validation checks
		 */
		if (errorMessages.size() > 0) {
			return errorMessages;
		}
		
		/**
		 * Sorted year intervals should now be sorted based on ascending order
		 * of start year.  We can assume here that all the intervals have
		 * valid integer values for start and end years.
		 */
		int firstIntervalIndex = 0;
		int secondIntervalIndex = 1;
		int maximumIndex = sortedAgeBands.size() - 1;
		while (secondIntervalIndex <= maximumIndex) {
			AgeBand firstAgeBand
				= sortedAgeBands.get(firstIntervalIndex);
			AgeBand secondAgeBand
				= sortedAgeBands.get(secondIntervalIndex);
			
			int fitExtent = firstAgeBand.calculateFitExtent(secondAgeBand);

			if (fitExtent > 0) {
				//indicates a gap
				String errorMessage
					= RIFServiceMessages.getMessage(
						"ageBand.error.gapExists",
						String.valueOf(Math.abs(fitExtent)),
						firstAgeBand.getDisplayName(),
						secondAgeBand.getDisplayName());
				errorMessages.add(errorMessage);					
			}
			else if (fitExtent < 0) {
				//indicates an overlap
				String errorMessage
					= RIFServiceMessages.getMessage(
						"ageBand.error.overlapExists",
						String.valueOf(Math.abs(fitExtent)),
						firstAgeBand.getDisplayName(),
						secondAgeBand.getDisplayName());
				errorMessages.add(errorMessage);
			}
							
			//shift indices for first and second intervals on in the list
			firstIntervalIndex = secondIntervalIndex;
			secondIntervalIndex++;
		}
		
		return errorMessages;		
	}

    /**
     * Assumes that both age bands involved are valid.
     *
     * @param ageBand the age band
     * @return whether the parameter
     * not legal integers
     */
    public Boolean startsBefore(
    	final AgeBand ageBand) {
    	
    	try {
    		String lowerLimitPhraseA = lowerLimitAgeGroup.getLowerLimit();
    		int lowerLimitA = Integer.valueOf(lowerLimitPhraseA);
    		String lowerLimitPhraseB = ageBand.getLowerLimitAgeGroup().getLowerLimit();  		
    		int lowerLimitB = Integer.valueOf(lowerLimitPhraseB);
    		
    		if (lowerLimitA < lowerLimitB) {
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
     * @param ageBand the age band
     * @return null if any of the integer values are invalid
     * positive number indicating gaps
     * negative numbers indicating overlaps
     */
    public Integer calculateFitExtent(
    	final AgeBand ageBand) {
    	
    	try {
    		int startYearA 
    			= Integer.valueOf(lowerLimitAgeGroup.getLowerLimit());
    		int endYearA = Integer.valueOf(upperLimitAgeGroup.getUpperLimit());
    		
    		int startYearB 
    			= Integer.valueOf(ageBand.getLowerLimitAgeGroup().getLowerLimit());
    		int endYearB 
    			= Integer.valueOf(ageBand.getUpperLimitAgeGroup().getUpperLimit());

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
	
    
    
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {			
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordType
			= RIFServiceMessages.getMessage("ageBand.label");

		if (validationPolicy == ValidationPolicy.STRICT) {

			//Test whether lower limit and upper limit are numbers
			boolean bothLimitsAreValid = true;
			String lowerLimitFieldName
				= RIFServiceMessages.getMessage("ageBand.lowerLimit.label");
			if (lowerLimitAgeGroup == null) {
				bothLimitsAreValid = false;
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordType,
						lowerLimitFieldName);
				errorMessages.add(errorMessage);
			}
			else { 
				try {
					lowerLimitAgeGroup.checkErrors(validationPolicy);
				}
				catch(RIFServiceException rifServiceException) {
					bothLimitsAreValid = false;
					errorMessages.addAll(rifServiceException.getErrorMessages());
				}
			}

			String upperLimitFieldName
				= RIFServiceMessages.getMessage("ageBand.lowerLimit.label");
			if (upperLimitAgeGroup == null) {
				bothLimitsAreValid = false;
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordType,
						upperLimitFieldName);
				errorMessages.add(errorMessage);
			}
			else {
				try {
					upperLimitAgeGroup.checkErrors(validationPolicy);
				}
				catch(RIFServiceException rifServiceException) {
					bothLimitsAreValid = false;
					errorMessages.addAll(rifServiceException.getErrorMessages());
				}
			}
		
		
			//Now check that the lower limit of the age group is not higher than the
			//upper limit of the upper age group
		
			String lowerLimitNumberFieldValue = lowerLimitAgeGroup.getLowerLimit();		
			String upperLimitNumberFieldValue = upperLimitAgeGroup.getUpperLimit();

			if (bothLimitsAreValid == true) {
				try {
					Integer lowerLimitNumber = Integer.valueOf(lowerLimitNumberFieldValue);
					Integer upperLimitNumber = Integer.valueOf(upperLimitNumberFieldValue);

					if ((lowerLimitNumber != null) && (upperLimitNumber) != null) {
						if (lowerLimitNumber.intValue() > upperLimitNumber.intValue()) {
							String errorMessage
								= RIFServiceMessages.getMessage(
									"ageBand.error.lowerGreaterthanUpperLimit",
									lowerLimitNumberFieldValue,
									upperLimitNumberFieldValue);
							errorMessages.add(errorMessage);
						}			
					}			
				}
				catch(Exception exception) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"ageBand.error.invalidAgeGroupValues",
							lowerLimitNumberFieldValue,
							upperLimitNumberFieldValue);
					errorMessages.add(errorMessage);
				}
			}
		}

		countErrors(RIFServiceError.INVALID_AGE_GROUP, errorMessages);
	}
	

	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();

		lowerLimitAgeGroup.checkSecurityViolations();
		upperLimitAgeGroup.checkSecurityViolations();		
	}
	

	@Override
	public String getDisplayName() {
		
		StringBuilder buffer = new StringBuilder();
				
		String lowerLimitFieldValue
			= lowerLimitAgeGroup.getLowerLimit();
		if (lowerLimitFieldValue != null) {
			buffer.append(lowerLimitFieldValue);	
		}

		buffer.append("-");

		String upperLimitFieldValue
			= upperLimitAgeGroup.getUpperLimit();
		if (upperLimitFieldValue != null) {
			buffer.append(upperLimitFieldValue);			
		}
		
		return buffer.toString();
	}
	
	/**
	 * Gets the display name.
	 *
	 * @param useLowerLimit the use lower limit
	 * @return the display name
	 */
	public String getDisplayName(
		final boolean useLowerLimit) {
		
		StringBuilder buffer = new StringBuilder();
		
		if (useLowerLimit == true) {
			String lowerLimitFieldValue
				= lowerLimitAgeGroup.getLowerLimit();
			if (lowerLimitFieldValue != null) {
				buffer.append(lowerLimitFieldValue);	
			}
		}
		else {
			//use upper limit
			String upperLimitFieldValue
				= upperLimitAgeGroup.getUpperLimit();
			if (upperLimitFieldValue != null) {
				buffer.append(upperLimitFieldValue);			
			}
		}
		
		return buffer.toString();
	}
	

	@Override
	public String getRecordType() {
		
		String recordType
			= RIFServiceMessages.getMessage("ageBand.label");
		return recordType;
	}

}
