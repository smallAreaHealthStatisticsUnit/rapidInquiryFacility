package rifServices.dataStorageLayer.common;

import rifServices.businessConceptLayer.RIFStudyResultRetrievalAPI;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.ms.MSSQLProductionRIFStudyServiceBundle;
import rifServices.dataStorageLayer.ms.MSSQLRIFStudySubmissionService;
import rifServices.dataStorageLayer.pg.PGSQLProductionRIFStudyServiceBundle;

public class ServiceBundleFactory {

	public static ServiceBundle getInstance(final ServiceResources resources) {

		RIFStudyResultRetrievalAPI retrieval = new ProductionStudyRetrievalService();

		switch (resources.getRIFServiceStartupOptions().getRifDatabaseType()) {

			case POSTGRESQL:
				RIFStudySubmissionAPI pgSubmission =
						new MSSQLRIFStudySubmissionService();
				return new PGSQLProductionRIFStudyServiceBundle(
						resources.getRIFServiceStartupOptions(),
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
}
