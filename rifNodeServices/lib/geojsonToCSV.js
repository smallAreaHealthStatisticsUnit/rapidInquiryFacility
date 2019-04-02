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
	  dbLoad = require('../lib/dbLoad'),
	  httpErrorResponse = require('../lib/httpErrorResponse');

const os = require('os'),
	  fs = require('fs'),
	  path = require('path');
	
/*
 * Function: 	geojsonToCSV()
 * Parameters:	Internal response object, xmlConfig, HTTP request object, HTTP response object, callback to call at end of processing
 * Description:	Convert geoJSON to CSV; save as CSV files; create load scripts for Postgres and MS SQL server
 */		
var geojsonToCSV = function geojsonToCSV(response, xmlConfig, req, res, endCallback) {
	
	scopeChecker(__file, __line, {
		response: response,
		message: response.message,
		xmlConfig: xmlConfig,
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
			callback: topojsonCallback,
			response: response,
			fields: response.fields,
			max_zoomlevel: response.fields["max_zoomlevel"],
			uuidV1: response.fields["uuidV1"]
		});
//		console.error("geoJSON2WKTSeries() csvFiles[" + i + "].rows.length: " + csvFiles[i].rows.length);

//
// Check has features and the GID property
//		
		if (topojson.geojson.features.length == 0) {
			topojsonCallback(new Error("geoJSON2WKTSeries() No features for csvFiles[" + 
				i + "].rows.length: " + csvFiles[i].rows.length + 
				";\nfor fileName: " + fileName + 
				"; zoomlevel: " + topojson.zoomlevel
				));
			return;
		}
		else if (topojson.geojson.features[0].properties["GID"] == undefined) {
			topojsonCallback(new Error("geoJSON2WKTSeries() No GID field for in properties for csvFiles[" + 
				i + "].rows.length: " + csvFiles[i].rows.length + 
				";\nfor fileName: " + fileName + 
				"; zoomlevel: " + topojson.zoomlevel +
				"\nKeys: " + Object.keys(topojson.geojson.features[0].properties).join(",")
				));
			return;
		}
 	
		var wktLen=0;
		var l=0;
		var m=-1;
		var l2start = new Date().getTime();
		if (topojson && topojson.geojson) {
			async.forEachOfSeries(topojson.geojson.features, 
				function geoJSON2WKTSeries(value, k, featureCallback) { // Processing code
					l++;
					m++;
					try {
						topojson.wkt[k]=wellknown.stringify(topojson.geojson.features[k]);	
						
						if (topojson.zoomlevel > response.fields["max_zoomlevel"]) {
							throw new Error("geoJSON2WKTSeries() zoomlevel (" + topojson.zoomlevel + 
								") > max_zoomlevel (" + response.fields["max_zoomlevel"] + ")");
						}
						var zoomlevelFieldName="WKT_" + topojson.zoomlevel;
						var row = {};
						if (csvFiles[i].rows[k] && csvFiles[i].rows[k].GID) { // Already added
							csvFiles[i].rows[k][zoomlevelFieldName]=topojson.wkt[k];

/*							
							if (k<=9) {
								console.error("geoJSON2WKTSeries() update k: " + k + 
									"; i: " + i + 
									"; m: " + m + 
									"; l: " + l + 
									"; zoomlevel: " + topojson.zoomlevel + 
									"; wkt length: " + csvFiles[i].rows[k][zoomlevelFieldName].length + 
									"; zoomlevelFieldName: " + zoomlevelFieldName + 
									"; keys: " + Object.keys(csvFiles[i].rows[k]).length + 
									"; properties: " + JSON.stringify(topojson.geojson.features[k].properties) + 
									"; fileName: " + fileName );
							} 
 */
						}
						else {
							for (var key in topojson.geojson.features[k].properties) {
								row[key] = (topojson.geojson.features[k].properties[key]||
									topojson.geojson.features[k].properties[key.toUpperCase()]||
									topojson.geojson.features[k].properties[key.toLowerCase()]||
									"");
                                if (row[key] == "") {
                                    if (k < 9) {
                                        console.error("geoJSON2WKTSeries() update k: " + k + 
                                            "; i: " + i + 
                                            "; m: " + m + 
                                            "; l: " + l + 
                                            "; k: " + k + 
                                            "; zoomlevel: " + topojson.zoomlevel + 
                                            "; null key: " + key + 
                                            "; properties: " + JSON.stringify(topojson.geojson.features[k].properties) + 
                                            "; fileName: " + fileName );
                                    }
                                }
							}
							row[zoomlevelFieldName]=topojson.wkt[k];
							csvFiles[i].rows.push(row);							
/*
							if (k<=9) {
								console.error("geoJSON2WKTSeries() add row m: " + m + 
									"; i: " + i + 
									"; k: " + k + 
									"; l: " + l + 
									"; (csvFiles[" + i + "].rows.length-1): " + (csvFiles[i].rows.length-1) + 
									"; zoomlevel: " + topojson.zoomlevel + 
									"; wkt length: " + csvFiles[i].rows[(csvFiles[i].rows.length-1)][zoomlevelFieldName].length + 
									"; zoomlevelFieldName: " + zoomlevelFieldName + 
									"; keys: " + Object.keys(csvFiles[i].rows[(csvFiles[i].rows.length-1)]).length + 
									"; properties: " + JSON.stringify(topojson.geojson.features[k].properties) + 
									"; fileName: " + fileName +
									";\nrow: " + JSON.stringify(row, null, 2).substring(0, 400) + "...");
							}	
 */					
							if (m != (csvFiles[i].rows.length-1)) {
								throw new Error("geoJSON2WKTSeries() add CSV row m: " + m +
									"; k: " + k + 
									" != (csvFiles[" + i + "].rows.length-1): " + (csvFiles[i].rows.length-1) + 
									";\nfor fileName: " + fileName + 
									"; zoomlevel: " + topojson.zoomlevel +
									";\nrow: " + JSON.stringify(row, null, 2).substring(0, 200) + "..." + 
									";\nproperties: " +  
										JSON.stringify(topojson.geojson.features[k].properties, null, 2).substring(0, 100)
									);
							}
						}
						
						wktLen+=topojson.wkt[k].length;
						if (l >= 1000) {
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
							" from geoJSON: " + fileName +
							"; rows: " + csvFiles[i].rows.length;
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
											
//	
// Create directory: $TEMP/shpConvert/<uuidV1>/data as required
//		  
	var dirArray=[os.tmpdir() + "/shpConvert", response.fields["uuidV1"], "data"];
	var dir=nodeGeoSpatialServicesCommon.createTemporaryDirectory(dirArray, response, req, serverLog);
		  
//	
// Write file to directory
//	
	var csvFiles = [];	
	var lstart = new Date().getTime();
	
	// Convert all geojson to wellknown text by file then zoomlevel
	async.forEachOfSeries(response.file_list, 
		function geoJSON2WKTFileSeries(value, i, fileCallback) { // Processing code			
			
			csvFiles[i] = {
				index: i,
				file_name: response.file_list[i].file_name,
				file_name_no_ext: path.basename(response.file_list[i].file_name, ".shp"),
				tableName: path.basename(response.file_list[i].file_name, ".shp").toLowerCase(),
				areas: response.file_list[i].total_areas,
				points: response.file_list[i].points,
				geolevel: response.file_list[i].geolevel_id,
				geolevelDescription: (response.file_list[i].desc || "Not defined"),
				topojson_arcs: undefined,
				topojson_points: undefined,
				bbox: response.file_list[i].bbox,
				rows: [],
			};
					
			async.forEachOfSeries(response.file_list[i].topojson, 
				function geoJSON2WKTFileTopojsonSeries(value, j, topojsonCallback) { // Processing code	
					geoJSON2WKT(response.file_list[i].topojson[j], response.file_list[i].file_name, topojsonCallback, i, csvFiles);
				},
				function geoJSON2WKTFileTopojsonEnd(err) { //  Callback
	
					if (err) {
						serverLog.serverError2(__file, __line, "geoJSON2WKTFileTopojsonEnd", 
							"geoJSON2WKTFileTopojsonEnd() processing error in geoJSON2WKT()", req, err, response, 
								"Stack >>>\n" + err.stack /* Additional info */);
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
						async.forEachOfSeries(csvFiles, 
							function geoJSON2WKTFileCSVFiles(value, i, csvfileCallback) { // Processing code					
												
								var csvStream;
								var fileNoext;
								var csvFileName;
								try {
									fileNoext = path.basename(response.file_list[i].file_name, ".shp");	
									csvFileName=dir + "/" + fileNoext + ".csv";
									csvStream = fs.createWriteStream(csvFileName, { flags : 'w' });	
									csvStream.on('finish', function csvStreamClose() {
										response.message+="\nsvStreamClose(): " + csvFileName;
									});		
									csvStream.on('error', function csvStreamError(e) {
										serverLog.serverLog2(__file, __line, "csvStreamError", 
											"WARNING: Exception in CSV write to file: " + csvFileName, req, e, response);										
									});
								}
								catch (e) {
									csvfileCallback(e);
								}	
			
								var keys=Object.keys(csvFiles[i].rows[0]);
								var buf;
								
								response.message+="\nCSV file [" + (i+1) + "]: " + (csvFiles[i].file_name||"No file") + "; rows: " + csvFiles[i].rows.length +
									"; " + keys.length + " keys: " + keys.toString();	
								var buf=keys.toString() + "\r\n";
								csvStream.write(buf);
								var l=0;
								
								async.forEachOfSeries(csvFiles[i].rows, 
									function geoJSON2WKTFileCSVSeries(value, j, csvCallback) { // Processing code	
										l++;
										buf=undefined;
										
										for (var key in value) { //csvFiles[i].rows[k][
											var str=value[key].toString();
											// CSV escape data 
											str=str.split('"' /* search: " */).join('""' /* replacement: "" */);
//											str=str.split(',' /* search: , */).join('","' /* replacement: "," */);
											if (buf) {
												buf+=',"' + str + '"';
											}
											else {					
												buf='"' + str + '"';
											}
										}
										buf+="\r\n";
										if (l >= 1000) {
											l=0;
											var nextTickFunc = function nextTick() {
												csvStream.write(buf, csvCallback);
											}
											process.nextTick(nextTickFunc);
										}
										else {
											csvStream.write(buf, csvCallback);
										} 		
									}, // End of geoJSON2WKTFileCSVSeries
									function geoJSON2WKTFileCSVEnd(err) { //  Callback
										var msg="Wrote CSV file " + fileNoext + ".csv";
										var code=200 /* HTTP OK */;
										if (err) {
											code=501;
											msg+="; caught error: " + err.message
										}
										else {
											csvStream.end();
											var stats = fs.statSync(csvFileName);	
											msg+="; size: " + nodeGeoSpatialServicesCommon.fileSize(stats.size);											
										}
										addStatus(__file, __line, response, msg,   // Add created WKT zoomlevel topojson status	
											code, serverLog, undefined /* req */, 
											function geoJSON2WKTFileCSVEndAddStatus(err2) {
												csvfileCallback(err || err2); // Run callback	
											}
										);
									} // End of geoJSON2WKTFileCSVEnd()
								); // End of async.forEachOfSeries()
							}, // End of geoJSON2WKTFileCSVFiles
							function geoJSON2WKTFileCSVFilesEnd(err) { //  Callback
								if (err) {
									endCallback(err); // Run end callback				
								}
								else {
									try {
										dbLoad.CreateDbLoadScripts(response, xmlConfig, req, res, dir, csvFiles, endCallback); // Create DB load scripts; 
														// Then run end callback
									}
									catch (err) {
										endCallback(err); // Run end callback				
									}
								}
							} // End of geoJSON2WKTFileCSVFilesEnd()
						); // End of for csvFiles[] async loop
					} // End of geoJSON2WKTFileAddStatus()
				);	// End of addStatus()
			}
		} // End of geoJSON2WKTFileEnd()
	);	// End of async.forEachOfSeries()	
											
} // End of geojsonToCSV()

module.exports.geojsonToCSV = geojsonToCSV;

// Eof
