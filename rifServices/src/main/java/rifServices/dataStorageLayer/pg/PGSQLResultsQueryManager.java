package rifServices.dataStorageLayer.pg;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLRecordExistsQueryFormatter;
import rifGenericLibrary.dataStorageLayer.pg.PGSQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.*;
import rifServices.businessConceptLayer.AbstractRIFConcept.ValidationPolicy;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.util.FieldValidationUtility;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 * Public methods assume that parameter values are non-null and do not present
 * security concerns.  Error checks focus on the following kinds of errors:
 * <ul>
 * <li>
 * check whether String values conform to regular expressions expected in the
 * database
 * </li>
 * <li>
 * check that parameter objects instantiated from business classes do not have
 * field-level errors or errors caused by combinations of errors
 * </li>
 * <li>
 * check that combinations of parameter values do not exhibit errors
 * </li>
 * </ul>
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

final class PGSQLResultsQueryManager extends PGSQLAbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private PGSQLRIFContextManager sqlRIFContextManager;
	private PGSQLMapDataManager sqlMapDataManager;
	private PGSQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager;
	private PGSQLInMemoryTileCache inMemoryTileCache;	
	private PGSQLFunctionCallerQueryFormatter getTilesQueryFormatter;
	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLResultsQueryManager(
		final RIFDatabaseProperties rifDatabaseProperties,
		final PGSQLRIFContextManager sqlRIFContextManager,
		final PGSQLMapDataManager sqlMapDataManager,
		final PGSQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager) {
		
		super(rifDatabaseProperties);
		this.sqlRIFContextManager = sqlRIFContextManager;
		this.sqlMapDataManager = sqlMapDataManager;
		this.sqlDiseaseMappingStudyManager = sqlDiseaseMappingStudyManager;
			
		inMemoryTileCache = PGSQLInMemoryTileCache.getInMemoryTileCache();

		
		getTilesQueryFormatter
			= new PGSQLFunctionCallerQueryFormatter();
		configureQueryFormatterForDB(getTilesQueryFormatter);
		getTilesQueryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		getTilesQueryFormatter.setFunctionName("rif40_get_geojson_tiles");
		getTilesQueryFormatter.setNumberOfFunctionParameters(9);		
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public BoundaryRectangle getGeoLevelBoundsForArea(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final MapArea mapArea)
		throws RIFServiceException {
	
		//Validate parameters
		ValidationPolicy validationPolicy = getValidationPolicy();
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		mapArea.checkErrors(validationPolicy);
		checkMapAreaExists(
			connection, 
			studyResultRetrievalContext,
			mapArea);
		checkMapAreaExistsInStudy(
			connection, 
			studyResultRetrievalContext,
			mapArea);

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
		
			//Create query
			PGSQLFunctionCallerQueryFormatter queryFormatter
				= new PGSQLFunctionCallerQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
			queryFormatter.setFunctionName("rif40_getgeolevelboundsforarea");
			queryFormatter.setNumberOfFunctionParameters(3);
		
			//Execute query and generate results
			statement
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.setString(1, studyResultRetrievalContext.getGeographyName());
			statement.setString(2, studyResultRetrievalContext.getGeoLevelSelectName());
			statement.setString(3, mapArea.getIdentifier());
			resultSet = statement.executeQuery();

			/*
			 * Expecting a result with four columns:
			 * yMax  |   xMax   |  yMax  |  yMin
			 * 
			 * Assumes at least one result returned because
			 * SQL function call will throw an exception if
			 * no results are returned
			 */
			//Assumes at least one result, because function will
			//
			resultSet.next();
			
			double yMax = resultSet.getDouble(1);
			double xMax = resultSet.getDouble(2);
			double yMin = resultSet.getDouble(3);			
			double xMin = resultSet.getDouble(4);
						
			BoundaryRectangle result
				= BoundaryRectangle.newInstance(
					String.valueOf(xMin),
					String.valueOf(yMin),
					String.valueOf(xMax),
					String.valueOf(yMax));
			
			connection.commit();
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetBoundsForArea",
					mapArea.getDisplayName(),
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;			
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
		}		
	}	
	
	public RIFResultTable getTileMakerCentroids(
			final Connection connection,
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect)
			throws RIFServiceException {
								
			PGSQLSelectQueryFormatter getMapTileTableQueryFormatter
				= new PGSQLSelectQueryFormatter();		
				
			getMapTileTableQueryFormatter.setDatabaseSchemaName("rif_data");
			getMapTileTableQueryFormatter.addSelectField(geoLevelSelect.getName());
			getMapTileTableQueryFormatter.addSelectField("areaname");
			getMapTileTableQueryFormatter.addSelectField("geographic_centroid");		
			getMapTileTableQueryFormatter.addFromTable("lookup_" + geoLevelSelect.getName());
					
			PreparedStatement resultCounterStatement = null;
			PreparedStatement statement = null;
			ResultSet resultCounterSet = null;
			ResultSet resultSet = null;
					
			try {
				//Count the number of results first
				resultCounterStatement = connection.prepareStatement(getMapTileTableQueryFormatter.generateQuery());
				resultCounterSet = resultCounterStatement.executeQuery();
				
				int totalNumberRowsInResults = 0;
				while (resultCounterSet.next()) {			
					totalNumberRowsInResults++;
				}

				//get the results
				statement = connection.prepareStatement(getMapTileTableQueryFormatter.generateQuery());	
				
				RIFResultTable results = new RIFResultTable();	
				
				String[] columnNames = new String[4];
				columnNames[0] = "id";
				columnNames[1] = "name";
				columnNames[2] = "x";
				columnNames[3] = "y";
				
				RIFResultTable.ColumnDataType[] columnDataTypes = new RIFResultTable.ColumnDataType[4];
				columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[1] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[2] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[3] = RIFResultTable.ColumnDataType.TEXT;
				
				String[][] data = new String[totalNumberRowsInResults][4];
				int ithRow = 0;
												
				resultSet = statement.executeQuery();
				while (resultSet.next()) {					
					data[ithRow][0] = resultSet.getString(1);
					data[ithRow][1] = resultSet.getString(2);									
					String coords = resultSet.getString(3).split(":")[2];	
					String x = coords.split(",")[0];
					String y = coords.split(",")[1];
					x = x.replaceAll("[^0-9?!\\.-]","");
					y = y.replaceAll("[^0-9?!\\.-]","");		
					data[ithRow][2] = x;
					data[ithRow][3] = y;
					ithRow++;
				}	
				
				results.setColumnProperties(columnNames, columnDataTypes);
				results.setData(data);	
				connection.commit();	
									
				return results;
			}
			catch(SQLException sqlException) {
				//Record original exception, throw sanitised, human-readable version			
				logSQLException(sqlException);
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.unableToGetCentroids",
						geoLevelSelect.getDisplayName(),
						geography.getDisplayName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.DATABASE_QUERY_FAILED,
						errorMessage);
				throw rifServiceException;
			}
			finally {
				//Cleanup database resources
				PGSQLQueryUtility.close(statement);
				PGSQLQueryUtility.close(resultSet);
			}			
		}
	
	public String getTileMakerTiles(
		final Connection connection,
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final Integer zoomlevel,
		final Integer x,		
		final Integer y)
		throws RIFServiceException {
		
		/*
		 * http://localhost:8080/rifServices/studyResultRetrieval/getTileMakerTiles?userID=kgarwood&geographyName=SAHSU&geoLevelSelectName=LEVEL2&zoomlevel={z}&x={x}&y={y}
		 */
		
		//STEP 1: get the tile table name
		//SELECT tiletable FROM rif40.rif40_geographies WHERE geography = 'SAHSULAND' 
							
		PGSQLSelectQueryFormatter getMapTileTableQueryFormatter
			= new PGSQLSelectQueryFormatter();		
			
		getMapTileTableQueryFormatter.setDatabaseSchemaName("rif40");
		getMapTileTableQueryFormatter.addSelectField("rif40_geographies", "tiletable");
		getMapTileTableQueryFormatter.addFromTable("rif40_geographies");
		getMapTileTableQueryFormatter.addWhereParameter("geography");
				
		//For tile table name
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		//For map tiles
		PreparedStatement statement2 = null;
		ResultSet resultSet2 = null;
		
		try {
	
			statement = connection.prepareStatement(getMapTileTableQueryFormatter.generateQuery());		
			statement.setString(1, geography.getName().toUpperCase());
					
			resultSet = statement.executeQuery();
			resultSet.next();
			
			//This is the tile table name for this geography
			String myTileTable = resultSet.getString(1);
					
			PGSQLSelectQueryFormatter getMapTilesQueryFormatter
				= new PGSQLSelectQueryFormatter();
				
			//STEP 2: get the tiles	
			/*
				SELECT 
				   TILES_SAHSULAND.optimised_topojson
				FROM 
				   TILES_SAHSULAND,
				   rif40_geolevels 
				WHERE
				   TILES_SAHSULAND.geolevel_id = rif40_geolevels.geolevel_id AND
				   rif40_geolevels.geolevel_name='SAHSU_GRD_LEVEL2' AND
				   TILES_SAHSULAND.zoomlevel=10 AND 
				   TILES_SAHSULAND.x=490 AND 
				   TILES_SAHSULAND.y=324
			*/
				
			getMapTilesQueryFormatter.addSelectField(myTileTable,"optimised_topojson");
			getMapTilesQueryFormatter.addFromTable(myTileTable);
			getMapTilesQueryFormatter.addFromTable("rif40_geolevels");	
			getMapTilesQueryFormatter.addWhereJoinCondition(myTileTable, "geolevel_id", "rif40_geolevels", "geolevel_id");
			getMapTilesQueryFormatter.addWhereParameter("rif40_geolevels", "geolevel_name");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "zoomlevel");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "x");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "y");
													
			statement2 = connection.prepareStatement(getMapTilesQueryFormatter.generateQuery());	
			statement2.setString(1, geoLevelSelect.getName().toUpperCase());
			statement2.setInt(2, zoomlevel);
			statement2.setInt(3, x);
			statement2.setInt(4, y);
			
			resultSet2 = statement2.executeQuery();
			resultSet2.next();
			String result = resultSet2.getString(1);

			connection.commit();				
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTiles",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);
			
			PGSQLQueryUtility.close(statement2);
			PGSQLQueryUtility.close(resultSet2);
		}			
	}
				

	/**
	 * Obtains the name of the table of calculated results that is associated with 
	 * a given study.  The name of the table should appear in the 'map_table' field
	 * of rif40_studies.  Note that this is different from the extract table, which
	 * appears in the 'extract_table' field of the same table.
	 * Assumes that the study exists
	 * @param connection
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	private String getCalculatedResultTableName(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
		
			//Create query
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("map_table");
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addWhereParameter("study_id");
		
			logSQLQuery(
				"getCalculatedResultTableName",
				queryFormatter,
				studyID);
		
			//Execute query and generate results
			statement 
				= createPreparedStatement(
					connection, 
					queryFormatter);
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			//there should be an entry for the study in rif40_studies.
			//However, it may have a blank value for the map table field value
			resultSet.next();
			String result = resultSet.getString(1);
			
			connection.commit();
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String studyName 
				= getStudyName(
					connection, 
					studyID);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetCalculatedResultsTableName",
					studyName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}		
	}


	/**
	 * This is a routine that provides a generic way of transforming rows
	 * returned by the JDBC call into rows that can be easily rendered by clients.
	 * This method needs to know how large the result set is going to be.  
	 * 
	 * <p>
	 * A common way of doing this is to move to the last row of the resultSet and
	 * obtain the row number.  However, this approach is slow.  
	 * It isn't clear how flexible this routine has to be.  If numberOfRows is 
	 * not null, then we will use this value to determine how many rows the result
	 * table should have. 
	 * @param resultSet
	 * @param tableName
	 * @param primaryKeyField
	 * @return
	 * @throws SQLException
	 */
	private RIFResultTable generateResultTable(
		final ResultSet resultSet)
		throws SQLException {
		
		RIFResultTable rifResultTable = new RIFResultTable();
		
		//Obtain the total number of rows
		int totalResultRows = 0;
		if (resultSet == null) {
			return rifResultTable;
		}		
		try {
			resultSet.last();
			totalResultRows = resultSet.getRow();
		}
		finally {
			resultSet.beforeFirst();
		}		
		
		//Obtain the column names
		ResultSetMetaData resultSetMetaData
			= resultSet.getMetaData();

		int numberOfColumns = resultSetMetaData.getColumnCount();
		String[] resultFieldNames = new String[numberOfColumns];
		for (int i = 0; i < resultFieldNames.length; i++) {
			resultFieldNames[i] = resultSetMetaData.getColumnName(i+1);
		}
		rifResultTable.setColumnProperties(resultFieldNames);
					
		String[][] resultsBlockData
			= new String[totalResultRows][resultFieldNames.length];
		
		int ithRow = 0;
		while (resultSet.next()) {
			for (int i = 1; i < resultFieldNames.length; i++) {
				resultsBlockData[ithRow][i] = resultSet.getString(i + 1);
			}			
			ithRow = ithRow + 1;
		}
		rifResultTable.setData(resultsBlockData);
		
		return rifResultTable;
	}
	
	
	private String getExtractTableName(
		final Connection connection,
		final String studyID) 
		throws RIFServiceException {
					
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String result = null;
		try {

			//Create query		
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.addSelectField("extract_table");
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addWhereParameter("study_id");
				
			logSQLQuery(
				"getExtractTableName",
				queryFormatter,
				studyID);
				
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			
			if (resultSet.next() == false) {
				/*
				 * In many of the queries in this class, we assume that 
				 * a query will generate a result.  This is not the case
				 * for getting the name of an extract table for a given study.
				 * A study may not have been run yet to produce a results table
				 * It is also the case that permissions problems prevent result
				 * tables from being generated.
				 */
				String studyName
					= getStudyName(
						connection,
						studyID);
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.unableToGetExtractTableName",
						studyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.UNABLE_TO_GET_EXTRACT_TABLE_NAME, 
						errorMessage);
				
				connection.commit();
				
				throw rifServiceException;
			}
			else {
				result = resultSet.getString(1);				
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
					"sqlResultsQueryManager.unableToGetExtractTableName");
			RIFServiceException rifServiceException	
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}
	}
	
	
	/**
	 * STUBBED
	 * @param connection
	 * @param user
	 * @param diseaseMappingStudy
	 * @return
	 * @throws RIFServiceException
	 */
	public String[] getGeometryColumnNames(
		final Connection connection,
		final User user,
		final DiseaseMappingStudy diseaseMappingStudy)
		throws RIFServiceException {
		
		//Validate parameters
		ValidationPolicy validationPolicy = getValidationPolicy();
		user.checkErrors();
		diseaseMappingStudy.checkErrors(validationPolicy);
		
		//Create query
		SQLGeneralQueryFormatter queryFormatter 
			= new SQLGeneralQueryFormatter();
		configureQueryFormatterForDB(queryFormatter);
		
		//KLG: TODO unfinished method
		logSQLQuery(
			"getGeometryColumnNames",
			queryFormatter,
			diseaseMappingStudy.getIdentifier());
		
		//Execute query and generate results
		String[] results = new String[0];
		//get the name of the calculated results table - ie the map table
		//KLG: TODO - do we need more parameters in the method here?
		String errorMessage
			= RIFServiceMessages.getMessage(
				"sqlResultsQueryManager.error.unableToGetGeometryColumnNames",
				diseaseMappingStudy.getDisplayName());
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, diseaseMappingStudy.getIdentifier());
			
			ArrayList<String> columnNames = new ArrayList<String>();
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				columnNames.add(resultSet.getString(1));
			}
			
			if (columnNames.isEmpty()) {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.UNABLE_TO_GET_GEOMETRY_COLUMN_NAMES, 
						errorMessage);
				throw rifServiceException;				
			}
			
			results = columnNames.toArray(new String[0]);
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}		
	}
		
	public RIFResultTable getPyramidData(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute) 
		throws RIFServiceException {
	
		//Validate parameters
		ValidationPolicy validationPolicy = getValidationPolicy();
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		geoLevelAttributeSource.checkErrors(validationPolicy);
		
		//check geoLevelAttributeSource exists
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource);
		
		//check geo level attribute exists
		checkGeoLevelAttributeExists(
			connection,
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttribute);

		
		
		/*
		 * @TODO - we need to develop this procedure in the DB
		 */
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		RIFResultTable result = new RIFResultTable();
		try {

			PGSQLFunctionCallerQueryFormatter queryFormatter
				= new PGSQLFunctionCallerQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		queryFormatter.setFunctionName("get_pyramid_data");
		queryFormatter.setNumberOfFunctionParameters(3);
		
		logSQLQuery(
			"getPyramidData",
			queryFormatter,
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource.getName(),
			geoLevelAttribute);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			resultSet = statement.executeQuery();
			result = generateResultTable(resultSet);
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace();
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.getPyramidData",
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getDisplayName(),
					geoLevelAttribute);
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

		return result;		
	}	
	
	public RIFResultTable getPyramidDataByYear(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute,
		final Integer year) 
		throws RIFServiceException {
				
		//Validate parameters
		ValidationPolicy validationPolicy = getValidationPolicy();
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		geoLevelAttributeSource.checkErrors(validationPolicy);
		
		//check geoLevelAttributeSource exists
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource);
		
		//check geo level attribute exists
		checkGeoLevelAttributeExists(
			connection,
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttribute);

		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		RIFResultTable result = new RIFResultTable();
		try {		
			/*
			 * @TODO - we need to develop this procedure in the DB
			 */

			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

			logSQLQuery(
				"getPyramidDataByYear",
				queryFormatter);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			

			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				//KLG: @TODO - fill in
			}
			
			connection.commit();
			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}
		
		return result;
	}
	
	public RIFResultTable getPyramidDataByMapAreas(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelToMap geoLevelToMap,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute,
		final ArrayList<MapArea> mapAreas) 
		throws RIFServiceException {

		//Validate parameters
		ValidationPolicy validationPolicy = getValidationPolicy();
		validateCommonParameters(
			connection,
			user,
			studyResultRetrievalContext);
		geoLevelToMap.checkErrors(validationPolicy);
		geoLevelAttributeSource.checkErrors(validationPolicy);

		for (MapArea mapArea : mapAreas) {
			mapArea.checkErrors(validationPolicy);
		}
		
		//check non-existent geo level to map
		sqlRIFContextManager.checkGeoLevelToMapOrViewValueExists(
			connection, 
			studyResultRetrievalContext.getGeographyName(), 
			studyResultRetrievalContext.getGeoLevelSelectName(),
			geoLevelToMap.getName(),
			true);
		
		//check non-existent map areas
		sqlMapDataManager.checkAreasExist(
			connection, 
			studyResultRetrievalContext.getGeographyName(), 
			geoLevelToMap.getName(), 
			mapAreas);
		
		//check geoLevelAttributeSource exists
		checkGeoLevelAttributeSourceExists(
			connection, 
			studyResultRetrievalContext.getStudyID(),
			geoLevelAttributeSource);
	
		//check geo level attribute exists
		checkGeoLevelAttributeExists(
			connection,
			studyResultRetrievalContext,
			geoLevelAttributeSource,
			geoLevelAttribute);

		//check map areas are valid
		for (MapArea mapArea : mapAreas) {
			mapArea.checkErrors(validationPolicy);
		}
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		RIFResultTable results = new RIFResultTable();
		try {
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

			logSQLQuery(
				"getPyramidDataByYear",
				queryFormatter);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			

			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				//KLG: @TODO - fill in
			}
			
			connection.commit();			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}
		
		return results;
	}
	
	public String[] getResultFieldsStratifiedByAgeGroup(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource)
		throws RIFServiceException {
	
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		String[] results = new String[0];
		try {
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		
			logSQLQuery(
				"getPyramidDataByYear",
				queryFormatter);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			

			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				//KLG: @TODO - fill in
			}
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
		}
		finally {
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}
		
		return results;
	}
	
	public RIFResultTable getSMRValues(
		final Connection connection,
		final User user,
		final StudySummary studySummary)
		throws RIFServiceException {
	
		ValidationPolicy validationPolicy = getValidationPolicy();
		studySummary.checkErrors(validationPolicy);
		String studyID = studySummary.getStudyID();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyID);
		
		
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		RIFResultTable results = new RIFResultTable();
		try {
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();		
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			resultSet = statement.executeQuery();
	
			//Obtain the column names
			ResultSetMetaData resultSetMetaData
				= resultSet.getMetaData();
			int numberOfColumns = resultSetMetaData.getColumnCount();
			String[] resultFieldNames = new String[numberOfColumns];
			for (int i = 0; i < resultFieldNames.length; i++) {
				resultFieldNames[i] = resultSetMetaData.getColumnName(i+1);
			}
			results.setColumnProperties(resultFieldNames);
			
			//Obtain the data
			int totalResultRows = 0;
			String[][] resultsBlockData
				= new String[totalResultRows][resultFieldNames.length];
			
			int ithRow = 0;
			while (resultSet.next()) {
				for (int i = 1; i < resultFieldNames.length; i++) {
					resultsBlockData[ithRow][i] = resultSet.getString(i + 1);
				}			
				ithRow = ithRow + 1;
			}
			results.setData(resultsBlockData);
			
			connection.commit();
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"");
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
	
	public RIFResultTable getRRValues(
		final Connection connection,
		final User user,
		final StudySummary studySummary)
		throws RIFServiceException {
				
		ValidationPolicy validationPolicy = getValidationPolicy();
		studySummary.checkErrors(validationPolicy);
		String studyID = studySummary.getStudyID();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyID);
		
	
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();		
		RIFResultTable results = new RIFResultTable();
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			resultSet = statement.executeQuery();

	
			//Obtain the column names
			ResultSetMetaData resultSetMetaData
				= resultSet.getMetaData();
			int numberOfColumns = resultSetMetaData.getColumnCount();
			String[] resultFieldNames = new String[numberOfColumns];
			for (int i = 0; i < resultFieldNames.length; i++) {
				resultFieldNames[i] = resultSetMetaData.getColumnName(i+1);
			}
			results.setColumnProperties(resultFieldNames);
			
			//Obtain the data
			int totalResultRows = 0;
			String[][] resultsBlockData
				= new String[totalResultRows][resultFieldNames.length];
			
			int ithRow = 0;
			while (resultSet.next()) {
				for (int i = 1; i < resultFieldNames.length; i++) {
					resultsBlockData[ithRow][i] = resultSet.getString(i + 1);
				}			
				ithRow = ithRow + 1;
			}
			results.setData(resultsBlockData);
		
			connection.commit();			
			return results;
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"");
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

	public RIFResultTable getRRUnadjustedValues(
		final Connection connection,
		final User user,
		final StudySummary studySummary)
		throws RIFServiceException {
				
		ValidationPolicy validationPolicy = getValidationPolicy();
		studySummary.checkErrors(validationPolicy);
		String studyID = studySummary.getStudyID();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyID);
		

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		RIFResultTable results = new RIFResultTable();
		try {
			SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();				
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			resultSet = statement.executeQuery();
	
			//Obtain the column names
			ResultSetMetaData resultSetMetaData
				= resultSet.getMetaData();
			int numberOfColumns = resultSetMetaData.getColumnCount();
			String[] resultFieldNames = new String[numberOfColumns];
			for (int i = 0; i < resultFieldNames.length; i++) {
				resultFieldNames[i] = resultSetMetaData.getColumnName(i+1);
			}
			results.setColumnProperties(resultFieldNames);
			
			//Obtain the data
			int totalResultRows = 0;
			String[][] resultsBlockData
				= new String[totalResultRows][resultFieldNames.length];
			
			int ithRow = 0;
			while (resultSet.next()) {
				for (int i = 1; i < resultFieldNames.length; i++) {
					resultsBlockData[ithRow][i] = resultSet.getString(i + 1);
				}			
				ithRow = ithRow + 1;
			}
			results.setData(resultsBlockData);
						
			connection.commit();
			
			return results;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"");
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
	
	public RIFResultTable getResultStudyGeneralInfo(
		final Connection connection,
		final User user,
		final StudySummary studySummary)
		throws RIFServiceException {
	
		ValidationPolicy validationPolicy = getValidationPolicy();
		studySummary.checkErrors(validationPolicy);
		String studyID = studySummary.getStudyID();
		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyID);
				
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();		
		

		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		RIFResultTable results = new RIFResultTable();		
		try {
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			resultSet
				= statement.executeQuery();
			
			
			
			
			
			connection.commit();
			return results;
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			//Record original exception, throw sanitised, human-readable version			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetResultStudyGeneralInfo");
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

	public ArrayList<AgeGroup> getResultAgeGroups(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelSourceAttribute)
		throws RIFServiceException {
		
		ValidationPolicy validationPolicy = getValidationPolicy();
		user.checkErrors();
		studyResultRetrievalContext.checkErrors(validationPolicy);
		geoLevelAttributeSource.checkErrors(validationPolicy);

		//@TODO: KLG - we need to implement this database method

		ArrayList<AgeGroup> results = new ArrayList<AgeGroup>();
		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				AgeGroup ageGroup = AgeGroup.newInstance();
				results.add(ageGroup);
			}
			
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			//Record original exception, throw sanitised, human-readable version			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToGetResultAgeGroups",
					studyResultRetrievalContext.getStudyID(),
					geoLevelAttributeSource.getName(),
					geoLevelSourceAttribute);
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
		
		
		return results;
	}
		
	public String getStudyName(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
		
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("study_name");
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addWhereParameter("study_id");
		
			logSQLQuery(
				"getStudyName",
				queryFormatter,
				studyID);
				
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setInt(
				1, 
				Integer.valueOf(studyID));
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
			
			String result = resultSet.getString(1);
			
			connection.commit();			
			return result;
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetStudyName",
					studyID);
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

	private String[][] getGeometryColumnNames(
		final Connection connection,
		final Geography geography) 
		throws RIFServiceException {
		
		ValidationPolicy validationPolicy = getValidationPolicy();
		geography.checkErrors(validationPolicy);

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String[][] results = null;
		try {
		
			PGSQLFunctionCallerQueryFormatter queryFormatter 
				= new PGSQLFunctionCallerQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
			queryFormatter.setFunctionName("rif40_getgeometrycolumnnames");
			queryFormatter.setNumberOfFunctionParameters(1);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setString(1, geography.getName());
			resultSet = statement.executeQuery();
			resultSet.last();			
			int numberOfResults = resultSet.getRow();

			resultSet.beforeFirst();
			results = new String[numberOfResults][2];
			for (int i = 0; i < results.length; i++) {
				resultSet.next();
				results[i][0] = resultSet.getString(1);
				results[i][1] = resultSet.getString(2);
			}
			
			connection.commit();
			return results;
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.getGeometryColumnNames",
					geography.getName());
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
		
	private int createMapAreaAttributeSource(
		final Connection connection,
		final String temporaryTableName,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect,
		final GeoLevelAttributeTheme geoLevelAttributeTheme,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String attributeNameArray) throws RIFServiceException {
		
		ValidationPolicy validationPolicy = getValidationPolicy();
		geography.checkErrors(validationPolicy);
		geoLevelSelect.checkErrors(validationPolicy);
		geoLevelAttributeTheme.checkErrors(validationPolicy);
		geoLevelAttributeSource.checkErrors(validationPolicy);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Integer result = null;
		try {

			PGSQLFunctionCallerQueryFormatter queryFormatter 
				= new PGSQLFunctionCallerQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
			queryFormatter.setFunctionName("rif40_createmapareaattributesource");		
			queryFormatter.setNumberOfFunctionParameters(6);
			
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setString(1, temporaryTableName);
			statement.setString(2, geography.getName());
			statement.setString(3, geoLevelSelect.getName());
			statement.setString(4, geoLevelAttributeTheme.getName());
			statement.setString(5, geoLevelAttributeSource.getName());
					
			result = statement.executeUpdate();
			
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.createMapAreaAttributeSource",
					temporaryTableName,
					geoLevelAttributeSource.getName());
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
		
		return result;
	}

	private void closeMapAreaAttributeCursor(
		final Connection connection,
		final String cursorName)
		throws RIFServiceException {
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			PGSQLFunctionCallerQueryFormatter queryFormatter 
				= new PGSQLFunctionCallerQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
			queryFormatter.setFunctionName("rif40_closegetmapareaattributecursor");		
			queryFormatter.setNumberOfFunctionParameters(1);
				
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, cursorName);
			statement.executeQuery();
			
			connection.commit();
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.closeMapAreaAttributeSource",
					cursorName);
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
	
	
	private void deleteMapAreaAttributeSource(
		final Connection connection,
		final String temporaryTableName) 
		throws RIFServiceException {
	
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			PGSQLFunctionCallerQueryFormatter queryFormatter 
				= new PGSQLFunctionCallerQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
			queryFormatter.setFunctionName("rif40_deletemapareaattributesource");		
			queryFormatter.setNumberOfFunctionParameters(1);		
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, temporaryTableName);
			statement.executeUpdate();
			
			connection.commit();			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.deleteMapAreaAttributeSource",
					temporaryTableName);
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
	// Section Errors and Validation
	// ==========================================
	private void validateCommonParameters(
		final Connection connection,
		final User user,
		final StudyResultRetrievalContext studyResultRetrievalContext)
		throws RIFServiceException {

		ValidationPolicy validationPolicy = getValidationPolicy();
		user.checkErrors();
		studyResultRetrievalContext.checkErrors(validationPolicy);		
		sqlRIFContextManager.checkGeographyExists(
			connection, 
			studyResultRetrievalContext.getGeographyName());
		
		sqlRIFContextManager.checkGeoLevelSelectExists(
			connection, 
			studyResultRetrievalContext.getGeographyName(),
			studyResultRetrievalContext.getGeoLevelSelectName());		

		sqlDiseaseMappingStudyManager.checkDiseaseMappingStudyExists(
			connection, 
			studyResultRetrievalContext.getStudyID());
	}

	
	private void validateCommonParameters(
		final Connection connection,
		final User user,
		final Geography geography,
		final GeoLevelSelect geoLevelSelect)
		throws RIFServiceException {
		
		ValidationPolicy validationPolicy = getValidationPolicy();
		user.checkErrors();
		geography.checkErrors(validationPolicy);	
		geoLevelSelect.checkErrors(validationPolicy);
		sqlRIFContextManager.checkGeographyExists(
			connection, 
			geography.getName());
		sqlRIFContextManager.checkGeoLevelSelectExists(
			connection, 
			geography.getName(),
			geoLevelSelect.getName());
	}
	
	
	
	
	/**
	 * Checks whether permissions associated with the table would
	 * prevent results tables from being generated.  We determine the
	 * permissions solely by looking at the value of the 'extract_permitted'
	 * field in the table rif40_studies.  We do *not* consider the value of
	 * 'transfer_permitted', which for now remains an information governance
	 * property that informs documentation rather than influences whether the
	 * middleware returns results or not.
	 * 
	 * <p>
	 * Assumes that the diseaseMappingStudy exists
	 * </p>
	 * @param connection
	 * @param diseaseMappingStudy
	 */
	private void checkPermissionsAllowResultsTable(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
	
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String studyName = "";
		try {
			PGSQLSelectQueryFormatter queryFormatter
				= new PGSQLSelectQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.addSelectField("extract_permitted");
			queryFormatter.addFromTable("rif40_studies");
			queryFormatter.addWhereParameter("study_id");
				
			studyName
				= getStudyName(
					connection, 
					studyID);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);
			statement.setInt(1, Integer.valueOf(studyID));
			resultSet = statement.executeQuery();
			//We can assume that the table will have an entry for the study
			resultSet.next();
			Boolean extractPermitted = resultSet.getBoolean(1);
			
			if (extractPermitted == null) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.unableToDetermineExtractPermissionForStudy",
						studyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.UNABLE_TO_DETERMINE_EXTRACT_PERMISSION_FOR_STUDY, 
						errorMessage);

				connection.commit();

				throw rifServiceException;				
			}
			else if (extractPermitted == false) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.studyDoesNotAllowExtraction",
						studyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.EXTRACT_NOT_PERMITTED_FOR_STUDY, 
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
					"sqlResultsQueryManager.error.unableToDetermineExtractPermissionForStudy",
					studyName);
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}		
	}

	
	private void validateTableFieldNames(
		final Connection connection, 
		final String tableName,
		final String[] tableFieldNames) 
		throws RIFServiceException {

		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
		
			PGSQLFunctionCallerQueryFormatter queryFormatter 
				= new PGSQLFunctionCallerQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setDatabaseSchemaName("rif40");
			queryFormatter.setFunctionName("table_field_exists");
			queryFormatter.setNumberOfFunctionParameters(2);
		
			FieldValidationUtility fieldValidationUtility
				= new FieldValidationUtility();		
			ArrayList<String> errorMessages = new ArrayList<String>();
			for (String tableFieldName : tableFieldNames) {
				if (fieldValidationUtility.isEmpty(tableFieldName)) {
					//field name is empty
					String errorMessage
						= RIFServiceMessages.getMessage(
							"sqlResultsQueryManager.error.emptyTableFieldName",
							tableName);
					errorMessages.add(errorMessage);
				}
			}

			if (errorMessages.size() > 0) {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.INVALID_TABLE_FIELD_NAMES,
						errorMessages);
				throw rifServiceException;
			}
		
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
						
			for (String tableFieldName : tableFieldNames) {
				//check if field exists in table
				statement.setString(1, tableName);
				statement.setString(2, tableFieldName);
					
				/**
				 * @TODO - we need a table function that does this
				resultSet = statement.executeQuery();
				resultSet.next();
				Boolean exists = resultSet.getBoolean(1);
				if (exists == false) {
					String errorMessage
						= RIFServiceMessages.getMessage(
							"sqlResultsQueryManager.error.nonExistentTableField",
							tableName,
							tableFieldName);
					errorMessages.add(errorMessage);
				}
				*/
			}

			connection.commit();
			
			if (errorMessages.size() > 0) {
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_TABLE_FIELD_NAME,
						errorMessages);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {			
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			PGSQLQueryUtility.rollback(connection);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckTableFieldExists",
					tableName);
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
	
	private void checkResultsTableExists(
		final Connection connection,
		final String studyID,
		final String resultsTableName)
		throws RIFServiceException {
		
		//Execute query and generate results
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String studyName = "";
		try {			
			//Create query/
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			
			studyName
				= getStudyName(
					connection, 
					studyID);
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, studyID);
			resultSet 
				= statement.executeQuery();
			if (resultSet.next() == false) {
				//the query did not return a result
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentResultTable",
						resultsTableName,
						studyName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_RESULT_TABLE, 
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
					"sqlResultsQueryManager.error.unableToCheckResultTableExists",
					studyName);
			RIFServiceException rifServiceException
				 = new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}		
	}
	
	private void checkResultsTableFieldExists(
		final Connection connection,
		final String studyID,
		final String resultsTableName,
		final String resultsTableFieldName)
		throws RIFServiceException {

		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLRecordExistsQueryFormatter queryFormatter
				= new PGSQLRecordExistsQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);		
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			resultSet 
				= statement.executeQuery();
			if (resultSet.next() == false) {
				//the query did not return a result
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentResultTableField",
						resultsTableFieldName,
						resultsTableName,
						studyID);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_RESULT_TABLE_FIELD_NAME, 
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
			String studyName
				= getStudyName(
					connection, 
					studyID);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckResultTableFieldExists",
					resultsTableFieldName,
					resultsTableName,
					studyName);
			RIFServiceException rifServiceException	
				= new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED, 
					errorMessage);
			throw rifServiceException;
		}
		finally {
			//Cleanup database resources
			PGSQLQueryUtility.close(statement);
			PGSQLQueryUtility.close(resultSet);			
		}		
	}
	
	
	//Check what query we need
	private void checkMapAreaExists(
		final Connection connection, 
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final MapArea mapArea) 
		throws RIFServiceException {
		
				
		//Use geo level select to determine the correct
		//resolution lookup table.
	
		//KLG: @TODO - need to implement this method
		//should this check be one that checks if a map area exists in
		//the geography or that it is part of the study?
		
		/**
		String geographyTable = "sahsuland_geography";
		
		
		SQLRecordExistsQueryFormatter query
			= new SQLRecordExistsQueryFormatter();
		query.setFromTable(geographyTable);
		query.
		query.addWhereParameter(geoLevelSelect.getName());
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(1, mapArea.getIdentifier());
			resultSet = statement.executeQuery();
		
			if (resultSet.next() == false) {
				//the query did not return a result
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentMapArea",
						mapArea.getDisplayName(),
						geography.getDisplayName(),
						geoLevelSelect.getDisplayName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_MAP_AREA, 
						errorMessage);
				throw rifServiceException;
			}			
		}
		catch(SQLException sqlException) {
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckMapAreaExists",
					mapArea.getDisplayName(),
					geography.getDisplayName(),
					geoLevelSelect.getDisplayName());
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
		
		*/
	}

	/**
	 * @TODO - This check determines whether the map area appears
	 * within the map table of the study
	 * @param connection
	 * @param studyResultRetrievalContext
	 * @param mapArea
	 * @throws RIFServiceException
	 */
	private void checkMapAreaExistsInStudy(
		final Connection connection, 
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final MapArea mapArea) 
		throws RIFServiceException {

		
		//@TODO
		
	}
	
	//CHECKED -- find out the name of the function
	private void checkGeoLevelAttributeExists(
		final Connection connection,
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final String geoLevelAttribute) 
		throws RIFServiceException {
		
		/*
		SQLFunctionCallerQueryFormatter query
			= new SQLFunctionCallerQueryFormatter();
		query.setDatabaseSchemaName("rif40_xml_pkg");
		query.setFunctionName("geo_level_attribute_exists");
		query.setNumberOfFunctionParameters(3);
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {			
			
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(
				1, 
				studyResultRetrievalContext.getGeographyName());
			statement.setString(
				2, 
				studyResultRetrievalContext.getGeoLevelSelectName());
			statement.setString(
				3, 
				geoLevelAttribute);
			resultSet = statement.executeQuery();
			resultSet.next();
			Boolean geoLevelSelectExists = resultSet.getBoolean(1);
			if (geoLevelSelectExists == false) {
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentGeoLevelAttribute",
						geoLevelAttribute,
						studyResultRetrievalContext.getGeographyName(),
						studyResultRetrievalContext.getGeoLevelSelectName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEO_LEVEL_ATTRIBUTE,
						errorMessage);
				throw rifServiceException;
			}
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace();
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckGeoLevelAttributeExists",
					geoLevelAttribute,
					studyResultRetrievalContext.getGeographyName(),
					studyResultRetrievalContext.getGeoLevelSelectName());
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
		*/
	}

	private void checkGeoLevelAttributeSourceExists(
		final Connection connection,
		final String studyID,
		final GeoLevelAttributeSource geoLevelAttributeSource)
		throws RIFServiceException {
		
	}
	
	private void checkGeoLevelAttributeThemeExists(
		final Connection connection, 
		final StudyResultRetrievalContext studyResultRetrievalContext,
		final GeoLevelAttributeSource geoLevelAttributeSource,
		final GeoLevelAttributeTheme geoLevelAttributeTheme)
		throws RIFServiceException {

		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			PGSQLFunctionCallerQueryFormatter queryFormatter
				= new PGSQLFunctionCallerQueryFormatter();
			configureQueryFormatterForDB(queryFormatter);
			queryFormatter.setUseDistinct(true);
			queryFormatter.addSelectField("theme");
			queryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
			queryFormatter.setFunctionName("rif40_getAllAttributesForGeoLevelAttributeTheme");
			queryFormatter.setNumberOfFunctionParameters(3);
			queryFormatter.addWhereParameter("attribute_source");
			logSQLQuery(
				"checkGeoLevelAttributeThemeExists1", 
				queryFormatter, 
				studyResultRetrievalContext.getGeographyName(),
				studyResultRetrievalContext.getGeoLevelSelectName(),
				geoLevelAttributeTheme.getName(),
				geoLevelAttributeSource.getName());
						
			statement 
				= createPreparedStatement(
					connection,
					queryFormatter);			
			statement.setString(
				1, 
				studyResultRetrievalContext.getGeographyName());
			statement.setString(
				2, 
				studyResultRetrievalContext.getGeoLevelSelectName());
			statement.setString(
				3, 
				geoLevelAttributeTheme.getName());
			statement.setString(
				4, 
				geoLevelAttributeSource.getName());
			resultSet = statement.executeQuery();
			
			if (resultSet.next() == false) {
				//Error doesn't exist
				String errorMessage
					= RIFServiceMessages.getMessage(
						"sqlResultsQueryManager.error.nonExistentGeoLevelAttributeTheme",
						geoLevelAttributeTheme.getDisplayName(),
						studyResultRetrievalContext.getGeographyName(),
						studyResultRetrievalContext.getGeoLevelSelectName());
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.NON_EXISTENT_GEO_LEVEL_ATTRIBUTE_THEME,
						errorMessage);
				
				connection.commit();
				
				throw rifServiceException;
			}
						
			connection.commit();
			
		}
		catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version			
			logSQLException(sqlException);
			String errorMessage
				= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.error.unableToCheckGeoLevelAttributeThemeExists",
					geoLevelAttributeTheme.getDisplayName(),
					geoLevelAttributeSource.getDisplayName());
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
