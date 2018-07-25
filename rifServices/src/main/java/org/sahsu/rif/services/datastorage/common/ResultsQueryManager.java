package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Calendar;

import org.json.JSONObject;

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
import org.sahsu.rif.services.datastorage.common.RifLocale;
import org.sahsu.rif.services.rest.RIFResultTableJSONGenerator;

public class ResultsQueryManager extends BaseSQLManager {

	public ResultsQueryManager(final RIFServiceStartupOptions options) {

		super(options);

		FunctionCallerQueryFormatter getTilesQueryFormatter = new FunctionCallerQueryFormatter();
		configureQueryFormatterForDB(getTilesQueryFormatter);
		getTilesQueryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		getTilesQueryFormatter.setFunctionName("rif40_get_geojson_tiles");
		getTilesQueryFormatter.setNumberOfFunctionParameters(9);
	}

	String getPostalCodeCapabilities(
			final Connection connection, 
			final Geography geography) throws RIFServiceException {
		String result="{}";

		SelectQueryFormatter getPostalCodeCapabilitiesQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getPostalCodeCapabilitiesQueryFormatter1.setDatabaseSchemaName("rif40");
		getPostalCodeCapabilitiesQueryFormatter1.addSelectField("postal_population_table");
		getPostalCodeCapabilitiesQueryFormatter1.addSelectField("srid");
		getPostalCodeCapabilitiesQueryFormatter1.addFromTable("rif40_geographies");
		getPostalCodeCapabilitiesQueryFormatter1.addWhereParameter("geography");

		logSQLQuery("getPostalCodeCapabilities", getPostalCodeCapabilitiesQueryFormatter1, geography.getName().toUpperCase());
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;
		
		try {
			statement1 = connection.prepareStatement(getPostalCodeCapabilitiesQueryFormatter1.generateQuery());
			statement1.setString(1, geography.getName().toUpperCase());
			resultSet1 = statement1.executeQuery();
		
			if (!resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getPostalCodeCapabilities query 1; expected 1 row, got none for geography: " + geography.getName().toUpperCase());
			}
			String postalPopulationTable = resultSet1.getString(1);
			int srid = resultSet1.getInt(2);
			
			if (resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getPostalCodeCapabilities query 1; expected 1 row, got >1 for geography: " + geography.getName().toUpperCase());
			}	

			if (srid == 0) {
				throw new Exception("getPostalCodeCapabilities no/invalid srid defined for geography: " + geography.getName().toUpperCase());
			}
	
			if (postalPopulationTable != null && postalPopulationTable.length() > 0) {
				boolean hasXcoordinate=doesColumnExist(connection, 
					"rif_data", postalPopulationTable.toLowerCase(), "xcoordinate");
				boolean hasYcoordinate=doesColumnExist(connection, 
					"rif_data", postalPopulationTable.toLowerCase(), "ycoordinate");

				if (hasXcoordinate == false || hasYcoordinate == false) {
					throw new Exception("getPostalCodeCapabilities X/Y coordinate columns not found in geography: " + geography.getName().toUpperCase() +
						" postal population table: " + postalPopulationTable + 
						"; hasXcoordinate: " + hasXcoordinate + 
						"; hasYcoordinate: " + hasYcoordinate);
				} 
			}			
			
			JSONObject getPostalCodeCapabilities = new JSONObject();
			getPostalCodeCapabilities.put("geography", geography.getName().toUpperCase());
			if (postalPopulationTable != null && postalPopulationTable.length() > 0) {
				getPostalCodeCapabilities.put("postalPopulationTable", postalPopulationTable);
			}
			getPostalCodeCapabilities.put("srid", srid);
			result=getPostalCodeCapabilities.toString();	
			
			connection.commit();
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetPostalCodeCapabilities",
					geography.getName().toUpperCase());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} catch(Exception exception) {
			
			logException(exception);
			
			JSONObject getPostalCodeCapabilities = new JSONObject();
			getPostalCodeCapabilities.put("geography", geography.getName().toUpperCase());
			getPostalCodeCapabilities.put("error", RIFResultTable.quote(exception.getMessage()));
			result=getPostalCodeCapabilities.toString();	
			
			try {
				connection.commit();
			} catch(SQLException sqlException) {
				logSQLException(sqlException);
			}
			
			return result;
			
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}	
		
	}
			
	String getPostalCodes(
			final Connection connection, 
			final Geography geography,
			final String postcode,
			final Locale locale) throws RIFServiceException {
				
		RifLocale rifLocale = new RifLocale(locale);
		Calendar calendar = rifLocale.getCalendar();			
		DateFormat df = rifLocale.getDateFormat();
		
		String cleanPostcode=postcode;
		String result="{}";
		
		SelectQueryFormatter getPostalCodesQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getPostalCodesQueryFormatter1.setDatabaseSchemaName("rif40");
		getPostalCodesQueryFormatter1.addSelectField("postal_population_table");
		getPostalCodesQueryFormatter1.addSelectField("srid");
		getPostalCodesQueryFormatter1.addFromTable("rif40_geographies");
		getPostalCodesQueryFormatter1.addWhereParameter("geography");

		logSQLQuery("getPostalCodes", getPostalCodesQueryFormatter1, geography.getName().toUpperCase());
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;
		PreparedStatement statement2 = null;
		ResultSet resultSet2 = null;

		try {
			statement1 = connection.prepareStatement(getPostalCodesQueryFormatter1.generateQuery());
			statement1.setString(1, geography.getName().toUpperCase());
			resultSet1 = statement1.executeQuery();
		
			if (!resultSet1.next()) {
				throw new Exception("getPostalCodes query 1; expected 1 row, got none");
			}
			String postalPopulationTable = resultSet1.getString(1);
			int srid = resultSet1.getInt(2);
			Double xcoordinate=null;
			Double ycoordinate=null;
			
			if (resultSet1.next()) {
				throw new Exception("getPostalCodes query 1; expected 1 row, got >1");
			}
			if (srid == 27700) {
				cleanPostcode=postcode.toUpperCase();
				String outwardCode=null;
				if (cleanPostcode.length() > 3) {
					outwardCode=cleanPostcode.substring(cleanPostcode.length()-3, cleanPostcode.length()); 	// Last 3 characters
				}		
				if (outwardCode == null) {			
					result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
						"\"warning\": " + 
						"\"Postal code: \\\"" + cleanPostcode + "\\\" has no outwardCode for geography: " + 
						geography.getName().toUpperCase() + 
						"\"}}";
					connection.commit();
					
					return result;
				}
				outwardCode=outwardCode.trim();
				if (outwardCode.length() != 3) {			
					result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
						"\"warning\": " + 
						"\"Postal code: \\\"" + cleanPostcode + "\\\" has null/wrong size outwardCode (" + outwardCode + ") for geography: " + 
						geography.getName().toUpperCase() + 
						"\"}}";
					connection.commit();
					
					return result;
				}
				String inwardCode=null;
				if (cleanPostcode.length() > 4) {
					inwardCode=cleanPostcode.substring(0, cleanPostcode.length()-4);	// Character 0 to last 3
				}
				if (inwardCode == null) {
					result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
						"\"warning\": " + 
						"\"Postal code: \\\"" + cleanPostcode + "\\\" has no inwardCode for geography: " + 
						geography.getName().toUpperCase() + 
						"; outwardCode; " + outwardCode +
						"\"}}";
					connection.commit();
					
					return result;
				}		
				inwardCode=String.format("%-4s", 									// Pad to 4 characters
					inwardCode.trim()); 											// Trim white space						
				if (inwardCode.length() != 4) {		
					result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
						"\"warning\": " + 
						"\"Postal code: \\\"" + cleanPostcode + "\\\" has wrong sized inwardCode (" + inwardCode + ") for geography: " + 
						geography.getName().toUpperCase() + 
						"; outwardCode; " + outwardCode +
						"\"}}";
					connection.commit();
					
					return result;
				}
				cleanPostcode = inwardCode + outwardCode;
				if (cleanPostcode.length() != 7) { // error in cleaning logic!
					throw new Exception("getPostalCodes wrong sized cleanPostcode (" + cleanPostcode + ") for geography: " + 
						geography.getName().toUpperCase() + 
						"; postcode: " + postcode +
						"; inwardCode; " + inwardCode +
						"; outwardCode; " + outwardCode);
				}		
			}
			
			if (postalPopulationTable == null) {
				throw new Exception("getPostalCodes no postal population table defined for geography: " + geography.getName().toUpperCase() +
					"; postcode: " + postcode);
			}
	
			boolean hasXcoordinate=doesColumnExist(connection, 
				"rif_data", postalPopulationTable.toLowerCase(), "xcoordinate");
			boolean hasYcoordinate=doesColumnExist(connection, 
				"rif_data", postalPopulationTable.toLowerCase(), "ycoordinate");

			if (hasXcoordinate == false || hasYcoordinate == false) {
				throw new Exception("getPostalCodes X/Y coordinate columns not found in geography: " + geography.getName().toUpperCase() +
					" postal population table: " + postalPopulationTable + 
					"; hasXcoordinate: " + hasXcoordinate + 
					"; hasYcoordinate: " + hasYcoordinate);
			} 
		
			SelectQueryFormatter getPostalCodesQueryFormatter2 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());
			
			getPostalCodesQueryFormatter2.setDatabaseSchemaName("rif_data");
			getPostalCodesQueryFormatter2.addSelectField("*");
			getPostalCodesQueryFormatter2.addFromTable(postalPopulationTable.toLowerCase());
			getPostalCodesQueryFormatter2.addWhereParameter("postcode");
			
			logSQLQuery("getPostalCodes", getPostalCodesQueryFormatter2, cleanPostcode);
			
			statement2 = connection.prepareStatement(getPostalCodesQueryFormatter2.generateQuery());
			statement2.setString(1, cleanPostcode);
			resultSet2 = statement2.executeQuery();
			String[] columnNames;
			String[][] data;
			RIFResultTable.ColumnDataType[] columnDataTypes;
		
			if (resultSet2.next()) {
				int rowCount=0;
				ResultSetMetaData rsmd = resultSet2.getMetaData();
				int columnCount = rsmd.getColumnCount();
							
				columnNames = new String[2];
				columnNames[0] = "Name";
				columnNames[1] = "Value";
				
				data = new String[columnCount][2];
				columnDataTypes = new RIFResultTable.ColumnDataType[2];
				columnDataTypes[0] = RIFResultTable.ColumnDataType.TEXT;
				columnDataTypes[1] = RIFResultTable.ColumnDataType.JSON; // Everything will be JSON valid

				// The column count starts from 1
				for (int i = 1; i <= columnCount; i++ ) {
					String name = rsmd.getColumnName(i); 
					String value = null;
					if (resultSet2.getString(i) != null) {
						value = RIFResultTable.quote(resultSet2.getString(i)); // JSON Stringify
					}
					String columnType = rsmd.getColumnTypeName(i);
					
					if (columnType.equals("timestamp") ||
						columnType.equals("timestamptz") ||
						columnType.equals("datetime")) {
							Timestamp dateTimeValue=resultSet2.getTimestamp(i, calendar);
							value=RIFResultTable.quote(df.format(dateTimeValue));
					}
					else if (value != null && (
							 columnType.equals("integer") || 
							 columnType.equals("bigint") || 
							 columnType.equals("int4") ||
							 columnType.equals("int") ||
							 columnType.equals("smallint"))) {
						try { // Use normal decimal formatting - will cause confusion with coordinates
							Long longVal=Long.parseLong(resultSet2.getString(i));
							if (name.equals("xcoordinate")) {
								xcoordinate=new Double(longVal);
							}
							if (name.equals("ycoordinate")) {
								ycoordinate=new Double(longVal);
							}
//							value=RIFResultTable.quote(NumberFormat.getNumberInstance(locale).format(longVal));
						}
						catch (Exception exception) {
							rifLogger.error(this.getClass(), "Unable to parseLong(" + 
								columnType + "): " + resultSet2.getString(i),
								exception);
							throw exception;
						}
					}
					else if (value != null && (
							 columnType.equals("json"))) {
							value = resultSet2.getString(i); // JSON already
					}
					else if (value != null && (
							 columnType.equals("float") || 
							 columnType.equals("float8") || 
							 columnType.equals("double precision") ||
							 columnType.equals("numeric"))) {
						try { // Ditto
							Double doubleVal=Double.parseDouble(resultSet2.getString(i));
							if (name.equals("xcoordinate")) {
								xcoordinate=new Double(doubleVal);
							}
							if (name.equals("ycoordinate")) {
								ycoordinate=new Double(doubleVal);
							}
//							value=RIFResultTable.quote(NumberFormat.getNumberInstance(locale).format(doubleVal));
						}
						catch (Exception exception) {
							rifLogger.error(this.getClass(), "Unable to parseDouble(" + 
								columnType + "): " + resultSet2.getString(i),
								exception);
							throw exception;
						}
					}
					
					if (value != null) {
						data[i-1][0] = name;
						data[i-1][1] = value;
					}
				} // For loop
				rifLogger.info(getClass(), "get postcode: " + cleanPostcode + 
					" for geography: " + geography.getName().toUpperCase() +
					"; srid: " + srid);
				
				RIFResultTable results = new RIFResultTable();	
					
				results.setColumnProperties(columnNames, columnDataTypes);
				results.setData(data);
				
				JSONObject additionalTableJson = new JSONObject();
				additionalTableJson.put("postalCode", cleanPostcode);
				if (xcoordinate != null && ycoordinate != null) {
					additionalTableJson.put("xcoordinate", xcoordinate);
					additionalTableJson.put("ycoordinate", ycoordinate);
				}
				
				RIFResultTableJSONGenerator rifResultTableJSONGenerator =
						new RIFResultTableJSONGenerator();
				result = rifResultTableJSONGenerator.writeResultTable(results, additionalTableJson);
			}
			else {		
				result="{\"nopostcodefound\":{\"postalCode\": \"" + cleanPostcode + "\"," + 
					"\"warning\": " + 
					"\"Postal code: \\\"" + cleanPostcode + "\\\" for geography: " + geography.getName().toUpperCase() + " is invalid\"}}";
			}
				
			connection.commit();
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetPostalCode",
					geography.getName().toUpperCase(),
					cleanPostcode);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetPostalCode",
					geography.getDisplayName(),
					cleanPostcode);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);

			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}			
			
	}

	RIFResultTable getTileMakerCentroids(
			final Connection connection, 
			final Geography geography,
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
								
		PreparedStatement resultCounterStatement = null;
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
