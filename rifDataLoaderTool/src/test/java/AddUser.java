import java.util.Date;

import rifDataLoaderTool.businessConceptLayer.RIFUserRole;
import rifDataLoaderTool.dataStorageLayer.DataLoaderService;
import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.test.AbstractRIFDataLoaderTestCase;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
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

public class AddUser extends AbstractRIFDataLoaderTestCase {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private static final String validPassword = "1a2b3c";
	private static final String emptyPassword = "";
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AddUser() {

	}

	private Date createValidExpirationDate() {
		return RIFServiceMessages.getDate("01-JAN-2050");
	}
	
	private Date createInvalidExpirationDate() {
		return RIFServiceMessages.getDate("01-JAN-1980");
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	@Test
	public void addUser_COMMON1() {
		
		User validAdministrationUser
			= cloneValidAdministrationUser();
		RIFUserRole validRIFUserRole = RIFUserRole.RIF_USER;
		Date validExpirationDate = createValidExpirationDate();

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				validAdministrationUser, 
				validPassword, 
				validRIFUserRole, 
				validExpirationDate);
		}
		catch(Exception exception) {
			fail();
		}
	}

	@Test
	/**
	 * test empty user - a user object containing an empty field
	 */
	public void addUser_EMPTY1() {
		
		User emptyUser = cloneEmptyAdministrationUser();
		RIFUserRole validRIFUserRole = RIFUserRole.RIF_USER;
		Date validExpirationDate = createValidExpirationDate();

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				emptyUser, 
				validPassword, 
				validRIFUserRole, 
				validExpirationDate);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER, 
				1);
		}
	}
	
	@Test
	/**
	 * null user object
	 */
	public void addUser_NULL1() {
		
		RIFUserRole validRIFUserRole = RIFUserRole.RIF_USER;
		Date validExpirationDate = createValidExpirationDate();

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				null, 
				validPassword, 
				validRIFUserRole, 
				validExpirationDate);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFDataLoaderToolError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}		
	}


	@Test
	/**
	 * empty password
	 */
	public void addUser_EMPTY2() {
		User validUser = cloneValidAdministrationUser();
		RIFUserRole validRIFUserRole = RIFUserRole.RIF_USER;
		Date validExpirationDate = createValidExpirationDate();

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				validUser, 
				emptyPassword, 
				validRIFUserRole, 
				validExpirationDate);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFDataLoaderToolError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}
	

	@Test
	/**
	 * null password
	 */
	public void addUser_NULL2() {
		User validUser = cloneValidAdministrationUser();
		RIFUserRole validRIFUserRole = RIFUserRole.RIF_USER;
		Date validExpirationDate = createValidExpirationDate();

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				validUser, 
				null, 
				validRIFUserRole, 
				validExpirationDate);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFDataLoaderToolError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
	}
	
	@Test
	/**
	 * null RIF user role
	 */
	public void addUser_NULL3() {
		
		User validAdministrationUser
			= cloneValidAdministrationUser();
		Date validExpirationDate = createValidExpirationDate();

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				validAdministrationUser, 
				validPassword, 
				null, 
				validExpirationDate);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFDataLoaderToolError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}
		
	}
	
	/**
	 * null expiration date
	 */
	public void addUser_NULL4() {
		
		User validAdministrationUser
			= cloneValidAdministrationUser();
		RIFUserRole validRIFUserRole = RIFUserRole.RIF_USER;

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				validAdministrationUser, 
				validPassword, 
				validRIFUserRole, 
				null);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFDataLoaderToolError.EMPTY_API_METHOD_PARAMETER, 
				1);
		}		
	}
	
	
	@Test
	/**
	 * an invalid user, perhaps with a userID that starts with a number
	 */
	public void addUser_INVALID1() {
		
		User invalidAdministrationUser
			= cloneInvalidAdministrationUser();
		RIFUserRole validRIFUserRole = RIFUserRole.RIF_USER;
		Date validExpirationDate = createValidExpirationDate();

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				invalidAdministrationUser, 
				validPassword, 
				validRIFUserRole, 
				validExpirationDate);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER, 
				1);
		}		
	}
	
	@Test
	/**
	 * 
	 */
	public void addUser_INVALID2() {
		
		User validAdministrationUser
			= cloneValidAdministrationUser();
		RIFUserRole validRIFUserRole = RIFUserRole.RIF_USER;
		Date invalidExpirationDate = createInvalidExpirationDate();

		DataLoaderService dataLoaderService = getDataLoaderService();		
		
		try {
			dataLoaderService.addUser(
				validAdministrationUser, 
				validPassword, 
				validRIFUserRole, 
				invalidExpirationDate);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_USER, 
				1);
		}		
	}
	

	
	
	
	
	@Test
	public void addUser_MALICIOUS1() {
		
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

}


