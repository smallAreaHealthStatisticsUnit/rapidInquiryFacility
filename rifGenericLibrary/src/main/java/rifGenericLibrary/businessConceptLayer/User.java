package rifGenericLibrary.businessConceptLayer;

import rifGenericLibrary.system.*;
import rifGenericLibrary.util.FieldValidationUtility;

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
 * Copyright 2016 Imperial College London, developed by the Small Area
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

final public class User {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The user id. */
	private String userID;
	
	/** The ip address. */
	private String ipAddress;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new user.
	 *
	 * @param userID the user id
	 * @param ipAddress the ip address
	 */
	private User(
		final String userID, 
		final String ipAddress) {

		this.userID = userID;
		this.ipAddress = ipAddress;
	}

	/**
	 * New instance.
	 *
	 * @param userID the user id
	 * @param ipAddress the ip address
	 * @return the user
	 */
	public static User newInstance(
		final String userID,
		final String ipAddress) {
		
		User user = new User(userID, ipAddress);
		return user;
	}

	/**
	 * Creates the copy.
	 *
	 * @param user the user
	 * @return the user
	 */
	public static User createCopy(
		final User user) {

		if (user == null) {
			return null;
		}
		User cloneUser = new User(user.getUserID(), user.getIPAddress());
		cloneUser.setIPAddress(user.getIPAddress());
		return cloneUser;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserID() {
		
		return userID;
	}
	
	
	/**
	 * Gets the record type.
	 *
	 * @return the record type
	 */
	public String getRecordType() {
		
		String recordType
			= RIFGenericLibraryMessages.getMessage("user.label");
		return recordType;
	}
	
	/**
	 * Gets the IP address.
	 *
	 * @return the IP address
	 */
	public String getIPAddress() {

		return ipAddress;
	}
	
	/**
	 * Sets the IP address.
	 *
	 * @param ipAddress the new IP address
	 */
	public void setIPAddress(
		final String ipAddress) {

		this.ipAddress = ipAddress;
	}
	
	public void identifyDifferences(
		final User anotherUser,
		final ArrayList<String> differences) {
		
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	/**
	 * Check errors.
	 *
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkErrors() 
		throws RIFServiceException {

		ArrayList<String> errorMessages = new ArrayList<String>();
		FieldValidationUtility fieldValidationUtility
			= new FieldValidationUtility();
		if (fieldValidationUtility.isEmpty(userID)) {
			String recordType = getRecordType();
			String userIDFieldName
				= RIFGenericLibraryMessages.getMessage("user.userID.label");
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					userIDFieldName);
			errorMessages.add(errorMessage);			
		}
			
		if (fieldValidationUtility.isEmpty(ipAddress)) {
			String recordType = getRecordType();
			String ipAddressFieldName
				= RIFGenericLibraryMessages.getMessage("user.ipAddress.label");
			String errorMessage
				= RIFGenericLibraryMessages.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					ipAddressFieldName);
			errorMessages.add(errorMessage);			
		}
		
		if (errorMessages.size() > 0) {
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFGenericLibraryError.INVALID_USER, 
					errorMessages);
			throw rifServiceException;
		}
	}
	
	/**
	 * Check security violations.
	 *
	 * @throws RIFServiceSecurityException the RIF service security exception
	 */
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		String recordType = getRecordType();
		if (userID != null) {
			String userIDFieldName
				= RIFGenericLibraryMessages.getMessage("user.userID.label");
			FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();	
			fieldValidationUtility.checkMaliciousCode(recordType, userIDFieldName, userID);
		}
		
		if (ipAddress != null) {
			String ipAddressFieldName
				= RIFGenericLibraryMessages.getMessage("user.ipAddress.label");
			FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();	
			fieldValidationUtility.checkMaliciousCode(recordType, ipAddressFieldName, ipAddress);			
		}
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
