package rifServices.dataStorageLayer.pg;

import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.common.CovariateManager;
import rifServices.dataStorageLayer.common.DiseaseMappingStudyManager;
import rifServices.dataStorageLayer.common.HealthOutcomeManager;
import rifServices.dataStorageLayer.common.MapDataManager;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.dataStorageLayer.common.ResultsQueryManager;
import rifServices.dataStorageLayer.common.ServiceResources;
import rifServices.dataStorageLayer.common.SmoothedResultManager;
import rifServices.dataStorageLayer.common.StudyExtractManager;
import rifServices.dataStorageLayer.common.StudyStateManager;
import rifServices.dataStorageLayer.common.SubmissionManager;
import rifServices.system.RIFServiceStartupOptions;

public final class PGSQLRIFServiceResources implements ServiceResources {
	
	/**
	 * The health outcome manager.
	 */
	private HealthOutcomeManager healthOutcomeManager;
	
	/**
	 * The covariate manager.
	 */
	private CovariateManager sqlCovariateManager;
	
	/**
	 * The sql connection manager.
	 */
	private PGSQLConnectionManager sqlConnectionManager;
	
	/**
	 * The sql rif context manager.
	 */
	private RIFContextManager sqlRIFContextManager;
	
	private SmoothedResultManager sqlSmoothedResultManager;
	
	/**
	 * The sql age gender year manager.
	 */
	private AgeGenderYearManager sqlAgeGenderYearManager;
	
	/**
	 * The sql map data manager.
	 */
	private MapDataManager sqlMapDataManager;
	
	/**
	 * The disease mapping study manager.
	 */
	private PGSQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager;
	
	private SubmissionManager sqlRIFSubmissionManager;
	
	private StudyExtractManager sqlStudyExtractManager;
	
	private ResultsQueryManager sqlResultsQueryManager;
	
	private StudyStateManager sqlStudyStateManager;
	
	private RIFServiceStartupOptions rifServiceStartupOptions;
	
	public static ServiceResources newInstance(RIFServiceStartupOptions rifStartupOptions)
			throws RIFServiceException {
		
		return new PGSQLRIFServiceResources(rifStartupOptions);
	}
	
	public static ServiceResources newInstance(
			boolean isWebDeployment,
			boolean useStrictValidationPolicy)
			throws RIFServiceException {
		
		RIFServiceStartupOptions rifServiceStartupOptions
				= RIFServiceStartupOptions.newInstance(
				isWebDeployment,
				useStrictValidationPolicy);
		
		return new PGSQLRIFServiceResources(rifServiceStartupOptions);
	}
	
	public PGSQLRIFServiceResources(final RIFServiceStartupOptions rifServiceStartupOptions)
			throws RIFServiceException {
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		sqlConnectionManager = new PGSQLConnectionManager(rifServiceStartupOptions);
		healthOutcomeManager = new PGSQLHealthOutcomeManager(rifServiceStartupOptions);
		
		RIFDatabaseProperties rifDatabaseProperties
				= rifServiceStartupOptions.getRIFDatabaseProperties();
		
		sqlRIFContextManager
				= new PGSQLRIFContextManager(rifDatabaseProperties);
		
		sqlSmoothedResultManager
				= new PGSQLSmoothedResultManager(rifDatabaseProperties);
		
		sqlAgeGenderYearManager
				= new PGSQLAgeGenderYearManager(
				rifDatabaseProperties,
				sqlRIFContextManager);
		sqlMapDataManager
				= new PGSQLMapDataManager(
				rifServiceStartupOptions,
				sqlRIFContextManager);
		sqlCovariateManager
				= new PGSQLCovariateManager(
				rifDatabaseProperties,
				sqlRIFContextManager);
		
		PGSQLInvestigationManager sqlInvestigationManager = new PGSQLInvestigationManager(
				rifDatabaseProperties,
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				sqlCovariateManager,
				healthOutcomeManager);
		
		sqlDiseaseMappingStudyManager
				= new PGSQLDiseaseMappingStudyManager(
				rifDatabaseProperties,
				sqlRIFContextManager,
				sqlInvestigationManager);
		
		sqlStudyStateManager
				= new PGSQLStudyStateManager(rifDatabaseProperties);
		
		sqlRIFSubmissionManager
				= new PGSQLRIFSubmissionManager(
				rifDatabaseProperties,
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				sqlCovariateManager,
				sqlDiseaseMappingStudyManager,
				sqlMapDataManager,
				sqlStudyStateManager);
		
		sqlStudyExtractManager
				= new PGSQLStudyExtractManager(
				rifServiceStartupOptions);
		
		sqlResultsQueryManager
				= new PGSQLResultsQueryManager(
				rifDatabaseProperties,
				sqlRIFContextManager,
				sqlMapDataManager,
				sqlDiseaseMappingStudyManager);
		
		ValidationPolicy validationPolicy;
		if (rifServiceStartupOptions.useStrictValidationPolicy()) {
			validationPolicy = ValidationPolicy.STRICT;
		} else {
			validationPolicy = ValidationPolicy.RELAXED;
		}
		
		sqlCovariateManager.setValidationPolicy(validationPolicy);
		sqlRIFContextManager.setValidationPolicy(validationPolicy);
		sqlAgeGenderYearManager.setValidationPolicy(validationPolicy);
		sqlMapDataManager.setValidationPolicy(validationPolicy);
		sqlDiseaseMappingStudyManager.setValidationPolicy(validationPolicy);
		sqlRIFContextManager.setValidationPolicy(validationPolicy);
		sqlRIFSubmissionManager.setValidationPolicy(validationPolicy);
		sqlStudyExtractManager.setValidationPolicy(validationPolicy);
		sqlResultsQueryManager.setValidationPolicy(validationPolicy);
		sqlRIFContextManager.setValidationPolicy(validationPolicy);
		sqlInvestigationManager.setValidationPolicy(validationPolicy);
		
		healthOutcomeManager.initialiseTaxomies();
	}
	
	@Override
	public RIFServiceStartupOptions getRIFServiceStartupOptions() {
		
		return rifServiceStartupOptions;
	}
	
	@Override
	public PGSQLConnectionManager getSqlConnectionManager() {
		
		return sqlConnectionManager;
	}
	
	@Override
	public RIFContextManager getSQLRIFContextManager() {
		
		return sqlRIFContextManager;
	}
	
	@Override
	public SmoothedResultManager getSQLSmoothedResultManager() {
		
		return sqlSmoothedResultManager;
	}
	
	@Override
	public AgeGenderYearManager getSqlAgeGenderYearManager() {
		
		return sqlAgeGenderYearManager;
	}
	
	@Override
	public CovariateManager getSqlCovariateManager() {
		
		return sqlCovariateManager;
	}
	
	@Override
	public DiseaseMappingStudyManager getSqlDiseaseMappingStudyManager() {
		
		return sqlDiseaseMappingStudyManager;
	}
	
	@Override
	public ResultsQueryManager getSqlResultsQueryManager() {
		
		return sqlResultsQueryManager;
	}
	
	@Override
	public HealthOutcomeManager getHealthOutcomeManager() {
		
		return healthOutcomeManager;
	}
	
	@Override
	public StudyStateManager getStudyStateManager() {
		
		return sqlStudyStateManager;
	}
	
	@Override
	public SubmissionManager getRIFSubmissionManager() {
		
		return sqlRIFSubmissionManager;
	}
	
	@Override
	public StudyExtractManager getSQLStudyExtractManager() {
		
		return sqlStudyExtractManager;
	}
	
	@Override
	public MapDataManager getSQLMapDataManager() {
		
		return this.sqlMapDataManager;
	}
}

