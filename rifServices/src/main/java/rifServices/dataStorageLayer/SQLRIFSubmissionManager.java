package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.*;

import rifServices.system.*;
import rifServices.util.RIFLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
	public void clearRIFJobSubmissions(
		Connection connection)
		throws RIFServiceException {
	
		/*
		SQLDeleteQueryFormatter deleteStudiesQueryFormatter
			= new SQLDeleteQueryFormatter();
		deleteStudiesQueryFormatter.setFromTable("rif40_studies");

		SQLDeleteQueryFormatter deleteInvestigationsQueryFormatter
			= new SQLDeleteQueryFormatter();
		deleteInvestigationsQueryFormatter.setFromTable("rif40_investigations");
		
		
		PreparedStatement deleteStudiesStatement = null;
		PreparedStatement deleteInvestigationsStatement = null;		
		try {
			deleteStudiesStatement 
				= connection.prepareStatement(deleteStudiesQueryFormatter.generateQuery());
			deleteStudiesStatement.executeQuery();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"rifJobSubmissionManager.error.unableToDeleteStudies");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.UNABLE_DELETE_STUDIES, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(deleteStudiesStatement);
			SQLQueryUtility.close(deleteInvestigationsStatement);
		}
		*/	
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
		
		DiseaseMappingStudy diseaseMappingStudy
			= (DiseaseMappingStudy) rifJobSubmission.getStudy();

		//verify that year range checks against the database
		
		//verify that all the age groups of all the age bands are
		//in the database

		//Step 1: Add an entry to the study view
		
		SQLInsertQueryFormatter insertStudyQueryFormatter
			= new SQLInsertQueryFormatter();
		insertStudyQueryFormatter.setIntoTable("rif40_studies");
		insertStudyQueryFormatter.addInsertField("username");
		//insertStudyQueryFormatter.addInsertField("study_id");
		//insertStudyQueryFormatter.addInsertField("extract_table");

		insertStudyQueryFormatter.addInsertField("project");
		insertStudyQueryFormatter.addInsertField("project_description");
		insertStudyQueryFormatter.addInsertField("study_name");
		insertStudyQueryFormatter.addInsertField("summary");
		insertStudyQueryFormatter.addInsertField("description");
		insertStudyQueryFormatter.addInsertField("other_notes");
		//insertStudyQueryFormatter.addInsertField("study_date");
		insertStudyQueryFormatter.addInsertField("geography");
		//Until we develop risk analysis studies, study_type will
		//be '1'
		insertStudyQueryFormatter.addInsertField("study_type");
		//Here, study state will always be 'C' for created
		insertStudyQueryFormatter.addInsertField("study_state");
		
		//@TODO probably geo level view?? clarify meaning
		insertStudyQueryFormatter.addInsertField("comparison_geolevel_name");
		insertStudyQueryFormatter.addInsertField("denom_tab");
		
		//???
		//insertStudyQueryFormatter.addInsertField("direct_stand_tab");

		/**
		 * @TODO
		 * year_start, year_stop, max_age_group, min_age_group
		 * should not be in a study but at the investigation
		 * for now, just use data from the first investigation
		 */
		insertStudyQueryFormatter.addInsertField("year_start");
		insertStudyQueryFormatter.addInsertField("year_stop");
		
		//@TODO: min and max will be the min age group and max
		//age group taken across consecutive age bands
		insertStudyQueryFormatter.addInsertField("max_age_group");
		insertStudyQueryFormatter.addInsertField("min_age_group");
		insertStudyQueryFormatter.addInsertField("study_geolevel_name");
		
		//@TODO: clarify where this comes from
		//insertStudyQueryFormatter.addInsertField("map_table");
		//@TODO specify this as an option.  For now, have a default of 5
		insertStudyQueryFormatter.addInsertField("suppression_value");
		
		//@TODOFor now, put '1' for true
		insertStudyQueryFormatter.addInsertField("extract_permitted");
		//@TODO For now, put '1' for true
		insertStudyQueryFormatter.addInsertField("transfer_permitted");
		insertStudyQueryFormatter.addInsertField("authorised_by");
		//@TODO for now, put NOW() time stamp
		insertStudyQueryFormatter.addInsertField("authorised_on");
		//@TODO for now, put empty string
		insertStudyQueryFormatter.addInsertField("authorised_notes");

		//@TODO clarify meaning
		//insertStudyQueryFormatter.addInsertField("audsid");
		//@TODO clarify whether what values this can have in PostGresQL
		//for now, assume 4 is OK
		insertStudyQueryFormatter.addInsertField("partition_parallelisation");

		//@TODO: Where to obtain this?
		insertStudyQueryFormatter.addInsertField("covariate_table");

		PreparedStatement addStudyStatement = null;
		try {
			addStudyStatement 
				= connection.prepareStatement(insertStudyQueryFormatter.generateQuery());
			addStudyStatement.setString(1, user.getUserID());
			
			Project project = rifJobSubmission.getProject();
			addStudyStatement.setString(2, project.getName());
			addStudyStatement.setString(3, project.getDescription());

			DiseaseMappingStudy study 
				= (DiseaseMappingStudy) rifJobSubmission.getStudy();
			addStudyStatement.setString(4, study.getName());
			//For now, put empty string for 'summary'
			addStudyStatement.setString(5, "");
			addStudyStatement.setString(6, study.getDescription());

			//@TODO: We need to obtain 'other_notes', which appears
			//at the end, just before the users preview their study
			//for now, put empty string
			addStudyStatement.setString(7, "");
			
			Geography geography = study.getGeography();
			addStudyStatement.setString(8, geography.getName());

			//Set '1' for disease mapping study
			addStudyStatement.setInt(9, 1);
			
			//Set 'C' for created but not verified
			addStudyStatement.setString(10, "C");
			
			//setting comparison_geolevel_name
			ComparisonArea comparisonArea = study.getComparisonArea();
			GeoLevelToMap comparisonAreaGeoLevelToMap
				= comparisonArea.getGeoLevelToMap();
			addStudyStatement.setString(11, comparisonAreaGeoLevelToMap.getName());
			
			//set denom_tab
			//Note that this varies from investigation to investigation
			//and does not belong at study level.  For now, just take
			//the numerator denominator pair from the first investigation
			ArrayList<Investigation> investigations
				= study.getInvestigations();
			Investigation firstInvestigation = investigations.get(0);
			NumeratorDenominatorPair ndPair
				= firstInvestigation.getNdPair();
			addStudyStatement.setString(12, ndPair.getDenominatorTableName());
			
			YearRange yearRange = firstInvestigation.getYearRange();
			
			//setting year_start
			addStudyStatement.setInt(13, Integer.valueOf(yearRange.getLowerBound()));
			//setting year_stop
			addStudyStatement.setInt(14, Integer.valueOf(yearRange.getUpperBound()));
			
			AgeGroup maximumAgeGroup
				= firstInvestigation.getMaximumAgeGroup();
			//setting max_age_group
			addStudyStatement.setInt(15, Integer.valueOf(maximumAgeGroup.getUpperLimit()));
			AgeGroup minimumAgeGroup
				= firstInvestigation.getMinimumAgeGroup();
			//setting min_age_group
			addStudyStatement.setInt(16, Integer.valueOf(minimumAgeGroup.getLowerLimit()));
			
			//study_geolevel_name : for now, just use the same geoLevelView
			//as above
			DiseaseMappingStudyArea studyArea
				= study.getDiseaseMappingStudyArea();
			GeoLevelToMap studyGeoLevelToMap 
				= studyArea.getGeoLevelToMap();
			addStudyStatement.setString(17, studyGeoLevelToMap.getName());
			
			//suppression_value: for now, set it to 5
			addStudyStatement.setInt(18, 5);
			
			//extract_permitted: for now, '1' for true
			addStudyStatement.setInt(19, 1);

			//transfer_permitted: for now, '1' for true
			addStudyStatement.setInt(20, 1);
			
			//authorised_by: for now, put user name
			addStudyStatement.setString(21, user.getUserID());

			//study_date
	
			addStudyStatement.setDate(22, new Date(System.currentTimeMillis()));
			//authorised_notes: for now, use empty string
			addStudyStatement.setString(23, "");
			
			//partition_parallelisation: for now, put 4
			addStudyStatement.setInt(24, 4);
					
			addStudyStatement.setString(25, "some_covariate_table");
			
			
			
			addStudyStatement.executeUpdate();
						
			/*
			addInvestigations(
				connection,
				user,
				geography,
				study);
			*/
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
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(addStudyStatement);
		}
	}
	
	private void addInvestigations(
		Connection connection,
		User user,
		Geography geography,
		AbstractStudy study) 
		throws SQLException,
		RIFServiceException {
		
		SQLInsertQueryFormatter insertInvestigationQueryFormatter
			= new SQLInsertQueryFormatter();
		insertInvestigationQueryFormatter.setIntoTable("rif40_investigations");
		insertInvestigationQueryFormatter.addInsertField("username");
		//insertInvestigationQueryFormatter.addInsertField("inv_id");
		//insertInvestigationQueryFormatter.addInsertField("study_id");
		insertInvestigationQueryFormatter.addInsertField("inv_name");
		insertInvestigationQueryFormatter.addInsertField("inv_description");
		insertInvestigationQueryFormatter.addInsertField("year_start");
		insertInvestigationQueryFormatter.addInsertField("year_stop");
		insertInvestigationQueryFormatter.addInsertField("max_age_group");
		insertInvestigationQueryFormatter.addInsertField("min_age_group");
		insertInvestigationQueryFormatter.addInsertField("genders");
		insertInvestigationQueryFormatter.addInsertField("numer_tab");
		//insertInvestigationQueryFormatter.addInsertField("mh_test_type");
		insertInvestigationQueryFormatter.addInsertField("geography");
		//insertInvestigationQueryFormatter.addInsertField("classifier");
		insertInvestigationQueryFormatter.addInsertField("classifier_bands");
		insertInvestigationQueryFormatter.addInsertField("investigation_state");
		
		ArrayList<Investigation> investigations = study.getInvestigations();
		PreparedStatement insertInvestigationStatement = null;
		
		//@TODO
		//This field should not appear in investigations for future releases
		String geographyName = geography.getName();
		try {
			insertInvestigationStatement
				 = connection.prepareStatement(insertInvestigationQueryFormatter.generateQuery());
			
			for (Investigation investigation : investigations) {
				insertInvestigationStatement.setString(1, user.getUserID());
				insertInvestigationStatement.setString(2, investigation.getTitle());
				
				//@TODO investigation should have its own description field
				insertInvestigationStatement.setString(3, "");
				
				YearRange yearRange = investigation.getYearRange();
				//year_start
				insertInvestigationStatement.setInt(4, Integer.valueOf(yearRange.getLowerBound()));
				//year_stop
				insertInvestigationStatement.setInt(5, Integer.valueOf(yearRange.getUpperBound()));
				//max_age_group
				AgeGroup maximumAgeGroup = investigation.getMaximumAgeGroup();
				insertInvestigationStatement.setInt(6, Integer.valueOf(maximumAgeGroup.getUpperLimit()));
				//min_age_group
				AgeGroup minimumAgeGroup = investigation.getMinimumAgeGroup();
				insertInvestigationStatement.setInt(7, Integer.valueOf(minimumAgeGroup.getLowerLimit()));
				//genders
				Sex sex = investigation.getSex();
				if (sex == Sex.MALES) {
					insertInvestigationStatement.setInt(8, 1);	
				}
				else if (sex == Sex.FEMALES){
					insertInvestigationStatement.setInt(8, 2);	
				}	
				else {
					//assume both
					insertInvestigationStatement.setInt(8, 3);	
				}
				//numer_tab
				NumeratorDenominatorPair ndPair = investigation.getNdPair();
				insertInvestigationStatement.setString(9, ndPair.getNumeratorTableName());
				insertInvestigationStatement.setString(10, geographyName);
				//classifier_bands default: 5
				insertInvestigationStatement.setInt(11, 5);
				//investigation_state: put 'C' for created but not verified
				insertInvestigationStatement.setString(12, "C");
				
				insertInvestigationStatement.executeUpdate();
			}		
		}
		finally {
			SQLQueryUtility.close(insertInvestigationStatement);
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
