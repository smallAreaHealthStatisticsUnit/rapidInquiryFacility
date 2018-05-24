package org.sahsu.rif.services.test.services;

import java.util.ArrayList;

import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.CalculationMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.sahsu.rif.generic.system.RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER;

public final class GetAvailableCalculationMethods extends CommonRIFServiceTestCase {

	public GetAvailableCalculationMethods() {

	}

	@Test
	public void getAvailableCalculationMethods_COMMON1() throws RIFServiceException {

		User validUser = cloneValidUser();
		ArrayList<CalculationMethod> calculationMethods
				= rifStudySubmissionService.getAvailableCalculationMethods(validUser);
		assertEquals(3, calculationMethods.size());
	}

	@Test
	public void getAvailableCalculationMethods_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			rifStudySubmissionService.getAvailableCalculationMethods(emptyUser);
			fail();
		} catch (RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFGenericLibraryError.INVALID_USER,
					1);
		}
	}

	@Test
	public void getAvailableCalculationMethods_NULL1() {
		try {
			rifStudySubmissionService.getAvailableCalculationMethods(null);
			fail();
		} catch (RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					EMPTY_API_METHOD_PARAMETER,
					1);
		}
	}

	@Test
	public void getAvailableCalculationMethods_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			rifStudySubmissionService.getAvailableCalculationMethods(nonExistentUser);
			fail();
		} catch (RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFGenericLibraryError.SECURITY_VIOLATION,
					1);
		}
	}

	@Test
	public void getAvailableCalculationMethods_MALICIOUS1() {
		try {
			User maliciousUser = cloneMaliciousUser();
			rifStudySubmissionService.getAvailableCalculationMethods(maliciousUser);
			fail();
		} catch (RIFServiceException rifServiceException) {
			checkErrorType(
					rifServiceException,
					RIFGenericLibraryError.SECURITY_VIOLATION,
					1);
		}
	}
}
