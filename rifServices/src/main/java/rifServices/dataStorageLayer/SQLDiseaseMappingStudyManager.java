package rifServices.dataStorageLayer;

import java.util.ArrayList;
import java.sql.*;

import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.AbstractStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.Project;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.RIFLogger;


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

class SQLDiseaseMappingStudyManager {

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
	 * Instantiates a new SQL disease mapping study manager.
	 */
	public SQLDiseaseMappingStudyManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the projects.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @return the projects
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Project> getProjects(
		final Connection connection,
		final User user) 
		throws RIFServiceException {
		
		SQLSelectQueryFormatter query = new SQLSelectQueryFormatter();
		query.addSelectField("project");
		query.addSelectField("description");
		query.addSelectField("date_started");		
		query.addSelectField("date_ended");		
		query.addFromTable("rif40_projects");
		
		ArrayList<Project> results = new ArrayList<Project>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.prepareStatement(query.generateQuery());
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				Project project = Project.newInstance();
				project.setName(resultSet.getString(1));
				project.setDescription(resultSet.getString(2));
				Date startDate
					= resultSet.getDate(3);
				String startDatePhrase
					= RIFServiceMessages.getDatePhrase(startDate);
				project.setStartDate(startDatePhrase);
				Date endDate
					= resultSet.getDate(4);
				if (endDate != null) {
					String endDatePhrase
						= RIFServiceMessages.getDatePhrase(endDate);
					project.setEndDate(endDatePhrase);					
				}
				results.add(project);
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"diseaseMappingStudyManager.error.unableToGetProjects",
					user.getUserID());
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
					SQLDiseaseMappingStudyManager.class, 
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
		
		return results;
	}
	
	/**
	 * Gets the studies.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param project the project
	 * @return the studies
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<AbstractStudy> getStudies(
		final Connection connection,
		final User user,
		final Project project) 
		throws RIFServiceException {
		
		project.checkErrors();
		
		ArrayList<AbstractStudy> results
			= new ArrayList<AbstractStudy>();
	
		//TODO: faking the output here
		SampleTestObjectGenerator sampleTestObjectGenerator
			= new SampleTestObjectGenerator();
		DiseaseMappingStudy diseaseMappingStudy1
			= sampleTestObjectGenerator.createSampleDiseaseMappingStudy();
		diseaseMappingStudy1.setName(user.getUserID()+"==study 1");
		DiseaseMappingStudy diseaseMappingStudy2
			= sampleTestObjectGenerator.createSampleDiseaseMappingStudy();
		diseaseMappingStudy2.setName(user.getUserID()+"==study 2");
		results.add(diseaseMappingStudy1);
		results.add(diseaseMappingStudy2);
		
		return results;
	}
	
	public void clearStudiesForUser(
		final Connection connection,
		final User user) 
		throws RIFServiceException {
		
		//KLG: To Do
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void checkDiseaseMappingStudyExists(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
		
		SQLRecordExistsQueryFormatter diseaseMappingStudyExistsQuery
			= new SQLRecordExistsQueryFormatter();
		diseaseMappingStudyExistsQuery.setFromTable("rif40_studies");
		diseaseMappingStudyExistsQuery.setLookupKeyFieldName("study_id");
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement 
				= connection.prepareStatement(diseaseMappingStudyExistsQuery.generateQuery());
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			
			if (resultSet.next() == false) {
				String recordType
					= RIFServiceMessages.getMessage("diseaseMappingStudy.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						studyID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_DISEASE_MAPPING_STUDY, 
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			String recordType
				= RIFServiceMessages.getMessage("diseaseMappingStudy.label");			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
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
		
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
