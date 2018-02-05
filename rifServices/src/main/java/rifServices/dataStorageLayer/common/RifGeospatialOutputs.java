package rifServices.dataStorageLayer.common;

import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifServices.dataStorageLayer.common.SQLAbstractSQLManager;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifServices.businessConceptLayer.RIFStudySubmission;

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
			final RIFStudySubmission rifStudySubmission)
					throws Exception {
		
		String studyID = rifStudySubmission.getStudyID();
	
		//Add geographies to zip file
		StringBuilder tileTableName = new StringBuilder();	
		tileTableName.append("rif_data.geometry_");
		String geog = rifStudySubmission.getStudy().getGeography().getName();			
		tileTableName.append(geog);
		
		//Write study area
		StringBuilder tileFileName = null;
		tileFileName = new StringBuilder();
		tileFileName.append(baseStudyName);
		tileFileName.append("_studyArea");
		
		writeMapQueryTogeoJSONFile(
				connection,
				"rif40_study_areas",
				temporaryDirectory,
				GEOGRAPHY_SUBDIRECTORY,
				tileTableName.toString(),
				tileFileName.toString(),
				zoomLevel,
				studyID,
				"S", /* areaType */
				", a.band_id");
		
		//Write comparison area
		tileFileName = new StringBuilder();
		tileFileName.append(baseStudyName);
		tileFileName.append("_comparisonArea");
		
		writeMapQueryTogeoJSONFile(
				connection,
				"rif40_comparison_areas",
				temporaryDirectory,
				GEOGRAPHY_SUBDIRECTORY,
				tileTableName.toString(),
				tileFileName.toString(),
				zoomLevel,
				studyID,
				"C", /* areaType */
				null /* extraColumns */);
	}		
						
	private void writeMapQueryTogeoJSONFile(
			final Connection connection,
			final String areaTableName,
			final File temporaryDirectory,
			final String dirName,
			final String tableName,
			final String outputFileName,
			final String zoomLevel,
			final String studyID,
			final String areaType,
			final String extraColumns)
					throws Exception {
		
		//get geolevel
		SQLGeneralQueryFormatter geolevelQueryFormatter = new SQLGeneralQueryFormatter();	
		geolevelQueryFormatter.addQueryLine(0, "SELECT b.geolevel_id");
		geolevelQueryFormatter.addQueryLine(0, "FROM rif40.rif40_studies a, rif40.rif40_geolevels b");
		geolevelQueryFormatter.addQueryLine(0, "WHERE study_id = ?");
		if (areaTableName.equals("rif40_comparison_areas")) {
			geolevelQueryFormatter.addQueryLine(0, "AND a.comparison_geolevel_name = b.geolevel_name");
		} else {
			geolevelQueryFormatter.addQueryLine(0, "AND a.study_geolevel_name = b.geolevel_name");
		}
	
		//count areas
		SQLGeneralQueryFormatter countQueryFormatter = new SQLGeneralQueryFormatter();
		countQueryFormatter.addQueryLine(0, "SELECT count(area_id) from rif40." + areaTableName + " where study_id = ?");
		
		//TODO: possible issues with Multi-polygon and point arrays
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		if (databaseType == DatabaseType.POSTGRESQL) {
			queryFormatter.addQueryLine(0, "SELECT ST_AsText(ST_ForceRHR(b.geom)) AS wkt, b.areaid, b.zoomlevel");			
		}
		else if (databaseType == DatabaseType.SQL_SERVER) {
			queryFormatter.addQueryLine(0, "SELECT b.wkt, b.areaid, b.zoomlevel");	
		}					
		if (extraColumns != null) {
			queryFormatter.addQueryLine(0, "      " + extraColumns);
		}
		queryFormatter.addQueryLine(0, "  FROM (SELECT *");
		queryFormatter.addQueryLine(0, "          FROM rif40." + areaTableName);
		queryFormatter.addQueryLine(0, "         WHERE study_id = ?) a");
		queryFormatter.addQueryLine(0, "        LEFT OUTER JOIN " + tableName.toLowerCase() + 
															" b ON (a.area_id = b.areaid)");
		queryFormatter.addQueryLine(0, " WHERE geolevel_id = ? AND zoomlevel = ?");
	
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
		
		PreparedStatement geolevelStatement = createPreparedStatement(connection, geolevelQueryFormatter);		
		ResultSet geolevelResultSet = null;
		PreparedStatement countStatement = createPreparedStatement(connection, countQueryFormatter);		
		ResultSet countResultSet = null;
		PreparedStatement statement = createPreparedStatement(connection, queryFormatter);		
		ResultSet resultSet = null;
		
		try {
			logSQLQuery("writeMapQueryTogeoJSONFile", geolevelQueryFormatter, studyID);
			geolevelStatement = createPreparedStatement(connection, geolevelQueryFormatter);
			geolevelStatement.setInt(1, Integer.parseInt(studyID));	
			geolevelResultSet = geolevelStatement.executeQuery();
			geolevelResultSet.next();
			Integer geolevel = geolevelResultSet.getInt(1);
			
			logSQLQuery("writeMapQueryTogeoJSONFile", countQueryFormatter, studyID);
			countStatement = createPreparedStatement(connection, countQueryFormatter);
			countStatement.setInt(1, Integer.parseInt(studyID));	
			countResultSet = countStatement.executeQuery();
			countResultSet.next();
			int rows = countResultSet.getInt(1);

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
								 
				GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

				WKTReader reader = new WKTReader(geometryFactory);
				Geometry geometry = reader.read(polygon); // Geotools JTS	
				GeometryJSON writer = new GeometryJSON();
				String strGeoJSON = writer.toString(geometry);
				
				rifLogger.info(this.getClass(), "Wkt: " + polygon.substring(0, 30));
				rifLogger.info(this.getClass(), "Geojson: " + strGeoJSON.substring(0, 30));
				bufferedWriter.write(strGeoJSON);
				bufferedWriter.write(",\"properties\":{");
				bufferedWriter.write("\"area_id\":\"" + resultSet.getString(2) + "\",");
				bufferedWriter.write("\"zoomLevel\":\"" + resultSet.getString(3) + "\",");
				if (areaType != null) {
					bufferedWriter.write("\"areatype\":\"" + areaType + "\"");
				}
				if (extraColumns != null) {
					
					// The column count starts from 4
					for (int j = 4; j <= columnCount; j++ ) {
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
		finally {
			SQLQueryUtility.close(statement);
			SQLQueryUtility.close(countStatement);
			SQLQueryUtility.close(geolevelStatement);
		}
	}
}	

