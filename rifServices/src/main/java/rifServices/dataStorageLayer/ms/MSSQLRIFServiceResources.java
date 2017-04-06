package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public final class MSSQLRIFServiceResources {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The health outcome manager. */
	private MSSQLHealthOutcomeManager healthOutcomeManager;
	
	/** The covariate manager. */
	private MSSQLCovariateManager sqlCovariateManager;
	
	/** The sql connection manager. */
	private MSSQLConnectionManager sqlConnectionManager;
	
	/** The sql rif context manager. */
	private MSSQLRIFContextManager sqlRIFContextManager;
	
	private MSSQLSmoothedResultManager sqlSmoothedResultManager;
	
	/** The sql age gender year manager. */
	private MSSQLAgeGenderYearManager sqlAgeGenderYearManager;
	
	/** The sql map data manager. */
	private MSSQLMapDataManager sqlMapDataManager;
	
	/** The disease mapping study manager. */
	private MSSQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager;
	
	private MSSQLRIFSubmissionManager sqlRIFSubmissionManager;
	
	private MSSQLStudyExtractManager sqlStudyExtractManager;
	
	private MSSQLResultsQueryManager sqlResultsQueryManager;
	
	private MSSQLInvestigationManager sqlInvestigationManager;

	private MSSQLStudyStateManager sqlStudyStateManager;
	
	
	private RIFServiceStartupOptions rifServiceStartupOptions;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private MSSQLRIFServiceResources(
		final RIFServiceStartupOptions rifServiceStartupOptions) 
		throws RIFServiceException {
		
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		sqlConnectionManager = new MSSQLConnectionManager(rifServiceStartupOptions);
		healthOutcomeManager = new MSSQLHealthOutcomeManager(rifServiceStartupOptions);

		RIFDatabaseProperties rifDatabaseProperties
			= rifServiceStartupOptions.getRIFDatabaseProperties();
		
		sqlRIFContextManager 
			= new MSSQLRIFContextManager(rifDatabaseProperties);
		
		
		sqlSmoothedResultManager
			= new MSSQLSmoothedResultManager(rifDatabaseProperties);
		
		sqlAgeGenderYearManager 
			= new MSSQLAgeGenderYearManager(
				rifDatabaseProperties,
				sqlRIFContextManager);
		sqlMapDataManager 
			= new MSSQLMapDataManager(
				rifServiceStartupOptions, 
				sqlRIFContextManager);
		sqlCovariateManager 
			= new MSSQLCovariateManager(
				rifDatabaseProperties,
				sqlRIFContextManager);

		sqlInvestigationManager 
			= new MSSQLInvestigationManager(
				rifDatabaseProperties,
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				sqlCovariateManager,
				healthOutcomeManager);
		
		
		sqlDiseaseMappingStudyManager 
			= new MSSQLDiseaseMappingStudyManager(
				rifDatabaseProperties,
				sqlRIFContextManager,
				sqlInvestigationManager,
				sqlMapDataManager);
				
		sqlStudyStateManager
			= new MSSQLStudyStateManager(rifDatabaseProperties);
		
		sqlRIFSubmissionManager 
			= new MSSQLRIFSubmissionManager(
				rifDatabaseProperties,
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				sqlCovariateManager,
				sqlDiseaseMappingStudyManager,
				sqlMapDataManager,
				sqlStudyStateManager);

		sqlStudyExtractManager
			= new MSSQLStudyExtractManager(
				rifServiceStartupOptions);
		
		sqlResultsQueryManager
			= new MSSQLResultsQueryManager(
				rifDatabaseProperties,
				sqlRIFContextManager,
				sqlMapDataManager,
				sqlDiseaseMappingStudyManager);

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
		final RIFServiceStartupOptions rifStartupOptions)
		throws RIFServiceException {
		
		MSSQLRIFServiceResources rifServiceResources
			= new MSSQLRIFServiceResources(rifStartupOptions);
		
		return rifServiceResources;
	}

	public static MSSQLRIFServiceResources newInstance(
		final boolean isWebDeployment,
		final boolean useStrictValidationPolicy)
		throws RIFServiceException {
			
		RIFServiceStartupOptions rifServiceStartupOptions
			= RIFServiceStartupOptions.newInstance(
				isWebDeployment,
				useStrictValidationPolicy);
		MSSQLRIFServiceResources rifServiceResources
			= new MSSQLRIFServiceResources(rifServiceStartupOptions);
		
		return rifServiceResources;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public RIFServiceStartupOptions getRIFServiceStartupOptions() {
		return rifServiceStartupOptions;
	}
	
	public MSSQLConnectionManager getSqlConnectionManager() {
		return sqlConnectionManager;
	}

	public MSSQLRIFContextManager getSQLRIFContextManager() {
		return sqlRIFContextManager;
	}

	public MSSQLSmoothedResultManager getSQLSmoothedResultManager() {
		return sqlSmoothedResultManager;
	}
	
	public MSSQLAgeGenderYearManager getSqlAgeGenderYearManager() {
		return sqlAgeGenderYearManager;
	}
	
	public MSSQLCovariateManager getSqlCovariateManager() {
		return sqlCovariateManager;
	}

	public MSSQLDiseaseMappingStudyManager getSqlDiseaseMappingStudyManager() {
		return sqlDiseaseMappingStudyManager;
	}

	public MSSQLResultsQueryManager getSqlResultsQueryManager() {
		return sqlResultsQueryManager;
	}
	
	public MSSQLHealthOutcomeManager getHealthOutcomeManager() {
		return healthOutcomeManager;
	}

	public MSSQLStudyStateManager getStudyStateManager() {
		return sqlStudyStateManager;
	}
	
	public MSSQLRIFSubmissionManager getRIFSubmissionManager() {
		return sqlRIFSubmissionManager;
	}
	
	public MSSQLStudyExtractManager getSQLStudyExtractManager() {
		return sqlStudyExtractManager;		
	}
	public MSSQLMapDataManager getSQLMapDataManager() {
		return this.sqlMapDataManager;
	}
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
