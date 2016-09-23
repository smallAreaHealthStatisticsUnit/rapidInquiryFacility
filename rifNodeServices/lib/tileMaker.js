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
	
const async = require('async'),
	  serverLog = require('../lib/serverLog'),
	  nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon'),
	  httpErrorResponse = require('../lib/httpErrorResponse');

const os = require('os'),
	  fs = require('fs'),
	  path = require('path');
	
/*
 * Function: 	tileMaker()
 * Parameters:	Internal response object, HTTP request object, HTTP response object, callback to call at end of processing
 * Description:	
 *				Then call geojsonToCSV() - Convert geoJSON to CSV; save as CSV files; create load scripts for Postgres and MS SQL server
 */		
 
var tileMaker = function tileMaker(response, req, res, endCallback) {

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
		
	const geojsonToCSV = require('../lib/geojsonToCSV');
		
/*
 * SQL statement name: 	longitude2tile.sql
 * Type:				Postgres/PostGIS PL/pgsql function
 * Parameters:			longitude, zoomLevel
 * Returns:				tile number
 *
 * Description:			Convert longitude (WGS84 - 4326) to OSM tile x
 
	Derivation of the tile X/Y 

	* Reproject the coordinates to the Mercator projection (from EPSG:4326 to EPSG:3857):

	x = lon
	y = arsinh(tan(lat)) = log[tan(lat) + sec(lat)]
	(lat and lon are in radians)

	* Transform range of x and y to 0 ? 1 and shift origin to top left corner:

	x = [1 + (x / p)] / 2
	y = [1 - (y / p)] / 2

	* Calculate the number of tiles across the map, n, using 2**zoom
	* Multiply x and y by n. Round results down to give tilex and tiley.

CREATE FUNCTION tileMaker_longitude2tile(@longitude DOUBLE PRECISION, @zoom_level INTEGER)
RETURNS INTEGER AS
BEGIN
	DECLARE @tileX INTEGER;
	SET @tileX=CAST(
			FLOOR( (@longitude + 180) / 360 * POWER(2, @zoom_level) ) AS INTEGER);
	RETURN @tileX;
END;
 */		
	var longitude2tile = function longitude2tile(longitude, zoomLevel) {
		
	}
	
/*
 * SQL statement name: 	latitude2tile.sql
 * Type:				Microsoft SQL Server T/sql function
 * Parameters:			None
 *
 * Description:			Convert latitude (WGS84 - 4326) to OSM tile y

 Derivation of the tile X/Y 

* Reproject the coordinates to the Mercator projection (from EPSG:4326 to EPSG:3857):

x = lon
y = arsinh(tan(lat)) = log[tan(lat) + sec(lat)]
(lat and lon are in radians)

* Transform range of x and y to 0 ? 1 and shift origin to top left corner:

x = [1 + (x / p)] / 2
y = [1 - (y / p)] / 2

* Calculate the number of tiles across the map, n, using 2**zoom
* Multiply x and y by n. Round results down to give tilex and tiley. 

CREATE FUNCTION tileMaker_latitude2tile(@latitude DOUBLE PRECISION, @zoom_level INTEGER)
RETURNS INTEGER 
AS
BEGIN
	DECLARE @tileY INTEGER;
	SET @tileY=CAST(
					FLOOR( 
						(1.0 - LOG /- Natural Log -/ 
							(TAN(RADIANS(@latitude)) + 1.0 / COS(RADIANS(@latitude))) / PI()) / 2.0 * POWER(2, @zoom_level) 
						) 
					AS INTEGER);
	RETURN @tileY;
END;
 */ 
 	var latitude2tile = function latitude2tile(longitude, zoomLevel) {
		
	}
	
	for (var i=0; i<response.file_list.length; i++) { 
//
// Get tile max/min lat/long from bounding box
//
		var msg="tileMaker() file [" + i + "/" + response.file_list.length + "]: " + 
			(response.file_list[i].file_name || "No file name") + 
			"; geolevel_id: " + (response.file_list[i].geolevel_id|| "No geolevel_id") + 
			"; zoom levels: " + (response.file_list[i].topojson.length|| "No zoomlevels") +
			"; min zoomlevel: " + response.file_list[i].topojson[(response.file_list[i].topojson.length-1)].zoomlevel +
			"; max zoomlevel: " + response.file_list[i].topojson[0].zoomlevel +
			"; bounding box (4326): " + 
			"xmin: " + response.file_list[i].bbox[0] + ", " +
			"ymin: " + response.file_list[i].bbox[1] + ", " +
			"xmax: " + response.file_list[i].bbox[2] + ", " +
			"ymax: " + response.file_list[i].bbox[3] + "];";
		console.error("TRACE: " + msg);
		response.message+=msg;
	}
	addStatus(__file, __line, response, "Created tiles", 200 /* HTTP OK */, serverLog, req, // Add status
		function addStatusTilesCallback(err) { 	
			if (err) {						
				serverLog.serverLog2(__file, __line, "addStatusTilesCallback", "ERROR! in adding tiles", req, e);
			}
	
			try {
				// Call geojsonToCSV() - Convert geoJSON to CSV; save as CSV files; create load scripts for Postgres and MS SQL server
				geojsonToCSV.geojsonToCSV(response, req, res, endCallback); // Convert geoJSON to CSV
			}
			catch (e) {	
				serverLog.serverError2(__file, __line, "shapeFileQueueDrain", 
					"Exception thrown by tileMaker.tileMaker() ", req, e, response);	
			}	
	
		});
											
} // End of tileMaker()

module.exports.tileMaker = tileMaker;

// Eof