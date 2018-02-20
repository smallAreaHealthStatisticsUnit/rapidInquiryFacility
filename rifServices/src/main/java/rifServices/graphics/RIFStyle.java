package rifServices.graphics;

import rifGenericLibrary.util.RIFLogger;

import java.io.*;
import java.awt.Color;
import java.util.ArrayList;

import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.StyleGenerator;

import org.geotools.styling.Style;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Stroke;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Fill;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PolygonSymbolizer;

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
import org.geotools.styling.StyleFactory;
import org.geotools.styling.UserLayer;

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

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================
	/**
     * Constructor.
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
			style=createRifStyle(rifMethod, columnName, paletteName, numberOfBreaks, invert, featureCollection);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFStyle() constructor", exception);
			throw new NullPointerException();
		}
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
		else if (rifMethod.equals("AtlasRelativeRisk")) { // fixed scale at: [0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51] between +/-infinity
			paletteName="PuOr";
			numberOfBreaks=8;
			Comparable min[] = new Comparable[]{Double.NEGATIVE_INFINITY, 0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35};
			Comparable max[] = new Comparable[]{0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, Double.POSITIVE_INFINITY};
			groups = new RangedClassifier(min, max);
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
		// Add support for -1 data
		if (groups == null) {
			Function classify = filterFactory.function(classifyFunctionName, 
				propteryExpression, filterFactory.literal(numberOfBreaks));
			groups = (Classifier) classify.evaluate(featureCollection);  // Classify data 
		}

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
			
		FeatureTypeStyle featureTypeStyle = StyleGenerator.createFeatureTypeStyle(
            groups												/* Classifier */,
            propteryExpression,
            colors,
            "Generated FeatureTypeStyle for " + paletteName		  /* Type ID */,
            featureCollection.getSchema().getGeometryDescriptor() /* GeometryDescriptor  */,
            StyleGenerator.ELSEMODE_IGNORE,
            0.95	/* opacity */,
            null 	/* defaultStroke */);
        Style style = builder.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);
			
		style.getDescription().setTitle("RIFStyle: " + columnName);
		style.getDescription().setAbstract(rifMethod + ": " + numberOfBreaks);

        return style;		
	}

	/** Write style SLD file
	  *
	  * @param String resultsColumn, 
	  * @param String studyID, 
	  * @param File temporaryDirectory, 
	  * @param String dirName, 
	  * @param String mapTitle
	  */
	public void writeSldFile(
			final String resultsColumn, 
			final String studyID, 
			final File temporaryDirectory,
			final String dirName,
			final String mapTitle) 
				throws Exception {
		StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
		StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();
		sld.setName(mapTitle);
		sld.setTitle(mapTitle);
		sld.setAbstract(mapTitle);
		UserLayer layer = styleFactory.createUserLayer();
		layer.setName(resultsColumn);

		layer.userStyles().add(style);		
		sld.layers().add(layer);
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
	
}		