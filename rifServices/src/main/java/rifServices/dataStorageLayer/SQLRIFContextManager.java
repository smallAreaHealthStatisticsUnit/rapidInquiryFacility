package rifServices.dataStorageLayer;

import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.system.RIFServiceError;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.util.RIFLogger;

import java.util.ArrayList;
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

public class SQLRIFContextManager 
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
	 * Instantiates a new SQLRIF context manager.
	 */
	public SQLRIFContextManager() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/**
	 * Gets the geographies.
	 *
	 * @param connection the connection
	 * @return the geographies
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<Geography> getGeographies(
		final Connection connection) 
		throws RIFServiceException {
		
		ArrayList<Geography> results = new ArrayList<Geography>();

		//Create SQL query		
		SQLSelectQueryFormatter query = new SQLSelectQueryFormatter();
		query.setUseDistinct(true);
		query.addSelectField("geography");
		query.addFromTable("rif40_geographies");
		query.addOrderByCondition("geography");
		
		//Parameterise and execute query		
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		
		try {
			statement
				= connection.prepareStatement(query.generateQuery());
			dbResultSet = statement.executeQuery();
			while (dbResultSet.next()) {
				Geography geography = Geography.newInstance();
				geography.setName(dbResultSet.getString(1));
				
				//KLG: note we should have a description field for geography
				geography.setDescription("");
				results.add(geography);
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeographies");
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);					
			
																		
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError.GET_GEOGRAPHIES, errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}
		
		return results;
		
	}

	
	/**
	 * Gets the health themes.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @return the health themes
	 * @throws RIFServiceException the RIF service exception
	 */
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
		
		ArrayList<HealthTheme> results = new ArrayList<HealthTheme>();
		
		//Create SQL query		
		SQLSelectQueryFormatter formatter = new SQLSelectQueryFormatter();
		formatter.setUseDistinct(true);
		formatter.addSelectField("theme");
		formatter.addSelectField("description");
		formatter.addFromTable("rif40_health_study_themes");
		formatter.addOrderByCondition("description");
		
		//Parameterise and execute query		
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		try {
			statement
				= connection.prepareStatement(formatter.generateQuery());
			dbResultSet = statement.executeQuery();
			while (dbResultSet.next()) {
				HealthTheme healthTheme = HealthTheme.newInstance();
				healthTheme.setName(dbResultSet.getString(1));
				healthTheme.setDescription(dbResultSet.getString(2));
				results.add(healthTheme);
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetHealthThemes");

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);					
																							
			RIFServiceException rifServiceException
				= new RIFServiceException(RIFServiceError.GET_HEALTH_THEMES, errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);
		}
		
		return results;
	}
	
	/**
	 * A helper method used by services which are deployed within web resources. 
	 * When users build up their queries using web-based forms, the forms obtain field values
	 * by making calls to the web services.  The URL is supposed to contain all the
	 * parameter values that are necessary to retrieve the correct information.  
	 * The parameter values are strings, not complete Java objects.  The web resource needs
	 * a means of creating Java objects for the api of the RIFJobSubmissionService.  This 
	 * method helps obtain a numerator denominator pair given the numerator table name
	 * @param connection
	 * @param geography
	 * @param numeratorTableName
	 * @return
	 * @throws RIFServiceException
	 */
	public NumeratorDenominatorPair getNDPairFromNumeratorTableName(
		final Connection connection,
		final Geography geography,
		final String numeratorTableName) 
		throws RIFServiceException {
		
		validateCommonMethodParameters(
				connection,
				geography,
				null,
				null);
				
		//Create SQL query		
		SQLSelectQueryFormatter query = new SQLSelectQueryFormatter();
		query.setUseDistinct(true);
		query.addSelectField("numerator_description");
		query.addSelectField("denominator_table");
		query.addSelectField("denominator_description");		
		query.addFromTable("rif40_num_denom");
		query.addWhereParameter("numerator_table");

		//Parameterise and execute query		
		ArrayList<NumeratorDenominatorPair> results 
			= new ArrayList<NumeratorDenominatorPair>();;
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		
		try {
			statement
				= connection.prepareStatement(query.generateQuery());
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
			
			if (results.size() == 0) {
				//ERROR: There is no numerator denominator pair for this health theme
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlRIFContextManager.error.noNDPairForNumeratorTableName",
						numeratorTableName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NO_ND_PAIR_FOR_NUMERATOR_TABLE_NAME,
						errorMessage);
				throw rifServiceException;
			}
			
			return results.get(0);
			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetNumeratorDenominatorPair");
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);			
		}
		
	}
	
	/**
	 * Gets the numerator denominator pairs.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param healthTheme the health theme
	 * @return the numerator denominator pairs
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<NumeratorDenominatorPair> getNumeratorDenominatorPairs(
		final Connection connection,
		final Geography geography,
		final HealthTheme healthTheme) 
		throws RIFServiceException {

		validateCommonMethodParameters(
			connection,
			geography,
			healthTheme,
			null);
				
		//Create SQL query		
		SQLSelectQueryFormatter query = new SQLSelectQueryFormatter();
		query.setUseDistinct(true);
		query.addSelectField("numerator_table");
		query.addSelectField("numerator_description");
		query.addSelectField("denominator_table");
		query.addSelectField("denominator_description");		
		query.addFromTable("rif40_num_denom");
		query.addWhereParameter("theme_description");

		//Parameterise and execute query		
		ArrayList<NumeratorDenominatorPair> results 
			= new ArrayList<NumeratorDenominatorPair>();;
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;
		
		try {
			statement
				= connection.prepareStatement(query.generateQuery());
			statement.setString(1, healthTheme.getDescription());			
			
			dbResultSet = statement.executeQuery();
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
			
			if (results.size() == 0) {
				//ERROR: There is no numerator denominator pair for this health theme
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlRIFContextManager.error.noNDPairForHealthTheme",
						healthTheme.getDescription());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NO_ND_PAIR_FOR_HEALTH_THEME,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlRIFContextManager.error.unableToGetNumeratorDenominatorPair");
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);			
		}
		
		return results;
	}
	
	
	/**
	 * Gets the geo level select values.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @return the geo level select values
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<GeoLevelSelect> getGeoLevelSelectValues(
		final Connection connection,
		final Geography geography) 
		throws RIFServiceException {
		
		//Validate parameters
		validateCommonMethodParameters(
			connection,
			geography,
			null,
			null);
			
		ArrayList<GeoLevelSelect> results = new ArrayList<GeoLevelSelect>();		
		
		String errorMessage
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelSelect");
		RIFServiceException getGeoLevelSelectValuesException
			= new RIFServiceException(
				RIFServiceError.GET_GEOLEVEL_SELECT_VALUES, 
				errorMessage);
		
		
		//Obtain the maximum value for geolevel_id. We need to return
		//all geolevel choices which have a priority less than this
		Integer maximumGeoLevelID = null;
		SQLMinMaxQueryFormatter maximumGeoLevelIDQueryFormatter
			= new SQLMinMaxQueryFormatter(SQLMinMaxQueryFormatter.OperationType.MAX);
		maximumGeoLevelIDQueryFormatter.setCountableFieldName("geolevel_id");
		maximumGeoLevelIDQueryFormatter.setFromTable("rif40_geolevels");
		maximumGeoLevelIDQueryFormatter.addWhereParameter("geography");
		
		
		PreparedStatement getMaxGeoLevelIDStatement = null;
		ResultSet getMaxGeoLevelIDResultSet = null;
		try {
			getMaxGeoLevelIDStatement
				= connection.prepareStatement(maximumGeoLevelIDQueryFormatter.generateQuery());
			getMaxGeoLevelIDStatement.setString(1, geography.getName());
			getMaxGeoLevelIDResultSet
				= getMaxGeoLevelIDStatement.executeQuery();
			getMaxGeoLevelIDResultSet.next();
			maximumGeoLevelID = getMaxGeoLevelIDResultSet.getInt(1);
		}
		catch(SQLException sqlException) {			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
			throw getGeoLevelSelectValuesException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(getMaxGeoLevelIDStatement);
			SQLQueryUtility.close(getMaxGeoLevelIDResultSet);			
		}
		
		if (maximumGeoLevelID == null) {
			return results;
		}
		
		//Create SQL query		
		SQLSelectQueryFormatter getGeoLevelSelectValuesQuery = new SQLSelectQueryFormatter();
		getGeoLevelSelectValuesQuery.addSelectField("geolevel_name");
		getGeoLevelSelectValuesQuery.addFromTable("rif40_geolevels");
		getGeoLevelSelectValuesQuery.addWhereParameter("geography");
		getGeoLevelSelectValuesQuery.addWhereParameter("listing");
		getGeoLevelSelectValuesQuery.addWhereParameterWithOperator("geolevel_id", "<");
		getGeoLevelSelectValuesQuery.addOrderByCondition("geolevel_id");
		
		//Parameterise and execute query		
		PreparedStatement getGeoLevelSelectStatement = null;
		ResultSet getGeoLevelSelectResultSet = null;		
		try {
			getGeoLevelSelectStatement
				= connection.prepareStatement(getGeoLevelSelectValuesQuery.generateQuery());
			getGeoLevelSelectStatement.setString(1, geography.getName());
			//only include those ids that are designated as 'listable'
			getGeoLevelSelectStatement.setInt(2, 1);
			getGeoLevelSelectStatement.setInt(3, maximumGeoLevelID);
			getGeoLevelSelectResultSet = getGeoLevelSelectStatement.executeQuery();
			
			while (getGeoLevelSelectResultSet.next()) {
				GeoLevelSelect geoLevelSelect
					= GeoLevelSelect.newInstance(getGeoLevelSelectResultSet.getString(1));
				results.add(geoLevelSelect);				
			}			
		}
		catch(SQLException sqlException) {
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
			throw getGeoLevelSelectValuesException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(getGeoLevelSelectStatement);
			SQLQueryUtility.close(getGeoLevelSelectResultSet);			
		}		
		return results;		
	}
	
	/**
	 * Gets the default geo level select value.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @return the default geo level select value
	 * @throws RIFServiceException the RIF service exception
	 */
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
		
		//Create SQL query		
		SQLSelectQueryFormatter formatter = new SQLSelectQueryFormatter();
		formatter.addSelectField("defaultcomparea");
		formatter.addFromTable("rif40_geographies");
		formatter.addWhereParameter("geography");


		//Parameterise and execute query				
		GeoLevelSelect result = null;				
		PreparedStatement statement = null;
		ResultSet dbResultSet = null;		

		try {
			statement
				= connection.prepareStatement(formatter.generateQuery());
			statement.setString(1, geography.getName());
			dbResultSet = statement.executeQuery();
				
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
			
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelSelect");

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(dbResultSet);			
		}		
		
		return result;		
	}

	/**
	 * Gets the geo level area values.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @return the geo level area values
	 * @throws RIFServiceException the RIF service exception
	 */
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
		SQLSelectQueryFormatter lookupTableQuery = new SQLSelectQueryFormatter();
		lookupTableQuery.addSelectField("lookup_table");
		lookupTableQuery.addFromTable("rif40_geolevels");
		lookupTableQuery.addWhereParameter("geography");
		lookupTableQuery.addWhereParameter("geolevel_name");

		PreparedStatement lookupTableStatement = null;
		ResultSet lookupTableResultSet = null;			
		
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
				= connection.prepareStatement(lookupTableQuery.generateQuery());
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
		}
		catch(SQLException sqlException) {
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
					
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(lookupTableStatement);
			SQLQueryUtility.close(lookupTableResultSet);				
		}
		
		if (lookupTableName == null) {
			throw rifServiceException;
		}

	
		//Given the lookup table name, retrieve the areas
		SQLSelectQueryFormatter geographicAreaQuery 
			= new SQLSelectQueryFormatter();
		geographicAreaQuery.addSelectField(useAppropriateFieldNameCase(geoLevelSelect.getName()));		
		geographicAreaQuery.addSelectField("name");
		geographicAreaQuery.addFromTable(lookupTableName);
		geographicAreaQuery.addOrderByCondition("name");
	
		PreparedStatement geographicAreaStatement = null;
		ResultSet geographicAreaResultSet = null;		
		
		try {
			geographicAreaStatement
				= connection.prepareStatement(geographicAreaQuery.generateQuery());
						
			geographicAreaResultSet = geographicAreaStatement.executeQuery();	
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
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
							
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(geographicAreaStatement);
			SQLQueryUtility.close(geographicAreaResultSet);			
		}
				
		return results;		
	}	
	
	
	/**
	 * Gets the geo level view values.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @return the geo level view values
	 * @throws RIFServiceException the RIF service exception
	 */
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
		
		ArrayList<GeoLevelView> results = new ArrayList<GeoLevelView>();
		
		//Create SQL query		
		SQLSelectQueryFormatter geoLevelIDQuery = new SQLSelectQueryFormatter();
		geoLevelIDQuery.addSelectField("geolevel_id");
		geoLevelIDQuery.addFromTable("rif40_geolevels");
		geoLevelIDQuery.addWhereParameter("geography");
		geoLevelIDQuery.addWhereParameter("geolevel_name");
		
		PreparedStatement geoLevelIDStatement = null;
		ResultSet geoLevelIDResultSet = null;			

		String errorMessage
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelView");
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFServiceError.GET_GEOLEVEL_VIEW_VALUES,
				errorMessage);	
		
		Integer geoLevelID = null;
		try {
			geoLevelIDStatement
				= connection.prepareStatement(geoLevelIDQuery.generateQuery());
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
		}
		catch(SQLException sqlException) {

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
						
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(geoLevelIDStatement);
			SQLQueryUtility.close(geoLevelIDResultSet);			
		}
		if (geoLevelID == null) {
			throw rifServiceException;
		}
		
		PreparedStatement geoLevelViewsStatement = null;
		ResultSet geoLevelViewsResultSet = null;
		SQLSelectQueryFormatter geoLevelViewsQuery 
			= new SQLSelectQueryFormatter();
		geoLevelViewsQuery.addSelectField("geolevel_name");
		geoLevelViewsQuery.addFromTable("rif40_geolevels");
		geoLevelViewsQuery.addWhereParameter("geography");
		geoLevelViewsQuery.addWhereParameterWithOperator("geolevel_id",">");
		geoLevelViewsQuery.addOrderByCondition("geolevel_name");
		try {
			geoLevelViewsStatement
				= connection.prepareStatement(geoLevelViewsQuery.generateQuery());
			geoLevelViewsStatement.setString(1, geography.getName());
			geoLevelViewsStatement.setInt(2, geoLevelID.intValue());
			geoLevelViewsResultSet = geoLevelViewsStatement.executeQuery();
			
			while (geoLevelViewsResultSet.next()) {
				GeoLevelView geoLevelView 
					= GeoLevelView.newInstance(geoLevelViewsResultSet.getString(1));
				results.add(geoLevelView);
			}			
		}
		catch(SQLException sqlException) {

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
					
			throw rifServiceException;			
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(geoLevelViewsStatement);
			SQLQueryUtility.close(geoLevelViewsResultSet);			
		}
		
		return results;
	}
		
	/**
	 * Gets the geo level to map values.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @return the geo level to map values
	 * @throws RIFServiceException the RIF service exception
	 */
	public ArrayList<GeoLevelToMap> getGeoLevelToMapValues(
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
				
		ArrayList<GeoLevelToMap> results = new ArrayList<GeoLevelToMap>();
			
		//Create SQL query		
		SQLSelectQueryFormatter geoLevelIDQuery = new SQLSelectQueryFormatter();
		geoLevelIDQuery.addSelectField("geolevel_id");
		geoLevelIDQuery.addFromTable("rif40_geolevels");
		geoLevelIDQuery.addWhereParameter("geography");
		geoLevelIDQuery.addWhereParameter("geolevel_name");
			
		PreparedStatement geoLevelIDStatement = null;
		ResultSet geoLevelIDResultSet = null;			

		String errorMessage
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelToMap");
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFServiceError.GET_GEOLEVEL_TO_MAP_VALUES,
				errorMessage);	
			
		Integer geoLevelID = null;
		try {
			geoLevelIDStatement
				= connection.prepareStatement(geoLevelIDQuery.generateQuery());
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
		}
		catch(SQLException sqlException) {
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
								
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(geoLevelIDStatement);
			SQLQueryUtility.close(geoLevelIDResultSet);			
		}
		if (geoLevelID == null) {
			throw rifServiceException;
		}
			
		PreparedStatement geoLevelToMapStatement = null;
		ResultSet geoLevelToMapResultSet = null;
		SQLSelectQueryFormatter geoLevelViewsQuery 
			= new SQLSelectQueryFormatter();
		geoLevelViewsQuery.addSelectField("geolevel_name");
		geoLevelViewsQuery.addFromTable("rif40_geolevels");
		geoLevelViewsQuery.addWhereParameter("geography");
		geoLevelViewsQuery.addWhereParameterWithOperator("geolevel_id",">");
		geoLevelViewsQuery.addOrderByCondition("geolevel_name");
		try {
			geoLevelToMapStatement
				= connection.prepareStatement(geoLevelViewsQuery.generateQuery());
			geoLevelToMapStatement.setString(1, geography.getName());
			geoLevelToMapStatement.setInt(2, geoLevelID.intValue());
			geoLevelToMapResultSet = geoLevelToMapStatement.executeQuery();
			
			while (geoLevelToMapResultSet.next()) {
				GeoLevelToMap geoLevelToMap
					= GeoLevelToMap.newInstance(geoLevelToMapResultSet.getString(1));
				results.add(geoLevelToMap);
			}			
		}
		catch(SQLException sqlException) {
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				errorMessage, 
				sqlException);										
								
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(geoLevelToMapStatement);
			SQLQueryUtility.close(geoLevelToMapResultSet);			
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

		if (geography != null) {
			geography.checkErrors();			
			checkGeographyExists(connection, geography);
		}
		
		if (healthTheme != null) {
			healthTheme.checkErrors();			
			checkHealthThemeExists(connection, healthTheme);
		}
		
		if (geoLevelSelect != null) {
			geoLevelSelect.checkErrors();
			checkGeoLevelSelectExists(
				connection, 
				geography, 
				geoLevelSelect);			
		}
	}
	
	/**
	 * checks if geography exists.  If it doesn't it throws an exception.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkGeographyExists(
		final Connection connection,
		final Geography geography) 
		throws RIFServiceException {
	
		//Create SQL query
		SQLRecordExistsQueryFormatter query
			= new SQLRecordExistsQueryFormatter();
		query.setFromTable("rif40_num_denom");
		query.setLookupKeyFieldName("geography");
		
		PreparedStatement checkGeographyExistsStatement = null;
		ResultSet checkGeographyExistsResultSet = null;
		
		//Parameterise and execute query		
		try {
			checkGeographyExistsStatement
				= connection.prepareStatement(query.generateQuery());
			checkGeographyExistsStatement.setString(1, geography.getName());
			checkGeographyExistsResultSet 
				= checkGeographyExistsStatement.executeQuery();
			
			if (checkGeographyExistsResultSet.next() == false) {
				
				//ERROR: no such geography exists
				String recordType
					= geography.getRecordType();
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						geography.getDisplayName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEOGRAPHY, 
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {	
			sqlException.printStackTrace(System.out);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					geography.getRecordType(),
					geography.getDisplayName());
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
			SQLQueryUtility.close(checkGeographyExistsStatement);
			SQLQueryUtility.close(checkGeographyExistsResultSet);			
		}
	}
	
	/**
	 * Check non existent geo level select.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkGeoLevelSelectExists(
		final Connection connection,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException {

		//Create SQL query
		SQLRecordExistsQueryFormatter query
			= new SQLRecordExistsQueryFormatter();
		query.setLookupKeyFieldName("geolevel_name");
		query.setFromTable("rif40_geolevels");
		query.addWhereParameter("geography");
		query.addWhereParameter("listing");
		
		PreparedStatement checkGeoLevelViewExistsStatement = null;
		ResultSet checkGeoLevelViewExistsResultSet = null;
		
		//Parameterise and execute query		
		try {
			checkGeoLevelViewExistsStatement
				= connection.prepareStatement(query.generateQuery());
			checkGeoLevelViewExistsStatement.setString(1, geoLevelSelect.getName());
			checkGeoLevelViewExistsStatement.setString(2, geography.getName());
			checkGeoLevelViewExistsStatement.setInt(3, 1);
			checkGeoLevelViewExistsResultSet 
				= checkGeoLevelViewExistsStatement.executeQuery();
			
			if (checkGeoLevelViewExistsResultSet.next() == false) {
				//ERROR: no such geography exists
				String recordType
					= geoLevelSelect.getRecordType();
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						geoLevelSelect.getDisplayName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEOLEVEL_SELECT_VALUE, 
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					geography.getRecordType(),
					geography.getDisplayName());

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
			SQLQueryUtility.close(checkGeoLevelViewExistsStatement);
			SQLQueryUtility.close(checkGeoLevelViewExistsResultSet);			
		}		
	}
		
	/**
	 * Check non existent geo level area.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelArea the geo level area
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkGeoLevelAreaExists(
		final Connection connection,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelArea geoLevelArea) 
		throws RIFServiceException {
		
		//Find the correct lookup table where all the areas will be listed
		String unableToCheckGeoLevelArea
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToCheckGeoLevelAreaExists");
		RIFServiceException unableToCheckGeoLevelAreaExistsException
			= new RIFServiceException(
				RIFServiceError.DB_UNABLE_CHECK_GEO_LEVEL_AREA_EXISTS,
				unableToCheckGeoLevelArea);	
		
		SQLSelectQueryFormatter lookupTableQuery = new SQLSelectQueryFormatter();
		lookupTableQuery.addSelectField("lookup_table");
		lookupTableQuery.addFromTable("rif40_geolevels");
		lookupTableQuery.addWhereParameter("geography");
		lookupTableQuery.addWhereParameter("geolevel_name");

		String geoLevelSelectLookupTable = null;
		PreparedStatement getLookupTableStatement = null;
		ResultSet getLookupTableResultSet = null;
		try {
			getLookupTableStatement 
				= connection.prepareStatement(lookupTableQuery.generateQuery());
			getLookupTableStatement.setString(1, geography.getName());
			getLookupTableStatement.setString(2, geoLevelSelect.getName());
			getLookupTableResultSet
				= getLookupTableStatement.executeQuery();
			getLookupTableResultSet.next();
			geoLevelSelectLookupTable = getLookupTableResultSet.getString(1);			
		}
		catch(SQLException sqlException) {
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				unableToCheckGeoLevelArea, 
				sqlException);										
			
			throw unableToCheckGeoLevelAreaExistsException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(getLookupTableStatement);
			SQLQueryUtility.close(getLookupTableResultSet);			
		}
		
		if (geoLevelSelectLookupTable == null) {
			return;
		}
		
		//Check whether the name exists
		SQLRecordExistsQueryFormatter recordExistsFormatter 
			= new SQLRecordExistsQueryFormatter();
		recordExistsFormatter.setFromTable(geoLevelSelectLookupTable);
		String keyFieldName
        	= useAppropriateFieldNameCase("name");
		//String keyFieldName
        // = useAppropriateFieldNameCase(geoLevelSelect.getName());
		recordExistsFormatter.setLookupKeyFieldName(keyFieldName);
		PreparedStatement geoLevelAreaExistsStatement = null;
		ResultSet geoLevelAreaExistsResultSet = null;
		
		try {
			geoLevelAreaExistsStatement
				= connection.prepareStatement(recordExistsFormatter.generateQuery());
			geoLevelAreaExistsStatement.setString(1, geoLevelArea.getName());
			geoLevelAreaExistsResultSet
				= geoLevelAreaExistsStatement.executeQuery();
			if (geoLevelAreaExistsResultSet.next() == false) {
				//No such geolevel area exists
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						geoLevelArea.getRecordType(),
						geoLevelArea.getDisplayName());					
				
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEOLEVEL_AREA_VALUE,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {

			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				unableToCheckGeoLevelArea, 
				sqlException);			
			throw unableToCheckGeoLevelAreaExistsException;			
		}
		finally {
			//Cleanup database resources
			SQLQueryUtility.close(getLookupTableStatement);
			SQLQueryUtility.close(getLookupTableResultSet);			
		}	
		
	}
	
	/**
	 * Check non existent geo level to map value.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param geoLevelSelect the geo level select
	 * @param geoLevelToMap the geo level to map
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkGeoLevelToMapValueExists(
		final Connection connection,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelToMap geoLevelToMap) 
		throws RIFServiceException {
				
		//Obtain the minimimum geolevel ID that the geoLevelMap needs to have
		SQLSelectQueryFormatter geoLevelIDQuery = new SQLSelectQueryFormatter();
		geoLevelIDQuery.addSelectField("geolevel_id");
		geoLevelIDQuery.addFromTable("rif40_geolevels");
		geoLevelIDQuery.addWhereParameter("geography");
		geoLevelIDQuery.addWhereParameter("geolevel_name");
			
		PreparedStatement geoLevelIDStatement = null;
		ResultSet geoLevelIDResultSet = null;			

		String unableToGetGeoLevelToMap
			= RIFServiceMessages.getMessage("sqlRIFContextManager.error.unableToGetGeoLevelToMap");
		RIFServiceException unableToCheckGeoLevelMapExistsException
			= new RIFServiceException(
				RIFServiceError.GET_GEOLEVEL_TO_MAP_VALUES,
				unableToGetGeoLevelToMap);	
			
		Integer geoLevelID = null;
		try {
			geoLevelIDStatement
				= connection.prepareStatement(geoLevelIDQuery.generateQuery());
			geoLevelIDStatement.setString(1, geography.getName());
			geoLevelIDStatement.setString(2, geoLevelSelect.getName());
			geoLevelIDResultSet = geoLevelIDStatement.executeQuery();	
			if (geoLevelIDResultSet.next() == false) {
				//ERROR: no views available
				throw unableToCheckGeoLevelMapExistsException;
			}
			else {
				geoLevelID = geoLevelIDResultSet.getInt(1);
			}
		}	
		catch(SQLException sqlException) {
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				unableToGetGeoLevelToMap, 
				sqlException);			
										
			throw unableToCheckGeoLevelMapExistsException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(geoLevelIDStatement);
			SQLQueryUtility.close(geoLevelIDResultSet);			
		}
		
		PreparedStatement geoLevelToMapExistsStatement = null;
		ResultSet geoLevelToMapExistsResultSet = null;
		SQLRecordExistsQueryFormatter geoLevelMapExistsQuery
			= new SQLRecordExistsQueryFormatter();
		geoLevelMapExistsQuery.setFromTable("rif40_geolevels");
		geoLevelMapExistsQuery.addWhereParameter("geography");
		geoLevelMapExistsQuery.addWhereParameterWithOperator("geolevel_id",">");
		geoLevelMapExistsQuery.addWhereParameter("geolevel_name");
		try {
			geoLevelToMapExistsStatement
				= connection.prepareStatement(geoLevelMapExistsQuery.generateQuery());
			geoLevelToMapExistsStatement.setString(1, geography.getName());
			geoLevelToMapExistsStatement.setInt(2, geoLevelID);
			geoLevelToMapExistsStatement.setString(3, geoLevelToMap.getName());

			geoLevelToMapExistsResultSet 
				= geoLevelToMapExistsStatement.executeQuery();
			if (geoLevelToMapExistsResultSet.next() == false) {
				//No such geolevel map exists
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						geoLevelToMap.getRecordType(),
						geoLevelToMap.getDisplayName());					
					
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEOLEVEL_TO_MAP_VALUE,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
				unableToGetGeoLevelToMap, 
				sqlException);			
												
			throw unableToCheckGeoLevelMapExistsException;
		}
		finally {
			//Cleanup database resources			
			SQLQueryUtility.close(geoLevelToMapExistsStatement);
			SQLQueryUtility.close(geoLevelToMapExistsResultSet);			
		}				
	}

	/**
	 * Check non existent health theme.
	 *
	 * @param connection the connection
	 * @param healthTheme the health theme
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkHealthThemeExists(
		final Connection connection,
		final HealthTheme healthTheme)
		throws RIFServiceException {

		SQLRecordExistsQueryFormatter query
			= new SQLRecordExistsQueryFormatter();
		query.setLookupKeyFieldName("theme_description");
		query.setFromTable("rif40_num_denom");

		PreparedStatement checkHealthThemeExistsStatement = null;
		ResultSet checkHealthThemeExistsResultSet = null;
		try {
			checkHealthThemeExistsStatement
				= connection.prepareStatement(query.generateQuery());
			checkHealthThemeExistsStatement.setString(1, healthTheme.getDescription());
			checkHealthThemeExistsResultSet 
				= checkHealthThemeExistsStatement.executeQuery();
			if (checkHealthThemeExistsResultSet.next() == false) {
				//ERROR: no such health theme exists
				String recordType = healthTheme.getRecordType();
				String errorMessage
					= RIFServiceMessages.getMessage(
						"general.validation.nonExistentRecord",
						recordType,
						healthTheme.getDisplayName());
				
				System.out.println("SQLRIFContextManager == XXXXXXXXXXXXXX errorMessage=="+errorMessage+"==");
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_HEALTH_THEME,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					healthTheme.getRecordType(),
					healthTheme.getDisplayName());
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
			SQLQueryUtility.close(checkHealthThemeExistsStatement);
			SQLQueryUtility.close(checkHealthThemeExistsResultSet);			
		}		
	}	

	/**
	 * Check non existent nd pair.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @throws RIFServiceException the RIF service exception
	 */
	public void checkNDPairExists(
		final Connection connection,
		final Geography geography,
		final NumeratorDenominatorPair ndPair) 
		throws RIFServiceException {
				
		SQLRecordExistsQueryFormatter ndPairExistsQuery
			= new SQLRecordExistsQueryFormatter();
		ndPairExistsQuery.setFromTable("rif40_num_denom");
		ndPairExistsQuery.addWhereParameter("geography");
		ndPairExistsQuery.addWhereParameter("numerator_table");
		ndPairExistsQuery.addWhereParameter("denominator_table");
		
		PreparedStatement getNDPairExistsStatement = null;
		ResultSet getNDPairExistsResultSet = null;
		try {
			getNDPairExistsStatement
				= connection.prepareStatement(ndPairExistsQuery.generateQuery());
			getNDPairExistsStatement.setString(1, geography.getName());
			getNDPairExistsStatement.setString(2, ndPair.getNumeratorTableName());
			getNDPairExistsStatement.setString(3, ndPair.getDenominatorTableName());

			getNDPairExistsResultSet
				= getNDPairExistsStatement.executeQuery();
			if (getNDPairExistsResultSet.next() == false) {
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
				
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			String errorMessage
				= RIFServiceMessages.getMessage(
					"general.validation.unableCheckNonExistentRecord",
					ndPair.getRecordType(),
					ndPair.getDisplayName());
			
			RIFLogger rifLogger = new RIFLogger();
			rifLogger.error(
				SQLRIFContextManager.class, 
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
			SQLQueryUtility.close(getNDPairExistsStatement);
			SQLQueryUtility.close(getNDPairExistsResultSet);						
		}		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
