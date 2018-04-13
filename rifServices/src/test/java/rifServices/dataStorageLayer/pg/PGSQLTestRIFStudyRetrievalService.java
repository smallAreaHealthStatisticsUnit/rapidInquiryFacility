package rifServices.dataStorageLayer.pg;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.StudyState;
import rifServices.dataStorageLayer.common.StudyRetrievalService;

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
