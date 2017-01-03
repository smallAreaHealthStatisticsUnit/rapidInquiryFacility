package rifServices.dataStorageLayer;

import rifServices.system.*;
import rifGenericLibrary.dataStorageLayer.*;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
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

final class SQLGenerateResultsSubmissionStep 
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
	 * Instantiates a new SQLRIF submission manager.
	 */
	public SQLGenerateResultsSubmissionStep(
		final RIFDatabaseProperties rifDatabaseProperties) {

		super(rifDatabaseProperties);		
		setEnableLogging(false);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	/**
	 * submit rif study submission.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param rifStudySubmission the rif job submission
	 * @throws RIFServiceException the RIF service exception
	 */	

	public String performStep(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
				
		String result = null;
		PreparedStatement runStudyStatement = null;
		PreparedStatement computeResultsStatement = null;
		ResultSet runStudyResultSet = null;
		ResultSet computeResultSet = null;
		
		try {
			
			enableDatabaseDebugMessages(connection);		
			
			PGSQLFunctionCallerQueryFormatter runStudyQueryFormatter = new PGSQLFunctionCallerQueryFormatter();
			runStudyQueryFormatter.setDatabaseSchemaName("rif40_sm_pkg");
			runStudyQueryFormatter.setFunctionName("rif40_run_study");
			runStudyQueryFormatter.setNumberOfFunctionParameters(2);

			logSQLQuery(
				"runStudy", 
				runStudyQueryFormatter,
				studyID);
			
			runStudyStatement
				= createPreparedStatement(
					connection,
					runStudyQueryFormatter);
			runStudyStatement.setInt(1, Integer.valueOf(studyID));
			runStudyStatement.setBoolean(2, true);
			runStudyResultSet
				= runStudyStatement.executeQuery();
			runStudyResultSet.next();
			
			result = String.valueOf(runStudyResultSet.getBoolean(1));	
			
			SQLWarning warning = runStudyStatement.getWarnings();
			while (warning != null) {
				/*
		        System.out.println("Message:" + warning.getMessage());
		        System.out.println("SQLState:" + warning.getSQLState());
		        System.out.print("Vendor error code: ");
		        System.out.println(warning.getErrorCode());
		        System.out.println("==");
		        */
		        warning = warning.getNextWarning();

			}
			
			connection.commit();
			
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFSubmissionManager.error.unableToRunStudy",
					studyID);

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				SQLGenerateResultsSubmissionStep.class, 
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
			PGSQLQueryUtility.close(runStudyStatement);
			PGSQLQueryUtility.close(runStudyResultSet);
			PGSQLQueryUtility.close(computeResultsStatement);
			PGSQLQueryUtility.close(computeResultSet);
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
