package rifServices.dataStorageLayer;

import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.businessConceptLayer.StudyState;
import rifServices.businessConceptLayer.StudySummary;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.SQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLUpdateQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

final class SQLStudyStateManager 
	extends AbstractSQLManager {

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
	 * Instantiates a new SQL covariate manager.
	 *
	 * @param sqlRIFContextManager the sql rif context manager
	 */
	public SQLStudyStateManager(
		final RIFDatabaseProperties rifDatabaseProperties) {
		
		super(rifDatabaseProperties);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	/**
	 * Gets the covariates.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelToMap the geo level to map
	 * @return the covariates
	 * @throws RIFServiceException the RIF service exception
	 */
	public StudyState getStudyState(
		final Connection connection,
		final User user,
		final String studyID) 
		throws RIFServiceException {
				
		StudyState result = null;
		//Validate parameters		
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
				
		try {
			/*
			 * Construct an SQL query of the form:
			 * 
			 * SELECT
			 * 	study_state
			 * FROM
			 * 	rif40.rif_studies
			 * WHERE
			 * 	study_id=? AND
			 * 	username=?
			 */
			SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
			queryFormatter.setDatabaseSchemaName("rif40");
			//queryFormatter.set
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.addSelectField("study_state");
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addWhereParameter("study_id");
			queryFormatter.addWhereParameter("username");
		
			logSQLQuery(
				"getStudyState",
				queryFormatter,
				studyID,
				user.getUserID());
		
			//Parameterise and execute query
			
			
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.setString(1, studyID);
			statement.setString(2, user.getUserID());
			dbResultSet = statement.executeQuery();
			connection.commit();
			if (dbResultSet.next() == false) {
				result = StudyState.STUDY_NOT_CREATED;
			}
			else {
				String studyStateNameValue
					= dbResultSet.getString(1);
				result = StudyState.getStudyStateFromName(studyStateNameValue);
			}
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"studyStateManager.error.unableToGetStudyState",
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLStudyStateManager.class, 
				errorMessage, 
				sqlException);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.UNABLE_TO_GET_STUDY_STATE, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}
		
		assert result != null;
		return result;
	}
	
	public void updateStudyState(
		final Connection connection, 
		final User user,
		final String studyID, 
		final StudyState studyState) 
		throws RIFServiceException {
		
		if (studyState == StudyState.STUDY_NOT_CREATED) {
			return;
		}
		
		/*
		 * Constructing a query of the form:
		 * 
		 * UPDATE 
		 * 	rif40.rif_studies
		 * SET 
		 * 	study_state=?
		 * WHERE 
		 * 	study_id=? AND
		 * 	username=?
		 */
		SQLUpdateQueryFormatter queryFormatter = new SQLUpdateQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.setUpdateTable("rif_studies");
		queryFormatter.addUpdateField("study_state");
		queryFormatter.addWhereParameter("study_id");
		queryFormatter.addWhereParameter("username");
		
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, studyState.getCode());
			statement.setString(2, studyID);
			statement.setString(3, user.getUserID());
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);

			String errorMessage
				= RIFServiceMessages.getMessage(
					"studyStateManager.error.unableToUpdateStudyState", 
					studyID, 
					studyState.getCode());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {			
			SQLQueryUtility.close(statement);
		}
	}
	
	/**
	 * Returns a list of study summaries, each describing a study that is in a given
	 * study state
	 * @param user
	 * @return
	 */
	public ArrayList<StudySummary> getStudiesInState(
		final Connection connection,
		final User user,
		final StudyState studyState) 
		throws RIFServiceException {
		
		ArrayList<StudySummary> results = new ArrayList<StudySummary>();
	
		/*
		 * Constructing a query of the form:
		 * SELECT
		 * 	study_id,
		 * 	study_name,
		 *  description,
		 *  study_state
		 * FROM
		 * 	rif40.rif40_studies
		 * WHERE
		 * 	rif40.rif40_studies.study_state=? AND
		 *  username=?
		 */		
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40");	
		queryFormatter.addSelectField("study_id");		
		queryFormatter.addSelectField("study_name");
		queryFormatter.addSelectField("description");	
		queryFormatter.addSelectField("study_state");
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addWhereParameter("study_state");
		queryFormatter.addWhereParameter("username");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			String studyStateName = studyState.getCode();
			statement.setString(1, studyStateName);
			statement.setString(2, user.getUserID());
			
			resultSet = statement.executeQuery();
			while (resultSet.next()) {

				String studyID = String.valueOf(resultSet.getInt(1));
				String studyName = resultSet.getString(2);
				String studyDescription = resultSet.getString(3);
				StudySummary studySummary 
					= StudySummary.newInstance(
						studyID, 
						studyName, 
						studyDescription);
				
				results.add(studySummary);				
			}
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);

			String errorMessage
				= RIFServiceMessages.getMessage(
					"",
					studyState.getCode());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			
		}
		finally {
			SQLQueryUtility.close(resultSet);
			SQLQueryUtility.close(statement);
		}
		
		return results;
		
	}
	
	/**
	 * Provides a list of all studies owned by the user, regardless of state
	 * @param connection
	 * @param user
	 * @return
	 * @throws RIFServiceException
	 */
	public ArrayList<StudySummary> getStudySummariesForUser(
		final Connection connection,
		final User user) 
		throws RIFServiceException {
		
		ArrayList<StudySummary> results = new ArrayList<StudySummary>();
		
		/*
		 * Constructing a query of the form:
		 * 
		 * SELECT
		 * 	study_id,
		 * 	study_name,
		 * 	study_description,
		 *  study_state
		 * FROM
		 * 	rif40.rif_studies.username=?
		 * ORDER BY
		 * 	study_name
		 */
		SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.addSelectField("study_id");		
		queryFormatter.addSelectField("study_name");
		queryFormatter.addSelectField("description");		
		queryFormatter.addSelectField("study_state");		
		queryFormatter.addFromTable("rif40_studies");
		queryFormatter.addWhereParameter("username");
		queryFormatter.addOrderByCondition("study_name");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, user.getUserID());
			resultSet = statement.executeQuery();
			while (resultSet.next()) {

				String studyID = String.valueOf(resultSet.getInt(1));
				String studyName = resultSet.getString(2);
				String studyDescription = resultSet.getString(3);
				StudyState studyState 
					= StudyState.getStudyStateFromName(resultSet.getString(4));
				StudySummary studySummary 
					= StudySummary.newInstance(
						studyID, 
						studyName, 
						studyDescription,
						studyState);
				
				results.add(studySummary);				
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);

			String errorMessage
				= RIFServiceMessages.getMessage(
					"", 
					user.getUserID());
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
		
		return results;
	}
	
	public String[] getStudyStatusHistory(
		final Connection connection, 
		final User user,
		final String studyID) 
		throws RIFServiceException {
		
		
		//validate parameters
		checkNonExistentStudyID(
			connection, 
			user,
			studyID);
		
		String[] results = new String[0];
							
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		//try {
			
			
			//stubbed for now
			/*
			 * Something that gets:
			 * time of update
			 * study name
			 * processing stage
			 * status message
			 */
			
			/*
			SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
			
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			resultSet
				= statement.executeQuery();

			ArrayList<String> statusUpdates = new ArrayList<String>();
			while (resultSet.next() ) {			
				String studyName = resultSet.getString(1);
				String processingStage = resultSet.getString(2);
				String currentStatus = resultSet.getString(3);
				Date timeStamp = resultSet.getDate(4);
				String datePhrase = RIFGenericLibraryMessages.getDatePhrase(timeStamp);
						
				String statusUpdate
					= RIFServiceMessages.getMessage(
						"sqlRIFSubmissionManager.statusUpdate",
						datePhrase,
						studyName,
						processingStage,
						currentStatus);
				statusUpdates.add(statusUpdate);
			}
			
			results
				= statusUpdates.toArray(new String[0]);
			
			connection.commit();
			
			return results;			
			*/
			String[] statusUpdates = new String[2];
			statusUpdates[0] = "study has been created but nothing much else is happening with it.";
			statusUpdates[1] = "The RIF has begun to process the study.";			
			return (statusUpdates);
/*			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToGetStatusUpdate",
					studyID);
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
*/		
		
	}
	
	public void addStatusMessage(
		final User user, 
		final String studyID, 
		final String statusMessage) 
		throws RIFServiceException {
		
		
		
	}
	
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	public void checkNonExistentStudyID(
		final Connection connection,
		final User user,
		final String studyID)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			SQLRecordExistsQueryFormatter queryFormatter
				= new SQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setLookupKeyFieldName("study_id");
			queryFormatter.setFromTable("rif40_studies");		
			logSQLQuery(
				"checkNonExistentStudyID", 
				queryFormatter, 
				studyID);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);	
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();

			if (resultSet.next() == false) {
				//ERROR: no such study exists
				String recordType
					= RIFServiceMessages.getMessage("abstractStudy.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						studyID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_STUDY, 
						errorMessage);
				
				connection.commit();
				
				throw rifServiceException;
				
			}	
		
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String recordType
				= RIFServiceMessages.getMessage("abstractStudy.label");			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
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
