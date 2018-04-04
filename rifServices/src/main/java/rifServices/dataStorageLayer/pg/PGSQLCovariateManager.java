package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.businessConceptLayer.AdjustableCovariate;
import rifServices.businessConceptLayer.CovariateType;
import rifServices.businessConceptLayer.DiseaseMappingStudy;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.Investigation;
import rifServices.dataStorageLayer.common.CovariateManager;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

final class PGSQLCovariateManager 
	extends PGSQLAbstractSQLManager implements CovariateManager {
	
	/** The sql rif context manager. */
	private RIFContextManager sqlRIFContextManager;

	/**
	 * Instantiates a new SQL covariate manager.
	 *
	 * @param sqlRIFContextManager the sql rif context manager
	 */
	public PGSQLCovariateManager(final RIFServiceStartupOptions options,
			final RIFContextManager sqlRIFContextManager) {

		super(options);
		this.sqlRIFContextManager = sqlRIFContextManager;
	}
	
	@Override
	public ArrayList<AbstractCovariate> getCovariatesForInvestigation(
			final Connection connection,
			final User user,
			final DiseaseMappingStudy diseaseMappingStudy,
			final Investigation investigation)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLSelectQueryFormatter queryFormatter 
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.addSelectField("covariate_name");
			queryFormatter.addSelectField("min");
			queryFormatter.addSelectField("max");
			queryFormatter.addFromTable("t_rif40_inv_covariates");
			queryFormatter.addWhereParameter("inv_id");
			queryFormatter.addWhereParameter("study_id");
				
			logSQLQuery(
				"getCovariatesForInvestigation",
				queryFormatter,
				investigation.getIdentifier(),
				diseaseMappingStudy.getIdentifier());
										
			ArrayList<AbstractCovariate> results 
				= new ArrayList<AbstractCovariate>();
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			Integer investigationID
				= Integer.valueOf(investigation.getIdentifier());
			statement.setInt(1, investigationID);
			Integer studyID
				= Integer.valueOf(diseaseMappingStudy.getIdentifier());
			statement.setInt(2, studyID);
			resultSet
				= statement.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString(1);
				Double minimumValue = resultSet.getDouble(2);
				Double maximumValue = resultSet.getDouble(3);
				
				//TODO: KLG find out where we can find out what type the variable is
				CovariateType covariateType = CovariateType.CONTINUOUS_VARIABLE;
				AdjustableCovariate adjustableCovariate
					= AdjustableCovariate.newInstance(
						name, 
						String.valueOf(minimumValue), 
						String.valueOf(maximumValue), 
						covariateType);	
				results.add(adjustableCovariate);
			}
			connection.commit();
			return results;
		}
		catch(SQLException exception) {
			logSQLException(exception);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"covariateManager.db.unableToGetCovariatesForInvestigation",
					diseaseMappingStudy.getDisplayName(),
					investigation.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_COVARIATES_FOR_INVESTIGATION, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}	
	}
		
	@Override
	public ArrayList<AbstractCovariate> getCovariates(
			final Connection connection,
			final Geography geography,
			final GeoLevelToMap geoLevelToMap)
		throws RIFServiceException {
				
		
		//Validate parameters
		validateCommonMethodParameters(
			connection,
			geography,
			null,
			geoLevelToMap);

		
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		ArrayList<AbstractCovariate> results = new ArrayList<AbstractCovariate>();		
		try {
			//Create SQL query		
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.addSelectField("covariate_name");
			queryFormatter.addSelectField("min");
			queryFormatter.addSelectField("max");
			queryFormatter.addSelectField("type");
			queryFormatter.addFromTable("rif40_covariates");
			queryFormatter.addWhereParameter("geography");
			queryFormatter.addWhereParameter("geolevel_name");
			queryFormatter.addOrderByCondition("covariate_name");
		
			logSQLQuery(
				"getCovariates",
				queryFormatter,
				geography.getName(),
				geoLevelToMap.getName());
		
			//Parameterise and execute query
				
			statement
				= createPreparedStatement(connection, queryFormatter);
			statement.setString(1, geography.getName());
			statement.setString(2, geoLevelToMap.getName());

			dbResultSet = statement.executeQuery();
			connection.commit();
			while (dbResultSet.next()) {				
				AdjustableCovariate adjustableCovariate
					= AdjustableCovariate.newInstance();
				adjustableCovariate.setName(dbResultSet.getString(1));
				double minimumValue = dbResultSet.getDouble(2);
				double maximumValue = dbResultSet.getDouble(3);
				adjustableCovariate.setMinimumValue(String.valueOf(minimumValue));
				adjustableCovariate.setMaximumValue(String.valueOf(maximumValue));				
				if ((minimumValue == 0) && (maximumValue == 1)) {
					adjustableCovariate.setCovariateType(CovariateType.BINARY_INTEGER_SCORE);								
				}
				else if (((minimumValue == 1) && (maximumValue == 3)) || ((minimumValue == 1) && (maximumValue == 5)) ) {
					//KLG: TODO - fix this, the logic for identifying ntile is not strong enough
					adjustableCovariate.setCovariateType(CovariateType.NTILE_INTEGER_SCORE);		
				}
				else {
					//it must be continuous, which is currently not supported
					adjustableCovariate.setCovariateType(CovariateType.CONTINUOUS_VARIABLE);
				}				
				
				results.add(adjustableCovariate);
			}
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage("covariateManager.db.unableToGetCovariates");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLCovariateManager.class, 
				errorMessage, 
				sqlException);
			
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError.GET_COVARIATES, errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(dbResultSet);
		}		
		return results;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	/**
	 * Validate common method parameters.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelToMap the geo level to map
	 * @throws RIFServiceException the RIF service exception
	 */
	private void validateCommonMethodParameters(
		final Connection connection,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelToMap geoLevelToMap) 
		throws RIFServiceException {

		ValidationPolicy validationPolicy = getValidationPolicy();
		
		if (geography != null) {
			geography.checkErrors(validationPolicy);
			sqlRIFContextManager.checkGeographyExists(
				connection, 
				geography.getName());			
		}
		
		if (geoLevelSelect != null) {
			geoLevelSelect.checkErrors(validationPolicy);
			sqlRIFContextManager.checkGeoLevelSelectExists(
				connection,
				geography.getName(), 
				geoLevelSelect.getName());			
		}
		
		if (geoLevelToMap != null) {
			
			geoLevelToMap.checkErrors(validationPolicy);			
			if (geoLevelSelect == null) {				
				sqlRIFContextManager.checkGeoLevelToMapOrViewValueExists(
					connection,
					geography.getName(),
					geoLevelToMap.getName(),
					true);
			}
			else {
				sqlRIFContextManager.checkGeoLevelToMapOrViewValueExists(
					connection,
					geography.getName(),
					geoLevelSelect.getName(),
					geoLevelToMap.getName(),
					true);				
			}
			
		}	
	}
	
	
	@Override
	@SuppressWarnings("resource")
	public void checkNonExistentCovariates(
			final Connection connection,
			final Geography geography,
			final GeoLevelToMap geoLevelToMap,
			final ArrayList<AbstractCovariate> covariates)
		throws RIFServiceException {
		
		
		if (covariates.size() == 0) {
			return;
		}
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		AbstractCovariate currentCovariate = null;
		try {
		
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.setFromTable("rif40_covariates");
			queryFormatter.setLookupKeyFieldName("covariate_name");
			queryFormatter.addWhereParameter("geography");
			queryFormatter.addWhereParameter("geolevel_name");
		
			logSQLQuery(
				"checkNonExistentCovariates - example query",
				queryFormatter,
				covariates.get(0).getName().toUpperCase(),
				geography.getName(),
				geoLevelToMap.getName());
				
			statement 
				= createPreparedStatement(
					connection, 
					queryFormatter);
			
			for (AbstractCovariate covariate : covariates) {
				
				statement.setString(1, covariate.getName().toUpperCase());
				statement.setString(2, geography.getName());
				statement.setString(3, geoLevelToMap.getName());	
				
				resultSet = statement.executeQuery();
				if (resultSet.next() == false) {
				
					String errorMessage
						= RIFServiceMessages.getMessage(
							"covariateManager.error.noCovariateFound",
							covariate.getName(),
							geography.getName(),
							geoLevelToMap.getName());

					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFServiceError.NON_EXISTENT_COVARIATE, 
							errorMessage);
			
					PGSQLQueryUtility.close(statement);
					PGSQLQueryUtility.close(resultSet);

					connection.commit();
					
					throw rifServiceException;
				}
				
			}
			
			connection.commit();
		
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					currentCovariate.getRecordType(),
					currentCovariate.getDisplayName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLCovariateManager.class, 
				errorMessage, 
				sqlException);										
			
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

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
