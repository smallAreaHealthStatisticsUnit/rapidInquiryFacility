package rifServices.dataStorageLayer.pg;

import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.common.ServiceResources;
import rifServices.system.RIFServiceStartupOptions;

public final class PGSQLTestRIFStudyServiceBundle extends PGSQLAbstractStudyServiceBundle {

	public PGSQLTestRIFStudyServiceBundle(final RIFServiceStartupOptions options,
			RIFStudySubmissionAPI submission,  RIFStudyResultRetrievalAPI retrieval) {

		super(options, submission, retrieval);
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
