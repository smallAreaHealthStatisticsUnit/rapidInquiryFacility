package rifServices.dataStorageLayer;


import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudyArea;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.RIFJobSubmission;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.YearRange;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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

public class SQLRIFSubmissionManager {
		
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public SQLRIFSubmissionManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/*
	public ArrayList<RIFJobSubmissionSummary> getRIFJobSubmissionsForUser(
		Connection connection,
		User user) {
	
		StringBuilder query = new StringBuilder();
			
		ArrayList<RIFJobSubmissionSummary> results
			= new ArrayList<RIFJobSubmissionSummary>();
		
		SQLSelectQueryFormatter formatter = new SQLSelectQueryFormatter();
		formatter.addFromTable("study_name");
		formatter.addSelectField("summary");
		formatter.addFromTable("t_rif40_studies");
		formatter.addOrderByCondition("study_name");
		formatter.addWhereParameter("username");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.prepareStatement(formatter.generateQuery());
			statement.setString(1, user.getUserID());
			resultSet
				= statement.executeQuery();
			while (resultSet.next()) {
				String studyIdentifier = resultSet.getString(1);
				String studyName = resultSet.getString(2);
				String studySummary = resultSet.getString(3);				
				
				RIFJobSubmissionSummary jobSubmissionSummary
					= RIFJobSubmissionSummary.newInstance(							
						studyIdentifier, 
						studyName, 
						studySummary);

				results.add(jobSubmissionSummary);				
			}
		}
		catch(SQLException exception) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"", 
					user.getUserID());
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError, errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
		
	}
	*/

	/*

	public RIFJobSubmission getRIFJobSubmissionForUser(
		Connection connection,
		User user,
		RIFJobSubmissionSummary jobSubmissionSummary) throws RIFServiceException {
		
		
		
		RIFJobSubmission result
			= RIFJobSubmission.newInstance();
		
		SQLSelectQueryFormatter formatter
			= new SQLSelectQueryFormatter();
		formatter.addSelectField("geography");
		formatter.addSelectField("project");
		formatter.addSelectField("");
		
		
		formatter.addFromTable("t_rif40_studies");
		
		
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			Integer studyID
				= Integer.valueOf(jobSubmissionSummary.getStudyIdentifier());
			
			
			
		}
		catch(Exception exception) {
			
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
	*/
	
	/*
	private ArrayList<Investigation> getInvestigationsForStudy(
		Connection connection,
		AbstractStudy study) throws SQLException {
		
		
	}
	*/
	
	/*
	private ArrayList<HealthCode> getHealthOutcomesForStudy(
		Connection connection,
		AbstractStudy study) throws SQLException {
		
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
		
		
		return results;
	}
	
	private ArrayList<AbstractCovariate> getCovariatesForInvestigation(
		Connection connection,
		User user,
		Integer studyID,
		Integer investigationID,
		Investigation investigation) throws Exception {
			
		SQLSelectQueryFormatter formatter 
			= new SQLSelectQueryFormatter();
		formatter.addSelectField("covariate_name");
		formatter.addSelectField("min");
		formatter.addSelectField("max");
		formatter.addFromTable("t_rif40_inv_covariates");
		formatter.addWhereParameter("inv_id");
		formatter.addWhereParameter("study_id");
		
		ArrayList<AbstractCovariate> results 
			= new ArrayList<AbstractCovariate>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			statement
				= connection.prepareStatement(formatter.generateQuery());
			statement.setInt(1, investigationID);
			statement.setInt(2, studyID);
			resultSet
				= statement.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString(1);
				Double minimumValue = resultSet.getDouble(2);
				Double maximumValue = resultSet.getDouble(3);
				
				//TODO: KLG find out where we can find out what type the variable is
				CovariateType covariateType = CovariateType.CONTINUOUS_VARIABLE;
				AdjustableCovariate adjustableCovariate
					= AdjustableCovariate.newInstance(
						name, 
						String.valueOf(minimumValue), 
						String.valueOf(maximumValue), 
						covariateType);	
				results.add(adjustableCovariate);
			}
			return results;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}	
				
		return results;		
	}
	
	
	*/
	
	
	
	
	
	
	
	
	/**
	 * Adds the rif job submission.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param rifJobSubmission the rif job submission
	 * @throws RIFServiceException the RIF service exception
	 */
	public void addRIFJobSubmission(
		final Connection connection,
		final User user,
		final RIFJobSubmission rifJobSubmission) 
		throws RIFServiceException {
		
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) rifJobSubmission.getStudy();

		String geographyName
			= diseaseMappingStudy.getGeography().getName();
		DiseaseMappingStudyArea diseaseMappingStudyArea
			= diseaseMappingStudy.getDiseaseMappingStudyArea();
		String geoLevelViewName
			= diseaseMappingStudyArea.getGeoLevelView().getName();
		String geoLevelAreaName
			= diseaseMappingStudyArea.getGeoLevelArea().getName();
		String geoLevelToMapName
			= diseaseMappingStudyArea.getGeoLevelToMap().getName();
		String geoLevelSelectName
			= diseaseMappingStudyArea.getGeoLevelSelect().getName();
		
		String projectName
			= rifJobSubmission.getProject().getName();
		String studyName
			= diseaseMappingStudy.getName();
		Investigation firstInvestigation
			= diseaseMappingStudy.getInvestigations().get(0);
		NumeratorDenominatorPair ndPair
			= firstInvestigation.getNdPair();
		String denominatorTableName
			= ndPair.getDenominatorTableName();
		String numeratorTableName
			= ndPair.getNumeratorTableName();
		YearRange yearRange
			= firstInvestigation.getYearRange();
		String yearStart
			= yearRange.getLowerBound();
		String yearStop
			= yearRange.getUpperBound();
		
		//build array of health codes
		ArrayList<HealthCode> healthCodes
			= firstInvestigation.getHealthCodes();
		int numberOfHealthCodes = healthCodes.size();
		String[] healthCodeNames = new String[numberOfHealthCodes];
		for (int i = 0; i < numberOfHealthCodes; i++) {
			healthCodeNames[i] = healthCodes.get(i).getCode();
		}
		
		//build array of investigation descriptions
		String[] investigationDescriptions = new String[1];
		investigationDescriptions[0] = firstInvestigation.getTitle();

		ArrayList<AbstractCovariate> covariates
			= firstInvestigation.getCovariates();
		int numberOfCovariates = covariates.size();		
		String[] covariateNames = new String[numberOfCovariates];
		for (int i = 0; i < numberOfCovariates; i++) {
			covariateNames[i] = covariates.get(i).getName();
		}
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT klg_create_disease_mapping_study(");
		query.append("?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) AS result;");
		
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(query.toString());
			statement.setString(1, geographyName);
			statement.setString(2, geoLevelViewName);
			statement.setString(3, geoLevelAreaName);
			statement.setString(4, geoLevelToMapName);
			statement.setString(5, geoLevelSelectName);
			statement.setString(6, "geolevel_selection");
			statement.setString(7, projectName);
			statement.setString(8, studyName);
			statement.setString(9, denominatorTableName);
			statement.setString(10, numeratorTableName);
			statement.setString(11, yearStart);
			statement.setString(12, yearStop);
			Array healthCodeArray
				= connection.createArrayOf("varchar", healthCodeNames);
			statement.setArray(13, healthCodeArray);
			Array investigationDescriptionArray
				= connection.createArrayOf("varchar", investigationDescriptions);
			statement.setArray(14, investigationDescriptionArray);
			Array covariateNameArray
				= connection.createArrayOf("varchar", covariateNames);
			statement.setArray(15, covariateNameArray);

			resultSet = statement.executeQuery();
			resultSet.next();
			String result = resultSet.getString(1);
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddSubmission",
					rifJobSubmission.getDisplayName());
			
			Logger logger 
				= LoggerFactory.getLogger(SQLRIFSubmissionManager.class);
			logger.error(errorMessage, sqlException);			
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
	
/*	
	private ArrayList<Investigation> getInvestigationsForStudy(
		Connection connection,
		User user,
		DiseaseMappingStudy diseaseMappingStudy) {
				
				
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
	
	/*
	public ArrayList<RIFJobSubmission> getRIFJobSubmissionsForUser(
		Connection connection,
		User user) {
		
		//Step 1: Obtain study records
		
		
		//Step 2: Obtain investigations for a given study
		
		//Step 3: Find health outcomes for a given investigation
		
		//Step 4: Find covariates for a given investigation
		

		
		
		
	}
		*/
	
	
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
