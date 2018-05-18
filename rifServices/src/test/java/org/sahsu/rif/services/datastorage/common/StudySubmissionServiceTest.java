package org.sahsu.rif.services.datastorage.common;

import java.util.Locale;

import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public final class StudySubmissionServiceTest extends CommonRIFServiceTestCase {

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
