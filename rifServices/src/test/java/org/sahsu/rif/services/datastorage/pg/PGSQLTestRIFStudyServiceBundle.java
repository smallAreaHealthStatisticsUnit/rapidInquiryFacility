package org.sahsu.rif.services.datastorage.pg;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.RIFStudyResultRetrievalAPI;
import org.sahsu.rif.services.concepts.RIFStudySubmissionAPI;
import org.sahsu.rif.services.datastorage.common.ServiceResources;
import org.sahsu.rif.services.datastorage.common.StudyServiceBundle;

public final class PGSQLTestRIFStudyServiceBundle extends StudyServiceBundle {

	public PGSQLTestRIFStudyServiceBundle(final ServiceResources resources,
			RIFStudySubmissionAPI submission,  RIFStudyResultRetrievalAPI retrieval) {

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
