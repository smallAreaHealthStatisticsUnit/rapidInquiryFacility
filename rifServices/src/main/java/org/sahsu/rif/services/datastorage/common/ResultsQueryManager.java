package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.datastorage.FunctionCallerQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.ms.MSSQLSelectQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

public class ResultsQueryManager extends BaseSQLManager {

	public ResultsQueryManager(final RIFServiceStartupOptions options) {

		super(options);

		FunctionCallerQueryFormatter getTilesQueryFormatter = new FunctionCallerQueryFormatter();
		configureQueryFormatterForDB(getTilesQueryFormatter);
		getTilesQueryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		getTilesQueryFormatter.setFunctionName("rif40_get_geojson_tiles");
		getTilesQueryFormatter.setNumberOfFunctionParameters(9);
	}

	RIFResultTable getTileMakerCentroids(
			final Connection connection, final Geography geography,
			final GeoLevelSelect geoLevelSelect) throws RIFServiceException {
				
		boolean hasPopulationWeightedCentroids=false;
		
		try {
			hasPopulationWeightedCentroids=doesColumnExist(connection, 
				"rif_data", "lookup_" + geoLevelSelect.getName().toLowerCase(), "population_weighted_centroid");
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in doesColumnExist()",
				exception);			
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetCentroids",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}				
		
		SelectQueryFormatter getTileMakerCentroidsQueryFormatter =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getTileMakerCentroidsQueryFormatter.setDatabaseSchemaName("rif_data");
		getTileMakerCentroidsQueryFormatter.addSelectField(geoLevelSelect.getName().toLowerCase());
		getTileMakerCentroidsQueryFormatter.addSelectField("areaname");
		if (hasPopulationWeightedCentroids) {
			getTileMakerCentroidsQueryFormatter.addSelectField("geographic_centroid");
			getTileMakerCentroidsQueryFormatter.addSelectField("population_weighted_centroid");
		}
		else {
			getTileMakerCentroidsQueryFormatter.addSelectField("geographic_centroid");
		}
		getTileMakerCentroidsQueryFormatter.addFromTable("lookup_" + geoLevelSelect.getName().toLowerCase());

		logSQLQuery("getTileMakerCentroids", getTileMakerCentroidsQueryFormatter);
								
		PreparedStatement resultCounterStatement;
		PreparedStatement statement = null;
		ResultSet resultCounterSet;
		ResultSet resultSet = null;

		try {
			//Count the number of results first
			resultCounterStatement = connection.prepareStatement(getTileMakerCentroidsQueryFormatter.generateQuery());
			resultCounterSet = resultCounterStatement.executeQuery();

			int totalNumberRowsInResults = 0;
			while (resultCounterSet.next()) {
				totalNumberRowsInResults++;
			}

			//get the results
			statement = connection.prepareStatement(getTileMakerCentroidsQueryFormatter.generateQuery());

			RIFResultTable results = new RIFResultTable();

			String[] columnNames;
			String[][] data;
			RIFResultTable.ColumnDataType[] columnDataTypes;
			if (hasPopulationWeightedCentroids) {
				columnNames = new String[6];
				data = new String[totalNumberRowsInResults][6];
				columnDataTypes = new RIFResultTable.ColumnDataType[6];
			}
			else {
				columnNames = new String[4];
				data = new String[totalNumberRowsInResults][4];
				columnDataTypes = new RIFResultTable.ColumnDataType[4];
			}
			
			columnNames[0] = "id";
			columnNames[1] = "name";
			columnNames[2] = "x";
			columnNames[3] = "y";
			if (hasPopulationWeightedCentroids) {
				columnNames[4] = "pop_x";
				columnNames[5] = "pop_y";
			}

			columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[1] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[2] = RIFResultTable.ColumnDataType.TEXT;
			columnDataTypes[3] = RIFResultTable.ColumnDataType.TEXT;
			if (hasPopulationWeightedCentroids) {
				columnDataTypes[4] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[5] = RIFResultTable.ColumnDataType.TEXT;
			}

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
				
				if (hasPopulationWeightedCentroids) {
					String pop_weighted_coords = resultSet.getString(4);
					if (pop_weighted_coords == null) {
						data[ithRow][4] = null;
						data[ithRow][5] = null;	
					}
					else {
						pop_weighted_coords=pop_weighted_coords.split(":")[2];
						String pop_x = pop_weighted_coords.split(",")[0];
						String pop_y = pop_weighted_coords.split(",")[1];
						pop_x = pop_x.replaceAll("[^0-9?!\\.-]","");
						pop_y = pop_y.replaceAll("[^0-9?!\\.-]","");
						data[ithRow][4] = pop_x;
						data[ithRow][5] = pop_y;	
					}					
				}
				ithRow++;
			}

			results.setColumnProperties(columnNames, columnDataTypes);
			results.setData(data);
			connection.commit();

			return results;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetCentroids",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);
		}
	}

	public String getTileMakerTiles(
			final Connection connection, final Geography geography,
			final GeoLevelSelect geoLevelSelect, final Integer zoomlevel, final Integer x,
			final Integer y) throws RIFServiceException {

		//STEP 1: get the tile table name
		/*
		SELECT tiletable
		FROM [sahsuland_dev].[rif40].[rif40_geographies]
		WHERE geography = 'SAHSULAND';
		*/

		SelectQueryFormatter getMapTileTableQueryFormatter =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getMapTileTableQueryFormatter.setDatabaseSchemaName(SCHEMA_NAME);
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

			getMapTilesQueryFormatter.addSelectField(myTileTable,"optimised_topojson");
			getMapTilesQueryFormatter.addFromTable(myTileTable);
			getMapTilesQueryFormatter.addFromTable("rif40.rif40_geolevels");
			getMapTilesQueryFormatter.addWhereJoinCondition(myTileTable, "geolevel_id", "rif40.rif40_geolevels", "geolevel_id");
			getMapTilesQueryFormatter.addWhereParameter(
					applySchemaPrefixIfNeeded("rif40_geolevels"),
					"geolevel_name");
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
			                           "; zoomlevel: " + zoomlevel.toString() +
			                           "; geolevel: " + geoLevelSelect.getName().toUpperCase() + " x/y: " + x + "/" + y +
			                           "; length: " + result.length());

			connection.commit();
			return result;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTiles",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);

			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}
	}
}
