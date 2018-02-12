package rifServices.graphics;

import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifServices.businessConceptLayer.RIFStudySubmission;

import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifServices.dataStorageLayer.common.SQLAbstractSQLManager;

import rifServices.dataStorageLayer.common.RifGeospatialOutputs;
import rifServices.dataStorageLayer.common.RifCoordinateReferenceSystem;
import rifServices.dataStorageLayer.common.RifLocale;

import com.sun.rowset.CachedRowSetImpl;
import java.io.*;
import java.sql.*;
import org.json.*;
import java.lang.*;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.Locale;

import org.w3c.dom.Document; 
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGeneratorContext;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Dimension;

import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.StyleGenerator;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.JTS;

import org.geotools.grid.Grids;

import org.geotools.data.simple.SimpleFeatureSource;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.CRS;

import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;

import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Stroke;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.StyleBuilder;

import org.geotools.filter.function.Classifier;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;

import org.geotools.factory.CommonFactoryFinder;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Function;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

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
	
public class RIFMaps extends SQLAbstractSQLManager {
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
		
	private RIFServiceStartupOptions rifServiceStartupOptions;
	private static DatabaseType databaseType;
	private RifGeospatialOutputs rifGeospatialOutputs = null;
	private static RifCoordinateReferenceSystem rifCoordinateReferenceSystem = 
		new RifCoordinateReferenceSystem();

	private static final String MAPS_SUBDIRECTORY = "maps";
	
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
	public RIFMaps(
			final RIFServiceStartupOptions rifServiceStartupOptions) {
		super(rifServiceStartupOptions.getRIFDatabaseProperties());
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		try {
			
			rifGeospatialOutputs = 
				new RifGeospatialOutputs(rifServiceStartupOptions);
//			denominatorPyramidWidthPixels=this.rifServiceStartupOptions.getOptionalRIfServiceProperty(
//					"denominatorPyramidWidthPixels", 3543);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFMaps() constructor");
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
	 * @param DefaultFeatureCollection featureCollection,
	 * @param Connection connection, 
	 * @param File temporaryDirectory,
	 * @param String baseStudyName,
	 * @param String zoomLevel,
	 * @param RIFStudySubmission rifStudySubmission,
	 * @param CachedRowSetImpl rif40Studies,
	 * @param Locale locale
	 */		
	public void writeResultsMaps(
			final DefaultFeatureCollection featureCollection,
			final Connection connection,
			final File temporaryDirectory,
			final String baseStudyName,
			final String zoomLevel,
			final RIFStudySubmission rifStudySubmission,
			final CachedRowSetImpl rif40Studies,
			final Locale locale)
					throws Exception {
	
			
		RifLocale rifLocale = new RifLocale(locale);			
		Calendar calendar = rifLocale.getCalendar();			
		DateFormat df = rifLocale.getDateFormat();
		
		String studyID = rifStudySubmission.getStudyID();
		String mapTable=getColumnFromResultSet(rif40Studies, "map_table");
		
		//Add geographies to zip file
		StringBuilder tileTableName = new StringBuilder();	
		tileTableName.append("geometry_");
		String geog = rifStudySubmission.getStudy().getGeography().getName();			
		tileTableName.append(geog);

		CachedRowSetImpl rif40Geolevels=rifGeospatialOutputs.getRif40Geolevels(connection, studyID, 
			"rif40_study_areas" 	/* areaTableName */);	
			//get geolevel
		String geolevel=getColumnFromResultSet(rif40Geolevels, "geolevel_id");
		String geolevelName = getColumnFromResultSet(rif40Geolevels, "geolevel_name");
		int max_geojson_digits=Integer.parseInt(getColumnFromResultSet(rif40Geolevels, "max_geojson_digits"));
		CoordinateReferenceSystem crs = rifGeospatialOutputs.getCRS(rifStudySubmission, rif40Geolevels);
	
		writeMap(
			featureCollection,
			connection,
			temporaryDirectory,
			"Smoothed SMR map"		/* mapTitle */, 
			"smoothed_smr"			/* mapColumn */,
			baseStudyName,
			"rif40_study_areas" 	/* areaTableName */,
			tileTableName.toString(),
			geolevel,
			geolevelName,
			"rif_data"				/* schemaName */,
			studyID,
			zoomLevel,
			crs						/* CoordinateReferenceSystem */);

	}
	
	private void writeMap(
		final DefaultFeatureCollection featureCollection,
		final Connection connection,
		final File temporaryDirectory,
		final String mapTitle,
		final String mapColumn,
		final String baseStudyName,
		final String areaTableName,
		final String tileTableName,
		final String geolevel,
		final String geolevelName,
		final String schemaName,
		final String studyID,
		final String zoomLevel,
		final CoordinateReferenceSystem crs) 
			throws Exception {
				
		//Create map
		StringBuilder mapFileName = new StringBuilder();
		mapFileName.append(baseStudyName);
		mapFileName.append("_" + mapColumn + "_map");
		MapContent map = new MapContent();
		map.setTitle(mapTitle);

		// Set projection
		MapViewport vp = map.getViewport();
		vp.setCoordinateReferenceSystem(crs);
		ReferencedEnvelope envelope=rifGeospatialOutputs.getMapReferencedEnvelope(
			connection, schemaName, areaTableName, tileTableName, 
			geolevel, zoomLevel, studyID, vp /* MapViewport */, crs);
		vp.setBounds(envelope);	

		Style style=createRifStyle(
			"Quantile"		/* Classifier function name */, 
			"sm_smr"		/* Column */, 
			"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
			8				/* numberOfBreaks */, 
			featureCollection);
			
		// Add layers to map			
        FeatureLayer layer = new FeatureLayer(featureCollection, style);						
		map.addLayer(layer);
		
//		Layer gridLayer = createGridLayer(style, envelope);
//		map.addLayer(gridLayer);			
		
		// Save image
		saveMapJPEGImage(map, temporaryDirectory, mapFileName.toString(), featureCollection.size(), 800);
		exportSVG(map, temporaryDirectory, mapFileName.toString(), featureCollection.size(), 800);	

		map.dispose();
	}


	/* The following “classifier” functions are available:

		EqualInterval - classifier where each group represents the same sized range
		Jenks - generate the Jenks’ Natural Breaks classification
		Quantile - classifier with an even number of items in each group
		StandardDeviation - generated using the standard deviation method
		UniqueInterval - variation of EqualInterval that takes into account unique values
 */
	private Style createRifStyle(
			final String classifyFunctionName,
			final String columnName,
			final String paletteName,
			final int numberOfBreaks,
			final DefaultFeatureCollection featureCollection) {
				
		StyleBuilder builder = new StyleBuilder();
		
		ColorBrewer brewer = ColorBrewer.instance();
		Color[] colors = brewer.getPalette(paletteName).getColors(numberOfBreaks);
	
		FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();
		PropertyName propteryExpression = filterFactory.property(columnName);
		Function classify = filterFactory.function("Quantile", 
			propteryExpression, filterFactory.literal(numberOfBreaks));
		Classifier groups = (Classifier) classify.evaluate(featureCollection);    

		FeatureTypeStyle featureTypeStyle = StyleGenerator.createFeatureTypeStyle(
            groups,
            propteryExpression,
            colors,
            "Generated FeatureTypeStyle for GreeBlue",
            featureCollection.getSchema().getGeometryDescriptor(),
            StyleGenerator.ELSEMODE_IGNORE,
            0.95,
            null);
        Style style = builder.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);

        return style;		
	}
		
    /**
     * Create a Style to draw polygon features with a thin blue outline and
     * a cyan fill
     */
    private Style createSimplePolygonStyle() {
		StyleBuilder builder = new StyleBuilder();
		FilterFactory filterFactory = builder.getFilterFactory();
		
        // create a partially opaque outline stroke
        Stroke stroke = builder.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1),
                filterFactory.literal(0.5));

        // create a partial opaque fill
        Fill fill = builder.createFill(
                filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PolygonSymbolizer sym = builder.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = builder.createRule(sym);
        FeatureTypeStyle fts = builder.createFeatureTypeStyle("rifstyle1", rule);
        Style style = builder.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

	/**
 * Generate an SVG document from the supplied information. Note, use cavasSize first if you want
 * to change the default output size.
 * 
 * @param map
 *            Contains the layers (features + styles) to be rendered
 * @param env
 *            The portion of the map to generate an SVG from
 * @param out
 *            Stream to write the resulting SVG out to (probable should be a new file)
 * @param canvasSize
 *            optional canvas size, will default to 300x300
 * @throws IOException
 *             Should anything go wrong whilst writing to 'out'
 * @throws ParserConfigurationException
 *             If critical XML tools are missing from the classpath
 */
	public void exportSVG(
		final MapContent map, 
		final File temporaryDirectory,
		final String outputFileName, 
		final int numberOfAreas,
		final int imageWidth) throws Exception {
			
		String mapDirName=temporaryDirectory.getAbsolutePath() + File.separator + MAPS_SUBDIRECTORY;
		File mapDirectory = new File(mapDirName);
		File newDirectory = new File(mapDirName);
		if (newDirectory.exists()) {
			rifLogger.debug(this.getClass(), 
				"Found directory: " + newDirectory.getAbsolutePath());
		}
		else {
			newDirectory.mkdirs();
			rifLogger.info(this.getClass(), 
				"Created directory: " + newDirectory.getAbsolutePath());
		}
		
		String mapFile=mapDirName + File.separator + outputFileName + ".svg";
		File file = new File(mapFile);
		if (file.exists()) {
			file.delete();
		}

		Rectangle imageBounds = null;
		ReferencedEnvelope mapBounds = null;
		int imageHeight=0;
		try {
			mapBounds = map.getViewport().getBounds();
			double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
			imageHeight=(int) Math.round(imageWidth * heightToWidth);
			imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);

		} catch (Exception e) {
			// failed to access map layers
			throw new RuntimeException(e);
		}
		
    Dimension canvasSize = new Dimension(imageWidth, imageHeight);
    Document document = null;
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    
    // Create an instance of org.w3c.dom.Document
    document = db.getDOMImplementation().createDocument(null, "svg", null);
    
    // Set up the map
    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
    ctx.setComment("Generated by GeoTools2 with Batik SVG Generator");
    
    SVGGraphics2D g2d = new SVGGraphics2D(ctx, true);
    
    g2d.setSVGCanvasSize(canvasSize);
    
    StreamingRenderer renderer = new StreamingRenderer();
    renderer.setMapContent(map);
    
    Rectangle outputArea = new Rectangle(g2d.getSVGCanvasSize());
    ReferencedEnvelope dataArea = map.getMaxBounds();
    
    renderer.paint(g2d, outputArea, dataArea);
	OutputStream outputStream = new FileOutputStream(mapFile);
    OutputStreamWriter osw = null;
    try {
        osw = new OutputStreamWriter(outputStream, "UTF-8");
        g2d.stream(osw);
    } finally {
        if (osw != null)
            osw.close();
    }
    
	rifLogger.info(this.getClass(), "Create map " + imageWidth + "x" + imageHeight + 
			"; areas: " + numberOfAreas + "; file: " + mapFile);
}

	// See: http://docs.geotools.org/latest/userguide/library/render/gtrenderer.html#image
	// Also SVG example
	public void saveMapJPEGImage(
		final MapContent map, 
		final File temporaryDirectory,
		final String outputFileName, 
		final int numberOfAreas,
		final int imageWidth)   
			throws Exception {

		String mapDirName=temporaryDirectory.getAbsolutePath() + File.separator + MAPS_SUBDIRECTORY;
		File mapDirectory = new File(mapDirName);
		File newDirectory = new File(mapDirName);
		if (newDirectory.exists()) {
			rifLogger.debug(this.getClass(), 
				"Found directory: " + newDirectory.getAbsolutePath());
		}
		else {
			newDirectory.mkdirs();
			rifLogger.info(this.getClass(), 
				"Created directory: " + newDirectory.getAbsolutePath());
		}
		
		String mapFile=mapDirName + File.separator + outputFileName + ".jpg";
		File file = new File(mapFile);
		if (file.exists()) {
			file.delete();
		}
		
		GTRenderer renderer = new StreamingRenderer();
		renderer.setMapContent(map);

		Rectangle imageBounds = null;
		ReferencedEnvelope mapBounds = null;
		int imageHeight=0;
		try {
			mapBounds = map.getViewport().getBounds();
			double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
			imageHeight=(int) Math.round(imageWidth * heightToWidth);
			imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);

		} catch (Exception e) {
			// failed to access map layers
			throw new RuntimeException(e);
		}

		BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, 
			BufferedImage.TYPE_INT_RGB);

		Graphics2D gr = image.createGraphics();
		gr.setPaint(Color.WHITE);
		gr.fill(imageBounds);

		try {
			renderer.paint(gr, imageBounds, mapBounds);
			File fileToSave = new File(mapFile);
			ImageIO.write(image, "jpeg", fileToSave);

		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		rifLogger.info(this.getClass(), "Create map " + imageWidth + "x" + imageHeight + 
			"; areas: " + numberOfAreas + "; file: " + mapFile);
	}

	// https://github.com/ianturton/geotools-cookbook/blob/master/modules/output/src/main/java/org/ianturton/cookbook/output/MapWithGrid.java
	private Layer createGridLayer(Style style, ReferencedEnvelope gridBounds)
			throws IOException {
		double squareWidth = 20.0;
		double extent = gridBounds.maxExtent();
		double ll = Math.log10(extent);
		if (ll > 0) {
			// there are ll 10's across the map
			while (ll-- > 4) {
				squareWidth *= 10;
			}
		}

		// max distance between vertices
		double vertexSpacing = squareWidth / 20;
		// grow to cover the whole map (and a bit).
		double left = gridBounds.getMinX();
		double bottom = gridBounds.getMinY();

		if (left % squareWidth != 0) {
			if (left > 0.0) { // east
				left -= Math.abs(left % squareWidth);
			} else { // west
				left += Math.abs(left % squareWidth);
			}
		}

		if (bottom % squareWidth != 0) {
			if (bottom > 0.0) {
				bottom -= Math.abs(bottom % squareWidth);
			} else {
				bottom += Math.abs(bottom % squareWidth);
			}
		}

		gridBounds.expandToInclude(left, bottom);
		double right = gridBounds.getMaxX();
		double top = gridBounds.getMaxY();
		if (right % squareWidth != 0) {
			if (right > 0.0) { // east
				right += Math.abs(right % squareWidth) + squareWidth;
			} else { // west
				right -= Math.abs(right % squareWidth) - squareWidth;
			}
		}

		if (top % squareWidth != 0) {
			if (top > 0.0) { // North
				top += Math.abs(top % squareWidth) + squareWidth;
			} else { // South
				top -= Math.abs(top % squareWidth) - squareWidth;
			}
		}

		gridBounds.expandToInclude(right, top);
		SimpleFeatureSource grid = Grids.createSquareGrid(gridBounds, squareWidth,
				vertexSpacing);
		Layer gridLayer = new FeatureLayer(grid.getFeatures(), style);
		return gridLayer;
	}
}