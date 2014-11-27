package rifServices.dataStorageLayer;

import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceStartupOptions;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public class RIFServiceResources {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	/** The health outcome manager. */
	private SQLHealthOutcomeManager healthOutcomeManager;
	
	/** The covariate manager. */
	private SQLCovariateManager sqlCovariateManager;
	
	/** The sql connection manager. */
	private SQLConnectionManager sqlConnectionManager;
	
	/** The sql rif context manager. */
	private SQLRIFContextManager sqlRIFContextManager;
	
	/** The sql age gender year manager. */
	private SQLAgeGenderYearManager sqlAgeGenderYearManager;
	
	/** The sql map data manager. */
	private SQLMapDataManager sqlMapDataManager;
	
	/** The disease mapping study manager. */
	private SQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager;
	
	private SQLRIFSubmissionManager sqlRIFSubmissionManager;
	
	private SQLResultsQueryManager sqlResultsQueryManager;
	
	private SQLInvestigationManager sqlInvestigationManager;

	// ==========================================
	// Section Construction
	// ==========================================

	private RIFServiceResources(
		final RIFServiceStartupOptions rifServiceStartupOptions) 
		throws RIFServiceException {

		sqlConnectionManager = new SQLConnectionManager(rifServiceStartupOptions);
		healthOutcomeManager = new SQLHealthOutcomeManager(rifServiceStartupOptions);
		sqlRIFContextManager = new SQLRIFContextManager();
		sqlAgeGenderYearManager 
			= new SQLAgeGenderYearManager(sqlRIFContextManager);
		sqlMapDataManager 
			= new SQLMapDataManager(
				rifServiceStartupOptions, 
				sqlRIFContextManager);
		sqlCovariateManager = new SQLCovariateManager(sqlRIFContextManager);

		sqlInvestigationManager 
			= new SQLInvestigationManager(
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				sqlCovariateManager,
				healthOutcomeManager);
		
		
		sqlDiseaseMappingStudyManager 
			= new SQLDiseaseMappingStudyManager(
				sqlRIFContextManager,
				sqlInvestigationManager,
				sqlMapDataManager);
				
				
		sqlRIFSubmissionManager 
			= new SQLRIFSubmissionManager(
				sqlRIFContextManager,
				sqlAgeGenderYearManager,
				sqlCovariateManager,
				sqlDiseaseMappingStudyManager);

		sqlResultsQueryManager
			= new SQLResultsQueryManager(
				sqlRIFContextManager,
				sqlMapDataManager,
				sqlDiseaseMappingStudyManager);

		healthOutcomeManager.initialiseTaxomies();	
	}
	
	public static RIFServiceResources newInstance(
		final RIFServiceStartupOptions rifStartupOptions)
		throws RIFServiceException {
		
		RIFServiceResources rifServiceResources
			= new RIFServiceResources(rifStartupOptions);
		
		return rifServiceResources;
	}

	public static RIFServiceResources newInstance(boolean isWebDeployment)
		throws RIFServiceException {
			
		RIFServiceStartupOptions rifServiceStartupOptions
			= new RIFServiceStartupOptions(isWebDeployment);
		RIFServiceResources rifServiceResources
			= new RIFServiceResources(rifServiceStartupOptions);
		
		return rifServiceResources;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public SQLConnectionManager getSqlConnectionManager() {
		return sqlConnectionManager;
	}

	public SQLRIFContextManager getSQLRIFContextManager() {
		return sqlRIFContextManager;
	}

	public SQLAgeGenderYearManager getSqlAgeGenderYearManager() {
		return sqlAgeGenderYearManager;
	}

	public SQLCovariateManager getSqlCovariateManager() {
		return sqlCovariateManager;
	}

	public SQLDiseaseMappingStudyManager getSqlDiseaseMappingStudyManager() {
		return sqlDiseaseMappingStudyManager;
	}

	public SQLResultsQueryManager getSqlResultsQueryManager() {
		return sqlResultsQueryManager;
	}
	
	public SQLHealthOutcomeManager getHealthOutcomeManager() {
		return healthOutcomeManager;
	}

	public SQLRIFSubmissionManager getRIFSubmissionManager() {
		return sqlRIFSubmissionManager;
	}
	
	public SQLMapDataManager getSQLMapDataManager() {
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
