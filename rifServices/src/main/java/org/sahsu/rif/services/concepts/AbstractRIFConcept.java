package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;

import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 *
 * The base class which holds some basic properties that should be shared by all
 * the business classes used in the RIF.  
 * </p>
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

public abstract class AbstractRIFConcept 
	implements DisplayableListItemInterface {

	// ==========================================
	// Section Constants
	// ==========================================
	public enum ValidationPolicy {STRICT, RELAXED};
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The identifier. */
	private String identifier;
	
	/** The is new record. */
	private boolean isNewRecord;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new abstract rif concept.
	 */
	public AbstractRIFConcept() {
		
		isNewRecord = true;
	}

	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getIdentifier() {

		return identifier;
	}

	/**
	 * Sets the identifier.
	 *
	 * @param identifier the new identifier
	 */
	public void setIdentifier(
		final String identifier) {

		this.identifier = identifier;
	}


	/**
	 * Gets the record type.
	 *
	 * @return the record type
	 */
	abstract public String getRecordType();

	
	public void identifyDifferences(
		final AbstractRIFConcept anotherConcept,
		final ArrayList<String> differences) {
	

	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherRIFConcept the other rif concept
	 * @return true, if successful
	 */
	protected boolean hasIdenticalContents(
		final AbstractRIFConcept otherRIFConcept) {
		
		if (otherRIFConcept == null) {
			return false;
		}

		Collator collator = Messages.genericMessages().getCollator();

		String otherIdentifier = otherRIFConcept.getIdentifier();

		if (FieldValidationUtility.hasDifferentNullity(identifier, otherIdentifier)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (identifier != null) {
			//they must both be non-null
			if (!collator.equals(identifier, otherIdentifier)) {
				return false;
			}
		}	
		
		return true;
	}
	
	/*
	 * @TODO May want to deprecate this method and the use of identifier property.
	 */
	/**
	 * Checks if is new record.
	 *
	 * @return true, if is new record
	 */
	public boolean isNewRecord() {
		
		return isNewRecord;
	}
	
	/**
	 * Sets the new record.
	 *
	 * @param isNewRecord the new new record
	 */
	public void setNewRecord(
		final boolean isNewRecord) {
		
		this.isNewRecord = isNewRecord;
	}
	
	public void countErrors(
		final RIFServiceError rifServiceError,
		final ArrayList<String> errorMessages) 
		throws RIFServiceException {
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		
		fieldValidationUtility.throwExceptionIfErrorsFound(
			rifServiceError, 
			errorMessages);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	/**
	 * Check errors.
	 *
	 * @throws RIFServiceException the RIF service exception
	 */
	public abstract void checkErrors(
		final ValidationPolicy validationPolicy) throws RIFServiceException;
		
	/**
	 * Check security violations.
	 *
	 * @throws RIFServiceSecurityException the RIF service security exception
	 */
	protected void checkSecurityViolations() 
		throws RIFServiceSecurityException {
		
		FieldValidationUtility fieldValidationUtility 
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(identifier) == true) {
			return;
		}
		
		String recordType = getRecordType();
		String identifierLabel
			= RIFServiceMessages.getMessage("general.fieldNames.identifier.label");
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			identifierLabel,
			identifier);
	}
		
	// ==========================================
	// Section Interfaces
	// ==========================================
	
	//Interface: DisplayableListItem
	public abstract String getDisplayName();
	
	// ==========================================
	// Section Override
	// ==========================================
}
