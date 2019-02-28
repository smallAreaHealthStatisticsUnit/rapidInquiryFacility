package org.sahsu.rif.services.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataEncoder.TagSet;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.GridElement;
import org.geotools.grid.GridFeatureBuilder;
import org.geotools.grid.Grids;
import org.geotools.grid.PolygonElement;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Font;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactoryImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.datastorage.common.RifCoordinateReferenceSystem;
import org.sahsu.rif.services.datastorage.common.RifFeatureCollection;
import org.sahsu.rif.services.datastorage.common.RifLocale;
import org.sahsu.rif.services.datastorage.common.SQLManager;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.w3c.dom.Document;

import javax.sql.rowset.CachedRowSet;
import com.vividsolutions.jts.geom.Polygon;

import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;

public class RIFMaps {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private Class rifMapsClass=this.getClass();
	
	private RIFServiceStartupOptions rifServiceStartupOptions;
	private final SQLManager manager;
	
	private static DatabaseType databaseType;
	private static RifCoordinateReferenceSystem rifCoordinateReferenceSystem = null;

	private static final String MAPS_SUBDIRECTORY = "maps";
	
	private int mapWidthPixels=0;
	private int printingDPI=0;
	
	protected int errors=0;
	protected int features=0;
	
	private RIFMapsParameters rifMapsParameters = null;
	
	private String copyrightInfo = null;
	private String software = "Rapid Inquiry Facility V4.0";
	private boolean enableMapGrids = false;
	private Color gridBackgroundColor=Color.WHITE;
	private Color gridColor=Color.WHITE; 	// enableMapGrids = false
	private int gridWidth=3;				// Pixels
		
	private boolean enableCoordinateDisplay=false;
	private double coordinateDisplayFontSize=80.0;
	
	public RIFMaps(final RIFServiceStartupOptions rifServiceStartupOptions,
			final SQLManager manager, final CachedRowSet rif40Studies) {
		
		this.manager = manager;
		rifCoordinateReferenceSystem = new RifCoordinateReferenceSystem();
		this.rifServiceStartupOptions = rifServiceStartupOptions;
	
		try {			
			ImageIO.scanForPlugins();
			mapWidthPixels=this.rifServiceStartupOptions.getOptionalRIfServiceProperty(
					"mapWidthPixels", 7480);		
			printingDPI=this.rifServiceStartupOptions.getOptionalRIfServiceProperty("printingDPI", 1000);
			copyrightInfo=this.rifServiceStartupOptions.getOptionalRIfServiceProperty("copyrightInfo", 
				(String)null);
			enableMapGrids=this.rifServiceStartupOptions.getOptionalRIfServiceProperty("enableMapGrids", true);
			enableCoordinateDisplay=this.rifServiceStartupOptions.getOptionalRIfServiceProperty(
				"enableCoordinateDisplay", false);

			if (enableMapGrids) {
				gridColor=Color.BLACK;
//				gridBackgroundColor=Color.decode("#fdfdfd"); // Very nearly white	
//				gridBackgroundColor=Color.RED;
				
				rifLogger.info(this.getClass(), "Grids enabled: " + gridColor.toString() + "; background: " +
					gridBackgroundColor.toString());
			}	
			else {	
				rifLogger.info(this.getClass(), "Grids disabled: " + gridColor.toString() + "; background: " +
					gridBackgroundColor.toString());
			}
			this.rifMapsParameters = new RIFMapsParameters(manager, rif40Studies);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFMaps() constructor");
			throw new NullPointerException();
		}
	}


	/** 
	 * Write Results Maps to MAPS_SUBDIRECTORY. Map data is also saved [elsewhere] as a shapefile with .SLD
	 * style layer descriptor files for each map. Maps are defined in the RIFMapsParameters object
	 *
	 * The column list for the map table is hard coded and reduced to 10 characters for DBF support
     * 
	 * @param DefaultFeatureCollection featureCollection,
	 * @param Connection connection, 
	 * @param File temporaryDirectory,
	 * @param String baseStudyName,
	 * @param String zoomLevel,
	 * @param RIFStudySubmission rifStudySubmission,
	 * @param CachedRowSet rif40Studies,
	 * @param CachedRowSet rif40Investigations,
	 * @param Locale locale
	 */		
	public void writeResultsMaps(
			final RifFeatureCollection featureCollection,
			final Connection connection,
			final File temporaryDirectory,
			final String baseStudyName,
			final String zoomLevel,
			final RIFStudySubmission rifStudySubmission,
			final CachedRowSet rif40Studies,
			final CachedRowSet rif40Investigations,
			final Locale locale)
					throws Exception {
	
			
		RifLocale rifLocale = new RifLocale(locale);
		Calendar calendar = rifLocale.getCalendar();			
		DateFormat df = rifLocale.getDateFormat();
		
		String studyID = rifStudySubmission.getStudyID();
		String mapTable=manager.getColumnFromResultSet(rif40Studies, "map_table");
		
		//Add geographies to zip file
		StringBuilder tileTableName = new StringBuilder();	
		tileTableName.append("geometry_");
		String geog = rifStudySubmission.getStudy().getGeography().getName();			
		tileTableName.append(geog);
		
		String studyDescription=manager.getColumnFromResultSet(rif40Studies, "description",
			true /* allowNulls */, false /* allowNoRows */);	
		if (studyDescription == null) {
			studyDescription=manager.getColumnFromResultSet(rif40Studies, "study_name",
				true /* allowNulls */, false /* allowNoRows */);
		} 
		
		// Available: inv_id, inv_name, inv_description, genders, numer_tab, year_start, year_stop, 
		// min_age_group, max_age_group
		int genders=Integer.parseInt(manager.getColumnFromResultSet(rif40Investigations, "genders"));
			// Usefully will currently blob if >1 row
		String invID=manager.getColumnFromResultSet(rif40Investigations, "inv_id");
		String invName=manager.getColumnFromResultSet(rif40Investigations, "inv_name");
		String invDescription=manager.getColumnFromResultSet(rif40Investigations, "inv_description");
		
		Set<Sex> allSexes = null;
		if (genders == Sex.MALES.getCode()) {
			allSexes=EnumSet.of(
				Sex.MALES);
		}
		else if (genders == Sex.FEMALES.getCode()) {
			allSexes=EnumSet.of(
				Sex.FEMALES);
		}
		else if (genders == Sex.BOTH.getCode()) {
			allSexes=EnumSet.of(
				Sex.MALES,   
				Sex.FEMALES,
				Sex.BOTH);
		}
		else {
			throw new Exception("Invalid gender code: " + genders);
		}
			
		// Interate RIFMapsParameters hash map for each RIFMapsParameter and write map
		for (String key : rifMapsParameters.getKeySet()) {
			RIFMapsParameters.RIFMapsParameter rifMapsParameter=
				rifMapsParameters.getRIFMapsParameter(key);	
				
			String mapTitle=rifMapsParameter.getMapTitle();
			String resultsColumn=rifMapsParameter.getResultsColumn();
			RIFStyle rifSyle=rifMapsParameter.getRIFStyle(featureCollection.getFeatureCollection());
						
			Iterator <Sex> GenderIter = allSexes.iterator();
			while (GenderIter.hasNext()) { // Iterate through genders
				Sex sex=GenderIter.next();
				
				writeMap( // JPEG, PS, EPS, SVG
					true /* whiteBackground */,
					featureCollection,
					temporaryDirectory,
					studyID,
					invID,
					mapTitle, 
					resultsColumn,
					rifSyle,
					baseStudyName,
					studyDescription,
					invName,
					invDescription,
					sex);
				
				writeMap( // GeoTIFF and PNG
					false /* whiteBackground */, // Transparent background
					featureCollection,
					temporaryDirectory,
					studyID,
					invID,
					mapTitle, 
					resultsColumn,
					rifSyle,
					baseStudyName,
					studyDescription,
					invName,
					invDescription,
					sex);		
			}					
		}	
	}
	
	/** Write map
	 * 
	 * @param: boolean whiteBackground,
	 * @param: RifFeatureCollection rifFeatureCollection,
	 * @param: File temporaryDirectory,
	 * @param: String studyID,
	 * @param: String InvID,
	 * @param: String mapTitle,
	 * @param: String resultsColumn,
	 * @param: RIFStyle rifStyle,
	 * @param: String baseStudyName,
	 * @param: String studyDescription,
	 * @param: String invName,
	 * @param: String invDescription,
	 * @param: Sex sex
	 */
	private void writeMap(
		final boolean whiteBackground,
		final RifFeatureCollection rifFeatureCollection,
		final File temporaryDirectory,
		final String studyID,
		final String invID,
		final String mapTitle,
		final String resultsColumn,
		final RIFStyle rifStyle,
		final String baseStudyName,
		final String studyDescription,
		final String invName,
		final String invDescription,
		final Sex sex) 
			throws Exception {
				
		FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
		Filter genderFilter=filterFactory.equal(
			filterFactory.property("genders"), 
			filterFactory.literal(""+sex.getCode() /* As string */),
			true);
		DefaultFeatureCollection dataFeatureCollection=rifFeatureCollection.getFeatureCollection();
		FeatureCollection featureCollection=dataFeatureCollection.subCollection(genderFilter); 
			// Apply gender filter
		
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
		// Use NamedLayer instead of UserLayer as more standard (e.g. QGis likes it)
		rifStyle.writeSldFile(resultsColumn, studyID, temporaryDirectory, dirName, mapTitle,
			true /* useNamedLayer */);

		//Create map		
		MapContent map = new MapContent();
		map.setTitle(mapTitle);

		// Set projection
		MapViewport vp = map.getViewport();
		vp.setCoordinateReferenceSystem(crs);
		vp.setBounds(expandedEnvelope);	
	
		// Deduce aspect ratio (the ratio of the width to the height of an image or screen)
		int imageWidth=mapWidthPixels;
		int imageHeight=0;
		double heightToWidth = expandedEnvelope.getSpan(1) / expandedEnvelope.getSpan(0); // Inverse aspect ratio
		imageHeight=(int) Math.round(imageWidth * heightToWidth); 
		Rectangle screenBounds = new Rectangle(0, 0, imageWidth, imageHeight);
		
		int xPixels=(int)Math.abs((mapWidthPixels*gridSquareWidth)/ 					// In a grid
			(expandedEnvelope.getMaximum(0)-expandedEnvelope.getMinimum(0)));
		int yPixels=(int)Math.abs(heightToWidth*xPixels);								// In a grid
		
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
			"; xPixels: " + xPixels +
			"; yPixels: " + yPixels + lineSeparator + 
			"map box: " + mapArea.toString() + "; CRS: " + CRS.toSRS(mapArea.getCoordinateReferenceSystem()) + lineSeparator +
			"map max bounds: " + mapMaxBounds.toString() + "; CRS: " + CRS.toSRS(mapMaxBounds.getCoordinateReferenceSystem()) + lineSeparator +
			"screenArea: " + screenArea.toString());	
			
		// Add layers to map
		Layer gridLayer = null;
		if (whiteBackground) { // JPEG, PS, EPS, SVG
			gridLayer = createGridLayer(expandedEnvelope, gridSquareWidth, gridVertexSpacing, crs,
				gridBackgroundColor, rifStyle, xPixels, yPixels);
		}
		else { // Transparent background: GeoTIFF and PNG
			gridLayer = createGridLayer(expandedEnvelope, gridSquareWidth, gridVertexSpacing, crs,
				null /* gridBackgroundColor */, rifStyle, xPixels, yPixels);
		}
		if (!map.addLayer(gridLayer)) { 
			throw new Exception("Failed to add gridLayer to map: " + mapTitle);
		}
	
		Layer backgroundAreasLayer = null;
		if (backgroundAreasFeatureCollection != null) {
			backgroundAreasLayer = new FeatureLayer(backgroundAreasFeatureCollection, 
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
		
        FeatureLayer featureLayer = new FeatureLayer(featureCollection, style);	
		featureLayer.setTitle(mapTitle);		
		if (!map.addLayer(featureLayer)) {
			throw new Exception("Failed to add FeatureLayer to map: " + mapTitle);
		}		
		
		LegendLayer legendLayer = createLegendLayer(rifStyle, expandedEnvelope, mapTitle, 
			studyDescription, invName, invDescription,
			sex, gridScale, imageWidth); 
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
		if (whiteBackground) { // JPEG, PS, EPS, SVG
			exportSVG(map, temporaryDirectory, dirName, filePrefix, studyID, invID, featureCollection.size(), 
				imageWidth, imageHeight, sex);	
				
			createGraphicsMaps(temporaryDirectory, dirName, filePrefix, studyID, invID, sex);
		}
		else { // Transparent background: GeoTIFF and PNG	
			createGeotoolsMaps(map, temporaryDirectory, dirName, filePrefix, studyID, invID, studyDescription,
				imageWidth, imageHeight, "tif", sex);			
			createGeotoolsMaps(map, temporaryDirectory, dirName, filePrefix, studyID, invID, studyDescription,
				imageWidth, imageHeight, "png", sex);				
		}
		
		map.dispose();
	}
	
	/**
	 * Create grid layer (white background maps only)
	 *
	 * @param ReferencedEnvelope expandedEnvelope, 
	 * @param double gridSquareWidth,
	 * @param double gridVertexSpacing,
	 * @param CoordinateReferenceSystem crs,
	 * @param Color backgroundColor,
	 * @param RIFStyle rifStyle,
	 * @param Rint xPixels,
	 * @param Rint yPixels
	 *
	 * @returns FeatureLayer
	 */
	private FeatureLayer createGridLayer(
		final ReferencedEnvelope expandedEnvelope, 
		final double gridSquareWidth,
		final double gridVertexSpacing,
		final CoordinateReferenceSystem crs,
		final Color backgroundColor,
		final RIFStyle rifStyle, 
		final int xPixels, 
		final int yPixels) 
			throws IOException {
		rifLogger.info(this.getClass(), "Add grid; gridSquareWidth: " + gridSquareWidth + 
			"; gridVertexSpacing: " + gridVertexSpacing);
			
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName("squaretype");
		typeBuilder.add("square", Polygon.class, crs);
		typeBuilder.add("color", Color.class);		
		typeBuilder.add("coord", String.class);		
		typeBuilder.add("horizAlign", Double.class);			
		typeBuilder.add("vertAlign", Double.class);			
		typeBuilder.add("rotation", Double.class);
		typeBuilder.add("xDisplacement", Integer.class);
		typeBuilder.add("yDisplacement", Integer.class);		
		
		if (enableCoordinateDisplay) {		
			rifLogger.info(this.getClass(), "Coordinate display enabled: " + gridColor.toString() + "; background: " +
				gridBackgroundColor.toString());
		}
		else {		
			rifLogger.info(this.getClass(), "Coordinate display disabled");
		}
		SimpleFeatureType TYPE = typeBuilder.buildFeatureType();
		GridFeatureBuilder builder = new GridFeatureBuilder(TYPE) {
			@Override
			public void setAttributes(GridElement element, Map<String, Object> attributes) {
				PolygonElement polyEl = (PolygonElement) element;
				ReferencedEnvelope bounds = element.getBounds();
				
				if (enableCoordinateDisplay) {	
					double xMin=bounds.getMinimum(0);
					double xMax=bounds.getMaximum(0); 		
					double yMin=bounds.getMinimum(1);
					double yMax=bounds.getMaximum(1);
					double horizAlign=rifStyle.ALIGN_RIGHT;		
					double vertAlign=rifStyle.ALIGN_MIDDLE; 	// Nothing else works!
					double rotation=0; 							// Clockwise in degrees
					int xDisplacement=0; 						// pixels
					int yDisplacement=(int)(yPixels/2); 		// pixels (TOP of box)
					
					String coord=createCoord(xMax, yMax);
					if ((expandedEnvelope.getMaximum(1) == yMax) &&
					    (expandedEnvelope.getMinimum(0) == xMin)) { // Top left
//						coord="(" + xMax + "," + yMax + ") " + "TL(xMax, yMax)";
					}
					else if ((expandedEnvelope.getMaximum(1) == yMax) &&
							 (expandedEnvelope.getMaximum(0) == xMax)) { // Top right
//						coord="(" + xMax + "," + yMax + ") " + "TR(xMax, yMax)";
					}
					else if ((expandedEnvelope.getMinimum(1) == yMin) &&
							 (expandedEnvelope.getMaximum(0) == xMax)) { // Bottom right
//						coord="(" + xMax + "," + yMax + ") " + "BR(xMax, yMax)";
					}
					else if (expandedEnvelope.getMaximum(1) == yMax) { // Top middle
//						coord="(" + xMax + "," + yMax + ") " + "TM(xMax, yMax)";
					}
					else if (expandedEnvelope.getMaximum(0) == xMax) { // Right middle
//						coord="(" + xMax + "," + yMax + ") " + "RM(xMax, yMax)";
					}
					else if (expandedEnvelope.getMinimum(0) == xMin) { // Left, not top; Suppressed: Legend
						coord="";
					}
					else if (expandedEnvelope.getMinimum(1) == yMin) { // Bottom
						coord="";
					}
					else { // Other
						coord="";
					}

					rifLogger.info(rifMapsClass, "(" + xMin + "," + yMin + ") label: " + coord);	
					attributes.put("coord", coord);
					attributes.put("horizAlign", horizAlign); 	
					attributes.put("vertAlign", vertAlign); 	
						// See: http://docs.geoserver.org/stable/en/user/styling/sld/reference/labeling.html
					attributes.put("xDisplacement", xDisplacement); 	// pixels
					attributes.put("yDisplacement", yDisplacement); 	// pixels
				}			
			}
		}; 
		SimpleFeatureSource grid = Grids.createSquareGrid(expandedEnvelope, gridSquareWidth,
			gridVertexSpacing, builder);	
		StyleFactoryImpl styleFactory = (StyleFactoryImpl) CommonFactoryFinder.getStyleFactory();
		FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
		Font font=styleFactory.getDefaultFont();
		font.setSize(filterFactory.literal(coordinateDisplayFontSize));
		
//		Style style=SLD.createPolygonStyle(gridColor, gridBackgroundColor, (float)0.5 /* fillOpacity */, 
//			"coord" /* labelField */, (Font)font /* labelFont */); // Old style
		RIFStyle rifSyle=new RIFStyle();
		Style style=rifSyle.createGridPolygonStyle(
			gridColor			/* lineColor */, 
			backgroundColor 	/* fillColor */, 
			gridWidth			/* lineWidth */, 
			(float)0.5 			/* fillOpacity */,
			"coord" 			/* label */,
			coordinateDisplayFontSize	/* labelFontSize */);
		FeatureLayer gridLayer = new FeatureLayer(grid.getFeatures(), style);
//		FeatureLayer gridLayer = new FeatureLayer(grid.getFeatures(), // Old gridLayer
//			SLD.createLineStyle(gridColor, gridWidth));
		if (backgroundColor != null) {
			gridLayer.setTitle("Grid");	
		}		
		else {
			gridLayer.setTitle("TransparentGrid");	
		}
		
		return gridLayer;
	}

	/**
	 * Create coordinate string (x, y) left padding to 20 characters
	 *
	 * @param double x 
	 * @param double y 
	 * @returns String
	 */
	private String createCoord(final double x, final double y) {
		int n=24; // left padding
		return /* "*" + */ String.format("%1$" + n + "s", "(" + x + "," + y + ")");  
	}

	/**
	 * Create map legend [layer]
	 *
	 * @param RIFStyle rifSyle, 
	 * @param ReferencedEnvelope envelope,
	 * @param String mapTitle,
	 * @param String studyDescription,
	 * @param String invName,
	 * @param String invDescription,
	 * @param Sex sex,
	 * @param String gridScale,
	 * @param int imageWidth
	 *
	 * @returns LegendLayer
	 */
	private LegendLayer createLegendLayer(
		final RIFStyle rifSyle, 
		final ReferencedEnvelope envelope,
		final String mapTitle,
		final String studyDescription,
		final String invName,
		final String invDescription,
		final Sex sex,
		final String gridScale,
		final int imageWidth)
				throws Exception {
			
		ArrayList<LegendLayer.LegendItem> legendItems = new ArrayList<LegendLayer.LegendItem>();
		ArrayList<RIFStyle.RIFStyleBand> rifStyleBands = rifSyle.getRifStyleBands();
		StringBuffer sb = new StringBuffer();
		int i=0;
		for (RIFStyle.RIFStyleBand rifStyleBand : rifStyleBands) {
			i++;
			String title=null;
			// Fix title into a mathematically correct form
			// As per http://docs.geotools.org/stable/javadocs/org/geotools/filter/function/RangedClassifier.html
			if (i < rifStyleBands.size()) {
				title=rifStyleBand.getTitle().replace("..", " to < ");	
			}
			else {
				title=rifStyleBand.getTitle().replace("..", " to <= ");		
			}
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
	
		if (enableMapGrids) {
			LegendLayer.LegendItem gridScaleLegendItem = new LegendLayer.LegendItem(
				"Grids: " + gridScale, 
				null,
				Geometries.MULTIPOLYGON);
			legendItems.add(gridScaleLegendItem);	
		}
		String crsName=""+envelope.getCoordinateReferenceSystem().getName();
		crsName=crsName.replace("EPSG:", "");
		if (crsName.equals("OSGB 1936 / British National Grid")) {
			crsName="British National Grid";
		}
		else if (crsName.length() < 12) {
			crsName="Projection: " + crsName;
		}		
		LegendLayer.LegendItem gridNameLegendItem = new LegendLayer.LegendItem(
			crsName, 
			null,
			Geometries.MULTIPOLYGON);
		legendItems.add(gridNameLegendItem);
		
		legendItems.add(spacerLegendItem);
		
		if (studyDescription != null) {
			LegendLayer.LegendItem studyDescriptionItem = new LegendLayer.LegendItem(
				studyDescription, 
				null,
				Geometries.MULTIPOLYGON);
			legendItems.add(studyDescriptionItem);	
		}
		if (invName != null && !invName.equals("My_New_Investigation")) {
			LegendLayer.LegendItem invNameItem = new LegendLayer.LegendItem(
				invName, 
				null,
				Geometries.MULTIPOLYGON);
			legendItems.add(invNameItem);	
		}
		if (invDescription != null && !invDescription.equals("")) {
			LegendLayer.LegendItem invDescriptionItem = new LegendLayer.LegendItem(
				invDescription, 
				null,
				Geometries.MULTIPOLYGON);
			legendItems.add(invDescriptionItem);	
		}
		LegendLayer.LegendItem gendersItem = new LegendLayer.LegendItem(
			"Genders: " + sex.getName(), 
			null,
			Geometries.MULTIPOLYGON);
		legendItems.add(gendersItem);
		
		LegendLayer.LegendItem abstractItem = new LegendLayer.LegendItem(
			"Style: " + rifSyle.getAbstract(), 
			null,
			Geometries.MULTIPOLYGON);
		legendItems.add(abstractItem);	
		
		LegendLayer legendLayer = new LegendLayer(mapTitle, Color.LIGHT_GRAY, legendItems, imageWidth);
		legendLayer.setTitle("Legend");
		
		return legendLayer;
	} 
	
	/**
	 * Generate an SVG document from the map. 
	 * 
	 * SVG file name:  <filePrefix><studyID>_<sex>_<printingDPI>dpi.svg	  
	 *
	 * @param MapContent map - Contains the layers (features + styles) to be rendered,
	 * @param File temporaryDirectory,
	 * @param String dirName, 
	 * @param String filePrefix, 
	 * @param String studyID, 
	 * @param String invID, 
	 * @param int numberOfAreas,
	 * @param int imageWidth. This sets the overall scale factor for the map, and is adjusted dependent on the 
	 *						  size of the map,
	 * @param int imageHeight. Fixed by the aspect ratio,
	 * @param: Sex sex
	 */
	public void exportSVG(
		final MapContent map, 
		final File temporaryDirectory,
		final String dirName, 
		final String filePrefix, 
		final String studyID,
		final String invID,
		final int numberOfAreas,
		final int imageWidth,
		final int imageHeight,
		final Sex sex) throws Exception {
			
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
		
		String svgFile=mapDirName + File.separator + filePrefix + studyID + "_inv" + invID + 
			"_" + sex.getName().toLowerCase() + "_" + printingDPI + "dpi.svg";
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
		rifLogger.info(this.getClass(), sb.toString());
		
			
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
				rifLogger.warning(this.getClass(), errors + " occurred rendering " + features + " SVG features");
			}
			else {
				rifLogger.info(this.getClass(),  "No errors occurred rendering " + features + " SVG features");
			}
			errors=0;
			features=0;
			
			g2d.dispose();
		}			
	}
	
	/**
	 * Generate map using Geotools GTRenderer and:
	 *
	 * a) ImageIO;
	 * b) GeoTiffWriter
	 * 
	 * File name:  <filePrefix><studyID>_<sex>_<printingDPI>dpi.<file extension>	  
	 *
	 * GeoTIFF output also writes a .tfw file (plain text files that store X and Y pixel size, 
	 * rotational information, and world coordinates for a map); and .prj projection files
	 * The goespatial information is embedded in the file anyway!
	 *
	 * @param MapContent map - Contains the layers (features + styles) to be rendered,
	 * @param File temporaryDirectory,
	 * @param String dirName, 
	 * @param String filePrefix, 
	 * @param String studyID,  
	 * @param String invID,  
	 * @param String studyDescription, 
	 * @param int imageWidth. This sets the overall scale factor for the map, and is adjusted dependent on the 
	 *						  size of the map,
	 * @param int imageHeight. Fixed by the aspect ratio,
	 * @param String imageType. In theory should support: Jtiff, bmp, gif, btiff, tif, wbmp, jpeg, jpg,
	 *							png, raw, pnm, jpeg2000.
	 *							Tested on TIFF before converting to geotiff,
	 * @param: Sex sex
	 */
	private void createGeotoolsMaps(
		final MapContent map, 
		final File temporaryDirectory,
		final String dirName, 
		final String filePrefix, 
		final String studyID,
		final String invID,
		final String studyDescription,
		final int imageWidth,
		final int imageHeight,
		final String imageType,
		final Sex sex) throws Exception {

		List<Layer>	mapLayers = map.layers();
		StringBuffer sb = new StringBuffer();
		int j=0;
		for (Layer mapLayer : mapLayers) {
			j++;
			if (mapLayer != null) {
				ReferencedEnvelope envel=mapLayer.getBounds();
				if (envel != null) {
					sb.append("Layer2[" + j + "]: " + mapLayer.getTitle() + 
						"; bounds: " + envel.toString() + lineSeparator);
				}
				else  {
					sb.append("Layer2[" + j + "]: " + mapLayer.getTitle() + 
						"; bounds: NONE" + lineSeparator);
				}
			}
		}
		rifLogger.info(this.getClass(), sb.toString());
		
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
		
		String outputFileName=mapDirName + File.separator + filePrefix + studyID + "_inv" + invID +
			"_" + sex.getName().toLowerCase() + "_" + printingDPI + "dpi." + imageType.toLowerCase();
		File outputFile = new File(outputFileName);
		if (outputFile.exists()) {
			outputFile.delete();
		}		
			
		ImageOutputStream outputImageFile = null;
		FileOutputStream fileOutputStream = null;
		try {	
		
			boolean hasPlugin=false;
			String[] imageIOWriter = ImageIO.getWriterFormatNames();
			/* 
			JPEG 2000, JPG, tiff, bmp, gif, WBMP, PNG, RAW, JPEG, btiff, PNM, tif, TIFF, wbmp, jpeg, jpg,
			JPEG2000, BMP, GIF, png, raw, pnm, TIF, jpeg2000, jpeg 2000, BTIFF
			 */
			for(int i=0; i < imageIOWriter.length; i++) {
//				rifLogger.info(this.getClass(), "imageIOWriter: " + imageIOWriter[i]);
				if (imageIOWriter[i].equals(imageType)) {
					hasPlugin=true;
				}
			}
			
			if (hasPlugin) {
				rifLogger.info(this.getClass(), "Create " + imageType + " map " + 
					imageWidth + "x" + imageHeight + "; file: " + outputFileName);

				BufferedImage bufferedImage = null;	
				if (imageType.toLowerCase().equals("tif") || 
				    imageType.toLowerCase().equals("tiff") ||
				    imageType.toLowerCase().equals("png")) {	
					bufferedImage = new BufferedImage(imageWidth, imageHeight, 
						BufferedImage.TYPE_INT_ARGB); // Allow transparency [will work for PNG as well!]
				}
				else {	
					bufferedImage = new BufferedImage(imageWidth, imageHeight, 
						BufferedImage.TYPE_INT_RGB);
				}
				Graphics2D g2d = bufferedImage.createGraphics();

				CoordinateReferenceSystem crs=map.getCoordinateReferenceSystem();
				ReferencedEnvelope envelope=map.getMaxBounds();	
				Rectangle screenBounds = new Rectangle(0, 0, imageWidth, imageHeight);

/*
 * Paint MapContent onto a buffered image
 */				
				GTRenderer gtRenderer = new StreamingRenderer();		
				gtRenderer.addRenderListener(new RenderListener() {
					public void featureRenderer(SimpleFeature feature) {
						features++;
					}
					public void errorOccurred(Exception exception) {			
						rifLogger.warning(this.getClass(), "GTRenderer error: " + exception.getMessage());
						errors++;
					}
				}); 	

				gtRenderer.setMapContent(map);
				Rectangle outputArea = new Rectangle(imageWidth, imageHeight);
				gtRenderer.paint(g2d, outputArea, envelope); 
					
				if (imageType.toLowerCase().equals("tif") || imageType.toLowerCase().equals("tiff")) {	
					// USe GeoTIFF renderer
					// Turn Graphics2D into a Coverage, and then save it to GeoTiff	
				
					GridCoverageFactory factory = new GridCoverageFactory();
					GridCoverage2D coverage = factory.create("geotiff", bufferedImage, envelope);
						   
					GeoTiffWriter geoTiffWriter = new GeoTiffWriter(outputFile); // get a writer
					
					if (copyrightInfo != null) {
						geoTiffWriter.setMetadataValue(
							Integer.toString(BaselineTIFFTagSet.TAG_COPYRIGHT), copyrightInfo);
					}	
					if (studyDescription != null) {
						geoTiffWriter.setMetadataValue(
							Integer.toString(BaselineTIFFTagSet.TAG_IMAGE_DESCRIPTION), studyDescription);
					}
					if (software != null) {
						geoTiffWriter.setMetadataValue(
							TagSet.BASELINE + ":" + Integer.toString(BaselineTIFFTagSet.TAG_SOFTWARE), software);
					}
		
					final ParameterValue<Boolean> tfw = GeoTiffFormat.WRITE_TFW.createValue();
					// force the writer to write a tfw file (plain text files that store X and Y pixel size, 
					// rotational information, and world coordinates for a map); and .prj projection files
					// The goespatial information is embedded in the file anyway!
					tfw.setValue(true);
					try {		
						geoTiffWriter.write(coverage, new GeneralParameterValue[]{tfw});
						rifLogger.info(this.getClass(),  "No errors occurred rendering " + features + " " + 
							imageType + " " + " features to: " + outputFileName);
					}
					catch (Exception exception) {
						rifLogger.error(this.getClass(), "Unable to write " + imageType + " file: " + 
							outputFileName, exception);			
					}
					finally {
						geoTiffWriter.dispose();
						coverage.dispose(true);
					}
				}
				else {	// Use imageIO renderer				
					fileOutputStream = new FileOutputStream(outputFile);
					outputImageFile = ImageIO.createImageOutputStream(fileOutputStream);
				
					boolean res=false;
					try {
						res=ImageIO.write(bufferedImage, imageType, outputImageFile);
					}
					catch (IllegalAccessError exception) {
						rifLogger.error(this.getClass(), "Unable to write " + imageType + " file: " + 
							outputFileName, exception);			
					}
					finally {
						if (errors > 0) {
							rifLogger.warning(this.getClass(), errors + " occurred rendering " + features + " " + imageType + 
								" " + " features to: " + outputFileName);
						}
						else {
							rifLogger.info(this.getClass(),  "No errors occurred rendering " + features + " " + imageType + 
								" " + " features to: " + outputFileName);
						}
						errors=0;
						features=0;
						
						g2d.dispose();
					}	
					
					if (!res) {
						throw new Exception("ImageIO.write failed writing file: " + outputFileName);
					}				
				} 
			}
			else {
				rifLogger.warning(this.getClass(), "Unable to write file: " + outputFileName + 
					"; NO PLUGIN FOUND FOR: " + imageType);
			}

		} 
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Exception writing file: " + outputFileName, exception);
			throw exception;
		} 
		finally {
			if (outputImageFile != null) {
				outputImageFile.flush();
				outputImageFile.close();
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		} 		
	} 
	
	/** Build Graphics file from SVG source
	  *
	  * Graphics file name: <filePrefix><studyID>_<sex>_<printingDPI>dpi_<year>.<outputType.getGraphicsExtentsion()>
      *
	  * SVG file name:  <filePrefix><studyID>.svg	  
	  *
	  * Does not build RIFGRAPHICS_GEOTIFF files (see createGeotoolsMaps)
	  *
	  * @param: File temporaryDirectory,
	  * @param: String dirName,
	  * @param: String filePrefix,
	  * @param: String studyID,
	  * @param: String invID,
	  * @param: RIFGraphicsOutputType outputType,
	  * @param: Sex sex
	  */
	private void createGraphicsMaps(
		final File temporaryDirectory,
		final String dirName,
		final String filePrefix,
		final String studyID,
		final String invID,
		final Sex sex) 
			throws Exception {
						
		RIFGraphics rifGraphics = new RIFGraphics(rifServiceStartupOptions, manager);
		
		Set<RIFGraphicsOutputType> allOutputTypes = EnumSet.of(
			RIFGraphicsOutputType.RIFGRAPHICS_JPEG,   
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
						invID,
						outputType,
						mapWidthPixels,
						sex);
				}
				else {
					rifGraphics.addGraphicsFile(
						temporaryDirectory,							/* Study scratch space diretory */
						dirName, 									/* directory */
						filePrefix, 								/* File prefix */
						studyID,
						invID,
						outputType,
						mapWidthPixels,
						sex);
				}
			}
		}		
	}
}
