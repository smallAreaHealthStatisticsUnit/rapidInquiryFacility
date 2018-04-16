package rifServices.dataStorageLayer.common;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.ms.MSSQLProductionRIFStudyServiceBundle;
import rifServices.dataStorageLayer.ms.MSSQLRIFStudySubmissionService;
import rifServices.dataStorageLayer.pg.PGSQLProductionRIFStudyServiceBundle;
import rifServices.dataStorageLayer.pg.PGSQLRIFStudySubmissionService;

public interface ServiceBundle {

	static ServiceBundle getInstance(final ServiceResources resources) {

		RIFStudyResultRetrievalAPI retrieval = new ProductionStudyRetrievalService();

		switch (resources.getRIFServiceStartupOptions().getRifDatabaseType()) {

			case POSTGRESQL:
				RIFStudySubmissionAPI pgSubmission =
						new PGSQLRIFStudySubmissionService();
				return new PGSQLProductionRIFStudyServiceBundle(
						resources,
						pgSubmission,
						retrieval);

			case SQL_SERVER:
				RIFStudySubmissionAPI msSubmission =
						new MSSQLRIFStudySubmissionService();
				return new MSSQLProductionRIFStudyServiceBundle(
						resources, msSubmission, retrieval);

			case UNKNOWN:
			default:
				throw new IllegalStateException(
						"Unknown database type: "
						+ resources.getRIFServiceStartupOptions().getRifDatabaseType());
		}
	}

	RIFStudyResultRetrievalAPI getRIFStudyRetrievalService();
	
	RIFStudySubmissionAPI getRIFStudySubmissionService();
	
	void login(String userID, String password) throws RIFServiceException;
	
	boolean isLoggedIn(String userID) throws RIFServiceException;
	
	void logout(User user) throws RIFServiceException;
}
