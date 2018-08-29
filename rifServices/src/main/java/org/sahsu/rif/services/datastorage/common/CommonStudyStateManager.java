package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringEscapeUtils;
import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.DeleteRowsQueryFormatter;
import org.sahsu.rif.generic.datastorage.RecordExistsQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.StudyState;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public final class CommonStudyStateManager extends BaseSQLManager implements StudyStateManager {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	/**
	 * Instantiates a new SQL covariate manager.
	 *
	 * @param sqlRIFContextManager the sql rif context manager
	 */
	public CommonStudyStateManager(
		final RIFServiceStartupOptions options) {
		
		super(options);
	}
	
	@Override
	public void clearStudyStatusUpdates(
		final Connection connection,
		final User user, 
		final String studyID) 
		throws RIFServiceException {
		
				
		String statusTableName = deriveStatusTableName(user.getUserID());
				
		DeleteRowsQueryFormatter queryFormatter = new DeleteRowsQueryFormatter();
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
			SQLQueryUtility.close(statement);
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
	@Override
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
				getClass(),
				errorMessage, 
				sqlException);

			throw new RIFServiceException(
				RIFServiceError.UNABLE_TO_GET_STUDY_STATE,
				errorMessage);
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}
		
		assert result != null;
		return result;
	}

 /*
	C: created, not verified; 
	V: verified, but no other work done; [NOT USED BY MIDDLEWARE]
	E: extracted imported or created, but no results or maps created; 
	G: Extract failure, extract, results or maps not created;
	R: initial results population, create map table; [NOT USED BY MIDDLEWARE]
	S: R success;
	F: R failure, R has caught one or more exceptions [depends on the exception handler design]
	W: R warning. [NOT USED BY MIDDLEWARE]
 */	
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

	@Override
	public void rollbackStudy(
		final Connection connection, 
		final String studyID) 
		throws RIFServiceException {
		try {
			connection.rollback();
			rifLogger.info(this.getClass(), getClass().getSimpleName() + ": study_id: "
			                                + studyID + "; ROLLBACK");
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
	
	@Override
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
			queryFormatter.addQueryLine(1, " (study_id, study_state, ith_update, message) ");
			queryFormatter.addQueryLine(1, "VALUES (?, ?, ?, ?)");
			Integer ithUpdate= getIthUpdate(studyState.getCode());
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
			queryFormatter.addQueryLine(1, " (study_id, study_state, ith_update, message, trace) ");
			queryFormatter.addQueryLine(1, "VALUES (?, ?, ?, ?, ?)");
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
			rifLogger.info(
					getClass(), getClass().getSimpleName()
					            + " updateStudyStatus: study_id: " + studyID + "; state: "
					            + studyState.getCode() + "; statusMessage: " + statusMessage);
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
			rifLogger.info(this.getClass(), getClass().getSimpleName() + ": study_id: " +
			                                studyID + "; COMMIT");
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
			SQLQueryUtility.close(statement);
		}
	}

	@Override
	public RIFResultTable getCurrentStatusAllStudies(
			final Connection connection, 
			final User user) 
			throws RIFServiceException {
				
			/*
  		WITH ordered_updates AS
		     (SELECT
		         study_id,
		         row_number() OVER(PARTITION BY creation_date ORDER BY ith_update ASC) AS update_number,
		         creation_date,
		         message
		      FROM
		         [sahsuland_dev].[peter].[study_status]
		     -- ORDER BY
		        -- creation_date DESC
				 ),
		  most_recent_updates AS
		     (SELECT
		         study_id,
		         creation_date,
		         message
		      FROM
		         ordered_updates
		      WHERE
		         update_number = 1)
		  SELECT
		     a.study_id,
		     study_date,
		     study_state,
		     creation_date,
		     message
		  FROM
		     [sahsuland_dev].[rif40].[rif40_studies] a,
		     most_recent_updates b
		  WHERE
		     a.study_id = b.study_id
		  ORDER BY
		     study_date DESC,
		     creation_date DESC
			 */
			
			RIFResultTable rifResultTable = new RIFResultTable();
			
			String[] columnNames = new String[8];
			columnNames[0] = "study_id";
			columnNames[1] = "study_name";		
			columnNames[2] = "study_description";
			columnNames[3] = "study_state";
			columnNames[4] = "date";
			columnNames[5] = "message";
			columnNames[6] = "trace";
			columnNames[7] = "study_type";
			
			RIFResultTable.ColumnDataType[] columnDataTypes = new RIFResultTable.ColumnDataType[8];
			columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[1] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[2] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[3] = RIFResultTable.ColumnDataType.TEXT;		
			columnDataTypes[4] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[5] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[6] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[7] = RIFResultTable.ColumnDataType.TEXT;		

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
			queryFormatter.addQueryLine(2,
			                            rifDatabaseProperties.getDatabaseType()
					                            .convertDateToString() + " AS creation_date,");
			queryFormatter.addQueryLine(2, "row_number() OVER(PARTITION BY study_id ORDER BY ith_update DESC) AS update_number,");
			queryFormatter.addQueryLine(2, "message,");
			queryFormatter.addQueryLine(2, "trace");	
			queryFormatter.addQueryLine(1, "FROM ");
			queryFormatter.addQueryLine(2, statusTableName);
			queryFormatter.addQueryLine(2, "),");	
			queryFormatter.addQueryLine(0, "most_recent_updates AS ");
			queryFormatter.addQueryLine(1, "(SELECT ");
			queryFormatter.addQueryLine(2, "study_id,");
			queryFormatter.addQueryLine(2, "study_state,");
			queryFormatter.addQueryLine(2, "creation_date,");
			queryFormatter.addQueryLine(2, "message,");		
			queryFormatter.addQueryLine(2, "trace");	
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
			queryFormatter.addQueryLine(1, "trace,");
			queryFormatter.addQueryLine(1, "CASE WHEN study_type = 1 THEN 'Disease Mapping' ELSE 'Risk Analysis' END AS study_type");
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
				String[][] data = new String[expectedNumberOfStatusUpdates][8];
				while (resultSet.next() ) {		
					data[ithRecord][0] = resultSet.getString(1); //study ID
					data[ithRecord][1] = resultSet.getString(2); //study name
					data[ithRecord][2] = resultSet.getString(3); //description
					data[ithRecord][3] = resultSet.getString(4); //study state

//					java.util.Date time 
//						= new java.util.Date(resultSet.getDate(5).getTime()); 
//					data[ithRecord][4] 
//						= RIFGenericLibraryMessages.getTimePhrase(time);
					data[ithRecord][4] = resultSet.getString(5); //creation_date formatted as DD MON YYYY HH24:MI:SS
				
					data[ithRecord][5] = resultSet.getString(6); //message
					data[ithRecord][6]=StringEscapeUtils.escapeJavaScript(resultSet.getString(7)); //trace 
					if (data[ithRecord][6] == null || data[ithRecord][6].equals("null")) {
						data[ithRecord][6]="";
					}
					else {
						String str=data[ithRecord][6];
						int len=str.length();
						int unEscapes=0;
						StringBuilder sb = new StringBuilder(len);
						for (int i = 0; i < len; i++) {
							char c0 = str.charAt(i);
							char c1;
							if ((i+1) >= len) {
								c1=0;
							}
							else {
								c1=str.charAt(i+1);
							}
							if (c0 == '\\' && c1 == '\'') { // "'" Does need to be escaped as in double quotes
								unEscapes++;
							}
							else {
								 sb.append(c0);
							}
						} 
						data[ithRecord][6] = sb.toString();
//						System.out.println("OK: " + unEscapes);
						// fix for Angular parse errors
						// Error: JSON.parse: bad escaped character at line 1 column 2871 of the JSON data
					}
					data[ithRecord][7]=StringEscapeUtils.escapeJavaScript(resultSet.getString(8)); //study_type 
					ithRecord++;
				}
				
				rifResultTable.setData(data);
				
				return rifResultTable;			
			}
			catch(SQLException sqlException) {
				logSQLException(sqlException);
				SQLQueryUtility.rollback(connection);
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
				SQLQueryUtility.close(statement);
				SQLQueryUtility.close(resultSet);
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
			queryFormatter.addQueryLine(2, "),");
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
				SQLQueryUtility.close(statement);
				SQLQueryUtility.close(resultSet);
			}
					
		}

	private String deriveStatusTableName(
		final String userID) {

		return "rif40.rif40_study_status";
	}
		
	@Override
	public void checkNonExistentStudyID(
		final Connection connection,
		final User user,
		final String studyID)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			RecordExistsQueryFormatter queryFormatter =
					RecordExistsQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setLookupKeyFieldName("study_id");
			queryFormatter.setFromTable("rif40.rif40_studies");		
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

			if (!resultSet.next()) {
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

			rifLogger.error(
				getClass(),
				errorMessage, 
				sqlException);

			throw new RIFServiceException(
				RIFServiceError.DATABASE_QUERY_FAILED,
				errorMessage);
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}		
	}
}
