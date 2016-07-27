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
	  serverLog = require('../lib/serverLog');
	  httpErrorResponse = require('../lib/httpErrorResponse');
	  
var geojsonToCSV = function geoJSON2WKT(response, req) {
	
	scopeChecker(__file, __line, {
		response: response,
		message: response.message,
		serverLog: serverLog,
		req: req,
		httpErrorResponse: httpErrorResponse
	});
		
		
	var lstart = new Date().getTime();
					
	/*
	 * Function: 	geoJSON2WKT()
	 * Parameters:	Topojson zoomlevel object, file name
	 * Description:	
	 */	
	function geoJSON2WKT(topojson, fileName) {	
		var wktLen=0;
		var l=0;
		
		if (topojson && topojson.geojson) {
			async.forEachOfSeries(topojson.geojson.features, 
				function geoJSON2WKTSeries(value, k, featureCallback) { // Processing code
					l++;
					try {
						topojson.wkt[k]=wellknown.stringify(topojson.geojson.features[k]);	
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
				function geoJSON2WKTError(err) { //  Callback
			
					if (err) {
						serverLog.serverError2(__file, __line, "geoJSON2WKTAddStatus", 
							"WKT processing error", req, err, response);
					}
					else {
						end = new Date().getTime();
						var msg="Created wellknown text for zoomlevel " + topojson.zoomlevel + 
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
									// Next or callback
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
		
	
	for (var i=0; i<response.file_list.length; i++) {
		for (var j=0; j<response.file_list[i].topojson.length; j++) {
			geoJSON2WKT(response.file_list[i].topojson[j], response.file_list[i].file_name);
		}
	}				
} // End of geojsonToCSV()

module.exports.geojsonToCSV = geojsonToCSV;

// Eof
