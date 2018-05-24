package org.sahsu.rif.generic.concepts;

import java.util.ArrayList;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceSecurityException;
import org.sahsu.rif.generic.util.FieldValidationUtility;

final public class User {

	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// Used where a user is required in a method call, but there is no sensible one available.
	public static final User NULL_USER = new User("no-ne", "0.0.0.0");
	
	/** The user id. */
	private String userID;
	
	/** The ip address. */
	private String ipAddress;
	
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
			= GENERIC_MESSAGES.getMessage("user.label");
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
				= GENERIC_MESSAGES.getMessage("user.userID.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
					"general.validation.emptyRequiredRecordField", 
					recordType,
					userIDFieldName);
			errorMessages.add(errorMessage);			
		}
			
		if (fieldValidationUtility.isEmpty(ipAddress)) {
			String recordType = getRecordType();
			String ipAddressFieldName
				= GENERIC_MESSAGES.getMessage("user.ipAddress.label");
			String errorMessage
				= GENERIC_MESSAGES.getMessage(
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
				= GENERIC_MESSAGES.getMessage("user.userID.label");
			FieldValidationUtility fieldValidationUtility = new FieldValidationUtility();	
			fieldValidationUtility.checkMaliciousCode(recordType, userIDFieldName, userID);
		}
		
		if (ipAddress != null) {
			String ipAddressFieldName
				= GENERIC_MESSAGES.getMessage("user.ipAddress.label");
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
