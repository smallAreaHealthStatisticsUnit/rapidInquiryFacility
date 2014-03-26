package rifServices.businessConceptLayer;


import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceSecurityException;
import rifServices.util.DisplayableItemSorter;
import rifServices.util.FieldValidationUtility;

import java.text.Collator;
import java.util.ArrayList;


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

public class Investigation 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The title. */
	private String title;
	
	/** The health theme. */
	private HealthTheme healthTheme;
	
	/** The nd pair. */
	private NumeratorDenominatorPair ndPair;
		
	/** The health codes. */
	private ArrayList<HealthCode> healthCodes;
	
	/** The age bands. */
	private ArrayList<AgeBand> ageBands;
	
	/** The sex. */
	private Sex sex;
	
	/** The year range. */
	private YearRange yearRange;
	
	/** The year intervals. */
	private ArrayList<YearInterval> yearIntervals;
	
	/** The interval. */
	private String interval;
	
	/** The covariates. */
	private ArrayList<AbstractCovariate> covariates;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new investigation.
	 */
	private Investigation() {

		title = "";
		healthTheme = HealthTheme.newInstance();
		ndPair = NumeratorDenominatorPair.newInstance();
		
		healthCodes = new ArrayList<HealthCode>();
		ageBands = new ArrayList<AgeBand>();
		sex = Sex.BOTH;
		yearIntervals = new ArrayList<YearInterval>();
		interval = "";
		covariates = new ArrayList<AbstractCovariate>();
	}

	/**
	 * New instance.
	 *
	 * @return the investigation
	 */
	public static Investigation newInstance() {

		return new Investigation();
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalInvestigations the original investigations
	 * @return the array list
	 */
	public static ArrayList<Investigation> createCopy(
		final ArrayList<Investigation> originalInvestigations) {
		
		ArrayList<Investigation> cloneInvestigations = new ArrayList<Investigation>();
		for (Investigation originalInvestigation : originalInvestigations) {
			cloneInvestigations.add(createCopy(originalInvestigation));
		}
		return cloneInvestigations;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalInvestigation the original investigation
	 * @return the investigation
	 */
	public static Investigation createCopy(
		final Investigation originalInvestigation) {
		
		Investigation cloneInvestigation = new Investigation();
		
		cloneInvestigation.setTitle(originalInvestigation.getTitle());
		HealthTheme cloneHealthTheme 
			= HealthTheme.createCopy(originalInvestigation.getHealthTheme());
		cloneInvestigation.setHealthTheme(cloneHealthTheme);
		NumeratorDenominatorPair
			cloneNDPair = NumeratorDenominatorPair.createCopy(originalInvestigation.getNdPair());
		cloneInvestigation.setNdPair(cloneNDPair);
		
		ArrayList<HealthCode> originalHealthCodes
			= originalInvestigation.getHealthCodes();
		ArrayList<HealthCode> cloneHealthCodes
			= HealthCode.createCopy(originalHealthCodes);
		cloneInvestigation.setHealthCodes(cloneHealthCodes);

		ArrayList<AgeBand> originalAgeBands
			= originalInvestigation.getAgeBands();
		ArrayList<AgeBand> cloneAgeBands
			= AgeBand.createCopy(originalAgeBands);
		cloneInvestigation.setAgeBands(cloneAgeBands);
		
		//Here we don't need to make an explicit copy because 
		//Gender is an enumerated type
		cloneInvestigation.setSex(originalInvestigation.getSex());

		YearRange cloneYearRange 
			= YearRange.createCopy(originalInvestigation.getYearRange());
		cloneInvestigation.setYearRange(cloneYearRange);
		
		ArrayList<YearInterval> cloneYearIntervals
			= YearInterval.createCopy(originalInvestigation.getYearIntervals());
		cloneInvestigation.setYearIntervals(cloneYearIntervals);
		
		cloneInvestigation.setInterval(originalInvestigation.getInterval());
		
		ArrayList<AbstractCovariate> originalCovariates
			= originalInvestigation.getCovariates();
		ArrayList<AbstractCovariate> cloneCovariates
			= CovariateUtility.copyCovariates(originalCovariates);
		cloneInvestigation.setCovariates(cloneCovariates);
		
		return cloneInvestigation;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {

		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(
		final String title) {

		this.title = title;
	}

	/**
	 * Gets the health theme.
	 *
	 * @return the health theme
	 */
	public HealthTheme getHealthTheme() {

		return healthTheme;
	}

	/**
	 * Sets the health theme.
	 *
	 * @param healthTheme the new health theme
	 */
	public void setHealthTheme(
		final HealthTheme healthTheme) {
		
		this.healthTheme = healthTheme;
	}

	/**
	 * Gets the nd pair.
	 *
	 * @return the nd pair
	 */
	public NumeratorDenominatorPair getNdPair() {
		
		return ndPair;
	}
	
	/**
	 * Sets the nd pair.
	 *
	 * @param ndPair the new nd pair
	 */
	public void setNdPair(
		final NumeratorDenominatorPair ndPair) {
		
		this.ndPair = ndPair;
	}

	/**
	 * Gets the health codes.
	 *
	 * @return the health codes
	 */
	public ArrayList<HealthCode> getHealthCodes() {
		
		return healthCodes;
	}

	/**
	 * Sets the health codes.
	 *
	 * @param healthCodes the new health codes
	 */
	public void setHealthCodes(
		final ArrayList<HealthCode> healthCodes) {
		
		this.healthCodes = healthCodes;
	}
	
	/**
	 * Adds the health code.
	 *
	 * @param healthCode the health code
	 */
	public void addHealthCode(
		final HealthCode healthCode) {
		
		healthCodes.add(healthCode);
	}

	/**
	 * Clear health codes.
	 */
	public void clearHealthCodes() {
		
		healthCodes.clear();
	}
	
	/**
	 * Sets the age bands.
	 *
	 * @param ageBands the new age bands
	 */
	public void setAgeBands(
		final ArrayList<AgeBand> ageBands) {
		
		this.ageBands = ageBands;
	}
	
	/**
	 * Adds the age bands.
	 *
	 * @param ageBands the age bands
	 */
	public void addAgeBands(
		final ArrayList<AgeBand> ageBands) {
		
		ageBands.addAll(ageBands);
	}
	
	/**
	 * Adds the age band.
	 *
	 * @param ageBand the age band
	 */
	public void addAgeBand(
		final AgeBand ageBand) {
		
		ageBands.add(ageBand);
	}
	
	/**
	 * Gets the age bands.
	 *
	 * @return the age bands
	 */
	public ArrayList<AgeBand> getAgeBands() {
		
		return ageBands;
	}
	
	/**
	 * Clear age bands.
	 */
	public void clearAgeBands() {
		
		ageBands.clear();
	}
	
	/**
	 * Gets the sex.
	 *
	 * @return the sex
	 */
	public Sex getSex() {
		
		return sex;
	}

	/**
	 * Sets the sex.
	 *
	 * @param sex the new sex
	 */
	public void setSex(
		final Sex sex) {
		
		this.sex = sex;
	}

	/**
	 * Gets the year range.
	 *
	 * @return the year range
	 */
	public YearRange getYearRange() {
		
		return yearRange;
	}

	/**
	 * Sets the year range.
	 *
	 * @param yearRange the new year range
	 */
	public void setYearRange(
		final YearRange yearRange) {
		
		this.yearRange = yearRange;
	}

	/**
	 * Gets the interval.
	 *
	 * @return the interval
	 */
	public String getInterval() {
		
		return interval;
	}

	/**
	 * Sets the interval.
	 *
	 * @param interval the new interval
	 */
	public void setInterval(
		final String interval) {
		
		this.interval = interval;
	}

	/**
	 * Gets the covariates.
	 *
	 * @return the covariates
	 */
	public ArrayList<AbstractCovariate> getCovariates() {
		
		return covariates;
	}

	/**
	 * Sets the covariates.
	 *
	 * @param covariates the new covariates
	 */
	public void setCovariates(
		final ArrayList<AbstractCovariate> covariates) {
		
		this.covariates = covariates;		
	}
	
	/**
	 * Adds the covariate.
	 *
	 * @param covariate the covariate
	 */
	public void addCovariate(
		final AbstractCovariate covariate) {
		
		covariates.add(covariate);
	}
	
	/**
	 * Clear covariates.
	 */
	public void clearCovariates() {
		
		covariates.clear();
	}
		
	/**
	 * Gets the year intervals.
	 *
	 * @return the year intervals
	 */
	public ArrayList<YearInterval> getYearIntervals() {
		
		return yearIntervals;
	}
	
	/**
	 * Sets the year intervals.
	 *
	 * @param yearIntervals the new year intervals
	 */
	public void setYearIntervals(
		final ArrayList<YearInterval> yearIntervals) {
		
		this.yearIntervals = yearIntervals;
	}
	
	/**
	 * Clear year intervals.
	 */
	public void clearYearIntervals() {
		
		yearIntervals.clear();
	}
	
	/**
	 * Adds the year interval.
	 *
	 * @param yearInterval the year interval
	 */
	public void addYearInterval(
		final YearInterval yearInterval) {
		
		yearIntervals.add(yearInterval);
	}

	/**
	 * Checks for identical contents.
	 *
	 * @param investigationListA the investigation list a
	 * @param investigationListB the investigation list b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<Investigation> investigationListA, 
		final ArrayList<Investigation> investigationListB) {

		if (FieldValidationUtility.hasDifferentNullity(
			investigationListA, 
			investigationListB)) {
			//reject if one is null and the other is non-null
			return false;
		}
			
		if (investigationListA.size() != investigationListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
			
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<Investigation> investigationsA = sortInvestigations(investigationListA);
		ArrayList<Investigation> investigationsB = sortInvestigations(investigationListB);
			
		int numberOfHealthCodes = investigationsA.size();
		for (int i = 0; i < numberOfHealthCodes; i++) {
			Investigation healthCodeA
				= investigationsA.get(i);				
			Investigation healthCodeB
				= investigationsB.get(i);
			if (healthCodeA.hasIdenticalContents(healthCodeB) == false) {					
				return false;
			}			
		}
			
		return true;
	}

	/**
	 * Sort investigations.
	 *
	 * @param investigations the investigations
	 * @return the array list
	 */
	private static ArrayList<Investigation> sortInvestigations(
		final ArrayList<Investigation> investigations) {

		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (Investigation investigation : investigations) {
			sorter.addDisplayableListItem(investigation);
		}
		
		ArrayList<Investigation> results = new ArrayList<Investigation>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			Investigation sortedInvestigation 
				= (Investigation) sorter.getItemFromIdentifier(identifier);
			results.add(sortedInvestigation);
		}
			
		return results;
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherInvestigation the other investigation
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final Investigation otherInvestigation) {
		
		if (otherInvestigation == null) {
			return false;
		}

		Collator collator = Collator.getInstance();

		String otherTitle = otherInvestigation.getTitle();
		HealthTheme otherHealthTheme = otherInvestigation.getHealthTheme();
		NumeratorDenominatorPair otherNDPair
			= otherInvestigation.getNdPair();
		ArrayList<HealthCode> otherHealthCodes
			= otherInvestigation.getHealthCodes();
		
		Sex otherSex = otherInvestigation.getSex();
		String otherInterval = otherInvestigation.getInterval();
		YearRange otherYearRange = otherInvestigation.getYearRange();
		ArrayList<YearInterval> otherYearIntervals
			= otherInvestigation.getYearIntervals();
		ArrayList<AbstractCovariate> otherCovariates
			= otherInvestigation.getCovariates();
		
		if (FieldValidationUtility.hasDifferentNullity(title, otherTitle)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (title != null) {
			//they must both be non-null
			if (collator.equals(title, otherTitle) == false) {
				return false;
			}			
		}
		
		if (healthTheme == null) {
			if (otherHealthTheme != null) {
				return false;
			}
		}
		else {
			if (healthTheme.hasIdenticalContents(otherHealthTheme) == false) {
				return false;
			}
		}

		if (ndPair == null) {
			if (otherNDPair != null) {
				return false;
			}
		}
		else {
			if (ndPair.hasIdenticalContents(otherNDPair) == false) {
				return false;
			}
		}

		if (HealthCode.hasIdenticalContents(healthCodes, otherHealthCodes) == false) {
			return false;
		}
		
		ArrayList<AgeBand> otherAgeBands
			= otherInvestigation.getAgeBands();
		if (AgeBand.hasIdenticalContents(ageBands, otherAgeBands) == false) {
			return false;
		}

		
		if (sex == null) {
			if (otherSex != null) {
				return false;
			}
		}
		else if (otherSex == null) {
			return false;
		}
		else if (sex != otherSex) {
			return false;
		}
				
		if (HealthCode.hasIdenticalContents(healthCodes, otherHealthCodes) == false) {
			return false;
		}

		if (yearRange == null) {
			if (otherYearRange != null) {
				return false;
			}
		}
		else {
			if (yearRange.hasIdenticalContents(otherYearRange) == false) {
				return false;
			}
		}
		
		if (YearInterval.hasIdenticalContents(yearIntervals, otherYearIntervals) == false) {
			return false;
		}

		if (CovariateUtility.hasIdenticalContents(covariates, otherCovariates) == false) {
			return false;
		}

				
		if (FieldValidationUtility.hasDifferentNullity(interval, otherInterval)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (interval != null) {
			//they must both be non-null
			if (collator.equals(interval, otherInterval) == false) {
				return false;
			}			
		}
		
		
		return super.hasIdenticalContents(otherInvestigation);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#checkSecurityViolations()
	 */
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType = getRecordType();
		if (interval != null) {
			String intervalFieldName
				= RIFServiceMessages.getMessage("investigation.interval.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				intervalFieldName,
				interval);
		}			
		
		if (title != null) {
			String titleFieldName
				= RIFServiceMessages.getMessage("investigation.title.label");
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				titleFieldName,
				title);
		}
		
		if (healthTheme != null) {
			healthTheme.checkSecurityViolations();
		}

		if (ndPair != null) {
			ndPair.checkSecurityViolations();
		}
		
		if (healthCodes != null) {
			for (HealthCode healthCode : healthCodes) {
				healthCode.checkSecurityViolations();
			}
		}
		
		if (ageBands != null) {
			for (AgeBand ageBand : ageBands) {
				ageBand.checkSecurityViolations();
			}
		}

		//Sex is an enumerated type so needs no checking

		if (yearRange != null) {
			yearRange.checkSecurityViolations();
		}

		if (yearIntervals != null) {
			for (YearInterval yearInterval : yearIntervals) {
				yearInterval.checkSecurityViolations();
			}
		}
		
		if (covariates != null) {
			for (AbstractCovariate covariate : covariates) {
				covariate.checkSecurityViolations();
			}		
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#checkErrors()
	 */
	public void checkErrors() 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordType = getRecordType();
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(title)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					"investigation.title.label");
			errorMessages.add(errorMessage);
		}
		
		if (healthTheme == null) {
			String healthTheme
				= RIFServiceMessages.getMessage("healthTheme.label");
			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					healthTheme);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				healthTheme.checkErrors();
			}
			catch(RIFServiceException rifServiceException) {
				errorMessages.addAll(rifServiceException.getErrorMessages());
			}			
		}

		if (ndPair == null) {
			String ndPairLabel
				= RIFServiceMessages.getMessage("numeratorDenominatorPair.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					ndPairLabel);			
			errorMessages.add(errorMessage);
		}
		else {
			try {
				ndPair.checkErrors();
			}
			catch(RIFServiceException rifServiceException) {
				errorMessages.addAll(rifServiceException.getErrorMessages());
			}			
		}

		if (healthCodes == null) {
			String healthCodesFieldName
				= RIFServiceMessages.getMessage("healthCode.plural.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					healthCodesFieldName);
			errorMessages.add(errorMessage);
		}
		else if (healthCodes.size() == 0) {
			String errorMessage
				= RIFServiceMessages.getMessage("investigation.error.noHealthCodesSpecified");
			errorMessages.add(errorMessage);			
		}
		else {
			for (HealthCode healthCode : healthCodes) {
				try {
					healthCode.checkErrors();
				}
				catch(RIFServiceException rifServiceException) {
					errorMessages.addAll(rifServiceException.getErrorMessages());					
				}
			}
		}
		
		if (ageBands == null) {
			String ageBandsFieldName
				= RIFServiceMessages.getMessage("ageBand.plural.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					ageBandsFieldName);
			errorMessages.add(errorMessage);
		}
		else if (ageBands.size() == 0) {
			String errorMessage
				= RIFServiceMessages.getMessage("investigation.error.noAgeBandsSpecified");
			errorMessages.add(errorMessage);			
		}
		else {
			
			boolean invalidAgeBandsDetected = false;
			for (AgeBand ageBand : ageBands) {
				try {
					ageBand.checkErrors();
				}
				catch(RIFServiceException rifServiceException) {
					errorMessages.addAll(rifServiceException.getErrorMessages());
					invalidAgeBandsDetected = true;
				}
			}
			
			//now check there are no overlapping age bands
			if (invalidAgeBandsDetected == false) {
				errorMessages.addAll(AgeBand.checkGapsAndOverlaps(ageBands));				
			}	
		}
				
		if (sex == null) {
			String sexFieldName
				= RIFServiceMessages.getMessage("sex.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					sexFieldName);
			errorMessages.add(errorMessage);
		}
		
		if (yearRange == null) {
			String yearRangeFieldName
				= RIFServiceMessages.getMessage("yearRange.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					yearRangeFieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				yearRange.checkErrors();
			}
			catch(RIFServiceException rifServiceException) {
				errorMessages.addAll(rifServiceException.getErrorMessages());
			}
		
		}
		
		if (yearIntervals == null) {
			String yearIntervalsFieldName
				= RIFServiceMessages.getMessage("yearInterval.plural.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					yearIntervalsFieldName);
			errorMessages.add(errorMessage);
		}
		else if (yearIntervals.size() == 0) {
			String errorMessage
				= RIFServiceMessages.getMessage("investigation.error.noYearIntervalsSpecified");
			errorMessages.add(errorMessage);
		}
		else {
			boolean allYearIntervalsAreValid = true;
			for (YearInterval yearInterval : yearIntervals) {
				try {
					yearInterval.checkErrors();					
				}
				catch(RIFServiceException rifServiceException) {
					errorMessages.addAll(rifServiceException.getErrorMessages());
					allYearIntervalsAreValid = false;
				}
			}
			
			//now check that there are no duplicate or overlapping year intervals
			//but only bother doing this if all the year interval values are valid
			if (allYearIntervalsAreValid == true) {
				ArrayList<String> gapAndOverlapErrorMessages
					= YearInterval.checkGapsAndOverlaps(yearIntervals);			
				errorMessages.addAll(gapAndOverlapErrorMessages);			
			}
		}

		//Interval can be none.  It just means that all the years will be included
		//together
		Collator collator = RIFServiceMessages.getCollator();
		String noneChoice
			= RIFServiceMessages.getMessage("general.choices.none");
		if ((fieldValidationUtility.isEmpty(interval) == false) &&
			collator.equals(interval, noneChoice) == false) {
			try {
				Integer.valueOf(interval);	
			}
			catch(NumberFormatException numberFormatException) {
				String intervalFieldName
					= RIFServiceMessages.getMessage("investigation.interval.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.illegalPositiveNumber",
						interval,
						recordType,
						intervalFieldName);
				errorMessages.add(errorMessage);
			}
		}
		
		if (covariates == null) {
			String covariatesFieldName
				= RIFServiceMessages.getMessage("investigation.covariates.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					covariatesFieldName);
			errorMessages.add(errorMessage);			
		}
		else if (covariates.size() == 0) {
			String errorMessage
				= RIFServiceMessages.getMessage("investigation.error.noCovariatesSpecified");
			errorMessages.add(errorMessage);			
		}
		else {
			for (AbstractCovariate covariate : covariates) {
				try {
					covariate.checkErrors();
				}
				catch(RIFServiceException rifServiceException) {
					errorMessages.addAll(rifServiceException.getErrorMessages());					
				}
			}			
		}

		countErrors(RIFServiceError.INVALID_INVESTIGATION, errorMessages);
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
	


	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		
		return title;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#getRecordType()
	 */
	@Override
	public String getRecordType() {
		
		String recordType
			= RIFServiceMessages.getMessage("investigation.label");
		return recordType;
	}
}
