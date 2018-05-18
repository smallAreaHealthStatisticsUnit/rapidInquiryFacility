
package org.sahsu.rif.services.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
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

public final class RIFServiceInformation 
	extends AbstractRIFConcept {

// ==========================================
// Section Constants
// ==========================================
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
// ==========================================
// Section Properties
// ==========================================
	/** The service name. */
	private String serviceName;
	
	private String serviceDescription;
	
	/** The version number. */
	private double versionNumber;

	/** The organisation. */
	private String organisation;
	
	/** The contact name. */
	private String contactName;
	
	/** The contact email. */
	private String contactEmail;
	
	/** The administrator messages. */
	private ArrayList<String> administratorMessages;
    
// ==========================================
// Section Construction
// ==========================================
    /**
 * Instantiates a new RIF service information.
 */
	public RIFServiceInformation() {

		serviceName = "";
		serviceDescription = "";
		versionNumber = 1.0;

		organisation = "";
		contactName = "";
		contactEmail = "";
		administratorMessages = new ArrayList<String>();
    }

    /**
     * New instance.
     *
     * @return the RIF service information
     */
    public static RIFServiceInformation newInstance() {
    	
    	RIFServiceInformation rifServiceInformation
    		= new RIFServiceInformation(); 	
    	return rifServiceInformation;
    }
    
    /**
     * Creates the copy.
     *
     * @param originalRIFServiceInformation the original rif service information
     * @return the RIF service information
     */
    public static RIFServiceInformation createCopy(
    	final RIFServiceInformation originalRIFServiceInformation) {
    	
    	RIFServiceInformation cloneServiceInformation
    		= new RIFServiceInformation();
    	cloneServiceInformation.setServiceName(originalRIFServiceInformation.getServiceName());
    	cloneServiceInformation.setServiceDescription(originalRIFServiceInformation.getServiceDescription());
    	cloneServiceInformation.setVersionNumber(originalRIFServiceInformation.getVersionNumber());
    	cloneServiceInformation.setOrganisation(originalRIFServiceInformation.getOrganisation());
    	cloneServiceInformation.setContactName(originalRIFServiceInformation.getContactName());
    	cloneServiceInformation.setContactEmail(originalRIFServiceInformation.getContactEmail());

    	ArrayList<String> administratorMessages
    		= originalRIFServiceInformation.getAdministratorMessages();
    	cloneServiceInformation.setAdministratorMessages(administratorMessages);
    	
    	return cloneServiceInformation;
    }
    
    
// ==========================================
// Section Accessors and Mutators
// ==========================================

	/**
	 * Gets the service name.
	 *
	 * @return the service name
	 */
    public String getServiceName() {
    	
		return serviceName;
	}
	
	/**
	 * Sets the service name.
	 *
	 * @param serviceName the new service name
	 */
	public void setServiceName(
		final String serviceName) {

		this.serviceName = serviceName;
	}
	
    public String getServiceDescription() {
    	return serviceDescription;
    }
    
    public void setServiceDescription(final String serviceDescription) {
    	this.serviceDescription = serviceDescription;
    }

	/**
	 * Gets the version number.
	 *
	 * @return the version number
	 */
	public double getVersionNumber() {
		
		return versionNumber;
	}

	/**
	 * Sets the version number.
	 *
	 * @param versionNumber the new version number
	 */
	public void setVersionNumber(
		final double versionNumber) {

		this.versionNumber = versionNumber;
	}

	
	/**
	 * Gets the organisation.
	 *
	 * @return the organisation
	 */
	public String getOrganisation() {
		
		return organisation;
	}

	/**
	 * Sets the organisation.
	 *
	 * @param organisation the new organisation
	 */
	public void setOrganisation(
		final String organisation) {

		this.organisation = organisation;
	}

	/**
	 * Gets the contact name.
	 *
	 * @return the contact name
	 */
	public String getContactName() {
		
		return contactName;
	}

	/**
	 * Sets the contact name.
	 *
	 * @param contactName the new contact name
	 */
	public void setContactName(
		final String contactName) {

		this.contactName = contactName;
	}

	/**
	 * Gets the contact email.
	 *
	 * @return the contact email
	 */
	public String getContactEmail() {
		
		return contactEmail;
	}

	/**
	 * Sets the contact email.
	 *
	 * @param contactEmail the new contact email
	 */
	public void setContactEmail(
		final String contactEmail) {

		this.contactEmail = contactEmail;
	}


	/**
	 * Gets the administrator messages.
	 *
	 * @return the administrator messages
	 */
	public ArrayList<String> getAdministratorMessages() {
		
		return administratorMessages;
	}

	/**
	 * Adds the administrator message.
	 *
	 * @param administratorMessage the administrator message
	 */
	public void addAdministratorMessage(
		final String administratorMessage) {

		administratorMessages.add(administratorMessage);
	}
	
	/**
	 * Sets the administrator messages.
	 *
	 * @param administratorMessages the new administrator messages
	 */
	public void setAdministratorMessages(
		final ArrayList<String> administratorMessages) {

		this.administratorMessages = administratorMessages;
	}
	
// ==========================================
// Section Errors and Validation
// ==========================================

	public void checkErrors(
		final ValidationPolicy validationPolicy) throws RIFServiceException {
		
		String recordType 
			= RIFServiceMessages.getMessage("rifServiceInformation.label");
		ArrayList<String> errorMessages = new ArrayList<String>();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(serviceName) == true) {
			String serviceNameLabel
				= RIFServiceMessages.getMessage("rifServiceInformation.name");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					serviceNameLabel);
			errorMessages.add(errorMessage);
		}

		if (fieldValidationUtility.isEmpty(serviceDescription) == true) {
			String serviceDescriptionLabel
				= RIFServiceMessages.getMessage("rifServiceInformation.description");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					serviceDescriptionLabel);
			errorMessages.add(errorMessage);
		}
		
		countErrors(RIFServiceError.INVALID_RIF_SERVICE_INFORMATION, errorMessages);
	}
	
// ==========================================
// Section Interfaces
// ==========================================

// ==========================================
// Section Override
// ==========================================

	public String getDisplayName() {
	
		StringBuilder buffer = new StringBuilder();
		buffer.append(serviceName);
		buffer.append("-v");
		buffer.append(String.valueOf(versionNumber));
		
		return buffer.toString();
	}
	

	@Override
	public String getRecordType() {
		
		String recordNameLabel
			= RIFServiceMessages.getMessage("rifServiceInformation.label");
		return recordNameLabel;
	}

}
