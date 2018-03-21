package rifServices.studyDataExtraction.pg;

import java.util.Locale;

import org.junit.Test;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.pg.PGSQLTestRIFStudyServiceBundle;
import rifServices.dataStorageLayer.pg.PGSQLTestRIFStudySubmissionService;
import rifServices.test.services.pg.AbstractRIFServiceTestCase;

public final class StudySubmissionServiceTest extends AbstractRIFServiceTestCase {

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
