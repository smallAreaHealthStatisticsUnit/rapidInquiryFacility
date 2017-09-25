package rifServices.dataStorageLayer.pg;

import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.businessConceptLayer.StudyState;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLDeleteRowsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLCreateTableQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;

import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.lang.Integer;
import org.apache.commons.lang.StringEscapeUtils;

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

final class PGSQLStudyStateManager 
	extends PGSQLAbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();

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
	public PGSQLStudyStateManager(
		final RIFDatabaseProperties rifDatabaseProperties) {
		
		super(rifDatabaseProperties);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	
	public void clearStudyStatusUpdates(
		final Connection connection,
		final User user, 
		final String studyID) 
		throws RIFServiceException {
		
				
		String statusTableName = deriveStatusTableName(user.getUserID());
				
		PGSQLDeleteRowsQueryFormatter queryFormatter
			= new PGSQLDeleteRowsQueryFormatter();
		queryFormatter.setFromTable(statusTableName);
		queryFormatter.addWhereParameter("study_id");
		logSQLQuery(
			"clearStatusUpdates", 
			queryFormatter, 
			"user", 
			"studyID");
		
		PreparedStatement statement = null;		
		try {
			statement 
				= connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			statement.executeUpdate();
			connection.commit();
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.unableToClearStatusMessagesForStudy",
					user.getUserID(),
					studyID);
			RIFServiceException rifServiceExeption
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceExeption;
		}
		finally {
			PGSQLQueryUtility.close(statement);
		}
		
	}
	
	
	
	
	
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

		
		String statusTableName = deriveStatusTableName(user.getUserID());
		try {

			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
			queryFormatter.addQueryLine(0, "WITH ordered_updates AS ");
			queryFormatter.addQueryLine(1, "(SELECT ");		
			queryFormatter.addQueryLine(2, "study_id,");
			queryFormatter.addQueryLine(2, "creation_date,");
			queryFormatter.addQueryLine(2, "row_number() OVER(PARTITION BY study_id ORDER BY ith_update DESC) AS update_number,");
			queryFormatter.addQueryLine(2, "message");
			queryFormatter.addQueryLine(1, "FROM ");
			queryFormatter.addQueryLine(2, statusTableName + "),");
			queryFormatter.addQueryLine(0, "most_recent_updates AS ");
			queryFormatter.addQueryLine(1, "(SELECT ");
			queryFormatter.addQueryLine(2, "study_id,");
			queryFormatter.addQueryLine(2, "creation_date,");
			queryFormatter.addQueryLine(2, "message ");
			queryFormatter.addQueryLine(1, "FROM ");
			queryFormatter.addQueryLine(2, "ordered_updates ");
			queryFormatter.addQueryLine(1, "WHERE ");
			queryFormatter.addQueryLine(2, "update_number = 1) ");
			queryFormatter.addQueryLine(0, "SELECT ");
			queryFormatter.addQueryLine(1, "study_state ");
			queryFormatter.addQueryLine(0, "FROM ");
			queryFormatter.addQueryLine(1, "most_recent_updates ");
			queryFormatter.addQueryLine(0, "WHERE ");
			queryFormatter.addQueryLine(1, "study_id=?");
					
			logSQLQuery(
				"getStudyState",
				queryFormatter,
				studyID);
		
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
					"sqlStudyStateManager.error.unableToGetStudyState",
					studyID);

			rifLogger.error(
				PGSQLStudyStateManager.class, 
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
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(dbResultSet);
		}
		
		assert result != null;
		return result;
	}
	
	private int getIthUpdate(
			final String studyState) {
		
		if (studyState.equals("C")) {
			return 0;
		}
		else if (studyState.equals("V")) {
			return 1;
		}
		else if (studyState.equals("E")) {
			return 2;
		} 
		else if (studyState.equals("G")) {
			return 3;
		}
		else if (studyState.equals("R")) {
			return 4;
		} 		
		else if (studyState.equals("S")) {
			return 5;
		}
		else if (studyState.equals("F")) {
			return 6;
		} 
		else if (studyState.equals("W")) {
			return 7;
		} 
		else {
			return -1;
		}
	}	
	

	public void rollbackStudy(
		final Connection connection, 
		final String studyID)  
		throws RIFServiceException {
		try {
			connection.rollback();
			rifLogger.info(this.getClass(), "PGSQLStudyStateManager: study_id: " + studyID + "; ROLLBACK");	
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);

			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.unableToRollback", 
					studyID);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}			
	}
	
	public void updateStudyStatus(
		final Connection connection, 
		final User user,
		final String studyID, 
		final StudyState studyState,
		final String statusMessage,
		final String traceMessage) 
		throws RIFServiceException {
		
		if (studyState == StudyState.STUDY_NOT_CREATED) {
			return;
		}
				
		/*
		 * We're just adding another entry to the status table. So an update
		 * is really adding a new row
		 */
		String statusTableName = deriveStatusTableName(user.getUserID());
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		if (traceMessage == null) {
			queryFormatter.addQueryLine(0, "INSERT INTO " + statusTableName);
			queryFormatter.addQueryLine(1, " (study_id, study_state, creation_date, ith_update, message) ");
			queryFormatter.addQueryLine(1, "VALUES (?, ?, NOW(), ?, ?)");		
			Integer ithUpdate=new Integer(getIthUpdate(studyState.getCode()));		
			logSQLQuery(
				"updateStudyStatus", 
				queryFormatter,
				studyID.toString(), 
				studyState.getCode(), 
				ithUpdate.toString(), 
				statusMessage);			
		}
		else {
			queryFormatter.addQueryLine(0, "INSERT INTO " + statusTableName);
			queryFormatter.addQueryLine(1, " (study_id, study_state, creation_date, ith_update, message, trace) ");
			queryFormatter.addQueryLine(1, "VALUES (?, ?, NOW(), ?, ?, ?)");		
			Integer ithUpdate=new Integer(getIthUpdate(studyState.getCode()));		
			logSQLQuery(
				"updateStudyStatus", 
				queryFormatter,
				studyID.toString(), 
				studyState.getCode(), 
				ithUpdate.toString(), 
				statusMessage,
				traceMessage);	
		}
			
		PreparedStatement statement = null;
		try {
			rifLogger.info(this.getClass(), "PGSQLStudyStateManager updateStudyStatus: study_id: " + studyID + 
				"; state: " + studyState.getCode() + 
				"; statusMessage: " + statusMessage);
//			createStatusTable(connection, user);
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			statement.setString(2, studyState.getCode());
			statement.setInt(3, getIthUpdate(studyState.getCode()));
			statement.setString(4, statusMessage);	
			if (traceMessage != null) {
				statement.setString(5, traceMessage);
			}
			statement.executeUpdate();
			connection.commit();
			rifLogger.info(this.getClass(), "PGSQLStudyStateManager: study_id: " + studyID + "; COMMIT");
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);

			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.unableToUpdateStudyState", 
					studyID, 
					studyState.getCode());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {			
			PGSQLQueryUtility.close(statement);
		}
	}
		
	
	public RIFResultTable getCurrentStatusAllStudies(
		final Connection connection, 
		final User user) 
		throws RIFServiceException {
		
		
		/*
		 * Assume that each study state in rif40_studies will have at least one
		 * comment in the corresponding status table.
		 *  
		 * WITH ordered_updates AS
		 *    (SELECT
		 *        study_id,
		 *        row_number() OVER(PARTITION BY creation_date) AS update_number,
		 *        creation_date
		 *        message
		 *     FROM
		 *        kgarwood.study_status
		 *     ORDER BY
		 *        creation_date DESC),
		 * most_recent_updates AS
		 *    (SELECT
		 *        study_id,
		 *        creation_date,
		 *        message
		 *     FROM
		 *        ordered_updates
		 *     WHERE
		 *        update_number = 1)
		 * SELECT
		 *    study_id,
		 *    study_date,
		 *    study_state,
		 *    creation_date,
		 *    message
		 * FROM
		 *    rif40.rif40_studies,
		 *    most_recent_updates
		 * WHERE
		 *    rif40.rif40_studies.study_id = kgarwood.study_status.study_id
		 * ORDER BY
		 *    study_date DESC,
		 *    creation_date DESC
		 * 
		 */
		
		RIFResultTable rifResultTable = new RIFResultTable();
		
		String[] columnNames = new String[7];
		columnNames[0] = "study_id";
		columnNames[1] = "study_name";		
		columnNames[2] = "study_description";
		columnNames[3] = "study_state";
		columnNames[4] = "date";
		columnNames[5] = "message";
		columnNames[6] = "trace";
		
		RIFResultTable.ColumnDataType[] columnDataTypes = new RIFResultTable.ColumnDataType[7];
		columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
		columnDataTypes[1] = RIFResultTable.ColumnDataType.TEXT;
		columnDataTypes[2] = RIFResultTable.ColumnDataType.TEXT;
		columnDataTypes[3] = RIFResultTable.ColumnDataType.TEXT;		
		columnDataTypes[4] = RIFResultTable.ColumnDataType.TEXT;
		columnDataTypes[5] = RIFResultTable.ColumnDataType.TEXT;
		columnDataTypes[6] = RIFResultTable.ColumnDataType.TEXT;		

		rifResultTable.setColumnProperties(columnNames, columnDataTypes);
		
		String userID = user.getUserID();
		String rifStudiesTableName = "rif40.rif40_studies";
		String statusTableName
			= deriveStatusTableName(
				userID);
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "WITH ordered_updates AS ");
		queryFormatter.addQueryLine(1, "(SELECT ");		
		queryFormatter.addQueryLine(2, "study_id,");
		queryFormatter.addQueryLine(2, "study_state,");
		queryFormatter.addQueryLine(2, "TO_CHAR(creation_date, \"DD MON YYYY HH24:MI:SS\") AS creation_date,");
		queryFormatter.addQueryLine(2, "row_number() OVER(PARTITION BY study_id ORDER BY ith_update DESC) AS update_number,");
		queryFormatter.addQueryLine(1, "message,");		
		queryFormatter.addQueryLine(1, "trace");
		queryFormatter.addQueryLine(1, "FROM ");
		queryFormatter.addQueryLine(2, statusTableName);
		queryFormatter.addQueryLine(1, "ORDER BY ");
		queryFormatter.addQueryLine(2, "creation_date DESC),");
		queryFormatter.addQueryLine(0, "most_recent_updates AS ");
		queryFormatter.addQueryLine(1, "(SELECT ");
		queryFormatter.addQueryLine(2, "study_id,");
		queryFormatter.addQueryLine(2, "study_state,");
		queryFormatter.addQueryLine(2, "creation_date,");		
		queryFormatter.addQueryLine(1, "message,");		
		queryFormatter.addQueryLine(1, "trace");
		queryFormatter.addQueryLine(1, "FROM ");
		queryFormatter.addQueryLine(2, "ordered_updates ");
		queryFormatter.addQueryLine(1, "WHERE ");
		queryFormatter.addQueryLine(2, "update_number = 1) ");
		queryFormatter.addQueryLine(0, "SELECT ");
		queryFormatter.addQueryLine(1, "most_recent_updates.study_id,");
		queryFormatter.addQueryLine(1, rifStudiesTableName + ".study_name,");
		queryFormatter.addQueryLine(1, rifStudiesTableName + ".description,");
		queryFormatter.addQueryLine(1, "most_recent_updates.study_state,");
		queryFormatter.addQueryLine(1, "creation_date,");		
		queryFormatter.addQueryLine(1, "message,");		
		queryFormatter.addQueryLine(1, "trace");
		queryFormatter.addQueryLine(0, "FROM ");
		queryFormatter.addQueryLine(1, rifStudiesTableName + ",");
		queryFormatter.addQueryLine(1, "most_recent_updates");
		queryFormatter.addQueryLine(0, "WHERE ");
		queryFormatter.addQueryLine(1, rifStudiesTableName + ".study_id = most_recent_updates.study_id");
		queryFormatter.addQueryLine(0, "ORDER BY ");
		queryFormatter.addQueryLine(1, "most_recent_updates.study_id DESC");
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {			

			int expectedNumberOfStatusUpdates
				= getExpectedNumberOfStatusUpdates(
					connection,
					statusTableName,
					rifStudiesTableName);
			
			//Disabled to keep tomcat window clear (called every 4 seconds by front-end)
			/*
			rifLogger.info(this.getClass(), "getCurrentStatusAllStudies 2  number of updates==" + expectedNumberOfStatusUpdates+"==");
*/
			logSQLQuery(
				"getCurrentStatusAllStudies", 
				queryFormatter, 
				"userID");
			
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			resultSet
				= statement.executeQuery();

			queryFormatter.addQueryLine(1, "most_recent_updates.study_id,");
			queryFormatter.addQueryLine(1, rifStudiesTableName + ".study_name,");
			queryFormatter.addQueryLine(1, rifStudiesTableName + ".description,");
			queryFormatter.addQueryLine(1, "most_recent_updates.study_state,");
			queryFormatter.addQueryLine(1, "creation_date,");		
			queryFormatter.addQueryLine(1, "message,");		
			queryFormatter.addQueryLine(1, "trace");			
			
			int ithRecord = 0;
			String[][] data = new String[expectedNumberOfStatusUpdates][7];
			while (resultSet.next() ) {		
				data[ithRecord][0] = resultSet.getString(1); //study ID
				data[ithRecord][1] = resultSet.getString(2); //study name
				data[ithRecord][2] = resultSet.getString(3); //description
				data[ithRecord][3] = resultSet.getString(4); //study state

//				java.util.Date time 
//					= new java.util.Date(resultSet.getDate(5).getTime()); 
//				data[ithRecord][4] 
//					= RIFGenericLibraryMessages.getTimePhrase(time);
				data[ithRecord][4] = resultSet.getString(5); //creation_date formatted as DD MON YYYY HH24:MI:SS
			
				data[ithRecord][5] = resultSet.getString(6); //message
				data[ithRecord][6] = StringEscapeUtils.escapeJavaScript(resultSet.getString(7)); //trace
				if (data[ithRecord][6] == null || data[ithRecord][6].equals("null")) {
					data[ithRecord][6]="";
				}
					
				ithRecord++;
			}
			
			rifResultTable.setData(data);
			
			return rifResultTable;			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.unableToStatusForAllStudies", 
					user.getUserID());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}	
		
	}
	
	private int getExpectedNumberOfStatusUpdates(
		final Connection connection,
		final String statusTableName,
		final String rifStudiesTableName) 
		throws SQLException, 
		RIFServiceException {
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "WITH ordered_updates AS ");
		queryFormatter.addQueryLine(1, "(SELECT ");
		queryFormatter.addQueryLine(2, "study_id,");
		queryFormatter.addQueryLine(2, "study_state,");
		queryFormatter.addQueryLine(2, "creation_date,");
		queryFormatter.addQueryLine(2, "row_number() OVER(PARTITION BY study_id ORDER BY ith_update DESC) AS update_number,");
		queryFormatter.addQueryLine(2, "message");
		queryFormatter.addQueryLine(1, "FROM ");
		queryFormatter.addQueryLine(2, statusTableName);
		queryFormatter.addQueryLine(1, "ORDER BY ");
		queryFormatter.addQueryLine(2, "creation_date DESC),");
		queryFormatter.addQueryLine(0, "most_recent_updates AS ");
		queryFormatter.addQueryLine(1, "(SELECT ");
		queryFormatter.addQueryLine(2, "study_id,");
		queryFormatter.addQueryLine(2, "study_state,");
		queryFormatter.addQueryLine(2, "creation_date,");		
		queryFormatter.addQueryLine(2, "message ");
		queryFormatter.addQueryLine(1, "FROM ");
		queryFormatter.addQueryLine(2, "ordered_updates ");
		queryFormatter.addQueryLine(1, "WHERE ");
		queryFormatter.addQueryLine(2, "update_number = 1) ");
		queryFormatter.addQueryLine(0, "SELECT ");
		queryFormatter.addQueryLine(1, "COUNT(most_recent_updates.study_id) AS number_of_results");
		queryFormatter.addQueryLine(0, "FROM ");
		queryFormatter.addQueryLine(1, rifStudiesTableName + ",");
		queryFormatter.addQueryLine(1, "most_recent_updates");
		queryFormatter.addQueryLine(0, "WHERE ");
		queryFormatter.addQueryLine(1, rifStudiesTableName + ".study_id = most_recent_updates.study_id");

		logSQLQuery(
			"getExpectedNumberOfStatusUpdates", 
			queryFormatter);
			
		//rifLogger.info(this.getClass(), queryFormatter.generateQuery());
		
		Integer result = 0;
		PreparedStatement statement = null;		
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			resultSet = statement.executeQuery();
			resultSet.next();
			result = resultSet.getInt(1);
			
			return result;
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);				
		}
				
	}
	
	
	/*
	 * Creates a status table for a given study, stored
	 * in the schema of the user
	 *
	private void createStatusTable(
		final Connection connection,
		final User user)
		throws SQLException, 
		RIFServiceException {
		
		String userID = user.getUserID();
		String statusTableName
			= deriveStatusTableName(
				userID);
		
		PGSQLCreateTableQueryFormatter queryFormatter
			= new PGSQLCreateTableQueryFormatter();
		queryFormatter.setUseIfExists(true);
		queryFormatter.setTableName(statusTableName);
		queryFormatter.addIntegerFieldDeclaration("study_id", false);
		queryFormatter.addTextFieldDeclaration("study_state", false);
		queryFormatter.addTimeStampFieldDeclaration("creation_date", false);
		queryFormatter.addAutoIncrementFieldDeclaration("ith_update");
		queryFormatter.addTextFieldDeclaration("message", 255, true);

		logSQLQuery(
			"createStatusTable", 
			queryFormatter, 
			"user");
		
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.executeUpdate();
			connection.commit();
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlStudyStateManager.error.unableToCreateUserStudyStatusTable",
					user.getUserID());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			PGSQLQueryUtility.close(statement);	
		}
		
	} */
	
	private String deriveStatusTableName(
		final String userID) {
				
		StringBuilder statusTableName = new StringBuilder();
//		statusTableName.append(userID);
		statusTableName.append("rif40.rif40_study_status");
		
		return statusTableName.toString();
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
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
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
			PGSQLQueryUtility.rollback(connection);
			String recordType
				= RIFServiceMessages.getMessage("abstractStudy.label");			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					studyID);

			rifLogger.error(
				PGSQLRIFContextManager.class, 
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
	

	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
