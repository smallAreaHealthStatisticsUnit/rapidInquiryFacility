package rifServices.dataStorageLayer;

import java.util.ArrayList;
import java.sql.*;

import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.SQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.SQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.*;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;


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

final class SQLDiseaseMappingStudyManager 
	extends AbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private SQLRIFContextManager rifContextManager;
	private SQLInvestigationManager investigationManager;
	private SQLMapDataManager mapDataManager;
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new SQL disease mapping study manager.
	 */
	public SQLDiseaseMappingStudyManager(
		final RIFDatabaseProperties rifDatabaseProperties,
		final SQLRIFContextManager rifContextManager,
		final SQLInvestigationManager investigationManager,
		final SQLMapDataManager mapDataManager) {

		super(rifDatabaseProperties);
		this.rifContextManager = rifContextManager;
		this.investigationManager = investigationManager;
		this.mapDataManager = mapDataManager;
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
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ArrayList<Project> results = new ArrayList<Project>();
		try {
			
			SQLSelectQueryFormatter queryFormatter = new SQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.addSelectField("project");
			queryFormatter.addSelectField("description");
			queryFormatter.addSelectField("date_started");		
			queryFormatter.addSelectField("date_ended");		
			queryFormatter.addFromTable("rif40_projects");
				
			logSQLQuery(
				"getProjects",
				queryFormatter);
									
			statement 
				= createPreparedStatement(
					connection, 
					queryFormatter);
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
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"diseaseMappingStudyManager.error.unableToGetProjects",
					user.getUserID());
			
			RIFLogger rifLogger = RIFLogger.getLogger();
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
		
		ValidationPolicy validationPolicy = getValidationPolicy();
		
		project.checkErrors(validationPolicy);
		
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
	
	public void checkNonExistentItems(
		final Connection connection,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws RIFServiceException {
		
		//check non-existent items in the Comparison and Study areas
		Geography geography = diseaseMappingStudy.getGeography();
		rifContextManager.checkGeographyExists(
			connection, 
			geography.getName());
		DiseaseMappingStudyArea diseaseMappingStudyArea
			= diseaseMappingStudy.getDiseaseMappingStudyArea();

		
		
		checkAreaNonExistentItems(
			connection,
			geography.getName(),
			diseaseMappingStudyArea);
		ComparisonArea comparisonArea
			= diseaseMappingStudy.getComparisonArea();
		checkAreaNonExistentItems(
			connection,
			geography.getName(),
			comparisonArea);

		//we need to know the geoLevelToMap resolution to check
		//whether we have data for investigation covariates for
		//a given geography for that resolution.  What is important here
		//is the geoLevelToMap of the study area; with respects to covariate
		//analysis the geoLevelToMap of the comparison area has no meaning.
		GeoLevelToMap geoLevelToMap
			= diseaseMappingStudyArea.getGeoLevelToMap();		
		System.out.println("The geolevelselect for the study area is=="+geoLevelToMap.getName()+"==");

		//Check non-existent items in the investigations
		ArrayList<Investigation> investigations
			= diseaseMappingStudy.getInvestigations();
		for (Investigation investigation : investigations) {
			investigationManager.checkNonExistentItems(
				connection, 
				geography,
				geoLevelToMap,
				investigation);
		}
		
	}
	
	private void checkAreaNonExistentItems(
		final Connection connection,
		final String geographyName,
		final AbstractGeographicalArea area) 
		throws RIFServiceException {
	
		GeoLevelSelect geoLevelSelect
			= area.getGeoLevelSelect();
		rifContextManager.checkGeoLevelSelectExists(
			connection, 
			geographyName, 
			geoLevelSelect.getName());

		ValidationPolicy validationPolicy
			= getValidationPolicy();
		
		/*
		if (getValidationPolicy() == ValidationPolicy.STRICT) {
			GeoLevelArea geoLevelArea
				= area.getGeoLevelArea();
			rifContextManager.checkGeoLevelAreaExists(
				connection, 
				geographyName, 
				geoLevelSelect.getName(), 
				geoLevelArea.getName());
		}
		*/
		
		GeoLevelView geoLevelView
			= area.getGeoLevelView();
		rifContextManager.checkGeoLevelToMapOrViewValueExists(
			connection, 
			geographyName, 
			geoLevelSelect.getName(), 
			geoLevelView.getName(),
			false);
		
		GeoLevelToMap geoLevelToMap
			= area.getGeoLevelToMap();
		rifContextManager.checkGeoLevelToMapOrViewValueExists(
			connection, 
			geographyName, 
			geoLevelSelect.getName(), 
			geoLevelToMap.getName(),
			true);
	
		ArrayList<MapArea> mapAreas = area.getMapAreas();
		mapDataManager.checkAreasExist(
			connection, 
			geographyName, 
			geoLevelToMap.getName(), 
			mapAreas);		
	}
		
	public void checkDiseaseMappingStudyExists(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			SQLRecordExistsQueryFormatter queryFormatter
				= new SQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setFromTable("rif40_studies");
			queryFormatter.setLookupKeyFieldName("study_id");

			logSQLQuery(
				"checkDiseaseMappingStudyExists",
				queryFormatter,
				studyID);
		
			statement 
				= createPreparedStatement(
					connection, 
					queryFormatter);
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
