package rifServices.dataStorageLayer.common;

import java.util.Locale;

import org.junit.Test;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.test.services.ms.AbstractRIFServiceTestCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public final class StudySubmissionServiceTest extends AbstractRIFServiceTestCase {

	@Test
	public void createStudyExtract_managerMethodCalled() throws RIFServiceException {

		User validUser = cloneValidUser();
		String validStudyID = "75";
		String validZoomLevel = "9";
		
		new StudyExtract(validUser, validStudyID, validZoomLevel, Locale.getDefault(),
				"", resources).create();
		verify(extractMgr).createStudyExtract(any(), any(User.class), any(),
				eq(validZoomLevel), eq(validStudyID), eq(Locale.getDefault()), eq(""));
	}
}
