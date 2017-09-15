package rifServices.dataStorageLayer.ms;

import rifServices.businessConceptLayer.*;
import rifServices.system.*;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.*;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;

import java.sql.*;

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
 * Copyright 2017 Imperial College London, developed by the Small Area
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

final class MSSQLRIFSubmissionManager 
	extends MSSQLAbstractSQLManager {

	public static void main(String[] args) {
		
		
	}
	
	
	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	private MSSQLStudyStateManager studyStateManager;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQLRIF submission manager.
	 */
	public MSSQLRIFSubmissionManager(
		final RIFDatabaseProperties rifDatabaseProperties,
		final MSSQLRIFContextManager rifContextManager,
		final MSSQLAgeGenderYearManager ageGenderYearManager,
		final MSSQLCovariateManager covariateManager,
		final MSSQLDiseaseMappingStudyManager diseaseMappingStudyManager,
		final MSSQLMapDataManager mapDataManager,
		final MSSQLStudyStateManager studyStateManager) {

		super(rifDatabaseProperties);		
		this.studyStateManager = studyStateManager;
		
		setEnableLogging(false);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String deleteStudy(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
		
		
		String result = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			
			PGSQLFunctionCallerQueryFormatter queryFormatter = new PGSQLFunctionCallerQueryFormatter();
			queryFormatter.setDatabaseSchemaName("rif40_sm_pkg");
			queryFormatter.setFunctionName("rif40_delete_study");
			queryFormatter.setNumberOfFunctionParameters(1);

			logSQLQuery(
				"deleteStudy", 
				queryFormatter,
				studyID);
			
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet
				= statement.executeQuery();
			resultSet.next();
			
			result = String.valueOf(resultSet.getBoolean(1));	
			
			connection.commit();
			
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToDeleteStudy",
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				MSSQLRIFSubmissionManager.class, 
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
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
	}
	

	public void deleteStudy(
		final Connection connection,
		final User user,
		final AbstractStudy study)
		throws RIFServiceException {
		
		
		String studyID 
			= study.getIdentifier();
		studyStateManager.checkNonExistentStudyID(
			connection,
			user,
			studyID);
		
		PreparedStatement statement = null;
		try {
			PGSQLFunctionCallerQueryFormatter queryFormatter 
				= new PGSQLFunctionCallerQueryFormatter();
			queryFormatter.setDatabaseSchemaName("rif40_sm_pkg");
			queryFormatter.setFunctionName("rif40_delete_study");
			queryFormatter.setNumberOfFunctionParameters(1);
		
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setString(1, study.getIdentifier());
			statement.executeUpdate();
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToDeleteStudy",
					study.getName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				MSSQLAgeGenderYearManager.class, 
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
			PGSQLQueryUtility.close(statement);
		}	
	}
	
	public RIFStudySubmission getRIFStudySubmission(
		final Connection connection,
		final User user,
		final String studyID)
		throws RIFServiceException {
		
		MSSQLSampleTestObjectGenerator testDataGenerator
			= new MSSQLSampleTestObjectGenerator();
		RIFStudySubmission rifStudySubmission
			= testDataGenerator.createSampleRIFJobSubmission();
		DiseaseMappingStudy diseaseMappingStudy
			= getDiseaseMappingStudy(
				connection, 
				user, 
				studyID);		
		rifStudySubmission.setStudy(diseaseMappingStudy);
		
		return rifStudySubmission;
	}


	/*
	 * Methods below are for retrieving a study
	 */
	public DiseaseMappingStudy getDiseaseMappingStudy(
		final Connection connection,
		final User user,
		final String studyID) 
		throws RIFServiceException {
				
		//check for non-existent study given id
		//checkNonExistentStudy(studyID);
		
		studyStateManager.checkNonExistentStudyID(
			connection,
			user,
			studyID);

		DiseaseMappingStudy result
			= DiseaseMappingStudy.newInstance();
		try {
			result.setIdentifier(studyID);
		
			retrieveGeneralInformationForStudy(
				connection,
				result);
		
			retrieveStudyAreaForStudy(
				connection,
				result);
		
			retrieveComparisonAreaForStudy(
				connection,
				result);

			retrieveInvestigationsForStudy(
				connection,
				user, 
				result);
			return result;
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToGetDiseaseMappingStudy",
					studyID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}		
	}
	
	private void retrieveGeneralInformationForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy) 
		throws SQLException,
		RIFServiceException {
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40.rif40_studies");
			queryFormatter.addSelectField("study_name");
			queryFormatter.addSelectField("geography");
			//queryFormatter.addSelectField("project");
			queryFormatter.addSelectField("comparison_geolevel_name");
			queryFormatter.addSelectField("study_geolevel_name");
			queryFormatter.addSelectField("denom_tab");
			queryFormatter.addWhereParameter("study_id");
			
			logSQLQuery(
				"retrieveGeneralInformationForStudy", 
				queryFormatter,
				diseaseMappingStudy.getIdentifier());		
			
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setInt(1, Integer.valueOf(diseaseMappingStudy.getIdentifier()));
			
			resultSet = statement.executeQuery();
			resultSet.next();
			
			diseaseMappingStudy.setName(resultSet.getString(1));
			Geography geography
				= Geography.newInstance(resultSet.getString(2), "");
			diseaseMappingStudy.setGeography(geography);		
			
			//KLG: Note that we cannot reconstitute geolevel select, geolevel view,
			//geo level area -- just 'to map'
			GeoLevelToMap comparisonAreaGeoLevelToMap
				= GeoLevelToMap.newInstance(resultSet.getString(3));
			ComparisonArea comparisonArea
				= diseaseMappingStudy.getComparisonArea();
			comparisonArea.setGeoLevelToMap(comparisonAreaGeoLevelToMap);
			
			GeoLevelToMap diseaseMappingStudyAreaGeoLevelToMap
				= GeoLevelToMap.newInstance(resultSet.getString(3));
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= diseaseMappingStudy.getDiseaseMappingStudyArea();
			diseaseMappingStudyArea.setGeoLevelToMap(diseaseMappingStudyAreaGeoLevelToMap);
			
			//retrieving denom is awkward because we need both denom and numer to
			//reconstitute ndPair
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}		
	}
		
	private void retrieveStudyAreaForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws SQLException,
		RIFServiceException {
					
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40.rif40_study_areas");
			queryFormatter.addSelectField("area_id");
			queryFormatter.addSelectField("band_id");
			queryFormatter.addWhereParameter("study_id");
			
			logSQLQuery(
				"retrieveStudyAreaForStudy", 
				queryFormatter,
				diseaseMappingStudy.getIdentifier());	
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(1, Integer.valueOf(diseaseMappingStudy.getIdentifier()));
						
			DiseaseMappingStudyArea diseaseMappingStudyArea
				= DiseaseMappingStudyArea.newInstance();
			//KLG: TODO - how can we improve this so we can add in extra
			//information?
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String geographicalIdentifier
					= resultSet.getString(1);
				MapArea mapArea = MapArea.newInstance(
					geographicalIdentifier, 
					geographicalIdentifier, 
					geographicalIdentifier);
				diseaseMappingStudyArea.addMapArea(mapArea);
			}
				diseaseMappingStudy.setDiseaseMappingStudyArea(diseaseMappingStudyArea);
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
	}	
	
	private void retrieveComparisonAreaForStudy(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws SQLException,
		RIFServiceException {
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
			= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40.rif40_comparison_areas");
			queryFormatter.addSelectField("area_id");
			queryFormatter.addWhereParameter("study_id");
			
			logSQLQuery(
				"retrieveComparisonAreaForStudy", 
				queryFormatter,
				diseaseMappingStudy.getIdentifier());	

			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(1, Integer.valueOf(diseaseMappingStudy.getIdentifier()));
						
			ComparisonArea comparisonArea
				= ComparisonArea.newInstance();
			//KLG: TODO - how can we improve this so we can add in extra
			//information?
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String geographicalIdentifier
					= resultSet.getString(1);
				MapArea mapArea = MapArea.newInstance(
					geographicalIdentifier, 
					geographicalIdentifier, 
					geographicalIdentifier);
				comparisonArea.addMapArea(mapArea);
			}

			diseaseMappingStudy.setComparisonArea(comparisonArea);
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
	}
	
	private void retrieveInvestigationsForStudy(
		final Connection connection,
		final User user, 
		final DiseaseMappingStudy diseaseMappingStudy)
		throws SQLException,
		RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.addSelectField("inv_id");
			queryFormatter.addSelectField("inv_name");
			queryFormatter.addSelectField("inv_description");
			queryFormatter.addSelectField("year_start");
			queryFormatter.addSelectField("year_stop");
			queryFormatter.addSelectField("max_age_group");
			queryFormatter.addSelectField("min_age_group");
			queryFormatter.addSelectField("genders");
			queryFormatter.addSelectField("numer_tab");
		
			queryFormatter.addFromTable("rif40.rif40_investigations");
			queryFormatter.addWhereParameter("study_id");
			
			logSQLQuery(
					"retrieveInvestigationsForStudy", 
					queryFormatter,
					diseaseMappingStudy.getIdentifier());
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(1, Integer.valueOf(diseaseMappingStudy.getIdentifier()));
			resultSet
				= statement.executeQuery();
			while (resultSet.next()) {
				
				Investigation investigation = Investigation.newInstance();
				//set identifier
				int ithParameter = 1;
				investigation.setIdentifier(
					resultSet.getString(ithParameter++));

				//set title
				investigation.setTitle(
					resultSet.getString(ithParameter++));
				//set description
				investigation.setDescription(
					resultSet.getString(ithParameter++));
		
				//set year range
				int startYearValue
					= resultSet.getInt(ithParameter++);
				int stopYearValue
					= resultSet.getInt(ithParameter++);
				YearRange yearRange
					= YearRange.newInstance(
						String.valueOf(startYearValue), 
						String.valueOf(stopYearValue));
				investigation.setYearRange(yearRange);
				
				
				//set the age bands.  
				//KLG: To do: we are not able to reconstitute age bands properly
				int maximumAgeGroupID
					= resultSet.getInt(ithParameter++);
				AgeGroup upperLimitAgeGroup
					= getAgeGroupFromIdentifier(
						connection,
						maximumAgeGroupID);
				int minimumAgeGroupID
					= resultSet.getInt(ithParameter++);
				AgeGroup lowerLimitAgeGroup
					= getAgeGroupFromIdentifier(
						connection,
						minimumAgeGroupID);				
				AgeBand ageBand
					= AgeBand.newInstance(lowerLimitAgeGroup, upperLimitAgeGroup);
				investigation.addAgeBand(ageBand);
								
				//set the sex value
				int sexIntValue
					= resultSet.getInt(ithParameter++);
				if (sexIntValue == 1) {
					investigation.setSex(Sex.MALES);
				}
				else if (sexIntValue == 2) {
					investigation.setSex(Sex.FEMALES);
				}
				else {
					investigation.setSex(Sex.BOTH);
				}
		
				//set the numerator denominator values
				String numeratorTableName
					= resultSet.getString(ithParameter++);
				NumeratorDenominatorPair ndPair
					= getNDPairForNumeratorTableName(
						connection,
						user,
						numeratorTableName);
				investigation.setNdPair(ndPair);
			
				diseaseMappingStudy.addInvestigation(investigation);				
			}
			
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
	}
		
	/**
	 * We assume the age group identifier will be valid
	 * @param connection
	 * @param ageGroupIdentifier
	 * @return
	 * @throws SQLException
	 */
	private AgeGroup getAgeGroupFromIdentifier(
		final Connection connection,
		final int ageGroupIdentifier) 
		throws SQLException,
		RIFServiceException {
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		AgeGroup result = null;
		try {
			
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			queryFormatter.addFromTable("rif40.rif40_age_groups");
			queryFormatter.addSelectField("low_age");
			queryFormatter.addSelectField("high_age");
			queryFormatter.addSelectField("fieldname");	
			queryFormatter.addWhereParameter("age_group_id");
			queryFormatter.addWhereParameter("\"offset\"");
			
			logSQLQuery(
					"getAgeGroupFromIdentifier", 
					queryFormatter,
					"1",
					String.valueOf(ageGroupIdentifier));
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(1, 1);
			statement.setInt(2, Integer.valueOf(ageGroupIdentifier));
			resultSet
				= statement.executeQuery();
			resultSet.next();
			result
				= AgeGroup.newInstance(
					String.valueOf(ageGroupIdentifier), 					
					resultSet.getString(1), 
					resultSet.getString(2), 
					resultSet.getString(3));		
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}
		
		return result;
	}
	
	private NumeratorDenominatorPair getNDPairForNumeratorTableName(
		final Connection connection,
		final User user, 
		final String numeratorTableName) 
		throws SQLException,
		RIFServiceException {

		ResultSet resultSet = null;
		NumeratorDenominatorPair result = null;
		PreparedStatement statement = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setUseDistinct(true);
			queryFormatter.addSelectField("numerator_description");
			queryFormatter.addSelectField("denominator_table");
			queryFormatter.addSelectField("denominator_description");		
			queryFormatter.addFromTable(user.getUserID() + ".rif40_num_denom");
			queryFormatter.addWhereParameter("numerator_table");		

			logSQLQuery(
					"getNDPairForNumeratorTableName", 
					queryFormatter,
					String.valueOf(numeratorTableName));			
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, numeratorTableName);
			resultSet = statement.executeQuery();
			resultSet.next();
			result = NumeratorDenominatorPair.newInstance();
			result.setNumeratorTableName(numeratorTableName);
			result.setNumeratorTableDescription(resultSet.getString(1));
			result.setDenominatorTableName(resultSet.getString(2));
			result.setDenominatorTableDescription(resultSet.getString(3));
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}
		
		return result;
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
