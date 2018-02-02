package rifServices.dataStorageLayer.common;

import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.DatabaseType;

import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;

import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.svggen.SVGGraphics2D;
//import org.jfree.graphics2d.svg.SVGGraphics2D; // Needs jfreesvg
import org.apache.batik.dom.GenericDOMImplementation;

import org.apache.fop.svg.AbstractFOPTranscoder; 
import org.apache.fop.svg.PDFTranscoder; 
import org.apache.fop.render.ps.PSTranscoder; 
import org.apache.fop.render.ps.EPSTranscoder; 

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.general.DefaultKeyedValues2DDataset;
import org.jfree.data.general.KeyedValues2DDataset;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.renderer.category.StandardBarPainter;

import org.w3c.dom.DOMImplementation; 
import org.w3c.dom.Document; 
import org.w3c.dom.Element; 

import java.io.*;
import java.sql.*;
import org.json.*;
import java.lang.*;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

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
	
public class RIFGraphics extends SQLAbstractSQLManager {
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static int denominatorPyramidWidthPixels;
	private static int printingDPI;
	
	// These need to become parameters. A generic method will be created
	private static float jpegQuality = new Float(0.8);
	private static float populationPyramidAspactRatio = new Float(1.43); 
										// ratio of the width to the height of an image or screen. 
										// r=w/h, h=w/r
	private static boolean enablePostscript = false; // Setting to true also disables the gradients
		
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
	public RIFGraphics(
			final RIFServiceStartupOptions rifServiceStartupOptions) {
		super(rifServiceStartupOptions.getRIFDatabaseProperties());
		
		this.rifServiceStartupOptions = rifServiceStartupOptions;
		
		denominatorPyramidWidthPixels=this.rifServiceStartupOptions.getDenominatorPyramidWidthPixels();
		printingDPI=this.rifServiceStartupOptions.getPrintingDPI();
	}

	private void PSTranscode(
			final TranscoderInput input,
			final TranscoderOutput output) 
			throws Exception {
			
		PSTranscoder transCoder=new PSTranscoder();
				// Set the transcoding hints.
		transCoder.addTranscodingHint(PSTranscoder.KEY_WIDTH, (float)denominatorPyramidWidthPixels); // Pixels
				// Default 3543: Single column 90 mm (255 pt)
		transCoder.addTranscodingHint(PSTranscoder.KEY_MEDIA, "print");
//		transCoder.addTranscodingHint(PSTranscoder.KEY_PAGE_ORIENTATION, "landscape");
//		transCoder.addTranscodingHint(PSTranscoder.KEY_SCALE_TO_PAGE, true);
		transCoder.addTranscodingHint(PSTranscoder.KEY_PIXEL_TO_MM, (float)(printingDPI/25.4)); // Default 1000dpi
//		transCoder.addTranscodingHint(PSTranscoder.KEY_USER_STYLESHEET_URI, 
//			"http://localhost:8080/RIF4/css/rifx-css-d3.css");	

		transCoder.transcode(input, output);	// Convert the image.		
	}
	
	private void EPSTranscode(
			final TranscoderInput input,
			final TranscoderOutput output) 
			throws Exception {
			
		EPSTranscoder transCoder=new EPSTranscoder();
				// Set the transcoding hints.
		transCoder.addTranscodingHint(EPSTranscoder.KEY_WIDTH, (float)denominatorPyramidWidthPixels); // Pixels
				// Default 3543: Single column 90 mm (255 pt)
		transCoder.addTranscodingHint(EPSTranscoder.KEY_MEDIA, "print");
//		transCoder.addTranscodingHint(EPSTranscoder.KEY_PAGE_ORIENTATION, "landscape");
//		transCoder.addTranscodingHint(EPSTranscoder.KEY_SCALE_TO_PAGE, true);
		transCoder.addTranscodingHint(EPSTranscoder.KEY_PIXEL_TO_MM, (float)(printingDPI/25.4)); // Default 1000dpi
//		transCoder.addTranscodingHint(EPSTranscoder.KEY_USER_STYLESHEET_URI, 
//			"http://localhost:8080/RIF4/css/rifx-css-d3.css");	

		transCoder.transcode(input, output);	// Convert the image.		
	}
			
	private void graphicsTranscode(
			final RIFGraphicsOutputType outputType,
			final TranscoderInput input,
			final TranscoderOutput output) 
			throws Exception {
			
		ImageTranscoder transCoder=null;
		switch (outputType) {
			case RIFGRAPHICS_JPEG:
		        // Create a JPEG transcoder
				transCoder = new JPEGTranscoder();						
				transCoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, jpegQuality);
				break;
			case RIFGRAPHICS_PNG:
		        // Create a PNG transcoder
				transCoder = new PNGTranscoder();
				break;
			case RIFGRAPHICS_TIFF:
		        // Create a TIFF  transcoder
				transCoder = new TIFFTranscoder();
				break;
			default:
				throw new Exception("graphicsTranscode(): Unsupported output type: " + outputType.toString());
		}	
		// Set the transcoding hints.
		transCoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float)denominatorPyramidWidthPixels); // Pixels
				// Default 3543: Single column 90 mm (255 pt)
		transCoder.addTranscodingHint(ImageTranscoder.KEY_MEDIA, "print");
		transCoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_TO_MM, (float)(printingDPI/25.4)); // Default 1000dpi
//		transCoder.addTranscodingHint(ImageTranscoder.KEY_USER_STYLESHEET_URI, 
//			"http://localhost:8080/RIF4/css/rifx-css-d3.css");	

		transCoder.transcode(input, output);	// Convert the image.		
	}
	
	private String getGraphicsExtentsion(
		final RIFGraphicsOutputType outputType) 
			throws Exception  {
			
		String extension="unk";
		
		switch (outputType) {
			case RIFGRAPHICS_JPEG:
		        extension="jpg";
				break;
			case RIFGRAPHICS_PNG:
		        extension="png";
				break;
			case RIFGRAPHICS_TIFF:
		        extension="tif";
				break;
			case RIFGRAPHICS_EPS:
		        extension="eps";
				break;
			case RIFGRAPHICS_PS:
		        extension="ps";
				break;
			default:
				throw new Exception("getGraphicsExtentsion(): Unsupported output type: " + outputType.toString());
		}			
		
		return extension;
	}
	
	public void addGraphicsFile(
		final File temporaryDirectory,
		final String dirName,
		final String filePrefix,
		final String studyID,
		final int year,
		final RIFGraphicsOutputType outputType) 
			throws Exception {
		Float printingPixelPermm=(float)(printingDPI/25.4);
		
		String graphicFileName=filePrefix + studyID + "_" + printingDPI +
			"dpi_" + year + "." + getGraphicsExtentsion(outputType);
		String svgFileName=filePrefix + studyID + "_" + year + ".svg";
		String svgDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName;
		String graphicFile=svgDirName + File.separator + graphicFileName;
		String svgFile=svgDirName + File.separator + svgFileName;
		rifLogger.info(this.getClass(), "Adding " + outputType.toString() + " for report file: " + 
			graphicFile +
			"; pixel width: " + denominatorPyramidWidthPixels + 
			"; pixels/mm: " + printingPixelPermm);


        // Create the transcoder input.
		File file = new File(svgFile);
		if (!file.exists()) {
			throw new Exception("SVG file: " + svgFile + " does not exist");
		}
        String svgURI = file.toURL().toString();
        TranscoderInput input = new TranscoderInput(svgURI);		
		
        // Use ZIP stream as the transcoder output.
		file = new File(graphicFile);
		if (file.exists()) {
			file.delete();
		}
		OutputStream ostream = new FileOutputStream(graphicFile);
        TranscoderOutput output = new TranscoderOutput(ostream);
		try {
			switch (outputType) {
				case RIFGRAPHICS_TIFF:  /* Fixed in Batik source: 21/8/2017, disabled using RIFGraphicsOutputType, 
				   RifTIFFTranscoder.java created to try to fix error but failed due to interface dependencies, 
			i.e you need a local org.apache.batik.ext.awt.image.codec.imageio.TIFFTranscoderImageIOWriteAdapter
			and then a local ImageWriter...
				   needs next release or build from source. 1.9.1 release is July 2017
				   see: https://mail-archives.apache.org/mod_mbox/xmlgraphics-batik-users/201708.mbox/%3CCY4PR04MB039071041456B1E485DCB893DDB40@CY4PR04MB0390.namprd04.prod.outlook.com%3E
				11:06:21.946 [http-nio-8080-exec-192] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.common.RifZipFile]:
createStudyExtract() ERROR
getMessage:          TranscoderException: null
Enclosed Exception:
Could not write TIFF file because no WriteAdapter is availble
getRootCauseMessage: TranscoderException: Could not write TIFF file because no WriteAdapter is availble
getThrowableCount:   2
getRootCauseStackTrace >>>
org.apache.batik.transcoder.TranscoderException: Could not write TIFF file because no WriteAdapter is availble
	at org.apache.batik.transcoder.image.TIFFTranscoder.writeImage(TIFFTranscoder.java:110)
	at org.apache.batik.transcoder.image.ImageTranscoder.transcode(ImageTranscoder.java:130)
 [wrapped] org.apache.batik.transcoder.TranscoderException: null
Enclosed Exception:
Could not write TIFF file because no WriteAdapter is availble
	at org.apache.batik.transcoder.image.ImageTranscoder.transcode(ImageTranscoder.java:132)
	at org.apache.batik.transcoder.XMLAbstractTranscoder.transcode(XMLAbstractTranscoder.java:142)
	at org.apache.batik.transcoder.SVGAbstractTranscoder.transcode(SVGAbstractTranscoder.java:156)
	at rifServices.dataStorageLayer.common.RifZipFile.addTIFFFile(RifZipFile.java:1130) 
				svgText); */
					break;
				case RIFGRAPHICS_JPEG:
				case RIFGRAPHICS_PNG:
					graphicsTranscode(outputType, input, output);
					break;
				case RIFGRAPHICS_EPS:
					EPSTranscode(input, output);
					break;
				case RIFGRAPHICS_PS:
					PSTranscode(input, output);
					break;
				default:
					throw new Exception("addGraphicsFile(): Unsupported output type: " + 
						outputType.toString());
			}
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), "Error in addGraphicsFile: " + svgURI + lineSeparator + 
				"; " + outputType.toString() + ": " + graphicFile,
				exception);
			throw exception;
		}
		finally {
			ostream.flush();	
			ostream.close();	
		}
	}
		
	public void addSvgFile(
			final File temporaryDirectory,
			final String dirName,
			final String tablePrefix,
			final String studyID,
			final int year,
			final String svgText) 
			throws Exception {
			
		String svgFileName=tablePrefix + studyID + "_" + year + ".svg";
		String svgDirName=temporaryDirectory.getAbsolutePath() + File.separator + dirName;
		String svgFile=svgDirName + File.separator + svgFileName;
		rifLogger.info(this.getClass(), "Adding SVG for report file: " + svgFile);
		OutputStream output = null;
		
		try {	
			File file = new File(svgFile);
			if (!file.exists()) {
				file.delete();
			}
			byte[] b=svgText.toString().getBytes();
			output = new FileOutputStream(svgFile);
			output.write(b, 0, b.length);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), "Error in addSvgFile: " + svgFile,
				exception);
			throw exception;
		}
		finally {		
			output.flush();	
			output.close();	
		}	
	}

	// Example from https://source.usc.edu/svn/opensha/tags/b2_0_0/org/jfree/chart/demo/PopulationChartDemo.java

     /**
      * Creates a stacked bar chart with default settings.  The chart object 
      * returned by this method uses a {@link CategoryPlot} instance as the
      * plot, with a {@link CategoryAxis} for the domain axis, a 
      * {@link NumberAxis} as the range axis, and a {@link StackedBarRenderer} 
      * as the renderer.
      *
      * @param title  the chart title (<code>null</code> permitted).
      * @param domainAxisLabel  the label for the category axis 
      *                         (<code>null</code> permitted).
      * @param rangeAxisLabel  the label for the value axis 
      *                        (<code>null</code> permitted).
      * @param dataset  the dataset for the chart (<code>null</code> permitted).
      * @param orientation  the orientation of the chart (horizontal or 
      *                     vertical) (<code>null</code> not permitted).
      * @param legend  a flag specifying whether or not a legend is required.
      * @param tooltips  configure chart to generate tool tips?
      *
      * @return A stacked bar chart.
      */
    public static JFreeChart rifCreateStackedBarChart(String title,
                                                   String domainAxisLabel,
                                                   String rangeAxisLabel,
                                                   KeyedValues2DDataset dataset,
                                                   PlotOrientation orientation,
                                                   boolean legend,
                                                   boolean tooltips,
												   boolean enablePostscript,
												   RifPopulationStackedBarRenderer renderer) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
 
        CategoryAxis categoryAxis = new CategoryAxis(domainAxisLabel);
		categoryAxis.setLowerMargin(0.0);
		categoryAxis.setUpperMargin(0.0);
		categoryAxis.setCategoryMargin(0.0);
        ValueAxis valueAxis = new NumberAxis(rangeAxisLabel);
	
        if (tooltips) { 
			renderer.setDefaultToolTipGenerator(
                     new StandardCategoryToolTipGenerator());
        }

		renderer.setItemMargin(0.0);
		if (enablePostscript) { // Disable color gradients - they break EPSTranscoder and PSTranscoder
			renderer.setBarPainter(new StandardBarPainter());
		}
		// Uses GradientBarPainter() by default
		
//      renderer.setDrawBarOutline(false);
//      renderer.setErrorIndicatorPaint(Color.black);
//      renderer.setIncludeBaseInRange(false);

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, 
                renderer);
				
		// crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
		
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);

        return chart;
    }
	
	private RifPopulationStackedBarRenderer getRifPopulationStackedBarRenderer() {
		RifPopulationStackedBarRenderer renderer = null;
		// From "Color Gradients/Tints and Shades" at http://encycolorpedia.com/7f82c9
		if (enablePostscript) {
			Color maleColors[] = {
				Color.decode("#b27073"),	// Slightly blacker
				Color.decode("#c97f82"),	// Pinkish red: rgb(201, 127, 130)
				Color.decode("#cf8d8f")		// Slightly whiter
			}; 		
			Color femaleColors[] = { 
				Color.decode("#7073b2"),	// Slightly blacker
				Color.decode("#7f82c9"),	// Blue: rgb(127,130,201)
				Color.decode("#8d8fcf")		// Slightly whiter
			}; 	
			renderer = new RifPopulationStackedBarRenderer(maleColors, femaleColors);
		}
		else { // Use gradients (does not work with EPSTranscoder/PSTranscoder)
			Color maleColors[] = {
				Color.decode("#c97f82")		// Pinkish red: rgb(201, 127, 130)
			}; 		
			Color femaleColors[] = { 
				Color.decode("#7f82c9")		// Blue: rgb(127,130,201)		
			};
			renderer = new RifPopulationStackedBarRenderer(maleColors, femaleColors);
		}

		return renderer;
	}
	
	public String getPopulationPyramid(Connection connection, String extractTable, 
		String denominatorTable, String studyDescription,
		String studyID, int year, boolean treeForm)
			throws Exception {

        KeyedValues2DDataset dataset = createDataset(connection, extractTable, denominatorTable, 
			year, treeForm);

		RifPopulationStackedBarRenderer renderer = getRifPopulationStackedBarRenderer();
		
        // create the chart... was createStackedHorizontalBarChart
		// Replaced with full code; use category axis to remove margins
        JFreeChart chart = rifCreateStackedBarChart(		// Was: ChartFactory.createStackedBarChart
                                                  "Denominator Population Pyramid for study " + 
														studyID + ": " + studyDescription,
                                                  "Age Group",     	// domain axis label
                                                  "Total Population (millions) " + year, // range axis label
                                                  dataset,         	// data
												  PlotOrientation.HORIZONTAL,
                                                  true,            	// include legend
                                                  true,            	// tooltips
												  enablePostscript,	// Setting to true also disables the gradients
												  renderer
                                              );
											  
//		LegendTitle legendTitle = chart.getLegend(0);
//		chart.removeLegend();
		
///		legendTitle.setPosition(RectangleEdge.RIGHT);
//		legendTitle.setMargin(0, 0, 0, 10);
//		legendTitle.setItemFont(new Font("Sans-serif", Font.PLAIN, 18));
//		chart.addSubtitle(legendTitle);

        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

		int width=886; // denominatorPyramidWidthPixels/4; fixed!
		
        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		svgGenerator.setSVGCanvasSize(new Dimension(width, 	// Width
								(int)(width/populationPyramidAspactRatio)	// Height. THIS A GUESS - MORE AGE SEX GROUPS
															// WILL CAUSE TROUBLE
									));
		
		chart.setBackgroundPaint(Color.white);
		ChartRenderingInfo chartInfo= new ChartRenderingInfo();
		BufferedImage image = chart.createBufferedImage(width, 	// Width
								(int)(width/populationPyramidAspactRatio),	// Height. THIS A GUESS - MORE AGE SEX GROUPS
																// WILL CAUSE TROUBLE
								chartInfo);	 			 		// Force jfreechart to plot so can get size!	
		PlotRenderingInfo plotInfo = chartInfo.getPlotInfo();
		Rectangle2D plotBounds = plotInfo.getPlotArea();
		Rectangle2D chartBounds = chartInfo.getChartArea();
		Rectangle bounds = new Rectangle(width, (int)(width/populationPyramidAspactRatio) /* Height */);
        // draw the chart in the SVG generator
		rifLogger.info(this.getClass(), "Bounds - SVG: " + bounds.toString() + 
			"; plot: " + plotBounds.toString() + 
			"; chart: " + chartBounds.toString());
									// SVG bounds: java.awt.geom.Rectangle2D$Double[x=0.0,y=26.0,w=881.0,h=570.0]
		chart.draw(svgGenerator, bounds);
		
		StringWriter writer = new StringWriter();
		svgGenerator.stream(writer, true /* use css */);	// Stream to a string
		String result=renderer.convertRGBtoHex(writer.toString());

		return result;
    }

    /**
	 * NEEDS TO BE MOVED TO THE dataStorageLayer!!!!!
	 */
	private KeyedValues2DDataset createDataset(Connection connection, 
		String extractTable, String denominatorTable,
		int year, boolean treeForm) 
			throws Exception {
				
		SQLGeneralQueryFormatter extractTableQueryFormatter = new SQLGeneralQueryFormatter();		
		
		ResultSet resultSet = null;
		// Convert age_group to textual age group strings
		extractTableQueryFormatter.addQueryLine(0, "WITH a AS (");		
		extractTableQueryFormatter.addQueryLine(0, "	SELECT a.offset AS id, a.low_age AS lower_limit, a.high_age AS upper_limit,");
		extractTableQueryFormatter.addQueryLine(0, "	       REPLACE(REPLACE(a.fieldname, '_', ' to '), 'PLUS', ' plus') AS fieldname");
		extractTableQueryFormatter.addQueryLine(0, "	  FROM rif40.rif40_age_groups a, rif40.rif40_tables b");
		extractTableQueryFormatter.addQueryLine(0, "	 WHERE a.age_group_id = b.age_group_id");
		extractTableQueryFormatter.addQueryLine(0, "	   AND b.table_name   = ?");
		extractTableQueryFormatter.addQueryLine(0, ")");				
		extractTableQueryFormatter.addQueryLine(0, "SELECT b.sex, b.age_group, a.fieldname, SUM(b.total_pop)/1000000 AS total_pop");
		extractTableQueryFormatter.addQueryLine(0, "  FROM rif_studies." + extractTable.toLowerCase() + " b");
		extractTableQueryFormatter.addQueryLine(0, "		LEFT OUTER JOIN a ON (a.id = b.age_group)");				
		extractTableQueryFormatter.addQueryLine(0, " WHERE b.year = ?");
		extractTableQueryFormatter.addQueryLine(0, "   AND b.study_or_comparison = 'S'");
		extractTableQueryFormatter.addQueryLine(0, " GROUP BY b.sex, b.age_group, a.fieldname");
		extractTableQueryFormatter.addQueryLine(0, " ORDER BY b.sex, b.age_group DESC");

		PreparedStatement statement = createPreparedStatement(connection, extractTableQueryFormatter);
		DefaultKeyedValues2DDataset data = null;
		try {	
			statement.setString(1, denominatorTable);
			statement.setInt(2, year);	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				int rowCount=0;
				data = new DefaultKeyedValues2DDataset();
				do {	
					rowCount++;
					String sex=null;
					String ageGroup=resultSet.getString(3);
					if (ageGroup == null) {
						resultSet.getString(2);
					}
					float totalPop=resultSet.getFloat(4);
					switch (resultSet.getInt(1)) {
						case 1: // Male
							sex="Male";
							if (treeForm) { // Otherwise stack to right
								totalPop=-totalPop; // Ugly
							}
							break;
						case 2: // female
							sex="Female";
							break;
						default:
							throw new Exception("createDataset() invalid sex code: " + 
								resultSet.getInt(1));
					}
					data.addValue( totalPop, sex, ageGroup);
						/* E.g. data.addValue( -6.0, "Male", "85+"); */
				} while (resultSet.next());
			}
			else {
				throw new Exception("No data found for " + extractTable + " year: " + year);
			}			
		}
		catch (Exception exception) {
			rifLogger.error(this.getClass(), "Error in SQL Statement: >>> " + 
				lineSeparator + extractTableQueryFormatter.generateQuery(),
				exception);
			throw exception;
		}
		finally {
			SQLQueryUtility.close(statement);
		}
		return data;
		
	}
}