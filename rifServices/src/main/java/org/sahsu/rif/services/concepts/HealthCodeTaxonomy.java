package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;

/**
 * Describes a source of terms that describe health concepts.  The most
 * commonly used sources will be ICD 9 and ICD 10, but in future the 
 * RIF will likely be adapted to accommodate more sources of terms.
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

public class HealthCodeTaxonomy 
	extends AbstractRIFConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	/** The name. */
	private String name;
	
	/** The description. */
	private String description;
	
	/** The name space. */
	private String nameSpace;
	
	/** The version. */
	private String version;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new health code taxonomy.
	 *
	 * @param name the name
	 * @param description the description
	 * @param nameSpace the name space
	 * @param version the version
	 */
	private HealthCodeTaxonomy(
		final String name,
		final String description,
		final String nameSpace,
		final String version) {
		
		this.name = name;
		this.description = description;
		this.nameSpace = nameSpace;	
		this.version = version;
	}

	/**
	 * New instance.
	 *
	 * @return the health code taxonomy
	 */
	public static HealthCodeTaxonomy newInstance() {
		HealthCodeTaxonomy healthCodeTaxonomy
		= new HealthCodeTaxonomy(
			"", 
			"", 
			"",
			"");
	
		return healthCodeTaxonomy;		
	}
	
	/**
	 * New instance.
	 *
	 * @param name the name
	 * @param description the description
	 * @param nameSpace the name space
	 * @param version the version
	 * @return the health code taxonomy
	 */
	public static HealthCodeTaxonomy newInstance(
		final String name,
		final String description,
		final String nameSpace,
		final String version) {
		
		HealthCodeTaxonomy healthCodeTaxonomy
			= new HealthCodeTaxonomy(
				name, 
				description, 
				nameSpace,
				version);
		
		return healthCodeTaxonomy;
	}
	
	/**
	 * Creates the copy.
	 *
	 * @param originalHealthCodeTaxonomy the original health code taxonomy
	 * @return the health code taxonomy
	 */
	public static HealthCodeTaxonomy createCopy(
		final HealthCodeTaxonomy originalHealthCodeTaxonomy) {
		
		if (originalHealthCodeTaxonomy == null) {
			return null;
		}

		HealthCodeTaxonomy cloneHealthCodeTaxonomy = HealthCodeTaxonomy.newInstance();
		cloneHealthCodeTaxonomy.setIdentifier(originalHealthCodeTaxonomy.getIdentifier());
		cloneHealthCodeTaxonomy.setName(originalHealthCodeTaxonomy.getName());
		cloneHealthCodeTaxonomy.setDescription(originalHealthCodeTaxonomy.getDescription());
		cloneHealthCodeTaxonomy.setNameSpace(originalHealthCodeTaxonomy.getNameSpace());
		cloneHealthCodeTaxonomy.setVersion(originalHealthCodeTaxonomy.getVersion());		
		return cloneHealthCodeTaxonomy;		
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
		
		this.nameSpace = nameSpace;
	}


	public String getRecordType() {
		
		String recordType
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.label");
		return recordType;
	}	

	/**
	 * Sets the version.
	 *
	 * @param version the new version
	 */
	public void setVersion(
		final String version) {

		this.version = version;
	}
	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		
		return version;
	}

	
	public void identifyDifferences(
		final HealthCodeTaxonomy anotherHealthCodeTaxonomy,
		final ArrayList<String> differences) {
			
		super.identifyDifferences(
			anotherHealthCodeTaxonomy, 
			differences);
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
		if (name != null) {
			String nameFieldName
				= RIFServiceMessages.getMessage("healthCodeTaxonomy.name.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				nameFieldName,
				name);
		}
		
		if (description != null) {
			String descriptionFieldName
				= RIFServiceMessages.getMessage("healthCodeTaxonomy.description.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				descriptionFieldName,
				description);
		}

		if (nameSpace != null) {
			String nameSpaceFieldName
				= RIFServiceMessages.getMessage("healthCodeTaxonomy.nameSpace.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				nameSpaceFieldName,
				nameSpace);
		}	
		
		if (version != null) {
			String versionFieldName
				= RIFServiceMessages.getMessage("healthCodeTaxonomy.version.label");		
			fieldValidationUtility.checkMaliciousCode(
				recordType,
				versionFieldName,
				nameSpace);
		}	
	}
	
	public void checkErrors(
		final ValidationPolicy validationPolicy) 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();

		String recordName = getRecordType();
		String nameFieldName
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.name.label");		
		String descriptionFieldName
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.description.label");
		String nameSpaceFieldName
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.nameSpace.label");
		String versionFieldName
			= RIFServiceMessages.getMessage("healthCodeTaxonomy.version.label");
		
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(name)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordName,
					nameFieldName);
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
		
		if (fieldValidationUtility.isEmpty(nameSpace)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordName,
						nameSpaceFieldName);
			errorMessages.add(errorMessage);			
		}

		if (fieldValidationUtility.isEmpty(version)) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
						"general.validation.emptyRequiredRecordField", 
						recordName,
						versionFieldName);
			errorMessages.add(errorMessage);			
		}
		
		
		countErrors(RIFServiceError.INVALID_HEALTH_CODE_TAXONOMY, errorMessages);
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: DisplayableItem

	public String getDisplayName() {

		return name;
	}
		
	// ==========================================
	// Section Override
	// ==========================================

}
