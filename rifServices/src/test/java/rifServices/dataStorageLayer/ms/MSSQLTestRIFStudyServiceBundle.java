package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.common.ServiceResources;
import rifServices.dataStorageLayer.common.StudyServiceBundle;

public final class MSSQLTestRIFStudyServiceBundle extends StudyServiceBundle {

	public MSSQLTestRIFStudyServiceBundle(final ServiceResources resources, RIFStudySubmissionAPI
			submission,  RIFStudyResultRetrievalAPI retrieval) {

		super(resources, submission, retrieval);
	}
	
	public ServiceResources getRIFServiceResources() {
		return super.getRIFServiceResources();
	}

	public void deregisterAllUsers() 
		throws RIFServiceException {

		super.deregisterAllUsers();
	}
}
