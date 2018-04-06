package rifServices.test;

import java.util.ArrayList;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.FieldValidationUtility;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;

import static org.junit.Assert.assertEquals;

public class AbstractRIFTestCase {

	/** The test malicious field value. */
	private String testMaliciousFieldValue;
	
	private ValidationPolicy validationPolicy = ValidationPolicy.STRICT;

	/**
	 * Instantiates a new abstract rif test case.
	 */
	public AbstractRIFTestCase() {
		FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();
		testMaliciousFieldValue = fieldValidationUtility.getTestMaliciousFieldValue();
	}

	public ValidationPolicy getValidationPolicy() {
		return validationPolicy;
	}
	
	/**
	 * Check error type.
	 *
	 * @param rifServiceException the rif service exception
	 * @param expectedRIFServiceErrorType the expected rif service error type
	 * @param expectedNumberOfErrors the expected number of errors
	 */
	protected void checkErrorType(
		RIFServiceException rifServiceException,
		Object expectedRIFServiceErrorType,
		int expectedNumberOfErrors) {
			
		Object actualRIFServiceErrorType
			= rifServiceException.getError();
		assertEquals(expectedRIFServiceErrorType, actualRIFServiceErrorType);
		int actualNumberOfErrorMessages 
			= rifServiceException.getErrorMessageCount();
		assertEquals(expectedNumberOfErrors, actualNumberOfErrorMessages);
	}	
	
	/**
	 * Prints the errors.
	 *
	 * @param label the label
	 * @param rifServiceException the rif service exception
	 */
	protected void printErrors(
		String label,
		RIFServiceException rifServiceException) {
		
		ArrayList<String> errorMessages 
			= rifServiceException.getErrorMessages();
		for (String errorMessage : errorMessages) {
			System.out.println(label + "=="+errorMessage+"==");
		}
	}
	
	/**
	 * Gets the test malicious value.
	 *
	 * @return the test malicious value
	 */
	public String getTestMaliciousValue() {
		return testMaliciousFieldValue;
	}
}
