package rifServices.dataStorageLayer.common;

import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifServices.dataStorageLayer.common.SQLAbstractSQLManager;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifServices.businessConceptLayer.RIFStudySubmission;

import com.sun.rowset.CachedRowSetImpl;
import java.sql.*;
import java.io.*;
import java.lang.*;

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

/**
 *
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
 * Peter Hambly
 * @author phambly
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

public class RifGeospatialOutputs extends SQLAbstractSQLManager {
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private Connection connection;
	private String studyID;
	private static String EXTRACT_DIRECTORY;
	private static int printingDPI;
	private static float jpegQuality=new Float(.8);
	
	private static final String STUDY_QUERY_SUBDIRECTORY = "study_query";
	private static final String STUDY_EXTRACT_SUBDIRECTORY = "study_extract";
	private static final String RATES_AND_RISKS_SUBDIRECTORY = "rates_and_risks";
	private static final String GEOGRAPHY_SUBDIRECTORY = "geography";
	private static final String DATA_SUBDIRECTORY = "data";
	private static final int BASE_FILE_STUDY_NAME_LENGTH = 100;
	
	private RIFServiceStartupOptions rifServiceStartupOptions;
	private static DatabaseType databaseType;
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================
	/**
     * Constructor.
     * 
     * @param RIFServiceStartupOptions rifServiceStartupOptions (required)
     */
	public RifGeospatialOutputs(
			final RIFServiceStartupOptions rifServiceStartupOptions) {
		super(rifServiceStartupOptions.getRIFDatabaseProperties());
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		try {
			databaseType=this.rifServiceStartupOptions.getRifDatabaseType();
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RifGeospatialOutputs() constructor");
			throw new NullPointerException();
		}
	}	
	
	public void writeGeographyFiles(
			final Connection connection,
			final File temporaryDirectory,
			final String baseStudyName,
			final String zoomLevel,
			final RIFStudySubmission rifStudySubmission,
			final CachedRowSetImpl rif40Studies)
					throws Exception {
		
		String studyID = rifStudySubmission.getStudyID();
		String mapTable=getColumnFromResultSet(rif40Studies, "map_table");
		
		//Add geographies to zip file
		StringBuilder tileTableName = new StringBuilder();	
		tileTableName.append("geometry_");
		String geog = rifStudySubmission.getStudy().getGeography().getName();			
		tileTableName.append(geog);
		
		//Write study area
		StringBuilder tileFileName = new StringBuilder();
		tileFileName.append(baseStudyName);
		tileFileName.append("_studyArea");
		
		writeMapQueryTogeoJSONFile(
				connection,
				"rif40_study_areas",
				temporaryDirectory,
				GEOGRAPHY_SUBDIRECTORY,
				"rif_data",								/* Schema */
				tileTableName.toString(),
				tileFileName.toString(),
				zoomLevel,
				studyID,
				"S", 									/* areaType */
				", a.area_id, a.band_id, b.zoomlevel, c.areaname",	/* extraColumns */
				null 									/* additionalJoin */);
		
		//Write comparison area
		tileFileName = new StringBuilder();
		tileFileName.append(baseStudyName);
		tileFileName.append("_comparisonArea");
		
		writeMapQueryTogeoJSONFile(
				connection,
				"rif40_comparison_areas",
				temporaryDirectory,
				GEOGRAPHY_SUBDIRECTORY,
				"rif_data",								/* Schema */
				tileTableName.toString(),
				tileFileName.toString(),
				zoomLevel,
				studyID,
				"C", 									/* areaType */
				", a.area_id, b.zoomlevel, c.areaname",	/* extraColumns */
				null 									/* additionalJoin */);	
		
		//Write results
		tileFileName = new StringBuilder();
		tileFileName.append(baseStudyName);
		tileFileName.append("_map");
		
		writeMapQueryTogeoJSONFile(
				connection,
				"rif40_study_areas",
				temporaryDirectory,
				DATA_SUBDIRECTORY,
				"rif_data",				/* Schema */
				tileTableName.toString(),
				tileFileName.toString(),
				zoomLevel,
				studyID,
				null, 									/* areaType */
				", b.zoomlevel, c.*",					/* extraColumns */
				"LEFT OUTER JOIN rif_studies." + mapTable.toLowerCase() + 
					" c ON (a.area_id = c.area_id)"
														/* additionalJoin */);
				
	}		
						
	private void writeMapQueryTogeoJSONFile(
			final Connection connection,
			final String areaTableName,
			final File temporaryDirectory,
			final String dirName,
			final String schemaName,
			final String tableName,
			final String outputFileName,
			final String zoomLevel,
			final String studyID,
			final String areaType,
			final String extraColumns,
			final String additionalJoin)
					throws Exception {
			
		String geojsonDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName;
		File geojsonDirectory = new File(geojsonDirName);
		File newDirectory = new File(geojsonDirName);
		if (newDirectory.exists()) {
			rifLogger.info(this.getClass(), 
				"Found directory: " + newDirectory.getAbsolutePath());
		}
		else {
			newDirectory.mkdirs();
			rifLogger.info(this.getClass(), 
				"Created directory: " + newDirectory.getAbsolutePath());
		}
		String geojsonFile=geojsonDirName + File.separator + outputFileName + ".json";
		rifLogger.info(this.getClass(), "Add JSON to ZIP file: " + geojsonFile);
		File file = new File(geojsonFile);
		if (file.exists()) {
			file.delete();
		}
		OutputStream ostream = new FileOutputStream(geojsonFile);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ostream);
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

		//get geolevel
		SQLGeneralQueryFormatter geolevelQueryFormatter = new SQLGeneralQueryFormatter();	
		geolevelQueryFormatter.addQueryLine(0, "SELECT b.geolevel_id, b.geolevel_name");
		geolevelQueryFormatter.addQueryLine(0, "  FROM rif40.rif40_studies a, rif40.rif40_geolevels b");
		geolevelQueryFormatter.addQueryLine(0, " WHERE study_id = ?");
		if (areaTableName.equals("rif40_comparison_areas")) {
			geolevelQueryFormatter.addQueryLine(0, "  AND a.comparison_geolevel_name = b.geolevel_name");
		} 
		else if (areaTableName.equals("rif40_study_areas")) {
			geolevelQueryFormatter.addQueryLine(0, "  AND a.study_geolevel_name = b.geolevel_name");
		} 
		else { // Map tables - same as study areas
			geolevelQueryFormatter.addQueryLine(0, "  AND a.study_geolevel_name = b.geolevel_name");
		}		
		Integer geolevel;
		String geolevelName = null;
		PreparedStatement geolevelStatement = createPreparedStatement(connection, geolevelQueryFormatter);		
		try {
			ResultSet geolevelResultSet = null;			
			logSQLQuery("writeMapQueryTogeoJSONFile", geolevelQueryFormatter, studyID);
			geolevelStatement = createPreparedStatement(connection, geolevelQueryFormatter);
			geolevelStatement.setInt(1, Integer.parseInt(studyID));	
			geolevelResultSet = geolevelStatement.executeQuery();
			geolevelResultSet.next();
			geolevel = geolevelResultSet.getInt(1);
			geolevelName = geolevelResultSet.getString(2);
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + geolevelQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {	
			SQLQueryUtility.close(geolevelStatement);
		}
		
/*

Form 1: areaType: C; extraColumns: a.area_id, b.zoomlevel; no additionalJoin

WITH a AS (
	SELECT *
	  FROM rif40.rif40_comparison_areas
	 WHERE study_id = ?
)
SELECT ST_AsText(ST_ForceRHR(b.geom)) AS wkt, a.area_id, b.zoomlevel, c.areaname
  FROM a
        LEFT OUTER JOIN rif_data.geometry_sahsuland b ON (a.area_id = b.areaid)
		LEFT OUTER JOIN rif_data.lookup_sahsu_grd_level1 c ON (a.area_id = c.sahsu_grd_level1)
 WHERE b.geolevel_id = ? AND b.zoomlevel = ?;	

Form 2: areaType: S; extraColumns: a.area_id, a.band_id, b.zoomlevel; no additionalJoin

WITH a AS (
	SELECT *
	  FROM rif40.rif40_study_areas
	 WHERE study_id = ?
)
SELECT ST_AsText(ST_ForceRHR(b.geom)) AS wkt, a.area_id, a.band_id, b.zoomlevel, c.areaname
  FROM a
        LEFT OUTER JOIN rif_data.geometry_sahsuland b ON (a.area_id = b.areaid)
		LEFT OUTER JOIN rif_data.lookup_sahsu_grd_level4 c ON (a.area_id = c.sahsu_grd_level4)
 WHERE b.geolevel_id = ? AND b.zoomlevel = ?;	

Form 3: areaType IS NULL; extraColumns: b.zoomlevel, c.*; additionalJoin: 
		LEFT OUTER JOIN rif_studies.s367_map c ON (a.area_id = c.area_id)

WITH a AS (
	SELECT *
	  FROM rif40.rif40_study_areas
	 WHERE study_id = ?
)
SELECT ST_AsText(ST_ForceRHR(b.geom)) AS wkt, b.zoomlevel, c.*
  FROM a
        LEFT OUTER JOIN rif_data.geometry_sahsuland b ON (a.area_id = b.areaid)
        LEFT OUTER JOIN rif_studies.s367_map c ON (a.area_id = c.area_id);
 */		
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "WITH a AS (");
		queryFormatter.addQueryLine(0, "	SELECT *");
		queryFormatter.addQueryLine(0, "	  FROM rif40." + areaTableName);
		queryFormatter.addQueryLine(0, "	 WHERE study_id = ?");
		queryFormatter.addQueryLine(0, ")");
		if (databaseType == DatabaseType.POSTGRESQL) { // Force RHR (not needed)
			queryFormatter.addQueryLine(0, "SELECT ST_AsText(ST_ForceRHR(b.geom)) AS wkt");			
		}
		else if (databaseType == DatabaseType.SQL_SERVER) {
			queryFormatter.addQueryLine(0, "SELECT b.wkt");	
		}					
		if (extraColumns != null) {
			queryFormatter.addQueryLine(0, "      " + extraColumns);
		}
		queryFormatter.addQueryLine(0, "  FROM a");
		queryFormatter.addQueryLine(0, "        LEFT OUTER JOIN "  + schemaName + "." + tableName.toLowerCase() + 
															" b ON (a.area_id = b.areaid)");												
		if (additionalJoin != null) {
			queryFormatter.addQueryLine(0, additionalJoin);
		}
		else {
			queryFormatter.addQueryLine(0, "LEFT OUTER JOIN rif_data.lookup_" + geolevelName + 
				" c ON (a.area_id = c." + geolevelName + ")");
		}
		queryFormatter.addQueryLine(0, " WHERE b.geolevel_id = ? AND b.zoomlevel = ?");		
		
		PreparedStatement statement = createPreparedStatement(connection, queryFormatter);		
		try {	
			ResultSet resultSet = null;
			String[] queryArgs = new String[3];
			queryArgs[0]=studyID;
			queryArgs[1]=geolevel.toString();
			queryArgs[2]=zoomLevel;
			logSQLQuery("writeMapQueryTogeoJSONFile", queryFormatter, queryArgs);
			statement = createPreparedStatement(connection, queryFormatter);
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, geolevel);
			statement.setInt(3, Integer.parseInt(zoomLevel));				
			resultSet = statement.executeQuery();
			
			//Write WKT to geoJSON
			int i = 0;
			bufferedWriter.write("{\"type\":\"FeatureCollection\",\"features\":[");	
			// Add bbox after FeatureCollection
			// SRID/CRS? geometry is in WGS84
			while (resultSet.next()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				i++;
				if (i > 1) {
					bufferedWriter.write(","); 
				}
				bufferedWriter.write("{\"type\":\"Feature\",\"geometry\":");

				String polygon = resultSet.getString(1);	
				if (polygon != null) {
					GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

					WKTReader reader = new WKTReader(geometryFactory);
					Geometry geometry = reader.read(polygon); // Geotools JTS	
					GeometryJSON writer = new GeometryJSON();
					String strGeoJSON = writer.toString(geometry);
					
	//				rifLogger.info(this.getClass(), "Wkt: " + polygon.substring(0, 30));
	//				rifLogger.info(this.getClass(), "Geojson: " + strGeoJSON.substring(0, 30));
					bufferedWriter.write(strGeoJSON);
				}			
				else {
					throw new Exception("Null polygon for record: " + 1);
				}
				bufferedWriter.write(",\"properties\":{");
				if (areaType != null) {
					bufferedWriter.write("\"areatype\":\"" + areaType + "\"");
				}
				else {
					bufferedWriter.write("\"maptype\":\"Results\"");
				}
				if (extraColumns != null) {
					
					// The column count starts from 2
					for (int j = 2; j <= columnCount; j++ ) {
						String name = rsmd.getColumnName(j);
						String value = resultSet.getString(j);	
//						String columnType = rsmd.getColumnTypeName(j);
						
						bufferedWriter.write(",\"" + name + "\":\"" + value + "\"");
					}
				}
				bufferedWriter.write("}");
				bufferedWriter.write("}");
			}
			
			bufferedWriter.write("]");
			bufferedWriter.write("}");

			bufferedWriter.flush();
			bufferedWriter.close();			

			connection.commit();
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + queryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
	}
}	

