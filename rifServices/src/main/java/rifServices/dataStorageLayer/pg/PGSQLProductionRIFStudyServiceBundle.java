package rifServices.dataStorageLayer.pg;

import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.common.ProductionStudyRetrievalService;
import rifServices.dataStorageLayer.common.ServiceResources;
import rifServices.dataStorageLayer.common.StudyServiceBundle;

public final class PGSQLProductionRIFStudyServiceBundle
		extends StudyServiceBundle {

	public PGSQLProductionRIFStudyServiceBundle(final ServiceResources resources,
			final RIFStudySubmissionAPI submissionService,
			final RIFStudyResultRetrievalAPI retrievalService) {

		super(resources, submissionService, retrievalService);
		PGSQLProductionRIFStudySubmissionService rifStudySubmissionService
			= new PGSQLProductionRIFStudySubmissionService();
		setRIFStudySubmissionService(rifStudySubmissionService);

		ProductionStudyRetrievalService rifStudyRetrievalService
			= new ProductionStudyRetrievalService();
		setRIFStudyRetrievalService(rifStudyRetrievalService);
		
	}
}
