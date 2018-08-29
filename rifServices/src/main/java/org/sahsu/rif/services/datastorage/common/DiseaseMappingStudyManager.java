package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.RecordExistsQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractGeographicalArea;
import org.sahsu.rif.services.concepts.AbstractStudy;
import org.sahsu.rif.services.concepts.AbstractStudyArea;
import org.sahsu.rif.services.concepts.ComparisonArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.Project;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public final class DiseaseMappingStudyManager extends BaseSQLManager {

	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private RIFContextManager rifContextManager;
	private InvestigationManager investigationManager;

	/**
	 * Instantiates a new SQL disease mapping study manager.
	 */
	DiseaseMappingStudyManager(
			final RIFServiceStartupOptions options,
			final RIFContextManager rifContextManager,
			final InvestigationManager investigationManager) {

		super(options);
		this.rifContextManager = rifContextManager;
		this.investigationManager = investigationManager;
	}

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
			
			SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			configureQueryFormatterForDB(queryFormatter);	
			queryFormatter.setDatabaseSchemaName("rif40");
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
					= GENERIC_MESSAGES.getDatePhrase(startDate);
				project.setStartDate(startDatePhrase);
				Date endDate
					= resultSet.getDate(4);
				if (endDate != null) {
					String endDatePhrase
						= GENERIC_MESSAGES.getDatePhrase(endDate);
					project.setEndDate(endDatePhrase);					
				}
				results.add(project);
			}
			connection.commit();
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"diseaseMappingStudyManager.error.unableToGetProjects",
					user.getUserID());
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
					getClass(),
					errorMessage,
					sqlException);

			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		return results;
	}
	
	public void clearStudiesForUser(
		final Connection connection,
		final User user) 
		throws RIFServiceException {
		
		//KLG: To Do
		
	}
	
	void checkNonExistentItems(
			final User user,
			final Connection connection,
			final AbstractStudy study)
		throws RIFServiceException {
		
		//check non-existent items in the Comparison and Study areas
		Geography geography = study.getGeography();
		rifContextManager.checkGeographyExists(
			connection, 
			geography.getName());
		AbstractStudyArea diseaseMappingStudyArea
			= study.getStudyArea();

		checkAreaNonExistentItems(
			connection,
			geography.getName(),
			diseaseMappingStudyArea);
		ComparisonArea comparisonArea
			= study.getComparisonArea();
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

		//Check non-existent items in the investigations
		ArrayList<Investigation> investigations
			= study.getInvestigations();
		for (Investigation investigation : investigations) {
			investigationManager.checkNonExistentItems(
				user,
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
	}
		
	public void checkDiseaseMappingStudyExists(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			RecordExistsQueryFormatter queryFormatter = RecordExistsQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setFromTable("rif40.rif40_studies");
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
			if (!resultSet.next()) {
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

			throw new RIFServiceException(
				RIFServiceError.DATABASE_QUERY_FAILED,
				errorMessage);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}
}
