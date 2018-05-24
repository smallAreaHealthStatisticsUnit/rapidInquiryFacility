package org.sahsu.rif.services.test.services.ms;

import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.sahsu.rif.generic.system.RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER;

public final class IsInformationGovernancePolicyActive extends CommonRIFServiceTestCase {

	private String validStudyID;
	private String emptyStudyID;
	private String invalidStudyID;
	private String nonExistentStudyID;
	private String maliciousStudyID;
	
	public IsInformationGovernancePolicyActive() {
		FieldValidationUtility fieldValidiationUtility
			= new FieldValidationUtility();
		
		validStudyID = "123";
		emptyStudyID = "";
		invalidStudyID = "xefrgg";
		nonExistentStudyID = "-999";
		maliciousStudyID
			= fieldValidiationUtility.getTestMaliciousFieldValue();
	}

	@Test
	public void isInformationGovernancePolicyActive_COMMON1() throws RIFServiceException {

		//test check
		User validUser = cloneValidUser();

		//until the Information Governance Tool is developed, this will remain
		//false
		boolean isInformationGovernancePolicyActive =
				rifStudySubmissionService.isInformationGovernancePolicyActive(validUser);
		assertFalse(isInformationGovernancePolicyActive);
	}
	
	@Test
	public void isInformationGovernancePolicyActive_NULL1() {

		try {
			
			rifStudySubmissionService.isInformationGovernancePolicyActive(
				null);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				EMPTY_API_METHOD_PARAMETER,
				1);
		}
	}
		
	@Test
	public void getStatusUpdates_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			rifStudySubmissionService.isInformationGovernancePolicyActive(emptyUser);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.INVALID_USER, 
				1);
		}	
	}

	@Test
	/**
	 * check malicious parameters
	 */
	public void getGeographies_NONEXISTENT1() {
		try {
			//test check
			User nonExistentUser = cloneNonExistentUser();
			rifStudySubmissionService.isInformationGovernancePolicyActive(nonExistentUser);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION, 
				1);
		}
	}
		

	@Test
	/**
	 * check malicious parameters
	 */
	public void getGeographies_MALICIOUS1() {
		try {
			//test check
			User maliciousUser = cloneMaliciousUser();
			rifStudySubmissionService.isInformationGovernancePolicyActive(maliciousUser);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION, 
				1);
		}			
	}
}
