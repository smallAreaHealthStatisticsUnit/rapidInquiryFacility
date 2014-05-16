package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifServices.util.RIFLogger;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.Date;



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
	private SQLRIFContextManager rifContextManager;
	private SQLAgeGenderYearManager ageGenderYearManager;
	private SQLCovariateManager covariateManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public SQLRIFSubmissionManager(
		SQLRIFContextManager rifContextManager,
		SQLAgeGenderYearManager ageGenderYearManager,
		SQLCovariateManager covariateManager) {

		this.rifContextManager = rifContextManager;
		this.ageGenderYearManager = ageGenderYearManager;
		this.covariateManager = covariateManager;		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	
	/**
	 * Used as a test method to clear rif job submissions.
	 * @param connection
	 * @throws RIFServiceException
	 */
	public void clearRIFJobSubmissionsForUser(
		Connection connection,
		User user)
		throws RIFServiceException {
	
		String userID = user.getUserID();
		PreparedStatement deleteComparisonAreasStatement = null;
		PreparedStatement deleteStudyAreasStatement = null;
		PreparedStatement deleteInvestigationsStatement = null;
		PreparedStatement deleteStudiesStatement = null;

		try {
			SQLDeleteQueryFormatter deleteComparisonAreasQuery
				= new SQLDeleteQueryFormatter();
			deleteComparisonAreasQuery.setFromTable("t_rif40_comparison_areas");
			deleteComparisonAreasQuery.addWhereParameter("username");
			
			deleteComparisonAreasStatement 
				= connection.prepareStatement(deleteComparisonAreasQuery.generateQuery());
			deleteComparisonAreasStatement.setString(1, userID);
			deleteComparisonAreasStatement.executeUpdate();
			
			SQLDeleteQueryFormatter deleteStudyAreasQuery
				= new SQLDeleteQueryFormatter();
			deleteStudyAreasQuery.setFromTable("t_rif40_study_areas");
			deleteStudyAreasQuery.addWhereParameter("username");
			deleteStudyAreasStatement 
				= connection.prepareStatement(deleteStudyAreasQuery.generateQuery());
			deleteStudyAreasStatement.setString(1, userID);
			deleteStudyAreasStatement.executeUpdate();
			
			SQLDeleteQueryFormatter deleteInvestigationsQuery
				= new SQLDeleteQueryFormatter();
			deleteInvestigationsQuery.setFromTable("t_rif40_investigations");
			deleteInvestigationsQuery.addWhereParameter("username");
			deleteInvestigationsStatement
				= connection.prepareStatement(deleteInvestigationsQuery.generateQuery());
			deleteInvestigationsStatement.setString(1, userID);
			deleteInvestigationsStatement.executeUpdate();
			
			SQLDeleteQueryFormatter deleteStudiesQuery
				= new SQLDeleteQueryFormatter();
			deleteStudiesQuery.setFromTable("t_rif40_studies");
			deleteStudiesQuery.addWhereParameter("username");
			deleteStudiesStatement
				= connection.prepareStatement(deleteInvestigationsQuery.generateQuery());
			deleteStudiesStatement.setString(1, userID);
			deleteStudiesStatement.executeUpdate();

		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToClearSubmissionsForUser",
					user.getUserID());
			RIFServiceException rifServiceException	
				= new RIFServiceException(
					RIFServiceError.UNABLE_DELETE_STUDIES_FOR_USER, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(deleteComparisonAreasStatement);
			SQLQueryUtility.close(deleteStudyAreasStatement);
			SQLQueryUtility.close(deleteInvestigationsStatement);
		}

	}
	
	
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
		
		
		user.checkErrors();
		rifJobSubmission.checkErrors();
		//verify that year range checks against the database
		
		//verify that all the age groups of all the age bands are
		//in the database

		try {
			
			//Step 1: Add general information about the study to the
			//underlying study table.  This modifies the rif40_studies table
			Project project = rifJobSubmission.getProject();
			DiseaseMappingStudy diseaseMappingStudy
				= (DiseaseMappingStudy) rifJobSubmission.getStudy();
			addStudyInformation(
				connection,
				project,
				diseaseMappingStudy);
			
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			addStudyArea(
				connection,
				diseaseMappingStudyArea);

			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			addComparisonArea(
				connection,
				comparisonArea);
		
			ArrayList<Investigation> investigations
				= diseaseMappingStudy.getInvestigations();
			for(Investigation investigation : investigations) {
				addInvestigation(
					connection,
					diseaseMappingStudy.getGeography(),
					investigation);
			}			
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace(System.out);
			String errorMessage 
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToAddSubmission",
					rifJobSubmission.getDisplayName());
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFSubmissionManager.class, 
				errorMessage, 
				sqlException);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.UNABLE_TO_ADD_STUDY, 
					errorMessage);
			throw rifServiceException;			
		}
	}
	
	private void addStudyInformation(
		Connection connection,
		Project project,
		DiseaseMappingStudy diseaseMappingStudy) 
		throws SQLException,
		RIFServiceException {
		
		SQLInsertQueryFormatter insertStudyQueryFormatter
			= new SQLInsertQueryFormatter();
		insertStudyQueryFormatter.setIntoTable("rif40_studies");
		insertStudyQueryFormatter.addInsertField("project");
		insertStudyQueryFormatter.addInsertField("study_name");		
		//to delete when schema changes take effect
		insertStudyQueryFormatter.addInsertField("summary");				
		insertStudyQueryFormatter.addInsertField("description");
		insertStudyQueryFormatter.addInsertField("other_notes");
		insertStudyQueryFormatter.addInsertField("geography");
		//Until we develop risk analysis studies, study_type will be '1'
		insertStudyQueryFormatter.addInsertField("study_type");
		//Here, study state will always be 'C' for created
		insertStudyQueryFormatter.addInsertField("study_state");		
		insertStudyQueryFormatter.addInsertField("study_geolevel_name");
		insertStudyQueryFormatter.addInsertField("comparison_geolevel_name");
		insertStudyQueryFormatter.addInsertField("denom_tab");
		insertStudyQueryFormatter.addInsertField("covariate_table");
		
		PreparedStatement statement = null;
		try {
			statement 
				= connection.prepareStatement(insertStudyQueryFormatter.generateQuery());
			statement.setString(1, project.getName());
			statement.setString(2, diseaseMappingStudy.getName());
			statement.setString(3, "");
			statement.setString(4, diseaseMappingStudy.getDescription());
			statement.setString(5, diseaseMappingStudy.getOtherNotes());			
			Geography geography = diseaseMappingStudy.getGeography();
			statement.setString(6, geography.getName());
			statement.setInt(7, 1);
			statement.setString(8, "C");
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			GeoLevelToMap studyAreaGeoLevelToMap
				= diseaseMappingStudyArea.getGeoLevelToMap();
			statement.setString(9, studyAreaGeoLevelToMap.getName());
			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			GeoLevelToMap comparisonAreaGeoLevelToMap
				= comparisonArea.getGeoLevelToMap();
			statement.setString(10, comparisonAreaGeoLevelToMap.getName());
			
			ArrayList<Investigation> investigations
				= diseaseMappingStudy.getInvestigations();
			NumeratorDenominatorPair ndPair
				= investigations.get(0).getNdPair();
			statement.setString(11, ndPair.getDenominatorTableName());
			
			String covariateTableName
				= getCovariateTableName(
					connection,
					geography, 
					studyAreaGeoLevelToMap);
			statement.setString(12, covariateTableName);
			
			statement.execute();
		}
		finally {
			SQLQueryUtility.close(statement);
		}

	}
	
	private String getCovariateTableName(
		Connection connection,
		Geography geography,
		GeoLevelToMap studyAreaGeoLevelToMap) 
		throws SQLException,
		RIFServiceException {
				
		SQLSelectQueryFormatter getCovariateTableNameQueryFormatter
			= new SQLSelectQueryFormatter();
		getCovariateTableNameQueryFormatter.addSelectField("covariate_table");
		getCovariateTableNameQueryFormatter.addFromTable("rif40_geolevels");
		getCovariateTableNameQueryFormatter.addWhereParameter("geography");
		getCovariateTableNameQueryFormatter.addWhereParameter("geolevel_name");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String covariateTableName = null;
		try {
			statement 
				= connection.prepareStatement(getCovariateTableNameQueryFormatter.generateQuery());
			statement.setString(1, geography.getName());
			statement.setString(2, studyAreaGeoLevelToMap.getName());
			resultSet = statement.executeQuery();
						
			if (resultSet.next() == false) {
				String errorMessage 
					= RIFServiceMessages.getMessage(
						"sqlRIFSubmissionManager.error.unableToGetCovariatesTable");
				RIFServiceException rifServiceException	
					 = new RIFServiceException(
						RIFServiceError.UNABLE_TO_ADD_STUDY, errorMessage);
				throw rifServiceException;				
			}
			
			return resultSet.getString(1);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
				
	}

	private void addStudyArea(
		Connection connection,
		DiseaseMappingStudyArea studyArea)
		throws SQLException,
		RIFServiceException {
		
		String[] mapIdentifiers
			= studyArea.getMapAreaIdentifiers();
				
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO rif40_study_areas ");
		query.append("(area_id, band_id) ");
		query.append("SELECT ");
		query.append("unnest AS area_id,");
		query.append("row_number() OVER() AS band_id ");
		query.append("FROM ");
		query.append("unnest(?)");
			
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(query.toString());
			Array names = connection.createArrayOf("text", mapIdentifiers);
			statement.setArray(1, names);
			statement.executeUpdate();		
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		

	}

	private void addComparisonArea(
		Connection connection,
		ComparisonArea comparisonArea)
		throws SQLException,
		RIFServiceException {
				
		String[] mapIdentifiers
			= comparisonArea.getMapAreaIdentifiers();
				
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO rif40_comparison_areas ");
		query.append("(area_id) ");
		query.append("SELECT ");
		query.append("unnest AS area_id ");
		query.append("FROM ");
		query.append("unnest(?)");
		
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(query.toString());
			Array names = connection.createArrayOf("text", mapIdentifiers);
			statement.setArray(1, names);
			statement.executeUpdate();		
		}
		finally {
			SQLQueryUtility.close(statement);
		}
	}
	
	private void addInvestigation(
		Connection connection,
		Geography geography,
		Investigation investigation) 
		throws SQLException,
		RIFServiceException {
		
		
				
		SQLInsertQueryFormatter insertInvestigationQueryFormatter
			= new SQLInsertQueryFormatter();
		insertInvestigationQueryFormatter.setIntoTable("rif40_investigations");
		insertInvestigationQueryFormatter.addInsertField("inv_name");		
		insertInvestigationQueryFormatter.addInsertField("inv_description");
		insertInvestigationQueryFormatter.addInsertField("year_start");
		insertInvestigationQueryFormatter.addInsertField("year_stop");
		insertInvestigationQueryFormatter.addInsertField("max_age_group");
		insertInvestigationQueryFormatter.addInsertField("min_age_group");
		insertInvestigationQueryFormatter.addInsertField("genders");
		insertInvestigationQueryFormatter.addInsertField("numer_tab");
		insertInvestigationQueryFormatter.addInsertField("geography");
		
		PreparedStatement statement = null;
		try {
			statement 
				= connection.prepareStatement(insertInvestigationQueryFormatter.generateQuery());
			statement.setString(1, investigation.getTitle());
			statement.setString(2, investigation.getDescription());
			
			YearRange yearRange = investigation.getYearRange();
			//year_start
			statement.setInt(3, Integer.valueOf(yearRange.getLowerBound()));
			//year_stop
			statement.setInt(4, Integer.valueOf(yearRange.getUpperBound()));
			//max_age_group
			AgeGroup maximumAgeGroup = investigation.getMaximumAgeGroup();
			statement.setInt(5, Integer.valueOf(maximumAgeGroup.getUpperLimit()));
			//min_age_group
			AgeGroup minimumAgeGroup = investigation.getMinimumAgeGroup();
			statement.setInt(6, Integer.valueOf(minimumAgeGroup.getLowerLimit()));
			//genders
			Sex sex = investigation.getSex();
			if (sex == Sex.MALES) {
				statement.setInt(7, 1);	
			}
			else if (sex == Sex.FEMALES){
				statement.setInt(7, 2);	
			}	
			else {
				//assume both
				statement.setInt(7, 3);	
			}
			//numer_tab
			NumeratorDenominatorPair ndPair = investigation.getNdPair();
			statement.setString(8, ndPair.getNumeratorTableName());
			
			statement.setString(9, geography.getName());
			statement.executeUpdate();

		}
		finally {
			SQLQueryUtility.close(statement);
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
