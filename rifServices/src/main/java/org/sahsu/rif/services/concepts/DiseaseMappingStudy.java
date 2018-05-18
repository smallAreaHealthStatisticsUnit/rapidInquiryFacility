
package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
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


public final class DiseaseMappingStudy 
	extends AbstractStudy {

// ==========================================
// Section Constants
// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
	/** The disease mapping study area. */
	private DiseaseMappingStudyArea diseaseMappingStudyArea;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new disease mapping study.
	*/
	private DiseaseMappingStudy() {
    	
		diseaseMappingStudyArea = DiseaseMappingStudyArea.newInstance();
    }
	
	/**
	 * New instance.
	 *
	 * @return the disease mapping study
	 */
	static public DiseaseMappingStudy newInstance() {
		
		DiseaseMappingStudy diseaseMappingStudy = new DiseaseMappingStudy();
		return diseaseMappingStudy;
	}

	/**
	 * Creates the copy.
	 *
	 * @param originalDiseaseMappingStudy the original disease mapping study
	 * @return the disease mapping study
	 */
	static public DiseaseMappingStudy createCopy(
		final DiseaseMappingStudy originalDiseaseMappingStudy) {

		if (originalDiseaseMappingStudy == null) {
			return null;
		}

		DiseaseMappingStudy cloneDiseaseMappingStudy = new DiseaseMappingStudy();
		Geography originalGeography 
			= originalDiseaseMappingStudy.getGeography();
		Geography cloneGeography = Geography.createCopy(originalGeography);
		cloneDiseaseMappingStudy.setGeography(cloneGeography);
		
		cloneDiseaseMappingStudy.setIdentifier(originalDiseaseMappingStudy.getIdentifier());	     
		cloneDiseaseMappingStudy.setName(originalDiseaseMappingStudy.getName());
		cloneDiseaseMappingStudy.setDescription(originalDiseaseMappingStudy.getDescription());
		DiseaseMappingStudyArea cloneDiseaseMappingStudyArea
			= DiseaseMappingStudyArea.copy(originalDiseaseMappingStudy.getDiseaseMappingStudyArea());
		cloneDiseaseMappingStudy.setDiseaseMappingStudyArea(cloneDiseaseMappingStudyArea);
		ComparisonArea cloneComparisonArea 
			= ComparisonArea.createCopy(originalDiseaseMappingStudy.getComparisonArea());
		cloneDiseaseMappingStudy.setComparisonArea(cloneComparisonArea);
		
		ArrayList<Investigation> originalInvestigations
			= originalDiseaseMappingStudy.getInvestigations();
		ArrayList<Investigation> cloneInvestigations
			= Investigation.createCopy(originalInvestigations);
		cloneDiseaseMappingStudy.setInvestigations(cloneInvestigations);

		return cloneDiseaseMappingStudy;
	}

	
// ==========================================
// Section Accessors and Mutators
// ==========================================
    /**
     * Gets the disease mapping study area.
     *
     * @return the disease mapping study area
     */
	public DiseaseMappingStudyArea getDiseaseMappingStudyArea() {
    	
		return diseaseMappingStudyArea;
	}
	
	/**
	 * Sets the disease mapping study area.
	 *
	 * @param diseaseMappingStudyArea the new disease mapping study area
	 */
	public void setDiseaseMappingStudyArea(
		final DiseaseMappingStudyArea diseaseMappingStudyArea) {
		
		this.diseaseMappingStudyArea = diseaseMappingStudyArea;
	}

	
	public void identifyDifferences(
		final DiseaseMappingStudy anotherDiseaseMappingStudy,
		final ArrayList<String> differences) {
	
		super.identifyDifferences(
			anotherDiseaseMappingStudy, 
			differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherDiseaseMappingStudy the other disease mapping study
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final DiseaseMappingStudy otherDiseaseMappingStudy) {
		
		if (otherDiseaseMappingStudy == null) {
			return false;
		}
		
		DiseaseMappingStudyArea otherDiseaseMappingStudyArea
			= otherDiseaseMappingStudy.getDiseaseMappingStudyArea();
		
		if (diseaseMappingStudyArea == null) {
			if (otherDiseaseMappingStudyArea != null) {
				return false;
			}			
		}
		else {
			if (diseaseMappingStudyArea.hasIdenticalContents(otherDiseaseMappingStudyArea) == false) {
				return false;
			}
		}
		
		return super.hasIdenticalContents(otherDiseaseMappingStudy);
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================
	

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {		
		
		String recordType
			= RIFServiceMessages.getMessage("diseaseMappingStudy.label");
	
		ArrayList<String> errorMessages = new ArrayList<String>();
		super.checkErrors(
			validationPolicy,
			errorMessages);
		if (errorMessages.size() > 0) {
			rifLogger.debug(this.getClass(), "AbstractStudy.checkErrors(): " + 
				errorMessages.size());
		}	
				
		//add any errors inherent in the study area object
		if (diseaseMappingStudyArea == null) {
			String diseaseMappingStudyAreaFieldName
				= RIFServiceMessages.getMessage("diseaseMappingStudyArea.label");
		
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField",
					recordType,
					diseaseMappingStudyAreaFieldName);
			errorMessages.add(errorMessage);						
		}
		else {			
			try {
				diseaseMappingStudyArea.checkErrors(validationPolicy);
			}
			catch(RIFServiceException exception) {
				rifLogger.debug(this.getClass(), "DiseaseMappingStudyArea.checkErrors(): " + 
					exception.getErrorMessages().size());
				errorMessages.addAll(exception.getErrorMessages());
			}
		}
		
		countErrors(RIFServiceError.INVALID_DISEASE_MAPPING_STUDY, errorMessages);
	}
	
// ==========================================
// Section Interfaces
// ==========================================

// ==========================================
// Section Override
// ==========================================


	@Override
	public String getRecordType() {
		
		String recordTypeLabel
			= RIFServiceMessages.getMessage("diseaseMappingStudy.label");
		return recordTypeLabel;
	}
	

	@Override
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		super.checkSecurityViolations();
		if (diseaseMappingStudyArea != null) {
			diseaseMappingStudyArea.checkSecurityViolations();
		}

	}
	
}
