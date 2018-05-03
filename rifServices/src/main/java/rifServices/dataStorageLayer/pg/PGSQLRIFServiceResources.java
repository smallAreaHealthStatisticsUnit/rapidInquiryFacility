package rifServices.dataStorageLayer.pg;

import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.common.BaseSQLManager;
import rifServices.dataStorageLayer.common.CommonHealthOutcomeManager;
import rifServices.dataStorageLayer.common.CommonStudyStateManager;
import rifServices.dataStorageLayer.common.CovariateManager;
import rifServices.dataStorageLayer.common.DiseaseMappingStudyManager;
import rifServices.dataStorageLayer.common.HealthOutcomeManager;
import rifServices.dataStorageLayer.common.InvestigationManager;
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
	private BaseSQLManager sqlConnectionManager;
	
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
	private DiseaseMappingStudyManager sqlDiseaseMappingStudyManager;
	
	private SubmissionManager sqlRIFSubmissionManager;
	
	private StudyExtractManager sqlStudyExtractManager;
	
	private ResultsQueryManager sqlResultsQueryManager;
	
	private StudyStateManager sqlStudyStateManager;
	
	private RIFServiceStartupOptions rifServiceStartupOptions;
	
	public static ServiceResources newInstance(RIFServiceStartupOptions rifStartupOptions) {
		
		return new PGSQLRIFServiceResources(rifStartupOptions);
	}
	
	public static ServiceResources newInstance(
			boolean isWebDeployment,
			boolean useStrictValidationPolicy) {
		
		RIFServiceStartupOptions rifServiceStartupOptions
				= RIFServiceStartupOptions.newInstance(
				isWebDeployment,
				useStrictValidationPolicy);
		
		return new PGSQLRIFServiceResources(rifServiceStartupOptions);
	}
	
	private PGSQLRIFServiceResources(final RIFServiceStartupOptions rifServiceStartupOptions) {
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		sqlConnectionManager = new BaseSQLManager(rifServiceStartupOptions);
		healthOutcomeManager = new CommonHealthOutcomeManager(rifServiceStartupOptions);
		
		RIFDatabaseProperties rifDatabaseProperties
				= rifServiceStartupOptions.getRIFDatabaseProperties();
		
		sqlRIFContextManager
				= new PGSQLRIFContextManager(rifServiceStartupOptions);
		
		sqlSmoothedResultManager
				= new PGSQLSmoothedResultManager(rifServiceStartupOptions);
		
		sqlAgeGenderYearManager = new AgeGenderYearManager(
				sqlRIFContextManager, rifServiceStartupOptions);
		sqlMapDataManager = new PGSQLMapDataManager(rifServiceStartupOptions, sqlRIFContextManager);
		sqlCovariateManager = new CovariateManager(rifServiceStartupOptions,
		                                           sqlRIFContextManager);
		
		InvestigationManager sqlInvestigationManager =
				new InvestigationManager(rifServiceStartupOptions, sqlRIFContextManager,
				                         sqlAgeGenderYearManager, sqlCovariateManager);
		
		sqlDiseaseMappingStudyManager = new DiseaseMappingStudyManager(rifServiceStartupOptions,
				sqlRIFContextManager, sqlInvestigationManager);
		
		sqlStudyStateManager
				= new CommonStudyStateManager(rifServiceStartupOptions);
		
		sqlRIFSubmissionManager = new PGSQLRIFSubmissionManager(rifServiceStartupOptions,
				sqlStudyStateManager);
		
		sqlStudyExtractManager
				= new PGSQLStudyExtractManager(
				rifServiceStartupOptions);
		
		sqlResultsQueryManager
				= new PGSQLResultsQueryManager(rifServiceStartupOptions);
		
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
	}
	
	@Override
	public RIFServiceStartupOptions getRIFServiceStartupOptions() {
		
		return rifServiceStartupOptions;
	}
	
	@Override
	public BaseSQLManager getSqlConnectionManager() {
		
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

