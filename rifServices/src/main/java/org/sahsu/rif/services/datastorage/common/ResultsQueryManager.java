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
import java.util.List;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.apache.commons.collections.IteratorUtils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.datastorage.FunctionCallerQueryFormatter;
import org.sahsu.rif.generic.datastorage.SelectQueryFormatter;
import org.sahsu.rif.generic.datastorage.UpdateQueryFormatter;
import org.sahsu.rif.generic.datastorage.SQLQueryUtility;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.ms.MSSQLSelectQueryFormatter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.datastorage.common.RifLocale;
import org.sahsu.rif.services.rest.RIFResultTableJSONGenerator;


import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;

import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.renderer.label.LabelCacheImpl;
import org.geotools.renderer.GTRenderer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

public class ResultsQueryManager extends BaseSQLManager {

	private static String lineSeparator = System.getProperty("line.separator");
		
	public ResultsQueryManager(final RIFServiceStartupOptions options) {

		super(options);

		FunctionCallerQueryFormatter getTilesQueryFormatter = new FunctionCallerQueryFormatter();
		configureQueryFormatterForDB(getTilesQueryFormatter);
		getTilesQueryFormatter.setDatabaseSchemaName("rif40_xml_pkg");
		getTilesQueryFormatter.setFunctionName("rif40_get_geojson_tiles");
		getTilesQueryFormatter.setNumberOfFunctionParameters(9);
	}
					
	String getMapBackground(
			final Connection connection,
			final Geography geography)
					throws RIFServiceException {
		String result="{}";
		
		SelectQueryFormatter getMapBackgroundQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getMapBackgroundQueryFormatter1.setDatabaseSchemaName("rif40");
		getMapBackgroundQueryFormatter1.addSelectField("map_background");
		getMapBackgroundQueryFormatter1.addFromTable("rif40_geographies");
		getMapBackgroundQueryFormatter1.addWhereParameter("geography");

		logSQLQuery("getMapBackground", getMapBackgroundQueryFormatter1, geography.getName().toUpperCase());
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;

		try {
			statement1 = connection.prepareStatement(getMapBackgroundQueryFormatter1.generateQuery());
			statement1.setString(1, geography.getName().toUpperCase());
			resultSet1 = statement1.executeQuery();
				
			if (!resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getMapBackground query 1; expected 1 row, got none for geography: " + geography.getName().toUpperCase());
			}
			String mapBackground = resultSet1.getString(1);
			if (mapBackground == null || mapBackground.length() == 0) {
				mapBackground="NONE"; // Default is: OpenStreetMap Mapnik
			}			
			connection.commit();
	
			JSONObject getMapBackground = new JSONObject();
			getMapBackground.put("geography", geography.getName().toUpperCase());
			getMapBackground.put("mapBackground", mapBackground);
			result=getMapBackground.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetMapBackground",
					geography.getName().toUpperCase());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}	
	}	
					
	String getSelectState(
			final Connection connection,
			final String studyID)
					throws RIFServiceException {
						
		String result="{}";
		
		SelectQueryFormatter getSelectStateQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getSelectStateQueryFormatter1.setDatabaseSchemaName("rif40");
		getSelectStateQueryFormatter1.addSelectField("select_state");
		getSelectStateQueryFormatter1.addFromTable("rif40_studies");
		getSelectStateQueryFormatter1.addWhereParameter("study_id");

		logSQLQuery("getSelectState", getSelectStateQueryFormatter1, studyID);
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;		

		try {
			statement1 = connection.prepareStatement(getSelectStateQueryFormatter1.generateQuery());
			statement1.setInt(1, Integer.parseInt(studyID));
			resultSet1 = statement1.executeQuery();
		
			if (!resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getSelectState query 1; expected 1 row, got none for study_id: " + studyID);
			}
			String selectStateStr = resultSet1.getString(1);
			JSONObject selectState;
			JSONObject getSelectState = new JSONObject();
			getSelectState.put("study_id", studyID);
			if (selectStateStr != null && selectStateStr.length() > 0) {
				try {
					selectState = new JSONObject(selectStateStr);
					getSelectState.put("select_state", selectState);
				}
				catch (JSONException jsonException) {
					throw new RIFServiceException(
						RIFServiceError.JSON_PARSE_ERROR,
						jsonException.getMessage() + "; in: select_state=" + selectStateStr);
				}
			}
			connection.commit();
			result=getSelectState.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetSelectState",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}
	}		
					
	String getPrintState(
			final Connection connection,
			final String studyID)
					throws RIFServiceException {
		String result="{}";
		
		SelectQueryFormatter getPrintStateQueryFormatter1 =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getPrintStateQueryFormatter1.setDatabaseSchemaName("rif40");
		getPrintStateQueryFormatter1.addSelectField("print_state");
		getPrintStateQueryFormatter1.addFromTable("rif40_studies");
		getPrintStateQueryFormatter1.addWhereParameter("study_id");

		logSQLQuery("getPrintState", getPrintStateQueryFormatter1, studyID);
	
		PreparedStatement statement1 = null;
		ResultSet resultSet1 = null;		

		try {
			statement1 = connection.prepareStatement(getPrintStateQueryFormatter1.generateQuery());
			statement1.setInt(1, Integer.parseInt(studyID));
			resultSet1 = statement1.executeQuery();
		
			if (!resultSet1.next()) {
				throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					"getPrintState query 1; expected 1 row, got none for study_id: " + studyID);
			}
			String printStateStr = resultSet1.getString(1);
			JSONObject getPrintState = new JSONObject();	
			getPrintState.put("study_id", studyID);
			JSONObject printState;
			if (printStateStr != null && printStateStr.length() > 0) {
				try {
					printState = new JSONObject(printStateStr);
					getPrintState.put("print_state", printState);
				}
				catch (JSONException jsonException) {
					throw new RIFServiceException(
						RIFServiceError.JSON_PARSE_ERROR, 
						jsonException.getMessage() + "; in: print_state=" + printStateStr);
				}
			}
			connection.commit();
			result=getPrintState.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetPrintState",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
			SQLQueryUtility.close(resultSet1);
		}
	}		
					
	String setPrintState(
			final Connection connection,
			final String studyID,
			final String printStateText)
					throws RIFServiceException {
		String result="{}";
		
		UpdateQueryFormatter setPrintStateQueryFormatter1 =
				UpdateQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		setPrintStateQueryFormatter1.setDatabaseSchemaName("rif40");
		if (rifDatabaseProperties.getDatabaseType() == DatabaseType.POSTGRESQL) { // Supports JSON natively
			setPrintStateQueryFormatter1.addUpdateField("print_state", "JSON");
		}
		else { // SQL Server doesn't yet
			setPrintStateQueryFormatter1.addUpdateField("print_state");
		}		
		setPrintStateQueryFormatter1.setUpdateTable("rif40_studies");
		setPrintStateQueryFormatter1.addWhereParameter("study_id");

		logSQLQuery("setPrintState", setPrintStateQueryFormatter1, printStateText, studyID);
	
		PreparedStatement statement1 = null;
		int rc;		

		try {
			statement1 = connection.prepareStatement(setPrintStateQueryFormatter1.generateQuery());
			statement1.setString(1, printStateText);
			statement1.setInt(2, Integer.parseInt(studyID));
			rc = statement1.executeUpdate();
		
			if (rc != 1) { 
				throw new RIFServiceException(
					RIFServiceError.DATABASE_UPDATE_FAILED,
					"setPrintState query 1; expected 1 row, got none for rif40_studies.study_id: " + studyID + " update");
			}
			connection.commit();
	
			JSONObject setPrintState = new JSONObject();		
			setPrintState.put("study_id", studyID);
			setPrintState.put("print_state", printStateText);
			result=setPrintState.toString();	
			
			return result;
		} catch(RIFServiceException rifServiceException) {
			throw rifServiceException;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToSetPrintState",
					studyID);
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);
		}  finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement1);
		}
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
			final Connection connection, 
			final Geography geography,
			final GeoLevelSelect geoLevelSelect, 
			final Integer zoomlevel, 
			final Integer x,
			final Integer y,
			final String tileType) throws RIFServiceException {

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
		getMapTileTableQueryFormatter.addSelectField("rif40_geographies", "geometrytable");
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
			String myGeometryTable = "rif_data." + resultSet.getString(2);

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

			connection.commit();
			
			if (tileType.equals("topojson")) {		

				rifLogger.info(getClass(), "get tile for geography: " + geography.getName().toUpperCase() +
										   "; tileType: " + tileType +
										   "; tileTable: " + myTileTable +
										   "; zoomlevel: " + zoomlevel.toString() +
										   "; geolevel: " + geoLevelSelect.getName().toUpperCase() + lineSeparator + 
										   "; x/y: " + x + "/" + y +
										   "; length: " + result.length());			
				return result;
			}
			else if (tileType.equals("geojson") || tileType.equals("png")) {	
				if (result != null && result.length() > 0 && 
					!result.equals("{\"type\": \"FeatureCollection\",\"features\":[]}") /* Null tile */) {
					try {
						JSONObject tileTopoJson = new JSONObject(result);
						JSONArray bboxJson = tileTopoJson.optJSONArray("bbox");
						if (bboxJson == null) {
							throw new JSONException("TopoJSON Array[\"bbox\"] not found");
						}
						else if (bboxJson.length() != 4) {
							throw new JSONException("TopoJSON Array[\"bbox\"] is not of length 4: " + bboxJson.toString());
						}
						JSONObject tileGeoJson = topoJson2geoJson(connection, tileTopoJson, tileType, myTileTable, myGeometryTable, 
							geography.getName().toUpperCase(),
							zoomlevel, geoLevelSelect.getName().toUpperCase(), x, y);
						
						if (tileType.equals("png")) {	
							result = geoJson2png(tileGeoJson, bboxJson, geography.getName().toUpperCase(), zoomlevel, 
								geoLevelSelect.getName().toUpperCase(), x, y);
						}
						else {
							result = tileGeoJson.toString();
						}
						rifLogger.info(getClass(), 
							"topoJson2geoJson tile for geography: " + geography.getName().toUpperCase() +
							"; tileType: " + tileType +
							"; tileTable: " + myTileTable +
							"; zoomlevel: " + zoomlevel.toString() +
							"; geolevel: " + geoLevelSelect.getName().toUpperCase() + lineSeparator + 
							"; x/y: " + x + "/" + y +
							"; length: " + result.length());
				
						return result;
					}
					catch (JSONException jsonException) {
						throw new RIFServiceException(
							RIFServiceError.JSON_PARSE_ERROR,
							jsonException.getMessage() + "; in: topojson[0-300]=" + result.substring(1, 300));
					}
					catch (IOException ioException) {
						throw new RIFServiceException(
							RIFServiceError.GRAPHICS_IO_ERROR,
							ioException.getMessage() + "; in: topojson[0-300]=" + result.substring(1, 300));
					}
				}
				else {
					return result;
				}
			}
			else {
				throw new RIFServiceException(RIFServiceError.INVALID_TILE_TYPE, "Invalid tileType: " + tileType);
			}
			
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
		} catch(RIFServiceException exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTiles",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			throw exception;
		} finally {
			//Cleanup database resources
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(resultSet);

			SQLQueryUtility.close(statement2);
			SQLQueryUtility.close(resultSet2);
		}
	}
	
	// https://gis.stackexchange.com/questions/245875/convert-geojson-to-png
	private String geoJson2png(
		final JSONObject tileGeoJson, 
		final JSONArray bboxJson, 
		final String geography,
		final Integer zoomlevel, 
		final String geoLevel, 
		final Integer x, 
		final Integer y) throws IOException, JSONException {
			
		FeatureJSON featureJSON = new FeatureJSON();
		InputStream is = new ByteArrayInputStream(tileGeoJson.toString().getBytes());
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		// Convert GeoJSON toFeatureCollection 	
		FeatureCollection features = featureJSON.readFeatureCollection(is);
		
		// Style 
		MapContent mapContent = new MapContent();
		mapContent.setTitle(geoLevel + "/" + zoomlevel + "/" + x + "/" + y + ".png");
		Style style = SLD.createSimpleStyle(features.getSchema());
		Layer layer = new FeatureLayer(features, style);
		mapContent.addLayer(layer);
	
		int w = 256;
		int h = 256;
		// bboxJson is: [minX, minY, maxX, maxY] 
		ReferencedEnvelope bounds = new ReferencedEnvelope(
				bboxJson.getDouble(0) /* xMin: West */,
				bboxJson.getDouble(2) /* xMax: East */,
				bboxJson.getDouble(1) /* yMin: South */,
				bboxJson.getDouble(3) /* yMax: North */,
				DefaultGeographicCRS.WGS84
			);	
		BufferedImage bufferedImage = new BufferedImage(w, h, 
			BufferedImage.TYPE_INT_ARGB); // Allow transparency [will work for PNG as well!]
		Graphics2D g2d = bufferedImage.createGraphics();

		mapContent.getViewport().setMatchingAspectRatio(true);
		mapContent.getViewport().setScreenArea(new Rectangle(Math.round(w), Math.round(h)));
		mapContent.getViewport().setBounds(bounds);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Rectangle outputArea = new Rectangle(w, h);

		GTRenderer renderer = new StreamingRenderer();
		LabelCacheImpl labelCache = new LabelCacheImpl();
		Map<Object, Object> hints = renderer.getRendererHints();
		if (hints == null) {
			hints = new HashMap<>();
		}
		hints.put(StreamingRenderer.LABEL_CACHE_KEY, labelCache);
		renderer.setRendererHints(hints);
		renderer.setMapContent(mapContent);
		renderer.paint(g2d, outputArea, bounds);
		ImageIO.write(bufferedImage, "png", os);

		String result=Base64.getEncoder().encodeToString(os.toByteArray());
		
		mapContent.dispose();
		g2d.dispose();
		
		return result;
	}
	
	/*
	 * Function: 		topoJson2geoJson()
	 * Description: 	Convert TopoJSON to GeoJSON
	 * 					Store result in <myTileTable>.optimised_geojson is the column exists and is NOT NULL
	 *					As there is no Java way of converting TopoJSON to GeoJSON like topojson.feature(topology, object)
	 *					https://github.com/topojson/topojson-client/blob/master/README.md#feature
	 *					Various Java libraries are immature, unmaintained and undocumented.
	 *					GDAL can convert, but again it is not documented, especially GDAL Java.
	 *
	 *					The choosen method here is to populate <myTileTable>.optimised_geojson by parsing the area_ids from
	 *					the topoJSON properties and then fetching the GeoJSON from <myGeometryTable>.
	 *
	 *					This will eventually be done by the tileMaker.
	 *	 
	 * Returns:			GeoJSON as JSONObject
	 */
	private JSONObject topoJson2geoJson(
		final Connection connection,
		final JSONObject tileTopoJson, 
		final String tileType,
		final String myTileTable, 
		final String myGeometryTable, 
		final String geography,
		final Integer zoomlevel, 
		final String geoLevel, 
		final Integer x, 
		final Integer y)
			throws SQLException, JSONException
	{
		/*
		{
			"transform": {
				"scale": [1.2645986036803175E-4, 1.964578552911635E-4],
				"translate": [-6.151058640054187, 53.198324512334224]
			},
			"objects": {
				"collection": {
					"type": "GeometryCollection",
					"bbox": [-8.649433731630149, 49.87112937372648, 1.7627739932037385, 60.84572000540925],
					"geometries": [{
							"type": "GeometryCollection",
							"properties": {
								"gid": 1,
								"area_id": "UK",
								"name": "United_Kingdom",
								"geographic_centroid": {
									"type": "Point",
									"coordinates": [-4.03309, 55.8001]
								},
								"x": 0,
								"y": 0,
								"SCNTRY2011": "UK",
								"zoomlevel": 0
							},
							"id": 1,
							"geometries": [{
									"type": "Polygon",
									"arcs": [[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]]
								}, ...
							]
						}
					],
						...
				}
			},
			"bbox": [-6.151058640054187, 53.198324512334224, -4.886586496234238, 55.16270660739057],
			"type": "Topology",
			"arcs": [[[95, 7301], [3, -10], [38, -13], [5, -6], [2, -9], [5, -10], [10, -7], [13, -4], [19, -3], [15, -6], [21, -5], [6, -2], [5, -11], [-4, -8], [33, 11], [26, 3], [13, 4], [16, 1], [24, -3], [10, -4], [14, -3], [6, -2], [10, -7], [41, -6], [66, -6], [23, -2], [22, 1], [25, -2], [12, -2], [9, -6], [14, -3], [23, 0], [39, -6], [11, 0], [8, -3], [18, 1], [12, -14], [7, 0], [1, 7], [6, 0], [14, -6], [11, -3], [15, -2], [11, 2], [23, -8], [1, -3], [-6, -4], [-1, -8], [-5, -6], [-10, -6], [-3, -5], [7, -7], [13, -3], [-4, -5], [-19, -5], [-24, -3], [-29, 5], [-9, -1], [-22, 4], [-13, 6], [-21, 1], [-11, -1], [-18, 1], [-7, 1], [-11, -2], [-14, 1], [-11, 1], [-3, 4], [4, 4], [-3, 4], [-13, -2], [-12, -3], [-18, -2], [-7, -4], [-17, -5], [-7, 1], [-1, -9], [-3, -7], [20, -17], [-12, -3], [-15, -7], [185, -85], [30, -8], [32, -6], [53, -8], [42, -4], [90, -9], [19, -1], [26, 1], [35, 4], [18, 4], [122, 31], [18, 5], [58, 14], [-4, -8], [-5, -4], [20, -11], [11, -7], [23, -12], [56, -32], [15, -9], [77, -44], [20, -11], [30, -17], [9, -6], [64, -36], [41, -51], [8, -9], [20, 5], [38, 6], [29, 2], [34, -3], [93, -4], [52, 1], [12, 0], [-6, 10], [-3, 12], [3, 4], [8, 1], [28, -8], [17, 2], [3, 20], [-5, 9], [3, 23], [-13, 13], [0, 4], [28, 29], [9, 5], [22, 8], [110, 28], [55, 15], [21, 5], [41, 6], [22, 2], [12, 0], [25, -2], [18, 2], [43, 16], [32, 14], [18, 11], [16, 5], [29, 11], [26, 14], [13, 9], [7, 3], [45, 6], [25, 2], [15, 3], [44, 17], [18, 2], [72, 2], [52, 2], [16, -19], [8, -5], [27, -9], [13, -7], [52, -35], [17, -11], [4, -7], [-5, -10], [4, -34], [8, -5], [67, -13], [53, -8], [26, -4], [30, -3], [32, -9], [2, -1], [35, -4], [22, -5], [8, -1], [319, 5], [-4, 21], [0, 16], [197, 4], [-8, 72], [7, 2], [46, 2], [11, 4], [8, 5], [0, 7], [-10, 12], [3, 7], [13, 3], [1, 6], [-29, 13], [20, 8], [11, 9], [15, 6], [15, 4], [25, 3], [55, 11], [15, 4], [17, 10], [16, 3], [11, 6], [25, 4], [25, 8], [-2, -4], [-6, -3], [-13, -12], [16, -14], [9, -2], [15, -9], [1, -5], [17, -3], [4, -6], [-2, -4], [-12, -9], [4, -9], [0, -10], [-2, -13], [-14, -4], [-1, -9], [18, -4], [11, -10], [10, -5], [9, -10], [-1, -7], [-11, -4], [-6, -4], [9, -10], [11, -3], [21, -9], [1, -5], [16, -6], [7, -1], [13, 0], [8, -2], [3, -6], [8, -5], [-1, -13], [-4, -8], [11, -12], [2, -15], [7, -2], [38, -1], [22, -4], [9, -5], [26, -6], [9, -13], [4, -4], [16, -5], [10, -6], [22, -4], [5, 0], [39, -7], [12, 0], [13, 2], [14, 0], [3, -2], [2, -10], [7, -8], [-1, -6], [-9, -16], [3, -20], [83, 2], [82, 29], [59, 34], [42, 8], [190, 12], [189, 27], [83, 18], [126, 10], [194, -12], [152, -21], [256, 4], [147, 19], [84, 10], [154, -38], [47, -31], [170, 3], [125, 27], [82, 25], [14, 64], [-47, 31], [-48, 39], [-5, 40], [78, 57], [-7, 56], [-68, 31], [-69, 39], [-73, 70], [40, 25], [56, 65], [144, 42], [-4, 32], [-113, 46], [-4, 32], [106, 10], [-74, 78], [-132, 22], [-52, 71], [-55, 95], [90, 137], [369, 135], [11, 88], [-10, 79], [76, 74], [122, 58], [12, 72], [12, 80], [-73, 63], [-33, 95], [59, 41], [-142, 101], [-29, 56], [102, 49], [-29, 56], [-127, -19], [-113, 46], [-31, 72], [140, 82], [80, 49], [36, 57], [14, 64], [80, 49], [148, 27], [200, -45], [282, -11], [107, 10], [62, 25], [16, 2], [-4, 7], [-8, 5], [-14, 5], [5, 6], [16, 2], [23, 7], [-1, 4], [-6, 2], [-11, 10], [13, 7], [2, 8], [5, 5], [-1, 8], [18, 2], [19, -4], [11, 2], [5, 4], [24, 3], [25, 0], [20, 9], [3, 4], [11, 2], [35, 4], [10, 1], [8, -5], [13, 1], [60, -5], [23, -3], [20, 7], [9, 2], [18, 1], [8, 3], [15, 1], [4, 4], [-3, 3], [-9, 4], [24, 2], [7, 3], [14, 12], [-11, 10], [7, 9], [-26, 4], [-18, 1], [-23, 7], [-13, 2], [-13, 7], [-13, 4], [-9, 1], [-19, 0], [-23, 4], [-14, 0], [-7, 4], [1, 7], [9, 11], [-18, 11], [-12, 4], [-4, 4], [-18, 4], [-21, 2], [-9, 6], [-27, 7], [-9, 1], [-31, -3], [-37, -1], [-22, 3], [1, 6], [-6, 6], [0, 19], [4, 14], [3, 4], [10, 20], [5, 5], [12, 3], [-6, 3], [-25, 4], [-22, 0], [-15, 2], [-17, 4], [-17, 0], [-36, 8], [-1, 4], [11, 3], [21, 12], [28, 6], [10, 4], [6, 9], [-10, 10], [-28, 8], [-7, 1], [-6, 5], [-1, 7], [-7, 6], [1, 12], [-42, 13], [-15, 6], [0, 3], [-8, 6], [-8, 2], [-28, -2], [-20, -4], [-13, 1], [-8, 4], [-3, 9], [-15, 4], [-21, -5], [-12, 1], [-10, 8], [-11, 3], [-21, 1], [-16, 3], [-23, -1], [-12, 0], [-17, 3], [-28, 1], [-28, 6], [-5, 6], [-18, 0], [-1, 7], [-24, 3], [-15, -2], [-9, -6], [-19, -3], [-13, 4], [-19, -1], [-11, -9], [1, -4], [-6, -5], [-13, 1], [-4, 3], [-25, 1], [-21, -2], [-17, 0], [-21, 8], [-25, 5], [-19, 5], [-17, 0], [-21, -4], [-31, -2], [-14, -4], [-12, -5], [-12, 0], [-10, -1], [-20, 0], [-33, -9], [-32, 4], [-18, 0], [-4, 5], [-13, 5], [-9, 5], [-13, 14], [-3, 5], [-13, 1], [-17, 7], [-26, 3], [-7, 6], [-14, 3], [-12, 4], [0, 4], [-36, 6], [-23, 4], [-23, 0], [-16, 2], [-4, 5], [-18, 7], [-16, 10], [-22, 7], [-21, 3], [-41, 1], [-17, -3], [-21, 1], [-35, -7], [-10, -1], [-15, 3], [-24, 0], [-24, -2], [-40, -12], [-26, -3], [-12, -6], [-19, -3], [-34, -1], [-30, -2], [-38, -6], [-10, -3], [-3, -6], [4, -6], [16, -9], [4, -4], [2, -8], [-3, -10], [-30, -14], [-3, -3], [-4, -8], [-8, -4], [-20, -6], [-11, -2], [-20, -8], [-9, -8], [-37, 0], [-5, -2], [-11, -6], [-2, -5], [5, -3], [16, -3], [1, -4], [-16, -5], [-13, -12], [-21, -7], [-8, -5], [1, -10], [7, -11], [-7, -9], [8, -11], [10, 2], [9, 1], [5, -3], [17, 0], [14, -2], [4, 0], [14, -7], [16, -3], [13, -2], [6, 3], [12, -3], [7, -4], [10, -11], [8, -5], [19, -5], [10, -9], [-6, -12], [2, -5], [-9, -5], [-10, 7], [-24, 7], [-27, -2], [-10, -2], [-12, 2], [-18, 8], [-17, -7], [-23, -6], [-5, 1], [-9, 11], [-16, 4], [-6, 5], [-20, 2], [-21, 8], [-15, 3], [-15, 1], [-16, 5], [-17, -1], [-14, -5], [-17, -2], [-21, 0], [-7, -12], [-17, -4], [-17, 0], [-3, -1], [8, -6], [-4, -3], [-19, -4], [-5, -4], [-29, -4], [-11, -9], [-57, -2], [-31, -3], [-39, -6], [-10, 6], [-14, 16], [-6, 4], [-30, 8], [-3, 9], [-7, 7], [-27, 7], [-6, 5], [-6, 8], [-10, 5], [-36, 11], [-8, 4], [-2, 4], [4, 10], [-40, 4], [-7, 5], [-7, 2], [-17, -2], [-11, 0], [-10, -2], [-2, -8], [0, -12], [-2, -3], [-23, -18], [-7, -3], [-25, -2], [-25, -6], [-23, -7], [-17, -6], [-8, 3], [-22, -1], [-13, 1], [-16, -4], [-3, -3], [-20, -3], [-1, -5], [-11, -2], [-10, -5], [-21, -4], [-30, 0], [-17, 2], [-13, 0], [-5, 1], [-1, 5], [-13, 6], [-9, 1], [-18, 0], [-16, -2], [-15, 0], [-14, -2], [-18, -4], [-32, -6], [-10, 1], [-20, 4], [-15, 2], [-29, 0], [-23, 4], [-26, 0], [-29, 2], [-8, -2], [-9, -6], [-27, -17], [-8, -6], [-23, -16], [-12, -9], [-7, -1], [-11, 3], [-10, 4], [-2, 3], [-10, 3], [-2, 6], [-17, 2], [-36, 0], [-21, 6], [-50, -2], [-11, -6], [-13, -2], [-13, 1], [-14, 2], [-8, 3], [-6, 11], [-13, 17], [-2, 9], [-11, 10], [-12, 6], [-14, 4], [-12, 5], [-35, 6], [-8, 10], [-7, 5], [-1, 6], [-21, 2], [-9, -3], [-5, -4], [5, -8], [-2, -5], [-30, -12], [-8, -1], [-17, 0], [-16, -3], [-2, -8], [-11, -7], [-26, -1], [-11, 0], [-17, -2], [-22, -6], [-31, 0], [-4, -13], [-6, -4], [-27, -14], [-34, 0], [-15, -6], [-17, -2], [-5, -9], [7, -12], [-2, -4], [-22, -2], [-15, -6], [6, -6], [-4, -4], [2, -4], [-3, -5], [-15, -9], [-4, -5], [-14, -5], [-17, -3], [-6, -2], [7, -6], [20, -10], [27, -9], [5, -6], [-11, -7], [-13, -11], [-13, -7], [-2, -4], [3, -11], [-6, -10], [3, -9], [14, -9], [3, -6], [-2, -4], [-11, -7], [-33, -15], [-31, -12], [-17, 1], [-14, -4], [-11, 0], [-6, -2], [-12, -11], [18, -14], [-17, -12], [-24, -4], [-37, -12], [-7, -2], [10, -10], [-8, -10], [-11, -9], [0, -5], [-14, -17], [-11, -5], [-17, -5], [0, -32], [12, -3], [-12, -8], [-14, -13], [-11, -4], [-6, -3], [-8, -2], [-9, -8], [-8, -3], [3, -2], [-11, -12], [-18, -2], [-29, -21], [-23, -11], [-23, -6], [-11, -10], [-14, -7], [-11, -2], [-7, -5], [-16, -10], [-12, -5], [-7, -10], [-13, -6], [-23, -3], [-5, -2], [-3, -6], [-11, -2], [-40, -5], [-21, 1], [-23, -5], [-18, 0], [-18, 2], [-37, -3], [-26, 0], [-9, 2], [-33, -1], [-21, 1], [-18, -2], [-30, -22], [-27, -5], [-27, -4], [-10, -6], [-3, -8], [-22, -3], [-8, -2], [-11, -5], [-3, -5], [-11, -4], [-33, -3], [-12, 5], [-35, 3], [-14, -1], [-12, 0], [-14, 2], [-18, 10], [-26, 7], [-8, 5], [-10, 1], [-26, -3], [-20, 2], [-22, 1], [-14, 2], [-8, -1], [-12, -4], [-14, -13], [-25, -9], [-30, -1], [-1, -2], [-13, -5], [-9, -8], [-14, -5], [-21, -2], [-10, 2], [-11, 4], [-21, -1], [-8, 2], [-11, 1], [-14, -6], [-25, -2], [-22, 1], [-16, 0], [-19, -7], [-21, 0], [-12, 7], [-14, 2], [-7, -3], [0, -7], [-6, -16], [4, -9], [-9, -5], [3, -8], [10, -5], [-9, -5], [-14, -3], [-5, -4], [5, -7], [-6, -4], [-26, -8], [-7, -6], [-12, -2], [-3, -2], [1, -15], [-17, -5], [1, -10], [6, -3], [-10, -2], [-2, -4], [1, -13], [4, -3], [-3, -13], [-5, -2], [-19, -2], [-6, -2], [-1, -4], [3, -7], [12, -5], [-3, -6], [-8, -7], [11, -8], [6, -1], [-6, -15], [-17, -3], [-19, 0], [-11, -3], [-11, -5], [-5, 0], [-23, 2], [-18, -4], [-14, -12], [2, -5], [-11, 0], [-11, -1], [-11, -5], [-16, -2], [-35, 6], [-8, -6], [-8, -2], [-3, -4], [-13, -3], [-11, 1], [-11, -6], [-2, -7], [-11, -3], [-8, -4], [-15, -1], [-15, 1], [-8, -5], [-9, -2], [-22, 2], [-28, -1], [-18, 4], [-34, 6], [-5, 2], [0, 10], [-6, 5], [-9, 2], [-25, 1], [-22, 0], [-22, 3], [-15, -4], [-17, -1], [-13, 2], [-7, 2], [-37, -2], [-5, -1], [-53, 3], [-5, 2], [-15, 0], [-20, -4], [-8, -6], [-16, -6], [-4, -4], [-13, -10], [-16, -10], [-18, -5], [-8, -1], [-11, -4], [-22, -2], [-26, 1], [-22, -7], [-8, -1], [-13, -4], [-18, -16], [-10, -4], [-5, -3], [-12, -5], [-19, -4], [-13, 3], [-14, -4], [-29, -3], [-25, -5], [-6, -4], [-11, -3], [-23, 1], [-29, -4], [-18, -1], [-27, 12], [-19, 3], [-30, -6], [-6, -4], [-17, -6], [-40, 5], [-46, 3], [-6, -2], [-4, -6], [-14, -5], [-17, -11], [-9, -1], [-23, -2], [-28, -4], [-13, 0], [-45, -7], [-72, -23], [-20, -7], [-10, -6], [-12, -3], [-8, -7], [-11, -4], [-17, -2], [-17, -6], [-9, -5], [-2, -7], [-18, -14], [-18, -6], [6, -11], [-15, -4], [-19, -1], [-29, -5], [-8, -8], [-16, -5], [-13, -6], [8, -5], [14, -4], [2, -2], [-15, -9], [-7, 0], [-10, -3], [0, -9], [3, -3], [-13, -5], [6, -8], [32, -3], [10, -6], [-13, -13], [2, -6], [-12, -8], [-5, -8], [-19, -7], [-1, -7], [-24, -4], [-22, -11], [-8, -1], [-28, -17], [-18, -10], [4, -5], [20, -7], [-23, -3], [-26, 4], [-18, -2], [-12, -9], [-13, -3], [-8, -4], [-10, -1], [-5, -5], [-13, -2], [-14, 0], [-25, -1], [-36, 2], [-18, -6], [-10, -2], [-12, -7], [-23, -4], [-2, -9], [0, -8], [-2, -7], [3, -10], [13, -6], [13, -3], [6, -10], [6, -5], [12, -5], [4, -4], [14, -3], [-4, -8], [0, -14], [9, -14], [-4, -4], [0, -10], [9, -7], [-5, -9], [4, -2], [21, -5], [7, -8], [8, -5], [12, -4], [27, 1], [13, -5], [6, -3], [16, -5], [30, 12], [10, 2], [13, -2], [31, 1], [30, -6], [5, 1], [29, 3], [30, -1], [15, -2], [21, -5], [4, -7], [25, -3], [25, -10], [24, -1], [18, 4], [17, 2], [38, -1], [11, 1], [21, 3], [20, 5], [22, 8], [10, 2], [17, 0], [27, -7], [12, -6], [23, -5], [20, -1], [-2, -9], [-4, -5], [1, -5], [5, -6], [17, -11], [1, -4], [-9, -10], [1, -2], [-7, -10], [-3, -9], [-8, -7], [-1, -4], [7, -4], [0, -11], [-8, -5], [3, -6], [7, -8], [-4, -12], [-5, -8], [6, -11], [1, -5], [-13, -7], [-2, -5], [2, -4], [-2, -9], [16, -1], [38, 3], [13, -1], [11, -3], [17, 1], [36, -3], [17, -5], [-2, -3], [7, -4], [6, -9], [8, -4], [17, -3], [14, 0], [17, -11], [0, -9], [-8, -4], [-8, -9], [3, -7], [7, -3], [-1, -7], [-7, -8], [2, -10], [6, -4], [9, -2], [28, -1], [8, -8], [-1, -5], [26, -2], [10, -1], [14, -9], [1, -4], [18, -9], [2, -10], [9, -12], [10, -5], [12, -4], [10, -4], [18, -16], [20, -6], [9, -4], [21, -7], [7, -7], [35, -6], [8, -9], [15, -4], [0, -8], [29, -21], [1, -9], [14, -10], [7, -11], [-9, -9], [3, -6], [-4, -5], [-31, -6], [-32, 1], [-8, 3], [-4, -1], [-6, -9], [-12, -6], [-10, -4], [-10, -5], [-2, -5], [-9, -8], [6, -6], [-9, -1], [-14, -7], [-2, -6], [5, -5], [-20, -8], [-3, -9], [-10, -7], [1, -9], [-11, -7], [0, -12], [-13, -3], [-13, -12], [-28, -15], [-6, -8], [-11, -3], [-3, -6], [-17, -10], [-13, -3], [-28, -5], [-33, -2], [-3, -5], [11, -11], [2, -8], [-4, -5], [3, -5], [22, -3], [10, -8], [27, 0], [-2, -10], [-9, -6], [-23, -10], [-1, -3], [5, -6], [2, -9], [-2, -8], [-6, -3], [-26, -9], [3, -5], [-8, -7], [-1, -8], [-10, -12], [-12, -2], [-10, -4], [-19, -2], [-13, -8], [-16, -5], [-7, -4], [-4, -8], [-31, -6], [-19, 2], [-8, -1], [-17, -6], [-49, -4], [-37, -5], [-35, -3], [-16, -6], [-28, -1], [-30, -4], [-12, 2], [-25, 1], [5, 10], [-1, 2], [-11, 1], [-5, 6], [-13, 2], [-44, -3], [-14, 1], [-4, 3], [-54, 0], [-4, 2], [-21, 4], [-14, 7], [-12, 0], [-20, 0], [-13, 1], [-10, 2], [-15, 0], [-4, 7], [2, 4], [-4, 3], [-13, 1], [-3, 2], [-28, 1], [-18, 3], [-2, 11], [1, 6], [-3, 4], [-15, 4], [-10, 5], [-9, 2], [1, 4], [-6, 5], [-10, 2], [-4, 7], [-13, 7], [-22, 1], [-17, 4], [-5, -7], [-3, -9], [1, -12], [-5, -11], [-8, -10], [-1, -6]], [[1191, 3498], [58, -11], [33, -5], [131, -18], [28, -3], [19, -1], [19, -3], [18, -4], [14, -7], [26, -8], [53, -7], [6, 4], [19, 6], [44, 10], [21, 8], [20, 6], [17, 10], [23, 6], [12, 5], [10, 6], [19, 6], [13, 0], [22, 4], [28, 8], [15, 7], [0, 2], [15, 6], [37, 8], [17, 2], [35, 4], [17, 3], [14, 5], [6, 9], [31, 13], [12, 6], [21, 9], [21, 6], [24, 5], [9, 6], [19, 3], [24, 2], [15, 3], [18, 7], [28, 10], [22, 5], [7, 3], [-5, 7], [2, 3], [8, 2], [5, 5], [24, 4], [48, 20], [10, 2], [17, 2], [30, 6], [19, 3], [35, 3], [18, 4], [27, 1], [25, 4], [18, 1], [18, 2], [46, 10], [8, 0], [16, -2], [21, -7], [23, 1], [19, -1], [33, -5], [12, -5], [1, -3], [7, -1], [1, 4], [13, 9], [13, 3], [24, 0], [22, -2], [26, 2], [46, 1], [14, 2], [21, 5], [15, 1], [27, 0], [19, -2], [10, 3], [17, 2], [9, 2], [6, 9], [11, 4], [14, 2], [9, 5], [13, 3], [43, 8], [30, 9], [13, 6], [10, 3], [9, 5], [11, 3], [5, 3], [0, 7], [3, 2], [1, 9], [11, 5], [6, 11], [11, 11], [-2, 7], [9, 10], [1, 10], [7, 9], [2, 5], [12, 12], [7, 6], [-3, 3], [-14, 7], [3, 3], [11, 4], [3, 6], [9, 8], [-4, 9], [10, 7], [3, 9], [-6, 8], [-13, 10], [-3, 15], [-4, 6], [-5, 16], [-4, 4], [0, 11], [-12, 11], [-2, 9], [-13, 12], [5, 5], [2, 5], [10, 8], [17, 6], [4, 5], [-4, 11], [4, 4], [20, 8], [21, 7], [7, 5], [16, 1], [16, 3], [11, -1], [20, 0], [13, -5], [31, 0], [21, -3], [22, 3], [39, 11], [9, 4], [6, 5], [13, 6], [9, 6], [9, 0], [4, -5], [-11, -8], [4, -11], [32, 0], [17, -4], [21, -1], [8, 0], [-2, 13], [9, 0], [12, -4], [7, 0], [8, 9], [12, 6], [7, 0], [0, -4], [6, 0], [17, 6], [26, 7], [13, 0], [15, 3], [29, 2], [20, 4], [4, 4], [51, 8], [22, 7], [9, -2], [26, 0], [20, -3], [19, -6], [10, 0], [14, 3], [47, 14], [31, 7], [21, 9], [7, 4], [6, 1], [3, -4], [-16, -13], [5, -4], [11, 3], [4, -2], [-1, -4], [9, -3], [14, -1], [25, 4], [8, -2], [-4, -4], [5, -4], [13, -2], [7, 1], [15, 6], [19, 3], [4, -3], [-11, -4], [-1, -2], [14, -2], [4, -3], [11, -3], [33, -5], [3, -5], [17, -5], [14, 1], [6, -2], [-3, -4], [12, -1], [30, -1], [4, -1], [-3, -15], [-5, -3], [-19, -6], [3, -14], [13, -6], [47, -8], [26, -1], [-19, 7], [-25, 5], [-16, 4], [2, 4], [5, 3], [2, 6], [11, 9], [13, 4], [2, 9], [9, 5], [-7, 10], [-19, 3], [15, 12], [-3, 5], [19, 13], [-1, 11], [-7, 6], [-26, 11], [-8, 0], [-15, 5], [-4, 8], [-10, 6], [-12, 1], [-13, 4], [-12, 7], [-2, 9], [5, 3], [-1, 6], [-18, 8], [-25, 6], [-1, 4], [-17, 12], [-11, 1], [-28, 17], [-13, 2], [-19, 8], [-10, 10], [-9, 4], [-7, 0], [-4, 31], [-4, 4], [-9, 65], [-27, 10], [-9, 59], [-74, 15], [-3, 0], [-63, 13], [-6, 1], [-29, 14], [-59, 29], [-88, 46], [-9, 4], [-82, 27], [15, 10], [9, 10], [7, 14], [29, 70], [19, 44], [-207, -4], [66, 36], [3, 3], [71, 38], [110, 3], [113, 1], [156, 4]], [[4039, 4871], [-2, 9], [30, 4], [-1, 19], [-35, 16], [-2, 13], [-8, 4], [2, -15], [-28, 10], [-27, 22], [0, 5], [6, 5], [-2, 4], [12, 16], [48, 25], [5, 3], [2, 6], [7, 8], [10, 8], [10, 10], [11, 4], [6, 0], [-31, 11], [15, 17], [-19, 13], [43, 20], [3, 20], [-21, 8], [8, 23], [-22, 28], [20, 41], [-6, 6], [-2, 6], [15, 34], [-15, 19], [56, 71], [-30, 13], [-9, 9], [-1, 16], [8, 17], [20, 5], [15, 5], [16, 4], [57, 12], [54, 9], [26, -6], [29, -3], [41, -5], [26, -3], [11, -6], [15, -5], [10, -1], [84, -7], [42, 1], [23, -3], [23, 2], [11, -3], [19, 2], [33, -1], [7, 1], [20, 9], [-1, 7], [-4, 7], [16, 1], [4, 5], [24, 11], [3, 8], [5, 6], [-5, 8], [-13, 6], [-3, 4], [-16, 35], [-34, 27], [-16, 3], [-12, 4], [-12, 11], [-2, 8], [26, 5], [11, 5], [49, 17], [14, 8], [14, 5], [13, 7], [26, 8], [49, 6], [13, 4], [6, 5], [13, -1], [14, 2], [14, 4], [6, 9], [24, -2], [9, 2], [7, 6], [31, 10], [7, 9], [5, 0], [15, -2], [11, 0], [34, 10], [8, -1], [0, -3], [25, 7], [9, 1], [38, 1], [34, 3], [12, 2], [28, 7], [11, 2], [28, 1], [21, 5], [18, 0], [19, 1], [17, 4], [57, 13], [11, -1], [29, 2], [12, -2], [12, -5], [29, -4], [19, -2], [11, -4], [97, -5], [6, 4], [19, 4], [11, 1], [30, 8], [13, -1], [18, -7], [15, -2], [16, 1], [79, 8], [33, -1], [7, 5], [10, 14], [14, 5], [33, 4], [60, 9], [21, -1], [21, 4], [36, 4], [36, 3], [36, 0], [11, 2], [12, 4], [29, 6], [25, 3], [31, 4], [-15, 17], [-8, 7], [6, 37], [26, 9], [115, 7], [24, 6], [38, 21], [39, 4], [60, 1], [19, 0], [11, -4], [12, 1], [7, 6], [20, 11], [-12, 11], [5, 6], [-6, 18], [4, 11], [0, 9], [-5, 10], [2, 10], [30, 4], [66, -9], [14, 1], [50, -1], [-1, 11], [30, 9], [42, 2], [16, -2], [21, -3], [21, -11], [19, -8], [23, -1], [25, -14], [21, -6], [10, -5], [8, -5], [14, -4], [47, -6], [47, 2], [146, 39], [-10, 37], [68, 6], [19, 2], [32, 6], [13, 12], [-32, 4], [-40, -3], [8, 15], [-10, 14], [7, 9], [-32, 11], [21, 15], [-5, 6], [-73, 11], [-14, 3], [-23, 5], [-16, 1], [-41, 9], [-55, 14], [-7, 9], [-13, 8], [-16, 3], [-36, -1], [-22, 1], [-4, 11], [7, 23], [-16, 5], [-46, 2], [-55, 11], [-13, 0], [-11, 2], [-15, 9], [-13, 10], [-28, 7], [-48, 15], [-10, 7], [23, 17], [9, -2], [13, -2], [27, -4], [11, -1], [27, -5], [21, -3], [29, -5], [26, -6], [33, -2], [8, -3], [23, -1], [24, 6], [16, 9], [15, 27], [84, 23], [5, 3], [33, 9], [11, 5], [6, 6], [18, 44], [14, 14], [-17, 12], [9, 11], [-21, 16], [0, 10], [-20, 49], [-9, 6], [-17, 7], [-4, 6], [0, 10], [4, 7], [1, 6], [-7, 3], [-43, 4], [-2, 22], [81, 13], [10, 14], [34, 10], [12, 13], [47, 0], [14, 8], [62, 3], [54, 86], [-10, 8], [-12, 13], [-2, 9], [24, 71], [-321, -36], [-2, -3], [9, -4], [10, -10], [0, -8], [-6, -8], [-9, -7], [-3, -10], [0, -13], [-5, -7], [-7, -4], [-9, -2], [-23, 1], [-15, -2], [-4, -8], [3, -3], [0, -8], [4, -3], [-4, -12], [3, -8], [11, -5], [-2, -6], [-13, -7], [3, -3], [-8, -3], [4, -4], [-7, -6], [-18, -5], [-3, -7], [-9, -2], [1, -10], [-4, -3], [3, -5], [-9, -2], [-5, -10], [-15, -7], [-10, -7], [-9, -1], [-3, -4], [-14, -3], [-6, -5], [4, -4], [-8, -7], [-21, -6], [5, -6], [-12, -18], [3, -4], [-11, -10], [-1, -3], [4, -11], [10, -3], [-2, -3], [14, -8], [-3, -7], [1, -12], [-9, -11], [-4, -3], [-16, -6], [-1, -5], [-20, -6], [-3, -4], [-15, -4], [-23, -9], [-8, -4], [-6, -1], [-16, -6], [-4, -5], [-10, -8], [-9, -4], [-15, -3], [-13, 0], [-20, -6], [-10, -1], [-18, 0], [-17, -1], [-13, 3], [-19, -1], [-31, 1], [-16, 3], [-35, -2], [-7, 2], [-9, -3], [-12, 0], [-8, -2], [-35, -1], [-6, 2], [-21, 1], [-8, -1], [-9, -3], [-12, 1], [-15, -2], [-19, -7], [-13, -2], [-20, -6], [0, -5], [-5, -5], [-10, -4], [-2, -3], [3, -5], [-7, -7], [-13, -6], [-1, -2], [-13, -5], [-1, -3], [-29, -12], [-11, -3], [-36, -6], [-32, -3], [-33, 3], [-16, 5], [-41, -2], [-30, 3], [-22, 6], [-16, 1], [-12, 4], [-10, 2], [-11, 0], [-6, 1], [-25, -1], [-24, 4], [-21, 7], [-28, 4], [-5, 4], [-9, 1], [-9, 4], [-30, 3], [-9, 2], [-7, 5], [-18, 2], [-17, 4], [-5, 3], [-12, 1], [-8, 4], [-7, 1], [-9, 5], [-16, 5], [-12, 0], [-21, 3], [-7, 2], [-12, -1], [-10, -2], [-46, -9], [-28, -3], [-13, 1], [-16, 3], [-15, 0], [-24, 4], [-17, -1], [-46, 2], [8, 6], [10, 3], [14, 8], [-41, 6], [-50, 6], [-79, 5], [-20, 4], [-18, 5], [-17, 7], [-36, 20], [-39, 16], [-23, 8], [-42, 16], [-11, 4], [-51, 19], [-18, 4], [-13, 1], [-21, 0], [-79, -1], [-22, 0], [-20, 1], [-43, 7], [-41, 10], [-13, 2], [-25, 2], [-145, 9], [-88, 5], [-26, -1], [-20, -1], [-25, -4], [-25, -10], [-23, -4], [-13, -3], [-26, -13], [-9, -9], [-6, -12], [-5, -6], [-16, -9], [-14, -3], [-27, -2], [-21, 3], [-89, 17], [-18, 5], [-40, 9], [-29, 3], [-35, 1], [-45, 0], [-31, 2], [-63, 14], [-53, 11], [-21, 7], [-55, 24], [-16, 5], [-13, 3], [-14, -12], [-7, -2], [-64, 1], [0, -4], [3, -18], [2, -2], [41, -26], [4, -10], [-151, -3], [-277, -6], [-6, 1], [-23, 5], [-42, 3], [-14, 5], [-41, 21], [-5, 3], [-10, 12], [-14, -1], [-23, -5], [-51, -10], [-35, -8], [-19, -6], [-47, -34], [14, -1], [54, -2], [25, 0], [37, 3], [13, -1], [5, -11], [10, -5], [18, -4], [15, -3], [49, -5], [20, -2], [20, -6], [28, -13], [11, -5], [16, -3], [18, -8], [-38, -9], [-112, -22], [-57, -9], [-22, 0], [-27, 2], [-81, 10], [-30, -1], [-70, -4], [-31, -4], [-15, -4], [-22, -1], [-72, 1], [-24, 1], [-69, 6], [-17, 1], [-25, 0], [-74, -5], [-21, -1], [-13, 1], [-25, 3], [-17, 1], [-31, 0], [-40, -3], [-73, -1], [-33, 0], [-15, 2], [-24, 4], [-9, -4], [-50, -19], [-70, -27], [13, -5], [-16, -7], [-39, -4], [-10, -4], [1, -8], [-7, -7], [1, -7], [-4, -3], [-15, -2], [-4, -14], [1, -12], [4, -10], [8, -5], [18, -9], [4, 1], [29, -2], [13, -38], [15, -30], [16, -24], [2, -16], [7, -8], [29, -29], [12, -9], [24, -14], [12, -6], [56, -29], [21, -12], [11, -12], [2, -11], [59, 2], [30, 9], [28, 0], [12, 1], [10, -3], [-1, -3], [-16, -1], [-6, -3], [-12, -13], [3, -5], [13, -3], [3, -4], [-1, -9], [12, -4], [16, 0], [3, 2], [-2, 9], [11, -1], [3, -5], [16, -6], [-1, -5], [24, -7], [6, -8], [47, -15], [9, -1], [12, 0], [12, -6], [-1, -9], [4, -3], [3, -12], [14, -5], [19, 0], [11, -5], [-181, -3], [3, -37], [5, -37], [3, -24], [3, -14], [5, -36], [4, -15], [-30, 12], [-30, 14], [-34, 16], [-40, 17], [-35, 16], [-19, 7], [-6, 4], [-17, -5], [-48, -17], [-165, -58], [-115, -39], [-59, -19], [-121, -38], [-24, -10], [-9, -7], [-10, -5], [-11, -4], [-54, -11], [-4, -2], [-9, -9], [1, -6], [12, -12], [5, -7], [1, -5], [-7, -7], [-8, -3], [-15, -3], [-37, -4], [-17, -2], [-43, -9], [-28, -15], [-4, -7], [15, -14], [17, -13], [6, -6], [11, -44], [8, -10], [13, -7], [19, -6], [12, -3], [16, -3], [26, -2], [16, -4], [7, -3], [5, -6], [1, -7], [-12, -5], [-12, -3], [-31, -4], [-54, -8], [-15, -2], [-28, 0], [-25, 3], [-97, 11], [-57, 6], [-11, 1], [-14, -2], [-35, -6], [-29, -8], [-14, -2], [-28, -13], [-8, -1], [-26, -2], [-8, 2], [-19, 9], [-16, 0], [-9, -5], [0, -15], [-6, -8], [-26, -9], [-31, -9], [-13, -6], [-6, -5], [-3, -6], [-1, -12], [-7, -8], [-10, -4], [-17, -5], [-12, -5], [-10, -11], [-11, -5], [-9, -2], [-27, -3], [-8, -7], [1, -3], [8, -7], [1, -3], [-7, -6], [-17, -4], [-11, -5], [-12, -18], [11, -9], [-3, -5], [5, -8], [6, -16], [-5, -7], [-13, -8], [-26, -4], [-9, -7], [-74, -17], [-15, -1], [-7, -7], [-6, -2], [-32, -6], [-32, 0], [-19, -6], [-40, -13], [-9, -2], [-7, -4], [-8, -2], [-20, -3], [-22, -13], [-14, -3], [-15, -2], [-18, -3], [-9, -4], [-24, -3], [-11, -7], [-19, -7], [-26, -14], [-9, -2], [-21, -1], [-29, -5], [-7, -3], [-11, -9], [-6, -9], [-17, -6], [-8, -6], [-24, -5], [-13, -5], [-6, -8], [-6, -3], [-50, -5], [-9, 1], [-19, 0], [-27, -3], [-11, -2], [-7, 0], [14, -2], [15, 4], [8, 0], [-6, -6], [-16, -6], [18, -6], [-5, -4], [-13, 1], [-17, 3], [-16, -2], [2, -4], [11, -4], [-11, 0], [-8, 3], [-16, 3], [-5, -4], [10, -12], [-4, -3], [6, -7], [-2, -9], [9, -5], [10, -3], [8, -4], [-7, -3], [-13, 1], [-9, -1], [13, -1], [21, -7], [19, -4], [5, 4], [37, -7], [-14, 0], [-3, -3], [-27, -7], [30, 0], [13, -4], [9, 2], [14, -3], [7, 1], [9, 5], [9, 0], [-5, -3], [-3, -9], [-12, -7], [3, -3], [-11, -6], [17, 3], [5, 4], [28, 1], [10, -2], [18, 3], [10, -8], [11, -13], [5, -8], [9, -8], [-1, -5], [15, -2], [2, -2], [-14, 1], [-24, 0], [-1, -2], [34, -5], [9, -7], [3, -9], [6, -1], [10, 3], [13, -4], [11, 9], [8, -13], [17, -8], [4, -12], [12, -8], [-1, -3], [-7, -3], [2, -4], [9, -9], [8, -3], [25, -2], [12, -6], [6, -8], [8, -3], [-2, 10], [21, -8], [11, -5], [5, 1], [-13, 13], [20, 3], [22, -1], [10, -2], [15, 0], [20, -3], [11, -1], [14, -4], [26, -11], [10, -6], [12, -13], [6, -11], [0, -4], [12, -7], [10, -15], [9, -12], [9, -5], [5, 1], [14, 7], [16, -3], [9, -6], [0, -10], [-11, -8], [-15, -7], [2, -3], [19, -2], [20, -4], [15, -9], [-7, -12], [-3, -18], [9, -3], [10, -9], [37, -18], [8, -6], [23, -25], [2, -4], [-18, -22], [-3, -12], [8, -19], [-3, -7], [-20, -21], [-16, -12], [-8, -13], [-8, -8], [-9, -5], [-8, -11], [-34, -23], [-4, -6], [0, -10], [-21, -17], [-19, -13], [-18, -9], [-13, -10], [-7, -4], [-9, -2], [-21, -2], [-50, -11], [-21, -4], [-23, -3], [-31, -1], [10, -7], [46, 1], [4, -8], [47, 0], [5, -16], [-30, -1], [35, -16], [53, 0], [20, -8], [19, -5], [17, -7], [11, -1], [27, -7], [19, 0], [37, -16], [30, -10], [15, -4], [8, 1], [13, 4], [-4, -6], [-9, -2], [-10, 2], [-10, -4], [-13, 3], [-1, -3], [15, -6], [9, -1], [22, -6], [5, -3], [15, -5], [6, -3], [15, -3], [36, -12], [24, -1], [12, -3], [18, -1], [22, 0], [26, 2], [28, 2], [18, 0], [8, 2], [14, 6], [24, 2], [3, -2], [-20, -1], [-4, -2], [-2, -4], [-13, -4], [2, -16], [-23, -1], [9, -86], [5, -33], [8, -51], [5, -54], [2, -17], [-104, -1], [-37, -1], [-10, 0], [-53, -2], [-16, -13], [-6, -10], [-15, -9], [-16, -13], [3, -13], [5, -35], [51, 1], [2, -19], [-50, -1], [-3, -38], [-21, -1], [20, -9], [11, -13], [10, -7], [32, -9], [12, -5], [10, -7], [15, -4], [29, -6], [16, -4], [12, -5], [13, -3], [91, -7], [24, -3], [12, -5], [7, -6], [3, -11], [-4, -9], [0, -6], [14, -10], [9, -6], [14, -4]], [[1191, 3498], [-4, -3], [-8, 1], [-20, -6], [-16, -8], [-13, -7], [-15, -6], [-18, -4], [-13, -5], [-10, -15], [-14, -7], [-14, -14], [-22, -3], [-18, -4], [-16, -5], [-45, -17], [-19, -10], [-6, -9], [-8, -5], [-17, -3], [-18, 0], [-17, -3], [-38, -3], [-9, -3], [-7, -9], [-13, -5], [-21, -2], [-14, 1], [-26, -7], [-22, -12], [-17, -7], [16, -3], [1, -5], [10, -4], [-3, -7], [18, -13], [6, -7], [3, -19], [-10, -3], [-23, -4], [-27, -1], [-35, -10], [-9, -8], [2, -6], [-23, 12], [1, -5], [-5, -7], [-12, -11], [-13, -1], [-6, -2], [-32, 4], [-10, -3], [-8, -8], [-16, -6], [-10, -12], [-2, -5], [6, -14], [-5, -7], [-18, -8], [-13, -4], [-23, -3], [-4, -2], [-12, -16], [-19, -7], [-22, -5], [10, -2], [18, 2], [17, -2], [9, -3], [8, -12], [-1, -9], [-21, -10], [-18, -13], [-17, -16], [-8, 0], [-16, 12], [-28, -13], [-11, -6], [-2, -3], [2, -9], [-11, -22], [10, -7], [-68, 15], [-10, 0], [-4, -9], [19, -7], [9, -8], [-8, -3], [-17, -3], [-10, -4], [3, -10], [10, -8], [-2, -4], [9, -4], [22, -5], [18, -8], [28, -9], [10, -7], [2, -4], [17, -14], [3, -13], [3, -11], [11, -13], [-1, -2], [-24, -23], [-1, -3], [1, -10], [-5, -5], [-18, -13], [-12, -4], [-23, -4], [2, -3], [1, -9], [-7, -3], [9, -5], [-1, -6], [5, -6], [13, -4], [1, -2], [-4, -7], [10, 1], [15, -13], [20, -7], [2, -2], [-13, -6], [0, -18], [-4, -7], [-13, -14], [-2, -11], [4, -6], [-3, -4], [7, -4], [2, -6], [6, -8], [14, -12], [25, -17], [16, -7], [12, -11], [13, -7], [4, -6], [13, -14], [-3, -9], [4, -7], [13, -5], [4, -4], [5, -9], [1, -16], [-22, -1], [-24, -4], [-11, -3], [-26, -6], [-19, -3], [-9, -2], [0, -5], [-7, -2], [-4, -5], [-21, -4], [-20, 1], [-21, -4], [-25, -2], [-14, -4], [-7, -5], [-20, -7], [1, -5], [-13, -6], [-10, -4], [-13, -8], [-10, -5], [-16, -6], [-3, -2], [4, -6], [-6, -10], [-6, -3], [-19, -3], [-14, -5], [-11, -7], [-28, -6], [-28, -2], [-13, -3], [12, -18], [-15, -15], [-1, -5], [-19, -13], [1, -3], [14, -4], [5, -4], [-1, -4], [-12, -4], [-14, -15], [-5, -9], [344, 8], [143, 3], [25, 1], [120, 1], [70, 1], [46, 1], [109, 3], [74, 2], [72, 0], [36, 1], [57, 1], [71, 1], [111, 3], [54, 2], [63, 1], [23, 0], [92, 2], [0, -5], [229, 5], [22, -29], [18, -17], [47, -44], [24, -22], [57, -51], [107, -100], [-67, -2], [-31, 0], [-23, -5], [-6, -5], [-19, -12], [-39, -17], [-11, -5], [-6, -6], [2, -27], [-29, -6], [-7, -3], [-2, -3], [-12, -7], [-16, -6], [0, -8], [9, -11], [0, -5], [-4, -9], [-18, -11], [-1, -9], [-6, -6], [-6, -2], [-39, -5], [-41, -11], [-18, -2], [-41, -12], [-15, -6], [-30, -9], [-48, -5], [-15, -3], [-9, -5], [-14, -11], [-4, -5], [-18, -27], [-3, -8], [7, -51], [-7, -24], [-1, -7], [-12, -11], [-13, -10], [-33, -18], [-10, -5], [-14, -10], [-17, -11], [-5, -6], [-1, -10], [5, -51], [5, -11], [8, -5], [2, -3], [-4, -9], [9, -3], [0, -4], [26, -1], [23, -3], [18, -8], [13, -5], [9, -1], [22, -7], [24, -5], [25, -14], [28, -9], [4, -8], [26, -7], [2, -3], [-5, -4], [8, -6], [-7, -9], [-5, -3], [2, -5], [7, -5], [-5, -7], [-3, -8], [5, -7], [8, -3], [20, 3], [13, 8], [7, 2], [21, 1], [8, 3], [1, 5], [-6, 8], [-1, 4], [4, 5], [18, 6], [15, 1], [1, 5], [7, 0], [6, -5], [11, -5], [8, 1], [0, 4], [8, 2], [9, -1], [41, -13], [0, 5], [14, -5], [14, -3], [14, 0], [55, 2], [28, 2], [6, -1], [25, 3], [8, 5], [18, 3], [34, 0], [14, -2], [25, -2], [28, 1], [13, 0], [54, -5], [19, -4], [40, -1], [23, 5], [21, 2], [107, 3], [10, 1], [16, -3], [8, -5], [28, 1], [4, -2], [8, -12], [8, -18], [2, -3], [46, -1], [44, 1], [0, 8], [123, 2], [0, -8], [4, -16], [2, -15], [54, 1], [12, -72], [15, -12], [4, -9], [3, -22], [3, -5], [38, -11], [38, 6], [37, 4], [29, -2], [45, 0], [31, -5], [36, -10], [24, -7], [24, -6], [18, -2], [21, -1], [42, 2], [34, -1], [55, 2], [44, 2], [19, 6], [26, 13], [29, 8], [22, 7], [20, 11], [15, 13], [10, 3], [11, 2], [23, 0], [22, 4], [33, 4], [24, 1], [18, 2], [21, 7], [24, 7], [8, 1], [45, -1], [18, 1], [40, 2], [24, 4], [22, 6], [19, 10], [9, 7], [2, 6], [-1, 8], [4, 5], [13, 6], [13, 4], [16, 7], [8, 5], [5, 7], [7, 3], [11, -7], [15, -7], [18, -6], [5, -6], [9, -3], [21, -6], [15, -7], [14, -1], [18, 2], [61, 21], [16, 5], [17, 4], [12, 0], [15, -1], [34, 0], [20, -2], [10, -2], [33, -5], [14, -5], [5, -8], [4, -14], [9, -19], [12, -7], [37, -14], [18, -10], [14, -11], [17, -10], [11, -10], [15, -5], [42, -15], [6, -3], [23, -15], [30, -13], [18, -7], [22, -4], [53, -5], [31, 2], [9, -57], [7, -62], [6, -38], [17, -157], [9, -94], [1, -10], [17, -123], [-15, -9], [-23, -9], [-21, -4], [-27, -2], [-20, 1], [-30, -1], [-85, -7], [-98, -13], [-23, -3], [-21, -5], [-7, -2], [-51, -22], [-22, -10], [-67, -36], [-12, -14], [-11, -9], [-25, -8], [-16, -15], [-11, -7], [-15, -4], [-42, -8], [-6, -3], [-7, -9], [-12, -18], [-2, -10], [10, -8], [1, -7], [-11, -10], [-3, -5], [-8, -33], [0, -5], [16, -14], [1, -10], [-5, -11], [-1, -12], [-6, -6], [-41, -26], [-11, -6], [-14, -4], [-13, -3], [-116, -30], [-24, -7], [-4, -8], [-4, -4], [19, -11], [10, -7], [22, -12], [54, -32], [15, -9], [74, -44], [19, -12], [99, -59], [38, -50], [8, -10], [20, 5], [37, 6], [28, 2], [33, -3], [90, -4], [50, 1], [12, -1], [-6, 10], [-3, 13], [4, 4], [7, 0], [27, -7], [17, 1], [3, 21], [-5, 8], [4, 23], [-13, 14], [0, 4], [27, 29], [9, 4], [22, 8], [107, 28], [53, 15], [21, 5], [40, 6], [21, 2], [11, 0], [25, -2], [17, 2], [42, 16], [32, 13], [16, 12], [17, 5], [27, 11], [26, 13], [12, 9], [8, 4], [43, 6], [25, 1], [14, 4], [43, 16], [17, 2], [70, 2], [51, 2], [-19, 13], [-18, 6], [-33, 6], [-11, 4], [-36, 14], [-25, 7], [-41, 10], [-20, 3], [-4, 7], [-16, 2], [10, 7], [-10, 6], [-19, 7], [-10, 10], [-7, 4], [-18, 6], [-10, 6], [-15, 5], [-31, 6], [2, 9], [15, 8], [19, 2], [6, 3], [16, 6], [28, 6], [17, 2], [2, 2], [19, 2], [14, 5], [2, 3], [22, 6], [1, 3], [17, 5], [25, 5], [4, 3], [37, 13], [11, 2], [25, 4], [17, 4], [46, 5], [13, 3], [26, 5], [28, 3], [30, 7], [26, 3], [12, 5], [33, 8], [20, 2], [23, 3], [19, 1], [22, 2], [21, 2], [42, 0], [40, 6], [23, 0], [22, 0], [8, 1], [15, 6], [14, 3], [23, 3], [4, 7], [12, 7], [4, 10], [11, 9], [5, 7], [11, 4], [8, 8], [9, 2], [20, 3], [18, 4], [38, 5], [28, 8], [24, 5], [24, 1], [37, 0], [21, -3], [22, -4], [111, 5], [72, 13], [46, 10], [83, 3], [115, 4], [188, 2], [91, 5], [80, -17], [30, 1], [58, 2], [196, 82], [120, 13], [121, 9], [69, 13], [37, 1], [33, -8], [95, 8], [22, 2], [29, 0], [93, 8], [78, -5], [18, 4], [9, 10], [50, 4], [75, 1], [117, 58], [59, -5], [68, 10], [41, 10], [76, 6], [176, -17], [158, 5], [49, 6], [64, 1], [228, -39], [41, 6], [-7, 5], [-7, 1], [-3, 4], [-9, 22], [5, 11], [7, 1], [1, 6], [-8, 0], [13, 5], [10, 0], [22, 6], [10, 6], [40, 11], [26, 5], [2, -3], [8, -2], [26, 1], [17, 2], [24, 0], [29, 0], [21, 3], [24, 2], [12, -1], [22, -3], [16, 1], [14, -3], [7, 1], [1, 7], [13, 6], [10, 8], [15, 4], [2, 6], [7, 1], [21, 1], [6, 5], [-3, 4], [0, 10], [32, -6], [15, 0], [4, 3], [1, 11], [5, 4], [-3, 4], [11, 4], [-1, 6], [11, 0], [0, 4], [12, 4], [5, 5], [6, 1], [0, 4], [6, 1], [5, 7], [9, 3], [-2, 2], [10, 7], [8, 3], [12, 11], [6, 0], [0, 4], [10, 3], [-3, 5], [8, 4], [-5, 3], [8, 3], [-3, 8], [6, 7], [7, 0], [-3, 5], [3, 4], [6, 3], [0, 8], [-4, 2], [3, 5], [8, 0], [0, 10], [7, 1], [3, 4], [-6, 4], [8, 2], [-4, 4], [9, 1], [8, 7], [-2, 5], [12, 3], [0, 5], [15, 2], [8, 3], [5, -2], [13, 5], [22, 2], [4, 6], [6, 2], [23, 14], [24, 6], [5, 4], [29, 11], [7, 1], [20, 7], [1, 3], [13, 2], [6, 2], [14, 1], [14, 3], [16, 6], [3, 4], [15, 9], [7, 1], [17, 11], [-1, 2], [8, 10], [7, 3], [3, 8], [4, 1], [2, 5], [-6, 7], [6, 1], [0, 4], [15, 9], [5, 0], [3, 12], [5, 0], [2, 6], [17, 5], [7, 9], [-7, 4], [13, 3], [6, 8], [15, 4], [5, 4], [8, 2], [8, 7], [-4, 4], [3, 5], [-2, 3], [9, 5], [4, 9], [8, 8], [-2, 4], [-12, 8], [1, 4], [-2, 5], [8, 3], [-7, 9], [-2, 7], [9, 7], [-3, 8], [14, 11], [10, 3], [9, 1], [3, 5], [5, 1], [3, 9], [-8, 5], [-11, 10], [-11, 2], [0, 4], [-11, 3], [-8, 6], [-9, 1], [-4, 6], [-8, 3], [0, 5], [-8, 2], [-2, 5], [-8, 5], [6, 5], [-12, 2], [1, 4], [8, 3], [-6, 8], [-7, 6], [-16, 5], [-5, 9], [-12, 9], [1, 9], [-4, 5], [-14, 6], [-12, 7], [-9, 10], [-11, 5], [-11, -2], [-10, 3], [-6, 6], [1, 6], [-14, 4], [-61, -1], [-19, -8], [-82, -5], [-92, 2], [-162, -14], [-62, -1], [-93, 10], [-132, -14], [-69, -20], [-254, -16], [-156, 21], [-173, -11], [-152, -14], [-61, -1], [-54, 22], [-62, 3], [-80, -9], [-20, -4], [-46, 42], [-39, 66], [-28, 67], [64, 71], [-51, 86], [-57, 46], [-5, 43], [97, 49], [28, 28], [-37, 50], [-45, 35], [-27, 58], [47, 36], [6, 32], [-13, 23], [-43, 19], [-12, 20], [-3, 23], [50, 9], [18, 20], [-22, 15], [-24, 27], [-15, 47], [-4, 31], [56, 52], [-16, 43], [-34, 27], [-3, 32], [7, 23], [17, 32], [-6, 51], [-15, 43], [-23, 23], [-34, 27], [-72, 18], [-12, -5], [-15, -4], [-29, -11], [-5, -4], [-21, -4], [-13, -4], [-16, -8], [-22, -20], [-5, -5], [-9, -4], [-9, -7], [-40, -25], [-33, -19], [-21, -12], [-20, -17], [-20, -19], [-13, -25], [-16, -15], [-15, -10], [-14, -7], [-93, 34], [-69, -18], [-38, -13], [-45, -5], [-74, -8], [19, -20], [-34, 9], [-87, 34], [-34, 45], [-52, 48], [0, 5], [-105, 119], [-178, 168], [104, 50], [143, 66], [1, -12], [203, 110], [-16, 5], [-89, 43], [-65, 35], [-56, 34], [-30, 21], [-19, 21], [-9, 24], [-3, 43], [-3, 32], [-1, 13], [3, 9], [6, 10], [4, 10], [90, -11], [4, -3], [33, -4], [-14, 5], [26, -3], [25, -2], [104, -5], [46, -2], [6, 12], [-35, 13], [-30, 9], [-3, -2], [-50, 4], [-29, 13], [-5, 19], [-10, 7], [-17, 6], [-49, 11], [-4, 3], [-2, 9], [2, 3], [-5, 7], [2, 2], [13, 6], [9, 12], [-3, 3], [-21, 6], [-5, 7], [-8, 5], [7, 14], [0, 8], [-2, 8], [-12, 31], [-1, 24], [-6, 15], [-20, 25], [-2, 8], [3, 20], [5, 6], [19, 31], [-1, 6], [-12, 21], [-7, 2], [-3, 5], [-1, 8], [-15, 14], [-10, 4], [-4, 4], [-4, 18], [1, 10], [-2, 3], [-19, 6], [-10, 5], [-35, 7], [-14, 7], [-25, 7], [-25, 8], [-20, 7], [-18, 11], [-17, 12], [-16, 8], [-16, 4], [-23, 2], [-15, 1], [-9, 3], [13, 12], [10, 17], [4, 12], [1, 18], [2, 6], [13, 10], [-11, 12], [6, 10], [-2, 17], [8, 7], [17, 13], [35, 16], [11, 7], [-2, 13], [10, 9], [9, 10], [1, 7], [-3, 8], [7, 11], [19, 15], [12, 9], [17, 8], [13, 10], [0, 5], [-3, 9], [-19, 18], [-19, 4], [-43, 5], [-32, 1], [-8, 4], [4, 7], [21, 28], [18, 23], [8, 3], [15, 3], [23, 8], [12, 10], [2, 14], [-11, 7], [0, 10], [-4, 7], [-5, 2], [-2, 9], [-4, 5], [9, 6], [18, 6], [8, 5], [6, 7], [5, 12], [-2, 10], [-13, 24], [0, 12], [5, 12], [11, 17], [28, 21], [2, 5], [-7, 8], [0, 4], [-7, 6], [-2, 5], [-15, 2], [-1, 4], [15, 12], [-3, 9], [-13, 7], [11, 14], [-2, 3], [10, 7], [13, 17], [6, 9], [8, 4], [2, 4], [9, 7], [1, 8], [4, 5], [11, 4], [17, 2], [-2, 3], [10, 5], [27, -8], [16, -4], [26, -3], [13, 0], [11, 2], [5, 3], [1, 6], [-13, 15], [-25, 11], [-42, 11], [1, 12], [-2, 31], [-16, 30], [-40, 28], [-52, 18], [-33, 9], [-9, 8], [-33, 46], [-27, 17], [-23, 6], [-14, 0], [-33, -1], [-15, 2], [-31, 12], [-10, 16], [-7, 23], [-2, 31], [-8, 15], [-35, 21], [-54, 13], [-54, 3], [-94, -2], [-106, -5], [-11, -2], [-19, 4], [-7, 6], [-6, 2], [-24, 2], [-20, 8], [-12, 0], [-25, -1], [-11, -3], [-33, -3], [-17, -5], [-18, -2], [-14, -6], [-10, -2], [-6, -4], [-22, -1], [-11, -3], [-17, -4], [-52, -6], [-15, -4], [-28, -4], [-14, -1], [-22, -7], [-13, 0], [-9, -4], [-18, -1], [-12, -5], [-17, -4], [-22, -4], [-35, -3], [-15, -2], [-6, -5], [-15, -2], [-11, -4], [-5, -6], [-4, -7], [-6, -3], [-19, -3], [-14, -6], [-13, -4], [-7, -3], [-24, -4], [-8, -6], [-44, -4], [-10, -4], [-30, -2], [-16, 0], [-11, -5], [-20, -1], [-23, -3], [-19, -1], [-31, -2], [-53, 1], [-22, -2], [-11, 1], [-27, 6], [67, -62], [59, -53], [61, -55], [-9, -1], [-11, 3], [-22, 8], [-40, 5], [-7, 0], [-3, 2], [-8, -8], [-8, -6], [2, -8], [12, -7], [3, -4], [-9, -11], [-20, -4], [-17, -5], [-32, -4], [-21, -4], [-25, -2], [-36, -3], [-34, -5], [-15, -1], [-47, -5], [-45, 1], [-25, 2], [-32, 0], [-40, 0], [-17, 1], [-50, -10], [-71, 0], [-5, 4], [-33, -3], [-15, 2], [-12, -2], [-178, -2], [-20, -2], [-28, -1], [-41, 0], [-84, -2], [-29, 0], [-27, 3], [-48, 2], [-17, -1], [-45, -6], [-29, 0]]]
		}				
							
		Null tile: {"type": "FeatureCollection","features":[]}					
		 */
		 
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		JSONArray geoJsonFeatures = new JSONArray();	 
		JSONArray geometries;
		
		JSONObject objects = tileTopoJson.optJSONObject("objects");
		if (objects != null) {
			JSONObject collection = objects.optJSONObject("collection");
			if (collection != null) {
				geometries = collection.optJSONArray("geometries");
				if (geometries != null) {
					
					GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
					GeometryJSON geoJSONWriter = new GeometryJSON();
					for (int i=0; i<geometries.length(); i++) {
						JSONObject jsonGeometry=geometries.getJSONObject(i);
						JSONObject properties=jsonGeometry.optJSONObject("properties");
						if (properties != null) {			
							JSONObject geoJsonFeature = new JSONObject();
							geoJsonFeature.put("type", "Feature");
							geoJsonFeature.put("properties", properties);	
							String areaId = properties.getString("area_id");
							
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

							getMapTilesQueryFormatter.addSelectField(myGeometryTable, "wkt");
							getMapTilesQueryFormatter.addFromTable(myGeometryTable);
							getMapTilesQueryFormatter.addFromTable("rif40.rif40_geolevels");
							getMapTilesQueryFormatter.addWhereJoinCondition(myGeometryTable, "geolevel_id", "rif40.rif40_geolevels", "geolevel_id");
							getMapTilesQueryFormatter.addWhereParameter(
									applySchemaPrefixIfNeeded("rif40_geolevels"),
									"geolevel_name");
							getMapTilesQueryFormatter.addWhereParameter(myGeometryTable, "zoomlevel");
							getMapTilesQueryFormatter.addWhereParameter(myGeometryTable, "areaid");

							logSQLQuery(
									"topoJson2geoJson",
									getMapTilesQueryFormatter,
									geoLevel,
									zoomlevel.toString(),
									areaId);

							statement = connection.prepareStatement(getMapTilesQueryFormatter.generateQuery());
							statement.setString(1, geoLevel);
							statement.setInt(2, zoomlevel);
							statement.setString(3, areaId);

							resultSet = statement.executeQuery();
							resultSet.next();
							
							String wkt=resultSet.getString(1);		
							Geometry geometry = null;
							if (wkt != null) {
								try {
									WKTReader reader = new WKTReader(geometryFactory);
									geometry = reader.read(wkt); // Geotools JTS
								}
								catch (ParseException wktParseException) {
									rifLogger.warning(getClass(), "wktParseException: " + wkt);
									throw new JSONException("wktParseException: " + wktParseException.getMessage() + 
										" for geoLevel: " + geoLevel +
										"; zoomlevel: " + zoomlevel +
										"; areaId: " + areaId);
								}
							}			
							else {
								throw new JSONException("Null wkt for geoLevel: " + geoLevel +
									"; zoomlevel: " + zoomlevel +
									"; areaId: " + areaId);
							}
							JSONObject njsonGeometry = new JSONObject(geoJSONWriter.toString(geometry));
							geoJsonFeature.put("geometry", njsonGeometry);
							geoJsonFeatures.put(geoJsonFeature);
			
						}
						else {
							List<String> jsonGeometryList = IteratorUtils.toList(jsonGeometry.keys());
							String jsonGeometryText = String.join(", ", jsonGeometryList);
							throw new JSONException("TopoJSON Object[\"properties\"] not found; keys: " + jsonGeometryText);
						}
					}
				}
				else {
					throw new JSONException("TopoJSON Array[\"geometries\"] not found");
				}				
			}
			else {
				List<String> collectionList = IteratorUtils.toList(collection.keys());
				String collectionText = String.join(", ", collectionList);
				throw new JSONException("TopoJSON Object[\"objects\"] not found; keys: " + collectionText);
			}
		}
		else {
			List<String> tileTopoJsonList = IteratorUtils.toList(tileTopoJson.keys());
			String tileTopoJsonText = String.join(", ", tileTopoJsonList);
			throw new JSONException("TopoJSON Object[\"objects\"] not found; keys: " + tileTopoJsonText);
		}
		
		JSONObject tileGeoJson = new JSONObject();
		tileGeoJson.put("type", "FeatureCollection");
		tileGeoJson.put("features", geoJsonFeatures);
		return tileGeoJson;
	}
	
	//get 'global' geography attribute table
				/* Instead of the topoJSON tile returned by getTileMakerTiles... it returns:
					data {
						attributes: [{
							area_id,
							name,
							band
						}, ...
						]
					}
				 */			
	public String getTileMakerAttributes(
			final Connection connection, final Geography geography,
			final GeoLevelSelect geoLevelSelect) throws RIFServiceException {

		//STEP 1: get the lookup table name
		/*
		SELECT lookup_table, lookup_desc_column
		  FROM [sahsuland_dev].[rif40].[rif40_geolevels]
		 WHERE geography     = 'EWS2011'
		   AND geolevel_name = 'MSOA2011';
		*/

		SelectQueryFormatter getLookupTableQueryFormatter =
				SelectQueryFormatter.getInstance(rifDatabaseProperties.getDatabaseType());

		getLookupTableQueryFormatter.setDatabaseSchemaName(SCHEMA_NAME);
		getLookupTableQueryFormatter.addSelectField("a", "lookup_table");
		getLookupTableQueryFormatter.addSelectField("a", "lookup_desc_column");
		getLookupTableQueryFormatter.addFromTable("rif40_geolevels a");
		getLookupTableQueryFormatter.addWhereParameter("geography");
		getLookupTableQueryFormatter.addWhereParameter("geolevel_name");

		logSQLQuery(
				"getTileMakerAttributes",
				getLookupTableQueryFormatter,
				geography.getName().toUpperCase(),
				geoLevelSelect.getName().toUpperCase());

		//For tile table name
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		//For map tiles
		PreparedStatement statement2 = null;
		ResultSet resultSet2 = null;

		try {

			statement = connection.prepareStatement(getLookupTableQueryFormatter.generateQuery());
			statement.setString(1, geography.getName().toUpperCase());
			statement.setString(2, geoLevelSelect.getName().toUpperCase());

			resultSet = statement.executeQuery();
			resultSet.next();

			//This is the tile table name for this geography
			String myLookupTable = "rif_data." + resultSet.getString(1);
			String myLookupDescName = resultSet.getString(2);

			SelectQueryFormatter getTileMakerAttributesQueryFormatter
					= new MSSQLSelectQueryFormatter();

			//STEP 2: get the tiles
			/*
				SELECT msoa2011, areaname
				FROM [rif_data].lookup_msoa2011
				ORDER BY 1;
			*/

			getTileMakerAttributesQueryFormatter.addSelectField("a", geoLevelSelect.getName().toLowerCase());
			getTileMakerAttributesQueryFormatter.addSelectField("a", myLookupDescName.toLowerCase());
			getTileMakerAttributesQueryFormatter.addFromTable(myLookupTable + " a");
			getTileMakerAttributesQueryFormatter.addQueryLine(0, "   ORDER BY 1");

			logSQLQuery(
					"getTileMakerAttributes",
					getTileMakerAttributesQueryFormatter);

			statement2 = connection.prepareStatement(getTileMakerAttributesQueryFormatter.generateQuery());

			resultSet2 = statement2.executeQuery();
			int rowCount=0;
			
			JSONObject attributes = new JSONObject();
			JSONArray attributesArray = new JSONArray();
			String result="";
			if (resultSet2.next()) {

				do {	
					JSONObject attributesData = new JSONObject();
					rowCount++;				
					String areaId = resultSet2.getString(1);
					String areaName = resultSet2.getString(2);
					
					attributesData.put("area_id", areaId);
					attributesData.put("name", areaName);
					attributesArray.put(attributesData);
				} while (resultSet2.next());
				
				attributes.put("attributes", attributesArray);
				result=attributes.toString();
				
				rifLogger.info(getClass(), "get tile attributes for geography: " + geography.getName().toUpperCase() +
										   "; geolevel: " + geoLevelSelect.getName().toUpperCase() +
										   "; rows: " + rowCount + 
										   "; length: " + result.length());
			}
			else {
				throw new Exception("get tile attributes for geography: " + geography.getName().toUpperCase() +
				   "; geolevel: " + geoLevelSelect.getName().toUpperCase() + " failed; no rows returned");
			}
 
			connection.commit();
			return result;
		} catch(SQLException sqlException) {
			//Record original exception, throw sanitised, human-readable version
			logSQLException(sqlException);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTileAttributes",
					geoLevelSelect.getDisplayName(),
					geography.getDisplayName());
			throw new RIFServiceException(
					RIFServiceError.DATABASE_QUERY_FAILED,
					errorMessage);	
		} catch(Exception exception) {
			//Record original exception, throw sanitised, human-readable version
			logException(exception);
			String errorMessage
					= RIFServiceMessages.getMessage(
					"sqlResultsQueryManager.unableToGetTileAttributes",
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
