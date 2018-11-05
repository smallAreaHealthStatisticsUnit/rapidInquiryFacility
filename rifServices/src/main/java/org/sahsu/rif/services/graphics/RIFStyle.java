package org.sahsu.rif.services.graphics;

import org.sahsu.rif.generic.util.RIFLogger;

import java.io.*;
import java.awt.Color;
import java.util.ArrayList;

import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.StyleGenerator;

import org.geotools.styling.Style;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Stroke;
import org.geotools.styling.Rule;
import org.geotools.styling.Fill;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.Displacement;

import org.geotools.feature.DefaultFeatureCollection;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Function;

import org.geotools.filter.function.Classifier;
import org.geotools.filter.function.RangedClassifier;

import org.geotools.factory.CommonFactoryFinder;

import org.geotools.styling.SLDTransformer;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.styling.UserLayer;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.Font;
import org.geotools.styling.TextSymbolizer;

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
	
public class RIFStyle {
	
	/* 
	 * Information about a RIF Style range band
	 */
	public static class RIFStyleBand
	{
		private Color color;
		private String title;
		
		/**
		  * Constructor: Create style band
		  *
		  * @param: Color color
		  * @param: String title
		  */
		public RIFStyleBand(Color color, String title) {
			this.color = color;
			this.title = title;
		}
		/**
		 * Accessors 
		 */		
		public Color getColor() {
			return color;
		}
		public String getTitle() {
			return title;
		}
	}
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private Style style=null;
	
	private String paletteName=null;
	private int numberOfBreaks=-1;
	private boolean invert=false;
	private Double minArray[] = null;
	private Double maxArray[] = null;
	private ArrayList<RIFStyleBand> rifStyleBands = new ArrayList<RIFStyleBand>();

	// https://github.com/geotools/geotools/blob/master/modules/unsupported/swt/src/main/java/org/geotools/swt/styling/simple/SLDs.java
	public static final double ALIGN_LEFT = 1.0;
    public static final double ALIGN_CENTER = 0.5;
    public static final double ALIGN_RIGHT = 0.0;
    public static final double ALIGN_BOTTOM = 1.0;
    public static final double ALIGN_MIDDLE = 0.5;
    public static final double ALIGN_TOP = 0.0;
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
     * Constructor with no Style
     */	
	public RIFStyle() {
		
	}
	
	/**
     * Constructor with pre-defined style
     * 
	 * @param String classifyFunctionName,
	 * @param String columnName,
	 * @param String paletteName,
	 * @param int numberOfBreaks,
	 * @param boolean invert,
	 * @param DefaultFeatureCollection featureCollection
     */
	public RIFStyle(
			final String rifMethod,
			final String columnName,
			final String paletteName,
			final int numberOfBreaks,
			final boolean invert,
			final DefaultFeatureCollection featureCollection) {
		
		try {
			style=createPredefinedRifStyle(rifMethod, columnName, paletteName, numberOfBreaks, 
				invert, featureCollection);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFStyle() constructor", exception);
			throw new IllegalArgumentException(exception);
		}
	}	

	/**
     * Constructor with user defined style
     * 	
	 * @param String userStyleName,
	 * @param String columnName,
	 * @param String paletteName,
	 * @param double[] breaks,
	 * @param boolean invert,
	 * @param DefaultFeatureCollection featureCollection
     */
	public RIFStyle(
			final String userStyleName,
			final String columnName,
			final String paletteName,
			final double[] breaks,
			final boolean invert,
			final DefaultFeatureCollection featureCollection) {
		
		try {
			style=createUserDefinedRifStyle(userStyleName, columnName, paletteName, breaks, 
				invert, featureCollection);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFStyle() constructor", exception);
			throw new NullPointerException();
		}
	}
			
	/** Create predefined style for RIF choropleth maps. Uses same colorbrewer and classification schemes 
	  * as the front end
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
	  * See: rifs-util-choro.js
	  *
	  * @param String rifMethod,
	  * @param String columnName,
	  * @param String paletteName,
	  * @param int numberOfBreaks,
	  * @param boolean invert,
	  * @param DefaultFeatureCollection featureCollection
	  *
	  * @return Style 
	  */													
	private Style createPredefinedRifStyle(
			final String rifMethod,
			final String columnName,
			final String lpaletteName,
			final int lnumberOfBreaks,
			final boolean linvert,
			final DefaultFeatureCollection featureCollection) 
				throws Exception {
		Classifier groups=null;
		
		paletteName=lpaletteName;
		numberOfBreaks=lnumberOfBreaks;
		invert=linvert;
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
		else {
			throw new Exception("Invalid RIF mapping method: " + rifMethod);
		}

		if (classifyFunctionName != null && !(
				classifyFunctionName.equals("EqualInterval") || 
				classifyFunctionName.equals("Jenks") ||
				classifyFunctionName.equals("Quantile") || 
				classifyFunctionName.equals("standardDeviation")
			)) {
			throw new Exception("Unsupported classify Function Name: " + classifyFunctionName);
		}

		FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();
		PropertyName propertyExpression = filterFactory.property(columnName);

		if (groups == null) {
			Function classify = filterFactory.function(classifyFunctionName, 
				propertyExpression, filterFactory.literal(numberOfBreaks));
			try {
				groups = (Classifier) classify.evaluate(featureCollection);  // Classify data 
			}
			catch (NullPointerException npe) {
				throw new Exception("Failed to classify featureCollection; function Name: " + classifyFunctionName +
					"; column: " + columnName + 
					"; NullPointerException in classify.evaluate()");
			}
		}

		return createUsingFeatureTypeStyle(rifMethod, columnName, paletteName, numberOfBreaks, invert, 
			groups, featureCollection, propertyExpression);
	}

	/**	Create user defined style for RIF choropleth maps. Uses same colorbrewer and classification schemes 
	  * as the front end  
	  *
	  * Two other scales are provided: 
	  * - AtlasRelativeRisk: fixed scale at: [0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51] between +/-infinity; PuOr
	  * - AtlasProbability: fixed scale at: [0.20, 0.81] between 0 and 1; RdYlGn
	  *
	  * @param String userStyleName,
	  * @param String columnName,
	  * @param String paletteName,
	  * @param int numberOfBreaks,
	  * @param boolean invert,
	  * @param DefaultFeatureCollection featureCollection
	  *
	  * @return Style 
	  */													
	private Style createUserDefinedRifStyle(
			final String userStyleName,
			final String columnName,
			final String lpaletteName,
			final double[] breaks,
			final boolean linvert,
			final DefaultFeatureCollection featureCollection) 
				throws Exception {
		Classifier groups=null;
		
		paletteName=lpaletteName;
		invert=linvert;
		String classifyFunctionName=null;	

		numberOfBreaks=(breaks.length-1);
		Comparable min[] = new Comparable[numberOfBreaks];
		Comparable max[] = new Comparable[numberOfBreaks];
		for (int i=1; i<breaks.length; i++) {
			min[i-1]=breaks[i-1];
			max[i-1]=breaks[i];
		}
		groups = new RangedClassifier(min, max);

		FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();
		PropertyName propertyExpression = filterFactory.property(columnName);

		return createUsingFeatureTypeStyle(userStyleName, columnName, paletteName, numberOfBreaks, invert, 
			groups, featureCollection, propertyExpression);
	}

	/** Create Style using createFeatureTypeStyle [common to both style creation functions]
	  * 
	  * @param String rifMethod,
	  * @param String columnName,
	  * @param String paletteName,
	  * @param int numberOfBreaks,
	  * @param boolean invert,
	  * @param Classifier groups,
	  * @param DefaultFeatureCollection featureCollection,
	  * @param PropertyName propertyExpression
	  *
	  * @return Style 
	  */
	private Style createUsingFeatureTypeStyle(
		final String rifMethod,
		final String columnName,
		final String paletteName,
		final int numberOfBreaks, 
		final boolean invert,
		final Classifier groups,
		final DefaultFeatureCollection featureCollection,
		final PropertyName propertyExpression) 
			throws Exception {
				
		StyleBuilder builder = new StyleBuilder();
		
		// Setup colours
		ColorBrewer brewer = ColorBrewer.instance();
		Color[] colors = brewer.getPalette(paletteName).getColors(numberOfBreaks);
		if (invert) {
			colors=invertColor(colors);
		}
		
		// Setup titles.Note these are modified by RIFMaps::createLegendLayer() from the .. form
		// to " to < " or " to <= "
		for (int i=0; i<colors.length; i++) {
			String title=null;
			try {
				title=groups.getTitle(i);
			}
			catch (Exception exception) {
				rifLogger.error(this.getClass(), "Classifier groups.getTitle() failed for color[" + i + "]: " + 
					colors[i], exception);
			}
			
			if (title == null) {
				throw new Exception("No title found for color[" + i + "]: " + colors[i]);
			}
			RIFStyleBand rifStyleBand=new RIFStyleBand(colors[i], title);
			rifStyleBands.add(rifStyleBand);
		}
			
		// Create FeatureTypeStyle
		FeatureTypeStyle featureTypeStyle = StyleGenerator.createFeatureTypeStyle(
            groups												/* Classifier */,
            propertyExpression,
            colors,
            "Generated FeatureTypeStyle for " + paletteName		  /* Type ID */,
            featureCollection.getSchema().getGeometryDescriptor() /* GeometryDescriptor  */,
            StyleGenerator.ELSEMODE_IGNORE,
            0.95	/* opacity */,
            null 	/* defaultStroke */);
	
		// You cannot add a genders filter genders filter here as 
		// 1. You would need to add it at the end (no effect, no styling)
		// 2. Modify existing - but the the bands would be wrong for Qunatiles etc
		// So it must be done pre styling
		Rule currRules[]=featureTypeStyle.rules().toArray(new Rule[0]);	
		// Dump rules for debug
		StringBuilder currRulesText = new StringBuilder();
		for (int i=0; i<currRules.length; i++) {
			currRulesText.append("[" + i + "]: " + currRules[i].toString());
		}
/*
currRules: [0]: <RuleImpl:rule01> [[ rr >= -Infinity ] AND [ rr < 0.68 ]]
	org.geotools.styling.PolygonSymbolizerImpl@40cc9623
[1]: <RuleImpl:rule02> [[ rr >= 0.68 ] AND [ rr < 0.76 ]]
	org.geotools.styling.PolygonSymbolizerImpl@b00c2acd
[2]: <RuleImpl:rule03> [[ rr >= 0.76 ] AND [ rr < 0.86 ]]
	org.geotools.styling.PolygonSymbolizerImpl@74b1b842
[3]: <RuleImpl:rule04> [[ rr >= 0.86 ] AND [ rr < 0.96 ]]
	org.geotools.styling.PolygonSymbolizerImpl@a012a045
[4]: <RuleImpl:rule05> [[ rr >= 0.96 ] AND [ rr < 1.07 ]]
	org.geotools.styling.PolygonSymbolizerImpl@7ddeb506
[5]: <RuleImpl:rule06> [[ rr >= 1.07 ] AND [ rr < 1.2 ]]
	org.geotools.styling.PolygonSymbolizerImpl@3d72f275
[6]: <RuleImpl:rule07> [[ rr >= 1.2 ] AND [ rr < 1.35 ]]
	org.geotools.styling.PolygonSymbolizerImpl@da25724c
[7]: <RuleImpl:rule08> [[ rr >= 1.35 ] AND [ rr < 1.51 ]]
	org.geotools.styling.PolygonSymbolizerImpl@a7cb1cd7
[8]: <RuleImpl:rule09> [[ rr >= 1.51 ] AND [ rr <= Infinity ]]
	org.geotools.styling.PolygonSymbolizerImpl@1ec14d21
 */
		rifLogger.info(this.getClass(), "Rules: " + currRulesText.toString());

		// Create Style
		Style style = builder.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);
			
		// Add title and abstract
		style.getDescription().setTitle("RIFStyle: " + columnName);
		style.getDescription().setAbstract(rifMethod.substring(0, 1).toUpperCase() + // Force first letter to
																					 // a capital
			rifMethod.substring(1));
 
        return style;	
	}
	
	/** Write style SLD file
	  *
	  * @param String resultsColumn, 
	  * @param String studyID, 
	  * @param File temporaryDirectory, 
	  * @param String dirName, 
	  * @param String mapTitle, 
	  * @param boolean useNamedLayer (as opposed to UserLayer)
	  */
	public void writeSldFile(
			final String resultsColumn, 
			final String studyID, 
			final File temporaryDirectory,
			final String dirName,
			final String mapTitle,
			final boolean useNamedLayer) 
				throws Exception {
		StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
		StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();
		sld.setName(mapTitle);
		sld.setTitle(mapTitle);
		sld.setAbstract(mapTitle);
		
		// Use NamedLayer instead of UserLayer as more standard (e.g. QGis likes it)
		if (useNamedLayer) {
			NamedLayer namedLayer = styleFactory.createNamedLayer();		
			namedLayer.setName(resultsColumn);
			namedLayer.addStyle(style);		
			sld.layers().add(namedLayer);
		}
		else {
			UserLayer userLayer = styleFactory.createUserLayer();	
			userLayer.setName(resultsColumn);
			userLayer.userStyles().add(style);
			sld.layers().add(userLayer);
		}
		SLDTransformer styleTransform = new SLDTransformer();
		String sldXml = styleTransform.transform(sld);
		BufferedWriter sldWriter = null;
		String mapDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName;
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
		
		String sldFileName=mapDirName + File.separator + resultsColumn + "_" + studyID + ".sld";
		File file = new File(sldFileName);
		if (file.exists()) {
			file.delete();
		}
		try {
			sldWriter = new BufferedWriter(new FileWriter(sldFileName));
			sldWriter.write(sldXml);
		}
		finally {
			if (sldWriter != null) {
				sldWriter.close();
			}
		}				
	}
	
	/**
	 * Accessors 
	 */
	public Style getStyle() {
		return style;
	}
	public String getPaletteName() {
		return paletteName;
	}
	public int getNumberOfBreaks() {
		return numberOfBreaks;
	}
	public boolean getInvert() {
		return invert;
	}
	public ArrayList<RIFStyleBand> getRifStyleBands() {
		return rifStyleBands;
	}
	public String getTitle() {
		if (style != null) {
			return style.getDescription().getTitle().toString();
		}
		else {
			return null;
		}
	}
	public String getAbstract() {
		if (style != null) {
			return style.getDescription().getAbstract().toString();
		}
		else {
			return null;
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
	
   /**
     * Create a Style to draw grid polygon features, with an optional label field. The position of the label
	 * is controlled by the following atributes of the type builder (see usage in: RIFMaps.java)
	 *	 
	 * horizAlign: 		ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT (no effect unless rotation 0, +/- 180 degrees)
	 * vertAlign: 		ALIGN_BOTTOM, ALIGN_MIDDLE, ALIGN_TOP (no effect unless rotation +/- 90 degrees)
	 * xDisplacement:	in pixels 
	 * yDisplacement:	in pixels
	 * rotation:		in degress
	 *
	 * horizAlign and vertAlign usage:
	 *
	 * https://github.com/geotools/geotools/blob/master/modules/unsupported/swt/src/main/java/org/geotools/swt/styling/simple/SLDs.java
	 *
	 * The effects are detailed in:
	 *
	 * http://docs.geoserver.org/stable/en/user/styling/sld/reference/labeling.html
	 *
	 *	  RIFStyle.ALIGN_LEFT = 1.0;
	 *    RIFStyle.ALIGN_CENTER = 0.5;
	 *    RIFStyle.ALIGN_RIGHT = 0.0;
	 *    RIFStyle.ALIGN_BOTTOM = 1.0;
	 *    RIFStyle.ALIGN_MIDDLE = 0.5;
	 *    RIFStyle.ALIGN_TOP = 0.0;
	 *
	 * @param Color lineColor, 
	 * @param Color fillColor, 
	 * @param double lineSize, 
	 * @param double opacity,
	 * @param String label,
	 * @param double labelFontSize
	 *
	 * @returns Style
     */
    public Style createGridPolygonStyle(
		final Color lineColor, 
		final Color fillColor, 
		final double lineSize, 
		final double fillOpacity,
		final String label,
		final double labelFontSize) {
		StyleBuilder builder = new StyleBuilder();
		FilterFactory filterFactory = builder.getFilterFactory();
		
        // create a fully opaque outline stroke
        Stroke stroke = builder.createStroke(
                filterFactory.literal(lineColor),
                filterFactory.literal(lineSize),
                filterFactory.literal(1)); 			/* lineOpacity */

        // create a partially opaque fill
        Fill fill = Fill.NULL;
        if (fillColor != null) {
			fill = builder.createFill(
                filterFactory.literal(fillColor),
                filterFactory.literal(fillOpacity)); /* fillOpacity */
		}
				
		// Create a polygon symbolizer for the square grid		
        PolygonSymbolizer sym = builder.createPolygonSymbolizer(stroke, fill, null /* geometryPropertyName */);

		StyleFactoryImpl styleFactory = (StyleFactoryImpl) CommonFactoryFinder.getStyleFactory();
		Font font=styleFactory.getDefaultFont();
		font.setSize(filterFactory.literal(labelFontSize));
		
		// Create a text symbolizer for the square grid label
		TextSymbolizer tSym = null;
		if (label != null) {
			AnchorPoint anchorPoint = builder.createAnchorPoint( // Anchor to this relative position in the square
 
				builder.attributeExpression("horizAlign"), 	/* horizAlign */
				builder.attributeExpression("vertAlign")); 	/* vertAlign */

			Displacement displacement=builder.createDisplacement(
				builder.attributeExpression("xDisplacement"),
				builder.attributeExpression("yDisplacement"));
			PointPlacement pointPlacement = builder.createPointPlacement(
				anchorPoint, 
				displacement,								/* Displacement */
				builder.attributeExpression("rotation")	 	/* rotation */);
			tSym = builder.createTextSymbolizer(
				builder.createFill(
					filterFactory.literal(Color.BLACK),
					filterFactory.literal(1)),		/* fill */
				new Font[] {font} , 				/* fonts */
				builder.createHalo(),				/* halo */
				builder.attributeExpression(label),	/* label */
				pointPlacement 						/* labelPlacement */,
				null 								/* geometryPropertyName */);
		}
		
		Style style = null;
		if (tSym != null) {
			style=wrapSymbolizers("gridPolgonStyle", builder, styleFactory, sym, tSym);
		}
		else {
			style=wrapSymbolizers("gridPolgonStyle2", builder, styleFactory, sym);
		}

        return style;
    }
	
	/**
     * Wrap one or more symbolizers into a Rule / FeatureTypeStyle / Style
     *
	 * @param String ftStyleName,
	 * @param StyleBuilder builder,
	 * @param StyleFactoryImpl styleFactory,
     * @param symbolizers one or more symbolizer objects
     *
     * @return a new Style instance or null if no symbolizers are provided
     */
    public static Style wrapSymbolizers(
		final String ftStyleName, 
		final StyleBuilder builder, 
		final StyleFactory styleFactory, 
		final Symbolizer ...symbolizers) {
        if (symbolizers == null || symbolizers.length == 0) {
            return null;
        }

        Rule rule = styleFactory.createRule();

        for (Symbolizer sym : symbolizers) {
            rule.symbolizers().add(sym);
        }

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] {rule});

        Style style = builder.createStyle();
        style.featureTypeStyles().add(fts);
		style.getDescription().setTitle("RIFStyle: " + ftStyleName);
		
        return style;
    }
	
}		
