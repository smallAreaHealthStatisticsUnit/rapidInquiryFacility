package rifServices.dataStorageLayer.common;

import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.AbstractStudy;
import rifGenericLibrary.util.RIFLogger;
import rifGenericLibrary.dataStorageLayer.DatabaseType;

import rifGenericLibrary.dataStorageLayer.SQLGeneralQueryFormatter;
import rifGenericLibrary.dataStorageLayer.common.SQLQueryUtility;

import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.svggen.SVGGraphics2D;
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
import org.jfree.chart.plot.PlotOrientation;

import org.w3c.dom.DOMImplementation; 
import org.w3c.dom.Document; 
import org.w3c.dom.Element; 

import java.io.*;
import java.sql.*;
import org.json.*;
import java.lang.*;
import java.awt.Rectangle;

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
	private static float jpegQuality=new Float(.8);
		
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
				transCoder = new RifTIFFTranscoder();
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
		final RIFGraphicsOutputType outputType,
		final String svgText) 
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
		if (!file.exists()) {
			file.delete();
		}
		OutputStream ostream = new FileOutputStream(graphicFile);
        TranscoderOutput output = new TranscoderOutput(ostream);
		try {
			switch (outputType) {
				case RIFGRAPHICS_TIFF:  /* Fixed in Batik source: 21/8/2017, disabled, 
				   RifTIFFTranscoder.java created to try to fix error but failed due to interface dependencies, 
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
	
	public String getPopulationPyramid(Connection connection, String extractTable, 
		String studyID, int year)
			throws Exception {

        KeyedValues2DDataset dataset = createDataset(connection, extractTable, year);

        // create the chart... was createStackedHorizontalBarChart

        JFreeChart chart = ChartFactory.createStackedAreaChart(
                                                  "Population Pyramid",
                                                  "Age Group",     // domain axis label
                                                  "Total Population (millions) " + year, // range axis label
                                                  dataset,         // data
												  PlotOrientation.HORIZONTAL,
                                                  true,            // include legend
                                                  true,            // tooltips
                                                  false            // urls
                                              );

        CategoryPlot plot = chart.getCategoryPlot();

        // add the chart to a panel...
  //      ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
 //       setContentPane(chartPanel);
        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        // draw the chart in the SVG generator
		Rectangle bounds=new Rectangle(500, 270);
        chart.draw(svgGenerator, bounds);
		
		StringWriter writer = new StringWriter();
		svgGenerator.stream(writer);
		
		return writer.toString();
    }

    /**
	 * 
	 */
	private KeyedValues2DDataset createDataset(Connection connection, String extractTable, 
		int year) 
			throws Exception {
				
		SQLGeneralQueryFormatter extractTableQueryFormatter = new SQLGeneralQueryFormatter();		
		
		ResultSet resultSet = null;
		// Convert age_group to textual age group strings 
		extractTableQueryFormatter.addQueryLine(0, "SELECT sex, age_group, SUM(total_pop)/1000000 AS total_pop");
		extractTableQueryFormatter.addQueryLine(0, "  FROM rif_studies." + extractTable.toLowerCase());
		extractTableQueryFormatter.addQueryLine(0, " WHERE year = ?");
		extractTableQueryFormatter.addQueryLine(0, "   AND study_or_comparison = 'S'");
		extractTableQueryFormatter.addQueryLine(0, " GROUP BY sex, age_group");
		extractTableQueryFormatter.addQueryLine(0, " ORDER BY sex, age_group");

		PreparedStatement statement = createPreparedStatement(connection, extractTableQueryFormatter);
		DefaultKeyedValues2DDataset data = null;
		try {	
			statement.setInt(1, year);	
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				int rowCount=0;
				data = new DefaultKeyedValues2DDataset();
				do {	
					rowCount++;
					String sex=null;
					String ageGroup=resultSet.getString(2);
					float totalPop=resultSet.getFloat(3);
					switch (resultSet.getInt(1)) {
						case 1: // Male
							sex="Male";
							totalPop=-totalPop; // Ugly
							break;
						case 2: // female
							sex="Remale";
							break;
						default:
							throw new Exception("createDataset() invalid sex code: " + 
								resultSet.getInt(1));
					}
					data.addValue( totalPop, sex, ageGroup);
		/*
		data.addValue( -6.0, "Male", "70+");
		data.addValue( -8.0, "Male", "60-69");
		data.addValue(-11.0, "Male", "50-59");
		data.addValue(-13.0, "Male", "40-49");
		data.addValue(-14.0, "Male", "30-39");
		data.addValue(-15.0, "Male", "20-29");
		data.addValue(-19.0, "Male", "10-19");
		data.addValue(-21.0, "Male", "0-9");
		data.addValue(10.0, "Female", "70+");
		data.addValue(12.0, "Female", "60-69");
		data.addValue(13.0, "Female", "50-59");
		data.addValue(14.0, "Female", "40-49");
		data.addValue(15.0, "Female", "30-39");
		data.addValue(17.0, "Female", "20-29");
		data.addValue(19.0, "Female", "10-19");
		data.addValue(20.0, "Female", "0-9"); */
		
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
	
/*
 * JS D3 code for the population pyramid: rifd-view-d3pyramid.js
 
                    scope.renderBase = function () {

                        // General dimensions of canvas
                        var margins = {top: 60, right: 60, bottom: 80, left: 80};
                        var chartWidth = scope.width - margins.left - margins.right;
                        var chartHeight = scope.height - margins.top - margins.bottom;

                        var numberOfXAxisTicks = 10;

                        var xScale = d3.scaleLinear()
                                .range([0, chartWidth]);
                        var yScale = d3.scaleBand()
                                .rangeRound([chartHeight, 0], .6);

                        d3.select("#poppyramid").remove();

                        // Create the main display area 
                        var mainImageArea = d3.select(element[0]).append("svg")
                                .attr("width", chartWidth + margins.left + margins.right)
                                .attr("height", chartHeight + margins.top + margins.bottom)
                                .attr("id", "poppyramid")
                                .append("g")
                                .attr("transform", "translate(" + margins.left + "," + margins.top + ")");

                        var maximumPopulation = d3.max(scope.data, function (d) {
                            return(d.males + d.females);
                        });

                        xScale.domain([0, maximumPopulation]);
                        if (angular.isArray(scope.data)) {
                            yScale.domain(scope.data.map(function (d) {
                                return d.population_label;
                            }));
                        }

                        var xTickPositions = xScale.ticks(numberOfXAxisTicks);

                        mainImageArea.selectAll("g.maleBar")
                                .data(scope.data)
                                .enter().append("g")
                                .append("rect")
                                .attr("class", "maleBar")
                                .attr("x", 0)
                                .attr("y", function (d) {
                                    return(yScale(d.population_label));
                                })
                                .attr("width", function (d, i) {
                                    return(xScale(d.males));
                                })
                                .attr("height", yScale.bandwidth());

                        mainImageArea.selectAll("g.femaleBar")
                                .data(scope.data)
                                .enter().append("g")
                                .append("rect")
                                .attr("class", "femaleBar")
                                .attr("x", function (d) {
                                    return(xScale(d.males));
                                })
                                .attr("y", function (d) {
                                    return(yScale(d.population_label));
                                })
                                .attr("width", function (d, i) {
                                    return(xScale(d.females));
                                })
                                .attr("height", yScale.bandwidth());

                        mainImageArea.selectAll("g.totalPopulationBar")
                                .data(scope.data)
                                .enter().append("g")
                                .append("rect")
                                .attr("class", "totalPopulationBar")
                                .attr("x", function (d) {
                                    return(0);
                                })
                                .attr("y", function (d) {
                                    return(yScale(d.population_label));
                                })
                                .attr("width", function (d, i) {
                                    return(xScale(d.males + d.females));
                                })
                                .attr("height", yScale.bandwidth());


                        // Add the X Axis
                        var myFormatter = function (d) {
                            return (d / 1e6 >= 1) ? (d / 1e6 + "M") :
                                    (d / 1e3 >= 1) ? (d / 1e3 + "K") : d;
                        };

                        var xAxis = d3.axisBottom()
                                .scale(xScale)
                                .ticks(5)
                                .tickFormat(function (d) {
                                    return myFormatter(+d);
                                });

                        function customXAxis(g) {
                            g.call(xAxis);
                            g.selectAll(".tick text").style("font-size", function (d) {
                                return Math.min((chartWidth / 20), 10);
                            });
                        }

                        mainImageArea.append("g")
                                .attr("transform", "translate(0," + chartHeight + ")")
                                .call(customXAxis);

                        // Add the Y Axis
                        var yAxis = d3.axisLeft()
                                .scale(yScale);

                        function customYAxis(g) {
                            g.call(yAxis);
                            g.selectAll(".tick text").style("font-size", function (d) {
                                return Math.min((chartHeight / 25), 10);
                            });
                        }

                        mainImageArea.append("g")
                                .call(customYAxis);

                        // Add drop lines
                        mainImageArea.selectAll("g.xTickMarkProjectionLines")
                                .data(xTickPositions.slice(1, xTickPositions.length))
                                .enter().append("g")
                                .append("line")
                                .attr("class", "xAxisDashedLines")
                                .attr("x1", function (d) {
                                    return(xScale(d));
                                })
                                .attr("y1", function (d) {
                                    return(0);
                                })
                                .attr("x2", function (d, i) {
                                    return(xScale(d));
                                })
                                .attr("y2", function (d) {
                                    return(chartHeight);
                                });

                        //Add legend and axis labels
                        mainImageArea.append("rect")
                                .attr("width", yScale.bandwidth())
                                .attr("height", yScale.bandwidth())
                                .style("fill", "#c97f82")
                                .attr("transform", "translate(0, " + (-yScale.bandwidth() - 10) + ")");
                        mainImageArea.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((chartWidth / 15), 10, (chartHeight / 15));
                                })
                                .attr("transform", "translate(" + (yScale.bandwidth() + 2) + "," + 
									((-yScale.bandwidth() / 2) - 10) + ")")
                                .style("text-anchor", "start")
                                .text("Male");
                        mainImageArea.append("rect")
                                .attr("width", yScale.bandwidth())
                                .attr("height", yScale.bandwidth())
                                .style("fill", "#7f82c9")
                                .attr("transform", "translate(80, " + (-yScale.bandwidth() - 10) + ")");
                        mainImageArea.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((chartWidth / 15), 10, (chartHeight / 15));
                                })
                                .attr("transform", "translate(" + (yScale.bandwidth() + 82) + "," + 
									((-yScale.bandwidth() / 2) - 10) + ")")
                                .style("text-anchor", "start")
                                .text("Female");
                        mainImageArea.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((chartWidth / 15), 15);
                                })
                                .attr("transform", "translate(" + chartWidth / 2 + "," + 
									(chartHeight + 35) + ")")
                                .style("text-anchor", "middle")
                                .text("TOTAL POPULATION");
                        mainImageArea.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((chartHeight / 15), 15);
                                })
                                .attr("text-anchor", "middle")
                                .attr("transform", "rotate(-90)")
                                .attr("y", 15 - margins.top)
                                .attr("x", 0 - (chartHeight / 2))
                                .text("AGE GROUP");
                    };
 */ 	
 
	public String getSvgPopulationPyramid(
		final String studyID,
		final int year,
		final String svgCss) 
		throws Exception {
		
		String svgText=
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator +
"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"" + 
" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11-flat.dtd\">" + lineSeparator +
"  <svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"598\" height=\"430.70001220703125\" id=\"poppyramid\">" + lineSeparator +
"    <style>" + lineSeparator +
			svgCss +
"    </style>" + lineSeparator +
"	<g transform=\"translate(80,60)\">" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"275\" width=\"35.94974490491911\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"262\" width=\"35.94974490491911\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"249\" width=\"35.94974490491911\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"236\" width=\"35.94974490491911\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"223\" width=\"35.94974490491911\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"210\" width=\"192.48965854215083\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"197\" width=\"184.7389994121776\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"184\" width=\"177.24052353397835\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"171\" width=\"193.91188120530876\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"158\" width=\"228.36329408730032\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"145\" width=\"234.03514533102833\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"132\" width=\"209.98685956501143\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"119\" width=\"189.8292388319944\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"106\" width=\"212.98806745324086\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"93\" width=\"183.20204472906843\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"80\" width=\"151.33380623687012\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"67\" width=\"142.44605055272666\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"54\" width=\"138.29979438617204\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"41\" width=\"124.97043278114404\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"28\" width=\"90.5917213771414\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"15\" width=\"59.419826728805525\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"maleBar\" x=\"0\" y=\"2\" width=\"37.81840008135264\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"35.94974490491911\" y=\"275\" width=\"34.200365590811124\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"35.94974490491911\" y=\"262\" width=\"34.200365590811124\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"35.94974490491911\" y=\"249\" width=\"34.200365590811124\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"35.94974490491911\" y=\"236\" width=\"34.200365590811124\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"35.94974490491911\" y=\"223\" width=\"34.200365590811124\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"192.48965854215083\" y=\"210\" width=\"183.12025556633094\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"184.7389994121776\" y=\"197\" width=\"173.2362624416208\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"177.24052353397835\" y=\"184\" width=\"166.64541907768927\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"193.91188120530876\" y=\"171\" width=\"178.89334619763235\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"228.36329408730032\" y=\"158\" width=\"208.48398369970957\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"234.03514533102833\" y=\"145\" width=\"223.96485466897164\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"209.98685956501143\" y=\"132\" width=\"205.95306349722088\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"189.8292388319944\" y=\"119\" width=\"192.31699253192718\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"212.98806745324086\" y=\"106\" width=\"214.17287435234127\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"183.20204472906843\" y=\"93\" width=\"183.77116098645033\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"151.33380623687012\" y=\"80\" width=\"155.0415816143042\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"142.44605055272666\" y=\"67\" width=\"150.7715057430497\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"138.29979438617204\" y=\"54\" width=\"156.47516388339787\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"124.97043278114404\" y=\"41\" width=\"157.35098950104543\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"90.5917213771414\" y=\"28\" width=\"131.2341194941255\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"59.419826728805525\" y=\"15\" width=\"104.775325348539\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"femaleBar\" x=\"37.81840008135264\" y=\"2\" width=\"103.85860514952267\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"275\" width=\"70.15011049573022\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"262\" width=\"70.15011049573022\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"249\" width=\"70.15011049573022\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"236\" width=\"70.15011049573022\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"223\" width=\"70.15011049573022\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"210\" width=\"375.6099141084818\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"197\" width=\"357.97526185379843\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"184\" width=\"343.88594261166764\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"171\" width=\"372.8052274029411\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"158\" width=\"436.8472777870099\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"145\" width=\"458\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"132\" width=\"415.9399230622323\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"119\" width=\"382.14623136392163\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"106\" width=\"427.1609418055821\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"93\" width=\"366.97320571551876\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"80\" width=\"306.37538785117425\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"67\" width=\"293.2175562957764\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"54\" width=\"294.7749582695699\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"41\" width=\"282.3214222821895\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"28\" width=\"221.8258408712669\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"15\" width=\"164.19515207734452\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<rect class=\"totalPopulationBar\" x=\"0\" y=\"2\" width=\"141.67700523087532\" height=\"13\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g transform=\"translate(0,290.70001220703125)\" fill=\"none\" text-anchor=\"middle\">" + lineSeparator +
"			<path class=\"domain\" stroke=\"#000\" d=\"M0.5,6V0.5H458.5V6\"/>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,0)\">" + lineSeparator +
"				<line stroke=\"#000\" y2=\"6\" x1=\"0.5\" x2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" y=\"9\" x=\"0.5\" dy=\"0.71em\" style=\"\">0</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(141.99507419707678,0)\">" + lineSeparator +
"				<line stroke=\"#000\" y2=\"6\" x1=\"0.5\" x2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" y=\"9\" x=\"0.5\" dy=\"0.71em\" style=\"\">500K</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(283.99014839415355,0)\">" + lineSeparator +
"				<line stroke=\"#000\" y2=\"6\" x1=\"0.5\" x2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" y=\"9\" x=\"0.5\" dy=\"0.71em\" style=\"\">1M</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(425.9852225912303,0)\">" + lineSeparator +
"				<line stroke=\"#000\" y2=\"6\" x1=\"0.5\" x2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" y=\"9\" x=\"0.5\" dy=\"0.71em\" style=\"\">1.5M</text>" + lineSeparator +
"			</g>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g fill=\"none\" text-anchor=\"end\">" + lineSeparator +
"			<path class=\"domain\" stroke=\"#000\" d=\"M-6,291.20001220703125H0.5V0.5H-6\"/>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,282)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">0</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,269)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">1</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,256)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">2</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,243)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">3</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,230)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">4</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,217)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">5_9</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,204)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">10_14</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,191)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">15_19</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,178)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">20_24</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,165)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">25_29</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,152)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">30_34</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,139)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">35_39</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,126)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">40_44</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,113)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">45_49</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,100)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">50_54</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,87)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">55_59</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,74)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">60_64</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,61)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">65_69</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,48)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">70_74</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,35)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">75_79</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,22)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">80_84</text>" + lineSeparator +
"			</g>" + lineSeparator +
"			<g class=\"tick\" opacity=\"1\" transform=\"translate(0,9)\">" + lineSeparator +
"				<line stroke=\"#000\" x2=\"-6\" y1=\"0.5\" y2=\"0.5\"/>" + lineSeparator +
"				<text fill=\"#000\" x=\"-9\" y=\"0.5\" dy=\"0.32em\" style=\"\">85PLUS</text>" + lineSeparator +
"			</g>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<line class=\"xAxisDashedLines\" x1=\"56.7980296788307\" y1=\"0\" x2=\"56.7980296788307\" y2=\"290.70001220703125\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<line class=\"xAxisDashedLines\" x1=\"113.5960593576614\" y1=\"0\" x2=\"113.5960593576614\" y2=\"290.70001220703125\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<line class=\"xAxisDashedLines\" x1=\"170.39408903649212\" y1=\"0\" x2=\"170.39408903649212\" y2=\"290.70001220703125\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<line class=\"xAxisDashedLines\" x1=\"227.1921187153228\" y1=\"0\" x2=\"227.1921187153228\" y2=\"290.70001220703125\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<line class=\"xAxisDashedLines\" x1=\"283.99014839415355\" y1=\"0\" x2=\"283.99014839415355\" y2=\"290.70001220703125\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<line class=\"xAxisDashedLines\" x1=\"340.78817807298424\" y1=\"0\" x2=\"340.78817807298424\" y2=\"290.70001220703125\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<line class=\"xAxisDashedLines\" x1=\"397.5862077518149\" y1=\"0\" x2=\"397.5862077518149\" y2=\"290.70001220703125\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<g>" + lineSeparator +
"			<line class=\"xAxisDashedLines\" x1=\"454.3842374306456\" y1=\"0\" x2=\"454.3842374306456\" y2=\"290.70001220703125\"/>" + lineSeparator +
"		</g>" + lineSeparator +
"		<rect width=\"13\" height=\"13\" class=\"maleRect\" transform=\"translate(0, -23)\"/>" + lineSeparator +
"		<text style=\"text-anchor: start;\" transform=\"translate(15,-16.5)\">Male</text>" + lineSeparator +
"		<rect width=\"13\" height=\"13\" class=\"femaleRect\" transform=\"translate(80, -23)\"/>" + lineSeparator +
"		<text style=\"text-anchor: start;\" transform=\"translate(95,-16.5)\">Female</text>" + lineSeparator +
"		<text style=\"text-anchor: middle;\" transform=\"translate(229,325.70001220703125)\">TOTAL POPULATION: " + year + "</text>" + lineSeparator +
"		<text style=\"text-anchor: middle;\" transform=\"rotate(-90)\" y=\"-45\" x=\"-145.35000610351562\">AGE GROUP</text>" + lineSeparator +
"	</g>" + lineSeparator +
"</svg>";
		return svgText;
	}
}
