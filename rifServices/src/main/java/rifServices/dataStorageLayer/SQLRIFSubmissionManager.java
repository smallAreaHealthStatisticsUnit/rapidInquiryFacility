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

class SQLRIFSubmissionManager extends AbstractSQLManager {
		
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private SQLDiseaseMappingStudyManager diseaseMappingStudyManager;
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
		final SQLRIFContextManager rifContextManager,
		final SQLAgeGenderYearManager ageGenderYearManager,
		final SQLCovariateManager covariateManager,
		final SQLDiseaseMappingStudyManager diseaseMappingStudyManager) {

		this.rifContextManager = rifContextManager;
		this.ageGenderYearManager = ageGenderYearManager;
		this.covariateManager = covariateManager;		
		this.diseaseMappingStudyManager = diseaseMappingStudyManager;
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
		final Connection connection,
		final User user)
		throws RIFServiceException {
	
		/*
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
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToClearSubmissionsForUser",
					user.getUserID());
			RIFServiceException rifServiceException	
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(deleteComparisonAreasStatement);
			SQLQueryUtility.close(deleteStudyAreasStatement);
			SQLQueryUtility.close(deleteInvestigationsStatement);
		}
*/
	}
	
	
	/**
	 * submit rif study submission.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param rifJobSubmission the rif job submission
	 * @throws RIFServiceException the RIF service exception
	 */
	public void submitStudy(
		final Connection connection,
		final User user,
		final RIFStudySubmission rifStudySubmission) 
		throws RIFServiceException {
		
		
		user.checkErrors();
		rifStudySubmission.checkErrors();
		
		//perform various checks for non-existent objects
		//such as geography,  geo level selects, covariates
		checkNonExistentItems(
			connection, 
			rifStudySubmission);
		
		//verify that all the age groups of all the age bands are
		//in the database

/*
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
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
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
		
	*/
		
	}
	
	private void addStudyInformation(
		final Connection connection,
		final Project project,
		final DiseaseMappingStudy diseaseMappingStudy) 
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
		final Connection connection,
		final Geography geography,
		final GeoLevelToMap studyAreaGeoLevelToMap) 
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
		final Connection connection,
		final DiseaseMappingStudyArea studyArea)
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

	/*
	private ComparisonArea getComparisonArea(
		final Connection connection,
		final String studyID)
		throws SQLException,
		RIFServiceException {
		
		ComparisonArea result = null;
		SQLSelectQueryFormatter queryFormatter
			= new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("area_id");
		queryFormatter.addFromTable("rif40_comparison_areas");
		queryFormatter.addWhereParameter("study_id");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, studyID);
			resultSet = statement.executeQuery();
			
			
			
			
			
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
	
	*/
	
	private void addComparisonArea(
		final Connection connection,
		final ComparisonArea comparisonArea)
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
		final Connection connection,
		final Geography geography,
		final Investigation investigation) 
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
	
	
	private ArrayList<Investigation> getInvestigationsForStudy(
		final Connection connection,
		final String userID,
		final String studyID) 
		throws SQLException,
		RIFServiceException {
		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("inv_description");
		queryFormatter.addSelectField("year_start");
		queryFormatter.addSelectField("year_stop");
		queryFormatter.addSelectField("max_age_group");
		queryFormatter.addSelectField("min_age_group");
		queryFormatter.addSelectField("genders");
		queryFormatter.addSelectField("numer_tab");		
		queryFormatter.addFromTable("rif40_investigations");
		queryFormatter.addWhereParameter("username");
		queryFormatter.addWhereParameter("study_id");
				
		ArrayList<Investigation> results = new ArrayList<Investigation>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, userID);
			statement.setString(2, studyID);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				Investigation investigation 
					= Investigation.newInstance();
				investigation.setDescription(resultSet.getString(1));
				
				String lowerYearBound = resultSet.getString(2);
				String upperYearBound = resultSet.getString(3);
				YearRange yearRange 
					= YearRange.newInstance(lowerYearBound, upperYearBound);			
				investigation.setYearRange(yearRange);
				
				//@TODO KLG
				//Here is where we lose information.  Because we only have
				//one age band, we will lose finer categories.  For example,
				//the set of age bands [0-5], [6-10], [11-15] could be part of 
				//a submitted investigation.  However, when it is retrieved,
				//it will become [0,15] when it is retrieved.  This is because
				//the schema does not yet have the capacity to store multiple
				//age bands
				int minimumAgeGroupCode = resultSet.getInt(4);
				AgeGroup minimumAgeGroup
					= getAgeGroupFromCode(
						connection, 
						minimumAgeGroupCode);
				int maximumAgeGroupCode = resultSet.getInt(5);
				AgeGroup maximumAgeGroup
					= getAgeGroupFromCode(
						connection, 
						maximumAgeGroupCode);			 
				AgeBand ageBand 
					= AgeBand.newInstance(
						minimumAgeGroup, 
						maximumAgeGroup);
				investigation.addAgeBand(ageBand);
				
				int sexCode = resultSet.getInt(6);
				if (sexCode == 1) {
					investigation.setSex(Sex.MALES);
				}
				else if (sexCode == 2) {
					investigation.setSex(Sex.FEMALES);					
				}
				else if (sexCode == 3) {
					investigation.setSex(Sex.BOTH);					
				}
				else {
					//it is required and should only be 1, 2, or 3.
					assert false;
				}
				
				//set numerator denominator pair
				String numeratorTableName
					= resultSet.getString(7);
				NumeratorDenominatorPair ndPair
					= getNDPairForNumeratorTableName(
						connection, 
						numeratorTableName);
				investigation.setNdPair(ndPair);
				
/*				
				//get study area
				DiseaseMappingStudyArea diseaseMappingStudyArea
					= getDiseaseMappingStudyArea(
						userID,
						studyID);
				
				
				//get comparison area
				ComparisonArea comparisonArea
					= getComparisonArea(
						userID,
						studyID);
*/				
				
				
				
			}
			
			
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		return results;
	}

	private AgeGroup getAgeGroupFromCode(
		final Connection connection, 
		final int ageGroupCode) 
		throws SQLException,
		RIFServiceException {
		
		AgeGroup result = null;
		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("low_age");
		queryFormatter.addSelectField("high_age");
		queryFormatter.addFromTable("rif40_age_groups");
		queryFormatter.addWhereParameter("age_group_id");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, ageGroupCode);
			//assume we get a result
			resultSet = statement.executeQuery();
			int lowAge = resultSet.getInt(1);
			int highAge = resultSet.getInt(2);
			AgeGroup ageGroup = AgeGroup.newInstance();
			ageGroup.setIdentifier(String.valueOf(ageGroupCode));
			ageGroup.setLowerLimit(String.valueOf(lowAge));
			ageGroup.setUpperLimit(String.valueOf(highAge));
			result = ageGroup;
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}

		return result;
	}

	private NumeratorDenominatorPair getNDPairForNumeratorTableName(
		final Connection connection,
		final String numeratorTableName) 
		throws SQLException,
		RIFServiceException {
		
		
		NumeratorDenominatorPair result = null;
		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.setUseDistinct(true);
		queryFormatter.addSelectField("numerator_description");
		queryFormatter.addSelectField("denominator_table");
		queryFormatter.addSelectField("denominator_description");		
		queryFormatter.addFromTable("rif40_num_denom");
		queryFormatter.addWhereParameter("numerator_table");
		
		PreparedStatement statement 
			= connection.prepareStatement(queryFormatter.generateQuery());
		statement.setString(1, numeratorTableName);
		ResultSet resultSet = null;
		try {
			resultSet = statement.executeQuery();
			resultSet.next();
			result = NumeratorDenominatorPair.newInstance();
			result.setNumeratorTableName(numeratorTableName);
			result.setNumeratorTableDescription(resultSet.getString(1));
			result.setDenominatorTableName(resultSet.getString(2));
			result.setDenominatorTableDescription(resultSet.getString(3));
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		return result;
	}
	
	private void addCovariates(
		final Connection connection,
		final Geography geography,
		final Investigation investigation) {
		
		//add in covariates
		SQLInsertQueryFormatter insertCovariatesQueryFormatter
			= new SQLInsertQueryFormatter();
		insertCovariatesQueryFormatter.setIntoTable("rif40_inv_covariates");
		insertCovariatesQueryFormatter.addInsertField("covariate_name");		
		insertCovariatesQueryFormatter.addInsertField("min");
		insertCovariatesQueryFormatter.addInsertField("max");
		insertCovariatesQueryFormatter.addInsertField("geography");
		insertCovariatesQueryFormatter.addInsertField("study_geolevel_name");			
		
		PreparedStatement statement = null;
		
		/*
		try {
			statement 
				= connection.prepareStatement(insertCovariatesQueryFormatter.generateQuery());			
			statement.setString(1, )
			
			
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		*/
	}
	
	private ArrayList<AdjustableCovariate> getCovariatesForInvestigation(
		final Connection connection,
		final String userID,
		final String studyID,
		final String investigationID) 
		throws SQLException,
		RIFServiceException {
		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.addSelectField("covariate_name");
		queryFormatter.addSelectField("min");
		queryFormatter.addSelectField("max");
		queryFormatter.addFromTable("rif40_inv_covariates");
		queryFormatter.addWhereParameter("username");
		queryFormatter.addWhereParameter("study_id");
		queryFormatter.addWhereParameter("inv_id");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ArrayList<AdjustableCovariate> results 
			= new ArrayList<AdjustableCovariate>();
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, userID);
			statement.setString(2, studyID);
			statement.setString(3, investigationID);
			resultSet = statement.executeQuery();
			while (resultSet.next() ) {
				AdjustableCovariate adjustableCovariate
					= AdjustableCovariate.newInstance();
				adjustableCovariate.setCovariateType(CovariateType.NTILE_INTEGER_SCORE);
				adjustableCovariate.setName(resultSet.getString(1));
				String minimumValue = String.valueOf(resultSet.getDouble(2));
				adjustableCovariate.setMinimumValue(minimumValue);
				String maximumValue = String.valueOf(resultSet.getDouble(3));
				adjustableCovariate.setMaximumValue(maximumValue);
				results.add(adjustableCovariate);				
			}
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);			
		}
		
		return results;		
	}
		

	
	
	
	/*
	
	private ArrayList<Investigation> getInvestigationsForStudy(
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
	
	/*
	public ArrayList<RIFJobSubmission> getRIFJobSubmissionsForUser(
		final Connection connection,
		final User user) {
		
		//Step 1: Obtain study records
		
		
		//Step 2: Obtain investigations for a given study
		
		//Step 3: Find health outcomes for a given investigation
		
		//Step 4: Find covariates for a given investigation
		

		
		
		
	}
	*/
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	private void checkNonExistentItems(
		final Connection connection,
		final RIFStudySubmission rifStudySubmission)
		throws RIFServiceException {

		Project project 
			= rifStudySubmission.getProject();
		checkProjectExists(
			connection,
			project);
		
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) rifStudySubmission.getStudy();
		diseaseMappingStudyManager.checkNonExistentItems(
			connection, 
			diseaseMappingStudy);
		
		ArrayList<CalculationMethod> calculationMethods
			= rifStudySubmission.getCalculationMethods();
		for (CalculationMethod calculationMethod : calculationMethods) {
			checkCalculationMethodExists(
				connection, 
				calculationMethod);
		}
		
		//we can assume that all rif output options that are supplied
		//do in fact exist because they are taken from an enumerated type
				
		
		
		
		
	}
	
	private void checkProjectExists(
		final Connection connection,
		final Project project) 
		throws RIFServiceException {
		
		//Create SQL query
		SQLRecordExistsQueryFormatter query
			= new SQLRecordExistsQueryFormatter();
		query.setLookupKeyFieldName("project");
		query.setFromTable("t_rif40_projects");
		
		//KLG: TODO - change table name
		
		PreparedStatement checkProjectExistsStatement = null;
		ResultSet checkProjectExistsResultSet = null;
		
		//Parameterise and execute query		
		try {
			checkProjectExistsStatement
				= connection.prepareStatement(query.generateQuery());
			checkProjectExistsStatement.setString(1, project.getName());
			checkProjectExistsResultSet 
				= checkProjectExistsStatement.executeQuery();
			
			if (checkProjectExistsResultSet.next() == false) {
				//ERROR: no such project exists
				String recordType
					= RIFServiceMessages.getMessage("project.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						project.getName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_PROJECT, 
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String recordType
				= RIFServiceMessages.getMessage("project.label");			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					project.getName());

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
					
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(checkProjectExistsStatement);
			SQLQueryUtility.close(checkProjectExistsResultSet);			
		}		
	}
	
	private void checkCalculationMethodExists(
		final Connection connection,
		final CalculationMethod calculationMethod) 
		throws RIFServiceException {
		
		//@TODO: Implement when RIF is able to register R routines
	}
	
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
