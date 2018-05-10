package rifServices.dataStorageLayer.common;

import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.system.RIFServiceStartupOptions;

public class ServiceResources {

	private HealthOutcomeManager healthOutcomeManager;
	private CovariateManager sqlCovariateManager;
	private BaseSQLManager sqlConnectionManager;
	private RIFContextManager sqlRIFContextManager;
	private SmoothedResultManager sqlSmoothedResultManager;
	private AgeGenderYearManager sqlAgeGenderYearManager;
	private MapDataManager sqlMapDataManager;
	private DiseaseMappingStudyManager sqlDiseaseMappingStudyManager;
	private SubmissionManager sqlRIFSubmissionManager;
	private rifServices.dataStorageLayer.common.StudyExtractManager sqlStudyExtractManager;
	private ResultsQueryManager sqlResultsQueryManager;
	private rifServices.dataStorageLayer.common.StudyStateManager sqlStudyStateManager;
	private RIFServiceStartupOptions rifServiceStartupOptions;

	private ServiceResources(final RIFServiceStartupOptions rifServiceStartupOptions) {

		this.rifServiceStartupOptions = rifServiceStartupOptions;

		sqlConnectionManager = new BaseSQLManager(rifServiceStartupOptions);
		healthOutcomeManager = new CommonHealthOutcomeManager(rifServiceStartupOptions);

		sqlRIFContextManager = new RIFContextManager(rifServiceStartupOptions);

		sqlSmoothedResultManager = new SmoothedResultManager(rifServiceStartupOptions);

		sqlAgeGenderYearManager = new AgeGenderYearManager(
				sqlRIFContextManager, rifServiceStartupOptions);
		sqlMapDataManager = new MapDataManager(rifServiceStartupOptions);
		sqlCovariateManager = new CovariateManager(rifServiceStartupOptions,
		                                           sqlRIFContextManager);

		InvestigationManager sqlInvestigationManager = new InvestigationManager(
				rifServiceStartupOptions,
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				sqlCovariateManager);

		sqlDiseaseMappingStudyManager = new DiseaseMappingStudyManager(
				rifServiceStartupOptions,
				sqlRIFContextManager,
				sqlInvestigationManager);

		sqlStudyStateManager = new CommonStudyStateManager(rifServiceStartupOptions);

		sqlRIFSubmissionManager = new SubmissionManager(rifServiceStartupOptions,
		                                                sqlStudyStateManager);

		sqlStudyExtractManager = new StudyExtractManager(rifServiceStartupOptions);

		sqlResultsQueryManager = new ResultsQueryManager(rifServiceStartupOptions);

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

	static ServiceResources getInstance(RIFServiceStartupOptions options) {

		return ServiceResources.newInstance(options);
	}

	public static ServiceResources newInstance(final RIFServiceStartupOptions rifStartupOptions) {

		return new ServiceResources(rifStartupOptions);
	}

	public static ServiceResources newInstance(final boolean isWebDeployment,
			final boolean useStrictValidationPolicy) {

		RIFServiceStartupOptions rifServiceStartupOptions
				= RIFServiceStartupOptions.newInstance(
				isWebDeployment,
				useStrictValidationPolicy);

		return new ServiceResources(rifServiceStartupOptions);
	}

	public RIFServiceStartupOptions getRIFServiceStartupOptions() {
		return rifServiceStartupOptions;
	}

	public BaseSQLManager getSqlConnectionManager() {
		return sqlConnectionManager;
	}

	RIFContextManager getSQLRIFContextManager() {
		return sqlRIFContextManager;
	}

	SmoothedResultManager getSQLSmoothedResultManager() {
		return sqlSmoothedResultManager;
	}

	public AgeGenderYearManager getSqlAgeGenderYearManager() {
		return sqlAgeGenderYearManager;
	}

	CovariateManager getSqlCovariateManager() {
		return sqlCovariateManager;
	}

	DiseaseMappingStudyManager getSqlDiseaseMappingStudyManager() {
		return sqlDiseaseMappingStudyManager;
	}

	ResultsQueryManager getSqlResultsQueryManager() {
		return sqlResultsQueryManager;
	}

	public HealthOutcomeManager getHealthOutcomeManager() {
		return healthOutcomeManager;
	}

	rifServices.dataStorageLayer.common.StudyStateManager getStudyStateManager() {
		return sqlStudyStateManager;
	}

	public SubmissionManager getRIFSubmissionManager() {
		return sqlRIFSubmissionManager;
	}

	public StudyExtractManager getSQLStudyExtractManager() {
		return sqlStudyExtractManager;
	}

	MapDataManager getSQLMapDataManager() {
		return this.sqlMapDataManager;
	}

}
