package rifServices.graphics;

import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifServices.businessConceptLayer.RIFStudySubmission;

import rifServices.graphics.RIFGraphics;
import rifServices.graphics.RIFGraphicsOutputType;
import rifServices.graphics.LegendLayer;

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
import java.util.ArrayList;
import java.util.List;

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

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.JTS;

import org.geotools.grid.Grids;

import org.geotools.data.simple.SimpleFeatureSource;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.CRS;

import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.StreamingRenderer;

import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;

import org.geotools.styling.Style;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;

import org.geotools.factory.CommonFactoryFinder;

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
	protected int errors=0;
	protected int features=0;
	
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

		RIFStyle rifSyle=new RIFStyle(
				"quantile"		/* Classifier function name */, 
				"sm_smr"		/* Column */, 
				"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				9				/* numberOfBreaks */, 
				true			/* invert */,
				featureCollection.getFeatureCollection());
		writeMap(
			featureCollection.getFeatureCollection(),
			temporaryDirectory,
			studyID,
			"Smoothed SMR"		/* mapTitle */, 
			"smoothed_smr"			/* resultsColumn */,
			rifSyle,
			baseStudyName,
			featureCollection.getCoordinateReferenceSystem()
									/* CoordinateReferenceSystem */,
			featureCollection.getReferencedEnvelope());

		rifSyle=new RIFStyle(
				"AtlasProbability"	/* Classifier function name */, 
				"post_prob"			/* Column */, 
				null				/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				0					/* numberOfBreaks */, 
				false				/* invert */,
				featureCollection.getFeatureCollection());
		writeMap(
			featureCollection.getFeatureCollection(),
			temporaryDirectory,
			studyID,
			"Posterior Probability"		/* mapTitle */, 
			"posterior_probability"		/* resultsColumn */,
			rifSyle,
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
		final RIFStyle rifSyle,
		final String baseStudyName,
		final CoordinateReferenceSystem crs,
		final ReferencedEnvelope envelope) 
			throws Exception {
				
		Style style=rifSyle.getStyle();
		
		//Create map
		String filePrefix=resultsColumn + "_";
		String dirName=MAPS_SUBDIRECTORY;
		MapContent map = new MapContent();
		map.setTitle(mapTitle);

//		ReferencedEnvelope envelope2=featureCollection.getBounds();
//		rifLogger.info(this.getClass(), mapTitle + "; database bounds: " + envelope.toString() + lineSeparator + 
//			"featureCollection bounds: " + envelope2.toString());
		// Set projection
		MapViewport vp = map.getViewport();
		vp.setCoordinateReferenceSystem(crs);
		vp.setBounds(envelope);	// Use featureCollection until fixed
			
		// Add layers to map			
        FeatureLayer layer = new FeatureLayer(featureCollection, style);	
		layer.setTitle(mapTitle);		
		if (!map.addLayer(layer)) {
			throw new Exception("Failed to add FeatureLayer to map: " + mapTitle);
		}
		
//		Layer gridLayer = createGridLayer(style, envelope); 		
//		if (!map.addLayer(gridLayer)) {
//			throw new Exception("Failed to add gridLayer to map: " + mapTitle);
//		}
		
		LegendLayer legendLayer = createLegendLayer(rifSyle, envelope, mapTitle); 
		if (!map.addLayer(legendLayer)) {
			throw new Exception("Failed to add legendLayer to map: " + mapTitle);
		}
		
		// Save image
		exportSVG(map, temporaryDirectory, dirName, filePrefix, studyID, featureCollection.size(), 
			(int)(mapWidthPixels/8));	
		createGraphicsMaps(temporaryDirectory, dirName, filePrefix, studyID);
		
		map.dispose();
	}

	private LegendLayer createLegendLayer(
		final RIFStyle rifSyle, 
		final ReferencedEnvelope envelope2,
		final String mapTitle)
				throws Exception {
			
		ArrayList<LegendLayer.LegendItem> legendItems = new ArrayList<LegendLayer.LegendItem>();
		ArrayList<RIFStyle.RIFStyleBand> rifStyleBands = rifSyle.getRifStyleBands();
		StringBuffer sb = new StringBuffer();
		int i=0;
		for (RIFStyle.RIFStyleBand rifStyleBand : rifStyleBands) {
			i++;
			String title=rifStyleBand.getTitle();
			sb.append("Legend item [" + i + "] " + title + "; " + rifStyleBand.getColor() + lineSeparator);
			LegendLayer.LegendItem legendItem = new LegendLayer.LegendItem(
				title, 
				rifStyleBand.getColor(),
				Geometries.MULTIPOLYGON);
			legendItems.add(legendItem);
		}
		rifLogger.info(this.getClass(), sb.toString());
		
		LegendLayer legendLayer = new LegendLayer(mapTitle, Color.LIGHT_GRAY, legendItems);
		legendLayer.setTitle("Legend");
		
		return legendLayer;
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

		int imageHeight=0;
		ReferencedEnvelope mapBounds = map.getViewport().getBounds();
		double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
		imageHeight=(int) Math.round(imageWidth * heightToWidth);
		Rectangle imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);
		int screenWidth=(int)(imageWidth*1.2);
		Rectangle screenBounds = new Rectangle(0, 0, screenWidth, imageHeight);
		
		List<Layer>	mapLayers = map.layers();
		StringBuffer sb = new StringBuffer();
		int i=0;
		for (Layer mapLayer : mapLayers) {
			i++;
			ReferencedEnvelope envel=mapLayer.getBounds();
			if (envel != null) {
				sb.append("Layer[" + i + "]: " + mapLayer.getTitle() + 
					"; bounds: " + envel.toString() + lineSeparator);
			}
			else  {
				sb.append("Layer[" + i + "]: " + mapLayer.getTitle() + 
					"; bounds: NONE" + lineSeparator);
			}
		}
		
		MapViewport vp = map.getViewport();
		vp.setScreenArea(screenBounds);
		map.setViewport(vp);
		ReferencedEnvelope envelope=vp.getBounds();	
		Rectangle screenArea=vp.getScreenArea();	
		rifLogger.info(this.getClass(), "Create map " + imageWidth + "x" + imageHeight + 
			"; areas: " + numberOfAreas + "; file: " + svgFile + lineSeparator +
			"bounding box: " + envelope.toString() + "; CRS: " + CRS.toSRS(crs) + lineSeparator +
			"image bounds: " + imageBounds.toString() + lineSeparator +
			"screenArea: " + screenArea.toString() + lineSeparator +
			sb.toString());	
			
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
		renderer.addRenderListener(new RenderListener() {
			public void featureRenderer(SimpleFeature feature) {
				features++;
			}
			public void errorOccurred(Exception exception) {			
				rifLogger.warning(this.getClass(), "Renderer error: " + exception.getMessage());
				errors++;
			}
		});
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
			if (errors > 0) {
				rifLogger.warning(this.getClass(), errors + " occurred rendering " + features + " features");
			}
			else {
				rifLogger.warning(this.getClass(),  "No errors occurred rendering " + features + " features");
			}
			errors=0;
			features=0;
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