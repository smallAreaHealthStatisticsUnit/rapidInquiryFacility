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
	  clone = require('clone'),
	  sizeof = require('object-sizeof'),
	  wellknown = require('wellknown'),
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
	
	var areaID=shapefileData["areaID"];
	var areaName=shapefileData["areaName"];
	var dbf_fields=shapefileData["dbf_fields"];
	
	function toTopoJSONCallback(err) {
		
		if (err) {
			serverLog.serverError2(__file, __line, "toTopoJSONCallback", 
				"WARNING: Unable to create topoJSON", req, err);
		}	
									
		response.file_list[shapefileData["shapefile_no"]-1].total_topojson_length=0;
		if (shapefile.topojson) {
			for (var i=0; i<shapefile.topojson.length; i++) { // total_topojson_length
				response.file_list[shapefileData["shapefile_no"]-1].total_topojson_length+=(shapefile.topojson[i].topojson_length || 0);
			}
		}
		
	// This need to be replaced with write record by record and then do the callback here
	// We can then also remove the geojson

	// Write topoJSON file; do NOT delete it
		if (response.file_list[shapefileData["shapefile_no"]-1].topojson && response.file_list[shapefileData["shapefile_no"]-1].topojson[0]) {	
			shapefile.geojson.features=undefined;			
			streamWriteFileWithCallback.streamWriteFileWithCallback(shapefileData["topojsonFileName"], 
				JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].topojson[0].topojson), 
				serverLog, shapefileData["uuidV1"], shapefileData["req"], response, records, 
				false /* do not delete data (by undefining) at stream end */, callback);
		}
		else {
			throw new Error('response.file_list[shapefileData["shapefile_no"]-1].topojson[0] is undefined');
		}		
	} // End of toTopoJSONCallback()
	
	if (areaName && areaID) {
		shapefile.topojson = toTopoJSON(shapefile.geojson, topojson_options, response, shapefileData["key"],
			areaName, areaID, dbf_fields, toTopoJSONCallback);
	}
	else {
		response.message+="\nNo areaID/areaName fields set for file: " + shapefileData["topojsonFileBaseName"] + "\nAreaID: " +
			(areaID || "no areaID") + "; areaName: " + (areaName || "no areaName");
		shapefile.topojson = toTopoJSON(shapefile.geojson, topojson_options, response, shapefileData["key"],
			undefined /* areaName  */, undefined /* areaID */, dbf_fields, toTopoJSONCallback);
	}

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
 * Parameters:  geoJOSN, topoJSON convertor options, internal response object, fileName, area name, area ID, dbf fields, callback
 * Returns: 	Array of topoJSON objects {
 *	 				topojson,
 *					topojson_length,
 *					topojson_runtime,
 * 					topojson_options,
 *					topojson_stderr,
 *					zoomlevel
 *				}
 * Description: Convert geoJSON to topoJSON
 */
var toTopoJSON = function toTopoJSON(geojson, topojson_options, response, fileName, areaName, areaID, dbf_fields, callback) {
	const topojson = require('topojson'),
		  stderrHook = require('../lib/stderrHook'),
		  serverLog = require('../lib/serverLog');
		  
	 scopeChecker(__file, __line, {
		response: response,
		geojson: geojson,
		sizeof: sizeof,
		callback: callback,
		wellknown: wellknown
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
//
// Retain properties gid
//				
	var myPropertyTransform;
	
	if (!topojson_options) {
		topojson_options = {
			verbose:      true
		};
		topojson_options["pre-quantization"]=1e6;
		topojson_options["post-quantization"]=1e6;
	
		response.message+="\nZoomlevel: " + response.fields["max_zoomlevel"] + "; default topoJSON options: " + JSON.stringify(topojson_options, null, 4);
	}
	else if (!topojson_options.simplify && topojson_options["pre-quantization"] && topojson_options["pre-quantization"] == 1e6) { // For zoomlevel 11
		topojson_options.simplify=9.011e-13; // For zoomlevel 11
		response.message+="\nZoomlevel: " + response.fields["max_zoomlevel"] + " topoJSON options (simplify defaulted): " + JSON.stringify(topojson_options, null, 4);
	}
	else {
		response.message+="\nZoomlevel: " + response.fields["max_zoomlevel"] + " topoJSON options: " + JSON.stringify(topojson_options, null, 4);
	}
	if (areaName && areaID) {
		myPropertyTransform=function myPropertyTransform(feature) {
			var propertyTransform = { 
				"areaName": feature.properties[areaName], 	
				"areaID": 	feature.properties[areaID] /*, 
				"id": 		feature.properties.id */
			};
			if (dbf_fields) {
				for (var i=0; i<dbf_fields.length; i++) {
					propertyTransform[dbf_fields[i]]=feature.properties[dbf_fields[i]];
				}
			}
			return propertyTransform;
		}	
		topojson_options["property-transform"]=myPropertyTransform;
		response.message+="; property-transform enabled";
	}
	else if (dbf_fields) {
		myPropertyTransform=function myPropertyTransform(feature) {
			var propertyTransform = {};
			for (var i=0; i<dbf_fields.length; i++) {
				propertyTransform[dbf_fields[i]]=feature.properties[dbf_fields[i]];
			}
			return propertyTransform;
		}	
		topojson_options["property-transform"]=myPropertyTransform;
		response.message+="; property-transform enabled (dbf_fields only)";
	}
	else {
		response.message+="; property-transform disabled";
	}
		
	stderr.disable();	// Re-route topoJSON stderr to stderr.str

	var lstart = new Date().getTime();		
	var convertedTopojson = []; 
	convertedTopojson[0] = {
		topojson: undefined,
		wkt: [],
		geojson_length: undefined,
		topojson_length: undefined,
		topojson_runtime: undefined,
		topojson_options: topojson_options,
		topojson_stderr: undefined,
		topojson_arcs: undefined,
		topojson_points: undefined,
		zoomlevel: response.fields["max_zoomlevel"]
	};
	
	convertedTopojson[0].geojson_length=sizeof(geojson);
	convertedTopojson[0].topojson=topojson.topology({   // Convert geoJSON to topoJSON
			collection: geojson
			}, topojson_options);
	var end = new Date().getTime();
	convertedTopojson[0].topojson_runtime=(end - lstart)/1000; // in S	
	convertedTopojson[0].topojson_length=sizeof(convertedTopojson[0].topojson);	
	stderr.enable(); 				   // Re-enable stderr
	convertedTopojson[0].topojson_stderr=stderr.str();
//	if (convertedTopojson[0].topojson.transform) {
//		console.error("pre-quantization: " + convertedTopojson[0].topojson.transform.scale.map(function(k) { return system.formatDistance(k); }).join(" ") + "\n");
//	}
	convertedTopojson[0].topojson_arcs=convertedTopojson[0].topojson.arcs.length;
	convertedTopojson[0].topojson_points=convertedTopojson[0].topojson.arcs.reduce(function(p, v) { return p + v.length; }, 0);
	
	response.message+="\nCreated topojson for zoomlevel: " + (convertedTopojson[0].zoomlevel || "N/A") + "; size: " + convertedTopojson[0].topojson_length + 
		"; took: " + convertedTopojson[0].topojson_runtime + " S;  diagnostics\n" + convertedTopojson[0].topojson_stderr;  // Get stderr as a string	
	
	stderr.clean();						// Clean down stderr string
	stderr.restore();                   // Restore normal stderr functionality 	
				
	var msg;
	if (convertedTopojson[0].zoomlevel) {
		msg=fileName + ": simplified topojson for zoomlevel 11";
		response.message+="\n" + msg + " took: " + convertedTopojson[0].topojson_runtime + "S";
		addStatus(__file, __line, response, msg, 
			200 /* HTTP OK */, serverLog, undefined /* req */,
			function zoomlevel11Callback(err) {
				if (err) {
					serverLog.serverLog2(__file, __line, "zoomlevel11Callback", "WARNING: Unable to write status file", req, err);
					callback(err);
				}				
				else {
					toTopoJSONZoomlevels(geojson, topojson_options, response, convertedTopojson, stderr, serverLog, fileName, myPropertyTransform, callback);
				}
			});  // Add end of shapefile read status
	
	}
	else {
		msg=fileName + ": simplified topojson for unknown zoomlevel";
		response.message+="\n" + msg + " took: " + convertedTopojson[0].topojson_runtime + "S";
		addStatus(__file, __line, response, msg, 
			200 /* HTTP OK */, serverLog, undefined /* req */,
			function zoomlevel11Callback2(err) {
				if (err) {
					serverLog.serverLog2(__file, __line, "zoomlevel11Callback2", "WARNING: Unable to write status file", req, err);
				}				
				callback(err);
			});  // Add end of shapefile read status
	}

	return convertedTopojson;
} // end of toTopoJSON()

/*
 * Function: 	toTopoJSONZoomlevels() 
 * Parameters:  geoJOSN, topoJSON convertor options, internal response object, convertedTopojson object, 
 *				stderr object, serverLog object, fileName, myPropertyTransform function, toTopoJSON() callback
 * Returns: 	Nothing
 * Description: Convert geoJSON to topoJSON for each zoomlevel 10=>6
 */
var toTopoJSONZoomlevels = function toTopoJSONZoomlevels(geojson, topojson_options, response, convertedTopojson, stderr, serverLog, 
	fileName, myPropertyTransform, topoJSONcallback) {
		  
	 scopeChecker(__file, __line, {
		response: response,
		clone: clone,
		sizeof: sizeof,
		wellknown: wellknown,
		callback: topoJSONcallback
	},
	{
		callback: myPropertyTransform
	} /* Optional */);
	
	var nTopojson_options=clone(topojson_options);
	nTopojson_options.simplify=undefined;	
	if (response.fields && response.fields["simplificationFactor"]) { // simplificationFactor field: Topojson --simplify-proportion option!
		nTopojson_options["retain-proportion"]=response.fields["simplificationFactor"]; 
		response.message+="\nZoomlevel: " + (convertedTopojson[0].zoomlevel-1) + "; using simplification factor: " + 
			nTopojson_options["retain-proportion"];
	}
	else {
		nTopojson_options["retain-proportion"]=0.75; 
		response.message+="\nZoomlevel: " + (convertedTopojson[0].zoomlevel-1) + "; using default simplification factor: " + 
			nTopojson_options["retain-proportion"];
	}
	if (myPropertyTransform) { // Put property-transform function back (removed by topology!)
		nTopojson_options["property-transform"]=myPropertyTransform;
		response.message+="; property-transform enabled";	
	}	
	else {
		response.message+="; property-transform disabled";
	}
	response.message+="; topoJSON options pre transform: " + JSON.stringify(nTopojson_options, null, 4);

	/*
	 * Function: 	toTopoJSONZoomlevel() 
	 * Parameters:  Zoomlevel, current zoomlevel object, previous zoomlevel object, callback function
	 * Returns: 	Nothing
	 * Description: Convert geoJSON to topoJSON for a zoomlevel
	 */	
	function toTopoJSONZoomlevel(i, currentConvertedTopojson, previousConvertedTopojson, callback) {

		scopeChecker(__file, __line, {
			response: response,
			clone: clone,
			sizeof: sizeof,
			wellknown: wellknown,
			currentConvertedTopojson: currentConvertedTopojson,
			previousConvertedTopojson: previousConvertedTopojson,
			callback: callback
		},
		{
			callback: myPropertyTransform
		} /* Optional */);
	
		stderr.disable();	// Re-route topoJSON stderr to stderr.str

		var lstart = new Date().getTime();	
		var nGeojson;	
		var nGeojsonLen;
		var nTopojson;
		var nTopojsonLen;
		var end;	
		var msg;
		
		// Create GeoJSON from topoJSON of the previous zoomlevel
		var j=0;
		for (var key in previousConvertedTopojson.topojson.objects) {
			j++;
			if (j == 1) {	
				end = new Date().getTime();
				response.message+="\n+"  + ((end - lstart)/1000) + " S; " + fileName + ": created geojson for zoomlevel " + currentConvertedTopojson.zoomlevel + 
					" from zoomlevel topojson: " + previousConvertedTopojson.zoomlevel;
				nGeojson = topojson.feature(previousConvertedTopojson.topojson, 
					previousConvertedTopojson.topojson.objects[key]);
				nGeojsonLen=sizeof(nGeojson);
				end = new Date().getTime();
				var msg="Created geojson (" + nGeojson.features.length + " areas) for zoomlevel " + currentConvertedTopojson.zoomlevel + 
					" from zoomlevel " + 
					previousConvertedTopojson.zoomlevel + " topojson: " + fileName;
				response.message+="\n" + msg  + "; took: " + ((end - lstart)/1000) + "S";
									
				addStatus(__file, __line, response, msg,   // Add created geojson from zoomlevel topojson status	
					200 /* HTTP OK */, serverLog, undefined /* req */,
					/*
					 * Function: 	createGeoJSONFromTopoJSON()
					 * Parameters:	error object
					 * Description:	Add status callback
					 */												
					function createGeoJSONFromTopoJSON(err) {
						if (err) {
							serverLog.serverLog2(__file, __line, "createGeoJSONFromTopoJSON", 
								"WARNING: Unable to add topojson processing status", req, err);
						}
						else {
							geoJSON2WKT(); // Now create WKT it
						}
					});
//				if (i == (convertedTopojson[0].zoomlevel-1)) {
//					response.message+="\nZoomlevel [" + i + "] " + key + " Truncated geoJSON >>>\n" + JSON.stringify(nGeojson, null, 4).substring(0, 600) + "\n<<< End of JSON";
//				} // Can cause - Stack: RangeError: Invalid string length
			}
			else {
				break; // Out  of for loop
			}
		} // For loop			

		/*
		 * Function: 	geoJSON2WKT()
		 * Parameters:	None
		 * Description:	Callback from createGeoJSONFromTopoJSON()
		 */	
		function geoJSON2WKT() {	
			var wktLen=0;
			var j=0;
			
			async.forEachOfSeries(nGeojson.features, 
				function geoJSON2WKTSeries(value, i, lcallback) { // Processing code
					j++;
					try {
						previousConvertedTopojson.wkt[i]=wellknown.stringify(nGeojson.features[i]);	
						wktLen+=previousConvertedTopojson.wkt[i].length;
					} 
					catch (e) {
						lcallback(e);
					}
					if (j >= 1000) {
						j=0;
						process.nextTick(lcallback);
					}
					else {
						lcallback();
					}
				},
				function geoJSON2WKTError(err) { //  Callback
			
					if (err) {
						callback(err);
					}
					else {
						end = new Date().getTime();
						var msg="Created wellknown text for zoomlevel " + currentConvertedTopojson.zoomlevel + 
							" from geoJSON: " + fileName;
						response.message+="\n" + msg  + "; size: " + wktLen + "; took: " + ((end - lstart)/1000) + "S";
											
						addStatus(__file, __line, response, msg,   // Add created WKT zoomlevel topojson status	
							200 /* HTTP OK */, serverLog, undefined /* req */,
							/*
							 * Function: 	createGeoJSONFromTopoJSON()
							 * Parameters:	error object
							 * Description:	Add status callback
							 */												
							function geoJSON2WKTAddStatus(err) {
								if (err) {
									serverLog.serverLog2(__file, __line, "geoJSON2WKTAddStatus", 
										"WARNING: Unable to add WKT processing status", req, err);
								}
								else {
									cloneTopoJSON(); // Now clone it
								}
							}
						);	
					}						
				}
			);
	
		}
		/*
		 * Function: 	cloneTopoJSON()
		 * Parameters:	None
		 * Description:	Callback from geoJSON2WKT()
		 */	
		 function cloneTopoJSON() {
			if (j > 1) {
				callback(new Error("Create topojson zoomlevel[" + i + "] topojson.objects length > 1: "  + j));
			} 
			
	// Clone using JSON.parse(JSON.stringify() ...
	/*
	Run shapeFileQueueCallback callback()
	ERROR! in readShapeFile(): Invalid string length
	Stack: RangeError: Invalid string length
		at join (native)
		at Object.stringify (native)
		at toTopoJSONZoomlevels (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\simplifyGeoJSON.js:355:74)
		at toTopoJSON (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\simplifyGeoJSON.js:252:3)
		at Object.shapefileSimplifyGeoJSON (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\simplifyGeoJSON.js:114:23)
		at topoFunction (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:866:23)
		at _myWrite (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\streamWriteFileWithCallback.js:303:6)
		at Object.streamWriteFilePieceWithCallback (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\streamWriteFileWithCallback.js:259:2)
		at writeGeoJsonbyFeatureSeriesEnd (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:885:35)
		at C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\async\dist\async.js:399:20
	 *
			end = new Date().getTime();
			response.message+="\n+"  + ((end - lstart)/1000) + " S; " + fileName + ": clone topojson[stage 1: stringify]: " + 
				previousConvertedTopojson.zoomlevel;
			var nTopojsonStr=JSON.stringify( // Clone
					previousConvertedTopojson.topojson);				
			end = new Date().getTime();
			addStatus(__file, __line, response, fileName + ": clone topojson[stage 1: stringify]: " + 
				previousConvertedTopojson.zoomlevel + "; took: " + ((end - lstart)/1000) + "S", 
				200 /- HTTP OK -/, serverLog, undefined /- req -/);  // Add clone 1 status		
			response.message+="\n+"  + ((end - lstart)/1000) + " S; " + fileName + ": clone topojson[stage 2: parse]: " + 
				previousConvertedTopojson.zoomlevel;				
			var nTopojson=JSON.parse(nTopojsonStr);
			end = new Date().getTime();
			addStatus(__file, __line, response, fileName + ": clone topojson[stage 2: parse]: " + 
				previousConvertedTopojson.zoomlevel + "; took: " + ((end - lstart)/1000) + "S", 
				200 /- HTTP OK -/, serverLog, undefined /- req -/);  // Add clone 2 status				
			nTopojsonStr=undefined;
	 */		
			else if (nGeojson && nGeojsonLen > 0) {		
				response.message+="\n+"  + ((end - lstart)/1000) + " S; " + fileName + ": clone topojson : " + 
					previousConvertedTopojson.zoomlevel;				
				nTopojson=clone(previousConvertedTopojson.topojson, false /* no cicrular references */);
				
				nTopojsonLen=sizeof(nTopojson);
				end = new Date().getTime();
				mnsg=fileName + ": clone topojson[: " + previousConvertedTopojson.zoomlevel;
				response.message+="\n" + msg  + "; took: " + ((end - lstart)/1000) + "S"
				addStatus(__file, __line, response, msg,   // Add clone() status
					200 /* HTTP OK */, serverLog, undefined /* req */, 
					/*
					 * Function: 	cloneTopoJSONAddStatus()
					 * Parameters:	error object
					 * Description:	Add status callback
					 */												
					function cloneTopoJSONAddStatus(err) {
						if (err) {
							serverLog.serverLog2(__file, __line, "cloneTopoJSONAddStatus", 
								"WARNING: Unable to add topojson processing status", req, err);
						}
						else {
							simplifyTopoJSON() // Simplify topojson
						}
					});				
			}
			else {	
				callback(new Error("Create topojson zoomlevel[" + i + "] no geojson created from topojson convertedTopojson[" + 
					(i-1) + 
					"]; length: " + previousConvertedTopojson.topojson_length));
			}
		} // End of cloneTopoJSON() 

		/*
		 * Function: 	simplifyTopoJSON()
		 * Parameters:	None
		 * Description:	Callback from cloneTopoJSONAddStatus()
		 */			
		function simplifyTopoJSON() {
			try { // Simplify topojson
				if (nTopojson && nTopojsonLen > 0) {
					currentConvertedTopojson.geojson_length=nGeojsonLen;
					response.message+="\nCreate topojson zoomlevel[" + i + "] geojson size: " + currentConvertedTopojson.geojson_length + 
						" created from zoomlevel: " +
						previousConvertedTopojson.zoomlevel;						
					currentConvertedTopojson.topojson=topojson.simplify(  // Simplify topoJSON
							nTopojson, nTopojson_options);						
				}
				else {	
					callback(new Error("Create topojson zoomlevel[" + i + "] no nTopojson cloned from topojson convertedTopojson[" + (i-1) + 
						"]; length: " + previousConvertedTopojson.topojson_length));
				}
			}
			catch (e) {
				stderr.enable(); 				   // Re-enable stderr
	//			console.error("Create topojson zoomlevel[" + i + "] error: " + e.message + "\nStack>>>\n" + e.stack + "<<<\nStderr:\n" + stderr.str());
				stderr.clean();						// Clean down stderr string
				stderr.restore();                   // Restore normal stderr functionality 						
				callback(e);
			}
			var end = new Date().getTime();
			currentConvertedTopojson.topojson_runtime=(end - lstart)/1000; // in S	
			currentConvertedTopojson.topojson_length=sizeof(currentConvertedTopojson.topojson);	
			stderr.enable(); 				   // Re-enable stderr
			currentConvertedTopojson.topojson_stderr=stderr.str();	
			currentConvertedTopojson.topojson_arcs=currentConvertedTopojson.topojson.arcs.length;
			currentConvertedTopojson.topojson_points=currentConvertedTopojson.topojson.arcs.reduce(
																				function(p, v) { return p + v.length; }, 0);
			if (i == (convertedTopojson[0].zoomlevel-1)) {
				response.message+="\nTopoJSON options post transform for zoomlevel[" + i + "] " + JSON.stringify(nTopojson_options, null, 4) + 
					"\nCreated topojson for zoomlevel[" + i + "]; size: " + currentConvertedTopojson.topojson_length + 
					"; took: " + currentConvertedTopojson.topojson_runtime + 
					"S;  diagnostics\n" + currentConvertedTopojson.topojson_stderr;  // Get stderr as a string
			}
			else {
				response.message+="\nCreated topojson for zoomlevel[" + i + "]; size: " + 
					currentConvertedTopojson.topojson_length + 
					"; took: " + currentConvertedTopojson.topojson_runtime + 
					"S;  diagnostics\n" + currentConvertedTopojson.topojson_stderr;  // Get stderr as a string
			}			
			var topojsonGeometries;
			if (currentConvertedTopojson.topojson &&
				currentConvertedTopojson.topojson.objects &&
				currentConvertedTopojson.topojson.objects.collection &&
				currentConvertedTopojson.topojson.objects.collection.geometries &&
				currentConvertedTopojson.topojson.objects.collection.geometries[0] &&
				currentConvertedTopojson.topojson.objects.collection.geometries[0].properties) {
				topojsonGeometries=currentConvertedTopojson.topojson.objects.collection.geometries;

				var properties;
				if (topojsonGeometries[0] && topojsonGeometries[0].properties) {
					properties=Object.keys(topojsonGeometries[0].properties).length;
				}
						
				response.message+="\nTopojson has " + (topojsonGeometries.length || "no") + " features with " + (properties || "no") + " properties";
			}
			
			msg=fileName + ": simplified topojson for zoomlevel: " + i;
			response.message+="\n" + msg + "; took: " + 
				currentConvertedTopojson.topojson_runtime + "S"
			addStatus(__file, __line, response, msg, 
				200 /* HTTP OK */, serverLog, undefined /* req */,  // Add zoomlevel topojson simplify status	
				/*
				 * Function: 	simplifiedTopoJSONComplete()
				 * Parameters:	error object
				 * Description:addStatus() callback
				 */												
				function simplifiedTopoJSONComplete(err) {
					if (err) {
						serverLog.serverLog2(__file, __line, "simplifiedTopoJSONComplete", 
							"WARNING: Unable to simplify TopoJSON", req, err);
					}
					stderr.clean();						// Clean down stderr string
					stderr.restore();                   // Restore normal stderr functionality 	
					callback(err);
				});		
		} // End of simplifyTopoJSON()		
	} // End of toTopoJSONZoomlevel()

	var startZoomlevel=convertedTopojson[0].zoomlevel;	
	for (var i=(startZoomlevel-1); i>=6; i--) { // Setup array for async
//		console.error("Create convertedTopojson[" + convertedTopojson.length + "]; zoomlevel: " + i);
		convertedTopojson[convertedTopojson.length] = {
			topojson: undefined,
			wkt: [],
			geojson_length: undefined,
			topojson_length: undefined,
			topojson_runtime: undefined,
			topojson_options: clone(nTopojson_options),
			topojson_stderr: undefined,
			topojson_arcs: undefined,
			topojson_points: undefined,
			zoomlevel: i
		};	
	} // End of for loop
	
	const async = require('async');

	var astart = new Date().getTime();
	async.forEachOfSeries(convertedTopojson, 
		function (value, j, callback) { // Processing code
			scopeChecker(__file, __line, {
				response: response,
				message: response.message,
				convertedTopojson: convertedTopojson,
				callback: topoJSONcallback,
				astart: astart
			});
		
//			console.error("toTopoJSONZoomlevel START: " +  convertedTopojson.length + "; zoomlevel: " + value.zoomlevel);
			var startZoomlevel=convertedTopojson[0].zoomlevel;	
			if (convertedTopojson[j] == undefined) {
				callback(new Error("Create topojson convertedTopojson[" + j + "].zoomlevel does exist"));
			}
			
			var i=convertedTopojson[j].zoomlevel;
			if (convertedTopojson[j] && convertedTopojson[j].zoomlevel && startZoomlevel) {
				if (convertedTopojson[j].zoomlevel < startZoomlevel) {
					
					if (!convertedTopojson[(j-1)]) {
						callback(new Error("Create topojson zoomlevel[" + i + "] no previous convertedTopojson[] element: " + (j-1)));
					}			
					else if (!convertedTopojson[(j-1)].topojson) {
						callback(new Error("Create topojson zoomlevel[" + i + "] no previous convertedTopojson[] topojson object: " + (j-1)));
					}		
					else if (!convertedTopojson[(j-1)].topojson.objects) {
						callback(new Error("Create topojson zoomlevel[" + i + "] no previous convertedTopojson[] topojson.objects object: " + (j-1)));
					}
//					console.error("toTopoJSONZoomlevel(" + i + "); zoomlevel: " + convertedTopojson[j].zoomlevel + "; previous zoomlevel: " + convertedTopojson[(j-1)].zoomlevel);
					toTopoJSONZoomlevel(i, convertedTopojson[j], convertedTopojson[(j-1)], callback);
				}
				else {
//					console.error("IGNORE FIRST toTopoJSONZoomlevel(" + i + "); zoomlevel: " + convertedTopojson[j].zoomlevel);
					callback();
				}		
			}
			else {
				callback(new Error("One of convertedTopojson[j] && convertedTopojson[j].zoomlevel && startZoomlevel does exist"));
			}	
		}, // End of convertTopoJSONSeries() 
		function seriesEndCallbackFunc(e) { // Cause seriesCallback to be named
//			console.error("toTopoJSONZoomlevel END: " + convertedTopojson.length);
			if (e) {
				serverLog.serverLog2(__file, __line, "seriesEndCallbackFunc", 
					"WARNING: Unable to simplify TopoJSON", req, e);
					topoJSONcallback(e);
			}
			else {			
				var msg=fileName + ": simplified topojson for zoomlevels " + convertedTopojson[(convertedTopojson.length-1)].zoomlevel + " to " +  convertedTopojson[0].zoomlevel;
				var end = new Date().getTime();
				response.message+="\n" + msg + "; took: " +  ((end - astart)/1000) + "S";
				addStatus(__file, __line, response, msg, 
					200 /* HTTP OK */, serverLog, undefined /* req */,  // Add zoomlevel topojson simplify status	
					/*
					 * Function: 	addStatusSeriesEndCallbackFunc()
					 * Parameters:	error object
					 * Description:addStatus() callback
					 */												
					function addStatusSeriesEndCallbackFunc(err) {
						if (err) {
							serverLog.serverLog2(__file, __line, "addStatusSeriesEndCallbackFunc", 
								"WARNING: Unable to simplify TopoJSON", req, err);
						}
						topoJSONcallback(err);
					}
				);
			}	
		} // End of seriesEndCallbackFunc()	
	); // End of async.forEachOfSeries(convertedTopojson, ...
		
} // end of toTopoJSONZoomlevels()

/*
 * Function:	jsonParse()
 * Parameters:	Buffer, internal reposnse object
 * Returns:		Buffer parsed to JSON
 * Description: Parses buffer as a feature collection feature by feature to avoid toString() errors where the string is over 255MB
 */
var jsonParse = function jsonParse(data, response) {

	scopeChecker(__file, __line, {
		data: data,
		response: response
	});	
	
	var lstart = new Date().getTime();	
	var myFeatureCollection;
	
	if ((data.indexOf('{"type":"FeatureCollection","features":[', 0) == 0) || 
	    (data.indexOf('{"type":"FeatureCollection","bbox":', 0) == 0)) { // Its a feature collection
		response.message+="\nParsing feature collection; data length: " + data.length;
			
		var featureOffset = [0];
		var newOffset;
		var offset=0;
		do { // Look for feature collection start: {"type":"Feature"
			newOffset=data.indexOf('{"type":"Feature",', offset);
			if (newOffset >= 0) {
				featureOffset.push(newOffset);
				offset=newOffset+1;
			}
		} while (newOffset >= 0);
		
		var parseAble;
		var parseError;
		var parsedJson=[];
		var totalParseTime=0;
		for (var i=0; i<featureOffset.length; i++) {
			parsedJson[i] = {
				json: undefined,
				featureOffset: featureOffset[i],
				offsetEnd: undefined,
				parseError: undefined,
				parseTime: undefined
			};
			// Look for end of feature collection end: ]]}}
			parsedJson[i].offsetEnd=data.indexOf(']]}}', featureOffset[i])+4;
		}
		
		for (var i=0; i< parsedJson.length; i++) { // Parse using featureOffset and offsetEnd
			var buf=data.slice(parsedJson[i].featureOffset, parsedJson[i].offsetEnd);
			var str=buf.toString('ascii', 0, 60);
			var str2=buf.toString('ascii', buf.length-60);
			try {
				if (i == 0) { // Add end of feature collection
					str=buf.toString()+"]}";
					parsedJson[i].json=JSON.parse(str);
				}
				else {
					parsedJson[i].json=JSON.parse(buf.toString());					
				}
			}
			catch (e) {
				parsedJson[i].parseError=e.message;
			}
			
			var now = new Date().getTime();
			parsedJson[i].parseTime=(now - lstart)/1000; // in S	
			if (parseError) {
				response.message+="\nWarning feature [" + i + "/" + (featureOffset.length-1) + "] start: " + parsedJson[i].featureOffset + "; end: " + parsedJson[i].offsetEnd + 
					"; Feature length: " + buf.length + 
					"; could not be parsed: " + parseError + "\n>>>\n" + buf.toString('ascii', 0, 240) + "\n<<<\n";
			}
			else if (i<4 || i>(featureOffset.length-4)) {
				if (str.length < 60) {
					response.message+="\nFeature [" + i + "/" + (featureOffset.length-1) + "] start: " + parsedJson[i].featureOffset + "; end: " + parsedJson[i].offsetEnd + 
						"; Feature length: " + buf.length + 
						"\n>>>" + str + "<<<";
				}
				else {
					response.message+="\nFeature [" + i + "/" + (featureOffset.length-1) + "] start: " + parsedJson[i].featureOffset + "; end: " + parsedJson[i].offsetEnd + 
						"; Feature length: " + buf.length + 
						"\n>>>" + str.substring(0, 60) + "<<< ... >>>" + str2 + "<<<";
				}
			}
			else if (((i/1000)-Math.floor(i/1000)) == 0 || parsedJson[i].parseTime > (totalParseTime + 1)) { 
				totalParseTime=parsedJson[i].ParseTime;
				var msg="Feature [" + i	+ "/" + (featureOffset.length-1) + "] start: " + parsedJson[i].featureOffset + "; end: " + parsedJson[i].offsetEnd + 
					"; Feature length: " + buf.length + 
					"\n>>>" + str + "<<< ... >>>" + str2 + "<<<";
//				console.error(msg);
				response.message+="\n" + msg;				
			}
		}

		var end = new Date().getTime();
		var ParseTime=(end - lstart)/1000; // in S			
		if (featureOffset.length == parsedJson.length) {
			response.message+="\nFeature collection parse complete; features detected: " + (featureOffset.length-1) + 
				"; data length: " + data.length + "; took: " + ParseTime + " S";
			myFeatureCollection=parsedJson[0].json;
			for (var i=1; i<parsedJson.length; i++) {
				myFeatureCollection.features[i-1]=parsedJson[i].json;
			}			
		}
		else {
			throw new Error("Feature collection parse failed: " + (featureOffset.length - parsedJson.length) + "/" + featureOffset.length + " features failed to parse");
		}
	}
	else {
		response.message+="\nNot a feature collection, parse may fail>>>\n" + data.toString('ascii', 0, 240) + "\n<<<\n";
	}
	
	var kStringMaxLength = process.binding('buffer').kStringMaxLength;
	if (myFeatureCollection) {
		return(myFeatureCollection);
	}		
	else if (data.length < kStringMaxLength) {		
		return JSON.parse(data.toString()); // Parse file stream data to JSON
	}	
	else {
		throw new Error("Cannot parse string: not a feature collection and too long: " + data.length + 
			" for JSON.parse(data.toString()); limit: " + kStringMaxLength + "bytes");
	}
} // End of jsonParse()

module.exports.jsonParse = jsonParse;
module.exports.toTopoJSON = toTopoJSON;
module.exports.shapefileSimplifyGeoJSON = shapefileSimplifyGeoJSON;
module.exports.getQuantization = getQuantization;

// Eof
		