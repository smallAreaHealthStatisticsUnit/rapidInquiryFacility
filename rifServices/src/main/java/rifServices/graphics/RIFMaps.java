package rifServices.graphics;

import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifServices.businessConceptLayer.RIFStudySubmission;

import rifServices.graphics.RIFGraphics;
import rifServices.graphics.RIFGraphicsOutputType;

import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;
import rifServices.dataStorageLayer.common.SQLAbstractSQLManager;

import rifServices.dataStorageLayer.common.RifFeatureCollection;
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
import java.util.Set;
import java.util.EnumSet;
import java.util.Iterator;

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
import org.geotools.filter.function.RangedClassifier;

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
	private static RifCoordinateReferenceSystem rifCoordinateReferenceSystem = 
		new RifCoordinateReferenceSystem();

	private static final String MAPS_SUBDIRECTORY = "maps";
	
	private int mapWidthPixels=0;
	
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
			mapWidthPixels=this.rifServiceStartupOptions.getOptionalRIfServiceProperty(
					"mapWidthPixels", 7480);
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
			final RifFeatureCollection featureCollection,
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

		writeMap(
			featureCollection.getFeatureCollection(),
			temporaryDirectory,
			studyID,
			"Smoothed SMR map"		/* mapTitle */, 
			"smoothed_smr"			/* resultsColumn */,
			createRifStyle(
				"quantile"		/* Classifier function name */, 
				"sm_smr"		/* Column */, 
				"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				9				/* numberOfBreaks */, 
				true			/* invert */,
				featureCollection.getFeatureCollection()),
			baseStudyName,
			featureCollection.getCoordinateReferenceSystem()
									/* CoordinateReferenceSystem */,
			featureCollection.getReferencedEnvelope());

		writeMap(
			featureCollection.getFeatureCollection(),
			temporaryDirectory,
			studyID,
			"Poster Probability map"		/* mapTitle */, 
			"posterior_probability"			/* resultsColumn */,
			createRifStyle(
				"AtlasProbability"	/* Classifier function name */, 
				"post_prob"			/* Column */, 
				null				/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				0					/* numberOfBreaks */, 
				false				/* invert */,
				featureCollection.getFeatureCollection()),
			baseStudyName,
			featureCollection.getCoordinateReferenceSystem()
									/* CoordinateReferenceSystem */,
			featureCollection.getReferencedEnvelope());			

	}
	
	private void writeMap(
		final DefaultFeatureCollection featureCollection,
		final File temporaryDirectory,
		final String studyID,
		final String mapTitle,
		final String resultsColumn,
		final Style style,
		final String baseStudyName,
		final CoordinateReferenceSystem crs,
		final ReferencedEnvelope envelope) 
			throws Exception {
				
		//Create map
		String filePrefix=resultsColumn + "_";
		String dirName=MAPS_SUBDIRECTORY;
		MapContent map = new MapContent();
		map.setTitle(mapTitle);

		ReferencedEnvelope envelope2=featureCollection.getBounds();
		rifLogger.info(this.getClass(), "database bounds: " + envelope.toString() +
			"; featureCollection bounds: " + envelope2.toString());
		// Set projection
		MapViewport vp = map.getViewport();
		vp.setCoordinateReferenceSystem(crs);
		vp.setBounds(envelope2);	// Use featureCollection until fixed
			
		// Add layers to map			
        FeatureLayer layer = new FeatureLayer(featureCollection, style);						
		map.addLayer(layer);
		
//		Layer gridLayer = createGridLayer(style, envelope2);
//		map.addLayer(gridLayer);			
		
		// Save image
		exportSVG(map, temporaryDirectory, dirName, filePrefix, studyID, featureCollection.size(), mapWidthPixels);	
		createGraphicsMaps(temporaryDirectory, dirName, filePrefix, studyID);
		
		map.dispose();
	}

	/**
	 * Generate an SVG document from the map. 
	 * 
	 * SVG file name:  <filePrefix><studyID>.svg	  
	 *
	 * @param MapContent map - Contains the layers (features + styles) to be rendered,
	 * @param File temporaryDirectory,
	 * @param String dirName, 
	 * @param String filePrefix, 
	 * @param String studyID, 
	 * @param int numberOfAreas,
	 * @param int imageWidth
	 */
	public void exportSVG(
		final MapContent map, 
		final File temporaryDirectory,
		final String dirName, 
		final String filePrefix, 
		final String studyID,
		final int numberOfAreas,
		final int imageWidth) throws Exception {
			
		CoordinateReferenceSystem crs=map.getCoordinateReferenceSystem();
		String mapDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName;
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
		
		String svgFile=mapDirName + File.separator + filePrefix + studyID + ".svg";
		File file = new File(svgFile);
		if (file.exists()) {
			file.delete();
		}

		Rectangle imageBounds = null;
		ReferencedEnvelope mapBounds = null;
		int imageHeight=0;

		mapBounds = map.getViewport().getBounds();
		double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
		imageHeight=(int) Math.round(imageWidth * heightToWidth);
		imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);
	
		MapViewport vp = map.getViewport();
		ReferencedEnvelope envelope=vp.getBounds();	
		rifLogger.info(this.getClass(), "Create map " + imageWidth + "x" + imageHeight + 
			"; areas: " + numberOfAreas + "; file: " + svgFile + lineSeparator +
			"bounding box: " + envelope.toString() + "; CRS: " + CRS.toSRS(crs));	
			
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
		OutputStream outputStream = new FileOutputStream(svgFile);
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(outputStream, "UTF-8");
			g2d.stream(osw);
		} 
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "IOException writing file: " + svgFile, exception);
			throw exception;
		}
		finally {
			if (osw != null) {
				osw.close();
			}
		}			
	}

	/** Build Graphics file from SVG source
	  *
	  * Graphics file name: <filePrefix><studyID>_<printingDPI>dpi_<year>.<outputType.getGraphicsExtentsion()>
      *
	  * SVG file name:  <filePrefix><studyID>.svg	  
	  *
	  * @param: File temporaryDirectory,
	  * @param: String dirName,
	  * @param: String filePrefix,
	  * @param: String studyID,
	  * @param: RIFGraphicsOutputType outputType
	  */
	private void createGraphicsMaps(
		final File temporaryDirectory,
		final String dirName,
		final String filePrefix,
		final String studyID) 
			throws Exception {
						
		RIFGraphics rifGraphics = new RIFGraphics(rifServiceStartupOptions);
		
		Set<RIFGraphicsOutputType> allOutputTypes = EnumSet.of(
			RIFGraphicsOutputType.RIFGRAPHICS_JPEG,
			RIFGraphicsOutputType.RIFGRAPHICS_PNG,
			RIFGraphicsOutputType.RIFGRAPHICS_TIFF,    // Requires 1.9.2 or higher Batik
			RIFGraphicsOutputType.RIFGRAPHICS_EPS,
			RIFGraphicsOutputType.RIFGRAPHICS_PS);
		Iterator <RIFGraphicsOutputType> allOutputTypeIter = allOutputTypes.iterator();
		while (allOutputTypeIter.hasNext()) {
			RIFGraphicsOutputType outputType=allOutputTypeIter.next();
			if (outputType.isRIFGraphicsOutputTypeEnabled()) {	
				if (outputType.doesRIFGraphicsOutputTypeUseFop()) {	
					rifGraphics.addGraphicsFile(
						temporaryDirectory,							/* Study scratch space diretory */
						dirName, 									/* directory */
						filePrefix, 								/* File prefix */
						studyID,
						outputType,
						mapWidthPixels);
				}
				else {
					rifGraphics.addGraphicsFile(
						temporaryDirectory,							/* Study scratch space diretory */
						dirName, 									/* directory */
						filePrefix, 								/* File prefix */
						studyID,
						outputType,
						mapWidthPixels);
				}
			}
		}		
	}

	/**
	 * Invert color array
	 *
	 * @param Color[] colors
	 *
	 * @returns Color[]
	 */
	private Color[] invertColor(Color[] colors) {
		for(int i=0; i<colors.length/2; i++){
			Color temp = colors[i];
			colors[i] = colors[colors.length -i -1];
			colors[colors.length -i -1] = temp;
		}

		return colors;		
	}

	/** Create style for RIF choropleth maps. Uses same colorbrewer and classification schemes as the front end
	  *
	  * See:
	  *
	  * http://docs.geotools.org/latest/userguide/extension/brewer/colorbrewer.html
	  * http://docs.geotools.org/latest/userguide/extension/brewer/classifier.html
	  *
	  * The following “classifier” functions are available [Front end names in brackets]:
	  * 
	  * -	EqualInterval - classifier where each group represents the same sized range [quantize]
	  * -	Jenks - generate the Jenks’ Natural Breaks classification [jenks]
	  * -	Quantile - classifier with an even number of items in each group [quantile]
  	  * -	StandardDeviation - generated using the standard deviation method [standardDeviation]
  	  * 		
	  * The RIF does NOT use:
	  * -	UniqueInterval - variation of EqualInterval that takes into account unique values	 
	  *	  
	  * Two other scales are provided: 
	  * - AtlasRelativeRisk: fixed scale at: [0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51] between +/-infinity; PuOr
	  * - AtlasProbability: fixed scale at: [0.20, 0.81] between 0 and 1; RdYlGn
	  *
	  * See: rifs-util-choro.js
	  *
	  * @param String classifyFunctionName,
	  * @param String columnName,
	  * @param String paletteName,
	  * @param int numberOfBreaks,
	  * @param boolean invert,
	  * @param DefaultFeatureCollection featureCollection
	  *
	  * @return Style 
	  */													
	private Style createRifStyle(
			final String rifMethod,
			final String columnName,
			final String lpaletteName,
			final int lnumberOfBreaks,
			final boolean linvert,
			final DefaultFeatureCollection featureCollection) 
				throws Exception {
		Classifier groups=null;
		
		String paletteName=lpaletteName;
		int numberOfBreaks=lnumberOfBreaks;
		boolean invert=linvert;
		String classifyFunctionName=null;	
		if (rifMethod.equals("quantize")) {
			classifyFunctionName="EqualInterval";
		}
		else if (rifMethod.equals("jenks")) {
			classifyFunctionName="Jenks";
		}
		else if (rifMethod.equals("quantile")) {
			classifyFunctionName="Quantile";
		}
		else if (rifMethod.equals("standardDeviation")) {
			classifyFunctionName="standardDeviation";
		}
		else if (rifMethod.equals("AtlasRelativeRisk")) { // fixed scale at: [0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51] between +/-infinity
			paletteName="PuOr";
			numberOfBreaks=8;
			Comparable min[] = new Comparable[]{Double.NEGATIVE_INFINITY, 0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35};
			Comparable max[] = new Comparable[]{0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, Double.POSITIVE_INFINITY};
			groups = new RangedClassifier(min, max);
			invert=true;
		}
		else if (rifMethod.equals("AtlasProbability")) { // fixed scale at: [0.20, 0.81] between 0 and 1
			paletteName="RdYlGn";
			numberOfBreaks=3;
			Comparable min[] = new Comparable[]{0.0, 0.20, 0.81};
			Comparable max[] = new Comparable[]{0.20, 0.81, 1.0};
			groups = new RangedClassifier(min, max);
		}		
		else {
			throw new Exception("Invalid RIF mapping method: " + rifMethod);
		}

		if (classifyFunctionName != null && !(classifyFunctionName.equals("EqualInterval") || 
		      classifyFunctionName.equals("Jenks") ||
			  classifyFunctionName.equals("Quantile") || 
			  classifyFunctionName.equals("standardDeviation"))) {
			throw new Exception("Unsupport classify Function Name: " + classifyFunctionName);
		}
		StyleBuilder builder = new StyleBuilder();
		
		ColorBrewer brewer = ColorBrewer.instance();
		Color[] colors = brewer.getPalette(paletteName).getColors(numberOfBreaks);
		if (invert) {
			colors=invertColor(colors);
		}
		FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();
		PropertyName propteryExpression = filterFactory.property(columnName);
		if (groups == null) {
			Function classify = filterFactory.function(classifyFunctionName, 
				propteryExpression, filterFactory.literal(numberOfBreaks));
			groups = (Classifier) classify.evaluate(featureCollection);  // Classify data 
		} 

		FeatureTypeStyle featureTypeStyle = StyleGenerator.createFeatureTypeStyle(
            groups,
            propteryExpression,
            colors,
            "Generated FeatureTypeStyle for " + paletteName		  /* Type ID */,
            featureCollection.getSchema().getGeometryDescriptor() /* GeometryDescriptor  */,
            StyleGenerator.ELSEMODE_IGNORE,
            0.95	/* opacity */,
            null 	/* defaultStroke */);
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