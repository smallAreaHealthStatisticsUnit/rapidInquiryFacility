package rifServices.dataStorageLayer.common;

import java.sql.Connection;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.Investigation;
import rifServices.dataStorageLayer.ms.MSSQLInvestigationManager;
import rifServices.dataStorageLayer.pg.PGSQLInvestigationManager;
import rifServices.system.RIFServiceStartupOptions;

public interface InvestigationManager extends SQLManager {

	static InvestigationManager getInstance(
			final DatabaseType type,
			final RIFServiceStartupOptions startupOptions,
			final RIFContextManager rifContextManager,
			final AgeGenderYearManager ageGenderYearManager,
			final CovariateManager covariateManager) {

		switch (type) {
			case SQL_SERVER:
				return new MSSQLInvestigationManager(startupOptions, rifContextManager,
				                                     ageGenderYearManager, covariateManager);
			case POSTGRESQL:
				return new PGSQLInvestigationManager(startupOptions, rifContextManager,
				                                     ageGenderYearManager, covariateManager);
			case UNKNOWN:
			default:
				throw new IllegalStateException("InvestigationManager.getInstance: unknown "
				                                + "database type.");
		}
	}
	
	void checkNonExistentItems(
			User user,
			Connection connection,
			Geography geography,
			GeoLevelToMap geoLevelToMap,
			Investigation investigation)
		throws RIFServiceException;

}
