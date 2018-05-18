package org.sahsu.rif.services.datastorage.pg;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.StudyState;
import org.sahsu.rif.services.datastorage.common.StudyRetrievalService;

public final class PGSQLTestRIFStudyRetrievalService extends StudyRetrievalService {

	public PGSQLTestRIFStudyRetrievalService() {

	}

	public void clearStudyStatusUpdates(
		final User user, 
		final String studyID) 
		throws RIFServiceException {
		
		super.clearStudyStatusUpdates(
			user, 
			studyID);
	}
	
	public void updateStudyStatus(
		final User user, 
		final String studyID, 
		final StudyState studyState,
		final String message)
		throws RIFServiceException {
	
		super.updateStudyStatus(
			user, 
			studyID, 
			studyState,
			message);
		
	}
}
