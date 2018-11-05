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
 * Describes a concept that can be used to identify health records of interest.
 * A health code will belong to some taxonomy of health terms, but will typically
 * come from the ICD 9 or ICD 10 coding conventions. 
 * 
 * <p>
 * A health code has the following key attributes:
 * <ul>
 * <li><b>code</b>: uniquely identifies the concept in a way that is easily understood
 * by software and machines.</li>
 * <li><b>description</b>: identifies the concept in a way that is meant for 
 * people to understand</li>
 * <li><b>nameSpace</b>: provides a unique identifier for the taxonomy to which
 * a concept belongs (eg: ICD 9, ICD 10)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The "numberofSubTerms" attribute is an optional attribute which is meant to 
 * benefit programs which are rendering displays of the taxonomy.  By default the
 * value is set to zero.
 * </p>
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

public final class HealthCode 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The is top level term. */
	private boolean isTopLevelTerm;
	
	/** The code. */
	private String code;
	
	/** The name space. */
	private String nameSpace;
	
	/** The description. */
	private String description;
	
	/** The number of sub terms. */
	private int numberOfSubTerms;
		
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new health code.
	 */
	private HealthCode() {
		
		isTopLevelTerm = false;
		code = "";
		nameSpace = "";
		description = "";
		numberOfSubTerms = 0;
	}

	/**
	 * New instance.
	 *
	 * @return the health code
	 */
	public static HealthCode newInstance() {
		
		HealthCode healthCode = new HealthCode();
		return healthCode;
	}
	
	/**
	 * New instance.
	 *
	 * @param code the code
	 * @param nameSpace the name space
	 * @param description the description
	 * @param isTopLevelTerm the is top level term
	 * @return the health code
	 */
	public static HealthCode newInstance(
		final String code,
		final String nameSpace,
		final String description,
		final boolean isTopLevelTerm) {
		
		HealthCode healthCode = new HealthCode();
		healthCode.setCode(code);
		healthCode.setNameSpace(nameSpace);
		healthCode.setDescription(description);
		healthCode.setTopLevelTerm(isTopLevelTerm);
		return healthCode;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalHealthCode the original health code
	 * @return the health code
	 */
	public static HealthCode createCopy(
		final HealthCode originalHealthCode) {
		
		if (originalHealthCode == null) {
			return null;
		}
		
		HealthCode cloneHealthCode = new HealthCode();
		cloneHealthCode.setIdentifier(originalHealthCode.getIdentifier());
		cloneHealthCode.setCode(originalHealthCode.getCode());
		cloneHealthCode.setDescription(originalHealthCode.getDescription());
		cloneHealthCode.setNameSpace(originalHealthCode.getNameSpace());
		cloneHealthCode.setTopLevelTerm(originalHealthCode.isTopLevelTerm());
		cloneHealthCode.setNumberOfSubTerms(originalHealthCode.getNumberOfSubTerms());
		return cloneHealthCode;
	}

	/**
	 * Creates the copy.
	 *
	 * @param originalHealthCodes the original health codes
	 * @return the array list
	 */
	public static ArrayList<HealthCode> createCopy(
		final ArrayList<HealthCode> originalHealthCodes) {
		
		if (originalHealthCodes == null) {
			return null;
		}
		
		ArrayList<HealthCode> cloneHealthCodes = new ArrayList<HealthCode>();
		for (HealthCode originalHealthCode : originalHealthCodes) {
			HealthCode cloneHealthCode = HealthCode.createCopy(originalHealthCode);
			cloneHealthCodes.add(cloneHealthCode);
		}
		
		return cloneHealthCodes;		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code the new code
	 */
	public void setCode(
		final String code) {
		
		this.code = code;
	}

	/**
	 * Gets the name space.
	 *
	 * @return the name space
	 */
	public String getNameSpace() {
		
		return nameSpace;
	}

	/**
	 * Sets the name space.
	 *
	 * @param nameSpace the new name space
	 */
	public void setNameSpace(
		final String nameSpace) {
		
		this.nameSpace = nameSpace != null ? nameSpace.trim() : null;
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
	 * Sets the top level term.
	 *
	 * @param isTopLevelTerm the new top level term
	 */
	public void setTopLevelTerm(
		final boolean isTopLevelTerm) {
		
		this.isTopLevelTerm = isTopLevelTerm;
	}
	
	/**
	 * Checks if is top level term.
	 *
	 * @return true, if is top level term
	 */
	public boolean isTopLevelTerm() {
		
		return isTopLevelTerm;
	}
	
	/**
	 * Sets the number of sub terms.
	 *
	 * @param numberOfSubTerms the new number of sub terms
	 */
	public void setNumberOfSubTerms(
		final int numberOfSubTerms) {

		this.numberOfSubTerms = numberOfSubTerms;
	}
	
	/**
	 * Gets the number of sub terms.
	 *
	 * @return the number of sub terms
	 */
	public int getNumberOfSubTerms() {
		
		return numberOfSubTerms;
	}
	
	public void identifyDifferences(
		final HealthCode anotherHealthCode,
		final ArrayList<String> differences) {
		
		super.identifyDifferences(
			anotherHealthCode, 
			differences);
	}
	
	/**
	 * Checks for identical contents.
	 *
	 * @param healthCodeListA the health code list a
	 * @param healthCodeListB the health code list b
	 * @return true, if successful
	 */
	public static boolean hasIdenticalContents(
		final ArrayList<HealthCode> healthCodeListA, 
		final ArrayList<HealthCode> healthCodeListB) {

		if (FieldValidationUtility.hasDifferentNullity(
			healthCodeListA, 
			healthCodeListB)) {
			//reject if one is null and the other is non-null
			return false;
		}
			
		if (healthCodeListA.size() != healthCodeListB.size() ) {
			//reject if lists do not have the same size
			return false;
		}
			
		//create temporary sorted lists to enable item by item comparisons
		//in corresponding lists
		ArrayList<HealthCode> healthCodesA = sortHealthCodes(healthCodeListA);
		ArrayList<HealthCode> healthCodesB = sortHealthCodes(healthCodeListB);
			
		int numberOfHealthCodes = healthCodesA.size();
		for (int i = 0; i < numberOfHealthCodes; i++) {
			HealthCode healthCodeA
				= healthCodesA.get(i);				
			HealthCode healthCodeB
				= healthCodesB.get(i);
			if (healthCodeA.hasIdenticalContents(healthCodeB) == false) {			
				return false;
			}			
		}
					
		return true;
	}

	/**
	 * Sort health codes.
	 *
	 * @param healthCodes the health codes
	 * @return the array list
	 */
	private static ArrayList<HealthCode> sortHealthCodes(
		final ArrayList<HealthCode> healthCodes) {

		DisplayableItemSorter sorter = new DisplayableItemSorter();
		
		for (HealthCode healthCode : healthCodes) {
			sorter.addDisplayableListItem(healthCode);
		}
		
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
		ArrayList<String> identifiers = sorter.sortIdentifiersList();
		for (String identifier : identifiers) {
			HealthCode sortedHealthCode 
				= (HealthCode) sorter.getItemFromIdentifier(identifier);
			results.add(sortedHealthCode);
		}
		
		return results;
	}
		
	/**
	 * Checks for identical contents.
	 *
	 * @param otherHealthCode the other health code
	 * @return true, if successful
	 */
	public boolean hasIdenticalContents(
		final HealthCode otherHealthCode) {
		
		if (otherHealthCode == null) {
			return false;
		}

		boolean otherIsTopLevelTerm = otherHealthCode.isTopLevelTerm();
		String otherCode = otherHealthCode.getCode();
		String otherNameSpace = otherHealthCode.getNameSpace();
		String otherDescription = otherHealthCode.getDescription();
		int otherNumberOfSubTerms = otherHealthCode.getNumberOfSubTerms();
		
		if (isTopLevelTerm != otherIsTopLevelTerm) {
			return false;
		}
		
		Collator collator = Collator.getInstance();
		if (FieldValidationUtility.hasDifferentNullity(code, otherCode)) {
			//reject if one is null and the other is non-null
			return false;
		}
		else if (code != null) {
			//they must both be non-null
			if (collator.equals(code, otherCode) == false) {
				return false;
			}			
		}
		
		if (FieldValidationUtility.hasDifferentNullity(nameSpace, otherNameSpace)) {
			//reject if one is null and the other is non-null			
			return false;
		}
		else if (nameSpace != null) {
			//they must both be non-null
			if (collator.equals(nameSpace, otherNameSpace) == false) {				
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
		
		if (numberOfSubTerms != otherNumberOfSubTerms) {
			return false;
		}

		return super.hasIdenticalContents(otherHealthCode);
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	

	public void checkErrors(
		final ValidationPolicy validationPolicy)
		throws RIFServiceException {
		
		ArrayList<String> errorMessages = new ArrayList<String>();
		
		String recordName = getRecordType();
		
		String codeFieldName
			= RIFServiceMessages.getMessage("healthCode.code.label");
		String nameSpaceFieldName
			= RIFServiceMessages.getMessage("healthCode.namespace.label");
		String descriptionFieldName
			= RIFServiceMessages.getMessage("healthCode.description.label");
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(code)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					codeFieldName);
			errorMessages.add(errorMessage);			
		}
		
		if (fieldValidationUtility.isEmpty(nameSpace)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordName,
						nameSpaceFieldName);
			errorMessages.add(errorMessage);			
		}
		
		if (fieldValidationUtility.isEmpty(description)) {

			String errorMessage
				= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordName,
						descriptionFieldName);
			errorMessages.add(errorMessage);			
		}
		
		countErrors(RIFServiceError.INVALID_HEALTH_CODE, errorMessages);
	}
	
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		super.checkSecurityViolations();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		String recordType = getRecordType();
		String codeFieldValue = getCode();
		if (codeFieldValue != null) {
			String codeFieldName
				= RIFServiceMessages.getMessage("healthCode.code.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				codeFieldName,
				codeFieldValue);
		}
				
		String descriptionFieldValue = getDescription();
		if (descriptionFieldValue != null) {
			String descriptionFieldName
				= RIFServiceMessages.getMessage("healthCode.description.label");
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				descriptionFieldName,
				descriptionFieldValue);			
		}
		
		String nameSpaceFieldValue = getNameSpace();
		if (nameSpaceFieldValue != null) {
			String nameSpaceFieldName
				= RIFServiceMessages.getMessage("healthCode.namespace.label");
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				nameSpaceFieldName,
				nameSpaceFieldValue);		
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
		
		StringBuilder displayName = new StringBuilder();
		displayName.append(code);
		displayName.append("-");
		
		if (description.length() <= 40) {
			displayName.append(description);
		}
		else {
			displayName.append(description.substring(0, 40));
			displayName.append("...");
		}
				
		return displayName.toString();
	}
	
	@Override
	public String getRecordType() {
		
		String recordNameLabel
			= RIFServiceMessages.getMessage("healthCode.label");
		return recordNameLabel;
	}	
		

	@Override
	public String getIdentifier() {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(getCode());
		buffer.append(":");
		buffer.append(nameSpace);
		return buffer.toString();
	}
		
	
}
