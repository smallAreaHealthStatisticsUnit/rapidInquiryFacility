
package rifServices.businessConceptLayer;

import rifServices.businessConceptLayer.AbstractRIFConcept;

import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.FieldValidationUtility;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * 
 * Embodies a request that the RIF uses to do a disease mapping or risk analysis
 * activity.  Note that the class {@link rifServices.businessConceptLayer.RIFJobSubmission}
 * encompasses both the request (study) and various information about how the results
 * should be configured.
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


abstract public class AbstractStudy 
	extends AbstractRIFConcept {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The name. */
	private String name;
	
	/** The description. */
	private String description;
	
	/** The known issues. */
	private String knownIssues;
	
	/** The geography. */
	private Geography geography;
	
	/** The comparison area. */
	private ComparisonArea comparisonArea;
	
	/** The investigations. */
	private ArrayList<Investigation> investigations;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new abstract study.
     */
	public AbstractStudy() {
    	
		name = "";
		description = "";
		knownIssues = "";

		geography = Geography.newInstance();
		comparisonArea = ComparisonArea.newInstance();
		investigations = new ArrayList<Investigation>();
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(
		final String name) {
		
		this.name = name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(
		final String description) {
		
		this.description = description;
	}

	/**
	 * Gets the known issues.
	 *
	 * @return the known issues
	 */
	public String getKnownIssues() {
		
		return knownIssues;
	}

	/**
	 * Sets the known issues.
	 *
	 * @param knownIssues the new known issues
	 */
	public void setKnownIssues(
		final String knownIssues) {
		
		this.knownIssues = knownIssues;
	}
	
	/**
	 * Gets the geography.
	 *
	 * @return the geography
	 */
	public Geography getGeography() {
		
		return geography;
	}
	
	/**
	 * Sets the geography.
	 *
	 * @param geography the new geography
	 */
	public void setGeography(
		final Geography geography) {
		
		this.geography = geography;
	}
	
	/**
	 * Gets the comparison area.
	 *
	 * @return the comparison area
	 */
	public ComparisonArea getComparisonArea() {
		
		return comparisonArea;
	}

	/**
	 * Sets the comparison area.
	 *
	 * @param comparisonArea the new comparison area
	 */
	public void setComparisonArea(
		final ComparisonArea comparisonArea) {
		
		this.comparisonArea = comparisonArea;
	}
	
	/**
	 * Gets the investigations.
	 *
	 * @return the investigations
	 */
	public ArrayList<Investigation> getInvestigations() {
		
		return investigations;
	}
	
	/**
	 * Adds the investigation.
	 *
	 * @param investigation the investigation
	 */
	public void addInvestigation(
		final Investigation investigation) {
		
		investigations.add(investigation);
	}
	
	/**
	 * Sets the investigations.
	 *
	 * @param investigations the new investigations
	 */
	public void setInvestigations(
		final ArrayList<Investigation> investigations) {
		
		this.investigations = investigations;
	}
	
	/**
	 * Clear investigations.
	 */
	public void clearInvestigations() {
		
		investigations.clear();
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherStudy the other study
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final AbstractStudy otherStudy) {
		
		String otherName = otherStudy.getName();
		String otherDescription = otherStudy.getDescription();
		String otherKnownIssues = otherStudy.getKnownIssues();

		Collator collator = Collator.getInstance();
		
		ComparisonArea otherComparisonArea = otherStudy.getComparisonArea();		
		ArrayList<Investigation> otherInvestigations
			= otherStudy.getInvestigations();
	
		if (FieldValidationUtility.hasDifferentNullity(name, otherName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (name != null) {
			//they must both be non-null
			if (collator.equals(name, otherName) == false) {
				return false;
			}
		}
		
		if (FieldValidationUtility.hasDifferentNullity(description, otherDescription)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (description != null) {
			//they must both be non-null
			if (collator.equals(description, otherDescription) == false) {
				return false;
			}
		}
		
		if (FieldValidationUtility.hasDifferentNullity(knownIssues, otherKnownIssues)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (knownIssues != null) {
			//they must both be non-null
			if (collator.equals(knownIssues, otherKnownIssues) == false) {
				return false;
			}
		}
				
		if (FieldValidationUtility.hasDifferentNullity(
			comparisonArea, 
			otherComparisonArea)) {
			
			//reject if one is null and the other is non-null
			return false;
		}
		else if (comparisonArea != null) {
			//they must both be non-null
			if (comparisonArea.hasIdenticalContents(otherComparisonArea) == false) {
				return false;
			}
		}
		
		if (Investigation.hasIdenticalContents(investigations, otherInvestigations) == false) {
			return false;
		}
		
		return super.hasIdenticalContents(otherStudy);
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#checkSecurityViolations()
	 */
	@Override
	protected void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();
		
		String recordType = getRecordType();
		
		//Extract field names
		String nameFieldLabel
			= RIFServiceMessages.getMessage("abstractStudy.name.label");
		String descriptionFieldLabel
			= RIFServiceMessages.getMessage("abstractStudy.description.label");
		String knownIssuesFieldLabel
			= RIFServiceMessages.getMessage("abstractStudy.knownIssues.label");
				
		//Extract field values
		
		//Check for security problems.  Ensure EVERY text field is checked
		//These checks will throw a security exception and stop further validation
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			nameFieldLabel, 
			name);
		
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			descriptionFieldLabel, 
			description);

		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			knownIssuesFieldLabel, 
			knownIssues);

		
		geography.checkSecurityViolations();
		comparisonArea.checkSecurityViolations();
		for (Investigation investigation : investigations) {
			investigation.checkSecurityViolations();
		}
	}
	
	/**
	 * Check errors.
	 *
	 * @param errorMessages the error messages
	 * @throws RIFServiceException the RIF service exception
	 */
	protected void checkErrors(
		final ArrayList<String> errorMessages) 
		throws RIFServiceException {

		//Extract field names
		String nameFieldLabel
			= RIFServiceMessages.getMessage("abstractStudy.name.label");
		String descriptionFieldLabel
			= RIFServiceMessages.getMessage("abstractStudy.description.label");
				
		//Extract field values
		String recordType = getRecordType();
		
		FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();		
		if (fieldValidationUtility.isEmpty(name) == true) {
			String errorMessage 
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					nameFieldLabel);
			errorMessages.add(errorMessage);
		}
    
		if (fieldValidationUtility.isEmpty(getDescription()) == true) {
			String errorMessage 
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					descriptionFieldLabel);
			errorMessages.add(errorMessage);
		}   
		
		if (geography == null) {
			String comparisonAreaLabel
				= RIFServiceMessages.getMessage("geography.label");
			String errorMessage 
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					comparisonAreaLabel);
			errorMessages.add(errorMessage);			
		}
		else {
			try {
				geography.checkErrors();
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
		
		if (comparisonArea == null) {
			String comparisonAreaLabel
				= RIFServiceMessages.getMessage("comparisonArea.label");
			String errorMessage 
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					comparisonAreaLabel);
			errorMessages.add(errorMessage);
		}
		else {
			try {
				comparisonArea.checkErrors();
			}
			catch(RIFServiceException exception) {
				errorMessages.addAll(exception.getErrorMessages());
			}			
		}
		
		if (investigations == null) {
			String investigationsFieldName
				= RIFServiceMessages.getMessage("investigation.plural.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					investigationsFieldName);
			errorMessages.add(errorMessage);			
		}
		else if (investigations.isEmpty() == true) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"abstractStudy.error.noInvestigationSpecified",
					recordType,
					getDisplayName());
			errorMessages.add(errorMessage);
		}
		else {
			for (Investigation investigation : investigations) {
				try {
					investigation.checkErrors();
				}
				catch(RIFServiceException exception) {
					errorMessages.addAll(exception.getErrorMessages());			
				}
			}
			
			//go through investigations again to ensure there are none with duplicate titles
			HashSet<String> uniqueInvestigationTitles = new HashSet<String>();
			for (Investigation investigation : investigations) {
				String currentInvestigationTitle
					= investigation.getTitle();
				if (uniqueInvestigationTitles.contains(currentInvestigationTitle) == true) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"abstractStudy.error.duplicateInvestigation",
							recordType,
							getDisplayName(),
							currentInvestigationTitle);
					errorMessages.add(errorMessage);
				}
				else {
					uniqueInvestigationTitles.add(currentInvestigationTitle);
				}
			}
		}		
	}
		
// ==========================================
// Section Interfaces
// ==========================================

// ==========================================
// Section Override
// ==========================================

	//Interface: Displayable List Item
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		
		return name;
	}

}
