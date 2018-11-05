package org.sahsu.rif.services.test;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FieldValidationUtility;
import org.sahsu.rif.services.concepts.AbstractRIFConcept.ValidationPolicy;
import org.sahsu.rif.services.concepts.RIFStudyResultRetrievalAPI;
import org.sahsu.rif.services.concepts.RIFStudySubmissionAPI;
import org.sahsu.rif.services.datastorage.common.SQLManager;
import org.sahsu.rif.services.datastorage.common.ServiceBundle;
import org.sahsu.rif.services.datastorage.common.ServiceResources;
import org.sahsu.rif.services.datastorage.common.StudyExtractManager;
import org.sahsu.rif.services.datastorage.common.StudySubmissionService;
import org.sahsu.rif.services.datastorage.common.SubmissionManager;
import org.sahsu.rif.services.datastorage.ms.MSSQLTestRIFStudyRetrievalService;
import org.sahsu.rif.services.datastorage.ms.MSSQLTestRIFStudyServiceBundle;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AbstractRIFTestCase {

	@Mock
	protected ServiceResources resources;

	@Mock
	private SQLManager sqlMgr;

	@Mock
	private SubmissionManager subMgr;

	@Mock
	protected StudyExtractManager extractMgr;

	@Mock
	private RIFServiceStartupOptions options;

	protected RIFStudySubmissionAPI rifStudySubmissionService;
	protected RIFStudyResultRetrievalAPI rifStudyRetrievalService;
	/** The test user. */
	protected User validUser;
	protected ServiceBundle rifServiceBundle;
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
	}

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);

		when(resources.getSqlConnectionManager()).thenReturn(sqlMgr);
		when(sqlMgr.userExists(validUser.getUserID())).thenReturn(true);
		when(resources.getRIFSubmissionManager()).thenReturn(subMgr);
		when(resources.getSQLStudyExtractManager()).thenReturn(extractMgr);
		when(resources.getRIFServiceStartupOptions()).thenReturn(options);
		when(options.getRifDatabaseType()).thenReturn(DatabaseType.POSTGRESQL); // Shouldn't matter.

		initialiseService(resources);
		rifServiceBundle = ServiceBundle.getInstance(resources);
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
	 * Gets the test malicious value.
	 *
	 * @return the test malicious value
	 */
	public String getTestMaliciousValue() {
		return testMaliciousFieldValue;
	}

	protected void initialiseService(ServiceResources resources) {

		rifServiceBundle = new MSSQLTestRIFStudyServiceBundle(
				resources,
				new StudySubmissionService(),
				new MSSQLTestRIFStudyRetrievalService());
		this.resources = resources;
		rifStudySubmissionService = rifServiceBundle.getRIFStudySubmissionService();
		rifStudyRetrievalService = rifServiceBundle.getRIFStudyRetrievalService();
	}
}
