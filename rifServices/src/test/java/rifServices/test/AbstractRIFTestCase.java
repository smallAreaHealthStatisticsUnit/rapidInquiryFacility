package rifServices.test;

import java.util.ArrayList;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.FieldValidationUtility;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.dataStorageLayer.common.SQLManager;
import rifServices.dataStorageLayer.common.ServiceResources;
import rifServices.dataStorageLayer.common.StudyExtractManager;
import rifServices.dataStorageLayer.common.SubmissionManager;
import rifServices.dataStorageLayer.ms.MSSQLRIFStudySubmissionService;
import rifServices.dataStorageLayer.ms.MSSQLTestRIFStudyRetrievalService;
import rifServices.dataStorageLayer.ms.MSSQLTestRIFStudyServiceBundle;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AbstractRIFTestCase {

	@Mock
	public ServiceResources resources;
	@Mock
	public SQLManager sqlMgr;
	@Mock
	public SubmissionManager subMgr;
	@Mock
	public StudyExtractManager extractMgr;
	protected MSSQLRIFStudySubmissionService rifStudySubmissionService;
	protected MSSQLTestRIFStudyRetrievalService rifStudyRetrievalService;
	/** The test user. */
	protected User validUser;
	public MSSQLTestRIFStudyServiceBundle rifServiceBundle;
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
		validUser = User.newInstance("kgarwood", "11.111.11.228");
		rifServiceBundle
			= new MSSQLTestRIFStudyServiceBundle();
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

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);

		when(resources.getSqlConnectionManager()).thenReturn(sqlMgr);
		when(sqlMgr.userExists(validUser.getUserID())).thenReturn(true);
		when(resources.getRIFSubmissionManager()).thenReturn(subMgr);
		when(resources.getSQLStudyExtractManager()).thenReturn(extractMgr);

		try {
			initialiseService(resources);
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}
	}

	public void initialiseService(ServiceResources resources) throws RIFServiceException {

		rifServiceBundle = new MSSQLTestRIFStudyServiceBundle();
		this.resources = resources;
		rifServiceBundle.initialise(resources);
		rifStudySubmissionService
			= (MSSQLRIFStudySubmissionService) rifServiceBundle.getRIFStudySubmissionService();
		rifStudyRetrievalService
			= (MSSQLTestRIFStudyRetrievalService) rifServiceBundle.getRIFStudyRetrievalService();
	}
}
