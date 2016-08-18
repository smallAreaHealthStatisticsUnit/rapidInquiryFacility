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
// Rapid Enquiry Facility (RIF) - GeoJSON to CSV conversion code
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
	
const wellknown = require('wellknown'),
	  async = require('async'),
	  serverLog = require('../lib/serverLog'),
	  nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon'),
	  httpErrorResponse = require('../lib/httpErrorResponse');
	  
var geojsonToCSV = function geoJSON2WKT(response, req, res, endCallback) {
	
	scopeChecker(__file, __line, {
		response: response,
		message: response.message,
		serverLog: serverLog,
		req: req,
		res: res,
		httpErrorResponse: httpErrorResponse,
		nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon,
		callback: endCallback
	});
					
	/*
	 * Function: 	geoJSON2WKT()
	 * Parameters:	Topojson zoomlevel object, file name, topojson Callback [file+zoomlevel async loop], file index, CSV files array
	 * Description:	Convert geoJSON to well known text async
	 */	
	function geoJSON2WKT(topojson, fileName, topojsonCallback, i, csvFiles) {	
		scopeChecker(__file, __line, {
			topojson: topojson,
			fileName: fileName,
			callback: topojsonCallback
		});
		
		var wktLen=0;
		var l=0;
		var l2start = new Date().getTime();
		if (topojson && topojson.geojson) {
			async.forEachOfSeries(topojson.geojson.features, 
				function geoJSON2WKTSeries(value, k, featureCallback) { // Processing code
					l++;
					try {
						topojson.wkt[k]=wellknown.stringify(topojson.geojson.features[k]);	
//						if (k<=9) {
							
						var zoomlevelFieldName="WKT_" + topojson.zoomlevel;
						var row = {};
						if (csvFiles[i].rows[k] && csvFiles[i].rows[k].GID) { // Already added
							csvFiles[i].rows[k][zoomlevelFieldName]=topojson.wkt[k];
//							console.error("geoJSON2WKTSeries() update k: " + k + 
//								"; i: " + i + 
//								"; l: " + l + 
//								"; zoomlevel: " + topojson.zoomlevel + 
//								"; wkt length: " + csvFiles[i].rows[k][zoomlevelFieldName].length + 
//								"; zoomlevelFieldName: " + zoomlevelFieldName + 
//								"; keys: " + Object.keys(csvFiles[i].rows[k]).length + 
//								"; fileName: " + fileName );
						}
						else {
							for (var key in topojson.geojson.features[k].properties) {
								row[key] = (topojson.geojson.features[k].properties[key]||"");
							}
							row[zoomlevelFieldName]=topojson.wkt[k];
							csvFiles[i].rows.push(row);
							if (k != (csvFiles[i].rows.length-1)) {
								throw new Error("geoJSON2WKTSeries() add CSV row k: " + k +
									" != (csvFiles[i].rows.length-1): " + (csvFiles[i].rows.length-1) + 
									"; for fileName: " + fileName + 
									"; zoomlevel: " + topojson.zoomlevel)
							}
//							console.error("geoJSON2WKTSeries() add row k: " + k + 
//								"; i: " + i + 
//								"; l: " + l + 
//								"; (csvFiles[i].rows.length-1): " + (csvFiles[i].rows.length-1) + 
//								"; zoomlevel: " + topojson.zoomlevel + 
//								"; wkt length: " + csvFiles[i].rows[(csvFiles[i].rows.length-1)][zoomlevelFieldName].length + 
//								"; zoomlevelFieldName: " + zoomlevelFieldName + 
//								"; keys: " + Object.keys(csvFiles[i].rows[(csvFiles[i].rows.length-1)]).length + 
//								"; fileName: " + fileName);

						}
//						} // k test restriction

						
						wktLen+=topojson.wkt[k].length;
						if (l >= 100) {
							l=0;
							process.nextTick(featureCallback);
						}
						else {
							featureCallback();
						} 
					}
					catch (e) {
						featureCallback(e);
					}
				
				},
				function geoJSON2WKTEnd(err) { //  Callback
			
					if (err) {
						topojsonCallback(err);
					}
					else {
						
						csvFiles[i].topojson_arcs=topojson.topojson_arcs;
						csvFiles[i].topojson_points=topojson.topojson_points;
						
						end = new Date().getTime();
						var msg="Created wellknown text for zoomlevel " + topojson.zoomlevel + 
							" from geoJSON: " + fileName;
						response.message+="\n" + msg  + "; size: " + wktLen + "; took: " + ((end - l2start)/1000) + "S";
											
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
									topojsonCallback(err)
								}
								else {
//									console.error("Call: topojsonCallback(); zoomlevel: " + topojson.zoomlevel + 
//										"; fileName: " + fileName);
									process.nextTick(topojsonCallback);
								}
							}
						);	
					}						
				}
			);
		}
		else if (topojson) {		
			serverLog.serverError2(__file, __line, "geoJSON2WKT", 
				"topojson.geojson not defined for zoomlevel: " + topojson.zoomlevel, req, undefined /* err */, response);
		}
		else {		
			serverLog.serverError2(__file, __line, "geoJSON2WKT", 
				"topojson not defined ", req, undefined /* err */, response);
		}

	} // End of geoJSON2WKT()

	
	var csvFiles = [];	
	var lstart = new Date().getTime();
	
	// Convert all geojson to wellknown text by file then zoomlevel
	async.forEachOfSeries(response.file_list, 
		function geoJSON2WKTFileSeries(value, i, fileCallback) { // Processing code	
			
			csvFiles[i] = {
				index: i,
				file_name: response.file_list[i].file_name,
				areas: response.file_list[i].total_areas,
				points: response.file_list[i].points,
				geolevel: response.file_list[i].geolevel_id,
				topojson_arcs: undefined,
				topojson_points: undefined,
				rows: [],
			};
					
			async.forEachOfSeries(response.file_list[i].topojson, 
				function geoJSON2WKTFileTopojsonSeries(value, j, topojsonCallback) { // Processing code	
					geoJSON2WKT(response.file_list[i].topojson[j], response.file_list[i].file_name, topojsonCallback, i, csvFiles);
				},
				function geoJSON2WKTFileTopojsonEnd(err) { //  Callback
	
					if (err) {
						serverLog.serverError2(__file, __line, "geoJSON2WKTFileTopojsonEnd", 
							"geoJSON2WKT() processing error", req, err, response);
						fileCallback(err)
					}
					else {
//						console.error("Call: fileCallback(); fileName[" + i + "]: " + response.file_list[i].file_name);
						process.nextTick(fileCallback);
					}
				}
			); // End of async.forEachOfSeries(response.file_list[i].topojson, ...)
		},
		function geoJSON2WKTFileEnd(err) { //  Callback
	
			if (err) {
				httpErrorResponse.httpErrorResponse(__file, __line, "geoJSON2WKTFileEnd()", 
					serverLog, 500, req, res, msg, err, response);
			}
			else {
				end = new Date().getTime();
				var msg="Created wellknown text for " + response.file_list.length + " zoomlevels; " +  csvFiles.length + " CSV files";
				response.message+="\n" + msg + "; took: " + ((end - lstart)/1000) + "S";
									
				addStatus(__file, __line, response, msg,   // Add created WKT zoomlevel topojson status	
					200 /* HTTP OK */, serverLog, undefined /* req */,
					/*
					 * Function: 	createGeoJSONFromTopoJSON()
					 * Parameters:	error object
					 * Description:	Add status callback
					 */												
					function geoJSON2WKTFileAddStatus(err) {
						if (err) {
							serverLog.serverLog2(__file, __line, "geoJSON2WKTFileAddStatus", 
								"WARNING: Unable to add WKT file processing status", req, err);
						}

						// Remove any geoJSON or WKT from response if topoJSON present; save geojson		
						for (var i=0; i<response.file_list.length; i++) {
							if (response.file_list[i].topojson) {
								for (var j=0; j<response.file_list[i].topojson.length; j++) {	
									if (response.file_list[i].topojson[j].wkt && response.file_list[i].topojson[j].wkt.length > 0) {
										response.file_list[i].topojson[j].wkt=undefined;
									}
									if (response.file_list[i].topojson[j].geojson) {
										response.file_list[i].topojson[j].geojson=undefined;
									}
									if (response.file_list[i].geojson) {
										response.file_list[i].geojson=undefined
									}
//									if (i == (response.file_list[i].length-1) && j == (response.file_list[i].topojson.length -1)) {
//										console.error("Removed any geoJSON or WKT from response if topoJSON present");
//									}
								}
							}			
						}

						// Dump CSV file data
	
						for (var i=0; i<csvFiles.length; i++) {
							var keys=Object.keys(csvFiles[i].rows[0]);
							response.message+="\nCSV file [" + (i+1) + "]: " + (csvFiles[i].file_name||"No file") + "; rows: " + csvFiles[i].rows.length +
								"; " + keys.length + " keys: " + keys.toString();
						}
						endCallback(); // Run callback
						
					} // End of geoJSON2WKTFileAddStatus()
				);	// End of addStatus()
			}
		} // End of geoJSON2WKTFileEnd()
	);	// End of async.forEachOfSeries()	
											
} // End of geojsonToCSV()

module.exports.geojsonToCSV = geojsonToCSV;

// Eof
