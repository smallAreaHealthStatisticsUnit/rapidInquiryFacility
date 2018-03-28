package rifServices.dataStorageLayer.common;

import rifServices.dataStorageLayer.pg.PGSQLConnectionManager;
import rifServices.system.RIFServiceStartupOptions;

public interface ServiceResources {
	
	RIFServiceStartupOptions getRIFServiceStartupOptions();
	
	AbstractSQLManager getSqlConnectionManager();
	
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
