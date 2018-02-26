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

import org.geotools.styling.SLD;

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
	private static RifCoordinateReferenceSystem rifCoordinateReferenceSystem = null;

	private static final String MAPS_SUBDIRECTORY = "maps";
	
	private int mapWidthPixels=0;
	private int printingDPI=0;
	
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
		
		this.rifCoordinateReferenceSystem = new RifCoordinateReferenceSystem();
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		try {
			mapWidthPixels=this.rifServiceStartupOptions.getOptionalRIfServiceProperty(
					"mapWidthPixels", 7480);		
			printingDPI=this.rifServiceStartupOptions.getOptionalRIfServiceProperty("printingDPI", 1000);		
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

		RIFStyle rifSyle1=new RIFStyle(
				"quantile"		/* Classifier function name */, 
				"rr"			/* Column */, 
				"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				9				/* numberOfBreaks */, 
				true			/* invert */,
				featureCollection.getFeatureCollection());
		writeMap(
			featureCollection,
			temporaryDirectory,
			studyID,
			"Relative Risk"		/* mapTitle */, 
			"relative_risk"		/* resultsColumn */,
			rifSyle1,
			baseStudyName);
			
		RIFStyle rifSyle2=new RIFStyle(
				"quantile"		/* Classifier function name */, 
				"sm_smr"		/* Column */, 
				"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				9				/* numberOfBreaks */, 
				true			/* invert */,
				featureCollection.getFeatureCollection());
		writeMap(
			featureCollection,
			temporaryDirectory,
			studyID,
			"Smoothed SMR"		/* mapTitle */, 
			"smoothed_smr"		/* resultsColumn */,
			rifSyle2,
			baseStudyName);

		RIFStyle rifSyle3=new RIFStyle(
				"AtlasProbability"	/* Classifier function name */, 
				"post_prob"			/* Column */, 
				null				/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				0					/* numberOfBreaks */, 
				false				/* invert */,
				featureCollection.getFeatureCollection());
		writeMap(
			featureCollection,
			temporaryDirectory,
			studyID,
			"Posterior Probability"		/* mapTitle */, 
			"posterior_probability"		/* resultsColumn */,
			rifSyle3,
			baseStudyName);			

	}
	
	private void writeMap(
		final RifFeatureCollection rifFeatureCollection,
		final File temporaryDirectory,
		final String studyID,
		final String mapTitle,
		final String resultsColumn,
		final RIFStyle rifStyle,
		final String baseStudyName) 
			throws Exception {
	
		DefaultFeatureCollection featureCollection=rifFeatureCollection.getFeatureCollection();
		DefaultFeatureCollection backgroundAreasFeatureCollection=
			rifFeatureCollection.getBackgroundAreasFeatureCollection();
		CoordinateReferenceSystem crs=rifFeatureCollection.getCoordinateReferenceSystem();
		ReferencedEnvelope expandedEnvelope=rifFeatureCollection.getExpandedEnvelope();
		ReferencedEnvelope initialEnvelope=rifFeatureCollection.getInitialEnvelope();
		double gridSquareWidth=rifFeatureCollection.getGridSquareWidth();
		double gridVertexSpacing=rifFeatureCollection.getGridVertexSpacing();
		String gridScale=rifFeatureCollection.getGridScale();

		String filePrefix=resultsColumn + "_";
		String dirName=MAPS_SUBDIRECTORY;
		
		// Create SLD styling file for GIS tools
		Style style=rifStyle.getStyle();
		rifStyle.writeSldFile(resultsColumn, studyID, temporaryDirectory, dirName, mapTitle);

		//Create map		
		MapContent map = new MapContent();
		map.setTitle(mapTitle);

		// Set projection
		MapViewport vp = map.getViewport();
		vp.setCoordinateReferenceSystem(crs);
		vp.setBounds(expandedEnvelope);	
	
		// Deduce aspect ratio (the ratio of the width to the height of an image or screen)
		int imageWidth=(int)(mapWidthPixels/8);
		int imageHeight=0;
		double heightToWidth = expandedEnvelope.getSpan(1) / expandedEnvelope.getSpan(0); // Inverse aspect ratio
		imageHeight=(int) Math.round(imageWidth * heightToWidth); 
		Rectangle screenBounds = new Rectangle(0, 0, imageWidth, imageHeight);
		// Set new right enlarged screen area and map bounds
		
		vp.setScreenArea(screenBounds);
		vp.setMatchingAspectRatio(false);
		vp.setEditable(false); /* Lock viewport to enforce bounds */
		map.setViewport(vp);
		
		Rectangle screenArea=vp.getScreenArea();
		ReferencedEnvelope mapArea=vp.getBounds();	
		ReferencedEnvelope mapMaxBounds=map.getMaxBounds();
		rifLogger.info(this.getClass(), 
			"Before layer add: Bounding box: " + expandedEnvelope.toString() + "; CRS: " + CRS.toSRS(crs) + lineSeparator +
			"screen bonds: " + screenBounds.toString() + lineSeparator +
			"aspect ratio: " + (double)(1/heightToWidth) + lineSeparator +
			"map box: " + mapArea.toString() + "; CRS: " + CRS.toSRS(mapArea.getCoordinateReferenceSystem()) + lineSeparator +
			"map max bounds: " + mapMaxBounds.toString() + "; CRS: " + CRS.toSRS(mapMaxBounds.getCoordinateReferenceSystem()) + lineSeparator +
			"screenArea: " + screenArea.toString());	
				
		// Add layers to map
		
		rifLogger.info(this.getClass(), "Add grid; gridSquareWidth: " + gridSquareWidth + 
			"; gridVertexSpacing: " + gridVertexSpacing);	
		SimpleFeatureSource grid = Grids.createSquareGrid(expandedEnvelope, gridSquareWidth,
			gridVertexSpacing);				
		Layer gridLayer = new FeatureLayer(grid.getFeatures(), SLD.createLineStyle(Color.LIGHT_GRAY, 1));
		gridLayer.setTitle("Grid");			
		if (!map.addLayer(gridLayer)) {
			throw new Exception("Failed to add gridLayer to map: " + mapTitle);
		}
	
		if (backgroundAreasFeatureCollection != null) {
			Layer backgroundAreasLayer = new FeatureLayer(backgroundAreasFeatureCollection, 
				SLD.createPolygonStyle(
					Color.LIGHT_GRAY /* outlineColor */,
					Color.decode("#ececec") /* fillColor */,
					1	/* opacity */,
					null /* labelField */,
					null /* labelFont */));
			backgroundAreasLayer.setTitle("Background areas");			
			if (!map.addLayer(backgroundAreasLayer)) {
				throw new Exception("Failed to add backgroundAreasLayer to map: " + mapTitle);
			}
		}
		
        FeatureLayer layer = new FeatureLayer(featureCollection, style);	
		layer.setTitle(mapTitle);		
		if (!map.addLayer(layer)) {
			throw new Exception("Failed to add FeatureLayer to map: " + mapTitle);
		}		
		
		LegendLayer legendLayer = createLegendLayer(rifStyle, expandedEnvelope, mapTitle, gridScale); 
		if (!map.addLayer(legendLayer)) {
			throw new Exception("Failed to add legendLayer to map: " + mapTitle);
		}

		screenArea=vp.getScreenArea();
		mapArea=vp.getBounds();	
		mapMaxBounds=map.getMaxBounds();
		rifLogger.info(this.getClass(), 
			"After layer add: map box: " + mapArea.toString() + "; CRS: " + CRS.toSRS(mapArea.getCoordinateReferenceSystem()) + lineSeparator +
			"map max bounds: " + mapMaxBounds.toString() + "; CRS: " + CRS.toSRS(mapMaxBounds.getCoordinateReferenceSystem()) + lineSeparator +
			"screenArea: " + screenArea.toString());	
			
		// Save image
		exportSVG(map, temporaryDirectory, dirName, filePrefix, studyID, featureCollection.size(), 
			imageWidth, imageHeight);	
		createGraphicsMaps(temporaryDirectory, dirName, filePrefix, studyID);
		
		map.dispose();
	}

	/**
	 * Create map legend [layer]
	 *
	 * @param RIFStyle rifSyle, 
	 * @param ReferencedEnvelope envelope,
	 * @param String mapTitle,
	 * @param String gridScale
	 *
	 * @returns LegendLayer
	 */
	private LegendLayer createLegendLayer(
		final RIFStyle rifSyle, 
		final ReferencedEnvelope envelope,
		final String mapTitle,
		final String gridScale)
				throws Exception {
			
		ArrayList<LegendLayer.LegendItem> legendItems = new ArrayList<LegendLayer.LegendItem>();
		ArrayList<RIFStyle.RIFStyleBand> rifStyleBands = rifSyle.getRifStyleBands();
		StringBuffer sb = new StringBuffer();
		int i=0;
		for (RIFStyle.RIFStyleBand rifStyleBand : rifStyleBands) {
			i++;
			String title=rifStyleBand.getTitle().replace("..", " to ");
			sb.append("Legend item [" + i + "] " + title + "; " + rifStyleBand.getColor() + lineSeparator);
			LegendLayer.LegendItem legendItem = new LegendLayer.LegendItem(
				title, 
				rifStyleBand.getColor(),
				Geometries.MULTIPOLYGON);
			legendItems.add(legendItem);
		}
		rifLogger.info(this.getClass(), sb.toString());
		
		LegendLayer.LegendItem spacerLegendItem = new LegendLayer.LegendItem( // spacer
			"", 
			null,
			Geometries.MULTIPOLYGON);
		legendItems.add(spacerLegendItem);
/*		LegendLayer.LegendItem noDataLegendItem = new LegendLayer.LegendItem( // -1 handler: nulls now not categorised
			"No data", 
			Color.decode("#808080"), // default fill is *probably* 50% gray
			Geometries.MULTIPOLYGON); 
		legendItems.add(noDataLegendItem); */
		String crsName=""+envelope.getCoordinateReferenceSystem().getName();
		crsName=crsName.replace("EPSG:", "");
		if (crsName.length() < 12) {
			crsName="Projection: " + crsName;
		}	
		LegendLayer.LegendItem gridNameLegendItem = new LegendLayer.LegendItem(
			crsName, 
			null,
			Geometries.MULTIPOLYGON);
		legendItems.add(gridNameLegendItem);
		LegendLayer.LegendItem gridScaleLegendItem = new LegendLayer.LegendItem(
			"Grids: " + gridScale, 
			null,
			Geometries.MULTIPOLYGON);
		legendItems.add(gridScaleLegendItem);	
			
		LegendLayer legendLayer = new LegendLayer(mapTitle, Color.LIGHT_GRAY, legendItems);
		legendLayer.setTitle("Legend");
		
		return legendLayer;
	} 
	
	/**
	 * Generate an SVG document from the map. 
	 * 
	 * SVG file name:  <filePrefix><studyID>_<printingDPI>dpi.svg	  
	 *
	 * @param MapContent map - Contains the layers (features + styles) to be rendered,
	 * @param File temporaryDirectory,
	 * @param String dirName, 
	 * @param String filePrefix, 
	 * @param String studyID, 
	 * @param int numberOfAreas,
	 * @param int imageWidth. This sets the overall scale factor for the map, and is adjusted dependent on the 
	 *						  size of the map,
	 * @param int imageHeight. Fixed by the aspect ratio
	 */
	public void exportSVG(
		final MapContent map, 
		final File temporaryDirectory,
		final String dirName, 
		final String filePrefix, 
		final String studyID,
		final int numberOfAreas,
		final int imageWidth,
		final int imageHeight) throws Exception {
			
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
		
		String svgFile=mapDirName + File.separator + filePrefix + studyID + "_" + printingDPI + "dpi.svg";
		File file = new File(svgFile);
		if (file.exists()) {
			file.delete();
		}
	
		rifLogger.info(this.getClass(), "Create map " + imageWidth + "x" + imageHeight + 
			"; areas: " + numberOfAreas + "; file: " + svgFile);
				
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
				rifLogger.info(this.getClass(),  "No errors occurred rendering " + features + " features");
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
}