package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.AbstractStudy;

import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;

import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.RIFLogger;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
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

class SQLInvestigationManager 
	extends AbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private SQLRIFContextManager rifContextManager;
	private SQLAgeGenderYearManager ageGenderYearManager;
	private SQLHealthOutcomeManager healthOutcomeManager;
	private SQLCovariateManager covariateManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL investigation manager.
	 */
	public SQLInvestigationManager(
		final SQLRIFContextManager rifContextManager,
		final SQLAgeGenderYearManager ageGenderYearManager,
		final SQLCovariateManager covariateManager,
		final SQLHealthOutcomeManager healthOutcomeManager) {

		this.rifContextManager = rifContextManager;
		this.ageGenderYearManager = ageGenderYearManager;
		this.covariateManager = covariateManager;
		this.healthOutcomeManager = healthOutcomeManager;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
/*
	public ArrayList<Investigation> getInvestigationsForStudy(
		final Connection connection,
		final User user,
		final DiseaseMappingStudy diseaseMappingStudy) {
		
		
		SQLSelectQueryFormatter formatter
			= new SQLSelectQueryFormatter();
		formatter.addSelectField("inv_id");
		formatter.addSelectField("geography");
		formatter.addSelectField("inv_name");
		formatter.addSelectField("inv_description");
		formatter.addSelectField("classifier");
		formatter.addSelectField("classifier_bands");
		formatter.addSelectField("genders");
		formatter.addSelectField("numer_tab");
		formatter.addSelectField("year_start");
		formatter.addSelectField("year_stop");
		formatter.addSelectField("max_age_group");
		formatter.addSelectField("min_age_group");
		formatter.addSelectField("investigation_state");
		
		
		formatter.addFromTable("t_rif40_investigations");
		
	}
		
*/		
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	public void checkNonExistentItems(
		final Connection connection, 
		final Geography geography,
		final Investigation investigation)
		throws RIFServiceException {
		
		ArrayList<AgeBand> ageBands
			= investigation.getAgeBands();
		ageGenderYearManager.checkNonExistentAgeGroups(
			connection, 
			ageBands);

		ArrayList<AbstractCovariate> covariates
			= investigation.getCovariates();
		covariateManager.checkNonExistentCovariates(
			connection, 
			covariates);

		//we will not check whether the health codes exist
		//for now, we'll assume they do
		ArrayList<HealthCode> healthCodes
			= investigation.getHealthCodes();
		healthOutcomeManager.checkNonExistentHealthCodes(healthCodes);
		
		HealthTheme healthTheme
			= investigation.getHealthTheme();
		rifContextManager.checkHealthThemeExists(
			connection, 
			healthTheme.getName());

		NumeratorDenominatorPair ndPair = investigation.getNdPair();
		rifContextManager.checkNDPairExists(
			connection, 
			geography, 
			ndPair);
	}
	
	public void checkInvestigationExists(
		final Connection connection,
		final AbstractStudy study,
		final Investigation investigation) 
		throws RIFServiceException {
		
		SQLRecordExistsQueryFormatter investigationExistsQuery
			= new SQLRecordExistsQueryFormatter();
		investigationExistsQuery.setFromTable("rif40_investigations");
		investigationExistsQuery.addWhereParameter("study_id");
		investigationExistsQuery.addWhereParameter("inv_id");

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(investigationExistsQuery.generateQuery());
			statement.setString(1, study.getIdentifier());
			statement.setString(2, investigation.getIdentifier());
			resultSet = statement.executeQuery();
			
			if (resultSet.next() == false) {
				String recordType
					= investigation.getRecordType();
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						investigation.getDisplayName());

				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_AGE_GROUP, 
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {			
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					investigation.getRecordType(),
					investigation.getDisplayName());

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLInvestigationManager.class, 
				errorMessage, 
				sqlException);										
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
	}
		
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
