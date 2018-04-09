package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.system.RIFServiceException;
import rifServices.dataStorageLayer.common.ServiceResources;

public final class MSSQLTestRIFStudyServiceBundle extends MSSQLAbstractStudyServiceBundle {

	private static final MSSQLTestRIFStudyServiceBundle rifStudyServiceBundle
		= new MSSQLTestRIFStudyServiceBundle();

	public MSSQLTestRIFStudyServiceBundle() {
		MSSQLRIFStudySubmissionService rifStudySubmissionService
			= new MSSQLRIFStudySubmissionService();
		setRIFStudySubmissionService(rifStudySubmissionService);

		MSSQLTestRIFStudyRetrievalService rifStudyRetrievalService
			= new MSSQLTestRIFStudyRetrievalService();
		setRIFStudyRetrievalService(rifStudyRetrievalService);		
	}
	
	public ServiceResources getRIFServiceResources() {
		return super.getRIFServiceResources();
	}

	public void deregisterAllUsers() 
		throws RIFServiceException {

		super.deregisterAllUsers();
	}
}
