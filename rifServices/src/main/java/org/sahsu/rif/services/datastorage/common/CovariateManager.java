package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.RecordExistsQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AbstractRIFConcept;
import org.sahsu.rif.services.concepts.AdjustableCovariate;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public final class CovariateManager extends BaseSQLManager {

	private static final Messages SERVICE_MESSAGES = Messages.serviceMessages();

	private RIFContextManager sqlRIFContextManager;
	private String covariatesTableName;

	/**
	 * Instantiates a new SQL covariate manager.
	 *
	 * @param sqlRIFContextManager the sql rif context manager
	 */
	CovariateManager(final RIFServiceStartupOptions options,
			final RIFContextManager sqlRIFContextManager) {
		
		super(options);
		this.sqlRIFContextManager = sqlRIFContextManager;
		covariatesTableName = (options.getRifDatabaseType() == DatabaseType.SQL_SERVER
				                       ? SCHEMA_PREFIX : "") + "rif40_covariates";
	}

	/**
	 * Gets the covariates.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelToMap the geo level to map
	 * @return the covariates
	 * @throws RIFServiceException the RIF service exception
	 */
	public List<AbstractCovariate> getCovariates(final Connection connection,
			final Geography geography, final GeoLevelToMap geoLevelToMap)
			throws RIFServiceException {
		
		//Validate parameters
		validateCommonMethodParameters(connection, geography, null, geoLevelToMap);
		
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		List<AbstractCovariate> results = new ArrayList<>();
		try {
			//Create SQL query		
			SelectQueryFormatter queryFormatter = SelectQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.addSelectField("covariate_name");
			queryFormatter.addSelectField("min");
			queryFormatter.addSelectField("max");
			queryFormatter.addSelectField("type");
			queryFormatter.addFromTable(covariatesTableName);
			queryFormatter.addWhereParameter("geography");
			queryFormatter.addWhereParameter("geolevel_name");
			queryFormatter.addOrderByCondition("covariate_name");
		
			logSQLQuery(
				"getCovariates",
				queryFormatter,
				geography.getName(),
				geoLevelToMap.getName());
		
			// Parameterise and execute query
				
			statement = createPreparedStatement(connection, queryFormatter);
			statement.setString(1, geography.getName());
			statement.setString(2, geoLevelToMap.getName());

			dbResultSet = statement.executeQuery();
			connection.commit();
			while (dbResultSet.next()) {				
				AdjustableCovariate adjustableCovariate = AdjustableCovariate.newInstance();
				adjustableCovariate.setName(dbResultSet.getString(1));
				double minimumValue = dbResultSet.getDouble(2);
				double maximumValue = dbResultSet.getDouble(3);
				adjustableCovariate.setMinimumValue(String.valueOf(minimumValue));
				adjustableCovariate.setMaximumValue(String.valueOf(maximumValue));
				adjustableCovariate.setType(AbstractCovariate.Type.fromNumber(
						dbResultSet.getDouble(4)));
				adjustableCovariate.setDescription(
						sqlRIFContextManager.getColumnComment(connection,
						                                      queryFormatter.getDatabaseSchemaName(),
						                                      covariatesTableName, "covariate_name"));
				results.add(adjustableCovariate);
			}
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String errorMessage = SERVICE_MESSAGES.getMessage(
					"covariateManager.db.unableToGetCovariates");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(getClass(), errorMessage, sqlException);
			
			throw new RIFServiceException(RIFServiceError.GET_COVARIATES, errorMessage);
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}		
		return results;
	}
	
	/**
	 * Validate common method parameters.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelToMap the geo level to map
	 * @throws RIFServiceException the RIF service exception
	 */
	private void validateCommonMethodParameters(final Connection connection,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect,
			final GeoLevelToMap geoLevelToMap) throws RIFServiceException {

		AbstractRIFConcept.ValidationPolicy validationPolicy = getValidationPolicy();
		
		if (geography != null) {
			geography.checkErrors(validationPolicy);
			sqlRIFContextManager.checkGeographyExists(connection, geography.getName());
		}
		
		if (geoLevelSelect != null) {
			geoLevelSelect.checkErrors(validationPolicy);
			sqlRIFContextManager.checkGeoLevelSelectExists(connection, geography.getName(),
			                                               geoLevelSelect.getName());
		}
		
		if (geoLevelToMap != null) {
			
			geoLevelToMap.checkErrors(validationPolicy);			
			if (geoLevelSelect == null) {				
				sqlRIFContextManager.checkGeoLevelToMapOrViewValueExists(connection,
				                                                         geography.getName(),
				                                                         geoLevelToMap.getName(),
				                                                         true);
			} else {
				sqlRIFContextManager.checkGeoLevelToMapOrViewValueExists(connection,
				                                                         geography.getName(),
				                                                         geoLevelSelect.getName(),
				                                                         geoLevelToMap.getName(),
				                                                         true);
			}
			
		}	
	}
	
	void checkNonExistentCovariates(final Connection connection, final Geography geography,
			final GeoLevelToMap geoLevelToMap, final ArrayList<AbstractCovariate> covariates)
			throws RIFServiceException {
		
		if (covariates.size() == 0) {
			return;
		}
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		AbstractCovariate currentCovariate = null;
		try {
		
			RecordExistsQueryFormatter queryFormatter = RecordExistsQueryFormatter.getInstance(
					rifDatabaseProperties.getDatabaseType());
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.setFromTable(covariatesTableName);
			queryFormatter.setLookupKeyFieldName("covariate_name");
			queryFormatter.addWhereParameter("geography");
			queryFormatter.addWhereParameter("geolevel_name");
		
			logSQLQuery(
				"checkNonExistentCovariates - example query",
				queryFormatter,
				covariates.get(0).getName().toUpperCase(),
				geography.getName(),
				geoLevelToMap.getName());
				
			statement = createPreparedStatement(connection, queryFormatter);
			
			for (AbstractCovariate covariate : covariates) {

				currentCovariate = covariate; // Just for logging.
				statement.setString(1, covariate.getName().toUpperCase());
				statement.setString(2, geography.getName());
				statement.setString(3, geoLevelToMap.getName());	
				
				resultSet = statement.executeQuery();
				if (!resultSet.next()) {

					String errorMessage = SERVICE_MESSAGES.getMessage(
							"covariateManager.error.noCovariateFound",
							covariate.getName(),
							geography.getName(),
							geoLevelToMap.getName());

					RIFServiceException rifServiceException = new RIFServiceException(
							RIFServiceError.NON_EXISTENT_COVARIATE,
							errorMessage);
			
					SQLQueryUtility.close(statement);
					SQLQueryUtility.close(resultSet);

					connection.commit();
					
					throw rifServiceException;
				}
				
			}
			
			connection.commit();
		
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);
			SQLQueryUtility.rollback(connection);
			String errorMessage = SERVICE_MESSAGES.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					currentCovariate.getRecordType(),
					currentCovariate.getDisplayName());

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(getClass(), errorMessage, sqlException);
			
			throw new RIFServiceException(RIFServiceError.DATABASE_QUERY_FAILED, errorMessage);
		}
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
	}

}
