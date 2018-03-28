package rifServices.dataStorageLayer.common;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;

public interface ServiceBundle { RIFStudyResultRetrievalAPI getRIFStudyRetrievalService();
	
	RIFStudySubmissionAPI getRIFStudySubmissionService();
	
	void login(String userID, String password) throws RIFServiceException;
	
	boolean isLoggedIn(String userID) throws RIFServiceException;
	
	void logout(User user) throws RIFServiceException;
}
