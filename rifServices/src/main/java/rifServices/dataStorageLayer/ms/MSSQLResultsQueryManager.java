package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifGenericLibrary.dataStorageLayer.ms.MSSQLFunctionCallerQueryFormatter;
import rifGenericLibrary.dataStorageLayer.ms.MSSQLQueryUtility;
import rifGenericLibrary.dataStorageLayer.ms.MSSQLSelectQueryFormatter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.*;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.util.RIFLogger;

import java.sql.*;

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

final class MSSQLResultsQueryManager extends MSSQLAbstractSQLManager {

	// ==========================================
	// Section Constants
	// ==========================================
	RIFLogger rifLogger = RIFLogger.getLogger();

	// ==========================================
	// Section Properties
	// ==========================================

	private MSSQLFunctionCallerQueryFormatter getTilesQueryFormatter;
	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLResultsQueryManager(
		final RIFDatabaseProperties rifDatabaseProperties,
		final MSSQLRIFContextManager sqlRIFContextManager,
		final MSSQLMapDataManager sqlMapDataManager,
		final MSSQLDiseaseMappingStudyManager sqlDiseaseMappingStudyManager) {
		
		super(rifDatabaseProperties);

		getTilesQueryFormatter
			= new MSSQLFunctionCallerQueryFormatter(false);
		configureQueryFormatterForDB(getTilesQueryFormatter);
		getTilesQueryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		getTilesQueryFormatter.setFunctionName("rif40_get_geojson_tiles");
		getTilesQueryFormatter.setNumberOfFunctionParameters(9);		
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public RIFResultTable getTileMakerCentroids(
			final Connection connection,
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect)
			throws RIFServiceException {
								
			MSSQLSelectQueryFormatter getMapTileTableQueryFormatter
				= new MSSQLSelectQueryFormatter(false);		
				
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
				MSSQLQueryUtility.close(statement);
				MSSQLQueryUtility.close(resultSet);
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
		/*
		SELECT tiletable 
		FROM [sahsuland_dev].[rif40].[rif40_geographies]
		WHERE geography = 'SAHSULAND';
		*/
							
		MSSQLSelectQueryFormatter getMapTileTableQueryFormatter
			= new MSSQLSelectQueryFormatter(false);		
			
		getMapTileTableQueryFormatter.setDatabaseSchemaName("rif40");
		getMapTileTableQueryFormatter.addSelectField("rif40_geographies", "tiletable");
		getMapTileTableQueryFormatter.addFromTable("rif40_geographies");
		getMapTileTableQueryFormatter.addWhereParameter("geography");
						
		logSQLQuery(
			"getTileMakerTiles",
			getMapTileTableQueryFormatter,
			geography.getName().toUpperCase());	
			
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
			String myTileTable = "rif_data." + resultSet.getString(1);
					
			MSSQLSelectQueryFormatter getMapTilesQueryFormatter
				= new MSSQLSelectQueryFormatter(false);
				
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
			getMapTilesQueryFormatter.addFromTable("rif40.rif40_geolevels");	
			getMapTilesQueryFormatter.addWhereJoinCondition(myTileTable, "geolevel_id", "rif40.rif40_geolevels", "geolevel_id");
			getMapTilesQueryFormatter.addWhereParameter("rif40.rif40_geolevels", "geolevel_name");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "zoomlevel");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "x");
			getMapTilesQueryFormatter.addWhereParameter(myTileTable, "y");
			
			logSQLQuery(
				"getTileMakerTiles",
				getMapTilesQueryFormatter,
				geoLevelSelect.getName().toUpperCase(),
				zoomlevel.toString(),
				x.toString(),
				y.toString());
				
			statement2 = connection.prepareStatement(getMapTilesQueryFormatter.generateQuery());	
			statement2.setString(1, geoLevelSelect.getName().toUpperCase());
			statement2.setInt(2, zoomlevel);
			statement2.setInt(3, x);
			statement2.setInt(4, y);
					
			resultSet2 = statement2.executeQuery();
			resultSet2.next();
			String result = resultSet2.getString(1);

			rifLogger.info(getClass(), "get tile for geogrpahy: " + geography.getName().toUpperCase() +
				"; tileTable: " + myTileTable + 
				"; zoomlevel: " + geoLevelSelect.getName().toUpperCase() + " x/y: " + x + "/" + y +
				"; length: " + result.length());
			
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
			MSSQLQueryUtility.close(statement);
			MSSQLQueryUtility.close(resultSet);
			
			MSSQLQueryUtility.close(statement2);
			MSSQLQueryUtility.close(resultSet2);
		}			
	}
					
	public String getStudyName(
		final Connection connection,
		final String studyID)
		throws RIFServiceException {
				
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
		
			MSSQLSelectQueryFormatter queryFormatter
				= new MSSQLSelectQueryFormatter(false);
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
			MSSQLQueryUtility.rollback(connection);
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
			MSSQLQueryUtility.close(statement);
			MSSQLQueryUtility.close(resultSet);
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
