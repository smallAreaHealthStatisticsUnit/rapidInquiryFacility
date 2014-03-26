package rifServices.businessConceptLayer;

import rifServices.util.FieldValidationUtility;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;

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

public final class NumeratorDenominatorPair 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The numerator table name. */
	private String numeratorTableName;
	
	/** The numerator table description. */
	private String numeratorTableDescription;
	
	/** The denominator table name. */
	private String denominatorTableName;
	
	/** The denominator table description. */
	private String denominatorTableDescription;
	
	// ==========================================
	// Section Construction
	// ==========================================


	
	/**
	 * Instantiates a new numerator denominator pair.
	 *
	 * @param numeratorTableName the numerator table name
	 * @param numeratorTableDescription the numerator table description
	 * @param denominatorTableName the denominator table name
	 * @param denominatorTableDescription the denominator table description
	 */
	private NumeratorDenominatorPair(
		final String numeratorTableName,
		final String numeratorTableDescription,
		final String denominatorTableName,
		final String denominatorTableDescription) {
		
		this.numeratorTableName = numeratorTableName;
		this.numeratorTableDescription = numeratorTableDescription;
		this.denominatorTableName = denominatorTableName;
		this.denominatorTableDescription = denominatorTableDescription;		
	}

	/**
	 * Instantiates a new numerator denominator pair.
	 */
	private NumeratorDenominatorPair() {
		numeratorTableName = "";
		numeratorTableDescription = "";
		denominatorTableName = "";
		denominatorTableDescription = "";		
	}
	
	/**
	 * New instance.
	 *
	 * @return the numerator denominator pair
	 */
	public static NumeratorDenominatorPair newInstance() {
		NumeratorDenominatorPair numeratorDenominatorPair 
			= new NumeratorDenominatorPair();
		return numeratorDenominatorPair;
	}

	
	/**
	 * New instance.
	 *
	 * @param numeratorTableName the numerator table name
	 * @param numeratorTableDescription the numerator table description
	 * @param denominatorTableName the denominator table name
	 * @param denominatorTableDescription the denominator table description
	 * @return the numerator denominator pair
	 */
	public static NumeratorDenominatorPair newInstance(
		final String numeratorTableName,
		final String numeratorTableDescription,
		final String denominatorTableName,
		final String denominatorTableDescription) {
		
		NumeratorDenominatorPair ndPair 
			= new NumeratorDenominatorPair(
				numeratorTableName,
				numeratorTableDescription,
				denominatorTableName,
				denominatorTableDescription);
		
		return ndPair;
	}	
	
	/**
	 * Creates the copy.
	 *
	 * @param originalNDPair the original nd pair
	 * @return the numerator denominator pair
	 */
	public static NumeratorDenominatorPair createCopy(
		final NumeratorDenominatorPair originalNDPair) {
		
		if (originalNDPair == null) {
			return null;
		}
		
		NumeratorDenominatorPair cloneNDPair 
			= new NumeratorDenominatorPair(
				originalNDPair.getNumeratorTableName(),
				originalNDPair.getNumeratorTableDescription(),
				originalNDPair.getDenominatorTableName(),
				originalNDPair.getDenominatorTableDescription());
			
		return cloneNDPair;
	}

	/**
	 * Gets the numerator table name.
	 *
	 * @return the numerator table name
	 */
	public String getNumeratorTableName() {
		return numeratorTableName;
	}

	/**
	 * Sets the numerator table name.
	 *
	 * @param numeratorTableName the new numerator table name
	 */
	public void setNumeratorTableName(
		final String numeratorTableName) {
		
		this.numeratorTableName = numeratorTableName;
	}
		
	/**
	 * Gets the numerator table description.
	 *
	 * @return the numerator table description
	 */
	public String getNumeratorTableDescription() {
		
		return numeratorTableDescription;
	}

	/**
	 * Sets the numerator table description.
	 *
	 * @param numeratorTableDescription the new numerator table description
	 */
	public void setNumeratorTableDescription(
		final String numeratorTableDescription) {
		
		this.numeratorTableDescription = numeratorTableDescription;
	}
	
	/**
	 * Gets the denominator table name.
	 *
	 * @return the denominator table name
	 */
	public String getDenominatorTableName() {
		return denominatorTableName;
	}

	/**
	 * Sets the denominator table name.
	 *
	 * @param denominatorTableName the new denominator table name
	 */
	public void setDenominatorTableName(
		final String denominatorTableName) {

		this.denominatorTableName = denominatorTableName;
	}
	
	/**
	 * Gets the denominator table description.
	 *
	 * @return the denominator table description
	 */
	public String getDenominatorTableDescription() {
		
		return denominatorTableDescription;
	}
	
	/**
	 * Sets the denominator table description.
	 *
	 * @param denominatorTableDescription the new denominator table description
	 */
	public void setDenominatorTableDescription(
		final String denominatorTableDescription) {
		
		this.denominatorTableDescription = denominatorTableDescription;
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherNDPair the other nd pair
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final NumeratorDenominatorPair otherNDPair) {

		if (otherNDPair == null) {
			return false;
		}
		
		String otherNumeratorTableName
			= otherNDPair.getNumeratorTableName();
		String otherNumeratorTableDescription
			= otherNDPair.getNumeratorTableDescription();
		String otherDenominatorTableName
			= otherNDPair.getDenominatorTableName();
		String otherDenominatorTableDescription
			= otherNDPair.getDenominatorTableDescription();
		
		Collator collator = Collator.getInstance();
		if (FieldValidationUtility.hasDifferentNullity(
			numeratorTableName, 
			otherNumeratorTableName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (numeratorTableName != null) {
			//they must both be non-null
			if (collator.equals(numeratorTableName, otherNumeratorTableName) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(
			numeratorTableDescription, 
			otherNumeratorTableDescription)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (numeratorTableDescription != null) {
			//they must both be non-null
			if (collator.equals(
				numeratorTableDescription, 
				otherNumeratorTableDescription) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(
			denominatorTableName, 
			otherDenominatorTableName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (numeratorTableName != null) {
			//they must both be non-null
			if (collator.equals(denominatorTableName, otherDenominatorTableName) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(
			denominatorTableDescription, 
			otherDenominatorTableDescription)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (numeratorTableDescription != null) {
			//they must both be non-null
			if (collator.equals(
				denominatorTableDescription, 
				otherDenominatorTableDescription) == false) {
				return false;
			}			
		}
		
		
		
		return super.hasIdenticalContents(otherNDPair);
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
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#checkSecurityViolations()
	 */
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();
		String recordType = getRecordType();

		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String numeratorTableFieldName
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.numeratorTableName.label");
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			numeratorTableFieldName, 
			numeratorTableName);
		
		String numeratorTableFieldDescription
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.numeratorTableDescription.label");
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			numeratorTableFieldDescription, 
			numeratorTableDescription);

		String denominatorTableFieldName
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.denominatorTableName.label");
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			denominatorTableFieldName, 
			denominatorTableName);
	
		String denominatorTableFieldDescription
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.denominatorTableDescription.label");
		fieldValidationUtility.checkMaliciousCode(
			recordType, 
			denominatorTableFieldDescription, 
			denominatorTableDescription);
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#checkErrors()
	 */
	public void checkErrors() 
		throws RIFServiceException {
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
	
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordType = getRecordType();
		String numeratorTableNameLabel
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.numeratorTableName.label");		
		if (fieldValidationUtility.isEmpty(numeratorTableName)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					numeratorTableNameLabel);
			errorMessages.add(errorMessage);			
		}
		String numeratorTableDescriptionLabel
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.numeratorTableDescription.label");		
		if (fieldValidationUtility.isEmpty(numeratorTableDescription)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					numeratorTableDescriptionLabel);
			errorMessages.add(errorMessage);			
		}
		
		String denominatorTableNameLabel
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.denominatorTableName.label");		
		if (fieldValidationUtility.isEmpty(denominatorTableName)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					denominatorTableNameLabel);
			errorMessages.add(errorMessage);			
		}
		String denominatorTableDescriptionLabel
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.denominatorTableDescription.label");		
		if (fieldValidationUtility.isEmpty(denominatorTableDescription)) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					denominatorTableDescriptionLabel);
			errorMessages.add(errorMessage);			
		}
		
		countErrors(
			RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR, 
			errorMessages);
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#getRecordType()
	 */
	public String getRecordType() {
		
		String recordType
			= RIFServiceMessages.getMessage("numeratorDenominatorPair.label");
		return recordType;
	}
	
	/* (non-Javadoc)
	 * @see rifServices.businessConceptLayer.AbstractRIFConcept#getDisplayName()
	 */
	public String getDisplayName() {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(numeratorTableName);
		buffer.append("-");
		buffer.append(denominatorTableName);
		return buffer.toString();
	}
}
