// ************************************************************************
//
// GIT Header
//
// $Format:Git ID: (%h) %ci$
// $Id: 7ccec3471201c4da4d181af6faef06a362b29526 $
// Version hash: $Format:%H$
//
// Description:
//
// Rapid Enquiry Facility (RIF) - GeoJSON simplification code
//
// Copyright:
//
// The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
// that rapidly addresses epidemiological and public health questions using 
// routinely collected health and population data and generates standardised 
// rates and relative risks for any given health outcome, for specified age 
// and year ranges, for any given geographical area.
//
// Copyright 2014 Imperial College London, developed by the Small Area
// Health Statistics Unit. The work of the Small Area Health Statistics Unit 
// is funded by the Public Health England as part of the MRC-PHE Centre for 
// Environment and Health. Funding for this project has also been received 
// from the Centers for Disease Control and Prevention.  
//
// This file is part of the Rapid Inquiry Facility (RIF) project.
// RIF is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// RIF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
// to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
// Boston, MA 02110-1301 USA
//
// Author:
//
// Peter Hambly, SAHSU
	
const topojson = require('topojson'),
	  stderrHook = require('../lib/stderrHook'),
	  serverLog = require('../lib/serverLog'),
	  streamWriteFileWithCallback = require('../lib/streamWriteFileWithCallback');
		
/*
 * Function:	shapefileSimplifyGeoJSON()
 * Parameters:	shapefile (base for geojson etc), response, shapefileData object, 
 *				topojson options (may be undefined), callback (may be undefined)
 * Returns:		Nothing
 * Description:	Simplify geoJSOn to topoJSON optimised for zoomlevel 9,
				Run callback
 
 Using to postGIS simplify caluylator as a base:
 
	SELECT * FROM rif40_geo_pkg.rif40_zoom_levels();
psql:alter_scripts/v4_0_alter_5.sql:134: INFO:  [DEBUG1] rif40_zoom_levels(): [60001] latitude: 0
zoom_level | latitude |    tiles     | degrees_per_tile | m_x_per_pixel_est | m_x_per_pixel | m_y_per_pixel |   m_x    |   m_y    | simplify_tolerance |      scale
------------+----------+--------------+------------------+-------------------+---------------+---------------+----------+----------+--------------------+------------------
	  0 |        0 |            1 |              360 |            156412 |        155497 |               | 39807187 |          |               1.40 | 1 in 591,225,112
	  1 |        0 |            4 |              180 |             78206 |         77748 |               | 19903593 |          |               0.70 | 1 in 295,612,556
	  2 |        0 |           16 |               90 |             39103 |         39136 |         39070 | 10018754 | 10001966 |               0.35 | 1 in 148,800,745
	  3 |        0 |           64 |               45 |             19552 |         19568 |         19472 |  5009377 |  4984944 |               0.18 | 1 in 74,400,373
	  4 |        0 |          256 |             22.5 |              9776 |          9784 |          9723 |  2504689 |  2489167 |               0.09 | 1 in 37,200,186
	  5 |        0 |         1024 |            11.25 |              4888 |          4892 |          4860 |  1252344 |  1244120 |               0.04 | 1 in 18,600,093
	  6 |        0 |         4096 |            5.625 |              2444 |          2446 |          2430 |   626172 |   622000 |              0.022 | 1 in 9,300,047
	  7 |        0 |        16384 |            2.813 |              1222 |          1223 |          1215 |   313086 |   310993 |              0.011 | 1 in 4,650,023
	  8 |        0 |        65536 |            1.406 |               611 |           611 |           607 |   156543 |   155495 |             0.0055 | 1 in 2,325,012
	  9 |        0 |       262144 |            0.703 |               305 |           306 |           304 |    78272 |    77748 |             0.0027 | 1 in 1,162,506
	 10 |        0 |      1048576 |            0.352 |               153 |           153 |           152 |    39136 |    38874 |             0.0014 | 1 in 581,253
	 11 |        0 |      4194304 |            0.176 |                76 |            76 |            76 |    19568 |    19437 |            0.00069 | 1 in 290,626
	 12 |        0 |     16777216 |            0.088 |                38 |            38 |            38 |     9784 |     9718 |            0.00034 | 1 in 145,313
	 13 |        0 |     67108864 |            0.044 |                19 |            19 |            19 |     4892 |     4859 |            0.00017 | 1 in 72,657
	 14 |        0 |    268435456 |            0.022 |               9.5 |           9.6 |           9.5 |     2446 |     2430 |          0.0000858 | 1 in 36,328
	 15 |        0 |   1073741824 |            0.011 |               4.8 |           4.8 |           4.7 |     1223 |     1215 |          0.0000429 | 1 in 18,164
	 16 |        0 |   4294967296 |            0.005 |               2.4 |           2.4 |           2.4 |      611 |      607 |          0.0000215 | 1 in 9,082
	 17 |        0 |  17179869184 |            0.003 |              1.19 |          1.19 |          1.19 |      306 |      304 |          0.0000107 | 1 in 4,541
	 18 |        0 |  68719476736 |           0.0014 |              0.60 |          0.60 |          0.59 |      153 |      152 |          0.0000054 | 1 in 2,271
	 19 |        0 | 274877906944 |          0.00069 |              0.30 |          0.30 |          0.30 |       76 |       76 |          0.0000027 | 1 in 1,135
	 
For zoomlevel 9 the area at the equator is  78272 x 77748 = 6,085 square km and a pixel is 306 x 304 = 0.093 square km
In steradians = (0.093 / (510,072,000 * 12.56637) [area of earth] = 1.4512882642054046732729181896167e-11 steradians
	 
For zoomlevel 11 the area at the equator is  19568 x 19437 = 380.3 square km and a pixel is 76 x 76 = 0.005776 square km
In steradians = (0.005776 / (510,072,000 * 12.56637) [area of earth] = 9.011266999968199e-13 steradians	
 */
var shapefileSimplifyGeoJSON = function shapefileSimplifyGeoJSON(shapefile, response, shapefileData, topojson_options, callback) {
	 const streamWriteFileWithCallback = require('../lib/streamWriteFileWithCallback');
		  
	scopeChecker(__file, __line, {
		shapefile: shapefile,
		topojsonFileName: shapefileData["topojsonFileName"],
		response: response,
		shapefileData: shapefileData,
		geojson: shapefile.geojson,
		features: shapefile.geojson.features,
		file_no: response.file_list[shapefileData["shapefile_no"]-1]
	} /* Manadatory */,
	{
		callback: callback
	} /* Optional */);	
	
	var records;
	if (shapefile.geojson.features) {
		records=shapefile.geojson.features.length;
	}	
	
	shapefile.topojson=toTopoJSON(shapefile.geojson, topojson_options, response);
	
// This need to be replaced with write record by record and then do the callback here
// We can then also remove the geojson

	if (response.file_list[shapefileData["shapefile_no"]-1].topojson) {
		response.file_list[shapefileData["shapefile_no"]-1].topojson_length=JSON.stringify(shapefile.topojson).length;
			shapefile.geojson.features=undefined;
	}

// Write topoJSON file; do NOT delete it				
	streamWriteFileWithCallback.streamWriteFileWithCallback(shapefileData["topojsonFileName"], 
		JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].topojson), 
		serverLog, shapefileData["uuidV1"], shapefileData["req"], response, records, 
		false /* do not delete data (by undefining) at stream end */, callback);		
} // End of shapefileSimplifyGeoJSON()

/*
 * Function: 	getQuantization() 
 * Parameters:  Level
 * Returns: 	Quantization
 * Description: Set quantization (the maximum number of differentiable values along each dimension) by zoomLevel
 *
 * Zoomlevel		Quantization
 * ---------		------------
 *
 * <=6				1,500
 * 7				3,000
 * 8				5,000
 * 9				10,000
 * 10				100,000
 * 11				1,000,0000
 * 
 * Modified in the light of experience to increase quantisations: e.g. 10**6 for US zoomlevel 11.
 */
var getQuantization = function getQuantization(lvl) {
			
	if (lvl <= 6) {
		return 1500;
	} 
	else if (lvl == 7) {
		return 3000;
	} 
	else if (lvl == 8) {
		return 5000;
	} 
	else if (lvl == 9) {
		return 1e4;
	} 
	else if (lvl == 10) {
		return 1e5;
	} 
	else {
		return 1e6; // Default
	}
};

/*
 * Function: 	toTopoJSON() 
 * Parameters:  geoJOSN, topoJSON convertor options, internal response object
 * Returns: 	TopoJSON
 * Description: Convert geoJSON to topoJSON
 */
var toTopoJSON = function toTopoJSON(geojson, topojson_options, response) {
	const topojson = require('topojson'),
		  stderrHook = require('../lib/stderrHook'),
		  serverLog = require('../lib/serverLog');

	 scopeChecker(__file, __line, {
		response: response,
		geojson: geojson
	});

// Add stderr hook to capture debug output from topoJSON	
	var stderr = stderrHook.stderrHook(function stderrHookOutput(output, obj) { 
		output.str += obj.str;
	});
	
// Default geo2TopoJSON options (see topology Node.js module)
/* For US data:

bounds: -179.148909 -14.548699000000001 179.77847 71.36516200000001 (spherical)
pre-quantization: 39.9m (0.000359°) 9.55m (0.0000859°)
topology: 1579 arcs, 247759 points
*/
	if (!topojson_options) {
		topojson_options = {
			verbose:      true,
			quantization: 1e6
		}; 		
		response.message+="\nDefault topoJSON options: " + JSON.stringify(topojson_options, null, 4);
	}
	else if (!topojson_options.simplify && topojson_options.quantization == 1e6) { // For zoomlevel 11
		topojson_options.simplify=9.011e-13; // For zoomlevel 11
		response.message+="\nTopoJSON options (simplify defaulted): " + JSON.stringify(topojson_options, null, 4);
	}
	else {
		response.message+="\nTopoJSON options: " + JSON.stringify(topojson_options, null, 4);
	}
	
		// Re-route topoJSON stderr to stderr.str
	stderr.disable();

	var convertedTopojson = topojson.topology({   // Convert geoJSON to topoJSON
		collection: geojson
		}, topojson_options);				
	stderr.enable(); 				   // Re-enable stderr
	
	response.message+="\nConvert to topojson:\n" + stderr.str();  // Get stderr as a string	
	stderr.clean();						// Clean down stderr string
	stderr.restore();                   // Restore normal stderr functionality 	
	
	return convertedTopojson;
} // end of toTopoJSON()

module.exports.toTopoJSON = toTopoJSON;
module.exports.shapefileSimplifyGeoJSON = shapefileSimplifyGeoJSON;
module.exports.getQuantization = getQuantization;

// Eof
		