package rifServices.dataStorageLayer.common;

import rifServices.dataStorageLayer.ms.MSSQLRIFServiceResources;
import rifServices.dataStorageLayer.pg.PGSQLRIFServiceResources;
import rifServices.system.RIFServiceStartupOptions;

public interface ServiceResources {

	static ServiceResources getInstance(RIFServiceStartupOptions options) {

		switch (options.getRifDatabaseType()) {

			case POSTGRESQL:
				return PGSQLRIFServiceResources.newInstance(options);

			case SQL_SERVER:
				return MSSQLRIFServiceResources.newInstance(options);

			case UNKNOWN:
			default:
				throw new IllegalStateException("Unknown database type: " + options.getRifDatabaseType());
		}
	}

	RIFServiceStartupOptions getRIFServiceStartupOptions();
	
	SQLManager getSqlConnectionManager();
	
	RIFContextManager getSQLRIFContextManager();
	
	SmoothedResultManager getSQLSmoothedResultManager();
	
	AgeGenderYearManager getSqlAgeGenderYearManager();
	
	CovariateManager getSqlCovariateManager();
	
	DiseaseMappingStudyManager getSqlDiseaseMappingStudyManager();
	
	ResultsQueryManager getSqlResultsQueryManager();
	
	HealthOutcomeManager getHealthOutcomeManager();
	
	StudyStateManager getStudyStateManager();
	
	SubmissionManager getRIFSubmissionManager();
	
	StudyExtractManager getSQLStudyExtractManager();
	
	MapDataManager getSQLMapDataManager();
}
