package rifServices.dataStorageLayer.common;

import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.ms.MSSQLProductionRIFStudyRetrievalService;
import rifServices.dataStorageLayer.ms.MSSQLProductionRIFStudyServiceBundle;
import rifServices.dataStorageLayer.ms.MSSQLRIFStudySubmissionService;
// import rifServices.dataStorageLayer.ms.
import rifServices.dataStorageLayer.pg.PGSQLProductionRIFStudyRetrievalService;
import rifServices.dataStorageLayer.pg.PGSQLProductionRIFStudyServiceBundle;

public class ServiceBundleFactory {

	public static ServiceBundle getInstance(final ServiceResources resources) {

		switch (resources.getRIFServiceStartupOptions().getRifDatabaseType()) {

			case POSTGRESQL:
				RIFStudySubmissionAPI pgSubmission =
						new MSSQLRIFStudySubmissionService();
				RIFStudyResultRetrievalAPI pgRetrieval =
						new PGSQLProductionRIFStudyRetrievalService();
				return new PGSQLProductionRIFStudyServiceBundle(
						resources.getRIFServiceStartupOptions(),
						pgSubmission,
						pgRetrieval);

			case SQL_SERVER:
				RIFStudySubmissionAPI msSubmission =
						new MSSQLRIFStudySubmissionService();
				RIFStudyResultRetrievalAPI msRetrieval =
						new MSSQLProductionRIFStudyRetrievalService();
				return new MSSQLProductionRIFStudyServiceBundle(
						resources, msSubmission, msRetrieval);

			case UNKNOWN:
			default:
				throw new IllegalStateException(
						"Unknown database type: "
						+ resources.getRIFServiceStartupOptions().getRifDatabaseType());
		}
	}
}
