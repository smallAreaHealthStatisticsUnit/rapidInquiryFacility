package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.generic.util.RIFComparisonUtility;
import org.sahsu.rif.generic.util.RIFLogger;
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

public class Investigation 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	/** The title. */
	private String title;
	
	
	/** The description */
	private String description;
	
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
		description = "";
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
		cloneInvestigation.setDescription(originalInvestigation.getDescription());
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
	 * Sets the description
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the description
	 * @return
	 */
	public String getDescription() {
		return description;
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
	 * @TODO
	 * the minimum age group of the minimum Age Band
	 * @return
	 */
	public AgeGroup getMinimumAgeGroup() {
		
		ArrayList<AgeBand> sortedAgeBands
			= AgeBand.sortAgeBands(ageBands);
		if (sortedAgeBands.isEmpty()) {
			return null;
		}
		
		AgeBand lowestAgeBand = sortedAgeBands.get(0);
		return lowestAgeBand.getLowerLimitAgeGroup();
	}	
	
	/**
	 * @TODO
	 * the maximum age group of the maximum Age Band
	 * @return
	 */
	public AgeGroup getMaximumAgeGroup() {

		ArrayList<AgeBand> sortedAgeBands
			= AgeBand.sortAgeBands(ageBands);
		if (sortedAgeBands.isEmpty()) {
			return null;
		}
	
		AgeBand highestAgeBand 
			= sortedAgeBands.get(sortedAgeBands.size() - 1);
		return highestAgeBand.getUpperLimitAgeGroup();		
	}
	

	public void identifyDifferences(
		final Investigation anotherInvestigation,
		final ArrayList<String> differences) {


		if (RIFComparisonUtility.identifyNullityDifferences(
			this, 
			anotherInvestigation, 
			differences)) {
			
			return;
		}
		
		super.identifyDifferences(
			anotherInvestigation, 
			differences);
		
		
		RIFComparisonUtility.identifyDifferences(
			"investigation.title.label", 
			this, 
			this.getTitle(), 
			anotherInvestigation, 
			anotherInvestigation.getTitle(), 
			differences);
		
		
		RIFComparisonUtility.identifyDifferences(
			"investigation.description.label", 
			this, 
			this.getDescription(), 
			anotherInvestigation, 
			anotherInvestigation.getDescription(), 
			differences);
		
		
		RIFComparisonUtility.identifyDifferences(
			"investigation.interval.label", 
			this, 
			this.getInterval(), 
			anotherInvestigation, 
			anotherInvestigation.getInterval(), 
			differences);

		//compare sex values
		Sex anotherSex = anotherInvestigation.getSex();		
		if (!RIFComparisonUtility.identifyFieldNullityDifferences(
						"sex.label",
						this,
						sex,
						anotherInvestigation,
						anotherInvestigation.getSex(),
						differences)) {
			
			
			if (sex != anotherSex) {
				
				String sexFieldName
					= RIFServiceMessages.getMessage("sex.label");
				String difference
					= RIFServiceMessages.getMessage(
						"differences.fieldsDiffer",
						sexFieldName,
						this.getDisplayName(),
						sex.getName(),
						anotherInvestigation.getDisplayName(),
						anotherSex.getName());
				differences.add(difference);
			}
		}
		
		//compare health theme values
		if (!RIFComparisonUtility.identifyFieldNullityDifferences(
						"healthTheme.label",
						this,
						healthTheme,
						anotherInvestigation,
						anotherInvestigation.getHealthTheme(),
						differences)) {

			if (healthTheme != null) {
				
				//we're guaranteed that the other health theme will not be null
				healthTheme.identifyDifferences(
					anotherInvestigation.getHealthTheme(), 
					differences);
			}
		}


		//@TODO: more to do!
		
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
			Investigation investigationA
				= investigationsA.get(i);				
			Investigation investigationB
				= investigationsB.get(i);
			if (!investigationA.hasIdenticalContents(investigationB)) {
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
		String otherDescription = otherInvestigation.getDescription();
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
			if (!collator.equals(title, otherTitle)) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(description, otherDescription)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (description != null) {
			//they must both be non-null
			if (!collator.equals(description, otherDescription)) {
				return false;
			}			
		}
		
		
		if (healthTheme == null) {
			if (otherHealthTheme != null) {
				return false;
			}
		}
		else {
			if (!healthTheme.hasIdenticalContents(otherHealthTheme)) {
				return false;
			}
		}

		if (ndPair == null) {
			if (otherNDPair != null) {
				return false;
			}
		}
		else {
			if (!ndPair.hasIdenticalContents(otherNDPair)) {
				return false;
			}
		}

		if (!HealthCode.hasIdenticalContents(healthCodes, otherHealthCodes)) {
			return false;
		}
		
		ArrayList<AgeBand> otherAgeBands
			= otherInvestigation.getAgeBands();
		if (!AgeBand.hasIdenticalContents(ageBands, otherAgeBands)) {
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
				
		if (!HealthCode.hasIdenticalContents(healthCodes, otherHealthCodes)) {
			return false;
		}

		if (yearRange == null) {
			if (otherYearRange != null) {
				return false;
			}
		}
		else {
			if (!yearRange.hasIdenticalContents(otherYearRange)) {
				return false;
			}
		}
		
		if (!YearInterval.hasIdenticalContents(yearIntervals, otherYearIntervals)) {
			return false;
		}

		if (!CovariateUtility.hasIdenticalContents(covariates, otherCovariates)) {
			return false;
		}

				
		if (FieldValidationUtility.hasDifferentNullity(interval, otherInterval)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (interval != null) {
			//they must both be non-null
			if (!collator.equals(interval, otherInterval)) {
				return false;
			}			
		}
		
		
		return super.hasIdenticalContents(otherInvestigation);
	}
	
	public ArrayList<String> getDifferencesInCovariates(
		final Investigation otherInvestigation) {

		ArrayList<String> covariateNames = new ArrayList<String>();
		for (AbstractCovariate covariate : covariates) {
			covariateNames.add(covariate.getName());
		}
		
		ArrayList<AbstractCovariate> otherCovariates
			= otherInvestigation.getCovariates();
	
		String otherInvestigationTitle = otherInvestigation.getTitle();
		ArrayList<String> otherCovariateNames = new ArrayList<String>();
		for (AbstractCovariate otherCovariate : otherCovariates) {
			otherCovariateNames.add(otherCovariate.getName());			
		}
	
		ArrayList<String> differenceMessages = new ArrayList<String>();
		
		/**
		 * Covariates that are in this investigation but not the other investigation
		 */
		for (String covariateName : covariateNames) {
			if (!otherCovariateNames.contains(covariateName)) {
				String differenceMessage
					= RIFServiceMessages.getMessage(
						"investigation.covariateDifferences",
						covariateName,
						title,
						otherInvestigationTitle);
				differenceMessages.add(differenceMessage);
			}
		}
		
		/**
		 * Covariates that are in the other investigation but not in this one
		 */
		for (String otherCovariateName : otherCovariateNames) {
			if (!covariateNames.contains(otherCovariateName)) {
				String differenceMessage
					= RIFServiceMessages.getMessage(
						"investigation.covariateDifferences",
						otherCovariateName,
						otherInvestigationTitle,
						title);
				differenceMessages.add(differenceMessage);
			}
		}
		
		return differenceMessages;
	}	
		
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

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
		
		if (description != null) {
			String titleFieldName
				= RIFServiceMessages.getMessage("investigation.description.label");
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				titleFieldName,
				description);
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
	

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordType = getRecordType();
		
		
		//TOUR_VALIDATION
		//In many cases, we're just checking whether a field has been left null or blank
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(title)) {
			String titleFieldName
				= RIFServiceMessages.getMessage("investigation.title.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					titleFieldName);
			errorMessages.add(errorMessage);
		}
		else {
			//TOUR_VALIDATION
			//Ensuring that the title can be converted into something that serves as the 
			//table name for the investigation
			
			if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9_ ]{0,19}$", title)) {
				String errorMessage
				= RIFServiceMessages.getMessage(
					"investigation.error.titleContainsIllegalCharacters",
					title);
				errorMessages.add(errorMessage);
			}
		}
		
		/**
		 * Allow the description field to be empty
		 */
		
		if (healthTheme == null) {
			String healthTheme
				= RIFServiceMessages.getMessage("healthTheme.label");
			
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					healthTheme);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				healthTheme.checkErrors(validationPolicy);
			}
			catch(RIFServiceException rifServiceException) {
				rifLogger.info(this.getClass(), "HealthTheme.checkErrors(): " + 
					rifServiceException.getErrorMessages().size());
				errorMessages.addAll(rifServiceException.getErrorMessages());
			}			
		}

		//TOUR_VALIDATION
		//Here, we check whether an object the investigation owns is null or not.
		if (ndPair == null) {
			String ndPairLabel
				= RIFServiceMessages.getMessage("numeratorDenominatorPair.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					ndPairLabel);			
			errorMessages.add(errorMessage);
		}
		else {
			try {
				//TOUR_VALIDATION
				//As Investigation calls the checkErrors() methods of the objects
				//it manages, it gathers the error messages. At the end, all of these
				//errors will be associated with an error type of INVALID_INVESTIGATION,
				//rather than what it might otherwise be here - INVALID_NUMERATOR_DENOMINATOR_PAIR
				ndPair.checkErrors(
					validationPolicy);
			}
			catch(RIFServiceException rifServiceException) {
				rifLogger.info(this.getClass(), "NumeratorDenominatorPair.checkErrors(): " + 
					rifServiceException.getErrorMessages().size());
				errorMessages.addAll(rifServiceException.getErrorMessages());
			}			
		}

		if (healthCodes == null) {
			String healthCodesFieldName
				= RIFServiceMessages.getMessage("healthCode.plural.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					healthCodesFieldName);
			errorMessages.add(errorMessage);
		}
		else if (healthCodes.isEmpty()) {
			//TOUR_VALIDATION
			//Example of checking when a collection is empty
			String errorMessage
				= RIFServiceMessages.getMessage("investigation.error.noHealthCodesSpecified");
			errorMessages.add(errorMessage);			
		}
		else {
			for (HealthCode healthCode : healthCodes) {
				if (healthCode == null) {
					String healthCodeRecordType
						= RIFServiceMessages.getMessage("healthCode.label");
					String errorMessage
						= RIFServiceMessages.getMessage(
							"general.validation.nullListItem",
							getRecordType(),
							healthCodeRecordType);
					errorMessages.add(errorMessage);
				}
				else {
					try {
						healthCode.checkErrors(validationPolicy);
					}
					catch(RIFServiceException rifServiceException) {
						rifLogger.info(this.getClass(), "HealthCode.checkErrors(): " + 
							rifServiceException.getErrorMessages().size());
						errorMessages.addAll(rifServiceException.getErrorMessages());					
					}
				}

			}
		}
		
		if (ageBands == null) {
			String ageBandsFieldName
				= RIFServiceMessages.getMessage("ageBand.plural.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					ageBandsFieldName);
			errorMessages.add(errorMessage);
		}
		else if (ageBands.isEmpty()) {
			String errorMessage
				= RIFServiceMessages.getMessage("investigation.error.noAgeBandsSpecified");
			errorMessages.add(errorMessage);			
		}
		else {
			
			boolean invalidAgeBandsDetected = false;
			for (AgeBand ageBand : ageBands) {
				if (ageBand == null) {
					String ageBandRecordType
						= RIFServiceMessages.getMessage("ageBand.label");
					String errorMessage
						= RIFServiceMessages.getMessage(
							"general.validation.nullListItem",
							getRecordType(),
							ageBandRecordType);
					errorMessages.add(errorMessage);				
					invalidAgeBandsDetected = true;
				}
				else {
					try {
						ageBand.checkErrors(validationPolicy);
					}
					catch(RIFServiceException rifServiceException) {
						rifLogger.info(this.getClass(), "AgeBand.checkErrors(): " + 
							rifServiceException.getErrorMessages().size());
						errorMessages.addAll(rifServiceException.getErrorMessages());
						invalidAgeBandsDetected = true;
					}
				}				
			}
			
			//now check there are no overlapping age bands
			if (!invalidAgeBandsDetected) {
				errorMessages.addAll(AgeBand.checkGapsAndOverlaps(ageBands));				
			}	
		}
				
		if (sex == null) {
			String sexFieldName
				= RIFServiceMessages.getMessage("sex.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					sexFieldName);
			errorMessages.add(errorMessage);
		}
		
		if (yearRange == null) {
			String yearRangeFieldName
				= RIFServiceMessages.getMessage("yearRange.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					yearRangeFieldName);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				yearRange.checkErrors(validationPolicy);
			}
			catch(RIFServiceException rifServiceException) {
				rifLogger.info(this.getClass(), "YearRange.checkErrors(): " + 
					rifServiceException.getErrorMessages().size());
				errorMessages.addAll(rifServiceException.getErrorMessages());
			}
		
		}
		
		if (validationPolicy == ValidationPolicy.STRICT) {
			
			if (yearIntervals == null) {
				String yearIntervalsFieldName
					= RIFServiceMessages.getMessage("yearInterval.plural.label");
				String errorMessage
					= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField",
						recordType,
						yearIntervalsFieldName);
				errorMessages.add(errorMessage);
			}
			else if (yearIntervals.isEmpty()) {
				String errorMessage
					= RIFServiceMessages.getMessage("investigation.error.noYearIntervalsSpecified");
				errorMessages.add(errorMessage);
			}
			else {
				boolean allYearIntervalsAreValid = true;
				for (YearInterval yearInterval : yearIntervals) {
					if (yearInterval == null) {
						String yearIntervalRecordType
							= RIFServiceMessages.getMessage("yearInterval.label");
						String errorMessage
							= RIFServiceMessages.getMessage(
								"general.validation.nullListItem",
								getRecordType(),
								yearIntervalRecordType);
						errorMessages.add(errorMessage);
						allYearIntervalsAreValid = false;
					}
					else {
						try {
							yearInterval.checkErrors(validationPolicy);					
						}
						catch(RIFServiceException rifServiceException) {
							rifLogger.info(this.getClass(), "YearInterval.checkErrors(): " + 
								rifServiceException.getErrorMessages().size());
							errorMessages.addAll(rifServiceException.getErrorMessages());
							allYearIntervalsAreValid = false;
						}
					}
				}
			
				//now check that there are no duplicate or overlapping year intervals
				//but only bother doing this if all the year interval values are valid
				if (allYearIntervalsAreValid) {
					ArrayList<String> gapAndOverlapErrorMessages
						= YearInterval.checkGapsAndOverlaps(yearIntervals);			
					errorMessages.addAll(gapAndOverlapErrorMessages);			
				}
			}
		}

		//Interval can be none.  It just means that all the years will be included
		//together
		Collator collator = GENERIC_MESSAGES.getCollator();
		String noneChoice
			= RIFServiceMessages.getMessage("general.choices.none");
		if ((!fieldValidationUtility.isEmpty(interval)) &&
		    !collator.equals(interval, noneChoice)) {
			if (!StringUtils.isNumeric(interval)) {
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
		
		if (covariates == null || fieldValidationUtility.isEmpty(covariates)) {
			rifLogger.info(this.getClass(), "Investigation " + title + " has no covariates");
/*			String covariatesFieldName
				= RIFServiceMessages.getMessage("investigation.covariates.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					covariatesFieldName);
			errorMessages.add(errorMessage); */
		} else {
			for (AbstractCovariate covariate : covariates) {
				if (covariate == null) {
					rifLogger.info(this.getClass(), "Investigation " + title + " has null covariates");
					// Do nothing - they can be
				} else {
					try {
						covariate.checkErrors(validationPolicy);
					} catch(RIFServiceException rifServiceException) {
						rifLogger.info(this.getClass(), "Covariate.checkErrors(): " + 
							rifServiceException.getErrorMessages().size());
						errorMessages.addAll(rifServiceException.getErrorMessages());					
					}
				}
			}			
		}

		if (errorMessages.size() > 0) {
			rifLogger.info(this.getClass(), "Investigation.checkErrors(): " + 
				errorMessages.size());
		}			
		
		countErrors(RIFServiceError.INVALID_INVESTIGATION, errorMessages);
	}
	
	
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
	



	@Override
	public String getDisplayName() {
		
		return title;
	}

	@Override
	public String getRecordType() {
		
		String recordType
			= RIFServiceMessages.getMessage("investigation.label");
		return recordType;
	}
}
