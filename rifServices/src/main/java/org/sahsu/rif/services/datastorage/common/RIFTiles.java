package org.sahsu.rif.services.datastorage.common;

import org.apache.commons.collections.IteratorUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sahsu.rif.generic.datastorage.RIFSQLException;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLSelectQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.datastorage.common.BaseSQLManager;
import org.sahsu.rif.services.datastorage.common.RIFTilesCache;
import org.sahsu.rif.services.datastorage.common.RifWellKnownText;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.graphics.SlippyTile;
import org.sahsu.rif.services.graphics.RIFPdfTiles;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Create PNG tiles for geolevels with more than 5000 areas.
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
public class RIFTiles  extends BaseSQLManager {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	// --Commented out by Inspection (10/12/2018 18:38):private static final String lineSeparator = System.getProperty("line.separator");

	private static final String NULL_GEOJSON_TILE="{\"features\":[],\"type\":\"FeatureCollection\"}\";";
	private static final String NULL_TOPOJSON_TILE="{\"type\": \"FeatureCollection\",\"features\":[]}";
	 
	private static RifWellKnownText rifWellKnownText = null;
	private static RIFTilesCache rifTilesCache = null;
	private static RIFPdfTiles rifPdfTiles = null;
	
	/**
	 * Constructor
	 *
	 * @param options RIFServiceStartupOptions for the extract directory
	 *
	 * @throws NullPointerException on Constructor failure
	 */
	public RIFTiles(final RIFServiceStartupOptions options) {
		super(options);	
		try {		
			rifWellKnownText = new RifWellKnownText();
			rifTilesCache = new RIFTilesCache(options);
			rifPdfTiles = new RIFPdfTiles(options);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFTiles() constructor");
			throw new NullPointerException();
		}
	}

	/** 
	 * Convert TopoJSON to GeoJSON
	 * <p>	 
	 * [Store result in <myTileTable>.optimised_geojson is the column exists and is NOT NULL] - Maybe
	 * As there is no Java way of converting TopoJSON to GeoJSON like topojson.feature(topology, object)
	 * https://github.com/topojson/topojson-client/blob/master/README.md#feature
	 * Various Java libraries are immature, unmaintained and undocumented.
	 * GDAL can convert, but again it is not documented, especially GDAL Java.
	 *
	 * The chosen method here is to populate <myTileTable>.optimised_geojson by parsing the area_ids from
	 * the topoJSON properties and then fetching the GeoJSON from <myGeometryTable>.
	 *
	 * This will eventually be done by the tileMaker.
	 *
	 * Called from: ResultsQueryManager.java; needs to determine minZoomlevel and maxZoomlevel
	 * </p>
	 *
	 * @param connection			JDBC Connection
	 * @param tileTopoJson			JSONObject of tile GeoJSON
	 * @param geography				Uppercase String
	 * @param slippyTile 			SlippyTile (zoomlevel, x, y)
	 * @param hmap 					HashMap<String, String>; hashes are "topoJSON", "tileTable" and "geometryTable"
	 * @param geoLevel 				Uppercase String
	 * @param addBoundingBoxToTile 	Add bounding box as an additional feature for debug purposes
	 *
	 * @return GeoJSON as JSONObject
	 *
	 * @throws RIFServiceException RIF error
	 * @throws RIFSQLException RIF SQL error
	 * @throws JSONException Error manipulating JSON
     */		
	 	public JSONObject topoJson2geoJson(
		final Connection connection,
		final JSONObject tileTopoJson, 
		final String geography,
		final SlippyTile slippyTile, 
		final HashMap<String, String> hmap,
		final String geoLevel, 
		final boolean addBoundingBoxToTile)
			throws RIFSQLException, JSONException, RIFServiceException, SQLException
	{
		String myGeometryTable = hmap.get("geometryTable");
		PreparedStatement statement2 = null;
		ResultSet resultSet2 = null;
		
		SelectQueryFormatter generateTilesForGeoLevelQueryFormatter2
				= new MSSQLSelectQueryFormatter();	
				
		//Get the min and max zoomlevel in the geometry table
		/*
		SELECT MIN(zoomlevel) AS min_zoomlevel, MAX(zoomlevel) AS max_zoomlevel
		  FROM rif_data.geometry_ews2011, rif_data.geometry_ews2011
		 WHERE rif_data.geometry_ews2011.geolevel_id = rif40.rif40_geolevels.geolevel_id
		   AND rif40.rif40_geolevels.geolevel_name = 'COA2011';
		  */
		
		generateTilesForGeoLevelQueryFormatter2.addSelectField("MIN(zoomlevel) AS min_zoomlevel");
		generateTilesForGeoLevelQueryFormatter2.addSelectField("MAX(zoomlevel) AS max_zoomlevel");
		generateTilesForGeoLevelQueryFormatter2.addFromTable("rif_data." + myGeometryTable.toLowerCase());
		generateTilesForGeoLevelQueryFormatter2.addFromTable("rif40.rif40_geolevels");
		generateTilesForGeoLevelQueryFormatter2.addWhereJoinCondition("rif_data." + myGeometryTable, 
			"geolevel_id", "rif40.rif40_geolevels", "geolevel_id");
		generateTilesForGeoLevelQueryFormatter2.addWhereParameter(
			applySchemaPrefixIfNeeded("rif40_geolevels"),
			"geolevel_name");
		
		String sqlQueryText = logSQLQuery(
				"topoJson2geoJson",
				generateTilesForGeoLevelQueryFormatter2,
				geoLevel);	

		JSONObject geoJSON;
		try {
			statement2 = connection.prepareStatement(generateTilesForGeoLevelQueryFormatter2.generateQuery());
			statement2.setString(1, geoLevel);
			
			resultSet2 = statement2.executeQuery();
			resultSet2.next();
			int minZoomlevel=resultSet2.getInt(1);	
			int maxZoomlevel=resultSet2.getInt(2);			
		
			geoJSON = topoJson2geoJson(connection, 
					tileTopoJson,
					geography, slippyTile, hmap, geoLevel, addBoundingBoxToTile,
					minZoomlevel, maxZoomlevel);
		}	
		catch (SQLException sqlException) {
			throw new RIFSQLException(this.getClass(), sqlException, statement2, sqlQueryText);
		}
		finally { //Cleanup database resources
			connection.commit();
			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}	
		
		return geoJSON;
	}

	/** 
	 * Get well Known Text from RIF database <myGeometryTable> for geoLevel, zoomlevel and list of areaIds in tile
     *
	 * @param connection 		Database JDBC Connection object	
	 * @param zoomlevel 		0-9 (depends in TileMaker maximum zoomlevel)
	 * @param geoLevel 			Uppercase String	 
	 * @param myGeometryTable 	Uppercase String	 
	 * @param areaIdList		ArrayList<String> of areaIds
	 * @param minZoomlevel		Minimum zoomlevel
	 * @param maxZoomlevel		Maximum zoomlevel
	 *
	 * @return HashMap<String, String> of areaID and Well known text
	 *
	 * @throws RIFServiceException RIF error
	 * @throws SQLException logical database error
     */	
	private HashMap<String, String> getWKT(
		final Connection connection,
		final Integer zoomlevel,
		final String geoLevel,
		final String myGeometryTable,
		final ArrayList<String> areaIdList,
		final int minZoomlevel,
		final int maxZoomlevel
		) throws RIFServiceException, SQLException {
			
		HashMap<String, String> result = new HashMap<>();
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		SelectQueryFormatter getMapTilesQueryFormatter
				= new MSSQLSelectQueryFormatter();

		//STEP 2: get the tiles
		/*
			SELECT
			   ST_AsGeoJson(GEOMETRY_SAHSULAND.optimised_topojson) AS geojson
			FROM
			   GEOMETRY_SAHSULAND,
			   rif40_geolevels
			WHERE
			   GEOMETRY_SAHSULAND.geolevel_id = rif40_geolevels.geolevel_id AND
			   rif40_geolevels.geolevel_name='SAHSU_GRD_LEVEL2' AND
			   GEOMETRY_SAHSULAND.zoomlevel=10 AND
			   GEOMETRY_SAHSULAND.areaid='01.012' 
			   
			   wkt?
		*/

		getMapTilesQueryFormatter.addSelectField("rif_data." + myGeometryTable, "wkt");
		getMapTilesQueryFormatter.addSelectField("rif_data." + myGeometryTable, "areaid");
		getMapTilesQueryFormatter.addFromTable("rif_data." + myGeometryTable);
		getMapTilesQueryFormatter.addFromTable("rif40.rif40_geolevels");
		getMapTilesQueryFormatter.addWhereJoinCondition("rif_data." + myGeometryTable, 
			"geolevel_id", "rif40.rif40_geolevels", "geolevel_id");
		getMapTilesQueryFormatter.addWhereParameter(
			applySchemaPrefixIfNeeded("rif40_geolevels"),
			"geolevel_name");
		getMapTilesQueryFormatter.addWhereParameter("rif_data." + myGeometryTable, "zoomlevel");
		getMapTilesQueryFormatter.addWhereIn("rif_data." + myGeometryTable, "areaid", areaIdList);

		String sqlQueryText = logSQLQuery(
				"topoJson2geoJson",
				getMapTilesQueryFormatter,
				geoLevel,
				zoomlevel.toString());

		try {
			statement = connection.prepareStatement(getMapTilesQueryFormatter.generateQuery());
			statement.setString(1, geoLevel);
			if (zoomlevel >= minZoomlevel && zoomlevel <= maxZoomlevel) {
				statement.setInt(2, zoomlevel);
			}
			else if (zoomlevel < minZoomlevel) {
				statement.setInt(2, minZoomlevel);
			}	
			else { // zoomlevel > maxZoomlevel
				   // minZoomLevel and maxZoomLevel are the limits of the Geometry lookup table
				   // minZoomLevel is set by the tile Maker and usually 6, and maxZoomLevel 9. Zoomlevel can
				   // be from 0 to 22, but is limited in the rest validation  to 11 (i.e. IntelliJ is ambiguous)
				statement.setInt(2, maxZoomlevel);
			}	
			
			resultSet = statement.executeQuery();
			while (resultSet.next()) {

				String wkt=resultSet.getString(1);
				String areaId=resultSet.getString(2);	
				result.put(areaId, wkt);
			}		
			if (result.size() != areaIdList.size()) {
				throw new SQLException("Error in WKT fetch; expecting: " + areaIdList.size() + "; got: " + result.size());
			}
		}
		catch (SQLException sqlException) {
			throw new RIFSQLException(this.getClass(), sqlException, statement, sqlQueryText);
		}
		finally { //Cleanup database resources
				connection.commit();
				SQLQueryUtility.close(statement);
				SQLQueryUtility.close(resultSet);

		}
		return result;
	}

	/** 
	 * Get topoJSON tile
	 *
	 * @param connection 		Database JDBC Connection object	
	 * @param geography 		Geography string in uppercase
	 * @param geoLevelSelect 	Geolevel string in uppercase
	 * @param slippyTile 		SlippyTile (zoomlevel, x, y)
     */	
	// Duplicate code in generateTilesForGeoLevel()
	public HashMap<String, String> getTopoJsonTile(
			final Connection connection, 
			final String geography,
			final String geoLevelSelect, 
			final SlippyTile slippyTile) 
			throws RIFServiceException, SQLException {

		HashMap<String, String> hmap = new HashMap<String, String>();

		//STEP 1: get the tile table name
		/*
		SELECT tiletable, geometrytable
		FROM [sahsuland_dev].[rif40].[rif40_geographies]
		WHERE geography = 'SAHSULAND';
		*/

		SelectQueryFormatter getMapTileTableQueryFormatter =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getMapTileTableQueryFormatter.setDatabaseSchemaName(SCHEMA_NAME);
		getMapTileTableQueryFormatter.addSelectField("rif40_geographies", "tiletable");
		getMapTileTableQueryFormatter.addSelectField("rif40_geographies", "geometrytable");
		getMapTileTableQueryFormatter.addFromTable("rif40_geographies");
		getMapTileTableQueryFormatter.addWhereParameter("geography");

		String sqlQueryText = logSQLQuery(
				"getTileMakerTiles",
				getMapTileTableQueryFormatter,
				geography);
		if (sqlQueryText == null) {
			throw new SQLException("sqlQueryText is null");
		}
		//For tile table name
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String tileTable = null;
		String geometryTable = null;
		try {
			String sqlStmt=getMapTileTableQueryFormatter.generateQuery();
			if (sqlStmt == null) {
				throw new SQLException("getMapTileTableQueryFormatter.generateQuery() returned null for: " + sqlQueryText);
			}
			statement = connection.prepareStatement(sqlStmt);
			statement.setString(1, geography);

			resultSet = statement.executeQuery();
			resultSet.next();

			tileTable = resultSet.getString(1);
			geometryTable = resultSet.getString(2);
			hmap.put("tileTable", tileTable);
			hmap.put("geometryTable", geometryTable);
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			throw new RIFSQLException(this.getClass(), sqlException, statement, sqlQueryText);
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
		
		connection.commit();
		
		getTopoJsonTileFromDb(connection, geoLevelSelect, slippyTile, hmap);

		return hmap;
	}

	/** 
	 * Generate tiles for geolevels with more than 5000 areaIds
	 *
	 * <p>
	 * Called from RIFTilesGenerator on RIF Services middleware start
	 * </p>
	 *
	 * @param connection 		Database JDBC Connection object	
     */		
	public void generateTiles(Connection connection) 
		throws RIFServiceException, SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		LocalDateTime start = LocalDateTime.now(); 
		int generatedCount=0;
		int geolevelCount=0;
		HashMap<String, String> hmap = new HashMap<String, String>();

		SelectQueryFormatter generateTilesQueryFormatter
				= new MSSQLSelectQueryFormatter();

		//STEP 1: get the geolevels with more than 5000 areaIds
		/*
			SELECT a.geography, a.geolevel_id, a.geolevel_name, a.description AS geolevel_description, a.areaid_count, b.tiletable
			  FROM rif40.rif40_geolevels a, rif40.rif40_geographies b
			 WHERE a.geography = b.geography
               AND a.areaid_count > 5000
             ORDER BY a.areaid_count DESC;
		*/

		generateTilesQueryFormatter.addSelectField("rif40.rif40_geolevels", "geography");
		generateTilesQueryFormatter.addSelectField("rif40.rif40_geolevels", "geolevel_id");
		generateTilesQueryFormatter.addSelectField("rif40.rif40_geolevels", "geolevel_name");
		generateTilesQueryFormatter.addSelectField("rif40.rif40_geographies", "tiletable");
		generateTilesQueryFormatter.addSelectField("rif40.rif40_geographies", "geometrytable");
		generateTilesQueryFormatter.addFromTable("rif40.rif40_geolevels");
		generateTilesQueryFormatter.addFromTable("rif40.rif40_geographies");
		generateTilesQueryFormatter.addWhereJoinCondition("rif40.rif40_geolevels", 
			"geography", "rif40.rif40_geographies", "geography");
		generateTilesQueryFormatter.addWhereParameterWithOperator("rif40.rif40_geolevels.areaid_count", ">=");
		generateTilesQueryFormatter.addOrderByCondition("areaid_count", SelectQueryFormatter.SortOrder.ASCENDING);

		int minAreaIdCount = 5000; // ItelliJ is wrong will not compile!
		String sqlQueryText = logSQLQuery(
				"generateTiles",
				generateTilesQueryFormatter,
				String.valueOf(minAreaIdCount));

		try {
			statement = connection.prepareStatement(generateTilesQueryFormatter.generateQuery());
			statement.setInt(1, minAreaIdCount); 
			resultSet = statement.executeQuery();
			while (resultSet.next()) {

				String geography=resultSet.getString(1);
				int geolevelId=resultSet.getInt(2); // XXXX
				String geolevelName=resultSet.getString(3);	
				String tileTable=resultSet.getString(4);		
				String geometryTable=resultSet.getString(5);
	 			hmap.put("tileTable", tileTable);
				hmap.put("geometryTable", geometryTable);
				geolevelCount++;
				generatedCount+=determineTilesForGeoLevel(connection, geolevelId, hmap, geography, geolevelName);
			}	
			
			LocalDateTime end = LocalDateTime.now(); 
			Duration duration = Duration.between(start, end);
			rifLogger.info(getClass(), "Generated " + generatedCount +
				" tiles for: " + geolevelCount + " geolevels in " + formatDuration(duration));
		}
		catch (SQLException sqlException) {
			LocalDateTime end = LocalDateTime.now(); 
			Duration duration = Duration.between(start, end);
			rifLogger.warning(getClass(), "generateTiles() had error: Generated " + generatedCount +
				" tiles for: " + geolevelCount + " geolevels in " + formatDuration(duration));

			throw new RIFSQLException(this.getClass(), sqlException, statement, sqlQueryText);
		}
		finally { //Cleanup database resources
			connection.commit();
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}			
	}

	/** 
	 * Format Duration
	 *
	 * @param duration Duration Object
	 *
	 * @return Duration formatted as HH:MM:SS 
     */	 
	private static String formatDuration(Duration duration) {
		long seconds = duration.getSeconds();
		long absSeconds = Math.abs(seconds);
		int nanoSeconds = duration.getNano();
		String positive = String.format(
			"%02d:%02d:%02d.%03d",
			absSeconds / 3600,
			(absSeconds % 3600) / 60,
			absSeconds % 60,
			(nanoSeconds / 1000000));
		return seconds < 0 ? "-" + positive : positive;
	}

	/** 
	 * Determine how many tiles to generate for geolevel
	 *
	 * @param connection 		Database JDBC Connection object	
	 * @param geolevelId 		Database geolevel ID
	 * @param hmap 				HashMap<String, String>; hashes are "topoJSON", "tileTable" and "geometryTable"
	 * @param geography			Uppercase String
	 * @param geolevelName 		Name of geolevel
	 *
	 * @return Number of tiles generated
     */	
	private int determineTilesForGeoLevel(
		final Connection connection, 
		final int geolevelId, 
		final HashMap<String, String> hmap,
		final String geography, 
		final String geolevelName)
			throws RIFServiceException, SQLException {

		String tileTable = hmap.get("tileTable");
		String geometryTable = hmap.get("geometryTable");
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet resultSet = null;
		ResultSet resultSet2 = null;
		int generatedCount;
		
		SelectQueryFormatter generateTilesForGeoLevelQueryFormatter
				= new MSSQLSelectQueryFormatter();
		SelectQueryFormatter generateTilesForGeoLevelQueryFormatter2
				= new MSSQLSelectQueryFormatter();

		//STEP 2: get the total tiles and areaIds from the tiles table
		/*
			SELECT COUNT(tile_id) AS tiles, SUM(areaid_count) AS areaid_count
			  FROM rif_data.t_tiles_sahsuland
			 WHERE geolevel_id = 4;
		*/

		generateTilesForGeoLevelQueryFormatter.addSelectField("COUNT(tile_id) AS tile_count");
		generateTilesForGeoLevelQueryFormatter.addSelectField("SUM(areaid_count) AS areaid_count");
		generateTilesForGeoLevelQueryFormatter.addFromTable("rif_data.t_" + tileTable.toLowerCase());
		generateTilesForGeoLevelQueryFormatter.addWhereParameter("rif_data.t_" + tileTable.toLowerCase(), "geolevel_id");

		//STEP 3: get the min and max zoomlevel in the geometry table
		/*
		SELECT MIN(zoomlevel) AS min_zoomlevel, MAX(zoomlevel) AS max_zoomlevel
		  FROM rif_data.geometry_ews2011
		 WHERE rif_data.geometry_ews2011.geolevel_id = 4;
		  */
		  
		generateTilesForGeoLevelQueryFormatter2.addSelectField("MIN(zoomlevel) AS min_zoomlevel");
		generateTilesForGeoLevelQueryFormatter2.addSelectField("MAX(zoomlevel) AS max_zoomlevel");
		generateTilesForGeoLevelQueryFormatter2.addFromTable("rif_data." + geometryTable.toLowerCase());
		generateTilesForGeoLevelQueryFormatter2.addWhereParameter("rif_data." + geometryTable.toLowerCase(), "geolevel_id");
		
		String sqlQueryText = logSQLQuery(
				"generateTiles",
				generateTilesForGeoLevelQueryFormatter,
				String.valueOf(geolevelId));	
		int tileCount;
		int areaidCount;
		try {
			statement = connection.prepareStatement(generateTilesForGeoLevelQueryFormatter.generateQuery());
			statement.setInt(1, geolevelId);
			resultSet = statement.executeQuery();
			resultSet.next();
			tileCount=resultSet.getInt(1);	
			areaidCount=resultSet.getInt(2);
		}
		catch (SQLException sqlException) {
			throw new RIFSQLException(this.getClass(), sqlException, statement, sqlQueryText);
		}
		finally { //Cleanup database resources
			connection.commit();
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}

		String sqlQueryText2 = logSQLQuery(
				"generateTiles",
				generateTilesForGeoLevelQueryFormatter2,
				String.valueOf(geolevelId));
				
		try {
			statement2 = connection.prepareStatement(generateTilesForGeoLevelQueryFormatter2.generateQuery());
			statement2.setInt(1, geolevelId);
			resultSet2 = statement2.executeQuery();
			resultSet2.next();	
			int minZoomlevel=resultSet2.getInt(1);	
			int maxZoomlevel=resultSet2.getInt(2);	
			
			rifLogger.info(getClass(), 
				"Generating up to " + tileCount + " tiles for: " + areaidCount + " areas in: " + geography + "." + geolevelName +
				"; minZoomlevel: " + minZoomlevel + "; maxZoomlevel: " + maxZoomlevel);
			generatedCount=generateTilesForGeoLevel(
				connection, 
				geolevelId, 
				hmap,
				geography, 
				geolevelName, 
				tileCount,
				minZoomlevel,
				maxZoomlevel);
			rifLogger.info(getClass(), "Generated " + generatedCount + "/" + tileCount + 
				" tiles for: " + areaidCount + " areas in: " + geography + "." + geolevelName);
		}	
		catch (SQLException sqlException2) {
			throw new RIFSQLException(this.getClass(), sqlException2, statement2, sqlQueryText2);
		}
		finally { //Cleanup database resources
			connection.commit();
			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}				

		return generatedCount;
	}

	/** 
	 * Generate tiles for geolevel from tiles view (i.e. including the null tile). Not the t tiles table in getTopoJsonTileFromDb()
	 *
	 * @param connection 		Database JDBC Connection object	
	 * @param geoLevelSelect 	Database geolevel name
	 * @param slippyTile		SlippyTile
	 * @param hmap 				HashMap<String, String>; hashes are "topoJSON", "tileTable" and "geometryTable". Sets "topoJSON"
     */	
	private void getTopoJsonTileFromDb(
			final Connection connection, 
			final String geoLevelSelect, 
			final SlippyTile slippyTile,
			final HashMap<String, String> hmap) 
			throws RIFServiceException, SQLException {

		//For map tiles
		PreparedStatement statement2 = null;
		ResultSet resultSet2 = null;
		String sqlQueryText2 = null;
		String tileTable = hmap.get("tileTable");

		try {
			//This is the tile table name for this geography
			SelectQueryFormatter getMapTilesQueryFormatter
					= new MSSQLSelectQueryFormatter();

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

			getMapTilesQueryFormatter.addSelectField("rif_data." + tileTable, "optimised_topojson");
			getMapTilesQueryFormatter.addFromTable("rif_data." + tileTable);
			getMapTilesQueryFormatter.addFromTable("rif40.rif40_geolevels");
			getMapTilesQueryFormatter.addWhereJoinCondition("rif_data." + tileTable, 
				"geolevel_id", "rif40.rif40_geolevels", "geolevel_id");
			getMapTilesQueryFormatter.addWhereParameter(
				applySchemaPrefixIfNeeded("rif40_geolevels"),
				"geolevel_name");
			getMapTilesQueryFormatter.addWhereParameter("rif_data." + tileTable, "zoomlevel");
			getMapTilesQueryFormatter.addWhereParameter("rif_data." + tileTable, "x");
			getMapTilesQueryFormatter.addWhereParameter("rif_data." + tileTable, "y");

			sqlQueryText2 = logSQLQuery(
					"getTileMakerTiles",
					getMapTilesQueryFormatter,
					geoLevelSelect,
					new Integer(slippyTile.getZoomlevel()).toString(),
					new Integer(slippyTile.getX()).toString(),
					new Integer(slippyTile.getY()).toString());

			statement2 = connection.prepareStatement(getMapTilesQueryFormatter.generateQuery());
			statement2.setString(1, geoLevelSelect);
			statement2.setInt(2, new Integer(slippyTile.getZoomlevel()));
			statement2.setInt(3, new Integer(slippyTile.getX()));
			statement2.setInt(4, new Integer(slippyTile.getY()));
			resultSet2 = statement2.executeQuery();
			if (resultSet2.next()) {
				String topoJSON = resultSet2.getString(1);
				hmap.put("topoJSON", topoJSON);
			}
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			throw new RIFSQLException(this.getClass(), sqlException, statement2, sqlQueryText2);
		} finally {
			//Cleanup database resources

			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}
		
		connection.commit();
	}

	/** 
	 * Generate tiles for geolevel from T_ tiles table (i.e. not the null tile). Not the tile table in getTopoJsonTileFromDb()
	 *
	 * @param connection 		Database JDBC Connection object	
	 * @param geolevelId 		Database geolevel ID
	 * @param hmap 				HashMap<String, String>; hashes are "topoJSON", "tileTable" and "geometryTable"
	 * @param geography			Uppercase String
	 * @param geolevelName 		Name of geolevel
	 * @param tileCount 		Number of tiles to be processed	
	 * @param minZoomlevel		Minimum zoomlevel
	 * @param maxZoomlevel		Maximum zoomlevel
	 *
	 * @return Number of tiles generated
     */		
	private int generateTilesForGeoLevel(
		final Connection connection, 
		final int geolevelId, 
		final HashMap<String, String> hmap,
		final String geography, 
		final String geolevelName,
		final int tileCount,
		final int minZoomlevel,
		final int maxZoomlevel)
			throws RIFServiceException, SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		String tileTable = hmap.get("tileTable");
		SelectQueryFormatter generateTilesForGeoLevelQueryFormatter
				= new MSSQLSelectQueryFormatter();

		//STEP 3: get the tile: zoomlevel, x, y, tile_id
		/*
			SELECT zoomlevel, x, y, tile_id
			  FROM rif_data.t_tiles_sahsuland
			 WHERE geolevel_id = 4
			 ORDER BY zoomlevel, x, y;
		*/

		generateTilesForGeoLevelQueryFormatter.addSelectField("zoomlevel");
		generateTilesForGeoLevelQueryFormatter.addSelectField("x");
		generateTilesForGeoLevelQueryFormatter.addSelectField("y");
		generateTilesForGeoLevelQueryFormatter.addSelectField("optimised_topojson");
		generateTilesForGeoLevelQueryFormatter.addFromTable("rif_data.t_" + tileTable.toLowerCase());
		generateTilesForGeoLevelQueryFormatter.addWhereParameter("rif_data.t_" + tileTable.toLowerCase(), "geolevel_id");
		generateTilesForGeoLevelQueryFormatter.addOrderByCondition("zoomlevel", SelectQueryFormatter.SortOrder.ASCENDING);
		generateTilesForGeoLevelQueryFormatter.addOrderByCondition("x", SelectQueryFormatter.SortOrder.ASCENDING);
		generateTilesForGeoLevelQueryFormatter.addOrderByCondition("y", SelectQueryFormatter.SortOrder.ASCENDING);

		String sqlQueryText = logSQLQuery(
				"generateTiles",
				generateTilesForGeoLevelQueryFormatter,
				String.valueOf(geolevelId));
		int generatedCount=0;

		try {
			statement = connection.prepareStatement(generateTilesForGeoLevelQueryFormatter.generateQuery());
			statement.setInt(1, geolevelId);

			resultSet = statement.executeQuery();
			int i=0;
			while (resultSet.next()) {
				i++;
				int zoomlevel=resultSet.getInt(1);	
				int x=resultSet.getInt(2);		
				int y=resultSet.getInt(3);			
				String optimisedTopojson=resultSet.getString(4);	
				hmap.put("topoJSON", optimisedTopojson);
				SlippyTile slippyTile = new SlippyTile(zoomlevel, x, y);
					
				File file=rifTilesCache.getCachedTileFile(geography, slippyTile, geolevelName, "png");	
				if (!file.exists()) {
					generatedCount++;
					rifLogger.debug(getClass(), "Generate GeoJSON (" + i + "/" + tileCount + "): " + file.toString() +
						"; from optimisedTopojson, size: " + optimisedTopojson.length());
		
					JSONObject tileTopoJson = new JSONObject(optimisedTopojson);
					JSONObject tileGeoJson = topoJson2geoJson(connection, 
						tileTopoJson,
						geography, slippyTile, hmap, geolevelName, false /* addBoundingBoxToTile */,
						minZoomlevel, maxZoomlevel);
						
					rifLogger.info(getClass(), "Generate PNG tile (" + i + "/" + tileCount + "): " + file.toString());
					try {
						rifPdfTiles.geoJson2png(
							tileGeoJson,
							geography,
							slippyTile,
							geolevelName);
					}	
					catch (Exception exception) {
						throw new RIFServiceException(
							RIFServiceError.TILE_GENERATE_GEOTOOLS_ERROR,
							exception.getMessage(), exception);
					}
				}
			}	
		}	
		catch (SQLException sqlException) {
			throw new RIFSQLException(this.getClass(), sqlException, statement, sqlQueryText);
		}
		catch (JSONException jsonException) {
			throw new RIFServiceException(
				RIFServiceError.TILE_GENERATE_JSON_ERROR,
				jsonException.getMessage(), jsonException);
		}		
		finally { //Cleanup database resources
			connection.commit();
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}			

		return generatedCount;
	}
	
	/** 
	 * Add bounding box as geoJSON feature so we can see the boundary of the tile
	 *
	 * @param bboxJson			JSONArray bounding box in GeoJSON format [minX, minY, maxX, maxY]
	 * @param properties		JSONArray of GeoJSON properties
	 *
	 * @return GeoJSON bounding box as a feature (for debugging Tile boundaries) JSONObject
	 *
	 * @throws JSONException Error manipulating JSON
     */		
	private JSONObject createGeoJsonBboxFeature(
		final JSONArray bboxJson,
		final JSONObject properties) 
			throws JSONException {
		JSONObject geoJsonFeature = new JSONObject();
		
		properties.put("gid", 0);
		properties.put("name", "BBOX");
		properties.put("area_id", "BBOX");
		
		geoJsonFeature.put("type", "Feature");
		geoJsonFeature.put("properties", properties);
		
		JSONObject geometry = new JSONObject();
		geometry.put("type", "MultiPolygon");
		
		// bboxJson is: [minX, minY, maxX, maxY] 
		// coordinates are(SW, NW, NE, SE, SW): [[[[minX, minY],[minX, maxY],[maxX, maxY],[maxX, minY],[minX, minY]]]]
		
		JSONArray sw = new JSONArray(); // [minX, minY]
		sw.put(bboxJson.getDouble(0));
		sw.put(bboxJson.getDouble(1));
		JSONArray nw = new JSONArray(); // [minX, maxY]
		nw.put(bboxJson.getDouble(0));
		nw.put(bboxJson.getDouble(3));
		JSONArray ne = new JSONArray(); // [maxX, maxY]
		ne.put(bboxJson.getDouble(2));
		ne.put(bboxJson.getDouble(3));
		JSONArray se = new JSONArray(); // [maxX, minY]
		se.put(bboxJson.getDouble(2));
		se.put(bboxJson.getDouble(1));
		
		JSONArray coordinates = new JSONArray();
		JSONArray coordinates2 = new JSONArray();
		JSONArray coordinates3 = new JSONArray();
		coordinates.put(sw);
		coordinates.put(nw);
		coordinates.put(ne);
		coordinates.put(se);
		coordinates.put(sw);
		coordinates2.put(coordinates);
		coordinates3.put(coordinates2);
		geometry.put("coordinates", coordinates3);
		
		geoJsonFeature.put("geometry", geometry);
		
//		rifLogger.info(getClass(), "Processed bbox: " + bboxJson.toString() + " into feature: " + geoJsonFeature.toString(2));
		return geoJsonFeature;
	}

	/** 
	 * Fetches the NULL topoJSON tile
	 *
	 * @return TopoJSON Tile as a string (JSON not JSON5) 
     */		
	public String getNullTopoJSONTile() {
		return NULL_TOPOJSON_TILE;
	}

	/** 
	 * Fetches the NULL GeoJSON tile	
	 *
	 * @return GeoJSON Tile as a string (JSON not JSON5) 
     */		
	public String getNullGeoJSONTile() {
		return NULL_GEOJSON_TILE;
	}
	
	/** 
	 * Fetches the NULL GeoJSON tile with the bounding box as a feature (for debugging Tile boundaries)
	 *
	 * @param bboxJson JSONArray bounding box in GeoJSON format [minX, minY, maxX, maxY]	
	 *
	 * @return GeoJSON Tile as a string (JSON not JSON5) 
     */	 
	public String getNullGeoJSONTile(
		final JSONArray bboxJson) {
		JSONObject nullGeoJsonTile = new JSONObject(NULL_GEOJSON_TILE);
		JSONObject bboxJsonProperties = new JSONObject();
		JSONArray geoJsonFeatures = nullGeoJsonTile.getJSONArray("features");
		geoJsonFeatures.put(createGeoJsonBboxFeature(bboxJson, bboxJsonProperties));
		nullGeoJsonTile.put("features", geoJsonFeatures);
		
		return nullGeoJsonTile.toString();
	}
	
	/** 
	 * Convert TopoJSON to GeoJSON
	 * <p>	 
	 * There is no Java way of converting TopoJSON to GeoJSON like topojson.feature(topology, object)
	 * https://github.com/topojson/topojson-client/blob/master/README.md#feature
	 * Various Java libraries to manipulate GeoJSon are immature, unmaintained and undocumented.
	 * GDAL can convert, but again it is not documented, especially GDAL Java.
	 *
	 * The chosen method here is to populate <myTileTable>.optimised_geojson by parsing the area_ids from
	 * the topoJSON properties and then fetching the GeoJSON from <myGeometryTable>
	 *	 
	 * Called from: RIFTiles.java generateTiles(); knows determine minZoomlevel and maxZoomlevel									
	 * </p>
	 *
	 * @param connection			JDBC Connection
	 * @param tileTopoJson			JSONObject of tile GeoJSON
	 * @param geography      	  	Geography as a uppercase String
	 * @param slippyTile 			SlippyTile (zoomlevel, x, y)
	 * @param hmap 					HashMap<String, String>; hashes are "topoJSON", "tileTable" and "geometryTable"
	 * @param geoLevel     	     	Geolevel as an uppercase String
	 * @param addBoundingBoxToTile 	Add bounding box as an additional feature for debug purposes
	 * @param minZoomlevel      	Minimum zoomlevel
	 * @param maxZoomlevel     		Maximum zoomlevel
	 *
	 * @return GeoJSON as JSONObject
	 *
	 * @throws RIFServiceException RIF error
	 * @throws RIFSQLException RIF SQL error
	 * @throws JSONException Error manipulating JSON
     */	
	/* Generated GeoJSON in Json5 format:
	 *
	 * {
	 * 	"features": [{
	 * 			"geometry": {
	 * 				"coordinates": [[[[-7.2447, 54.3752], [-7.2332, 54.3759], [-7.2339, 54.3767], [-7.2326, 54.3772], ... ]]]
	 * 				"type": "MultiPolygon"
	 *			},
	 *			"type": "Feature",
	 *			"properties": {
	 *				"gid": 0,
	 *				"SAHSU_GRD_LEVEL1": "01",
	 *				"zoomlevel": 0,
	 *				"name": "BBOX",
	 *				"geographic_centroid": {
	 *					"coordinates": [-6.30097, 54.1803],
	 *					"type": "Point"
	 *				},
	 *				"x": 0,
	 *				"y": 0,
	 *				"area_id": "BBOX"
	 *			}
	 *		}, { ...
	 *	 }
	 *		 	],
	 *	"type": "FeatureCollection"
	 * }
	 *
	 * Source TopoJSON in Json5 format:
	 *
	 *		{
	 *			"transform": {
	 *				"scale": [1.2645986036803175E-4, 1.964578552911635E-4],
	 *				"translate": [-6.151058640054187, 53.198324512334224]
	 *			},
	 *			"objects": {
	 *				"collection": {
	 *					"type": "GeometryCollection",
	 *					"bbox": [-8.649433731630149, 49.87112937372648, 1.7627739932037385, 60.84572000540925],
	 *					"geometries": [{
	 *							"type": "GeometryCollection",
	 *							"properties": {
	 *								"gid": 1,
	 *								"area_id": "UK",
	 *								"name": "United_Kingdom",
	 *								"geographic_centroid": {
	 *									"type": "Point",
	 *									"coordinates": [-4.03309, 55.8001]
	 *								},
	 *								"x": 0,
	 *								"y": 0,
	 *								"SCNTRY2011": "UK",
	 *								"zoomlevel": 0
	 *							},
	 *							"id": 1,
	 *							"geometries": [{
	 *									"type": "Polygon",
	 *									"arcs": [[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]]
	 *								}, ...
	 *							]
	 *						}
	 *					],
	 *						...
	 *				}
	 *			},
	 *			"bbox": [-6.151058640054187, 53.198324512334224, -4.886586496234238, 55.16270660739057],
	 *			"type": "Topology",
	 *			"arcs": [[[95, 7301], ... ]]
	 *		}				
	 */			
	private JSONObject topoJson2geoJson(
			final Connection connection,
			final JSONObject tileTopoJson,
			final String geography,
			final SlippyTile slippyTile, 
			final HashMap<String, String> hmap,
			final String geoLevel,
			final boolean addBoundingBoxToTile,
			final int minZoomlevel,
			final int maxZoomlevel)
			throws SQLException, JSONException, RIFServiceException
	{	
		String myGeometryTable = hmap.get("geometryTable");
		JSONArray bboxJson = slippyTile.tile2boundingBox();
		JSONArray geoJsonFeatures = new JSONArray();	 
		JSONArray geometries;
		
		JSONObject objects = tileTopoJson.optJSONObject("objects");
		if (objects != null) {
			JSONObject collection = objects.optJSONObject("collection");
			if (collection != null) {
				geometries = collection.optJSONArray("geometries");
				if (geometries != null) {
					
					ArrayList<String> areaIdList = new ArrayList<>();
					JSONObject bboxJsonProperties = null;
					HashMap<String, JSONObject> propertiesHash = new HashMap<>();
					int processedCount=0;
					
					for (int i=0; i<geometries.length(); i++) {
						
						JSONObject jsonGeometry=geometries.getJSONObject(i);
						JSONObject properties=jsonGeometry.optJSONObject("properties");		
						if (i == 0) {
							bboxJsonProperties = jsonGeometry.optJSONObject("properties");
						}
					
						if (properties != null) {			
							String areaId = properties.getString("area_id");
							areaIdList.add(areaId);
							propertiesHash.put(areaId, properties);	
						}
						else {
							List<String> propertiesList = IteratorUtils.toList(properties.keys());
							String propertiesText = String.join(", ", propertiesList);
							throw new JSONException("TopoJSON Object[\"properties\"] not found; keys: " + propertiesText);
						}

						if (i % 10000 == 0) { // Process in blocks of 10,000
							processedCount+=processGeoJsonArrayList(connection, slippyTile.getZoomlevel(), geoLevel, myGeometryTable, 
								areaIdList, geoJsonFeatures, propertiesHash, minZoomlevel, maxZoomlevel);
							
							areaIdList.clear();
							propertiesHash.clear();
						}
						
					} // End of for loop
					
					/* processedCount+= */
					processGeoJsonArrayList(connection, slippyTile.getZoomlevel(), geoLevel, myGeometryTable, areaIdList,
						geoJsonFeatures, propertiesHash, minZoomlevel, maxZoomlevel);
//					rifLogger.debug(getClass(), "Processed: " + geometries.length() + " geometries" +
//						"; geoJsonFeatures: " + geoJsonFeatures.length() +
//						"; geometries: " + geometries.length() +
//						"; valid areaIds: " + processedCount +
//						"; for geoLevel: " + geoLevel +
//						"; zoomlevel: " + zoomlevel +
//						"; x: " + x +
//						"; y: " + y);
					
					if (addBoundingBoxToTile && bboxJsonProperties != null) {
						geoJsonFeatures.put(createGeoJsonBboxFeature(bboxJson, bboxJsonProperties));
					}
						
				}
				else {
					throw new JSONException("TopoJSON Array[\"geometries\"] not found");
				}				
			}
			else /* if (collection != null) */ {
				List<String> collectionList = IteratorUtils.toList(collection.keys()); // Ignore NPE warnings from ItelliJ
				String collectionText = String.join(", ", collectionList);
				throw new JSONException("TopoJSON Object[\"objects\"] not found; keys: " + collectionText);
			}
		}
		else /* if (tileTopoJson != null) */ {
			List<String> tileTopoJsonList = IteratorUtils.toList(tileTopoJson.keys()); // Ignore NPE warnings from ItelliJ
			String tileTopoJsonText = String.join(", ", tileTopoJsonList);
			throw new JSONException("TopoJSON Object[\"objects\"] not found; keys: " + tileTopoJsonText);
		}
		
		JSONObject tileGeoJson = new JSONObject();
		tileGeoJson.put("type", "FeatureCollection");
		tileGeoJson.put("features", geoJsonFeatures);
		
		if (addBoundingBoxToTile) {
			rifTilesCache.cacheTile(tileGeoJson, null /* pngTileStream */, geography.toLowerCase(), slippyTile, geoLevel.toLowerCase(), "json");
		}
		return tileGeoJson;
	}

	/** 
	 * Process array list into GeoJSON features
	 *
	 * @param connection 		Database JDBC Connection object	
	 * @param zoomlevel 		0-9 (depends in TileMaker maximum zoomlevel)
	 * @param geoLevel 			Uppercase String	 
	 * @param myGeometryTable 	Uppercase String	 
	 * @param areaIdList		ArrayList<String> of areaIDs
	 * @param geoJsonFeatures	JSONArray GeoJSON features 
	 * @param propertiesHash	HashMap<String, JSONObject> of areaID and JSON properties	
	 * @param minZoomlevel		Minimum zoomlevel
	 * @param maxZoomlevel		Maximum zoomlevel
	 *
	 * @return Number of GeoJSON features processed
	 *
	 * @throws RIFServiceException RIF error
	 * @throws SQLException logical database error
     */		
	private int processGeoJsonArrayList(
		final Connection connection,
		final Integer zoomlevel,
		final String geoLevel,
		final String myGeometryTable,
		final ArrayList<String> areaIdList,
		final JSONArray geoJsonFeatures,
		final HashMap<String, JSONObject> propertiesHash,
		final int minZoomlevel,
		final int maxZoomlevel) 
			throws SQLException, RIFServiceException {
		
		int processedCount=0;
		int removedCount=0;
		HashMap<String, String> wktHash = getWKT(connection, zoomlevel, geoLevel, myGeometryTable.toLowerCase(), areaIdList,
			minZoomlevel, maxZoomlevel);
		if (wktHash != null) {
			
			if (wktHash.size() != areaIdList.size()) {
				throw new SQLException("Error in getWKT; expecting: " + areaIdList.size() + "; got: " + wktHash.size());
			}
			Set set = wktHash.entrySet();
			for (Object o : set) {
				processedCount++;
				Map.Entry mentry = (Map.Entry) o;
				String areaIdKey = (String) mentry.getKey();
				String wkt = (String) mentry.getValue();

				JSONObject geoJsonFeature = new JSONObject();
				geoJsonFeature.put("type", "Feature");
				geoJsonFeature.put("properties", propertiesHash.get(areaIdKey));
				JSONObject njsonGeometry = rifWellKnownText.wktToGeoJSON(wkt, geoLevel, zoomlevel, areaIdKey);
				if (njsonGeometry != null) {
					geoJsonFeature.put("geometry", njsonGeometry);
					geoJsonFeatures.put(geoJsonFeature);
				} 
				else {
					removedCount++;
				}
			} // End of while loop
		}
		else {
			throw new JSONException("Null wktHash for geoLevel: " + geoLevel +
				"; zoomlevel: " + zoomlevel);
		}	
		
		if (processedCount != areaIdList.size()) {
			throw new SQLException("Error processing GoeJSON features from WKT; expecting: " + areaIdList.size() + 
				"; got: " + processedCount);
		}	
		else {		
			rifLogger.debug(getClass(), "Processed: " + processedCount + " areaIds" +
				"; removed: " + removedCount +
				"; total geoJsonFeatures: " + geoJsonFeatures.length());
		}
							
		return geoJsonFeatures.length();
	}
}