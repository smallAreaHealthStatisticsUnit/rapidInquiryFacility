package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.StudyState;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLUpdateQueryFormatter;


import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



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
		final RIFDatabaseProperties rifDatabaseProperties,
		final SQLRIFContextManager sqlRIFContextManager) {
		
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
				
		StudyState studyState = null;
		//Validate parameters		
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
				
		try {
			//Create SQL query		
			SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
			queryFormatter.setDatabaseSchemaName("rif40");
			//queryFormatter.set
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.addSelectField("study_state");
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addWhereParameter("study_id");
		
			logSQLQuery(
				"getStudyState",
				queryFormatter,
				studyID);
		
			//Parameterise and execute query
				
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.setString(1, studyID);

			dbResultSet = statement.executeQuery();
			connection.commit();
			if (dbResultSet.next() == false) {
				studyState = StudyState.STUDY_NOT_CREATED;
			}
			else {
				String studyStateNameValue
					= dbResultSet.getString(1);
				studyState = StudyState.getStudyStateFromName(studyStateNameValue);
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
		
		assert studyState != null;
		return studyState;
	}
	
	public void updateStudyState(
		final Connection connection, 
		final String studyID, 
		final StudyState studyState) {
		
		if (studyState == StudyState.STUDY_NOT_CREATED) {
			return;
		}
		
		SQLUpdateQueryFormatter queryFormatter = new SQLUpdateQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.setUpdateTable("rif_studies");
		queryFormatter.addUpdateField("study_state");
		queryFormatter.addWhereParameter("study_id");
		
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(queryFormatter.generateQuery());
			statement.setString(1, studyState.getName());
			statement.setString(2, studyID);
			statement.executeUpdate();
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);

			String errorMessage
				= RIFServiceMessages.getMessage(
					"", 
					studyID, 
					studyState.getName());
			RIFServiceException rifServiceException
				= new RIFServiceException(, errorMessage);
			throw rifServiceException;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
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
