package rifServices.dataStorageLayer.common;

import rifServices.dataStorageLayer.ms.MSSQLProductionRIFStudyServiceBundle;
import rifServices.dataStorageLayer.pg.PGSQLProductionRIFStudyServiceBundle;

public class ServiceBundleFactory {

	public static ServiceBundle getInstance(final ServiceResources resources) {

		switch (resources.getRIFServiceStartupOptions().getRifDatabaseType()) {

			case POSTGRESQL:
				PGSQLProductionRIFStudyServiceBundle pgBundle =
						PGSQLProductionRIFStudyServiceBundle.getRIFServiceBundle();
				pgBundle.initialise(resources.getRIFServiceStartupOptions());
				return pgBundle;

			case SQL_SERVER:
				MSSQLProductionRIFStudyServiceBundle msBundle =
						MSSQLProductionRIFStudyServiceBundle.getRIFServiceBundle();
				msBundle.initialise(resources);
				return msBundle;

			case UNKNOWN:
			default:
				throw new IllegalStateException(
						"Unknown database type: "
						+ resources.getRIFServiceStartupOptions().getRifDatabaseType());
		}
	}
}
