package rifServices.studyDataExtraction.ms;

import java.util.Locale;

import org.junit.Test;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.ms.MSSQLRIFStudySubmissionService;
import rifServices.dataStorageLayer.ms.MSSQLTestRIFStudyServiceBundle;
import rifServices.test.services.ms.AbstractRIFServiceTestCase;

import static org.junit.Assert.fail;

public final class TestStudyDataExtraction extends AbstractRIFServiceTestCase {

	@Test
	public void testExtract1() {
		try {
			User validUser = cloneValidUser();
			String validStudyID = "75";
			String validZoomLevel = "9";
			
			MSSQLTestRIFStudyServiceBundle testRIFStudyServiceBundle
				= getRIFServiceBundle();
			
			MSSQLRIFStudySubmissionService testSubmissionService
				= (MSSQLRIFStudySubmissionService) testRIFStudyServiceBundle.getRIFStudySubmissionService();
		
			testSubmissionService.createStudyExtract(
				validUser, 
				validStudyID,
				validZoomLevel,
				Locale.getDefault(),
				"");
		}
		catch(RIFServiceException rifServiceException) {
			fail();
		}
	}
}
