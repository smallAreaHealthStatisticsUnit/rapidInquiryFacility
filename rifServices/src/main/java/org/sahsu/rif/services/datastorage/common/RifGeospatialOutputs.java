package org.sahsu.rif.services.datastorage.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.SQLGeneralQueryFormatter;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.graphics.RIFGraphicsOutputType;
import org.sahsu.rif.services.graphics.RIFMaps;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.datastorage.common.RifWellKnownText;

import javax.sql.rowset.CachedRowSet;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

public class RifGeospatialOutputs {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static int printingDPI;

	private static final String GEOGRAPHY_SUBDIRECTORY = "geography";
	private static final String DATA_SUBDIRECTORY = "data";

	private RIFServiceStartupOptions rifServiceStartupOptions;
	private static DatabaseType databaseType;
	
	private static GeometryFactory geometryFactory = null;
	private static GeometryJSON geoJSONWriter = null;

	private static RifWellKnownText rifWellKnownText = null;
	private static RifCoordinateReferenceSystem rifCoordinateReferenceSystem = null;
	private RIFMaps rifMaps = null;
	private static int roundDP=3;
	
	private final SQLManager manager;
	
	/**
     * Constructor.
     * 
     * @param rifServiceStartupOptions (required)
     */
	public RifGeospatialOutputs(
			final RIFServiceStartupOptions rifServiceStartupOptions, final SQLManager manager) {
		
		this.manager = manager;
		rifCoordinateReferenceSystem = new RifCoordinateReferenceSystem();
		geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		geoJSONWriter = new GeometryJSON();
		
		RifWellKnownText rifWellKnownText = new RifWellKnownText();
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		try {
			databaseType=this.rifServiceStartupOptions.getRifDatabaseType();
			printingDPI=this.rifServiceStartupOptions.getOptionalRIfServiceProperty("printingDPI", 1000);
			roundDP=this.rifServiceStartupOptions.getOptionalRIfServiceProperty("roundDP", 3);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RifGeospatialOutputs() constructor");
			throw new NullPointerException();
		}		
	}	

	/** 
	 * Write geospatial files in both geoJSON and shapefile format.
	 * a) rif study area to GEOGRAPHY_SUBDIRECTORY
	 * b) Comparison area to GEOGRAPHY_SUBDIRECTORY
	 * c) Map (results) table to DATA_SUBDIRECTORY
	 *    The column list for the map tsble is hard coded and reduced to 10 characters for DBF support
     * 
	 * @param Connection connection, 
	 * @param File temporaryDirectory,
	 * @param String baseStudyName,
	 * @param String zoomLevel,
	 * @param RIFStudySubmission rifStudySubmission,
	 * @param CachedRowSet rif40Studies,
	 * @param CachedRowSet rif40Investigations,
	 * @param Locale locale
	 *
	 * @returns String
	 */		
	public String writeGeospatialFiles(
			final Connection connection,
			final File temporaryDirectory,
			final String baseStudyName,
			final String zoomLevel,
			final RIFStudySubmission rifStudySubmission,
			final CachedRowSet rif40Studies,
			final CachedRowSet rif40Investigations,
			final Locale locale)
					throws Exception {
						
		if (rifMaps == null) {
			rifMaps = new RIFMaps(rifServiceStartupOptions, manager, rif40Studies);
		}						
		
		String studyID = rifStudySubmission.getStudyID();
		String mapTable=manager.getColumnFromResultSet(rif40Studies, "map_table");
		String selectStateText=manager.getColumnFromResultSet(rif40Studies, "select_state", true /* allowNulls */, false /*  allowNoRows */);
		boolean isDiseaseMappingStudy=true;
		
		if (selectStateText != null) {
			JSONObject selectStateJson = new JSONObject(selectStateText); // Check it parses OK
			String studyTypeStr = selectStateJson.optString("studyType");
			if (studyTypeStr != null && studyTypeStr.equals("risk_analysis_study")) {
				isDiseaseMappingStudy=false;
			}
		}
				
		//Add geographies to zip file
		StringBuilder tileTableName = new StringBuilder();	
		tileTableName.append("geometry_");
		String geog = rifStudySubmission.getStudy().getGeography().getName();			
		tileTableName.append(geog);
		
		//Write study area
		StringBuilder tileFileName = new StringBuilder();
		tileFileName.append(baseStudyName);
		tileFileName.append("_studyArea");
		
		RifFeatureCollection studyFeatureCollection=writeMapQueryTogeoJSONFile(
				connection,
				rifStudySubmission,
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
				null 									/* additionalJoin */,
				locale);
		
		//Write comparison area
		tileFileName = new StringBuilder();
		tileFileName.append(baseStudyName);
		tileFileName.append("_comparisonArea");
		
		RifFeatureCollection comparisonFeatureCollection=writeMapQueryTogeoJSONFile(
				connection,
				rifStudySubmission,
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
				null 									/* additionalJoin */,
				locale);	
		
		//Write results
		tileFileName = new StringBuilder();
		tileFileName.append(baseStudyName);
		tileFileName.append("_map");
		
		String extraColumns=null;
		
		
		if (databaseType == DatabaseType.POSTGRESQL) { 
			extraColumns=", b.zoomlevel, a.area_id, c.username, c.study_id, c.inv_id, c.band_id, c.genders" +
//				"/*, c.direct_standardisation */" +
				", ROUND(c.adjusted::NUMERIC, " + roundDP + ") As adjusted, c.observed" +
				", ROUND(c.expected::NUMERIC, " + roundDP + ") AS expected" +
				", ROUND(c.lower95::NUMERIC, " + roundDP + ") AS lower95" +
				", ROUND(c.upper95::NUMERIC, " + roundDP + ") AS upper95" +
				", ROUND(c.relative_risk::NUMERIC, " + roundDP + ") AS rr" +
				", ROUND(c.smoothed_relative_risk::NUMERIC, " + roundDP + ") AS sm_rr" +
				", ROUND(c.posterior_probability::NUMERIC, " + roundDP + ") AS post_prob" +
//				"/*, c.posterior_probability_upper95, c.posterior_probability_lower95" +
//				", c.residual_relative_risk, c.residual_rr_lower95, c.residual_rr_upper95 */" +
				", ROUND(c.smoothed_smr::NUMERIC, " + roundDP + ") AS sm_smr" +
				", ROUND(c.smoothed_smr_lower95::NUMERIC, " + roundDP + ") AS sm_smr_l95" +
				", ROUND(c.smoothed_smr_upper95::NUMERIC, " + roundDP + ") AS sm_smr_u95";
		}
		else {
			extraColumns=", b.zoomlevel, a.area_id, c.username, c.study_id, c.inv_id, c.band_id, c.genders" +
//				"/*, c.direct_standardisation */" +
				", ROUND(c.adjusted, " + roundDP + ") As adjusted, c.observed" +
				", ROUND(c.expected, " + roundDP + ") AS expected" +
				", ROUND(c.lower95, " + roundDP + ") AS lower95" +
				", ROUND(c.upper95, " + roundDP + ") AS upper95" +
				", ROUND(c.relative_risk, " + roundDP + ") AS rr" +
				", ROUND(c.smoothed_relative_risk, " + roundDP + ") AS sm_rr" +
				", ROUND(c.posterior_probability, " + roundDP + ") AS post_prob" +
//				"/*, c.posterior_probability_upper95, c.posterior_probability_lower95" +
//				", c.residual_relative_risk, c.residual_rr_lower95, c.residual_rr_upper95 */" +
				", ROUND(c.smoothed_smr, " + roundDP + ") AS sm_smr" +
				", ROUND(c.smoothed_smr_lower95, " + roundDP + ") AS sm_smr_l95" +
				", ROUND(c.smoothed_smr_upper95, " + roundDP + ") AS sm_smr_u95";
		}
		
		// Add alter 11 columns
		if (manager.doesColumnExist(connection, "rif_studies", mapTable.toLowerCase(), "intersect_count")) { 
			extraColumns=extraColumns + ", c.intersect_count";
		}
		if (manager.doesColumnExist(connection, "rif_studies", mapTable.toLowerCase(), "nearest_rifshapepolyid")) { 
			extraColumns=extraColumns + ", c.nearest_rifshapepolyid";
		}
		if (manager.doesColumnExist(connection, "rif_studies", mapTable.toLowerCase(), "distance_from_nearest_source")) { 
			if (databaseType == DatabaseType.POSTGRESQL) { 
				extraColumns=extraColumns + ", ROUND(c.distance_from_nearest_source::NUMERIC, " + roundDP + 
					") AS distance_from_nearest_source";
			}
			else {
				extraColumns=extraColumns + ", ROUND(c.distance_from_nearest_source, " + roundDP + 
					") AS distance_from_nearest_source";
			}
		}
		if (manager.doesColumnExist(connection, "rif_studies", mapTable.toLowerCase(), "exposure_value")) { 
			if (databaseType == DatabaseType.POSTGRESQL) { 
				extraColumns=extraColumns + ", ROUND(c.exposure_value::NUMERIC, " + roundDP + 
					") AS exposure_value";
			}
			else {
				extraColumns=extraColumns + ", ROUND(c.exposure_value, " + roundDP + 
					") AS exposure_value";
			}
		}
		
		String additionalJoin=null;
		if (isDiseaseMappingStudy) {
			additionalJoin="LEFT OUTER JOIN rif_studies." + mapTable.toLowerCase() + 
					" c ON (a.area_id = c.area_id) /* Disease mapping */";
		}
		else {
			additionalJoin="LEFT OUTER JOIN rif_studies." + mapTable.toLowerCase() + 
					" c ON (a.band_id = c.band_id) /* Risk analysis */";
		}
		RifFeatureCollection mapFeatureCollection=writeMapQueryTogeoJSONFile(
				connection,
				rifStudySubmission,
				"rif40_study_areas",
				temporaryDirectory,
				DATA_SUBDIRECTORY,
				"rif_data",				/* Schema */
				tileTableName.toString(),
				tileFileName.toString(),
				zoomLevel,
				studyID,
				null, 					/* areaType */
				extraColumns,			/* extraColumns: reduced to 10 characters */
				additionalJoin,			
				locale);
				
		rifMaps.writeResultsMaps(
				mapFeatureCollection,
				connection,
				temporaryDirectory,
				baseStudyName,
				zoomLevel,
				rifStudySubmission,
				rif40Studies,
				rif40Investigations,
				locale);
				
		return createMapsHTML(studyID, rif40Investigations, isDiseaseMappingStudy);
	}		

	/** 
	 * Create HTML to view maps in ZIP html app
     *  
	 * @param String studyID
	 * @param CachedRowSet rif40Investigations
	 *
	 * @returns HTML as string
	 */	
	private String createMapsHTML(
		final String studyID,
		final CachedRowSet rif40Investigations,
		final boolean isDiseaseMappingStudy)
			throws Exception {
			
		StringBuffer mapHTML=new StringBuffer();
		// Available: inv_id, inv_name, inv_description, genders, numer_tab, year_start, year_stop, 
		// min_age_group, max_age_group
		int genders=Integer.parseInt(manager.getColumnFromResultSet(rif40Investigations, "genders"));
		String invId=manager.getColumnFromResultSet(rif40Investigations, "inv_id");
		String invName=manager.getColumnFromResultSet(rif40Investigations, "inv_name");
		String invDescription=manager.getColumnFromResultSet(rif40Investigations, "inv_description");
		String numerTab=manager.getColumnFromResultSet(rif40Investigations, "numer_tab");
		String yearStart=manager.getColumnFromResultSet(rif40Investigations, "year_start");
		String yearStop=manager.getColumnFromResultSet(rif40Investigations, "year_stop");
			// Usefully will currently blob if >1 row
		Set<Sex> allSexes = null;
		Sex defaultGender = null;
		if (genders == Sex.MALES.getCode()) {
			allSexes=EnumSet.of(
				Sex.MALES);
			defaultGender = Sex.MALES;	
		}
		else if (genders == Sex.FEMALES.getCode()) {
			allSexes=EnumSet.of(
				Sex.FEMALES);
			defaultGender = Sex.FEMALES;	
		}
		else if (genders == Sex.BOTH.getCode()) {
			allSexes=EnumSet.of(
				Sex.MALES,   
				Sex.FEMALES,
				Sex.BOTH);
			defaultGender = Sex.BOTH;	
		}
		else {
			throw new Exception("Invalid gender code: " + genders);
		}
		
		// File name structure: <field>_<study ID>_inv<inv id>_<gender>_<printing DPI>.<extension>
		String nonFieldFileName=studyID + 
			"_inv" + invId + 
			"_" + defaultGender.getName().toLowerCase() + 
			"_" + printingDPI + "dpi.png\"";
		mapHTML.append("    <h1 id=\"maps\">Maps</h1>" + lineSeparator);
		mapHTML.append("      <h2 id=\"mapsInv\">Investigation: " + invId + ": " + invName + "; " + invDescription +
			"</h2>" + lineSeparator);
		mapHTML.append("      <p><il>" + lineSeparator);
		mapHTML.append("           <li>Numerator table: " + numerTab + "</li>" + lineSeparator);
		mapHTML.append("           <li>Period: " + yearStart + " to " + yearStop + "</li>" + lineSeparator);
		mapHTML.append("         </il></p>" + lineSeparator);	
		mapHTML.append("      <p>" + lineSeparator);	
		mapHTML.append("      <div>" + lineSeparator);
		mapHTML.append("        <form id=\"downloadForm2\" method=\"get\" action=\"maps\\smoothed_smr_" + 
			nonFieldFileName + ">" + lineSeparator);
		mapHTML.append("        Year: <select id=\"rifMapsList\">" + lineSeparator);	
		if (isDiseaseMappingStudy) {
			mapHTML.append("          <option value=\"maps\\relative_risk_" + 
				nonFieldFileName + "/>Relative Risk</option>" + 
				lineSeparator);		
			mapHTML.append("          <option value=\"maps\\posterior_probability_" + 
				nonFieldFileName + "/>Posterior Probability_</option>" + 
				lineSeparator);		
			mapHTML.append("          <option value=\"maps\\smoothed_smr_" + 
				nonFieldFileName + "\" selected />Smoothed SMR</option>" + 
				lineSeparator);
		}
		else {
			mapHTML.append("          <option value=\"maps\\relative_risk_" + 
				nonFieldFileName + "/>Relative Risk</option>" + 
				lineSeparator);	
		}
		mapHTML.append("        </select>" + lineSeparator);
		
		Iterator <Sex> GenderIter = allSexes.iterator();
		mapHTML.append("        Genders: <select id=\"rifMapsGender\">" + lineSeparator);					
		while (GenderIter.hasNext()) {
			Sex sex=GenderIter.next();
			if (sex.getCode() == defaultGender.getCode()) {
				mapHTML.append("          <option value=\"" + sex.getName().toLowerCase() + "\" selected />" + sex.getName() + "</option>" + 
					lineSeparator);
			}
			else {
				mapHTML.append("          <option value=\"" + sex.getName().toLowerCase() + "\" />" + sex.getName() + "</option>" + 
					lineSeparator);	
			}	
		}
		mapHTML.append("        </select></br>" + lineSeparator);
			
		mapHTML.append("        Graphics Format: <select id=\"rifMapsFileType\">" + lineSeparator);
		Set<RIFGraphicsOutputType> htmlOutputTypes = EnumSet.of( // Can be viewed in browser
		                                                         RIFGraphicsOutputType.RIFGRAPHICS_PNG,
		                                                         RIFGraphicsOutputType.RIFGRAPHICS_JPEG,
		                                                         RIFGraphicsOutputType.RIFGRAPHICS_GEOTIFF,
		                                                         RIFGraphicsOutputType.RIFGRAPHICS_SVG);
		Iterator <RIFGraphicsOutputType> htmlOutputTypeIter = htmlOutputTypes.iterator();
		int j=0;
		while (htmlOutputTypeIter.hasNext()) {
			String selected="";
			String disabled="";
			RIFGraphicsOutputType outputType=htmlOutputTypeIter.next();
			j++;
			if (outputType.getGraphicsExtentsion().equals("png")) {
				selected="selected";
			}
			if (!outputType.isRIFGraphicsOutputTypeEnabled()) {
				disabled="disabled";
			}
			mapHTML.append("          <option value=\"" + 
				outputType.getGraphicsExtentsion() +
				"\" " + disabled + " " +
				"id=\"" + outputType.getRIFGraphicsOutputTypeShortName().toLowerCase() + "Select\" " + 
				"title=\"" + outputType.getRIFGraphicsOutputTypeDescription() + "\" " + 
				selected + " />" + outputType.getRIFGraphicsOutputTypeShortName() + " (" + 
					outputType.getRIFGraphicsOutputTypeDescription() +
				")</option>" + lineSeparator);
		}
		mapHTML.append("        </select>" + lineSeparator);	
		mapHTML.append("        <button id=\"downloadButton2\" type=\"submit\">Download PNG</button>" + lineSeparator);
		mapHTML.append("        </form>" + lineSeparator);	
		mapHTML.append("      </div>" + lineSeparator);
			
		if (isDiseaseMappingStudy) {
			mapHTML.append("      <img src=\"maps\\smoothed_smr_" + 
				nonFieldFileName + " id=\"rifMaps\" width=\"80%\" />");
		}
		else {
			mapHTML.append("      <img src=\"maps\\relative_risk_" + 
				nonFieldFileName + " id=\"rifMaps\" width=\"80%\" />");
		}
		mapHTML.append("      </p>" + lineSeparator);	
		
		return mapHTML.toString();
	}

	/** 
	 * Query geolevel_id, geolevel_name, geography, srid, max_geojson_digits from rif40_studies, 
	 * rif40_geographies,
	 * bg_geolevel_id, bg_geolevel_name: for geolevel 2 if geolevel_id>2
     * 
	 * @param Connection connection, 
	 * @param String studyID, 
	 * @param String areaTableName
	 */	
	private CachedRowSet getRif40Geolevels(
			final Connection connection,
			final String studyID,
			final String areaTableName)
			throws Exception {
		SQLGeneralQueryFormatter geolevelQueryFormatter = new SQLGeneralQueryFormatter();	
		geolevelQueryFormatter.addQueryLine(0, "WITH a AS (");
		geolevelQueryFormatter.addQueryLine(1, "SELECT b.geolevel_id,");
		geolevelQueryFormatter.addQueryLine(1, "       CASE WHEN b.geolevel_id > 2 THEN 2 ELSE null END AS bg_geolevel_id,");
		geolevelQueryFormatter.addQueryLine(1, "       b.geolevel_name, c.geography, c.srid, c.max_geojson_digits");
		geolevelQueryFormatter.addQueryLine(1, "  FROM rif40.rif40_studies a, rif40.rif40_geolevels b, rif40.rif40_geographies c");
		geolevelQueryFormatter.addQueryLine(1, " WHERE study_id = ?");
		if (areaTableName.equals("rif40_comparison_areas")) {
			geolevelQueryFormatter.addQueryLine(1, "   AND a.comparison_geolevel_name = b.geolevel_name");
		} 
		else if (areaTableName.equals("rif40_study_areas")) {
			geolevelQueryFormatter.addQueryLine(1, "   AND a.study_geolevel_name = b.geolevel_name");
		} 
		else { // Map tables - same as study areas
			geolevelQueryFormatter.addQueryLine(1, "   AND a.study_geolevel_name = b.geolevel_name");
		}	
		geolevelQueryFormatter.addQueryLine(1, "   AND c.geography = b.geography");
		geolevelQueryFormatter.addQueryLine(0, ")");
		geolevelQueryFormatter.addQueryLine(0, "SELECT a.*, b1.geolevel_name AS bg_geolevel_name");
		geolevelQueryFormatter.addQueryLine(0, "   FROM a");
		geolevelQueryFormatter.addQueryLine(0, "		LEFT OUTER JOIN rif40.rif40_geolevels b1 ON (a.bg_geolevel_id = b1.geolevel_id");
		geolevelQueryFormatter.addQueryLine(0, "		 										 AND a.geography = b1.geography)");
		
		int[] params = new int[1];
		params[0]=Integer.parseInt(studyID);
		CachedRowSet cachedRowSet=manager.createCachedRowSet(connection, geolevelQueryFormatter,
			"writeMapQueryTogeoJSONFile", params);	
		
		return cachedRowSet;
	}
	
	/** 
	 * Create shapefile data store. Does not currently support <filename.shp.xml>: fgdc metadata
	 * (Needs the MetadataLinkTypeBinding class)
	 *
	 * @param File temporaryDirectory,
	 * @param String dirName, 
	 * @param String outputFileName,
	 * @param boolean enableIndexes
	 *
	 * @returns ShapefileDataStore
     */	
	private ShapefileDataStore createShapefileDataStore(
		final File temporaryDirectory,
		final String dirName, 
		final String outputFileName,
		final boolean enableIndexes) 
			throws Exception {
			
		String shapefileDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName +
			File.separator + outputFileName;
		File shapefileDirectory = new File(shapefileDirName);
		if (shapefileDirectory.exists()) {
			rifLogger.debug(this.getClass(), 
				"Found directory: " + shapefileDirectory.getAbsolutePath());
		}
		else {
			shapefileDirectory.mkdirs();
			rifLogger.info(this.getClass(), 
				"Created directory: " + shapefileDirectory.getAbsolutePath());
		}			
		String shapefileName=shapefileDirName + File.separator + outputFileName + ".shp";
		File shapefile=new File(shapefileName);			
		ShapefileDataStore shapeDataStore = new ShapefileDataStore(
			shapefile.toURI().toURL());
		shapeDataStore.setFidIndexed(enableIndexes);		// Enable indexes (DBF and SHAPEFILE)
		shapeDataStore.setIndexCreationEnabled(enableIndexes);
		
		rifLogger.info(this.getClass(), "Add shapefile to ZIP file: " + shapefileName);
		
		return shapeDataStore;
	}
		
	/** 
	 * Get CoordinateReferenceSystem using SRID [EXPERIMENTAL DOES NOT WORK: see Exception comment below]
	 *
	 * Error: NoSuchAuthorityCodeException: No code "EPSG:27700" from authority "EPSG" found for object of type "EngineeringCRS"
	 *
	 * a) Could use SRID from database
	 * b) Probably needs to access an [external?] geotools datbase
	 * c) Some defaults may be hard codable
	 *
	 * No support at present for anything other than WGS85 in the shapefile code 
	 * (i.e. re-projection required)
	 *
	 * @param RIFStudySubmission rifStudySubmission
	 * @param CachedRowSet rif40Geolevels
	 *
	 * @returns CoordinateReferenceSystem
     */	
	private CoordinateReferenceSystem getCRS(
			final RIFStudySubmission rifStudySubmission, 
			final CachedRowSet rif40Geolevels) 
				throws Exception {
		
		CoordinateReferenceSystem crs=null;
		
		String geographyName = manager.getColumnFromResultSet(rif40Geolevels, "geography");
		int srid=Integer.parseInt(manager.getColumnFromResultSet(rif40Geolevels, "srid"));
		try {
			crs = rifCoordinateReferenceSystem.getCRS(srid);	
		}
		catch (Exception exception) {
			rifStudySubmission.addStudyWarning(this.getClass(), 
				"Unable to deduce Coordinate Reference System for SRID: " + srid + "; using WGS84" +
					lineSeparator + exception.getMessage());
			crs = DefaultGeographicCRS.WGS84;
		}	
				
		return crs;
	}
		
	/** 
	 * Create GeoJSON writer 
	 *
	 * @param File temporaryDirectory,
	 * @param String dirName, 
	 * @param String outputFileName
	 *
	 * @returns BufferedWriter
     */	
	private BufferedWriter createGeoJSonWriter(
		final File temporaryDirectory,
		final String dirName, 
		final String outputFileName) 
			throws Exception {
		String geojsonDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName;
		File geojsonDirectory = new File(geojsonDirName);
		File newDirectory = new File(geojsonDirName);
		if (newDirectory.exists()) {
			rifLogger.debug(this.getClass(), 
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
		
		return bufferedWriter;
	}
	

	
	/** 
	 * Get referenced envelope for map, using the map study area extent
	 * the default is the defined extent of data Coordinate Reference System
	 * (i.e. the projection bounding box as a ReferencedEnvelope). 
	 * 
	 * Postgres SQL>
	 *
	 * WITH c AS (
	 * 		SELECT SST_Envelope(ST_Union(ST_Envelope(b.geom))) AS envelope
	 * 		  FROM rif40.rif40_study_areas a, rif_data.geometry_sahsuland b
	 *     WHERE a.study_id    = ?
	 * 		 AND b.geolevel_id = ? AND b.zoomlevel = ?
	 *		 AND a.area_id     = b.areaid
	 * )
	 * SELECT ST_Xmin(c.envelope) AS xmin,
	 *        ST_Xmax(c.envelope) AS xmax,
	 *        ST_Ymin(c.envelope) AS ymin,
	 *        ST_Ymax(c.envelope) AS ymax
	 *   FROM c;
	 *   
	 * SQL Server SQL>
	 *
	 * WITH c AS (
	 *    SELECT geometry::EnvelopeAggregate(b.geom) AS envelope
	 *      FROM rif40.rif40_study_areas a, rif_data.geometry_usa_2014 b
	 *     WHERE a.study_id    = ?
	 * 		 AND b.geolevel_id = ? AND b.zoomlevel = ?
	 *       AND a.area_id     = b.areaid
	 * )
	 * SELECT CAST(c.envelope.STPointN(1).STX AS numeric(8,5)) AS Xmin,
	 *        CAST(c.envelope.STPointN(3).STX AS numeric(8,5)) AS Xmax,
	 *        CAST(c.envelope.STPointN(1).STY AS numeric(8,5)) AS Ymin,
	 *        CAST(c.envelope.STPointN(3).STY AS numeric(8,5)) AS Ymax
	 *   FROM c;
	 *   
	 * @param Connection connection,
	 * @param String schemaName,
	 * @param String areaTableName,
	 * @param String tileTableName,
	 * @param String geolevel,
	 * @param String zoomLevel,
	 * @param String studyID,
	 * @param CoordinateReferenceSystem rif40GeographiesCRS,
	 * @param int srid
	 *
	 * @returns ReferencedEnvelope in database CRS (4326)
	 */
	private ReferencedEnvelope getMapReferencedEnvelope(
		final Connection connection,
		final String schemaName,
		final String areaTableName,
		final String tileTableName,
		final String geolevel,
		final String zoomLevel,
		final String studyID,
		final CoordinateReferenceSystem rif40GeographiesCRS,
		final int srid)  
			throws Exception {
				
		CoordinateReferenceSystem databaseCRS=DefaultGeographicCRS.WGS84; // 4326
		ReferencedEnvelope dbEnvelope = rifCoordinateReferenceSystem.getDefaultReferencedEnvelope(
			rif40GeographiesCRS); // Default is the geographical extent of rif40GeographiesCRS
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "WITH c AS (");
		if (databaseType == DatabaseType.POSTGRESQL) { 
			queryFormatter.addQueryLine(1, "SELECT ST_Envelope(ST_Union(ST_Envelope(b.geom))) AS envelope");			
		}
		else if (databaseType == DatabaseType.SQL_SERVER) {
			queryFormatter.addQueryLine(1, "SELECT geometry::EnvelopeAggregate(b.geom) AS envelope");	
		}
		queryFormatter.addQueryLine(1, "  FROM rif40." + areaTableName + " a, "  + 
											schemaName + "." + tileTableName.toLowerCase() + " b");
		queryFormatter.addQueryLine(1, " WHERE a.study_id    = ?");	
		queryFormatter.addQueryLine(1, "   AND b.geolevel_id = ? AND b.zoomlevel = ?");
		queryFormatter.addQueryLine(1, "   AND a.area_id     = b.areaid");
		queryFormatter.addQueryLine(0, ")");
		if (databaseType == DatabaseType.POSTGRESQL) { 
			queryFormatter.addQueryLine(0, "SELECT ST_AsText(ST_Transform(c.envelope, " + srid + ")) AS envelope_" + srid + ",");	
			queryFormatter.addQueryLine(0, "       ST_Xmin(c.envelope) AS xmin,");	
			queryFormatter.addQueryLine(0, "       ST_Xmax(c.envelope) AS xmax,");	
			queryFormatter.addQueryLine(0, "       ST_Ymin(c.envelope) AS ymin,");	
			queryFormatter.addQueryLine(0, "       ST_Ymax(c.envelope) AS ymax");	
		}
		else if (databaseType == DatabaseType.SQL_SERVER) {
			queryFormatter.addQueryLine(0, "SELECT c.envelope.STAsText() AS envelope_4326,"); // SQL Server cannot transform!!!
			queryFormatter.addQueryLine(0, "       CAST(c.envelope.STPointN(1).STX AS numeric(8,5)) AS Xmin,");
			queryFormatter.addQueryLine(0, "       CAST(c.envelope.STPointN(3).STX AS numeric(8,5)) AS Xmax,");
			queryFormatter.addQueryLine(0, "       CAST(c.envelope.STPointN(1).STY AS numeric(8,5)) AS Ymin,");
			queryFormatter.addQueryLine(0, "       CAST(c.envelope.STPointN(3).STY AS numeric(8,5)) AS Ymax");	
		}
		queryFormatter.addQueryLine(0, "  FROM c");
		
		PreparedStatement statement = manager.createPreparedStatement(connection, queryFormatter);
		ResultSet resultSet = null;
		try {	
			String[] queryArgs = new String[3];
			queryArgs[0]=studyID;
			queryArgs[1]=geolevel;
			queryArgs[2]=zoomLevel;
			manager.logSQLQuery("getMapReferencedEnvelope", queryFormatter, queryArgs);
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, Integer.parseInt(geolevel));
			statement.setInt(3, Integer.parseInt(zoomLevel));				
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				String envelopeText=resultSet.getString(1);
				Float xMin=resultSet.getFloat(2);
				Float xMax=resultSet.getFloat(3);
				Float yMin=resultSet.getFloat(4);
				Float yMax=resultSet.getFloat(5); // In 4326
				
				dbEnvelope = new ReferencedEnvelope(
					xMin /* bounds.getWestBoundLongitude() */,
					xMax /* bounds.getEastBoundLongitude() */,
					yMin /* bounds.getSouthBoundLatitude() */,
					yMax /* bounds.getNorthBoundLatitude() */,
					databaseCRS
				);		

				if (databaseType == DatabaseType.POSTGRESQL) { 
					rifLogger.info(this.getClass(), 
						"Get bounds from database bbox: [" + xMin + "," + yMin + " " + xMax + "," + yMax + "]" + lineSeparator +
						"dbEnvelope: " + dbEnvelope.toString() + lineSeparator +
						"db(in " + srid + "): "+ envelopeText);
				}
				else { 
					rifLogger.info(this.getClass(), 
						"bbox: [" + xMin + "," + yMin + " " + xMax + "," + yMax + "]" + lineSeparator +
						"dbEnvelope: " + dbEnvelope.toString() + lineSeparator +
						"db(in 4326): "+ envelopeText);
				}				
				if (resultSet.next()) {
					throw new Exception("getMapReferencedEnvelope(): expected 1 row, got many");
				}
			}
			else {
				throw new Exception("getMapReferencedEnvelope(): expected 1 row, got none");
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + queryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
		}
		
		return dbEnvelope;
	}
	
	/**
	 * Get background areas (as geolevel 2) so that partial mapping of a geography can have the rest of the 
	 * administrative boundaries added.
	 *
	 * SELECT b.wkt, b.areaid AS area_id, b.zoomlevel, c.areaname
     *   FROM rif_data.geometry_sahsuland b
     * 		LEFT OUTER JOIN rif_data.lookup_sahsu_grd_level1 c ON (b.areaid = c.sahsu_grd_level1)
     *  WHERE b.geolevel_id = ? AND b.zoomlevel = ?;	
	 *
	 * @param Connection connection,
	 * @param RIFStudySubmission rifStudySubmission
	 * @param String areaTableName,
	 * @param File temporaryDirectory,
	 * @param String dirName,
	 * @param String schemaName,
	 * @param String tileTableName,
	 * @param String geolevelName,
	 * @param String outputFileName,
	 * @param String zoomLevel,
	 * @param String geolevel, 
	 * @param CoordinateReferenceSystem rif40GeographiesCRS,
	 * @param Locale locale,
	 * @param MathTransform transform,
	 * @param int srid,
	 * @param String geographyName
	 *
	 * @returns DefaultFeatureCollection
     */	 
	private DefaultFeatureCollection getBackgroundAreas(
			final Connection connection,
			final RIFStudySubmission rifStudySubmission,
			final String areaTableName,
			final File temporaryDirectory,
			final String dirName,
			final String schemaName,
			final String tileTableName,
			final String geolevelName,
			final String outputFileName,
			final String zoomLevel,
			final String geolevel,
			final CoordinateReferenceSystem rif40GeographiesCRS,
			final Locale locale,
			final MathTransform transform,
			final int srid,
			final String geographyName)
					throws Exception {
			
		ShapefileDataStore shapeDataStore = createShapefileDataStore(temporaryDirectory,
			dirName, outputFileName, true /* enableIndexes */);	
		FeatureWriter<SimpleFeatureType, SimpleFeature> shapefileWriter = null; 
			// Created once feature types are defined

		DefaultFeatureCollection backgroundAreasFeatureCollection=null;
	
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();	
		queryFormatter.addQueryLine(0, "SELECT b.wkt, b.areaid AS area_id, b.zoomlevel, c.areaname");	
		queryFormatter.addQueryLine(0, "  FROM "  + schemaName + "." + tileTableName.toLowerCase() + " b");												
		queryFormatter.addQueryLine(2, "LEFT OUTER JOIN rif_data.lookup_" + geolevelName.toLowerCase() + 
			" c ON (b.areaid = c." + geolevelName.toLowerCase() + ")");
		queryFormatter.addQueryLine(0, " WHERE b.geolevel_id = ? AND b.zoomlevel = ?");		
		
		PreparedStatement statement = manager.createPreparedStatement(connection, queryFormatter);
		
		SimpleFeatureType simpleFeatureType=null;
		
		try {	
			ResultSet resultSet = null;
			String[] queryArgs = new String[2];
			queryArgs[0]=geolevel;
			queryArgs[1]=zoomLevel;
			manager.logSQLQuery("getBackgroundAreas", queryFormatter, queryArgs);
			statement = manager.createPreparedStatement(connection, queryFormatter);
			statement.setInt(1, Integer.parseInt(geolevel));
			statement.setInt(2, Integer.parseInt(zoomLevel));				
			resultSet = statement.executeQuery();
			
			int i = 0;
		
			while (resultSet.next()) {
				
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				i++;
				
				String areaId = "(????)";
				for (int j = 2; j <= columnCount; j++) {		
					String name = rsmd.getColumnName(j);
					if (name.equals("area_id")) {
						areaId = resultSet.getString(j);
					}
				}				
				Geometry geometry = rifWellKnownText.createGeometryFromWkt(resultSet.getString(1), geolevel, Integer.parseInt(zoomLevel), 
					areaId);
				if (i == 1) {
					simpleFeatureType=setupShapefile(rsmd, columnCount, shapeDataStore, null /* areaType */, 
						outputFileName, geometry, rif40GeographiesCRS);
					
					shapefileWriter = shapeDataStore.getFeatureWriter(shapeDataStore.getTypeNames()[0],
							Transaction.AUTO_COMMIT);				
					backgroundAreasFeatureCollection = new DefaultFeatureCollection(geolevelName, 
						simpleFeatureType);
				}
				SimpleFeature shapefileFeature = (SimpleFeature) shapefileWriter.next(); 
				SimpleFeatureBuilder builder = new SimpleFeatureBuilder(simpleFeatureType);
				if (i == 1) {		
					printShapefileColumns(shapefileFeature, rsmd, outputFileName, rif40GeographiesCRS, geographyName, srid);
				}	
				
				AttributeDescriptor ad = shapefileFeature.getType().getDescriptor(0); // Create shapefile feature
					// Add first hsapefile feature attribute
				if (ad instanceof GeometryDescriptor) { 
					// Need to handle CoordinateReferenceSystem
					if (CRS.toSRS(rif40GeographiesCRS).equals(CRS.toSRS(DefaultGeographicCRS.WGS84))) {
						Geometries geomType = Geometries.get(geometry);
						switch (geomType) {
							case POLYGON: // Convert POLYGON to MULTIPOLYGON
								GeometryBuilder geometryBuilder = new GeometryBuilder(geometryFactory);
								Polygon polygons[] = new Polygon[1];
								polygons[0]=(Polygon)geometry;
								MultiPolygon multipolygon=geometryBuilder.multiPolygon(polygons);
								shapefileFeature.setAttribute(0, multipolygon); 
								builder.set(0, multipolygon); 
								break;
							case MULTIPOLYGON:
								shapefileFeature.setAttribute(0, geometry); 
								builder.set(0, geometry); 
								break;
							default:
								throw new Exception("Unsupported Geometry:" + geomType.toString());
						}
					} 
					else if (transform == null) {
						throw new Exception("Null transform from: " + CRS.toSRS(rif40GeographiesCRS) + " to: " +
							CRS.toSRS(DefaultGeographicCRS.WGS84));
					}
					else { // Transform from WGS84 to SRID CRS
						Geometry newGeometry = JTS.transform(geometry, transform); // Re-project
						shapefileFeature.setAttribute(0, newGeometry); 
						builder.set(0, newGeometry); 
					}
				} 
				else { 
					throw new Exception("First attribute is not MultiPolygon: " + 
						ad.getName().toString());
				}

				shapefileFeature.setAttribute(1, geolevelName); 
				builder.set(1, geolevelName); 
				
				// The column count starts from 2
				for (int j = 2; j <= columnCount; j++ ) {		
					String name = rsmd.getColumnName(j);
					String value = resultSet.getString(j);	
					String columnType = rsmd.getColumnTypeName(j);
					
					addDatumToShapefile(shapefileFeature, builder,
						null, name, value, columnType, j, i,
						locale);
				}
					
				backgroundAreasFeatureCollection.add(builder.buildFeature("id" + i));
				shapefileWriter.write();
			} // End of while loop			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + queryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			closeStatement(statement);
			if (shapefileWriter != null) {
				shapefileWriter.close();	
			}	
			connection.commit();
		}
		
		return backgroundAreasFeatureCollection;
	}
	
	/** 
     * Write results map query to geoJSON file and shapefile
     * 
	 * Query types:
	 *
     * Type 1: areaType: C; extraColumns: a.area_id, b.zoomlevel; no additionalJoin
     * 
     * WITH a AS (
     * 	SELECT *
     * 	  FROM rif40.rif40_comparison_areas
     * 	 WHERE study_id = ?
     * )
     * SELECT ST_AsText(ST_ForceRHR(b.geom)) AS wkt, a.area_id, b.zoomlevel, c.areaname
     *   FROM a
     *         LEFT OUTER JOIN rif_data.geometry_sahsuland b ON (a.area_id = b.areaid)
     * 		LEFT OUTER JOIN rif_data.lookup_sahsu_grd_level1 c ON (a.area_id = c.sahsu_grd_level1)
     *  WHERE b.geolevel_id = ? AND b.zoomlevel = ?;	
     * 
     * Type 2: areaType: S; extraColumns: a.area_id, a.band_id, b.zoomlevel; no additionalJoin
     * 
     * WITH a AS (
     * 	SELECT *
     * 	  FROM rif40.rif40_study_areas
     * 	 WHERE study_id = ?
     * )
     * SELECT ST_AsText(ST_ForceRHR(b.geom)) AS wkt, a.area_id, a.band_id, b.zoomlevel, c.areaname
     *   FROM a
     *         	LEFT OUTER JOIN rif_data.geometry_sahsuland b ON (a.area_id = b.areaid)
     * 			LEFT OUTER JOIN rif_data.lookup_sahsu_grd_level4 c ON (a.area_id = c.sahsu_grd_level4)
     *  WHERE b.geolevel_id = ? AND b.zoomlevel = ?;	
     * 
     * Type 3: areaType IS NULL; extraColumns: b.zoomlevel, c.*; additionalJoin: 
     * 			LEFT OUTER JOIN rif_studies.s367_map c ON (a.area_id = c.area_id)
     *
     * WITH a AS (
     * 	SELECT *
     *	  FROM rif40.rif40_study_areas
     *	 WHERE study_id = ?
     * )
     * SELECT ST_AsText(ST_ForceRHR(b.geom)) AS wkt, b.zoomlevel, c.*
     *   FROM a
     *        	LEFT OUTER JOIN rif_data.geometry_sahsuland b ON (a.area_id = b.areaid)
     *        	LEFT OUTER JOIN rif_studies.s367_map c ON (a.area_id = c.area_id);
     *
	 * @param Connection connection,
	 * @param RIFStudySubmission rifStudySubmission
	 * @param String areaTableName,
	 * @param File temporaryDirectory,
	 * @param String dirName,
	 * @param String schemaName,
	 * @param String tileTableName,
	 * @param String outputFileName,
	 * @param String zoomLevel,
	 * @param String studyID,
	 * @param String areaType,
	 * @param String extraColumns,
	 * @param String additionalJoin,
	 * @param Locale locale	 
	 *
	 * @returns RifFeatureCollection
     */	 
	private RifFeatureCollection writeMapQueryTogeoJSONFile(
			final Connection connection,
			final RIFStudySubmission rifStudySubmission,
			final String areaTableName,
			final File temporaryDirectory,
			final String dirName,
			final String schemaName,
			final String tileTableName,
			final String outputFileName,
			final String zoomLevel,
			final String studyID,
			final String areaType,
			final String extraColumns,
			final String additionalJoin,
			final Locale locale)
					throws Exception {
			
		RifLocale rifLocale = new RifLocale(locale);			
		Calendar calendar = rifLocale.getCalendar();			
		DateFormat df = rifLocale.getDateFormat();

		BufferedWriter bufferedWriter = createGeoJSonWriter(temporaryDirectory,
			dirName, outputFileName);

		ShapefileDataStore shapeDataStore = createShapefileDataStore(temporaryDirectory,
			dirName, outputFileName, true /* enableIndexes */);	
		FeatureWriter<SimpleFeatureType, SimpleFeature> shapefileWriter = null; 
			// Created once feature types are defined
		
		CachedRowSet rif40Geolevels=getRif40Geolevels(connection, studyID, areaTableName);	
			//get geolevel
		String geolevel=manager.getColumnFromResultSet(rif40Geolevels, "geolevel_id");
		String geolevelName = manager.getColumnFromResultSet(rif40Geolevels, "geolevel_name");
		int max_geojson_digits=Integer.parseInt(manager.getColumnFromResultSet(rif40Geolevels, "max_geojson_digits"));
		int srid=Integer.parseInt(manager.getColumnFromResultSet(rif40Geolevels, "srid"));
		String geographyName = manager.getColumnFromResultSet(rif40Geolevels, "geography");
		CoordinateReferenceSystem rif40GeographiesCRS = getCRS(rifStudySubmission, rif40Geolevels);
		MathTransform transform = null; // For re-projection
		if (!CRS.toSRS(rif40GeographiesCRS).equals(CRS.toSRS(DefaultGeographicCRS.WGS84))) {
			transform = rifCoordinateReferenceSystem.getMathTransform(rif40GeographiesCRS);
		}
		
		ReferencedEnvelope envelope=getMapReferencedEnvelope(connection, schemaName, areaTableName,tileTableName, 
			geolevel, zoomLevel, studyID, rif40GeographiesCRS, srid);
			
		SQLGeneralQueryFormatter queryFormatter = new SQLGeneralQueryFormatter();
		queryFormatter.addQueryLine(0, "WITH a AS (");
		queryFormatter.addQueryLine(0, "	SELECT *");
		queryFormatter.addQueryLine(0, "	  FROM rif40." + areaTableName);
		queryFormatter.addQueryLine(0, "	 WHERE study_id = ?");
		queryFormatter.addQueryLine(0, ")");
		queryFormatter.addQueryLine(0, "SELECT b.wkt");					
		if (extraColumns != null) {
			queryFormatter.addQueryLine(0, "      " + extraColumns);
		}
		queryFormatter.addQueryLine(0, "  FROM a");
		queryFormatter.addQueryLine(0, "        LEFT OUTER JOIN "  + 
															schemaName + "." + tileTableName.toLowerCase() + 
															" b ON (a.area_id = b.areaid)");												
		if (additionalJoin != null) {
			queryFormatter.addQueryLine(0, additionalJoin);
		}
		else {
			queryFormatter.addQueryLine(0, "LEFT OUTER JOIN rif_data.lookup_" + geolevelName + 
				" c ON (a.area_id = c." + geolevelName + ")");
		}	
		queryFormatter.addQueryLine(0, " WHERE b.geolevel_id = ? AND b.zoomlevel = ?");	
		
		PreparedStatement statement = manager.createPreparedStatement(connection, queryFormatter);
		
		DefaultFeatureCollection featureCollection = null;
		SimpleFeatureType simpleFeatureType=null;
		
		try {	
			ResultSet resultSet = null;
			String[] queryArgs = new String[3];
			queryArgs[0]=studyID;
			queryArgs[1]=geolevel;
			queryArgs[2]=zoomLevel;
			manager.logSQLQuery("writeMapQueryTogeoJSONFile", queryFormatter, queryArgs);
			statement = manager.createPreparedStatement(connection, queryFormatter);
			statement.setInt(1, Integer.parseInt(studyID));	
			statement.setInt(2, Integer.parseInt(geolevel));
			statement.setInt(3, Integer.parseInt(zoomLevel));				
			resultSet = statement.executeQuery();
			
			//Write WKT to geoJSON
			int i = 0;
			
			rifLogger.debug(this.getClass(), "Bounding box: " + geoJSONWriter.toString((BoundingBox)envelope));
				// In 4236
			bufferedWriter.write("{\"type\":\"FeatureCollection\",");
			bufferedWriter.write("\"bbox\":" + geoJSONWriter.toString((BoundingBox)envelope) + ","); 
				// e.g. "bbox": [52.6876106262207,-7.588294982910156,55.52680969238281,-4.886538028717041],
			bufferedWriter.write("\"features\":[");	
		
			while (resultSet.next()) {
				StringBuffer stringFeature = new StringBuffer();
				
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				i++;
				
				String areaId = "(????)";
				for (int j = 2; j <= columnCount; j++) {		
					String name = rsmd.getColumnName(j);
					if (name.equals("area_id")) {
						areaId = resultSet.getString(j);
					}
				}		
				Geometry geometry = null;
				if (rifWellKnownText == null) {
					rifWellKnownText = new RifWellKnownText();
				}
				try {
					String wkt=resultSet.getString(1);
					int zl=Integer.parseInt(zoomLevel);
					geometry = rifWellKnownText.createGeometryFromWkt(
						wkt, geolevel, zl, areaId);
				}
				catch (Exception exception) {
					rifLogger.error(getClass(), "Error in createGeometryFromWkt for areaId: " + areaId, exception);
					throw exception;
				}
				
				stringFeature.append("{\"type\":\"Feature\",\"geometry\":"); // GeoJSON feature header 	
				stringFeature.append(geoJSONWriter.toString(geometry));
				if (i == 1) {
					simpleFeatureType=setupShapefile(rsmd, columnCount, shapeDataStore, areaType, outputFileName, 
						geometry, rif40GeographiesCRS);
					
					shapefileWriter = shapeDataStore.getFeatureWriter(shapeDataStore.getTypeNames()[0],
							Transaction.AUTO_COMMIT);				
							
					if (areaType != null) {	
						featureCollection = new DefaultFeatureCollection(areaType, simpleFeatureType);
					}
					else {
						featureCollection = new DefaultFeatureCollection("Results", simpleFeatureType);
					}
				}
				SimpleFeature shapefileFeature = (SimpleFeature) shapefileWriter.next(); 
				SimpleFeatureBuilder builder = new SimpleFeatureBuilder(simpleFeatureType);
				if (i == 1) {		
					printShapefileColumns(shapefileFeature, rsmd, outputFileName, rif40GeographiesCRS, geographyName, srid);
				}	
				
				AttributeDescriptor ad = shapefileFeature.getType().getDescriptor(0); // Create shapefile feature
					// Add first hsapefile feature attribute
				if (ad instanceof GeometryDescriptor) { 
					// Need to handle CoordinateReferenceSystem
					if (CRS.toSRS(rif40GeographiesCRS).equals(CRS.toSRS(DefaultGeographicCRS.WGS84))) {
						// Multipolygon conversion now done by RifWellKnownText
					} 
					else if (transform == null) {
						throw new Exception("Null transform from: " + CRS.toSRS(rif40GeographiesCRS) + " to: " +
							CRS.toSRS(DefaultGeographicCRS.WGS84));
					}
					else { // Transform from WGS84 to SRID CRS
						Geometry newGeometry = JTS.transform(geometry, transform); // Re-project
						shapefileFeature.setAttribute(0, newGeometry); 
						builder.set(0, newGeometry); 
					}
				} 
				else { 
					throw new Exception("First attribute is not MultiPolygon: " + 
						ad.getName().toString());
				}
				
				stringFeature.append(",\"properties\":{"); // Add DBF properties
				if (areaType != null) {
					stringFeature.append("\"areatype\":\"" + areaType + "\"");
					shapefileFeature.setAttribute(1, areaType); 
					builder.set(1, areaType); 
				}
				else {
					stringFeature.append("\"maptype\":\"Results\"");
					shapefileFeature.setAttribute(1, "results"); 
					builder.set(1, "results"); 
				}
				
				if (extraColumns != null) {
					
					// The column count starts from 2
					for (int j = 2; j <= columnCount; j++ ) {		
						String name = rsmd.getColumnName(j);
						String value = resultSet.getString(j);	
						String columnType = rsmd.getColumnTypeName(j);
						if (columnType.equals("timestamp") ||
							columnType.equals("timestamptz") ||
							columnType.equals("datetime")) {
							Timestamp dateTimeValue=resultSet.getTimestamp(i, calendar);
							value=df.format(dateTimeValue);
						}
						addDatumToShapefile(shapefileFeature, builder,
							stringFeature, name, value, columnType, j, i,
							locale);
					}
				}				
				
				featureCollection.add(builder.buildFeature("id" + i));		
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
			closeStatement(statement);

			bufferedWriter.flush();
			bufferedWriter.close();	
			if (shapefileWriter != null) {
				shapefileWriter.close();	
			}	
			connection.commit();
		}
		
		String backgroundAreasGeolevel=manager.getColumnFromResultSet(rif40Geolevels, "bg_geolevel_id",
			true /* allowNulls */, false /*  allowNoRows */);
		String backgroundAreasGeolevelName=manager.getColumnFromResultSet(rif40Geolevels, "bg_geolevel_name",
			true /* allowNulls */, false /*  allowNoRows */);
		DefaultFeatureCollection backgroundAreasFeatureCollection=null;
		if (backgroundAreasGeolevelName != null) {
			String backgroundAreasOutputFileName=backgroundAreasGeolevelName.toLowerCase() + "_map";
			
			backgroundAreasFeatureCollection=getBackgroundAreas(
				connection,
				rifStudySubmission,
				areaTableName,
				temporaryDirectory,
				dirName,
				schemaName,
				tileTableName,
				backgroundAreasGeolevelName,
				backgroundAreasOutputFileName,
				zoomLevel,
				backgroundAreasGeolevel,
				rif40GeographiesCRS,
				locale,
				transform,
				srid,
				geographyName);
		}
		
		RifFeatureCollection rifFeatureCollection=new RifFeatureCollection(
			featureCollection, 
			backgroundAreasFeatureCollection,
			rif40GeographiesCRS);
		rifFeatureCollection.SetupRifFeatureCollection();
		
		return rifFeatureCollection;
	}
	
	/** 
	 * Setup shapefile using feature builder. Defines field meta data and the coordinate reference 
	 * system, currently WGS84. Could use the SRID from rif40_geographies to use the original SRID.
	 * Supports POLYGONs, MULTIPOLYGONs, Double and Long. Everything else stays as String
	 *
	 * @param ResultSetMetaData rsmd, 
	 * @param int columnCount, 
	 * @param ShapefileDataStore dataStore, 
	 * @param String areaType, 
	 * @param String featureSetName, 
	 * @param Geometry geometry,
	 * @param CoordinateReferenceSystem crs
	 *
	 * @returns SimpleFeatureType
	 *
	 * https://www.programcreek.com/java-api-examples/index.php?source_dir=geotools-old-master/modules/library/render/src/test/java/org/geotools/renderer/lite/LabelObstacleTest.java
     */
	  private SimpleFeatureType setupShapefile(
			final ResultSetMetaData rsmd, 
			final int columnCount, 
			final ShapefileDataStore dataStore, 
			final String areaType, 
			final String featureSetName, 
			final Geometry geometry,
			final CoordinateReferenceSystem crs)
			throws Exception {
		
		SimpleFeatureTypeBuilder featureBuilder = new SimpleFeatureTypeBuilder();
		
		if (crs == null) {
			featureBuilder.setCRS(DefaultGeographicCRS.WGS84); // <- Default coordinate reference system
		}
		else if (CRS.toSRS(crs).equals(CRS.toSRS(DefaultGeographicCRS.WGS84))) {
			featureBuilder.setCRS(DefaultGeographicCRS.WGS84); // <- Default coordinate reference system
		}
		else {
			featureBuilder.setCRS(crs); // <- Geography SRID coordinate reference system
		}
        featureBuilder.setName(featureSetName);  
		Geometries geomType = Geometries.get(geometry);
		switch (geomType) {
			case POLYGON: // Force to multipolygon
				featureBuilder.add("geometry", MultiPolygon.class);
				break;
			case MULTIPOLYGON:
				featureBuilder.add("geometry", MultiPolygon.class);
				break;
			default:
				throw new Exception("Unsupported Geometry:" + geomType.toString());
		}
		featureBuilder.length(7).add("areatype", String.class);
		
		// The column count starts from 2
		for (int k = 2; k <= columnCount; k++ ) {
			String name = rsmd.getColumnName(k);
			String columnType = rsmd.getColumnTypeName(k);
			if (columnType.equals("integer") || 
				columnType.equals("bigint") || 
				columnType.equals("int4") ||
				columnType.equals("int") ||
				columnType.equals("smallint")) {	
				featureBuilder.add(name, Long.class);
			}				
			else if (columnType.equals("float") || 
					 columnType.equals("float8") || 
					 columnType.equals("double precision") ||
					 columnType.equals("numeric")) {
				featureBuilder.add(name, Double.class); // geotools uses double instead of float
			}
			else {
				featureBuilder.add(name, String.class);	
			}
		}								
		
		// build the type
		SimpleFeatureType featureType = featureBuilder.buildFeatureType();
		
		dataStore.createSchema(featureType);
		
		return featureType;
	}
	
	/**
	 * Add datum point to shapefile
	 *	
     * @param SimpleFeature shapefileFeature (required)
     * @param SimpleFeatureBuilder builder (required)
     * @param StringBuffer stringFeature (required)
     * @param String name (required)
     * @param String value (required)
     * @param String columnType (required)
     * @param int columnIndex (required)
     * @param int rowCount (required)
     * @param int Locale locale (required)
	 */	
	private void addDatumToShapefile(
		final SimpleFeature shapefileFeature, 
		final SimpleFeatureBuilder builder, 
		final StringBuffer stringFeature, 
		final String name, 
		final String value, 
		final String columnType, 
		final int columnIndex, 
		final int rowCount, 
		final Locale locale)
			throws Exception {
	
		AttributeDescriptor ad = shapefileFeature.getType().getDescriptor(columnIndex); 
		String featureName = ad.getName().toString();	
		if (rowCount < 2) {
			if (ad instanceof GeometryDescriptor) { 
				throw new Exception("Shapefile attribute is Geometry when expecting non geospatial type for column: " + 
					name + ", columnIndex: " + columnIndex + "; type: " + ad.getType().getBinding());
			}
			
			if (!name.equals(featureName)) {
				throw new Exception("Shapefile attribute name: " + featureName + 
					" does not match [truncated] column name: " + name + ", columnIndex: " + columnIndex + 
					"; type: " + ad.getType().getBinding());
			}
		}

		String newValue=value;
		Long longVal=null;
		Double doubleVal=null;
		if (value != null && (
			columnType.equals("integer") || 
			columnType.equals("bigint") || 
			columnType.equals("int4") ||
			columnType.equals("int") ||
			columnType.equals("smallint"))) {
			try {
				longVal=Long.parseLong(value);
				newValue=NumberFormat.getNumberInstance(locale).format(longVal);
			}
			catch (Exception exception) {
				rifLogger.error(this.getClass(), "Unable to parseLong(" + 
					columnType + "): " + value +
					"; row: " + rowCount +
					"; column: " + name + ", columnIndex: " + columnIndex,
					exception);
				throw exception;
			}
		}
		else if (value != null && (
				 columnType.equals("float") || 
				 columnType.equals("float8") || 
				 columnType.equals("double precision") ||
				 columnType.equals("numeric"))) {
			try {
				doubleVal=Double.parseDouble(value);
				newValue=NumberFormat.getNumberInstance(locale).format(doubleVal);
			}
			catch (Exception exception) {
				rifLogger.error(this.getClass(), "Unable to parseDouble(" + 
					columnType + "): " + value +
					"; row: " + rowCount +
					"; column: " + name + ", columnIndex: " + columnIndex,
					exception);
				throw exception;
			}
		}		
		
		if (stringFeature != null) {			
			stringFeature.append(",\"" + name + "\":\"" + newValue + "\"");
		}
		
		try {
			if (ad.getType().getBinding() == Double.class) {
				shapefileFeature.setAttribute(columnIndex, doubleVal);
				builder.set(columnIndex, doubleVal);
			}
			else if (ad.getType().getBinding() == Long.class) {
				shapefileFeature.setAttribute(columnIndex, longVal);
				builder.set(columnIndex, longVal);
			}
			else if (ad.getType().getBinding() == String.class) {
				shapefileFeature.setAttribute(columnIndex, newValue);
				builder.set(columnIndex, newValue);
			}
			else {
				throw new Exception("Unsupported attribute type: " + ad.getType().getBinding());
			}
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in addDatumToShapefile() row: " + rowCount +
				"; column: " + name + ", columnIndex: " + columnIndex + 
				"; type: " + ad.getType().getBinding(),
				exception);
			throw exception;
		}
	} 
	
	/**
	 * Print shapefile and database cilumn names and types
	 *	
     * @param SimpleFeature feature (required),
     * @param ResultSetMetaData rsmd (required),
     * @param String outputFileName (required),
	 * @param CoordinateReferenceSystem crs,
	 * @param String geographyName,
	 * @param int srid
	 *
	 * E.g.
	 * 11:32:25.396 [http-nio-8080-exec-105] INFO  RIFLogger : [rifServices.datastorage.common.RifGeospatialOutputs]:
	 * Database: POSTGRESQL
	 * Column[1]: wkt; DBF name: WKT; truncated: false; type: text
	 * Column[2]: area_id; DBF name: AREA_ID; truncated: false; type: varchar
	 * Column[3]: band_id; DBF name: BAND_ID; truncated: false; type: int4
	 * Column[4]: zoomlevel; DBF name: ZOOMLEVEL; truncated: false; type: int4
	 * Column[5]: areaname; DBF name: AREANAME; truncated: false; type: varchar
	 * Shapefile: s367_1002_lung_cancer_studyArea
	 * Feature[0]: the_geom; type: GeometryTypeImpl MultiPolygon<MultiPolygon>
	 * Feature[1]: areatype; type: AttributeTypeImpl areatype<String>
	 * restrictions=[ length([.]) <= 7 ]
	 * Feature[2]: area_id; type: AttributeTypeImpl area_id<String>
	 * restrictions=[ length([.]) <= 254 ]
	 * Feature[3]: band_id; type: AttributeTypeImpl band_id<String>
	 * restrictions=[ length([.]) <= 254 ]
	 * Feature[4]: zoomlevel; type: AttributeTypeImpl zoomlevel<String>
	 * restrictions=[ length([.]) <= 254 ]
	 * Feature[5]: areaname; type: AttributeTypeImpl areaname<String>
	 * restrictions=[ length([.]) <= 254 ]
	 * 	
	 */
	private void printShapefileColumns(
		final SimpleFeature feature, 
		final ResultSetMetaData rsmd,
		final String outputFileName,
		final CoordinateReferenceSystem crs,
		final String geographyName,
		final int srid)
			throws Exception {
							
		StringBuilder sb = new StringBuilder();
		
		int truncatedCount=0;
		
		String crsWkt = crs.toWKT();
		sb.append("Geography: " + geographyName + "; SRID: " + srid + "; CRS wkt: " + crsWkt);
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
	private void closeStatement(PreparedStatement statement) {

		if (statement == null) {
			return;
		}

		try {
			statement.close();
		}
		catch(SQLException ignore) {}
	}
}	
