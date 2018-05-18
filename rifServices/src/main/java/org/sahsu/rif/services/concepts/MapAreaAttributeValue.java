package org.sahsu.rif.services.concepts;

import java.text.Collator;
import java.util.ArrayList;

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

public final class MapAreaAttributeValue 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The label. */
	private String label;
	private String attributeName;
	private String attributeValue;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new map area.
	 *
	 * @param identifier the identifier
	 * @param label the label
	 */
	private MapAreaAttributeValue(
		final String identifier,
		final String label,
		final String attributeName,
		final String attributeValue) {
		
		setIdentifier(identifier);
		setLabel(label);
		setAttributeName(attributeName);
		setAttributeValue(attributeValue);
	}
	
	/**
	 * Instantiates a new map area.
	 */
	private MapAreaAttributeValue() {
		setIdentifier("");
		setLabel("");
		setAttributeName("");
		setAttributeValue("");
	}

	/**
	 * New instance.
	 *
	 * @return the map area attribute value
	 */
	public static MapAreaAttributeValue newInstance() {		
		MapAreaAttributeValue mapAreaAttributeValue 
			= new MapAreaAttributeValue();
		return mapAreaAttributeValue;		
	}
	
	
	/**
	 * New instance.
	 *
	 * @param identifier the identifier
	 * @param label the label
	 * @param attributeName
	 * @param attributeValue
	 * @return the map area
	 */
	public static MapAreaAttributeValue newInstance(
		final String identifier,
		final String label,
		final String attributeName,
		final String attributeValue) {
		
		MapAreaAttributeValue mapAreaAttributeValue 
			= new MapAreaAttributeValue(
				identifier, 
				label,
				attributeName,
				attributeValue);
		return mapAreaAttributeValue;		
	}
		
	/**
	 * Creates the copy.
	 *
	 * @param originalMapArea the original map area
	 * @return the map area
	 */
	public static MapAreaAttributeValue createCopy(
		final MapAreaAttributeValue originalMapAreaAttributeValue) {

		MapAreaAttributeValue cloneMapArea
			= new MapAreaAttributeValue(
				originalMapAreaAttributeValue.getIdentifier(),
				originalMapAreaAttributeValue.getLabel(),
				originalMapAreaAttributeValue.getAttributeName(),
				originalMapAreaAttributeValue.getAttributeValue());
		return cloneMapArea;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalMapAreas the original map area attribute value
	 * @return the array list
	 */
	public static ArrayList<MapAreaAttributeValue> createCopy(
		final ArrayList<MapAreaAttributeValue> originalMapAreaAttributeValues) {
		
		ArrayList<MapAreaAttributeValue> cloneMapAreaAttributeValues 
			= new ArrayList<MapAreaAttributeValue>();
		
		for (MapAreaAttributeValue originalMapAreaAttributeValue : originalMapAreaAttributeValues) {
			MapAreaAttributeValue cloneMapAreaAttributeValue 
				= MapAreaAttributeValue.createCopy(originalMapAreaAttributeValue);
			cloneMapAreaAttributeValues.add(cloneMapAreaAttributeValue);			
		}
		
		return cloneMapAreaAttributeValues;
	}
	
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(
		final String label) {

		this.label = label;
	}
	
	
		
	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public void identifyDifferences(
		final MapAreaAttributeValue anotherMapAreaAttributeValue,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherMapAreaAttributeValue, 
			differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param otherMapArea the other map area
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final MapAreaAttributeValue otherMapAreaAttributeValue) {

		Collator collator = GENERIC_MESSAGES.getCollator();

		String otherLabel = otherMapAreaAttributeValue.getLabel();
		if (FieldValidationUtility.hasDifferentNullity(label, otherLabel)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (label != null) {
			//they must both be non-null
			if (collator.equals(label, otherLabel) == false) {
				return false;
			}			
		}

		String otherAttributeName = otherMapAreaAttributeValue.getAttributeName();
		if (FieldValidationUtility.hasDifferentNullity(attributeName, otherAttributeName)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (attributeName != null) {
			//they must both be non-null
			if (collator.equals(attributeName, otherAttributeName) == false) {
				return false;
			}			
		}
		
		String otherAttributeValue = otherMapAreaAttributeValue.getAttributeValue();
		if (FieldValidationUtility.hasDifferentNullity(
			attributeValue, 
			otherAttributeValue)) {
			
			//reject if one is null and the other is non-null
			return false;
		}
		else if (attributeValue != null) {
			//they must both be non-null
			if (collator.equals(attributeValue, otherAttributeValue) == false) {
				return false;
			}			
		}
		
		return super.hasIdenticalContents(otherMapAreaAttributeValue);
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
		if (label != null) {
			String labelFieldName
				= RIFServiceMessages.getMessage(
					"mapAreaAttributeValue.label.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				labelFieldName,
				label);
		}	

		if (attributeName != null) {
			String attributeNameFieldName
				= RIFServiceMessages.getMessage(
					"mapAreaAttributeValue.label.attributeName");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				attributeNameFieldName,
				attributeName);
		}		
	
		if (attributeValue != null) {
			String attributeValueFieldName
				= RIFServiceMessages.getMessage(
					"mapAreaAttributeValue.label.attributeValue");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				attributeValueFieldName,
				attributeValue);
		}	
	}
	

	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();	
		if (fieldValidationUtility.isEmpty(getIdentifier()) == true) {
			String recordType = getRecordType();
			String identifierFieldName
				= RIFServiceMessages.getMessage("general.fieldNames.identifier.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					identifierFieldName);
			errorMessages.add(errorMessage);
		}		
		
	//@TODO uncomment this line when we get new release of RIF database
	//05-0302014
/*		
		if (fieldValidationUtility.isEmpty(label)) {
			String recordType = getRecordType();
			String labelFieldName
				= RIFServiceMessages.getMessage("mapArea.label.label");	
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					labelFieldName);
			errorMessages.add(errorMessage);
		}
*/
		countErrors(RIFServiceError.INVALID_MAP_AREA, errorMessages);
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
		buffer.append(getIdentifier());
		buffer.append("-");
		buffer.append(label);
		
		return buffer.toString();		
	}
	

	@Override
	public String getRecordType() {

		String recordType
			= RIFServiceMessages.getMessage("mapArea.label");
		return recordType;
	}

}
