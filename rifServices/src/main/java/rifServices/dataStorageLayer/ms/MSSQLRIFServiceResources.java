package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.dataStorageLayer.common.BaseSQLManager;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
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
import rifServices.dataStorageLayer.common.SubmissionManager;
import rifServices.system.RIFServiceStartupOptions;

public class MSSQLRIFServiceResources implements ServiceResources {

	/** The health outcome manager. */
	private HealthOutcomeManager healthOutcomeManager;
	
	/** The covariate manager. */
	private CovariateManager sqlCovariateManager;
	
	/** The sql connection manager. */
	private BaseSQLManager sqlConnectionManager;
	
	/** The sql rif context manager. */
	private RIFContextManager sqlRIFContextManager;
	
	private SmoothedResultManager sqlSmoothedResultManager;
	
	/** The sql age gender year manager. */
	private AgeGenderYearManager sqlAgeGenderYearManager;
	
	/** The sql map data manager. */
	private MapDataManager sqlMapDataManager;
	
	/** The disease mapping study manager. */
	private DiseaseMappingStudyManager sqlDiseaseMappingStudyManager;
	
	private SubmissionManager sqlRIFSubmissionManager;
	
	private StudyExtractManager sqlStudyExtractManager;
	
	private ResultsQueryManager sqlResultsQueryManager;
	
	private rifServices.dataStorageLayer.common.StudyStateManager sqlStudyStateManager;
	
	private RIFServiceStartupOptions rifServiceStartupOptions;
	
	private MSSQLRIFServiceResources(final RIFServiceStartupOptions rifServiceStartupOptions) {
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		sqlConnectionManager = new BaseSQLManager(rifServiceStartupOptions);
		healthOutcomeManager = new MSSQLHealthOutcomeManager(rifServiceStartupOptions);

		RIFDatabaseProperties rifDatabaseProperties
			= rifServiceStartupOptions.getRIFDatabaseProperties();
		
		sqlRIFContextManager 
			= new MSSQLRIFContextManager(rifServiceStartupOptions);
		
		sqlSmoothedResultManager
			= new MSSQLSmoothedResultManager(rifServiceStartupOptions);
		
		sqlAgeGenderYearManager = new AgeGenderYearManager(
				sqlRIFContextManager, rifServiceStartupOptions);
		sqlMapDataManager 
			= new MSSQLMapDataManager(
				rifServiceStartupOptions
		);
		sqlCovariateManager = new CovariateManager(rifServiceStartupOptions,
		                                           sqlRIFContextManager);
		
		InvestigationManager sqlInvestigationManager = InvestigationManager.getInstance(
				rifDatabaseProperties.getDatabaseType(),
				rifServiceStartupOptions,
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				sqlCovariateManager);
		
		sqlDiseaseMappingStudyManager 
			= new DiseaseMappingStudyManager(
				rifServiceStartupOptions,
				sqlRIFContextManager,
				sqlInvestigationManager);
				
		sqlStudyStateManager
			= new CommonStudyStateManager(rifServiceStartupOptions);
		
		sqlRIFSubmissionManager 
			= new MSSQLRIFSubmissionManager(
				rifServiceStartupOptions,
				sqlStudyStateManager);

		sqlStudyExtractManager
			= new MSSQLStudyExtractManager(
				rifServiceStartupOptions);
		
		sqlResultsQueryManager = new MSSQLResultsQueryManager(rifServiceStartupOptions);

		ValidationPolicy validationPolicy = null;
		if (rifServiceStartupOptions.useStrictValidationPolicy()) {
			validationPolicy = ValidationPolicy.STRICT;
		}
		else {
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
	
	public static MSSQLRIFServiceResources newInstance(
		final RIFServiceStartupOptions rifStartupOptions) {
		
		return new MSSQLRIFServiceResources(rifStartupOptions);
	}

	public static MSSQLRIFServiceResources newInstance(
		final boolean isWebDeployment,
		final boolean useStrictValidationPolicy)
		throws RIFServiceException {
			
		RIFServiceStartupOptions rifServiceStartupOptions
			= RIFServiceStartupOptions.newInstance(
				isWebDeployment,
				useStrictValidationPolicy);
		
		return new MSSQLRIFServiceResources(rifServiceStartupOptions);
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
	public rifServices.dataStorageLayer.common.StudyStateManager getStudyStateManager() {
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
