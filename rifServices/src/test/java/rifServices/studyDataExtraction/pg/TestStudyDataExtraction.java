package rifServices.studyDataExtraction.pg;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.test.services.pg.AbstractRIFServiceTestCase;


import rifServices.dataStorageLayer.pg.PGSQLTestRIFStudyServiceBundle;
import rifServices.dataStorageLayer.pg.PGSQLTestRIFStudySubmissionService;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.Locale;

public final class TestStudyDataExtraction extends AbstractRIFServiceTestCase {

	public TestStudyDataExtraction() {
	}

	@Test
	public void testExtract1() throws RIFServiceException {

		User validUser = cloneValidUser();
		String validStudyID = "45";
		String validZoomLevel = "9";
		
		PGSQLTestRIFStudyServiceBundle testRIFStudyServiceBundle
			= getRIFServiceBundle();
		
		PGSQLTestRIFStudySubmissionService testSubmissionService
			= (PGSQLTestRIFStudySubmissionService) testRIFStudyServiceBundle.getRIFStudySubmissionService();
	
		testSubmissionService.createStudyExtract(
			validUser,
			validStudyID,
			validZoomLevel,
			Locale.getDefault(),
			"");
	}
}
