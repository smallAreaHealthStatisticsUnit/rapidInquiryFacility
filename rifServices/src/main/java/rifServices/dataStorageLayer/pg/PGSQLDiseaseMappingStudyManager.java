package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.system.Messages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractGeographicalArea;
import rifServices.businessConceptLayer.ComparisonArea;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.DiseaseMappingStudyArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.Investigation;
import rifServices.businessConceptLayer.Project;
import rifServices.dataStorageLayer.common.DiseaseMappingStudyManager;
import rifServices.dataStorageLayer.common.InvestigationManager;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

final class PGSQLDiseaseMappingStudyManager extends PGSQLAbstractSQLManager
		implements DiseaseMappingStudyManager {
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private RIFContextManager rifContextManager;
	private InvestigationManager investigationManager;

	/**
	 * Instantiates a new SQL disease mapping study manager.
	 */
	public PGSQLDiseaseMappingStudyManager(
		final RIFServiceStartupOptions startupOptions,
		final RIFContextManager rifContextManager,
		final InvestigationManager investigationManager) {

		super(startupOptions);
		this.rifContextManager = rifContextManager;
		this.investigationManager = investigationManager;

	}
	@Override
	public ArrayList<Project> getProjects(
			final Connection connection,
			final User user)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ArrayList<Project> results = new ArrayList<Project>();
		try {
			
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
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
					PGSQLDiseaseMappingStudyManager.class, 
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
	
	
	@Override
	public void clearStudiesForUser(
			final Connection connection,
			final User user)
		throws RIFServiceException {
		
		//KLG: To Do
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	@Override
	public void checkNonExistentItems(
			final User user,
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
		/*
		ValidationPolicy validationPolicy
			= getValidationPolicy();
		

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
	}
		
	@Override
	public void checkDiseaseMappingStudyExists(
			final Connection connection,
			final String studyID)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
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
