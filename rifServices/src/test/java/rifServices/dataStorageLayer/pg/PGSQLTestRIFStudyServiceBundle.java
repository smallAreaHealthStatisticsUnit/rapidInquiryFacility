package rifServices.dataStorageLayer.pg;

import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.common.ServiceResources;
import rifServices.dataStorageLayer.common.StudyServiceBundle;

public final class PGSQLTestRIFStudyServiceBundle extends StudyServiceBundle {

	public PGSQLTestRIFStudyServiceBundle(final ServiceResources resources,
			RIFStudySubmissionAPI submission,  RIFStudyResultRetrievalAPI retrieval) {

		super(resources, submission, retrieval);
		PGSQLTestRIFStudySubmissionService rifStudySubmissionService
			= new PGSQLTestRIFStudySubmissionService();
		setRIFStudySubmissionService(rifStudySubmissionService);

		PGSQLTestRIFStudyRetrievalService rifStudyRetrievalService
			= new PGSQLTestRIFStudyRetrievalService();
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
