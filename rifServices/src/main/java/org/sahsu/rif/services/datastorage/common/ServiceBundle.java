package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.RIFStudyResultRetrievalAPI;
import org.sahsu.rif.services.concepts.RIFStudySubmissionAPI;

public interface ServiceBundle {

	static ServiceBundle getInstance(final ServiceResources resources) {

		return new StudyServiceBundle(
				resources, new StudySubmissionService(), new ProductionStudyRetrievalService());
	}

	RIFStudyResultRetrievalAPI getRIFStudyRetrievalService();
	
	RIFStudySubmissionAPI getRIFStudySubmissionService();
	
	void login(String userID, String password) throws RIFServiceException;
	
	boolean isLoggedIn(String userID) throws RIFServiceException;
	
	void logout(User user) throws RIFServiceException;
}
