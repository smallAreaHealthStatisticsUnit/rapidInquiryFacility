package rifServices.dataStorageLayer.ms;

import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.common.ServiceResources;
import rifServices.dataStorageLayer.common.StudyServiceBundle;

public final class MSSQLProductionRIFStudyServiceBundle extends StudyServiceBundle {

	public MSSQLProductionRIFStudyServiceBundle(final ServiceResources resources,
			final RIFStudySubmissionAPI submissionService,
			final RIFStudyResultRetrievalAPI retrievalService) {

		super(resources, submissionService, retrievalService);
	}
}
