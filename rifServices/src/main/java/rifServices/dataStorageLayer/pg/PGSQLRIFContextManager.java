package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLAggregateValueQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.RIFLogger;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.dataStorageLayer.common.RIFContextManager;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;

final class PGSQLRIFContextManager extends PGSQLAbstractSQLManager implements RIFContextManager {

	public PGSQLRIFContextManager(final RIFServiceStartupOptions options) {

		super(options);
		if (rifDatabaseProperties == null) {
			rifDatabaseProperties = options.getRIFDatabaseProperties();
		}
	}

	@Override
	public ArrayList<Geography> getGeographies(
			final Connection connection)
		throws RIFServiceException {
		
		//Parameterise and execute query		
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		ArrayList<Geography> results = new ArrayList<Geography>();
		try {
		
			//Create SQL query		
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setUseDistinct(true);
			queryFormatter.addSelectField("geography");
			queryFormatter.addFromTable("rif40_geographies");
			queryFormatter.addOrderByCondition("geography");
		
			logSQLQuery(
				"getGeographies",
				queryFormatter);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			dbResultSet = statement.executeQuery();
			connection.commit();

			while (dbResultSet.next()) {
				Geography geography = Geography.newInstance();
				geography.setName(dbResultSet.getString(1));
				
				//KLG: note we should have a description field for geography
				geography.setDescription("");
				results.add(geography);
			}
			
			connection.commit();
			
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeographies");
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);					
			
																		
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError.GET_GEOGRAPHIES, errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(dbResultSet);
		}		
	}

	
	@Override
	public ArrayList<HealthTheme> getHealthThemes(
			final Connection connection,
			final Geography geography)
		throws RIFServiceException {
		
		//Validate parameters
		validateCommonMethodParameters(
			connection,
			geography,
			null,
			null);
		
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		ArrayList<HealthTheme> results = new ArrayList<HealthTheme>();
		try {
		
			//Create SQL query		
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setUseDistinct(true);
			queryFormatter.addSelectField("theme");
			queryFormatter.addSelectField("description");
			queryFormatter.addFromTable("rif40_health_study_themes");
			queryFormatter.addOrderByCondition("description");

			logSQLQuery(
				"getHealthThemes",
				queryFormatter);
				
			//Parameterise and execute query		
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			dbResultSet = statement.executeQuery();

			while (dbResultSet.next()) {
				HealthTheme healthTheme = HealthTheme.newInstance();
				healthTheme.setName(dbResultSet.getString(1));
				healthTheme.setDescription(dbResultSet.getString(2));
				results.add(healthTheme);
			}
			
			connection.commit();
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetHealthThemes");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);					
																							
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError.GET_HEALTH_THEMES, errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(dbResultSet);
		}		
	}
	
	@Override
	public NumeratorDenominatorPair getNDPairFromNumeratorTableName(
			final User user,
			final Connection connection,
			final Geography geography,
			final String numeratorTableName)
		throws RIFServiceException {
		
		validateCommonMethodParameters(
				connection,
				geography,
				null,
				null);

		checkNumeratorTableExists(user, connection, geography, numeratorTableName);

		ArrayList<NumeratorDenominatorPair> results 
			= new ArrayList<NumeratorDenominatorPair>();;
			PreparedStatement statement = null;
			ResultSet dbResultSet = null;
	
		try {
		
			//Create SQL query		
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setUseDistinct(true);
			queryFormatter.addSelectField("numerator_description");
			queryFormatter.addSelectField("denominator_table");
			queryFormatter.addSelectField("denominator_description");		
			queryFormatter.addFromTable("rif40_num_denom");
			queryFormatter.addWhereParameter("numerator_table");


			logSQLQuery(
				"getNDPairFromNumeratorTableName",
				queryFormatter,
				numeratorTableName);
				
			//Parameterise and execute query			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, numeratorTableName);			
			
			dbResultSet = statement.executeQuery();

			while (dbResultSet.next()) {
				String numeratorDescription = dbResultSet.getString(1);
				String denominatorTable = dbResultSet.getString(2);
				String denominatorDescription = dbResultSet.getString(3);
				
				NumeratorDenominatorPair result
					= NumeratorDenominatorPair.newInstance(
						numeratorTableName,
						numeratorDescription,
						denominatorTable,
						denominatorDescription);			
				results.add(result);				
			}
			
			if (results.isEmpty()) {
				//ERROR: There is no numerator denominator pair for this health theme
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlRIFContextManager.error.noNDPairForNumeratorTableName",
						numeratorTableName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NO_ND_PAIR_FOR_NUMERATOR_TABLE_NAME,
						errorMessage);
				
				connection.commit();
				throw rifServiceException;
			}

			connection.commit();

			return results.get(0);
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetNumeratorDenominatorPair");
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);					
																							
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_NUMERATOR_DENOMINATOR_PAIR,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(dbResultSet);			
		}
		
	}
	
	@Override
	public ArrayList<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
			final Connection connection,
			final Geography geography,
			final HealthTheme healthTheme,
			final User user)
		throws RIFServiceException {

		validateCommonMethodParameters(
			connection,
			geography,
			healthTheme,
			null);
				
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		ArrayList<NumeratorDenominatorPair> results 
			= new ArrayList<NumeratorDenominatorPair>();;
	
		try {
			//Create SQL query		
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setUseDistinct(true);
			queryFormatter.addSelectField("numerator_table");
			queryFormatter.addSelectField("numerator_description");
			queryFormatter.addSelectField("denominator_table");
			queryFormatter.addSelectField("denominator_description");		
			queryFormatter.addFromTable("rif40_num_denom");
			queryFormatter.addWhereParameter("theme_description");
			queryFormatter.addWhereParameter("geography");
		
			logSQLQuery(
			"getNumeratorDenominatorPairs",
			queryFormatter,
			healthTheme.getDescription());
		
			//Parameterise and execute query			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, healthTheme.getDescription());	
			statement.setString(2, geography.getDisplayName());			
			
			dbResultSet = statement.executeQuery();
			connection.commit();

			while (dbResultSet.next()) {
				String numeratorTable = dbResultSet.getString(1);
				String numeratorDescription = dbResultSet.getString(2);
				String denominatorTable = dbResultSet.getString(3);
				String denominatorDescription = dbResultSet.getString(4);
				
				NumeratorDenominatorPair result
					= NumeratorDenominatorPair.newInstance(
						numeratorTable,
						numeratorDescription,
						denominatorTable,
						denominatorDescription);			
				results.add(result);				
			}
			
			connection.commit();
			
			if (results.isEmpty()) {
				//ERROR: There is no numerator denominator pair for this health theme
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlRIFContextManager.error.noNDPairForHealthTheme",
						healthTheme.getName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NO_ND_PAIR_FOR_HEALTH_THEME,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetNumeratorDenominatorPair");
			
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);					
																							
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_NUMERATOR_DENOMINATOR_PAIR,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(dbResultSet);			
		}
		
		return results;
	}
	
	
	@Override
	public ArrayList<GeoLevelSelect> getGeoLevelSelectValues(
			final Connection connection,
			final Geography geography)
		throws RIFServiceException {
				
		//TOUR_VALIDATION
		/* Here, we assume that all the parameters are non-null
		 * and that they contain no security risks.  
		 * 
		 */
		//Validate parameters
		validateCommonMethodParameters(
			connection,
			geography,
			null,
			null);

		
		PreparedStatement getMaxGeoLevelIDStatement = null;
		ResultSet getMaxGeoLevelIDResultSet = null;
		PreparedStatement getGeoLevelSelectStatement = null;
		ResultSet getGeoLevelSelectResultSet = null;		
		ArrayList<GeoLevelSelect> results = new ArrayList<GeoLevelSelect>();		
		String errorMessage
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelSelect");
		RIFServiceException getGeoLevelSelectValuesException
			= new RIFServiceException(
				RIFServiceError.GET_GEOLEVEL_SELECT_VALUES, 
				errorMessage);
		Integer maximumGeoLevelID = null;
		try {
					
			//Obtain the maximum value for geolevel_id. We need to return
			//all geolevel choices which have a priority less than this
			PGSQLAggregateValueQueryFormatter maximumGeoLevelIDQueryFormatter
				= new PGSQLAggregateValueQueryFormatter(PGSQLAggregateValueQueryFormatter.OperationType.MAX);
			configureQueryFormatterForDB(maximumGeoLevelIDQueryFormatter);
			maximumGeoLevelIDQueryFormatter.setCountableFieldName("geolevel_id");
			maximumGeoLevelIDQueryFormatter.setFromTable("rif40_geolevels");
			maximumGeoLevelIDQueryFormatter.addWhereParameter("geography");
		
			logSQLQuery(
				"maximumGeoLevelIDQuery",
				maximumGeoLevelIDQueryFormatter,
				geography.getName());
		
			getMaxGeoLevelIDStatement
				= createPreparedStatement(
					connection,
					maximumGeoLevelIDQueryFormatter);			
			
			getMaxGeoLevelIDStatement.setString(1, geography.getName());
			getMaxGeoLevelIDResultSet
				= getMaxGeoLevelIDStatement.executeQuery();
			getMaxGeoLevelIDResultSet.next();
			maximumGeoLevelID = getMaxGeoLevelIDResultSet.getInt(1);
		
			if (maximumGeoLevelID == null) {
				
				connection.commit();				
				return results;
			}
		
			//Create SQL query		
			PGSQLSelectQueryFormatter getGeoLevelSelectValuesQueryFormatter 
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(getGeoLevelSelectValuesQueryFormatter);
			getGeoLevelSelectValuesQueryFormatter.addSelectField("geolevel_name");
			getGeoLevelSelectValuesQueryFormatter.addFromTable("rif40_geolevels");
			getGeoLevelSelectValuesQueryFormatter.addWhereParameter("geography");
		//	getGeoLevelSelectValuesQueryFormatter.addWhereParameter("listing");
			getGeoLevelSelectValuesQueryFormatter.addWhereParameterWithOperator("geolevel_id", "<=");
			getGeoLevelSelectValuesQueryFormatter.addOrderByCondition("geolevel_id");
		
			logSQLQuery(
				"getGeoLevelSelectValuesQuery",
				getGeoLevelSelectValuesQueryFormatter,
				geography.getName());
		
			//Parameterise and execute query		

			getGeoLevelSelectStatement
				= createPreparedStatement(
					connection,
					getGeoLevelSelectValuesQueryFormatter);
			getGeoLevelSelectStatement.setString(1, geography.getName());
			
			//TODO: (DM) listable is contradictory for SAHSULAND geolevels and default areas
			//only include those ids that are designated as 'listable'
			//getGeoLevelSelectStatement.setInt(2, 1);
			//getGeoLevelSelectStatement.setInt(3, maximumGeoLevelID);
			
			getGeoLevelSelectStatement.setInt(2, maximumGeoLevelID);
			
			getGeoLevelSelectResultSet = getGeoLevelSelectStatement.executeQuery();
			connection.commit();
			
			while (getGeoLevelSelectResultSet.next()) {
				GeoLevelSelect geoLevelSelect
					= GeoLevelSelect.newInstance(getGeoLevelSelectResultSet.getString(1));
				results.add(geoLevelSelect);				
			}
		}
		catch(SQLException sqlException) {		
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
			throw getGeoLevelSelectValuesException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(getMaxGeoLevelIDStatement);
			PGSQLQueryUtility.close(getMaxGeoLevelIDResultSet);			
			PGSQLQueryUtility.close(getGeoLevelSelectStatement);
			PGSQLQueryUtility.close(getGeoLevelSelectResultSet);			
		}		
		return results;		
	}
	
	@Override
	public GeoLevelSelect getDefaultGeoLevelSelectValue(
			final Connection connection,
			final Geography geography)
		throws RIFServiceException {
			
		//Validate parameters
		validateCommonMethodParameters(
			connection,
			geography,
			null,
			null);

		PreparedStatement statement = null;
		ResultSet dbResultSet = null;		
		GeoLevelSelect result = null;
		try {
		
			//Create SQL query		
			PGSQLSelectQueryFormatter queryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("defaultcomparea");
			queryFormatter.addFromTable("rif40_geographies");
			queryFormatter.addWhereParameter("geography");

			logSQLQuery(
				"getDefaultGeoLevelSelectValue",
				queryFormatter,
				geography.getName());

			//Parameterise and execute query
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setString(1, geography.getName());
			dbResultSet = statement.executeQuery();
			connection.commit();
				
			if (dbResultSet.next() == false) {
				//ERROR: no default value found
				String errorMessage
					= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetDefaultGeoLevelSelect");
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.GET_DEFAULT_GEOLEVEL_SELECT_VALUE, 
						errorMessage);
				throw rifServiceException;
			}
			result = GeoLevelSelect.newInstance(dbResultSet.getString(1));
			
			connection.commit();
			
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelSelect");

			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										

			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.GET_GEOLEVEL_SELECT_VALUES, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(dbResultSet);			
		}		
	}

	@Override
	public ArrayList<GeoLevelArea> getGeoLevelAreaValues(
			final Connection connection,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException {
		
		//Validate parameters
		validateCommonMethodParameters(
			connection,
			geography,
			null,
			geoLevelSelect);

		ArrayList<GeoLevelArea> results = new ArrayList<GeoLevelArea>(); 

		//First, obtain the name of the table that will contain the names of 
		//areas		
		PGSQLSelectQueryFormatter lookupTableQueryFormatter 
			= new PGSQLSelectQueryFormatter();
		configureQueryFormatterForDB(lookupTableQueryFormatter);
		lookupTableQueryFormatter.addSelectField("lookup_table");
		lookupTableQueryFormatter.addFromTable("rif40_geolevels");
		lookupTableQueryFormatter.addWhereParameter("geography");
		lookupTableQueryFormatter.addWhereParameter("geolevel_name");

		logSQLQuery(
			"lookupTableQuery",
			lookupTableQueryFormatter,
			geography.getName(),
			geoLevelSelect.getName());
	
		PreparedStatement lookupTableStatement = null;
		ResultSet lookupTableResultSet = null;			
		PreparedStatement geographicAreaStatement = null;
		ResultSet geographicAreaResultSet = null;		
		
		//Declaring an exception here because it may be thrown in multiple
		//places and putting it here reduces repetitive code 
		String errorMessage
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelArea");
		RIFServiceException rifServiceException
			= new RIFServiceException(RIFServiceError.GET_GEOLEVEL_AREA_VALUES,
				errorMessage);
		String lookupTableName = null;
		try {
			lookupTableStatement
				= createPreparedStatement(
					connection,
					lookupTableQueryFormatter);
			lookupTableStatement.setString(1, geography.getName());
			lookupTableStatement.setString(2, geoLevelSelect.getName());
			
			lookupTableResultSet = lookupTableStatement.executeQuery();	
			if (lookupTableResultSet.next() == false) {
				//ERROR: no areas available
				throw rifServiceException;
			}
			else {
				lookupTableName = lookupTableResultSet.getString(1);
			}
		
			if (lookupTableName == null) {
				
				connection.commit();
				
				throw rifServiceException;
			}

			//Given the lookup table name, retrieve the areas
			PGSQLSelectQueryFormatter geographicAreaQueryFormatter
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(geographicAreaQueryFormatter);
			geographicAreaQueryFormatter.addSelectField(geoLevelSelect.getName());		
			geographicAreaQueryFormatter.addSelectField("name");
			geographicAreaQueryFormatter.addFromTable(lookupTableName);
			geographicAreaQueryFormatter.addOrderByCondition("name");

			logSQLQuery(
				"geographicAreaQuery",
				geographicAreaQueryFormatter,
				geography.getName());
				
		
			geographicAreaStatement
				= createPreparedStatement(
					connection,
					geographicAreaQueryFormatter);
						
			geographicAreaResultSet = geographicAreaStatement.executeQuery();	
			connection.commit();
			while (geographicAreaResultSet.next()) {
				String identifier = geographicAreaResultSet.getString(1);
				GeoLevelArea geoLevelArea 
					= GeoLevelArea.newInstance();
				geoLevelArea.setIdentifier(identifier);
				String name = geographicAreaResultSet.getString(2);
				
				//TODO KLG: - this is a work around scheme - sahsuland should
				//not have any nulls in the name field
				if (name == null) {
					geoLevelArea.setName(identifier);
				}
				else {
					geoLevelArea.setName(name);					
				}
				results.add(geoLevelArea);
			}
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
							
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(lookupTableStatement);
			PGSQLQueryUtility.close(lookupTableResultSet);				
			PGSQLQueryUtility.close(geographicAreaStatement);
			PGSQLQueryUtility.close(geographicAreaResultSet);			
		}
				
		return results;		
	}	
	
	
	@Override
	public ArrayList<GeoLevelView> getGeoLevelViewValues(
			final Connection connection,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException {

		//Validate parameters
		validateCommonMethodParameters(
			connection,
			geography,
			null,
			geoLevelSelect);
		
		PreparedStatement geoLevelIDStatement = null;
		ResultSet geoLevelIDResultSet = null;			
		PreparedStatement geoLevelViewsStatement = null;
		ResultSet geoLevelViewsResultSet = null;
		Integer geoLevelID = null;
		String errorMessage
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelView");
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFServiceError.GET_GEOLEVEL_VIEW_VALUES,
				errorMessage);	
		ArrayList<GeoLevelView> results = new ArrayList<GeoLevelView>();
		try {
			
			//Create SQL query		
			PGSQLSelectQueryFormatter geoLevelIDQueryFormatter 
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(geoLevelIDQueryFormatter);
			geoLevelIDQueryFormatter.addSelectField("geolevel_id");
			geoLevelIDQueryFormatter.addFromTable("rif40_geolevels");
			geoLevelIDQueryFormatter.addWhereParameter("geography");
			geoLevelIDQueryFormatter.addWhereParameter("geolevel_name");

			logSQLQuery(
				"geoLevelIDQuery",
				geoLevelIDQueryFormatter,
				geography.getName());
				
		
			geoLevelIDStatement
				= createPreparedStatement(
					connection,
					geoLevelIDQueryFormatter);
			geoLevelIDStatement.setString(1, geography.getName());
			geoLevelIDStatement.setString(2, geoLevelSelect.getName());
			geoLevelIDResultSet = geoLevelIDStatement.executeQuery();	
			if (geoLevelIDResultSet.next() == false) {
				//ERROR: no views available
				throw rifServiceException;
			}
			else {
				geoLevelID = geoLevelIDResultSet.getInt(1);
			}

			if (geoLevelID == null) {
				
				connection.commit();

				throw rifServiceException;
			}
		
			PGSQLSelectQueryFormatter geoLevelViewsQueryFormatter 
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(geoLevelViewsQueryFormatter);
			geoLevelViewsQueryFormatter.addSelectField("geolevel_name");
			geoLevelViewsQueryFormatter.addFromTable("rif40_geolevels");
			geoLevelViewsQueryFormatter.addWhereParameter("geography");
			geoLevelViewsQueryFormatter.addWhereParameterWithOperator("geolevel_id",">=");
			geoLevelViewsQueryFormatter.addOrderByCondition("geolevel_name");

			logSQLQuery(
				"geoLevelViewsQuery",
				geoLevelIDQueryFormatter,
				geography.getName(),
				String.valueOf(geoLevelID.intValue()));
		
			geoLevelViewsStatement
				= createPreparedStatement(
					connection,
					geoLevelViewsQueryFormatter);
			geoLevelViewsStatement.setString(1, geography.getName());
			geoLevelViewsStatement.setInt(2, geoLevelID.intValue());
			geoLevelViewsResultSet = geoLevelViewsStatement.executeQuery();
			connection.commit();
			
			while (geoLevelViewsResultSet.next()) {
				GeoLevelView geoLevelView 
					= GeoLevelView.newInstance(geoLevelViewsResultSet.getString(1));
				results.add(geoLevelView);
			}
			
			connection.commit();
			
			return results;
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
					
			throw rifServiceException;			
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(geoLevelIDStatement);
			PGSQLQueryUtility.close(geoLevelIDResultSet);			
			PGSQLQueryUtility.close(geoLevelViewsStatement);
			PGSQLQueryUtility.close(geoLevelViewsResultSet);			
		}		
	}
					
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	
	/**
	 * Validate common method parameters.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param healthTheme the health theme
	 * @param geoLevelSelect the geo level select
	 * @throws RIFServiceException the RIF service exception
	 */
	private void validateCommonMethodParameters(
		final Connection connection,
		final Geography geography,
		final HealthTheme healthTheme,
		final GeoLevelSelect geoLevelSelect) 
		throws RIFServiceException {

		ValidationPolicy validationPolicy = getValidationPolicy();
		if (geography != null) {
			geography.checkErrors(validationPolicy);			
			checkGeographyExists(connection, geography.getName());
		}
		
		if (healthTheme != null) {
			healthTheme.checkErrors(validationPolicy);			
			checkHealthThemeExists(
				connection, 
				healthTheme.getDescription());
		}
		
		if (geoLevelSelect != null) {
			geoLevelSelect.checkErrors(validationPolicy);
			checkGeoLevelSelectExists(
				connection, 
				geography.getName(), 
				geoLevelSelect.getName());			
		}
	}
	
	@Override
	public void checkGeographyExists(
			final Connection connection,
			final String geographyName)
		throws RIFServiceException {

		
		PreparedStatement checkGeographyExistsStatement = null;
		ResultSet checkGeographyExistsResultSet = null;
		try {
		
			//Create SQL query
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setFromTable("rif40_geographies");
			queryFormatter.setLookupKeyFieldName("geography");

			logSQLQuery(
				"checkGeographyExists",
				queryFormatter,
				geographyName);
					
			//Parameterise and execute query
			checkGeographyExistsStatement
				= createPreparedStatement(
					connection,
					queryFormatter);
			checkGeographyExistsStatement.setString(1, geographyName);
			checkGeographyExistsResultSet 
				= checkGeographyExistsStatement.executeQuery();
			
			if (checkGeographyExistsResultSet.next() == false) {
				
				//ERROR: no such geography exists
				String recordType
					= RIFServiceMessages.getMessage("geography.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						geographyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEOGRAPHY, 
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
				= RIFServiceMessages.getMessage("geography.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					geographyName);
			
			RIFLogger rifLogger = RIFLogger.getLogger();
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
			PGSQLQueryUtility.close(checkGeographyExistsStatement);
			PGSQLQueryUtility.close(checkGeographyExistsResultSet);			
		}
	}
	
	@Override
	public void checkGeoLevelSelectExists(
			final Connection connection,
			final String geographyName,
			final String geoLevelSelectName)
		throws RIFServiceException {

		PreparedStatement checkGeoLevelViewExistsStatement = null;
		ResultSet checkGeoLevelViewExistsResultSet = null;
		try {

			//Create SQL query
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setLookupKeyFieldName("geolevel_name");
			queryFormatter.setFromTable("rif40_geolevels");
			queryFormatter.addWhereParameter("geography");
		
			//	TODO: (DM) listing is contradictory for SAHSULAND default values
		//	queryFormatter.addWhereParameter("listing");

			logSQLQuery(
				"checkGeoLevelViewExistsQuery",
				queryFormatter,
				geoLevelSelectName,
				geographyName,
				String.valueOf(1));
					
			//Parameterise and execute query		
			checkGeoLevelViewExistsStatement
				= createPreparedStatement(
					connection,
					queryFormatter);
			checkGeoLevelViewExistsStatement.setString(1, geoLevelSelectName);
			checkGeoLevelViewExistsStatement.setString(2, geographyName);
		//	checkGeoLevelViewExistsStatement.setInt(3, 1);
			checkGeoLevelViewExistsResultSet 
				= checkGeoLevelViewExistsStatement.executeQuery();

			if (checkGeoLevelViewExistsResultSet.next() == false) {
				//ERROR: no such geography exists
				String recordType
					= RIFServiceMessages.getMessage("geoLevelSelect.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						geoLevelSelectName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE, 
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
				= RIFServiceMessages.getMessage("geoLevelSelect.label");			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					geographyName);

			RIFLogger rifLogger = RIFLogger.getLogger();
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
			PGSQLQueryUtility.close(checkGeoLevelViewExistsStatement);
			PGSQLQueryUtility.close(checkGeoLevelViewExistsResultSet);			
		}		
	}
		
	@Override
	public void checkGeoLevelAreaExists(
			final Connection connection,
			final String geographyName,
			final String geoLevelSelectName,
			final String geoLevelAreaName)
		throws RIFServiceException {
		
		//Find the correct lookup table where all the areas will be listed
		String unableToCheckGeoLevelArea
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToCheckGeoLevelAreaExists");
		RIFServiceException unableToCheckGeoLevelAreaExistsException
			= new RIFServiceException(
				RIFServiceError.DB_UNABLE_CHECK_GEO_LEVEL_AREA_EXISTS,
				unableToCheckGeoLevelArea);	
		
		
		String geoLevelSelectLookupTable = null;
		PreparedStatement getLookupTableStatement = null;
		PreparedStatement geoLevelAreaExistsStatement = null;
		ResultSet geoLevelAreaExistsResultSet = null;
		ResultSet getLookupTableResultSet = null;
		try {
			PGSQLSelectQueryFormatter lookupTableQueryQueryFormatter = new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(lookupTableQueryQueryFormatter);
			lookupTableQueryQueryFormatter.addSelectField("lookup_table");
			lookupTableQueryQueryFormatter.addFromTable("rif40_geolevels");
			lookupTableQueryQueryFormatter.addWhereParameter("geography");
			lookupTableQueryQueryFormatter.addWhereParameter("geolevel_name");

			logSQLQuery(
				"lookupTableQuery",
				lookupTableQueryQueryFormatter,
				geographyName,
				geoLevelSelectName,
				String.valueOf(1));

			getLookupTableStatement 
					= createPreparedStatement(
						connection,
						lookupTableQueryQueryFormatter);
			getLookupTableStatement.setString(1, geographyName);
			getLookupTableStatement.setString(2, geoLevelSelectName);
			getLookupTableResultSet
				= getLookupTableStatement.executeQuery();
			getLookupTableResultSet.next();
			geoLevelSelectLookupTable = getLookupTableResultSet.getString(1);			
			

			if (geoLevelSelectLookupTable == null) {				
				connection.commit();
				
				return;
			}
		
			//Check whether the name exists
			PGSQLRecordExistsQueryFormatter recordExistsQueryFormatter 
				= new PGSQLRecordExistsQueryFormatter();
			recordExistsQueryFormatter.setFromTable(geoLevelSelectLookupTable);
			recordExistsQueryFormatter.setLookupKeyFieldName("name");
		
			logSQLQuery(
				"checkGeoLevelSelectExistsQuery",
				recordExistsQueryFormatter,
				geoLevelAreaName);
		
			geoLevelAreaExistsStatement
				= createPreparedStatement(
					connection,
					recordExistsQueryFormatter);
			geoLevelAreaExistsStatement.setString(1, geoLevelAreaName);
			geoLevelAreaExistsResultSet
				= geoLevelAreaExistsStatement.executeQuery();
			if (geoLevelAreaExistsResultSet.next() == false) {
				String recordType
					= RIFServiceMessages.getMessage("geoLevelArea.label");			
				//No such geolevel area exists
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						geoLevelAreaName);					
				
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEOLEVEL_AREA_VALUE,
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
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				unableToCheckGeoLevelArea, 
				sqlException);			
			throw unableToCheckGeoLevelAreaExistsException;			
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(getLookupTableStatement);
			PGSQLQueryUtility.close(getLookupTableResultSet);			
			PGSQLQueryUtility.close(getLookupTableStatement);
			PGSQLQueryUtility.close(getLookupTableResultSet);			
		}	
		
	}
	
	@Override
	public void checkGeoLevelToMapOrViewValueExists(
			final Connection connection,
			final String geographyName,
			final String geoLevelSelectName,
			final String geoLevelValueName,
			final boolean isToMapValue)
		throws RIFServiceException {

		PreparedStatement geoLevelIDStatement = null;
		ResultSet geoLevelIDResultSet = null;			
		PreparedStatement geoLevelValueExistsStatement = null;
		ResultSet geoLevelValueExistsResultSet = null;
		Integer geoLevelID = null;
		RIFServiceException unableToCheckValueExistsException = null;
		String unableToGetGeoLevelToMap
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelToMap");
		String unableToGetGeoLevelView
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelView");

		if (isToMapValue == true) {
			unableToCheckValueExistsException
				= new RIFServiceException(
					RIFServiceError.GET_GEOLEVEL_TO_MAP_VALUES,
					unableToGetGeoLevelToMap);				
		}
		else {
			unableToCheckValueExistsException
				= new RIFServiceException(
					RIFServiceError.GET_GEOLEVEL_VIEW_VALUES,
					unableToGetGeoLevelView);			
		}

		try {
		
			//Obtain the minimimum geolevel ID that the geoLevelMap needs to have
			PGSQLSelectQueryFormatter geoLevelIDQueryFormatter 
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(geoLevelIDQueryFormatter);		
			geoLevelIDQueryFormatter.addSelectField("geolevel_id");
			geoLevelIDQueryFormatter.addFromTable("rif40_geolevels");
			geoLevelIDQueryFormatter.addWhereParameter("geography");
			geoLevelIDQueryFormatter.addWhereParameter("geolevel_name");
			
			logSQLQuery(
				"geoLevelIDQuery",
				geoLevelIDQueryFormatter,
				geographyName,
				geoLevelSelectName);
					
			geoLevelIDStatement
				= createPreparedStatement(
					connection,
					geoLevelIDQueryFormatter);
			geoLevelIDStatement.setString(1, geographyName);
			geoLevelIDStatement.setString(2, geoLevelSelectName);
			geoLevelIDResultSet = geoLevelIDStatement.executeQuery();	
			if (geoLevelIDResultSet.next() == false) {
				//ERROR: no views available
				
				connection.commit();
				
				throw unableToCheckValueExistsException;
			}
			else {
				geoLevelID = geoLevelIDResultSet.getInt(1);
			}
			
			PGSQLRecordExistsQueryFormatter geoLevelMapExistsQueryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(geoLevelMapExistsQueryFormatter);		
			geoLevelMapExistsQueryFormatter.setFromTable("rif40_geolevels");
			geoLevelMapExistsQueryFormatter.addWhereParameter("geography");
			geoLevelMapExistsQueryFormatter.addWhereParameterWithOperator("geolevel_id",">=");
			geoLevelMapExistsQueryFormatter.addWhereParameter("geolevel_name");
		
			logSQLQuery(
				"geoLevelMapExistsQuery",
				geoLevelMapExistsQueryFormatter,
				geographyName,
				geoLevelSelectName);
				
			geoLevelValueExistsStatement
				= createPreparedStatement(
					connection,
					geoLevelMapExistsQueryFormatter);
			geoLevelValueExistsStatement.setString(1, geographyName);
			geoLevelValueExistsStatement.setInt(2, geoLevelID);
			geoLevelValueExistsStatement.setString(3, geoLevelValueName);

			geoLevelValueExistsResultSet 
				= geoLevelValueExistsStatement.executeQuery();
			if (geoLevelValueExistsResultSet.next() == false) {
				//No such geolevel map exists
				if (isToMapValue == true) {
					String recordType
						= RIFServiceMessages.getMessage("geoLevelToMap.label");
					String errorMessage
						= RIFServiceMessages.getMessage(
							"general.validation.nonExistentRecord",
							recordType,
							geoLevelValueName);					
					
					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFServiceError.NON_EXISTENT_GEOLEVEL_TO_MAP_VALUE,
							errorMessage);
					
					connection.commit();
					
					throw rifServiceException;
				}
				else {
					//it is a geo level view value
					String recordType
						= RIFServiceMessages.getMessage("geoLevelView.label");
					String errorMessage
						= RIFServiceMessages.getMessage(
							"general.validation.nonExistentRecord",
							recordType,
							geoLevelValueName);
				
					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFServiceError.NON_EXISTENT_GEOLEVEL_VIEW_VALUE,
							errorMessage);
					
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
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				unableToGetGeoLevelToMap, 
				sqlException);			
												
			throw unableToCheckValueExistsException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(geoLevelIDStatement);
			PGSQLQueryUtility.close(geoLevelIDResultSet);			
			PGSQLQueryUtility.close(geoLevelValueExistsStatement);
			PGSQLQueryUtility.close(geoLevelValueExistsResultSet);			
		}				
	}

	
	@Override
	public void checkGeoLevelToMapOrViewValueExists(
			final Connection connection,
			final String geographyName,
			final String geoLevelValueName,
			final boolean isToMapValue)
		throws RIFServiceException {

		PGSQLRecordExistsQueryFormatter queryFormatter 
			= new PGSQLRecordExistsQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);		
		queryFormatter.setFromTable("rif40_geolevels");
		queryFormatter.addWhereParameter("geography");
		queryFormatter.setLookupKeyFieldName("geolevel_name");

		logSQLQuery(
			"checkGeoLevelToMapOrViewValueExists",
			queryFormatter,
			geoLevelValueName,
			geographyName);
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;			

		RIFServiceException unableToCheckValueExistsException = null;
		String unableToGetGeoLevelToMap
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelToMap");
		String unableToGetGeoLevelView
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelView");
		if (isToMapValue == true) {
			unableToCheckValueExistsException
				= new RIFServiceException(
					RIFServiceError.GET_GEOLEVEL_TO_MAP_VALUES,
					unableToGetGeoLevelToMap);				
		}
		else {
			unableToCheckValueExistsException
				= new RIFServiceException(
					RIFServiceError.GET_GEOLEVEL_VIEW_VALUES,
					unableToGetGeoLevelView);			
		}
		
		try {
			statement
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setString(1, geoLevelValueName);
			statement.setString(2, geographyName);
			
			resultSet = statement.executeQuery();
			connection.commit();
			if (resultSet.next() == false) {

				connection.commit();
				
				if (isToMapValue == true) {
					String recordType = RIFServiceMessages.getMessage("geoLevelToMap.label");

					String errorMessage
						= RIFServiceMessages.getMessage(
							"general.validation.nonExistentRecord",
							recordType,
							geoLevelValueName);

					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFServiceError.NON_EXISTENT_GEOLEVEL_TO_MAP_VALUE, 
							errorMessage);
										
					throw rifServiceException;				
				}
				else {
					String recordType = RIFServiceMessages.getMessage("geoLevelView.label");

					String errorMessage
						= RIFServiceMessages.getMessage(
							"general.validation.nonExistentRecord",
							recordType,
							geoLevelValueName);

					RIFServiceException rifServiceException
						= new RIFServiceException(
							RIFServiceError.NON_EXISTENT_GEOLEVEL_VIEW_VALUE, 
							errorMessage);
					
					throw rifServiceException;				
				}				
			}
			
			connection.commit();
			
		}	
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version						
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			RIFLogger rifLogger = RIFLogger.getLogger();
			rifLogger.error(
				PGSQLRIFContextManager.class, 
				unableToGetGeoLevelToMap, 
				sqlException);			
										
			throw unableToCheckValueExistsException;
		}
		finally {
			//Cleanup database resources			
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}
		
	}
	
	@Override
	public void checkHealthThemeExists(
			final Connection connection,
			final String healthThemeDescription)
		throws RIFServiceException {

		PreparedStatement checkHealthThemeExistsStatement = null;
		ResultSet checkHealthThemeExistsResultSet = null;
		try {
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.setLookupKeyFieldName("description");
			queryFormatter.setFromTable("rif40_health_study_themes");

			logSQLQuery(
				"checkHealthThemeExists",
				queryFormatter,
				healthThemeDescription);
				
			checkHealthThemeExistsStatement
				= createPreparedStatement(
					connection,
					queryFormatter);
			checkHealthThemeExistsStatement.setString(1, healthThemeDescription);
			checkHealthThemeExistsResultSet 
				= checkHealthThemeExistsStatement.executeQuery();
			if (checkHealthThemeExistsResultSet.next() == false) {
				//ERROR: no such health theme exists
				String recordType = 
					RIFServiceMessages.getMessage("healthTheme.label");
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						healthThemeDescription);
				
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_HEALTH_THEME,
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
				= RIFServiceMessages.getMessage("healthTheme.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					healthThemeDescription);
			
			RIFLogger rifLogger = RIFLogger.getLogger();
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
			PGSQLQueryUtility.close(checkHealthThemeExistsStatement);
			PGSQLQueryUtility.close(checkHealthThemeExistsResultSet);			
		}		
	}	

	@Override
	public void checkNDPairExists(
			final User user,
			final Connection connection,
			final Geography geography,
			final NumeratorDenominatorPair ndPair)
		throws RIFServiceException {
				
		PreparedStatement getNDPairExistsStatement = null;
		ResultSet getNDPairExistsResultSet = null;
		try {
			PGSQLRecordExistsQueryFormatter ndPairExistsQueryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(ndPairExistsQueryFormatter);		
			ndPairExistsQueryFormatter.setFromTable("rif40_num_denom");
			ndPairExistsQueryFormatter.addWhereParameter("geography");
			ndPairExistsQueryFormatter.addWhereParameter("numerator_table");
			ndPairExistsQueryFormatter.addWhereParameter("denominator_table");

			logSQLQuery(
				"ndPairExistsQuery",
				ndPairExistsQueryFormatter,
				geography.getName(),
				ndPair.getNumeratorTableName(),
				ndPair.getDenominatorTableName());
		
			getNDPairExistsStatement
				= createPreparedStatement(
					connection,
					ndPairExistsQueryFormatter);
			getNDPairExistsStatement.setString(1, geography.getName());
			getNDPairExistsStatement.setString(2, ndPair.getNumeratorTableName());
			getNDPairExistsStatement.setString(3, ndPair.getDenominatorTableName());

			getNDPairExistsResultSet
				= getNDPairExistsStatement.executeQuery();
			connection.commit();
			if (!getNDPairExistsResultSet.next()) {
				//no such ND pair exists
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						ndPair.getRecordType(),
						ndPair.getDisplayName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_ND_PAIR, 
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
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					ndPair.getRecordType(),
					ndPair.getDisplayName());
			
			RIFLogger rifLogger = RIFLogger.getLogger();
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
			PGSQLQueryUtility.close(getNDPairExistsStatement);
			PGSQLQueryUtility.close(getNDPairExistsResultSet);						
		}		
	}

	@Override
	public void checkNumeratorTableExists(
			final User user,
			final Connection connection,
			final Geography geography,
			final String numeratorTableName)
		throws RIFServiceException {
				
		PreparedStatement getNDPairExistsStatement = null;
		ResultSet getNDPairExistsResultSet = null;
		try {
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.setFromTable("rif40_num_denom");
			queryFormatter.addWhereParameter("geography");
			queryFormatter.addWhereParameter("numerator_table");

			logSQLQuery(
				"checkNumeratorTableExists",
				queryFormatter,
				geography.getName(),
				numeratorTableName);

			getNDPairExistsStatement
				= createPreparedStatement(
					connection,
					queryFormatter);
			getNDPairExistsStatement.setString(1, geography.getName());
			getNDPairExistsStatement.setString(2, numeratorTableName);

			getNDPairExistsResultSet
				= getNDPairExistsStatement.executeQuery();
			if (!getNDPairExistsResultSet.next()) {
				String recordType
					= RIFServiceMessages.getMessage("numeratorDenominatorPair.numerator.label");
				//no such ND pair exists
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						numeratorTableName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_NUMERATOR_TABLE, 
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
				= RIFServiceMessages.getMessage("numeratorDenominatorPair.numerator.label");
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					recordType,
					numeratorTableName);
			
			RIFLogger rifLogger = RIFLogger.getLogger();
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
			PGSQLQueryUtility.close(getNDPairExistsStatement);
			PGSQLQueryUtility.close(getNDPairExistsResultSet);						
		}		
	}
}
