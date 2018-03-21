package rifServices.studyDataExtraction.ms;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.ms.MSSQLConnectionManager;
import rifServices.dataStorageLayer.ms.MSSQLRIFServiceResources;
import rifServices.dataStorageLayer.ms.MSSQLRIFStudySubmissionService;
import rifServices.dataStorageLayer.ms.MSSQLRIFSubmissionManager;
import rifServices.dataStorageLayer.ms.MSSQLStudyExtractManager;
import rifServices.dataStorageLayer.ms.MSSQLTestRIFStudyServiceBundle;
import rifServices.test.services.ms.AbstractRIFServiceTestCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
	public void testExtract1() throws RIFServiceException {

		User validUser = cloneValidUser();
		String validStudyID = "75";
		String validZoomLevel = "9";
		
		MSSQLTestRIFStudyServiceBundle testRIFStudyServiceBundle
			= getRIFServiceBundle();
		
		MSSQLRIFStudySubmissionService testSubmissionService
			= (MSSQLRIFStudySubmissionService) testRIFStudyServiceBundle.getRIFStudySubmissionService();

		when(resources.getSqlConnectionManager()).thenReturn(sqlMgr);
		when(sqlMgr.userExists(validUser.getUserID())).thenReturn(true);
		when(resources.getRIFSubmissionManager()).thenReturn(subMgr);
		when(resources.getSQLStudyExtractManager()).thenReturn(extractMgr);
		
		testSubmissionService.initialise(resources);
		testSubmissionService.createStudyExtract(
			validUser,
			validStudyID,
			validZoomLevel,
			Locale.getDefault(),
			"");
		verify(extractMgr).createStudyExtract(any(), any(), any(), any(), any(), any(),
				anyString());
		
	}
}
