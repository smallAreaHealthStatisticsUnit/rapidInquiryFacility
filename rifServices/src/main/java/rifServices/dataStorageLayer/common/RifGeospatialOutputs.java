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

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor; 
import org.opengis.feature.type.GeometryDescriptor; 

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.data.shapefile.ShapefileDataStore; 
import org.geotools.data.FeatureWriter; 
import org.geotools.data.Transaction; 

import com.vividsolutions.jts.geom.MultiPolygon;
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
				", b.zoomlevel, c.area_id, c.username, c.study_id, c.inv_id, c.band_id, c.genders" +
				"/*, c.direct_standardisation */, c.adjusted, c.observed, c.expected" +
				", c.lower95, c.upper95, c.relative_risk AS rr, c.smoothed_relative_risk AS sm_rr" +
				", c.posterior_probability AS post_prob" +
				"/*, c.posterior_probability_upper95, c.posterior_probability_lower95" +
				", c.residual_relative_risk, c.residual_rr_lower95, c.residual_rr_upper95 */" +
				", c.smoothed_smr AS sm_smr, c.smoothed_smr_lower95 AS sm_smr_l95" +
				", c.smoothed_smr_upper95 AS sm_smr_u95",	
														/* extraColumns: reduced to 10 characters */
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
		String shapefileDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName +
			File.separator + outputFileName;
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
		File shapefileDirectory = new File(geojsonDirName);
		newDirectory = new File(shapefileDirName);
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
		String shapefileName=shapefileDirName + File.separator + outputFileName + ".shp";
		rifLogger.info(this.getClass(), "Add JSON to ZIP file: " + geojsonFile);
		File file = new File(geojsonFile);
		if (file.exists()) {
			file.delete();
		}
		OutputStream ostream = new FileOutputStream(geojsonFile);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ostream);
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
		
		File shapefile=new File(shapefileName);
		ShapefileDataStore shapeDataStore = new ShapefileDataStore(
			shapefile.toURI().toURL());			
		FeatureWriter<SimpleFeatureType, SimpleFeature> shapefileWriter = null; 
			// Created once feature types are defined

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
		
			while (resultSet.next()) {
				StringBuffer stringFeature = new StringBuffer();
				
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				i++;
				if (i == 1) {
					setupShapefile(rsmd, columnCount, shapeDataStore, areaType, outputFileName);
					
					shapefileWriter = shapeDataStore.getFeatureWriter(shapeDataStore.getTypeNames()[0],
							Transaction.AUTO_COMMIT);
				}
				SimpleFeature feature = (SimpleFeature) shapefileWriter.next(); 
				
				stringFeature.append("{\"type\":\"Feature\",\"MultiPolygon\":");
		
				if (i == 1) {	
					printShapefileColumns(feature, rsmd, outputFileName);
				}
			
				String polygon = resultSet.getString(1);	
				MultiPolygon geometry = null;
				if (polygon != null) {
					GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

					WKTReader reader = new WKTReader(geometryFactory);
					geometry = (MultiPolygon)reader.read(polygon); // Geotools JTS	
					GeometryJSON geoJSONWriter = new GeometryJSON();
					String strGeoJSON = geoJSONWriter.toString(geometry);
	//				rifLogger.info(this.getClass(), "Wkt: " + polygon.substring(0, 30));
	//				rifLogger.info(this.getClass(), "Geojson: " + strGeoJSON.substring(0, 30));
					stringFeature.append(strGeoJSON);
				}			
				else {
					throw new Exception("Null polygon for record: " + 1);
				}
				AttributeDescriptor ad = feature.getType().getDescriptor(0); 
				if (ad instanceof GeometryDescriptor) { 
					feature.setAttribute(0, geometry); 
				} 
				else { 
					throw new Exception("First attribute is not MultiPolygon: " + 
						ad.getName().toString());
				}
				
				stringFeature.append(",\"properties\":{");
				if (areaType != null) {
					stringFeature.append("\"areatype\":\"" + areaType + "\"");
					feature.setAttribute(1, areaType); 
				}
				else {
					stringFeature.append("\"maptype\":\"Results\"");
					feature.setAttribute(1, "results"); 
				}
				
				if (extraColumns != null) {
					
					// The column count starts from 2
					for (int j = 2; j <= columnCount; j++ ) {		
						String name = rsmd.getColumnName(j);
						String value = resultSet.getString(j);	
						String columnType = rsmd.getColumnTypeName(j);
						addDatumToShapefile(feature, stringFeature, name, value, columnType, j, i);
					}
				}				
		
				stringFeature.append("}");
				stringFeature.append("}");
				
				if (i > 1) {
					bufferedWriter.write(","); 	// Array separator between features
				}
				bufferedWriter.write(stringFeature.toString());	
				shapefileWriter.write();
			} // End of while loop
			
			bufferedWriter.write("]}"); // End FeatureCollection
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + queryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			SQLQueryUtility.close(statement);

			bufferedWriter.flush();
			bufferedWriter.close();	
			if (shapefileWriter != null) {
				shapefileWriter.close();	
			}	
			connection.commit();
		}
	}
	
	// https://www.programcreek.com/java-api-examples/index.php?source_dir=geotools-old-master/modules/library/render/src/test/java/org/geotools/renderer/lite/LabelObstacleTest.java
	private void setupShapefile(ResultSetMetaData rsmd, int columnCount, ShapefileDataStore dataStore, 
		String areaType, String featureSetName)
			throws Exception {
		
		SimpleFeatureTypeBuilder featureBuilder = new SimpleFeatureTypeBuilder();
		featureBuilder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
        featureBuilder.setName(featureSetName);  

		featureBuilder.add("geometry", MultiPolygon.class);
		featureBuilder.length(7).add("areatype", String.class);
		
		// The column count starts from 2
		for (int k = 2; k <= columnCount; k++ ) {
			String name = rsmd.getColumnName(k);
			String columnType = rsmd.getColumnTypeName(k);
			featureBuilder.add(name, String.class);
		}								
		
		// build the type
		final SimpleFeatureType featureType = featureBuilder.buildFeatureType();
		
		dataStore.createSchema(featureType);
	}
	
	private void addDatumToShapefile(SimpleFeature feature, StringBuffer stringFeature, 
		String name, String value, String columnType, int index, int rowCount)
			throws Exception {
			
		if (rowCount < 2) {
			AttributeDescriptor ad = feature.getType().getDescriptor(index); 
			String featureName = ad.getName().toString();	
			String featureType = ad.getType().toString();

			if (ad instanceof GeometryDescriptor) { 
				throw new Exception("Shapefile attribute is Geometry when expecting non geospatial type for column: " + 
					name + ", index: " + index + "; type: " + featureType);
			}
			
			if (name.equals(featureName)) {
				stringFeature.append(",\"" + name + "\":\"" + value + "\"");
				try {
					feature.setAttribute(index, value);
				}
				catch (Exception exception) {
					rifLogger.error(this.getClass(), "Error in addDatumToShapefile() row: " + rowCount +
						"; column: " + name + ", index: " + index + "; type: " + featureType,
						exception);
					throw exception;
				}				
			}
			else {
				throw new Exception("Shapefile attribute name: " + featureName + 
					" does not match [truncated] column name: " + name + ", index: " + index + "; type: " + featureType);
			}
		}
	} 
	
	/**
	 * Print shapefile and database cilumn names and types
	 *	
     * @param SimpleFeature feature (required)
     * @param ResultSetMetaData rsmd (required)
     * @param String outputFileName (required)
	 *
	 * E.g.
11:32:25.396 [http-nio-8080-exec-105] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.common.RifGeospatialOutputs]:
Database: POSTGRESQL
Column[1]: wkt; DBF name: WKT; truncated: false; type: text
Column[2]: area_id; DBF name: AREA_ID; truncated: false; type: varchar
Column[3]: band_id; DBF name: BAND_ID; truncated: false; type: int4
Column[4]: zoomlevel; DBF name: ZOOMLEVEL; truncated: false; type: int4
Column[5]: areaname; DBF name: AREANAME; truncated: false; type: varchar
Shapefile: s367_1002_lung_cancer_studyArea
Feature[0]: the_geom; type: GeometryTypeImpl MultiPolygon<MultiPolygon>
Feature[1]: areatype; type: AttributeTypeImpl areatype<String>
restrictions=[ length([.]) <= 7 ]
Feature[2]: area_id; type: AttributeTypeImpl area_id<String>
restrictions=[ length([.]) <= 254 ]
Feature[3]: band_id; type: AttributeTypeImpl band_id<String>
restrictions=[ length([.]) <= 254 ]
Feature[4]: zoomlevel; type: AttributeTypeImpl zoomlevel<String>
restrictions=[ length([.]) <= 254 ]
Feature[5]: areaname; type: AttributeTypeImpl areaname<String>
restrictions=[ length([.]) <= 254 ]
	
	 */
	private void printShapefileColumns(SimpleFeature feature, ResultSetMetaData rsmd,
		String outputFileName)
		throws Exception {
							
		StringBuilder sb = new StringBuilder();
		
		int truncatedCount=0;
		sb.append("Database: " + databaseType + lineSeparator);
		for (int j = 1; j <= rsmd.getColumnCount(); j++) { 
			String name = rsmd.getColumnName(j);
			String dbfName = name.substring(0, Math.min(name.length(), 9)).toUpperCase(); // Trim to 10 chars
			boolean isTruncated=false;
			if (name.length() > 10) {
				isTruncated=true;
				truncatedCount++;
			}
			String columnType = rsmd.getColumnTypeName(j);
			sb.append("Column[" + j + "]: " + name +
				"; DBF name: " + dbfName +
				"; truncated: " + isTruncated +
				"; type: " + columnType + lineSeparator);
		}
		sb.append("Shapefile: " + outputFileName + lineSeparator);
		for (int k = 0; k < feature.getAttributeCount(); k++) { 			
			AttributeDescriptor ad = feature.getType().getDescriptor(k); 
			String featureName = ad.getName().toString();	
			String featureType = ad.getType().toString();
			sb.append("Feature[" + k + "]: " + featureName +
				"; type: " + featureType + lineSeparator);	
		}
		rifLogger.info(this.getClass(), sb.toString());
		if (truncatedCount > 0) {
			throw new Exception("Shapefile: " + outputFileName + 
				truncatedCount + " columns will be truncated; names will be unpredictable");
		}
	}
}	
