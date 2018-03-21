package rifServices.studyDataExtraction.ms;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.common.StudyExtract;
import rifServices.dataStorageLayer.ms.MSSQLConnectionManager;
import rifServices.dataStorageLayer.ms.MSSQLRIFServiceResources;
import rifServices.dataStorageLayer.ms.MSSQLRIFSubmissionManager;
import rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager;
import rifServices.test.services.ms.AbstractRIFServiceTestCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StudySubmissionServiceTest extends AbstractRIFServiceTestCase {

	@Mock
	private MSSQLRIFServiceResources resources;
	
	@Mock
	private	MSSQLConnectionManager sqlMgr;
	
	@Mock
	private	MSSQLRIFSubmissionManager subMgr;
	
	@Mock
	private MSSQLStudyExtractManager extractMgr;
	
	@Before
	public void setup() {
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void createStudyExtract_managerMethodCalled() throws RIFServiceException {

		User validUser = cloneValidUser();
		String validStudyID = "75";
		String validZoomLevel = "9";
		
		when(resources.getSqlConnectionManager()).thenReturn(sqlMgr);
		when(sqlMgr.userExists(validUser.getUserID())).thenReturn(true);
		when(resources.getRIFSubmissionManager()).thenReturn(subMgr);
		when(resources.getSQLStudyExtractManager()).thenReturn(extractMgr);
		
		new StudyExtract(validUser, validStudyID, validZoomLevel, Locale.getDefault(),
				"", resources).create();
		verify(extractMgr).createStudyExtract(any(), any(User.class), any(),
				eq(validZoomLevel), eq(validStudyID), eq(Locale.getDefault()), eq(""));
	}
}
